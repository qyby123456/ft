import hashlib
import json
from pathlib import Path

review = Path(__file__).parent
source_root = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S7\revisions\automatic-hooks-v7-supplement')
external = Path(r'C:\Users\YOUR_USERNAME\.codex\visualizations\2026\11\04\01a1a685-045b-76d2-aa2b-2a3f24520603\s7-maker-hooks-isolated')
payload = (source_root / 'ready-for-review.json').read_bytes()
ready = json.loads(payload)
destination = review / 'S7-hooks-supplement-20261107-1047'
destination.mkdir()
records = []
for item in ready['evidence_manifest']:
    source = Path(item['path']).resolve()
    assert source.is_relative_to(external) or source.is_relative_to(source_root)
    content = source.read_bytes()
    assert len(content) == item['bytes'] and hashlib.sha256(content).hexdigest() == item['sha256']
    relative = Path('external') / source.relative_to(external) if source.is_relative_to(external) else source.relative_to(source_root)
    target = destination / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_bytes(content)
    records.append(dict(source=str(source), snapshot=str(target), sha256=item['sha256'], bytes=len(content)))
for item in records:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest() == item['sha256']
assert (source_root / 'ready-for-review.json').read_bytes() == payload
(destination / 'ready-for-review.json').write_bytes(payload)
(destination / 'submission-snapshot.json').write_text(json.dumps({'accepted': False, 'stage': 'HASH_CAPTURE_ONLY', 'manifest_sha256': hashlib.sha256(payload).hexdigest(), 'files': records}, ensure_ascii=False, indent=2), encoding='utf-8')
print(destination, len(records))
