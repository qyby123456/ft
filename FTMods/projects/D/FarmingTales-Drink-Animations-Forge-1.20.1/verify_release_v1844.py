"""Check fixed source bottles, three-length corks, source-sized mouths and scope."""
from pathlib import Path
from copy import deepcopy
import hashlib
import json
import zipfile
import xml.etree.ElementTree as ET
from PIL import Image
from research_letsdo import JARS, TARGETS, source, model
from prepare_letsdo_profiles import NECKS, SCREW

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.43-before-static-wine-long-corks'
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
PREFIX = 'assets/farmingtales_drink_anim/'
read = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
baseline = read(BACKUP / 'baseline.json')
profiles = {p['id']: p for p in read(ASSETS / 'drink_profiles.json')}
fixed = {ns + ':' + name for ns, names in TARGETS.items() for name in names if not name.startswith('beer_')}
assert len(fixed) == 35
long_corks = []
jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.44.jar'
previous = BACKUP / 'farmingtales_drink_anim-1.8.43.jar'
assert sha(previous) == baseline['previous_sha256']
with zipfile.ZipFile(previous) as old:
    old_profiles = {p['id']: p for p in json.loads(old.read(PREFIX + 'drink_profiles.json'))}
    assert profiles.keys() == old_profiles.keys()
    for item, p in profiles.items():
        if item not in fixed:
            assert p == old_profiles[item], item + ' unrelated profile changed'
            continue
        assert not p['liquid'], item + ' still has moving liquid'
        assert not read(ASSETS / f"models/{p['neckModel']}.json")['elements'], item
        ns, name = item.split(':')
        with zipfile.ZipFile(JARS[ns]) as original:
            m = model(original, source(original, ns, name))
            cap_index, cut = NECKS.get(name, (0, 14))
            if name in SCREW:
                cut = m['elements'][cap_index]['from'][1]
            body = read(ASSETS / f"models/{p['bodyModel']}.json")
            by_name = {e['name']: e for e in body['elements']}
            floor_face = by_name['mouth_interior_floor']['faces']['up']
            texture = body['textures'][floor_face['texture'][1:]].split(':')[1]
            im = Image.open(ASSETS / f'textures/{texture}.png').convert('RGBA')
            u, v = floor_face['uv'][:2]
            assert abs((u*im.width/16)%1-.5) < 1e-7 and abs((v*im.height/16)%1-.5) < 1e-7
            assert im.getpixel((int(u*im.width/16), int(v*im.height/16)))[3] == 255
            assert not any(k.startswith('glass_') or k == 'mouth' for k in body['textures']), item
            for i, e in enumerate(m['elements']):
                if i == cap_index:
                    continue
                actual = by_name[f'source_{i}']
                for key in ('from', 'to', 'rotation'):
                    assert actual.get(key) == e.get(key), (item, i, key, 'source solid changed')
                for direction, face in e['faces'].items():
                    if direction == 'up' or face['texture'][1:] not in body['textures']:
                        continue
                    expected = deepcopy(face)
                    expected.pop('cullface', None)
                    assert actual['faces'][direction] == expected, (item, i, direction, 'source texels changed')
            for key, ref in m['textures'].items():
                if ref.startswith('#'):
                    ref = m['textures'][ref[1:]]
                tns, rel = ref.split(':')
                if tns == 'minecraft':
                    continue
                texture = ASSETS / ('textures/' + body['textures'][key].split(':')[1] + '.png')
                assert texture.read_bytes() == original.read(f'assets/{tns}/textures/{rel}.png'), (item, key)
        cap = read(ASSETS / f"models/{p['capModel']}.json")['elements']
        old_cap = json.loads(old.read(PREFIX + f"models/{p['capModel']}.json"))['elements'][0]
        if name in SCREW:
            assert len(cap) == 1
            for key in ('from', 'to'):
                assert cap[0][key] == old_cap[key], item + ' screw cap was lengthened'
            continue
        long_corks.append(item)
        original_height = old_cap['to'][1] - old_cap['from'][1]
        assert len(cap) == 3 and abs(max(e['to'][1] for e in cap)-min(e['from'][1] for e in cap)-3*original_height) < 1e-7
        for i, piece in enumerate(cap):
            for axis in (0, 2):
                assert piece['from'][axis] == old_cap['from'][axis] and piece['to'][axis] == old_cap['to'][axis]
            for direction in ('north', 'east', 'south', 'west'):
                assert piece['faces'].get(direction) == old_cap['faces'].get(direction), (item, 'cork texels stretched')
            assert abs(piece['to'][1] - (old_cap['to'][1]-i*original_height)) < 1e-7
    assert len(long_corks) == 33

    allowed_resources = {PREFIX + name for name in ('drink_profiles.json', 'hand_volumes.json')}
    allowed_resources |= {PREFIX + f'models/animation/letsdo/{item.replace(":", "/")}_{kind}.json'
                          for item in fixed for kind in ('body', 'cap', 'neck')}
    with zipfile.ZipFile(jar) as new:
        for entry in old.namelist():
            if entry.startswith('assets/') and entry not in allowed_resources:
                assert old.read(entry) == new.read(entry), entry + ' unrelated resource changed'
        for p in (ROOT / 'src/main/resources').rglob('*'):
            if p.is_file():
                assert new.read(p.relative_to(ROOT / 'src/main/resources').as_posix()) == p.read_bytes(), p
        assert 'version="1.8.44"' in new.read('META-INF/mods.toml').decode()
        assert not any('/smoke/' in name for name in new.namelist())

