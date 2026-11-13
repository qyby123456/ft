from pathlib import Path
import copy,hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.8.28-before-inspect-restore'
ORIGINAL=ROOT/'backups/1.8.27-before-cup-surfaces'
read=lambda p:json.loads(p.read_text(encoding='utf-8-sig'))
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
current=read(BACKUP/'verification.json');original=read(ORIGINAL/'verification.json')
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.29.jar'
previous=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.28.jar'
old=ORIGINAL/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.27.jar'
assert sha(previous)==current['sha256'] and sha(old)==original['sha256']
with zipfile.ZipFile(jar) as n,zipfile.ZipFile(previous) as p,zipfile.ZipFile(old) as o:
    names=lambda z:{s for s in z.namelist() if not s.endswith('/')}
    assert names(n)==names(p)==names(o)
    changed={s for s in names(n) if n.read(s)!=p.read(s)}
    assert changed=={'META-INF/mods.toml','com/farmingtales/drinkanim/math/CupAnimations.class'},changed
    assert all(n.read(s)==o.read(s) for s in names(n) if s.endswith('.class'))
    meta=n.read('META-INF/mods.toml').decode()
    assert all(s in meta for s in ['version="1.8.29"','authors="紫菜"','license="ARR"','农场物语-饮用与检视动画'])
    for source in (ROOT/'src/main/resources').rglob('*'):
        if source.is_file():assert n.read(source.relative_to(ROOT/'src/main/resources').as_posix())==source.read_bytes()
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==125
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.29.log').read_text(encoding='utf-8-sig')
assert sha(Path('Z:/config/farmingtales_drink_anim-client.toml'))==sha(BACKUP/'farmingtales_drink_anim-client.toml')
for source in read(ROOT/'cocktail-sources.json')['jars']:assert sha(Path(source['path']))==source['sha256']
folder=ROOT/'blockbench/杯底修复-v57';fixes=read(folder/'导出检查.json')
assert len(fixes)==4
for e in fixes:
    bb=read(Path(e['path']))
    prior=next(p for p in original['blockbench'] if (p['item'],p['arm'])==(e['item'],e['arm']))
    approved=read(Path(prior['path']))
    assert bb['animations']==approved['animations'] and bb['textures']==approved['textures']
archive=folder.with_suffix('.zip')
with zipfile.ZipFile(archive) as z:
    for p in folder.iterdir():
        if p.is_file():assert p.read_bytes()==z.read(p.name)
replace={(e['item'],e['arm']):e for e in fixes}
report=copy.deepcopy(current)
report.update(version='1.8.29',sha256=sha(jar),unit_tests=count,
    blockbench=[replace.get((e['item'],e['arm']),e) for e in original['blockbench']],
    blockbench_fit=original['blockbench_fit'],
    blockbench_archives=original['blockbench_archives']+[dict(path=str(archive),sha256=sha(archive))],
    baseline_verification=dict(path=str(BACKUP/'verification.json'),sha256=sha(BACKUP/'verification.json')),
    original_motion_verification=dict(path=str(ORIGINAL/'verification.json'),sha256=sha(ORIGINAL/'verification.json')),
    changed_entries=sorted(changed),client_retested=False,client_runs={},
    scope='Restored all cup inspection motion exactly to 1.8.27; all runtime classes byte-identical to 1.8.27. '
    'All resources byte-identical to 1.8.28 except version metadata, retaining coconut bottom and jerk coplanar fixes. '
    '125 tests passed. Reuses prior hidden-client evidence; no new client launch. '
    'Blockbench restored to pre-v58 projects, with the four v57 corrected cup-bottom projects. '
    'Source mods and client configuration unchanged.')
assert len(report['blockbench'])==418
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.29: original 1.8.27 animation classes, retained 1.8.28 surfaces, 125 tests.')
