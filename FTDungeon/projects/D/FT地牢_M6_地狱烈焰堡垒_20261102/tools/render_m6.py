from __future__ import annotations

import json
import math
import zipfile

from PIL import Image, ImageDraw

import mc_models as engine
from build_m6 import DELIVERY, ROOT, SIZE, digest, write_json
from nbtio import Structure


BACKGROUND = "#14131a"
TEXT = "#ede2d0"
MUTED = "#b7aca9"
GOLD = "#e5b955"
TEAL = "#60dedc"
ORIGINAL_SYNTHETIC = engine.synthetic_model
engine.PARTIAL.update({"glass", "lava", "soul_fire", "bell", "warped_fence", "potted_crimson_roots", "polished_blackstone_brick_wall"})


def synthetic(name, properties):
    if name in ("lava", "bell"):
        texture = "block/lava_still" if name == "lava" else "block/gold_block"
        shapes = [([0, 0, 0], [16, 16, 16])] if name == "lava" else [([4, 2, 4], [12, 10, 12]), ([2, 1, 2], [14, 4, 14]), ([7, 10, 7], [9, 16, 9])]
        elements = [{"from": start, "to": end, "faces": {face: {"texture": texture, "uv": [0, 0, 16, 16]} for face in engine.FACE_STEPS}} for start, end in shapes]
        return {"textures": {}, "elements": elements}, {}
    return ORIGINAL_SYNTHETIC(name, properties)


engine.synthetic_model = synthetic


def rotated(blocks, turns):
    results = {}
    names = ["north", "east", "south", "west"]
    for position, block in blocks.items():
        pos_x, pos_y, pos_z = position
        properties = dict(block.properties)
        for rotation in range(turns):
            pos_x, pos_z = 20 - pos_z, pos_x
        if "facing" in properties and properties["facing"] in names:
            properties["facing"] = names[(names.index(properties["facing"]) + turns) % 4]
        originals = dict(properties)
        for index, direction in enumerate(names):
            if direction in originals:
                properties[names[(index + turns) % 4]] = originals[direction]
        if properties.get("axis") in ("x", "z") and turns % 2:
            properties["axis"] = "z" if properties["axis"] == "x" else "x"
        if "rotation" in properties:
            properties["rotation"] = str((int(properties["rotation"]) + turns * 4) % 16)
        results[(pos_x, pos_y, pos_z)] = {"Name": block.name, "Properties": properties}
    return results


