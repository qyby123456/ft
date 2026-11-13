import argparse
import copy
import functools
import io
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont

ROOT = Path(__file__).resolve().parents[2]


CLIENT = Path("C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar")
ASSETS = None
PREVIEW = ROOT / "preview"
BACKGROUND = "#101a1e"
PANEL = "#19262a"
TEXT = "#e4e6db"
MUTED = "#9fb2b2"
ACCENT = "#79dfd1"
GOLD = "#d6bd84"
PARTIAL = {"skeleton_skull", "black_wall_banner", "lectern", "iron_bars", "iron_door", "candle", "soul_lantern", "chain", "stone_pressure_plate", "birch_pressure_plate", "brown_carpet", "cobweb"}
FACE_STEPS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
USED_MODELS = set()
USED_TEXTURES = set()


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


@functools.lru_cache(maxsize=None)
def asset_json(path):
    return json.loads(ASSETS.read("assets/minecraft/" + path))


@functools.lru_cache(maxsize=None)
def model(identifier):
    identifier = identifier.removeprefix("minecraft:")
    raw = asset_json("models/" + identifier + ".json")
    result = {"textures": {}}
    if raw.get("parent") and not raw["parent"].startswith("builtin/"):
        result = copy.deepcopy(model(raw["parent"]))
    result["textures"].update(raw.get("textures", {}))
    if "elements" in raw:
        result["elements"] = raw["elements"]
    USED_MODELS.add(identifier)
    return result


def matches(condition, properties):
    if "OR" in condition:
        return any(matches(part, properties) for part in condition["OR"])
    if "AND" in condition:
        return all(matches(part, properties) for part in condition["AND"])
    return all(properties.get(key) in str(value).split("|") for key, value in condition.items())


def selected_models(name, properties):
    state = asset_json("blockstates/" + name + ".json")
    if "variants" in state:
        for variant, choices in state["variants"].items():
            condition = dict(item.split("=", 1) for item in variant.split(",") if item)
            if matches(condition, properties):
                return [choices[0] if isinstance(choices, list) else choices]
        raise ValueError(f"Unmatched block state: {name} {properties}")
    result = []
    for part in state.get("multipart", []):
        if "when" not in part or matches(part["when"], properties):
            choices = part["apply"]
            result.append(choices[0] if isinstance(choices, list) else choices)
    return result


def vertices(element, face):
    left, bottom, front = element["from"]
    right, top, back = element["to"]
    return {
        "north": [(left, top, front), (right, top, front), (right, bottom, front), (left, bottom, front)],
        "west": [(left, top, back), (left, top, front), (left, bottom, front), (left, bottom, back)],
        "up": [(left, top, back), (right, top, back), (right, top, front), (left, top, front)],
        "south": [(right, top, back), (left, top, back), (left, bottom, back), (right, bottom, back)],
        "east": [(right, top, front), (right, top, back), (right, bottom, back), (right, bottom, front)],
        "down": [(left, bottom, front), (right, bottom, front), (right, bottom, back), (left, bottom, back)],
    }[face]


def rotate(position, axis, angle, origin=(8, 8, 8)):
    if not angle:
        return position
    centered = [position[index] - origin[index] for index in range(3)]
    first, second = {"x": (1, 2), "y": (0, 2), "z": (0, 1)}[axis]
    cosine, sine = math.cos(math.radians(angle)), math.sin(math.radians(angle))
    centered[first], centered[second] = centered[first] * cosine - centered[second] * sine, centered[first] * sine + centered[second] * cosine
    return tuple(centered[index] + origin[index] for index in range(3))


def default_uv(element, face):
    left, bottom, front = element["from"]
    right, top, back = element["to"]
    return {"north": [16 - right, 16 - top, 16 - left, 16 - bottom], "south": [left, 16 - top, right, 16 - bottom], "west": [front, 16 - top, back, 16 - bottom], "east": [16 - back, 16 - top, 16 - front, 16 - bottom], "up": [left, front, right, back], "down": [left, 16 - back, right, 16 - front]}[face]


def synthetic_model(name, properties):
    if name == "skeleton_skull":
        texture = "entity/skeleton/skeleton"
        faces = {"north": {"texture": texture, "uv": [2, 4, 4, 8]}, "south": {"texture": texture, "uv": [6, 4, 8, 8]}, "west": {"texture": texture, "uv": [0, 4, 2, 8]}, "east": {"texture": texture, "uv": [4, 4, 6, 8]}, "up": {"texture": texture, "uv": [2, 0, 4, 4]}}
        return {"textures": {}, "elements": [{"from": [4, 0, 4], "to": [12, 8, 12], "faces": faces}]}, {"y": int(properties["rotation"]) * 22.5}
    if name == "black_wall_banner":
        faces = {face: {"texture": "block/black_wool", "uv": [0, 0, 16, 16]} for face in FACE_STEPS}
        return {"textures": {}, "elements": [{"from": [2, -12, 14], "to": [14, 15, 15], "faces": faces}]}, {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]}
    return None


