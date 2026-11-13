from __future__ import annotations

import gzip
import json
from datetime import datetime
from zoneinfo import ZoneInfo

from PIL import Image

from build_r6 import REFERENCE, REFERENCE_HASH, ROOT, Structure, hash_file, write_json


def main():
    metadata = json.loads((ROOT / "room.json").read_text(encoding="utf-8"))
    structure = Structure.read(ROOT / "room.nbt")
    assert hash_file(ROOT / "room.nbt") == metadata["sha256"]
    assert hash_file(REFERENCE) == REFERENCE_HASH
    reports = {name: json.loads((ROOT / path).read_text(encoding="utf-8")) for name, path in {"static": "qa/static.json", "negative": "qa/negative.json", "native": "qa/native.json", "preview": "qa/preview.json"}.items()}
    assert all(report["status"] == "PASS" for report in reports.values())
    assert reports["native"]["structure_sha256"] == reports["preview"]["source_structure_sha256"] == metadata["sha256"]
    console = (ROOT / "evidence/forge-native-console.log").read_text(encoding="utf-8", errors="replace")
    assert "R6_NATIVE_PLACEMENT_PASS" in console and "All 1 required tests passed" in console and "BUILD SUCCESSFUL" in console
    assert reports["native"]["rotation_count"] == 4 and reports["native"]["stability_server_ticks"] == 200
    assert len(reports["native"]["checks"]) == 8
    assert reports["native"]["forced_chunk_ticking"]
    assert min(check["minimum_crop_raw_brightness"] for check in reports["native"]["checks"]) >= 8
    assert all(check["state_mismatches"] == 0 and check["escaped_fluids"] == 0 for check in reports["native"]["checks"])
    with Image.open(ROOT / "preview.png") as image:
        image.verify()
    semantic_rows = [[*position, block.name, dict(block.properties)] for position, block in sorted(structure.blocks.items())]
    semantic_payload = json.dumps(semantic_rows, ensure_ascii=True, separators=(",", ":")).encode("utf-8")
    import hashlib

    semantic_hash = hashlib.sha256(semantic_payload).hexdigest()
    stable_identity = {"id": "R6", "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "facing": "north", "semantic_sha256": semantic_hash, "reference_sha256": REFERENCE_HASH}
    identity_payload = json.dumps(stable_identity, sort_keys=True, ensure_ascii=True, separators=(",", ":")).encode("utf-8")
    evidence = ["qa/static.json", "qa/negative.json", "qa/native.json", "qa/materials.json", "qa/preview.json", "qa/cutaway.png", "qa/full-shell.png", "evidence/forge-native-console.log", "evidence/forge-native-server.log", "evidence/forge-attempt1-crop-light.log", "evidence/attempt1-server.log"]
    qa = {
        "status": "PASS", "id": "R6", "name": metadata["name"], "completed_local": datetime.now(ZoneInfo("Asia/Shanghai")).isoformat(),
        "scope": "Independent unregistered room delivery; no deployment, player save access, core changes or parallel-delivery writes.",
        "structure_sha256": metadata["sha256"],
        "uncompressed_nbt_sha256": hashlib.sha256(gzip.decompress((ROOT / "room.nbt").read_bytes())).hexdigest(),
        "semantic_sha256": semantic_hash,
        "semantic_hash_algorithm": "SHA-256 of UTF-8 compact ensure_ascii=true JSON sorted rows [x,y,z,block_id,sorted_properties]",
        "independent_identity": stable_identity,
        "independent_identity_sha256": hashlib.sha256(identity_payload).hexdigest(),
        "identity_algorithm": "SHA-256 of UTF-8 JSON independent_identity, sort_keys=true, ensure_ascii=true, compact separators. Not an FT runtime catalog fingerprint.",
        "reference": str(REFERENCE), "reference_sha256": REFERENCE_HASH,
        "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "facing": "north",
        "static": {key: value for key, value in reports["static"].items() if key not in {"reachable", "limits"}},
        "negative_tests": {"status": reports["negative"]["status"], "count": len(reports["negative"]["cases"]), "report": "qa/negative.json"},
        "native": {key: value for key, value in reports["native"].items() if key not in {"checks", "limits"}},
        "native_check_summary": {"count": 8, "total_state_comparisons_including_immediate": 2550 * 12, "minimum_crop_brightness_at_midnight": min(check["minimum_crop_raw_brightness"] for check in reports["native"]["checks"]), "native_reachable_cells": sorted({check["native_reachable_cells"] for check in reports["native"]["checks"]}), "escaped_fluids": 0, "state_mismatches": 0},
        "preview": {"method": reports["preview"]["method"], "size": reports["preview"]["size"], "sha256": hash_file(ROOT / "preview.png")},
        "design_review": {"glass_shell_and_roof": "Continuous glass envelope except deliberate north 3x3 entrance; roof checked across 255 cells.", "plants": "78 persistent leaves, 10 supported mature crops on hydrated farmland, four bed shroomlights, 46 roof-anchored fixed-length vines, soil-supported flowers.", "paths": "Three-wide central dry aisle plus side circulation; north entrance and interior connected with native 0.6x1.8x0.6 player AABB.", "water": "Eight true sources, stone bottom and sealed side beds; 6118-cell halo per rotation contains no escaped liquid.", "suspensions": "Two dirt-filled trapdoor baskets and two hanging lanterns, each chained directly to continuous roof beams.", "reference_adaptations": metadata["reference_adaptations"]},
        "iteration_history": [{"attempt": 1, "result": "FAIL", "reason": "UPDATE_ALL default placement before lighting settled caused wheat to drop; recorded rather than reported as pass."}, {"attempt": 2, "result": "PASS", "changes": "Added four shroomlights; matched existing FT production known-shape flags 18; settled light for 20 ticks, then forced neighbor checks and 200 further ticking ticks at midnight. All placement chunks force-tick."}],
        "evidence": [{"path": path, "sha256": hash_file(ROOT / path)} for path in evidence],
        "limits": reports["native"]["limits"] + reports["preview"]["limits"] + ["No new paid image request; existing image2.5 design only", "Current native test guarantees production-equivalent placement flags, not unsafe UPDATE_ALL placement in unlit newly loaded chunks", "No catalog slot or fixed world origin assigned; no hallway connection, generation-marker idempotence or occupancy rejection claim"]
    }
    write_json(ROOT / "qa.json", qa)
    tracked = ["room.nbt", "room.json", "preview.png", "qa.json", "README.md", "HANDOFF.md", "Run-QA.ps1"] + evidence
    tracked += [str(path.relative_to(ROOT)).replace("\\", "/") for path in (ROOT / "tools").rglob("*.py")]
    tracked += [str(path.relative_to(ROOT)).replace("\\", "/") for path in (ROOT / "native-test/src").rglob("*") if path.is_file()]
    tracked += ["native-test/build.gradle", "native-test/settings.gradle", "native-test/gradle.properties", "native-test/gradle/wrapper/gradle-wrapper.jar", "native-test/gradle/wrapper/gradle-wrapper.properties"]
    manifest = {"id": "R6", "status": "PASS", "root": str(ROOT), "reference_sha256": REFERENCE_HASH, "hash_algorithm": "SHA-256", "files": [{"path": path, "bytes": (ROOT / path).stat().st_size, "sha256": hash_file(ROOT / path)} for path in sorted(set(tracked))], "excludes": ["This manifest itself", "Native Gradle caches/build products and disposable GameTest world", "Python bytecode caches"]}
    write_json(ROOT / "delivery-manifest.json", manifest)
    print(json.dumps({"status": "PASS", "room_nbt": metadata["sha256"], "room_json": hash_file(ROOT / "room.json"), "preview_png": hash_file(ROOT / "preview.png"), "qa_json": hash_file(ROOT / "qa.json"), "semantic": semantic_hash, "manifest_files": len(manifest["files"])}))


if __name__ == "__main__":
    main()
