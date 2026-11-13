import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def save(name, value):
    (ROOT / name).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    source_lock = load(ROOT / "inputs/source-lock.json")
    for record in source_lock["files"]:
        assert digest(Path(record["path"])) == record["sha256"], record["path"]
    tests = load(ROOT / "results/test-summary.json")
    nine = load(ROOT / "results/real-nine-results.json")
    independent = load(ROOT / "results/independent-verification.json")
    assert tests["result"] == "PASS" and tests["assertions"] == 47948 and tests["java"] == "17.0.8"
    assert nine["successes"] == 128 and nine["failures"] == 0
    assert independent["checks"] == 33060 and independent["plansIndependentlyChecked"] == 128
    assert "94932 assertions; Java=17.0.8" in (ROOT / "logs/baseline-regression-java17.log").read_text(encoding="utf-8-sig")
    jar = ROOT / "build/ft-biomedungeon-planner-y-v2-experimental.jar"
    assert digest(jar) in (ROOT / "logs/reproducible-jar.log").read_text(encoding="utf-8-sig").lower()
    assert (ROOT / "logs/cross-jvm.log").read_text(encoding="utf-8-sig").startswith("PASS")
    for path in ROOT.rglob("*.json"):
        if path.name not in {"ready.json", "SHA256SUMS.json"}:
            load(path)
    files = [{"path": path.relative_to(ROOT).as_posix(), "sha256": digest(path), "bytes": path.stat().st_size}
             for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"ready.json", "SHA256SUMS.json"}]
    save("SHA256SUMS.json", {"algorithm": "SHA-256", "component": "core-next-planner-y", "files": files})
    files.append({"path": "SHA256SUMS.json", "sha256": digest(ROOT / "SHA256SUMS.json"), "bytes": (ROOT / "SHA256SUMS.json").stat().st_size})
    save("ready.json", {
        "component": "core-next-planner-y", "apiVersion": 1, "ready": True,
        "status": "JAVA17_Y_ALIGNED_NINE_ROOM_GEOMETRY_READY_NOT_WORLD_ACCEPTED",
        "algorithmVersion": "ft-core-y-splitmix64-dfs-v2", "productionPromotion": False,
        "entryPoint": "cn.ft.biomedungeon.core.next.DungeonYPlanner.plan",
        "interface": "runtime-consumption.json", "sources": [record for record in files if record["path"].startswith("src/main/")],
        "artifacts": [record for record in files if record["path"].endswith(".jar")],
        "requiredDependencies": [{"path": str(ROOT.parent / "build/ft-biomedungeon-core-v1.jar"), "sha256": digest(ROOT.parent / "build/ft-biomedungeon-core-v1.jar")}],
        "baselineCoreReadySha256": digest(ROOT.parent / "ready.json"),
        "nextStairsReadySha256": digest(ROOT.parent / "next-stairs/ready.json"),
        "inputLock": "inputs/source-lock.json", "files": files,
        "tests": {"command": "& .\\build.ps1", "cwd": str(ROOT), "java": "17.0.8", "newAssertions": tests["assertions"],
                  "baselineRegressionAssertions": 94932, "independentPythonChecks": independent["checks"],
                  "realWorldSeeds": 32, "rootRotations": 4, "nineRoomSuccesses": 128, "nineRoomFailures": 0,
                  "coverage": "s8_01..s8_09 exactly once in every graph", "mixedHall04LocalFeetGraphs": nine["hall04UsesDifferentLocalFeetGraphs"],
                  "templatePortTreeVariants": nine["templateAndPortTreeVariants"], "crossJvmReplay": "PASS Java17/25", "reproducibleJar": "PASS"},
        "evidence": {"planDirectory": "results/plans", "outcomes": "results/real-nine-results.json", "independent": "results/independent-verification.json"},
        "limitations": ["Seven catalog-v3 candidates remain accepted=false; pure core does not enforce world resource eligibility",
                        "S8-01/07 stairs and original doors require native body/headroom, step-up and return-route verification",
                        "S8-08 lava/vine and dynamic/runtime policy not approved by geometry success",
                        "Runtime actual translated placement, payloads, ownership, reservations and recovery not world-tested here",
                        "Tree graphs and straight horizontal exterior corridors only; finite search budget is not a completeness guarantee"],
        "baselineChanged": False, "nextStairsChanged": False, "otherOwnerFilesModified": False,
        "sourceNbtModified": False, "worldAcceptance": "NOT_RUN", "overallMvpAccepted": False})
    for record in load(ROOT / "ready.json")["files"]:
        assert digest(ROOT / record["path"]) == record["sha256"], record["path"]
    print("PASS ready inventory: %d exact files; %d read-only external locks rechecked" % (len(files), len(source_lock["files"])))
    print("planner JAR SHA256 " + digest(jar))
    print("ready.json SHA256 " + digest(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
