from __future__ import annotations

import argparse
import json
import zipfile
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from build_m5 import OUT, REFERENCE, REFERENCE_SHA256, ROOT, digest, dump


def read(relative):
    return json.loads((ROOT / relative).read_text(encoding="utf-8-sig"))


def main(native_log):
    metadata = read("out/M5/room.json")
    fingerprint = digest(OUT / "room.nbt")
    static = read("qa/static-qa.json")
    native = read("qa/native-world-qa.json")
    registry = read("qa/native-registry-qa.json")
    preview = read("qa/preview-provenance.json")
    for report in (static, native, registry, preview):
        if report["status"] != "PASS" or report["sha256"] != fingerprint:
            raise ValueError("Incomplete or stale QA evidence")
    if digest(REFERENCE) != REFERENCE_SHA256 or metadata["sha256"] != fingerprint:
        raise ValueError("Fingerprint changed")
    log_path = ROOT / native_log
    log = log_path.read_text(encoding="utf-8-sig")
    if "BUILD SUCCESSFUL" not in log or "M5_NATIVE_QA_PASS" not in log or "All 1 required tests passed" not in log:
        raise ValueError("Missing successful native GameTest execution log")
    boundary = []
    for entry in read("evidence/read-only-boundary-baseline.json"):
        path = Path(entry["path"])
        after = digest(path) if path.is_file() else None
        boundary.append({**entry, "current_sha256": after, "unchanged": after == entry["sha256"]})
    dump(ROOT / "qa/read-only-boundary-review.json", {"scope": "Read-only baseline comparison; concurrent authorized coordinator edits may change these files. This M5 workspace writes only below its own root.", "checked": len(boundary), "unchanged": sum(entry["unchanged"] for entry in boundary), "files": boundary})
    qa = dict(static)
    qa["status"] = "PASS"
    qa["static_qa"] = {"status": "PASS", "checks": len(static["checks"]), "report": "../../qa/static-qa.json"}
    qa["native_registry"] = {"status": "PASS", "palette_states": registry["palette_states"], "report": "../../qa/native-registry-qa.json"}
    qa["native_placement"] = native
    qa["preview"] = preview
    qa["scope"] = "Independent M5 static QA plus real Minecraft 1.20.1 / Forge 47.4.10 GameTest. Not a full modpack gameplay or FT catalog integration test."
    qa["native_log"] = {"path": "../../" + native_log, "sha256": digest(log_path), "gradle_exit": 0}
    qa["client_gameplay_tested"] = False
    qa["production_integration_tested"] = False
    qa["deployment"] = {"deployed": False, "player_worlds_opened": False, "installed_jars_modified": False, "other_room_sources_modified_by_this_workspace": False}
    qa["rollback"] = {"fixture_rollback": "PASS in all four rotations after 80 ticks", "before_integration": "Remove this M5 candidate from coordinator staging only; production untouched", "after_integration": "Coordinator must snapshot target world volume and use existing per-room generation markers; do not issue a blind air fill or rebuild older rooms"}
    dump(OUT / "qa.json", qa)
    materials = ["# M5 materials", "", "Actual delivered NBT counts, excluding air.", "", "| Block | Count |", "| --- | ---: |"]
    materials.extend(f"| `{name}` | {count} |" for name, count in static["materials"].items())
    (ROOT / "MATERIALS.md").write_text("\n".join(materials) + "\n", encoding="utf-8")
    delivery_files = [{"name": name, "path": str(OUT / name), "sha256": digest(OUT / name), "bytes": (OUT / name).stat().st_size} for name in ("room.nbt", "room.json", "preview.png", "qa.json")]
    manifest = {
        "id": "M5", "name": metadata["name"], "status": "READY_FOR_COORDINATOR_REVIEW_NOT_DEPLOYED",
        "created_at": datetime.now(ZoneInfo("Asia/Shanghai")).isoformat(timespec="seconds"),
        "workspace": str(ROOT), "delivery": str(OUT), "size": metadata["size"],
        "entrance": metadata["entrance"], "interior": metadata["interior"], "facing": "north",
        "template_id": metadata["template_id"], "structure_sha256": fingerprint,
        "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA256,
        "files": delivery_files,
        "qa": {"static": "PASS", "native_registry": "PASS", "forge_four_rotation_placement": "PASS", "stable_ticks": 80, "fixture_rollback": "PASS", "modpack_gameplay": "NOT_RUN", "FT_catalog_integration": "NOT_RUN"},
        "report_to_thread": "01a19758-b0e5-75c2-822a-4860527a1e8b",
        "deployment_authorized": False,
        "boundary_review": "qa/read-only-boundary-review.json"
    }
    dump(ROOT / "delivery-manifest.json", manifest)
    archive_path = ROOT / "M5-independent-delivery.zip"
    with zipfile.ZipFile(archive_path, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for entry in delivery_files:
            archive.write(entry["path"], "out/M5/" + entry["name"])
        for relative in ("README.md", "MATERIALS.md", "delivery-manifest.json", "REPORT_FOR_COORDINATOR.md", "Run-QA.ps1"):
            archive.write(ROOT / relative, relative)
        for directory in ("qa", "evidence", "tools"):
            for path in sorted((ROOT / directory).rglob("*")):
                if path.is_file() and "__pycache__" not in path.parts:
                    archive.write(path, path.relative_to(ROOT).as_posix())
        for path in sorted((ROOT / "native-test/src").rglob("*")):
            if path.is_file():
                archive.write(path, path.relative_to(ROOT).as_posix())
        for relative in ("native-test/build.gradle", "native-test/settings.gradle", "native-test/gradle.properties", "native-test/gradle/wrapper/gradle-wrapper.jar", "native-test/gradle/wrapper/gradle-wrapper.properties"):
            archive.write(ROOT / relative, relative)
    dump(ROOT / "package-receipt.json", {"path": str(archive_path), "sha256": digest(archive_path), "bytes": archive_path.stat().st_size, "contains_runtime_save": False, "deployed": False})
    print(json.dumps({"status": manifest["status"], "structure_sha256": fingerprint, "package": str(archive_path), "package_sha256": digest(archive_path), "baseline_unchanged": sum(entry["unchanged"] for entry in boundary), "baseline_checked": len(boundary)}, ensure_ascii=False))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--native-log", default="evidence/forge-gametest-attempt2.log")
    main(parser.parse_args().native_log)
