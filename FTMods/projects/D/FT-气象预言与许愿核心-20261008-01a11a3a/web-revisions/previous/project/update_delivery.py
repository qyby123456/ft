import hashlib
import json
from pathlib import Path

from archive_delivery import DESTINATION, ROOT, copy_file


def main():
    if not DESTINATION.resolve().is_relative_to(Path("D:/ft工程文件").resolve()):
        raise ValueError("Unexpected archive root")
    manifest_path = DESTINATION / "CHECKSUMS.json"
    manifest = json.loads(manifest_path.read_text(encoding="utf-8"))
    if Path(manifest["source_workspace"]).resolve() != ROOT:
        raise ValueError("This archive is owned by another task")
    for entry in manifest["files"]:
        destination = DESTINATION / entry["path"]
        if not destination.resolve().is_relative_to(DESTINATION.resolve()):
            raise ValueError("Manifest path escapes archive")
        if hashlib.sha256(destination.read_bytes()).hexdigest() != entry["sha256"]:
            raise ValueError(f"Archive was independently edited: {destination}")
    rollback = DESTINATION / "web-revisions/previous"
    for source in sorted(ROOT.rglob("*")):
        if not source.is_file():
            continue
        relative = source.relative_to(ROOT)
        if relative.parts[0] in {"backups", "__pycache__"} or source.name == "delivery-report.json":
            continue
        destination = DESTINATION / "project" / relative
        if destination.exists() and destination.read_bytes() != source.read_bytes():
            previous = rollback / "project" / relative
            if not previous.exists():
                copy_file(destination, previous)
        copy_file(source, destination)
    previous_readme = rollback / "README.md"
    if not previous_readme.exists():
        copy_file(DESTINATION / "README.md", previous_readme)
    copy_file(ROOT / "DELIVERY.md", DESTINATION / "README.md")
    files = []
    for source in sorted(DESTINATION.rglob("*")):
        if not source.is_file() or source == manifest_path:
            continue
        files.append({"path": source.relative_to(DESTINATION).as_posix(), "bytes": source.stat().st_size,
                      "sha256": hashlib.sha256(source.read_bytes()).hexdigest()})
    manifest["file_count"] = len(files)
    manifest["files"] = files
    manifest["web_preview_revision"] = "unified-entry-animation-controls-avatar"
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for entry in files:
        destination = DESTINATION / entry["path"]
        assert hashlib.sha256(destination.read_bytes()).hexdigest() == entry["sha256"]
    report_path = ROOT / "delivery-report.json"
    report = json.loads(report_path.read_text(encoding="utf-8"))
    report["verified_files"] = len(files)
    report["bytes"] = sum(entry["bytes"] for entry in files)
    report["unified_web_handoff"] = str(DESTINATION / "project/web-handoff.json")
    report["animation_controls"] = True
    report["avatar_scale"] = 1.8
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
