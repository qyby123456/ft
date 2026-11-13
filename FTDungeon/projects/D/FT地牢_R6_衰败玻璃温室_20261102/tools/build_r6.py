from __future__ import annotations

import hashlib
import json
import math
import sys
from collections import Counter, deque
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / "tools/vendor"))
from nbtio import AIR, Structure, state

SIZE = (15, 10, 17)
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R6-overgrown-greenhouse.png")
REFERENCE_HASH = "525b5a4b3c1043be2d45abe0502274509c0d5f1ffa376bb1251a4a964aee13ee"
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def hash_file(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def write_json(path, payload):
    Path(path).write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def leaf_distances(structure):
    distances = {}
    pending = deque()
    for position, block in structure.blocks.items():
        if block.name.endswith("_log"):
            distances[position] = 0
            pending.append(position)
    while pending:
        position = pending.popleft()
        for delta in DIRECTIONS:
            neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
            if neighbor in distances or not structure.get(neighbor).name.endswith("_leaves"):
                continue
            distance = min(distances[position] + 1, 7)
            distances[neighbor] = distance
            if distance < 7:
                pending.append(neighbor)
    for position, block in list(structure.blocks.items()):
        if block.name.endswith("_leaves"):
            structure.set(position, state(block.name, distance=distances.get(position, 7), persistent=True, waterlogged=False))


def make_structure():
    structure = Structure(SIZE)
    structure.box((0, 0, 0), tuple(limit - 1 for limit in SIZE), AIR)
    for block_x in range(SIZE[0]):
        for block_z in range(SIZE[2]):
            selector = (block_x * 17 + block_z * 29 + block_x * block_z) % 13
            material = "mossy_stone_bricks" if selector < 4 else "cracked_stone_bricks" if selector == 4 else "stone_bricks"
            if 6 <= block_x <= 8:
                material = "polished_andesite" if (block_x + block_z) % 5 else "stone_bricks"
            structure.set((block_x, 0, block_z), state(material))
    for block_x in range(SIZE[0]):
        for block_z in range(SIZE[2]):
            if block_x in (0, 14) or block_z in (0, 16):
                for block_y in range(1, 9):
                    material = "mossy_stone_bricks" if block_y <= 2 else "glass"
                    structure.set((block_x, block_y, block_z), state(material))
            frame = block_x in (0, 4, 7, 10, 14) or block_z in (0, 4, 8, 12, 16)
            structure.set((block_x, 9, block_z), state("waxed_weathered_cut_copper" if frame else "glass"))
    pillars = {(block_x, block_z) for block_x in (0, 14) for block_z in (0, 4, 8, 12, 16)}
    pillars |= {(block_x, block_z) for block_x in (4, 10) for block_z in (0, 16)}
    for block_x, block_z in sorted(pillars):
        for block_y in range(1, 9):
            material = "mossy_stone_bricks" if block_y < 3 else "deepslate_tiles" if block_y in (3, 6, 8) else "waxed_weathered_cut_copper"
            structure.set((block_x, block_y, block_z), state(material))
    structure.box((5, 4, 0), (9, 4, 0), state("deepslate_tiles"))
    for block_x in (5, 9):
        structure.box((block_x, 1, 0), (block_x, 3, 0), state("mossy_stone_bricks"))
    structure.box((6, 1, 0), (8, 3, 0), AIR)

    water, crops = [], []
    for west in (True, False):
        low_x, high_x = (2, 5) if west else (9, 12)
        crop_x, water_x = (3, 4) if west else (11, 10)
        for block_x in range(low_x, high_x + 1):
            for block_z in range(5, 12):
                border = block_x in (low_x, high_x) or block_z in (5, 11)
                structure.set((block_x, 1, block_z), state("mossy_stone_bricks" if border else "moss_block"))
        for block_z in range(6, 11):
            structure.set((crop_x, 1, block_z), state("farmland", moisture=7))
            structure.set((crop_x, 2, block_z), state("wheat" if (block_z + int(west)) % 2 else "carrots", age=7))
            crops.append([crop_x, 2, block_z])
        for block_z in range(7, 11):
            structure.set((water_x, 1, block_z), state("water", level=0))
            water.append([water_x, 1, block_z])
        for block_z in (7, 10):
            structure.set((low_x if west else high_x, 1, block_z), state("shroomlight"))
        structure.set((water_x, 2, 6), state("oxeye_daisy" if west else "azure_bluet"))
        for block_x in range(low_x, high_x + 1):
            for block_z in (2, 3):
                structure.set((block_x, 1, block_z), state("moss_block" if (block_x + block_z) % 2 else "rooted_dirt"))
                plant = ("fern", "oxeye_daisy", "flowering_azalea", "blue_orchid")[(block_x + block_z) % 4]
                structure.set((block_x, 2, block_z), state(plant))
    return finish_structure(structure, water, crops)


def finish_structure(structure, water, crops):
    for block_x in range(5, 10):
        for block_z in range(13, 16):
            border = block_x in (5, 9) or block_z in (13, 15)
            structure.set((block_x, 1, block_z), state("mossy_stone_bricks" if border else "rooted_dirt"))
    for block_y in range(2, 6):
        structure.set((7, block_y, 14), state("oak_log", axis="y"))
    structure.set((6, 4, 14), state("oak_log", axis="x"))
    structure.set((8, 5, 14), state("oak_log", axis="x"))
    for block_x in range(4, 11):
        for block_y in range(4, 8):
            for block_z in range(11, 16):
                radial = ((block_x - 7) / 3.2) ** 2 + ((block_y - 5.6) / 2.1) ** 2 + ((block_z - 13.5) / 2.8) ** 2
                if radial <= 1 and not structure.get((block_x, block_y, block_z)).name.endswith("_log"):
                    material = "flowering_azalea_leaves" if (block_x * 7 + block_y * 3 + block_z) % 11 == 0 else "oak_leaves"
                    structure.set((block_x, block_y, block_z), state(material, distance=7, persistent=True, waterlogged=False))
    for block_x in (1, 3):
        for block_z in (14, 15):
            structure.set((block_x, 1, block_z), state("spruce_log", axis="y"))
    structure.box((1, 2, 14), (3, 2, 15), state("spruce_slab", type="top", waterlogged=False))
    structure.set((1, 3, 14), state("potted_fern"))
    structure.set((2, 3, 15), state("potted_brown_mushroom"))
    structure.set((3, 3, 14), state("lantern", hanging=False, waterlogged=False))
    structure.set((12, 1, 13), state("composter", level=0))
    structure.set((12, 1, 15), state("composter", level=0))
    structure.set((11, 1, 14), state("coarse_dirt"))
    structure.set((11, 2, 14), state("potted_dead_bush"))
    lanterns = []
    for block_z in (4, 10):
        structure.set((7, 6, block_z), state("lantern", hanging=True, waterlogged=False))
        structure.box((7, 7, block_z), (7, 8, block_z), state("chain", axis="y", waterlogged=False))
        lanterns.append([7, 6, block_z])
    baskets = []
    for block_x in (3, 11):
        structure.set((block_x, 6, 4), state("rooted_dirt"))
        structure.box((block_x, 7, 4), (block_x, 8, 4), state("chain", axis="y", waterlogged=False))
        structure.set((block_x, 5, 4), state("cave_vines", age=25, berries=True))
        for delta_x, delta_z, facing in ((-1, 0, "east"), (1, 0, "west"), (0, -1, "south"), (0, 1, "north")):
            structure.set((block_x + delta_x, 6, 4 + delta_z), state("spruce_trapdoor", facing=facing, half="bottom", open=True, powered=False, waterlogged=False))
        baskets.append([block_x, 6, 4])
    vines = []
    for block_x, block_z, bottom in ((1, 4, 4), (13, 4, 4), (1, 8, 5), (13, 8, 5), (1, 12, 4), (13, 12, 4), (4, 1, 5), (10, 1, 5), (4, 15, 5), (10, 15, 5)):
        for block_y in range(bottom, 9):
            plant = state("cave_vines", age=25, berries=False) if block_y == bottom else state("cave_vines_plant", berries=False)
            structure.set((block_x, block_y, block_z), plant)
            vines.append([block_x, block_y, block_z])
    leaf_distances(structure)
    features = {"irrigation_sources": water, "mature_crops": crops, "hanging_baskets": baskets, "ceiling_lanterns": lanterns, "fixed_length_vines": vines, "tree_root": [7, 2, 14], "roof_y": 9, "main_dry_aisle": {"x": [6, 8], "y": 1, "z": [0, 12]}}
    return structure, features


def main():
    if hash_file(REFERENCE) != REFERENCE_HASH:
        raise ValueError("The assigned reference image SHA-256 does not match")
    structure, features = make_structure()
    structure.write(ROOT / "room.nbt")
    counts = Counter(block.name for block in structure.blocks.values())
    metadata = {
        "id": "R6", "name": "衰败玻璃温室", "english_name": "Overgrown Greenhouse",
        "size": list(SIZE), "entrance": [7, 1, 0], "interior": [7, 1, 10], "facing": "north",
        "sha256": hash_file(ROOT / "room.nbt"), "reference": str(REFERENCE), "reference_sha256": REFERENCE_HASH,
        "data_version": 3465, "minecraft_version": "1.20.1",
        "coordinate_convention": "Relative integer XYZ; entrance/interior are player feet positions",
        "features": features, "block_records": math.prod(SIZE),
        "nonair_blocks": sum(count for name, count in counts.items() if name != "minecraft:air"),
        "scope": "Independent unregistered room. No catalog slot/origin, deployment, core edits or player-world writes.",
        "reference_adaptations": [
            "Three-wide central path stays dry; two sealed side troughs replace the exposed central channel.",
            "The NBT preserves a continuous glass roof and shell; cutaways are preview-only.",
            "Persistent leaves, hydrated mature crops, four bed-integrated shroomlights and age-25 hanging vines avoid decay, darkness and extension.",
            "Vanilla potting table and composters replace concept-only bottles, sacks and wheelbarrow."
        ]
    }
    write_json(ROOT / "room.json", metadata)
    write_json(ROOT / "qa/materials.json", {"size": list(SIZE), "blocks": dict(sorted(counts.items())), "palette_states": len(set(structure.blocks.values())), "block_entities": 0, "entities": 0})
    print(json.dumps({"id": "R6", "size": SIZE, "records": len(structure.blocks), "nonair": metadata["nonair_blocks"], "sha256": metadata["sha256"]}))


if __name__ == "__main__":
    main()
