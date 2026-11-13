import argparse
import json
import math
from pathlib import Path

from audit_s8_submission import load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    layout = load(root / "merged/design.json")
    source = load(root / "independent-qa/route-input.json")[-1]
    original = source["targets"][:-1]
    assert source["targets"][-1]["label"] == "S5-20-east"
    targets = list(original)
    modules = {module["id"]: module for module in layout["modules"]}
    assert len(layout["edges"]) == 9
    for edge_number, edge in enumerate(layout["edges"]):
        assert any(port["feet"] == edge["start"] for port in modules[edge["from"]]["ports"])
        assert any(port["feet"] == edge["end"] for port in modules[edge["to"]]["ports"])
        assert edge["centerline"][0] == edge["start"] and edge["centerline"][-1] == edge["end"]
        for segment, (start, end) in enumerate(zip(edge["centerline"], edge["centerline"][1:])):
            assert start[1] == end[1], "Elevated corridor needs slope-specific independent coverage"
            displacement = [end[axis] - start[axis] for axis in range(3)]
            distance = max(abs(value) for value in displacement)
            assert distance > 0
            strips = []
            if displacement[0]:
                strips.append((0, 1))
            if displacement[2]:
                strips.append((1, 0))
            for step in range(distance + 1):
                center = [math.floor(start[axis] + displacement[axis] * step / distance + 0.5) for axis in range(3)]
                for strip_east, strip_south in strips:
                    for across in range(-2, 3):
                        point = [center[0] + strip_east * across, center[1], center[2] + strip_south * across]
                        targets.append({"label": f"independent-edge-{edge_number + 1}-segment-{segment}-step-{step}-axis-{strip_east}-{strip_south}-strip-{across}", "position": point})
    destination = root / "independent-corridor-qa"
    destination.mkdir()
    source["targets"] = targets
    (destination / "route-input.json").write_text(json.dumps([source], ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    (destination / "coverage.json").write_text(json.dumps({"stage": "INDEPENDENT_GEOMETRIC_CORRIDOR_SAMPLE_GENERATION", "sha256": source["sha256"], "edges": 9, "declared_original_targets_retained": len(original), "new_five_wide_strip_targets": len(targets) - len(original), "scope": "Every integer longitudinal sample across five lanes; diagonal segments use both orthogonal cross strips at nearest lattice centers. Includes endpoints and U-turn corners. Explicitly capped unused S5-20-east port is excluded and remains a separately reported failed standing target. Static native body test still required; no world movement certification.", "accepted": False}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "targets": len(targets), "added": len(targets) - len(original)}))


if __name__ == "__main__":
    main()
