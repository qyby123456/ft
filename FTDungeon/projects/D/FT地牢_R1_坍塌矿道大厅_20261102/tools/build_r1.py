from __future__ import annotations

import hashlib
import json
from collections import Counter, deque
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (17, 11, 17)
ENTRANCE = (8, 1, 0)
INTERIOR = (8, 1, 4)
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R1-collapsed-mine-hall.png")
REFERENCE_SHA256 = "cfe244bb4c9220b41ecd464b4c02c5fd33192d268e6f727372713ebcfb2a86df"


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def barrel(structure, position):
    structure.set(position, state("barrel", facing="up", open=False), compound(id=string("minecraft:barrel"), Items=Tag(9, [], 10)))


def build():
    if digest(REFERENCE) != REFERENCE_SHA256:
        raise ValueError("Reference fingerprint mismatch")
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (16, 10, 16), AIR)
    floors = ["deepslate_bricks", "cobbled_deepslate", "polished_andesite", "cracked_deepslate_bricks", "tuff"]
    walls = ["deepslate_bricks", "cobbled_deepslate", "cracked_deepslate_bricks", "tuff"]
    for pos_x in range(17):
        for pos_z in range(17):
            material = floors[(pos_x * 13 + pos_z * 7 + pos_x * pos_z) % 19 % len(floors)]
            if 7 <= pos_x <= 9:
                material = "stone_bricks" if pos_z % 5 else "cracked_stone_bricks"
            structure.set((pos_x, 0, pos_z), state(material))
            structure.set((pos_x, 10, pos_z), state("deepslate_tiles"))
            if pos_x in (0, 16) or pos_z in (0, 16):
                for pos_y in range(1, 10):
                    structure.set((pos_x, pos_y, pos_z), state(walls[(pos_x * 3 + pos_z * 5 + pos_y * 7) % 13 % len(walls)]))
    for pos_x, pos_z in ((0, 0), (16, 0), (0, 16), (16, 16)):
        structure.box((pos_x, 1, pos_z), (pos_x, 9, pos_z), state("polished_deepslate"))
    for pos_x in range(6, 11):
        structure.box((pos_x, 4, 0), (pos_x, 5, 0), state("deepslate_bricks"))
    structure.box((7, 1, 0), (9, 3, 0), AIR)
    for pos_x in (6, 10):
        structure.box((pos_x, 1, 0), (pos_x, 3, 0), state("polished_deepslate"))
    for pos_z in (5, 13):
        for pos_x in (3, 13):
            structure.box((pos_x, 1, pos_z), (pos_x, 6, pos_z), state("stripped_spruce_log", axis="y"))
            structure.set((pos_x, 1, pos_z), state("polished_deepslate"))
            structure.box((pos_x, 8, pos_z), (pos_x, 9, pos_z), state("stripped_spruce_log", axis="y"))
        structure.box((3, 7, pos_z), (13, 7, pos_z), state("stripped_spruce_log", axis="x"))
        structure.box((0, 9, pos_z), (16, 9, pos_z), state("spruce_log", axis="x"))
        for pos_x in (6, 10):
            structure.box((pos_x, 5, pos_z), (pos_x, 6, pos_z), state("chain", axis="y", waterlogged=False))
            structure.set((pos_x, 4, pos_z), state("lantern", hanging=True, waterlogged=False))
    for pos_x in (3, 13):
        structure.box((pos_x, 7, 5), (pos_x, 7, 13), state("stripped_spruce_log", axis="z"))
    for pos_z in range(1, 15):
        structure.set((8, 1, pos_z), state("rail", shape="north_south", waterlogged=False))
    for pos_z in range(8, 14):
        structure.set((5, 1, pos_z), state("rail", shape="north_south", waterlogged=False))
    structure.set((5, 1, 10), state("cauldron"))
    structure.set((4, 1, 10), state("coal_block"))
    structure.set((4, 1, 11), state("deepslate_coal_ore"))
    for pos_z in range(7, 10):
        for pos_x in (1, 2):
            structure.set((pos_x, 1, pos_z), state("spruce_fence", north=pos_z > 7, south=pos_z < 9, east=pos_x == 1, west=True, waterlogged=False))
            structure.set((pos_x, 2, pos_z), state("spruce_slab", type="top", waterlogged=False))
    structure.set((2, 3, 8), state("lantern", hanging=False, waterlogged=False))
    for pos_z in (7, 9):
        structure.set((1, 4, pos_z), state("tripwire_hook", facing="east", attached=False, powered=False))
    structure.set((2, 1, 11), state("anvil", facing="north"))
    structure.set((2, 1, 12), state("crafting_table"))
    for position in ((2, 1, 14), (3, 1, 14), (3, 2, 14), (14, 1, 13), (14, 1, 14), (14, 2, 14)):
        barrel(structure, position)
    structure.set((14, 3, 14), state("deepslate_iron_ore"))
    collapse = {
        (11, 7): 1, (12, 7): 1, (13, 7): 2, (14, 7): 2, (15, 7): 3,
        (11, 8): 1, (12, 8): 2, (13, 8): 2, (14, 8): 3, (15, 8): 4,
        (11, 9): 1, (12, 9): 1, (13, 9): 2, (14, 9): 3, (15, 9): 3,
        (12, 10): 1, (13, 10): 1, (14, 10): 2, (15, 10): 3,
        (13, 11): 1, (14, 11): 1, (15, 11): 2,
    }
    for (pos_x, pos_z), height in collapse.items():
        for pos_y in range(1, height + 1):
            structure.set((pos_x, pos_y, pos_z), state("cobbled_deepslate" if (pos_x + pos_z + pos_y) % 3 else "tuff"))
    for position in ((11, 1, 7), (12, 2, 8), (13, 2, 9), (14, 2, 10), (15, 3, 11)):
        structure.set(position, state("gravel"))
    for pos_x in range(11, 15):
        structure.set((pos_x, 1, 6), state("stripped_spruce_log", axis="x"))
    for position in ((13, 2, 8), (14, 3, 8), (15, 4, 8)):
        structure.set(position, state("stripped_spruce_log", axis="x"))
    for position in ((10, 1, 8), (10, 1, 9), (11, 1, 10), (12, 1, 11), (15, 3, 12)):
        if structure.get((position[0], position[1] - 1, position[2])) == AIR:
            structure.box((position[0], 1, position[2]), (position[0], position[1] - 1, position[2]), state("cobbled_deepslate"))
        structure.set(position, state("cobbled_deepslate_slab", type="bottom", waterlogged=False))
    for position in ((0, 3, 6), (0, 4, 6), (0, 3, 10), (16, 4, 6), (16, 5, 6), (16, 4, 12), (15, 0, 3)):
        structure.set(position, state("deepslate_iron_ore"))
    for position in ((0, 4, 12), (0, 5, 12), (16, 5, 10), (16, 6, 10), (11, 0, 2)):
        structure.set(position, state("deepslate_coal_ore"))
    structure.write(ROOT / "room.nbt")
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / "src/main/resources/data/ftr1verification/structures/empty.nbt")
    write_json(ROOT / "room.json", {
        "id": "R1", "name": "坍塌矿道大厅", "size": SIZE,
        "entrance": ENTRANCE, "interior": INTERIOR, "facing": "north",
        "sha256": digest(ROOT / "room.nbt"), "reference": str(REFERENCE),
        "reference_sha256": REFERENCE_SHA256, "reference_size": [1536, 1024],
        "data_version": 3465, "planned_template_id": "ftdungeonmobs:image25/r1",
        "entrance_clearance": [3, 3], "roof": "complete sealed stone roof; cutaway only in preview",
        "entities": 0, "loot": "six empty vanilla barrels; no rewards or loot tables",
        "cart_representation": "static empty cauldron ore tub beside track, not a minecart entity",
        "collapse_bounds": [[10, 1, 6], [15, 4, 12]],
        "walkway": "three-wide central strip x=7..9, z=0..15; rails have no collision",
        "landmarks": {"workbench": [3, 1, 8], "ore_tub": [6, 1, 10], "barrels_west": [4, 1, 14], "collapse_view": [9, 1, 9], "barrels_east": [12, 1, 14], "rear_frame": [8, 1, 14]},
        "deployment": "not deployed; no catalog, fixed slot, shared road or production changes",
    })
    report = static_qa()
    write_json(ROOT / "qa/static.json", report)
    write_json(ROOT / "qa.json", {"id": "R1", "status": "PENDING_NATIVE", "sha256": digest(ROOT / "room.nbt"), "static": report, "native": {"status": "NOT_RUN"}, "preview": {"status": "NOT_RENDERED"}})
    print(json.dumps(report, ensure_ascii=False))


