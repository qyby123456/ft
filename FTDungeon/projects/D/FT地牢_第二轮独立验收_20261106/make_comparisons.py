import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps


ROOT = Path(__file__).resolve().parent


def main():
    target = Path(json.loads((ROOT / "latest-preliminary.json").read_text(encoding="utf-8"))["directory"])
    snapshot = json.loads((target / "snapshot.json").read_text(encoding="utf-8"))
    references = json.loads((Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106") / f"S{snapshot['group']}/references.json").read_text(encoding="utf-8"))["references"]
    outputs = []
    for offset in range(0, len(snapshot["rooms"]), 2):
        canvas = Image.new("RGB", (1600, 1160), "#121922")
        draw = ImageDraw.Draw(canvas)
        evidence = []
        for row, room in enumerate(snapshot["rooms"][offset:offset + 2]):
            reference = next(entry for entry in references if entry["id"] == room["id"])
            reference_path = Path(reference["image_path"])
            previews = [target / room["id"] / name for name in ("cutaway.png", "preview-cutaway.png")]
            previews = [path for path in previews if path.is_file()]
            assert len(previews) == 1, f"Missing or ambiguous frozen cutaway: {room['id']}"
            preview = previews[0]
            assert hashlib.sha256(reference_path.read_bytes()).hexdigest() == reference["sha256"]
            captured = next(entry for entry in room["evidence"] if entry["snapshot"] == str(preview))
            assert hashlib.sha256(preview.read_bytes()).hexdigest() == captured["sha256"]
            draw.text((16, row * 580 + 12), room["id"] + " | reference concept (left) vs frozen actual Lodestone cutaway (right)", fill="white")
            for column, path in enumerate((reference_path, preview)):
                with Image.open(path) as image:
                    view = ImageOps.contain(image.convert("RGB"), (790, 540))
                    canvas.paste(view, (column * 800 + (800 - view.width) // 2, row * 580 + 36 + (540 - view.height) // 2))
            evidence.append({"id": room["id"], "reference": reference, "preview": captured, "candidate_sha256": room["sha256"]})
        output = target / f"comparison-{offset + 11:02d}-{min(offset + 12, 20):02d}.jpg"
        canvas.save(output, quality=94)
        outputs.append({"path": str(output), "sha256": hashlib.sha256(output.read_bytes()).hexdigest(), "pairs": evidence})
    (target / "comparison-index.json").write_text(json.dumps(outputs, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(f"Created {len(outputs)} evidence-bound comparison sheets; no acceptance inferred.")


if __name__ == "__main__":
    main()
