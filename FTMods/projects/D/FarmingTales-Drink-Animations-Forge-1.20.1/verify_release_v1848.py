"""Check short openings, original ornaments, and the built/installed release."""
from pathlib import Path
from copy import deepcopy
import hashlib
import json
import zipfile
import xml.etree.ElementTree as ET
from container_assets import bounds, FACE_AXIS
from prepare_world_bottles import SPECS, ORNAMENTS, READ, ASSETS, PACK

ROOT=Path(__file__).resolve().parent
BACKUP=ROOT/'backups/1.8.47-before-short-mouth-and-ornaments'
PREFIX='src/main/resources/assets/farmingtales_drink_anim/'
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
profiles=READ(ASSETS/'drink_profiles.json')
by_id={p['id']:p for p in profiles}
decorations=READ(ASSETS/'decorations.json')
masks=READ(ASSETS/'hand_volumes.json')
baseline=READ(BACKUP/'baseline.json')

with zipfile.ZipFile(BACKUP/'source.zip') as old:
    previous=json.loads(old.read(PREFIX+'drink_profiles.json'))
    assert len(profiles)==279 and len(by_id)==279
    assert [p for p in profiles if p['id'] not in SPECS]==[p for p in previous if p['id'] not in SPECS]
    for p in previous:
        if p['id'] in SPECS:
            assert {k:v for k,v in p.items() if k not in ['mouthY','neckMin','neckMax']}=={
                k:v for k,v in by_id[p['id']].items() if k not in ['mouthY','neckMin','neckMax']},p['id']
    for key,value in json.loads(old.read(PREFIX+'decorations.json')).items():assert decorations[key]==value,key
    for key,value in json.loads(old.read(PREFIX+'hand_volumes.json')).items():assert masks[key]==value,key
    # Prior resources outside this bottle family remain byte-for-byte identical.
    for path in old.namelist():
        if not path.startswith(PREFIX):continue
        if path in {PREFIX+'drink_profiles.json',PREFIX+'hand_volumes.json',PREFIX+'decorations.json'}:continue
        if path.startswith(PREFIX+'models/animation/world_bottles/'):continue
        assert old.read(path)==(ROOT/path).read_bytes(),path

sources=READ(ROOT/'world-bottle-sources.json')
evidence={s['entry']:s for s in sources['sources']}
neck_report=[]
for item,spec in SPECS.items():
    p=by_id[item]
    entry=next(s['source'] for s in sources['items'] if s['id']==item)
    with zipfile.ZipFile(evidence[entry]['jar']) as source:original=json.loads(source.read(entry))
    cap=original['elements'][spec['cap'][0]]
    donor=original['elements'][spec['shell'][-1]]
    ca,cb=bounds(cap);da,db=bounds(donor)
    neck=READ(ASSETS/f"models/{p['neckModel']}.json")
    lo=[min(bounds(e)[0][a] for e in neck['elements']) for a in range(3)]
    hi=[max(bounds(e)[1][a] for e in neck['elements']) for a in range(3)]
    assert abs(hi[1]+p['neckJoin']*16-db[1]-1)<1e-6,item
    assert abs(p['mouthY']-(hi[1]+p['neckJoin']*16))<1e-6,item
    assert abs(p['neckMin']*16-lo[1])<1e-6 and abs(p['neckMax']*16-hi[1])<1e-6,item
    for axis in [0,2]:
        assert lo[axis]==max(ca[axis],da[axis]) and hi[axis]==min(cb[axis],db[axis]),item
    # No horizontal face covers the centre of the open neck.
    cx,cz=(lo[0]+hi[0])/2,(lo[2]+hi[2])/2
    for e in neck['elements']:
        a,b=bounds(e)
        if 'up' in e['faces'] or 'down' in e['faces']:
            assert not(a[0]<cx<b[0] and a[2]<cz<b[2]),item
    neck_report.append(dict(id=item,exposed_height=1,width=hi[0]-lo[0],depth=hi[2]-lo[2]))
    if item in ORNAMENTS:
        idx,kind,_=ORNAMENTS[item]
        parts=decorations[item];assert len(parts)==1
        part=parts[0];assert part['capAttached'] and part['kind']==kind
        mesh=READ(ASSETS/f"models/{part['model']}.json")
        element=deepcopy(original['elements'][idx]);element['name']=f'source_{idx}'
        element['faces']={d:f for d,f in element['faces'].items() if all(
            abs(element['to'][a]-element['from'][a])>.00001 for a in range(3) if a!=FACE_AXIS[d][0])}
        for face in element['faces'].values():face.pop('cullface',None)
        assert mesh['elements']==[element],item
        for key in ['bodyModel','capModel']:
            assert all(e['name']!=f'source_{idx}' for e in READ(ASSETS/f"models/{p[key]}.json")['elements']),item
    for key in ['bodyModel','capModel','neckModel']:
        model=READ(ASSETS/f"models/{p[key]}.json")
        assert model['elements']
        for e in model['elements']:
            assert e['faces']
            for face in e['faces'].values():
                ns,path=model['textures'][face['texture'][1:]].split(':')
                assert (ROOT/f'src/main/resources/assets/{ns}/textures/{path}.png').is_file()
