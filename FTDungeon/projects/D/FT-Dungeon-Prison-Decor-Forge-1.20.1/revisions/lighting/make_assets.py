from __future__ import annotations

import ast
import hashlib
import io
import itertools
import json
import math
from collections import deque
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont
import numpy as np


ROOT = Path(__file__).resolve().parent
FACES = ("north", "south", "west", "east", "up", "down")
NORMALS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0),
           "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
PARENT_EXPORTER = ROOT.parents[1] / "forge-project" / "tools" / "export_kit.py"
PARENT_RENDERER = PARENT_EXPORTER.with_name("render_kit.py")
UV_POLICY = "minecraft_default_uv_projected_2_texels_per_model_unit"
GLOW_TILES = set(range(8, 16))
REVIEWED_ARTIFACTS = {
    "contact-sheet.png": "eb5f456c648c6f6afed884f4ab888b3e05641a0400b750f5286ac07fd39a2d74",
    "atlas.png": "7c20fbe6831ef19dfce38af5713c40462a0760b2d4e5b9775ee936591d15e389",
}
VIEWS = tuple((key, label,
               (math.sin(math.radians(yaw)) * math.cos(math.radians(27)),
                math.sin(math.radians(27)),
                -math.cos(math.radians(yaw)) * math.cos(math.radians(27))))
              for key, label, yaw in (("front_east", "FRONT / EAST", 42),
                                      ("rear_east", "REAR / EAST", 138),
                                      ("rear_west", "REAR / WEST", 222),
                                      ("front_west", "FRONT / WEST", 318)))
MATERIALS = (
    ("riveted_plinth", "Cold forged iron with restrained rust and edge wear"),
    ("slate_worktop", "Inset slate worktop and carved plinth top"),
    ("plinth_underside", "Dark structural underside, not luminous"),
    ("aged_brass", "Brass vessel shoes and compression collars"),
    ("horizontal_pipe", "Continuous iron pipe field, projected at two texels per unit"),
    ("vertical_pipe", "Continuous vertical condenser tube field, no stretched full-tile frame"),
    ("control_face", "Pressure dial and valve marks painted on housing"),
    ("carved_cold_stone", "Cold slate stela, crown and base with chisel wear"),
    ("cyan_body_front", "Cyan front/back crop windows, liquid boundary at global y7 / texture row18"),
    ("cyan_body_side", "Cyan facets use the same global y7 / row18 boundary"),
    ("cyan_transition", "Narrow cyan neck, shoulder, foot and planar ends"),
    ("amber_body_front", "Amber front/back crop windows, liquid boundary at global y5.5 / row21"),
    ("amber_body_side", "Amber facets use the same global y5.5 / row21 boundary"),
    ("amber_transition", "Amber neck, shoulder, foot and planar ends"),
    ("sealed_rune_face", "Cold incised stone, restrained cyan inscription"),
    ("sealed_rune_reverse", "Dark side/back chisel relief with tiny rune accents"),
)


def box(name, start, end, tile, faces=None, glow=False):
    result = {"name": name, "from": list(start), "to": list(end), "tile": tile}
    if faces:
        result["faces"] = faces
    result["glow"] = glow
    return result


def default_uv(bounds, face):
    lower_x, lower_y, lower_z, upper_x, upper_y, upper_z = bounds
    return {
        "north": [16 - upper_x, 16 - upper_y, 16 - lower_x, 16 - lower_y],
        "south": [lower_x, 16 - upper_y, upper_x, 16 - lower_y],
        "west": [lower_z, 16 - upper_y, upper_z, 16 - lower_y],
        "east": [16 - upper_z, 16 - upper_y, 16 - lower_z, 16 - lower_y],
        "up": [lower_x, lower_z, upper_x, upper_z],
        "down": [lower_x, 16 - upper_z, upper_x, 16 - lower_z],
    }[face]


def pixel_uv(part, face):
    return [value * 2 for value in default_uv(part["from"] + part["to"], face)]


def parent_export_functions():
    syntax = ast.parse(PARENT_EXPORTER.read_text(encoding="utf-8"))
    names = ("default_uv", "cells", "face_visible", "export_model")
    selected = [node for node in syntax.body if isinstance(node, ast.FunctionDef) and node.name in names]
    if {node.name for node in selected} != set(names):
        raise ValueError("Parent exporter no longer exposes the four reviewed pure functions.")
    namespace = {"FACES": FACES, "NORMALS": NORMALS, "NAMESPACE": "ftdungeonprops"}
    exec(compile(ast.Module(body=selected, type_ignores=[]), str(PARENT_EXPORTER), "exec"), namespace)
    return namespace


def lamp_boxes():
    cyan_faces = {"north": 8, "south": 8, "up": 10, "down": 10}
    amber_faces = {"north": 11, "south": 11, "up": 13, "down": 13}
    lamp = [
        box("plinth / lower iron sole", (0, 0, 1), (16, 1, 13), 0, {"up": 1, "down": 2}),
        box("plinth / recessed slate deck", (1, 1, 2), (15, 2, 12), 0, {"up": 1, "down": 2}),
        box("main vessel / brass heating shoe", (3, 2, 5), (8, 3, 10), 3),
        box("collector / brass receiver shoe", (11, 2, 3), (14, 3, 6), 3),
        box("pressure control / solid housing", (2, 2, 2), (7, 4, 4), 0, {"north": 6}),
        box("cyan retort / lower shoulder", (3, 3, 5), (8, 4, 10), 10, glow=True),
        box("cyan retort / deep central bulb", (3, 4, 4), (8, 10, 11), 9, cyan_faces, True),
        box("cyan retort / west rounded facet", (2, 5, 5), (3, 9, 10), 9, {"up": 10, "down": 10}, True),
        box("cyan retort / east rounded facet", (8, 5, 5), (9, 9, 10), 9, {"up": 10, "down": 10}, True),
        box("cyan retort / upper shoulder", (3, 10, 5), (8, 11, 10), 10, glow=True),
        box("cyan retort / pronounced narrow neck", (4, 11, 6), (7, 13, 9), 10, glow=True),
        box("cyan retort / compression cap", (4, 13, 6), (7, 14, 9), 3),
        box("pipe bridge / hot-side upright", (5, 14, 6), (7, 15, 8), 5, {"up": 4, "down": 4}),
        box("pipe bridge / continuous elevated span", (7, 14, 6), (14, 15, 8), 4, {"east": 5, "west": 5}),
        box("pipe bridge / forward elbow", (12, 14, 4), (14, 15, 6), 4),
        box("pipe bridge / condenser downcomer", (12, 11, 4), (14, 14, 6), 5, {"up": 4, "down": 4}),
        box("amber collector / compression cap", (12, 10, 4), (14, 11, 6), 3),
        box("amber collector / narrow receiving neck", (12, 8, 4), (14, 10, 6), 13, glow=True),
        box("amber collector / upper shoulder", (11, 7, 3), (14, 8, 6), 13, glow=True),
        box("amber collector / deep central bulb", (11, 4, 2), (14, 7, 7), 12, amber_faces, True),
        box("amber collector / west rounded facet", (10, 4, 3), (11, 7, 6), 12, {"up": 13, "down": 13}, True),
        box("amber collector / east rounded facet", (14, 4, 3), (15, 7, 6), 12, {"up": 13, "down": 13}, True),
        box("amber collector / lower shoulder", (11, 3, 3), (14, 4, 6), 13, glow=True),
    ]
    return lamp


