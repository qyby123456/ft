import argparse
import json
from pathlib import Path

from audit_s8_submission import load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    layout = load(root / "assembly/layout.json")
    source = load(root / "independent-qa/route-input.json")[-1]
    assert source["sha256"] == layout["source_sha256"]
    targets = list(source["targets"])
    original_count = len(targets)
    assert len(layout["corridors"]) == 11
    for corridor in layout["corridors"]:
        assert corridor["width"] == 3
        for segment, (start, end) in enumerate(zip(corridor["waypoints"], corridor["waypoints"][1:])):
            displacement = [end[axis] - start[axis] for axis in range(3)]
            assert displacement[1] == 0 and sum(value != 0 for value in displacement) == 1
            distance = abs(displacement[0] or displacement[2])
            delta_east = (displacement[0] > 0) - (displacement[0] < 0)
            delta_south = (displacement[2] > 0) - (displacement[2] < 0)
            for step in range(distance + 1):
                for across in range(-1, 2):
                    point = [start[0] + delta_east * step - delta_south * across, start[1], start[2] + delta_south * step + delta_east * across]
                    targets.append({"label": f"independent-corridor-{corridor['id']}-segment-{segment}-step-{step}-lane-{across}", "position": point})
    source["targets"] = targets
    destination = root / "independent-corridor-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps([source], ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    (destination / "coverage.json").write_text(json.dumps({"sha256": source["sha256"], "original_targets": original_count, "added_full_width_targets": len(targets) - original_count, "scope": "Three lanes at every integer centerline step, including endpoints and both perpendicular strips at corners. Player body clearance only; does not certify all five blocks of architectural clearHeight or world movement.", "accepted": False}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(len(targets))


if __name__ == "__main__":
    main()
