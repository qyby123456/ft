from __future__ import annotations

import argparse
import hashlib
import itertools
import json
import math
from collections import deque
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw, ImageFont
import PIL


ALLOWED_ROOT = Path(__file__).resolve().parent
MODEL_IDS = (
    "prison_supply_crate", "prison_ironbound_barrel", "prison_shackled_post",
    "prison_weapon_rack", "prison_alchemy_shelf", "prison_ossuary_niche",
    "prison_warden_lectern",
)
MODEL_NUMBERS = (9, 10, 11, 12, 13, 15, 16)
MODEL_NAMES = (
    ("封存补给箱", "Sealed Supply Crate", "minecraft:barrel"),
    ("锈箍鼓腹桶", "Ironbound Stave Barrel", "minecraft:barrel"),
    ("双镣刑柱", "Twin Shackle Post", "minecraft:chain"),
    ("狱卒兵器架", "Jailer Weapon Rack", "minecraft:iron_ingot"),
    ("旧炼金药书架", "Apothecary Book Shelf", "minecraft:glass_bottle"),
    ("支拱藏骨龛", "Corbelled Ossuary Niche", "minecraft:bone_block"),
    ("典狱账卷台", "Warden Ledger Lectern", "minecraft:book"),
)
TILES = (
    ("recessed_oak_boards", "凹入橡木板", "wood"),
    ("hewn_structural_oak", "刨削木梁", "wood"),
    ("peened_prison_iron", "锤锻箍铁", "iron"),
    ("restrained_lock_brass", "暗黄铜锁件", "brass"),
    ("crosscut_lid_and_cork", "横纹箱桶盖与软木", "wood"),
    ("curved_barrel_staves", "弧腹桶板", "wood"),
    ("carved_cold_slate", "雕凿冷灰石", "stone"),
    ("aged_ivory_bone", "旧象牙骨质", "bone"),
    ("honed_blade_steel", "磨亮刃钢", "steel"),
    ("oxblood_bound_leather", "暗红皮革装帧", "leather"),
    ("opaque_cyan_phial", "不透明青釉药瓶", "glass"),
    ("opaque_amber_phial", "不透明琥珀药瓶", "glass"),
    ("layered_old_paper", "旧纸与书页断面", "paper"),
    ("ruled_open_ledger", "展开账页与中缝", "paper"),
    ("deep_soot_slate", "凹龛深色背石", "stone"),
    ("carved_skull_features", "颅骨眼窝与齿列", "bone"),
)
FACE_DIRECTIONS = {
    "west": (0, -1), "east": (0, 1), "down": (1, -1),
    "up": (1, 1), "north": (2, -1), "south": (2, 1),
}
VIEW_CAMERAS = {
    "front_iso": (-0.66, 0.50, -0.88),
    "rear_iso": (0.70, 0.46, 0.88),
    "front": (0.0, 0.0, -1.0),
    "right": (1.0, 0.0, 0.0),
    "top": (0.0, 1.0, 0.0),
}
VISUAL_REVIEW = {
    "status": "pass_with_stated_runtime_limitations",
    "images": ["contact-sheet.png", "work/clay-sheet.png", "work/atlas-guide.png",
               "work/prison_ironbound_barrel/review-strip.png", "work/prison_ossuary_niche/review-strip.png"],
    "all_seven_five_view_rows_and_64px_icons_reviewed": True,
    "notes": [
        "Crate reads as a closed framed chest; front recess, lid straps, lock and rear hinges remain joined.",
        "Final barrel uses two corner steps per quadrant, 14-unit belly versus 10-unit end shoulders, and 28 cuboids. It is a stepped polygonal barrel, not a smooth cylinder.",
        "Cuff centers remain visibly open in textured and clay views; forward anchors keep the holes clear of the mast.",
        "Sword crossguard/blade and asymmetric halberd head remain distinguishable at the 64px icon scale.",
        "Shelf shows three necked corked bottles, stacked and standing books, two levels and useful rear retaining rails.",
        "Ossuary has a narrowing supporting arch, deep recess, carved skull face and physically wider bone joints.",
        "Ledger lectern has a slender column, thin writing top, two raised outer leaf sections and a lower central binding.",
        "Views are offline previews. Editor/game displays, collision and runtime visual/performance checks remain for parent QA.",
    ],
}


def box(name, lower, upper, tile, **faces):
    result = {"name": name, "from": list(lower), "to": list(upper), "tile": tile}
    if faces:
        result["faces"] = faces
    return result


def crate_boxes():
    parts = [
        box("continuous ground sill", (1, 0, 2), (15, 1, 14), 1, up=4, down=4),
        box("closed recessed boarded body", (2, 1, 3), (14, 10, 13), 0, up=4, down=4),
        box("closed overhanging timber lid", (1, 10, 2), (15, 12, 14), 1, up=4),
    ]
    for label, front_z, back_z in (("front", 2, 3), ("back", 13, 14)):
        parts.extend([
            box(f"{label} lower frame rail", (1, 1, front_z), (15, 3, back_z), 1),
            box(f"{label} upper frame rail", (1, 8, front_z), (15, 10, back_z), 1),
            box(f"{label} left frame stile", (1, 3, front_z), (3, 8, back_z), 0),
            box(f"{label} right frame stile", (13, 3, front_z), (15, 8, back_z), 0),
        ])
    for label, left_x, right_x in (("left", 1, 2), ("right", 14, 15)):
        parts.extend([
            box(f"{label} bottom return rail", (left_x, 1, 3), (right_x, 3, 13), 1),
            box(f"{label} top return rail", (left_x, 8, 3), (right_x, 10, 13), 1),
        ])
    parts.extend([
        box("left fitted lid strap", (3, 12, 2), (5, 13, 14), 2),
        box("right fitted lid strap", (11, 12, 2), (13, 13, 14), 2),
        box("hanging iron hasp plate", (7, 6, 1), (10, 10, 2), 2),
        box("solid brass padlock body", (7, 5, 0), (10, 8, 1), 3),
        box("left rear lid hinge", (4, 9, 14), (6, 12, 15), 2),
        box("right rear lid hinge", (10, 9, 14), (12, 12, 15), 2),
    ])
    return parts


def barrel_course(name, bottom, top, minimum, maximum, corner, tile, fine_corners=False):
    if fine_corners:
        return [
            box(f"{name} central stave partition", (5, bottom, 1), (11, top, 15), tile, up=4 if tile == 5 else tile),
            box(f"{name} left intermediate curve", (3, bottom, 3), (5, top, 13), tile),
            box(f"{name} right intermediate curve", (11, bottom, 3), (13, top, 13), tile),
            box(f"{name} left narrow tangent", (1, bottom, 5), (3, top, 11), tile),
            box(f"{name} right narrow tangent", (13, bottom, 5), (15, top, 11), tile),
        ]
    return [
        box(f"{name} central partition", (minimum + corner, bottom, minimum),
            (maximum - corner, top, maximum), tile, up=4 if tile == 5 else tile),
        box(f"{name} left rounded partition", (minimum, bottom, minimum + corner),
            (minimum + corner, top, maximum - corner), tile),
        box(f"{name} right rounded partition", (maximum - corner, bottom, minimum + corner),
            (maximum, top, maximum - corner), tile),
    ]


def barrel_boxes():
    parts = []
    courses = (
        ("narrow lower stave shoulder", 0, 3, 3, 13, 2, 5),
        ("lower finely fitted polygonal hoop", 3, 4, 1, 15, 3, 2, True),
        ("wide finely curved stave belly", 4, 10, 1, 15, 3, 5, True),
        ("upper finely fitted polygonal hoop", 10, 11, 1, 15, 3, 2, True),
        ("upper tapered stave shoulder", 11, 13, 3, 13, 2, 5),
        ("recessed closed plank lid", 13, 14, 3, 13, 2, 4),
    )
    for course in courses:
        parts.extend(barrel_course(*course))
    parts.extend([
        box("front raised mouth rim", (5, 14, 3), (11, 15, 5), 2),
        box("back raised mouth rim", (5, 14, 11), (11, 15, 13), 2),
        box("left raised mouth rim", (3, 14, 5), (5, 15, 11), 2),
        box("right raised mouth rim", (11, 14, 5), (13, 15, 11), 2),
    ])
    return parts


