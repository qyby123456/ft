from __future__ import annotations

import argparse
import hashlib
import json
import shutil

from PIL import Image

from common import INCOMING, ROOT, SPECS, Structure, dump_json, image_metadata
from preview import make_preview
from qa import validate
from rooms import BUILDERS


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--publish", action="store_true")
    arguments = parser.parse_args()
    output = ROOT / "out"
    output.mkdir(parents=True, exist_ok=True)
    reports = []
    for room_id, builder in BUILDERS.items():
        folder = output / room_id
        folder.mkdir(parents=True, exist_ok=True)
        room = builder()
        room.structure.write(folder / "room.nbt")
        report, visited = validate(room, folder / "room.nbt")
        metadata = {"id": room_id, "name": room.spec["name"], "size": list(room.structure.size), "entrance": [room.center, 1, 0], "interior": list(room.spec["interior"]), "facing": "north", "sha256": digest(folder / "room.nbt"), "reference": image_metadata(room)["image"]}
        dump_json(folder / "room.json", metadata)
        make_preview(room, Structure.read(folder / "room.nbt"), visited, folder / "preview.png")
        with Image.open(folder / "preview.png") as image:
            assert image.size == (1536, 1024)
            image.verify()
        report["preview"] = {"path": str(folder / "preview.png"), "size": [1536, 1024], "sha256": digest(folder / "preview.png"), "source": "实际序列化NBT重新读取，剖视仅用于预览"}
        report["room_json_sha256"] = digest(folder / "room.json")
        dump_json(folder / "qa.json", report)
        reports.append({"id": room_id, "status": report["status"], "size": metadata["size"], "sha256": metadata["sha256"], "walkable": report["navigation"]["reachable_ground_cells"], "ground_reachable_fraction": report["navigation"]["ground_reachable_fraction"], "loot": 0, "block_entities": report["nbt"]["block_entities"], "source": str(folder)})
        print(json.dumps(reports[-1], ensure_ascii=False), flush=True)
    if arguments.publish:
        for room_id in BUILDERS:
            target = INCOMING / room_id
            assert target.resolve().parent == INCOMING.resolve()
            assert not target.exists(), (room_id, "incoming folder already exists; refusing to overwrite")
        for room_id in BUILDERS:
            target = INCOMING / room_id
            target.mkdir(parents=True, exist_ok=False)
            source = output / room_id
            for name in ("room.nbt", "room.json", "preview.png", "qa.json"):
                shutil.copy2(source / name, target / name)
                assert digest(source / name) == digest(target / name)
            delivered = json.loads((target / "room.json").read_text(encoding="utf-8"))
            assert digest(target / "room.nbt") == delivered["sha256"]
            assert set(path.name for path in target.iterdir()) == {"room.nbt", "room.json", "preview.png", "qa.json"}
            print("Delivered verified room: " + str(target), flush=True)
    manifest = {"status": "PASS", "rooms": reports, "incoming": str(INCOMING), "published_to_incoming": arguments.publish, "core_modified": False, "saves_modified": False, "deployed": False, "runtime_tested": False, "image_generation_api_calls": 0, "write_scope": [str(ROOT), *[str(INCOMING / room_id) for room_id in BUILDERS]]}
    dump_json(ROOT / "delivery-report.json", manifest)


if __name__ == "__main__":
    main()
