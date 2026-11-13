import hashlib
import json
from collections import Counter
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
SIZE = (29, 23, 33)
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-stair-rooms-20261102/out/stair-nether-hall.png')
REFERENCE_HASH = '9d5461ff3fa0058ba614ab44a1d24f55c373f0b321fa35cab6eafc2e8a7a0736'
BRICK = state('polished_blackstone_bricks')
BLACK = state('polished_blackstone')
GOLD = state('gold_block')
NETHER = state('red_nether_bricks')
GILDED = state('gilded_blackstone')
GLASS = state('orange_stained_glass')


def save_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def floor(position):
    pos_x, pos_y, pos_z = position
    if pos_x in (4, 24) or pos_z in (3, 29):
        return GILDED if (pos_x + pos_z) % 3 else GOLD
    return NETHER if (pos_x * 7 + pos_z * 13 + pos_y) % 7 < 3 else BRICK


def main():
    assert hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_HASH
    room = Structure(SIZE)
    room.box((0, 0, 0), (28, 22, 32), AIR)
    room.box((0, 0, 0), (28, 6, 32), BRICK)
    for pos_z in range(33):
        for pos_x in range(29):
            room.set((pos_x, 6, pos_z), floor((pos_x, 6, pos_z)))
    room.box((0, 7, 0), (0, 21, 32), BRICK)
    room.box((28, 7, 0), (28, 21, 32), BRICK)
    room.box((0, 7, 0), (28, 21, 0), BRICK)
    room.box((0, 7, 32), (28, 21, 32), BRICK)
    room.box((0, 22, 0), (28, 22, 32), BRICK)
    for pos_y in range(7, 21):
        for pos_z in range(1, 32):
            for pos_x in (0, 28):
                room.set((pos_x, pos_y, pos_z), NETHER if (pos_y + pos_z) % 6 else BRICK)
        for pos_x in range(1, 28):
            room.set((pos_x, pos_y, 32), NETHER if (pos_x + pos_y) % 6 else BRICK)
    for pos_z in (2, 15, 30):
        room.box((1, 21, pos_z), (27, 21, pos_z), BLACK)
    features = {'grand_stairs': [], 'descent_stairs': [], 'upper_stairs': [], 'landing': [],
                'railings': [], 'pillars': [], 'lanterns': [], 'chains': [], 'braziers': []}
    supports, stair_supports, lava, seals = [], [], [], []

    def railing(position, gold=False):
        pos_x, pos_y, pos_z = position
        room.set(position, GOLD if gold else BLACK)
        features['railings'].append(list(position))
        supports.append({'position': list(position), 'support': [pos_x, pos_y - 1, pos_z]})

    for channel_x in (2, 26):
        for pos_z in range(5, 13):
            room.set((channel_x, 4, pos_z), BLACK)
            room.set((channel_x, 5, pos_z), state('lava', level=0))
            room.set((channel_x, 6, pos_z), GLASS)
            lava.append([channel_x, 5, pos_z])
            seals.append([channel_x, 6, pos_z])
        for edge_x in (channel_x - 1, channel_x + 1):
            room.box((edge_x, 4, 4), (edge_x, 7, 13), BRICK)
            for pos_z in (4, 8, 13):
                room.set((edge_x, 7, pos_z), GILDED)
        room.box((channel_x, 4, 4), (channel_x, 6, 4), BLACK)
        room.box((channel_x, 4, 13), (channel_x, 6, 13), BLACK)

    room.box((20, 1, 8), (24, 5, 32), AIR)
    for pos_z in range(8, 33):
        room.box((19, 1, pos_z), (19, 6, pos_z), NETHER)
        room.box((25, 1, pos_z), (25, 6, pos_z), NETHER)
        for pos_x in range(20, 25):
            room.set((pos_x, 0, pos_z), GILDED if pos_x in (20, 24) else BRICK)
    for index, pos_z in enumerate(range(8, 14)):
        stair_y = 6 - index
        room.box((20, stair_y + 1, pos_z), (24, 11, pos_z), AIR)
        for pos_x in range(20, 25):
            if stair_y > 1:
                room.box((pos_x, 1, pos_z), (pos_x, stair_y - 1, pos_z), BRICK)
            position = (pos_x, stair_y, pos_z)
            room.set(position, state('polished_blackstone_brick_stairs', facing='north', half='bottom', shape='straight', waterlogged=False))
            features['descent_stairs'].append(list(position))
            stair_supports.append({'position': list(position), 'support': [pos_x, stair_y - 1, pos_z]})
        for side_x in (19, 25):
            room.box((side_x, 1, pos_z), (side_x, max(6, stair_y + 1), pos_z), BRICK)
            railing((side_x, max(6, stair_y + 1) + 1, pos_z), gold=index % 2 == 0)

    room.box((5, 7, 14), (25, 12, 22), BRICK)
    for pos_z in range(14, 23):
        for pos_x in range(5, 26):
            rim = pos_x in (5, 25) or pos_z in (14, 22)
            ring = (pos_x in (11, 17) and 16 <= pos_z <= 20) or (pos_z in (16, 20) and 11 <= pos_x <= 17)
            room.set((pos_x, 12, pos_z), GOLD if rim or ring else GILDED if (pos_x + pos_z) % 5 == 0 else BLACK)
            features['landing'].append([pos_x, 12, pos_z])
    for index, pos_z in enumerate(range(8, 14)):
        stair_y = 7 + index
        room.box((8, 7, pos_z), (16, max(7, stair_y - 1), pos_z), BRICK)
        for pos_x in range(8, 17):
            position = (pos_x, stair_y, pos_z)
            room.set(position, state('polished_blackstone_brick_stairs', facing='south', half='bottom', shape='straight', waterlogged=False))
            features['grand_stairs'].append(list(position))
            stair_supports.append({'position': list(position), 'support': [pos_x, stair_y - 1, pos_z]})
        for side_x in (7, 17):
            room.box((side_x, 7, pos_z), (side_x, stair_y + 1, pos_z), BRICK)
            railing((side_x, stair_y + 2, pos_z), gold=index % 2 == 0)
    for pos_x in list(range(5, 8)) + list(range(17, 26)):
        room.box((pos_x, 7, 13), (pos_x, 13, 13), BRICK)
        railing((pos_x, 14, 13), gold=pos_x in (5, 7, 17, 21, 25))
    for side_x in (4, 26):
        for pos_z in range(14, 23):
            room.box((side_x, 7, pos_z), (side_x, 13, pos_z), BRICK)
            railing((side_x, 14, pos_z), gold=pos_z in (14, 18, 22))
    for pos_x in list(range(5, 10)) + list(range(18, 26)):
        room.box((pos_x, 7, 23), (pos_x, 13, 23), BRICK)
        railing((pos_x, 14, 23), gold=pos_x % 4 == 1)

    for index, pos_z in enumerate(range(23, 27)):
        stair_y = 13 + index
        room.box((10, 7, pos_z), (16, stair_y - 1, pos_z), BRICK)
        for pos_x in range(10, 17):
            position = (pos_x, stair_y, pos_z)
            room.set(position, state('polished_blackstone_brick_stairs', facing='south', half='bottom', shape='straight', waterlogged=False))
            features['upper_stairs'].append(list(position))
            stair_supports.append({'position': list(position), 'support': [pos_x, stair_y - 1, pos_z]})
        for side_x in (9, 17):
            room.box((side_x, 7, pos_z), (side_x, stair_y + 1, pos_z), BRICK)
            railing((side_x, stair_y + 2, pos_z), gold=index % 2 == 0)
    room.box((10, 7, 27), (18, 16, 32), BRICK)
    for pos_z in range(27, 33):
        for pos_x in range(10, 19):
            room.set((pos_x, 16, pos_z), GILDED if pos_x in (10, 18) else BLACK)
        for side_x in (9, 19):
            room.box((side_x, 7, pos_z), (side_x, 17, pos_z), BRICK)
            railing((side_x, 18, pos_z), gold=pos_z in (27, 31))

    for pos_x, pos_z in ((1, 1), (27, 1), (1, 16), (27, 16), (1, 31), (27, 31)):
        for pos_y in range(7, 22):
            material = GOLD if pos_y in (8, 20) else state('polished_basalt', axis='y') if pos_y > 8 else BLACK
            room.set((pos_x, pos_y, pos_z), material)
            features['pillars'].append([pos_x, pos_y, pos_z])
    for pos_x in (9, 19):
        room.box((pos_x, 17, 27), (pos_x, 20, 27), BLACK)
        room.set((pos_x, 21, 27), GOLD)
    room.box((9, 21, 27), (19, 21, 27), BLACK)
    for pos_x in (10, 18):
        room.set((pos_x, 20, 27), GILDED)
    room.set((14, 20, 31), state('red_wall_banner', facing='north'),
             compound(id=string('minecraft:banner'), Patterns=Tag(9, [], 10)))

    for pos_x in (4, 24):
        for pos_z in (6, 18, 26):
            for pos_y in range(18, 22):
                position = (pos_x, pos_y, pos_z)
                room.set(position, state('chain', axis='y', waterlogged=False))
                features['chains'].append(list(position))
                supports.append({'position': list(position), 'support': [pos_x, pos_y + 1, pos_z]})
            position = (pos_x, 17, pos_z)
            room.set(position, state('soul_lantern', hanging=True, waterlogged=False))
            features['lanterns'].append(list(position))
            supports.append({'position': list(position), 'support': [pos_x, 18, pos_z]})
    for pos_z in (18, 26):
        room.set((22, 4, pos_z), state('lantern', hanging=True, waterlogged=False))
        room.set((22, 5, pos_z), state('chain', axis='y', waterlogged=False))
        features['lanterns'].append([22, 4, pos_z])
        supports.extend([{'position': [22, 4, pos_z], 'support': [22, 5, pos_z]}, {'position': [22, 5, pos_z], 'support': [22, 6, pos_z]}])
    for pos_x in (7, 23):
        room.set((pos_x, 13, 20), state('chiseled_polished_blackstone'))
        room.set((pos_x, 14, 20), state('lantern', hanging=False, waterlogged=False))
        features['braziers'].append([pos_x, 14, 20])
        supports.append({'position': [pos_x, 14, 20], 'support': [pos_x, 13, 20]})

    room.box((20, 1, 32), (24, 5, 32), BRICK)
    room.box((13, 7, 0), (15, 9, 0), AIR)
    room.box((13, 17, 32), (15, 19, 32), AIR)
    room.box((21, 1, 32), (23, 3, 32), AIR)
    room.write(ROOT / 'out/room.nbt')
    fingerprint = hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest()
    metadata = {'id': 'STAIR_D_NETHER_HALL', 'name': '下界黑石楼梯厅 · 楼梯 D', 'size': list(SIZE),
                'entrance': [14, 7, 0], 'interior': [14, 7, 4], 'facing': 'north', 'sha256': fingerprint,
                'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'minecraft_version': '1.20.1', 'data_version': 3465,
                'interfaces': [{'id': 'north_main', 'feet': [14, 7, 0], 'facing': 'north', 'clear_width': 3, 'clear_height': 3},
                               {'id': 'south_upper', 'feet': [14, 17, 32], 'facing': 'south', 'clear_width': 3, 'clear_height': 3},
                               {'id': 'south_lower', 'feet': [22, 1, 32], 'facing': 'south', 'clear_width': 3, 'clear_height': 3}],
                'integration': {'status': 'STANDALONE_CANDIDATE_NOT_REGISTERED', 'id_is_provisional': True, 'deployed': False,
                                'coordinator_thread': '01a157ef-83ef-7e51-8671-2d7f9db88086', 'requires_coordinator_acceptance': True},
                'layout': {'main_floor_feet_y': 7, 'landing_feet_y': 13, 'upper_interface_feet_y': 17, 'lower_interface_feet_y': 1,
                           'grand_stair_width': 9, 'upper_stair_width': 7, 'descent_stair_width': 5, 'full_roof': True,
                           'real_lava_blocks': len(lava), 'lava_enclosure': 'six-face full-collision blackstone/glass envelope'},
                'substitutions': ['概念图落为29×23×33网格，3×3北入口保持中置；南侧分别设+10/-6高度接口。',
                                  '熔岩保留真实源方块，但以黑石底侧和橙色玻璃顶完全密封，矮墙隔离步道。',
                                  '平台火盆采用受支撑的暖色灯笼底座，消除裸火伤害和燃烧风险。',
                                  '屋顶完整封闭；预览仅移除显示用近侧墙和屋顶，不改交付模板。'],
                'gameplay': {'entities': 0, 'loot_tables': 0, 'inventory_items': 0, 'combat_or_rewards': False}}
    routes = [{'id': 'entrance_to_landing', 'start_y': 7, 'waypoints': [[14.5, 0.5], [14.5, 4.5], [12.5, 4.5], [12.5, 18.5]], 'end_y': 13},
              {'id': 'landing_to_upper', 'start_y': 13, 'waypoints': [[12.5, 18.5], [14.5, 18.5], [14.5, 32.5]], 'end_y': 17},
              {'id': 'entrance_to_lower', 'start_y': 7, 'waypoints': [[14.5, 0.5], [14.5, 4.5], [22.5, 4.5], [22.5, 32.5]], 'end_y': 1}]
    blueprint = {'size': list(SIZE), 'features': features, 'supports': supports, 'stair_supports': stair_supports,
                 'sealed_lava': lava, 'glass_seals': seals, 'routes': routes,
                 'safe_points': [[14, 7, 4], [12, 13, 18], [14, 17, 30], [22, 1, 29]],
                 'negative_probes': {'lava_cover': [2, 6, 8], 'lava': [2, 5, 8], 'chain': [4, 18, 6], 'lamp': [4, 17, 6],
                                     'blocked_entry': [14, 7, 0], 'missing_stair': [12, 9, 10], 'roof': [14, 22, 18]},
                 'scope': 'Physical three-level transit room, not a registered FT route or deployment.'}
    save_json(ROOT / 'out/room.json', metadata)
    save_json(ROOT / 'out/blueprint.json', blueprint)
    save_json(ROOT / 'out/materials.json', dict(sorted(Counter(block.name for block in room.blocks.values()).items())))
    empty = Structure((1, 1, 1))
    empty.set((0, 0, 0), AIR)
    empty.write(ROOT / 'src/main/resources/data/ftstairdqa/structures/empty.nbt')
    print(json.dumps({'size': SIZE, 'records': len(room.blocks), 'lava': len(lava), 'stairs': len(stair_supports), 'sha256': fingerprint}))


if __name__ == '__main__':
    main()
