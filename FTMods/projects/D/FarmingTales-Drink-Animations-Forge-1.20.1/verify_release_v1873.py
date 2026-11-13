"""Verify continuous straw, hand and camera motion while preserving every resource from 1.8.72."""
from pathlib import Path
import hashlib, json, zipfile, tomllib, csv, math
import xml.etree.ElementTree as ET

ROOT = Path('\\\\?\\' + str(Path(__file__).resolve()).removeprefix('\\\\?\\')).parent
BACKUP = ROOT / 'backups/1.8.72-before-lively-straw'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.73.jar'
baseline = json.loads((BACKUP / 'baseline.json').read_text())
previous = BACKUP / Path(baseline['installed_path']).name
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(previous) == baseline['previous_sha256']
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(JAR) as new:
    assert set(new.namelist()) == set(old.namelist())
    changed = {n for n in old.namelist() if old.read(n) != new.read(n)}
    expected = {'META-INF/mods.toml', 'com/farmingtales/drinkanim/client/BottleRenderer.class'} | {'com/farmingtales/drinkanim/math/' + n + '.class'
                                      for n in ('CoconutAnimations', 'BottleRig', 'DrinkMotion', 'DecorationMotion', 'DecorationMotion$2', 'DecorationMotion$State')}
    assert changed == expected, changed
    old_meta = tomllib.loads(old.read('META-INF/mods.toml').decode())
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.73'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta
    for p in (ROOT / 'src/main/resources').rglob('*'):
        if p.is_file():
            assert new.read(p.relative_to(ROOT / 'src/main/resources').as_posix()) == p.read_bytes(), p

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 200, count
logs = []
for hand, username in [('slim', 'SlimSteve29'), ('left', 'CoconutLeft')]:
    logpath = ROOT / f'preview/verify-coconut-live-{hand}-1.8.73.log'
    raw = logpath.read_bytes()
    log = raw.decode('utf-16' if raw.startswith((b'\xff\xfe', b'\xfe\xff')) else 'utf-8-sig', errors='replace')
    assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 1' in log and 'SMOKE FAIL' not in log
    for label in ('body neck and decoration textures are present', 'inspect preserves all three items',
                  'drink consumes once and returns to idle', 'stow finishes with empty hand'):
        assert 'CATALOG_SMOKE PASS papercraft_magic_decoration:ferry_whiskey ' + label in log
    for phase in ('idle', 'coconut-inspect-60', 'coconut-drink-12', 'coconut-drink-24', 'coconut-drink-50', 'coconut-drink-66'):
        capture = ROOT / (f'run-smoke/screenshots/v184-catalog-{username}-ferry_whiskey-' + phase + '.png')
        assert capture.stat().st_mtime > JAR.stat().st_mtime
    logs.append(str(logpath))
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256']
motion = {}
for username in ('SlimSteve29', 'CoconutLeft'):
    trace = ROOT / f'run-smoke/coconut-frames-{username}.csv'
    assert trace.stat().st_mtime > JAR.stat().st_mtime
    rows = list(csv.DictReader(trace.open()))
    assert len(rows) > 60
    ticks = [float(r['tick']) for r in rows]
    assert all(b >= a for a,b in zip(ticks, ticks[1:]))
    assert sum(0 < b-a < .9 for a,b in zip(ticks, ticks[1:])) > 50
    def extent(selected, name):
        values = [float(r[name]) for r in selected]
        return max(values)-min(values)
    for cue in (49,59,69,79):
        selected = [r for r in rows if cue-1 <= float(r['tick']) <= cue+9]
        assert len(selected) > 8
        assert extent(selected, 'handY') > .008
        assert extent(selected, 'cameraPitch') > .3
        assert extent(selected, 'strawX') > .018
    values = {key: extent(rows,key) for key in ('handX','handY','handZ','cameraPitch','cameraYaw','strawX','strawZ')}
    assert .45 < values['cameraPitch'] < 1.5
    files = sorted((ROOT/'run-smoke/screenshots').glob(f'coconut-v1873-{username}-*.png'))
    assert len(files) >= 25 and all(p.stat().st_mtime > JAR.stat().st_mtime for p in files)
    assert len({sha(p) for p in files}) == len(files), 'static rendered frames'
    motion[username] = dict(render_frames=len(rows), captures=len(files), ranges=values)
report = dict(version='1.8.73', sha256=sha(JAR), previous_sha256=sha(previous),
    configuration_sha256=sha(config), unit_tests=count, changed_jar_entries=sorted(changed),
    client_logs=logs, rendered_motion=motion,
    scope='Ferry whiskey: four sound-aligned suction/recovery gestures with wrist, coconut and subtle camera response. '
    'Restore straw spring during carry/inspect; during sipping rotate the exposed straw around its mouth opening with bounded spring and authored flex. '
    'Keep close mouth distance and shell/sleeve clearance; verify every sip in rendered frame traces for both hands. '
    'All production models/textures, flesh interior, sound assets, user metadata and configuration remain unchanged. '
    '200 tests and both independent Forge replays passed. Full modpack shader replay was not performed.')
(ROOT / 'verification-1.8.73.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.73: {count} tests; all four sips move wrist, straw and world camera in both client replays.')
print(json.dumps(motion,indent=2))