def opaque(state):
    if state is None:
        return False
    name = state["Name"].removeprefix("minecraft:")
    return name not in PARTIAL and not name.endswith(("_slab", "_stairs"))


def block_faces(position, state, visible):
    name = state["Name"].removeprefix("minecraft:")
    properties = state.get("Properties", {})
    synthetic = synthetic_model(name, properties)
    choices = [synthetic[1]] if synthetic else selected_models(name, properties)
    result = []
    for choice in choices:
        definition = synthetic[0] if synthetic else model(choice["model"])
        for element in definition.get("elements", []):
            for face, surface in element["faces"].items():
                points = vertices(element, face)
                if "rotation" in element:
                    rotation = element["rotation"]
                    points = [rotate(point, rotation["axis"], rotation["angle"], rotation["origin"]) for point in points]
                points = [rotate(rotate(point, "x", -choice.get("x", 0)), "y", choice.get("y", 0)) for point in points]
                first = [points[1][axis] - points[0][axis] for axis in range(3)]
                second = [points[2][axis] - points[1][axis] for axis in range(3)]
                normal = (first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0])
                length = math.sqrt(sum(value * value for value in normal))
                if length < 0.001 or -normal[0] + normal[1] - normal[2] <= 0.001:
                    continue
                if "cullface" in surface and not choice.get("x") and not choice.get("y"):
                    delta = FACE_STEPS[surface["cullface"]]
                    neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
                    if opaque(visible.get(neighbor)):
                        continue
                identifier = surface["texture"]
                while identifier.startswith("#"):
                    identifier = definition["textures"][identifier[1:]]
                identifier = identifier.removeprefix("minecraft:")
                world_points = [tuple(point[axis] / 16 + position[axis] for axis in range(3)) for point in points]
                brightness = (1.22 if normal[1] > 0 else 0.87 if normal[0] < 0 else 1.02) * (1.18 if name in ("soul_lantern", "candle") else 1)
                result.append((world_points, identifier, tuple(surface.get("uv", default_uv(element, face))), surface.get("rotation", 0), brightness))
    if name == "lectern" and properties.get("has_book") == "true":
        for page in (0, 1):
            left = 2 + page * 6
            element = {"from": [left, 17, 4], "to": [left + 6, 17.5, 13]}
            points = [rotate(point, "y", {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]) for point in vertices(element, "up")]
            points = [tuple(point[axis] / 16 + position[axis] for axis in range(3)) for point in points]
            result.append((points, "preview/book_page", (0, 0, 16, 16), 0, 1))
    return result


@functools.lru_cache(maxsize=None)
def texture(identifier, uv, rotation, brightness):
    USED_TEXTURES.add(identifier)
    if identifier == "preview/book_page":
        image = Image.new("RGBA", (16, 16), "#ddcf9b")
        draw = ImageDraw.Draw(image)
        for line_y in (3, 5, 7, 9, 11, 13):
            draw.line((2, line_y, 12 - line_y % 3, line_y), fill="#83774e")
    else:
        image = Image.open(io.BytesIO(ASSETS.read("assets/minecraft/textures/" + identifier + ".png"))).convert("RGBA")
        image = image.crop((0, 0, image.width, min(image.width, image.height))) if identifier.startswith("block/") else image
        bounds = (uv[0] * image.width / 16, uv[1] * image.height / 16, uv[2] * image.width / 16, uv[3] * image.height / 16)
        left, top, right, bottom = bounds
        image = image.crop((min(left, right), min(top, bottom), max(left, right), max(top, bottom)))
        if right < left:
            image = image.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        if bottom < top:
            image = image.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    image = image.resize((32, 32), Image.Resampling.NEAREST).rotate(-rotation)
    return ImageEnhance.Brightness(image).enhance(brightness * 1.2)


def parallelogram(canvas, image, points):
    minimum_x = math.floor(min(point[0] for point in points))
    minimum_y = math.floor(min(point[1] for point in points))
    width = math.ceil(max(point[0] for point in points)) - minimum_x + 1
    height = math.ceil(max(point[1] for point in points)) - minimum_y + 1
    anchor_x, anchor_y = points[0][0] - minimum_x, points[0][1] - minimum_y
    first_x, first_y = ((points[1][axis] - points[0][axis]) / image.width for axis in range(2))
    second_x, second_y = ((points[3][axis] - points[0][axis]) / image.height for axis in range(2))
    determinant = first_x * second_y - second_x * first_y
    if abs(determinant) < 0.0001 or width <= 0 or height <= 0:
        return
    coefficients = (second_y / determinant, -second_x / determinant, (-second_y * anchor_x + second_x * anchor_y) / determinant, -first_y / determinant, first_x / determinant, (first_y * anchor_x - first_x * anchor_y) / determinant)
    warped = image.transform((width, height), Image.Transform.AFFINE, coefficients, Image.Resampling.NEAREST)
    canvas.alpha_composite(warped, (minimum_x, minimum_y))
