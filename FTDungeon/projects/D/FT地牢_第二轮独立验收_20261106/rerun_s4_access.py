import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path


snapshot = Path(sys.argv[1]).resolve()
destination = snapshot / 'independent-access-gravity'
destination.mkdir()
records = []
for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
    records += json.loads((snapshot / name).read_text(encoding='utf-8'))['files']
for item in records:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
project = destination / 'native-test'
source = snapshot / 'qa/worldtest/native-test'
shutil.copytree(source / 'src', project / 'src')
for original in source.iterdir():
    if original.suffix in ('.gradle', '.properties'):
        shutil.copy2(original, project / original.name)
fixture = snapshot / 'qa/access-gravity-r3'
shutil.copy2(fixture / 'layout.json', destination / 'layout.json')
installation = json.loads((fixture / 'installation.json').read_text(encoding='utf-8-sig'))
source_record = next(item for item in records if item['source'] == installation['candidate'])
installation['candidate'] = source_record['snapshot']
assert hashlib.sha256(Path(installation['candidate']).read_bytes()).hexdigest() == installation['candidate_sha256']
(destination / 'installation.json').write_text(json.dumps(installation, ensure_ascii=False, indent=2), encoding='utf-8')
server = destination / 'server'
shutil.copytree(fixture / 'datapack', server / 'world/datapacks/s4-access')
environment = os.environ.copy()
environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + r'\bin;' + environment['PATH']
gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
command = [gradle, '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', '-Pfts4.namespace=fts4accessqa', '-Pfts4.work=../server', '-Pfts4.output=' + str(destination), 'runGameTestServer']
with (destination / 'console.log').open('w', encoding='utf-8') as output:
    process = subprocess.Popen(command, cwd=project, env=environment, stdout=output, stderr=subprocess.STDOUT)
    (destination / 'process.json').write_text(json.dumps(dict(pid=process.pid, command=command, cwd=str(project)), indent=2), encoding='utf-8')
    print(json.dumps(dict(pid=process.pid, directory=str(destination)), ensure_ascii=False), flush=True)
    exit_code = process.wait()
for item in records:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
(destination / 'independent-result.json').write_text(json.dumps(dict(exit_code=exit_code, frozen_files_unchanged=len({item['source'] for item in records}), accepted=False), indent=2), encoding='utf-8')
print(json.dumps(dict(exit_code=exit_code, status='EXECUTED_PENDING_AUDIT'), ensure_ascii=False))
sys.exit(exit_code)
