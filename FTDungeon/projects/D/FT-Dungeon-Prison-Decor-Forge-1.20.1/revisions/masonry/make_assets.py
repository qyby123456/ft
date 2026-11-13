from __future__ import annotations

import argparse
import hashlib
import io
import itertools
import json
import math
from collections import deque
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, __version__ as pillow_version


ROOT = Path(__file__).resolve().parent
PALETTE = {
    "cavity": "#141d27",
    "stone_dark": "#253744",
    "stone": "#354c59",
    "stone_mid": "#405965",
    "stone_light": "#4e6570",
    "stone_edge": "#607781",
    "stone_high": "#74858b",
    "mineral": "#8d6b3c",
    "mineral_light": "#9b8260",
    "damp": "#36565c",
}
FACE_AXES = {
    "north": (2, -1, (0, 1)),
    "south": (2, 1, (0, 1)),
    "west": (0, -1, (2, 1)),
    "east": (0, 1, (2, 1)),
    "up": (1, 1, (0, 2)),
    "down": (1, -1, (0, 2)),
}
SHADES = {"north": 0.94, "south": 0.84, "west": 0.86, "east": 0.78, "up": 1.0, "down": 0.64}
TILE_SPECS = [
    ("slab_flagstone_top", "Carved flagstone top", "xz", "Four large inset flagstones; continuous cut border, no brick courses."),
    ("slab_quarried_bottom", "Quarried slab underside", "xz", "Broad quarry marks and restrained mineral seams; distinct from top."),
    ("slab_carved_edge", "Native slab carved edge", "x", "Identical 16px-high halves: dedicated bevel and incised edge for both native slab halves."),
    ("pillar_plinth", "Pillar plinth bedding", "x", "Plinth y0..2 maps to rows 28..31; continuous dressed bevel."),
    ("pillar_collar", "Pillar shoulder and capital cushion", "x", "Bands explicitly placed at rows 4..7 and 26..27, matching component heights."),
    ("pillar_shaft", "Raised shaft arrises", "y", "Long grouped tooling strokes on the four solid shaft corners."),
    ("pillar_flute", "Recessed flute beds and returns", "y", "Dark stone only on physically recessed beds and flute returns."),
    ("pillar_horizontal", "Pillar dressed horizontal cuts", "xz", "Quiet concentric tooling, shared by base and capital horizontal faces."),
    ("pillar_abacus", "Capital abacus fascia", "x", "Rows 0..3 form the y14..16 upper abacus; no fake projecting texture geometry."),
    ("cornice_coping", "Cornice projecting coping", "x", "Rows 0..3: restrained bright coping above a shadow cut."),
    ("cornice_step", "Cornice second moulding", "x", "Rows 4..7: a carved ogee band on the second real geometric tier."),
    ("cornice_frieze", "Cornice recessed frieze", "x", "Rows 8..11: broad repeated chisel notches between paired structural corbels."),
    ("cornice_corbel", "Corbel fronts", "y", "Long channel-shaped cuts at the two corbel x footprints, not per-pixel geometry."),
    ("cornice_horizontal", "Cornice upper and soffit cuts", "xz", "Dressed horizontal stone with a drainage joint and a few chipped edges."),
    ("cornice_wall_heel", "Paired wall mounting heels", "y", "Two narrow structural heels, not a full-width solid backboard."),
    ("cornice_corbel_return", "Corbel side returns", "y", "Subdued split-stone tooling on the stepped corbel sides."),
]


def box(name, lower, upper, tile, **faces):
    result = {"name": name, "from": list(lower), "to": list(upper), "tile": tile}
    if faces:
        result["faces"] = faces
    return result


