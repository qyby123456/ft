import collections
import dataclasses
import gzip
import hashlib
import json
import math
import struct
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "out/datapack"
NAMESPACE = "ftfrostgarden"
DIMENSION = "ftdungeonmobs:dungeon"
OBJECTIVE = "ftfg.state"
AREA = "x=-23,y=63,z=57,dx=46,dy=25,dz=46"
MOBS = f"@e[tag=ftfg.mob,{AREA}]"
PLAYERS = f"@a[tag=ftfg.player,{AREA}]"
SCOPED_MOBS = "@e[tag=ftfg.mob,x=0,y=64,z=80,distance=..96]"
BLOCKS = {}
FEATURES = {}


@dataclasses.dataclass
class Block:
    name: str
    properties: dict
    nbt: dict | None = None

    def state(self):
        result = {"Name": "minecraft:" + self.name}
        if self.properties:
            result["Properties"] = self.properties
        return result


def put(block_x, block_y, block_z, name, nbt=None, **properties):
    BLOCKS[block_x, block_y, block_z] = Block(name, {key: str(value).lower() for key, value in properties.items()}, nbt)


def cuboid(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def noise(block_x, block_y, block_z):
    return abs(block_x * 73856093 ^ block_y * 19349663 ^ block_z * 83492791) % 100


def masonry(block_x, block_y, block_z):
    selector = noise(block_x, block_y, block_z)
    return "mossy_stone_bricks" if selector < 12 else "cracked_stone_bricks" if selector < 30 else "deepslate_bricks" if selector < 46 else "stone_bricks"


def snow(block_x, block_y, block_z, layers=1):
    put(block_x, block_y, block_z, "snow", layers=layers)


def pane(block_x, block_y, block_z):
    put(block_x, block_y, block_z, "light_blue_stained_glass_pane", north=False, south=False, east=False, west=False, waterlogged=False)


def icicle(block_x, top, block_z, length):
    for offset in range(length):
        pane(block_x, top - offset, block_z)


def lantern(block_x, block_z, tall=4):
    cuboid((block_x, 64, block_z), (block_x, 63 + tall, block_z), "spruce_fence", north=False, south=False, east=False, west=False, waterlogged=False)
    put(block_x + 1, 63 + tall, block_z, "spruce_fence", west=True, east=False, north=False, south=False, waterlogged=False)
    put(block_x + 1, 62 + tall, block_z, "soul_lantern", hanging=True, waterlogged=False)
    put(block_x, 64 + tall, block_z, "spruce_slab", type="top", waterlogged=False)
    snow(block_x, 65 + tall, block_z, 2)


def terrain():
    for block_x in range(-28, 29):
        for block_z in range(48, 113):
            if (max(abs(block_x) - 22, 0) ** 2 + max(abs(block_z - 80) - 26, 0) ** 2) > 36:
                continue
            put(block_x, 61, block_z, "deepslate_bricks")
            put(block_x, 62, block_z, "deepslate_bricks")
            interior = abs(block_x) < 24 and 56 < block_z < 104
            distance = math.hypot(block_x, block_z - 80)
            path = interior and (abs(block_x) <= 2 or abs(block_z - 80) <= 2 or 7.5 <= distance <= 10)
            entrance = abs(block_x) <= 2 and block_z <= 58
            if path or entrance:
                material = "cobblestone" if noise(block_x, 63, block_z) < 25 else masonry(block_x, 63, block_z)
                put(block_x, 63, block_z, material)
                if abs(block_x) == 2 and block_z % 9 == 1:
                    snow(block_x, 64, block_z)
            else:
                put(block_x, 63, block_z, "snow_block")
                if not interior and abs(block_x) > 3 and noise(block_x, 64, block_z) < 28:
                    snow(block_x, 64, block_z, 2 + noise(block_x, 65, block_z) % 4)
    for block_x in range(-3, 4):
        for block_z in range(16, 48):
            put(block_x, 62, block_z, "deepslate_bricks")
            put(block_x, 63, block_z, "polished_deepslate" if abs(block_x) == 3 else masonry(block_x, 63, block_z))
            if abs(block_x) == 3:
                put(block_x, 64, block_z, "stone_brick_wall", up=True, north="low", south="low", east="none", west="none", waterlogged=False)
    for block_z in (20, 28, 36, 44):
        for block_x in (-3, 3):
            put(block_x, 64, block_z, "chiseled_deepslate")
            put(block_x, 65, block_z, "soul_lantern", hanging=False, waterlogged=False)
    FEATURES["approach"] = {"from": [0, 64, 16], "to": [0, 64, 57]}


def walls():
    for block_x in range(-24, 25):
        for block_z in range(56, 105):
            if abs(block_x) != 24 and block_z not in (56, 104):
                continue
            if block_z == 56 and abs(block_x) <= 2:
                continue
            height = 4 if block_z == 56 else 5
            for block_y in range(64, 64 + height):
                put(block_x, block_y, block_z, "deepslate_bricks" if block_y == 64 else masonry(block_x, block_y, block_z))
            put(block_x, 64 + height, block_z, "stone_brick_slab", type="top", waterlogged=False)
            snow(block_x, 65 + height, block_z, 2)
    for block_x in (-24, 24):
        for block_z in range(56, 105, 8):
            cuboid((block_x - 1, 64, block_z), (block_x + 1, 69, block_z), "deepslate_bricks")
            for offset in (-1, 0, 1):
                put(block_x + offset, 70, block_z, "stone_brick_slab", type="top", waterlogged=False)
                snow(block_x + offset, 71, block_z, 3)
            if block_z not in (56, 104):
                icicle(block_x - (1 if block_x > 0 else -1), 68, block_z + 2, 2 + block_z % 3)
    for block_x in range(-20, 21, 5):
        icicle(block_x, 68, 103, 2 + abs(block_x) % 3)
    for block_x in (-4, -3, 3, 4):
        cuboid((block_x, 64, 55), (block_x, 70, 57), "stone_bricks")
        put(block_x, 71, 56, "chiseled_stone_bricks")
        snow(block_x, 72, 56, 3)
    for block_x in range(-3, 4):
        base = 70 + min(2, 3 - abs(block_x))
        cuboid((block_x, base, 56), (block_x, base + 1, 56), "stone_bricks")
        snow(block_x, base + 2, 56, 3)
        if abs(block_x) < 2:
            icicle(block_x, base - 1, 56, 2)
    for block_x in (-2, 2):
        for block_y in range(64, 68):
            put(block_x, block_y, 56, "iron_bars", north=True, south=True, east=False, west=False, waterlogged=False)
            put(block_x, block_y, 57, "iron_bars", north=True, south=True, east=False, west=False, waterlogged=False)
    for block_x in (-4, 4):
        put(block_x, 68, 54, "soul_lantern", hanging=True, waterlogged=False)
        put(block_x, 69, 54, "chain", axis="y", waterlogged=False)
        put(block_x, 70, 54, "stone_brick_slab", type="top", waterlogged=False)
    FEATURES["entrance"] = {"center": [0, 64, 56], "clear_width": 3, "minimum_headroom": 5}


def fountain():
    for offset_x in range(-6, 7):
        for offset_z in range(-6, 7):
            distance = math.hypot(offset_x, offset_z)
            if distance > 6:
                continue
            block_x, block_z = offset_x, 80 + offset_z
            put(block_x, 63, block_z, "polished_deepslate")
            if distance > 5.1:
                put(block_x, 64, block_z, "stone_brick_slab", type="bottom", waterlogged=False)
            elif distance >= 4.1:
                put(block_x, 64, block_z, masonry(block_x, 64, block_z))
                put(block_x, 65, block_z, "stone_brick_slab", type="top", waterlogged=False)
                snow(block_x, 66, block_z, 1)
            else:
                put(block_x, 64, block_z, "blue_ice" if noise(block_x, 64, block_z) < 30 else "packed_ice")
    cuboid((-1, 65, 79), (1, 67, 81), "stone_bricks")
    for block_x in range(-2, 3):
        for block_z in range(78, 83):
            if abs(block_x) + abs(block_z - 80) <= 3:
                put(block_x, 68, block_z, "packed_ice")
    cuboid((0, 69, 80), (0, 73, 80), "blue_ice")
    pane(0, 74, 80)
    for offset_x, offset_z, height in ((-2, -2, 3), (2, -1, 4), (-1, 2, 2), (2, 2, 3)):
        cuboid((offset_x, 65, 80 + offset_z), (offset_x, 64 + height, 80 + offset_z), "blue_ice")
        pane(offset_x, 65 + height, 80 + offset_z)
    for block_x, block_z in ((-2, 79), (2, 81), (-1, 82), (1, 78)):
        icicle(block_x, 67, block_z, 2)
    for block_x, block_z in ((-3, 80), (3, 80), (0, 77), (0, 83)):
        put(block_x, 63, block_z, "sea_lantern")
    FEATURES["fountain"] = {"center": [0, 64, 80], "diameter": 13, "height": 11}


def flower_bed(minimum_x, maximum_x, minimum_z, maximum_z, index):
    flowers = ("blue_orchid", "azure_bluet", "cornflower", "white_tulip")
    for block_x in range(minimum_x, maximum_x + 1):
        for block_z in range(minimum_z, maximum_z + 1):
            edge = block_x in (minimum_x, maximum_x) or block_z in (minimum_z, maximum_z)
            if edge:
                put(block_x, 64, block_z, masonry(block_x, 64, block_z))
                snow(block_x, 65, block_z, 1)
            else:
                put(block_x, 64, block_z, "grass_block", snowy=True)
                selector = noise(block_x, 65, block_z)
                if selector < 68:
                    put(block_x, 65, block_z, flowers[(block_x + block_z) % 4])
                elif selector < 85:
                    snow(block_x, 65, block_z, 2)
                else:
                    put(block_x, 64, block_z, "packed_ice")
                    put(block_x, 65, block_z, "light_blue_stained_glass")
                    pane(block_x, 66, block_z)
    FEATURES[f"flower_bed_{index}"] = {"minimum": [minimum_x, 64, minimum_z], "maximum": [maximum_x, 66, maximum_z]}


def fir(block_x, block_z, height, radius):
    cuboid((block_x, 64, block_z), (block_x, 64 + height - 2, block_z), "spruce_log", axis="y")
    for level in range(4, height, 3):
        branch_radius = max(1, round(radius * (height - level) / (height - 4)))
        for offset_x in range(-branch_radius, branch_radius + 1):
            for offset_z in range(-branch_radius, branch_radius + 1):
                if abs(offset_x) + abs(offset_z) > branch_radius * 1.65:
                    continue
                if offset_x == 0 and offset_z == 0 and level < height - 2:
                    continue
                put(block_x + offset_x, 64 + level, block_z + offset_z, "spruce_leaves", persistent=True, distance=1, waterlogged=False)
                snow(block_x + offset_x, 65 + level, block_z + offset_z, 2)
                if level > 5 and abs(offset_x) == branch_radius and offset_z == 0:
                    icicle(block_x + offset_x, 63 + level, block_z, 2)
    put(block_x, 64 + height, block_z, "spruce_leaves", persistent=True, distance=1, waterlogged=False)
    snow(block_x, 65 + height, block_z, 2)


def pergola():
    for block_x in (-14, -3):
        for block_z in (98, 102):
            cuboid((block_x, 64, block_z), (block_x, 70, block_z), "stripped_spruce_log", axis="y")
            put(block_x, 64, block_z, "stone_bricks")
    for block_z in (98, 102):
        cuboid((-15, 71, block_z), (-2, 71, block_z), "spruce_log", axis="x")
        for block_x in range(-15, -1):
            snow(block_x, 72, block_z, 2)
    for block_x in range(-15, 0, 3):
        cuboid((block_x, 72, 97), (block_x, 72, 103), "spruce_slab", type="top", waterlogged=False)
        for block_z in range(97, 104):
            snow(block_x, 73, block_z, 3)
        icicle(block_x, 70, 98, 2)
    for block_x in range(-12, -4):
        put(block_x, 64, 101, "spruce_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    for block_x in (-13, -4):
        put(block_x, 64, 101, "spruce_trapdoor", facing="north", half="bottom", open=False, powered=False, waterlogged=False)
    for block_x in (-12, -6):
        put(block_x, 70, 102, "soul_lantern", hanging=True, waterlogged=False)
    FEATURES["pergola"] = {"minimum": [-15, 64, 97], "maximum": [-2, 74, 103]}


def pond_and_statue():
    for block_x in range(16, 23):
        for block_z in range(82, 95):
            distance = ((block_x - 19) / 3.5) ** 2 + ((block_z - 88) / 6.5) ** 2
            if distance <= 1:
                put(block_x, 63, block_z, "blue_ice" if noise(block_x, 63, block_z) < 35 else "packed_ice")
                if distance > 0.7:
                    put(block_x, 63, block_z, "snow_block")
                    snow(block_x, 64, block_z, 2)
    for block_x, block_z, height in ((22, 87, 4), (21, 92, 3), (17, 94, 2)):
        cuboid((block_x, 64, block_z), (block_x, 63 + height, block_z), "blue_ice")
        pane(block_x, 64 + height, block_z)
    cuboid((18, 64, 66), (20, 64, 68), "stone_bricks")
    cuboid((18, 65, 66), (20, 65, 68), "polished_deepslate")
    cuboid((19, 66, 67), (19, 69, 67), "deepslate_tiles")
    for block_x in (18, 20):
        cuboid((block_x, 66, 67), (block_x, 68, 67), "polished_deepslate")
        put(block_x, 69, 67, "deepslate_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    put(19, 69, 66, "chiseled_deepslate")
    put(19, 70, 67, "deepslate_brick_slab", type="top", waterlogged=False)
    snow(19, 71, 67, 3)
    put(19, 68, 66, "soul_lantern", hanging=False, waterlogged=False)
    FEATURES["hooded_statue"] = {"center": [19, 64, 67], "height": 8}
    FEATURES["frozen_pond"] = {"center": [19, 63, 88], "size": [7, 13]}


def details():
    for block_x in (-6, 6):
        for block_z in (72, 88):
            for offset_x, offset_z in ((-1, 0), (1, 0), (0, -1), (0, 1)):
                put(block_x + offset_x, 63, block_z + offset_z, "chiseled_stone_bricks")
            put(block_x, 63, block_z, "sea_lantern")
            FEATURES[f"seal_{block_x}_{block_z}"] = {"position": [block_x, 64, block_z]}
    for block_x, block_z in ((-6, 61), (6, 61), (-19, 88), (16, 77)):
        lantern(block_x, block_z)
    for block_x, block_z in ((-20, 65), (20, 99)):
        put(block_x, 64, block_z, "chest", nbt={"id": "minecraft:chest", "LootTable": NAMESPACE + ":chests/cache"}, facing="north", type="single", waterlogged=False)
    put(0, 64, 101, "barrel", nbt={"id": "minecraft:barrel", "LootTable": NAMESPACE + ":chests/victory", "Lock": "霜庭封印", "CustomName": '{"text":"霜庭守望者的宝匣","color":"aqua"}'}, facing="north", open=False)
    for block_x, block_z in ((-23, 57), (23, 103), (-23, 103)):
        put(block_x, 65, block_z, "cobweb")
    for block_x, block_z in ((-21, 60), (21, 73), (-21, 94)):
        put(block_x, 64, block_z, "spruce_leaves", persistent=True, distance=1, waterlogged=False)
        snow(block_x, 65, block_z, 2)
    cuboid((20, 64, 78), (20, 72, 78), "spruce_log", axis="y")
    cuboid((18, 69, 78), (22, 69, 78), "spruce_log", axis="x")
    cuboid((20, 71, 77), (20, 71, 79), "spruce_log", axis="z")
    snow(18, 70, 78, 2)
    snow(22, 70, 78, 2)
    icicle(18, 68, 78, 3)
    FEATURES["reward_cache"] = {"position": [0, 64, 101], "unlock": "first victory only"}


def encode_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def lighting():
    for block_x in range(-21, 22, 6):
        for block_z in range(59, 102, 6):
            position = (block_x, 66, block_z)
            if position not in BLOCKS:
                put(*position, "light", level=11, waterlogged=False)
    for position, block in list(BLOCKS.items()):
        if block.name in {"blue_orchid", "azure_bluet", "cornflower", "white_tulip"}:
            above = (position[0], position[1] + 1, position[2])
            if above not in BLOCKS:
                put(*above, "light", level=10, waterlogged=False)


def nbt_payload(value):
    if isinstance(value, dict):
        encoded = bytearray()
        for key, child in value.items():
            tag_type, payload = nbt_payload(child)
            encoded.extend(bytes([tag_type]) + encode_string(key) + payload)
        return 10, bytes(encoded) + b"\x00"
    if isinstance(value, list):
        entries = [nbt_payload(child) for child in value]
        tag_type = entries[0][0] if entries else 10
        assert all(entry[0] == tag_type for entry in entries)
        return 9, bytes([tag_type]) + struct.pack(">i", len(entries)) + b"".join(entry[1] for entry in entries)
    if isinstance(value, str):
        return 8, encode_string(value)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    raise TypeError(type(value))


def structure(name, positions, origin, size):
    palette, indices, entries = [], {}, []
    for position in sorted(positions, key=lambda position: (position[1], position[2], position[0])):
        block = BLOCKS[position]
        key = (block.name, tuple(sorted(block.properties.items())))
        if key not in indices:
            indices[key] = len(palette)
            palette.append(block.state())
        entry = {"pos": [position[axis] - origin[axis] for axis in range(3)], "state": indices[key]}
        if block.nbt:
            entry["nbt"] = block.nbt
        entries.append(entry)
    value = {"DataVersion": 3465, "author": "FarmingTales · frost garden", "size": size, "palette": palette, "blocks": entries, "entities": []}
    _, payload = nbt_payload(value)
    path = PACK / f"data/{NAMESPACE}/structures/{name}.nbt"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(b"\x0a\x00\x00" + payload, mtime=0))
    return {"origin": origin, "size": size, "blocks": len(entries), "states": len(palette), "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}


def write_json(path, value):
    destination = PACK / path
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def function(name, lines):
    destination = PACK / f"data/{NAMESPACE}/functions/{name}.mcfunction"
    destination.parent.mkdir(parents=True, exist_ok=True)
    destination.write_text("\n".join(lines) + "\n", encoding="utf-8")


def score_set(holder, value):
    return f"scoreboard players set {holder} {OBJECTIVE} {value}"


def tell(target, text, color="aqua"):
    return "tellraw " + target + " " + json.dumps({"text": "[霜雪花园] " + text, "color": color}, ensure_ascii=False, separators=(",", ":"))


def chunk_functions():
    acquire, release = [], []
    for chunk_x in range(-2, 2):
        for chunk_z in range(1, 8):
            holder = f"#chunk{chunk_x}_{chunk_z}"
            position = f"{chunk_x * 16} {chunk_z * 16}"
            acquire.extend([f"execute store success score {holder} {OBJECTIVE} run forceload query {position}", f"execute if score {holder} {OBJECTIVE} matches 0 run forceload add {position}"])
            release.append(f"execute if score {holder} {OBJECTIVE} matches 0 run forceload remove {position}")
    function("internal/acquire_chunks", acquire)
    function("internal/release_chunks", release)


def build_functions():
    checked = set(BLOCKS)
    checked.update((block_x, block_y, block_z) for block_x in range(-23, 24) for block_y in range(64, 69) for block_z in range(57, 104))
    checked.update((block_x, block_y, block_z) for block_x in range(-2, 3) for block_y in (64, 65) for block_z in range(16, 56))
    lines = []
    for position in sorted(checked):
        expected = "minecraft:deepslate" if position[1] <= 62 else "minecraft:polished_blackstone_bricks" if position[1] == 63 else "minecraft:air"
        lines.append(f"execute unless block {' '.join(map(str, position))} {expected} run {score_set('#blocked', 1)}")
    function("internal/preflight", lines)
    function("install", [f"execute if score #built {OBJECTIVE} matches 1 run {tell('@s', '庭院已存在；不会覆盖已有建筑。', 'yellow')}", f"execute if score #built {OBJECTIVE} matches 0 in {DIMENSION} positioned 0 64 80 run function {NAMESPACE}:internal/build"])
    function("internal/build", [score_set("#blocked", 0), f"function {NAMESPACE}:internal/acquire_chunks", f"function {NAMESPACE}:internal/preflight", f"execute if score #blocked {OBJECTIVE} matches 0 run function {NAMESPACE}:internal/place", f"execute if score #blocked {OBJECTIVE} matches 1 run {tell('@s', '目标区域不是空旷原始平地，建造取消；没有覆盖方块。', 'red')}", f"function {NAMESPACE}:internal/release_chunks"])
    function("internal/place", [f"execute store success score #garden {OBJECTIVE} run place template {NAMESPACE}:garden -28 61 48", f"execute if score #garden {OBJECTIVE} matches 1 store success score #road {OBJECTIVE} run place template {NAMESPACE}:approach -3 62 16", f"execute if score #garden {OBJECTIVE} matches 1 if score #road {OBJECTIVE} matches 1 run function {NAMESPACE}:internal/placed", f"execute unless score #garden {OBJECTIVE} matches 1 run {tell('@s', '模板放置失败，请检查服务端日志；未标记完成。', 'red')}", f"execute if score #garden {OBJECTIVE} matches 1 unless score #road {OBJECTIVE} matches 1 run {tell('@s', '庭院已放置但连接路失败；请检查日志，不要强制清空重建。', 'red')}"])
    function("internal/placed", [score_set("#built", 1), tell("@s", "霜雪花园建造完成。由旧大厅南侧沿蓝灯路进入，院内 /trigger ftfg.start 开始挑战。")])
    return len(checked)


def spawn_mob(entity, position, health, boss=False):
    tags = '["ftfg.mob","ftfg.boss"]' if boss else '["ftfg.mob"]'
    name = ',CustomName:\'{"text":"霜庭守望者","color":"aqua","bold":true}\',CustomNameVisible:1b' if boss else ""
    armor = '{id:"minecraft:leather_helmet",Count:1b,tag:{display:{color:10473453}}}'
    weapon = '{id:"minecraft:bow",Count:1b,tag:{Enchantments:[{id:"minecraft:power",lvl:2s}]}}' if boss else '{id:"minecraft:bow",Count:1b}'
    attributes = '[{Name:"minecraft:generic.max_health",Base:' + str(float(health)) + 'd},{Name:"minecraft:generic.follow_range",Base:48.0d}' + (',{Name:"minecraft:generic.knockback_resistance",Base:0.8d}' if boss else "") + ']'
    return f'summon minecraft:{entity} {position[0] + 0.5} {position[1]} {position[2] + 0.5} {{Tags:{tags},PersistenceRequired:1b,CanPickUpLoot:0b,DeathLootTable:"minecraft:empty",Health:{float(health)}f,Attributes:{attributes},ArmorItems:[{{}},{{}},{{}},{armor}],ArmorDropChances:[0.0f,0.0f,0.0f,0.0f],HandItems:[{weapon},{{}}],HandDropChances:[0.0f,0.0f]{name}}}'


def challenge_functions():
    load = [f"scoreboard objectives add {OBJECTIVE} dummy", "scoreboard objectives add ftfg.start trigger", "scoreboard objectives add ftfg.round dummy", "scoreboard objectives add ftfg.deaths deathCount", "scoreboard objectives add ftfg.life dummy"]
    load.extend(f"scoreboard players add {holder} {OBJECTIVE} 0" for holder in ("#built", "#phase", "#cleared", "#round", "#ticker"))
    load.extend([f"execute unless score #initialized {OBJECTIVE} matches 1 run function {NAMESPACE}:internal/initialize", tell("@a[tag=ftfg.admin]", "数据包已载入，未自动改建世界。")])
    function("load", load)
    function("internal/initialize", ['bossbar add ftfrostgarden:warden {"text":"霜庭守望者","color":"aqua"}', "bossbar set ftfrostgarden:warden color blue", "bossbar set ftfrostgarden:warden max 120", "bossbar set ftfrostgarden:warden visible false", score_set("#initialized", 1)])
    function("tick", [f"execute as @a[tag=ftfg.player] unless score @s ftfg.round = #round {OBJECTIVE} run tag @s remove ftfg.player", "execute as @a[tag=ftfg.player] if score @s ftfg.deaths > @s ftfg.life run tag @s remove ftfg.player", f"execute as @a[scores={{ftfg.start=1..}}] at @s run function {NAMESPACE}:internal/request", f"execute if score #built {OBJECTIVE} matches 1 in {DIMENSION} run scoreboard players enable @a[{AREA}] ftfg.start", f"scoreboard players add #ticker {OBJECTIVE} 1", f"execute if score #ticker {OBJECTIVE} matches 20.. in {DIMENSION} positioned 0 64 80 run function {NAMESPACE}:internal/second"])
    function("internal/request", ["scoreboard players set @s ftfg.start 0", f"execute unless predicate {NAMESPACE}:in_garden run {tell('@s', '请先进入霜雪花园。', 'yellow')}", f"execute if predicate {NAMESPACE}:in_garden if score #built {OBJECTIVE} matches 1 run function {NAMESPACE}:internal/request_inside"])
    function("internal/request_inside", [f"execute if score #phase {OBJECTIVE} matches 2.. run {tell('@s', '战斗已开始，请等待下一轮。', 'yellow')}", f"execute if score #phase {OBJECTIVE} matches 0 run function {NAMESPACE}:internal/begin", f"execute if score #phase {OBJECTIVE} matches 1 unless entity @s[tag=ftfg.player] run function {NAMESPACE}:internal/join"])
    function("internal/begin", [f"scoreboard players add #round {OBJECTIVE} 1", score_set("#phase", 1), score_set("#clock", 10), score_set("#wave", 0), score_set("#elapsed", 0), score_set("#absent", 0), f"function {NAMESPACE}:internal/acquire_chunks", tell(f"@a[{AREA}]", "霜印正在苏醒：10 秒后开战。队友 /trigger ftfg.start 加入，最多四人。")])
    function("internal/join", [score_set("#members", 0), f"execute as {PLAYERS} run scoreboard players add #members {OBJECTIVE} 1", f"execute if score #members {OBJECTIVE} matches 4.. run {tell('@s', '本轮四人已满。', 'yellow')}", f"execute if score #members {OBJECTIVE} matches ..3 run function {NAMESPACE}:internal/join_player"])
    function("internal/join_player", ["tag @s add ftfg.player", f"scoreboard players operation @s ftfg.round = #round {OBJECTIVE}", "scoreboard players add @s ftfg.deaths 0", "scoreboard players operation @s ftfg.life = @s ftfg.deaths", tell("@s", "已加入。死亡、离开庭院或管理员中止都会退出本轮。")])
    function("internal/second", [score_set("#ticker", 0), f"execute as @a[tag=ftfg.player] at @s unless predicate {NAMESPACE}:in_garden run tag @s remove ftfg.player", f"execute if score #built {OBJECTIVE} matches 1 if entity @a[distance=..64] run particle minecraft:snowflake 0 73 80 21 6 21 0.005 45 normal @a[distance=..64]", f"execute if score #phase {OBJECTIVE} matches 1..3 as {SCOPED_MOBS} at @s unless predicate {NAMESPACE}:in_garden run tp @s 0.5 64 92.5", f"execute if score #phase {OBJECTIVE} matches 1..3 run function {NAMESPACE}:internal/round"])
    function("internal/round", [f"scoreboard players add #elapsed {OBJECTIVE} 1", score_set("#members", 0), f"execute as {PLAYERS} run scoreboard players add #members {OBJECTIVE} 1", f"execute if score #members {OBJECTIVE} matches 0 run scoreboard players add #absent {OBJECTIVE} 1", f"execute if score #members {OBJECTIVE} matches 1.. run {score_set('#absent', 0)}", f"execute if score #absent {OBJECTIVE} matches 15.. run function {NAMESPACE}:internal/fail", f"execute if score #elapsed {OBJECTIVE} matches 300.. if score #phase {OBJECTIVE} matches 1..3 run function {NAMESPACE}:internal/fail", f"execute if score #phase {OBJECTIVE} matches 1 run scoreboard players remove #clock {OBJECTIVE} 1", f"execute if score #phase {OBJECTIVE} matches 1 if score #clock {OBJECTIVE} matches ..0 run function {NAMESPACE}:internal/next_wave", f"execute if score #phase {OBJECTIVE} matches 3 run scoreboard players remove #clock {OBJECTIVE} 1", f"execute if score #phase {OBJECTIVE} matches 3 if score #clock {OBJECTIVE} matches ..0 run function {NAMESPACE}:internal/next_wave", f"execute if score #phase {OBJECTIVE} matches 2 run function {NAMESPACE}:internal/fight"])
    function("internal/next_wave", [f"scoreboard players add #wave {OBJECTIVE} 1", score_set("#phase", 2), *[f"execute if score #wave {OBJECTIVE} matches {wave} run function {NAMESPACE}:waves/{wave}" for wave in range(1, 5)]])
    spawn_points = [(-6, 64, 72), (6, 64, 72), (-6, 64, 88), (6, 64, 88), (-12, 64, 80), (12, 64, 80), (0, 64, 65), (0, 64, 95)]
    for wave, count in ((1, 4), (2, 6), (3, 8)):
        lines = [tell(PLAYERS, f"第 {wave}/4 波：击退 {count} 名冰霜守卫。")]
        for index, position in enumerate(spawn_points[:count]):
            lines.append(spawn_mob("skeleton" if wave == 1 and index % 2 else "stray", position, 20 + (wave - 1) * 4))
        function(f"waves/{wave}", lines)
    function("waves/4", [tell(PLAYERS, "最终波：霜庭守望者苏醒！", "light_purple"), spawn_mob("stray", (0, 64, 92), 120, boss=True), spawn_mob("stray", (-6, 64, 88), 28), spawn_mob("stray", (6, 64, 88), 28), "bossbar set ftfrostgarden:warden visible true"])
    function("internal/fight", [score_set("#alive", 0), f"execute as {MOBS} run scoreboard players add #alive {OBJECTIVE} 1", f"bossbar set ftfrostgarden:warden players {PLAYERS}", f"execute as @e[tag=ftfg.boss,{AREA},limit=1] store result bossbar ftfrostgarden:warden value run data get entity @s Health 1", f"execute if score #alive {OBJECTIVE} matches 0 if score #wave {OBJECTIVE} matches 1..3 run function {NAMESPACE}:internal/rest", f"execute if score #alive {OBJECTIVE} matches 0 if score #wave {OBJECTIVE} matches 4 run function {NAMESPACE}:internal/victory"])
    function("internal/rest", [score_set("#phase", 3), score_set("#clock", 6), tell(PLAYERS, "霜印解除，休整 6 秒。")])
    function("internal/victory", [f"execute if score #cleared {OBJECTIVE} matches 0 run data remove block 0 64 101 Lock", tell(PLAYERS, "花园恢复宁静。庭院后方宝匣已解锁；重复挑战不会刷新宝物。", "green"), score_set("#cleared", 1), f"function {NAMESPACE}:internal/cleanup"])
    function("internal/fail", [tell(f"@a[{AREA}]", "挑战中止：参与者已离场，或超过五分钟。可以重新发起。", "yellow"), f"function {NAMESPACE}:internal/cleanup"])
    function("internal/cleanup", [f"kill {MOBS}", "tag @a[tag=ftfg.player] remove ftfg.player", "bossbar set ftfrostgarden:warden visible false", "bossbar set ftfrostgarden:warden players", score_set("#phase", 0), f"scoreboard players add #round {OBJECTIVE} 1", f"function {NAMESPACE}:internal/release_chunks"])
    function("reset", [f"execute if score #phase {OBJECTIVE} matches 1..3 in {DIMENSION} positioned 0 64 80 run function {NAMESPACE}:internal/cleanup", tell("@s", "本包挑战已中止；建筑和已开宝物保持原样。", "yellow")])


def data_files():
    write_json("pack.mcmeta", {"pack": {"pack_format": 15, "description": {"text": "霜雪花园 · 参考图实建 / FT 地牢扩展 / 1.20.1", "color": "aqua"}}})
    write_json("data/minecraft/tags/functions/load.json", {"values": [NAMESPACE + ":load"]})
    write_json("data/minecraft/tags/functions/tick.json", {"values": [NAMESPACE + ":tick"]})
    write_json(f"data/{NAMESPACE}/predicates/in_garden.json", {"condition": "minecraft:location_check", "predicate": {"dimension": DIMENSION, "position": {"x": {"min": -23, "max": 24}, "y": {"min": 63, "max": 89}, "z": {"min": 57, "max": 104}}}})
    for name, items in {"cache": [("iron_ingot", 3, 6), ("amethyst_shard", 4, 8), ("emerald", 1, 3)], "victory": [("diamond", 2, 3), ("emerald", 4, 8), ("golden_apple", 1, 2)]}.items():
        pools = [{"rolls": 1, "entries": [{"type": "minecraft:item", "name": "minecraft:" + item, "functions": [{"function": "minecraft:set_count", "count": {"min": minimum, "max": maximum}}]}]} for item, minimum, maximum in items]
        write_json(f"data/{NAMESPACE}/loot_tables/chests/{name}.json", {"type": "minecraft:chest", "pools": pools})


def walkable(position):
    block_x, block_y, block_z = position
    feet = BLOCKS.get(position)
    head = BLOCKS.get((block_x, block_y + 1, block_z))
    floor = BLOCKS.get((block_x, block_y - 1, block_z))
    return floor is not None and (feet is None or feet.name == "snow") and head is None


def verify_blueprint():
    start = (0, 64, 16)
    reachable, frontier = {start}, collections.deque([start])
    while frontier:
        block_x, block_y, block_z = frontier.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            position = (block_x + offset_x, block_y, block_z + offset_z)
            if position not in reachable and walkable(position):
                reachable.add(position)
                frontier.append(position)
    targets = [(0, 64, 58), (0, 64, 99), (-19, 64, 65), (20, 64, 98), (-8, 64, 99), (15, 64, 88), (0, 64, 92), (-6, 64, 72), (6, 64, 72), (-6, 64, 88), (6, 64, 88), (-12, 64, 80), (12, 64, 80), (0, 64, 65), (0, 64, 95)]
    assert all(position in reachable for position in targets), [position for position in targets if position not in reachable]
    for block_x in range(-1, 2):
        for block_z in (55, 56, 57):
            assert all((block_x, block_y, block_z) not in BLOCKS for block_y in range(64, 69))
    assert not any(block.name in {"ice", "water", "lava", "snowy_grass_block"} for block in BLOCKS.values())
    assert min(position[2] for position in BLOCKS) == 16
    assert all(-28 <= position[0] <= 28 and 61 <= position[1] <= 84 and 16 <= position[2] <= 112 for position in BLOCKS)
    for position, block in BLOCKS.items():
        if block.name in {"blue_orchid", "azure_bluet", "cornflower", "white_tulip"}:
            below = BLOCKS.get((position[0], position[1] - 1, position[2]))
            assert below is not None and below.name == "grass_block"
    return {"walkable_cells": len(reachable), "reachable_targets": [list(position) for position in targets], "entrance_clear": True, "old_lobby_untouched": True, "flower_supports": True, "non_melting_ice": True}


def verify_pack():
    commands = []
    function_files = list(PACK.rglob("*.mcfunction"))
    for path in function_files:
        for line in path.read_text(encoding="utf-8").splitlines():
            assert line and not line.startswith("#")
            assert "kill @e" not in line or "tag=ftfg.mob" in line and AREA in line
            assert not line.startswith(("fill ", "gamerule ", "time ", "weather ", "schedule "))
            commands.append(line)
            if " run function " in line or line.startswith("function "):
                identifier = line.split("function ")[-1]
                namespace, resource = identifier.split(":")
                assert (PACK / f"data/{namespace}/functions/{resource}.mcfunction").is_file(), identifier
    assert len((PACK / f"data/{NAMESPACE}/functions/internal/preflight.mcfunction").read_text(encoding="utf-8").splitlines()) < 30000
    for path in PACK.rglob("*.json"):
        json.loads(path.read_text(encoding="utf-8"))
    load = (PACK / f"data/{NAMESPACE}/functions/load.mcfunction").read_text(encoding="utf-8")
    assert "place template" not in load and "function ftfrostgarden:install" not in load
    assert "scoreboard players set #built" not in load
    return {"functions": len(function_files), "commands": len(commands), "safe_selectors": True, "no_auto_build": True, "no_world_rule_changes": True, "resource_links": True}


def main():
    terrain()
    walls()
    fountain()
    for index, bounds in enumerate(((-16, -8, 64, 70), (8, 16, 64, 70), (-16, -8, 89, 95), (8, 15, 89, 95)), 1):
        flower_bed(*bounds, index)
    fir(-19, 78, 18, 4)
    fir(-20, 99, 13, 3)
    fir(19, 99, 15, 3)
    pergola()
    pond_and_statue()
    details()
    lighting()
    data_files()
    chunk_functions()
    checked = build_functions()
    challenge_functions()
    geometry = verify_blueprint()
    garden = [position for position in BLOCKS if position[2] >= 48]
    approach = [position for position in BLOCKS if position[2] < 48]
    structures = {"garden": structure("garden", garden, [-28, 61, 48], [57, 24, 65]), "approach": structure("approach", approach, [-3, 62, 16], [7, 4, 32])}
    validation = verify_pack()
    manifest = {"name": "霜雪花园", "minecraft": "1.20.1", "pack_format": 15, "dimension": DIMENSION, "world_center": [0, 64, 80], "structures": structures, "features": FEATURES, "block_counts": dict(collections.Counter(block.name for block in BLOCKS.values()).most_common()), "total_blocks": len(BLOCKS), "preflight_cells": checked, "build": "manual command only", "reward": "first victory unlocks existing barrel; never replenished"}
    (ROOT / "qa").mkdir(parents=True, exist_ok=True)
    (ROOT / "qa/manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (ROOT / "qa/blueprint.json").write_text(json.dumps([{"pos": list(position), "state": block.state(), **({"nbt": block.nbt} if block.nbt else {})} for position, block in sorted(BLOCKS.items())], ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    (ROOT / "qa/static-verification.json").write_text(json.dumps({"status": "PASS", "geometry": geometry, "pack": validation, "preflight_cells": checked, "scope": "Static validation only; real Minecraft results are in minecraft-format-verification.json."}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    archive_path = ROOT / "out/ft-frost-garden-1.20.1.zip"
    with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in sorted(PACK.rglob("*")):
            if path.is_file():
                entry = zipfile.ZipInfo(path.relative_to(PACK).as_posix(), (2026, 11, 1, 0, 0, 0))
                entry.compress_type = zipfile.ZIP_DEFLATED
                archive.writestr(entry, path.read_bytes())
    print(json.dumps({"status": "PASS", "blocks": len(BLOCKS), "preflight_cells": checked, "functions": validation["functions"], "archive": str(archive_path), "sha256": hashlib.sha256(archive_path.read_bytes()).hexdigest()}, ensure_ascii=False))


if __name__ == "__main__":
    main()
