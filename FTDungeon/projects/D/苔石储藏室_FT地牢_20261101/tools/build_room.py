import collections
import dataclasses
import gzip
import hashlib
import io
import json
import struct
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SIZE = (11, 9, 15)
ENTRANCE = (5, 2, 0)
INTERIOR = (5, 2, 7)
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-room-smugglers-passage-20261101/out/dungeon-room-smugglers-passage-hero.png")
BLOCKS = {}


class Byte(int):
    pass


@dataclasses.dataclass
class Block:
    name: str
    properties: dict
    data: dict | None = None

    def state(self):
        result = {"Name": "minecraft:" + self.name}
        if self.properties:
            result["Properties"] = self.properties
        return result


def put(block_x, block_y, block_z, name, data=None, **properties):
    assert 0 <= block_x < SIZE[0] and 0 <= block_y < SIZE[1] and 0 <= block_z < SIZE[2]
    BLOCKS[block_x, block_y, block_z] = Block(name, {key: str(value).lower() for key, value in properties.items()}, data)


def cuboid(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def noise(block_x, block_y, block_z):
    return abs(block_x * 73856093 ^ block_y * 19349663 ^ block_z * 83492791) % 100


def masonry(block_x, block_y, block_z):
    selector = noise(block_x, block_y, block_z)
    moss_threshold = 36 if block_y < 4 else 24
    if selector < moss_threshold:
        return "mossy_stone_bricks"
    if selector < moss_threshold + 16:
        return "cracked_stone_bricks"
    if selector > 92:
        return "cobblestone"
    return "stone_bricks"


def shell():
    cuboid((0, 0, 0), (10, 8, 14), "air")
    cuboid((0, 0, 0), (10, 0, 14), "stone_bricks")
    for block_x in range(11):
        for block_z in range(15):
            put(block_x, 1, block_z, masonry(block_x, 1, block_z))
            put(block_x, 7, block_z, masonry(block_x, 7, block_z))
            if block_x in (0, 10) or block_z in (0, 14):
                put(block_x, 8, block_z, "mossy_stone_brick_slab" if noise(block_x, 8, block_z) < 40 else "stone_brick_slab", type="bottom", waterlogged=False)
                for block_y in range(2, 7):
                    put(block_x, block_y, block_z, masonry(block_x, block_y, block_z))
    for block_z in (0, 4, 9, 14):
        for block_x in (0, 10):
            cuboid((block_x, 2, block_z), (block_x, 6, block_z), "spruce_log", axis="y")
        cuboid((0, 6, block_z), (10, 6, block_z), "spruce_log", axis="x")
    for block_x in (0, 10):
        cuboid((block_x, 6, 0), (block_x, 6, 14), "spruce_log", axis="z")
    cuboid((5, 2, 14), (5, 6, 14), "spruce_log", axis="y")
    for block_x in (3, 7):
        cuboid((block_x, 2, 0), (block_x, 4, 0), "stone_bricks")
    cuboid((3, 5, 0), (7, 5, 0), "spruce_log", axis="x")
    cuboid((4, 2, 0), (6, 4, 0), "air")


def barrel(block_x, block_y, block_z, facing="north"):
    data = {"id": "minecraft:barrel", "Items": [], "CustomName": json.dumps({"text": "旧商栈空木箱"}, ensure_ascii=False)}
    put(block_x, block_y, block_z, "barrel", data, facing=facing, open=False)


def furniture():
    for position in ((1, 2, 2), (2, 2, 2), (1, 3, 2), (1, 2, 3), (1, 2, 7), (2, 2, 7), (1, 2, 8), (9, 2, 2), (8, 2, 2), (9, 3, 2), (9, 2, 3)):
        barrel(*position, facing="east" if position[0] < 5 else "west")
    for block_x in range(7, 10):
        for block_z in (12, 13):
            barrel(block_x, 2, block_z)
            put(block_x, 3, block_z, "spruce_planks")
    put(7, 3, 12, "red_wool")
    put(7, 4, 12, "red_carpet")
    put(8, 4, 13, "chest", {"id": "minecraft:chest", "Items": [], "CustomName": json.dumps({"text": "守卫旧物箱"}, ensure_ascii=False)}, facing="north", type="single", waterlogged=False)
    put(9, 4, 12, "lantern", hanging=False, waterlogged=False)
    for block_x, block_z in ((7, 13), (8, 12)):
        put(block_x, 4, block_z, "light_weighted_pressure_plate", power=0)
    cuboid((8, 2, 6), (9, 2, 6), "spruce_slab", type="bottom", waterlogged=False)
    for block_x, block_z in ((2, 5), (3, 5), (2, 6), (3, 6), (7, 8), (8, 8), (7, 9), (8, 9), (2, 11)):
        put(block_x, 1, block_z, "water", level=0)


def atmosphere():
    for block_x, block_z in ((3, 4), (7, 9)):
        put(block_x, 5, block_z, "chain", axis="y", waterlogged=False)
        put(block_x, 4, block_z, "lantern", hanging=True, waterlogged=False)
    for position in ((1, 5, 13), (9, 5, 11), (8, 5, 13)):
        put(*position, "cobweb")
    for block_x, block_z in ((7, 13), (8, 13), (9, 12)):
        put(block_x, 6, block_z, "hanging_roots", waterlogged=False)
    for block_y in (4, 5):
        put(9, block_y, 7, "vine", east=True, west=False, north=False, south=False, up=False)
    put(1, 5, 11, "vine", east=False, west=True, north=False, south=False, up=False)
    for block_x, block_z in ((1, 9), (9, 10), (3, 12)):
        put(block_x, 2, block_z, "moss_carpet")
    messages = [json.dumps({"text": text}, ensure_ascii=False) for text in ("商栈旧库", "请循干道", "北口返厅", "勿扰旧物")]
    front = {"messages": messages, "color": "black", "has_glowing_text": Byte(0)}
    back = {"messages": [json.dumps({"text": ""})] * 4, "color": "black", "has_glowing_text": Byte(0)}
    put(1, 3, 5, "spruce_wall_sign", {"id": "minecraft:sign", "front_text": front, "back_text": back, "is_waxed": Byte(1)}, facing="east", waterlogged=False)


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
    result = read_nbt(stream, 10)
    assert stream.read() == b""
    return result


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_structure():
    palette, indexes, entries = [], {}, []
    for position, block in sorted(BLOCKS.items()):
        state = block.state()
        key = json.dumps(state, sort_keys=True)
        if key not in indexes:
            indexes[key] = len(palette)
            palette.append(state)
        entry = {"pos": list(position), "state": indexes[key]}
        if block.data is not None:
            entry["nbt"] = block.data
        entries.append(entry)
    template = {"DataVersion": 3465, "size": list(SIZE), "palette": palette, "blocks": entries, "entities": []}
    target = ROOT / "out/room.nbt"
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(gzip.compress(b"\x0a\x00\x00" + nbt_payload(template)[1], mtime=0))
    assert load_structure(target) == template
    return target, template


def full_cube(block):
    if block is None:
        return False
    return block.name in {"stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "cobblestone", "spruce_log", "spruce_planks", "red_wool", "barrel"}


def dry_walkable(position):
    block_x, block_y, block_z = position
    if not (0 <= block_x < 11 and 0 <= block_z < 15 and block_y == 2):
        return False
    return full_cube(BLOCKS.get((block_x, block_y - 1, block_z))) and all(BLOCKS.get((block_x, block_y + offset, block_z)).name in {"air", "vine", "spruce_wall_sign", "moss_carpet"} for offset in (0, 1))


def verify_geometry(structure):
    failures = []
    if len(BLOCKS) != SIZE[0] * SIZE[1] * SIZE[2]:
        failures.append("Incomplete explicit-air bounding volume")
    entrance_clear = all(BLOCKS[block_x, block_y, 0].name == "air" for block_x in (4, 5, 6) for block_y in (2, 3, 4))
    if not entrance_clear:
        failures.append("North 3x3 entrance is blocked")
    dry_trunk = all(dry_walkable((block_x, 2, block_z)) for block_x in (4, 5, 6) for block_z in range(12))
    if not dry_trunk:
        failures.append("Three-wide dry central route is blocked")
    roof_closed = all(full_cube(BLOCKS[block_x, 7, block_z]) for block_x in range(11) for block_z in range(15))
    if not roof_closed:
        failures.append("Roof has a hole")
    reached = {ENTRANCE}
    pending = collections.deque([ENTRANCE])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (block_x + delta_x, block_y, block_z + delta_z)
            if neighbor not in reached and dry_walkable(neighbor):
                reached.add(neighbor)
                pending.append(neighbor)
    anchors = {"interior": INTERIOR, "treasure_approach": (6, 2, 12), "crate_approach": (3, 2, 3), "sign_approach": (2, 2, 4)}
    anchor_results = {name: position in reached for name, position in anchors.items()}
    if not all(anchor_results.values()):
        failures.append("An interaction anchor is unreachable")
    waters = []
    decorations = []
    for position, block in BLOCKS.items():
        block_x, block_y, block_z = position
        if block.name == "water":
            waters.append(position)
            if not full_cube(BLOCKS.get((block_x, block_y - 1, block_z))):
                failures.append(f"Water has no basin bottom: {position}")
            for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                adjacent = BLOCKS.get((block_x + delta_x, block_y, block_z + delta_z))
                if not adjacent or (adjacent.name != "water" and not full_cube(adjacent)):
                    failures.append(f"Water can escape: {position}")
        if block.name in {"lantern", "hanging_roots", "red_carpet", "moss_carpet", "light_weighted_pressure_plate"}:
            above = BLOCKS.get((block_x, block_y + 1, block_z))
            below = BLOCKS.get((block_x, block_y - 1, block_z))
            supported = full_cube(above) if block.name == "hanging_roots" else full_cube(above) or (above and above.name == "chain") if block.name == "lantern" and block.properties["hanging"] == "true" else full_cube(below)
            decorations.append({"position": list(position), "block": block.name, "supported": bool(supported)})
            if not supported:
                failures.append(f"Unsupported decoration: {position} {block.name}")
        if block.data is not None and (block.data.get("Items") or "LootTable" in block.data):
            failures.append(f"Unexpected reward contents: {position}")
        if "door" in block.name or block.name in {"spawner", "command_block", "structure_block", "jigsaw"}:
            failures.append(f"Unexpected gate/runtime block: {position} {block.name}")
    result = {
        "status": "FAIL" if failures else "PASS", "id": "S2", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR),
        "entrance_clear_size": [3, 3], "entrance_open": entrance_clear, "dry_trunk_width": 3, "dry_trunk_depth": 12, "dry_trunk": dry_trunk,
        "roof_closed": roof_closed, "roof_solid_columns": 165, "explicit_volume_records": len(BLOCKS), "non_air_blocks": sum(block.name != "air" for block in BLOCKS.values()),
        "reachable_dry_positions": len(reached), "dry_routes": anchor_results, "water_sources": len(waters), "contained_water": not any("Water" in failure for failure in failures),
        "supported_decorations": decorations, "containers": sum(block.name in {"barrel", "chest"} for block in BLOCKS.values()), "reward_items": 0, "entities": len(structure["entities"]), "failures": failures,
        "route_model": "Full-height dry floor with two unobstructed player blocks; no doors, jumps, swimming or pressure-plate assumptions",
    }
    write_json(ROOT / "qa/geometry.json", result)
    if failures:
        raise AssertionError(failures)
    return result


def main():
    BLOCKS.clear()
    shell()
    furniture()
    atmosphere()
    target, structure = write_structure()
    digest = hashlib.sha256(target.read_bytes()).hexdigest()
    geometry = verify_geometry(structure)
    metadata = {"id": "S2", "name": "苔石储藏室·商栈密道", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "sha256": digest, "reference": str(REFERENCE)}
    write_json(ROOT / "out/room.json", metadata)
    write_json(ROOT / "out/materials.json", dict(collections.Counter(block.name for block in BLOCKS.values() if block.name != "air").most_common()))
    write_json(ROOT / "qa/reference.json", {"path": str(REFERENCE), "sha256": hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), "paid_image_requests": 0})
    geometry["structure_sha256"] = digest
    write_json(ROOT / "qa/geometry.json", geometry)
    print(json.dumps({"structure": str(target), "size": list(SIZE), "palette": len(structure["palette"]), "geometry": geometry["status"], "sha256": digest}, ensure_ascii=False))


if __name__ == "__main__":
    main()
