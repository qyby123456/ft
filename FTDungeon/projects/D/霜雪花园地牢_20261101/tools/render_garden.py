import functools
import gzip
import io
import json
import math
import struct
import zipfile
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFont


ROOT = Path(__file__).resolve().parents[1]
CLIENT = Path("C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar")
ASSETS = zipfile.ZipFile(CLIENT)
PREVIEW = ROOT / "preview"
FONT_PATH = "C:/Windows/Fonts/msyh.ttc"
TEXTURES = {}


def read_string(stream):
    length = struct.unpack(">H", stream.read(2))[0]
    return stream.read(length).decode("utf-8")


def read_nbt(stream, kind):
    if kind == 3:
        return struct.unpack(">i", stream.read(4))[0]
    if kind == 8:
        return read_string(stream)
    if kind == 9:
        element = stream.read(1)[0]
        length = struct.unpack(">i", stream.read(4))[0]
        return [read_nbt(stream, element) for _ in range(length)]
    if kind == 10:
        result = {}
        while True:
            element = stream.read(1)[0]
            if element == 0:
                return result
            name = read_string(stream)
            result[name] = read_nbt(stream, element)
    raise ValueError(kind)


def load_structure():
    path = ROOT / "out/datapack/data/ftfrostgarden/structures/garden.nbt"
    stream = io.BytesIO(gzip.decompress(path.read_bytes()))
    assert stream.read(1) == b"\x0a"
    read_string(stream)
    template = read_nbt(stream, 10)
    result = {}
    for entry in template["blocks"]:
        position = tuple(entry["pos"][axis] + (-28, 61, 48)[axis] for axis in range(3))
        state = template["palette"][entry["state"]]
        result[position] = (state["Name"].removeprefix("minecraft:"), state.get("Properties", {}))
    return result


def texture_name(name, face):
    if name == "snow_block" or name == "snow":
        return "snow"
    if name.endswith("_slab") or name.endswith("_stairs") or name.endswith("_wall"):
        material = name.rsplit("_", 1)[0]
        aliases = {"stone_brick": "stone_bricks", "deepslate_brick": "deepslate_bricks", "spruce": "spruce_planks"}
        return aliases.get(material, material)
    if name in ("spruce_fence", "spruce_trapdoor"):
        return "spruce_planks" if name == "spruce_fence" else "spruce_trapdoor"
    if name in ("spruce_log", "stripped_spruce_log"):
        return name + "_top" if face == "up" else name
    if name == "grass_block":
        return "grass_block_top" if face == "up" else "dirt"
    if name in ("barrel", "chest"):
        return "barrel_top" if face == "up" else "barrel_side"
    if name == "soul_lantern":
        return "soul_lantern"
    if name == "iron_bars":
        return "iron_block"
    if name == "chain":
        return "deepslate_tiles"
    if name == "light_blue_stained_glass_pane":
        return "light_blue_stained_glass"
    return name


@functools.lru_cache(maxsize=None)
def texture(name, face, shade):
    filename = texture_name(name, face)
    path = f"assets/minecraft/textures/block/{filename}.png"
    try:
        image = Image.open(io.BytesIO(ASSETS.read(path))).convert("RGBA")
    except KeyError:
        raise ValueError(f"Missing vanilla texture: {filename}")
    image = image.crop((0, 0, image.width, image.width)).resize((16, 16), Image.Resampling.NEAREST)
    if name == "spruce_leaves":
        image = ImageChops.multiply(image, Image.new("RGBA", image.size, (90, 145, 130, 255)))
    if name in ("snow", "snow_block"):
        image = ImageChops.multiply(image, Image.new("RGBA", image.size, (205, 225, 246, 255)))
    if name == "grass_block" and face == "up":
        image = ImageChops.multiply(image, Image.new("RGBA", image.size, (100, 150, 135, 255)))
    if name in ("blue_ice", "packed_ice", "light_blue_stained_glass", "light_blue_stained_glass_pane"):
        shade = max(0.85, shade)
    image = ImageEnhance.Brightness(image).enhance(shade)
    TEXTURES[filename] = True
    return image


