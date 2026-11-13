import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path


root = Path(sys.argv[1]).resolve()
scratch = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s6-independent-walk-20261107-1015')
scratch.mkdir()
records = {}
for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
    for item in json.loads((root / name).read_text(encoding='utf-8'))['files']:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
        records[item['source']] = item
assert len(records) == 763
runtime = scratch / 'qa/runtime'
runtime.mkdir(parents=True)
shutil.copytree(root / 'qa/runtime/src', runtime / 'src')
for name in ('build.gradle', 'settings.gradle', 'gradle.properties'):
    shutil.copy2(root / 'qa/runtime' / name, runtime / name)
assembly = scratch / 'assembly'
(assembly / 'access').mkdir(parents=True)
shutil.copy2(root / 'assembly/s6-volcanic-connected.nbt', assembly / 's6-volcanic-connected.nbt')
shutil.copy2(root / 'assembly/design.json', assembly / 'design.json')
summary = json.loads((root / 'assembly/summary.json').read_text(encoding='utf-8'))
summary['nbtPath'] = str(assembly / 's6-volcanic-connected.nbt')
(assembly / 'summary.json').write_text(json.dumps(summary, ensure_ascii=False, indent=2), encoding='utf-8')
environment = os.environ.copy()
environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + r'\bin;' + environment['PATH']
gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
command = [gradle, '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', '-PqaNamespace=fts6walk', '-PqaWorkDir=world-parent-walk', 'runGameTestServer']
pointer = root / 'independent-walk-result.json'
with (scratch / 'console.log').open('w', encoding='utf-8') as output:
    process = subprocess.Popen(command, cwd=runtime, env=environment, stdout=output, stderr=subprocess.STDOUT)
    progress = dict(stage='RUNNING', pid=process.pid, scratch=str(scratch), command=command, accepted=False)
    pointer.write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(progress, ensure_ascii=False), flush=True)
    exit_code = process.wait()
for item in records.values():
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
result = dict(stage='EXECUTED_PENDING_AUDIT', exit_code=exit_code, scratch=str(scratch), frozen_files_unchanged=len(records), accepted=False)
pointer.write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False), flush=True)
sys.exit(exit_code)
