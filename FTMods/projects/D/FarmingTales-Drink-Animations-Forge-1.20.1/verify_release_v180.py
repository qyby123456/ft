"""Validate packaged resources, original pack assets, client evidence and portable exports."""
from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET
from PIL import Image
import io

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.7.2-before-cup-expansion'
VERSION='1.8.0'
PREFIX='assets/farmingtales_drink_anim/'
def read(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()
jar=ROOT/f'build/libs/farmingtales_drink_anim-{VERSION}.jar'
oldjar=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.7.2.jar'
assert sha(oldjar)==read(BACKUP/'verification.json')['sha256']
targets={'farmingtales:'+n for n in read(ROOT/'preview/cup-targets.json')}
with zipfile.ZipFile(jar) as new,zipfile.ZipFile(oldjar) as old:
    names=set(new.namelist())
    profiles=json.loads(new.read(PREFIX+'drink_profiles.json'))
    byid={p['id']:p for p in profiles}
    assert len(profiles)==len(byid)==53
    assert {p['id'] for p in profiles if p['style']=='CUP'}==targets
    assert 'DrinkPlacementMixin' in new.read('farmingtales_drink_anim.mixins.json').decode()
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in names)
    assert VERSION in new.read('META-INF/mods.toml').decode()
    unchanged_audio=[n for n in old.namelist() if n.startswith(PREFIX) and (n.endswith('.ogg') or n==PREFIX+'sounds.json')]
    assert all(new.read(n)==old.read(n) for n in unchanged_audio)
    previous_profiles=json.loads(old.read(PREFIX+'drink_profiles.json'))
    permitted_necks={'farmingtales:mysticfruit_wine','farmingtales:cherry_liqueur'}
    for p in previous_profiles:
        expected=dict(p)
        if p['id'] in permitted_necks:expected['neckJoin']=-.1/16
        assert byid[p['id']]==expected,p['id']
        for part in ['bodyModel','capModel','neckModel']:
            path=PREFIX+'models/'+p[part]+'.json'
            if p['id'] not in permitted_necks or part=='capModel':assert new.read(path)==old.read(path),path
    for p in profiles:
        for part in ['bodyModel','capModel','neckModel']:
            model=json.loads(new.read(PREFIX+'models/'+p[part]+'.json'))
            for texture in model.get('textures',{}).values():
                if not texture.startswith('#'):assert 'assets/'+texture.replace(':','/textures/')+'.png' in names,texture
        for region in p['liquid']:
            assert 'assets/'+region['texture'].replace(':','/') in names
    for name in ['light_beer_cup_1','wheatbeer_cup_1']:
        bottom=Image.open(io.BytesIO(new.read(PREFIX+f'textures/cups/{name}_bottom.png'))).convert('RGBA')
        assert bottom.getchannel('A').getextrema()==(255,255)
        body=json.loads(new.read(PREFIX+f'models/animation/cups/{name}.json'))
        assert any(e.get('name')=='closed_cup_bottom' and 'up' in e['faces'] for e in body['elements'])
        assert any(e.get('name')=='inner_cup_walls' and len(e['faces'])==4 for e in body['elements'])
    changed=sorted(n for n in old.namelist() if n in names and not n.endswith('/') and new.read(n)!=old.read(n))

sources=read(ROOT/'cup-profile-sources.json')
assert all(sha(s['path'])==s['sha256'] for s in sources),'An original pack asset changed'
details={p['id']:p for p in read(ROOT/'preview/cup-model-details.json')}
assert details['farmingtales:pumpkin_wine_cup']['removed']==[2,3,5]
assert details['farmingtales:watermelon_wine_cup']['removed']==[2,3,4]
assert sha(BACKUP/'farmingtales_drink_anim-client.toml')==sha(ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml')
tests=[ET.parse(p).getroot().attrib for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t['failures'])==0 and int(t['errors'])==0 for t in tests)
test_count=sum(int(t['tests']) for t in tests)
assert test_count>=75
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.0.log').read_text(encoding='utf-8-sig')

