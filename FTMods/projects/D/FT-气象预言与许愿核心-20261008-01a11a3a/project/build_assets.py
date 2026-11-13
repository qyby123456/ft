import json
import math
import random
from pathlib import Path

from PIL import Image, ImageDraw
from optimize_geometry import optimize_elements


ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / "payload/assets/qys_event"
MATERIALS = ["stone", "wood", "copper", "brass", "patina", "ivory", "dark", "screen", "dial", "vent", "switch", "crystal", "inlay", "rune", "eye", "endgrain"]
PALETTES = {
    "weather_forecaster": [(58, 67, 67), (104, 65, 39), (170, 100, 55), (211, 168, 79), (53, 116, 104), (214, 210, 174), (35, 51, 52), (21, 63, 72), (224, 215, 168), (58, 87, 80), (38, 103, 89), (124, 182, 190), (40, 75, 77), (105, 169, 154), (130, 175, 169), (120, 78, 47)],
    "oracle": [(45, 39, 59), (78, 43, 54), (137, 88, 68), (193, 155, 84), (80, 68, 113), (196, 180, 172), (26, 24, 43), (75, 45, 111), (193, 160, 112), (62, 42, 87), (106, 71, 150), (139, 107, 214), (39, 32, 65), (111, 81, 154), (61, 41, 100), (91, 53, 62)],
}


