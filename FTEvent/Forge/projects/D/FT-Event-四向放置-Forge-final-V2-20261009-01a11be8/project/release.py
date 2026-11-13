import argparse
import ctypes
import hashlib
import json
import os
import shutil
import zipfile
from contextlib import contextmanager
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parent
WORKSPACE = Path(os.environ.get("FT_EVENT_FORGE_WORKSPACE", ROOT.parent.parent)).resolve()
NEO = WORKSPACE.parent / "FarmingTales_NeoForge 机械动力"
TRACKING = WORKSPACE / "_work/model-release-152-20261008/tracking.json"
ARCHIVE = Path("D:/ft工程文件/FT-Event-四向放置-Forge-final-V2-20261009-01a11be8")
JAR = "mods/qys-event-forge-1.20.1-1.0.6.jar"
SCRIPT = "kubejs/startup_scripts/content/wishing_well.js"
PACK = "resourcepacks/FT_Instruments_Remodel_20261008/assets"
MIXINS = "qys_event.mixins.json"
POLICY = "cn/farmingtales/qysevent/CoreFacingPolicy.class"
MIXIN = "cn/farmingtales/qysevent/mixin/CoreFacingMixin.class"
ENTRIES = (MIXINS, POLICY, MIXIN)


def sha(content):
    return hashlib.sha256(content).hexdigest()


def save(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def timestamp():
    return datetime.now(ZoneInfo("Asia/Shanghai")).isoformat(timespec="seconds")


def target(relative):
    path = (WORKSPACE / relative).resolve()
    if not path.is_relative_to(WORKSPACE) or not path.is_file():
        raise RuntimeError("Target outside Forge or missing: " + str(path))
    return path


def forge_paths():
    paths = {JAR, SCRIPT, "kubejs/server_scripts/block_events/wishing_well.js",
             "kubejs/client_scripts/tooltips/wishing_well.js"}
    for namespace in ("qys_event", "farmingtales", "ftboss"):
        for path in (WORKSPACE / PACK / namespace).rglob("*"):
            if path.is_file():
                paths.add(path.relative_to(WORKSPACE).as_posix())
    for path in (WORKSPACE / "kubejs/assets/farmingtales").rglob("*"):
        if path.is_file() and "wishing_well_core" in path.name:
            paths.add(path.relative_to(WORKSPACE).as_posix())
    return sorted(paths)


def hashes(paths):
    return {relative: sha(target(relative).read_bytes()) for relative in paths}


def check_baseline(expected):
    changed = [relative for relative, digest in expected.items() if sha(target(relative).read_bytes()) != digest]
    if changed:
        raise RuntimeError("BASELINE_CHANGED: " + ", ".join(changed))


def neo_audit():
    tracking = json.loads(TRACKING.read_text(encoding="utf-8"))
    entry = next(item for item in tracking["threads"] if item["key"] == "event_four_way")
    pairs = (("mods/qys-event-neoforge-1.21.1-1.0.8.jar", "neoForgeRestoredJarSha256"),
             (SCRIPT, "neoForgeRestoredScriptSha256"),
             ("kubejs/assets/farmingtales/blockstates/wishing_well_core.json", "neoForgeRestoredBlockstateSha256"))
    results = []
    for relative, key in pairs:
        path = (NEO / relative).resolve()
        if not path.is_relative_to(NEO.resolve()):
            raise RuntimeError("NeoForge read path escaped instance")
        actual = sha(path.read_bytes())
        if actual.upper() != entry[key].upper():
            raise RuntimeError("NeoForge rollback no longer matches: " + relative)
        results.append({"path": relative, "sha256": actual, "matches_coordinator_rollback": True})
    return {"read_only": True, "written": False, "rollback_verified": True, "files": results}


def jar_map(path):
    with zipfile.ZipFile(path) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)) or archive.testzip():
            raise RuntimeError("Invalid ZIP entries")
        return {name: sha(archive.read(name)) for name in names}


