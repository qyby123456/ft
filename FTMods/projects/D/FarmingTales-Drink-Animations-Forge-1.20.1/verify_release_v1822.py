from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
BACKUP=ROOT/'backups/1.8.21-before-breathing'
JAR=ROOT/'build/libs/farmingtales_drink_anim-1.8.22.jar'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.21.jar'
assert sha(previous)==baseline['sha256']
with zipfile.ZipFile(JAR) as new,zipfile.ZipFile(previous) as old:
    assets=[n for n in old.namelist() if n.startswith('assets/')]
    assert assets and all(old.read(n)==new.read(n) for n in assets)
    new_mixins=json.loads(new.read('farmingtales_drink_anim.mixins.json'))
    old_mixins=json.loads(old.read('farmingtales_drink_anim.mixins.json'))
    assert new_mixins['client']==old_mixins['client']+['JarServerEatingParticlesMixin']
    changed=[]
    for name in old.namelist():
        if name.endswith('.class') and old.read(name)!=new.read(name):changed.append(name)
    assert set(changed)=={'com/farmingtales/drinkanim/client/BottleRenderer.class','com/farmingtales/drinkanim/mixin/JarEatingParticlesMixin.class'},changed
    assert 'com/farmingtales/drinkanim/math/BreathingMotion.class' in new.namelist()
    profiles=json.loads(new.read('assets/farmingtales_drink_anim/drink_profiles.json'))
    assert profiles==baseline['profiles'] and len(profiles)==182
    metadata=new.read('META-INF/mods.toml').decode()
    assert all(s in metadata for s in ['version="1.8.22"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in new.namelist())
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert new.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()
for source in read(ROOT/'jar-family-sources.json')['sources']+read(ROOT/'jam-source-assets.json'):
    assert sha(Path(source['path']))==source['sha256']
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests)
assert count==120
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.22.log').read_text(encoding='utf-8-sig')
assert all(p.stat().st_mtime<=JAR.stat().st_mtime+2 for p in (ROOT/'src/main').rglob('*') if p.is_file())
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(BACKUP/config.name)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
runs={}
for arm,model,size in [('slim','slim',2),('wide','default',3)]:
    path=ROOT/f'preview/smoke-breathing-{arm}-1.8.22.log'
    log=path.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in log and f'CATALOG_SMOKE_COMPLETE {size}' in log
    assert 'CATALOG_SMOKE FAIL' not in log and 'player arm renderer model='+model in log
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
    checks=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in log.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    items=['salmon_roe_sauce','egg_mayonnaise'] if arm=='slim' else ['orange_wine','ancient_fruit_jam_1','egg_mayonnaise']
    for item in items:
        selected=[s for s in checks if s.startswith('farmingtales:'+item+' ')]
        for expected in ['drink consumes once','inspect preserves all three items','ordinary right click keeps vanilla animation','stow finishes with empty hand']:
            assert any(expected in s for s in selected),(arm,item,expected)
        if item.endswith('mayonnaise') or item.endswith('roe_sauce'):
            for expected in ['custom jar eating has no vanilla item crumbs','ordinary right click still produces vanilla eating particles']:
                assert any(expected in s for s in selected),(arm,item,expected)
    runs[arm]=dict(log=str(path),sha256=sha(path),assertions=checks)

fit=read(ROOT/'preview/bb-fit-v51.json')
assert len(fit)==108 and all(r['max_position_error_pixels']<=.01501 for r in fit)
projects=[];archives=[]
for folder,size in [('酱罐动画-v51',258),('酒水扩展-v51',106)]:
    directory=ROOT/'blockbench'/folder
    entries=read(directory/'导出检查.json');assert len(entries)==size
    for entry in entries:
        data=read(Path(entry['path']));assert len(data['animations'])==10
        assert 'ARR' in data['credit'] and '1.8.22' in data['credit']
        idle=next(a for a in data['animations'] if a['name'].endswith('.idle'))
        assert idle['length']==4 and idle['loop']=='loop'
        assert all(any(t.get('type')=='camera' for t in a['animators'].values()) for a in data['animations'])
    archive=directory.with_suffix('.zip')
    with zipfile.ZipFile(archive) as z:
        assert sum(n.endswith('.bbmodel') for n in z.namelist())==size
        for path in directory.rglob('*'):
            if path.is_file():assert z.read(path.relative_to(directory).as_posix())==path.read_bytes()
    projects+=entries;archives.append(dict(path=str(archive),sha256=sha(archive)))
assert {p['item'] for p in projects}=={p['id'] for p in profiles}
templates={arm:read(ROOT/f'blockbench/上古水果果酱-v51/ancient_fruit_jam_1-{label}-v51.bbmodel') for arm,label in [('slim','纤细手臂'),('wide','Steve')]}
for entry in projects[:258]:
    model=read(Path(entry['path']));template=templates[entry['arm']]
    assert model['elements']==template['elements'] and model['outliner']==template['outliner']
    for actual,expected in zip(model['animations'],template['animations']):
        actual['name']=actual['name'].replace(entry['item'].split(':')[1],'ancient_fruit_jam_1')
        assert actual==expected
report=dict(version='1.8.22',sha256=sha(JAR),unit_tests=count,profiles=profiles,aged_aliases=baseline['aged_aliases'],
    jar_counts=baseline['jar_counts'],blockbench=projects,blockbench_fit=fit,blockbench_archives=archives,
    client_runs=runs,client_retested=True,soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
    configuration_sha256=sha(config),scope='Continuous shared breathing for all 182 profiles and 72 aged aliases. '
    'Idle and eating remain live; grips, caps and relative airborne paths preserved; no added world-camera motion. '
    '120 tests, representative hidden isolated Forge clients with both arm shapes, shaders disabled. '
    'Packet-origin filtering prevents server eating fragments escaping the local-mouth filter after Gaussian spread; ordinary use retains crumbs. '
    '364 Blockbench projects, full idle loop and continuous preview, 108 prototype projects fitted against Java. '
    'Original assets, authored gestures, sounds, user configuration and original mods unchanged; full pack not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.22:',count,'tests;',len(projects),'Blockbench projects; both hidden clients.')
