"""Verify exact aged registrations, canonical animation reuse and vanilla item ownership."""
from pathlib import Path
import hashlib
import json
import re
import zipfile
import xml.etree.ElementTree as ET
from prepare_aged_aliases import aliases_from_registration, SOURCE

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT / 'backups/1.8.57-before-aged-animations'
JAR = ROOT / 'build/libs/farmingtales_drink_anim-1.8.58.jar'
CASES = ['farmingtales:' + name for name in (
    'orange_wine_i', 'tavern_carignan_g', 'tavern_brandy_d', 'world_liquor_dassai_d',
    'world_liquor_maotai_i', 'world_liquor_bamboo_leaf_green_liquor_g', 'world_liquor_strongbow_d',
    'vinery_chenet_wine_g', 'brewery_whiskey_highland_hearth_i', 'smc_ice_tea_d')]

def sha(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

previous = next(BACKUP.glob('*.jar'))
baseline = json.loads((ROOT / 'installation-1.8.57.json').read_text(encoding='utf-8-sig'))
assert sha(previous) == baseline['sha256']
aliases = aliases_from_registration()
assert len(aliases) == 303 and len(set(aliases.values())) == 101
assert sha(SOURCE) == sha(BACKUP / SOURCE.name), 'Aging registration must remain unchanged.'
with zipfile.ZipFile(previous) as old, zipfile.ZipFile(JAR) as new:
    definitions = 'assets/farmingtales_drink_anim/aged_aliases.json'
    added = set(new.namelist()) - set(old.namelist())
    assert added == {definitions}, added
    assert not set(old.namelist()) - set(new.namelist())
    changed = {name for name in old.namelist() if old.read(name) != new.read(name)}
    assert changed == {'META-INF/mods.toml', 'com/farmingtales/drinkanim/math/DrinkCatalog.class'}, changed
    assert json.loads(new.read(definitions)) == aliases
    assert 'version="1.8.58"' in new.read('META-INF/mods.toml').decode()
    for path in (ROOT / 'src/main/resources').rglob('*'):
        if path.is_file():assert new.read(path.relative_to(ROOT / 'src/main/resources').as_posix()) == path.read_bytes()

tests = [ET.parse(path).getroot() for path in (ROOT / 'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(test.get(k)) == 0 for test in tests for k in ('errors', 'failures', 'skipped'))
count = sum(int(test.get('tests')) for test in tests)
assert count == 173, count
names = {case.get('name') for test in tests for case in test.findall('testcase')}
assert 'allThreeAgingTiersUseTheirExactSourceNamespaceAndRejectPlacedStacks()' in names
logpath = ROOT / 'preview/verify-aged-animations-1.8.58.log'
log = logpath.read_text(encoding='utf-8-sig', errors='replace')
assert 'BUILD SUCCESSFUL' in log and 'AGED_SMOKE_REGISTRY_COMPLETE 303' in log and 'AGED_SMOKE_COMPLETE 10' in log
assert not re.search(r'(?:AGED|DRINK|CATALOG)_SMOKE FAIL', log)
for alias in aliases:
    for level in (0, 1, 6, 12):
        assert f'AGED_SMOKE PASS registry registered alias {alias} BrewLevel={level}' in log
captures = []
for item in CASES:
    for label in ('draw reaches canonical idle', 'draw preserves aged ID and NBT',
                  'canonical bottle cap and ornament resources loaded', 'inspection starts',
                  'inspection preserves aged item and full NBT', 'drinking starts with canonical animation',
                  'drink consumes one aged item and preserves remaining ID and full NBT',
                  'vanilla aged item consumption callback runs once', 'original container return runs once',
                  'original aged item effect is applied', 'stow finishes with empty hand'):
        assert f'AGED_SMOKE PASS {item} {label}' in log, (item, label)
    assert f'AGED_SMOKE_SEQUENCE_COMPLETE {item}' in log
    for phase in ('idle', 'inspect', 'drink', 'replacement'):
        path = ROOT / f'run-smoke/screenshots/v1858-aged-{item.split(":")[1]}-{phase}.png'
        assert path.stat().st_mtime > JAR.stat().st_mtime, path
        captures.append(str(path))
config = ROOT.parents[2] / 'config/farmingtales_drink_anim-client.toml'
assert sha(config) == sha(BACKUP / config.name)
report = dict(version='1.8.58', sha256=sha(JAR), previous_sha256=sha(previous),
              configuration_sha256=sha(config), aging_registration_sha256=sha(SOURCE), unit_tests=count,
              normal_drinks=101, aged_items=303, newly_supported_aged_items=231,
              changed_jar_entries=sorted(changed), added_jar_entries=sorted(added), captures=captures,
              client_logs=[dict(path=str(logpath), sha256=sha(logpath), registered_aliases=303, full_sequences=10)],
              scope='All 303 registered iron/gold/diamond single bottles reuse their exact normal profile; external namespaces remain distinct. All aliases checked in Forge with absent BrewLevel and values 1, 6, 12. Ten client sequences cover all six source namespaces, all tiers, cork/screw/XO and four rigid ornaments. Actual item identity and full NBT remain intact; vanilla fixture consumption, return container and effects execute once. Original models, textures, normal profiles, closures, renderer and 1.8.57 ornament clearance unchanged. Isolated Forge fixtures; complete modpack and shaders not tested.')
(ROOT / 'verification-1.8.58.json').write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(f'Verified 1.8.58: {count} tests, 303 registered aliases and 10 complete client sequences; SHA256 {sha(JAR)}')
