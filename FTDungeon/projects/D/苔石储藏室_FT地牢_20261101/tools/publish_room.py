import hashlib
import json
import shutil
from pathlib import Path

from PIL import Image

from build_room import ENTRANCE, INTERIOR, ROOT, SIZE, load_structure, write_json


DESTINATION = Path("D:/ft工程文件/FT地牢_Image25接入_20261101/incoming/S2")


def read_json(path):
    return json.loads(path.read_text(encoding="utf-8"))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    structure = ROOT / "out/room.nbt"
    metadata = read_json(ROOT / "out/room.json")
    geometry = read_json(ROOT / "qa/geometry.json")
    native = read_json(ROOT / "qa/minecraft-format-verification.json")
    render = read_json(ROOT / "qa/render.json")
    reference = read_json(ROOT / "qa/reference.json")
    structure_hash = digest(structure)
    for report in (metadata, geometry, native, render):
        expected = report.get("structure_sha256", report.get("sha256"))
        if expected != structure_hash:
            raise ValueError("A validation report is stale")
    if any(report["status"] != "PASS" for report in (geometry, native, render)):
        raise ValueError("Room validation has not passed")
    template = load_structure(structure)
    if metadata["id"] != "S2" or metadata["facing"] != "north" or metadata["size"] != list(SIZE) or metadata["entrance"] != list(ENTRANCE) or metadata["interior"] != list(INTERIOR):
        raise ValueError("Invalid shared room interface")
    if template["size"] != metadata["size"] or template["entities"]:
        raise ValueError("Structure does not match metadata")
    with Image.open(ROOT / "out/preview.png") as image:
        image.verify()
    if digest(ROOT / "out/preview.png") != render["preview_sha256"]:
        raise ValueError("Preview report is stale")
    report = {
        "id": "S2", "name": metadata["name"], "status": "PASS", "minecraft": "1.20.1", "DataVersion": 3465,
        "size": metadata["size"], "entrance": metadata["entrance"], "interior": metadata["interior"], "facing": "north", "entrance_clear_size": [3, 3],
        "roof_closed": geometry["roof_closed"], "dry_route_verified": all(geometry["dry_routes"].values()), "dry_central_width": 3,
        "water_sources": geometry["water_sources"], "water_contained": geometry["contained_water"], "reward_items": 0, "loot_tables": 0,
        "entities": 0, "closed_doors": 0, "non_air_blocks": geometry["non_air_blocks"], "volume_records": geometry["explicit_volume_records"],
        "geometry": geometry, "minecraft_registry_validation": native, "preview": render, "reference": reference,
        "structure_sha256": structure_hash, "preview_sha256": render["preview_sha256"], "source_project": str(ROOT), "publication_path": str(DESTINATION),
        "core_files_modified": [], "core_build_or_deployment": False, "automatic_generation": False, "player_saves_opened": False,
        "gameplay_tested": False, "paid_image_requests": 0,
        "integration": "Unified north room interface only. Placement, corridor connection and core deployment belong to the central FT room importer; no independent datapack, hall opening or mod replacement.",
        "limitations": ["Preview is a software cutaway from final NBT, not a client screenshot.", "Client placement, lighting, fluid ticks and multiplayer have not been tested in a player world.", "Explicit air records require the core importer to preflight the entire target volume before placement.", "Chest and barrels are empty; gold pressure plates are fixed architectural decoration, not a reward system."],
    }
    write_json(ROOT / "out/qa.json", report)
    artifacts = ("room.nbt", "room.json", "preview.png", "qa.json")
    for name in artifacts:
        target = DESTINATION / name
        if target.exists() and digest(target) != digest(ROOT / "out" / name):
            raise FileExistsError(f"Refusing to overwrite a different incoming artifact: {target}")
    DESTINATION.mkdir(parents=True, exist_ok=True)
    for name in artifacts:
        shutil.copy2(ROOT / "out" / name, DESTINATION / name)
        assert digest(DESTINATION / name) == digest(ROOT / "out" / name)
    print(json.dumps({"id": "S2", "status": "PASS", "destination": str(DESTINATION), "files": list(artifacts), "sha256": structure_hash}, ensure_ascii=False))


if __name__ == "__main__":
    main()
