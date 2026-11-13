from __future__ import annotations

import json
import zipfile
from datetime import datetime, timedelta, timezone

from PIL import Image

from build_m11 import DELIVERY, REFERENCE, REFERENCE_SHA, ROOT, digest, write_json


def load(path):
    return json.loads(path.read_text(encoding="utf-8"))


def main():
    metadata = load(DELIVERY / "room.json")
    static = load(ROOT / "qa/static.json")
    native = load(ROOT / "qa/native.json")
    render = load(ROOT / "qa/render.json")
    structure_hash = digest(DELIVERY / "room.nbt")
    assert metadata["sha256"] == static["sha256"] == native["sha256"] == render["structure_sha256"] == structure_hash
    assert all(report["status"] == "PASS" for report in (static, native, render))
    assert native["rotations"] == len(native["initial"]) == len(native["after_400_ticks"]) == 4
    assert all(entry["records"] == 3060 and entry["supported_skulls"] == 14 and entry["roof_full_blocks"] == 255 and entry["landmark_routes"] == 6 for entry in native["after_400_ticks"])
    assert digest(REFERENCE) == metadata["reference_sha256"] == REFERENCE_SHA
    assert render["preview_sha256"] == digest(DELIVERY / "preview.png")
    with Image.open(DELIVERY / "preview.png") as preview:
        assert preview.size == (1536, 1024)
    log = (ROOT / "qa/gametest.log").read_text(encoding="utf-8", errors="replace")
    assert "M11_NATIVE_GAME_TEST_PASS" in log and "BUILD SUCCESSFUL" in log
    timestamp = datetime.now(timezone(timedelta(hours=8), name="Asia/Shanghai")).isoformat(timespec="seconds")
    hashes = {name: digest(DELIVERY / name) for name in ("room.nbt", "room.json", "preview.png")}
    report = {
        "id": "M11",
        "name": "白骨祷室",
        "status": "PASS",
        "timestamp": timestamp,
        "reference": {"path": str(REFERENCE), "sha256": REFERENCE_SHA, "dimensions": [1536, 1024], "paid_regeneration": False},
        "artifacts_sha256": hashes,
        "static": static,
        "native": native,
        "render": render,
        "visual_review": {
            "status": "PASS",
            "reviewed": ["delivery/M11/preview.png", "qa/four-views.png", "qa/exterior.png", "qa/floorplan.png"],
            "landmarks": ["石砌墙面与承重拱肋", "14 颗带支撑的骷髅头与骨块壁龛", "红布祭坛与空仪式盆", "5 面带骷髅纹的红旗", "15 组蜡烛与 5 盏灯", "屋顶梁锚固的中央吊灯", "完整屋顶与北侧三格接口"],
            "limits": "软件预览，不是整合包游戏截图；吊灯是原版方块转译，旗布静态、无粒子。"
        },
        "scope": {
            "writes": str(ROOT), "deployment": False, "production_save_used": False,
            "core_modified": False, "other_rooms_modified": False, "shared_incoming_modified": False,
            "image_connection_modified": False, "full_modpack_or_multiplayer_playtest": False,
            "integration_required": "由唯一核心负责人分配槽位和道路；完整体积/实体/边界预检，占用拒绝，持久标记与幂等保留。"
        },
        "attempts": [
            {"id": 1, "status": "STOPPED", "reason": "ForgeGradle 公网证书前置探测 SSL 握手阻塞；确认 M11 daemon currentDir 后只终止该进程；未执行放置", "log": "qa/gametest-attempt1-network-probe-stopped.log"},
            {"id": 2, "status": "PASS", "reason": "离线缓存运行，-Dnet.minecraftforge.gradle.check.certs=false 仅跳过 ForgeGradle 环境联网探测", "log": "qa/gametest.log"}
        ]
    }
    write_json(DELIVERY / "qa.json", report)
    handoff = {
        "id": "M11", "status": "READY_FOR_INTEGRATION_NOT_DEPLOYED", "directory": str(DELIVERY),
        "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"],
        "facing": "north", "source_thread": "01a157ef-83ef-7e51-8671-2d7f9db88086",
        "suggested_resource_id": metadata["resource_id_suggestion"], "reference_sha256": REFERENCE_SHA,
        "files": {name: {"bytes": (DELIVERY / name).stat().st_size, "sha256": digest(DELIVERY / name)} for name in ("room.nbt", "room.json", "preview.png", "qa.json")},
        "evidence": {name: digest(ROOT / name) for name in ("qa/static.json", "qa/native.json", "qa/render.json", "qa/four-views.png", "qa/exterior.png", "qa/floorplan.png", "qa/gametest.log")},
        "slot_or_world_origin_assigned": False, "deployed": False,
        "restrictions": ["仅交付独立目录，未写统一 incoming/catalog", "不能因显式空气记录而清空占用", "不得重建已有房间或覆盖玩家修改", "不得将测试用 verification mod 部署到整合包"]
    }
    write_json(ROOT / "handoff.json", handoff)
    members = [ROOT / "README.md", ROOT / "handoff.json", ROOT / "build.gradle", ROOT / "settings.gradle", ROOT / "gradle.properties"]
    members.extend(sorted(DELIVERY.glob("*")))
    members.extend(ROOT / name for name in ("qa/static.json", "qa/native.json", "qa/render.json", "qa/materials.json", "qa/four-views.png", "qa/exterior.png", "qa/floorplan.png", "qa/cutaway.png", "qa/gametest.log", "qa/gametest-attempt1-network-probe-stopped.log"))
    members.extend(sorted((ROOT / "qa").glob("view-*.png")))
    members.extend(sorted((ROOT / "tools").glob("*.py")))
    members.append(ROOT / "tools/M11Verification.java")
    members.extend(sorted(path for path in (ROOT / "tools/verification-resources").rglob("*") if path.is_file()))
    archive_path = ROOT / "M11-bone-prayer-chamber-handoff.zip"
    with zipfile.ZipFile(archive_path, "w", compression=zipfile.ZIP_DEFLATED, compresslevel=8) as archive:
        for path in members:
            assert path.is_file(), path
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive_path) as archive:
        assert archive.testzip() is None
        assert all(archive.read("delivery/M11/" + name) == (DELIVERY / name).read_bytes() for name in ("room.nbt", "room.json", "preview.png", "qa.json"))
    write_json(ROOT / "package-receipt.json", {"status": "PASS", "timestamp": timestamp, "archive": str(archive_path), "bytes": archive_path.stat().st_size, "sha256": digest(archive_path), "members": len(members), "delivery_hashes": handoff["files"], "archive_byte_comparison": "PASS", "excludes": ["test world", "Gradle caches", "build classes", "production saves", "secrets"]})
    print(json.dumps({"status": "PASS", "nbt_sha256": structure_hash, "archive_sha256": digest(archive_path), "archive": str(archive_path)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