def cuboids(name, properties):
    if name == "snow":
        return [(0, 0, 0, 1, int(properties["layers"]) / 8, 1)]
    if name.endswith("_slab"):
        minimum = 0.5 if properties.get("type") == "top" else 0
        return [(0, minimum, 0, 1, minimum + 0.5, 1)]
    if name.endswith("_stairs"):
        facing = properties.get("facing", "north")
        top = {"north": (0, 0.5, 0, 1, 1, 0.5), "south": (0, 0.5, 0.5, 1, 1, 1), "east": (0.5, 0.5, 0, 1, 1, 1), "west": (0, 0.5, 0, 0.5, 1, 1)}[facing]
        return [(0, 0, 0, 1, 0.5, 1), top]
    if name.endswith("_fence"):
        return [(0.375, 0, 0.375, 0.625, 1, 0.625), (0, 0.375, 0.4375, 1, 0.5625, 0.5625), (0, 0.75, 0.4375, 1, 0.9375, 0.5625)]
    if name.endswith("_wall"):
        return [(0.25, 0, 0.25, 0.75, 1, 0.75), (0.3125, 0, 0, 0.6875, 0.8125, 1)]
    if name in ("light_blue_stained_glass_pane", "chain"):
        return [(0.4375, 0, 0.4375, 0.5625, 1, 0.5625)]
    if name == "iron_bars":
        return [(0.4375, 0, 0, 0.5625, 1, 1)]
    if name == "soul_lantern":
        return [(0.3125, 0.0625, 0.3125, 0.6875, 0.625, 0.6875), (0.4375, 0.625, 0.4375, 0.5625, 0.875, 0.5625)]
    if name == "spruce_trapdoor":
        return [(0, 0, 0, 1, 0.1875, 1)]
    if name == "chest":
        return [(0.0625, 0, 0.0625, 0.9375, 0.875, 0.9375)]
    return [(0, 0, 0, 1, 1, 1)]


def projection(point, camera):
    block_x, block_y, block_z = point[0], point[1] - 63, point[2] - 80
    camera_x, camera_z = camera
    length = math.hypot(camera_x, camera_z)
    horizontal = (camera_x * block_x + camera_z * block_z) / length
    return (camera_z * block_x - camera_x * block_z) / length, 0.5 * horizontal - block_y * 0.866


def view_depth(vertices, camera):
    length = math.hypot(*camera)
    return sum(0.866 * (camera[0] * vertex[0] + camera[1] * vertex[2]) / length + vertex[1] * 0.5 for vertex in vertices) / len(vertices)


def world_faces(blocks, camera):
    camera_x, camera_z = camera
    faces = []
    flowers = {"blue_orchid", "azure_bluet", "cornflower", "white_tulip", "cobweb"}
    for position, (name, properties) in blocks.items():
        if name == "light":
            continue
        if position[1] < 63:
            continue
        block_x, block_y, block_z = position
        if name in flowers:
            planes = [((0, 0, 0), (1, 0, 1), (1, 1, 1), (0, 1, 0)), ((1, 0, 0), (0, 0, 1), (0, 1, 1), (1, 1, 0))]
            for plane in planes:
                vertices = [(block_x + offset[0], block_y + offset[1], block_z + offset[2]) for offset in plane]
                depth = view_depth(vertices, camera)
                faces.append((depth, vertices, name, "north", 1.0))
            continue
        for minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z in cuboids(name, properties):
            vertices = [(block_x + offset_x, block_y + offset_y, block_z + offset_z) for offset_x, offset_y, offset_z in ((minimum_x, minimum_y, minimum_z), (maximum_x, minimum_y, minimum_z), (maximum_x, minimum_y, maximum_z), (minimum_x, minimum_y, maximum_z), (minimum_x, maximum_y, minimum_z), (maximum_x, maximum_y, minimum_z), (maximum_x, maximum_y, maximum_z), (minimum_x, maximum_y, maximum_z))]
            definitions = [("up", (4, 5, 6, 7), (0, 1, 0), 1.12), ("east" if camera_x > 0 else "west", (5, 1, 2, 6) if camera_x > 0 else (7, 3, 0, 4), (camera_x, 0, 0), 0.76), ("south" if camera_z > 0 else "north", (6, 2, 3, 7) if camera_z > 0 else (4, 0, 1, 5), (0, 0, camera_z), 0.9)]
            for face, indices, normal, shade in definitions:
                neighbor = (block_x + normal[0], block_y + normal[1], block_z + normal[2])
                if (minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z) == (0, 0, 0, 1, 1, 1) and neighbor in blocks:
                    other_name, other_properties = blocks[neighbor]
                    if cuboids(other_name, other_properties) == [(0, 0, 0, 1, 1, 1)] and other_name not in flowers:
                        continue
                polygon = [vertices[index] for index in indices]
                depth = view_depth(polygon, camera)
                faces.append((depth, polygon, name, face, shade))
    return sorted(faces, key=lambda entry: entry[0])


