from __future__ import annotations

import hashlib
import json
import math
from collections import deque
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent
WORK = ROOT / "work" / "qa"
TILE_SIZE = 32
ATLAS_SIZE = 128
BOX_LIMIT = 28
FACE_NAMES = ("north", "south", "east", "west", "up", "down")
EXPECTED_IDS = (
    "prison_cell_lantern",
    "prison_wall_lantern",
    "prison_iron_chandelier",
    "prison_candle_cluster",
    "prison_alchemical_lamp",
    "prison_wall_crystal",
    "prison_rune_beacon",
    "prison_barred_window",
)
MATERIALS = (
    ("forged_iron", "锻铁", False),
    ("riveted_iron", "铆接铁板", False),
    ("cold_stone", "冷蓝石", False),
    ("carved_socket", "凿槽石", False),
    ("old_brass", "旧黄铜", False),
    ("oxidized_copper", "氧化铜", False),
    ("dripping_wax", "融蜡立面", False),
    ("charred_wick", "炭芯", False),
    ("teal_enamel", "青釉仪器底座", False),
    ("pressure_gauge", "压力表盘", False),
    ("warden_plate", "狱吏铭牌", False),
    ("pooled_wax", "融蜡顶面", False),
    ("amber_flame", "琥珀火芯", True),
    ("frost_core", "霜色晶尖", True),
    ("rune_tablet", "原创禁文", True),
    ("cyan_crystal_vessel", "青晶与炼金液", True),
)
BUDGETS = dict(zip(EXPECTED_IDS, (19, 14, 25, 16, 17, 11, 11, 10)))


def box(name, start, end, tile, glow=False, **faces):
    result = {"name": name, "from": list(start), "to": list(end), "tile": tile}
    if faces:
        result["faces"] = faces
    if glow:
        result["glow"] = True
    return result


def definition(identifier, chinese, english, mount, light, ingredient, description, boxes):
    return {
        "id": identifier,
        "name_zh": chinese,
        "name_en": english,
        "kind": "decor",
        "mount": mount,
        "light": light,
        "ingredient": ingredient,
        "boxes": boxes,
        "description_zh": description,
    }


