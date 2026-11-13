import datetime
import hashlib
import json
import os
from pathlib import Path
import shutil
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / "work/session-boss"
BUILD = WORK / "build/boss-split-d9472e1d"
PREVIOUS = WORK / "build/boss-split-0b113b69"
STARTUP = WORK / "reports/independent-boss-final-startup-3306abc8.json"
LOOP = WORK / "reports/independent-boss-loop-96794243.json"


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def digest(path):
    checksum = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(1048576), b""):
            checksum.update(block)
    return checksum.hexdigest()


def save(path, value):
    with Path(path).open("x", encoding="utf-8") as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)


claim = load(ROOT / "WRITE_OWNER.claim")
assert claim["threadId"] == "01a1c43e-ae80-7390-b604-b43b4ab53f2f"
startup = load(STARTUP)
loop = load(LOOP)
for result in [startup, loop]:
    terminal = result["terminal"]
    assert terminal["exitCode"] == 0 and terminal["stopSent"] and terminal["completeSave"] and terminal["inputHashesUnchanged"]
    assert result["probe"]["failures"] == 0
    for name, wanted in result["inputHashes"].items():
        assert digest(name) == wanted, name
assert len(startup["probe"]["cases"]) == 21
assert loop["probe"]["actualPostTicks"] == 660 and loop["probe"]["snapshots"] == 33 and loop["probe"]["actualNaturalBossCount"] == 24
built = load(BUILD / "build-receipt.json")
core_jar = Path(built["jar"])
assert digest(core_jar) == built["jarSha256"]
assert startup["inputHashes"][str(core_jar)] == built["jarSha256"]
plan = load(BUILD / "partition-plan.json")
previous = load(PREVIOUS / "partition-plan.json")
shared = set(plan["core"]) & set(previous["core"])
assert len(shared) == 1428
assert all(plan["classInputs"][name]["sha256"] == previous["classInputs"][name]["sha256"] for name in shared)
assert set(plan["core"]) - shared == {"net/farmingtales/ftboss/client/BossPreviewScreen", "net/farmingtales/ftboss/client/BossPreviewScreenRegistration"}
assert not plan["sharedFrontier"] and built["coreClasses"] == 1430 and built["removedWeaponClasses"] == 966
for row in plan["classInputs"].values():
    assert digest(row["path"]) == row["sha256"]
with zipfile.ZipFile(core_jar) as archive:
    assert archive.testzip() is None
    assert "net/farmingtales/ftboss/item/FoodWeaponEvolution.class" not in archive.namelist()
baseline = load(ROOT / "baseline.json")
target = Path(baseline["neoforge"]["live"]).resolve()
expected = Path("C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar").resolve()
assert target == expected and target.parent == expected.parent and target.suffix == ".jar"
assert digest(target) == baseline["neoforge"]["sha256"], "Live target drift; no overwrite"
forge_observed_sha256 = digest(Path(baseline["forge"]["live"]))
release = WORK / "releases/boss-core-stage01-20261112"
assert not release.exists()
release.mkdir(parents=True)
rollback = release / "rollback-qys-bosses-1.21.1.jar"
shutil.copy2(target, rollback)
assert digest(rollback) == baseline["neoforge"]["sha256"]
released_core = release / "ftboss-boss-core-1.21.1-stage01.jar"
shutil.copyfile(core_jar, released_core)
assert digest(released_core) == built["jarSha256"]
weapon_jar = release / "ftboss-weapons-1.21.1-PAUSED-SNAPSHOT.jar"
resource_plan = load(BUILD / "resource-partition.json")
original = Path(plan["originalApplication"])
weapon_manifest = {}
with zipfile.ZipFile(weapon_jar, "x", compression=zipfile.ZIP_DEFLATED) as archive:
    archive.writestr("META-INF/MANIFEST.MF", "Manifest-Version: 1.0\nImplementation-Title: FT Weapons Paused Snapshot\nImplementation-Version: a08a7373\nFTBoss-Installable: false\n\n")
    archive.writestr("README.txt", "Weapon functional development is paused by user.\nThis is a separate recoverable JAR archive, not a NeoForge addon release.\n966 original compiled weapon classes are preserved under frozen-classes/.\nThere is no mod entrypoint or neoforge.mods.toml. Never put this snapshot in mods/.\nResuming a runnable addon still requires package relocation, registration/adapters, installation guard rebinding and original full signed Session acceptance.\nThe complete source/evidence checkpoint remains at archives/weapons-paused-a08a7373/.\n")
    for name in plan["weapons"]:
        row = plan["classInputs"][name]
        archive_name = "frozen-classes/" + name + ".class"
        archive.write(row["path"], archive_name)
        weapon_manifest[archive_name] = row["sha256"]
    for name, row in resource_plan["movedResources"].items():
        archive_name = "frozen-resources/" + name
        archive.write(row["path"], archive_name)
        weapon_manifest[archive_name] = row["sha256"]
    for path in original.glob("*.mixins.json"):
        if path.name != "ftboss.mixins.json":
            archive.write(path, "frozen-resources/" + path.name)
            weapon_manifest["frozen-resources/" + path.name] = digest(path)
    archive.writestr("weapon-class-manifest.json", json.dumps(weapon_manifest, ensure_ascii=False, indent=2))
