import copy
import functools
import hashlib
import io
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFilter, ImageFont, ImageOps

from build_room import ROOT, SIZE, load_structure, write_json


CLIENT = Path("C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar")
ASSETS = None
USED_MODELS = set()
USED_TEXTURES = set()
BACKGROUND = "#202220"
TEXT = "#ece6d5"
MUTED = "#aaa997"
FACE_STEPS = {"north": (0, 0, -1), "south": (0, 0, 1), "west": (-1, 0, 0), "east": (1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
PARTIAL = {"air", "chain", "lantern", "chest", "spruce_wall_sign", "vine", "hanging_roots", "cobweb", "water", "moss_carpet", "red_carpet", "light_weighted_pressure_plate"}


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
    definition = asset_json("blockstates/" + name + ".json")
    if "variants" in definition:
        for variant, choices in definition["variants"].items():
            condition = dict(part.split("=", 1) for part in variant.split(",") if part)
            if matches(condition, properties):
                return [choices[0] if isinstance(choices, list) else choices]
        raise ValueError(f"Unmatched block state: {name} {properties}")
    choices = []
    for part in definition.get("multipart", []):
        if "when" not in part or matches(part["when"], properties):
            candidate = part["apply"]
            choices.append(candidate[0] if isinstance(candidate, list) else candidate)
    return choices


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


def box(minimum, maximum, texture_id):
    return {"from": list(minimum), "to": list(maximum), "faces": {face: {"texture": texture_id} for face in FACE_STEPS}}


def synthetic_model(name, properties):
    if name == "water":
        return {"textures": {}, "elements": [box((0, 0, 0), (16, 14, 16), "block/water_still")]}, {}
    if name == "spruce_wall_sign":
        return {"textures": {}, "elements": [box((0, 4, 14), (16, 12, 15), "block/spruce_planks")]}, {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]}
    if name == "chest":
        base = box((1, 0, 1), (15, 10, 15), "entity/chest/normal")
        lid = box((1, 10, 1), (15, 14, 15), "entity/chest/normal")
        for element, coordinates in ((base, {"north": [14, 33, 28, 43], "west": [0, 33, 14, 43], "up": [14, 19, 28, 33]}), (lid, {"north": [14, 14, 28, 19], "west": [0, 14, 14, 19], "up": [14, 0, 28, 14]})):
            for face, bounds in coordinates.items():
                element["faces"][face]["uv"] = [value / 4 for value in bounds]
        latch = box((7, 7, 0), (9, 11, 2), "block/iron_block")
        return {"textures": {}, "elements": [base, lid, latch]}, {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]}
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
                if -normal[0] + normal[1] - normal[2] <= .001:
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
                brightness = (1.15 if normal[1] > 0 else .80 if normal[0] < 0 else .98) * (1.25 if name == "lantern" else 1)
                result.append((world_points, identifier, tuple(surface.get("uv", default_uv(element, face))), surface.get("rotation", 0), brightness))
    return result


@functools.lru_cache(maxsize=None)
def texture(identifier, uv, rotation, brightness):
    USED_TEXTURES.add(identifier)
    image = Image.open(io.BytesIO(ASSETS.read("assets/minecraft/textures/" + identifier + ".png"))).convert("RGBA")
    if identifier.startswith("block/"):
        image = image.crop((0, 0, image.width, min(image.width, image.height)))
    if "water_still" in identifier:
        image = ImageOps.colorize(image.convert("L"), "#24516b", "#538fa4").convert("RGBA")
        image.putalpha(210)
    elif identifier in {"block/vine", "block/vine_plant"}:
        alpha = image.getchannel("A")
        image = ImageChops.multiply(image, Image.new("RGBA", image.size, (104, 148, 57, 255)))
        image.putalpha(alpha)
    bounds = (uv[0] * image.width / 16, uv[1] * image.height / 16, uv[2] * image.width / 16, uv[3] * image.height / 16)
    left, top, right, bottom = bounds
    image = image.crop((min(left, right), min(top, bottom), max(left, right), max(top, bottom)))
    if right < left:
        image = image.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
    if bottom < top:
        image = image.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
    image = image.resize((48, 48), Image.Resampling.NEAREST).rotate(-rotation)
    return ImageEnhance.Brightness(image).enhance(brightness * 1.1)


def parallelogram(canvas, image, points):
    minimum_x = math.floor(min(point[0] for point in points))
    minimum_y = math.floor(min(point[1] for point in points))
    width = math.ceil(max(point[0] for point in points)) - minimum_x + 1
    height = math.ceil(max(point[1] for point in points)) - minimum_y + 1
    anchor_x, anchor_y = points[0][0] - minimum_x, points[0][1] - minimum_y
    first_x, first_y = ((points[1][axis] - points[0][axis]) / image.width for axis in range(2))
    second_x, second_y = ((points[3][axis] - points[0][axis]) / image.height for axis in range(2))
    determinant = first_x * second_y - second_x * first_y
    if abs(determinant) < .0001 or width <= 0 or height <= 0:
        return
    coefficients = (second_y / determinant, -second_x / determinant, (-second_y * anchor_x + second_x * anchor_y) / determinant, -first_y / determinant, first_x / determinant, (first_y * anchor_x - first_x * anchor_y) / determinant)
    warped = image.transform((width, height), Image.Transform.AFFINE, coefficients, Image.Resampling.NEAREST)
    canvas.alpha_composite(warped, (minimum_x, minimum_y))


