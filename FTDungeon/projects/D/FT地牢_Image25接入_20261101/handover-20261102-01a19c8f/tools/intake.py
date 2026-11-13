import argparse
import hashlib
import json
import re
import shutil
import sys
import zipfile
from datetime import datetime, timedelta, timezone
from functools import lru_cache
from pathlib import Path

from PIL import Image


HOME = Path(__file__).resolve().parents[1]
PROJECT = HOME.parent
WORK = PROJECT.parent
INSTANCE = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2")
REFINEMENT = Path("C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/02/01a19b79-d8b2-7820-86c4-3d7106853139/dungeon-refinement")
sys.path.insert(0, str(INSTANCE / "output/dungeon-expansion-20261101/tools"))
from nbtio import Structure, plain


OLD_IDS = ("L1", "L2", "M1", "M2", "S1", "S2", "L3", "L4", "S3")
NEW_IDS = (
    "M5", "M6", "M7", "S5", "S6", "S7", "SHOP_SUPPLY_DSH",
    "M07", "M08", "M09", "M10", "M11", "M12", "R1", "R2", "R3", "R4", "R5", "R6",
    "STAIR_A", "STAIR_B", "STAIR_C_RUINED", "STAIR_D_NETHER_HALL",
    "SHOP_A_RUINED", "SHOP_B_MONSTER", "SHOP_C_END_VOID", "SHOP_D_NETHER_BARTER",
    "SHOP_E_GOBLIN_MARKET", "SHOP_F_DARK_IRON", "SHOP_G_FROST", "SHOP_H_SWAMP_WITCH", "SHOP_I_UNDEAD_PAWN",
)
IGNORED = {"qa", "history", "baseline-v1", "baseline", "seed", "build", "backup", "release", "rejected", "rejected-attempt-1", "four-view-review"}
PRODUCERS = {
    "M5": "01a199f6-9ffe-7260-af60-45db52b6a693",
    "M6": "01a199f6-a691-7312-9440-a9be6f3c0ab0",
    "M7": "01a199f6-abfc-72c0-8d71-314558cdc7ae",
    "S5": "01a199f6-b146-7222-91d2-668afe1a599b",
    "S6": "01a199f6-b679-7e22-8e02-bf0bb13a4fef",
    "S7": "01a199f6-be36-7a92-805f-eaf567fe3637",
    "SHOP_SUPPLY_DSH": "01a199f3-e330-70f2-8a3f-9c19bc99438b",
    "M07": "01a19a76-8c7d-7a93-ba93-6f7f326e96e1",
    "M10": "01a19a76-9c87-7842-957f-23eb738f801a",
    "R2": "01a19a76-b2b2-7211-a399-dd2a679ba537",
    "R4": "01a19a77-6abb-73a2-b22c-26f6c4abd789",
    "STAIR_C_RUINED": "01a19a77-d8b0-7241-9e4a-bddfeb9f1e31",
}
for room_id in OLD_IDS:
    PRODUCERS[room_id] = "01a19b90-77e4-7940-82d5-8e0245ff4fbc" if room_id.startswith("L") else "01a19b90-80b6-75e3-951d-e094906fffdd" if room_id.startswith("M") else "01a19b90-8521-77f2-b648-2db7dbdc6ac1"


def load(path):
    return json.loads(Path(path).read_text(encoding="utf-8-sig"))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def file_record(path):
    path = Path(path)
    return {"path": str(path.resolve()), "sha256": digest(path), "bytes": path.stat().st_size}


def png_record(path):
    result = file_record(path)
    with Image.open(path) as picture:
        result["dimensions"] = list(picture.size)
        picture.verify()
    return result


def installed():
    jars = sorted((INSTANCE / "mods").glob("FT地牢怪物-*.jar"))
    if len(jars) != 1:
        raise ValueError("Expected exactly one active FT dungeon JAR")
    jar = jars[0]
    rooms = {}
    with zipfile.ZipFile(jar) as archive:
        catalog = json.loads(archive.read("data/ftdungeonmobs/dungeon_rooms/catalog.json").decode("utf-8-sig"))
        for entry in catalog["rooms"]:
            room_id = entry["id"]
            metadata = json.loads(archive.read(f"data/ftdungeonmobs/dungeon_rooms/image25/{room_id.lower()}.json").decode("utf-8-sig"))
            nbt = archive.read(f"data/ftdungeonmobs/structures/image25/{room_id.lower()}.nbt")
            sha256 = hashlib.sha256(nbt).hexdigest()
            if sha256 != metadata["sha256"]:
                raise ValueError("Installed JAR metadata/NBT mismatch: " + room_id)
            rooms[room_id] = {**entry, "sha256": sha256, "metadata": metadata}
    return {**file_record(jar), "rooms": rooms}


