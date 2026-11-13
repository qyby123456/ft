"""Check packaged resources, runtime evidence, exports, and untouched pack files."""
from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET
ROOT=Path(__file__).absolute().parent
VERSION='1.8.2'
BACKUP=ROOT/'backups/1.8.1-before-surface-fix'
PREFIX='assets/farmingtales_drink_anim/'
def read(p):return json.loads(Path(p).read_text(encoding='utf-8-sig'))
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()
jar=ROOT/f'build/libs/farmingtales_drink_anim-{VERSION}.jar'
oldjar=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.1.jar'
assert sha(oldjar)==read(BACKUP/'verification.json')['sha256']
with zipfile.ZipFile(jar) as new,zipfile.ZipFile(oldjar) as old:
    names=set(new.namelist())
    profiles=json.loads(new.read(PREFIX+'drink_profiles.json'))
    decorations=json.loads(new.read(PREFIX+'decorations.json'))
    assert len(profiles)==53 and sum(p['style']=='CUP' for p in profiles)==29
    assert VERSION in new.read('META-INF/mods.toml').decode()
    assert 'DrinkPlacementMixin' in new.read('farmingtales_drink_anim.mixins.json').decode()
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in names)
    old_assets=[n for n in old.namelist() if n.startswith('assets/') and not n.endswith('/')]
    assert all(new.read(n)==old.read(n) for n in old_assets),'Existing models, textures, audio or catalog changed'
    sprites={r['texture'].replace(':textures/',':').removesuffix('.png') for p in profiles for r in p['liquid']}
    sprites.add('farmingtales_drink_anim:orange_wine')
    atlas=json.loads(new.read('assets/minecraft/atlases/blocks.json'))
    assert sprites<={s['resource'] for s in atlas['sources']}
    assert all(s['type'] in ['single','minecraft:single'] for s in atlas['sources'])
    for s in sprites:assert 'assets/'+s.replace(':','/textures/')+'.png' in names,s
    for f in (ROOT/'src/main/resources').rglob('*'):
        if f.is_file():assert new.read(f.relative_to(ROOT/'src/main/resources').as_posix())==f.read_bytes()
    changed=sorted(n for n in old.namelist() if n in names and not n.endswith('/') and new.read(n)!=old.read(n))
sources=read(ROOT/'cup-profile-sources.json')
assert all(sha(s['path'])==s['sha256'] for s in sources),'Original pack assets changed'
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(BACKUP/'farmingtales_drink_anim-client.toml')
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
tests=[ET.parse(p).getroot().attrib for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t['failures'])==int(t['errors'])==0 for t in tests)
test_count=sum(int(t['tests']) for t in tests)
assert test_count>=86
assert 'BUILD SUCCESSFUL' in (ROOT/f'preview/build-{VERSION}.log').read_text(encoding='utf-8-sig')
assert all(f.stat().st_mtime<=jar.stat().st_mtime+2 for f in (ROOT/'src/main').rglob('*') if f.is_file())
selections={
 'slim':'orange_wine,mysticfruit_wine,cherry_liqueur,purified_water,orange_juice_1,pumpkin_wine_cup,red_grape_juice_1,blueberry_juice_1,sweet_berry_juice_1,white_grape_juice_1,coconut_wine_cup_1,mysticfruit_wine_cup_1,rum_cup_1,brandy_xo,light_beer_cup_1,wheatbeer_cup_1',
 'wide':'orange_wine,red_grape_juice_1,orange_juice_1,pumpkin_wine_cup,rum_cup_1,brandy_xo,light_beer_cup_1'}
runs={}
for name,items in selections.items():
    expected={'farmingtales:'+n for n in items.split(',')}
    log=ROOT/f'preview/smoke-{name}-{VERSION}.log';content=log.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in content and f'CATALOG_SMOKE_COMPLETE {len(expected)}' in content,name
    assert all(f'{tag} FAIL' not in content for tag in ['CATALOG_SMOKE','DRINK_SMOKE','SWITCH_SMOKE']),name
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in content
    assert 'PASS player arm renderer model='+('slim' if name=='slim' else 'default') in content
    lines=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in content.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    assert {s.split(' ')[0] for s in lines}==expected
    for item in expected:
        for label in ['drink consumes once','four drinking sounds','ordinary right click keeps vanilla animation','stow finishes with empty hand','liquid atlas sprite loaded','body neck and decoration textures are present']:
            assert any(s.startswith(item+' '+label) for s in lines),(name,item,label)
    for label in ['successful placement uses vanilla swing without replacement draw','failed placement does not animate or consume a cup','rapid placement keeps the remaining cup without a replacement draw','placing last cup releases it and returns to empty hand']:
        assert any(label in s for s in lines),(name,label)
    runs[name]=dict(log=str(log),sha256=sha(log),items=sorted(expected),assertions=lines)
