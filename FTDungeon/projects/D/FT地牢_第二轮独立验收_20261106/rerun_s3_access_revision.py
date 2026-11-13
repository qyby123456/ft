import hashlib
import json
import os
import shutil
import subprocess
from pathlib import Path

workspace = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root = workspace / 'preserved-review-snapshots/S3-access-revision-20261107-1103'
snapshot = json.loads((root / 'submission-snapshot.json').read_text(encoding='utf-8'))
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
destination = workspace / 's3-independent-access-revision-20261107-1106'
destination.mkdir()
runtime = destination / 'accessqa-r1'
project = runtime / 'native-test'
for directory in ('rooms','deployment'):
    shutil.copytree(root / directory, destination / directory)
shutil.copytree(root / 'accessqa-r1/native-test/src', project / 'src')
for name in ('build.gradle','gradle.properties','settings.gradle'):
    shutil.copyfile(root / 'accessqa-r1/native-test' / name, project / name)
(runtime / 'server').mkdir()
shutil.copyfile(root / 'accessqa-r1/server/server.properties', runtime / 'server/server.properties')
shutil.copytree(root / 'deployment/datapack', runtime / 'server/world/datapacks/fts3')
assert not (runtime / 'server/world/level.dat').exists()
environment = os.environ.copy()
environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + r'\bin;' + environment['PATH']
gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
command = [gradle, '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
pointer = root / 'independent-run.json'
with (runtime / 'console.log').open('w', encoding='utf-8') as log:
    process = subprocess.Popen(command, cwd=project, env=environment, stdout=log, stderr=subprocess.STDOUT)
    progress = dict(status='RUNNING', pid=process.pid, runtime=str(runtime), accepted=False)
    pointer.write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(progress, ensure_ascii=False), flush=True)
    code = process.wait()
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
progress.update(status='FINISHED', exit_code=code, frozen_files_unchanged=len(snapshot['files']))
pointer.write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(progress, ensure_ascii=False), flush=True)
raise SystemExit(code)
