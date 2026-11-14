import hashlib
import json
import os
import re
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}

def digest(path):
    path = Path(path)
    key = str(path.resolve())
    if key not in hashes:
        checksum = hashlib.sha256()
        with path.open("rb") as stream:
            for block in iter(lambda: stream.read(1024 * 1024), b""):
                checksum.update(block)
        hashes[key] = checksum.hexdigest()
    return hashes[key]

def load(path):
    digest(path)
    return json.loads(Path(path).read_text(encoding="utf-8"))

def frozen(data):
    for name, expected in data.get("inputHashes", {}).items():
        assert digest(name) == expected, Path(name).name

parent_audit_path = work / "reports/native-exact-melee-configured-final-process-check-849680bb.json"
parent = load(parent_audit_path)
assert parent["failures"] == 0 and parent["actualRuntimeCount"] == 39
candidate_path = work / "reports/native-repair-ranged-use-candidate-d71b9448.json"
candidate = load(candidate_path)
frozen(candidate)
assert candidate["classCount"] == 1381 and candidate["effectiveSourceCount"] == 716 and candidate["guardCount"] == 208
assert load(Path(candidate["attempt"]) / "asm.json")["failures"] == 0
build_tags = ["2da17c25", "5fdaea2d", "a4974954", "b80915d4", "800b39d9", "837686c4", "58f5ec6e", "75b35398", "0ced186d", "6e1c380f", "d71b9448"]
artifact_roots = [work / "build/native-repair-86e6e3ee"]
assert load(artifact_roots[0] / "compile-terminal.json")["exitCode"] == 1
for tag in build_tags:
    path = work / ("reports/native-repair-ranged-use-candidate-" + tag + ".json")
    receipt = load(path)
    attempt = Path(receipt["attempt"])
    assert attempt.name == "native-repair-" + tag and load(attempt / "asm.json")["failures"] == 0
    artifact_roots.extend([path, attempt])
runtime_paths = [work / ("reports/native-repair-runtime-exact-melee-defect-fx-diagnostic-" + tag + ".json")
                 for tag in ["5fdaea2d", "a4974954", "b80915d4", "800b39d9", "58f5ec6e", "75b35398", "0ced186d", "6e1c380f"]]
runtime_paths += [work / ("reports/native-repair-runtime-fx-owner-" + mode + "-d71b9448.json") for mode in ["positive", "controls"]]
rows = []
external = []
results = []
for index, path in enumerate(runtime_paths):
    data = load(path)
    frozen(data)
    attempt = Path(data["attempt"])
    terminal = load(attempt / "terminal308.json")
    assert data["exitCode"] == terminal["exitCode"] == 0 and terminal["completeSave"] and terminal["stopSent"]
    wanted = 1 if index < 8 else 0
    assert data["probe"]["failures"] == terminal["probe"]["failures"] == wanted
    text = (attempt / "runtime308.log").read_text(encoding="utf-8", errors="replace")
    assert "Stopping server" in text and "All dimensions are saved" in text
    digest(attempt / "runtime308.log")
    pid = load(attempt / "live-process308.json")["pid"]
    rows.append({"receipt": str(path), "attempt": str(attempt), "pid": pid, "observerFailures": wanted, "normalCompleteSave": True, "javaExitCode": 0})
    results.append(data)
    artifact_roots.extend([path, attempt])
    for name in ["mods", "kubejs", "recipe-inputs"]:
        directory = attempt / "server" / name
        if directory.is_dir():
            external.append(str(directory))
positive, controls = results[-2:]
assert len(positive["probe"]["cases"]) == len(controls["probe"]["cases"]) == 1
row = positive["probe"]["cases"][0]
assert row["id"] == "ftboss:defect_claw" and row["originalFxCooldownOwnerTransitionAtBothActualSignedDamagePhases"]
assert row["genuineNonNullLeaseAndGenerationBeforeAttack"] and row["wearBefore"] == 0 and row["wearAfter"] == 1
assert row["healthAfter"] < row["healthBefore"] and len(row["damageEvents"]) == 2
for event in row["damageEvents"]:
    for key in ["resolvedSnapshotValid", "repairEpochPresent", "serverSignaturePresent", "diagnostic_ownerDomainPresentAtActualDamagePhase"]:
        assert event[key]
    assert event["diagnostic_ownerDomainRetiredAtActualDamagePhase"] is False
    assert event["diagnostic_originalFxCooldownAtActualDamagePhase"] == event["diagnostic_actualDamageTick"]
