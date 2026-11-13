from __future__ import annotations

import argparse
import copy
import hashlib
import io
import json
import math
import sys
from collections import Counter, deque
from itertools import combinations
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent
WORK = ROOT / "work"
SIZE = 32
FACES = ("north", "south", "east", "west", "up", "down")
PALETTE = {
    "stone_dark": "#253744",
    "stone": "#354c59",
    "stone_mid": "#4e6570",
    "stone_light": "#74858b",
    "cavity": "#141d27",
    "iron_dark": "#252b35",
    "iron": "#59626a",
    "rust_dark": "#784936",
    "rust": "#b4794e",
    "brass": "#8d6b3c",
    "brass_light": "#c3a065",
    "wood_dark": "#493532",
    "wood": "#735044",
    "cloth_dark": "#542b42",
    "cloth": "#8b4356",
    "bone": "#bcae8b",
    "paper": "#e0cfaa",
    "cyan_dark": "#247f8b",
    "cyan": "#50cfca",
    "cyan_light": "#b8f2de",
    "amber_dark": "#bd682d",
    "amber": "#f7bf58",
    "amber_light": "#fff0b0",
}
TILE_NAMES = (
    ("salvaged_planks", "旧木竖纹", "wood"),
    ("boarded_endgrain", "木端与桶盖", "wood"),
    ("weathered_iron", "锈铁与铆钉", "iron"),
    ("aged_brass", "暗铜锁件", "brass"),
    ("prison_ashlar", "凉灰石砌面", "stone"),
    ("recess_shadow", "实心龛背暗石", "stone"),
    ("broken_key_standard", "原创断钥旗纹", "cloth"),
    ("ledger_paper", "账页与卷纸", "paper"),
    ("bound_volumes", "分色旧书脊", "book"),
    ("opaque_cyan_glass", "不透明青釉药瓶", "glass"),
    ("opaque_amber_glass", "不透明琥珀药瓶", "glass"),
    ("carved_skull", "骨面与凹眼", "bone"),
    ("bundled_bones", "束骨与骨纹", "bone"),
    ("forged_steel", "钢刃与中脊", "steel"),
    ("wax_paper_seal", "纸封与暗红蜡印", "paper"),
    ("plain_burgundy", "暗红布褶与书皮", "cloth"),
)


def box(name, start, end, tile, faces=None):
    result = {"name": name, "from": list(start), "to": list(end), "tile": tile}
    if faces:
        result["faces"] = faces
    return result


def definition(identifier, chinese, english, boxes, description, ingredient, mount="floor"):
    return {
        "id": identifier,
        "name_zh": chinese,
        "name_en": english,
        "kind": "decor",
        "mount": mount,
        "light": 0,
        "ingredient": ingredient,
        "boxes": boxes,
        "description_zh": description,
    }


def make_definitions():
    crate = []
    for label, horizontal, depth in (
        ("front left", 1, 1),
        ("front right", 13, 1),
        ("back left", 1, 13),
        ("back right", 13, 13),
    ):
        crate.append(box(f"iron corner {label}", (horizontal, 0, depth), (horizontal + 2, 12, depth + 2), 2))
    for level, bottom in (("lower", 0), ("upper", 10)):
        crate.extend((
            box(f"{level} front rail", (3, bottom, 1), (13, bottom + 2, 3), 0, {"up": 1}),
            box(f"{level} back rail", (3, bottom, 13), (13, bottom + 2, 15), 0, {"up": 1}),
            box(f"{level} left rail", (1, bottom, 3), (3, bottom + 2, 13), 0, {"up": 1}),
            box(f"{level} right rail", (13, bottom, 3), (15, bottom + 2, 13), 0, {"up": 1}),
        ))
    crate.extend((
        box("inset boarded core and lid", (3, 1, 3), (13, 12, 13), 0, {"up": 1, "down": 1}),
        box("front paper seal", (6, 3, 2), (10, 8, 3), 14),
        box("lid sealing strip", (7, 12, 3), (9, 13, 13), 14),
        box("brass locking hasp", (7, 8, 1), (9, 10, 3), 3),
    ))

    barrel = []
    tiers = (
        ("standing chime", 0, 1, 4, 12, 4, 12, 1),
        ("lower shoulder", 1, 3, 3, 13, 3, 13, 0),
        ("lower iron hoop", 3, 4, 1, 15, 1, 15, 2),
        ("stave belly", 4, 11, 2, 14, 2, 14, 0),
        ("upper iron hoop", 11, 12, 1, 15, 1, 15, 2),
        ("upper shoulder", 12, 14, 3, 13, 3, 13, 0),
        ("boarded lid", 14, 15, 3, 13, 3, 13, 1),
    )
    for label, bottom, top, left, right, front, back, tile in tiers:
        bevel = 2 if right - left >= 10 else 1
        barrel.extend((
            box(f"{label} center", (left + bevel, bottom, front), (right - bevel, top, back), tile, {"up": 1} if tile == 0 else None),
            box(f"{label} left bevel", (left, bottom, front + bevel), (left + bevel, top, back - bevel), tile, {"up": 1} if tile == 0 else None),
            box(f"{label} right bevel", (right - bevel, bottom, front + bevel), (right, top, back - bevel), tile, {"up": 1} if tile == 0 else None),
        ))
    barrel.append(box("sealed wooden bung", (7, 15, 7), (9, 16, 9), 3, {"up": 1}))

    post = [
        box("wide timber base", (4, 0, 5), (12, 2, 13), 0, {"up": 1}),
        box("front stabilizing foot", (3, 0, 3), (13, 1, 5), 0, {"up": 1}),
        box("back stabilizing foot", (3, 0, 13), (13, 1, 15), 0, {"up": 1}),
        box("lower upright", (6, 2, 8), (10, 9, 12), 0, {"up": 1}),
        box("structural iron collar", (6, 9, 8), (10, 10, 12), 2),
        box("upper upright", (6, 10, 8), (10, 16, 12), 0, {"up": 1}),
        box("transverse hanging beam", (1, 11, 6), (15, 13, 8), 0, {"up": 1, "south": 0}),
        box("rear reinforcing strap", (6, 4, 12), (10, 14, 13), 2),
    ]
    for label, left in (("left", 1), ("right", 11)):
        post.extend((
            box(f"{label} shackle hanger", (left + 1, 9, 5), (left + 3, 11, 7), 2),
            box(f"{label} shackle top", (left, 8, 4), (left + 4, 9, 6), 2),
            box(f"{label} shackle bottom", (left, 5, 4), (left + 4, 6, 6), 2),
            box(f"{label} shackle outer side", (left, 6, 4), (left + 1, 8, 6), 2),
            box(f"{label} shackle inner side", (left + 3, 6, 4), (left + 4, 8, 6), 2),
        ))

    rack = [
        box("left long foot", (0, 0, 5), (5, 2, 15), 0, {"up": 1}),
        box("right long foot", (11, 0, 5), (16, 2, 15), 0, {"up": 1}),
        box("left rack post", (1, 2, 10), (3, 15, 12), 0, {"up": 1}),
        box("right rack post", (13, 2, 10), (15, 15, 12), 0, {"up": 1}),
        box("lower rear crossrail", (3, 4, 10), (13, 6, 12), 0, {"south": 2, "up": 1}),
        box("upper rear crossrail", (3, 12, 10), (13, 14, 12), 0, {"south": 2, "up": 1}),
        box("weapon resting ledge", (2, 2, 5), (14, 3, 10), 0, {"up": 1}),
        box("sword pommel", (4, 3, 7), (6, 4, 9), 3),
        box("sword leather grip", (4, 4, 7), (6, 7, 9), 15),
        box("sword crossguard", (3, 7, 7), (7, 8, 9), 3),
        box("sword steel blade", (4, 8, 7), (6, 14, 8), 13),
        box("sword narrow tip", (4, 14, 7), (5, 15, 8), 13),
        box("bearded axe haft", (10, 3, 7), (11, 14, 9), 0, {"up": 1}),
        box("bearded axe crown", (11, 10, 7), (14, 14, 8), 13),
        box("bearded axe hanging edge", (12, 8, 7), (14, 10, 8), 13),
        box("axe rear poll", (9, 11, 7), (10, 13, 9), 2),
        box("sword retaining saddle", (4, 12, 8), (6, 13, 10), 2),
        box("axe retaining saddle", (10, 12, 9), (11, 13, 10), 2),
    ]

    shelf = [
        box("wide standing plinth", (1, 0, 7), (15, 2, 15), 0, {"up": 1}),
        box("left load bearing upright", (1, 2, 10), (3, 16, 14), 0, {"up": 1}),
        box("right load bearing upright", (13, 2, 10), (15, 16, 14), 0, {"up": 1}),
        box("lower shelf board", (3, 2, 8), (13, 3, 14), 0, {"up": 1}),
        box("middle shelf board", (3, 8, 8), (13, 9, 14), 0, {"up": 1}),
        box("upper shelf cornice", (3, 15, 8), (13, 16, 14), 0, {"up": 1}),
        box("continuous rear backing", (3, 2, 14), (13, 16, 15), 0, {"south": 0}),
        box("lower cyan bottle body", (4, 3, 9), (7, 6, 12), 9),
        box("lower cyan bottle neck", (5, 6, 10), (6, 7, 11), 9),
        box("lower cyan bottle cork", (5, 7, 10), (6, 8, 11), 0, {"up": 1}),
        box("lower bound book group", (8, 3, 10), (12, 7, 13), 8, {"up": 7, "east": 7}),
        box("upper amber flask body", (9, 9, 9), (12, 12, 12), 10),
        box("upper amber flask neck", (10, 12, 10), (11, 14, 11), 10),
        box("upper amber flask cork", (10, 14, 10), (11, 15, 11), 0, {"up": 1}),
        box("upper slim folio", (4, 9, 10), (6, 14, 13), 8, {"up": 7, "east": 7}),
        box("upper squat cyan jar", (6, 9, 10), (8, 11, 12), 9),
        box("upper jar brass lid", (6, 11, 10), (8, 12, 12), 3),
    ]

    banner = [
        box("left wall anchor", (3, 12, 14), (5, 16, 16), 2),
        box("right wall anchor", (11, 12, 14), (13, 16, 16), 2),
        box("iron hanging rod", (2, 13, 12), (14, 15, 14), 2),
        box("broad heraldic cloth", (4, 6, 12), (12, 13, 13), 15, {"north": 6}),
        box("short left torn lobe", (4, 2, 12), (6, 6, 13), 15),
        box("receded central torn lobe", (6, 4, 12), (9, 6, 13), 15),
        box("long right torn lobe", (9, 1, 12), (11, 6, 13), 15),
    ]

    niche = [
        box("continuous stone foundation", (1, 0, 3), (15, 2, 15), 4),
        box("left masonry pier", (1, 2, 4), (4, 14, 15), 4),
        box("right masonry pier", (12, 2, 4), (15, 14, 15), 4),
        box("solid recessed back wall", (4, 2, 13), (12, 14, 15), 4, {"north": 5}),
        box("heavy stone crown", (1, 14, 4), (15, 16, 15), 4),
        box("left stepped arch corbel", (4, 11, 4), (6, 14, 13), 4),
        box("right stepped arch corbel", (10, 11, 4), (12, 14, 13), 4),
        box("central arch keystone", (6, 13, 4), (10, 14, 13), 4),
        box("internal bone carrying dais", (4, 2, 6), (12, 4, 13), 4, {"north": 12}),
        box("supported skull mandible", (6, 4, 9), (10, 5, 12), 12),
        box("supported skull cranium", (6, 5, 9), (10, 9, 12), 12, {"north": 11}),
        box("left bone bundle", (4, 4, 9), (6, 6, 12), 12),
        box("right bone bundle", (10, 4, 9), (12, 6, 12), 12),
        box("brass memorial plaque", (6, 2, 4), (10, 3, 6), 3),
    ]

    lectern = [
        box("stable stone foot", (3, 0, 3), (13, 2, 14), 4),
        box("timber pedestal shoe", (5, 2, 6), (11, 3, 12), 0, {"up": 1}),
        box("square warden pedestal", (6, 3, 7), (10, 8, 11), 0, {"up": 1}),
        box("wide load bearing collar", (5, 8, 6), (11, 9, 12), 2),
        box("broad reading desk corbel", (4, 9, 5), (12, 10, 12), 0, {"up": 1}),
        box("horizontal reading desk", (2, 10, 3), (14, 12, 13), 0, {"up": 1, "south": 2}),
        box("front page retaining lip", (2, 12, 3), (14, 13, 4), 0, {"up": 1}),
        box("burgundy open ledger cover", (3, 12, 5), (11, 13, 12), 15),
        box("left open ledger page block", (3, 13, 5), (6, 14, 12), 7, {"north": 12, "south": 12}),
        box("dark central book gutter", (6, 13, 5), (7, 14, 12), 0),
        box("right open ledger page block", (7, 13, 5), (11, 14, 12), 7, {"north": 12, "south": 12}),
        box("rolled paper scroll", (12, 12, 6), (14, 14, 10), 7),
        box("scroll front spindle", (12, 12, 5), (14, 14, 6), 3),
        box("scroll back spindle", (12, 12, 10), (14, 14, 11), 3),
        box("pedestal brass appointment badge", (7, 4, 6), (9, 7, 7), 3),
    ]

    return [
        definition("prison_supply_crate", "封存补给箱", "Sealed Supply Crate", crate, "封条、旧木框与四角铁护件构成的单格陈设；不可打开，无储物功能。", "minecraft:barrel"),
        definition("prison_ironbound_barrel", "锈箍鼓腹桶", "Ironbound Stave Barrel", barrel, "收腰桶肩、鼓腹木板与两圈凸出的铁箍；密封木塞仅作装饰，不存液体。", "minecraft:barrel"),
        definition("prison_shackled_post", "双镣刑柱", "Twin Shackle Post", post, "带稳定木脚、后铁带和两枚实体中空方镣的刑柱；不拘束、不伤害实体。", "minecraft:chain"),
        definition("prison_weapon_rack", "狱卒兵器架", "Jailer Weapon Rack", rack, "长剑与垂刃斧拥有独立轮廓、底部托板和后固定卡座；不可取用武器。", "minecraft:iron_ingot"),
        definition("prison_alchemy_shelf", "旧炼金药书架", "Apothecary Book Shelf", shelf, "两层药瓶、窄颈瓶、青釉矮罐及旧书；实心不透明贴图，不提供酿造或储物。", "minecraft:glass_bottle"),
        definition("prison_torn_banner", "断钥残旗", "Torn Key Standard", banner, "暗红旧旗配原创断钥纹样，三片大幅残摆形成真实不规则轮廓；背面为素布。", "minecraft:red_wool", "wall"),
        definition("prison_ossuary_niche", "支拱藏骨龛", "Corbelled Ossuary Niche", niche, "实体石基、双墩和阶拱支撑深凹骨龛，头骨与束骨坐在内台；无生成或祭献功能。", "minecraft:bone_block"),
        definition("prison_warden_lectern", "典狱账卷台", "Warden Ledger Lectern", lectern, "石脚木柱承托开放账册、暗色书脊和独立卷轴；台面水平，不读取或存储书本。", "minecraft:book"),
    ]


