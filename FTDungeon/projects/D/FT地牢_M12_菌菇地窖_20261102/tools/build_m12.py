from __future__ import annotations

import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (17, 11, 17)
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M12-fungal-cellar-hero.png")
REFERENCE_HASH = "827226cc6b2d2a0ad450bde5342825cb197898601d83a05854b313cb6e8df96f"
ENTRANCE = (8, 2, 0)
INTERIOR = (8, 2, 4)
LANDMARKS = {
    "entrance": ENTRANCE, "safe_interior": INTERIOR,
    "workbench_north": (8, 2, 6), "workbench_west": (6, 2, 8),
    "workbench_east": (10, 2, 8), "left_mushroom": (5, 2, 9),
    "right_mushroom": (11, 2, 10), "rear_shelves": (8, 2, 13),
    "ladder_approach": (12, 2, 14), "pool_dry_edge": (5, 2, 4),
}


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def generate():
    if digest(REFERENCE) != REFERENCE_HASH:
        raise ValueError("Reference fingerprint mismatch")
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (16, 10, 16), AIR)
    structure.box((0, 0, 0), (16, 0, 16), state("stone_bricks"))
    masonry = ["stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "mossy_stone_bricks", "andesite"]
    for pos_x in range(17):
        for pos_z in range(17):
            structure.set((pos_x, 1, pos_z), state(masonry[(pos_x * 7 + pos_z * 11 + pos_x * pos_z) % 5]))
            structure.set((pos_x, 10, pos_z), state("stone_bricks"))
            if pos_x in (0, 16) or pos_z in (0, 16):
                for pos_y in range(2, 10):
                    structure.set((pos_x, pos_y, pos_z), state(masonry[(pos_x * 3 + pos_z * 5 + pos_y * 7) % 5]))
    for pos_x, pos_z in [(0, 0), (16, 0), (0, 16), (16, 16), (0, 5), (16, 5), (0, 12), (16, 12)]:
        for pos_y in range(2, 10):
            structure.set((pos_x, pos_y, pos_z), state("mossy_stone_bricks" if pos_y % 3 else "chiseled_stone_bricks"))
    structure.box((7, 2, 0), (9, 4, 0), AIR)
    structure.box((7, 1, 0), (9, 1, 5), state("stone_bricks"))
    for pos_z in (5, 12):
        structure.box((0, 9, pos_z), (16, 9, pos_z), state("stripped_spruce_log", axis="x"))
    for pos_x in (1, 15):
        structure.box((pos_x, 9, 1), (pos_x, 9, 15), state("stripped_spruce_log", axis="z"))
    for low_x, low_z, high_x, high_z in [(1, 7, 5, 11), (11, 8, 15, 12)]:
        structure.box((low_x, 1, low_z), (high_x, 1, high_z), state("moss_block"))
        structure.box((low_x + 1, 1, low_z + 1), (high_x - 1, 1, high_z - 1), state("podzol", snowy=False))
    directions = {"north": (0, 0, -1), "south": (0, 0, 1), "east": (1, 0, 0), "west": (-1, 0, 0), "up": (0, 1, 0), "down": (0, -1, 0)}
    for center_x, center_z in [(3, 9), (13, 10)]:
        for pos_y in range(2, 6):
            structure.set((center_x, pos_y, center_z), state("mushroom_stem", north=True, south=True, east=True, west=True, up=pos_y == 5, down=pos_y == 2))
        cap = {(center_x + offset_x, 6, center_z + offset_z) for offset_x in range(-2, 3) for offset_z in range(-2, 3) if abs(offset_x) + abs(offset_z) < 4}
        cap.update((center_x + offset_x, 7, center_z + offset_z) for offset_x in range(-1, 2) for offset_z in range(-1, 2))
        for position in sorted(cap):
            exposed = {direction: direction != "down" and tuple(position[axis] + delta[axis] for axis in range(3)) not in cap for direction, delta in directions.items()}
            structure.set(position, state("red_mushroom_block", **exposed))
        for offset_x, offset_z in [(-1, -1), (0, -1), (1, -1), (-1, 1), (0, 1), (1, 1)]:
            structure.set((center_x + offset_x, 2, center_z + offset_z), state("red_mushroom"))
    for pos_y in (2, 4, 6):
        structure.box((5, pos_y, 15), (11, pos_y, 15), state("spruce_planks"))
    for pos_x in (5, 8, 11):
        structure.box((pos_x, 2, 15), (pos_x, 7, 15), state("stripped_spruce_log", axis="y"))
    containers = [(1, 2, 12), (15, 2, 14), (4, 2, 14), (6, 3, 15), (10, 3, 15), (7, 5, 15), (9, 5, 15), (8, 4, 8)]
    for position in containers:
        structure.set(position, state("barrel", facing="north", open=False), compound(id=string("minecraft:barrel")))
    for position, plant in [((7, 3, 15), "red"), ((9, 3, 15), "brown"), ((6, 5, 15), "brown"), ((10, 5, 15), "red"), ((6, 7, 15), "red"), ((7, 7, 15), "brown"), ((9, 7, 15), "red"), ((10, 7, 15), "brown"), ((1, 3, 12), "red"), ((15, 3, 14), "brown")]:
        structure.set(position, state(f"potted_{plant}_mushroom"))
    for pos_y in range(2, 7):
        structure.set((12, pos_y, 15), state("ladder", facing="north", waterlogged=False))
    for pos_x in (7, 9):
        for pos_z in (7, 8):
            structure.set((pos_x, 2, pos_z), state("stripped_spruce_log", axis="y"))
    structure.box((7, 3, 7), (9, 3, 8), state("spruce_planks"))
    structure.set((7, 4, 7), state("potted_red_mushroom"))
    structure.set((9, 4, 8), state("potted_brown_mushroom"))
    water = []
    for pos_x in range(2, 5):
        for pos_z in range(3, 5):
            position = (pos_x, 1, pos_z)
            structure.set(position, state("water", level=0))
            water.append(position)
    for pos_x in (6, 10):
        for pos_y in (7, 8):
            structure.set((pos_x, pos_y, 5), state("chain", axis="y", waterlogged=False))
        structure.set((pos_x, 6, 5), state("lantern", hanging=True, waterlogged=False))
    for position in [(2, 8, 5), (14, 8, 5), (3, 8, 12), (13, 8, 12)]:
        structure.set(position, state("hanging_roots", waterlogged=False))
    for position in [(4, 8, 12), (12, 8, 5)]:
        structure.set(position, state("spore_blossom"))
    for position, direction in [((1, 4, 5), "west"), ((15, 4, 6), "east"), ((3, 5, 15), "south"), ((13, 5, 15), "south"), ((1, 5, 13), "west"), ((15, 5, 13), "east")]:
        faces = {face: False for face in directions}
        faces[direction] = True
        structure.set(position, state("glow_lichen", waterlogged=False, **faces))
    structure.write(ROOT / "room.nbt")
    nbt_hash = digest(ROOT / "room.nbt")
    write_json(ROOT / "room.json", {
        "id": "M12", "name": "菌菇地窖", "size": SIZE,
        "entrance": ENTRANCE, "interior": INTERIOR, "facing": "north",
        "sha256": nbt_hash, "reference": str(REFERENCE), "reference_sha256": REFERENCE_HASH,
        "minecraft": "1.20.1", "data_version": 3465,
        "template_id_proposed": "ftdungeonmobs:image25/m12",
        "coordinate_convention": "Relative integer XYZ at player feet; floor is one block below",
        "landmarks": LANDMARKS, "liquid_cells": water,
        "roof": "Complete stone roof at y=10; cutaway is preview-only",
        "placement": "Independent handoff only; fixed slot and integration are coordinator-owned",
        "inventory": "Eight empty barrels; no rewards, loot tables, spawners or entities",
        "reference_adaptations": ["Expanded to 17x17 for a three-wide entrance and two routes around the table", "Fixed vanilla giant mushrooms and potted mushrooms representing jars", "Six shallow water sources, complete roof, supported spore blossoms"],
    })
    counts = Counter(block.name for block in structure.blocks.values() if block != AIR)
    write_json(ROOT / "evidence/materials.json", dict(sorted(counts.items())))
    write_json(ROOT / "qa.json", {"id": "M12", "status": "PENDING", "sha256": nbt_hash, "native": "NOT_RUN", "production_deployed": False, "full_modpack_playtest": False})
    print(json.dumps({"status": "BUILT", "size": SIZE, "records": len(structure.blocks), "non_air": sum(counts.values()), "sha256": nbt_hash}, ensure_ascii=False))


if __name__ == "__main__":
    generate()
