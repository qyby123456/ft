import hashlib
import json
import os
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
    prior = root.parent / 'S6-submission-20261107-075528-324589'
    manifest, previous = load(root / 'ready-for-review.json'), load(prior / 'ready-for-review.json')
    assert manifest['assembly']['sha256'] == previous['assembly']['sha256']
    assert all(room['id'] == old['id'] and room['sha256'] == old['sha256'] for room, old in zip(manifest['rooms'], previous['rooms']))
    records = {}
    for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
        records.update({item['source']: item for item in load(root / name)['files']})
    for item in records.values():
        assert digest(Path(item['snapshot'])) == item['sha256']
    relative_archive = 'assembly/access/fts6-access-mc1.20.1.zip'
    assert digest(root / relative_archive) == digest(prior / relative_archive)
    package = load(root / 'assembly/access/package.json')
    assert digest(root / relative_archive) == package['sha256']
    with zipfile.ZipFile(root / relative_archive) as archive:
        assert set(archive.namelist()) == {item['path'] for item in package['files']}
        for item in package['files']:
            payload = archive.read(item['path'])
            assert hashlib.sha256(payload).hexdigest() == item['sha256']
            assert payload == (root / 'assembly/access/datapack' / item['path']).read_bytes()
            if item['path'].startswith('data/'):
                assert payload == (root / 'qa/runtime/src/main/resources' / item['path']).read_bytes()
    scratch = root / 'independent-hooks-world-offline'
    scratch.mkdir()
    runtime = scratch / 'qa/runtime'
    runtime.mkdir(parents=True)
    shutil.copytree(root / 'qa/runtime/src', runtime / 'src')
    for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
        shutil.copyfile(root / 'qa/runtime' / name, runtime / name)
    assembly = scratch / 'assembly'
    assembly.mkdir()
    shutil.copyfile(root / 'assembly/s6-volcanic-connected.nbt', assembly / 's6-volcanic-connected.nbt')
    summary = load(root / 'assembly/summary.json')
    summary['nbtPath'] = str(assembly / 's6-volcanic-connected.nbt')
    (assembly / 'summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
    shutil.copytree(root / 'assembly/access', assembly / 'access')
    for phase in ('initial', 'restart'):
        (assembly / 'access' / ('hooks-' + phase + '-result.json')).unlink()
    environment = dict(os.environ)
    environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
    environment['PATH'] = str(Path(environment['JAVA_HOME']) / 'bin') + os.pathsep + environment['PATH']
    environment['PYTHONIOENCODING'] = 'utf-8'
    gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
    reports = []
    progress = scratch / 'progress.json'
    for phase in ('initial', 'restart'):
        progress.write_text(json.dumps({'stage': 'RUNNING', 'phase': phase, 'accepted': False}), encoding='utf-8')
        command = [gradle, '--offline', '--no-daemon', '--console=plain', '-Dnet.minecraftforge.gradle.check.certs=false', '-PqaNamespace=fts6hooks', '-PaccessPhase=' + phase, '-PqaWorkDir=world-parent-isolated', 'runGameTestServer']
        with (scratch / (phase + '.log')).open('wb') as stream:
            process = subprocess.Popen(command, cwd=runtime, env=environment, stdout=stream, stderr=subprocess.STDOUT)
            progress.write_text(json.dumps({'stage': 'RUNNING', 'phase': phase, 'gradle_launcher_pid': process.pid, 'accepted': False}), encoding='utf-8')
            result = process.wait(timeout=420)
        assert result == 0, str(scratch / (phase + '.log'))
        report = load(assembly / 'access' / ('hooks-' + phase + '-result.json'))
        assert report['status'] == 'PASS' and report['phase'] == phase
        assert report['source_sha256'] == manifest['assembly']['sha256']
        assert report['explicit_load_or_tick_calls'] == 0 and len(report['checks']) == 15
        assert report['natural_server_ticks_elapsed'] >= (150 if phase == 'initial' else 300)
        reports.append(report)
        print(phase, 'PASS', report['pid'], flush=True)
    assert reports[0]['pid'] != reports[1]['pid']
    for item in records.values():
        assert digest(Path(item['snapshot'])) == item['sha256']
    result = {'stage': 'PARENT_INDEPENDENT_ISOLATED_FORGE_HOOKS_REEXECUTION', 'frozen_files_unchanged': len(records), 'source_sha256': manifest['assembly']['sha256'], 'zip_sha256': package['sha256'], 'all_packaged_data_including_minecraft_tags_equals_test_resources': True, 'initial_and_restart': reports, 'same_world_restart': True, 'accepted': False, 'limitations': ['Forge GameTestServer registered FakePlayer with suppressed packet sends, not a real network client', 'No complete natural traversal or all container ray/use coverage proved by this access test']}
    (scratch / 'independent-result.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
    progress.write_text(json.dumps({'stage': 'PASS_SCOPED', 'accepted': False}), encoding='utf-8')
    print('Parent initial/restart hooks PASS; frozen inputs unchanged', flush=True)


if __name__ == '__main__':
    main()
