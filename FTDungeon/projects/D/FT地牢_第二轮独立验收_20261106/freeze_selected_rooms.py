import argparse
import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from freeze_submission import ROOT, WORK


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("group", type=int, choices=range(1, 9))
    parser.add_argument("rooms", type=int, nargs="+", choices=range(11, 21))
    arguments = parser.parse_args()
    group = WORK / f"S{arguments.group}"
    stamp = datetime.now(ZoneInfo("Asia/Shanghai")).strftime("%Y%m%d-%H%M%S-%f")
    destination = ROOT / f"S{arguments.group}-selected-{stamp}"
    destination.mkdir()
    captured = []
    room_records = []
    routes = []
    for number in sorted(set(arguments.rooms)):
        matches = [parent for parent in group.iterdir() if parent.is_dir() and parent.name.startswith(str(number) + "-")]
        assert len(matches) == 1
        parent = matches[0]
        candidates = list(parent.glob("*.nbt"))
        assert len(candidates) == 1
        source = candidates[0]
        room_id = f"S{arguments.group}-{number}"
        expected = hashlib.sha256(source.read_bytes()).hexdigest()
        files = [source, *parent.glob("*.mjs"), *parent.glob("*.json"), *parent.glob("*.png"), *parent.glob("qa/**/*.json")]
        for path in dict.fromkeys(files):
            payload = path.read_bytes()
            frozen = destination / room_id / path.relative_to(parent)
            frozen.parent.mkdir(parents=True, exist_ok=True)
            frozen.write_bytes(payload)
            captured.append({"source": str(path), "snapshot": str(frozen), "sha256": hashlib.sha256(payload).hexdigest()})
        frozen_source = destination / room_id / source.name
        assert hashlib.sha256(frozen_source.read_bytes()).hexdigest() == expected
        route_path = destination / room_id / "qa/route-input.json"
        route = json.loads(route_path.read_text(encoding="utf-8-sig"))
        matches = [entry for entry in route if entry["sha256"] == expected and Path(entry["candidate"]).resolve() == source.resolve()]
        assert len(matches) == 1, "Current route not bound to this source"
        routes.append(dict(matches[0], candidate=str(frozen_source), group=arguments.group * 100 + number))
        render = json.loads((destination / room_id / "qa/render.json").read_text(encoding="utf-8-sig"))
        assert (render.get("source_sha256") or render.get("sha256")) == expected
        room_records.append({"id": room_id, "source": str(source), "snapshot": str(frozen_source), "sha256": expected})
    reference = group / "references.json"
    payload = reference.read_bytes()
    (destination / "references.json").write_bytes(payload)
    captured.append({"source": str(reference), "snapshot": str(destination / "references.json"), "sha256": hashlib.sha256(payload).hexdigest()})
    for record in captured:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"], "Source or evidence changed during capture"
    (destination / "snapshot.json").write_text(json.dumps({"stage": "SELECTED_ROOM_SCOPED_FREEZE_NOT_FORMAL_GROUP_ACCEPTANCE", "rooms": room_records, "files": captured, "accepted": False}, ensure_ascii=False, indent=2), encoding="utf-8")
    (destination / "route-input.json").write_text(json.dumps(routes, ensure_ascii=False, indent=2), encoding="utf-8")
    tags = ROOT / "support-fixtures-20261106-171423-281/vanilla-block-tags.json"
    (destination / "vanilla-block-tags.json").write_bytes(tags.read_bytes())
    print(json.dumps({"directory": str(destination), "files": len(captured), "rooms": len(room_records), "accepted": False}, ensure_ascii=False))


if __name__ == "__main__":
    main()
