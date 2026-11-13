"""Verify final geometry, original assets, gameplay, audio, and portable exports."""
from pathlib import Path
import json,zipfile,hashlib,struct,subprocess,sys,xml.etree.ElementTree as ET,base64,math
from prepare_matching_mouths import ROOT,ASSETS,PRIOR,V20,PREFIX,DRINKS,top_views

def read(p):return json.loads(Path(p).read_text(encoding='utf-8-sig'))
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()

subprocess.run([sys.executable,'-m','unittest','test_container_assets','test_wine_surfaces','test_matching_mouths'],cwd=ROOT,check=True)
jar=ROOT/'build/libs/farmingtales_drink_anim-1.6.7.jar'
specs=read(ROOT/'matching-mouth-sources.json')
changed={PREFIX+'models/'+p['neck']+'.json' for p in specs}|{PREFIX+'models/animation/drinks/'+n+'_body.json' for n in DRINKS}
allowed_classes={'DrinkConfig','BottleRenderer','NeckRenderer','BottleMotion','DrinkMotion','MotionDetail','ContainerAnimations','Locomotion','DrinkController','ClientEvents'}
with zipfile.ZipFile(jar) as z,zipfile.ZipFile(PRIOR) as old,zipfile.ZipFile(V20) as v20:
    names=set(z.namelist());profiles=json.loads(z.read(PREFIX+'drink_profiles.json'))
    assert len(profiles)==24 and 'version="1.6.7"' in z.read('META-INF/mods.toml').decode()
    assert names-set(old.namelist())=={'com/farmingtales/drinkanim/math/MotionDetail$1.class'},'Unexpected packaged file added'
    assert not set(old.namelist())-names,'Unexpected packaged file removed'
    assert not any('/smoke/' in n or 'SmokeFixture' in n or 'BendableHands' in n for n in names)
    for n in names:
        if n.endswith('.class'):
            assert struct.unpack('>H',z.read(n)[6:8])[0]==61
            if n.rsplit('/',1)[1].split('$')[0].removesuffix('.class') not in allowed_classes:assert z.read(n)==old.read(n),n
        if n.endswith('.json'):json.loads(z.read(n))
        if n.startswith('assets/') and not n.endswith('/'):
            if n in changed:assert z.read(n)==(ROOT/'src/main/resources'/n).read_bytes(),n
            else:assert z.read(n)==old.read(n),'Unrelated resource changed: '+n
    for name in DRINKS:
        path=PREFIX+'models/animation/drinks/'+name+'_body.json'
        assert json.loads(z.read(path))==top_views(json.loads(v20.read(path)))
    for suffix in ['body','cap','neck']:
        n=PREFIX+'models/animation/drinks/brandy_xo_'+suffix+'.json';assert z.read(n)==old.read(n)
    for row in specs:assert json.loads(z.read(PREFIX+'models/'+row['neck']+'.json'))['render_type']=='minecraft:cutout'
    assert json.loads(z.read('farmingtales_drink_anim.refmap.json'))['mappings']
    assert not any(n.startswith('assets/minecraft/') and 'sound' in n for n in names)
    events=json.loads(z.read(PREFIX+'sounds.json'))
    for event in ['inspect_lift','inspect_support','inspect_turn','inspect_return']:
        assert all(s['volume']==.15 for s in events[event]['sounds'])
sources=read(ROOT/'drink-profile-sources.json')
for item in sources:assert sha(item['path'])==item['sha256']
for item in read(ROOT/'original-hashes.json'):
    p=Path(item['Path'])
    if not p.exists() and Path(str(p)+'.disabled').exists():p=Path(str(p)+'.disabled')
    assert sha(p).upper()==item['Hash']

