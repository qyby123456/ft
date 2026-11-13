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
    for record in records:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    manifest = load(root / "ready-for-review.json")
    sources = {room["id"]: room for room in manifest["rooms"]}
    sources["S6-99"] = manifest["assembly"]
    captured = {record["source"]: Path(record["snapshot"]) for record in records}
    runtime = load(root / "qa/runtime/result.json")
    assert runtime["status"] == "PASS" and len(runtime["fixtures"]) == 11
    reports = []
    route_inputs = []
    air = {"state": {"Name": "minecraft:air"}, "nbt": None}
    for fixture in runtime["fixtures"]:
        specification = sources[fixture["id"]]
        assert fixture["source_sha256"] == specification["sha256"]
        original, before = decode(captured[specification["candidate"]])
        after_path = captured[fixture["capture"]]
        assert hashlib.sha256(after_path.read_bytes()).hexdigest() == fixture["capture_sha256"]
        after_data, after = decode(after_path)
        assert original["size"] == after_data["size"]
        route_source = str(Path(specification["candidate"]).parent / "qa/route-input.json")
        route = load(captured[route_source])
        assert len(route) == 1 and route[0]["sha256"] == specification["sha256"]
        route_inputs.append(dict(route[0], candidate=str(after_path), sha256=fixture["capture_sha256"], group=int(fixture["id"].split("-")[1]) + 600))
        type_changes, state_changes, entity_changes = [], [], []
        for point in before.keys() | after.keys():
            first, second = before.get(point, air), after.get(point, air)
            if first["state"]["Name"] != second["state"]["Name"]:
                type_changes.append({"position": point, "before": first["state"]["Name"], "after": second["state"]["Name"]})
            if first["state"] != second["state"]:
                state_changes.append({"position": point, "before": first["state"], "after": second["state"]})
            if first["nbt"] != second["nbt"]:
                entity_changes.append({"position": point, "before": first["nbt"], "after": second["nbt"]})
        for change in entity_changes:
            if change["before"] is None:
                assert change["after"] == {"id": "minecraft:lectern"}
                change["classification"] = "empty_lectern_generated_id"
            else:
                assert change["before"]["id"] in {"minecraft:furnace", "minecraft:smoker"}
                assert change["after"] == dict(change["before"], RecipesUsed={})
                change["classification"] = "empty_furnace_recipe_table_default"
        assert not fixture["dangerous"] and fixture["external_fluid_cells"] == 0
        reports.append({"id": fixture["id"], "source_sha256": specification["sha256"], "server_ticks": fixture["server_ticks"], "type_changes": type_changes, "raw_state_change_count": len(state_changes), "raw_block_entity_changes": entity_changes, "producer_dangerous": fixture["dangerous"], "producer_external_fluid_cells": fixture["external_fluid_cells"], "interactions": fixture["interactions"]})
    result = {"stage": "INDEPENDENT_INSPECTION_OF_PRODUCER_RUNTIME_CAPTURES", "fixtures": reports, "accepted": False, "scope": "Frozen producer ServerLevel captures decoded independently; no parent server rerun. 400 ticks plus 20 explicit tickChunk dispatches, not indefinite or random tick coverage proof. Door source checks distance/outline ray from assigned positions; containers use assigned positions and direct slot insertion, not player walking reach."}
    (root / "independent-qa/runtime-capture-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    destination = root / "independent-posttick-qa"
    destination.mkdir(exist_ok=True)
    (destination / "route-input.json").write_text(json.dumps(route_inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "vanilla-block-tags.json").write_bytes((root / "independent-qa/vanilla-block-tags.json").read_bytes())
    print(json.dumps([{"id": item["id"], "types": len(item["type_changes"]), "states": item["raw_state_change_count"], "block_entities": len(item["raw_block_entity_changes"]), "interactions": len(item["interactions"])} for item in reports], ensure_ascii=False))


if __name__ == "__main__":
    main()
