import argparse
import functools
import io
import json
import math
import zipfile
from pathlib import Path

from PIL import Image, ImageDraw, ImageEnhance, ImageFont

from build_dungeon import roof_height


ROOT = Path(__file__).resolve().parents[1]
FONT = Path("C:/Windows/Fonts/msyh.ttc")
BACKGROUND = "#100c18"
GOLD = "#efc876"
TEXT = "#f0e8f8"
MUTED = "#b5a7c6"
PALETTE = []
BLOCKS = {}
TEXTURES = None


def font(size):
    return ImageFont.truetype(str(FONT), size)


def block_name(state):
    return state["Name"].split(":", 1)[1]


@functools.lru_cache(maxsize=None)
def texture(name, face="side"):
    if name.endswith("_stairs") or name.endswith("_slab"):
        name = name.rsplit("_", 1)[0]
        if name.endswith("_brick"):
            name += "s"
    if name == "lava":
        name = "lava_still"
    elif name == "fire":
        name = "fire_0"
    elif name == "barrel":
        name = "barrel_top" if face == "top" else "barrel_side"
    path = f"assets/minecraft/textures/block/{name}.png"
    image = Image.open(io.BytesIO(TEXTURES.read(path))).convert("RGBA")
    image = image.crop((0, 0, image.width, min(image.width, image.height)))
    if name not in ("lava_still", "fire_0", "lantern", "chain"):
        image = ImageEnhance.Brightness(image).enhance(1.35)
    return image.resize((16, 16), Image.Resampling.NEAREST)


def parallelogram(canvas, surface, points, brightness=1):
    minimum_x = math.floor(min(point[0] for point in points))
    minimum_y = math.floor(min(point[1] for point in points))
    maximum_x = math.ceil(max(point[0] for point in points)) + 1
    maximum_y = math.ceil(max(point[1] for point in points)) + 1
    width, height = maximum_x - minimum_x, maximum_y - minimum_y
    anchor_x, anchor_y = points[0][0] - minimum_x, points[0][1] - minimum_y
    first_x = (points[1][0] - points[0][0]) / 16
    first_y = (points[1][1] - points[0][1]) / 16
    second_x = (points[3][0] - points[0][0]) / 16
    second_y = (points[3][1] - points[0][1]) / 16
    determinant = first_x * second_y - second_x * first_y
    if abs(determinant) < 0.001:
        return
    coefficients = (second_y / determinant, -second_x / determinant, (-second_y * anchor_x + second_x * anchor_y) / determinant, -first_y / determinant, first_x / determinant, (first_y * anchor_x - first_x * anchor_y) / determinant)
    warped = surface.transform((width, height), Image.Transform.AFFINE, coefficients, Image.Resampling.NEAREST)
    if brightness != 1:
        warped = ImageEnhance.Brightness(warped).enhance(brightness)
    mask = Image.new("L", (width, height))
    ImageDraw.Draw(mask).polygon([(point[0] - minimum_x, point[1] - minimum_y) for point in points], fill=255)
    warped.putalpha(Image.composite(warped.getchannel("A"), Image.new("L", (width, height)), mask))
    canvas.alpha_composite(warped, (minimum_x, minimum_y))


def visible_blocks():
    visible = {}
    for position, state in BLOCKS.items():
        block_x, block_y, block_z = position
        if block_y >= 13 or (block_z >= 12 and block_y >= roof_height(block_x, block_z) - 1):
            continue
        if block_y >= 5 and (block_x <= 1 or (12 <= block_z <= 13 and not 13 <= block_x <= 21)):
            continue
        if block_z <= 11 and block_y >= 9 and (block_x <= 12 or block_x >= 22 or block_z <= 8):
            continue
        visible[position] = state
    return visible


