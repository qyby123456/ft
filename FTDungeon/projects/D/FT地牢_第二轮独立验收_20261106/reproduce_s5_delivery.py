import hashlib
import json
from pathlib import Path
import subprocess
import sys


def main():
    root = Path(sys.argv[1]).resolve()
    source = root / "reproduce.py"
    original = source.read_text(encoding="utf-8-sig")
    substitutions = {"target=root/'qa/reproduction-final'": "target=root/'independent-reproduction-final'", "root/'qa/reproduction-report.json'": "root/'independent-qa/reproduction-report.json'"}
    rewritten = original
    for before, after in substitutions.items():
        assert rewritten.count(before) == 1
        rewritten = rewritten.replace(before, after)
    script = root / "independent-reproduce.py"
    assert not script.exists()
    script.write_text(rewritten, encoding="utf-8")
    records = []
    for filename in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        records += json.loads((root / filename).read_text(encoding="utf-8"))["files"]
    assert any(record["snapshot"] == str(source) and record["sha256"] == hashlib.sha256(source.read_bytes()).hexdigest() for record in records)
    result = subprocess.run([sys.executable, str(script)], capture_output=True, timeout=300)
    (root / "independent-qa/reproduction.log").write_bytes(result.stdout + result.stderr)
    assert result.returncode == 0, result.stderr.decode("utf-8", errors="replace")
    for record in records:
        assert hashlib.sha256(Path(record["snapshot"]).read_bytes()).hexdigest() == record["sha256"]
    report_path = root / "independent-qa/reproduction-report.json"
    report = json.loads(report_path.read_text(encoding="utf-8"))
    assert report["passed"] and len(report["results"]) == 11 and all(item["byte_equal"] for item in report["results"])
    report["scope"] = "Parent independently executed frozen reproduction wrapper with only output and report destinations redirected; ten source rooms and merged output reproduced byte-identically, declared evidence unchanged. No world run."
    report["wrapper_substitutions"] = substitutions
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print("Eleven NBT outputs independently reproduced byte-identically")


if __name__ == "__main__":
    main()