def sources():
    result = {}
    roots = sorted(path for path in WORK.glob("FT地牢_*") if path.is_dir() and path != PROJECT)
    roots += [WORK / "M7_暗铁献祭地窖_20261102", WORK / "S7-Dark-Iron-Cell-20261102"]
    for root in roots:
        for metadata_path in root.rglob("room.json"):
            relative = metadata_path.relative_to(root)
            if any(part in IGNORED or part.startswith("native-") for part in relative.parts[:-1]):
                continue
            metadata = load(metadata_path)
            if metadata.get("id") not in NEW_IDS or not metadata_path.with_name("room.nbt").is_file():
                continue
            result.setdefault(metadata["id"], []).append((root, metadata_path.parent))
    for room_id in OLD_IDS:
        result[room_id] = [(REFINEMENT, REFINEMENT / "candidate/incoming" / room_id)]
    return result


def aggregate_qa(root, folder):
    local = folder / "qa.json"
    if local.is_file():
        return local, load(local), "room-local"
    aggregate = root / "qa/refinement-audit.json"
    if aggregate.is_file():
        return aggregate, load(aggregate), "aggregate-refinement-audit; original file preserved without per-room rewriting"
    return None, {}, "missing"


@lru_cache(maxsize=None)
def evidence(root):
    result = []
    for path in sorted(root.rglob("*.json")):
        relative = path.relative_to(root)
        if len(relative.parts) > 4 or any(part in {"history", "build", "native-classes", "world"} or part.startswith("forge-run") for part in relative.parts):
            continue
        lowered = str(relative).lower()
        if not any(term in lowered for term in ("qa", "handoff", "manifest", "checksum", "receipt", "visual")) or path.stat().st_size > 1000000:
            continue
        try:
            value = load(path)
        except (ValueError, UnicodeError):
            continue
        status = value.get("status", value.get("passed")) if isinstance(value, dict) else None
        result.append({**file_record(path), "reported_status": status})
    return result


def inspect(folder, metadata):
    structure = Structure.read(folder / "room.nbt")
    bounds = all(all(0 <= coordinate < structure.size[axis] for axis, coordinate in enumerate(position)) for position in structure.blocks)
    forbidden = {"minecraft:" + name for name in ("spawner", "command_block", "chain_command_block", "repeating_command_block", "structure_block", "jigsaw")}
    inventory = []
    books = []
    for position, entity in structure.block_entities.items():
        content = plain(entity)
        if content.get("Items") or content.get("LootTable") or content.get("RemainingStock", 0):
            inventory.append({"position": list(position), "content": content})
        if content.get("Book"):
            books.append({"position": list(position), "id": content["Book"].get("id")})
    checks = {
        "metadata_sha256": digest(folder / "room.nbt") == metadata.get("sha256"),
        "size": list(structure.size) == metadata.get("size"),
        "bounds_unique_positions": bounds,
        "data_version_3465": structure.data_version == 3465,
        "id_syntax": bool(re.fullmatch(r"[A-Z][A-Z0-9_]{0,31}", metadata.get("id", ""))),
        "north_interface_metadata": metadata.get("facing") == "north" and metadata.get("entrance", [0, 0, -1])[2] == 0,
        "no_entities": not structure.entities,
        "no_forbidden_blocks": not any(block.name in forbidden for block in structure.blocks.values()),
        "no_reward_inventory": not inventory,
    }
    return {
        "status": "PASS_INTAKE_ONLY" if all(checks.values()) else "FAIL", "checks": checks,
        "block_records": len(structure.blocks), "block_entities": len(structure.block_entities),
        "lectern_books": books, "inventory_findings": inventory,
        "modded_blocks": sorted({block.name for block in structure.blocks.values() if not block.name.startswith("minecraft:")}),
        "scope": "Receipt checks only; not native support, walking, liquid dynamics, roof integrity, visual approval or deployment.",
    }


def group_name(room_id):
    return "halls" if room_id.startswith("L") else "ritual-vault" if room_id.startswith("M") else "side-rooms"


