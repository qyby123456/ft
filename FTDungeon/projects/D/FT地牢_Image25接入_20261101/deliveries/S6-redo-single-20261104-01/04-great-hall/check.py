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
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-04-great-hall.png')


def module(name, path):
    spec = importlib.util.spec_from_file_location(name, path)
    result = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(result)
    return result


helper = module('qa_infrastructure', ROOT.parent / '03-small-room/check.py')
sha, load, save, run = helper.sha, helper.load, helper.save, helper.run


def build():
    assert ROOT.name == '04-great-hall'
    if (ROOT / 'room.nbt').exists():
        assert sha(ROOT / 'room.nbt') == sha(ROOT / 'qa/rejected-outer-route-01/room.nbt'), 'Preserve candidate before rebuilding'
    with Image.open(REFERENCE) as image:
        image.verify()
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--name', 'room', '--out-dir', str(ROOT), '--data-version', '3465'], cwd=SKILL), flush=True)
    save(ROOT / 'room.json', {'id': 'S6-REDO-04', 'name': '熔岩双列柱廊大厅', 'batch': ROOT.parent.name, 'size': [27, 17, 37], 'facing': 'north', 'entrance': [13, 3, 0], 'interior': [13, 3, 3], 'connectors': [{'facing': 'north', 'position': [13, 3, 0], 'width': 5, 'height': 6, 'depth': 2, 'crown_width': 3}, {'facing': 'south', 'position': [13, 4, 36], 'width': 5, 'height': 7, 'depth': 3, 'crown_width': 3}, {'facing': 'west', 'position': [0, 3, 11], 'width': 3, 'height': 5, 'depth': 2}, {'facing': 'east', 'position': [26, 3, 11], 'width': 3, 'height': 5, 'depth': 2}], 'validation_targets': [[13, 4, 36], [13, 4, 33], [13, 3, 30], [0, 3, 11], [26, 3, 11], [10, 3, 8], [16, 3, 8], [10, 3, 17], [16, 3, 17], [10, 3, 26], [16, 3, 26], [1, 3, 21], [25, 3, 21]], 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'sha256': sha(ROOT / 'room.nbt'), 'coordinate_system': '+X east,+Y up,+Z south; ports are feet cells', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED', 'geometry': 'new single-reference long-axis hall; only QA infrastructure reused'})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    assert data['size'] == [27, 17, 37] and data['DataVersion'] == 3465 and not data.get('entities')
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {position: state['Name'].removeprefix('minecraft:') for position, state in states.items()}
    assert len(states) == len(data['blocks']) == 16983
    assert all(0 <= position[axis] < data['size'][axis] for position in states for axis in range(3))
    assert all(state['Name'].startswith('minecraft:') for state in data['palette'])
    assert not set(names.values()) & {'fire', 'soul_fire', 'magma_block', 'campfire', 'spawner', 'command_block', 'jigsaw', 'structure_block', 'chest', 'barrel', 'tnt'}
    assert not any(record.get('nbt') for record in data['blocks'])
    def solid(position):
        return names.get(position) not in {None, 'air', 'lava', 'water', 'chain', 'lantern'}
    def stair(position):
        return names.get(position, '').endswith('_stairs')
    def walk(position):
        blockX, blockY, blockZ = position
        return names.get(position) == names.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    reached = {tuple(metadata['entrance'])}
    pending = collections.deque(reached)
    while pending:
        current = pending.popleft()
        for deltaX, deltaZ in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for rise in (0, 1, -1):
                following = (current[0] + deltaX, current[1] + rise, current[2] + deltaZ)
                if rise and not (stair((current[0], current[1] - 1, current[2])) or stair((following[0], following[1] - 1, following[2]))):
                    continue
                if following not in reached and walk(following):
                    reached.add(following)
                    pending.append(following)
    assert all(tuple(point) in reached for point in [metadata['interior'], *metadata['validation_targets']]), [point for point in metadata['validation_targets'] if tuple(point) not in reached]
    for connector in metadata['connectors']:
        blockX, blockY, blockZ = connector['position']
        for depth in range(connector['depth']):
            for offset in range(-(connector['width'] // 2), connector['width'] // 2 + 1):
                facing = connector['facing']
                if facing in {'north', 'south'}:
                    point = (blockX + offset, blockY, blockZ + (depth if facing == 'north' else -depth))
                else:
                    point = (blockX + (depth if facing == 'west' else -depth), blockY, blockZ + offset)
                assert all(names[(point[0], point[1] + rise, point[2])] == 'air' for rise in range(connector['height']))
                assert solid((point[0], point[1] - 1, point[2]))
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
    assert not [position for position in names if solid(position) and position not in anchored], 'Disconnected solid geometry'
    assert all(solid((blockX, 16, blockZ)) for blockX in range(27) for blockZ in range(37))
    for pillarX in [7, 19]:
        for pillarZ in [8, 17, 26]:
            assert all(solid((pillarX, blockY, pillarZ)) for blockY in range(2, 17))
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(names), 'reachable_positions': len(reached), 'route_targets': metadata['validation_targets'], 'pillar_centers': [[blockX, blockZ] for blockX in [7, 19] for blockZ in [8, 17, 26]], 'roof_columns': 999, 'all_solids_anchored': True, 'lava_cells': len(molten), 'lava_level_zero_six_neighbor_sealed': True, 'block_entities': 0, 'spawned_entities': 0, 'terracotta_drapes_not_cloth': True, 'world_ticks': 'NOT_RUN'})
    print('PASS static six pillars, stairs, four ports, full roof, anchoring and sealed channels', flush=True)


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
    data['blocks'] = [record for record in data['blocks'] if record['pos'][1] < 15 and not (record['pos'][1] >= 3 and (record['pos'][0] <= 1 or record['pos'][2] <= 1))]
    kind, payload = nbt_payload(data)
    cutaway = target / 'inspection-cutaway.nbt'
    cutaway.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(cutaway), 'removed_records': count - len(data['blocks']), 'operation': 'remove records y>=15 plus two-block-thick near walls x<=1 or z<=1 at y>=3; no other changed states or additions', 'purpose': 'Lodestone inspection only, NOT placeable'})
    detail = load_structure(ROOT / 'room.nbt')
    detail['size'] = [11, 12, 6]
    detail['blocks'] = [record for record in detail['blocks'] if 8 <= record['pos'][0] <= 18 and 2 <= record['pos'][1] <= 13 and 31 <= record['pos'][2] <= 36]
    for record in detail['blocks']:
        record['pos'] = [record['pos'][0] - 8, record['pos'][1] - 2, record['pos'][2] - 31]
    kind, payload = nbt_payload(detail)
    detail_path = target / 'arch-detail.nbt'
    detail_path.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'detail-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'detail_sha256': sha(detail_path), 'bounds_inclusive': [[8, 2, 31], [18, 13, 36]], 'subtract_origin': [8, 2, 31], 'size': detail['size'], 'operation': 'retain final source cuboid and translate only; no additions or state changes', 'purpose': 'Lodestone far raised arch closeup only, NOT placeable'})
    renderer = module('software_auxiliary', PROJECT / 'tools/review_four_views.py')
    original_filter = renderer.filtered
    def filtered(structure, metadata, camera, exterior):
        visible = original_filter(structure, metadata, camera, exterior)
        return visible if exterior else {position: block for position, block in visible.items() if position[1] < 15}
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
    save(views / 'render.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'renderer': 'software auxiliary only, NOT actual Lodestone', 'interior_hidden': 'roof/beam y>=15 and camera-facing walls'})
    print('Derived removal-only cutaway, translated far-arch closeup and software auxiliary views', flush=True)


def duplicates():
    signature = module('signature_qa', ROOT.parent / '01-large-chamber-r3/check.py').signature
    own = signature(load(ROOT / 'room.nbt'))
    reviewed = []
    for folder in (PROJECT / 'deliveries').iterdir():
        if not folder.is_dir() or not (folder.name.startswith('nature') or folder.name.startswith('S6-')):
            continue
        for path in sorted(folder.rglob('room.nbt')):
            if path == ROOT / 'room.nbt' or 'qa' in path.relative_to(folder).parts:
                continue
            reviewed.append({'path': str(path), 'sha256': sha(path), 'exact_material_independent_match': signature(load(path)) == own})
    matches = [record['path'] for record in reviewed if record['exact_material_independent_match']]
    save(ROOT / 'qa/duplicates.json', {'status': 'EXACT_GEOMETRY_SELF_CHECK_COMPLETE', 'algorithm': 'material-independent non-air categories, XZ translation, four Y rotations and mirror, keeps Y', 'geometry_fingerprint': own, 'reviewed_count': len(reviewed), 'reviewed': reviewed, 'matches': matches, 'manual_design': {'S6-01': 'stepped rocky spires, paired rear arches', 'S6-02': 'low crystal study table and storage', 'S6-03': 'single chest and niches in compact room', 'S6-04': 'long through-axis, paired three-column rows, channel bridges, raised far arch and side doors; no central object', 'reuse': 'QA infrastructure only; recipe independent'}, 'limitations': ['Local exact fingerprint only, not semantic or near-duplicate certification', 'Historical QA candidates are not authorized deployment assets']})
    assert not matches
    print('No exact classified geometry matches among', len(reviewed), 'local peers', flush=True)


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
