from __future__ import annotations

import json
import zipfile

from PIL import Image, ImageDraw

import mc_models as engine
from build_r1 import ROOT, SIZE, digest, write_json
from nbtio import Structure


BACKGROUND = "#15191f"
TEXT = "#e7e3d7"
MUTED = "#aeb5bd"
GOLD = "#e9b864"
TEAL = "#74c8b8"
engine.PARTIAL.update({"lantern", "rail", "spruce_fence", "cauldron", "tripwire_hook", "anvil"})


def rotated(blocks, turns):
    results = {}
    directions = ["north", "east", "south", "west"]
    for position, block in blocks.items():
        pos_x, pos_y, pos_z = position
        properties = dict(block.properties)
        for turn in range(turns):
            pos_x, pos_z = 16 - pos_z, pos_x
        if properties.get("facing") in directions:
            properties["facing"] = directions[(directions.index(properties["facing"]) + turns) % 4]
        original = dict(properties)
        for index, direction in enumerate(directions):
            if direction in original:
                properties[directions[(index + turns) % 4]] = original[direction]
        if properties.get("axis") in ("x", "z") and turns % 2:
            properties["axis"] = "z" if properties["axis"] == "x" else "x"
        if properties.get("shape") in ("north_south", "east_west") and turns % 2:
            properties["shape"] = "east_west" if properties["shape"] == "north_south" else "north_south"
        results[(pos_x, pos_y, pos_z)] = {"Name": block.name, "Properties": properties}
    return results


def render(blocks, turns=0, size=(1800, 1400), exterior=False):
    all_blocks = rotated(blocks, turns)
    visible = {}
    for position, block in all_blocks.items():
        pos_x, pos_y, pos_z = position
        if block["Name"] == "minecraft:air":
            continue
        if not exterior and (pos_y >= 9 or pos_y >= 1 and (pos_x == 0 or pos_z == 0)):
            continue
        visible[position] = block
    projected = [(position[0] - position[2], -(position[0] + position[2]) * 0.62 - position[1] * 0.95) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1, max(point[1] for point in projected) + 1
    scale = min((size[0] - 80) / (high_x - low_x), (size[1] - 180) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 60) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(point):
        pos_x, pos_y, pos_z = point
        return offset_x + (pos_x - pos_z) * scale, offset_y - (pos_x + pos_z) * scale * 0.62 - pos_y * scale * 0.95

    faces = []
    for position, block in visible.items():
        faces.extend(engine.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 1.45 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        texture = engine.texture(identifier, uv, rotation, min(1.35, brightness * 1.08))
        engine.parallelogram(canvas, texture, [project(point) for point in points])
    draw = ImageDraw.Draw(canvas)
    heading = "R1  坍塌矿道大厅  ·  完整顶盖外观" if exterior else "R1  坍塌矿道大厅  ·  结构剖切预览"
    draw.text((32, 20), heading, font=engine.font(30 if size[0] > 1200 else 22), fill=GOLD)
    labels = ("北 / 西侧观察", "东 / 北侧观察", "南 / 东侧观察", "西 / 南侧观察")
    draw.text((32, 65), labels[turns] + "   |   17 × 11 × 17", font=engine.font(23 if size[0] > 1200 else 17), fill=TEAL)
    draw.text((32, size[1] - 54), "实际 NBT + 原版模型纹理软件渲染  ·  非游戏截图  ·  剖切不修改最终屋顶", font=engine.font(21 if size[0] > 1200 else 15), fill=MUTED)
    return canvas.convert("RGB")


def floorplan(structure):
    canvas = Image.new("RGB", (1020, 1100), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((34, 22), "R1 平面核验  /  北 -Z ↑", font=engine.font(30), fill=GOLD)
    scale = 52
    for pos_x in range(17):
        for pos_z in range(17):
            feet = structure.get((pos_x, 1, pos_z))
            color = "#626971" if feet.name in ("minecraft:air", "minecraft:rail") else "#363d45"
            if pos_x in range(7, 10) and pos_z < 16:
                color = "#438779"
            if feet.name == "minecraft:barrel":
                color = "#a47545"
            if 10 <= pos_x <= 15 and 6 <= pos_z <= 12 and feet.name != "minecraft:air":
                color = "#8b7566"
            if feet.name == "minecraft:cauldron":
                color = "#b5bec8"
            left, top = 68 + pos_x * scale, 95 + pos_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=color)
            if feet.name == "minecraft:rail":
                for offset in (17, 34):
                    draw.line((left + offset, top, left + offset, top + scale - 2), fill=GOLD, width=3)
            if feet.name == "minecraft:polished_deepslate" and pos_x in (3, 13) and pos_z in (5, 13):
                draw.text((left + 9, top + 8), "柱", font=engine.font(24), fill=GOLD)
    for label, position, color in (("E", (8, 0), "#ffffff"), ("S", (8, 4), "#ffffff")):
        left, top = 68 + position[0] * scale, 95 + position[1] * scale
        draw.ellipse((left + 10, top + 10, left + 40, top + 40), fill=TEAL, outline=color, width=2)
        draw.text((left + 15, top + 10), label, font=engine.font(23), fill=BACKGROUND)
    draw.text((68, 1005), "绿色：三格干燥主路   棕色：落地塌方   E：入口   S：安全点", font=engine.font(24), fill=TEXT)
    draw.text((68, 1048), "所有坐标为模板相对坐标；实际屋顶完整，平面图仅隐藏上层。", font=engine.font(21), fill=MUTED)
    return canvas


def main():
    source_hash = digest(ROOT / "room.nbt")
    structure = Structure.read(ROOT / "room.nbt")
    engine.ASSETS = zipfile.ZipFile(engine.CLIENT)
    render(structure.blocks).save(ROOT / "preview.png")
    render(structure.blocks, exterior=True, size=(1600, 1300)).save(ROOT / "qa/exterior.png")
    sheet = Image.new("RGB", (2000, 1640), BACKGROUND)
    for turns in range(4):
        sheet.paste(render(structure.blocks, turns, (1000, 820)), ((turns % 2) * 1000, (turns // 2) * 820))
    sheet.save(ROOT / "qa/four-views.png")
    floorplan(structure).save(ROOT / "qa/floorplan.png")
    if source_hash != digest(ROOT / "room.nbt"):
        raise ValueError("Rendering changed the delivered NBT")
    files = ["preview.png", "qa/exterior.png", "qa/four-views.png", "qa/floorplan.png"]
    report = {
        "status": "PASS", "source_sha256": source_hash, "source": "room.nbt",
        "renderer": "deterministic local software rendering of actual serialized NBT",
        "assets": str(engine.CLIENT), "assets_sha256": digest(engine.CLIENT),
        "original_minecraft_models": sorted(engine.USED_MODELS),
        "original_minecraft_textures": sorted(engine.USED_TEXTURES),
        "synthetic_models_used": [], "game_screenshot": False,
        "cutaway": "preview hides y>=9 and camera-near perimeter walls only; room.nbt remains sealed",
        "files": {name: {"sha256": digest(ROOT / name), "pixels": Image.open(ROOT / name).size} for name in files},
    }
    write_json(ROOT / "qa/render.json", report)
    print(json.dumps({"status": "PASS", "source_sha256": source_hash, "rendered_files": files}, ensure_ascii=False))


if __name__ == "__main__":
    main()