for s in sources['sources']:
    with zipfile.ZipFile(s['jar']) as source:assert hashlib.sha256(source.read(s['entry'])).hexdigest()==s['sha256']

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ['failures','errors','skipped'])
count=sum(int(t.get('tests')) for t in tests)
client_logs=[]
for user,names in [('SteveSmoke',['dassai','strongbow','cool_tea','sour_plum','ice_tea','kwas_chlebowy','jack_daniel','luminous_bride']),
                   ('SlimSmoke',['dassai','strongbow','cool_tea','orange_juice_1'])]:
    path=ROOT/f'preview/smoke-short-mouth-{user}-1.8.48.log'
    log=path.read_text(encoding='utf-8-sig',errors='replace')
    assert f'CATALOG_SMOKE_COMPLETE {len(names)}' in log and 'BUILD SUCCESSFUL' in log,path
    assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log,path
    for name in names:
        item=next(p['id'] for p in profiles if p['id'].split(':')[1]==name)
        for label in ['body neck and decoration textures are present','drink consumes once and returns to idle']:
            assert f'CATALOG_SMOKE PASS {item} {label}' in log,(user,item,label)
        if item in ORNAMENTS:
            assert f'CATALOG_SMOKE PASS {item} cap ornament responds to rendered inspection, peak=' in log
        for stage in ['idle','inspect','open-neck','drink','replacement']:
            assert (ROOT/f'run-smoke/screenshots/v184-catalog-{user}-{name}-{stage}.png').is_file()
    client_logs.append(dict(path=str(path),sha256=sha(path),profiles=len(names)))

# The bow's final pivot was aligned to the knot after the first normal-arm pass.
path=ROOT/'preview/smoke-ornaments-SteveSmoke-1.8.48.log'
log=path.read_text(encoding='utf-8-sig',errors='replace')
assert 'CATALOG_SMOKE_COMPLETE 2' in log and 'BUILD SUCCESSFUL' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'DRINK_SMOKE FAIL' not in log
for item in ORNAMENTS:
    assert f'CATALOG_SMOKE PASS {item} cap ornament responds to rendered inspection, peak=' in log
    assert f'CATALOG_SMOKE PASS {item} drink consumes once and returns to idle' in log
client_logs.append(dict(path=str(path),sha256=sha(path),profiles=2,scope='Final bow knot pivot'))

jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.48.jar'
res=ROOT/'src/main/resources'
with zipfile.ZipFile(jar) as built:
    for path in res.rglob('*'):
        if path.is_file():assert built.read(path.relative_to(res).as_posix())==path.read_bytes(),path
    assert 'version="1.8.48"' in built.read('META-INF/mods.toml').decode()
    assert not any('/smoke/' in name for name in built.namelist())
config=PACK/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==baseline['configuration_sha256']
report=dict(version='1.8.48',sha256=sha(jar),previous_sha256=baseline['previous_sha256'],
            configuration_sha256=sha(config),unit_tests=count,client_logs=client_logs,openings=neck_report,
            scope='One-pixel neck extensions matched to original neck width. Dassai uses Orange wine gestures; original bow and Strongbow leaf use cap-attached inertial decorations. Isolated Forge client checks; full modpack/shaders not exercised.')
(ROOT/'verification-1.8.48.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.48: {count} tests; 8 normal-arm, 2 final ornament, and 4 slim-arm client sequences; SHA256 {sha(jar)}')
