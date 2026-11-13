from __future__ import annotations

import functools
import importlib.util
import json
import math
import zipfile

from PIL import Image, ImageChops, ImageDraw, ImageFont

from build_r6 import ROOT, Structure, hash_file, write_json

CLIENT = ROOT.home() / ".gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar"
BACKGROUND = "#111c1a"
TEXT = "#ecede1"
MUTED = "#a5b9ad"


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def initialize():
    source = ROOT / "tools/vendor/asset_faces.py"
    specification = importlib.util.spec_from_file_location("r6_faces", source)
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    renderer.ASSETS = zipfile.ZipFile(CLIENT)
    renderer.PARTIAL |= {"glass", "oak_leaves", "flowering_azalea_leaves", "fern", "oxeye_daisy", "azure_bluet", "blue_orchid", "flowering_azalea", "carrots", "wheat", "spruce_trapdoor", "lantern", "water", "farmland", "cave_vines", "cave_vines_plant", "potted_fern", "potted_dead_bush", "potted_brown_mushroom", "composter"}
    base_synthetic = renderer.synthetic_model

    def synthetic(name, properties):
        if name == "water":
            surfaces = {face: {"texture": "block/water_still", "uv": [0, 0, 16, 16]} for face in renderer.FACE_STEPS}
            return {"textures": {}, "elements": [{"from": [0, 0, 0], "to": [16, 14, 16], "faces": surfaces}]}, {}
        return base_synthetic(name, properties)

    renderer.synthetic_model = synthetic
    return renderer


