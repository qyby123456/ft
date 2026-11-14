from __future__ import annotations

import hashlib
import json
import sys
from pathlib import Path

import numpy as np
from PIL import Image, ImageDraw


PROJECT = Path(__file__).resolve().parents[4]
sys.path.insert(0, str(PROJECT / "tools/model3d/boss_materials_refine/spore"))
from preview import checker, font, render


ASSETS = PROJECT / "src/main/resources/assets/ftboss"
OUTPUT = PROJECT / "art/boss_relics/refine/spore"
WORK = PROJECT / "_work/boss-relics-refine-20261021/spore"
IDENTIFIER = "rainbow_spore_charm"


def load_final():
    wrapper_path = ASSETS / "models/item" / (IDENTIFIER + ".json")
    wrapper = json.loads(wrapper_path.read_text(encoding="utf-8"))
    model_path = ASSETS / "models" / (wrapper["base"]["parent"].split(":", 1)[1] + ".json")
    model = json.loads(model_path.read_text(encoding="utf-8"))
    texture_path = ASSETS / "textures" / (model["textures"]["atlas"].split(":", 1)[1] + ".png")
    texture = np.array(Image.open(texture_path).convert("RGBA"))
    return model, texture, [wrapper_path, model_path, texture_path]


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    model, texture, final_paths = load_final()
    sheet = Image.new("RGB", (1440, 900), "#1e252c")
    draw = ImageDraw.Draw(sheet)
    draw.text((34, 22), "虹彩孢子符  /  最终运行时资源 · 四视图", font=font(30, True), fill="#ebe3cf")
    draw.text((37, 74), "CRAFTED SPORE TALISMAN   ·   FRONT / BACK / LEFT / RIGHT   ·   2026.10.21", font=font(16), fill="#a6b4a7")
    for index, (name, label, yaw) in enumerate([
        ("front", "正面 / NORTH", 0), ("back", "背面 / SOUTH", 180),
        ("left", "左侧 / WEST", -90), ("right", "右侧 / EAST", 90),
    ]):
        origin_x, origin_y = (index % 2) * 720, 120 + (index // 2) * 385
        image = render(model, texture, (650, 325), yaw=yaw, margin=12, common_scale=23)
        image.save(OUTPUT / (name + ".png"))
        sheet.paste(image, (origin_x + 35, origin_y + 15), image)
        draw.text((origin_x + 38, origin_y + 345), label, font=font(19), fill="#c6ccbf")
    sheet.save(OUTPUT / "four_views.png")
    hero = render(model, texture, (760, 880), yaw=-24, pitch=8, margin=50)
    hero.save(OUTPUT / "hero.png")
    back_detail = render(model, texture, (600, 690), yaw=155, pitch=10, margin=42)
    back_detail.save(OUTPUT / "back_detail.png")
    detail = render(model, texture, (740, 760), yaw=25, pitch=-8, margin=42)
    detail.save(OUTPUT / "setting_detail.png")
    icon_path = ASSETS / "textures/item" / (IDENTIFIER + ".png")
    icon = Image.open(icon_path).convert("RGBA")
    old_icon = Image.open(WORK / "backup/src/main/resources/assets/ftboss/textures/item" / (IDENTIFIER + ".png")).convert("RGBA")
    material_icon = Image.open(ASSETS / "textures/item/spore_material.png").convert("RGBA")
    comparison = Image.new("RGB", (1220, 455), "#1e252c")
    comparison_draw = ImageDraw.Draw(comparison)
    comparison_draw.text((28, 20), "背包图标 / 精细模型 / 同 Boss 材质呼应", font=font(27, True), fill="#e9e0cd")
    for image, position, text in [
        (icon, (28, 105), "原尺寸 32 px"),
        (icon.resize((256, 256), Image.Resampling.NEAREST), (148, 105), "8× 像素放大"),
        (old_icon.resize((192, 192), Image.Resampling.NEAREST), (443, 105), "旧版遗物图标"),
        (render(model, texture, (280, 300), yaw=-8, pitch=3, margin=10), (682, 86), "同角度运行时模型"),
        (material_icon.resize((160, 160), Image.Resampling.NEAREST), (1010, 105), "同 Boss / 虹彩孢子"),
    ]:
        background = checker(image.size)
        background.paste(image, (0, 0), image)
        comparison.paste(background, position)
        comparison_draw.text((position[0], 402), text, font=font(14), fill="#c3cebd")
    comparison.save(OUTPUT / "icon_comparison.png")
    board = Image.new("RGB", (1600, 1240), "#ece8dc")
    board_draw = ImageDraw.Draw(board)
    board_draw.text((46, 30), "FTBOSS   /   RELIC STUDY 07", font=font(19), fill="#738173")
    board_draw.text((42, 67), "虹彩孢子符", font=font(58, True), fill="#2c3f3c")
    board_draw.text((414, 111), "IRIDESCENT SPORE TALISMAN", font=font(22), fill="#738173")
    board_draw.text((46, 165), "炫彩答辩超人  ·  遗物  ·  ftboss:rainbow_spore_charm", font=font(21), fill="#5a6e62")
    board.paste(hero, (25, 218), hero)
    board_draw.line((825, 230, 825, 1100), fill="#c9cec3", width=2)
    board_draw.text((874, 228), "镶晶菌印 / 锻铜包边 / 双环佩戴结构", font=font(27, True), fill="#334f45")
    small_back = back_detail.resize((375, 430))
    small_detail = detail.resize((380, 390))
    board.paste(small_back, (834, 294), small_back)
    board.paste(small_detail, (1195, 313), small_detail)
    board_draw.text((867, 741), "正面立体菌纹 · 放射菌褶 · 虹彩宝石镶座", font=font(21), fill="#536a59")
    board_draw.text((867, 788), "背面压印菌符 · 铆接背板 · 金属倒角与包边", font=font(21), fill="#536a59")
    board_draw.text((867, 835), "吊环与下悬孢晶均有实体连接，不是平面贴片", font=font(21), fill="#536a59")
    board_draw.text((867, 882), "菌质棕 / 灰绿边 / 青绿主晶，与原材料成套", font=font(21), fill="#536a59")
    icon_large = icon.resize((160, 160), Image.Resampling.NEAREST)
    material_large = material_icon.resize((160, 160), Image.Resampling.NEAREST)
    board.paste(icon_large, (920, 958), icon_large)
    board.paste(material_large, (1230, 958), material_large)
    board_draw.text((925, 1130), "成品遗物", font=font(18), fill="#61755f")
    board_draw.text((1235, 1130), "同源原材料", font=font(18), fill="#61755f")
    board_draw.text((45, 1165), f'{len(model["elements"])} 个结构部件  /  逐面 UV 2 px/单位  /  32² 独立图标  /  可编辑 Blockbench',
                    font=font(18), fill="#62725f")
    board_draw.text((45, 1202), "最终运行时模型与贴图离线渲染 · 不代表游戏实测 · 不改 Curios 效果、NBT、名称与掉落", font=font(15), fill="#7a8475")
    board.save(OUTPUT / "presentation.png")
    final_paths.append(icon_path)
    manifest = {"directions": ["north", "south", "west", "east"],
                "source": "final forge separate_transforms entry, base model, per-face UV atlas",
                "renderer": "read-only material renderer with actual Java element rotation/rescale, z-buffer and nearest RGBA sampling",
                "files": {str(target): hashlib.sha256(target.read_bytes()).hexdigest() for target in final_paths}}
    (WORK / "render-manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8", newline="\n")
    print("Relic runtime preview completed")


if __name__ == "__main__":
    main()
