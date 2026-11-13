from __future__ import annotations

import argparse
import json
import shutil
import zipfile
from pathlib import Path

from PIL import Image

from build_m6 import DELIVERY, REFERENCE_HASH, ROOT, digest, validate, write_json


INCOMING = Path("D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/M6")
FILES = ("room.nbt", "room.json", "preview.png", "qa.json")


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--publish", action="store_true")
    arguments = parser.parse_args()
    report = validate(DELIVERY / "room.nbt")
    native = load(ROOT / "qa/native.json")
    rendered = load(ROOT / "qa/render.json")
    metadata = load(DELIVERY / "room.json")
    actual_hash = digest(DELIVERY / "room.nbt")
    assert native["status"] == rendered["status"] == "PASS"
    assert actual_hash == metadata["sha256"] == native["sha256"] == rendered["structure_sha256"]
    assert report["reference"]["sha256"] == REFERENCE_HASH
    assert native["rotations"] == 4 and native["fire_tick_enabled"] and native["lava_ticks_explicitly_scheduled"]
    for phase in ("initial", "after_400_ticks"):
        assert len(native[phase]) == 4
        assert len({room["rotation"] for room in native[phase]}) == 4
        for room in native[phase]:
            assert room["records"] == 5292 and room["native_supported_blocks"] == 2539
            assert room["reachable_dry_feet_positions"] == 319 and room["dry_landmark_routes"] == 9
            assert room["lava_cells"] == 38 and room["soul_fire"] == 1 and room["reward_inventory"] == 0
            assert room["supported_lamps"] == 6 and room["supported_bells"] == 2 and room["anchored_chain_cells"] == 42
    log = (ROOT / "qa/gametest.log").read_text(encoding="utf-8-sig", errors="replace")
    for evidence in ("M6_NATIVE_GAME_TEST_PASS: rotations=4/4; records=21168; ticks=400; sha256=" + actual_hash, "All 1 required tests passed", "BUILD SUCCESSFUL"):
        assert evidence in log, evidence
    assert "BUILD FAILED" not in log
    assert digest(DELIVERY / "preview.png") == rendered["preview_sha256"]
    with Image.open(DELIVERY / "preview.png") as image:
        assert image.size == (1536, 1024)
        image.verify()
    report["status"] = "PASS"
    report["native"] = native
    report["native"]["report_path"] = str(ROOT / "qa/native.json")
    report["native"]["report_sha256"] = digest(ROOT / "qa/native.json")
    report["native"]["log_path"] = str(ROOT / "qa/gametest.log")
    report["native"]["log_sha256"] = digest(ROOT / "qa/gametest.log")
    report["render"] = rendered
    report["room_json_sha256"] = digest(DELIVERY / "room.json")
    report["navigation"] = {"native_collision_verified": True, "entrance_to_interior_and_back": True, "reachable_dry_feet_positions": 319, "dry_landmark_routes": 9, "no_hazards_on_route": True}
    report["visual_review"] = {"four_cutaway_views": "PASS", "complete_roof_exterior": "PASS", "reviewer": "agent visual inspection of actual-NBT software render", "game_screenshot": False}
    report["integration"] = {"status": "READY_FOR_COORDINATOR_REVIEW_NOT_DEPLOYED", "thread_id": "01a19758-b0e5-75c2-822a-4860527a1e8b", "target_template_id": "ftdungeonmobs:image25/m6", "fixed_slot_and_hall_route": "not assigned or tested by this room-only project"}
    write_json(DELIVERY / "qa.json", report)
    files = {name: {"bytes": (DELIVERY / name).stat().st_size, "sha256": digest(DELIVERY / name)} for name in FILES}
    handoff = {"status": "PASS_NOT_DEPLOYED", "room_id": "M6", "image_sha256": REFERENCE_HASH, "structure_sha256": actual_hash, "delivery": str(DELIVERY), "incoming": str(INCOMING), "files": files, "native": str(ROOT / "qa/native.json"), "log": str(ROOT / "qa/gametest.log"), "rollback": "Only M6 is newly added; no previous M6 or production files replaced"}
    write_json(ROOT / "handoff.json", handoff)
    evidence_files = [ROOT / "README.md", ROOT / "handoff.json", ROOT / "build.gradle", ROOT / "settings.gradle", ROOT / "gradle.properties"]
    evidence_files.extend(DELIVERY / name for name in FILES)
    evidence_files.extend(path for path in (ROOT / "tools").rglob("*") if path.is_file() and "__pycache__" not in path.parts)
    evidence_files.extend(ROOT / "qa" / name for name in ("static.json", "native.json", "gametest.log", "gametest-attempt1-missing-pack-metadata.log", "render.json", "four-views.png", "exterior.png", "floorplan.png"))
    archive = ROOT / "M6-nether-bastion-handoff.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as bundle:
        for source in evidence_files:
            bundle.write(source, source.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as bundle:
        assert bundle.testzip() is None
        for source in evidence_files:
            assert bundle.read(source.relative_to(ROOT).as_posix()) == source.read_bytes()
    receipt = {"status": "PACKAGED", "archive": str(archive), "sha256": digest(archive), "bytes": archive.stat().st_size, "zip_files": len(evidence_files), "zip_crc": "PASS", "all_entries_roundtrip": True, "published": False}
    if arguments.publish:
        assert INCOMING.parent.resolve() == Path("D:/ft工程文件/FT地牢_Image25接入_20261101/incoming").resolve()
        if INCOMING.exists():
            assert set(path.name for path in INCOMING.iterdir()) == set(FILES), "Existing M6 directory differs; refusing overwrite"
            assert all(digest(INCOMING / name) == files[name]["sha256"] for name in FILES), "Existing M6 fingerprint differs; refusing overwrite"
            receipt["published"] = True
            receipt["publish_mode"] = "already identical; no overwrite"
        else:
            INCOMING.mkdir(exist_ok=False)
            for name in FILES:
                shutil.copy2(DELIVERY / name, INCOMING / name)
            assert all(digest(INCOMING / name) == files[name]["sha256"] for name in FILES)
            receipt["published"] = True
            receipt["publish_mode"] = "new M6 directory only"
        receipt["incoming"] = str(INCOMING)
    write_json(ROOT / "package-receipt.json", receipt)
    print(json.dumps({"status": handoff["status"], "structure_sha256": actual_hash, **receipt}, ensure_ascii=False))


if __name__ == "__main__":
    main()
