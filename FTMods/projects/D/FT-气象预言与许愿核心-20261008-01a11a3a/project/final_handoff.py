import hashlib
import json
from pathlib import Path


ROOT = Path(__file__).resolve().parent


def main():
    deployment = json.loads((ROOT / "final-release/deployment.json").read_text(encoding="utf-8"))
    assert deployment["status"] == "deployed_and_verified"
    validation = json.loads((ROOT / "final-release/full-validation.json").read_text(encoding="utf-8"))
    assert validation["all_passed"]
    visual = json.loads((ROOT / "optimization/visual-comparison.json").read_text(encoding="utf-8"))
    models = []
    for name in ("weather_forecaster", "oracle", "wishing_well_core"):
        model = json.loads((ROOT / "optimization" / (name + ".json")).read_text(encoding="utf-8"))
        models.append({"model": name, "before_quads": model["original_quads"], "final_quads": model["final_quads"], "removed_quads": model["removed_quads"]})
    report = {"status": "final_optimized_deployed_pending_game_tests", "models": models,
              "deploy_target": deployment["target"], "old_sha256": deployment["old_sha256"], "new_sha256": deployment["new_sha256"],
              "backup": deployment["backup"], "written_jar_resources": deployment["write_entries"],
              "jar_non_target_entries_preserved": deployment["unrelated_entries_preserved"],
              "class_files_preserved": deployment["class_files_preserved"],
              "written_file_resources": deployment["file_resources"],
              "validation_steps_passed": len(validation["steps"]), "offline_visual_cases": visual["caseCount"],
              "superseded_retained": "revisions/superseded-preoptimization/status.json", "source_deleted": False,
              "pending_game_checks": deployment["pending_game_checks"], "gameplay_actual_client_tested": False,
              "unified_entry": "http://127.0.0.1:18790/?model=instruments",
              "archive": "D:/ft工程文件/FT-气象预言与许愿核心-20261008-01a11a3a",
              "no_neoForge_writes_this_round": True, "no_shared_qys_bosses_writes": True}
    before_files = json.loads((ROOT / "final-release/neoforge-readonly-baseline.json").read_text(encoding="utf-8"))
    assert all(hashlib.sha256(Path(filename).read_bytes()).hexdigest() == checksum for filename, checksum in before_files.items())
    (ROOT / "final-release/neoforge-unchanged-verification.json").write_text(json.dumps({"all_unchanged": True, "verified_files": len(before_files), "sha256": before_files}, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    preview = json.loads((ROOT / "unified-preview-verification.json").read_text(encoding="utf-8"))
    assert preview["idleDemandRenderingPassed"]
    report["web_idle_demand_rendering_verified"] = True
    (ROOT / "final-release/final-handoff.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    handoff_file = ROOT / "web-handoff.json"
    handoff = json.loads(handoff_file.read_text(encoding="utf-8"))
    handoff["tracking_thread_entry"]["ready"] = True
    handoff["tracking_thread_entry"]["note"] = "最终优化轮完成：面数330→288 / 588→510 / 342→282；8项优化单测、许愿脚本/结构模拟、21视角完整交互与42角度图像回归通过，网页按需绘制验证通过。以当刻当前Forge qys-event JAR增量部署，保留其他条目及类；旧工程标记superseded保留，归档最终SHA256见CHECKSUMS.json。未进行游戏实测，未改NeoForge或共享qys-bosses。"
    handoff["final_handoff_report"] = "final-release/final-handoff.json"
    handoff_file.write_text(json.dumps(handoff, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps({key: value for key, value in report.items() if key not in {"written_jar_resources", "written_file_resources"}}, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
