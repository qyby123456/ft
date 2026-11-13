import argparse
import copy
import functools
import hashlib
import importlib.util
import json
import math
import shutil
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFont


ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools")
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure

SOURCE = Path("D:/ft工程文件/墓狱档案地牢_20261101/tools")
sys.path.insert(0, str(SOURCE))
specification = importlib.util.spec_from_file_location("archive_renderer", SOURCE / "render_archive.py")
renderer = importlib.util.module_from_spec(specification)
specification.loader.exec_module(renderer)
renderer.ASSETS = zipfile.ZipFile(renderer.CLIENT)
REVIEW = ROOT / "four-view-review"
ROOM_IDS = ("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")
VIEWS = (("north", "北面 / 正视", (-0.35, -1)), ("south", "南面 / 后视", (0.35, 1)), ("west", "西面 / 左视", (-1, 0.35)), ("east", "东面 / 右视", (1, -0.35)))
INVISIBLE = {"air", "cave_air", "void_air", "light", "structure_void"}


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


@functools.lru_cache(maxsize=None)
def model(identifier):
    raw = renderer.asset_json("models/" + identifier.removeprefix("minecraft:") + ".json")
    parent = raw.get("parent", "").removeprefix("minecraft:")
    result = copy.deepcopy(model(parent)) if parent and not parent.startswith("builtin/") else {"textures": {}}
    result["textures"].update(raw.get("textures", {}))
    if "elements" in raw:
        result["elements"] = raw["elements"]
    return result


def box_model(start, end, identifier):
    return {"textures": {}, "elements": [{"from": start, "to": end, "faces": {face: {"texture": identifier} for face in renderer.FACE_STEPS}}]}


def definitions(name, properties):
    if name.endswith("_wall_banner"):
        definition = box_model([2, -12, 14], [14, 15, 15], "block/" + name.removesuffix("_wall_banner") + "_wool")
        return [(definition, {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]})]
    if name == "skeleton_skull":
        return [renderer.synthetic_model(name, properties)]
    if name in ("water", "lava"):
        return [(box_model([0, 0, 0], [16, 14, 16], "block/" + name + "_still"), {})]
    if name == "chest":
        definition = box_model([1, 0, 1], [15, 14, 15], "block/oak_planks")
        atlas = "entity/chest/normal"
        regions = {"north": [14, 33, 28, 43], "south": [42, 33, 56, 43], "west": [0, 33, 14, 43], "east": [28, 33, 42, 43], "up": [14, 0, 28, 14], "down": [28, 0, 42, 14]}
        definition["elements"][0]["faces"] = {face: {"texture": atlas, "uv": [value / 4 for value in region]} for face, region in regions.items()}
        definition["elements"].extend(box_model([7, 6, 0], [9, 10, 1], "block/gold_block")["elements"])
        return [(definition, {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]})]
    choices = [(model(choice["model"]), choice) for choice in renderer.selected_models(name, properties)]
    if name == "bell":
        choices.append((box_model([4, 4, 4], [12, 12, 12], "block/gold_block"), {}))
    return choices


