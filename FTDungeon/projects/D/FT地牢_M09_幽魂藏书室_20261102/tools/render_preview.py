from __future__ import annotations

import io
import json
import math
import zipfile
from functools import lru_cache
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFont

from build_m09 import OUT, ROOT, digest, dump
from nbtio import AIR, Structure


GAME_JAR = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/FarmingTales_ForgeⅡ v1.5.2.jar")
WIDTH, HEIGHT = 1920, 1440
FONT = "C:/Windows/Fonts/msyh.ttc"


def font(size):
    return ImageFont.truetype(FONT, size)


def projection(vertex):
    block_x, height, block_z = vertex
    return block_x + block_z, (block_x - block_z) * 0.5 - height * 1.04


def background(title, subtitle):
    canvas = Image.new("RGBA", (WIDTH, HEIGHT), (20, 23, 29, 255))
    draw = ImageDraw.Draw(canvas)
    for screen_y in range(HEIGHT):
        draw.line((0, screen_y, WIDTH, screen_y), fill=(20 + screen_y * 6 // HEIGHT, 23 + screen_y * 5 // HEIGHT, 29 + screen_y * 4 // HEIGHT, 255))
    draw.text((72, 40), title, font=font(44), fill=(236, 226, 208, 255))
    draw.text((76, 109), subtitle, font=font(22), fill=(157, 187, 187, 255))
    draw.line((72, 154, WIDTH - 72, 154), fill=(63, 76, 78), width=2)
    return canvas


def texture_loader():
    archive = zipfile.ZipFile(GAME_JAR)
    inventory = set(archive.namelist())

    @lru_cache(maxsize=250)
    def texture(name, face, brightness, uv=(0, 0, 16, 16)):
        direct_resource = name.removeprefix("texture:") if name.startswith("texture:") else None
        short = direct_resource.rsplit("/", 1)[-1] if direct_resource else name.removeprefix("minecraft:")
        material = short
        if short in ("dark_oak_stairs",):
            material = "dark_oak_planks"
        elif short == "bookshelf" and face == "top":
            material = "oak_planks"
        elif short == "cobbled_deepslate_slab":
            material = "cobbled_deepslate"
        elif short == "enchanting_table":
            material = "enchanting_table_top" if face == "top" else "enchanting_table_side"
        elif short == "barrel":
            material = "barrel_top" if face == "north" else "barrel_side"
        elif short == "lectern":
            material = "lectern_top" if face == "top" else "lectern_front"
        elif short == "dark_oak_log":
            material = "dark_oak_log_top" if face == "top" else short
        elif short == "purple_wall_banner":
            material = "purple_wool"
        elif short == "soul_lantern":
            material = "soul_lantern"
        elif short == "candle":
            material = "candle_lit"
        if short == "skeleton_skull":
            source = Image.open(io.BytesIO(archive.read("assets/minecraft/textures/entity/skeleton/skeleton.png"))).convert("RGBA")
            tile = source.crop((8, 0, 16, 8) if face == "top" else (8, 8, 16, 16)).resize((16, 16), Image.Resampling.NEAREST)
        else:
            resource = f"assets/minecraft/textures/{direct_resource}.png" if direct_resource else f"assets/minecraft/textures/block/{material}.png"
            if resource not in inventory:
                raise ValueError(f"Missing real vanilla texture: {name}, {face}, {resource}")
            tile = Image.open(io.BytesIO(archive.read(resource))).convert("RGBA").crop((0, 0, 16, 16))
        if "glass" in short:
            base = Image.new("RGBA", (16, 16), (51, 160, 169, 48))
            base.alpha_composite(tile)
            tile = base
        alpha = tile.getchannel("A")
        tile = ImageEnhance.Brightness(tile.convert("RGB")).enhance(brightness).convert("RGBA")
        tile.putalpha(alpha)
        first_u, first_v, last_u, last_v = uv
        tile = tile.crop((min(first_u, last_u), min(first_v, last_v), max(first_u, last_u), max(first_v, last_v)))
        if first_u > last_u:
            tile = tile.transpose(Image.Transpose.FLIP_LEFT_RIGHT)
        if first_v > last_v:
            tile = tile.transpose(Image.Transpose.FLIP_TOP_BOTTOM)
        tile = tile.resize((16, 16), Image.Resampling.NEAREST)
        return tile

    return texture


def native_model_loader():
    archive = zipfile.ZipFile(GAME_JAR)

    @lru_cache(maxsize=50)
    def model(name):
        location = name.removeprefix("minecraft:")
        data = json.loads(archive.read(f"assets/minecraft/models/{location}.json"))
        inherited = model(data["parent"]) if "parent" in data else {}
        return {"elements": data.get("elements", inherited.get("elements", [])), "textures": {**inherited.get("textures", {}), **data.get("textures", {})}}

    def faces(position, block):
        if block.name == "minecraft:candle":
            count = {"1": "one_candle", "2": "two_candles", "3": "three_candles", "4": "four_candles"}[block.get("candles")]
            name = "minecraft:block/candle_" + count + ("_lit" if block.get("lit") == "true" else "")
        elif block.name == "minecraft:soul_lantern":
            name = "minecraft:block/soul_lantern_hanging" if block.get("hanging") == "true" else "minecraft:block/soul_lantern"
        elif block.name in ("minecraft:chain", "minecraft:cobweb"):
            name = "minecraft:block/" + block.name.removeprefix("minecraft:")
        else:
            return None
        data = model(name)
        result = []
        for element in data["elements"]:
            low_x, low_y, low_z = (value / 16 for value in element["from"])
            high_x, high_y, high_z = (value / 16 for value in element["to"])
            vertices_by_face = {
                "east": [(high_x, low_y, low_z), (high_x, low_y, high_z), (high_x, high_y, high_z), (high_x, high_y, low_z)],
                "north": [(low_x, low_y, low_z), (high_x, low_y, low_z), (high_x, high_y, low_z), (low_x, high_y, low_z)],
                "up": [(low_x, high_y, low_z), (high_x, high_y, low_z), (high_x, high_y, high_z), (low_x, high_y, high_z)],
            }
            for face, definition in element["faces"].items():
                if face not in vertices_by_face:
                    continue
                vertices = vertices_by_face[face]
                rotation = element.get("rotation")
                if rotation:
                    angle = math.radians(rotation["angle"])
                    origin = [value / 16 for value in rotation["origin"]]
                    axis = rotation["axis"]
                    rotated = []
                    for vertex in vertices:
                        offset = [vertex[index] - origin[index] for index in range(3)]
                        if rotation.get("rescale"):
                            offset = [value if "xyz"[index] == axis else value / math.cos(angle) for index, value in enumerate(offset)]
                        if axis == "y":
                            offset = [offset[0] * math.cos(angle) + offset[2] * math.sin(angle), offset[1], -offset[0] * math.sin(angle) + offset[2] * math.cos(angle)]
                        elif axis == "x":
                            offset = [offset[0], offset[1] * math.cos(angle) - offset[2] * math.sin(angle), offset[1] * math.sin(angle) + offset[2] * math.cos(angle)]
                        else:
                            offset = [offset[0] * math.cos(angle) - offset[1] * math.sin(angle), offset[0] * math.sin(angle) + offset[1] * math.cos(angle), offset[2]]
                        rotated.append(tuple(offset[index] + origin[index] for index in range(3)))
                    vertices = rotated
                vertices = [tuple(vertex[index] + position[index] for index in range(3)) for vertex in vertices]
                resource = definition["texture"]
                while resource.startswith("#"):
                    resource = data["textures"][resource[1:]]
                resource = resource.removeprefix("minecraft:")
                brightness = 1.20 if block.name in ("minecraft:candle", "minecraft:soul_lantern") else 0.9
                result.append((vertices, "texture:" + resource, "top" if face == "up" else face, brightness, tuple(definition.get("uv", (0, 0, 16, 16)))))
        return result

    return faces


def textured_face(canvas, points, tile):
    left = math.floor(min(point[0] for point in points))
    top = math.floor(min(point[1] for point in points))
    right = math.ceil(max(point[0] for point in points)) + 1
    bottom = math.ceil(max(point[1] for point in points)) + 1
    first = points[0]
    horizontal = (points[1][0] - first[0], points[1][1] - first[1])
    vertical = (points[3][0] - first[0], points[3][1] - first[1])
    determinant = horizontal[0] * vertical[1] - horizontal[1] * vertical[0]
    if abs(determinant) < 0.001:
        return
    horizontal_x = 16 * vertical[1] / determinant
    horizontal_y = -16 * vertical[0] / determinant
    vertical_x = -16 * horizontal[1] / determinant
    vertical_y = 16 * horizontal[0] / determinant
    offset_x, offset_y = left - first[0], top - first[1]
    patch = tile.transform((right - left, bottom - top), Image.Transform.AFFINE, (horizontal_x, horizontal_y, horizontal_x * offset_x + horizontal_y * offset_y, vertical_x, vertical_y, vertical_x * offset_x + vertical_y * offset_y), Image.Resampling.NEAREST)
    mask = Image.new("L", patch.size)
    ImageDraw.Draw(mask).polygon([(screen_x - left, screen_y - top) for screen_x, screen_y in points], fill=255)
    patch.putalpha(ImageChops.multiply(mask, patch.getchannel("A")))
    canvas.alpha_composite(patch, (left, top))


def render(structure, shapes, texture, cutaway):
    native_faces = native_model_loader()
    blocks = {}
    for position, block in structure.blocks.items():
        if block == AIR:
            continue
        block_x, height, block_z = position
        if cutaway and (height >= 9 or (block_x == 20 or block_z == 0) and height >= 2):
            continue
        blocks[position] = block
    primitives = []
    for position, block in blocks.items():
        detailed = native_faces(position, block)
        if detailed is not None:
            for vertices, name, face, brightness, uv in detailed:
                center = tuple(sum(vertex[axis] for vertex in vertices) / 4 for axis in range(3))
                primitives.append((-center[0] + center[2] - center[1] * 1.15, vertices, name, face, brightness, uv))
            continue
        for bounds in shapes[(block.name, block.properties)]:
            low_x, low_y, low_z, high_x, high_y, high_z = bounds
            low_x, high_x = low_x + position[0], high_x + position[0]
            low_y, high_y = low_y + position[1], high_y + position[1]
            low_z, high_z = low_z + position[2], high_z + position[2]
            full = bounds == [0.0, 0.0, 0.0, 1.0, 1.0, 1.0]
            faces = (
                ("east", (position[0] + 1, position[1], position[2]), [(high_x, low_y, low_z), (high_x, low_y, high_z), (high_x, high_y, high_z), (high_x, high_y, low_z)], 0.84),
                ("north", (position[0], position[1], position[2] - 1), [(low_x, low_y, low_z), (high_x, low_y, low_z), (high_x, high_y, low_z), (low_x, high_y, low_z)], 0.67),
                ("top", (position[0], position[1] + 1, position[2]), [(low_x, high_y, low_z), (high_x, high_y, low_z), (high_x, high_y, high_z), (low_x, high_y, high_z)], 1.10),
            )
            for face_name, neighbor, vertices, brightness in faces:
                adjacent = blocks.get(neighbor)
                if full and adjacent is not None and shapes[(adjacent.name, adjacent.properties)] == [[0.0, 0.0, 0.0, 1.0, 1.0, 1.0]] and ("glass" not in adjacent.name or adjacent == block):
                    continue
                center = tuple(sum(vertex[axis] for vertex in vertices) / 4 for axis in range(3))
                depth = -center[0] + center[2] - center[1] * 1.15
                if block.name in ("minecraft:sea_lantern", "minecraft:soul_lantern", "minecraft:candle"):
                    brightness = 1.3
                primitives.append((depth, vertices, block.name, face_name, brightness, (0, 0, 16, 16)))
    projected = [projection(vertex) for _, vertices, _, _, _, _ in primitives for vertex in vertices]
    minimum_x, maximum_x = min(point[0] for point in projected), max(point[0] for point in projected)
    minimum_y, maximum_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min(1760 / (maximum_x - minimum_x), 1110 / (maximum_y - minimum_y))
    offset_x = WIDTH / 2 - (minimum_x + maximum_x) * scale / 2
    offset_y = 760 - (minimum_y + maximum_y) * scale / 2
    title = "M09  /  幽魂藏书室"
    subtitle = "实际 NBT · 原版纹理 + 原生形状软件预览 · 非实机截图" if cutaway else "完整封闭外壳 · 441 格实体屋顶保留 · 北侧三宽三高入口"
    canvas = background(title, subtitle)
    for _, vertices, name, face_name, brightness, uv in sorted(primitives, key=lambda item: item[0], reverse=True):
        points = [(projection(vertex)[0] * scale + offset_x, projection(vertex)[1] * scale + offset_y) for vertex in vertices]
        textured_face(canvas, points, texture(name, face_name, brightness, uv))
    draw = ImageDraw.Draw(canvas)
    draw.line((72, 1350, WIDTH - 72, 1350), fill=(63, 76, 78), width=2)
    draw.text((76, 1372), "21 × 12 × 21  |  北口 [10,1,0]  |  安全点 [10,1,2]  |  空容器 / 无实体 / 无真实火", font=font(23), fill=(201, 193, 175))
    if cutaway:
        draw.text((WIDTH - 670, 112), "仅预览隐去北/东上墙与 Y≥9 顶部", font=font(21), fill=(177, 208, 200))
    return canvas.convert("RGB")


def plan(structure, texture):
    canvas = Image.new("RGB", (1260, 1410), (22, 25, 29))
    draw = ImageDraw.Draw(canvas)
    draw.text((55, 28), "M09 / N ↑  实际 NBT 首层 · 干燥往返通路", font=font(32), fill=(231, 221, 204))
    cell, margin, top = 54, 63, 120
    for block_z in range(21):
        for block_x in range(21):
            block = structure.get((block_x, 1, block_z))
            if block == AIR:
                block = structure.get((block_x, 0, block_z))
            tile = texture(block.name, "top", 0.85).resize((cell, cell), Image.Resampling.NEAREST).convert("RGB")
            canvas.paste(tile, (margin + block_x * cell, top + block_z * cell))
    static = json.loads((ROOT / "qa/static-qa.json").read_text(encoding="utf-8"))
    for route in static["routes"]:
        points = [(margin + position[0] * cell + cell // 2, top + position[2] * cell + cell // 2) for position in route["steps"]]
        if len(points) > 1:
            draw.line(points, fill=(56, 223, 201), width=4)
        if points:
            screen_x, screen_y = points[-1]
            draw.ellipse((screen_x - 8, screen_y - 8, screen_x + 8, screen_y + 8), fill=(202, 255, 234))
    draw.text((63, 1294), "青线为静态空脚位路线；Forge 另验真实 0.6×1.8 玩家碰撞。", font=font(24), fill=(170, 211, 205))
    draw.text((63, 1338), "书桌两侧与椅后均可回到北口；东侧残骸和蛛网不侵入主路。", font=font(24), fill=(170, 211, 205))
    canvas.save(ROOT / "evidence/plan-dry-routes.png")


def main():
    structure = Structure.read(OUT / "room.nbt")
    native = json.loads((ROOT / "qa/native-registry-qa.json").read_text(encoding="utf-8"))
    if native["sha256"] != digest(OUT / "room.nbt") or native["status"] != "PASS":
        raise ValueError("Native shape evidence missing or stale")
    shapes = {(entry["name"], tuple(sorted(entry["properties"].items()))): entry["boxes"] for entry in native["native_shapes"]}
    texture = texture_loader()
    render(structure, shapes, texture, True).save(OUT / "preview.png")
    render(structure, shapes, texture, False).save(ROOT / "evidence/preview-full-envelope.png")
    plan(structure, texture)
    dump(ROOT / "qa/preview-provenance.json", {
        "status": "PASS", "source_nbt": str(OUT / "room.nbt"), "sha256": digest(OUT / "room.nbt"), "preview_sha256": digest(OUT / "preview.png"),
        "source": "Delivered NBT with native Minecraft outline AABBs and installed vanilla textures/models. Candles, chains, soul lanterns and cobwebs use real vanilla model elements and UVs. Textured isometric software preview, not a client screenshot.",
        "cutaway_changes_nbt": False, "full_roof_view": str(ROOT / "evidence/preview-full-envelope.png"), "route_plan": str(ROOT / "evidence/plan-dry-routes.png"),
        "limitations": "AABB previews approximate transparent and crossed models; block-entity book animation and real shader lighting are not rendered. No AI-generated picture is used as the finished-room preview.",
    })
    print("M09 actual-NBT cutaway, full roof and dry route plan rendered")


if __name__ == "__main__":
    main()
