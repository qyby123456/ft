"""Verify opaque beers against source pixels, source face UVs and client captures."""
from pathlib import Path
import hashlib
import io
import json
import zipfile
import xml.etree.ElementTree as ET
from PIL import Image
from research_letsdo import JARS, model, source
from liquid_textures import patch

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.42-before-original-beer-textures'
ASSETS = ROOT / 'src/main/resources/assets/farmingtales_drink_anim'
PREFIX = 'assets/farmingtales_drink_anim/'
read = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
baseline = read(BACKUP / 'baseline.json')
beers = [f'brewery:beer_{name}' for name in ('wheat', 'barley', 'hops', 'nettle', 'haley', 'oat')]
profiles = {p['id']: p for p in read(ASSETS / 'drink_profiles.json')}
foam = read(ASSETS / 'beer_foam.json')
assert set(foam['items']) == set(foam['variants']) == set(beers)
with zipfile.ZipFile(JARS['brewery']) as original:
    for item in beers:
        name = item.split(':')[1]
        m = model(original, source(original, 'brewery', name))
        for kind, element in [('liquid', 4), ('foam', 5 if name == 'beer_hops' else 4)]:
            top = m['elements'][element]['faces']['up']
            namespace, texture = m['textures'][top['texture'][1:]].split(':')
            expected = Image.open(io.BytesIO(original.read(f'assets/{namespace}/textures/{texture}.png'))).convert('RGBA')
            entry = profiles[item]['liquid'][0] if kind == 'liquid' else foam['variants'][item]
            actual = Image.open(ASSETS / entry['texture'].split(':')[1]).convert('RGBA')
            assert actual.size == expected.size and actual.tobytes() == expected.tobytes(), (item, kind, 'source pixels')
            assert entry['surfaceUV'] == top['uv'], (item, kind, 'source UVs')
            for face in ('sideUV', 'surfaceUV'):
                assert patch(actual, entry[face]).getchannel('A').getextrema() == (255, 255), (item, kind, face)
        fixture = read(ROOT / f'src/smoke/resources/assets/brewery/models/block/{name}.json')
        assert fixture['elements'] == m['elements'], item + ' comparison uses the original model'

jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.43.jar'
previous = BACKUP / 'farmingtales_drink_anim-1.8.42.jar'
assert sha(previous) == baseline['previous_sha256']
allowed_assets = {PREFIX + name for name in ('drink_profiles.json', 'beer_foam.json')}
allowed_assets |= {PREFIX + f'textures/letsdo/brewery/{item.split(":")[1]}_{kind}.png'
                   for item in beers for kind in ('foam', 'liquid')}
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(jar) as new:
    old_profiles = {p['id']: p for p in json.loads(old.read(PREFIX + 'drink_profiles.json'))}
    assert profiles.keys() == old_profiles.keys()
    for item, p in profiles.items():
        if item not in beers:
            assert p == old_profiles[item], item + ' unrelated profile changed'
        else:
            assert {k: v for k, v in p.items() if k != 'liquid'} == {k: v for k, v in old_profiles[item].items() if k != 'liquid'}
            assert len(p['liquid']) == len(old_profiles[item]['liquid']) == 1
            for key in ('min', 'max', 'texture'):
                assert p['liquid'][0][key] == old_profiles[item]['liquid'][0][key]
    for entry in old.namelist():
        if entry.startswith('assets/') and entry not in allowed_assets:
            assert old.read(entry) == new.read(entry), entry + ' unrelated resource changed'
    for p in (ROOT / 'src/main/resources').rglob('*'):
        if p.is_file():
            assert new.read(p.relative_to(ROOT / 'src/main/resources').as_posix()) == p.read_bytes(), p
    assert 'version="1.8.43"' in new.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in name for name in new.namelist())
with zipfile.ZipFile(BACKUP / 'source.zip') as old:
    allowed_java = {'src/main/java/com/farmingtales/drinkanim/' + name
                    for name in ('client/BottleRenderer.java', 'math/DrinkProfile.java')}
    for entry in old.namelist():
        if entry.startswith('src/main/java/') and entry not in allowed_java:
            assert old.read(entry) == (ROOT / entry).read_bytes(), entry + ' unrelated code changed'
for p, expected in baseline['source_mods'].items():
    assert sha(Path(p)) == expected
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256']

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(key)) == 0 for test in tests for key in ('failures', 'errors', 'skipped'))
test_count = sum(int(test.get('tests')) for test in tests)
assert test_count == 152
log = ROOT / 'preview/smoke-beer-1.8.43.log'
client = log.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 6' in client and 'BUILD SUCCESSFUL' in client and 'SILENT_CLIENT' in client
assert 'CATALOG_SMOKE FAIL' not in client and 'DRINK_SMOKE FAIL' not in client
captures = []
for item in beers:
    assert f'CATALOG_SMOKE PASS {item} body neck and decoration textures are present' in client
    capture = ROOT / f'run-smoke/screenshots/v184-catalog-BeerOriginal-{item.split(":")[1]}-source-comparison.png'
    assert capture.is_file(), capture
    captures.append(str(capture))
report = dict(version='1.8.43', sha256=sha(jar), previous_sha256=baseline['previous_sha256'],
              configuration_sha256=sha(config), unit_tests=test_count, client_profiles=6,
              foam_items=beers, beer_liquid_alpha=255, original_texture_pixel_checks=12,
              original_surface_uv_checks=12, client_log=str(log), client_log_sha256=sha(log),
              comparison_screenshots=captures,
              scope='Six Brewery beers use their own unchanged source pixels and source surface UVs, '
                    'without whitening, borrowed textures or translucency. Damped foam motion is retained.')
(ROOT / 'verification-1.8.43.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print(f'Verified 1.8.43: {test_count} tests, six client profiles, 12 original images and face UVs; SHA256 {sha(jar)}')