assert all(entry["nativeBeforePresent"] for entry in row["publications"])
assert any(entry["remainingPreparedUnsupported"] and not entry["actualTraceComplete"] for entry in row["publications"])
assert controls["probe"]["foreignFxExceptionIdentityExactlyOnceAndNoOwnerDomainReplacement"]
assert controls["probe"]["externalSameFxKeyMutationRetiresOriginalDomainAndNeverReissuesSuccessor"]
control = controls["probe"]["cases"][0]
assert control["genuineGenerationTwoBeforeControl"] and control["noPrivatePermissionOrAuthorityReissued"] and control["originalForeignCallbackExactlyOnce"]
for result in [positive, controls]:
    assert not result["probe"]["authorityWrittenByReflection"] and not result["probe"]["authenticatedMultiplayerAccepted"] and not result["probe"]["installable"]
previous_attempt = work / "build/native-repair-849680bb"
new_attempt = Path(candidate["attempt"])
previous_classes = {path.relative_to(previous_attempt / "complete-classes").as_posix(): path for path in (previous_attempt / "complete-classes").rglob("*.class")}
new_classes = {path.relative_to(new_attempt / "complete-classes").as_posix(): path for path in (new_attempt / "complete-classes").rglob("*.class")}
assert set(previous_classes) == set(new_classes) and len(new_classes) == 1381
changed = []
for name, path in new_classes.items():
    if digest(path) != digest(previous_classes[name]):
        assert re.fullmatch(r"net/farmingtales/ftboss/(?:item/(?:FoodWeaponSourceLedger|OwnerFieldExpectedAuthority|NativeCapabilityInstallationProof170)(?:\$[^/]*)?|mixin/OwnerFullRepairCompoundTagMixin)\.class", name), name
        changed.append(name)
def effective_sources(attempt):
    arguments = load(attempt / "complete-arguments316.json")
    sources = {}
    for name in arguments:
        path = Path(name)
        if path.suffix == ".java":
            text = path.read_text(encoding="utf-8")
            declaration = re.search(r"(?m)^package\s+([\w.]+)\s*;", text)
            assert declaration is not None, path.name
            key = declaration.group(1) + "." + path.name
            assert key not in sources
            sources[key] = (path, text)
    return sources
old_sources = effective_sources(previous_attempt)
new_sources = effective_sources(new_attempt)
assert set(old_sources) == set(new_sources) and len(new_sources) == 716
source_changes = []
for name, (path, text) in new_sources.items():
    old_path, old_text = old_sources[name]
    digest(path)
    digest(old_path)
    if text != old_text:
        assert name in ["net.farmingtales.ftboss.item.FoodWeaponSourceLedger.java", "net.farmingtales.ftboss.item.OwnerFieldExpectedAuthority.java",
                        "net.farmingtales.ftboss.item.NativeCapabilityInstallationProof170.java", "net.farmingtales.ftboss.mixin.OwnerFullRepairCompoundTagMixin.java"], name
        source_changes.append(name)
    assert "NativeFxDiagnostic" not in text and "NativeFxNoticeDiagnostic" not in text
assert len(source_changes) == 4
assert old_sources["net.farmingtales.ftboss.item.FoodWeaponBossPassive.java"][1] == new_sources["net.farmingtales.ftboss.item.FoodWeaponBossPassive.java"][1]
assert old_sources["net.farmingtales.ftboss.item.SwordCavityBuild.java"][1] == new_sources["net.farmingtales.ftboss.item.SwordCavityBuild.java"][1]
for name, value in candidate["additionalProductionResources"].items():
    old = load(work / "reports/native-repair-ranged-use-candidate-849680bb.json")["additionalProductionResources"][name]
    assert digest(value) == digest(old)
