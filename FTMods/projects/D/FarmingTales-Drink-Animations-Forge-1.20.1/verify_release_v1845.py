"""Check the requested drink changes against the installed 1.8.44 baseline."""
from pathlib import Path
import hashlib, json, zipfile, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT/'backups/1.8.44-before-grip-fruit-liquid'
PREFIX = 'src/main/resources/assets/farmingtales_drink_anim/'
READ = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))
SHA = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
JUICES = {f'farmingtales:{fruit}_juice_1' for fruit in ('orange','lemon','strawberry','apple','watermelon')}
BEERS = {f'brewery:beer_{grain}' for grain in ('wheat','barley','hops','nettle','haley','oat')}
WHISKY = 'brewery:whiskey_highland_hearth'
ITEMS = JUICES | BEERS | {WHISKY, 'vinery:jo_special_mixture'}
jar = ROOT/'build/libs/farmingtales_drink_anim-1.8.45.jar'
baseline = READ(BACKUP/'baseline.json')

with zipfile.ZipFile(BACKUP/'source.zip') as old:
    for name, allowed, keyed in [('drink_profiles.json',{WHISKY},True),
                                 ('decorations.json',JUICES,False),
                                 ('hand_volumes.json',JUICES|BEERS,False)]:
        before = json.loads(old.read(PREFIX+name)); after = READ(ROOT/(PREFIX+name))
        if keyed:
            before = {p['id']:p for p in before}; after = {p['id']:p for p in after}
        assert before.keys() == after.keys() or name == 'decorations.json'
        for key, value in before.items():
            if key not in allowed: assert value == after[key], (name,key)
    profiles = {p['id']:p for p in READ(ROOT/(PREFIX+'drink_profiles.json'))}
    parts = READ(ROOT/(PREFIX+'decorations.json'))
    for item in JUICES:
        path = PREFIX+'models/'+profiles[item]['bodyModel']+'.json'
        source = json.loads(old.read(path))
        body = READ(ROOT/path)
        assert len(parts[item]) == 1 and parts[item][0]['kind'] == 'FRUIT'
        garnish = READ(ROOT/(PREFIX+'models/'+parts[item][0]['model']+'.json'))
        assert body['textures'] == garnish['textures'] == source['textures']
        assert body['elements'] + garnish['elements'] == source['elements'], item
    assert profiles[WHISKY]['liquid']
    expected_resources = {'src/main/resources/META-INF/mods.toml'} | {PREFIX+f for f in ('drink_profiles.json','decorations.json','hand_volumes.json')}
    expected_resources |= {PREFIX+'models/'+profiles[item]['bodyModel']+'.json' for item in JUICES}
    expected_resources |= {PREFIX+f'models/animation/letsdo/brewery/whiskey_highland_hearth_{part}.json' for part in ('body','neck','cap')}
    for name in old.namelist():
        if name.startswith('src/main/resources/') and name not in expected_resources:
            assert old.read(name) == (ROOT/name).read_bytes(), name
    sources = json.loads(old.read('letsdo-sources.json'))
    for mod in sources['jars']: assert SHA(Path(mod['path'])) == mod['sha256'], mod['path']

with zipfile.ZipFile(jar) as built:
    for path in (ROOT/'src/main/resources').rglob('*'):
        if path.is_file(): assert built.read(path.relative_to(ROOT/'src/main/resources').as_posix()) == path.read_bytes(), path
    assert 'version="1.8.45"' in built.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in entry for entry in built.namelist())
tests = [ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures','errors','skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count >= 157
logs = []
for name, items in [('smoke-drink-fixes-1.8.45.log',ITEMS),
                    ('smoke-jo-final-1.8.45.log',{'vinery:jo_special_mixture'}),
                    ('smoke-drink-fixes-slim-1.8.45.log',{'vinery:jo_special_mixture','brewery:beer_hops'})]:
    path = ROOT/'preview'/name
    log = path.read_text(encoding='utf-8-sig', errors='replace')
    assert f'CATALOG_SMOKE_COMPLETE {len(items)}' in log and 'BUILD SUCCESSFUL' in log and 'SILENT_CLIENT:' in log, name
    assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log, name
    for item in items: assert f'CATALOG_SMOKE PASS {item} body neck and decoration textures are present' in log, item
    logs.append(dict(path=str(path),sha256=SHA(path),items=sorted(items)))
config = ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert SHA(config) == baseline['configuration_sha256']
report = dict(version='1.8.45',sha256=SHA(jar),previous_sha256=baseline['previous_sha256'],
              configuration_sha256=SHA(config),unit_tests=count,client_profiles=len(ITEMS),client_logs=logs,
              scope='Jo wide-jar palm clearance; Highland Hearth moving whisky; five original juice slices use cocktail inertia; six opaque beer mug masks end at the actual rim and only clip their interiors.',
              limitation='Isolated Forge client visual checks; the full running modpack and its shader combination were not relaunched.')
(ROOT/'verification-1.8.45.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'Verified 1.8.45: {count} unit tests, 13 drinks plus 2 slim-arm repeats; SHA256 {SHA(jar)}')
