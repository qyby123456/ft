from __future__ import annotations

import collections
import gzip
import hashlib
import json
import sys
from pathlib import Path

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parents[1]
DELIVERY = ROOT / "out/S5"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-design-batch3-20261101")
LEGACY = ROOT / "tools/vendor"
sys.path.insert(0, str(LEGACY))
from nbtio import AIR, Structure, plain, read_nbt, state

SIZE = (7, 8, 13)
ENTRANCE = (3, 3, 0)
INTERIOR = (3, 3, 10)
EXPECTED_REFERENCE = "dc508de8c23a414740405a67eb1169553a3337b398dbdee54297ab9bdcf73bcf"
SPECIAL = {"minecraft:end_rod", "minecraft:chorus_flower", "minecraft:end_portal_frame"}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def construct():
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (6, 7, 12), AIR)
    structure.box((0, 0, 0), (6, 0, 12), state("obsidian"))
    for block_x in (0, 1, 5, 6):
        structure.box((block_x, 1, 0), (block_x, 1, 12), state("purpur_pillar", axis="z"))
    for block_z in (0, 2, 5, 6, 9, 11, 12):
        structure.box((1, 1, block_z), (5, 1, block_z), state("purpur_pillar", axis="x"))
    structure.box((0, 2, 0), (6, 2, 12), state("end_stone_bricks"))
    for block_z in (3, 4, 7, 8):
        for block_x in (2, 4):
            structure.set((block_x, 2, block_z), state("tinted_glass"))
        structure.set((3, 1, block_z), state("purpur_pillar", axis="z"))
        structure.set((3, 2, block_z), state("end_stone"))
    for block_z in range(13):
        for block_x in (0, 6):
            for block_y in range(3, 7):
                material = state("purpur_pillar", axis="y") if block_z in (0, 2, 6, 10, 12) else state("purple_stained_glass")
                structure.set((block_x, block_y, block_z), material)
        for block_x in (1, 5):
            structure.set((block_x, 3, block_z), state("obsidian" if block_z in (3, 4, 7, 8) else "purpur_block"))
    for block_x in (1, 5):
        for block_z in (1, 5, 9):
            structure.set((block_x, 4, block_z), state("purpur_pillar", axis="y"))
            structure.set((block_x, 5, block_z), state("end_rod", facing="up"))
        structure.set((block_x, 3, 7), state("end_stone"))
        structure.set((block_x, 4, 7), state("chorus_flower", age=5))
        structure.set((block_x, 3, 10), state("obsidian"))
        structure.set((block_x, 4, 10), state("end_portal_frame", eye=False, facing="north"))
        structure.set((block_x, 4, 11), state("obsidian"))
        structure.set((block_x, 5, 11), state("obsidian"))
    structure.box((0, 3, 12), (6, 6, 12), state("end_stone_bricks"))
    structure.box((2, 3, 12), (4, 5, 12), state("obsidian"))
    structure.set((3, 4, 12), state("purple_stained_glass"))
    structure.set((3, 5, 12), state("purple_stained_glass"))
    structure.box((0, 6, 0), (6, 6, 0), state("purpur_pillar", axis="x"))
    for block_y in range(3, 6):
        for block_x in (1, 5):
            structure.set((block_x, block_y, 0), state("purpur_pillar", axis="y"))
    for block_z in range(13):
        for block_x in range(7):
            material = "end_stone_bricks" if block_x in (0, 6) or block_z in (0, 12) else "tinted_glass"
            if block_z in (2, 6, 10):
                material = "purpur_block"
            structure.set((block_x, 7, block_z), state(material))
    return structure


def full_floor(block):
    return block.name != "minecraft:air" and block.name not in SPECIAL


def walkable(structure, position):
    block_x, block_y, block_z = position
    return structure.get(position) == AIR and structure.get((block_x, block_y + 1, block_z)) == AIR and full_floor(structure.get((block_x, block_y - 1, block_z)))


