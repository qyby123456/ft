import collections
import hashlib
import importlib.util
import json
import os
from pathlib import Path
import subprocess
import sys

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
PROJECT = ROOT.parents[1]
SKILL = Path('C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill')
REFERENCES = Path('D:/ft工程文件/image2.5/nature-dungeon-styles-20261104/S6-volcanic-cavern/out')
LIBRARY = Path('D:/ft工程文件/结构参考_20261103')
sys.path.insert(0, str(Path('D:/ft工程文件/血棘祭炉祭坛厅地牢_20261101/tools')))
from structure_io import load_structure

ROOMS = [
    ('large-chamber', '熔核大殿'), ('medium-room', '玄武岩议事室'),
    ('small-room', '余烬密室'), ('great-hall', '黑曜石柱廊大厅'),
    ('corridor', '熔脉直廊'), ('corner', '玄武岩折廊'),
    ('entrance', '熔岩门庭'), ('courtyard', '火山天井'), ('treasure', '烬金祭坛宝库'),
]
DESIGNS = [
    {'topology': 'longitudinal solid reactor spine splits west/east routes with reunion behind core; north to east/west branches', 'vertical': 'floor feet2; monumental core to y9, non-walkable landmark', 'landmark': 'gilded inlet face and alternating hot inspection slots', 'pacing': 'entry reveal -> side cover choice -> flank core -> regroup at rear', 'mechanism': 'sealed lava visual core; no automatic reactor logic', 'waypoints': [[9, 2, 12], [27, 2, 12], [18, 2, 21]], 'blocked': [[18, 2, 6], [18, 2, 18]]},
    {'topology': 'west-edge dogleg around transverse divider, then side-route to east exit', 'vertical': 'feet2 main route with low central plinth', 'landmark': 'western lit end of blocking bulkhead', 'pacing': 'lost sightline -> left turn -> chamber reveal', 'mechanism': 'fixed sightline obstruction, not a moving door', 'waypoints': [[2, 2, 6], [12, 2, 10]], 'blocked': [[7, 2, 5]]},
    {'topology': 'single-ended maintenance bay, no through-route', 'vertical': 'feet2; valve lever at y3', 'landmark': 'manual thermal beacon switch next to sealed lava inspection strip', 'pacing': 'quiet service stop -> optional switch -> backtrack', 'mechanism': 'real vanilla lever on redstone lamp, initially unpowered; tick test pending', 'waypoints': [], 'blocked': []},
    {'topology': 'three alternating W/E/W gap baffles force lateral zigzag despite north/south external ports', 'vertical': 'feet2; baffles y2..5 allow visibility above but not straight passage', 'landmark': 'lit alternating baffle ends', 'pacing': 'three separated sightline/reposition beats, then south release', 'mechanism': 'static cover; no spawned waves or trigger claims', 'waypoints': [[5, 2, 5], [24, 2, 8], [5, 2, 11]], 'blocked': [[14, 2, 4], [14, 2, 7], [14, 2, 10]]},
    {'topology': 'single mandatory elevated kiln crossing; no ground-level bypass', 'vertical': 'feet2 -> stairs3/4 -> deck5 -> stairs4/3 -> feet2', 'landmark': 'raised central heat-inspection deck over enclosed channels', 'pacing': 'compress on ascent -> elevated exposed span -> descent release', 'mechanism': 'passive grade separation, no timed lava or piston motion', 'waypoints': [[4, 5, 10]], 'blocked': [[4, 2, 10]]},
    {'topology': 'north/east elbow with solid blind inner quadrant; external elbow is shared connector vocabulary, not uniqueness claim', 'vertical': 'feet2, no elevation trick', 'landmark': 'two offset ember lamps mark blind turn', 'pacing': 'blind 90-degree corner; transition connector only', 'mechanism': 'none', 'waypoints': [[7, 2, 7]], 'blocked': [[3, 2, 3]]},
    {'topology': 'two-ended threshold with optional side valve alcove, not core combat room', 'vertical': 'feet2; high gilded portal lintel', 'landmark': 'manual unpowered thermal beacon under double basalt uprights', 'pacing': 'arrival and inspection before reactor zone', 'mechanism': 'real vanilla lever on lamp; no lockout or prerequisite imposed', 'waypoints': [], 'blocked': []},
    {'topology': 'ground bypass around well plus two optional stepped side inspection ledges', 'vertical': 'ground feet2, side stairs to feet5, open central skylight', 'landmark': 'well core framed by paired elevated inspection ledges', 'pacing': 'open sky respite -> choose low path or raised observation -> descend', 'mechanism': 'passive dual-height traversal, no teleport or falling trap', 'waypoints': [[4, 5, 7], [10, 5, 7]], 'blocked': [[7, 2, 7]]},
    {'topology': 'single-ended treasure bay with centerline blast baffle and bilateral approach to back altar', 'vertical': 'feet2 approaches, altar platform with chest at y4', 'landmark': 'paired hot baffle corners and gilded rear altar', 'pacing': 'blocked direct sightline -> choose flank -> reveal final empty chest -> return', 'mechanism': 'no reward distribution, no concealed moving trap', 'waypoints': [[2, 2, 6], [12, 2, 6]], 'blocked': [[7, 2, 5]]},
]