def shackle_boxes():
    parts = [
        box("left ground runner", (2, 0, 6), (5, 1, 15), 1, up=4),
        box("right ground runner", (11, 0, 6), (14, 1, 15), 1, up=4),
        box("wide mortised foot sill", (3, 1, 8), (13, 2, 14), 1, up=4),
        box("tall restraint mast", (6, 2, 11), (10, 15, 14), 0),
        box("forward load bearing crossbeam", (1, 12, 9), (15, 14, 11), 1, up=4),
        box("mast iron cap", (6, 15, 11), (10, 16, 14), 2),
        box("front mast reinforcement", (6, 3, 10), (10, 6, 11), 2),
        box("rear fastening plate", (6, 9, 14), (10, 13, 15), 2),
    ]
    for label, left_x, chain_x in (("left", 1, 3), ("right", 10, 12)):
        parts.extend([
            box(f"{label} cuff outer upright", (left_x, 4, 6), (left_x + 1, 9, 8), 2),
            box(f"{label} cuff inner upright", (left_x + 4, 4, 6), (left_x + 5, 9, 8), 2),
            box(f"{label} cuff upper bridge", (left_x + 1, 8, 6), (left_x + 4, 9, 8), 2),
            box(f"{label} cuff lower bridge", (left_x + 1, 4, 6), (left_x + 4, 5, 8), 2),
            box(f"{label} suspended chain stem", (chain_x, 9, 7), (chain_x + 1, 11, 8), 2),
            box(f"{label} forward anchor arm", (chain_x, 11, 7), (chain_x + 1, 12, 11), 2),
        ])
    return parts


def rack_boxes():
    return [
        box("left long ground foot", (1, 0, 5), (4, 1, 15), 1, up=4),
        box("right long ground foot", (12, 0, 5), (15, 1, 15), 1, up=4),
        box("continuous base sill", (1, 1, 6), (15, 2, 13), 1, up=4),
        box("left rack upright", (1, 2, 11), (3, 16, 13), 0, up=4),
        box("right rack upright", (13, 2, 11), (15, 16, 13), 0, up=4),
        box("lower rear stretcher", (3, 2, 11), (13, 4, 13), 1),
        box("upper weapon carrying rail", (3, 10, 11), (13, 12, 13), 1),
        box("forward weapon resting tray", (3, 2, 6), (13, 3, 11), 1, up=4),
        box("upper front return beam", (1, 15, 10), (15, 16, 11), 1, up=4),
        box("sword brass pommel", (4, 3, 7), (7, 4, 9), 3),
        box("sword leather grip", (5, 4, 7), (6, 7, 9), 9),
        box("sword wide crossguard", (3, 7, 7), (8, 8, 9), 3),
        box("sword broad fullered blade", (4, 8, 7), (7, 14, 8), 8),
        box("sword narrowed terminal point", (5, 14, 7), (6, 15, 8), 8),
        box("halberd long timber shaft", (10, 3, 8), (11, 12, 10), 0),
        box("halberd metal socket", (10, 12, 8), (11, 15, 10), 2),
        box("halberd vertical spear point", (10, 15, 8), (11, 16, 9), 8),
        box("halberd broad axe head", (11, 12, 8), (13, 15, 9), 8),
        box("halberd hooked lower toe", (12, 11, 8), (13, 12, 9), 8),
        box("halberd rear counterpick", (9, 13, 8), (10, 14, 9), 8),
        box("sword rail support shoe", (4, 10, 8), (7, 11, 11), 2),
        box("halberd rail support shoe", (10, 10, 10), (12, 11, 11), 2),
    ]


def shelf_boxes():
    return [
        box("deep ground shelf plinth", (1, 0, 5), (15, 2, 14), 1, up=4),
        box("left full depth stile", (1, 2, 6), (3, 15, 14), 0),
        box("right full depth stile", (13, 2, 6), (15, 15, 14), 0),
        box("projecting intermediate shelf", (3, 7, 5), (13, 8, 14), 1, up=4),
        box("deep overhanging top cornice", (1, 15, 5), (15, 16, 14), 1, up=4),
        box("lower open back retaining rail", (3, 3, 13), (13, 5, 14), 0),
        box("upper open back retaining rail", (3, 10, 13), (13, 12, 14), 0),
        box("lower tall bound folio", (3, 2, 7), (5, 6, 11), 9, east=12, up=12),
        box("lower parchment bound volume", (5, 2, 8), (7, 5, 12), 12, south=9),
        box("lower slim oxblood volume", (7, 2, 8), (8, 6, 12), 9, east=12, up=12),
        box("front horizontal closed book", (4, 2, 5), (9, 3, 7), 9, north=12, east=12),
        box("front horizontal parchment book", (4, 3, 5), (8, 4, 7), 12, up=9),
        box("lower amber medicine body", (10, 2, 7), (13, 4, 10), 11),
        box("lower amber narrow neck", (11, 4, 8), (12, 5, 9), 11),
        box("lower amber cork", (11, 5, 8), (12, 6, 9), 4),
        box("upper cyan medicine body", (4, 8, 7), (7, 11, 10), 10),
        box("upper cyan long narrow neck", (5, 11, 8), (6, 13, 9), 10),
        box("upper cyan cork", (5, 13, 8), (6, 14, 9), 4),
        box("upper amber medicine body", (10, 8, 8), (12, 10, 10), 11),
        box("upper amber narrow neck", (10, 10, 8), (11, 12, 9), 11),
        box("upper amber cork", (10, 12, 8), (11, 13, 9), 4),
        box("upper tall stitched folio", (8, 8, 9), (9, 12, 12), 9, up=12),
        box("upper short parchment volume", (9, 8, 10), (10, 11, 12), 12, south=9),
    ]


def niche_boxes():
    return [
        box("wide carved ground plinth", (0, 0, 4), (16, 2, 16), 6),
        box("left deep load bearing pier", (1, 2, 6), (4, 10, 16), 6),
        box("right deep load bearing pier", (12, 2, 6), (15, 10, 16), 6),
        box("left projecting arch springer", (1, 10, 5), (5, 12, 16), 6, down=14),
        box("right projecting arch springer", (11, 10, 5), (15, 12, 16), 6, down=14),
        box("left inward corbel arch tier", (2, 12, 5), (6, 14, 16), 6, down=14),
        box("right inward corbel arch tier", (10, 12, 5), (14, 14, 16), 6, down=14),
        box("closed load bearing arch crown", (4, 14, 6), (12, 16, 16), 6, down=14),
        box("deep recessed lower backing", (4, 2, 14), (12, 10, 16), 14),
        box("deep recessed shoulder backing", (5, 10, 14), (11, 12, 16), 14),
        box("deep recessed apex backing", (6, 12, 14), (10, 14, 16), 14),
        box("set back ossuary shelf", (4, 2, 8), (12, 3, 14), 6),
        box("central carved skull cranium", (6, 4, 9), (10, 8, 12), 7, north=15),
        box("narrow skull jaw", (7, 3, 9), (9, 4, 12), 7, north=15),
        box("rounded skull crown", (7, 8, 9), (9, 9, 12), 7),
        box("left resting long bone", (4, 3, 10), (6, 4, 12), 7),
        box("right resting long bone", (10, 3, 9), (12, 4, 11), 7),
        box("foreground femur shaft", (5, 2, 5), (11, 3, 7), 7),
        box("foreground femur left joint", (4, 2, 5), (5, 4, 7), 7),
        box("foreground femur right joint", (11, 2, 5), (12, 4, 7), 7),
        box("left stacked bone end", (4, 4, 11), (5, 6, 12), 7),
        box("right stacked bone end", (11, 4, 10), (12, 6, 11), 7),
        box("left projecting pier foot", (0, 2, 5), (4, 3, 6), 6),
        box("right projecting pier foot", (12, 2, 5), (16, 3, 6), 6),
        box("forward crown keystone", (7, 14, 5), (9, 16, 6), 6),
    ]


def lectern_boxes():
    return [
        box("broad stable slate foot", (2, 0, 3), (14, 2, 14), 6),
        box("stepped timber pedestal", (5, 2, 6), (11, 3, 12), 1, up=4),
        box("column lower collar", (6, 3, 7), (10, 4, 12), 1, up=4),
        box("slender ledger support column", (7, 4, 8), (9, 9, 11), 0),
        box("wide underside desk capital", (5, 9, 6), (11, 10, 12), 1),
        box("thin deep writing desk", (1, 10, 3), (15, 11, 14), 1, up=4),
        box("forward raised retaining lip", (1, 11, 3), (15, 12, 4), 1),
        box("rear raised book stop", (2, 11, 12), (14, 12, 14), 1),
        box("open ledger oxblood cover", (3, 11, 4), (13, 12, 12), 9),
        box("open ledger recessed central spine", (7, 12, 4), (9, 13, 12), 9, up=13),
        box("left ledger inner page block", (3, 12, 4), (7, 13, 12), 12, up=13),
        box("right ledger inner page block", (9, 12, 4), (13, 13, 12), 12, up=13),
        box("left ledger lifted outer leaves", (3, 13, 4), (5, 14, 12), 12, up=13),
        box("right ledger lifted outer leaves", (11, 13, 4), (13, 14, 12), 12, up=13),
        box("desk side inkpot body", (13, 11, 8), (15, 13, 10), 2),
        box("desk side inkpot brass mouth", (13, 13, 8), (14, 14, 9), 3),
    ]


