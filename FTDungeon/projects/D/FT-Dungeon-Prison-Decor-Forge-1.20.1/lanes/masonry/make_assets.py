from __future__ import annotations

import argparse
import hashlib
import json
import math
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parent
PALETTE = {
    "cavity": "#141d27",
    "stone_dark": "#253744",
    "stone": "#354c59",
    "stone_mid": "#405965",
    "stone_light": "#4e6570",
    "stone_edge": "#607781",
    "stone_high": "#74858b",
    "iron": "#252b35",
    "iron_edge": "#59626a",
    "rust": "#784936",
    "rust_edge": "#b4794e",
    "brass": "#8d6b3c",
    "brass_edge": "#c3a065",
    "mineral": "#bcae8b",
    "damp": "#2c5356",
    "damp_edge": "#3e6362",
    "verdigris": "#247f8b",
}
FACES = ("north", "south", "east", "west", "up", "down")
EXPECTED_IDS = (
    "prison_blue_bricks",
    "prison_damp_bricks",
    "prison_fractured_bricks",
    "prison_inlaid_tiles",
    "prison_brick_stairs",
    "prison_brick_slab",
    "prison_fluted_pillar",
    "prison_cornice",
)
TILE_SPECS = (
    ("blue_bricks", "冷蓝错缝砖", "xy", "Shared native stair/slab base; four 8px courses."),
    ("damp_bricks", "渗灰潮砌砖", "xy", "Attached mineral fans and blue-green seam damp, not moss noise."),
    ("fractured_bricks", "锈补裂砌砖", "xy", "Connected stepped fractures, edge chips and two iron stitch marks."),
    ("inlaid_floor", "黄铜菱嵌地砖", "xy", "Matte brass lozenge in a four-slab bed; no emission."),
    ("dressed_returns", "嵌砖侧面", "xy", "Quiet dressed stone, used on all four inlay returns."),
    ("pillar_recess", "柱槽阴面", "y", "Continuous recessed shaft material without a fake cap band."),
    ("pillar_rib", "双棱柱面", "y", "Paired raised-rib faces; long intentional chisel clusters."),
    ("pillar_end", "柱端刀痕", "", "XY/XZ-projected stone end grain, top equals bottom assignment."),
    ("cornice_crown", "檐冠立面", "x", "Bands in rows 0..6 match the crown's y=13..16 footprint."),
    ("cornice_frieze", "檐下雕带", "x", "Shallow-looking carved chevrons in rows 6..14; texture only."),
    ("cornice_soffit", "檐面与仰底", "xy", "Dressed horizontal stone on top, underside and returns."),
    ("cornice_dentil", "齿饰端面", "x", "Four stone teeth with real air between; no pixel extrusion."),
    ("inlay_underside", "嵌砖底面", "xy", "Plain non-emitting underside with broad tooling."),
    ("pillar_returns", "柱棱窄侧", "y", "Darker narrow rib returns with uninterrupted vertical rhythm."),
    ("cornice_wall_back", "檐背贴墙石", "x", "Finished rear heel; a small lime patch is deliberately placed."),
    ("cornice_drip", "檐口滴水线", "x", "Stone drip fillet with old iron runoff, not a metal plate."),
)


def rgba(color):
    value = PALETTE.get(color, color).lstrip("#")
    return tuple(int(value[offset:offset + 2], 16) for offset in (0, 2, 4)) + (255,)


def new_tile(color="stone"):
    return Image.new("RGBA", (32, 32), rgba(color))


def shape(image, kind, points, color, width=1, wrap=False):
    offsets = (-32, 0, 32) if wrap else (0,)
    drawing = ImageDraw.Draw(image)
    for offset_x in offsets:
        for offset_y in offsets:
            shifted = [(point[0] + offset_x, point[1] + offset_y) for point in points]
            if kind == "line":
                drawing.line(shifted, fill=rgba(color), width=width)
            elif kind == "rect":
                drawing.rectangle(shifted, fill=rgba(color))
            else:
                drawing.polygon(shifted, fill=rgba(color))


def seal_edges(image, axes="xy"):
    if "x" in axes:
        for pixel_y in range(32):
            image.putpixel((31, pixel_y), image.getpixel((0, pixel_y)))
    if "y" in axes:
        for pixel_x in range(32):
            image.putpixel((pixel_x, 31), image.getpixel((pixel_x, 0)))
    return image


def brick_tile():
    image = new_tile("stone_dark")
    brick_tones = ("stone_mid", "stone", "stone_light", "stone_mid", "stone", "stone_mid", "stone_mid", "stone_light")
    for course in range(4):
        course_y = course * 8
        stagger = 8 if course % 2 else 0
        for brick_number in range(2):
            brick_x = brick_number * 16 + stagger
            variation = course * 2 + brick_number
            shape(image, "rect", [(brick_x + 1, course_y + 1), (brick_x + 14, course_y + 6)], brick_tones[variation], wrap=True)
            shape(image, "line", [(brick_x + 2, course_y + 1), (brick_x + 11, course_y + 1)], "stone_edge", wrap=True)
            shape(image, "line", [(brick_x + 14, course_y + 3), (brick_x + 14, course_y + 6), (brick_x + 4, course_y + 6)], "stone", wrap=True)
            if variation in (0, 3, 6):
                shape(image, "poly", [(brick_x + 3, course_y + 3), (brick_x + 8, course_y + 3), (brick_x + 10, course_y + 4), (brick_x + 12, course_y + 4), (brick_x + 12, course_y + 5), (brick_x + 3, course_y + 5)], "stone_light", wrap=True)
            if variation in (1, 4, 7):
                shape(image, "line", [(brick_x + 6, course_y + 4), (brick_x + 9, course_y + 4), (brick_x + 10, course_y + 3)], "stone_dark", wrap=True)
            if variation in (2, 5):
                shape(image, "line", [(brick_x + 2, course_y + 2), (brick_x + 4, course_y + 2)], "stone_high", wrap=True)
            shape(image, "line", [(brick_x, course_y + 1), (brick_x, course_y + 6)], "cavity", wrap=True)
    return seal_edges(image)