def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def run(command, **options):
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', creationflags=subprocess.CREATE_NO_WINDOW, **options)
    if result.returncode:
        raise RuntimeError(result.stdout + result.stderr)
    return result.stdout + result.stderr


def audit(template, metadata):
    palette = template['palette']
    blocks = {tuple(record['pos']): palette[record['state']]['Name'].removeprefix('minecraft:') for record in template['blocks']}
    assert len(blocks) == len(template['blocks'])
    assert template['DataVersion'] == 3465 and not template.get('entities')
    assert all(state['Name'].startswith('minecraft:') for state in palette)
    assert not set(blocks.values()) & {'spawner', 'command_block', 'structure_block', 'jigsaw', 'fire', 'magma_block'}
    width, height, depth = template['size']
    assert len(blocks) == width * height * depth
    assert all(0 <= point[axis] < template['size'][axis] for point in blocks for axis in range(3))
    for record in template['blocks']:
        assert not record.get('nbt', {}).get('Items') and not record.get('nbt', {}).get('LootTable')
    def solid(point):
        return point in blocks and blocks[point] not in {'air', 'lava', 'water'}
    def walk(point):
        blockX, blockY, blockZ = point
        return blocks.get(point) == 'air' and blocks.get((blockX, blockY + 1, blockZ)) == 'air' and solid((blockX, blockY - 1, blockZ))
    start = tuple(metadata['entrance'])
    reached = {start}
    pending = collections.deque([start])
    while pending:
        blockX, blockY, blockZ = pending.popleft()
        for deltaX, deltaZ in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            for deltaY in (0, 1, -1):
                point = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
                if point not in reached and walk(point):
                    reached.add(point)
                    pending.append(point)
    assert walk(start) and tuple(metadata['interior']) in reached
    for waypoint in metadata['distinctive_design']['waypoints']:
        assert tuple(waypoint) in reached, (metadata['id'], 'unreachable design waypoint', waypoint)
    for blocked in metadata['distinctive_design']['blocked']:
        assert not walk(tuple(blocked)), (metadata['id'], 'missing route barrier', blocked)
    rotations = []
    for quarter in range(4):
        transformed = []
        for blockX, blockY, blockZ in reached:
            for rotation in range(quarter):
                blockX, blockZ = -blockZ, blockX
            transformed.append((blockX, blockY, blockZ))
        minimum = [min(point[axis] for point in transformed) for axis in range(3)]
        rotations.append(json.dumps(sorted(tuple(point[axis] - minimum[axis] for axis in range(3)) for point in transformed), separators=(',', ':')))
    route_fingerprint = hashlib.sha256(min(rotations).encode()).hexdigest()
    save(ROOT / 'incoming' / metadata['id'] / 'topology.json', {'nbt_sha256': metadata['sha256'], 'route_fingerprint': route_fingerprint, 'algorithm': 'reachable integer feet positions, translation and Y-quarter-rotation canonicalized, material names excluded; stair-aware native target validation is separate', 'reachable_feet_positions': sorted(reached), 'reachable_foot_heights': sorted({point[1] for point in reached}), 'design': metadata['distinctive_design'], 'limitation': 'Exact lattice fingerprint only; unequal hash does NOT prove semantic uniqueness or eliminate scaled/mirrored near-duplicates'})
    for connector in metadata['connectors']:
        blockX, blockY, blockZ = connector['position']
        assert (blockX, blockY, blockZ) in reached
        for horizontal in (-1, 0, 1):
            for vertical in range(3):
                point = (blockX + (horizontal if connector['facing'] in ('north', 'south') else 0), blockY + vertical, blockZ + (horizontal if connector['facing'] in ('east', 'west') else 0))
                assert blocks[point] == 'air', (metadata['id'], 'blocked connector', point)
    lava = [point for point, block in blocks.items() if block == 'lava']
    for blockX, blockY, blockZ in lava:
        for deltaX, deltaY, deltaZ in ((1, 0, 0), (-1, 0, 0), (0, 0, 1), (0, 0, -1), (0, 1, 0), (0, -1, 0)):
            adjacent = (blockX + deltaX, blockY + deltaY, blockZ + deltaZ)
            assert solid(adjacent) or blocks.get(adjacent) == 'lava', ('unsealed lava', blockX, blockY, blockZ)
    for point, name in blocks.items():
        if name == 'chest':
            assert solid((point[0], point[1] - 1, point[2])) and blocks[(point[0], point[1] + 1, point[2])] == 'air'
    if metadata['kind'] != 'courtyard':
        assert all(solid((blockX, height - 1, blockZ)) for blockX in range(width) for blockZ in range(depth))
    return {'status': 'PASS', 'checks': ['bounds', 'unique_coordinates', 'full_explicit_volume', 'vanilla_namespace', 'no_entities_or_rewards', 'three_by_three_connectors', 'all_connectors_reachable', 'distinctive_waypoints_reachable', 'distinctive_route_barriers_present', 'safe_point_reachable', 'lava_sealed_on_all_six_sides', 'chest_support_and_lid_clearance', 'complete_roof_except_courtyard'], 'reachable_feet_positions': len(reached), 'sealed_lava_cells': len(lava), 'native_minecraft': 'see qa/native-review.json', 'forge_world_placement': 'NOT_RUN', 'forge_tick_stability': 'NOT_RUN', 'source_image_fidelity': 'adapted, not exact reconstruction'}