def tile_canvas(color, rectangles=()):
    image = Image.new("RGBA", (32, 32), color)
    draw = ImageDraw.Draw(image)
    for bounds, shade in rectangles:
        draw.rectangle(bounds, fill=shade)
    return image, draw


def metal_tiles():
    iron, draw = tile_canvas("#303d47", (
        ((0, 2, 31, 2), "#5d6e78"), ((0, 3, 31, 3), "#3a4954"),
        ((0, 14, 31, 14), "#586974"), ((0, 15, 31, 15), "#2a3741"),
        ((0, 28, 31, 28), "#52626d"), ((0, 29, 31, 29), "#283640"),
        ((0, 30, 31, 30), "#293741"), ((0, 31, 31, 31), "#18252e"),
        ((5, 17, 5, 24), "#43535e"), ((26, 18, 26, 23), "#43535e"),
        ((11, 6, 15, 6), "#3e4e59"), ((18, 20, 22, 20), "#24333d"),
        ((7, 29, 9, 29), "#784936"), ((24, 25, 25, 26), "#784936")))
    slate, draw = tile_canvas("#354c59", (
        ((0, 0, 31, 1), "#74858b"), ((0, 2, 1, 31), "#4e6570"),
        ((30, 2, 31, 31), "#253744"), ((2, 30, 29, 31), "#253744"),
        ((7, 19, 16, 20), "#3e5864"), ((20, 11, 24, 13), "#4e6570"),
        ((6, 25, 10, 25), "#8d6b3c"), ((21, 24, 25, 25), "#536570")))
    draw.rectangle((4, 4, 27, 27), outline="#253744", width=2)
    draw.line(((5, 7), (15, 7), (15, 5)), fill="#4e6570", width=1)
    underside, draw = tile_canvas("#1b2630", (
        ((14, 3, 17, 28), "#253744"), ((3, 14, 28, 17), "#253744")))
    draw.rectangle((3, 3, 28, 28), outline="#2b3943", width=2)
    brass, draw = tile_canvas("#8d6b3c", (
        ((0, 4, 31, 4), "#c3a065"), ((0, 5, 31, 5), "#705332"),
        ((0, 10, 31, 10), "#c3a065"), ((0, 11, 31, 11), "#705332"),
        ((0, 26, 31, 26), "#b49359"), ((0, 27, 31, 27), "#60472f"),
        ((9, 2, 9, 20), "#b18b52"), ((25, 9, 25, 27), "#b18b52"),
        ((15, 15, 19, 15), "#705333")))
    pipe, draw = tile_canvas("#35434d", (
        ((0, 2, 31, 2), "#829196"), ((0, 3, 31, 3), "#46545e"),
        ((0, 8, 31, 8), "#53646e"), ((0, 12, 31, 12), "#70838c"),
        ((0, 13, 31, 15), "#465762"), ((0, 23, 31, 24), "#293943")))
    vertical_pipe, draw = tile_canvas("#35434d")
    for column in (6, 10, 18, 22, 26):
        draw.line((column, 0, column, 31), fill="#697d87", width=1)
        draw.line((column + 1, 0, column + 1, 31), fill="#465762", width=1)
    controls, draw = tile_canvas("#293740")
    draw.rectangle((18, 24, 27, 27), fill="#27343d")
    draw.rectangle((20, 24, 21, 24), fill="#b59458")
    draw.rectangle((19, 25, 22, 26), fill="#b59458")
    draw.rectangle((20, 25, 21, 26), fill="#e0cfaa")
    draw.point((21, 25), fill="#493532")
    draw.rectangle((20, 27, 21, 27), fill="#8d6b3c")
    draw.rectangle((24, 25, 26, 25), fill="#a18b62")
    draw.rectangle((24, 27, 25, 27), fill="#697b84")
    stone = stone_tile()
    return [iron, slate, underside, brass, pipe, vertical_pipe, controls, stone]


def stone_tile():
    image, draw = tile_canvas("#354c59", (
        ((8, 4, 23, 4), "#586f7a"), ((8, 7, 23, 7), "#293f4c"),
        ((0, 26, 31, 26), "#536c78"), ((0, 29, 31, 29), "#253744"),
        ((7, 11, 9, 12), "#4e6570"), ((23, 22, 25, 23), "#4e6570"),
        ((9, 27, 11, 28), "#655449"), ((21, 5, 23, 5), "#253744")))
    draw.line(((15, 9), (15, 13), (13, 13)), fill="#253744", width=1)
    draw.line(((16, 9), (16, 12)), fill="#4e6570", width=1)
    draw.line(((21, 18), (21, 22), (23, 22)), fill="#253744", width=1)
    return image


