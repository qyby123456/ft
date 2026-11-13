from pathlib import Path
import hashlib
import json

ROOT = Path(__file__).resolve().parents[1]
WORK = ROOT / 'output/room-expansion-v035'
TARGET = ROOT / 'src/eclipseStyleTest/resources/data/fteclipsestylesqa/expansion'
plan = json.loads((WORK / 'plan.json').read_text(encoding='utf-8'))
rooms = []
for style in plan['threads']:
    for batch in range(1, 6):
        directory = WORK / style['style'] / f'batch-{batch:02}'
        manifest = json.loads((directory / 'manifest.json').read_text(encoding='utf-8'))
        for entry in manifest['templates']:
            raw = (directory / entry['file']).read_bytes()
            assert hashlib.sha256(raw).hexdigest() == entry['sha256']
            relative = Path(style['style']) / (entry['key'] + '.nbt')
            destination = TARGET / relative
            destination.parent.mkdir(parents=True, exist_ok=True)
            destination.write_bytes(raw)
            rooms.append({'style': style['style'], 'key': entry['key'], 'baseKey': entry['baseKey'], 'sha256': entry['sha256'], 'resource': 'data/fteclipsestylesqa/expansion/' + relative.as_posix()})
assert len(rooms) == 80
(TARGET / 'inventory.json').write_text(json.dumps({'schema': 1, 'rooms': rooms}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print('Prepared 80 room native test fixtures; production resources unchanged')