def build():
    sys.path.insert(0, str(LIBRARY))
    from analyze_structures import NbtReader
    source_paths = ['keep', 'halls/room/armory_md_side_0', 'halls/room/storeroom_sm_0', 'halls/hall_straight', 'halls/hall_straight', 'halls/hall_turn_left', 'battle_bridge/battle_bridge_gate', 'keep/keep_well', 'halls/room/worship_md_l_0']
    changes = [
        'Retain actual keep XZ footprint 37x25; reduce 32-high multilevel runtime to 12-high single floor. Three original horizontal branch roles retained as explicit N/E/W connectors, not YUNG slots.',
        '7x8x14 armory side-room role becomes 15x10x15 meeting room: two 7-wide bays plus wall and one-block depth margin; image controls only volcanic finish.',
        '7x8x7 storeroom expanded by one-block border on XZ to 9x8x9; retain one entrance and empty container function.',
        'Hall grid 7: four bays plus end wall =29 long, two bays plus wall =15 deep; ceremonial double-column hall is original adaptation, not copied source geometry.',
        'Three 7-long straight halls =21 deep, width7 plus two protective sides=9, height8 plus one service floor=9; rotate source E/W circulation to N/S.',
        'Two 7-wide bays plus wall=15 on X/Z, original left-turn topology mirrored/rotated to N/E; height8 plus service floor=9.',
        'Actual gate footprint11x19 becomes13x19 with two protective side borders; height20 reduced to12 and arch rebuilt, retain two-ended gate function.',
        '7x11x7 well module becomes two 7-wide bays plus wall=15x15 courtyard, height11 plus floor=12; open roof is explicit original image-driven adaptation.',
        '7x9x14 worship side-room becomes two bays plus wall=15 wide, depth14 plus3 altar clearance=17, height9 plus floor=10; source side entrance normalized to north and shrine gets empty chest.',
    ]
    evidence = []
    for index, relative in enumerate(source_paths):
        detail_path = LIBRARY / 'nbt-details/better_nether_fortresses/betterfortresses' / (relative + '.json')
        detail = json.loads(detail_path.read_text(encoding='utf-8'))
        source_path = LIBRARY / detail['path']
        reader = NbtReader(source_path.read_bytes())
        assert reader.number('>B') == 10
        reader.string()
        source = reader.value(10)
        assert source['size'] == detail['size']
        assert len(source['blocks']) == detail['block_records']
        evidence.append({'room': f'NAT6-{index + 1:02d}', 'source_nbt': str(source_path), 'source_nbt_sha256': digest(source_path), 'details_path': str(detail_path), 'details_sha256': digest(detail_path), 'source_size': source['size'], 'source_records': len(source['blocks']), 'source_palette': source.get('palette', source.get('palettes', [[]])[0]), 'source_connectors': detail['connectors'], 'changes': changes[index]})
    for relative in ['README.md', 'LEARNING_NOTES.md', 'SAMPLE_CATALOG.md', 'EVIDENCE.md', 'unpacked/better_nether_fortresses/data/betterfortresses/worldgen/template_pool/halls.json', 'unpacked/better_nether_fortresses/data/betterfortresses/worldgen/processor_list/main.json']:
        path = LIBRARY / relative
        path.read_text(encoding='utf-8')
    save(ROOT / 'reference-evidence.json', {'sources': evidence, 'authority': 'Actual unpacked templates and details read and cross-checked before this revision; image-only preliminary draft superseded.', 'runtime_boundary': 'Original YUNG jigsaw name/target/pool/orientation are evidence, NOT exported live jigsaws; FT explicit JSON connector interface replaces them. No processors/functions/mobs/loot copied.', 'material_retention': ['minecraft:nether_bricks', 'minecraft:red_nether_bricks', 'minecraft:polished_blackstone'], 'material_adaptation': 'blackstone, obsidian, basalt and enclosed lava chosen for volcanic image; no global placeholder material replacement', 'geometry': 'Original parametric reconstruction based on source scale, topology and function; not exact source block-layout reproduction', 'usage': 'local research and integration only; no public redistribution authorization inferred'})
    records = []
    for index, (kind, name) in enumerate(ROOMS, 1):
        room_id = f'NAT6-{index:02d}'
        folder = ROOT / 'incoming' / room_id
        environment = os.environ.copy()
        environment['S6_ROOM_INDEX'] = str(index - 1)
        print(run(['node', str(SKILL / 'scripts/run-build.mjs'), str(ROOT / 'tools/build.mjs'), '--out-dir', str(folder), '--name', 'room', '--data-version', '3465'], cwd=SKILL, env=environment), flush=True)
        reference = REFERENCES / f'S6-{index:02d}-{kind}.png'
        with Image.open(reference) as image:
            image.verify()
        template = load_structure(folder / 'room.nbt')
        width, height, depth = template['size']
        centerX, centerZ = width // 2, depth // 2
        connectors = [{'facing': 'north', 'position': [centerX, 2, 0], 'width': 3, 'height': 3}]
        if kind in ('corridor', 'great-hall', 'entrance', 'courtyard'):
            connectors.append({'facing': 'south', 'position': [centerX, 2, depth - 1], 'width': 3, 'height': 3})
        if kind in ('large-chamber', 'medium-room', 'corner'):
            connectors.append({'facing': 'east', 'position': [width - 1, 2, centerZ], 'width': 3, 'height': 3})
        if kind == 'large-chamber':
            connectors.append({'facing': 'west', 'position': [0, 2, centerZ], 'width': 3, 'height': 3})
        metadata = {'id': room_id, 'name': name, 'kind': kind, 'size': template['size'], 'entrance': [centerX, 2, 0], 'interior': [centerX, 2, 2], 'facing': 'north', 'connectors': connectors, 'sha256': digest(folder / 'room.nbt'), 'reference': str(reference), 'reference_sha256': digest(reference), 'coordinate_system': '+X east, +Y up, +Z south; all entrance/connector coordinates are feet', 'provisional_id': True, 'group': 'nature-batch1-S6', 'adaptations': ['sealed lava behind orange glass, no damaging magma floors', 'full roof except courtyard skylight', 'no outside scenery lava, smoke, particles or entities', 'dimensions adapted to modular entrance and safe routes', 'overview is layout reference, not a placeable module']}
        save(folder / 'room.json', metadata)
        metadata['structure_evidence'] = evidence[index - 1]
        metadata['revision'] = 'S6-R2-distinct-topology'
        metadata['distinctive_design'] = DESIGNS[index - 1]
        save(folder / 'room.json', metadata)
        save(folder / 'qa.json', audit(template, metadata))
        save(folder / 'blocks.json', dict(sorted(collections.Counter(template['palette'][record['state']]['Name'] for record in template['blocks']).items())))
        records.append(metadata)
    peers = []
    for relative in ['nature-batch1-group4-swamp', 'nature-S5-coral-ocean-20261104', 'nature-b1-S8-crystal']:
        path = ROOT.parent / relative / 'README.md'
        if path.exists():
            path.read_text(encoding='utf-8')
            peers.append({'readme': str(path), 'sha256': digest(path), 'scope': 'read-only current design documentation comparison; not whole-group NBT similarity certification'})
    save(ROOT / 'distinctiveness-audit.json', {'revision': 'S6-R2-distinct-topology', 'group': 'S6', 'peer_documents_reviewed': peers, 'other_groups_modified_or_imported': False, 'core_signature': ['longitudinal opaque reactor spine with bilateral loops and rear reunion', 'alternating three-baffle lateral zigzag hall', 'mandatory 2-5-2 feet-height kiln crossing', 'well with ground bypass and two optional 2-5-2 side ledges'], 'rooms': [{'id': record['id'], 'sha256': record['sha256'], **DESIGNS[index], 'topology_file': str(ROOT / 'incoming' / record['id'] / 'topology.json')} for index, record in enumerate(records)], 'mechanisms': 'Two manually switched vanilla lamp beacons implemented; no timers, wave logic, trap, lava gates or automatic rewards claimed.', 'combat': 'Geometry-based encounter pacing proposal only. No mobs, wave script or combat playtest.', 'global_matrix': 'PENDING final coordinator audit, especially unavailable group1/2/3/7 final revisions. No all-groups uniqueness PASS claimed.'})
    references = [dict(path=str(path), sha256=digest(path)) for path in sorted(REFERENCES.glob('S6-0*.png'))] + [dict(path=str(REFERENCES / 'S6-10-overview.png'), sha256=digest(REFERENCES / 'S6-10-overview.png'))]
    save(ROOT / 'manifest.json', {'group': 'nature-batch1-S6', 'revision': 'S6-R2-distinct-topology', 'thread': '01a1a5df-bd6b-7403-ae9c-35c85144f0c5', 'group_fingerprint': hashlib.sha256(json.dumps(references, sort_keys=True).encode()).hexdigest(), 'references': references, 'rooms': records, 'scope': 'first 10 reference images only: nine modules and one layout reference', 'image_generation_calls': 0, 'image_generation_partial_files': [], 'deployment': 'NOT_PERFORMED'})


