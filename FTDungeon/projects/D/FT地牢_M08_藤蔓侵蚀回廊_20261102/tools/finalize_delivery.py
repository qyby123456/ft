from __future__ import annotations

import argparse
import hashlib
import json
import zipfile
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from PIL import Image

from build_m08 import OUT, REFERENCE, REFERENCE_SHA, ROOT, digest, dump


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def record(path):
    path = Path(path)
    return {"path": str(path), "bytes": path.stat().st_size, "sha256": digest(path)}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--native-log", type=Path, required=True)
    args = parser.parse_args()
    native_log = args.native_log.resolve()
    if not native_log.is_relative_to(ROOT):
        raise ValueError("Native log must belong to the independent M08 workspace")
    metadata = load(OUT / "room.json")
    fingerprint = digest(OUT / "room.nbt")
    reports = {name: load(ROOT / "qa" / filename) for name, filename in {
        "static": "static-qa.json", "native_registry": "native-registry-qa.json", "native_world": "native-world-qa.json", "preview": "preview-provenance.json"
    }.items()}
    if any(report["status"] != "PASS" or report["sha256"] != fingerprint for report in reports.values()):
        raise ValueError("Missing, failed or stale QA evidence")
    if metadata["sha256"] != fingerprint or digest(REFERENCE) != REFERENCE_SHA:
        raise ValueError("Delivery or reference SHA mismatch")
    text = native_log.read_text(encoding="utf-8", errors="replace")
    if "M08_NATIVE_QA_PASS" not in text or "All 1 required tests passed" not in text or "BUILD SUCCESSFUL" not in text or text.count("M08_NATIVE_PLACEMENT_PASS:") != 4:
        raise ValueError("Native log does not prove a successful four-rotation build/test")
    if reports["preview"]["preview_sha256"] != digest(OUT / "preview.png"):
        raise ValueError("Preview evidence is stale")
    visual = load(ROOT / "qa/visual-review.json")
    if visual["status"] != "PASS" or visual["nbt_sha256"] != fingerprint:
        raise ValueError("Visual review is missing or stale")
    master = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-master-20261102")
    entry = next(item for item in load(master / "manifest.json")["rooms"] if item["id"] == "M08")
    with Image.open(REFERENCE) as image:
        if image.size != (1536, 1024) or entry["sha256"] != REFERENCE_SHA:
            raise ValueError("Master reference manifest mismatch")
    inputs = [REFERENCE, master / "README.md", master / "manifest.json", Path("D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md")]
    dump(ROOT / "evidence/reference-provenance.json", {"status": "PASS", "id": "M08", "reference_size": [1536, 1024], "reference_sha256": REFERENCE_SHA, "master_manifest_entry": entry, "read_only_inputs": [record(path) for path in inputs], "new_image_api_calls": 0})
    dump(ROOT / "qa/boundary-audit.json", {
        "status": "PASS", "method": "Operation-scope review, not a whole-disk before/after hash claim. Concurrent changes by other chats are neither restored nor overwritten.",
        "artifact_write_root": str(ROOT), "standard_tool_cache_writes": "Gradle/Forge dependency and tool caches only",
        "core_source_modified": False, "shared_incoming_modified": False, "formal_mods_or_backups_modified": False,
        "player_save_opened_or_modified": False, "other_room_projects_modified": False,
        "imagegen_or_chat_provider_config_modified": False, "new_image_api_calls": 0,
        "world_used_for_qa": reports["native_world"]["world_path"],
        "initial_directory_wide_hash_baseline": False,
        "retained_for_integrator": ["Catalog registration and unique ID/slot", "Full-volume occupation/entity/liquid preflight", "Actual hall-to-north-door branch", "Existing personal return data and five difficulty tiers", "Persistent room marker, idempotence and player edits", "Client/multiplayer validation and deployment decision"]
    })
    materials = load(ROOT / "qa/materials.json")
    material_text = ["# M08 材料清单", "", f"尺寸：19×11×23；显式记录：{materials['records']}；非空气：{materials['non_air']}。", "", "| 原版方块 | 数量 |", "| --- | ---: |"]
    material_text.extend(f"| `{name}` | {count} |" for name, count in materials["materials"].items())
    material_text.extend(["", "7 个木桶均显式空库存，无 LootTable；不新增奖励、刷怪笼或实体。", ""])
    (ROOT / "MATERIALS.md").write_text("\n".join(material_text), encoding="utf-8")
    evidence = {name: {"path": str(ROOT / "qa" / filename), "sha256": digest(ROOT / "qa" / filename), "status": reports[name]["status"]} for name, filename in {
        "static": "static-qa.json", "native_registry": "native-registry-qa.json", "native_world": "native-world-qa.json", "preview": "preview-provenance.json"
    }.items()}
    timestamp = datetime.now(ZoneInfo("Asia/Shanghai")).isoformat()
    rotations = reports["native_world"]["rotations"]
    summary = {
        "id": "M08", "name": "藤蔓侵蚀回廊", "status": "PASS", "timestamp": timestamp,
        "sha256": fingerprint, "reference_sha256": REFERENCE_SHA,
        "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "facing": "north",
        "records": materials["records"], "non_air": materials["non_air"],
        "static_checks_passed": len(reports["static"]["checks"]), "native_palette_states": reports["native_registry"]["palette_states"],
        "native_placement": {"minecraft": "1.20.1", "forge": "47.4.10", "rotations": 4, "records_per_snapshot": 19228, "stable_ticks": 200, "random_tick_speed": 3, "explicit_foliage_random_tick_sweeps": 4, "native_player_collision_box": [0.6, 1.8, 0.6], "reachable_feet_each_rotation": [result["reachable_feet"] for result in rotations], "empty_barrels_each_rotation": [result["empty_barrels"] for result in rotations], "natural_foliage_changes_after_200_ticks": [result["after_200_ticks"]["natural_foliage_changes"] for result in rotations], "fixture_rollback": "PASS"},
        "native_log": record(native_log), "evidence": evidence, "visual_review": record(ROOT / "qa/visual-review.json"),
        "preview": record(OUT / "preview.png"), "metadata": record(OUT / "room.json"),
        "support_clearance_entry_safe_point_roof": "PASS",
        "deployment": "NOT_DEPLOYED", "ft_core_or_hall_integration": "NOT_TESTED", "client_multiplayer_gameplay": "NOT_TESTED",
        "limitations": [
            "Actual-NBT software preview is not a game screenshot; outline AABBs approximate plants, lanterns and alpha geometry; no shader lighting.",
            "Native stability means structural integrity, valid surviving foliage and collision-safe routes after 200 ticks, not byte-identical living vegetation. Natural leaf distance updates/vine growth are counted.",
            "No claim of unlimited-duration growth stability, FT integration, full-modpack gameplay or multiplayer testing.",
            "Core must preflight complete volume before construction. Explicit template air never authorizes clearing existing blocks."
        ]
    }
    dump(OUT / "qa.json", summary)
    deliverable_paths = []
    for folder in ("out", "qa", "evidence", "tools"):
        deliverable_paths.extend(path for path in (ROOT / folder).rglob("*") if path.is_file() and "__pycache__" not in path.parts)
    deliverable_paths.extend(ROOT / name for name in ("README.md", "REPORT_FOR_COORDINATOR.md", "MATERIALS.md", "Run-QA.ps1"))
    deliverable_paths.extend(path for path in (ROOT / "native-test/src").rglob("*") if path.is_file())
    deliverable_paths.extend(path for path in (ROOT / "native-test/gradle").rglob("*") if path.is_file())
    deliverable_paths.extend(ROOT / "native-test" / name for name in ("build.gradle", "settings.gradle", "gradle.properties"))
    deliverable_paths = sorted(set(deliverable_paths))
    files = [{"path": path.relative_to(ROOT).as_posix(), "bytes": path.stat().st_size, "sha256": digest(path)} for path in deliverable_paths]
    dump(ROOT / "delivery-manifest.json", {"id": "M08", "timestamp": timestamp, "status": "PASS", "deployment": "NOT_DEPLOYED", "files": files, "excluded": ["native-test/build", "native-test/.gradle", "native-test/run", "tools/__pycache__"], "manifest_self_hash": "Excluded to avoid self-reference; receipt holds manifest hash."})
    package = ROOT / "M08-independent-delivery.zip"
    with zipfile.ZipFile(package, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for path in [*deliverable_paths, ROOT / "delivery-manifest.json"]:
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(package) as archive:
        if archive.testzip() is not None:
            raise ValueError("Package CRC test failed")
        for item in files:
            if hashlib.sha256(archive.read(item["path"])).hexdigest() != item["sha256"]:
                raise ValueError("Packaged file SHA mismatch: " + item["path"])
    dump(ROOT / "package-receipt.json", {"id": "M08", "status": "PASS", "timestamp": timestamp, "package": record(package), "manifest": record(ROOT / "delivery-manifest.json"), "nbt": record(OUT / "room.nbt"), "file_count_including_manifest": len(files) + 1, "zip_crc_test": "PASS", "packaged_sha256_verification": "PASS", "deployment": "NOT_DEPLOYED"})
    print(json.dumps({"status": "PASS", "nbt_sha256": fingerprint, "zip_sha256": digest(package), "files": len(files) + 1}, ensure_ascii=False))


if __name__ == "__main__":
    main()
