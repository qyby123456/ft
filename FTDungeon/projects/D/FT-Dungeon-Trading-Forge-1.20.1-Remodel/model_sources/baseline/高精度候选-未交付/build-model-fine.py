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
SIZE = 256
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
    randomizer = random.Random("ft-dungeon-remodel/" + name)
    palette = {
        "top": (77, 52, 30), "edge": (69, 45, 27), "leg": (76, 51, 30),
        "rail": (59, 39, 25), "shelf": (62, 44, 28), "moss_leg": (73, 50, 29),
        "endgrain": (86, 60, 36), "iron": (52, 52, 49), "iron_top": (52, 52, 49),
        "lock": (55, 52, 43), "gold": (181, 130, 26), "gold_top": (211, 165, 41),
        "wax": (211, 199, 153), "wax_top": (227, 215, 170), "flame": (248, 173, 20),
        "cork": (126, 82, 40), "bottle_red": (146, 42, 25), "bottle_blue": (37, 80, 133),
        "bottle_neck": (99, 112, 115), "bottle_top": (102, 113, 112),
        "bone": (172, 162, 127), "skull_front": (194, 181, 145),
        "skull_side": (181, 168, 133), "skull_top": (207, 194, 157), "jaw_front": (173, 160, 123),
        "paper": (205, 188, 143), "paper_end": (180, 159, 110), "ribbon": (135, 38, 28),
        "gem": (15, 124, 49), "gem_top": (22, 171, 67), "blade": (121, 129, 126),
        "blade_edge": (66, 75, 73), "grip": (62, 43, 28), "pot": (112, 70, 33),
        "pot_top": (110, 71, 36), "web": (0, 0, 0),
    }
    tile = Image.new("RGBA", (width, height), palette[name] + (255,))
    pixels = tile.load()
    for row in range(height):
        for column in range(width):
            variation = randomizer.choice([-15, -8, -4, 0, 0, 4, 7, 12])
            pixels[column, row] = tuple(max(0, min(255, channel + variation)) for channel in palette[name]) + (255,)
    draw = ImageDraw.Draw(tile)
    if name in {"top", "edge", "leg", "rail", "shelf", "moss_leg"}:
        vertical = name in {"leg", "moss_leg"}
        for grain in range(1, width if vertical else height, 4):
            color = randomizer.choice([(39, 28, 20), (52, 35, 23), (97, 66, 35), (112, 79, 44)])
            if vertical:
                start = randomizer.randrange(0, height // 2)
                draw.line((grain, start, grain, min(height - 1, start + randomizer.randrange(9, height))), fill=color)
            else:
                start = randomizer.randrange(0, width // 2)
                draw.line((start, grain, min(width - 1, start + randomizer.randrange(9, width)), grain), fill=color)
        for index in range(5):
            column = randomizer.randrange(2, width - 3)
            row = randomizer.randrange(2, height - 3)
            draw.line((column, row, min(width - 1, column + 5), row), fill=(38, 28, 20))
            draw.point((column + 1, row + 1), fill=(127, 91, 49))
        if name in {"top", "shelf"}:
            for index in range(175 if name == "top" else 220):
                column = randomizer.randrange(width)
                row = randomizer.randrange(height)
                edge_distance = min(column, row, width - column - 1, height - row - 1)
                if edge_distance < 10 or randomizer.random() < .10:
                    color = randomizer.choice([(133, 128, 107), (104, 104, 89), (161, 153, 124), (91, 91, 76)])
                    draw.rectangle((column, row, min(width - 1, column + randomizer.randrange(1, 4)), min(height - 1, row + randomizer.randrange(1, 3))), fill=color)
            draw.line((32, 0, 31, 19, 33, 30, 32, height - 1), fill=(27, 24, 18), width=1)
        if name == "edge":
            draw.line((0, 0, width - 1, 0), fill=(129, 90, 48), width=2)
            draw.line((0, height - 1, width - 1, height - 1), fill=(37, 28, 21), width=2)
            draw.rectangle((29, 2, 32, 9), fill=(38, 27, 19))
            draw.rectangle((31, 7, 32, 15), fill=(26, 23, 17))
        if name == "moss_leg":
            for index in range(12):
                column = randomizer.randrange(1, width - 2)
                row = randomizer.randrange(height - 7, height - 1)
                draw.rectangle((column, row, min(width - 1, column + 2), min(height - 1, row + 2)), fill=randomizer.choice([(65, 76, 34), (77, 86, 38), (50, 64, 31)]))
    elif name.startswith("iron"):
        draw.rectangle((0, 0, width - 1, height - 1), outline=(27, 30, 29))
        draw.line((1, 1, width - 2, 1), fill=(104, 104, 91))
        draw.rectangle((5, 5, 10, 10), fill=(27, 29, 28))
        draw.rectangle((6, 5, 9, 8), fill=(115, 115, 103))
        draw.rectangle((7, 6, 9, 8), fill=(78, 79, 70))
        draw.point((2, 12), fill=(98, 66, 37))
    elif name == "lock":
        draw.rectangle((1, 1, 14, 14), outline=(97, 87, 59), width=2)
        draw.rectangle((6, 5, 9, 8), fill=(18, 20, 17))
        draw.rectangle((7, 8, 8, 12), fill=(18, 20, 17))
    elif name.startswith("gold"):
        draw.rectangle((0, 0, 15, 15), outline=(104, 78, 18))
        draw.line((1, 1, 13, 1), fill=(255, 224, 91), width=2)
        draw.line((1, 2, 1, 12), fill=(245, 206, 62), width=2)
        draw.rectangle((5, 5, 10, 10), outline=(237, 192, 54))
    elif name.startswith("wax"):
        draw.line((2, 1, 2, 12), fill=(240, 228, 184), width=2)
        draw.rectangle((10, 3, 12, 7), fill=(181, 169, 126))
        if name == "wax_top":
            draw.rectangle((5, 5, 10, 10), fill=(180, 157, 104))
            draw.rectangle((7, 5, 8, 8), fill=(38, 32, 22))
    elif name == "flame":
        draw.rectangle((3, 5, 12, 15), fill=(255, 215, 58))
        draw.rectangle((6, 10, 10, 15), fill=(255, 244, 159))
        draw.rectangle((0, 0, 15, 2), fill=(230, 119, 16))
    elif name in {"bottle_red", "bottle_blue"}:
        light = (192, 66, 41) if name == "bottle_red" else (61, 112, 172)
        shadow = (81, 29, 24) if name == "bottle_red" else (26, 51, 85)
        draw.line((0, 0, 15, 0), fill=light)
        draw.line((1, 2, 1, 13), fill=light)
        draw.rectangle((3, 3, 5, 6), fill=(204, 218, 208))
        draw.rectangle((6, 3, 7, 4), fill=(141, 177, 188))
        draw.line((13, 2, 13, 15), fill=shadow, width=2)
        draw.line((0, 15, 15, 15), fill=(74, 83, 80))
    elif name.startswith("bottle"):
        draw.line((0, 0, 15, 0), fill=(142, 159, 159), width=2)
        draw.line((1, 2, 1, 14), fill=(82, 93, 98), width=2)
        draw.rectangle((3, 3, 5, 8), fill=(200, 211, 202))
        draw.rectangle((6, 3, 8, 4), fill=(131, 163, 171))
        draw.line((13, 3, 13, 15), fill=(35, 50, 56), width=2)
        draw.line((0, 15, 15, 15), fill=(69, 80, 79), width=2)
    elif name == "jaw_front":
        draw.line((1, 2, 14, 2), fill=(74, 60, 39), width=2)
        for column in [3, 6, 9, 12]:
            draw.line((column, 2, column, 9), fill=(87, 72, 46))
        draw.line((2, 13, 13, 13), fill=(134, 116, 78))
    elif name.startswith("skull"):
        draw.rectangle((0, 0, 15, 1), fill=(223, 210, 172))
        draw.line((1, 1, 1, 14), fill=(211, 198, 160))
        if name == "skull_front":
            draw.rectangle((2, 5, 6, 9), fill=(39, 34, 25))
            draw.rectangle((10, 5, 13, 9), fill=(34, 31, 24))
            draw.rectangle((7, 9, 9, 12), fill=(43, 37, 26))
            draw.line((3, 14, 12, 14), fill=(67, 55, 36))
            draw.line((8, 0, 7, 3, 8, 4), fill=(129, 115, 81))
        elif name == "skull_side":
            draw.rectangle((1, 7, 3, 10), fill=(95, 82, 55))
            draw.line((10, 1, 9, 4, 11, 6), fill=(120, 108, 76))
    elif name == "paper":
        draw.line((0, 0, 31, 0), fill=(240, 220, 170), width=2)
        draw.line((0, 15, 31, 15), fill=(141, 118, 74), width=2)
        for row in [5, 8, 11]:
            draw.line((4, row, 25 - row, row), fill=(145, 122, 80))
    elif name == "paper_end":
        for inset, color in [(1, (222, 204, 156)), (3, (139, 114, 69)), (5, (211, 190, 136)), (7, (114, 92, 56))]:
            draw.rectangle((inset, inset, 15 - inset, 15 - inset), outline=color)
    elif name == "ribbon":
        draw.line((2, 0, 2, 15), fill=(202, 72, 47), width=2)
        draw.line((13, 0, 13, 15), fill=(81, 27, 22), width=2)
    elif name.startswith("gem"):
        draw.polygon([(0, 4), (6, 0), (9, 5), (2, 13)], fill=(54, 211, 96))
        draw.polygon([(10, 1), (15, 4), (15, 15), (10, 12)], fill=(9, 88, 35))
        draw.rectangle((4, 3, 7, 5), fill=(170, 249, 184))
    elif name == "blade":
        draw.polygon([(0, 0), (5, 0), (9, 31), (0, 31)], fill=(189, 200, 193))
        draw.line((7, 0, 9, 31), fill=(225, 231, 219), width=2)
        draw.rectangle((12, 0, 15, 31), fill=(67, 78, 77))
    elif name == "grip":
        for row in range(2, 16, 4):
            draw.line((0, row, 15, row), fill=(27, 31, 28), width=2)
        draw.rectangle((6, 6, 9, 9), fill=(165, 126, 53))
    elif name == "pot":
        draw.line((1, 1, 14, 1), fill=(159, 105, 53), width=2)
        draw.line((1, 3, 1, 13), fill=(144, 88, 40), width=2)
        draw.line((13, 4, 13, 15), fill=(67, 45, 26), width=2)
    elif name == "pot_top":
        draw.rectangle((2, 2, 13, 13), fill=(61, 41, 24))
        draw.rectangle((4, 4, 11, 11), fill=(27, 24, 18))
        draw.line((0, 0, 15, 0), fill=(169, 114, 59), width=2)
    elif name == "web":
        tile = Image.new("RGBA", (width, height), (0, 0, 0, 0))
        draw = ImageDraw.Draw(tile)
        color = (175, 174, 150, 255)
        for end in [(31, 0), (29, 12), (22, 22), (12, 29), (0, 31)]:
            draw.line((0, 0, *end), fill=color)
        for radius in [7, 14, 22, 30]:
            points = [(round(radius * math.cos(math.radians(angle))), round(radius * math.sin(math.radians(angle)))) for angle in [0, 22.5, 45, 67.5, 90]]
            draw.line(points, fill=color)
    return tile


def make_texture():
    atlas = Image.new("RGBA", (SIZE, SIZE), (0, 0, 0, 0))
    for name, (left, top, width, height) in TILES.items():
        atlas.alpha_composite(textured_tile(name, width, height), (left, top))
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
            box(label + " cap", [column, 11.105, depth], [column + 1.73, 11.26, depth + 1.73], "iron_top", "铁件与锁", {"down": "iron"})
            border = depth if depth < 8 else depth + 1.58
            box(label + " front wrap", [column, 9.4, border], [column + 1.73, 11.12, border + .16], "iron", "铁件与锁")
            border = column if column < 8 else column + 1.58
            box(label + " side wrap", [border, 9.4, depth + .16], [border + .16, 11.12, depth + 1.57], "iron", "铁件与锁")
    box("Old contract lock", [7.5, 8.35, .55], [8.65, 10.1, .79], "iron", "铁件与锁", {"north": "lock"})


def make_bottle(label, center_x, center_z, height, color):
    base = 11.1
    for part, half_width, bottom, top, material in [
        ("foot", .82, 0, .32, "bottle_neck"),
        ("body", .98, .32, height - 1.5, color),
        ("shoulder", .72, height - 1.5, height - 1.0, color),
        ("neck", .40, height - 1.0, height - .42, "bottle_neck"),
        ("lip", .55, height - .64, height - .38, "bottle_top"),
        ("cork", .38, height - .38, height, "cork"),
    ]:
        box(label + " / " + part, [center_x - half_width, base + bottom, center_z - half_width],
            [center_x + half_width, base + top, center_z + half_width], material, "红蓝药瓶")


def make_props():
    make_bottle("Red restorative bottle", 6.3, 12.85, 4.75, "bottle_red")
    make_bottle("Blue mana bottle", 9.15, 12.15, 3.95, "bottle_blue")
    box("Candle / brass saucer", [1.65, 11.1, 10.35], [4.1, 11.35, 12.8], "gold", "蜡烛", {"up": "gold_top"}, ([2.88, 11.1, 11.58], "y", 22.5))
    box("Candle / wax puddle", [2.05, 11.35, 10.75], [3.75, 11.48, 12.42], "wax", "蜡烛", {"up": "wax_top"})
    box("Candle / wax body", [2.27, 11.45, 10.97], [3.5, 14.35, 12.2], "wax", "蜡烛", {"up": "wax_top"})
    box("Candle / wick", [2.82, 14.35, 11.5], [2.98, 14.65, 11.66], "grip", "蜡烛")
    box("Candle / golden flame", [2.65, 14.58, 11.34], [3.14, 15.55, 11.83], "flame", "蜡烛")
    box("Candle / light core", [2.74, 14.55, 11.26], [3.04, 14.96, 11.84], "wax_top", "蜡烛")
    box("Skull / cranium", [11.3, 11.63, 11.4], [13.75, 14.1, 13.8], "skull_side", "颅骨与陶器",
        {"north": "skull_front", "up": "skull_top", "down": "bone"})
    box("Skull / jaw", [11.45, 11.1, 11.25], [13.6, 11.68, 13.38], "bone", "颅骨与陶器", {"north": "jaw_front"})
    for label, center_x, center_z, size in [("Tall pot", 14.1, 9.25, 1.6), ("Chipped cup", 12.9, 7.6, 1.2)]:
        box(label + " / foot", [center_x - size * .32, 11.1, center_z - size * .32], [center_x + size * .32, 11.4, center_z + size * .32], "pot", "颅骨与陶器")
        box(label + " / belly", [center_x - size * .5, 11.4, center_z - size * .5], [center_x + size * .5, 12.2 + size * .3, center_z + size * .5], "pot", "颅骨与陶器")
        box(label + " / hollow lip", [center_x - size * .38, 12.2 + size * .3, center_z - size * .38], [center_x + size * .38, 12.5 + size * .3, center_z + size * .38], "pot", "颅骨与陶器", {"up": "pot_top"})
    origin = [8.2, 11.1, 8.7]
    turn = (origin, "y", 22.5)
    box("Scroll / rolled parchment", [5.45, 11.15, 8.0], [10.95, 11.91, 9.4], "paper", "卷轴与钱币", {"east": "paper_end", "west": "paper_end"}, turn)
    for side, column in [("left", 5.25), ("right", 10.75)]:
        box("Scroll / " + side + " rolled end", [column, 11.1, 7.86], [column + .45, 12.01, 9.54], "paper", "卷轴与钱币", {"east": "paper_end", "west": "paper_end"}, turn)
    box("Scroll / red seal band", [7.95, 11.13, 7.96], [8.55, 11.94, 9.44], "ribbon", "卷轴与钱币", rotation=turn)
    for index, (column, depth, bottom, width) in enumerate([(2.2, 8.4, 11.1, 1.2), (3.5, 8.2, 11.1, 1.35), (4.3, 9.2, 11.1, 1.3), (3.0, 9.5, 11.1, 1.4), (3.15, 9.45, 11.38, 1.15), (2.25, 9.6, 11.1, 1.15), (3.12, 9.45, 11.67, .95)]):
        box(f"Coin pile / coin {index + 1}", [column, bottom, depth], [column + width, bottom + .27, depth + width], "gold", "卷轴与钱币", {"up": "gold_top"}, ([column + width / 2, bottom, depth + width / 2], "y", 22.5 if index % 2 else 45))
    box("Emerald / lower facet", [3.3, 11.1, 5.8], [4.9, 11.54, 7.2], "gem", "钥匙宝石匕首", {"up": "gem_top"}, ([4.1, 11.1, 6.5], "y", 22.5))
    box("Emerald / crown", [3.56, 11.54, 5.99], [4.64, 11.95, 7.01], "gem", "钥匙宝石匕首", {"up": "gem_top"}, ([4.1, 11.1, 6.5], "y", 22.5))
    for part, start, end in [
        ("ring left", [7.2, 11.12, 5.25], [7.48, 11.35, 6.5]),
        ("ring right", [8.24, 11.12, 5.25], [8.52, 11.35, 6.5]),
        ("ring front", [7.48, 11.12, 5.25], [8.24, 11.35, 5.53]),
        ("ring rear", [7.48, 11.12, 6.22], [8.24, 11.35, 6.5]),
        ("shaft", [7.73, 11.12, 3.4], [7.99, 11.35, 5.3]),
        ("tooth", [7.99, 11.12, 3.5], [8.6, 11.35, 3.86]),
    ]:
        box("Old key / " + part, start, end, "blade_edge", "钥匙宝石匕首", rotation=([7.9, 11.1, 5.0], "y", -22.5))
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
    write_json(OUTPUT / "budget.json", {"budgets": {"components": 112, "declared_faces": 620, "texture_pixels": 65536}, "lanes": []})
    assert len(ELEMENTS) <= 112 and faces <= 620
    assert len({element["name"] for element in ELEMENTS}) == len(ELEMENTS)
    for element in ELEMENTS:
        assert all(0 <= value <= 16 for value in element["from"] + element["to"]), element["name"]
        assert all(element["from"][axis] <= element["to"][axis] for axis in range(3)), element["name"]
        if element["name"] not in CUTOUT:
            assert all(element["from"][axis] < element["to"][axis] for axis in range(3)), element["name"]
    report = {"version": "1.1.0", "components": len(ELEMENTS), "declared_faces": faces, "textures": 1, "texture_pixels": SIZE * SIZE,
              "cutout_planes": len(CUTOUT), "cutout_components": sorted(CUTOUT), "transparent_shells": 0,
              "geometry_animation_copies": 0, "removed_hidden_faces": len(proofs), "dynamic_product_previews_maximum": 6, "client_tested": False}
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
