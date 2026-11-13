import argparse
import copy
import hashlib
import importlib.util
import json
import re
import sys
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image


HOME = Path(__file__).resolve().parents[1]
ROOT = HOME / 'zones-20261103'
BATCH = Path('D:/ft工程文件/image2.5/dungeon-zone-batch-20261103')
DELIVERIES = HOME.parent / 'deliveries'
INSTANCE = Path('C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2')
CATEGORIES = {'C': 'corners', 'P': 'parkour', 'H': 'halls', 'N': 'hub', 'M': 'mines', 'R': 'ruins', 'A': 'abandoned'}
IDS = tuple(f'zone-{prefix}{number:02d}' for prefix in CATEGORIES for number in range(1, 6))
REQUIRED = ('room.nbt', 'room.json', 'preview.png', 'qa.json')
VERIFIER = None


def require(condition, message):
    if not condition:
        raise ValueError(message)


def resolved(path):
    value = str(Path(path).resolve())
    return Path(value[4:] if value.startswith('\\\\?\\') else value)


def io_path(path):
    return Path('\\\\?\\' + str(resolved(path)))


def load(path):
    return json.loads(io_path(path).read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(io_path(path).read_bytes()).hexdigest()


def record(path):
    return {'path': str(resolved(path)), 'sha256': digest(path), 'bytes': io_path(path).stat().st_size}


def save(path, value, exclusive=False):
    path = resolved(path)
    require(path.is_relative_to(resolved(ROOT)), 'Write outside zone audit')
    io_path(path).parent.mkdir(parents=True, exist_ok=True)
    with io_path(path).open('x' if exclusive else 'w', encoding='utf-8') as stream:
        stream.write(json.dumps(value, ensure_ascii=False, indent=2) + '\n')


def verify_file(information):
    require(digest(information['path']) == information['sha256'], 'Evidence changed: ' + information['path'])
    return Path(information['path'])


def freeze(source, target, expected=None):
    require(resolved(target).is_relative_to(resolved(ROOT)), 'Freeze outside zone audit')
    before = record(source)
    require(expected is None or before['sha256'] == expected, 'Unexpected source hash')
    payload = io_path(source).read_bytes()
    require(hashlib.sha256(payload).hexdigest() == before['sha256'], 'Unstable source read')
    io_path(target).parent.mkdir(parents=True, exist_ok=True)
    if io_path(target).exists():
        require(digest(target) == before['sha256'], 'Refusing to overwrite frozen evidence')
    else:
        with io_path(target).open('xb') as stream:
            stream.write(payload)
    require(digest(source) == digest(target) == before['sha256'], 'Source changed while freezing')
    return {**record(target), 'source': str(resolved(source))}


def picture(path, expected_size=None, expected_mode=None):
    with Image.open(io_path(path)) as image:
        image.load()
        require(image.format == 'PNG', 'Expected PNG image')
        require(expected_size is None or image.size == expected_size, 'Image dimensions mismatch')
        require(expected_mode is None or image.mode == expected_mode, 'Image mode mismatch')
        return {'dimensions': list(image.size), 'mode': image.mode}


def bootstrap():
    mapping = load(ROOT / 'thread-map.json')
    require(tuple(mapping) == IDS and len(set(mapping.values())) == 35, 'Incorrect dedicated thread mapping')
    require(all(re.fullmatch(r'[0-9a-f-]{36}', value) for value in mapping.values()), 'Invalid thread ID')
    if io_path(ROOT / 'slots.json').exists():
        slots = load(ROOT / 'slots.json')
        require(digest(ROOT / 'thread-map.json') == slots['thread_mapping']['sha256'], 'Thread ownership changed')
        return slots
    manifest_path = BATCH / 'docs/manifest.json'
    manifest = load(manifest_path)
    verification = load(BATCH / 'docs/verify.json')
    require(len(manifest['files']) == verification['total'] == verification['decoded_ok'] == 35 and not verification['bad'], 'Source verification is incomplete')
    require(verification['categories'] == {category: 5 for category in CATEGORIES.values()}, 'Incorrect seven-category counts')
    source_directory = ROOT / 'sources' / digest(manifest_path)
    documents = [freeze(BATCH / relative, source_directory / relative) for relative in ('README.md', 'docs/manifest.json', 'docs/verify.json')]
    verified = {value['file']: value for value in verification['records']}
    designs = {}
    for original in manifest['files']:
        identifier = Path(original['file']).name.split('-', 1)[0]
        canonical = 'zone-' + identifier
        require(canonical in IDS and canonical not in designs, 'Unexpected/duplicate source design ID')
        require(original == verified[original['file']], 'Manifest/verification source mismatch')
        require(original['category'] == CATEGORIES[identifier[0]], 'Design category mismatch')
        source = resolved(BATCH / original['file'])
        require(source.is_relative_to(resolved(BATCH)), 'Source outside batch')
        require(io_path(source).stat().st_size == original['bytes'], 'Design byte size mismatch')
        decoded = picture(source, (1536, 1024), 'RGB')
        reference = freeze(source, ROOT / 'references' / original['sha256'] / source.name, original['sha256'])
        designs[canonical] = {**reference, **decoded}
    require(set(designs) == set(IDS), 'Missing designs')
    base_registries = []
    old_slots = []
    for label, pointer, key in [('old', HOME / 'latest.json', 'rooms'), ('corridors', HOME / 'corridors-20261103/latest.json', 'corridors')]:
        information = load(pointer)['registry']
        verify_file(information)
        frozen = freeze(information['path'], ROOT / 'sources/base' / information['sha256'] / (label + '-registry.json'), information['sha256'])
        base_registries.append(frozen)
        old_slots += [{field: entry[field] for field in ('id', 'slot', 'template_id')} for entry in load(frozen['path'])[key]]
    require(len(old_slots) == 81 and {entry['slot'] for entry in old_slots} == set(range(81)), 'Base slots are not exactly 0-80')
    reservations = [{'id': identifier, 'slot': 81 + index, 'template_id': 'ftdungeonmobs:image25/' + identifier.lower().replace('-', '_'),
                     'producer_thread': mapping[identifier], 'category': CATEGORIES[identifier[5]],
                     'reference': designs[identifier], 'delivery_directory': str(DELIVERIES / identifier)} for index, identifier in enumerate(IDS)]
    support_source = HOME / 'tools/intake_corridors.py'
    support = freeze(support_source, ROOT / 'sources/verifier' / digest(support_source) / support_source.name)
    overview_source = BATCH / manifest['overview']
    overview = {**record(overview_source), **picture(overview_source), 'is_independent_room': False}
    slots = {'schema_version': 1, 'slot_scope': 'LOCAL_RESERVATION_ONLY_NOT_RUNTIME_CATALOG', 'base_registries': base_registries,
             'old_slots': old_slots, 'zones': reservations, 'source_documents': documents, 'overview': overview,
             'thread_mapping': record(ROOT / 'thread-map.json'), 'support_verifier': support,
             'producer_mapping_evidence': '35 dedicated thread IDs supplied in delegation from 01a157ef-83ef-7e51-8671-2d7f9db88086'}
    save(ROOT / 'slots.json', slots, exclusive=True)
    return slots


def verifier(slots):
    global VERIFIER
    if VERIFIER is None:
        source = verify_file(slots['support_verifier'])
        specification = importlib.util.spec_from_file_location('zone_frozen_receiver_verifier', source)
        VERIFIER = importlib.util.module_from_spec(specification)
        specification.loader.exec_module(VERIFIER)
        VERIFIER.ROOT = ROOT
    return VERIFIER


def static_check(folder, entry):
    sys.path.insert(0, str(INSTANCE / 'output/dungeon-expansion-20261101/tools'))
    from nbtio import Structure, plain, read_nbt
    metadata = load(folder / 'room.json')
    require(metadata['id'] in (entry['id'], entry['id'].removeprefix('zone-')), 'Producer metadata ID belongs to another room')
    declared_hash = metadata.get('sha256', metadata.get('nbt_sha256'))
    require(isinstance(declared_hash, str) and declared_hash.lower() == entry['candidate_sha256'], 'Metadata NBT hash missing/mismatched')
    reference = metadata.get('reference', metadata.get('source'))
    reference_path = reference.get('path', reference.get('image')) if isinstance(reference, dict) else reference
    reference_hash = metadata.get('reference_sha256', metadata.get('referenceSha256', reference.get('sha256') if isinstance(reference, dict) else None))
    require(reference_hash == entry['reference']['sha256'] or (isinstance(reference_path, str) and resolved(reference_path) == resolved(entry['reference']['source'])), 'Metadata does not bind the assigned original design')
    require(io_path(folder / 'room.nbt').read_bytes().startswith(b'\x1f\x8b'), 'Expected gzip NBT')
    unused, raw = read_nbt(io_path(folder / 'room.nbt'))
    payload = plain(raw)
    require('palette' in payload and 'palettes' not in payload, 'Expected one structure palette')
    positions = [tuple(block['pos']) for block in payload['blocks']]
    require(all(len(position) == 3 and all(isinstance(coordinate, int) for coordinate in position) for position in positions), 'Invalid block coordinate vector')
    require(len(positions) == len(set(positions)), 'Duplicate block coordinates')
    structure = Structure.read(io_path(folder / 'room.nbt'))
    require(structure.data_version == 3465 and list(structure.size) == metadata['size'], 'Minecraft version or structure size mismatch')
    require(all(isinstance(limit, int) and 1 <= limit <= 512 for limit in structure.size), 'Invalid structure extent')
    require(all(all(0 <= coordinate < limit for coordinate, limit in zip(position, structure.size)) for position in positions), 'Block outside structure bounds')
    require(not structure.entities, 'Preplaced entities are not accepted')
    forbidden = {'command_block', 'chain_command_block', 'repeating_command_block', 'jigsaw', 'structure_block', 'spawner'}
    require(all(state.name.startswith('minecraft:') and state.name.split(':', 1)[1] not in forbidden for state in structure.blocks.values()), 'Modded/forbidden palette requires separate dependency review')
    for value in structure.block_entities.values():
        content = plain(value)
        require(not content.get('Items') and not content.get('LootTable') and not content.get('Command'), 'Reward inventory or executable block entity')
    require(isinstance(load(folder / 'qa.json'), dict), 'Producer QA must be an object')
    picture(folder / 'preview.png')
    return {'status': 'PASS_INTAKE_ONLY', 'size': list(structure.size), 'blocks': len(structure.blocks),
            'block_entities': len(structure.block_entities), 'category': entry['category'], 'producer_metadata_id': metadata['id'],
            'interfaces_as_declared': {key: metadata[key] for key in ('entrance', 'interior', 'exit', 'connectors', 'ports', 'route', 'parkour') if key in metadata},
            'category_path_status': 'NOT_VERIFIED', 'limitations': 'No straight-corridor rule applied; supports, liquids, collision, jumps, ports and visuals still need independent receiver tests'}


def receiver_acceptance(entry, slots):
    holds = load(ROOT / 'holds.json') if io_path(ROOT / 'holds.json').is_file() else {}
    require(entry['id'] not in holds, 'Receiver hold: ' + holds.get(entry['id'], {}).get('reason', ''))
    result = verifier(slots).receiver_acceptance(entry)
    if result:
        acceptance = load(result['acceptance']['path'])
        require(acceptance.get('category') == entry['category'] and acceptance.get('category_specific_review') == 'PASS', 'Missing category-specific receiver review')
    return result


def receive(reserved, slots):
    entry = copy.deepcopy(reserved)
    entry.update({'candidate_sha256': None, 'bundle_sha256': None, 'receipt_status': 'AWAITING_DELIVERY', 'files': {}, 'missing': [],
                  'receiver_static': {'status': 'PENDING'}, 'four_view_status': 'PENDING', 'receiver_retest_status': 'PENDING',
                  'ready_for_unified_integration': False, 'hard_blockers': []})
    folder = Path(entry['delivery_directory'])
    entry['missing'] = [name for name in REQUIRED if not io_path(folder / name).is_file()]
    entry['delivery_directory_exists'] = io_path(folder).is_dir()
    holds = load(ROOT / 'holds.json') if io_path(ROOT / 'holds.json').is_file() else {}
    if entry['id'] in holds:
        entry['source_hold'] = holds[entry['id']]
        entry['hard_blockers'].append(holds[entry['id']]['reason'])
        entry['receipt_status'] = 'SOURCE_FINALIZATION_BLOCKED'
        return entry
    if entry['missing']:
        entry['receipt_status'] = 'PARTIAL_DELIVERY' if len(entry['missing']) < len(REQUIRED) else 'AWAITING_DELIVERY'
        return entry
    try:
        originals = {name: record(folder / name) for name in REQUIRED}
        confirmations = load(ROOT / 'final-confirmations.json') if io_path(ROOT / 'final-confirmations.json').is_file() else {}
        if entry['id'] in confirmations:
            require(originals['room.nbt']['sha256'] == confirmations[entry['id']]['candidate_sha256'], 'Confirmed final NBT changed before receipt')
            require(entry['reference']['sha256'] == confirmations[entry['id']]['reference_sha256'], 'Confirmed design reference mismatch')
        entry['candidate_sha256'] = originals['room.nbt']['sha256']
        entry['bundle_sha256'] = hashlib.sha256(json.dumps({name: originals[name]['sha256'] for name in REQUIRED}, sort_keys=True).encode()).hexdigest()
        snapshot = ROOT / 'incoming' / entry['id'] / entry['candidate_sha256'] / entry['bundle_sha256']
        entry['files'] = {name: freeze(folder / name, snapshot / name, originals[name]['sha256']) for name in REQUIRED}
        require(all(digest(folder / name) == originals[name]['sha256'] for name in REQUIRED), 'Delivery changed during receipt')
        entry['snapshot'] = str(snapshot)
        entry['receipt_status'] = 'RECEIVED'
        entry['receiver_static'] = static_check(snapshot, entry)
        holds = load(ROOT / 'holds.json') if io_path(ROOT / 'holds.json').is_file() else {}
        if entry['id'] in holds:
            entry['source_hold'] = holds[entry['id']]
            entry['hard_blockers'].append(holds[entry['id']]['reason'])
            return entry
        decision = receiver_acceptance(entry, slots)
        if decision:
            entry['receiver_acceptance'] = decision
            entry['four_view_status'] = entry['receiver_retest_status'] = 'PASS'
            entry['ready_for_unified_integration'] = True
    except (ValueError, KeyError, TypeError, OSError, EOFError) as error:
        entry['hard_blockers'].append(type(error).__name__ + ': ' + str(error))
        if entry['receipt_status'] != 'RECEIVED':
            entry['receipt_status'] = 'UNSTABLE_OR_UNREADABLE_DELIVERY'
            entry['candidate_sha256'] = entry['bundle_sha256'] = None
            entry['files'] = {}
    return entry


def counts(entries):
    return {'registered': len(entries), 'designs_verified': len(entries),
            'received': sum(entry['receipt_status'] == 'RECEIVED' for entry in entries),
            'awaiting_or_partial': sum(entry['receipt_status'] != 'RECEIVED' for entry in entries),
            'pending_four_views': sum(entry['four_view_status'] != 'PASS' for entry in entries),
            'pending_receiver_retest': sum(entry['receiver_retest_status'] != 'PASS' for entry in entries),
            'ready_for_unified_integration': sum(entry['ready_for_unified_integration'] for entry in entries),
            'blocked': sum(bool(entry['hard_blockers']) for entry in entries)}


def verify_registry(registry):
    slots = load(ROOT / 'slots.json')
    require(registry['deployment_authorized'] is False and registry['reservation_only'] is True, 'No deployment permission')
    verify_file(slots['thread_mapping'])
    mapping = load(ROOT / 'thread-map.json')
    require(tuple(mapping) == IDS, 'Thread mapping IDs changed')
    for information in slots['base_registries'] + slots['source_documents'] + [slots['support_verifier']]:
        verify_file(information)
    expected_base = []
    for information in slots['base_registries']:
        base = load(information['path'])
        expected_base += [{field: entry[field] for field in ('id', 'slot', 'template_id')}
                          for entry in base.get('rooms', base.get('corridors', []))]
    require(slots['old_slots'] == expected_base and {entry['slot'] for entry in expected_base} == set(range(81)), 'Base reservations changed')
    manifest_record = next(information for information in slots['source_documents'] if Path(information['source']).name == 'manifest.json')
    source_designs = {'zone-' + Path(original['file']).name.split('-', 1)[0]: original for original in load(manifest_record['path'])['files']}
    require(set(source_designs) == set(IDS), 'Source design IDs changed')
    require(slots['overview']['is_independent_room'] is False, 'Overview cannot reserve a room')
    records = registry['zones']
    require(len(records) == 35 and tuple(entry['id'] for entry in records) == IDS, 'Zone IDs/count changed')
    combined = slots['old_slots'] + records
    for field in ('id', 'slot', 'template_id'):
        require(len({entry[field] for entry in combined}) == 116, 'Duplicate reserved ' + field)
    require([entry['slot'] for entry in records] == list(range(81, 116)), 'Zone slot drift')
    require(len({entry['producer_thread'] for entry in records}) == 35, 'Duplicate producer thread')
    for entry, reserved in zip(records, slots['zones']):
        require(all(entry[field] == reserved[field] for field in ('id', 'slot', 'template_id', 'producer_thread', 'category')), 'Reservation drift')
        require(mapping[entry['id']] == entry['producer_thread'], 'Producer mapping mismatch')
        require(entry['reference'] == reserved['reference'], 'Reference reassigned')
        original = source_designs[entry['id']]
        require(entry['reference']['sha256'] == original['sha256']
                and resolved(entry['reference']['source']) == resolved(BATCH / original['file'])
                and entry['category'] == original['category'], 'Reference does not match frozen manifest')
        verify_file(entry['reference'])
        require(digest(entry['reference']['source']) == entry['reference']['sha256'], 'Original design changed')
        for information in entry['files'].values():
            verify_file(information)
            require(resolved(information['path']).is_relative_to(resolved(ROOT / 'incoming')), 'Unfrozen delivery path')
        if entry['candidate_sha256'] is not None:
            require(entry['candidate_sha256'] == entry['files']['room.nbt']['sha256'], 'Candidate identity mismatch')
            bundle = hashlib.sha256(json.dumps({name: entry['files'][name]['sha256'] for name in REQUIRED}, sort_keys=True).encode()).hexdigest()
            require(bundle == entry['bundle_sha256'], 'Bundle identity mismatch')
        if entry['ready_for_unified_integration'] or entry['four_view_status'] == 'PASS' or entry['receiver_retest_status'] == 'PASS':
            require(entry['receipt_status'] == 'RECEIVED' and not entry['hard_blockers'] and entry['receiver_static']['status'] == 'PASS_INTAKE_ONLY'
                    and entry['four_view_status'] == entry['receiver_retest_status'] == 'PASS' and receiver_acceptance(entry, slots), 'Unproven receiver acceptance')
    require(registry['counts'] == counts(records), 'Incorrect counts')
    return registry['counts']


def self_test(registry):
    cases = [
        ('unprefixed_id', lambda value: value['zones'][0].update(id='C01')),
        ('old_slot_collision', lambda value: value['zones'][0].update(slot=16)),
        ('duplicate_thread', lambda value: value['zones'][0].update(producer_thread=value['zones'][1]['producer_thread'])),
        ('wrong_reference', lambda value: value['zones'][0]['reference'].update(sha256='0' * 64)),
        ('ready_without_evidence', lambda value: value['zones'][0].update(ready_for_unified_integration=True)),
        ('forged_producer_pass', lambda value: value['zones'][0].update(four_view_status='PASS', receiver_retest_status='PASS')),
        ('overview_counted_as_room', lambda value: value['zones'].append(copy.deepcopy(value['zones'][0]))),
    ]
    results = []
    for name, mutate in cases:
        changed = copy.deepcopy(registry)
        mutate(changed)
        try:
            verify_registry(changed)
        except (ValueError, KeyError, TypeError, OSError):
            results.append({'case': name, 'status': 'PASS_REJECTED'})
        else:
            raise ValueError('Negative guard accepted: ' + name)
    return {'status': 'PASS', 'scope': 'in-memory registration and evidence guards; no runtime test', 'cases': results}


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument('--verify', type=Path)
    parser.add_argument('--self-test', type=Path)
    arguments = parser.parse_args()
    if arguments.verify or arguments.self_test:
        path = arguments.verify or arguments.self_test
        registry = load(path)
        result = verify_registry(registry)
        if arguments.self_test:
            result = self_test(registry)
            save(path.parent / 'negative-guards.json', result)
        print(json.dumps(result, ensure_ascii=False))
        return
    protected = [HOME / 'latest.json', HOME / 'corridors-20261103/latest.json']
    before = {str(path): digest(path) for path in protected}
    slots = bootstrap()
    entries = [receive(reserved, slots) for reserved in slots['zones']]
    registry = {'schema_version': 1, 'created_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
                'coordinator_thread': '01a19c8f-6747-7572-a77a-6e90084bbd34', 'deployment_authorized': False,
                'reservation_only': True, 'slot_scope': slots['slot_scope'], 'zones': entries, 'counts': counts(entries)}
    verify_registry(registry)
    require(before == {str(path): digest(path) for path in protected}, 'Prior ledger pointer changed during scan')
    run = ROOT / 'runs' / datetime.now(timezone(timedelta(hours=8))).strftime('%Y%m%d-%H%M%S-%f')
    save(run / 'registry.json', registry, exclusive=True)
    save(run / 'queue.json', {'pending': [{field: entry.get(field) for field in ('id', 'slot', 'producer_thread', 'receipt_status', 'missing', 'candidate_sha256', 'bundle_sha256', 'hard_blockers')}
                                        for entry in entries if not entry['ready_for_unified_integration']]}, exclusive=True)
    save(run / 'combined-reservations.json', {'reservation_only': True, 'entries': slots['old_slots'] + [{field: entry[field] for field in ('id', 'slot', 'template_id')} for entry in entries]}, exclusive=True)
    save(run / 'negative-guards.json', self_test(registry), exclusive=True)
    lines = ['# 分区房间接收盘点', '', registry['created_at'], '', json.dumps(registry['counts'], ensure_ascii=False), '',
             '槽位仅本地预留；设计图、制作方 PASS 和收到四件套均不能替代本接收者八视图与四旋转 240 tick 复验。', '',
             '| ID | 槽位 | 收件 | 静态筛查 | 四视图 | 原生复验 | 可接入 | 缺件/阻断 |', '| --- | ---: | --- | --- | --- | --- | --- | --- |']
    lines += [f"| {entry['id']} | {entry['slot']} | {entry['receipt_status']} | {entry['receiver_static']['status']} | {entry['four_view_status']} | {entry['receiver_retest_status']} | {entry['ready_for_unified_integration']} | {', '.join(entry['missing'] + entry['hard_blockers']) or '-'} |" for entry in entries]
    report = run / 'REPORT.md'
    with io_path(report).open('x', encoding='utf-8') as stream:
        stream.write('\n'.join(lines) + '\n')
    latest = {'run': str(run), 'registry': record(run / 'registry.json'), 'report': str(report), 'counts': registry['counts']}
    save(ROOT / 'latest.json', latest)
    print(json.dumps(latest, ensure_ascii=False))


if __name__ == '__main__':
    main()
