import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
FONT = "C:/Windows/Fonts/msyh.ttc"
directions = (("north", "北面"), ("east", "东面"), ("south", "南面"), ("west", "西面"))
records = []
for path in sorted((ROOT / "views").glob("*.png")):
    with Image.open(path) as source:
        source.load()
        if source.size != (1440, 1000):
            raise ValueError(f"Unexpected image size: {path}")
        records.append({"file": str(path.relative_to(ROOT)), "size": list(source.size), "sha256": hashlib.sha256(path.read_bytes()).hexdigest()})

for mode, title in (("interior", "内部四向 · 仅预览剖切"), ("exterior", "外壳四向 · 完整屋顶")):
    sheet = Image.new("RGB", (2000, 1560), "#13151a")
    draw = ImageDraw.Draw(sheet)
    draw.text((40, 24), f"P01 跳坑跑酷 | {title}", font=ImageFont.truetype(FONT, 38), fill="#f4d9bf")
    draw.text((40, 86), "19 × 14 × 29 · 四次两格跨跳 · 三格落差安全回收 · Lodestone / 实际 NBT 渲染", font=ImageFont.truetype(FONT, 24), fill="#b4b9c0")
    for index, (direction, label) in enumerate(directions):
        left = 24 + (index % 2) * 988
        top = 146 + (index // 2) * 700
        with Image.open(ROOT / "views" / f"{mode}-{direction}.png") as image:
            mask = image.convert("HSV").getchannel("S").point(lambda value: 255 if value > 45 else 0)
            bounds = mask.getbbox()
            if bounds:
                bounds = (max(0, bounds[0] - 50), max(0, bounds[1] - 50), min(image.width, bounds[2] + 50), min(image.height, bounds[3] + 50))
                image = image.crop(bounds)
            thumbnail = ImageOps.contain(image, (964, 620), Image.Resampling.LANCZOS)
            sheet.paste(thumbnail, (left + (964 - thumbnail.width) // 2, top + (620 - thumbnail.height) // 2))
        draw.text((left + 16, top + 638), f"{label} / {direction.upper()}", font=ImageFont.truetype(FONT, 26), fill="#e5c5a6")
    sheet.save(ROOT / "views" / f"{mode}-sheet.png")

report = {"status": "PASS", "checks": {"all_ten_source_pngs_decode": len(records) == 10, "all_source_pngs_1440x1000": True}, "images": records, "sheets": ["views/interior-sheet.png", "views/exterior-sheet.png"], "sheet_crop": "Source screenshots unchanged; sheets crop neutral background and retain geometry for layout."}
if not all(report["checks"].values()):
    raise ValueError("Missing source images")
(ROOT / "qa/image-decode.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("PASS: 10 original screenshots decode; 2 contact sheets written")

