from __future__ import annotations

import copy
import json
import math
from collections import Counter, deque

from build_r6 import AIR, DIRECTIONS, REFERENCE, REFERENCE_HASH, ROOT, SIZE, Structure, hash_file, leaf_distances, state, write_json

PLANTS = {"minecraft:fern", "minecraft:oxeye_daisy", "minecraft:blue_orchid", "minecraft:azure_bluet", "minecraft:flowering_azalea"}
CROPS = {"minecraft:wheat", "minecraft:carrots"}
SOIL = {"minecraft:moss_block", "minecraft:rooted_dirt", "minecraft:dirt", "minecraft:grass_block"}
FORBIDDEN = {"lava", "fire", "soul_fire", "cobweb", "powder_snow", "spawner", "command_block", "chain_command_block", "repeating_command_block", "jigsaw", "structure_block", "end_portal", "nether_portal", "tnt"}


def full_support(block):
    return block.name in {"minecraft:stone_bricks", "minecraft:mossy_stone_bricks", "minecraft:cracked_stone_bricks", "minecraft:polished_andesite", "minecraft:glass", "minecraft:waxed_weathered_cut_copper", "minecraft:deepslate_tiles", "minecraft:coarse_dirt", "minecraft:rooted_dirt", "minecraft:moss_block", "minecraft:oak_log", "minecraft:spruce_log", "minecraft:shroomlight"} or block.name == "minecraft:spruce_slab" and block.get("type") == "top"


def walkable(structure, position):
    block_x, block_y, block_z = position
    return structure.get(position) == AIR and structure.get((block_x, block_y + 1, block_z)) == AIR and full_support(structure.get((block_x, block_y - 1, block_z)))


def validate(structure):
    errors = []

    def require(condition, code, position=None):
        if not condition:
            errors.append({"code": code, "position": position})

    require(structure.size == SIZE, "dimensions")
    require(structure.data_version == 3465, "data_version")
    require(len(structure.blocks) == math.prod(SIZE), "dense_complete_volume")
    require(not structure.entities and not structure.block_entities, "no_entities_or_block_entities")
    for position, block in structure.blocks.items():
        require(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)), "out_of_bounds", position)
        require(block.name.startswith("minecraft:") and block.name.split(":")[-1] not in FORBIDDEN, "forbidden_block", position)
        block_x, block_y, block_z = position
        below = structure.get((block_x, block_y - 1, block_z))
        above = structure.get((block_x, block_y + 1, block_z))
        if block.name in PLANTS:
            require(below.name in SOIL, "plant_support", position)
        if block.name in CROPS:
            require(below.name == "minecraft:farmland" and below.get("moisture") == "7", "crop_support", position)
            require(block.get("age") == "7", "mature_crop", position)
        if block.name.endswith("_leaves"):
            require(block.get("persistent") == "true", "persistent_leaves", position)
        if block.name in {"minecraft:cave_vines", "minecraft:cave_vines_plant"}:
            require(full_support(above) or above.name == "minecraft:cave_vines_plant", "vine_anchor", position)
            if block.name == "minecraft:cave_vines":
                require(block.get("age") == "25", "vine_growth_limit", position)
        if block.name == "minecraft:chain":
            require(above.name == "minecraft:chain" or full_support(above), "chain_anchor", position)
        if block.name == "minecraft:lantern":
            require((above.name == "minecraft:chain" or full_support(above)) if block.get("hanging") == "true" else full_support(below), "lantern_support", position)
        if block.name.startswith("minecraft:potted_"):
            require(full_support(below), "pot_support", position)
        if block.name == "minecraft:water":
            require(block.get("level") == "0", "source_water", position)
            require(full_support(below), "water_bottom", position)
            for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                neighbor = structure.get((block_x + delta_x, block_y, block_z + delta_z))
                require(full_support(neighbor) or neighbor.name in {"minecraft:water", "minecraft:farmland"}, "water_side_seal", position)
    for block_x in range(15):
        for block_z in range(17):
            require(full_support(structure.get((block_x, 0, block_z))), "floor_seal", (block_x, 0, block_z))
            require(structure.get((block_x, 9, block_z)).name in {"minecraft:glass", "minecraft:waxed_weathered_cut_copper"}, "roof_seal", (block_x, 9, block_z))
            if block_x in (0, 14) or block_z in (0, 16):
                for block_y in range(1, 9):
                    if block_z == 0 and 6 <= block_x <= 8 and block_y <= 3:
                        require(structure.get((block_x, block_y, block_z)) == AIR, "entrance_clearance", (block_x, block_y, block_z))
                    else:
                        require(full_support(structure.get((block_x, block_y, block_z))), "glass_shell", (block_x, block_y, block_z))
    for block_x in range(6, 9):
        for block_z in range(13):
            require(walkable(structure, (block_x, 1, block_z)), "dry_three_wide_aisle", (block_x, 1, block_z))
    reachable = {(7, 1, 0)}
    pending = deque(reachable)
    while pending:
        current = pending.popleft()
        for delta in (DIRECTIONS[0], DIRECTIONS[1], DIRECTIONS[4], DIRECTIONS[5]):
            neighbor = tuple(current[axis] + delta[axis] for axis in range(3))
            if 0 <= neighbor[0] < 15 and 0 <= neighbor[2] < 17 and neighbor not in reachable and walkable(structure, neighbor):
                reachable.add(neighbor)
                pending.append(neighbor)
    require((7, 1, 10) in reachable, "entrance_to_interior")
    recomputed = copy.deepcopy(structure)
    leaf_distances(recomputed)
    for position, block in structure.blocks.items():
        if block.name.endswith("_leaves"):
            require(block == recomputed.get(position), "leaf_distance", position)
    counts = Counter(block.name for block in structure.blocks.values())
    require(counts["minecraft:water"] == 8, "water_source_count")
    require(counts["minecraft:glass"] >= 300, "greenhouse_glass_identity")
    require(sum(counts[name] for name in CROPS) == 10, "crop_count")
    return {"status": "PASS" if not errors else "FAIL", "errors": errors, "size": list(structure.size), "block_records": len(structure.blocks), "nonair_blocks": sum(count for name, count in counts.items() if name != "minecraft:air"), "dry_reachable_cells": len(reachable), "reachable": sorted(reachable), "three_wide_aisle_cells": 39, "entrance_opening": [3, 3], "water_sources": counts["minecraft:water"], "glass_blocks": counts["minecraft:glass"], "persistent_leaves": sum(count for name, count in counts.items() if name.endswith("_leaves")), "full_roof_cells": 255, "limits": ["Static support predicates are intentionally conservative; native collision, registry and canSurvive are tested separately."]}


