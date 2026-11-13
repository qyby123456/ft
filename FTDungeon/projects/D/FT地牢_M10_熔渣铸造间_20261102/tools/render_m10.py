from __future__ import annotations

import json
import zipfile

from PIL import Image, ImageDraw

import mc_models as engine
from build_m10 import ENTRANCE, INTERIOR, ROOT, SIZE, digest, write_json
from nbtio import Structure


BACKGROUND = "#15171d"
TEXT = "#f1e4d1"
MUTED = "#b6b3ad"
ACCENT = "#f5a249"
SAFE = "#74d1b6"
ORIGINAL_SYNTHETIC = engine.synthetic_model
engine.PARTIAL.update({"glass", "lava", "fire", "lantern", "anvil", "grindstone", "lever", "tripwire_hook", "rail"})


def synthetic(name, properties):
    if name == "lava":
        element = {"from": [0, 0, 0], "to": [16, 16, 16], "faces": {face: {"texture": "block/lava_still", "uv": [0, 0, 16, 16]} for face in engine.FACE_STEPS}}
        return {"textures": {}, "elements": [element]}, {}
    return ORIGINAL_SYNTHETIC(name, properties)


engine.synthetic_model = synthetic


def rotated(blocks, turns):
    results = {}
    directions = ["north", "east", "south", "west"]
    for position, block in blocks.items():
        pos_x, pos_y, pos_z = position
        properties = dict(block.properties)
        for turn in range(turns):
            pos_x, pos_z = SIZE[0] - 1 - pos_z, pos_x
        if properties.get("facing") in directions:
            properties["facing"] = directions[(directions.index(properties["facing"]) + turns) % 4]
        originals = dict(properties)
        for index, direction in enumerate(directions):
            if direction in originals:
                properties[directions[(index + turns) % 4]] = originals[direction]
        if properties.get("axis") in ("x", "z") and turns % 2:
            properties["axis"] = "z" if properties["axis"] == "x" else "x"
        if properties.get("shape") in ("north_south", "east_west") and turns % 2:
            properties["shape"] = "east_west" if properties["shape"] == "north_south" else "north_south"
        results[(pos_x, pos_y, pos_z)] = {"Name": block.name, "Properties": properties}
    return results


