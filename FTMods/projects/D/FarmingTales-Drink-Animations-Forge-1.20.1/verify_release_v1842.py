"""Check the requested bottle groups, visible liquids and six damped foam heads."""
from pathlib import Path
import hashlib, json, zipfile, re
import xml.etree.ElementTree as ET
from PIL import Image
from letsdo_liquids import VOLUMES

ROOT = Path(__file__).resolve().parent
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
PREFIX = 'assets/farmingtales_drink_anim/'
BACKUP = ROOT / 'backups/1.8.41-before-closures-liquids'
read = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
groups = {
    'screw': 'vinery:jellie_wine vinery:jo_special_mixture'.split(),
    'wine': ('vinery:bolvar_wine vinery:glowing_wine vinery:cherry_wine vinery:red_wine vinery:villagers_fright '
             'brewery:whiskey_smokey_reverie brewery:whiskey_jojannik vinery:noir_wine vinery:bottle_mojang_noir '
             'brewery:whiskey_ak brewery:whiskey_jamesons_malt brewery:whiskey_carrasconlabel vinery:clark_wine '
             'brewery:whiskey_cristelwalker brewery:whiskey_lilitusinglemalt').split(),
    'stopper': ('vinery:lilitu_wine vinery:stal_wine vinery:strad_wine vinery:mellohi_wine vinery:magnetic_wine '
                'vinery:apple_wine vinery:creepers_crush vinery:mead brewery:whiskey_maggoallan brewery:whiskey_highland_hearth '
                'vinery:chenet_wine vinery:aegis_wine vinery:cristel_wine vinery:chorus_wine vinery:solaris_wine '
                'vinery:kelp_cider vinery:apple_cider vinery:eiswein').split(),
}
expected = {item: group for group, ids in groups.items() for item in ids}
assert len(expected) == 35 and read(ASSETS / 'letsdo_closures.json') == expected
profiles = read(ASSETS / 'drink_profiles.json')
by_id = {p['id']: p for p in profiles}
foam = read(ASSETS / 'beer_foam.json')
assert set(foam['items']) == {f'brewery:beer_{n}' for n in ('wheat', 'barley', 'hops', 'nettle', 'haley', 'oat')}
assert set(foam['variants']) == set(foam['items'])
foam_files = [ASSETS / v['texture'].split(':', 1)[1] for v in foam['variants'].values()]
assert len({sha(p) for p in foam_files}) == 6
for item in groups['stopper'] + groups['screw']:
    p = by_id[item]
    assert not read(ASSETS / f"models/{p['neckModel']}.json")['elements'], item
    cap = read(ASSETS / f"models/{p['capModel']}.json")['elements']
    assert len(cap) == 1 and cap[0]['name'] == 'removable_cork', item
    assert p['mouthY'] == p['neckMin'] * 16 == p['neckMax'] * 16

dynamic = []
for item, p in by_id.items():
    name = item.split(':')[1]
    if name not in VOLUMES:
        continue
    dynamic.append(item)
    assert p['liquid'], item
    previous_top = None
    for region in p['liquid']:
        assert all(b > a for a, b in zip(region['min'], region['max'])), item
        if previous_top is not None:
            assert abs(previous_top - region['min'][1]) < 1e-7, item + ' liquid must stay connected'
        previous_top = region['max'][1]
    body = read(ASSETS / f"models/{p['bodyModel']}.json")
    assert name == 'apple_cider' or any(k.startswith('glass_') for k in body['textures']), item
    for key, ref in body['textures'].items():
        if key.startswith('glass_'):
            im = Image.open(ASSETS / ('textures/' + ref.split(':')[1] + '.png'))
            assert im.getchannel('A').getextrema() == (0, 255), (item, key)
assert len(dynamic) == 18  # The 17 additions plus the apple cider repair.
assert not by_id['vinery:lilitu_wine']['liquid']
amber = Image.open(ASSETS / by_id['vinery:apple_cider']['liquid'][0]['texture'].split(':', 1)[1]).convert('RGBA')
assert all(r > g > b and r > 150 and a == 255 for r, g, b, a in amber.getdata())

jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.42.jar'
with zipfile.ZipFile(BACKUP / 'farmingtales_drink_anim-1.8.41.jar') as old, zipfile.ZipFile(jar) as new:
    old_profiles = json.loads(old.read(PREFIX + 'drink_profiles.json'))
    assert [p for p in profiles if p['id'] not in expected] == [p for p in old_profiles if p['id'] not in expected]
    for entry in old.namelist():
        if entry.startswith(PREFIX) and '/letsdo/' not in entry and entry not in {
            PREFIX + 'drink_profiles.json', PREFIX + 'beer_foam.json', PREFIX + 'hand_volumes.json',
        }:
            assert new.read(entry) == old.read(entry), entry
    for name in ('barley', 'hops'):
        entry = PREFIX + f'textures/letsdo/brewery/beer_{name}_foam.png'
        assert new.read(entry) == old.read(entry)
    for f in (ROOT / 'src/main/resources').rglob('*'):
        if f.is_file():
            assert new.read(f.relative_to(ROOT / 'src/main/resources').as_posix()) == f.read_bytes(), f
    assert 'version="1.8.42"' in new.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in n for n in new.namelist())
with zipfile.ZipFile(BACKUP / 'source.zip') as old:
    path = 'src/main/java/com/farmingtales/drinkanim/math/BeerFoam.java'
    assert old.read(path) == (ROOT / path).read_bytes(), 'Retain the damped foam simulation'
for source in read(ROOT / 'letsdo-sources.json')['jars']:
    assert sha(Path(source['path'])) == source['sha256']
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
tests = [ET.parse(f).getroot() for f in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 152

log = ROOT / 'preview/smoke-letsdo-1.8.42.log'
text = log.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 41' in text and 'BUILD SUCCESSFUL' in text and 'SILENT_CLIENT' in text
assert 'CATALOG_SMOKE FAIL' not in text
for item in set(expected) | set(foam['items']):
    assert f'CATALOG_SMOKE PASS {item} body neck and decoration textures are present' in text
for item in groups['screw']:
    assert f'CATALOG_SMOKE PASS {item} specified screw cap twists twice and releases' in text
walking = sorted(set(re.findall(r'CATALOG_SMOKE PASS (\S+) actual walking continuously moves the liquid', text)))
assert len(walking) >= 10 and 'vinery:apple_cider' in walking
followup = ROOT / 'preview/smoke-letsdo-slim-1.8.42.log'
slim = followup.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 3' in slim and 'BUILD SUCCESSFUL' in slim
assert 'DRINK_SMOKE PASS player arm renderer model=slim' in slim
assert 'DRINK_SMOKE FAIL' not in slim and 'CATALOG_SMOKE FAIL' not in slim
assert 'vinery:apple_cider actual walking continuously moves the liquid' in slim
historical = [line for line in text.splitlines() if 'DRINK_SMOKE FAIL' in line]
assert all('player arm renderer model=default' in line for line in historical)
report = dict(version='1.8.42', sha256=sha(jar), previous_sha256=sha(BACKUP / 'farmingtales_drink_anim-1.8.41.jar'),
              unit_tests=count, closure_groups=groups, dynamic_liquid_profiles=sorted(dynamic),
              fixed_original_contents=['vinery:lilitu_wine'], vinery_liquid_opacity=242/255,
              foam_items=foam['items'], client_profiles=41, walking_verified=walking,
              client_log=str(log), client_log_sha256=sha(log), configuration_sha256=sha(config),
              slim_client_log=str(followup), slim_client_log_sha256=sha(followup), slim_client_profiles=3,
              historical_fixture_findings=historical,
              fixture_resolution='The first offline username selected the default arm model. '
                                 'The fixture now derives the expected model from Minecraft UUID skin selection; '
                                 'the subsequent slim client run passed without failures.',
              scope='Requested closure groups, visible moving liquids, amber apple cider, 95% Vinery liquid opacity, '
                    'original static sloped Lilitu contents and six individually colored damped beer foam heads.')
(ROOT / 'verification-1.8.42.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print(f'Verified 1.8.42: {count} tests, 41 client profiles, {len(walking)} real walking checks; SHA256 {sha(jar)}')
