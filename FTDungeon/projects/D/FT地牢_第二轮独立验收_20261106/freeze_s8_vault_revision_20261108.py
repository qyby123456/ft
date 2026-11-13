import hashlib
import json
import shutil
from datetime import datetime
from pathlib import Path

workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
source = workspace / 's8-maker-access-isolated/vault-shell-revision-r2'
destination = workspace / 'preserved-review-snapshots/S8-vault-shell-revision-20261108'
manifest_path = source / 'REVIEW-MANIFEST.json'
manifest = json.loads(manifest_path.read_text(encoding='utf-8'))
assert manifest['source_sha256'] == '763608faa6bc65fc85f8483912b7df6aafbbf84ee207a97ec2db0e3795c9775d'
assert len(manifest['assets']) == 196
assert not destination.exists()
destination.mkdir()
records = []
for item in [*manifest['assets'], dict(path=str(manifest_path),sha256=hashlib.sha256(manifest_path.read_bytes()).hexdigest(),bytes=manifest_path.stat().st_size)]:
    original = Path(item['path'])
    assert original.is_relative_to(source)
    assert original.stat().st_size == item['bytes']
    assert hashlib.sha256(original.read_bytes()).hexdigest() == item['sha256'], original
    frozen = destination / original.relative_to(source)
    frozen.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(original,frozen)
    assert hashlib.sha256(frozen.read_bytes()).hexdigest() == item['sha256']
    records.append(dict(source=str(original),snapshot=str(frozen),sha256=item['sha256'],bytes=item['bytes']))
snapshot = dict(group='S8',accepted=False,stage='INDEPENDENT_FREEZE_VAULT_SHELL_REVISION',created_at=datetime.now().isoformat(),source_sha256=manifest['source_sha256'],manifest_sha256=hashlib.sha256(manifest_path.read_bytes()).hexdigest(),source_directory=str(source),files=records)
(destination/'submission-snapshot.json').write_text(json.dumps(snapshot,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(dict(status='FROZEN_NOT_ACCEPTED',files=len(records),directory=str(destination),source_sha256=manifest['source_sha256']),ensure_ascii=False))
