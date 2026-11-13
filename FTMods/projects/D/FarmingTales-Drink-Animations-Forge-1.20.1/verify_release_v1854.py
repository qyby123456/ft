"""Audit the four flexible ornaments and the completed tagged client sequences."""
from pathlib import Path
import hashlib
import json
import re
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.53-before-ribbon-motion'
ITEMS = ['kaleidoscope_world_liquor:'+name for name in ('bamboo_leaf_green_liquor','maotai','dassai','strongbow')]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.54.jar'
previous = next(BACKUP.glob('*.jar'))
baseline = json.loads((ROOT / 'installation-1.8.53.json').read_text(encoding='utf-8-sig'))
assert sha(previous) == baseline['sha256']
prefix = 'assets/farmingtales_drink_anim/'
body = prefix+'models/animation/tavern_bottles/maotai_body.json'
ribbon = prefix+'models/animation/tavern_bottles/maotai_ribbon.json'
decoration = prefix+'decorations.json'
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(jar) as new:
    assert set(old.namelist()) <= set(new.namelist())
    changed = {p for p in old.namelist() if old.read(p) != new.read(p)}
    resources = {'META-INF/mods.toml', decoration, body}
    assert {p for p in changed if not p.endswith('.class')} == resources
    for p in changed-resources:
        assert p == 'com/farmingtales/drinkanim/client/BottleRenderer.class' or p.startswith('com/farmingtales/drinkanim/math/DecorationMotion'), p
    added = set(new.namelist())-set(old.namelist())
    assert added == {ribbon, 'com/farmingtales/drinkanim/client/RibbonRenderer.class',
                     'com/farmingtales/drinkanim/client/RibbonRenderer$Point.class',
                     'com/farmingtales/drinkanim/math/DecorationMotion$Ribbon.class'}, added
    before, after = json.loads(old.read(decoration)), json.loads(new.read(decoration))
    assert before.keys() == after.keys()
    assert {k for k in before if before[k] != after[k]} == set(ITEMS)
    for item in ITEMS:
        part = after[item][0]
        assert part['ribbon']['gain'] > 2
        if not item.endswith(':maotai'):
            assert {k:v for k,v in part.items() if k != 'ribbon'} == before[item][0]
    original, extracted = json.loads(old.read(body)), json.loads(new.read(ribbon))
    remaining = json.loads(new.read(body))
    removed = next(e for e in original['elements'] if e.get('name') == 'source_3')
    original['elements'].remove(removed)
    assert original == remaining
    assert extracted['elements'] == [removed]
    assert extracted['textures'] == remaining['textures']
    assert 'version="1.8.54"' in new.read('META-INF/mods.toml').decode()
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes()

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 169, count
logpath = ROOT / 'preview/verify-ribbon-motion-1.8.54.log'
log = logpath.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 4' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
captures, peaks = [], {}
for item in ITEMS:
    for label in ('inspect preserves all three items', 'drink consumes once and returns to idle',
                  'bottle animation ignores BrewLevel=6', 'bottle animation works without BrewLevel',
                  'drinking preserves remaining stack BrewLevel', 'stow finishes with empty hand'):
        assert f'CATALOG_SMOKE PASS {item} {label}' in log, (item, label)
    match = re.search(re.escape('CATALOG_SMOKE PASS '+item+' flexible ribbon visibly bends in rendered inspection, pixels=')+r'([\d.]+)',log)
    assert match and float(match[1]) > .25, item
    peaks[item] = float(match[1])
    for stage in ('idle','ribbon-inspect-80','ribbon-inspect-112','ribbon-inspect-144','opening','drink','replacement'):
        path = ROOT / f"run-smoke/screenshots/v184-catalog-SteveSmoke-{item.split(':')[1]}-{stage}.png"
        assert path.stat().st_mtime > jar.stat().st_mtime, path
        captures.append(str(path))
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
report = dict(version='1.8.54', sha256=sha(jar), previous_sha256=sha(previous),
              configuration_sha256=sha(config), unit_tests=count, items=ITEMS,
              changed_jar_entries=sorted(changed), added_jar_entries=sorted(added), captures=captures,
              ribbon_tip_travel_model_pixels=peaks,
              client_logs=[dict(path=str(logpath), sha256=sha(logpath), profiles=4)],
              scope='Four original ornaments bend with larger damped flutter and axial-turn response. Maotai ribbon separated without changing its original texture or geometry; roots stay attached. All drink profiles, closures, other decorations and XO motion retained. Isolated Forge client; complete modpack and shaders not tested.')
(ROOT / 'verification-1.8.54.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.54: {count} tests, 4 client sequences, visible ribbon motion; SHA256 {sha(jar)}')
print('Ribbon travel:', peaks)