def paste_face(canvas, points, tile):
    minimum_x = math.floor(min(point[0] for point in points))
    minimum_y = math.floor(min(point[1] for point in points))
    maximum_x = math.ceil(max(point[0] for point in points)) + 1
    maximum_y = math.ceil(max(point[1] for point in points)) + 1
    width, height = maximum_x - minimum_x, maximum_y - minimum_y
    origin_x, origin_y = points[0][0] - minimum_x, points[0][1] - minimum_y
    along_x, along_y = points[1][0] - points[0][0], points[1][1] - points[0][1]
    across_x, across_y = points[3][0] - points[0][0], points[3][1] - points[0][1]
    determinant = along_x * across_y - across_x * along_y
    if abs(determinant) < 0.05:
        return
    coefficient_a, coefficient_b = across_y * 16 / determinant, -across_x * 16 / determinant
    coefficient_d, coefficient_e = -along_y * 16 / determinant, along_x * 16 / determinant
    transform = (coefficient_a, coefficient_b, -coefficient_a * origin_x - coefficient_b * origin_y, coefficient_d, coefficient_e, -coefficient_d * origin_x - coefficient_e * origin_y)
    patch = tile.transform((width, height), Image.Transform.AFFINE, transform, Image.Resampling.NEAREST)
    canvas.alpha_composite(patch, (minimum_x, minimum_y))


def render(blocks, camera, label, filename, size=(1800, 1380)):
    faces = world_faces(blocks, camera)
    projected = [projection(vertex, camera) for _, polygon, _, _, _ in faces for vertex in polygon]
    bounds = (min(point[0] for point in projected), min(point[1] for point in projected), max(point[0] for point in projected), max(point[1] for point in projected))
    scale = min((size[0] - 140) / (bounds[2] - bounds[0]), (size[1] - 240) / (bounds[3] - bounds[1]))
    translate_x = size[0] / 2 - (bounds[0] + bounds[2]) * scale / 2
    translate_y = 130 - bounds[1] * scale
    canvas = Image.new("RGBA", size, (14, 24, 40, 255))
    background = ImageDraw.Draw(canvas)
    for row in range(130, size[1]):
        amount = (row - 130) / (size[1] - 130)
        background.line((0, row, size[0], row), fill=(14 + round(amount * 8), 24 + round(amount * 10), 40 + round(amount * 14)))
    for _, polygon, name, face, shade in faces:
        points = [(projection(vertex, camera)[0] * scale + translate_x, projection(vertex, camera)[1] * scale + translate_y) for vertex in polygon]
        paste_face(canvas, points, texture(name, face, shade))
    draw = ImageDraw.Draw(canvas)
    draw.text((62, 32), "霜雪花园  /  FROST GARDEN", font=ImageFont.truetype(FONT_PATH, 37), fill="#e5f5ff")
    draw.text((64, 86), label, font=ImageFont.truetype(FONT_PATH, 19), fill="#98bfd3")
    draw.text((64, size[1] - 49), "最终 NBT 方块结构 · 原版贴图软件示意（非游戏截图；局部方块形状近似）", font=ImageFont.truetype(FONT_PATH, 17), fill="#86a5bb")
    canvas.convert("RGB").save(PREVIEW / filename)
    print(filename, len(faces), "faces")