def definitions():
    makers = (crate_boxes, barrel_boxes, shackle_boxes, rack_boxes, shelf_boxes, niche_boxes, lectern_boxes)
    descriptions = (
        "封闭凹板箱体、包边、整块箱盖与黄铜锁扣；纯装饰，无物品栏。",
        "细阶折圆鼓腹、窄上下收口、贴合铁箍与下沉封闭桶盖；纯装饰，无物品栏。",
        "高立刑柱、承重横梁和两只真实贯通空心铁镣；纯装饰，不束缚玩家。",
        "落地支撑架承托带护手的宽刃剑及钩斧长柄戟；纯装饰，不能取下兵器。",
        "双层深药架、三只收颈塞口药瓶和分层账册；纯装饰，不酿造。",
        "逐层内挑的支拱、深背板、颅骨与长骨组成的真实凹龛；纯装饰。",
        "宽稳底座、纤细承柱、薄书桌及展开的分层双页账簿；纯装饰，无界面。",
    )
    result = []
    for model_id, names, maker, description in zip(MODEL_IDS, MODEL_NAMES, makers, descriptions):
        result.append({
            "id": model_id, "name_zh": names[0], "name_en": names[1],
            "kind": "decor", "mount": "floor", "light": 0,
            "ingredient": names[2], "boxes": maker(), "description_zh": description,
        })
    return result


def paint_tile(index):
    bases = (
        "#735044", "#68483d", "#414b54", "#8d6b3c", "#88624b", "#805b44",
        "#4e6570", "#bcae8b", "#74858b", "#542b42", "#247f8b", "#bd682d",
        "#c9ba96", "#e0cfaa", "#253744", "#bcae8b",
    )
    texture = Image.new("RGBA", (32, 32), bases[index])
    drawing = ImageDraw.Draw(texture)
    if index in (0, 5):
        for column, width, tone in ((0, 7, "#735044"), (8, 7, "#88604c"), (16, 7, "#785340"), (24, 8, "#8e644b")):
            drawing.rectangle((column, 0, column + width - 1, 31), fill=tone)
            drawing.line((column, 0, column, 31), fill="#493532", width=1)
            drawing.line((column + 1, 1, column + 1, 30), fill="#a07857", width=1)
        for start_x, start_y, length in ((4, 3, 9), (11, 18, 10), (20, 5, 7), (27, 14, 12), (13, 1, 8)):
            drawing.line((start_x, start_y, start_x, start_y + length), fill="#5e4236", width=1)
            drawing.line((start_x + 1, start_y + 2, start_x + 1, start_y + length - 1), fill="#946d50", width=1)
        drawing.rectangle((17, 21, 20, 23), fill="#5e4236")
        drawing.rectangle((18, 22, 20, 22), fill="#493532")
        if index == 5:
            drawing.line((5, 0, 5, 31), fill="#a07857")
            drawing.line((27, 0, 27, 31), fill="#5e4236")
            drawing.rectangle((0, 30, 31, 31), fill="#414b54")
            drawing.line((0, 30, 31, 30), fill="#74858b")
    elif index == 1:
        for row in (3, 12, 23):
            drawing.rectangle((1, row, 29, row + 1), fill="#795440")
            drawing.line((3, row + 2, 22, row + 2), fill="#493532")
        drawing.rectangle((0, 0, 31, 1), fill="#a07857")
        drawing.rectangle((0, 30, 31, 31), fill="#493532")
        for start_x, start_y, length in ((5, 6, 9), (16, 18, 10), (1, 27, 12)):
            drawing.line((start_x, start_y, start_x + length, start_y), fill="#936b50")
    elif index == 2:
        drawing.rectangle((0, 0, 31, 1), fill="#74858b")
        drawing.rectangle((0, 30, 31, 31), fill="#252b35")
        for row in (7, 23):
            drawing.line((0, row, 31, row), fill="#59626a")
        for center_x, center_y in ((5, 7), (15, 7), (25, 7), (5, 23), (15, 23), (25, 23)):
            drawing.rectangle((center_x - 1, center_y - 1, center_x + 1, center_y + 1), fill="#252b35")
            drawing.point((center_x - 1, center_y - 1), fill="#95a1a4")
        for rectangle in ((1, 13, 4, 14), (23, 27, 27, 28), (17, 2, 19, 3)):
            drawing.rectangle(rectangle, fill="#784936")
            drawing.point((rectangle[0], rectangle[1]), fill="#b4794e")
    elif index == 3:
        drawing.rectangle((1, 1, 30, 30), outline="#c3a065", width=2)
        drawing.rectangle((4, 5, 9, 6), fill="#b18b50")
        drawing.rectangle((22, 24, 27, 25), fill="#705337")
        drawing.rectangle((14, 17, 16, 19), fill="#252b35")
        drawing.rectangle((15, 19, 15, 22), fill="#252b35")
        drawing.line((13, 16, 17, 16), fill="#e0cfaa")
    elif index == 4:
        for row in (7, 15, 23):
            drawing.rectangle((0, row, 31, row), fill="#493532")
            drawing.line((0, row + 1, 31, row + 1), fill="#ad815b")
        for start_x, start_y, end_x in ((2, 4, 11), (16, 11, 28), (5, 20, 20), (21, 28, 29)):
            drawing.line((start_x, start_y, end_x, start_y), fill="#6c4c3b")
        drawing.rectangle((15, 13, 18, 17), fill="#6c4c3b")
        drawing.rectangle((16, 14, 17, 16), fill="#b18b50")
    elif index in (6, 14):
        light, middle, dark = (("#74858b", "#4e6570", "#354c59") if index == 6 else ("#354c59", "#253744", "#1b2934"))
        for row, divisions in ((0, (10, 24)), (11, (5, 19)), (23, (12, 27))):
            drawing.line((0, row, 31, row), fill=dark)
            drawing.line((0, row + 1, 31, row + 1), fill=light)
            for column in divisions:
                drawing.line((column, row + 1, column, min(31, row + 10)), fill=dark)
        for rectangle in ((2, 5, 7, 6), (17, 15, 22, 16), (4, 27, 8, 28)):
            drawing.rectangle(rectangle, fill=light)
        drawing.line((25, 4, 28, 4, 28, 7, 30, 7), fill=dark, width=1)
        if index == 6:
            drawing.rectangle((10, 19, 13, 20), fill="#735044")
            drawing.rectangle((11, 19, 12, 19), fill="#b4794e")
    elif index in (7, 15):
        drawing.rectangle((0, 0, 31, 3), fill="#e0cfaa")
        drawing.rectangle((1, 27, 30, 31), fill="#9d8e72")
        for start_x, start_y, length in ((3, 6, 8), (21, 8, 6), (5, 17, 7)):
            drawing.line((start_x, start_y, start_x + length, start_y), fill="#d7c69e")
        drawing.line((25, 14, 27, 16, 27, 19, 29, 20), fill="#8b7c64")
        if index == 15:
            drawing.rectangle((13, 17, 15, 20), fill="#493f36")
            drawing.rectangle((18, 17, 20, 20), fill="#493f36")
            drawing.rectangle((14, 18, 15, 20), fill="#141d27")
            drawing.rectangle((18, 18, 19, 20), fill="#141d27")
            drawing.rectangle((16, 21, 17, 23), fill="#493f36")
            drawing.line((14, 25, 17, 25), fill="#493f36")
            drawing.line((15, 24, 15, 26), fill="#6e604e")
            drawing.line((17, 24, 17, 26), fill="#6e604e")
    elif index == 8:
        for left, right, tone in ((0, 6, "#59626a"), (7, 12, "#95a1a4"), (13, 17, "#59626a"), (18, 25, "#aeb9b9"), (26, 31, "#74858b")):
            drawing.rectangle((left, 0, right, 31), fill=tone)
        drawing.line((19, 0, 19, 31), fill="#e0e5dc")
        drawing.rectangle((10, 6, 12, 8), fill="#74858b")
        drawing.rectangle((23, 22, 25, 23), fill="#59626a")
    elif index == 9:
        drawing.rectangle((0, 0, 31, 2), fill="#8b4356")
        drawing.rectangle((0, 29, 31, 31), fill="#351f2c")
        for row in (6, 19, 25):
            drawing.rectangle((1, row, 30, row), fill="#9e6d50")
            drawing.line((2, row + 1, 28, row + 1), fill="#351f2c")
        drawing.rectangle((8, 10, 13, 14), outline="#bcae8b")
        drawing.rectangle((22, 11, 26, 13), fill="#6b344a")
    elif index in (10, 11):
        highlight, light, middle, dark = (("#b8f2de", "#50cfca", "#247f8b", "#205b67") if index == 10 else ("#fff0b0", "#f7bf58", "#bd682d", "#854b2f"))
        for left, right, tone in ((0, 3, dark), (4, 6, light), (7, 8, highlight), (9, 17, middle), (18, 20, light), (21, 24, middle), (25, 31, dark)):
            drawing.rectangle((left, 0, right, 31), fill=tone)
        for rectangle in ((5, 8, 6, 12), (19, 17, 20, 20), (8, 25, 10, 27)):
            drawing.rectangle(rectangle, fill=highlight)
        drawing.rectangle((18, 25, 23, 28), fill="#e0cfaa")
        drawing.line((19, 26, 22, 26), fill="#8d6b3c")
    elif index == 12:
        for row in range(1, 32, 4):
            drawing.line((0, row, 31, row), fill="#9f8f71")
            drawing.line((1, row + 1, 30, row + 1), fill="#e0cfaa")
        drawing.rectangle((0, 0, 1, 31), fill="#a39375")
        drawing.rectangle((30, 0, 31, 31), fill="#ad9d7d")
    elif index == 13:
        drawing.rectangle((14, 0, 17, 31), fill="#ac9876")
        drawing.line((15, 0, 15, 31), fill="#79674f")
        drawing.line((17, 0, 17, 31), fill="#c9ba96")
        for row in (10, 13, 16, 19, 22):
            drawing.line((7, row, 12, row), fill="#6a6151")
            drawing.line((19, row, 24, row), fill="#6a6151")
            drawing.point((10 if row % 2 else 22, row), fill="#e0cfaa")
        drawing.line((6, 8, 12, 8), fill="#8b4356")
        drawing.line((19, 8, 25, 8), fill="#8b4356")
        drawing.rectangle((23, 21, 25, 22), fill="#a08d6c")
    return texture


