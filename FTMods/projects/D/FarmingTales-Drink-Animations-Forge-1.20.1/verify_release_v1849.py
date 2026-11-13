"""Verify all requested fixed bottles, source artwork, and client/build evidence."""
from pathlib import Path
import hashlib
import json
import zipfile
import xml.etree.ElementTree as ET
from container_assets import bounds
from prepare_fixed_tavern_bottles import SPECS, ASSETS, ROOT, PACK, READ

BACKUP=ROOT/'backups/1.8.48-before-fixed-tavern-bottles'
PREFIX='src/main/resources/assets/farmingtales_drink_anim/'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
profiles=READ(ASSETS/'drink_profiles.json')
catalog={p['id']:p for p in profiles}
closures=READ(ASSETS/'letsdo_closures.json')
masks=READ(ASSETS/'hand_volumes.json')
baseline=READ(BACKUP/'baseline.json')
assert len(profiles)==len(catalog)==293
allowed={PREFIX+'drink_profiles.json',PREFIX+'hand_volumes.json',PREFIX+'letsdo_closures.json',
         'src/main/resources/assets/minecraft/atlases/blocks.json','src/main/resources/META-INF/mods.toml',
         'src/test/java/com/farmingtales/drinkanim/math/DrinkProfilesTest.java',
         'src/smoke/java/com/farmingtales/drinkanim/smoke/CatalogSmoke.java'}
with zipfile.ZipFile(BACKUP/'source.zip') as previous:
    old=json.loads(previous.read(PREFIX+'drink_profiles.json'))
    assert profiles[:len(old)]==old and {p['id'] for p in profiles[len(old):]}==SPECS.keys()
    for file,now in [('hand_volumes.json',masks),('letsdo_closures.json',closures)]:
        before=json.loads(previous.read(PREFIX+file))
        assert now.keys()==before.keys()|SPECS.keys()
        for key,value in before.items():assert now[key]==value,(file,key)
    atlas='src/main/resources/assets/minecraft/atlases/blocks.json'
    assert all(s in READ(ROOT/atlas)['sources'] for s in json.loads(previous.read(atlas))['sources'])
    for path in previous.namelist():
        if path.startswith('src/') and path not in allowed:
            assert previous.read(path)==(ROOT/path).read_bytes(),path

source_report=READ(ROOT/'fixed-tavern-bottle-sources.json')
sources={s['entry']:s for s in source_report['sources']}
for item,(cap_index,cut) in SPECS.items():
    p=catalog[item]
    assert p['style']=='WINE' and p['liquid']==[] and p['canLid'] is None and p['cupShape'] is None,item
    assert closures[item]=='wine' and masks[item],item
    body=READ(ASSETS/f"models/{p['bodyModel']}.json")
    cap=READ(ASSETS/f"models/{p['capModel']}.json")
    assert not READ(ASSETS/f"models/{p['neckModel']}.json")['elements'],item
    entry=next(s['source'] for s in source_report['items'] if s['id']==item)
    with zipfile.ZipFile(sources[entry]['jar']) as source:original=json.loads(source.read(entry))
    neck=original['elements'][cap_index];a,b=bounds(neck)
    assert p['mouthY']==cut and p['capPivot'][1]==b[1]
    assert len(cap['elements'])==3 and min(bounds(e)[0][1] for e in cap['elements'])==cut-4
    assert max(bounds(e)[1][1] for e in body['elements'])==cut
    lower=next(e for e in body['elements'] if e['name']==f'source_{cap_index}')
    assert bounds(lower)==(a,[b[0],cut,b[2]])
    head=cap['elements'][0]
    assert bounds(head)==([a[0],cut,a[2]],b)
    # The closed neck reconstructs every original side UV without stretching or repainting.
    for direction in ['north','east','south','west']:
        u,v,s,t=neck['faces'][direction]['uv']
        seam=v+(t-v)*(b[1]-cut)/(b[1]-a[1])
        assert head['faces'][direction]['uv']==[u,v,s,seam],(item,direction)
        assert lower['faces'][direction]['uv']==[u,seam,s,t],(item,direction)
    # Its original broad shoulder and label faces retain their exact artwork.
    other=1-cap_index
    shell=next(e for e in body['elements'] if e['name']==f'source_{other}')
    for direction in ['north','east','south','west','down']:
        assert shell['faces'].get(direction)==original['elements'][other]['faces'].get(direction),(item,direction)
    # No solid lid across the centre of the opened mouth.
    cx,cz=(a[0]+b[0])/2,(a[2]+b[2])/2
    for e in body['elements']:
        lo,hi=bounds(e)
        if abs(hi[1]-cut)<1e-7 and 'up' in e['faces']:
            assert not(lo[0]<cx<hi[0] and lo[2]<cz<hi[2]),item
    for model in [body,cap]:
        for e in model['elements']:
            assert e['faces']
            for f in e['faces'].values():
                assert 'cullface' not in f
                ns,path=model['textures'][f['texture'][1:]].split(':')
                assert (ROOT/f'src/main/resources/assets/{ns}/textures/{path}.png').is_file()
    for key,loc in original['textures'].items():
        if key=='particle':continue
        ns,path=loc.split(':');entry=f'assets/{ns}/textures/{path}.png'
        ns,path=body['textures'][key].split(':')
        assert sha(ROOT/f'src/main/resources/assets/{ns}/textures/{path}.png')==sources[entry]['sha256']
