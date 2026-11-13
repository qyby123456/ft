import collections
import dataclasses
import gzip
import hashlib
import io
import json
import struct
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
PACK = ROOT / "out/datapack"
REFERENCE = Path("D:/ft工程文件/image2.5/tomb-archive-block-structure-20261101/out")
NAMESPACE = "fttombarchive"
SIZE = (31, 16, 39)
BLOCKS = {}


class Byte(int):
    pass


@dataclasses.dataclass
class Block:
    name: str
    properties: dict
    data: dict | None = None

    def state(self):
        result = {"Name": "minecraft:" + self.name}
        if self.properties:
            result["Properties"] = self.properties
        return result


def put(block_x, block_y, block_z, name, data=None, **properties):
    position = (block_x, block_y, block_z)
    assert all(0 <= position[axis] < SIZE[axis] for axis in range(3)), position
    BLOCKS[position] = Block(name, {key: str(value).lower() for key, value in properties.items()}, data)


def cuboid(minimum, maximum, name, **properties):
    for block_x in range(minimum[0], maximum[0] + 1):
        for block_y in range(minimum[1], maximum[1] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                put(block_x, block_y, block_z, name, **properties)


def clear(minimum, maximum):
    for position in list(BLOCKS):
        if all(minimum[axis] <= position[axis] <= maximum[axis] for axis in range(3)):
            del BLOCKS[position]


def noise(block_x, block_y, block_z):
    return abs(block_x * 73856093 ^ block_y * 19349663 ^ block_z * 83492791) % 100


def stone(block_x, block_y, block_z):
    value = noise(block_x, block_y, block_z)
    return "cracked_deepslate_bricks" if value < 17 else "cobbled_deepslate" if value < 23 else "deepslate_bricks"


def stair(block_x, block_y, block_z, facing, half="bottom"):
    put(block_x, block_y, block_z, "deepslate_brick_stairs", facing=facing, half=half, shape="straight", waterlogged=False)


def slab(block_x, block_y, block_z, material="deepslate_tile", kind="top"):
    put(block_x, block_y, block_z, material + "_slab", type=kind, waterlogged=False)


def candle(block_x, block_y, block_z, count=2, lit=True):
    put(block_x, block_y, block_z, "candle", candles=count, lit=lit, waterlogged=False)


def skull(block_x, block_y, block_z, rotation=0):
    put(block_x, block_y, block_z, "skeleton_skull", rotation=rotation)


def main_roof(block_x, block_z):
    return 11 + min(block_x, 30 - block_x, block_z - 8, 30 - block_z) // 3


def vault_roof(block_x, block_z):
    return 11 + min(block_x - 7, 23 - block_x, (block_z - 30) * 2, (38 - block_z) * 2) // 3


def hanging_lamp(block_x, block_z, lamp_y, ceiling_y):
    put(block_x, lamp_y, block_z, "soul_lantern", hanging=True, waterlogged=False)
    for block_y in range(lamp_y + 1, ceiling_y):
        put(block_x, block_y, block_z, "chain", axis="y", waterlogged=False)


def book(title, pages):
    return {"id": "minecraft:written_book", "Count": Byte(1), "tag": {"title": title, "author": "墓狱书记官", "pages": [json.dumps({"text": page}, ensure_ascii=False) for page in pages]}}


def lectern(block_x, block_y, block_z, title, pages, facing="north"):
    put(block_x, block_y, block_z, "lectern", data={"id": "minecraft:lectern", "Book": book(title, pages), "Page": 0}, facing=facing, has_book=True, powered=False)


def barrel(block_x, block_y, block_z, title):
    put(block_x, block_y, block_z, "barrel", data={"id": "minecraft:barrel", "CustomName": json.dumps({"text": title}, ensure_ascii=False), "Items": []}, facing="up", open=False)


def foundations():
    footprints = [((0, 0, 8), (30, 2, 30)), ((10, 0, 3), (20, 2, 9)), ((7, 0, 30), (23, 2, 38))]
    for minimum, maximum in footprints:
        cuboid(minimum, maximum, "cobbled_deepslate")
        for block_x in range(minimum[0], maximum[0] + 1):
            for block_z in range(minimum[2], maximum[2] + 1):
                edge = block_x in (minimum[0], maximum[0]) or block_z in (minimum[2], maximum[2])
                name = "deepslate_tiles" if edge else stone(block_x, 2, block_z)
                if 13 <= block_x <= 17:
                    name = "polished_deepslate" if block_x in (13, 17) else "deepslate_tiles"
                elif block_x in (5, 10, 20, 25) and 10 <= block_z <= 28:
                    name = "chiseled_deepslate" if block_z % 6 == 4 else "polished_deepslate"
                elif noise(block_x, 2, block_z) == 99:
                    name = "mossy_stone_bricks"
                put(block_x, 2, block_z, name)
    for block_z in range(3):
        for block_x in range(12, 19):
            if block_z:
                cuboid((block_x, 0, block_z), (block_x, block_z - 1, block_z), "deepslate_bricks")
            stair(block_x, block_z, block_z, "south")


def shell():
    for block_x in range(31):
        for block_z in range(8, 31):
            if block_x <= 1 or block_x >= 29 or block_z <= 9 or block_z >= 29:
                for block_y in range(3, 11):
                    put(block_x, block_y, block_z, stone(block_x, block_y, block_z))
            roof_y = main_roof(block_x, block_z)
            cuboid((block_x, roof_y - 1, block_z), (block_x, roof_y, block_z), "deepslate_tiles")
    for block_x in range(7, 24):
        for block_z in range(30, 39):
            if block_x <= 8 or block_x >= 22 or block_z >= 37:
                for block_y in range(3, 11):
                    put(block_x, block_y, block_z, stone(block_x, block_y, block_z))
            roof_y = vault_roof(block_x, block_z)
            cuboid((block_x, roof_y - 1, block_z), (block_x, roof_y, block_z), "deepslate_tiles")
    for block_x in range(10, 21):
        for block_z in range(3, 9):
            if block_x in (10, 20) or block_z == 3:
                for block_y in range(3, 8):
                    put(block_x, block_y, block_z, stone(block_x, block_y, block_z))
            roof_y = 8 + min(block_x - 10, 20 - block_x) // 2
            cuboid((block_x, roof_y - 1, block_z), (block_x, roof_y, block_z), "deepslate_tiles")
    for block_z in (3, 8, 9):
        clear((12, 3, block_z), (18, 6, block_z))
        clear((13, 7, block_z), (17, 7, block_z))
        stair(12, 6, block_z, "east", "top")
        stair(18, 6, block_z, "west", "top")
    clear((12, 3, 29), (18, 6, 30))
    clear((13, 7, 29), (17, 7, 30))
    for block_z in (29, 30):
        cuboid((12, 3, block_z), (12, 6, block_z), "iron_bars")
        cuboid((18, 3, block_z), (18, 6, block_z), "iron_bars")
        cuboid((12, 7, block_z), (18, 7, block_z), "iron_bars")
    cuboid((12, 3, 30), (18, 7, 30), "iron_bars")
    for block_y, half in ((3, "lower"), (4, "upper")):
        put(15, block_y, 30, "iron_door", facing="south", half=half, hinge="left", open=False, powered=False)
    for block_z in (29, 31):
        put(15, 3, block_z, "stone_pressure_plate", powered=False)
    for block_z in (10, 16, 22, 28):
        for block_x in (2, 28):
            cuboid((block_x, 3, block_z), (block_x, 9, block_z), "deepslate_bricks")
            for block_y in (3, 7, 9):
                put(block_x, block_y, block_z, "chiseled_deepslate")
            stair(block_x + (1 if block_x == 2 else -1), 9, block_z, "west" if block_x == 2 else "east", "top")
    for block_x in (11, 19):
        cuboid((block_x, 3, 3), (block_x, 7, 3), "chiseled_deepslate")
        cuboid((block_x, 3, 29), (block_x, 8, 30), "chiseled_deepslate")
    for block_x in (5, 25):
        put(block_x, 8, 9, "chiseled_deepslate")
        put(block_x, 8, 29, "chiseled_deepslate")


def shelves():
    for block_x in (3, 27):
        for start_z, end_z in ((12, 18), (23, 27)):
            for block_z in range(start_z, end_z + 1):
                for block_y in range(3, 7):
                    put(block_x, block_y, block_z, "bookshelf")
                slab(block_x, 7, block_z, "dark_oak", "bottom")
            for block_z in (start_z - 1, end_z + 1):
                cuboid((block_x, 3, block_z), (block_x, 7, block_z), "dark_oak_log", axis="y")
        facing = "east" if block_x == 3 else "west"
        slots = (0, 2, 4)
        properties = {f"slot_{slot}_occupied": slot in slots for slot in range(6)}
        items = [{"Slot": Byte(slot), "id": "minecraft:book", "Count": Byte(1)} for slot in slots]
        put(block_x, 3, 17, "chiseled_bookshelf", data={"id": "minecraft:chiseled_bookshelf", "Items": items, "last_interacted_slot": -1}, facing=facing, **properties)
    for block_x in range(10, 21):
        for block_y in range(3, 7):
            if block_x in (10, 20):
                put(block_x, block_y, 36, "dark_oak_log", axis="y")
            else:
                put(block_x, block_y, 36, "bookshelf")
        slab(block_x, 7, 36, "dark_oak", "bottom")


def niches():
    for side_x, skull_rotation in ((1, 4), (29, 12)):
        for block_z in (20,):
            clear((side_x, 4, block_z - 1), (side_x, 6, block_z + 1))
            cuboid((side_x, 3, block_z - 1), (side_x, 3, block_z + 1), "chiseled_deepslate")
            skull(side_x, 4, block_z, skull_rotation)
            candle(side_x, 4, block_z - 1, 2)
            slab(side_x, 6, block_z - 1, "deepslate_brick")
            slab(side_x, 6, block_z + 1, "deepslate_brick")
    for block_x in (5, 25):
        clear((block_x - 1, 4, 9), (block_x + 1, 6, 9))
        cuboid((block_x - 1, 3, 9), (block_x + 1, 3, 9), "chiseled_deepslate")
        skull(block_x, 4, 9)
        candle(block_x - 1, 4, 9)
    for block_x in (5, 8):
        cuboid((block_x, 3, 23), (block_x + 1, 3, 26), "deepslate_bricks")
        for offset_x in range(2):
            for block_z in range(23, 27):
                slab(block_x + offset_x, 4, block_z, "deepslate_brick", "bottom")
        put(block_x, 4, 25, "bone_block", axis="z")
        skull(block_x, 5, 25)
        put(block_x + 1, 4, 23, "chiseled_deepslate")
        candle(block_x + 1, 5, 23, 3)
    barrel(6, 3, 27, "封存骨匣 · 空")


def reading_table():
    for block_x in range(11, 20):
        for block_z in range(18, 21):
            slab(block_x, 4, block_z)
    for block_x in (11, 19):
        for block_z in (18, 20):
            put(block_x, 3, block_z, "chiseled_deepslate")
    lectern(14, 5, 19, "墓狱档案 · 值守名录", ["第七档案库\n\n石壁记录亡者，铁栏看守名字。\n\n后门的密库从不入阳光。", "借阅守则\n\n不得移走骨匣。\n不得熄灭青灯。\n归还的书，须翻到最后一页。"])
    lectern(16, 5, 19, "缺失的第十三卷", ["封存记录\n\n第十三卷已移交后部密库。\n\n转交人与收卷人，姓名皆被涂去。"])
    candle(12, 5, 19, 3)
    skull(18, 5, 19, 15)
    for block_x in (13, 17):
        for block_z in (16, 22):
            slab(block_x, 3, block_z, "deepslate_brick", "bottom")
    for block_x, block_z in ((6, 15), (24, 15)):
        put(block_x, 3, block_z, "dark_oak_planks")
        lectern(block_x, 4, block_z, "旧案索引", ["甲类：借阅及看守记录\n乙类：囚名与墓号\n\n检索到第十三卷时，索引与卷号之间留下了一道刀痕。"], "east" if block_x < 15 else "west")
    for block_x, block_z in ((7, 12), (23, 12), (9, 17), (21, 19), (10, 27)):
        put(block_x, 3, block_z, "brown_carpet")
    for block_x, block_z in ((12, 12), (19, 15), (8, 21), (20, 25), (17, 32)):
        put(block_x, 3, block_z, "birch_pressure_plate", powered=False)
    barrel(7, 3, 18, "待归档旧卷 · 空")
    barrel(23, 3, 18, "旧狱物证 · 空")


def prison_cell():
    for block_x in range(22, 28):
        cuboid((block_x, 3, 23), (block_x, 6, 23), "iron_bars")
        put(block_x, 7, 23, "deepslate_bricks")
    cuboid((22, 3, 24), (22, 6, 28), "iron_bars")
    for block_x in (23, 25):
        cuboid((block_x, 3, 23), (block_x, 6, 23), "deepslate_bricks")
    for block_y, half in ((3, "lower"), (4, "upper")):
        put(24, block_y, 23, "iron_door", facing="south", half=half, hinge="left", open=False, powered=False)
    for block_z in (22, 24):
        put(24, 3, block_z, "stone_pressure_plate", powered=False)
    put(24, 3, 27, "dark_oak_planks")
    lectern(24, 4, 27, "囚徒抄本", ["抄录到第九百页时，我已不记得自己的名字。\n\n看守每日从门缝递来一支新烛，却从不问我，昨日抄下了谁。"])
    barrel(26, 3, 27, "封存抄本 · 空")


def vault():
    put(15, 3, 34, "chiseled_deepslate")
    lectern(15, 4, 34, "第十三卷 · 封名册", ["墓狱档案 · 第十三卷\n\n此处封存的不是尸骸，而是死者仍未被遗忘的姓名。", "最后一页\n\n名单到这里戛然而止。\n\n纸张的另一面，仍能看见尚未干透的墨迹。"])
    for block_x in (13, 17):
        put(block_x, 3, 34, "chiseled_deepslate")
        for block_y in range(4, vault_roof(block_x, 34) - 1):
            put(block_x, block_y, 34, "chain", axis="y", waterlogged=False)
        put(block_x, vault_roof(block_x, 34) - 1, 34, "chiseled_deepslate")
    for block_x in (10, 20):
        put(block_x, 3, 32, "chiseled_deepslate")
        candle(block_x, 4, 32, 3)
    barrel(10, 3, 35, "密库预留 · 空")
    barrel(20, 3, 35, "密库预留 · 空")
    hanging_lamp(15, 34, 8, vault_roof(15, 34) - 1)


def atmosphere():
    for block_x, block_z in ((6, 12), (24, 12), (6, 21), (24, 21), (10, 27), (20, 27)):
        hanging_lamp(block_x, block_z, 7, main_roof(block_x, block_z) - 1)
    for block_x in (11, 19):
        put(block_x, 3, 5, "chiseled_deepslate")
        put(block_x, 4, 5, "soul_lantern", hanging=False, waterlogged=False)
    for block_x, block_y, block_z in ((4, 8, 12), (26, 8, 18), (4, 8, 27), (26, 8, 27), (9, 8, 35), (21, 8, 35)):
        put(block_x, block_y, block_z, "cobweb")
    for block_x in (9, 21):
        put(block_x, 7, 28, "black_wall_banner", data={"id": "minecraft:banner", "Patterns": [{"Pattern": "bs", "Color": 8}, {"Pattern": "bts", "Color": 7}]}, facing="north")
        put(block_x, 7, 29, "chiseled_deepslate")


def solid_top(block):
    if block is None:
        return False
    if block.name.endswith("_slab"):
        return block.properties["type"] in ("top", "double")
    if block.name.endswith("_stairs"):
        return block.properties["half"] == "top"
    return block.name not in {"candle", "skeleton_skull", "cobweb", "soul_lantern", "chain", "iron_bars", "iron_door", "stone_pressure_plate", "birch_pressure_plate", "brown_carpet", "black_wall_banner", "lectern"}


def connections():
    for (block_x, block_y, block_z), block in BLOCKS.items():
        if block.name == "iron_bars":
            for direction, delta_x, delta_z in (("north", 0, -1), ("south", 0, 1), ("east", 1, 0), ("west", -1, 0)):
                neighbor = BLOCKS.get((block_x + delta_x, block_y, block_z + delta_z))
                connected = neighbor and (neighbor.name == "iron_bars" or solid_top(neighbor))
                block.properties[direction] = str(bool(connected)).lower()
            block.properties["waterlogged"] = "false"


def encode_string(value):
    encoded = value.encode("utf-8")
    return struct.pack(">H", len(encoded)) + encoded


def nbt_payload(value):
    if isinstance(value, Byte):
        return 1, struct.pack(">b", value)
    if isinstance(value, dict):
        payload = bytearray()
        for key, child in value.items():
            kind, encoded = nbt_payload(child)
            payload.extend(bytes([kind]) + encode_string(key) + encoded)
        return 10, bytes(payload) + b"\x00"
    if isinstance(value, list):
        entries = [nbt_payload(child) for child in value]
        kind = entries[0][0] if entries else 0
        assert all(entry[0] == kind for entry in entries)
        return 9, bytes([kind]) + struct.pack(">i", len(entries)) + b"".join(entry[1] for entry in entries)
    if isinstance(value, int):
        return 3, struct.pack(">i", value)
    if isinstance(value, str):
        return 8, encode_string(value)
    raise TypeError(type(value))


def read_string(stream):
    return stream.read(struct.unpack(">H", stream.read(2))[0]).decode("utf-8")


def read_nbt(stream, kind):
    if kind == 1:
        return Byte(struct.unpack(">b", stream.read(1))[0])
    if kind == 3:
        return struct.unpack(">i", stream.read(4))[0]
    if kind == 8:
        return read_string(stream)
    if kind == 9:
        element = stream.read(1)[0]
        return [read_nbt(stream, element) for _ in range(struct.unpack(">i", stream.read(4))[0])]
    if kind == 10:
        result = {}
        while True:
            element = stream.read(1)[0]
            if element == 0:
                return result
            name = read_string(stream)
            result[name] = read_nbt(stream, element)
    raise ValueError(kind)


def load_structure(path):
    stream = io.BytesIO(gzip.decompress(path.read_bytes()))
    assert stream.read(1) == b"\x0a"
    read_string(stream)
    return read_nbt(stream, 10)


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_structure():
    palette, indexes, entries = [], {}, []
    for position, block in sorted(BLOCKS.items()):
        state = block.state()
        key = json.dumps(state, sort_keys=True)
        if key not in indexes:
            indexes[key] = len(palette)
            palette.append(state)
        entry = {"pos": list(position), "state": indexes[key]}
        if block.data is not None:
            entry["nbt"] = block.data
        entries.append(entry)
    template = {"DataVersion": 3465, "size": list(SIZE), "palette": palette, "blocks": entries, "entities": []}
    structure = PACK / f"data/{NAMESPACE}/structures/archive.nbt"
    structure.parent.mkdir(parents=True, exist_ok=True)
    structure.write_bytes(gzip.compress(b"\x0a\x00\x00" + nbt_payload(template)[1], mtime=0))
    assert load_structure(structure) == template
    (ROOT / "out/FT_Tomb_Archive.nbt").write_bytes(structure.read_bytes())
    return template, structure


def function(name, lines):
    path = PACK / f"data/{NAMESPACE}/functions/{name}.mcfunction"
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text("\n".join(lines) + "\n", encoding="utf-8")


def tell(text, color="aqua"):
    return "tellraw @s " + json.dumps({"text": text, "color": color}, ensure_ascii=False)


def build_functions():
    write_json(PACK / "pack.mcmeta", {"pack": {"pack_format": 15, "description": "墓狱档案地牢 · 31×39×16 · 原版建筑 / Minecraft 1.20.1"}})
    write_json(PACK / "data/minecraft/tags/functions/load.json", {"values": [f"{NAMESPACE}:load"]})
    write_json(PACK / f"data/{NAMESPACE}/tags/blocks/air.json", {"replace": False, "values": ["minecraft:air", "minecraft:cave_air", "minecraft:void_air"]})
    function("load", ["scoreboard objectives add ftta.build dummy"])
    function("help", [tell("墓狱档案：站在空域西北角最低基础高度，+X 留31格、+Z 留39格、向上留16格。入口向北（-Z）。"), tell("执行 /function fttombarchive:build；地面比执行高度高3格。仅建筑，不自动生成、刷怪或发奖励。", "gold"), tell("先在副本/测试世界放置。占地内必须全为空气且相关区块已加载，不会自动挖空已有建筑。", "gray")])
    function("build", ["execute if entity @s[type=minecraft:player] align xyz run function fttombarchive:internal/build"])
    lines = ["scoreboard players set #ok ftta.build 1", "scoreboard players set #placed ftta.build 0", "tag @s add ftta.builder"]
    for block_x in (0, 16, 30):
        for block_z in (0, 16, 32, 38):
            lines.append(f"execute unless loaded ~{block_x} ~0 ~{block_z} run scoreboard players set #ok ftta.build 0")
    for coordinates in ("~0 ~0 ~0", "~30 ~15 ~38"):
        lines.extend(["scoreboard players set #bounds ftta.build 0", f"execute store success score #bounds ftta.build if block {coordinates} #fttombarchive:air", "execute if score #bounds ftta.build matches 0 run scoreboard players set #ok ftta.build 0"])
    lines.append("execute if entity @e[tag=!ftta.builder,dx=30,dy=15,dz=38] run scoreboard players set #ok ftta.build 0")
    lines.extend(["execute if score #ok ftta.build matches 1 run function fttombarchive:internal/scan", "execute if score #ok ftta.build matches 1 store success score #placed ftta.build run place template fttombarchive:archive ~ ~ ~ none none 1.0 0", "tag @s remove ftta.builder", "execute if score #placed ftta.build matches 1 run tp @s ~15.5 ~3 ~5.5 0 0", "execute if score #placed ftta.build matches 1 run " + tell("墓狱档案建造完成：入口前室 → 中央阅档厅 → 铁栅密库；右侧抄录室铁门内外均有踏板。"), "execute if score #ok ftta.build matches 0 run " + tell("拒绝建造：空域有方块/其它实体、区块未加载或超出世界高度。未放置结构。", "red"), "execute if score #ok ftta.build matches 1 if score #placed ftta.build matches 0 run " + tell("模板未成功放置，请检查服务器日志与模板资源。", "red")])
    function("internal/build", lines)
    function("internal/scan", [f"execute if score #ok ftta.build matches 1 run function fttombarchive:internal/scan_{block_y:02d}" for block_y in range(SIZE[1])])
    for block_y in range(SIZE[1]):
        lines = []
        for block_x in range(SIZE[0]):
            for block_z in range(SIZE[2]):
                coordinates = f"~{block_x} ~{block_y} ~{block_z}"
                lines.append(f"execute unless block {coordinates} #fttombarchive:air run scoreboard players set #ok ftta.build 0")
        function(f"internal/scan_{block_y:02d}", lines)


def is_walkable(position):
    block_x, block_y, block_z = position
    support = BLOCKS.get((block_x, block_y - 1, block_z))
    if not (solid_top(support) or support and support.name.endswith("_stairs")):
        return False
    allowed = {"brown_carpet", "stone_pressure_plate", "birch_pressure_plate", "iron_door"}
    for offset_y in (0, 1):
        obstruction = BLOCKS.get((block_x, block_y + offset_y, block_z))
        if obstruction and obstruction.name not in allowed:
            return False
    return True


def verify_geometry():
    failures = []
    start = (15, 1, 0)
    reachable = {start}
    pending = collections.deque([start])
    assert is_walkable(start)
    while pending:
        block_x, block_y, block_z = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, 1, -1):
                target = (block_x + delta_x, block_y + delta_y, block_z + delta_z)
                if target in reachable or not all(0 <= target[axis] < SIZE[axis] for axis in range(3)):
                    continue
                if delta_y == 1 and BLOCKS.get((block_x, block_y + 2, block_z)):
                    continue
                if delta_y == -1 and BLOCKS.get((target[0], block_y + 1, target[2])):
                    continue
                if is_walkable(target):
                    reachable.add(target)
                    pending.append(target)
    targets = {"入口前室": (15, 3, 5), "主厅入口": (15, 3, 11), "左书架": (4, 3, 15), "右书架": (26, 3, 15), "中央石桌北": (15, 3, 17), "中央石桌南": (15, 3, 21), "骨匣区": (7, 3, 27), "抄录囚室": (24, 3, 25), "后部铁栅门": (15, 3, 30), "密库封名册": (15, 3, 33), "密库左预留桶": (10, 3, 34), "密库右预留桶": (20, 3, 34)}
    for name, position in targets.items():
        if position not in reachable:
            failures.append(f"Unreachable: {name}: {position}")
    supported_count = 0
    books_count = 0
    for (block_x, block_y, block_z), block in BLOCKS.items():
        below = BLOCKS.get((block_x, block_y - 1, block_z))
        above = BLOCKS.get((block_x, block_y + 1, block_z))
        if block.name in ("candle", "skeleton_skull", "brown_carpet", "stone_pressure_plate", "birch_pressure_plate"):
            supported_count += 1
            if not solid_top(below):
                failures.append(f"Unsupported: {block.name}: {(block_x, block_y, block_z)}")
        if block.name == "soul_lantern":
            supported_count += 1
            valid = above and (above.name == "chain" or solid_top(above)) if block.properties["hanging"] == "true" else solid_top(below)
            if not valid:
                failures.append(f"Unsupported lantern: {(block_x, block_y, block_z)}")
        if block.name == "chain" and block.properties["axis"] == "y":
            if not above or above.name != "chain" and not solid_top(above):
                failures.append(f"Unanchored chain: {(block_x, block_y, block_z)}")
        if block.name == "lectern":
            books_count += 1
            if not block.data or block.data["Book"]["Count"] != 1 or not solid_top(below):
                failures.append(f"Invalid lectern: {(block_x, block_y, block_z)}")
        if block.name == "iron_door" and block.properties["half"] == "lower":
            if not above or above.name != "iron_door" or above.properties["half"] != "upper":
                failures.append("Broken iron door")
            for offset_z in (-1, 1):
                plate = BLOCKS.get((block_x, block_y, block_z + offset_z))
                if not plate or plate.name != "stone_pressure_plate":
                    failures.append("Missing two-sided door pressure plate")
    source_count = sum(block.name in {"soul_lantern", "candle"} for block in BLOCKS.values())
    result = {"status": "FAIL" if failures else "PASS", "size_xyz": list(SIZE), "block_count": len(BLOCKS), "reachable_positions": len(reachable), "two_block_clear_routes": {name: position in reachable for name, position in targets.items()}, "supported_decorations": supported_count, "written_lectern_books": books_count, "light_sources": source_count, "door_check": "Paired iron door with supported pressure plates on both sides; BFS assumes plate-operated door can open", "empty_storage": all(not block.data["Items"] for block in BLOCKS.values() if block.name == "barrel"), "entities": 0, "failures": failures}
    write_json(ROOT / "qa/geometry.json", result)
    assert not failures, failures
    return result


