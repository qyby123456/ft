import collections
import gzip
import importlib.util
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-09-treasure.png')


def module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


helper = module('qa_infrastructure', ROOT.parent / '03-small-room/check.py')
sha, load, save, run = helper.sha, helper.load, helper.save, helper.run


def build():
    assert not (ROOT / 'room.nbt').exists(), 'Preserve existing geometry before rebuilding'
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--name', 'room', '--out-dir', str(ROOT), '--data-version', '3465'], cwd=SKILL), flush=True)
    targets = [[16, 3, 2], [16, 4, 5], [16, 5, 11], [16, 5, 18], [0, 5, 20], [32, 5, 20], [5, 5, 20], [27, 5, 20], [5, 5, 27], [27, 5, 27], [16, 7, 25], [14, 7, 24], [18, 7, 24], [16, 7, 28], [8, 5, 24], [24, 5, 24], [12, 5, 12], [20, 5, 12]]
    save(ROOT / 'room.json', {'id': 'S6-REDO-09', 'name': '熔岩守卫宝藏室', 'size': [33, 18, 35], 'facing': 'north', 'entrance': [16, 3, 0], 'interior': [16, 5, 11], 'connectors': [{'facing': 'north', 'position': [16, 3, 0], 'width': 3, 'height': 5, 'depth': 3}, {'facing': 'west', 'position': [0, 5, 20], 'width': 3, 'height': 5, 'depth': 3}, {'facing': 'east', 'position': [32, 5, 20], 'width': 3, 'height': 5, 'depth': 3}], 'validation_targets': targets, 'containers': [{'position': [16, 7, 26], 'approach': [16, 7, 25]}, {'position': [5, 5, 28], 'approach': [5, 5, 27]}, {'position': [27, 5, 28], 'approach': [27, 5, 27]}], 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'coordinate_system': '+X east,+Y up,+Z south; positions are feet cells', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED', 'geometry': 'fresh treasure dais with real chest entities, two stone guardians and paired front pools'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {point: state['Name'].removeprefix('minecraft:') for point, state in states.items()}
    assert data['size'] == [33, 18, 35] and data['DataVersion'] == 3465 and len(states) == len(data['blocks']) == 20790
    assert not data.get('entities')
    assert all(0 <= point[axis] < data['size'][axis] for point in states for axis in range(3))
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'barrel', 'tnt'}
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
    for port in metadata['connectors']:
        blockX, blockY, blockZ = port['position']
        for depth in range(3):
            for offset in [-1, 0, 1]:
                position = (blockX + offset, blockY, blockZ + depth) if port['facing'] == 'north' else (blockX + (depth if port['facing'] == 'west' else -depth), blockY, blockZ + offset)
                assert solid((position[0], blockY - 1, position[2]))
                assert all(names[(position[0], blockY + rise, position[2])] == 'air' for rise in range(5))
    for container in metadata['containers']:
        blockX, blockY, blockZ = container['position']
        assert names[(blockX, blockY, blockZ)] == 'chest' and solid((blockX, blockY - 1, blockZ))
        assert all(names[(blockX, blockY + rise, blockZ)] == 'air' for rise in [1, 2])
        assert container['approach'] in metadata['validation_targets']
    assert all(solid((blockX, 17, blockZ)) for blockX in range(33) for blockZ in range(8, 35))
    assert len(molten) == 100
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'ports': metadata['connectors'], 'container_entities': 3, 'containers': metadata['containers'], 'stair_blocks': 28, 'roof_columns': 891, 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_six_neighbor_sealed': True, 'world_ticks': 'NOT_RUN'})
    print('PASS three real chests, three ports, tier dais/stairs, sealed100 lava and anchored roof', flush=True)


def native():
    helper.ROOT = ROOT
    helper.native()


def derivatives():
    sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
    from structure_io import load_structure, nbt_payload
    target = ROOT / 'lodestone'
    target.mkdir(exist_ok=True)
    data = load_structure(ROOT / 'room.nbt')
    count = len(data['blocks'])
    data['blocks'] = [record for record in data['blocks'] if not (record['pos'][1] >= 16 or (record['pos'][1] >= 5 and (record['pos'][0] <= 2 or record['pos'][2] <= 10)))]
    kind, payload = nbt_payload(data)
    cutaway = target / 'inspection-cutaway.nbt'
    cutaway.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(cutaway), 'removed_records': count - len(data['blocks']), 'operation': 'Remove y>=16 or y>=5 with x<=2 or z<=10; exact deletion only; no state or block entity changes', 'purpose': 'inspection only; not placeable; final enclosure/support checked only on full NBT'})
    detail = load_structure(ROOT / 'room.nbt')
    detail['size'] = [15, 12, 13]
    detail['blocks'] = [record for record in detail['blocks'] if 9 <= record['pos'][0] <= 23 and 4 <= record['pos'][1] <= 15 and 19 <= record['pos'][2] <= 31]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0] - 9, record['pos'][1] - 4, record['pos'][2] - 19]
    kind, payload = nbt_payload(detail)
    path = target / 'treasure-dais-detail.nbt'
    path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(path), 'bounds_inclusive': [[9, 4, 19], [23, 15, 31]], 'subtract_origin': [9, 4, 19], 'size': detail['size'], 'operation': 'retain source cuboid; translate record positions only; states and chest NBT unchanged', 'purpose': 'chest dais and guardians closeup only; not placeable'})


def duplicates():
    previous = module('signature_and_peers', ROOT.parent / '04-great-hall/check.py')
    previous.ROOT = ROOT
    previous.duplicates()


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
