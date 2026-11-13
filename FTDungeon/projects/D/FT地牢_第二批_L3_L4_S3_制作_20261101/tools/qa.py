from __future__ import annotations

import collections
import gzip
import hashlib
import json
import math
import zipfile
from pathlib import Path

from common import GAME, SPECS, Structure, image_metadata, plain, read_nbt
from audit_blocks import allowed_properties
from verify_flat_world import READER, as_plain


AIR_NAMES = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
FLUIDS = {"minecraft:lava", "minecraft:water"}
PASSABLE = AIR_NAMES | {"minecraft:rail", "minecraft:torch", "minecraft:soul_torch", "minecraft:wall_torch", "minecraft:soul_wall_torch"}
FORBIDDEN = {"minecraft:chest", "minecraft:trapped_chest", "minecraft:barrel", "minecraft:command_block", "minecraft:structure_block", "minecraft:jigsaw", "minecraft:barrier", "minecraft:structure_void", "minecraft:spawner", "minecraft:ender_chest"}


def support(block):
    if block.name in AIR_NAMES | FLUIDS | {"minecraft:cobweb", "minecraft:fire", "minecraft:soul_fire", "minecraft:campfire", "minecraft:soul_campfire", "minecraft:magma_block"}:
        return False
    return not any(token in block.name for token in ("_bars", "_fence", "chain", "_candle", "lantern", "_button", "rail", "torch", "brewing_stand", "amethyst_cluster", "pointed_dripstone", "skull", "flower", "potted_", "turtle_egg"))


def safe(structure, position):
    pos_x, pos_y, pos_z = position
    return 0 <= pos_x < structure.size[0] and 1 <= pos_y < structure.size[1] - 1 and 0 <= pos_z < structure.size[2] and support(structure.get((pos_x, pos_y - 1, pos_z))) and all(structure.get((pos_x, pos_y + offset, pos_z)).name in PASSABLE for offset in (0, 1))


def walking(structure, room):
    entrance = (room.center, 1, 0)
    assert safe(structure, entrance)
    visited = {entrance}
    pending = collections.deque([entrance])
    while pending:
        pos_x, pos_y, pos_z = pending.popleft()
        for shift_x, shift_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for shift_y in (0, 1, -1):
                adjacent = pos_x + shift_x, pos_y + shift_y, pos_z + shift_z
                if adjacent in visited or not safe(structure, adjacent):
                    continue
                if shift_y == 1 and structure.get((pos_x, pos_y + 2, pos_z)).name not in PASSABLE:
                    continue
                if shift_y == -1 and structure.get((adjacent[0], pos_y + 1, adjacent[2])).name not in PASSABLE:
                    continue
                visited.add(adjacent)
                pending.append(adjacent)
    required = [{"name": "安全内点", "position": list(room.spec["interior"])}, *room.required]
    for landmark in required:
        assert tuple(landmark["position"]) in visited, (room.room_id, "unreachable station", landmark)
    ground = {(pos_x, 1, pos_z) for pos_x in range(1, room.width - 1) for pos_z in range(1, room.depth - 1) if safe(structure, (pos_x, 1, pos_z))}
    fraction = len(ground & visited) / max(1, len(ground))
    assert fraction >= 0.92, (room.room_id, "isolated walkable floor", fraction, sorted(ground - visited)[:20])
    return {"passed": True, "reachable_positions": len(visited), "reachable_ground_cells": len(ground & visited), "ground_walkable_cells": len(ground), "ground_reachable_fraction": round(fraction, 4), "stations": required, "entrance_clearance": [3, 3], "cobweb_free_entry": True}, visited


def full_cube(block):
    if block.name in AIR_NAMES | FLUIDS:
        return False
    return not any(token in block.name for token in ("glass_pane", "_slab", "_stairs", "_wall", "_bars", "_fence", "chain", "_candle", "lantern", "_button", "rail", "torch", "campfire", "brewing_stand", "amethyst_cluster", "pointed_dripstone", "skull", "potted_", "turtle_egg", "trapdoor", "anvil", "cauldron", "cobweb", "lectern", "grindstone"))


