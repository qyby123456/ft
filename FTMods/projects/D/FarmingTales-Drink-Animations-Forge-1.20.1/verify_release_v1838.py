"""Verify the installed baseline, edible stages, client run, audio and editable exports."""
from pathlib import Path
import json,hashlib,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.37-before-cup-interior-fix'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.38.jar'
old=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.37.jar'
assert sha(old)==baseline['sha256']
assets=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
profiles=read(assets/'drink_profiles.json');servings=read(assets/'servings.json')
assert len(profiles)==225 and len(servings)==2
coffees={f'papercraft_magic_decoration:{n}' for n in ('gold_medal_coffee','white_rabbit_mocha','nocturnal_cat_coffee')}
assert all(p['capModel']=='animation/cups/empty' for p in profiles if p['id'] in coffees)
assert not coffees.intersection(servings)
ice=servings['papercraft_magic_decoration:long_island_popsicle_tea']
assert len(ice['stages'])==4
for i,model in enumerate(ice['stages']):
    elements=read(assets/f'models/{model}.json')['elements']
    if i==3:assert len(elements)==1 and elements[0]['name']=='original_0'
    else:assert any(e['name'].startswith('original_1') for e in elements)
    assert all('down' in e['faces'] for e in elements)
unchanged=lambda ps:[p for p in ps if p['id'] not in coffees]
assert unchanged(profiles)==unchanged(baseline['profiles'])
sources=read(ROOT/'papercraft-sources.json');assert sha(Path(sources['jar']))==sources['sha256']
for source in read(ROOT/'cocktail-sources.json')['jars']:assert sha(Path(source['path']))==source['sha256']
allowed={'com/farmingtales/drinkanim/math/BottleRig.class', 'com/farmingtales/drinkanim/math/ServingAnimations.class', 'com/farmingtales/drinkanim/math/CupAnimations.class', 'com/farmingtales/drinkanim/math/AnimationSoundTimeline.class', 'assets/farmingtales_drink_anim/hand_volumes.json', 'assets/farmingtales_drink_anim/models/animation/papercraft/diplomatico_coffee_serving_bite1.json', 'assets/farmingtales_drink_anim/models/animation/papercraft/white_rabbit_mocha.json', 'assets/farmingtales_drink_anim/models/animation/papercraft/gold_medal_coffee.json', 'assets/farmingtales_drink_anim/models/animation/papercraft/nocturnal_cat_coffee.json', 'META-INF/mods.toml'}
wood=[]
for model in ice['stages']:
    e=read(assets/f'models/{model}.json')['elements'][0];wood.append(e)
assert all(e==wood[0] for e in wood)
assert abs(wood[0]['to'][1]-wood[0]['from'][1]-9)<1e-5

with zipfile.ZipFile(jar) as current,zipfile.ZipFile(old) as previous:
    files=lambda z:{s for s in z.namelist() if not s.endswith('/')}
    changed={p for p in files(current)&files(previous) if current.read(p)!=previous.read(p)}
    assert changed == allowed,(changed-allowed,allowed-changed)
    assert files(current)==files(previous),'Unexpected added/removed JAR entries'
    assert not any('/smoke/' in p or p.startswith('assets/papercraft_magic_decoration/') for p in files(current))
    for p in (ROOT/'src/main/resources').rglob('*'):
        if p.is_file():assert current.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes(),p
    meta=current.read('META-INF/mods.toml').decode()
    assert all(x in meta for x in ['version="1.8.38"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests)
assert count==142
surface=read(ROOT/'preview/surface-check-1.8.38.json')
assert surface['opaque_cup_faces']==18 and surface['opaque_chocolate_core_faces']==6
assert all(n==1512 for n in surface['interior_rays'].values())
config=Path('Z:/config/farmingtales_drink_anim-client.toml');assert sha(config)==sha(BACKUP/config.name)
runs={}
for name,num in [('smoke-palm-slim-1.8.38.log',5),('smoke-palm-final-slim-1.8.38.log',2),('smoke-palm-wide-1.8.38.log',5),('smoke-forward-slim-1.8.38.log',1),('smoke-forward-wide-1.8.38.log',1)]:
    p=ROOT/'preview'/name;log=p.read_text(encoding='utf-8-sig')
    assert 'CATALOG_SMOKE_COMPLETE '+str(num) in log and 'BUILD SUCCESSFUL' in log
    assert 'SILENT_CLIENT' in log and 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
    assert 'Unknown soundEvent' not in log and 'Unable to play' not in log
    runs[name]={'path':str(p),'sha256':sha(p),'profiles':num,'shaders':False,'hidden':True}
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
audio=read(ROOT/'preview/audio-balance-wide-1.8.38.json')
for item,event,n in [('long_island_popsicle_tea','popsicle_bite',3),('long_island_popsicle_tea','stick_spit',1),('diplomatico_coffee','chocolate_bite',2),('long_island_popsicle_tea','snack_swallow',3),('diplomatico_coffee','snack_swallow',2)]:
    samples=[a for a in audio if a['item']=='papercraft_magic_decoration:'+item and a['event']==event]
    assert len(samples)==n and all(a['volume']>0 for a in samples),(item,event,samples)
folder=ROOT/'blockbench/01-常用工程/纸鸢酒水动画-v68';projects=read(folder/'导出检查.json');fit=read(ROOT/'preview/bb-fit-v68.json')
assert len(projects)==len(fit)==32
for project in projects:
    bb=read(Path(project['path']));assert len(bb['animations'])==13
    names={g['name'] for g in bb['groups']}
    assert any(e.get('type')=='camera' for e in bb['elements'])
    if project['item'].endswith(':long_island_popsicle_tea'):assert {f'serving_stage_{i}' for i in range(4)}<=names
    if project['item'] in coffees:assert not any(n.startswith('serving_stage_') for n in names)
    project['sha256']=sha(Path(project['path']))
archive=folder.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    assert sum(n.endswith('.bbmodel') for n in z.namelist())==32
    for project in projects:assert hashlib.sha256(z.read(Path(project['path']).name)).hexdigest()==project['sha256']
report=dict(version='1.8.38',sha256=sha(jar),unit_tests=count,profiles=profiles,
    blockbench=projects,blockbench_fit=fit,blockbench_archives=[dict(path=str(archive),sha256=sha(archive))],
    client_runs=runs,client_retested=True,soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.50,
    configuration_sha256=sha(config),switch_ticks=dict(draw=10,stow=9),servings=servings,
    changed_entries=sorted(changed),baseline_verification=str(BACKUP/'verification.json'),
    scope='Independent animation mod only. Opaque ceramic interiors and original carry orientation with the palm end centre attached to each coffee handle. UV-cropped closed chocolate bite with seated inertia. Popsicle helper turns inward before grasp and returns after extraction; food contacts the marked palm end centre. Draw 0.5s / stow 0.45s preserved',
    inherited_catalog_evidence=str(BACKUP/'verification.json'))
visual=read(ROOT/'preview/cup-interior-review-1.8.38.json')
assert visual['version']=='1.8.38' and visual['client_frames_reviewed']==482 and visual['reference_frames_reviewed']==1950
for recording in visual['client_recordings']:
    for sheet in recording['sheets']:assert sha(Path(sheet['path']))==sheet['sha256']
assert sum(c['frames'] for c in visual['client_recordings'])==visual['client_frames_reviewed']
report['surface_check']=surface
report['visual_review']=visual
report['reference_manifest']=str(ROOT/visual['reference_manifest'])
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('Verified 1.8.38:',count,'tests, 5 slim + 5 Steve client profiles, 32 editable projects;',sha(jar))
