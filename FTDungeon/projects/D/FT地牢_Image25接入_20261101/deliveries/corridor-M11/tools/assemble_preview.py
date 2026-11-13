import argparse
import hashlib
import json
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
RAW = ROOT / "qa/render-raw"
VIEWS = ROOT / "views"
TITLES = {"north": "北向剖视 / 入口 → 远端", "south": "南向剖视 / 出口 → 入口", "west": "西向剖视 / 白骨肋拱与棺龛", "east": "东向剖视 / 蔓生与封护灯槽"}
BACKGROUND = "#0d1519"
TEXT = "#eee6d5"
ACCENT = "#cbac72"


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def card(source, title, subtitle):
    picture = Image.open(source).convert("RGB")
    canvas = Image.new("RGB", (picture.width, picture.height + 136), BACKGROUND)
    canvas.paste(picture, (0, 92))
    draw = ImageDraw.Draw(canvas)
    draw.rectangle((0, 0, 7, 92), fill=ACCENT)
    draw.text((30, 16), title, font=font(30), fill=TEXT)
    draw.text((31, 57), subtitle, font=font(18), fill="#a6b8b6")
    draw.text((30, canvas.height - 32), "最终 room.nbt 实际方块渲染 · 非 AI 成品图 / 非游戏截图 · 骷髅与火焰等特殊显示以游戏为准", font=font(17), fill="#a2aba6")
    return canvas


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--approve", action="store_true")
    arguments = parser.parse_args()
    VIEWS.mkdir(exist_ok=True)
    capture = json.loads((RAW / "capture.json").read_text(encoding="utf-8"))
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    sha256 = digest(ROOT / "room.nbt")
    assert capture["sha256"] == capture["served_nbt_sha256"] == metadata["sha256"] == sha256
    assert not capture["errors"]
    for view in capture["views"]:
        with Image.open(RAW / (view["name"] + ".png")) as picture:
            assert picture.size == (1536, 1024)
            picture.verify()
    card(RAW / "hero.png", "M11 / 蔓生白骨墓廊", "中型直廊 15 × 12 × 40 · 2.5 区块长 · 五格平直主路 · 北南双接口 · 完整顶盖").save(ROOT / "preview.png")
    for direction, title in TITLES.items():
        card(RAW / (direction + ".png"), "M11 / " + title, "仅预览移除屋面与近侧墙壳；保留室内柱拱 / 远侧棺龛；最终 NBT 不剖切").save(VIEWS / (direction + ".png"))
    card(RAW / "exterior.png", "M11 / 完整外壳与双端接口", "最终 NBT 未剖切外观；屋顶 600 格封闭，北南开口保持可穿行").save(VIEWS / "exterior.png")
    sheet = Image.new("RGB", (2400, 1900), BACKGROUND)
    draw = ImageDraw.Draw(sheet)
    draw.text((45, 25), "M11 / 蔓生白骨墓廊 · 四视图 QA", font=font(40), fill=TEXT)
    draw.text((47, 84), "最终 NBT 软件渲染 · 仅移除屋面与近侧墙壳 · 长轴南北贯通 / 无楼梯 / 无转弯 / 无分岔", font=font(23), fill="#a7bbb5")
    for index, (direction, title) in enumerate(TITLES.items()):
        left = 35 + index % 2 * 1180
        top = 142 + index // 2 * 830
        draw.rounded_rectangle((left, top, left + 1150, top + 810), radius=10, fill="#101d21", outline="#34413b", width=2)
        draw.text((left + 22, top + 18), title, font=font(27), fill=ACCENT)
        picture = Image.open(RAW / (direction + ".png")).convert("RGB")
        picture.thumbnail((1114, 744), Image.Resampling.LANCZOS)
        sheet.paste(picture, (left + (1150 - picture.width) // 2, top + 62 + (730 - picture.height) // 2))
    draw.text((45, 1830), "骨块肋拱 / 深色木棺龛 / 苔砖 / 成熟洞穴藤蔓 / 22 骷髅 / 24 燃烛簇 / 36 格玻璃封护熔岩", font=font(23), fill=TEXT)
    draw.text((45, 1863), "图像不是游戏光照验收；原生放置、碰撞、支撑及 400 tick 稳定性见 qa/forge-placement.json。", font=font(20), fill="#a4b3b0")
    sheet.save(ROOT / "four-views.png")
    comparison = Image.new("RGB", (2400, 1010), BACKGROUND)
    compare_draw = ImageDraw.Draw(comparison)
    compare_draw.text((35, 20), "M11 / 原图与方块实现 · 非一比一像素复刻", font=font(34), fill=TEXT)
    for index, (source, title) in enumerate(((Path(metadata["reference"]), "参考 · image2.5 概念图"), (RAW / "hero.png", "交付 · 最终 NBT / Lodestone 实际渲染"))):
        left = 35 + index * 1180
        compare_draw.text((left, 79), title, font=font(25), fill=ACCENT)
        picture = Image.open(source).convert("RGB")
        picture.thumbnail((1150, 805), Image.Resampling.LANCZOS)
        comparison.paste(picture, (left, 127))
    compare_draw.text((35, 935), "保留：骨墓 / 蔓生 / 烛火 / 熔岩辉光 / 重复肋拱与侧棺龛；安全转译：熔岩加玻璃盖，主路保持五格平直。", font=font(22), fill=TEXT)
    compare_draw.text((35, 976), "参考 SHA-256 " + metadata["reference_sha256"], font=font(17), fill="#a4b3b0")
    comparison.save(ROOT / "reference-comparison.png")
    visual_checks = ["中轴一点透视贯通", "四个剖向完整且无遮屏三角伪影", "侧龛与肋拱可读", "蔓生只占边带与高位", "完整外壳保留屋顶与双端口"]
    report = {
        "status": "PASS" if arguments.approve else "PENDING_VISUAL_REVIEW", "sha256": sha256,
        "renderer": capture["renderer"], "source": "room.nbt", "source_records": 7200,
        "served_nbt_sha256": capture["served_nbt_sha256"], "hero": "preview.png", "four_view_sheet": "four-views.png",
        "views": [{**view, "file": "views/" + view["name"] + ".png", "png_sha256": digest(VIEWS / (view["name"] + ".png"))} for view in capture["views"] if view["name"] in TITLES],
        "exterior": "views/exterior.png", "comparison": "reference-comparison.png", "browser_errors": [],
        "cutaway_rule": "Preview copies only: omit y>=10 roof and y>=3 nearest side wall (x<=2 or x>=12) and nearest end (z=0 or z=39); final NBT remains unchanged",
        "lighting": "Illustrative software lighting, warm hero and neutral overview; not Minecraft skylight, block-light or shader validation",
        "special_model_limitations": "Lodestone special block/entity models and candle particle display may differ from the game; native skull block entities, rotations, support and lit candle states verified separately",
        "visual_review": {"performed": arguments.approve, "checks": visual_checks if arguments.approve else []},
        "timestamp": datetime.now(timezone(timedelta(hours=8))).isoformat(),
        "not_ai_substitution": True, "not_gameplay_screenshot": True,
    }
    save_json(ROOT / "qa/render.json", report)
    print(json.dumps({"status": report["status"], "preview": str(ROOT / "preview.png"), "four_views": str(ROOT / "four-views.png"), "sha256": sha256}, ensure_ascii=False))


if __name__ == "__main__":
    main()
