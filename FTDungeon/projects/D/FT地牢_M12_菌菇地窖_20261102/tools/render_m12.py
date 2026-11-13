from __future__ import annotations

import json
import zipfile

from PIL import Image, ImageDraw, ImageOps

import mc_models as engine
from build_m12 import ENTRANCE, INTERIOR, ROOT, digest, write_json
from nbtio import Structure


BACKGROUND = "#131b19"
TEXT = "#e7e6d7"
MUTED = "#98b7a4"
GOLD = "#edc477"
engine.PARTIAL.update({"water", "red_mushroom", "brown_mushroom", "spore_blossom", "hanging_roots", "glow_lichen", "lantern", "ladder", "potted_red_mushroom", "potted_brown_mushroom"})
ORIGINAL_SYNTHETIC = engine.synthetic_model
ORIGINAL_TEXTURE = engine.texture


def synthetic(name, properties):
    if name == "water":
        faces = {face: {"texture": "block/water_still", "uv": [0, 0, 16, 16], "cullface": face} for face in engine.FACE_STEPS}
        return {"textures": {}, "elements": [{"from": [0, 0, 0], "to": [16, 14, 16], "faces": faces}]}, {}
    return ORIGINAL_SYNTHETIC(name, properties)


def tinted_texture(identifier, uv, rotation, brightness):
    image = ORIGINAL_TEXTURE(identifier, uv, rotation, brightness)
    if identifier == "block/water_still":
        tinted = ImageOps.colorize(image.convert("L"), "#163746", "#4cabc1").convert("RGBA")
        tinted.putalpha(image.getchannel("A"))
        return tinted
    return image


engine.synthetic_model = synthetic
engine.texture = tinted_texture


def rotate_blocks(blocks, turns):
    names = ["north", "east", "south", "west"]
    result = {}
    for position, block in blocks.items():
        pos_x, pos_y, pos_z = position
        properties = dict(block.properties)
        for turn in range(turns):
            pos_x, pos_z = 16 - pos_z, pos_x
        if properties.get("facing") in names:
            properties["facing"] = names[(names.index(properties["facing"]) + turns) % 4]
        original = dict(properties)
        for index, direction in enumerate(names):
            if direction in original:
                properties[names[(index + turns) % 4]] = original[direction]
        if properties.get("axis") in ("x", "z") and turns % 2:
            properties["axis"] = "z" if properties["axis"] == "x" else "x"
        result[(pos_x, pos_y, pos_z)] = {"Name": block.name, "Properties": properties}
    return result


def render(blocks, turns=0, size=(1536, 1250), exterior=False, hero=False):
    rotated = rotate_blocks(blocks, turns)
    visible = {}
    for position, block in rotated.items():
        pos_x, pos_y, pos_z = position
        if block["Name"] == "minecraft:air":
            continue
        if not exterior and (pos_y == 10 or pos_y >= 2 and (pos_x == 0 or pos_z == 0)):
            continue
        visible[position] = block
    def screen(point):
        pos_x, pos_y, pos_z = point
        if hero:
            return pos_x * 0.94 - pos_z * 0.342, -(pos_x * 0.342 + pos_z * 0.94) * 0.25 - pos_y
        return pos_x - pos_z, -(pos_x + pos_z) * 0.72 - pos_y * 0.85
    projected = [screen(position) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1, max(point[1] for point in projected) + 1
    scale = min((size[0] - 60) / (high_x - low_x), (size[1] - 180) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 80) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)
    def project(point):
        screen_x, screen_y = screen(point)
        return offset_x + screen_x * scale, offset_y + screen_y * scale
    faces = []
    for position, block in visible.items():
        faces.extend(engine.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] * 0.342 + point[2] * 0.94 - point[1] * 0.25 if hero else point[0] + point[2] - point[1] * 1.6941 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        engine.parallelogram(canvas, engine.texture(identifier, uv, rotation, brightness * 0.95), [project(point) for point in points])
    draw = ImageDraw.Draw(canvas)
    draw.text((30, 20), "M12 菌菇地窖  |  " + ("完整屋顶外观" if exterior else ["北 / 西剖面", "东 / 北剖面", "南 / 东剖面", "西 / 南剖面"][turns]), font=engine.font(30), fill=GOLD)
    draw.text((30, 64), "17 × 11 × 17  ·  双巨菇 / 采集台 / 培养架 / 密封水洼", font=engine.font(21), fill=TEXT)
    draw.text((30, size[1] - 63), "实际 NBT + 原版 1.20.1 模型/纹理软件渲染，非游戏截图", font=engine.font(21), fill=MUTED)
    draw.text((30, size[1] - 34), "剖切仅用于预览；交付 NBT 保留完整顶盖与四面墙。", font=engine.font(18), fill=MUTED)
    return canvas.convert("RGB")


