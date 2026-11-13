"""Verify the coconut-only animation release against the installed 1.8.70 backup."""
from pathlib import Path
import hashlib, io, json, zipfile, tomllib
import xml.etree.ElementTree as ET
from PIL import Image
from papercraft_repairs import coconut_interior

ROOT = Path('\\\\?\\' + str(Path(__file__).resolve()).removeprefix('\\\\?\\')).parent
BACKUP = ROOT / 'backups/1.8.70-before-coconut-straw'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.71.jar'
baseline = json.loads((BACKUP / 'baseline.json').read_text())
previous = BACKUP / Path(baseline['installed_path']).name
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(previous) == baseline['previous_sha256']
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(JAR) as new:
    added = set(new.namelist()) - set(old.namelist())
    assert added == {'com/farmingtales/drinkanim/math/CoconutAnimations.class'}
    assert not set(old.namelist()) - set(new.namelist())
    changed = {n for n in old.namelist() if old.read(n) != new.read(n)}
    model = 'assets/farmingtales_drink_anim/models/animation/papercraft/ferry_whiskey.json'
    allowed = {'META-INF/mods.toml', model, 'com/farmingtales/drinkanim/client/BottleRenderer.class'}
    allowed |= {'com/farmingtales/drinkanim/math/' + n + '.class' for n in (
        'AnimationSoundTimeline', 'BottleRig', 'CupAnimations', 'DecorationMotion',
        'DecorationMotion$2', 'DecorationMotion$State', 'DrinkMotion')}
    assert changed == allowed, changed
    before, after = json.loads(old.read(model)), json.loads(new.read(model))
    assert after == dict(before, elements=[e for e in before['elements'] if e['name'] != 'inner_wood_floor'] + coconut_interior())
    texture = Image.open(io.BytesIO(new.read('assets/farmingtales_drink_anim/textures/papercraft/ferry_whiskey_0.png'))).convert('RGBA')
    for element in coconut_interior():
        for face in element['faces'].values():
            uv = face['uv']
            patch = texture.crop(tuple(round(v * 2) for v in uv))
            assert patch.getchannel('A').getextrema() == (255, 255)
            assert all(min(pixel[:3]) >= 235 for pixel in patch.getdata())
    old_meta = tomllib.loads(old.read('META-INF/mods.toml').decode())
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.71'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta
    for p in (ROOT / 'src/main/resources').rglob('*'):
        if p.is_file():
            assert new.read(p.relative_to(ROOT / 'src/main/resources').as_posix()) == p.read_bytes(), p

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 197
logpath = ROOT / 'preview/verify-coconut-slim-1.8.71.log'
raw = logpath.read_bytes()
log = raw.decode('utf-16' if raw.startswith((b'\xff\xfe', b'\xfe\xff')) else 'utf-8-sig', errors='replace')
assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 1' in log and 'SMOKE FAIL' not in log
for label in ('body neck and decoration textures are present', 'inspect preserves all three items',
              'drink consumes once and returns to idle', 'stow finishes with empty hand'):
    assert 'CATALOG_SMOKE PASS papercraft_magic_decoration:ferry_whiskey ' + label in log
for phase in ('idle', 'coconut-inspect-28', 'coconut-inspect-60', 'coconut-drink-24', 'coconut-drink-60', 'coconut-drink-66'):
    capture = ROOT / ('run-smoke/screenshots/v184-catalog-SlimSteve29-ferry_whiskey-' + phase + '.png')
    assert capture.stat().st_mtime > JAR.stat().st_mtime
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256']
report = dict(version='1.8.71', sha256=sha(JAR), previous_sha256=sha(previous),
    configuration_sha256=sha(config), unit_tests=count, changed_jar_entries=sorted(changed),
    added_jar_entries=sorted(added), client_log=str(logpath),
    scope='Ferry whiskey only: upright straw drinking, mirrored mouth contact, supported coconut inspection, fixed striped straw, opaque original coconut flesh on four inner walls and floor, no residual liquid after emptying. Original exterior, straw, fruit and texture assets are preserved. Independent Forge client replay passed; full modpack shader replay not performed.')
(ROOT / 'verification-1.8.71.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.71: {count} tests, coconut client replay, opaque flesh interior, preserved metadata and configuration.')
