import hashlib
import json
from pathlib import Path

workspace = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
source_root = workspace / 's3-maker-access-followup'
manifest_path = source_root / 'access-ready-for-review.json'
payload = manifest_path.read_bytes()
ready = json.loads(payload)
destination = workspace / 'preserved-review-snapshots/S3-access-revision-20261107-1103'
destination.mkdir()
records = []
for item in ready['files']:
    source = Path(item['path']).resolve()
    assert source.is_relative_to(source_root)
    content = source.read_bytes()
    assert len(content) == item['bytes'] and hashlib.sha256(content).hexdigest() == item['sha256']
    target = destination / source.relative_to(source_root)
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(content)
    records.append(dict(source=str(source), snapshot=str(target), sha256=item['sha256'], bytes=len(content)))
for item in records:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest() == item['sha256']
assert manifest_path.read_bytes() == payload
(destination / 'access-ready-for-review.json').write_bytes(payload)
(destination / 'submission-snapshot.json').write_text(json.dumps({'stage':'HASH_CAPTURE_ONLY','accepted':False,'manifest_sha256':hashlib.sha256(payload).hexdigest(),'files':records},ensure_ascii=False,indent=2),encoding='utf-8')
print(destination, len(records))
