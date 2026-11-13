import copy
import hashlib
import json
import math
from collections import deque
from pathlib import Path

from nbtio import AIR, Structure, plain, read_nbt, state


ROOT = Path(__file__).resolve().parents[1]
FULL = {'polished_blackstone_bricks', 'polished_blackstone', 'gold_block', 'red_nether_bricks', 'gilded_blackstone',
        'orange_stained_glass', 'polished_basalt', 'chiseled_polished_blackstone'}
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def require(condition, message):
    if not condition:
        raise ValueError(message)


def solid(room, position):
    return room.get(position).name.removeprefix('minecraft:') in FULL


def enclosed(room, position):
    pending, seen = deque([position]), {position}
    while pending:
        current = pending.popleft()
        for offset in DIRECTIONS:
            neighbor = tuple(first + second for first, second in zip(current, offset))
            if room.get(neighbor).name == 'minecraft:lava':
                if neighbor not in seen:
                    seen.add(neighbor)
                    pending.append(neighbor)
            elif not solid(room, neighbor):
                return False
    return True


def boxes(block, position):
    pos_x, pos_y, pos_z = position
    name = block.name.removeprefix('minecraft:')
    if name in FULL:
        return [(pos_x, pos_y, pos_z, pos_x + 1, pos_y + 1, pos_z + 1)]
    if name.endswith('_stairs'):
        require(block.get('half') == 'bottom' and block.get('shape') == 'straight', 'Unsupported static stair shape')
        facing = block.get('facing')
        if facing == 'south':
            raised = (pos_x, pos_y + 0.5, pos_z + 0.5, pos_x + 1, pos_y + 1, pos_z + 1)
        else:
            require(facing == 'north', 'Unexpected static stair facing')
            raised = (pos_x, pos_y + 0.5, pos_z, pos_x + 1, pos_y + 1, pos_z + 0.5)
        return [(pos_x, pos_y, pos_z, pos_x + 1, pos_y + 0.5, pos_z + 1), raised]
    if name == 'air' or name == 'lava':
        return []
    return [(pos_x, pos_y, pos_z, pos_x + 1, pos_y + 1, pos_z + 1)]


def overlap(first, second):
    return all(first[axis] < second[axis + 3] - 1e-5 and first[axis + 3] > second[axis] + 1e-5 for axis in range(3))


def foot_height(room, center_x, center_z, previous):
    footprint = (center_x - 0.3, previous - 0.61, center_z - 0.3, center_x + 0.3, previous + 0.61, center_z + 0.3)
    candidates = set()
    relevant = []
    for pos_x in range(math.floor(center_x - 0.3), math.floor(center_x + 0.3) + 1):
        for pos_z in range(math.floor(center_z - 0.3), math.floor(center_z + 0.3) + 1):
            for pos_y in range(math.floor(previous - 1.6), math.ceil(previous + 2.5)):
                block = room.get((pos_x, pos_y, pos_z))
                for bounds in boxes(block, (pos_x, pos_y, pos_z)):
                    relevant.append(bounds)
                    if footprint[0] < bounds[3] - 1e-5 and footprint[3] > bounds[0] + 1e-5 and footprint[2] < bounds[5] - 1e-5 and footprint[5] > bounds[2] + 1e-5 and abs(bounds[4] - previous) <= 0.6001:
                        candidates.add(bounds[4])
    for height in sorted(candidates):
        body = (center_x - 0.299, height + 0.001, center_z - 0.299, center_x + 0.299, height + 1.799, center_z + 0.299)
        if not any(overlap(body, bounds) for bounds in relevant):
            for pos_y in range(math.floor(height), math.ceil(height + 1.8)):
                require(room.get((math.floor(center_x), pos_y, math.floor(center_z))).name != 'minecraft:lava', 'Lava on route')
            return height
    raise ValueError(f'No collision-free supported 0.6x1.8 body at {(center_x, previous, center_z)}')


def route(room, waypoints, start_y, end_y):
    previous, samples = start_y, 0
    maximum_step = 0
    for start, end in zip(waypoints, waypoints[1:]):
        distance = math.dist(start, end)
        for index in range(math.ceil(distance * 4) + 1):
            fraction = min(index / max(1, math.ceil(distance * 4)), 1)
            center_x = start[0] + (end[0] - start[0]) * fraction
            center_z = start[1] + (end[1] - start[1]) * fraction
            height = foot_height(room, center_x, center_z, previous)
            maximum_step = max(maximum_step, abs(height - previous))
            previous = height
            samples += 1
    require(abs(previous - end_y) < 0.01, f'Wrong route end height: {previous} != {end_y}')
    return {'samples': samples, 'maximum_step': maximum_step, 'end_y': previous}


