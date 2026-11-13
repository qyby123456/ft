import hashlib
import json
import math
import sys
from collections import Counter
from itertools import combinations
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    records = []
    for name in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records.extend(load(root / name)["files"])
    records = {record["source"]: record for record in records}
    for record in records.values():
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    def frozen(source):
        return Path(records[source]["snapshot"])
    manifest = load(root / "ready-for-review.json")
    source_specs = {Path(room["candidate"]).parent.name: room for room in manifest["rooms"]}
    source_specs["merged"] = manifest["merged"]
    report = load(root / "worldqa-run4/world-report.json")
    assert load(root / "worldqa-run4/exit.json")["exit_code"] == 0
    assert report["issues"] == 0 and report["doFireTick"] is True and report["randomTickSpeed"] == 3
    assert len(report["candidates"]) == 11
    original_routes = load(root / "independent-qa/route-input.json")
    after_specs = {item["id"]: item for item in load(root / "worldqa-run4/posttick-inputs.json")}
    summaries, routes = [], []
    empty = {"state": {"Name": "minecraft:air"}, "nbt": None}
    for index, candidate in enumerate(report["candidates"]):
        identifier = candidate["id"]
        specification = source_specs[identifier]
        assert candidate["source_sha256"] == specification["sha256"]
        assert candidate["elapsed_ticks"] == 1200
        assert not candidate["support_failures"] and not candidate["dropped_items"]
        assert candidate["fires"] == candidate["new_fluid_cells"] == candidate["interaction_issues"] == 0
        source, before = decode(frozen(specification["candidate"]))
        rebuilt_spec = after_specs[identifier]
        assert rebuilt_spec["original_sha256"] == specification["sha256"]
        rebuilt_data, rebuilt = decode(frozen(rebuilt_spec["candidate"]))
        assert rebuilt_data["size"] == source["size"]
        types, states, payloads = [], [], []
        state_kinds = Counter()
        boxes = []
        capture_volume = 0
        for capture in candidate["after_captures"]:
            path = frozen(capture["file"])
            assert hashlib.sha256(path.read_bytes()).hexdigest() == capture["sha256"]
            data, blocks = decode(path)
            offset, size = capture["offset"], capture["size"]
            assert data["size"] == size and len(blocks) == math.prod(size)
            assert all(0 <= offset[axis] and offset[axis] + size[axis] <= source["size"][axis] for axis in range(3))
            boxes.append((offset, size))
            capture_volume += len(blocks)
            for point, actual in blocks.items():
                absolute = tuple(point[axis] + offset[axis] for axis in range(3))
                expected = before.get(absolute, empty)
                assert rebuilt.get(absolute, empty) == actual, (identifier, absolute, "posttick reassembly mismatch")
                if expected["state"]["Name"] != actual["state"]["Name"]:
                    types.append({"position": absolute, "before": expected["state"], "after": actual["state"]})
                elif expected["state"] != actual["state"]:
                    row = {"position": absolute, "before": expected["state"], "after": actual["state"]}
                    states.append(row)
                    original_properties = expected["state"].get("Properties", {})
                    actual_properties = actual["state"].get("Properties", {})
                    changed = {key for key in original_properties if original_properties[key] != actual_properties.get(key)}
                    state_kinds[("completed_omitted_properties" if not changed else "explicit_property_change") + ":" + expected["state"]["Name"]] += 1
                if expected["nbt"] != actual["nbt"]:
                    payloads.append({"position": absolute, "before": expected["nbt"], "after": actual["nbt"]})
        assert capture_volume == math.prod(source["size"])
        for first, second in combinations(boxes, 2):
            assert not all(max(first[0][axis], second[0][axis]) < min(first[0][axis] + first[1][axis], second[0][axis] + second[1][axis]) for axis in range(3))
        assert not types, (identifier, types[:3])
        detail = {"id": identifier, "source_sha256": specification["sha256"], "after_sha256": rebuilt_spec["sha256"], "full_capture_cells": capture_volume, "captures": len(boxes), "type_changes": types, "raw_state_changes": states, "state_change_kinds": dict(state_kinds), "raw_be_changes": payloads, "reassembled_payload_exact": True}
        summaries.append(detail)
        route = original_routes[index]
        assert route["sha256"] == specification["sha256"]
        routes.append(dict(route, candidate=str(frozen(rebuilt_spec["candidate"])), sha256=rebuilt_spec["sha256"]))
        print(identifier, "full_cells", capture_volume, "states", len(states), "BE", len(payloads), flush=True)
    destination = root / "independent-posttick-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(routes, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    result = {"stage": "INDEPENDENT_DECODE_OF_FROZEN_PRODUCER_S1_1200_TICK_CAPTURES", "files_verified": len(records), "candidates": summaries, "all_eleven_no_type_changes": True, "limits": ["Producer world inspected, no parent world rerun", "1200 ordinary ticks with explicit fluid/neighbor scheduling; forced chunks alone do not prove random-tick coverage of each cell", "Raw state and BE differences retained for exact classification", "No full player travel or reach/ray interaction acceptance"], "accepted": False}
    (root / "independent-qa/world-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")


if __name__ == "__main__":
    main()
