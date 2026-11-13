import json
import sys
from collections import Counter
from pathlib import Path

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    world = load(root / "independent-qa/world-inspection.json")
    rows = []
    for candidate in world["candidates"]:
        kinds = Counter()
        for change in candidate["raw_state_changes"]:
            before, after = change["before"], change["after"]
            initial, current = before.get("Properties", {}), after.get("Properties", {})
            changed = {key for key in initial if initial[key] != current.get(key)}
            if not changed:
                kind = "completed_omitted_properties"
            elif before["Name"] == "minecraft:oak_leaves":
                assert changed == {"distance"} and current["persistent"] == "true"
                kind = "persistent_leaf_distance_recalculation"
            elif before["Name"] == "minecraft:iron_door":
                assert changed <= {"open", "powered", "hinge"}
                kind = "door_state_after_button_neighbor_updates"
            elif before["Name"] == "minecraft:dark_oak_fence":
                assert changed <= {"east", "north", "west", "south"}
                kind = "fence_connection_recalculation"
            else:
                raise AssertionError(change)
            kinds[kind] += 1
        payloads = Counter()
        for change in candidate["raw_be_changes"]:
            before, after = change["before"], change["after"]
            if before is None:
                assert after in ({"id": "minecraft:lectern"}, {"id": "minecraft:skull"})
                payloads[after["id"]] += 1
            else:
                assert before == {"id": "minecraft:furnace", "Items": []}
                assert after == {"CookTime": 0, "BurnTime": 0, "Items": [], "id": "minecraft:furnace", "CookTimeTotal": 0, "RecipesUsed": {}}
                payloads["minecraft:furnace"] += 1
        rows.append({"id": candidate["id"], "source_sha256": candidate["source_sha256"], "after_sha256": candidate["after_sha256"], "full_capture_cells": candidate["full_capture_cells"], "type_changes": 0, "raw_state_change_count": len(candidate["raw_state_changes"]), "state_classification": dict(kinds), "raw_be_change_count": len(candidate["raw_be_changes"]), "default_be_classification": dict(payloads)})
    routes = load(root / "independent-posttick-qa/independent-routes.json")
    assert len(routes) == 11
    assert all(target["standing_clear"] and target["reachable"] for item in routes for target in item["targets"])
    assert sum(len(item["targets"]) for item in routes) == 132
    report = {"stage": "INDEPENDENT_S1_FULL_POST_WORLD_DIFFERENCE_CLASSIFICATION", "candidates": rows, "post_world_route_targets_passed": 132, "limits": world["limits"][:2] + world["limits"][3:], "accepted": False}
    (root / "independent-qa/world-classification.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
