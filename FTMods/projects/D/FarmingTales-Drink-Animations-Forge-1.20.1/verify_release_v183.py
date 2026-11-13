"""Verify the 1.8.3 artifacts and its focused client regression evidence."""
from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
VERSION='1.8.3'
BACKUP=ROOT/'backups/1.8.2-before-juice-speed'
PREFIX='assets/farmingtales_drink_anim/'
def read(p):return json.loads(Path(p).read_text(encoding='utf-8-sig'))
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()
def source_path(p):
    return ROOT.parents[2]/p[3:] if p.startswith('Z:\\') else Path(p)

jar=ROOT/f'build/libs/farmingtales_drink_anim-{VERSION}.jar'
oldjar=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.2.jar'
assert sha(oldjar)==read(BACKUP/'verification.json')['sha256']
with zipfile.ZipFile(jar) as new,zipfile.ZipFile(oldjar) as old:
    names=set(new.namelist())
    profiles=json.loads(new.read(PREFIX+'drink_profiles.json'))
    decorations=json.loads(new.read(PREFIX+'decorations.json'))
    assert len(profiles)==53 and sum(p['style']=='CUP' for p in profiles)==29
    assert VERSION in new.read('META-INF/mods.toml').decode()
    assert 'com/farmingtales/drinkanim/client/ShaderHandPass.class' in names
    assert 'DrinkPlacementMixin' in new.read('farmingtales_drink_anim.mixins.json').decode()
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in names)
    old_assets={n for n in old.namelist() if n.startswith('assets/') and not n.endswith('/')}
    assert old_assets=={n for n in names if n.startswith('assets/') and not n.endswith('/')}
    assert all(new.read(n)==old.read(n) for n in old_assets),'Existing model, texture, audio or catalog changed'
    for f in (ROOT/'src/main/resources').rglob('*'):
        if f.is_file():assert new.read(f.relative_to(ROOT/'src/main/resources').as_posix())==f.read_bytes()
    changed=sorted(n for n in names if not n.endswith('/') and (n not in old.namelist() or new.read(n)!=old.read(n)))
sources=read(ROOT/'cup-profile-sources.json')
assert all(sha(source_path(s['path']))==s['sha256'] for s in sources),'Original pack assets changed'
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(BACKUP/'farmingtales_drink_anim-client.toml')
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
tests=[ET.parse(p).getroot().attrib for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t['failures'])==int(t['errors'])==0 for t in tests)
test_count=sum(int(t['tests']) for t in tests)
assert test_count==89
assert 'BUILD SUCCESSFUL' in (ROOT/f'preview/build-{VERSION}-final.log').read_text(encoding='utf-8-sig')
assert all(f.stat().st_mtime<=jar.stat().st_mtime+2 for f in (ROOT/'src/main').rglob('*') if f.is_file())

def check_run(filename,items,arm='slim',shader=False):
    log=ROOT/'preview'/filename
    content=log.read_text(encoding='utf-8-sig')
    expected={'farmingtales:'+n for n in items.split(',')}
    assert 'BUILD SUCCESSFUL' in content and f'CATALOG_SMOKE_COMPLETE {len(expected)}' in content,filename
    assert all(f'{tag} FAIL' not in content for tag in ['CATALOG_SMOKE','DRINK_SMOKE','SWITCH_SMOKE']),filename
    assert 'Unable to query the shader hand pass' not in content
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in content
    assert 'PASS player arm renderer model='+('slim' if arm=='slim' else 'default') in content
    lines=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in content.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    assert {s.split(' ')[0] for s in lines}==expected
    for item in expected:
        for label in ['drink consumes once','four drinking sounds','ordinary right click keeps vanilla animation','stow finishes with empty hand','liquid atlas sprite loaded','body neck and decoration textures are present']:
            assert any(s.startswith(item+' '+label) for s in lines),(filename,item,label)
        if shader:
            for label in ['requested shader pack is active','shader switch retains the old cup until it is stowed','shader switch draws and settles the next cup','shader switch returns to the original cup']:
                assert any(s.startswith(item+' '+label) for s in lines),(filename,item,label)
    return dict(log=str(log),sha256=sha(log),items=sorted(expected),assertions=lines)

runs={
    'slim':check_run('smoke-slim-1.8.3.log','orange_juice_1,red_grape_juice_1,apple_juice_1,sweet_orange_juice,orange_wine,brandy_xo'),
    'wide':check_run('smoke-wide-1.8.3.log','orange_juice_1,sweet_orange_juice','wide')}
