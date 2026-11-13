import json
from collections import deque
from pathlib import Path

from PIL import Image

from build_room import REFERENCE, REFERENCE_HASH, ROOT, SIZE, fingerprint, save_json
from nbtio import AIR, Structure, plain, read_nbt


MASONRY = {'minecraft:stone_bricks', 'minecraft:cracked_stone_bricks', 'minecraft:mossy_stone_bricks'}
FULL = MASONRY | {'minecraft:barrel', 'minecraft:sea_lantern'}
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'campfire', 'soul_campfire', 'spawner', 'magma_block', 'cactus', 'powder_snow', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'sand', 'gravel', 'anvil', 'chipped_anvil', 'damaged_anvil'}


def run():
    room = Structure.read(ROOT / 'out/room.nbt')
    _, tag = read_nbt(ROOT / 'out/room.nbt')
    encoded = plain(tag)
    metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    blueprint = json.loads((ROOT / 'out/blueprint.json').read_text(encoding='utf-8'))
    features = blueprint['features']
    checks = []

    def check(name, condition, detail):
        checks.append({'check': name, 'status': 'PASS' if condition else 'FAIL', 'detail': detail})

    def full(position):
        return room.get(tuple(position)).name in FULL

    def supported(position):
        pos_x, pos_y, pos_z = position
        below = room.get((pos_x, pos_y - 1, pos_z))
        return below.name in FULL or below.name.endswith('_stairs') or below.name.endswith('_slab') and below.get('type') in ('top', 'double')

    def safe(position):
        pos_x, pos_y, pos_z = position
        return room.get((pos_x, pos_y, pos_z)) == AIR and room.get((pos_x, pos_y + 1, pos_z)) == AIR and supported(position)

    check('source_fingerprint', fingerprint(REFERENCE) == REFERENCE_HASH == fingerprint(ROOT / 'reference/stair-ruined.png'), REFERENCE_HASH)
    with Image.open(REFERENCE) as image:
        check('source_dimensions', image.size == (1536, 1024), list(image.size))
    check('gzip_single_palette', (ROOT / 'out/room.nbt').read_bytes()[:2] == b'\x1f\x8b' and 'palette' in encoded and 'palettes' not in encoded, 'Java compressed single palette')
    check('data_version', room.data_version == 3465, room.data_version)
    check('metadata_fingerprint', metadata['sha256'] == fingerprint(ROOT / 'out/room.nbt'), metadata['sha256'])
    check('dimensions', room.size == SIZE == tuple(metadata['size']), list(room.size))
    check('complete_explicit_volume', len(room.blocks) == 23 * 18 * 25 == len(encoded['blocks']), len(room.blocks))
    check('unique_bounded_positions', len({tuple(entry['pos']) for entry in encoded['blocks']}) == len(room.blocks) and all(all(0 <= coordinate < limit for coordinate, limit in zip(position, SIZE)) for position in room.blocks), len(room.blocks))
    check('vanilla_only', all(block.name.startswith('minecraft:') for block in room.blocks.values()), 'Native registry/properties independently checked by Forge')
    check('no_hazards_or_gravity_blocks', all(block.name.split(':')[1] not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()), sorted(FORBIDDEN))
    check('no_entities', not room.entities, len(room.entities))
    check('empty_containers_only', len(room.block_entities) == 4 and all(plain(value) == {'id': 'minecraft:barrel', 'Items': []} for value in room.block_entities.values()), len(room.block_entities))
    check('complete_foundation', all(full((pos_x, 0, pos_z)) for pos_x in range(23) for pos_z in range(25)), 575)
    check('complete_roof', all(full((pos_x, 17, pos_z)) for pos_x in range(23) for pos_z in range(25)), 575)
    allowed = set()
    for portal in metadata['interfaces']:
        center_x, feet_y, pos_z = portal['feet']
        opening = {(pos_x, pos_y, pos_z) for pos_x in range(center_x - 1, center_x + 2) for pos_y in range(feet_y, feet_y + 3)}
        allowed.update(opening)
        check('portal_' + portal['name'], all(room.get(position) == AIR for position in opening) and all(full((pos_x, feet_y - 1, pos_z)) for pos_x in range(center_x - 1, center_x + 2)), portal)
    boundary = {position for position in room.blocks if position[0] in (0, 22) or position[2] in (0, 24)}
    check('shell_only_declared_openings', all((room.get(position) == AIR) == (position in allowed) for position in boundary), {'declared_air_cells': len(allowed), 'boundary_records': len(boundary)})
    check('north_centered_entrance', metadata['entrance'] == [11, 6, 0] and metadata['facing'] == 'north', metadata['entrance'])
    check('main_clearance_three_high', all(room.get((pos_x, pos_y, pos_z)) == AIR for pos_x in range(10, 13) for pos_y in range(6, 9) for pos_z in range(9)), 'X10..12,Y6..8,Z0..8')
    required = {tuple(position) for position in blueprint['required_walkable']}
    blocked = [position for position in required if not safe(position)]
    check('three_wide_route_foot_head_support', not blocked, {'required_nodes': len(required), 'blocked': blocked})
    reached = {tuple(metadata['entrance'])}
    pending = deque(reached)
    while pending:
        current = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for offset_y in (-1, 0, 1):
                candidate = (current[0] + offset_x, current[1] + offset_y, current[2] + offset_z)
                if candidate in required and candidate not in reached and safe(candidate):
                    if offset_y and not any(room.get((point[0], point[1] - 1, point[2])).name.endswith('_stairs') for point in (current, candidate)):
                        continue
                    reached.add(candidate)
                    pending.append(candidate)
    check('declared_routes_connected', required <= reached, {'reached': len(reached), 'missing': sorted(required - reached)})
    check('all_safe_points_connected', all(tuple(portal[key]) in reached for portal in metadata['interfaces'] for key in ('feet', 'interior')), metadata['interfaces'])
    check('upper_three_wide_preserved_lane', all(room.get((pos_x, pos_z - 4, pos_z)).name.endswith('_stairs') for pos_x in range(5, 8) for pos_z in range(10, 16)), 'X5..7,Z10..15, six risers')
    check('visible_upper_break', len(features['broken_top']) == 4 and all(not room.get(tuple(position)).name.endswith('_stairs') for position in features['broken_top']), features['broken_top'])
    check('lower_three_wide_gap', all(room.get((pos_x, 15 - pos_z, pos_z)).name.endswith('_stairs') for pos_x in range(15, 18) for pos_z in range(10, 15)), 'X15..17,Z10..14, five descending risers')
    check('stair_full_footings', all(full((pos_x, pos_y - 1, pos_z)) for pos_x, pos_y, pos_z in features['stairs_up'] + features['stairs_down']), len(features['stairs_up']) + len(features['stairs_down']))
    check('rubble_grounded_or_stacked', all(supported(position) for position in features['debris']), len(features['debris']))
    check('fallen_column_really_obstructs_outer_two_columns', all(room.get((pos_x, 16 - pos_z, pos_z)).name in MASONRY for pos_x in (18, 19) for pos_z in range(10, 15)), 'Two occupied columns; no fake unobstructed descent')
    check('beam_wall_anchor', all(room.get((22, pos_y, pos_z)).name in MASONRY for pos_y in (14, 15) for pos_z in (21, 22)), 'East wall X22')
    check('beam_grounded_pier', all(full((pos_x, pos_y, 22)) for pos_x in (14, 15) for pos_y in range(14)), 'Two full-height masonry stacks beneath broken end')
    check('rails_grounded', all(supported(position) for position in features['rails']), len(features['rails']))
    check('cobwebs_wall_attached', all(any(room.get((pos_x + offset_x, pos_y, pos_z + offset_z)).name in FULL | {'minecraft:light_blue_stained_glass'} for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1))) for pos_x, pos_y, pos_z in features['cobwebs']), features['cobwebs'])
    check('cold_breach_sealed', len(features['cold_window']) == 15 and all(room.get(tuple(position)).name == 'minecraft:light_blue_stained_glass' for position in features['cold_window']), 'Sealed glass plus sea-lantern lintel, roof intact')
    check('lantern_support', all(all(room.get((entry['pos'][0], pos_y, entry['pos'][2])).name == 'minecraft:chain' for pos_y in range(entry['pos'][1] + 1, 17)) and full((entry['pos'][0], 17, entry['pos'][2])) if entry['hanging'] else supported(entry['pos']) for entry in features['lanterns']), features['lanterns'])
    check('no_slot_or_deployment', metadata['assigned_slot'] is None and metadata['deployment_status'] == 'independent_candidate_not_deployed', 'No shared incoming, core, mods or player-save writes')
    result = {'status': 'PASS' if all(entry['status'] == 'PASS' for entry in checks) else 'FAIL', 'room_sha256': metadata['sha256'], 'checks': checks, 'pass_count': sum(entry['status'] == 'PASS' for entry in checks), 'total': len(checks), 'limitations': ['Static stair graph is not the native player collision proof; see the independent Forge sampled-step QA.']}
    save_json(ROOT / 'qa/static.json', result)
    print(json.dumps({'status': result['status'], 'passed': result['pass_count'], 'total': len(checks), 'failures': [entry for entry in checks if entry['status'] == 'FAIL']}, ensure_ascii=False))
    if result['status'] != 'PASS':
        raise SystemExit(1)


if __name__ == '__main__':
    run()
