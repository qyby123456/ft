from pathlib import Path
import copy, hashlib, json, zipfile, xml.etree.ElementTree as ET
from PIL import Image
from prepare_cocktail_profiles import SPECS

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.26-before-cocktails'
JAR=ROOT/'build/libs/farmingtales_drink_anim-1.8.27.jar'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
baseline=read(BACKUP/'verification.json')
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.26.jar'
assert sha(previous)==baseline['sha256']
prefix='assets/farmingtales_drink_anim/'
manifest=read(ROOT/'cocktail-sources.json')
assert {e['id'] for e in manifest['items']}==set(SPECS) and len(SPECS)==26
assert sum(len(e['moving_parts']) for e in manifest['items'])==69
for jar in manifest['jars']:assert sha(Path(jar['path']))==jar['sha256']
for source in manifest['assets']:
    with zipfile.ZipFile(source['jar']) as z:assert hashlib.sha256(z.read(source['entry'])).hexdigest()==source['sha256']
with zipfile.ZipFile(JAR) as new,zipfile.ZipFile(previous) as old:
    changed={n for n in old.namelist() if not n.endswith('/') and old.read(n)!=new.read(n)}
    allowed={'META-INF/mods.toml',prefix+'drink_profiles.json',prefix+'hand_volumes.json',prefix+'decorations.json','assets/minecraft/atlases/blocks.json'}
    assert all(n in allowed or n.startswith('com/farmingtales/drinkanim/math/DecorationMotion')
               or n=='com/farmingtales/drinkanim/client/BottleRenderer.class' for n in changed),changed
    profiles=json.loads(new.read(prefix+'drink_profiles.json'))
    assert len(profiles)==209 and [p for p in profiles if p['id'] not in SPECS]==baseline['profiles']
    assert sum(p['style']=='CUP' for p in profiles)==55
    for file in ['hand_volumes.json','decorations.json']:
        current=json.loads(new.read(prefix+file));prior=json.loads(old.read(prefix+file))
        assert {k:v for k,v in current.items() if k not in SPECS}==prior
    metadata=new.read('META-INF/mods.toml').decode()
    assert all(s in metadata for s in ['version="1.8.27"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    assert not any('/smoke/' in n or n.startswith(tuple('assets/'+ns+'/' for ns in ['farmingtales','beachparty','kaleidoscope_tavern','kaleidoscope_world_liquor'])) for n in new.namelist())
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert new.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()
    new_profiles={p['id']:p for p in profiles if p['id'] in SPECS}
    for item,spec in SPECS.items():
        profile=new_profiles[item];assert profile['style']=='CUP'
        assert profile['capModel']==profile['neckModel']=='animation/cups/empty'
        body=json.loads(new.read(prefix+'models/'+profile['bodyModel']+'.json'))
        body_indices={int(e['name'].split('_')[-1]) for e in body['elements']}
        detail=next(e for e in manifest['items'] if e['id']==item)
        assert not body_indices.intersection(detail['ground_decorations'])
        assert not body_indices.intersection(spec['remove'])
        for kind,indices in spec['groups']:assert not body_indices.intersection(indices)
        parts=json.loads(new.read(prefix+'decorations.json'))[item]
        assert len(parts)==len(spec['groups'])
        for part,(kind,indices) in zip(parts,spec['groups']):
            meshes=json.loads(new.read(prefix+'models/'+part['model']+'.json'))['elements']
            assert part['kind']==kind and part['layered']
            assert {int(e['name'].split('_')[-1]) for e in meshes}==set(indices)
        for r in profile['liquid']:
            path=ROOT/'src/main/resources/assets'/r['texture'].replace(':','/')
            assert Image.open(path).getchannel('A').getextrema()==(255,255)
    mystery=ROOT/'src/main/resources'/prefix/'textures/cocktails/kaleidoscope_tavern/mystery_cocktail_liquid.png'
    assert Image.open(mystery).size==(32,144)
    meta=read(Path(str(mystery)+'.mcmeta'))['animation']
    assert meta['height']==16 and meta['width']==32 and meta['frametime']==6 and meta['interpolate']
    assert json.loads(new.read(prefix+'liquid_tints.json'))['kaleidoscope_tavern:signature_cocktail']==1

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==125,count
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.27.log').read_text(encoding='utf-8-sig')
assert all(p.stat().st_mtime<=JAR.stat().st_mtime+2 for p in (ROOT/'src/main').rglob('*') if p.is_file())
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml';assert sha(config)==sha(BACKUP/config.name)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
runs={}
for name,items in [('slim',list(SPECS)),('duration', ['kaleidoscope_tavern:signature_cocktail','kaleidoscope_tavern:mystery_cocktail','beachparty:coconut_cocktail'])]:
    path=ROOT/f'preview/smoke-cocktails-{name}-1.8.27.log';log=path.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in log and f'CATALOG_SMOKE_COMPLETE {len(items)}' in log
    assert 'CATALOG_SMOKE FAIL' not in log
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
    checks=[s.split('CATALOG_SMOKE PASS ',1)[1] for s in log.splitlines() if 'CATALOG_SMOKE PASS ' in s]
    for item in items:
        selected=[s for s in checks if s.startswith(item+' ')]
        for expected in ['drink consumes once','inspect preserves all three items','ordinary right click keeps vanilla animation',
            'stow finishes with empty hand','cocktail liquid atlas loaded','cocktail mesh has painted faces']:
            assert any(expected in s for s in selected),(name,item,expected)
        if name=='duration':assert any('original 32-tick consumption duration' in s for s in selected)
    assert any('signature liquid reads each recipe color without caching' in s for s in checks)
    assert any('mystery cocktail retains all nine animated color frames' in s for s in checks)
    runs[name]=dict(log=str(path),sha256=sha(path),assertions=checks)

directory=ROOT/'blockbench/01-常用工程/鸡尾酒动画-v56';entries=read(directory/'导出检查.json')
assert len(entries)==52 and {e['item'] for e in entries}==set(SPECS)
fit=read(ROOT/'preview/bb-fit-v56.json');assert len(fit)==52 and all(e['max_position_error_pixels']<=.01501 for e in fit)
for entry in entries:
    model=read(Path(entry['path']));assert len(model['animations'])==13
    assert 'ARR' in model['credit'] and '1.8.27' in model['credit']
    clips={a['name'].split('.')[-1]:a for a in model['animations']}
    assert clips['draw']['length']==.5 and clips['put_away']['length']==.45
    assert clips['idle']['length']==4
    assert all(any(t.get('type')=='camera' for t in a['animators'].values()) for a in model['animations'])
    for part in next(i for i in manifest['items'] if i['id']==entry['item'])['moving_parts']:
        tracks=clips['inspect']['animators'].values()
        assert any(t['name']==part['name'] and len(t['keyframes'])>2 for t in tracks)
archive=directory.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    assert sum(n.endswith('.bbmodel') for n in z.namelist())==52
    for path in directory.iterdir():
        if path.is_file():assert z.read(path.name)==path.read_bytes()
for a in baseline['blockbench_archives']:assert sha(Path(a['path']))==a['sha256']
report=copy.deepcopy(baseline)
report.update(version='1.8.27',sha256=sha(JAR),unit_tests=count,profiles=profiles,
    blockbench=baseline['blockbench']+entries,blockbench_fit=baseline['blockbench_fit']+fit,
    blockbench_archives=baseline['blockbench_archives']+[dict(path=str(archive),sha256=sha(archive))],
    client_runs=runs,client_retested=True,cocktails=26,moving_cocktail_parts=69,
    baseline_verification=dict(path=str(BACKUP/'verification.json'),sha256=sha(BACKUP/'verification.json')),
    scope='26 cocktails; original glass, color providers and animated texture metadata retained, 69 inertial garnish groups, hand-only ground decals removed. '
    '125 tests; all 26 in a hidden Forge fixture client, plus three source-duration cases; shaders off. Source JARs unchanged. '
    '52 Blockbench files with camera and liquid/decoration motion, both arms, thirteen clips, validated against runtime samples. '
    'Blockbench textures preview the first frame and signature default blue; game retains live recipe color and animated sprites. '
    'Draw 0.5s, stow 0.45s and vanilla right-click behavior preserved. Full pack not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.27:',count,'tests; 26 cocktails, 69 moving parts, 52 Blockbench projects.')
