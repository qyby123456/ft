import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps


ROOT = Path(__file__).resolve().parents[1]
ORDER = ("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")
BACKGROUND = "#0d131a"
PANEL = "#151f2b"
TEXT = "#e7eef7"
MUTED = "#96aac0"


def font(size):
    return ImageFont.truetype("C:/Windows/Fonts/msyh.ttc", size)


def main():
    width, height = 2460, 2150
    canvas = Image.new("RGB", (width, height), BACKGROUND)
    draw = ImageDraw.Draw(canvas)
    draw.text((48, 28), "FT 地牢 · image2.5 设计落地方块结构", font=font(43), fill=TEXT)
    draw.text((50, 95), "九间实际 NBT 软件预览 / 独立缩放 / 剖切仅用于展示，非游戏截图", font=font(25), fill=MUTED)
    for index, identity in enumerate(ORDER):
        folder = ROOT / "incoming" / identity
        metadata = json.loads((folder / "room.json").read_text(encoding="utf-8-sig"))
        left = 40 + index % 3 * 805
        top = 160 + index // 3 * 635
        accent = "#ffba7c" if identity.startswith("L") else "#f0d480" if identity.startswith("M") else "#83cbe7"
        draw.rounded_rectangle((left, top, left + 775, top + 610), radius=12, fill=PANEL, outline="#34485d", width=2)
        with Image.open(folder / "preview.png") as image:
            preview = ImageOps.contain(image.convert("RGB"), (745, 490), Image.Resampling.LANCZOS)
        canvas.paste(preview, (left + 15 + (745 - preview.width) // 2, top + 15 + (490 - preview.height) // 2))
        draw.text((left + 22, top + 516), identity + "  " + metadata["name"], font=font(28), fill=accent)
        size = metadata["size"]
        draw.text((left + 22, top + 562), f"{size[0]} × {size[2]} 占地 / 高 {size[1]} / 北侧三格接口", font=font(22), fill=MUTED)
    draw.text((48, 2090), "结构、道路与保护规则的最终验收以 integration/ 及 qa/ 报告为准；不把概念图或软件预览当成实机验证。", font=font(23), fill=MUTED)
    (ROOT / "preview").mkdir(exist_ok=True)
    canvas.save(ROOT / "preview/nine-room-delivery-sheet.png")
    print(str(ROOT / "preview/nine-room-delivery-sheet.png"))


if __name__ == "__main__":
    main()
