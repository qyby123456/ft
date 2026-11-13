import collections
import copy
import gzip
import importlib.util
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-10-overview.png')
REGIONS = [
    {'id': 'entry', 'bounds_xz': [27, 0, 37, 14], 'roof_y': 9, 'target': [32, 3, 9]},
    {'id': 'court', 'bounds_xz': [20, 14, 44, 38], 'roof_y': None, 'target': [24, 3, 26]},
    {'id': 'west-medium', 'bounds_xz': [0, 24, 20, 46], 'roof_y': 11, 'target': [10, 3, 35]},
    {'id': 'west-pool', 'bounds_xz': [0, 48, 22, 72], 'roof_y': 13, 'target': [18, 3, 59]},
    {'id': 'great-hall', 'bounds_xz': [22, 42, 46, 72], 'roof_y': 16, 'target': [34, 3, 60]},
    {'id': 'treasure', 'bounds_xz': [46, 50, 64, 72], 'roof_y': 13, 'target': [55, 3, 65]},
    {'id': 'east-small', 'bounds_xz': [48, 14, 64, 30], 'roof_y': 9, 'target': [57, 3, 25]},
    {'id': 'L-corridor', 'bounds_xz': [42, 28, 60, 52], 'roof_y': 9, 'target': [57, 3, 43]}
]


def module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


helper = module('qa_infrastructure', ROOT.parent / '03-small-room/check.py')
sha, load, save, run = helper.sha, helper.load, helper.save, helper.run


