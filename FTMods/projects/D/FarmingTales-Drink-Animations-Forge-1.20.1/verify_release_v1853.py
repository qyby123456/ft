"""Audit the XO motion update, its regression suite and tagged client sequences."""
from pathlib import Path
import hashlib
import json
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.52-before-xo-motion'
ITEMS = ['kaleidoscope_tavern:brandy', 'kaleidoscope_tavern:carignan']

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

jar = ROOT / 'build/libs/farmingtales_drink_anim-1.8.53.jar'
previous = next(BACKUP.glob('*.jar'))
baseline = json.loads((ROOT / 'installation-1.8.52.json').read_text(encoding='utf-8-sig'))
assert sha(previous) == baseline['sha256']
allowed = {'META-INF/mods.toml'} | {
    f'com/farmingtales/drinkanim/math/{name}.class'
    for name in ('BottleRig', 'ContainerAnimations', 'DrinkMotion', 'EatingMotion')
}
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(jar) as new:
    assert set(old.namelist()) == set(new.namelist())
    changed = {p for p in old.namelist() if old.read(p) != new.read(p)}
    assert changed == allowed, changed
    assert 'version="1.8.53"' in new.read('META-INF/mods.toml').decode()
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes()

tests = [ET.parse(p).getroot() for p in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k)) == 0 for t in tests for k in ('failures', 'errors', 'skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 166, count
logpath = ROOT / 'preview/verify-xo-motion-1.8.53.log'
log = logpath.read_text(encoding='utf-8-sig', errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 2' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
captures = []
for item in ITEMS:
    for label in ('inspect preserves all three items', 'drink consumes once and returns to idle',
                  'bottle animation ignores BrewLevel=6', 'drinking preserves remaining stack BrewLevel',
                  'stow finishes with empty hand'):
        assert f'CATALOG_SMOKE PASS {item} {label}' in log, (item, label)
    for stage in ('inspect', 'inspect-43', 'opening', 'drink', 'replacement'):
        path = ROOT / f"run-smoke/screenshots/v184-catalog-SteveSmoke-{item.split(':')[1]}-{stage}.png"
        assert path.stat().st_mtime > jar.stat().st_mtime, path
        captures.append(str(path))
assert 'CATALOG_SMOKE PASS kaleidoscope_tavern:carignan specified screw cap twists twice and releases' in log
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
report = dict(version='1.8.53', sha256=sha(jar), previous_sha256=sha(previous),
              configuration_sha256=sha(config), unit_tests=count, items=ITEMS,
              changed_jar_entries=sorted(changed), captures=captures,
              client_logs=[dict(path=str(logpath), sha256=sha(logpath), profiles=2)],
              scope='Brandy and Carignan reuse XO motion, camera and two-hand grip. Original resources and closures preserved; Carignan still twists open. BrewLevel=6 client sequences passed. Isolated Forge client; complete modpack and shaders not tested.')
(ROOT / 'verification-1.8.53.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.53: {count} tests, 2 tagged client sequences, unchanged bottle assets; SHA256 {sha(jar)}')
