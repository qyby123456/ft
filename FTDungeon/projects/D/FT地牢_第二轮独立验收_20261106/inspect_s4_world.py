import argparse
import hashlib
import json
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest = load(root / "ready-for-review.json")
    source_root = Path(manifest["output_directory"])
    assert (source_root / "ready-for-review.json").read_bytes() == (root / "ready-for-review.json").read_bytes()
    maker = source_root / "qa/worldtest"
    destination = root / "maker-world-evidence"
    destination.mkdir(exist_ok=True)
    sources = list(maker.glob("*.json")) + list(maker.glob("*.log")) + list(maker.glob("*.xml")) + list(maker.glob("*.ps1")) + list(maker.glob("S4-*/*.nbt")) + list(maker.glob("native-test/src/**/*.java"))
    records = []
    for source in sources:
        payload = source.read_bytes()
        frozen = destination / source.relative_to(maker)
        frozen.parent.mkdir(parents=True, exist_ok=True)
        frozen.write_bytes(payload)
        records.append({"source": str(source), "snapshot": str(frozen), "sha256": hashlib.sha256(payload).hexdigest()})
    report = load(destination / "world-report.json")
    assert load(destination / "exit.json")["exit_code"] == 0
    observed = {room["id"]: room for room in report["reports"]}
    originals = {record["source"]: Path(record["snapshot"]) for record in load(root / "submission-snapshot.json")["files"]}
    rooms = []
    for room in manifest["rooms"]:
        evidence = observed[room["id"]]
        assert evidence["sha256"] == room["sha256"] and evidence["ticks"] == 800
        assert not evidence["escaped_water"] and not evidence["lost_source_water"] and not evidence["dropped_items"] and evidence["fire_blocks"] == 0
        original, before = decode(originals[room["candidate"]])
        after_data, after = decode(destination / room["id"] / "after-800-ticks.nbt")
        assert after_data["size"] == [original["size"][0] + 4, original["size"][1] + 2, original["size"][2] + 4]
        after = {(point[0] - 2, point[1], point[2] - 2): value for point, value in after.items() if 2 <= point[0] < original["size"][0] + 2 and point[1] < original["size"][1] and 2 <= point[2] < original["size"][2] + 2}
        types = []
        states = []
        be_changes = []
        for point in before.keys() | after.keys():
            expected = before.get(point, {"state": {"Name": "minecraft:air"}, "nbt": None})
            actual = after.get(point, {"state": {"Name": "minecraft:air"}, "nbt": None})
            if expected["state"]["Name"] != actual["state"]["Name"]:
                types.append({"position": point, "before": expected, "after": actual})
            elif expected["state"] != actual["state"]:
                states.append({"position": point, "before": expected["state"], "after": actual["state"]})
            if expected["nbt"] != actual["nbt"]:
                be_changes.append({"position": point, "before": expected["nbt"], "after": actual["nbt"]})
        rooms.append({"id": room["id"], "sha256": room["sha256"], "type_changes": types, "raw_state_changes": states, "raw_be_changes": be_changes, "source_entities": original["entities"], "after_entities": after_data["entities"]})
    assert len(observed) == len(rooms) == 10
    for record in records:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    result = {"stage": "INDEPENDENT_INSPECTION_OF_MAKER_800_TICK_REPORT_AND_NBT", "files": records, "rooms": rooms, "accepted": False, "scope": "Parent independent inspection of maker execution, not parent world rerun; raw default/state/BE/entity changes require classification. Ten individual rooms only, no merged world or real player certification."}
    (root / "independent-maker-world-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"captured": len(records), "rooms": len(rooms), "type_changes": sum(len(room["type_changes"]) for room in rooms), "raw_state_changes": sum(len(room["raw_state_changes"]) for room in rooms), "raw_be_changes": sum(len(room["raw_be_changes"]) for room in rooms)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
