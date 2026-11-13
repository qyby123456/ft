import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    results = []
    for relative in (
        "core/next-stairs", "catalog/expansion-v3",
        "catalog/acceptance-candidate-v4", "runtime/next-fluid",
    ):
        component = (ROOT.parent / relative).resolve()
        ready_path = component / "ready.json"
        ready_hash = digest(ready_path)
        ready = load(ready_path)
        assert ready["ready"] is True
        entries = ready.get("files")
        if entries is None:
            inventory = component / "SHA256SUMS.json"
            assert digest(inventory) == ready["inventorySha256"]
            entries = load(inventory)["files"]
        if isinstance(entries, dict):
            entries = [{"path": name, "sha256": value} for name, value in entries.items()]
        assert isinstance(entries, list) and entries
        for entry in entries:
            path = (component / entry["path"]).resolve()
            assert path.is_relative_to(component), path
            assert digest(path) == entry["sha256"], path
            if "bytes" in entry:
                assert path.stat().st_size == entry["bytes"], path
        assert digest(ready_path) == ready_hash
        results.append({"component": relative, "ready_sha256": ready_hash,
                        "verified_files": len(entries), "integrity": "PASS"})
    report = {"extensions": results, "scope": "Frozen handoff integrity, not independent world or command-pool acceptance",
              "all_new_templates_promoted": False, "complete_generator_ready": False}
    (ROOT / "extension-handoff-audit.json").write_text(
        json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8"
    )
    print(json.dumps(report, ensure_ascii=False))


if __name__ == "__main__":
    main()
