"""Verify Long Island's isolated performance change and the user-provided reference archive."""
from pathlib import Path
import hashlib
import json
import re
import tomllib
import zipfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
PACK = ROOT.parents[2]
BACKUP = ROOT / 'backups/1.8.60-before-popsicle-motion'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.61.jar'
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
    assert added == {MATH + 'PopsicleMotion.class', MATH + 'PopsicleMotion$Track.class'}, added
    assert not set(old.namelist()) - set(new.namelist())
    changed = {name for name in old.namelist() if old.read(name) != new.read(name)}
    assert changed == {'META-INF/mods.toml', MATH + 'DrinkMotion.class', MATH + 'ServingAnimations.class'}, changed
    old_meta = tomllib.loads(baseline['user_metadata'])
    new_meta = tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert new_meta['mods'][0]['version'] == '1.8.61'
    new_meta['mods'][0]['version'] = old_meta['mods'][0]['version']
    assert new_meta == old_meta
    for name, digest in baseline['preserve_hashes'].items():
        assert hashlib.sha256(new.read(name)).hexdigest() == digest, name
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():
            assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes(), path

references = ROOT / 'blockbench/03-参考素材/Minecraft-Swords-Pack'
manifest = read(references / '素材清单.json')
assert len(manifest) == 2
for pack in manifest:
    assert sha(Path(pack['source'])) == pack['sha256'] == sha(references / pack['archive'])
    for animation in pack['animations']:
        assert sha(references / animation['path']) == animation['sha256']
        assert len(animation['clips']) == 22
assert manifest[0]['animations'][0]['sha256'] == manifest[1]['animations'][0]['sha256']

tests = [ET.parse(path).getroot() for path in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(k)) == 0 for test in tests for k in ('errors','failures','skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 176, count
scope = read(ROOT / 'preview/popsicle-scope-1.8.61.json')
assert scope == dict(unchanged_profiles=304,compared_rig_samples=124032,event_clocks_unchanged=True,mismatches=0)
client_logs = []
for variant,username,names in (
    ('slim','SlimSteve29',('long_island_popsicle_tea','diplomatico_coffee')),
    ('wide','WideSteve10',('long_island_popsicle_tea',))):
    logpath = ROOT / f'preview/verify-popsicle-{variant}-1.8.61.log'
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
                  'garnish-meal-72','garnish-meal-84','garnish-meal-96','motion-090','motion-110','motion-140'):
        path = ROOT / f'run-smoke/screenshots/v184-catalog-{username}-long_island_popsicle_tea-{phase}.png'
        assert path.stat().st_mtime > JAR.stat().st_mtime, path
        captures.append(str(path))
    audio = read(ROOT / f'run-smoke/audio-balance-{username}.json')
    for event,expected in (('popsicle_bite',3),('snack_swallow',3),('stick_spit',1)):
        cues = [a for a in audio if a['item']=='papercraft_magic_decoration:long_island_popsicle_tea' and a['event']==event]
        assert len(cues) == expected and all(a['volume'] > 0 for a in cues), event
    client_logs.append(dict(path=str(logpath),sha256=sha(logpath),profiles=len(names),captures=captures))
config = PACK / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == baseline['configuration_sha256'] == sha(BACKUP / config.name)
report = dict(version='1.8.61',sha256=sha(JAR),previous_sha256=sha(previous),
              configuration_sha256=sha(config),unit_tests=count,changed_jar_entries=sorted(changed),
              added_jar_entries=sorted(added),reference_manifest=str(references / '素材清单.json'),
              motion_comparison=scope,client_logs=client_logs,
              scope='Only Long Island popsicle tea receives new carrying-hand weight transfers, asymmetric bite recoveries, wrist follow-through and continuous inspection. The three-bite, stick transfer/spit and drinking clocks and sounds are preserved. All 304 other profile motions match the backed-up user JAR in 124032 rig samples. All production resource assets are byte-identical except the version in mods.toml. Reference archives remain outside the runtime JAR. 176 tests and isolated slim/wide Forge client sequences pass; full modpack and shaders not tested.')
(ROOT / 'verification-1.8.61.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.61: {count} tests, {scope["unchanged_profiles"]} unaffected profiles, slim/wide client passes; SHA256 {sha(JAR)}')
