import argparse
import importlib.util
import json
import sys
import zipfile
from pathlib import Path

from PIL import Image, ImageChops, ImageDraw, ImageFont, ImageOps

from build_room import DELIVERY, WORK, roof_height


OLD_TOOLS = Path("D:/ft工程文件/黑曜试炼地牢_20261101/tools")
BACKGROUND = "#100c18"
GOLD = "#efc876"
TEXT = "#f0e8f8"
MUTED = "#b5a7c6"


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def load_renderer():
    sys.path.append(str(OLD_TOOLS))
    spec = importlib.util.spec_from_file_location("obsidian_renderer", OLD_TOOLS / "render_dungeon.py")
    renderer = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(renderer)
    return renderer


def cutaway(renderer):
    visible = {}
    for position, state in renderer.BLOCKS.items():
        block_x, block_y, block_z = position
        if block_y >= roof_height(block_x, block_z) - 1:
            continue
        if block_x == 0 and block_y >= 3:
            continue
        if block_z == 0 and block_y >= 3 and not 10 <= block_x <= 14:
            continue
        visible[position] = state
    return visible


def floorplan(renderer):
    canvas = Image.new("RGB", (450, 450), BACKGROUND)
    scale = 16
    for block_x in range(25):
        for block_z in range(25):
            column = [(position[1], state) for position, state in renderer.BLOCKS.items() if position[0] == block_x and position[2] == block_z and position[1] < roof_height(block_x, block_z) - 1]
            if not column:
                continue
            block_y, state = max(column, key=lambda item: item[0])
            name = renderer.block_name(state)
            if name in ("fire", "lantern"):
                surface = Image.new("RGBA", (scale, scale), "#ffb841")
            elif name in ("chain", "iron_bars"):
                surface = Image.new("RGBA", (scale, scale), "#7d6e87")
            else:
                surface = renderer.texture(name, "top").resize((scale, scale), Image.Resampling.NEAREST)
            canvas.paste(surface, (25 + block_x * scale, 25 + block_z * scale), surface)
    draw = ImageDraw.Draw(canvas)
    draw.rectangle((25 + 11 * scale, 24, 25 + 14 * scale, 25 + 3 * scale), outline="#63edb3", width=3)
    draw.text((158, 426), "北入口 ↑ / -Z", font=font(17), fill="#63edb3")
    return canvas


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--textures-jar", type=Path, required=True)
    arguments = parser.parse_args()
    template = json.loads((WORK / "qa/blueprint.json").read_text(encoding="utf-8"))
    renderer = load_renderer()
    renderer.BLOCKS = {tuple(entry["pos"]): template["palette"][entry["state"]] for entry in template["blocks"] if template["palette"][entry["state"]]["Name"] != "minecraft:air"}
    renderer.visible_blocks = lambda: cutaway(renderer)
    with zipfile.ZipFile(arguments.textures_jar) as textures:
        renderer.TEXTURES = textures
        hero = renderer.isometric((1800, 1320))
        bounds = ImageChops.difference(hero, Image.new("RGB", hero.size, BACKGROUND)).getbbox()
        hero = ImageOps.contain(hero.crop(bounds), (1090, 785), Image.Resampling.LANCZOS)
        plan = floorplan(renderer)
    canvas = Image.new("RGB", (1600, 1100), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((44, 24), "L1 · 黑曜试炼大厅", font=font(47), fill=TEXT)
    draw.text((47, 91), "25 × 25 占地 / 12 格高 / 北向 3 × 3 通行接口", font=font(23), fill=GOLD)
    draw.line((45, 140, 1555, 140), fill="#65446f", width=2)
    canvas.paste(hero, ((1135 - hero.width) // 2, 174 + (785 - hero.height) // 2))
    canvas.paste(plan, (1120, 169))
    draw.text((1140, 642), "紧凑接入版", font=font(28), fill=GOLD)
    for index, line in enumerate(("北口与内点均干燥安全", "四座三格宽跨熔岩桥", "9 × 9 切角抬高竞技台", "金边常燃装饰火盆", "四角金顶符文柱 / 垂链", "完整围墙及双层穹顶")):
        draw.text((1142, 695 + index * 37), line, font=font(20), fill=TEXT)
    draw.rounded_rectangle((45, 947, 1555, 1025), radius=14, fill="#1a1324", outline="#61436e", width=2)
    draw.text((69, 966), "原版 1.20.1 方块  ·  不含关闭铁门 / 刷怪 / 奖励逻辑  ·  火盆非 FT 交互火盆", font=font(24), fill=GOLD)
    draw.text((48, 1051), "最终 NBT 同源方块剖切预览；使用原版贴图并提亮，小模型简化，非实机截图。制作与质检交付，未部署、未改核心。", font=font(18), fill=MUTED)
    canvas.save(DELIVERY / "preview.png")
    hero.save(WORK / "qa/isometric.png")
    plan.save(WORK / "qa/floorplan.png")
    print(DELIVERY / "preview.png")


if __name__ == "__main__":
    main()
