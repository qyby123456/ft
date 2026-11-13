import hashlib
import json
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parent
parent = root.parent / 'local-relief-v4'
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
def save(path, value):
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')
ready = json.loads((parent / 'ready-for-review.json').read_text(encoding='utf-8'))
assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in ready['evidence_manifest'])
save(root / 'parent-checkpoint.json', {'parent': str(parent), 'ready_sha256': digest(parent / 'ready-for-review.json'), 'verified_files': len(ready['evidence_manifest']), 'mismatches': 0, 'no_candidate_changes': True})
entries = [{'id': 'S7-assembly', 'candidate': ready['assembly']['candidate'], 'sha256': ready['assembly']['sha256']}, next({'id': entry['id'], 'candidate': entry['candidate'], 'sha256': entry['sha256']} for entry in ready['rooms'] if entry['id'] == 'S7-11')]
save(root / 'worldqa/inputs.json', entries)
route_input = json.loads((parent / 'assembly/qa/route-input.json').read_text(encoding='utf-8'))
route_input[0]['targets'] += [{'label': 'S7-20 chest use feet', 'position': [39, 5, 105]}, {'label': 'S7-20 lever use feet', 'position': [41, 5, 111]}]
save(root / 'paths/route-input.json', route_input)
classes = root / 'audit-classes'
classes.mkdir(exist_ok=True)
dependencies = Path(r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines()
classpath = ';'.join([str(classes), r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar'] + [entry for entry in dependencies if entry and 'minecraft_user_repo' not in entry])
java = Path(r'C:\Program Files\Common Files\Oracle\Java\javapath')
for command in [[str(java / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(root / 'AuthorPathExport.java')], [str(java / 'java.exe'), '-Xmx1536m', '-Djava.awt.headless=true', '-cp', classpath, 'AuthorPathExport', str(root / 'paths')]]:
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180)
    with (root / 'paths/export.log').open('a', encoding='utf-8') as stream:
        stream.write(result.stdout + result.stderr)
    print(result.stdout[-1000:], flush=True)
    if result.returncode:
        print(result.stderr[-1500:], flush=True)
        raise SystemExit(result.returncode)
