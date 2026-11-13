"""Verify close straw contact while preserving every resource from 1.8.71."""
from pathlib import Path
import hashlib, json, zipfile, tomllib
import xml.etree.ElementTree as ET

ROOT = Path('\\\\?\\' + str(Path(__file__).resolve()).removeprefix('\\\\?\\')).parent
BACKUP = ROOT / 'backups/1.8.71-before-straw-mouth-contact'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.72.jar'
baseline = json.loads((BACKUP / 'baseline.json').read_text())
previous = BACKUP / Path(baseline['installed_path']).name
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(previous) == baseline['previous_sha256']
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(JAR) as new:
    assert set(new.namelist()) == set(old.namelist())
    changed = {n for n in old.namelist() if old.read(n) != new.read(n)}
    expected = {'META-INF/mods.toml'} | {'com/farmingtales/drinkanim/math/' + n + '.class'
                                      for n in ('CoconutAnimations', 'BottleRig', 'BottleFraming')}
    assert changed == expected, changed
    old_meta = tomllib.loads(old.read('META-INF/mods.toml').decode())
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.72'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta
    for p in (ROOT / 'src/main/resources').rglob('*'):
        if p.is_file():
            assert new.read(p.relative_to(ROOT / 'src/main/resources').as_posix()) == p.read_bytes(), p

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 199, count
logs = []
for hand, username in [('slim', 'SlimSteve29'), ('left', 'CoconutLeft')]:
    logpath = ROOT / f'preview/verify-coconut-mouth-{hand}-1.8.72.log'
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
report = dict(version='1.8.72', sha256=sha(JAR), previous_sha256=sha(previous),
    configuration_sha256=sha(config), unit_tests=count, changed_jar_entries=sorted(changed),
    client_logs=logs, scope='Ferry whiskey straw now reaches the lower camera edge at food-eating distance. '
    'Fold the carrying elbow down to prevent sleeve clearance from pushing the cup away; present the leaning straw toward the lips in both hands. '
    'Test final contact after eating variations, breathing, camera and framing, plus shell/sleeve near-plane clearance and continuous approach/return. '
    'All resources, coconut flesh, inspection, audio, other food animations and user metadata preserved byte-for-byte. '
    'Right- and left-hand independent Forge client replays passed. Full modpack shader replay not performed.')
(ROOT / 'verification-1.8.72.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.72: {count} tests, both-hand client replays, unchanged models/textures/inspection/audio.')
