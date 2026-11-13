import gzip
import io
import json
import struct
from pathlib import Path


class Byte(int):
    pass


def encode_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def nbt_payload(value):
    if isinstance(value, Byte):
        return 1, struct.pack(">b", value)
    if isinstance(value, dict):
        payload = bytearray()
        for key, child in value.items():
            kind, encoded = nbt_payload(child)
            payload.extend(bytes([kind]) + encode_string(key) + encoded)
        return 10, bytes(payload) + b"\x00"
    if isinstance(value, list):
        entries = [nbt_payload(child) for child in value]
        kind = entries[0][0] if entries else 0
        assert all(entry[0] == kind for entry in entries)
        return 9, bytes([kind]) + struct.pack(">i", len(entries)) + b"".join(entry[1] for entry in entries)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, str):
        return 8, encode_string(value)
    raise TypeError(type(value))


def read_string(stream):
    return stream.read(struct.unpack(">H", stream.read(2))[0]).decode("utf-8")


def read_nbt(stream, kind):
    if kind == 1:
        return Byte(struct.unpack(">b", stream.read(1))[0])
    if kind == 3:
        return struct.unpack(">i", stream.read(4))[0]
    if kind == 8:
        return read_string(stream)
    if kind == 9:
        element = stream.read(1)[0]
        return [read_nbt(stream, element) for _ in range(struct.unpack(">i", stream.read(4))[0])]
    if kind == 10:
        result = {}
        while True:
            element = stream.read(1)[0]
            if element == 0:
                return result
            name = read_string(stream)
            result[name] = read_nbt(stream, element)
    raise ValueError(kind)


def load_structure(path):
    stream = io.BytesIO(gzip.decompress(path.read_bytes()))
    assert stream.read(1) == b"\x0a"
    read_string(stream)
    return read_nbt(stream, 10)


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")

