import hashlib
import json
from datetime import datetime
from pathlib import Path


review = Path(__file__).resolve().parent
source = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S3')
manifest_path = source / 'runtime-ready-for-review.json'
manifest_bytes = manifest_path.read_bytes()
manifest = json.loads(manifest_bytes.decode('utf-8-sig'))
assert manifest['source_sha256'] == '933a5f44ccc2c84d00b7ec321080068e99989403de35e7ad7aa3e09bc9a569b7'
package = json.loads((source / 'deployment/package-report.json').read_text(encoding='utf-8-sig'))
assert package['source_sha256'] == manifest['source_sha256']
destination = review / ('S3-supplement-' + datetime.now().strftime('%Y%m%d-%H%M%S-%f'))
destination.mkdir()
records = {}
for item in manifest['files']:
    original = Path(item['path']).resolve()
    assert original.is_relative_to(source)
    contents = original.read_bytes()
    assert hashlib.sha256(contents).hexdigest() == item['sha256']
    assert len(contents) == item['bytes']
    records[original] = item['sha256']
for relative, digest in package['files'].items():
    original = (source / 'deployment/datapack' / relative).resolve()
    assert original.is_relative_to(source / 'deployment/datapack')
    assert hashlib.sha256(original.read_bytes()).hexdigest() == digest
    records[original] = digest
captured = []
for original, digest in records.items():
    contents = original.read_bytes()
    assert hashlib.sha256(contents).hexdigest() == digest
    frozen = destination / original.relative_to(source)
    frozen.parent.mkdir(parents=True, exist_ok=True)
    frozen.write_bytes(contents)
    captured.append(dict(source=str(original), snapshot=str(frozen), sha256=digest, bytes=len(contents)))
for item in captured:
    assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest() == item['sha256']
assert manifest_path.read_bytes() == manifest_bytes
(destination / 'runtime-ready-for-review.json').write_bytes(manifest_bytes)
(destination / 'supplement-snapshot.json').write_text(json.dumps(dict(source_sha256=manifest['source_sha256'], files=captured, accepted=False), ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(dict(directory=str(destination), files=len(captured), status='FROZEN_NOT_ACCEPTED'), ensure_ascii=False))