def cutaway(blocks):
    visible = {}
    for position, state in blocks.items():
        block_x, block_y, block_z = position
        if block_y >= 7:
            continue
        if block_y >= 2 and (block_x == 0 or block_z == 0):
            if not (block_z == 0 and 3 <= block_x <= 7 and block_y <= 5) and not (block_x == 0 and block_z in (4, 9, 14)):
                continue
        if block_y == 6 and 0 < block_x < 10 and block_z in (4, 9):
            continue
        visible[position] = state
    return visible


def isometric(blocks, exterior=False):
    size = (1600, 1200)
    visible = blocks if exterior else cutaway(blocks)
    projected = [(position[0] - position[2], -(position[0] + position[2]) / 2 - position[1]) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 2, max(point[1] for point in projected) + 1
    scale = min((size[0] - 110) / (high_x - low_x), (size[1] - 200) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 65) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((50, 32), "S2  苔石储藏室 · " + ("完整封顶外观" if exterior else "商栈密道"), font=font(38), fill=TEXT)
    draw.text((52, 92), "11 × 9 × 15 格 / XYZ    ·    北向 3 × 3 敞口    ·    苔石 / 木梁 / 暖灯 / 浅水", font=font(22), fill=MUTED)

    def project(point):
        block_x, block_y, block_z = point
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale / 2 - block_y * scale

    faces = []
    for position, state in visible.items():
        faces.extend(block_faces(position, state, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        parallelogram(canvas, texture(identifier, uv, rotation, brightness), [project(point) for point in points])
    if not exterior:
        glow = Image.new("RGBA", size)
        glow_draw = ImageDraw.Draw(glow)
        for position, state in visible.items():
            if state["Name"] != "minecraft:lantern":
                continue
            center_x, center_y = project((position[0] + .5, position[1] + .45, position[2] + .5))
            radius = scale * .48
            glow_draw.ellipse((center_x - radius, center_y - radius, center_x + radius, center_y + radius), fill=(255, 161, 48, 68))
        canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(scale * .22)))
    draw = ImageDraw.Draw(canvas)
    caption = "最终 NBT 软件预览 · 非游戏截图" + (" · 屋顶完整保留" if exterior else " · 为观察室内剖去屋顶与部分近侧墙梁，成品封顶")
    draw.text((50, 1140), caption, font=font(23), fill=MUTED)
    return canvas.convert("RGB")


def plan_view(blocks):
    unit, margin_x, margin_y = 52, 80, 142
    image = Image.new("RGB", (SIZE[0] * unit + 160, SIZE[2] * unit + 250), BACKGROUND)
    draw = ImageDraw.Draw(image)
    draw.text((margin_x, 30), "S2 · 北接口与干燥通路", font=font(30), fill=TEXT)
    draw.text((margin_x, 80), "北 ↑   入口脚部 (5, 2, 0) / 内点 (5, 2, 7)", font=font(18), fill=MUTED)
    for block_x in range(11):
        for block_z in range(15):
            state = blocks[block_x, 1, block_z]
            material = state["Name"].removeprefix("minecraft:")
            image_id = "water_still" if material == "water" else material
            tile = texture("block/" + image_id, (0, 0, 16, 16), 0, 1).resize((unit, unit), Image.Resampling.NEAREST)
            image.paste(tile, (margin_x + block_x * unit, margin_y + block_z * unit))
            above = blocks.get((block_x, 2, block_z))
            if above and opaque(above):
                color = "#81603c" if above["Name"] == "minecraft:barrel" else "#575a4c"
                draw.rectangle((margin_x + block_x * unit + 3, margin_y + block_z * unit + 3, margin_x + (block_x + 1) * unit - 3, margin_y + (block_z + 1) * unit - 3), fill=color)
    route_x = margin_x + 5.5 * unit
    draw.line((route_x, margin_y + .5 * unit, route_x, margin_y + 11.5 * unit), fill="#f7ce7d", width=5)
    for block_z, text in ((.5, "入口"), (7.5, "内点")):
        center_y = margin_y + block_z * unit
        draw.ellipse((route_x - 12, center_y - 12, route_x + 12, center_y + 12), fill="#f7ce7d")
        draw.text((route_x + 22, center_y - 12), text, font=font(20), fill=TEXT, stroke_width=2, stroke_fill=BACKGROUND)
    draw.text((margin_x, image.height - 60), "中心三格宽干道 · 9 格蓄水均有底与四侧封挡 · 无关闭门", font=font(18), fill=MUTED)
    return image


def main():
    global ASSETS
    structure_path = ROOT / "out/room.nbt"
    template = load_structure(structure_path)
    blocks = {tuple(entry["pos"]): template["palette"][entry["state"]] for entry in template["blocks"] if template["palette"][entry["state"]]["Name"] != "minecraft:air"}
    with zipfile.ZipFile(CLIENT) as assets:
        ASSETS = assets
        (ROOT / "preview").mkdir(parents=True, exist_ok=True)
        isometric(blocks).save(ROOT / "out/preview.png")
        isometric(blocks, exterior=True).save(ROOT / "preview/exterior.png")
        plan_view(blocks).save(ROOT / "preview/plan.png")
    report = {"status": "PASS", "source": "Final compressed structure NBT; Minecraft 1.20.1 block models and textures", "structure_sha256": hashlib.sha256(structure_path.read_bytes()).hexdigest(), "preview_sha256": hashlib.sha256((ROOT / "out/preview.png").read_bytes()).hexdigest(), "preview_size": [1600, 1200], "models": sorted(USED_MODELS), "textures": sorted(USED_TEXTURES), "cutaway_only": True, "game_screenshot": False, "paid_image_requests": 0}
    write_json(ROOT / "qa/render.json", report)
    print(json.dumps({"preview": str(ROOT / "out/preview.png"), "models": len(USED_MODELS), "textures": len(USED_TEXTURES)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