def atlas_and_tilemap():
    atlas = Image.new("RGBA", (128, 128))
    entries = []
    for index, (name, chinese_name, material) in enumerate(TILES):
        pixel_x, pixel_y = index % 4 * 32, index // 4 * 32
        atlas.paste(paint_tile(index), (pixel_x, pixel_y))
        entries.append({
            "index": index, "name": name, "name_zh": chinese_name, "material": material,
            "bounds_px": [pixel_x, pixel_y, pixel_x + 32, pixel_y + 32],
            "uv_java": [pixel_x / 8, pixel_y / 8, (pixel_x + 32) / 8, (pixel_y + 32) / 8],
            "glow": False,
        })
    tilemap = {
        "schema": "ft-prison-kit-tilemap-1", "lane": "furnishings", "atlas": "atlas.png",
        "width": 128, "height": 128, "tile_size": 32, "columns": 4, "rows": 4,
        "filter": "nearest", "alpha": "opaque", "tiles": entries,
    }
    return atlas, tilemap


def voxel_owners(model):
    owners = np.full((16, 16, 16), -1, dtype=np.int16)
    overlaps = []
    for part_index, part in enumerate(model["boxes"]):
        low_x, low_y, low_z = part["from"]
        high_x, high_y, high_z = part["to"]
        region = owners[low_x:high_x, low_y:high_y, low_z:high_z]
        if np.any(region >= 0):
            overlaps.append(part["name"])
        region[:] = part_index
    return owners, overlaps


def positive_face_contact(first, second):
    for axis in range(3):
        touching = first["to"][axis] == second["from"][axis] or second["to"][axis] == first["from"][axis]
        other_axes = [other for other in range(3) if other != axis]
        if touching and all(min(first["to"][other], second["to"][other]) > max(first["from"][other], second["from"][other]) for other in other_axes):
            return True
    return False


def source_face_coverage(part, owners):
    coverage = {}
    for face_name, (axis, sign) in FACE_DIRECTIONS.items():
        other_axes = [other for other in range(3) if other != axis]
        exterior_coordinate = part["from"][axis] - 1 if sign < 0 else part["to"][axis]
        hidden, area = 0, 0
        for first_coordinate, second_coordinate in itertools.product(
            range(part["from"][other_axes[0]], part["to"][other_axes[0]]),
            range(part["from"][other_axes[1]], part["to"][other_axes[1]]),
        ):
            point = [0, 0, 0]
            point[axis] = exterior_coordinate
            point[other_axes[0]] = first_coordinate
            point[other_axes[1]] = second_coordinate
            area += 1
            if 0 <= exterior_coordinate < 16 and owners[tuple(point)] >= 0:
                hidden += 1
        coverage[face_name] = {"area": area, "hidden": hidden, "exposed": area - hidden}
    return coverage


def duplicate_exposed_unit_planes(parts, owners):
    seen, duplicates = set(), 0
    for part in parts:
        for face_name, (axis, sign) in FACE_DIRECTIONS.items():
            other_axes = [other for other in range(3) if other != axis]
            plane = part["from"][axis] if sign < 0 else part["to"][axis]
            outside = plane - 1 if sign < 0 else plane
            for first_coordinate, second_coordinate in itertools.product(
                range(part["from"][other_axes[0]], part["to"][other_axes[0]]),
                range(part["from"][other_axes[1]], part["to"][other_axes[1]]),
            ):
                point = [0, 0, 0]
                point[axis], point[other_axes[0]], point[other_axes[1]] = outside, first_coordinate, second_coordinate
                if 0 <= outside < 16 and owners[tuple(point)] >= 0:
                    continue
                key = axis, sign, plane, first_coordinate, second_coordinate
                duplicates += key in seen
                seen.add(key)
    return duplicates


