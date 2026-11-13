from __future__ import annotations

import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, state, string


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out"
SIZE = (19, 11, 23)
ENTRANCE = (9, 1, 0)
DESTINATIONS = ((9, 1, 2), (9, 1, 8), (5, 1, 12), (13, 1, 12), (9, 1, 16))
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M08-vine-overrun-hall-hero.png")
REFERENCE_SHA = "b1b4990e31ee724aad3bbe2185b636bc392d6bd0e79b46190bc4c972be607f81"


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def dump(path, payload):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(payload, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def masonry(block_x, height, block_z):
    sample = (block_x * 43 + height * 19 + block_z * 71 + block_x * block_z * 11) % 100
    return state("mossy_stone_bricks" if sample < 49 else "cracked_stone_bricks" if sample < 72 else "stone_bricks")


def stairs(facing, half="bottom"):
    return state("mossy_stone_brick_stairs", facing=facing, half=half, shape="straight", waterlogged=False)


def main():
    if digest(REFERENCE) != REFERENCE_SHA:
        raise ValueError("Reference fingerprint mismatch")
    room = Structure(SIZE)
    room.box((0, 0, 0), (18, 10, 22), AIR)
    for block_x in range(19):
        for block_z in range(23):
            floor = state("andesite") if block_x in (6, 12) and 2 <= block_z <= 17 else masonry(block_x, 0, block_z)
            room.set((block_x, 0, block_z), floor)
            roof = state("spruce_planks") if 2 <= block_x <= 16 and 2 <= block_z <= 20 else masonry(block_x, 10, block_z)
            if 12 <= block_x <= 15 and 17 <= block_z <= 20:
                roof = state("glass")
            room.set((block_x, 10, block_z), roof)
    for height in range(1, 10):
        for block_z in range(23):
            for block_x in (0, 18):
                room.set((block_x, height, block_z), masonry(block_x, height, block_z))
        for block_x in range(19):
            for block_z in (0, 22):
                room.set((block_x, height, block_z), masonry(block_x, height, block_z))
    for block_z in (2, 7, 12, 17, 21):
        room.box((1, 8, block_z), (17, 8, block_z), state("stripped_spruce_log", axis="x"))
    for block_x in (1, 17):
        room.box((block_x, 8, 1), (block_x, 8, 21), state("stripped_spruce_log", axis="z"))
        for pillar_z in (2, 7, 12, 17, 21):
            room.box((block_x, 1, pillar_z), (block_x, 7, pillar_z), state("mossy_stone_bricks"))
            inner_x = block_x + (1 if block_x == 1 else -1)
            room.set((inner_x, 1, pillar_z), stairs("west" if block_x == 1 else "east"))
            room.set((inner_x, 7, pillar_z), stairs("west" if block_x == 1 else "east", "top"))
        for bay_start, bay_end in ((3, 6), (8, 11), (13, 16), (18, 20)):
            room.set((block_x, 6, bay_start), stairs("south", "top"))
            room.set((block_x, 6, bay_end), stairs("north", "top"))
            room.box((block_x, 7, bay_start), (block_x, 7, bay_end), state("mossy_stone_bricks"))
    room.box((7, 1, 18), (11, 1, 20), state("mossy_stone_bricks"))
    for block_x in range(8, 11):
        room.set((block_x, 1, 17), stairs("south"))
        for block_z in (19, 20):
            room.set((block_x, 2, block_z), state("mossy_stone_brick_slab", type="bottom", waterlogged=False))
    for block_x in (7, 11):
        room.set((block_x, 2, 20), state("chiseled_stone_bricks"))
    room.box((8, 4, 21), (10, 7, 21), state("red_wool"))
    for position in ((9, 7, 21), (9, 6, 21), (8, 6, 21), (10, 6, 21), (9, 5, 21)):
        room.set(position, state("yellow_terracotta"))
    for block_x in (8, 10):
        room.set((block_x, 3, 21), state("red_wool"))
    for block_x in (5, 13):
        for block_z in (7, 17):
            room.box((block_x, 6, block_z), (block_x, 7, block_z), state("chain", axis="y", waterlogged=False))
            room.set((block_x, 5, block_z), state("lantern", hanging=True, waterlogged=False))
    barrels = ((2, 1, 5), (3, 1, 6), (2, 2, 5), (15, 1, 6), (16, 1, 11), (15, 1, 17), (16, 1, 19))
    for position in barrels:
        room.set(position, state("barrel", facing="up", open=False), Tag(10, {"id": string("minecraft:barrel"), "Items": Tag(9, [], 10)}))
    for position in ((3, 1, 9), (2, 1, 14), (3, 1, 20), (15, 1, 10), (15, 1, 20)):
        room.set(position, state("mossy_cobblestone"))
    for position in ((4, 1, 9), (3, 1, 14), (4, 1, 20), (14, 1, 20)):
        room.set(position, state("mossy_cobblestone_slab", type="bottom", waterlogged=False))
    for block_x, block_z in ((3, 3), (15, 3), (4, 11), (14, 14), (3, 17), (16, 8)):
        room.set((block_x, 0, block_z), state("moss_block"))
        room.set((block_x, 1, block_z), state("fern"))
    for block_x in (1, 17):
        for block_z in (4, 5, 9, 10, 14, 15, 19, 20):
            room.set((block_x, 8, block_z), state("oak_leaves", persistent=True, distance=7, waterlogged=False))
    for block_z in (20, 21):
        for block_x in (2, 3, 4, 13, 14, 15, 16):
            if room.get((block_x, 8, block_z)) == AIR:
                room.set((block_x, 8, block_z), state("oak_leaves", persistent=True, distance=7, waterlogged=False))
    for block_x, side in ((1, "west"), (17, "east")):
        for block_z in (4, 6, 9, 11, 14, 16, 19, 20):
            for height in range(2 + (block_z * 3 + block_x) % 3, 8):
                room.set((block_x, height, block_z), state("vine", **{side: True, "up": False, "north": False, "south": False, "east" if side == "west" else "west": False}))
    for block_x in (2, 16):
        side = "west" if block_x == 2 else "east"
        for block_z in (2, 7, 12, 17, 21):
            for height in range(3 + block_z % 3, 7):
                room.set((block_x, height, block_z), state("vine", **{side: True, "up": False, "north": False, "south": False, "east" if side == "west" else "west": False}))
    for block_x in (4, 6, 12, 14):
        for height in range(3 + block_x % 3, 8):
            room.set((block_x, height, 21), state("vine", south=True, north=False, east=False, west=False, up=False))
    for position in ((2, 7, 4), (16, 7, 14)):
        room.set(position, state("cobweb"))
    room.box((8, 1, 0), (10, 3, 0), AIR)
    room.box((7, 4, 0), (11, 4, 0), state("chiseled_stone_bricks"))
    room.write(OUT / "room.nbt")
    metadata = {
        "id": "M08", "name": "藤蔓侵蚀回廊", "size": list(SIZE),
        "entrance": list(ENTRANCE), "interior": list(DESTINATIONS[0]), "facing": "north",
        "sha256": digest(OUT / "room.nbt"), "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA,
        "data_version": 3465, "template_id_suggestion": "ftdungeonmobs:image25/m08",
        "route_targets": [list(position) for position in DESTINATIONS],
        "roof": "Sealed spruce/stone roof with bounded glass skylight; cutaway is preview-only.",
        "design_adaptations": ["19x11x23 elongated four-bay arcade", "Mossy stone, vines, beams, four chain lanterns, seven empty barrels and raised stone plinth", "Supported wool/terracotta textile motif instead of a banner entity", "Roof breach sealed with glass per non-garden contract", "Three-wide three-high north opening and five-wide dry central lane"],
        "deployment": "Independent handoff only; not registered or deployed; no player saves opened.",
        "placement_policy": "Explicit air is NOT clearing authorization. Preflight full volume/liquids/entities/routes; preserve persistent per-room marker and player edits."
    }
    dump(OUT / "room.json", metadata)
    materials = Counter(block.name for block in room.blocks.values() if block != AIR)
    dump(ROOT / "qa/materials.json", {"size": SIZE, "records": len(room.blocks), "non_air": sum(materials.values()), "materials": dict(sorted(materials.items())), "empty_barrels": len(barrels)})
    print(json.dumps({"id": "M08", "size": SIZE, "sha256": metadata["sha256"], "records": len(room.blocks), "non_air": sum(materials.values())}, ensure_ascii=False))


if __name__ == "__main__":
    main()