def floorplan(blocks):
    canvas = Image.new("RGB", (850, 920), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((25, 18), "M12 北 -Z ↑  |  脚位 y=2 平面", font=engine.font(27), fill=GOLD)
    scale = 44
    for pos_x in range(17):
        for pos_z in range(17):
            floor = blocks[(pos_x, 1, pos_z)].name
            feet = blocks[(pos_x, 2, pos_z)].name
            head = blocks[(pos_x, 3, pos_z)].name
            color = "#757b66"
            if floor == "minecraft:water":
                color = "#368aa2"
            elif floor in {"minecraft:moss_block", "minecraft:podzol"}:
                color = "#4b633e"
            if feet not in {"minecraft:air", "minecraft:red_mushroom", "minecraft:ladder"} or head not in {"minecraft:air", "minecraft:ladder"}:
                color = "#394039" if pos_x in (0, 16) or pos_z in (0, 16) else "#855c37"
            rectangle = [50 + pos_x * scale, 75 + pos_z * scale, 50 + (pos_x + 1) * scale - 1, 75 + (pos_z + 1) * scale - 1]
            draw.rectangle(rectangle, fill=color, outline="#25392e")
            if (pos_x, 2, pos_z) in (ENTRANCE, INTERIOR):
                draw.ellipse([rectangle[0] + 9, rectangle[1] + 9, rectangle[2] - 9, rectangle[3] - 9], fill="#edc477")
    draw.text((45, 844), "黄色点：入口 [8,2,0] / 安全点 [8,2,4]", font=engine.font(23), fill=TEXT)
    draw.text((45, 882), "蓝：密封浅水洼  ·  棕：家具/菌柄  ·  绿灰：干燥通路", font=engine.font(19), fill=MUTED)
    return canvas


def main():
    structure = Structure.read(ROOT / "room.nbt")
    with zipfile.ZipFile(engine.CLIENT) as assets:
        engine.ASSETS = assets
        views = [render(structure.blocks, turns) for turns in range(4)]
        render(structure.blocks, size=(1536, 1150), hero=True).save(ROOT / "preview.png")
        for index, image in enumerate(views):
            image.save(ROOT / f"preview/cutaway-{index * 90:03}.png")
        sheet = Image.new("RGB", (1840, 1540), BACKGROUND)
        for index, image in enumerate(views):
            sheet.paste(image.resize((920, 750), Image.Resampling.LANCZOS), ((index % 2) * 920, (index // 2) * 770))
        sheet.save(ROOT / "preview/four-views.png")
        render(structure.blocks, size=(1536, 1250), exterior=True).save(ROOT / "preview/roof-exterior.png")
        floorplan(structure.blocks).save(ROOT / "preview/floorplan.png")
    report = {"status": "PASS", "nbt_sha256": digest(ROOT / "room.nbt"), "preview_sha256": digest(ROOT / "preview.png"), "minecraft_assets": str(engine.CLIENT), "minecraft_assets_sha256": digest(engine.CLIENT), "view_count": 4, "actual_structure_nbt": True, "game_screenshot": False, "cutaway_changes_nbt": False, "models": sorted(engine.USED_MODELS), "textures": sorted(engine.USED_TEXTURES), "render_adaptations": ["Water: first frame of vanilla still-water texture, cyan tint, 14/16-height preview model", "Preview is orthographic software rendering without client lighting, particles, biome or shader simulation"]}
    write_json(ROOT / "qa/render.json", report)
    print(json.dumps({key: value for key, value in report.items() if key not in ("models", "textures")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
