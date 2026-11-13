from __future__ import annotations

import collections
import hashlib
import json
from pathlib import Path

from nbtio import AIR, Structure, plain, read_nbt, state


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M10-slag-foundry-hero.png")
REFERENCE_HASH = "1fcca8784da261c65ee881026b0c6a1d2640a31c86195f1c6c3287c1f0e232f4"
SIZE = (17, 11, 17)
ENTRANCE = (8, 2, 0)
INTERIOR = (8, 2, 4)
LANDMARKS = ((8, 2, 6), (5, 2, 7), (5, 2, 11), (6, 2, 9), (10, 2, 9),
             (8, 2, 12), (4, 2, 12), (12, 2, 12), (12, 2, 7), (13, 2, 6), (8, 2, 14))
LAVA = tuple((2, 2, coordinate) for coordinate in range(6, 12))
FIRES = ((4, 3, 14), (5, 3, 14), (11, 3, 14), (12, 3, 14))
LAMPS = ((2, 5, 6), (8, 6, 11), (14, 5, 6))
STEPS = ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, 1, 0), (0, -1, 0))
PARTIAL = {"air", "fire", "lava", "anvil", "grindstone", "lantern", "chain", "rail", "lever", "tripwire_hook", "iron_trapdoor"}
FORBIDDEN = {"command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "chest", "trapped_chest", "barrel", "hopper", "dispenser", "dropper", "shulker_box", "tnt", "coal_block"}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def add(first, second):
    return tuple(first[index] + second[index] for index in range(3))


def full(block):
    name = block.name.removeprefix("minecraft:")
    return name not in PARTIAL and not name.endswith(("_slab", "_stairs", "_wall", "_button"))


def make_room():
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (16, 10, 16), AIR)

    def put(position, name, **properties):
        structure.set(position, state(name, **properties))

    def box(start, end, name, **properties):
        structure.box(start, end, state(name, **properties))

    for pos_x in range(SIZE[0]):
        for pos_z in range(SIZE[2]):
            put((pos_x, 0, pos_z), "blackstone")
            floor = "deepslate_tiles" if (pos_x * 7 + pos_z * 11) % 13 else "cracked_deepslate_tiles"
            if pos_x in (1, 15) or pos_z in (1, 15):
                floor = "polished_blackstone"
            if 7 <= pos_x <= 9 and pos_z <= 6:
                floor = "polished_andesite"
            put((pos_x, 1, pos_z), floor)
            put((pos_x, 10, pos_z), "deepslate_bricks")
            if pos_x in (0, 16) or pos_z in (0, 16):
                for pos_y in range(2, 10):
                    wall = "deepslate_bricks" if (pos_x * 3 + pos_y * 5 + pos_z) % 11 else "cracked_deepslate_bricks"
                    if pos_y in (2, 8, 9):
                        wall = "polished_blackstone_bricks"
                    put((pos_x, pos_y, pos_z), wall)
    for pos_x, pos_z in ((0, 0), (0, 5), (0, 11), (0, 16), (16, 0), (16, 5), (16, 11), (16, 16), (8, 16)):
        box((pos_x, 2, pos_z), (pos_x, 9, pos_z), "polished_basalt", axis="y")
        for pos_y in (2, 7, 9):
            put((pos_x, pos_y, pos_z), "chiseled_polished_blackstone")
    for pos_z in (0, 6, 11, 16):
        box((0, 9, pos_z), (16, 9, pos_z), "polished_basalt", axis="x")
    for pos_x in (2, 8, 14):
        box((pos_x, 9, 0), (pos_x, 9, 16), "polished_blackstone_bricks")

    for left in (3, 10):
        box((left, 2, 13), (left + 3, 4, 15), "polished_blackstone_bricks")
        box((left, 5, 13), (left + 3, 5, 15), "polished_andesite_slab", type="bottom", waterlogged=False)
        box((left + 1, 5, 14), (left + 2, 9, 15), "cobbled_deepslate")
        for pos_x in (left + 1, left + 2):
            put((pos_x, 2, 14), "netherrack")
            put((pos_x, 3, 14), "fire", age=0, east=False, north=False, south=False, up=False, west=False)
            put((pos_x, 3, 13), "glass")
            put((pos_x, 2, 13), "chiseled_polished_blackstone")
            put((pos_x, 4, 13), "polished_andesite")
    box((8, 2, 15), (8, 8, 15), "polished_basalt", axis="y")

    box((1, 2, 5), (3, 2, 12), "polished_blackstone_bricks")
    for position in LAVA:
        put(position, "lava", level=0)
        put(add(position, (0, 1, 0)), "glass")
    for pos_z in range(5, 13):
        for pos_x in (1, 3):
            put((pos_x, 3, pos_z), "polished_blackstone_brick_slab", type="bottom", waterlogged=False)
    for pos_z in (5, 12):
        put((2, 3, pos_z), "polished_blackstone_brick_slab", type="bottom", waterlogged=False)
    for pos_z in (8, 9):
        put((2, 4, pos_z), "iron_trapdoor", facing="north", half="bottom", open=False, powered=False, waterlogged=False)
    for pos_z in (6, 9, 12):
        put((4, 1, pos_z), "shroomlight")

    box((7, 2, 8), (9, 2, 9), "polished_andesite")
    put((8, 3, 9), "anvil", facing="north")
    for pos_x in (7, 9):
        put((pos_x, 3, 8), "polished_andesite_slab", type="bottom", waterlogged=False)
    for pos_z in range(7, 13):
        put((14, 2, pos_z), "rail", shape="north_south", waterlogged=False)
        for pos_y in (3, 5):
            put((16, pos_y, pos_z), "polished_andesite")
    for pos_z in (7, 9, 11):
        put((15, 4, pos_z), "lever", face="wall", facing="west", powered=False)
    for pos_z in (8, 10, 12):
        put((15, 3, pos_z), "tripwire_hook", facing="west", attached=False, powered=False)
    for pos_z in (8, 11):
        put((13, 2, pos_z), "grindstone", face="floor", facing="west")

    for position, material in (((1, 2, 14), "raw_iron_block"), ((2, 2, 14), "iron_block"),
                               ((2, 3, 14), "raw_iron_block"), ((1, 2, 15), "deepslate_coal_ore"),
                               ((14, 2, 14), "deepslate_coal_ore"), ((15, 2, 14), "raw_iron_block"),
                               ((2, 2, 2), "deepslate_coal_ore"), ((3, 2, 2), "raw_iron_block"),
                               ((14, 2, 2), "deepslate_coal_ore"), ((13, 2, 2), "raw_iron_block")):
        put(position, material)
    for position in ((2, 2, 3), (3, 2, 3), (13, 2, 3), (14, 2, 3), (14, 2, 13)):
        put(position, "blackstone_slab", type="bottom", waterlogged=False)

    for pos_x, pos_y, pos_z in LAMPS:
        put((pos_x, pos_y, pos_z), "lantern", hanging=True, waterlogged=False)
        for chain_y in range(pos_y + 1, 9):
            put((pos_x, chain_y, pos_z), "chain", axis="y", waterlogged=False)

    for pos_x in (6, 10):
        box((pos_x, 2, 0), (pos_x, 5, 0), "polished_basalt", axis="y")
    box((6, 5, 0), (10, 5, 0), "polished_andesite")
    box((7, 2, 0), (9, 4, 0), "air")
    return structure


