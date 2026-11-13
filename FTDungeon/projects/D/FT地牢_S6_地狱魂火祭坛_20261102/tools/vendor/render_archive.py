import argparse
import copy
import functools
import io
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFilter, ImageFont

from build_s6 import ROOT, SIZE, dump as write_json
from nbtio import plain, read_nbt


def load_structure(path):
    return plain(read_nbt(path)[1])


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


def cutaway(blocks):
    visible = {}
    for position, state in blocks.items():
        block_x, block_y, block_z = position
        name = state["Name"].removeprefix("minecraft:")
        if name == "deepslate_tiles" and block_y >= 7:
            continue
        if block_y >= 5 and (block_x <= 1 or block_z <= 9 and (block_z >= 8 or block_x <= 20)):
            continue
        if block_y >= 7 and block_z < 8:
            continue
        if block_x == 3 and block_y >= 4 and name in ("bookshelf", "dark_oak_log", "dark_oak_slab"):
            continue
        visible[position] = state
    return visible


def isometric(blocks, size=(2100, 1500), exterior=False, detail=False):
    visible = blocks if exterior else cutaway(blocks)
    if detail:
        visible = {position: state for position, state in visible.items() if 8 <= position[0] <= 22 and position[2] >= 28}
        visible = {position: state for position, state in visible.items() if not (position[1] >= 5 and (position[0] <= 8 or position[2] <= 30) and state["Name"] not in ("minecraft:iron_bars", "minecraft:iron_door"))}
    projected = [(position[0] - position[2], -(position[0] + position[2]) / 2 - position[1]) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1.8, max(point[1] for point in projected) + 1
    scale = min((size[0] - 100) / (high_x - low_x), (size[1] - 170) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 50) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((48, 25), "墓狱档案 / " + ("完整建筑" if exterior else "后部密库细节" if detail else "轴测剖面"), font=font(30), fill=TEXT)

    def project(point):
        block_x, block_y, block_z = point
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale / 2 - block_y * scale

    faces = []
    for position, state in visible.items():
        faces.extend(block_faces(position, state, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        parallelogram(canvas, texture(identifier, uv, rotation, brightness), [project(point) for point in points])
    glow = Image.new("RGBA", size)
    glow_draw = ImageDraw.Draw(glow)
    for position, state in visible.items():
        name = state["Name"].removeprefix("minecraft:")
        if name not in ("soul_lantern", "candle") or exterior:
            continue
        center_x, center_y = project((position[0] + 0.5, position[1] + 0.3, position[2] + 0.5))
        radius = scale * (0.45 if name == "soul_lantern" else 0.22)
        color = (65, 241, 229, 78) if name == "soul_lantern" else (255, 182, 60, 85)
        glow_draw.ellipse((center_x - radius, center_y - radius, center_x + radius, center_y + radius), fill=color)
    canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(scale * 0.18)))
    draw = ImageDraw.Draw(canvas)
    draw.text((48, size[1] - 48), "软件结构预览 · 从最终 NBT 读取 · 非游戏截图" + ("" if exterior else " · 已剖去屋顶和近侧遮挡墙架"), font=font(21), fill=MUTED)
    return canvas.convert("RGB")


def topdown(blocks):
    scale, margin_x, margin_y = 26, 78, 130
    size = (SIZE[0] * scale + margin_x * 2 + 240, SIZE[2] * scale + margin_y + 100)
    canvas = Image.new("RGB", size, BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((48, 30), "墓狱档案 / 平面布局", font=font(31), fill=TEXT)
    draw.text((48, 77), "北 -Z ↑   入口在上方；后部密库位于 +Z", font=font(19), fill=MUTED)
    categories = {}
    for block_z in range(SIZE[2]):
        for block_x in range(SIZE[0]):
            floor = blocks.get((block_x, 2, block_z))
            if floor is None:
                continue
            color = "#3c4e52"
            category = "通路"
            for block_y in range(3, 8):
                state = blocks.get((block_x, block_y, block_z))
                if state is None:
                    continue
                name = state["Name"].removeprefix("minecraft:")
                if name in ("bookshelf", "dark_oak_log", "chiseled_bookshelf") or name == "dark_oak_slab":
                    color, category = "#877145", "书架"
                    break
                if name == "iron_bars" or name == "iron_door":
                    color, category = "#8aafb3", "铁栅 / 铁门"
                    break
                if name == "lectern":
                    color, category = "#e1c77b", "可阅读书籍"
                    break
                if name == "barrel":
                    color, category = "#9f7154", "空储物桶"
                    break
                if name in ("soul_lantern", "candle", "skeleton_skull", "brown_carpet", "chain", "cobweb", "black_wall_banner", "stone_pressure_plate", "birch_pressure_plate"):
                    continue
                color, category = "#283437", "墙 / 石台"
            categories[category] = color
            left, top = margin_x + block_x * scale, margin_y + block_z * scale
            draw.rectangle((left, top, left + scale - 1, top + scale - 1), fill=color)
    for block_x, block_z, label in ((15, 5, "01"), (15, 13, "02"), (15, 19, "03"), (7, 25, "04"), (24, 25, "05"), (15, 33, "06")):
        center = (margin_x + (block_x + 0.5) * scale, margin_y + (block_z + 0.5) * scale)
        draw.rounded_rectangle((center[0] - 22, center[1] - 17, center[0] + 22, center[1] + 17), radius=5, fill="#101a1e", outline=ACCENT, width=2)
        draw.text(center, label, font=font(20), fill=ACCENT, anchor="mm")
    legend_x = margin_x + SIZE[0] * scale + 30
    for index, text in enumerate(("01 入口前室", "02 阅档主厅", "03 中央石桌", "04 墓龛骨匣", "05 抄录囚室", "06 铁栅密库")):
        draw.text((legend_x, 160 + index * 47), text, font=font(20), fill=TEXT)
    for index, (label, color) in enumerate(categories.items()):
        top = 550 + index * 43
        draw.rectangle((legend_x, top, legend_x + 18, top + 18), fill=color)
        draw.text((legend_x + 29, top - 4), label, font=font(17), fill=MUTED)
    draw.text((48, size[1] - 54), "占地 31×39 格 / 高16格预留 / 主地面 +3 / 单层 + 局部高桌", font=font(20), fill=MUTED)
    return canvas


def design_sheet(hero, plan, exterior):
    canvas = Image.new("RGB", (2400, 1780), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((72, 42), "TOMB—PRISON ARCHIVES", font=font(24), fill=ACCENT)
    draw.text((68, 86), "墓狱档案地牢", font=font(57), fill=TEXT)
    draw.text((72, 166), "31 × 39 × 16 格预留   /   Minecraft 1.20.1   /   全原版方块", font=font(25), fill=MUTED)
    draw.line((72, 223, 2328, 223), fill="#34484c", width=2)
    hero_view = hero.crop((0, 90, hero.width, hero.height - 70))
    hero_view.thumbnail((1640, 1260), Image.Resampling.LANCZOS)
    canvas.paste(hero_view, (48 + (1640 - hero_view.width) // 2, 260 + (1260 - hero_view.height) // 2))
    draw.rounded_rectangle((1745, 270, 2328, 1375), radius=18, fill=PANEL)
    plan_view = plan.crop((60, 118, 60 + 31 * 26 + 28, 118 + 39 * 26 + 30))
    plan_view.thumbnail((450, 610), Image.Resampling.NEAREST)
    canvas.paste(plan_view, (1810, 305))
    draw.text((1790, 945), "一条贯通的探索动线", font=font(28), fill=TEXT)
    for index, label in enumerate(("01 / 入口前室 · 青灯拱门", "02 / 阅档大厅 · 双侧高书架", "03 / 中央石桌 · 可读档案", "04 / 侧翼墓龛 · 骨匣石棺", "05 / 抄录囚室 · 双向踏板门", "06 / 后部密库 · 吊链封名册")):
        draw.text((1790, 1008 + index * 48), label, font=font(22), fill=MUTED)
    draw.line((72, 1460, 2328, 1460), fill="#34484c", width=2)
    labels = [("深板岩砖 / 裂纹与雕纹", "#62686a"), ("深色橡木框 / 原版书架", "#866b45"), ("铁栅 / 铁链 / 铁门", "#abb7b7"), ("灵魂灯笼 / 暖烛 / 骨匣", "#68d4cc")]
    for index, (label, color) in enumerate(labels):
        left = 75 + index * 570
        draw.rectangle((left, 1512, left + 25, 1537), fill=color)
        draw.text((left + 42, 1508), label, font=font(24), fill=TEXT)
    draw.text((72, 1600), "参考图落地方块结构；完整成品含屋顶。此图为剖面软件预览，不是游戏截图。", font=font(24), fill=MUTED)
    draw.text((72, 1650), "独立建筑数据包 + NBT 模板 · 无自动刷怪 / 无奖励逻辑 / 未写入玩家存档", font=font(24), fill=MUTED)
    draw.text((72, 1708), "FT / STRUCTURE STUDY / 2026.11.01", font=font(20), fill=ACCENT)
    return canvas


def main():
    global ASSETS
    parser = argparse.ArgumentParser()
    parser.add_argument("--textures-jar", type=Path, default=CLIENT)
    arguments = parser.parse_args()
    ASSETS = zipfile.ZipFile(arguments.textures_jar)
    template = load_structure(ROOT / "out/FT_Tomb_Archive.nbt")
    blocks = {tuple(entry["pos"]): template["palette"][entry["state"]] for entry in template["blocks"]}
    PREVIEW.mkdir(parents=True, exist_ok=True)
    hero = isometric(blocks)
    hero.save(PREVIEW / "tomb-archive-cutaway.png")
    exterior = isometric(blocks, exterior=True)
    exterior.save(PREVIEW / "tomb-archive-exterior.png")
    isometric(blocks, size=(1600, 1300), detail=True).save(PREVIEW / "tomb-archive-vault.png")
    plan = topdown(blocks)
    plan.save(PREVIEW / "tomb-archive-floorplan.png")
    design_sheet(hero, plan, exterior).save(PREVIEW / "tomb-archive-design-sheet.png")
    write_json(ROOT / "qa/render-manifest.json", {"source": "Final compressed Minecraft NBT", "block_count": len(blocks), "models": sorted(USED_MODELS), "textures": sorted(USED_TEXTURES), "views": ["cutaway", "exterior", "vault", "floorplan", "design-sheet"], "limitations": "Software orthographic preview with vanilla JSON block meshes/textures. Skull, banner and lectern book entity models are simplified. Cutaway removes roofs, near wall and upper near shelves. No Minecraft gameplay or lighting simulation."})
    print(json.dumps({"preview": str(PREVIEW), "blocks": len(blocks), "models": len(USED_MODELS), "textures": len(USED_TEXTURES)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