for arm,run in runs.items():
    for item in run['items']:
        if item in ['farmingtales:orange_juice_1','farmingtales:red_grape_juice_1','farmingtales:apple_juice_1']:
            assert item+' cup reaches sipping within one second' in run['assertions'],(arm,item)
        if item=='farmingtales:sweet_orange_juice':
            assert item+' bottled juice raises faster after opening' in run['assertions'],arm
shader_runs={
    'cups':check_run('smoke-shaders-1.8.3.log','rum_cup_1,red_grape_juice_1,brandy_xo_cup_1',shader=True),
    'transparent_juice':check_run('smoke-shaders-juice-1.8.3.log','red_grape_juice_1,white_grape_juice_1',shader=True)}
shaderpack=next((ROOT/'run-smoke/shaderpacks').glob('*.zip'))
assert sha(shaderpack)==sha(ROOT.parents[2]/'shaderpacks'/shaderpack.name)
def properties(path):
    return dict(line.split('=',1) for line in Path(path).read_text(encoding='utf-8-sig').splitlines() if line and not line.startswith('#') and '=' in line)
assert properties(str(shaderpack)+'.txt')==properties(ROOT.parents[2]/'shaderpacks'/(shaderpack.name+'.txt'))
for item in ['rum_cup_1','brandy_xo_cup_1','red_grape_juice_1','white_grape_juice_1']:
    model=read(ROOT/f'src/smoke/resources/assets/farmingtales/models/block/{item}.json')
    assert model['render_type'] in ['translucent','minecraft:translucent']

folder=ROOT/'blockbench/酒水扩展-v33'
bb=read(folder/'导出检查.json');fit=read(ROOT/'preview/bb-fit-v33.json')
assert len(bb)==len(fit)==106
for entry in bb:
    model=read(entry['path'])
    assert entry['animations']==len(model['animations'])==10
    assert any(e.get('type')=='camera' for e in model['elements'])
    assert all(t.get('source','').startswith('data:image/png;base64,') for t in model['textures'])
    for a in model['animations']:assert any(t.get('type')=='camera' for t in a['animators'].values())
    entry['sha256']=sha(entry['path'])
for f in fit:assert f['max_position_error_pixels']<=.06001 and f['max_rotation_error_degrees']<.9 and f['max_camera_error_degrees']<.3
archive=ROOT/'blockbench/酒水扩展-v33.zip'
with zipfile.ZipFile(archive) as z:
    files=[p for p in folder.iterdir() if p.is_file()]
    assert set(z.namelist())=={p.name for p in files}
    for f in files:assert z.read(f.name)==f.read_bytes()
boards=[ROOT/'preview/review-v33'/n for n in ['shader-pass-comparison.jpg','shader-juice.jpg','juice-raise.jpg','liquid-pitch.jpg']]
assert all(p.is_file() for p in boards)
report=dict(version=VERSION,sha256=sha(jar),profiles=profiles,decorations=decorations,unit_tests=test_count,client_runs=runs,shader_runs=shader_runs,
    blockbench=bb,blockbench_archive=str(archive),blockbench_sha256=sha(archive),blockbench_fit=fit,
    shaderpack=dict(name=shaderpack.name,sha256=sha(shaderpack),settings=properties(str(shaderpack)+'.txt')),
    soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
    unchanged_asset_entries=len(old_assets),original_pack_assets_checked=len(sources),configuration_sha256=sha(config),
    changed_jar_entries=changed,review_boards=[dict(path=str(p),sha256=sha(p)) for p in boards],
    baseline_verification=dict(path=str(BACKUP/'verification.json'),sha256=sha(BACKUP/'verification.json')),
    scope='1.8.3 focused regression: faster juice raise, bottle slosh +45%, cup slosh +20% with headroom bounds, and one complete Oculus hand submission to avoid gray liquid occlusion. 89 unit tests, 106 Blockbench projects. Hidden isolated Forge clients: 6 slim and 2 Steve drinks without shaders; 3 cup shader checks plus 2 correctly translucent grape juice checks and roundtrip switches, using the original Complementary Reimagined r5.9 + Euphoria 1.10.0 pack/settings. Main model, texture and sound assets unchanged. Full modpack and user worlds not launched; broader 1.8.2 regression evidence retained separately.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified',VERSION,':',test_count,'tests,',sum(len(r['assertions']) for r in runs.values()),'client assertions,',sum(len(r['assertions']) for r in shader_runs.values()),'shader assertions, 106 projects.')
print('SHA256',report['sha256'])
