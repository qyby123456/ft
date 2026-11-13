import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def entries(value):
    return value if isinstance(value, list) else [value]


def main():
    inventory = load(ROOT / "inventory.json")
    results = []
    for group in inventory["groups"]:
        for room in group["candidates"]:
            if len(room["candidate_evidence"]) != 1 or "source" not in room["candidate_evidence"][0]:
                continue
            candidate = room["candidate_evidence"][0]
            source = Path(candidate["source"])
            before = digest(source)
            findings = []
            linked = []
            report_files = {
                "native": list(source.parent.rglob("independent-native*.json")),
                "routes": list(source.parent.rglob("independent-routes.json")),
                "render": list(source.parent.rglob("render*.json")),
            }
            for kind, paths in report_files.items():
                current = []
                for path in paths:
                    if any(part in ("r1", "r2", "history", "rejected", "archive") for part in path.relative_to(source.parent).parts):
                        continue
                    try:
                        data = load(path)
                        matching = [record for record in entries(data) if isinstance(record, dict) and any(record.get(key) == before for key in ("source_sha256", "candidate_sha256", "sha256"))]
                        if not matching:
                            linked.append({"kind": kind, "path": str(path), "binding": "NOT_CURRENT_SOURCE"})
                            continue
                        current.extend(matching)
                        linked.append({"kind": kind, "path": str(path), "sha256": digest(path), "binding": "CURRENT_SOURCE"})
                        if kind == "native":
                            for record in matching:
                                if record.get("registry_properties") != "PASS" or record.get("template_roundtrip_block_count") != "PASS" or record.get("survival_failures") or record.get("survival_unsupported"):
                                    findings.append({"code": "NATIVE_FAILURE_OR_UNSUPPORTED", "path": str(path)})
                        if kind == "routes":
                            for record in matching:
                                if not record.get("start_clear") or not record.get("targets") or any(not target.get("standing_clear") or not target.get("reachable") for target in record.get("targets", [])):
                                    findings.append({"code": "ROUTE_FAILURE_OR_EMPTY", "path": str(path)})
                                route_input = path.parent / "route-input.json"
                                if route_input.is_file():
                                    inputs = [value for value in entries(load(route_input)) if value.get("sha256") == before]
                                    if len(inputs) != 1:
                                        findings.append({"code": "AMBIGUOUS_ROUTE_INPUT", "path": str(route_input)})
                                    else:
                                        expected = [(value.get("label"), value.get("position")) for value in inputs[0].get("targets", [])]
                                        observed = [(value.get("label"), value.get("position")) for value in record.get("targets", [])]
                                        if expected != observed:
                                            findings.append({"code": "ROUTE_TARGET_LIST_DIFFERS", "path": str(path)})
                    except (OSError, ValueError, TypeError, AttributeError) as error:
                        findings.append({"code": "REPORT_UNREADABLE_RETRY_AFTER_FREEZE", "path": str(path), "error": str(error)})
                if not current:
                    findings.append({"code": f"NO_CURRENT_HASH_BOUND_{kind.upper()}_REPORT"})
            if digest(source) != before:
                findings.append({"code": "SOURCE_CHANGED_DURING_CHECK_RETRY_AFTER_FREEZE"})
            results.append({"id": room["id"], "candidate": str(source), "source_sha256": before, "reports": linked, "findings": findings, "review_scope": "PRODUCER_EVIDENCE_CONSISTENCY_ONLY_NOT_INDEPENDENT_NATIVE_OR_VISUAL_ACCEPTANCE", "accepted": False})
    report = {"inventory_checked_at": inventory["checked_at"], "rooms_checked": len(results), "rooms_with_findings": sum(bool(entry["findings"]) for entry in results), "accepted": 0, "rooms": results}
    (ROOT / "evidence-consistency.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"rooms_checked": report["rooms_checked"], "rooms_with_findings": report["rooms_with_findings"], "accepted": 0}))


if __name__ == "__main__":
    main()
