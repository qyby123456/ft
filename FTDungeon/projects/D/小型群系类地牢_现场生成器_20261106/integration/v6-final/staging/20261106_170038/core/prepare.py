import hashlib
import json
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    locks = {}
    verified = []
    for component in (PROJECT / "core", PROJECT / "core/next-stairs", PROJECT / "core/next-planner-y", PROJECT / "core/route-coordinate-review", PROJECT / "integration/v4-unified"):
        ready_path = component / "ready.json"
        ready = read(ready_path)
        files = ready["files"]
        if isinstance(files, dict):
            files = [{"path": path, "sha256": value} for path, value in files.items()]
        for record in files:
            path = component / record["path"]
            assert sha(path) == record["sha256"], path
            locks[str(path)] = sha(path)
        locks[str(ready_path)] = sha(ready_path)
        verified.append({"component": str(component), "readySha256": sha(ready_path), "verifiedInventoryFiles": len(files)})
    integration = PROJECT / "integration/v4-unified"
    for name in ("budget-rejected-seed.json", "budget-rejected-plan.json", "budget-seed0.json", "installed-q3-retry-progress.json", "README.md", "PlanBudgetProbe.java"):
        (ROOT / "inputs" / name).write_bytes((integration / name).read_bytes())
    for path in (PROJECT / "runtime/next-unified/src/main/java/cn/ft/biomedungeon/runtime/Placement.java",
                 PROJECT / "runtime/next-unified/src/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java"):
        locks[str(path)] = sha(path)
        (ROOT / "inputs" / path.name).write_bytes(path.read_bytes())
    (ROOT / "inputs/real-nine-ports.tsv").write_bytes((PROJECT / "core/next-planner-y/inputs/real-nine-ports.tsv").read_bytes())
    rejected = read(integration / "budget-rejected-plan.json")
    dimensions = [high - low for low, high in zip(rejected["envelopeMin"], rejected["envelopeMaxExclusive"])]
    assert dimensions == [277, 31, 97]
    assert [value - 6 for value in dimensions] == [271, 25, 91]
    assert rejected["cells"] == 832939 and not rejected["fitsRuntimeBudget"]
    retry = read(integration / "budget-seed0.json")
    assert retry["request"]["worldSeed"] == 0 and retry["request"]["instanceOrdinal"] == rejected["request"]["instanceOrdinal"] == 3
    summary = {"verifiedComponents": verified, "firstRefusal": {"worldSeed": rejected["request"]["worldSeed"], "ordinal": 3,
              "reason": "Runtime envelope innerSpanX271 exceeds256; not planner EXHAUSTED", "innerSpans": [271,25,91], "paddedCells":832939},
              "retry": {"worldSeed":0,"ordinal":3,"cells":retry["cells"],"sameRequest":False},
              "scope": "Read-only sealed integration evidence; no world test or door correction by core"}
    (ROOT / "inputs/integrity-and-evidence.json").write_text(json.dumps(summary, indent=2) + "\n", encoding="utf-8")
    (ROOT / "inputs/external-lock.json").write_text(json.dumps({"files":[{"path":path,"sha256":digest}for path,digest in sorted(locks.items())]}, indent=2) + "\n", encoding="utf-8")
    print("PASS frozen inventories: " + str(verified))
    print("CONFIRMED original q3 geometry planned but innerX271>256; seed0 retry differs from original worldSeed")


if __name__ == "__main__":
    main()
