"""Verify the three held-beer overflow textures against the user's installed release."""
from pathlib import Path
import hashlib
import io
import json
import re
import tomllib
import zipfile
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
BACKUP = ROOT / 'backups/1.8.59-before-beer-overflow'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.60.jar'
PREFIX = 'assets/farmingtales_drink_anim/textures/letsdo/brewery/'
NAMES = ('beer_wheat', 'beer_nettle', 'beer_haley')
SPILL = {(9,0), (10,0), (9,1), (10,1), (10,2), (5,2), (5,3), (3,5), (4,5)}


def native(path):
    return Path('\\\\?\\' + str(path.resolve()))


def sha(path):
    return hashlib.sha256(native(path).read_bytes()).hexdigest()


def png(data):
    return Image.open(io.BytesIO(data)).convert('RGBA')


baseline = json.loads((BACKUP / 'baseline.json').read_text(encoding='utf-8'))
previous = BACKUP / Path(baseline['installed_path']).name
assert sha(previous) == baseline['previous_sha256']
source = next((PACK / 'mods').glob('*letsdo-brewery-forge*.jar'))
assert sha(source) == 'c8ae46cac66ab2a054cac6f9353512855c2cebaf5eb2eacc38503a50f05c56ea'
checks = {}
with zipfile.ZipFile(native(previous)) as old, zipfile.ZipFile(JAR) as new, zipfile.ZipFile(source) as brewery:
    assert set(old.namelist()) == set(new.namelist()), 'Unexpected packaged files added or removed.'
    changed = {name for name in old.namelist() if old.read(name) != new.read(name)}
    assert changed == {'META-INF/mods.toml'} | {PREFIX + name + '_1.png' for name in NAMES}, changed
    old_meta = tomllib.loads(baseline['user_metadata'])
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.60'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta, 'User metadata changed.'
    for name, digest in baseline['preserve_hashes'].items():
        assert hashlib.sha256(new.read(name)).hexdigest() == digest, name
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes(), path
    for name in NAMES:
        before = png(old.read(PREFIX + name + '_1.png'))
        after = png(new.read(PREFIX + name + '_1.png'))
        original = png(brewery.read('assets/brewery/textures/block/' + name + '.png'))
        assert before.size == after.size == original.size == (16, 16)
        assert before.tobytes() == original.tobytes()
        actual = {(x,y) for y in range(16) for x in range(16) if before.getpixel((x,y)) != after.getpixel((x,y))}
        assert actual == SPILL, (name, actual)
        assert after.getchannel('A').tobytes() == before.getchannel('A').tobytes()
        foam = png(new.read(PREFIX + name + '_foam.png')).crop((1,1,5,5))
        foam_colors = set(foam.getdata())
        assert all(after.getpixel(point) in foam_colors for point in SPILL)
        checks[name] = dict(changed_pixels=len(actual), foam_palette_matched=True, alpha_preserved=True)

tests = [ET.parse(path).getroot() for path in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(k)) == 0 for test in tests for k in ('errors','failures','skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 173, count
logpath = ROOT / 'preview/verify-beer-overflow-1.8.60.log'
raw_log = logpath.read_bytes()
log = raw_log.decode('utf-16' if raw_log.startswith((b'\xff\xfe', b'\xfe\xff')) else 'utf-8-sig', errors='replace')
assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 3' in log
assert not re.search(r'(?:AGED|DRINK|CATALOG)_SMOKE FAIL', log)
captures = []
for name in NAMES:
    for label in ('body neck and decoration textures are present', 'beer foam atlas loaded',
                  'inspect preserves all three items', 'drink consumes once and returns to idle',
                  'actual walking continuously moves the liquid'):
        assert 'CATALOG_SMOKE PASS brewery:' + name + ' ' + label in log, (name,label)
    for phase in ('idle','inspect','drink','replacement','walking-390','walking-398',
                  'surface-pitch--80.0','surface-pitch--25.0','surface-pitch-0.0',
                  'surface-pitch-25.0','surface-pitch-80.0','source-comparison'):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-SteveSmoke-{name}-{phase}.png'
        assert path.stat().st_mtime > JAR.stat().st_mtime, path
        captures.append(str(path))
config = PACK / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256'] == sha(BACKUP / config.name)
report = dict(version='1.8.60', sha256=sha(JAR), previous_sha256=sha(previous),
              configuration_sha256=sha(config), source_mod_sha256=sha(source), unit_tests=count,
              texture_checks=checks, changed_jar_entries=sorted(changed), captures=captures,
              client_logs=[dict(path=str(logpath),sha256=sha(logpath),profiles=3)],
              scope='Wheat, Nettle and Haley held mugs receive the existing Barley overflow silhouette, colored from their own foam. Exactly nine atlas pixels per mug change; alpha is preserved. All runtime classes, models, profiles, aged aliases and other assets match the user-customized 1.8.59 JAR byte-for-byte. Metadata changes only the version. Original Brewery mod and user configuration remain unchanged. Isolated Forge client covers inspect, drink, walking and five pitch views; full modpack and shaders not tested.')
(ROOT / 'verification-1.8.60.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.60: {count} tests, three beer client sequences; SHA256 {sha(JAR)}')
