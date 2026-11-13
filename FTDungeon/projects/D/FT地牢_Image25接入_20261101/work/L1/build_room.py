import collections
import dataclasses
import gzip
import hashlib
import json
import struct
from pathlib import Path


WORK = Path(__file__).resolve().parent
DELIVERY = WORK.parents[1] / "incoming/L1"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-room-obsidian-trial-hall-20261101/out/dungeon-room-obsidian-trial-hall-hero.png")
SIZE = (25, 12, 25)
ENTRANCE = (12, 2, 0)
INTERIOR = (12, 2, 4)
BLOCKS = {}


@dataclasses.dataclass
class Block:
    name: str
    properties: dict

    def state(self):
        value = {"Name": "minecraft:" + self.name}
        if self.properties:
            value["Properties"] = self.properties
        return value


def put(block_x, block_y, block_z, name, **properties):
    BLOCKS[block_x, block_y, block_z] = Block(name, {key: str(value).lower() for key, value in properties.items()})


def cuboid(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def stairs(block_x, block_y, block_z, facing):
    put(block_x, block_y, block_z, "polished_blackstone_brick_stairs", facing=facing, half="bottom", shape="straight", waterlogged=False)


def roof_height(block_x, block_z):
    distance = max(abs(block_x - 12), abs(block_z - 12))
    return 9 + min(2, (12 - distance) // 4)


def build_floor():
    cuboid((0, 0, 0), (24, 1, 24), "polished_blackstone_bricks")
    for block_x in range(1, 24):
        for block_z in range(1, 24):
            delta_x, delta_z = abs(block_x - 12), abs(block_z - 12)
            noise = (block_x * 47 + block_z * 29) % 101
            material = "polished_blackstone" if delta_x <= 1 or delta_z <= 1 else "obsidian" if noise < 40 else "polished_blackstone_bricks"
            if noise < 5 and delta_x > 2 and delta_z > 2:
                material = "gilded_blackstone"
            put(block_x, 1, block_z, material)
            radius = max(delta_x, delta_z) + min(delta_x, delta_z) * 0.5
            bridge = delta_x <= 1 or delta_z <= 1
            if 7.5 <= radius <= 9.5 and not bridge:
                put(block_x, 0, block_z, "obsidian")
                put(block_x, 1, block_z, "lava", level=0)
            elif 6.5 <= radius < 7.5 or 9.5 < radius <= 10.5:
                if not bridge:
                    put(block_x, 1, block_z, "chiseled_polished_blackstone")
            if max(delta_x, delta_z) <= 4 and delta_x + delta_z <= 7:
                edge = max(delta_x, delta_z) == 4 or delta_x + delta_z == 7
                put(block_x, 2, block_z, "gilded_blackstone" if edge else "polished_blackstone")
    for offset in range(-1, 2):
        stairs(12 + offset, 2, 7, "south")
        stairs(12 + offset, 2, 17, "north")
        stairs(7, 2, 12 + offset, "east")
        stairs(17, 2, 12 + offset, "west")


def build_shell():
    for block_x in range(25):
        for block_z in range(25):
            if block_x in (0, 24) or block_z in (0, 24):
                for block_y in range(2, 9):
                    material = "polished_blackstone_bricks" if block_y in (2, 3, 8) else "obsidian"
                    put(block_x, block_y, block_z, material)
            height = roof_height(block_x, block_z)
            rib = block_x == 12 or block_z == 12 or abs(block_x - 12) == abs(block_z - 12)
            for block_y in (height - 1, height):
                put(block_x, block_y, block_z, "polished_blackstone_bricks" if rib else "deepslate_tiles")
    for wall_offset in (5, 9, 15, 19):
        for block_y in (4, 6):
            for block_x, block_z in ((wall_offset, 0), (wall_offset, 24), (0, wall_offset), (24, wall_offset)):
                put(block_x, block_y, block_z, "crying_obsidian")
    for block_x in range(11, 14):
        for block_y in range(2, 5):
            BLOCKS.pop((block_x, block_y, 0), None)
    for block_x in (10, 14):
        cuboid((block_x, 2, 1), (block_x, 5, 1), "chiseled_polished_blackstone")
        put(block_x, 4, 1, "crying_obsidian")
        put(block_x, 6, 1, "gilded_blackstone")
    cuboid((10, 5, 1), (14, 5, 1), "polished_blackstone_bricks")
    for block_x in (11, 13):
        put(block_x, 6, 0, "crying_obsidian")
    put(12, 6, 0, "amethyst_block")
    for block_x in range(10, 15):
        put(block_x, 7, 0, "gilded_blackstone")


def build_pillars():
    for block_x, block_z in ((4, 4), (20, 4), (4, 20), (20, 20)):
        cuboid((block_x - 1, 2, block_z - 1), (block_x + 1, 2, block_z + 1), "polished_blackstone_bricks")
        cuboid((block_x, 3, block_z), (block_x, 7, block_z), "obsidian")
        for block_y in (4, 5, 6):
            put(block_x, block_y, block_z, "crying_obsidian")
        for block_y in (3, 7):
            put(block_x, block_y, block_z, "gold_block")
        for block_y in range(8, roof_height(block_x, block_z) - 1):
            put(block_x, block_y, block_z, "chain", axis="y", waterlogged=False)


def build_brazier():
    cuboid((10, 3, 10), (14, 3, 14), "polished_blackstone_bricks")
    cuboid((11, 4, 11), (13, 4, 13), "polished_blackstone")
    put(12, 5, 12, "gold_block")
    cuboid((10, 6, 10), (14, 6, 14), "polished_blackstone_bricks")
    for block_x in range(10, 15):
        for block_z in range(10, 15):
            edge = block_x in (10, 14) or block_z in (10, 14)
            put(block_x, 7, block_z, "gold_block" if edge else "netherrack")
            if not edge:
                put(block_x, 8, block_z, "fire", age=0, north=False, south=False, east=False, west=False, up=False)
    for block_x in (10, 14):
        for block_z in (10, 14):
            put(block_x, 4, block_z, "gilded_blackstone")


def build_details():
    for block_x, block_z in ((1, 7), (23, 7), (1, 17), (23, 17), (8, 23), (16, 23)):
        put(block_x, 2, block_z, "chiseled_polished_blackstone")
        put(block_x, 3, block_z, "lantern", hanging=False, waterlogged=False)
    for block_x, block_z in ((2, 7), (22, 7), (2, 17), (22, 17)):
        cuboid((block_x, 2, block_z - 1), (block_x, 2, block_z + 1), "polished_blackstone_bricks")
        for offset in (-1, 0, 1):
            put(block_x, 3, block_z + offset, "iron_bars", north=True, south=True, east=False, west=False, waterlogged=False)
    for block_x in (2, 22):
        put(block_x, 2, 21, "barrel", facing="up", open=False)
    for block_x in (8, 16):
        put(block_x, 2, 22, "chiseled_polished_blackstone")
        put(block_x, 3, 22, "crying_obsidian")
    put(12, 6, 24, "amethyst_block")
    for block_x in (11, 13):
        put(block_x, 6, 24, "crying_obsidian")


def nbt_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def nbt_payload(value):
    if isinstance(value, dict):
        payload = bytearray()
        for key, child in value.items():
            tag_type, encoded = nbt_payload(child)
            payload.extend(bytes([tag_type]) + nbt_string(key) + encoded)
        return 10, bytes(payload) + b"\x00"
    if isinstance(value, list):
        entries = [nbt_payload(child) for child in value]
        tag_type = entries[0][0] if entries else 0
        assert all(entry[0] == tag_type for entry in entries)
        return 9, bytes([tag_type]) + struct.pack(">i", len(entries)) + b"".join(entry[1] for entry in entries)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, str):
        return 8, nbt_string(value)
    raise TypeError(type(value))


def make_template():
    palette, indexes, entries = [], {}, []
    for block_x in range(25):
        for block_y in range(12):
            for block_z in range(25):
                position = (block_x, block_y, block_z)
                block = BLOCKS.get(position, Block("air", {}))
                state = block.state()
                key = json.dumps(state, sort_keys=True)
                if key not in indexes:
                    indexes[key] = len(palette)
                    palette.append(state)
                entries.append({"pos": list(position), "state": indexes[key]})
    return {"DataVersion": 3465, "size": list(SIZE), "palette": palette, "blocks": entries, "entities": []}


def safe_feet(position):
    block_x, block_y, block_z = position
    support = BLOCKS.get((block_x, block_y - 1, block_z))
    excluded = {"lava", "fire", "chain", "lantern", "iron_bars", "barrel"}
    return support is not None and support.name not in excluded and position not in BLOCKS and (block_x, block_y + 1, block_z) not in BLOCKS


def verify_geometry():
    assert all(all(0 <= position[axis] < SIZE[axis] for axis in range(3)) for position in BLOCKS)
    for block_x in range(25):
        for block_z in range(25):
            height = roof_height(block_x, block_z)
            assert all((block_x, block_y, block_z) in BLOCKS for block_y in (height - 1, height))
            assert BLOCKS[block_x, 0, block_z].name not in ("lava", "fire")
    opening = {(block_x, block_y, 0) for block_x in range(11, 14) for block_y in range(2, 5)}
    for block_x in range(25):
        for block_z in range(25):
            if block_x in (0, 24) or block_z in (0, 24):
                for block_y in range(2, 9):
                    position = (block_x, block_y, block_z)
                    assert (position in BLOCKS) == (position not in opening), position
    for block_x in range(11, 14):
        assert BLOCKS[block_x, 1, 0].name not in ("lava", "fire")
        assert safe_feet((block_x, 2, 0))
        for block_z in range(0, 7):
            assert safe_feet((block_x, 2, block_z)), (block_x, block_z)
    for offset in range(-1, 2):
        for distance in range(1, 7):
            for position in ((12 + offset, 2, distance), (12 + offset, 2, 24 - distance), (distance, 2, 12 + offset), (24 - distance, 2, 12 + offset)):
                assert safe_feet(position), ("bridge_clearance", position)
        for position in ((12 + offset, 3, 7), (12 + offset, 3, 17), (7, 3, 12 + offset), (17, 3, 12 + offset)):
            assert safe_feet(position), ("stair_clearance", position)
    lava = [position for position, block in BLOCKS.items() if block.name == "lava"]
    solid_neighbors = {"obsidian", "polished_blackstone", "polished_blackstone_bricks", "chiseled_polished_blackstone", "gilded_blackstone"}
    for block_x, block_y, block_z in lava:
        assert block_y == 1
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)):
            neighbor = BLOCKS.get((block_x + offset[0], block_y + offset[1], block_z + offset[2]))
            assert neighbor is not None and neighbor.name in solid_neighbors | {"lava"}, ("uncontained_lava", (block_x, block_y, block_z))
    reached, pending = {ENTRANCE}, collections.deque([ENTRANCE])
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, 1, -1):
                destination = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                if destination not in reached and safe_feet(destination):
                    reached.add(destination)
                    pending.append(destination)
    anchors = {"entrance": ENTRANCE, "interior": INTERIOR, "north_platform": (12, 3, 9), "south_platform": (12, 3, 15), "west_platform": (9, 3, 12), "east_platform": (15, 3, 12), "north_bridge": (12, 2, 5), "south_bridge": (12, 2, 19), "west_bridge": (5, 2, 12), "east_bridge": (19, 2, 12), "rear_walkway": (12, 2, 22), "west_walkway": (3, 2, 12), "east_walkway": (21, 2, 12)}
    assert all(position in reached for position in anchors.values()), {name: position for name, position in anchors.items() if position not in reached}
    for position, block in BLOCKS.items():
        if block.name == "fire":
            assert BLOCKS[position[0], position[1] - 1, position[2]].name == "netherrack"
    counts = collections.Counter(block.name for block in BLOCKS.values())
    assert counts["barrel"] == 2
    return {"status": "PASS", "size": list(SIZE), "solid_detail_blocks": len(BLOCKS), "template_cells_including_explicit_air": 7500, "block_counts": dict(counts.most_common()), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "north_opening": {"width": 3, "height": 3, "foot_y": 2, "dry_full_block_threshold": True, "inward_three_wide_dry_path_length": 7}, "roof": "all 625 columns have a two-block-thick stepped sealed vault", "walls": "all four boundaries closed except the single 3x3 north opening", "lava": {"sources": len(lava), "y": 1, "closed_bottom_and_sides": True, "bridge_width": 3, "bridges": 4}, "walkability": {"two_air_blocks_clearance": True, "reachable_safe_cells": len(reached), "anchors": {name: list(position) for name, position in anchors.items()}}, "decorative_brazier": {"vanilla_netherrack_fire": True, "ft_interaction": False}, "empty_barrels": 2, "entities": 0, "loot_tables": 0, "spawners": 0, "tick_functions": 0, "source_reference_sha256": hashlib.sha256(REFERENCE.read_bytes()).hexdigest(), "scope": "Deterministic voxel checks; native NBT and registry validation added separately. No game world, deployment, mob/reward logic, paid API calls or FT core changes."}


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    DELIVERY.mkdir(parents=True, exist_ok=True)
    (WORK / "qa").mkdir(parents=True, exist_ok=True)
    build_floor()
    build_shell()
    build_pillars()
    build_brazier()
    build_details()
    geometry = verify_geometry()
    template = make_template()
    path = DELIVERY / "room.nbt"
    path.write_bytes(gzip.compress(b"\x0a\x00\x00" + nbt_payload(template)[1], mtime=0))
    metadata = {"id": "L1", "name": "黑曜试炼大厅·紧凑接入版", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), "reference": str(REFERENCE)}
    write_json(DELIVERY / "room.json", metadata)
    write_json(WORK / "qa/geometry.json", geometry)
    write_json(WORK / "qa/blueprint.json", template)
    print(json.dumps({"directory": str(DELIVERY), "size": list(SIZE), "solid_blocks": len(BLOCKS), "template_cells": len(template["blocks"]), "palette": len(template["palette"]), "sha256": metadata["sha256"], "static_qa": geometry["status"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
