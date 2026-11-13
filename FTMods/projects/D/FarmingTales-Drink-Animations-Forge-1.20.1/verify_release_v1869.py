"""Verify the isolated chocolate palm inset against the installed 1.8.68 baseline."""
from pathlib import Path
import hashlib
import io
from PIL import Image
import json
import re
import tomllib
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
BACKUP = ROOT / 'backups/1.8.68-before-chocolate-palm-inset'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.69.jar'
MATH = 'com/farmingtales/drinkanim/math/'


def native(path):
    return Path('\\\\?\\' + str(path.resolve()))


def sha(path):
    return hashlib.sha256(native(path).read_bytes()).hexdigest()


def read(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


baseline = read(BACKUP / 'baseline.json')
previous = BACKUP / Path(baseline['installed_path']).name
assert sha(previous) == baseline['previous_sha256']
with zipfile.ZipFile(native(previous)) as old, zipfile.ZipFile(JAR) as new:
    added = set(new.namelist()) - set(old.namelist())
    assert not added, added
    assert not set(old.namelist()) - set(new.namelist())
    changed = {name for name in old.namelist() if old.read(name) != new.read(name)}
    assert changed == {'META-INF/mods.toml',MATH+'ChocolateInspection.class'}, changed
    old_meta = tomllib.loads(baseline['user_metadata'])
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.69'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta
    for name, digest in baseline['preserve_hashes'].items():
        assert hashlib.sha256(new.read(name)).hexdigest() == digest, name
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == native(path).read_bytes(), path

# All production resources, including the accepted blue popsicle texture, are preserved.
tests = [ET.parse(path).getroot() for path in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(k)) == 0 for test in tests for k in ('errors','failures','skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 194, count
scope = read(ROOT / 'preview/chocolate-scope-1.8.69.json')
assert scope == dict(unchanged_profiles=304,compared_rig_samples=124372,other_clocks_and_target_drink_unchanged=True,sound_cues_unchanged=True,target_inspection_prop_and_holding_hand_unchanged=True,mismatches=0)
client_logs = []
for variant,username,names in (
    ('slim','SlimSteve29',('diplomatico_coffee',)),
    ('wide','WideSteve10',('diplomatico_coffee',))):
    logpath = ROOT / f'preview/verify-chocolate-{variant}-1.8.69.log'
    raw = logpath.read_bytes()
    log = raw.decode('utf-16' if raw.startswith((b'\xff\xfe',b'\xfe\xff')) else 'utf-8-sig',errors='replace')
    assert 'BUILD SUCCESSFUL' in log and f'CATALOG_SMOKE_COMPLETE {len(names)}' in log
    assert not re.search(r'(?:AGED|DRINK|CATALOG)_SMOKE FAIL',log)
    for name in names:
        for label in ('body neck and decoration textures are present','inspect preserves all three items',
                      'drink consumes once and returns to idle','stow finishes with empty hand'):
            assert 'CATALOG_SMOKE PASS papercraft_magic_decoration:' + name + ' ' + label in log, (name,label)
    captures = []
    for phase in ('idle','inspect','drink','replacement','garnish-meal-8','garnish-meal-16',
                  'garnish-meal-26','garnish-meal-42','garnish-meal-58','garnish-meal-64',
                  'garnish-meal-72','garnish-meal-84','garnish-meal-96',
                  'chocolate-inspect-46','chocolate-inspect-58','chocolate-inspect-86','chocolate-inspect-108','chocolate-inspect-156'):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-{username}-diplomatico_coffee-{phase}.png'
        assert path.stat().st_mtime > JAR.stat().st_mtime, path
        captures.append(str(path))
    audio = read(ROOT / f'run-smoke/audio-balance-{username}.json')
    for event,expected in (('chocolate_bite',2),('snack_swallow',2)):
        cues = [a for a in audio if a['item']=='papercraft_magic_decoration:diplomatico_coffee' and a['event']==event]
        assert len(cues) == expected and all(a['volume'] > 0 for a in cues), event
    frame_report = read(ROOT / f'preview/chocolate-frame-report-{variant}-1.8.69.json')
    assert all(v['clock_monotonic'] and v['rendered_frames'] > 100 and v['subtick_advances'] > 50 for v in frame_report.values())
    assert frame_report['INSPECT']['bite_bursts']==0 and frame_report['DRINK']['bite_bursts']==2
    assert frame_report['DRINK']['max_crumbs']==8 and frame_report['DRINK']['crumb_frames']>20
    client_logs.append(dict(path=str(logpath),sha256=sha(logpath),profiles=len(names),captures=captures,frame_report=frame_report))
config = PACK / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256'] == sha(BACKUP / config.name)
report = dict(version='1.8.69',sha256=sha(JAR),previous_sha256=sha(previous),
              configuration_sha256=sha(config),unit_tests=count,changed_jar_entries=sorted(changed),
              added_jar_entries=sorted(added),
              motion_comparison=scope,client_logs=client_logs,
              scope='Only the chocolate inspection helper grip changes: the actual retained corner is embedded 0.65 skin pixels into the palm. A small wrist-angle adjustment and lateral palm offset keep the sleeve clear of the cup during pickup and return. Tests use the exported chocolate mesh vertex, both arm widths and hand sides, real solid/sleeve collision geometry, screen visibility and continuous motion. All 194 tests and slim/wide isolated Forge replay pass. Other 304 profiles and all coffee non-inspection rig/framing samples match 1.8.68 across 124372 samples; the coffee inspection prop/cup/holding hand trajectory and clock are also preserved. Eating motion, bite particles, production assets, metadata, tooltip, logo and configuration remain unchanged. Full modpack shader replay was not performed.')
(ROOT / 'verification-1.8.69.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.69: {count} tests, {scope["unchanged_profiles"]} unaffected profiles, client replay passes; SHA256 {sha(JAR)}')
