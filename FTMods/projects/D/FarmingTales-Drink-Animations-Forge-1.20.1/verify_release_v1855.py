"""Verify Dassai's player-driven ribbon response with the bottle held idle."""
from pathlib import Path
import hashlib
import json
import re
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.54-before-dassai-view-motion'
ITEM = 'kaleidoscope_world_liquor:dassai'

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.55.jar'
previous = next(BACKUP.glob('*.jar'))
baseline = json.loads((ROOT / 'installation-1.8.54.json').read_text(encoding='utf-8-sig'))
assert sha(previous) == baseline['sha256']
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(jar) as new:
    assert set(old.namelist()) == set(new.namelist())
    changed = {p for p in old.namelist() if old.read(p) != new.read(p)}
    assert {p for p in changed if not p.endswith('.class')} == {'META-INF/mods.toml'}
    for p in changed-{'META-INF/mods.toml'}:
        assert p == 'com/farmingtales/drinkanim/client/BottleRenderer.class' or p.startswith('com/farmingtales/drinkanim/math/DecorationMotion'), p
    assert 'version="1.8.55"' in new.read('META-INF/mods.toml').decode()
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes()

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 171, count
logpath = ROOT / 'preview/verify-dassai-view-1.8.55.log'
log = logpath.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 1' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
for label in ('inspect preserves all three items', 'drink consumes once and returns to idle',
              'bottle animation ignores BrewLevel=6', 'drinking preserves remaining stack BrewLevel',
              'stow finishes with empty hand'):
    assert f'CATALOG_SMOKE PASS {ITEM} {label}' in log, label
captures, peaks = [], {}
for motion in ('look_left','look_right','look_up','look_down','walk_forward','walk_back','walk_left','walk_right'):
    assert f'CATALOG_SMOKE PASS {ITEM} manual ribbon test uses idle hold {motion}' in log
    if motion.startswith('walk_'):
        assert f'CATALOG_SMOKE PASS {ITEM} actual player movement {motion}' in log
    match = re.search(re.escape(f'CATALOG_SMOKE PASS {ITEM} ribbon follows manual {motion}, pixels=')+r'([\d.]+)',log)
    assert match and float(match[1]) > .25, motion
    peaks[motion] = float(match[1])
    for phase in (20,28,36,44):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-SteveSmoke-dassai-ribbon-{motion}-{phase}.png'
        assert path.stat().st_mtime > jar.stat().st_mtime, path
        captures.append(str(path))
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
report = dict(version='1.8.55', sha256=sha(jar), previous_sha256=sha(previous),
              configuration_sha256=sha(config), unit_tests=count, items=[ITEM],
              changed_jar_entries=sorted(changed), captures=captures, idle_ribbon_travel_model_pixels=peaks,
              client_logs=[dict(path=str(logpath), sha256=sha(logpath), profiles=1)],
              scope='Dassai cap ornament receives camera and player world motion as well as local bottle animation. Tested left/right/up/down view turns and forward/back/left/right walking with the idle bottle. All existing assets and other ornament routing unchanged. Isolated Forge client; complete modpack and shaders not tested.')
(ROOT / 'verification-1.8.55.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.55: {count} tests, 8 actual idle movement scenarios; SHA256 {sha(jar)}')
print('Idle ribbon travel:', peaks)
