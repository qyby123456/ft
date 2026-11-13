import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path("D:/ft工程文件/FT地牢_第二批_L3_L4_S3_制作_20261101")
EXPECTED = {"L3": "aa6e5f006d2c89b216013fbd0c50f6606b21db2de36010ce3c9d4d6649ca5f80", "S3": "d72f6fa1d576958417ed362f81f5a1866a96e991e1a45a833e2d7f15aecdb51c"}
FILES = ("room.nbt", "room.json", "preview.png", "qa.json")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    fingerprints = {}
    backup = ROOT / "qa/batch2-before-support-correction"
    backup.mkdir(exist_ok=False)
    for room_id, expected in EXPECTED.items():
        target = ROOT / "incoming" / room_id
        assert digest(target / "room.nbt") == expected, "Concurrent room change; do not overwrite"
        fingerprints[room_id] = {name: digest(target / name) for name in FILES}
        (backup / room_id).mkdir()
        for name in FILES:
            shutil.copy2(target / name, backup / room_id / name)
    subprocess.run([sys.executable, str(SOURCE / "tools/build.py")], cwd=SOURCE, check=True, timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
    assert digest(SOURCE / "out/L4/room.nbt") == digest(ROOT / "incoming/L4/room.nbt"), "Unrelated L4 changed"
    for room_id, previous in fingerprints.items():
        target = ROOT / "incoming" / room_id
        for name in FILES:
            assert digest(target / name) == previous[name], "Concurrent delivery change; do not overwrite"
        for name in FILES:
            shutil.copy2(SOURCE / "out" / room_id / name, target / name)
        metadata = json.loads((target / "room.json").read_text(encoding="utf-8"))
        assert metadata["sha256"] == digest(target / "room.nbt")
    corrections = {"status": "REBUILT_PENDING_NATIVE_PLACEMENT", "L3": {"reason": "Add solid pillar brackets above all four previously unsupported hanging lamps", "old_sha256": EXPECTED["L3"], "sha256": digest(ROOT / "incoming/L3/room.nbt")}, "S3": {"reason": "Empty brewing inventory, false bottle-state flags, updated source QA and preview", "sha256": digest(ROOT / "incoming/S3/room.nbt")}, "L4_unchanged": True}
    report_path = SOURCE / "delivery-report.json"
    report = json.loads(report_path.read_text(encoding="utf-8"))
    report["published_to_incoming"] = True
    report["coordinator_corrections"] = corrections
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (ROOT / "qa/batch2-corrections.json").write_text(json.dumps(corrections, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(corrections, ensure_ascii=False))


if __name__ == "__main__":
    main()
