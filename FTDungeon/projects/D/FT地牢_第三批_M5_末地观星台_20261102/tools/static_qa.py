from __future__ import annotations

import collections
import json
import math

from build_m5 import OUT, REFERENCE, REFERENCE_SHA256, ROOT, digest, dump
from nbtio import AIR, Structure, plain, read_nbt


def verify():
    room = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    structure = Structure.read(OUT / "room.nbt")
    _, root_tag = read_nbt(OUT / "room.nbt")
    root = plain(root_tag)
    blocks = structure.blocks
    checks = {}

    def require(name, condition):
        checks[name] = bool(condition)

    def air(position):
        return structure.get(position).name == "minecraft:air"

    def stand(position):
        block_x, height, block_z = position
        return all(0 <= coordinate < limit for coordinate, limit in zip(position, structure.size)) and air(position) and air((block_x, height + 1, block_z)) and structure.get((block_x, height - 1, block_z)).name in {"minecraft:end_stone_bricks", "minecraft:end_stone", "minecraft:purpur_block"}

    require("gzip_single_palette", (OUT / "room.nbt").read_bytes()[:2] == b"\x1f\x8b" and "palette" in root and "palettes" not in root)
    require("java_1_20_1_data_version", root["DataVersion"] == 3465)
    require("metadata_dimensions", room["size"] == list(structure.size) == root["size"])
    require("structure_fingerprint", room["sha256"] == digest(OUT / "room.nbt"))
    require("reference_fingerprint", room["reference_sha256"] == digest(REFERENCE) == REFERENCE_SHA256)
    require("all_coordinates_inside", all(all(0 <= coordinate < limit for coordinate, limit in zip(position, structure.size)) for position in blocks))
    require("full_volume_explicit", len(blocks) == math.prod(structure.size))
    require("no_entities", not structure.entities)
    require("no_block_entities_or_inventories", not structure.block_entities)
    require("vanilla_namespace", all(block.name.startswith("minecraft:") for block in blocks.values()))
    forbidden = {"water", "lava", "fire", "soul_fire", "magma_block", "powder_snow", "cobweb", "end_portal", "end_gateway", "nether_portal", "end_portal_frame", "spawner", "command_block", "repeating_command_block", "chain_command_block", "structure_block", "structure_void", "jigsaw", "tnt", "respawn_anchor", "budding_amethyst", "chest", "trapped_chest", "barrel"}
    require("no_danger_or_control_blocks", not any(block.name.removeprefix("minecraft:") in forbidden for block in blocks.values()))
    require("north_center_schema", room["facing"] == "north" and room["entrance"] == [structure.size[0] // 2, 1, 0])
    require("continuous_full_floor", all(structure.get((block_x, 0, block_z)).name in {"minecraft:end_stone", "minecraft:end_stone_bricks", "minecraft:purpur_block"} for block_x in range(15) for block_z in range(15)))
    require("complete_roof", all(not air((block_x, 9, block_z)) for block_x in range(15) for block_z in range(15)))
    connected = {position for position, block in blocks.items() if position[1] == 0 and block != AIR}
    solid_queue = collections.deque(connected)
    while solid_queue:
        position = solid_queue.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            if neighbor not in connected and structure.get(neighbor) != AIR:
                connected.add(neighbor)
                solid_queue.append(neighbor)
    require("all_structure_and_decorations_connected_to_floor", len(connected) == sum(block != AIR for block in blocks.values()))
    require("three_by_three_north_opening", all(air((block_x, height, 0)) for block_x in (6, 7, 8) for height in (1, 2, 3)))
    require("three_wide_entrance_floor", all(stand((block_x, 1, block_z)) for block_x in (6, 7, 8) for block_z in range(4)))
    require("safe_points", all(stand(tuple(room[key])) for key in ("entrance", "interior")))
    start = tuple(room["entrance"])
    queue = collections.deque([start])
    parents = {start: None}
    while queue:
        block_x, height, block_z = queue.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (block_x + offset_x, height, block_z + offset_z)
            if neighbor not in parents and stand(neighbor):
                parents[neighbor] = (block_x, height, block_z)
                queue.append(neighbor)
    destinations = [{"name": "interior", "position": room["interior"]}, *room["walk_stations"]]
    routes = []
    for station in destinations:
        endpoint = tuple(station["position"])
        require("dry_route_" + station["name"], endpoint in parents)
        path = []
        current = endpoint
        while current in parents:
            path.append(list(current))
            current = parents[current]
        routes.append({"name": station["name"], "steps": list(reversed(path))})
    supports = []
    for position, block in blocks.items():
        block_x, height, block_z = position
        below = structure.get((block_x, height - 1, block_z))
        if block.name == "minecraft:chorus_plant":
            supports.append({"position": list(position), "kind": "chorus", "pass": below.name in {"minecraft:chorus_plant", "minecraft:end_stone"}})
        elif block.name == "minecraft:chorus_flower":
            supports.append({"position": list(position), "kind": "inert_flower", "pass": below.name == "minecraft:chorus_plant" and block.get("age") == "5"})
        elif "amethyst" in block.name and block.name != "minecraft:amethyst_block":
            offset = {"up": (0, -1, 0), "west": (1, 0, 0)}[block.get("facing")]
            support = tuple(position[axis] + offset[axis] for axis in range(3))
            supports.append({"position": list(position), "kind": "amethyst", "pass": not air(support)})
        elif block.name == "minecraft:end_rod":
            offset = {"up": (0, -1, 0), "east": (-1, 0, 0)}[block.get("facing")]
            support = tuple(position[axis] + offset[axis] for axis in range(3))
            supports.append({"position": list(position), "kind": "rod_mount", "pass": not air(support)})
    require("decorations_have_support", all(record["pass"] for record in supports))
    result = {
        "id": "M5", "status": "PASS" if all(checks.values()) else "FAIL",
        "scope": "Static NBT and full-volume topology; native registries and world placement are separate evidence.",
        "sha256": room["sha256"], "reference_sha256": REFERENCE_SHA256,
        "size": room["size"], "block_records": len(blocks),
        "non_air_blocks": sum(block != AIR for block in blocks.values()),
        "palette_states": len(root["palette"]), "reachable_flat_feet": len(parents),
        "checks": checks, "supports": supports, "routes": routes,
        "materials": dict(collections.Counter(block.name for block in blocks.values() if block != AIR).most_common()),
        "native_placement": {"status": "NOT_RUN", "client_gameplay_tested": False},
        "deployment": {"deployed": False, "player_worlds_opened": False}
    }
    dump(ROOT / "qa/static-qa.json", result)
    dump(OUT / "qa.json", result)
    print(json.dumps({"static_qa": result["status"], "checks": len(checks), "failed": [name for name, passed in checks.items() if not passed], "walkable_feet": len(parents)}, ensure_ascii=False))
    if result["status"] != "PASS":
        raise SystemExit(1)


if __name__ == "__main__":
    verify()
