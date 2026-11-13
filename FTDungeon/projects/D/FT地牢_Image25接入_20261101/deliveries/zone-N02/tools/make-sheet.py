import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
FONT = "C:/Windows/Fonts/msyh.ttc"
DIRECTIONS = (("north", "北面"), ("east", "东面"), ("south", "南面"), ("west", "西面"))
records = []
for source in sorted((ROOT / "views").glob("*.png")):
    if "sheet" in source.stem:
        continue
    with Image.open(source) as image:
        image.load()
        if image.size != (1440, 1080):
            raise ValueError(f"Unexpected screenshot dimensions: {source}")
        records.append({"path": str(source.relative_to(ROOT)), "size": list(image.size), "sha256": hashlib.sha256(source.read_bytes()).hexdigest()})
if len(records) != 11:
    raise ValueError("Expected 8 cardinal views plus 3 eye views")
for mode, label in (("interior", "内部四向 · 预览剖切"), ("exterior", "外壳四向 · 完整屋顶")):
    sheet = Image.new("RGB", (2000, 1740), "#121c23")
    draw = ImageDraw.Draw(sheet)
    draw.text((36, 22), "N02 石拱矿车站枢纽 | " + label, font=ImageFont.truetype(FONT, 37), fill="#f1e5c8")
    draw.text((36, 80), "33 × 15 × 31 · 三条固定轨道 · 五格宽步道 · 实际 NBT / Lodestone 渲染", font=ImageFont.truetype(FONT, 23), fill="#a9c8ca")
    for index, (direction, title) in enumerate(DIRECTIONS):
        left = 20 + index % 2 * 990
        top = 130 + index // 2 * 790
        with Image.open(ROOT / "views" / f"{mode}-{direction}.png") as image:
            crop = image.crop((140, 150, 1320, 1020))
            thumbnail = ImageOps.contain(crop, (970, 720), Image.Resampling.LANCZOS)
            sheet.paste(thumbnail, (left + (970 - thumbnail.width) // 2, top + (720 - thumbnail.height) // 2))
        draw.text((left + 24, top + 727), title + " / " + direction.upper(), font=ImageFont.truetype(FONT, 25), fill="#e8d7b3")
    sheet.save(ROOT / "views" / f"{mode}-sheet.png")
with Image.open(ROOT / "preview.png") as image:
    image.load()
    if image.size != (1440, 1080):
        raise ValueError("Preview size differs")
report = {"status": "PASS", "original_images": 11, "source_screenshot_dimensions": [1440, 1080], "preview_decodes": True, "images": records, "sheets": ["views/interior-sheet.png", "views/exterior-sheet.png"], "note": "Contact sheets crop neutral background only; originals remain unchanged. All images are structure software renders, not Minecraft client screenshots."}
(ROOT / "qa/image-decode.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("PASS: 11 screenshots and preview decode; 2 cardinal contact sheets created")