def receive(root, folder, room_id, slot, run):
    metadata = load(folder / "room.json")
    sha256 = digest(folder / "room.nbt")
    qa_path, producer_qa, qa_scope = aggregate_qa(root, folder)
    reference = Path(metadata["reference"])
    reference_record = png_record(reference)
    expected_reference = metadata.get("reference_sha256") or producer_qa.get("reference_sha256")
    record = {
        "id": room_id, "name": metadata["name"], "slot": slot,
        "slot_scope": "unchanged installed slot; candidate replacement not authorized" if room_id in OLD_IDS else "coordinator reservation only; not in runtime catalog or connected to hall",
        "template_id": "ftdungeonmobs:image25/" + room_id.lower(),
        "candidate_sha256": sha256, "source_folder": str(folder), "source_root": str(root),
        "producer_thread": PRODUCERS.get(room_id), "producer_thread_identity_status": "historical thread evidence" if room_id in PRODUCERS else "not independently bound; do not create duplicate producer",
        "metadata": {key: metadata[key] for key in ("size", "entrance", "interior", "facing")},
        "reference": {**reference_record, "expected_sha256": expected_reference, "matches_declared": reference_record["sha256"] == expected_reference if expected_reference else None},
        "design_fingerprint": {"producer_thread": PRODUCERS.get(room_id), "png_path": str(reference), "png_sha256": reference_record["sha256"], "source_revision": "observed-final-NBT:" + sha256, "dsh_session": None},
        "producer_qa": {"path": str(qa_path) if qa_path else None, "status": producer_qa.get("status"), "scope": qa_scope},
        "package_scope": "Four producer room-local files" if qa_scope == "room-local" else "Three producer room files plus an explicitly labeled unchanged aggregate QA snapshot; no producer room-local QA is fabricated",
        "evidence": evidence(root), "receiver_static": inspect(folder, metadata),
        "four_views": {"status": "PENDING_RECEIVER_REVIEW", "producer_images_are_not_receiver_approval": True},
        "receiver_retest": {"status": "NOT_RUN"}, "ready_for_unified_integration": False,
        "deployed": False, "hard_blockers": [], "notes": [],
    }
    if reference_record["sha256"] != expected_reference and expected_reference:
        record["hard_blockers"].append("REFERENCE_HASH_MISMATCH")
    if record["receiver_static"]["status"] == "FAIL":
        record["hard_blockers"].append("RECEIVER_INTAKE_CHECK_FAILED")
    if room_id in OLD_IDS:
        group_folder = REFINEMENT / "groups" / group_name(room_id) / "incoming" / room_id
        record["current_group_matches_aggregate"] = all(digest(folder / name) == digest(group_folder / name) for name in ("room.nbt", "room.json"))
        if not record["current_group_matches_aggregate"]:
            record["hard_blockers"].append("REFINEMENT_GROUP_AGGREGATE_DIVERGENCE")
        record["notes"].append("Latest refinement selected, not installed baseline or older support-only revision; preserve old generated-world fingerprints, never force rebuild.")
    if room_id.startswith("SHOP_"):
        record["hard_blockers"].append("CORE_SHOP_NO_SPAWNS_POLICY_NOT_ACCEPTED")
        record["notes"].append("Runtime core accepts vanilla blocks only; shop no-spawn behavior requires implemented, tested policy, not metadata alone.")
    if record["receiver_static"]["modded_blocks"]:
        record["hard_blockers"].append("MODDED_PALETTE_REQUIRES_CORE_ALLOWLIST_AND_EXACT_DEPENDENCY")
    if room_id.startswith("STAIR_"):
        record["hard_blockers"].append("MULTILEVEL_PORTS_NOT_CONNECTED_OR_TESTED_IN_CORE")
    if room_id == "S5":
        record["notes"].append("Reference is a local THREE render, not a successful image2.5 generation.")
    snapshot = HOME / "incoming" / room_id / sha256
    snapshot.mkdir(parents=True, exist_ok=True)
    inputs = [(folder / "room.nbt", "room.nbt"), (folder / "room.json", "room.json"), (folder / "preview.png", "preview.png")]
    if qa_path:
        inputs.append((qa_path, "qa.json"))
    copied = {}
    for source, name in inputs:
        target = snapshot / name
        if target.exists() and digest(target) != digest(source):
            raise ValueError("Refusing to replace changed intake snapshot: " + str(target))
        if not target.exists():
            shutil.copy2(source, target)
        copied[name] = png_record(source) if name == "preview.png" else file_record(source)
    if len(copied) != 4:
        record["hard_blockers"].append("FOUR_FILE_PACKAGE_INCOMPLETE")
    record["snapshot"] = str(snapshot)
    record["files"] = copied
    save(run / "receipts" / (room_id + ".json"), record)
    return record