def vessel_tile(amber=False, side=False):
    empty, tint, fluid, meniscus, highlight, edge = (
        ("#795332", "#986a3b", "#cc913a", "#e8b354", "#ffe0a0", "#60412b")
        if amber else
        ("#235c68", "#2f7882", "#309fa8", "#60c7bf", "#b8f2de", "#1b4d5a"))
    liquid_row = 21 if amber else 18
    image, draw = tile_canvas(empty, (((0, liquid_row + 1, 31, 31), fluid),
                                     ((0, liquid_row, 31, liquid_row), meniscus)))
    if side:
        for column in ((6, 12, 20, 26) if amber else (4, 10, 16, 22, 26)):
            draw.line((column, liquid_row - 4, column, liquid_row - 2), fill=tint, width=1)
            draw.line((column, liquid_row + 1, column, liquid_row + 2), fill=meniscus, width=1)
        draw.point((6 if amber else 12, liquid_row - 2), fill=highlight)
    else:
        windows = ((4, 18, 10, 24), (22, 18, 28, 24)) if amber else ((16, 12, 26, 24), (6, 12, 16, 24))
        for left, top, right, bottom in windows:
            draw.rectangle((left, top, left, bottom - 1), fill=edge)
            draw.rectangle((right - 1, top, right - 1, bottom - 1), fill=edge)
            draw.rectangle((left + 1, top, right - 2, top), fill=tint)
            draw.line((left + 2, top + 1, left + 2, liquid_row - 1), fill=highlight, width=1)
            draw.point((left + 2, liquid_row + 1), fill=highlight)
            if not amber:
                draw.line((right - 3, top + 2, right - 3, top + 3), fill=tint, width=1)
    return image


def transition_tile(amber=False):
    base, light, glint, shadow = (
        ("#996a35", "#cda052", "#ffe0a0", "#6e492b")
        if amber else ("#2a7480", "#4aa8aa", "#a3e1d3", "#205762"))
    image, draw = tile_canvas(base, (
        ((8, 4, 9, 14), light), ((20, 4, 21, 14), light),
        ((14, 15, 16, 23), shadow), ((6, 24, 25, 24), light)))
    if amber:
        draw.line((6, 12, 6, 15), fill=glint, width=1)
        draw.line((26, 12, 26, 15), fill=glint, width=1)
    else:
        draw.line((9, 6, 9, 9), fill=glint, width=1)
        draw.line((20, 6, 20, 9), fill=glint, width=1)
    return image


def rune_tile(reverse=False):
    image = stone_tile()
    draw = ImageDraw.Draw(image)
    draw.rectangle((10, 8, 21, 23), fill="#304754")
    draw.rectangle((10, 8, 21, 23), outline="#213643", width=1)
    draw.line((11, 9, 20, 9), fill="#4e6570", width=1)
    if reverse:
        paths = (((14, 11), (14, 14), (18, 14)), ((18, 18), (15, 18), (15, 21)))
        for path in paths:
            draw.line(path, fill="#182e3b", width=2)
            draw.line(path, fill="#46636f", width=1)
        draw.point((14, 11), fill="#3b999e")
        draw.point((15, 21), fill="#247f8b")
    else:
        paths = (((13, 10), (18, 10), (18, 12), (16, 12), (16, 13)),
                 ((13, 18), (13, 21), (18, 21), (18, 19), (16, 19)))
        for path in paths:
            draw.line(path, fill="#142631", width=3)
            draw.line(path, fill="#50cfca", width=1)
        draw.point((13, 10), fill="#b8f2de")
        draw.point((13, 21), fill="#b8f2de")
        draw.point((20, 22), fill="#75614a")
    return image


