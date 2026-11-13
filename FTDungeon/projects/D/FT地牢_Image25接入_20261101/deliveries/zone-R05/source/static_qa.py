from __future__ import annotations

from collections import Counter, deque
from pathlib import Path
import hashlib
import json
import sys

ROOT = Path(__file__).resolve().parents[1]
NBT_TOOLS = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/output/dungeon-expansion-20261101/tools')
sys.path.insert(0, str(NBT_TOOLS))
from nbtio import Structure, read_nbt, plain

REFERENCE = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/ruins/R05-ruins-shattered-archives-hall.png')
REFERENCE_HASH = '8440f06ed3f0330b74660e57fa1d51376a212541c49b7e540eaa7f9759b3a810'
SIZE = (27, 16, 29)
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
DIRECTIONS = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))


def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()


def dump(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def main():
    structure = Structure.read(ROOT / 'room.nbt')
    tag = plain(read_nbt(ROOT / 'room.nbt')[1])
    fingerprint = digest(ROOT / 'room.nbt')
    counts = Counter(state.name for state in structure.blocks.values())
    checks = {}

    def check(label, condition, details=None):
        checks[label] = {'status': 'PASS' if condition else 'FAIL', 'details': details}

    def name(position):
        state = structure.blocks.get(tuple(position))
        return state.name if state else 'minecraft:air'

    def full(position):
        block = name(position)
        return block not in AIR and not block.endswith(('_stairs', '_slab', '_carpet', '_wall_banner', '_fence')) and block not in {'minecraft:chain', 'minecraft:lantern', 'minecraft:candle', 'minecraft:cobweb', 'minecraft:iron_bars'}

    check('reference_sha256', digest(REFERENCE) == REFERENCE_HASH, REFERENCE_HASH)
    check('gzip_single_palette_1_20_1', (ROOT / 'room.nbt').read_bytes()[:2] == b'\x1f\x8b' and tag['DataVersion'] == 3465 and 'palette' in tag and 'palettes' not in tag)
    check('dimensions_explicit_air_and_unique_coordinates', tuple(structure.size) == SIZE and len(tag['blocks']) == len(structure.blocks) == 12528, {'size': SIZE, 'records': len(tag['blocks'])})
    check('coordinates_in_bounds', all(all(0 <= coordinate < SIZE[axis] for axis, coordinate in enumerate(position)) for position in structure.blocks))
    forbidden = {'minecraft:command_block', 'minecraft:repeating_command_block', 'minecraft:chain_command_block', 'minecraft:structure_block', 'minecraft:jigsaw', 'minecraft:spawner', 'minecraft:lava', 'minecraft:water', 'minecraft:fire', 'minecraft:soul_fire', 'minecraft:powder_snow', 'minecraft:tnt', 'minecraft:gravel', 'minecraft:sand'}
    check('vanilla_palette_without_functional_hazards', all(block.startswith('minecraft:') and block not in forbidden for block in counts))
    check('no_template_entities', not tag.get('entities'))
    check('complete_double_floor_and_roof', all(full((block_x, height, block_z)) for block_x in range(27) for block_z in range(29) for height in (0, 1, 14, 15)))
    check('north_five_wide_five_high_port', all(name((block_x, height, block_z)) in AIR for block_x in range(11, 16) for height in range(2, 7) for block_z in (0, 1)))
    check('five_wide_spine_clear', all(full((block_x, 1, block_z)) and name((block_x, 2, block_z)) in AIR and name((block_x, 3, block_z)) in AIR for block_x in range(11, 16) for block_z in range(27)))
    reached = {(13, 2, 0)}
    pending = deque(reached)
    while pending:
        current = pending.popleft()
        for delta_x, delta_z in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            neighbor = (current[0] + delta_x, 2, current[2] + delta_z)
            if neighbor in reached or not (0 <= neighbor[0] < 27 and 0 <= neighbor[2] < 29):
                continue
            if full((neighbor[0], 1, neighbor[2])) and name(neighbor) in AIR and name((neighbor[0], 3, neighbor[2])) in AIR:
                reached.add(neighbor)
                pending.append(neighbor)
    goals = {(13, 2, 4), (13, 2, 24), (23, 2, 24), (4, 2, 24), (22, 2, 18)}
    check('bidirectional_routes_to_safepoints_shelves_and_barrels', goals <= reached, {'reachable_feet': len(reached), 'goals': sorted(goals)})
    unsupported = []
    hanging = []
    for position, state in structure.blocks.items():
        block_x, height, block_z = position
        properties = dict(state.properties)
        if state.name == 'minecraft:bookshelf' or state.name.endswith('_carpet') or state.name == 'minecraft:candle':
            if not full((block_x, height - 1, block_z)):
                unsupported.append(position)
        if state.name == 'minecraft:lantern':
            anchor_height = height + 1
            while name((block_x, anchor_height, block_z)) == 'minecraft:chain':
                anchor_height += 1
            valid = anchor_height == 13 and full((block_x, anchor_height, block_z))
            hanging.append({'position': position, 'anchor': [block_x, anchor_height, block_z], 'valid': valid})
        if state.name.endswith('_wall_banner'):
            delta = {'north': (0, 1), 'south': (0, -1), 'west': (1, 0), 'east': (-1, 0)}[properties['facing']]
            if not full((block_x + delta[0], height, block_z + delta[1])):
                unsupported.append(position)
    check('bookshelf_paper_candle_banner_support', not unsupported, unsupported)
    check('lantern_chain_beam_anchors', len(hanging) == 4 and all(entry['valid'] for entry in hanging), hanging)
    pillars = []
    for pillar_x, pillar_z in ((5, 7), (20, 7), (5, 20), (20, 20)):
        continuous = all(full((pillar_x + offset_x, height, pillar_z + offset_z)) for offset_x in (0, 1) for offset_z in (0, 1) for height in range(2, 15))
        pillars.append({'shaft_from': [pillar_x, 2, pillar_z], 'shaft_to': [pillar_x + 1, 14, pillar_z + 1], 'continuous': continuous})
    check('four_floor_to_roof_columns', all(entry['continuous'] for entry in pillars), pillars)
    structural = {position for position, state in structure.blocks.items() if state.name not in AIR and state.name not in {'minecraft:cobweb', 'minecraft:candle'} and not state.name.endswith(('_carpet', '_wall_banner'))}
    grounded = {position for position in structural if position[1] == 0}
    pending = deque(grounded)
    while pending:
        current = pending.popleft()
        for direction in DIRECTIONS:
            neighbor = tuple(current[axis] + direction[axis] for axis in range(3))
            if neighbor in structural and neighbor not in grounded:
                grounded.add(neighbor)
                pending.append(neighbor)
    check('all_structural_materials_contact_ground', grounded == structural, {'connected_blocks': len(grounded), 'unanchored': sorted(structural - grounded)})
    entities = []
    for position, data in structure.block_entities.items():
        value = plain(data)
        entities.append({'position': position, 'id': value.get('id'), 'items': value.get('Items', []), 'loot_table': value.get('LootTable')})
    check('two_empty_barrels_three_patterned_banners', len(entities) == 5 and sum(entry['id'] == 'minecraft:barrel' for entry in entities) == 2 and sum(entry['id'] == 'minecraft:banner' for entry in entities) == 3 and all(not entry['items'] and not entry['loot_table'] for entry in entities), entities)
    check('archive_theme_present', counts['minecraft:bookshelf'] == 32 and counts['minecraft:cobweb'] == 6 and counts['minecraft:lantern'] == 4 and counts['minecraft:light_blue_stained_glass'] == 70 and counts['minecraft:red_carpet'] >= 8, dict(counts))
    check('collapse_gap_kept_under_intact_roof', all(name((block_x, 7, block_z)) in AIR for block_x in (6, 7) for block_z in (13, 14, 15)) and all(full((block_x, 14, block_z)) for block_x in (6, 7) for block_z in (13, 14, 15)))
    metadata = {'id': 'zone-R05', 'zone_id': 'R05', 'name': '破碎档案厅', 'category': 'ruins', 'size': list(SIZE), 'entrance': [13, 2, 0], 'interior': [13, 2, 4], 'safe_point': [13, 2, 24], 'facing': 'north', 'floor_y': 1, 'feet_y': 2, 'entrance_width': 5, 'entrance_height': 5, 'main_walkway_width': 5, 'sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_HASH, 'minecraft_version': '1.20.1', 'data_version': 3465, 'room_type': 'independent_dead_end_hall', 'roof': 'complete_two_layers_y14_y15', 'containers': {'barrels': 2, 'prefilled_items': 0, 'loot_tables': 0}, 'integration_status': 'INDEPENDENT_DELIVERY_NOT_DEPLOYED', 'placement_requires_full_volume_occupancy_refusal': True, 'design_adaptations': ['Concept cutaway is visualization only; final NBT has full double roof.', 'Vanilla bookshelves have no lying block state; two low grounded bookcase remnants represent toppled shelves.', 'Loose books and paper are carpet block motifs, with no dropped item entities.', 'Torn textile impression uses three patterned red wall banners; no custom resource pack.', 'Collapsed gallery is a supported remnant; central five-wide route remains clear.']}
    dump(ROOT / 'room.json', metadata)
    failures = [label for label, result in checks.items() if result['status'] != 'PASS']
    report = {'status': 'PASS' if not failures else 'FAIL', 'id': 'zone-R05', 'sha256': fingerprint, 'checks': checks, 'failures': failures, 'claims': {'forge_runtime_tested': False, 'client_gameplay_tested': False, 'deployed': False}}
    dump(ROOT / 'qa/static-qa.json', report)
    dump(ROOT / 'qa/materials.json', {'sha256': fingerprint, 'counts': dict(sorted(counts.items()))})
    print(json.dumps({'status': report['status'], 'sha256': fingerprint, 'checks': len(checks), 'failures': failures}, ensure_ascii=False))
    if failures:
        raise SystemExit(1)


if __name__ == '__main__':
    main()
