import argparse
import hashlib
import json
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    maker = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S5\qa\world-delivery")
    destination = root / "maker-world-evidence"
    destination.mkdir()
    sources = list(maker.glob("*.json")) + list(maker.glob("*.log")) + list(maker.glob("*.xml")) + list(maker.glob("*.ps1")) + list(maker.glob("native-test/src/**/*.java"))
    captured = []
    for source in sources:
        payload = source.read_bytes()
        target = destination / source.relative_to(maker)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        captured.append({"source": str(source), "snapshot": str(target), "sha256": hashlib.sha256(payload).hexdigest()})
    report = load(destination / "natural-results.json")
    manifest = load(root / "ready-for-review.json")
    assert report["passed"] and load(destination / "exit.json")["exit_code"] == 0
    rooms = []
    for room in manifest["rooms"]:
        observed = report[room["id"]]
        assert observed["sha256"] == room["sha256"]
        assert observed["completed_ticks"] == 1200 and observed["randomTickSpeed"] == 3
        assert not observed["aquatic_or_source_changes"] and not observed["invalid_aquatic_survival"] and not observed["original_air_flooded"]
        source = next((root / room["id"]).glob("*.nbt"))
        assert hashlib.sha256(source.read_bytes()).hexdigest() == room["sha256"]
        data, blocks = decode(source)
        full_volume = data["size"][0] * data["size"][1] * data["size"][2]
        rooms.append({"id": room["id"], "sha256": room["sha256"], "full_volume": full_volume, "explicit_records": len(blocks), "full_explicit_air_coverage": len(blocks) == full_volume, "aquatic_records": sum(any(token in value["state"]["Name"] for token in ("coral", "pickle", "kelp", "seagrass")) for value in blocks.values()), "maker_1200_ticks_no_listed_aquatic_failure": True})
    assert len(report["door_interaction"]) == 6
    assert all(door["initial_open"] and door["outside_toggle_closed"] and door["inside_toggle_reopened"] for door in report["door_interaction"])
    for record in captured:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    result = {"stage": "INDEPENDENT_INSPECTION_OF_MAKER_WORLD_REPORT_NOT_INDEPENDENT_WORLD_EXECUTION", "files": captured, "rooms": rooms, "six_door_dual_lever_cycles_in_maker_report": True, "accepted": False, "limitations": ["World test uses direct LeverBlock.pull, not player use or reach/line of sight", "No post-tick NBT captures are present in this delivery, so parent cannot independently compare all resulting state/BE payloads", "World checks aquatic/source blocks and explicit original air, not arbitrary machinery/container/furniture interactions", "Single orientation individual rooms in cleared external air; merged placement and flooded external ocean not tested", "1200 GameTest tick callback and enabled randomTickSpeed are finite observation, not proof for all times"]}
    (root / "independent-maker-world-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"captured_files": len(captured), "rooms": len(rooms), "all_rooms_have_full_explicit_volume": all(room["full_explicit_air_coverage"] for room in rooms), "six_door_report_cycles": True, "accepted": False}))


if __name__ == "__main__":
    main()
