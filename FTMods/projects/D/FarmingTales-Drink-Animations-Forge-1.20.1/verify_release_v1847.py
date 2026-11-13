"""Verify requested bottles, original assets, build and isolated-client evidence."""
from pathlib import Path
import hashlib
import json
import zipfile
import xml.etree.ElementTree as ET
from prepare_world_bottles import SPECS

ROOT=Path(__file__).resolve().parent
RES=ROOT/'src/main/resources'
PREFIX='src/main/resources/assets/farmingtales_drink_anim/'
BACKUP=ROOT/'backups/1.8.46-before-world-bottles'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
baseline=read(BACKUP/'baseline.json')

with zipfile.ZipFile(BACKUP/'source.zip') as old:
    previous=json.loads(old.read(PREFIX+'drink_profiles.json'))
    assert all((p.get('cupShape') is not None)==(p['style'] in ['CUP','JAM']) for p in previous)
    current=read(ROOT/(PREFIX+'drink_profiles.json'))
    assert len(current)==278 and current[:len(previous)]==previous
    added={p['id']:p for p in current[len(previous):]}
    assert added.keys()==SPECS.keys()
    masks=read(ROOT/(PREFIX+'hand_volumes.json'))
    for key,value in json.loads(old.read(PREFIX+'hand_volumes.json')).items():assert masks[key]==value,key
    atlas='src/main/resources/assets/minecraft/atlases/blocks.json'
    assert all(s in read(ROOT/atlas)['sources'] for s in json.loads(old.read(atlas))['sources'])
    allowed={PREFIX+'drink_profiles.json',PREFIX+'hand_volumes.json',atlas,
             'src/main/java/com/farmingtales/drinkanim/client/BottleRenderer.java',
             'src/main/java/com/farmingtales/drinkanim/math/BottleClosures.java',
             'src/main/java/com/farmingtales/drinkanim/math/DrinkMotion.java',
             'src/main/resources/META-INF/mods.toml',
             'src/test/java/com/farmingtales/drinkanim/math/DrinkProfilesTest.java',
             'src/smoke/java/com/farmingtales/drinkanim/smoke/CatalogSmoke.java'}
    for entry in old.namelist():
        if entry.startswith('src/') and entry not in allowed:assert old.read(entry)==(ROOT/entry).read_bytes(),entry
    renderer='src/main/java/com/farmingtales/drinkanim/client/BottleRenderer.java'
    prior=old.read(renderer).decode().replace('\r\n','\n')
    expected=prior.replace('        if(profile.cup()||profile.jam()) {',
        "        // Optional liquid orientation also fits the Tavern bottles' rotated glass.\n        if(profile.cupShape()!=null) {")
    assert (ROOT/renderer).read_text()==expected

for item,p in added.items():
    assert p['style']=='WINE' and p['liquid'] and masks[item]
    for key in ['bodyModel','capModel','neckModel']:
        model=read(RES/'assets/farmingtales_drink_anim/models'/str(p[key]+'.json'))
        assert model['elements'],(item,key)
        if key=='neckModel':
            assert len(model['elements'])==8 and 'neck' in model['textures'],item
            assert sum('up' in e['faces'] and 'down' in e['faces'] for e in model['elements'])==4,item
        for element in model['elements']:
            assert element['faces'],(item,key,'zero-face element')
            for face in element['faces'].values():
                texture=model['textures'][face['texture'][1:]]
                ns,path=texture.split(':')
                assert (RES/f'assets/{ns}/textures/{path}.png').is_file(),texture
                assert 'cullface' not in face
    for r in p['liquid']:
        assert all(b>a for a,b in zip(r['min'],r['max']))
        assert (RES/'assets'/r['texture'].replace(':','/')).is_file()

for s in read(ROOT/'world-bottle-sources.json')['sources']:
    with zipfile.ZipFile(s['jar']) as source:assert hashlib.sha256(source.read(s['entry'])).hexdigest()==s['sha256']