def rectangle(draw, bounds, color):
    draw.rectangle(bounds, fill=PALETTE.get(color, color))


def line(draw, points, color, width=1):
    draw.line(points, fill=PALETTE.get(color, color), width=width)


def make_tiles():
    tiles = []
    for index in range(16):
        image = Image.new("RGBA", (32, 32), PALETTE["cavity"])
        draw = ImageDraw.Draw(image)
        if index == 0:
            rectangle(draw, (0, 0, 31, 31), "wood")
            for seam in (0, 10, 21, 31):
                rectangle(draw, (seam, 0, seam, 31), "wood_dark")
            for edge in (1, 11, 22):
                rectangle(draw, (edge, 1, edge + 1, 28), "#966c53")
            for bounds in ((5, 2, 6, 8), (7, 15, 8, 25), (15, 5, 16, 16), (18, 20, 19, 29), (26, 4, 27, 12), (24, 22, 25, 30)):
                rectangle(draw, bounds, "wood_dark")
            for center in ((5, 12), (16, 24), (27, 17)):
                draw.ellipse((center[0] - 2, center[1] - 2, center[0] + 2, center[1] + 2), fill=PALETTE["wood_dark"])
                rectangle(draw, (center[0], center[1] - 1, center[0] + 1, center[1] + 1), "#a17658")
            for nail in ((4, 3), (17, 3), (27, 28)):
                rectangle(draw, (nail[0], nail[1], nail[0] + 1, nail[1] + 1), "iron_dark")
        elif index == 1:
            rectangle(draw, (0, 0, 31, 31), "wood")
            for seam in (0, 9, 20, 31):
                line(draw, ((0, seam), (31, seam)), "wood_dark", 1)
                if seam < 30:
                    line(draw, ((2, seam + 1), (29, seam + 1)), "#a17658")
            line(draw, ((3, 5), (13, 5), (15, 6), (27, 6)), "wood_dark")
            line(draw, ((1, 15), (7, 15), (10, 13), (14, 13)), "wood_dark")
            line(draw, ((3, 26), (12, 26), (14, 25), (28, 25)), "wood_dark")
            draw.ellipse((17, 12, 27, 18), outline=PALETTE["wood_dark"], width=1)
            draw.ellipse((20, 14, 24, 16), fill=PALETTE["wood_dark"])
            rectangle(draw, (3, 3, 12, 3), "#a17658")
            rectangle(draw, (4, 23, 10, 23), "#a17658")
        elif index == 2:
            rectangle(draw, (0, 0, 31, 31), "iron_dark")
            rectangle(draw, (2, 2, 29, 29), "#3b424b")
            rectangle(draw, (2, 1, 29, 2), "iron")
            rectangle(draw, (2, 3, 3, 24), "stone_light")
            rectangle(draw, (5, 26, 29, 29), "iron_dark")
            draw.polygon(((0, 19), (5, 19), (5, 22), (9, 22), (9, 27), (13, 27), (13, 31), (0, 31)), fill=PALETTE["rust_dark"])
            for bounds in ((1, 24, 4, 27), (5, 27, 7, 30), (10, 29, 12, 31), (26, 9, 30, 12), (28, 13, 31, 15)):
                rectangle(draw, bounds, "rust")
            for horizontal, vertical in ((6, 6), (25, 6), (6, 25), (25, 25)):
                rectangle(draw, (horizontal - 1, vertical - 1, horizontal + 1, vertical + 1), "iron_dark")
                rectangle(draw, (horizontal - 1, vertical - 1, horizontal, vertical), "stone_light")
        elif index == 3:
            rectangle(draw, (0, 0, 31, 31), "brass")
            rectangle(draw, (2, 2, 29, 29), "#a27d45")
            rectangle(draw, (1, 1, 30, 2), "brass_light")
            rectangle(draw, (1, 2, 2, 29), "brass_light")
            rectangle(draw, (3, 28, 30, 31), "#66523b")
            rectangle(draw, (28, 4, 31, 28), "#66523b")
            draw.rectangle((8, 8, 23, 23), outline=PALETTE["brass"], width=2)
            rectangle(draw, (14, 14, 17, 17), "#66523b")
            rectangle(draw, (14, 14, 15, 15), "brass_light")
            for bounds in ((5, 5, 7, 7), (24, 5, 26, 7), (5, 23, 7, 25)):
                rectangle(draw, bounds, "paper")
            rectangle(draw, (23, 24, 26, 26), "rust_dark")
        elif index == 4:
            rectangle(draw, (0, 0, 31, 31), "stone")
            rectangle(draw, (1, 1, 10, 13), "stone_mid")
            rectangle(draw, (12, 1, 30, 13), "#435b67")
            rectangle(draw, (1, 16, 23, 30), "#405765")
            rectangle(draw, (25, 16, 30, 30), "stone_mid")
            line(draw, ((0, 14), (31, 14)), "stone_dark", 2)
            line(draw, ((11, 0), (11, 13)), "stone_dark")
            line(draw, ((24, 16), (24, 31)), "stone_dark")
            line(draw, ((1, 0), (10, 0)), "stone_light")
            line(draw, ((13, 0), (28, 0)), "stone_light")
            line(draw, ((2, 16), (18, 16)), "stone_mid")
            for bounds in ((3, 4, 6, 5), (20, 9, 26, 10), (6, 25, 12, 27), (26, 18, 28, 20)):
                rectangle(draw, bounds, "stone")
            line(draw, ((17, 1), (17, 4), (19, 4), (19, 7)), "stone_dark")
        elif index == 5:
            rectangle(draw, (0, 0, 31, 31), "stone_dark")
            rectangle(draw, (2, 2, 29, 30), "cavity")
            rectangle(draw, (4, 4, 27, 6), "#1c2833")
            rectangle(draw, (4, 7, 6, 26), "#1c2833")
            rectangle(draw, (24, 8, 27, 28), "#1c2833")
            line(draw, ((10, 3), (10, 11), (12, 11), (12, 17)), "stone_dark")
            line(draw, ((20, 3), (20, 8)), "stone_dark")
            rectangle(draw, (8, 28, 23, 30), "#20303c")
        elif index == 6:
            rectangle(draw, (0, 0, 31, 31), "cloth_dark")
            for bounds in ((3, 0, 6, 31), (23, 0, 26, 31)):
                rectangle(draw, bounds, "#6e354c")
            rectangle(draw, (7, 0, 9, 31), "cloth")
            rectangle(draw, (20, 0, 22, 31), "#3f2639")
            line(draw, ((2, 0), (2, 31)), "rust_dark")
            line(draw, ((29, 0), (29, 31)), "rust_dark")
            draw.rectangle((11, 5, 21, 15), outline=PALETTE["bone"], width=3)
            rectangle(draw, (14, 8, 18, 12), "cloth_dark")
            rectangle(draw, (15, 15, 17, 26), "bone")
            rectangle(draw, (17, 23, 23, 25), "bone")
            rectangle(draw, (21, 20, 23, 24), "bone")
            rectangle(draw, (15, 19, 17, 20), "cloth_dark")
            rectangle(draw, (12, 5, 16, 6), "paper")
            rectangle(draw, (5, 25, 8, 26), "cloth_dark")
        elif index == 7:
            rectangle(draw, (0, 0, 31, 31), "bone")
            rectangle(draw, (3, 1, 29, 29), "paper")
            rectangle(draw, (0, 0, 2, 31), "#9c8869")
            rectangle(draw, (30, 2, 31, 31), "#a39071")
            line(draw, ((6, 3), (24, 3)), "rust_dark")
            for vertical, runs in ((7, ((6, 12), (15, 24))), (11, ((6, 18), (21, 26))), (15, ((6, 10), (13, 24))), (19, ((6, 20), (23, 26))), (23, ((6, 14), (17, 23)))):
                for left, right in runs:
                    rectangle(draw, (left, vertical, right, vertical + 1), "#9a8768")
            rectangle(draw, (23, 25, 27, 28), "cloth")
            line(draw, ((4, 30), (28, 30)), "#a39071")
        elif index == 8:
            rectangle(draw, (0, 0, 31, 31), "wood_dark")
            for left, right, color in ((1, 7, "cloth"), (9, 14, "stone"), (16, 23, "wood"), (25, 30, "cloth_dark")):
                rectangle(draw, (left, 1, right, 30), color)
                rectangle(draw, (left, 1, left, 30), "#a87965" if left != 9 else "stone_light")
                for vertical in (4, 7, 25, 28):
                    rectangle(draw, (left + 1, vertical, right - 1, vertical), "brass_light")
                rectangle(draw, (left + 2, 12, right - 1, 17), "wood_dark")
                rectangle(draw, (left + 2, 12, right - 1, 13), "bone")
            rectangle(draw, (2, 0, 6, 0), "paper")
            rectangle(draw, (17, 31, 22, 31), "bone")
        elif index == 9:
            rectangle(draw, (0, 0, 31, 31), "cyan_dark")
            rectangle(draw, (0, 2, 4, 29), "#1c4f5d")
            rectangle(draw, (5, 3, 9, 26), "cyan")
            rectangle(draw, (6, 4, 7, 14), "cyan_light")
            rectangle(draw, (10, 5, 12, 22), "#3ba8ad")
            rectangle(draw, (23, 2, 30, 29), "#1b616f")
            rectangle(draw, (1, 18, 30, 20), "#2f9f9f")
            rectangle(draw, (5, 18, 9, 20), "cyan")
            rectangle(draw, (2, 28, 29, 31), "#164652")
            rectangle(draw, (11, 1, 22, 2), "#3ba8ad")
            rectangle(draw, (18, 23, 20, 25), "cyan_dark")
        elif index == 10:
            rectangle(draw, (0, 0, 31, 31), "amber_dark")
            rectangle(draw, (0, 1, 4, 29), "rust_dark")
            rectangle(draw, (5, 3, 10, 27), "amber")
            rectangle(draw, (6, 4, 7, 13), "amber_light")
            rectangle(draw, (24, 2, 31, 30), "#8e4f32")
            rectangle(draw, (1, 18, 30, 20), "#d58d39")
            rectangle(draw, (5, 18, 10, 20), "amber")
            rectangle(draw, (2, 28, 29, 31), "rust_dark")
            rectangle(draw, (11, 1, 22, 2), "#d58d39")
        elif index == 11:
            rectangle(draw, (0, 0, 31, 31), "bone")
            rectangle(draw, (3, 2, 28, 8), "paper")
            rectangle(draw, (1, 7, 4, 24), "#a08c6b")
            rectangle(draw, (28, 8, 31, 26), "#a08c6b")
            draw.polygon(((5, 11), (12, 10), (14, 13), (12, 19), (6, 18)), fill=PALETTE["cavity"])
            draw.polygon(((19, 10), (26, 11), (26, 18), (20, 19), (18, 13)), fill=PALETTE["cavity"])
            rectangle(draw, (6, 12, 9, 13), "stone_dark")
            rectangle(draw, (21, 12, 25, 13), "stone_dark")
            draw.polygon(((15, 18), (18, 21), (17, 24), (14, 24)), fill=PALETTE["wood_dark"])
            rectangle(draw, (6, 25, 26, 26), "#9a8869")
            for left in (7, 12, 17, 22):
                rectangle(draw, (left, 27, left + 2, 30), "paper")
                rectangle(draw, (left + 3, 27, left + 3, 31), "#8c7c60")
            line(draw, ((19, 2), (18, 5), (20, 7)), "#9a8869")
        elif index == 12:
            rectangle(draw, (0, 0, 31, 31), "bone")
            line(draw, ((4, 7), (27, 25)), "#998667", 8)
            line(draw, ((5, 6), (28, 24)), "paper", 5)
            line(draw, ((5, 25), (25, 6)), "#998667", 7)
            line(draw, ((5, 24), (25, 5)), "paper", 4)
            for horizontal, vertical in ((4, 6), (27, 24), (5, 24), (25, 5)):
                rectangle(draw, (horizontal - 2, vertical - 2, horizontal + 2, vertical + 2), "paper")
                rectangle(draw, (horizontal - 1, vertical, horizontal + 1, vertical + 1), "bone")
            rectangle(draw, (1, 1, 4, 2), "#a18e6e")
            rectangle(draw, (25, 29, 30, 30), "#a18e6e")
        elif index == 13:
            rectangle(draw, (0, 0, 31, 31), "iron")
            rectangle(draw, (0, 0, 2, 31), "stone_light")
            rectangle(draw, (3, 0, 8, 31), "#9aabae")
            rectangle(draw, (9, 0, 12, 31), "stone_light")
            rectangle(draw, (13, 0, 16, 31), "stone_dark")
            rectangle(draw, (17, 0, 20, 31), "stone_mid")
            rectangle(draw, (28, 0, 31, 31), "#3d4c57")
            rectangle(draw, (23, 22, 29, 25), "rust_dark")
            rectangle(draw, (26, 26, 30, 28), "rust")
            rectangle(draw, (3, 13, 6, 14), "iron")
            rectangle(draw, (0, 23, 2, 24), "iron")
        elif index == 14:
            rectangle(draw, (0, 0, 31, 31), "bone")
            rectangle(draw, (3, 0, 28, 31), "paper")
            rectangle(draw, (3, 1, 4, 29), "#aa9370")
            rectangle(draw, (27, 1, 28, 30), "#aa9370")
            for vertical, right in ((3, 24), (6, 22), (26, 23), (29, 19)):
                rectangle(draw, (8, vertical, right, vertical), "#998363")
            draw.ellipse((8, 10, 25, 24), fill=PALETTE["cloth_dark"])
            draw.ellipse((9, 10, 23, 22), fill=PALETTE["cloth"])
            draw.rectangle((13, 13, 19, 19), outline=PALETTE["cloth_dark"], width=2)
            rectangle(draw, (15, 15, 16, 21), "cloth_dark")
            rectangle(draw, (11, 11, 15, 12), "#ac6470")
        else:
            rectangle(draw, (0, 0, 31, 31), "cloth_dark")
            rectangle(draw, (3, 0, 6, 31), "#6e354c")
            rectangle(draw, (7, 0, 9, 31), "cloth")
            rectangle(draw, (20, 0, 23, 31), "#3f2639")
            rectangle(draw, (24, 0, 26, 31), "#6e354c")
            line(draw, ((1, 29), (30, 29)), "#915064")
            line(draw, ((1, 31), (30, 31)), "#3f2639")
            rectangle(draw, (5, 21, 8, 22), "cloth_dark")
            rectangle(draw, (24, 9, 26, 10), "cloth_dark")
        tiles.append(image)
    return tiles