def make_definitions():
    slab = {
        "id": "prison_brick_slab",
        "name_zh": "雕边囚石台阶",
        "name_en": "Carved Prison Flagstone Slab",
        "kind": "slab", "mount": "any", "light": 0,
        "ingredient": "minecraft:stone_bricks", "base": "prison_blue_bricks",
        "native_tiles": {"top": 0, "bottom": 1, "side": 2},
        "boxes": [box("native bottom slab with dedicated flagstone materials", (0, 0, 0), (16, 8, 16), 2, up=0, down=1)],
        "description_zh": "原生上、下、双层台阶；全新大块石板面、独立凿刻底面与双半高雕边侧材。预览仅为下台阶，原生状态由父工程接入。",
    }
    horizontal = {"up": 7, "down": 7}
    pillar_boxes = [
        box("broad dressed plinth", (1, 0, 1), (15, 2, 15), 3, **horizontal),
        box("inset base shoulder", (3, 2, 3), (13, 3, 13), 4, **horizontal),
        box("northwest carved shaft corner", (4, 3, 4), (7, 12, 7), 5, east=6, south=6, **horizontal),
        box("northeast carved shaft corner", (9, 3, 4), (12, 12, 7), 5, west=6, south=6, **horizontal),
        box("southwest carved shaft corner", (4, 3, 9), (7, 12, 12), 5, east=6, north=6, **horizontal),
        box("southeast carved shaft corner", (9, 3, 9), (12, 12, 12), 5, west=6, north=6, **horizontal),
        box("north recessed flute bed", (7, 3, 6), (9, 12, 7), 6, **horizontal),
        box("south recessed flute bed", (7, 3, 9), (9, 12, 10), 6, **horizontal),
        box("central web with east and west flute beds", (6, 3, 7), (10, 12, 9), 6, **horizontal),
        box("capital neck collar", (3, 12, 3), (13, 13, 13), 4, **horizontal),
        box("capital cushion step", (2, 13, 2), (14, 14, 14), 4, **horizontal),
        box("broad stacking abacus", (1, 14, 1), (15, 16, 15), 8, **horizontal),
    ]
    pillar = {
        "id": "prison_fluted_pillar", "name_zh": "四槽基冠囚石柱",
        "name_en": "Capped Fluted Prison Pillar", "kind": "decor",
        "mount": "floor", "light": 0, "ingredient": "minecraft:polished_andesite",
        "boxes": pillar_boxes,
        "description_zh": "全新基座、肩台、颈圈、阶叠柱冠；方形柱身四面各有宽2、深2的真实竖槽。上下14×14接触面一致，可堆叠但不提供自动连接状态。",
    }
    cornice_boxes = [
        box("full-width projecting coping", (0, 14, 2), (16, 16, 16), 9, up=13, down=13, south=14),
        box("receding carved moulding", (0, 12, 4), (16, 14, 16), 10, up=13, down=13, south=14),
        box("load-bearing recessed frieze", (0, 10, 7), (16, 12, 16), 11, up=13, down=13, south=14),
    ]
    for label, start, end in (("left", 2, 5), ("right", 11, 14)):
        cornice_boxes.extend([
            box(f"{label} narrow wall heel", (start, 0, 14), (end, 10, 16), 14, up=13, down=13, west=15, east=15),
            box(f"{label} corbel foot", (start, 0, 12), (end, 4, 14), 12, up=13, down=13, west=15, east=15),
            box(f"{label} corbel middle shoulder", (start, 4, 10), (end, 7, 14), 12, up=13, down=13, west=15, east=15),
            box(f"{label} corbel projecting head", (start, 7, 7), (end, 10, 14), 12, up=13, down=13, west=15, east=15),
        ])
    cornice = {
        "id": "prison_cornice", "name_zh": "双牛腿阶叠石檐",
        "name_en": "Paired-Corbel Prison Cornice", "kind": "decor", "mount": "wall",
        "light": 0, "ingredient": "minecraft:smooth_stone", "boxes": cornice_boxes,
        "description_zh": "全新三层前伸阶檐、左右各三段承托牛腿及两条独立贴墙后跟；中央宽6、高10的负空间贯穿至背面。无整片背板，无跨格或支撑检测机制。",
    }
    return [slab, pillar, cornice]


def stone_sheet(base="stone_mid", variant=0):
    image = Image.new("RGBA", (32, 32), PALETTE[base])
    draw = ImageDraw.Draw(image)
    clusters = [(3, 7, 8, 10), (18, 3, 24, 5), (22, 18, 28, 22), (8, 22, 14, 25)]
    for ordinal, (left, top, right, bottom) in enumerate(clusters):
        shift = (variant * 3 + ordinal) % 4
        draw.rectangle((left, top + shift, right, bottom + shift), fill=PALETTE["stone_light" if ordinal % 2 else "stone"])
        draw.line((left + 1, bottom + shift, right - 2, bottom + shift), fill=PALETTE["stone_edge"])
    return image


