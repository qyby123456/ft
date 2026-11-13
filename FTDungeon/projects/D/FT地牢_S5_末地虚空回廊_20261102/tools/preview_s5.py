from __future__ import annotations

import hashlib
import importlib.util
import math
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
RENDERER_SOURCE = ROOT / "tools/vendor"
CLIENT = Path("C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar")
BACKGROUND = "#100d1d"
PANEL = "#1b172a"
TEXT = "#eee9f6"
MUTED = "#b5abc7"


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def initialize_renderer():
    sys.dont_write_bytecode = True
    sys.path.insert(0, str(RENDERER_SOURCE))
    specification = importlib.util.spec_from_file_location("s5_asset_renderer", RENDERER_SOURCE / "asset_faces.py")
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    renderer.ASSETS = zipfile.ZipFile(CLIENT)
    renderer.PARTIAL |= {"end_rod", "chorus_flower", "end_portal_frame", "purple_stained_glass", "tinted_glass"}
    return renderer


def render(renderer, blocks, size, cutaway=False, underdeck=False):
    visible = {}
    for position, block in blocks.items():
        if block["Name"] == "minecraft:air":
            continue
        block_x, block_y, block_z = position
        if cutaway and (block_y >= 7 or block_x == 0 and block_y >= 3 or block_z == 0 and block_y >= 3):
            continue
        if underdeck and block_y >= 2:
            continue
        visible[position] = block
    projected = [(position[0] - position[2], -(position[0] + position[2]) / 2 - position[1]) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 2, max(point[1] for point in projected) + 1
    scale = min((size[0] - 24) / (high_x - low_x), (size[1] - 24) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = size[1] / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(point):
        block_x, block_y, block_z = point
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale / 2 - block_y * scale

    faces = []
    for position, block in visible.items():
        faces.extend(renderer.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        polygon = [project(point) for point in points]
        if abs(sum(polygon[index][0] * polygon[(index + 1) % 4][1] - polygon[(index + 1) % 4][0] * polygon[index][1] for index in range(4))) < 0.001:
            continue
        renderer.parallelogram(canvas, renderer.texture(identifier, uv, rotation, brightness), polygon)
    return canvas.convert("RGB")


def plan(structure, reachable, size):
    canvas = Image.new("RGB", size, PANEL)
    draw = ImageDraw.Draw(canvas)
    cell = min((size[0] - 28) // 7, (size[1] - 42) // 13)
    origin_x = (size[0] - cell * 7) // 2
    origin_y = 30
    draw.text((12, 5), "N ↑   北侧 3×3 接口", font=font(16), fill=MUTED)
    for block_z in range(13):
        for block_x in range(7):
            floor = structure.get((block_x, 2, block_z)).name
            fill = "#74618e"
            if (block_x, 3, block_z) in reachable:
                fill = "#74cfcc" if floor != "minecraft:tinted_glass" else "#39515d"
            if block_z == 0 and block_x in (2, 3, 4):
                fill = "#ecd394"
            left, top = origin_x + block_x * cell, origin_y + block_z * cell
            draw.rectangle((left, top, left + cell - 2, top + cell - 2), fill=fill)
    left, top = origin_x + 3 * cell, origin_y + 10 * cell
    draw.ellipse((left + 3, top + 3, left + cell - 5, top + cell - 5), fill="#f7f7f2")
    return canvas


def make_preview(structure, reachable, destination):
    renderer = initialize_renderer()
    blocks = {position: {"Name": block.name, "Properties": dict(block.properties)} for position, block in structure.blocks.items()}
    canvas = Image.new("RGB", (1536, 1024), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((28, 17), "S5 / 末地虚空回廊", font=font(38), fill=TEXT)
    draw.text((30, 73), "实际 NBT · 原版 1.20.1 模型与纹理 · 7×8×13 (X×Y×Z) · 非游戏截图", font=font(21), fill=MUTED)
    canvas.paste(render(renderer, blocks, (1090, 720), cutaway=True), (5, 132))
    draw.text((30, 118), "剖面：仅预览移除屋顶 / 北面 / 近侧外墙", font=font(20), fill="#d3c5ee")
    draw.text((1120, 116), "完整封护与天窗", font=font(20), fill=TEXT)
    canvas.paste(render(renderer, blocks, (394, 244)), (1116, 148))
    draw.text((1120, 396), "落地底座与紫珀承梁", font=font(20), fill=TEXT)
    canvas.paste(render(renderer, blocks, (394, 165), underdeck=True), (1116, 428))
    draw.text((1120, 597), "三格连续通路 / 36 格可达", font=font(19), fill=TEXT)
    canvas.paste(plan(structure, reachable, (394, 293)), (1116, 627))
    draw.text((30, 870), "北入口 (3,3,0) → 安全内点 (3,3,10)，坐标均为玩家脚部。", font=font(23), fill=TEXT)
    draw.text((30, 913), "玻璃封住裂隙；黑曜石基座＋紫珀承梁，六根末地烛均有完整支撑。", font=font(22), fill=MUTED)
    draw.text((30, 954), "零奖励 / 零流体 / 零实体；末地门残片无眼，不构成可激活门环。", font=font(22), fill=MUTED)
    canvas.save(destination)
    with Image.open(destination) as verification:
        assert verification.size == (1536, 1024)
        verification.verify()
    result = {
        "path": str(destination), "size": [1536, 1024],
        "source": "Reparsed final room.nbt rendered with cached vanilla 1.20.1 blockstates, models, textures. Cutaway only changes preview visibility. Not an AI image or gameplay screenshot.",
        "renderer_path": str(RENDERER_SOURCE / "asset_faces.py"),
        "renderer_sha256": hashlib.sha256((RENDERER_SOURCE / "asset_faces.py").read_bytes()).hexdigest(),
        "client_asset_jar": str(CLIENT),
        "models": sorted(renderer.USED_MODELS), "textures": sorted(renderer.USED_TEXTURES),
    }
    renderer.ASSETS.close()
    return result
