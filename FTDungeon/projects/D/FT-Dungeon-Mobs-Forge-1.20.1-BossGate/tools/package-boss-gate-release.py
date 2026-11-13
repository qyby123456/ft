import argparse
import hashlib
import json
import shutil
import zipfile
from pathlib import Path


VERSION = "0.20.1"
ALLOWED_CHANGED_CLASSES = {
    "ArenaDifficultyApi", "ArenaTorchBlock", "BloodthornBrazierBlock", "DungeonDifficultyConfig",
    "DungeonGateBlock", "DungeonGateBlockEntity", "DungeonGateData", "DungeonGateInteraction",
    "DungeonGateTravel", "FTDungeonMobs", "DungeonClient",
}
ALLOWED_NEW_CLASSES = {"DungeonBossGateApi", "DungeonBossGateData", "DungeonBossGatherEvent", "DungeonBossGateCommands"}
ALLOWED_CHANGED_RESOURCES = {
    "META-INF/MANIFEST.MF", "META-INF/mods.toml",
    "assets/ftdungeonmobs/lang/en_us.json", "assets/ftdungeonmobs/lang/zh_cn.json",
}
ALLOWED_NEW_RESOURCES = {
    "assets/ftdungeonmobs/blockstates/dungeon_boss_gate.json",
    "assets/ftdungeonmobs/models/item/dungeon_boss_gate.json",
    "data/ftdungeonmobs/loot_tables/blocks/dungeon_boss_gate.json",
}


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def sha256(content):
    return hashlib.sha256(content).hexdigest()


def archive_entries(path):
    with zipfile.ZipFile(path) as archive:
        require(archive.testzip() is None, f"ZIP CRC failed: {path}")
        names = [entry.filename for entry in archive.infolist() if not entry.is_dir()]
        require(len(names) == len(set(names)), f"Duplicate entries: {path}")
        return {name: archive.read(name) for name in names}


def class_name(name):
    return Path(name).name.split("$")[0].removesuffix(".class")