def make_definitions():
    return [
        definition(
            EXPECTED_IDS[0], "囚室巡夜灯", "Cell Watch Lantern", "floor", 12,
            "minecraft:iron_ingot",
            "四足落地的暖色巡夜灯；六根护栏与细火芯之间保留真实空隙。主工程提供空手切换明暗。",
            [
                box("front left foot", (3, 0, 4), (5, 1, 6), 0),
                box("front right foot", (11, 0, 4), (13, 1, 6), 0),
                box("rear left foot", (3, 0, 10), (5, 1, 12), 0),
                box("rear right foot", (11, 0, 10), (13, 1, 12), 0),
                box("raised riveted tray", (3, 1, 4), (13, 3, 12), 1, up=0),
                box("front left guard", (3, 3, 4), (4, 11, 5), 0),
                box("front right guard", (12, 3, 4), (13, 11, 5), 0),
                box("rear left guard", (3, 3, 11), (4, 11, 12), 0),
                box("rear right guard", (12, 3, 11), (13, 11, 12), 0),
                box("front offset guard", (7, 3, 4), (8, 11, 5), 0),
                box("rear offset guard", (8, 3, 11), (9, 11, 12), 0),
                box("brass burner", (6, 3, 7), (10, 4, 9), 4, up=7),
                box("narrow amber chimney", (7, 4, 7), (9, 9, 9), 12, True),
                box("offset flame tip", (7, 9, 7), (8, 10, 8), 12, True),
                box("flat cage crown", (3, 11, 4), (13, 12, 12), 1, up=0),
                box("stepped brass cap", (5, 12, 6), (11, 13, 10), 4),
                box("carry bail left", (5, 13, 7), (6, 16, 9), 0),
                box("carry bail right", (10, 13, 7), (11, 16, 9), 0),
                box("carry bail bridge", (6, 15, 7), (10, 16, 9), 0),
            ],
        ),
        definition(
            EXPECTED_IDS[1], "狱吏悬臂灯", "Warden Bracket Lantern", "wall", 12,
            "minecraft:iron_ingot",
            "铭牌背板连接折臂与短吊杆，悬挂暖光铁笼灯；不是壁龛火盆。主工程提供空手切换明暗。",
            [
                box("wall service backplate", (6, 4, 14), (10, 15, 16), 1, north=10),
                box("forward cantilever", (7, 13, 7), (9, 15, 14), 0),
                box("pendant drop link", (7, 11, 6), (9, 15, 7), 0),
                box("suspended lid", (4, 10, 3), (12, 11, 11), 1, up=4),
                box("suspended tray", (4, 2, 3), (12, 4, 11), 1, up=0),
                box("front left upright", (4, 4, 3), (5, 10, 4), 0),
                box("front right upright", (11, 4, 3), (12, 10, 4), 0),
                box("rear left upright", (4, 4, 10), (5, 10, 11), 0),
                box("rear right upright", (11, 4, 10), (12, 10, 11), 0),
                box("front guard tooth", (7, 4, 3), (8, 10, 4), 0),
                box("brass oil cup", (6, 4, 6), (10, 5, 8), 4, up=7),
                box("amber burner", (7, 5, 6), (9, 9, 8), 12, True),
                box("amber tip", (7, 9, 6), (8, 10, 7), 12, True),
                box("lower pendant finial", (7, 1, 6), (9, 2, 8), 4),
            ],
        ),
        definition(
            EXPECTED_IDS[2], "四臂锁环吊灯", "Four Arm Iron Chandelier", "ceiling", 14,
            "minecraft:chain",
            "四角蜡烛装在镂空铁环上，四根拉杆汇入十字梁、主吊杆与天花固定盘；无悬空零件。",
            [
                box("ceiling fixing plate", (5, 15, 5), (11, 16, 11), 1),
                box("central suspension stem", (7, 9, 7), (9, 15, 9), 0),
                box("east west hanger beam", (3, 8, 7), (13, 9, 9), 0),
                box("north hanger beam", (7, 8, 3), (9, 9, 7), 0),
                box("south hanger beam", (7, 8, 9), (9, 9, 13), 0),
                box("west tension link", (3, 6, 7), (4, 8, 9), 0),
                box("east tension link", (12, 6, 7), (13, 8, 9), 0),
                box("north tension link", (7, 6, 3), (9, 8, 4), 0),
                box("south tension link", (7, 6, 12), (9, 8, 13), 0),
                box("north ring rail", (2, 4, 2), (14, 6, 4), 1),
                box("south ring rail", (2, 4, 12), (14, 6, 14), 1),
                box("west ring rail", (2, 4, 4), (4, 6, 12), 1),
                box("east ring rail", (12, 4, 4), (14, 6, 12), 1),
                box("north west candle pan", (2, 6, 2), (5, 7, 5), 4),
                box("north east candle pan", (11, 6, 2), (14, 7, 5), 4),
                box("south west candle pan", (2, 6, 11), (5, 7, 14), 4),
                box("south east candle pan", (11, 6, 11), (14, 7, 14), 4),
                box("north west wax", (3, 7, 3), (4, 10, 4), 6, up=11),
                box("north east wax", (12, 7, 3), (13, 9, 4), 6, up=11),
                box("south west wax", (3, 7, 12), (4, 9, 13), 6, up=11),
                box("south east wax", (12, 7, 12), (13, 10, 13), 6, up=11),
                box("north west flame", (3, 10, 3), (4, 12, 4), 12, True),
                box("north east flame", (12, 9, 3), (13, 11, 4), 12, True),
                box("south west flame", (3, 9, 12), (4, 11, 13), 12, True),
                box("south east flame", (12, 10, 12), (13, 12, 13), 12, True),
            ],
        ),
        definition(
            EXPECTED_IDS[3], "守夜融蜡烛丛", "Vigil Candle Cluster", "floor", 11,
            "minecraft:candle",
            "有围沿的铁托盘承载高、中、短三支点燃蜡烛和一截熄灭残烛；蜡身、蜡池与融流纹理均不发光。",
            [
                box("iron drip tray", (2, 0, 3), (14, 1, 13), 1, up=0),
                box("north tray lip", (2, 1, 3), (14, 2, 4), 0),
                box("south tray lip", (2, 1, 12), (14, 2, 13), 0),
                box("west tray lip", (2, 1, 4), (3, 2, 12), 0),
                box("east tray lip", (13, 1, 4), (14, 2, 12), 0),
                box("settled wax pool", (4, 1, 5), (12, 2, 11), 11),
                box("tall wax body", (4, 2, 7), (7, 10, 10), 6, up=11),
                box("tall uneven shoulder", (4, 10, 7), (6, 11, 9), 6, up=11),
                box("tall flame", (5, 11, 8), (6, 14, 9), 12, True),
                box("medium wax body", (9, 2, 6), (12, 7, 9), 6, up=11),
                box("medium melted shoulder", (9, 7, 6), (11, 8, 8), 6, up=11),
                box("medium flame", (10, 8, 7), (11, 10, 8), 12, True),
                box("short front wax", (7, 2, 4), (9, 4, 6), 6, up=11),
                box("short flame", (7, 4, 4), (8, 6, 5), 12, True),
                box("spent rear candle", (9, 2, 10), (11, 3, 12), 6, up=11),
                box("spent charred wick", (9, 3, 10), (10, 4, 11), 7),
            ],
        ),
        definition(
            EXPECTED_IDS[4], "双胆炼金灯", "Twin Vessel Alchemical Lamp", "floor", 12,
            "minecraft:brewing_stand",
            "青釉底座、夹架、双胆容器、弯折铜管和压力表构成微型仪器轮廓；容器为不透明风格化发光体，无炼药功能。",
            [
                box("stone instrument foot", (2, 0, 3), (14, 2, 13), 2, up=8),
                box("enamel instrument rail", (3, 2, 4), (13, 3, 12), 8),
                box("large retort lower collar", (4, 3, 5), (8, 4, 10), 4),
                box("cyan retort belly", (4, 4, 5), (8, 8, 10), 15, True),
                box("cyan retort shoulder", (5, 8, 6), (7, 10, 9), 15, True),
                box("brass retort neck", (5, 10, 7), (7, 12, 9), 4),
                box("copper neck stopper", (5, 12, 7), (7, 13, 9), 5),
                box("west retort stand", (3, 3, 6), (4, 9, 9), 0),
                box("east retort stand", (8, 3, 6), (9, 9, 9), 0),
                box("west split clamp", (3, 9, 6), (5, 10, 9), 4),
                box("east split clamp", (7, 9, 6), (9, 10, 9), 4),
                box("horizontal copper transfer pipe", (7, 10, 7), (12, 11, 9), 5),
                box("brass valve block", (10, 11, 7), (12, 12, 9), 4),
                box("descending copper pipe", (11, 7, 7), (12, 10, 9), 5),
                box("amber receiver vessel", (10, 3, 6), (13, 6, 10), 12, True),
                box("receiver brass cap", (10, 6, 6), (13, 7, 10), 4),
                box("front pressure gauge", (9, 3, 3), (12, 6, 5), 4, north=9),
            ],
        ),
        definition(
            EXPECTED_IDS[5], "锁座寒晶簇", "Socketed Frost Crystal", "wall", 13,
            "minecraft:amethyst_shard",
            "凿槽石背板与承台托住三枚高低不同的寒晶，每枚有黄铜镶脚和实体晶尖；无透明贴片。",
            [
                box("carved wall backplate", (4, 2, 14), (12, 13, 16), 3, north=2),
                box("projecting stone corbel", (3, 2, 7), (13, 4, 14), 2, up=3),
                box("central brass socket", (6, 4, 8), (10, 5, 12), 4),
                box("central crystal body", (6, 5, 8), (10, 12, 12), 15, True),
                box("central crystal termination", (7, 12, 9), (9, 15, 11), 13, True),
                box("west brass socket", (3, 4, 6), (6, 5, 9), 4),
                box("west crystal body", (3, 5, 6), (6, 9, 9), 15, True),
                box("west crystal termination", (4, 9, 7), (5, 11, 8), 13, True),
                box("east brass socket", (10, 4, 10), (13, 5, 13), 4),
                box("east crystal body", (10, 5, 10), (13, 8, 13), 15, True),
                box("east crystal termination", (11, 8, 11), (12, 10, 12), 13, True),
            ],
        ),
        definition(
            EXPECTED_IDS[6], "禁文立碑灯", "Bound Rune Tablet", "floor", 10,
            "minecraft:glow_ink_sac",
            "双石柱与铜卡爪固定禁文立碑，正背均有原创发光刻文；仅为可切换装饰灯，不产生信标光束、增益或交互界面。",
            [
                box("cold stone lower plinth", (2, 0, 4), (14, 2, 12), 2, up=3),
                box("brass edged upper plinth", (4, 2, 5), (12, 3, 11), 4, up=3),
                box("west carved pier", (3, 3, 6), (5, 12, 10), 3),
                box("east carved pier", (11, 3, 6), (13, 12, 10), 3),
                box("carved lintel", (3, 12, 6), (13, 14, 10), 3, up=4),
                box("double faced luminous tablet", (5, 4, 7), (11, 12, 9), 14, True, east=15, west=15, up=15, down=15),
                box("west front retaining claw", (5, 3, 6), (6, 5, 7), 4),
                box("east front retaining claw", (10, 3, 6), (11, 5, 7), 4),
                box("west rear retaining claw", (5, 3, 9), (6, 5, 10), 4),
                box("east rear retaining claw", (10, 3, 9), (11, 5, 10), 4),
                box("brass seal crown", (6, 14, 7), (10, 15, 9), 4),
            ],
        ),
        definition(
            EXPECTED_IDS[7], "铁栅囚窗", "Iron Barred Prison Window", "wall", 0,
            "minecraft:iron_bars",
            "厚石窗框包围实际镂空的铁栅窗洞，三根立栅与前置横扣接合；不是透明玻璃、传送门或发光方块。",
            [
                box("stone sill block", (0, 0, 13), (16, 3, 16), 2, north=3),
                box("stone lintel block", (0, 13, 13), (16, 16, 16), 2, north=3),
                box("west stone jamb", (0, 3, 13), (3, 13, 16), 2, north=3),
                box("east stone jamb", (13, 3, 13), (16, 13, 16), 2, north=3),
                box("west forged window bar", (4, 3, 14), (5, 13, 16), 0),
                box("central forged window bar", (7, 3, 14), (8, 13, 16), 0),
                box("east forged window bar", (10, 3, 14), (11, 13, 16), 0),
                box("front locking cross rail", (3, 7, 13), (13, 8, 14), 1),
                box("front stone key", (7, 13, 12), (9, 16, 13), 3),
                box("projecting stone sill nose", (1, 1, 11), (15, 3, 13), 3, up=2),
            ],
        ),
    ]


