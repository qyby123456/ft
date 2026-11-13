import hashlib
import json
from datetime import datetime
from pathlib import Path
from zoneinfo import ZoneInfo


ROOT = Path(__file__).resolve().parent
WORK = Path(r"D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106")


def excluded_summary(relative):
    excluded_names = {"qa", "rejected", "archive", "history", "node_modules"}
    archived_group_directories = {"archive-preliminary-v1", "revision-before-visual-r2", "archive-pre-revision-20261106"}
    return any(part in excluded_names for part in relative.parts) or relative.parts[0] in archived_group_directories


def load(path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def main():
    results = []
    for number in range(1, 9):
        group = WORK / f"S{number}"
        references = load(group / "references.json")
        progress_path = group / "progress.json"
        progress = load(progress_path) if progress_path.is_file() else None
        ready_path = group / "ready-for-review.json"
        ready = load(ready_path) if ready_path.is_file() else None
        excluded_summaries = []
        live_summaries = []
        for summary_path in group.rglob("summary.json"):
            if excluded_summary(summary_path.relative_to(group)):
                excluded_summaries.append(str(summary_path))
            else:
                live_summaries.append((summary_path, load(summary_path)))
        candidates = []
        for reference in references["references"]:
            identifier = reference["id"]
            summaries = []
            for summary_path, summary in live_summaries:
                name = str(summary.get("name", "")).lower()
                if name == identifier.lower() or name.startswith(identifier.lower() + "-"):
                    summaries.append((summary_path, summary))
            evidence = []
            for summary_path, summary in summaries:
                source = Path(summary.get("nbtPath", ""))
                if not source.is_absolute():
                    source = summary_path.parent / source
                source = source.resolve()
                if not source.is_relative_to(group.resolve()) or not source.is_file():
                    evidence.append({"summary": str(summary_path), "error": "MISSING_OR_OUT_OF_SCOPE_SOURCE"})
                    continue
                before = source.stat()
                digest = hashlib.sha256(source.read_bytes()).hexdigest()
                after = source.stat()
                evidence.append({"summary": str(summary_path), "source": str(source), "sha256": digest, "declared_size": summary.get("size"), "stable_during_hash": (before.st_size, before.st_mtime_ns) == (after.st_size, after.st_mtime_ns), "preview_paths": [str(path) for path in source.parent.glob("*.png")], "design_metadata_paths": [str(path) for path in source.parent.glob("*design*.json")]})
            candidates.append({"id": identifier, "reference_image": reference["image_path"], "reference_sha256": reference["sha256"], "candidate_evidence": evidence, "inventory_status": "UNAMBIGUOUS_SUMMARY_FOUND" if len(evidence) == 1 and "source" in evidence[0] else "MISSING_OR_AMBIGUOUS_SUMMARY", "independent_acceptance": "NOT_RUN"})
        assert len(candidates) == 10
        results.append({"group": f"S{number}", "producer_progress": progress, "submission_found": ready is not None, "submission": ready, "excluded_summary_paths": excluded_summaries, "candidates": candidates, "unambiguous_candidates": sum(entry["inventory_status"] == "UNAMBIGUOUS_SUMMARY_FOUND" for entry in candidates), "review_status": "SUBMISSION_NEEDS_FREEZE_AND_REVIEW" if ready else "WAITING_PRODUCER_SUBMISSION", "accepted": False})
    report = {"checked_at": datetime.now(ZoneInfo("Asia/Shanghai")).isoformat(), "groups": results, "group_count": 8, "reference_count": 80, "independent_accepted_groups": 0, "note": "Read-only current-production inventory. Files may be changing. No acceptance inferred from presence or producer PASS."}
    (ROOT / "inventory.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({"groups": [{"group": entry["group"], "submission_found": entry["submission_found"], "unambiguous_candidates": entry["unambiguous_candidates"]} for entry in results], "accepted": 0}, ensure_ascii=False))


if __name__ == "__main__":
    main()
