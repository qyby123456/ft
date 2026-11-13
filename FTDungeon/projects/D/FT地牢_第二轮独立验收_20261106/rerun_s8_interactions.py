import hashlib
import json
import os
import shutil
import subprocess
from pathlib import Path
from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
records=load(root/'submission-snapshot.json')['files']
for item in records:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
source=root/'interaction-r9'
destination=workspace/'s8-independent-interactions-20261107-1400'
destination.mkdir()
shutil.copytree(source/'native-test/src',destination/'native-test/src')
for name in ('build.gradle','settings.gradle','gradle.properties'):
    shutil.copyfile(source/'native-test'/name,destination/'native-test'/name)
(destination/'server').mkdir()
shutil.copyfile(source/'server/server.properties',destination/'server/server.properties')
inputs=load(source/'input.json');inputs['candidate']=str(source/'frozen-candidate.nbt')
assert hashlib.sha256(Path(inputs['candidate']).read_bytes()).hexdigest()==inputs['sha256']
(destination/'input.json').write_text(json.dumps(inputs,ensure_ascii=False,indent=2),encoding='utf-8')
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
for item in records:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
progress.update(status='FINISHED',exit_code=code,frozen_files_unchanged=len(records))
(root/'independent-run.json').write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(progress,ensure_ascii=False),flush=True)
raise SystemExit(code)
