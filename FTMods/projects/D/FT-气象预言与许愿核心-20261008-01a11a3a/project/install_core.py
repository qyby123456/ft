import argparse
import hashlib
import json
import os
import shutil
from datetime import datetime
from pathlib import Path

from verify_assets import verify


ROOT = Path(__file__).resolve().parent
WORKSPACE = ROOT.parent.parent.resolve()
WORKSPACES = [WORKSPACE, WORKSPACE.parent / "FarmingTales_NeoForge 机械动力"]
PAYLOAD = ROOT / "payload/assets/farmingtales"
RESOURCES = ["models/block/wishing_well_core.json", "models/item/wishing_well_core.json",
             "textures/block/wishing_well_core_atlas.png", "textures/block/wishing_well_core_particle.png"]


def digest(filename):
    return hashlib.sha256(filename.read_bytes()).hexdigest()


def gameplay_hashes(workspace):
    candidates = list((workspace / "kubejs/startup_scripts").rglob("*.js"))
    candidates += list((workspace / "kubejs/server_scripts").rglob("*.js"))
    candidates += list((workspace / "kubejs/client_scripts").rglob("*.js"))
    candidates += list((workspace / "kubejs/assets").rglob("blockstates/*.json"))
    return {str(file.relative_to(workspace)): digest(file) for file in candidates}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify-only", action="store_true")
    arguments = parser.parse_args()
    geometry = verify("wishing_well_core", "farmingtales")
    report_path = ROOT / "core-installation-report.json"
    prior = json.loads(report_path.read_text(encoding="utf-8")) if report_path.exists() else []
    report = []
    for workspace in WORKSPACES:
        workspace = workspace.resolve(strict=True)
        assets = workspace / "kubejs/assets/farmingtales"
        before = gameplay_hashes(workspace)
        backups = []
        if not arguments.verify_only:
            stamp = datetime.now().strftime("%Y%m%d-%H%M%S")
            for relative in RESOURCES:
                source, destination = PAYLOAD / relative, assets / relative
                if not destination.resolve().is_relative_to(assets.resolve()):
                    raise ValueError("Asset destination escapes namespace directory")
                if destination.exists() and destination.read_bytes() == source.read_bytes():
                    continue
                if destination.exists():
                    backup = ROOT / "backups/core" / stamp / workspace.name / relative
                    backup.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(destination, backup)
                    backups.append(str(backup))
                destination.parent.mkdir(parents=True, exist_ok=True)
                temporary = destination.with_name(destination.name + ".ft-core.tmp")
                if temporary.exists():
                    raise ValueError(f"Temporary asset already exists: {temporary}")
                try:
                    temporary.write_bytes(source.read_bytes())
                    os.replace(temporary, destination)
                finally:
                    temporary.unlink(missing_ok=True)
        for relative in RESOURCES:
            assert (assets / relative).read_bytes() == (PAYLOAD / relative).read_bytes()
        after = gameplay_hashes(workspace)
        assert before == after, "Gameplay scripts or blockstates changed during installation"
        previous = next((entry for entry in prior if entry["workspace"] == str(workspace)), {})
        if previous.get("gameplay_hashes"):
            assert previous["gameplay_hashes"] == after, "Gameplay baseline no longer matches"
        report.append({"workspace": str(workspace), "resources_verified": len(RESOURCES),
                       "backups": backups or previous.get("backups", []), "gameplay_hashes": after,
                       "gameplay_files_unchanged": len(after), "geometry": geometry,
                       "collision_unchanged": True, "structure_validation_unchanged": True})
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps([{key: value for key, value in entry.items() if key != "gameplay_hashes"} for entry in report], ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