def static_qa(path):
    assert path.read_bytes()[:2] == b"\x1f\x8b"
    _, decoded = read_nbt(path)
    data = plain(decoded)
    structure = Structure.read(path)
    assert structure.size == SIZE and data["DataVersion"] == 3465
    assert len(structure.blocks) == 7 * 8 * 13
    assert not structure.entities and not structure.block_entities
    allowed = {"air", "obsidian", "end_stone", "end_stone_bricks", "purpur_block", "purpur_pillar", "purple_stained_glass", "tinted_glass", "end_rod", "chorus_flower", "end_portal_frame"}
    for position, block in structure.blocks.items():
        assert all(0 <= value < size for value, size in zip(position, SIZE))
        assert block.name.removeprefix("minecraft:") in allowed
    assert not any(key in json.dumps(data) for key in ("LootTable", '"Items"', '"Book"'))
    reachable = {ENTRANCE}
    queue = collections.deque([ENTRANCE])
    while queue:
        current_x, current_y, current_z = queue.popleft()
        for step_x, step_z in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            next_position = (current_x + step_x, current_y, current_z + step_z)
            if 0 <= next_position[0] < SIZE[0] and 0 <= next_position[2] < SIZE[2] and next_position not in reachable and walkable(structure, next_position):
                reachable.add(next_position)
                queue.append(next_position)
    expected = {(block_x, 3, block_z) for block_z in range(12) for block_x in range(2, 5)}
    assert reachable == expected and INTERIOR in reachable
    for block_x in (2, 3, 4):
        assert full_floor(structure.get((block_x, 2, 0)))
        assert all(structure.get((block_x, block_y, 0)) == AIR for block_y in (3, 4, 5))
    for position in reachable:
        block_x, block_y, block_z = position
        for step_x, step_z in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            neighbor = (block_x + step_x, block_y, block_z + step_z)
            if neighbor in reachable or neighbor[2] == -1 and neighbor[0] in (2, 3, 4):
                continue
            assert full_floor(structure.get(neighbor)), (position, "unguarded edge", neighbor)
    solid = {position for position, block in structure.blocks.items() if full_floor(block)}
    connected = {position for position in solid if position[1] == 0}
    queue = collections.deque(connected)
    while queue:
        current = queue.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(coordinate + step for coordinate, step in zip(current, offset))
            if neighbor in solid and neighbor not in connected:
                connected.add(neighbor)
                queue.append(neighbor)
    assert connected == solid, sorted(solid - connected)
    supported = []
    for position, block in structure.blocks.items():
        below = structure.get((position[0], position[1] - 1, position[2]))
        if block.name in SPECIAL:
            assert full_floor(below), (position, "unsupported special block")
            if block.name == "minecraft:chorus_flower":
                assert below.name == "minecraft:end_stone" and block.get("age") == "5"
            if block.name == "minecraft:end_rod":
                assert block.get("facing") == "up"
            if block.name == "minecraft:end_portal_frame":
                assert block.get("eye") == "false"
            supported.append({"position": list(position), "block": block.name, "support": below.name})
    assert all(full_floor(structure.get((block_x, 7, block_z))) for block_x in range(7) for block_z in range(13))
    canonical = [[*position, block.name, dict(block.properties)] for position, block in sorted(structure.blocks.items())]
    semantic_hash = hashlib.sha256(json.dumps({"size": SIZE, "blocks": canonical, "entities": [], "block_entities": []}, sort_keys=True, separators=(",", ":")).encode()).hexdigest()
    return structure, reachable, {
        "status": "PASS", "data_version": data["DataVersion"], "size": list(SIZE),
        "nbt_records": len(structure.blocks), "explicit_air_records": sum(block == AIR for block in structure.blocks.values()), "palette_states": len(data["palette"]),
        "structural_blocks_connected_to_base": len(connected), "unsupported_structural_blocks": 0,
        "supported_special_blocks": supported,
        "navigation": {"feet_y": 3, "reachable_cells": len(reachable), "minimum_width": 3, "north_opening": [3, 3], "bidirectional_path": True, "reachable_unguarded_drop_edges": 0, "authorized_boundary": "north entrance only, x=2..4 z=-1; connector remains integration responsibility"},
        "roof": {"fully_sealed": True, "glass_skylights": True},
        "fluids": 0, "hazardous_blocks": 0, "containers": 0, "reward_inventory": 0, "loot_tables": 0, "block_entities": 0, "entities": 0,
        "structure_semantic_sha256": semantic_hash,
        "semantic_hash_algorithm": "SHA256 UTF8 json.dumps({size,blocks:[[x,y,z,Name,sortedProperties],...],entities:[],block_entities:[]},sort_keys=True,separators=(',',':')); positions lexicographic; includes explicit air",
    }


