from __future__ import annotations

import json
from datetime import datetime
from zoneinfo import ZoneInfo

from PIL import Image

from build_m10 import ENTRANCE, INTERIOR, REFERENCE_HASH, ROOT, SIZE, digest, static_qa, write_json


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def main():
    static = static_qa()
    native = load(ROOT / "qa/native.json")
    rendered = load(ROOT / "qa/render.json")
    structure_hash = digest(ROOT / "room.nbt")
    require(native["status"] == "PASS" and native["sha256"] == structure_hash, "Native report failed or belongs to another template")
    require(native["rotations"] == 4 and native["settle_ticks"] >= 600, "Missing native rotations or stability duration")
    require(native["fire_tick_enabled"] and native["lava_and_fire_ticks_explicitly_scheduled"] and native["fixture_chunks_forced"], "Incomplete heat-source simulation")
    require(native["record_comparisons_per_snapshot"] == 12716, "Native record count mismatch")
    for phase in ("initial", "after_600_ticks"):
        require(len(native[phase]) == 4, "Missing rotation reports")
        for report in native[phase]:
            require(report["records"] == 3179 and report["native_supported_non_air"] == static["non_air"], "Native support or template coverage mismatch")
            require(report["lava_cells"] == 6 and report["enclosed_fire_cells"] == 4, "Native heat-source count changed")
            require(report["supported_devices"] == 9 and report["supported_wall_tools"] == 6, "Equipment support not verified")
            require(report["supported_lamps"] == 3 and report["anchored_chain_cells"] == 8, "Lighting support not verified")
            require(report["dry_landmark_routes"] == 11 and report["roof_cells"] == 289, "Route or roof coverage missing")
            require(report["flammable_blocks"] == 0 and report["unexpected_entities"] == 0 and report["block_entities"] == 0, "Unexpected fire risk or entities")
    require(rendered["status"] == "PASS" and rendered["structure_sha256"] == structure_hash, "Rendered preview belongs to another NBT")
    require(rendered["preview_sha256"] == digest(ROOT / "preview.png") and rendered["views"] == 4 and rendered["roof_unchanged"], "Render evidence mismatch")
    with Image.open(ROOT / "preview.png") as preview:
        require(preview.size == (1536, 1024), "Unexpected preview size")
        preview.verify()
    visual = load(ROOT / "qa/visual-review.json")
    require(visual["status"] == "PASS" and visual["structure_sha256"] == structure_hash, "Final visual review is missing or stale")
    require(visual["preview_sha256"] == digest(ROOT / "preview.png"), "Visual review belongs to another preview")
    marker = "M10_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=600; sha256=" + structure_hash
    successful = []
    attempts = []
    for path in sorted((ROOT / "qa").glob("gametest-*.log")):
        text = path.read_text(encoding="utf-8", errors="replace")
        passed = marker in text and "All 1 required tests passed" in text and "BUILD SUCCESSFUL" in text
        summary = {"path": str(path.relative_to(ROOT)).replace("\\", "/"), "sha256": digest(path), "status": "PASS" if passed else "FAIL"}
        if "Combustible construction:" in text and "minecraft:coal_block" in text:
            summary["reason"] = "Native flammability check rejected decorative coal blocks; replaced with nonflammable deepslate coal ore. Original failed NBT retained in qa/attempt1-coal-block.room.nbt."
            summary["structure_sha256"] = digest(ROOT / "qa/attempt1-coal-block.room.nbt")
        elif passed:
            summary["structure_sha256"] = structure_hash
            successful.append(summary)
        attempts.append(summary)
    require(successful, "No complete successful Forge log for the final NBT")
    write_json(ROOT / "qa/attempts.json", {"attempts": attempts, "failure_history_preserved": True, "final_status": "PASS"})
    evidence_names = ["qa/static.json", "qa/native.json", "qa/render.json", "qa/visual-review.json", "qa/attempts.json",
                      "qa/four-views.png", "qa/exterior.png", "qa/floorplan.png", "qa/heat-sections.png",
                      successful[-1]["path"], "tools/M10Verification.java", "tools/build_m10.py", "tools/render_m10.py"]
    evidence = {name: {"sha256": digest(ROOT / name), "bytes": (ROOT / name).stat().st_size} for name in evidence_names}
    report = {
        "id": "M10", "name": "熔渣铸造间", "status": "PASS", "delivery_state": "INDEPENDENT_NOT_INTEGRATED_NOT_DEPLOYED",
        "checked_at": datetime.now(ZoneInfo("Asia/Shanghai")).isoformat(timespec="seconds"),
        "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR),
        "structure_sha256": structure_hash, "reference_sha256": REFERENCE_HASH,
        "static": {"status": "PASS", "report": "qa/static.json", "records": 3179, "non_air": static["non_air"], "palette_states": static["palette_states"]},
        "forge_native": {"status": "PASS", "report": "qa/native.json", "minecraft": "1.20.1", "forge": "47.4.10", "java": "17.0.8",
                         "rotations": 4, "records_per_snapshot": 12716, "settle_ticks": native["settle_ticks"], "implementation": native["test"], "successful_log": successful[-1]["path"]},
        "safety": {"lava_cells_sealed_on_six_faces": 6, "fire_cells_sealed_on_six_faces": 4,
                   "netherrack_fire_bases": True, "flammable_blocks": 0, "complete_roof_cells": 289,
                   "entrance_clearance": [3, 3, 6], "dry_landmark_routes_per_rotation": 11,
                   "native_reachable_dry_feet_per_rotation": [entry["reachable_dry_feet_positions"] for entry in native["after_600_ticks"]],
                   "supported_devices_per_rotation": 9, "supported_wall_tools_per_rotation": 6, "anchored_lamps_per_rotation": 3,
                   "unexpected_entities": 0, "block_entities": 0, "reward_inventory": 0},
        "preview": {"status": "PASS", "path": "preview.png", "sha256": digest(ROOT / "preview.png"), "size": [1536, 1024], "source": "actual room.nbt; Minecraft block model CPU render", "game_screenshot": False},
        "visual_review": {"status": "PASS", "report": "qa/visual-review.json"},
        "failure_history": "qa/attempts.json", "evidence": evidence,
        "boundaries": {"independent_directory_only": True, "deployed": False, "production_save_accessed": False,
                       "production_core_modified": False, "parallel_deliveries_modified": False, "image_api_called": False},
        "remaining_for_coordinator": ["Assign fixed slot and walkable connection from existing FT hall", "Validate whole-volume occupancy refusal, world border and idempotence", "Review persistence, return route and five difficulty tiers", "Package and deploy only in a coordinated stopped-game release"],
        "limits": ["Native isolated Forge GameTest is not a full-modpack client or multiplayer playtest", "600 ticks is a bounded functional stability check, not long-term performance validation", "Software rendering approximates fluid shape/animation and does not bake game lighting", "No deployment or shared registry/incoming edits were performed"]
    }
    write_json(ROOT / "qa.json", report)
    deliverables = ("room.nbt", "room.json", "preview.png", "qa.json")
    write_json(ROOT / "checksums.json", {"algorithm": "SHA-256", "files": {name: {"sha256": digest(ROOT / name), "bytes": (ROOT / name).stat().st_size} for name in deliverables}, "reference_sha256": REFERENCE_HASH})
    print(json.dumps({"status": report["status"], "files": {name: digest(ROOT / name) for name in deliverables}}, ensure_ascii=False))


if __name__ == "__main__":
    main()
