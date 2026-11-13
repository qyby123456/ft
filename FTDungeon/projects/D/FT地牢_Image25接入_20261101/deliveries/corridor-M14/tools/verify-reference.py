import hashlib
import json
from pathlib import Path

from PIL import Image

root = Path(__file__).resolve().parents[1]
batch = Path('D:/ft工程文件/image2.5/dungeon-corridors-20261103')
manifest = json.loads((batch / 'manifest.json').read_text(encoding='utf-8-sig'))
index = json.loads((batch / 'out/corridor_index.json').read_text(encoding='utf-8-sig'))
entry = next(item for item in manifest['images'] if item['id'] == 'M14')
design = next(item for item in index if item['id'] == 'M14')
reference = batch / entry['path']
digest = hashlib.sha256(reference.read_bytes()).hexdigest()
assert digest == entry['sha256'] == 'aeea1468c5e12912daa7b14bded792897778cd3331cca76999c302646e11ea31'
assert reference.stat().st_size == entry['bytes'] == 1820339
assert reference.name == design['filename'] == 'M14-fungal-cave-aged.png'
assert entry['kind'] == design['kind'] == 'medium'
assert design['theme'] == 'fungal-cave' and design['condition'] == 'aged'
with Image.open(reference) as image:
    image.load()
    assert image.format == 'PNG' and list(image.size) == entry['size'] == [1536, 1024]
report = {'status': 'PASS', 'id': 'corridor-M14', 'reference': str(reference), 'sha256': digest, 'bytes': entry['bytes'], 'size': entry['size'], 'full_png_decode': True, 'manifest_entry': entry, 'index_entry': design}
(root / 'qa/reference.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('M14_REFERENCE_MANIFEST_INDEX_PNG_DECODE_PASS')