def static_qa():
    path = ROOT / "room.nbt"
    structure = Structure.read(path)
    raw = plain(read_nbt(path)[1])
    errors = []

    def check(condition, message):
        if not condition:
            errors.append(message)

    check(path.read_bytes()[:2] == b"\x1f\x8b", "not gzip")
    check(raw.get("DataVersion") == 3465 and "palette" in raw and "palettes" not in raw, "wrong data version or palette layout")
    check(structure.size == SIZE, "size mismatch")
    check(len(structure.blocks) == SIZE[0] * SIZE[1] * SIZE[2], "template must explicitly record its complete volume")
    check(not structure.entities and not structure.block_entities, "entities or inventory-bearing block entities present")
    counts = collections.Counter(block.name for block in structure.blocks.values())
    for position, block in structure.blocks.items():
        name = block.name.removeprefix("minecraft:")
        check(block.name.startswith("minecraft:") and name not in FORBIDDEN, f"forbidden block {position}: {block}")
        check(not any(token in name for token in ("planks", "log", "wood", "wool", "carpet", "leaves", "bookshelf", "scaffolding")), f"combustible construction {position}: {name}")
        check(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)), f"out of bounds {position}")
    for pos_x in range(SIZE[0]):
        for pos_z in range(SIZE[2]):
            check(full(structure.get((pos_x, 0, pos_z))), "foundation gap")
            check(full(structure.get((pos_x, 1, pos_z))), "floor gap")
            check(full(structure.get((pos_x, 10, pos_z))), "roof gap")
    for pos_y in range(2, 10):
        for pos_x in range(17):
            for pos_z in range(17):
                if pos_x not in (0, 16) and pos_z not in (0, 16):
                    continue
                opening = pos_z == 0 and pos_x in (7, 8, 9) and pos_y in (2, 3, 4)
                check(structure.get((pos_x, pos_y, pos_z)) == AIR if opening else full(structure.get((pos_x, pos_y, pos_z))), "wall or north portal gap")

    actual_lava = {position for position, block in structure.blocks.items() if block.name == "minecraft:lava"}
    actual_fire = {position for position, block in structure.blocks.items() if block.name == "minecraft:fire"}
    check(actual_lava == set(LAVA) and actual_fire == set(FIRES), "heat source count mismatch")
    for position in actual_lava:
        for step in STEPS:
            neighbor = add(position, step)
            check(neighbor in actual_lava or full(structure.get(neighbor)), f"unsealed lava {position} {step}")
        check(structure.get(add(position, (0, 1, 0))).name == "minecraft:glass", "missing full glass lava lid")
    for position in actual_fire:
        for step in STEPS:
            neighbor = add(position, step)
            check(neighbor in actual_fire or full(structure.get(neighbor)), f"unsealed fire chamber {position} {step}")
        check(structure.get(add(position, (0, -1, 0))).name == "minecraft:netherrack", "fire lacks permanent base")
    for position, block in structure.blocks.items():
        if block.name in ("minecraft:anvil", "minecraft:grindstone", "minecraft:rail"):
            check(full(structure.get(add(position, (0, -1, 0)))), f"unsupported device {position}")
        if block.name in ("minecraft:chain", "minecraft:lantern"):
            anchor = add(position, (0, 1, 0))
            while structure.get(anchor).name == "minecraft:chain":
                anchor = add(anchor, (0, 1, 0))
            check(full(structure.get(anchor)), f"unanchored suspension {position}")
        if block.name in ("minecraft:lever", "minecraft:tripwire_hook"):
            check(full(structure.get(add(position, (1, 0, 0)))), f"unsupported wall tool {position}")

    def walkable(position):
        if not all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)):
            return False
        return (structure.get(position).name in ("minecraft:air", "minecraft:rail")
                and structure.get(add(position, (0, 1, 0))) == AIR
                and full(structure.get(add(position, (0, -1, 0))))
                and structure.get(add(position, (0, -1, 0))).name not in ("minecraft:lava", "minecraft:fire"))

    def flood(start):
        visited = {start}
        pending = collections.deque([start])
        while pending:
            current = pending.popleft()
            for step in STEPS[:4]:
                candidate = add(current, step)
                if candidate not in visited and walkable(candidate):
                    visited.add(candidate)
                    pending.append(candidate)
        return visited

    for pos_x in range(7, 10):
        for pos_z in range(6):
            check(walkable((pos_x, 2, pos_z)) and structure.get((pos_x, 4, pos_z)) == AIR, "unsafe three-wide three-high approach")
    reachable = flood(ENTRANCE)
    check(walkable(ENTRANCE) and walkable(INTERIOR), "unsafe entrance/interior")
    check(INTERIOR in reachable and ENTRANCE in flood(INTERIOR), "entry or return route disconnected")
    check(all(position in reachable for position in LANDMARKS), "a workstation approach is disconnected")
    check(digest(REFERENCE) == REFERENCE_HASH and digest(ROOT / "reference/M10-slag-foundry-hero.png") == REFERENCE_HASH, "reference fingerprint mismatch")
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    check(metadata["sha256"] == digest(path), "metadata fingerprint mismatch")
    report = {
        "status": "PASS" if not errors else "FAIL", "errors": errors,
        "source": str(path), "sha256": digest(path), "reference_sha256": REFERENCE_HASH,
        "size": list(SIZE), "records": len(structure.blocks), "non_air": len(structure.blocks) - counts["minecraft:air"],
        "palette_states": len(raw["palette"]), "block_entities": len(structure.block_entities), "entities": len(structure.entities),
        "material_counts": dict(sorted(counts.items())), "complete_volume": True,
        "floor_and_foundation_cells": 578, "complete_roof_cells": 289,
        "entrance": list(ENTRANCE), "interior": list(INTERIOR), "entrance_clearance": [3, 3, 6],
        "reachable_dry_feet_positions": len(reachable), "landmark_routes": [list(position) for position in LANDMARKS],
        "lava_cells": len(actual_lava), "glass_capped_six_face_lava_containment": True,
        "fire_cells": len(actual_fire), "six_face_fire_chambers_with_netherrack_bases": True,
        "hanging_lamps": len(LAMPS), "equipment_has_full_support": True,
        "no_combustible_building_materials": True, "inventory_or_rewards": 0,
        "scope": "Independent re-read of serialized NBT; native legality, collision and survival are verified separately."
    }
    write_json(ROOT / "qa/static.json", report)
    if errors:
        raise RuntimeError("; ".join(errors[:12]))
    return report


