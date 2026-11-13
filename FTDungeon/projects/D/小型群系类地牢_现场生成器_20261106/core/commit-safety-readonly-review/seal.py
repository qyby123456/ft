import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    lock = json.loads((ROOT / "source-lock.json").read_text(encoding="utf-8"))
    checks = []
    for record in lock["files"]:
        assert sha(ROOT / record["snapshot"]) == record["sha256"], "Review snapshot changed"
        current = sha(Path(record["path"]))
        checks.append({"path": record["path"], "reviewedSha256": record["sha256"],
                       "currentSha256": current, "changed": current != record["sha256"]})
    engine = next(record for record in lock["files"] if record["path"].endswith("UnifiedEngine.java"))
    version = re.search(r'static final String VERSION\s*=\s*"([^"]+)"', Path(engine["path"]).read_text(encoding="utf-8")).group(1)
    drift = {"runtimeVersionAtStart": lock["runtimeVersion"], "runtimeVersionAtEnd": version,
             "changedFiles": [record["path"] for record in checks if record["changed"]], "files": checks}
    (ROOT / "source-drift.json").write_text(json.dumps(drift, indent=2) + "\n", encoding="utf-8")
    assert not drift["changedFiles"], "Review only affected files before sealing: " + str(drift["changedFiles"])
    files = [{"path": path.relative_to(ROOT).as_posix(), "sha256": sha(path)}
             for path in sorted(ROOT.rglob("*")) if path.is_file() and path.name != "ready.json"]
    ready = {"status": "READ_ONLY_REVIEW_COMPLETE_ONE_P2_FINDING", "runtimeVersion": version,
             "sourceFilesLocked": len(checks), "sourceDriftAtEnd": False, "findings": 1,
             "probe": "JAVA17_SOURCE_EXTRACTED_COMMAND_FAILURE_CONTROL_FLOW_ONLY",
             "worldTests": "NOT_RUN", "actualCommitted": "NOT_EXECUTED", "runtimeModified": False,
             "activeSourceMayChangeAfterSeal": True, "files": files}
    (ROOT / "ready.json").write_text(json.dumps(ready, indent=2) + "\n", encoding="utf-8")
    print("READ_ONLY_REVIEW_READY findings=1 sources=" + str(len(checks)) + " files=" + str(len(files)))
    print("ready SHA256=" + sha(ROOT / "ready.json"))


if __name__ == "__main__":
    main()