def patch(current, staged, payload):
    before = jar_map(current)
    with zipfile.ZipFile(current) as archive, zipfile.ZipFile(staged, "w") as output:
        output.comment = archive.comment
        for entry in archive.infolist():
            output.writestr(entry, payload.get(entry.filename, archive.read(entry)))
        for name in sorted(set(payload) - set(before)):
            entry = zipfile.ZipInfo(name, date_time=(1980, 1, 1, 0, 0, 0))
            entry.compress_type = zipfile.ZIP_DEFLATED
            output.writestr(entry, payload[name])
    after = jar_map(staged)
    if set(after) != set(before) | set(payload):
        raise RuntimeError("Unexpected entries added or removed")
    if any(before[name] != after[name] for name in set(before) - set(payload)):
        raise RuntimeError("Unrelated JAR entries changed")
    if any(after[name] != sha(content) for name, content in payload.items()):
        raise RuntimeError("Payload mismatch")
    return {"modified": sorted(name for name in before if before[name] != after[name]),
            "added": sorted(set(after) - set(before)), "unchanged_entries": len(set(before) - set(payload)),
            "unchanged_classes": sum(name.endswith(".class") for name in set(before) - set(payload))}


def payload_bytes():
    payload = ROOT / "payload"
    actual = {path.relative_to(payload).as_posix() for path in payload.rglob("*") if path.is_file()}
    if actual != set(ENTRIES):
        raise RuntimeError("Runtime delta exceeds exact qys-event entry whitelist")
    return {name: (payload / name).read_bytes() for name in ENTRIES}


def prepare():
    if (ROOT / "baseline.json").exists():
        raise RuntimeError("Baseline already exists; do not rebase tested inputs")
    baseline = hashes(forge_paths())
    neo = neo_audit()
    save(ROOT / "baseline.json", {"time": timestamp(), "forge": baseline, "neo_read_only": neo})
    for relative in forge_paths():
        path = ROOT / "baseline" / relative
        path.parent.mkdir(parents=True, exist_ok=True)
        shutil.copy2(target(relative), path)
    with zipfile.ZipFile(target(JAR)) as archive:
        config = json.loads(archive.read(MIXINS))
        config.setdefault("mixins", [])
        if "CoreFacingMixin" in config["mixins"]:
            raise RuntimeError("CoreFacingMixin already installed; audit instead of duplicate install")
        config["mixins"].append("CoreFacingMixin")
        save(ROOT / "payload" / MIXINS, config)
        for name in archive.namelist():
            if name.startswith("assets/qys_event/") and name.endswith((".json", ".png")):
                output = ROOT / "preview/payload" / name
                output.parent.mkdir(parents=True, exist_ok=True)
                output.write_bytes(archive.read(name))
    for namespace in ("qys_event", "farmingtales", "ftboss"):
        for path in (WORKSPACE / PACK / namespace).rglob("*"):
            if path.is_file():
                output = ROOT / "preview/payload/assets" / namespace / path.relative_to(WORKSPACE / PACK / namespace)
                output.parent.mkdir(parents=True, exist_ok=True)
                shutil.copy2(path, output)
    model_project = WORKSPACE / "ftboss_model_edit/ft_instruments_remodel_20261008"
    for filename in ("verify_assets.py", "optimize_geometry.py", "test_optimization.py"):
        shutil.copy2(model_project / filename, ROOT / "preview" / filename)
    prior_preview = WORKSPACE / "_work/event-four-way-final-20261008/preview"
    for filename in ("viewer.html", "preview.js"):
        shutil.copy2(prior_preview / filename, ROOT / "preview" / filename)
    shutil.copytree(model_project / "vendor", ROOT / "preview/vendor", dirs_exist_ok=True)
    for filename in ("VerifyForgeFacing.java", "QysEventVerifier.java"):
        shutil.copy2(WORKSPACE / "_work/event-four-way-final-20261008" / filename, ROOT / filename)
    check_baseline(baseline)
    print("Fresh Forge baseline and latest optimized preview copied; NeoForge rollback verified read-only")


def stage():
    baseline = json.loads((ROOT / "baseline.json").read_text(encoding="utf-8"))["forge"]
    check_baseline(baseline)
    report = patch(target(JAR), ROOT / "candidate.jar", payload_bytes())
    save(ROOT / "candidate-delta.json", report)
    print(json.dumps(report, indent=2))


