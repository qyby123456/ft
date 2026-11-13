import argparse
import hashlib
import json
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest_bytes = (root / "ready-for-review.json").read_bytes()
    manifest = load(root / "ready-for-review.json")
    maker = Path(manifest["rooms"][0]["folder"]).parent
    assert (maker / "ready-for-review.json").read_bytes() == manifest_bytes
    destination = root / "maker-world-evidence"
    destination.mkdir()
    sources = [maker / "assembly/parts/index.json"]
    for room in manifest["rooms"]:
        runtime = Path(room["runtime_report"])
        assert runtime.is_relative_to(maker)
        sources.extend([runtime, runtime.with_name(runtime.stem.replace("-runtime", "-after") + ".nbt")])
        parent = runtime.parent
        sources.extend(parent.glob("*.json"))
        sources.extend(parent.glob("*.log"))
        sources.extend(parent.glob("*.ps1"))
        sources.extend(parent.glob("native-test/src/**/*.java"))
        sources.extend(parent.glob("native-test/build/test-results/**/*.xml"))
    captured = {}
    records = []
    for source in dict.fromkeys(sources):
        assert source.is_relative_to(maker) and source.is_file()
        payload = source.read_bytes()
        target = destination / source.relative_to(maker)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        captured[str(source)] = target
        records.append({"source": str(source), "snapshot": str(target), "sha256": hashlib.sha256(payload).hexdigest(), "bytes": len(payload)})
    static_records = load(root / "submission-snapshot.json")["files"]
    originals = {record["source"]: Path(record["snapshot"]) for record in static_records}
    results = []
    for room in manifest["rooms"]:
        runtime = Path(room["runtime_report"])
        report = load(captured[str(runtime)])
        assert report["source_sha256"] == room["sha256"]
        assert load(captured[str(runtime.parent / "exit.json")])["exit_code"] == 0
        after = captured[str(runtime.with_name(runtime.stem.replace("-runtime", "-after") + ".nbt"))]
        original_data, original_blocks = decode(originals[room["candidate"]])
        after_data, after_blocks = decode(after)
        assert original_data["size"] == after_data["size"]
        changed_types = []
        state_changes = []
        for point in original_blocks.keys() | after_blocks.keys():
            before = original_blocks.get(point, {"state": {"Name": "minecraft:air"}, "nbt": None})
            observed = after_blocks.get(point, {"state": {"Name": "minecraft:air"}, "nbt": None})
            if before["state"]["Name"] != observed["state"]["Name"]:
                changed_types.append({"position": point, "before": before, "after": observed})
            elif before["state"] != observed["state"]:
                state_changes.append({"position": point, "before": before["state"], "after": observed["state"]})
        results.append({"id": room["id"], "sha256": room["sha256"], "maker_ticks": report.get("ticks"), "maker_report_lost_water": report.get("lost_water"), "maker_report_spread_water": report.get("spread_water"), "maker_report_dropped_items": report.get("dropped_items"), "independent_type_changes": changed_types, "independent_raw_state_changes": state_changes, "before_structure_entities": len(original_data["entities"]), "after_structure_entities": len(after_data["entities"])})
    ladder = load(destination / "runtime-qa-r4/ladder-physics.json")
    assert ladder["source_sha256"] == manifest["rooms"][0]["sha256"]
    assert ladder["top_landing_clear"] and ladder["climbable_observed"]
    assert any(sample["on_ground"] and not sample["on_climbable"] and sample["position"][1] >= 9 for sample in ladder["trace"])
    for record in records:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    assert (maker / "ready-for-review.json").read_bytes() == manifest_bytes
    report = {"stage": "INDEPENDENT_INSPECTION_OF_MAKER_WORLD_REPORTS_AND_AFTER_NBT", "files": records, "rooms": results, "ladder_current_source_hash_bound": True, "ladder_standing_top_observed_in_maker_trace": True, "accepted": False, "limitations": ["Maker world execution, not parent independent world rerun", "Raw state differences include omitted native defaults and connection shapes; classification still required", "Block entity payload content and real container GUI interactions not audited here", "Finite 220 tick reports do not guarantee random tick or long-term stability", "No merged world run; ladder trace/code semantics require separate source review"]}
    (root / "independent-maker-world-inspection.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"files": len(records), "rooms": len(results), "type_changes": sum(len(room["independent_type_changes"]) for room in results), "raw_state_changes": sum(len(room["independent_raw_state_changes"]) for room in results), "current_ladder_trace_bound": True}, ensure_ascii=False))


if __name__ == "__main__":
    main()
