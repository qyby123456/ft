import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    lock = load(ROOT / "inputs/input-lock.json")
    engine = Path(lock["runtimeSource"])
    version = re.search(r'static final String VERSION\s*=\s*"([^"]+)"', engine.read_text(encoding="utf-8")).group(1)
    assert version == lock["runtimeVersion"], "VERSION changed: rerun prepare, Java matrix and independent verification"
    assert sha(Path(lock["manifest"])) == lock["manifestSha256"], "Manifest changed: full rerun required"
    for record in lock["frozenInventory"] + lock["sourceResources"]:
        assert sha(Path(record["path"])) == record["sha256"], record["path"]
    baseline = ROOT.parent / "build/ft-biomedungeon-core-v1.jar"
    assert sha(baseline) == "764427a79d9a5330a8d82ac50a2924679bb8e702d7482e75ae254ba2aca203d0"
    matrix = load(ROOT / "results/matrix.json")
    independent = load(ROOT / "results/independent-verification.json")
    assert matrix["passed"] == independent["verifiedPlans"] == 129 and matrix["failed"] == 0
    assert independent["runtimeVersion"] == version
    (ROOT / "inputs/UnifiedEngine-at-end.java").write_bytes(engine.read_bytes())
    runtime = {"runtimeVersionAtStart": lock["runtimeVersion"], "runtimeVersionAtEnd": version,
               "runtimeSourceSha256AtStart": lock["runtimeSourceSha256AtStart"], "runtimeSourceSha256AtEnd": sha(engine),
               "versionUnchanged": True, "activeRuntimeIsFrozen": False,
               "finalRerunRequiredIfVersionManifestOrResourcesChange": True,
               "testedAnchor": [1000, 200, 1000], "minY": 164, "maxYExclusive": 316,
               "runtimeMaxBuildHeightAssumption": 320, "maxAttempts": 50000,
               "dimension": "minecraft:overworld", "biome": "s8", "roomCount": 9,
               "occupiedBoxes": [], "worldTests": "NOT_RUN"}
    (ROOT / "results/runtime-binding.json").write_text(json.dumps(runtime, indent=2) + "\n", encoding="utf-8")
    external = [{"path": str(baseline), "sha256": sha(baseline)},
                {"path": str(ROOT.parent / "next-planner-y/verify_outputs.py"),
                 "sha256": sha(ROOT.parent / "next-planner-y/verify_outputs.py")}]
    (ROOT / "inputs/additional-external-lock.json").write_text(json.dumps({"files": external}, indent=2) + "\n", encoding="utf-8")
    files = [{"path": path.relative_to(ROOT).as_posix(), "sha256": sha(path)}
             for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name != "ready.json"]
    result = {"status": "PASS_OFFLINE_V5_EXACT_INPUT_EVIDENCE_ONLY", "runtimeVersion": version,
              "requests": 129, "passed": 129, "failed": 0,
              "javaAssertions": load(ROOT / "results/java-summary.json")["assertions"],
              "pythonAssertions": independent["checks"], "scope": "eight originals and one unaccepted derived template",
              "productionCoreSourceChanged": False, "newProductionJar": False,
              "frozenPlannerJarSha256": lock["frozenPlannerJarSha256"],
              "frozenPreviousFilesVerified": len(lock["frozenInventory"]) - 1,
              "worldAcceptance": "NOT_RUN", "productionPromotion": False,
              "finalRerunRequiredIfVersionManifestOrResourcesChange": True, "files": files}
    (ROOT / "ready.json").write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in result.items() if key != "files"}, ensure_ascii=False))
    print("ready.json SHA256=" + sha(ROOT / "ready.json") + " files=" + str(len(files)))


if __name__ == "__main__":
    main()
