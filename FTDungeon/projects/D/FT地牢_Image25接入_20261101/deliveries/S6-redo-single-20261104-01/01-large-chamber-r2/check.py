import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image

ROOT = Path(__file__).resolve().parent
PROJECT = ROOT.parents[2]
REFERENCE = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out/S6-01-large-chamber.png')
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
sys.path.insert(0, 'D:/ft工程文件/结构参考_20261103')
from analyze_structures import NbtReader


def save(path, content):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(content, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


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
    assert ROOT.name == '01-large-chamber-r2' and ROOT.parent.name == 'S6-redo-single-20261104-01'
    analysis = ROOT / 'analysis.md'
    assert analysis.is_file() and analysis.stat().st_mtime <= (ROOT / 'recipe.mjs').stat().st_mtime
    if (ROOT / 'room.nbt').exists():
        if (ROOT / 'qa/static.json').exists() or (ROOT / 'manifest.json').exists():
            raise RuntimeError('Refusing to rebuild validated delivery')
        import shutil
        rejected = ROOT / 'qa' / ('rejected-' + sha(ROOT / 'room.nbt')[:12])
        rejected.mkdir(parents=True, exist_ok=False)
        for name in ['room.nbt', 'room.json', 'summary.json']:
            if (ROOT / name).exists():
                shutil.copy2(ROOT / name, rejected / name)
        save(rejected / 'status.json', {'status': 'REJECTED_STATIC_SUPPORT_CHECK', 'candidate': False, 'reason': 'lava glass cover segments require edge supports; same-image repair only'})
    with Image.open(REFERENCE) as image:
        image.verify()
    print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'recipe.mjs'), '--name', 'room', '--out-dir', str(ROOT), '--data-version', '3465'], cwd=SKILL), flush=True)
    metadata = {'id': 'S6-REDO-01', 'name': '熔岩古殿·中央阶台', 'batch': 'S6-redo-single-20261104-01', 'size': [21, 13, 21], 'facing': 'north', 'entrance': [10, 2, 0], 'interior': [10, 2, 2], 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'sha256': sha(ROOT / 'room.nbt'), 'analysis_sha256': sha(analysis), 'recipe_sha256': sha(ROOT / 'recipe.mjs'), 'connectors': [{'facing': 'north', 'position': [10, 2, 0], 'width': 3, 'height': 3, 'evidence': 'restored cutaway front'}, {'facing': 'south', 'position': [5, 2, 20], 'width': 3, 'height': 3, 'evidence': 'visible rear-left arch'}, {'facing': 'south', 'position': [15, 2, 20], 'width': 3, 'height': 3, 'evidence': 'visible rear-right arch'}], 'validation_targets': [[5, 2, 20], [15, 2, 20], [5, 2, 10], [15, 2, 10], [10, 2, 5], [10, 2, 16], [10, 4, 7]], 'coordinate_system': '+X east, +Y up, +Z south; all connectors are player feet', 'source_primacy': 'single reference image; unpacked data compatibility only', 'new_recipe_no_archived_imports': True, 'acceptance': 'PENDING_COORDINATOR_REVIEW', 'next_reference_started': False, 'world_slot': None, 'deployment': 'NOT_PERFORMED'}
    metadata.update({'id': 'S6-REDO-01-R2', 'name': '熔岩古殿·收尖石峰修订', 'size': [29, 18, 29], 'revision': 'R2-NEEDS_REVISION-response', 'entrance': [14, 2, 0], 'interior': [14, 2, 2], 'connectors': [{'facing': 'north', 'position': [14, 2, 0], 'width': 3, 'height': 5}, {'facing': 'south', 'position': [7, 2, 28], 'width': 3, 'height': 5}, {'facing': 'south', 'position': [21, 2, 28], 'width': 3, 'height': 5}], 'validation_targets': [[7, 2, 28], [21, 2, 28], [8, 2, 14], [20, 2, 14], [14, 2, 8], [14, 2, 21], [14, 4, 11]], 'supersedes': '01-large-chamber reviewed NEEDS_REVISION; retained unchanged', 'scale_rationale': 'analysis.md door-height estimates, ratios and uncertainty; not single-chunk crossing as proof', 'new_recipe_no_archived_imports': False, 'revision_geometry': 'own same-image candidate revised; checker infrastructure reused, no cross-group topology import'})
    save(ROOT / 'room.json', metadata)


