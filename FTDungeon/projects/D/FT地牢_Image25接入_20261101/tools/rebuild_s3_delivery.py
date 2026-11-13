import hashlib
import json
import shutil
import subprocess
import sys
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SOURCE = Path("D:/ft工程文件/FT地牢_第二批_L3_L4_S3_制作_20261101")
TARGET = ROOT / "incoming/S3"
EXPECTED_REJECTED = "2659516878c3e5604f5c90d9b12c09e58b89821fd9cd527c2f75265a873d0b0e"
FILES = ("room.nbt", "room.json", "preview.png", "qa.json")


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    assert digest(TARGET / "room.nbt") == EXPECTED_REJECTED, "Concurrent delivery change; do not overwrite"
    previous = {name: digest(TARGET / name) for name in FILES}
    backup = ROOT / "qa/s3-rejected-inventory"
    backup.mkdir(exist_ok=False)
    for name in FILES:
        shutil.copy2(TARGET / name, backup / name)
    (backup / "reason.txt").write_text("Rejected: brewing stand Items contained five collectible water bottles; room contract permits empty inventories only.\n", encoding="utf-8")
    subprocess.run([sys.executable, str(SOURCE / "tools/build.py")], cwd=SOURCE, check=True, timeout=180, creationflags=subprocess.CREATE_NO_WINDOW)
    for name, fingerprint in previous.items():
        assert digest(TARGET / name) == fingerprint, "Concurrent delivery change; do not overwrite"
    for room_id in ("L3", "L4"):
        assert digest(SOURCE / "out" / room_id / "room.nbt") == digest(ROOT / "incoming" / room_id / "room.nbt"), "Unrelated template unexpectedly changed"
    for name in FILES:
        shutil.copy2(SOURCE / "out/S3" / name, TARGET / name)
    metadata = json.loads((TARGET / "room.json").read_text(encoding="utf-8"))
    assert metadata["sha256"] == digest(TARGET / "room.nbt")
    report_path = SOURCE / "delivery-report.json"
    report = json.loads(report_path.read_text(encoding="utf-8"))
    report["published_to_incoming"] = True
    report["coordinator_correction"] = {"room": "S3", "reason": "Remove collectible brewing inventory; retained functional furniture and readable book", "previous_sha256": EXPECTED_REJECTED, "final_sha256": metadata["sha256"]}
    report_path.write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    (ROOT / "qa/s3-correction.json").write_text(json.dumps(report["coordinator_correction"], ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(report["coordinator_correction"], ensure_ascii=False))


if __name__ == "__main__":
    main()
