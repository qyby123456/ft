import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import struct
import tomllib
import zipfile


ROOT = Path(__file__).resolve().parents[1]
EVIDENCE = ROOT / "output/dimension-difficulty-20261102"
VERSION = "0.19.1"
CLASS_PREFIXES = ("ArenaDifficultyApi", "ArenaTorchBlock", "BloodthornBrazierBlock", "DungeonDifficultyApi",
                  "DungeonDifficultyConfig", "DungeonGateData", "DungeonGateTravel")
RESOURCE_CHANGES = {"META-INF/MANIFEST.MF", "META-INF/mods.toml",
                    "assets/ftdungeonmobs/lang/zh_cn.json", "assets/ftdungeonmobs/lang/en_us.json"}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def audit(jar, baseline):
    game_log = (EVIDENCE / "gametest-final.log").read_text(encoding="utf-8-sig", errors="replace")
    build_log = (EVIDENCE / "build.log").read_text(encoding="utf-8-sig", errors="replace")
    runtime_log = (EVIDENCE / "forge-data.log").read_text(encoding="utf-8-sig", errors="replace")
    match = re.search(r"DIMENSION_DIFFICULTY_TEST passed with (\d+) assertions", game_log)
    require(match is not None and int(match.group(1)) == 147, "Missing dimension GameTest result")
    require("All 5 required tests passed" in game_log and "BUILD SUCCESSFUL" in game_log, "Incomplete GameTest run")
    require("BUILD SUCCESSFUL" in build_log, "Missing successful final build")
    require("BUILD SUCCESSFUL" in runtime_log and "GATE_ROUNDS_VERIFIED" in runtime_log,
            "Missing actual Forge gate-round validation")
    with zipfile.ZipFile(jar) as candidate, zipfile.ZipFile(baseline) as previous:
        require(candidate.testzip() is None and previous.testzip() is None, "JAR CRC failure")
        names = candidate.namelist()
        require(len(names) == len(set(names)), "Duplicate JAR entries")
        require(set(names) == set(previous.namelist()), "Unexpected registration/resource/class additions or removals")
        mod = tomllib.loads(candidate.read("META-INF/mods.toml").decode("utf-8"))["mods"][0]
        require(mod["modId"] == "ftdungeonmobs" and mod["version"] == VERSION, "Wrong mod ID or version")
        old_mod = tomllib.loads(previous.read("META-INF/mods.toml").decode("utf-8"))["mods"][0]
        require(old_mod["modId"] == "ftdungeonmobs" and old_mod["version"] == "0.19.0", "Expected a 0.19.0 baseline")
        changed = []
        unchanged = []
        for name in names:
            if name.endswith(".class"):
                require(struct.unpack(">H", candidate.read(name)[6:8])[0] == 61, "Non-Java-17 class: " + name)
            require("GameTest" not in name and not name.endswith((".java", ".bbmodel", ".ps1")), "Development file in JAR: " + name)
            require(name != "data/ftdungeonmobs/structures/empty.nbt", "Test structure in production JAR")
            if candidate.read(name) == previous.read(name):
                unchanged.append(name)
                continue
            class_name = name.removeprefix("net/farmingtales/ftdungeonmobs/")
            permitted = name in RESOURCE_CHANGES or name.startswith("net/farmingtales/ftdungeonmobs/") and any(
                class_name == prefix + ".class" or class_name.startswith(prefix + "$") for prefix in CLASS_PREFIXES)
            require(permitted, "Unrelated runtime change: " + name)
            changed.append(name)
        source_resources = [path for path in (ROOT / "src/main/resources").rglob("*") if path.is_file()]
        for path in source_resources:
            relative = path.relative_to(ROOT / "src/main/resources").as_posix()
            require(candidate.read(relative) == path.read_bytes(), "Source resource differs from JAR: " + relative)
        preserved_visuals = [name for name in unchanged if name.endswith((".png", ".png.mcmeta", ".nbt"))
                             or any(marker in name for marker in ("/models/", "/geo/", "/animations/", "/blockstates/"))]
        for language in ("zh_cn", "en_us"):
            values = json.loads(candidate.read("assets/ftdungeonmobs/lang/" + language + ".json"))
            message = values["message.ftdungeonmobs.bloodthorn_tier"]
            require(message.count("%s") == 3 and "Radius" not in message and "半径" not in message, "Stale radius interaction message")
    return {"passed": True, "version": VERSION, "minecraft": "1.20.1", "forge_tested": "47.4.10",
            "java_class_major": 61, "gametests_passed": 5, "dimension_assertions": int(match.group(1)),
            "jar_sha256": digest(jar), "jar_bytes": jar.stat().st_size, "baseline_sha256": digest(baseline),
            "changed_jar_entries": changed, "unchanged_jar_entries": len(unchanged),
            "unchanged_visual_and_structure_entries": len(preserved_visuals), "source_resources_identical": len(source_resources),
            "new_runtime_entries": 0, "deployed": False, "client_tested": False,
            "limitations": ["Forge GameTest and resource validation are not an integrated multiplayer client test.",
                            "Only the existing FT dungeon dimension is targeted; other dimensions have no local aura."]}