def build():
    if (ROOT / 'room.nbt').exists():
        assert any(sha(path) == sha(ROOT / 'room.nbt') for path in (ROOT / 'qa').glob('draft-*/room.nbt')), 'Preserve prior candidate before rebuilding'
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--name', 'room', '--out-dir', str(ROOT), '--data-version', '3465'], cwd=SKILL), flush=True)
    targets = [region['target'] for region in REGIONS] + [[32, 3, 14], [20, 3, 32], [10, 3, 47], [10, 3, 51], [32, 3, 40], [32, 3, 44], [46, 3, 60], [44, 3, 35], [50, 3, 35], [57, 3, 35], [57, 3, 30], [57, 3, 49], [24, 3, 23], [39, 3, 23], [24, 3, 30], [39, 3, 30], [3, 3, 59], [18, 3, 67], [28, 3, 60], [40, 3, 60], [34, 5, 67], [5, 3, 35], [59, 3, 25], [51, 3, 61], [60, 3, 61]]
    save(ROOT / 'room.json', {'id': 'S6-REDO-10', 'name': '熔岩地牢整座总览', 'size': [65, 18, 73], 'facing': 'north', 'entrance': [32, 3, 0], 'interior': [32, 3, 9], 'connectors': [{'facing': 'north', 'position': [32, 3, 0], 'width': 3, 'height': 5, 'depth': 3}], 'regions': REGIONS, 'validation_targets': targets, 'containers': [{'position': [55, 3, 66], 'approach': [55, 3, 65]}, {'position': [5, 3, 36], 'approach': [5, 3, 35]}, {'position': [59, 3, 26], 'approach': [59, 3, 25]}], 'doors': [], 'door_policy': 'No door leaves in this reference reconstruction; entry is open arch, no closed-door/open-property analysis required', 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_origin': None, 'deployment': 'NOT_PERFORMED', 'coordinate_system': '+X east,+Y up,+Z south; feet cells', 'geometry': 'fresh complete multi-region source, no prior geometry reused'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {point: state['Name'].removeprefix('minecraft:') for point, state in states.items()}
    assert data['size'] == [65, 18, 73] and data['DataVersion'] == 3465
    assert len(states) == len(data['blocks']) == 85410 and not data.get('entities')
    assert all(0 <= point[axis] < data['size'][axis] for point in states for axis in range(3))
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'barrel', 'tnt'}
    assert not any(name.endswith('_door') for name in names.values())
    entities = [record for record in data['blocks'] if record.get('nbt')]
    assert len(entities) == 3 and all(record['nbt']['id'] == 'minecraft:chest' and not record['nbt'].get('Items') and not record['nbt'].get('LootTable') for record in entities)
    def solid(point):
        return names.get(point) not in {None, 'air', 'lava', 'water', 'chain', 'lantern', 'chest'}
    directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    molten = [point for point in names if names[point] == 'lava']
    for point in molten:
        assert states[point].get('Properties', {}).get('level', '0') == '0'
        assert all(solid(tuple(point[axis] + delta[axis] for axis in range(3))) or names.get(tuple(point[axis] + delta[axis] for axis in range(3))) == 'lava' for delta in directions), point
    solids = {point for point in names if solid(point)}
    anchored = {point for point in solids if point[1] == 0}
    pending = collections.deque(anchored)
    while pending:
        point = pending.popleft()
        for delta in directions:
            neighbor = tuple(point[axis] + delta[axis] for axis in range(3))
            if neighbor in solids and neighbor not in anchored:
                anchored.add(neighbor)
                pending.append(neighbor)
    assert anchored == solids, sorted(solids - anchored)[:20]
    assert all(names[(blockX, blockY, blockZ)] == 'air' for blockX in [31, 32, 33] for blockZ in range(3) for blockY in range(3, 8))
    assert all(names[(blockX, 3, blockZ)] == names[(blockX, 4, blockZ)] == 'air' for blockX in [56, 57, 58] for blockZ in range(28, 54))
    assert all(names[(blockX, 3, blockZ)] == names[(blockX, 4, blockZ)] == 'air' for blockX in range(42, 58) for blockZ in [34, 35, 36])
    assert names[(54, 3, 40)] != 'air', 'L inner wall prevents diagonal shortcut'
    roofs = []
    for region in REGIONS:
        if region['roof_y'] is None or region['id'] == 'L-corridor':
            continue
        leftX, frontZ, rightX, backZ = region['bounds_xz']
        points = [(blockX, blockZ) for blockX in range(leftX, rightX + 1) for blockZ in range(frontZ, backZ + 1)]
        assert all(solid((blockX, region['roof_y'] + 1, blockZ)) for blockX, blockZ in points), region['id']
        roofs.append({'region': region['id'], 'roof_y': region['roof_y'] + 1, 'columns': len(points)})
    assert all(names[(blockX, 17, blockZ)] == 'air' for blockX in range(22, 43) for blockZ in range(16, 37)), 'Court is intentionally open'
    for container in metadata['containers']:
        blockX, blockY, blockZ = container['position']
        assert solid((blockX, blockY - 1, blockZ))
        assert all(names[(blockX, blockY + rise, blockZ)] == 'air' for rise in [1, 2])
        assert container['approach'] in metadata['validation_targets']
    assert len(molten) == 179
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'regions': REGIONS, 'major_region_count': len(REGIONS), 'ports': metadata['connectors'], 'containers': metadata['containers'], 'roof_regions': roofs, 'court': 'OPEN_SKY', 'L_corridor_three_wide': True, 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_six_neighbor_sealed': True, 'door_count': 0, 'world_ticks': 'NOT_RUN'})
    print('PASS full multi-region geometry, real L, per-room roofs, three chests and sealed179 lava', flush=True)


def native():
    helper.ROOT = ROOT
    helper.native()


def write_nbt(data, path):
    sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
    from structure_io import nbt_payload
    kind, payload = nbt_payload(data)
    path.parent.mkdir(exist_ok=True, parents=True)
    path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))


def derivatives():
    source = load(ROOT / 'room.nbt')
    data = copy.deepcopy(source)
    data['blocks'] = [record for record in data['blocks'] if not (record['pos'][1] >= 8 or (record['pos'][1] >= 3 and (record['pos'][0] == 0 or record['pos'][2] in [14, 24, 42, 48, 50])))]
    path = ROOT / 'lodestone/inspection-cutaway.nbt'
    write_nbt(data, path)
    save(path.parent / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(path), 'removed_records': len(source['blocks']) - len(data['blocks']), 'operation': 'Delete y>=8 or y>=3 with x==0 or z in [14,24,42,48,50]; exact record deletion, no state/BE changes', 'purpose': 'overview floorplan inspection only; removes roofs, upper columns and some fluid boundary/attachments; not placeable'})
    detail = copy.deepcopy(source)
    detail['size'] = [19, 14, 23]
    detail['blocks'] = [record for record in detail['blocks'] if 46 <= record['pos'][0] <= 64 and 0 <= record['pos'][1] <= 13 and 50 <= record['pos'][2] <= 72]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0] - 46, record['pos'][1], record['pos'][2] - 50]
    path = ROOT / 'lodestone/treasure-detail.nbt'
    write_nbt(detail, path)
    save(path.parent / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(path), 'bounds_inclusive': [[46, 0, 50], [64, 13, 72]], 'subtract_origin': [46, 0, 50], 'size': detail['size'], 'operation': 'Exact source cuboid, record-position translation only; BE payloads unchanged', 'purpose': 'treasure region source crop only, not placeable'})


