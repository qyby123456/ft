from __future__ import annotations

import hashlib
import json
from collections import Counter, deque
from pathlib import Path

from nbtio import AIR, Structure, compound, plain, state, string


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out/S6"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-design-batch3-20261101/out/S6-nether-soul-shrine-hero.png")
REFERENCE_SHA = "cd80e2ae91d8d5b47d80243422a87abcab5e0d68d8fda13d9c41365fea2b15a7"
SIZE = (9, 7, 9)
ENTRANCE = (4, 1, 0)
INTERIOR = (4, 1, 2)
TARGETS = ((2, 1, 4), (6, 1, 4), (4, 1, 6), (4, 1, 7))


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def make_room():
    room = Structure(SIZE)
    room.box((0, 0, 0), (8, 6, 8), AIR)

    def put(position, name, **properties):
        room.set(position, state(name, **properties))

    for block_x in range(9):
        for block_z in range(9):
            floor = "polished_blackstone_bricks" if (block_x + block_z) % 3 else "red_nether_bricks"
            put((block_x, 0, block_z), floor)
            put((block_x, 6, block_z), "nether_bricks" if block_x % 2 else "polished_blackstone_bricks")
            if block_x in (0, 8) or block_z in (0, 8):
                for block_y in range(1, 6):
                    wall = "red_nether_bricks" if (block_x + block_z + block_y) % 4 else "cracked_nether_bricks"
                    put((block_x, block_y, block_z), wall)
                put((block_x, 5, block_z), "polished_blackstone_bricks")
    for block_x in range(1, 8):
        put((block_x, 6, 2), "polished_basalt", axis="x")
        put((block_x, 6, 4), "polished_basalt", axis="x")
    for block_x in range(3, 6):
        for block_z in range(3):
            put((block_x, 0, block_z), "polished_blackstone")
        for block_y in range(1, 4):
            room.set((block_x, block_y, 0), AIR)
    for block_x, block_z in ((1, 1), (7, 1), (1, 4), (7, 4)):
        put((block_x, 1, block_z), "polished_blackstone_bricks")
        for block_y in (2, 3):
            put((block_x, block_y, block_z), "quartz_pillar", axis="y")
        put((block_x, 4, block_z), "chiseled_polished_blackstone")
        put((block_x, 5, block_z), "nether_bricks")
    for block_x in (2, 6):
        for block_z in range(2, 7):
            put((block_x, 0, block_z), "gilded_blackstone")
    for block_x in range(3, 6):
        for block_z in range(3, 6):
            put((block_x, 1, block_z), "polished_blackstone_bricks")
    put((4, 1, 3), "polished_blackstone_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    put((4, 1, 4), "gilded_blackstone")
    put((4, 2, 4), "soul_soil")
    put((4, 3, 4), "soul_fire")
    for position in ((3, 2, 4), (5, 2, 4), (4, 2, 3), (4, 2, 5)):
        put(position, "polished_blackstone_brick_slab", type="bottom", waterlogged=False)
    for position in ((3, 2, 3), (5, 2, 3), (3, 2, 5), (5, 2, 5)):
        put(position, "soul_lantern", hanging=False, waterlogged=False)
    for block_x, block_z in ((4, 2), (2, 4), (6, 4)):
        put((block_x, 5, block_z), "chain", axis="y", waterlogged=False)
        put((block_x, 4, block_z), "soul_lantern", hanging=True, waterlogged=False)
    for block_x in (3, 5):
        for block_y in range(3, 6):
            put((block_x, block_y, 7), "chain", axis="y", waterlogged=False)
    for position, facing in (((4, 3, 7), "north"), ((1, 2, 3), "east"), ((7, 2, 3), "west")):
        room.set(position, state("skeleton_wall_skull", facing=facing), compound(id=string("minecraft:skull")))
    for block_x in (2, 6):
        put((block_x, 5, 7), "weeping_vines_plant")
        put((block_x, 4, 7), "weeping_vines", age=25)
    for block_x in (1, 7):
        inner_side = 2 if block_x == 1 else 6
        for block_y in range(1, 6):
            put((block_x, block_y, 6), "glass")
            put((inner_side, block_y, 7), "polished_blackstone_bricks")
        put((block_x, 0, 7), "polished_blackstone_bricks")
    for block_y in range(1, 6):
        put((1, block_y, 7), "lava", level=0 if block_y == 5 else 8)
    put((7, 5, 7), "lava", level=0)
    put((7, 4, 7), "polished_blackstone_bricks")
    put((7, 3, 7), "pointed_dripstone", vertical_direction="down", thickness="tip", waterlogged=False)
    return room


def static_qa(room):
    blocks = room.blocks
    full = {"nether_bricks", "red_nether_bricks", "cracked_nether_bricks", "polished_blackstone_bricks", "polished_blackstone", "chiseled_polished_blackstone", "polished_basalt", "quartz_pillar", "gilded_blackstone", "glass", "soul_soil"}

    def name(position):
        return room.get(position).name.removeprefix("minecraft:")

    def solid(position):
        return name(position) in full

    def walkable(position):
        block_x, block_y, block_z = position
        return 0 <= block_x < 9 and block_y == 1 and 0 <= block_z < 9 and name(position) == "air" and name((block_x, 2, block_z)) == "air" and solid((block_x, 0, block_z))

    assert len(blocks) == 567 and not room.entities
    assert room.data_version == 3465
    assert all(block.name.startswith("minecraft:") for block in blocks.values())
    forbidden = ("chest", "barrel", "shulker", "spawner", "command_block", "structure_block", "jigsaw", "portal")
    assert not any(any(token in block.name for token in forbidden) for block in blocks.values())
    for block_x in range(9):
        for block_z in range(9):
            assert solid((block_x, 0, block_z)) and solid((block_x, 6, block_z))
            if block_x in (0, 8) or block_z in (0, 8):
                for block_y in range(1, 6):
                    entry = block_z == 0 and block_x in (3, 4, 5) and block_y in (1, 2, 3)
                    assert name((block_x, block_y, block_z)) == "air" if entry else solid((block_x, block_y, block_z))
    for block_x in (3, 4, 5):
        assert walkable((block_x, 1, 0)) and name((block_x, 3, 0)) == "air"
    visited = {ENTRANCE}
    pending = deque(visited)
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            adjacent = block_x + offset_x, block_y, block_z + offset_z
            if adjacent not in visited and walkable(adjacent):
                visited.add(adjacent)
                pending.append(adjacent)
    assert INTERIOR in visited and all(target in visited for target in TARGETS)
    supports = Counter()
    fluid_positions = set()
    for position, block in blocks.items():
        block_x, block_y, block_z = position
        block_name = name(position)
        above = block_x, block_y + 1, block_z
        below = block_x, block_y - 1, block_z
        if block_name == "soul_fire":
            assert name(below) == "soul_soil"
            supports["soul_fire"] += 1
        if block_name == "chain" or (block_name == "soul_lantern" and block.get("hanging") == "true"):
            cursor = above
            while name(cursor) == "chain":
                cursor = cursor[0], cursor[1] + 1, cursor[2]
            assert solid(cursor), (position, "unanchored suspension")
            supports[block_name] += 1
        if block_name == "soul_lantern" and block.get("hanging") == "false":
            assert solid(below)
            supports["standing_soul_lantern"] += 1
        if block_name == "skeleton_wall_skull":
            support = {"north": (block_x, block_y, block_z + 1), "east": (block_x - 1, block_y, block_z), "west": (block_x + 1, block_y, block_z)}[block.get("facing")]
            assert solid(support) and position in room.block_entities
            supports["wall_skull"] += 1
        if block_name.startswith("weeping_vines"):
            assert solid(above) or name(above) == "weeping_vines_plant"
            supports["vines"] += 1
        if block_name == "pointed_dripstone":
            assert solid(above) and name((block_x, block_y + 2, block_z)) == "lava"
            assert name(below) == "air" and name((block_x, block_y - 2, block_z)) == "air"
            supports["lava_dripstone"] += 1
        if block_name == "lava":
            fluid_positions.add(position)
            for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                assert solid((block_x + offset_x, block_y, block_z + offset_z))
            assert solid(below) or name(below) == "lava"
    assert len(fluid_positions) == 6 and supports["soul_fire"] == 1
    for entity in room.block_entities.values():
        assert plain(entity) == {"id": "minecraft:skull"}
    return {
        "status": "PASS", "passed": True, "full_volume_blocks": len(blocks),
        "single_palette": True, "floor_and_roof_columns": 81,
        "north_opening": {"width": 3, "height": 3, "full_floor": True, "only_opening": True},
        "navigation": {"dry_reachable_positions": len(visited), "entrance": ENTRANCE, "interior": INTERIOR, "required_safe_points": TARGETS, "side_and_rear_route": True, "full_block_floor_route": True},
        "support_checks": dict(supports), "lava": {"positions": sorted(fluid_positions), "sealed_sides_and_bottom": True, "drip_reservoir": [7, 5, 7], "drip_support": [7, 4, 7], "drip_tip": [7, 3, 7]},
        "inventory_items": 0, "loot_tables": 0, "entities": 0, "block_entities": 3,
        "reward_containers": 0, "flammable_wood": 0,
        "materials": dict(sorted(Counter(block.name for block in blocks.values()).items())),
    }, visited


def main():
    assert hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_SHA
    OUT.mkdir(parents=True, exist_ok=True)
    room = make_room()
    room.write(OUT / "room.nbt")
    loaded = Structure.read(OUT / "room.nbt")
    assert loaded.blocks == room.blocks and loaded.size == SIZE
    assert {position: plain(tag) for position, tag in loaded.block_entities.items()} == {position: plain(tag) for position, tag in room.block_entities.items()}
    report, visited = static_qa(loaded)
    digest = hashlib.sha256((OUT / "room.nbt").read_bytes()).hexdigest()
    metadata = {
        "id": "S6", "name": "地狱魂火祭坛", "size": SIZE, "entrance": ENTRANCE, "interior": INTERIOR,
        "facing": "north", "sha256": digest, "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA,
        "minecraft": "1.20.1", "data_version": 3465, "tier": "small",
        "adaptations": ["概念图7×7 h5扩为9×9 h7，为三格北接口、绕行通路、灯链与封闭熔岩滴落装置留出原版方块空间。", "概念图熔岩细线改为镀金黑石嵌线；真实熔岩集中于两个封闭玻璃壁龛。", "东龛为真实熔岩储液、实体支撑和向下滴水石锥，展示原版熔岩滴落粒子；不放炼药锅，不产生可领取熔岩库存。", "西龛为封闭熔岩落流；骨骼装饰为三颗无持有物、无玩家头颅数据的原版骷髅头。", "剖切仅用于软件预览，NBT保留完整屋顶与唯一北开口。"],
        "safe_points": TARGETS, "deployed": False,
    }
    dump(OUT / "room.json", metadata)
    report.update({"id": "S6", "sha256": digest, "reference_sha256": REFERENCE_SHA, "roundtrip": "PASS"})
    dump(ROOT / "qa/static.json", report)
    dump(ROOT / "qa/reachable.json", sorted(visited))
    test_space = Structure((34, 12, 34))
    test_space.write(ROOT / "forge/src/main/resources/data/fts6qa/structures/empty.nbt")
    native = ROOT / "qa/forge-native.json"
    combined = {"id": "S6", "status": "STATIC_PASS_NATIVE_PENDING", "sha256": digest, "reference_sha256": REFERENCE_SHA, "static": report, "native": json.loads(native.read_text(encoding="utf-8")) if native.exists() else {"status": "NOT_RUN"}, "preview": {"path": "preview.png", "source": "actual room.nbt", "kind": "software cutaway, not an in-game screenshot"}, "player_saves_modified": False, "core_modified": False, "deployed": False, "paid_image_calls": 0}
    if combined["native"].get("status") == "PASS" and combined["native"].get("sha256") == digest:
        combined["status"] = "PASS"
    dump(OUT / "qa.json", combined)
    print(json.dumps({"out": str(OUT), "sha256": digest, "static": "PASS", "reachable": len(visited)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