def historical_versions(runtime):
    result = []
    for room_id in OLD_IDS:
        for label, folder in (
            ("legacy-incoming", PROJECT / "incoming" / room_id),
            ("support-only-candidate", PROJECT / "four-view-review/candidate/incoming" / room_id),
            ("legacy-corrected-render", PROJECT / "four-view-review/corrected" / room_id),
        ):
            metadata = load(folder / "room.json")
            result.append({"id": room_id, "stage": label, "path": str(folder), "sha256": digest(folder / "room.nbt"), "matches_installed": digest(folder / "room.nbt") == runtime["rooms"][room_id]["sha256"], "not_selected": True, "declared_sha256": metadata.get("sha256")})
    for room_id in NEW_IDS:
        folder = PROJECT / "incoming" / room_id
        if (folder / "room.nbt").is_file():
            result.append({"id": room_id, "stage": "prior-central-receipt-not-runtime-import", "path": str(folder), "sha256": digest(folder / "room.nbt"), "not_selected": True})
    return result


def isolated_v2():
    root = INSTANCE / "output/dungeon-interiors-20261101-v2"
    build = load(root / "build-report.json")
    result = {"scope": "Separate reference-based world project; NEVER equate FT_R_* or named rooms with image25 IDs or slots.", "root": str(root), "reported_total_rooms": build.get("total_rooms"), "rooms": [], "reports": [], "receiver_approved": False, "not_imported": True}
    for room in load(root / "review-rooms.json"):
        result["rooms"].append({"key": room["key"], **file_record(root / "blueprints" / room["file"])})
    for name in ("build-report.json", "layout-validation.json", "native-review-result.json", "native-stability-result.json", "blockstate-audit.json"):
        path = root / name
        value = load(path)
        result["reports"].append({**file_record(path), "reported_passed": value.get("passed"), "scope": value.get("scope")})
    result["notes"] = ["12 named review templates / 16-room staged map. Historical result says 12 rooms, 575936 records and 40 ticks; not this receiver's rerun.", "Native summary has no per-template SHA binding, no unified deployment receipt or completed final-world validation was established in this intake."]
    return result


def apply_receiver_reviews(records):
    for record in records:
        review = HOME / "reviews" / record["id"] / record["candidate_sha256"] / "acceptance.json"
        if not review.is_file():
            continue
        decision = load(review)
        if decision.get("sha256") != record["candidate_sha256"]:
            raise ValueError("Receiver review fingerprint mismatch")
        record["four_views"] = {"status": decision.get("four_view_status"), "evidence": file_record(review)}
        record["receiver_retest"] = {"status": decision.get("retest_status"), "evidence": decision.get("retest_evidence")}
        record["hard_blockers"] += decision.get("hard_blockers", [])
        record["ready_for_unified_integration"] = all((decision.get("four_view_status") == "PASS", decision.get("retest_status") == "PASS", record["receiver_static"]["status"] == "PASS_INTAKE_ONLY", not record["hard_blockers"]))


def classification(records):
    return {
        "received": [record["id"] for record in records],
        "pending_four_views": [record["id"] for record in records if record["four_views"]["status"] != "PASS"],
        "pending_receiver_retest": [record["id"] for record in records if record["receiver_retest"]["status"] != "PASS"],
        "ready_for_unified_integration": [record["id"] for record in records if record["ready_for_unified_integration"]],
        "blocked": [record["id"] for record in records if record["hard_blockers"]],
    }


