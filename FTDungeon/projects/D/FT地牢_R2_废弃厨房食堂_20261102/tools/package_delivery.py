import json
import zipfile

from build_r2 import ROOT, digest, write_json


def main():
    handoff = json.loads((ROOT / 'handoff.json').read_text(encoding='utf-8'))
    assert handoff['status'] == 'READY_FOR_INTEGRATION_NOT_DEPLOYED'
    assert handoff['room_nbt_sha256'] == digest(ROOT / 'out/room.nbt')
    paths = [ROOT / 'README.md', ROOT / 'handoff.json']
    for directory in ['out', 'qa', 'preview', 'tools', 'forge/src', 'forge/gradle']:
        paths.extend(path for path in (ROOT / directory).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
    paths.extend(ROOT / relative for relative in ['forge/build.gradle', 'forge/settings.gradle', 'forge/gradle.properties'])
    destination = ROOT / 'R2-abandoned-kitchen-delivery.zip'
    with zipfile.ZipFile(destination, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=6) as archive:
        for path in sorted(set(paths)):
            archive.write(path, path.relative_to(ROOT).as_posix())
    with zipfile.ZipFile(destination) as archive:
        assert archive.testzip() is None
        count = len(archive.namelist())
    receipt = {'status': 'PASS', 'path': str(destination), 'bytes': destination.stat().st_size, 'sha256': digest(destination), 'files': count, 'room_nbt_sha256': digest(ROOT / 'out/room.nbt'), 'production_deployed': False, 'excluded': ['Gradle build/cache directories', 'GameTest world save files', 'Python bytecode cache']}
    write_json(ROOT / 'package-receipt.json', receipt)
    print(json.dumps(receipt, ensure_ascii=False))


if __name__ == '__main__':
    main()
