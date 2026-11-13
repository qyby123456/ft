import hashlib
import json
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    qa = json.loads((ROOT / "qa.json").read_text(encoding="utf-8"))
    assert qa["status"] == "PASS"
    assert qa["sha256"] == digest(ROOT / "room.nbt")
    assert qa["four_view"]["sha256"] == qa["forge_placement_stability"]["sha256"] == qa["sha256"]
    assert qa["forge_placement_stability"]["rotations"] == 4
    required = ["room.nbt", "room.json", "preview.png", "qa.json"]
    selected = [ROOT / name for name in required]
    selected.extend(ROOT / name for name in ("README.md", "recipe.mjs", "summary.json", "four-views.png", "reference-comparison.png"))
    selected.extend(sorted((ROOT / "views").glob("*.png")))
    selected.extend(sorted((ROOT / "tools").glob("*.py")))
    selected.extend(sorted((ROOT / "tools").glob("*.cjs")))
    selected.extend(sorted((ROOT / "qa").glob("*.json")))
    selected.extend(sorted((ROOT / "qa").glob("*.log")))
    selected.extend(sorted((ROOT / "qa/render-raw").glob("*")))
    selected.extend(ROOT / "native" / name for name in ("build.gradle", "settings.gradle", "gradle.properties"))
    selected.extend(sorted(path for path in (ROOT / "native/src").rglob("*") if path.is_file()))
    for name in ("latest.log", "debug.log"):
        log = ROOT / "qa/forge-run/logs" / name
        if log.exists():
            selected.append(log)
    selected = sorted(set(selected))
    records = []
    for path in selected:
        assert path.is_file(), path
        if path.suffix == ".png":
            with Image.open(path) as picture:
                picture.verify()
        records.append({"path": path.relative_to(ROOT).as_posix(), "bytes": path.stat().st_size, "sha256": digest(path)})
    timestamp = datetime.now(timezone(timedelta(hours=8))).isoformat()
    manifest = {"id": "corridor-M11", "status": "PASS", "timestamp": timestamp, "room_nbt_sha256": qa["sha256"], "reference_sha256": qa["reference_sha256"], "four_piece_delivery": required, "records": records, "exclusions": ["native/build", "native/.gradle", "qa/forge-run/world", "qa/forge-run/generated", "qa/rebuild-proof", "packaging receipts and this manifest to avoid self-hashing"]}
    (ROOT / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    archive_path = ROOT / "corridor-M11-delivery.zip"
    with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED, compresslevel=7) as archive:
        for path in selected + [ROOT / "manifest.json"]:
            archive.write(path, "corridor-M11/" + path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
        for name in required:
            assert archive.read("corridor-M11/" + name) == (ROOT / name).read_bytes()
        assert len(archive.namelist()) == len(set(archive.namelist()))
    receipt = {"id": "corridor-M11", "status": "PASS", "root": str(ROOT), "timestamp": timestamp, "four_piece_delivery": required, "size": [15, 12, 40], "entrance": [7, 2, 0], "interior": [7, 2, 5], "exit": [7, 2, 39], "sha256": qa["sha256"], "qa": {"static": "PASS", "four_views": "PASS", "forge_placement": "PASS", "rotations": 4, "settling_ticks": 400}, "archive": str(archive_path), "archive_sha256": digest(archive_path), "archive_bytes": archive_path.stat().st_size, "included_files": len(records) + 1, "deployed": False, "production_saves_touched": False, "hall_connection": "NOT_IN_SCOPE", "full_modpack_playtest": "NOT_PERFORMED"}
    (ROOT / "delivery-receipt.json").write_text(json.dumps(receipt, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(receipt, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
