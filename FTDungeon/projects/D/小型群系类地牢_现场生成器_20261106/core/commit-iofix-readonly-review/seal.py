import hashlib
import json
from pathlib import Path
from probe_commands import extract

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    lock = load(ROOT / "source-lock.json")
    for record in lock["files"]:
        assert sha(Path(record["path"])) == record["sha256"], "Affected source changed: " + record["path"]
        assert sha(ROOT / record["snapshot"]) == record["sha256"], "Snapshot changed"
    frozen = []
    for directory in (ROOT.parent / "commit-safety-readonly-review", ROOT.parent / "v5-template-usability-review", PROJECT / "runtime/next-commit"):
        ready = directory / "ready.json"
        records = load(ready)["files"]
        pairs = records.items() if isinstance(records, dict) else ((item["path"], item["sha256"]) for item in records)
        count = 0
        for relative, expected in pairs:
            assert sha(directory / relative) == expected, "Frozen evidence drift: " + str(directory / relative)
            count += 1
        frozen.append({"path": str(ready), "sha256": sha(ready), "verifiedFiles": count})
    relative = "snapshot/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java"
    previous = (ROOT.parent / "commit-safety-readonly-review" / relative).read_text(encoding="utf-8")
    current = (ROOT / relative).read_text(encoding="utf-8")
    for signature in ("String pause(String id)", "String commit(String id,"):
        previous = previous.replace(extract(previous, signature), "REVIEWED_METHOD")
        current = current.replace(extract(current, signature), "REVIEWED_METHOD")
    assert previous == current, "Changes outside reviewed methods require affected-file review"
    changed = [item["snapshot"] for item in lock["files"] if item["changedFromPreviousReview"]]
    assert changed == [relative], "Additional changed sources require review: " + str(changed)
    geometry = load(ROOT.parent / "v5-template-usability-review/inputs/input-lock.json")
    assert sha(Path(geometry["manifest"])) == geometry["manifestSha256"], "Geometry manifest changed"
    for item in geometry["sourceResources"]:
        assert sha(Path(item["path"])) == item["sha256"], "Geometry resource changed"
    assert lock["runtimeVersion"] == geometry["runtimeVersion"]
    core = []
    for source in sorted((PROJECT / "runtime/next-commit/core-snapshot").rglob("*.java")):
        relative_core = source.relative_to(PROJECT / "runtime/next-commit/core-snapshot")
        revised = PROJECT / "runtime/next-commit-iofix/core-snapshot" / relative_core
        assert sha(source) == sha(revised), "Planner core snapshot changed"
        core.append({"path": relative_core.as_posix(), "sha256": sha(source)})
    probe = (ROOT / "probe/result.log").read_text(encoding="utf-8-sig")
    assert "checks=41 cases=7" in probe and "IOFIX_SOURCE_EXTRACTED_CONTROL_FLOW_PASS" in probe
    result = {"status": "PASS_PREVIOUS_P2_CLOSED_AT_SOURCE_CONTROL_FLOW_LEVEL", "runtimeVersion": lock["runtimeVersion"],
              "changedFiles": changed, "onlyPauseAndCommitChanged": True, "sourceDriftAtEnd": False,
              "probeCases": 7, "probeAssertions": 41, "java": "17.0.8", "newFindings": 0,
              "geometryVersionManifestResourcesAndCoreUnchanged": True, "geometry129RerunRequired": False,
              "frozenEvidenceVerified": frozen, "unchangedCoreSnapshots": core,
              "worldTests": "NOT_RUN", "actualCommitted": "NOT_EXECUTED", "runtimeModified": False}
    (ROOT / "result.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    files = [{"path": path.relative_to(ROOT).as_posix(), "sha256": sha(path)}
             for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name != "ready.json"]
    ready = dict(result, files=files, activeRuntimeMayChangeAfterSeal=True)
    (ROOT / "ready.json").write_text(json.dumps(ready, indent=2) + "\n", encoding="utf-8")
    print("IOFIX_REVIEW_PASS cases=7 assertions=41 newFindings=0 files=" + str(len(files)))
    print("ready SHA256=" + sha(ROOT / "ready.json"))
    for item in frozen:
        print("FROZEN_PASS", item["path"], item["verifiedFiles"])


if __name__ == "__main__":
    main()
