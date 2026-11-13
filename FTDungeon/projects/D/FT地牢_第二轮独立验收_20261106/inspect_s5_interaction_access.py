import hashlib
import json
import math
import sys
import zipfile
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root, prior = map(lambda value: Path(value).resolve(), sys.argv[1:3])
    records = []
    for name in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records.extend(load(root / name)["files"])
    records = {record["source"]: record for record in records}
    for record in records.values():
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    manifest, previous = load(root / "ready-for-review.json"), load(prior / "ready-for-review.json")
    for current, old in zip(manifest["rooms"], previous["rooms"]):
        assert current["id"] == old["id"] and current["sha256"] == old["sha256"]
    assembly = manifest["merged"]
    assert assembly["sha256"] == previous["merged"]["sha256"]
    candidate = next(Path(record["snapshot"]) for record in records.values() if record["source"] == assembly["candidate"])
    data, blocks = decode(candidate)
    reach = load(root / "qa/interaction-reach-final/reach-results.json")
    assert reach["sha256"] == assembly["sha256"] and reach["passed"] is False
    interactions = reach["interactions"]
    assert len(interactions) == 37 and len({tuple(item["target"]) for item in interactions}) == 37
    actual_targets = {point for point, block in blocks.items() if block["state"]["Name"] in {"minecraft:chest", "minecraft:barrel", "minecraft:lever"}}
    assert actual_targets == {tuple(item["target"]) for item in interactions}
    failures = [item for item in interactions if not item["passed"]]
    assert {tuple(item["target"]) for item in failures} == {(156, 14, 81), (159, 14, 81)}
    valid = [item for item in interactions if item["passed"]]
    for item in valid:
        assert item["standing_found"] and item["standing_clear"] and item["dry_supported"] and item["ray_hit_target"] and item["consumed"]
        assert item["reach_limit"] == 4.5
        distance = math.dist(item["eye"], item["hit"])
        assert abs(distance - item["eye_to_hit"]) < 1e-8 and distance <= 4.5
        if item["block"] == "minecraft:lever":
            assert item["toggled"] and item["restored"]
        else:
            assert item["menu_created"]
    routes = load(root / "qa/interaction-reach-final/route-input.json")
    assert len(routes) == 2
    expected_stands = {tuple([item["feet"][0] - 0.5, item["feet"][1], item["feet"][2] - 0.5]) for item in valid}
    assert len(expected_stands) == 35
    for item in routes:
        assert item["sha256"] == assembly["sha256"]
        assert {tuple(target["position"]) for target in item["targets"]} == expected_stands
        item["candidate"] = str(candidate)
        item["size"] = data["size"]
    archive = root / "access-package/s5access.zip"
    assert hashlib.sha256(archive.read_bytes()).hexdigest() == manifest["external_access_zip_sha256"]
    package = load(root / "access-package/manifest.json")
    pack = root / "access-package/s5access"
    resources = root / "qa/access-native-verified/native-test/src/main/resources"
    entries = package["files"]
    with zipfile.ZipFile(archive) as zipped:
        assert set(zipped.namelist()) == {entry["path"].replace("\\", "/") for entry in entries}
        for entry in entries:
            relative = entry["path"].replace("\\", "/")
            contents = zipped.read(relative)
            assert hashlib.sha256(contents).hexdigest() == entry["sha256"]
            assert contents == (pack / relative).read_bytes()
            if relative.startswith("data/"):
                assert contents == (resources / relative).read_bytes()
    access = load(root / "qa/access-native-verified/access-results.json")
    assert access["candidate_sha256"] == assembly["sha256"] and access["passed"] is True
    assert all(access["checks"].values()) and access["barrier_count"] == 57482
    destination = root / "independent-interaction-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(routes, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "qa/interaction-reach-final/vanilla-block-tags.json").read_bytes())
    report = {"stage": "INDEPENDENT_FROZEN_INTERACTION_AND_ACCESS_INSPECTION", "source_sha256": assembly["sha256"], "frozen_files_verified": len(records), "prior_same_hash_review": str(prior), "all_ten_rooms_and_merged_hashes_unchanged": True, "all_chest_barrel_lever_targets_accounted_for": 37, "usable_interactions": len(valid), "failed_interactions": failures, "max_eye_to_hit": max(item["eye_to_hit"] for item in valid), "datapack_zip_files_byte_verified": len(entries), "all_data_including_load_tick_tags_equals_test_resources": True, "producer_access_checks": access["checks"], "route_targets_prepared_for_independent_run": sum(len(item["targets"]) for item in routes), "limitations": ["Producer worlds independently inspected, no parent world rerun", "Actors positioned at recorded feet; separate route graph is not actual walking", "BlockState.use plus provider.createMenu does not certify network UI", "Two original high barrels explicitly fail and remain required; no retirement or relabeling", "Access automatic tick evidence does not prove full walk from entry to finish"], "accepted": False}
    (destination / "inspection.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
