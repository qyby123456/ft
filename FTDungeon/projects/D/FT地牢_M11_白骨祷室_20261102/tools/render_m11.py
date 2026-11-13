from __future__ import annotations

import functools
import hashlib
import io
import json
import zipfile

from PIL import Image, ImageChops, ImageDraw, ImageEnhance

import mc_models as engine
from build_m11 import DELIVERY, ENTRANCE, INTERIOR, ROOT, SIZE, digest, write_json
from nbtio import Structure, plain


BACKGROUND = "#14151b"
TEXT = "#f0e6d4"
MUTED = "#aaa8ab"
ACCENT = "#d6b77f"
TEAL = "#68d0d1"
CUSTOM_TEXTURES = {}
ORIGINAL_SYNTHETIC = engine.synthetic_model
ORIGINAL_TEXTURE = engine.texture
engine.PARTIAL.add("lantern")


def banner_texture(entity):
    signature = hashlib.sha256(json.dumps(entity, sort_keys=True).encode()).hexdigest()[:12]
    identifier = "preview/banner_" + signature
    if identifier in CUSTOM_TEXTURES:
        return identifier
    base = Image.open(io.BytesIO(engine.ASSETS.read("assets/minecraft/textures/entity/banner/base.png"))).convert("RGBA").crop((1, 1, 21, 41))
    base = ImageChops.multiply(base, Image.new("RGBA", base.size, (176, 46, 38, 255)))
    for pattern in entity.get("Patterns", []):
        pattern_name = {"sku": "skull", "bo": "border"}[pattern["Pattern"]]
        mask = Image.open(io.BytesIO(engine.ASSETS.read(f"assets/minecraft/textures/entity/banner/{pattern_name}.png"))).convert("RGBA").crop((1, 1, 21, 41)).getchannel("A")
        color = (249, 255, 254, 255) if pattern["Color"] == 0 else (29, 29, 33, 255)
        base.paste(Image.new("RGBA", base.size, color), (0, 0), mask)
    CUSTOM_TEXTURES[identifier] = base
    return identifier


def synthetic(name, properties):
    if name == "skeleton_skull":
        definition, choice = ORIGINAL_SYNTHETIC(name, properties)
        return definition, {"y": (choice["y"] + 180) % 360}
    if name.endswith("_wall_banner") and "preview_texture" in properties:
        faces = {face: {"texture": properties["preview_texture"], "uv": [0, 0, 16, 16]} for face in engine.FACE_STEPS}
        return {"textures": {}, "elements": [{"from": [2, -12, 14], "to": [14, 15, 15], "faces": faces}]}, {"y": {"north": 0, "east": 90, "south": 180, "west": 270}[properties["facing"]]}
    return ORIGINAL_SYNTHETIC(name, properties)


@functools.lru_cache(maxsize=None)
def texture(identifier, uv, rotation, brightness):
    if identifier not in CUSTOM_TEXTURES:
        return ORIGINAL_TEXTURE(identifier, uv, rotation, brightness)
    image = CUSTOM_TEXTURES[identifier].copy()
    image = image.rotate(-rotation, expand=True)
    alpha = image.getchannel("A")
    image = ImageEnhance.Brightness(image).enhance(brightness)
    image.putalpha(alpha)
    return image.resize((96, 192), Image.Resampling.NEAREST)


engine.synthetic_model = synthetic
engine.texture = texture


def rotated(structure, turns):
    result = {}
    directions = ["north", "east", "south", "west"]
    for position, block in structure.blocks.items():
        pos_x, pos_y, pos_z = position
        width, depth = SIZE[0], SIZE[2]
        properties = dict(block.properties)
        if block.name == "minecraft:red_wall_banner":
            properties["preview_texture"] = banner_texture(plain(structure.block_entities[position]))
        for turn in range(turns):
            pos_x, pos_z = depth - 1 - pos_z, pos_x
            width, depth = depth, width
        if properties.get("facing") in directions:
            properties["facing"] = directions[(directions.index(properties["facing"]) + turns) % 4]
        if properties.get("axis") in ("x", "z") and turns % 2:
            properties["axis"] = "z" if properties["axis"] == "x" else "x"
        if "rotation" in properties:
            properties["rotation"] = str((int(properties["rotation"]) + turns * 4) % 16)
        result[(pos_x, pos_y, pos_z)] = {"Name": block.name, "Properties": properties}
    return result


