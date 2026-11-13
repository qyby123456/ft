from __future__ import annotations

import hashlib
import json
from pathlib import Path

from PIL import Image

from build_s6 import OUT, REFERENCE, REFERENCE_SHA, ROOT, dump
from nbtio import Structure, plain


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    structure_hash = digest(OUT / "room.nbt")
    metadata = json.loads((OUT / "room.json").read_text(encoding="utf-8"))
    static = json.loads((ROOT / "qa/static.json").read_text(encoding="utf-8"))
    native = json.loads((ROOT / "qa/forge-native.json").read_text(encoding="utf-8"))
    render = json.loads((ROOT / "qa/render.json").read_text(encoding="utf-8"))
    assert digest(REFERENCE) == REFERENCE_SHA
    for report in (metadata, static, native, render):
        assert report["sha256"] == structure_hash
    assert static["status"] == native["status"] == render["status"] == "PASS"
    assert native["ticks_observed"] == 240 and len(native["rotations"]) == 4
    assert native["native_dripstone_fluid_resolution"].startswith("LAVA")
    original = Structure.read(OUT / "room.nbt")
    roundtrip = Structure.read(ROOT / "qa/native-roundtrip.nbt")
    assert original.blocks == roundtrip.blocks and original.size == roundtrip.size
    assert {position: plain(tag) for position, tag in original.block_entities.items()} == {position: plain(tag) for position, tag in roundtrip.block_entities.items()}
    with Image.open(OUT / "preview.png") as image:
        image.load()
        assert image.size == (1760, 1100)
    evidence = {
        "static": str(ROOT / "qa/static.json"), "forge_native": str(ROOT / "qa/forge-native.json"),
        "forge_log": str(ROOT / "qa/forge-run.log"), "native_roundtrip": str(ROOT / "qa/native-roundtrip.nbt"),
        "render_manifest": str(ROOT / "qa/render.json"), "four_views": str(ROOT / "qa/four-views.png"),
        "complete_exterior": str(ROOT / "qa/exterior.png"),
    }
    result = {
        "id": "S6", "name": "地狱魂火祭坛", "status": "PASS", "sha256": structure_hash,
        "reference": {"path": str(REFERENCE), "sha256": REFERENCE_SHA, "verified": True},
        "size_xyz": [9, 7, 9], "footprint_xz": [9, 9], "height": 7,
        "entrance": [4, 1, 0], "interior": [4, 1, 2], "facing": "north",
        "static": static, "native": native,
        "native_roundtrip_block_state_and_block_entity_comparison": "EXACT_MATCH",
        "preview": {"source": "room.nbt", "sha256": digest(OUT / "preview.png"), "size": [1760, 1100], "kind": "software vanilla-model cutaway", "visually_reviewed": True, "not_game_screenshot": True},
        "evidence": evidence, "player_saves_modified": False, "deployed_jars_modified": False,
        "core_modified": False, "other_rooms_modified": False, "deployed": False,
        "paid_image_calls": 0, "integration_status": "Independent S6 delivery; core owner must register, connect and test before deploying",
    }
    dump(OUT / "qa.json", result)
    files = {name: {"bytes": (OUT / name).stat().st_size, "sha256": digest(OUT / name)} for name in ("room.nbt", "room.json", "preview.png", "qa.json")}
    handoff = {
        "recipient_thread": "01a19758-b0e5-75c2-822a-4860527a1e8b", "room_id": "S6", "status": "READY_FOR_INTEGRATION_REVIEW_NOT_DEPLOYED",
        "delivery_directory": str(OUT), "files": files, "reference": result["reference"],
        "structure_sha256": structure_hash, "size_xyz": [9, 7, 9], "entrance": [4, 1, 0], "interior": [4, 1, 2], "facing": "north",
        "validation": {"static": "PASS", "forge": "1.20.1-47.4.10", "native_placement": "PASS, four rotations, 240 ticks, forced neighbor/fluid updates", "native_roundtrip": "EXACT_MATCH", "safe_reachable_positions_per_rotation": 31, "dripstone_native_lava_resolution": "PASS", "reward_inventory_items": 0, "loot_tables": 0},
        "evidence": evidence, "design_adaptations": metadata["adaptations"],
        "deployment_authorized": False, "integration_constraints": ["Dense 9x7x9 structure includes air; check full destination volume before placing, never unconditionally clear an occupied volume", "Only north opening, 3 wide by 3 high with full in-template floor", "Keep independent persistent generation marker, preserve player changes on re-entry", "No other room, player save or installed JAR was modified"],
        "limitations": native["limitations"],
        "thread_message_sent": False,
    }
    dump(ROOT / "handoff.json", handoff)
    print(json.dumps({"status": "PASS", "sha256": structure_hash, "delivery": str(OUT), "files": files}, ensure_ascii=True))


if __name__ == "__main__":
    main()
