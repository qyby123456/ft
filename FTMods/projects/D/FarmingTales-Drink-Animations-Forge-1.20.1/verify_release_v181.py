"""Verify release resources, untouched pack files, runtime evidence and portable projects."""
from pathlib import Path
from copy import deepcopy
import hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.0-before-motion-refinement'
VERSION='1.8.1'
PREFIX='assets/farmingtales_drink_anim/'
def read(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def same_numbers(a,b):
    if isinstance(a,dict):return isinstance(b,dict) and a.keys()==b.keys() and all(same_numbers(a[k],b[k]) for k in a)
    if isinstance(a,list):return isinstance(b,list) and len(a)==len(b) and all(same_numbers(x,y) for x,y in zip(a,b))
    if isinstance(a,(float,int)) and not isinstance(a,bool):return abs(a-b)<1e-7
    return a==b

jar=ROOT/f'build/libs/farmingtales_drink_anim-{VERSION}.jar'
oldjar=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.0.jar'
assert sha(oldjar)==read(BACKUP/'verification.json')['sha256']
targets={n:(6,'LEAF') for n in ['red_grape_juice_1','blueberry_juice_1','sweet_berry_juice_1','white_grape_juice_1']}
targets.update({n:(7,'RIBBON') for n in ['mysticfruit_wine','cherry_liqueur']})
targets.update({'pumpkin_wine_cup':(2,'STICK'),'coconut_wine_cup_1':(5,'ICE'),'mysticfruit_wine_cup_1':(4,'ICE'),'rum_cup_1':(4,'ICE')})
permitted_necks={'farmingtales:mysticfruit_wine','farmingtales:cherry_liqueur'}
with zipfile.ZipFile(jar) as new,zipfile.ZipFile(oldjar) as old:
    names=set(new.namelist())
    profiles=json.loads(new.read(PREFIX+'drink_profiles.json'))
    byid={p['id']:p for p in profiles}
    assert len(profiles)==len(byid)==53
    assert {p['id'] for p in profiles if p['style']=='CUP'}=={'farmingtales:'+n for n in read(ROOT/'preview/cup-targets.json')}
    assert 'DrinkPlacementMixin' in new.read('farmingtales_drink_anim.mixins.json').decode()
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in names)
    assert VERSION in new.read('META-INF/mods.toml').decode()
    unchanged_audio=[n for n in old.namelist() if n.startswith(PREFIX) and (n.endswith('.ogg') or n==PREFIX+'sounds.json')]
    assert all(new.read(n)==old.read(n) for n in unchanged_audio)
    old_textures=[n for n in old.namelist() if n.startswith(PREFIX+'textures/') and not n.endswith('/')]
    assert all(new.read(n)==old.read(n) for n in old_textures),'An original texture changed'
    decorations=json.loads(new.read(PREFIX+'decorations.json'))
    assert set(decorations)=={'farmingtales:'+n for n in targets}
    permitted_model_paths=set()
    for p in json.loads(old.read(PREFIX+'drink_profiles.json')):
        expected=deepcopy(p)
        if p['id'] in permitted_necks:expected['neckJoin']=-.25/16
        assert byid[p['id']]==expected,p['id']
        item=p['id'].split(':')[1]
        for part in ['bodyModel','capModel','neckModel']:
            path=PREFIX+'models/'+p[part]+'.json'
            if (part=='bodyModel' and item in targets) or (part=='neckModel' and p['id'] in permitted_necks):
                permitted_model_paths.add(path)
            else:assert new.read(path)==old.read(path),path
        if item not in targets:continue
        index,kind=targets[item]
        path=PREFIX+'models/'+p['bodyModel']+'.json'
        original=json.loads(old.read(path))
        expected=deepcopy(original)
        if kind=='RIBBON':
            for index2,dy in [(5,.15),(6,.15),(7,.25)]:
                element=expected['elements'][index2]
                for key in ['from','to']:element[key][1]-=dy
                element['rotation']['origin'][1]-=dy
        detached=expected['elements'].pop(index)
        assert same_numbers(json.loads(new.read(path)),expected),(item,'body geometry')
        parts=decorations[p['id']]
        assert len(parts)==1 and parts[0]['kind']==kind
        part=parts[0]
        assert len(part['pivot'])==len(part['extent'])==3 and min(part['extent'])>=0
        accessory=json.loads(new.read(PREFIX+'models/'+part['model']+'.json'))
        assert same_numbers(accessory['elements'],[detached]),(item,'accessory geometry or UV')
        assert accessory['textures']==original['textures']
        assert detached not in expected['elements'],(item,'duplicate accessory')
    for name in old.namelist():
        if name.startswith(PREFIX+'models/') and name.endswith('.json') and name not in permitted_model_paths:
            assert old.read(name)==new.read(name),name
    masks=json.loads(old.read(PREFIX+'hand_volumes.json'))
    for item in permitted_necks:
        for box in masks[item]:
            if box['min'][1]>=11.1:box['shiftY']=-.25
    assert same_numbers(masks,json.loads(new.read(PREFIX+'hand_volumes.json')))
    for name in names:
        if name.startswith(PREFIX+'models/') and name.endswith('.json'):
            model=json.loads(new.read(name))
            for texture in model.get('textures',{}).values():
                if not texture.startswith('#'):assert 'assets/'+texture.replace(':','/textures/')+'.png' in names,(name,texture)
    for p in profiles:
        for region in p['liquid']:assert 'assets/'+region['texture'].replace(':','/') in names
    changed=sorted(n for n in old.namelist() if n in names and not n.endswith('/') and new.read(n)!=old.read(n))
    for file in (ROOT/'src/main/resources').rglob('*'):
        if file.is_file():assert new.read(file.relative_to(ROOT/'src/main/resources').as_posix())==file.read_bytes(),file