@functools.lru_cache(maxsize=None)
def geometry(name, properties_tuple):
    properties = dict(properties_tuple)
    result = []
    for definition, choice in definitions(name, properties):
        for element in definition.get("elements", []):
            for face, surface in element["faces"].items():
                points = renderer.vertices(element, face)
                if "rotation" in element:
                    options = element["rotation"]
                    points = [renderer.rotate(point, options["axis"], options["angle"], options["origin"]) for point in points]
                points = [renderer.rotate(renderer.rotate(point, "x", -choice.get("x", 0)), "y", choice.get("y", 0)) for point in points]
                first = [points[1][axis] - points[0][axis] for axis in range(3)]
                second = [points[2][axis] - points[1][axis] for axis in range(3)]
                normal = (first[1] * second[2] - first[2] * second[1], first[2] * second[0] - first[0] * second[2], first[0] * second[1] - first[1] * second[0])
                length = math.sqrt(sum(value * value for value in normal))
                if length < 0.001:
                    continue
                normal = tuple(value / length for value in normal)
                cull = None
                if "cullface" in surface:
                    original = renderer.FACE_STEPS[surface["cullface"]]
                    cull = renderer.rotate(renderer.rotate(original, "x", -choice.get("x", 0), (0, 0, 0)), "y", choice.get("y", 0), (0, 0, 0))
                    cull = tuple(round(value) for value in cull)
                identifier = surface["texture"]
                while identifier.startswith("#"):
                    identifier = definition["textures"][identifier[1:]]
                points = [tuple(value / 16 for value in point) for point in points]
                result.append((points, normal, cull, identifier.removeprefix("minecraft:"), tuple(surface.get("uv", renderer.default_uv(element, face))), surface.get("rotation", 0)))
    if name == "lectern" and properties.get("has_book") == "true":
        for page in (0, 1):
            left = 2 + page * 6
            element = {"from": [left, 17, 4], "to": [left + 6, 17.5, 13]}
            points = [renderer.rotate(point, "y", {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]) for point in renderer.vertices(element, "up")]
            result.append(([tuple(value / 16 for value in point) for point in points], (0, 1, 0), None, "preview/book_page", (0, 0, 16, 16), 0))
    return result


@functools.lru_cache(maxsize=None)
def opaque(block):
    if block is None:
        return False
    name = block.name.removeprefix("minecraft:")
    if name in INVISIBLE or name in ("water", "lava"):
        return False
    if name.endswith(("_glass", "_leaves", "_wall_banner")):
        return False
    parts = geometry(name, block.properties)
    return len(parts) == 6 and all(all(abs(value - round(value)) < 0.001 for value in point) for part in parts for point in part[0])


