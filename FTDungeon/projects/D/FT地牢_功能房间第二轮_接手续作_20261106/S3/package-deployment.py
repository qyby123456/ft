import hashlib
import json
import shutil
from pathlib import Path

root = Path(__file__).resolve().parent
manifest = json.loads((root / 'parts/manifest.json').read_text(encoding='utf-8'))
candidate = root / 'rooms/s3-merged/s3-merged.nbt'
assert hashlib.sha256(candidate.read_bytes()).hexdigest() == manifest['source_sha256']
pack = root / 'deployment/datapack'
functions = pack / 'data/fts3/functions'
structures = pack / 'data/fts3/structures'
functions.mkdir(parents=True, exist_ok=True)
structures.mkdir(parents=True, exist_ok=True)
(pack / 'pack.mcmeta').write_text(json.dumps({'pack': {'pack_format': 15, 'description': 'S3 explicit admin deployment, origin 0 100 0, no auto load/tick functions'}}, ensure_ascii=False, indent=2), encoding='utf-8')
clear = []
volume = 0
for east in range(0, 207, 16):
    for south in range(0, 217, 16):
        last_east = min(206, east + 15)
        last_south = min(216, south + 15)
        count = (last_east - east + 1) * 35 * (last_south - south + 1)
        assert count <= 32768
        volume += count
        clear.append(f'fill {east} 100 {south} {last_east} 134 {last_south} minecraft:air replace')
assert volume == 207 * 35 * 217
(functions / 'clear.mcfunction').write_text('\n'.join(clear) + '\n', encoding='utf-8')
place = []
for part in manifest['parts']:
    path = Path(part['path'])
    assert hashlib.sha256(path.read_bytes()).hexdigest() == part['sha256']
    shutil.copyfile(path, structures / path.name)
    east, up, south = part['origin']
    place.append(f'place template fts3:{path.stem} {east} {up + 100} {south} none none 1.0 0')
(functions / 'place.mcfunction').write_text('\n'.join(place) + '\n', encoding='utf-8')
(functions / 'outside-pad.mcfunction').write_text('fill -10 102 29 -4 102 33 minecraft:stone_bricks\nfill -10 103 29 -4 106 33 minecraft:air\n', encoding='utf-8')
(functions / 'install.mcfunction').write_text('forceload add 0 0 206 216\nforceload add -16 16 -1 47\nfunction fts3:clear\nfunction fts3:place\nfunction fts3:outside-pad\n', encoding='utf-8')
(functions / 'enter.mcfunction').write_text('tp @s 10.5 103 31.5\n', encoding='utf-8')
(functions / 'exit.mcfunction').write_text('execute if entity @s[x=134,y=108,z=156,dx=2,dy=2,dz=2] run tp @s -7.5 103 31.5\n', encoding='utf-8')
(functions / 'release-chunks.mcfunction').write_text('forceload remove 0 0 206 216\nforceload remove -16 16 -1 47\n', encoding='utf-8')
controls = []
for east in (10, 18):
    for south in (17, 23, 28):
        controls.append(f'setblock {65 + east - 1} 103 {22 + south} minecraft:lever[face=floor,facing=north,powered=false]')
(functions / 'optional-door-controls.mcfunction').write_text('\n'.join(controls) + '\n', encoding='utf-8')
evidence = {'source_sha256': manifest['source_sha256'], 'origin': [0, 100, 0], 'size': [207, 35, 217], 'clear_commands': len(clear), 'cleared_volume': volume, 'parts': len(place), 'status': 'PACKAGED_NOT_INSTALLED', 'no_auto_functions': True, 'files': {str(path.relative_to(pack)): hashlib.sha256(path.read_bytes()).hexdigest() for path in sorted(pack.rglob('*')) if path.is_file()}}
(root / 'deployment/package-report.json').write_text(json.dumps(evidence, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key: value for key, value in evidence.items() if key != 'files'}, ensure_ascii=False))
