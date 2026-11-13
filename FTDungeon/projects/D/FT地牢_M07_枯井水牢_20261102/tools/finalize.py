from __future__ import annotations

import hashlib
import json

from PIL import Image

from build_m07 import OUT, REFERENCE, REFERENCE_SHA, ROOT, dump
from nbtio import Structure


def read_json(path):
    return json.loads(path.read_text(encoding="utf-8"))


def fingerprint(path):
    return {"path": str(path.relative_to(ROOT)), "bytes": path.stat().st_size, "sha256": hashlib.sha256(path.read_bytes()).hexdigest()}


def main():
    metadata = read_json(OUT / "room.json")
    digest = hashlib.sha256((OUT / "room.nbt").read_bytes()).hexdigest()
    assert metadata["id"] == "M07" and metadata["sha256"] == digest
    assert metadata["reference_sha256"] == REFERENCE_SHA == hashlib.sha256(REFERENCE.read_bytes()).hexdigest()
    source = Structure.read(OUT / "room.nbt")
    native = Structure.read(ROOT / "qa/native-roundtrip.nbt")
    assert source.size == native.size and source.data_version == native.data_version == 3465
    assert source.blocks == native.blocks and source.block_entities == native.block_entities and source.entities == native.entities
    roundtrip = {"id": "M07", "status": "PASS", "sha256": digest, "native_nbt": fingerprint(ROOT / "qa/native-roundtrip.nbt"), "states_compared": len(source.blocks), "block_entities_compared": len(source.block_entities), "exact_states_equal": True, "exact_block_entity_nbt_equal": True, "entities_equal": True, "size_and_data_version_equal": True, "note": "Native serializer may reorder palette/records and gzip differently; decoded data, not compressed bytes, are compared"}
    dump(ROOT / "qa/roundtrip.json", roundtrip)
    reports = {name: read_json(ROOT / ("qa/" + name + ".json")) for name in ("static", "forge-native", "render", "visual-review")}
    assert all(report["status"] == "PASS" and report["sha256"] == digest for report in reports.values())
    runtime = reports["forge-native"]
    assert runtime["ticks_observed"] == 240 and len(runtime["rotations"]) == 4
    assert {rotation["rotation"] for rotation in runtime["rotations"]} == {"NONE", "CLOCKWISE_90", "CLOCKWISE_180", "COUNTERCLOCKWISE_90"}
    assert all(rotation["states_preserved"] == 3315 and rotation["fluid_leaks"] == rotation["dropped_items"] == 0 for rotation in runtime["rotations"])
    log = (ROOT / "qa/forge-run.log").read_text(encoding="utf-8", errors="replace")
    markers = ("M07_FORGE_PLACEMENT_PASS", "All 1 required tests passed", "BUILD SUCCESSFUL")
    assert all(marker in log for marker in markers)
    image_dimensions = {}
    for path in (OUT / "preview.png", ROOT / "qa/four-views.png", ROOT / "qa/exterior.png", ROOT / "qa/floor-plan.png"):
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            image_dimensions[str(path.relative_to(ROOT))] = list(image.size)
    limitations = [
        "No deployment, no FT core/catalog/route changes and no access to permanent player saves.",
        "Software NBT previews are not in-game screenshots; no full-modpack client, multiplayer or FPS verification.",
        "Winch, lever, shackles and grates are architectural props, not a scripted trap or reward mechanic.",
        "Water is intentionally sealed below dry routes; no claim of a visible flowing water surface.",
        "Integration must use entrance feet Y=4, keep M07 distinct from M7, refuse occupied whole volume plus margins, provide a hall connection and a separate persistent generation marker."
    ]
    summary = {
        "id": "M07", "name": metadata["name"], "status": "PASS", "sha256": digest,
        "size_xyz": list(source.size), "data_version": 3465, "block_records": len(source.blocks),
        "entrance": metadata["entrance"], "interior": metadata["interior"], "facing": "north",
        "reference_sha256": REFERENCE_SHA, "static": reports["static"],
        "forge_native": runtime, "roundtrip": roundtrip,
        "preview": {"source": "Actual delivered NBT", "software_render_only": True, "image_dimensions": image_dimensions, "visual_review": "qa/visual-review.json"},
        "evidence": {name: "qa/" + name + ".json" for name in reports},
        "forge_log": "qa/forge-run.log", "required_log_markers": list(markers),
        "paid_image_generation_calls": 0, "deployed": False, "limits": limitations
    }
    dump(OUT / "qa.json", summary)
    deliverables = [OUT / name for name in ("room.nbt", "room.json", "preview.png", "qa.json")]
    evidence = [ROOT / path for path in ("qa/static.json", "qa/forge-native.json", "qa/roundtrip.json", "qa/native-roundtrip.nbt", "qa/forge-run.log", "qa/render.json", "qa/visual-review.json", "qa/four-views.png", "qa/exterior.png", "qa/floor-plan.png")]
    handoff = {"id": "M07", "name": "枯井水牢", "status": "PASS_INDEPENDENT_DELIVERY_NOT_DEPLOYED", "root": str(ROOT), "delivery_directory": str(OUT), "nbt_sha256": digest, "size_xyz": list(source.size), "entrance_feet": metadata["entrance"], "safe_interior_feet": metadata["interior"], "deliverables": [fingerprint(path) for path in deliverables], "evidence": [fingerprint(path) for path in evidence], "runtime_summary": {"minecraft": "1.20.1", "forge": "47.4.10", "rotations": 4, "ticks": 240, "records_each_rotation": 3315, "collision_reachable_positions_each_rotation": 141, "water_sources_each_rotation": 33, "fluid_leaks": 0, "dropped_items": 0}, "deployed": False, "limits": limitations}
    dump(ROOT / "handoff.json", handoff)
    print(json.dumps({"status": handoff["status"], "nbt_sha256": digest, "delivery": str(OUT), "files": len(deliverables)}))


if __name__ == "__main__":
    main()
