from __future__ import annotations

import argparse
import collections
import copy
import hashlib
import json
from pathlib import Path

from build_s5 import DELIVERY, ROOT, construct, digest, dump, static_qa


def mutation_checks():
    folder = ROOT / "qa/negative-fixtures"
    folder.mkdir(parents=True, exist_ok=True)
    original = construct()
    cases = {
        "walkway_floor_hole": ((3, 2, 4), "air", {}),
        "blocked_north_opening": ((3, 4, 0), "obsidian", {}),
        "missing_rod_pedestal": ((1, 4, 5), "air", {}),
        "unstable_chorus_age": ((1, 4, 7), "chorus_flower", {"age": 0}),
        "eyed_portal_fragment": ((1, 4, 10), "end_portal_frame", {"facing": "north", "eye": True}),
        "missing_roof_tile": ((3, 7, 4), "air", {}),
        "unsafe_fluid_in_path": ((3, 3, 4), "lava", {"level": 0}),
    }
    from nbtio import state

    results = []
    for name, (position, block, properties) in cases.items():
        modified = copy.deepcopy(original)
        modified.set(position, state(block, **properties))
        path = folder / (name + ".nbt")
        modified.write(path)
        try:
            static_qa(path)
        except (AssertionError, ValueError) as failure:
            results.append({"case": name, "status": "REJECTED_AS_EXPECTED", "fixture_sha256": digest(path), "diagnostic": str(failure)})
        else:
            raise AssertionError("Safety validator missed mutation: " + name)
    result = {"status": "PASS", "scope": "Seven intentionally broken independent fixtures; no delivery or reference file modified", "cases": results}
    dump(ROOT / "qa/negative-checks.json", result)
    return result


