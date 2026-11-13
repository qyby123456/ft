import collections
import gzip
import importlib.util
import json
from pathlib import Path
import sys

sys.dont_write_bytecode = True
from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-05-corridor.png')


def module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


helper = module('qa_infrastructure', ROOT.parent / '03-small-room/check.py')
sha, load, save, run = helper.sha, helper.load, helper.save, helper.run


def build():
    assert ROOT.name == '05-corridor'
    assert not (ROOT / 'room.nbt').exists(), 'Preserve any existing candidate before rebuilding'
    with Image.open(REFERENCE) as image:
        image.verify()
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--name', 'room', '--out-dir', str(ROOT), '--data-version', '3465'], cwd=SKILL), flush=True)
    save(ROOT / 'room.json', {'id': 'S6-REDO-05', 'name': '熔岩低顶直通走廊', 'batch': ROOT.parent.name, 'size': [17, 12, 35], 'facing': 'north', 'entrance': [8, 3, 0], 'interior': [8, 3, 2], 'connectors': [{'facing': 'north', 'position': [8, 3, 0], 'width': 3, 'height': 5, 'depth': 2, 'crown_width': 1}, {'facing': 'south', 'position': [8, 3, 34], 'width': 3, 'height': 5, 'depth': 2, 'crown_width': 1}], 'validation_targets': [[8, 3, 34], *[[8, 3, blockZ] for blockZ in [5, 9, 13, 17, 21, 25, 29]], *[[blockX, 3, blockZ] for blockZ in [9, 17, 25] for blockX in [6, 10]]], 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'coordinate_system': '+X east,+Y up,+Z south; ports are feet cells', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED', 'geometry': 'new single-reference straight low corridor; QA infrastructure reused only'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    assert data['size'] == [17, 12, 35] and data['DataVersion'] == 3465 and not data.get('entities')
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {position: state['Name'].removeprefix('minecraft:') for position, state in states.items()}
    assert len(states) == len(data['blocks']) == 7140
    assert all(0 <= position[axis] < data['size'][axis] for position in states for axis in range(3))
    assert all(state['Name'].startswith('minecraft:') for state in data['palette'])
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'chest', 'barrel', 'tnt'}
    assert not any(record.get('nbt') for record in data['blocks'])
    def solid(position):
        return names.get(position) not in {None, 'air', 'lava', 'water', 'chain', 'lantern'}
    def walk(position):
        blockX, blockY, blockZ = position
        return names.get(position) == names.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    reached = {tuple(metadata['entrance'])}
    pending = collections.deque(reached)
    while pending:
        current = pending.popleft()
        for deltaX, deltaZ in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            following = (current[0] + deltaX, current[1], current[2] + deltaZ)
            if following not in reached and walk(following):
                reached.add(following)
                pending.append(following)
    assert all(tuple(point) in reached for point in [metadata['interior'], *metadata['validation_targets']])
    assert all(walk((blockX, 3, blockZ)) for blockX in [7, 8, 9] for blockZ in range(35))
    for connector in metadata['connectors']:
        blockX, blockY, blockZ = connector['position']
        for depth in range(2):
            tunnelZ = blockZ + (depth if connector['facing'] == 'north' else -depth)
            assert all(names[(blockX + offset, blockY + rise, tunnelZ)] == 'air' for offset in [-1, 0, 1] for rise in range(5))
            assert all(solid((blockX + offset, blockY - 1, tunnelZ)) for offset in [-1, 0, 1])
    assert all(solid((wallX, blockY, blockZ)) for wallX in [0, 16] for blockY in [3, 4] for blockZ in range(35)), 'Unexpected side exit'
    directions = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
    molten = [position for position, name in names.items() if name == 'lava']
    for position in molten:
        assert states[position]['Properties']['level'] == '0'
        for delta in directions:
            neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
            assert solid(neighbor) or names.get(neighbor) == 'lava', ('open lava', position, neighbor)
    anchored = {position for position in names if position[1] == 0 and solid(position)}
    pending = collections.deque(anchored)
    while pending:
        current = pending.popleft()
        for delta in directions:
            neighbor = tuple(current[axis] + delta[axis] for axis in range(3))
            if neighbor not in anchored and solid(neighbor):
                anchored.add(neighbor)
                pending.append(neighbor)
    assert not [position for position in names if solid(position) and position not in anchored], 'Disconnected solids'
    assert all(solid((blockX, 11, blockZ)) for blockX in range(17) for blockZ in range(35))
    for blockX in [6, 10]:
        for blockZ in [5, 13, 21, 29]:
            assert names[(blockX, 7, blockZ)] == 'lantern'
            assert all(names[(blockX, blockY, blockZ)] == 'chain' for blockY in [8, 9]) and solid((blockX, 10, blockZ))
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'reachable_positions': len(reached), 'route_targets': metadata['validation_targets'], 'roof_columns': 595, 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_level_zero_six_neighbor_sealed': True, 'unbroken_three_wide_dry_center': True, 'side_door_count': 0, 'end_ports': 2, 'chain_supported_lanterns': 8, 'block_entities': 0, 'spawned_entities': 0, 'world_ticks': 'NOT_RUN'})
    print('PASS static straight corridor, two end ports, uninterrupted3-wide aisle, roof and sealed lava', flush=True)


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
    data['blocks'] = [record for record in data['blocks'] if record['pos'][1] < 10 and not (record['pos'][1] >= 3 and (record['pos'][0] == 0 or record['pos'][2] <= 1))]
    kind, payload = nbt_payload(data)
    cutaway = target / 'inspection-cutaway.nbt'
    cutaway.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(cutaway), 'removed_records': count - len(data['blocks']), 'operation': 'remove y>=10 roof/beams plus x0 or z<=1 near walls at y>=3; no additions or state changes', 'purpose': 'Lodestone inspection only; NOT placeable'})
    detail = load_structure(ROOT / 'room.nbt')
    detail['size'] = [4, 10, 5]
    detail['blocks'] = [record for record in detail['blocks'] if 0 <= record['pos'][0] <= 3 and 1 <= record['pos'][1] <= 10 and 6 <= record['pos'][2] <= 10]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0], record['pos'][1] - 1, record['pos'][2] - 6]
    kind, payload = nbt_payload(detail)
    detail_path = target / 'lava-window-detail.nbt'
    detail_path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(detail_path), 'bounds_inclusive': [[0, 1, 6], [3, 10, 10]], 'subtract_origin': [0, 1, 6], 'size': detail['size'], 'operation': 'retain final cuboid, translate positions only; no additions/state changes', 'purpose': 'actual Lodestone sealed lava window closeup, NOT placeable'})
    renderer = module('software_auxiliary', PROJECT / 'tools/review_four_views.py')
    original_filter = renderer.filtered
    def filtered(structure, metadata, camera, exterior):
        visible = original_filter(structure, metadata, camera, exterior)
        return visible if exterior else {position: block for position, block in visible.items() if position[1] < 10}
    renderer.filtered = filtered
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    structure = renderer.Structure.read(ROOT / 'room.nbt')
    views = ROOT / 'views'
    views.mkdir(exist_ok=True)
    for exterior in (False, True):
        sheet = Image.new('RGB', (2400, 1800), '#101a20')
        for index, (direction, title, camera) in enumerate(renderer.VIEWS):
            image = renderer.render(structure, metadata, camera, title, exterior, size=(1200, 900))
            image.save(views / f'{"exterior" if exterior else "interior"}-{direction}.png')
            sheet.paste(image, ((index % 2) * 1200, (index // 2) * 900))
        sheet.save(views / f'{"exterior" if exterior else "interior"}-four-views.png')
    save(views / 'render.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'renderer': 'software auxiliary only, NOT actual Lodestone', 'interior_hidden': 'roof/beam y>=10 and camera-facing walls'})
    print('Removal-only cutaway, translated lava window detail and auxiliary views ready', flush=True)


def duplicates():
    previous_qa = module('signature_and_peers', ROOT.parent / '04-great-hall/check.py')
    previous_qa.ROOT = ROOT
    previous_qa.duplicates()
    path = ROOT / 'qa/duplicates.json'
    report = json.loads(path.read_text(encoding='utf-8'))
    report['manual_design']['S6-05'] = 'flat straight low corridor; four column bays and sealed falls; two ends only, no branch, no drapes, no platform or central object'
    save(path, report)


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
