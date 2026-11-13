from pathlib import Path
from PIL import Image
import base64, copy, hashlib, io, json, zipfile, xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
BACKUP=ROOT/'backups/1.8.25-before-chocolate-sauce'
JAR=ROOT/'build/libs/farmingtales_drink_anim-1.8.26.jar'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.25.jar'
assert sha(previous)==baseline['sha256']
prefix='assets/farmingtales_drink_anim/'
item='farmingtales:chocolate_sauce'
prototype='farmingtales:ancient_fruit_jam_1'
with zipfile.ZipFile(JAR) as new,zipfile.ZipFile(previous) as old:
    changed={n for n in old.namelist() if not n.endswith('/') and old.read(n)!=new.read(n)}
    assert changed=={'META-INF/mods.toml',prefix+'drink_profiles.json',prefix+'hand_volumes.json',
        'assets/minecraft/atlases/blocks.json','com/farmingtales/drinkanim/math/DrinkProfile.class',
        'com/farmingtales/drinkanim/math/AnimationSoundTimeline.class'},changed
    added={n for n in new.namelist() if not n.endswith('/') and n not in old.namelist()}
    assert added=={prefix+'models/animation/jam/chocolate_sauce/'+s+'.json' for s in ['body','open_body','stopper']}|{
        prefix+'textures/jam/chocolate_sauce'+s+'.png' for s in ['','_sealed','_rim','_content']},added
    profiles=json.loads(new.read(prefix+'drink_profiles.json'))
    assert len(profiles)==183 and [p for p in profiles if p['id']!=item]==baseline['profiles']
    chocolate=next(p for p in profiles if p['id']==item)
    expected=copy.deepcopy(next(p for p in profiles if p['id']==prototype))
    expected.update(id=item,bodyModel='animation/jam/chocolate_sauce/body',
        capModel='animation/jam/chocolate_sauce/stopper',neckModel='animation/jam/chocolate_sauce/open_body')
    expected['liquid'][0]['texture']='farmingtales_drink_anim:textures/jam/chocolate_sauce_content.png'
    assert chocolate==expected
    masks=json.loads(new.read(prefix+'hand_volumes.json'));prior=json.loads(old.read(prefix+'hand_volumes.json'))
    assert masks[item]==masks[prototype] and {k:v for k,v in masks.items() if k!=item}==prior
    atlas=json.loads(new.read('assets/minecraft/atlases/blocks.json'))
    old_atlas=json.loads(old.read('assets/minecraft/atlases/blocks.json'))
    assert atlas['sources'][:-4]==old_atlas['sources']
    assert {s['resource'] for s in atlas['sources'][-4:]}=={'farmingtales_drink_anim:jam/chocolate_sauce'+s for s in ['','_sealed','_rim','_content']}
    metadata=new.read('META-INF/mods.toml').decode()
    assert all(s in metadata for s in ['version="1.8.26"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in new.namelist())
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert new.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()

manifest=read(ROOT/'jar-family-sources.json')
for source in manifest['sources']+read(ROOT/'jam-source-assets.json'):
    assert sha(Path(source['path']))==source['sha256']
for source in read(BACKUP/'jar-family-sources.json')['sources']:
    assert sha(Path(source['path']))==source['sha256']
entry=next(e for e in manifest['entries'] if e['id']==item)
reference=next(e for e in manifest['entries'] if e['id']==prototype)
assert read(Path(entry['geometry']))['elements']==read(Path(reference['geometry']))['elements']
assets=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
assert sha(Path(entry['texture']))==sha(assets/'textures/jam/chocolate_sauce.png')

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==125,count
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.26.log').read_text(encoding='utf-8-sig')
assert all(p.stat().st_mtime<=JAR.stat().st_mtime+2 for p in (ROOT/'src/main').rglob('*') if p.is_file())
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(BACKUP/config.name)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
logpath=ROOT/'preview/smoke-chocolate-slim-1.8.26.log'
log=logpath.read_text(encoding='utf-8-sig')
assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 1' in log
assert 'CATALOG_SMOKE FAIL' not in log and 'player arm renderer model=slim' in log
assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
checks=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in log.splitlines() if 'CATALOG_SMOKE PASS ' in s]
for expected in ['drink consumes once','inspect preserves all three items','ordinary right click keeps vanilla animation',
    'stow finishes with empty hand','fast switch keeps the old food visible until stowed','fast switch reaches the new food in',
    'rapid reselection during draw settles on the latest food','fruit jam eating sounds are 35 percent louder',
    'jam uses local vanilla honey audio','jam eating uses only the main hand']:
    assert any(s.startswith(item+' ') and expected in s for s in checks),expected
assert any('jar family models and original-texture liquid loaded '+item in s for s in checks)

directory=ROOT/'blockbench/巧克力酱补充-v55'
entries=read(directory/'导出检查.json');assert len(entries)==2
for entry in entries:
    data=read(Path(entry['path']));label='纤细手臂' if entry['arm']=='slim' else 'Steve'
    template=read(ROOT/f'blockbench/上古水果果酱-v54/ancient_fruit_jam_1-{label}-v54.bbmodel')
    assert data['elements']==template['elements'] and data['outliner']==template['outliner']
    assert len(data['animations'])==13 and '1.8.26' in data['credit'] and 'ARR' in data['credit']
    for actual,expected in zip(data['animations'],template['animations']):
        actual['name']=actual['name'].replace('chocolate_sauce','ancient_fruit_jam_1')
        assert actual==expected
    for texture in data['textures']:
        if not texture['name'].startswith('chocolate_sauce'):continue
        actual=Image.open(io.BytesIO(base64.b64decode(texture['source'].split(',')[1]))).convert('RGBA')
        expected=Image.open(assets/'textures/jam'/texture['name']).convert('RGBA')
        assert actual.convert('RGB').tobytes()==expected.convert('RGB').tobytes()
archive=directory.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    assert sum(n.endswith('.bbmodel') for n in z.namelist())==2
    for path in directory.iterdir():
        if path.is_file():assert z.read(path.name)==path.read_bytes()
for previous_archive in baseline['blockbench_archives']:
    assert sha(Path(previous_archive['path']))==previous_archive['sha256']

report=copy.deepcopy(baseline)
report.update(version='1.8.26',sha256=sha(JAR),unit_tests=count,profiles=profiles,
    jar_counts=dict(jam=32,roe_sauce=89,mayonnaise=8,chocolate_sauce=1),
    blockbench=baseline['blockbench']+entries,
    blockbench_archives=baseline['blockbench_archives']+[dict(path=str(archive),sha256=sha(archive))],
    client_runs={'slim':dict(log=str(logpath),sha256=sha(logpath),assertions=checks)},client_retested=True,
    baseline_verification=dict(path=str(BACKUP/'verification.json'),sha256=sha(BACKUP/'verification.json')),
    scope='Add chocolate sauce using original texture and unchanged approved jam geometry/motion. '
    '125 tests; one hidden Forge client case with shaders off verifies resources, consumption, inspection, vanilla right click, honey audio and switching. '
    'Two new Blockbench rigs reuse verified v54 curves for both arm shapes; previous 364 projects unchanged. '
    'Draw 0.5s and stow 0.45s preserved. Full pack not launched; original mods and KubeJS unchanged.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.26:',count,'tests;',len(profiles),'profiles; chocolate sauce hidden client and two Blockbench projects.')