sources=read(ROOT/'cup-profile-sources.json')
assert all(sha(s['path'])==s['sha256'] for s in sources),'An original pack asset changed'
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(BACKUP/'farmingtales_drink_anim-client.toml')==sha(config)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
tests=[ET.parse(p).getroot().attrib for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t['failures'])==int(t['errors'])==0 for t in tests)
test_count=sum(int(t['tests']) for t in tests)
assert test_count==82,test_count
assert 'BUILD SUCCESSFUL' in (ROOT/f'preview/build-{VERSION}.log').read_text(encoding='utf-8-sig')
assert not any(p.stat().st_mtime>jar.stat().st_mtime+2 for p in (ROOT/'src/main').rglob('*') if p.is_file()),'Source newer than build'

slim='orange_wine,mysticfruit_wine,cherry_liqueur,purified_water,orange_juice_1,pumpkin_wine_cup,red_grape_juice_1,blueberry_juice_1,sweet_berry_juice_1,white_grape_juice_1,coconut_wine_cup_1,mysticfruit_wine_cup_1,rum_cup_1,brandy_xo,light_beer_cup_1,wheatbeer_cup_1'
wide='orange_wine,mysticfruit_wine,cherry_liqueur,purified_water,orange_juice_1,pumpkin_wine_cup,red_grape_juice_1,coconut_wine_cup_1,mysticfruit_wine_cup_1,rum_cup_1,brandy_xo,light_beer_cup_1,wheatbeer_cup_1'
runs={}
for name,items in [('slim',slim),('wide',wide)]:
    expected={'farmingtales:'+n for n in items.split(',')}
    log=ROOT/f'preview/smoke-{name}-{VERSION}.log'
    content=log.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in content and f'CATALOG_SMOKE_COMPLETE {len(expected)}' in content,name
    assert all(f'{tag} FAIL' not in content for tag in ['CATALOG_SMOKE','DRINK_SMOKE','SWITCH_SMOKE']),name
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in content
    assert 'PASS player arm renderer model='+('slim' if name=='slim' else 'default') in content
    lines=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in content.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    assert {line.split(' ')[0] for line in lines}==expected
    for item in expected:
        for assertion in ['drink consumes once','four drinking sounds','ordinary right click keeps vanilla animation','stow finishes with empty hand']:
            assert any(s.startswith(item+' '+assertion) for s in lines),(name,item,assertion)
        if item in decorations:
            for part in decorations[item]:assert item+' dynamic '+part['kind']+' model loaded' in lines
    for assertion in ['failed placement does not animate or consume a cup','rapid placement preserves the moving hand clock','rapid placements consume exactly two and recover once']:
        assert any(assertion in s for s in lines),(name,assertion)
    runs[name]=dict(log=str(log),sha256=sha(log),items=sorted(expected),assertions=lines)

