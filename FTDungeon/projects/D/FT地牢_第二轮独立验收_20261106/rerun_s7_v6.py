import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path


root = Path(sys.argv[1]).resolve()
mode = sys.argv[2]
assert mode in ('worldqa-proof','naturalqa-complete')
scratch = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603') / ('s7-v6-independent-' + mode + '-20261107-1032')
scratch.mkdir()
snapshot = json.loads((root / 'submission-snapshot.json').read_text(encoding='utf-8'))
by_source = {}
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
    by_source[item['source']] = item
destination = scratch / mode
project = destination / 'native-test'
source = root / mode
shutil.copytree(source / 'native-test/src', project / 'src')
for name in ('build.gradle','settings.gradle','gradle.properties'):
    shutil.copy2(source / 'native-test' / name, project / name)
inputs = json.loads((source / 'inputs.json').read_text(encoding='utf-8-sig'))
for item in inputs:
    candidate = Path(by_source[item['candidate']]['snapshot'])
    assert hashlib.sha256(candidate.read_bytes()).hexdigest() == item['sha256']
    item['candidate'] = str(candidate)
(destination / 'inputs.json').write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding='utf-8')
if mode == 'worldqa-proof':
    shutil.copytree(root / 'paths', scratch / 'paths')
environment = os.environ.copy()
environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + r'\bin;' + environment['PATH']
gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
command = [gradle, '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
pointer = root / ('independent-' + mode + '-result.json')
with (destination / 'console.log').open('w', encoding='utf-8') as output:
    process = subprocess.Popen(command, cwd=project, env=environment, stdout=output, stderr=subprocess.STDOUT)
    progress = dict(stage='RUNNING', pid=process.pid, runtime=str(destination), accepted=False)
    pointer.write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(progress,ensure_ascii=False),flush=True)
    exit_code = process.wait()
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
result = dict(stage='EXECUTED_PENDING_AUDIT',exit_code=exit_code,runtime=str(destination),verified_frozen_files=len(snapshot['files']),accepted=False)
pointer.write_text(json.dumps(result,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(result,ensure_ascii=False),flush=True)
sys.exit(exit_code)
