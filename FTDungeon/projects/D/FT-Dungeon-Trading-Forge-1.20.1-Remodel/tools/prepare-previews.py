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
    cutout_faces = 0
    budget = json.loads((ROOT / "model_sources/model-report.json").read_text(encoding="utf-8"))
    cutout = set(budget["cutout_components"])
    cleanup_baseline = ROOT / "model_sources/baseline/1.1.0-陈设清理前"
    remaining_names = {element["name"] for element in model["elements"]}
    if any(name.startswith(("Coin pile /", "Emerald /", "Old key /")) for name in remaining_names):
        raise RuntimeError("Removed decorations returned")
    if "Candle / brass saucer" in remaining_names or not {"Candle / melted wax pool", "Candle / wax spill"} <= remaining_names:
        raise RuntimeError("Candle base must use the new melted wax geometry")
    rejected_colors = {(139, 131, 110), (108, 107, 90), (159, 162, 150), (160, 162, 151)}
    for rectangle in [(0, 0, 64, 32), (64, 8, 128, 32)]:
        colors = {color[:3] for _, color in texture.crop(rectangle).getcolors(16384)}
        if rejected_colors & colors:
            raise RuntimeError("Removed dust or key colors remain on the wooden surfaces")
    cleanup = {"version": budget["version"], "passed": True, "removed_decorations": ["coins", "emerald", "key", "brass candle base"],
               "candle_base": "two thin connected ivory wax pools", "wooden_surfaces_free_of_old_dust_and_key_colors": True,
               "footprint_blocks": [2, 1], "prop_proportions_preserved": True}
    (QA / "decoration-cleanup-check.json").write_text(json.dumps(cleanup, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for element in model["elements"]:
        for face in element["faces"].values():
            first_x, first_y, second_x, second_y = face["uv"]
            bounds = (math.floor(min(first_x, second_x) * texture.width / 16), math.floor(min(first_y, second_y) * texture.height / 16),
                      math.ceil(max(first_x, second_x) * texture.width / 16), math.ceil(max(first_y, second_y) * texture.height / 16))
            alpha = texture.crop(bounds).getchannel("A")
            if element["name"] in cutout:
                if alpha.getextrema() != (0, 255) or set(alpha.tobytes()) != {0, 255}:
                    raise RuntimeError("Cobweb must use binary cutout alpha")
                cutout_faces += 1
                continue
            if alpha.getextrema() != (255, 255):
                raise RuntimeError(f"Visible UV samples transparency: {element['name']}")
            checked_faces += 1
    sheet(["front", "back", "left", "right", "top", "bottom", "oblique", "rear-oblique"], "view", "all-views.png")
    sheet(["front", "back", "left", "right", "top", "bottom", "oblique", "rear-oblique"], "assembled", "all-assembled-views.png")
    sheet(["gui", "ground", "fixed", "thirdperson_righthand", "thirdperson_lefthand", "firstperson_righthand", "firstperson_lefthand"], "display", "all-displays.png")
    lowest = [16.0, 16.0, 16.0]
    highest = [0.0, 0.0, 0.0]
    for element in model["elements"]:
        rotation = element.get("rotation")
        for column in [element["from"][0], element["to"][0]]:
            for row in [element["from"][1], element["to"][1]]:
                for depth in [element["from"][2], element["to"][2]]:
                    point = [column, row, depth]
                    if rotation:
                        if rotation["axis"] != "y":
                            raise RuntimeError("Bounds verifier only expects this model's Y-axis rotations")
                        origin = rotation["origin"]
                        angle = math.radians(rotation["angle"])
                        relative_x, relative_z = point[0] - origin[0], point[2] - origin[2]
                        point[0] = origin[0] + relative_x * math.cos(angle) + relative_z * math.sin(angle)
                        point[2] = origin[2] - relative_x * math.sin(angle) + relative_z * math.cos(angle)
                    for axis in range(3):
                        lowest[axis] = min(lowest[axis], point[axis])
                        highest[axis] = max(highest[axis], point[axis])
    if lowest[0] < -8 or highest[0] > 24 or any(lowest[axis] < 0 or highest[axis] > 16 for axis in [1, 2]):
        raise RuntimeError(f"Rotated geometry exceeds the declared two-block volume: {lowest}, {highest}")
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
        "binary_cutout_faces_checked": cutout_faces,
        "rotated_model_bounds": {"from": lowest, "to": highest},
        "client_tested": False,
    }
    (QA / "roundtrip-render-check.json").write_text(json.dumps(report, indent=2) + "\n", encoding="utf-8")
    assembly = {}
    for name in ["front", "back", "left", "right", "top", "bottom", "oblique", "rear-oblique"]:
        source_view = Image.open(QA / f"view-{name}.png").convert("RGBA")
        assembled_view = Image.open(QA / f"assembled-{name}.png").convert("RGBA")
        if source_view.size != assembled_view.size:
            raise RuntimeError("Assembly camera size differs")
        source_bytes, assembled_bytes = source_view.tobytes(), assembled_view.tobytes()
        differing = sum(source_bytes[index:index + 4] != assembled_bytes[index:index + 4] for index in range(0, len(source_bytes), 4))
        foreground = sum(source_bytes[index + 3] > 0 or assembled_bytes[index + 3] > 0 for index in range(0, len(source_bytes), 4))
        difference = ImageChops.difference(source_view, assembled_view)
        difference.convert("RGB").point(lambda value: min(255, value * 8)).save(QA / f"assembly-difference-{name}.png")
        assembly[name] = {"strict_pixel_equal": differing == 0, "differing_rgba_pixels": differing,
                          "foreground_pixels": foreground, "foreground_difference_fraction": differing / max(1, foreground)}
    (QA / "assembly-render-check.json").write_text(json.dumps({"views": assembly, "strict_all_views_equal": all(value["strict_pixel_equal"] for value in assembly.values()),
        "review": "All eight assembled views and difference maps reviewed; strict equality is not claimed. Surface/UV splitting separately checked.",
        "client_render_tested": False}, indent=2) + "\n", encoding="utf-8")
    if not equal:
        raise RuntimeError("Reopened Blockbench render differs from the baseline")
    canvas = Image.new("RGBA", (1500, 1030), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((56, 38), "地牢交易长桌 · 两格宽 / 米白融蜡", font=font(44), fill=GOLD)
    draw.text((58, 105), "FT DUNGEON TRADING 1.2.0  /  FORGE 1.20.1", font=font(20), fill=TEXT)
    draw.rounded_rectangle((40, 158, 990, 875), 16, fill=PANEL)
    place(canvas, QA / "view-oblique.png", (68, 172, 964, 860))
    draw.rounded_rectangle((1010, 158, 1460, 502), 16, fill=PANEL)
    place(canvas, QA / "view-front.png", (1030, 194, 1440, 480))
    draw.text((1036, 170), "正面 · 铁包角 / 蛛网 / 木架", font=font(19), fill=GOLD)
    draw.rounded_rectangle((1010, 520, 1460, 875), 16, fill=PANEL)
    place(canvas, QA / "view-top.png", (1030, 556, 1440, 852))
    draw.text((1036, 533), "俯视 · 药瓶 / 卷轴 / 地牢杂货", font=font(19), fill=GOLD)
    draw.text((56, 902), f"编辑 {budget['components']} 组件 / {budget['declared_faces']} 面  ·  放置 58 组件 / 294 面  ·  128 × 128 图集", font=font(22), fill=TEXT)
    draw.text((56, 944), "右键查看完整报价 → 再次右键确认购买   |   付款与商品均可配置", font=font(23), fill=GOLD)
    draw.text((56, 990), "Blockbench 实际模型渲染，非游戏截图。杂货为装饰；真正商品由交易接口动态显示。", font=font(17), fill=(156, 167, 178, 255))
    canvas.convert("RGB").save(ROOT / "model_sources/地牢交易桌预览.png")
    comparison = Image.new("RGBA", (1600, 850), BACKGROUND)
    compare_draw = ImageDraw.Draw(comparison)
    compare_draw.text((50, 20), "1.0.0 · 旧版", font=font(30), fill=GOLD)
    compare_draw.text((845, 20), "1.2.0 · 两格长桌", font=font(30), fill=GOLD)
    previous = Image.open(ROOT / "model_sources/baseline/旧版预览.png")
    previous.crop((40, 158, 990, 875)).save(QA / "previous-model-crop.png")
    place(comparison, QA / "previous-model-crop.png", (25, 90, 785, 790))
    place(comparison, QA / "view-oblique.png", (815, 90, 1575, 790))
    compare_draw.text((50, 808), "新旧造型对照（非像素保真优化；本轮主动改变造型、配色与陈设）", font=font(20), fill=TEXT)
    comparison.convert("RGB").save(ROOT / "model_sources/新旧模型对照.png")
    pixel_comparison = Image.new("RGBA", (1600, 850), BACKGROUND)
    pixel_draw = ImageDraw.Draw(pixel_comparison)
    pixel_draw.text((45, 20), "细像素候选 · 84 组件 / 256² 图集", font=font(29), fill=GOLD)
    pixel_draw.text((835, 20), f"当前精简 · {budget['components']} 组件 / 128² 图集", font=font(29), fill=GOLD)
    place(pixel_comparison, ROOT / "model_sources/baseline/高精度候选-未交付/细像素候选预览.png", (25, 90, 785, 785))
    place(pixel_comparison, QA / "view-oblique.png", (815, 90, 1575, 785))
    pixel_draw.text((45, 805), "造型对照，各图适配画幅：保留粗像素，桌架两格宽，摆件不横向拉伸。", font=font(20), fill=TEXT)
    pixel_comparison.convert("RGB").save(ROOT / "model_sources/像素与体块优化对照.png")
    cleanup_comparison = Image.new("RGBA", (1600, 850), BACKGROUND)
    cleanup_draw = ImageDraw.Draw(cleanup_comparison)
    cleanup_draw.text((45, 20), "1.1.0 · 清理前", font=font(30), fill=GOLD)
    cleanup_draw.text((835, 20), "1.2.0 · 融蜡长桌", font=font(30), fill=GOLD)
    place(cleanup_comparison, cleanup_baseline / "view-oblique.png", (25, 90, 785, 785))
    place(cleanup_comparison, QA / "view-oblique.png", (815, 90, 1575, 785))
    cleanup_draw.text((45, 805), "移除钱币、宝石、钥匙和灰斑；烛底改米白融蜡，台面扩为两格宽；各图适配画幅。", font=font(20), fill=TEXT)
    cleanup_comparison.convert("RGB").save(ROOT / "model_sources/陈设清理前后对照.png")
    wide = Image.new("RGBA", (1600, 850), BACKGROUND)
    wide_draw = ImageDraw.Draw(wide)
    wide_draw.text((45, 20), "窄桌候选 · 未发布", font=font(30), fill=GOLD)
    wide_draw.text((835, 20), "1.2.0 · 两格宽长桌", font=font(30), fill=GOLD)
    place(wide, ROOT / "model_sources/baseline/1.1.1-未发布窄桌/view-oblique.png", (25, 90, 785, 785))
    place(wide, QA / "view-oblique.png", (815, 90, 1575, 785))
    wide_draw.text((45, 805), "两格宽 × 一格深；烛台改融蜡，摆件保留原比例。分别适配画幅，并非同尺度照片。", font=font(20), fill=TEXT)
    wide.convert("RGB").save(ROOT / "model_sources/两格长桌与融蜡对照.png")
    print(json.dumps(report))


if __name__ == "__main__":
    main()
