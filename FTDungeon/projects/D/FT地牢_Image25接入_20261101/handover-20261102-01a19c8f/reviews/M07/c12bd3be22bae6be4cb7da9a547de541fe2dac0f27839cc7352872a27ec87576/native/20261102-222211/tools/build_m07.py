from __future__ import annotations

import hashlib
import json
from pathlib import Path

from nbtio import AIR, Structure, compound, read_nbt, state, string


ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / "out/M07"
SIZE = (15, 13, 17)
FEET = 4
ENTRANCE = (7, FEET, 0)
INTERIOR = (7, FEET, 3)
TARGETS = ((3, FEET, 9), (11, FEET, 9), (7, FEET, 13), (7, FEET, 15))
REFERENCE = Path("D:/ft工程文件/image2.5/dungeon-rooms-medium-random-20261102/out/M07-oubliette-well-hero.png")
REFERENCE_SHA = "66b0acc53cad4302ad7daa3589df6181055e2ce89b84947fbb565ce835c6d775"


def dump(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def make_room():
    room = Structure(SIZE)
    room.box((0, 0, 0), (14, 12, 16), AIR)
    room.box((0, 0, 0), (14, 2, 16), state("stone_bricks"))

    def put(position, name, **properties):
        room.set(position, state(name, **properties))

    floor_palette = ("stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "mossy_stone_bricks", "cobblestone", "polished_andesite")
    wall_palette = ("stone_bricks", "mossy_stone_bricks", "mossy_stone_bricks", "cracked_stone_bricks", "stone_bricks")
    for block_x in range(SIZE[0]):
        for block_z in range(SIZE[2]):
            put((block_x, 3, block_z), floor_palette[(block_x * 17 + block_z * 23 + block_x * block_z) % len(floor_palette)])
            put((block_x, 11, block_z), "spruce_planks")
            put((block_x, 12, block_z), "mossy_stone_bricks" if (block_x + 2 * block_z) % 5 == 0 else "stone_bricks")
            if block_x in (0, 14) or block_z in (0, 16):
                for block_y in range(4, 11):
                    put((block_x, block_y, block_z), wall_palette[(block_x * 11 + block_z * 7 + block_y * 3) % len(wall_palette)])
                put((block_x, 4, block_z), "mossy_stone_bricks")
                put((block_x, 10, block_z), "chiseled_stone_bricks")
    for block_z in (4, 9, 14):
        for block_x in (1, 13):
            for block_y in range(4, 10):
                put((block_x, block_y, block_z), "chiseled_stone_bricks" if block_y in (4, 9) else "mossy_stone_bricks")
        for block_x in range(1, 14):
            put((block_x, 10, block_z), "spruce_log", axis="x")
    for block_x in range(6, 9):
        for block_z in range(4):
            put((block_x, 3, block_z), "polished_andesite")
        for block_y in range(4, 7):
            room.set((block_x, block_y, 0), AIR)
    for block_x in (5, 9):
        for block_y in range(4, 8):
            put((block_x, block_y, 0), "chiseled_stone_bricks" if block_y == 7 else "stone_bricks")
    for block_x in range(6, 9):
        put((block_x, 7, 0), "chiseled_stone_bricks")

    for block_x in range(5, 10):
        for block_z in range(7, 12):
            if block_x in (5, 9) or block_z in (7, 11):
                for block_y in range(4):
                    put((block_x, block_y, block_z), "mossy_stone_bricks")
                put((block_x, 4, block_z), "mossy_stone_bricks" if (block_x + block_z) % 3 else "stone_bricks")
                put((block_x, 5, block_z), "stone_brick_slab", type="bottom", waterlogged=False)
            else:
                for block_y in range(1, 4):
                    if (block_x, block_z) == (7, 9):
                        put((block_x, block_y, block_z), "iron_block")
                    else:
                        put((block_x, block_y, block_z), "water", level=0)
                facing = "west" if block_x == 6 else "east" if block_x == 8 else "north" if block_z == 8 else "south"
                put((block_x, 4, block_z), "iron_trapdoor", facing=facing, half="top", open=False, powered=False, waterlogged=False)
    for block_z in range(5, 14):
        put((12, 2, block_z), "water", level=0)
        put((12, 3, block_z), "iron_trapdoor", facing="east", half="top", open=False, powered=False, waterlogged=False)
    for block_y in range(6, 10):
        put((7, block_y, 9), "chain", axis="y", waterlogged=False)

    for first_x in (3, 9):
        for jamb_x in (first_x - 1, first_x + 3):
            for block_z in (14, 15):
                for block_y in range(4, 8):
                    put((jamb_x, block_y, block_z), "mossy_stone_bricks")
        for block_x in range(first_x, first_x + 3):
            for block_y in range(4, 7):
                put((block_x, block_y, 14), "iron_bars", east=True, west=True, north=False, south=False, waterlogged=False)
            put((block_x, 7, 14), "stone_bricks")
            put((block_x, 8, 14), "stone_brick_slab", type="bottom", waterlogged=False)
    for block_x, block_z, facing in ((1, 6, "east"), (13, 12, "west"), (3, 15, "north"), (11, 15, "north")):
        for block_y in range(6, 11):
            put((block_x, block_y, block_z), "chain", axis="y", waterlogged=False)
        put((block_x, 5, block_z), "tripwire_hook", facing=facing, attached=False, powered=False)
    for block_x, block_y, block_z in ((7, 8, 4), (3, 7, 9), (11, 7, 9), (7, 8, 14)):
        put((block_x, block_y, block_z), "lantern", hanging=True, waterlogged=False)
        for chain_y in range(block_y + 1, 10):
            put((block_x, chain_y, block_z), "chain", axis="y", waterlogged=False)
    put((1, 6, 3), "wall_torch", facing="east")
    put((13, 6, 3), "wall_torch", facing="west")
    put((1, 5, 8), "lever", face="wall", facing="east", powered=False)
    put((2, 4, 11), "grindstone", face="floor", facing="east")
    put((10, 4, 10), "cauldron")
    for block_x, block_z in ((2, 5), (2, 6), (3, 12), (11, 13), (12, 14), (3, 15), (11, 15)):
        put((block_x, 3, block_z), "hay_block", axis="y")
    for position, rotation in (((2, 4, 6), 3), ((11, 4, 13), 12)):
        room.set(position, state("skeleton_skull", rotation=rotation), compound(id=string("minecraft:skull")))
    return room


def main():
    from static_qa import static_review

    OUT.mkdir(parents=True, exist_ok=True)
    room = make_room()
    room.write(OUT / "room.nbt")
    loaded = Structure.read(OUT / "room.nbt")
    assert loaded.blocks == room.blocks and loaded.block_entities == room.block_entities
    _, tag = read_nbt(OUT / "room.nbt")
    assert "palette" in tag.value and "palettes" not in tag.value
    report = static_review(loaded)
    digest = hashlib.sha256((OUT / "room.nbt").read_bytes()).hexdigest()
    metadata = {
        "id": "M07", "name": "枯井水牢", "size": list(SIZE),
        "entrance": list(ENTRANCE), "interior": list(INTERIOR), "facing": "north", "sha256": digest,
        "reference": str(REFERENCE), "reference_sha256": REFERENCE_SHA,
        "minecraft": "1.20.1", "data_version": 3465, "tier": "medium",
        "safe_points": [list(position) for position in TARGETS], "deployed": False,
        "id_note": "M07 retains its leading zero and is not the existing third-batch M7 room",
        "adaptations": [
            "概念图约13×15×13，按可建造的3格北接口、牢龛、环路和暗渠扩为15×13×17（XYZ）。",
            "完整地基厚4格，入口脚位Y=4，接入方需提供相应阶梯；不允许将入口误作Y=1。",
            "中央方井为5×5石砌井圈、3×3有盖井腔，中央铁支柱与四侧井圈承托闭合铁活板门。",
            "33格真实水源全部位于封闭井腔或东侧有盖暗渠，主环路无水；地表湿痕以石材表达。",
            "梁、锁链、灯和墙钩均有明确支撑；石顶与木衬层完整，剖切只影响预览。",
            "绞盘/拉杆/锁链/牢龛为建筑装饰，不新增脚本机关、怪物、波次、奖励或实体。"
        ],
        "integration": {
            "copy_only_after_coordinator_review": True,
            "requires_whole_volume_and_margin_occupancy_refusal": True,
            "requires_hall_connection_and_independent_generation_marker": True,
            "north_interface_width": 3, "north_interface_height": 3, "entrance_feet_y": FEET
        }
    }
    dump(OUT / "room.json", metadata)
    report["sha256"] = digest
    dump(ROOT / "qa/static.json", report)
    empty = Structure((56, 22, 56))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / "forge/src/main/resources/data/ftm07qa/structures/empty.nbt")
    print(json.dumps({"status": "PASS", "sha256": digest, "records": report["records"], "dry_reachable_positions": report["dry_reachable_positions"]}))


if __name__ == "__main__":
    main()
