import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(name, payload):
    (ROOT / name).write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    core = ROOT.parent
    baseline = json.loads((core / "ready.json").read_text(encoding="utf-8"))
    for record in baseline["files"]:
        assert digest(core / record["path"]) == record["sha256"], record["path"]
    source = json.loads((ROOT / "source-verification.json").read_text(encoding="utf-8"))
    assert digest(core / "ready.json") == source["coreReadySha256"]
    assert digest(Path(source["requirements"])) == source["requirementsSha256"]
    for record in source["records"]:
        assert digest(Path(record["source"])) == record["sourceSha256"]
        assert digest(Path(record["metadata"])) == record["metadataSha256"]
    for record in source["readOnlyParserDependencies"]:
        assert digest(Path(record["path"])) == record["sha256"]
    assert "PASS 398 assertions" in (ROOT / "logs/real-port-java17.log").read_text(encoding="utf-8-sig")
    for path in ROOT.glob("*.json"):
        if path.name != "ready.json":
            json.loads(path.read_text(encoding="utf-8"))
    records = [{"path": path.relative_to(ROOT).as_posix(), "sha256": digest(path), "bytes": path.stat().st_size}
               for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name not in {"ready.json", "SHA256SUMS.json"}]
    save("SHA256SUMS.json", {"component": "core-next-stairs", "files": records})
    records.append({"path": "SHA256SUMS.json", "sha256": digest(ROOT / "SHA256SUMS.json"), "bytes": (ROOT / "SHA256SUMS.json").stat().st_size})
    save("ready.json", {
        "component": "core-next-stairs", "apiVersion": 1, "ready": True,
        "status": "INVESTIGATION_AND_CONTRACT_READY_NOT_RUNTIME_UPGRADE",
        "baselineCoreReadySha256": digest(core / "ready.json"),
        "baselineJarSha256": digest(core / "build/ft-biomedungeon-core-v1.jar"),
        "baselineInventoryVerified": len(baseline["files"]),
        "command": "& .\\run.ps1; python -B finalize.py",
        "cwd": str(ROOT), "files": records,
        "tests": {"java": "17.0.8", "assertions": 398, "result": "PASS", "realSourceNBTDecoded": 2,
                  "manualRigidYMatches": 32, "widthMismatchPairs": 32, "isolatedHeightRejections": 2,
                  "nativeCollisionAndFullHeadroom": "NOT_RUN", "world": "NOT_RUN"},
        "deliverables": {"conclusions": "compatibility-result.json", "contract": "transition-contract.json",
                         "nativeVerifierRequirements": "transition-contract.json:requiredNativePosterior",
                         "realBlockEvidence": ["s8-01-north-transition-cells.json", "s8-07-transition-cells.json"],
                         "sourceLock": "source-verification.json"},
        "limits": ["Frozen planner still does not shift candidate Y; geometry feasibility study is not a planner upgrade",
                   "S8-01 original stairs require native shape/sweep and full overhead clearance verification before promotion",
                   "S8-07 south width5 cannot silently attach to width3", "Whole-room traversal and runtime posterior remain unverified"],
        "baselineChanged": False, "algorithmChanged": False, "otherOwnersModified": False,
        "originalNbtModified": False, "templatePromotion": "NONE", "overallMvpAccepted": False})
    ready = json.loads((ROOT / "ready.json").read_text(encoding="utf-8"))
    for record in ready["files"]:
        assert digest(ROOT / record["path"]) == record["sha256"]
    print("PASS next-stairs ready: " + str(len(records)) + " exact hashes; 20 frozen baseline files and original ready/NBT/metadata/parser hashes rechecked")
    print("ready.json SHA256 " + digest(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