shader_runs={}
shaderpack=next((ROOT/'run-smoke/shaderpacks').glob('*.zip'))
assert sha(shaderpack)==sha(ROOT.parents[2]/'shaderpacks'/shaderpack.name)
def properties(path):
    return dict(line.split('=',1) for line in Path(path).read_text(encoding='utf-8-sig').splitlines() if line and not line.startswith('#') and '=' in line)
assert properties(str(shaderpack)+'.txt')==properties(ROOT.parents[2]/'shaderpacks'/(shaderpack.name+'.txt'))
for label,filename,count in [('catalog','smoke-shaders-1.8.2.log',3),('switch','smoke-shader-switch-1.8.2.log',1)]:
    log=ROOT/'preview'/filename;content=log.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in content and f'CATALOG_SMOKE_COMPLETE {count}' in content
    assert 'CATALOG_SMOKE FAIL' not in content and 'requested shader pack is active' in content
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in content
    assert 'ComplementaryReimagined_r5.9 + EuphoriaPatches_1.10.0.zip' in content
    if label=='switch':
        for message in ['shader switch retains the old cup until it is stowed','shader switch draws and settles the next cup','shader switch returns to the original cup']:
            assert 'farmingtales:white_grape_juice_1 '+message in content,message
    shader_runs[label]=dict(log=str(log),sha256=sha(log),items=count)
color_check=read(ROOT/'preview/review-v32/shader-color-check.json')
assert color_check['max_channel_change']<3,'Cup color changes after a round trip switch'
folder=ROOT/'blockbench/酒水扩展-v32'
bb=read(folder/'导出检查.json');fit=read(ROOT/'preview/bb-fit-v32.json')
assert len(bb)==len(fit)==106
for entry in bb:
    model=read(entry['path'])
    assert entry['animations']==len(model['animations'])==10
    assert any(e.get('type')=='camera' for e in model['elements'])
    assert all(t.get('source','').startswith('data:image/png;base64,') for t in model['textures'])
    for a in model['animations']:assert any(t.get('type')=='camera' for t in a['animators'].values())
    entry['sha256']=sha(entry['path'])
for f in fit:assert f['max_position_error_pixels']<=.06001 and f['max_rotation_error_degrees']<.9 and f['max_camera_error_degrees']<.3
archive=ROOT/'blockbench/酒水扩展-v32.zip'
with zipfile.ZipFile(archive) as z:
    files=[p for p in folder.iterdir() if p.is_file()]
    assert set(z.namelist())=={p.name for p in files}
    for f in files:assert z.read(f.name)==f.read_bytes()
boards=[Path(p) for p in read(ROOT/'preview/review-v32/boards.json')]
assert boards and all(p.is_file() for p in boards)
report=dict(version=VERSION,sha256=sha(jar),profiles=profiles,decorations=decorations,unit_tests=test_count,client_runs=runs,shader_runs=shader_runs,
 blockbench=bb,blockbench_archive=str(archive),blockbench_sha256=sha(archive),blockbench_fit=fit,
 shaderpack=dict(name=shaderpack.name,sha256=sha(shaderpack),settings=properties(str(shaderpack)+'.txt')),shader_color_check=color_check,
 soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
 unchanged_asset_entries=len(old_assets),original_pack_assets_checked=len(sources),configuration_sha256=sha(config),
 changed_jar_entries=changed,review_boards=[dict(path=str(p),sha256=sha(p)) for p in boards],
 scope='53 profiles; lower cup drinking, faster wine raise, short placement without replacement draw, culled decorative faces, bounded cup liquid with shared transparent liquid/ice sorting. 16 slim and 7 Steve profiles in isolated hidden Forge clients with Embeddium/Oculus (shaders disabled) and Cookery/Grilling/Immersive Eating. Additional Complementary Reimagined r5.9 + Euphoria 1.10.0 tests on 3 cups and a direct white/red grape cup switching scenario, using Oculus-managed translucent batching. Full modpack and user worlds not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified',VERSION,':',test_count,'tests,',sum(len(r['assertions']) for r in runs.values()),'client assertions, 106 projects.')
print('SHA256',report['sha256'])
