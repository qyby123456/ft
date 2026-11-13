import hashlib
import json
from pathlib import Path
from audit_s8_submission import load

workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
allowed=workspace/'s8-maker-access-isolated'
source=allowed/'access-revision-r4'
payload=(source/'INTERACTION-EVIDENCE.json').read_bytes()
ready=json.loads(payload)
destination=workspace/'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
destination.mkdir()
records=[]
for item in ready['assets']:
    original=Path(item['path']).resolve();assert original.is_relative_to(allowed)
    content=original.read_bytes();assert len(content)==item['bytes'] and hashlib.sha256(content).hexdigest()==item['sha256'],original
    target=destination/original.relative_to(allowed);target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(content)
    records.append(dict(source=str(original),snapshot=str(target),sha256=item['sha256'],bytes=len(content)))
for item in records:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest()==item['sha256']
assert (source/'INTERACTION-EVIDENCE.json').read_bytes()==payload
(destination/'INTERACTION-EVIDENCE.json').write_bytes(payload)
(destination/'submission-snapshot.json').write_text(json.dumps({'accepted':False,'files':records,'manifest_sha256':hashlib.sha256(payload).hexdigest()},ensure_ascii=False,indent=2),encoding='utf-8')
print(destination,len(records))
