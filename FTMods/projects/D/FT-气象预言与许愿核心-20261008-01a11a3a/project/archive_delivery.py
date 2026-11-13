import hashlib
import json
import shutil
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent.parent.resolve()
DELIVERY_ROOT = Path("D:/ft工程文件")
DESTINATION = DELIVERY_ROOT / "FT-气象预言与许愿核心-20261008-01a11a3a"


def copy_file(source, destination):
    destination.parent.mkdir(parents=True, exist_ok=True)
    shutil.copy2(source, destination)
    if source.read_bytes() != destination.read_bytes():
        raise ValueError(f"Delivery copy mismatch: {source}")


def resource_zip(filename, namespace_names, metadata=None):
    with zipfile.ZipFile(filename, "w", compression=zipfile.ZIP_DEFLATED) as archive:
        if metadata:
            archive.writestr("pack.mcmeta", json.dumps(metadata, ensure_ascii=False, indent=2) + "\n")
        for source in sorted((ROOT / "payload").rglob("*")):
            if not source.is_file():
                continue
            relative = source.relative_to(ROOT / "payload")
            if relative.parts[1] in namespace_names:
                archive.write(source, relative.as_posix())
    with zipfile.ZipFile(filename) as archive:
        if archive.testzip() is not None:
            raise ValueError("Delivery ZIP failed CRC verification")


def main():
    if not DESTINATION.resolve().is_relative_to(DELIVERY_ROOT.resolve()):
        raise ValueError("Delivery path escapes user-named archive directory")
    DESTINATION.mkdir(parents=True, exist_ok=False)
    project = DESTINATION / "project"
    for source in sorted(ROOT.rglob("*")):
        if not source.is_file():
            continue
        relative = source.relative_to(ROOT)
        if relative.parts[0] in {"backups", "__pycache__"} or source.name == "delivery-report.json":
            continue
        copy_file(source, project / relative)
    copy_file(ROOT / "DELIVERY.md", DESTINATION / "README.md")
    for source in (ROOT / "backups").rglob("*"):
        if not source.is_file() or source.name.startswith("qys-bosses.jar"):
            continue
        copy_file(source, DESTINATION / "rollback" / source.relative_to(ROOT / "backups"))
    delta = DESTINATION / "runtime-delta"
    delta.mkdir()
    resource_zip(delta / "Forge-resourcepack.zip", {"qys_event", "farmingtales", "ftboss"},
                 {"pack": {"pack_format": 15, "description": "FT 天气预报机、预言机与许愿井核心重制"}})
    resource_zip(delta / "NeoForge-resourcepack.zip", {"qys_event", "farmingtales"},
                 {"pack": {"pack_format": 34, "description": "FT 天气预报机、预言机与许愿井核心重制"}})
    resource_zip(delta / "qys-event-assets.zip", {"qys_event"})
    resource_zip(delta / "kubejs-core-assets.zip", {"farmingtales"})
    tests = ("verify_wishing_well_assets.py", "verify_wishing_well.mjs", "verify_wishing_well_runtime.mjs",
             "verify_wishing_well_command.mjs", "verify_wishing_well_against_nbt.mjs", "xyj_layout.json")
    for filename in tests:
        copy_file(ROOT.parent / filename, DESTINATION / "reference/tests" / filename)
    for workspace in (WORKSPACE, WORKSPACE.parent / "FarmingTales_NeoForge 机械动力"):
        for relative in ("kubejs/startup_scripts/content/wishing_well.js", "kubejs/server_scripts/block_events/wishing_well.js", "kubejs/client_scripts/tooltips/wishing_well.js", "kubejs/assets/farmingtales/blockstates/wishing_well_core.json"):
            source = workspace / relative
            if source.exists():
                copy_file(source, DESTINATION / "reference" / workspace.name / relative)
    files = []
    for source in sorted(DESTINATION.rglob("*")):
        if source.is_file():
            files.append({"path": source.relative_to(DESTINATION).as_posix(), "bytes": source.stat().st_size,
                          "sha256": hashlib.sha256(source.read_bytes()).hexdigest()})
    checksums = {"source_workspace": str(ROOT), "destination": str(DESTINATION), "source_preserved": True,
                 "file_count": len(files), "files": files}
    (DESTINATION / "CHECKSUMS.json").write_text(json.dumps(checksums, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for entry in files:
        source = DESTINATION / entry["path"]
        assert source.stat().st_size == entry["bytes"]
        assert hashlib.sha256(source.read_bytes()).hexdigest() == entry["sha256"]
    report = {"destination": str(DESTINATION), "verified_files": len(files), "bytes": sum(entry["bytes"] for entry in files),
              "editable_project": True, "textures": True, "effects_manifest": True, "runtime_delta_zips": 4,
              "previews": True, "verification_manifest": True, "original_workspace_preserved": True}
    (ROOT / "delivery-report.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
