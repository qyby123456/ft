import hashlib
import json
import subprocess
import sys
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from build_room import REFERENCE, REFERENCE_SHA256
from nbtio import State, Structure, plain
from qa_static import stock_empty


ROOT = Path(__file__).resolve().parents[1]
ROTATIONS = ('NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90')


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def write_json(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def transformed(position, rotation):
    pos_x, pos_y, pos_z = position
    if rotation == 'CLOCKWISE_90':
        return 12 - pos_z, pos_y, pos_x
    if rotation == 'CLOCKWISE_180':
        return 12 - pos_x, pos_y, 12 - pos_z
    if rotation == 'COUNTERCLOCKWISE_90':
        return pos_z, pos_y, 12 - pos_x
    return position


def rotated(block, rotation):
    properties = dict(block.properties)
    turns = {'NONE': 0, 'CLOCKWISE_90': 1, 'CLOCKWISE_180': 2, 'COUNTERCLOCKWISE_90': 3}[rotation]
    directions = ('north', 'east', 'south', 'west')
    if properties.get('facing') in directions:
        properties['facing'] = directions[(directions.index(properties['facing']) + turns) % 4]
    if turns % 2 and properties.get('axis') in ('x', 'z'):
        properties['axis'] = 'z' if properties['axis'] == 'x' else 'x'
    return State(block.name, tuple(sorted(properties.items())))


def sign_text(value):
    return {key: [json.loads(message) for message in child] if key in ('messages', 'filtered_messages') else child for key, child in value.items()}


def main():
    required = ['room.nbt', 'room.json', 'blueprint.json', 'preview.png']
    before = {name: digest(ROOT / 'out' / name) for name in required}
    initial_metadata = json.loads((ROOT / 'out/room.json').read_text(encoding='utf-8'))
    subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], check=True, cwd=ROOT)
    after = {name: digest(ROOT / 'out' / name) for name in required}
    if before != after:
        raise ValueError('Rebuild differs from native-verified artifacts')
    write_json(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'scope': 'Exact deterministic rebuild of room.nbt, room.json and blueprint.json; preview unchanged', 'before': before, 'after': after})
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], check=True, cwd=ROOT)
    static = json.loads((ROOT / 'qa/static.json').read_text(encoding='utf-8'))
    native = json.loads((ROOT / 'qa/native.json').read_text(encoding='utf-8'))
    render = json.loads((ROOT / 'qa/render-manifest.json').read_text(encoding='utf-8'))
    visual = json.loads((ROOT / 'qa/visual-review.json').read_text(encoding='utf-8'))
    source = Structure.read(ROOT / 'out/room.nbt')
    fingerprint = digest(ROOT / 'out/room.nbt')
    if any(report.get('status') != 'PASS' for report in (static, native, visual)):
        raise ValueError('Missing passing static, native or visual review')
    if any(value != fingerprint for value in (initial_metadata['sha256'], static['structure_sha256'], native['structure_sha256'], render['source_nbt_sha256'], visual['structure_sha256'])):
        raise ValueError('QA evidence refers to a different template')
    if digest(REFERENCE) != REFERENCE_SHA256:
        raise ValueError('Concept fingerprint changed')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    for marker in ('SHOP_C_NATIVE_GAME_TEST_PASS:', 'rotations=4/4, records=6084, ticks=100', 'All 1 required tests passed', 'BUILD SUCCESSFUL'):
        if marker not in log:
            raise ValueError(f'Missing native log marker: {marker}')
    if tuple(entry['rotation'] for entry in native['rotations']) != ROTATIONS:
        raise ValueError('All four native captures are required')
    captures = []
    for entry in native['rotations']:
        path = ROOT / entry['world_capture']
        if digest(path) != entry['world_capture_sha256']:
            raise ValueError('Native capture fingerprint mismatch')
        placed = Structure.read(path)
        expected = {transformed(position, entry['rotation']): rotated(block, entry['rotation']) for position, block in source.blocks.items()}
        expected_entities = {transformed(position, entry['rotation']): value for position, value in source.block_entities.items()}
        if placed.size != source.size or placed.blocks != expected or set(placed.block_entities) != set(expected_entities):
            raise ValueError(f"Native capture state mismatch: {entry['rotation']}")
        for position, value in placed.block_entities.items():
            actual = plain(value)
            original = plain(expected_entities[position])
            if actual['id'] != original['id'] or not stock_empty(actual):
                raise ValueError('Captured block entity type, inventory or loot differs')
            for key in ('front_text', 'back_text'):
                if key in original and sign_text(actual.get(key, {})) != sign_text(original[key]):
                    raise ValueError('Sign content changed during native placement')
            if 'is_waxed' in original and actual.get('is_waxed') != original['is_waxed']:
                raise ValueError('Sign wax state changed during native placement')
        if placed.entities:
            raise ValueError('Unexpected captured entities')
        captures.append({'rotation': entry['rotation'], 'status': 'PASS', 'block_state_matches': len(expected), 'block_entity_type_inventory_sign_matches': len(expected_entities), 'capture_sha256': entry['world_capture_sha256']})
    write_json(ROOT / 'qa/capture-comparison.json', {'status': 'PASS', 'source_sha256': fingerprint, 'captures': captures, 'notes': ['Every captured block state matches independently calculated rotated source state.', 'Runtime block-entity coordinates and engine-added empty/default fields are not required to be byte-identical.', 'Sign component JSON is compared semantically; native serialization removes insignificant whitespace.']})
    for name in ('preview.png',):
        with Image.open(ROOT / 'out' / name) as image:
            image.verify()
    evidence_paths = ['qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/capture-comparison.json', 'qa/reproducibility.json', 'qa/visual-review.json', 'qa/render-manifest.json', 'qa/materials.json', 'qa/finalize-comparison-note.json']
    report = {
        'status': 'PASS', 'id': 'SHOP_C_END_VOID', 'variant': 'C',
        'verified_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'structure_sha256': fingerprint, 'reference_sha256': REFERENCE_SHA256,
        'size': list(source.size), 'records': len(source.blocks), 'non_air_blocks': static['non_air_blocks'],
        'entrance': [6, 1, 0], 'interior': [6, 1, 3],
        'static': {'status': 'PASS', 'checks': len(static['checks']), 'reachable_foot_positions': static['reachable_foot_positions'], 'end_rods': static['end_rods'], 'explicit_decor_supports': static['supported_decor']},
        'forge_native': {'status': 'PASS', 'minecraft': native['minecraft'], 'forge': native['forge'], 'rotations': 4, 'stabilization_ticks': 100, 'records_checked_after_ticks': native['records_checked_after_ticks'], 'missing_support_negative_probes': native['negative_missing_rod_support_probes'], 'blocked_aisle_negative_probes': native['negative_blocked_aisle_probes'], 'unexpected_world_entities': 0},
        'capture_comparison': {'status': 'PASS', 'rotations': 4, 'independent_block_state_matches': sum(entry['block_state_matches'] for entry in captures)},
        'reproducibility': 'PASS', 'visual_review': 'PASS_SOFTWARE_ONLY',
        'integration': {'registered': False, 'assigned_slot': None, 'deployed': False, 'production_world_touched': False, 'coordinator_acceptance_required': True},
        'evidence': {path: {'path': str(ROOT / path), 'sha256': digest(ROOT / path)} for path in evidence_paths},
        'limitations': [
            'preview.png is software rendering of actual delivered NBT, not an AI concept or client screenshot. No emitted-light, particle or shader simulation.',
            'No complete modpack client, multiplayer, FPS, merchant NPC or functional trading acceptance performed.',
            'No FT catalog slot, actual lobby connection, occupancy refusal, generation marker idempotence or production deployment tested.',
            'Source containers contain no stock or loot; the vanilla ender chest accesses existing player personal storage and is not a new reward.',
            'STYLES.md is referenced by the supplied design README but was absent from the design directory and its parent subtree at inspection; existing C concept and fingerprint were used without fabrication.'
        ]
    }
    write_json(ROOT / 'out/qa.json', report)
    files = required + ['qa.json']
    manifest = {
        'id': 'SHOP_C_END_VOID', 'variant': 'C', 'delivery_root': str(ROOT / 'out'),
        'structure_sha256': fingerprint, 'reference': str(REFERENCE), 'reference_sha256': REFERENCE_SHA256,
        'deployed': False, 'assigned_slot': None,
        'files': {name: {'sha256': digest(ROOT / 'out' / name), 'bytes': (ROOT / 'out' / name).stat().st_size} for name in files}
    }
    write_json(ROOT / 'out/manifest.json', manifest)
    archive = ROOT / 'delivery-C-end-void.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as bundle:
        for name in files + ['manifest.json']:
            bundle.write(ROOT / 'out' / name, name)
        bundle.write(ROOT / 'README.md', 'README.md')
        for path in evidence_paths:
            bundle.write(ROOT / path, path)
        for entry in native['rotations']:
            bundle.write(ROOT / entry['world_capture'], entry['world_capture'])
        for name in ('four-views.png', 'exterior.png', 'plan.png', 'native-world-capture.png', 'delivery-sheet.png'):
            bundle.write(ROOT / 'preview' / name, 'preview/' + name)
    write_json(ROOT / 'qa/package.json', {'status': 'CREATED', 'path': str(archive), 'sha256': digest(archive), 'bytes': archive.stat().st_size, 'contains_runtime_jar': False, 'deployed': False})
    print(json.dumps({'status': report['status'], 'structure_sha256': fingerprint, 'captures': len(captures), 'delivery': str(ROOT / 'out'), 'package': str(archive)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
