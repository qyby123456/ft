import hashlib
import json
import os
import subprocess
from pathlib import Path

review = Path(__file__).parent
root = review / 'S7-hooks-supplement-20261107-1047'
ready = json.loads((root / 'ready-for-review.json').read_text(encoding='utf-8'))
snapshot = json.loads((root / 'submission-snapshot.json').read_text(encoding='utf-8'))
original = Path(ready['successful_root'])
destination = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s7-independent-automatic-hooks-20261107-1050')
destination.mkdir()
for item in snapshot['files']:
    source = Path(item['snapshot'])
    assert hashlib.sha256(source.read_bytes()).hexdigest() == item['sha256']
    original_path = Path(item['source'])
    if not original_path.is_relative_to(original):
        continue
    relative = original_path.relative_to(original)
    if str(relative).startswith('native-test') or relative == Path('server/server.properties') or relative == Path('server/world/datapacks/fts7-datapack.zip'):
        target = destination / relative
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(source.read_bytes())
assert not (destination / 'server/world/level.dat').exists()
environment = os.environ.copy()
environment['JAVA_HOME'] = r'C:\Users\YOUR_USERNAME\AppData\Roaming\.minecraft\runtime\java-runtime-gamma-snapshot'
environment['PATH'] = environment['JAVA_HOME'] + r'\bin;' + environment['PATH']
gradle = r'C:\Users\YOUR_USERNAME\.gradle\wrapper\dists\gradle-8.8-bin\dl7vupf4psengwqhwktix4v1\gradle-8.8\bin\gradle.bat'
command = [gradle, '--offline', '--no-daemon', '--console=plain', '--max-workers=1', '-Dnet.minecraftforge.gradle.check.certs=false', 'runGameTestServer']
pointer = root / 'independent-run.json'
with (destination / 'console.log').open('w', encoding='utf-8') as log:
    process = subprocess.Popen(command, cwd=destination / 'native-test', env=environment, stdout=log, stderr=subprocess.STDOUT)
    progress = dict(status='RUNNING', pid=process.pid, runtime=str(destination), accepted=False)
    pointer.write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(progress, ensure_ascii=False), flush=True)
    code = process.wait()
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
progress.update(status='FINISHED', exit_code=code, frozen_files_unchanged=len(snapshot['files']))
pointer.write_text(json.dumps(progress, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(progress, ensure_ascii=False), flush=True)
raise SystemExit(code)
