import hashlib
import json
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}

def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding="utf-8"))

def frozen(data):
    for name, expected in data.get("inputHashes", {}).items():
        assert digest(name) == expected, Path(name).name

paid = load(work / "reports/native-exact-melee-configured-paid-focused-evidence-849680bb.json")
frozen(paid)
assert paid["failures"] == 0 and len(paid["actualRegisteredItems"]) == 19
excluded = []
results = {}
for suffix in ["configured-signed", "defect-semantic-diagnostic", "defect-semantic-consumer-qualified-diagnostic", "defect-binding-diagnostic"]:
    path = work / ("reports/native-repair-runtime-exact-melee-" + suffix + "-849680bb.json")
    data = load(path)
    frozen(data)
    attempt = Path(data["attempt"])
    terminal = load(attempt / "terminal308.json")
    assert data["exitCode"] == terminal["exitCode"] == 0
    assert data["probe"]["failures"] == terminal["probe"]["failures"] == 1
    assert terminal["stopSent"] and terminal["completeSave"]
    for name in ["runtime308.log", "live-process308.json", "server/actual-storage-view308.json", "compile-command308.json", "compile308.log", "restart-world-seed.json"]:
        digest(attempt / name)
    excluded.append(str(path))
    results[suffix] = data
full = results["configured-signed"]["probe"]
assert len(full["cases"]) == 14 and full["cases"][-1]["id"] == "ftboss:defect_claw"
observed = []
for row in full["cases"][:13]:
    assert row["actualOriginalPostCombatSaveAuthenticatedCommitted"] and row["sameLeaseAndGenerationAfterAttack"] and not row["generationRetiredAfterAttack"]
    assert row["healthAfter"] < row["healthBefore"]
    for publication in row["publications"]:
        assert publication["traceComplete"] and publication["session_coverageInstalled"] and not publication["session_traceRevoked"]
        assert publication["session_next"] == publication["originalExpectedReturnedStepCount"]
    observed.append({"id": row["id"], "observedWear": row["wearAfter"], "wholeNineteenAcceptance": False})
failed = full["cases"][-1]
assert failed["healthAfter"] < failed["healthBefore"] and failed["wearBefore"] == 0 and failed["wearAfter"] == 1
assert all(event["resolvedSnapshotValid"] and event["repairEpochPresent"] and event["serverSignaturePresent"] for event in failed["damageEvents"])
publication = failed["publications"][0]
assert publication["prepared_unsupported"] and publication["session_traceRevoked"] and not publication["traceComplete"] and not publication["session_coverageInstalled"]
qualified = results["defect-binding-diagnostic"]
diagnostic = qualified["probe"]["cases"][0]["publications"][0]
assert diagnostic["diagnostic_fieldRegistryInvalid"] is False
prepared = diagnostic["diagnostic_preparedFields"]
assert prepared["unsupported"] == "true" and prepared["nativeBefore"] == prepared["catalogueGeneration"] == prepared["coverage"] == "null"
assert prepared["player"]["retired"] == prepared["carrier"]["retired"] == "true"
assert "ftboss:weapon_fx_hit" in prepared["player"]["originalPersistent"]
producers = diagnostic["diagnostic_producerKindsAndAttempts"]
passive = next(row for row in producers if row["class"].endswith("$PassiveProducer"))
assert "PASSIVE_HIT" in passive["actualQuotedAttempts"]
evolution = next(row for row in producers if row["class"].endswith("$EvolutionProducer"))
assert [row["class"].rsplit("$", 1)[-1] for row in evolution["actualLeaves"]] == ["HealLeaf", "OutcomeTagLeaf", "TimerSaveLeaf"]
cursor = diagnostic["diagnostic_actualSemanticCursors"][0]
assert cursor["program_profile"] == "DEFECT" and cursor["program_unsupported"] == cursor["originalFailure"] == "null"
assert cursor["next"] == cursor["returnedHelpers"] == "1" and cursor["timerReturned"] == "true"
assert cursor["status"] == "ORIGINAL_HELPERS_RETURNED_NOT_FIELD_AUTHORITY"
assert diagnostic["diagnostic_commitTimerSemanticFailure"] == "null"
group = diagnostic["diagnostic_actualSemanticGroups"][0]
assert group["profile"] == "defect" and group["done"] == "false" and group["timers"] == "[]"
assert "Native timer assignment is not the single real ItemStack.set" in diagnostic["diagnostic_firstObservationFailure"]
assert "bindOwnerFieldExpectedSequence" in diagnostic["diagnostic_firstRevocationSite434"]
args = load(work / "build/native-repair-849680bb/complete-arguments316.json")
field_source = next(Path(name) for name in args if name.endswith("OwnerFieldExpectedAuthority.java"))
digest(field_source)
source = field_source.read_text(encoding="utf-8")
quote = source[source.index("static void quoteTimers("):source.index("static void unsupported(")]
assert "originalAttributeHasNoWrites" in quote and "OwnerEvolutionPlanFields" in quote and "var0.unsupported = true" in quote
assert "PassiveProducer" not in quote
runtime_attempt = Path(qualified["attempt"])
proposal = runtime_attempt / "ftboss-unpackaged-proposal"
original_fx = proposal / "net/farmingtales/ftboss/skill/WeaponFx.class"
digest(original_fx)
assert b"ftboss:weapon_fx_hit" in original_fx.read_bytes()
artifact = work / "build/exact-melee-defect-original-writer-diagnosis-849680bb"
artifact.mkdir(exist_ok=False)
javap = Path("C:/Program Files/Zulu/zulu-21/bin/javap.exe")
digest(javap)
log = artifact / "javap-original-WeaponFx.txt"
with log.open("xb") as stream:
    result = subprocess.run([str(javap), "-p", "-s", "-c", "-classpath", str(proposal), "net.farmingtales.ftboss.skill.WeaponFx"], cwd=root, stdout=stream, stderr=subprocess.STDOUT)