def verify_registry(registry):
    records = registry["rooms"]
    assert len({record["id"] for record in records}) == len(records)
    assert len({record["slot"] for record in records}) == len(records)
    assert len({record["template_id"] for record in records}) == len(records)
    assert next(record for record in records if record["id"] == "M07")["slot"] != next(record for record in records if record["id"] == "M7")["slot"]
    for record in records:
        assert record["candidate_sha256"] == digest(Path(record["snapshot"]) / "room.nbt")
        for name, information in record["files"].items():
            assert information["sha256"] == digest(information["path"]), (record["id"], "source changed", name)
            assert information["sha256"] == digest(Path(record["snapshot"]) / name), (record["id"], "snapshot changed", name)
        assert digest(record["reference"]["path"]) == record["reference"]["sha256"]
        for information in record["evidence"]:
            assert digest(information["path"]) == information["sha256"], (record["id"], information["path"])
        if record["id"] in registry["installed"]["rooms"]:
            assert record["slot"] == registry["installed"]["rooms"][record["id"]]["slot"]
        if record["four_views"]["status"] == "PASS" or record["receiver_retest"]["status"] == "PASS":
            acceptance_information = record["four_views"]["evidence"]
            assert digest(acceptance_information["path"]) == acceptance_information["sha256"]
            acceptance = load(acceptance_information["path"])
            assert acceptance["sha256"] == record["candidate_sha256"] and acceptance["id"] == record["id"]
            assert acceptance["four_view_status"] == record["four_views"]["status"]
            assert acceptance["retest_status"] == record["receiver_retest"]["status"]
            assert acceptance["retest_evidence"] == record["receiver_retest"]["evidence"]
            for key in ("retest_evidence", "render_manifest"):
                information = acceptance[key]
                assert digest(information["path"]) == information["sha256"]
            result = load(acceptance["retest_evidence"]["path"])
            assert result["status"] == "PASS" and result["sha256"] == record["candidate_sha256"]
            for key in ("static", "native", "log", "invocation"):
                information = result[key]
                assert digest(information["path"]) == information["sha256"]
            manifest = load(acceptance["render_manifest"]["path"])
            assert manifest["sha256"] == record["candidate_sha256"] and len(manifest["images"]) == 10
            for information in manifest["images"] + acceptance["actually_opened_images"]:
                assert digest(information["path"]) == information["sha256"]
        if record["ready_for_unified_integration"]:
            assert not record["hard_blockers"] and record["four_views"]["status"] == record["receiver_retest"]["status"] == "PASS"
    assert classification(records) == registry["classification"]
    assert digest(registry["installed"]["path"]) == registry["installed"]["sha256"]
    return {"status": "PASS", "selected_room_count": len(records), "unique_ids_slots_template_ids": True, "source_snapshot_reference_evidence_and_runtime_hashes_unchanged": True, "counts": {key: len(value) for key, value in registry["classification"].items()}}


