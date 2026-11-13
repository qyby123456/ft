import argparse
import hashlib
import json
import os
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from install_assets import archive_map, shared_archive_lock
from install_core import gameplay_hashes
from install_resourcepack import PACK_ID, pack_options
from verify_assets import verify


ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent.parent.resolve()
TARGET = WORKSPACE / "mods/qys-event-forge-1.20.1-1.0.6.jar"
PACK = WORKSPACE / "resourcepacks/FT_Instruments_Remodel_20261008"
REPORT = ROOT / "final-release/deployment.json"


def sha256(data):
    return hashlib.sha256(data).hexdigest()


def payload():
    files = {file.relative_to(ROOT / "payload").as_posix(): file.read_bytes()
             for file in (ROOT / "payload").rglob("*") if file.is_file()}
    assert len(files) == 14
    return files


def document(report):
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    REPORT.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    if report.get("release_directory"):
        (Path(report["release_directory"]) / "deployment.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def file_targets(resources):
    destinations = {}
    for relative, content in resources.items():
        destination = PACK / relative
        assert destination.resolve().is_relative_to(PACK.resolve())
        destinations[destination] = content
        if relative.startswith("assets/farmingtales/"):
            destination = WORKSPACE / "kubejs" / relative
            assert destination.resolve().is_relative_to((WORKSPACE / "kubejs/assets/farmingtales").resolve())
            destinations[destination] = content
    return destinations


def verify_installed(report, resources):
    entries = archive_map(TARGET)
    jar_resources = {relative: content for relative, content in resources.items() if relative.startswith("assets/qys_event/")}
    assert all(entries.get(relative) == sha256(content) for relative, content in jar_resources.items())
    assert all(destination.read_bytes() == content for destination, content in file_targets(resources).items())
    baseline = archive_map(Path(report["backup"]))
    assert all(entries.get(name) == value for name, value in baseline.items() if name not in jar_resources)
    assert set(entries) == set(baseline) | set(jar_resources)
    assert sha256(TARGET.read_bytes()) == report["new_sha256"]
    assert sha256(Path(report["backup"]).read_bytes()) == report["old_sha256"]
    assert gameplay_hashes(WORKSPACE) == report["gameplay_hashes"]
    _, packs = pack_options((WORKSPACE / "options.txt").read_bytes())
    assert PACK_ID in packs
    return {"jar_payload_hashes": True, "unrelated_entries_unchanged": True, "no_duplicate_entries": True,
            "zip_crc": True, "backup_sha256": True, "installed_sha256": True,
            "core_and_resourcepack_match": True, "gameplay_scripts_and_blockstates_unchanged": True}


def deploy(resources):
    assert TARGET.resolve().is_relative_to(WORKSPACE / "mods")
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime("%Y%m%d-%H%M%S")
    release = ROOT / "final-release" / stamp
    release.mkdir(parents=True, exist_ok=False)
    if REPORT.exists():
        previous = json.loads(REPORT.read_text(encoding="utf-8"))
        if previous.get("backup"):
            retained = Path(previous["backup"]).parent / "retained-attempt.json"
            retained.write_text(json.dumps(previous, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    report = {"status": "preparing", "target": str(TARGET), "timezone": "Asia/Shanghai", "release_directory": str(release),
              "deployment_timestamp": stamp, "scope": "Forge 1.5.2 only", "baseline": "current target read under exclusive shared archive lock",
              "jar_retries": 0, "awaiting_gameplay_validation": True,
              "superseded_revision": "revisions/superseded-preoptimization/status.json",
              "pending_game_checks": ["Minecraft resource baking and reload", "world placement, adjacent block lighting and culling", "inventory and first-person item appearance", "well interaction, particle/sound appearance in actual client"],
              "neoForge_written": False, "qys_bosses_written": False}
    try:
        with shared_archive_lock(TARGET, wait_steps=0):
            baseline_bytes = TARGET.read_bytes()
            old_hash = sha256(baseline_bytes)
            backup = release / (TARGET.name + ".before-final-optimization.bak")
            with backup.open("xb") as output:
                output.write(baseline_bytes)
            assert sha256(backup.read_bytes()) == old_hash
            original_entries = archive_map(backup)
            report.update({"backup": str(backup), "old_sha256": old_hash,
                           "gameplay_hashes": gameplay_hashes(WORKSPACE),
                           "options_sha256_before": sha256((WORKSPACE / "options.txt").read_bytes())})
            destinations = file_targets(resources)
            original_files = {destination: destination.read_bytes() if destination.exists() else None for destination in destinations}
            file_backups = []
            for index, (destination, before) in enumerate(original_files.items()):
                if before is not None:
                    copy = release / "file-backups" / (f"{index:02d}_" + destination.name)
                    copy.parent.mkdir(parents=True, exist_ok=True)
                    copy.write_bytes(before)
                    file_backups.append({"original": str(destination), "backup": str(copy), "sha256": sha256(before)})
            report["file_backups"] = file_backups
            jar_resources = {relative: content for relative, content in resources.items() if relative.startswith("assets/qys_event/")}
            staged = TARGET.with_name(TARGET.name + f".ft-instruments-final-{stamp}.tmp")
            if staged.exists():
                raise FileExistsError(staged)
            report["staged_jar"] = str(staged)
            document(report)
            with zipfile.ZipFile(backup) as original, zipfile.ZipFile(staged, "w", compression=zipfile.ZIP_DEFLATED) as updated:
                updated.comment = original.comment
                for entry in original.infolist():
                    updated.writestr(entry, jar_resources.get(entry.filename, original.read(entry)))
                for relative, content in jar_resources.items():
                    if relative not in original_entries:
                        updated.writestr(relative, content)
            staged_entries = archive_map(staged)
            unchanged = {name for name in original_entries if name not in jar_resources}
            assert all(staged_entries.get(name) == original_entries[name] for name in unchanged)
            assert all(staged_entries.get(name) == sha256(content) for name, content in jar_resources.items())
            assert set(staged_entries) == set(original_entries) | set(jar_resources)
            if sha256(TARGET.read_bytes()) != old_hash:
                raise RuntimeError("Deployment baseline changed; retained backup/staged JAR, stopped without overwrite")
            for destination, before in original_files.items():
                current = destination.read_bytes() if destination.exists() else None
                if current != before:
                    raise RuntimeError(f"Asset baseline changed; stopped: {destination}")
            report["write_entries"] = [{"path": relative, "old_sha256": original_entries.get(relative), "new_sha256": sha256(content),
                                        "changed": original_entries.get(relative) != sha256(content)} for relative, content in sorted(jar_resources.items())]
            report["unrelated_entries_preserved"] = len(unchanged)
            report["class_files_preserved"] = sum(name.endswith(".class") for name in unchanged)
            report["new_sha256"] = sha256(staged.read_bytes())
            report["status"] = "staged_verified"
            document(report)
            os.replace(staged, TARGET)
            report["status"] = "jar_installed"
            document(report)
            file_changes = []
            for destination, content in destinations.items():
                before = original_files[destination]
                changed = before != content
                if changed:
                    temporary = destination.with_name(destination.name + ".ft-final.tmp")
                    if temporary.exists():
                        raise FileExistsError(temporary)
                    destination.parent.mkdir(parents=True, exist_ok=True)
                    temporary.write_bytes(content)
                    current = destination.read_bytes() if destination.exists() else None
                    if current != before:
                        raise RuntimeError(f"Asset changed during deployment; stopped: {destination}")
                    os.replace(temporary, destination)
                file_changes.append({"path": str(destination), "changed": changed, "old_sha256": sha256(before) if before is not None else None, "new_sha256": sha256(content)})
            report["file_resources"] = file_changes
            report["options_unchanged"] = report["options_sha256_before"] == sha256((WORKSPACE / "options.txt").read_bytes())
            assert report["options_unchanged"]
            report["verification"] = verify_installed(report, resources)
            report["status"] = "deployed_and_verified"
            document(report)
            return report
    except Exception as error:
        report["previous_status"] = report["status"]
        report["status"] = "stopped"
        report["error"] = str(error)
        document(report)
        raise


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify-only", action="store_true")
    arguments = parser.parse_args()
    for namespace, name in (("qys_event", "weather_forecaster"), ("qys_event", "oracle"), ("farmingtales", "wishing_well_core")):
        verify(name, namespace)
    resources = payload()
    tests = json.loads((ROOT / "final-release/full-validation.json").read_text(encoding="utf-8"))
    assert tests["all_passed"], "Full final validation must pass before deployment"
    assert tests.get("resource_sha256") == {name: sha256(content) for name, content in resources.items()}, "Final payload changed after tests; rerun validation"
    if arguments.verify_only:
        report = json.loads(REPORT.read_text(encoding="utf-8"))
        assert report["status"] == "deployed_and_verified"
        print(json.dumps(verify_installed(report, resources), indent=2))
    else:
        report = deploy(resources)
        print(json.dumps({key: value for key, value in report.items() if key != "gameplay_hashes"}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