def top_view(blocks):
    size = (1600, 1660)
    canvas = Image.new("RGB", size, "#101c2d")
    draw = ImageDraw.Draw(canvas)
    scale = 21
    for block_x in range(-28, 29):
        for block_z in range(48, 113):
            column = [(position, state) for position, state in blocks.items() if position[0] == block_x and position[2] == block_z and state[0] != "light"]
            if not column:
                continue
            position, (name, properties) = max(column, key=lambda entry: entry[0][1])
            tile = texture(name, "up", 1.05).resize((scale, scale), Image.Resampling.NEAREST)
            if position[1] >= 67:
                tile = ImageEnhance.Brightness(tile).enhance(0.8)
            canvas.paste(tile, (200 + (block_x + 28) * scale, 180 + (block_z - 48) * scale), tile)
    draw.text((60, 32), "霜雪花园 / 布局与动线", font=ImageFont.truetype(FONT_PATH, 36), fill="#e6f5ff")
    draw.text((60, 89), "入口（上） → 十字石径 → 环形冰泉 → 后方封印宝匣", font=ImageFont.truetype(FONT_PATH, 22), fill="#98bfd3")
    for text, location, label in [("冰封喷泉", (0, 80), (1260, 805)), ("霜花坛 ×4", (-12, 67), (70, 540)), ("雪松廊架", (-9, 100), (64, 1430)), ("冰湖", (19, 88), (1360, 1110)), ("兜帽石像", (19, 67), (1320, 480))]:
        anchor = (200 + (location[0] + 28.5) * scale, 180 + (location[1] - 47.5) * scale)
        draw.line((anchor, label), fill="#66bed8", width=2)
        draw.ellipse((anchor[0] - 4, anchor[1] - 4, anchor[0] + 4, anchor[1] + 4), fill="#b2eeff")
        draw.rounded_rectangle((label[0] - 7, label[1] - 4, label[0] + len(text) * 21 + 9, label[1] + 31), radius=5, fill="#193248")
        draw.text(label, text, font=ImageFont.truetype(FONT_PATH, 20), fill="#dbf5ff")
    draw.text((60, 1610), "49×49 围墙 · 57×65 地景 · 另附 32 格旧大厅连接路 / 最终 NBT 软件俯视示意", font=ImageFont.truetype(FONT_PATH, 18), fill="#86a5bb")
    canvas.save(PREVIEW / "frost-garden-top.png")


def main():
    PREVIEW.mkdir(parents=True, exist_ok=True)
    blocks = load_structure()
    render(blocks, (1, -1), "3/4 总览 · 冰泉 / 四霜花坛 / 开放铁栅拱门 / 积雪廊架 / 冰湖", "frost-garden-overview.png")
    for camera, label, filename in [((0.15, -1), "正向 / 入口", "front"), ((-0.15, 1), "背向 / 廊架与宝匣", "back"), ((-1, -0.15), "左侧 / 高雪松", "left"), ((1, 0.15), "右侧 / 冰湖与石像", "right")]:
        render(blocks, camera, label, "frost-garden-" + filename + ".png", (1400, 1080))
    sheet = Image.new("RGB", (1800, 1390), "#101c2d")
    for index, name in enumerate(("front", "back", "left", "right")):
        tile = Image.open(PREVIEW / f"frost-garden-{name}.png").resize((900, 695), Image.Resampling.LANCZOS)
        sheet.paste(tile, ((index % 2) * 900, (index // 2) * 695))
    sheet.save(PREVIEW / "frost-garden-four-views.png")
    top_view(blocks)
    (ROOT / "qa/render-manifest.json").write_text(json.dumps({"source": "final compressed Minecraft structure NBT", "blocks": len(blocks), "vanilla_textures": sorted(TEXTURES), "views": ["overview", "front", "back", "left", "right", "top"], "limitations": "Software orthographic preview, simplified non-cube block meshes, not Minecraft rendering or FPS evidence."}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


if __name__ == "__main__":
    main()
