import hashlib
import json
import sys
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    records = []
    for name in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records.extend(load(root / name)["files"])
    captured = {record["source"]: record for record in records}
    for record in captured.values():
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    manifest = load(root / "ready-for-review.json")
    specification = manifest["assembly"]
    source_path = Path(captured[specification["candidate"]]["snapshot"])
    source, before = decode(source_path)
    world_root = root / "qa/merged-world-r2"
    world = load(world_root / "world-report.json")
    assert load(world_root / "exit.json")["exit_code"] == 0
    assert len(world["reports"]) == 1
    observation = world["reports"][0]
    assert observation["sha256"] == specification["sha256"]
    assert observation["ticks"] == world["natural_ticks"] == 800
    assert world["random_tick_speed"] == 3 and world["do_fire_tick"] is True
    assert world["wheat_probe_grown"] > 0
    for field in ("escaped_water", "lost_source_water", "dropped_items"):
        assert observation[field] == []
    assert observation["fire_blocks"] == 0
    route = load(root / "independent-qa/route-input.json")[-1]
    assert route["sha256"] == specification["sha256"]
    phases = []
    inputs = []
    empty = {"state": {"Name": "minecraft:air"}, "nbt": None}
    for phase_number, phase in enumerate(("after-neighbor-updates", "after-800-ticks", "after-player-interactions")):
        path = world_root / "S4-assembly" / (phase + ".nbt")
        record = next(record for record in captured.values() if Path(record["snapshot"]) == path)
        after_data, raw_after = decode(path)
        assert after_data["size"] == [source["size"][0] + 4, source["size"][1] + 2, source["size"][2] + 4]
        after = {(point[0] - 2, point[1], point[2] - 2): value for point, value in raw_after.items() if 2 <= point[0] < source["size"][0] + 2 and 0 <= point[1] < source["size"][1] and 2 <= point[2] < source["size"][2] + 2}
        types, states, payloads = [], [], []
        for point in before.keys() | after.keys():
            expected, actual = before.get(point, empty), after.get(point, empty)
            if expected["state"]["Name"] != actual["state"]["Name"]:
                types.append({"position": point, "before": expected["state"], "after": actual["state"]})
            elif expected["state"] != actual["state"]:
                states.append({"position": point, "before": expected["state"], "after": actual["state"]})
            if expected["nbt"] != actual["nbt"]:
                payloads.append({"position": point, "before": expected["nbt"], "after": actual["nbt"]})
        assert all(change["before"]["Name"] == "minecraft:air" and change["after"]["Name"] == "minecraft:vine" for change in types)
        phases.append({"phase": phase, "sha256": record["sha256"], "full_source_volume_checked": len(before), "type_changes": types, "raw_state_changes": states, "raw_be_changes": payloads, "captured_entities": after_data["entities"]})
        def shifted(point):
            return [point[0] + 2, point[1], point[2] + 2]
        inputs.append(dict(route, candidate=str(path), sha256=record["sha256"], group=490 + phase_number, size=after_data["size"], start=shifted(route["start"]), targets=[dict(target, position=shifted(target["position"])) for target in route["targets"]]))
    interaction = load(world_root / "merged-interaction.json")
    targets = interaction["live_targets"]
    assert len(targets) == len(route["targets"]) == 56
    assert Counter(target["label"] for target in targets) == Counter(target["label"] for target in route["targets"])
    assert all(target["roundtrip"] for target in targets)
    operations = interaction["block_interactions"]
    assert len(operations) == 38
    assert all(operation["ray_hit"] and operation["eye_distance"] <= 4.5 for operation in operations)
    doors = [operation for operation in operations if "door_opened" in operation]
    assert len(doors) == 6 and all(operation["door_opened"] and operation["door_closed"] for operation in doors)
    containers = [operation for operation in operations if "door_opened" not in operation]
    assert len(containers) == 32 and all(not operation["menu"].endswith("InventoryMenu") for operation in containers)
    prisms = interaction["full_interface_prisms"]
    assert len(prisms) == 155 and all(prism["clear"] for prism in prisms)
    destination = root / "independent-posttick-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    report = {"stage": "INDEPENDENT_INSPECTION_OF_FROZEN_PRODUCER_WORLD_EVIDENCE", "source_sha256": specification["sha256"], "bound_files_verified": len(captured), "natural_ticks": 800, "random_tick_speed": 3, "wheat_probe_grown": world["wheat_probe_grown"], "live_roundtrip_targets": len(targets), "reachable_ray_container_uses": len(containers), "reachable_ray_lever_open_close_uses": len(doors), "full_interface_prisms": len(prisms), "phases": phases, "limitations": ["Producer world run independently inspected, no parent world rerun", "MergedInteraction uses no-gravity FakePlayer and explicit vertical MoverType.SELF displacements; not natural climbing or network client input", "Closed exterior terminal faces still lack installed usable external access", "Posttick routes prepared here require independent Java execution"], "accepted": False}
    (root / "independent-qa/world-r2-inspection.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "phases"}, ensure_ascii=False))
    for phase in phases:
        print(json.dumps({"phase": phase["phase"], "types": len(phase["type_changes"]), "raw_states": len(phase["raw_state_changes"]), "raw_be": len(phase["raw_be_changes"]), "entities": len(phase["captured_entities"])}, ensure_ascii=False))


if __name__ == "__main__":
    main()
