import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
frozen = workspace / 'preserved-review-snapshots/S8-vault-shell-revision-20261108'
destination = workspace / 's8-independent-vault-install-20261109'
assert not destination.exists()
destination.mkdir()
source = frozen / 'native-install-r1'
shutil.copytree(source / 'native-test', destination / 'native-test')
gradle = destination / 'native-test/build.gradle'
gradle.write_text(chr(10).join(line for line in gradle.read_text(encoding='utf-8').splitlines() if not line.startswith('layout.buildDirectory')),encoding='utf-8')
(destination / 'server').mkdir()
shutil.copyfile(source / 'server/server.properties',destination / 'server/server.properties')
candidate = frozen / 'assembly/s8-functional-vault-shell.nbt'
digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
assert digest == '763608faa6bc65fc85f8483912b7df6aafbbf84ee207a97ec2db0e3795c9775d'
shutil.copyfile(candidate,destination / 'candidate.nbt')
inputs = json.loads((source/'install-input.json').read_text(encoding='utf-8-sig'))
assert inputs['sha256'] == digest
inputs['candidate'] = str(destination/'candidate.nbt')
pack_files = []
for filename,folder in [('s8_access_revision_install.zip','s8_frozen_install'),('s8_admin_access.zip','s8_admin_access')]:
    package_path = frozen/filename
    assert package_path.read_bytes() == (source/filename).read_bytes()
    target = destination/'server/world/datapacks'/folder
    with zipfile.ZipFile(package_path) as package:
        assert package.testzip() is None
        for member in package.infolist():
            assert (target/member.filename).resolve().is_relative_to(target.resolve())
        package.extractall(target)
    for path in target.rglob('*'):
        if path.is_file():
            pack_files.append(dict(path=path.relative_to(destination).as_posix(),sha256=hashlib.sha256(path.read_bytes()).hexdigest(),bytes=path.stat().st_size))
inputs['frozen_pack_files'] = pack_files
(destination/'install-input.json').write_text(json.dumps(inputs,ensure_ascii=False,indent=2),encoding='utf-8')
environment = os.environ.copy()
environment['JAVA_HOME'] = 'C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME']+'/bin;'+environment['PATH']
command = ['C:/Users/YOUR_USERNAME/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false','runGameTestServer']
with (destination/'console.log').open('w',encoding='utf-8') as log:
    process = subprocess.Popen(command,cwd=destination/'native-test',env=environment,stdout=log,stderr=subprocess.STDOUT)
    progress = dict(status='RUNNING',pid=process.pid,directory=str(destination),source_sha256=digest,accepted=False)
    (frozen/'independent-install-run.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(progress,ensure_ascii=False),flush=True)
    code = process.wait()
progress.update(status='FINISHED',exit_code=code)
(frozen/'independent-install-run.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(progress,ensure_ascii=False),flush=True)
assert code == 0