def render(structure, turns=0, size=(1600, 1300), exterior=False):
    visible = {}
    for position, block in rotated(structure, turns).items():
        pos_x, pos_y, pos_z = position
        if block["Name"] == "minecraft:air":
            continue
        if not exterior and (pos_y == 11 or pos_y >= 2 and (pos_x <= 1 or pos_z <= 1)):
            continue
        visible[position] = block
    projected = [(position[0] - position[2], -(position[0] + position[2]) * 0.66 - position[1] * 0.92) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1, max(point[1] for point in projected) + 1
    scale = min((size[0] - 50) / (high_x - low_x), (size[1] - 140) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 40) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(point):
        return offset_x + (point[0] - point[2]) * scale, offset_y - (point[0] + point[2]) * scale * 0.66 - point[1] * scale * 0.92

    faces = []
    for position, block in visible.items():
        faces.extend(engine.block_faces(position, block, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 1.55 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        engine.parallelogram(canvas, texture(identifier, uv, rotation, brightness * 0.95), [project(point) for point in points])
    draw = ImageDraw.Draw(canvas)
    draw.text((28, 18), "完整屋顶 / 北口外观" if exterior else ("西北剖面", "东北剖面", "东南剖面", "西南剖面")[turns], font=engine.font(25), fill=ACCENT)
    draw.text((28, size[1] - 43), "实际 NBT + 原版材质软件渲染 · 非游戏截图", font=engine.font(21), fill=MUTED)
    return canvas.convert("RGB")


def floorplan(structure):
    canvas = Image.new("RGB", (480, 610), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((18, 12), "北 -Z ↑ / 3×3 接口", font=engine.font(23), fill=TEAL)
    scale = 28
    for pos_x in range(SIZE[0]):
        for pos_z in range(SIZE[2]):
            block = structure.get((pos_x, 2, pos_z))
            name = block.name.removeprefix("minecraft:")
            color = "#666569" if name == "air" else "#343740"
            if name == "polished_andesite" and 5 <= pos_x <= 9 and 9 <= pos_z <= 13:
                color = "#8e5358"
            if name == "stone_brick_stairs":
                color = "#b88670"
            if name == "skeleton_skull":
                color = "#cbbb99"
            left, top = 25 + pos_x * scale, 60 + pos_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=color)
    routes = (((7, 0), (7, 7)), ((7, 6), (3, 6), (3, 9), (4, 9), (4, 14), (10, 14), (10, 9), (11, 9), (11, 6), (7, 6)))
    for route in routes:
        for start, end in zip(route, route[1:]):
            for pos_x in range(min(start[0], end[0]), max(start[0], end[0]) + 1):
                for pos_z in range(min(start[1], end[1]), max(start[1], end[1]) + 1):
                    assert all(structure.get((pos_x, pos_y, pos_z)).name == "minecraft:air" for pos_y in (2, 3))
        draw.line([(25 + (pos_x + 0.5) * scale, 60 + (pos_z + 0.5) * scale) for pos_x, pos_z in route], fill=TEAL, width=4)
    for point in (ENTRANCE, INTERIOR):
        center_x, center_y = 25 + (point[0] + 0.5) * scale, 60 + (point[2] + 0.5) * scale
        draw.ellipse((center_x - 6, center_y - 6, center_x + 6, center_y + 6), fill="#e7ecd8")
    draw.text((18, 553), "青：干燥环路  红：抬高祭坛", font=engine.font(21), fill=MUTED)
    return canvas


def main():
    structure = Structure.read(DELIVERY / "room.nbt")
    before = digest(DELIVERY / "room.nbt")
    with zipfile.ZipFile(engine.CLIENT) as archive:
        engine.ASSETS = archive
        hero = render(structure, 0, (1800, 1500))
        hero.save(ROOT / "qa/cutaway.png")
        four = Image.new("RGB", (2400, 2200), BACKGROUND)
        for turns in range(4):
            image = render(structure, turns, (1200, 1100))
            image.save(ROOT / f"qa/view-{turns}.png")
            four.paste(image, ((turns % 2) * 1200, (turns // 2) * 1100))
        four.save(ROOT / "qa/four-views.png")
        render(structure, 0, (1400, 1200), True).save(ROOT / "qa/exterior.png")
    plan = floorplan(structure)
    plan.save(ROOT / "qa/floorplan.png")
    canvas = Image.new("RGB", (1536, 1024), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((35, 21), "M11 / 白骨祷室", font=engine.font(39), fill=TEXT)
    draw.text((37, 78), "BONE PRAYER CHAMBER  ·  15 × 12 × 17  ·  Java 1.20.1", font=engine.font(23), fill=MUTED)
    hero = hero.crop((0, 65, hero.width, hero.height - 60))
    hero.thumbnail((1155, 835), Image.Resampling.LANCZOS)
    canvas.paste(hero, ((1165 - hero.width) // 2, 130 + (835 - hero.height) // 2))
    plan.thumbnail((330, 410), Image.Resampling.LANCZOS)
    canvas.paste(plan, (1180, 127))
    labels = ("14 颗白骨头饰 / 合法支撑", "5 面骷髅纹红旗 / 原生 NBT", "15 组烛火 / 5 盏灯", "红布祭坛 + 空仪式盆", "锚固吊灯 / 石砌拱肋", "完整屋顶 / 双侧安全环路")
    for index, label in enumerate(labels):
        draw.text((1172, 555 + index * 43), label, font=engine.font(20), fill=TEXT if index < 3 else MUTED)
    draw.text((1172, 845), "入口脚位 (7, 2, 0)", font=engine.font(21), fill=TEAL)
    draw.text((1172, 880), "安全内点 (7, 2, 4)", font=engine.font(21), fill=TEAL)
    draw.line((35, 953, 1500, 953), fill="#48424a", width=2)
    draw.text((37, 974), "实际 NBT 软件预览 / 剖切仅用于展示 / 非游戏截图 / 无奖励库存 / 未部署", font=engine.font(22), fill=MUTED)
    canvas.save(DELIVERY / "preview.png")
    assert before == digest(DELIVERY / "room.nbt")
    write_json(ROOT / "qa/render.json", {"status": "PASS", "structure_sha256": before, "preview_sha256": digest(DELIVERY / "preview.png"), "preview_size": [1536, 1024], "source_textures": str(engine.CLIENT), "models": len(engine.USED_MODELS), "textures": len(engine.USED_TEXTURES), "banner_patterns_from_nbt": True, "views": 4, "roof_unchanged": True, "scope": "CPU raster of actual NBT and vanilla block models/textures; skulls and static banner cloth use simple entity meshes; no game lighting, waving cloth or particles; not a game screenshot"})
    print(json.dumps({"render": "PASS", "preview": str(DELIVERY / "preview.png")}, ensure_ascii=False))


if __name__ == "__main__":
    main()