def shards():
    source = load(ROOT / 'room.nbt')
    expected = {tuple(record['pos']): {'state': source['palette'][record['state']], 'nbt': record.get('nbt')} for record in source['blocks']}
    reconstructed = {}
    entries = []
    for index, (leftX, rightX, frontZ, backZ) in enumerate([(0, 32, 0, 36), (33, 64, 0, 36), (0, 32, 37, 72), (33, 64, 37, 72)], 1):
        origin = [leftX, 0, frontZ]
        part = copy.deepcopy(source)
        part['size'] = [rightX - leftX + 1, 18, backZ - frontZ + 1]
        part['blocks'] = [record for record in part['blocks'] if leftX <= record['pos'][0] <= rightX and frontZ <= record['pos'][2] <= backZ]
        for record in part['blocks']:
            record['pos'] = [record['pos'][axis] - origin[axis] for axis in range(3)]
            if record.get('nbt'):
                for axis, key in enumerate(['x', 'y', 'z']):
                    if key in record['nbt']:
                        record['nbt'][key] -= origin[axis]
        path = ROOT / f'shards/part-{index:02d}.nbt'
        write_nbt(part, path)
        reread = load(path)
        assert max(reread['size']) <= 48
        assert len(reread['blocks']) == reread['size'][0] * reread['size'][1] * reread['size'][2]
        for record in reread['blocks']:
            position = tuple(record['pos'][axis] + origin[axis] for axis in range(3))
            payload = copy.deepcopy(record.get('nbt'))
            if payload:
                for axis, key in enumerate(['x', 'y', 'z']):
                    if key in payload:
                        payload[key] += origin[axis]
            assert position not in reconstructed
            reconstructed[position] = {'state': reread['palette'][record['state']], 'nbt': payload}
        entries.append({'order': index, 'file': path.relative_to(ROOT).as_posix(), 'origin': origin, 'size': reread['size'], 'sha256': sha(path), 'explicit_records': len(reread['blocks']), 'block_entities': sum(bool(record.get('nbt')) for record in reread['blocks'])})
    assert reconstructed == expected
    save(ROOT / 'shards/placement.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'world_origin': None, 'parts': entries, 'rotation': 'NONE', 'order': '01,02,03,04; relative origins added to same chosen world origin', 'air_policy': 'ALL explicit air MUST be placed; do not ignore air, do not independently fill/clear overlapping margins; disjoint cuboids partition entire source bounding box', 'block_entity_coordinate_policy': 'Localize any existing internal x/y/z on split and restore on reassembly; preserve every other tag', 'placement_runtime': 'NOT_RUN; seams may temporarily expose source lava or attachments; no claim that sequential live-tick placement is safe', 'deployment': 'NOT_PERFORMED'})
    save(ROOT / 'qa/reassembly.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'parts': entries, 'verified_coordinates': len(reconstructed), 'exact_block_states': True, 'exact_block_entity_payloads_after_coordinate_restore': True, 'no_overlap_no_omission': True, 'all_part_dimensions_le_48': True, 'source_entities': 0, 'scope': 'Read emitted gzip NBT parts and reconstruct all source states/BE by relative origin, including explicit air; mathematical equivalence only, no world placement/ticks'})
    print('PASS four <=48-axis parts reassembled exactly:', len(reconstructed), 'coordinates', flush=True)


def duplicates():
    previous = module('signature_and_peers', ROOT.parent / '04-great-hall/check.py')
    previous.ROOT = ROOT
    previous.duplicates()


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
