"""Verify four rigid ornaments and retain Dassai's player-driven response."""
from pathlib import Path
import hashlib
import json
import re
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.55-before-rigid-ornaments'
ITEMS = ['kaleidoscope_world_liquor:' + name for name in
         ('dassai', 'maotai', 'bamboo_leaf_green_liquor', 'strongbow')]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.56.jar'
previous = next(BACKUP.glob('*.jar'))
baseline = json.loads((ROOT / 'installation-1.8.55.json').read_text(encoding='utf-8-sig'))
assert sha(previous) == baseline['sha256']
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(jar) as new:
    assert set(old.namelist()) == set(new.namelist())
    changed = {p for p in old.namelist() if old.read(p) != new.read(p)}
    definitions = 'assets/farmingtales_drink_anim/decorations.json'
    assert {p for p in changed if not p.endswith('.class')} == {'META-INF/mods.toml', definitions}
    for p in changed - {'META-INF/mods.toml', definitions}:
        assert p == 'com/farmingtales/drinkanim/client/BottleRenderer.class' or p.startswith('com/farmingtales/drinkanim/math/DecorationMotion'), p
    assert 'version="1.8.56"' in new.read('META-INF/mods.toml').decode()
    before, after = json.loads(old.read(definitions)), json.loads(new.read(definitions))
    for item in ITEMS:
        assert len(after[item]) == 1 and after[item][0]['ribbon'].pop('rigid') is True
    assert before == after, 'Only the four rigid flags should change; anchors and original shapes stay intact.'
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes()

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 171, count
names = {case.get('name') for test in tests for case in test.findall('testcase')}
assert 'wholeModelStaysRigidWithPinnedRootAndMaotaiNeverSwingsIntoItsBottleWall()' in names
logpath = ROOT / 'preview/verify-rigid-ornaments-1.8.56.log'
log = logpath.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 4' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
captures, inspect_peaks = [], {}
for item in ITEMS:
    for label in ('inspect preserves all three items', 'drink consumes once and returns to idle',
                  'stow finishes with empty hand'):
        assert f'CATALOG_SMOKE PASS {item} {label}' in log, (item, label)
    if not item.endswith(':strongbow'):
        for label in ('bottle animation ignores BrewLevel=6', 'drinking preserves remaining stack BrewLevel'):
            assert f'CATALOG_SMOKE PASS {item} {label}' in log, (item, label)
    match = re.search(re.escape(f'CATALOG_SMOKE PASS {item} rigid ornament visibly swings in rendered inspection, pixels=') + r'([\d.]+)', log)
    assert match and float(match[1]) > .25, item
    inspect_peaks[item] = float(match[1])
    for phase in (64, 88, 112, 144):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-SteveSmoke-{item.split(":")[1]}-ribbon-inspect-{phase}.png'
        assert path.stat().st_mtime > jar.stat().st_mtime, path
        captures.append(str(path))
peaks = {}
item = ITEMS[0]
for motion in ('look_left', 'look_right', 'look_up', 'look_down', 'walk_forward', 'walk_back', 'walk_left', 'walk_right'):
    assert f'CATALOG_SMOKE PASS {item} manual ribbon test uses idle hold {motion}' in log
    if motion.startswith('walk_'):
        assert f'CATALOG_SMOKE PASS {item} actual player movement {motion}' in log
    match = re.search(re.escape(f'CATALOG_SMOKE PASS {item} ribbon follows manual {motion}, pixels=') + r'([\d.]+)', log)
    assert match and float(match[1]) > .25, motion
    peaks[motion] = float(match[1])
    for phase in (20, 28, 36, 44):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-SteveSmoke-dassai-ribbon-{motion}-{phase}.png'
        assert path.stat().st_mtime > jar.stat().st_mtime, path
        captures.append(str(path))
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
report = dict(version='1.8.56', sha256=sha(jar), previous_sha256=sha(previous),
              configuration_sha256=sha(config), unit_tests=count, items=ITEMS,
              changed_jar_entries=sorted(changed), captures=captures,
              inspection_travel_model_pixels=inspect_peaks, dassai_idle_travel_model_pixels=peaks,
              client_logs=[dict(path=str(logpath), sha256=sha(logpath), profiles=4)],
              scope='Four ornaments rotate as complete original models around fixed anchors without bending. Maotai uses a rigid outward hinge. Dassai retains view and walking response in all eight idle scenarios. Existing models, textures, profiles and closures unchanged. Isolated Forge client; complete modpack and shaders not tested.')
(ROOT / 'verification-1.8.56.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.56: {count} tests, 4 bottle sequences, 8 actual idle movement scenarios; SHA256 {sha(jar)}')
print('Inspection travel:', inspect_peaks)
print('Dassai idle travel:', peaks)
