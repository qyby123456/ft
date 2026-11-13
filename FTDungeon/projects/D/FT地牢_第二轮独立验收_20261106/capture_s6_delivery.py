import hashlib
import json
from pathlib import Path
import sys

from audit_s8_submission import load


def main():
    root = Path(sys.argv[1]).resolve()
    manifest = load(root / "ready-for-review.json")
    assembly = manifest.get("merged") or manifest.get("assembly")
    group_root = Path(assembly["candidate"]).parent.parent
    source = Path(manifest.get("manifest") or manifest.get("file_manifest") or (group_root / ("DELIVERY-SHA256.json" if "complete_manifest" in manifest.get("evidence", {}) else "delivery-manifest.json"))).resolve()
    source_root = source.parent
    payload = source.read_bytes()
    records = load(root / "submission-snapshot.json")["files"] + load(root / "supplemental-snapshot.json")["files"]
    existing = {record["source"]: record for record in records}
    index = load(source)
    artifacts = index if isinstance(index, list) else index.get("artifacts", index.get("files"))
    if manifest.get("style") == "S1":
        artifacts = artifacts + [dict(item, role="read-only original reference") for item in index["external_references"]] + [dict(item, role="read-only external tool") for item in index["external_tools"]]
    additions = []
    for artifact in artifacts:
        original = Path(artifact.get("absolute") or (source_root / artifact["path"])).resolve()
        internal = original.is_relative_to(source_root)
        if internal:
            assert original == (source_root / artifact["path"]).resolve(), artifact["path"]
        else:
            assert artifact.get("role") in {"read-only external tool", "read-only original reference"}
        if str(original) in existing:
            assert existing[str(original)]["sha256"] == artifact["sha256"]
            continue
        contents = original.read_bytes()
        assert hashlib.sha256(contents).hexdigest() == artifact["sha256"]
        assert len(contents) == artifact["bytes"]
        relative = original.relative_to(source_root) if internal else Path("external-dependencies") / artifact["sha256"] / original.name
        destination = root / relative
        if destination.exists():
            assert destination.read_bytes() == contents, str(destination)
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(contents)
        record = {"source": str(original), "snapshot": str(destination), "sha256": artifact["sha256"], "bytes": len(contents)}
        existing[str(original)] = record
        additions.append(record)
    for record in existing.values():
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"]
    assert source.read_bytes() == payload
    assert (source_root / "ready-for-review.json").read_bytes() == (root / "ready-for-review.json").read_bytes()
    (root / "delivery-manifest.json").write_bytes(payload)
    result = {"stage": "INDEPENDENT_DECLARED_DELIVERY_CAPTURE", "manifest_sha256": hashlib.sha256(payload).hexdigest(), "files": additions, "total_unique_files": len(existing), "accepted": False}
    (root / "delivery-snapshot.json").write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({key: value for key, value in result.items() if key != "files"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
