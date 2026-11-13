"""Verify the pineapple-first coconut release and both independent client replays."""
from pathlib import Path
import hashlib,json,zipfile,tomllib,csv
import xml.etree.ElementTree as ET
from PIL import Image
ROOT=Path('\\\\?\\'+str(Path(__file__).resolve()).removeprefix('\\\\?\\')).parent
BACKUP=ROOT/'backups/1.8.73-before-pineapple-first'
JAR=ROOT/'build/libs/farmingtales_drink_anim-1.8.74.jar'
base=json.loads((BACKUP/'baseline.json').read_text(encoding='utf-8'))
previous=BACKUP/Path(base['installed_path']).name
sha=lambda p:hashlib.sha256(p.read_bytes()).hexdigest()
assert sha(previous)==base['previous_sha256']
ns='assets/farmingtales_drink_anim/'
key='papercraft_magic_decoration:ferry_whiskey'
with zipfile.ZipFile(previous) as old,zipfile.ZipFile(JAR) as new:
    changed={n for n in old.namelist() if old.read(n)!=new.read(n)}
    expected={'META-INF/mods.toml'}|{ns+n for n in ('decorations.json','drink_profiles.json','servings.json','sounds.json','lang/en_us.json','lang/zh_cn.json')}|{'com/farmingtales/drinkanim/math/'+n+'.class' for n in ('AnimationPlayback','BottleFraming','BottleRig','CoconutAnimations','DecorationMotion','DecorationMotion$State','DrinkMotion','ServingAnimations')}
    assert changed==expected,changed^expected
    added=set(new.namelist())-set(old.namelist())
    assert added=={'com/farmingtales/drinkanim/math/PineappleMotion.class',ns+'models/animation/papercraft/ferry_whiskey_serving.json'}
    oldmeta=tomllib.loads(old.read('META-INF/mods.toml').decode());meta=tomllib.loads(new.read('META-INF/mods.toml').decode())
    assert meta['mods'][0]['version']=='1.8.74';meta['mods'][0]['version']=oldmeta['mods'][0]['version'];assert meta==oldmeta
    for name in ('decorations','servings'):
        a=json.loads(old.read(ns+name+'.json'));b=json.loads(new.read(ns+name+'.json'));a.pop(key,None);b.pop(key,None);assert a==b
    a=json.loads(old.read(ns+'drink_profiles.json'));b=json.loads(new.read(ns+'drink_profiles.json'))
    assert [p for p in a if p['id']!=key]==[p for p in b if p['id']!=key]
    for name in ('sounds.json','lang/en_us.json','lang/zh_cn.json'):
        a=json.loads(old.read(ns+name));b=json.loads(new.read(ns+name));b.pop('pineapple_bite' if name=='sounds.json' else 'subtitles.farmingtales_drink_anim.pineapple_bite');assert a==b
    for p in (ROOT/'src/main/resources').rglob('*'):
        if p.is_file():assert new.read(p.relative_to(ROOT/'src/main/resources').as_posix())==p.read_bytes(),p
    shell=json.loads(new.read(ns+'models/animation/papercraft/ferry_whiskey.json'))
    assert len([e for e in shell['elements'] if 'coconut' in e.get('name','')])>=2

tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert tests and all(int(t.get(k))==0 for t in tests for k in ('failures','errors','skipped'))
count=sum(int(t.get('tests')) for t in tests);assert count==201,count
logs=[];motion={}
for hand,user in [('slim','SlimSteve29'),('left','CoconutLeft')]:
    logpath=ROOT/f'preview/verify-pineapple-{hand}-1.8.74.log';raw=logpath.read_bytes()
    log=raw.decode('utf-16' if raw.startswith((b'\xff\xfe',b'\xfe\xff')) else 'utf-8-sig',errors='replace')
    assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 1' in log and 'SMOKE FAIL' not in log
    for label in ('body neck and decoration textures are present','inspect preserves all three items','drink consumes once and returns to idle','pineapple eaten before drinking','stow finishes with empty hand'):
        assert 'CATALOG_SMOKE PASS '+key+' '+label in log,label
    logs.append(str(logpath))
    rows=list(csv.DictReader((ROOT/f'run-smoke/coconut-frames-{user}.csv').open()))
    assert len(rows)>100
    assert all(float(b['tick'])>=float(a['tick']) for a,b in zip(rows,rows[1:]))
    assert any(r['fruitVisible']=='true' and int(r['fruitStage'])==0 and float(r['tick'])<14 for r in rows)
    assert all(r['fruitVisible']=='false' for r in rows if float(r['tick'])>=14)
    assert all(abs(float(r['fill'])-.8)<1e-6 for r in rows if float(r['tick'])<=48)
    sip=[r for r in rows if float(r['tick'])>=48]
    assert float(sip[-1]['tilt'])-float(sip[0]['tilt'])>19
    def extent(name):return max(float(r[name]) for r in sip)-min(float(r[name]) for r in sip)
    assert extent('cameraPitch')>.8 and extent('handY')+extent('handZ')>.025
    captures=[]
    for r in rows:
        if int(r['screenshot'])<0:continue
        p=ROOT/f"run-smoke/screenshots/coconut-v1874-{user}-{int(r['screenshot']):04}.png"
        assert p.exists();captures.append(p)
    assert len(captures)>70 and len({sha(p) for p in captures})==len(captures)
    motion[user]=dict(render_frames=len(rows),captures=len(captures),tilt=[float(sip[0]['tilt']),float(sip[-1]['tilt'])],camera_pitch_range=extent('cameraPitch'))
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==base['configuration_sha256']
report=dict(version='1.8.74',sha256=sha(JAR),previous_sha256=sha(previous),configuration_sha256=sha(config),unit_tests=count,changed_jar_entries=sorted(changed),added_jar_entries=sorted(added),client_logs=logs,rendered_motion=motion,
    scope='Eat the original pineapple garnish in one bite with the free hand before drinking directly from the coconut rim, with progressive tilt and wrist/camera response. Straw remains an inertial decoration. Preserve coconut flesh interior, source textures, inspection, user metadata, configuration and unrelated drinks. Verified in both hands in isolated Forge clients; full modpack shaders were not replayed.')
(ROOT/'verification-1.8.74.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(f'Verified 1.8.74: {count} tests; pineapple first, progressive drinking and consumed exactly once in both hands.')
print(json.dumps(motion,indent=2))