def make_atlas(tiles):
    atlas = Image.new("RGBA", (128, 128))
    for index, image in enumerate(tiles):
        atlas.paste(image, ((index % 4) * 32, (index // 4) * 32))
    return atlas


def make_tilemap():
    return {
        "schema": "ft-prison-kit-tilemap-1",
        "lane": "furnishings",
        "atlas": "atlas.png",
        "width": 128,
        "height": 128,
        "tile_size": 32,
        "columns": 4,
        "rows": 4,
        "filter": "nearest",
        "alpha": "opaque",
        "tiles": [
            {
                "index": index,
                "name": name,
                "name_zh": chinese,
                "material": material,
                "bounds_px": [(index % 4) * 32, (index // 4) * 32, (index % 4 + 1) * 32, (index // 4 + 1) * 32],
                "uv_java": [(index % 4) * 4, (index // 4) * 4, (index % 4 + 1) * 4, (index // 4 + 1) * 4],
                "glow": False,
            }
            for index, (name, chinese, material) in enumerate(TILE_NAMES)
        ],
    }


def dot(first, second):
    return sum(left * right for left, right in zip(first, second))


def normalized(vector):
    length = math.sqrt(dot(vector, vector))
    return tuple(component / length for component in vector)


def cross(first, second):
    return (
        first[1] * second[2] - first[2] * second[1],
        first[2] * second[0] - first[0] * second[2],
        first[0] * second[1] - first[1] * second[0],
    )


def cuboid_faces(element):
    left, bottom, front = element["from"]
    right, top, back = element["to"]
    return {
        "north": ((0, 0, -1), ((left, top, front), (right, top, front), (right, bottom, front), (left, bottom, front))),
        "south": ((0, 0, 1), ((right, top, back), (left, top, back), (left, bottom, back), (right, bottom, back))),
        "east": ((1, 0, 0), ((right, top, front), (right, top, back), (right, bottom, back), (right, bottom, front))),
        "west": ((-1, 0, 0), ((left, top, back), (left, top, front), (left, bottom, front), (left, bottom, back))),
        "up": ((0, 1, 0), ((left, top, front), (right, top, front), (right, top, back), (left, top, back))),
        "down": ((0, -1, 0), ((left, bottom, back), (right, bottom, back), (right, bottom, front), (left, bottom, front))),
    }


def render_model(model, tiles, direction=(0.9, 1.0, -2.0), size=384, gray=False):
    camera = normalized(direction)
    right = normalized((-camera[2], 0, camera[0])) if abs(camera[0]) + abs(camera[2]) > 0.001 else (1, 0, 0)
    up = cross(right, camera)
    corners = [(horizontal, vertical, depth) for horizontal in (-8, 8) for vertical in (-8, 8) for depth in (-8, 8)]
    projected_width = max(dot(corner, right) for corner in corners) - min(dot(corner, right) for corner in corners)
    projected_height = max(dot(corner, up) for corner in corners) - min(dot(corner, up) for corner in corners)
    scale = size * 0.80 / max(projected_width, projected_height)
    center = (size * 0.50, size * 0.47)
    image = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    pixels = image.load()
    depths = [-float("inf")] * (size * size)
    surface_lighting = {"north": 1.0, "south": 0.96, "east": 0.83, "west": 0.87, "up": 1.08, "down": 0.68}

    def project(vertex):
        centered = tuple(component - 8 for component in vertex)
        return (center[0] + dot(centered, right) * scale, center[1] - dot(centered, up) * scale, dot(centered, camera))

    for element in model["boxes"]:
        for face_name, (normal, vertices) in cuboid_faces(element).items():
            if dot(normal, camera) <= 0.00001:
                continue
            projected = [project(vertex) for vertex in vertices]
            origin = projected[0]
            across = tuple(projected[1][axis] - origin[axis] for axis in range(3))
            down = tuple(projected[3][axis] - origin[axis] for axis in range(3))
            determinant = across[0] * down[1] - across[1] * down[0]
            if abs(determinant) < 0.00001:
                continue
            tile_index = element.get("faces", {}).get(face_name, element["tile"])
            texture = tiles[tile_index].load()
            brightness = surface_lighting[face_name]
            minimum_horizontal = max(0, math.floor(min(vertex[0] for vertex in projected)))
            maximum_horizontal = min(size - 1, math.ceil(max(vertex[0] for vertex in projected)))
            minimum_vertical = max(0, math.floor(min(vertex[1] for vertex in projected)))
            maximum_vertical = min(size - 1, math.ceil(max(vertex[1] for vertex in projected)))
            for vertical in range(minimum_vertical, maximum_vertical + 1):
                for horizontal in range(minimum_horizontal, maximum_horizontal + 1):
                    relative_horizontal = horizontal + 0.5 - origin[0]
                    relative_vertical = vertical + 0.5 - origin[1]
                    texture_horizontal = (relative_horizontal * down[1] - relative_vertical * down[0]) / determinant
                    texture_vertical = (relative_vertical * across[0] - relative_horizontal * across[1]) / determinant
                    if not (0 <= texture_horizontal < 1 and 0 <= texture_vertical < 1):
                        continue
                    depth = origin[2] + texture_horizontal * across[2] + texture_vertical * down[2]
                    pixel_index = vertical * size + horizontal
                    if depth <= depths[pixel_index] + 0.000001:
                        continue
                    depths[pixel_index] = depth
                    source = (157, 164, 166, 255) if gray else texture[min(31, int(texture_horizontal * 32)), min(31, int(texture_vertical * 32))]
                    pixels[horizontal, vertical] = tuple(min(255, round(channel * brightness)) for channel in source[:3]) + (255,)
    return image


def font(size, chinese=False, bold=False):
    candidates = (
        ("C:/Windows/Fonts/msyhbd.ttc" if bold else "C:/Windows/Fonts/msyh.ttc") if chinese else ("C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf"),
        "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
    )
    for candidate in candidates:
        if Path(candidate).exists():
            return ImageFont.truetype(candidate, size=size)
    return ImageFont.load_default(size=size)


def draw_wrapped(draw, text, position, text_font, fill, max_width, line_height):
    start_horizontal, vertical = position
    current = ""
    for character in text:
        candidate = current + character
        if draw.textlength(candidate, font=text_font) > max_width and current:
            draw.text((start_horizontal, vertical), current, font=text_font, fill=fill)
            current = character
            vertical += line_height
        else:
            current = candidate
    if current:
        draw.text((start_horizontal, vertical), current, font=text_font, fill=fill)


FEATURES = (
    ("SEAL / RECESSED BOARDS / FOUR IRON CORNERS", "封条、锁扣与护角是真实层级；凹板不是整块箱壳。"),
    ("SEVEN STRUCTURAL TIERS / TWO PROUD HOOPS", "七段收肩与三段式削角横截面；不是一个贴桶皮的立方体。"),
    ("TWO OPEN SHACKLES / TIMBER FEET / REAR STRAP", "双镣内孔可穿视，刑柱有稳定脚与背带，不拘束实体。"),
    ("SWORD + BEARDED AXE / ACTUAL SUPPORT SADDLES", "剑、垂刃斧独立造型，托板和卡座真实承托，不可取用。"),
    ("TWO OPEN BAYS / BOTTLES + BOUND VOLUMES", "高瓶、窄颈与矮罐区分，书瓶分层；不透明、无酿造功能。"),
    ("WALL ANCHORS / BROAD TORN HEM / PLAIN BACK", "墙锚伸至方块背面，三幅残摆构成不齐轮廓，背面素布。"),
    ("DEEP RECESS / CORBEL ARCH / SUPPORTED BONES", "阶拱围出深凹负空间；头骨与束骨坐在内台，不悬浮。"),
    ("STONE FOOT / OPEN LEDGER / SEPARATE SCROLL", "水平书台承托开放账册和独立卷轴；无阅读或储存交互。"),
)


def contact_sheet(definitions, tiles, renders):
    sheet = Image.new("RGB", (2048, 1490), "#e3e7e4")
    draw = ImageDraw.Draw(sheet)
    draw.text((48, 25), "FT / PRISON FURNISHINGS", font=font(35, bold=True), fill="#233b46")
    draw.text((50, 74), "08 ORIGINAL PROPS  /  JAVA 1.20.1 AUTHORING SOURCE  /  NEAREST PIXEL MATERIALS", font=font(16), fill="#53636a")
    draw.text((1515, 30), "NEUTRAL-LIGHT / OFFLINE", font=font(18, bold=True), fill="#53636a")
    draw.text((1515, 59), "FRONT-RIGHT + BACK-LEFT", font=font(15), fill="#53636a")
    for index, model in enumerate(definitions):
        card_horizontal = 48 + (index % 4) * 493
        card_vertical = 120 + (index // 4) * 551
        draw.rectangle((card_horizontal, card_vertical, card_horizontal + 472, card_vertical + 529), fill="#f0f2ed", outline="#c9d1cd", width=1)
        draw.text((card_horizontal + 20, card_vertical + 16), f"{index + 1:02d}  {model['name_zh']}", font=font(22, chinese=True, bold=True), fill="#263b46")
        draw.text((card_horizontal + 21, card_vertical + 52), model["name_en"], font=font(17), fill="#637179")
        main = renders[model["id"]]["front_right"].resize((316, 316), Image.Resampling.NEAREST)
        back = renders[model["id"]]["back_left"].resize((150, 150), Image.Resampling.NEAREST)
        sheet.paste(main, (card_horizontal - 1, card_vertical + 98), main)
        sheet.paste(back, (card_horizontal + 306, card_vertical + 230), back)
        draw.text((card_horizontal + 328, card_vertical + 391), "BACK", font=font(12, bold=True), fill="#7a878b")
        draw.line((card_horizontal + 20, card_vertical + 425, card_horizontal + 450, card_vertical + 425), fill="#c9d1cd")
        mount = "WALL" if model["mount"] == "wall" else "FLOOR"
        draw.text((card_horizontal + 20, card_vertical + 438), f"{len(model['boxes']):02d} / 28 CUBOIDS   ·   {mount}   ·   LIGHT 0", font=font(15, bold=True), fill="#354f59")
        draw.text((card_horizontal + 20, card_vertical + 466), FEATURES[index][0], font=font(10), fill="#67767b")
        draw_wrapped(draw, FEATURES[index][1], (card_horizontal + 20, card_vertical + 485), font(13, chinese=True), "#53636a", 433, 20)
    draw.text((48, 1266), "ONE OPAQUE 128 x 128 ATLAS", font=font(20, bold=True), fill="#2d4550")
    draw.text((48, 1298), "16 deliberate 32 px tiles", font=font(16), fill="#65757c")
    draw.text((48, 1325), "0 copied source pixels", font=font(16), fill="#65757c")
    draw.text((48, 1352), "0 emissive / 0 animation", font=font(16), fill="#65757c")
    for index, tile in enumerate(tiles):
        horizontal = 365 + index * 100
        enlarged = tile.resize((80, 80), Image.Resampling.NEAREST)
        sheet.paste(enlarged.convert("RGB"), (horizontal, 1281))
        draw.text((horizontal + 29, 1371), f"{index:02d}", font=font(14, bold=True), fill="#53636a")
    draw.line((48, 1416, 2000, 1416), fill="#c0cac5")
    draw.text((48, 1435), "GEOMETRY-AWARE SOFTWARE PREVIEW  /  NOT A BLOCKBENCH OR MINECRAFT SCREENSHOT", font=font(15, bold=True), fill="#53636a")
    draw.text((1511, 1435), "ORIGINAL PIXELS · NO IMAGE API", font=font(14), fill="#65757c")
    return sheet


def gray_sheet(definitions, tiles):
    sheet = Image.new("RGB", (1600, 860), "#e8ebe8")
    draw = ImageDraw.Draw(sheet)
    draw.text((28, 15), "STRUCTURAL SILHOUETTE / MATERIALS DISABLED", font=font(22, bold=True), fill="#263b46")
    for index, model in enumerate(definitions):
        horizontal = 400 * (index % 4)
        vertical = 60 + 395 * (index // 4)
        image = render_model(model, tiles, size=350, gray=True)
        sheet.paste(image, (horizontal + 25, vertical), image)
        draw.text((horizontal + 22, vertical + 350), f"{index + 1:02d} {model['name_en']} / {len(model['boxes'])} boxes", font=font(15), fill="#354f59")
    return sheet


def orthographic_sheet(definitions, renders):
    directions = ("north", "south", "east", "west", "up", "down")
    sheet = Image.new("RGB", (1260, 1660), "#e8ebe8")
    draw = ImageDraw.Draw(sheet)
    draw.text((24, 12), "SIX ORTHOGRAPHIC VIEWS / GEOMETRY + NEAREST MATERIALS", font=font(22, bold=True), fill="#263b46")
    for column, direction in enumerate(directions):
        draw.text((210 + column * 175, 53), direction.upper(), font=font(15, bold=True), fill="#53636a")
    for index, model in enumerate(definitions):
        vertical = 83 + index * 195
        if index % 2 == 0:
            draw.rectangle((15, vertical, 1244, vertical + 190), fill="#f2f3ee")
        draw.text((24, vertical + 43), f"{index + 1:02d} / {len(model['boxes']):02d} BOXES", font=font(15, bold=True), fill="#354f59")
        draw_wrapped(draw, model["name_zh"], (24, vertical + 74), font(18, chinese=True), "#53636a", 145, 25)
        for column, direction in enumerate(directions):
            image = renders[model["id"]][direction].resize((175, 175), Image.Resampling.NEAREST)
            sheet.paste(image, (185 + column * 175, vertical + 5), image)
    return sheet


def intersects(first, second):
    return all(max(first["from"][axis], second["from"][axis]) < min(first["to"][axis], second["to"][axis]) for axis in range(3))


def touches_face(first, second):
    for axis in range(3):
        plane_matches = first["to"][axis] == second["from"][axis] or second["to"][axis] == first["from"][axis]
        overlaps = all(max(first["from"][other], second["from"][other]) < min(first["to"][other], second["to"][other]) for other in range(3) if other != axis)
        if plane_matches and overlaps:
            return True
    return False


def occupied_cells(model):
    cells = {}
    for index, element in enumerate(model["boxes"]):
        for horizontal in range(element["from"][0], element["to"][0]):
            for vertical in range(element["from"][1], element["to"][1]):
                for depth in range(element["from"][2], element["to"][2]):
                    coordinate = (horizontal, vertical, depth)
                    assert coordinate not in cells, f"overlapping volume in {model['id']}: {coordinate}"
                    cells[coordinate] = index
    return cells


def inspect_face_coverage(model, cells):
    offsets = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
    exposed_by_box = Counter()
    face_counts = {"fully_exposed": 0, "partly_exposed": 0, "fully_occluded": 0}
    exposed_unit_faces = 0
    for index, element in enumerate(model["boxes"]):
        for face, (normal, vertices) in cuboid_faces(element).items():
            axis = next(position for position, component in enumerate(normal) if component)
            remaining = [position for position in range(3) if position != axis]
            boundary = element["from"][axis] if normal[axis] == -1 else element["to"][axis] - 1
            total = 0
            exposed = 0
            for first in range(element["from"][remaining[0]], element["to"][remaining[0]]):
                for second in range(element["from"][remaining[1]], element["to"][remaining[1]]):
                    coordinate = [0, 0, 0]
                    coordinate[axis] = boundary
                    coordinate[remaining[0]] = first
                    coordinate[remaining[1]] = second
                    neighbor = tuple(coordinate[position] + offsets[face][position] for position in range(3))
                    total += 1
                    if neighbor not in cells:
                        exposed += 1
            exposed_by_box[index] += exposed
            exposed_unit_faces += exposed
            face_counts["fully_occluded" if exposed == 0 else "fully_exposed" if exposed == total else "partly_exposed"] += 1
    assert len(exposed_by_box) == len(model["boxes"])
    assert all(exposed_by_box[index] > 0 for index in range(len(model["boxes"]))), f"hidden fragment in {model['id']}"
    return {**face_counts, "exposed_unit_face_area": exposed_unit_faces, "every_box_contributes_boundary": True}


def merge_candidates(model):
    candidates = []
    for first, second in combinations(model["boxes"], 2):
        if first["tile"] != second["tile"] or first.get("faces", {}) != second.get("faces", {}):
            continue
        for axis in range(3):
            same_ranges = all(first["from"][other] == second["from"][other] and first["to"][other] == second["to"][other] for other in range(3) if other != axis)
            adjacent = first["to"][axis] == second["from"][axis] or second["to"][axis] == first["from"][axis]
            if same_ranges and adjacent:
                candidates.append([first["name"], second["name"]])
    return candidates


def duplicate_oriented_faces(model):
    duplicates = []
    for first, second in combinations(model["boxes"], 2):
        for axis, positive in ((0, True), (0, False), (1, True), (1, False), (2, True), (2, False)):
            endpoint = "to" if positive else "from"
            same_plane = first[endpoint][axis] == second[endpoint][axis]
            overlaps = all(max(first["from"][other], second["from"][other]) < min(first["to"][other], second["to"][other]) for other in range(3) if other != axis)
            if same_plane and overlaps:
                duplicates.append([first["name"], second["name"], axis, positive])
    return duplicates


def empty_region(cells, start, end):
    return all((horizontal, vertical, depth) not in cells for horizontal in range(start[0], end[0]) for vertical in range(start[1], end[1]) for depth in range(start[2], end[2]))


def check_distinctive_structure(model, cells):
    identifier = model["id"]
    checks = {}
    if identifier == "prison_supply_crate":
        checks["four_iron_corners"] = len([element for element in model["boxes"] if element["name"].startswith("iron corner")]) == 4
        checks["paper_seal_and_lid_strip"] = len([element for element in model["boxes"] if element["tile"] == 14]) == 2
        checks["panels_recessed_from_frame"] = empty_region(cells, (3, 3, 1), (6, 8, 3))
    elif identifier == "prison_ironbound_barrel":
        checks["two_structural_hoops"] = len([element for element in model["boxes"] if element["tile"] == 2]) == 6
        checks["beveled_belly_corner_remains_empty"] = empty_region(cells, (2, 4, 2), (4, 11, 4))
        checks["narrower_standing_chime"] = len({coordinate[0] for coordinate in cells if coordinate[1] == 0}) < len({coordinate[0] for coordinate in cells if coordinate[1] == 5})
    elif identifier == "prison_shackled_post":
        checks["left_ring_real_through_hole"] = empty_region(cells, (2, 6, 4), (4, 8, 6))
        checks["right_ring_real_through_hole"] = empty_region(cells, (12, 6, 4), (14, 8, 6))
        checks["rear_strap_has_volume"] = any(element["name"] == "rear reinforcing strap" for element in model["boxes"])
    elif identifier == "prison_weapon_rack":
        checks["sword_and_axe_distinct_silhouettes"] = all(any(element["name"].startswith(prefix) for element in model["boxes"]) for prefix in ("sword steel", "bearded axe crown", "bearded axe hanging"))
        checks["open_space_between_weapons"] = empty_region(cells, (7, 7, 7), (9, 12, 10))
        checks["two_actual_retaining_saddles"] = len([element for element in model["boxes"] if "retaining saddle" in element["name"]]) == 2
    elif identifier == "prison_alchemy_shelf":
        checks["lower_bay_open"] = empty_region(cells, (7, 3, 8), (8, 8, 13))
        checks["upper_bay_open"] = empty_region(cells, (8, 9, 8), (9, 15, 13))
        checks["two_necks_three_vessels_and_books"] = len([element for element in model["boxes"] if "neck" in element["name"]]) == 2 and any(element["tile"] == 8 for element in model["boxes"])
    elif identifier == "prison_torn_banner":
        checks["three_broad_uneven_torn_lobes"] = len([element for element in model["boxes"] if "torn lobe" in element["name"]]) == 3
        checks["missing_lower_right_corner"] = empty_region(cells, (11, 1, 12), (12, 6, 13))
        checks["receded_central_hem"] = empty_region(cells, (6, 1, 12), (9, 4, 13))
        checks["two_solid_wall_anchors"] = len([element for element in model["boxes"] if element["to"][2] == 16]) == 2
    elif identifier == "prison_ossuary_niche":
        checks["deep_arch_void_is_geometry"] = empty_region(cells, (6, 9, 4), (10, 13, 13))
        checks["recess_closed_by_real_back_wall"] = all((horizontal, vertical, 13) in cells for horizontal in range(4, 12) for vertical in range(2, 14))
        checks["skull_and_jaw_directly_supported"] = all((horizontal, 3, depth) in cells and (horizontal, 4, depth) in cells for horizontal in range(6, 10) for depth in range(9, 12))
    elif identifier == "prison_warden_lectern":
        checks["two_open_page_blocks"] = len([element for element in model["boxes"] if "open ledger page" in element["name"]]) == 2
        checks["rolled_scroll_has_spindles"] = len([element for element in model["boxes"] if element["name"].startswith("scroll ")]) == 2
        checks["horizontal_reading_desk_not_fake_slope"] = any(element["name"] == "horizontal reading desk" and element["from"][1] == 10 and element["to"][1] == 12 for element in model["boxes"])
    assert checks and all(checks.values()), f"distinctive structure failed: {identifier}: {checks}"
    return checks


def validate_definitions(definitions):
    expected = (
        "prison_supply_crate", "prison_ironbound_barrel", "prison_shackled_post", "prison_weapon_rack",
        "prison_alchemy_shelf", "prison_torn_banner", "prison_ossuary_niche", "prison_warden_lectern",
    )
    assert tuple(model["id"] for model in definitions) == expected
    report = []
    used_tiles = set()
    for model in definitions:
        elements = model["boxes"]
        assert 1 <= len(elements) <= 28, f"budget exceeded: {model['id']}"
        assert model["kind"] == "decor" and model["mount"] in ("floor", "wall")
        assert model["light"] == 0 and model["ingredient"].startswith("minecraft:")
        assert len({element["name"] for element in elements}) == len(elements)
        for element in elements:
            assert set(element).issubset({"name", "from", "to", "tile", "faces", "glow"})
            assert len(element["from"]) == len(element["to"]) == 3
            assert all(type(coordinate) is int and 0 <= coordinate <= 16 for coordinate in element["from"] + element["to"])
            assert all(element["from"][axis] < element["to"][axis] for axis in range(3))
            assert set(element.get("faces", {})).issubset(FACES)
            assert not element.get("glow", False)
            for tile in [element["tile"], *element.get("faces", {}).values()]:
                assert type(tile) is int and 0 <= tile <= 15
                used_tiles.add(tile)
        for first, second in combinations(elements, 2):
            assert not intersects(first, second), f"intersecting volumes: {model['id']}: {first['name']} / {second['name']}"
        if model["mount"] == "floor":
            assert min(element["from"][1] for element in elements) == 0
        else:
            assert max(element["to"][2] for element in elements) == 16
        adjacency = {index: set() for index in range(len(elements))}
        for first, second in combinations(range(len(elements)), 2):
            if touches_face(elements[first], elements[second]):
                adjacency[first].add(second)
                adjacency[second].add(first)
        visited = {0}
        queue = deque([0])
        while queue:
            current = queue.popleft()
            for neighbor in adjacency[current] - visited:
                visited.add(neighbor)
                queue.append(neighbor)
        assert len(visited) == len(elements), f"floating or edge-only fragment in {model['id']}: {[elements[index]['name'] for index in range(len(elements)) if index not in visited]}"
        cells = occupied_cells(model)
        coverage = inspect_face_coverage(model, cells)
        candidates = merge_candidates(model)
        duplicates = duplicate_oriented_faces(model)
        assert not duplicates, f"same-normal coplanar duplicate in {model['id']}: {duplicates}"
        assert not candidates, f"unreviewed cuboid merge candidate in {model['id']}: {candidates}"
        report.append({
            "id": model["id"],
            "boxes": len(elements),
            "budget": 28,
            "declared_faces_before_parent_pruning": len(elements) * 6,
            "occupied_unit_cells": len(cells),
            "integer_coordinates": True,
            "within_one_block": True,
            "zero_rotations": True,
            "volume_intersections": 0,
            "same_normal_duplicate_visible_faces": len(duplicates),
            "face_connected_components": 1,
            "mount_contact": True,
            "coverage": coverage,
            "safe_merge_candidates_before_uv_review": candidates,
            "distinctive_structure": check_distinctive_structure(model, cells),
        })
    assert used_tiles == set(range(16))
    return report


def validator_regressions():
    passed = []

    def rejects(label, mutation):
        altered = copy.deepcopy(make_definitions())
        mutation(altered)
        try:
            validate_definitions(altered)
        except AssertionError:
            passed.append(label)
            return
        raise AssertionError(f"validator accepted deliberately invalid fixture: {label}")

    rejects("reject_negative_coordinate", lambda models: models[0]["boxes"][0]["from"].__setitem__(0, -1))
    rejects("reject_fractional_coordinate", lambda models: models[0]["boxes"][0]["from"].__setitem__(0, 1.5))
    rejects("reject_zero_volume", lambda models: models[0]["boxes"][0]["to"].__setitem__(0, 1))
    rejects("reject_invalid_tile", lambda models: models[0]["boxes"][0].__setitem__("tile", 16))
    rejects("reject_nonzero_rotation", lambda models: models[0]["boxes"][0].__setitem__("rotation", [0, 22.5, 0]))
    rejects("reject_unassigned_emissivity", lambda models: models[0]["boxes"][0].__setitem__("glow", True))
    rejects("reject_intersecting_volume", lambda models: models[0]["boxes"].append(box("intersecting iron duplicate", (1, 0, 1), (3, 12, 3), 2)))
    rejects("reject_29_box_budget", lambda models: models[0]["boxes"].extend(box(f"overbudget copy {index}", (1, 0, 1), (3, 12, 3), 2) for index in range(13)))
    rejects("reject_disconnected_fragment", lambda models: models[0]["boxes"].append(box("floating fragment", (0, 15, 0), (1, 16, 1), 2)))
    rejects("reject_filled_shackle_hole", lambda models: models[2]["boxes"].append(box("filled ring opening", (2, 6, 4), (4, 8, 6), 2)))
    rejects("reject_filled_torn_hem", lambda models: models[5]["boxes"].append(box("filled lower banner corner", (11, 1, 12), (12, 6, 13), 15)))
    rejects("reject_filled_ossuary_void", lambda models: models[6]["boxes"].append(box("filled arch opening", (6, 9, 4), (10, 13, 13), 4)))
    return passed


def png_bytes(image):
    buffer = io.BytesIO()
    image.save(buffer, format="PNG", optimize=False, compress_level=9)
    return buffer.getvalue()


def write_json(path, data):
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")


def write_design(definitions, audit):
    total_boxes = sum(item["boxes"] for item in audit)
    total_faces = sum(item["declared_faces_before_parent_pruning"] for item in audit)
    hidden_faces = sum(item["coverage"]["fully_occluded"] for item in audit)
    lines = [
        "# FT Prison Kit — Furnishings / 陈设分线",
        "",
        "原创作者源文件；适用 Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17，命名空间由主聊天集成为 `ftdungeonprops`。",
        "本分线不生成正式 `.bbmodel`、游戏包或运行代码，不触碰既有 FT 工程、存档、共享代码或当前 Blockbench。",
        "",
        "## 六份交付",
        "",
        "- `definitions.json`：按合同顺序给出八款完整结构盒与材质，NORTH / z=0 为正面。",
        "- `atlas.png`：128×128 RGBA，全不透明；4×4 共 16 个 32×32 原创图块。",
        "- `tilemap.json`：每块像素边界、Java 0–16 UV 边界、材质名称与不发光声明。",
        "- `make_assets.py`：只依赖 Python 标准库与 Pillow，离线确定性生成全部交付和自检。",
        "- `contact-sheet.png`：正前右斜视 + 后左斜视；中性亮背景、最近邻采样、实际深度遮挡的软件渲染。",
        "- `DESIGN.md`：本文，包含结构、预算、参考边界与实际检查。",
        "",
        "## 原创风格与参考边界",
        "",
        "实际访问了 Mine Cells 公开分支 `minecells-2.0.0+1.20.1`，只研究材质分工、凉灰监牢、旧木和锈铁的关系。",
        "- 仓库： https://github.com/mim1q/MineCells/tree/minecells-2.0.0%2B1.20.1",
        "- 研究模型：`models/block/brittle_barrel.json`（7 盒，分别使用桶身/盖/箍材质）与 `models/block/small_crate/small_crate_0.json`（1 盒）。",
        "- 内存研究贴图：`prison_bricks.png`、`crate.png`、`brittle_barrel.png`，均为 16×16；只读取尺寸、主色和透明分工，未保存或采样到成品。",
        "- 本分线桶为七层削角轮廓 22 盒、箱为凹板框架 16 盒；未复刻以上模型坐标、UV、PNG、标志或命名。",
        "- 本地图像完全由本生成器的有意像素簇、木纹走向、金属边线与骨纸笔迹绘制；不用随机逐像素噪点、图片生成 API 或外部图片。",
        "- 只读检查了既有首批 FT 资源中的 `dungeon_cracked_tiles`、`blood_skull_shrine` 与 `arena_torch` 及其贴图引用；未发现本批八个同名标识。旧血色祭坛不作为本批蓝灰阶拱骨龛的源模型。",
        "",
        "基色遵循合同：石 `#253744 / #354c59 / #4e6570 / #74858b`，凹面 `#141d27`，旧木 `#493532 / #735044`，",
        "锈铁 `#252b35 / #59626a / #784936 / #b4794e`，暗红布 `#542b42 / #8b4356`，骨纸 `#bcae8b / #e0cfaa`。",
        "青与琥珀仅用于不透明药瓶的釉面高光，不是光源、不承诺透光或 shader 发光。补色只为同材质的邻阶像素簇服务。",
        "",
        "## 结构与成本",
        "",
        "全部是 decor 单格静态方块，整数端点 0..16，零旋转，正体积；不是可操作容器、伤害方块或原版工作站。",
        "7 款落地（最低 y=0），1 款挂墙（两个墙锚达到 z=16）。无悬空零件：每款所有盒通过正面积接触构成一个连通组件。",
        "",
        "| ID | 名称 | 结构盒 / 28 | 声明面，主线裁面前 | 全遮挡面，交主线删除 | 安装 |",
        "|---|---|---:|---:|---:|---|",
    ]
    for model, item in zip(definitions, audit):
        lines.append(f"| `{model['id']}` | {model['name_zh']} | {item['boxes']} | {item['declared_faces_before_parent_pruning']} | {item['coverage']['fully_occluded']} | {model['mount']} |")
    lines.extend((
        "",
        f"合计 **{total_boxes} 个结构盒 / {total_faces} 个声明四边面**，每款最多 22 盒；合同上限为每款 28。",
        f"有 {hidden_faces} 个完整接触内面可由主线统一剔除；相邻部分遮挡面的可见区域必须保留，不能整面误删。",
        "每个保留盒均贡献外边界；没有可直接合并且材质/面覆盖一致的相邻长方体对，没有体积交叉或同法向重叠面。",
        "这不是最终导出四边面数、draw calls 或 FPS 测量，主线局部裁面后可能切分面。碰撞由主线按结构盒并集生成。",
        "",
        "### 八款轮廓决定",
        "",
        "1. **封存补给箱 / 16**：一体内凹木板核，八根框材、四根铁护角；纸封与顶面封条凸出，锁扣正面有厚度。背面有独立上下框、后护角与木纹。不是单一箱体加贴图。",
        "2. **锈箍鼓腹桶 / 22**：七个有实际用途的高度区段：立地桶脚、下桶肩、下铁箍、鼓腹、上铁箍、上桶肩、桶盖；每段三块构成削角横截面，另有木塞。不是逐扫描线雕出圆桶；两圈完整铁箍是实体截面而非与桶身穿插的壳。",
        "3. **双镣刑柱 / 18**：前后稳脚、中心立柱、横梁和后铁带；两枚四边实体方环保留 2×2×2 真孔，短吊件面接触承托。无独立悬浮链粒；无拘束/伤害行为。",
        "4. **狱卒兵器架 / 18**：两脚、两柱、双后梁与承托板；长剑有柄头、握把、护手、窄刃尖，斧有长柄、冠、下垂刃与斧背。两件都有固定卡座，不是把武器画在一块背板上。背面铁带和后梁可见。",
        "5. **旧炼金药书架 / 17**：三层板围出两格真实空腔，后背板与立柱承重；下层窄颈青瓶配书组，上层琥珀长颈瓶、窄册、矮青罐。瓶颈与塞是三维结构；书页/书脊靠图块而非逐书线挤出。",
        "6. **断钥残旗 / 7**：双墙锚、铁杆、一块主要旗面与三幅大残摆；主面 8 格宽、薄 1 格，残摆分别落到 y=2/4/1，右下缺角是真空。原创断钥图案只在正面，后面与边厚为素暗红布。不逐像素堆轮廓，不使用透明贴片。",
        "7. **支拱藏骨龛 / 14**：石基、双墩、后壁、冠石、双阶托和拱心石，围成深 9 格的实际凹腔。头骨、下颌和两束骨都有内台支撑。凹眼属于贴图小细节，深凹龛与支拱属于几何；两者没有混淆。",
        "8. **典狱账卷台 / 15**：石脚、木柱、铁承圈、托座与水平台面；暗红书皮上有左右页块和深书沟，旁边独立卷纸与前后轴头。零旋转合同下选择水平阅读台，不用堆扫描线伪造斜面。背面有铁台边、柱身、卷轴端头。",
        "",
        "## 图集与 UV",
        "",
        "- 一个 128×128 不透明 RGBA 静态图集，16,384 像素 / 原始 RGBA 65,536 字节；无透明层、动画条带、发光图或额外材质。该字节量不是显存实测。",
        "- 16 格均使用且内容各异。第 6 格只给残旗正面，第 11 格只给头骨正面；侧面和背面由普通布/骨图块补足。",
        "- `bounds_px` 为左上含、右下不含；`uv_java` 为 128 atlas 对应的 Java 0..16 范围，行列从零计数。",
        "- `boxes[].tile` 指定默认材质；可选 `faces` 仅覆盖 north/south/east/west/up/down。合同没有每盒单独 UV 裁切字段，离线预览将指定的 32px 图块铺满该盒面；小件密度和游戏最终 UV 由主线统一导出复核。",
        "- 纹理严格最近邻，无滤镜模糊；金属线、木节、瓶体高光、骨纹与纸墨均为有意组合簇，而非噪声散点。",
        "",
        "## 真正运行的检查",
        "",
        "- 八款 ID/数量/类型/安装/配方原料检查；128×128 RGBA PNG 解码及 alpha 全 255。",
        "- 每款 1..28 盒；整数端点、单格边界、正体积、无旋转、无 glow、图块引用 0..15 和合法面名。",
        "- 每对盒的严格 AABB 相交体积测试；16³ 单元占用复核，禁止重复占用。",
        "- 正面积接触图遍历：每款恰好一个连通组件，排除浮件和仅点/边接触。",
        "- 同法向同平面矩形重叠测试；盒边界贡献检查；简单可合并长方体对检查。",
        "- 可重算各盒的完整/部分/全遮挡面统计，不擅自把部分遮挡面整面删除。",
        "- 针对轮廓的断言：两枚镣孔、兵器间空隙、书架两空格、残摆缺口、深骨龛、后壁与骨件承托。",
        "- 12 个故意错误的回归样本，必须拒绝越界、小数、零体积、非法图块、旋转、发光、重复体积、29 盒、浮片、填镣孔、补齐残摆和封骨龛。",
        "- 软件 z-buffer 实际渲染每款正前右/后左斜视与前、后、左、右、上、下六个正交方向，共 64 张。另有八款禁材质灰模总览。",
        "- 重新打开实际写出的 JSON/PNG；与生成器重算的定义、逐像素图集和 tilemap 一致；`--verify-only` 执行相同静态检查与错误样本回归。",
        "",
        "证据在 `work/audit.json`、`work/gray-sheet.png`、`work/orthographic-sheet.png`、`work/renders/<id>/`；图像均为离线软件渲染，不冒充游戏或编辑器截图。",
        "",
        "## 重建",
        "",
        "```powershell",
        "& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' -B ./make_assets.py",
        "& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' -B ./make_assets.py --verify-only",
        "```",
        "",
        "生成器只写自身所在分线和其 `work/`。输入无联网依赖、无随机数。当前使用 Pillow；预览标题字体优先 Windows 微软雅黑/Arial，其他系统的字体替代只影响接触表文字。",
        "同一环境重复运行后，用 `work/audit.json` 记录的六份交付 SHA-256 对比即可核验确定性；主线可独立重算，不依赖本报告结论。",
        "",
        "## 验收边界 / 尚未运行",
        "",
        "**没有启动或修改 Blockbench，没有启动 Minecraft 客户端/服务器，没有安装任何游戏包，没有实测 FPS、光照或最终物品 Display。**",
        "本分线也未运行最终 `.bbmodel` / 游戏模型审计，因为正式导出、资源解析、朝向状态、实际并集碰撞与父级局部裁面由主聊天统一完成。",
        "墙挂/落地只是元数据与实际接触几何；不承诺支撑检测。所有陈设均无库存、无 ticking、无 block entity、无水涝状态、无战斗/生物群系/难度耦合。",
    ))
    (ROOT / "DESIGN.md").write_text("\n".join(lines) + "\n", encoding="utf-8", newline="\n")


def verify_saved(definitions, tiles):
    saved_definitions = json.loads((ROOT / "definitions.json").read_text(encoding="utf-8"))
    assert saved_definitions == definitions
    assert json.loads((ROOT / "tilemap.json").read_text(encoding="utf-8")) == make_tilemap()
    atlas = Image.open(ROOT / "atlas.png")
    atlas.load()
    assert atlas.format == "PNG" and atlas.mode == "RGBA" and atlas.size == (128, 128)
    assert atlas.getchannel("A").getextrema() == (255, 255)
    assert atlas.tobytes() == make_atlas(tiles).tobytes()
    fingerprints = []
    for index in range(16):
        tile = atlas.crop(((index % 4) * 32, (index // 4) * 32, (index % 4 + 1) * 32, (index // 4 + 1) * 32))
        assert len(tile.getcolors(1024)) >= 3
        fingerprints.append(hashlib.sha256(tile.tobytes()).hexdigest())
    assert len(set(fingerprints)) == 16
    contact = Image.open(ROOT / "contact-sheet.png")
    contact.load()
    assert contact.size == (2048, 1490) and contact.format == "PNG"
    deliverable_names = ("definitions.json", "atlas.png", "tilemap.json", "make_assets.py", "DESIGN.md", "contact-sheet.png")
    assert set(path.name for path in ROOT.iterdir() if path.is_file()) == set(deliverable_names)
    return {
        "actual_json_reopened_and_matches_generator": True,
        "actual_atlas_reopened_and_pixel_exact": True,
        "atlas_size": [128, 128],
        "atlas_mode": "RGBA",
        "alpha_extrema": [255, 255],
        "unique_used_tiles": 16,
        "texture_pixels": 16384,
        "rgba_uncompressed_bytes_not_vram": 65536,
        "contact_sheet_size": [2048, 1490],
        "deliverable_sha256": {name: hashlib.sha256((ROOT / name).read_bytes()).hexdigest() for name in deliverable_names},
    }


def main():
    if not __debug__:
        raise RuntimeError("Do not run this auditable generator with Python -O.")
    parser = argparse.ArgumentParser(description="Generate and validate original prison-kit furnishings without touching shared editors or projects.")
    parser.add_argument("--verify-only", action="store_true", help="Reopen existing deliverables, revalidate geometry and run rejection regressions without regenerating deliverables.")
    arguments = parser.parse_args()
    definitions = make_definitions()
    tiles = make_tiles()
    audit = validate_definitions(definitions)
    regressions = validator_regressions()
    WORK.mkdir(parents=True, exist_ok=True)
    views = {
        "front_right": (0.9, 1.0, -2.0),
        "back_left": (-0.9, 1.0, 2.0),
        "north": (0, 0, -1),
        "south": (0, 0, 1),
        "east": (1, 0, 0),
        "west": (-1, 0, 0),
        "up": (0, 1, 0),
        "down": (0, -1, 0),
    }
    if not arguments.verify_only:
        write_json(ROOT / "definitions.json", definitions)
        write_json(ROOT / "tilemap.json", make_tilemap())
        (ROOT / "atlas.png").write_bytes(png_bytes(make_atlas(tiles)))
        renders = {}
        for model in definitions:
            destination = WORK / "renders" / model["id"]
            destination.mkdir(parents=True, exist_ok=True)
            renders[model["id"]] = {}
            for name, direction in views.items():
                image = render_model(model, tiles, direction=direction)
                renders[model["id"]][name] = image
                (destination / f"{name}.png").write_bytes(png_bytes(image))
        (ROOT / "contact-sheet.png").write_bytes(png_bytes(contact_sheet(definitions, tiles, renders)))
        (WORK / "gray-sheet.png").write_bytes(png_bytes(gray_sheet(definitions, tiles)))
        (WORK / "orthographic-sheet.png").write_bytes(png_bytes(orthographic_sheet(definitions, renders)))
        write_design(definitions, audit)
    files = verify_saved(definitions, tiles)
    render_files = list((WORK / "renders").glob("*/*.png"))
    assert len(render_files) == 64
    assert {path.relative_to(WORK / "renders").as_posix() for path in render_files} == {f"{model['id']}/{view}.png" for model in definitions for view in views}
    for path in render_files:
        with Image.open(path) as image:
            image.load()
            assert image.size == (384, 384) and image.mode == "RGBA"
            assert image.getchannel("A").getbbox() is not None
    report = {
        "status": "PASS",
        "scope": "furnishings authoring definitions, actual atlas, offline previews; not final runtime export",
        "exactly_eight_models": True,
        "total_boxes": sum(item["boxes"] for item in audit),
        "maximum_boxes_per_model": max(item["boxes"] for item in audit),
        "geometry": audit,
        "rejection_regressions_passed": regressions,
        "rejection_regression_count": len(regressions),
        "files": files,
        "offline_view_directions": list(views),
        "offline_render_pngs_reopened": len(render_files),
        "gray_sheet": "work/gray-sheet.png",
        "orthographic_sheet": "work/orthographic-sheet.png",
        "blockbench_used_or_modified": False,
        "minecraft_client_or_server_started": False,
        "existing_ft_project_modified": False,
        "reference_png_copied_into_outputs": False,
        "image_generation_api_used": False,
        "limitations": [
            "No final .bbmodel or runtime model export in this lane; parent performs integration and export audit.",
            "No Blockbench or Minecraft visual test, collision gameplay test, item Display test, FPS or VRAM measurement.",
            "Software UVs map each declared tile across its box face; final exported UV and partial-face pruning must be reviewed by the parent.",
            "Mount metadata does not implement support enforcement; all objects are decorative and non-interactive.",
        ],
    }
    write_json(WORK / "audit.json", report)
    print(json.dumps({"status": report["status"], "models": len(definitions), "boxes": report["total_boxes"], "largest_model": report["maximum_boxes_per_model"], "rejection_regressions": len(regressions), "offline_views": len(render_files), "root": str(ROOT)}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
