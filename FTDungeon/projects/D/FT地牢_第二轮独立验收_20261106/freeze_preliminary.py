import argparse
import copy
import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parent
WORK = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106")
TAGS = Path(r"D:\ft工程文件\FT地牢_独立复核_20261104_01a1a685\vanilla-block-tags.json")


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("group", type=int, choices=range(1, 9))
    arguments = parser.parse_args()
    group = arguments.group
    records = next(entry for entry in load(ROOT / "inventory.json")["groups"] if entry["group"] == f"S{group}")
    assert records["unambiguous_candidates"] == 10, "Wait for ten unambiguous candidates"
    stamp = datetime.now(ZoneInfo("Asia/Shanghai")).strftime("%Y%m%d-%H%M%S-%f")
    target = ROOT / f"S{group}-preliminary-{stamp}"
    target.mkdir()
    routes = []
    frozen = []
    for room in records["candidates"]:
        source = Path(room["candidate_evidence"][0]["source"])
        expected = digest(source)
        payload = source.read_bytes()
        assert hashlib.sha256(payload).hexdigest() == expected
        route_path = source.parent / "qa/route-input.json"
        assert route_path.is_file(), room["id"]
        inputs = [entry for entry in load(route_path) if entry["sha256"] == expected]
        assert len(inputs) == 1, "Route plan not bound to current source"
        destination = target / (room["id"] + ".nbt")
        destination.write_bytes(payload)
        current = copy.deepcopy(inputs[0])
        current.update(candidate=str(destination), group=group * 100 + int(room["id"].split("-")[1]))
        routes.append(current)
        evidence = []
        for path in [route_path, source.parent / "summary.json", *source.parent.glob("*.png")]:
            if not path.is_file():
                continue
            evidence_path = target / room["id"] / path.name
            evidence_path.parent.mkdir(exist_ok=True)
            data = path.read_bytes()
            evidence_path.write_bytes(data)
            evidence.append({"source": str(path), "snapshot": str(evidence_path), "sha256": hashlib.sha256(data).hexdigest()})
        assert digest(source) == expected, "Source modified while freezing; reject snapshot"
        frozen.append({"id": room["id"], "original": str(source), "snapshot": str(destination), "sha256": expected, "evidence": evidence})
    (target / "route-input.json").write_text(json.dumps(routes, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (target / "vanilla-block-tags.json").write_bytes(TAGS.read_bytes())
    (target / "snapshot.json").write_text(json.dumps({"group": group, "stage": "PRELIMINARY_SINGLE_ROOM_REVIEW_NOT_GROUP_ACCEPTANCE", "producer_targets_reused_for_independent_rerun": True, "additional_target_coverage_visual_review_and_merge_required": True, "rooms": frozen}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (ROOT / "latest-preliminary.json").write_text(json.dumps({"directory": str(target)}, ensure_ascii=False), encoding="utf-8")
    print(str(target))


if __name__ == "__main__":
    main()
