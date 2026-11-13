from __future__ import annotations

import argparse
import hashlib
import json
import zipfile
from datetime import datetime, timezone, timedelta

from PIL import Image

from build_m09 import OUT, REFERENCE, REFERENCE_SHA256, ROOT, digest, dump


def finalize():
    parser = argparse.ArgumentParser()
    parser.add_argument("--native-log", required=True)
    arguments = parser.parse_args()
    log_path = ROOT / arguments.native_log
    fingerprint = digest(OUT / "room.nbt")
    evidence = {}
    for name in ("static-qa", "native-registry-qa", "native-world-qa", "preview-provenance"):
        evidence[name] = json.loads((ROOT / f"qa/{name}.json").read_text(encoding="utf-8"))
        if evidence[name]["status"] != "PASS" or evidence[name]["sha256"] != fingerprint:
            raise ValueError(f"Missing, failed or stale evidence: {name}")
    native = evidence["native-world-qa"]
    log = log_path.read_text(encoding="utf-8", errors="replace")
    if "M09_NATIVE_QA_PASS" not in log or "BUILD SUCCESSFUL" not in log:
        raise ValueError("No successful native GameTest process evidence")
    if len(native["rotations"]) != 4 or native["stable_ticks"] != 120:
        raise ValueError("Four rotations and full tick delay required")
    for rotation in native["rotations"]:
        if any(rotation[key] != "PASS" for key in ("placement", "neighbor_updates", "after_120_ticks", "fixture_rollback")):
            raise ValueError("Incomplete rotation")
    image = Image.open(OUT / "preview.png")
    if image.size != (1920, 1440) or digest(OUT / "preview.png") != evidence["preview-provenance"]["preview_sha256"]:
        raise ValueError("Preview fingerprint or resolution mismatch")
    if digest(REFERENCE) != REFERENCE_SHA256:
        raise ValueError("Reference changed")
    visual_path = ROOT / "qa/visual-review.json"
    if visual_path.exists():
        visual = json.loads(visual_path.read_text(encoding="utf-8"))
        if visual["sha256"] != fingerprint or visual["preview_sha256"] != digest(OUT / "preview.png"):
            raise ValueError("Visual review is stale; inspect final images again")
    checks = evidence["static-qa"]["checks"]
    result = {
        "id": "M09", "name": "幽魂藏书室", "status": "PASS", "sha256": fingerprint,
        "timestamp": datetime.now(timezone(timedelta(hours=8))).isoformat(),
        "reference_sha256": REFERENCE_SHA256, "static_checks": checks,
        "native": {key: value for key, value in native.items() if key not in ("status", "timestamp", "scope")},
        "preview": {"size": list(image.size), "sha256": digest(OUT / "preview.png"), "source": "Actual delivered NBT + native outline shapes + vanilla textures; not AI and not a game screenshot", "cutaway_modifies_nbt": False},
        "evidence": {name: {"path": str(ROOT / f"qa/{name}.json"), "sha256": digest(ROOT / f"qa/{name}.json")} for name in evidence},
        "visual_review": {"path": str(visual_path), "sha256": digest(visual_path)} if visual_path.exists() else {"status": "not_repeated_on_rerun"},
        "native_log": {"path": str(log_path), "sha256": digest(log_path)},
        "rejected_attempt": {"path": "qa/rejected-attempt-1", "reason": "Iron bars adjoining window glazing had south=false; native placement required south=true. Original failed hash and log retained, corrected template fully retested."},
        "boundaries": {"deployed": False, "core_modified": False, "player_saves_opened": False, "other_room_directories_modified": False, "paid_image_api_called": False},
        "limitations": ["No FT core/catalog connection or placement into production dungeon", "No full modpack client, shader/FPS or multiplayer gameplay validation", "Software preview approximates transparent models and omits block-entity animations", "No mobs, waves, loot or FT reward behavior added; normal vanilla block mechanics remain"],
    }
    dump(OUT / "qa.json", result)
    static = evidence["static-qa"]
    material_lines = ["# M09 材料与安全口径", "", "以下数量从最终 NBT 读取；不计空气。", "", "| 原版方块 | 数量 |", "| --- | ---: |"]
    material_lines += [f"| `{name}` | {count} |" for name, count in static["materials"].items() if name != "minecraft:air"]
    material_lines += ["", "两只木桶为原生空库存；讲台无可领取书。附魔台仅保留原版方块与其原生书本动画，不加入自定义库存或奖励。", "", "炭黑墙、残木和石渣代替持续燃烧：无明火/魂火/熔岩/水，正常火焰蔓延不会由本模板主动触发。蛛网远离主通道。"]
    (ROOT / "MATERIALS.md").write_text("\n".join(material_lines) + "\n", encoding="utf-8")
    coordinator_lines = [
        "# M09 幽魂藏书室 · 独立交接报告", "", "## 结果", "",
        "静态 QA 与真实 Forge 四向放置/正常邻居更新/120 tick 稳定性 QA 均通过；尚未接入或部署。", "",
        f"- 最终模板 SHA-256：`{fingerprint}`。",
        f"- 参考图 SHA-256：`{REFERENCE_SHA256}`。",
        f"- 实际 NBT 软件预览 SHA-256：`{digest(OUT / 'preview.png')}`。",
        "- 交付：`out/M09/room.nbt`、`room.json`、`preview.png`、`qa.json`。",
        "- 尺寸 21×12×21，5292 条显式体积记录；北入口 `[10,1,0]`，内安全点 `[10,1,2]`。", "",
        "## 实际验证", "",
        f"- 静态 {len(checks)} 项检查全部通过；NBT 单调色板、DataVersion 3465、完整地板/441 格实体屋顶和唯一北口。",
        "- 原版注册表与状态属性真实解码；StructureTemplate 原生加载和保存往返通过。",
        "- NONE / CLOCKWISE_90 / CLOCKWISE_180 / COUNTERCLOCKWISE_90 四向真实放置，每次放置后主动执行正常邻居更新，再等待 120 个服务端 tick。",
        "- 每快照核对 21168 条四向记录，三次快照共 63504 条；原生 canSurvive 支撑、141 格书架直接承托、三道横梁/纵梁/柱及灯具均通过。",
        "- 原生 0.6×1.8 玩家体积和边间扫掠碰撞检查覆盖入口、安全点、书墙、残骸侧、书桌两侧和椅后回路。",
        "- 每方向两只空木桶，共 54 个实际库存格逐格为空；讲台无书、无 LootTable，无实体/掉落物。",
        "- 测试夹具从已验证空气体积放置，测试后恢复本次夹具；不清理其它线程或玩家数据。", "",
        "| 旋转 | 原生可达脚位 | 非空气生存检查/快照 | 空库存格 | 120 tick |",
        "| --- | ---: | ---: | ---: | --- |",
    ]
    coordinator_lines += [f"| {entry['rotation']} | {entry['reachable_feet']} | {entry['non_air_survival_checks']} | {entry['empty_inventory_slots']} | {entry['after_120_ticks']} |" for entry in native["rotations"]]
    coordinator_lines += [
        "", "## 失败留证与修正", "",
        "首轮原生测试拒绝了不正确的铁栏 `south=false`。相邻青色玻璃需要 `south=true`，现已在生成源修正并用新独立世界全量重测。原失败模板/元数据/JSON 与 attempt-1 日志保留，不将失败轮次记为通过。", "",
        "## 证据与边界", "",
        "- `qa/static-qa.json`、`qa/native-registry-qa.json`、`qa/native-world-qa.json`、`qa/preview-provenance.json`。",
        f"- 最终原生进程日志：`{log_path.relative_to(ROOT).as_posix()}`，SHA-256 `{digest(log_path)}`。",
        "- `evidence/preview-full-envelope.png`：完整屋顶外壳；`evidence/plan-dry-routes.png`：真实 NBT 路径。",
        "- 交付清单与压缩包指纹：`delivery-manifest.json`、`package-receipt.json`；ZIP 不包含开发缓存与测试世界。",
        "- 未修改核心/正式 mods/正式存档/并行房间，未调用付费生图；没有执行 FT 连接路线、战斗波次或奖励逻辑。",
        "- 未验完整整合包客户端/多人游玩/FPS；预览是原生 AABB 软件渲染而非实机截图，透明模型与方块实体动画的显示有限。",
        "- 日志中的单次四房模板施工/全量核验触发约 12.5 秒 behind 警告；不将此隔离 QA 宣称为性能达标或无卡顿。",
    ]
    (ROOT / "REPORT_FOR_COORDINATOR.md").write_text("\n".join(coordinator_lines) + "\n", encoding="utf-8")
    files = []
    for folder in (OUT, ROOT / "qa", ROOT / "evidence", ROOT / "tools", ROOT / "native-test/src", ROOT / "native-test/gradle"):
        files.extend(path for path in folder.rglob("*") if path.is_file() and "__pycache__" not in path.parts)
    files.extend(ROOT / name for name in ("README.md", "MATERIALS.md", "REPORT_FOR_COORDINATOR.md", "Run-QA.ps1", "native-test/build.gradle", "native-test/gradle.properties", "native-test/settings.gradle"))
    files = sorted(set(files))
    missing = [str(path) for path in files if not path.is_file()]
    if missing:
        raise ValueError("Missing package inputs: " + ", ".join(missing))
    manifest = {"id": "M09", "status": "independent-only-not-installed", "room_sha256": fingerprint, "files": [{"path": path.relative_to(ROOT).as_posix(), "bytes": path.stat().st_size, "sha256": digest(path)} for path in files]}
    dump(ROOT / "delivery-manifest.json", manifest)
    archive_path = ROOT / "M09-haunted-scriptorium-independent.zip"
    with zipfile.ZipFile(archive_path, "w", zipfile.ZIP_DEFLATED) as archive:
        for path in files + [ROOT / "delivery-manifest.json"]:
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive_path) as archive:
        if archive.testzip() is not None:
            raise ValueError("Archive CRC failure")
        for entry in manifest["files"]:
            if hashlib.sha256(archive.read(entry["path"])).hexdigest() != entry["sha256"]:
                raise ValueError("Archive fingerprint mismatch")
    dump(ROOT / "package-receipt.json", {"id": "M09", "status": "PASS", "archive": str(archive_path), "bytes": archive_path.stat().st_size, "sha256": digest(archive_path), "room_sha256": fingerprint, "manifest_sha256": digest(ROOT / "delivery-manifest.json"), "entries": len(files) + 1, "zip_crc_and_all_hashes": "PASS", "deployment": "not_installed"})
    print(f"M09 final QA PASS; template={fingerprint}; archive={digest(archive_path)}")


if __name__ == "__main__":
    finalize()
