import argparse
import hashlib
import json
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    snapshot = load(root / "snapshot.json")
    for record in snapshot["files"]:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    source_root = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2\runtime-qa-r2")
    destination = root / "maker-world-evidence"
    destination.mkdir()
    sources = list(source_root.glob("*.json")) + list(source_root.glob("*.log")) + list(source_root.glob("*.ps1")) + list(source_root.glob("*.nbt")) + list(source_root.glob("native-test/src/**/*.java"))
    captured = []
    for source in sources:
        payload = source.read_bytes()
        target = destination / source.relative_to(source_root)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        captured.append({"source": str(source), "snapshot": str(target), "sha256": hashlib.sha256(payload).hexdigest()})
    report = load(destination / "12-prison-block-runtime.json")
    source_room = next(room for room in snapshot["rooms"] if room["id"] == "S2-12")
    assert report["source_sha256"] == source_room["sha256"]
    before, original = decode(Path(source_room["snapshot"]))
    after, observed = decode(destination / "12-prison-block-after.nbt")
    assert before["size"] == after["size"]
    changed_types = []
    changed_properties = []
    for point, value in original.items():
        actual = observed.get(point)
        if (actual["state"]["Name"] if actual else "minecraft:air") != value["state"]["Name"]:
            changed_types.append({"position": list(point), "before": value, "after": actual})
        elif actual["state"] != value["state"]:
            changed_properties.append({"position": list(point), "before": value["state"], "after": actual["state"]})
    original_doors = {point for point, value in original.items() if value["state"]["Name"] == "minecraft:iron_door" and value["state"]["Properties"]["half"] == "lower"}
    assert len(original_doors) == 8 and {tuple(door["position"]) for door in report["doors"]} == original_doors
    assert all(door["lever_found"] and not door["before_open"] and door["after_use_open"] and not door["after_second_use_open"] for door in report["doors"])
    for record in captured:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    coverage = root / "control-coverage-qa"
    coverage.mkdir()
    route = next(entry for entry in load(root / "route-input.json") if entry["group"] == 212)
    original_targets = list(route["targets"])
    route["targets"] += [{"label": f"independent-control-standing-{east}-{south}", "position": [east, 2, south]} for east in (10, 16) for south in (3, 10, 17, 24)]
    (coverage / "route-input.json").write_text(json.dumps([route], ensure_ascii=False, indent=2), encoding="utf-8")
    (coverage / "vanilla-block-tags.json").write_bytes((root / "vanilla-block-tags.json").read_bytes())
    result = {"stage": "INDEPENDENT_INSPECTION_OF_MAKER_PRISON_WORLD_EVIDENCE_AND_ADDED_CONTROL_INPUT", "candidate_sha256": source_room["sha256"], "maker_world_files": captured, "maker_ticks": report["ticks"], "door_roundtrips_observed_in_maker_report": 8, "source_block_type_changes": changed_types, "source_property_changes": changed_properties, "original_static_targets": len(original_targets), "additional_control_standing_targets": 8, "accepted": False, "limitations": ["Maker test uses FakePlayer and LeverBlock.use; no physical player path, reach or line-of-sight assertion", "Maker harness records most findings without assertions; exit zero alone is not sufficient", "Parent inspected source, logs, report and NBT but did not independently run this world test", "Full cell-interior route in open door configuration and ladder climbing remain unverified"]}
    (root / "independent-prison-world-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"world_files_captured": len(captured), "block_type_changes": len(changed_types), "property_changes": len(changed_properties), "door_roundtrips_in_report": 8, "added_control_standing_targets": 8, "accepted": False}))


if __name__ == "__main__":
    main()