def make_tiles():
    tiles = []
    top = Image.new("RGBA", (32, 32), PALETTE["stone_light"])
    draw = ImageDraw.Draw(top)
    draw.rectangle((0, 0, 31, 31), outline=PALETTE["stone_edge"])
    draw.rectangle((1, 1, 30, 30), outline=PALETTE["stone_high"])
    draw.rectangle((3, 3, 28, 28), fill=PALETTE["stone_dark"])
    panels = [(4, 4, 11, 14), (13, 4, 27, 14), (4, 16, 19, 27), (21, 16, 27, 27)]
    for ordinal, (left, upper, right, bottom) in enumerate(panels):
        draw.rectangle((left, upper, right, bottom), fill=PALETTE["stone_mid" if ordinal % 2 else "stone_light"])
        draw.line((left, bottom, right, bottom), fill=PALETTE["stone"])
        draw.line((left, upper, right - 1, upper), fill=PALETTE["stone_edge"])
        if right - left > 8:
            draw.line((left + 3, upper + 4, right - 3, upper + 4), fill=PALETTE["stone"])
            draw.line((left + 4, upper + 5, right - 2, upper + 5), fill=PALETTE["stone_light"])
    draw.line((5, 7, 7, 7), fill=PALETTE["stone"])
    draw.line((23, 22, 25, 22), fill=PALETTE["stone"])
    for left, upper in ((0, 0), (29, 29), (0, 28), (28, 0)):
        draw.rectangle((left, upper, left + 1, upper + 2), fill=PALETTE["stone_dark"])
    draw.line((25, 2, 27, 2), fill=PALETTE["mineral"])
    draw.point((26, 3), fill=PALETTE["mineral_light"])
    tiles.append(top)
    bottom = stone_sheet("stone", 1)
    draw = ImageDraw.Draw(bottom)
    draw.rectangle((1, 1, 30, 30), outline=PALETTE["stone_dark"])
    for upper, left, length in ((6, 5, 8), (8, 6, 6), (21, 18, 9), (24, 20, 6)):
        draw.line((left, upper, left + length, upper), fill=PALETTE["stone_edge"])
    draw.line([(5, 27), (9, 27), (9, 25), (12, 25)], fill=PALETTE["mineral"])
    tiles.append(bottom)
    edge = Image.new("RGBA", (32, 32), PALETTE["stone_light"])
    draw = ImageDraw.Draw(edge)
    for upper in (0, 16):
        for offset, color in ((0, "stone_high"), (1, "stone_edge"), (2, "stone_dark"), (3, "stone"), (12, "stone_dark"), (13, "stone_edge"), (14, "stone_mid"), (15, "stone_dark")):
            draw.line((0, upper + offset, 31, upper + offset), fill=PALETTE[color])
        for center in (7, 23):
            draw.line([(center - 2, upper + 7), (center, upper + 5), (center + 2, upper + 7), (center, upper + 9), (center - 2, upper + 7)], fill=PALETTE["stone_dark"])
            draw.point((center + 1, upper + 8), fill=PALETTE["stone_high"])
        draw.line((12, upper + 8, 17, upper + 8), fill=PALETTE["stone"])
        draw.line((13, upper + 9, 16, upper + 9), fill=PALETTE["stone_edge"])
        draw.line((28, upper + 4, 30, upper + 4), fill=PALETTE["mineral"])
    tiles.append(edge)
    plinth = stone_sheet("stone_light", 2)
    draw = ImageDraw.Draw(plinth)
    for upper, color in ((28, "stone_high"), (29, "stone_light"), (30, "stone_mid"), (31, "stone_dark")):
        draw.line((0, upper, 31, upper), fill=PALETTE[color])
    draw.line((3, 30, 6, 30), fill=PALETTE["mineral"])
    draw.line((25, 29, 27, 29), fill=PALETTE["stone"])
    tiles.append(plinth)
    collar = stone_sheet("stone_light", 3)
    draw = ImageDraw.Draw(collar)
    for upper, color in ((4, "stone_high"), (5, "stone_mid"), (6, "stone_edge"), (7, "stone_dark"), (26, "stone_high"), (27, "stone_mid")):
        draw.line((0, upper, 31, upper), fill=PALETTE[color])
    draw.line((6, 5, 8, 5), fill=PALETTE["stone"])
    draw.line((24, 26, 26, 26), fill=PALETTE["mineral"])
    tiles.append(collar)
    shaft = Image.new("RGBA", (32, 32), PALETTE["stone_light"])
    draw = ImageDraw.Draw(shaft)
    for left, upper, lower in ((8, 8, 24), (11, 10, 22), (19, 7, 20), (22, 12, 25)):
        draw.line((left, upper, left, lower), fill=PALETTE["stone"])
        draw.line((left + 1, upper + 2, left + 1, lower - 2), fill=PALETTE["stone_edge"])
    draw.line((10, 19, 10, 21), fill=PALETTE["mineral"])
    draw.line((23, 9, 23, 11), fill=PALETTE["stone_high"])
    tiles.append(shaft)
    flute = Image.new("RGBA", (32, 32), PALETTE["stone_dark"])
    draw = ImageDraw.Draw(flute)
    for left in (9, 13, 18, 22):
        draw.line((left, 7, left, 24), fill=PALETTE["stone"])
    draw.line((15, 11, 15, 21), fill=PALETTE["cavity"])
    draw.line((17, 8, 17, 22), fill=PALETTE["stone_mid"])
    draw.line((9, 20, 9, 23), fill=PALETTE["damp"])
    tiles.append(flute)
    horizontal = stone_sheet("stone_mid", 4)
    draw = ImageDraw.Draw(horizontal)
    draw.rectangle((3, 3, 28, 28), outline=PALETTE["stone_edge"])
    draw.rectangle((5, 5, 26, 26), outline=PALETTE["stone"])
    draw.line((4, 18, 4, 21), fill=PALETTE["mineral"])
    tiles.append(horizontal)
    abacus = stone_sheet("stone_light", 5)
    draw = ImageDraw.Draw(abacus)
    for upper, color in ((0, "stone_high"), (1, "stone_light"), (2, "stone_edge"), (3, "stone_dark")):
        draw.line((0, upper, 31, upper), fill=PALETTE[color])
    draw.line((5, 1, 7, 1), fill=PALETTE["stone_mid"])
    draw.line((26, 2, 28, 2), fill=PALETTE["mineral"])
    tiles.append(abacus)
    coping = stone_sheet("stone_light", 6)
    draw = ImageDraw.Draw(coping)
    for upper, color in ((0, "stone_high"), (1, "stone_light"), (2, "stone_edge"), (3, "stone_dark")):
        draw.line((0, upper, 31, upper), fill=PALETTE[color])
    draw.line((3, 1, 5, 1), fill=PALETTE["stone"])
    draw.line((24, 2, 27, 2), fill=PALETTE["mineral"])
    tiles.append(coping)
    step = stone_sheet("stone_mid", 7)
    draw = ImageDraw.Draw(step)
    for upper, color in ((4, "stone_dark"), (5, "stone_edge"), (6, "stone_light"), (7, "stone")):
        draw.line((0, upper, 31, upper), fill=PALETTE[color])
    draw.line((7, 6, 10, 6), fill=PALETTE["stone_mid"])
    draw.line((20, 5, 22, 5), fill=PALETTE["stone_high"])
    tiles.append(step)
    frieze = stone_sheet("stone_mid", 8)
    draw = ImageDraw.Draw(frieze)
    draw.rectangle((0, 8, 31, 11), fill=PALETTE["stone_light"])
    for left in (3, 11, 19, 27):
        draw.line((left, 9, left + 2, 9), fill=PALETTE["stone_dark"])
        draw.point((left + 2, 10), fill=PALETTE["stone_high"])
    draw.line((0, 11, 31, 11), fill=PALETTE["stone"])
    tiles.append(frieze)
    corbel = stone_sheet("stone_light", 9)
    draw = ImageDraw.Draw(corbel)
    for left in (4, 22):
        draw.line([(left, 30), (left, 15), (left + 1, 13), (left + 3, 13), (left + 4, 15), (left + 4, 17), (left + 2, 17)], fill=PALETTE["stone"])
        draw.line((left + 1, 20, left + 1, 27), fill=PALETTE["stone_edge"])
        draw.line((left + 4, 25, left + 4, 27), fill=PALETTE["mineral"])
    tiles.append(corbel)
    soffit = stone_sheet("stone_mid", 10)
    draw = ImageDraw.Draw(soffit)
    draw.line((0, 4, 31, 4), fill=PALETTE["stone_edge"])
    draw.line((0, 6, 31, 6), fill=PALETTE["stone"])
    draw.line((15, 8, 15, 31), fill=PALETTE["stone"])
    draw.line((16, 8, 16, 31), fill=PALETTE["stone_edge"])
    draw.line((25, 9, 27, 9), fill=PALETTE["mineral"])
    tiles.append(soffit)
    heel = stone_sheet("stone", 11)
    draw = ImageDraw.Draw(heel)
    for left in (5, 23):
        draw.line((left, 14, left, 29), fill=PALETTE["stone_mid"])
        draw.line((left + 1, 24, left + 1, 30), fill=PALETTE["damp"])
    tiles.append(heel)
    returns = stone_sheet("stone_mid", 12)
    draw = ImageDraw.Draw(returns)
    draw.line([(8, 15), (11, 15), (11, 17), (14, 17)], fill=PALETTE["stone"])
    draw.line((12, 23, 17, 23), fill=PALETTE["stone_edge"])
    draw.line((7, 29, 10, 29), fill=PALETTE["mineral"])
    tiles.append(returns)
    return tiles


