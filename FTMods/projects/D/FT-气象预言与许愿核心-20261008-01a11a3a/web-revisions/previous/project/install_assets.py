import argparse
import ctypes
import hashlib
import json
import os
import shutil
import time
import zipfile
from contextlib import contextmanager
from datetime import datetime
from pathlib import Path


ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent.parent
NEOFORGE = WORKSPACE.parent / "FarmingTales_NeoForge 机械动力"
TARGETS = [WORKSPACE / "mods/qys-event-forge-1.20.1-1.0.6.jar", NEOFORGE / "mods/qys-event-neoforge-1.21.1-1.0.8.jar"]
SOURCE = WORKSPACE / "_work/nf-sync-20261005/qys-event/src/main/resources"


def digest(data):
    return hashlib.sha256(data).hexdigest()


def archive_map(target):
    with zipfile.ZipFile(target) as archive:
        names = archive.namelist()
        if len(names) != len(set(names)):
            raise ValueError(f"Duplicate ZIP entries in {target}")
        bad_entry = archive.testzip()
        if bad_entry:
            raise ValueError(f"Invalid ZIP entry: {bad_entry}")
        return {entry.filename: digest(archive.read(entry)) for entry in archive.infolist()}


@contextmanager
def shared_archive_lock(target):
    if os.name != "nt":
        raise RuntimeError("This deployment helper requires Windows archive locking")
    kernel = ctypes.WinDLL("kernel32", use_last_error=True)
    kernel.CreateFileW.argtypes = [ctypes.c_wchar_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p, ctypes.c_uint32, ctypes.c_uint32, ctypes.c_void_p]
    kernel.CreateFileW.restype = ctypes.c_void_p
    kernel.CloseHandle.argtypes = [ctypes.c_void_p]
    kernel.CloseHandle.restype = ctypes.c_int
    lock_name = ".qys-bosses-install.lock" if target.name == "qys-bosses.jar" else f".{target.name}.shared-install.lock"
    lock_path = target.parent / lock_name
    handle = None
    for attempt in range(61):
        handle = kernel.CreateFileW(str(lock_path), 0xC0000000, 0, None, 4, 0x80, None)
        if handle != ctypes.c_void_p(-1).value:
            break
        error = ctypes.get_last_error()
        if error not in (32, 33) or attempt == 60:
            raise ctypes.WinError(error)
        time.sleep(0.5)
    try:
        yield
    finally:
        kernel.CloseHandle(handle)


def install(target, payload):
    target = target.resolve(strict=True)
    if not any(target.is_relative_to(workspace.resolve()) for workspace in (WORKSPACE, NEOFORGE)):
        raise ValueError(f"Target outside FT workspaces: {target}")
    with shared_archive_lock(target):
        current = archive_map(target)
        if all(current.get(name) == digest(content) for name, content in payload.items()):
            return {"target": str(target), "status": "already_installed", "resources": len(payload)}
        return patch_archive(target, payload)


def patch_archive(target, payload):
    lock_path = target.with_name(target.name + ".instruments-install.lock")
    lock_handle = os.open(lock_path, os.O_CREAT | os.O_EXCL | os.O_WRONLY)
    temporary = target.with_name(target.name + f".instruments-{os.getpid()}.tmp")
    try:
        before_digest = digest(target.read_bytes())
        before_entries = archive_map(target)
        backup = ROOT / "backups" / (target.name + "." + datetime.now().strftime("%Y%m%d-%H%M%S") + ".bak")
        backup.parent.mkdir(exist_ok=True)
        shutil.copy2(target, backup)
        with zipfile.ZipFile(target) as original, zipfile.ZipFile(temporary, "w", compression=zipfile.ZIP_DEFLATED) as patched:
            patched.comment = original.comment
            for entry in original.infolist():
                if entry.filename not in payload:
                    patched.writestr(entry, original.read(entry))
            for name, content in payload.items():
                patched.writestr(name, content)
        after_entries = archive_map(temporary)
        unchanged = {name for name in before_entries if name not in payload}
        if any(before_entries[name] != after_entries.get(name) for name in unchanged):
            raise ValueError("Unrelated archive content was changed")
        if any(after_entries.get(name) != digest(content) for name, content in payload.items()):
            raise ValueError("Installed resource mismatch")
        if digest(target.read_bytes()) != before_digest:
            raise RuntimeError(f"Another editor changed {target}; installation aborted")
        for attempt in range(5):
            try:
                os.replace(temporary, target)
                break
            except PermissionError:
                if attempt == 4:
                    raise
                time.sleep(0.5)
        if archive_map(target) != after_entries:
            raise ValueError("Final archive verification failed")
        return {"target": str(target), "backup": str(backup), "resources": len(payload), "unchanged_entries": len(unchanged),
                "class_files_unchanged": sum(name.endswith(".class") for name in unchanged),
                "blockstates_unchanged": all(before_entries[name] == after_entries[name] for name in before_entries if "/blockstates/" in name),
                "sha256": digest(target.read_bytes())}
    finally:
        os.close(lock_handle)
        lock_path.unlink(missing_ok=True)
        temporary.unlink(missing_ok=True)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify-only", action="store_true")
    arguments = parser.parse_args()
    payload = {file.relative_to(ROOT / "payload").as_posix(): file.read_bytes() for file in sorted((ROOT / "payload").rglob("*")) if file.is_file() and file.relative_to(ROOT / "payload").parts[1] in {"qys_event", "ftboss"}}
    legacy_names = {"assets/ftboss/models/block/oracle.json", "assets/ftboss/models/item/oracle.json"}
    if not payload or any(not name.startswith("assets/qys_event/") and name not in legacy_names for name in payload):
        raise ValueError("Only instrument visual resources may be installed")
    def resources_for(target):
        prefix = "assets/ftboss/" if target.name == "qys-bosses.jar" else "assets/qys_event/"
        return {name: content for name, content in payload.items() if name.startswith(prefix)}
    if arguments.verify_only:
        report = []
        for target in TARGETS:
            entries = archive_map(target)
            target_payload = resources_for(target)
            if any(entries.get(name) != digest(content) for name, content in target_payload.items()):
                raise ValueError(f"Resource mismatch in {target}")
            report.append({"target": str(target), "resources_verified": len(target_payload)})
        print(json.dumps(report, ensure_ascii=False, indent=2))
        return
    report_path = ROOT / "installation-report.json"
    report = json.loads(report_path.read_text(encoding="utf-8")) if report_path.exists() else []
    for target in TARGETS:
        result = install(target, resources_for(target))
        if result.get("status") != "already_installed" or not any(entry["target"] == result["target"] for entry in report):
            report = [entry for entry in report if entry["target"] != result["target"]] + [result]
        report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for name, content in payload.items():
        if not name.startswith("assets/qys_event/"):
            continue
        destination = SOURCE / name
        source_backup = ROOT / "backups/source" / name
        if destination.exists() and not source_backup.exists():
            source_backup.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(destination, source_backup)
        destination.parent.mkdir(parents=True, exist_ok=True)
        destination.write_bytes(content)
    (ROOT / "installation-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
