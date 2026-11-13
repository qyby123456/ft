"""Validate the packaged Paper Kite extension against the installed baseline."""
from pathlib import Path
import copy, hashlib, json, zipfile, xml.etree.ElementTree as ET

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.30-before-serving-rework'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.31.jar'
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.30.jar'
assert sha(previous)==baseline['sha256']
assets=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
profiles=read(assets/'drink_profiles.json')
added=[p for p in profiles if p['id'].startswith('papercraft_magic_decoration:')]
assert len(profiles)==225 and len(added)==16
assert [p for p in profiles if p not in added]==[p for p in baseline['profiles'] if not p['id'].startswith('papercraft_magic_decoration:')]
servings=read(assets/'servings.json')
assert len(servings)==5 and sum(p['kind']=='SPOON' for p in servings.values())==3 and sum(p['kind']=='POPSICLE' for p in servings.values())==1
manifest=read(ROOT/'papercraft-sources.json')
assert sha(Path(manifest['jar']))==manifest['sha256']
with zipfile.ZipFile(manifest['jar']) as source:
    for a in manifest['assets']:assert hashlib.sha256(source.read(a['entry'])).hexdigest()==a['sha256']
for source in read(ROOT/'cocktail-sources.json')['jars']:assert sha(Path(source['path']))==source['sha256']

allowed_resources={'META-INF/mods.toml','assets/minecraft/atlases/blocks.json'}
allowed_resources |= {x.relative_to(ROOT/'src/main/resources').as_posix() for x in (ROOT/'src/main/resources').rglob('*') if x.is_file()}
allowed_resources |= {'assets/farmingtales_drink_anim/'+p for p in ['decorations.json','drink_profiles.json','hand_volumes.json','lang/en_us.json','lang/zh_cn.json','sounds.json']}
allowed_classes={'client/BottleRenderer','math/AnimationPlayback','math/AnimationSoundTimeline','math/BottleRig',
    'math/ContainerAnimations','math/ContainerAnimations$1','math/ContainerAnimations$Curve','math/DecorationMotion',
    'math/DecorationMotion$State','math/DrinkMotion','math/DrinkProfile','math/ServingAnimations','math/ServingAnimations$Serving','math/ServingAnimations$1','client/DrinkKeys'}
with zipfile.ZipFile(jar) as n,zipfile.ZipFile(previous) as p:
    names=lambda z:{s for s in z.namelist() if not s.endswith('/')}
    assert not names(p)-names(n)
    changed={s for s in names(p)&names(n) if p.read(s)!=n.read(s)}
    assert changed<=allowed_resources|{'com/farmingtales/drinkanim/'+s+'.class' for s in allowed_classes},changed
    assert n.read('com/farmingtales/drinkanim/math/CupAnimations.class')==p.read('com/farmingtales/drinkanim/math/CupAnimations.class')
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert n.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()
    assert not any(s.startswith('assets/papercraft_magic_decoration/') or '/smoke/' in s for s in names(n))
    meta=n.read('META-INF/mods.toml').decode()
    assert all(s in meta for s in ['version="1.8.31"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    # Resource manifests intentionally extend the baseline with Paper Kite models and stages.


tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count>=131
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.31.log').read_text(encoding='utf-8-sig')
config=Path('Z:/config/farmingtales_drink_anim-client.toml')
assert sha(config)==sha(BACKUP/'farmingtales_drink_anim-client.toml')
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()

runs={}
for name,num in [('smoke-papercraft-1.8.30.log',16),('smoke-serving-1.8.31.log',8),('smoke-papercraft-spoons-final-1.8.30.log',3)]:
    path=ROOT/'preview'/name;log=path.read_text(encoding='utf-8-sig')
    assert 'CATALOG_SMOKE_COMPLETE '+str(num) in log and 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log,name
    assert 'BUILD SUCCESSFUL' in log and 'SILENT_CLIENT' in log,name
    runs[name]=dict(path=str(path),sha256=sha(path),profiles=num,shaders=False,hidden=True)
spoon_log=(ROOT/'preview/smoke-papercraft-spoons-final-1.8.30.log').read_text(encoding='utf-8-sig')
assert spoon_log.count('drinking sounds match mouthfuls: 8')==3
assert spoon_log.count('spoon hidden outside eating')==6
assert spoon_log.count('two spoonfuls do not consume the entire coffee')==3

folder=ROOT/'blockbench/纸鸢酒水动画-v61'
projects=read(folder/'导出检查.json');assert len(projects)==32
fit=read(ROOT/'preview/bb-fit-v61.json');assert len(fit)==32
for project in projects:
    bb=read(Path(project['path']));assert len(bb['animations'])==13
    item=project['item'].split(':')[1]
    data=read(ROOT/f'build/blockbench/drinks/papercraft_{item}.json')
    if project['item'] in servings and servings[project['item']]['kind']=='SPOON':
        for name,clip in data['clips'].items():
            if name.split('/')[-1] not in ['drink','drink_variant_2','drink_variant_3','drink_variant_4','preview_complete']:
                assert all(not f['capVisible'] for f in clip['frames']),name
        frames=data['clips'][project['arm']+'/drink']['frames']
        assert not frames[0]['capVisible'] and not frames[-1]['capVisible']
        assert any(f['capVisible'] for f in frames)
archive=folder.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    for p in folder.iterdir():
        if p.is_file():assert p.read_bytes()==z.read(p.name)

report=copy.deepcopy(baseline)
report.update(version='1.8.31',sha256=sha(jar),unit_tests=count,profiles=profiles,
    blockbench=[p for p in baseline['blockbench'] if not p['item'].startswith('papercraft_magic_decoration:')]+projects,blockbench_fit=baseline['blockbench_fit']+fit,
    blockbench_archives=baseline['blockbench_archives']+[dict(path=str(archive),sha256=sha(archive))],
    baseline_verification=dict(path=str(BACKUP/'verification.json'),sha256=sha(BACKUP/'verification.json')),
    changed_entries=sorted(changed),client_retested=True,client_runs=runs,papercraft_drinks=16,
    papercraft_moving_parts=sum(len(i['moving_parts']) for i in manifest['items']),servings=servings,
    spoon_mouthfuls=8,spoon_sip_seconds=7.2,
    scope='Added 16 Paper Kite drinks using original assets in the independent mod. Dynamic garnishes; removed placement trays. '
    'Chocolate before coffee, original pull-tab can, and three spoons drawn only while eating, eight small scoops, residue, then stowed. '
    'Final spoon trajectories tested with slim arms; prior wide-arm meal smoke verified visibility/eight uses before final minor spoon angle/depth refinement. '
    'Both arm widths and handedness covered by rig tests. Hidden isolated fixture clients with shaders disabled; not a full-pack playthrough. '
    'Original mods, client configuration, 209 prior profiles and original cup inspection unchanged. 32 editable Blockbench projects with camera. '
    'Draw 0.5 seconds, stow 0.45 seconds; ordinary right click stays vanilla; hotkeys configurable.')
assert len(report['blockbench'])==450 and report['papercraft_moving_parts']==18
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.31: {count} tests; 225 profiles; 16 new drinks; 32 new Blockbench projects.')


