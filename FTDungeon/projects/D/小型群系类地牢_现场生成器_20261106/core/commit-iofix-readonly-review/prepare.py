import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent
SOURCE = PROJECT / "runtime/next-commit-iofix/src"


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    frozen = []
    for directory in ("commit-safety-readonly-review", "v5-template-usability-review"):
        parent = ROOT.parent / directory
        ready = parent / "ready.json"
        records = json.loads(ready.read_text(encoding="utf-8"))["files"]
        for record in records:
            path = parent / record["path"]
            assert sha(path) == record["sha256"], "Frozen evidence changed: " + str(path)
        frozen.append({"path": str(ready), "sha256": sha(ready), "verifiedFiles": len(records)})
    assert SOURCE.is_dir(), "New runtime source not available yet: " + str(SOURCE)
    previous = json.loads((ROOT.parent / "commit-safety-readonly-review/source-lock.json").read_text(encoding="utf-8"))
    old = {record["snapshot"]: record for record in previous["files"]}
    records = []
    for source in sorted(SOURCE.rglob("*.java")):
        relative = Path("snapshot") / source.relative_to(SOURCE)
        data = source.read_bytes()
        target = ROOT / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        digest = hashlib.sha256(data).hexdigest()
        prior = old.get(relative.as_posix())
        records.append({"path": str(source), "snapshot": relative.as_posix(), "sha256": digest,
                        "changedFromPreviousReview": prior is None or prior["sha256"] != digest})
    engine = ROOT / "snapshot/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java"
    version = re.search(r'static final String VERSION\s*=\s*"([^"]+)"', engine.read_text(encoding="utf-8")).group(1)
    result = {"runtimeVersion": version, "previousRuntimeVersion": previous["runtimeVersion"],
              "geometry129RerunRequiredForVersion": version != previous["runtimeVersion"],
              "frozenEvidence": frozen, "mode": "READ_ONLY_IOFIX_CONTROL_FLOW_REVIEW_NO_WORLD", "files": records}
    (ROOT / "source-lock.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print("VERSION", version, "geometry129RerunRequiredForVersion", result["geometry129RerunRequiredForVersion"])
    for record in records:
        if record["changedFromPreviousReview"]:
            print("CHANGED", record["snapshot"])


if __name__ == "__main__":
    main()
