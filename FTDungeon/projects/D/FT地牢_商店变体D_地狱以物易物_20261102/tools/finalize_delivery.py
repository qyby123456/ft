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


def require(condition, message):
    if not condition:
        raise RuntimeError(message)


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def load(relative):
    return json.loads((ROOT / relative).read_text(encoding='utf-8'))


def save(relative, value):
    (ROOT / relative).write_text(json.dumps(value, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')


def files_for_package():
    result = [ROOT / name for name in ('README.md', 'HANDOFF.md', 'build.gradle', 'settings.gradle', 'gradle.properties')]
    for folder in ('tools', 'src', 'out', 'preview', 'reference'):
        result.extend(path for path in (ROOT / folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    result.extend(path for path in (ROOT / 'qa').glob('*') if path.is_file() and path.name not in {'file-manifest.json', 'package-verification.json'})
    return sorted(result, key=lambda path: path.relative_to(ROOT).as_posix())


def main():
    metadata = load('out/room.json')
    fingerprint = digest(ROOT / 'out/room.nbt')
    require(fingerprint == metadata['sha256'], 'Source metadata mismatch')
    for relative in ('qa/static.json', 'qa/native.json'):
        report = load(relative)
        require(report['status'] == 'PASS' and report['structure_sha256'] == fingerprint, 'Failed or stale report: ' + relative)
    native = load('qa/native.json')
    require(len(native['rotations']) == 4 and native['stabilization_ticks'] == 200, 'Native rotation coverage incomplete')
    require(all(entry['records_checked'] == 1521 and entry['block_entities_checked'] == 6 and entry['unexpected_world_entities'] == 0 and entry['sealed_magma_blocks'] == 2 for entry in native['rotations']), 'Native placement metrics incomplete')
    require('All 1 required tests passed' in (ROOT / 'qa/forge-gametest.log').read_text(encoding='utf-8', errors='replace'), 'Native success log missing')
    for entry in native['rotations']:
        require(digest(ROOT / entry['world_capture']) == entry['world_capture_sha256'], 'World capture fingerprint mismatch')
    visual = load('qa/visual-review.json')
    require(visual['status'] == 'PASS' and visual['source_nbt_sha256'] == fingerprint, 'Visual evidence stale')
    render = load('qa/render-manifest.json')
    require(render['source_nbt_sha256'] == fingerprint and len(render['native_captures']) == 1, 'Render evidence stale or missing native capture')
    images = []
    for path in sorted((ROOT / 'preview').glob('*.png')) + [ROOT / 'out/preview.png']:
        with Image.open(path) as image:
            image.load()
            images.append({'path': path.relative_to(ROOT).as_posix(), 'size': list(image.size), 'sha256': digest(path)})
    reference = ROOT / 'reference/shop-variant-D-nether-barter.png'
    require(digest(reference) == metadata['reference_sha256'] == digest(Path(metadata['reference'])), 'Reference fingerprint changed')
    with Image.open(reference) as image:
        image.load()
        require(image.size == (1536, 1024), 'Reference size mismatch')
    baseline = {name: digest(ROOT / 'out' / name) for name in ('room.nbt', 'room.json', 'blueprint.json', 'materials.json')}
    runs = []
    for iteration in range(2):
        subprocess.run([sys.executable, str(ROOT / 'tools/build_room.py')], cwd=ROOT, check=True)
        hashes = {name: digest(ROOT / 'out' / name) for name in baseline}
        require(hashes == baseline, 'Nondeterministic build or source changed')
        runs.append({'iteration': iteration + 1, 'sha256': hashes})
    source = Structure.read(ROOT / 'out/room.nbt')
    placed = Structure.read(ROOT / 'qa/native-placed-none.nbt')
    require(source.blocks == placed.blocks and source.size == placed.size, 'Native world states differ from source')
    save('qa/reproducibility.json', {'status': 'PASS', 'source_sha256': fingerprint, 'rebuilds': runs, 'unrotated_native_world_states': 'EXACT_EQUAL', 'records_compared': len(source.blocks)})
    summary = {
        'status': 'PASS', 'generated_at': datetime.now(timezone(timedelta(hours=8))).isoformat(), 'id': metadata['id'],
        'structure_sha256': fingerprint, 'structure_size': metadata['size'], 'records': 1521, 'entrance': metadata['entrance'], 'interior': metadata['interior'],
        'reference': {'source': metadata['reference'], 'sha256': metadata['reference_sha256'], 'actual_size': [1536, 1024], 'source_styles_md_present': False},
        'static': {'status': 'PASS', 'report': 'qa/static.json', 'checks': 18, 'negative_probes': 8},
        'native': {'status': 'PASS', 'report': 'qa/native.json', 'log': 'qa/forge-gametest.log', 'minecraft': '1.20.1', 'forge': '47.4.10',
                   'rotations': 4, 'stabilization_ticks': 200, 'records_after_ticks': 6084, 'reachable_foot_positions_per_rotation': 94,
                   'block_entities_per_rotation': 6, 'extra_world_entities': 0, 'negative_probes': 12, 'source_vs_unrotated_world_states': 'EXACT_EQUAL'},
        'visual': {'status': 'PASS', 'report': 'qa/visual-review.json', 'render_manifest': 'qa/render-manifest.json', 'images': images},
        'reproducibility': {'status': 'PASS', 'report': 'qa/reproducibility.json'}, 'integration': metadata['integration'],
        'gameplay': metadata['gameplay'],
        'limitations': ['Independent structure candidate only; no registered FT catalog ID/slot, core source edit or deployment.',
                        'No full modpack client, multiplayer, FPS, player screenshot or trade/reward verification.',
                        'No live-world clearing, hallway, occupancy refusal, idempotence or persistence test; coordinator must validate integration.',
                        'Soul-fire visual uses safe soul lanterns; lava visual uses sealed magma and safe full-block shroomlight floor.',
                        'Software render uses native models/textures with approximate skull appearance and transparency; no native lighting.'],
        'provenance': {'copied_utilities_from': 'D:/ft工程文件/FT地牢_补给商店_独立交付_20261102', 'source_project_modified': False,
                       'nbtio_sha256': digest(ROOT / 'tools/nbtio.py'), 'source_styles_md_note': 'Mentioned by source README but file absent; source files left untouched.'}
    }
    save('out/qa.json', summary)
    files = files_for_package()
    manifest = {'structure_sha256': fingerprint, 'files': [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    save('qa/file-manifest.json', manifest)
    archive = ROOT / 'release/SHOP_D_NETHER_BARTER-standalone-20261102.zip'
    with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED, compresslevel=6) as package:
        for path in files + [ROOT / 'qa/file-manifest.json']:
            package.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(archive) as package:
        require(package.testzip() is None, 'ZIP CRC verification failed')
        for entry in manifest['files']:
            require(hashlib.sha256(package.read(entry['path'])).hexdigest() == entry['sha256'], 'ZIP member fingerprint mismatch: ' + entry['path'])
        require({'out/room.nbt', 'out/room.json', 'out/preview.png', 'out/qa.json'}.issubset(package.namelist()), 'Four-piece delivery incomplete')
        require(not any(name.endswith('.jar') or name.startswith(('build/', '.gradle/', 'qa/forge-run/')) for name in package.namelist()), 'Runtime or world leaked into package')
    save('qa/package-verification.json', {'status': 'PASS', 'archive': str(archive), 'archive_sha256': digest(archive), 'bytes': archive.stat().st_size,
                                          'members_verified': len(manifest['files']) + 1, 'crc': 'PASS', 'four_piece_present': True, 'structure_sha256': fingerprint})
    print(json.dumps({'status': 'PASS', 'archive': str(archive), 'archive_sha256': digest(archive), 'members': len(manifest['files']) + 1}, ensure_ascii=False))


if __name__ == '__main__':
    main()