def main():
    assert digest(REFERENCE) == REFERENCE_HASH
    structure = make_room()
    structure.write(ROOT / "room.nbt")
    write_json(ROOT / "room.json", {
        "id": "M10", "name": "熔渣铸造间", "english_name": "Slag Foundry", "theme": "灼热铸造间",
        "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north",
        "sha256": digest(ROOT / "room.nbt"), "reference": str(REFERENCE), "reference_sha256": REFERENCE_HASH,
        "reference_size": [1536, 1024], "minecraft_version": "1.20.1", "data_version": 3465,
        "template_id_proposal": "ftdungeonmobs:image25/m10", "coordinate_convention": "relative integer XYZ; entrance/interior are player feet, not floor blocks",
        "entrance_clearance": {"width": 3, "height": 3, "straight_depth": 6, "floor_y": 1},
        "heat_sources": {"lava": [list(position) for position in LAVA], "fire": [list(position) for position in FIRES], "containment": "full stone boundaries and full glass covers/fronts; fire on netherrack; no combustible construction"},
        "landmark_feet": [list(position) for position in LANDMARKS],
        "features": ["双座密封铸造炉与烟囱", "玻璃全封闭熔渣槽与铁格盖饰", "石台铁砧", "工具墙、磨石、地轨", "原矿与煤矿石堆", "三盏锚固吊灯", "完整耐火屋顶与干燥环路"],
        "adaptations": ["为三格北接口与设备环路将概念占地合理落地为17×17，高11格", "木梁/木箱改成玄武岩梁与铁矿堆，避免火源附近可燃件", "煤炭块在原生验收中确认可燃，替换为不可燃的深层煤矿石", "工具墙使用原版杠杆/绊线钩静态陈设，不用物品实体", "炉窗和熔槽用完整玻璃封闭，剖切只用于预览"],
        "inventory": "none", "loot": "none", "entities": 0, "deployed": False,
        "integration_boundary": "Independent delivery only; coordinator assigns fixed slot and hallway. Do not write production saves or overwrite existing templates."
    })
    report = static_qa()
    write_json(ROOT / "qa.json", {"id": "M10", "status": "PENDING_NATIVE_AND_RENDER", "structure_sha256": report["sha256"], "static": "qa/static.json", "deployed": False})
    print(json.dumps({key: report[key] for key in ("status", "size", "records", "non_air", "palette_states", "lava_cells", "fire_cells", "reachable_dry_feet_positions", "sha256")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
