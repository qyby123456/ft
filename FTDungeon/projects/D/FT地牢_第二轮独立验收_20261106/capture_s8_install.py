import hashlib
import json
from pathlib import Path

from audit_s8_submission import load

group=Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S8')
source_root=group/'runtime-install-r5'
workspace=Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603')
external=workspace/'s8-maker-access-isolated'
storage=load(source_root/'STORAGE-MANIFEST.json')
payload=(source_root/'INSTALLATION-EVIDENCE.json').read_bytes()
ready=json.loads(payload)
destination=workspace/'preserved-review-snapshots/S8-install-supplement-20261107-1125'
destination.mkdir()
records=[]
for item in ready['assets']:
    original=(source_root/item['path']).resolve()
    assert original.is_relative_to(group)
    source=original
    for move in storage['moves']:
        base=Path(move['source'])
        if original.is_relative_to(base):
            source=Path(move['destination'])/original.relative_to(base);break
    assert source.is_relative_to(group) or source.is_relative_to(external)
    content=source.read_bytes()
    assert len(content)==item['bytes'] and hashlib.sha256(content).hexdigest()==item['sha256'],source
    target=destination/original.relative_to(group)
    target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(content)
    records.append({'source':str(source),'original_path':str(original),'snapshot':str(target),'sha256':item['sha256'],'bytes':len(content)})
for name in ('INSTALLATION-EVIDENCE.json','STORAGE-MANIFEST.json','STORAGE.md','README.md'):
    (destination/name).write_bytes((source_root/name).read_bytes())
for item in records:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest()==item['sha256']
assert (source_root/'INSTALLATION-EVIDENCE.json').read_bytes()==payload
(destination/'submission-snapshot.json').write_text(json.dumps({'accepted':False,'manifest_sha256':hashlib.sha256(payload).hexdigest(),'files':records},ensure_ascii=False,indent=2),encoding='utf-8')
print(destination,len(records))