def verify_pack():
    errors, commands, references = [], [], []
    for path in PACK.rglob("*.json"):
        json.loads(path.read_text(encoding="utf-8"))
    json.loads((PACK / "pack.mcmeta").read_text(encoding="utf-8"))
    for path in PACK.rglob("*.mcfunction"):
        for line in path.read_text(encoding="utf-8").splitlines():
            if line:
                commands.append(line)
                if " run function " in line:
                    identifier = line.rsplit(" run function ", 1)[1]
                    references.append(identifier)
                    target = PACK / "data" / identifier.replace(":", "/functions/")
                    if not target.with_suffix(".mcfunction").is_file():
                        errors.append("Missing function: " + identifier)
    cell_count = SIZE[0] * SIZE[1] * SIZE[2]
    scans = [line for line in commands if line.startswith("execute unless block ")]
    assert len(scans) == cell_count
    assert len(commands) < 60000
    assert not (PACK / "data/minecraft/tags/functions/tick.json").exists()
    assert not any(line.startswith(("summon ", "kill ", "gamerule ", "forceload ", "fill ")) for line in commands)
    result = {"status": "FAIL" if errors else "PASS", "parsed_json": True, "function_commands": len(commands), "full_air_scan_cells": cell_count, "worst_case_chain_upper_bound": len(commands), "default_command_chain_limit": 65536, "function_references": len(references), "automatic_build": False, "tick_function": False, "worldgen": False, "errors": errors}
    write_json(ROOT / "qa/datapack.json", result)
    assert not errors, errors
    return result


