import argparse
import hashlib
import json
from pathlib import Path

from audit_s8_submission import load


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("directory", type=Path)
    root = parser.parse_args().directory.resolve()
    manifest = load(root / "ready-for-review.json")
    primary = load(root / "submission-snapshot.json")
    assert hashlib.sha256((root / "ready-for-review.json").read_bytes()).hexdigest() == primary["manifest_sha256"]
    records = list(primary["files"])
    if (root / "supplemental-snapshot.json").is_file():
        records += load(root / "supplemental-snapshot.json")["files"]
    captured = {}
    for record in records:
        frozen = Path(record["snapshot"])
        assert frozen.is_relative_to(root)
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == record["sha256"]
        assert record["source"] not in captured
        captured[record["source"]] = frozen
    group = manifest.get("style") or manifest.get("group") or primary["style"]
    inputs = []
    bindings = []
    for index, specification in enumerate([*manifest["rooms"], manifest.get("assembly") or manifest["merged"]]):
        specification = dict(specification)
        if "candidate" not in specification:
            specification["candidate"] = specification["source"]["path"]
            specification["sha256"] = specification["source"]["sha256"]
        original = Path(specification["candidate"])
        frozen = captured[str(original)]
        assert hashlib.sha256(frozen.read_bytes()).hexdigest() == specification["sha256"]
        route_source = str(original.parent / "qa/route-input.json")
        route_path = captured[route_source]
        route_inputs = load(route_path)
        assert len(route_inputs) == 1
        route = route_inputs[0]
        assert route["sha256"] == specification["sha256"]
        inherited_path = Path(route["candidate"]).resolve() != original.resolve()
        if inherited_path:
            if manifest.get("revision") == "local-relief-v4":
                assert specification.get("changed_in_v4") is False
                prior = load(Path(__file__).parent / "S7-submission-20261107-061916-548777/submission-snapshot.json")
                previous = next(record for record in prior["files"] if record["source"] == route["candidate"])
                assert previous["sha256"] == specification["sha256"]
                assert hashlib.sha256(Path(previous["snapshot"]).read_bytes()).hexdigest() == specification["sha256"]
            else:
                assert specification.get("changed_in_v3") is False
                assert specification.get("qa_provenance", "").startswith("Inherited exact-hash")
            assert Path(route["candidate"]).name == original.name
            assert route["size"] == specification["size"]
        inputs.append(dict(route, candidate=str(frozen), group=int(group[1:]) * 100 + (11 + index if index < 10 else 99)))
        bindings.append({"source": str(frozen), "sha256": specification["sha256"], "route_input_snapshot": str(route_path), "targets": len(route["targets"]), "producer_route_candidate": route["candidate"], "inherited_exact_hash_route": inherited_path})
    assert len(inputs) == 11
    destination = root / "independent-qa"
    destination.mkdir()
    (destination / "route-input.json").write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding="utf-8")
    tags = Path(__file__).parent / "support-fixtures-20261106-171423-281/vanilla-block-tags.json"
    (destination / "vanilla-block-tags.json").write_bytes(tags.read_bytes())
    (destination / "input-bindings.json").write_text(json.dumps({"stage": "INDEPENDENT_RUN_INPUT_PREPARATION", "bindings": bindings, "scope": "Frozen producer-selected targets, independently executed against frozen source. Target completeness and merged payload/parts require separate audit.", "accepted": False}, ensure_ascii=False, indent=2), encoding="utf-8")
    print(json.dumps({"directory": str(destination), "sources": len(inputs), "targets": sum(len(item["targets"]) for item in inputs)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
