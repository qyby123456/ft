from __future__ import annotations

import hashlib
import json
from collections import Counter, deque
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, integer, plain, state, string


ROOT = Path(__file__).resolve().parents[1]
DELIVERY = ROOT / "delivery/M11"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M11-bone-prayer-chamber-hero.png")
REFERENCE_SHA = "1660fa43b88cd9bd661a580af3bdaa937c860d0f7ac280d64679860b56616045"
SIZE = (15, 12, 17)
ENTRANCE = (7, 2, 0)
INTERIOR = (7, 2, 4)
TARGETS = ((7, 2, 7), (4, 2, 11), (10, 2, 11), (7, 2, 14), (3, 2, 7), (11, 2, 7))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def build():
    room = Structure(SIZE)
    room.box((0, 0, 0), (14, 11, 16), AIR)
    room.box((0, 0, 0), (14, 0, 16), state("deepslate_bricks"))
    for pos_x in range(15):
        for pos_z in range(17):
            value = (pos_x * 31 + pos_z * 17) % 19
            material = "cracked_stone_bricks" if value < 4 else "stone_bricks"
            if pos_x in (0, 14) or pos_z in (0, 16):
                material = "polished_andesite"
            if 6 <= pos_x <= 8 and pos_z <= 7:
                material = "polished_andesite" if pos_x == 7 else "stone_bricks"
            room.set((pos_x, 1, pos_z), state(material))
    for pos_y in range(2, 11):
        for pos_x in range(15):
            for pos_z in range(17):
                if pos_x in (0, 14) or pos_z in (0, 16):
                    material = "cracked_stone_bricks" if (pos_x * 7 + pos_z * 13 + pos_y * 5) % 13 < 3 else "stone_bricks"
                    room.set((pos_x, pos_y, pos_z), state(material))
    room.box((0, 11, 0), (14, 11, 16), state("deepslate_tiles"))
    room.box((6, 3, 16), (8, 8, 16), state("red_terracotta"))
    for pos_y in range(3, 9):
        for pos_x in (5, 9):
            room.set((pos_x, pos_y, 16), state("chiseled_stone_bricks"))
    room.box((6, 2, 0), (8, 4, 0), AIR)
    for pos_x in (5, 9):
        room.box((pos_x, 2, 0), (pos_x, 5, 0), state("polished_andesite"))
        room.set((pos_x, 2, 0), state("chiseled_stone_bricks"))
    room.box((5, 5, 0), (9, 5, 0), state("chiseled_stone_bricks"))
    for pos_z in (1, 8, 15):
        for pos_x in (1, 13):
            room.box((pos_x, 2, pos_z), (pos_x, 8, pos_z), state("polished_andesite"))
            for pos_y in (2, 6):
                room.set((pos_x, pos_y, pos_z), state("chiseled_stone_bricks"))
            room.set((pos_x, 7, pos_z), state("bone_block", axis="y"))
        room.box((2, 8, pos_z), (2, 9, pos_z), state("stone_bricks"))
        room.box((12, 8, pos_z), (12, 9, pos_z), state("stone_bricks"))
        room.box((3, 9, pos_z), (4, 10, pos_z), state("polished_andesite"))
        room.box((10, 9, pos_z), (11, 10, pos_z), state("polished_andesite"))
        room.box((5, 10, pos_z), (9, 10, pos_z), state("stone_bricks"))
    for pos_x, pos_z in ((3, 4), (4, 6), (10, 5), (11, 8), (4, 13), (10, 14), (6, 6), (9, 7), (3, 11), (11, 11)):
        room.set((pos_x, 1, pos_z), state("red_nether_bricks"))
    room.box((5, 2, 9), (9, 2, 13), state("polished_andesite"))
    for pos_x in (6, 7, 8):
        room.set((pos_x, 2, 8), state("stone_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged="false"))
    room.box((6, 3, 10), (8, 3, 11), state("red_wool"))
    for pos_x in (6, 8):
        room.set((pos_x, 3, 11), state("bone_block", axis="y"))
    room.set((7, 3, 12), state("cauldron"))
    skulls = ((6, 4, 10), (8, 4, 10), (2, 2, 2), (12, 2, 2), (2, 2, 13), (12, 2, 12))
    for index, position in enumerate(skulls):
        room.set(position, state("skeleton_skull", rotation=(8 if index < 2 else index * 3) % 16), compound(id=string("minecraft:skull")))
    for pos_x in (2, 3, 11, 12):
        room.set((pos_x, 2, 15), state("bone_block", axis="x"))
        room.set((pos_x, 3, 15), state("chiseled_stone_bricks"))
        room.set((pos_x, 4, 15), state("skeleton_skull", rotation="8"), compound(id=string("minecraft:skull")))
        room.set((pos_x, 5, 15), state("stone_bricks"))
        room.set((pos_x, 6, 15), state("skeleton_skull", rotation="8"), compound(id=string("minecraft:skull")))
    for pos_x, pos_z in ((4, 4), (10, 4), (4, 8), (10, 8)):
        room.set((pos_x, 2, pos_z), state("chiseled_stone_bricks"))
        room.set((pos_x, 3, pos_z), state("candle", candles="3", lit="true", waterlogged="false"))
    for pos_x in (1, 13):
        for pos_z in (3, 10):
            room.set((pos_x, 3, pos_z), state("chiseled_stone_bricks"))
            room.set((pos_x, 2, pos_z), state("stone_bricks"))
            room.set((pos_x, 4, pos_z), state("candle", candles="2", lit="true", waterlogged="false"))
    for pos_x in (6, 8):
        room.set((pos_x, 4, 11), state("candle", candles="2", lit="true", waterlogged="false"))
    room.set((7, 4, 10), state("red_carpet"))
    for pos_x in (3, 11):
        room.set((pos_x, 2, 13), state("polished_andesite"))
        room.set((pos_x, 3, 13), state("chiseled_stone_bricks"))
    room.set((3, 4, 13), state("candle", candles="4", lit="true", waterlogged="false"))
    room.set((11, 4, 13), state("soul_lantern", hanging="false", waterlogged="false"))
    patterns = Tag(9, [compound(Pattern=string("sku"), Color=integer(0)), compound(Pattern=string("bo"), Color=integer(15))], 10)
    for position, facing in (((7, 7, 15), "north"), ((1, 6, 5), "east"), ((13, 6, 5), "west"), ((1, 6, 12), "east"), ((13, 6, 12), "west")):
        room.set(position, state("red_wall_banner", facing=facing), compound(id=string("minecraft:banner"), Patterns=patterns))
    for position in ((6, 7, 8), (7, 7, 8), (8, 7, 8), (7, 7, 7), (7, 7, 9)):
        room.set(position, state("chiseled_deepslate"))
    for pos_y in (8, 9):
        room.set((7, pos_y, 8), state("chain", axis="y", waterlogged="false"))
    for pos_x, pos_z in ((6, 8), (8, 8), (7, 7), (7, 9)):
        room.set((pos_x, 6, pos_z), state("lantern", hanging="true", waterlogged="false"))
        room.set((pos_x, 8, pos_z), state("candle", candles="1", lit="true", waterlogged="false"))
    for position in ((3, 7, 15), (11, 7, 15)):
        room.set(position, state("cobweb"))
    return room


def static_qa(room):
    def full(position):
        name = room.get(position).name.removeprefix("minecraft:")
        return name not in {"air", "candle", "skeleton_skull", "lantern", "soul_lantern", "chain", "red_wall_banner", "red_carpet", "cobweb", "cauldron"} and not name.endswith("_stairs")

    def walkable(position):
        pos_x, pos_y, pos_z = position
        clear = all(room.get((pos_x, pos_y + height, pos_z)) == AIR for height in (0, 1))
        below = (pos_x, pos_y - 1, pos_z)
        return clear and (full(below) or room.get(below).name.endswith("_stairs"))

    visited = {ENTRANCE}
    queue = deque([ENTRANCE])
    while queue:
        pos_x, pos_y, pos_z = queue.popleft()
        for delta_x, delta_z in ((0, 1), (1, 0), (0, -1), (-1, 0)):
            for delta_y in (0, 1, -1):
                candidate = (pos_x + delta_x, pos_y + delta_y, pos_z + delta_z)
                if all(0 <= coordinate < limit for coordinate, limit in zip(candidate, SIZE)) and candidate not in visited and walkable(candidate):
                    visited.add(candidate)
                    queue.append(candidate)
    assert INTERIOR in visited and all(position in visited for position in TARGETS)
    for pos_x in range(6, 9):
        for pos_z in range(7):
            assert full((pos_x, 1, pos_z))
            assert all(room.get((pos_x, pos_y, pos_z)) == AIR for pos_y in range(2, 5))
    supports = Counter()
    reverse = {"north": (0, 0, 1), "south": (0, 0, -1), "east": (-1, 0, 0), "west": (1, 0, 0)}
    for position, block in room.blocks.items():
        pos_x, pos_y, pos_z = position
        name = block.name.removeprefix("minecraft:")
        if name in {"candle", "skeleton_skull", "red_carpet"} or name.endswith("lantern"):
            direction = 1 if block.get("hanging") == "true" else -1
            assert full((pos_x, pos_y + direction, pos_z)), (position, name, "support")
            supports[name] += 1
        if name == "red_wall_banner":
            delta = reverse[block.get("facing")]
            assert full(tuple(position[axis] + delta[axis] for axis in range(3)))
            supports[name] += 1
        if name == "chain":
            anchor = (pos_x, pos_y + 1, pos_z)
            while room.get(anchor).name == "minecraft:chain":
                anchor = (anchor[0], anchor[1] + 1, anchor[2])
            assert full(anchor)
            supports[name] += 1
    assert all(full((pos_x, 11, pos_z)) for pos_x in range(15) for pos_z in range(17))
    assert len(room.blocks) == 3060 and room.entities == []
    forbidden = {"spawner", "command_block", "chain_command_block", "repeating_command_block", "jigsaw", "structure_block", "structure_void", "chest", "barrel", "dispenser", "dropper", "hopper", "lava", "water", "fire", "soul_fire"}
    assert all(block.name.removeprefix("minecraft:") not in forbidden for block in room.blocks.values())
    payload = json.dumps([plain(entity) for entity in room.block_entities.values()])
    assert all(key not in payload for key in ('"Items"', '"LootTable"', '"Book"', '"Command"'))
    return {"status": "PASS", "explicit_volume_records": len(room.blocks), "non_air_blocks": sum(block != AIR for block in room.blocks.values()), "reachable_feet_positions": len(visited), "landmark_routes": len(TARGETS), "targets": TARGETS, "support_checks": dict(supports), "roof_full_blocks": 255, "north_interface": {"width": 3, "height": 3, "clear_depth": 7, "full_floor": True}, "template_entities": 0, "reward_inventory": 0, "loot_tables": 0, "fluids": 0, "open_fire": 0, "static_only": True}


def main():
    assert digest(REFERENCE) == REFERENCE_SHA, "Reference mismatch"
    room = build()
    report = static_qa(room)
    DELIVERY.mkdir(parents=True, exist_ok=True)
    room.write(DELIVERY / "room.nbt")
    roundtrip = Structure.read(DELIVERY / "room.nbt")
    assert roundtrip.blocks == room.blocks and roundtrip.size == room.size
    assert {position: plain(entity) for position, entity in roundtrip.block_entities.items()} == {position: plain(entity) for position, entity in room.block_entities.items()}
    write_json(DELIVERY / "room.json", {"id": "M11", "name": "白骨祷室", "size": SIZE, "entrance": ENTRANCE, "interior": INTERIOR, "facing": "north", "sha256": digest(DELIVERY / "room.nbt"), "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA, "minecraft": "1.20.1", "data_version": 3465, "resource_id_suggestion": "ftdungeonmobs:image25/m11", "full_roof": True, "explicit_air": True, "deployed": False, "safe_route_targets": TARGETS, "description": "石砌拱肋、白骨壁龛、红旗头骨纹、祭坛与空仪式盆、蜡烛和锚固吊灯；北侧三格接口，祭坛两侧连通环路。"})
    write_json(ROOT / "qa/static.json", report | {"sha256": digest(DELIVERY / "room.nbt"), "reference_sha256": REFERENCE_SHA, "roundtrip": "PASS"})
    write_json(ROOT / "qa/materials.json", dict(sorted(Counter(block.name for block in room.blocks.values() if block != AIR).items())))
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / "tools/verification-resources/data/ftm11verification/structures/empty.nbt")
    print(json.dumps({"static": "PASS", "sha256": digest(DELIVERY / "room.nbt"), "size": SIZE}, ensure_ascii=False))


if __name__ == "__main__":
    main()
