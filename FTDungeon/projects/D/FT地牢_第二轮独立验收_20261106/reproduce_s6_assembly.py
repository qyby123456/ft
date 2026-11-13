import hashlib
import json
from pathlib import Path
import subprocess
import sys


def main():
    root = Path(sys.argv[1]).resolve()
    source = root / "reproduce-frozen-assembly.mjs"
    original = source.read_text(encoding="utf-8-sig")
    old = "const workspace=path.join(root,'qa/frozen-assembly-reproduction')"
    new = "const workspace=path.join(root,'independent-reproduction-assembly')"
    assert original.count(old) == 1
    destination = root / "independent-reproduce-assembly.mjs"
    assert not destination.exists()
    destination.write_text(original.replace(old, new), encoding="utf-8")
    existing = {}
    for filename in ("submission-snapshot.json", "supplemental-snapshot.json", "delivery-snapshot.json"):
        for record in json.loads((root / filename).read_text(encoding="utf-8"))["files"]:
            existing[record["snapshot"]] = record["sha256"]
    assert hashlib.sha256(source.read_bytes()).hexdigest() == existing[str(source)]
    result = subprocess.run([r"C:\Program Files\nodejs\node.exe", str(destination)], capture_output=True, encoding="utf-8", errors="replace", timeout=240)
    (root / "independent-qa/assembly-reproduction.log").write_text(result.stdout + result.stderr, encoding="utf-8")
    assert result.returncode == 0, result.stderr
    for filename, digest in existing.items():
        assert hashlib.sha256(Path(filename).read_bytes()).hexdigest() == digest
    report = json.loads((root / "independent-reproduction-assembly/result.json").read_text(encoding="utf-8"))
    assert report["compressed_bytes_equal"] and report["source_sha256"] == report["reproduced_sha256"]
    report["reviewer_scope"] = "Parent independently executed frozen wrapper with only its output workspace literal redirected; summary path locators rewritten by original wrapper, original declared evidence hashes unchanged. Not a world run."
    report["wrapper_source_sha256"] = hashlib.sha256(source.read_bytes()).hexdigest()
    report["wrapper_rewrite"] = {"from": old, "to": new}
    (root / "independent-qa/assembly-reproduction.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    print(report["status"], report["source_sha256"])


if __name__ == "__main__":
    main()
