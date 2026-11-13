from __future__ import annotations

import hashlib
import json
import shutil
from pathlib import Path

from nbtio import AIR, Structure, state


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out/M5"
REFERENCE_ROOT = Path("D:/ft工程文件/image2.5/dungeon-design-batch3-20261101")
REFERENCE = REFERENCE_ROOT / "out/M5-end-stargazer-terrace-hero.png"
REFERENCE_SHA256 = "dcd0ffbe2727cc55608994a09e224cac2a42d78bd1b60aca13d399d6cf858cae"
SIZE = (15, 10, 15)
ENTRANCE = (7, 1, 0)
INTERIOR = (7, 1, 2)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def build():
    if digest(REFERENCE) != REFERENCE_SHA256:
        raise ValueError("M5 reference fingerprint mismatch")
    structure = Structure(SIZE)
    structure.box((0, 0, 0), (14, 9, 14), AIR)

    def put(position, name, **properties):
        structure.set(position, state(name, **properties))

    def box(minimum, maximum, name, **properties):
        structure.box(minimum, maximum, state(name, **properties))

    for block_x in range(15):
        for block_z in range(15):
            material = "end_stone_bricks"
            if block_x in (0, 14) or block_z in (0, 14):
                material = "purpur_block" if (block_x + block_z) % 3 == 0 else "end_stone_bricks"
            elif block_x in (3, 11) or block_z in (3, 11):
                material = "purpur_block"
            elif (block_x + block_z) % 7 == 0:
                material = "end_stone"
            put((block_x, 0, block_z), material)
            roof = "glass"
            if block_x in (0, 14) or block_z in (0, 14):
                roof = "end_stone_bricks"
            elif block_x in (1, 7, 13) or block_z in (1, 7, 13):
                roof = "purpur_block"
            put((block_x, 9, block_z), roof)

    for along in range(15):
        for height in range(1, 9):
            for position in ((0, height, along), (14, height, along), (along, height, 0), (along, height, 14)):
                put(position, "end_stone_bricks")
        for height in range(3, 7):
            if along not in (0, 1, 4, 7, 10, 13, 14):
                for position in ((0, height, along), (14, height, along)):
                    put(position, "purple_stained_glass")
            if along in (2, 3, 11, 12):
                for position in ((along, height, 0), (along, height, 14)):
                    put(position, "purple_stained_glass")
        for height in (2, 7):
            for position in ((0, height, along), (14, height, along), (along, height, 0), (along, height, 14)):
                put(position, "purpur_block")
    for block_x, block_z in ((0, 0), (14, 0), (0, 14), (14, 14), (0, 7), (14, 7), (4, 14), (10, 14), (4, 0), (10, 0)):
        box((block_x, 1, block_z), (block_x, 8, block_z), "purpur_pillar", axis="y")

    box((5, 1, 5), (9, 1, 9), "purpur_block")
    box((6, 1, 6), (8, 1, 8), "obsidian")
    for block_x in range(6, 9):
        put((block_x, 1, 5), "purpur_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    for block_x in (5, 9):
        for block_z in (5, 9):
            for height in (2, 3):
                put((block_x, height, block_z), "end_rod", facing="up")
    box((6, 2, 6), (8, 2, 8), "purpur_block")
    put((7, 3, 7), "polished_blackstone_wall", east="none", west="none", north="none", south="none", up=True, waterlogged=False)
    put((7, 4, 7), "chiseled_deepslate")
    put((6, 4, 7), "purpur_stairs", facing="east", half="top", shape="straight", waterlogged=False)
    for block_x in (6, 7):
        put((block_x, 5, 7), "purpur_pillar", axis="x")
    put((5, 5, 7), "amethyst_block")
    put((4, 5, 7), "large_amethyst_bud", facing="west", waterlogged=False)
    put((6, 6, 7), "purpur_slab", type="bottom", waterlogged=False)
    put((5, 4, 7), "end_stone_brick_stairs", facing="east", half="top", shape="straight", waterlogged=False)
    put((8, 4, 7), "purpur_pillar", axis="x")
    put((9, 4, 7), "end_rod", facing="east")

    for block_x in (3, 11):
        box((block_x, 1, 11), (block_x, 4, 11), "obsidian")
        put((block_x, 5, 11), "amethyst_block")
        for offset_x, offset_z in ((-1, 0), (1, 0), (0, -1), (0, 1)):
            put((block_x + offset_x, 5, 11 + offset_z), "purple_stained_glass")
        put((block_x, 6, 11), "amethyst_cluster", facing="up", waterlogged=False)
        put((block_x, 1, 10), "purpur_block")
        put((block_x, 2, 10), "end_rod", facing="up")

    box((5, 1, 13), (5, 6, 13), "end_stone_bricks")
    box((9, 1, 13), (9, 5, 13), "end_stone_bricks")
    box((6, 7, 13), (8, 7, 13), "end_stone_bricks")
    put((5, 6, 13), "purpur_block")
    put((9, 6, 13), "purpur_stairs", facing="west", half="bottom", shape="straight", waterlogged=False)
    put((7, 8, 13), "purpur_block")
    put((7, 7, 13), "amethyst_block")
    box((6, 1, 14), (8, 6, 14), "black_stained_glass")
    for block_x, height in ((6, 1), (6, 2), (8, 1), (8, 2), (6, 6), (7, 6)):
        put((block_x, height, 13), "obsidian")
    put((8, 6, 13), "crying_obsidian")
    put((8, 1, 12), "purpur_slab", type="bottom", waterlogged=False)

    plants = ((2, 3, 2), (12, 3, 3), (1, 7, 2), (13, 7, 3), (1, 12, 3), (13, 12, 2))
    for block_x, block_z, stalk in plants:
        put((block_x, 0, block_z), "end_stone")
        for height in range(1, stalk + 1):
            put((block_x, height, block_z), "chorus_plant", up=True, down=True, east=False, west=False, north=False, south=False)
        put((block_x, stalk + 1, block_z), "chorus_flower", age=5)

    for block_x, block_z in ((1, 1), (13, 1), (1, 13), (13, 13)):
        put((block_x, 1, block_z), "purpur_block")
        put((block_x, 2, block_z), "end_rod", facing="up")
    for block_x in range(6, 9):
        for block_z in range(4):
            put((block_x, 0, block_z), "end_stone_bricks")
            for height in range(1, 4):
                put((block_x, height, block_z), "air")
    box((6, 4, 0), (8, 4, 0), "purpur_block")
    for block_x in (5, 9):
        box((block_x, 1, 0), (block_x, 4, 0), "end_stone_bricks")

    OUT.mkdir(parents=True, exist_ok=True)
    structure.write(OUT / "room.nbt")
    metadata = {
        "id": "M5", "name": "末地观星台", "name_en": "End Stargazer Terrace",
        "size": list(SIZE), "entrance": list(ENTRANCE), "interior": list(INTERIOR),
        "facing": "north", "sha256": digest(OUT / "room.nbt"),
        "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA256,
        "template_id": "ftdungeonmobs:image25/m5", "data_version": 3465,
        "edition": "Minecraft Java 1.20.1", "stage": "independent_delivery_not_deployed",
        "north_opening": {"minimum": [6, 1, 0], "maximum": [8, 3, 0], "width": 3, "height": 3},
        "walk_stations": [
            {"name": "北侧观星台前", "position": [7, 1, 4]},
            {"name": "西侧镜头观测位", "position": [4, 1, 7]},
            {"name": "东侧观测位", "position": [10, 1, 7]},
            {"name": "南侧破损门前", "position": [7, 1, 11]}
        ],
        "landmarks": {
            "telescope_lens": [5, 5, 7], "telescope_mount": [7, 3, 7],
            "broken_decorative_portal": [7, 4, 13],
            "inert_crystal_pedestals": [[3, 5, 11], [11, 5, 11]],
            "roof": {"y": 9, "complete": True, "kind": "glass_with_purpur_ribs"},
            "chorus_planters": [[block_x, 0, block_z] for block_x, block_z, _ in plants]
        },
        "adaptations": [
            "保持15×15占地；高度由概念h8调整为10，容纳玻璃星顶、完整屋檐与望远镜。",
            "遵守非花园完整屋顶契约，露天虚空改为透明玻璃星顶、紫玻璃侧窗与深色门后景；剖切只用于预览。",
            "浮岛意境以末地石与紫珀承台转译；模板满铺225格连续底板，不制造虚空坑或模板外悬浮岛。",
            "破损门只用黑曜石、末地石与紫珀装饰，不含活动传送门、末地传送门框或功能性门。",
            "末地水晶改为固定紫晶、紫玻璃和黑曜石基座，无实体、爆炸物或奖励库存。",
            "紫颂为末地石上合法支撑的竖直茎与age=5花，避免生长侵占干燥环路。"
        ],
        "preview_kind": "actual_nbt_software_cutaway_not_gameplay_screenshot",
        "integration_policy": "Coordinator imports M5 only after reviewing evidence; never rebuild existing rooms."
    }
    dump(OUT / "room.json", metadata)
    shutil.copy2(OUT / "room.nbt", ROOT / "native-test/src/main/resources/data/m5qa/structures/m5.nbt")
    print(json.dumps({"id": "M5", "size": SIZE, "sha256": metadata["sha256"], "record_count": len(structure.blocks)}, ensure_ascii=False))


if __name__ == "__main__":
    build()
