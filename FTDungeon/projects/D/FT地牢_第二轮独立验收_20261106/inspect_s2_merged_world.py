import hashlib
import json
import math
from pathlib import Path
import sys

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    records = load(root / "submission-snapshot.json")["files"] + load(root / "supplemental-snapshot.json")["files"]
    for record in records:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    manifest = load(root / "ready-for-review.json")
    assembly = manifest["assembly"]
    candidate = next(Path(record["snapshot"]) for record in records if record["source"] == assembly["candidate"])
    before, original = decode(candidate)
    after, final = decode(root / "runtime-assembly-r1/assembly-after.nbt")
    assert before["size"] == after["size"]
    report = load(root / "runtime-assembly-r1/assembly-runtime.json")
    inputs = load(root / "runtime-assembly-r1/assembly-input.json")
    assert report["source_sha256"] == inputs["sha256"] == assembly["sha256"]
    assert load(root / "runtime-assembly-r1/exit.json")["exit_code"] == 0
    assert load(root / "runtime-assembly-r1/execution-result.json")["pass"] is True
    air = {"state": {"Name": "minecraft:air"}, "nbt": None}
    type_changes = []
    state_changes = 0
    block_entity_changes = 0
    for point in original.keys() | final.keys():
        first, second = original.get(point, air), final.get(point, air)
        if first["state"]["Name"] != second["state"]["Name"]:
            type_changes.append({"position": point, "before": first["state"]["Name"], "after": second["state"]["Name"]})
        state_changes += first["state"] != second["state"]
        block_entity_changes += first["nbt"] != second["nbt"]
    route = inputs["movement_route"]
    assert route[0] == route[-1]
    assert len(route) == report["movement_waypoints_total"] == report["movement_waypoints_reached"]
    assert report["movement_roundtrip_complete"]
    end_distance = math.dist(route[-1], report["movement_end"])
    assert end_distance < 0.3
    trace = report["movement_trace"]
    assert all(first["tick"] < second["tick"] and first["waypoint"] <= second["waypoint"] for first, second in zip(trace, trace[1:]))
    assert all(door["lever_found"] and door["opened"] and door["restored"] for door in report["doors"])
    assert all(container["server_menu_opened"] for container in report["containers"])
    assert not report["lost_water"] and not report["spread_water"] and not report["dropped_items"]
    result = {"stage": "INDEPENDENT_INSPECTION_OF_PRODUCER_MERGED_WORLD_EVIDENCE", "source_sha256": assembly["sha256"], "source_records": len(original), "after_records": len(final), "type_changes": type_changes, "raw_state_changes": state_changes, "raw_block_entity_changes": block_entity_changes, "server_ticks": report["server_ticks"], "natural_wait_ticks": report["natural_wait_ticks"], "movement_waypoints": len(route), "trace_records": len(trace), "return_endpoint_distance": end_distance, "doors_open_restore": len(report["doors"]), "server_menus_opened": len(report["containers"]), "occupied_container_slots": sum(container["occupied_slots"] for container in report["containers"]), "water_loss_spread_and_drops": 0, "accepted": False, "limitations": ["Producer world output inspected independently; parent has not rerun this server test", "Movement source was inspected: initial moveTo then Player.travel; separate operator moves for containers, so menus do not prove player reach or line of sight", "Raw state changes include omitted default properties and connection updates; raw block entity differences are not fully classified", "No human client, combat, arbitrary rotations, restart or random tick coverage certification"]}
    (root / "independent-qa/merged-world-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
