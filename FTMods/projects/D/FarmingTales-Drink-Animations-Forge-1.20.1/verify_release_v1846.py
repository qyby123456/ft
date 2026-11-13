"""Verify Jo's opening-hand correction and preservation of the other drink fixes."""
from pathlib import Path
import hashlib, json, zipfile, re, xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parent
BACKUP = ROOT/'backups/1.8.45-before-jo-left-opening'
sha = lambda p: hashlib.sha256(p.read_bytes()).hexdigest()
read = lambda p: json.loads(p.read_text(encoding='utf-8-sig'))
baseline = read(BACKUP/'baseline.json')
rig = 'src/main/java/com/farmingtales/drinkanim/math/BottleRig.java'
current = (ROOT/rig).read_text()
with zipfile.ZipFile(ROOT/'backups/1.8.44-before-grip-fruit-liquid/source.zip') as original:
    restored = re.sub(r'        if\(profile.id\(\).equals\("vinery:jo_special_mixture"\).*?\n        }\n', '', current, flags=re.S)
    assert restored == original.read(rig).decode().replace('\r\n','\n'), 'Original carry hand and cap must be fully restored'
with zipfile.ZipFile(BACKUP/'source.zip') as old:
    allowed = {rig,'src/test/java/com/farmingtales/drinkanim/math/RequestedDrinkFixesTest.java','src/main/resources/META-INF/mods.toml'}
    for entry in old.namelist():
        if entry.startswith('src/') and entry not in allowed: assert old.read(entry)==(ROOT/entry).read_bytes(),entry
tests = [ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ('failures','errors','skipped'))
count = sum(int(t.get('tests')) for t in tests)
assert count == 158
logs=[]
for user, shape in [('SteveSmoke','default'),('SlimSmoke','slim')]:
    p=ROOT/f'preview/smoke-jo-left-{user}-1.8.46.log'
    log=p.read_text(encoding='utf-8-sig',errors='replace')
    assert 'CATALOG_SMOKE_COMPLETE 1' in log and 'BUILD SUCCESSFUL' in log and 'SILENT_CLIENT:' in log
    assert f'DRINK_SMOKE PASS player arm renderer model={shape}' in log
    assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
    logs.append(dict(path=str(p),sha256=sha(p),arm_shape=shape))
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.46.jar'
with zipfile.ZipFile(jar) as built:
    for p in (ROOT/'src/main/resources').rglob('*'):
        if p.is_file():assert built.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes(),p
    assert 'version="1.8.46"' in built.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in name for name in built.namelist())
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.46',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],
            configuration_sha256=sha(config),unit_tests=count,client_profiles=1,client_logs=logs,
            scope='Restore Jo special mixture carry/right hand to 1.8.44. Adjust only the opening/left hand to clear the bottle shoulder during approach, twisting and withdrawal. Preserve cap motion, whisky liquid, juice garnish motion and beer clipping fixes.')
(ROOT/'verification-1.8.46.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'Verified 1.8.46: {count} tests, both arm shapes, original carry-hand/cap solve restored; SHA256 {sha(jar)}')