def native():
    target = ROOT / 'qa'
    target.mkdir(exist_ok=True)
    support = Path('D:/ft工程文件/霜雪花园地牢_20261101/build/classpath/runData_minecraftClasspath.txt')
    dependencies = [line for line in support.read_text(encoding='utf-8').splitlines() if line.strip() and 'minecraft_user_repo' not in line]
    classpath = os.pathsep.join(['D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar', *dependencies])
    classes = target / 'native-classes'
    classes.mkdir(exist_ok=True)
    java = Path('C:/Program Files/Common Files/Oracle/Java/javapath')
    output = run([str(java / 'javac.exe'), '-encoding', 'UTF-8', '-classpath', classpath, '-d', str(classes), str(PROJECT / 'tools/VerifyDeliveries.java'), str(ROOT / 'tools/NativeS6.java')], timeout=90)
    (target / 'native-compile.log').write_text(output, encoding='utf-8')
    output = run([str(java / 'java.exe'), '-Djava.awt.headless=true', '-classpath', str(classes) + os.pathsep + classpath, 'NativeS6', str(ROOT)], cwd=ROOT, timeout=120)
    (target / 'native-run.log').write_text(output, encoding='utf-8')
    print(output[-1200:], flush=True)


def render():
    specification = importlib.util.spec_from_file_location('fourviews', PROJECT / 'tools/review_four_views.py')
    renderer = importlib.util.module_from_spec(specification)
    specification.loader.exec_module(renderer)
    sheet = Image.new('RGB', (1800, 1500), '#12141b')
    font = ImageFont.truetype('C:/Windows/Fonts/msyh.ttc', 26)
    for index, (kind, name) in enumerate(ROOMS, 1):
        folder = ROOT / 'incoming' / f'NAT6-{index:02d}'
        metadata = json.loads((folder / 'room.json').read_text(encoding='utf-8'))
        structure = renderer.Structure.read(folder / 'room.nbt')
        views = folder / 'views'
        views.mkdir(exist_ok=True)
        for exterior in (False, True):
            four = Image.new('RGB', (1600, 1200), '#12141b')
            for view_index, (direction, title, camera) in enumerate(renderer.VIEWS):
                image = renderer.render(structure, metadata, camera, title, exterior, size=(800, 600))
                image.save(views / f'{"exterior" if exterior else "interior"}-{direction}.png')
                four.paste(image, ((view_index % 2) * 800, (view_index // 2) * 600))
                if not exterior and view_index == 0:
                    image.save(folder / 'preview.png')
                    thumbnail = image.resize((600, 450))
                    sheet.paste(thumbnail, (((index - 1) % 3) * 600, ((index - 1) // 3) * 500))
            four.save(views / f'{"exterior" if exterior else "interior"}-four-views.png')
        draw = ImageDraw.Draw(sheet)
        draw.text((((index - 1) % 3) * 600 + 10, ((index - 1) // 3) * 500 + 455), f'NAT6-{index:02d} {name}', font=font, fill='white')
        print('RENDERED', metadata['id'], flush=True)
    sheet.save(ROOT / 'preview-sheet.png')


def finalize():
    import urllib.request
    manifest = json.loads((ROOT / 'manifest.json').read_text(encoding='utf-8'))
    native_report = json.loads((ROOT / 'qa/native-review.json').read_text(encoding='utf-8'))
    assert native_report['status'] == 'PASS' and len(native_report['rooms']) == 9
    expected_hashes = {record['id']: record['sha256'] for record in native_report['rooms']}
    files = []
    lever_count = 0
    for record in manifest['rooms']:
        folder = ROOT / 'incoming' / record['id']
        assert digest(folder / 'room.nbt') == record['sha256'] == expected_hashes[record['id']]
        template = load_structure(folder / 'room.nbt')
        states = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
        for position, state in states.items():
            if state['Name'] == 'minecraft:lever':
                assert state['Properties'] == {'face': 'floor', 'facing': 'north', 'powered': 'false'}
                support = states[(position[0], position[1] - 1, position[2])]
                assert support['Name'] == 'minecraft:redstone_lamp' and support['Properties']['lit'] == 'false'
                lever_count += 1
        assert json.loads((folder / 'qa.json').read_text(encoding='utf-8'))['status'] == 'PASS'
        assert len(list((folder / 'views').glob('*.png'))) == 10
        for path in [folder / 'room.nbt', folder / 'room.json', folder / 'blocks.json', folder / 'qa.json', folder / 'topology.json', folder / 'preview.png', *sorted((folder / 'views').glob('*.png'))]:
            if path.suffix == '.png':
                with Image.open(path) as image:
                    image.verify()
            files.append({'path': str(path.relative_to(ROOT)), 'sha256': digest(path), 'bytes': path.stat().st_size})
    assert lever_count == 2
    uniqueness = json.loads((ROOT / 'distinctiveness-audit.json').read_text(encoding='utf-8'))
    uniqueness['palette'] = {'retained_authority': ['nether_bricks', 'red_nether_bricks', 'polished_blackstone'], 'thermal_landmarks': ['basalt', 'obsidian', 'gilded_blackstone', 'shroomlight', 'enclosed lava', 'orange_stained_glass'], 'mechanical_accents': ['lever', 'redstone_lamp'], 'note': 'Palette differences alone are not treated as topology uniqueness'}
    uniqueness['final_revision_verification'] = {'revision': manifest['revision'], 'native_connectors_and_design_waypoints': 'PASS', 'implemented_lever_lamp_pairs': lever_count, 'lever_tick_test': 'NOT_RUN', 'global_duplicate_matrix': 'PENDING'}
    save(ROOT / 'distinctiveness-audit.json', uniqueness)
    for name in ['DISTINCTIVE_DESIGN.md', 'distinctiveness-audit.json', 'reference-evidence.json', 'manifest.json', 'qa/native-review.json']:
        path = ROOT / name
        files.append({'path': name, 'sha256': digest(path), 'bytes': path.stat().st_size})
    with urllib.request.urlopen('http://127.0.0.1:5236/__minecraft_builder__/build.nbt', timeout=10) as response:
        assert hashlib.sha256(response.read()).hexdigest() == manifest['rooms'][0]['sha256']
    save(ROOT / 'qa/delivery-verification.json', {'status': 'PASS', 'modules': 9, 'decoded_pngs': 99, 'source_evidence': 'reference-evidence.json', 'all_manifest_hashes_match_native_report': True, 'lodestone_http_serves_final_nbt': True, 'native_validation': 'PASS', 'static_validation': 'PASS', 'forge_placement': 'NOT_RUN', 'forge_tick_stability': 'NOT_RUN', 'visual_review': 'Preview sheet visually inspected; four-way evidence generated, not equivalent to in-game acceptance', 'files': files})
    print('PASS: final hashes, 9 modules, 99 preview PNGs and Lodestone NBT endpoint')


if __name__ == '__main__':
    {'build': build, 'native': native, 'render': render, 'finalize': finalize}[sys.argv[1]]()
