from pathlib import Path
import copy, hashlib, json, zipfile, xml.etree.ElementTree as ET
from PIL import Image
from prepare_cocktail_profiles import fix_handheld_surfaces

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.27-before-cup-surfaces'
JAR=ROOT/'build/libs/farmingtales_drink_anim-1.8.28.jar'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.27.jar'
assert sha(previous)==baseline['sha256']
prefix='assets/farmingtales_drink_anim/'
models={item:prefix+'models/animation/cocktails/'+item.replace(':','/')+'.json'
    for item in ['kaleidoscope_world_liquor:jerk','beachparty:coconut_cocktail']}
with zipfile.ZipFile(JAR) as new,zipfile.ZipFile(previous) as old:
    names={n for n in new.namelist() if not n.endswith('/')}
    assert names=={n for n in old.namelist() if not n.endswith('/')}
    changed={n for n in names if new.read(n)!=old.read(n)}
    assert changed==set(models.values())|{'META-INF/mods.toml','com/farmingtales/drinkanim/math/CupAnimations.class'},changed
    profiles=json.loads(new.read(prefix+'drink_profiles.json'))
    assert profiles==baseline['profiles'] and len(profiles)==209
    cups={p['id'] for p in profiles if p['style']=='CUP'};assert len(cups)==55
    for item,path in models.items():
        current=json.loads(new.read(path))
        assert current==fix_handheld_surfaces(item,json.loads(old.read(path)))
        assert current==fix_handheld_surfaces(item,current),item+' repeated generation must be stable'
    jerk={e['name']:e for e in json.loads(new.read(models['kaleidoscope_world_liquor:jerk']))['elements']}
    assert 'original_5' not in jerk
    assert set(jerk['original_1']['faces'])=={'up','down'}
    assert not {'up','down'}.intersection(jerk['original_0']['faces'])
    coconut=json.loads(new.read(models['beachparty:coconut_cocktail']))
    bottom=next(e for e in coconut['elements'] if e['name']=='original_4')
    assert set(bottom['faces'])=={'up','down'} and bottom['to'][1]==.95
    prior=next(e for e in json.loads(old.read(models['beachparty:coconut_cocktail']))['elements'] if e['name']=='original_4')
    assert bottom['faces']['down']==prior['faces']['down']
    profile=next(p for p in profiles if p['id']=='beachparty:coconut_cocktail')
    assert bottom['to'][1]<min(r['min'][1] for r in profile['liquid'])*16
    location=coconut['textures'][bottom['faces']['up']['texture'][1:]]
    texture=ROOT/'src/main/resources/assets'/location.replace(':','/textures/')
    im=Image.open(Path(str(texture)+'.png')).convert('RGBA')
    uv=bottom['faces']['up']['uv'];region=im.crop(tuple(round(v*(im.width if i%2==0 else im.height)/16) for i,v in enumerate(uv)))
    assert region.getchannel('A').getextrema()==(255,255)
    metadata=new.read('META-INF/mods.toml').decode()
    assert all(s in metadata for s in ['version="1.8.28"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert new.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()
for source in read(ROOT/'cocktail-sources.json')['jars']:
    assert sha(Path(source['path']))==source['sha256'],'original mod changed'

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==127,count
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.28.log').read_text(encoding='utf-8-sig')
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(BACKUP/config.name)==baseline['configuration_sha256']
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
logpath=ROOT/'preview/smoke-cup-surfaces-1.8.28.log';log=logpath.read_text(encoding='utf-8-sig')
assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 3' in log
assert 'CATALOG_SMOKE FAIL' not in log
assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
checks=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in log.splitlines() if 'CATALOG_SMOKE PASS ' in s]
for item in list(models)+['farmingtales:orange_wine_cup_1']:
    selected=[s for s in checks if s.startswith(item+' ')]
    for expected in ['drink consumes once','inspect preserves all three items','ordinary right click keeps vanilla animation',
        'stow finishes with empty hand']:
        assert any(expected in s for s in selected),(item,expected)
    if item in models:
        for expected in ['cocktail liquid atlas loaded','cocktail mesh has painted faces']:
            assert any(expected in s for s in selected),(item,expected)

directory=ROOT/'blockbench/杯装摇晃与杯底修复-v58'
entries=read(directory/'导出检查.json');assert len(entries)==110 and {e['item'] for e in entries}==cups
fit=read(ROOT/'preview/bb-fit-v58.json');assert len(fit)==110 and all(e['max_position_error_pixels']<=.01501 for e in fit)
for entry in entries:
    model=read(Path(entry['path']));assert len(model['animations'])==13
    assert 'ARR' in model['credit'] and '1.8.28' in model['credit']
    clips={a['name'].split('.')[-1]:a for a in model['animations']}
    assert clips['draw']['length']==.5 and clips['put_away']['length']==.45
    assert clips['idle']['length']==4
    assert all(any(t.get('type')=='camera' for t in a['animators'].values()) for a in model['animations'])
    if entry['item']=='beachparty:coconut_cocktail':
        el=next(e for e in model['elements'] if e['name']=='bottle_4')
        assert el['faces']['up']['texture'] is not None and el['faces']['down']['texture'] is not None
        assert abs(el['to'][1]-(16+(.95-4.8)*entry['geometry_scale']))<1e-7
    if entry['item']=='kaleidoscope_world_liquor:jerk':
        sheets=[e for e in model['elements'] if e['name'].startswith('bottle_') and e['from'][1]==e['to'][1]]
        assert len(sheets)==1
        assert {k for k,v in sheets[0]['faces'].items() if v.get('texture') is not None}=={'up','down'}
archive=directory.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    assert sum(n.endswith('.bbmodel') for n in z.namelist())==110
    for path in directory.iterdir():
        if path.is_file():assert z.read(path.name)==path.read_bytes()
for a in baseline['blockbench_archives']:assert sha(Path(a['path']))==a['sha256']
report=copy.deepcopy(baseline)
report.update(version='1.8.28',sha256=sha(JAR),unit_tests=count,
    blockbench=[e for e in baseline['blockbench'] if e['item'] not in cups]+entries,
    blockbench_fit=[e for e in baseline['blockbench_fit'] if e['item'] not in cups]+fit,
    blockbench_archives=baseline['blockbench_archives']+[dict(path=str(archive),sha256=sha(archive))],
    client_runs={'cups':dict(log=str(logpath),sha256=sha(logpath),assertions=checks)},client_retested=True,
    baseline_verification=dict(path=str(BACKUP/'verification.json'),sha256=sha(BACKUP/'verification.json')),
    changed_entries=sorted(changed),
    scope='jerk coplanar cup-bottom removal; coconut cocktail opaque inner floor using original coconut flesh texture, exterior floor retained. '
    'All 55 cups inspect upright with a stronger bidirectional orbit, unchanged contained-liquid limits and low camera gain. '
    '127 tests; jerk, coconut cocktail and orange wine cup tested in hidden Forge fixture client, shaders off. '
    '110 updated Blockbench projects with both arm widths, camera, liquid and decorations, validated against runtime samples. '
    'All other runtime classes, resources, profiles, sound and source mods unchanged. Draw 0.5s and stow 0.45s. Full pack not launched.')
assert len(report['blockbench'])==418
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.28:',count,'tests; three hidden-client cases; 110 updated Blockbench projects; four JAR entries changed.')
