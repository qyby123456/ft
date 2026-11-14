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
BUILD = WORK / "build/boss-status-immunity-v1"
parser = argparse.ArgumentParser()
parser.add_argument("candidate_runtime", type=Path)
arguments = parser.parse_args()


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8"))


def digest(path):
    checksum = hashlib.sha256()
    with Path(path).open("rb") as stream:
        for block in iter(lambda: stream.read(1048576), b""):
            checksum.update(block)
    return checksum.hexdigest()


def save(path, data):
    with Path(path).open("x", encoding="utf-8") as stream:
        json.dump(data, stream, ensure_ascii=False, indent=2)


assert load(ROOT / "WRITE_OWNER.claim")["threadId"] == "01a1c43e-ae80-7390-b604-b43b4ab53f2f"
baseline_path = WORK / "reports/manor-status-baseline-32ac9efe.json"
candidate_path = arguments.candidate_runtime.resolve()
baseline = load(baseline_path)
candidate = load(candidate_path)
for result in [baseline, candidate]:
    terminal = result["terminal"]
    assert terminal["exitCode"] == 0 and terminal["stopSent"] and terminal["completeSave"] and terminal["inputHashesUnchanged"]
    assert terminal["supervisionFailure"] is None and result["probe"]["failures"] == 0
    for name, wanted in result["inputHashes"].items():
        assert digest(name) == wanted, name
assert baseline["baselineGapObserved"] and len(baseline["probe"]["cases"]) == 24
assert candidate["manorStatusFeatureAccepted"] and len(candidate["probe"]["cases"]) == 40
assert candidate["probe"]["actualNaturalPostTicks"] >= 20
proof = load(BUILD / "proof-terminal.json")
assert proof["proofExitCode"] == 0
assert "onlyCanBeAffectedReturnGuardAdded=true" in (BUILD / "proof-run.log").read_text(encoding="utf-8")
built = load(BUILD / "candidate-build-receipt.json")
jar = Path(built["jar"])
assert digest(jar) == built["jarSha256"] and candidate["inputHashes"][str(jar)] == built["jarSha256"]
for name, wanted in built["inputHashes"].items():
    assert digest(name) == wanted