def damp_tile(base):
    image = base.copy()
    patches = (
        [(1, 8), (8, 8), (8, 10), (6, 10), (6, 13), (4, 13), (4, 11), (2, 11)],
        [(18, 1), (22, 1), (22, 3), (24, 3), (24, 5), (21, 5), (21, 4), (19, 4)],
        [(10, 16), (16, 16), (16, 18), (14, 18), (14, 22), (12, 22), (12, 20), (10, 20)],
        [(22, 24), (29, 24), (29, 27), (27, 27), (27, 29), (24, 29), (24, 27), (22, 27)],
    )
    for points in patches:
        shape(image, "poly", points, "damp")
    for points in ([(3, 9), (6, 9), (6, 11), (5, 11)], [(12, 17), (14, 17), (14, 19)], [(25, 25), (28, 25), (28, 27)]):
        shape(image, "line", points, "damp_edge")
    for points in ([(9, 8), (10, 9), (10, 12), (11, 12), (11, 14)], [(25, 16), (24, 17), (24, 20)], [(18, 24), (18, 28)]):
        shape(image, "line", points, "stone_high")
    shape(image, "line", [(9, 9), (9, 11)], "mineral")
    shape(image, "line", [(23, 17), (23, 19)], "mineral")
    shape(image, "rect", [(17, 26), (18, 27)], "stone_edge")
    return seal_edges(image)


def fractured_tile(base):
    image = base.copy()
    fractures = (
        [(6, 1), (6, 3), (7, 3), (7, 5), (9, 5), (9, 8), (10, 8), (10, 11)],
        [(10, 9), (13, 9), (13, 10), (16, 10), (16, 12), (18, 12), (18, 14)],
        [(22, 17), (21, 17), (21, 19), (19, 19), (19, 21), (18, 21), (18, 24), (17, 24), (17, 27), (15, 27), (15, 30)],
        [(19, 21), (23, 21), (23, 22), (26, 22)],
    )
    for points in fractures:
        shape(image, "line", [(pixel_x + 1, pixel_y) for pixel_x, pixel_y in points], "stone_edge")
        shape(image, "line", points, "cavity")
    for points in ([(2, 14), (6, 14), (6, 15), (4, 15), (4, 17), (2, 17)], [(26, 5), (29, 5), (29, 8), (27, 8), (27, 7), (26, 7)]):
        shape(image, "poly", points, "stone_dark")
    for origin_x, origin_y in ((7, 2), (19, 18)):
        shape(image, "rect", [(origin_x, origin_y), (origin_x + 4, origin_y + 1)], "iron")
        shape(image, "line", [(origin_x + 1, origin_y), (origin_x + 3, origin_y)], "iron_edge")
        shape(image, "rect", [(origin_x, origin_y + 2), (origin_x + 1, origin_y + 3)], "rust")
        shape(image, "line", [(origin_x, origin_y + 2), (origin_x, origin_y + 2)], "rust_edge")
    return seal_edges(image)


def dressed_tile(underside=False):
    image = new_tile("stone_mid" if not underside else "stone")
    for origin_x, origin_y in ((0, 0), (16, 0), (8, 16), (24, 16)):
        shape(image, "rect", [(origin_x, origin_y), (origin_x + 15, origin_y + 15)], "stone_dark", wrap=True)
        shape(image, "rect", [(origin_x + 1, origin_y + 1), (origin_x + 14, origin_y + 14)], "stone_mid" if not underside else "stone", wrap=True)
        shape(image, "line", [(origin_x + 2, origin_y + 1), (origin_x + 12, origin_y + 1)], "stone_light", wrap=True)
        shape(image, "poly", [(origin_x + 3, origin_y + 9), (origin_x + 6, origin_y + 8), (origin_x + 12, origin_y + 8), (origin_x + 12, origin_y + 11), (origin_x + 8, origin_y + 12), (origin_x + 3, origin_y + 12)], "stone" if not underside else "stone_mid", wrap=True)
        shape(image, "line", [(origin_x + 5, origin_y + 5), (origin_x + 9, origin_y + 5)], "stone_edge" if not underside else "stone_light", wrap=True)
    return seal_edges(image)


def inlaid_tile():
    image = new_tile("stone_dark")
    for origin_x, origin_y, tone in ((1, 1, "stone_mid"), (17, 1, "stone"), (1, 17, "stone"), (17, 17, "stone_mid")):
        shape(image, "rect", [(origin_x, origin_y), (origin_x + 13, origin_y + 13)], tone)
        shape(image, "line", [(origin_x + 1, origin_y), (origin_x + 12, origin_y)], "stone_light")
        shape(image, "line", [(origin_x + 3, origin_y + 10), (origin_x + 6, origin_y + 10), (origin_x + 7, origin_y + 9)], "stone_dark")
    diamond = [(15, 3), (28, 16), (16, 28), (3, 15), (15, 3)]
    shape(image, "line", diamond, "iron", width=3)
    shape(image, "line", diamond, "brass")
    shape(image, "line", [(15, 4), (25, 14)], "brass_edge")
    shape(image, "line", [(5, 16), (15, 26)], "rust")
    shape(image, "rect", [(13, 13), (18, 18)], "iron")
    shape(image, "line", [(14, 13), (17, 13), (18, 14)], "iron_edge")
    shape(image, "rect", [(15, 15), (16, 16)], "verdigris")
    for origin_x, origin_y in ((3, 3), (25, 3), (3, 25), (25, 25)):
        shape(image, "line", [(origin_x, origin_y + 3), (origin_x, origin_y), (origin_x + 3, origin_y)], "iron_edge")
    return seal_edges(image)


