import hashlib
import json
import os
import shutil
import subprocess
from pathlib import Path

from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-install-supplement-20261107-1125'
frozen=workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543'
snapshot=load(root/'submission-snapshot.json')
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
destination=workspace/'s8-independent-install-20261107-1127'
destination.mkdir()
source=root/'runtime-install-r5'
shutil.copytree(source/'native-test',destination/'native-test')
gradle=destination/'native-test/build.gradle'
gradle.write_text('\n'.join(line for line in gradle.read_text(encoding='utf-8').splitlines() if not line.startswith('layout.buildDirectory')),encoding='utf-8')
shutil.copytree(source/'server/world/datapacks',destination/'server/world/datapacks')
shutil.copyfile(source/'server/server.properties',destination/'server/server.properties')
inputs=load(source/'install-input.json')
inputs['candidate']=str(frozen/'assembly/s8-functional-combined.nbt')
assert hashlib.sha256(Path(inputs['candidate']).read_bytes()).hexdigest()==inputs['sha256']
(destination/'install-input.json').write_text(json.dumps(inputs,ensure_ascii=False,indent=2),encoding='utf-8')
assert not (destination/'server/world/level.dat').exists()
environment=os.environ.copy()
environment['JAVA_HOME']=r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH']=environment['JAVA_HOME']+r'\bin;'+environment['PATH']
command=[r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false','runGameTestServer']
with (destination/'console.log').open('w',encoding='utf-8') as log:
    process=subprocess.Popen(command,cwd=destination/'native-test',env=environment,stdout=log,stderr=subprocess.STDOUT)
    progress=dict(status='RUNNING',pid=process.pid,runtime=str(destination),accepted=False)
    (root/'independent-run.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(progress,ensure_ascii=False),flush=True)
    code=process.wait()
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
progress.update(status='FINISHED',exit_code=code,frozen_files_unchanged=len(snapshot['files']))
(root/'independent-run.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(progress,ensure_ascii=False),flush=True)
raise SystemExit(code)