def archive(path, entries):
    targets = [target for _, target in entries]
    require(len(targets) == len(set(targets)), "Duplicate source archive entries")
    with zipfile.ZipFile(path, "w", zipfile.ZIP_DEFLATED, compresslevel=9) as output:
        for source, target in sorted(entries, key=lambda item: item[1]):
            output.write(source, target)
    with zipfile.ZipFile(path) as output:
        require(output.testzip() is None, "Source archive CRC failure")


def main():
    parser = argparse.ArgumentParser(description="Audit and package the isolated dimension-difficulty release without deploying it.")
    parser.add_argument("--baseline-jar", required=True, type=Path)
    arguments = parser.parse_args()
    jar = ROOT / f"build/libs/ft-dungeon-mobs-forge-1.20.1-{VERSION}.jar"
    report = audit(jar, arguments.baseline_jar)
    write_json(EVIDENCE / "release-audit.json", report)
    release = ROOT.parent / "导出" / f"FT地牢怪物-Forge-1.20.1-{VERSION}-维度难度"
    require(not release.exists(), "Refusing to overwrite an existing release: " + str(release))
    release.mkdir(parents=True)
    shutil.copy2(jar, release / f"FT地牢怪物-Forge-1.20.1-{VERSION}.jar")
    for name in ("README.md", "DIMENSION_DIFFICULTY.md", "DIFFICULTY.md", "DUNGEON_GATES.md", "BLOODTHORN_BRAZIER.md",
                 "VERIFICATION.md", "DEPLOYMENT.md", "CREDITS.md"):
        shutil.copy2(ROOT / name, release / name)
    shutil.copy2(EVIDENCE / "release-audit.json", release / "release-audit.json")
    source_entries = []
    for directory in ("src", "tools", "config-examples", "gradle"):
        for path in (ROOT / directory).rglob("*"):
            if path.is_file() and "__pycache__" not in path.parts:
                source_entries.append((path, path.relative_to(ROOT).as_posix()))
    for path in ROOT.iterdir():
        if path.is_file() and (path.suffix in (".md", ".gradle", ".properties", ".ps1") or path.name == ".gitignore"):
            source_entries.append((path, path.name))
    for name in ("source-baseline.json", "latest-base-merge.json", "gametest.log", "gametest-final.log", "build.log", "forge-data.log", "release-audit.json"):
        path = EVIDENCE / name
        source_entries.append((path, path.relative_to(ROOT).as_posix()))
    archive(release / f"FT地牢怪物-源码-{VERSION}.zip", source_entries)
    manifest = {"name": "FT Dungeon Mobs - Dimension Difficulty", "version": VERSION, "date": "2026-11-02",
                "timezone": "Asia/Shanghai", "deployed": False, "client_tested": False, "source_dependencies_bundled": False,
                "source_dependency_note": "Reuse the six existing Forge development JAR dependencies from the original project libs directory.", "files": {}}
    for path in sorted(release.rglob("*")):
        if path.is_file():
            manifest["files"][path.relative_to(release).as_posix()] = {"bytes": path.stat().st_size, "sha256": digest(path)}
    write_json(release / "manifest.json", manifest)
    print(json.dumps({"release": str(release), "report": report, "files": len(manifest["files"])}, ensure_ascii=False))


if __name__ == "__main__":
    main()
