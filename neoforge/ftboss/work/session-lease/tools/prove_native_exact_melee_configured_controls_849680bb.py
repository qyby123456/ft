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
for name in ["reports/native-exact-melee-configured-controls-preparation-849680bb.json", "build/exact-melee-configured-controls-precompile-849680bb/terminal.json"]:
    prepared = load(work / name)
    frozen(prepared)
precompile = prepared
assert precompile["exitCode"] == 0 and not precompile["serverJvmStarted"]
audit = load(work / "reports/native-remaining-variant-gap-audit-3320e085.json")
frozen(audit)
expected_items = audit["groups"]["otherExactOriginalClassesStillNeedActualMatrix"]
runtime_path = work / "reports/native-repair-runtime-exact-melee-configured-controls-849680bb.json"
runtime = load(runtime_path)
frozen(runtime)
attempt = Path(runtime["attempt"])
terminal = load(attempt / "terminal308.json")
assert runtime["exitCode"] == terminal["exitCode"] == runtime["probe"]["failures"] == terminal["probe"]["failures"] == 0
assert terminal["stopSent"] and terminal["completeSave"]
probe = runtime["probe"]
assert not probe["authorityWrittenByReflection"] and not probe["authenticatedMultiplayerAccepted"] and not probe["installable"]
assert [row["id"] for row in probe["cases"]] == paid["actualRegisteredItems"] == [row["id"] for row in expected_items]
for original, row in zip(expected_items, probe["cases"]):
    for key in ["genuineGenerationTwoBeforeControl", "originalForeignCallbackExactlyOnce",
                "originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation", "foreignOriginalMutationRetiresGenuineAuthority",
                "noPrivatePermissionOrAuthorityReissued", "unknownNonNativeMaterialStillOriginalAndAllSlotRulesReject"]:
        assert row[key], (row["id"], key)
    assert row["copyControl"] == (original["class"] == "net.minecraft.world.item.SwordItem")
for name in ["runtime308.log", "live-process308.json", "server/actual-storage-view308.json", "compile-command308.json", "compile308.log", "restart-world-seed.json"]:
    digest(attempt / name)
world = attempt / "server/world"
for path in [world / "level.dat", world / "data/ftboss_native_weapon_wear.dat", *sorted((world / "ftboss-authority").rglob("*.bin")), *sorted((world / "playerdata").glob("*.dat"))]:
    digest(path)
assert digest(root / "work/session-chain/reports/actual-current.json") == "1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243"
digest(Path(__file__))
digest(root / "WRITE_OWNER.claim")
output = work / "reports/native-exact-melee-configured-controls-focused-evidence-849680bb.json"
with output.open("x", encoding="utf-8") as stream:
    json.dump({"failures": 0, "actualRegisteredItems": paid["actualRegisteredItems"], "acceptedRuntime": str(runtime_path),
               "acceptedConfiguredOriginalPaidEvidence": str(paid_path), "genuineGenerationTwoOriginalCallbackControls": True,
               "actualOriginalCallbackExactlyOnceAndExceptionIdentityLiveBeforeMutation": True,
               "actualOriginalMutationWithdrawsGenuineAuthorityAndClosesPrivateScopes": True,
               "copiedSwordAuthorityNeverReissuedAndOriginalAxeWearPreserved": True,
               "unknownNonNativeMeadowMaterialRemainsOriginalAndAllOriginalSlotRulesReject": True,
               "configuredNineteenSignedOrRestartAcceptance": False, "productChangedByRuntimeFixture": False,
               "authorityWrittenByReflection": False, "authenticatedMultiplayerAccepted": False,
               "installable": False, "deployed": False, "inputHashes": hashes}, stream, ensure_ascii=False, indent=2)
print(json.dumps({"report": output.name, "cases": len(probe["cases"]), "hashes": len(hashes), "failures": 0}))
