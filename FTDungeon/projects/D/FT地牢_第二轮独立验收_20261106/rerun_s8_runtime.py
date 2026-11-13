import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path
from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
root=workspace/'preserved-review-snapshots/S8-runtime-supplement-20261107-1340'
frozen=workspace/'preserved-review-snapshots/S8-submission-20261107-104351-507543'
mode=sys.argv[1]
assert mode in ('runtime-assembly-r2','runtime-r4')
snapshot=load(root/'submission-snapshot.json')
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
destination=workspace/('s8-independent-'+mode+'-20261107-1345')
destination.mkdir()
source=root/mode
shutil.copytree(source/'native-test/src',destination/'native-test/src')
for name in ('build.gradle','settings.gradle','gradle.properties'):
    shutil.copyfile(source/'native-test'/name,destination/'native-test'/name)
for name in ('assembly-input.json','ladder-input.json','rooms.json'):
    if not (source/name).exists():
        continue
    inputs=load(source/name)
    entries=inputs if isinstance(inputs,list) else [inputs]
    for entry in entries:
        if 'candidate' in entry:
            original=Path(entry['candidate'])
            group=Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S8')
            candidate=frozen/original.relative_to(group)
            entry['candidate']=str(candidate)
            if 'sha256' in entry:
                assert hashlib.sha256(candidate.read_bytes()).hexdigest()==entry['sha256']
    (destination/name).write_text(json.dumps(inputs,ensure_ascii=False,indent=2),encoding='utf-8')
environment=os.environ.copy()
environment['JAVA_HOME']=r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH']=environment['JAVA_HOME']+r'\bin;'+environment['PATH']
command=[r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat','--offline','--no-daemon','--console=plain','--max-workers=1','-Dnet.minecraftforge.gradle.check.certs=false','runGameTestServer']
with (destination/'console.log').open('w',encoding='utf-8') as log:
    process=subprocess.Popen(command,cwd=destination/'native-test',env=environment,stdout=log,stderr=subprocess.STDOUT)
    progress=dict(status='RUNNING',pid=process.pid,runtime=str(destination),accepted=False)
    (root/('independent-'+mode+'.json')).write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
    print(json.dumps(progress,ensure_ascii=False),flush=True)
    code=process.wait()
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest()==item['sha256']
progress.update(status='FINISHED',exit_code=code,frozen_files_unchanged=len(snapshot['files']))
(root/('independent-'+mode+'.json')).write_text(json.dumps(progress,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(progress,ensure_ascii=False),flush=True)
raise SystemExit(code)
