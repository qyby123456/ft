import argparse
import hashlib
import json
from pathlib import Path

from PIL import Image, ImageDraw, ImageOps


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest = json.loads((root / "ready-for-review.json").read_text(encoding="utf-8-sig"))
    snapshot = json.loads((root / "submission-snapshot.json").read_text(encoding="utf-8"))
    records = {record["source"]: record for record in snapshot["files"]}
    if (root / "supplemental-snapshot.json").is_file():
        records.update({record["source"]: record for record in json.loads((root / "supplemental-snapshot.json").read_text(encoding="utf-8"))["files"]})
    reference_path = root / "independent-references.json"
    if not reference_path.exists():
        frozen_references = root / "references.json"
        if frozen_references.is_file():
            binding = next(record for record in records.values() if record["snapshot"] == str(frozen_references))
            assert hashlib.sha256(frozen_references.read_bytes()).hexdigest() == binding["sha256"]
            reference_path.write_bytes(frozen_references.read_bytes())
        else:
            source_root = Path(next(iter(records))).parents
            group_root = next(parent for parent in source_root if parent.name == (manifest.get("style") or manifest["group"]))
            reference_path.write_bytes((group_root / "references.json").read_bytes())
    references = json.loads(reference_path.read_text(encoding="utf-8-sig"))["references"]
    index = []
    for offset in range(0, 10, 2):
        canvas = Image.new("RGB", (1600, 1160), "#121922")
        draw = ImageDraw.Draw(canvas)
        pairs = []
        for row, room in enumerate(manifest["rooms"][offset:offset + 2]):
            source = room.get("candidate") or room["source"]
            if isinstance(source, str):
                source = {"path": source, "sha256": room["sha256"]}
            reference = next(record for record in references if record["id"].upper() == room["id"].upper())
            images = room.get("images") or [path for path in records if Path(path).parent == Path(source["path"]).parent and Path(path).suffix == ".png"]
            images = [str(Path(image) if Path(image).is_absolute() else Path(source["path"]).parent / image) for image in images]
            room_previews = room.get("previews") or [{"path": image, "sha256": records[image]["sha256"]} for image in images]
            previews = [record for record in room_previews if Path(record["path"]).name in {"cutaway.png", "preview-cutaway.png"}]
            assert len(previews) == 1
            frozen = records[previews[0]["path"]]
            preview_path = Path(frozen["snapshot"])
            original_path = Path(reference["image_path"])
            assert hashlib.sha256(preview_path.read_bytes()).hexdigest() == frozen["sha256"] == previews[0]["sha256"]
            assert hashlib.sha256(original_path.read_bytes()).hexdigest() == reference["sha256"]
            render_paths = [str(Path(source["path"]).parent / suffix) for suffix in ("qa/render.json", "render.json")]
            qa_records = room["qa"] if isinstance(room.get("qa"), list) else [{"path": path} for path in render_paths if path in records]
            render = [record for record in qa_records if Path(record["path"]).name in {"render.json", "render-evidence.json"}]
            assert len(render) == 1
            render_report = json.loads(Path(records[render[0]["path"]]["snapshot"]).read_text(encoding="utf-8-sig"))
            assert (render_report.get("sha256") or render_report.get("source_sha256")) == source["sha256"]
            draw.text((16, row * 580 + 12), room["id"] + " | reference left / actual frozen Lodestone cutaway right", fill="white")
            for column, image_path in enumerate((original_path, preview_path)):
                with Image.open(image_path) as image:
                    view = ImageOps.contain(image.convert("RGB"), (790, 540))
                    canvas.paste(view, (column * 800 + (800 - view.width) // 2, row * 580 + 36 + (540 - view.height) // 2))
            pairs.append({"id": room["id"], "candidate_sha256": source["sha256"], "reference": reference, "preview": frozen})
        output = root / f"comparison-{offset + 11}-{offset + 12}.jpg"
        canvas.save(output, quality=94)
        index.append({"path": str(output), "sha256": hashlib.sha256(output.read_bytes()).hexdigest(), "pairs": pairs})
    (root / "comparison-index.json").write_text(json.dumps(index, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Created five frozen comparisons; visual review pending.")


if __name__ == "__main__":
    main()
