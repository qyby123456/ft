import argparse
import collections
import hashlib
import json
import sys
from pathlib import Path

from PIL import Image


ROOT = Path(__file__).resolve().parents[1]
IMAGE_ROOT = Path("D:/ft工程文件/image2.5")
NBT_TOOLS = Path("C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools")
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import plain, read_nbt


FIRST = json.loads((IMAGE_ROOT / "dungeon-room-styles-20261101/out/rooms_meta.json").read_text(encoding="utf-8"))["rooms"]
SECOND = [
    {"id": "L3", "name": "熔岩锻造大厅", "path": "dungeon-design-batch2-20261101/out/L3-molten-forge-hall-hero.png"},
    {"id": "L4", "name": "蛛巢神殿", "path": "dungeon-design-batch2-20261101/out/L4-spider-shrine-hero.png"},
    {"id": "S3", "name": "炼金实验室", "path": "dungeon-design-batch2-20261101/out/S3-alchemist-lab-hero.png"},
]
REFERENCES = FIRST + SECOND
AIR = {"minecraft:air", "minecraft:cave_air", "minecraft:void_air"}
HAZARDS = {"minecraft:water", "minecraft:lava", "minecraft:fire", "minecraft:soul_fire", "minecraft:powder_snow", "minecraft:cobweb", "minecraft:magma_block", "minecraft:campfire", "minecraft:soul_campfire", "minecraft:sweet_berry_bush", "minecraft:wither_rose"}
FORBIDDEN = {"minecraft:spawner", "minecraft:command_block", "minecraft:chain_command_block", "minecraft:repeating_command_block", "minecraft:structure_block", "minecraft:structure_void", "minecraft:jigsaw"}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def inventory():
    destination = ROOT / "qa/reference-inventory.json"
    previous = json.loads(destination.read_text(encoding="utf-8")) if destination.exists() else None
    old = {entry["id"]: entry for entry in previous["references"]} if previous else {}
    records = []
    for reference in REFERENCES:
        path = IMAGE_ROOT / reference["path"]
        with Image.open(path) as image:
            image.verify()
        with Image.open(path) as image:
            size = list(image.size)
        actual = digest(path)
        expected = reference.get("sha256", old.get(reference["id"], {}).get("sha256", actual))
        if actual != expected:
            raise ValueError(f"Reference fingerprint changed: {reference['id']}")
        records.append({"id": reference["id"], "name": reference["name"], "path": str(path), "bytes": path.stat().st_size, "size": size, "sha256": actual, "published_sha256_matches": True})
    result = {"status": "PASS", "count": len(records), "references": records, "not_generated_by_this_task": True, "missing_at_initial_review": ["M3", "M4", "S4", "LAYOUT"], "scope": "Only nine existing room PNGs; prompts alone are not a completed design."}
    save(destination, result)
    return {entry["id"]: entry for entry in records}