def isometric(size=(1880, 1330)):
    visible = visible_blocks()
    scale = min((size[0] - 100) / 82, (size[1] - 90) / 52)
    projected = [(block_x - block_z, -(block_x + block_z) / 2 - block_y) for block_x, block_y, block_z in visible]
    minimum_x = min(point[0] for point in projected) * scale
    maximum_x = max(point[0] for point in projected) * scale
    minimum_y = min(point[1] for point in projected) * scale - scale
    maximum_y = max(point[1] for point in projected) * scale + scale
    offset_x = (size[0] - maximum_x - minimum_x) / 2
    offset_y = (size[1] - maximum_y - minimum_y) / 2
    canvas = Image.new("RGBA", size, BACKGROUND)

    def project(block_x, block_y, block_z):
        return offset_x + (block_x - block_z) * scale, offset_y - (block_x + block_z) * scale / 2 - block_y * scale

    def box(block_x, block_y, block_z, bounds, name, exposed):
        left, bottom, front, right, top, back = bounds
        if exposed[0]:
            points = [project(block_x + left, block_y + top, block_z + front), project(block_x + right, block_y + top, block_z + front), project(block_x + right, block_y + bottom, block_z + front), project(block_x + left, block_y + bottom, block_z + front)]
            parallelogram(canvas, texture(name), points, 0.86)
        if exposed[1]:
            points = [project(block_x + left, block_y + top, block_z + back), project(block_x + left, block_y + top, block_z + front), project(block_x + left, block_y + bottom, block_z + front), project(block_x + left, block_y + bottom, block_z + back)]
            parallelogram(canvas, texture(name), points, 0.66)
        if exposed[2]:
            points = [project(block_x + left, block_y + top, block_z + back), project(block_x + right, block_y + top, block_z + back), project(block_x + right, block_y + top, block_z + front), project(block_x + left, block_y + top, block_z + front)]
            parallelogram(canvas, texture(name, "top"), points, 1.14)

    transparent = {"fire", "chain", "lantern", "iron_bars"}
    for (block_x, block_y, block_z), state in sorted(visible.items(), key=lambda item: (-item[0][0] - item[0][2], item[0][1], item[0][0])):
        name = block_name(state)
        exposed = []
        for neighbor in ((block_x, block_y, block_z - 1), (block_x - 1, block_y, block_z), (block_x, block_y + 1, block_z)):
            exposed.append(neighbor not in visible or block_name(visible[neighbor]) in transparent)
        if name == "fire":
            points = [project(block_x, block_y + 1.4, block_z + 0.5), project(block_x + 1, block_y + 1.4, block_z + 0.5), project(block_x + 1, block_y, block_z + 0.5), project(block_x, block_y, block_z + 0.5)]
            parallelogram(canvas, texture(name), points)
            points = [project(block_x + 0.5, block_y + 1.4, block_z + 1), project(block_x + 0.5, block_y + 1.4, block_z), project(block_x + 0.5, block_y, block_z), project(block_x + 0.5, block_y, block_z + 1)]
            parallelogram(canvas, texture(name), points)
        elif name == "chain":
            box(block_x, block_y, block_z, (0.44, 0, 0.44, 0.56, 1, 0.56), "polished_blackstone", (True, True, True))
        elif name == "lantern":
            box(block_x, block_y, block_z, (0.30, 0.20, 0.30, 0.70, 0.82, 0.70), "gold_block", (True, True, True))
            box(block_x, block_y, block_z, (0.26, 0.13, 0.26, 0.74, 0.24, 0.74), "polished_blackstone", (True, True, True))
            box(block_x, block_y, block_z, (0.26, 0.82, 0.26, 0.74, 0.94, 0.74), "polished_blackstone", (True, True, True))
        elif name == "iron_bars":
            box(block_x, block_y, block_z, (0, 0, 0.46, 1, 1, 0.54), name, (True, True, True))
        elif name.endswith("_slab"):
            box(block_x, block_y, block_z, (0, 0, 0, 1, 0.5, 1), name, (True, True, True))
        elif name.endswith("_stairs"):
            box(block_x, block_y, block_z, (0, 0, 0, 1, 0.5, 1), name, (True, True, True))
            upper = {"south": (0, 0.5, 0.5, 1, 1, 1), "north": (0, 0.5, 0, 1, 1, 0.5), "east": (0.5, 0.5, 0, 1, 1, 1), "west": (0, 0.5, 0, 0.5, 1, 1)}[state["Properties"]["facing"]]
            box(block_x, block_y, block_z, upper, name, (True, True, True))
        else:
            box(block_x, block_y, block_z, (0, 0, 0, 1, 0.88 if name == "lava" else 1, 1), name, exposed)
    return canvas.convert("RGB")


