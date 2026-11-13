import hashlib
import json
import zipfile
from pathlib import Path

ROOT = Path(__file__).resolve().parent
SOURCE = Path('D:/ft工程文件/锈铜宝库地牢_20261101/qa/vanilla-1.20.1-mapped.jar')
raw = {}
with zipfile.ZipFile(SOURCE) as archive:
    prefix = 'data/minecraft/tags/blocks/'
    for name in archive.namelist():
        if name.startswith(prefix) and name.endswith('.json'):
            raw['minecraft:' + name[len(prefix):-5]] = json.loads(archive.read(name))
resolved = {}

def resolve(name, stack=()):
    if name in resolved:
        return resolved[name]
    if name in stack:
        raise ValueError('Cyclic tag reference')
    result = set()
    for entry in raw[name]['values']:
        identifier = entry['id'] if isinstance(entry, dict) else entry
        if identifier.startswith('#'):
            nested = identifier[1:]
            if nested not in raw and isinstance(entry, dict) and not entry.get('required', True):
                continue
            result.update(resolve(nested, stack + (name,)))
        else:
            result.add(identifier)
    resolved[name] = sorted(result)
    return resolved[name]

for name in raw:
    resolve(name)
(ROOT / 'vanilla-block-tags.json').write_text(json.dumps(resolved, indent=2), encoding='utf-8')
(ROOT / 'vanilla-tag-provenance.json').write_text(json.dumps({'source': str(SOURCE), 'sha256': hashlib.sha256(SOURCE.read_bytes()).hexdigest(), 'tags': len(resolved), 'scope': 'Vanilla jar block tag JSON only, no mod/datapack overrides.'}, indent=2), encoding='utf-8')
print(len(resolved))