def close(a,b):return all(math.isclose(x,y,rel_tol=0,abs_tol=1e-7) for x,y in zip(a,b))
projects=[]
for p in profiles:
    name=p['id'].split(':')[1];scale=.7625*p['scale']
    for shape,label in [('slim','纤细手臂'),('wide','Steve')]:
        path=ROOT/f'blockbench/酒水扩展-v26/{name}-{label}-v26.bbmodel';bb=read(path)
        assert len(bb['animations'])==9 and any(e.get('type')=='camera' for e in bb['elements'])
        for part,key,pivot,shift in [('bottle','bodyModel',[8,4.8,8],[0,0,0]),('closure','capModel',p['capPivot'],[0,0,0]),('open_neck','neckModel',[8,4.8,8],[0,p['neckJoin']*16,0])]:
            m=read(ASSETS/('models/'+p[key]+'.json'))
            es=[e for e in bb['elements'] if e.get('name','').startswith(part+'_')]
            assert len(es)==len(m['elements']),(name,part)
            for e,src in zip(es,m['elements']):
                for bound in ['from','to']:
                    expected=[base+(v-piv+s)*scale for base,v,piv,s in zip([0,16,15.5],src[bound],pivot,shift)]
                    assert close(e[bound],expected),(name,part,bound)
                for f,s in src['faces'].items():
                    actual=e['faces'][f];tex=bb['textures'][int(actual['texture'])]
                    file=ASSETS/('textures/'+m['textures'][s['texture'][1:]].split(':')[1]+'.png')
                    assert tex['render_sides']=='front'
                    assert base64.b64decode(tex['source'].split(',')[1])==file.read_bytes()
                    assert close(actual['uv'],[v*(tex['width'] if i%2==0 else tex['height'])/16 for i,v in enumerate(s['uv'])])
                    assert actual.get('rotation',0)==s.get('rotation',0)
        projects.append(dict(item=p['id'],arm=shape,path=str(path),sha256=sha(path)))
fit=read(ROOT/'preview/bb-fit-v26.json');assert len(fit)==48
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(t.attrib['failures']=='0' and t.attrib['errors']=='0' for t in tests)
count=sum(int(t.attrib['tests']) for t in tests);assert count==65
selected={'orange_wine','sweet_grape_wine','rum','coconut_wine','brandy_xo','purified_water','vitamin_drink','sweet_orange_juice','light_beer','wheatbeer','mozhao','mozhao_p'}
clients={}
for arm,user in [('slim','SlimSteve29'),('wide','WideSteve10')]:
    logpath=ROOT/f'preview/smoke-{arm}-1.6.7.log';log=logpath.read_text(encoding='utf-8-sig')
    for marker in ['BUILD SUCCESSFUL','CATALOG_SMOKE_COMPLETE 12','SILENT_CLIENT: GLFW create/show/focus suppressed before first window','splash screen is disabled']:assert marker in log,marker
    for forbidden in ['CATALOG_SMOKE FAIL','DRINK_SMOKE FAIL','SWITCH_SMOKE FAIL','Using shaderpack','Redirect conflict',"Unable to load model: 'farmingtales_drink_anim:"]:assert forbidden not in log,forbidden
    catalog=(ROOT/f'preview/catalog-results-{user}-1.6.7.txt').read_text().splitlines()
    expected=12*12+4*3+2
    assert len(catalog)==expected and all(s.startswith('PASS ') for s in catalog),(arm,len(catalog))
    assert {s.split()[1].split(':')[1] for s in catalog}==selected
    clients[arm]=dict(catalog=catalog,log=str(logpath),sha256=sha(logpath),window_hidden_before_creation=True,shaders=False)
basic=(ROOT/'preview/smoke-results-slim-1.6.7.txt').read_text().splitlines()
switches=(ROOT/'preview/switch-results-slim-1.6.7.txt').read_text().splitlines()
assert len(basic)==30 and len(switches)==14 and all(s.startswith('PASS ') for s in basic+switches)
report=dict(version='1.6.7',sha256=sha(jar),profiles=profiles,unit_tests=count,surface_regressions=8,
    client_runs=clients,smoke_assertions=basic,switch_assertions=switches,catalog_assertions=clients['slim']['catalog'],
    blockbench=projects,blockbench_runtime_fit=fit,source_resources_unchanged=len(sources),soundVolume=.80,
    scope='Isolated Forge 47.4.10 client with Grilling, Cookery, ImmersiveEating, GeckoLib, Embeddium and Mekalus; hidden before window creation with shaders disabled. Twelve representative items tested on slim and Steve arms. Base interactions and switching rerun on slim. Original modpack resource hashes preserved. Full modpack not tested. Blockbench cannot simulate runtime liquid inertia or hand clipping.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.6.7:',count,'unit tests, 8 surface checks, 48 BB projects, both client runs and unchanged original assets.')
print('SHA256',report['sha256'])
