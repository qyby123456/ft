from __future__ import annotations

import json
import zipfile
from pathlib import Path

from PIL import Image

from build_m12 import REFERENCE, REFERENCE_HASH, ROOT, digest, write_json
from static_qa import inspect, require


def read_json(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8-sig"))


def main():
    inspect()
    nbt_hash = digest(ROOT / "room.nbt")
    static = read_json("qa/static.json")
    native = read_json("qa/native.json")
    rendering = read_json("qa/render.json")
    run = read_json("evidence/native-run.json")
    log_path = ROOT / run["log"]
    log = log_path.read_text(encoding="utf-8-sig", errors="replace")
    require(run["exit_code"] == 0, "Forge exited unsuccessfully")
    require("M12_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=400; sha256=" + nbt_hash in log, "Native test success marker missing")
    require("All 1 required tests passed" in log and "BUILD SUCCESSFUL" in log, "GameTest or build did not pass")
    for report in (static, native):
        require(report["status"] == "PASS" and report["sha256"] == nbt_hash, "Stale or failed QA report")
    require(rendering["status"] == "PASS" and rendering["nbt_sha256"] == nbt_hash, "Stale render evidence")
    require(rendering["preview_sha256"] == digest(ROOT / "preview.png"), "Preview fingerprint mismatch")
    require(native["rotations"] == 4 and len(native["initial"]) == 4 and len(native["after_400_ticks"]) == 4, "Incomplete native rotations")
    for rotation in native["initial"] + native["after_400_ticks"]:
        require(rotation["exact_records"] == 3179 and rotation["sealed_water_sources"] == 6 and rotation["supported_small_mushrooms"] == 12, "Native record or water/mushroom mismatch")
        require(rotation["roundtrip_landmarks"] == 10 and rotation["connected_giant_mushrooms"] == 2 and rotation["full_roof_cells"] == 289, "Native geometry or path mismatch")
        require(rotation["entities_and_drops"] == 0 and rotation["empty_barrels"] == 8, "Unexpected entities, drops or inventory")
    require(digest(REFERENCE) == REFERENCE_HASH, "Reference has changed")
    with Image.open(ROOT / "preview.png") as preview:
        preview.verify()
    baseline = read_json("evidence/source-and-boundary-baseline.json")
    observations = []
    for record in baseline["source_files"]:
        path = Path(record["path"])
        observations.append({"path": str(path), "baseline_sha256": record["sha256"], "current_sha256": digest(path), "unchanged": digest(path) == record["sha256"]})
    boundary = {"status": "PASS", "authorized_write_root": str(ROOT), "production_deployed": False, "production_save_opened": False, "core_or_other_rooms_authored": False, "shared_incoming_authored": False, "image_generation_requested": False, "read_only_source_observations": observations, "caveat": "Read-only source hashes are observations, not a lock on parallel coordinator work; global Gradle cache may change normally"}
    write_json(ROOT / "evidence/boundary-review.json", boundary)
    result = {
        "id": "M12", "status": "PASS", "scope": "Independent structure handoff; no production deployment",
        "sha256": nbt_hash, "reference_sha256": REFERENCE_HASH,
        "static": static, "native": native,
        "render": {key: value for key, value in rendering.items() if key not in ("models", "textures")},
        "evidence": {"native_log": run["log"], "native_log_sha256": digest(log_path), "static": "qa/static.json", "native": "qa/native.json", "render": "qa/render.json", "four_views": "preview/four-views.png", "roof": "preview/roof-exterior.png", "floorplan": "preview/floorplan.png", "boundary": "evidence/boundary-review.json"},
        "production_deployed": False, "production_save_opened": False, "full_modpack_playtest": False,
        "limitations": ["The reference is an AI concept; actual NBT uses vanilla block-scale geometry with documented adaptations", "Software preview is not a Minecraft client screenshot and does not demonstrate FPS or multiplayer behavior", "Forge fixture tests vanilla blocks plus Forge, not the complete modpack", "Coordinator must still allocate the unique ID/slot, connect to the hall, check full-volume occupancy and idempotence, package and deploy"],
    }
    write_json(ROOT / "qa.json", result)
    deliverables = [ROOT / name for name in ("room.nbt", "room.json", "preview.png", "qa.json")]
    receipt = {"id": "M12", "status": "PASS", "files": [{"path": str(path), "bytes": path.stat().st_size, "sha256": digest(path)} for path in deliverables]}
    write_json(ROOT / "delivery-manifest.json", receipt)
    archive = ROOT / "M12-fungal-cellar-independent-handoff.zip"
    candidates = []
    for name in ("room.nbt", "room.json", "preview.png", "qa.json", "README.md", "REPORT_FOR_COORDINATOR.md", "Run-QA.ps1", "delivery-manifest.json"):
        path = ROOT / name
        require(path.is_file(), "Missing handoff file: " + name)
        candidates.append(path)
    for directory in ("tools", "qa", "preview", "evidence", "native-test/src"):
        candidates.extend(path for path in (ROOT / directory).rglob("*") if path.is_file() and "__pycache__" not in path.parts)
    candidates.extend(ROOT / "native-test" / name for name in ("build.gradle", "settings.gradle", "gradle.properties"))
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED) as package:
        for path in sorted(set(candidates)):
            package.write(path, path.relative_to(ROOT).as_posix())
    write_json(ROOT / "package-receipt.json", {"path": str(archive), "bytes": archive.stat().st_size, "sha256": digest(archive), "files": len(set(candidates)), "excluded": ["native-test/runs", "native-test/build", "native-test/.gradle", "__pycache__"], "production_deployed": False})
    print(json.dumps(receipt, ensure_ascii=False))
    print("M12_HANDOFF_FINALIZED " + digest(archive))


if __name__ == "__main__":
    main()
