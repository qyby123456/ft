import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
allowed = workspace / 's8-maker-access-isolated'
manifest_path = allowed / 'access-followup-r1/FOLLOWUP-EVIDENCE.json'
payload = manifest_path.read_bytes()
manifest = json.loads(payload)
frozen = workspace / 'preserved-review-snapshots/S8-followup-20261107-1600'
frozen.mkdir()
records = []
for item in manifest['assets']:
    source = Path(item['path']).resolve()
    assert source.is_relative_to(allowed)
    content = source.read_bytes()
    assert len(content) == item['bytes'] and hashlib.sha256(content).hexdigest() == item['sha256'], source
    target = frozen / source.relative_to(allowed)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(content)
    records.append(dict(source=str(source), snapshot=str(target), sha256=item['sha256'], bytes=len(content)))
assert len(records) == 216 and manifest_path.read_bytes() == payload
for item in records:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest() == item['sha256']
(frozen / 'FOLLOWUP-EVIDENCE.json').write_bytes(payload)
(frozen / 'submission-snapshot.json').write_text(json.dumps(dict(accepted=False, files=records, manifest_sha256=hashlib.sha256(payload).hexdigest()), ensure_ascii=False, indent=2), encoding='utf-8')
candidate = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355/access-revision-r4/assembly/s8-functional-access-revision.nbt'
digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
assert digest == '6988e265a3fc6f89ce8a1b507088ffe8da664a38b28bcf8058b08eedc02d3fee'
environment = os.environ.copy()
environment['JAVA_HOME'] = 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + '/bin;' + environment['PATH']
command = ['C:/Users/YOUR_USERNAME/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat', '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
runs = []
for source_name, kind, input_name in [('access-followup-install-r1', 'installation', 'install-input.json'), ('access-followup-motion-r4', 'movement', 'motion-input.json')]:
    source = frozen / source_name
    destination = workspace / ('s8-independent-followup-' + kind + '-20261107-1600')
    destination.mkdir()
    shutil.copytree(source / 'native-test', destination / 'native-test')
    gradle = destination / 'native-test/build.gradle'
    gradle.write_text(chr(10).join(line for line in gradle.read_text(encoding='utf-8').splitlines() if not line.startswith('layout.buildDirectory')), encoding='utf-8')
    (destination / 'server').mkdir()
    shutil.copyfile(source / 'server/server.properties', destination / 'server/server.properties')
    shutil.copyfile(candidate, destination / 'candidate.nbt')
    inputs = json.loads((source / input_name).read_text(encoding='utf-8-sig'))
    assert inputs['sha256'] == digest
    inputs['candidate'] = str(destination / 'candidate.nbt')
    if kind == 'installation':
        pack_files = []
        for name, folder in [('s8_access_revision_install.zip', 's8_frozen_install'), ('s8_admin_access.zip', 's8_admin_access')]:
            archive = source / name
            canonical = candidate.parents[1] / name
            assert archive.read_bytes() == canonical.read_bytes()
            target = destination / 'server/world/datapacks' / folder
            with zipfile.ZipFile(archive) as package:
                assert package.testzip() is None
                for member in package.infolist():
                    assert (target / member.filename).resolve().is_relative_to(target.resolve())
                package.extractall(target)
            for path in target.rglob('*'):
                if path.is_file():
                    pack_files.append(dict(path=path.relative_to(destination).as_posix(), sha256=hashlib.sha256(path.read_bytes()).hexdigest(), bytes=path.stat().st_size))
        inputs['frozen_pack_files'] = pack_files
    (destination / input_name).write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding='utf-8')
    assert not (destination / 'server/world/level.dat').exists()
    with (destination / 'console.log').open('w', encoding='utf-8') as log:
        process = subprocess.Popen(command, cwd=destination / 'native-test', env=environment, stdout=log, stderr=subprocess.STDOUT)
        progress = dict(status='RUNNING', kind=kind, pid=process.pid, runtime=str(destination), source_sha256=digest, accepted=False)
        (frozen / ('independent-' + kind + '-run.json')).write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
        print(json.dumps(progress, ensure_ascii=False), flush=True)
        code = process.wait()
    for item in records:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
    progress.update(status='FINISHED', exit_code=code, frozen_files_unchanged=len(records))
    (frozen / ('independent-' + kind + '-run.json')).write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(progress, ensure_ascii=False), flush=True)
    assert code == 0
    runs.append(progress)
(frozen / 'independent-runs.json').write_text(json.dumps(runs, ensure_ascii=False, indent=2), encoding='utf-8')
