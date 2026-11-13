import hashlib
import json
from pathlib import Path
import sys

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    records = []
    for filename in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records += load(root / filename)["files"]
    captured = {record["source"]: Path(record["snapshot"]) for record in records}
    for record in records:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    manifest = load(root / "ready-for-review.json")
    sources = {room["id"]: room for room in manifest["rooms"]}
    sources["merged"] = manifest["merged"]
    directory = root / "qa/world-revision2"
    runtime = load(directory / "natural-results.json")
    assert set(sources).issubset(runtime) and len(sources) == 11 and load(directory / "exit.json")["exit_code"] == 0
    reports = []
    for identifier in sources:
        fixture = runtime[identifier]
        specification = sources[identifier]
        assert fixture["sha256"] == specification["sha256"]
        assert fixture["completed_ticks"] == fixture["requested_ticks"] == 1200
        assert fixture["passed"] and not fixture["aquatic_or_source_changes"] and not fixture["invalid_aquatic_survival"] and not fixture["original_air_flooded"]
        placed_data, placed = decode(directory / f"{identifier}-placed.nbt")
        after_path = directory / f"{identifier}-after1200.nbt"
        assert hashlib.sha256(after_path.read_bytes()).hexdigest() == fixture["post_tick_sha256"]
        after_data, after = decode(after_path)
        assert placed_data["size"] == after_data["size"] and placed.keys() == after.keys()
        changes = [{"position": point, "placed": value, "after": after[point]} for point, value in placed.items() if value != after[point]]
        if identifier == "S5-12":
            assert len(changes) == 12
            assert all(change["placed"]["state"]["Name"] in {"minecraft:lever", "minecraft:iron_door"} and change["placed"]["state"]["Name"] == change["after"]["state"]["Name"] and change["placed"]["nbt"] == change["after"]["nbt"] for change in changes)
        else:
            assert not changes
        reports.append({"id": identifier, "source_sha256": specification["sha256"], "records_compared": len(placed), "changes": changes, "exact_stability_from_placed_snapshot": not changes, "server_ticks": 1200})
    result = {"stage": "INDEPENDENT_INSPECTION_OF_PRODUCER_1200_TICK_SNAPSHOTS", "fixtures": reports, "accepted": False, "limitations": ["Compares full placed and after snapshots, not unnormalized original source to world", "Producer server run inspected independently, no parent world rerun", "Single orientation and finite ticks; no human movement, oxygen, reach or client certification"]}
    (root / "independent-qa/world-revision-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps([{"id": item["id"], "records": item["records_compared"], "changes": len(item["changes"])} for item in reports], ensure_ascii=False))


if __name__ == "__main__":
    main()
