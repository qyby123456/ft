import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from audit_s8_submission import decode, load


ROOT = Path(__file__).resolve().parent
WORK = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S7")


def main():
    source_root = WORK / "worldqa"
    report = load(source_root / "world-report.json")
    inputs = load(source_root / "inputs.json")
    assert len(inputs) == len(report["rooms"]) == 10
    destination = ROOT / ("S7-world-evidence-" + datetime.now(ZoneInfo("Asia/Shanghai")).strftime("%Y%m%d-%H%M%S-%f"))
    destination.mkdir()
    files = []

    def capture(source, relative):
        data = source.read_bytes()
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        files.append({"source": str(source), "snapshot": str(target), "sha256": hashlib.sha256(data).hexdigest()})
        return target

    for name in ("world-report.json", "inputs.json", "exit.json", "tests.xml", "console.log", "run.ps1"):
        capture(source_root / name, Path(name))
    java = source_root / "native-test/src/main/java/net/farmingtales/s7qa/FungalTests.java"
    capture(java, Path("FungalTests.java"))
    results = []
    for specification in inputs:
        room_id = specification["id"]
        matching = [entry for entry in report["rooms"] if entry["id"] == room_id]
        assert len(matching) == 1 and matching[0]["sha256"] == specification["sha256"]
        source = Path(specification["candidate"]).resolve()
        assert source.is_relative_to(WORK)
        frozen = capture(source, Path(room_id) / "source.nbt")
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == specification["sha256"], "Current source differs from maker world input"
        after = capture(source_root / (room_id + "-after-1200.nbt"), Path(room_id) / "after.nbt")
        original, before_blocks = decode(frozen)
        world, after_blocks = decode(after)
        assert original["size"] == world["size"]
        changes = []
        same_name_changes = 0
        for point, value in before_blocks.items():
            observed = after_blocks.get(point)
            before_name = value["state"]["Name"]
            after_name = observed["state"]["Name"] if observed else "minecraft:air"
            if before_name != after_name:
                changes.append({"position": list(point), "before": value, "after": observed})
            elif observed["state"] != value["state"]:
                same_name_changes += 1
        additions = [{"position": list(point), "state": value["state"]} for point, value in after_blocks.items() if point not in before_blocks and value["state"]["Name"] not in {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}]
        results.append({"id": room_id, "source_sha256": specification["sha256"], "after_sha256": hashlib.sha256(after.read_bytes()).hexdigest(), "block_type_changes": changes, "same_type_property_changes": same_name_changes, "new_nonair_in_omitted_source_cells": additions, "source_structure_entities": len(original["entities"]), "captured_world_entities": len(world["entities"]), "maker_equipped_stands": matching[0]["equipped_armor_stands"], "maker_ticks": matching[0]["ticks"], "maker_door_roundtrips": matching[0]["door_toggle_roundtrips"]})
    for record in files:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"], "Evidence changed during capture"
    result = {"stage": "INDEPENDENT_INSPECTION_OF_MAKER_WORLD_EVIDENCE_NOT_INDEPENDENT_WORLD_RUN", "files": files, "rooms": results, "test_scope_issues": ["Maker totalIssues excludes state_changes, so zero failures alone does not prove no source blocks were lost", "Fluid/fire detection iterates only source-record coordinates; source-omitted air and outside volume require separate coverage", "Door setOpen API is not player/lever activation or proof of container interaction", "Single orientation 1200-tick observation is finite evidence, not all seeds/times or full gameplay"], "accepted": False}
    (destination / "independent-inspection.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "rooms": [{"id": room["id"], "block_type_changes": len(room["block_type_changes"]), "property_changes": room["same_type_property_changes"], "new_omitted_solids": len(room["new_nonair_in_omitted_source_cells"]), "world_entities": room["captured_world_entities"]} for room in results]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
