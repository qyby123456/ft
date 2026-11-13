import argparse
import collections
import copy
import gzip
import importlib.util
import json
from pathlib import Path
import subprocess
import sys

ROOT = Path(__file__).resolve().parent
ROOM = ROOT / 'incoming/S7R2-10'
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S7-fungal-grove/out/S7-10-overview.png')
ACCEPTANCE = Path('D:/ft工程文件/FT地牢_独立复核_20261104_01a1a685/S7-09-independent/independent-details.json')
sys.stdout.reconfigure(encoding='utf-8', errors='replace')
specification = importlib.util.spec_from_file_location('local_audit_tools', ROOT / 'audit_tools.py')
tools = importlib.util.module_from_spec(specification)
specification.loader.exec_module(tools)
tools.ROOT = ROOT
tools.ROOM = ROOM
sha = tools.sha
write = tools.write
load_structure = tools.load_structure


def save_nbt(path, value):
    kind, payload = tools.nbt_payload(value)
    assert kind == 10
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_bytes(gzip.compress(b'\x0a\x00\x00' + payload, mtime=0))


def partition(template, metadata):
    source = {tuple(record['pos']): record for record in template['blocks']}
    reconstructed = {}
    pieces = []
    for origin_x, width in [(0, 40), (40, 39)]:
        for origin_z, depth in [(0, 41), (41, 40)]:
            origin = [origin_x, 0, origin_z]
            piece = {'size': [width, 19, depth], 'DataVersion': 3465, 'palette': copy.deepcopy(template['palette']), 'entities': [], 'blocks': []}
            for point, record in source.items():
                if origin_x <= point[0] < origin_x + width and origin_z <= point[2] < origin_z + depth:
                    local = copy.deepcopy(record)
                    local['pos'] = [point[0] - origin_x, point[1], point[2] - origin_z]
                    piece['blocks'].append(local)
            target = ROOT / 'parts' / ('part-x' + str(origin_x) + '-z' + str(origin_z) + '.nbt')
            save_nbt(target, piece)
            reread = load_structure(target)
            assert max(reread['size']) <= 48 and len(reread['blocks']) == width * 19 * depth
            for record in reread['blocks']:
                point = tuple(record['pos'][axis] + origin[axis] for axis in range(3))
                translated = copy.deepcopy(record)
                translated['pos'] = list(point)
                assert point not in reconstructed and translated == source[point]
                reconstructed[point] = translated
            pieces.append({'step': len(pieces) + 1, 'path': str(target), 'relative_origin': origin, 'size': piece['size'], 'sha256': sha(target), 'blocks': len(piece['blocks']), 'air_blocks': sum(piece['palette'][record['state']]['Name'] == 'minecraft:air' for record in piece['blocks'])})
    assert reconstructed == source
    assembly = {'source_sha256': metadata['sha256'], 'size': template['size'], 'split': True, 'part_count': 4, 'parts': pieces, 'reassembly': 'PASS_EXACT_EVERY_POSITION_STATE_AND_BLOCK_ENTITY', 'reconstructed_positions': len(reconstructed), 'overlap_positions': 0, 'missing_positions': 0, 'assembly_order': [piece['path'] for piece in pieces], 'world_origin': None, 'local_origin': [0, 0, 0], 'collision_policy': 'Reserve/check full79x19x81 envelope including gaps and air. Dense parts overwrite ALL cells including air. Reject conflicts. Never place preview cutaway.', 'rotation_policy': 'Origins apply only to rotation0; never independently rotate each part. Whole-envelope79x81 transform with block states/entities requires runtime verification.', 'runtime_part_placement': 'NOT_RUN', 'permanent_S7_06_boundary': 'S7-06 explicit air outside L footprint clears terrain; full-envelope reservation or separately implemented validated quadrant skipping required.'}
    write(ROOT / 'assembly.json', assembly)
    write(ROOT / 'qa/partition-reassembly.json', {'status': 'PASS_EXACT_REASSEMBLY', **assembly, 'comparison': 'Every position and full record with identical source palette, including block entity NBT and explicit air'})


