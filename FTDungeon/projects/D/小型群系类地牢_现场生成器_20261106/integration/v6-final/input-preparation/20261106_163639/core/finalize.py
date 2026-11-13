import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def save(name, payload):
    (ROOT / name).write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    locks = read(ROOT / "inputs/external-lock.json")["files"]
    for record in locks:
        assert sha(Path(record["path"])) == record["sha256"], record["path"]
    tests = read(ROOT / "results/java-summary.json")
    matrix = read(ROOT / "results/seed-ordinal-matrix.json")
    independent = read(ROOT / "results/independent-verification.json")
    assert tests["assertions"] == 79683 and tests["java"] == "17.0.8"
    assert matrix["requests"] == matrix["newPassed"] == 352 and matrix["newFailures"] == 0
    assert matrix["worldSeeds"] == 32 and matrix["oldEnvelopeRefusals"] == 9 and matrix["oldSearchFailures"] == 4
    assert independent["checks"] == 80578 and independent["verifiedPlans"] == 352
    assert "PASS ALL: 94932 assertions; Java=17.0.8" in (ROOT / "logs/baseline-regression.log").read_text(encoding="utf-8-sig")
    assert "PASS ALL Y PLANNER: 47948 assertions; Java=17.0.8" in (ROOT / "logs/previous-y-regression.log").read_text(encoding="utf-8-sig")
    jar = ROOT / "build/ft-biomedungeon-envelope-planner-v3-experimental.jar"
    assert sha(jar) in (ROOT / "logs/reproducible-jar.log").read_text(encoding="utf-8-sig").lower()
    assert "byte-identical352" in (ROOT / "logs/cross-jvm.log").read_text(encoding="utf-8-sig")
    hashes = read(ROOT / "results/java17-plan-hashes.json")
    assert len(hashes) == 352
    for name, digest in hashes.items():
        assert sha(ROOT / "results/plans" / name) == digest
    for path in ROOT.rglob("*.json"):
        if "build" not in path.relative_to(ROOT).parts and path.name not in {"ready.json", "SHA256SUMS.json"}:
            read(path)
    inventory = [{"path": path.relative_to(ROOT).as_posix(), "sha256": sha(path), "bytes": path.stat().st_size}
                 for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"ready.json", "SHA256SUMS.json"}
                 and ("build" not in path.relative_to(ROOT).parts or path == jar)]
    save("SHA256SUMS.json", {"algorithm": "SHA-256", "component": "core-envelope-usability-review", "files": inventory,
                             "excluded": "Unpackaged build/main,test,baseline-test,previous-test and previous-run generated classes/fixtures; production JAR is included"})
    inventory.append({"path": "SHA256SUMS.json", "sha256": sha(ROOT / "SHA256SUMS.json"), "bytes": (ROOT / "SHA256SUMS.json").stat().st_size})
    integrity = read(ROOT / "inputs/integrity-and-evidence.json")
    latest = read(ROOT.parent.parent / "integration/v4-unified/final-evidence.json")
    save("ready.json", {
        "component": "core-envelope-usability-review", "apiVersion": 1, "ready": True,
        "status": "JAVA17_ENVELOPE_PLANNER_V3_READY_NOT_WORLD_ACCEPTED", "algorithm": "ft-core-envelope-splitmix64-dfs-v3",
        "entryPoint": "cn.ft.biomedungeon.core.next.DungeonEnvelopePlanner.plan", "interface": "runtime-consumption.json",
        "sources": [record for record in inventory if record["path"] == "src/DungeonEnvelopePlanner.java"],
        "artifacts": [record for record in inventory if record["path"].endswith(".jar")], "files": inventory,
        "requiredProductionDependency": {"path": str(ROOT.parent / "build/ft-biomedungeon-core-v1.jar"), "sha256": sha(ROOT.parent / "build/ft-biomedungeon-core-v1.jar")},
        "frozenInventoryVerification": integrity["verifiedComponents"], "externalLocksVerified": len(locks), "externalLock": "inputs/external-lock.json",
        "tests": {"command": "& .\\build.ps1", "cwd": str(ROOT), "java": "17.0.8", "newAssertions": tests["assertions"],
                  "baselineRegressionAssertions": 94932, "previousYRegressionAssertions": 47948, "independentPythonChecks": independent["checks"],
                  "worldSeeds": 32, "minimumRootRotationsPerSeed": 4, "edgeSeedsExtendedTo32Ordinals": 8,
                  "totalNineRoomRequests": 352, "newPassed": 352, "newFailed": 0,
                  "oldEnvelopeRefusals": 9, "oldSearchExhaustions": 4, "maximumOperationCellsObserved": independent["maxCells"],
                  "crossJvmReplay": "PASS Java17/25 byte-identical352 Plan JSON hashes", "reproducibleJar": "PASS"},
        "exactOriginalRefusal": {"worldSeed": 267611119455246637, "ordinal": 3, "roomCount": 9, "maxAttempts": 50000,
                                 "oldInnerSpanX": 271, "oldOperationCells": 832939, "newInnerSpanX": 160, "newOperationCells": 668980,
                                 "oldPlanIncludingInstanceAndSeedExactlyReplayed": True, "worldSeedSubstitution": False},
        "budget": {"maxSpanX": 256, "maxSpanY": 96, "maxSpanZ": 256, "maxCells": 1500000, "mayOnlyTighten": True},
        "searchPolicy": "Same-input deterministic5000-attempt DFS passes, shared advancing RNG and maxAttempts total, remaining-port necessary condition; no seed replacement",
        "latestIntegrationScope": {"isolatedNativeMatrixChecks": latest["isolatedFullMatrix"]["attempted"],
                                   "independentlyReplayed": latest["isolatedFullMatrix"]["independentlyReplayed"],
                                   "fullWidthSampledLanes": latest["isolatedFullMatrix"]["fullWidthSampledLanes"],
                                   "fullWidthFailedLanes": latest["isolatedFullMatrix"]["fullWidthFailedLanes"],
                                   "scope": "Read-only existing integration evidence; does not test this new planner artifact or fix S8-07"},
        "limitations": ["Finite352-request geometry matrix is not universal seed completeness", "New planner not integrated or world-tested by core",
                        "Current envelope equation bound to exact frozen runtime Placement formula; do not remove runtime preflight",
                        "Caller supplies complete other-job operation envelopes and world protection; arbitrary world blocks are not queried by pure core",
                        "S8-07 full-width112 failed lanes remain catalog/runtime blocker; no source doors/targets/widths altered",
                        "Original room traversal, native collisions, fluids, payload/restart/recovery need new artifact exact-hash independent reruns"],
        "sourceRoomsModified": False, "frozenComponentsModified": False, "runtimeOrCatalogModified": False,
        "runtimeBudgetWidened": False, "roomCountReduced": False, "worldExecutionByCore": "NOT_RUN",
        "productionPromotion": False, "completeGeneratorReady": False})
    for record in read(ROOT / "ready.json")["files"]:
        assert sha(ROOT / record["path"]) == record["sha256"]
    print("PASS ready: %d local exact hashes; %d external locks rechecked; original failed request fixed without seed substitution" % (len(inventory), len(locks)))
    print("JAR SHA256 " + sha(jar))
    print("ready.json SHA256 " + sha(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
