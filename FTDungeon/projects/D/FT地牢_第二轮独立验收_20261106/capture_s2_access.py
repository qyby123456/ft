import hashlib
import json
from datetime import datetime
from pathlib import Path


def main():
    source = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S2')
    manifest = source / 'access-install-33ceb33f/evidence-manifest.json'
    payload = manifest.read_bytes()
    data = json.loads(payload.decode('utf-8-sig'))
    destination = Path(__file__).parent / ('S2-access-' + datetime.now().strftime('%Y%m%d-%H%M%S-%f'))
    destination.mkdir()
    records = []
    for item in data['artifacts']:
        original = Path(item['path']).resolve()
        assert original.is_relative_to(source) and original == (source / item['relative_path']).resolve()
        contents = original.read_bytes()
        assert hashlib.sha256(contents).hexdigest() == item['sha256'] and len(contents) == item['bytes']
        target = destination / item['relative_path']
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(contents)
        records.append({'source': str(original), 'snapshot': str(target), 'sha256': item['sha256'], 'bytes': len(contents)})
    for item in records:
        assert hashlib.sha256(Path(item['source']).read_bytes()).hexdigest() == item['sha256']
    assert manifest.read_bytes() == payload
    (destination / 'evidence-manifest.json').write_bytes(payload)
    (destination / 'access-snapshot.json').write_text(json.dumps({'source_sha256': data['source_sha256'], 'files': records, 'accepted': False}, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({'directory': str(destination), 'files': len(records)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
