import hashlib
import json
import shutil
import zipfile
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parent.parent


def digest(file):
    return hashlib.sha256(file.read_bytes()).hexdigest()


def read(name):
    return json.loads((ROOT / name).read_text(encoding="utf-8"))


def write(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    metadata = read("room.json")
    fingerprint = digest(ROOT / "room.nbt")
    assert metadata["sha256"] == fingerprint
    reports = {name: read(f"qa/{name}.json") for name in ("static", "forge-native", "views", "composites")}
    assert all(report["status"] == "PASS" and report["sha256"] == fingerprint for report in reports.values())
    native = reports["forge-native"]
    assert native["rotations"] == 4 and native["production_save_used"] is False
    assert len(native["negative_probes_rejected"]) == 4
    for phase in ("initial", "after_400_ticks"):
        assert len(native[phase]) == 4
        for rotation in native[phase]:
            assert rotation["records"] == 1584
            assert rotation["sealed_source_water"] == 44 and rotation["dry_player_positions"] == 48
            assert rotation["fluid_seal_faces"] == 220 and rotation["unchanged_boundary_air"] == 990
            assert rotation["unwanted_entities"] == 0 and rotation["two_way_route"]
    log = (ROOT / "qa/forge-run.log").read_text(encoding="utf-8")
    assert "S20_NATIVE_GAME_TEST_PASS: rotations=4/4; records=6336; ticks=400; sha256=" + fingerprint in log
    assert "BUILD SUCCESSFUL" in log
    assert len(reports["views"]["views"]) == 10
    assert reports["views"]["original_nbt_modified"] is False
    for evidence in reports["views"]["views"] + reports["composites"]["images"]:
        assert digest(ROOT / evidence["file"]) == evidence["sha256"]
        with Image.open(ROOT / evidence["file"]) as image:
            image.verify()
    reference = Path(metadata["reference"])
    assert digest(reference) == metadata["reference_sha256"]
    batch_root = reference.parent.parent
    batch = json.loads((batch_root / "manifest.json").read_text(encoding="utf-8"))
    index = json.loads((batch_root / "out/corridor_index.json").read_text(encoding="utf-8"))
    batch_record = next(entry for entry in batch["images"] if entry["id"] == "S20")
    index_record = next(entry for entry in index if entry["id"] == "S20")
    assert batch_record["sha256"] == metadata["reference_sha256"]
    (ROOT / "reference").mkdir(exist_ok=True)
    shutil.copy2(reference, ROOT / "reference" / reference.name)
    shutil.copy2(batch_root / "prompts/S20-abandoned-mine-flooded.txt", ROOT / "reference/S20-prompt.txt")
    write("reference/batch-s20.json", {
        "batch_root": str(batch_root), "manifest_record": batch_record, "index_record": index_record,
        "documents_reviewed": [{"file": name, "sha256": digest(batch_root / name)} for name in ("README.md", "SPEC.md", "manifest.json", "out/corridor_index.json")],
        "reference_kind": "AI concept image, not in-game screenshot",
        "new_paid_generation_requests": 0,
    })
    summary = {
        "id": "S20", "status": "PASS", "sha256": fingerprint,
        "reference_sha256": metadata["reference_sha256"], "size": metadata["size"],
        "minecraft": "1.20.1", "forge": "47.4.10", "data_version": 3465,
        "static": "PASS", "four_direction_interior_and_exterior_views": "PASS",
        "forge_placement_rotations": 4, "native_records_per_phase": 6336,
        "stability_ticks": 400, "fluid_seal": "PASS", "dry_two_way_route": "PASS",
        "negative_probes_rejected": native["negative_probes_rejected"],
        "visual_review": {
            "status": "PASS", "method": "agent visual inspection of overview, four-interior, four-exterior and top-plan images",
            "complete_roof": True, "both_ports_open": True, "sealed_side_water": True,
            "clear_center_rail": True, "broken_arches_readable": True,
            "images_are_software_previews": True, "cutaways_labeled": True,
        },
        "initial_test_fixture_issue": "First QA fixture used relative Y and overlapped isolated test terrain; moved to absolute Y=100. Original NBT unchanged. Rejected log retained.",
        "deployment": "NOT_DEPLOYED", "production_save_used": False,
        "production_mods_modified": False, "full_modpack_client_playtest": False,
        "loot_tables": 0, "block_entities": 0, "entities": 0,
        "limits": ["Isolated Forge test is not a complete modpack/client/multiplayer/FPS playtest.", "Preview cutaways do not modify the exported NBT.", "Vanilla decorative ores remain mineable.", "Explicit air records require a full-volume occupancy check before any future placement."],
    }
    write("qa.json", summary)
    write("qa/summary.json", summary)
    included = [ROOT / name for name in ("README.md", "room.nbt", "room.json", "summary.json", "qa.json", "preview.png", ".gitignore")]
    for folder in ("source", "preview", "reference", "qa", "forge/src", "forge/gradle"):
        included.extend(file for file in (ROOT / folder).rglob("*") if file.is_file() and "__pycache__" not in file.parts)
    included.extend(ROOT / "forge" / name for name in ("build.gradle", "settings.gradle", "gradle.properties"))
    included = sorted(set(included))
    manifest = {
        "id": "corridor-S20", "status": "PASS", "sha256": fingerprint,
        "delivery_root": str(ROOT), "reference_sha256": metadata["reference_sha256"],
        "not_deployed": True, "player_saves_untouched": True,
        "archive_excludes": ["forge/build", "forge/.gradle", "forge/run*", "node_modules"],
        "files": [{"file": file.relative_to(ROOT).as_posix(), "bytes": file.stat().st_size, "sha256": digest(file)} for file in included],
    }
    write("delivery-manifest.json", manifest)
    archive = ROOT / "corridor-S20-handoff.zip"
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED) as package:
        for file in included + [ROOT / "delivery-manifest.json"]:
            package.write(file, "corridor-S20/" + file.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as package:
        assert package.testzip() is None
        archived = package.read("corridor-S20/room.nbt")
        assert hashlib.sha256(archived).hexdigest() == fingerprint
    write("package-receipt.json", {"status": "PASS", "file": archive.name, "bytes": archive.stat().st_size, "sha256": digest(archive), "nbt_sha256": fingerprint, "files": len(included) + 1})
    print(json.dumps({"status": "PASS", "sha256": fingerprint, "archive": str(archive), "archive_bytes": archive.stat().st_size, "files": len(included) + 1}, ensure_ascii=False))


if __name__ == "__main__":
    main()
