import hashlib
import json
import math
from itertools import combinations
from pathlib import Path
import sys

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    snapshot = load(root / "submission-snapshot.json")
    for record in snapshot["files"]:
        frozen = Path(record["snapshot"])
        assert frozen.is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
    manifest = load(root / "ready-for-review.json")
    plan = load(root / "assembly/clearance/clearance-plan.json")
    assembly = manifest["assembly"]
    assert plan["assembly_sha256"] == assembly["sha256"]
    assert plan["size"] == assembly["size"]
    commands = root / "assembly/clearance/clear-entire-bounds.mcfunction"
    assert hashlib.sha256(commands.read_bytes()).hexdigest() == plan["commands_sha256"]
    lines = commands.read_text(encoding="utf-8").splitlines()
    assert len(lines) == len(plan["tiles"])
    total = 0
    for tile, line in zip(plan["tiles"], lines):
        assert all(0 <= tile["from"][axis] <= tile["to"][axis] < plan["size"][axis] for axis in range(3))
        volume = math.prod(tile["to"][axis] - tile["from"][axis] + 1 for axis in range(3))
        assert volume == tile["volume"] and volume <= 32768
        assert line == "fill " + " ".join("~" + str(value) for value in tile["from"] + tile["to"]) + " minecraft:air replace"
        total += volume
    for first, second in combinations(plan["tiles"], 2):
        assert any(first["to"][axis] < second["from"][axis] or second["to"][axis] < first["from"][axis] for axis in range(3))
    assert total == math.prod(plan["size"]) == plan["volume"] == plan["covered_volume"]
    support = load(root / "independent-qa/independent-all-support.json")
    native = load(root / "independent-qa/independent-native-frozen.json")
    routes = load(root / "independent-qa/independent-routes.json")
    assert all(not item["failures"] and not item["unsupported"] for item in support)
    assert all(item["registry_properties"] == item["template_roundtrip_block_count"] == "PASS" for item in native)
    assert all(item["start_clear"] for item in routes)
    assert all(target["reachable"] and target["standing_clear"] for item in routes for target in item["targets"])
    report = {"stage": "INDEPENDENT_CLEARANCE_PLAN_AND_NATIVE_RESULT_INSPECTION", "assembly_sha256": assembly["sha256"], "command_count": len(lines), "full_bounds_volume": total, "nonoverlap_bounded_complete": True, "execution": "NOT_EXECUTED", "nonair_static_support_checks": sum(item["nonair_checked"] for item in support), "support_failures": 0, "route_targets": sum(len(item["targets"]) for item in routes), "unreachable_targets": 0, "accepted": False, "limitations": ["Clearance is an offline block-only relative-coordinate plan for zero rotation, not deployment approval or executed world clearance", "Native routes are sampled 0.6 by 1.8 body probes, not real player motion or proof of continuous global width", "Changed source hashes and merged structure still need world updates and interactions"]}
    (root / "independent-qa/clearance-native-summary.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