def main():
    parser = argparse.ArgumentParser(description="Audit and package the isolated Boss gate candidate without installing it.")
    parser.add_argument("--baseline-jar", type=Path, required=True)
    parser.add_argument("--output", type=Path, required=True)
    arguments = parser.parse_args()
    root = Path(__file__).resolve().parents[1]
    audit_directory = root / "output/boss-gate-20261102"
    built_jar = root / f"build/libs/ft-dungeon-mobs-forge-1.20.1-{VERSION}.jar"
    logs = {}
    for filename in ("gametest.log", "runData.log", "build.log"):
        logs[filename] = (audit_directory / filename).read_text(encoding="utf-8", errors="replace")
        require("BUILD SUCCESSFUL" in logs[filename], f"Missing successful validation: {filename}")
    require("All 10 required tests passed" in logs["gametest.log"], "Expected ten passing Forge GameTests")
    require("BOSS_GATE_VERIFIED assertions=93" in logs["gametest.log"], "Missing Boss gate scenario assertions")
    require("BOSS_GATE_PLAYER_TICK_PASS" in logs["gametest.log"], "Missing actual Forge PlayerTick entry-path test")
    require("147 assertions" in logs["gametest.log"], "Missing dimension difficulty regression")
    require("3456 tier/open/moving states across three gates" in logs["runData.log"], "Missing three-gate native validation")
    require("BOSS_GATE_RESOURCES_VERIFIED" in logs["build.log"], "Missing Boss gate asset verification")
    previous = archive_entries(arguments.baseline_jar)
    current = archive_entries(built_jar)
    removed = sorted(previous.keys() - current.keys())
    added = sorted(current.keys() - previous.keys())
    changed = sorted(name for name in previous.keys() & current.keys() if previous[name] != current[name])
    unchanged = sorted(name for name in previous.keys() & current.keys() if previous[name] == current[name])
    require(not removed, f"Unexpected removed entries: {removed}")
    for name in changed:
        require(name in ALLOWED_CHANGED_RESOURCES or name.endswith(".class")
                and class_name(name) in ALLOWED_CHANGED_CLASSES, f"Unexpected changed entry: {name}")
    for name in added:
        require(name in ALLOWED_NEW_RESOURCES or name.endswith(".class")
                and class_name(name) in ALLOWED_NEW_CLASSES, f"Unexpected added entry: {name}")
    for name, content in current.items():
        if name.endswith(".class"):
            require(content[:4] == bytes.fromhex("cafebabe") and int.from_bytes(content[6:8], "big") == 61,
                    f"Not Java 17 bytecode: {name}")
            require("GameTest" not in name and "/verification/" not in name, f"Test code leaked into JAR: {name}")
    require(f'version="{VERSION}"' in current["META-INF/mods.toml"].decode("utf-8"), "Wrong mod version")
    visual_names = [name for name in previous if name.startswith("assets/ftdungeonmobs/")
                    and any(segment in name for segment in ("/models/", "/textures/", "/animations/", "/geo/", "/blockstates/"))]
    structure_names = [name for name in previous if name.startswith("data/ftdungeonmobs/structures/")]
    for name in visual_names + structure_names:
        require(current[name] == previous[name], f"Existing visual/structure changed: {name}")
    require(json.loads(current["assets/ftdungeonmobs/blockstates/dungeon_boss_gate.json"])
            == json.loads(current["assets/ftdungeonmobs/blockstates/dungeon_entry_gate.json"]), "Shared gate model drift")
    report = {
        "version": VERSION, "installed": False, "baseline": str(arguments.baseline_jar.resolve()),
        "baseline_sha256": sha256(arguments.baseline_jar.read_bytes()), "jar_sha256": sha256(built_jar.read_bytes()),
        "unchanged_entries": len(unchanged), "changed_entries": changed, "added_entries": added,
        "removed_entries": removed, "unchanged_visuals": len(visual_names), "unchanged_structures": len(structure_names),
        "validation": {"forge_gametests": 10, "boss_scenario_assertions": 93, "actual_player_tick": True,
                       "dimension_assertions": 147, "runData": True, "build": True, "full_pack_client_playtest": False},
    }
    output = arguments.output.resolve()
    output.mkdir(parents=True, exist_ok=True)
    release_jar = output / f"FT地牢怪物-Forge-1.20.1-{VERSION}.jar"
    if release_jar.exists():
        require(sha256(release_jar.read_bytes()) == report["jar_sha256"], "Refusing to replace a different release JAR")
    shutil.copy2(built_jar, release_jar)
    for filename in ("README.md", "BOSS_GATE.md", "DIMENSION_DIFFICULTY.md", "ROOM_RULES.md", "DIFFICULTY.md",
                     "DUNGEON_GATES.md", "CREDITS.md", "VERIFICATION.md", "DEPLOYMENT.md"):
        shutil.copy2(root / filename, output / filename)
    source_zip = output / f"FT地牢怪物-源码-{VERSION}.zip"
    source_files = [file for folder in ("src", "tools", "gradle", "config-examples")
                    for file in (root / folder).rglob("*") if file.is_file() and "__pycache__" not in file.parts]
    source_files += [file for file in root.iterdir() if file.is_file()
                     and (file.suffix in (".md", ".gradle", ".properties", ".ps1") or file.name == ".gitignore")]
    with zipfile.ZipFile(source_zip, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for file in sorted(source_files):
            archive.write(file, file.relative_to(root).as_posix())
    source_entries = archive_entries(source_zip)
    require(all(not name.startswith(("libs/", "run/", "build/", ".gradle/")) for name in source_entries),
            "Source archive must not bundle third-party mods or test worlds")
    for filename in ("gametest.log", "runData.log", "build.log"):
        shutil.copy2(audit_directory / filename, output / filename)
    manifest = {file.name: {"bytes": file.stat().st_size, "sha256": sha256(file.read_bytes())}
                for file in sorted(output.iterdir()) if file.is_file() and file.name not in ("release-audit.json", "manifest.json")}
    (output / "release-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (audit_directory / "release-audit.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    manifest["release-audit.json"] = {"bytes": (output / "release-audit.json").stat().st_size,
                                     "sha256": sha256((output / "release-audit.json").read_bytes())}
    (output / "manifest.json").write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    for filename, entry in manifest.items():
        require(sha256((output / filename).read_bytes()) == entry["sha256"], f"Delivery hash mismatch: {filename}")
    print(json.dumps({"release": str(output), **report, "source_files": len(source_entries),
                      "delivery_hashes_verified": len(manifest)}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