def finalize():
    report_path = DELIVERY / "qa.json"
    report = json.loads(report_path.read_text(encoding="utf-8"))
    native_path = ROOT / "qa/native-placement.json"
    native = json.loads(native_path.read_text(encoding="utf-8"))
    console_path = ROOT / "qa/forge-native-console.log"
    console = console_path.read_text(encoding="utf-8")
    hash_value = digest(DELIVERY / "room.nbt")
    assert native["status"] == "PASS" and native["structure_sha256"] == hash_value
    assert native["minecraft"] == "1.20.1" and native["forge"] == "47.4.10"
    assert "All 1 required tests passed" in console and "BUILD SUCCESSFUL" in console and "S5_NATIVE_PLACEMENT_PASS" in console
    structure, reachable, checked = static_qa(DELIVERY / "room.nbt")
    assert checked == report["static"] and len(reachable) == 36
    for name, fingerprint in report["files"].items():
        assert digest(DELIVERY / name) == fingerprint["sha256"]
    for source in report["source_files"]:
        assert digest(Path(source["path"])) == source["sha256"]
    negative = mutation_checks()
    report["status"] = "PASS"
    report["native"] = {
        "status": "PASS", "report": str(native_path), "sha256": digest(native_path),
        "console": str(console_path), "console_sha256": digest(console_path),
        "server_log": str(ROOT / "qa/forge-run/logs/latest.log"),
        "server_log_sha256": digest(ROOT / "qa/forge-run/logs/latest.log"),
        "minecraft": native["minecraft"], "forge": native["forge"],
        "actual_world_placements": native["actual_world_placements"],
        "rotations": 4, "post_placement_server_ticks": native["post_placement_server_ticks"],
        "compared_states_per_rotation_per_phase": native["roundtrip_records"],
        "scope": native["scope"],
    }
    report["negative_checks"] = {"status": negative["status"], "rejected_cases": len(negative["cases"]), "path": str(ROOT / "qa/negative-checks.json"), "sha256": digest(ROOT / "qa/negative-checks.json")}
    report["materials"] = dict(sorted(collections.Counter(block.name for block in structure.blocks.values() if block.name != "minecraft:air").items()))
    report["software_preview_visually_reviewed"] = True
    report["acceptance_limits"] = [
        "No full-modpack client screenshot, player input playthrough, multiplayer/FPS or boss/reward test was performed.",
        "No coordinator catalog slot was assigned; final runtime fingerprint, lobby connector, occupation rejection and persistent build markers remain integration responsibilities.",
        "Eight native placeInWorld calls test repeat template placement, NOT the production generator's persisted once-only behavior.",
        "Only the independent project's disposable GameTest world was written. No deployment, formal save edit, source-core edit, or paid generation call was performed.",
    ]
    dump(report_path, report)
    manifest = {
        "id": "S5", "status": "PASS", "delivery_directory": str(DELIVERY),
        "files": {name: {"path": str(DELIVERY / name), "sha256": digest(DELIVERY / name), "bytes": (DELIVERY / name).stat().st_size} for name in ("room.nbt", "room.json", "preview.png", "qa.json")},
        "structure_sha256": hash_value,
        "structure_semantic_sha256": report["structure_semantic_sha256"],
        "delivery_fingerprint": report["delivery_fingerprint"],
        "fingerprint_scope": report["fingerprint_scope"],
        "native_evidence": report["native"], "deployment": "NOT_DEPLOYED", "formal_save_modified": False,
        "target_integration_thread": "01a19758-b0e5-75c2-822a-4860527a1e8b",
    }
    dump(ROOT / "delivery-manifest.json", manifest)
    handoff = f"""# S5 末地虚空回廊 · 独立交付报告

状态：静态 QA、7 个破坏性负例、Minecraft 1.20.1 / Forge 47.4.10 实际四向放置 QA 均通过。未部署、未写正式存档、未改核心或其它房间。

## 交付

- 目录：`{DELIVERY}`
- 四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`
- 尺寸 XYZ：7×8×13；北入口脚部 [3,3,0]；安全内点脚部 [3,3,10]；朝向 north。
- 模板本体 Y=2 地板，Y=3 脚部，Y=7 完整封顶。入口净宽/高 3×3，地板必须保留；连接道路要适配脚部高度 +3。
- 保留 5×11、净高 4 的设计内腔，两侧陈设使净主路维持三格。两组玻璃封护裂隙、六根有支撑末地烛、两朵 age=5 紫颂花、两块 eye=false 门框残片。
- 728 条显式方块记录（包括空气）、36 格双向安全地面；无奖励库存、容器、流体、实体、方块实体或可激活传送门。

## 指纹

- 原始 S5 参考 PNG：`dc508de8c23a414740405a67eb1169553a3337b398dbdee54297ab9bdcf73bcf`
- gzip 结构 SHA-256：`{hash_value}`
- 解压 NBT SHA-256：`{report['structure_uncompressed_sha256']}`
- 规范方块语义 SHA-256：`{report['structure_semantic_sha256']}`
- 独立交付指纹：`{report['delivery_fingerprint']}`
- room.json SHA-256：`{digest(DELIVERY / 'room.json')}`
- preview.png SHA-256：`{digest(DELIVERY / 'preview.png')}`
- qa.json SHA-256：`{digest(report_path)}`

独立交付指纹的载荷与算法见 qa.json，不等同于依赖 slot 的 DungeonRoomCatalog 运行时指纹。未擅自选择目录槽位；由唯一接入负责人计算最终 slot/origin/fingerprint。

## 原生证据

- `{native_path}`：四种 rotation，各两次 StructureTemplate.placeInWorld（共8次），邻居更新后和100 server tick后逐一比较728状态；原生 canSurvive、0.6×1.8 玩家碰撞体、三格连续干燥接近区/通路、非入口护边、玻璃地板及末地烛/紫颂花支撑全部通过。
- `{console_path}`：`S5_NATIVE_PLACEMENT_PASS`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。
- `{ROOT / 'qa/forge-run/logs/latest.log'}`：原生服务器日志。
- `{ROOT / 'qa/negative-checks.json'}`：地板缺口、入口阻挡、末地烛失去基座、活跃紫颂花、带眼门框、屋顶缺块、通路熔岩七个破坏性负例均被拒绝。
- `{ROOT / 'delivery-manifest.json'}`：全部最终文件哈希和证据哈希。

## 必须如实转交的边界

参考是 image2.5 七次失败后的本地 THREE 方块示意渲染，不是 AI 成功图，也不是实机证据。preview 是从最终 NBT 读取的原版纹理软件预览，仅预览移除屋顶和近侧遮挡，NBT 本体完整封护。

原生验收在独立 GameTest 世界完成，不冒充完整整合包客户端实机游玩、多人/FPS或大厅接入验收。原生重复放置不等于核心生成标记的幂等验收。未加入 catalog、incoming 或已部署 JAR，未修改门/火盆/五档/玩家返回记录。后续由接入线程 `01a19758-b0e5-75c2-822a-4860527a1e8b` 追加固定槽位与道路、复验占用拒绝和生成标记，再协调部署。
"""
    (ROOT / "HANDOFF.md").write_text(handoff, encoding="utf-8")
    print(json.dumps({"status": "PASS", "sha256": hash_value, "delivery_fingerprint": report["delivery_fingerprint"], "negative_cases": 7, "native_rotations": 4, "manifest": str(ROOT / "delivery-manifest.json")}, ensure_ascii=False))


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("--negative-only", action="store_true")
    arguments = parser.parse_args()
    if arguments.negative_only:
        mutation_checks()
    else:
        finalize()