def static_qa():
    structure = Structure.read(ROOT / "room.nbt")
    checks = {}

    def check(name, condition):
        checks[name] = bool(condition)
        if not condition:
            raise ValueError("Static QA failed: " + name)

    check("size_and_data_version", structure.size == SIZE and structure.data_version == 3465)
    check("full_explicit_volume_unique", len(structure.blocks) == 17 * 11 * 17)
    check("vanilla_only", all(block.name.startswith("minecraft:") for block in structure.blocks.values()))
    forbidden = {"command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "lava", "water", "fire", "soul_fire", "cobweb", "powder_snow"}
    check("no_forbidden_or_route_hazards", not any(block.name.removeprefix("minecraft:") in forbidden for block in structure.blocks.values()))
    check("full_floor", all(structure.get((pos_x, 0, pos_z)) != AIR for pos_x in range(17) for pos_z in range(17)))
    check("complete_roof", all(structure.get((pos_x, 10, pos_z)) != AIR for pos_x in range(17) for pos_z in range(17)))
    check("closed_perimeter_except_north_port", all(structure.get((pos_x, pos_y, pos_z)) != AIR for pos_x in range(17) for pos_y in range(1, 10) for pos_z in range(17) if (pos_x in (0, 16) or pos_z in (0, 16)) and not (pos_z == 0 and 7 <= pos_x <= 9 and 1 <= pos_y <= 3)))
    check("north_three_by_three_air_port", all(structure.get((pos_x, pos_y, 0)) == AIR for pos_x in range(7, 10) for pos_y in range(1, 4)))
    passable = {"minecraft:air", "minecraft:rail"}

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)) and structure.get((pos_x, pos_y - 1, pos_z)) != AIR and all(structure.get((pos_x, height, pos_z)).name in passable for height in (pos_y, pos_y + 1))

    reached = {ENTRANCE}
    pending = deque(reached)
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            candidate = (pos_x + delta_x, pos_y, pos_z + delta_z)
            if candidate not in reached and walkable(candidate):
                reached.add(candidate)
                pending.append(candidate)
    check("entrance_interior_two_way_route", INTERIOR in reached)
    check("three_wide_central_walkway", all((pos_x, 1, pos_z) in reached for pos_x in range(7, 10) for pos_z in range(16)))
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    check("landmarks_reachable", all(tuple(position) in reached for position in metadata["landmarks"].values()))
    rubble = {"minecraft:cobbled_deepslate", "minecraft:tuff", "minecraft:gravel", "minecraft:stripped_spruce_log", "minecraft:cobbled_deepslate_slab"}
    collapse_positions = [position for position, block in structure.blocks.items() if 10 <= position[0] <= 15 and 6 <= position[2] <= 12 and 1 <= position[1] <= 4 and block.name in rubble]
    check("collapse_grounded_no_floating", all(structure.get((position[0], position[1] - 1, position[2])) != AIR for position in collapse_positions))
    check("all_gravity_blocks_supported", all(structure.get((position[0], position[1] - 1, position[2])) != AIR for position, block in structure.blocks.items() if block.name in ("minecraft:gravel", "minecraft:anvil")))
    check("grounded_posts_and_roof_frames", all(structure.get((pos_x, pos_y, pos_z)) != AIR for pos_x in (3, 13) for pos_z in (5, 13) for pos_y in range(11)))
    for position, block in structure.blocks.items():
        if block.name == "minecraft:lantern":
            direction = 1 if block.get("hanging") == "true" else -1
            check("lantern_anchor_" + "_".join(map(str, position)), structure.get((position[0], position[1] + direction, position[2])) != AIR)
    check("no_entities", not structure.entities)
    check("six_empty_barrels_only", len(structure.block_entities) == 6 and all(set(entity.value) == {"id", "Items"} and entity.value["id"].value == "minecraft:barrel" and not entity.value["Items"].value for entity in structure.block_entities.values()))
    materials = Counter(block.name for block in structure.blocks.values() if block != AIR)
    return {"status": "PASS", "sha256": digest(ROOT / "room.nbt"), "size": SIZE, "records": len(structure.blocks), "non_air_blocks": sum(materials.values()), "palette_states": len(set(structure.blocks.values())), "reachable_floor_cells": len(reached), "collapse_blocks_checked": len(collapse_positions), "checks": checks, "materials": dict(materials), "scope": "static checks only; native collision and survival require Forge test"}


if __name__ == "__main__":
    build()