for variant,reference in [('skyy_vodka','smirnoff_red_vodka'),('bacardi_carta_blanca','smirnoff_red_vodka'),
                          ('spiryt_vodka','smirnoff_red_vodka'),('glowflower_brew','luminous_bride')]:
    for part in ['body','cap','neck']:
        model_dir=RES/'assets/farmingtales_drink_anim/models/animation/world_bottles'
        assert read(model_dir/f'{variant}_{part}.json')['elements']==read(model_dir/f'{reference}_{part}.json')['elements']

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ['failures','errors','skipped'])
count=sum(int(t.get('tests')) for t in tests);assert count==161
tea=['cool_tea','sour_plum','ice_tea']
slim=['luminous_bride','smirnoff_red_vodka','johnnie_walker',*tea,'absolut_vodka','jack_daniel','kwas_chlebowy','bombay_sapphire_gin','strongbow']
logs=[]
baseline_log=ROOT/'preview/smoke-world-bottles-SteveSmoke-before-tea-1.8.47.log'
baseline_text=baseline_log.read_text(encoding='utf-8-sig',errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 15' in baseline_text and 'BUILD SUCCESSFUL' in baseline_text
assert 'CATALOG_SMOKE FAIL' not in baseline_text and 'DRINK_SMOKE FAIL' not in baseline_text
logs.append(dict(path=str(baseline_log),sha256=sha(baseline_log),arm_shape='default',profiles=15,
                 scope='All 15 functional sequences before tea-lid correction and thick-rim restoration'))
for username,shape,names in [('SteveSmoke','default',tea),('SlimSmoke','slim',slim)]:
    path=ROOT/f'preview/smoke-world-bottles-{username}-1.8.47.log';log=path.read_text(encoding='utf-8-sig',errors='replace')
    assert f'CATALOG_SMOKE_COMPLETE {len(names)}' in log and 'BUILD SUCCESSFUL' in log and 'SILENT_CLIENT:' in log
    assert f'DRINK_SMOKE PASS player arm renderer model={shape}' in log
    assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
    for name in names:
        item=next(i for i in SPECS if i.split(':')[1]==name)
        for check in ['bottle animation works without BrewLevel','bottle animation ignores BrewLevel=6',
                      'body neck and decoration textures are present','drink consumes once and returns to idle']:
            assert f'CATALOG_SMOKE PASS {item} {check}' in log,(item,check)
        assert f'CATALOG_SMOKE PASS {item} drinking preserves remaining stack BrewLevel' in log
        if name in tea:
            assert f'CATALOG_SMOKE PASS {item} specified screw cap twists twice and releases' in log
        for stage in ['idle','inspect','opening','open-neck','drink','replacement']:
            assert (ROOT/f'run-smoke/screenshots/v184-catalog-{username}-{name}-{stage}.png').is_file()
    logs.append(dict(path=str(path),sha256=sha(path),arm_shape=shape,profiles=len(names),geometry='final thick rim and tea screw caps'))

jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.47.jar'
with zipfile.ZipFile(jar) as built:
    for p in RES.rglob('*'):
        if p.is_file():assert built.read(p.relative_to(RES).as_posix())==p.read_bytes(),p
    assert 'version="1.8.47"' in built.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in name for name in built.namelist())
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.47',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],configuration_sha256=sha(config),
            unit_tests=count,client_profiles=15,slim_client_profiles=len(slim),client_logs=logs,items=list(SPECS),
            scope='15 requested drinks use original bottle textures and dynamic liquid with thick open rims. The three tea bottles use purified-water screw-cap drinking and retain Orange wine inspection; the other 12 use Orange wine gestures throughout. All 15 initial normal-arm sequences passed; final tea corrections pass both arm shapes and 11 final slim-arm sequences cover every bottle shape. ID matching ignores BrewLevel. All 263 previous profiles and prior Jo left-hand/beer/juice fixes preserved.')
(ROOT/'verification-1.8.47.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(f'Verified 1.8.47: {count} tests, 15 baseline / 3 final normal / {len(slim)} final slim client bottles; SHA256 {sha(jar)}')
