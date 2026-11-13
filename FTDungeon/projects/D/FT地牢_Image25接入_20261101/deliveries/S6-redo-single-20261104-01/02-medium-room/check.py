import collections
import gzip
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

sys.dont_write_bytecode = True

from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-02-medium-room.png')
sys.path.insert(0, 'D:/ft工程文件/结构参考_20261103')
from analyze_structures import NbtReader


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def save(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def load(path):
    reader = NbtReader(path.read_bytes())
    assert reader.number('>B') == 10
    reader.string()
    return reader.value(10)


def run(command, **options):
    result = subprocess.run(command, capture_output=True, encoding='utf-8', errors='replace', creationflags=subprocess.CREATE_NO_WINDOW, **options)
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout + result.stderr


def build():
    assert ROOT.name == '02-medium-room'
    if (ROOT / 'room.nbt').exists():
        archived = ROOT / 'qa/rejected-fire-buffer-02/room.nbt'
        assert archived.exists() and sha(archived) == sha(ROOT / 'room.nbt'), 'Preserve existing candidate before rebuild'
    accepted = json.loads((ROOT.parent / '01-large-chamber-r3/manifest.json').read_text(encoding='utf-8'))
    assert sha(ROOT.parent / '01-large-chamber-r3/room.nbt') == accepted['nbt_sha256']
    with Image.open(REFERENCE) as image:
        image.verify()
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--name', 'room', '--out-dir', str(ROOT), '--data-version', '3465'], cwd=SKILL), flush=True)
    targets = [[17, 3, 24], [8, 3, 11], [16, 3, 11], [12, 3, 8], [12, 3, 15], [21, 3, 8], [21, 3, 6], [21, 3, 11], [6, 3, 21]]
    save(ROOT / 'room.json', {'id': 'S6-REDO-02', 'name': '熔岩书房·紫晶石桌', 'batch': ROOT.parent.name, 'size': [25, 15, 25], 'facing': 'north', 'entrance': [12, 3, 0], 'interior': [12, 3, 2], 'connectors': [{'facing': 'north', 'position': [12, 3, 0], 'width': 3, 'height': 5}, {'facing': 'south', 'position': [17, 3, 24], 'width': 3, 'height': 5, 'depth': 2}], 'validation_targets': targets, 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'sha256': sha(ROOT / 'room.nbt'), 'analysis_sha256': sha(ROOT / 'analysis.md'), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'coordinate_system': '+X east, +Y up, +Z south; connector positions are player feet', 'source_primacy': 'single S6-02 image; original geometry, no S6-01 recipe import', 'acceptance': 'PENDING_INDEPENDENT_REVIEW', 'next_reference_started': False, 'deployment': 'NOT_PERFORMED', 'world_slot': None})


def static():
    data = load(ROOT / 'room.nbt')
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    assert data['size'] == [25, 15, 25] and data['DataVersion'] == 3465
    assert not data.get('entities') and sha(ROOT / 'room.nbt') == metadata['sha256']
    states = {tuple(record['pos']): data['palette'][record['state']] for record in data['blocks']}
    names = {position: state['Name'].removeprefix('minecraft:') for position, state in states.items()}
    assert len(states) == len(data['blocks']) == 25 * 15 * 25
    assert all(0 <= position[axis] < data['size'][axis] for position in states for axis in range(3))
    assert all(state['Name'].startswith('minecraft:') for state in data['palette'])
    assert not set(names.values()) & {'spawner', 'jigsaw', 'structure_block', 'command_block', 'magma_block', 'fire', 'campfire', 'lever', 'tnt'}
    assert not any(record.get('nbt', {}).get('Items') or record.get('nbt', {}).get('LootTable') for record in data['blocks'])
    def solid(position):
        return names.get(position) not in {None, 'air', 'lava', 'water', 'lantern', 'chain', 'amethyst_cluster', 'small_amethyst_bud'}
    def walk(position):
        blockX, blockY, blockZ = position
        return names.get(position) == 'air' and names.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    reached = {tuple(metadata['entrance'])}
    queue = collections.deque(reached)
    while queue:
        blockX, blockY, blockZ = queue.popleft()
        for deltaX, deltaZ in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            following = (blockX + deltaX, blockY, blockZ + deltaZ)
            if following not in reached and walk(following):
                reached.add(following)
                queue.append(following)
    assert all(tuple(point) in reached for point in [metadata['interior'], *metadata['validation_targets']])
    for connector in metadata['connectors']:
        blockX, blockY, blockZ = connector['position']
        for depthZ in range(blockZ - connector.get('depth', 1) + 1, blockZ + 1):
            assert all(names[(blockX + offset, blockY + rise, depthZ)] == 'air' for offset in (-1, 0, 1) for rise in range(5))
            assert all(solid((blockX + offset, blockY - 1, depthZ)) for offset in (-1, 0, 1))
    molten = [position for position, name in names.items() if name == 'lava']
    directions = ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1))
    for position in molten:
        assert states[position]['Properties']['level'] == '0'
        for delta in directions:
            neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
            assert solid(neighbor) or names.get(neighbor) == 'lava', ('unsealed lava', position, neighbor)
    anchored = {position for position in states if position[1] == 0 and solid(position)}
    queue = collections.deque(anchored)
    while queue:
        position = queue.popleft()
        for delta in directions:
            neighbor = tuple(position[axis] + delta[axis] for axis in range(3))
            if neighbor not in anchored and solid(neighbor):
                anchored.add(neighbor)
                queue.append(neighbor)
    assert not [position for position in states if solid(position) and position not in anchored], 'Disconnected solids'
    assert all(solid((blockX, 14, blockZ)) for blockX in range(25) for blockZ in range(25))
    assert names[(11, 5, 11)] == 'amethyst_cluster' and names[(14, 5, 11)] == 'small_amethyst_bud'
    assert sum(name == 'bookshelf' for name in names.values()) == 9
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(names.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': sha(ROOT / 'room.nbt'), 'explicit_volume': len(states), 'reachable_floor_positions': len(reached), 'route_targets': metadata['validation_targets'], 'lava_cells': len(molten), 'lava_six_neighbors_sealed': True, 'all_lava_level_zero': True, 'solid_attachment_graph_to_base': 'PASS', 'full_roof': True, 'entities': 0, 'empty_containers': 2, 'fire_safety': 'native FireBlock odds and position buffers evaluated separately; bookshelf IS flammable', 'world_placement': 'NOT_RUN', 'fluid_random_ticks': 'NOT_RUN'})
    print('PASS static room, routes, attachments, sealed lava', flush=True)


def native():
    target = ROOT / 'qa'
    target.mkdir(exist_ok=True)
    dependencies = [line for line in Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join(['D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    classes = target / 'classes'
    classes.mkdir(exist_ok=True)
    output = run([str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(classes), str(PROJECT / 'tools/VerifyDeliveries.java'), str(ROOT / 'CheckMedium.java')], timeout=90)
    (target / 'native-compile.log').write_text(output, encoding='utf-8')
    output = run([str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, 'CheckMedium', str(ROOT)], timeout=120, cwd=ROOT)
    (target / 'native-run.log').write_text(output, encoding='utf-8')
    print(output[-1500:], flush=True)


def derivatives():
    sys.path.insert(0, 'D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')
    from structure_io import load_structure, nbt_payload
    data = load_structure(ROOT / 'room.nbt')
    original_count = len(data['blocks'])
    data['blocks'] = [record for record in data['blocks'] if record['pos'][1] < 12 and not (record['pos'][1] >= 3 and (record['pos'][0] == 0 or record['pos'][2] == 0))]
    target = ROOT / 'lodestone'
    target.mkdir(exist_ok=True)
    kind, payload = nbt_payload(data)
    derived = target / 'inspection-cutaway.nbt'
    derived.write_bytes(gzip.compress(bytes([kind]) + b'\x00\x00' + payload, mtime=0))
    save(target / 'cutaway-provenance.json', {'final_nbt_sha256': sha(ROOT / 'room.nbt'), 'cutaway_nbt_sha256': sha(derived), 'removed_records': original_count - len(data['blocks']), 'operation': 'remove records y>=12 plus west/front boundaries x0 or z0 at y>=3; no additions or changed states; final NBT unchanged', 'purpose': 'Lodestone inspection only, not placeable delivery'})
    spec = importlib.util.spec_from_file_location('renderer', PROJECT / 'tools/review_four_views.py')
    renderer = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(renderer)
    original_filter = renderer.filtered
    def filtered(structure, metadata, camera, exterior):
        visible = original_filter(structure, metadata, camera, exterior)
        return visible if exterior else {position: block for position, block in visible.items() if position[1] < 12}
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
    save(views / 'render.json', {'source_nbt_sha256': sha(ROOT / 'room.nbt'), 'renderer': 'software auxiliary only; NOT Lodestone', 'interior_only_hidden': 'roof and upper cornices y>=12 plus camera-facing boundary walls'})
    print('Cutaway and auxiliary four-view sheets ready', flush=True)


def duplicates():
    spec = importlib.util.spec_from_file_location('previous_qa', ROOT.parent / '01-large-chamber-r3/check.py')
    helper = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(helper)
    own = helper.signature(load(ROOT / 'room.nbt'))
    reviewed = []
    for folder in PROJECT.joinpath('deliveries').iterdir():
        if not folder.is_dir() or not (folder.name.startswith('nature') or folder.name.startswith('S6-')):
            continue
        for peer in sorted(folder.rglob('room.nbt')):
            if peer == ROOT / 'room.nbt' or 'qa' in peer.relative_to(folder).parts:
                continue
            fingerprint = helper.signature(load(peer))
            reviewed.append({'path': str(peer), 'sha256': sha(peer), 'exact_material_independent_match': fingerprint == own})
    save(ROOT / 'qa/duplicates.json', {'status': 'EXACT_GEOMETRY_SELF_CHECK_COMPLETE', 'algorithm': 'non-air material-independent categories, XZ translation, four Y rotations and mirror; retains Y', 'geometry_fingerprint': own, 'reviewed_count': len(reviewed), 'matches': [record['path'] for record in reviewed if record['exact_material_independent_match']], 'reviewed': reviewed, 'manual_design': {'S6-01': 'stepped spires, paired rear arches', 'S6-02': 'flat low table with crystals, storage corner, one offset rear portal, side niche', 'mechanisms': 'none', 'reuse': 'QA signature function reused; geometry independently written from S6-02'}, 'limitations': ['Not semantic/near-duplicate proof', 'Only locally available peer files', 'Historical candidates are not authorized for delivery']})
    assert not any(record['exact_material_independent_match'] for record in reviewed)
    print('No exact category-geometry matches among', len(reviewed), 'peers', flush=True)


if __name__ == '__main__':
    for action in sys.argv[1:]:
        globals()[action]()
