import hashlib
import json
import os
import shutil
import subprocess
import sys
from pathlib import Path


root = Path(sys.argv[1]).resolve()
mode = sys.argv[2]
assert mode in ('natural-movement', 'natural-detours')
records = {}
for name in ('submission-snapshot.json', 'supplemental-snapshot.json', 'delivery-snapshot.json'):
    for item in json.loads((root / name).read_text(encoding='utf-8'))['files']:
        assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
        records[item['source']] = item
assert len(records) == 1016
run_root = root / ('independent-' + mode)
destination = run_root / 'qa' / mode
destination.mkdir(parents=True)
candidate = root / 'merged/merged.nbt'
assert hashlib.sha256(candidate.read_bytes()).hexdigest() == 'e0c71315c81727e81cd15011eda9a0f5da8624387c59b8b9764c42d8d740397e'
(run_root / 'merged').mkdir()
shutil.copy2(candidate, run_root / 'merged/merged.nbt')
source = root / 'qa' / mode
project = destination / 'native-test'
shutil.copytree(source / 'native-test/src', project / 'src')
for original in (source / 'native-test').iterdir():
    if original.suffix in ('.gradle', '.properties'):
        shutil.copy2(original, project / original.name)
shutil.copy2(source / 'planned-paths.json', destination / 'planned-paths.json')
properties = source / 'server/server.properties'
if properties.is_file():
    (destination / 'server').mkdir()
    shutil.copy2(properties, destination / 'server/server.properties')
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
for item in records.values():
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
result = dict(exit_code=exit_code, verified_frozen_files=len(records), accepted=False)
(destination / 'independent-result.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result), flush=True)
sys.exit(exit_code)
