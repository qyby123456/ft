import collections
import gzip
import hashlib
import importlib.util
import io
import json
import struct
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out"
SIZE = [21, 12, 21]
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-room-frost-garden-atrium-20261101/out/dungeon-room-frost-garden-atrium-hero.png")
ENTRANCE = [10, 1, 0]
INTERIOR = [10, 2, 6]
CELLS = {}
FEATURES = {}
FLOWERS = {"blue_orchid", "azure_bluet", "white_tulip", "cornflower"}
FLUIDS = set()


def put(block_x, block_y, block_z, name, **properties):
    assert 0 <= block_x < SIZE[0] and 0 <= block_y < SIZE[1] and 0 <= block_z < SIZE[2], (block_x, block_y, block_z)
    state = {"Name": "minecraft:" + name}
    if properties:
        state["Properties"] = {key: str(value).lower() for key, value in properties.items()}
    CELLS[block_x, block_y, block_z] = state
    if name == "water":
        FLUIDS.add((block_x, block_y, block_z))
    else:
        FLUIDS.discard((block_x, block_y, block_z))


def volume(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def name_at(position):
    return CELLS.get(tuple(position), {"Name": "minecraft:air"})["Name"].split(":")[1]


def snow(block_x, block_y, block_z, layers=1):
    put(block_x, block_y, block_z, "snow", layers=layers)


def ice_tip(block_x, block_y, block_z):
    put(block_x, block_y, block_z, "light_blue_stained_glass_pane", north=False, south=False, east=False, west=False, waterlogged=False)


def stone(block_x, block_y, block_z):
    choice = (block_x * 37 + block_y * 17 + block_z * 29) % 19
    return "mossy_stone_bricks" if choice < 2 else "cracked_stone_bricks" if choice < 6 else "deepslate_bricks" if choice < 9 else "stone_bricks"


def ground_and_walls():
    volume((0, 0, 0), (20, 0, 20), "stone_bricks")
    volume((0, 1, 0), (20, 1, 20), "snow_block")
    for block_x in range(21):
        for block_z in range(21):
            courtyard = 0 < block_x < 20 and 0 < block_z < 20
            if courtyard and (9 <= block_x <= 11 or 10 <= block_z <= 12):
                put(block_x, 1, block_z, stone(block_x, 1, block_z))
            if block_x in (0, 20) or block_z in (0, 20):
                for block_y in range(2, 7):
                    put(block_x, block_y, block_z, stone(block_x, block_y, block_z))
                snow(block_x, 7, block_z, 2)
    for block_x in range(9, 12):
        put(block_x, 0, 0, "chiseled_stone_bricks")
        volume((block_x, 1, 0), (block_x, 3, 0), "air")
        put(block_x, 0, 1, "stone_bricks")
        put(block_x, 1, 1, "stone_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    for block_x in (8, 12):
        for block_y in range(1, 7):
            put(block_x, block_y, 0, "chiseled_stone_bricks" if block_y in (1, 4) else "stone_bricks")
    for block_x in range(9, 12):
        put(block_x, 4, 0, "chiseled_stone_bricks")
    FEATURES["north_interface"] = {"entrance": ENTRANCE, "opening_min": [9, 1, 0], "opening_max": [11, 3, 0], "internal_step": {"position_min": [9, 1, 1], "position_max": [11, 1, 1], "facing": "south"}}
    FEATURES["walls"] = {"height_above_main_floor": 5, "solid_y": [2, 6], "snow_y": 7, "roof": "open", "inside_floor_y": 1}


def fountain():
    for offset_x in range(-2, 3):
        for offset_z in range(-2, 3):
            if abs(offset_x) == 2 and abs(offset_z) == 2:
                continue
            block_x, block_z = 10 + offset_x, 10 + offset_z
            if abs(offset_x) == 2 or abs(offset_z) == 2:
                put(block_x, 2, block_z, "deepslate_bricks")
                put(block_x, 3, block_z, "stone_brick_slab", type="top", waterlogged=False)
                snow(block_x, 4, block_z, 1)
            else:
                put(block_x, 2, block_z, "water", level=0)
                put(block_x, 3, block_z, "packed_ice")
    put(10, 2, 10, "stone_bricks")
    put(10, 3, 10, "stone_bricks")
    put(10, 4, 10, "chiseled_stone_bricks")
    put(10, 5, 10, "blue_ice")
    ice_tip(10, 6, 10)
    for block_x, block_z in ((9, 9), (11, 11)):
        put(block_x, 4, block_z, "blue_ice")
        ice_tip(block_x, 5, block_z)
    FEATURES["ice_fountain"] = {"minimum": [8, 2, 8], "maximum": [12, 6, 12], "footprint": [5, 5], "enclosed_source_water": 8, "ice_cap_y": 3}


def pond():
    for block_x in range(2, 7):
        for block_z in range(2, 7):
            border = block_x in (2, 6) or block_z in (2, 6)
            if border:
                put(block_x, 1, block_z, "stone_bricks")
                put(block_x, 2, block_z, "stone_brick_slab", type="top", waterlogged=False)
                if block_z in (2, 6):
                    snow(block_x, 3, block_z, 1)
            else:
                put(block_x, 1, block_z, "water", level=0)
                put(block_x, 2, block_z, "packed_ice")
    put(2, 3, 4, "blue_ice")
    ice_tip(2, 4, 4)
    FEATURES["frozen_pond"] = {"minimum": [2, 1, 2], "maximum": [6, 4, 6], "enclosed_source_water": 9, "ice_cap_y": 2}


def flower_bed(minimum_x, maximum_x, minimum_z, maximum_z, index):
    flowers = ("blue_orchid", "azure_bluet", "white_tulip", "cornflower")
    for block_x in range(minimum_x, maximum_x + 1):
        for block_z in range(minimum_z, maximum_z + 1):
            border = block_x in (minimum_x, maximum_x) or block_z in (minimum_z, maximum_z)
            if border:
                put(block_x, 2, block_z, "stone_bricks")
                if (block_x + block_z) % 2 == 0:
                    snow(block_x, 3, block_z)
            else:
                put(block_x, 2, block_z, "grass_block", snowy=False)
                put(block_x, 3, block_z, flowers[(block_x + block_z) % 4])
                put(block_x, 4, block_z, "light", level=10, waterlogged=False)
    FEATURES["flower_bed_" + str(index)] = {"minimum": [minimum_x, 2, minimum_z], "maximum": [maximum_x, 4, maximum_z]}


def fir(block_x, block_z, tall=False):
    maximum = 10 if tall else 8
    volume((block_x, 2, block_z), (block_x, maximum - 1, block_z), "spruce_log", axis="y")
    levels = ((4, 2), (6, 2), (8, 1), (10, 0)) if tall else ((4, 2), (6, 1), (8, 0))
    for block_y, radius in levels:
        for offset_x in range(-radius, radius + 1):
            for offset_z in range(-radius, radius + 1):
                if abs(offset_x) + abs(offset_z) > radius * 1.7:
                    continue
                if offset_x == offset_z == 0 and block_y < maximum:
                    continue
                put(block_x + offset_x, block_y, block_z + offset_z, "spruce_leaves", persistent=True, distance=1, waterlogged=False)
                snow(block_x + offset_x, block_y + 1, block_z + offset_z, 2)
    FEATURES["fir_" + str(block_x)] = {"position": [block_x, 2, block_z], "top_y": maximum + 1}


def pergola_and_statue():
    for block_x in (5, 8):
        for block_z in (17, 19):
            volume((block_x, 2, block_z), (block_x, 6, block_z), "stripped_spruce_log", axis="y")
            put(block_x, 2, block_z, "stone_bricks")
    for block_z in (17, 19):
        volume((4, 7, block_z), (8, 7, block_z), "spruce_log", axis="x")
    for block_x in (4, 6, 8):
        for block_z in range(16, 20):
            put(block_x, 8, block_z, "spruce_slab", type="top", waterlogged=False)
            snow(block_x, 9, block_z, 2)
    for block_x in (6, 7):
        put(block_x, 2, 18, "spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    put(6, 6, 19, "soul_lantern", hanging=True, waterlogged=False)
    volume((14, 2, 17), (16, 2, 19), "stone_bricks")
    put(15, 3, 18, "polished_deepslate")
    volume((15, 4, 18), (15, 5, 18), "deepslate_tiles")
    put(14, 4, 18, "deepslate_brick_stairs", facing="east", half="bottom", shape="straight", waterlogged=False)
    put(16, 4, 18, "deepslate_brick_stairs", facing="west", half="bottom", shape="straight", waterlogged=False)
    put(15, 6, 18, "chiseled_deepslate")
    snow(15, 7, 18, 2)
    FEATURES["pergola"] = {"minimum": [4, 2, 16], "maximum": [8, 9, 19]}
    FEATURES["hooded_statue"] = {"minimum": [14, 2, 17], "maximum": [16, 7, 19]}


def details():
    for block_x in (8, 12):
        put(block_x, 2, 4, "stone_bricks")
        put(block_x, 3, 4, "cobblestone_wall", up=True, north="none", south="none", east="none", west="none", waterlogged=False)
        put(block_x, 4, 4, "soul_lantern", hanging=False, waterlogged=False)
    volume((18, 2, 17), (18, 6, 17), "spruce_log", axis="y")
    volume((17, 5, 17), (19, 5, 17), "spruce_log", axis="x")
    put(18, 7, 17, "spruce_log", axis="y")
    snow(18, 8, 17, 1)
    snow(17, 6, 17, 2)
    snow(19, 6, 17, 2)
    for block_z in (3, 9, 15):
        for block_x in (1, 19):
            ice_tip(block_x, 5, block_z)
    put(19, 4, 18, "cobweb")
    for block_x in (3, 7, 13, 17):
        if name_at((block_x, 4, 11)) == "air":
            put(block_x, 4, 11, "light", level=11, waterlogged=False)
    FEATURES["lanterns"] = [[8, 4, 4], [12, 4, 4]]


def encode_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def encode_nbt(value):
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, str):
        return 8, encode_string(value)
    if isinstance(value, list):
        children = [encode_nbt(child) for child in value]
        kind = children[0][0] if children else 10
        assert all(child[0] == kind for child in children)
        return 9, bytes([kind]) + struct.pack(">i", len(children)) + b"".join(child[1] for child in children)
    if isinstance(value, dict):
        result = bytearray()
        for key, child in value.items():
            kind, payload = encode_nbt(child)
            result.extend(bytes([kind]) + encode_string(key) + payload)
        return 10, bytes(result) + b"\x00"
    raise TypeError(type(value))


def write_template():
    palette, indices, blocks = [], {}, []
    for block_y in range(SIZE[1]):
        for block_z in range(SIZE[2]):
            for block_x in range(SIZE[0]):
                state = CELLS.get((block_x, block_y, block_z), {"Name": "minecraft:air"})
                key = json.dumps(state, sort_keys=True)
                if key not in indices:
                    indices[key] = len(palette)
                    palette.append(state)
                blocks.append({"pos": [block_x, block_y, block_z], "state": indices[key]})
    template = {"DataVersion": 3465, "author": "FarmingTales / L2 frost atrium north-interface adaptation", "size": SIZE, "palette": palette, "blocks": blocks, "entities": []}
    _, payload = encode_nbt(template)
    destination = OUT / "room.nbt"
    destination.write_bytes(gzip.compress(b"\x0a\x00\x00" + payload, mtime=0))
    return template, hashlib.sha256(destination.read_bytes()).hexdigest()


def solid_support(name, properties):
    if name == "snow_block" or name.endswith("_leaves"):
        return True
    if name.endswith("_slab"):
        return properties.get("type") in ("top", "double")
    return name in {"stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "deepslate_bricks", "polished_deepslate", "deepslate_tiles", "chiseled_deepslate", "spruce_log", "stripped_spruce_log", "spruce_planks", "chiseled_stone_bricks", "grass_block"}


def verify_geometry(template):
    assert len(template["blocks"]) == SIZE[0] * SIZE[1] * SIZE[2]
    assert template["entities"] == []
    for block_x in range(9, 12):
        for block_y in range(1, 4):
            assert name_at((block_x, block_y, 0)) == "air"
        assert name_at((block_x, 0, 0)) == "chiseled_stone_bricks"
        assert name_at((block_x, 1, 1)) == "stone_brick_stairs"
    for position, state in CELLS.items():
        name = state["Name"].split(":")[1]
        below = (position[0], position[1] - 1, position[2])
        if name == "snow":
            below_state = CELLS.get(below, {})
            assert solid_support(name_at(below), below_state.get("Properties", {})), (position, below_state)
        if name in FLOWERS:
            assert name_at(below) == "grass_block", position
        if name == "water":
            for offset in ((0, -1, 0), (0, 1, 0), (-1, 0, 0), (1, 0, 0), (0, 0, -1), (0, 0, 1)):
                neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
                assert name_at(neighbor) in {"water", "stone_bricks", "deepslate_bricks", "snow_block", "mossy_stone_bricks", "cracked_stone_bricks", "packed_ice"}, (position, neighbor, name_at(neighbor))
    assert len(FLUIDS) == 17
    return {"full_volume_explicit_air": True, "north_opening_clear": True, "internal_stair": True, "enclosed_water_cells": len(FLUIDS), "water_ice_caps": True, "snow_supports": True, "plant_supports": True, "entities": 0, "block_entity_nbt": 0, "rewards_or_spawners": 0}


def standable(block_x, block_z):
    if not (0 <= block_x < 21 and 0 <= block_z < 21):
        return None
    if 9 <= block_x <= 11 and block_z == 0:
        return 1
    if 9 <= block_x <= 11 and block_z == 1:
        return 2
    if name_at((block_x, 1, block_z)) not in {"snow_block", "stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "deepslate_bricks"}:
        return None
    if all(name_at((block_x, block_y, block_z)) in {"air", "light"} for block_y in (2, 3)):
        return 2
    return None


def verify_walk():
    start = tuple(ENTRANCE)
    reachable = {start}
    frontier = collections.deque([start])
    previous = {}
    while frontier:
        block_x, feet, block_z = frontier.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor_x, neighbor_z = block_x + offset_x, block_z + offset_z
            neighbor_y = standable(neighbor_x, neighbor_z)
            if neighbor_y is None:
                continue
            step = abs(neighbor_y - feet)
            if step > 0.6 and not (neighbor_x == block_x and 9 <= block_x <= 11 and {neighbor_z, block_z} == {0, 1}):
                continue
            target = (neighbor_x, neighbor_y, neighbor_z)
            if target not in reachable:
                reachable.add(target)
                previous[target] = (block_x, feet, block_z)
                frontier.append(target)
    targets = [tuple(INTERIOR), (10, 2, 7), (7, 2, 10), (13, 2, 10), (10, 2, 14), (10, 2, 18), (7, 2, 17), (14, 2, 16), (7, 2, 4)]
    assert all(target in reachable for target in targets), [target for target in targets if target not in reachable]
    route = [tuple(INTERIOR)]
    while route[-1] != start:
        route.append(previous[route[-1]])
    route.reverse()
    return {"status": "PASS", "reachable_dry_cells": len(reachable), "entrance_to_interior": route, "targets": targets, "method": "2-block clear foot/head cells; 1-block rise permitted only along internal south-facing stair; water and icy surfaces excluded"}


def render_preview(template):
    source = ROOT.parent / "tools/render_garden.py"
    specification = importlib.util.spec_from_file_location("legacy_frost_renderer", source)
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    renderer.PREVIEW = OUT
    legacy_texture_name = renderer.texture_name
    renderer.texture_name = lambda name, face: "water_still" if name == "water" else legacy_texture_name(name, face)
    stream = io.BytesIO(gzip.decompress((OUT / "room.nbt").read_bytes()))
    assert stream.read(1) == b"\x0a"
    renderer.read_string(stream)
    decoded = renderer.read_nbt(stream, 10)
    assert decoded == template
    template = decoded
    blocks = {}
    for entry in template["blocks"]:
        state = template["palette"][entry["state"]]
        name = state["Name"].split(":")[1]
        if name == "air":
            continue
        block_x, block_y, block_z = entry["pos"]
        blocks[block_x - 10, block_y + 63, block_z + 70] = (name, state.get("Properties", {}))
    cutaway = {position: state for position, state in blocks.items() if not ((position[0] == 10 or position[2] == 70) and position[1] > 65)}
    renderer.render(cutaway, (1, -1), "L2 · 21×21 · 北接口 3×3 / 前侧墙预览剖切（NBT 围墙完整）", "preview.png", (1500, 1200))
    for camera, label, filename in (((0.15, -1), "L2 正面 · 北入口与内部踏步", "front"), ((-0.15, 1), "L2 背面 · 廊架与石像", "back"), ((-1, -0.15), "L2 左侧 · 冰湖与高雪松", "left"), ((1, 0.15), "L2 右侧 · 小雪松与石像", "right")):
        renderer.render(blocks, camera, label, filename + ".png", (1200, 1000))
    from PIL import Image
    sheet = Image.new("RGB", (1440, 1200), "#101c2d")
    for index, direction in enumerate(("front", "back", "left", "right")):
        image = Image.open(OUT / (direction + ".png")).resize((720, 600), Image.Resampling.LANCZOS)
        sheet.paste(image, ((index % 2) * 720, (index // 2) * 600))
    sheet.save(OUT / "four-views.png")


def main():
    OUT.mkdir(parents=True, exist_ok=True)
    ground_and_walls()
    fountain()
    pond()
    for index, bounds in enumerate(((3, 6, 7, 9), (14, 17, 7, 9), (3, 6, 13, 15), (14, 17, 13, 15)), 1):
        flower_bed(*bounds, index)
    fir(3, 17, True)
    fir(17, 4)
    pergola_and_statue()
    details()
    template, digest = write_template()
    metadata = {"id": "L2", "name": "霜雪花园中庭", "size": SIZE, "entrance": ENTRANCE, "interior": INTERIOR, "facing": "north", "sha256": digest, "reference": str(REFERENCE)}
    (OUT / "room.json").write_text(json.dumps(metadata, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    qa = {"id": "L2", "status": "STATIC_PASS_PENDING_MINECRAFT_REGISTRY", "sha256": digest, "geometry": verify_geometry(template), "walkability": verify_walk(), "features": FEATURES, "non_air_blocks": sum(entry["Name"] != "minecraft:air" for entry in CELLS.values()), "palette_states": len(template["palette"]), "template_blocks": len(template["blocks"]), "minecraft_registry": "pending", "visual_review": "pending", "deployed": False, "core_modified": False, "player_saves_touched": False, "legacy_delivery_overwritten": False}
    (OUT / "qa-static.json").write_text(json.dumps(qa, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    render_preview(template)
    print(json.dumps({"id": "L2", "size": SIZE, "sha256": digest, "water_cells": len(FLUIDS), "reachable_dry_cells": qa["walkability"]["reachable_dry_cells"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
