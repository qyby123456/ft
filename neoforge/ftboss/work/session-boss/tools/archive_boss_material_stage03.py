import argparse
import datetime
import hashlib
import json
from pathlib import Path
import shutil
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
result = load(runtime_receipt)
terminal = result["terminal"]
assert terminal["exitCode"] == 0 and terminal["stopSent"] and terminal["completeSave"] and terminal["inputHashesUnchanged"]
assert result["bossMaterialPublicOutputAccepted"] and result["probe"]["failures"] == 0
assert not result["authenticatedPlayerKillRewardAccepted"] and not result["probe"]["authenticatedPlayerKillRewardAccepted"]
for name, wanted in result["inputHashes"].items():
    assert digest(name) == wanted, name
build = WORK / "build/boss-split-12a3be06"
built = load(build / "build-receipt.json")
plan = load(build / "partition-plan.json")
jar = Path(built["jar"])
assert digest(jar) == built["jarSha256"] and result["inputHashes"][str(jar)] == built["jarSha256"]
assert not plan["sharedFrontier"] and built["coreClasses"] == 1434 and built["removedWeaponClasses"] == 966
assert "NOT-INSTALLABLE" in jar.name
proof = WORK / "build/boss-material-proof-v2"
assert load(proof / "terminal.json")["runExitCode"] == 0
assert "failures=0;" in (proof / "run.log").read_text(encoding="utf-8", errors="replace")
for row in plan["classInputs"].values():
    assert digest(row["path"]) == row["sha256"]
prior_deployment_path = WORK / "releases/boss-core-stage02-absorption-20261112/deployment-receipt.json"
prior_deployment = load(prior_deployment_path)
target = Path(prior_deployment["target"])
assert digest(target) == prior_deployment["deployedSha256"], "Formal instance changed externally; no adoption"
weapon_archive = WORK / "archives/weapons-paused-a08a7373"
frozen_weapon = load(weapon_archive / "archive-receipt.json")
assert digest(frozen_weapon["archive"]) == frozen_weapon["archiveSha256"]
assert digest(weapon_archive / "manifest.jsonl") == frozen_weapon["manifestSha256"]
assert digest(weapon_archive / "RESTORE.md") == frozen_weapon["restoreInstructionsSha256"]
output = WORK / "candidates/boss-core-stage03-material-drops-20261112"
assert not output.exists()
output.mkdir(parents=True)
frozen_jar = output / jar.name
shutil.copyfile(jar, frozen_jar)
assert digest(frozen_jar) == built["jarSha256"]
receipt = {
    "createdAt": datetime.datetime.now(datetime.timezone.utc).isoformat(),
    "feature": "Restore original Boss material drops independently of paused growth/customization",
    "status": "focused_checks_passed_full_reward_acceptance_pending_NOT_INSTALLABLE",
    "candidateJar": str(frozen_jar), "candidateSha256": digest(frozen_jar),
    "coreClasses": 1434, "weaponClassesSeparated": 966,
    "completionCriteria": str(WORK / "STAGE03_BOSS_MATERIAL_DROPS.md"),
    "originalForgeMaterialMappingAndCountVerified": True,
    "originalDropMethodsUnchangedExceptMaterialCallbackOwner": True,
    "sourceCompilerControlFlowDifferencePreservedAsFailedProof": str(WORK / "build/boss-material-proof-v1"),
    "exactOriginalMethodLinkReceipt": str(proof / "link-receipt.json"),
    "targetedBasicVerifierPassed": True,
    "runtimeReceipt": str(runtime_receipt), "runtimeCases": len(result["probe"]["cases"]),
    "actualRegisteredBossMaterialItems": result["probe"]["actualRewards"],
    "actualItemRegistryCount": result["probe"]["actualItemRegistryCount"],
    "actualEntityRegistryCount": result["probe"]["actualEntityRegistryCount"],
    "scope": "Real NeoForge packaged Boss JAR, direct original public material reward outputs, item properties, tooltips, serialization, creative output and direct no-player event-handler rejection",
    "authenticatedPlayerKillRewardAccepted": False,
    "actualBossDeathPositiveRewardAccepted": False,
    "clientGlAccepted": False, "fullBossMigrationComplete": False,
    "installable": False, "deployed": False,
    "formalInstanceUntouchedInThisStage": True,
    "priorDeployedSha256Unchanged": prior_deployment["deployedSha256"],
    "priorDeploymentKnownGap": "Stage01/02 optional-module split omitted ten original Boss materials; correction staged here, not falsely accepted as authenticated kill integration",
    "weaponFunctionalDevelopmentPaused": True, "weaponCandidateValidationResumed": False,
    "inputHashes": {str(path): digest(path) for path in [Path(__file__), runtime_receipt, build / "build-receipt.json", build / "partition-plan.json", proof / "terminal.json", proof / "run.log", proof / "link-receipt.json", WORK / "reports/resume-boss-audit-20261112.json", WORK / "STAGE03_BOSS_MATERIAL_DROPS.md", prior_deployment_path]}
}
save(output / "candidate-receipt.json", receipt)
(output / "RESTORE.md").write_text("""# Stage03 Boss materials — NOT-INSTALLABLE\n\nNo deployment occurred in this batch. The formal NeoForge JAR remains stage02, with the hash recorded in candidate-receipt.json. Stage01/02 omitted ten material drops; this candidate corrects that boundary, but full authenticated player kill integration is not accepted. Do not copy this candidate to mods/ until the required remaining acceptance passes.\n\nRestore development into a new scratch folder inside the active C workspace. Verify the archive SHA256 in archive-receipt.json, extract and verify ARCHIVE-MANIFEST.json for every entry. Sources, compiler output, exact original-bytecode boundary linker, directed ASM proof, runtime observer, logs, saved-world original location, failed source-compiler proof and restore notes are included or referenced explicitly. Never restore into historical D or Forge.\n\nReproduce: compile the 17 Boss boundary Java sources against the frozen genuine SDK classpath from the v6 compile context. The source compiler changed an equivalent branch layout in normalDrops, so preserve that raw class and use LinkBossMaterialDropBoundary with the frozen original BossRelicDrops.class to replace only its material reward callback owner. Run BossMaterialBoundaryProof; every original reward method must remain instruction-identical after normalizing that owner. Build with build_boss_core_stage03.py, then run only the focused Boss material probe. No weapon source compilation or signed Session validation is needed or permitted.\n\nFrozen SDK/application paths remain external read-only prerequisites. The 966 paused weapon classes are not resumed or installed. The complete weapon ZIP, manifest, hashes and RESTORE.md remain unchanged at archives/weapons-paused-a08a7373/.\n\nRemaining: real authenticated player kills through both original scripted and normal death/drop paths, one material per eligible Boss, no duplicate payout, expected relic/staff output, real client rendering and multiplayer. Do not invent players, authority, lease or signed evidence. The original event-handler rejection and public output checks are scoped evidence only.\n\nRuntime isolated-home/isolated-tmp files are preserved in the archive before those two disposable directories are removed. Keep saved worlds, inputs and failure evidence at original paths. No other game clients or background Python services are owned by this batch.\n""", encoding="utf-8")
runtime = Path(result["attempt"])
files = list(output.rglob("*")) + list((WORK / "boss-core").rglob("*")) + list((WORK / "tools").glob("*.py")) + list((WORK / "tests").glob("*.java"))
for folder in [WORK / "build/boss-module-boundary-v6", WORK / "build/boss-material-proof-v1", proof, WORK / "build/recovered-boundary-0f610aa0"]:
    files += list(folder.rglob("*"))
