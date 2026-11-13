from __future__ import annotations

import collections
import hashlib
import json
from pathlib import Path

from nbtio import AIR, Structure, compound, plain, read_nbt, state, string


ROOT = Path(__file__).resolve().parents[1]
DELIVERY = ROOT / "delivery/M6"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-design-batch3-20261101/out/M6-nether-bastion-hall-hero.png")
REFERENCE_HASH = "4fbbc2048340fbff123189e5df3bc011b8c5ba1fedef83a0950f2795113e0c91"
SIZE = (21, 12, 21)
ENTRANCE = (10, 2, 0)
INTERIOR = (10, 2, 4)
DIRECTIONS = {"east": (1, 0, 0), "west": (-1, 0, 0), "south": (0, 0, 1), "north": (0, 0, -1)}
FORBIDDEN = {"command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "chest", "trapped_chest", "barrel", "hopper", "dispenser", "dropper", "shulker_box"}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def make_room():
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (20, 11, 20), AIR)

    def put(position, name, entity=None, **properties):
        structure.set(position, state(name, **properties), entity)

    def box(start, end, name, **properties):
        structure.box(start, end, state(name, **properties))

    for pos_x in range(21):
        for pos_z in range(21):
            put((pos_x, 0, pos_z), "blackstone")
            floor = "polished_blackstone_bricks" if (pos_x * 7 + pos_z * 11) % 17 else "cracked_polished_blackstone_bricks"
            if pos_x in (1, 19) or pos_z in (1, 19):
                floor = "red_nether_bricks"
            put((pos_x, 1, pos_z), floor)
            put((pos_x, 11, pos_z), "polished_blackstone_bricks")
            if pos_x in (0, 20) or pos_z in (0, 20):
                for pos_y in range(2, 11):
                    wall = "nether_bricks" if (pos_x + pos_z + pos_y) % 7 else "red_nether_bricks"
                    if pos_y in (2, 9, 10):
                        wall = "polished_blackstone_bricks"
                    put((pos_x, pos_y, pos_z), wall)
    for pos_x, pos_z in ((0, 0), (0, 20), (20, 0), (20, 20), (0, 7), (20, 7), (7, 20), (13, 20)):
        box((pos_x, 2, pos_z), (pos_x, 10, pos_z), "polished_basalt", axis="y")
        for pos_y in (3, 8):
            put((pos_x, pos_y, pos_z), "gilded_blackstone")
    for pos_z in (4, 9, 14, 19):
        box((1, 10, pos_z), (19, 10, pos_z), "crimson_hyphae", axis="x")
    for pos_x in (6, 14):
        for pos_z in (7, 15):
            put((pos_x, 2, pos_z), "chiseled_polished_blackstone")
            box((pos_x, 3, pos_z), (pos_x, 7, pos_z), "polished_basalt", axis="y")
            put((pos_x, 3, pos_z), "gilded_blackstone")
            put((pos_x, 8, pos_z), "polished_blackstone_bricks")
            for neighbor_x, facing in ((pos_x - 1, "east"), (pos_x + 1, "west")):
                put((neighbor_x, 8, pos_z), "red_nether_brick_stairs", facing=facing, half="top", shape="straight", waterlogged=False)
    for pos_z in (7, 15):
        box((6, 9, pos_z), (14, 9, pos_z), "crimson_hyphae", axis="x")
    for pos_x in (6, 14):
        box((pos_x, 9, 7), (pos_x, 9, 15), "crimson_hyphae", axis="z")

    for center_x in (3, 17):
        center_z = 16
        for pos_x in range(center_x - 1, center_x + 2):
            for pos_z in range(center_z - 1, center_z + 2):
                put((pos_x, 1, pos_z), "lava", level=0)
        for pos_y in range(2, 9):
            for pos_x in range(center_x - 1, center_x + 2):
                for pos_z in range(center_z - 1, center_z + 2):
                    if (pos_x, pos_z) != (center_x, center_z):
                        put((pos_x, pos_y, pos_z), "glass")
            put((center_x, pos_y, center_z), "lava", level=0 if pos_y == 8 else 8)
        box((center_x - 1, 9, center_z - 1), (center_x + 1, 9, center_z + 1), "polished_blackstone_bricks")
        for pos_x in (center_x - 1, center_x + 1):
            put((pos_x, 9, center_z - 1), "gilded_blackstone")
        for pos_x in range(center_x - 2, center_x + 3):
            for pos_z in (center_z - 2, center_z + 2):
                put((pos_x, 1, pos_z), "chiseled_polished_blackstone")
        for pos_z in range(center_z - 2, center_z + 3):
            for pos_x in (center_x - 2, center_x + 2):
                put((pos_x, 1, pos_z), "chiseled_polished_blackstone")

    for pos_z in range(3, 9):
        put((2, 1, pos_z), "lava", level=0)
        put((1, 2, pos_z), "polished_blackstone_bricks")
        put((2, 2, pos_z), "glass")
        put((3, 2, pos_z), "glass")
    for pos_z in (2, 9):
        put((2, 2, pos_z), "polished_blackstone_bricks")
    for pos_z in (4, 5, 6):
        for pos_x in (2, 3):
            put((pos_x, 2, pos_z), "warped_planks")
    for pos_x, facing in ((1, "east"), (4, "west")):
        put((pos_x, 2, 5), "warped_stairs", facing=facing, half="bottom", shape="straight", waterlogged=False)
    for pos_x in (2, 3):
        for pos_z in (4, 6):
            put((pos_x, 3, pos_z), "warped_fence", east=False, west=False, north=False, south=False, waterlogged=False)

    box((8, 2, 9), (12, 2, 13), "polished_blackstone_bricks")
    for pos_x in (9, 10, 11):
        put((pos_x, 2, 8), "polished_blackstone_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    for pos_x, pos_z in ((8, 9), (12, 9), (8, 13), (12, 13)):
        put((pos_x, 2, pos_z), "gold_block")
    box((9, 3, 10), (11, 3, 12), "gilded_blackstone")
    put((10, 3, 11), "soul_sand")
    put((10, 4, 11), "soul_fire")
    for pos_x, pos_z in ((9, 11), (11, 11), (10, 10), (10, 12)):
        put((pos_x, 4, pos_z), "polished_blackstone_brick_wall", east="none", west="none", north="none", south="none", up=True, waterlogged=False)
    for pos_x in (8, 12):
        for pos_z in (10, 12):
            box((pos_x, 3, pos_z), (pos_x, 9, pos_z), "chain", axis="y", waterlogged=False)
            put((pos_x, 10, pos_z), "crimson_hyphae", axis="x")

    add_decorations(structure)
    for pos_x in (8, 12):
        box((pos_x, 2, 0), (pos_x, 5, 0), "chiseled_polished_blackstone")
    box((8, 5, 0), (12, 5, 0), "red_nether_bricks")
    for pos_x in (9, 10, 11):
        for pos_z in range(6):
            put((pos_x, 1, pos_z), "polished_basalt", axis="y")
            box((pos_x, 2, pos_z), (pos_x, 4, pos_z), "air")
    for position, block in list(structure.blocks.items()):
        if block.name.removeprefix("minecraft:") not in ("iron_bars", "warped_fence"):
            continue
        properties = dict(block.properties)
        for direction, offset in DIRECTIONS.items():
            neighbor = structure.get(tuple(coordinate + delta for coordinate, delta in zip(position, offset)))
            properties[direction] = str(neighbor.name == block.name).lower()
        structure.set(position, state(block.name, **properties))
    return structure


def add_decorations(structure):
    def put(position, name, entity=None, **properties):
        structure.set(position, state(name, **properties), entity)

    def box(start, end, name, **properties):
        structure.box(start, end, state(name, **properties))

    for center_x in (4, 16):
        center_z = 10
        box((center_x - 1, 5, center_z - 1), (center_x + 1, 5, center_z + 1), "polished_blackstone_slab", type="top", waterlogged=False)
        box((center_x - 1, 8, center_z - 1), (center_x + 1, 8, center_z + 1), "polished_blackstone_bricks")
        for pos_y in (6, 7):
            for pos_x in range(center_x - 1, center_x + 2):
                for pos_z in range(center_z - 1, center_z + 2):
                    if pos_x in (center_x - 1, center_x + 1) or pos_z in (center_z - 1, center_z + 1):
                        put((pos_x, pos_y, pos_z), "iron_bars", east=False, west=False, north=False, south=False, waterlogged=False)
        box((center_x, 9, center_z), (center_x, 10, center_z), "chain", axis="y", waterlogged=False)
        put((center_x, 7, center_z), "chain", axis="y", waterlogged=False)
        put((center_x, 6, center_z), "lantern", hanging=True, waterlogged=False)
        put((center_x, 11, center_z), "gilded_blackstone")
    for pos_x in (8, 12):
        put((pos_x, 5, 17), "bell", compound(id=string("minecraft:bell")), attachment="ceiling", facing="north", powered=False)
        put((pos_x, 6, 17), "gilded_blackstone")
        box((pos_x, 7, 17), (pos_x, 10, 17), "chain", axis="y", waterlogged=False)
    for pos_x, pos_z in ((3, 12), (17, 12), (7, 18), (13, 18)):
        put((pos_x, 2, pos_z), "polished_blackstone_bricks")
        put((pos_x, 3, pos_z), "potted_crimson_roots")
    for pos_x in (2, 18):
        put((pos_x, 2, 11), "bone_block", axis="y")
        put((pos_x, 3, 11), "skeleton_skull", compound(id=string("minecraft:skull")), rotation=8)
        box((pos_x, 7, 12), (pos_x, 9, 12), "weeping_vines_plant")
        put((pos_x, 6, 12), "weeping_vines", age=25)
        put((pos_x, 10, 12), "red_nether_bricks")
    for pos_x, pos_z in ((5, 3), (15, 3), (5, 18), (15, 18)):
        put((pos_x, 2, pos_z), "chiseled_polished_blackstone")
        put((pos_x, 3, pos_z), "soul_lantern", hanging=False, waterlogged=False)
    for pos_z in (5, 17):
        for pos_x in (7, 9, 11, 13):
            put((pos_x, 1, pos_z), "shroomlight")


def is_full(block):
    name = block.name.removeprefix("minecraft:")
    partial = {"air", "cave_air", "void_air", "lava", "soul_fire", "glass_pane", "chain", "iron_bars", "lantern", "soul_lantern", "bell", "skeleton_skull", "weeping_vines", "weeping_vines_plant", "potted_crimson_roots"}
    return name not in partial and not name.endswith(("_stairs", "_slab", "_fence", "_wall"))


def validate(path):
    structure = Structure.read(path)
    _, root = read_nbt(path)
    data = plain(root)
    assert data["DataVersion"] == 3465 and structure.size == SIZE
    assert len(data["blocks"]) == len(structure.blocks) == 5292
    assert "palette" in data and "palettes" not in data
    assert not structure.entities
    counts = collections.Counter(block.name for block in structure.blocks.values())
    assert all(block.name.startswith("minecraft:") for block in structure.blocks.values())
    assert not any(block.name.removeprefix("minecraft:") in FORBIDDEN for block in structure.blocks.values())
    for pos_x in range(21):
        for pos_z in range(21):
            assert is_full(structure.get((pos_x, 0, pos_z)))
            assert is_full(structure.get((pos_x, 11, pos_z)))
            for pos_y in range(2, 11):
                if pos_x not in (0, 20) and pos_z not in (0, 20):
                    continue
                opening = pos_z == 0 and 9 <= pos_x <= 11 and 2 <= pos_y <= 4
                block = structure.get((pos_x, pos_y, pos_z))
                assert block.name == "minecraft:air" if opening else is_full(block)
    for pos_x in (9, 10, 11):
        for pos_z in range(6):
            assert is_full(structure.get((pos_x, 1, pos_z)))
            assert all(structure.get((pos_x, pos_y, pos_z)) == AIR for pos_y in (2, 3, 4))
    liquid_positions = []
    for position, block in structure.blocks.items():
        name = block.name.removeprefix("minecraft:")
        if name == "lava":
            liquid_positions.append(list(position))
            for offset in (*DIRECTIONS.values(), (0, -1, 0)):
                neighbor = structure.get(tuple(coordinate + delta for coordinate, delta in zip(position, offset)))
                assert neighbor.name == "minecraft:lava" or is_full(neighbor), (position, offset, neighbor)
        if name == "soul_fire":
            assert structure.get((position[0], position[1] - 1, position[2])).name == "minecraft:soul_sand"
        if name == "chain":
            above = (position[0], position[1] + 1, position[2])
            while structure.get(above).name == "minecraft:chain":
                above = (above[0], above[1] + 1, above[2])
            assert is_full(structure.get(above)), (position, "unanchored chain")
        if name in ("lantern", "soul_lantern"):
            hanging = block.get("hanging") == "true"
            support = structure.get((position[0], position[1] + (1 if hanging else -1), position[2]))
            assert is_full(support) or hanging and support.name == "minecraft:chain"
        if name == "bell":
            assert is_full(structure.get((position[0], position[1] + 1, position[2])))
    for tag in structure.block_entities.values():
        entity = plain(tag)
        assert entity["id"] in ("minecraft:bell", "minecraft:skull")
        assert not any(key in entity for key in ("Items", "LootTable", "Book", "Command"))
    assert not any("oak" in block.name or "wool" in block.name or "carpet" in block.name for block in structure.blocks.values())
    assert len(liquid_positions) == 38
    assert counts["minecraft:soul_fire"] == 1 and counts["minecraft:bell"] == 2
    return {
        "status": "STATIC_PASS_NATIVE_PENDING",
        "room_id": "M6",
        "reference": {"path": str(REFERENCE), "sha256": digest(REFERENCE), "matches_manifest": True, "size": [1536, 1024], "generation_requested": False},
        "structure": {"sha256": digest(path), "size": list(SIZE), "data_version": 3465, "records": len(structure.blocks), "non_air": len(structure.blocks) - counts["minecraft:air"], "palette_states": len(data["palette"]), "single_palette": True, "entities": 0, "block_entities": len(structure.block_entities)},
        "static": {"status": "PASS", "complete_roof_and_base": True, "only_opening": "north 3x3", "entrance": list(ENTRANCE), "interior": list(INTERIOR), "north_clear_depth": 6, "lava_cells": len(liquid_positions), "lava_sides_and_bases_sealed": True, "hanging_chains_anchored": True, "lamps_and_bells_supported": True, "soul_fire_on_soul_sand": True, "reward_inventory": 0, "loot_tables": 0, "containers": 0, "forbidden_blocks": 0},
        "lava_positions": liquid_positions,
        "materials": dict(sorted(counts.items())),
        "adaptations": ["概念15×15 h9合理扩为21×21；模板总高12含双层基座与完整屋顶", "双瀑布与池用完整透明玻璃封闭，亮纹用菌光体替代裸露熔岩", "魂火使用灵魂沙和石栏围护，避开北接口和干燥环路", "诡异木桥跨密封槽；绯红木/诡异木是原版不可燃木材", "牢笼不放生物；钟和头骨只有合法空方块实体", "最终NBT屋顶完整；预览剖切不修改结构"],
        "native": {"status": "PENDING", "required": "Minecraft 1.20.1 / Forge 47.4.10 world placement, four rotations, collision route, native supports, 400 fluid/fire ticks"},
        "scope": {"id_only": "M6", "deployed": False, "player_saves_modified": False, "other_rooms_modified": False, "full_modpack_client_playtest": False},
    }


def main():
    assert digest(REFERENCE) == REFERENCE_HASH, "Reference image fingerprint changed"
    DELIVERY.mkdir(parents=True, exist_ok=True)
    structure = make_room()
    structure.write(DELIVERY / "room.nbt")
    metadata = {"id": "M6", "name": "地狱烈焰堡垒", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "sha256": digest(DELIVERY / "room.nbt"), "reference": str(REFERENCE)}
    write_json(DELIVERY / "room.json", metadata)
    report = validate(DELIVERY / "room.nbt")
    write_json(DELIVERY / "qa.json", report)
    write_json(ROOT / "qa/static.json", report)
    print(json.dumps({"status": report["status"], "sha256": metadata["sha256"], "records": len(structure.blocks), "lava": report["static"]["lava_cells"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