def tile_origin(material):
    index = MATERIALS.index(material)
    return (index % 4) * 32, (index // 4) * 32


def make_atlas(name, namespace="qys_event"):
    image = Image.new("RGBA", (128, 128), (0, 0, 0, 255))
    randomizer = random.Random(20261008)
    for material, base in zip(MATERIALS, PALETTES[name]):
        left, top = tile_origin(material)
        for row in range(32):
            for column in range(32):
                noise = randomizer.choice([-5, -2, 0, 0, 2, 4])
                if material in {"wood", "endgrain"}:
                    noise += [-7, -3, 2, 0, 3, 0, -2, 1][column % 8]
                if material in {"brass", "copper", "ivory"}:
                    noise += round(8 * (1 - row / 31))
                image.putpixel((left + column, top + row), tuple(max(0, min(255, channel + noise)) for channel in base) + (255,))
    for material in MATERIALS:
        left, top = tile_origin(material)
        tile = image.crop((left, top, left + 32, top + 32))
        draw = ImageDraw.Draw(tile)
        if material == "stone":
            for row in (8, 23):
                draw.line([(0, row), (31, row)], fill=(31, 33, 40))
            draw.line([(11, 0), (11, 8)], fill=(31, 33, 40))
            draw.line([(23, 8), (23, 23)], fill=(31, 33, 40))
        elif material in {"copper", "brass", "patina"}:
            base = PALETTES[name][MATERIALS.index(material)]
            highlight = tuple(min(255, value + 30) for value in base)
            shadow = tuple(max(0, value - 35) for value in base)
            draw.line([(1, 1), (30, 1), (30, 30)], fill=highlight)
            draw.line([(1, 1), (1, 30), (30, 30)], fill=shadow)
            for column, row in ((4, 4), (27, 4), (4, 27), (27, 27)):
                draw.rectangle((column - 1, row - 1, column + 1, row + 1), fill=shadow)
                draw.point((column, row - 1), fill=highlight)
        elif material == "screen":
            draw.rectangle((0, 0, 31, 31), fill=(14, 40, 49))
            for row in (5, 10, 15, 20, 25):
                draw.line([(2, row), (29, row)], fill=(25, 63, 67))
            draw.rectangle((3, 3, 11, 11), fill=(235, 191, 77))
            draw.rectangle((1, 6, 13, 8), fill=(247, 211, 101))
            draw.rectangle((6, 1, 8, 13), fill=(247, 211, 101))
            draw.rectangle((16, 7, 24, 12), fill=(192, 223, 210))
            draw.rectangle((13, 10, 29, 15), fill=(213, 233, 218))
            for column in (17, 23, 28):
                draw.line([(column, 18), (column - 2, 22)], fill=(100, 183, 204), width=2)
            for column, height in ((3, 3), (7, 6), (11, 4), (15, 8), (19, 5), (23, 3), (27, 6)):
                draw.rectangle((column, 30 - height, column + 1, 30), fill=(110, 179, 146))
        elif material == "dial":
            draw.rectangle((0, 0, 31, 31), fill=(36, 56, 55))
            draw.rectangle((5, 1, 26, 30), fill=(183, 144, 68))
            draw.rectangle((1, 5, 30, 26), fill=(183, 144, 68))
            draw.rectangle((5, 4, 26, 27), fill=(226, 221, 181))
            draw.rectangle((4, 5, 27, 26), fill=(226, 221, 181))
            for angle in range(0, 360, 30):
                radians = math.radians(angle)
                start = (16 + round(10 * math.sin(radians)), 16 - round(10 * math.cos(radians)))
                end = (16 + round(12 * math.sin(radians)), 16 - round(12 * math.cos(radians)))
                draw.line([start, end], fill=(72, 79, 62))
            draw.line([(16, 17), (22, 8)], fill=(176, 72, 48), width=2)
            draw.rectangle((14, 14, 17, 17), fill=(67, 76, 61))
        elif material == "vent":
            for row in range(4, 30, 5):
                draw.rectangle((4, row, 27, row + 1), fill=(22, 43, 44))
                draw.line([(4, row + 2), (27, row + 2)], fill=(114, 135, 113))
        elif material == "switch":
            draw.rectangle((0, 0, 31, 31), fill=(31, 49, 46))
            for column, color in ((3, (225, 173, 69)), (13, (137, 203, 146)), (23, (164, 198, 206))):
                draw.rectangle((column, 6, column + 5, 13), fill=color)
                draw.rectangle((column, 20, column + 5, 25), fill=(117, 131, 102))
        elif material == "crystal":
            draw.rectangle((0, 0, 31, 31), fill=(132, 100, 201))
            draw.polygon([(0, 0), (12, 0), (5, 31), (0, 31)], fill=(99, 76, 158))
            draw.polygon([(12, 0), (26, 0), (17, 31), (5, 31)], fill=(167, 133, 228))
            draw.polygon([(26, 0), (31, 0), (31, 31), (17, 31)], fill=(119, 91, 182))
            draw.line([(12, 0), (5, 31)], fill=(198, 167, 245), width=2)
            draw.rectangle((18, 5, 24, 7), fill=(228, 215, 255))
            draw.rectangle((22, 2, 24, 10), fill=(228, 215, 255))
            draw.rectangle((8, 24, 11, 25), fill=(207, 183, 245))
        elif material == "inlay":
            draw.rectangle((0, 0, 31, 31), fill=(33, 30, 55) if name == "oracle" else (35, 68, 68))
            draw.rectangle((1, 1, 30, 30), outline=(161, 132, 79))
            draw.rectangle((4, 4, 27, 27), outline=(99, 82, 113))
            draw.line([(16, 5), (16, 26)], fill=(196, 163, 102))
            draw.line([(5, 16), (26, 16)], fill=(196, 163, 102))
            draw.polygon([(16, 5), (20, 16), (16, 26), (12, 16)], outline=(212, 182, 123))
            draw.polygon([(5, 16), (16, 12), (26, 16), (16, 20)], outline=(212, 182, 123))
            for column, row in ((7, 7), (24, 7), (7, 24), (24, 24)):
                draw.rectangle((column, row, column + 1, row + 1), fill=(172, 153, 204))
        elif material == "rune":
            draw.rectangle((0, 0, 31, 31), fill=(65, 44, 93))
            draw.line([(1, 1), (30, 1), (30, 30)], fill=(167, 132, 76))
            draw.line([(1, 1), (1, 30), (30, 30)], fill=(115, 83, 64))
            for column in (5, 13, 21):
                draw.line([(column, 8), (column + 4, 12), (column, 17), (column + 4, 22)], fill=(197, 163, 227), width=2)
                draw.line([(column + 2, 6), (column + 2, 25)], fill=(197, 163, 227))
        elif material == "eye":
            draw.rectangle((0, 0, 31, 31), fill=(34, 25, 60))
            draw.polygon([(3, 16), (10, 9), (21, 9), (28, 16), (21, 23), (10, 23)], fill=(191, 157, 88))
            draw.polygon([(6, 16), (11, 12), (20, 12), (25, 16), (20, 20), (11, 20)], fill=(141, 107, 199))
            draw.rectangle((14, 11, 17, 21), fill=(43, 27, 77))
            draw.rectangle((17, 12, 18, 14), fill=(236, 220, 253))
            draw.line([(16, 2), (16, 6)], fill=(203, 171, 107))
            draw.line([(16, 26), (16, 29)], fill=(203, 171, 107))
        elif material == "endgrain":
            for inset in (3, 7, 11):
                draw.rectangle((inset, inset, 31 - inset, 31 - inset), outline=(76, 47, 38))
        image.paste(tile, (left, top))
    output = ROOT / "payload/assets" / namespace / "textures/block" / f"{name}_atlas.png"
    output.parent.mkdir(parents=True, exist_ok=True)
    image.save(output)
    left, top = tile_origin("patina" if name == "weather_forecaster" else "stone")
    image.crop((left, top, left + 32, top + 32)).save(output.with_name(f"{name}_particle.png"))


def uv(material):
    left, top = tile_origin(material)
    return [left / 8, top / 8, (left + 32) / 8, (top + 32) / 8]


class Model:
    def __init__(self, name, namespace="qys_event"):
        self.name = name
        self.namespace = namespace
        self.assets = ROOT / "payload/assets" / namespace
        self.elements = []

    def box(self, name, minimum, maximum, material, overrides=None, rotation=None):
        faces = {direction: {"uv": uv((overrides or {}).get(direction, material)), "texture": "#atlas"}
                 for direction in ("down", "up", "north", "south", "west", "east")}
        element = {"name": name, "from": list(minimum), "to": list(maximum), "faces": faces}
        if rotation:
            element["rotation"] = {"origin": list(rotation[0]), "axis": rotation[1], "angle": rotation[2], "rescale": False}
        self.elements.append(element)

    def save(self):
        display = {
            "gui": {"rotation": [30, 225, 0], "translation": [0, 0, 0], "scale": [0.72] * 3},
            "ground": {"rotation": [0, 0, 0], "translation": [0, 3, 0], "scale": [0.25] * 3},
            "fixed": {"rotation": [0, 180, 0], "translation": [0, 0, 0], "scale": [0.55] * 3},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.375] * 3},
            "thirdperson_lefthand": {"rotation": [75, 225, 0], "translation": [0, 2.5, 0], "scale": [0.375] * 3},
            "firstperson_righthand": {"rotation": [0, 45, 0], "translation": [0, 0, 0], "scale": [0.4] * 3},
            "firstperson_lefthand": {"rotation": [0, 225, 0], "translation": [0, 0, 0], "scale": [0.4] * 3},
            "head": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1] * 3},
        }
        model = {"parent": "minecraft:block/block", "ambientocclusion": True,
                 "textures": {"atlas": f"{self.namespace}:block/{self.name}_atlas", "particle": f"{self.namespace}:block/{self.name}_particle"},
                 "display": display, "elements": self.elements}
        model["elements"], optimization = optimize_elements(self.elements)
        report_path = ROOT / "optimization" / f"{self.name}.json"
        report_path.parent.mkdir(parents=True, exist_ok=True)
        report_path.write_text(json.dumps(optimization, indent=2) + "\n", encoding="utf-8")
        target = self.assets / "models/block" / f"{self.name}.json"
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_text(json.dumps(model, indent=2) + "\n", encoding="utf-8")
        item = self.assets / "models/item" / f"{self.name}.json"
        item.parent.mkdir(parents=True, exist_ok=True)
        item.write_text(json.dumps({"parent": f"{self.namespace}:block/{self.name}"}, indent=2) + "\n", encoding="utf-8")
        return model


