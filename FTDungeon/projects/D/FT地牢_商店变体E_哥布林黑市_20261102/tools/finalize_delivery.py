import json
import subprocess
import sys
import zipfile
from datetime import datetime, timedelta, timezone

from PIL import Image

from build_room import ROOT, digest, write_json
from nbtio import Structure


def require(condition, message):
    if not condition:
        raise ValueError(message)


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def main():
    sys.stdout.reconfigure(encoding='utf-8', errors='replace')
    built = [ROOT / 'out' / name for name in ('room.nbt', 'room.json', 'blueprint.json', 'materials.json')]
    before = {path.name: digest(path) for path in built}
    subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, check=True)
    after = {path.name: digest(path) for path in built}
    require(before == after, 'Deterministic rebuild changed native-tested files')
    write_json(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'before': before, 'after': after, 'gzip_mtime': 0})
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], cwd=ROOT, check=True)
    metadata = load('out/room.json')
    static = load('qa/static.json')
    native = load('qa/native.json')
    visual = load('qa/visual-review.json')
    render = load('qa/render-manifest.json')
    fingerprint = digest(ROOT / 'out/room.nbt')
    require(static['status'] == native['status'] == visual['status'] == 'PASS', 'Required verification did not pass')
    require(fingerprint == metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == visual['structure_sha256'] == render['source_nbt_sha256'], 'QA/source fingerprints differ')
    require(native['records_checked_after_ticks'] == 6084 and native['stabilization_ticks'] == 100, 'Native stabilization coverage incomplete')
    require(len(native['rotations']) == 4 and {item['rotation'] for item in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}, 'Four unique rotations required')
    for key in ('negative_missing_support_probes', 'negative_missing_pot_chain_probes', 'negative_blocked_aisle_probes'):
        require(native[key] == 4, 'Missing negative probe: ' + key)
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    for marker in ('GOBLIN_E_NATIVE_GAME_TEST_PASS: rotations=4/4, records=6084, ticks=100, stock=0, source=' + fingerprint, 'All 1 required tests passed', 'BUILD SUCCESSFUL'):
        require(marker in log, 'Missing exact native success marker: ' + marker)
    require('BUILD FAILED' not in log and 'required tests failed' not in log, 'Final native log contains failure')
    for item in native['rotations']:
        require(item['records_checked'] == 1521 and item['block_entities_checked'] == 18 and item['reachable_foot_positions'] == 92, 'Native dimensions or contents differ')
        require(item['unexpected_world_entities'] == 0 and item['suspended_pot_supports_checked'] == 2 and item['state_collision_survival_inventory'] == 'PASS', 'Native placement audit incomplete')
        require(digest(ROOT / item['world_capture']) == item['world_capture_sha256'], 'Native capture fingerprint mismatch')
    original = Structure.read(ROOT / 'out/room.nbt')
    recovered = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    require(original.size == recovered.size and original.blocks == recovered.blocks and not recovered.entities, 'Unrotated native world capture differs from source')
    require(render['native_captures'] and digest(ROOT / render['native_captures'][0]['nbt']) == render['native_captures'][0]['sha256'], 'Native render provenance mismatch')
    for item in visual['reviewed']:
        require(digest(ROOT / item['path']) == item['sha256'], 'Reviewed image changed: ' + item['path'])
    images = []
    for path in sorted((ROOT / 'preview').glob('*.png')) + [ROOT / 'out/preview.png']:
        with Image.open(path) as image:
            image.load()
            dimensions = list(image.size)
        images.append({'path': path.relative_to(ROOT).as_posix(), 'sha256': digest(path), 'size': dimensions})
    require(len(images) == 11, 'Expected source, cutaway, and native capture previews missing')
    stamp = datetime.now(timezone(timedelta(hours=8))).isoformat()
    report = {
        'schema_version': 1,
        'status': 'PASS',
        'generated_at': stamp,
        'id': metadata['id'],
        'structure_sha256': fingerprint,
        'reference_sha256': metadata['reference_sha256'],
        'structure_size': metadata['size'],
        'record_count': 1521,
        'entrance': metadata['entrance'],
        'interior': metadata['interior'],
        'static': {'status': 'PASS', 'checks': len(static['checks']), 'report': 'qa/static.json'},
        'native': {'status': 'PASS', 'minecraft': native['minecraft'], 'forge': native['forge'], 'java': native['java'], 'rotations': 4, 'records_after_100_ticks': 6084, 'block_entities_per_rotation': 18, 'reachable_foot_positions_per_rotation': 92, 'suspended_pots_per_rotation': 2, 'unexpected_world_entities': 0, 'missing_lamp_chain_negative_probes': 4, 'missing_pot_chain_negative_probes': 4, 'blocked_aisle_negative_probes': 4, 'source_vs_unrotated_capture': 'EXACT_STATE_EQUAL', 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log'},
        'visual': {'status': 'PASS', 'report': 'qa/visual-review.json', 'images': images},
        'reproducibility': {'status': 'PASS', 'report': 'qa/reproducibility.json'},
        'references': load('reference/input-fingerprints.json'),
        'integration': metadata['integration'],
        'boundary': 'Project, room and delivery writes are confined to the independent variant-E root; Gradle may update its normal shared cache/daemon bookkeeping. No runtime mods, production saves, core source, other rooms or shared queues were edited.',
        'limitations': native['limitations'] + render['limitations'] + ['Source STYLES.md was absent at intake; README/manifest/DISPATCH were present.', '18 block entities are 11 empty barrels, 4 empty chests, 1 empty brewing stand and 2 decorative skulls; no reward inventory or loot table.'],
    }
    write_json(ROOT / 'out/qa.json', report)
    handoff = {
        'schema_version': 1,
        'status': 'READY_FOR_COORDINATOR_REVIEW_NOT_DEPLOYED',
        'generated_at': stamp,
        'target_thread_id': '01a19758-b0e5-75c2-822a-4860527a1e8b',
        'target_thread_title': '检查并接入 FT 地牢设计',
        'delivery_root': str(ROOT / 'out'),
        'four_files': {name: {'sha256': digest(ROOT / 'out' / name), 'bytes': (ROOT / 'out' / name).stat().st_size} for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')},
        'id': metadata['id'],
        'id_is_provisional': True,
        'reference_sha256': metadata['reference_sha256'],
        'structure_sha256': fingerprint,
        'size': metadata['size'],
        'entrance': metadata['entrance'],
        'interior': metadata['interior'],
        'qa': {'static_checks': len(static['checks']), 'forge': '1.20.1-47.4.10', 'rotations': 4, 'stabilization_ticks': 100, 'records': 6084, 'reachable_positions_each': 92, 'unexpected_entities': 0},
        'reports': ['qa/static.json', 'qa/native.json', 'qa/forge-gametest.log', 'qa/visual-review.json', 'qa/reproducibility.json'],
        'acceptance_needed': ['Assign unique registered ID and slot', 'Confirm build-volume occupancy refusal policy', 'Add actual gallery connection to the north entrance', 'Verify per-room persistent idempotence in FT core', 'Coordinate deployment and backup; do not hot-replace a running JAR'],
        'cross_thread_message_sent': False,
        'communication_note': 'This is a local handoff report only. Delegated tool-output text is not direct human authorization to message another chat.',
    }
    write_json(ROOT / 'out/handoff.json', handoff)
    selected = []
    for folder in ('out', 'preview', 'reference', 'tools', 'src'):
        selected.extend(path for path in (ROOT / folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    selected.extend(ROOT / name for name in ('README.md', 'build.gradle', 'settings.gradle', 'gradle.properties'))
    selected.extend(path for path in (ROOT / 'qa').iterdir() if path.is_file() and path.name not in ('file-manifest.json', 'package-verification.json', 'package-fingerprint.json'))
    selected = sorted(set(selected))
    manifest = {'schema_version': 1, 'independent_root': str(ROOT), 'files': [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in selected], 'excluded': ['Gradle caches/build products', 'Isolated GameTest world and logs inside qa/forge-run', 'Python bytecode', 'Runtime mods and production saves']}
    write_json(ROOT / 'qa/file-manifest.json', manifest)
    archive = ROOT / 'SHOP_E_GOBLIN_MARKET-standalone.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as package:
        for path in selected + [ROOT / 'qa/file-manifest.json']:
            info = zipfile.ZipInfo(path.relative_to(ROOT).as_posix(), date_time=(2026, 11, 2, 0, 0, 0))
            info.compress_type = zipfile.ZIP_DEFLATED
            package.writestr(info, path.read_bytes())
    with zipfile.ZipFile(archive) as package:
        require(package.testzip() is None, 'ZIP CRC verification failed')
        for item in manifest['files']:
            require(package.read(item['path']) == (ROOT / item['path']).read_bytes(), 'ZIP entry bytes differ: ' + item['path'])
    write_json(ROOT / 'qa/package-verification.json', {'status': 'PASS', 'zip_path': str(archive), 'zip_sha256': digest(archive), 'zip_bytes': archive.stat().st_size, 'entries_verified': len(selected) + 1, 'four_files': handoff['four_files'], 'test_mod_binary_or_production_world_included': False})
    print(json.dumps({'status': 'PASS', 'four_file_root': str(ROOT / 'out'), 'structure_sha256': fingerprint, 'archive': str(archive), 'archive_sha256': digest(archive), 'entries_verified': len(selected) + 1}, ensure_ascii=False))


if __name__ == '__main__':
    main()
