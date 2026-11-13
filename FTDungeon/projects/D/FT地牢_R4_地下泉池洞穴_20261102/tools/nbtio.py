from __future__ import annotations

import gzip
import io
import struct
from dataclasses import dataclass, field
from pathlib import Path


@dataclass
class Tag:
    kind: int
    value: object
    subtype: int = 0


NUMBER_FORMATS = {1: ">b", 2: ">h", 3: ">i", 4: ">q", 5: ">f", 6: ">d"}
ARRAY_FORMATS = {7: ">b", 11: ">i", 12: ">q"}


def take(stream, count):
    if count < 0:
        raise ValueError("Negative NBT length")
    payload = stream.read(count)
    if len(payload) != count:
        raise ValueError("Truncated NBT")
    return payload


def read_string(stream):
    length = struct.unpack(">H", take(stream, 2))[0]
    decoded = take(stream, length).replace(b"\xc0\x80", b"\0").decode("utf-8", "surrogatepass")
    return decoded.encode("utf-16", "surrogatepass").decode("utf-16", "surrogatepass")


def read_payload(stream, kind):
    if kind in NUMBER_FORMATS:
        number_format = NUMBER_FORMATS[kind]
        return Tag(kind, struct.unpack(number_format, take(stream, struct.calcsize(number_format)))[0])
    if kind == 8:
        return Tag(kind, read_string(stream))
    if kind in ARRAY_FORMATS:
        count = struct.unpack(">i", take(stream, 4))[0]
        array_format = ARRAY_FORMATS[kind]
        payload = take(stream, count * struct.calcsize(array_format))
        return Tag(kind, [item[0] for item in struct.iter_unpack(array_format, payload)])
    if kind == 9:
        subtype, count = struct.unpack(">bi", take(stream, 5))
        if count < 0 or (subtype == 0 and count):
            raise ValueError("Invalid NBT list")
        return Tag(kind, [read_payload(stream, subtype) for _ in range(count)], subtype)
    if kind == 10:
        children = {}
        while True:
            subtype = take(stream, 1)[0]
            if subtype == 0:
                return Tag(kind, children)
            name = read_string(stream)
            if name in children:
                raise ValueError(f"Duplicate NBT name: {name}")
            children[name] = read_payload(stream, subtype)
    raise ValueError(f"Unknown NBT tag: {kind}")


def encode_string(value):
    code_units = value.encode("utf-16-be", "surrogatepass")
    encoded = "".join(chr(entry[0]) for entry in struct.iter_unpack(">H", code_units)).encode("utf-8", "surrogatepass").replace(b"\0", b"\xc0\x80")
    return struct.pack(">H", len(encoded)) + encoded


def encode_payload(tag):
    if tag.kind in NUMBER_FORMATS:
        return struct.pack(NUMBER_FORMATS[tag.kind], tag.value)
    if tag.kind == 8:
        return encode_string(tag.value)
    if tag.kind in ARRAY_FORMATS:
        return struct.pack(">i", len(tag.value)) + b"".join(struct.pack(ARRAY_FORMATS[tag.kind], value) for value in tag.value)
    if tag.kind == 9:
        return struct.pack(">bi", tag.subtype, len(tag.value)) + b"".join(encode_payload(child) for child in tag.value)
    if tag.kind == 10:
        return b"".join(bytes([child.kind]) + encode_string(name) + encode_payload(child) for name, child in tag.value.items()) + b"\0"
    raise ValueError(f"Unknown NBT tag: {tag.kind}")


def read_nbt(path):
    payload = Path(path).read_bytes()
    if payload[:2] == b"\x1f\x8b":
        payload = gzip.decompress(payload)
    stream = io.BytesIO(payload)
    kind = take(stream, 1)[0]
    name = read_string(stream)
    root = read_payload(stream, kind)
    if stream.read():
        raise ValueError("Trailing NBT bytes")
    return name, root


def write_nbt(path, root, name=""):
    payload = bytes([root.kind]) + encode_string(name) + encode_payload(root)
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_bytes(gzip.compress(payload, compresslevel=6, mtime=0))


