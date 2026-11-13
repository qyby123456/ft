import hashlib
import json
import zipfile

from build_room import ROOT, digest, write_json


def main():
    report = json.loads((ROOT / 'out/qa.json').read_text(encoding='utf-8'))
    assert report['status'] == 'PASS' and digest(ROOT / 'out/room.nbt') == report['structure_sha256']
    files = [ROOT / name for name in ('README.md', '.gitignore', 'build.gradle', 'settings.gradle', 'gradle.properties')]
    for folder in ('tools', 'src', 'out', 'preview', 'reference'):
        files.extend(path for path in (ROOT / folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    files.extend(path for path in (ROOT / 'qa').iterdir() if path.is_file() and path.suffix in ('.json', '.nbt', '.log') and path.name not in ('package.json', 'file-manifest.json'))
    files = sorted(set(files), key=lambda path: path.relative_to(ROOT).as_posix())
    manifest = {'structure_sha256': report['structure_sha256'], 'scope': 'Independent A only; no Minecraft/Forge binaries, cache, test world or deployed mod', 'files': [{'path': path.relative_to(ROOT).as_posix(), 'bytes': path.stat().st_size, 'sha256': digest(path)} for path in files]}
    manifest_path = ROOT / 'qa/file-manifest.json'
    write_json(manifest_path, manifest)
    files.append(manifest_path)
    destination = ROOT / 'release/SHOP_A_RUINED-20261102.zip'
    destination.parent.mkdir(exist_ok=True)
    with zipfile.ZipFile(destination, 'w', zipfile.ZIP_DEFLATED, compresslevel=8) as archive:
        for path in files:
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(destination) as archive:
        assert archive.testzip() is None
        assert all(not name.startswith(('.gradle/', 'build/', 'qa/forge-run/')) and not name.endswith(('.class', '.jar', '.pyc')) for name in archive.namelist())
        for record in manifest['files']:
            assert hashlib.sha256(archive.read(record['path'])).hexdigest() == record['sha256']
    receipt = {'status': 'PASS', 'archive': str(destination), 'sha256': digest(destination), 'bytes': destination.stat().st_size, 'files': len(files), 'content_hashes_verified': True, 'excludes': ['.gradle', 'build', 'qa/forge-run', 'compiled verification mod', 'client.jar', 'production saves and mods']}
    write_json(ROOT / 'qa/package.json', receipt)
    print(json.dumps(receipt, ensure_ascii=False))


if __name__ == '__main__':
    main()
