import json
import math
from pathlib import Path
from PIL import Image, ImageChops, ImageDraw, ImageFont


ROOT = Path(__file__).resolve().parents[1]
QA = ROOT / "model_sources/qa"
FONT = "C:/Windows/Fonts/msyh.ttc"
BACKGROUND = (23, 28, 35, 255)
PANEL = (31, 38, 47, 255)
GOLD = (218, 183, 111, 255)
TEXT = (220, 226, 228, 255)


def font(size):
    return ImageFont.truetype(FONT, size)


def place(canvas, path, rectangle):
    image = Image.open(path).convert("RGBA")
    bounds = image.getchannel("A").getbbox()
    if bounds:
        image = image.crop(bounds)
    width = rectangle[2] - rectangle[0]
    height = rectangle[3] - rectangle[1]
    image.thumbnail((width, height), Image.Resampling.LANCZOS)
    canvas.alpha_composite(image, (rectangle[0] + (width - image.width) // 2, rectangle[1] + (height - image.height) // 2))


def sheet(names, prefix, output):
    canvas = Image.new("RGBA", (1600, 960), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    for index, name in enumerate(names):
        left = index % 4 * 400
        top = index // 4 * 480
        draw.rounded_rectangle((left + 10, top + 10, left + 390, top + 470), 10, fill=PANEL)
        place(canvas, QA / f"{prefix}-{name}.png", (left + 22, top + 55, left + 378, top + 445))
        draw.text((left + 26, top + 20), name.replace("_", " "), font=font(17), fill=GOLD)
    canvas.convert("RGB").save(QA / output)


def main():
    assets = ROOT / "src/main/resources/assets/ftdungeontrading"
    model = json.loads((assets / "models/block/trading_table.json").read_text(encoding="utf-8"))
    texture = Image.open(assets / "textures/block/trading_table.png").convert("RGBA")
    checked_faces = 0
    for element in model["elements"]:
        for face in element["faces"].values():
            first_x, first_y, second_x, second_y = face["uv"]
            bounds = (math.floor(min(first_x, second_x) * 8), math.floor(min(first_y, second_y) * 8),
                      math.ceil(max(first_x, second_x) * 8), math.ceil(max(first_y, second_y) * 8))
            if texture.crop(bounds).getchannel("A").getextrema() != (255, 255):
                raise RuntimeError(f"Visible UV samples transparency: {element['name']}")
            checked_faces += 1
    sheet(["front", "back", "left", "right", "top", "bottom", "oblique", "rear-oblique"], "view", "all-views.png")
    sheet(["gui", "ground", "fixed", "thirdperson_righthand", "thirdperson_lefthand", "firstperson_righthand", "firstperson_lefthand"], "display", "all-displays.png")
    baseline = Image.open(QA / "view-oblique.png").convert("RGBA")
    reopened = Image.open(QA / "view-reopened.png").convert("RGBA")
    equal = baseline.size == reopened.size and baseline.tobytes() == reopened.tobytes()
    difference = ImageChops.difference(baseline, reopened) if baseline.size == reopened.size else None
    report = {
        "bbmodel_roundtrip_pixel_equal": equal,
        "baseline_size": baseline.size,
        "reopened_size": reopened.size,
        "difference_bbox": difference.getbbox() if difference else None,
        "fully_opaque_face_uvs_checked": checked_faces,
        "client_tested": False,
    }
    (QA / "roundtrip-render-check.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    if not equal:
        raise RuntimeError("Reopened Blockbench render differs from the baseline")
    canvas = Image.new("RGBA", (1500, 1030), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((56, 38), "暮铁契约 · 地牢交易桌", font=font(44), fill=GOLD)
    draw.text((58, 105), "FT DUNGEON TRADING  /  FORGE 1.20.1", font=font(20), fill=TEXT)
    draw.rounded_rectangle((40, 158, 990, 875), 16, fill=PANEL)
    place(canvas, QA / "view-oblique.png", (68, 172, 964, 860))
    draw.rounded_rectangle((1010, 158, 1460, 502), 16, fill=PANEL)
    place(canvas, QA / "view-front.png", (1030, 194, 1440, 480))
    draw.text((1036, 170), "正面 · 分区交易标识", font=font(19), fill=GOLD)
    draw.rounded_rectangle((1010, 520, 1460, 875), 16, fill=PANEL)
    place(canvas, QA / "view-top.png", (1030, 556, 1440, 852))
    draw.text((1036, 533), "俯视 · 双托盘 / 账本 / 封蜡", font=font(19), fill=GOLD)
    draw.text((56, 902), "57 组件  ·  290 声明面  ·  128 × 128 原创图集  ·  可编辑 Blockbench 工程", font=font(22), fill=TEXT)
    draw.text((56, 944), "右键查看完整报价 → 再次右键确认购买   |   付款与商品均可配置", font=font(23), fill=GOLD)
    draw.text((56, 990), "桌体为 Blockbench 实际渲染；商品预览由模组运行时显示。本图不是游戏客户端截图。", font=font(17), fill=(156, 167, 178, 255))
    canvas.convert("RGB").save(ROOT / "model_sources/地牢交易桌预览.png")
    print(json.dumps(report))


if __name__ == "__main__":
    main()
