from __future__ import annotations

import hashlib
from collections import Counter, deque

from build_m07 import ENTRANCE, FEET, INTERIOR, REFERENCE, REFERENCE_SHA, ROOT, SIZE, TARGETS, dump
from nbtio import AIR, plain


DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1))
SOLIDS = {"stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "chiseled_stone_bricks", "cobblestone", "mossy_cobblestone", "polished_andesite", "andesite", "deepslate_bricks", "spruce_log", "spruce_planks", "iron_block", "hay_block"}


def short(block):
    return block.name.removeprefix("minecraft:")


def solid(room, position):
    return short(room.get(position)) in SOLIDS


def static_review(room):
    checks = {}

    def check(name, condition):
        checks[name] = bool(condition)
        if not condition:
            raise AssertionError(name)

    check("reference_sha256", hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_SHA)
    check("data_version_3465", room.data_version == 3465)
    check("dense_volume_3315_unique_positions", len(room.blocks) == 3315 and room.size == SIZE)
    check("all_positions_inside_declared_volume", all(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)) for position in room.blocks))
    check("vanilla_namespace_only", all(block.name.startswith("minecraft:") for block in room.blocks.values()))
    check("no_preplaced_entities", not room.entities)
    forbidden = {"command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "spawner", "chest", "barrel", "dispenser", "dropper", "hopper", "tnt", "fire", "soul_fire", "lava", "cobweb", "powder_snow"}
    check("no_forbidden_blocks_rewards_or_exposed_hazards", all(short(block) not in forbidden for block in room.blocks.values()))
    check("skull_block_entities_only_no_items_or_loot", len(room.block_entities) == 2 and all(plain(value) == {"id": "minecraft:skull"} for value in room.block_entities.values()))
    check("continuous_solid_bottom_foundation", all(solid(room, (block_x, 0, block_z)) for block_x in range(15) for block_z in range(17)))
    check("complete_two_layer_roof", all(solid(room, (block_x, block_y, block_z)) for block_x in range(15) for block_z in range(17) for block_y in (11, 12)))
    check("only_north_3_by_3_interface", all(short(room.get((block_x, block_y, block_z))) == "air" if block_z == 0 and block_x in (6, 7, 8) and block_y in (4, 5, 6) else solid(room, (block_x, block_y, block_z)) for block_x in range(15) for block_z in range(17) if block_x in (0, 14) or block_z in (0, 16) for block_y in range(4, 11)))
    check("entrance_floor_and_three_deep_headroom", all(solid(room, (block_x, 3, block_z)) and all(room.get((block_x, block_y, block_z)) == AIR for block_y in (4, 5, 6)) for block_x in (6, 7, 8) for block_z in (0, 1, 2)))

    def dry(position):
        block_x, block_y, block_z = position
        return solid(room, (block_x, block_y - 1, block_z)) and all(room.get((block_x, block_y + height, block_z)) == AIR for height in (0, 1))

    reachable = {ENTRANCE}
    pending = deque([ENTRANCE])
    while pending:
        position = pending.popleft()
        for step in DIRECTIONS:
            neighbour = tuple(coordinate + offset for coordinate, offset in zip(position, step))
            if 0 <= neighbour[0] < 15 and 0 <= neighbour[2] < 17 and neighbour not in reachable and dry(neighbour):
                reachable.add(neighbour)
                pending.append(neighbour)
    check("dry_entrance_interior_and_full_ring_routes", all(position in reachable for position in (INTERIOR, *TARGETS)))
    check("three_wide_front_approach", all((block_x, FEET, block_z) in reachable for block_x in (6, 7, 8) for block_z in range(6)))
    water_positions = [position for position, block in room.blocks.items() if short(block) == "water"]
    check("water_sources_in_interior_only", len(water_positions) == 33 and all(0 < position[0] < 14 and 0 < position[1] < 11 and 0 < position[2] < 16 and room.get(position).get("level") == "0" for position in water_positions))
    water_sealed = True
    for position in water_positions:
        for step in (*DIRECTIONS, (0, -1, 0), (0, 1, 0)):
            neighbour = tuple(coordinate + offset for coordinate, offset in zip(position, step))
            neighbour_state = room.get(neighbour)
            water_sealed &= short(neighbour_state) == "water" or solid(room, neighbour) or short(neighbour_state) == "iron_trapdoor" and neighbour_state.get("open") == "false"
    check("water_has_solid_sides_bottom_and_closed_lid", water_sealed)
    attachments = True
    for position, block in room.blocks.items():
        block_x, block_y, block_z = position
        name = short(block)
        if name == "chain":
            anchor_y = block_y + 1
            while short(room.get((block_x, anchor_y, block_z))) == "chain":
                anchor_y += 1
            attachments &= solid(room, (block_x, anchor_y, block_z))
        if name == "lantern":
            attachments &= short(room.get((block_x, block_y + 1, block_z))) == "chain"
        if name in ("wall_torch", "lever", "tripwire_hook"):
            support_step = {"east": (-1, 0, 0), "west": (1, 0, 0), "north": (0, 0, 1), "south": (0, 0, -1)}[block.get("facing")]
            support = tuple(coordinate + offset for coordinate, offset in zip(position, support_step))
            attachments &= solid(room, support)
        if name in ("skeleton_skull", "cauldron", "grindstone"):
            attachments &= solid(room, (block_x, block_y - 1, block_z))
        if name == "iron_trapdoor":
            attachments &= solid(room, (block_x, block_y - 1, block_z)) or any(solid(room, tuple(coordinate + offset for coordinate, offset in zip(position, step))) for step in DIRECTIONS)
    check("all_lantern_chains_hooks_controls_skulls_grates_supported", attachments)
    check("roof_beams_bear_on_full_height_masonry", all(solid(room, (block_x, block_y, block_z)) for block_x in (1, 13) for block_z in (4, 9, 14) for block_y in range(4, 11)))
    check("well_guarded_all_four_sides", all(solid(room, (block_x, 4, block_z)) for block_x in range(5, 10) for block_z in range(7, 12) if block_x in (5, 9) or block_z in (7, 11)))
    counts = Counter(short(block) for block in room.blocks.values())
    check("theme_features_present", counts["iron_trapdoor"] == 18 and counts["iron_bars"] == 18 and counts["lantern"] == 4 and counts["skeleton_skull"] == 2 and counts["tripwire_hook"] == 4 and counts["lever"] == counts["grindstone"] == counts["cauldron"] == 1)
    dump(ROOT / "qa/reachable.json", sorted(reachable))
    result = {"id": "M07", "status": "PASS", "checks": checks, "size": list(SIZE), "records": len(room.blocks), "non_air_records": len(room.blocks) - counts["air"], "dry_reachable_positions": len(reachable), "water_source_cells": len(water_positions), "materials": dict(sorted(counts.items())), "limitations": ["Static structural checks are not Minecraft runtime evidence; see forge-native.json", "The winch, lever, shackles and grates are architectural props, not scripted traps"]}
    dump(ROOT / "qa/static.json", result)
    return result
