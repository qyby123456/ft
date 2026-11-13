import io
import json
from pathlib import Path
import zipfile
from PIL import Image, ImageDraw


ROOT = Path(__file__).resolve().parents[1]
MODS = Path.home() / "AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods"
QA = ROOT / "model_sources/qa"


def main():
    cases = [
        ("[精巧手艺] handcrafted-forge-1.20.1-3.0.6.jar", "assets/handcrafted/models/block/desk.json", "assets/handcrafted/textures/block/table/desk/dark_oak_desk.png"),
        ("[别样家具] another_furniture-forge-1.20.1-3.0.4.jar", "assets/another_furniture/models/block/template/table_top.json", "assets/another_furniture/textures/block/table/dark_oak_top.png"),
        ("[家具]letsdo-furniture-forge-1.0.4.jar", "assets/furniture/models/block/oak_desk_single.json", "assets/furniture/textures/block/dark_oak_desk.png"),
    ]
    report = []
    preview = Image.new("RGB", (900, 400), (35, 38, 42))
    draw = ImageDraw.Draw(preview)
    for index, (jar_name, model_path, texture_path) in enumerate(cases):
        with zipfile.ZipFile(MODS / jar_name) as archive:
            model = json.loads(archive.read(model_path))
            texture = Image.open(io.BytesIO(archive.read(texture_path))).convert("RGBA")
            elements = model.get("elements", [])
            record = {"jar": str(MODS / jar_name), "model": model_path, "texture": texture_path,
                      "texture_size": list(texture.size), "parent": model.get("parent"), "elements": len(elements),
                      "declared_faces": sum(len(element.get("faces", {})) for element in elements),
                      "first_element": elements[0] if elements else None,
                      "use": "Local technical study of load-bearing structure and actual UV only; no third-party art redistributed."}
            if index == 1:
                leg = json.loads(archive.read("assets/another_furniture/models/block/template/table_leg.json"))
                record["leg_elements"] = len(leg.get("elements", []))
            report.append(record)
            tile = texture.copy()
            tile.thumbnail((270, 320), Image.Resampling.NEAREST)
            scale = max(1, min(270 // tile.width, 320 // tile.height))
            tile = tile.resize((tile.width * scale, tile.height * scale), Image.Resampling.NEAREST)
            preview.paste(tile, (index * 300 + 12, 50), tile)
            draw.text((index * 300 + 12, 20), f"Reference {index + 1}: {texture.width} x {texture.height}", fill=(225, 214, 184))
    (QA / "reference-study.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    preview.save(QA / "reference-textures-LOCAL-ONLY.png")
    print(json.dumps([{key: value for key, value in entry.items() if key != "first_element"} for entry in report], ensure_ascii=False))


if __name__ == "__main__":
    main()
