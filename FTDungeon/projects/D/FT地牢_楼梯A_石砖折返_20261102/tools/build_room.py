import hashlib
import json
from pathlib import Path

from nbtio import AIR, Structure, Tag, compound, state, string


ROOT = Path(__file__).resolve().parents[1]
REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-stair-rooms-20261102/out/stair-switchback.png')
REFERENCE_HASH = '5c5f70a5a6fc3ca28abdb037c32f3c8ba3d95d14f0d2a2b3bdc60fc5f1c75d1e'
SIZE = (23, 19, 25)
FULL_NAMES = {'stone_bricks', 'mossy_stone_bricks', 'cracked_stone_bricks', 'chiseled_stone_bricks', 'polished_andesite', 'andesite', 'stone', 'stripped_oak_wood', 'barrel'}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def masonry(position, aged=True):
    pos_x, pos_y, pos_z = position
    variant = (pos_x * 13 + pos_y * 19 + pos_z * 7) % 29
    if aged and variant in (0, 1, 2):
        return state('mossy_stone_bricks')
    if aged and variant in (3, 4):
        return state('cracked_stone_bricks')
    return state('stone_bricks')


def main():
    if digest(REFERENCE) != REFERENCE_HASH:
        raise ValueError('Design image fingerprint changed')
    room = Structure(SIZE)
    room.box((0, 0, 0), (22, 18, 24), AIR)
    for position in room.blocks:
        pos_x, pos_y, pos_z = position
        if pos_y in (0, 18) or pos_x in (0, 22) or pos_z in (0, 24):
            room.set(position, masonry(position))
    for pos_x in range(1, 22):
        for pos_z in range(1, 24):
            room.set((pos_x, 0, pos_z), state('polished_andesite') if (pos_x + pos_z) % 4 else state('stone_bricks'))
            if not (6 <= pos_x <= 8 and 6 <= pos_z <= 13):
                room.set((pos_x, 6, pos_z), state('polished_andesite') if (pos_x + pos_z) % 4 else masonry((pos_x, 6, pos_z)))

    for pos_x in (1, 21):
        for pos_z in (1, 8, 16, 23):
            for pos_y in range(1, 18):
                room.set((pos_x, pos_y, pos_z), state('chiseled_stone_bricks') if pos_y in (1, 6, 12, 17) else state('stone_bricks'))
    for pos_z in (1, 8, 16, 23):
        room.box((1, 17, pos_z), (21, 17, pos_z), state('stone_bricks'))

    steps = []
    guard_anchors = []
    for pos_z in range(8, 14):
        pos_y = pos_z - 7
        for pos_x in range(6, 9):
            if pos_y > 1:
                room.box((pos_x, 1, pos_z), (pos_x, pos_y - 1, pos_z), state('stone_bricks'))
            room.set((pos_x, pos_y, pos_z), state('stone_brick_stairs', facing='south', half='bottom', shape='straight', waterlogged=False))
            steps.append({'flight': 'lower', 'position': [pos_x, pos_y, pos_z], 'anchor': [pos_x, pos_y - 1, pos_z], 'facing': 'south'})
    for pos_x in (5, 9):
        room.box((pos_x, 1, 5), (pos_x, 6, 14), state('stone_bricks'))

    room.box((12, 12, 1), (20, 12, 7), state('polished_andesite'))
    for pos_z in (3, 7):
        room.box((20, 7, pos_z), (20, 11, pos_z), state('stone_bricks'))
        room.box((20, 12, pos_z), (22, 12, pos_z), state('stone_bricks'))
    for pos_z in range(8, 14):
        pos_y = 20 - pos_z
        for pos_x in range(14, 17):
            room.box((pos_x, 1, pos_z), (pos_x, pos_y - 1, pos_z), state('stone_bricks'))
            room.set((pos_x, pos_y, pos_z), state('stone_brick_stairs', facing='north', half='bottom', shape='straight', waterlogged=False))
            steps.append({'flight': 'upper', 'position': [pos_x, pos_y, pos_z], 'anchor': [pos_x, pos_y - 1, pos_z], 'facing': 'north'})
        for pos_x in (13, 17):
            room.box((pos_x, 1, pos_z), (pos_x, pos_y + 1, pos_z), state('stone_bricks'))
            guard_anchors.append({'position': [pos_x, pos_y + 1, pos_z], 'anchor': [pos_x, pos_y, pos_z], 'type': 'stepped_stone_parapet'})

    bars = set()
    for pos_x in (5, 9):
        bars.update((pos_x, 7, pos_z) for pos_z in range(5, 14))
    bars.update((pos_x, 7, 5) for pos_x in range(6, 9))
    room.box((6, 6, 5), (8, 6, 5), state('stone_bricks'))
    for pos_x in (12, 20):
        bars.update((pos_x, 13, pos_z) for pos_z in range(1, 8))
    bars.update((pos_x, 13, 7) for pos_x in (13, 17, 18, 19))
    for position in bars:
        room.set(position, state('iron_bars', north=False, south=False, west=False, east=False, waterlogged=False))
    for pos_x in (5, 9):
        room.set((pos_x, 7, 14), state('chiseled_stone_bricks'))
        guard_anchors.append({'position': [pos_x, 7, 14], 'anchor': [pos_x, 6, 14], 'type': 'landing_newel'})
    directions = {'north': (0, 0, -1), 'south': (0, 0, 1), 'west': (-1, 0, 0), 'east': (1, 0, 0)}
    for position in sorted(bars):
        properties = {'waterlogged': False}
        for facing, vector in directions.items():
            neighbor = tuple(position[axis] + vector[axis] for axis in range(3))
            properties[facing] = neighbor in bars or room.get(neighbor).name.removeprefix('minecraft:') in FULL_NAMES
        room.set(position, state('iron_bars', **properties))
        guard_anchors.append({'position': list(position), 'anchor': [position[0], position[1] - 1, position[2]], 'type': 'iron_safety_rail'})
    decorations = []
    for pos_x, pos_y, pos_z in ((2, 3, 4), (2, 3, 18), (20, 3, 4), (20, 3, 20), (2, 10, 4), (20, 10, 15), (2, 10, 21), (20, 16, 4)):
        wall_x = 0 if pos_x == 2 else 22
        room.box((min(pos_x, wall_x), pos_y - 1, pos_z), (max(pos_x, wall_x), pos_y - 1, pos_z), state('stone_bricks'))
        room.set((pos_x, pos_y, pos_z), state('lantern', hanging=False, waterlogged=False))
        decorations.append({'position': [pos_x, pos_y, pos_z], 'anchor': [pos_x, pos_y - 1, pos_z], 'block': 'lantern', 'support_face': 'up'})
    for pos_x in (3, 19):
        for pos_y in range(11, 18):
            room.set((pos_x, pos_y, 20), state('chain', axis='y', waterlogged=False))
            decorations.append({'position': [pos_x, pos_y, 20], 'anchor': [pos_x, pos_y + 1, 20], 'block': 'chain', 'support_face': 'down'})
        room.set((pos_x, 10, 20), state('lantern', hanging=True, waterlogged=False))
        decorations.append({'position': [pos_x, 10, 20], 'anchor': [pos_x, 11, 20], 'block': 'lantern', 'support_face': 'down'})

    crates = [(2, 7, 18), (3, 7, 19), (2, 7, 19), (2, 8, 19)]
    for position in crates:
        room.set(position, state('stripped_oak_wood', axis='y'))
    containers = [(3, 7, 18), (3, 1, 21)]
    for position in containers:
        room.set(position, state('barrel', facing='up', open=False), compound(id=string('minecraft:barrel'), Items=Tag(9, [], 10)))
    carpets = []
    for pos_x in range(7, 17):
        for pos_z in range(18, 21):
            if (pos_x, pos_z) not in ((7, 18), (16, 20), (8, 20), (15, 18)):
                room.set((pos_x, 7, pos_z), state('red_carpet'))
                carpets.append([pos_x, 7, pos_z])
    for position in ((1, 16, 2), (21, 16, 2), (1, 16, 22), (21, 16, 22)):
        room.set(position, state('cobweb'))
    room.set((19, 7, 6), state('stone_brick_slab', type='bottom', waterlogged=False))

    portals = [
        {'id': 'middle', 'feet': [11, 7, 0], 'facing': 'north', 'role': 'FT primary entrance', 'width': 3, 'height': 3},
        {'id': 'lower', 'feet': [7, 1, 0], 'facing': 'north', 'role': 'lower connection reservation', 'width': 3, 'height': 3},
        {'id': 'upper', 'feet': [15, 13, 0], 'facing': 'north', 'role': 'upper connection reservation', 'width': 3, 'height': 3},
    ]
    for portal in portals:
        center, feet_y, _ = portal['feet']
        room.box((center - 1, feet_y - 1, 0), (center + 1, feet_y - 1, 0), state('stone_bricks'))
        room.box((center - 1, feet_y, 0), (center + 1, feet_y + 2, 0), AIR)
        for pos_x in (center - 2, center + 2):
            room.box((pos_x, feet_y - 1, 0), (pos_x, feet_y + 3, 0), state('chiseled_stone_bricks'))
        room.box((center - 1, feet_y + 3, 0), (center + 1, feet_y + 3, 0), state('chiseled_stone_bricks'))

    blueprint = {
        'size': list(SIZE),
        'levels': {'lower_feet': 1, 'middle_feet': 7, 'upper_feet': 13},
        'portals': portals,
        'stair_steps': steps,
        'guards': guard_anchors,
        'supported_decor': decorations,
        'carpets': carpets,
        'empty_containers': [list(position) for position in containers],
        'routes': [
            {'id': 'middle_to_lower', 'start_y': 7, 'end_y': 1, 'points_xz': [[11.5, 0.5], [11.5, 16.5], [7.5, 16.5], [7.5, 0.5]]},
            {'id': 'middle_to_upper', 'start_y': 7, 'end_y': 13, 'points_xz': [[11.5, 0.5], [11.5, 16.5], [15.5, 16.5], [15.5, 0.5]]},
        ],
        'landing': {'bounds': [[6, 6, 14], [17, 6, 17]], 'safe_feet': [11, 7, 16]},
        'lower_opening': {'x': [6, 8], 'z': [6, 13], 'guard_y': 7},
        'upper_landing': {'bounds': [[12, 12, 1], [20, 12, 7]]},
        'walking_probe': {'width': 0.6, 'height': 1.8, 'horizontal_step': 0.25, 'maximum_step_height': 0.6},
    }
    output = ROOT / 'room.nbt'
    room.write(output)
    metadata = {
        'id': 'STAIR_A', 'name': '石砖折返楼梯间', 'size': list(SIZE),
        'entrance': [11, 7, 0], 'interior': [11, 7, 16], 'facing': 'north',
        'sha256': digest(output), 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH,
        'data_version': 3465, 'minecraft': '1.20.1', 'forge': '47.4.10',
        'connections': portals,
        'delivery_state': 'independent_candidate_not_deployed',
        'integration_notes': [
            'Primary entrance feet are six blocks above the lower interface; the external connector must match Y=7.',
            'Lower and upper north interfaces reserve three-wide dry landings; external corridors and fall protection are the integrator responsibility.',
            'Air records do not authorize clearing occupied production volume. Validate complete volume plus buffer before placement.',
            'Candidate ID, slot, FT route, saved-data idempotence, return portal and mob rules require the sole integration owner.',
        ],
    }
    write_json(ROOT / 'room.json', metadata)
    write_json(ROOT / 'qa/blueprint.json', blueprint)
    print(json.dumps({'room': str(output), 'size': SIZE, 'records': len(room.blocks), 'stairs': len(steps), 'guards': len(guard_anchors), 'sha256': metadata['sha256']}, ensure_ascii=False))


if __name__ == '__main__':
    main()
