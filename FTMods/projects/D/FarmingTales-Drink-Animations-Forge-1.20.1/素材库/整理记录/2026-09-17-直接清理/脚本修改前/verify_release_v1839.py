from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.38-before-coffee-disable-can-fix'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.39.jar'
old=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.38.jar'
assert sha(old)==baseline['sha256']
assets=ROOT/'src/main/resources/assets/farmingtales_drink_anim'
profiles=read(assets/'drink_profiles.json')
removed={'papercraft_magic_decoration:'+n for n in ('white_rabbit_mocha','gold_medal_coffee','nocturnal_cat_coffee')}
can='papercraft_magic_decoration:guang_s'
assert len(profiles)==222 and removed.isdisjoint(p['id'] for p in profiles)
assert any(p['id']=='farmingtales:sakura_wine' for p in profiles)
assert [p for p in profiles if p['id']!=can]==[p for p in baseline['profiles'] if p['id'] not in removed|{can}]
before=next(p for p in baseline['profiles'] if p['id']==can)
after=next(p for p in profiles if p['id']==can)
assert {k:v for k,v in before.items() if k!='liquid'}=={k:v for k,v in after.items() if k!='liquid'}
assert len(after['liquid'])==2
for name in ['decorations','hand_volumes']:
    a=read(assets/(name+'.json'))
    b=read(BACKUP/'src/main/resources/assets/farmingtales_drink_anim'/(name+'.json'))
    assert removed.isdisjoint(a)
    assert {k:v for k,v in a.items() if k!=can}=={k:v for k,v in b.items() if k not in removed|{can}}
allowed={'META-INF/mods.toml'}|{'assets/farmingtales_drink_anim/'+n for n in ['drink_profiles.json','decorations.json','hand_volumes.json','models/animation/papercraft/guang_s.json']}
with zipfile.ZipFile(jar) as current,zipfile.ZipFile(old) as previous:
    files=lambda z:{p for p in z.namelist() if not p.endswith('/')}
    assert files(current)==files(previous)
    changed={p for p in files(current) if current.read(p)!=previous.read(p)}
    assert changed==allowed,changed
    for p in (ROOT/'src/main/resources').rglob('*'):
        if p.is_file():assert current.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes()
    meta=current.read('META-INF/mods.toml').decode()
    assert all(v in meta for v in ['version="1.8.39"','authors="紫菜"','license="ARR"'])
source=read(ROOT/'papercraft-sources.json');assert sha(Path(source['jar']))==source['sha256']
config=Path('Z:/config/farmingtales_drink_anim-client.toml')
assert sha(config)==sha(BACKUP/config.name)
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==143
runs={}
for arm in ['slim','wide']:
    log=ROOT/f'preview/smoke-can-{arm}-1.8.39.log';s=log.read_text(encoding='utf-8-sig')
    assert 'CATALOG_SMOKE_COMPLETE 1' in s and 'BUILD SUCCESSFUL' in s and 'SILENT_CLIENT' in s
    assert 'CATALOG_SMOKE FAIL' not in s and 'DRINK_SMOKE FAIL' not in s
    runs[arm]=dict(path=str(log),sha256=sha(log),profiles=1,hidden=True,shaders=False)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
folder=ROOT/'blockbench/纸鸢酒水动画-v69'
projects=read(folder/'导出检查.json');fit=read(ROOT/'preview/bb-fit-v69.json')
assert len(projects)==len(fit)==2
archive=folder.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    assert sum(n.endswith('.bbmodel') for n in z.namelist())==2
    for p in projects:
        path=Path(p['path']);assert p['item']==can
        p['sha256']=sha(path);assert z.read(path.name)==path.read_bytes()
        assert len(read(path)['animations'])==13
visual=read(ROOT/'preview/can-review-1.8.39.json')
for entry in visual['sheets']:assert sha(Path(entry['path']))==entry['sha256']
report=dict(version='1.8.39',sha256=sha(jar),unit_tests=count,profiles=profiles,
    removed_profiles=sorted(removed),blockbench=projects,blockbench_fit=fit,
    blockbench_archives=[dict(path=str(archive),sha256=sha(archive))],client_runs=runs,client_retested=True,
    visual_review=visual,soundVolume=.8,inspectionVolumeMultiplier=1.7,liquidGainMultiplier=.5,
    switch_ticks=dict(draw=10,stow=9),configuration_sha256=sha(config),changed_entries=sorted(changed),
    scope='Removed three coffee animation profiles; sakura wine retained. Guang S inner walls and liquid narrow at the original can shoulder. Original source mod untouched.',
    inherited_catalog_evidence=str(BACKUP/'verification.json'))
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print('Verified 1.8.39:',count,'tests; 222 supported profiles; can checked with both arm shapes;',sha(jar))
