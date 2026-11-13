from __future__ import annotations

import collections
import json
import math

from build_m09 import DESTINATIONS, ENTRANCE, OUT, REFERENCE, REFERENCE_SHA256, ROOT, SIZE, digest, dump
from nbtio import AIR, Structure, plain, read_nbt


def verify():
    room = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    structure = Structure.read(OUT / "room.nbt")
    _, root_tag = read_nbt(OUT / "room.nbt")
    data = plain(root_tag)
    checks = {}

    def require(name, condition):
        checks[name] = bool(condition)

    def air(position):
        return structure.get(position) == AIR

    def walkable(position):
        block_x, height, block_z = position
        return 0 <= block_x < SIZE[0] and 0 <= block_z < SIZE[2] and height == 1 and air(position) and air((block_x, 2, block_z)) and not air((block_x, 0, block_z))

    require("gzip_single_palette", (OUT / "room.nbt").read_bytes()[:2] == b"\x1f\x8b" and "palette" in data and "palettes" not in data)
    require("data_version_3465", data["DataVersion"] == 3465)
    require("reference_sha256", digest(REFERENCE) == REFERENCE_SHA256 == room["reference_sha256"])
    require("template_sha256", digest(OUT / "room.nbt") == room["sha256"])
    require("dimensions_and_full_volume", tuple(data["size"]) == SIZE == structure.size and len(structure.blocks) == math.prod(SIZE))
    require("all_positions_in_bounds", all(all(0 <= coordinate < bound for coordinate, bound in zip(position, SIZE)) for position in structure.blocks))
    require("vanilla_only", all(block.name.startswith("minecraft:") for block in structure.blocks.values()))
    require("no_entities", not data["entities"])
    require("no_embedded_inventory_or_loot_nbt", not structure.block_entities)
    forbidden = {"water", "lava", "fire", "soul_fire", "magma_block", "powder_snow", "spawner", "command_block", "repeating_command_block", "chain_command_block", "structure_block", "structure_void", "jigsaw", "tnt", "nether_portal", "end_portal", "end_gateway", "end_portal_frame", "sand", "gravel", "suspicious_sand", "suspicious_gravel"}
    require("no_fluids_fire_rewards_or_control_blocks", all(block.name.removeprefix("minecraft:") not in forbidden for block in structure.blocks.values()))
    require("centered_north_entrance", room["entrance"] == list(ENTRANCE) and room["facing"] == "north")
    require("entrance_three_wide_three_high", all(air((block_x, height, 0)) for block_x in range(9, 12) for height in range(1, 4)))
    require("continuous_floor", all(not air((block_x, 0, block_z)) for block_x in range(21) for block_z in range(21)))
    require("complete_roof", all(not air((block_x, 11, block_z)) for block_x in range(21) for block_z in range(21)))
    require("sealed_outer_walls_except_entrance", all(not air((block_x, height, block_z)) for block_x in range(21) for block_z in range(21) for height in range(1, 11) if block_x in (0, 20) or block_z in (0, 20) if not (block_z == 0 and 9 <= block_x <= 11 and 1 <= height <= 3)))
    parents = {ENTRANCE: None}
    pending = collections.deque([ENTRANCE])
    while pending:
        block_x, height, block_z = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (block_x + offset_x, height, block_z + offset_z)
            if neighbor not in parents and walkable(neighbor):
                parents[neighbor] = (block_x, height, block_z)
                pending.append(neighbor)
    require("dry_return_routes", all(destination in parents for destination in DESTINATIONS))
    routes = []
    for destination in DESTINATIONS:
        steps = []
        current = destination
        while current in parents:
            steps.append(current)
            current = parents[current]
        routes.append({"destination": destination, "steps": list(reversed(steps))})
    bookshelf_supports = []
    for position, block in structure.blocks.items():
        if block.name == "minecraft:bookshelf":
            below = (position[0], position[1] - 1, position[2])
            bookshelf_supports.append(not air(below))
    require("bookshelves_directly_supported", len(bookshelf_supports) >= 100 and all(bookshelf_supports))
    require("roof_beams_anchor_in_walls", all(not air((block_x, 9, block_z)) for block_z in (3, 10, 18) for block_x in range(21)))
    connected = {position for position, block in structure.blocks.items() if position[1] == 0 and block != AIR}
    pending = collections.deque(connected)
    while pending:
        position = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            if neighbor not in connected and not air(neighbor):
                connected.add(neighbor)
                pending.append(neighbor)
    floating = [position for position, block in structure.blocks.items() if block != AIR and position not in connected]
    require("all_fixtures_connected_to_foundation", not floating)
    counts = collections.Counter(block.name for block in structure.blocks.values())
    require("library_identity_not_empty_shell", counts["minecraft:bookshelf"] >= 100 and counts["minecraft:lectern"] == 1 and counts["minecraft:enchanting_table"] == 1)
    result = {
        "id": "M09", "status": "PASS" if all(checks.values()) else "FAIL", "sha256": digest(OUT / "room.nbt"),
        "checks": checks, "records": len(structure.blocks), "palette_states": len(data["palette"]), "non_air": len(structure.blocks) - counts["minecraft:air"],
        "reachable_feet": len(parents), "routes": routes, "bookshelf_support_checks": len(bookshelf_supports), "floating_components": floating,
        "materials": dict(sorted(counts.items())), "limitations": "Static connectivity is not native collision or block support. Native GameTest must separately pass for all rotations and ticks.",
    }
    dump(ROOT / "qa/static-qa.json", result)
    print(f"M09_STATIC_QA_{result['status']}: {len(checks)} checks; reachable={len(parents)}; shelves={len(bookshelf_supports)}")
    if result["status"] != "PASS":
        raise SystemExit("Failed: " + ", ".join(name for name, passed in checks.items() if not passed))


if __name__ == "__main__":
    verify()