def static():
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    data = load(ROOT / 'room.nbt')
    assert data['DataVersion'] == 3465 and data['size'] == metadata['size'] and not data.get('entities')
    assert sha(ROOT / 'room.nbt') == metadata['sha256']
    palette = data['palette']
    states = {tuple(record['pos']): palette[record['state']] for record in data['blocks']}
    names = {position: state['Name'].removeprefix('minecraft:') for position, state in states.items()}
    assert len(states) == len(data['blocks']) == 29 * 18 * 29
    assert all(0 <= position[axis] < data['size'][axis] for position in states for axis in range(3))
    assert all(state['Name'].startswith('minecraft:') for state in palette)
    assert not set(names.values()) & {'spawner', 'jigsaw', 'structure_block', 'command_block', 'magma_block', 'fire', 'campfire', 'lever'}
    assert not any(record.get('nbt', {}).get('Items') or record.get('nbt', {}).get('LootTable') for record in data['blocks'])
    def solid(position):
        return names.get(position) not in {None, 'air', 'lava', 'water', 'chain', 'lantern'}
    def walk(position):
        blockX, blockY, blockZ = position
        return names.get(position) == 'air' and names.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    reached = {tuple(metadata['entrance'])}
    queue = collections.deque(reached)
    while queue:
        blockX, blockY, blockZ = queue.popleft()
        for deltaX, deltaZ in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for deltaY in (0, 1, -1):
                following = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
                if following not in reached and walk(following):
                    reached.add(following)
                    queue.append(following)
    assert tuple(metadata['interior']) in reached
    assert all(tuple(point) in reached for point in metadata['validation_targets'])
    for connector in metadata['connectors']:
        blockX, blockY, blockZ = connector['position']
        assert all(names[(blockX + offset, blockY + rise, blockZ)] == 'air' for offset in (-1, 0, 1) for rise in range(connector['height']))
        assert all(solid((blockX + offset, blockY - 1, blockZ)) for offset in (-1, 0, 1))
    molten = [position for position, name in names.items() if name == 'lava']
    for blockX, blockY, blockZ in molten:
        for deltaX, deltaY, deltaZ in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, 1, 0), (0, -1, 0)):
            adjacent = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
            assert solid(adjacent) or names.get(adjacent) == 'lava', ('unsealed lava', (blockX, blockY, blockZ), adjacent)
    lamps = [position for position, name in names.items() if name == 'lantern']
    assert len(lamps) == 4
    for blockX, blockY, blockZ in lamps:
        assert all(names[(blockX, rise, blockZ)] == 'chain' for rise in range(blockY + 1, 17))
        assert solid((blockX, 17, blockZ))
    assert all(solid((blockX, 17, blockZ)) for blockX in range(29) for blockZ in range(29))
    anchored = {position for position in states if position[1] == 0 and solid(position)}
    support_queue = collections.deque(anchored)
    while support_queue:
        blockX, blockY, blockZ = support_queue.popleft()
        for deltaX, deltaY, deltaZ in ((1, 0, 0), (-1, 0, 0), (0, 1, 0), (0, -1, 0), (0, 0, 1), (0, 0, -1)):
            neighbor = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
            if neighbor not in anchored and solid(neighbor):
                anchored.add(neighbor)
                support_queue.append(neighbor)
    unsupported = [position for position in states if solid(position) and position not in anchored]
    assert not unsupported, ('Disconnected solid component; horizontal arch/beam support is allowed', unsupported)
    allowed = {'air', 'basalt', 'blackstone', 'cracked_polished_blackstone_bricks', 'polished_blackstone', 'polished_blackstone_bricks', 'obsidian', 'glowstone', 'gilded_blackstone', 'lava', 'orange_stained_glass', 'polished_blackstone_brick_stairs', 'chain', 'lantern', 'shroomlight'}
    assert set(names.values()) <= allowed
    peak_layers = []
    for blockY in range(4, 11):
        layer = [position for position, name in names.items() if position[1] == blockY and 11 <= position[0] <= 18 and 12 <= position[2] <= 18 and name != 'air']
        peak_layers.append({'y': blockY, 'non_air_area': len(layer)})
    assert peak_layers[-1]['non_air_area'] == 1 and peak_layers[-2]['non_air_area'] < peak_layers[0]['non_air_area'] / 4
    save(ROOT / 'qa/stability-boundary.json', {'status': 'PASS_STATIC_BOUNDARY_ONLY', 'nbt_sha256': metadata['sha256'], 'full_palette_whitelist': sorted(set(names.values())), 'no_wood_leaves_wool_or_other_fire_spread_fuels_in_entire_volume': True, 'lava_six_neighbors_solid_or_same_fluid': True, 'outside_volume_open_lava': False, 'all_source_states_level_zero': all(states[point]['Properties']['level'] == '0' for point in molten), 'fire_exists': False, 'peak_layers': peak_layers, 'highest_peak_cells': 1, 'limitations': ['not a ServerLevel tick simulation', 'no Forge world placement, external flammable world neighbors, rotation placement or gameplay tests', 'caller must reject occupied/unsafe full volume and exterior interface neighbors before placement']})
    save(ROOT / 'materials.json', dict(sorted(collections.Counter(state['Name'] for state in states.values()).items())))
    save(ROOT / 'qa/static.json', {'status': 'PASS', 'sha256': metadata['sha256'], 'explicit_volume': len(states), 'lava_cells': len(molten), 'reachable_positions': len(reached), 'checks': ['full volume bounds and unique records', 'vanilla palette/no entities or forbidden blocks', 'all three 3x3 dry supported connectors', 'four-sided low circulation and altar stair point', 'six-direction lava enclosure', 'four chain-supported lanterns', 'complete top cover', 'low structure support'], 'forge_placement': 'NOT_RUN', 'forge_tick_stability': 'NOT_RUN'})
    print('PASS static: single structure and all declared connectors/landmarks')