def write_report(run, registry):
    categories = registry["classification"]
    lines = ["# DSH 地牢统一交接阶段盘点", "", "盘点时间：" + registry["created_at"], "", "## 结论", "", "41 个逻辑房间：32 个独立新房间 + 9 个最新美化替换候选。只冻结收件、登记和审核，不是导入/部署。", "", "五类是可重叠的验收轴；阻断项仍计入待四视图/待复验，不以 completed 或历史 PASS 充当接入成功。", ""]
    for key, title in (("received", "已接收"), ("pending_four_views", "待四视图"), ("pending_receiver_retest", "待复验"), ("ready_for_unified_integration", "可统一接入候选"), ("blocked", "明确接入阻断")):
        lines += [f"- {title}：{len(categories[key])}；" + (" / ".join(categories[key]) or "无")]
    lines += ["", "## 正式版本与历史差异", "", "当前真实已装：" + registry["installed"]["path"], "", "JAR SHA-256：`" + registry["installed"]["sha256"] + "`", "", "JAR 内目录只有旧九间，槽位 0–8；逐项元数据与压缩 NBT 哈希一致。旧 README/收据的 0.17.0 是历史版本，不能代表当前 0.20.0。新版九间、第三批和各独立房间没有因此接入。", "", "旧 queue/batch3.json 的 QUEUED_NOT_DISPATCHED 已滞后：六间实际交付均找到；旧文件原样保留，使用本登记清单的收件事实，不重发制作。M6 原 incoming 是同哈希收件，不重复导入。M7 与 M07 永远保留不同字符串 ID。", "", "美化九间使用 01a19b79.../dungeon-refinement/candidate；四个更早支撑修正版及全部旧哈希只记历史，不作为最新候选。", "", "## 候选槽位", "", "0–8 继承已装；9–14 第三批；15 补给店；16–21 M07–M12；22–27 R1–R6；28–31 楼梯；32–40 商店 A–I。所有新槽位仅在本账本预留，没有写运行时目录、SavedData 或大厅道路。", "", "| ID | 槽位 | NBT SHA-256 | 四视图 | 接收者复验 | 接入阻断 |", "| --- | ---: | --- | --- | --- | --- |"]
    for record in registry["rooms"]:
        lines.append(f"| {record['id']} | {record['slot']} | `{record['candidate_sha256']}` | {record['four_views']['status']} | {record['receiver_retest']['status']} | {'; '.join(record['hard_blockers']) or '无明确硬阻断，仍受验收门禁'} |")
    lines += ["", "## 证据与未闭环项", "", "- `registry.json`：全部来源、图片 SHA、NBT SHA、四件套原始哈希、QA 报告哈希、制作线程已知/待绑定身份、槽位、门禁。未知 DSH session 不编造。", "- `receipts/ID.json`：本次收件时逐间证据；`incoming/ID/完整SHA/` 为独立冻结副本，不改旧 incoming。", "- 商店 10 间：禁刷策略尚未被共享核心接收；补给商店还需交易桌精确依赖与核心原版方块白名单变更。仅填写 spawn_points=false 不算生效。", "- 楼梯 4 间：上/下层接口需独立连接与世界路线验证，普通单北入口展示不等于楼梯系统完成。", "- 所有候选进入共享核心前仍需大厅路线、整卷占用拒绝、持久指纹/幂等、旧世界不覆盖和所需依赖回归；部署需要报告候选 JAR 哈希与接收条件后另行授权。", "- `isolated-v2.json`：原参考地图 12 个审查模板 / 16 房暂存图，独立命名空间保留；历史 40 tick PASS 不混入本次 image25 接入。", "- 软件 NBT 渲染不是游戏截图；原生无界面测试不是整合包客户端/多人/FPS 验收。", "", "未修改正式源码、JAR、配置、资源包或玩家存档；未生图，未向不可用旧线程发送消息。"]
    (run / "REPORT.md").write_text("\n".join(lines) + "\n", encoding="utf-8")


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--verify", type=Path)
    args = parser.parse_args()
    if args.verify:
        print(json.dumps(verify_registry(load(args.verify)), ensure_ascii=False))
        return
    stamp = datetime.now(timezone(timedelta(hours=8))).strftime("%Y%m%d-%H%M%S")
    run = HOME / "runs" / stamp
    run.mkdir(parents=True, exist_ok=False)
    runtime = installed()
    expected_slots = {room_id: index for index, room_id in enumerate(OLD_IDS + NEW_IDS)}
    if any(runtime["rooms"][room_id]["slot"] != expected_slots[room_id] for room_id in OLD_IDS):
        raise ValueError("Installed slot order changed; rebase reservations explicitly")
    discovered = sources()
    records = []
    for room_id, slot in expected_slots.items():
        variants = discovered.get(room_id, [])
        hashes = {digest(folder / "room.nbt") for root, folder in variants}
        if len(hashes) != 1:
            raise ValueError(f"Missing or ambiguous active delivery for {room_id}: {variants}")
        root, folder = sorted(variants, key=lambda pair: (len(pair[1].parts), str(pair[1])))[0]
        record = receive(root, folder, room_id, slot, run)
        record["same_hash_aliases_not_reimported"] = [str(other) for source, other in variants if other != folder]
        records.append(record)
        print("RECEIVED", room_id, slot, record["candidate_sha256"], flush=True)
    apply_receiver_reviews(records)
    registry = {"schema_version": 1, "coordinator_thread": "01a19c8f-6747-7572-a77a-6e90084bbd34", "created_at": datetime.now(timezone(timedelta(hours=8))).isoformat(), "deployment_authorized": False, "installed": runtime, "rooms": records, "classification": classification(records)}
    save(run / "registry.json", registry)
    save(run / "queue.json", {"priority": ["M5", "M6", "M7", "S5", "S6", "S7", "M08", "M09", "M10", "M11", "M12", "R1", "R2", "R3", "R5", "R6", *OLD_IDS], "pending": [{"id": record["id"], "slot": record["slot"], "candidate_sha256": record["candidate_sha256"], "snapshot": record["snapshot"], "four_views": record["four_views"]["status"], "retest": record["receiver_retest"]["status"], "hard_blockers": record["hard_blockers"]} for record in records if not record["ready_for_unified_integration"]], "no_redispatch_or_duplicate_import": True})
    save(run / "historical-versions.json", historical_versions(runtime))
    save(run / "isolated-v2.json", isolated_v2())
    verification = verify_registry(registry)
    save(run / "verification.json", verification)
    write_report(run, registry)
    save(HOME / "latest.json", {"run": str(run), "registry": file_record(run / "registry.json"), "report": str(run / "REPORT.md"), "counts": verification["counts"]})
    print(json.dumps(verification, ensure_ascii=False))


if __name__ == "__main__":
    main()