def main():
    structure = Structure.read(ROOT / "room.nbt")
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    assert hash_file(REFERENCE) == REFERENCE_HASH == metadata["reference_sha256"]
    assert hash_file(ROOT / "room.nbt") == metadata["sha256"]
    assert metadata["entrance"] == [7, 1, 0] and metadata["interior"] == [7, 1, 10] and metadata["facing"] == "north"
    report = validate(structure)
    write_json(ROOT / "qa/static.json", report)
    assert report["status"] == "PASS", report["errors"]
    mutations = [
        ("missing_roof", (2, 9, 2), AIR, "roof_seal"),
        ("broken_glass_shell", (0, 5, 2), AIR, "glass_shell"),
        ("blocked_entrance", (7, 1, 0), state("stone_bricks"), "entrance_clearance"),
        ("missing_aisle_floor", (7, 0, 6), AIR, "dry_three_wide_aisle"),
        ("broken_crop_support", (3, 1, 6), AIR, "crop_support"),
        ("unanchored_vine", (1, 9, 4), AIR, "vine_anchor"),
        ("leaking_irrigation", (5, 1, 8), AIR, "water_side_seal"),
        ("blocked_safety_point", (7, 1, 10), state("cobweb"), "entrance_to_interior"),
        ("unsupported_lantern", (7, 7, 4), AIR, "lantern_support")
    ]
    negative = []
    for name, position, replacement, expected in mutations:
        modified = copy.deepcopy(structure)
        modified.set(position, replacement)
        result = validate(modified)
        detected = {error["code"] for error in result["errors"]}
        negative.append({"case": name, "mutation": list(position), "expected_detection": expected, "status": "PASS" if expected in detected else "FAIL", "detected": sorted(detected)})
    write_json(ROOT / "qa/negative.json", {"status": "PASS" if all(case["status"] == "PASS" for case in negative) else "FAIL", "cases": negative, "scope": "In-memory mutations only; no corrupted structures or production world writes."})
    assert all(case["status"] == "PASS" for case in negative), negative
    print(json.dumps({"static": report["status"], "reachable": report["dry_reachable_cells"], "negative_cases": len(negative)}))


if __name__ == "__main__":
    main()
