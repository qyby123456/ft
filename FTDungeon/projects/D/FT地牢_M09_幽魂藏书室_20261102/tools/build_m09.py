from __future__ import annotations

import hashlib
import json
from pathlib import Path

from nbtio import AIR, Structure, state


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out/M09"
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M09-haunted-scriptorium-hero.png")
REFERENCE_SHA256 = "4bafda1f3ec74ed1aaa0daa66d58a5e0f3e1267b1287d8635331143df256f16b"
SIZE = (21, 12, 21)
ENTRANCE = (10, 1, 0)
INTERIOR = (10, 1, 2)
DESTINATIONS = ((10, 1, 2), (3, 1, 6), (16, 1, 7), (5, 1, 12), (15, 1, 12), (10, 1, 18))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def dump(path, value):
    Path(path).parent.mkdir(parents=True, exist_ok=True)
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def build():
    if digest(REFERENCE) != REFERENCE_SHA256:
        raise ValueError("M09 design fingerprint mismatch")
    room = Structure(SIZE)
    room.box((0, 0, 0), (20, 11, 20), AIR)

    def put(position, material, **properties):
        room.set(position, state(material, **properties))

    def box(start, end, material, **properties):
        room.box(start, end, state(material, **properties))

    for block_x in range(21):
        for block_z in range(21):
            pattern = (block_x * 17 + block_z * 31) % 19
            floor = "stone_bricks"
            if pattern in (0, 3, 8):
                floor = "cracked_stone_bricks"
            elif pattern in (4, 11):
                floor = "mossy_stone_bricks"
            if 15 <= block_x <= 19 and 12 <= block_z <= 18:
                floor = "polished_blackstone_bricks" if pattern % 3 else "cracked_polished_blackstone_bricks"
            put((block_x, 0, block_z), floor)
            put((block_x, 11, block_z), "deepslate_tiles" if block_x in (3, 10, 17) else "stone_bricks")
    for along in range(21):
        for height in range(1, 11):
            for position in ((0, height, along), (20, height, along), (along, height, 0), (along, height, 20)):
                pattern = (position[0] * 7 + position[1] * 13 + position[2] * 11) % 23
                material = "mossy_stone_bricks" if pattern in (2, 9, 14) else "cracked_stone_bricks" if pattern in (0, 1, 6) else "stone_bricks"
                if position[0] == 20 and 12 <= along <= 18:
                    material = "cracked_deepslate_bricks" if pattern % 3 else "polished_blackstone_bricks"
                put(position, material)
    columns = [(block_x, block_z) for block_x in (0, 20) for block_z in (0, 3, 10, 18, 20)]
    columns += [(block_x, block_z) for block_z in (0, 20) for block_x in (3, 7, 13, 17)]
    for block_x, block_z in columns:
        box((block_x, 1, block_z), (block_x, 10, block_z), "polished_andesite")
        put((block_x, 8, block_z), "chiseled_stone_bricks")
    for block_x in (1, 19):
        for block_z in (3, 10, 18):
            box((block_x, 1, block_z), (block_x, 8, block_z), "stone_bricks")
            put((block_x, 8, block_z), "chiseled_stone_bricks")
    for block_z in (3, 10, 18):
        box((0, 9, block_z), (20, 9, block_z), "dark_oak_log", axis="x")
    for block_x in (3, 10, 17):
        box((block_x, 10, 0), (block_x, 10, 20), "dark_oak_log", axis="z")
    box((8, 4, 19), (12, 8, 19), "stone_bricks")
    for block_x in range(9, 12):
        for height in range(4, 8):
            put((block_x, height, 20), "cyan_stained_glass")
            put((block_x, height, 19), "iron_bars", east=True, west=True, north=False, south=True, waterlogged=False)
    put((10, 6, 20), "sea_lantern")
    box((8, 3, 19), (12, 3, 19), "polished_andesite")

    shelf_positions = []
    for start, end in ((4, 8), (12, 16)):
        for block_z in range(start, end + 1):
            for height in range(1, 6):
                put((1, height, block_z), "bookshelf")
                shelf_positions.append((1, height, block_z))
            put((1, 6, block_z), "dark_oak_planks")
    for block_x in list(range(3, 8)) + list(range(13, 18)):
        for height in range(1, 7):
            put((block_x, height, 19), "bookshelf")
            shelf_positions.append((block_x, height, 19))
        put((block_x, 7, 19), "dark_oak_planks")
    for block_z in range(4, 9):
        for height in range(1, 6):
            put((19, height, block_z), "bookshelf")
            shelf_positions.append((19, height, block_z))
        put((19, 6, block_z), "dark_oak_planks")
        put((18, block_z - 3, 8), "ladder", facing="west", waterlogged=False)
    put((18, 6, 8), "ladder", facing="west", waterlogged=False)

    for block_x in range(7, 14):
        for block_z in range(4, 12):
            border = block_x in (7, 13) or block_z in (4, 11)
            put((block_x, 0, block_z), "black_wool" if border else "purple_wool")
    for block_z in (4, 11):
        for block_x in (8, 9, 11, 12):
            put((block_x, 0, block_z), "yellow_terracotta")
    for block_z in (5, 7, 8, 10):
        for block_x in (7, 13):
            put((block_x, 0, block_z), "yellow_terracotta")
    for block_x, block_z in ((10, 6), (9, 7), (11, 7), (10, 8)):
        put((block_x, 0, block_z), "yellow_terracotta")

    box((6, 2, 13), (14, 2, 14), "dark_oak_planks")
    for block_x in (6, 14):
        for block_z in (13, 14):
            put((block_x, 1, block_z), "dark_oak_log", axis="y")
    for block_x in (7, 13):
        put((block_x, 1, 14), "barrel", facing="north", open=False)
    put((10, 2, 13), "enchanting_table")
    for block_x, block_z in ((6, 13), (14, 13)):
        put((block_x, 3, block_z), "candle", candles=4, lit=True, waterlogged=False)
    put((8, 3, 14), "flower_pot")
    put((12, 3, 14), "flower_pot")
    put((4, 1, 12), "lectern", facing="north", has_book=False, powered=False)
    put((10, 1, 16), "dark_oak_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
    for block_x in range(9, 12):
        box((block_x, 1, 17), (block_x, 3, 17), "red_wool" if block_x == 10 else "dark_oak_planks")
    box((9, 4, 17), (11, 4, 17), "dark_oak_planks")
    for block_x, block_z in ((4, 7), (4, 3), (16, 3)):
        put((block_x, 1, block_z), "dark_oak_planks")
    put((4, 2, 7), "skeleton_skull", rotation=4)
    for block_x in (4, 16):
        put((block_x, 2, 3), "candle", candles=3, lit=True, waterlogged=False)

    for block_z in range(13, 18):
        put((19, 1, block_z), "dark_oak_log", axis="z")
        if block_z != 15:
            put((19, 2, block_z), "bookshelf")
            shelf_positions.append((19, 2, block_z))
    for block_z in (14, 16):
        put((19, 3, block_z), "bookshelf")
        shelf_positions.append((19, 3, block_z))
    put((19, 4, 16), "dark_oak_planks")
    rubble = [(18, 1, 14), (18, 1, 15), (18, 1, 16), (17, 1, 15), (17, 1, 16), (16, 1, 16), (18, 2, 16)]
    for rubble_index, position in enumerate(rubble):
        put(position, "cobbled_deepslate" if rubble_index % 2 else "cracked_polished_blackstone_bricks")
    for position in ((17, 1, 14), (16, 1, 15), (18, 1, 17), (15, 1, 17)):
        put(position, "cobbled_deepslate_slab", type="bottom", waterlogged=False)
    put((17, 2, 16), "dark_oak_log", axis="z")
    put((18, 3, 16), "dark_oak_stairs", facing="east", half="bottom", shape="straight", waterlogged=False)

    for position in ((1, 7, 5), (1, 7, 15), (19, 6, 15), (19, 4, 12), (18, 3, 17)):
        put(position, "cobweb")
    for block_x in (4, 16):
        put((block_x, 5, 1), "purple_wall_banner", facing="south")
    for position in ((1, 7, 7), (1, 7, 13), (5, 8, 19), (15, 8, 19), (19, 7, 6)):
        put(position, "candle", candles=3, lit=True, waterlogged=False)
    for block_x in (7, 10, 13):
        put((block_x, 8, 10), "chain", axis="y", waterlogged=False)
        put((block_x, 7, 10), "soul_lantern", hanging=True, waterlogged=False)

    box((9, 1, 0), (11, 3, 0), "air")
    for block_x in range(8, 13):
        put((block_x, 4, 0), "chiseled_stone_bricks")
    room.write(OUT / "room.nbt")
    actual_shelves = [list(position) for position in shelf_positions if room.get(position).name == "minecraft:bookshelf"]
    dump(OUT / "room.json", {
        "id": "M09", "name": "幽魂藏书室", "english_name": "Haunted Scriptorium",
        "size": SIZE, "entrance": ENTRANCE, "interior": INTERIOR, "facing": "north",
        "sha256": digest(OUT / "room.nbt"), "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA256,
        "data_version": 3465, "minecraft": "1.20.1", "theme": "焚毁残破藏书室",
        "walk_destinations": DESTINATIONS, "role": "combat", "deployment": "independent_only_not_installed",
        "features": ["落地书墙与完整横梁", "开放式抄写桌与原生附魔书动画", "无书物品的装饰讲台", "镶平地毯主通道", "炭化东侧书架和有底坍塌堆", "魂灯窗与暖色烛光", "完整石质屋顶"],
        "reward_inventory": "none; barrels and lectern must remain empty",
        "design_adaptations": ["参考剖切不用于交付：成品保留全屋顶和四墙，仅北侧三宽三高开口", "焚毁以炭黑砌石、断架和残木表现，不放真实火/熔岩", "中央地毯为地板镶嵌羊毛，避免脚位碰撞", "不放可领取书本、战利品、实体或刷怪器"],
    })
    dump(ROOT / "qa/design-ledger.json", {
        "id": "M09", "sha256": digest(OUT / "room.nbt"), "bookshelves": actual_shelves,
        "columns": columns, "cross_beams": {"y": 9, "z": [3, 10, 18], "x": [0, 20]},
        "longitudinal_beams": {"y": 10, "x": [3, 10, 17], "z": [0, 20]},
        "rubble": rubble, "containers": [[7, 1, 14], [13, 1, 14]],
        "scope": "This ledger records intended construction; static and native QA independently read the delivered NBT.",
    })
    print(f"M09 built: {SIZE}, {len(room.blocks)} records, SHA256={digest(OUT / 'room.nbt')}")


if __name__ == "__main__":
    build()
