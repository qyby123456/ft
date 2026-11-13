import argparse
import ctypes
import json
from pathlib import Path

import release


ROOT = Path(__file__).resolve().parent
KEY = "event_four_way"


def update(mode):
    path = release.TRACKING
    original = path.read_bytes()
    tracking = json.loads(original)
    previous = next(entry for entry in tracking["threads"] if entry["key"] == KEY)
    entry = dict(previous)
    entry.update({"manorBuffCheckExempt": True, "gameTested": False, "inGameVerified": False,
                  "neoForgeRollbackVerified": release.neo_audit()["rollback_verified"],
                  "previewUrl": "http://127.0.0.1:18790/event_four_way/viewer.html",
                  "scopeExcludes": ["qys-bosses", "NeoForge writes", "manor scaling and speed", "character/Boss spells"],
                  "skillEffectCoverageStatus": "N/A with scoped evidence",
                  "skillEffectCoverageEvidence": "Static block state/collision/rotation/mirror audit only; no character/Boss skills or shared renderer assets modified"})
    if mode == "preview":
        entry.update({"ready": False, "finalDeploymentStatus": "candidate-preview-ready-deployment-pending",
                      "note": "Forge当前四向状态、三模型和NeoForge回滚已复核；核心结构旋转/镜像的qys-event-only候选增量正在最终校验。此阶段不宣称部署完成；实机摆放与交互仍待验。"})
    else:
        deployment = json.loads((ROOT / "deployment.json").read_text(encoding="utf-8"))
        archive = json.loads((ROOT / "archive-sha256.json").read_text(encoding="utf-8"))
        checks = json.loads((ROOT / "verification.json").read_text(encoding="utf-8"))
        installed = deployment["status"] == "installed-offline-verified-game-pending"
        ready = installed and checks["passed"] and archive["verified"]
        entry.update({"ready": ready, "forgeInstalled": True, "installed": installed,
                      "finalOptimizationPassed": checks["passed"], "archivePath": str(release.ARCHIVE),
                      "handoffPath": str(release.ARCHIVE / "handoff.json"),
                      "forgeJarSha256": release.sha(release.target(release.JAR).read_bytes()).upper(),
                      "baselineSha256": deployment["old_sha256"], "installedSha256": deployment["new_sha256"],
                      "deploymentReceipt": str(release.ARCHIVE / "validation/deployment.json"),
                      "archiveSha256": archive["sha256"], "archiveSha256Verified": archive["verified"],
                      "archiveManifestSha256": archive["manifest_sha256"], "archiveFileCount": archive["files"],
                      "runtimeDeltaPath": str(release.ARCHIVE / "runtime/qys-event-core-facing.entries.zip"),
                      "runtimePatchEntries": len(release.ENTRIES),
                      "finalDeploymentStatus": deployment["status"],
                      "previousArchiveStatus": "Early final and early engineering superseded; retained for coordinator cleanup",
                      "neoForgeWrittenThisPass": False, "qysBossesWrittenThisPass": False,
                      "pendingInGame": checks["pending_in_game"], "readyScope": "Offline final delivery only; Minecraft restart, Mixin application, placement and interactions not tested",
                      "note": ("Event四向放置V2独立归档、运行时增量、交互预览与SHA256复核完成。仅当前Forge qys-event JAR补核心旋转/镜像：1份Mixin配置与2个新增类；109个其他条目、48个现有类及KubeJS脚本/资源完全保留。NeoForge三项回滚哈希只读复核一致，庄园倍率与技能效果审计不适用且有范围证据。早期工程仅标superseded并保留；ready仅指离线工程交付，重启游戏后的Mixin应用、四向摆放、旧存档、结构与绿宝石交互待验。"
                               if installed else "Event四向放置V2独立最终归档、三条目运行时增量、源码、交接与SHA256复核完成；81项JVM/ASM/API断言、9项部署安全测试、8项模型回归、1824个旋转后核心顶点与独立/统一入口21视角和12朝向交互通过。Forge已有三方块四向放置仍保留，但当前qys-event JAR占用，安全部署在备份/写入前停止，V2核心结构旋转镜像未安装，保持未ready。所有实例文件哈希未变，NeoForge三项回滚哈希只读复核一致，qys-bosses未触碰。庄园/技能专项不适用且有范围证据；早期仅标superseded，重启游戏后的四向摆放、Mixin应用、结构与绿宝石交互待验。")})
    index = tracking["threads"].index(previous)
    tracking["threads"][index] = entry
    preview = {"key": KEY, "label": "event四向放置 · 三方块状态与旋转", "owner": KEY,
               "root": "_work/event-four-way-final-v2-20261009/preview", "pages": ["viewer.html"],
               "images": ["previews/weather_forecaster_facing_east.png", "previews/oracle_facing_west.png",
                          "previews/wishing_well_core_facing_south.png"]}
    existing = next((item for item in tracking["previews"] if item["key"] == KEY), None)
    if existing is None:
        tracking["previews"].append(preview)
    else:
        tracking["previews"][tracking["previews"].index(existing)] = preview
    for old, new in zip(json.loads(original)["threads"], tracking["threads"]):
        if old["key"] != KEY and old != new:
            raise RuntimeError("Unrelated tracking entry changed")
    backup = ROOT / ("tracking-before-" + mode + ".json")
    if backup.exists():
        raise RuntimeError("Tracking update already attempted; inspect the receipt instead of stale overwrite")
    backup.write_bytes(original)
    temporary = path.with_name("tracking.event-four-way.tmp")
    with temporary.open("xb") as output:
        output.write((json.dumps(tracking, ensure_ascii=False, indent=2) + "\n").encode("utf-8"))
    try:
        if path.read_bytes() != original:
            raise RuntimeError("Tracking changed concurrently; stopped without overwrite")
        kernel = ctypes.WinDLL("kernel32", use_last_error=True)
        kernel.ReplaceFileW.argtypes = [ctypes.c_wchar_p, ctypes.c_wchar_p, ctypes.c_wchar_p,
                                       ctypes.c_uint32, ctypes.c_void_p, ctypes.c_void_p]
        kernel.ReplaceFileW.restype = ctypes.c_int
        if not kernel.ReplaceFileW(str(path), str(temporary), None, 0, None, None):
            raise ctypes.WinError(ctypes.get_last_error())
    finally:
        if temporary.exists():
            temporary.unlink()
    current = json.loads(path.read_bytes())
    actual = next(item for item in current["threads"] if item["key"] == KEY)
    if actual != entry:
        raise RuntimeError("Tracking receipt mismatch")
    release.save(ROOT / ("tracking-update-" + mode + ".json"), {"time": release.timestamp(), "entry": entry,
                 "preview": preview, "other_thread_entries_unchanged": True, "backup": str(backup)})
    print("Updated only Event tracking entry and Event preview mount: " + mode)


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("mode", choices=("preview", "final"))
    update(parser.parse_args().mode)
