import argparse
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageFilter

import voxel_preview as engine
from build_chapel import CAGES, ROOT, SIZE, load_structure, write_json


PREVIEW = ROOT / "preview"
BACKGROUND = "#130f15"
PANEL = "#21191f"
TEXT = "#f0e2d5"
MUTED = "#b6a4a2"
RED = "#d47670"
GOLD = "#efb15d"


def cutaway(blocks):
    visible = {}
    for position, state in blocks.items():
        block_x, block_y, block_z = position
        if block_y >= 10 and state["Name"] != "minecraft:dark_oak_log":
            continue
        if block_y >= 3 and (block_x == 0 or block_z <= 1):
            continue
        if block_y >= 5 and block_x == 1 and block_z in (2, 7) and state["Name"] in ("minecraft:deepslate_bricks", "minecraft:chiseled_deepslate"):
            continue
        visible[position] = state
    return visible


def isometric(blocks, size=(2000, 1560), mode="cutaway"):
    visible = blocks if mode == "exterior" else cutaway(blocks)
    if mode == "altar":
        visible = {position: state for position, state in blocks.items() if 4 <= position[0] <= 10 and 5 <= position[2] <= 11 and position[1] <= 4}
    label = {"cutaway": "轴测剖面", "exterior": "完整含顶结构", "altar": "五格祭坛细节"}[mode]
    projected = [(position[0] - position[2], -(position[0] + position[2]) * 0.72 - position[1] * 0.85) for position in visible]
    low_x, high_x = min(point[0] for point in projected) - 1, max(point[0] for point in projected) + 1
    low_y, high_y = min(point[1] for point in projected) - 1.5, max(point[1] for point in projected) + 1
    scale = min((size[0] - 100) / (high_x - low_x), (size[1] - 185) / (high_y - low_y))
    offset_x = size[0] / 2 - (low_x + high_x) * scale / 2
    offset_y = (size[1] + 48) / 2 - (low_y + high_y) * scale / 2
    canvas = Image.new("RGBA", size, BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((48, 28), "血棘祭炉祭坛厅 / " + label, font=engine.font(32), fill=TEXT)

    def project(point):
        block_x, block_y, block_z = point
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale * 0.72 - block_y * scale * 0.85

    faces = []
    for position, state in visible.items():
        faces.extend(engine.block_faces(position, state, visible))
    faces.sort(key=lambda face: sum(point[0] + point[2] - point[1] * 1.6941 for point in face[0]) / 4, reverse=True)
    for points, identifier, uv, rotation, brightness in faces:
        engine.parallelogram(canvas, engine.texture(identifier, uv, rotation, brightness * 0.74), [project(point) for point in points])
    glow = Image.new("RGBA", size)
    glow_draw = ImageDraw.Draw(glow)
    for position, state in visible.items():
        name = state["Name"].removeprefix("minecraft:")
        if name not in ("lantern", "candle", "campfire") or mode == "exterior":
            continue
        center_x, center_y = project((position[0] + 0.5, position[1] + 0.45, position[2] + 0.5))
        radius = scale * (0.6 if name == "campfire" else 0.28)
        glow_draw.ellipse((center_x - radius, center_y - radius, center_x + radius, center_y + radius), fill=(255, 154, 46, 65))
    canvas.alpha_composite(glow.filter(ImageFilter.GaussianBlur(scale * 0.22)))
    draw = ImageDraw.Draw(canvas)
    draw.text((48, size[1] - 52), "最终 NBT 方块软件预览 · 非游戏截图" + (" · 屋顶与近侧墙已剖去" if mode == "cutaway" else ""), font=engine.font(22), fill=MUTED)
    return canvas.convert("RGB")


def floorplan(blocks):
    scale, offset_x, offset_y = 47, 65, 160
    canvas = Image.new("RGB", (1260, 990), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((60, 30), "血棘祭炉 / 平面动线", font=engine.font(32), fill=TEXT)
    draw.text((60, 90), "北 -Z ↑ · 入口在上方 · 灰色虚线框为上方吊笼", font=engine.font(22), fill=MUTED)
    colors = {"floor": "#665b59", "wall": "#332d31", "altar": "#a54e4a", "totem": "#804940", "bars": "#9b9692", "storage": "#87643f", "book": "#d4b884"}
    for block_x in range(SIZE[0]):
        for block_z in range(SIZE[2]):
            if not blocks.get((block_x, 1, block_z)) and not blocks.get((block_x, 0, block_z)):
                continue
            category = "floor"
            for block_y in range(2, 4):
                state = blocks.get((block_x, block_y, block_z))
                if not state:
                    continue
                name = state["Name"].removeprefix("minecraft:")
                if name in ("deepslate_bricks", "chiseled_deepslate", "cracked_deepslate_bricks"):
                    category = "wall"
                elif name in ("iron_door", "iron_bars"):
                    category = "bars"
                elif name == "barrel":
                    category = "storage"
                elif name == "lectern":
                    category = "book"
                elif name in ("dark_oak_log", "crimson_hyphae"):
                    category = "totem"
            if 5 <= block_x <= 9 and 6 <= block_z <= 10:
                category = "altar"
            left, top = offset_x + block_x * scale, offset_y + block_z * scale
            draw.rectangle((left, top, left + scale - 2, top + scale - 2), fill=colors[category])
    for center_x, center_z in CAGES:
        left, top = offset_x + (center_x - 1) * scale, offset_y + (center_z - 1) * scale
        right, bottom = left + scale * 3 - 2, top + scale * 3 - 2
        for edge_x in range(left, right, 15):
            draw.line((edge_x, top, min(edge_x + 8, right), top), fill="#d0b68f", width=2)
            draw.line((edge_x, bottom, min(edge_x + 8, right), bottom), fill="#d0b68f", width=2)
        for edge_y in range(top, bottom, 15):
            draw.line((left, edge_y, left, min(edge_y + 8, bottom)), fill="#d0b68f", width=2)
            draw.line((right, edge_y, right, min(edge_y + 8, bottom)), fill="#d0b68f", width=2)
    for block_x, block_z, label in ((7, 2, "01"), (7, 8, "02"), (7, 13, "03"), (3, 11, "04"), (2, 7, "05"), (12, 9, "06")):
        center = offset_x + (block_x + 0.5) * scale, offset_y + (block_z + 0.5) * scale
        draw.rounded_rectangle((center[0] - 24, center[1] - 18, center[0] + 24, center[1] + 18), radius=5, fill=BACKGROUND, outline=GOLD, width=2)
        draw.text(center, label, font=engine.font(22), fill=GOLD, anchor="mm")
    for index, label in enumerate(("01 / 北向入口", "02 / 5×5 阶梯祭坛", "03 / 血棘图腾与头骨", "04 / 四只悬挂铁笼", "05 / 烛龛、骨匣与残卷", "06 / 双侧踏板遗物门")):
        draw.text((825, 210 + index * 67), label, font=engine.font(24), fill=TEXT)
    draw.text((825, 665), "占地：15×15 格", font=engine.font(24), fill=MUTED)
    draw.text((825, 710), "墙高：8 格", font=engine.font(24), fill=MUTED)
    draw.text((825, 755), "NBT：15×12×15", font=engine.font(24), fill=MUTED)
    draw.text((60, 920), "通路按两格净高检查；铁门按两侧踏板可打开计算。", font=engine.font(23), fill=MUTED)
    return canvas


def design_sheet(hero, plan, altar):
    canvas = Image.new("RGB", (2500, 1860), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((75, 38), "M1 / BLOODTHORN CHAPEL", font=engine.font(25), fill=RED)
    draw.text((72, 84), "血棘祭炉祭坛厅", font=engine.font(60), fill=TEXT)
    draw.text((78, 171), "参考拼图第 3 张 · 15×15 主厅 / 墙高8格 / 5×5祭坛 / 4只吊笼", font=engine.font(28), fill=MUTED)
    draw.line((76, 238, 2420, 238), fill="#493336", width=2)
    hero_view = hero.crop((0, 100, hero.width, hero.height - 90))
    hero_view.thumbnail((1770, 1340), Image.Resampling.LANCZOS)
    canvas.paste(hero_view, (15 + (1770 - hero_view.width) // 2, 260 + (1340 - hero_view.height) // 2))
    draw.rounded_rectangle((1820, 275, 2420, 1450), radius=18, fill=PANEL)
    plan_view = plan.crop((60, 153, 780, 875))
    plan_view.thumbnail((490, 490), Image.Resampling.NEAREST)
    canvas.paste(plan_view, (1875, 315))
    for index, label in enumerate(("阶梯祭坛 + 铁框常燃营火", "梁下四笼 + 真正暖光灯笼", "后墙血棘图腾 + 深红长幡", "裂纹地坪 + 暗红斑驳", "烛群头骨 + 一册祭仪残卷", "含完整屋顶；近墙仅预览剖切")):
        draw.text((1865, 855 + index * 60), label, font=engine.font(25), fill=TEXT if index < 3 else MUTED)
    altar_view = altar.crop((140, 165, altar.width - 150, altar.height - 130))
    altar_view.thumbnail((420, 270), Image.Resampling.LANCZOS)
    canvas.paste(altar_view, (1900, 1200))
    draw.line((76, 1570, 2420, 1570), fill="#493336", width=2)
    for index, (color, label) in enumerate((("#67605c", "裂纹石砖"), ("#883f42", "血色砖与红幡"), ("#6a4b30", "深色橡木梁"), ("#b4aaa0", "铁笼与吊链"), ("#e8a253", "营火与烛光"))):
        left = 78 + index * 470
        draw.rectangle((left, 1620, left + 26, 1646), fill=color)
        draw.text((left + 42, 1617), label, font=engine.font(27), fill=TEXT)
    draw.text((78, 1700), "Minecraft 1.20.1 / 全原版方块 / 独立建筑数据包 + NBT / 未改动存档与现有地牢", font=engine.font(25), fill=MUTED)
    draw.text((78, 1750), "软件预览，并非实机截图；无自动刷怪、Boss、收费与奖励逻辑。自定义祭炉未被冒用为已注册方块。", font=engine.font(23), fill=MUTED)
    draw.text((78, 1805), "FT / REFERENCE III / 2026.11.01", font=engine.font(21), fill=RED)
    return canvas


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--textures-jar", type=Path, default=engine.CLIENT)
    arguments = parser.parse_args()
    engine.ASSETS = zipfile.ZipFile(arguments.textures_jar)
    template = load_structure(ROOT / "out/FT_Bloodthorn_Chapel.nbt")
    blocks = {tuple(entry["pos"]): template["palette"][entry["state"]] for entry in template["blocks"]}
    PREVIEW.mkdir(parents=True, exist_ok=True)
    hero = isometric(blocks)
    hero.save(PREVIEW / "bloodthorn-chapel-cutaway.png")
    isometric(blocks, mode="exterior").save(PREVIEW / "bloodthorn-chapel-exterior.png")
    altar = isometric(blocks, size=(1500, 1200), mode="altar")
    altar.save(PREVIEW / "bloodthorn-chapel-altar.png")
    plan = floorplan(blocks)
    plan.save(PREVIEW / "bloodthorn-chapel-floorplan.png")
    design_sheet(hero, plan, altar).save(PREVIEW / "bloodthorn-chapel-design-sheet.png")
    write_json(ROOT / "qa/render-manifest.json", {"source": "Final compressed structure NBT", "blocks": len(blocks), "vanilla_models": sorted(engine.USED_MODELS), "textures": sorted(engine.USED_TEXTURES), "views": ["cutaway", "exterior", "altar", "floorplan", "design-sheet"], "limitations": "Software orthographic preview, not Minecraft gameplay. Skull, banner and lectern book entities simplified; animated campfire sampled as static frame; no actual lighting/smoke simulation. Roof and near walls removed in cutaway only."})
    print(json.dumps({"preview": str(PREVIEW), "blocks": len(blocks), "models": len(engine.USED_MODELS)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