def render(renderer, structure, size, cutaway=False):
    visible = {}
    for position, block in structure.blocks.items():
        block_x, block_y, block_z = position
        if block.name == "minecraft:air" or cutaway and (block_y >= 9 or (block_x == 0 or block_z == 0) and block_y >= 1):
            continue
        visible[position] = {"Name": block.name, "Properties": dict(block.properties)}
    projected = [(position[0] - position[2], -(position[0] + position[2]) / 2 - position[1]) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 2, max(point[1] for point in projected) + 1
    scale = min((size[0] - 36) / (high_x - low_x), (size[1] - 36) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = size[1] / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(point):
        block_x, block_y, block_z = point
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale / 2 - block_y * scale

    @functools.lru_cache(maxsize=None)
    def texture(identifier, uv, rotation, brightness):
        result = renderer.texture(identifier, uv, rotation, brightness)
        tint = (140, 187, 81) if identifier in {"block/oak_leaves", "block/fern"} else (64, 119, 220) if identifier == "block/water_still" else None
        if tint:
            alpha = result.getchannel("A")
            result = ImageChops.multiply(result, Image.new("RGBA", result.size, (*tint, 255)))
            result.putalpha(alpha if identifier != "block/water_still" else alpha.point(lambda value: int(value * 0.78)))
        return result

    faces = []
    for position, block in visible.items():
        faces.extend(renderer.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        polygon = [project(point) for point in points]
        area = sum(polygon[index][0] * polygon[(index + 1) % 4][1] - polygon[(index + 1) % 4][0] * polygon[index][1] for index in range(4))
        if abs(area) > 0.001:
            renderer.parallelogram(canvas, texture(identifier, uv, rotation, brightness), polygon)
    return canvas.convert("RGB")


def plan(structure, reachable, size):
    canvas = Image.new("RGB", size, "#192923")
    draw = ImageDraw.Draw(canvas)
    cell = min((size[0] - 26) // 15, (size[1] - 56) // 17)
    start_x = (size[0] - cell * 15) // 2
    start_y = 34
    draw.text((15, 7), "N ↑   黄色：入口  /  青色：干燥可达", font=font(15), fill=TEXT)
    for block_x in range(15):
        for block_z in range(17):
            material = structure.get((block_x, 1, block_z)).name
            color = "#65705d" if material != "minecraft:air" else "#26352f"
            if (block_x, 1, block_z) in reachable:
                color = "#6cbda3"
            if material == "minecraft:water":
                color = "#4681aa"
            if block_z == 0 and 6 <= block_x <= 8:
                color = "#dfbd6f"
            left, top = start_x + block_x * cell, start_y + block_z * cell
            draw.rectangle((left, top, left + cell - 2, top + cell - 2), fill=color)
    left, top = start_x + 7 * cell, start_y + 10 * cell
    draw.ellipse((left + 3, top + 3, left + cell - 5, top + cell - 5), fill="white")
    return canvas


def main():
    structure = Structure.read(ROOT / "room.nbt")
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    static = json.loads((ROOT / "qa/static.json").read_text(encoding="utf-8"))
    assert hash_file(ROOT / "room.nbt") == metadata["sha256"]
    renderer = initialize()
    canvas = Image.new("RGB", (1800, 1200), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((35, 21), "R6  /  衰败玻璃温室", font=font(47), fill=TEXT)
    draw.text((37, 87), "实际 NBT · 原版 1.20.1 模型与纹理 · 15×10×17 (X×Y×Z) · 非 AI 图 / 非游戏截图", font=font(23), fill=MUTED)
    draw.text((37, 136), "内部剖面：仅预览隐藏屋顶、北墙与西墙；最终模板保持完整玻璃外壳", font=font(23), fill="#b8d78c")
    canvas.paste(render(renderer, structure, (1260, 800), cutaway=True), (10, 184))
    draw.text((1298, 146), "完整外壳 / 连续玻璃顶", font=font(24), fill=TEXT)
    canvas.paste(render(renderer, structure, (472, 402)), (1295, 184))
    draw.text((1298, 615), "干燥通路 / 安全内点", font=font(24), fill=TEXT)
    reachable = {tuple(position) for position in static["reachable"]}
    canvas.paste(plan(structure, reachable, (472, 430)), (1295, 665))
    draw.text((37, 1010), "北入口 (7,1,0) → 安全点 (7,1,10)；中央三格干路，两侧密封灌溉种植床。", font=font(26), fill=TEXT)
    draw.text((37, 1064), "玻璃格栅顶 / 苔石基座 / 古树树池 / 育苗台 / 堆肥桶 / 双吊篮与有锚吊灯。", font=font(24), fill=MUTED)
    draw.text((37, 1116), "叶片持久化、成熟作物、定长垂藤；零实体、零方块实体、零自动奖励。", font=font(24), fill=MUTED)
    canvas.save(ROOT / "preview.png")
    render(renderer, structure, (1600, 1200), cutaway=True).save(ROOT / "qa/cutaway.png")
    render(renderer, structure, (1200, 1000)).save(ROOT / "qa/full-shell.png")
    with Image.open(ROOT / "preview.png") as verification:
        assert verification.size == (1800, 1200)
        verification.verify()
    report = {"status": "PASS", "source_structure_sha256": metadata["sha256"], "preview_sha256": hash_file(ROOT / "preview.png"), "size": [1800, 1200], "method": "Final NBT reparsed into block faces from local vanilla client blockstates/models/textures; fixed plains foliage tint, simplified source-water surfaces; preview-only cutaway.", "client_asset_jar": str(CLIENT), "renderer_sha256": hash_file(ROOT / "tools/vendor/asset_faces.py"), "models": sorted(renderer.USED_MODELS), "textures": sorted(renderer.USED_TEXTURES), "limits": ["Software isometric preview, not an in-game screenshot", "Fixed foliage tint, first texture animation frame, simplified fluid surface, no runtime biome lighting or shaders"]}
    write_json(ROOT / "qa/preview.json", report)
    renderer.ASSETS.close()
    print(json.dumps({"preview": str(ROOT / "preview.png"), "models": len(report["models"]), "textures": len(report["textures"])}))


if __name__ == "__main__":
    main()
