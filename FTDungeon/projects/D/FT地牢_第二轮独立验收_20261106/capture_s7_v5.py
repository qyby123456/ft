import hashlib
import json
from datetime import datetime
from pathlib import Path


def digest(payload):
    return hashlib.sha256(payload).hexdigest()


def main():
    root = Path(__file__).parent
    parent = root / 'S7-submission-20261107-083104-571437'
    source = Path(r'D:\ft工程文件\FT地牢_功能房间第二轮_接手续作_20261106\S7\revisions\interaction-traversal-v5')
    contents = (source / 'ready-for-review.json').read_bytes()
    ready = json.loads(contents.decode('utf-8-sig'))
    assert ready['parent_ready']['sha256'] == digest((parent / 'ready-for-review.json').read_bytes())
    destination = root / ('S7-supplement-' + datetime.now().strftime('%Y%m%d-%H%M%S-%f'))
    destination.mkdir()
    records = []
    for item in ready['evidence_manifest']:
        original = Path(item['path']).resolve()
        assert original.is_relative_to(source.resolve())
        payload = original.read_bytes()
        assert digest(payload) == item['sha256'], str(original)
        target = destination / original.relative_to(source)
        target.parent.mkdir(parents=True, exist_ok=True)
        target.write_bytes(payload)
        records.append({'source': str(original), 'snapshot': str(target), 'sha256': digest(payload), 'bytes': len(payload)})
    for item in records:
        assert digest(Path(item['source']).read_bytes()) == item['sha256']
        assert digest(Path(item['snapshot']).read_bytes()) == item['sha256']
    assert (source / 'ready-for-review.json').read_bytes() == contents
    (destination / 'ready-for-review.json').write_bytes(contents)
    (destination / 'supplement-snapshot.json').write_text(json.dumps({'stage': 'INDEPENDENT_CAPTURE_NOT_ACCEPTANCE', 'parent': str(parent), 'parent_ready_sha256': ready['parent_ready']['sha256'], 'files': records, 'accepted': False}, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps({'directory': str(destination), 'files': len(records)}, ensure_ascii=False))


if __name__ == '__main__':
    main()
