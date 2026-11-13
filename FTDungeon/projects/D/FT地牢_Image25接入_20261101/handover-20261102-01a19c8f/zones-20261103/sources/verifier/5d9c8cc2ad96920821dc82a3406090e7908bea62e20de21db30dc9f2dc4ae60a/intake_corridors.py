import argparse
import copy
import hashlib
import json
import re
import shutil
import struct
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path
from xml.etree import ElementTree

from PIL import Image


HOME = Path(__file__).resolve().parents[1]
ROOT = HOME / 'corridors-20261103'
PROJECT = HOME.parent
BATCH = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103')
INSTANCE = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2')
REVIEWER = '01a19c8f-6747-7572-a77a-6e90084bbd34'
IDS = tuple(f'corridor-{kind}{number:02d}' for kind in ('M', 'S') for number in range(1, 21))
REQUIRED = ('room.nbt', 'room.json', 'preview.png', 'qa.json')
AIR = {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air', 'minecraft:light'}
DANGEROUS = {'minecraft:lava', 'minecraft:water', 'minecraft:fire', 'minecraft:soul_fire',
             'minecraft:powder_snow', 'minecraft:cobweb', 'minecraft:magma_block',
             'minecraft:cactus', 'minecraft:campfire', 'minecraft:soul_campfire'}


def require(condition, message):
    if not condition:
        raise ValueError(message)


def load(path):
    return json.loads(io_path(path).read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(io_path(path).read_bytes()).hexdigest()


def resolved(path):
    value = str(Path(path).resolve())
    if value.startswith('\\\\?\\UNC\\'):
        value = '\\\\' + value[8:]
    elif value.startswith('\\\\?\\'):
        value = value[4:]
    return Path(value)


def io_path(path):
    return Path('\\\\?\\' + str(resolved(path)))


def record(path):
    path = resolved(path)
    return {'path': str(path), 'sha256': digest(path), 'bytes': io_path(path).stat().st_size}


def save(path, value):
    path = resolved(path)
    require(path.is_relative_to(resolved(ROOT)), 'Write outside corridor audit root')
    io_path(path).parent.mkdir(parents=True, exist_ok=True)
    io_path(path).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def freeze(source, target):
    source, target = resolved(source), resolved(target)
    require(target.is_relative_to(resolved(ROOT)), 'Snapshot outside audit root')
    original = digest(source)
    io_path(target).parent.mkdir(parents=True, exist_ok=True)
    if io_path(target).exists():
        require(digest(target) == original, 'Refusing to overwrite frozen evidence: ' + str(target))
    else:
        shutil.copy2('\\\\?\\' + str(source), '\\\\?\\' + str(target))
    require(digest(source) == original == digest(target), 'Evidence changed while copying: ' + str(source))
    return {**record(target), 'source': str(source)}


def verify_file(information, base=None):
    path = resolved(information['path'])
    if base is not None:
        require(path.is_relative_to(resolved(base)), 'Evidence outside expected directory')
    require(io_path(path).is_file() and digest(path) == information['sha256'], 'Evidence hash mismatch: ' + str(path))
    return io_path(path)


def picture(path, size=None):
    with Image.open(io_path(path)) as image:
        require(image.format == 'PNG', 'Not a PNG: ' + str(path))
        image.load()
        dimensions = list(image.size)
    if size is not None:
        require(dimensions == size, 'Unexpected PNG dimensions: ' + str(path))
    return dimensions


def reference_inputs():
    manifest = load(BATCH / 'manifest.json')
    index = load(BATCH / 'out/corridor_index.json')
    mapping = load(ROOT / 'thread-map.json')
    expected = {identifier.removeprefix('corridor-') for identifier in IDS}
    require(len(mapping) == len(set(mapping.values())) == 40 and set(mapping) == set(IDS), 'Thread map is not one-to-one')
    require(all(re.fullmatch(r'[0-9a-f]{8}(?:-[0-9a-f]{4}){3}-[0-9a-f]{12}', value) for value in mapping.values()), 'Invalid thread ID')
    require(len(manifest['images']) == len(index) == 40, 'Expected exactly 40 originals; sheets are not corridors')
    require({entry['id'] for entry in manifest['images']} == {entry['id'] for entry in index} == expected, 'Source ID mismatch')
    require(len({entry['sha256'] for entry in manifest['images']}) == 40, 'Duplicate original image hashes')
    documentation = []
    for relative in ('README.md', 'SPEC.md', 'manifest.json', 'out/corridor_index.json'):
        source = BATCH / relative
        documentation.append(freeze(source, ROOT / 'sources' / digest(source) / source.name))
    by_index = {entry['id']: entry for entry in index}
    images = {}
    for entry in manifest['images']:
        source = (BATCH / entry['path']).resolve()
        require(source.is_relative_to(BATCH.resolve()), 'Reference path escapes batch')
        style = by_index[entry['id']]
        require(source.name == style['filename'], 'Index filename mismatch')
        require(entry['kind'] == style['kind'] == ('medium' if entry['id'].startswith('M') else 'short'), 'Length class mismatch')
        require(source.stat().st_size == entry['bytes'] and digest(source) == entry['sha256'], 'Original hash/size mismatch')
        dimensions = picture(source, [1536, 1024])
        frozen = freeze(source, ROOT / 'references' / entry['sha256'] / source.name)
        require(frozen['sha256'] == entry['sha256'], 'Original changed after manifest check')
        images['corridor-' + entry['id']] = {**frozen, 'dimensions': dimensions, 'style': style}
    for kind in ('medium', 'short'):
        styles = [value for value in index if value['kind'] == kind]
        require(len(styles) == len({tuple(value[key] for key in ('theme', 'condition', 'light', 'feature')) for value in styles}) == 20, 'Repeated style combination')
    sheets = []
    for entry in manifest['sheets'].values():
        source = (BATCH / entry['path']).resolve()
        require(source.is_relative_to(BATCH.resolve()) and digest(source) == entry['sha256'] and source.stat().st_size == entry['bytes'], 'Sheet hash mismatch')
        sheets.append({**record(source), 'dimensions': picture(source, entry['size']), 'is_independent_corridor': False})
    return mapping, images, documentation, sheets


def reservations(mapping):
    path = ROOT / 'slots.json'
    if path.exists():
        slots = load(path)
        verify_file(slots['base_registry'])
    else:
        previous = load(HOME / 'latest.json')
        original = Path(previous['registry']['path'])
        require(digest(original) == previous['registry']['sha256'], 'Previous room registry changed')
        rooms = load(original)['rooms']
        require(len(rooms) == 41 and {value['slot'] for value in rooms} == set(range(41)), 'Explicit rebase required; old slots are not 0-40')
        require(not any(value['id'].startswith('corridor-') for value in rooms), 'Corridor already present in old room registry')
        slots = {'base_registry': record(original), 'reservation_only': True, 'deployment_authorized': False,
                 'old_slots': [{'id': value['id'], 'slot': value['slot'], 'template_id': value['template_id']} for value in rooms],
                 'corridors': [{'id': identifier, 'slot': 41 + offset, 'template_id': 'ftdungeonmobs:image25/' + identifier.lower().replace('-', '_'),
                                'producer_thread': mapping[identifier]} for offset, identifier in enumerate(IDS)]}
        save(path, slots)
    require([value['id'] for value in slots['corridors']] == list(IDS), 'Reservation order changed')
    require(all(value['slot'] == 41 + offset and value['producer_thread'] == mapping[value['id']] for offset, value in enumerate(slots['corridors'])), 'Slot/thread reservation drift')
    combined = slots['old_slots'] + slots['corridors']
    for key in ('id', 'slot', 'template_id'):
        require(len({value[key] for value in combined}) == 81, 'Duplicate reserved ' + key)
    return slots


def static_check(folder, reservation, reference):
    sys.path.insert(0, str(INSTANCE / 'output/dungeon-expansion-20261101/tools'))
    from nbtio import Structure, plain, read_nbt

    metadata = load(folder / 'room.json')
    require(metadata['id'] in (reservation['id'], reservation['id'].removeprefix('corridor-')), 'Metadata ID does not belong to this corridor')
    require(metadata['sha256'].lower() == digest(folder / 'room.nbt'), 'Metadata NBT hash mismatch')
    require(Path(metadata['reference']).resolve() == Path(reference['source']).resolve(), 'Metadata points to another original')
    require((folder / 'room.nbt').read_bytes().startswith(b'\x1f\x8b'), 'Expected gzip NBT')
    unused, root = read_nbt(folder / 'room.nbt')
    require('palette' in root.value and 'palettes' not in root.value, 'Expected a single palette')
    structure = Structure.read(folder / 'room.nbt')
    require(structure.data_version == 3465 and list(structure.size) == metadata['size'], '1.20.1 size/version mismatch')
    require(len(structure.size) == 3 and all(isinstance(value, int) and 3 <= value <= 128 for value in structure.size), 'Unsafe structure dimensions')
    width, height, length = structure.size
    require(32 <= length <= 48 if reservation['id'].startswith('corridor-M') else length == 16, 'Corridor length outside assigned tier')
    require(not structure.entities, 'Entities are not allowed')
    require(all(all(0 <= coordinate < limit for coordinate, limit in zip(position, structure.size)) for position in structure.blocks), 'Block position outside volume')
    forbidden = {'command_block', 'chain_command_block', 'repeating_command_block', 'structure_block', 'jigsaw', 'spawner'}
    require(all(state.name.startswith('minecraft:') and state.name.split(':', 1)[1] not in forbidden for state in structure.blocks.values()), 'Non-vanilla/forbidden block')
    for value in structure.block_entities.values():
        payload = plain(value)
        require(not payload.get('Items') and not payload.get('LootTable') and not payload.get('Command'), 'Container rewards or executable NBT')
    entrance = metadata['entrance']
    require(len(entrance) == 3 and all(isinstance(value, int) for value in entrance), 'Invalid entrance coordinates')
    center, foot, front = entrance
    require(center == width // 2 and front == 0 and 1 <= foot <= height - 4, 'Expected centered north entrance with headroom')
    require(metadata.get('exit', [center, foot, length - 1]) == [center, foot, length - 1], 'Exit is not straight through at the same level')
    for depth in range(length):
        for across in range(center - 1, center + 2):
            require(all(structure.get((across, foot + rise, depth)).name in AIR for rise in range(3)), f'Blocked/hazardous main lane: {across},{depth}')
            floor = structure.get((across, foot - 1, depth))
            require(floor.name not in AIR | DANGEROUS and not floor.name.endswith(('_stairs', '_fence', '_wall', '_pane', '_torch', '_carpet', '_trapdoor', '_button')),
                    f'Unsafe/non-full floor candidate: {across},{depth}')
            require(not floor.name.endswith('_slab') or floor.get('type') == 'double', 'Slab step on straight level route')
            require(any(structure.get((across, elevation, depth)).name not in AIR for elevation in range(foot + 3, height)), 'Missing roof above main lane')
    interior = metadata['interior']
    require(len(interior) == 3 and all(isinstance(value, int) and 0 <= value < structure.size[axis] for axis, value in enumerate(interior)), 'Invalid interior safe point')
    require(interior[1] == foot and abs(interior[0] - center) <= 1, 'Interior point off verified lane')
    picture(folder / 'preview.png')
    load(folder / 'qa.json')
    return {'status': 'PASS_INTAKE_ONLY', 'size': list(structure.size), 'producer_metadata_id': metadata['id'],
            'blocks': len(structure.blocks), 'data_version': structure.data_version, 'verified_lane': 'north-south, 3x3, level',
            'limitations': 'Not a native block-state, support, liquid-stability or visual acceptance test'}


def receiver_acceptance(entry):
    if entry['candidate_sha256'] is None:
        return None
    directory = ROOT / 'reviews' / entry['id'] / entry['candidate_sha256'] / entry['bundle_sha256']
    path = directory / 'acceptance.json'
    if not io_path(path).exists():
        return None
    acceptance = load(path)
    require(acceptance['reviewer_thread'] == REVIEWER and acceptance['visual_review_complete'] is True, 'Missing receiver visual review')
    for key in ('id', 'candidate_sha256', 'bundle_sha256'):
        require(acceptance[key] == entry[key], 'Receiver acceptance identity mismatch: ' + key)
    require(acceptance['reference_sha256'] == entry['reference']['sha256'], 'Reference changed after review')
    require(acceptance['four_view_status'] == acceptance['retest_status'] == 'PASS' and not acceptance.get('blockers'), 'Receiver gates not passed')
    render = load(verify_file(acceptance['render_manifest'], directory))
    native = load(verify_file(acceptance['native_result'], directory))
    for evidence in (render, native):
        for key in ('id', 'candidate_sha256', 'bundle_sha256'):
            require(evidence[key] == entry[key], 'Receiver evidence identity mismatch')
    images = render['images']
    require(len(images) == 8 and {(image['direction'], image['kind']) for image in images} == {(direction, kind) for direction in ('north', 'south', 'east', 'west') for kind in ('interior', 'exterior')}, 'Need eight bound four-view images')
    for image in images:
        picture(verify_file(image, directory))
    require(render['input_nbt']['sha256'] == entry['candidate_sha256'], 'Rendered input differs from candidate')
    verify_file(render['input_nbt'], directory)
    for information in render['renderer_inputs']:
        verify_file(information, directory)
    require(load(verify_file(render['render_report'], directory))['status'] == 'PASS', 'Render report is not PASS')
    verify_file(render['render_log'], directory)
    require(native['status'] == 'PASS' and native['exit_code'] == 0 and set(native['rotations']) == {0, 90, 180, 270}, 'Native rotation/exit status failed')
    require(native['stable_ticks'] >= 240 and native['isolated_world'] is True, 'Insufficient independent native stability test')
    require(resolved(native['world_directory']).is_relative_to(resolved(directory)), 'Native world must be isolated under this receiver review')
    require(native['logs'] and native['test_results'], 'Missing native logs/results')
    require(len(native['native_reports']) == 4, 'Need four per-rotation native reports')
    rotations = set()
    for information in native['native_reports']:
        report = load(verify_file(information, directory))
        require(report['status'] == 'PASS' and report['source_nbt_sha256'] == entry['candidate_sha256']
                and report['stabilization_ticks'] >= 240, 'Unbound/failed native report')
        rotations.add(report['requested_rotation'])
        require(len(report['rotations']) == 1 and report['rotations'][0]['status'] == 'PASS', 'Invalid rotation report')
    require(rotations == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}, 'Per-rotation evidence incomplete')
    verify_file(native['invocation'], directory)
    require(native['native_captures_match_frozen_nbt'] is True, 'Missing native snapshot equivalence')
    require(len(acceptance['native_captures']) == 4, 'Missing native captured templates')
    for information in acceptance['native_captures']:
        verify_file(information, directory)
    static = load(verify_file(acceptance['receiver_static'], directory))
    require(static['status'] == 'PASS_INTAKE_ONLY' and static['candidate_sha256'] == entry['candidate_sha256'], 'Receiver static evidence mismatch')
    verify_file(acceptance['preparation'], directory)
    for information in native['logs']:
        verify_file(information, directory)
    tests = failures = errors = skipped = 0
    for information in native['test_results']:
        xml = ElementTree.parse(verify_file(information, directory)).getroot()
        cases = list(xml.iter('testcase'))
        require(len({case.get('name') for case in cases}) == len(cases), 'Duplicate native test case')
        tests += len(cases)
        failures += len(list(xml.iter('failure')))
        errors += len(list(xml.iter('error')))
        skipped += len(list(xml.iter('skipped')))
        require(all(int(suite.get(key, 0)) == 0 for suite in xml.iter('testsuite')
                    for key in ('failures', 'errors', 'skipped')), 'Native XML reports failed/skipped tests')
    require(tests >= 4 and failures == errors == skipped == 0, 'Native XML does not prove four passing tests')
    return {'acceptance': record(path), 'four_view_status': 'PASS', 'retest_status': 'PASS', 'native_tests': tests}


def receive(reservation, reference):
    folder = PROJECT / 'deliveries' / reservation['id']
    entry = {**reservation, 'kind': reference['style']['kind'], 'reference': reference, 'delivery_directory': str(folder),
             'candidate_sha256': None, 'bundle_sha256': None, 'receipt_status': 'AWAITING_DELIVERY', 'missing': [], 'files': {},
             'receiver_static': {'status': 'PENDING'}, 'four_view_status': 'PENDING', 'receiver_retest_status': 'PENDING',
             'hard_blockers': [], 'ready_for_unified_integration': False}
    entry['missing'] = [name for name in REQUIRED if not (folder / name).is_file()]
    if entry['missing']:
        entry['receipt_status'] = 'IN_PROGRESS' if folder.exists() else 'AWAITING_DELIVERY'
        return entry
    try:
        source_files = {name: record(folder / name) for name in REQUIRED}
        require(all(Path(value['path']).is_relative_to(folder.resolve()) for value in source_files.values()), 'Delivery symlink escapes dedicated directory')
        entry['candidate_sha256'] = source_files['room.nbt']['sha256']
        entry['bundle_sha256'] = hashlib.sha256(json.dumps({name: source_files[name]['sha256'] for name in REQUIRED}, sort_keys=True).encode()).hexdigest()
        snapshot = ROOT / 'incoming' / entry['id'] / entry['candidate_sha256'] / entry['bundle_sha256']
        entry['files'] = {name: freeze(folder / name, snapshot / name) for name in REQUIRED}
        require(all(digest(folder / name) == source_files[name]['sha256'] == entry['files'][name]['sha256'] for name in REQUIRED), 'Delivery changed while receiving')
        entry['snapshot'] = str(snapshot)
        entry['receipt_status'] = 'RECEIVED'
        entry['receiver_static'] = static_check(snapshot, reservation, reference)
        decision = receiver_acceptance(entry)
        if decision:
            entry['receiver_acceptance'] = decision
            entry['four_view_status'] = entry['receiver_retest_status'] = 'PASS'
            entry['ready_for_unified_integration'] = True
    except (ValueError, KeyError, TypeError, OSError, IndexError, EOFError, struct.error) as failure:
        if entry['receipt_status'] != 'RECEIVED':
            entry['candidate_sha256'] = entry['bundle_sha256'] = None
            entry['files'] = {}
            entry['receipt_status'] = 'CHANGED_OR_INCOMPLETE'
        entry['hard_blockers'].append(type(failure).__name__ + ': ' + str(failure))
        entry['receiver_static'] = {'status': 'NEEDS_REVIEW'}
    return entry


def counts(records):
    return {'registered': len(records), 'designs_verified': len(records),
            'received': sum(entry['receipt_status'] == 'RECEIVED' for entry in records),
            'awaiting_delivery': sum(entry['receipt_status'] != 'RECEIVED' for entry in records),
            'pending_four_views': sum(entry['four_view_status'] != 'PASS' for entry in records),
            'pending_receiver_retest': sum(entry['receiver_retest_status'] != 'PASS' for entry in records),
            'ready_for_unified_integration': sum(entry['ready_for_unified_integration'] for entry in records),
            'blocked': sum(bool(entry['hard_blockers']) for entry in records)}


def verify_registry(registry):
    slots = load(ROOT / 'slots.json')
    mapping = load(ROOT / 'thread-map.json')
    require(registry['deployment_authorized'] is False and registry['reservation_only'] is True, 'Deployment must remain forbidden')
    records = registry['corridors']
    require(len(records) == 40 and [entry['id'] for entry in records] == list(IDS), 'Canonical corridor IDs/count changed')
    require(len({entry['producer_thread'] for entry in records}) == 40, 'Duplicate producer ownership')
    require([entry['slot'] for entry in records] == list(range(41, 81)), 'Corridor slot drift/collision')
    require(len({entry['template_id'] for entry in records} | {entry['template_id'] for entry in slots['old_slots']}) == 81, 'Template collision with old rooms')
    verify_file(slots['base_registry'])
    for information in registry['source_documents'] + registry['protected_files']:
        verify_file(information)
    manifest_file = next(value for value in registry['source_documents'] if Path(value['source']).name == 'manifest.json')
    manifest = load(manifest_file['path'])
    originals = {value['id']: value for value in manifest['images']}
    require(len(registry['overview_sheets']) == 2, 'Expected two excluded overview sheets')
    for information in registry['overview_sheets']:
        verify_file(information)
        require(information['is_independent_corridor'] is False, 'Overview must not reserve a corridor slot')
    for entry, reserved in zip(records, slots['corridors']):
        require(all(entry[key] == reserved[key] for key in ('id', 'slot', 'template_id', 'producer_thread')), 'Reservation changed')
        require(mapping[entry['id']] == entry['producer_thread'], 'Incorrect thread mapping')
        original = originals[entry['id'].removeprefix('corridor-')]
        require(entry['reference']['sha256'] == original['sha256']
                and Path(entry['reference']['source']).resolve() == (BATCH / original['path']).resolve(), 'Reference not bound to original manifest')
        verify_file(entry['reference'], ROOT / 'references')
        require(digest(entry['reference']['source']) == entry['reference']['sha256'], 'Original reference changed')
        for information in entry['files'].values():
            verify_file(information, ROOT / 'incoming')
        if entry['candidate_sha256'] is not None:
            require(entry['files'] and entry['candidate_sha256'] == entry['files']['room.nbt']['sha256'], 'NBT identity mismatch')
            bundle = hashlib.sha256(json.dumps({name: entry['files'][name]['sha256'] for name in REQUIRED}, sort_keys=True).encode()).hexdigest()
            require(bundle == entry['bundle_sha256'], 'Bundle identity mismatch')
        require(not entry['ready_for_unified_integration'] or (not entry['hard_blockers'] and entry['receipt_status'] == 'RECEIVED'
                and entry['receiver_static']['status'] == 'PASS_INTAKE_ONLY' and entry['four_view_status'] == entry['receiver_retest_status'] == 'PASS'
                and receiver_acceptance(entry) is not None), 'Cannot mark ready without receiver evidence')
        require((entry['four_view_status'] != 'PASS' and entry['receiver_retest_status'] != 'PASS') or receiver_acceptance(entry) is not None, 'Unproven receiver PASS')
    require(registry['counts'] == counts(records), 'Classification counts changed')
    return registry['counts']


def self_test(registry):
    cases = []
    mutations = (
        ('unprefixed_old_room_id', lambda value: value['corridors'][6].update(id='M07')),
        ('collision_with_old_slot', lambda value: value['corridors'][0].update(slot=16)),
        ('duplicate_thread', lambda value: value['corridors'][0].update(producer_thread=value['corridors'][1]['producer_thread'])),
        ('forged_reference_hash', lambda value: value['corridors'][0]['reference'].update(sha256='0' * 64)),
        ('ready_without_reviews', lambda value: value['corridors'][0].update(ready_for_unified_integration=True)),
        ('forged_producer_pass', lambda value: value['corridors'][0].update(four_view_status='PASS', receiver_retest_status='PASS')),
        ('sheet_as_corridor', lambda value: value['corridors'].append(copy.deepcopy(value['corridors'][0]))),
    )
    for name, mutate in mutations:
        candidate = copy.deepcopy(registry)
        mutate(candidate)
        try:
            verify_registry(candidate)
        except (ValueError, KeyError):
            cases.append({'case': name, 'result': 'REJECTED_AS_EXPECTED'})
        else:
            raise ValueError('Negative guard accepted: ' + name)
    return {'status': 'PASS', 'cases': cases}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--verify', type=Path)
    parser.add_argument('--self-test', type=Path)
    arguments = parser.parse_args()
    if arguments.verify or arguments.self_test:
        registry = load(arguments.verify or arguments.self_test)
        print(json.dumps(verify_registry(registry), ensure_ascii=False))
        if arguments.self_test:
            result = self_test(registry)
            save((arguments.self_test).parent / 'negative-guards.json', result)
            print(json.dumps(result, ensure_ascii=False))
        return
    mapping, images, documentation, sheets = reference_inputs()
    slots = reservations(mapping)
    timestamp = datetime.now(timezone(timedelta(hours=8)))
    run = ROOT / 'runs' / timestamp.strftime('%Y%m%d-%H%M%S-%f')
    jars = list((INSTANCE / 'mods').glob('FT地牢怪物-*.jar'))
    require(len(jars) == 1, 'Ambiguous installed core JAR')
    protected_paths = [jars[0], INSTANCE / 'options.txt', HOME / 'latest.json', Path(slots['base_registry']['path'])]
    installed_patch = INSTANCE / 'resourcepacks/FT-Skull-Relief-ZFight-Fix-1.20.1.zip'
    if installed_patch.is_file():
        protected_paths.append(installed_patch)
    protected = [record(path) for path in protected_paths]
    records = [receive(value, images[value['id']]) for value in slots['corridors']]
    registry = {'schema_version': 1, 'created_at': timestamp.isoformat(), 'coordinator_thread': REVIEWER,
                'deployment_authorized': False, 'reservation_only': True, 'source_batch': str(BATCH),
                'source_documents': documentation, 'overview_sheets': sheets, 'protected_files': protected,
                'base_registry': slots['base_registry'], 'corridors': records, 'counts': counts(records)}
    verify_registry(registry)
    save(run / 'registry.json', registry)
    save(run / 'combined-reservations.json', {'reservation_only': True, 'runtime_catalog_changed': False,
        'base_registry': slots['base_registry'], 'slots': slots['old_slots'] + slots['corridors']})
    save(run / 'queue.json', {'no_redispatch': True, 'no_deployment': True, 'pending': [
        {key: entry[key] for key in ('id', 'slot', 'producer_thread', 'delivery_directory', 'receipt_status', 'missing',
                                     'candidate_sha256', 'bundle_sha256', 'four_view_status', 'receiver_retest_status', 'hard_blockers')}
        for entry in records if not entry['ready_for_unified_integration']]})
    save(run / 'negative-guards.json', self_test(registry))
    lines = ['# 走道批次收件与验收快照', '', '时间：' + timestamp.isoformat(), '',
             '- 40 张原图逐张 SHA-256、字节数和完整 PNG 解码通过；两张总览不计入走道。',
             '- 40 个独立线程和 corridor- ID 已登记；仅本地预留 41–80，旧 0–40 不变。',
             '- 四视图和原生复验必须是接收者对最终冻结成品的独立证据，制作线程的自报 PASS 不自动接纳。',
             '- 未部署、未改正式源码/JAR/配置/资源包/存档；旧登记数据及已部署的方颅修复包哈希不变。', '', '## 实际数量', '']
    lines += ['- ' + key + ': ' + str(value) for key, value in registry['counts'].items()]
    lines += ['', '## 唯一槽位与当前状态', '', '| ID | 本地预留槽位 | 收件 | 四视图 | 接收者复验 | 可接入 | 阻塞 |', '|---|---:|---|---|---|---|---|']
    lines += [f"| {entry['id']} | {entry['slot']} | {entry['receipt_status']} | {entry['four_view_status']} | {entry['receiver_retest_status']} | {entry['ready_for_unified_integration']} | {'; '.join(entry['hard_blockers']) or '-'} |" for entry in records]
    lines += ['', '待交付不是验收失败；未收到 NBT 时不制作假四视图或假原生报告。软件渲染与隔离原生验证不等于客户端实机。',
              '槽位不代表运行时结构位置或多区块布局已经接入；双端连接、路线与幂等仍需核心集成负责人另行回归。', '']
    report = run / 'REPORT.md'
    report.parent.mkdir(parents=True, exist_ok=True)
    report.write_text('\n'.join(lines), encoding='utf-8')
    save(ROOT / 'latest.json', {'run': str(run), 'registry': record(run / 'registry.json'), 'report': str(report), 'counts': registry['counts']})
    print(json.dumps({'run': str(run), 'counts': registry['counts'], 'negative_guards': '7/7 PASS'}, ensure_ascii=False, indent=2))


if __name__ == '__main__':
    main()
