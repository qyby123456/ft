import hashlib
import json
import py_compile
import shutil
import subprocess
import sys
import tempfile
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from nbtio import AIR, Structure, integer


ROOT = Path(__file__).resolve().parents[1]


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def write(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def require(condition, message):
    if not condition:
        raise ValueError(message)


def preload_table_stock(room):
    room.block_entities[(4, 1, 6)].value['RemainingStock'] = integer(1)


def main():
    subprocess.run([sys.executable, str(ROOT / 'tools/build_integration_patch.py')], cwd=ROOT, check=True)
    built = [ROOT / 'out' / name for name in ('room.nbt', 'room.json', 'blueprint.json', 'materials.json')]
    before = {path.name: digest(path) for path in built}
    subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, check=True)
    after = {path.name: digest(path) for path in built}
    require(before == after, 'Repeated deterministic build differs from the native-tested outputs')
    write(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'before': before, 'after': after, 'gzip_mtime': 0})
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], cwd=ROOT, check=True)
    scripts = sorted((ROOT / 'tools').glob('*.py'))
    for script in scripts:
        py_compile.compile(str(script), doraise=True)
    write(ROOT / 'qa/python-syntax.json', {'status': 'PASS', 'scripts': [script.relative_to(ROOT).as_posix() for script in scripts]})
    original_static = load(ROOT / 'qa/static.json')
    mutation_cases = [
        ('blocked_main_aisle', lambda room: room.set((5, 1, 2), room.get((0, 0, 0))), 'entrance_to_counter_3_wide_3_high'),
        ('missing_floor', lambda room: room.blocks.pop((5, 0, 3)), 'full_solid_floor'),
        ('missing_roof', lambda room: room.blocks.pop((5, 7, 3)), 'complete_roof'),
        ('missing_lamp_chain', lambda room: room.blocks.pop((4, 5, 3)), 'two_chained_lamps_over_aisle_not_counter'),
        ('missing_trading_table_half', lambda room: room.set((5, 1, 6), AIR), 'original_trading_table_complete_pair_unbound_zero_stock'),
        ('preloaded_trading_stock', preload_table_stock, 'original_trading_table_complete_pair_unbound_zero_stock'),
    ]
    mutation_results = []
    with tempfile.TemporaryDirectory(prefix='ftshop-static-', dir=ROOT / 'qa') as temporary:
        sandbox = Path(temporary)
        require(sandbox.resolve().is_relative_to((ROOT / 'qa').resolve()), 'Temporary cleanup target must remain inside this independent QA workspace')
        (sandbox / 'out').mkdir()
        (sandbox / 'qa').mkdir()
        (sandbox / 'reference').mkdir()
        (sandbox / 'dependencies').mkdir()
        for dependency in (ROOT / 'dependencies').glob('*.jar'):
            shutil.copy2(dependency, sandbox / 'dependencies' / dependency.name)
        (sandbox / 'reference/input-fingerprints.json').write_bytes((ROOT / 'reference/input-fingerprints.json').read_bytes())
        for reference in (ROOT / 'reference').glob('*.png'):
            shutil.copy2(reference, sandbox / 'reference' / reference.name)
        for name in ('room.json', 'blueprint.json'):
            (sandbox / 'out' / name).write_bytes((ROOT / 'out' / name).read_bytes())
        for name, mutate, detection in mutation_cases:
            candidate = Structure.read(ROOT / 'out/room.nbt')
            mutate(candidate)
            candidate.write(sandbox / 'out/room.nbt')
            metadata_mutated = load(ROOT / 'out/room.json')
            metadata_mutated['sha256'] = digest(sandbox / 'out/room.nbt')
            write(sandbox / 'out/room.json', metadata_mutated)
            command = "import sys; from pathlib import Path; sys.path.insert(0, sys.argv[1]); import qa_static; qa_static.ROOT=Path(sys.argv[2]); qa_static.main()"
            result = subprocess.run([sys.executable, '-c', command, str(ROOT / 'tools'), str(sandbox)], capture_output=True, text=True, encoding='utf-8', errors='replace')
            if not (sandbox / 'qa/static.json').exists():
                raise ValueError('Mutation QA failed before producing its report: ' + result.stderr)
            failed = load(sandbox / 'qa/static.json')
            require(result.returncode == 1 and any(check['name'] == detection and check['status'] == 'FAIL' for check in failed['checks']), 'Negative static test was not detected: ' + name)
            mutation_results.append({'mutation': name, 'expected_failed_check': detection, 'detected': True})
    require(load(ROOT / 'qa/static.json') == original_static, 'Negative tests modified the authoritative static report')
    write(ROOT / 'qa/static-negative-tests.json', {'status': 'PASS', 'tests': mutation_results, 'method': 'Mutated copies of the NBT with matching candidate hashes; authoritative delivery remains untouched.'})

    metadata = load(ROOT / 'out/room.json')
    static = load(ROOT / 'qa/static.json')
    native = load(ROOT / 'qa/native.json')
    visual = load(ROOT / 'qa/visual-review.json')
    render = load(ROOT / 'qa/render-manifest.json')
    fingerprint = digest(ROOT / 'out/room.nbt')
    require(static['status'] == native['status'] == visual['status'] == 'PASS', 'Required QA is not PASS')
    require(metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == render['source_nbt_sha256'] == fingerprint, 'QA/source fingerprints differ')
    require(visual['structure_sha256'] == fingerprint, 'Visual review is from a different source')
    dependency = metadata['dependencies'][0]
    dependency_path = 'dependencies/' + dependency['file']
    require(metadata['room_type'] == 'shop' and metadata['spawn_points'] is False, 'Shop must disable spawn points')
    require(digest(ROOT / dependency_path) == dependency['sha256'] == render['trading_mod_jar_sha256'], 'Original trading dependency differs')
    require(native['candidate_core_spawn_policy_cases'] == 5 and native['candidate_core_layout_trade_allowlist_validation'] == 'PASS' and native['candidate_core_layout_negative_cases'] == 2, 'Candidate policy/layout probes incomplete')
    require(native['candidate_patch_applied_to_shared_core'] is False and metadata['integration']['ready_for_unpatched_core'] is False, 'Independent candidate must not claim core deployment')
    for relative, tested_hash in native['tested_input_sha256'].items():
        require(digest(ROOT / relative) == tested_hash, 'Native-tested input has changed: ' + relative)
    patch_manifest = load(ROOT / 'integration/patch-manifest.json')
    require(patch_manifest['patch_roundtrip_on_snapshot'] == 'PASS' and digest(ROOT / 'integration/shop-import.patch') == patch_manifest['patch_sha256'], 'Integration patch is not snapshot-verified')
    for item in patch_manifest['files']:
        require(digest(ROOT / item['baseline']) == item['before_sha256'] and digest(ROOT / item['candidate']) == item['after_sha256'], 'Patch snapshot fingerprint differs')
    require(len(native['rotations']) == 4 and native['records_checked_after_ticks'] == 3872 and native['stabilization_ticks'] == 100, 'Native test coverage incomplete')
    require({item['rotation'] for item in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}, 'Four rotations required')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    for marker in ('SHOP_NATIVE_GAME_TEST_PASS: rotations=4/4, records=3872, ticks=100, stock=0, trading=1.2.0, spawnPolicy=excluded, source=' + fingerprint, 'All 1 required tests passed', 'BUILD SUCCESSFUL'):
        require(marker in log, 'Missing final test/build success marker: ' + marker)
    require('required tests failed' not in log and 'BUILD FAILED' not in log, 'Final log contains failure')
    for item in native['rotations']:
        capture = ROOT / item['world_capture']
        require(digest(capture) == item['world_capture_sha256'], 'Native capture fingerprint differs')
        require(item['unexpected_world_entities'] == 0 and item['block_entities_checked'] == 11 and item['reachable_foot_positions'] == 63, 'Unexpected native fixture contents')
        require(item['state_collision_survival_inventory'] == 'PASS' and item['records_checked'] == 968, 'Native state audit incomplete')
        require(item['original_trading_table_pair_shared_api_unbound_zero_stock'] == 'PASS', 'Original trading pair API audit incomplete')
    original = Structure.read(ROOT / 'out/room.nbt')
    recovered = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    require(original.size == recovered.size and original.blocks == recovered.blocks and not recovered.entities, 'Unrotated world capture differs from source states')
    image_records = []
    for path in sorted((ROOT / 'preview').glob('*.png')) + [ROOT / 'out/preview.png']:
        with Image.open(path) as image:
            image.load()
            dimensions = list(image.size)
        image_records.append({'path': path.relative_to(ROOT).as_posix(), 'size': dimensions, 'sha256': digest(path)})
    require(len(image_records) >= 10 and render['native_captures'], 'Expected source and native-world previews missing')
    for relative in visual['reviewed']:
        require((ROOT / relative).is_file(), 'Visual review references missing image')
    report = {
        'schema_version': 2,
        'status': 'PASS',
        'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'id': metadata['id'],
        'structure_sha256': fingerprint,
        'structure_size': metadata['size'],
        'record_count': 968,
        'entrance': metadata['entrance'],
        'interior': metadata['interior'],
        'static': {'status': 'PASS', 'checks': len(static['checks']), 'report': 'qa/static.json'},
        'native': {'status': 'PASS', 'minecraft': native['minecraft'], 'forge': native['forge'], 'java': native['java'], 'rotations': 4, 'records_after_100_ticks': 3872, 'block_entities_per_rotation': 11, 'reachable_foot_positions_per_rotation': 63, 'extra_world_entities': 0, 'missing_support_negative_probes': 4, 'blocked_path_negative_probes': 4, 'source_vs_unrotated_world_states': 'EXACT_EQUAL', 'original_trading_table_pair_api': 'PASS', 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log'},
        'shop_policy': {'room_type': 'shop', 'spawn_points': False, 'candidate_parser_cases': 5, 'candidate_layout_negative_cases': 2, 'core_patch': 'integration/shop-import.patch', 'patch_snapshot_roundtrip': 'PASS', 'applied_to_shared_core': False, 'full_spawn_handler_runtime_tested': False},
        'dependencies': metadata['dependencies'],
        'visual': {'status': 'PASS', 'report': 'qa/visual-review.json', 'images': image_records},
        'reproducibility': {'status': 'PASS', 'report': 'qa/reproducibility.json', 'static_negative_tests': 'qa/static-negative-tests.json', 'python_syntax': 'qa/python-syntax.json'},
        'references': load(ROOT / 'reference/input-fingerprints.json'),
        'integration': metadata['integration'],
        'boundary': 'Independent directory only; nine-room revisions, third-batch queue, core, runtime mods and production saves were not modified by this task.',
        'limitations': native['limitations'] + visual['limitations'] + patch_manifest['limitations'],
    }
    write(ROOT / 'out/qa.json', report)
    files = [ROOT / name for name in ('README.md', 'build.gradle', 'gradle.properties', 'settings.gradle')]
    for directory in ('out', 'preview', 'reference', 'tools', 'src', 'dependencies', 'integration'):
        files.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    qa_evidence = ('static.json', 'native.json', 'forge-gametest.log', 'visual-review.json', 'render-manifest.json', 'reproducibility.json', 'python-syntax.json', 'static-negative-tests.json')
    files.extend(ROOT / 'qa' / name for name in qa_evidence)
    files.extend((ROOT / 'qa').glob('native-placed-*.nbt'))
    files = sorted(set(files))
    manifest = {'structure_sha256': fingerprint, 'files': [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    manifest_file = ROOT / 'qa/file-manifest.json'
    write(manifest_file, manifest)
    files.append(manifest_file)
    release = ROOT / 'release/FT_Dungeon_Supply_Shop_Trading_NoSpawns_20261102.zip'
    release.parent.mkdir(parents=True, exist_ok=True)
    with zipfile.ZipFile(release, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as package:
        for path in files:
            package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(release) as package:
        require(package.testzip() is None, 'Package CRC failure')
        require(hashlib.sha256(package.read('out/room.nbt')).hexdigest() == fingerprint, 'Packaged NBT fingerprint mismatch')
        for item in manifest['files']:
            require(hashlib.sha256(package.read(item['path'])).hexdigest() == item['sha256'], 'Packaged file mismatch: ' + item['path'])
        require(not any('/forge-run/' in name or name.startswith(('build/', '.gradle/', 'baseline-v1/')) or name.endswith('.jar') and name != dependency_path for name in package.namelist()), 'Unapproved JAR/cache/world accidentally packaged')
        require(hashlib.sha256(package.read(dependency_path)).hexdigest() == dependency['sha256'], 'Packaged original trading JAR differs')
    result = {'status': 'PASS', 'package': str(release), 'bytes': release.stat().st_size, 'sha256': digest(release), 'files': len(files), 'structure_sha256': fingerprint, 'checks': ['ZIP CRC', 'all file SHA-256', 'packaged NBT matches tested source', 'only original trading 1.2.0 dependency JAR included', 'no cache or test/production world', 'candidate core patch verified on snapshot but not deployed']}
    write(ROOT / 'qa/package-verification.json', result)
    print(json.dumps(result, ensure_ascii=False))


if __name__ == '__main__':
    main()