def native():
    target = ROOT / 'qa'
    target.mkdir(exist_ok=True)
    dependencies = [line for line in Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join(['D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', *dependencies])
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    classes = target / 'classes'
    classes.mkdir(exist_ok=True)
    output = run([str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(classes), str(PROJECT / 'tools/VerifyDeliveries.java'), str(ROOT / 'CheckOne.java')], timeout=90)
    (target / 'native-compile.log').write_text(output, encoding='utf-8')
    output = run([str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, 'CheckOne', str(ROOT)], timeout=120, cwd=ROOT)
    (target / 'native-run.log').write_text(output, encoding='utf-8')
    print(output[-1300:], flush=True)


def signature(data):
    palette = data.get('palette', data.get('palettes', [[]])[0])
    cells = []
    for record in data['blocks']:
        name = palette[record['state']]['Name'].removeprefix('minecraft:')
        if name in {'air', 'cave_air', 'void_air', 'structure_void'}:
            continue
        category = 2 if name in {'water', 'lava', 'magma_block'} else 3 if name.endswith('_stairs') or name.endswith('_slab') else 4 if name in {'chain', 'lantern', 'soul_lantern'} else 1
        cells.append((*record['pos'], category))
    variants = []
    for mirror in (1, -1):
        for quarter in range(4):
            transformed = []
            for blockX, blockY, blockZ, category in cells:
                blockX *= mirror
                for unused in range(quarter):
                    blockX, blockZ = -blockZ, blockX
                transformed.append((blockX, blockY, blockZ, category))
            minimumX = min(point[0] for point in transformed)
            minimumZ = min(point[2] for point in transformed)
            variants.append(json.dumps(sorted((blockX - minimumX, blockY, blockZ - minimumZ, category) for blockX, blockY, blockZ, category in transformed), separators=(',', ':')))
    return hashlib.sha256(min(variants).encode()).hexdigest()


def duplicates():
    own = signature(load(ROOT / 'room.nbt'))
    reviewed = []
    folders = [folder for folder in ROOT.parents[1].iterdir() if folder.is_dir() and (folder.name.startswith('nature') or folder.name.startswith('S6-')) and folder != ROOT.parent]
    for folder in folders:
        for path in sorted(folder.rglob('room.nbt')):
            if any(part in {'qa', 'four-view-review', 'logs'} for part in path.relative_to(folder).parts):
                continue
            try:
                data = load(path)
                fingerprint = signature(data)
                reviewed.append({'path': str(path), 'sha256': sha(path), 'size': data['size'], 'geometry_fingerprint': fingerprint, 'exact_material_independent_match': own == fingerprint, 'historical_S6_not_candidate': folder.name == 'nature-batch1-S6'})
            except (ValueError, KeyError, AssertionError) as error:
                reviewed.append({'path': str(path), 'status': 'UNREADABLE', 'error': str(error)})
    save(ROOT / 'qa/duplicates.json', {'status': 'EXACT_GEOMETRY_SELF_CHECK_COMPLETE', 'algorithm': 'all non-air cells, material-independent classes, translation-normalized XZ, four Y rotations and mirror; preserves Y and partial/fluid categories but not exact collision geometry', 'geometry_fingerprint': own, 'reviewed_count': len(reviewed), 'matches': [record['path'] for record in reviewed if record.get('exact_material_independent_match')], 'reviewed': reviewed, 'limitations': ['No automatic near-duplicate, scaling, or semantic/topology uniqueness certification', 'Source screenshot itself intentionally matches its prior interpretation; old S6 is excluded from candidacy, not reused', 'Peer files sampled from locally available nature/S6 directories only; global final matrix pending'], 'manual_design': {'topology': 'central terrace, two rear arch destinations, all-four-side circulation', 'vertical': 'ground feet2, altar approach feet4, nonwalkable staggered spires', 'mechanisms': 'none shown in image, none added', 'pacing': 'two-sided approach and rear regroup only; not implemented combat waves', 'landmarks': 'asymmetric lava falls, three staggered peaks, corner glow belts, variable-height pendants', 'palette': 'dark blackstone/obsidian/basalt with sparse hot windows; palette alone is not distinctness'}})
    print('Duplicate self-check:', len(reviewed), 'peer structures;', sum(record.get('exact_material_independent_match', False) for record in reviewed), 'exact geometry matches')


def render():
    spec = importlib.util.spec_from_file_location('view_support', PROJECT / 'tools/review_four_views.py')
    renderer = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(renderer)
    original = renderer.filtered
    def filtered(structure, metadata, camera, exterior):
        visible = original(structure, metadata, camera, exterior)
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
            if not exterior and index == 0:
                image.save(ROOT / 'preview.png')
        sheet.save(views / f'{"exterior" if exterior else "interior"}-four-views.png')
    save(views / 'render.json', {'sha256': sha(ROOT / 'room.nbt'), 'source': 'software inspection only, NOT Lodestone screenshots', 'cutaway': 'roof, ceiling ribs and cells y>=15 hidden in interior only; full exterior and NBT unchanged'})
    print('Rendered one NBT, four internal directions and full exterior')


def package():
    import urllib.request
    metadata = json.loads((ROOT / 'room.json').read_text(encoding='utf-8'))
    native_report = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    assert native_report['status'] == 'PASS' and native_report['room']['sha256'] == metadata['sha256'] == sha(ROOT / 'room.nbt')
    assert json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))['status'] == 'PASS'
    assert len(native_report['native_collision_targets']) == 7
    duplicates_report = json.loads((ROOT / 'qa/duplicates.json').read_text(encoding='utf-8'))
    assert not duplicates_report['matches'], 'Exact geometry duplicate found; do not propose acceptance'
    url = 'http://127.0.0.1:5371/'
    with urllib.request.urlopen(url + '__minecraft_builder__/build.nbt', timeout=10) as response:
        assert hashlib.sha256(response.read()).hexdigest() == metadata['sha256']
    for path in [ROOT / 'preview.png', *sorted((ROOT / 'views').glob('*.png'))]:
        with Image.open(path) as image:
            image.verify()
    shots = json.loads((ROOT / 'lodestone/screenshots.json').read_text(encoding='utf-8'))
    assert shots['final_nbt_sha256'] == metadata['sha256']
    for shot in shots['screenshots']:
        assert sha(ROOT / 'lodestone' / shot['file']) == shot['sha256']
    files = [path for path in ROOT.rglob('*') if path.is_file() and path.suffix not in {'.class'} and 'collision-targets' not in path.parts and path.name != 'manifest.json']
    save(ROOT / 'manifest.json', {'batch': metadata['batch'], 'reference_count': 1, 'structure_count': 1, 'stage': 'READY_FOR_REGISTRATION_NOT_ACCEPTED', 'acceptance': 'PENDING_COORDINATOR_REVIEW', 'reference': str(REFERENCE), 'reference_sha256': sha(REFERENCE), 'nbt_sha256': metadata['sha256'], 'size': metadata['size'], 'lodestone_url': url + '?nbt=%2F__minecraft_builder__%2Fbuild.nbt&name=S6-REDO-01', 'lodestone_http_hash': 'PASS', 'static': 'PASS', 'native_load_collision_and_connectors': 'PASS', 'exact_geometry_self_check': 'no matches in reviewed local set; not global uniqueness certification', 'files': [{'path': str(path.relative_to(ROOT)), 'sha256': sha(path), 'bytes': path.stat().st_size} for path in files], 'next_image_started': False, 'forge_world_placement': 'NOT_RUN', 'forge_tick_stability': 'NOT_RUN', 'deployment': 'NOT_PERFORMED'})
    save(ROOT / 'registration.json', {'batch': metadata['batch'], 'revision': 'R2', 'current_reference': 'S6-01-large-chamber.png', 'current_module': str(ROOT / 'room.nbt'), 'manifest': str(ROOT / 'manifest.json'), 'sha256': metadata['sha256'], 'state': 'AWAITING_INDEPENDENT_REREVIEW', 'previous_review': 'NEEDS_REVISION', 'accepted': False, 'next_image_may_start': False, 'remaining_references_started': []})
    print('PASS: single NBT delivery ready for registration; acceptance pending, next reference NOT STARTED')


if __name__ == '__main__':
    {'build': build, 'static': static, 'native': native, 'duplicates': duplicates, 'render': render, 'package': package}[sys.argv[1]]()
