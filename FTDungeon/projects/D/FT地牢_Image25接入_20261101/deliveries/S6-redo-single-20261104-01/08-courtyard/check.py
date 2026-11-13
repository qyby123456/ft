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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-08-courtyard.png')


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
    targets = [[18, 3, 36], [18, 3, 6], [18, 3, 30], [11, 3, 18], [25, 3, 18], [11, 3, 11], [25, 3, 25], [5, 3, 6], [31, 3, 6], [5, 13, 18], [31, 13, 18], [5, 13, 26], [31, 13, 26], [18, 13, 5], [18, 13, 31], [11, 13, 6], [25, 13, 30]]
    save(ROOT / 'room.json', {'id': 'S6-REDO-08', 'name': '熔岩泉双层天井庭院', 'batch': ROOT.parent.name, 'size': [37, 23, 37], 'facing': 'north', 'entrance': [18, 3, 0], 'interior': [18, 3, 6], 'connectors': [{'facing': 'north', 'position': [18, 3, 0], 'width': 3, 'height': 5, 'depth': 3}, {'facing': 'south', 'position': [18, 3, 36], 'width': 3, 'height': 5, 'depth': 3}], 'validation_targets': targets, 'gallery_feet_y': 13, 'stair_runs': [{'x': [4, 6], 'z': [8, 17], 'feet_from': 3, 'feet_to': 13}, {'x': [30, 32], 'z': [8, 17], 'feet_from': 3, 'feet_to': 13}], 'skylight': {'x': [10, 26], 'z': [10, 26]}, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'coordinate_system': '+X east,+Y up,+Z south; positions are feet cells', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED', 'geometry': 'fresh tier fountain and two-layer gallery court, north/south ports, dual real stairs'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {point: state['Name'].removeprefix('minecraft:') for point, state in states.items()}
    assert data['size'] == [37, 23, 37] and data['DataVersion'] == 3465
    assert len(states) == len(data['blocks']) == 31487
    assert all(0 <= point[axis] < data['size'][axis] for point in states for axis in range(3))
    assert not data.get('entities') and not any(record.get('nbt') for record in data['blocks'])
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'chest', 'barrel', 'tnt'}
    def solid(point):
        return names.get(point) not in {None, 'air', 'lava', 'water', 'chain', 'lantern', 'crimson_roots'}
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
                position = (blockX + offset, blockY, blockZ + (depth if port['facing'] == 'north' else -depth))
                assert solid((position[0], blockY - 1, position[2]))
                assert all(names[(position[0], blockY + rise, position[2])] == 'air' for rise in range(5))
    for blockX in [11, 12, 24, 25]:
        assert all(names[(blockX, blockY, blockZ)] == 'air' for blockZ in range(11, 26) for blockY in [3, 4])
    assert all(names[(blockX, 22, blockZ)] == 'air' for blockX in range(10, 27) for blockZ in range(10, 27))
    roof = [(blockX, blockZ) for blockX in range(37) for blockZ in range(37) if blockX <= 9 or blockX >= 27 or blockZ <= 9 or blockZ >= 27]
    assert all(solid((blockX, 22, blockZ)) for blockX, blockZ in roof)
    assert sum(name == 'polished_blackstone_brick_stairs' for name in names.values()) == 60
    assert len(molten) == 152
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'ports': metadata['connectors'], 'ground_dry_ring': True, 'stair_blocks': 60, 'stair_runs': metadata['stair_runs'], 'upper_gallery_feet_y': 13, 'skylight_columns': 289, 'peripheral_roof_columns': len(roof), 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_six_neighbor_sealed': True, 'world_ticks': 'NOT_RUN'})
    print('PASS tier fountain, two stairs/60 blocks, upper gallery, 289-column skylight and sealed152 lava', flush=True)


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
    data['blocks'] = [record for record in data['blocks'] if not (record['pos'][1] >= 21 or (record['pos'][1] >= 3 and (record['pos'][0] <= 2 or record['pos'][2] <= 2)))]
    kind, payload = nbt_payload(data)
    cutaway = target / 'inspection-cutaway.nbt'
    cutaway.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(cutaway), 'removed_records': count - len(data['blocks']), 'operation': 'Remove y>=21 or y>=3 with x<=2 or z<=2; no additions or state changes', 'purpose': 'inspection only; not placeable; removes some lava-window cells/anchors, final support and enclosure checked only on full NBT'})
    detail = load_structure(ROOT / 'room.nbt')
    detail['size'] = [13, 15, 13]
    detail['blocks'] = [record for record in detail['blocks'] if 12 <= record['pos'][0] <= 24 and 1 <= record['pos'][1] <= 15 and 12 <= record['pos'][2] <= 24]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0] - 12, record['pos'][1] - 1, record['pos'][2] - 12]
    kind, payload = nbt_payload(detail)
    path = target / 'fountain-detail.nbt'
    path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(path), 'bounds_inclusive': [[12, 1, 12], [24, 15, 24]], 'subtract_origin': [12, 1, 12], 'size': detail['size'], 'operation': 'retain source cuboid and translate only; no additions/state changes', 'purpose': 'tier fountain closeup only; not placeable'})


def duplicates():
    previous = module('signature_and_peers', ROOT.parent / '04-great-hall/check.py')
    previous.ROOT = ROOT
    previous.duplicates()


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
