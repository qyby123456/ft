import hashlib
import json
from collections import deque
from pathlib import Path

from nbtio import AIR, Structure, plain, read_nbt


ROOT = Path(__file__).resolve().parents[1]
FORBIDDEN = {'water', 'lava', 'fire', 'soul_fire', 'magma_block', 'cactus', 'powder_snow', 'campfire', 'soul_campfire', 'wither_rose', 'sweet_berry_bush', 'cobweb', 'spawner', 'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'structure_void', 'light', 'end_portal', 'end_portal_frame', 'nether_portal'}


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def empty_stock(value):
    if isinstance(value, dict):
        for key, child in value.items():
            if key in ('LootTable', 'LootTableSeed', 'Book', 'RecordItem', 'Offers', 'SpawnData', 'SpawnPotentials') or key in ('Items', 'Item') and child:
                return False
            if not empty_stock(child):
                return False
    elif isinstance(value, list):
        return all(empty_stock(child) for child in value)
    return True


def main():
    checks = []

    def check(name, valid, detail):
        checks.append({'name': name, 'status': 'PASS' if valid else 'FAIL', 'detail': detail})

    metadata = load(ROOT / 'out/room.json')
    blueprint = load(ROOT / 'out/blueprint.json')
    room = Structure.read(ROOT / 'out/room.nbt')
    raw = plain(read_nbt(ROOT / 'out/room.nbt')[1])
    fingerprint = digest(ROOT / 'out/room.nbt')
    features = {name: {tuple(position) for position in positions} for name, positions in blueprint['features'].items()}
    check('metadata_hash_and_interface', metadata['sha256'] == fingerprint and metadata['size'] == list(room.size) == [11, 8, 11] and metadata['entrance'] == [5, 1, 0] and metadata['interior'] == [5, 1, 3] and metadata['facing'] == 'north', fingerprint)
    check('gzip_single_palette_version', (ROOT / 'out/room.nbt').read_bytes()[:2] == b'\x1f\x8b' and raw['DataVersion'] == 3465 and 'palette' in raw and 'palettes' not in raw, {'data_version': raw['DataVersion'], 'palette': len(raw['palette'])})
    check('full_unique_bounded_volume', len(room.blocks) == 968 and all(all(type(value) is int and 0 <= value < limit for value, limit in zip(position, room.size)) for position in room.blocks), {'records': len(room.blocks), 'explicit_air': True})
    check('no_entities_spawns_or_trades', not room.entities and empty_stock(raw), {'entities': len(room.entities), 'trade_or_spawn_payloads': False})
    forbidden = sorted({state.name for state in room.blocks.values() if not state.name.startswith('minecraft:') or state.name.removeprefix('minecraft:') in FORBIDDEN or state.get('waterlogged') == 'true'})
    check('only_safe_vanilla_blocks', not forbidden, forbidden)
    check('empty_containers_and_no_loot', all(empty_stock(plain(entity)) for entity in room.block_entities.values()), {'block_entities': len(room.block_entities), 'nonempty_inventory': 0})
    for name, pos_y in (('complete_solid_floor', 0), ('complete_roof', 7)):
        check(name, all(room.get((pos_x, pos_y, pos_z)) != AIR for pos_x in range(11) for pos_z in range(11)), {'checked_cells': 121})
    holes = []
    for pos_x in range(11):
        for pos_z in range(11):
            if pos_x not in (0, 10) and pos_z not in (0, 10):
                continue
            for pos_y in range(1, 7):
                opening = pos_z == 0 and 4 <= pos_x <= 6 and pos_y <= 3
                if (room.get((pos_x, pos_y, pos_z)) == AIR) != opening:
                    holes.append([pos_x, pos_y, pos_z])
    check('closed_shell_only_north_3x3_opening', not holes, holes)
    pillars = features['pillars']
    columns = {(position[0], position[2]) for position in pillars}
    check('four_wall_embedded_pillars', columns == {(0, 0), (10, 0), (0, 10), (10, 10)} and len(pillars) == 24, sorted(columns))
    furniture = set().union(*(positions for name, positions in features.items() if name not in {'walls', 'floor', 'roof', 'beams', 'pillars', 'sign_backing', 'sign', 'chains', 'lanterns'}))
    contacts = [[pillar, other] for pillar in pillars for other in furniture if sum(abs(pillar[axis] - other[axis]) for axis in range(3)) <= 1]
    check('no_pillar_furniture_contact', not contacts, contacts)
    check('independent_5x1_counter', features['counter'] == {(pos_x, 1, 6) for pos_x in range(3, 8)}, sorted(features['counter']))
    for name, key in (('counter_four_sides_clearance', 'counter_clearance'), ('3x3_main_aisle', 'entrance_to_counter_aisle')):
        blocked = [position for position in blueprint[key] if room.get(tuple(position)) != AIR]
        check(name, not blocked, {'cells': len(blueprint[key]), 'blocked': blocked})
    unsupported = [link for link in blueprint['support_links'] if room.get(tuple(link['block'])) == AIR or room.get(tuple(link['support'])) == AIR or link['block'][1] != link['support'][1] + 1]
    check('eye_vat_cage_horns_and_shelf_supports', not unsupported, {'links': len(blueprint['support_links']), 'unsupported': unsupported})
    lamp_failures = []
    for link in blueprint['lamp_supports']:
        if room.get(tuple(link['lantern'])).name not in ('minecraft:lantern', 'minecraft:soul_lantern') or room.get(tuple(link['chain'])).name != 'minecraft:chain' or room.get(tuple(link['beam'])).name != 'minecraft:dark_oak_log':
            lamp_failures.append(link)
    check('lamps_chain_beam_connected_above_aisle', not lamp_failures and len(blueprint['lamp_supports']) == 2, lamp_failures)
    check('wall_attached_shelves', all(position[0] in (1, 9) or position[2] == 9 for position in features['shelves']), {'shelf_blocks': len(features['shelves'])})
    statue = features['merchant_statue'] | features['merchant_horns']
    check('merchant_not_in_counter_or_aisle', all(position[2] == 8 for position in statue) and all(room.get((pos_x, pos_y, 7)) == AIR for pos_x in range(2, 9) for pos_y in (1, 2)), blueprint['merchant_separation'])
    anchored = {position for position in statue if position[1] == 1 and room.get((position[0], 0, position[2])) != AIR}
    pending = deque(anchored)
    while pending:
        position = pending.popleft()
        for offset in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = tuple(position[axis] + offset[axis] for axis in range(3))
            if neighbor in statue and neighbor not in anchored:
                anchored.add(neighbor)
                pending.append(neighbor)
    check('merchant_sculpture_connected_to_floor', anchored == statue, {'connected_blocks': len(anchored), 'statue_blocks': len(statue)})
    check('eye_vat_core_pupil_lid_and_plinth', room.get((2, 2, 2)).name == 'minecraft:shroomlight' and room.get((2, 2, 1)).name == 'minecraft:black_concrete' and len(features['eye_lid']) == 9 and len(features['eye_plinth']) == 9, {'eye_vat': '3x3 green-glass vat on solid plinth'})
    check('cage_floor_seated_no_mobs', len(features['cage_base']) == 9 and len(features['cage_roof']) == 9 and len(features['cage_bars']) == 16 and not room.entities, {'cage': 'static skull effigy only'})

    def walkable(position):
        pos_x, pos_y, pos_z = position
        return 0 <= pos_x < 11 and 0 <= pos_z < 11 and room.get(position) == AIR and room.get((pos_x, pos_y + 1, pos_z)) == AIR and room.get((pos_x, pos_y - 1, pos_z)) != AIR

    start = tuple(metadata['entrance'])
    reached = {start}
    pending = deque([start])
    while pending:
        current = pending.popleft()
        for offset_x, offset_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            next_position = (current[0] + offset_x, 1, current[2] + offset_z)
            if next_position not in reached and walkable(next_position):
                reached.add(next_position)
                pending.append(next_position)
    targets = [tuple(metadata['interior'])] + [tuple(position) for position in blueprint['required_walkable']]
    check('bidirectional_path_and_counter_circuit', walkable(start) and all(position in reached for position in targets), {'reachable_foot_positions': len(reached), 'required': targets})
    integration = metadata['integration']
    check('unassigned_and_not_deployed', integration['assigned_slot'] is None and integration['id_is_provisional'] and not integration['deployed'], integration)
    references = load(ROOT / 'reference/input-fingerprints.json')
    valid_references = all(digest(ROOT / 'reference' / entry['file']) == entry['sha256'] for entry in references['images'] + references['source_documents'])
    check('reference_B_full_fingerprint', valid_references and references['images'][0]['sha256'] == metadata['reference_sha256'], references)
    report = {'status': 'PASS' if all(entry['status'] == 'PASS' for entry in checks) else 'FAIL', 'structure_sha256': fingerprint, 'checks': checks, 'limitations': ['Static air-cell BFS is conservative, not a native collision engine; see separate Forge test.', 'No core integration, deployment, production save, live merchant or trade behavior is tested.']}
    (ROOT / 'qa/static.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps({'status': report['status'], 'checks': len(checks), 'failed': [entry['name'] for entry in checks if entry['status'] != 'PASS']}))
    if report['status'] != 'PASS':
        raise SystemExit(1)


if __name__ == '__main__':
    main()