assert built["coreClasses"] == 1432 and built["allOtherClassAndResourceBytesUnchanged"] and not built["stage03ChangesIncluded"]
previous_path = WORK / "releases/boss-core-stage02-absorption-20261112/deployment-receipt.json"
previous = load(previous_path)
target = Path(previous["target"]).resolve()
expected = Path("C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar").resolve()
assert target == expected and target.parent == expected.parent
assert digest(target) == previous["deployedSha256"] == built["baseJarSha256"], "Formal target drift; do not overwrite"
assert not list(target.parent.glob(target.name + ".stage*.tmp"))
baseline_config = load(ROOT / "baseline.json")
forge_before = digest(baseline_config["forge"]["live"])
release = WORK / "releases/boss-core-stage04-status-immunity-20261112"
assert not release.exists()
release.mkdir(parents=True)
backup = release / "rollback-stage02-qys-bosses-1.21.1.jar"
shutil.copy2(target, backup)
assert digest(backup) == previous["deployedSha256"]
released = release / "ftboss-boss-core-1.21.1-stage04.jar"
shutil.copyfile(jar, released)
assert digest(released) == built["jarSha256"]
verification = {"scope": "Original Manor Boss status immunity for HealBan and ShadowErosion only", "base": "Verified deployed stage02; stage03 material reward candidate intentionally excluded", "baselineActualMissingBehaviorCases": 24, "candidatePassedChecks": 40, "naturalEntityTicks": candidate["probe"]["actualNaturalPostTicks"], "allTwelveBossVariantsCovered": True, "ordinaryEntityControlsPreserved": True, "nativeEntityJoinAndTickPurgeAccepted": True, "persistedStatusSetup": "Native MobEffectInstance.save and full entity load with original UUID/health/age/AI preserved; no private state or authority writes", "asm": (BUILD / "proof-run.log").read_text(encoding="utf-8").strip(), "jarDelta": {"added": built["addedEntries"], "changed": built["changedEntries"], "removed": [], "allOtherBytesUnchanged": True}, "incrementalFeatureAccepted": True, "fullBossInstallable": False, "clientRenderingAccepted": False, "authenticatedMultiplayerAccepted": False, "fullCombatAccepted": False, "stage03PositiveKillRewardsAccepted": False, "weaponFunctionalDevelopmentPaused": True}
save(release / "feature-verification.json", verification)
restore = """# Stage04 Manor status immunity: incremental deployment only\n\nThis restores original immunity to ftboss HealBan and ShadowErosion on Manor EasterBossEntity. It is not a fully accepted Boss migration. Client rendering, authenticated multiplayer, complete battle behavior and the stage03 material-reward positive player kill path remain unaccepted. The prior stage02 material-drop omission remains in this deployed branch; stage03's correction is isolated and not silently promoted.\n\nRollback: stop the relevant NeoForge game/server. Verify the deployed SHA and exact target from deployment-receipt.json. Copy rollback-stage02-qys-bosses-1.21.1.jar to that target, then verify its SHA equals backupSha256. Never modify Forge or historical D.\n\nRebuild: use the frozen stage02 Boss JAR and genuine SDK from build/boss-status-immunity-v1/compile-context.json. Compile only ManorStatusImmunity.java. Run ManorStatusBoundaryProof on original EasterBossEntity.class to add the original allowed(boolean,LivingEntity,MobEffectInstance) guard at three native return sites. All 295 original entity methods must match after stripping only those guards; 303 related methods pass BasicVerifier. The frozen original Forge has four return guard sites due to its additional pre-existing predicate branch; the port retains the actual NF control flow. Package only the new guard class and the guarded original entity class with package_manor_status_feature.py. All other JAR classes and resources must be byte-identical to stage02.\n\nBaseline real JVM: 24 checks show all twelve variants accepted the forbidden effects. Candidate real JVM: 40 checks show actual admission rejection, native NBT-deserialized status cleanup on real entity join and after 20 natural entity ticks, and ordinary living controls/unrelated Boss effects preserved. Both JVMs stop normally and save all dimensions.\n\nArchive includes source, compile context, linker/ASM test, original method evidence, runtime logs and manifests. Historical SDK/Forge snapshot and prior source paths remain read-only external prerequisites with hashes; no complete game distribution or paid weapon world is copied. The full paused weapon checkpoint remains unchanged.\n\nPrior scratch deletion was blocked by automatic approval review. No bypass or repeat deletion is attempted; new isolated-home/isolated-tmp files are archived and retained, clearly distinct from a running JVM or half-written deployment.\n"""
(release / "RESTORE.md").write_text(restore, encoding="utf-8")
save(release / "predeployment.json", {"target": str(target), "priorSha256": previous["deployedSha256"], "candidateSha256": digest(released), "rollback": str(backup), "rollbackSha256": digest(backup), "baselineReceiptSha256": digest(baseline_path), "candidateReceiptSha256": digest(candidate_path), "incrementalFeatureVerified": True, "fullBossInstallable": False})
assert digest(target) == previous["deployedSha256"]
temporary = target.with_name(target.name + ".stage04-" + uuid.uuid4().hex[:8] + ".tmp")
assert temporary.resolve().parent == expected.parent and not temporary.exists()
shutil.copyfile(released, temporary)
assert digest(temporary) == built["jarSha256"]
os.replace(temporary, target)
assert digest(target) == built["jarSha256"] and digest(baseline_config["forge"]["live"]) == forge_before
deployment = {"deployedAt": datetime.datetime.now(datetime.timezone.utc).isoformat(), "target": str(target), "deployedSha256": digest(target), "backup": str(backup), "backupSha256": digest(backup), "backupHashVerifiedBeforeDeployment": True, "previousDeployment": str(previous_path), "feature": verification["scope"], "incrementalFeatureAccepted": True, "fullBossInstallable": False, "fullBossMigrationComplete": False, "clientRenderingAccepted": False, "authenticatedMultiplayerAccepted": False, "completeBattleAccepted": False, "formalInstanceLaunched": False, "stage03CandidateIncluded": False, "stage03MaterialDropCorrectionStillPending": True, "weaponCandidateValidatedOrDeployed": False, "weaponFunctionalDevelopmentPaused": True, "duplicateDependencyModsChanged": False, "forgeModified": False, "historicalDModified": False, "inputHashes": {str(path): digest(path) for path in [Path(__file__), baseline_path, candidate_path, BUILD / "candidate-build-receipt.json", BUILD / "compile-context.json", BUILD / "proof-terminal.json", WORK / "reports/pre-stage04-audit-20261112.json", release / "feature-verification.json"]}}
save(release / "deployment-receipt.json", deployment)
files = list(release.rglob("*")) + list(BUILD.rglob("*")) + [WORK / "STAGE04_MANOR_STATUS_IMMUNITY.md", WORK / "reports/pre-stage04-audit-20261112.json", WORK / "CURRENT_BOSS_BATCH.json", ROOT / "AGENTS.md", ROOT / "WRITE_OWNER.json", ROOT / "WRITE_OWNER.claim", WORK / "boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/ManorStatusImmunity.java"]
files += [WORK / "tools" / name for name in ["compile_manor_status_feature.py", "run_manor_status_probe.py", "run_manor_status_candidate.py", "package_manor_status_feature.py", Path(__file__).name]]
files += [WORK / "tests" / name for name in ["ManorStatusRuntimeProbe.java", "CandidateManorStatusRuntimeProbe.java", "ManorStatusBoundaryProof.java"]]
files += list((WORK / "build/stage04-original-mechanism-inspection").rglob("*"))
scratch = []
for receipt_path, result in [(baseline_path, baseline), (candidate_path, candidate)]:
    runtime = Path(result["attempt"])
    files.append(receipt_path)
    for name in ["runtime.log", "terminal.json", "runtime-command.json", "inputs-before.json", "compile-command.json", "compile-terminal.json", "compile.log", "live-process.json", "server/actual-manor-status-probe.json"]:
        files.append(runtime / name)
    for name in ["isolated-home", "isolated-tmp", "observer-classes", "observer-resources"]:
        folder = runtime / name
        files += list(folder.rglob("*"))
        if name.startswith("isolated-"):
            scratch.append(str(folder))