for s in sources.values():
    with zipfile.ZipFile(s['jar']) as archive:assert hashlib.sha256(archive.read(s['entry'])).hexdigest()==s['sha256']

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ['failures','errors','skipped'])
count=sum(int(t.get('tests')) for t in tests)
client_logs=[]
for user,items in [('SteveSmoke',list(SPECS)),('SlimSmoke',[
        'kaleidoscope_tavern:champagne','kaleidoscope_tavern:plum_wine',
        'kaleidoscope_world_liquor:pina_colada','kaleidoscope_tavern:wine'])]:
    path=ROOT/f'preview/smoke-fixed-tavern-{user}-1.8.49.log'
    log=path.read_text(encoding='utf-8-sig',errors='replace')
    assert f'CATALOG_SMOKE_COMPLETE {len(items)}' in log and 'BUILD SUCCESSFUL' in log,path
    assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log,path
    for item in items:
        for label in ['body neck and decoration textures are present','original fixed contents have no dynamic liquid pass',
                      'baked cork is three times the original length','drink consumes once and returns to idle',
                      'bottle animation works without BrewLevel','bottle animation ignores BrewLevel=6',
                      'drinking preserves remaining stack BrewLevel','stow finishes with empty hand']:
            assert f'CATALOG_SMOKE PASS {item} {label}' in log,(user,item,label)
        for stage in ['idle','inspect','opening','open-neck','drink','replacement']:
            name=item.split(':')[1]
            assert (ROOT/f'run-smoke/screenshots/v184-catalog-{user}-{name}-{stage}.png').is_file()
    client_logs.append(dict(path=str(path),sha256=sha(path),profiles=len(items)))
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.49.jar'
resources=ROOT/'src/main/resources'
with zipfile.ZipFile(jar) as built:
    for path in resources.rglob('*'):
        if path.is_file():assert built.read(path.relative_to(resources).as_posix())==path.read_bytes(),path
    assert 'version="1.8.49"' in built.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in path for path in built.namelist())
config=PACK/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.49',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],
    configuration_sha256=sha(config),unit_tests=count,client_logs=client_logs,items=list(SPECS),
    scope='Fourteen original opaque bottles use Vinery-style fixed contents and extracted corks, with no dynamic liquid rendering. BrewLevel is preserved. All 279 previous profiles and runtime Java code remain unchanged. Isolated Forge client; full modpack and shaders not tested.')
(ROOT/'verification-1.8.49.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.49: {count} tests; 14 normal-arm and 4 slim-arm bottle sequences; SHA256 {sha(jar)}')
