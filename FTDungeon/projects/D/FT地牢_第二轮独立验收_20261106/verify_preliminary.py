import argparse
import hashlib
import json
from pathlib import Path


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    target = parser.parse_args().directory.resolve()
    snapshot = load(target / "snapshot.json")
    native = load(target / "independent-native-frozen.json")
    routes = load(target / "independent-routes.json")
    inputs = load(target / "route-input.json")
    assert len(snapshot["rooms"]) == len(native) == len(routes) == len(inputs) == 10
    results = []
    for room in snapshot["rooms"]:
        frozen = Path(room["snapshot"])
        assert frozen.is_relative_to(target)
        assert digest(frozen) == room["sha256"], room["id"]
        for evidence in room["evidence"]:
            evidence_path = Path(evidence["snapshot"])
            assert evidence_path.is_relative_to(target)
            assert digest(evidence_path) == evidence["sha256"], evidence_path
        group = snapshot["group"] * 100 + int(room["id"].split("-")[1])
        native_matches = [entry for entry in native if entry["group"] == group]
        route_matches = [entry for entry in routes if entry["group"] == group]
        plan_matches = [entry for entry in inputs if entry["group"] == group]
        assert len(native_matches) == len(route_matches) == len(plan_matches) == 1
        native_result = native_matches[0]
        route_result = route_matches[0]
        plan = plan_matches[0]
        assert native_result["sha256"] == route_result["sha256"] == plan["sha256"] == room["sha256"]
        expected_targets = [(entry["label"], entry["position"]) for entry in plan["targets"]]
        actual_targets = [(entry["label"], entry["position"]) for entry in route_result["targets"]]
        assert actual_targets == expected_targets and actual_targets, room["id"]
        native_pass = (
            native_result["registry_properties"] == "PASS"
            and native_result["template_roundtrip_block_count"] == "PASS"
            and not native_result["survival_failures"]
            and not native_result["survival_unsupported"]
        )
        route_pass = route_result["start_clear"] and all(
            entry["standing_clear"] and entry["reachable"] for entry in route_result["targets"]
        )
        results.append({
            "id": room["id"], "sha256": room["sha256"],
            "native_static_pass": native_pass, "selected_route_targets_pass": route_pass,
            "selected_support_points": native_result["survival_checked"],
            "selected_route_targets": len(actual_targets),
            "original_still_matches": digest(Path(room["original"])) == room["sha256"],
        })
    result = {
        "stage": snapshot["stage"], "rooms": results,
        "bound_snapshot_and_target_integrity": "PASS",
        "all_native_static_pass": all(entry["native_static_pass"] for entry in results),
        "all_selected_routes_pass": all(entry["selected_route_targets_pass"] for entry in results),
        "support_point_count": sum(entry["selected_support_points"] for entry in results),
        "route_target_count": sum(entry["selected_route_targets"] for entry in results),
        "visual_review": "SEE_VISUAL_REVIEW_MD_NOT_AUTO_PASSED",
        "natural_tick_and_gameplay": "NOT_RUN", "merged_world_acceptance": "NOT_RUN",
        "group_accepted": False,
    }
    (target / "verified-preliminary-result.json").write_text(
        json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(result, ensure_ascii=False))


if __name__ == "__main__":
    main()
