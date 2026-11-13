import hashlib
import json
import zipfile
from pathlib import Path

root = Path(__file__).resolve().parents[1]
sha256 = lambda data: hashlib.sha256(data).hexdigest()
nbt_hash = sha256((root / 'room.nbt').read_bytes())
for relative in ['room.json', 'qa.json', 'qa/static.json', 'qa/native.json', 'qa/visual.json']:
    report = json.loads((root / relative).read_text(encoding='utf-8'))
    assert report['sha256'] == nbt_hash, relative
    if relative != 'room.json':
        assert report['status'] == 'PASS', relative
qa = json.loads((root / 'qa.json').read_text(encoding='utf-8'))
assert 'A02_NATIVE_GAME_TEST_PASS rotations=4/4 records=21168 ticks=400' in (root / qa['successful_log']).read_text(encoding='utf-8-sig')
visual = json.loads((root / 'qa/visual.json').read_text(encoding='utf-8'))
assert len(visual['captures']) == 8
for capture in visual['captures']:
    assert sha256((root / capture['file']).read_bytes()) == capture['sha256']
selected = [root / name for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'README.md', 'summary.json']]
for folder in ['tools', 'views']:
    selected.extend(file for file in (root / folder).rglob('*') if file.is_file() and '__pycache__' not in file.parts)
selected.extend(file for file in (root / 'qa').iterdir() if file.is_file())
selected.extend(root / 'native' / name for name in ['build.gradle', 'settings.gradle', 'gradle.properties'])
selected.extend(file for file in (root / 'native/src').rglob('*') if file.is_file())
selected = sorted(set(selected), key=lambda file: file.relative_to(root).as_posix())
manifest = {'id': 'zone-A02', 'status': 'PASS', 'room_nbt_sha256': nbt_hash, 'source_image_sha256': qa['source_sha256'], 'excluded': ['native/build', 'native/.gradle', 'qa/forge-run-*'], 'files': [{'file': file.relative_to(root).as_posix(), 'bytes': file.stat().st_size, 'sha256': sha256(file.read_bytes())} for file in selected]}
manifest_path = root / 'delivery-manifest.json'
manifest_path.write_text(json.dumps(manifest, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
selected.append(manifest_path)
archive = root / 'zone-A02-delivery.zip'
with zipfile.ZipFile(archive, 'w', zipfile.ZIP_DEFLATED) as package:
    for file in selected:
        package.write(file, 'zone-A02/' + file.relative_to(root).as_posix())
with zipfile.ZipFile(archive) as package:
    assert package.testzip() is None
    for record in manifest['files']:
        assert sha256(package.read('zone-A02/' + record['file'])) == record['sha256']
archive_hash = sha256(archive.read_bytes())
(root / 'SHA256SUMS.txt').write_text('\n'.join(f"{sha256((root / name).read_bytes())}  {name}" for name in ['room.nbt', 'room.json', 'preview.png', 'qa.json', 'delivery-manifest.json', archive.name]) + '\n', encoding='utf-8')
print(json.dumps({'status': 'PASS', 'files': len(selected), 'nbt_sha256': nbt_hash, 'zip_sha256': archive_hash, 'zip_bytes': archive.stat().st_size, 'archive': str(archive)}, ensure_ascii=False))