def semantic_checks(models):
    by_id = {model["id"]: model for model in models}
    crate = by_id[MODEL_IDS[0]]
    barrel = by_id[MODEL_IDS[1]]
    shackles = by_id[MODEL_IDS[2]]
    shelf = by_id[MODEL_IDS[4]]
    niche = by_id[MODEL_IDS[5]]
    lectern = by_id[MODEL_IDS[6]]
    rack = by_id[MODEL_IDS[3]]
    rack_parts = {part["name"]: part for part in rack["boxes"]}
    crate_parts = {part["name"]: part for part in crate["boxes"]}
    niche_parts = {part["name"]: part for part in niche["boxes"]}
    lectern_parts = {part["name"]: part for part in lectern["boxes"]}
    crate_voxels, _ = voxel_owners(crate)
    barrel_voxels, _ = voxel_owners(barrel)
    shackle_voxels, _ = voxel_owners(shackles)
    shelf_voxels, _ = voxel_owners(shelf)
    niche_voxels, _ = voxel_owners(niche)
    barrel_profile = []
    for level in range(16):
        coordinates = np.argwhere(barrel_voxels[:, level, :] >= 0)
        if coordinates.size:
            extents = coordinates.max(axis=0) - coordinates.min(axis=0) + 1
            barrel_profile.append({"y": level, "width": int(extents[0]), "depth": int(extents[1]), "area": len(coordinates)})
    cuff_holes = [
        bool(np.all(shackle_voxels[2:5, 5:8, :] == -1)),
        bool(np.all(shackle_voxels[11:14, 5:8, :] == -1)),
    ]
    shelf_bottles = []
    for prefix in ("lower amber", "upper cyan", "upper amber"):
        body = next(part for part in shelf["boxes"] if part["name"] == f"{prefix} medicine body")
        neck = next(part for part in shelf["boxes"] if "narrow neck" in part["name"] and part["name"].startswith(prefix))
        shelf_bottles.append({
            "name": prefix, "body_width": body["to"][0] - body["from"][0],
            "neck_width": neck["to"][0] - neck["from"][0],
            "narrow_neck": neck["to"][0] - neck["from"][0] < body["to"][0] - body["from"][0],
        })
    arch_openings = []
    for level in (7, 10, 12):
        clear_x = np.where(niche_voxels[:, level, 6] < 0)[0]
        central_opening = [int(coordinate) for coordinate in clear_x if 3 <= coordinate <= 12]
        arch_openings.append({"y": level, "clear_x_at_z6": central_opening})
    return {
        "crate": {
            "closed_board_body": bool(np.all(crate_voxels[2:14, 1:10, 3:13] >= 0)),
            "real_front_recess_one_unit": bool(crate_voxels[6, 5, 2] == -1 and crate_voxels[6, 5, 3] >= 0),
            "lid_straps_and_back_hinges": all(name in crate_parts for name in ("left fitted lid strap", "right fitted lid strap", "left rear lid hinge", "right rear lid hinge")),
        },
        "barrel": {
            "horizontal_profiles": barrel_profile,
            "belly_wider_than_bottom": barrel_profile[7]["width"] > barrel_profile[0]["width"],
            "belly_corner_voids": bool(barrel_voxels[1, 7, 1] < 0 and barrel_voxels[14, 7, 14] < 0),
            "belly_area_fraction_of_bounding_square": barrel_profile[7]["area"] / (barrel_profile[7]["width"] * barrel_profile[7]["depth"]),
            "two_corner_steps_each_quadrant": bool(barrel_voxels[3, 7, 3] >= 0 and barrel_voxels[2, 7, 3] < 0 and barrel_voxels[2, 7, 5] >= 0),
            "lid_one_unit_below_rim": bool(barrel_voxels[8, 14, 8] < 0 and barrel_voxels[8, 13, 8] >= 0),
        },
        "shackles": {"cuff_air_openings_are_3x3_and_through_depth": cuff_holes},
        "weapons": {
            "broad_sword_crossguard_grip_blade_and_point": all(name in rack_parts for name in ("sword wide crossguard", "sword leather grip", "sword broad fullered blade", "sword narrowed terminal point")),
            "halberd_has_shaft_socket_spear_axe_toe_counterpick": all(name in rack_parts for name in ("halberd long timber shaft", "halberd metal socket", "halberd vertical spear point", "halberd broad axe head", "halberd hooked lower toe", "halberd rear counterpick")),
            "two_physical_rear_support_shoes": all(positive_face_contact(rack_parts[name], rack_parts["upper weapon carrying rail"]) for name in ("sword rail support shoe", "halberd rail support shoe")),
        },
        "shelf": {
            "necked_corked_bottles": shelf_bottles,
            "usable_shelf_levels": [2, 8],
            "actual_center_lower_air_gap": bool(np.all(shelf_voxels[8:10, 4:7, 7:12] == -1)),
            "upper_open_back_air": bool(np.all(shelf_voxels[3:13, 13:15, 13:14] == -1)),
        },
        "niche": {
            "recess_depth_from_front_pier_to_back": niche_parts["deep recessed lower backing"]["from"][2] - niche_parts["left deep load bearing pier"]["from"][2],
            "layered_arch_openings": arch_openings,
            "actual_central_air_above_skull": bool(np.all(niche_voxels[6:10, 10:12, 6:14] == -1)),
            "skull_has_cranium_narrow_jaw_and_crown": niche_parts["central carved skull cranium"]["to"][0] - niche_parts["central carved skull cranium"]["from"][0] > niche_parts["narrow skull jaw"]["to"][0] - niche_parts["narrow skull jaw"]["from"][0] and positive_face_contact(niche_parts["central carved skull cranium"], niche_parts["rounded skull crown"]),
            "bone_shaft_has_wider_end_joints": all(niche_parts[name]["to"][1] > niche_parts["foreground femur shaft"]["to"][1] and positive_face_contact(niche_parts[name], niche_parts["foreground femur shaft"]) for name in ("foreground femur left joint", "foreground femur right joint")),
        },
        "lectern": {
            "column_width": lectern_parts["slender ledger support column"]["to"][0] - lectern_parts["slender ledger support column"]["from"][0],
            "base_width": lectern_parts["broad stable slate foot"]["to"][0] - lectern_parts["broad stable slate foot"]["from"][0],
            "desk_width": lectern_parts["thin deep writing desk"]["to"][0] - lectern_parts["thin deep writing desk"]["from"][0],
            "book_outer_leaf_top": lectern_parts["left ledger lifted outer leaves"]["to"][1],
            "book_inner_leaf_top": lectern_parts["left ledger inner page block"]["to"][1],
            "book_separate_cover_spine_and_page_layers": all(name in lectern_parts for name in ("open ledger oxblood cover", "open ledger recessed central spine", "left ledger inner page block", "right ledger inner page block", "left ledger lifted outer leaves", "right ledger lifted outer leaves")),
            "nonflat_open_book_real_height_step": lectern_parts["left ledger lifted outer leaves"]["to"][1] > lectern_parts["left ledger inner page block"]["to"][1],
        },
    }


def geometry_audit(models, atlas, tilemap):
    errors, reports = [], []
    if tuple(model["id"] for model in models) != MODEL_IDS:
        errors.append("ID set/order changed")
    for model_index, model in enumerate(models):
        parts = model["boxes"]
        local_errors = []
        if not 1 <= len(parts) <= 28:
            local_errors.append("cuboid budget exceeded")
        if (model["kind"], model["mount"], model["light"], model["ingredient"]) != ("decor", "floor", 0, MODEL_NAMES[model_index][2]):
            local_errors.append("runtime metadata mismatch")
        for part in parts:
            coordinates = part["from"] + part["to"]
            if any(type(coordinate) is not int or not 0 <= coordinate <= 16 for coordinate in coordinates):
                local_errors.append(f"noninteger/out-of-block coordinates: {part['name']}")
            if any(part["from"][axis] >= part["to"][axis] for axis in range(3)):
                local_errors.append(f"nonpositive volume: {part['name']}")
            if part.get("rotation", [0, 0, 0]) != [0, 0, 0]:
                local_errors.append(f"nonzero rotation: {part['name']}")
            if any(not 0 <= index <= 15 for index in [part["tile"], *part.get("faces", {}).values()]):
                local_errors.append(f"bad tile reference: {part['name']}")
        owners, overlaps = voxel_owners(model)
        if overlaps:
            local_errors.append(f"intersecting volume: {overlaps}")
        ground_roots = {index for index, part in enumerate(parts) if part["from"][1] == 0}
        adjacency = {index: set() for index in range(len(parts))}
        for first_index, second_index in itertools.combinations(range(len(parts)), 2):
            if positive_face_contact(parts[first_index], parts[second_index]):
                adjacency[first_index].add(second_index)
                adjacency[second_index].add(first_index)
        connected = set(ground_roots)
        pending = deque(ground_roots)
        while pending:
            for neighbor in adjacency[pending.popleft()] - connected:
                connected.add(neighbor)
                pending.append(neighbor)
        floating = [parts[index]["name"] for index in range(len(parts)) if index not in connected]
        if floating:
            local_errors.append(f"no positive-area face path to ground: {floating}")
        fully_occluded_faces, partially_occluded_faces, exposed_unit_faces = 0, 0, 0
        fully_hidden_parts = []
        face_plan = {}
        for part in parts:
            coverage = source_face_coverage(part, owners)
            face_plan[part["name"]] = [face for face, entry in coverage.items() if entry["exposed"] == 0]
            fully_occluded_faces += sum(entry["exposed"] == 0 for entry in coverage.values())
            partially_occluded_faces += sum(0 < entry["hidden"] < entry["area"] for entry in coverage.values())
            exposed_unit_faces += sum(entry["exposed"] for entry in coverage.values())
            if not any(entry["exposed"] for entry in coverage.values()):
                fully_hidden_parts.append(part["name"])
        if fully_hidden_parts:
            local_errors.append(f"unnecessary fully hidden parts: {fully_hidden_parts}")
        duplicate_planes = duplicate_exposed_unit_planes(parts, owners)
        if duplicate_planes:
            local_errors.append(f"duplicate exposed same-normal unit planes: {duplicate_planes}")
        used_tiles = sorted({tile for part in parts for tile in [part["tile"], *part.get("faces", {}).values()]})
        report = {
            "number": MODEL_NUMBERS[model_index], "id": model["id"], "cuboids": len(parts),
            "source_declared_faces": len(parts) * 6,
            "provably_fully_hidden_source_faces": fully_occluded_faces,
            "source_faces_after_whole_face_prune": len(parts) * 6 - fully_occluded_faces,
            "partially_covered_faces_keep_or_partition": partially_occluded_faces,
            "exposed_voxel_unit_face_area": exposed_unit_faces,
            "occupied_unit_voxels": int(np.count_nonzero(owners >= 0)),
            "integer_0_to_16": True, "all_positive_volumes": True, "rotations": 0,
            "intersecting_volume_pairs": len(overlaps), "ground_connected_cuboids": len(connected),
            "duplicate_exposed_same_normal_unit_planes": duplicate_planes,
            "floating_parts": floating, "fully_hidden_parts": fully_hidden_parts,
            "used_tiles": used_tiles, "transparent_parts": 0, "animation_tracks": 0,
            "inverse_shells": 0, "errors": local_errors,
            "safe_full_face_prune_plan": face_plan,
        }
        reports.append(report)
        errors.extend(f"{model['id']}: {error}" for error in local_errors)
    if atlas.size != (128, 128) or atlas.mode != "RGBA" or atlas.getchannel("A").getextrema() != (255, 255):
        errors.append("atlas is not opaque 128x128 RGBA")
    all_used_tiles = sorted({tile for report in reports for tile in report["used_tiles"]})
    if all_used_tiles != list(range(16)):
        errors.append("unused atlas material slots")
    pixel_colors = np.asarray(atlas)
    unique_colors = len(np.unique(pixel_colors.reshape(-1, 4), axis=0))
    semantics = semantic_checks(models)
    required_semantics = [
        semantics["crate"]["closed_board_body"], semantics["crate"]["real_front_recess_one_unit"],
        semantics["barrel"]["belly_wider_than_bottom"], semantics["barrel"]["belly_corner_voids"],
        semantics["barrel"]["lid_one_unit_below_rim"], semantics["barrel"]["two_corner_steps_each_quadrant"],
        *semantics["shackles"]["cuff_air_openings_are_3x3_and_through_depth"],
        *(entry["narrow_neck"] for entry in semantics["shelf"]["necked_corked_bottles"]),
        semantics["shelf"]["actual_center_lower_air_gap"], semantics["shelf"]["upper_open_back_air"],
        semantics["niche"]["actual_central_air_above_skull"],
        semantics["crate"]["lid_straps_and_back_hinges"],
        *semantics["weapons"].values(),
        semantics["niche"]["skull_has_cranium_narrow_jaw_and_crown"],
        semantics["niche"]["bone_shaft_has_wider_end_joints"],
        semantics["lectern"]["book_separate_cover_spine_and_page_layers"],
        semantics["lectern"]["nonflat_open_book_real_height_step"],
    ]
    if not all(required_semantics):
        errors.append("required silhouette/negative-space semantic check failed")
    return {
        "schema": "ft-prison-furnishings-remake-qa-2", "status": "pass" if not errors else "fail",
        "target": "Minecraft Java 1.20.1 / Forge 47.4.10 / Java 17",
        "assigned_numbers": list(MODEL_NUMBERS), "models": reports, "errors": errors,
        "totals": {"cuboids": sum(report["cuboids"] for report in reports),
                   "source_declared_faces": sum(report["source_declared_faces"] for report in reports),
                   "source_faces_after_whole_face_prune": sum(report["source_faces_after_whole_face_prune"] for report in reports)},
        "atlas": {"size": [128, 128], "rgba_pixels": 16384, "base_rgba_bytes_not_gpu_memory": 65536,
                  "unique_colors": unique_colors, "tiles": len(tilemap["tiles"]), "used_tile_indices": all_used_tiles,
                  "alpha_min_max": [255, 255], "animation_frames": 1, "generated_random_noise": False},
        "semantic_checks": semantics, "manual_visual_review": VISUAL_REVIEW,
        "toolchain": {"python_minimum": "3.10", "pillow": PIL.__version__, "numpy": np.__version__},
        "image_generation_api_calls": 0, "image_generation_cost": 0,
        "editor_tested": False, "minecraft_client_tested": False, "java_export_tested_by_lane": False,
        "limitations": [
            "Offline orthographic ray-cast previews, not Blockbench or Minecraft screenshots.",
            "The parent exports and audits actual bbmodel, Java JSON, collision, recipes and Forge jar.",
            "Source face-prune counts are exact voxel occlusion proofs for integer opaque cuboids, not measurements of final exported faces.",
            "No FPS, GPU memory, draw-call or client-lighting measurement was made.",
            "128x128 atlas is new material content. Do not replace baseline pixels still referenced by unchanged model 14.",
        ],
    }