def main():
    from preview_s5 import make_preview

    reference_image = REFERENCE / "out/S5-end-void-passage-hero.png"
    assert digest(reference_image) == EXPECTED_REFERENCE
    DELIVERY.mkdir(parents=True, exist_ok=True)
    construct().write(DELIVERY / "room.nbt")
    structure, reachable, static = static_qa(DELIVERY / "room.nbt")
    metadata = {"id": "S5", "name": "末地虚空回廊", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "sha256": digest(DELIVERY / "room.nbt"), "reference": str(reference_image)}
    dump(DELIVERY / "room.json", metadata)
    preview = make_preview(structure, reachable, DELIVERY / "preview.png")
    source_files = [reference_image, REFERENCE / "README.md", REFERENCE / "ROOM_STYLES_BATCH3.md", REFERENCE / "manifest.json", REFERENCE / "prompts/S5-end-void-passage.txt"]
    fingerprint_payload = {"id": "S5", "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "nbt_sha256": metadata["sha256"], "reference_sha256": EXPECTED_REFERENCE}
    fingerprint = hashlib.sha256(json.dumps(fingerprint_payload, sort_keys=True, separators=(",", ":")).encode()).hexdigest()
    report = {
        "schema": "ft-room-independent-qa/v1", "id": "S5", "status": "STATIC_PASS_NATIVE_PENDING", "minecraft": "Java 1.20.1", "forge_target": "47.4.10",
        "reference_kind": "local THREE block schematic fallback after seven failed image2.5 attempts; NOT an AI success and NOT gameplay evidence",
        "source_files": [{"path": str(path), "sha256": digest(path)} for path in source_files],
        "structure_sha256": metadata["sha256"],
        "structure_uncompressed_sha256": hashlib.sha256(gzip.decompress((DELIVERY / "room.nbt").read_bytes())).hexdigest(),
        "structure_semantic_sha256": static["structure_semantic_sha256"],
        "delivery_fingerprint": fingerprint, "delivery_fingerprint_payload": fingerprint_payload,
        "fingerprint_scope": "Independent asset identity only; NOT DungeonRoomCatalog runtime fingerprint, which requires the coordinator-assigned slot",
        "files": {name: {"sha256": digest(DELIVERY / name), "bytes": (DELIVERY / name).stat().st_size} for name in ("room.nbt", "room.json", "preview.png")},
        "static": static, "preview": preview,
        "adaptations": [
            "Concept 5x11 h4 becomes a 5x11 h4 usable interior inside a 7x8x13 supported shell; floor y=2, feet y=3, roof y=7.",
            "Broken segments are dark under-deck observation slots. Eight tinted-glass floor tiles and a one-wide end-stone center spine keep all three walking lanes continuous and dry.",
            "No genuine void holes, floating collision blocks, explodable end crystals, active end portals, fluids, fire, rewards or spawned entities.",
            "Obsidian base, longitudinal purpur beams, transverse ribs, sealed side glazing and a complete glazed roof physically connect every structural block to the base.",
            "Six end rods stand on full purpur supports. Two age-5 chorus flowers sit on end stone outside the walkway. Two eye=false frame fragments sit on obsidian and cannot form a portal ring.",
            "The three-wide, three-high north opening is retained; the south end is a sealed broken-frame alcove, not a second exit.",
        ],
        "native": {"status": "PENDING"}, "deployed": False, "formal_saves_modified": False, "existing_jars_modified": False, "paid_image_api_calls": 0,
        "write_scope": [str(ROOT)],
        "read_only_tool_dependencies": [{"path": str(LEGACY / "nbtio.py"), "sha256": digest(LEGACY / "nbtio.py")}],
    }
    dump(DELIVERY / "qa.json", report)
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / "qa-resources/data/fts5qa/structures/empty.nbt")
    print(json.dumps({"status": report["status"], "path": str(DELIVERY), "sha256": metadata["sha256"], "reachable": len(reachable)}, ensure_ascii=False))


if __name__ == "__main__":
    main()
