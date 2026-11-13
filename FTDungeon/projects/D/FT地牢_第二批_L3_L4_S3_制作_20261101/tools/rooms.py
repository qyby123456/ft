from __future__ import annotations

import json

from common import AIR, Room, Tag, compound, integer, state, string


def lava_fall(room, side, center_z):
    center_x = 1 if side == "west" else 21
    pool_start, pool_end = (1, 3) if side == "west" else (19, 21)
    for pos_x in range(max(0, pool_start - 1), min(22, pool_end + 1) + 1):
        for pos_z in range(center_z - 2, center_z + 3):
            if pool_start <= pos_x <= pool_end and center_z - 1 <= pos_z <= center_z + 1:
                room.set((pos_x, 1, pos_z), "lava", level=0)
            else:
                room.set((pos_x, 1, pos_z), "polished_blackstone_bricks")
                if pos_x not in (0, 22):
                    room.set((pos_x, 2, pos_z), "tinted_glass")
    for pos_y in range(2, 11):
        room.set((center_x, pos_y, center_z), "lava", level=0 if pos_y == 10 else 8)
        room.set((center_x + (1 if side == "west" else -1), pos_y, center_z), "tinted_glass")
        for offset in (-1, 1):
            room.set((center_x, pos_y, center_z + offset), "tinted_glass")
    room.box((max(0, center_x - 1), 11, center_z - 1), (min(22, center_x + 1), 11, center_z + 1), "polished_blackstone_bricks")
    room.set((center_x, 12, center_z), "gilded_blackstone")
    room.mark(f"lava_fall_{side}_{center_z}", [(center_x, 10, center_z), (center_x, 1, center_z)])


def forge(room, center_x, center_z, face):
    room.box((center_x - 1, 1, center_z), (center_x + 1, 4, center_z + 2), "polished_blackstone_bricks")
    room.campfire((center_x, 1, center_z + 1))
    for pos_y in (1, 2):
        room.set((center_x, pos_y, center_z + (0 if face == "north" else 2)), "iron_bars", north=False, south=False, east=True, west=True, waterlogged=False)
    room.box((center_x, 5, center_z + 1), (center_x, 12, center_z + 1), "polished_basalt", axis="y")
    room.set((center_x, 4, center_z), "gilded_blackstone")
    room.set((center_x + (2 if center_x < 11 else -2), 1, center_z + 1), "blast_furnace", compound(id=string("minecraft:blast_furnace"), Items=Tag(9, [], 10), BurnTime=Tag(2, 0), CookTime=Tag(2, 0), CookTimeTotal=Tag(2, 200)), facing=face, lit=False)
    room.mark(f"forge_{center_x}_{center_z}", [(center_x, 1, center_z + 1), (center_x, 12, center_z + 1)])


