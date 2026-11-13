import json
import shutil
import zipfile
from pathlib import Path

import release


ROOT = Path(__file__).resolve().parent


def transfer(source, relative):
    destination = (release.ARCHIVE / relative).resolve()
    if not destination.is_relative_to(release.ARCHIVE.resolve()):
        raise RuntimeError("Archive path escaped independent final directory")
    destination.parent.mkdir(parents=True, exist_ok=True)
    if destination.exists() and destination.read_bytes() != source.read_bytes():
        raise RuntimeError("Do not overwrite a different final artifact: " + str(destination))
    if not destination.exists():
        shutil.copy2(source, destination)


def main():
    deployment = json.loads((ROOT / "deployment.json").read_text(encoding="utf-8"))
    checks = json.loads((ROOT / "verification.json").read_text(encoding="utf-8"))
    if not checks["passed"]:
        raise RuntimeError("Final verification has not passed")
    installed = deployment["status"] == "installed-offline-verified-game-pending"
    if installed:
        release.check_baseline(deployment["related_after_sha256"])
        with zipfile.ZipFile(release.target(release.JAR)) as archive:
            for name, content in release.payload_bytes().items():
                if archive.read(name) != content:
                    raise RuntimeError("Final runtime entry changed: " + name)
    neo = release.neo_audit()
    release.ARCHIVE.mkdir(parents=True, exist_ok=True)
    if release.ARCHIVE.resolve().parent != Path("D:/ft工程文件").resolve():
        raise RuntimeError("Final archive must be under explicitly named D drive target")
    source_files = [path for path in ROOT.rglob("*.java") if "preview" not in path.parts]
    source_files += [ROOT / name for name in ("release.py", "build.ps1", "audit.py", "post_audit.py", "test_release.py", "tracking.py", "delivery.py", "README.md")]
    for source in source_files:
        transfer(source, Path("project") / source.relative_to(ROOT))
    for source in (ROOT / "payload").rglob("*"):
        if source.is_file():
            transfer(source, Path("project") / source.relative_to(ROOT))
    for source in (ROOT / "preview").rglob("*"):
        if source.is_file() and "__pycache__" not in source.parts:
            transfer(source, Path("preview") / source.relative_to(ROOT / "preview"))
    reports = ("baseline.json", "candidate-delta.json", "core-runtime-verification.json", "forge-runtime-verification.json",
               "release-safety.json", "model-optimization-tests.txt", "VerifyCoreFacing.txt", "VerifyForgeFacing.txt",
               "QysEventVerifier.txt", "verification.json", "deployment.json", "post-deployment-verification.json")
    for name in reports:
        path = ROOT / name
        if path.exists():
            transfer(path, Path("validation") / name)
    transfer(ROOT / "baseline" / release.JAR, "validation/test-baseline-only.jar")
    if deployment.get("backup"):
        transfer(Path(deployment["backup"]), "validation/deployment-backup.jar")
    runtime = release.ARCHIVE / "runtime/qys-event-core-facing.entries.zip"
    runtime.parent.mkdir(parents=True, exist_ok=True)
    if runtime.exists():
        raise RuntimeError("Entry-only runtime bundle already exists; inspect before regenerating")
    with zipfile.ZipFile(runtime, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for name, content in release.payload_bytes().items():
            entry = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            archive.writestr(entry, content)
    manifest = {"scope": "Only current Forge qys-event JAR", "entries": list(release.ENTRIES),
                "entry_sha256": checks["payload_sha256"], "runtime_delta_sha256": release.sha(runtime.read_bytes()),
                "baseline_sha256": deployment["old_sha256"], "actual_after_sha256": deployment["new_sha256"],
                "installed": installed, "standalone_mod": False,
                "instructions": "This partial entry ZIP is not a mod and must not be placed in mods or expanded over an instance. Future installation requires fresh baseline audit and a whitelist-only atomic merge; never deploy an archived full JAR."}
    release.save(release.ARCHIVE / "runtime/manifest.json", manifest)
    handoff = {"threadId": "01a11be8-fba2-77a3-a117-5471717a4c17", "title": "event四向放置",
               "time": release.timestamp(), "archive_root": str(release.ARCHIVE), "scope": "Forge 1.5.2 Event blocks only",
               "status": deployment["status"], "installed": installed, "game_tested": False,
               "early_four_way_placement_still_installed": True,
               "core_structure_rotation_mirror_increment_applied": installed,
               "no_write_status": (None if installed else "Final JAR preflight locked; stopped before backup or write. The three-entry V2 delta is validated but NOT installed."),
               "source": "project/src", "runtime": "runtime/qys-event-core-facing.entries.zip",
               "runtime_manifest": "runtime/manifest.json", "preview": "preview/viewer.html",
               "unified_preview": "http://127.0.0.1:18790/event_four_way/viewer.html",
               "runtime_entries": list(release.ENTRIES), "baseline_sha256": deployment["old_sha256"],
               "actual_after_sha256": deployment["new_sha256"], "backup": deployment.get("backup"),
               "preserved": ["109 non-target JAR entries", "48 existing class files", "current optimized 1080-face models",
                             "all KubeJS startup/server/client scripts", "resourcepack and textures", "original Event/weather/wish logic"],
               "neo_rollback": neo, "qys_bosses_written": False, "manor_scaling_speed": "N/A, no manor hooks",
               "skill_effect_coverage": checks["skill_effect_coverage"],
               "pending_in_game": checks["pending_in_game"],
               "early_versions": {"status": "superseded, retained for coordinator cleanup", "paths": [
                   str(release.WORKSPACE / "_work/event-four-way-20261008"),
                   str(release.WORKSPACE / "_work/event-four-way-final-20261008"),
                   "D:/ft工程文件/FT-Event-四向放置-Forge-final-20261008"]},
               "test_fixtures": "preview/revisions/superseded-preoptimization is comparison-only; no runtime/render resource points to it"}
    release.save(release.ARCHIVE / "handoff.json", handoff)
    files = sorted(path for path in release.ARCHIVE.rglob("*") if path.is_file()
                   and path.name not in {"CHECKSUMS.json", "archive-sha256.json"} and path.name != "event-four-way-forge-V2.zip")
    checksums = {path.relative_to(release.ARCHIVE).as_posix(): {"sha256": release.sha(path.read_bytes()), "bytes": path.stat().st_size}
                 for path in files}
    release.save(release.ARCHIVE / "CHECKSUMS.json", checksums)
    for relative, record in checksums.items():
        path = release.ARCHIVE / relative
        if release.sha(path.read_bytes()) != record["sha256"] or path.stat().st_size != record["bytes"]:
            raise RuntimeError("Final checksum mismatch: " + relative)
    output = release.ARCHIVE / "event-four-way-forge-V2.zip"
    with zipfile.ZipFile(output, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        for source in files + [release.ARCHIVE / "CHECKSUMS.json"]:
            archive.write(source, source.relative_to(release.ARCHIVE).as_posix())
    with zipfile.ZipFile(output) as archive:
        if archive.testzip() or set(archive.namelist()) != set(checksums) | {"CHECKSUMS.json"}:
            raise RuntimeError("Final archive integrity mismatch")
        for relative, record in checksums.items():
            content = archive.read(relative)
            if release.sha(content) != record["sha256"] or len(content) != record["bytes"]:
                raise RuntimeError("Packed checksum mismatch: " + relative)
    record = {"archive": str(output), "sha256": release.sha(output.read_bytes()), "files": len(files) + 1,
              "manifest_sha256": release.sha((release.ARCHIVE / "CHECKSUMS.json").read_bytes()),
              "verified": True, "runtime_delta_sha256": manifest["runtime_delta_sha256"]}
    release.save(ROOT / "archive-sha256.json", record)
    release.save(release.ARCHIVE / "archive-sha256.json", record)
    print(json.dumps(record, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