def enclosure(structure, room):
    for pos_x in range(room.width):
        for pos_z in range(room.depth):
            assert full_cube(structure.get((pos_x, 0, pos_z))), (room.room_id, "incomplete floor", (pos_x, pos_z))
            assert full_cube(structure.get((pos_x, room.height - 1, pos_z))), (room.room_id, "missing roof", (pos_x, pos_z))
    wall_positions = 0
    for pos_y in range(1, room.height - 1):
        for pos_x in range(room.width):
            for pos_z in range(room.depth):
                if pos_x not in (0, room.width - 1) and pos_z not in (0, room.depth - 1):
                    continue
                opening = pos_z == 0 and room.center - 1 <= pos_x <= room.center + 1 and 1 <= pos_y <= 3
                block = structure.get((pos_x, pos_y, pos_z))
                assert (block.name in AIR_NAMES) if opening else full_cube(block), (room.room_id, "wall or opening", (pos_x, pos_y, pos_z), block)
                wall_positions += 1
    for pos_x in range(room.center - 1, room.center + 2):
        for pos_z in range(4):
            for pos_y in range(1, 4):
                assert structure.get((pos_x, pos_y, pos_z)).name in AIR_NAMES, (room.room_id, "doorway obstruction", (pos_x, pos_y, pos_z))
    for pos_x in (room.center - 2, room.center + 2):
        for pos_z in (1, 2, 3):
            assert structure.get((pos_x, 1, pos_z)).name in AIR_NAMES, (room.room_id, "protruding doorway base", (pos_x, 1, pos_z))
    return {"passed": True, "floor_columns": room.width * room.depth, "roof_columns": room.width * room.depth, "wall_positions_checked": wall_positions, "only_opening": {"side": "north", "width": 3, "height": 3}, "entry_floor_inside_template": True, "door_blocks": 0, "protruding_entry_stones": 0}


def fluids_and_fire(structure, room):
    liquid_cells = []
    fire_sources = []
    for position, block in structure.blocks.items():
        if block.name in FLUIDS:
            liquid_cells.append(position)
            pos_x, pos_y, pos_z = position
            for shift_x, shift_y, shift_z in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, -1, 0)):
                adjacent = pos_x + shift_x, pos_y + shift_y, pos_z + shift_z
                assert all(0 <= coordinate < limit for coordinate, limit in zip(adjacent, structure.size))
                neighbor = structure.get(adjacent)
                assert neighbor.name in FLUIDS or full_cube(neighbor), (room.room_id, "uncontained fluid", position, adjacent, neighbor)
        if block.name in ("minecraft:fire", "minecraft:soul_fire") or block.get("lit") == "true" and ("campfire" in block.name or "candle" in block.name):
            fire_sources.append(position)
    flammable = [(position, block.name) for position, block in structure.blocks.items() if any(token in block.name for token in ("oak_", "spruce_", "birch_", "jungle_", "acacia_", "mangrove_", "cherry_", "bookshelf", "_wool", "_carpet", "_leaves", "hay_block", "tnt"))]
    closest = None
    for source in fire_sources:
        for position, name in flammable:
            distance = max(abs(first - second) for first, second in zip(source, position))
            assert distance > 3, (room.room_id, "fire too close to flammable material", source, position, name)
            closest = min(closest, distance) if closest is not None else distance
    return {"passed": True, "free_fluid_cells": len(liquid_cells), "water_cauldron_cells": sum(block.name == "minecraft:water_cauldron" for block in structure.blocks.values()), "all_fluid_sides_and_bases_sealed": True, "lit_fire_sources": len(fire_sources), "flammable_blocks": len(flammable), "nearest_flammable_chebyshev_distance": closest, "minimum_fire_clearance_blocks": 3}


def block_entities(structure):
    counts = collections.Counter()
    decorative_items = 0
    for position, tag in structure.block_entities.items():
        entity = plain(tag)
        block = structure.get(position)
        assert "LootTable" not in entity and "Command" not in entity
        counts[entity["id"]] += 1
        if "campfire" in block.name:
            assert entity["id"] == "minecraft:campfire" and len(entity["CookingTimes"]) == len(entity["CookingTotalTimes"]) == 4
            assert entity["Items"] == []
        elif block.name == "minecraft:brewing_stand":
            assert entity["id"] == "minecraft:brewing_stand" and entity["Fuel"] == entity["BrewTime"] == 0
            assert entity["Items"] == [], (position, "room contract requires empty brewing inventory")
            slots = {item["Slot"] for item in entity["Items"]}
            for index in range(3):
                assert block.get(f"has_bottle_{index}") == ("true" if index in slots else "false")
        elif block.name == "minecraft:lectern":
            assert entity["id"] == "minecraft:lectern" and block.get("has_book") == "true"
            assert entity["Book"]["id"] == "minecraft:written_book" and entity["Book"]["Count"] == 1
            for page in entity["Book"]["tag"]["pages"]:
                json.loads(page)
            decorative_items += 1
        elif "skull" in block.name:
            assert entity["id"] == "minecraft:skull"
        elif block.name == "minecraft:blast_furnace":
            assert entity["id"] == "minecraft:blast_furnace" and entity["Items"] == [] and entity["BurnTime"] == 0
        else:
            raise AssertionError((position, block, entity["id"]))
    return {"passed": True, "types": dict(counts), "reward_containers": 0, "loot_tables": 0, "functional_decorative_items": decorative_items, "item_note": "仅保留讲台中的可读工作记录；所有酿造台及其它容器库存为空，不预装水瓶。"}