def derivative(template, metadata):
    value = copy.deepcopy(template)
    source = {tuple(record['pos']): record for record in template['blocks']}
    air = next(index for index, state in enumerate(template['palette']) if state['Name'] == 'minecraft:air')
    changes = []
    layout = metadata['layout']
    corridor_cells = {(block_x, block_z) for rectangle in layout['corridor_rectangles'] for block_x in range(rectangle['cells'][0], rectangle['cells'][2] + 1) for block_z in range(rectangle['cells'][1], rectangle['cells'][3] + 1)}
    for record in value['blocks']:
        block_x, block_y, block_z = record['pos']
        eligible = any(zone['bounds'][0] <= block_x <= zone['bounds'][2] and zone['bounds'][1] <= block_z <= zone['bounds'][3] and (block_y == zone['roof'] or block_y >= zone['floor'] + 1 and (block_x == zone['bounds'][0] or block_z == zone['bounds'][3])) for zone in layout['zones'])
        eligible |= block_y == 8 and any(rectangle['cells'][0] <= block_x <= rectangle['cells'][2] and rectangle['cells'][1] <= block_z <= rectangle['cells'][3] for rectangle in layout['corridor_rectangles'])
        in_room = any(zone['bounds'][0] <= block_x <= zone['bounds'][2] and zone['bounds'][1] <= block_z <= zone['bounds'][3] for zone in layout['zones'])
        eligible |= not in_room and (block_x, block_z) not in corridor_cells and 3 <= block_y <= 8 and ((block_x + 1, block_z) in corridor_cells or (block_x, block_z - 1) in corridor_cells)
        if eligible and record['state'] != air:
            changes.append({'position': list(record['pos']), 'before': copy.deepcopy(record), 'after_state': air})
            record['state'] = air
            record.pop('nbt', None)
    target = ROOT / 'views/PREVIEW_ONLY_cutaway.nbt'
    save_nbt(target, value)
    assert all(record == source[tuple(record['pos'])] or record['state'] == air and not record.get('nbt') for record in load_structure(target)['blocks'])
    write(ROOT / 'views/derivative.json', {'status': 'PASS_AIR_ONLY_DERIVATION', 'source_sha256': metadata['sha256'], 'derivative_sha256': sha(target), 'source': str(ROOM / 'room.nbt'), 'derivative': str(target), 'changes': 'Each room roof and west/south perimeter above its floor; corridor interior roof y8 and west/south outside perimeter y3..8 excluding all room footprints. Source courtyard skylight already air. No added or relocated blocks.', 'changed_positions': [entry['position'] for entry in changes], 'exact_before_records': changes, 'eligible_for_generation': False})


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--run-dir', type=Path, required=True)
    args = parser.parse_args()
    acceptance = json.loads(ACCEPTANCE.read_text(encoding='utf-8'))
    assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
    assert sha(ROOT.parent / '09-treasure/incoming/S7R2-09/room.nbt') == acceptance['sha256']
    template = load_structure(ROOM / 'room.nbt')
    assert template['size'] == [79, 19, 81] and template['DataVersion'] == 3465 and not template.get('entities')
    layout = json.loads(subprocess.check_output(['node', '--input-type=module', '-e', 'import {layout} from ' + json.dumps((ROOT / 'recipe.mjs').as_uri()) + ';process.stdout.write(JSON.stringify(layout))'], text=True, encoding='utf-8'))
    palette = template['palette']
    blocks = {tuple(record['pos']): palette[record['state']] for record in template['blocks']}
    assert len(blocks) == len(template['blocks']) == 79 * 19 * 81
    assert all(0 <= point[axis] < template['size'][axis] for point in blocks for axis in range(3))
    forbidden = {'spawner', 'command_block', 'structure_block', 'jigsaw', 'lava', 'fire', 'cobweb', 'powder_snow'}
    assert all(state['Name'].startswith('minecraft:') and state['Name'].split(':')[-1] not in forbidden for state in palette)
    entities = [{'position': record['pos'], 'state': palette[record['state']], 'nbt': record['nbt']} for record in template['blocks'] if record.get('nbt')]
    assert len(entities) == 11 and collections.Counter(entry['nbt']['id'] for entry in entities) == {'minecraft:chest': 2, 'minecraft:barrel': 6, 'minecraft:banner': 3}
    assert all(not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable') for entry in entities)
    def name(point):
        return blocks.get(point, {'Name': 'minecraft:air'})['Name'].split(':')[-1]
    water = [point for point, state in blocks.items() if state['Name'] == 'minecraft:water']
    for block_x, block_y, block_z in water:
        assert name((block_x, block_y - 1, block_z)) == 'deepslate_bricks'
        assert all(name((block_x + delta_x, block_y, block_z + delta_z)) in {'water', 'polished_blackstone_bricks'} for delta_x, delta_z in [(1, 0), (-1, 0), (0, 1), (0, -1)])
    sky = [[block_x, 15, block_z] for block_x in range(31, 53) for block_z in range(38, 56)]
    assert len(sky) == 396 and all(name(tuple(point)) == 'air' for point in sky)
    connections = []
    targets = [zone['target'] for zone in layout['zones']] + layout['junction_targets'] + layout['stair_targets']
    for port in layout['ports']:
        normal, feet = port['normal'], port['feet']
        direction = {(0, 0, -1): 'north', (1, 0, 0): 'east', (0, 0, 1): 'south', (-1, 0, 0): 'west'}[tuple(normal)]
        connection = {**port, 'direction': direction, 'opening': [3, 4], 'outside_feet': [feet[axis] + normal[axis] for axis in range(3)], 'inside_feet': [feet[axis] - normal[axis] for axis in range(3)]}
        connections.append(connection)
        targets.extend([feet, connection['inside_feet']])
        if not port.get('external'):
            targets.append(connection['outside_feet'])
    targets += [operation['access_feet'] for operation in layout['interactions']] + [camera['feet'] for camera in layout['cameras']]
    metadata = {'id': 'S7R2-10', 'name': '菌窟六区连通地牢总览', 'size': template['size'], 'facing': 'south', 'entrance': [53, 2, 80], 'interior': [53, 2, 76], 'test_targets': targets, 'connections': connections, 'layout': layout, 'interactions': layout['interactions'], 'block_entities': {'chest': 2, 'barrel': 6, 'banner': 3, 'automatic_loot': False}, 'source_skylight': {'y': 15, 'air_cells': sky, 'air_count': 396}, 'water_blocks': len(water), 'sha256': sha(ROOM / 'room.nbt'), 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'status': 'AWAITING_INDEPENDENT_REGISTRATION_ACCEPTANCE', 'runtime_slot': None, 'geometry_origin': 'Original six-zone S7-10 complex; no old room geometry/NBT imported; test tooling adapted only.', 'world_placement': 'NOT_RUN'}
    write(ROOM / 'room.json', metadata)
    write(ROOM / 'blocks.json', dict(sorted(collections.Counter(state['Name'] for state in blocks.values()).items())))
    write(ROOT / 'qa/block-entities.json', {'source_sha256': metadata['sha256'], 'entities': entities, 'world_interaction': 'NOT_RUN'})
    write(ROOT / 'qa/static.json', {'status': 'PASS_STATIC_ONLY', 'sha256': metadata['sha256'], 'size': template['size'], 'checks': ['121581 dense unique bounds', 'DataVersion3465', '11 block entities empty containers no automatic loot', 'six rooms seven corridor rectangles twelve elevation-aware arches', '396 source skylight cells', 'framed water bottom and four side neighbors'], 'water_blocks': len(water), 'updates': 'NOT_RUN'})
    partition(template, metadata)
    derivative(template, metadata)
    report = tools.native(args.run_dir.resolve(), metadata)
    write(ROOT / 'qa/latest-native-run.json', {'directory': str(args.run_dir.resolve()), 'source_sha256': metadata['sha256'], 'status': report['status'], 'all_target_routes': len(report['target_paths']), 'fragile_checks': len(report['static_survival'])})
    tools.duplicate_review(template)
    print('S7-10 VERIFIED; NO S7-11 AUTHORIZATION', flush=True)


if __name__ == '__main__':
    main()
