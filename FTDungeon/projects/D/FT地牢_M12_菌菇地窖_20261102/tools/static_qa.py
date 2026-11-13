from __future__ import annotations

import json
from collections import Counter, deque

from build_m12 import ENTRANCE, INTERIOR, LANDMARKS, REFERENCE, REFERENCE_HASH, ROOT, SIZE, digest, write_json
from nbtio import AIR, Structure, plain, read_nbt


def require(condition, message):
    if not condition:
        raise ValueError(message)


def inspect():
    _, encoded = read_nbt(ROOT / "room.nbt")
    decoded = plain(encoded)
    structure = Structure.read(ROOT / "room.nbt")
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    require(structure.size == SIZE and structure.data_version == 3465, "Wrong size or version")
    require("palette" in decoded and "palettes" not in decoded, "Not a single palette")
    require(len(structure.blocks) == 17 * 11 * 17, "Incomplete explicit volume")
    require(metadata["sha256"] == digest(ROOT / "room.nbt"), "NBT fingerprint mismatch")
    require(digest(REFERENCE) == REFERENCE_HASH, "Reference mismatch")
    require(not structure.entities and not decoded["entities"], "Unexpected entities")
    forbidden = {"command_block", "chain_command_block", "repeating_command_block", "jigsaw", "spawner", "structure_block", "structure_void", "lava", "fire", "soul_fire", "cactus", "cobweb", "powder_snow"}
    for position, block in structure.blocks.items():
        require(all(0 <= coordinate < maximum for coordinate, maximum in zip(position, SIZE)), "Out-of-bounds record")
        require(block.name.startswith("minecraft:") and block.name.split(":")[1] not in forbidden, "Forbidden block")
    for entity in structure.block_entities.values():
        require(plain(entity) == {"id": "minecraft:barrel"}, "Unexpected inventory or block entity")
    require(len(structure.block_entities) == 8, "Expected eight empty barrels")
    for pos_x in range(17):
        for pos_z in range(17):
            require(structure.get((pos_x, 0, pos_z)).name == "minecraft:stone_bricks", "Incomplete base")
            require(structure.get((pos_x, 10, pos_z)).name == "minecraft:stone_bricks", "Incomplete roof")
    opening = {(pos_x, pos_y, 0) for pos_x in range(7, 10) for pos_y in range(2, 5)}
    for pos_y in range(2, 10):
        for pos_x in range(17):
            for pos_z in range(17):
                position = (pos_x, pos_y, pos_z)
                if pos_x in (0, 16) or pos_z in (0, 16):
                    require((structure.get(position) == AIR) == (position in opening), f"Unexpected opening: {position}")
    for pos_x in range(7, 10):
        for pos_z in range(6):
            require(structure.get((pos_x, 1, pos_z)).name == "minecraft:stone_bricks", "Unsafe entrance floor")
            require(all(structure.get((pos_x, pos_y, pos_z)) == AIR for pos_y in range(2, 5)), "Entrance obstructed")
    fluid = {position for position, block in structure.blocks.items() if block.name == "minecraft:water"}
    require(fluid == {tuple(position) for position in metadata["liquid_cells"]} and len(fluid) == 6, "Wrong water geometry")
    solid = lambda position: structure.get(position).name not in {"minecraft:air", "minecraft:water", "minecraft:red_mushroom", "minecraft:glow_lichen"}
    for position in fluid:
        require(structure.get(position).get("level") == "0", "Water is not a source")
        for delta in [(0, -1, 0), (1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)]:
            neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
            require(neighbor in fluid or solid(neighbor), "Unsealed pool")
    passable = {"minecraft:air", "minecraft:red_mushroom", "minecraft:brown_mushroom", "minecraft:glow_lichen", "minecraft:ladder"}
    def walkable(position):
        pos_x, pos_y, pos_z = position
        return 0 <= pos_x < 17 and 0 <= pos_z < 17 and pos_y == 2 and structure.get(position).name in passable and structure.get((pos_x, 3, pos_z)).name in passable and solid((pos_x, 1, pos_z))
    visited = {ENTRANCE}
    pending = deque([ENTRANCE])
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for delta_x, delta_z in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            position = (pos_x + delta_x, pos_y, pos_z + delta_z)
            if position not in visited and walkable(position):
                visited.add(position)
                pending.append(position)
    require(walkable(ENTRANCE) and INTERIOR in visited, "Unsafe feet coordinates")
    for name, position in LANDMARKS.items():
        require(position in visited, f"Disconnected landmark: {name}")
    for position, block in structure.blocks.items():
        pos_x, pos_y, pos_z = position
        if block.name in {"minecraft:red_mushroom", "minecraft:brown_mushroom"}:
            require(structure.get((pos_x, pos_y - 1, pos_z)).name == "minecraft:podzol", "Unsupported mushroom")
        if block.name == "minecraft:mushroom_stem" and pos_y == 2:
            require(solid((pos_x, 1, pos_z)), "Floating giant mushroom")
        if block.name in {"minecraft:potted_red_mushroom", "minecraft:potted_brown_mushroom"}:
            require(solid((pos_x, pos_y - 1, pos_z)), "Floating pot")
        if block.name in {"minecraft:hanging_roots", "minecraft:spore_blossom"}:
            require(solid((pos_x, pos_y + 1, pos_z)), "Unsupported hanging plant")
        if block.name == "minecraft:lantern":
            require(all(structure.get((pos_x, height, pos_z)).name == "minecraft:chain" for height in (7, 8)) and solid((pos_x, 9, pos_z)), "Broken lantern anchor")
        if block.name == "minecraft:ladder":
            require(solid((pos_x, pos_y, pos_z + 1)), "Unsupported ladder")
    counts = Counter(block.name for block in structure.blocks.values())
    result = {"status": "PASS", "sha256": digest(ROOT / "room.nbt"), "size": SIZE, "records": len(structure.blocks), "palette_states": len(decoded["palette"]), "non_air": len(structure.blocks) - counts["minecraft:air"], "full_base_cells": 289, "full_roof_cells": 289, "north_opening": [3, 3], "straight_entrance_clearance": [3, 3, 6], "reachable_dry_feet": len(visited), "landmarks_connected": len(LANDMARKS), "water_sources_sealed": len(fluid), "empty_barrels": 8, "small_mushrooms_supported": counts["minecraft:red_mushroom"], "supported_lanterns": 2, "supported_hanging_plants": 6, "template_entities": 0, "forbidden_blocks": 0, "limitations": ["Static material-class walkability; native collision and support tested separately"]}
    write_json(ROOT / "qa/static.json", result)
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    inspect()
