from pathlib import Path
import json,zipfile
root=Path(__file__).absolute().parent
assets=root/'src/main/resources/assets'
profiles=json.loads((assets/'farmingtales_drink_anim/drink_profiles.json').read_text(encoding='utf-8'))
sprites={r['texture'].replace(':textures/',':').removesuffix('.png') for p in profiles for r in p['liquid']}
sprites.add('farmingtales_drink_anim:orange_wine')
path=assets/'minecraft/atlases/blocks.json'
path.parent.mkdir(parents=True,exist_ok=True)
with zipfile.ZipFile(next((root/'backups/1.8.1-before-surface-fix').glob('*.jar'))) as old:
    original=old.read('assets/minecraft/atlases/blocks.json')
atlas=json.loads(original)
missing=sorted(sprites-{s['resource'] for s in atlas['sources']})
if missing:
    atlas['sources'] += [{'type':'single','resource':s,'sprite':s} for s in missing]
    path.write_text(json.dumps(atlas,indent=2)+'\n',encoding='utf-8')
else:path.write_bytes(original)
print('Preserved original atlas; added liquid sprites:',missing)