def vanilla_assets(structure):
    archive_path = GAME / "FarmingTales_ForgeⅡ v1.5.2.jar"
    blocks = set(structure.blocks.values())
    checked = 0
    nonvisual = set()
    with zipfile.ZipFile(archive_path) as archive:
        for name in {block.name for block in blocks} - AIR_NAMES:
            assert name.startswith("minecraft:") and name not in FORBIDDEN and not name.endswith(("_door", "_fence_gate", "shulker_box"))
            asset = "assets/" + name.replace(":", "/blockstates/") + ".json"
            properties = {key: {value.lower() for value in values} for key, values in allowed_properties(json.loads(archive.read(asset))).items()}
            for block in (entry for entry in blocks if entry.name == name):
                for key, value in block.properties:
                    if key in properties:
                        assert value in properties[key], (name, key, value, properties[key])
                        checked += 1
                    else:
                        hidden_values = {"waterlogged": {"true", "false"}, "powered": {"true", "false"}, "signal_fire": {"true", "false"}, "has_book": {"true", "false"}, "level": {str(index) for index in range(16)}, "rotation": {str(index) for index in range(16)}}
                        assert key in hidden_values and value in hidden_values[key], (name, "invalid non-rendered property", key, value)
                        nonvisual.add(name + "#" + key + "=" + value)
    return {"passed": True, "minecraft": "1.20.1", "block_ids": len({block.name for block in blocks}), "block_states": len(blocks), "namespaces": ["minecraft"], "render_property_checks": checked, "non_rendered_properties": sorted(nonvisual), "archive": str(archive_path), "scope_note": "方块名称与模型可见属性按本机1.20.1资源校验，隐藏属性另行按已知原版取值校验；不代替游戏注册表和实机放置。"}


def independent_nbt(path, expected):
    _, root = READER.parse_nbt(gzip.decompress(path.read_bytes()))
    data = as_plain(root)
    assert data["DataVersion"] == 3465 and data["size"] == list(expected.size) and data["entities"] == []
    assert len(data["blocks"]) == math.prod(expected.size)
    palette = data["palette"]
    seen = set()
    for entry in data["blocks"]:
        position = tuple(entry["pos"])
        assert position not in seen
        seen.add(position)
        block = expected.get(position)
        saved = palette[entry["state"]]
        assert block.name == saved["Name"] and dict(block.properties) == saved.get("Properties", {})
        if position in expected.block_entities:
            assert entry["nbt"] == plain(expected.block_entities[position])
        else:
            assert "nbt" not in entry
    return {"passed": True, "independent_parser": str(READER.__file__), "positions": len(seen), "block_entities": len(expected.block_entities), "entities": 0, "data_version": 3465, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}


def validate(room, path):
    structure = Structure.read(path)
    assert structure.size == room.spec["size"] and structure.blocks == room.structure.blocks
    assert {position: plain(entity) for position, entity in structure.block_entities.items()} == {position: plain(entity) for position, entity in room.structure.block_entities.items()}
    navigation, visited = walking(structure, room)
    source = image_metadata(room)
    reference = {key: {"path": value, "sha256": hashlib.sha256(Path(value).read_bytes()).hexdigest()} for key, value in source.items()}
    report = {"id": room.room_id, "name": room.spec["name"], "status": "PASS", "passed": True, "sha256": hashlib.sha256(path.read_bytes()).hexdigest(), "size": list(structure.size), "entrance": [room.center, 1, 0], "interior": list(room.spec["interior"]), "facing": "north", "nbt": independent_nbt(path, structure), "enclosure": enclosure(structure, room), "navigation": navigation, "hazards": fluids_and_fire(structure, room), "block_entities": block_entities(structure), "blocks": vanilla_assets(structure), "features": room.spec["features"], "landmarks": room.landmarks, "adaptations": room.spec["substitutions"], "reference": reference, "runtime_tested": False, "preview_is_cutaway_only": True, "generation_api_calls": 0, "core_modified": False, "saves_modified": False, "deployed": False}
    if room.room_id == "L4":
        assert len([name for name in room.landmarks if name.startswith("spider_leg_")]) == 8
        assert len([name for name in room.landmarks if name.startswith("hanging_cocoon_")]) == 4
        leg_segments = 0
        for name, points in room.landmarks.items():
            if not name.startswith("spider_leg_"):
                continue
            start = (9 if "_left_" in name else 11, 4, points[0][2])
            for first, second in zip([start, *points], points):
                distance = max(abs(last - initial) for initial, last in zip(first, second))
                previous = tuple(first)
                for step in range(distance + 1):
                    position = tuple(round(initial + (last - initial) * step / max(1, distance)) for initial, last in zip(first, second))
                    current = list(previous)
                    for axis in range(3):
                        while current[axis] != position[axis]:
                            current[axis] += 1 if position[axis] > current[axis] else -1
                            assert full_cube(structure.get(tuple(current))), (name, "incomplete spider leg", current)
                            leg_segments += 1
                    assert full_cube(structure.get(position)), (name, "missing leg segment", position)
                    previous = position
        for position in room.landmarks["spider_head_body_abdomen"]:
            assert full_cube(structure.get(tuple(position)))
        report["spider_anatomy"] = {"complete_legs": 8, "leg_segment_steps_checked": leg_segments, "hanging_cocoons": 4, "head_body_abdomen_present": True, "walkable_web_free_routes": True}
    return report, visited
