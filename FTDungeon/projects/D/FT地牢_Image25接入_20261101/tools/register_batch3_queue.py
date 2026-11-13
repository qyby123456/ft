import datetime
import hashlib
import json
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path("D:/ft工程文件/image2.5/dungeon-design-batch3-20261101")
EXPECTED = {
    "M5": "dcd0ffbe2727cc55608994a09e224cac2a42d78bd1b60aca13d399d6cf858cae",
    "M6": "4fbbc2048340fbff123189e5df3bc011b8c5ba1fedef83a0950f2795113e0c91",
    "M7": "c9b49119f192c80c1d8aca9299efc8787ec1d7cafdb342745c78fea180f057be",
    "S5": "dc508de8c23a414740405a67eb1169553a3337b398dbdee54297ab9bdcf73bcf",
    "S6": "cd80e2ae91d8d5b47d80243422a87abcab5e0d68d8fda13d9c41365fea2b15a7",
    "S7": "6b5857d5b06a07ab67343b8faf1fc353802d56e14a24801681ab14ba94c8015e",
}
NOTES = {
    "M5": "末地观星台：保持连续安全地面；破损门仅为装饰，不放活动末地传送门或可爆炸末地水晶实体。水晶使用紫晶与玻璃转译，紫颂植物必须有原版合法支撑。",
    "M6": "地狱烈焰堡垒：熔岩瀑布有底与护面；魂火下方用灵魂土/灵魂沙，木桥不穿火源或裸露熔岩。吊笼和灯使用合法支撑。",
    "M7": "暗铁献祭地窖：使用1.20.1原版暗色方块与金饰转译灾变视觉，不复制第三方模型；血池采用红色玻璃/地毯等封闭装饰，不增添未配置的危险流体或交互Boss。",
    "S5": "本地THREE方块渲染，不标作image2.5成功生图；示意偏简，先做结构二次设计。保留虚空回廊风格但北入口至内部点必须连续可走，不做真实传送门、跳跃必经断桥或虚空坠落点。",
    "S6": "地狱魂火祭坛：保证魂火支撑、熔岩封闭和三格北门；祭坛绕行安全；头骨与灯有支撑，不预置刷怪实体。",
    "S7": "暗金囚笼：牢笼是陈设，唯一通路不得由关闭铁门阻断；熔岩裂纹用发光拼花或封闭槽，不堆放奖励库存。",
}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main():
    manifest = json.loads((SOURCE / "manifest.json").read_text(encoding="utf-8-sig"))
    rooms = manifest["rooms"]
    assert {room["id"] for room in rooms} == set(EXPECTED), "Unexpected batch3 identity set"
    assert len(rooms) == len(EXPECTED)
    queue_path = ROOT / "queue/batch3.json"
    previous = json.loads(queue_path.read_text(encoding="utf-8")) if queue_path.exists() else {}
    stages = {room["id"]: room["stages"] for room in previous.get("rooms", [])}
    records = []
    for room in rooms:
        identity = room["id"]
        image_path = SOURCE / room["path"]
        assert image_path.resolve().is_relative_to(SOURCE.resolve())
        assert digest(image_path) == EXPECTED[identity] == room["sha256"], f"Fingerprint mismatch: {identity}"
        with Image.open(image_path) as image:
            assert image.size == (1536, 1024)
            image.verify()
        records.append({"id": identity, "name": room["name"], "tier": room["tier"], "design_dimensions": room["dims"], "style": room["style"], "reference": str(image_path), "sha256": EXPECTED[identity], "source_kind": "local_three_render" if identity == "S5" else "image2.5_concept", "image_decoded": True, "image_size": [1536, 1024], "target_directory": str(ROOT / "incoming" / identity), "target_template_id": "ftdungeonmobs:image25/" + identity.lower(), "stages": stages.get(identity, {"received": "PASS", "construction": "QUEUED_NOT_DISPATCHED", "static_qa": "NOT_RUN", "native_placement_qa": "NOT_RUN", "integration": "NOT_STARTED"}), "adaptation_requirements": NOTES[identity]})
    report = {"status": "RECEIVED_AND_QUEUED", "received_at": datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(timespec="seconds"), "source_directory": str(SOURCE), "source_manifest_sha256": digest(SOURCE / "manifest.json"), "room_count": 6, "ai_concepts": 5, "local_renders": 1, "image_review": "PASS", "gate": "Finish current nine-room corrections and final FT deployment verification first; this queue does not alter the active catalog or incoming deliveries.", "rooms": records, "construction_or_integration_completed": False, "paid_generation_calls": 0}
    write_json(queue_path, report)
    write_json(ROOT / "qa/batch3-image-review.json", {"status": "PASS", "room_count": 6, "references": [{key: room[key] for key in ("id", "name", "reference", "sha256", "source_kind", "image_decoded", "image_size")} for room in records], "manifest_sha256": report["source_manifest_sha256"], "no_source_files_modified": True, "scope": "Image decoding, fingerprints, concept review and construction queue receipt; not a structure or gameplay acceptance."})
    print(json.dumps({"status": report["status"], "ids": [room["id"] for room in records], "queue": str(queue_path), "image_review": "PASS", "S5_source": "local_three_render"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