def make_atlas(tiles):
    atlas = Image.new("RGBA", (128, 128), PALETTE["stone"])
    for index, tile in enumerate(tiles):
        atlas.paste(tile, ((index % 4) * 32, (index // 4) * 32))
    return atlas


def make_tilemap():
    return {
        "schema_version": 1, "lane": "masonry", "revision": "remake-06-08-20261103",
        "atlas": "atlas.png", "size": [128, 128], "tile_size": 32,
        "columns": 4, "rows": 4, "opaque": True, "filter": "nearest",
        "texels_per_model_unit": 2,
        "uv_policy": "Block-local projected UV, 2 texels per model unit. Crop sub-box footprints; never stretch an entire tile across a small component.",
        "java_face_projection": {"north": ["16-x", "16-y"], "south": ["x", "16-y"], "east": ["16-z", "16-y"], "west": ["z", "16-y"], "up": ["x", "z"], "down": ["x", "16-z"]},
        "java_atlas_uv": "tile pixel origin / 8 + projected model coordinate / 4; each tile occupies 4x4 Java UV units in the 128px atlas.",
        "native_slab_tiles": {"prison_brick_slab": {"top": 0, "bottom": 1, "side": 2}},
        "native_slab_side_policy": "Bottom y0..8 samples rows16..31; top y8..16 samples rows0..15. These halves are identical. Double y0..16 shows the intended two-course edge joint.",
        "glowing_tiles": [], "palette": PALETTE,
        "tiles": [
            {"index": index, "tile": index, "id": spec[0], "name_en": spec[1], "x": (index % 4) * 32, "y": (index // 4) * 32, "width": 32, "height": 32, "rect": [(index % 4) * 32, (index // 4) * 32, (index % 4 + 1) * 32, (index // 4 + 1) * 32], "repeat_axes": spec[2], "notes": spec[3]}
            for index, spec in enumerate(TILE_SPECS)
        ],
    }


def projected_uv(face, point):
    horizontal, height, depth = point
    return {
        "north": (16 - horizontal, 16 - height),
        "south": (horizontal, 16 - height),
        "east": (16 - depth, 16 - height),
        "west": (depth, 16 - height),
        "up": (horizontal, depth),
        "down": (horizontal, 16 - depth),
    }[face]


def face_vertices(component, face):
    left, lower, front = component["from"]
    right, upper, back = component["to"]
    return {
        "north": [(left, lower, front), (right, lower, front), (right, upper, front), (left, upper, front)],
        "south": [(right, lower, back), (left, lower, back), (left, upper, back), (right, upper, back)],
        "west": [(left, lower, back), (left, lower, front), (left, upper, front), (left, upper, back)],
        "east": [(right, lower, front), (right, lower, back), (right, upper, back), (right, upper, front)],
        "up": [(left, upper, front), (right, upper, front), (right, upper, back), (left, upper, back)],
        "down": [(left, lower, back), (right, lower, back), (right, lower, front), (left, lower, front)],
    }[face]


def dot(first, second):
    return sum(first_value * second_value for first_value, second_value in zip(first, second))


def render_model(definition, tiles, size=256, yaw=35, elevation=28, gray=False):
    yaw_radians = math.radians(yaw)
    elevation_radians = math.radians(elevation)
    view = (math.cos(elevation_radians) * math.sin(yaw_radians), math.sin(elevation_radians), -math.cos(elevation_radians) * math.cos(yaw_radians))
    right = (math.cos(yaw_radians), 0, math.sin(yaw_radians))
    upward = (-math.sin(elevation_radians) * math.sin(yaw_radians), math.cos(elevation_radians), math.sin(elevation_radians) * math.cos(yaw_radians))
    image = Image.new("RGBA", (size, size), "#1b2934")
    output = image.load()
    depth_buffer = [-math.inf] * (size * size)
    scale = size / 26

    def project(point, face):
        centered = tuple(coordinate - 8 for coordinate in point)
        texel_u, texel_v = projected_uv(face, point)
        return (size / 2 + dot(centered, right) * scale, size * 0.51 - dot(centered, upward) * scale, dot(centered, view), texel_u, texel_v)

    def triangle(vertices, texture, shade):
        first, second, third = vertices
        denominator = (second[1] - third[1]) * (first[0] - third[0]) + (third[0] - second[0]) * (first[1] - third[1])
        if abs(denominator) < 1e-10:
            return
        minimum_x = max(0, math.floor(min(vertex[0] for vertex in vertices)))
        maximum_x = min(size - 1, math.ceil(max(vertex[0] for vertex in vertices)))
        minimum_y = max(0, math.floor(min(vertex[1] for vertex in vertices)))
        maximum_y = min(size - 1, math.ceil(max(vertex[1] for vertex in vertices)))
        texture_pixels = texture.load()
        for pixel_y in range(minimum_y, maximum_y + 1):
            sample_y = pixel_y + 0.5
            for pixel_x in range(minimum_x, maximum_x + 1):
                sample_x = pixel_x + 0.5
                first_weight = ((second[1] - third[1]) * (sample_x - third[0]) + (third[0] - second[0]) * (sample_y - third[1])) / denominator
                second_weight = ((third[1] - first[1]) * (sample_x - third[0]) + (first[0] - third[0]) * (sample_y - third[1])) / denominator
                third_weight = 1 - first_weight - second_weight
                if min(first_weight, second_weight, third_weight) < -1e-8:
                    continue
                weights = (first_weight, second_weight, third_weight)
                depth = sum(weight * vertex[2] for weight, vertex in zip(weights, vertices))
                buffer_index = pixel_y * size + pixel_x
                if depth <= depth_buffer[buffer_index] + 1e-8:
                    continue
                depth_buffer[buffer_index] = depth
                if gray:
                    color = (112, 129, 139, 255)
                else:
                    texel_u = sum(weight * vertex[3] for weight, vertex in zip(weights, vertices))
                    texel_v = sum(weight * vertex[4] for weight, vertex in zip(weights, vertices))
                    color = texture_pixels[max(0, min(31, math.floor(texel_u * 2))), max(0, min(31, math.floor(texel_v * 2)))]
                output[pixel_x, pixel_y] = tuple(round(channel * shade) for channel in color[:3]) + (255,)

    for component in definition["boxes"]:
        for face, (axis, sign, unused_axes) in FACE_AXES.items():
            if view[axis] * sign <= 1e-8:
                continue
            tile_index = component.get("faces", {}).get(face, component["tile"])
            vertices = [project(point, face) for point in face_vertices(component, face)]
            for indices in ((0, 1, 2), (0, 2, 3)):
                triangle([vertices[index] for index in indices], tiles[tile_index], SHADES[face])
    return image, sum(depth > -math.inf for depth in depth_buffer)


def image_bytes(image):
    stream = io.BytesIO()
    image.save(stream, format="PNG", optimize=False, compress_level=9)
    return stream.getvalue()


def json_bytes(value):
    return (json.dumps(value, ensure_ascii=False, indent=2) + "\n").encode("utf-8")


def occupied_cells(definition):
    owners = {}
    overlaps = []
    for index, component in enumerate(definition["boxes"]):
        for cell in itertools.product(*(range(lower, upper) for lower, upper in zip(component["from"], component["to"]))):
            if cell in owners:
                overlaps.append({"cell": cell, "boxes": [owners[cell], index]})
            owners[cell] = index
    return owners, overlaps


def audit_geometry(definition):
    components = definition["boxes"]
    assert 0 < len(components) <= 12
    for component in components:
        assert all(type(coordinate) is int and 0 <= coordinate <= 16 for coordinate in component["from"] + component["to"])
        assert all(lower < upper for lower, upper in zip(component["from"], component["to"]))
        assert "rotation" not in component
        assert 0 <= component["tile"] < 16
        assert all(face in FACE_AXES and type(tile) is int and 0 <= tile < 16 for face, tile in component.get("faces", {}).items())
        assert not component.get("glow", False)
        for face in FACE_AXES:
            for point in face_vertices(component, face):
                assert all(0 <= coordinate <= 16 for coordinate in projected_uv(face, point))
    owners, overlaps = occupied_cells(definition)
    assert not overlaps
    contacts = {index: set() for index in range(len(components))}
    exposed_units = [0] * len(components)
    exterior_planes = set()
    duplicate_visible_planes = 0
    face_coverage = {(index, face): [0, 0] for index in range(len(components)) for face in FACE_AXES}
    for cell, owner in owners.items():
        component = components[owner]
        for face, (axis, sign, transverse) in FACE_AXES.items():
            neighbor = list(cell)
            neighbor[axis] += sign
            other_owner = owners.get(tuple(neighbor))
            if other_owner is not None and other_owner != owner:
                contacts[owner].add(other_owner)
            at_boundary = cell[axis] == (component["from"][axis] if sign < 0 else component["to"][axis] - 1)
            if at_boundary:
                face_coverage[(owner, face)][0] += 1
                face_coverage[(owner, face)][1] += int(other_owner is None)
            if other_owner is None:
                exposed_units[owner] += 1
                plane_key = (axis, sign, cell[axis] + int(sign > 0), cell[transverse[0]], cell[transverse[1]])
                duplicate_visible_planes += int(plane_key in exterior_planes)
                exterior_planes.add(plane_key)
    reached = {0}
    pending = deque([0])
    while pending:
        for neighbor in contacts[pending.popleft()]:
            if neighbor not in reached:
                reached.add(neighbor)
                pending.append(neighbor)
    assert len(reached) == len(components)
    assert all(exposed_units) and duplicate_visible_planes == 0
    if definition["mount"] == "wall":
        assert any(component["to"][2] == 16 for component in components)
    else:
        assert min(component["from"][1] for component in components) == 0
    fully_hidden_faces = sum(exposed == 0 for total, exposed in face_coverage.values())
    result = {
        "id": definition["id"], "boxes": len(components), "box_budget": 12,
        "raw_declared_faces": len(components) * 6,
        "fully_occluded_box_faces_parent_may_prune": fully_hidden_faces,
        "visible_or_partly_visible_box_faces": len(components) * 6 - fully_hidden_faces,
        "exterior_unit_square_area": sum(exposed_units), "volume_unit_cubes": len(owners),
        "integer_grid_0_to_16": True, "positive_volumes": True, "zero_rotations": True,
        "overlapping_volume_units": 0, "duplicate_visible_unit_planes": duplicate_visible_planes,
        "face_connected_components": 1, "floating_boxes": [],
        "each_box_has_exterior_contribution": True,
        "mount_interface_passed": True, "uv_projection_in_tile_bounds": True,
    }
    if definition["id"] == "prison_brick_slab":
        assert definition["native_tiles"] == {"top": 0, "bottom": 1, "side": 2}
        assert components[0]["from"] == [0, 0, 0] and components[0]["to"] == [16, 8, 16]
        result["native_slab_preview"] = "one 16x8x16 box; upper and double states are integration work, not exported here"
    if definition["id"] == "prison_fluted_pillar":
        gaps = [((7, 3, 4), (9, 12, 6)), ((7, 3, 10), (9, 12, 12)), ((4, 3, 7), (6, 12, 9)), ((10, 3, 7), (12, 12, 9))]
        for lower, upper in gaps:
            assert all(cell not in owners for cell in itertools.product(*(range(start, end) for start, end in zip(lower, upper))))
        lower_interface = {(horizontal, depth) for horizontal, height, depth in owners if height == 0}
        upper_interface = {(horizontal, depth) for horizontal, height, depth in owners if height == 15}
        assert lower_interface == upper_interface and len(lower_interface) == 196
        result["four_real_flutes"] = {"width": 2, "depth": 2, "height": 9, "empty_volume_units_total": 144}
        result["stacking_interface"] = {"top_equals_bottom": True, "size": [14, 14], "area": 196}
    if definition["id"] == "prison_cornice":
        assert all((horizontal, height, depth) not in owners for horizontal in range(5, 11) for height in range(10) for depth in range(16))
        result["central_through_negative_space"] = {"bounds": [[5, 0, 0], [11, 10, 16]], "width": 6, "height": 10, "empty_volume_units": 960, "passed": True}
        result["three_coping_front_positions"] = [2, 4, 7]
        result["paired_structural_corbels"] = True
    return result


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/segoeui.ttf", size)


def make_contact_sheet(definitions, renders):
    sheet = Image.new("RGBA", (1600, 1120), "#101923")
    draw = ImageDraw.Draw(sheet)
    draw.text((28, 20), "THE FORGOTTEN CELLS / MASONRY REMAKE", font=font(29), fill="#e0e9e9")
    draw.text((30, 67), "06-08 only  |  New authored geometry + opaque pixel atlas  |  Java 1.20.1 / Forge integration pending", font=font(17), fill="#91a8b6")
    columns = (344, 652, 960, 1268)
    for horizontal, label in zip(columns, ("FRONT", "RIGHT SIDE", "BACK", "FRONT 3/4")):
        draw.text((horizontal + 10, 112), label, font=font(15), fill="#b0c6d1")
    captions = [
        ("06", "CARVED FLAGSTONE SLAB", "1 box / 6 raw faces", "Dedicated top / bottom / side", "Native upper + double: parent"),
        ("07", "CAPPED FLUTED PILLAR", "12 boxes / 72 raw faces", "Four physical 2x2 flutes", "Matching 14x14 stacking ends"),
        ("08", "PAIRED-CORBEL CORNICE", "11 boxes / 66 raw faces", "Three real projecting tiers", "6x10 central through opening"),
    ]
    for row, (definition, caption) in enumerate(zip(definitions, captions)):
        upper = 142 + row * 296
        draw.rectangle((24, upper, 1576, upper + 280), fill="#1b2934")
        draw.line((26, upper, 1574, upper), fill="#435e6d", width=2)
        number, title, cost, feature, support = caption
        draw.text((40, upper + 16), f"{number}  {title}", font=font(17), fill="#dfebea")
        draw.text((40, upper + 50), cost, font=font(16), fill="#b7cbd3")
        draw.text((40, upper + 80), feature, font=font(14), fill="#8fa6b4")
        draw.text((40, upper + 104), support, font=font(14), fill="#8fa6b4")
        identifier = definition["id"]
        sheet.paste(renders[(identifier, "icon64")], (42, upper + 148))
        sheet.paste(renders[(identifier, "gray-icon64")], (158, upper + 148))
        draw.rectangle((41, upper + 147, 106, upper + 212), outline="#526b77")
        draw.rectangle((157, upper + 147, 222, upper + 212), outline="#526b77")
        draw.text((40, upper + 220), "64px icon", font=font(13), fill="#a2b8c2")
        draw.text((150, upper + 220), "64px gray", font=font(13), fill="#a2b8c2")
        for horizontal, view in zip(columns, ("front", "side", "back", "iso")):
            sheet.paste(renders[(identifier, view)], (horizontal, upper + 14))
    draw.text((30, 1046), "Pillow software orthographic previews, nearest sampling / fixed face lighting. Not Blockbench or Minecraft screenshots.", font=font(17), fill="#91a8b6")
    draw.text((30, 1077), "All endpoints integer 0..16. No rotations, overlaps, floating parts, transparent pixels, emissive parts or paid image calls.", font=font(16), fill="#91a8b6")
    return sheet


def build_payload():
    definitions = make_definitions()
    tiles = make_tiles()
    atlas = make_atlas(tiles)
    assert len(tiles) == 16 and atlas.size == (128, 128)
    assert atlas.getchannel("A").getextrema() == (255, 255)
    assert tiles[2].crop((0, 0, 32, 16)).tobytes() == tiles[2].crop((0, 16, 32, 32)).tobytes()
    assert len({tiles[index].tobytes() for index in (0, 1, 2)}) == 3
    reports = [audit_geometry(definition) for definition in definitions]
    renders = {}
    previews = {}
    render_reports = []
    views = [("front", 256, 0, 0, False), ("side", 256, 90, 0, False), ("back", 256, 180, 0, False), ("iso", 256, 35, 28, False), ("underside", 256, 35, -28, False), ("gray-iso", 256, 35, 28, True), ("icon64", 64, 35, 28, False), ("gray-icon64", 64, 35, 28, True)]
    for definition in definitions:
        identifier = definition["id"]
        for view, size, yaw, elevation, gray in views:
            image, foreground = render_model(definition, tiles, size, yaw, elevation, gray)
            assert foreground > 0
            renders[(identifier, view)] = image
            name = f"work/renders/{identifier}/{view}.png"
            previews[name] = image_bytes(image)
            render_reports.append({"id": identifier, "view": view, "path": name, "size": [size, size], "yaw_degrees": yaw, "elevation_degrees": elevation, "gray": gray, "foreground_pixels": foreground})
    artifacts = {
        "definitions.json": json_bytes(definitions),
        "atlas.png": image_bytes(atlas),
        "tilemap.json": json_bytes(make_tilemap()),
        "contact-sheet.png": image_bytes(make_contact_sheet(definitions, renders)),
    }
    return artifacts, previews, reports, render_reports


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def main():
    parser = argparse.ArgumentParser(description="Rebuild only the three masonry revision entries; writes below this lane only.")
    parser.add_argument("--output", type=Path, default=ROOT)
    options = parser.parse_args()
    target = options.output.resolve()
    if not target.is_relative_to(ROOT):
        parser.error("Output must stay within this masonry revision lane.")
    first, previews, reports, render_reports = build_payload()
    second, repeat_previews, repeat_reports, repeat_render_reports = build_payload()
    assert first == second and previews == repeat_previews
    assert reports == repeat_reports and render_reports == repeat_render_reports
    used_tiles = sorted({component.get("faces", {}).get(face, component["tile"]) for definition in make_definitions() for component in definition["boxes"] for face in FACE_AXES})
    assert used_tiles == list(range(16))
    qa = {
        "schema_version": 1, "lane": "masonry", "revision": "remake-06-08-20261103",
        "status": "passed_offline_authoring_checks_not_runtime_acceptance",
        "scope": {"numbers": [6, 7, 8], "ids": [report["id"] for report in reports], "baseline_read_only": True, "shared_editor_touched": False, "game_launched": False},
        "checks": {"definition_count_exactly_3": True, "all_box_budgets_passed": True, "all_integer_endpoints": True, "all_positive_volumes": True, "zero_rotations": True, "zero_volume_overlaps": True, "zero_visible_coplanar_duplicates": True, "face_connected_no_floating_boxes": True, "mount_interfaces_passed": True, "uv_projection_bounds_passed": True, "native_slab_dedicated_top_bottom_side": True, "native_slab_upper_lower_edge_halves_identical": True, "pillar_four_real_flutes": True, "pillar_stack_interfaces_equal": True, "cornice_true_central_through_void": True},
        "models": reports,
        "texture": {"path": "atlas.png", "size": [128, 128], "mode": "RGBA", "alpha_extrema": [255, 255], "tiles": 16, "tile_size": [32, 32], "used_tiles": used_tiles, "source_palette_colors": len(PALETTE), "texels_per_model_unit": 2, "total_texels_one_frame": 16384, "rgba_uncompressed_bytes_not_gpu_memory": 65536, "glow_tiles": [], "animation_frames": 1, "paid_image_api_calls": 0},
        "rendering": {"renderer": "Pillow software orthographic triangle rasterizer with depth buffer", "filter": "nearest", "background": "#1b2934", "face_light_factors": SHADES, "center": [8, 8, 8], "scale_pixels_per_unit": "viewport_width / 26", "vertical_center": "viewport_height * 0.51", "principal_views": ["front", "side", "back", "iso"], "additional_checks": ["underside", "gray-iso", "icon64", "gray-icon64"], "view_count_total": len(render_reports), "evidence": render_reports},
        "reproducibility": {"passed": True, "method": "Two independently rebuilt in-memory payloads and all 24 render PNGs compared byte-for-byte before writing.", "compared_deliverables": list(first), "compared_preview_pngs": len(previews), "pillow_version": pillow_version, "font": "C:/Windows/Fonts/segoeui.ttf", "font_sha256": sha256(Path("C:/Windows/Fonts/segoeui.ttf").read_bytes()), "generator_sha256": sha256(Path(__file__).read_bytes())},
        "artifact_sha256": {name: sha256(data) for name, data in first.items()},
        "limitations": ["No Blockbench session was opened or modified.", "No Java model, bbmodel, native slab states, Forge jar or in-game validation is claimed here; parent handles integration and final export audits.", "Reported raw faces precede parent hidden-face pruning; unit-square exterior area is not a runtime quad count or draw-call count.", "Visual evidence is a neutral software preview, not proof of client lighting, collision, performance or placement behavior.", "The other fifteen baseline models and native slab runtime integration require parent-level byte checks."],
    }
    for name, data in {**first, **previews, "qa.json": json_bytes(qa)}.items():
        destination = target / name
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(data)
    print(json.dumps({"output": str(target), "status": qa["status"], "boxes": [report["boxes"] for report in reports], "raw_faces": [report["raw_declared_faces"] for report in reports], "reproducible": True, "preview_pngs": len(previews)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
