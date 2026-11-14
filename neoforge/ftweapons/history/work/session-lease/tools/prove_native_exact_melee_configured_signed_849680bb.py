import hashlib
import json
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

paid_path = work / "reports/native-exact-melee-configured-paid-focused-evidence-849680bb.json"
paid = load(paid_path)
frozen(paid)
assert paid["failures"] == 0 and len(paid["actualRegisteredItems"]) == 19
preparation = load(work / "reports/native-exact-melee-configured-signed-preparation-849680bb.json")
frozen(preparation)
audit = load(work / "reports/native-remaining-variant-gap-audit-3320e085.json")
frozen(audit)
expected_items = audit["groups"]["otherExactOriginalClassesStillNeedActualMatrix"]
runtime_path = work / "reports/native-repair-runtime-exact-melee-configured-signed-849680bb.json"
runtime = load(runtime_path)
frozen(runtime)
attempt = Path(runtime["attempt"])
terminal = load(attempt / "terminal308.json")
assert runtime["exitCode"] == terminal["exitCode"] == runtime["probe"]["failures"] == terminal["probe"]["failures"] == 0
assert terminal["stopSent"] and terminal["completeSave"]
probe = runtime["probe"]
assert probe["nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions"]
assert probe["scratchPlayersRemoved"] and not probe["authorityWrittenByReflection"]
assert not probe["authenticatedMultiplayerAccepted"] and not probe["installable"]
assert [row["id"] for row in probe["cases"]] == paid["actualRegisteredItems"] == [row["id"] for row in expected_items]
observed = []
for original, row in zip(expected_items, probe["cases"]):
    assert row["originalConfiguredMaximumDamage"] == original["maxDamage"]
    for key in ["genuineNonNullLeaseAndGenerationBeforeAttack", "actualPaidReloadRetainedOriginalInstalledFunctionReadyConfiguration",
                "originalExactRegisteredClassAndMaximumPreserved", "repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority",
                "foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation", "foreignDirectEntryPreservesOriginalCallbackExceptionIdentity",
                "originalPlayerAttackReturned", "originalMeleeSnapshotPresent", "originalMeleeSnapshotValid", "originalMeleeSnapshotRepairEpochPresent",
                "sameLeaseAndGenerationAfterAttack", "sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope",
                "actualOriginalPostCombatSaveCalled", "actualOriginalPostCombatSaveAuthenticatedCommitted"]:
        assert row[key], (row["id"], key)
    assert not row["generationRetiredAfterAttack"]
    assert row["healthAfter"] < row["healthBefore"] and row["wearBefore"] == 0 and row["wearAfter"] > row["wearBefore"]
    events = row["damageEvents"]
    assert len(events) == 2
    for event in events:
        for key in ["resolvedSnapshotPresent", "resolvedSnapshotValid", "repairEpochPresent", "serverSignaturePresent"]:
            assert event[key], (row["id"], key)
        assert event.get("observationFailure") is None
    assert row["publications"]
    sessions = []
    for publication in row["publications"]:
        for key in ["publicationPresent", "preparedCommitted", "originalSequencePresent", "preparationPresent", "prepared_sealed", "prepared_bound",
                    "sessionPresent", "traceComplete", "session_coverageInstalled"]:
            assert publication[key], (row["id"], key)
        assert not publication["prepared_unsupported"] and not publication["prepared_retired"] and not publication["session_traceRevoked"]
        assert publication.get("firstObservationFailure") is None
        assert publication["originalStaticExpectedPlanSize"] == publication["actualStaticSessionStepCount"]
        returns = publication["originalExpectedReturnedStepCount"]
        assert publication["session_next"] == returns and returns > 0
        dynamic = publication["originalDynamicSemanticBookPresent"]
        if dynamic:
            assert publication["exactOriginalNativeBindingAndTargetReturnIdentityCoverage"]
            assert publication["originalQuotedNativePutCount"] == publication["originalReturnedNativeLeafCount"]
            assert publication["originalQuotedTargetWriteCount"] == publication["originalReturnedTargetLeafCount"]
            operations = publication["actualOriginalMaterializedNativeOperations"]
            assert len(operations) == publication["originalQuotedNativePutCount"]
            for operation in operations:
                assert operation["kind"] == "NATIVE_PUT" and operation["exactQuotedBindingAndReturnedLeafIdentity"]
            calculated = len(operations)
            groups = publication["actualOriginalCompletedSemanticGroups"]
            for group in groups:
                assert group["done"] and group["exactQuotedKeyCoverage"]
                assert group["quotedTimerWrites"] == group["returnedTimers"]
                assert group["acquired"] == group["rooted"]
                if not group["acquired"]:
                    assert group["returnedRanged"] == 0
                calculated += group["returnedTimers"] + group["returnedRanged"] + int(group["acquired"]) + int(group["rooted"])
            assert calculated == returns
        else:
            assert publication["exactExpectedTerminalCursorIdentity"] and publication["exactOriginalStaticStepIdentityAndReturnCoverage"]
            assert returns == publication["originalStaticExpectedPlanSize"]
        sessions.append({"dynamicOriginalBook": dynamic, "exactOriginalReturnedSteps": returns})
    observed.append({"id": row["id"], "originalClass": original["class"], "originalMaximumDamage": original["maxDamage"],
                     "actualWearBefore": row["wearBefore"], "actualWearAfter": row["wearAfter"], "completeOriginalSessions": sessions})
for name in ["runtime308.log", "live-process308.json", "server/actual-storage-view308.json", "compile-command308.json", "compile308.log", "restart-world-seed.json"]:
    digest(attempt / name)
world = attempt / "server/world"
for path in [world / "level.dat", world / "data/ftboss_native_weapon_wear.dat", *sorted((world / "ftboss-authority").rglob("*.bin")), *sorted((world / "playerdata").glob("*.dat"))]:
    digest(path)
source_delta = load(work / "reports/native-native-reference-id-source-delta-849680bb.json")
frozen(source_delta)
assert source_delta["failures"] == source_delta["newProductClasses"] == 0
assert source_delta["classCount"] == 1381 and source_delta["guardCount"] == 208
assert digest(root / "work/session-chain/reports/actual-current.json") == "1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243"
digest(Path(__file__))
digest(root / "WRITE_OWNER.claim")
output = work / "reports/native-exact-melee-configured-signed-focused-evidence-849680bb.json"
with output.open("x", encoding="utf-8") as stream:
    json.dump({"failures": 0, "actualRegisteredItems": paid["actualRegisteredItems"], "acceptedRuntime": str(runtime_path),
               "acceptedConfiguredOriginalPaidEvidence": str(paid_path), "actualGenuineGenerationTwoFullSignedSessions": observed,
               "exactOriginalStaticOrDynamicPlanCoverageNotArtificialFixedFour": True,
               "newJvmRestartAcceptedByThisReceipt": False, "foreignMutationControlsAcceptedByThisReceipt": False,
               "productChangedByRuntimeFixture": False, "authorityWrittenByReflection": False,
               "authenticatedMultiplayerAccepted": False, "installable": False, "deployed": False, "inputHashes": hashes},
              stream, ensure_ascii=False, indent=2)
print(json.dumps({"report": output.name, "cases": len(observed), "hashes": len(hashes), "failures": 0}))
