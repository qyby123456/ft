import argparse
import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo

from freeze_submission import ROOT, WORK, capture


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--existing", type=Path)
    parser.add_argument("--group", type=int, choices=(1, 2, 4, 5, 6, 7, 8), default=7)
    arguments = parser.parse_args()
    group = WORK / f"S{arguments.group}"
    stamp = datetime.now(ZoneInfo("Asia/Shanghai")).strftime("%Y%m%d-%H%M%S-%f")
    destination = arguments.existing.resolve() if arguments.existing else ROOT / (f"S{arguments.group}-submission-" + stamp)
    captured = json.loads((destination / "submission-snapshot.json").read_text(encoding="utf-8")) if arguments.existing else capture(group, destination)
    for record in captured["files"]:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    manifest_bytes = (destination / "ready-for-review.json").read_bytes()
    manifest = json.loads(manifest_bytes.decode("utf-8-sig"))
    existing = {record["source"]: record for record in captured["files"]}
    supplements = []
    sources = list(group.glob("*.py")) + list(group.glob("*.mjs")) + list(group.glob("*.cjs")) + [group / "references.json"]
    assert (manifest.get("group") or manifest.get("style") or captured["style"]) == group.name
    for specification in [*manifest["rooms"], manifest.get("assembly") or manifest["merged"]]:
        candidate = Path(specification["candidate"] if "candidate" in specification else specification["source"]["path"])
        if str(candidate) not in existing:
            assert hashlib.sha256(candidate.read_bytes()).hexdigest() == specification.get("sha256", specification.get("source", {}).get("sha256"))
            sources.append(candidate)
        parent = candidate.parent
        sources.extend(parent.glob("*.png"))
        sources.extend(parent.glob("*.json"))
        sources.extend(parent.glob("*.mjs"))
        sources.extend(parent.glob("*.cjs"))
        sources.extend(parent.glob("qa/**/*.json"))
        sources.extend(parent.glob("parts/*.nbt"))
        sources.extend(parent.glob("chunks/*.nbt"))
        sources.extend(parent.glob("chunks/*.json"))
        sources.extend(parent.glob("slices/*.nbt"))
        sources.extend(parent.glob("slices/*.json"))
    for source in dict.fromkeys(sources):
        source = source.resolve()
        assert source.is_relative_to(group)
        if str(source) in existing:
            continue
        payload = source.read_bytes()
        frozen = destination / source.relative_to(group)
        assert not frozen.exists()
        frozen.parent.mkdir(parents=True, exist_ok=True)
        frozen.write_bytes(payload)
        supplements.append({"source": str(source), "snapshot": str(frozen), "sha256": hashlib.sha256(payload).hexdigest(), "bytes": len(payload)})
    for record in supplements:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    assert (group / "ready-for-review.json").read_bytes() == manifest_bytes
    (destination / "supplemental-snapshot.json").write_text(json.dumps({"stage": "REVIEWER_CAPTURE_OF_ADDITIONAL_EVIDENCE_NOT_MAKER_DECLARED_HASHES", "files": supplements, "accepted": False}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "declared_files": captured["unique_file_count"], "reviewer_supplement_files": len(supplements), "accepted": False}, ensure_ascii=False))


if __name__ == "__main__":
    main()