def shaft_tile(role):
    image = new_tile("stone_light" if role == "rib" else "stone")
    for strip_x, strip_width, tone in ((0, 3, "stone_dark"), (6, 2, "stone_mid"), (12, 3, "stone_edge"), (20, 2, "stone_light"), (27, 3, "stone_mid")):
        shape(image, "rect", [(strip_x, 0), (strip_x + strip_width - 1, 31)], tone if role == "rib" else "stone_mid")
    marks = ((4, 4, 10), (10, 17, 25), (18, 7, 13), (24, 21, 28), (29, 1, 5))
    for mark_x, start_y, end_y in marks:
        shape(image, "line", [(mark_x, start_y), (mark_x, end_y)], "stone_high" if role == "rib" else "stone_light")
        if role == "recess":
            shape(image, "line", [(mark_x + 1, start_y + 2), (mark_x + 1, end_y)], "stone_dark")
    if role == "return":
        shape(image, "rect", [(8, 0), (9, 31)], "stone_dark")
        shape(image, "rect", [(23, 0), (24, 31)], "stone_dark")
    return seal_edges(image, "y")


def pillar_end_tile():
    image = new_tile("stone_mid")
    drawing = ImageDraw.Draw(image)
    for bounds, color in (((4, 4, 27, 27), "stone"), ((8, 8, 23, 23), "stone_light"), ((11, 11, 20, 20), "stone_mid")):
        drawing.rectangle(bounds, outline=rgba(color))
    shape(image, "line", [(14, 8), (14, 12), (17, 12), (17, 15), (20, 15)], "stone_dark")
    shape(image, "line", [(9, 20), (14, 20)], "stone_edge")
    return image


def cornice_tiles():
    crown = new_tile("stone_light")
    for start_y, end_y, tone in ((0, 0, "stone_high"), (1, 2, "stone_edge"), (3, 4, "stone_light"), (5, 6, "stone_dark"), (7, 31, "stone_mid")):
        shape(crown, "rect", [(0, start_y), (31, end_y)], tone)
    for start_x in (4, 20):
        shape(crown, "line", [(start_x, 2), (start_x + 4, 2)], "stone_light")
    frieze = new_tile("stone_mid")
    for start_y, tone in ((6, "stone_dark"), (7, "stone_edge"), (13, "stone_dark"), (14, "stone")):
        shape(frieze, "line", [(0, start_y), (31, start_y)], tone)
    for start_x in range(0, 32, 8):
        shape(frieze, "line", [(start_x, 10), (start_x + 2, 8), (start_x + 4, 10), (start_x + 6, 8)], "stone_dark")
        shape(frieze, "line", [(start_x + 1, 11), (start_x + 3, 9), (start_x + 5, 11)], "stone_light")
    soffit = dressed_tile(True)
    shape(soffit, "line", [(2, 4), (12, 4)], "stone_light")
    shape(soffit, "line", [(18, 27), (23, 27), (23, 28)], "stone_dark")
    dentil = new_tile("stone_light")
    for start_x in range(0, 32, 8):
        shape(dentil, "rect", [(start_x + 2, 6), (start_x + 5, 11)], "stone_edge")
        shape(dentil, "line", [(start_x + 2, 6), (start_x + 5, 6)], "stone_high")
        shape(dentil, "line", [(start_x + 5, 7), (start_x + 5, 11)], "stone")
        shape(dentil, "line", [(start_x + 2, 11), (start_x + 5, 11)], "stone_dark")
    backing = dressed_tile()
    shape(backing, "poly", [(9, 18), (13, 18), (13, 20), (12, 20), (12, 24), (10, 24), (10, 21), (9, 21)], "damp")
    shape(backing, "line", [(13, 19), (13, 22)], "stone_high")
    drip = new_tile("stone")
    for start_y, tone in ((14, "stone_edge"), (15, "stone_light"), (16, "stone_dark"), (17, "cavity"), (18, "stone_light")):
        shape(drip, "line", [(0, start_y), (31, start_y)], tone)
    shape(drip, "line", [(6, 14), (7, 15), (7, 17)], "rust")
    shape(drip, "line", [(24, 14), (24, 16)], "rust")
    return tuple(seal_edges(tile, axes) for tile, axes in ((crown, "x"), (frieze, "x"), (soffit, "xy"), (dentil, "x"), (backing, "x"), (drip, "x")))


def make_tiles():
    blue = brick_tile()
    crown, frieze, soffit, dentil, backing, drip = cornice_tiles()
    return (
        blue, damp_tile(blue), fractured_tile(blue), inlaid_tile(),
        dressed_tile(), shaft_tile("recess"), shaft_tile("rib"), pillar_end_tile(),
        crown, frieze, soffit, dentil, dressed_tile(True), shaft_tile("return"), backing, drip,
    )


def box(name, start, end, tile, faces=None):
    result = {"name": name, "from": list(start), "to": list(end), "tile": tile}
    if faces:
        result["faces"] = faces
    return result


def definition(identifier, chinese, english, kind, mount, ingredient, boxes, description, base=None):
    result = {
        "id": identifier,
        "name_zh": chinese,
        "name_en": english,
        "kind": kind,
        "mount": mount,
        "light": 0,
        "ingredient": f"minecraft:{ingredient}",
        "boxes": boxes,
        "description_zh": description,
    }
    if base:
        result["base"] = base
    return result


