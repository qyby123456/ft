"""Verify the twelve-bottle build and completed BrewLevel:6 client sequences."""
import json,zipfile
import xml.etree.ElementTree as ET
from verify_twelve_tavern_assets import ROOT,ASSETS,BACKUP,SPECS,SCREW,sha,verify

surfaces=verify();baseline=json.loads((BACKUP/'baseline.json').read_text(encoding='utf-8'))
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.52.jar'
oldjar=next(BACKUP.glob('*drink_anim*.jar'));assert sha(oldjar)==baseline['previous_sha256']
prefix='assets/farmingtales_drink_anim/'
allowed={prefix+name for name in ('decorations.json','drink_profiles.json','hand_volumes.json','letsdo_closures.json')}
allowed|={'META-INF/mods.toml','assets/minecraft/atlases/blocks.json'}
with zipfile.ZipFile(oldjar) as previous,zipfile.ZipFile(jar) as built:
    assert set(previous.namelist())<=set(built.namelist())
    changed={p for p in previous.namelist() if previous.read(p)!=built.read(p)}
    assert changed==allowed,changed
    added=set(built.namelist())-set(previous.namelist())
    expected={prefix+f'models/animation/tavern_bottles/{item.split(":")[1]}_{part}.json' for item in SPECS for part in ('body','cap','neck')}
    expected|={prefix+f'textures/tavern_bottles/{item.split(":")[1]}_'+('2' if item.endswith('bamboo_leaf_green_liquor') else '3' if item.endswith('maotai') else '0')+'.png' for item in SPECS}
    expected.add(prefix+'models/animation/tavern_bottles/bamboo_leaf_green_liquor_leaf.json')
    assert added==expected,added^expected
    for path in (ROOT/'src/main/resources').rglob('*'):
        if path.is_file():assert built.read(path.relative_to(ROOT/'src/main/resources').as_posix())==path.read_bytes(),path
    assert 'version="1.8.52"' in built.read('META-INF/mods.toml').decode()
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ('failures','errors','skipped'))
count=sum(int(t.get('tests')) for t in tests);assert count==164
logpath=ROOT/'preview/smoke-twelve-tavern-SteveSmoke-1.8.52.log'
log=logpath.read_text(encoding='utf-8-sig',errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 12' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
firstjar=ROOT/'preview/twelve-tavern-first-build-1.8.52.jar'
with zipfile.ZipFile(firstjar) as first,zipfile.ZipFile(jar) as final:
    assert set(first.namelist())==set(final.namelist())
    assert {p for p in first.namelist() if first.read(p)!=final.read(p)}=={prefix+'models/animation/tavern_bottles/vodka_cap.json'}
recheck=ROOT/'preview/smoke-twelve-tavern-vodka-final-1.8.52.log'
vodka_log=recheck.read_text(encoding='utf-8-sig',errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 1' in vodka_log and 'BUILD SUCCESSFUL' in vodka_log
assert 'CATALOG_SMOKE FAIL' not in vodka_log and 'DRINK_SMOKE FAIL' not in vodka_log
captures=[]
for item in SPECS:
    labels=['body neck and decoration textures are present','original fixed contents have no dynamic liquid pass',
            'drink consumes once and returns to idle','bottle animation works without BrewLevel',
            'bottle animation ignores BrewLevel=6','drinking preserves remaining stack BrewLevel','stow finishes with empty hand']
    labels.append('specified screw cap twists twice and releases' if item in SCREW else 'baked cork is three times the original length')
    item_log=vodka_log if item=='kaleidoscope_tavern:vodka' else log
    for label in labels:assert f'CATALOG_SMOKE PASS {item} {label}' in item_log,(item,label)
    for stage in ('idle','inspect','opening','open-neck','drink','replacement','cavity-angle-197','cavity-angle-201','cavity-angle-205'):
        p=ROOT/f"run-smoke/screenshots/v184-catalog-SteveSmoke-{item.split(':')[1]}-{stage}.png"
        tested_jar=jar if item=='kaleidoscope_tavern:vodka' else firstjar
        assert p.stat().st_mtime>tested_jar.stat().st_mtime,p
        captures.append(str(p))
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.52',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],configuration_sha256=sha(config),
    unit_tests=count,items=list(SPECS),screw_caps=sorted(SCREW),surface_checks=surfaces,changed_jar_entries=sorted(changed),
    added_jar_entries=sorted(added),client_logs=[dict(path=str(logpath),sha256=sha(logpath),profiles=12),
        dict(path=str(recheck),sha256=sha(recheck),profiles=1)],first_tested_jar_sha256=sha(firstjar),captures=captures,
    scope='Twelve original opaque Tavern/World Liquor bottle profiles added; Carignan and Mother Snow use screw caps. BrewLevel=6 retained. All previous runtime classes and bottle assets are byte-identical to 1.8.51. Isolated Forge client; complete modpack and shaders not tested.')
(ROOT/'verification-1.8.52.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.52: {count} tests; 12 tagged client sequences; SHA256 {sha(jar)}')
