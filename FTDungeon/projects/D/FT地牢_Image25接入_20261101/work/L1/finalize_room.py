import gzip
import hashlib
import json
import zipfile
from pathlib import Path

from PIL import Image

from build_room import DELIVERY, ENTRANCE, INTERIOR, REFERENCE, SIZE, WORK, nbt_payload


def main():
    metadata = json.loads((DELIVERY / "room.json").read_text(encoding="utf-8"))
    geometry = json.loads((WORK / "qa/geometry.json").read_text(encoding="utf-8"))
    native = json.loads((WORK / "qa/native.json").read_text(encoding="utf-8"))
    blueprint = json.loads((WORK / "qa/blueprint.json").read_text(encoding="utf-8"))
    digest = hashlib.sha256((DELIVERY / "room.nbt").read_bytes()).hexdigest()
    assert geometry["status"] == native["status"] == "PASS"
    assert metadata["id"] == "L1" and metadata["facing"] == "north"
    assert metadata["size"] == native["size"] == list(SIZE)
    assert metadata["entrance"] == list(ENTRANCE) and metadata["interior"] == list(INTERIOR)
    assert metadata["reference"] == str(REFERENCE)
    assert metadata["sha256"] == digest
    assert gzip.decompress((DELIVERY / "room.nbt").read_bytes()) == b"\x0a\x00\x00" + nbt_payload(blueprint)[1]
    assert all(not entry.get("nbt") for entry in blueprint["blocks"])
    assert blueprint["entities"] == []
    names = {entry["Name"] for entry in blueprint["palette"]}
    assert all(name.startswith("minecraft:") for name in names)
    assert not names & {"minecraft:spawner", "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block", "minecraft:iron_door", "minecraft:structure_block", "minecraft:jigsaw"}
    with Image.open(DELIVERY / "preview.png") as image:
        image.verify()
    with Image.open(DELIVERY / "preview.png") as image:
        image_size = list(image.size)
    prior_delivery = Path("D:/ft工程文件/黑曜试炼地牢_20261101/out/FT_Obsidian_Trial_20261101.zip")
    prior_digest = hashlib.sha256(prior_delivery.read_bytes()).hexdigest()
    assert prior_digest == "ff01cc6fe7191a699dd613ebb168d1d08c158c363b1f3124d1884ab1d222d8ea"
    report = {"id": "L1", "status": "PASS", "minecraft": "1.20.1", "size": list(SIZE), "sha256": digest, "reference": str(REFERENCE), "interface": {"facing": "north", "entrance_feet": list(ENTRANCE), "interior_feet": list(INTERIOR), "opening_width": 3, "opening_height": 3, "local_z": 0, "dry_full_block_support": True, "no_closed_door": True}, "geometry": geometry, "native_validation": native, "blueprint_matches_compressed_nbt": True, "preview": {"status": "PASS", "size": image_size, "sha256": hashlib.sha256((DELIVERY / "preview.png").read_bytes()).hexdigest(), "render": "Actual template voxel positions with original vanilla textures; cutaway roof and near walls, simplified small models; not a game screenshot."}, "scope": {"only_production_and_qa": True, "core_modified": False, "deployed": False, "world_modified": False, "new_paid_image_requests": 0, "mobs_or_rewards_added": False, "barrels": "two empty decorative barrels with no LootTable or item payload", "decorative_brazier": "vanilla netherrack fire, not an interactive FT brazier", "previous_delivery_unchanged_sha256": prior_digest}, "caveats": ["No Minecraft world/client/server gameplay test. The receiving integrator is responsible for placement, collision checks with neighbouring rooms and game-runtime acceptance.", "Explicit minecraft:air occupies the full 25x12x25 template volume; do not place over existing builds without the integrator's protection checks.", "Real lava and netherrack fire are damaging hazards; the entry and marked interior point are dry. No game rules are changed."]}
    (DELIVERY / "qa.json").write_text(json.dumps(report, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    archive = WORK / "L1-handoff.zip"
    with zipfile.ZipFile(archive, "w", zipfile.ZIP_DEFLATED) as bundle:
        for name in ("room.nbt", "room.json", "preview.png", "qa.json"):
            bundle.write(DELIVERY / name, name)
    with zipfile.ZipFile(archive) as bundle:
        assert bundle.testzip() is None
    print(json.dumps({"status": "PASS", "directory": str(DELIVERY), "size": list(SIZE), "sha256": digest, "files": {name: (DELIVERY / name).stat().st_size for name in ("room.nbt", "room.json", "preview.png", "qa.json")}, "native": native["status"], "previous_delivery": "UNCHANGED"}, ensure_ascii=False))


if __name__ == "__main__":
    main()
