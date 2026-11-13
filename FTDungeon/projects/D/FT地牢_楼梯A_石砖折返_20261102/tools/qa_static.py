import json
from collections import Counter, deque
from pathlib import Path

from build_room import FULL_NAMES, REFERENCE, REFERENCE_HASH, ROOT, SIZE, digest, write_json
from nbtio import Structure, plain


FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'campfire', 'soul_campfire', 'magma_block', 'cactus', 'powder_snow', 'sweet_berry_bush', 'wither_rose', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'structure_void', 'jigsaw', 'end_portal', 'end_gateway', 'nether_portal', 'light'}
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def short(block):
    return block.name.removeprefix('minecraft:')


def empty_stock(value):
    if isinstance(value, dict):
        return all(key not in {'LootTable', 'LootTableSeed', 'Book', 'RecordItem'} and (key not in {'Items', 'Item'} or child == []) and empty_stock(child) for key, child in value.items())
    if isinstance(value, list):
        return all(empty_stock(child) for child in value)
    return True


def full(room, position):
    return short(room.get(position)) in FULL_NAMES


def main():
    checks = []
    report = {'status': 'RUNNING', 'evidence_level': 'static_nbt_not_native', 'checks': checks}

    def require(name, condition, detail):
        checks.append({'name': name, 'status': 'PASS' if condition else 'FAIL', 'detail': detail})
        if not condition:
            raise ValueError(f'{name}: {detail}')

    try:
        room = Structure.read(ROOT / 'room.nbt')
        metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
        blueprint = json.loads((ROOT / 'qa/blueprint.json').read_text(encoding='utf-8'))
        report['source_sha256'] = digest(ROOT / 'room.nbt')
        report['block_counts'] = dict(sorted(Counter(short(block) for block in room.blocks.values()).items()))
        require('design_and_metadata_fingerprints', digest(REFERENCE) == REFERENCE_HASH == metadata['reference_sha256'] and report['source_sha256'] == metadata['sha256'], 'Existing design and exported room fingerprints match; no new image API call')
        require('full_explicit_volume', room.size == SIZE and room.data_version == 3465 and len(room.blocks) == 23 * 19 * 25 and all(all(0 <= value < bound for value, bound in zip(position, SIZE)) for position in room.blocks), f'{len(room.blocks)} unique records including air, Java 1.20.1 DataVersion 3465')
        require('vanilla_and_no_forbidden_blocks', all(block.name.startswith('minecraft:') and short(block) not in FORBIDDEN and block.get('waterlogged', 'false') == 'false' for block in room.blocks.values()), 'Only original vanilla candidates; no liquid, live fire, rewards, spawner, admin or portal blocks')
        require('empty_entities_and_containers', not room.entities and all(empty_stock(plain(value)) for value in room.block_entities.values()), f'{len(room.block_entities)} empty barrels, no loot or entity')
        require('complete_floor_and_roof', all(full(room, (pos_x, pos_y, pos_z)) for pos_y in (0, 18) for pos_x in range(23) for pos_z in range(25)), '575 floor and 575 roof full blocks; preview clipping cannot alter room.nbt')
        portal_air = set()
        for portal in blueprint['portals']:
            pos_x, pos_y, _ = portal['feet']
            opening = {(pos_x + offset, pos_y + height, 0) for offset in (-1, 0, 1) for height in range(3)}
            portal_air |= opening
            require('portal_' + portal['id'], all(short(room.get(position)) == 'air' for position in opening) and all(full(room, (pos_x + offset, pos_y - 1, 0)) for offset in (-1, 0, 1)), f'North 3x3 clear portal at feet {portal["feet"]}, three complete dry floor blocks')
        perimeter = {position for position in room.blocks if position[0] in (0, 22) or position[2] in (0, 24)}
        require('complete_envelope_except_three_ports', all(full(room, position) for position in perimeter - portal_air), f'{len(perimeter - portal_air)} full perimeter cells; only the three declared doorways are open')
        stair_positions = {tuple(step['position']) for step in blueprint['stair_steps']}
        require('all_stairs_audited', stair_positions == {position for position, block in room.blocks.items() if short(block) == 'stone_brick_stairs'} and len(stair_positions) == 36, 'Two opposite-facing flights, six rises each, three blocks wide')
        require('each_step_has_full_support', all(full(room, tuple(step['anchor'])) and room.get(tuple(step['position'])).get('facing') == step['facing'] and room.get(tuple(step['position'])).get('half') == 'bottom' for step in blueprint['stair_steps']), '36 stair cells supported by full masonry, no suspended tread')
        require('continuous_solid_step_foundations', all(full(room, (step['position'][0], pos_y, step['position'][2])) for step in blueprint['stair_steps'] for pos_y in range(0, step['position'][1])), 'Every tread has a continuous solid foundation to Y=0')
        require('every_guard_is_floor_supported', all(full(room, tuple(guard['anchor'])) and short(room.get(tuple(guard['position']))) in FULL_NAMES | {'iron_bars'} for guard in blueprint['guards']), f'{len(blueprint["guards"])} documented guard positions and sturdy anchors')
        bars = {position for position, block in room.blocks.items() if short(block) == 'iron_bars'}
        required_bars = {(pos_x, 7, pos_z) for pos_x in (5, 9) for pos_z in range(5, 14)} | {(pos_x, 7, 5) for pos_x in range(6, 9)}
        require('lower_shaft_has_no_unguarded_side', required_bars <= bars and all(short(room.get((pos_x, 7, 14))) == 'air' for pos_x in range(6, 9)), 'Both shaft sides and north lip guarded; the south landing remains open for the stair exit')
        require('upper_landing_safety_rails', {(pos_x, 13, pos_z) for pos_x in (12, 20) for pos_z in range(1, 8)} <= bars and {(pos_x, 13, 7) for pos_x in (13, 17, 18, 19)} <= bars, 'Upper platform side/far edges guarded except the three-wide stair and north doorway')
        require('upper_flight_stepped_parapets', all(full(room, (pos_x, 21 - pos_z, pos_z)) for pos_x in (13, 17) for pos_z in range(8, 14)), 'Both ascending flight sides have a one-full-block-above-tread parapet')
        require('dry_main_entrance_and_safe_point', metadata['entrance'] == [11, 7, 0] and metadata['interior'] == [11, 7, 16] and all(short(room.get((pos_x, pos_y, pos_z))) == 'air' for pos_x in range(10, 13) for pos_z in range(17) for pos_y in range(7, 10)), 'Three-wide by three-high north-to-landing middle aisle; safe point is not carpet or furniture')
        require('carpet_support', all(full(room, (position[0], position[1] - 1, position[2])) for position in blueprint['carpets']), 'Every red carpet has a full, dry supporting floor')
        for entry in blueprint['supported_decor']:
            position, anchor = tuple(entry['position']), tuple(entry['anchor'])
            expected = (0, -1, 0) if entry['support_face'] == 'up' else (0, 1, 0)
            attached = all(anchor[axis] - position[axis] == expected[axis] for axis in range(3))
            anchor_ok = full(room, anchor) or short(room.get(anchor)) == 'chain' and room.get(anchor).get('axis') == 'y'
            require('decor_' + '_'.join(map(str, position)), attached and anchor_ok and short(room.get(position)) == entry['block'], f'{entry["block"]} anchored at {anchor}')
        non_air = {position for position, block in room.blocks.items() if short(block) != 'air'}
        connected = {position for position in non_air if position[1] == 0}
        queue = deque(connected)
        while queue:
            position = queue.popleft()
            for vector in DIRECTIONS:
                neighbor = tuple(position[axis] + vector[axis] for axis in range(3))
                if neighbor in non_air and neighbor not in connected:
                    connected.add(neighbor)
                    queue.append(neighbor)
        require('all_material_connected_to_foundation', connected == non_air, f'{len(connected)}/{len(non_air)} non-air records connected; explicit native center-face support and collision checks still required')
        report['status'] = 'PASS'
        report['limitations'] = ['Static connectivity is not player movement proof. Native collision, survival, rotations and tick stability are separate evidence.', 'No production slot, FT hallway, return, mob rules, deployment or multiplayer verification.']
    except Exception as error:
        report['status'] = 'FAIL'
        report['error'] = str(error)
    write_json(ROOT / 'qa/static.json', report)
    print(json.dumps({'status': report['status'], 'checks': len(checks), 'error': report.get('error')}, ensure_ascii=False))
    if report['status'] != 'PASS':
        raise SystemExit(1)


if __name__ == '__main__':
    main()
