"""Verify Highland Hearth's label, blue glass and moving whisky against source pixels."""
from pathlib import Path
import hashlib
import io
import json
import re
import zipfile
import xml.etree.ElementTree as ET
from PIL import Image

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
BACKUP = ROOT / 'backups/1.8.58-before-highland-materials'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.59.jar'
PREFIX = 'assets/farmingtales_drink_anim/textures/letsdo/brewery/whiskey_highland_hearth_'
FACES = ('north', 'east', 'south', 'west', 'up', 'down')
GLASS = {(93,143,194), (179,207,236), (57,92,121), (249,253,253)}
LIQUOR = {(129,55,28), (121,69,40), (83,33,22), (181,115,58),
          (239,133,62), (254,228,141), (252,188,76), (255,241,206)}

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

def image(data):
    return Image.open(io.BytesIO(data)).convert('RGBA')

previous = next(BACKUP.glob('*.jar'))
baseline = json.loads((ROOT / 'installation-1.8.58.json').read_text(encoding='utf-8-sig'))
assert sha(previous) == baseline['sha256']
source = next((PACK / 'mods').glob('*letsdo-brewery-forge*.jar'))
counts = dict(label_pixels_preserved=0, glass_pixels_preserved=0, liquid_pixels_removed=0)
with zipfile.ZipFile(source) as original, zipfile.ZipFile(previous) as old, zipfile.ZipFile(JAR) as new:
    assert set(old.namelist()) == set(new.namelist()), 'Unexpected added/removed packaged files.'
    changed = {name for name in old.namelist() if old.read(name) != new.read(name)}
    expected = {PREFIX + 'glass_0_' + face + '.png' for face in FACES}
    expected |= {PREFIX + 'liquid.png', 'META-INF/mods.toml'}
    assert changed == expected, changed
    assert 'version="1.8.59"' in new.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in name for name in new.namelist())
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes(), path
    particle = image(original.read('assets/brewery/textures/block/whiskey_highland_hearth.png'))
    top = image(original.read('assets/brewery/textures/block/whiskey_highland_hearth_top.png'))
    for face in FACES:
        side = face not in ('up', 'down')
        source_face = (particle.crop((3,6,13,16)) if side else top.crop((3,3,13,13)))
        shell = image(new.read(PREFIX + 'glass_0_' + face + '.png'))
        assert shell.size == (40,40)
        for y in range(40):
            for x in range(40):
                before = source_face.getpixel((x//4,y//4))
                after = shell.getpixel((x,y))
                label = side and 20 <= y < 32
                if label or before[:3] in GLASS:
                    assert after == before, (face,x,y,'label/glass changed')
                    counts['label_pixels_preserved' if label else 'glass_pixels_preserved'] += 1
                else:
                    assert before[:3] in LIQUOR and before[3] == 255, (face,x,y,before)
                    assert after[3] == 0, (face,x,y,'fixed liquid remains')
                    counts['liquid_pixels_removed'] += 1
        if not side:
            assert shell.crop((4,4,36,36)).getchannel('A').getextrema() == (0,0), 'Brown corner remains.'
    liquid = image(new.read(PREFIX + 'liquid.png'))
    assert liquid.size == (32,16) and liquid.getchannel('A').getextrema() == (255,255)
    assert {c[:3] for c in liquid.getdata()} == LIQUOR, 'Moving liquid must retain the original dark corners too.'
    aliases = json.loads(new.read('assets/farmingtales_drink_anim/aged_aliases.json'))
    for tier in ('i','g','d'):
        assert aliases['farmingtales:brewery_whiskey_highland_hearth_' + tier] == 'brewery:whiskey_highland_hearth'

tests = [ET.parse(path).getroot() for path in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(k)) == 0 for test in tests for k in ('errors','failures','skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 173, count
logpath = ROOT / 'preview/verify-highland-materials-1.8.59.log'
log = logpath.read_text(encoding='utf-8-sig', errors='replace')
assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 1' in log
assert not re.search(r'(?:AGED|DRINK|CATALOG)_SMOKE FAIL', log)
for label in ('body neck and decoration textures are present', 'Highland Hearth uses dynamic whisky',
              'drink consumes once and returns to idle', 'stow finishes with empty hand',
              'actual walking continuously moves the liquid'):
    assert 'CATALOG_SMOKE PASS brewery:whiskey_highland_hearth ' + label in log, label
captures = []
for phase in ('idle','inspect','open-neck','drink','replacement','low-fill-246','low-fill-250','low-fill-252',
              'walking-390','walking-398','surface-pitch--80.0','surface-pitch--25.0',
              'surface-pitch-0.0','surface-pitch-25.0','surface-pitch-80.0'):
    path = ROOT / f'run-smoke/screenshots/v184-catalog-SteveSmoke-whiskey_highland_hearth-{phase}.png'
    assert path.stat().st_mtime > JAR.stat().st_mtime, path
    captures.append(str(path))
config = PACK / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
report = dict(version='1.8.59', sha256=sha(JAR), previous_sha256=sha(previous),
              configuration_sha256=sha(config), source_mod_sha256=sha(source), unit_tests=count,
              material_checks=counts, aged_tiers=['iron','gold','diamond'],
              changed_jar_entries=sorted(changed), captures=captures,
              client_logs=[dict(path=str(logpath),sha256=sha(logpath),profiles=1)],
              scope='Only Highland Hearth six glass face masks and moving liquor palette change. Source paper label and blue glass including white reflections are preserved exactly. All liquor pixels including dark corners are removed from the fixed shell and all eight original liquor colors are in the moving atlas. All classes, models, profiles, aged aliases and other assets match 1.8.58 byte-for-byte. All three aged tiers reuse this corrected normal profile. Isolated Forge client includes drinking, low-fill, walking and five pitch captures; full modpack and shaders not tested.')
(ROOT / 'verification-1.8.59.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.59: {count} tests, {counts}, Highland client sequence; SHA256 {sha(JAR)}')