def main():
    BLOCKS.clear()
    for step in (foundations, shell, shelves, niches, reading_table, prison_cell, vault, atmosphere, connections):
        step()
    geometry = verify_geometry()
    template, structure = write_structure()
    build_functions()
    checks = verify_pack()
    write_json(ROOT / "qa/blueprint.json", template)
    materials = collections.Counter(block.name for block in BLOCKS.values())
    write_json(ROOT / "out/materials.json", dict(materials.most_common()))
    archive = ROOT / "out/ft-tomb-archive-1.20.1.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as bundle:
        for path in sorted(PACK.rglob("*")):
            if path.is_file():
                info = zipfile.ZipInfo(path.relative_to(PACK).as_posix(), (2026, 11, 1, 0, 0, 0))
                info.compress_type = zipfile.ZIP_DEFLATED
                bundle.writestr(info, path.read_bytes())
    references = [{"path": str(path), "sha256": hashlib.sha256(path.read_bytes()).hexdigest()} for path in sorted(REFERENCE.glob("*.png"))]
    write_json(ROOT / "out/manifest.json", {"title": "墓狱档案地牢", "minecraft": "1.20.1", "namespace": NAMESPACE, "size_xyz": list(SIZE), "footprint_xz": [31, 39], "walk_height_from_origin": 3, "entry": [15.5, 3, 5.5], "orientation": "Entrance faces north (-Z); vault is south (+Z)", "references": references, "geometry": geometry, "datapack": checks, "palette_states": len(template["palette"]), "material_types": len(materials), "structure_sha256": hashlib.sha256(structure.read_bytes()).hexdigest(), "archive_sha256": hashlib.sha256(archive.read_bytes()).hexdigest(), "installed": False, "worlds_modified": False, "paid_image_requests": 0, "scope": "Standalone architecture and written-book narrative only; no mobs, challenge runtime, rewards or natural worldgen"})
    print(json.dumps({"archive": str(archive), "structure": str(structure), "blocks": len(BLOCKS), "palette": len(template["palette"]), "geometry": geometry["status"], "datapack": checks["status"]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
