import hashlib
import json
import zipfile
from pathlib import Path

from archive_delivery import DESTINATION, ROOT


def checksum(content):
    return hashlib.sha256(content).hexdigest()


def main():
    manifest_file = DESTINATION / "CHECKSUMS.json"
    manifest = json.loads(manifest_file.read_text(encoding="utf-8"))
    entries = manifest["files"]
    assert len(entries) == manifest["file_count"]
    names = {entry["path"] for entry in entries}
    assert len(names) == len(entries)
    actual = {file.relative_to(DESTINATION).as_posix() for file in DESTINATION.rglob("*") if file.is_file() and file != manifest_file}
    assert actual == names
    for entry in entries:
        file = DESTINATION / entry["path"]
        assert file.resolve().is_relative_to(DESTINATION.resolve())
        content = file.read_bytes()
        assert len(content) == entry["bytes"]
        assert checksum(content) == entry["sha256"]
    for file in ROOT.rglob("*"):
        if not file.is_file():
            continue
        relative = file.relative_to(ROOT)
        if relative.parts[0] in {"backups", "__pycache__"} or file.name == "delivery-report.json":
            continue
        assert (DESTINATION / "project" / relative).read_bytes() == file.read_bytes(), str(relative)
    payload = {file.relative_to(ROOT / "payload").as_posix(): file.read_bytes() for file in (ROOT / "payload").rglob("*") if file.is_file()}
    archives = {"Forge-resourcepack.zip": ({"qys_event", "farmingtales", "ftboss"}, 15),
                "NeoForge-resourcepack.zip": ({"qys_event", "farmingtales"}, 34),
                "qys-event-assets.zip": ({"qys_event"}, None),
                "kubejs-core-assets.zip": ({"farmingtales"}, None)}
    runtime_hashes = {}
    for name, (namespaces, pack_format) in archives.items():
        file = DESTINATION / "runtime-delta" / name
        expected = {relative: content for relative, content in payload.items() if relative.split("/")[1] in namespaces}
        with zipfile.ZipFile(file) as archive:
            assert archive.testzip() is None
            assert len(archive.namelist()) == len(set(archive.namelist()))
            expected_names = set(expected) | ({"pack.mcmeta"} if pack_format is not None else set())
            assert set(archive.namelist()) == expected_names
            assert all(archive.read(relative) == content for relative, content in expected.items())
            if pack_format is not None:
                assert json.loads(archive.read("pack.mcmeta"))["pack"]["pack_format"] == pack_format
        runtime_hashes[name] = checksum(file.read_bytes())
    deployment = json.loads((DESTINATION / "project/final-release/deployment.json").read_text(encoding="utf-8"))
    assert deployment["status"] == "deployed_and_verified"
    archived_backup = DESTINATION / "project" / Path(deployment["backup"]).relative_to(ROOT)
    assert checksum(archived_backup.read_bytes()) == deployment["old_sha256"]
    print(json.dumps({"all_passed": True, "verified_files": len(entries), "current_source_matches_archive": True,
                      "runtime_delta_matches_final_payload": True, "deployment_backup_verified": True,
                      "manifest_sha256": checksum(manifest_file.read_bytes()), "runtime_delta_sha256": runtime_hashes}, indent=2))


if __name__ == "__main__":
    main()