def inspect_room(reference):
    room_id = reference["id"]
    folder = ROOT / "incoming" / room_id
    required = [folder / name for name in ("room.nbt", "room.json", "preview.png", "qa.json")]
    missing = [path.name for path in required if not path.exists()]
    if missing:
        return {"id": room_id, "status": "PENDING", "missing": missing}
    metadata = json.loads((folder / "room.json").read_text(encoding="utf-8-sig"))
    template = plain(read_nbt(folder / "room.nbt")[1])
    checks = []

    def require(condition, name):
        if not condition:
            raise ValueError(f"{room_id}: {name}")
        checks.append(name)

    require(metadata["id"] == room_id, "metadata_id")
    require(metadata["sha256"].lower() == digest(folder / "room.nbt"), "nbt_fingerprint")
    require(Path(metadata["reference"]).resolve() == Path(reference["path"]).resolve(), "correct_reference")
    require(template["size"] == metadata["size"], "declared_size")
    size = template["size"]
    require(len(size) == 3 and all(type(value) is int and 2 <= value <= 64 for value in size), "valid_dimensions")
    require(template.get("DataVersion") == 3465, "minecraft_1_20_1")
    require(not template.get("entities"), "no_entities")
    require(metadata["facing"] == "north", "north_interface")
    entrance, interior = tuple(metadata["entrance"]), tuple(metadata["interior"])
    require(len(entrance) == len(interior) == 3 and entrance[2] == 0, "coordinate_schema")
    require(all(0 <= point[axis] < size[axis] for point in (entrance, interior) for axis in range(3)), "safe_point_bounds")
    palette = template["palette"]
    require(bool(palette) and all(state["Name"].startswith("minecraft:") for state in palette), "vanilla_palette")
    require(not any(state["Name"] in FORBIDDEN for state in palette), "no_executable_or_spawn_blocks")
    blocks = {}
    block_entities = []
    for record in template["blocks"]:
        position = tuple(record["pos"])
        require(len(position) == 3 and all(0 <= position[axis] < size[axis] for axis in range(3)), "block_in_bounds")
        if position in blocks:
            raise ValueError(f"{room_id}: duplicate block position {position}")
        require(0 <= record["state"] < len(palette), "valid_palette_index")
        blocks[position] = palette[record["state"]]
        data = record.get("nbt")
        if data:
            block_entities.append(data)
            require(not data.get("LootTable") and not data.get("Items"), "no_automatic_rewards")
    checks = list(dict.fromkeys(checks))

    def name_at(position):
        return blocks.get(position, {"Name": "minecraft:air"})["Name"]

    def clear(position):
        name = name_at(position)
        return name in AIR or name.endswith("_carpet")

    def walkable(position):
        if not all(0 <= position[axis] < size[axis] for axis in range(3)):
            return False
        below = (position[0], position[1] - 1, position[2])
        head = (position[0], position[1] + 1, position[2])
        return name_at(below) not in AIR | HAZARDS and clear(position) and clear(head)

    require(all(clear((entrance[0] + offset_x, entrance[1] + offset_y, 0)) for offset_x in (-1, 0, 1) for offset_y in range(3)), "three_by_three_clear_entrance")
    require(all(walkable((entrance[0] + offset_x, entrance[1], 0)) for offset_x in (-1, 0, 1)), "supported_entrance")
    require(walkable(entrance) and walkable(interior), "safe_points_walkable")
    reachable = {entrance}
    pending = collections.deque([entrance])
    while pending:
        position = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for delta_y in (0, 1, -1):
                following = (position[0] + delta_x, position[1] + delta_y, position[2] + delta_z)
                if following in reachable or not walkable(following):
                    continue
                if delta_y > 0 and not clear((position[0], position[1] + 2, position[2])):
                    continue
                if delta_y < 0 and not clear((following[0], following[1] + 2, following[2])):
                    continue
                reachable.add(following)
                pending.append(following)
    require(interior in reachable, "entrance_to_interior_connected")
    materials = collections.Counter(state["Name"] for state in blocks.values())
    with Image.open(folder / "preview.png") as image:
        preview_size = list(image.size)
        image.verify()
    authored_qa = json.loads((folder / "qa.json").read_text(encoding="utf-8-sig"))
    require(authored_qa.get("status") == "PASS", "builder_qa_pass")
    return {"id": room_id, "name": metadata["name"], "status": "PASS", "size": size, "blocks": len(blocks), "palette_states": len(palette), "block_entities": len(block_entities), "entrance": entrance, "interior": interior, "reachable_positions": len(reachable), "preview_size": preview_size, "sha256": digest(folder / "room.nbt"), "checks": list(dict.fromkeys(checks)), "fluids": {name: materials[name] for name in ("minecraft:water", "minecraft:lava") if materials[name]}, "limits": "Independent static and NBT verification; approximate block collision; native Minecraft state/placement verification and gameplay are separate."}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--require-all", action="store_true")
    arguments = parser.parse_args()
    references = inventory()
    rooms = []
    for reference in references.values():
        try:
            rooms.append(inspect_room(reference))
        except Exception as error:
            rooms.append({"id": reference["id"], "status": "FAIL", "error": str(error)})
    counts = dict(collections.Counter(room["status"] for room in rooms))
    complete = counts.get("PASS", 0) == len(references)
    report = {"status": "PASS" if complete else "FAIL" if counts.get("FAIL") else "PENDING", "counts": counts, "rooms": rooms, "scope": "Nine already-generated image2.5 dungeon-room concepts, no new image charges."}
    save(ROOT / "qa/delivery-review.json", report)
    for room in rooms:
        print(json.dumps({key: room[key] for key in ("id", "status", "size", "missing", "error") if key in room}, ensure_ascii=False))
    if counts.get("FAIL") or arguments.require_all and not complete:
        raise SystemExit(1)


if __name__ == "__main__":
    main()