for stage in ["boss-core-stage01-20261112", "boss-core-stage02-absorption-20261112"]:
    files += [WORK / "releases" / stage / name for name in ["deployment-receipt.json", "archive-receipt.json", "RESTORE.md"]]
archive_path = WORK / "archives/boss-core-stage04-status-immunity-20261112.zip"
assert not archive_path.exists()
manifest = {}
with zipfile.ZipFile(archive_path, "x", compression=zipfile.ZIP_DEFLATED) as archive:
    for path in sorted(set(files)):
        if not path.is_file():
            continue
        assert path.resolve().is_relative_to(ROOT.resolve()) and not path.is_symlink()
        name = path.relative_to(ROOT).as_posix()
        manifest[name] = {"bytes": path.stat().st_size, "sha256": digest(path)}
        archive.write(path, name)
    archive.writestr("ARCHIVE-MANIFEST.json", json.dumps(manifest, ensure_ascii=False, indent=2))
with zipfile.ZipFile(archive_path) as archive:
    assert archive.testzip() is None
    for name, row in manifest.items():
        assert hashlib.sha256(archive.read(name)).hexdigest() == row["sha256"]
assert digest(target) == built["jarSha256"]
save(release / "archive-receipt.json", {"archive": str(archive_path), "archiveSha256": digest(archive_path), "archiveBytes": archive_path.stat().st_size, "files": len(manifest), "allFilesReadBackAndHashVerified": True, "physicalRestorePerformed": False, "failures": 0, "deployedIncrementSha256": digest(target), "fullBossInstallable": False, "scratchArchivedAndRetained": scratch, "scratchDeletionNotAttemptedBecausePriorPolicyRejection": True})
print(json.dumps({"deploymentReceipt": str(release / "deployment-receipt.json"), "archive": str(archive_path), "archiveBytes": archive_path.stat().st_size, "files": len(manifest), "baselineCases": 24, "candidateCases": 40, "deployedSha256": digest(target), "fullBossInstallable": False}), flush=True)