def make_definitions():
    definitions = [
        definition(EXPECTED_IDS[0], "冷蓝囚砌砖", "Blue Prison Bricks", "solid", "any", "stone_bricks", [box("continuous running-bond stone", (0, 0, 0), (16, 16, 16), 0)], "冷蓝错缝石砖；四面平铺和地面均可使用，无发光或附加行为。"),
        definition(EXPECTED_IDS[1], "渗灰囚砌砖", "Limewashed Damp Prison Bricks", "solid", "any", "moss_block", [box("mineral-streaked stone", (0, 0, 0), (16, 16, 16), 1)], "沿砖缝积聚的青灰潮痕与石灰挂流；仅为原创静态材质，不会蔓延。"),
        definition(EXPECTED_IDS[2], "锈补裂砌砖", "Iron-Stitched Fractured Prison Bricks", "solid", "any", "cracked_stone_bricks", [box("fractured and iron-stitched stone", (0, 0, 0), (16, 16, 16), 2)], "阶折裂口、崩角和少量锈铁补钉均以像素簇表达；实体仍为完整石砖。"),
        definition(EXPECTED_IDS[3], "菱嵌囚石地砖", "Brass-Inlaid Prison Tiles", "solid", "any", "copper_ingot", [box("four-slab inlaid paving block", (0, 0, 0), (16, 16, 16), 4, {"up": 3, "down": 12})], "黄铜菱形镶线与微量铜绿的四拼地砖，四侧为素切石；不发光，无符文机制。"),
        definition(EXPECTED_IDS[4], "冷蓝囚砖楼梯", "Blue Prison Brick Stairs", "stairs", "any", "stone_bricks", [box("native stair lower half preview", (0, 0, 0), (16, 8, 16), 0), box("native stair upper half preview", (0, 8, 8), (16, 16, 16), 0)], "原版楼梯形态族，直梯预览使用与冷蓝囚砌砖完全相同的底材；运行状态由主工程导出。", EXPECTED_IDS[0]),
        definition(EXPECTED_IDS[5], "冷蓝囚砖台阶", "Blue Prison Brick Slab", "slab", "any", "stone_bricks", [box("native bottom slab preview", (0, 0, 0), (16, 8, 16), 0)], "原版上、下、双台阶形态族，共用冷蓝囚砌砖底材；预览为下台阶。", EXPECTED_IDS[0]),
    ]
    rib_boxes = [box("continuous recessed core", (4, 0, 4), (12, 16, 12), 5, {"up": 7, "down": 7})]
    for side, axis_ranges in (
        ("north", (((4, 0, 2), (7, 16, 4)), ((9, 0, 2), (12, 16, 4)))),
        ("south", (((4, 0, 12), (7, 16, 14)), ((9, 0, 12), (12, 16, 14)))),
        ("west", (((2, 0, 4), (4, 16, 7)), ((2, 0, 9), (4, 16, 12)))),
        ("east", (((12, 0, 4), (14, 16, 7)), ((12, 0, 9), (14, 16, 12)))),
    ):
        for rib_index, (start, end) in enumerate(axis_ranges, 1):
            overrides = {"up": 7, "down": 7}
            for face in ("west", "east") if side in ("north", "south") else ("north", "south"):
                overrides[face] = 13
            rib_boxes.append(box(f"{side} structural flute rib {rib_index}", start, end, 6, overrides))
    definitions.append(definition(EXPECTED_IDS[6], "双槽囚石柱段", "Twin-Fluted Prison Pillar", "decor", "floor", "polished_andesite", rib_boxes, "九体块构成的双棱槽柱，四侧各有真实凹槽与退角；上下同截面，可竖向连续堆叠，无多余柱帽。"))
    cornice_boxes = [
        box("continuous projecting crown", (0, 13, 6), (16, 16, 16), 8, {"up": 10, "down": 10, "south": 14}),
        box("carved load-bearing frieze", (0, 9, 9), (16, 13, 16), 9, {"up": 10, "down": 10, "south": 14}),
        box("full-width wall mounting heel", (0, 0, 14), (16, 9, 16), 14, {"up": 10, "down": 10}),
        box("lower drip fillet", (0, 7, 12), (16, 9, 14), 15, {"up": 10, "down": 10, "west": 10, "east": 10}),
    ]
    for tooth_number, start_x in enumerate((1, 5, 9, 13), 1):
        cornice_boxes.append(box(f"separate stone dentil {tooth_number}", (start_x, 10, 7), (start_x + 2, 13, 9), 11, {"up": 10, "down": 10, "west": 10, "east": 10}))
    definitions.append(definition(EXPECTED_IDS[7], "齿饰囚墙檐", "Dentilled Prison Cornice", "decor", "wall", "smooth_stone", cornice_boxes, "背面齐平贴墙的阶层石檐与四枚实体齿饰；齿间留空，可横向连续拼接。无支撑检测或跨格承重机制。"))
    return definitions


def occupancy(boxes):
    cells = {}
    for box_index, element in enumerate(boxes):
        for cell_x in range(element["from"][0], element["to"][0]):
            for cell_y in range(element["from"][1], element["to"][1]):
                for cell_z in range(element["from"][2], element["to"][2]):
                    cell = (cell_x, cell_y, cell_z)
                    if cell in cells:
                        raise ValueError(f"Intersecting volumes at {cell}")
                    cells[cell] = box_index
    return cells


NORMALS = {
    "north": (0, 0, -1), "south": (0, 0, 1),
    "west": (-1, 0, 0), "east": (1, 0, 0),
    "down": (0, -1, 0), "up": (0, 1, 0),
}


def exposed_cells(boxes):
    occupied = occupancy(boxes)
    result = []
    for cell, box_index in occupied.items():
        for face, normal in NORMALS.items():
            neighbor = tuple(cell[axis] + normal[axis] for axis in range(3))
            if neighbor not in occupied:
                element = boxes[box_index]
                tile = element.get("faces", {}).get(face, element["tile"])
                result.append((cell, face, tile, box_index))
    return result


def world_point(cell, face, across, vertical):
    cell_x, cell_y, cell_z = cell
    if face in ("north", "south"):
        return cell_x + across, cell_y + vertical, cell_z + (face == "south")
    if face in ("west", "east"):
        return cell_x + (face == "east"), cell_y + vertical, cell_z + across
    return cell_x + across, cell_y + (face == "up"), cell_z + vertical


