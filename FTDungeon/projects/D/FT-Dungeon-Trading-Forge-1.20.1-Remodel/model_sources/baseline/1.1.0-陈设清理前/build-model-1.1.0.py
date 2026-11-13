import base64
import copy
import json
import math
from pathlib import Path
import random
import uuid
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
ASSETS = ROOT / "src/main/resources/assets/ftdungeontrading"
OUTPUT = ROOT / "model_sources"
SIZE = 128
FACES = ["north", "south", "east", "west", "up", "down"]
TILES = {
    "top": (0, 0, 64, 64), "edge": (64, 0, 64, 16),
    "leg": (128, 0, 16, 48), "rail": (144, 0, 32, 16),
    "iron": (176, 0, 16, 16), "iron_top": (192, 0, 16, 16),
    "shelf": (64, 16, 64, 48), "moss_leg": (144, 16, 16, 48),
    "endgrain": (160, 16, 16, 16), "lock": (176, 16, 16, 16),
    "gold": (0, 80, 16, 16), "gold_top": (16, 80, 16, 16),
    "wax": (32, 80, 16, 16), "wax_top": (48, 80, 16, 16),
    "flame": (64, 80, 16, 16), "cork": (80, 80, 16, 16),
    "bottle_red": (96, 80, 16, 16), "bottle_blue": (112, 80, 16, 16),
    "bottle_neck": (128, 80, 16, 16), "bottle_top": (144, 80, 16, 16),
    "bone": (160, 80, 16, 16), "skull_front": (176, 80, 16, 16),
    "skull_side": (192, 80, 16, 16), "skull_top": (208, 80, 16, 16), "jaw_front": (224, 80, 16, 16),
    "paper": (0, 96, 32, 16), "paper_end": (32, 96, 16, 16),
    "ribbon": (48, 96, 16, 16), "gem": (64, 96, 16, 16),
    "gem_top": (80, 96, 16, 16), "blade": (96, 96, 16, 32),
    "blade_edge": (112, 96, 16, 16), "grip": (128, 96, 16, 16),
    "pot": (144, 96, 16, 16), "pot_top": (160, 96, 16, 16),
    "web": (0, 144, 32, 32),
}
ELEMENTS = []
GROUPS = {}
CUTOUT = set()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def textured_tile(name, width, height):
    randomizer = random.Random("ft-dungeon-coarse/" + name)
    palette = {
        "top": (84, 58, 33), "edge": (77, 51, 29), "leg": (81, 54, 31),
        "rail": (64, 43, 27), "shelf": (69, 48, 30), "moss_leg": (81, 54, 31),
        "endgrain": (94, 66, 39), "iron": (54, 54, 51), "iron_top": (60, 60, 56),
        "lock": (63, 57, 44), "gold": (185, 135, 28), "gold_top": (218, 172, 44),
        "wax": (216, 204, 163), "wax_top": (235, 222, 179), "flame": (248, 166, 18),
        "cork": (131, 86, 45), "bottle_red": (153, 46, 32), "bottle_blue": (42, 86, 143),
        "bottle_neck": (112, 126, 127), "bottle_top": (112, 126, 127),
        "bone": (179, 168, 133), "skull_front": (203, 190, 154),
        "skull_side": (190, 178, 141), "skull_top": (217, 204, 166), "jaw_front": (185, 172, 134),
        "paper": (214, 197, 155), "paper_end": (193, 172, 123), "ribbon": (147, 43, 32),
        "gem": (21, 139, 55), "gem_top": (31, 177, 74), "blade": (135, 145, 141),
        "blade_edge": (73, 83, 80), "grip": (67, 45, 29), "pot": (121, 77, 38),
        "pot_top": (129, 84, 43), "web": (0, 0, 0),
    }
    tile = Image.new("RGBA", (width, height), palette[name] + (255,))
    draw = ImageDraw.Draw(tile)
    if name in {"top", "edge", "leg", "rail", "shelf", "moss_leg"}:
        vertical = name in {"leg", "moss_leg"}
        if vertical:
            draw.rectangle((0, 0, 1, height - 1), fill=(99, 68, 38))
            draw.rectangle((width - 2, 0, width - 1, height - 1), fill=(57, 39, 26))
            draw.rectangle((3, 4, 4, 11), fill=(72, 48, 29))
            draw.rectangle((2, 16, 3, 19), fill=(105, 72, 40))
        else:
            draw.rectangle((0, 0, width - 1, 1), fill=(103, 73, 42))
            draw.rectangle((0, height - 2, width - 1, height - 1), fill=(56, 39, 25))
            for row in range(5, height - 2, 8):
                column = randomizer.randrange(2, max(3, width // 3))
                draw.rectangle((column, row, min(width - 3, column + width // 2), row), fill=(68, 46, 28))
                draw.rectangle((column + 2, row + 1, min(width - 3, column + width // 3), row + 1), fill=(99, 68, 38))
        if name in {"top", "shelf"}:
            for index in range(12 if name == "top" else 15):
                column = randomizer.randrange(0, width - 3)
                row = randomizer.choice([randomizer.randrange(0, 5), randomizer.randrange(height - 6, height - 2)])
                draw.rectangle((column, row, column + 2, row + 1), fill=(139, 131, 110))
                draw.rectangle((column + 1, row + 1, column + 3, row + 2), fill=(108, 107, 90))
            if name == "top":
                draw.line((16, 0, 15, 8, 16, 13), fill=(37, 29, 21), width=1)
                draw.line((25, 18, 24, 22, 25, 25), fill=(41, 31, 22), width=1)
                draw.rectangle((11, 7, 13, 9), fill=(40, 34, 27))
                draw.rectangle((11, 6, 13, 8), outline=(159, 162, 150))
                draw.rectangle((12, 9, 12, 13), fill=(160, 162, 151))
                draw.rectangle((12, 12, 14, 13), fill=(160, 162, 151))
                draw.point((13, 12), fill=(66, 65, 54))
        if name == "moss_leg":
            draw.rectangle((0, height - 4, 2, height - 1), fill=(70, 87, 43))
            draw.rectangle((1, height - 5, 2, height - 3), fill=(89, 101, 50))
    elif name == "endgrain":
        draw.rectangle((1, 1, 6, 6), outline=(67, 46, 29))
        draw.rectangle((3, 3, 4, 4), fill=(117, 82, 45))
    elif name in {"iron", "iron_top"}:
        draw.line((0, 0, 7, 0), fill=(111, 111, 99))
        draw.line((0, 1, 0, 7), fill=(83, 85, 77))
        draw.rectangle((3, 3, 5, 5), fill=(29, 31, 29))
        draw.rectangle((3, 2, 4, 3), fill=(139, 138, 123))
        draw.rectangle((3, 4, 4, 4), fill=(80, 82, 73))
    elif name == "lock":
        draw.rectangle((0, 0, 7, 7), outline=(113, 96, 63))
        draw.rectangle((3, 2, 4, 3), fill=(26, 26, 22))
        draw.rectangle((3, 4, 3, 6), fill=(26, 26, 22))
    elif name.startswith("gold"):
        draw.rectangle((0, 0, 7, 7), outline=(123, 89, 21))
        draw.line((1, 1, 6, 1), fill=(255, 222, 91))
        draw.line((1, 2, 1, 5), fill=(248, 207, 65))
        draw.rectangle((3, 3, 4, 4), fill=(195, 143, 28))
    elif name.startswith("wax"):
        draw.rectangle((1, 0, 2, 7), fill=(242, 230, 186))
        draw.rectangle((6, 2, 7, 7), fill=(185, 172, 131))
        if name == "wax_top":
            draw.rectangle((3, 3, 4, 4), fill=(69, 53, 32))
    elif name == "flame":
        draw.rectangle((1, 3, 6, 7), fill=(255, 213, 60))
        draw.rectangle((2, 5, 5, 7), fill=(255, 245, 176))
    elif name in {"bottle_red", "bottle_blue"}:
        highlight = (199, 71, 46) if name == "bottle_red" else (65, 120, 184)
        shadow = (99, 32, 26) if name == "bottle_red" else (29, 58, 100)
        draw.rectangle((0, 0, 1, 6), fill=highlight)
        draw.rectangle((6, 1, 7, 7), fill=shadow)
        draw.rectangle((2, 1, 3, 2), fill=(221, 229, 207))
        draw.line((0, 7, 7, 7), fill=(113, 124, 116))
    elif name.startswith("bottle"):
        draw.line((0, 0, 7, 0), fill=(180, 191, 181))
        draw.rectangle((1, 1, 2, 4), fill=(209, 219, 204))
        draw.rectangle((6, 1, 7, 7), fill=(62, 79, 83))
    elif name == "jaw_front":
        draw.line((1, 2, 6, 2), fill=(82, 68, 44))
        for column in [2, 4, 6]:
            draw.line((column, 2, column, 4), fill=(111, 94, 61))
    elif name.startswith("skull"):
        draw.line((0, 0, 7, 0), fill=(234, 222, 183))
        draw.rectangle((6, 2, 7, 7), fill=(178, 164, 126))
        if name == "skull_front":
            draw.rectangle((1, 2, 2, 4), fill=(43, 38, 28))
            draw.rectangle((5, 2, 6, 4), fill=(38, 35, 27))
            draw.rectangle((3, 4, 4, 5), fill=(54, 46, 31))
            draw.line((1, 7, 6, 7), fill=(86, 69, 44))
        elif name == "skull_side":
            draw.rectangle((1, 4, 2, 5), fill=(126, 110, 77))
    elif name == "paper":
        draw.line((0, 0, 15, 0), fill=(242, 224, 180))
        draw.line((0, 7, 15, 7), fill=(157, 133, 88))
        draw.line((3, 3, 7, 3), fill=(172, 149, 103))
        draw.line((3, 5, 10, 5), fill=(181, 157, 110))
    elif name == "paper_end":
        draw.rectangle((1, 1, 6, 6), outline=(232, 211, 166))
        draw.rectangle((2, 2, 5, 5), outline=(143, 119, 76))
        draw.rectangle((3, 3, 4, 4), fill=(211, 188, 141))
    elif name == "ribbon":
        draw.line((0, 0, 0, 7), fill=(206, 76, 49))
        draw.rectangle((6, 0, 7, 7), fill=(95, 31, 25))
    elif name.startswith("gem"):
        draw.polygon([(0, 2), (3, 0), (4, 2), (1, 6)], fill=(72, 220, 110))
        draw.polygon([(5, 0), (7, 2), (7, 7), (5, 6)], fill=(12, 101, 42))
        draw.rectangle((2, 1, 3, 2), fill=(184, 249, 190))
    elif name == "blade":
        draw.rectangle((0, 0, 2, 15), fill=(200, 211, 198))
        draw.rectangle((6, 0, 7, 15), fill=(79, 91, 88))
        draw.line((3, 0, 4, 15), fill=(233, 236, 216))
    elif name == "grip":
        for row in [2, 5]:
            draw.line((0, row, 7, row), fill=(40, 34, 27))
    elif name == "cork":
        draw.rectangle((0, 0, 7, 1), fill=(164, 115, 63))
        draw.rectangle((6, 2, 7, 7), fill=(102, 66, 38))
    elif name == "pot":
        draw.line((0, 0, 7, 0), fill=(165, 111, 58))
        draw.rectangle((0, 1, 1, 6), fill=(144, 91, 43))
        draw.rectangle((6, 1, 7, 7), fill=(83, 56, 31))
    elif name == "pot_top":
        draw.rectangle((1, 1, 6, 6), fill=(67, 46, 27))
        draw.rectangle((2, 2, 5, 5), fill=(32, 28, 20))
        draw.line((0, 0, 7, 0), fill=(174, 122, 65))
    elif name == "web":
        tile = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(tile)
        for endpoint in [(15, 0), (13, 7), (7, 13), (0, 15)]:
            draw.line((0, 0, *endpoint), fill=(174, 173, 149, 255))
        for radius in [5, 10, 15]:
            draw.line([(radius, 0), (round(radius * .87), round(radius * .45)),
                       (round(radius * .45), round(radius * .87)), (0, radius)], fill=(156, 157, 137, 255))
    return tile


def make_texture():
    atlas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for name, (left, top, width, height) in TILES.items():
        atlas.alpha_composite(textured_tile(name, width // 2, height // 2), (left // 2, top // 2))
    destination = ASSETS / "textures/block/trading_table.png"
    destination.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(destination)
    atlas.resize((1024, 1024), Image.Resampling.NEAREST).save(OUTPUT / "图集放大.png")
    return destination


def uv(material):
    left, top, width, height = TILES[material]
    return [left / 16, top / 16, (left + width) / 16, (top + height) / 16]


def box(name, start, end, material, group, overrides=None, rotation=None, sides=None):
    faces = {side: {"uv": uv((overrides or {}).get(side, material)), "texture": "#body"} for side in (sides or FACES)}
    element = {"name": name, "from": [round(value, 5) for value in start], "to": [round(value, 5) for value in end], "faces": faces}
    if rotation:
        element["rotation"] = {"origin": rotation[0], "axis": rotation[1], "angle": rotation[2]}
    ELEMENTS.append(element)
    GROUPS[name] = group
    return element


def horizontal_uv(element, material):
    left, top, width, height = TILES[material]
    element["faces"]["up"]["uv"] = [(left + element["from"][0] / 16 * width) / 16,
                                          (top + element["from"][2] / 16 * height) / 16,
                                          (left + element["to"][0] / 16 * width) / 16,
                                          (top + element["to"][2] / 16 * height) / 16]


def make_frame():
    for column in [1.1, 12.6]:
        for depth in [1.1, 12.6]:
            box(f"Timber leg {column} {depth}", [column, 0, depth], [column + 2.3, 10.15, depth + 2.3],
                "moss_leg" if column == depth else "leg", "承重木架", {"up": "endgrain", "down": "endgrain"})
    for index, (start, end) in enumerate([
        ([.5, 9.6, .5], [4.1, 11.1, 15.5]),
        ([4.22, 9.6, .5], [7.85, 11.1, 15.5]),
        ([8.02, 9.6, 1.4], [11.65, 11.1, 15.5]),
        ([8.02, 9.6, .5], [9.3, 11.1, 1.4]),
        ([9.3, 9.6, .85], [11.65, 11.1, 1.4]),
        ([11.8, 9.6, .5], [15.5, 11.1, 15.5]),
    ]):
        plank = box(f"Worn desktop plank {index + 1}", start, end, "edge", "破损桌面", {"up": "top", "down": "rail"})
        horizontal_uv(plank, "top")
    for label, start, end in [
        ("front", [.9, 8.25, .8], [15.1, 9.65, 2.1]),
        ("rear", [.9, 8.25, 13.9], [15.1, 9.65, 15.2]),
        ("left", [.8, 8.25, 2.1], [2.1, 9.65, 13.9]),
        ("right", [13.9, 8.25, 2.1], [15.2, 9.65, 13.9]),
        ("lower front", [2.2, 1.9, 1.8], [13.8, 3.1, 3.0]),
        ("lower left", [1.8, 1.9, 2.5], [3.0, 3.1, 13.8]),
        ("lower right", [13.0, 1.9, 2.5], [14.2, 3.1, 13.8]),
        ("rear brace", [2.3, 2.0, 13.8], [13.7, 7.5, 14.4]),
    ]:
        box("Apron / " + label, start, end, "rail", "承重木架")
    shelf = box("Dusty storage shelf", [2.7, 3.0, 2.5], [13.3, 3.55, 13.7], "rail", "承重木架", {"up": "shelf"})
    horizontal_uv(shelf, "shelf")
    for column in [.42, 13.85]:
        for depth in [.42, 13.85]:
            label = f"Riveted corner {column} {depth}"
            box(label + " solid cap", [column, 9.4, depth], [column + 1.73, 11.26, depth + 1.73], "iron", "铁件与锁", {"up": "iron_top"})
    box("Old contract lock", [7.5, 8.35, .55], [8.65, 10.1, .79], "iron", "铁件与锁", {"north": "lock"})


def make_bottle(label, center_x, center_z, height, color):
    base = 11.1
    for part, half_width, bottom, top, material in [
        ("body", .98, 0, height - 1.5, color),
        ("shoulder", .72, height - 1.5, height - 1.0, color),
        ("neck", .45, height - 1.0, height - .38, "bottle_neck"),
        ("cork", .38, height - .38, height, "cork"),
    ]:
        box(label + " / " + part, [center_x - half_width, base + bottom, center_z - half_width],
            [center_x + half_width, base + top, center_z + half_width], material, "红蓝药瓶")


def make_props():
    make_bottle("Red restorative bottle", 6.3, 12.85, 4.75, "bottle_red")
    make_bottle("Blue mana bottle", 9.15, 12.15, 3.95, "bottle_blue")
    box("Candle / brass saucer", [1.65, 11.1, 10.35], [4.1, 11.35, 12.8], "gold", "蜡烛", {"up": "gold_top"}, ([2.88, 11.1, 11.58], "y", 22.5))
    box("Candle / wax body", [2.27, 11.35, 10.97], [3.5, 14.35, 12.2], "wax", "蜡烛", {"up": "wax_top"})
    box("Candle / wick", [2.82, 14.35, 11.5], [2.98, 14.65, 11.66], "grip", "蜡烛")
    box("Candle / golden flame", [2.65, 14.58, 11.34], [3.14, 15.55, 11.83], "flame", "蜡烛")
    box("Skull / cranium", [11.3, 11.63, 11.4], [13.75, 14.1, 13.8], "skull_side", "颅骨与陶器",
        {"north": "skull_front", "up": "skull_top", "down": "bone"})
    box("Skull / jaw", [11.45, 11.1, 11.25], [13.6, 11.68, 13.38], "bone", "颅骨与陶器", {"north": "jaw_front"})
    for label, center_x, center_z, size in [("Tall pot", 14.1, 9.25, 1.6), ("Chipped cup", 12.9, 7.6, 1.2)]:
        box(label + " / belly", [center_x - size * .5, 11.1, center_z - size * .5], [center_x + size * .5, 12.2 + size * .3, center_z + size * .5], "pot", "颅骨与陶器")
        box(label + " / hollow lip", [center_x - size * .38, 12.2 + size * .3, center_z - size * .38], [center_x + size * .38, 12.5 + size * .3, center_z + size * .38], "pot", "颅骨与陶器", {"up": "pot_top"})
    origin = [8.2, 11.1, 8.7]
    turn = (origin, "y", 22.5)
    box("Scroll / rolled parchment", [5.45, 11.15, 8.0], [10.95, 11.91, 9.4], "paper", "卷轴与钱币", {"east": "paper_end", "west": "paper_end"}, turn)
    box("Scroll / red seal band", [7.95, 11.13, 7.96], [8.55, 11.94, 9.44], "ribbon", "卷轴与钱币", rotation=turn)
    for index, (column, depth, bottom, width) in enumerate([(2.2, 8.4, 11.1, 1.35), (3.65, 8.6, 11.1, 1.45), (2.85, 9.6, 11.1, 1.45), (2.98, 9.65, 11.38, 1.15)]):
        box(f"Coin pile / coin {index + 1}", [column, bottom, depth], [column + width, bottom + .27, depth + width], "gold", "卷轴与钱币", {"up": "gold_top"}, ([column + width / 2, bottom, depth + width / 2], "y", 22.5 if index % 2 else 45))
    box("Emerald / lower facet", [3.3, 11.1, 5.8], [4.9, 11.54, 7.2], "gem", "钥匙宝石匕首", {"up": "gem_top"}, ([4.1, 11.1, 6.5], "y", 22.5))
    box("Emerald / crown", [3.56, 11.54, 5.99], [4.64, 11.95, 7.01], "gem", "钥匙宝石匕首", {"up": "gem_top"}, ([4.1, 11.1, 6.5], "y", 22.5))
    dagger_turn = ([11.8, 11.1, 4.6], "y", -22.5)
    for part, start, end, material in [
        ("blade", [11.35, 11.18, 2.4], [12.25, 11.42, 5.0], "blade"),
        ("point", [11.58, 11.18, 1.8], [12.02, 11.42, 2.4], "blade"),
        ("crossguard", [10.82, 11.13, 4.98], [12.78, 11.5, 5.36], "blade_edge"),
        ("wrapped grip", [11.52, 11.18, 5.36], [12.08, 11.55, 6.8], "grip"),
        ("pommel", [11.44, 11.14, 6.8], [12.16, 11.59, 7.14], "gold"),
    ]:
        box("Dagger / " + part, start, end, material, "钥匙宝石匕首", rotation=dagger_turn)
    first_web = box("Cobweb / front left", [3.38, 5.0, 2.14], [7.5, 8.26, 2.14], "web", "蛛网", sides=["north", "south"])
    first_web["faces"]["north"]["rotation"] = 180
    second_web = box("Cobweb / right rear", [13.87, 5.0, 8.5], [13.87, 8.26, 12.6], "web", "蛛网", sides=["east", "west"])
    second_web["faces"]["west"]["rotation"] = 90
    CUTOUT.update([first_web["name"], second_web["name"]])


def optimize_hidden():
    originals = copy.deepcopy(ELEMENTS)
    proofs = []
    directions = {"north": (2, -1), "south": (2, 1), "east": (0, 1), "west": (0, -1), "up": (1, 1), "down": (1, -1)}
    for element in originals:
        if "rotation" in element or element["name"] in CUTOUT:
            continue
        for side in element["faces"]:
            axis, sign = directions[side]
            coordinate = element["to" if sign > 0 else "from"][axis]
            for other in originals:
                if "rotation" in other or other["name"] == element["name"] or other["name"] in CUTOUT:
                    continue
                extends_beyond = other["to"][axis] > coordinate + 1e-6 if sign > 0 else other["from"][axis] < coordinate - 1e-6
                covers_face = all(other["from"][other_axis] <= element["from"][other_axis] and other["to"][other_axis] >= element["to"][other_axis] for other_axis in range(3) if other_axis != axis)
                if other["from"][axis] <= coordinate <= other["to"][axis] and extends_beyond and covers_face:
                    proofs.append({"element": element["name"], "face": side, "opaque_occluder": other["name"]})
                    break
    lookup = {element["name"]: element for element in ELEMENTS}
    for proof in proofs:
        del lookup[proof["element"]]["faces"][proof["face"]]
    return proofs


def align_reference_front():
    for element in ELEMENTS:
        first, second = element["from"][0], element["to"][0]
        element["from"][0], element["to"][0] = round(16 - second, 5), round(16 - first, 5)
        east, west = element["faces"].pop("east", None), element["faces"].pop("west", None)
        if east:
            element["faces"]["west"] = east
        if west:
            element["faces"]["east"] = west
        if "rotation" in element:
            rotation = element["rotation"]
            rotation["origin"] = list(rotation["origin"])
            rotation["origin"][0] = round(16 - rotation["origin"][0], 5)
            if rotation["axis"] in {"y", "z"}:
                rotation["angle"] = -rotation["angle"]


def export(texture, proofs):
    display = {
        "gui": {"rotation": [28, 225, 0], "translation": [0, 1, 0], "scale": [.7, .7, .7]},
        "ground": {"translation": [0, 3, 0], "scale": [.25, .25, .25]},
        "fixed": {"rotation": [0, 180, 0], "scale": [.55, .55, .55]},
        "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [.375, .375, .375]},
        "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 2.5, 0], "scale": [.375, .375, .375]},
        "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 1.0, 0], "scale": [.4, .4, .4]},
        "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 1.0, 0], "scale": [.4, .4, .4]},
    }
    runtime = {"credit": "FT original dungeon merchant table remake, 2026-11-01", "render_type": "minecraft:cutout", "textures": {"body": "ftdungeontrading:block/trading_table", "particle": "#body"}, "elements": ELEMENTS, "display": display}
    write_json(ASSETS / "models/block/trading_table.json", runtime)
    write_json(ASSETS / "models/item/trading_table.json", {"parent": "ftdungeontrading:block/trading_table"})
    write_json(ASSETS / "blockstates/trading_table.json", {"variants": {"facing=" + direction: {"model": "ftdungeontrading:block/trading_table", "y": angle} for direction, angle in [("north", 0), ("east", 90), ("south", 180), ("west", 270)]}})
    cubes = []
    groups = {}
    for element in ELEMENTS:
        identifier = str(uuid.uuid5(uuid.NAMESPACE_URL, "fttrading/remodel/" + element["name"]))
        rotation = element.get("rotation", {"origin": [8, 0, 8], "axis": "y", "angle": 0})
        angles = [0, 0, 0]
        angles[{"x": 0, "y": 1, "z": 2}[rotation["axis"]]] = rotation["angle"]
        faces = {side: {"texture": None, "uv": [0, 0, 0, 0]} for side in FACES}
        faces.update({side: {**copy.deepcopy(data), "texture": 0} for side, data in element["faces"].items()})
        cubes.append({"name": element["name"], "type": "cube", "uuid": identifier, "from": element["from"], "to": element["to"], "origin": rotation["origin"], "rotation": angles, "autouv": 0, "box_uv": False, "faces": faces})
        groups.setdefault(GROUPS[element["name"]], []).append(identifier)
    editable = {"meta": {"format_version": "4.10", "model_format": "java_block", "box_uv": False}, "name": "地牢旧货交易桌_1.1.0", "resolution": {"width": 16, "height": 16}, "elements": cubes,
                "outliner": [{"name": name, "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, "fttrading/remodel/group/" + name)), "origin": [8, 0, 8], "rotation": [0, 0, 0], "children": children} for name, children in groups.items()],
                "textures": [{"name": "trading_table.png", "id": "0", "uuid": str(uuid.uuid5(uuid.NAMESPACE_URL, "fttrading/remodel/texture")), "namespace": "ftdungeontrading", "folder": "block", "width": SIZE, "height": SIZE, "uv_width": 16, "uv_height": 16, "source": "data:image/png;base64," + base64.b64encode(texture.read_bytes()).decode("ascii"), "mode": "bitmap"}], "display": display}
    write_json(OUTPUT / "地牢交易桌.bbmodel", editable)
    gray_image = Image.open(texture).convert("RGBA")
    gray_image.paste((173, 174, 170), (0, 0, SIZE, SIZE), gray_image.getchannel("A"))
    gray_path = OUTPUT / "qa/gray-atlas.png"
    gray_image.save(gray_path)
    gray = copy.deepcopy(editable)
    gray["textures"][0]["source"] = "data:image/png;base64," + base64.b64encode(gray_path.read_bytes()).decode("ascii")
    write_json(OUTPUT / "qa/灰模.bbmodel", gray)
    faces = sum(len(element["faces"]) for element in ELEMENTS)
    write_json(OUTPUT / "budget.json", {"budgets": {"components": 64, "declared_faces": 352, "texture_pixels": 16384}, "lanes": []})
    assert len(ELEMENTS) <= 64 and faces <= 352
    assert len({element["name"] for element in ELEMENTS}) == len(ELEMENTS)
    for element in ELEMENTS:
        assert all(0 <= value <= 16 for value in element["from"] + element["to"]), element["name"]
        assert all(element["from"][axis] <= element["to"][axis] for axis in range(3)), element["name"]
        if element["name"] not in CUTOUT:
            assert all(element["from"][axis] < element["to"][axis] for axis in range(3)), element["name"]
    report = {"version": "1.1.0", "components": len(ELEMENTS), "declared_faces": faces, "textures": 1, "texture_pixels": SIZE * SIZE,
              "cutout_planes": len(CUTOUT), "cutout_components": sorted(CUTOUT), "transparent_shells": 0,
              "geometry_animation_copies": 0, "removed_hidden_faces": len(proofs), "dynamic_product_previews_maximum": 6, "client_tested": False,
              "texture_style": "coarse clustered pixels, manually redrawn; tabletop 32x32, small props 8x8",
              "geometry_changes": {"previous_candidate_components": 84, "removed_components": 84 - len(ELEMENTS), "key_representation": "painted tabletop detail, no independent geometry"}}
    write_json(OUTPUT / "model-report.json", report)
    write_json(OUTPUT / "hidden-face-proofs.json", proofs)
    print(json.dumps(report, ensure_ascii=False))


def main():
    texture = make_texture()
    make_frame()
    make_props()
    align_reference_front()
    export(texture, optimize_hidden())


if __name__ == "__main__":
    main()