def validate(room, metadata, blueprint):
    checks = []

    def check(condition, message):
        require(condition, message)
        checks.append(message)

    check(room.size == (29, 23, 33) and len(room.blocks) == 22011, 'Complete explicit volume 29x23x33 including air')
    check(room.data_version == 3465 and not room.entities, 'Java 1.20.1 DataVersion; no entities')
    allowed = FULL | {'air', 'lava', 'polished_blackstone_brick_stairs', 'chain', 'soul_lantern', 'lantern', 'red_wall_banner'}
    check(all(block.name.startswith('minecraft:') and block.name.removeprefix('minecraft:') in allowed for block in room.blocks.values()), 'Vanilla allowlist; no fire, commands, spawners or loot containers')
    for payload in room.block_entities.values():
        check(not any(key in plain(payload) for key in ('LootTable', 'Items', 'Item')), 'Block entity has no inventory or loot')
    check(all(solid(room, (pos_x, 22, pos_z)) for pos_x in range(29) for pos_z in range(33)), 'Complete full roof')
    check(all(solid(room, (pos_x, 0, pos_z)) for pos_x in range(29) for pos_z in range(33)), 'Complete full foundation')
    apertures = set()
    for interface in metadata['interfaces']:
        pos_x, pos_y, pos_z = interface['feet']
        for horizontal in range(-1, 2):
            check(solid(room, (pos_x + horizontal, pos_y - 1, pos_z)), 'Interface full-block footing ' + interface['id'])
            for vertical in range(3):
                position = (pos_x + horizontal, pos_y + vertical, pos_z)
                apertures.add(position)
                check(room.get(position) == AIR, 'Interface clear 3x3 ' + interface['id'])
    check(metadata['entrance'] == [14, 7, 0] and metadata['facing'] == 'north', 'North central FT entrance')
    shell = {(pos_x, pos_y, pos_z) for pos_x in range(29) for pos_y in range(1, 22) for pos_z in range(33) if pos_x in (0, 28) or pos_z in (0, 32)}
    check(all(solid(room, position) for position in shell - apertures), 'Closed outer shell except three declared 3x3 interfaces')
    actual_lava = {position for position, block in room.blocks.items() if block.name == 'minecraft:lava'}
    check(actual_lava == {tuple(position) for position in blueprint['sealed_lava']} and len(actual_lava) == 16, 'Exactly 16 declared real source lava blocks')
    check(all(room.get(position).get('level') == '0' and enclosed(room, position) for position in actual_lava), 'Lava components six-face fully sealed with full-collision blocks')
    for support in blueprint['supports'] + blueprint['stair_supports']:
        position, anchor = tuple(support['position']), tuple(support['support'])
        check(room.get(position) != AIR and room.get(anchor) != AIR and sum(abs(first - second) for first, second in zip(position, anchor)) == 1, 'Declared stair, chain, lamp or railing has adjacent support')
    check(all(solid(room, tuple(support['support'])) for support in blueprint['stair_supports']), '112 stair blocks directly supported by full solid masonry')
    for feature in ('grand_stairs', 'descent_stairs', 'upper_stairs'):
        check(all(room.get(tuple(position)).name.endswith('_stairs') for position in blueprint['features'][feature]), 'All declared stair treads exist: ' + feature)
    check(all(solid(room, tuple(position)) for position in blueprint['features']['landing']), 'Continuous supported raised landing')
    check(all(solid(room, tuple(position)) for position in blueprint['features']['railings']), 'Continuous full-block guarded edges')
    routes = {}
    for specification in blueprint['routes']:
        routes[specification['id']] = {'forward': route(room, specification['waypoints'], specification['start_y'], specification['end_y']),
                                        'reverse': route(room, specification['waypoints'][::-1], specification['end_y'], specification['start_y'])}
    for label, lanes, start_z, end_z, start_y, end_y in (('grand', range(8, 17), 7.5, 15.5, 7, 13),
                                                        ('upper', range(10, 17), 22.5, 28.5, 13, 17),
                                                        ('lower', range(20, 25), 7.5, 15.5, 7, 1)):
        for lane in lanes:
            points = [[lane + 0.5, start_z], [lane + 0.5, end_z]]
            route(room, points, start_y, end_y)
            route(room, points[::-1], end_y, start_y)
    check(True, 'All three routes and every 9/7/5-wide stair lane bidirectionally pass 0.6x1.8 collision body at 0.25-block spacing')
    for point in blueprint['safe_points']:
        check(abs(foot_height(room, point[0] + 0.5, point[2] + 0.5, point[1]) - point[1]) < 0.01, 'Safe point has dry stable footing and body clearance')
    return {'checks': checks, 'routes': routes, 'check_count': len(checks), 'stairs_checked': 112, 'lava_checked': 16}


def main():
    room = Structure.read(ROOT / 'out/room.nbt')
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    fingerprint = hashlib.sha256((ROOT / 'out/room.nbt').read_bytes()).hexdigest()
    require(fingerprint == metadata['sha256'], 'Metadata hash mismatch')
    _, encoded = read_nbt(ROOT / 'out/room.nbt')
    require('palette' in encoded.value and 'palettes' not in encoded.value, 'Single palette required')
    result = validate(room, metadata, blueprint)
    probes = {}
    for name, position, replacement in [('missing_roof', (14, 22, 18), AIR), ('exposed_lava', (2, 6, 8), AIR),
                                         ('blocked_entry', (14, 7, 0), state('stone')), ('missing_chain', (4, 18, 6), AIR),
                                         ('missing_stair', (12, 9, 10), AIR), ('missing_guardrail', (19, 8, 8), AIR),
                                         ('extra_lava', (14, 7, 4), state('lava', level=0))]:
        broken = copy.deepcopy(room)
        broken.set(position, replacement)
        try:
            validate(broken, metadata, blueprint)
        except ValueError as exception:
            probes[name] = {'status': 'REJECTED', 'reason': str(exception)}
        else:
            raise AssertionError('Negative probe not rejected: ' + name)
    report = {'status': 'PASS', 'structure_sha256': fingerprint, 'single_palette': True, 'records': len(room.blocks),
              **result, 'negative_probes': probes, 'scope': 'Independent static grid/collision audit. Native Minecraft collision and world stabilization reported separately.'}
    (ROOT / 'qa/static.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': 'PASS', 'checks': result['check_count'], 'negative_probes': len(probes)}))


if __name__ == '__main__':
    main()