def make_atlas():
    atlas = Image.new("RGBA", (ATLAS_SIZE, ATLAS_SIZE), "#141d27")
    tiles = []
    for index in range(16):
        tile = Image.new("RGBA", (32, 32), "#252b35")
        draw = ImageDraw.Draw(tile)
        if index == 0:
            draw.rectangle((0, 0, 31, 31), fill="#252b35")
            draw.rectangle((2, 0, 5, 31), fill="#48515b")
            draw.rectangle((6, 0, 8, 31), fill="#353e48")
            draw.rectangle((22, 0, 24, 31), fill="#1d2630")
            for offset_x, offset_y, length in ((11, 5, 8), (9, 18, 5), (15, 27, 9)):
                draw.rectangle((offset_x, offset_y, offset_x + length, offset_y + 1), fill="#3c4650")
            draw.rectangle((25, 23, 29, 26), fill="#784936")
            draw.rectangle((27, 21, 29, 23), fill="#59626a")
        elif index == 1:
            draw.rectangle((0, 0, 31, 31), fill="#323a44")
            draw.rectangle((1, 1, 30, 3), fill="#59626a")
            draw.rectangle((1, 4, 3, 29), fill="#48525b")
            draw.rectangle((3, 28, 30, 30), fill="#1d2630")
            draw.rectangle((28, 4, 30, 27), fill="#1d2630")
            draw.rectangle((8, 7, 24, 24), fill="#2b323c")
            for offset_x, offset_y in ((5, 5), (24, 5), (5, 24), (24, 24)):
                draw.rectangle((offset_x, offset_y, offset_x + 2, offset_y + 2), fill="#171f29")
                draw.rectangle((offset_x, offset_y, offset_x + 1, offset_y + 1), fill="#74858b")
            draw.rectangle((11, 16, 20, 17), fill="#414a54")
            draw.rectangle((17, 22, 22, 24), fill="#784936")
        elif index == 2:
            draw.rectangle((0, 0, 31, 31), fill="#354c59")
            draw.rectangle((2, 2, 29, 4), fill="#4e6570")
            draw.rectangle((2, 4, 4, 22), fill="#425a67")
            draw.rectangle((3, 27, 28, 30), fill="#253744")
            draw.rectangle((27, 7, 30, 26), fill="#2b414e")
            for cluster, color in (
                ((7, 7, 16, 10), "#3d5562"), ((15, 11, 23, 15), "#3d5562"),
                ((6, 19, 14, 23), "#2d4350"), ((18, 22, 25, 25), "#4e6570"),
                ((24, 5, 28, 8), "#74858b"), ((4, 24, 7, 27), "#253744"),
            ):
                draw.rectangle(cluster, fill=color)
            draw.line(((21, 3), (21, 7), (18, 7), (18, 11)), fill="#253744", width=2)
        elif index == 3:
            draw.rectangle((0, 0, 31, 31), fill="#354c59")
            draw.rectangle((2, 2, 29, 29), outline="#74858b", width=2)
            draw.rectangle((5, 5, 27, 27), outline="#253744", width=3)
            draw.rectangle((8, 8, 23, 23), fill="#405864")
            draw.rectangle((8, 9, 10, 20), fill="#4e6570")
            draw.rectangle((11, 21, 23, 23), fill="#2b414e")
            for offset in (11, 18):
                draw.rectangle((offset, 2, offset + 2, 5), fill="#253744")
                draw.rectangle((offset, 26, offset + 2, 29), fill="#253744")
        elif index == 4:
            draw.rectangle((0, 0, 31, 31), fill="#8d6b3c")
            draw.rectangle((2, 0, 5, 31), fill="#c3a065")
            draw.rectangle((6, 0, 8, 31), fill="#a7854e")
            draw.rectangle((26, 0, 29, 31), fill="#624c32")
            for offset_y in (6, 17, 27):
                draw.rectangle((12, offset_y, 22, offset_y + 1), fill="#aa8751")
            draw.rectangle((10, 23, 14, 26), fill="#59625a")
        elif index == 5:
            draw.rectangle((0, 0, 31, 31), fill="#784936")
            draw.rectangle((3, 0, 7, 31), fill="#b4794e")
            draw.rectangle((8, 0, 10, 31), fill="#996141")
            draw.rectangle((26, 0, 29, 31), fill="#493532")
            for cluster in ((16, 5, 23, 9), (19, 9, 26, 12), (11, 20, 18, 25), (15, 25, 21, 28)):
                draw.rectangle(cluster, fill="#486961")
            draw.rectangle((18, 6, 22, 8), fill="#638577")
        elif index == 6:
            draw.rectangle((0, 0, 31, 31), fill="#e0cfaa")
            draw.rectangle((2, 0, 5, 31), fill="#f1dfb8")
            draw.rectangle((26, 0, 31, 31), fill="#bcae8b")
            draw.polygon(((9, 0), (14, 0), (14, 11), (12, 11), (12, 20), (9, 20)), fill="#bcae8b")
            draw.rectangle((9, 0, 11, 15), fill="#f1dfb8")
            draw.polygon(((20, 0), (24, 0), (24, 7), (22, 7), (22, 13), (20, 13)), fill="#cbb78e")
            draw.rectangle((20, 0, 21, 8), fill="#f1dfb8")
            draw.rectangle((6, 27, 20, 29), fill="#d0bd95")
        elif index == 7:
            draw.rectangle((0, 0, 31, 31), fill="#141d27")
            draw.rectangle((5, 7, 10, 25), fill="#252b35")
            draw.rectangle((13, 3, 16, 28), fill="#493532")
            draw.rectangle((14, 2, 15, 8), fill="#735044")
            draw.rectangle((21, 11, 26, 22), fill="#252b35")
        elif index == 8:
            draw.rectangle((0, 0, 31, 31), fill="#253744")
            draw.rectangle((2, 2, 29, 4), fill="#4e6570")
            draw.rectangle((3, 5, 5, 27), fill="#3b6870")
            draw.rectangle((7, 7, 25, 25), fill="#2a4c58")
            draw.rectangle((10, 9, 23, 11), fill="#356270")
            draw.rectangle((20, 22, 28, 28), fill="#354c59")
            draw.rectangle((4, 26, 10, 29), fill="#784936")
            draw.rectangle((11, 26, 13, 28), fill="#b4794e")
        elif index == 9:
            draw.rectangle((0, 0, 31, 31), fill="#8d6b3c")
            draw.rectangle((1, 1, 30, 30), outline="#c3a065", width=2)
            draw.ellipse((4, 4, 27, 27), fill="#493532")
            draw.ellipse((6, 6, 25, 25), fill="#e0cfaa")
            for start, end in (((9, 10), (11, 12)), ((15, 7), (15, 10)), ((21, 9), (19, 12)), ((23, 16), (20, 16))):
                draw.line((start, end), fill="#493532", width=2)
            draw.line(((15, 19), (19, 11)), fill="#784936", width=2)
            draw.rectangle((13, 18, 17, 21), fill="#493532")
            draw.rectangle((11, 25, 21, 27), fill="#624c32")
        elif index == 10:
            draw.rectangle((0, 0, 31, 31), fill="#252b35")
            draw.rectangle((2, 2, 29, 29), outline="#59626a", width=2)
            for offset_x, offset_y in ((5, 5), (24, 5), (5, 24), (24, 24)):
                draw.rectangle((offset_x, offset_y, offset_x + 2, offset_y + 2), fill="#8d6b3c")
            draw.rectangle((9, 8, 22, 22), fill="#323d48")
            for offset in (10, 15, 20):
                draw.rectangle((offset, 10, offset + 1, 18), fill="#bcae8b")
            draw.rectangle((10, 20, 21, 21), fill="#784936")
        elif index == 11:
            draw.rectangle((0, 0, 31, 31), fill="#cbb78e")
            draw.polygon(((5, 2), (24, 2), (24, 5), (29, 5), (29, 24), (26, 24), (26, 29), (7, 29), (7, 26), (2, 26), (2, 7), (5, 7)), fill="#e0cfaa")
            draw.rectangle((8, 5, 22, 7), fill="#f1dfb8")
            draw.rectangle((5, 8, 7, 21), fill="#f1dfb8")
            draw.rectangle((10, 10, 22, 23), fill="#bcae8b")
            draw.rectangle((12, 12, 20, 21), fill="#d3bf94")
            draw.rectangle((15, 13, 17, 19), fill="#493532")
            draw.rectangle((16, 12, 18, 14), fill="#735044")
        elif index == 12:
            draw.rectangle((0, 0, 31, 31), fill="#bd682d")
            draw.polygon(((8, 0), (22, 0), (22, 6), (27, 6), (27, 24), (24, 24), (24, 31), (6, 31), (6, 23), (3, 23), (3, 9), (8, 9)), fill="#f7bf58")
            draw.polygon(((14, 3), (19, 3), (19, 12), (23, 12), (23, 24), (19, 24), (19, 29), (11, 29), (11, 21), (8, 21), (8, 13), (14, 13)), fill="#fff0b0")
            draw.rectangle((13, 17, 18, 26), fill="#fff6ce")
        elif index == 13:
            draw.rectangle((0, 0, 31, 31), fill="#b8f2de")
            draw.polygon(((0, 0), (9, 0), (19, 31), (0, 31)), fill="#7bdcd0")
            draw.polygon(((9, 0), (14, 0), (23, 31), (19, 31)), fill="#e3fff1")
            draw.polygon(((27, 0), (31, 0), (31, 31), (22, 31)), fill="#50cfca")
        elif index == 14:
            draw.rectangle((0, 0, 31, 31), fill="#183b43")
            draw.rectangle((2, 2, 29, 29), outline="#247f8b", width=2)
            draw.rectangle((5, 5, 26, 26), fill="#1c464e")
            draw.line(((15, 6), (15, 24)), fill="#b8f2de", width=3)
            draw.line(((7, 10), (10, 10), (10, 13), (15, 13), (20, 8), (24, 8)), fill="#50cfca", width=3)
            draw.line(((15, 19), (21, 19), (21, 15), (25, 15)), fill="#50cfca", width=3)
            draw.line(((8, 23), (11, 23), (11, 26), (20, 26)), fill="#b8f2de", width=2)
            draw.rectangle((7, 17, 9, 19), fill="#247f8b")
        else:
            draw.rectangle((0, 0, 31, 31), fill="#247f8b")
            draw.polygon(((1, 0), (9, 0), (19, 31), (4, 31)), fill="#50cfca")
            draw.polygon(((9, 0), (13, 0), (23, 31), (19, 31)), fill="#b8f2de")
            draw.polygon(((26, 0), (31, 0), (31, 31), (23, 31)), fill="#1e646f")
            draw.rectangle((13, 22, 18, 24), fill="#7bdcd0")
            draw.rectangle((15, 25, 20, 27), fill="#50cfca")
        tiles.append(tile)
        atlas.paste(tile, ((index % 4) * 32, (index // 4) * 32))
    return atlas, tiles


def make_tilemap():
    entries = []
    for index, (identifier, chinese, glowing) in enumerate(MATERIALS):
        pixel_x = (index % 4) * 32
        pixel_y = (index // 4) * 32
        entries.append({
            "index": index,
            "id": identifier,
            "name_zh": chinese,
            "glow": glowing,
            "pixel_bounds": [pixel_x, pixel_y, pixel_x + 32, pixel_y + 32],
            "java_uv": [pixel_x / 8, pixel_y / 8, (pixel_x + 32) / 8, (pixel_y + 32) / 8],
        })
    return {
        "atlas": "atlas.png",
        "size": [128, 128],
        "grid": [4, 4],
        "tile_size": [32, 32],
        "filter": "nearest",
        "alpha": "fully_opaque",
        "uv_policy": "entire tile per face; orientation follows face; no animation",
        "glow_tiles": [12, 13, 14, 15],
        "non_glow_tiles": list(range(12)),
        "tiles": entries,
    }


def occupancy_for(model):
    occupied = {}
    for index, part in enumerate(model["boxes"]):
        for coord_x in range(part["from"][0], part["to"][0]):
            for coord_y in range(part["from"][1], part["to"][1]):
                for coord_z in range(part["from"][2], part["to"][2]):
                    location = (coord_x, coord_y, coord_z)
                    if location in occupied:
                        raise ValueError(f'{model["id"]}: overlapping volume at {location}')
                    occupied[location] = index
    return occupied


FACE_STEPS = {
    "north": (0, 0, -1), "south": (0, 0, 1),
    "east": (1, 0, 0), "west": (-1, 0, 0),
    "up": (0, 1, 0), "down": (0, -1, 0),
}


def surface_analysis(model, occupied):
    exposed = [{face: 0 for face in FACE_NAMES} for part in model["boxes"]]
    connected = [set() for part in model["boxes"]]
    exposed_keys = set()
    for location, index in occupied.items():
        for face, step in FACE_STEPS.items():
            neighbor = tuple(location[axis] + step[axis] for axis in range(3))
            neighbor_index = occupied.get(neighbor)
            if neighbor_index is None:
                exposed[index][face] += 1
                key = (*location, face)
                if key in exposed_keys:
                    raise ValueError(f'{model["id"]}: duplicate exterior face unit {key}')
                exposed_keys.add(key)
            elif neighbor_index != index:
                connected[index].add(neighbor_index)
    seen = {0}
    queue = deque([0])
    while queue:
        index = queue.popleft()
        for neighbor in connected[index] - seen:
            seen.add(neighbor)
            queue.append(neighbor)
    if len(seen) != len(model["boxes"]):
        disconnected = [part["name"] for index, part in enumerate(model["boxes"]) if index not in seen]
        raise ValueError(f'{model["id"]}: disconnected parts: {disconnected}')
    hidden_full = 0
    partial_faces = 0
    exposed_faces = 0
    for index, part in enumerate(model["boxes"]):
        size_x, size_y, size_z = [part["to"][axis] - part["from"][axis] for axis in range(3)]
        areas = {"north": size_x * size_y, "south": size_x * size_y,
                 "east": size_z * size_y, "west": size_z * size_y,
                 "up": size_x * size_z, "down": size_x * size_z}
        if not sum(exposed[index].values()):
            raise ValueError(f'{model["id"]}: fully hidden box {part["name"]}')
        for face in FACE_NAMES:
            count = exposed[index][face]
            if count == 0:
                hidden_full += 1
            else:
                exposed_faces += 1
                partial_faces += count < areas[face]
    return {
        "exposed_unit_faces": exposed,
        "fully_hidden_rectangle_faces": hidden_full,
        "partially_contacted_rectangle_faces": partial_faces,
        "rectangle_faces_with_exterior_area": exposed_faces,
        "exterior_unit_surface_area": len(exposed_keys),
        "connected_components": 1,
        "duplicate_exterior_unit_faces": 0,
    }


def validate(definitions, atlas, tilemap):
    if [model["id"] for model in definitions] != list(EXPECTED_IDS):
        raise ValueError("Definitions must contain exactly the eight assigned IDs, in order")
    if atlas.mode != "RGBA" or atlas.size != (128, 128):
        raise ValueError("Atlas must be RGBA 128x128")
    if atlas.getchannel("A").getextrema() != (255, 255):
        raise ValueError("Transparent texels are prohibited")
    if len(tilemap["tiles"]) != 16:
        raise ValueError("Exactly sixteen atlas tiles are required")
    glow_tiles = set()
    ordinary_tiles = set()
    results = []
    for model in definitions:
        boxes = model["boxes"]
        if not 0 < len(boxes) <= BOX_LIMIT or len(boxes) != BUDGETS[model["id"]]:
            raise ValueError(f'{model["id"]}: box budget not met')
        if model["kind"] != "decor" or model["mount"] not in ("floor", "wall", "ceiling"):
            raise ValueError(f'{model["id"]}: invalid kind or mount')
        if type(model["light"]) is not int or not 0 <= model["light"] <= 15:
            raise ValueError(f'{model["id"]}: invalid light value')
        if len({part["name"] for part in boxes}) != len(boxes):
            raise ValueError(f'{model["id"]}: duplicate part names')
        seen_geometry = set()
        for part in boxes:
            coordinates = part["from"] + part["to"]
            if len(coordinates) != 6 or any(type(value) is not int or not 0 <= value <= 16 for value in coordinates):
                raise ValueError(f'{model["id"]}: endpoints must be integers in 0..16')
            if any(part["from"][axis] >= part["to"][axis] for axis in range(3)):
                raise ValueError(f'{model["id"]}: nonpositive volume')
            if "rotation" in part:
                raise ValueError(f'{model["id"]}: rotations are prohibited')
            identity = tuple(coordinates)
            if identity in seen_geometry:
                raise ValueError(f'{model["id"]}: duplicate structural box')
            seen_geometry.add(identity)
            if set(part.get("faces", {})) - set(FACE_NAMES):
                raise ValueError(f'{model["id"]}: unknown face override')
            indices = [part["tile"], *part.get("faces", {}).values()]
            for tile_index in indices:
                if type(tile_index) is not int or not 0 <= tile_index < 16:
                    raise ValueError(f'{model["id"]}: invalid tile index')
                if MATERIALS[tile_index][2] != bool(part.get("glow", False)):
                    raise ValueError(f'{model["id"]}: tile {tile_index} crosses glow semantics')
            (glow_tiles if part.get("glow", False) else ordinary_tiles).update(indices)
        occupied = occupancy_for(model)
        surface = surface_analysis(model, occupied)
        for turn in range(4):
            rotated = set()
            for location in occupied:
                coord_x, coord_y, coord_z = location
                for step in range(turn):
                    coord_x, coord_z = 15 - coord_z, coord_x
                rotated.add((coord_x, coord_y, coord_z))
            if len(rotated) != len(occupied) or any(not 0 <= value <= 15 for cell in rotated for value in cell):
                raise ValueError(f'{model["id"]}: horizontal facing transform invalid')
        mount = model["mount"]
        mount_axis = 2 if mount == "wall" else 1
        mount_value = 0 if mount == "floor" else 15
        if not any(location[mount_axis] == mount_value for location in occupied):
            raise ValueError(f'{model["id"]}: mount does not reach its supporting plane')
        glow_count = sum(bool(part.get("glow")) for part in boxes)
        if bool(model["light"]) != bool(glow_count):
            raise ValueError(f'{model["id"]}: luminous parts and block light disagree')
        results.append({
            "id": model["id"], "boxes": len(boxes), "box_budget": BOX_LIMIT,
            "unpruned_declared_face_upper_bound": 6 * len(boxes),
            "glow_boxes": glow_count, "light": model["light"],
            "occupied_unit_cubes": len(occupied), "positive_volume_intersections": 0,
            "duplicate_boxes": 0, "integer_endpoints": True, "zero_rotation": True,
            "mount_plane_reached": True, "four_horizontal_facing_bounds_checked": True,
            **surface,
        })
    if glow_tiles & ordinary_tiles or glow_tiles != {12, 13, 14, 15} or ordinary_tiles != set(range(12)):
        raise ValueError("Atlas glow / non-glow segregation failed")
    window = occupancy_for(definitions[-1])
    aperture_cells = [(coord_x, coord_y) for coord_x in range(3, 13) for coord_y in range(3, 13)]
    open_cells = [(coord_x, coord_y) for coord_x, coord_y in aperture_cells
                  if all((coord_x, coord_y, coord_z) not in window for coord_z in range(16))]
    if len(open_cells) != 63:
        raise ValueError("Window must have 63% true front-to-back aperture openness")
    lantern = occupancy_for(definitions[0])
    if not all((coord_x, 7, coord_z) not in lantern for coord_x in (5, 10) for coord_z in range(4, 12)):
        raise ValueError("Lantern cage sightlines are blocked")
    chandelier = occupancy_for(definitions[2])
    if (7, 5, 7) in chandelier:
        raise ValueError("Chandelier ring must remain hollow")
    return {
        "status": "pass", "definition_count": 8, "total_boxes": sum(row["boxes"] for row in results),
        "atlas": {"mode": "RGBA", "size": [128, 128], "pixels": 16384, "alpha_extrema": [255, 255],
                  "static_frames": 1, "base_rgba_bytes_not_vram": 65536, "all_16_tiles_used": True},
        "glow_tiles": sorted(glow_tiles), "ordinary_tiles": sorted(ordinary_tiles),
        "glow_tile_cross_use": False, "window_aperture_open_percent": 63,
        "window_emits_light": False, "lantern_two_through_sightlines": True,
        "chandelier_ring_hollow": True, "models": results,
        "limitations": [
            "Authoring definitions and QA adapter only; parent owns actual runtime / final export.",
            "No Blockbench session, Minecraft client, FPS or GPU measurements were performed.",
            "Preview is an orthographic CPU rasterizer with nearest texture sampling, not a game screenshot.",
            "Preview OFF texture scaling is illustrative; parent chooses actual LIT=false palette.",
            "Exterior unit-face counts are not GPU draw calls or the parent's final split-quad counts.",
        ],
    }


def vector_dot(first, second):
    return sum(first[axis] * second[axis] for axis in range(3))


def vector_cross(first, second):
    return (first[1] * second[2] - first[2] * second[1],
            first[2] * second[0] - first[0] * second[2],
            first[0] * second[1] - first[1] * second[0])


def normalized(vector):
    length = math.sqrt(vector_dot(vector, vector))
    return tuple(value / length for value in vector)


def face_records(part):
    start_x, start_y, start_z = part["from"]
    end_x, end_y, end_z = part["to"]
    size_x, size_y, size_z = end_x - start_x, end_y - start_y, end_z - start_z
    return {
        "north": ((start_x, end_y, start_z), (size_x, 0, 0), (0, -size_y, 0)),
        "south": ((end_x, end_y, end_z), (-size_x, 0, 0), (0, -size_y, 0)),
        "east": ((end_x, end_y, start_z), (0, 0, size_z), (0, -size_y, 0)),
        "west": ((start_x, end_y, end_z), (0, 0, -size_z), (0, -size_y, 0)),
        "up": ((start_x, end_y, start_z), (size_x, 0, 0), (0, 0, size_z)),
        "down": ((start_x, start_y, end_z), (size_x, 0, 0), (0, 0, -size_z)),
    }


def rasterize(model, tiles, view=(1.25, 0.95, -1.65), size=(344, 294), lit=True, ambient=1.0, clay=False):
    width, height = size
    image = Image.new("RGBA", size, "#17232d")
    pixels = image.load()
    depth_buffer = [-math.inf] * (width * height)
    camera = normalized(view)
    if abs(camera[1]) > 0.999:
        camera_right = (1.0, 0.0, 0.0)
        camera_up = (0.0, 0.0, -1.0 if camera[1] > 0 else 1.0)
    else:
        camera_right = normalized(vector_cross(camera, (0, 1, 0)))
        camera_up = normalized(vector_cross(camera_right, camera))
    scale = min(width, height) / 24.0
    light_direction = normalized((-0.45, 1.0, -0.75))

    def project(point):
        centered = tuple(point[axis] - 8 for axis in range(3))
        return (width / 2 + scale * vector_dot(centered, camera_right),
                height / 2 - scale * vector_dot(centered, camera_up), vector_dot(centered, camera))

    for part in model["boxes"]:
        for face, (origin, texture_axis, vertical_axis) in face_records(part).items():
            normal = FACE_STEPS[face]
            if vector_dot(normal, camera) <= 0.00001:
                continue
            projected_origin = project(origin)
            projected_texture = project(tuple(origin[axis] + texture_axis[axis] for axis in range(3)))
            projected_vertical = project(tuple(origin[axis] + vertical_axis[axis] for axis in range(3)))
            basis_u = tuple(projected_texture[axis] - projected_origin[axis] for axis in range(3))
            basis_v = tuple(projected_vertical[axis] - projected_origin[axis] for axis in range(3))
            determinant = basis_u[0] * basis_v[1] - basis_u[1] * basis_v[0]
            if abs(determinant) < 0.00001:
                continue
            corners = (projected_origin, projected_texture, projected_vertical,
                       tuple(projected_texture[axis] + basis_v[axis] for axis in range(3)))
            minimum_x = max(0, math.floor(min(point[0] for point in corners)))
            maximum_x = min(width - 1, math.ceil(max(point[0] for point in corners)))
            minimum_y = max(0, math.floor(min(point[1] for point in corners)))
            maximum_y = min(height - 1, math.ceil(max(point[1] for point in corners)))
            tile_index = part.get("faces", {}).get(face, part["tile"])
            shading = ambient * (0.58 + 0.40 * max(0, vector_dot(normal, light_direction)))
            if not lit and part.get("glow", False):
                shading *= 0.22
            source_colors = [(160, 171, 177, 255)] * 1024 if clay else list(tiles[tile_index].get_flattened_data())
            shaded_colors = [tuple(min(255, round(channel * shading)) for channel in color[:3]) + (255,)
                             for color in source_colors]
            for pixel_y in range(minimum_y, maximum_y + 1):
                delta_y = pixel_y + 0.5 - projected_origin[1]
                for pixel_x in range(minimum_x, maximum_x + 1):
                    delta_x = pixel_x + 0.5 - projected_origin[0]
                    texture_u = (delta_x * basis_v[1] - delta_y * basis_v[0]) / determinant
                    texture_v = (basis_u[0] * delta_y - basis_u[1] * delta_x) / determinant
                    if not (0 <= texture_u < 1 and 0 <= texture_v < 1):
                        continue
                    depth = projected_origin[2] + basis_u[2] * texture_u + basis_v[2] * texture_v
                    address = pixel_y * width + pixel_x
                    if depth <= depth_buffer[address] + 1e-8:
                        continue
                    depth_buffer[address] = depth
                    source_x = min(31, int(texture_u * 32))
                    source_y = min(31, int(texture_v * 32))
                    pixels[pixel_x, pixel_y] = shaded_colors[source_y * 32 + source_x]
    return image


def font(size, bold=False, mono=False):
    filename = "consola.ttf" if mono else ("msyhbd.ttc" if bold else "msyh.ttc")
    location = Path("C:/Windows/Fonts") / filename
    if location.is_file():
        return ImageFont.truetype(str(location), size)
    return ImageFont.load_default(size=size)


def draw_text(draw, position, text, size=16, color="#b9c7cc", bold=False, mono=False):
    draw.text(position, text, font=font(size, bold, mono), fill=color)


def contact_sheet(definitions, tiles, report):
    card_width, card_height = 492, 450
    margin, gap, header = 32, 16, 116
    sheet = Image.new("RGBA", (2080, 1240), "#0f1922")
    draw = ImageDraw.Draw(sheet)
    draw_text(draw, (32, 20), "PRISON / LIGHT & IRON", 19, "#c3a065", mono=True)
    draw_text(draw, (32, 48), "囚室灯火 · 铁作八件", 32, "#e0e6e2", True)
    draw_text(draw, (1360, 23), "08 ORIGINALS   /   128 x 128 ATLAS", 18, "#8eabb4", mono=True)
    draw_text(draw, (1360, 53), f'{report["total_boxes"]} STRUCTURAL BOXES  /  MAX 28 EACH', 18, "#c3a065", mono=True)
    for index, model in enumerate(definitions):
        row, column = divmod(index, 4)
        offset_x = margin + column * (card_width + gap)
        offset_y = header + row * (card_height + gap)
        draw.rounded_rectangle((offset_x, offset_y, offset_x + card_width, offset_y + card_height), radius=10, fill="#17232d", outline="#354c59", width=2)
        draw_text(draw, (offset_x + 16, offset_y + 13), f'{index + 1:02d}  {model["name_zh"]}', 23, "#e0cfaa", True)
        draw_text(draw, (offset_x + 17, offset_y + 46), model["id"], 12, "#8eabb4", mono=True)
        main = rasterize(model, tiles, size=(350, 302))
        rear = rasterize(model, tiles, view=(-1.25, 0.95, 1.65), size=(124, 118))
        unlit = rasterize(model, tiles, size=(124, 118), lit=False)
        sheet.paste(main, (offset_x + 2, offset_y + 73))
        sheet.paste(rear, (offset_x + 356, offset_y + 99))
        sheet.paste(unlit, (offset_x + 356, offset_y + 258))
        draw_text(draw, (offset_x + 370, offset_y + 78), "BACK", 12, "#8eabb4", mono=True)
        draw_text(draw, (offset_x + 370, offset_y + 236), "OFF*" if model["light"] else "NO LIGHT", 12, "#8eabb4", mono=True)
        count = len(model["boxes"])
        draw.line((offset_x + 16, offset_y + 388, offset_x + card_width - 16, offset_y + 388), fill="#354c59", width=1)
        draw_text(draw, (offset_x + 18, offset_y + 399), f'{count:02d} boxes  /  {count * 6:03d} faces max  /  light {model["light"]:02d}', 16, "#c3a065", mono=True)
        draw_text(draw, (offset_x + 18, offset_y + 423), f'{model["mount"].upper()}   |   OPAQUE PIXELS   |   ZERO OVERLAPS', 12, "#8eabb4", mono=True)
    draw_text(draw, (32, 1058), "ORIGINAL MATERIAL LIBRARY    00–11 STRUCTURE / 12–15 DEDICATED GLOW TILES", 17, "#c3a065", mono=True)
    for index, tile in enumerate(tiles):
        offset_x = 34 + index * 127
        sheet.paste(tile.resize((48, 48), Image.Resampling.NEAREST), (offset_x, 1094))
        draw_text(draw, (offset_x + 55, 1098), f'{index:02d}', 15, "#e0cfaa", mono=True)
        draw_text(draw, (offset_x, 1148), MATERIALS[index][1], 12, "#b9c7cc")
    draw_text(draw, (32, 1194), "OFFLINE CPU PREVIEW · NEAREST / FULL-TILE UV · NO BLOOM OR EMISSION SHADER · OFF* = ILLUSTRATIVE 22% GLOW-TILE BRIGHTNESS", 14, "#8eabb4", mono=True)
    sheet.save(ROOT / "contact-sheet.png", compress_level=9)


def diagnostic_sheets(definitions, tiles):
    views = (
        ("NORTH / FRONT", (0, 0, -1)), ("SOUTH / BACK", (0, 0, 1)),
        ("EAST", (1, 0, 0)), ("WEST", (-1, 0, 0)),
        ("UP", (0, 1, 0)), ("DOWN", (0, -1, 0)),
    )
    sheet = Image.new("RGBA", (1380, 1720), "#0f1922")
    draw = ImageDraw.Draw(sheet)
    draw_text(draw, (24, 14), "LIGHTING / SIX ORTHOGRAPHIC DIRECTIONS", 23, "#e0cfaa", True)
    for column, (label, view) in enumerate(views):
        draw_text(draw, (175 + column * 200, 56), label, 13, "#8eabb4", mono=True)
    for row, model in enumerate(definitions):
        offset_y = 94 + row * 200
        draw_text(draw, (16, offset_y + 65), f'{row + 1:02d}', 24, "#c3a065", mono=True)
        draw_text(draw, (16, offset_y + 100), model["name_zh"], 16, "#c6d0d0")
        for column, (label, view) in enumerate(views):
            image = rasterize(model, tiles, view=view, size=(188, 188))
            sheet.paste(image, (172 + column * 200, offset_y))
    sheet.save(WORK / "six-views.png", compress_level=9)
    conditions = (("CLAY", True, True, 1.0), ("NEUTRAL ON", False, True, 1.0),
                  ("DIM ON / 0.35", False, True, 0.35), ("NEUTRAL OFF*", False, False, 1.0))
    sheet = Image.new("RGBA", (1060, 1496), "#0f1922")
    draw = ImageDraw.Draw(sheet)
    draw_text(draw, (22, 14), "SILHOUETTE / TEXTURE / LIGHTING CHECK", 23, "#e0cfaa", True)
    for column, (label, clay, lit, ambient) in enumerate(conditions):
        draw_text(draw, (184 + column * 216, 54), label, 13, "#8eabb4", mono=True)
    for row, model in enumerate(definitions):
        offset_y = 88 + row * 174
        draw_text(draw, (16, offset_y + 65), model["name_zh"], 16, "#c6d0d0")
        for column, (label, clay, lit, ambient) in enumerate(conditions):
            image = rasterize(model, tiles, size=(206, 164), lit=lit, ambient=ambient, clay=clay)
            sheet.paste(image, (180 + column * 216, offset_y))
    sheet.save(WORK / "clay-and-lighting.png", compress_level=9)


def write_json(path, document):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(document, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def write_qa_adapter(definitions, atlas, tilemap, report):
    assets = WORK / "java-adapter" / "assets" / "ftdungeonprops"
    textures = assets / "textures" / "block"
    textures.mkdir(parents=True, exist_ok=True)
    atlas.save(textures / "lighting_atlas.png", compress_level=9)
    for model, result in zip(definitions, report["models"]):
        elements = []
        for index, part in enumerate(model["boxes"]):
            faces = {}
            for face in FACE_NAMES:
                if result["exposed_unit_faces"][index][face] == 0:
                    continue
                tile_index = part.get("faces", {}).get(face, part["tile"])
                faces[face] = {"uv": tilemap["tiles"][tile_index]["java_uv"], "texture": "#atlas"}
            elements.append({"name": part["name"], "from": part["from"], "to": part["to"], "faces": faces})
        exported = {"ambientocclusion": True, "textures": {"atlas": "ftdungeonprops:block/lighting_atlas", "particle": "#atlas"}, "elements": elements}
        write_json(assets / "models" / "block" / f'{model["id"]}.json', exported)


def write_design(definitions, report):
    rows = []
    for model, result in zip(definitions, report["models"]):
        rows.append(f'| `{model["id"]}` | {model["name_zh"]} | {result["boxes"]} | {result["unpruned_declared_face_upper_bound"]} | {result["glow_boxes"]} | {model["mount"]} | {model["light"]} |')
    cost_table = "\n".join(rows)
    face_rows = "\n".join(f'| `{result["id"]}` | {result["fully_hidden_rectangle_faces"]} | {result["partially_contacted_rectangle_faces"]} | {result["rectangle_faces_with_exterior_area"]} | {result["exterior_unit_surface_area"]} |' for result in report["models"])
    document = f"""# Lighting / Ironwork — original prison kit, 2026-11-03

## Deliverable and boundary

Eight original, single-block Minecraft Java 1.20.1 / Forge authoring definitions for namespace `ftdungeonprops`. The required files are `definitions.json`, `atlas.png`, `tilemap.json`, `make_assets.py`, `DESIGN.md`, and `contact-sheet.png`. All generated sources, previews and QA files stay inside this lighting lane. No existing FT project, save, installed mod or shared Blockbench session was opened for editing. The parent owns runtime code, final Java models, `.bbmodel` files, recipes and client QA.

Rebuild from the script directory using Python 3.12 + Pillow 13 (the supplied bundled interpreter). `make_assets.py` deterministically overwrites only its own generated files below its directory. It uses no network, random noise, image API, paid service or copied texture. Contact-sheet typography uses Windows Microsoft YaHei and Consolas; a Pillow fallback exists but will change preview typography on other systems. Geometry and atlas pixels do not depend on fonts.

```powershell
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' './make_assets.py'
```

## Shape and material decisions

1. **囚室巡夜灯 / Cell Watch Lantern:** four raised feet, six offset cage guards, a narrow amber burner, brass stepped cap and open carry bail. The sightlines at x=5 and x=10, y=7 remain empty through the cage. This is not the first batch's ten-box cyan soul lantern: the elongated tray, six-bar layout, four feet and small amber chimney are newly authored.
2. **狱吏悬臂灯 / Warden Bracket Lantern:** a service plate at the back, horizontal cantilever and short down-link carry an actual open lantern. This replaces the old wall-cresset/bowl language with a suspended ironwork silhouette; no wall niche or opaque flame-filled bowl is reused.
3. **四臂锁环吊灯 / Four Arm Iron Chandelier:** a hollow square ring carries four brass candle pans. Four tension links reach split cruciform beams, a main suspension stem and the fixing plate at y=16. The split beams avoid intersecting central volumes. The central ring opening is real, not painted black.
4. **守夜融蜡烛丛 / Vigil Candle Cluster:** an iron drip tray with four rim rails, a settled wax pool, three deliberately uneven candles and one burnt stub. Lit flame tops are y=14, 10 and 6. Only the three small flames glow; wax bodies and their long melt-flow clusters remain ordinary opaque material. Shoulders are structural two-step wax masses, not per-pixel extrusion.
5. **双胆炼金灯 / Twin Vessel Alchemical Lamp:** asymmetric large cyan retort and small amber receiver, brass split clamp, copper transfer elbow, valve and front pressure gauge on a teal enamel instrument foot. The front gauge is a dedicated face override. All vessels are opaque stylized fluid/crystal bodies, not glass or alpha planes. It has no brewing or inventory behavior.
6. **锁座寒晶簇 / Socketed Frost Crystal:** stone wall backplate and projecting corbel support three separate brass sockets. Three solid crystal bodies terminate at different heights with pale stepped crystal tips. No crystal floats or intersects its mounting.
7. **禁文立碑灯 / Bound Rune Tablet:** a double-faced engraved tablet is held between two carved stone piers, a lintel and four bronze retaining claws on a layered plinth. The rune was drawn specifically for this atlas. It is a decorative lamp, NOT a vanilla beacon: no beam, status effects, inventory or beacon mechanics.
8. **铁栅囚窗 / Iron Barred Prison Window:** four thick stone frame members, three vertical forged bars, a forward locking rail, a keystone and projecting sill. The 10×10-unit aperture retains exactly 63/100 completely open front-to-back unit columns. Alpha is 255 everywhere; there is no glass, fake transparent hole, teleportation or emission. Installation into an opaque full wall does not magically cut the adjacent wall; the opening must be built into the architecture. Runtime collision follows the actual union, not a promised walk-through portal.

## Geometry and mount contract

NORTH/front is z=0, wall/back is z=16. Floor models touch y=0, wall fixtures touch z=16, and the chandelier touches y=16. Every endpoint is an integer within 0..16. Every box has positive volume and zero rotation. Every model is one face-connected structural assembly, including flames, sockets, beams and mounting hardware. All pairwise positive-volume overlap and duplicate-box checks pass; no hidden zero-contribution box is retained. Touching surface regions are deliberate structural joints, not duplicate visible faces. The parent may prune/split internal contacting face regions during final export.

### Costs — source definitions, not GPU measurements

Budget was set before generation: maximum 28 boxes each, 168 unpruned declared rectangle faces each, one shared static 128×128 opaque atlas, zero transparent planes, zero outline shells, zero animations or frame copies. Planned source counts were 19/14/25/16/17/11/11/10 and actual counts match.

| ID | Name | Boxes | Unpruned face ceiling | Glow boxes | Mount | Block light |
|---|---|---:|---:|---:|---|---:|
{cost_table}

Total source cost: **{report['total_boxes']} structural boxes / {report['total_boxes'] * 6} unpruned face ceiling**. All eight are static. Counts are per selected model, not sixteen LIT-state copies loaded together. Seven lamps are intended for parent-provided LIT and horizontal FACING; the window has horizontal FACING but no light. Materials are sixteen semantic atlas tiles, not sixteen measured draw calls. One RGBA atlas is 16,384 texels / 65,536 base bytes, excluding mips, GPU alignment and engine copies; this is not measured VRAM.

| ID | Fully hidden rectangle faces | Partial-contact rectangle faces | Rectangles with some exterior area | Exterior unit area |
|---|---:|---:|---:|---:|
{face_rows}

These are exact unit-grid surface statistics for the authoring boxes. A partially contacted face can require splitting when exported, so neither the exterior-rectangle column nor the QA adapter's face count is a claim about final parent-export quads. The adapter removes only faces proven fully internal; it never deletes a partially visible face wholesale.

## Atlas and UV

The original RGBA atlas is exactly 128×128, four columns by four rows of 32×32. Tile index is row-major; pixel bounds and Java 0..16 UV rectangles are explicit in `tilemap.json`. Intended rendering is nearest filtering with one complete tile per face; dedicated gauge/rune motifs must not be cropped to a tiny corner. Small bars therefore intentionally use a stretched reusable surface, not a globally uniform texel-density unwrap. This authoring schema has no per-box UV-size field. Palette clusters, forged streaks, copper oxidation, stone chips, wick marks and wax drips are rasterized directly with no smoothing, stochastic TV noise or pixel-to-box extrusion.

Tiles **0–11** are structure-only: iron, riveted plate, cold stone, socket stone, brass, copper, wax, char, enamel, gauge, service plate and wax pool. Tiles **12–15** are exclusive to `glow:true` boxes: amber flame, frost core, rune tablet, cyan vessel/crystal. Every default tile and face override is checked against this partition. No glowing tile is reused by non-glowing geometry, including candle wax and the entire window. The rune's side-face overrides remain within the glowing partition.

Parent integration should darken only the dedicated glowing tiles for LIT=false; it must not dim unrelated wax, brass, stone or gauge faces. Java 1.20.1 material albedo alone does not provide shader emissivity or bloom. The CPU preview adds neither. OFF thumbnails illustrate a 0.22 multiplier on glowing tiles, not the parent's finalized dark palette. The DIM ON diagnostic scales all surfaces by 0.35 and is a readability comparison, not a physical lighting simulation. Main-hand toggle handling, actual block light state and collision are parent work, not implemented here.

## Reference and originality

Read-only reference: [Mine Cells 2.0.0+1.20.1](https://github.com/mim1q/MineCells/tree/minecells-2.0.0%2B1.20.1), including its public prison guide image and asset-tree names. Borrowed ideas are limited to cool damp prison masonry, weathered iron, warm/cyan contrast, cage negative space and compact alchemical equipment. The upstream README states that textures/models are **All Rights Reserved**; none is copied, bundled, traced, recolored or redistributed here. All eight coordinates, all sixteen tile drawings and the rune are newly authored from primitives. Existing first-batch lantern/cresset source was inspected read-only solely to avoid repeating its silhouette. No upstream or first-batch image is included in this lane.

## Actual checks and preview evidence

`make_assets.py` runs fail-fast checks for the exact eight IDs, kind/mount/light schema, 28-box budget, all coordinate bounds and integer types, positive volume, zero rotation, unique part names, duplicate boxes, occupied-volume intersections, connected structural assembly, hidden boxes, mount contact, all four horizontal-facing bounds, all tile references and face overrides, strict glow tile segregation, atlas size/mode/alpha, and reopened-file round trips. Window openness, lantern through-sightlines and chandelier ring openness have explicit feature assertions. The generated `work/qa/validation-report.json` records exact counts, complete limitations and detailed per-box unit-face coverage.

- `contact-sheet.png`: eight neutral-light front-right authoring renders, rear miniatures, and illustrative OFF miniatures, plus all sixteen original material swatches.
- `work/qa/six-views.png`: all eight models in front, back, east, west, up and down orthographic projections with identical 0..16 framing.
- `work/qa/clay-and-lighting.png`: all eight in untextured clay, neutral ON, dim ON and neutral OFF conditions, with fixed projection and no bloom.
- `work/qa/java-adapter/assets/`: isolated QA-only Java JSON conversion with explicit UV, one self-contained atlas and fully internal full faces removed. It is not the final parent export and is not installed anywhere.
- `work/qa/artifact-hashes.json`: SHA-256 of required deliverables for deterministic rebuild comparison.

Generator checks and offline projections are performed here. The delivered isolated QA adapter was additionally checked read-only with the skill's `audit_model.py`: **8/8 pass, zero errors, zero warnings, all texture dependencies resolved, 123 elements and 590 declared adapter faces**. Eight detailed `work/qa/*.audit.json` reports preserve the audit's limitations. The audit uses the closest available `java-legacy` structural profile; that profile is not a complete Java 1.20.1 certification. The parent independently checks final compiled models, `.bbmodel` import, actual LIT/FACING behavior, recipes, loot and in-game presentation. **No shared editor inspection, Minecraft client/server launch, Display-slot validation, runtime collision test, performance measurement or FPS claim is made.** The CPU renderer is original, orthographic and uses full-tile UV; it is not Blockbench or Minecraft. A final game render can differ in ambient occlusion, lighting, UV policy and item transforms.

API/service cost: **0 paid image requests / 0 image API charges**. Source and atlas are authored locally. Public GitHub reads were reference inspection only. No token-price or external runtime-cost estimate is invented.
"""
    (ROOT / "DESIGN.md").write_text(document, encoding="utf-8")


def main():
    WORK.mkdir(parents=True, exist_ok=True)
    definitions = make_definitions()
    atlas, tiles = make_atlas()
    tilemap = make_tilemap()
    report = validate(definitions, atlas, tilemap)
    write_json(ROOT / "definitions.json", definitions)
    write_json(ROOT / "tilemap.json", tilemap)
    atlas.save(ROOT / "atlas.png", compress_level=9)
    write_qa_adapter(definitions, atlas, tilemap, report)
    contact_sheet(definitions, tiles, report)
    diagnostic_sheets(definitions, tiles)
    if json.loads((ROOT / "definitions.json").read_text(encoding="utf-8")) != definitions:
        raise ValueError("Definitions JSON did not round-trip")
    if json.loads((ROOT / "tilemap.json").read_text(encoding="utf-8")) != tilemap:
        raise ValueError("Tilemap JSON did not round-trip")
    with Image.open(ROOT / "atlas.png") as reopened:
        if reopened.mode != "RGBA" or reopened.size != (128, 128) or reopened.tobytes() != atlas.tobytes():
            raise ValueError("Atlas PNG did not round-trip")
    previews = {}
    for relative in ("contact-sheet.png", "work/qa/six-views.png", "work/qa/clay-and-lighting.png"):
        with Image.open(ROOT / relative) as reopened:
            reopened.verify()
        with Image.open(ROOT / relative) as reopened:
            previews[relative] = list(reopened.size)
    report["saved_file_roundtrip"] = True
    report["preview_png_sizes"] = previews
    write_json(WORK / "validation-report.json", report)
    write_design(definitions, report)
    required = ("definitions.json", "atlas.png", "tilemap.json", "make_assets.py", "DESIGN.md", "contact-sheet.png")
    hashes = {filename: hashlib.sha256((ROOT / filename).read_bytes()).hexdigest() for filename in required}
    write_json(WORK / "artifact-hashes.json", hashes)
    print(json.dumps({"status": report["status"], "output": str(ROOT), "total_boxes": report["total_boxes"],
                      "costs": {row["id"]: row["boxes"] for row in report["models"]},
                      "window_open_percent": report["window_aperture_open_percent"],
                      "glow_tiles": report["glow_tiles"], "png_roundtrip": True}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
