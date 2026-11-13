import hashlib
import json
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont, ImageOps

ROOT = Path(__file__).resolve().parents[1]
FONT = "C:/Windows/Fonts/msyh.ttc"
directions = (("north", "北向 · 从东北看"), ("east", "东向 · 从东南看"), ("south", "南向 · 从西南看"), ("west", "西向 · 从西北看"))
records = []
for source_path in sorted((ROOT / "views").glob("*.png")):
    if "sheet" in source_path.stem:
        continue
    with Image.open(source_path) as image:
        image.load()
        if image.size != (1440, 1000):
            raise ValueError(f"Unexpected screenshot dimensions: {source_path}")
        records.append({"file": str(source_path.relative_to(ROOT)), "size": list(image.size), "sha256": hashlib.sha256(source_path.read_bytes()).hexdigest()})
for mode, title in (("interior", "内部四向 / 仅预览剖切"), ("exterior", "完整外壳四向 / 双层屋顶")):
    sheet = Image.new("RGB", (2000, 1590), "#121b18")
    draw = ImageDraw.Draw(sheet)
    draw.text((35, 22), f"C03 蔓生角落神龛 | {title}", font=ImageFont.truetype(FONT, 37), fill="#e0e5c6")
    draw.text((35, 82), "19 × 12 × 19 · 北入东出 · 5格宽平路 · 实际 NBT / Lodestone 软件渲染", font=ImageFont.truetype(FONT, 24), fill="#a9b5a4")
    for index, (direction, label) in enumerate(directions):
        left = 16 + (index % 2) * 992
        top = 140 + (index // 2) * 710
        with Image.open(ROOT / "views" / f"{mode}-{direction}.png") as image:
            thumbnail = ImageOps.contain(image.convert("RGB"), (972, 660), Image.Resampling.LANCZOS)
            sheet.paste(thumbnail, (left + (972 - thumbnail.width) // 2, top + (660 - thumbnail.height) // 2))
        draw.text((left + 20, top + 660), label, font=ImageFont.truetype(FONT, 25), fill="#c9d7b7")
    sheet.save(ROOT / "views" / f"{mode}-sheet.png")
if len(records) != 11:
    raise ValueError(f"Expected 11 original screenshots, got {len(records)}")
(ROOT / "qa/image-decode.json").write_text(json.dumps({"status": "PASS", "source_images": records, "all_11_decode": True, "sheets": ["views/interior-sheet.png", "views/exterior-sheet.png"]}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("PASS: 11 original screenshots decode, 2 four-view sheets written")

