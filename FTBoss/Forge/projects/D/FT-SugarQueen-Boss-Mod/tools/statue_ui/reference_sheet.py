import hashlib
import io
import json
import zipfile
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUTPUT = ROOT / "_work/statue-ui-restyle-20261024/themes"
REFERENCES = [
    ("Sugar queen / crystal crown", "sugar_queen_v3.png", "statue_sugar_queen.png"),
    ("Butcher / cleaver and iron", "butcher_v3.png", "statue_butcher.png"),
    ("Saya / camellia and thorns", "tea_alex.png", "statue_saya.png"),
    ("Wine barrel / oak and brass", "wine_barrel_boss_v2.png", "statue_wine_barrel.png"),
    ("Rat swarm / cheese and tail", "rat_boss.png", "statue_rat_swarm.png"),
    ("Defect / circuits and coils", "defect_core.png", "statue_defect.png"),
    ("Compost / earth and spores", "compost_superman.png", "statue_compost.png"),
    ("Apple / red fruit and leaves", "apple_boss.png", "statue_apple.png"),
    ("Tide fish / fins and pearls", "tide_fish_boss.png", "statue_tide_fish.png"),
    ("Benger / sage and tawny", "benger.png", "statue_benger.png"),
]


def main():
    OUTPUT.mkdir(parents=True, exist_ok=True)
    sheet = Image.new("RGB", (1080, 850), "#10151c")
    draw = ImageDraw.Draw(sheet)
    font = ImageFont.truetype("C:/Windows/Fonts/consola.ttf", 14)
    baseline = zipfile.ZipFile(ROOT / "_work/statue-ui-restyle-20261024/baseline.jar")
    facts = []
    for index, (title, entity, statue) in enumerate(REFERENCES):
        origin_x, origin_y = (index % 2) * 540, (index // 2) * 170
        draw.text((origin_x + 12, origin_y + 9), title, font=font, fill="#e8e8df")
        for offset, (folder, filename) in enumerate((("entity", entity), ("block", statue))):
            entry = "assets/ftboss/textures/" + folder + "/" + filename
            data = baseline.read(entry)
            source_data = (ROOT / "src/main/resources" / entry).read_bytes()
            facts.append({"entry": entry, "baseline_sha256": hashlib.sha256(data).hexdigest(), "source_matches_baseline": data == source_data})
            image = Image.open(io.BytesIO(data)).convert("RGBA")
            factor = min(246 / image.width, 126 / image.height)
            image = image.resize((round(image.width * factor), round(image.height * factor)), Image.Resampling.NEAREST)
            sheet.paste(image, (origin_x + 12 + offset * 264, origin_y + 33), image)
    target = OUTPUT / "runtime_texture_references.png"
    sheet.save(target)
    (OUTPUT / "reference_facts.json").write_text(json.dumps(facts, indent=2) + "\n", encoding="utf-8")
    print(target)


if __name__ == "__main__":
    main()
