import argparse
import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parent
WORK = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106")


def file_records(value):
    if isinstance(value, dict):
        if "path" in value and "sha256" in value:
            yield value
        elif isinstance(value.get("candidate"), str) and "sha256" in value:
            yield {"path": value["candidate"], "sha256": value["sha256"]}
        for child in value.values():
            yield from file_records(child)
    elif isinstance(value, list):
        for child in value:
            yield from file_records(child)


def capture(group, destination, expected_group=None):
    group = group.resolve()
    group_name = expected_group or group.name
    manifest_path = group / "ready-for-review.json"
    manifest_bytes = manifest_path.read_bytes()
    manifest = json.loads(manifest_bytes.decode("utf-8-sig"))
    assert manifest.get("stage") != "PARTIAL_REVISION_REVIEW_ONLY", "Partial revision list is not a complete group submission"
    declared_group = manifest.get("style") or manifest.get("group")
    assert declared_group is None or declared_group == group_name
    assert len(manifest["rooms"]) == 10
    assert {room["id"].upper() for room in manifest["rooms"]} == {f"{group_name}-{number}" for number in range(11, 21)}
    records = list(file_records(manifest))
    assemblies = [manifest[key] for key in ("assembly", "merged") if manifest.get(key)]
    assert records and len(assemblies) == 1, "Exactly one assembly delivery required"
    unique = {}
    reference_paths = {Path(room["reference"]["path"]).resolve() for room in manifest["rooms"] if isinstance(room.get("reference"), dict) and "path" in room["reference"] and "sha256" in room["reference"]}
    prompt_paths = {Path(room["prompt"]["path"]).resolve() for room in manifest["rooms"] if isinstance(room.get("prompt"), dict) and room["prompt"].get("role") == "readonly_reference_prompt" and "sha256" in room["prompt"]}
    for record in records:
        source = Path(record["path"]).resolve()
        assert source.is_relative_to(group) or source in reference_paths or source in prompt_paths, f"Out of scope {source}"
        if not source.is_relative_to(group):
            assert source.suffix.lower() in ({".txt"} if source in prompt_paths else {".png", ".jpg", ".jpeg", ".webp"})
        if source in unique:
            assert unique[source]["sha256"] == record["sha256"], "Conflicting file hashes"
        unique[source] = record
    destination.mkdir()
    (destination / "ready-for-review.json").write_bytes(manifest_bytes)
    captured = []
    for source, record in unique.items():
        payload = source.read_bytes()
        digest = hashlib.sha256(payload).hexdigest()
        assert digest == record["sha256"], f"Declared hash mismatch {source}"
        if "bytes" in record:
            assert len(payload) == record["bytes"], f"Declared size mismatch {source}"
        relative = source.relative_to(group) if source.is_relative_to(group) else Path("external-references") / digest / source.name
        frozen = destination / relative
        frozen.parent.mkdir(parents=True, exist_ok=True)
        frozen.write_bytes(payload)
        captured.append({"source": str(source), "snapshot": str(frozen), "sha256": digest, "bytes": len(payload)})
    for record in captured:
        assert hashlib.sha256(Path(record["source"]).read_bytes()).hexdigest() == record["sha256"], "Source changed during capture"
    assert manifest_path.read_bytes() == manifest_bytes, "Manifest changed during capture"
    result = {"stage": "SUBMISSION_HASH_CAPTURE_ONLY_NOT_ACCEPTANCE", "style": group_name, "manifest_sha256": hashlib.sha256(manifest_bytes).hexdigest(), "files": captured, "unique_file_count": len(captured), "accepted": False}
    (destination / "submission-snapshot.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return result


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("group", type=int, choices=range(1, 9))
    parser.add_argument("--revision", type=Path)
    arguments = parser.parse_args()
    stamp = datetime.now(ZoneInfo("Asia/Shanghai")).strftime("%Y%m%d-%H%M%S-%f")
    destination = ROOT / f"S{arguments.group}-submission-{stamp}"
    group_root = (WORK / f"S{arguments.group}").resolve()
    source = (group_root / arguments.revision).resolve() if arguments.revision else group_root
    assert source.is_relative_to(group_root)
    result = capture(source, destination, expected_group=group_root.name)
    (ROOT / "latest-submission.json").write_text(json.dumps({"directory": str(destination)}, ensure_ascii=False), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "files": result["unique_file_count"], "accepted": False}, ensure_ascii=False))


if __name__ == "__main__":
    main()
