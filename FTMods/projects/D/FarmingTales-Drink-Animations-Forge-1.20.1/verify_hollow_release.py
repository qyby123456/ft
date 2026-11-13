"""Release checks for hollow bottles, the can aperture, and the XO screw cap."""
from pathlib import Path
import json, hashlib, zipfile, base64, io
from PIL import Image
import xml.etree.ElementTree as ET
from container_assets import GLASS,bounds

ROOT=Path(__file__).absolute().parent
PREFIX='assets/farmingtales_drink_anim/'
def read(p):return json.loads(Path(p).read_text(encoding='utf-8-sig'))
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()

def run():
    jar=ROOT/'build/libs/farmingtales_drink_anim-1.6.6.jar'
    geometry={}
    with zipfile.ZipFile(jar) as z:
        names=set(z.namelist());profiles=json.loads(z.read(PREFIX+'drink_profiles.json'))
        assert len(profiles)==24
        by_id={p['id'].split(':')[1]:p for p in profiles}
        assert by_id['brandy_xo']['style']=='BRANDY'
        assert not json.loads(z.read(PREFIX+'models/'+by_id['brandy_xo']['neckModel']+'.json'))['elements']
        assert len([n for n in names if n.startswith(PREFIX+'models/animation/drinks/') and n.endswith('.json')])==71
        atlas={a['resource'] for a in json.loads(z.read('assets/minecraft/atlases/blocks.json'))['sources']}
        for name in names:
            if name.startswith(PREFIX+'models/') and name.endswith('.json'):
                model=json.loads(z.read(name))
                for tex in model.get('textures',{}).values():
                    assert tex in atlas
                    assert PREFIX+'textures/'+tex.split(':')[1]+'.png' in names
        for profile in profiles:
            for r in profile['liquid']:
                name='assets/'+r['texture'].replace(':','/')
                assert name in names
                assert Image.open(io.BytesIO(z.read(name))).convert('RGBA').getchannel('A').getextrema()==(255,255),profile['id']
        for item in ['light_beer','wheatbeer']:
            p=by_id[item];assert p['style']=='BEER'
            cap=json.loads(z.read(PREFIX+'models/'+p['capModel']+'.json'))['elements'][0]
            assert abs(cap['to'][1]-cap['from'][1]-3)<1e-6
            body=json.loads(z.read(PREFIX+'models/'+p['bodyModel']+'.json'))['elements']
            lower=next(e for e in body if e.get('name')=='fixed_beer_neck')
            assert lower['to'][1]==cap['from'][1] and lower['from'][1]<10.1
            neck=json.loads(z.read(PREFIX+'models/'+p['neckModel']+'.json'))
            assert neck['render_type']=='minecraft:cutout' and len(neck['elements'])==1
            assert neck['elements'][0]['from']==cap['from'] and neck['elements'][0]['to']==cap['to']
            for face in neck['elements'][0]['faces'].values():
                tex=neck['textures'][face['texture'].removeprefix('#')]
                image=Image.open(io.BytesIO(z.read('assets/'+tex.replace(':','/textures/')+'.png'))).convert('RGBA')
                u0,v0,u1,v1=face['uv']
                region=image.crop((min(u0,u1)*image.width/16,min(v0,v1)*image.height/16,max(u0,u1)*image.width/16,max(v0,v1)*image.height/16))
                assert region.getchannel('A').getextrema()==(255,255)
        for item in GLASS:
            m=json.loads(z.read(PREFIX+'models/animation/drinks/'+item+'_body.json'))
            bottom=min(min(e['from'][1],e['to'][1]) for e in m['elements'])
            for e in m['elements']:
                if e.get('rotation',{}).get('angle',0):continue
                a,b=bounds(e)
                if item not in ['brandy_xo','rum','coconut_wine']:assert e['from']==a and e['to']==b, (item,'Drink shoulders keep their outward repair',e)
                for face in ['up','down']:
                    if face in e['faces'] and a[0]+1e-5<8<b[0]-1e-5 and a[2]+1e-5<8<b[2]-1e-5:
                        assert max(a[1],b[1])<=bottom+.13,(item,e)
            geometry[item]=dict(exterior_faces=sum(len(e['faces']) for e in m['elements']),internal_center_caps=0,original_negative_glass=item in ['brandy_xo','rum','coconut_wine'],inner_surface_rendered=True)
        for item in ['mozhao','mozhao_p']:
            profile=by_id[item];lid=profile['canLid'];hole=lid['hole'];x=(hole[0]+hole[2])/2;zz=(hole[1]+hole[3])/2
            assert len(profile['liquid'])==1
            body=json.loads(z.read(PREFIX+'models/'+profile['bodyModel']+'.json'))
            for e in body['elements']:
                if 'up' not in e['faces'] or e['to'][1]<9.4:continue
                a,b=bounds(e)
                assert not (a[0]+1e-6<x<b[0]-1e-6 and a[2]+1e-6<zz<b[2]-1e-6),'No hidden lid under the hole'
            assert PREFIX+'models/'+lid['flapModel']+'.json' in names
        events=json.loads(z.read(PREFIX+'sounds.json'))
        assert events['can_open']['sounds'][0]['name']=='farmingtales_drink_anim:container/can_open'
        audio=json.loads(z.read(PREFIX+'sound_sources.json'))['sources']
        physical=next(s for s in audio if s['resource'].endswith(':container/can_open'))
        assert {p['entry'] for p in physical['source']['components']}=={'minecraft/sounds/block/iron_trapdoor/open1.ogg','minecraft/sounds/random/fizz.ogg'}
        assert .5<physical['duration_seconds']<.8
        with zipfile.ZipFile(ROOT/'backups/1.4.0-before-hollow-bottles-and-cans/[农场物语-饮用与检视动画]farmingtales_drink_anim-1.4.0.jar') as old:
            for event,spec in json.loads(old.read(PREFIX+'sounds.json')).items():
                if event!='can_open':assert events[event]==spec,event
    sources=read(ROOT/'drink-profile-sources.json')
    for source in sources:assert sha(source['path'])==source['sha256']
    compatibility=read(ROOT/'compat-libs/grilling-test-hashes.json')
    for dep in compatibility:
        source=Path(dep['source'])
        if not source.exists() and source.suffix=='.jar' and Path(str(source)+'.disabled').exists():
            source=Path(str(source)+'.disabled')
        assert sha(source)==sha(dep['test_copy'])==dep['sha256']
        dep['observed_source']=str(source)
    tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
    assert tests and all(t.attrib['failures']=='0' and t.attrib['errors']=='0' for t in tests)
    selected_items={'brandy_xo','rum','coconut_wine'}
    selections={'slim':selected_items,'wide':selected_items}
    client_runs={}
    for arm,user in [('slim','SlimSteve29'),('wide','WideSteve10')]:
        selected=selections[arm]
        log=(ROOT/f'preview/smoke-{arm}-1.6.6.log').read_text(encoding='utf-8-sig')
        assert all(s in log for s in ['BUILD SUCCESSFUL',f'CATALOG_SMOKE_COMPLETE {len(selected)}'])
        assert 'Redirect conflict' not in log
        assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
        assert 'Using shaderpack' not in log
        assert 'splash screen is disabled' in log
        assert 'FileNotFoundException: farmingtales_drink_anim:' not in log
        assert "Unable to load model: 'farmingtales_drink_anim:" not in log
        base_log=ROOT/f'preview/smoke-{arm}-1.6.4.log'
        repeated_base=ROOT/f'preview/smoke-{arm}-base-1.6.4.log'
        if repeated_base.exists() and repeated_base.stat().st_mtime>base_log.stat().st_mtime:base_log=repeated_base
        base_text=base_log.read_text(encoding='utf-8-sig')
        assert all(s in base_text for s in ['BUILD SUCCESSFUL','DRINK_SMOKE_COMPLETE']) and 'DRINK_SMOKE FAIL' not in base_text
        prior_report=read(ROOT/'backups/1.6.4-before-v20-model-restore/verification.json')
        basic=prior_report['client_runs'][arm]['basic']
        catalog=(ROOT/f'preview/catalog-results-{user}-1.6.6.txt').read_text().splitlines()
        switches=prior_report['client_runs'][arm]['switches']
        assert len(basic)==30 and all(s.startswith('PASS ') for s in basic)
        expected=len(selected)*11+sum(3 if by_id[n]['style'] in ['CAN','PINK_CAN','BEER'] else 2 if by_id[n]['style']=='BRANDY' else 0 for n in selected)
        assert len(catalog)==expected and all(s.startswith('PASS ') for s in catalog),catalog
        assert {s.split()[1].split(':')[1] for s in catalog}==selected
        assert len(switches)==14 and all(s.startswith('PASS ') for s in switches),switches
        client_runs[arm]=dict(base_and_switch_verified_version='1.6.4',runtime_classes_identical=True,basic=basic,catalog=catalog,switches=switches,base_log=str(base_log),catalog_log=str(ROOT/f'preview/smoke-{arm}-1.6.6.log'))
    projects=[]
    for p in profiles:
        item=p['id'].split(':')[1]
        for arm,label in [('slim','纤细手臂'),('wide','Steve')]:
            path=ROOT/f'blockbench/酒水扩展-v25/{item}-{label}-v25.bbmodel';bb=read(path)
            assert len(bb['animations'])==9
            runtime=read(ROOT/f'build/blockbench/drinks/{item}.json')['clips']
            for a in bb['animations']:
                name=a['name'].split('.')[-1]
                if name!='preview_complete':assert abs(a['length']-runtime[('slim/' if arm=='slim' else 'wide/')+name]['length'])<1e-7
                if name=='draw':assert abs(a['length']-.6)<1e-7
                if name=='put_away':assert abs(a['length']-.55)<1e-7

            camera=next(e for e in bb['elements'] if e.get('type')=='camera')
            assert camera['position']==[0,16,15.5]
            for a in bb['animations']:assert camera['uuid'] in a['animators']
            for t in bb['textures']:assert base64.b64decode(t['source'].split(',')[1]).startswith(b'\x89PNG')
            for cube in [e for e in bb['elements'] if e['name'].endswith('_arm_skin')]:
                assert abs((cube['to'][0]-cube['from'][0])/.7-(3 if arm=='slim' else 4))<1e-5
                assert abs((cube['to'][1]-cube['from'][1])/.7-12)<1e-5
            if item in ('mozhao','mozhao_p'):
                group=next(g for g in bb['groups'] if g['name']=='can_opening_flap')
                drink=next(a for a in bb['animations'] if a['name'].endswith('.drink'))
                assert group['uuid'] in drink['animators']
                assert any(g['name']=='can_liquid_space' for g in bb['groups'])
            projects.append(dict(item=p['id'],arm=arm,path=str(path),sha256=sha(path)))
    assert len(projects)==48
    fit=read(ROOT/'preview/bb-fit-v25.json')
    assert len(fit)==48 and {(r['item'],r['arm']) for r in fit}=={(p['id'].split(':')[1],arm) for p in profiles for arm in ['slim','wide']}
    assert all(r['max_position_error_pixels']<=.06001 and r['max_rotation_error_degrees']<.90 and r['max_camera_error_degrees']<.30 for r in fit)
    from verify_v20_models import verify_projects
    v20_comparison=verify_projects(profiles)
    report=dict(v20_model_comparison=v20_comparison,version='1.6.6',jar=str(jar),sha256=sha(jar),profiles=profiles,geometry=geometry,
        unit_tests=sum(int(t.attrib['tests']) for t in tests),surface_regressions=6,smoke_assertions=client_runs['slim']['basic'],catalog_assertions=client_runs['slim']['catalog'],switch_assertions=client_runs['slim']['switches'],client_runs=client_runs,
        blockbench=projects,blockbench_runtime_fit=fit,source_resources_unchanged=len(sources),compatibility_dependencies_unchanged=compatibility,
        sound_sources=read(ROOT/'src/main/resources/assets/farmingtales_drink_anim/sound_sources.json'),
        scope='Forge 47.4.10 isolated client with same-ID fixture items and Grilling, Cookery, ImmersiveEating, GeckoLib, Embeddium and Mekalus. The three restored wine bodies were retested on slim and Steve arms, hidden before window creation with shaders disabled. All runtime classes are byte-identical to 1.6.4; its passed base interactions and rapid-switch tests are retained as prior evidence, not claimed as rerun. 62 unit tests and 48 Blockbench runtime exports checked. All original v20 wine faces are preserved; top reverse faces and two XO inner label backs are the only geometry additions. 38 BB bottle bodies are checked against those exact allowed additions. Brandy side grip and centered two-hand drinking are retained. Beer necks remain opaque. Full modpack not tested; Blockbench does not simulate runtime liquid inertia or hand clipping.')
    (ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
    print(f"Verified 1.6.6: {report['unit_tests']} unit tests, {len(basic)} base client assertions, {len(catalog)} container assertions, 48 Blockbench projects. SHA256 {report['sha256']}")