# The reported green bottle has a 4x4 neck and a 2x2 opening, all on source pixel boundaries.
creeper = read(ASSETS / 'models/animation/letsdo/vinery/creepers_crush_body.json')['elements']
rim = [e for e in creeper if e['name'].startswith('source_2_up_mouth_')]
assert len(rim) == 4
assert all(v == round(v) for e in rim for key in ('from', 'to') for v in e[key])
assert sum((e['to'][0]-e['from'][0])*(e['to'][2]-e['from'][2]) for e in rim) == 12
assert not any('open_glass_mouth' == e['name'] for e in creeper)

with zipfile.ZipFile(BACKUP / 'source.zip') as old:
    allowed_java = {'src/main/java/com/farmingtales/drinkanim/' + name for name in (
        'client/BottleRenderer.java', 'client/NeckRenderer.java', 'math/BottleClosures.java',
        'math/BottleFraming.java', 'math/DrinkMotion.java')}
    for entry in old.namelist():
        if entry.startswith('src/main/java/') and entry not in allowed_java:
            assert old.read(entry) == (ROOT / entry).read_bytes(), entry
for path, expected in baseline['source_mods'].items():
    assert sha(Path(path)) == expected
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256']

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(key)) == 0 for test in tests for key in ('failures', 'errors', 'skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 153
log = ROOT / 'preview/smoke-static-corks-1.8.44.log'
client = log.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 9' in client and 'BUILD SUCCESSFUL' in client and 'SILENT_CLIENT' in client
assert 'CATALOG_SMOKE FAIL' not in client and 'DRINK_SMOKE FAIL' not in client
checked = ['vinery:creepers_crush', 'vinery:apple_wine', 'vinery:clark_wine', 'brewery:whiskey_highland_hearth',
           'vinery:lilitu_wine', 'vinery:jellie_wine', 'vinery:cristel_wine', 'brewery:beer_haley', 'vinery:apple_cider']
for item in checked:
    assert f'CATALOG_SMOKE PASS {item} body neck and decoration textures are present' in client
    if item in fixed:
        assert f'CATALOG_SMOKE PASS {item} original fixed contents have no dynamic liquid pass' in client
    if item in long_corks:
        assert f'CATALOG_SMOKE PASS {item} baked cork is three times the original length' in client
report = dict(version='1.8.44', sha256=sha(jar), previous_sha256=baseline['previous_sha256'],
              configuration_sha256=sha(config), fixed_bottles=sorted(fixed), long_corks=sorted(long_corks),
              unit_tests=count, client_profiles=len(checked), client_items=checked,
              client_log=str(log), client_log_sha256=sha(log),
              scope='35 Vinery/Brewery whiskey bottles retain original fixed contents. 33 pull corks have '
                    'three times the original length with unscaled source side texels and clipped buried shanks. '
                    'Openings follow original cork footprints without a miniature replacement rim.')
followup = ROOT / 'preview/smoke-static-corks-fixed-1.8.44.log'
final_client = followup.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 3' in final_client and 'BUILD SUCCESSFUL' in final_client
assert 'CATALOG_SMOKE FAIL' not in final_client and 'DRINK_SMOKE FAIL' not in final_client
report.update(final_client_log=str(followup), final_client_log_sha256=sha(followup), final_client_profiles=3,
              final_client_scope='Repeat Creeper, Highland Hearth and Cristel after correcting mouth floor texel centers.')
(ROOT / 'verification-1.8.44.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print(f'Verified 1.8.44: {count} tests, 35 fixed bottles, 33 long corks, nine client profiles; SHA256 {sha(jar)}')
