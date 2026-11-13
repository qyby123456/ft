import hashlib
import json
from collections import Counter, defaultdict
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-C-end-void.png')
REFERENCE_SHA256 = 'e43021de1da6f121f343912490520ac611b8a46d6d586f9bc9404f4832111a3b'
SIZE = (13, 9, 13)


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    if hashlib.sha256(REFERENCE.read_bytes()).hexdigest() != REFERENCE_SHA256:
        raise ValueError('Source concept fingerprint mismatch')
    if hashlib.sha256((ROOT / 'reference/shop-variant-C-end-void.png').read_bytes()).hexdigest() != REFERENCE_SHA256:
        raise ValueError('Archived concept fingerprint mismatch')
    room = Structure(SIZE)
    for pos_y in range(SIZE[1]):
        for pos_z in range(SIZE[2]):
            for pos_x in range(SIZE[0]):
                room.set((pos_x, pos_y, pos_z), AIR)
    features = defaultdict(set)
    owners = {}
    supports = []

    def put(position, name, group=None, entity=None, **properties):
        if group:
            if position in owners and owners[position] != group:
                raise ValueError(f'Furniture overlap at {position}: {owners[position]} / {group}')
            owners[position] = group
            features[group].add(position)
        room.set(position, state(name, **properties), entity)

    def supported(position, name, anchor, face='up', group='decor', entity=None, **properties):
        put(position, name, group, entity, **properties)
        supports.append({'position': list(position), 'anchor': list(anchor), 'support_face': face, 'block': name})

    def block_entity(identity, inventory=False):
        values = {'id': string('minecraft:' + identity)}
        if inventory:
            values['Items'] = Tag(9, [], 10)
        return Tag(10, values)

    for pos_z in range(13):
        for pos_x in range(13):
            floor = 'end_stone_bricks'
            if pos_x in (0, 12) or pos_z in (0, 12):
                floor = 'purpur_block'
            if pos_x in (0, 12) and pos_z in (0, 12):
                floor = 'obsidian'
            if 2 <= pos_x <= 10 and 5 <= pos_z <= 9 and (pos_x in (2, 10) or pos_z in (5, 9)):
                put((pos_x, 0, pos_z), 'purple_glazed_terracotta', facing='east' if pos_z in (5, 9) else 'north')
            else:
                put((pos_x, 0, pos_z), floor)
            ceiling = 'purpur_block' if (pos_x + pos_z) % 3 else 'purpur_pillar'
            put((pos_x, 8, pos_z), ceiling, **({'axis': 'z'} if ceiling == 'purpur_pillar' else {}))
            if pos_x in (0, 12) or pos_z in (0, 12):
                for pos_y in range(1, 8):
                    put((pos_x, pos_y, pos_z), 'purpur_block' if pos_y in (1, 7) else 'end_stone_bricks')

    for pos_x in (0, 12):
        for pos_z in (0, 6, 12):
            for pos_y in range(1, 8):
                put((pos_x, pos_y, pos_z), 'obsidian' if pos_y in (1, 7) else 'purpur_pillar', 'pillars', **({} if pos_y in (1, 7) else {'axis': 'y'}))
        for pos_z in (3, 4, 8, 9):
            for pos_y in range(3, 6):
                put((pos_x, pos_y, pos_z), 'tinted_glass', 'star_windows')

    for pos_x in range(5, 8):
        for pos_y in range(1, 4):
            room.set((pos_x, pos_y, 0), AIR)
        put((pos_x, 4, 0), 'purpur_pillar', axis='x')
    for pos_x in (4, 8):
        for pos_y in range(1, 5):
            put((pos_x, pos_y, 0), 'purpur_pillar', 'pillars', axis='y')

    for pos_z in (4, 10):
        for pos_x in range(13):
            put((pos_x, 7, pos_z), 'purpur_pillar', 'beams', axis='x')
    for pos_x in (4, 8):
        supported((pos_x, 6, 4), 'end_rod', (pos_x, 7, 4), 'down', 'end_rods', facing='down')
    for pos_x, face, anchor_x in ((1, 'east', 0), (11, 'west', 12)):
        supported((pos_x, 4, 2), 'end_rod', (anchor_x, 4, 2), face, 'end_rods', facing=face)

    for pos_x in range(3, 10):
        put((pos_x, 1, 7), 'purpur_block', 'counter')
    for pos_x in (3, 9):
        supported((pos_x, 2, 7), 'end_rod', (pos_x, 1, 7), group='end_rods', facing='up')
    supported((4, 2, 7), 'amethyst_cluster', (4, 1, 7), facing='up', waterlogged=False)
    supported((5, 2, 7), 'potted_azalea_bush', (5, 1, 7))
    supported((6, 2, 7), 'enchanting_table', (6, 1, 7), entity=block_entity('enchanting_table'))
    supported((7, 2, 7), 'brewing_stand', (7, 1, 7), entity=block_entity('brewing_stand', True), has_bottle_0=False, has_bottle_1=False, has_bottle_2=False)
    supported((8, 2, 7), 'flower_pot', (8, 1, 7))

    for pos_x in (3, 9):
        for pos_y in range(1, 6):
            put((pos_x, pos_y, 11), 'purpur_pillar', 'shelves', axis='y')
    for pos_x in range(4, 9):
        if pos_x == 6:
            put((pos_x, 1, 11), 'ender_chest', 'shelves', entity=block_entity('ender_chest'), facing='north', waterlogged=False)
        else:
            put((pos_x, 1, 11), 'bookshelf' if pos_x in (5, 7) else 'purpur_block', 'shelves')
        for pos_y in (3, 5):
            put((pos_x, pos_y, 11), 'purpur_slab', 'shelves', type='top', waterlogged=False)
    for pos_x in (4, 6, 8):
        put((pos_x, 4, 12), 'crying_obsidian')
    for pos_x in (4, 8):
        supported((pos_x, 4, 11), 'amethyst_cluster', (pos_x, 3, 11), facing='up', waterlogged=False)
    for pos_x in (5, 7):
        supported((pos_x, 4, 11), 'end_rod', (pos_x, 3, 11), group='end_rods', facing='up')
    supported((6, 4, 11), 'potted_warped_fungus', (6, 3, 11))
    supported((4, 6, 11), 'amethyst_cluster', (4, 5, 11), facing='up', waterlogged=False)
    put((6, 6, 11), 'purpur_block', 'shelf_sign_anchor')
    supported((8, 6, 11), 'potted_azalea_bush', (8, 5, 11))

    for pos_x in (1, 11):
        for pos_z in (4, 6):
            put((pos_x, 1, pos_z), 'purpur_block', 'side_displays')
        put((pos_x, 1, 5), 'barrel', 'barrels', entity=block_entity('barrel', True), facing='up', open=False)
        supported((pos_x, 2, 4), 'end_rod', (pos_x, 1, 4), group='end_rods', facing='up')
        supported((pos_x, 2, 6), 'potted_warped_fungus', (pos_x, 1, 6))
        put((pos_x, 1, 10), 'obsidian', 'crystal_plinths')
        put((pos_x, 2, 10), 'amethyst_block', 'crystal_plinths')
        supported((pos_x, 3, 10), 'amethyst_cluster', (pos_x, 2, 10), facing='up', waterlogged=False)
    for pos_x in (2, 10):
        put((pos_x, 1, 11), 'purpur_block', 'side_displays')

    messages = ['末地虚空商店', 'END VOID SHOP', '陈列 / 无预置货物', '北侧入口 / C']
    front = compound(messages=Tag(9, [string(json.dumps({'text': message}, ensure_ascii=False)) for message in messages], 8), color=string('yellow'), has_glowing_text=Tag(1, 1))
    back = compound(messages=Tag(9, [string('{"text":""}') for _ in range(4)], 8), color=string('black'), has_glowing_text=Tag(1, 0))
    supported((6, 6, 10), 'spruce_wall_sign', (6, 6, 11), 'north', 'sign', entity=compound(id=string('minecraft:sign'), front_text=front, back_text=back, is_waxed=Tag(1, 1)), facing='north', waterlogged=False)
    for pos_x in range(4, 9):
        put((pos_x, 7, 11), 'purpur_block', 'shelf_cornice')

    target = ROOT / 'out/room.nbt'
    room.write(target)
    digest = hashlib.sha256(target.read_bytes()).hexdigest()
    metadata = {
        'id': 'SHOP_C_END_VOID', 'name': '末地虚空商店', 'variant': 'C', 'size': list(SIZE),
        'entrance': [6, 1, 0], 'interior': [6, 1, 3], 'facing': 'north',
        'sha256': digest, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_SHA256,
        'minecraft_version': '1.20.1', 'data_version': 3465,
        'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'assigned_slot': None, 'deployed': False, 'requires_coordinator_acceptance': True},
        'layout': {'main_aisle_width_blocks': 3, 'main_aisle_height_blocks': 3, 'counter': {'from': [3, 1, 7], 'to': [9, 1, 7]}, 'counter_clearance_blocks': 1, 'roof': 'complete', 'floor': 'continuous_full_cube_structural_base', 'void_openings': 0, 'window_material': 'tinted_glass'},
        'substitutions': [
            '漂浮台变为整块承重底板与全封护外墙；虚空观感通过黑曜石边框、遮光玻璃星窗、紫珀/末地石与地面符纹表达，不挖洞或制造坠落边缘。',
            '悬空货架改为后墙连接的紫珀上半砖与落地立架；末地烛均贴实墙、实梁或台面，不使用自由漂浮端杆。',
            '货币、珍珠、卷轴与小瓶用紫水晶、空酿造台、书架与附魔台作块级转译，无物品实体、预装库存或奖励表。',
            '保留一只原版末影箱。模板不含物品或新奖励；玩家打开时连接已有个人末影箱库存，不保证玩家个人库存为空。',
            '屋顶完整，剖切只用于预览。没有店主实体、交易脚本、真实传送门、刷怪或 FT 核心路径改动。'
        ]
    }
    clearance = {(pos_x, 1, pos_z) for pos_x in range(3, 10) for pos_z in (6, 8)} | {(pos_x, 1, 7) for pos_x in (2, 10)}
    required = clearance | {(6, 1, 10), (2, 1, 5), (10, 1, 5)}
    aisle = [(pos_x, pos_y, pos_z) for pos_z in range(7) for pos_x in range(5, 8) for pos_y in range(1, 4)]
    blueprint = {
        'features': {name: [list(position) for position in sorted(positions)] for name, positions in sorted(features.items())},
        'entrance_to_counter_aisle': [list(position) for position in aisle],
        'counter_clearance': [list((pos_x, pos_y + offset, pos_z)) for pos_x, pos_y, pos_z in sorted(clearance) for offset in (0, 1)],
        'required_walkable': [list(position) for position in sorted(required)],
        'supported_decor': supports,
        'shelf_wall_connections': [{'position': [pos_x, pos_y, 11], 'anchor': [pos_x, pos_y, 12]} for pos_x in range(4, 9) for pos_y in (3, 5)],
        'counter_footprint': [[pos_x, 1, 7] for pos_x in range(3, 10)],
        'floor_y': 0, 'roof_y': 8,
        'furniture_overlap_check': 'Unique feature owner per occupied coordinate; shell attachment explicitly allowed.'
    }
    write_json(ROOT / 'out/room.json', metadata)
    write_json(ROOT / 'out/blueprint.json', blueprint)
    counts = Counter(block.name for block in room.blocks.values())
    write_json(ROOT / 'qa/materials.json', {'size': list(SIZE), 'records': len(room.blocks), 'non_air_blocks': sum(count for name, count in counts.items() if name != 'minecraft:air'), 'materials': dict(sorted(counts.items()))})
    print(json.dumps({'structure': digest, 'size': SIZE, 'records': len(room.blocks), 'end_rods': counts['minecraft:end_rod']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
