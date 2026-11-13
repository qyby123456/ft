import hashlib
import json
import zipfile
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
ARCHIVE = ROOT / 'corridor-S09-delivery.zip'


def sha256(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    qa = json.loads((ROOT / 'qa.json').read_text(encoding='utf-8'))
    fingerprint = sha256(ROOT / 'room.nbt')
    if qa['status'] != 'PASS' or qa['sha256'] != fingerprint:
        raise ValueError('Only package the exact final PASS template')
    files = [ROOT / name for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json']]
    for directory in [ROOT / 'source', ROOT / 'qa', ROOT / 'views', ROOT / 'forge/src']:
        files.extend(path for path in directory.rglob('*') if path.is_file()
                     and '__pycache__' not in path.parts and path.name not in {'package-verification.json'})
    files.extend(ROOT / 'forge' / name for name in ['build.gradle', 'settings.gradle', 'gradle.properties'])
    files = sorted(set(files), key=lambda path: path.relative_to(ROOT).as_posix())
    entries = [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': sha256(path)} for path in files]
    manifest = {'id': 'corridor-S09', 'status': 'PASS', 'sha256': fingerprint,
                'archive': ARCHIVE.name, 'excludes': ['forge/build', 'forge/.gradle', 'forge/run', 'Python __pycache__'],
                'files': entries}
    manifest_path = ROOT / 'manifest.json'
    manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    with zipfile.ZipFile(ARCHIVE, 'w', zipfile.ZIP_DEFLATED, compresslevel=7) as bundle:
        for path in files + [manifest_path]:
            bundle.write(path, 'corridor-S09/' + path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(ARCHIVE) as bundle:
        if bundle.testzip() is not None:
            raise ValueError('ZIP CRC check failed')
        for entry in entries:
            if hashlib.sha256(bundle.read('corridor-S09/' + entry['path'])).hexdigest() != entry['sha256']:
                raise ValueError('ZIP hash mismatch: ' + entry['path'])
        if any('/forge/run/' in name or '/forge/build/' in name for name in bundle.namelist()):
            raise ValueError('Build cache or test world accidentally packaged')
    verification = {'id': 'corridor-S09', 'status': 'PASS', 'nbt_sha256': fingerprint,
                    'archive': ARCHIVE.name, 'archive_sha256': sha256(ARCHIVE),
                    'archive_bytes': ARCHIVE.stat().st_size, 'entries': len(entries) + 1,
                    'crc_and_all_file_hashes': 'PASS', 'production_world_or_build_cache_included': False}
    (ROOT / 'package-verification.json').write_text(json.dumps(verification, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
    print(json.dumps(verification))


if __name__ == '__main__':
    main()
