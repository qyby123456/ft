import hashlib
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parent.parent
SOURCE = PROJECT / "runtime/next-commit/src"


def digest(data):
    return hashlib.sha256(data).hexdigest()


def main():
    records = []
    for source in sorted(SOURCE.rglob("*.java")):
        relative = source.relative_to(SOURCE)
        data = source.read_bytes()
        target = ROOT / "snapshot" / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(data)
        records.append({"path": str(source), "snapshot": target.relative_to(ROOT).as_posix(), "sha256": digest(data)})
    engine = ROOT / "snapshot/main/java/cn/ft/biomedungeon/runtime/UnifiedEngine.java"
    version = re.search(r'static final String VERSION\s*=\s*"([^"]+)"', engine.read_text(encoding="utf-8")).group(1)
    result = {"runtimeVersion": version, "mode": "READ_ONLY_CODE_REVIEW_NO_WORLD_RUN", "files": records}
    (ROOT / "source-lock.json").write_text(json.dumps(result, indent=2) + "\n", encoding="utf-8")
    print("Captured", len(records), "Java sources; VERSION", version)


if __name__ == "__main__":
    main()