def make_atlas():
    tiles = metal_tiles() + [vessel_tile(), vessel_tile(side=True), transition_tile(),
                            vessel_tile(amber=True), vessel_tile(amber=True, side=True),
                            transition_tile(amber=True), rune_tile(), rune_tile(reverse=True)]
    atlas = Image.new("RGBA", (128, 128))
    for index, tile in enumerate(tiles):
        atlas.paste(tile, ((index % 4) * 32, (index // 4) * 32))
    return atlas


def tilemap():
    entries = []
    for index, (name, purpose) in enumerate(MATERIALS):
        left, top = (index % 4) * 32, (index // 4) * 32
        entries.append({"index": index, "id": name, "purpose": purpose,
                        "glow": index in GLOW_TILES,
                        "pixel_bounds": [left, top, left + 32, top + 32],
                        "atlas_region_uv_0_16": [left / 8, top / 8, (left + 32) / 8, (top + 32) / 8]})
    return {"atlas": "atlas.png", "size": [128, 128], "grid": [4, 4], "tile_size": [32, 32],
            "filter": "nearest", "alpha": "fully_opaque",
            "uv_policy": UV_POLICY, "texels_per_model_unit": 2,
            "exported_texture_size": [32, 32], "face_uv_override_fields": False,
            "glow_tiles": sorted(GLOW_TILES), "non_glow_tiles": list(range(8)), "tiles": entries}
def definitions():
    beacon = [
        box("runestone plinth / forged sole", (1, 0, 2), (15, 1, 14), 0, {"up": 1, "down": 2}),
        box("runestone plinth / carved load-bearing block", (2, 1, 3), (14, 3, 13), 7, {"up": 1}),
        box("runestone / narrow stone socket", (4, 3, 5), (12, 4, 11), 7),
        box("runestone / inscribed deep tablet", (5, 4, 6), (11, 12, 10), 15, {"north": 14}, True),
        box("runestone / tapering crown shoulder", (6, 12, 6), (10, 14, 10), 7),
        box("runestone / narrow crown peak", (7, 14, 7), (9, 15, 9), 7),
        box("runestone / west buttress foot", (4, 4, 6), (5, 7, 10), 7),
        box("runestone / east buttress foot", (11, 4, 6), (12, 7, 10), 7),
        box("seal support / west load-bearing post", (2, 3, 7), (3, 8, 9), 0),
        box("seal support / east load-bearing post", (13, 3, 7), (14, 8, 9), 0),
        box("iron sealing hoop / front span", (2, 8, 4), (14, 9, 5), 0),
        box("iron sealing hoop / rear span", (2, 8, 11), (14, 9, 12), 0),
        box("iron sealing hoop / west return", (2, 8, 5), (3, 9, 11), 0),
        box("iron sealing hoop / east return", (13, 8, 5), (14, 9, 11), 0),
        box("binding / front iron key", (7, 8, 5), (9, 9, 6), 3),
        box("binding / rear iron key", (7, 8, 10), (9, 9, 11), 3),
        box("upper restraint / west rising horn", (2, 9, 7), (3, 12, 9), 0),
        box("upper restraint / west inward hook", (3, 11, 7), (5, 12, 9), 0),
        box("upper restraint / east rising horn", (13, 9, 7), (14, 12, 9), 0),
        box("upper restraint / east inward hook", (11, 11, 7), (13, 12, 9), 0),
    ]
    return [
        {"id": "prison_alchemical_lamp", "name_zh": "双胆炼金灯",
         "name_en": "Twin Vessel Alchemical Lamp", "kind": "decor", "mount": "floor",
         "light": 12, "ingredient": "minecraft:brewing_stand", "boxes": lamp_boxes(),
         "description_zh": "单格双胆蒸馏仪：青色主胆、偏置琥珀收集胆、连续金属管桥与真实负空间。装饰光源，无酿造或储物功能。"},
        {"id": "prison_rune_beacon", "name_zh": "禁文立碑灯", "name_en": "Bound Rune Tablet",
         "kind": "decor", "mount": "floor", "light": 10, "ingredient": "minecraft:glow_ink_sac",
         "boxes": beacon,
         "description_zh": "单格束缚石碑：收尖石冠、环抱铁封箍、上扣铁角与真实空隙，冷青古符仅作装饰灯光；不是原版信标。"},
    ]


def render(model, atlas, direction, size=(384, 360), clay=False):
    syntax = ast.parse(PARENT_RENDERER.read_text(encoding="utf-8"))
    names = ("face_vertices", "uv_vertices", "render")
    selected = [node for node in syntax.body if isinstance(node, ast.FunctionDef) and node.name in names]
    if {node.name for node in selected} != set(names):
        raise ValueError("Parent renderer no longer exposes the three reviewed pure functions.")
    encoded = {}
    for tile in range(16):
        left, top = (tile % 4) * 32, (tile // 4) * 32
        image = (Image.new("RGB", (32, 32), (156, 170, 177)) if clay else
                 atlas.crop((left, top, left + 32, top + 32)).convert("RGB"))
        stream = io.BytesIO()
        image.save(stream, format="PNG")
        encoded[tile] = stream.getvalue()

    def texture_file(reference):
        return io.BytesIO(encoded[int(reference.rsplit("_", 1)[1])])

    namespace = {"np": np, "math": math, "Image": Image, "NORMALS": NORMALS,
                 "texture_reference": lambda current, key: current["textures"][key.lstrip("#")],
                 "texture_file": texture_file}
    exec(compile(ast.Module(body=selected, type_ignores=[]), str(PARENT_RENDERER), "exec"), namespace)
    yaw = math.degrees(math.atan2(direction[0], -direction[2]))
    pitch = math.degrees(math.atan2(direction[1], math.hypot(direction[0], direction[2])))
    render_size = (64, 80) if size == (64, 64) else size
    image = namespace["render"](model, size=render_size, yaw=yaw, pitch=pitch)
    if size == (64, 64):
        image = image.crop((0, 12, 64, 76))
    foreground = np.any(np.asarray(image) != np.array((34, 45, 56)), axis=2)
    mask = Image.fromarray(np.where(foreground, 255, 0).astype(np.uint8))
    return image.convert("RGBA"), mask


def font(size, bold=False):
    for candidate in (Path("C:/Windows/Fonts/segoeuib.ttf" if bold else "C:/Windows/Fonts/segoeui.ttf"),
                      Path("C:/Windows/Fonts/arialbd.ttf" if bold else "C:/Windows/Fonts/arial.ttf")):
        if candidate.is_file():
            return ImageFont.truetype(str(candidate), size)
    return ImageFont.load_default(size=size)


def contact_sheet(data, atlas, destination, exported):
    sheet = Image.new("RGBA", (1648, 1280), "#14212b")
    draw = ImageDraw.Draw(sheet)
    draw.text((28, 20), "THE FORGOTTEN CELLS / LIGHTING REDESIGNS", font=font(35, True), fill="#e5e2d4")
    draw.text((30, 72), "UV REPAIR / parent exporter + parent renderer, projected 2 texels per unit. No whole-tile stretch.",
              font=font(20), fill="#9eb2bd")
    results = {}
    for model_index, definition in enumerate(data):
        header_top = 132 + model_index * 534
        model_label = ("21 / DISTILLATION LAMP   -   23 STRUCTURAL CUBOIDS / LIGHT 12" if model_index == 0 else
                       "23 / BOUND RUNESTONE   -   20 STRUCTURAL CUBOIDS / LIGHT 10")
        draw.text((30, header_top), model_label, font=font(25, True), fill="#80c8c6")
        clay_sheet = Image.new("RGBA", (1024, 256), "#202e3a")
        records = []
        for view_index, (key, label, direction) in enumerate(VIEWS):
            left, top = 24 + 406 * view_index, header_top + 48
            draw.rounded_rectangle((left, top, left + 382, top + 454), radius=9,
                                   fill="#202e3a", outline="#384d5a", width=1)
            draw.text((left + 16, top + 12), label, font=font(20, True), fill="#d2dbdd")
            draw.line((left + 16, top + 44, left + 366, top + 44), fill="#405761", width=1)
            rendered, mask = render(exported[definition["id"]], atlas, direction, size=(350, 330))
            sheet.paste(rendered, (left + 16, top + 49))
            icon, icon_mask = render(exported[definition["id"]], atlas, direction, size=(64, 64))
            sheet.paste(icon, (left + 296, top + 382))
            draw.text((left + 16, top + 391), "PROJECTED UV / 2px per unit", font=font(14), fill="#9eb2bd")
            draw.text((left + 16, top + 416), "64px native icon at right", font=font(14), fill="#7d96a3")
            prefix = definition["id"]
            rendered.save(destination / "work" / f"{prefix}-{key}.png", optimize=False)
            icon.save(destination / "work" / f"{prefix}-icon64-{key}.png", optimize=False)
            clay, _ = render(exported[definition["id"]], atlas, direction, size=(256, 256), clay=True)
            clay_sheet.paste(clay, (view_index * 256, 0))
            records.append({"view": key, "yaw_degrees": (42, 138, 222, 318)[view_index], "pitch_degrees": 27,
                            "direction": list(direction), "image_size": list(rendered.size),
                            "foreground_bounds": list(mask.getbbox()), "icon_size": [64, 64],
                            "icon_foreground_bounds": list(icon_mask.getbbox()),
                            "foreground_pixels": mask.histogram()[255],
                            "icon_foreground_pixels": icon_mask.histogram()[255]})
        clay_sheet.save(destination / "work" / f"{definition['id']}-clay-four-views.png", optimize=False)
        results[definition["id"]] = records
    draw.text((30, 1230), "REAL UV CANDIDATE REVIEW / parent's pure export + render functions, local replacement pixels, no bloom or transparency",
              font=font(18), fill="#9eb2bd")
    draw.text((30, 1256), "Candidate only: parent must re-export, rebuild and accept the final resources. This does not replace final before/after QA.",
              font=font(15), fill="#77919e")
    sheet.save(destination / "contact-sheet.png", optimize=False)
    return results


def overlap(first, second):
    return tuple(min(first["to"][axis], second["to"][axis])
                 - max(first["from"][axis], second["from"][axis]) for axis in range(3))


def face_contact(first, second):
    dimensions = overlap(first, second)
    return dimensions.count(0) == 1 and all(value >= 0 for value in dimensions)


def voxels(part):
    return itertools.product(*(range(start, end) for start, end in zip(part["from"], part["to"])))


def face_cells(part, face):
    left, bottom, front = part["from"]
    right, top, back = part["to"]
    if face in ("north", "south"):
        return [(horizontal, vertical, front - 1 if face == "north" else back)
                for horizontal in range(left, right) for vertical in range(bottom, top)]
    if face in ("east", "west"):
        return [(right if face == "east" else left - 1, vertical, depth)
                for vertical in range(bottom, top) for depth in range(front, back)]
    return [(horizontal, top if face == "up" else bottom - 1, depth)
            for horizontal in range(left, right) for depth in range(front, back)]


def floor_graph(parts):
    graph = {index: set() for index in range(len(parts))}
    for first_index, second_index in itertools.combinations(range(len(parts)), 2):
        if face_contact(parts[first_index], parts[second_index]):
            graph[first_index].add(second_index)
            graph[second_index].add(first_index)
    reached = {index for index, part in enumerate(parts) if part["from"][1] == 0}
    queue = deque(sorted(reached))
    while queue:
        current = queue.popleft()
        for neighbor in sorted(graph[current] - reached):
            reached.add(neighbor)
            queue.append(neighbor)
    return graph, reached


def duplicate_facelets(parts):
    duplicates = []
    seen = {}
    axes = {"north": 2, "south": 2, "east": 0, "west": 0, "up": 1, "down": 1}
    for part in parts:
        for face in FACES:
            axis = axes[face]
            plane = part["to"][axis] if face in ("south", "east", "up") else part["from"][axis]
            other_axes = tuple(index for index in range(3) if index != axis)
            for first_coord in range(part["from"][other_axes[0]], part["to"][other_axes[0]]):
                for second_coord in range(part["from"][other_axes[1]], part["to"][other_axes[1]]):
                    key = (face, plane, first_coord, second_coord)
                    if key in seen:
                        duplicates.append([seen[key], part["name"], face])
                    seen[key] = part["name"]
    return duplicates


def audit_model(definition, views):
    errors = []
    parts = definition["boxes"]
    integer_bounds = all(type(value) is int and 0 <= value <= 16
                         for part in parts for value in part["from"] + part["to"])
    positive = all(end > start for part in parts for start, end in zip(part["from"], part["to"]))
    rotated = sum("rotation" in part for part in parts)
    if not integer_bounds or not positive or rotated or len(parts) > 28:
        errors.append("Budget, integer 0..16, positive volume or zero-rotation constraint failed.")
    intersections = [[first["name"], second["name"]]
                     for first, second in itertools.combinations(parts, 2)
                     if all(value > 0 for value in overlap(first, second))]
    if intersections:
        errors.append("Positive-volume intersection.")
    graph, reached = floor_graph(parts)
    if len(reached) != len(parts):
        errors.append("A part has no positive-area face-contact path to the floor.")
    occupied, duplicates = set(), set()
    for part in parts:
        cells = set(voxels(part))
        duplicates.update(occupied.intersection(cells))
        occupied.update(cells)
    coplanar = duplicate_facelets(parts)
    if coplanar:
        errors.append("Same-direction coplanar duplicate surface.")
    hidden, zero_surface = [], []
    exposed_area = 0
    for part in parts:
        visible_count = 0
        for face in FACES:
            uncovered = sum(cell not in occupied for cell in face_cells(part, face))
            exposed_area += uncovered
            if uncovered == 0:
                hidden.append({"box": part["name"], "face": face})
            else:
                visible_count += 1
        if visible_count == 0:
            zero_surface.append(part["name"])
    if zero_surface:
        errors.append("A box makes no exposed-surface contribution.")
    normal_tiles, glow_tiles = set(), set()
    for part in parts:
        referenced = set(part.get("faces", {}).values()) | {part["tile"]}
        if any(type(tile) is not int or tile not in range(16) for tile in referenced):
            errors.append("Illegal atlas tile reference.")
        (glow_tiles if part["glow"] else normal_tiles).update(referenced)
    if normal_tiles.intersection(GLOW_TILES) or glow_tiles - GLOW_TILES:
        errors.append("Ordinary and glowing material roles are not isolated.")
    if definition["id"] == "prison_alchemical_lamp":
        apertures = [{"from": [9, 8, 6], "to": [12, 14, 8]}]
        pipe_path = [11, 12, 13, 14, 15, 16]
        continuous = all(second in graph[first] for first, second in zip(pipe_path, pipe_path[1:]))
        connection = {"continuous_cap_to_cap": continuous,
                      "ordered_parts": [parts[index]["name"] for index in pipe_path]}
        if not continuous:
            errors.append("The cap-to-cap metal bridge is disconnected.")
    else:
        apertures = [{"from": [3, 9, 7], "to": [5, 11, 9]},
                     {"from": [11, 9, 7], "to": [13, 11, 9]},
                     {"from": [3, 4, 5], "to": [5, 8, 6]}]
        ring_path = [10, 12, 11, 13, 10]
        continuous = all(second in graph[first] for first, second in zip(ring_path, ring_path[1:]))
        connection = {"continuous_iron_seal_ring": continuous,
                      "ordered_parts": [parts[index]["name"] for index in ring_path]}
        if not continuous:
            errors.append("The surrounding iron sealing ring is disconnected.")
    aperture_reports = []
    for aperture in apertures:
        cells = set(voxels(aperture))
        obstruction = sorted(cells.intersection(occupied))
        aperture_reports.append({**aperture, "volume": len(cells), "occupied_voxels": obstruction})
        if obstruction:
            errors.append("A designed negative-space aperture is obstructed.")
    clipped = []
    for view in views:
        for field, size_field in (("foreground_bounds", "image_size"), ("icon_foreground_bounds", "icon_size")):
            bounds = view[field]
            width, height = view[size_field]
            if bounds[0] == 0 or bounds[1] == 0 or bounds[2] == width or bounds[3] == height:
                clipped.append([view["view"], field])
    if clipped:
        errors.append("A preview or a 64px icon clips the geometry.")
    identity = {key: definition[key] for key in ("id", "name_zh", "name_en", "kind", "mount", "light", "ingredient")}
    return {
        "status": "pass" if not errors else "fail", "errors": errors, "identity": identity,
        "geometry": {
            "boxes": len(parts), "budget": 28, "declared_faces_before_parent_pruning": len(parts) * 6,
            "bounds": {"from": [min(part["from"][axis] for part in parts) for axis in range(3)],
                       "to": [max(part["to"][axis] for part in parts) for axis in range(3)]},
            "integer_coordinates_0_16": integer_bounds, "positive_volumes": positive, "rotated_boxes": rotated,
            "intersecting_pairs": intersections, "duplicate_occupied_voxels": len(duplicates),
            "same_normal_coplanar_duplicates": coplanar, "floor_connected_boxes": len(reached),
            "contact_edges": sum(len(neighbors) for neighbors in graph.values()) // 2,
            "zero_surface_contribution_boxes": zero_surface, "fully_internal_faces": len(hidden),
            "fully_internal_face_candidates": hidden,
            "faces_after_only_full_internal_face_pruning": len(parts) * 6 - len(hidden),
            "exposed_union_area_model_units_squared": exposed_area, "volume_model_units_cubed": len(occupied),
            "empty_apertures": aperture_reports, "connected_feature": connection,
        },
        "materials": {"ordinary_tiles": sorted(normal_tiles), "glow_tiles": sorted(glow_tiles),
                      "shared_glow_ordinary_tiles": sorted(normal_tiles.intersection(glow_tiles)),
                      "glow_boxes": sum(part["glow"] for part in parts)},
        "views": views, "clipped_views": clipped,
    }


def audit(data, atlas, views):
    models = {definition["id"]: audit_model(definition, views[definition["id"]]) for definition in data}
    errors = [f"{name}: {error}" for name, result in models.items() for error in result["errors"]]
    expected = {"prison_alchemical_lamp": (12, "minecraft:brewing_stand"),
                "prison_rune_beacon": (10, "minecraft:glow_ink_sac")}
    if len(data) != 2 or set(models) != set(expected):
        errors.append("Exactly the two assigned IDs are required.")
    for definition in data:
        if (definition["light"], definition["ingredient"]) != expected[definition["id"]]:
            errors.append("Identity light or ingredient changed.")
        if definition["kind"] != "decor" or definition["mount"] != "floor":
            errors.append("Kind or floor mount changed.")
    if atlas.mode != "RGBA" or atlas.size != (128, 128) or atlas.getchannel("A").getextrema() != (255, 255):
        errors.append("Atlas dimensions, mode or opacity is invalid.")
    all_normal = set().union(*(set(result["materials"]["ordinary_tiles"]) for result in models.values()))
    all_glow = set().union(*(set(result["materials"]["glow_tiles"]) for result in models.values()))
    if all_normal.intersection(all_glow) or all_glow != GLOW_TILES:
        errors.append("Cross-model glow tile roles are not isolated.")
    rune = atlas.crop((64, 96, 96, 128))
    bright_rune_pixels = sum(green > 135 and blue > 130 and green > red * 1.4
                            for red, green, blue, alpha in
                            (rune.getpixel((horizontal, vertical)) for horizontal in range(32) for vertical in range(32)))
    if bright_rune_pixels / 1024 > 0.15:
        errors.append("Cyan rune coverage is not restrained.")
    return {
        "status": "pass" if not errors else "fail", "errors": errors, "definition_count": len(data), "models": models,
        "materials": {"atlas_size": [128, 128], "tile_size": [32, 32], "tiles": 16,
                      "ordinary_tiles": sorted(all_normal), "glow_only_tiles": sorted(all_glow),
                      "shared_glow_ordinary_tiles": sorted(all_normal.intersection(all_glow)),
                      "alpha_range": list(atlas.getchannel("A").getextrema()), "pixels_per_frame": 16384,
                      "frames": 1, "raw_rgba_bytes_before_mipmaps_or_gpu_overhead": 65536,
                      "texture_images": 1, "transparent_pixels": 0, "outline_shells": 0, "animation_tracks": 0,
                      "bright_cyan_rune_front_pixels": bright_rune_pixels,
                      "bright_cyan_rune_front_fraction": bright_rune_pixels / 1024,
                      "runtime_emissivity": "None promised; parent creates dim LIT=false pixels and ordinary block light."},
        "preview": {"renderer": "Actual parent render_kit.py pure functions, candidate JSON from parent export_kit.py pure functions.",
                    "is_minecraft_or_blockbench_screenshot": False, "texture_filter": "nearest", "bloom": False,
                    "uv_policy": UV_POLICY, "yaw_degrees": [42, 138, 222, 318], "pitch_degrees": 27,
                    "views_per_model": 4, "native_64px_icons_per_model": 4, "clay_review_per_model": True,
                    "native_icon_framing": "Render 64x80 with the parent's unchanged 64/27 scale, crop y12..76 to remove its +4px vertical offset; no resampling."},
        "validation_limits": [
            "No shared Blockbench state was read or modified; no editor open/reopen claim.",
            "No Minecraft client or Forge runtime was launched or tested.",
            "Parent pure exporter is exercised in memory for local candidate JSON; final shared resources, bbmodels, packaged jar and LIT states remain the parent's responsibility.",
            "Full internal face candidates are informational; parent performs conservative face removal.",
            "Face connectivity is checked, not gravity, heat, fluid or gameplay simulation.",
            "Runtime FPS, draw calls and memory were not measured."]}


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def uv_contract_audit(data, atlas, exported, parent_functions, destination):
    errors, inventory, key_crops, liquid_checks = [], [], [], []
    declared_matches = 0
    crop_sheet = Image.new("RGBA", (1056, 556), "#14212b")
    drawing = ImageDraw.Draw(crop_sheet)
    targets = (
        ("prison_alchemical_lamp", "pressure control / solid housing", "north", [18, 24, 28, 28], "GAUGE / 10 x 4 texels"),
        ("prison_alchemical_lamp", "cyan retort / deep central bulb", "north", [16, 12, 26, 24], "CYAN FRONT / y7 liquid"),
        ("prison_alchemical_lamp", "cyan retort / deep central bulb", "south", [6, 12, 16, 24], "CYAN BACK / y7 liquid"),
        ("prison_alchemical_lamp", "amber collector / deep central bulb", "north", [4, 18, 10, 24], "AMBER FRONT / y5.5 liquid"),
        ("prison_alchemical_lamp", "amber collector / deep central bulb", "south", [22, 18, 28, 24], "AMBER BACK / y5.5 liquid"),
        ("prison_rune_beacon", "runestone / inscribed deep tablet", "north", [10, 8, 22, 24], "RUNE FRONT / 12 x 16 texels"),
    )
    part_index = {}
    for definition in data:
        for part in definition["boxes"]:
            part_index[definition["id"], part["name"]] = part
            for face in FACES:
                ours = default_uv(part["from"] + part["to"], face)
                theirs = parent_functions["default_uv"](part["from"] + part["to"], face)
                declared_matches += ours == theirs
                if ours != theirs:
                    errors.append(f"UV mismatch with parent: {definition['id']} / {part['name']} / {face}")
        for element in exported[definition["id"]]["elements"]:
            for face, details in element["faces"].items():
                part = part_index[definition["id"], element["name"]]
                bounds = [value * 2 for value in details["uv"]]
                tile = part.get("faces", {}).get(face, part["tile"])
                extent = [end - start for start, end in zip(part["from"], part["to"])]
                width = extent[2] if face in ("west", "east") else extent[0]
                height = extent[2] if face in ("up", "down") else extent[1]
                density_matches = bounds[2] - bounds[0] == width * 2 and bounds[3] - bounds[1] == height * 2
                if not density_matches:
                    errors.append("Nonuniform projected texel density.")
                inventory.append({"id": definition["id"], "box": part["name"], "face": face,
                                  "tile": tile, "java_uv": details["uv"], "tile_pixel_uv": bounds,
                                  "texels_per_unit": [2, 2], "density_matches": density_matches})
                if tile in (8, 9, 11, 12) and face in ("north", "south", "west", "east"):
                    row = 18 if tile in (8, 9) else 21
                    expected_color = (96, 199, 191, 255) if row == 18 else (232, 179, 84, 255)
                    if bounds[1] < row < bounds[3]:
                        tile_left, tile_top = (tile % 4) * 32, (tile // 4) * 32
                        covered = sum(atlas.getpixel((tile_left + column, tile_top + row)) == expected_color
                                      for column in range(bounds[0], bounds[2]))
                        fraction = covered / (bounds[2] - bounds[0])
                        liquid_checks.append({"box": part["name"], "face": face, "tile": tile,
                                              "texture_row": row, "line_top_global_y": 16 - row / 2,
                                              "line_thickness_texels": 1, "meniscus_coverage_of_face_width": fraction})
                        if fraction < 0.5:
                            errors.append("Liquid boundary is absent from its real projected face crop.")
    for index, (identifier, part_name, face, expected, label) in enumerate(targets):
        part = part_index[identifier, part_name]
        actual = pixel_uv(part, face)
        tile = part.get("faces", {}).get(face, part["tile"])
        tile_left, tile_top = (tile % 4) * 32, (tile // 4) * 32
        image = atlas.crop((tile_left + actual[0], tile_top + actual[1], tile_left + actual[2], tile_top + actual[3]))
        key_crops.append({"id": identifier, "box": part_name, "face": face, "tile": tile,
                          "tile_pixel_uv": actual, "expected_parent_rectangle": expected,
                          "matches_expected_rectangle": actual == expected,
                          "crop_rgba_sha256": hashlib.sha256(image.tobytes()).hexdigest()})
        if actual != expected:
            errors.append("A required key face no longer matches the parent's supplied pixel rectangle.")
        left, top = 12 + index % 3 * 352, 10 + index // 3 * 274
        drawing.text((left, top), label, font=font(17, True), fill="#d2dbdd")
        drawing.text((left, top + 28), f"tile {tile} / {actual}", font=font(15), fill="#9eb2bd")
        crop_sheet.paste(image.resize((image.width * 11, image.height * 11), Image.Resampling.NEAREST), (left + 44, top + 66))
    crop_sheet.save(destination / "work" / "actual-uv-crops.png", optimize=False)
    gauge = atlas.crop((64 + 18, 32 + 24, 64 + 28, 32 + 28))
    gauge_ivory = sum(gauge.getpixel((across, height)) == (224, 207, 170, 255)
                      for across in range(gauge.width) for height in range(gauge.height))
    if gauge_ivory != 3:
        errors.append("The tiny pressure gauge is not present in the actual four-texel-high north crop.")
    rune_tile = atlas.crop((64, 96, 96, 128))
    rune_points = [(across, height) for across in range(10, 22) for height in range(8, 24)
                   if (lambda color: color[1] > 135 and color[2] > 130 and color[1] > color[0] * 1.4)
                   (rune_tile.getpixel((across, height)))]
    rune_parts = data[1]["boxes"]
    obscured = []
    for across, height in rune_points:
        world_x, world_y = 16 - (across + 0.5) / 2, 16 - (height + 0.5) / 2
        if any(part["from"][2] < 6 and part["to"][2] <= 6
               and part["from"][0] <= world_x < part["to"][0]
               and part["from"][1] <= world_y < part["to"][1] for part in rune_parts):
            obscured.append([across, height])
    if not rune_points or obscured or len(rune_points) / 192 > 0.15:
        errors.append("A cyan rune stroke is absent or hidden behind the sealing hoop in the north view.")
    write_json(destination / "work" / "face-uv-inventory.json", inventory)
    shared_models = {}
    for identifier, candidate in exported.items():
        shared_path = PARENT_EXPORTER.parent.parent / "src" / "main" / "resources" / "assets" / "ftdungeonprops" / "models" / "block" / f"{identifier}.json"
        if shared_path.is_file():
            shared = json.loads(shared_path.read_text(encoding="utf-8"))
            def shape_uv(model):
                return [{"name": element["name"], "from": element["from"], "to": element["to"],
                         "faces": {face: details["uv"] for face, details in element["faces"].items()}}
                        for element in model["elements"]]
            matches = shape_uv(shared) == shape_uv(candidate)
            shared_models[identifier] = {"existing_shared_geometry_and_uv_match": matches,
                                         "existing_shared_json_sha256": sha256(shared_path),
                                         "pixels_tested": "Local replacement atlas, not the stale shared texture files."}
            if not matches:
                errors.append("Current shared exported geometry/UV no longer matches this source candidate.")
    return {"status": "pass" if not errors else "fail", "errors": errors,
            "policy": UV_POLICY, "face_uv_overrides_used": False,
            "parent_default_uv_declared_faces_compared": declared_matches,
            "parent_exported_faces_rendered": len(inventory), "all_face_density_checks_pass": all(item["density_matches"] for item in inventory),
            "source": "Four pure functions read from parent's export_kit.py AST; no main routine or shared state executed.",
            "exporter_sha256": sha256(PARENT_EXPORTER), "renderer_sha256": sha256(PARENT_RENDERER),
            "existing_parent_export_readonly_check": shared_models,
            "key_face_crops": key_crops, "gauge_ivory_pixels_in_actual_crop": gauge_ivory,
            "liquid_boundaries": liquid_checks, "rune_bright_pixels_in_actual_crop": len(rune_points),
            "rune_bright_fraction_of_actual_crop": len(rune_points) / 192,
            "rune_bright_pixels_obscured_by_front_iron": obscured,
            "geometry_changed_from_first_pass": False,
            "final_integration": "Candidate ready only after visual freeze; parent still must re-export/build/final-QA."}


def build(destination):
    destination.mkdir(parents=True, exist_ok=True)
    (destination / "work").mkdir(exist_ok=True)
    data = definitions()
    atlas = make_atlas()
    parent_functions = parent_export_functions()
    exported = {}
    for definition in data:
        model, removed = parent_functions["export_model"]({**definition, "lane": "lighting", "material_set": "lighting_uv_repair"})
        exported[definition["id"]] = model
        write_json(destination / "work" / f"{definition['id']}-candidate-java.json", model)
    write_json(destination / "definitions.json", data)
    write_json(destination / "tilemap.json", tilemap())
    atlas.save(destination / "atlas.png", optimize=False)
    views = contact_sheet(data, atlas, destination, exported)
    report = audit(data, atlas, views)
    uv_check = uv_contract_audit(data, atlas, exported, parent_functions, destination)
    report["uv_contract"] = uv_check
    report["candidate_gate"] = "ready_for_parent_reexport" if not uv_check["errors"] else "repair_required"
    if uv_check["errors"]:
        report["status"] = "fail"
        report["errors"].extend(uv_check["errors"])
    report["superseded_first_pass"] = {
        "withdrawn": True,
        "failure": "First pass preview stretched full 32px tile onto every face; real export used projected default_uv, hiding the gauge and clipping vessel and rune artwork.",
        "first_contact_sheet_sha256": "105f5e94e1cd251f650072b0cbeb3e03c645a3ace7ed4362c7efa3b5adc727cc",
        "first_atlas_sha256": "1c88e0e04c24a1c244b60c53ebe63fdc858957556158a4f56a5b7f94f568ddee",
        "inspected_failed_final_before_after_sha256": "8d9a138b4e9a91bb6b19eb9939f7abcd41811087c4a9b5d13c2058e2b0846282",
        "repair": "Use actual parent pure export and render functions and redraw motifs inside real projected crops. Geometry unchanged. No per-face whole-tile stretch or UV overrides.",
    }
    review_matches = {name: sha256(destination / name) == digest for name, digest in REVIEWED_ARTIFACTS.items()}
    report["visual_review"] = {
        "reviewed_snapshot_matches": all(review_matches.values()),
        "sha256_matches": review_matches,
        "scope": "Visual inspection of corrected parent-UV contact sheet, atlas, both clay sheets, six exact crop windows and both native front 64px icons.",
        "observations": [
            "Lamp: tiny four-texel-high gauge is inside its real crop; both body fronts/backs retain a container reflection and one-texel meniscus.",
            "Lamp: main cyan boundary is row18/global y7 and amber boundary row21/global y5.5; rods sample continuous metal.",
            "Tablet: narrow stone crown, thick carved base, enclosing iron hoop and inward upper hooks replace the old sign frame.",
            "Tablet: both cyan glyph groups are wholly inside the real north UV rectangle and outside the front hoop occlusion band.",
            "Four parent yaw views and 64px icons retain complete silhouettes. No stretch, transparency or bloom hides the repair.",
        ],
        "limitations": "Candidate uses parent's exact projection/export/render helpers. Final shared re-export, LIT states, Forge build and acceptance remain pending in parent chat.",
    }
    if not all(review_matches.values()):
        report["status"] = "fail"
        report["errors"].append("Generated visuals differ from the inspected snapshot; visual review required.")
    report["candidate_gate"] = "ready_for_parent_reexport" if report["status"] == "pass" else "repair_or_visual_review_required"
    write_json(destination / "qa.json", report)
    return report


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    first = build(ROOT)
    repeat_root = ROOT / "work" / "reproducibility"
    second = build(repeat_root)
    compared = ("definitions.json", "atlas.png", "tilemap.json", "contact-sheet.png", "qa.json")
    matches = {name: (ROOT / name).read_bytes() == (repeat_root / name).read_bytes() for name in compared}
    reproducibility = {
        "method": "Two complete builds in separate directories; byte comparison before identical report metadata.",
        "files": matches, "byte_identical": all(matches.values()),
        "output_sha256_excluding_self": {name: sha256(ROOT / name) for name in compared if name != "qa.json"},
        "source_sha256": sha256(Path(__file__).resolve()), "design_sha256": sha256(ROOT / "DESIGN.md"),
    }
    for report, destination in ((first, ROOT), (second, repeat_root)):
        report["reproducibility"] = reproducibility
        if not reproducibility["byte_identical"]:
            report["status"] = "fail"
            report["errors"].append("Nonreproducible build.")
        write_json(destination / "qa.json", report)
    final_qa_identical = (ROOT / "qa.json").read_bytes() == (repeat_root / "qa.json").read_bytes()
    print(json.dumps({"status": first["status"],
                      "models": {name: {"boxes": result["geometry"]["boxes"],
                                        "source_faces": result["geometry"]["declared_faces_before_parent_pruning"],
                                        "full_internal_faces": result["geometry"]["fully_internal_faces"]}
                                 for name, result in first["models"].items()},
                      "all_five_outputs_reproducible": all(matches.values()) and final_qa_identical,
                      "rune_cyan_fraction": first["materials"]["bright_cyan_rune_front_fraction"],
                      "errors": first["errors"]}, indent=2))
    if first["errors"] or not final_qa_identical:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