def tile_pixel(face, point):
    point_x, point_y, point_z = point
    if face == "north":
        horizontal, vertical = 16 - point_x, 16 - point_y
    elif face == "south":
        horizontal, vertical = point_x, 16 - point_y
    elif face == "east":
        horizontal, vertical = 16 - point_z, 16 - point_y
    elif face == "west":
        horizontal, vertical = point_z, 16 - point_y
    elif face == "up":
        horizontal, vertical = point_x, point_z
    else:
        horizontal, vertical = point_x, 16 - point_z
    return math.floor(horizontal * 2) % 32, math.floor(vertical * 2) % 32


def dot(first, second):
    return sum(first[axis] * second[axis] for axis in range(3))


def normalize(vector):
    length = math.sqrt(dot(vector, vector))
    return tuple(value / length for value in vector)


def render(boxes, tiles, size=(340, 225), eye=(1.25, 0.9, -1.65), background=(233, 238, 237, 255), scale=None):
    camera = normalize(eye)
    right = normalize((-camera[2], 0, camera[0])) if abs(camera[1]) < 0.99 else (1, 0, 0)
    upward = (
        right[1] * camera[2] - right[2] * camera[1],
        right[2] * camera[0] - right[0] * camera[2],
        right[0] * camera[1] - right[1] * camera[0],
    )
    projected = []
    for element in boxes:
        for point_x in (element["from"][0], element["to"][0]):
            for point_y in (element["from"][1], element["to"][1]):
                for point_z in (element["from"][2], element["to"][2]):
                    point = (point_x, point_y, point_z)
                    projected.append((dot(point, right), -dot(point, upward)))
    min_x, max_x = min(point[0] for point in projected), max(point[0] for point in projected)
    min_y, max_y = min(point[1] for point in projected), max(point[1] for point in projected)
    if scale is None:
        scale = min((size[0] - 32) / max(1, max_x - min_x), (size[1] - 26) / max(1, max_y - min_y))
    center_x = (size[0] - (max_x - min_x) * scale) / 2 - min_x * scale
    center_y = (size[1] - (max_y - min_y) * scale) / 2 - min_y * scale
    image = Image.new("RGBA", size, background)
    drawing = ImageDraw.Draw(image)
    micro_faces = []
    shade = {"up": 1.08, "north": 0.97, "west": 0.86, "east": 0.8, "south": 0.86, "down": 0.72}
    for cell, face, tile, _ in exposed_cells(boxes):
        if dot(NORMALS[face], camera) <= 0:
            continue
        for pixel_across in range(2):
            for pixel_vertical in range(2):
                across, vertical = pixel_across / 2, pixel_vertical / 2
                corners = [world_point(cell, face, across + offset_x, vertical + offset_y) for offset_x, offset_y in ((0, 0), (0.5, 0), (0.5, 0.5), (0, 0.5))]
                midpoint = world_point(cell, face, across + 0.25, vertical + 0.25)
                sample = tiles[tile].getpixel(tile_pixel(face, midpoint))
                color = tuple(min(255, round(channel * shade[face])) for channel in sample[:3]) + (255,)
                screen = [(round(center_x + dot(point, right) * scale, 4), round(center_y - dot(point, upward) * scale, 4)) for point in corners]
                micro_faces.append((dot(midpoint, camera), screen, color))
    for _, polygon, color in sorted(micro_faces, key=lambda entry: entry[0]):
        drawing.polygon(polygon, fill=color)
    return image