@contextmanager
def locked_archive():
    kernel = ctypes.WinDLL("kernel32", use_last_error=True)
    kernel.CreateFileW.argtypes = [ctypes.c_wchar_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p,
                                  ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p]
    kernel.CreateFileW.restype = ctypes.c_void_p
    kernel.CloseHandle.argtypes = [ctypes.c_void_p]
    kernel.CloseHandle.restype = ctypes.c_int
    lock_path = target(JAR).with_name("." + Path(JAR).name + ".shared-install.lock")
    handle = kernel.CreateFileW(str(lock_path), 0xC0000000, 0, None, 4, 0x80, None)
    if handle == ctypes.c_void_p(-1).value:
        raise RuntimeError("LOCKED: shared qys-event deployment lock unavailable")
    try:
        probe = kernel.CreateFileW(str(target(JAR)), 0x80000000, 0, None, 3, 0x80, None)
        if probe == ctypes.c_void_p(-1).value:
            raise RuntimeError("LOCKED: current qys-event JAR occupied; stopped without retry")
        kernel.CloseHandle(probe)
        yield kernel
    finally:
        kernel.CloseHandle(handle)


def deploy():
    if (ROOT / "deployment.json").exists():
        raise RuntimeError("Deployment already audited; do not automatically retry")
    checks = json.loads((ROOT / "verification.json").read_text(encoding="utf-8"))
    payload = payload_bytes()
    if not checks["passed"] or checks["payload_sha256"] != {name: sha(content) for name, content in payload.items()}:
        raise RuntimeError("Untested or changed runtime delta")
    expected = json.loads((ROOT / "baseline.json").read_text(encoding="utf-8"))["forge"]
    report = {"time": timestamp(), "scope": "Forge qys-event JAR only", "old_sha256": sha(target(JAR).read_bytes()),
              "written_items": [], "backup": None, "neo": neo_audit(), "qys_bosses_written": False}
    try:
        with locked_archive() as kernel:
            check_baseline(expected)
            report["old_sha256"] = expected[JAR]
            backup = ROOT / "deployment-backup" / Path(JAR).name
            backup.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(target(JAR), backup)
            if sha(backup.read_bytes()) != expected[JAR]:
                raise RuntimeError("BASELINE_CHANGED during backup")
            report["backup"] = str(backup)
            report["backup_sha256"] = expected[JAR]
            staged = ROOT / "deployment-current.jar"
            delta = patch(target(JAR), staged, payload)
            if staged.read_bytes() != (ROOT / "candidate.jar").read_bytes():
                raise RuntimeError("Candidate differs from tested current-JAR delta")
            report["jar_delta"] = delta
            save(ROOT / "deployment-journal.json", report)
            check_baseline(expected)
            kernel.ReplaceFileW.argtypes = [ctypes.c_wchar_p, ctypes.c_wchar_p, ctypes.c_wchar_p,
                                           ctypes.c_uint32, ctypes.c_void_p, ctypes.c_void_p]
            kernel.ReplaceFileW.restype = ctypes.c_int
            if not kernel.ReplaceFileW(str(target(JAR)), str(staged), None, 0, None, None):
                raise RuntimeError("LOCKED_OR_REPLACE_FAILED: stopped without fallback; error " + str(ctypes.get_last_error()))
            installed = sha(target(JAR).read_bytes())
            tested = sha((ROOT / "candidate.jar").read_bytes())
            if installed != tested:
                raise RuntimeError("Installed JAR hash mismatch")
            report.update({"status": "installed-offline-verified-game-pending", "new_sha256": installed,
                           "written_items": [{"path": JAR, "entries": list(ENTRIES)}]})
            expected[JAR] = installed
            check_baseline(expected)
            report["related_after_sha256"] = expected
            report["neo_after"] = neo_audit()
    except (RuntimeError, OSError) as error:
        report.update({"status": "STOPPED", "reason": str(error), "new_sha256": sha(target(JAR).read_bytes())})
        save(ROOT / "deployment.json", report)
        raise
    save(ROOT / "deployment.json", report)
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("operation", choices=("prepare", "stage", "deploy"))
    {"prepare": prepare, "stage": stage, "deploy": deploy}[parser.parse_args().operation]()
