from pathlib import Path
import json
import zipfile
from io import BytesIO
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[4]
OUT = ROOT / "art/boss_materials/refine/wine/references"
OUT.mkdir(parents=True, exist_ok=True)
ASSETS = ROOT / "src/main/resources/assets/ftboss"
FONT = ImageFont.truetype("C:/Windows/Fonts/consola.ttf", 18)

geo = json.loads((ASSETS / "geo/wine_barrel_boss.geo.json").read_text(encoding="utf-8"))["minecraft:geometry"][0]
summary = []
for bone in geo["bones"]:
    summary.append({"name": bone["name"], "parent": bone.get("parent"), "rotation": bone.get("rotation"), "cubes": bone.get("cubes", [])})
(OUT / "boss_geometry_summary.json").write_text(json.dumps(summary, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
print("Boss parts:", [(bone["name"], len(bone["cubes"])) for bone in summary])
images = []
for relative, label in [("textures/entity/wine_barrel_boss_v2.png", "BOSS: oak / iron / gold / red wax"), ("textures/item/aged_wine_seal.png", "Existing relic: aged wine seal"), ("textures/item/wine_material.png", "Rejected previous wine material")]:
    images.append((Image.open(ASSETS / relative).convert("RGBA"), label))
mods = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_Forge\u2161 v1.5.2/mods")
jar = next(mods.glob("*L_Enders_Cataclysm-3.31.jar"))
with zipfile.ZipFile(jar) as archive:
    matches = [name for name in archive.namelist() if name.startswith("assets/cataclysm/textures/item/") and any(key in name for key in ["ingot", "monstrous", "eye_of", "burning_ashes"]) and name.endswith(".png")]
    for name in matches[:8]:
        payload = archive.read(name)
        images.append((Image.open(BytesIO(payload)).convert("RGBA"), Path(name).stem))
        (OUT / Path(name).name).write_bytes(payload)
    print("Mature ingot/seal references:", matches[:8])
sheet = Image.new("RGB", (1200, ((len(images) + 2) // 3) * 330), "#1b1e25")
draw = ImageDraw.Draw(sheet)
for index, (image, label) in enumerate(images):
    left, top = (index % 3) * 400, (index // 3) * 330
    scale = max(1, min(280 // image.width, 260 // image.height))
    image = image.resize((image.width * scale, image.height * scale), Image.Resampling.NEAREST)
    sheet.paste(image, (left + (400 - image.width) // 2, top + 18), image)
    draw.text((left + 15, top + 290), label, fill="#c8c2ac", font=FONT)
sheet.save(OUT / "reference_sheet.png")
print(OUT / "reference_sheet.png")