def face_uv(face, positions):
    world_x, world_y, world_z = positions[..., 0], positions[..., 1], positions[..., 2]
    if face == "north":
        return 16 - world_x, 16 - world_y
    if face == "south":
        return world_x, 16 - world_y
    if face == "west":
        return world_z, 16 - world_y
    if face == "east":
        return 16 - world_z, 16 - world_y
    if face == "up":
        return world_x, world_z
    return world_x, 16 - world_z


def render(model, atlas, view, size=256, clay=False):
    camera = np.asarray(VIEW_CAMERAS[view], dtype=np.float64)
    camera /= np.linalg.norm(camera)
    camera_up = np.array((0.0, 0.0, 1.0) if view == "top" else (0.0, 1.0, 0.0))
    camera_right = np.cross(camera, camera_up)
    camera_right /= np.linalg.norm(camera_right)
    camera_up = np.cross(camera_right, camera)
    horizontal, vertical = np.meshgrid(np.arange(size) + 0.5, np.arange(size) + 0.5)
    scale = size / 23.5
    origins = (
        np.array((8.0, 8.0, 8.0)) + camera * 48
        + (horizontal - size / 2)[..., None] / scale * camera_right
        + (size / 2 - vertical)[..., None] / scale * camera_up
    )
    ray = -camera
    depth = np.full((size, size), np.inf)
    pixels = np.zeros((size, size, 4), dtype=np.uint8)
    texture_pixels = np.asarray(atlas)
    shades = {"north": 1.0, "south": 0.86, "west": 0.82, "east": 0.90, "up": 1.10, "down": 0.68}
    axis_faces = (("west", "east"), ("down", "up"), ("north", "south"))
    for part in model["boxes"]:
        intervals = []
        exits = []
        eligible = np.ones((size, size), dtype=bool)
        for axis in range(3):
            if abs(ray[axis]) < 1e-9:
                eligible &= (origins[..., axis] >= part["from"][axis]) & (origins[..., axis] <= part["to"][axis])
                intervals.append(np.full((size, size), -np.inf))
                exits.append(np.full((size, size), np.inf))
            else:
                first = (part["from"][axis] - origins[..., axis]) / ray[axis]
                second = (part["to"][axis] - origins[..., axis]) / ray[axis]
                intervals.append(np.minimum(first, second))
                exits.append(np.maximum(first, second))
        near = np.max(intervals, axis=0)
        far = np.min(exits, axis=0)
        visible = eligible & (near <= far) & (far >= 0) & (near < depth)
        if not np.any(visible):
            continue
        entry_axes = np.argmax(intervals, axis=0)
        positions = origins + near[..., None] * ray
        for axis in range(3):
            selected = visible & (entry_axes == axis)
            if not np.any(selected):
                continue
            face = axis_faces[axis][0 if ray[axis] > 0 else 1]
            tile = part.get("faces", {}).get(face, part["tile"])
            texture_u, texture_v = face_uv(face, positions)
            pixel_u = np.clip(np.floor(texture_u * 2).astype(np.int32), 0, 31) + tile % 4 * 32
            pixel_v = np.clip(np.floor(texture_v * 2).astype(np.int32), 0, 31) + tile // 4 * 32
            selected_colors = texture_pixels[pixel_v[selected], pixel_u[selected], :3]
            if clay:
                selected_colors = np.full_like(selected_colors, (159, 174, 179))
            pixels[selected, :3] = np.clip(selected_colors.astype(np.float64) * shades[face], 0, 255).astype(np.uint8)
            pixels[selected, 3] = 255
        depth[visible] = near[visible]
    result = Image.new("RGBA", (size, size), (29, 40, 50, 255))
    if view.endswith("iso"):
        drawing = ImageDraw.Draw(result)
        ground = np.array((0, -8, 0))
        screen_y = size / 2 - float(np.dot(ground, camera_up)) * scale
        drawing.ellipse((size * 0.19, screen_y - size * 0.065, size * 0.81, screen_y + size * 0.055), fill=(20, 29, 38))
    result.alpha_composite(Image.fromarray(pixels))
    return result


def font(size, bold=False):
    font_path = Path("C:/Windows/Fonts/msyhbd.ttc" if bold else "C:/Windows/Fonts/msyh.ttc")
    if font_path.is_file():
        return ImageFont.truetype(str(font_path), size)
    return ImageFont.load_default(size=size)