assert result.returncode == 0
digest(log)
text = log.read_bytes().decode("gb18030", errors="replace")
assert "LivingIncomingDamageEvent" in text and "ServerPlayer.getPersistentData" in text and "CompoundTag.putLong" in text and "ftboss:weapon_fx_hit" in text
assert "long 6l" in text
for data in results.values():
    original = Path(data["attempt"]) / "ftboss-unpackaged-proposal/net/farmingtales/ftboss/skill/WeaponFx.class"
    assert digest(original) == digest(original_fx)
assert digest(root / "work/session-chain/reports/actual-current.json") == "1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243"
digest(Path(__file__))
digest(root / "WRITE_OWNER.claim")
output = work / "reports/native-exact-melee-defect-original-writer-diagnosis-849680bb.json"
with output.open("x", encoding="utf-8") as stream:
    json.dump({"failures": 0, "diagnosisOnly": True, "excludedRuntimeReceipts": excluded,
               "observedFirstThirteenNotWholeMatrixAcceptance": observed,
               "actualDefectDamageBothPhasesSignedButOriginalSessionRevoked": True,
               "playerAndCarrierDomainRetiredBeforeOriginalFieldCoverageBinding": True,
               "originalIncomingDamageWeaponFxWritesTrackedOwnerPersistentCooldown": True,
               "originalFxCooldownTicksUnchanged": 6,
               "additionalPassiveHitProducerNotRepresentedByExistingQuoteTimersBranches": True,
               "timerOriginalHelpersReturnedButNoFieldAuthorityRatherThanFixedStepCountAssumption": True,
               "soleCauseOrCompleteRootFixClaimed": False,
               "failedWorldNotAnAcceptedRestartSeed": True, "noPermissionOrDomainReset": True,
               "originalFxAndPassiveGameplaySuppressed": False, "productChanged": False,
               "configuredNineteenSignedAccepted": False, "authenticatedMultiplayerAccepted": False,
               "installable": False, "deployed": False, "inputHashes": hashes}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"report": output.name, "excludedActualRuntimes": len(excluded), "hashes": len(hashes), "failures": 0}))
