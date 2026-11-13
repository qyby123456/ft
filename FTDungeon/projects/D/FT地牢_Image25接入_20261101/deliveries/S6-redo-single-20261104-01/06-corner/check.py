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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-06-corner.png')


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
    targets = [[7, 3, blockZ] for blockZ in [2, 5, 9, 13, 17, 21, 24]] + [[blockX, 3, 21] for blockX in [10, 13, 16, 19, 22, 25, 28]] + [[6, 3, 20], [8, 3, 22]]
    save(ROOT / 'room.json', {'id': 'S6-REDO-06', 'name': '熔岩黑石九十度转角走廊', 'batch': ROOT.parent.name, 'size': [29, 12, 29], 'facing': 'north', 'entrance': [7, 3, 0], 'interior': [7, 3, 2], 'connectors': [{'facing': 'north', 'position': [7, 3, 0], 'width': 3, 'height': 5, 'depth': 2}, {'facing': 'east', 'position': [28, 3, 21], 'width': 3, 'height': 5, 'depth': 2}], 'validation_targets': targets, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'coordinate_system': '+X east,+Y up,+Z south; positions are feet cells', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED', 'geometry': 'fresh L footprint, north approach and east exit, solid inner corner'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {point: state['Name'].removeprefix('minecraft:') for point, state in states.items()}
    assert data['size'] == [29, 12, 29] and data['DataVersion'] == 3465
    assert len(states) == len(data['blocks']) == 10092
    assert all(0 <= point[axis] < data['size'][axis] for point in states for axis in range(3))
    assert not data.get('entities') and not any(record.get('nbt') for record in data['blocks'])
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'chest', 'barrel', 'tnt'}
    def solid(point):
        return names.get(point) not in {None, 'air', 'lava', 'water', 'chain', 'lantern'}
    def walk(point):
        blockX, blockY, blockZ = point
        return names.get(point) == names.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    reached = {tuple(metadata['entrance'])}
    pending = collections.deque(reached)
    while pending:
        point = pending.popleft()
        for deltaX, deltaZ in [(1, 0), (-1, 0), (0, 1), (0, -1)]:
            following = (point[0] + deltaX, 3, point[2] + deltaZ)
            if following not in reached and walk(following):
                reached.add(following)
                pending.append(following)
    assert all(tuple(point) in reached for point in metadata['validation_targets'])
    assert all(walk((blockX, 3, blockZ)) for blockX in range(6, 9) for blockZ in range(23))
    assert all(walk((blockX, 3, blockZ)) for blockX in range(6, 29) for blockZ in range(20, 23))
    boundary = {point for point in reached if point[0] in [0, 28] or point[2] in [0, 28]}
    expected = {(blockX, 3, 0) for blockX in [6, 7, 8]} | {(28, 3, blockZ) for blockZ in [20, 21, 22]}
    assert boundary == expected, boundary
    assert all(solid((blockX, blockY, blockZ)) for blockX in range(12, 15) for blockZ in range(2, 17) for blockY in [3, 4]), 'Solid inside corner required'
    for port in metadata['connectors']:
        blockX, blockY, blockZ = port['position']
        for depth in range(2):
            for offset in [-1, 0, 1]:
                position = (blockX + offset, blockY, blockZ + depth) if port['facing'] == 'north' else (blockX - depth, blockY, blockZ + offset)
                assert solid((position[0], blockY - 1, position[2]))
                assert all(names[(position[0], blockY + rise, position[2])] == 'air' for rise in range(5))
    directions = [(1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)]
    molten = [point for point in names if names[point] == 'lava']
    for point in molten:
        assert states[point]['Properties']['level'] == '0'
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
    assert anchored == solids, 'Floating solid assembly'
    roof = [(blockX, blockZ) for blockX in range(29) for blockZ in range(29) if blockX <= 14 or blockZ >= 14]
    assert all(solid((blockX, 11, blockZ)) for blockX, blockZ in roof)
    assert len(molten) == 40
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'reachable_positions': len(reached), 'targets': metadata['validation_targets'], 'ports': metadata['connectors'], 'only_reachable_boundary_cells': sorted(boundary), 'genuine_L_3_wide': True, 'solid_inner_corner': True, 'roof_columns': len(roof), 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_six_neighbor_sealed': True, 'world_ticks': 'NOT_RUN'})
    print('PASS true L, two perpendicular ports, dry3-wide arms, solid inner corner, sealed40 lava and anchored roof', flush=True)


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
    data['blocks'] = [record for record in data['blocks'] if record['pos'][1] < 10 and not (record['pos'][1] >= 3 and (record['pos'][0] <= 2 or record['pos'][2] >= 26 or record['pos'][2] <= 1))]
    kind, payload = nbt_payload(data)
    cutaway = target / 'inspection-cutaway.nbt'
    cutaway.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(cutaway), 'removed_records': count - len(data['blocks']), 'operation': 'Remove y>=10 plus y>=3 with x<=2 or z>=26 or z<=1; no additions or state changes', 'purpose': 'inspection only; not placeable; west cut removes lava cells and their enclosure, final fluid validation applies only to full NBT'})
    detail = load_structure(ROOT / 'room.nbt')
    detail['size'] = [6, 9, 6]
    detail['blocks'] = [record for record in detail['blocks'] if 9 <= record['pos'][0] <= 14 and 1 <= record['pos'][1] <= 9 and 13 <= record['pos'][2] <= 18]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0] - 9, record['pos'][1] - 1, record['pos'][2] - 13]
    kind, payload = nbt_payload(detail)
    path = target / 'corner-niche-detail.nbt'
    path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(path), 'bounds_inclusive': [[9, 1, 13], [14, 9, 18]], 'subtract_origin': [9, 1, 13], 'size': detail['size'], 'operation': 'retain source cuboid and translate only; no additions/state changes', 'purpose': 'corner niche closeup only, not placeable'})
    print('Removal-only cutaway and exact source-crop corner detail ready', flush=True)


def duplicates():
    previous = module('signature_and_peers', ROOT.parent / '04-great-hall/check.py')
    previous.ROOT = ROOT
    previous.duplicates()


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