def translated(boxes, offset):
    result = []
    for element in boxes:
        copy = dict(element)
        copy["from"] = [element["from"][axis] + offset[axis] for axis in range(3)]
        copy["to"] = [element["to"][axis] + offset[axis] for axis in range(3)]
        result.append(copy)
    return result


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def make_tilemap():
    return {
        "schema_version": 1,
        "lane": "masonry",
        "atlas": "atlas.png",
        "size": [128, 128],
        "tile_size": 32,
        "columns": 4,
        "rows": 4,
        "opaque": True,
        "filter": "nearest",
        "texels_per_model_unit": 2,
        "uv_policy": "Block-local projected UV: preserve the position and orientation of a 16-unit cube face; crop each sub-box footprint, never stretch a whole tile onto a narrow rib or 3-unit cornice crown.",
        "java_face_projection": {
            "north": ["16-x", "16-y"], "south": ["x", "16-y"],
            "east": ["16-z", "16-y"], "west": ["z", "16-y"],
            "up": ["x", "z"], "down": ["x", "16-z"],
        },
        "java_atlas_uv": "tile origin in pixels / 8 + local projected model coordinate / 4; each 32px tile spans 4 Java UV units in the 128px atlas.",
        "native_base_tile": 0,
        "glowing_tiles": [],
        "palette": PALETTE,
        "tiles": [
            {
                "index": index, "tile": index, "id": identifier, "name_zh": chinese,
                "x": (index % 4) * 32, "y": (index // 4) * 32,
                "width": 32, "height": 32,
                "rect": [(index % 4) * 32, (index // 4) * 32, (index % 4 + 1) * 32, (index // 4 + 1) * 32],
                "repeat_axes": axes, "notes": notes,
            }
            for index, (identifier, chinese, axes, notes) in enumerate(TILE_SPECS)
        ],
    }


def validate(definitions, atlas, tilemap):
    checks = []

    def require(condition, description):
        if not condition:
            raise AssertionError(description)
        checks.append(description)

    require(isinstance(definitions, list) and len(definitions) == 8, "definitions.json is an array containing exactly 8 models")
    require(tuple(entry["id"] for entry in definitions) == EXPECTED_IDS, "all 8 assigned identifiers, no extras or renamed old-kit assets")
    require(atlas.mode == "RGBA" and atlas.size == (128, 128), "atlas.png is RGBA 128x128")
    require(atlas.getchannel("A").getextrema() == (255, 255), "all 16,384 atlas pixels are fully opaque")
    require(tilemap["native_base_tile"] == 0 and len(tilemap["tiles"]) == 16, "tilemap provides exactly 16 valid tiles and base tile 0")
    tiles = [atlas.crop(tuple(tile["rect"])) for tile in tilemap["tiles"]]
    require(len({tile.tobytes() for tile in tiles}) == 16, "all 16 tiles have distinct pixel content; no duplicate material tiles")
    used_tiles = set()
    used_texels = set()
    costs = []
    for entry in definitions:
        require(entry["kind"] in ("solid", "decor", "stairs", "slab"), f'{entry["id"]}: recognized block kind')
        require(entry["light"] == 0, f'{entry["id"]}: no light emission')
        require(1 <= len(entry["boxes"]) <= 12, f'{entry["id"]}: architecture budget <=12 boxes')
        for element in entry["boxes"]:
            require(len(element["from"]) == len(element["to"]) == 3, f'{entry["id"]}/{element["name"]}: three-component endpoints')
            require(all(type(coordinate) is int and 0 <= coordinate <= 16 for coordinate in element["from"] + element["to"]), f'{entry["id"]}/{element["name"]}: integer endpoints in 0..16')
            require(all(element["from"][axis] < element["to"][axis] for axis in range(3)), f'{entry["id"]}/{element["name"]}: positive-volume zero-rotation box')
            require("rotation" not in element and not element.get("glow", False), f'{entry["id"]}/{element["name"]}: no rotations or glow shells')
            face_tiles = [element["tile"]] + list(element.get("faces", {}).values())
            require(all(type(tile) is int and 0 <= tile < 16 for tile in face_tiles), f'{entry["id"]}/{element["name"]}: tile references in 0..15')
            require(set(element.get("faces", {})).issubset(FACES), f'{entry["id"]}/{element["name"]}: legal face override keys')
            used_tiles.update(face_tiles)
        occupied = occupancy(entry["boxes"])
        visible = exposed_cells(entry["boxes"])
        for cell, face, tile, _ in visible:
            for offset_across in (0.25, 0.75):
                for offset_vertical in (0.25, 0.75):
                    used_texels.add((tile, *tile_pixel(face, world_point(cell, face, offset_across, offset_vertical))))
        require(len(visible) == len({(cell, face) for cell, face, _, _ in visible}), f'{entry["id"]}: unique exposed face regions, no coincident visible duplicates')
        require(len(occupied) == sum(math.prod(element["to"][axis] - element["from"][axis] for axis in range(3)) for element in entry["boxes"]), f'{entry["id"]}: union volume equals box sum, no intersecting-volume boxes')
        if entry["mount"] == "floor":
            require(any(cell[1] == 0 for cell in occupied), f'{entry["id"]}: reaches floor y=0')
        if entry["mount"] == "wall":
            require(any(cell[2] == 15 for cell in occupied), f'{entry["id"]}: rear mounting surface reaches z=16')
        visible_faces = {(box_index, face) for _, face, _, box_index in visible}
        costs.append({
            "id": entry["id"], "preview_boxes": len(entry["boxes"]),
            "declared_faces_before_culling": len(entry["boxes"]) * 6,
            "box_faces_with_any_exposed_region": len(visible_faces),
            "fully_internal_box_faces": len(entry["boxes"]) * 6 - len(visible_faces),
            "exposed_area_model_units_squared": len(visible),
            "union_volume_model_units_cubed": len(occupied),
            "runtime_geometry": "parent-owned native shape family" if entry["kind"] in ("stairs", "slab") else "static cuboids",
        })
    require(used_tiles == set(range(16)), "all 16 atlas tiles are referenced; no unused decorative atlas cells")
    for index, tile in enumerate(tiles):
        axes = TILE_SPECS[index][2]
        if "x" in axes:
            require(all(tile.getpixel((0, pixel_y)) == tile.getpixel((31, pixel_y)) for pixel_y in range(32)), f"tile {index:02}: exact left/right boundary equality")
        if "y" in axes:
            require(all(tile.getpixel((pixel_x, 0)) == tile.getpixel((pixel_x, 31)) for pixel_x in range(32)), f"tile {index:02}: exact top/bottom boundary equality")
    for entry in definitions[4:6]:
        require(entry["base"] == EXPECTED_IDS[0] and all(element["tile"] == 0 and not element.get("faces") for element in entry["boxes"]), f'{entry["id"]}: exact base material matches blue bricks on every preview face')
    require(all(tiles[0].getpixel((pixel_x, 15)) == tiles[0].getpixel((pixel_x, 16)) for pixel_x in range(32)), "base tile half-height course crosses y=8 slab/stair split without a new material boundary")
    pillar = occupancy(definitions[6]["boxes"])
    lower_section = {(cell[0], cell[2]) for cell in pillar if cell[1] == 0}
    upper_section = {(cell[0], cell[2]) for cell in pillar if cell[1] == 15}
    require(lower_section == upper_section and len(lower_section) == 112, "pillar top/bottom are the same 112-unit section for vertical stacking")
    require({(15 - cell_z, cell_x) for cell_x, cell_z in lower_section} == lower_section, "pillar section has exact fourfold rotation symmetry")
    require((7, 8, 2) not in pillar and (7, 8, 4) in pillar, "pillar front central flute is real recessed geometry, not a dark decal")
    stacked_pillar = []
    for level in range(3):
        stacked_pillar.extend(translated(definitions[6]["boxes"], (0, level * 16, 0)))
    require(len(occupancy(stacked_pillar)) == len(pillar) * 3, "three pillar segments touch without overlap or missing-section gaps")
    cornice = occupancy(definitions[7]["boxes"])
    require({(cell[0], cell[1]) for cell in cornice if cell[2] == 15} == {(cell_x, cell_y) for cell_x in range(16) for cell_y in range(16)}, "cornice has a complete flush 16x16 rear wall-contact plane")
    require({(cell[1], cell[2]) for cell in cornice if cell[0] == 0} == {(cell[1], cell[2]) for cell in cornice if cell[0] == 15}, "cornice left/right section matches for continuous runs")
    require((3, 11, 8) not in cornice and (1, 11, 8) in cornice and (5, 11, 8) in cornice, "cornice dentils retain actual 2-unit open spacing")
    wall = [box("QA-only mounting wall", (0, 0, 16), (32, 16, 20), 14)]
    repeated_cornice = definitions[7]["boxes"] + translated(definitions[7]["boxes"], (16, 0, 0))
    require(len(occupancy(repeated_cornice + wall)) == len(cornice) * 2 + 32 * 16 * 4, "two cornices mount directly against a wall without penetration or horizontal overlap")
    return {
        "status": "PASS", "check_count": len(checks), "checks": checks, "models": costs,
        "totals": {"preview_boxes": sum(cost["preview_boxes"] for cost in costs), "declared_faces_before_culling": sum(cost["declared_faces_before_culling"] for cost in costs), "atlas_pixels": 16384, "base_rgba_bytes": 65536, "textures": 1, "texture_frames": 1},
        "texture_audit": {"opaque_pixels": 16384, "transparent_pixels": 0, "distinct_rgba_colors": len(atlas.getcolors(16385)), "used_tile_count": len(used_tiles), "unique_tile_count": 16, "source_projected_exposed_texels": len(used_texels), "source_projected_exposed_uv_fraction": round(len(used_texels) / 16384, 6), "assumption": "Projected UV documented in tilemap; source exposed surfaces only, not final exported resource UV."},
        "limitations": [
            "Source definition and CPU-render validation only; no claim of Blockbench/editor or Minecraft client testing.",
            "Final Java model UV, state families, culling, collision, registration and game behavior are owned by the parent integration.",
            "Software previews assume block-local projected UV at two texels per model unit, as documented in tilemap.json.",
            "Native stair/slab runtime state and waterlogging files are not authored or validated by this lane.",
            "Declared faces are pre-export six-per-box costs, not final Java quad counts or measured FPS.",
        ],
    }


def font(size, bold=False):
    candidates = [Path("C:/Windows/Fonts/msyhbd.ttc" if bold else "C:/Windows/Fonts/msyh.ttc"), Path("C:/Windows/Fonts/arial.ttf")]
    for candidate in candidates:
        if candidate.exists():
            return ImageFont.truetype(str(candidate), size)
    return ImageFont.load_default(size=size)


def tiled_image(tile, columns, rows, scale=1):
    image = Image.new("RGBA", (32 * columns, 32 * rows))
    for column in range(columns):
        for row in range(rows):
            image.paste(tile, (column * 32, row * 32))
    return image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)


def make_previews(definitions, tiles, report):
    work = ROOT / "work"
    views = work / "views"
    views.mkdir(parents=True, exist_ok=True)
    sheet = Image.new("RGBA", (1600, 1400), (21, 30, 40, 255))
    drawing = ImageDraw.Draw(sheet)
    drawing.text((32, 21), "FT / PRISON MASONRY", font=font(35, True), fill="#e3e8e6")
    drawing.text((34, 70), "冷蓝囚砌 · 原创砖石八件组     JAVA 1.20.1 / MASONRY 01—08", font=font(18), fill="#a0b7bc")
    card_background = (233, 238, 237, 255)
    view_directions = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
    orthographic = Image.new("RGBA", (1440, 1760), card_background)
    orthographic_draw = ImageDraw.Draw(orthographic)
    main_renders = []
    for model_index, entry in enumerate(definitions):
        card_x = 32 + (model_index % 4) * 388
        card_y = 116 + (model_index // 4) * 330
        drawing.rounded_rectangle((card_x, card_y, card_x + 375, card_y + 315), radius=7, fill=card_background)
        preview = render(entry["boxes"], tiles, (350, 216))
        preview.save(views / f'{entry["id"]}_three-quarter.png')
        main_renders.append(preview)
        sheet.paste(preview, (card_x + 12, card_y + 49))
        drawing.text((card_x + 17, card_y + 12), f'{model_index + 1:02}  {entry["name_zh"]}', font=font(21, True), fill="#253744")
        drawing.text((card_x + 17, card_y + 270), entry["id"], font=font(13), fill="#354c59")
        cost = report["models"][model_index]
        suffix = "原版形态族 / 仅预览计数" if entry["kind"] in ("stairs", "slab") else "结构盒 / 声明面（剔除前）"
        drawing.text((card_x + 17, card_y + 291), f'{cost["preview_boxes"]} / {cost["declared_faces_before_culling"]}  ·  {suffix}', font=font(11), fill="#59626a")
        for view_index, (view_name, eye) in enumerate(view_directions.items()):
            view = render(entry["boxes"], tiles, (232, 192), eye=eye)
            view.save(views / f'{entry["id"]}_{view_name}.png')
            orthographic.paste(view, (view_index * 240 + 4, model_index * 220 + 24))
            orthographic_draw.text((view_index * 240 + 10, model_index * 220 + 3), f'{model_index + 1:02} / {view_name}', font=font(13), fill="#354c59")
    orthographic.save(work / "orthographic-sheet.png")
    contrast_sheet = Image.new("RGBA", (1400, 480), (21, 30, 40, 255))
    contrast_draw = ImageDraw.Draw(contrast_sheet)
    for model_index, entry in enumerate(definitions):
        contrast_x = (model_index % 4) * 350
        contrast_y = (model_index // 4) * 240
        contrast_sheet.paste(render(entry["boxes"], tiles, (350, 210), background=(21, 30, 40, 255)), (contrast_x, contrast_y + 20))
        contrast_draw.text((contrast_x + 15, contrast_y), entry["name_zh"], font=font(15), fill="#bfcfd0")
    contrast_sheet.save(work / "dark-background-check.png")
    drawing.text((34, 797), "JOINERY / 平铺、叠接与挂墙实证", font=font(22, True), fill="#dce4e2")
    proof_labels = ("砖缝：3×2 连续平铺", "潮痕与阶折裂口：定点像素簇", "三段石柱：112 单位同截面", "两段檐口：背面齐平贴墙")
    for proof_index, label in enumerate(proof_labels):
        panel_x = 32 + proof_index * 388
        drawing.rounded_rectangle((panel_x, 840, panel_x + 375, 1112), radius=7, fill=card_background)
        drawing.text((panel_x + 16, 852), label, font=font(15, True), fill="#354c59")
    sheet.paste(tiled_image(tiles[0], 3, 2, 3), (76, 899))
    sheet.paste(tiles[1].resize((160, 160), Image.Resampling.NEAREST), (440, 912))
    sheet.paste(tiles[2].resize((160, 160), Image.Resampling.NEAREST), (615, 912))
    stacked = []
    for level in range(3):
        stacked.extend(translated(definitions[6]["boxes"], (0, 16 * level, 0)))
    stack_preview = render(stacked, tiles, (350, 225))
    stack_preview.save(work / "pillar-stack.png")
    sheet.paste(stack_preview, (820, 878))
    mounted = definitions[7]["boxes"] + translated(definitions[7]["boxes"], (16, 0, 0))
    mounted.append(box("QA-only wall, not part of any model", (0, -2, 16), (32, 20, 20), 14))
    mount_preview = render(mounted, tiles, (350, 225), eye=(1.25, 0.75, -1.65))
    mount_preview.save(work / "cornice-wall.png")
    sheet.paste(mount_preview, (1208, 878))
    drawing.text((34, 1140), "ATLAS / 16 原创不透明图块 · 32px × 16 · 最近邻采样", font=font(20, True), fill="#dce4e2")
    for tile_index, tile in enumerate(tiles):
        tile_x = 32 + tile_index * 96
        sheet.paste(tile.resize((90, 90), Image.Resampling.NEAREST), (tile_x, 1185))
        drawing.text((tile_x + 2, 1283), f"{tile_index:02}", font=font(14), fill="#a0b7bc")
    drawing.text((34, 1331), "24 预览结构盒 / 144 声明面（剔除前） · 无旋转、透明片、发光、噪点发生器或逐像素挤出", font=font(17), fill="#c4d1d0")
    drawing.text((34, 1365), "离线软件示意，非游戏截图；最终 Java UV、原版状态族与碰撞由主工程独立验收。", font=font(14), fill="#8ca4ad")
    sheet.convert("RGB").save(ROOT / "contact-sheet.png")
    seams = Image.new("RGBA", (1056, 520), card_background)
    seams_draw = ImageDraw.Draw(seams)
    for tile_index in range(4):
        seams.paste(tiled_image(tiles[tile_index], 2, 2, 4), (8 + tile_index * 264, 28))
        seams_draw.text((8 + tile_index * 264, 5), TILE_SPECS[tile_index][0], font=font(13), fill="#354c59")
        seams.paste(tiled_image(tiles[tile_index], 2, 1, 4), (8 + tile_index * 264, 318))
        seams_draw.text((8 + tile_index * 264, 292), "repeat boundary 32px / no filtering", font=font(11), fill="#354c59")
    seams.save(work / "tiling-checks.png")
    report["render_checks"] = {"software_three_quarter": 8, "software_orthographic": 48, "dark_background_comparison_models": 8, "pillar_segments": 3, "cornice_run_segments": 2, "model_view_images": 56, "client_screenshots": 0, "background_note": "Background contrast only, not a simulation of Minecraft darkness or light levels."}


def main():
    parser = argparse.ArgumentParser(description="Rebuild the original masonry lane in this directory only.")
    parser.add_argument("--check-only", action="store_true", help="Validate actual saved lane assets without overwriting them.")
    arguments = parser.parse_args()
    if arguments.check_only:
        definitions = json.loads((ROOT / "definitions.json").read_text(encoding="utf-8"))
        tilemap = json.loads((ROOT / "tilemap.json").read_text(encoding="utf-8"))
        atlas = Image.open(ROOT / "atlas.png")
        report = validate(definitions, atlas, tilemap)
    else:
        definitions = make_definitions()
        tiles = make_tiles()
        atlas = Image.new("RGBA", (128, 128), rgba("stone_dark"))
        for tile_index, tile in enumerate(tiles):
            atlas.paste(tile, ((tile_index % 4) * 32, (tile_index // 4) * 32))
        tilemap = make_tilemap()
        report = validate(definitions, atlas, tilemap)
        write_json(ROOT / "definitions.json", definitions)
        write_json(ROOT / "tilemap.json", tilemap)
        atlas.save(ROOT / "atlas.png")
        make_previews(definitions, tiles, report)
        saved_report = validate(json.loads((ROOT / "definitions.json").read_text(encoding="utf-8")), Image.open(ROOT / "atlas.png"), json.loads((ROOT / "tilemap.json").read_text(encoding="utf-8")))
        report["saved_asset_revalidation"] = saved_report["status"]
        report["sha256"] = {name: hashlib.sha256((ROOT / name).read_bytes()).hexdigest() for name in ("definitions.json", "atlas.png", "tilemap.json", "contact-sheet.png")}
        write_json(ROOT / "work" / "qa.json", report)
    print(json.dumps({"status": report["status"], "checks": report["check_count"], "totals": report["totals"], "costs": report["models"]}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
