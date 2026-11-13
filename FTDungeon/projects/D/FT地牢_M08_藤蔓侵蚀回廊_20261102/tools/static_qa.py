from __future__ import annotations

import json
import math
from collections import deque

from build_m08 import DESTINATIONS, ENTRANCE, OUT, REFERENCE, REFERENCE_SHA, ROOT, SIZE, digest, dump
from nbtio import AIR, Structure, plain, read_nbt


def main():
    room = Structure.read(OUT / "room.nbt")
    metadata = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    checks = []

    def check(name, success, details=None):
        checks.append({"name": name, "status": "PASS" if success else "FAIL", "details": details})

    check("reference_sha256", digest(REFERENCE) == REFERENCE_SHA)
    check("delivery_sha256", metadata["sha256"] == digest(OUT / "room.nbt"))
    check("size_and_data_version", room.size == SIZE and room.data_version == 3465 and metadata["size"] == list(SIZE))
    _, root = read_nbt(OUT / "room.nbt")
    check("single_palette", "palette" in root.value and "palettes" not in root.value)
    check("explicit_volume_no_duplicates", len(room.blocks) == math.prod(SIZE))
    check("bounds", all(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)) for position in room.blocks))
    check("vanilla_only", all(block.name.startswith("minecraft:") for block in room.blocks.values()))
    forbidden = {"spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "structure_void", "jigsaw", "lava", "water", "fire", "soul_fire", "powder_snow", "nether_portal", "end_portal", "tnt"}
    check("no_hazards_or_runtime_blocks", all(block.name.removeprefix("minecraft:") not in forbidden and block.get("waterlogged") != "true" for block in room.blocks.values()))
    check("no_entities", not room.entities)
    barrel_positions = {position for position, block in room.blocks.items() if block.name == "minecraft:barrel"}
    check("seven_empty_barrels", len(barrel_positions) == 7 and set(room.block_entities) == barrel_positions and all(plain(tag) == {"id": "minecraft:barrel", "Items": []} for tag in room.block_entities.values()))
    check("continuous_full_floor", all(room.get((block_x, 0, block_z)) != AIR and not room.get((block_x, 0, block_z)).name.endswith(("slab", "stairs")) for block_x in range(19) for block_z in range(23)))
    check("complete_roof", all(room.get((block_x, 10, block_z)) != AIR for block_x in range(19) for block_z in range(23)))
    check("north_3x3", metadata["facing"] == "north" and metadata["entrance"] == list(ENTRANCE) and all(room.get((block_x, height, 0)) == AIR for block_x in range(8, 11) for height in range(1, 4)))
    check("opening_floor", all(room.get((block_x, 0, 0)) != AIR for block_x in range(8, 11)))
    check("five_wide_clear_central_lane", all(room.get((block_x, height, block_z)) == AIR for block_x in range(7, 12) for block_z in range(1, 17) for height in range(1, 4)))
    check("lantern_chain_support", all(room.get((block_x, 5, block_z)).name == "minecraft:lantern" and all(room.get((block_x, height, block_z)).name == "minecraft:chain" for height in (6, 7)) and room.get((block_x, 8, block_z)).name == "minecraft:stripped_spruce_log" for block_x in (5, 13) for block_z in (7, 17)))
    sides = {"west": (-1, 0, 0), "east": (1, 0, 0), "north": (0, 0, -1), "south": (0, 0, 1), "up": (0, 1, 0)}
    vines = [(position, block) for position, block in room.blocks.items() if block.name == "minecraft:vine"]
    check("vines_have_direct_mounts", all(any(block.get(side) == "true" and room.get(tuple(position[axis] + offset[axis] for axis in range(3))) != AIR for side, offset in sides.items()) for position, block in vines), len(vines))
    leaves = [block for block in room.blocks.values() if block.name == "minecraft:oak_leaves"]
    check("persistent_nondecaying_leaves", leaves and all(block.get("persistent") == "true" for block in leaves), len(leaves))
    decorative = [(position, block) for position, block in room.blocks.items() if block.name in ("minecraft:fern", "minecraft:barrel")]
    check("floor_decor_support", all(room.get((position[0], position[1] - 1, position[2])) != AIR for position, _ in decorative))
    blocks = {position for position, block in room.blocks.items() if block != AIR}
    connected = set(position for position in blocks if position[1] == 0)
    pending = deque(connected)
    while pending:
        position = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            if neighbor in blocks and neighbor not in connected:
                connected.add(neighbor)
                pending.append(neighbor)
    check("all_decor_structurally_connected", connected == blocks, {"connected": len(connected), "non_air": len(blocks)})

    def standable(position):
        block_x, height, block_z = position
        return 0 <= block_x < 19 and 0 <= block_z < 23 and room.get(position) == AIR and room.get((block_x, height + 1, block_z)) == AIR and room.get((block_x, height - 1, block_z)) != AIR

    predecessors = {ENTRANCE: None}
    pending = deque([ENTRANCE])
    while pending:
        position = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (position[0] + offset_x, 1, position[2] + offset_z)
            if neighbor not in predecessors and standable(neighbor):
                predecessors[neighbor] = position
                pending.append(neighbor)
    routes = []
    for destination in DESTINATIONS:
        path = []
        if destination in predecessors:
            cursor = destination
            while cursor is not None:
                path.append(cursor)
                cursor = predecessors[cursor]
        path.reverse()
        routes.append({"destination": destination, "status": "PASS" if path else "FAIL", "steps": path})
    check("entrance_and_five_return_routes", standable(ENTRANCE) and all(route["steps"] for route in routes))
    check("interior_feet", metadata["interior"] == list(DESTINATIONS[0]) and DESTINATIONS[0] in predecessors)
    check("theme_not_empty_box", len(vines) >= 100 and len(leaves) >= 20 and len(barrel_positions) == 7 and room.get((9, 2, 19)).name.endswith("slab"))
    status = "PASS" if all(check["status"] == "PASS" for check in checks) else "FAIL"
    dump(ROOT / "qa/static-qa.json", {"id": "M08", "status": status, "sha256": digest(OUT / "room.nbt"), "checks": checks, "routes": routes, "reachable_feet": len(predecessors), "scope": "Static NBT checks only. Native collision/survival and tick stability are separate evidence."})
    print(f"M08 static QA: {status}; {sum(item['status'] == 'PASS' for item in checks)}/{len(checks)} checks; reachable feet={len(predecessors)}")
    for item in checks:
        if item["status"] != "PASS":
            print(item)
    if status != "PASS":
        raise SystemExit(1)


if __name__ == "__main__":
    main()
