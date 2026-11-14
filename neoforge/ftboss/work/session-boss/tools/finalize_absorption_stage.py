import argparse
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
parser = argparse.ArgumentParser()
parser.add_argument("runtime_receipt", type=Path)
arguments = parser.parse_args()
runtime_receipt = arguments.runtime_receipt.resolve()


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


assert load(ROOT / "WRITE_OWNER.claim")["threadId"] == "01a1c43e-ae80-7390-b604-b43b4ab53f2f"
first = WORK / "releases/boss-core-stage01-20261112"
first_deployment = load(first / "deployment-receipt.json")
first_archive = load(first / "archive-receipt.json")
assert digest(first_archive["archive"]) == first_archive["archiveSha256"]
result = load(runtime_receipt)
terminal = result["terminal"]
assert result["originalHealBanAbsorptionGuardAccepted"] and result["probe"]["failures"] == 0
assert terminal["exitCode"] == 0 and terminal["stopSent"] and terminal["completeSave"] and terminal["inputHashesUnchanged"]
assert result["probe"]["actualPostTicks"] >= 20 and result["probe"]["originalBanDurationTicks"] == 600
for name, wanted in result["inputHashes"].items():
    assert digest(name) == wanted, name
build = WORK / "build/boss-split-9339206b"
built = load(build / "build-receipt.json")
jar = Path(built["jar"])
assert digest(jar) == built["jarSha256"] and result["inputHashes"][str(jar)] == built["jarSha256"]
plan = load(build / "partition-plan.json")
previous = load(WORK / "build/boss-split-d9472e1d/partition-plan.json")
assert set(previous["core"]) <= set(plan["core"])
assert set(plan["core"]) - set(previous["core"]) == {"net/farmingtales/ftboss/boss/ai/HealBanAbsorptionGuard"}
assert all(plan["classInputs"][name]["sha256"] == previous["classInputs"][name]["sha256"] for name in previous["core"])
assert not plan["sharedFrontier"] and built["coreClasses"] == 1431 and built["removedWeaponClasses"] == 966
baseline = load(ROOT / "baseline.json")
target = Path(first_deployment["target"]).resolve()
expected = Path("C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar").resolve()
assert target == expected and target.parent == expected.parent
assert digest(target) == first_deployment["deployedSha256"], "Target drift, no overwrite"
forge_before = digest(baseline["forge"]["live"])
release = WORK / "releases/boss-core-stage02-absorption-20261112"
assert not release.exists()
release.mkdir(parents=True)
backup = release / "rollback-stage01-qys-bosses-1.21.1.jar"
shutil.copy2(target, backup)
assert digest(backup) == first_deployment["deployedSha256"]
released_jar = release / "ftboss-boss-core-1.21.1-stage02.jar"
shutil.copyfile(jar, released_jar)
assert digest(released_jar) == built["jarSha256"]
source = WORK / "boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/HealBanAbsorptionGuard.java"
original = WORK / "build/absorption-original-recovery/sources/net/farmingtales/ftboss/boss/ai/HealBanAbsorptionGuard.java"
save(release / "stage02-verification.json", {"feature": "Original HealBan absorption guard restored", "originalSource": str(original), "originalSourceSha256": digest(original), "portSource": str(source), "portSourceSha256": digest(source), "runtimeReceipt": str(runtime_receipt), "cases": result["probe"]["cases"], "unchangedPriorCoreClasses": 1430, "onlyNewCoreClass": "boss/ai/HealBanAbsorptionGuard", "originalBanTicks": 600, "semantics": ["Original heal-ban Added event clears existing absorption", "Original absorption Applicable denied on banned server entities", "Natural living and player tick suppression", "Native incoming damage suppression before absorption can shield damage", "Unbanned controls and post-ban absorption preserved"], "playerSpecificTickAndAuthenticatedMultiplayerAccepted": False, "fullBossMigrationComplete": False, "clientGlAccepted": False, "weaponFunctionalDevelopmentPaused": True})
(release / "RESTORE.md").write_text("# Boss core stage 02: original heal-ban absorption guard\n\nStop Minecraft completely before rollback. Verify the current deployed hash from deployment-receipt.json. Copy rollback-stage01-qys-bosses-1.21.1.jar to the exact target in that receipt and verify backupSha256.\n\nThis is an incremental dedicated-server feature release. The original 600-tick ban, normal entity healing and unbanned absorption values are preserved. Actual effect addition, rejection, incoming damage and 20 natural living ticks passed, with unbanned controls. Authenticated player-specific behavior and real client rendering remain unaccepted.\n\nStage01 and the full paused weapon archive remain at their original paths; weapon functional work remains paused and its snapshot JAR stays outside mods/. No duplicate dependency mods, Forge files or historical D files were changed.\n", encoding="utf-8")
temporary = target.with_name(target.name + ".stage02-" + uuid.uuid4().hex[:8] + ".tmp")
assert temporary.resolve().parent == expected.parent and not temporary.exists()
assert digest(target) == first_deployment["deployedSha256"]
shutil.copyfile(released_jar, temporary)
assert digest(temporary) == built["jarSha256"]
os.replace(temporary, target)
assert digest(target) == built["jarSha256"]
assert digest(baseline["forge"]["live"]) == forge_before
save(release / "deployment-receipt.json", {"deployedAt": datetime.datetime.now(datetime.timezone.utc).isoformat(), "target": str(target), "deployedSha256": digest(target), "backup": str(backup), "backupSha256": digest(backup), "backupHashVerifiedBeforeDeployment": True, "formalInstanceLaunched": False, "feature": "Boss original heal-ban absorption guard", "coreClasses": 1431, "weaponSnapshotDeployed": False, "weaponDevelopmentPaused": True, "fullBossMigrationComplete": False, "clientGlAccepted": False, "authenticatedPlayerSpecificBehaviorAccepted": False, "duplicateDependenciesModified": False, "forgeModified": False, "historicalDModified": False, "previousDeployment": str(first / "deployment-receipt.json"), "previousArchiveSha256": first_archive["archiveSha256"], "inputHashes": {str(path): digest(path) for path in [Path(__file__), runtime_receipt, build / "build-receipt.json", source, original, release / "stage02-verification.json"]}})
archive_path = WORK / "archives/boss-core-stage02-absorption-20261112.zip"
assert not archive_path.exists()
files = list(release.rglob("*")) + list((WORK / "boss-core").rglob("*")) + list((WORK / "tests").glob("*.java")) + list((WORK / "tools").glob("*.py"))
files += [runtime_receipt, build / "build-receipt.json", build / "partition-plan.json", build / "resource-partition.json", first / "deployment-receipt.json", first / "archive-receipt.json", WORK / "archives/weapons-paused-a08a7373/archive-receipt.json", WORK / "CURRENT_BOSS_BATCH.json", original, WORK / "build/boss-module-boundary-v5/compile-context.json", WORK / "build/boss-module-boundary-v5/compile.log"]
runtime = Path(result["attempt"])
files += [runtime / "runtime.log", runtime / "terminal.json", runtime / "runtime-command.json", runtime / "inputs-before.json", runtime / "server/actual-boss-absorption-probe.json"]
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
save(release / "archive-receipt.json", {"archive": str(archive_path), "archiveSha256": digest(archive_path), "archiveBytes": archive_path.stat().st_size, "files": len(manifest), "allFilesReadBackAndHashVerified": True, "physicalRestorePerformed": False, "failures": 0, "deployedSha256": digest(target)})
print(json.dumps({"deployment": str(release / "deployment-receipt.json"), "archive": str(archive_path), "archiveBytes": archive_path.stat().st_size, "coreClasses": 1431, "passedRuntimeCases": len(result["probe"]["cases"]), "deployedSha256": digest(target)}), flush=True)