runs={}
for name,expected in [('cups-slim',targets),('wide',{'farmingtales:'+n for n in ['orange_juice_1','light_beer_cup_1','wheatbeer_cup_1','pumpkin_wine_cup','watermelon_wine_cup','purified_water','mysticfruit_wine','cherry_liqueur']})]:
    log=ROOT/f'preview/smoke-{name}-1.8.0.log'
    text=log.read_text(encoding='utf-8-sig')
    if name=='cups-slim':
        # All 29 item checks completed before E: filled during world-save shutdown.
        # Preserve that failure and require a clean follow-up for the final item and
        # both beer cups, whose interior meshes were subsequently completed.
        assert 'Unable to write to stream' in text and 'java.io.IOException' in text
        assert all(item+' stow finishes with empty hand' in text for item in expected)
        retry=ROOT/'preview/smoke-cups-slim-resume-1.8.0.log'
        retry_text=retry.read_text(encoding='utf-8-sig')
        assert 'BUILD SUCCESSFUL' in retry_text and 'CATALOG_SMOKE_COMPLETE 3' in retry_text
        assert all(f'{tag} FAIL' not in retry_text for tag in ['CATALOG_SMOKE','DRINK_SMOKE','SWITCH_SMOKE'])
    else:
        assert 'BUILD SUCCESSFUL' in text and f'CATALOG_SMOKE_COMPLETE {len(expected)}' in text,name
    assert all(f'{tag} FAIL' not in text for tag in ['CATALOG_SMOKE','DRINK_SMOKE','SWITCH_SMOKE']),name
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in text
    lines=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in text.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    if name=='cups-slim':
        retried={'farmingtales:'+n for n in ['light_beer_cup_1','wheatbeer_cup_1','cherry_liqueur_cup_1']}
        lines=[s for s in lines if s.split(' ')[0] not in retried]
        lines += [s.split('CATALOG_SMOKE PASS ',1)[1] for s in retry_text.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    assert {line.split(' ')[0] for line in lines}==expected
    assert all(any(s.startswith(item+' drink consumes once') for s in lines) for item in expected)
    assert any('failed placement does not animate or consume a cup' in s for s in lines)
    expected_skin='slim' if name=='cups-slim' else 'default'
    assert 'PASS player arm renderer model='+expected_skin in text
    runs[name]=dict(log=str(log),sha256=sha(log),items=sorted(expected),assertions=lines)
    if name=='cups-slim':runs[name].update(shutdown_interrupted_by_disk_full=True,resume_log=str(retry),resume_sha256=sha(retry),retested_items=sorted(retried))

bb=read(ROOT/'blockbench/酒水扩展-v30/导出检查.json')
fit=read(ROOT/'preview/bb-fit-v30.json')
assert len(bb)==len(fit)==106
for p in bb:
    model=read(p['path'])
    assert p['animations']==10
    assert any(e.get('type')=='camera' for e in model['elements'])
    assert any(a['name'].endswith('.place') for a in model['animations'])
    assert all(t.get('source','').startswith('data:image/png;base64,') for t in model['textures'])
archive=ROOT/'blockbench/酒水扩展-v30.zip'
with zipfile.ZipFile(archive) as z:
    assert len([n for n in z.namelist() if n.endswith('.bbmodel')])==106
    for p in bb:assert z.read(Path(p['path']).name)==Path(p['path']).read_bytes()

report=dict(version=VERSION,sha256=sha(jar),profiles=profiles,unit_tests=test_count,client_runs=runs,
    blockbench=bb,blockbench_archive=str(archive),blockbench_sha256=sha(archive),blockbench_fit=fit,
    soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
    unchanged_audio_entries=len(unchanged_audio),original_pack_assets_checked=len(sources),changed_jar_entries=changed,
    scope='53 profiles, including 29 new cups. Isolated Forge client with Embeddium, Oculus shaders disabled, Cookery, Grilling and Immersive Eating; every cup checked on slim, representative cups and repaired bottles on Steve. Ground/table/final-item/failed placement verified. Full modpack and user worlds not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified',VERSION,':',test_count,'tests,',sum(len(r['assertions']) for r in runs.values()),'client assertions, 106 Blockbench projects.')
print('SHA256',report['sha256'])