files += [runtime_receipt, build / "build-receipt.json", build / "partition-plan.json", build / "resource-partition.json", prior_deployment_path, WORK / "STAGE03_BOSS_MATERIAL_DROPS.md", WORK / "reports/resume-boss-audit-20261112.json", WORK / "reports/stage03-audit-notes.md", WORK / "CURRENT_BOSS_BATCH.json", ROOT / "WRITE_OWNER.claim", ROOT / "WRITE_OWNER.json", ROOT / "AGENTS.md", weapon_archive / "archive-receipt.json", weapon_archive / "RESTORE.md"]
files += [runtime / name for name in ["runtime.log", "terminal.json", "runtime-command.json", "inputs-before.json", "compile-command.json", "compile-terminal.json", "compile.log", "live-process.json", "server/actual-boss-material-probe.json"]]
for folder in [runtime / "isolated-home", runtime / "isolated-tmp", runtime / "observer-classes", runtime / "observer-resources"]:
    files += list(folder.rglob("*"))
archive_path = WORK / "archives/boss-core-stage03-material-drops-NOT-INSTALLABLE-20261112.zip"
assert not archive_path.exists()
manifest = {}
with zipfile.ZipFile(archive_path, "x", compression=zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(set(files)):
        if not path.is_file():
            continue
        assert not path.is_symlink() and path.resolve().is_relative_to(ROOT.resolve())
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {"bytes": path.stat().st_size, "sha256": digest(path)}
        archive.write(path, name)
    archive.writestr("ARCHIVE-MANIFEST.json", json.dumps(manifest, ensure_ascii=False, indent=2))
with zipfile.ZipFile(archive_path) as archive:
    assert archive.testzip() is None
    for name, row in manifest.items():
        assert hashlib.sha256(archive.read(name)).hexdigest() == row["sha256"]
assert digest(target) == prior_deployment["deployedSha256"]
save(output / "archive-receipt.json", {"archive": str(archive_path), "archiveSha256": digest(archive_path), "archiveBytes": archive_path.stat().st_size, "files": len(manifest), "allFilesReadBackAndHashVerified": True, "physicalRestorePerformed": False, "failures": 0, "deployed": False, "installable": False, "candidateSha256": digest(frozen_jar), "formalDeploymentSha256Unchanged": digest(target), "disposableScratchDirectoriesArchivedBeforeRemoval": [str(runtime / "isolated-home"), str(runtime / "isolated-tmp")]})
print(json.dumps({"candidate": str(frozen_jar), "archive": str(archive_path), "archiveBytes": archive_path.stat().st_size, "files": len(manifest), "runtimeCases": len(result["probe"]["cases"]), "installable": False, "deployed": False, "formalDeploymentUnchanged": True}), flush=True)