def plain(tag):
    if tag.kind == 10:
        return {name: plain(child) for name, child in tag.value.items()}
    if tag.kind == 9:
        return [plain(child) for child in tag.value]
    return tag.value


def compound(**values):
    return Tag(10, values)


def integer(value):
    return Tag(3, int(value))


def string(value):
    return Tag(8, str(value))


def intlist(values):
    return Tag(9, [integer(value) for value in values], 3)


@dataclass(frozen=True)
class State:
    name: str
    properties: tuple = ()

    def get(self, name, default=None):
        return dict(self.properties).get(name, default)

    def as_tag(self):
        value = {"Name": string(self.name)}
        if self.properties:
            value["Properties"] = Tag(10, {name: string(entry) for name, entry in self.properties})
        return Tag(10, value)

    @classmethod
    def from_tag(cls, tag):
        value = tag.value
        properties = value.get("Properties", Tag(10, {}))
        return cls(value["Name"].value, tuple(sorted((name, entry.value) for name, entry in properties.value.items())))


def state(name, **properties):
    if ":" not in name:
        name = "minecraft:" + name
    return State(name, tuple(sorted((key, str(value).lower()) for key, value in properties.items())))


AIR = state("air")


@dataclass
class Structure:
    size: tuple
    blocks: dict = field(default_factory=dict)
    block_entities: dict = field(default_factory=dict)
    entities: list = field(default_factory=list)
    data_version: int = 3465

    @classmethod
    def read(cls, path):
        _, root = read_nbt(path)
        values = root.value
        palette_tag = values.get("palette")
        if palette_tag is None:
            palette_tag = values["palettes"].value[0]
        palette = [State.from_tag(entry) for entry in palette_tag.value]
        result = cls(tuple(plain(values["size"])), data_version=values["DataVersion"].value)
        for entry in values["blocks"].value:
            position = tuple(plain(entry.value["pos"]))
            if position in result.blocks:
                raise ValueError(f"Duplicate block position: {position}")
            result.blocks[position] = palette[entry.value["state"].value]
            if "nbt" in entry.value:
                result.block_entities[position] = entry.value["nbt"]
        result.entities = values.get("entities", Tag(9, [], 10)).value
        return result

    def write(self, path):
        palette = []
        state_indices = {}
        ordered = sorted(self.blocks.items(), key=lambda entry: (entry[0][1], entry[0][2], entry[0][0]))
        for position, block_state in ordered:
            if block_state not in state_indices:
                state_indices[block_state] = len(palette)
                palette.append(block_state.as_tag())
        stream = io.BytesIO()
        stream.write(b"\x0a\x00\x00")
        for name, tag in {"size": intlist(self.size), "entities": Tag(9, self.entities, 10), "palette": Tag(9, palette, 10), "DataVersion": integer(self.data_version)}.items():
            stream.write(bytes([tag.kind]) + encode_string(name) + encode_payload(tag))
        stream.write(b"\x09" + encode_string("blocks") + struct.pack(">bi", 10, len(ordered)))
        for position, block_state in ordered:
            values = {"pos": intlist(position), "state": integer(state_indices[block_state])}
            if position in self.block_entities:
                values["nbt"] = self.block_entities[position]
            stream.write(encode_payload(Tag(10, values)))
        stream.write(b"\0")
        Path(path).parent.mkdir(parents=True, exist_ok=True)
        Path(path).write_bytes(gzip.compress(stream.getvalue(), compresslevel=6, mtime=0))

    def set(self, position, block_state, block_entity=None):
        if not all(0 <= coordinate < limit for coordinate, limit in zip(position, self.size)):
            raise ValueError(f"Block outside structure: {position} / {self.size}")
        self.blocks[position] = block_state
        self.block_entities.pop(position, None)
        if block_entity is not None:
            self.block_entities[position] = block_entity

    def get(self, position):
        return self.blocks.get(position, AIR)

    def box(self, start, end, block_state):
        for pos_y in range(start[1], end[1] + 1):
            for pos_z in range(start[2], end[2] + 1):
                for pos_x in range(start[0], end[0] + 1):
                    self.set((pos_x, pos_y, pos_z), block_state)