def filtered(structure, metadata, camera, exterior):
    width, height, depth = structure.size
    feet = metadata["entrance"][1]
    room_id = metadata["id"]
    roof_positions = set()
    if room_id == "M2":
        blueprint = json.loads(Path("D:/ft工程文件/锈铜宝库地牢_20261101/qa/blueprint.json").read_text(encoding="utf-8"))
        roof_positions = {tuple(position) for position in blueprint["roof"]}
    result = {}
    for position, block in structure.blocks.items():
        name = block.name.removeprefix("minecraft:")
        if name in INVISIBLE:
            continue
        block_x, block_y, block_z = position
        if not exterior:
            vaulted_roof = room_id == "L1" and block_y >= 8 + min(2, (12 - max(abs(block_x - 12), abs(block_z - 12))) // 4)
            flat_roof = room_id not in ("L1", "L2", "M2") and (block_y == height - 1 or room_id == "S2" and block_y >= 7 or room_id == "M1" and block_y == 10 and name == "deepslate_tiles")
            if vaulted_roof or flat_roof or position in roof_positions:
                continue
            close_x = block_x == 0 if camera[0] < 0 else block_x == width - 1
            close_z = block_z == 0 if camera[1] < 0 else block_z == depth - 1
            if room_id == "M2":
                close_x = block_x <= 2 if camera[0] < 0 else block_x >= width - 3
                close_z = block_z <= 6 if camera[1] < 0 else block_z >= depth - 3
            if block_y >= feet and (close_x or close_z):
                continue
        result[position] = block
    return result


def render(structure, metadata, camera, title, exterior, size=(1400, 1080)):
    length = math.hypot(*camera)
    camera_x, camera_z = (value / length for value in camera)
    camera_y = 0.8
    horizontal_scale = 1 / math.hypot(1, camera_y)
    vertical_scale = camera_y * horizontal_scale
    visible = filtered(structure, metadata, camera, exterior)

    def project(point):
        horizontal = camera_x * point[0] + camera_z * point[2]
        return camera_z * point[0] - camera_x * point[2], vertical_scale * horizontal - horizontal_scale * point[1]

    faces = []
    for position, block in visible.items():
        name = block.name.removeprefix("minecraft:")
        for points, normal, cull, identifier, uv, rotation in geometry(name, block.properties):
            if normal[0] * camera_x + normal[1] * camera_y + normal[2] * camera_z <= 0.0001:
                continue
            if cull and opaque(visible.get(tuple(position[axis] + cull[axis] for axis in range(3)))):
                continue
            world = [tuple(point[axis] + position[axis] for axis in range(3)) for point in points]
            depth = sum(camera_x * point[0] + camera_y * point[1] + camera_z * point[2] for point in world) / 4
            brightness = 1.1 if normal[1] > 0.5 else 0.9 if normal[0] < 0 else 1.0
            faces.append((depth, [project(point) for point in world], identifier, uv, rotation, brightness))
    projected = [point for face in faces for point in face[1]]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min((size[0] - 100) / (maximum_x - minimum_x), (size[1] - 190) / (maximum_y - minimum_y))
    offset_x = size[0] / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 115 + (size[1] - 190) / 2 - (minimum_y + maximum_y) * scale / 2
    image = Image.new("RGBA", size, "#101a20")
    for depth, points, identifier, uv, rotation, brightness in sorted(faces, key=lambda item: item[0]):
        coordinates = [(point[0] * scale + offset_x, point[1] * scale + offset_y) for point in points]
        renderer.parallelogram(image, renderer.texture(identifier, uv, rotation, brightness), coordinates)
    draw = ImageDraw.Draw(image)
    draw.text((35, 22), metadata["id"] + " " + metadata["name"] + " · " + title, font=font(30), fill="#ede8d6")
    draw.text((35, 69), "最终 NBT / 1.20.1 原版材质 / " + ("完整外壳" if exterior else "移除屋面与近侧外墙，保留室内柱梁及陈设"), font=font(20), fill="#8ebbb9")
    draw.text((35, size[1] - 42), "方块数据检查图，非游戏截图；箱子/铃铛/旗帜为近似实体外观。", font=font(18), fill="#99a4aa")
    return image.convert("RGB")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--rooms", nargs="+", default=ROOM_IDS)
    parser.add_argument("--revision", default="baseline")
    parser.add_argument("--source-root", type=Path, default=ROOT / "incoming")
    parser.add_argument("--skip-exterior", action="store_true")
    arguments = parser.parse_args()
    report = []
    for room_id in arguments.rooms:
        incoming = arguments.source_root / room_id
        target = REVIEW / arguments.revision / room_id
        target.mkdir(parents=True, exist_ok=True)
        source = target / "room.nbt"
        if not source.exists():
            shutil.copy2(incoming / "room.nbt", source)
            shutil.copy2(incoming / "room.json", target / "room.json")
        digest = hashlib.sha256(source.read_bytes()).hexdigest()
        if digest != hashlib.sha256((incoming / "room.nbt").read_bytes()).hexdigest():
            raise ValueError("Refusing to mix old review snapshot with changed incoming: " + room_id)
        metadata = json.loads((target / "room.json").read_text(encoding="utf-8-sig"))
        structure = Structure.read(source)
        for exterior in ((False,) if arguments.skip_exterior else (False, True)):
            sheet = Image.new("RGB", (2800, 2160), "#101a20")
            mode = "exterior" if exterior else "interior"
            for index, (direction, title, camera) in enumerate(VIEWS):
                image = render(structure, metadata, camera, title, exterior)
                image.save(target / (mode + "-" + direction + ".png"))
                sheet.paste(image, ((index % 2) * 1400, (index // 2) * 1080))
            sheet.save(target / (mode + "-four-views.png"))
        report.append({"id": room_id, "sha256": digest, "size": list(structure.size), "source": str(source), "concept": metadata["reference"]})
        print("RENDERED " + room_id, flush=True)
    (REVIEW / arguments.revision / "render-manifest.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
