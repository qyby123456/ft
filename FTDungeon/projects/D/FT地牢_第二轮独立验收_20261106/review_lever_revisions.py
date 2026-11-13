import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from audit_s8_submission import decode, load


ROOT = Path(__file__).resolve().parent
WORK = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106")
CASES = [
    ("S5-20", WORK / "S5/S5-20/s5-20.nbt", ROOT / "S5-preliminary-20261107-025045-361444/S5-20.nbt", (26, 5, 6)),
    ("S7-20", WORK / "S7/S7-20/s7-20-secret-vault.nbt", ROOT / "S7-preliminary-20261107-004405-272572/S7-20.nbt", (29, 4, 12)),
]


def main():
    destination = ROOT / ("lever-revisions-" + datetime.now(ZoneInfo("Asia/Shanghai")).strftime("%Y%m%d-%H%M%S-%f"))
    destination.mkdir()
    captured = []
    inputs = []
    revisions = []
    for room_id, source, previous, failed_position in CASES:
        room = destination / room_id
        room.mkdir()
        files = [source, *source.parent.glob("*.json"), *source.parent.glob("*.mjs"), *source.parent.glob("*.png"), *source.parent.glob("qa/*.json")]
        for path in dict.fromkeys(files):
            data = path.read_bytes()
            frozen = room / path.relative_to(source.parent)
            frozen.parent.mkdir(parents=True, exist_ok=True)
            frozen.write_bytes(data)
            captured.append({"source": str(path), "snapshot": str(frozen), "sha256": hashlib.sha256(data).hexdigest()})
        frozen = room / source.name
        digest = hashlib.sha256(frozen.read_bytes()).hexdigest()
        route = load(room / "qa/route-input.json")
        matches = [entry for entry in route if entry["sha256"] == digest and Path(entry["candidate"]).resolve() == source.resolve()]
        assert len(matches) == 1, "Current route not bound to source"
        specification = dict(matches[0], candidate=str(frozen), group=int(room_id[1]) * 100 + 20)
        inputs.append(specification)
        before_data, before = decode(previous)
        after_data, after = decode(frozen)
        assert before_data["size"] == after_data["size"]
        changes = [{"position": list(point), "before": before.get(point), "after": after.get(point)} for point in sorted(set(before) | set(after)) if before.get(point) != after.get(point)]
        neighborhood = {str(point): after.get(point) for point in [failed_position, *[tuple(failed_position[axis] + (direction if axis == changed_axis else 0) for axis in range(3)) for changed_axis in range(3) for direction in (-1, 1)]]}
        revisions.append({"id": room_id, "previous_sha256": hashlib.sha256(previous.read_bytes()).hexdigest(), "candidate_sha256": digest, "previous_failed_lever": list(failed_position), "current_neighborhood": neighborhood, "changed_records": len(changes), "changes": changes, "current_levers": [{"position": list(point), "payload": value} for point, value in after.items() if value["state"]["Name"] == "minecraft:lever"]})
    for record in captured:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"], "Source changed during capture"
    (destination / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    tags = ROOT / "support-fixtures-20261106-171423-281/vanilla-block-tags.json"
    (destination / "vanilla-block-tags.json").write_bytes(tags.read_bytes())
    (destination / "snapshot.json").write_text(json.dumps({"stage": "SCOPED_LEVER_REVISION_REVIEW_NOT_GROUP_ACCEPTANCE", "files": captured, "revisions": revisions, "accepted": False}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "revisions": [{key: value for key, value in revision.items() if key in {"id", "candidate_sha256", "changed_records", "current_levers"}} for revision in revisions]}, ensure_ascii=False))


if __name__ == "__main__":
    main()
