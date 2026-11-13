import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def save(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    result = load(ROOT / "result.json")
    check_files = load(ROOT / "input-lock.json")["files"]
    for record in check_files:
        assert sha(Path(record["path"])) == record["sha256"], record["path"]
    assert result["pythonChecks"] == 31179
    assert result["realEndpointOccurrences"] == 4400 and result["actualReportedEndpointCoordinatesChecked"] == 2200
    assert result["selectedS8FractionalEndpointOccurrences"] == 0
    assert result["nativeCompletedByRuntime"] == 1100 and result["requiredChecks"] == 2200
    assert not result["all36IndependentCasesCertified"] and not result["productionPromotion"]
    assert "PASS 52808 Java assertions" in (ROOT / "logs/java17.log").read_text(encoding="utf-8-sig")
    bindings = load(ROOT / "evidence/jar-bindings.json")
    assert sha(Path(bindings["artifact"])) == bindings["sha256"] == result["runtimeJarSha256"]
    bytecode = (ROOT / "evidence/NativeRouteAudit-javap.txt").read_text(encoding="utf-8-sig")
    center = bytecode.split("private net.minecraft.world.phys.Vec3 center", 1)[1].split("private boolean supported", 1)[0]
    assert "double -0.5d" in center and "double 0.5d" in center and "double 96.0d" in center
    assert "dsub" in center and "dadd" in center
    placement = (ROOT / "evidence/Placement-javap.txt").read_text(encoding="utf-8-sig")
    assert "DungeonGeometry.blockPosition" in placement
    for path in ROOT.rglob("*.json"):
        if path.name not in {"ready.json", "SHA256SUMS.json"}:
            load(path)
    inventory = [{"path": path.relative_to(ROOT).as_posix(), "sha256": sha(path), "bytes": path.stat().st_size}
                 for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"ready.json", "SHA256SUMS.json"}]
    save("SHA256SUMS.json", {"component": "core-route-coordinate-review", "algorithm": "SHA-256", "files": inventory})
    inventory.append({"path": "SHA256SUMS.json", "sha256": sha(ROOT / "SHA256SUMS.json"), "bytes": (ROOT / "SHA256SUMS.json").stat().st_size})
    save("ready.json", {
        "component": "core-route-coordinate-review", "apiVersion": 1, "ready": True,
        "status": "CURRENT_REAL_ENDPOINT_COORDINATE_CONTRACT_PROVED_NOT_NATIVE_RELEASE",
        "runtimeJarSha256": result["runtimeJarSha256"], "runtimeArtifact": bindings["artifact"],
        "contract": "coordinate-contract.json", "findings": "ISSUES.json", "files": inventory,
        "externalInputLockCount": len(check_files), "externalInputLock": "input-lock.json",
        "tests": {"command": "& .\\run.ps1; python -B finalize.py", "cwd": str(ROOT), "java": "17.0.8",
                  "pythonChecks": 31179, "javaAssertions": 52808, "realSelectedEndpointOccurrences": 4400,
                  "actualFrozenReportEndpointCoordinates": 2200, "runtimeBytecode": "EXACT_FROZEN_JAR_JAVAP_REVIEWED",
                  "currentFractionalSelectedEndpoints": 0, "nativeWorldExecutionByReview": "NOT_RUN"},
        "coordinateConclusion": "R(q)*(0.5,0,0.5) matches original Node half-grid center and blockPosition compensation for all actual selected integer endpoints; no current half-block bug proved",
        "fractionLimit": "General non-half-grid equivalence disproved by explicitly synthetic examples; keep original targets and version/domain-check semantics",
        "nativeReleaseGates": {"producerCompletedChecks": 1100, "requiredChecks": 2200, "all36IndependentCasesCertified": False,
                               "nativeUIAndDoorInteractions": "NOT_RUN", "globalEntranceReturnRouteCertificate": "NOT_RUN",
                               "fullDeclaredWidthStairClearance": "NOT_CERTIFIED", "forcedKillPowerLoss": "NOT_RUN"},
        "sourceTargetsMoved": False, "runtimeModified": False, "catalogModified": False, "frozenCoreVersionsModified": False,
        "productionPromotion": False, "completeGeneratorReady": False})
    for record in load(ROOT / "ready.json")["files"]:
        assert sha(ROOT / record["path"]) == record["sha256"]
    print("PASS ready: %d exact local hashes; %d external locks verified; native release stays false" % (len(inventory), len(check_files)))
    print("ready.json SHA256 " + sha(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