assert digest(root / "work/session-chain/reports/actual-current.json") == "1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243"
owned = str(work).replace("'", "''")
command = "$rows=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') -and $_.CommandLine -like '*" + owned + "*' }); ConvertTo-Json -InputObject @($rows | Select-Object ProcessId,Name) -Compress"
process = subprocess.run(["powershell", "-NoProfile", "-Command", command], cwd=root, capture_output=True, text=True)
assert process.returncode == 0
assert not [row for row in json.loads(process.stdout or "[]") if int(row["ProcessId"]) != os.getpid()]
digest(Path(__file__))
digest(root / "WRITE_OWNER.claim")
output = work / "reports/native-fx-owner-transition-batch-evidence-d71b9448.json"
with output.open("x", encoding="utf-8") as stream:
    json.dump({"failures": 0, "batchId": "configured-nineteen-defect-fx-owner-transition", "scope": "original incoming-damage FX cooldown owner-domain transition only",
               "acceptedDirectedRuntimeReceipts": [str(path) for path in runtime_paths[-2:]], "excludedDiagnosticRuntimeReceipts": [str(path) for path in runtime_paths[:8]],
               "actualOriginalFxCooldownPreserved": 6, "genuinePaidGenerationTwoAndBothActualDamagePhasesSigned": True,
               "exactOriginalPutLongReturnOneMutationNoticeAndWholeFieldSuccessor": True,
               "externalSameKeyAndDirectCallbackCannotCreateOrReissueOwnerDomain": True,
               "changedClassNames": changed, "changedEffectiveJavaSources": source_changes, "unchangedEffectiveJavaSources": 716 - len(source_changes),
               "classCount": 1381, "guardCount": 208, "newProductClasses": 0, "asmFailures": 0,
               "reusedPriorTerminalProcessAuditWithoutRerunningFullTests": str(parent_audit_path), "newRuntimeRows": rows,
               "cumulativeActualRuntimes": 49, "cumulativeObserverPasses": 18, "cumulativePreservedFailures": 31,
               "allNewRuntimesNormalCompleteSavedJavaZero": True, "ownedProcessesRemaining": False,
               "separateOriginalPassiveProducerFieldPlanGapStillOpen": True, "wholeNineteenSignedOrRestartAccepted": False,
               "productDiagnosticLoggingRemaining": False, "authenticatedMultiplayerAccepted": False, "installable": False, "deployed": False,
               "inputHashes": hashes}, stream, ensure_ascii=False, indent=2)
artifact_roots.extend([output, root / "work/session-lease/CURRENT_INCREMENTAL_BATCH.json", root / "work/session-lease/MATERIAL_RANGED_NATIVE_PROGRESS.md", Path(__file__), work / "tools/archive_incremental_native_fx_batch.py"])
for directory, pattern in [("tests", "*fx*"), ("tests", "native-fx-owner-*"), ("tools", "*fx*"), ("reports", "*fx*")]:
    artifact_roots.extend((work / directory).glob(pattern))
plan_path = work / "reports/native-fx-owner-transition-archive-plan-d71b9448.json"
with plan_path.open("x", encoding="utf-8") as stream:
    json.dump({"batchId": "configured-nineteen-defect-fx-owner-transition", "batchComplete": True,
               "archiveDirectory": "work/session-lease/archives/configured-nineteen-fx-owner-transition-d71b9448",
               "terminalRuntimeReceipts": [str(path.relative_to(root)) for path in runtime_paths],
               "artifactRoots": sorted({str(path.resolve()) for path in artifact_roots}),
               "externalDependencyDirectories": sorted(set(external)), "remainingNineteenSignedAndRestartAccepted": False,
               "preserveAllReferencedOriginalFiles": True, "singleHeavyJvmPolicy": True}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"evidence": output.name, "archivePlan": plan_path.name, "newActualRuntimes": 10, "cumulativeActualRuntimes": 49,
                  "unchangedEffectiveSources": 716 - len(source_changes), "hashes": len(hashes), "failures": 0}))
