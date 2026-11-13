import hashlib
import json
import shutil
import subprocess
from pathlib import Path

root = Path(__file__).resolve().parent
parent = root.parent / 'local-relief-v4'
prior = root.parent / 'interaction-traversal-v5'
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))
def save(path, value):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(value, ensure_ascii=False, indent=2), encoding='utf-8')
def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()
ready = load(parent / 'ready-for-review.json')
for directory in [parent, prior]:
    frozen = load(directory / 'ready-for-review.json')
    assert all(digest(Path(entry['path'])) == entry['sha256'] for entry in frozen['evidence_manifest'])
assembly = load(root / 'assembly/assembly.json') if (root / 'assembly/assembly.json').exists() else ready['assembly']
save(root / 'parent-checkpoint.json', {'v4_ready': str(parent / 'ready-for-review.json'), 'v4_sha256': digest(parent / 'ready-for-review.json'), 'v5_ready': str(prior / 'ready-for-review.json'), 'v5_sha256': digest(prior / 'ready-for-review.json'), 'candidate_sha256': assembly['sha256'], 'candidate_changed_only_15_16_access': True})
paths = root / 'paths'
paths.mkdir(exist_ok=True)
design = load(root / 'assembly/design.json')
save(paths / 'route-input.json', [{'group':7,'candidate':assembly['candidate'],'sha256':assembly['sha256'],'size':assembly['size'],'start':design['start'],'targets':design['targets'],'swimming':False}])
source = (prior / 'AuthorPathExport.java').read_text(encoding='utf-8')
source = source.replace('public final class AuthorPathExport', 'public final class ReachableEyesExport')
source = source.replace('Files.writeString(root.resolve("author-paths.json")', 'Files.writeString(root.resolve("author-paths.json")')
source = source.replace('List<Object> results = new ArrayList<>();', 'Files.writeString(root.resolve("reachable-feet.json"), new GsonBuilder().create().toJson(visited.stream().map(point -> List.of(point.eastHalf/2.0,point.upHalf/2.0,point.southHalf/2.0)).toList()));\n            List<Object> results = new ArrayList<>();')
(root / 'ReachableEyesExport.java').write_text(source, encoding='utf-8')
classes = root / 'audit-classes'
classes.mkdir(exist_ok=True)
dependencies = Path(r'D:\ft工程文件\霜雪花园地牢_20261101\build\classpath\runData_minecraftClasspath.txt').read_text(encoding='utf-8').splitlines()
classpath = ';'.join([str(classes), r'D:\ft工程文件\锈铜宝库地牢_20261101\qa\vanilla-1.20.1-mapped.jar'] + [entry for entry in dependencies if entry and 'minecraft_user_repo' not in entry])
java = Path(r'C:\Program Files\Common Files\Oracle\Java\javapath')
for command in [[str(java / 'javac.exe'), '-encoding', 'UTF-8', '-cp', classpath, '-d', str(classes), str(root / 'ReachableEyesExport.java')], [str(java / 'java.exe'), '-Xmx1536m', '-Djava.awt.headless=true', '-cp', classpath, 'ReachableEyesExport', str(paths)]]:
    result = subprocess.run(command, capture_output=True, text=True, encoding='utf-8', errors='replace', timeout=180)
    with (paths / 'export.log').open('a', encoding='utf-8') as stream:
        stream.write(result.stdout + result.stderr)
    print(result.stdout[-1000:], flush=True)
    if result.returncode:
        print(result.stderr[-1500:], flush=True)
        raise SystemExit(result.returncode)
save(root / 'worldqa/inputs-current.json', [{'id': 'S7-assembly', 'candidate': assembly['candidate'], 'sha256': assembly['sha256']}])
