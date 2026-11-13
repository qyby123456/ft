import hashlib
import json
import zipfile
from collections import Counter, deque
from pathlib import Path

from build_m7 import DELIVERY, REFERENCE, REFERENCE_SHA256, ROOT
from structure_io import load_nbt, write_json


CLIENT = Path('C:/Users/YOUR_USERNAME/.gradle/caches/forge_gradle/minecraft_repo/versions/1.20.1/client.jar')
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
PARTIAL = ('stairs', 'wall', 'banner', 'skull', 'candle', 'chain', 'lantern', 'campfire', 'bars')
HAZARDS = {'minecraft:lava', 'minecraft:fire', 'minecraft:soul_fire', 'minecraft:magma_block', 'minecraft:campfire', 'minecraft:soul_campfire', 'minecraft:water', 'minecraft:powder_snow', 'minecraft:cobweb'}


def add(first, second):
    return tuple(first[index] + second[index] for index in range(3))


def main():
    template = load_nbt(DELIVERY / 'room.nbt')
    metadata = json.loads((DELIVERY / 'room.json').read_text(encoding='utf-8'))
    features = json.loads((ROOT / 'qa/features.json').read_text(encoding='utf-8'))
    size = template['size']
    checks = []

    def require(condition, label, details=None):
        checks.append({'check': label, 'status': 'PASS' if condition else 'FAIL', 'details': details})
        if not condition:
            raise AssertionError(label + ': ' + str(details))

    actual_hash = hashlib.sha256((DELIVERY / 'room.nbt').read_bytes()).hexdigest()
    require(actual_hash == metadata['sha256'], 'structure_sha256', actual_hash)
    require(hashlib.sha256(REFERENCE.read_bytes()).hexdigest() == REFERENCE_SHA256, 'reference_sha256', REFERENCE_SHA256)
    require(template['DataVersion'] == 3465 and size == metadata['size'], 'minecraft_1_20_1_dimensions', size)
    require(not template['entities'], 'no_spawned_entities')
    states = {}
    inventory_count = 0
    for record in template['blocks']:
        position = tuple(record['pos'])
        require(position not in states and all(0 <= coordinate < limit for coordinate, limit in zip(position, size)), 'unique_bounded_record')
        states[position] = template['palette'][record['state']]
        data = record.get('nbt', {})
        inventory_count += len(data.get('Items', []))
        require('LootTable' not in data and not data.get('Items'), 'no_reward_inventory')
    checks = [check for check in checks if check['check'] not in ('unique_bounded_record', 'no_reward_inventory')]
    require(len(states) == size[0] * size[1] * size[2], 'full_volume_explicit_records', len(states))
    require(not inventory_count, 'zero_reward_items', inventory_count)
    forbidden = {'minecraft:command_block', 'minecraft:repeating_command_block', 'minecraft:chain_command_block', 'minecraft:jigsaw', 'minecraft:spawner', 'minecraft:structure_block', 'minecraft:structure_void', 'minecraft:end_portal', 'minecraft:nether_portal'}
    with zipfile.ZipFile(CLIENT) as assets:
        for state in template['palette']:
            identity = state['Name']
            require(identity.startswith('minecraft:') and identity not in forbidden, 'vanilla_only_safe_palette', identity)
            require('assets/minecraft/blockstates/' + identity.removeprefix('minecraft:') + '.json' in assets.namelist(), 'available_client_block', identity)

    def name(position):
        return states.get(position, {'Name': 'minecraft:air'})['Name']

    def full(position):
        identity = name(position)
        return identity not in HAZARDS | {'minecraft:air'} and not any(identity.endswith('_' + suffix) or identity == 'minecraft:' + suffix for suffix in PARTIAL)

    def walkable(position):
        floor = add(position, (0, -1, 0))
        return full(floor) and name(position) == 'minecraft:air' and name(add(position, (0, 1, 0))) == 'minecraft:air'

    entrance = tuple(metadata['entrance'])
    require(entrance[0] == size[0] // 2 and entrance[2] == 0 and metadata['facing'] == 'north', 'north_centered_interface', entrance)
    require(all(walkable(add(entrance, (offset, 0, 0))) and name(add(entrance, (offset, 2, 0))) == 'minecraft:air' for offset in (-1, 0, 1)), 'dry_supported_3x3_opening')
    reached = {entrance}
    pending = deque([entrance])
    while pending:
        current = pending.popleft()
        for delta in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1)):
            candidate = add(current, delta)
            if 0 <= candidate[0] < size[0] and 0 <= candidate[2] < size[2] and candidate not in reached and walkable(candidate):
                reached.add(candidate)
                pending.append(candidate)
    waypoints = [metadata['interior'], [4, 3, 7], [14, 3, 7], [9, 3, 15], [9, 3, 16]]
    require(all(tuple(position) in reached for position in waypoints), 'two_sided_circumnavigation_and_return', {'waypoints': waypoints, 'reachable_floor_positions': len(reached)})
    require(all(full((block_x, size[1] - 1, block_z)) for block_x in range(size[0]) for block_z in range(size[2])), 'complete_solid_roof', size[0] * size[2])
    lava = {position for position, state in states.items() if state['Name'] == 'minecraft:lava'}
    require(len(lava) == len(features['lava']) and bool(lava), 'real_lava_record_count', len(lava))
    for position in lava:
        require(all(add(position, delta) in lava or full(add(position, delta)) for delta in DIRECTIONS), 'sealed_lava_six_faces', position)
        require(all(0 < position[index] < size[index] - 1 for index in range(3)), 'lava_not_on_boundary', position)
    require(not any(state['Name'] in ('minecraft:fire', 'minecraft:soul_fire', 'minecraft:water') for state in states.values()), 'no_open_fire_or_unsupported_fluid')
    for label in ('candles', 'skulls', 'braziers'):
        require(all(full(add(tuple(position), (0, -1, 0))) for position in features[label]), label + '_floor_support', len(features[label]))
    for position in features['lanterns']:
        cursor = add(tuple(position), (0, 1, 0))
        while name(cursor) == 'minecraft:chain':
            require(states[cursor]['Properties']['axis'] == 'y', 'vertical_lantern_chain_axis')
            cursor = add(cursor, (0, 1, 0))
        require(full(cursor), 'lantern_chain_roof_anchor', cursor)
    facing_support = {'north': (0, 0, 1), 'south': (0, 0, -1), 'east': (-1, 0, 0), 'west': (1, 0, 0)}
    require(all(full(add(tuple(position), facing_support[states[tuple(position)]['Properties']['facing']])) for position in features['banners']), 'all_banner_wall_support', len(features['banners']))
    solids = {position for position in states if full(position)}
    anchored = {position for position in solids if position[1] == 0}
    pending = deque(anchored)
    while pending:
        current = pending.popleft()
        for delta in DIRECTIONS:
            neighbor = add(current, delta)
            if neighbor in solids and neighbor not in anchored:
                anchored.add(neighbor)
                pending.append(neighbor)
    require(anchored == solids, 'all_full_structure_face_connected_to_foundation', {'anchored': len(anchored), 'unsupported': list(solids - anchored)})
    for position in features['chains']:
        position = tuple(position)
        axis = states[position]['Properties']['axis']
        delta = (0, 1, 0) if axis == 'y' else (0, 0, 1)
        endpoints = []
        for sign in (-1, 1):
            cursor = add(position, tuple(value * sign for value in delta))
            while name(cursor) == 'minecraft:chain' and states[cursor]['Properties']['axis'] == axis:
                cursor = add(cursor, tuple(value * sign for value in delta))
            endpoints.append(cursor)
        require(any(endpoint in anchored for endpoint in endpoints), 'chain_connected_to_real_anchor', position)
    result = {'status': 'PASS', 'id': 'M7', 'structure_sha256': actual_hash, 'reference_sha256': REFERENCE_SHA256, 'size': size, 'block_records': len(states), 'non_air_blocks': sum(state['Name'] != 'minecraft:air' for state in states.values()), 'palette_states': len(template['palette']), 'block_entities': sum('nbt' in record for record in template['blocks']), 'reachable_floor_positions': len(reached), 'checks': checks, 'materials': dict(Counter(state['Name'] for state in states.values())), 'scope': 'Static NBT, legal palette assets, physical support graph, safe route and full lava enclosure. Native Forge placement is recorded separately.'}
    write_json(ROOT / 'qa/static.json', result)
    print(f'M7_STATIC_PASS records={len(states)} checks={len(checks)} floor_reachable={len(reached)} lava={len(lava)}')


if __name__ == '__main__':
    main()