def render(blocks, turns=0, size=(1600, 1250), exterior=False):
    visible = {}
    for position, block in rotated(blocks, turns).items():
        pos_x, pos_y, pos_z = position
        if block["Name"] == "minecraft:air":
            continue
        if not exterior and (pos_y >= 9 or pos_y >= 2 and (pos_x == 0 or pos_z == 0)):
            continue
        visible[position] = block
    projected = [(position[0] - position[2], -(position[0] + position[2]) * 0.5 - position[1] * 1.05) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1, max(point[1] for point in projected) + 1
    scale = min((size[0] - 45) / (high_x - low_x), (size[1] - 125) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 32) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(point):
        pos_x, pos_y, pos_z = point
        return offset_x + (pos_x - pos_z) * scale, offset_y - (pos_x + pos_z) * scale * 0.5 - pos_y * scale * 1.05

    faces = []
    for position, block in visible.items():
        faces.extend(engine.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 0.95238 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        if any(token in identifier for token in ("lava", "fire_", "lantern", "shroomlight")):
            brightness = max(brightness, 1.3)
        engine.parallelogram(canvas, engine.texture(identifier, uv, rotation, brightness), [project(point) for point in points])
    draw = ImageDraw.Draw(canvas)
    label = "完整屋顶 · 北入口外观" if exterior else ("北 / 西侧剖面", "东 / 北侧剖面", "南 / 东侧剖面", "西 / 南侧剖面")[turns]
    draw.text((26, 16), label, font=engine.font(25), fill=ACCENT)
    draw.text((26, size[1] - 42), "实际 NBT 软件渲染 · 非游戏截图", font=engine.font(21), fill=MUTED)
    return canvas.convert("RGB")


def floorplan(structure):
    canvas = Image.new("RGB", (550, 640), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((20, 15), "北 -Z ↑ / 玩家脚位 y=2", font=engine.font(24), fill=SAFE)
    scale = 29
    for pos_x in range(SIZE[0]):
        for pos_z in range(SIZE[2]):
            block = structure.get((pos_x, 2, pos_z)).name.removeprefix("minecraft:")
            color = "#756c61" if block == "air" else "#444953"
            if block == "rail":
                color = "#a99b7d"
            if block == "lava":
                color = "#ee7d2c"
            if 7 <= pos_x <= 9 and 8 <= pos_z <= 9:
                color = "#9aa1aa"
            if pos_x in (4, 5, 11, 12) and pos_z == 14:
                color = "#f3a654"
            left, top = 25 + pos_x * scale, 65 + pos_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=color)
    draw.line([(25 + 8.5 * scale, 65 + depth * scale) for depth in (0, 6.6)], fill=SAFE, width=5)
    for pos_x, pos_y, pos_z in (ENTRANCE, INTERIOR):
        center_x, center_y = 25 + (pos_x + 0.5) * scale, 65 + (pos_z + 0.5) * scale
        draw.ellipse((center_x - 7, center_y - 7, center_x + 7, center_y + 7), fill=SAFE)
    draw.text((20, 579), "橙：密封热源  灰：铁砧石台", font=engine.font(21), fill=MUTED)
    draw.text((20, 611), "青：入口与安全点  外围可环行", font=engine.font(21), fill=MUTED)
    return canvas


def heat_sections(structure):
    canvas = Image.new("RGB", (1400, 660), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    colors = {"air": "#222730", "lava": "#ef8b28", "fire": "#ffb946", "glass": "#69c2cf", "netherrack": "#8e4038"}
    panels = (("熔槽横剖面 z=8 / 每个热源六面封闭", [(pos_x, pos_y, 8) for pos_y in range(6) for pos_x in range(6)], 6, 6, 40),
              ("左炉纵剖面 x=4 / 炉窗与永燃基座", [(4, pos_y, pos_z) for pos_y in range(7) for pos_z in range(11, 17)], 6, 7, 740))
    for title, positions, width, height, left in panels:
        draw.text((left, 26), title, font=engine.font(25), fill=TEXT)
        for index, position in enumerate(positions):
            column, row = index % width, height - 1 - index // width
            name = structure.get(position).name.removeprefix("minecraft:")
            start_x, start_y = left + column * 85, 90 + row * 66
            draw.rectangle((start_x, start_y, start_x + 81, start_y + 62), fill=colors.get(name, "#59616d"))
            label = {"glass": "玻璃", "lava": "熔岩", "fire": "火", "netherrack": "地狱岩", "air": "空气"}.get(name, "石/铁")
            draw.text((start_x + 7, start_y + 19), label, font=engine.font(17), fill="#0b1117" if name in colors and name != "air" else TEXT)
    draw.text((40, 597), "来自实际 NBT；蓝色是有完整碰撞的玻璃，不是玻璃板。剖切不更改结构。", font=engine.font(24), fill=MUTED)
    canvas.save(ROOT / "qa/heat-sections.png")


def main():
    structure = Structure.read(ROOT / "room.nbt")
    fingerprint = digest(ROOT / "room.nbt")
    with zipfile.ZipFile(engine.CLIENT) as archive:
        engine.ASSETS = archive
        hero = render(structure.blocks)
        hero.save(ROOT / "qa/cutaway.png")
        four = Image.new("RGB", (2400, 2000), BACKGROUND)
        for turns in range(4):
            image = render(structure.blocks, turns, (1200, 1000))
            image.save(ROOT / f"qa/view-{turns}.png")
            four.paste(image, ((turns % 2) * 1200, (turns // 2) * 1000))
        four.save(ROOT / "qa/four-views.png")
        render(structure.blocks, 0, (1400, 1200), True).save(ROOT / "qa/exterior.png")
    floor = floorplan(structure)
    floor.save(ROOT / "qa/floorplan.png")
    heat_sections(structure)
    canvas = Image.new("RGB", (1536, 1024), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((38, 22), "M10 / 熔渣铸造间", font=engine.font(39), fill=TEXT)
    draw.text((40, 78), "17×11×17  ·  双炉 / 铁砧 / 密封熔槽  ·  完整耐火屋顶", font=engine.font(23), fill=MUTED)
    hero = hero.crop((0, 55, hero.width, hero.height - 50))
    hero.thumbnail((1110, 800), Image.Resampling.LANCZOS)
    canvas.paste(hero, (10 + (1110 - hero.width) // 2, 133 + (800 - hero.height) // 2))
    floor.thumbnail((350, 407), Image.Resampling.LANCZOS)
    canvas.paste(floor, (1154, 132))
    labels = ("双铸造炉 · 四格密封火膛", "六格熔岩 · 玻璃完整封盖", "石台铁砧 · 双磨石 · 工具墙", "原矿堆 · 侧轨 · 三盏吊灯", "入口脚位 (8, 2, 0)", "安全内点 (8, 2, 4)")
    for index, label in enumerate(labels):
        draw.text((1152, 572 + index * 47), label, font=engine.font(21), fill=TEXT if index < 4 else SAFE)
    draw.line((38, 948, 1498, 948), fill="#4e4640", width=2)
    draw.text((40, 969), "实际 NBT 软件预览 / 屋顶仅为展示剖切 / 非游戏截图 / 无奖励库存 / 未部署", font=engine.font(23), fill=MUTED)
    canvas.save(ROOT / "preview.png")
    assert fingerprint == digest(ROOT / "room.nbt")
    report = {"status": "PASS", "source": str(ROOT / "room.nbt"), "structure_sha256": fingerprint,
              "preview_sha256": digest(ROOT / "preview.png"), "preview_size": [1536, 1024],
              "source_assets": str(engine.CLIENT), "source_assets_sha256": digest(engine.CLIENT),
              "used_models": sorted(engine.USED_MODELS), "used_textures": sorted(engine.USED_TEXTURES),
              "views": 4, "roof_unchanged": True,
              "scope": "CPU raster using actual Minecraft 1.20.1 block models and textures; fluid geometry is a full source-cell approximation with its first texture frame; no baked game lighting, not a game screenshot"}
    write_json(ROOT / "qa/render.json", report)
    print(json.dumps({"status": "PASS", "preview": str(ROOT / "preview.png"), "models": len(engine.USED_MODELS), "textures": len(engine.USED_TEXTURES)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