def molten_forge():
    room = Room("L3")
    for pos_z in (5, 16):
        for side in ("west", "east"):
            lava_fall(room, side, pos_z)
    for pos_x in (4, 18):
        for pos_z in (9, 13):
            room.box((pos_x, 1, pos_z), (pos_x, 11, pos_z), "polished_basalt", axis="y")
            room.set((pos_x, 6, pos_z), "gilded_blackstone")
            room.set((pos_x + (1 if pos_x == 4 else -1), 6, pos_z), "polished_blackstone_bricks")
            room.lantern((pos_x + (1 if pos_x == 4 else -1), 5, pos_z), hanging=True)
    for pos_z in (9, 13):
        room.box((1, 12, pos_z), (21, 12, pos_z), "polished_basalt", axis="x")
    room.box((9, 1, 9), (13, 1, 13), "polished_blackstone_bricks")
    for pos_x in (10, 11, 12):
        room.set((pos_x, 1, 8), "polished_blackstone_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
        room.set((pos_x, 1, 14), "polished_blackstone_brick_stairs", facing="north", half="bottom", shape="straight", waterlogged=False)
    room.box((10, 2, 10), (12, 2, 12), "polished_blackstone_bricks")
    room.box((11, 3, 10), (11, 3, 12), "polished_blackstone")
    room.box((9, 4, 10), (13, 4, 12), "deepslate_tiles")
    room.set((8, 4, 11), "polished_blackstone_brick_slab", type="top", waterlogged=False)
    room.set((14, 4, 11), "polished_blackstone_bricks")
    room.set((11, 4, 11), "shroomlight")
    for pos_x, pos_z in ((9, 9), (13, 9), (9, 13), (13, 13)):
        room.lantern((pos_x, 2, pos_z))
    room.mark("central_giant_anvil", [(8, 4, 11), (11, 3, 11), (14, 4, 11)])
    for pos_x in (6, 16):
        for pos_z, facing in ((2, "south"), (17, "north")):
            forge(room, pos_x, pos_z, facing)
    for pos_x, pos_z in ((6, 9), (16, 9), (6, 13), (16, 13)):
        room.anvil((pos_x, 1, pos_z), facing="east")
        room.set((pos_x, 1, pos_z + 1), "smooth_stone")
        room.set((pos_x, 2, pos_z + 1), "grindstone", face="floor", facing="north")
    for pos_x in (7, 15):
        for offset_x in (-1, 1):
            for offset_z in (-1, 1):
                room.box((pos_x + offset_x, 7, 7 + offset_z), (pos_x + offset_x, 12, 7 + offset_z), "chain", axis="y", waterlogged=False)
        room.box((pos_x - 1, 6, 6), (pos_x + 1, 6, 8), "polished_blackstone_brick_slab", type="top", waterlogged=False)
        room.campfire((pos_x, 7, 7))
        for offset in (-1, 1):
            room.set((pos_x + offset, 7, 7), "iron_bars", east=True, west=True, north=False, south=False, waterlogged=False)
    for pos_z in (4, 7, 15, 18):
        room.set((10, 0, pos_z), "shroomlight")
        room.set((12, 0, pos_z), "orange_glazed_terracotta", facing="north")
    for pos_x in range(5, 18):
        room.set((pos_x, 1, 20), "rail", shape="east_west", waterlogged=False)
    room.box((14, 1, 20), (16, 1, 20), "iron_block")
    for pos_x in (14, 16):
        room.set((pos_x, 2, 20), "iron_trapdoor", facing="east" if pos_x == 14 else "west", half="bottom", open=True, powered=False, waterlogged=False)
    room.set((15, 2, 20), "raw_iron_block")
    for pos_x in (6, 8):
        room.box((pos_x, 1, 15), (pos_x + 1, 1, 16), "smooth_stone")
        room.set((pos_x, 2, 15), "raw_copper_block")
    for pos_x in (3, 19):
        room.box((pos_x, 1, 11), (pos_x, 2, 11), "polished_blackstone_bricks")
        room.box((pos_x, 3, 11), (pos_x, 5, 11), "iron_bars", east=False, west=False, north=True, south=True, waterlogged=False)
        room.box((pos_x, 6, 10), (pos_x, 6, 12), "polished_blackstone_bricks")
        for anchor_z in (10, 12):
            room.set((4 if pos_x == 3 else 18, 6, anchor_z), "polished_blackstone_bricks")
    for name, position in (("入口与祭坛前庭", (11, 1, 5)), ("西侧锻造带", (6, 1, 11)), ("东侧锻造带", (16, 1, 11)), ("后方装卸带", (11, 1, 18)), ("祭坛北侧踏步", (11, 2, 9))):
        room.station(name, position)
    return room.finish()


def connect_stone(room, start, end):
    distance = max(abs(last - first) for first, last in zip(start, end))
    previous = start
    for step in range(distance + 1):
        position = tuple(round(first + (last - first) * step / max(1, distance)) for first, last in zip(start, end))
        current = list(previous)
        for axis in range(3):
            while current[axis] != position[axis]:
                current[axis] += 1 if position[axis] > current[axis] else -1
                room.set(tuple(current), "mossy_cobblestone" if (sum(current) + axis) % 4 == 0 else "stone_bricks")
        room.set(position, "stone_bricks")
        previous = position


def spider_shrine():
    room = Room("L4")
    for pos_z in (0, 20):
        room.box((0, 8, pos_z), (20, 8, pos_z), "dark_oak_log", axis="x")
    for pos_x in (0, 20):
        room.box((pos_x, 8, 1), (pos_x, 8, 19), "dark_oak_log", axis="z")
    for pos_z in (6, 14):
        for pos_x in (0, 20):
            room.box((pos_x, 1, pos_z), (pos_x, 8, pos_z), "dark_oak_log", axis="y")
    for pos_x in (6, 14):
        room.box((pos_x, 1, 20), (pos_x, 8, 20), "dark_oak_log", axis="y")
    for pos_x, pos_z, height in ((3, 5, 5), (17, 5, 6), (3, 17, 6), (17, 17, 5)):
        room.box((pos_x, 1, pos_z), (pos_x, height, pos_z), "mossy_stone_bricks")
        room.box((pos_x - 1, height, pos_z), (pos_x + 1, height, pos_z), "mossy_stone_brick_slab", type="bottom", waterlogged=False)
        room.set((pos_x, height + 1, pos_z), "cobweb")
    room.box((7, 1, 8), (13, 1, 16), "mossy_stone_bricks")
    for pos_x in (9, 10, 11):
        room.set((pos_x, 1, 7), "stone_brick_stairs", facing="south", half="bottom", shape="straight", waterlogged=False)
    room.box((9, 2, 10), (11, 4, 14), "mossy_cobblestone")
    room.box((9, 3, 9), (11, 4, 10), "chiseled_stone_bricks")
    room.box((9, 4, 13), (11, 5, 15), "mossy_stone_bricks")
    for pos_x in (9, 11):
        room.set((pos_x, 4, 8), "polished_blackstone_button", face="wall", facing="north", powered=False)
        room.set((pos_x, 2, 8), "iron_bars", north=True, south=True, east=False, west=False, waterlogged=False)
    routes = [
        [(8, 4, 10), (7, 5, 9), (6, 5, 8), (5, 4, 7), (5, 1, 7)],
        [(8, 4, 11), (7, 5, 11), (6, 5, 11), (4, 4, 10), (4, 1, 10)],
        [(8, 4, 13), (7, 5, 14), (6, 5, 15), (4, 4, 15), (4, 1, 15)],
        [(8, 4, 14), (7, 5, 16), (6, 5, 17), (5, 4, 18), (5, 1, 18)],
    ]
    for side in ("left", "right"):
        for index, points in enumerate(routes, 1):
            points = [(20 - pos_x if side == "right" else pos_x, pos_y, pos_z) for pos_x, pos_y, pos_z in points]
            connect_stone(room, (9 if side == "left" else 11, 4, points[0][2]), points[0])
            for first, second in zip(points, points[1:]):
                connect_stone(room, first, second)
            room.mark(f"spider_leg_{side}_{index}", points)
    room.mark("spider_head_body_abdomen", [(10, 3, 9), (10, 3, 12), (10, 5, 14)])
    for pos_x, pos_z in ((3, 11), (17, 11), (6, 18), (14, 18)):
        room.set((pos_x, 8, pos_z), "cobweb")
        room.set((pos_x, 7, pos_z), "bone_block", axis="y")
        room.box((pos_x, 5, pos_z), (pos_x, 6, pos_z), "calcite")
        room.set((pos_x, 4, pos_z), "pointed_dripstone", vertical_direction="down", thickness="tip", waterlogged=False)
        room.mark(f"hanging_cocoon_{pos_x}_{pos_z}", [(pos_x, 8, pos_z), (pos_x, 5, pos_z)])
    for pos_x in (1, 2, 18, 19):
        for pos_z in (9, 10, 14, 15, 18):
            room.set((pos_x, 1, pos_z), "cobweb")
    for pos_x in range(1, 20):
        if pos_x % 3 != 0:
            room.set((pos_x, 8, 19), "cobweb")
            if pos_x not in (9, 10, 11):
                room.set((pos_x, 8, 1), "cobweb")
    for pos_z in range(3, 19):
        if pos_z % 3:
            room.set((1, 7, pos_z), "cobweb")
            room.set((19, 7, pos_z), "cobweb")
    for pos_z in (9, 13, 17):
        for pos_x in (6, 8, 12, 14):
            room.set((pos_x, 7, pos_z), "cobweb")
            if (pos_x + pos_z) % 3:
                room.set((pos_x + 1, 8, pos_z), "cobweb")
    for pos_x, pos_z in ((6, 5), (14, 5)):
        room.set((pos_x, 1, pos_z), "green_glazed_terracotta", facing="north")
        room.campfire((pos_x, 2, pos_z), soul=True)
        for offset in (-1, 1):
            room.set((pos_x + offset, 2, pos_z), "iron_bars", east=True, west=True, north=False, south=False, waterlogged=False)
    for pos_x, pos_z in ((7, 8), (13, 8), (7, 16), (13, 16)):
        room.candle((pos_x, 2, pos_z), count=2, lit=True)
    for pos_x, pos_z in ((6, 8), (14, 8), (6, 16), (14, 16)):
        room.set((pos_x, 1, pos_z), "calcite")
        room.set((pos_x, 2, pos_z), "turtle_egg", eggs=3, hatch=0)
    for pos_x, pos_z in ((6, 11), (14, 11), (8, 18)):
        room.set((pos_x, 1, pos_z), "skeleton_skull", compound(id=string("minecraft:skull")), rotation=0)
    for pos_z in (8, 13):
        for pos_x in (1, 19):
            room.box((pos_x, 1, pos_z), (pos_x, 1, pos_z + 1), "mossy_stone_bricks")
            room.box((pos_x, 2, pos_z), (pos_x, 4, pos_z + 1), "iron_bars", east=False, west=False, north=True, south=True, waterlogged=False)
            room.box((pos_x, 5, pos_z), (pos_x, 5, pos_z + 1), "stone_bricks")
    for name, position in (("北侧三格宽前庭", (10, 1, 4)), ("西侧绕行道", (3, 1, 12)), ("东侧绕行道", (17, 1, 12)), ("后侧祭坛绕行道", (10, 1, 18))):
        room.station(name, position)
    return room.finish()


def alchemist_lab():
    room = Room("S3")
    for pos_z in range(1, 8):
        for pos_x in range(1, 8):
            if (pos_x - 4) ** 2 + (pos_z - 4) ** 2 in (4, 5, 8):
                room.set((pos_x, 0, pos_z), "purple_glazed_terracotta", facing="north" if pos_x <= 4 else "south")
    room.set((4, 0, 4), "amethyst_block")
    room.box((2, 1, 6), (6, 1, 6), "polished_andesite")
    room.brew((3, 2, 6))
    room.set((5, 1, 5), "water_cauldron", level=2)
    room.set((5, 2, 6), "potted_fern")
    room.set((6, 2, 6), "amethyst_cluster", facing="up", waterlogged=False)
    for pos_z in (4, 6):
        room.set((1, 1, pos_z), "spruce_planks")
        room.brew((1, 2, pos_z))
    room.box((1, 1, 5), (1, 2, 5), "bookshelf")
    room.box((1, 3, 4), (1, 3, 6), "spruce_slab", type="top", waterlogged=False)
    room.set((2, 1, 7), "bookshelf")
    room.set((3, 1, 7), "bookshelf")
    room.set((1, 1, 7), "bookshelf")
    room.box((7, 1, 4), (7, 1, 6), "spruce_planks")
    page = json.dumps({"text": "炼金工作记录\n水瓶 · 药草 · 紫晶\n仅供室内布置，无奖励箱。", "color": "dark_green"}, ensure_ascii=False)
    book = compound(id=string("minecraft:written_book"), Count=Tag(1, 1), tag=compound(title=string("炼金记录"), author=string("FT"), pages=Tag(9, [string(page)], 8), resolved=Tag(1, 1)))
    room.set((7, 1, 4), "lectern", compound(id=string("minecraft:lectern"), Book=book, Page=integer(0)), facing="west", has_book=True, powered=False)
    room.set((7, 2, 4), AIR)
    room.candle((7, 2, 5), color="green", count=2)
    room.lantern((7, 2, 6))
    room.set((1, 1, 2), "potted_azalea_bush")
    room.set((7, 1, 2), "moss_block")
    room.set((7, 2, 2), "potted_fern")
    room.lantern((2, 3, 2), hanging=True)
    room.lantern((6, 3, 2), soul=True, hanging=True)
    room.set((1, 3, 7), "cobweb")
    room.set((7, 3, 7), "cobweb")
    room.mark("alchemy_bench", [(3, 2, 6), (5, 1, 5)])
    room.mark("potion_shelves", [(1, 2, 4), (1, 2, 6)])
    room.mark("writing_desk", [(7, 1, 4)])
    room.mark("purple_rune_floor", [(4, 0, 4), (2, 0, 4), (6, 0, 4)])
    for name, position in (("入口与符纹中央", (4, 1, 3)), ("炼药操作位", (3, 1, 5)), ("药锅操作位", (4, 1, 5)), ("抄写桌前", (6, 1, 4))):
        room.station(name, position)
    return room.finish()


BUILDERS = {"L3": molten_forge, "L4": spider_shrine, "S3": alchemist_lab}