def weather_forecaster():
    model = Model("weather_forecaster")
    box = model.box
    box("stone foundation", (0, 0, 0), (16, 1.3, 16), "stone")
    box("copper plinth", (0.5, 1.3, 0.5), (15.5, 2, 15.5), "copper")
    box("lower timber cabinet", (2, 2, 2), (14, 9.5, 14), "wood", {"up": "endgrain", "down": "endgrain"})
    box("patinated sill", (1.5, 2.1, 1.5), (14.5, 2.7, 14.5), "patina")
    for column in (1.85, 13):
        for depth in (1.85, 13):
            box(f"brass corner {column} {depth}", (column, 2.7, depth), (column + 1.15, 9.5, depth + 1.15), "brass")
            box(f"corner collar {column} {depth}", (column - 0.15, 8.65, depth - 0.15), (column + 1.15, 9.35, depth + 1.15), "copper")
    box("instrument front plate", (3, 4.2, 1.6), (13, 8.9, 2), "patina")
    box("forecast display bezel", (3.35, 5.05, 1.25), (8.8, 8.5, 1.6), "brass")
    box("weather pictogram display", (3.65, 5.35, 1.17), (8.5, 8.2, 1.25), "dark", {"north": "screen"})
    box("barometer bezel", (9.3, 5.15, 1.25), (12.65, 8.5, 1.6), "brass")
    box("barometer face", (9.55, 5.4, 1.17), (12.4, 8.25, 1.25), "dark", {"north": "dial"})
    box("control fascia", (3.5, 3, 1.5), (9.7, 4.35, 1.9), "dark", {"north": "switch"})
    box("emerald payment slot frame", (10.3, 3.05, 1.45), (12.65, 4.3, 1.9), "brass")
    box("emerald payment slot", (10.7, 3.48, 1.37), (12.25, 3.8, 1.45), "dark")
    for depth in (4, 10):
        box(f"left cabinet air vent {depth}", (1.8, 4.5, depth), (2, 8.5, depth + 2.5), "patina", {"west": "vent"})
        box(f"right cabinet air vent {depth}", (14, 4.5, depth), (14.2, 8.5, depth + 2.5), "patina", {"east": "vent"})
    box("rear inspection hatch", (4, 3.4, 14), (12, 8.4, 14.2), "copper", {"south": "vent"})
    box("rear inspection latch", (7.6, 5.2, 14.2), (8.4, 6.7, 14.5), "brass")
    box("overhanging copper instrument deck", (1.1, 9.5, 1.1), (14.9, 10.25, 14.9), "copper")
    box("deck oxidized inlay", (2, 10.25, 2), (14, 10.5, 14), "patina")
    box("rain collector pedestal", (3.6, 10.5, 3.4), (6.2, 11, 6), "brass")
    box("rain collector ceramic tube", (4, 11, 3.8), (5.8, 13.6, 5.6), "ivory")
    box("rain collector funnel rim", (3.2, 13.6, 3), (6.6, 14.05, 6.4), "copper")
    box("rain collector recessed opening", (3.65, 14.05, 3.45), (6.15, 14.12, 5.95), "dark")
    box("weather sensor pedestal", (3.4, 10.5, 8.4), (7.6, 11, 12.6), "brass")
    box("weather sensor dark core", (4.3, 11, 9.3), (6.7, 14.15, 11.7), "dark")
    for index in range(5):
        height = 11.15 + index * 0.65
        box(f"weather shield louver {index + 1}", (3.4, height, 8.4), (7.6, height + 0.3, 12.6), "ivory")
    box("weather shield copper roof", (3.1, 14.1, 8.1), (7.9, 14.55, 12.9), "copper")
    box("anemometer mast foot", (10.2, 10.5, 10.2), (12.6, 11, 12.6), "brass")
    box("anemometer mast", (11.05, 11, 11.05), (11.75, 14.65, 11.75), "brass")
    box("wind direction collar", (10.65, 12.45, 10.65), (12.15, 13.05, 12.15), "patina")
    box("anemometer cross east west", (8.6, 14.45, 11.18), (14.2, 14.8, 11.62), "brass")
    box("anemometer cross north south", (11.18, 14.45, 8.6), (11.62, 14.8, 14.2), "brass")
    for index, (column, depth) in enumerate(((8.6, 11.4), (14.2, 11.4), (11.4, 8.6), (11.4, 14.2))):
        box(f"anemometer cup {index + 1}", (column - 0.65, 14.4, depth - 0.65), (column + 0.65, 15.4, depth + 0.65), "copper")
        box(f"anemometer cup inset {index + 1}", (column - 0.4, 15.4, depth - 0.4), (column + 0.4, 15.47, depth + 0.4), "dark")
    box("mast cap", (11, 14.8, 11), (11.8, 15.65, 11.8), "patina")
    box("deck calibration plaque", (8.5, 10.5, 3), (12.5, 10.65, 6.4), "dark", {"up": "dial"})
    return model.save()