def preview_assets(root, models, atlas):
    work = root / "work"
    work.mkdir(parents=True, exist_ok=True)
    width, header, row_height = 1560, 150, 280
    sheet = Image.new("RGB", (width, header + len(models) * row_height + 70), "#111d27")
    drawing = ImageDraw.Draw(sheet)
    drawing.text((28, 19), "THE FORGOTTEN CELLS / FURNISHINGS REMAKE", font=font(27, True), fill="#e0cfaa")
    drawing.text((28, 62), "09–13 · 15 · 16  /  原创结构重做 · 真实负空间 · 64px识别度", font=font(18), fill="#99b1bc")
    labels = ("正前斜视", "背后斜视", "正面", "右侧", "俯视")
    for column, label in enumerate(labels):
        drawing.text((252 + column * 258, 113), label, font=font(17), fill="#b9c9cb")
    clay_sheet = Image.new("RGB", (1000, 96 + len(models) * 218), "#111d27")
    clay_drawing = ImageDraw.Draw(clay_sheet)
    clay_drawing.text((20, 18), "GEOMETRY ONLY / 纹理关闭", font=font(23, True), fill="#e0cfaa")
    for model_index, model in enumerate(models):
        row_y = header + model_index * row_height
        drawing.rounded_rectangle((17, row_y + 3, width - 17, row_y + row_height - 8), radius=10, fill="#1d2832", outline="#344954")
        drawing.text((28, row_y + 16), f"{MODEL_NUMBERS[model_index]:02d}  {model['name_zh']}", font=font(19, True), fill="#dfd6c0")
        drawing.text((28, row_y + 47), f"{len(model['boxes'])} cuboids / {len(model['boxes']) * 6} raw faces", font=font(13), fill="#9caeb1")
        icon = render(model, atlas, "front_iso", size=64)
        model_dir = work / model["id"]
        model_dir.mkdir(exist_ok=True)
        icon.save(model_dir / "icon-64.png")
        sheet.paste(icon.resize((128, 128), Image.Resampling.NEAREST).convert("RGB"), (58, row_y + 83))
        drawing.text((47, row_y + 225), "原生64px · nearest×2", font=font(12), fill="#8ca2aa")
        strip = Image.new("RGB", (1280, 294), "#1d2832")
        strip_drawing = ImageDraw.Draw(strip)
        for column, (view, label) in enumerate(zip(VIEW_CAMERAS, labels)):
            rendered = render(model, atlas, view)
            rendered.save(model_dir / f"{view}.png")
            sheet.paste(rendered.convert("RGB"), (240 + column * 258, row_y + 8))
            strip.paste(rendered.convert("RGB"), (column * 256, 30))
            strip_drawing.text((column * 256 + 12, 4), label, font=font(16), fill="#b9c9cb")
        strip.save(model_dir / "review-strip.png")
        clay_y = 87 + model_index * 218
        clay_drawing.text((18, clay_y + 21), f"{MODEL_NUMBERS[model_index]:02d}", font=font(23, True), fill="#b9c9cb")
        for column, view in enumerate(("front_iso", "rear_iso", "top", "front")):
            clay = render(model, atlas, view, size=208, clay=True)
            clay.save(model_dir / f"clay_{view}.png")
            clay_sheet.paste(clay.convert("RGB"), (105 + column * 219, clay_y))
    drawing.text((28, sheet.height - 48), "离线同条件正交光线投射；非游戏截图。Java/Blockbench/Forge导出与客户端检查由父任务负责。", font=font(16), fill="#8ca2aa")
    sheet.save(root / "contact-sheet.png")
    clay_sheet.save(work / "clay-sheet.png")
    tile_sheet = Image.new("RGB", (640, 656), "#111d27")
    tile_drawing = ImageDraw.Draw(tile_sheet)
    for index, entry in enumerate(TILES):
        pixel_x, pixel_y = index % 4 * 160, index // 4 * 164
        tile = atlas.crop((index % 4 * 32, index // 4 * 32, index % 4 * 32 + 32, index // 4 * 32 + 32))
        tile_sheet.paste(tile.resize((128, 128), Image.Resampling.NEAREST).convert("RGB"), (pixel_x + 16, pixel_y + 4))
        tile_drawing.text((pixel_x + 4, pixel_y + 139), f"{index:02d} {entry[1]}", font=font(12), fill="#cfccba")
    tile_sheet.save(work / "atlas-guide.png")


def json_text(value):
    return json.dumps(value, ensure_ascii=False, indent=2) + "\n"


def file_hash(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def baseline_comparison(baseline, models, atlas):
    if baseline is None:
        return {"status": "not_requested", "baseline_imported_as_geometry_or_texture": False}
    baseline = baseline.resolve()
    original_models = json.loads((baseline / "definitions.json").read_text(encoding="utf-8"))
    originals = {model["id"]: model for model in original_models}
    costs = []
    for model in models:
        original = originals[model["id"]]
        old_shape = {(tuple(part["from"]), tuple(part["to"])) for part in original["boxes"]}
        new_shape = {(tuple(part["from"]), tuple(part["to"])) for part in model["boxes"]}
        costs.append({
            "id": model["id"], "original_cuboids": len(original["boxes"]), "new_cuboids": len(model["boxes"]),
            "shape_identical_ignoring_materials_and_names": old_shape == new_shape,
            "unchanged_box_volumes": len(old_shape & new_shape),
            "runtime_metadata_equal": all(model.get(field) == original.get(field) for field in ("id", "kind", "mount", "light", "ingredient", "base")),
        })
    original_atlas = Image.open(baseline / "atlas.png").convert("RGBA")
    return {
        "status": "pass" if all(not entry["shape_identical_ignoring_materials_and_names"] and entry["runtime_metadata_equal"] for entry in costs) else "fail",
        "baseline_imported_as_geometry_or_texture": False,
        "baseline_read_only_files_sha256": {name: file_hash(baseline / name) for name in ("definitions.json", "atlas.png", "tilemap.json")},
        "model_comparisons": costs,
        "new_atlas_pixels_differ": atlas.tobytes() != original_atlas.tobytes(),
        "unchanged_model_14_excluded_from_this_lane": True,
    }


def design_text(models, qa):
    rows = []
    for report in qa["models"]:
        rows.append(f"| {report['number']:02d} | `{report['id']}` | {report['cuboids']} | {report['source_declared_faces']} | {report['provably_fully_hidden_source_faces']} | {report['source_faces_after_whole_face_prune']} |")
    body = f"""# 囚狱陈设重做：09–13、15、16

## 交付与运行边界

目标 Java 1.20.1 / Forge 47.4.10 / Java 17；由父任务导出 Java 方块、物品展示、`.bbmodel` 和 Forge 包。本 lane 仅提供新结构定义、原创图集、可复现工具及离线检查。七款均为原 ID 的 `decor`、`floor`、`light:0`，配方素材与原款相同；无物品栏、tick、实体、玩家束缚或新玩法。

所有写入均在本目录内，调试图与复跑副本放 `work/`。原款只读，不触碰 Blockbench、已有模组或另一批 image 参考资产。没有使用图片 API，付费图片调用为 0；贴图由 Pillow 以硬边像素簇原创绘制。没有运行客户端或编辑器。

## 轮廓与结构预算

制作前约束为每款最多 28 个结构六面体、整数 0..16、零旋转、正体积、无体积交叠和漂浮。零旋转限制下，桶使用少量有功能的宽截面层表达桶肩/鼓腹/箍铁，而非逐像素挤出。拱顶、打开的账本和器物均保留真实空隙及厚度。

| 原编号 | 原 ID | 六面体 | 源声明面 | 可证明全遮挡面 | 全面剔除后理论源面 |
|---|---|---:|---:|---:|---:|
{chr(10).join(rows)}

合计 {qa['totals']['cuboids']} 盒、{qa['totals']['source_declared_faces']} 个源声明面。全遮挡计划保存在 `qa.json` 各模型 `safe_full_face_prune_plan`；接触面的部分遮挡不能删除整面，父导出器可按可见区域保守分割。表中数字不是最终 Java 已导出面数、draw calls 或 FPS。

- **09 封存补给箱**：连续封闭凹板箱身；前后四边框与左右回折边压住箱板，底托和整盖闭合。盖顶双铁箍、前落扣与独立铜锁、后合页都有实体连接。凹板是实际 1 单位深度，不是把黑纹当凹槽；四角不再是断开的巨大柱子。
- **10 锈箍鼓腹桶**：上下收口均为 10×10，鼓腹 14×14；宽腹和两道箍各以五块不相交分区，给每个角两级 2 单位细阶，避免旧四角大方块裁切的明显十字。宽腹截面面积为 148/196，约 75.5%，前后和左右端部直边均缩为 6 单位，再以两阶连续转入侧面。上下窄肩让侧向鼓腹差增加。上下箍替代同高木料而不交叉，底铁边以贴图表示，节省三盒给更细的曲面分区。四段收口凸沿围住下沉 1 单位的封闭木盖。最终 28 盒，是整数零旋转约束下的细阶折圆木桶，不是数学圆筒。
- **11 双镣刑柱**：16 高立柱连接地脚、横梁与两只前悬镣环。每环四条铁构件形成 3×3 贯通空洞；吊杆、前伸锚臂与横梁接合，镣环位于立柱之前，不被背后木头填空。背面独立紧固铁板，脚座不是漂浮木片。
- **12 狱卒兵器架**：深落地脚、双立柱、上下横档与前置托槽；一把有铜球头、皮握把、宽护手、宽刃和收尖的剑，一支带金属套、尖头、斧翼、钩趾和反向配重尖的长柄戟。两件均有后托靴接至承重横档；背面能看到真实支撑关系。刃钢与木杆的材质区分明确。
- **13 旧炼金药书架**：两层 7–9 单位深的书药架；三只药瓶均有宽瓶腹、细颈与塞口。下层多本不同厚度立书和前排横放书，上层不对称高矮书册。后方用低横条防滑落，留出透空而非整张黑背；侧板端面与顶底层深度一致。
- **15 支拱藏骨龛**：深脚基、双厚墩、起拱石、内挑支拱、合龙冠石和凸出的关键石。开口从 8 单位宽依次缩为 6、4；最前支拱 z5，深背板 z14，龛内实深 8–9 单位。颅骨由收窄下颌、颅体和顶部组成，眼窝/鼻孔/齿列使用骨材质像素；另有带粗关节的真实长骨和后方短骨。没有平面假开洞。
- **16 典狱账卷台**：12 宽低石脚、收束木座、仅 2 宽承柱、薄桌面和前后挡书边。展开账簿有独立暗红封皮、凹中缝、双内页、双外侧抬起的页块；由真实高度台阶表达打开的两翼，页数、横线、列数写进贴图。桌边墨水瓶与铜口直连桌面。未用大量薄片/逐像素盒模拟倾斜页。

## 材质与 UV

`atlas.png` 为 128×128 RGBA，不透明，4×4 个 32px 材质块。16 块全部有引用，单帧 16,384 像素，基础未压缩 RGBA 65,536 字节，1 帧，无额外描边壳或动画。父导出器可按原合同提取 tile，并对整格坐标裁切 UV：north=(16−x,16−y)，south=(x,16−y)，west=(z,16−y)，east=(16−z,16−y)，up=(x,z)，down=(x,16−z)。使用 nearest，2 texel/模型单位。不能把每个狭长面重复映射完整 32px 纹理。

木料为暖褐底色+顺纹的成段明暗；箍铁为冷蓝灰锤痕和少量成簇铁锈；锁/护手使用小面积黄铜。刃钢横向分配脊与刃的窄亮簇，皮革用暗红与装帧线，纸页用连续断面横线。瓶为不透明青釉/琥珀大色块，刻意不使用 alpha 或 shader；石龛与石脚保持冷灰蓝，只有少量矿物棕斑。背侧与俯视也有对应木端纹、合页、支撑和后挡条。

**集成要求：新图集属于 revision 材质。14 的定义和其引用的原像素必须保持不变；父任务应为 revision tile 分配独立纹理路径/重映射，不可整张覆盖原 furnishings atlas。**

## 已执行检查与证据

`qa.json` 记录精确整数体素检查：坐标、正体积、零旋转、所有盒的体积交叠、每个组件与地面的正面积接触路径、全遮挡面、完全隐藏组件、材料索引、独立轮廓/空洞断言。所有正向检查均通过；箱板凹槽、桶腹折角、3×3 真镣孔、药瓶细颈与书架/拱顶透空都有程序检查。

`contact-sheet.png` 七行五视图为相同尺度与中性背景的离线正交光线投射；包含前斜视、后斜视、正面、右侧、俯视，额外提供原生 64px 图标。`work/clay-sheet.png` 关闭贴图检查结构。`work/<id>/` 保存各视图、灰模、64px 图标与复核长条，`work/atlas-guide.png` 展示硬边像素材料簇。手工视觉检查结果见 `qa.json/manual_visual_review`。

复现命令（从任何目录运行，输出默认脚本所在目录）：

```powershell
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' -B './make_assets.py' --baseline 'C:/Users/YOUR_USERNAME/Documents/Codex/2026-11-03/zai/outputs/ft-dungeon-prison-kit-20261103/lanes/furnishings'
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' -B './make_assets.py' --verify-repro
```

`--out` 只能指向本 lane 内部路径；复跑副本只写 `work/repro`。无随机数、系统时间或网络请求。相同依赖版本下 definitions、atlas、tilemap、DESIGN、contact-sheet 五个主要产物要求字节完全一致，复现结果单独保存为 `work/repro-check.json` 并写回 qa 摘要。

## 未执行与实际成本

未打开或改动共享 Blockbench；未生成本 lane 的最终 Java / bbmodel / Forge jar，未启动 Minecraft，无游戏内光照、手持 Display、碰撞或性能实测。父任务统一导出与独立 QA。所有可见图是离线预览，不能替代真实运行验证。图片 API 使用 0 次、图片生成费用 0；对话推理与本地计算时长/费用未计量。
"""
    return body


def write_bundle(root, baseline=None):
    root = root.resolve()
    if root != ALLOWED_ROOT and ALLOWED_ROOT not in root.parents:
        raise ValueError("All output paths must stay inside the assigned furnishings lane")
    root.mkdir(parents=True, exist_ok=True)
    models = definitions()
    atlas, tilemap = atlas_and_tilemap()
    qa = geometry_audit(models, atlas, tilemap)
    qa["baseline_comparison"] = baseline_comparison(baseline, models, atlas)
    if qa["baseline_comparison"]["status"] == "fail":
        qa["errors"].append("baseline geometry/metadata comparison failed")
        qa["status"] = "fail"
    if qa["errors"]:
        (root / "qa.json").write_text(json_text(qa), encoding="utf-8")
        raise ValueError("\n".join(qa["errors"]))
    (root / "definitions.json").write_text(json_text(models), encoding="utf-8")
    (root / "tilemap.json").write_text(json_text(tilemap), encoding="utf-8")
    atlas.save(root / "atlas.png")
    preview_assets(root, models, atlas)
    (root / "DESIGN.md").write_text(design_text(models, qa), encoding="utf-8")
    qa["artifact_sha256"] = {name: file_hash(root / name) for name in ("definitions.json", "atlas.png", "tilemap.json", "contact-sheet.png", "DESIGN.md", "make_assets.py") if (root / name).is_file()}
    qa["previews"] = {"views_per_model": list(VIEW_CAMERAS), "raw_icon_size": [64, 64],
                      "projection": "orthographic", "renderer": "nearest-sampled opaque cuboid ray caster",
                      "camera_unit_vectors": {name: [round(float(value), 6) for value in np.asarray(vector) / np.linalg.norm(vector)] for name, vector in VIEW_CAMERAS.items()},
                      "unit_model_scale": "256px image / 23.5 model units", "randomness": False,
                      "source": "same definitions.json geometry and global face UV mapping as parent contract"}
    (root / "qa.json").write_text(json_text(qa), encoding="utf-8")
    return qa


def check_repro(root):
    root = root.resolve()
    repeated_root = root / "work" / "repro"
    write_bundle(repeated_root)
    comparisons = {}
    names = ("definitions.json", "atlas.png", "tilemap.json", "DESIGN.md", "contact-sheet.png")
    for name in names:
        original_hash, repeated_hash = file_hash(root / name), file_hash(repeated_root / name)
        comparisons[name] = {"sha256": original_hash, "repeated_sha256": repeated_hash, "byte_identical": original_hash == repeated_hash}
    result = {"status": "pass" if all(entry["byte_identical"] for entry in comparisons.values()) else "fail",
              "same_toolchain": True, "random_or_clock_input": False, "files": comparisons}
    (root / "work" / "repro-check.json").write_text(json_text(result), encoding="utf-8")
    qa_path = root / "qa.json"
    qa = json.loads(qa_path.read_text(encoding="utf-8"))
    qa["reproducibility"] = result
    if result["status"] != "pass":
        qa["status"] = "fail"
        qa["errors"].append("generator reproducibility failed")
    qa_path.write_text(json_text(qa), encoding="utf-8")
    return result


def main():
    parser = argparse.ArgumentParser(description="Original seven-model prison furnishings revision; writes only inside its assigned lane")
    parser.add_argument("--out", type=Path, default=ALLOWED_ROOT)
    parser.add_argument("--baseline", type=Path)
    parser.add_argument("--verify-repro", action="store_true")
    arguments = parser.parse_args()
    root = arguments.out.resolve()
    if root != ALLOWED_ROOT and ALLOWED_ROOT not in root.parents:
        parser.error("--out must resolve inside this furnishings lane")
    if arguments.verify_repro:
        result = check_repro(root)
        print(json.dumps(result, ensure_ascii=False))
        raise SystemExit(0 if result["status"] == "pass" else 1)
    qa = write_bundle(root, arguments.baseline)
    print(json.dumps({"status": qa["status"], "models": [{"number": entry["number"], "id": entry["id"], "cuboids": entry["cuboids"], "faces": entry["source_declared_faces"]} for entry in qa["models"]], "atlas": qa["atlas"], "output": str(root)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