def render(blocks, turns=0, size=(1500, 1200), exterior=False):
    all_blocks = rotated(blocks, turns)
    visible = {}
    for position, block in all_blocks.items():
        pos_x, pos_y, pos_z = position
        if block["Name"] == "minecraft:air":
            continue
        if not exterior and (pos_y >= 10 or pos_y >= 2 and (pos_x == 0 or pos_z == 0)):
            continue
        visible[position] = block
    projected = [(position[0] - position[2], -(position[0] + position[2]) * 0.72 - position[1] * 0.85) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1, max(point[1] for point in projected) + 1
    scale = min((size[0] - 40) / (high_x - low_x), (size[1] - 135) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 45) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(point):
        pos_x, pos_y, pos_z = point
        return offset_x + (pos_x - pos_z) * scale, offset_y - (pos_x + pos_z) * scale * 0.72 - pos_y * scale * 0.85

    faces = []
    for position, block in visible.items():
        faces.extend(engine.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 1.6941 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        engine.parallelogram(canvas, engine.texture(identifier, uv, rotation, brightness * 0.91), [project(point) for point in points])
    draw = ImageDraw.Draw(canvas)
    labels = ("北 / 西侧剖面", "东 / 北侧剖面", "南 / 东侧剖面", "西 / 南侧剖面")
    draw.text((28, 18), "完整屋顶外观" if exterior else labels[turns], font=engine.font(26), fill=GOLD)
    draw.text((28, size[1] - 45), "实际 NBT 软件渲染 · 非游戏截图", font=engine.font(21), fill=MUTED)
    return canvas.convert("RGB")


def plan(blocks):
    canvas = Image.new("RGB", (630, 690), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((20, 12), "北 -Z ↑  /  三格接口", font=engine.font(24), fill=TEAL)
    scale = 28
    for pos_x in range(21):
        for pos_z in range(21):
            block = blocks[(pos_x, 2, pos_z)]
            floor = blocks[(pos_x, 1, pos_z)]
            color = "#5d5759"
            name = block.name.removeprefix("minecraft:")
            if name not in ("air", "glass"):
                color = "#47323b"
            if floor.name == "minecraft:lava":
                color = "#eb7d31"
            if 8 <= pos_x <= 12 and 9 <= pos_z <= 13:
                color = "#b18347"
            if name.startswith("warped_"):
                color = "#459c94"
            left, top = 20 + pos_x * scale, 60 + pos_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=color)
    draw.line([(20 + 10.5 * scale, 60 + position * scale) for position in (0, 7)], fill=TEAL, width=5)
    for pos_x, pos_z in ((10, 0), (10, 4)):
        center_x, center_y = 20 + (pos_x + 0.5) * scale, 60 + (pos_z + 0.5) * scale
        draw.ellipse((center_x - 7, center_y - 7, center_x + 7, center_y + 7), fill=TEAL)
    draw.text((20, 651), "橙：封闭熔岩  青：桥与北通路", font=engine.font(22), fill=MUTED)
    return canvas


def main():
    structure = Structure.read(DELIVERY / "room.nbt")
    before = digest(DELIVERY / "room.nbt")
    with zipfile.ZipFile(engine.CLIENT) as archive:
        engine.ASSETS = archive
        hero = render(structure.blocks, 2, (1800, 1400))
        hero.save(ROOT / "qa/cutaway.png")
        four = Image.new("RGB", (2400, 2000), BACKGROUND)
        for turns in range(4):
            image = render(structure.blocks, turns, (1200, 1000))
            image.save(ROOT / f"qa/view-{turns}.png")
            four.paste(image, ((turns % 2) * 1200, (turns // 2) * 1000))
        four.save(ROOT / "qa/four-views.png")
        render(structure.blocks, 0, (1500, 1200), True).save(ROOT / "qa/exterior.png")
    floor = plan(structure.blocks)
    floor.save(ROOT / "qa/floorplan.png")
    canvas = Image.new("RGB", (1536, 1024), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((38, 22), "M6 / 地狱烈焰堡垒", font=engine.font(38), fill=TEXT)
    draw.text((40, 75), "21×21 格主厅  ·  模板 21×12×21  ·  完整屋顶", font=engine.font(23), fill=MUTED)
    hero = hero.crop((0, 60, hero.width, hero.height - 60))
    hero.thumbnail((1160, 850), Image.Resampling.LANCZOS)
    canvas.paste(hero, (5 + (1160 - hero.width) // 2, 125 + (850 - hero.height) // 2))
    floor.thumbnail((320, 350), Image.Resampling.LANCZOS)
    canvas.paste(floor, (1180, 140))
    for index, label in enumerate(("双瀑布 + 玻璃封闭承池", "灵魂火战祭坛 + 金石基座", "四柱绯红拱梁 + 完整屋顶", "锁链吊笼 + 六盏实际灯具", "原生金钟 + 骨饰与根须", "不可燃诡异木桥 + 干燥环路")):
        draw.text((1180, 520 + index * 49), label, font=engine.font(21), fill=TEXT if index < 3 else MUTED)
    draw.text((1180, 846), "入口脚位 (10, 2, 0)", font=engine.font(21), fill=TEAL)
    draw.text((1180, 881), "安全内点 (10, 2, 4)", font=engine.font(21), fill=TEAL)
    draw.line((38, 953, 1498, 953), fill="#493b40", width=2)
    draw.text((40, 973), "实际 NBT 软件预览 / 仅展示剖切 / 非游戏截图 / 无奖励库存 / 未部署", font=engine.font(23), fill=MUTED)
    canvas.save(DELIVERY / "preview.png")
    assert before == digest(DELIVERY / "room.nbt")
    write_json(ROOT / "qa/render.json", {"status": "PASS", "source": str(DELIVERY / "room.nbt"), "structure_sha256": before, "preview_sha256": digest(DELIVERY / "preview.png"), "preview_size": [1536, 1024], "source_textures": str(engine.CLIENT), "models": len(engine.USED_MODELS), "textures": len(engine.USED_TEXTURES), "views": 4, "complete_roof_unchanged": True, "scope": "CPU raster of actual Minecraft block models; lava uses actual first texture frame, bell uses simplified gold shape; not a game screenshot"})
    print(json.dumps({"render": "PASS", "preview": str(DELIVERY / "preview.png"), "models": len(engine.USED_MODELS), "textures": len(engine.USED_TEXTURES)}))


if __name__ == "__main__":
    main()
