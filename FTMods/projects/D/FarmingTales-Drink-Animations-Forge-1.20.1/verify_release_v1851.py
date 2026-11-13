"""Verify the targeted bottle release, built artifact, and isolated client run."""
import hashlib,json,zipfile
import xml.etree.ElementTree as ET
from verify_bottle_surfaces_v1851 import ROOT,BACKUP,CHANGED,verify

sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
surfaces=verify()
baseline=json.loads((BACKUP/'baseline.json').read_text(encoding='utf-8'))
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.51.jar'
oldjar=next(BACKUP.glob('*.jar'))
assert sha(oldjar)==baseline['previous_sha256']
allowed={p.removeprefix('src/main/resources/') for p in CHANGED}|{'META-INF/mods.toml'}
with zipfile.ZipFile(oldjar) as before,zipfile.ZipFile(jar) as built:
    assert set(before.namelist())==set(built.namelist())
    changed={p for p in before.namelist() if before.read(p)!=built.read(p)}
    assert changed==allowed,changed
    assert 'version="1.8.51"' in built.read('META-INF/mods.toml').decode()
    for p in (ROOT/'src/main/resources').rglob('*'):
        if p.is_file():assert built.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes(),p
    assert not any('/smoke/' in p for p in built.namelist())
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ('failures','errors','skipped'))
count=sum(int(t.get('tests')) for t in tests)
items=['kaleidoscope_tavern:wine','kaleidoscope_world_liquor:lafite_1982',
       'kaleidoscope_tavern:ice_wine','kaleidoscope_tavern:champagne']
logpath=ROOT/'preview/smoke-bottle-surfaces-SteveSmoke-1.8.51.log'
log=logpath.read_text(encoding='utf-8-sig',errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 4' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
captures=[]
for item in items:
    for label in ['body neck and decoration textures are present','original fixed contents have no dynamic liquid pass',
                  'baked cork is three times the original length','drink consumes once and returns to idle',
                  'bottle animation works without BrewLevel','bottle animation ignores BrewLevel=6',
                  'drinking preserves remaining stack BrewLevel','stow finishes with empty hand']:
        assert f'CATALOG_SMOKE PASS {item} {label}' in log,(item,label)
    for stage in ['idle','inspect','opening','open-neck','drink','replacement',
                  'cavity-angle-197','cavity-angle-201','cavity-angle-205']:
        p=ROOT/f"run-smoke/screenshots/v184-catalog-SteveSmoke-{item.split(':')[1]}-{stage}.png"
        assert p.stat().st_mtime>jar.stat().st_mtime,p
        captures.append(str(p))
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.51',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],
    configuration_sha256=sha(config),unit_tests=count,items=items,surface_checks=surfaces,
    changed_jar_entries=sorted(changed),captures=captures,
    client_logs=[dict(path=str(logpath),sha256=sha(logpath),profiles=4)],
    scope='Only wine/lafite_1982 exposed-neck palettes and ice_wine/champagne cavity geometry change. Other resources and runtime classes are byte-identical to 1.8.50. Isolated Forge client tests use BrewLevel=6, with three steep-angle opening captures each. Full modpack and shader rendering not tested.')
(ROOT/'verification-1.8.51.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.51: {count} unit tests; four tagged bottle sequences; SHA256 {sha(jar)}')
