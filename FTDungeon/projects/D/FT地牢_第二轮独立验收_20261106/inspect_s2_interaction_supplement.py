import hashlib
import json
import math
import sys
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root, prior = [Path(value).resolve() for value in sys.argv[1:3]]
    snapshot = load(root / "supplement-snapshot.json")
    for record in snapshot["files"]:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    old_records = load(prior / "submission-snapshot.json")["files"] + load(prior / "supplemental-snapshot.json")["files"]
    old_records = {record["source"]: record for record in old_records}
    for record in old_records.values():
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    source_hash = snapshot["source_sha256"]
    runtime = load(root / "runtime-interaction-r2/interaction-runtime.json")
    assert runtime["source_sha256"] == source_hash and runtime["pass"] is True and runtime["ticks"] == 640
    assert load(root / "runtime-interaction-r2/exit.json")["exit_code"] == 0
    fixtures = runtime["fixtures"]
    assert len(fixtures) == 40 and len({item["fixture"]["label"] for item in fixtures}) == 40
    for item in fixtures:
        assert item["pass"] and item["first_hit_is_fixture"] and item["within_survival_reach"] and item["body_clear_at_use"] and item["station_clear_supported"]
        assert math.dist(item["actual_eye"], item["actual_hit"]) <= 4.5
        assert abs(math.dist(item["actual_eye"], item["actual_hit"]) - item["eye_hit_distance"]) < 1e-8
        if item["fixture"]["kind"] == "container":
            assert item["server_menu_opened"] and item["occupied_slots"] == 0
        else:
            assert item["door_opened"] and item["door_restored"] and item["second_ray_valid"]
    jumps = [item for item in fixtures if item["requires_jump"]]
    assert len(jumps) == 1 and jumps[0]["fixture"]["position"] == [197, 11, 93]
    trace = runtime["jump_trace"]
    assert len(trace) == 1 and trace[0]["target_first"] and trace[0]["tick"] == 629
    assert abs(trace[0]["feet"][1] - jumps[0]["station"][1] - 0.419999986886978) < 1e-7
    assert abs(trace[0]["vertical_velocity"] - 0.33319999363422365) < 1e-7
    inputs = []
    fine = []
    geometries = []
    source_labels = {item["fixture"]["label"] for item in fixtures}
    for index, name in enumerate(("current_source-interactions.json", "real_server_posttick-interactions.json")):
        geometry = load(root / "qa/interaction-range-current" / name)
        assert geometry["merged_candidate_sha256"] == source_hash
        assert {item["fixture"]["label"] for item in geometry["fixtures"]} == source_labels
        record = next(record for record in old_records.values() if record["sha256"] == geometry["source_sha256"])
        candidate = Path(record["snapshot"])
        data, blocks = decode(candidate)
        starts, targets = [], []
        for item in geometry["fixtures"]:
            assert item["pass"] and item["standing_clear_and_supported"] and item["native_outline_first_hit_is_fixture"]
            assert item["eye_to_hit_distance"] <= 4.5
            path = item["path_from_entrance"]
            starts.append(path[0])
            coarse = next(point for point in reversed(path) if all(abs(value * 2 - round(value * 2)) < 1e-8 for value in point))
            fine.append({"geometry": name, "label": item["fixture"]["label"], "coarse": coarse, "standing": item["standing_feet"], "requires_fine_offset": coarse != item["standing_feet"]})
            assert math.dist(coarse, item["standing_feet"]) <= 0.2 + 1e-8
            targets.append({"label": item["fixture"]["label"], "position": [coarse[0] - 0.5, coarse[1], coarse[2] - 0.5]})
        assert all(point == starts[0] for point in starts)
        start = starts[0]
        inputs.append({"group": 280 + index, "candidate": str(candidate), "sha256": record["sha256"], "size": data["size"], "start": [start[0] - 0.5, start[1], start[2] - 0.5], "targets": targets, "swimming": False})
        geometries.append({"name": name, "sha256": record["sha256"], "fixtures": len(targets)})
    destination = root / "independent-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((prior / "independent-qa/vanilla-block-tags.json").read_bytes())
    report = {"stage": "INDEPENDENT_SCOPED_INTERACTION_EVIDENCE_INSPECTION", "source_sha256": source_hash, "supplement_files_verified": len(snapshot["files"]), "original_frozen_files_reverified": len(old_records), "runtime_fixtures": len(fixtures), "server_menus_opened_empty": sum(item["fixture"]["kind"] == "container" for item in fixtures), "doors_opened_and_restored": sum(item["fixture"]["kind"] == "door_lever" for item in fixtures), "standing_interactions": 39, "native_jump_interactions": 1, "max_eye_hit_distance": max(item["eye_hit_distance"] for item in fixtures), "actual_jump_rise_observed": trace[0]["feet"][1] - jumps[0]["station"][1], "runtime_ticks": 640, "geometries": geometries, "fine_offsets": [item for item in fine if item["requires_fine_offset"]], "limitations": ["Producer world independently inspected, not parent world rerun", "Local interactions start with moveTo; full entry paths supplied offline", "Independent route targets are coarse half-grid approach stations; the well's final 0.19 offset uses separate 11-sample and actual-world body evidence", "One successful native jump frame does not establish a complete jump arc or return landing", "No network client UI, survival or external-access acceptance"], "accepted": False}
    (destination / "interaction-inspection.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