folder=ROOT/'blockbench/酒水扩展-v31'
bb=read(folder/'导出检查.json')
fit=read(ROOT/'preview/bb-fit-v31.json')
assert len(bb)==len(fit)==106
expected_projects={(p['id'],arm) for p in profiles for arm in ['wide','slim']}
assert {(p['item'],p['arm']) for p in bb}==expected_projects
assert {('farmingtales:'+p['item'],p['arm']) for p in fit}==expected_projects
for p in fit:
    assert p['max_position_error_pixels']<=.06001 and p['max_rotation_error_degrees']<.9 and p['max_camera_error_degrees']<.3
for entry in bb:
    model=read(entry['path'])
    assert entry['animations']==len(model['animations'])==10
    assert any(e.get('type')=='camera' for e in model['elements'])
    assert all(t.get('source','').startswith('data:image/png;base64,') for t in model['textures'])
    assert {a['name'].split('.')[-1] for a in model['animations']}=={'idle','draw','put_away','inspect','drink','walk','sprint','jump','place','preview_complete'}
    for animation in model['animations']:
        assert any(track.get('type')=='camera' for track in animation['animators'].values())
        for part in decorations.get(entry['item'],[]):
            tracks=[t for t in animation['animators'].values() if t['name']==part['name']]
            assert len(tracks)==1 and {'position','rotation'}<={k['channel'] for k in tracks[0]['keyframes']}
    entry['sha256']=sha(entry['path'])
archive=ROOT/'blockbench/酒水扩展-v31.zip'
with zipfile.ZipFile(archive) as z:
    files=[p for p in folder.iterdir() if p.is_file()]
    assert set(z.namelist())=={p.name for p in files}
    assert len([n for n in z.namelist() if n.endswith('.bbmodel')])==106
    for file in files:assert z.read(file.name)==file.read_bytes(),file
boards=[Path(p) for p in read(ROOT/'preview/review-v31/boards.json')]+[ROOT/'preview/review-v31/arm-comparison.jpg']
assert all(p.is_file() for p in boards)
report=dict(version=VERSION,sha256=sha(jar),profiles=profiles,decorations=decorations,unit_tests=test_count,client_runs=runs,
    blockbench=bb,blockbench_archive=str(archive),blockbench_sha256=sha(archive),blockbench_fit=fit,
    soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
    unchanged_audio_entries=len(unchanged_audio),unchanged_textures=len(old_textures),original_pack_assets_checked=len(sources),
    configuration_sha256=sha(config),changed_jar_entries=changed,review_boards=[dict(path=str(p),sha256=sha(p)) for p in boards],
    scope='53 existing profiles. New dynamic ornaments on 10 containers; revised cup raise, placement, water catch return and wine sip position. Isolated Forge clients: 16 items on slim, 13 on Steve, Embeddium/Oculus with shaders disabled, Cookery/Grilling/Immersive Eating. Hidden before window creation. Representative actions reviewed at 0.1-second intervals. Full modpack and user worlds not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified',VERSION,':',test_count,'tests,',sum(len(r['assertions']) for r in runs.values()),'client assertions, 106 Blockbench projects.')
print('SHA256',report['sha256'])