def oracle():
    model = Model("oracle")
    box = model.box
    box("complete basalt foundation", (0, 0, 0), (16, 1.4, 16), "stone")
    box("lower antique gold rim", (0.55, 1.4, 0.55), (15.45, 2, 15.45), "brass")
    box("purple stone step", (1.1, 2, 1.1), (14.9, 2.7, 14.9), "patina")
    for column in (2, 11.5):
        for depth in (2, 11.5):
            box(f"carved walnut foot {column} {depth}", (column, 2.7, depth), (column + 2.5, 6.4, depth + 2.5), "wood")
            box(f"gilded foot band {column} {depth}", (column - 0.15, 3.25, depth - 0.15), (column + 2.65, 3.75, depth + 2.65), "brass")
    box("altar front apron", (4.5, 4.8, 2), (11.5, 6.4, 3), "wood")
    box("altar rear apron", (4.5, 4.8, 13), (11.5, 6.4, 14), "wood")
    box("altar left apron", (2, 4.8, 4.5), (3, 6.4, 11.5), "wood")
    box("altar right apron", (13, 4.8, 4.5), (14, 6.4, 11.5), "wood")
    box("lower shelf", (3, 3.8, 3), (13, 4.35, 13), "dark", {"up": "inlay"})
    box("altar tabletop gold molding", (1, 6.4, 1), (15, 7.15, 15), "brass")
    box("altar tabletop dark celestial inlay", (1.45, 7.15, 1.45), (14.55, 7.65, 14.55), "dark", {"up": "inlay"})
    box("front prophecy plaque frame", (5.7, 4.5, 1.65), (10.3, 6.3, 2), "brass")
    box("front prophecy plaque", (5.95, 4.7, 1.58), (10.05, 6.1, 1.65), "dark", {"north": "rune"})
    box("left amethyst inset", (1.78, 5, 6.2), (2, 6.15, 9.8), "dark", {"west": "rune"})
    box("right amethyst inset", (14, 5, 6.2), (14.22, 6.15, 9.8), "dark", {"east": "rune"})
    box("crystal cradle foot", (4.9, 7.65, 4.4), (11.1, 8.05, 10.6), "brass")
    box("crystal cradle lapis step", (5.4, 8.05, 4.9), (10.6, 8.45, 10.1), "patina")
    box("crystal cradle upper rim", (5.85, 8.45, 5.35), (10.15, 8.8, 9.65), "brass")
    box("crystal cradle recess", (6.25, 8.8, 5.75), (9.75, 8.87, 9.25), "dark")
    for index, (column, depth) in enumerate(((5.6, 7.5), (10.4, 7.5), (8, 5.1), (8, 9.9))):
        box(f"crystal retaining claw {index + 1}", (column - 0.28, 8.35, depth - 0.28), (column + 0.28, 10.1, depth + 0.28), "brass")
    layers = [(9.0, 9.55, 1.5), (9.55, 10.2, 2.1), (10.2, 11.1, 2.65), (11.1, 12.2, 2.9), (12.2, 13.05, 2.65), (13.05, 13.7, 2.1), (13.7, 14.15, 1.35)]
    for index, (bottom, top, radius) in enumerate(layers):
        cut = radius * 0.33
        inner = radius - cut
        box(f"amethyst orb layer {index + 1} core", (8 - radius, bottom, 7.5 - inner), (8 + radius, top, 7.5 + inner), "crystal")
        box(f"amethyst orb layer {index + 1} front", (8 - inner, bottom, 7.5 - radius), (8 + inner, top, 7.5 - inner), "crystal")
        box(f"amethyst orb layer {index + 1} back", (8 - inner, bottom, 7.5 + inner), (8 + inner, top, 7.5 + radius), "crystal")
        half_side = cut / math.sqrt(2)
        for column_sign in (-1, 1):
            for depth_sign in (-1, 1):
                column, depth = 8 + column_sign * inner, 7.5 + depth_sign * inner
                box(f"amethyst orb layer {index + 1} facet {column_sign} {depth_sign}",
                    (column - half_side, bottom + 0.002, depth - half_side),
                    (column + half_side, top - 0.002, depth + half_side), "crystal",
                    rotation=((column, bottom, depth), "y", 45))
        for element in model.elements[-7:]:
            for direction in ("north", "south", "west", "east"):
                left, tile_top, right, tile_bottom = uv("crystal")
                element["faces"][direction]["uv"] = [left, tile_top + (14.15 - top) / 5.15 * 4, right, tile_top + (14.15 - bottom) / 5.15 * 4]
    for column in (2.2, 12.5):
        box(f"rear shrine pillar {column}", (column, 7.65, 11.2), (column + 1.3, 12.4, 12.5), "wood")
        box(f"rear shrine gilded pillar {column}", (column + 0.35, 7.9, 10.98), (column + 0.95, 12.2, 11.2), "brass")
        box(f"rear shrine pillar cap {column}", (column - 0.2, 12.2, 11), (column + 1.5, 12.7, 12.7), "brass")
    box("left celestial arch slope", (3.1, 12.3, 11.2), (4.1, 15.2, 12.4), "brass", rotation=((3.6, 12.3, 11.8), "z", -45))
    box("right celestial arch slope", (11.9, 12.3, 11.2), (12.9, 15.2, 12.4), "brass", rotation=((12.4, 12.3, 11.8), "z", 45))
    box("left celestial arch lintel", (5.6, 14.2, 11.2), (7.3, 15.1, 12.4), "brass")
    box("right celestial arch lintel", (8.7, 14.2, 11.2), (10.4, 15.1, 12.4), "brass")
    box("all seeing crest", (7.1, 13.75, 10.98), (8.9, 15.55, 12.55), "brass", {"north": "eye", "south": "eye"})
    box("left table incense base", (2.5, 7.65, 3), (4.1, 8, 4.6), "brass")
    box("right table incense base", (11.9, 7.65, 3), (13.5, 8, 4.6), "brass")
    for column in (2.95, 12.35):
        box(f"ceremonial candle {column}", (column, 8, 3.45), (column + 0.7, 9.35, 4.15), "ivory")
        box(f"ceremonial candle wick {column}", (column + 0.25, 9.35, 3.7), (column + 0.45, 9.6, 3.9), "dark")
    box("offering tablet frame", (6, 7.65, 1.95), (10, 7.85, 3.55), "brass")
    box("offering tablet inscriptions", (6.2, 7.85, 2.15), (9.8, 7.9, 3.35), "dark", {"up": "rune"})
    return model.save()


def main():
    manifest = {}
    for name, builder in (("weather_forecaster", weather_forecaster), ("oracle", oracle)):
        make_atlas(name)
        model = builder()
        manifest[name] = {"elements": len(model["elements"]), "faces": sum(len(element["faces"]) for element in model["elements"])}
    for kind in ("block", "item"):
        legacy = ROOT / "payload/assets/ftboss/models" / kind / "oracle.json"
        legacy.parent.mkdir(parents=True, exist_ok=True)
        legacy.write_text(json.dumps({"parent": "qys_event:block/oracle"}, indent=2) + "\n", encoding="utf-8")
    (ROOT / "build-report.json").write_text(json.dumps(manifest, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(manifest, indent=2))


if __name__ == "__main__":
    main()
