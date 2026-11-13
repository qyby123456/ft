import hashlib
import json
from pathlib import Path
import sys

from freeze_submission import file_records


def main():
    root = Path(sys.argv[1]).resolve()
    primary = json.loads((root / "submission-snapshot.json").read_text(encoding="utf-8"))
    manifest = json.loads((root / "ready-for-review.json").read_text(encoding="utf-8-sig"))
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == primary["manifest_sha256"]
    binding = manifest["artifact_manifest"]
    source_root = Path(binding["path"]).parent.resolve()
    index_path = root / "artifact-manifest.json"
    assert hashlib.sha256(index_path.read_bytes()).hexdigest() == binding["sha256"]
    index = json.loads(index_path.read_text(encoding="utf-8-sig"))
    existing = {record["source"]: record for record in primary["files"]}
    additions = []
    for record in file_records(index):
        source = Path(record["path"]).resolve()
        if str(source) in existing:
            assert existing[str(source)]["sha256"] == record["sha256"]
            continue
        assert source.is_relative_to(source_root), str(source)
        payload = source.read_bytes()
        assert hashlib.sha256(payload).hexdigest() == record["sha256"], str(source)
        if "bytes" in record:
            assert len(payload) == record["bytes"]
        destination = root / source.relative_to(source_root)
        if destination.exists():
            assert destination.read_bytes() == payload
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(payload)
        frozen = {"source": str(source), "snapshot": str(destination), "sha256": record["sha256"], "bytes": len(payload)}
        additions.append(frozen)
        existing[str(source)] = frozen
    for record in existing.values():
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    assert (source_root / "ready-for-review.json").read_bytes() == (root / "ready-for-review.json").read_bytes()
    report = {"stage": "DECLARED_NESTED_ARTIFACT_MANIFEST_CAPTURE_NOT_ACCEPTANCE", "files": additions, "total_unique_files": len(existing), "accepted": False}
    (root / "supplemental-snapshot.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key != "files"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
