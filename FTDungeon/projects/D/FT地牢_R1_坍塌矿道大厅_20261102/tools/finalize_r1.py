from __future__ import annotations

import hashlib
import json
import shutil
import zipfile
from datetime import datetime, timedelta, timezone

from build_r1 import REFERENCE, REFERENCE_SHA256, ROOT, digest, static_qa, write_json


def main():
    stamp = datetime.now(timezone(timedelta(hours=8))).isoformat(timespec="seconds")
    source_hash = digest(ROOT / "room.nbt")
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    native = json.loads((ROOT / "qa/native.json").read_text(encoding="utf-8"))
    rendered = json.loads((ROOT / "qa/render.json").read_text(encoding="utf-8"))
    static = static_qa()
    log = (ROOT / "qa/forge-native-console.log").read_text(encoding="utf-8", errors="replace")
    marker = "R1_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=400; sha256=" + source_hash
    required = [marker, "All 1 required tests passed", "BUILD SUCCESSFUL"]
    if not all(value in log for value in required):
        raise ValueError("Native success markers missing")
    if metadata["sha256"] != source_hash or native["sha256"] != source_hash or rendered["source_sha256"] != source_hash:
        raise ValueError("Evidence does not match current NBT")
    if native["status"] != "PASS" or native["rotations"] != 4 or len(native["initial"]) != 4 or len(native["after_400_ticks"]) != 4:
        raise ValueError("Four-direction native or stability result is missing")
    for report in native["initial"] + native["after_400_ticks"]:
        if report["records"] != 3179 or report["native_supported_blocks"] != 1351 or report["grounded_collapse_blocks"] != 54:
            raise ValueError("Native record count mismatch")
        if report["reachable_dry_player_positions"] != 178 or report["reachable_landmarks"] != 6 or report["unwanted_entities"] != 0:
            raise ValueError("Native path or entity check failed")
    if len(native["negative_probes_rejected"]) != 6:
        raise ValueError("Missing negative probe results")
    if digest(REFERENCE) != REFERENCE_SHA256:
        raise ValueError("Reference changed")
    for filename, record in rendered["files"].items():
        if digest(ROOT / filename) != record["sha256"]:
            raise ValueError("Rendered asset changed: " + filename)
    shutil.copy2(ROOT / "qa/forge-run/logs/latest.log", ROOT / "qa/forge-server.log")
    write_json(ROOT / "qa/static.json", static)
    evidence_names = ["qa/static.json", "qa/native.json", "qa/render.json", "qa/forge-native-console.log", "qa/forge-server.log", "qa/four-views.png", "qa/exterior.png", "qa/floorplan.png"]
    evidence = {name: {"sha256": digest(ROOT / name), "bytes": (ROOT / name).stat().st_size} for name in evidence_names}
    write_json(ROOT / "qa.json", {
        "id": "R1", "status": "PASS", "completed_at": stamp, "timezone": "Asia/Shanghai",
        "sha256": source_hash, "static": static, "native": native,
        "preview": {"status": "PASS", "sha256": digest(ROOT / "preview.png"), "source_sha256": source_hash, "game_screenshot": False, "visual_review": "preview, all four cutaway views, complete roof exterior and floor plan inspected"},
        "evidence": evidence,
        "limitations": [
            "Isolated Forge GameTest, not a full modpack client/multiplayer/FPS playtest.",
            "No production save, installed mod, FT core, shared incoming directory, catalog, fixed slot or connecting road was modified.",
            "Shared-route access, occupancy refusal and persistent generation-marker idempotency remain integration-owner work, not claimed here.",
            "Reference minecart is represented by a static empty cauldron ore tub; no entity or moving cart is included.",
            "Preview removes roof and near-side walls only for viewing; delivered NBT roof is complete.",
            "First-start Forge config/default-file warnings and a 10205 ms/204 tick overload warning are retained in the successful test log; no performance guarantee is made.",
        ],
    })
    entries = ["room.nbt", "room.json", "preview.png", "qa.json", "README.md", "build.gradle", "settings.gradle", "gradle.properties", ".gitignore"] + evidence_names
    entries += [str(path.relative_to(ROOT)).replace("\\", "/") for parent in (ROOT / "tools", ROOT / "src") for path in parent.rglob("*") if path.is_file() and "__pycache__" not in path.parts]
    files = {name: {"sha256": digest(ROOT / name), "bytes": (ROOT / name).stat().st_size} for name in sorted(set(entries))}
    manifest = {
        "id": "R1", "name": "坍塌矿道大厅", "status": "INDEPENDENT_DELIVERY_READY_NOT_DEPLOYED",
        "created_at": stamp, "root": str(ROOT), "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"],
        "reference": {"path": str(REFERENCE), "sha256": REFERENCE_SHA256, "pixels": [1536, 1024], "new_image_requests": 0},
        "native_pass_marker": marker, "four_piece_delivery": ["room.nbt", "room.json", "preview.png", "qa.json"],
        "files": files, "production_changed": False,
    }
    write_json(ROOT / "delivery-manifest.json", manifest)
    archive = ROOT / "R1-collapsed-mine-hall-handoff.zip"
    with zipfile.ZipFile(archive, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=6) as output:
        for name in sorted(files):
            output.write(ROOT / name, name)
        output.write(ROOT / "delivery-manifest.json", "delivery-manifest.json")
    with zipfile.ZipFile(archive) as check:
        for name, record in files.items():
            if hashlib.sha256(check.read(name)).hexdigest() != record["sha256"]:
                raise ValueError("ZIP roundtrip hash mismatch: " + name)
        if hashlib.sha256(check.read("delivery-manifest.json")).hexdigest() != digest(ROOT / "delivery-manifest.json"):
            raise ValueError("ZIP manifest roundtrip hash mismatch")
    write_json(ROOT / "package-receipt.json", {"status": "PASS", "created_at": stamp, "archive": archive.name, "sha256": digest(archive), "bytes": archive.stat().st_size, "manifest_sha256": digest(ROOT / "delivery-manifest.json"), "files_verified": len(files) + 1, "production_changed": False})
    print(json.dumps({"status": "PASS", "room_sha256": source_hash, "archive_sha256": digest(archive), "root": str(ROOT), "files_verified": len(files) + 1}, ensure_ascii=False))


if __name__ == "__main__":
    main()