with zipfile.ZipFile(weapon_jar) as archive:
    assert archive.testzip() is None
    for name, wanted in weapon_manifest.items():
        assert hashlib.sha256(archive.read(name)).hexdigest() == wanted
    assert "META-INF/neoforge.mods.toml" not in archive.namelist()
save(release / "release-preflight.json", {"coreJar": str(released_core), "coreSha256": digest(released_core), "weaponSnapshotJar": str(weapon_jar), "weaponSnapshotSha256": digest(weapon_jar), "weaponRuntimeInstallable": False, "coreStartupReceipt": str(STARTUP), "loopReceipt": str(LOOP), "reusedIdenticalCoreClassesFromNaturalLoop": len(shared), "coreClasses": 1430, "separatedWeaponClasses": 966, "originalLiveTarget": str(target), "rollback": str(rollback), "rollbackSha256": digest(rollback), "bossClientGlAccepted": False, "bossAuthenticatedCombatAccepted": False, "fullBossMigrationComplete": False, "releaseScope": "Incremental Boss core module separation and independent dedicated-server loading"})
restore = """# Boss core stage 01\n\nThis is an incremental Boss migration release, not complete client/GL/authenticated combat acceptance.\nThe Boss core JAR alone was loaded in genuine NeoForge 21.1.233, with 21 startup/registry/owner checks passing.\n24 original Manor Boss actors completed 660 natural post ticks and 33 snapshots in the preceding package. All 1428 shared core classes are byte-for-byte identical; the final package removes 53 orphan weapon helpers and adds two client screen classes.\nThe new client screen is compiled, but actual rendering remains unaccepted.\n\nThe weapon snapshot JAR is a separate compressed recovery artifact, not an installable mod. Keep it outside mods/. The original paused full source/evidence ZIP is preserved.\n\nRollback: stop Minecraft completely; verify the current deployed SHA from deployment-receipt.json, then copy rollback-qys-bosses-1.21.1.jar to the exact originalLiveTarget. Verify the restored SHA equals rollbackSha256. Do not write the Forge instance or historical D workspace.\n\nRemaining migration: resolve original Boss feature inventory, real client rendering and preview UI, skill/battle behavior, authenticated multiplayer, and formal-instance startup. No duplicate dependency mods have been changed.\n"""
(release / "RESTORE.md").write_text(restore, encoding="utf-8")
assert digest(target) == baseline["neoforge"]["sha256"]
temporary = target.with_name(target.name + ".stage01-" + uuid.uuid4().hex[:8] + ".tmp")
assert temporary.resolve().parent == expected.parent and not temporary.exists()
shutil.copyfile(released_core, temporary)
assert digest(temporary) == built["jarSha256"]
os.replace(temporary, target)
assert digest(target) == built["jarSha256"]
deployment = {"deployedAt": datetime.datetime.now(datetime.timezone.utc).isoformat(), "target": str(target), "deployedSha256": digest(target), "deployedBytes": target.stat().st_size, "backup": str(rollback), "backupSha256": digest(rollback), "rollbackHashVerifiedBeforeDeployment": True, "weaponSnapshot": str(weapon_jar), "weaponSnapshotSha256": digest(weapon_jar), "weaponSnapshotDeployed": False, "weaponFunctionalDevelopmentPaused": True, "bossIndependentDedicatedServerStartupAccepted": True, "bossClientGlAccepted": False, "fullBossMigrationComplete": False, "formalInstanceLaunched": False, "duplicateDependenciesModified": False, "forgeModified": False, "historicalDModified": False, "inputHashes": {str(path): digest(path) for path in [Path(__file__), STARTUP, LOOP, BUILD / "build-receipt.json", BUILD / "partition-plan.json", BUILD / "resource-partition.json", ROOT / "baseline.json", release / "release-preflight.json"]}}
deployment["readOnlyForgeObservedSha256"] = forge_observed_sha256
deployment["readOnlyForgeMatchesHistoricalBaseline"] = forge_observed_sha256 == baseline["forge"]["sha256"]
assert digest(Path(baseline["forge"]["live"])) == forge_observed_sha256
save(release / "deployment-receipt.json", deployment)
archive_path = WORK / "archives/boss-core-stage01-20261112.zip"
assert not archive_path.exists()
files = list(release.rglob("*")) + list((WORK / "boss-core").rglob("*")) + list((WORK / "tools").glob("*.py")) + list((WORK / "tests").glob("*.java")) + list((WORK / "reports").glob("*.json"))
files += [BUILD / "build-receipt.json", BUILD / "partition-plan.json", BUILD / "resource-partition.json", WORK / "build/boss-module-boundary-v4/compile-context.json", WORK / "build/boss-module-boundary-v4/compile.log", WORK / "CURRENT_BOSS_BATCH.json", ROOT / "AGENTS.md", WORK / "archives/weapons-paused-a08a7373/archive-receipt.json"]
for result in [startup, loop]:
    runtime = Path(result["attempt"])
    files += [runtime / "runtime.log", runtime / "terminal.json", runtime / "runtime-command.json", runtime / "inputs-before.json"]
    files += list((runtime / "server").glob("actual-*.json")) + list((runtime / "server").glob("actual-*.jsonl"))
manifest = {}
with zipfile.ZipFile(archive_path, "x", compression=zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(set(files)):
        if not path.is_file():
            continue
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {"bytes": path.stat().st_size, "sha256": digest(path)}
        archive.write(path, name)
    archive.writestr("ARCHIVE-MANIFEST.json", json.dumps(manifest, ensure_ascii=False, indent=2))
with zipfile.ZipFile(archive_path) as archive:
    assert archive.testzip() is None
    for name, row in manifest.items():
        assert hashlib.sha256(archive.read(name)).hexdigest() == row["sha256"]
save(release / "archive-receipt.json", {"archive": str(archive_path), "archiveBytes": archive_path.stat().st_size, "archiveSha256": digest(archive_path), "files": len(manifest), "allFilesReadBackAndHashVerified": True, "physicalRestorePerformed": False, "failures": 0, "deployedCoreSha256": digest(target), "weaponSnapshotInstallable": False})
print(json.dumps({"deployment": str(release / "deployment-receipt.json"), "archive": str(archive_path), "coreClasses": 1430, "weaponClassesSeparated": 966, "deployedSha256": digest(target), "archiveBytes": archive_path.stat().st_size, "files": len(manifest), "weaponSnapshotInstallable": False}), flush=True)
