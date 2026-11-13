from __future__ import annotations

import io
import json
import math
import random
import zipfile
from functools import lru_cache
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageEnhance, ImageFont

from build_m5 import OUT, ROOT, digest, dump
from nbtio import AIR, Structure


GAME_JAR = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/FarmingTales_ForgeⅡ v1.5.2.jar")
FONT = "C:/Windows/Fonts/msyh.ttc"
WIDTH, HEIGHT = 1800, 1360
TEXTURES = {
    "purpur_stairs": "purpur_block", "purpur_slab": "purpur_block",
    "end_stone_brick_stairs": "end_stone_bricks",
    "polished_blackstone_wall": "polished_blackstone",
    "chorus_flower": "chorus_flower_dead", "large_amethyst_bud": "large_amethyst_bud",
}


def font(size):
    return ImageFont.truetype(FONT, size)


def raw_projection(vertex):
    block_x, height, block_z = vertex
    return block_x - block_z, -(block_x + block_z) * 0.5 - height * 1.05


def background(title, subtitle):
    canvas = Image.new("RGBA", (WIDTH, HEIGHT), (20, 18, 35, 255))
    draw = ImageDraw.Draw(canvas)
    for screen_y in range(HEIGHT):
        color = (20 + screen_y * 6 // HEIGHT, 18 + screen_y * 4 // HEIGHT, 35 + screen_y * 12 // HEIGHT, 255)
        draw.line((0, screen_y, WIDTH, screen_y), fill=color)
    generator = random.Random(503)
    for _ in range(150):
        screen_x, screen_y = generator.randrange(35, WIDTH - 35), generator.randrange(170, HEIGHT - 130)
        draw.rectangle((screen_x, screen_y, screen_x + 1, screen_y + 1), fill=(67, 53, 91, 255))
    draw.text((74, 42), title, font=font(44), fill=(239, 227, 248, 255))
    draw.text((78, 112), subtitle, font=font(22), fill=(166, 153, 186, 255))
    draw.line((76, 160, WIDTH - 76, 160), fill=(66, 51, 86), width=2)
    return canvas


def texture_loader():
    archive = zipfile.ZipFile(GAME_JAR)

    @lru_cache(maxsize=200)
    def texture(name, face, shade):
        short = name.removeprefix("minecraft:")
        material = TEXTURES.get(short, short)
        if short == "end_rod":
            tile = Image.new("RGBA", (16, 16), (218, 255, 247, 255))
            tile_draw = ImageDraw.Draw(tile)
            tile_draw.line((0, 0, 0, 15), fill=(142, 211, 211), width=2)
        else:
            if short == "purpur_pillar":
                material = "purpur_pillar_top" if face == "top" else "purpur_pillar"
            path = f"assets/minecraft/textures/block/{material}.png"
            tile = Image.open(io.BytesIO(archive.read(path))).convert("RGBA").crop((0, 0, 16, 16))
        if "glass" in short:
            tint = (135, 98, 169, 35) if "purple" in short else ((26, 21, 37, 110) if "black" in short else (147, 192, 201, 22))
            base = Image.new("RGBA", (16, 16), tint)
            base.alpha_composite(tile)
            tile = base
        alpha = tile.getchannel("A")
        tile = ImageEnhance.Brightness(tile.convert("RGB")).enhance(shade).convert("RGBA")
        tile.putalpha(alpha)
        return tile

    return texture


def textured_face(canvas, points, tile):
    left = math.floor(min(point[0] for point in points))
    top = math.floor(min(point[1] for point in points))
    right = math.ceil(max(point[0] for point in points)) + 1
    bottom = math.ceil(max(point[1] for point in points)) + 1
    first = points[0]
    first_axis = (points[1][0] - first[0], points[1][1] - first[1])
    second_axis = (points[3][0] - first[0], points[3][1] - first[1])
    determinant = first_axis[0] * second_axis[1] - first_axis[1] * second_axis[0]
    if abs(determinant) < 0.001:
        return
    horizontal_x = 16 * second_axis[1] / determinant
    horizontal_y = -16 * second_axis[0] / determinant
    vertical_x = -16 * first_axis[1] / determinant
    vertical_y = 16 * first_axis[0] / determinant
    offset_x, offset_y = left - first[0], top - first[1]
    patch = tile.transform((right - left, bottom - top), Image.Transform.AFFINE, (horizontal_x, horizontal_y, horizontal_x * offset_x + horizontal_y * offset_y, vertical_x, vertical_y, vertical_x * offset_x + vertical_y * offset_y), Image.Resampling.NEAREST)
    mask = Image.new("L", patch.size)
    ImageDraw.Draw(mask).polygon([(screen_x - left, screen_y - top) for screen_x, screen_y in points], fill=255)
    patch.putalpha(ImageChops.multiply(mask, patch.getchannel("A")))
    canvas.alpha_composite(patch, (left, top))


def render(structure, shapes, texture, cutaway):
    blocks = {}
    for position, block in structure.blocks.items():
        if block == AIR:
            continue
        block_x, height, block_z = position
        if cutaway and (height >= 9 or (block_x == 0 or block_z == 0) and height >= 2):
            continue
        blocks[position] = block
    primitives = []
    for position, block in blocks.items():
        state_key = (block.name, block.properties)
        for bounds in shapes[state_key]:
            low_x, low_y, low_z, high_x, high_y, high_z = bounds
            low_x, high_x = low_x + position[0], high_x + position[0]
            low_y, high_y = low_y + position[1], high_y + position[1]
            low_z, high_z = low_z + position[2], high_z + position[2]
            full = bounds == [0.0, 0.0, 0.0, 1.0, 1.0, 1.0]
            faces = (
                ("west", (position[0] - 1, position[1], position[2]), [(low_x, low_y, low_z), (low_x, low_y, high_z), (low_x, high_y, high_z), (low_x, high_y, low_z)], 0.64),
                ("north", (position[0], position[1], position[2] - 1), [(low_x, low_y, low_z), (high_x, low_y, low_z), (high_x, high_y, low_z), (low_x, high_y, low_z)], 0.83),
                ("top", (position[0], position[1] + 1, position[2]), [(low_x, high_y, low_z), (high_x, high_y, low_z), (high_x, high_y, high_z), (low_x, high_y, high_z)], 1.12),
            )
            for face_name, neighbor, vertices, brightness in faces:
                adjacent = blocks.get(neighbor)
                if full and adjacent is not None:
                    adjacent_shapes = shapes[(adjacent.name, adjacent.properties)]
                    if adjacent_shapes == [[0.0, 0.0, 0.0, 1.0, 1.0, 1.0]] and ("glass" not in adjacent.name or adjacent == block):
                        continue
                center = tuple(sum(vertex[axis] for vertex in vertices) / 4 for axis in range(3))
                depth = center[0] + center[2] - center[1] * 1.15
                primitives.append((depth, vertices, block.name, face_name, 1.3 if block.name == "minecraft:end_rod" else brightness))
    projected = [raw_projection(vertex) for _, vertices, _, _, _ in primitives for vertex in vertices]
    min_x, max_x = min(point[0] for point in projected), max(point[0] for point in projected)
    min_y, max_y = min(point[1] for point in projected), max(point[1] for point in projected)
    scale = min(1640 / (max_x - min_x), 1020 / (max_y - min_y))
    offset_x = WIDTH / 2 - (min_x + max_x) * scale / 2
    offset_y = 720 - (min_y + max_y) * scale / 2
    canvas = background("M5  /  末地观星台", "实际 NBT · 原版纹理 + 原生方块形状软件预览  /  非实机截图" if cutaway else "完整结构外观 · 玻璃星顶保留 · 不以剖切结构交付")
    for _, vertices, name, face_name, brightness in sorted(primitives, key=lambda item: item[0], reverse=True):
        points = [(raw_projection(vertex)[0] * scale + offset_x, raw_projection(vertex)[1] * scale + offset_y) for vertex in vertices]
        textured_face(canvas, points, texture(name, face_name, brightness))
    draw = ImageDraw.Draw(canvas)
    draw.line((76, 1254, WIDTH - 76, 1254), fill=(66, 51, 86), width=2)
    draw.text((78, 1275), "15 × 10 × 15   |   北口 [7,1,0]   |   内点 [7,1,2]   |   无传送门 / 无水晶实体 / 无库存", font=font(21), fill=(193, 180, 209))
    if cutaway:
        draw.text((WIDTH - 560, 112), "仅预览移除北/西上墙与屋顶", font=font(20), fill=(168, 212, 209))
    return canvas.convert("RGB")


def plan(structure, texture):
    canvas = Image.new("RGB", (1040, 1120), (23, 21, 37))
    draw = ImageDraw.Draw(canvas)
    draw.text((60, 25), "M5  /  N ↑  实际 NBT 首层通路", font=font(30), fill=(232, 223, 241))
    cell, margin, top = 60, 70, 110
    for block_z in range(15):
        for block_x in range(15):
            block = structure.get((block_x, 1, block_z))
            if block == AIR:
                block = structure.get((block_x, 0, block_z))
            tile = texture(block.name, "top", 0.9).resize((cell, cell), Image.Resampling.NEAREST).convert("RGB")
            canvas.paste(tile, (margin + block_x * cell, top + block_z * cell))
    static = json.loads((ROOT / "qa/static-qa.json").read_text(encoding="utf-8"))
    for route in static["routes"]:
        points = [(margin + position[0] * cell + cell // 2, top + position[2] * cell + cell // 2) for position in route["steps"]]
        if len(points) > 1:
            draw.line(points, fill=(54, 231, 204), width=5)
        if points:
            screen_x, screen_y = points[-1]
            draw.ellipse((screen_x - 9, screen_y - 9, screen_x + 9, screen_y + 9), fill=(201, 255, 242))
    draw.text((65, 1040), "青线：北入口至安全内点与四侧观测位的干燥往返路", font=font(24), fill=(172, 222, 213))
    canvas.save(ROOT / "evidence/plan-dry-routes.png")


def main():
    structure = Structure.read(OUT / "room.nbt")
    native = json.loads((ROOT / "qa/native-registry-qa.json").read_text(encoding="utf-8"))
    if native["sha256"] != digest(OUT / "room.nbt"):
        raise ValueError("Native shape evidence is stale")
    shapes = {(entry["name"], tuple(sorted(entry["properties"].items()))): entry["boxes"] for entry in native["native_shapes"]}
    texture = texture_loader()
    render(structure, shapes, texture, True).save(OUT / "preview.png")
    render(structure, shapes, texture, False).save(ROOT / "evidence/preview-full-envelope.png")
    plan(structure, texture)
    dump(ROOT / "qa/preview-provenance.json", {"status": "PASS", "source_nbt": str(OUT / "room.nbt"), "sha256": digest(OUT / "room.nbt"), "preview_sha256": digest(OUT / "preview.png"), "source": "Actual delivered NBT, native Minecraft block-shape AABBs and installed vanilla textures; textured isometric software rendering, not a game screenshot.", "cutaway_changes_nbt": False, "full_roof_view": str(ROOT / "evidence/preview-full-envelope.png"), "dry_route_plan": str(ROOT / "evidence/plan-dry-routes.png"), "limits": "Native outline AABBs approximate the geometry of crossed/transparent models. Software painter ordering is not the Minecraft rendering pipeline."})
    print("Actual-NBT preview, full envelope and route plan rendered.")


if __name__ == "__main__":
    main()
