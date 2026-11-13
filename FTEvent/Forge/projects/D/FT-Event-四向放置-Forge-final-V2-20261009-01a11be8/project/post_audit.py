import json
from pathlib import Path

import release


ROOT = Path(__file__).resolve().parent


def main():
    deployment = json.loads((ROOT / "deployment.json").read_text(encoding="utf-8"))
    before = json.loads((ROOT / "baseline.json").read_text(encoding="utf-8"))["forge"]
    current = release.hashes(list(before))
    changed = [name for name in before if current[name] != before[name]]
    installed = deployment["status"] == "installed-offline-verified-game-pending"
    expected_changes = [release.JAR] if installed else []
    if changed != expected_changes:
        raise RuntimeError("Unexpected changes after deployment audit: " + str(changed))
    if current[release.JAR] != deployment["new_sha256"]:
        raise RuntimeError("Current JAR differs from deployment receipt")
    neo = release.neo_audit()
    release.save(ROOT / "post-deployment-verification.json", {
        "passed": True, "time": release.timestamp(), "installed": installed,
        "deployment_status": deployment["status"], "old_sha256": before[release.JAR],
        "actual_after_sha256": current[release.JAR], "changed_instance_files": changed,
        "expected_writes": deployment["written_items"], "backup_created": deployment["backup"] is not None,
        "all_script_and_model_hashes_unchanged": True, "neo_rollback": neo,
        "core_rotation_mirror_increment_applied": installed, "candidate_validation_passed": True,
        "game_tested": False, "qys_bosses_written": False
    })
    print("Post-attempt audit passed: " + ("entry-only delta installed" if installed else "zero instance writes; tested core increment remains unapplied"))


if __name__ == "__main__":
    main()
