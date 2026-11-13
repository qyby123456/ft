from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
BACKUP=ROOT/'backups/1.8.22-before-eating-variation'
JAR=ROOT/'build/libs/farmingtales_drink_anim-1.8.23.jar'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.22.jar'
assert sha(previous)==baseline['sha256']
with zipfile.ZipFile(JAR) as new,zipfile.ZipFile(previous) as old:
    assets=[n for n in old.namelist() if n.startswith('assets/')]
    assert assets and all(old.read(n)==new.read(n) for n in assets)
    assert new.read('farmingtales_drink_anim.mixins.json')==old.read('farmingtales_drink_anim.mixins.json')
    changed={name for name in old.namelist() if name.endswith('.class') and old.read(name)!=new.read(name)}
    expected={'client/BottleRenderer','client/DrinkController','math/DrinkMotion','math/BottleFraming'}
    assert changed=={'com/farmingtales/drinkanim/'+n+'.class' for n in expected},changed
    for name in ['EatingMotion','EatingVariants']:
        assert 'com/farmingtales/drinkanim/math/'+name+'.class' in new.namelist()
    profiles=json.loads(new.read('assets/farmingtales_drink_anim/drink_profiles.json'))
    assert profiles==baseline['profiles'] and len(profiles)==182
    metadata=new.read('META-INF/mods.toml').decode()
    assert all(s in metadata for s in ['version="1.8.23"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in new.namelist())
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert new.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()
for source in read(ROOT/'jar-family-sources.json')['sources']+read(ROOT/'jam-source-assets.json'):
    assert sha(Path(source['path']))==source['sha256']
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==125,count
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.23.log').read_text(encoding='utf-8-sig')
assert all(p.stat().st_mtime<=JAR.stat().st_mtime+2 for p in (ROOT/'src/main').rglob('*') if p.is_file())
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(BACKUP/config.name)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
runs={}
selections={
    'slim':['orange_wine','brandy_xo','mozhao','orange_juice_1','ancient_fruit_jam_1','egg_mayonnaise'],
    'wide':['orange_wine','brandy_xo','orange_juice_1','ancient_fruit_jam_1']}
for arm,items in selections.items():
    path=ROOT/f'preview/smoke-eating-{arm}-1.8.23.log'
    log=path.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in log and f'CATALOG_SMOKE_COMPLETE {len(items)}' in log
    assert 'CATALOG_SMOKE FAIL' not in log and 'player arm renderer model='+('slim' if arm=='slim' else 'default') in log
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
    checks=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in log.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    assert any('runtime selects all four eating styles' in s for s in checks)
    for item in items:
        selected=[s for s in checks if s.startswith('farmingtales:'+item+' ')]
        for expected in ['drink consumes once','inspect preserves all three items','ordinary right click keeps vanilla animation',
                         'stow finishes with empty hand','consecutive eating styles differ',
                         'selected eating style stays stable','rendered eating world camera moves subtly']:
            assert any(expected in s for s in selected),(arm,item,expected)
        if item.endswith('mayonnaise'):
            for expected in ['custom jar eating has no vanilla item crumbs','ordinary right click still produces vanilla eating particles']:
                assert any(expected in s for s in selected),(arm,item,expected)
    runs[arm]=dict(log=str(path),sha256=sha(path),assertions=checks)

fit=read(ROOT/'preview/bb-fit-v52.json')
assert len(fit)==108 and all(r['max_position_error_pixels']<=.01501 for r in fit)
projects=[];archives=[]
for folder,size in [('酱罐动画-v52',258),('酒水扩展-v52',106)]:
    directory=ROOT/'blockbench'/folder
    entries=read(directory/'导出检查.json');assert len(entries)==size
    for entry in entries:
        data=read(Path(entry['path']));assert len(data['animations'])==13
        assert 'ARR' in data['credit'] and '1.8.23' in data['credit']
        clips={a['name'].split('.')[-1]:a for a in data['animations']}
        assert all(k in clips for k in ['drink','drink_variant_2','drink_variant_3','drink_variant_4'])
        assert clips['idle']['length']==4 and clips['idle']['loop']=='loop'
        assert all(any(t.get('type')=='camera' for t in a['animators'].values()) for a in data['animations'])
        assert len({json.dumps(clips[k]['animators'],sort_keys=True) for k in ['drink','drink_variant_2','drink_variant_3','drink_variant_4']})==4
    archive=directory.with_suffix('.zip')
    with zipfile.ZipFile(archive) as z:
        assert sum(n.endswith('.bbmodel') for n in z.namelist())==size
        for path in directory.rglob('*'):
            if path.is_file():assert z.read(path.relative_to(directory).as_posix())==path.read_bytes()
    projects+=entries;archives.append(dict(path=str(archive),sha256=sha(archive)))
assert {p['item'] for p in projects}=={p['id'] for p in profiles}
templates={arm:read(ROOT/f'blockbench/上古水果果酱-v52/ancient_fruit_jam_1-{label}-v52.bbmodel') for arm,label in [('slim','纤细手臂'),('wide','Steve')]}
for entry in projects[:258]:
    model=read(Path(entry['path']));template=templates[entry['arm']]
    assert model['elements']==template['elements'] and model['outliner']==template['outliner']
    for actual,expected in zip(model['animations'],template['animations']):
        actual['name']=actual['name'].replace(entry['item'].split(':')[1],'ancient_fruit_jam_1')
        assert actual==expected
report=dict(version='1.8.23',sha256=sha(JAR),unit_tests=count,profiles=profiles,aged_aliases=baseline['aged_aliases'],
    jar_counts=baseline['jar_counts'],blockbench=projects,blockbench_fit=fit,blockbench_archives=archives,
    client_runs=runs,client_retested=True,soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
    configuration_sha256=sha(config),eating_variants=4,
    scope='Four stable eating variations with no consecutive repeats, mild delayed world-camera feedback and retained continuous breathing. '
    'Common rigid motion preserves hand/container/cap contacts; selected camera also drives framing. '
    '125 tests; ten representative hidden Forge cases across both arm shapes, shaders off. '
    '364 Blockbench projects with thirteen clips each, 108 prototype fits checked against shared runtime samples. '
    'Original models, textures, sounds, use timing and original mods unchanged. Full pack not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.23:',count,'tests;',len(projects),'Blockbench projects; both hidden clients.')
