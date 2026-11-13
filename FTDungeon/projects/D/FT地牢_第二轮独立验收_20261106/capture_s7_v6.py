import hashlib
import json
from datetime import datetime
from pathlib import Path


review = Path(__file__).resolve().parent
source = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S7\revisions\install-interact-v6')
manifest_path = source / 'ready-for-review.json'
payload = manifest_path.read_bytes()
manifest = json.loads(payload.decode('utf-8-sig'))
assert manifest['revision'] == 'install-interact-v6' and manifest['group_accepted'] is False
assert len(manifest['rooms']) == 10 and len(manifest['evidence_manifest']) == 828
assert manifest['parents'][0]['sha256'] == '35ec0e9b0dce2fe253b7730e490ae2afab434fcf985c84f173910cb94ad4c2f1'
group = source.parent.parent
destination = review / ('S7-submission-' + datetime.now().strftime('%Y%m%d-%H%M%S-%f'))
destination.mkdir()
parents = []
for item in manifest['parents']:
    original = Path(item['path']).resolve()
    assert original.is_relative_to(group / 'revisions') and original.name == 'ready-for-review.json'
    contents = original.read_bytes()
    assert hashlib.sha256(contents).hexdigest() == item['sha256']
    target = destination / 'parent-manifests' / (original.parent.name + '.json')
    target.parent.mkdir(exist_ok=True)
    target.write_bytes(contents)
    parents.append(dict(source=str(original), snapshot=str(target), sha256=item['sha256']))
records = []
unique = set()
for item in manifest['evidence_manifest']:
    original = Path(item['path']).resolve()
    assert original.is_relative_to(source) and original not in unique
    unique.add(original)
    contents = original.read_bytes()
    digest = hashlib.sha256(contents).hexdigest()
    assert digest == item['sha256']
    target = destination / original.relative_to(source)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(contents)
    records.append(dict(source=str(original), snapshot=str(target), sha256=digest, bytes=len(contents)))
for item in records:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest() == item['sha256']
assert manifest_path.read_bytes() == payload
(destination / 'ready-for-review.json').write_bytes(payload)
report = dict(stage='STRICT_V6_DECLARED_EVIDENCE_CAPTURE_NOT_ACCEPTANCE', files=records, parent_manifests=parents, manifest_sha256=hashlib.sha256(payload).hexdigest(), accepted=False)
(destination / 'submission-snapshot.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
(review / 'latest-s7-v6.json').write_text(json.dumps(dict(directory=str(destination)), ensure_ascii=False), encoding='utf-8')
print(json.dumps(dict(directory=str(destination), files=len(records), parents=len(parents), accepted=False), ensure_ascii=False))
