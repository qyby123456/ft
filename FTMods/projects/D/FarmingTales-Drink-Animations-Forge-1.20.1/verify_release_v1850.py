"""Verify the two foil-row corrections and preserve every other drink asset."""
from pathlib import Path
import hashlib
import io
import json
import zipfile
import xml.etree.ElementTree as ET
from PIL import Image

ROOT=Path(__file__).resolve().parent
BACKUP=ROOT/'backups/1.8.49-before-wine-mouth-colors'
RES=ROOT/'src/main/resources'
PREFIX='assets/farmingtales_drink_anim/'
READ=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=READ(BACKUP/'baseline.json')
targets={
    'kaleidoscope_tavern:wine':('wine_0.png',7,7),
    'kaleidoscope_world_liquor:lafite_1982':('lafite_1982_2.png',10,2),
}
paths={PREFIX+'textures/tavern_bottles/'+spec[0] for spec in targets.values()}
with zipfile.ZipFile(BACKUP/'source.zip') as old:
    for entry in old.namelist():
        if not entry.startswith('src/'):continue
        if entry=='src/main/resources/META-INF/mods.toml' or entry.removeprefix('src/main/resources/') in paths:continue
        assert old.read(entry)==(ROOT/entry).read_bytes(),entry
    for item,(file,x,y) in targets.items():
        path=PREFIX+'textures/tavern_bottles/'+file
        original=Image.open(io.BytesIO(old.read('src/main/resources/'+path))).convert('RGBA')
        fixed=Image.open(RES/path).convert('RGBA')
        assert original.size==fixed.size==(16,16),item
        changed={(i,j) for i in range(16) for j in range(16) if original.getpixel((i,j))!=fixed.getpixel((i,j))}
        assert changed=={(x,y),(x+1,y)},(item,changed)
        for pixel in changed:
            assert fixed.getpixel(pixel)==original.getpixel((pixel[0],y+1)),item
            r,g,b,a=fixed.getpixel(pixel);assert b>=r and a==255,item
profiles=READ(RES/(PREFIX+'drink_profiles.json'))
for p in profiles:
    if p['id'] in targets:assert p['liquid']==[]
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ['failures','errors','skipped'])
count=sum(int(t.get('tests')) for t in tests)
log_path=ROOT/'preview/smoke-wine-mouth-SteveSmoke-1.8.50.log'
log=log_path.read_text(encoding='utf-8-sig',errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 2' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
for item in targets:
    for label in ['body neck and decoration textures are present','original fixed contents have no dynamic liquid pass',
                  'drink consumes once and returns to idle','drinking preserves remaining stack BrewLevel']:
        assert f'CATALOG_SMOKE PASS {item} {label}' in log,(item,label)
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.50.jar'
with zipfile.ZipFile(jar) as built:
    for p in RES.rglob('*'):
        if p.is_file():assert built.read(p.relative_to(RES).as_posix())==p.read_bytes(),p
    assert 'version="1.8.50"' in built.read('META-INF/mods.toml').decode()
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.50',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],
    configuration_sha256=sha(config),unit_tests=count,items=list(targets),
    client_logs=[dict(path=str(log_path),sha256=sha(log_path),profiles=2)],
    scope='Only wine and lafite_1982 animation texture foil rows changed: two pixels each copied from adjacent original neck glass. All geometry, other textures, profiles, and Java sources unchanged. Isolated Forge client verification.')
(ROOT/'verification-1.8.50.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.50: exactly two textures, four corrected pixels; {count} tests and two client sequences. SHA256 {sha(jar)}')