def topdown():
    scale, margin_x, margin_y = 23, 95, 120
    canvas = Image.new("RGB", (1200, 1300), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((46, 26), "黑曜试炼 / 平面布局", font=font(35), fill=GOLD)
    draw.text((48, 78), "北 ↑   入口向 -Z   /   每格 1 方块", font=font(20), fill=MUTED)
    for block_x in range(35):
        for block_z in range(47):
            column = [(position[1], state) for position, state in BLOCKS.items() if position[0] == block_x and position[2] == block_z and position[1] <= 11]
            if not column:
                continue
            block_y, state = max(column, key=lambda item: item[0])
            name = block_name(state)
            if name in ("fire", "chain", "lantern", "iron_bars"):
                surface = Image.new("RGBA", (scale, scale), "#ffa53c" if name in ("fire", "lantern") else "#6c6578")
            else:
                surface = texture(name, "top").resize((scale, scale), Image.Resampling.NEAREST)
            canvas.paste(surface, (margin_x + block_x * scale, margin_y + block_z * scale), surface)
    markers = [(17, 7, "A"), (17, 17, "B"), (23, 24, "C"), (17, 29, "D"), (4, 29, "E"), (17, 42, "F")]
    for block_x, block_z, label in markers:
        center_x, center_y = margin_x + (block_x + 0.5) * scale, margin_y + (block_z + 0.5) * scale
        draw.ellipse((center_x - 15, center_y - 15, center_x + 15, center_y + 15), fill="#17111e", outline=GOLD, width=2)
        draw.text((center_x - 7, center_y - 13), label, font=font(21), fill=GOLD)
    labels = [("A", "符文闸门前室"), ("B", "五格宽跨桥"), ("C", "切角抬高竞技台"), ("D", "金边永燃火盆"), ("E", "双侧补给龛"), ("F", "后殿奖励龛")]
    for index, (label, text) in enumerate(labels):
        baseline = 300 + index * 92
        draw.text((950, baseline), label, font=font(28), fill=GOLD)
        draw.text((950, baseline + 39), text, font=font(18), fill=TEXT)
    draw.text((48, 1240), "35 × 47 格占地；高度 18 格。显示剖顶后的上表面，非游戏截图。", font=font(19), fill=MUTED)
    return canvas


def gate_front():
    scale = 56
    canvas = Image.new("RGBA", (1120, 785), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((35, 22), "试炼门 / 抬起式铁闸 · 紫晶符文", font=font(30), fill=GOLD)
    for block_x in range(8, 27):
        for block_y in range(13):
            candidates = [(position[2], state) for position, state in BLOCKS.items() if position[0] == block_x and position[1] == block_y and 9 <= position[2] <= 14]
            if not candidates:
                continue
            block_z, state = min(candidates, key=lambda item: item[0])
            name = block_name(state)
            surface = texture(name).resize((scale, scale), Image.Resampling.NEAREST)
            canvas.alpha_composite(surface, (28 + (block_x - 8) * scale, 82 + (11 - block_y) * scale))
    draw.rectangle((0, 735, 1120, 785), fill=BACKGROUND)
    draw.text((38, 747), "门下保留 7 格宽通行口；铁闸是固定装饰，不含开闭逻辑。", font=font(20), fill=MUTED)
    return canvas.convert("RGB")


def design_sheet(hero, plan, gate):
    canvas = Image.new("RGB", (2100, 1640), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((54, 34), "黑曜试炼", font=font(64), fill=TEXT)
    draw.text((58, 122), "OBSIDIAN TRIAL  /  可放置的 Minecraft 1.20.1 地牢建筑", font=font(25), fill=GOLD)
    draw.line((56, 180, 2044, 180), fill="#65446f", width=2)
    canvas.paste(hero.resize((1420, 1000), Image.Resampling.LANCZOS), (18, 205))
    canvas.paste(plan.resize((600, 650), Image.Resampling.LANCZOS), (1468, 205))
    canvas.paste(gate.resize((600, 420), Image.Resampling.LANCZOS), (1468, 877))
    draw.rounded_rectangle((56, 1270, 1410, 1555), radius=20, fill="#1a1324", outline="#61436e", width=2)
    draw.text((85, 1290), "35 × 47 × 18", font=font(43), fill=GOLD)
    draw.text((87, 1354), "黑曜石穹顶大厅 / 镀金黑石竞技台 / 环形熔岩", font=font(26), fill=TEXT)
    draw.text((87, 1403), "入口前室 → 四桥跨环 → 中央火盆 → 后殿奖励龛", font=font(25), fill=TEXT)
    draw.text((87, 1455), "原版方块 · 纯建筑包 · 不自动刷怪 · 不改现有门与五档火盆", font=font(22), fill=MUTED)
    draw.text((87, 1504), "静态检查：熔岩封边 / 四向通行 / 2 格净高 / 资源与坐标", font=font(22), fill=MUTED)
    draw.text((1476, 1355), "按参考图保留", font=font(26), fill=GOLD)
    for index, line in enumerate(("紫黑 × 橙金配色", "中央高火盆与金箍", "哭泣黑曜石柱面", "符文门楣与铁闸", "四角塔柱和垂链")):
        draw.text((1476, 1404 + index * 33), line, font=font(21), fill=TEXT)
    draw.text((57, 1590), "从最终结构的方块数据绘制，使用原版贴图并提亮；屋顶、近侧墙剖切，灯笼/锁链简化。非实机截图，未调用收费生图 API。", font=font(19), fill=MUTED)
    return canvas


def main():
    global PALETTE, BLOCKS, TEXTURES
    parser = argparse.ArgumentParser()
    parser.add_argument("--textures-jar", type=Path, required=True)
    arguments = parser.parse_args()
    template = json.loads((ROOT / "qa/blueprint.json").read_text(encoding="utf-8"))
    PALETTE = template["palette"]
    BLOCKS = {tuple(entry["pos"]): PALETTE[entry["state"]] for entry in template["blocks"]}
    with zipfile.ZipFile(arguments.textures_jar) as archive:
        TEXTURES = archive
        hero = isometric()
        plan = topdown()
        gate = gate_front()
        images = {"obsidian-trial-isometric.png": hero, "obsidian-trial-floorplan.png": plan, "obsidian-trial-gate.png": gate, "obsidian-trial-design-sheet.png": design_sheet(hero, plan, gate)}
        for name, image in images.items():
            path = ROOT / "preview" / name
            image.save(path)
            print(path)


if __name__ == "__main__":
    main()
