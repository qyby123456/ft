import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path


snapshot = Path(sys.argv[1]).resolve()
fixture = sys.argv[2]
assert fixture in ('worldqa-r3', 'ladderqa-r2')
base = snapshot.parent / 'S3-submission-20261107-072954-274564'
destination = snapshot / ('independent-' + fixture + '-r2')
destination.mkdir()
frozen = json.loads((snapshot / 'supplement-snapshot.json').read_text(encoding='utf-8'))
for item in frozen['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
source_project = snapshot / fixture / 'native-test'
project = destination / 'native-test'
shutil.copytree(source_project / 'src', project / 'src')
for original in source_project.iterdir():
    if original.suffix in ('.gradle', '.properties'):
        shutil.copy2(original, project / original.name)
properties = snapshot / 'worldqa-r3/server/server.properties'
server = destination / 'server'
server.mkdir()
shutil.copy2(properties, server / 'server.properties')
inputs = json.loads((snapshot / fixture / 'inputs.json').read_text(encoding='utf-8-sig'))
for item in inputs:
    candidate = base / 'rooms' / item['id'] / (item['id'] + '.nbt')
    design = base / 'assembly.json' if item['id'] == 's3-merged' else candidate.parent / 'design.json'
    assert hashlib.sha256(candidate.read_bytes()).hexdigest() == item['sha256']
    assert design.is_file()
    item['candidate'] = str(candidate)
    item['design'] = str(design)
(destination / 'inputs.json').write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding='utf-8')
environment = os.environ.copy()
environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + r'\bin;' + environment['PATH']
gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
command = [gradle, '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
with (destination / 'console.log').open('w', encoding='utf-8') as output:
    process = subprocess.Popen(command, cwd=project, env=environment, stdout=output, stderr=subprocess.STDOUT)
    (destination / 'process.json').write_text(json.dumps(dict(pid=process.pid, command=command, cwd=str(project)), indent=2), encoding='utf-8')
    print(json.dumps(dict(pid=process.pid, directory=str(destination)), ensure_ascii=False), flush=True)
    exit_code = process.wait()
for item in frozen['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
(destination / 'independent-result.json').write_text(json.dumps(dict(exit_code=exit_code, frozen_files_unchanged=len(frozen['files']), accepted=False), indent=2), encoding='utf-8')
print(json.dumps(dict(directory=str(destination), exit_code=exit_code, status='EXECUTED_PENDING_AUDIT'), ensure_ascii=False))
sys.exit(exit_code)
