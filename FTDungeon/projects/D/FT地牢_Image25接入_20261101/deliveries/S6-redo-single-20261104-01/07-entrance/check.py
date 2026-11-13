import collections
import gzip
import importlib.util
import json
import os
from pathlib import Path
import sys

sys.dont_write_bytecode = True
ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-07-entrance.png')


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
    targets = [[13, 3, 2], [13, 4, 5], [13, 4, 6], [12, 5, 9], [13, 5, 9], [13, 5, 12], [13, 5, 18], [13, 5, 24], [13, 5, 28], [13, 5, 32], [6, 5, 22], [20, 5, 22], [6, 5, 23], [20, 5, 23], [5, 5, 12], [9, 5, 14]]
    save(ROOT / 'room.json', {'id': 'S6-REDO-07', 'name': '熔岩门楼与警戒前庭', 'batch': ROOT.parent.name, 'size': [27, 16, 33], 'facing': 'north', 'entrance': [13, 3, 0], 'interior': [13, 5, 12], 'connectors': [{'facing': 'north', 'position': [13, 3, 0], 'width': 3, 'height': 5, 'depth': 2}, {'facing': 'south', 'position': [13, 5, 32], 'width': 3, 'height': 5, 'depth': 2}], 'validation_targets': targets, 'door': {'type': 'minecraft:dark_oak_door', 'lower_halves': [[12, 5, 9], [13, 5, 9]], 'state': 'OPEN', 'opening_width': 2, 'interaction': 'NOT_RUN'}, 'stair_rises': [{'z': 4, 'feet_from': 3, 'feet_to': 4}, {'z': 7, 'feet_from': 4, 'feet_to': 5}], 'roof': 'OPEN_COURT; covered gate lintel, guard niches and rear passage only', 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'coordinate_system': '+X east,+Y up,+Z south; positions are feet cells', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED', 'geometry': 'fresh stepped entrance, paired physical doors, open court and two guard alcoves before rear arch'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {point: state['Name'].removeprefix('minecraft:') for point, state in states.items()}
    assert data['size'] == [27, 16, 33] and data['DataVersion'] == 3465
    assert len(states) == len(data['blocks']) == 14256
    assert all(0 <= point[axis] < data['size'][axis] for point in states for axis in range(3))
    assert not data.get('entities') and not any(record.get('nbt') for record in data['blocks'])
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'chest', 'barrel', 'tnt'}
    def solid(point):
        return names.get(point) not in {None, 'air', 'lava', 'water', 'chain', 'lantern', 'dark_oak_door'}
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
    assert anchored == solids, sorted(solids - anchored)[:20]
    for port in metadata['connectors']:
        blockX, blockY, blockZ = port['position']
        for depth in range(2):
            for offset in [-1, 0, 1]:
                position = (blockX + offset, blockY, blockZ + (depth if port['facing'] == 'north' else -depth))
                assert solid((position[0], blockY - 1, position[2]))
                assert all(names[(position[0], blockY + rise, position[2])] == 'air' for rise in range(5))
    doors = []
    for blockX in [12, 13]:
        for blockY, half in [(5, 'lower'), (6, 'upper')]:
            state = states[(blockX, blockY, 9)]
            assert state['Name'] == 'minecraft:dark_oak_door'
            assert state['Properties']['half'] == half and state['Properties']['open'] == 'true'
            assert state['Properties']['hinge'] == ('left' if blockX == 12 else 'right')
            doors.append({'position': [blockX, blockY, 9], 'state': state})
    for blockX in range(10, 17):
        for blockY, blockZ in [(3, 4), (4, 7)]:
            assert names[(blockX, blockY, blockZ)] == 'polished_blackstone_brick_stairs'
            assert states[(blockX, blockY, blockZ)]['Properties']['facing'] == 'south'
    assert all(names[(blockX, 5, blockZ)] == names[(blockX, 6, blockZ)] == 'air' for blockX in [12, 13] for blockZ in range(11, 33))
    assert all(solid((blockX, blockY, blockZ)) for blockX in [0, 26] for blockZ in range(8, 33) for blockY in [5, 6])
    assert all(names[(blockX, blockY, blockZ)] == 'air' for blockX in range(11, 16) for blockZ in range(13, 23) for blockY in range(5, 16)), 'Open court required'
    assert all(solid((blockX, 14, blockZ)) for blockX in range(10, 17) for blockZ in range(26, 33))
    assert len(molten) == 48
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'ports': metadata['connectors'], 'paired_open_doors': doors, 'stair_runs': metadata['stair_rises'], 'court': 'OPEN_SKY', 'rear_roof_columns': 49, 'side_boundaries': 'CLOSED', 'guard_niches': 2, 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_six_neighbor_sealed': True, 'world_ticks': 'NOT_RUN'})
    print('PASS paired doors, two real stair rises, open court, two alcoves, rear arch and sealed48 lava', flush=True)


def native():
    target = ROOT / 'qa'
    target.mkdir(exist_ok=True)
    dependencies = [line for line in Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join(['D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    classes = target / 'classes'
    classes.mkdir(exist_ok=True)
    output = run([str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(classes), str(ROOT / 'qa-source/VerifyDeliveries.java'), str(ROOT / 'CheckMedium.java'), str(ROOT / 'BodyRoutes.java')], timeout=90)
    (target / 'native-compile.log').write_text(output, encoding='utf-8')
    for executable, log in [('CheckMedium', 'native-run.log'), ('BodyRoutes', 'body-routes-run.log')]:
        output = run([str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, executable, str(ROOT)], timeout=120, cwd=ROOT)
        (target / log).write_text(output, encoding='utf-8')
        print(output[-1500:], flush=True)
    save(target / 'checker-correction.json', {'status': 'CHECKER_ADAPTED_TO_OPEN_DOOR_SHAPE', 'nbt_sha256_before_and_after': sha(ROOT / 'room.nbt'), 'initial_failure': 'Generic whole-cell clear() requires empty collision shape; an open physical door retains a side leaf and is therefore incorrectly treated as a blocked full cell. Initial check stopped before template/support/body checks.', 'correction': 'Only for native DoorBlock.OPEN, test its actual native shape against center 0.6-wide cell body instead of requiring the entire cell empty. Separate BodyRoutes still tests all native boxes with 0.6x1.8 body and 0.1 sweep sampling, including the two open leaves.', 'shared_source_sha256': sha(PROJECT / 'tools/VerifyDeliveries.java'), 'local_source_sha256': sha(ROOT / 'qa-source/VerifyDeliveries.java'), 'geometry_revisions': 0, 'shared_source_modified': False})


def derivatives():
    sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
    from structure_io import load_structure, nbt_payload
    target = ROOT / 'lodestone'
    target.mkdir(exist_ok=True)
    data = load_structure(ROOT / 'room.nbt')
    count = len(data['blocks'])
    data['blocks'] = [record for record in data['blocks'] if not (record['pos'][1] >= 10 or (record['pos'][1] >= 5 and (record['pos'][0] <= 2 or record['pos'][2] <= 10)))]
    kind, payload = nbt_payload(data)
    cutaway = target / 'inspection-cutaway.nbt'
    cutaway.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(cutaway), 'removed_records': count - len(data['blocks']), 'operation': 'Remove y>=10, or y>=5 with x<=2 or z<=10; no additions or state changes', 'purpose': 'inspection only; not placeable; front doors and some lava-window cells are removed by the cut, fluid/door checks apply to full NBT'})
    detail = load_structure(ROOT / 'room.nbt')
    detail['size'] = [9, 10, 8]
    detail['blocks'] = [record for record in detail['blocks'] if 9 <= record['pos'][0] <= 17 and 2 <= record['pos'][1] <= 11 and 4 <= record['pos'][2] <= 11]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0] - 9, record['pos'][1] - 2, record['pos'][2] - 4]
    kind, payload = nbt_payload(detail)
    path = target / 'gate-stairs-detail.nbt'
    path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(path), 'bounds_inclusive': [[9, 2, 4], [17, 11, 11]], 'subtract_origin': [9, 2, 4], 'size': detail['size'], 'operation': 'retain source cuboid and translate only; no additions/state changes', 'purpose': 'actual gate and two stair runs closeup only; not placeable'})
    print('Removal-only court cutaway and exact source-crop gate/stair detail ready', flush=True)


def duplicates():
    previous = module('signature_and_peers', ROOT.parent / '04-great-hall/check.py')
    previous.ROOT = ROOT
    previous.duplicates()


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
