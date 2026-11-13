import hashlib
import json
import subprocess
import sys
import zipfile
from datetime import datetime, timedelta, timezone
from pathlib import Path

from PIL import Image

from nbtio import Structure


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


def main():
    outputs = [ROOT / 'out' / name for name in ('room.nbt', 'room.json', 'blueprint.json', 'materials.json')]
    before = {path.name: digest(path) for path in outputs}
    subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, check=True)
    after = {path.name: digest(path) for path in outputs}
    require(before == after, 'Rebuild differs from native-tested deliverables')
    write(ROOT / 'qa/reproducibility.json', {'status': 'PASS', 'before': before, 'after': after, 'gzip_mtime': 0})
    subprocess.run([sys.executable, str(ROOT / 'tools/qa_static.py')], cwd=ROOT, check=True)
    metadata = load(ROOT / 'out/room.json')
    source = Structure.read(ROOT / 'out/room.nbt')
    fingerprint = digest(ROOT / 'out/room.nbt')
    static = load(ROOT / 'qa/static.json')
    native = load(ROOT / 'qa/native.json')
    visual = load(ROOT / 'qa/visual-review.json')
    render = load(ROOT / 'qa/render-manifest.json')
    require(static['status'] == native['status'] == visual['status'] == 'PASS', 'Required QA not PASS')
    require(metadata['sha256'] == static['structure_sha256'] == native['structure_sha256'] == visual['structure_sha256'] == render['source_nbt_sha256'] == fingerprint, 'QA fingerprints differ')
    require(native['stabilization_ticks'] == 100 and native['records_checked_after_ticks'] == 4 * len(source.blocks), 'Native coverage incomplete')
    require({entry['rotation'] for entry in native['rotations']} == {'NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90'}, 'Four rotations required')
    for entry in native['rotations']:
        capture_path = ROOT / entry['world_capture'].replace('\\', '/')
        require(capture_path.is_relative_to(ROOT) and digest(capture_path) == entry['world_capture_sha256'], 'Native capture file/hash mismatch')
        capture = Structure.read(capture_path)
        require(capture.size == source.size and len(capture.blocks) == 968, 'Native capture size/records differ')
        require(entry['block_entities_checked'] == len(source.block_entities) == 8 and entry['unexpected_world_entities'] == 0 and entry['feature_support_links_checked'] == 60, 'Native block entity/support/entity coverage differs')
        if entry['rotation'] == 'NONE':
            require(source.blocks == capture.blocks, 'Unrotated world states differ from delivered source')
    require(native['negative_missing_support_probes'] == 4 and native['negative_eye_vat_cage_support_probes'] == 8 and native['negative_blocked_aisle_probes'] == 4, 'Negative probes incomplete')
    log = (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace')
    require('MONSTER_SHOP_B_NATIVE_GAME_TEST_PASS:' in log and 'All 1 required tests passed' in log and 'BUILD SUCCESSFUL' in log, 'Native run log lacks success markers')
    require(not metadata['integration']['deployed'] and metadata['integration']['assigned_slot'] is None, 'Not an independent candidate')
    images = []
    for path in sorted((ROOT / 'preview').glob('*.png')) + [ROOT / 'out/preview.png']:
        with Image.open(path) as image:
            image.load()
            dimensions = list(image.size)
        images.append({'path': path.relative_to(ROOT).as_posix(), 'size': dimensions, 'sha256': digest(path)})
    require(len(images) >= 10 and render['native_captures'], 'Preview evidence incomplete')
    image_names = {entry['path'] for entry in images}
    require(all(name in image_names for name in visual['reviewed']), 'Visual report references missing image')
    qa = {
        'schema_version': 1, 'status': 'PASS', 'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(),
        'id': metadata['id'], 'variant': 'B', 'structure_sha256': fingerprint, 'structure_size': metadata['size'], 'record_count': len(source.blocks), 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'reference_sha256': metadata['reference_sha256'],
        'static': {'status': 'PASS', 'checks': len(static['checks']), 'report': 'qa/static.json'},
        'native': {'status': 'PASS', 'minecraft': native['minecraft'], 'forge': native['forge'], 'java': native['java'], 'rotations': 4, 'ticks': 100, 'records_after_ticks': native['records_checked_after_ticks'], 'block_entities_per_rotation': 8, 'support_links_per_rotation': 60, 'reachable_foot_positions_per_rotation': [entry['reachable_foot_positions'] for entry in native['rotations']], 'extra_world_entities': 0, 'negative_probes': 16, 'source_vs_unrotated_world_states': 'EXACT_EQUAL', 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log'},
        'visual': {'status': 'PASS', 'report': 'qa/visual-review.json', 'images': images},
        'reproducibility': {'status': 'PASS', 'report': 'qa/reproducibility.json'},
        'references': load(ROOT / 'reference/input-fingerprints.json'), 'integration': metadata['integration'],
        'boundary': 'Task writes only within independent B root; no edits to other rooms, core source, deployed JARs, image API settings or production saves.',
        'limitations': native['limitations'] + visual['limitations'],
    }
    write(ROOT / 'out/qa.json', qa)
    files = [ROOT / name for name in ('README.md', 'build.gradle', 'gradle.properties', 'settings.gradle')]
    for directory in ('out', 'preview', 'reference', 'tools', 'src'):
        files.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    files.extend(path for path in (ROOT / 'qa').iterdir() if path.is_file() and path.name not in ('file-manifest.json', 'package-verification.json'))
    files = sorted(set(files))
    manifest = {'structure_sha256': fingerprint, 'files': [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    manifest_path = ROOT / 'qa/file-manifest.json'
    write(manifest_path, manifest)
    files.append(manifest_path)
    package_path = ROOT / 'release/FT_Dungeon_Monster_Shop_B_Standalone_20261102.zip'
    with zipfile.ZipFile(package_path, 'w', zipfile.ZIP_DEFLATED, compresslevel=9) as package:
        for path in files:
            package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(package_path) as package:
        require(package.testzip() is None, 'ZIP CRC failure')
        for entry in manifest['files']:
            require(hashlib.sha256(package.read(entry['path'])).hexdigest() == entry['sha256'], 'Packaged file SHA mismatch: ' + entry['path'])
        require(not any('/forge-run/' in name or name.startswith(('build/', '.gradle/')) or name.endswith('.jar') for name in package.namelist()), 'Cache, world or JAR was packaged')
        require(all('out/' + name in package.namelist() for name in ('room.nbt', 'room.json', 'preview.png', 'qa.json')), 'Four-piece delivery incomplete')
    report = {'status': 'PASS', 'package': str(package_path), 'bytes': package_path.stat().st_size, 'sha256': digest(package_path), 'files': len(files), 'structure_sha256': fingerprint, 'checks': ['ZIP CRC', 'every packaged file SHA-256', 'four-piece deliverables', 'NBT matches native-tested fingerprint', 'no JAR, Gradle cache, test world or production world'], 'deployment': 'NOT_DEPLOYED_AWAITING_COORDINATOR'}
    write(ROOT / 'qa/package-verification.json', report)
    print(json.dumps(report, ensure_ascii=False))


if __name__ == '__main__':
    main()
