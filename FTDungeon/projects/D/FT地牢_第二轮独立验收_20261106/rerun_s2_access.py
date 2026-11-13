import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import zipfile
from pathlib import Path

from audit_s8_submission import load


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def main():
    root = Path(sys.argv[1]).resolve()
    records = load(root / 'access-snapshot.json')['files']
    for item in records:
        assert digest(Path(item['snapshot'])) == item['sha256']
    base = root.parent / 'S2-submission-20261107-063435-165422'
    ready = load(base / 'ready-for-review.json')
    candidate = base / 'assembly/s2-desert-functional-combined.nbt'
    assert digest(candidate) == ready['assembly']['sha256']
    package = root / 'access-install-33ceb33f'
    archive = package / 's2_desert_access.zip'
    pack = package / 's2_desert_access'
    with zipfile.ZipFile(archive) as zipped:
        assert set(zipped.namelist()) == {path.relative_to(pack).as_posix() for path in pack.rglob('*') if path.is_file()}
        for name in zipped.namelist():
            assert zipped.read(name) == (pack / name).read_bytes()
        assert hashlib.sha256(zipped.read('data/s2access/structures/desert_33ceb33f.nbt')).hexdigest() == digest(candidate)
        assert json.loads(zipped.read('data/minecraft/tags/functions/load.json')) == {'values': ['s2access:load']}
        assert json.loads(zipped.read('data/minecraft/tags/functions/tick.json')) == {'values': ['s2access:tick']}
    scratch = root / 'independent-access-world'
    scratch.mkdir()
    runtime = scratch / 'runtime'
    runtime.mkdir()
    original = root / 'runtime-access-r4/native-test'
    project = runtime / 'native-test'
    project.mkdir()
    shutil.copytree(original / 'src', project / 'src')
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        shutil.copyfile(original / name, project / name)
    assembly = scratch / 'assembly'
    assembly.mkdir()
    shutil.copyfile(candidate, assembly / candidate.name)
    server = runtime / 'server'
    datapacks = server / 's2-new-isolated-qa-world/datapacks'
    datapacks.mkdir(parents=True)
    shutil.copyfile(archive, datapacks / archive.name)
    shutil.copyfile(root / 'runtime-access-r4/server/server.properties', server / 'server.properties')
    environment = dict(os.environ)
    environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    environment['PATH'] = str(Path(environment['JAVA_HOME']) / 'bin') + os.pathsep + environment['PATH']
    gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
    command = [gradle, '--offline', '--no-daemon', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
    with (scratch / 'run.log').open('wb') as stream:
        process = subprocess.Popen(command, cwd=project, env=environment, stdout=stream, stderr=subprocess.STDOUT)
        (scratch / 'progress.json').write_text(json.dumps({'stage': 'RUNNING', 'gradle_launcher_pid': process.pid, 'accepted': False}), encoding='utf-8')
        result = process.wait(timeout=900)
    assert result == 0, str(scratch / 'run.log')
    report = load(runtime / 'access-runtime.json')
    assert report['pass'] and report['source_sha256'] == digest(candidate)
    assert report['roundtrips'] == 2 and report['automatic_load_count'] >= 1
    assert report['ordinary_permission_level'] == 0 and not report['ordinary_has_permission_2']
    assert report['ordinary_install_denied'] and report['repeat_install_no_clear_or_replace']
    assert report['barriers_preserved'] == 38872 and report['installed_records_checked'] == 166642
    assert report['block_identity_failures'] == 0 and report['natural_tick_after'] > report['natural_tick_before_install'] > 0
    for item in records:
        assert digest(Path(item['snapshot'])) == item['sha256']
    summary = {'stage': 'PARENT_INDEPENDENT_FORGE_DATAPACK_INSTALL_ACCESS_REEXECUTION', 'status': 'PASS_SCOPED', 'source_sha256': digest(candidate), 'zip_sha256': digest(archive), 'frozen_files_unchanged': len(records), 'report': report, 'accepted': False, 'limits': ['Native ServerPlayer without real client; no human network UI certification', 'Main complete template packaged intact; fifteen <=48 slices remain separately delivered', 'Pre-reserved empty fixed overworld area; no asset-preserving install or arbitrary coordinate support']}
    (scratch / 'independent-result.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    (scratch / 'progress.json').write_text(json.dumps({'stage': 'PASS_SCOPED', 'accepted': False}), encoding='utf-8')
    print('S2 parent install and two permission-zero natural-tick roundtrips PASS', flush=True)


if __name__ == '__main__':
    main()
