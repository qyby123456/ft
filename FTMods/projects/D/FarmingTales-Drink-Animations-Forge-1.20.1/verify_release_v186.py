from pathlib import Path
import hashlib,json,zipfile,xml.etree.ElementTree as ET

ROOT=Path(__file__).resolve().parent
ASSET='assets/farmingtales_drink_anim/'
def read(p):return json.loads(Path(p).read_text(encoding='utf-8-sig'))
def sha(p):return hashlib.sha256(Path(p).read_bytes()).hexdigest()
backup=ROOT/'backups/1.8.5-before-jam'
prior=backup/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.8.5.jar'
jar=ROOT/'build/libs/farmingtales_drink_anim-1.8.6.jar'
baseline=read(backup/'verification.json')
assert sha(prior)==baseline['sha256']
with zipfile.ZipFile(jar) as built,zipfile.ZipFile(prior) as old:
    profiles=json.loads(built.read(ASSET+'drink_profiles.json'))
    assert len(profiles)==54
    assert profiles[:-1]==json.loads(old.read(ASSET+'drink_profiles.json'))
    assert profiles[-1]['id']=='farmingtales:ancient_fruit_jam_1' and profiles[-1]['style']=='JAM'
    meta=built.read('META-INF/mods.toml').decode()
    assert 'version="1.8.6"' in meta and 'license="ARR"' in meta and 'authors="紫菜"' in meta
    allowed={ASSET+'drink_profiles.json',ASSET+'hand_volumes.json','assets/minecraft/atlases/blocks.json'}
    for name in old.namelist():
        if name.startswith('assets/') and name not in allowed:assert built.read(name)==old.read(name),name
    for file in (ROOT/'src/main/resources').rglob('*'):
        if file.is_file():assert built.read(file.relative_to(ROOT/'src/main/resources').as_posix())==file.read_bytes()
    assert not any('/smoke/' in n or n.startswith('assets/farmingtales/') for n in built.namelist())
for source in read(ROOT/'jam-source-assets.json'):assert sha(source['path'])==source['sha256']
tests=[ET.parse(p).getroot() for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t.get('failures'))==int(t.get('errors'))==int(t.get('skipped'))==0 for t in tests)
count=sum(int(t.get('tests')) for t in tests);assert count==96
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.8.6.log').read_text(encoding='utf-8-sig')
assert all(f.stat().st_mtime<=jar.stat().st_mtime+2 for f in (ROOT/'src/main').rglob('*') if f.is_file())
runs={}
for shape,model in [('slim','slim'),('wide','default')]:
    path=ROOT/f'preview/smoke-jam-{shape}-1.8.6.log';log=path.read_text(encoding='utf-8-sig')
    assert 'BUILD SUCCESSFUL' in log and 'CATALOG_SMOKE_COMPLETE 1' in log
    assert 'CATALOG_SMOKE FAIL' not in log and 'player arm renderer model='+model in log
    assert 'SILENT_CLIENT: GLFW create/show/focus suppressed before first window' in log
    checks=[line.split('CATALOG_SMOKE PASS ',1)[1] for line in log.splitlines() if 'CATALOG_SMOKE PASS ' in line]
    for expected in ['liquid atlas sprite loaded farmingtales_drink_anim:jam/ancient_fruit_jam_content','drink consumes once','inspect preserves all three items','ordinary right click keeps vanilla animation','stow finishes with empty hand','four drinking sounds']:
        assert any(expected in line for line in checks),(shape,expected)
    runs[shape]=dict(log=str(path),sha256=sha(path),assertions=checks)
assert 'enableShaders=false' in (ROOT/'run-smoke/config/oculus.properties').read_text()
config=ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml'
assert sha(config)==sha(backup/'farmingtales_drink_anim-client.toml')
folder=ROOT/'blockbench/上古水果果酱-v35';bb=read(folder/'导出检查.json')
fit=read(ROOT/'preview/bb-fit-v35.json');assert len(bb)==len(fit)==2
for entry in bb:
    data=read(entry['path']);assert len(data['animations'])==10
    assert any(e.get('type')=='camera' for e in data['elements'])
    assert all(any(t.get('type')=='camera' for t in a['animators'].values()) for a in data['animations'])
for entry in fit:
    assert entry['max_position_error_pixels']<=.06001 and entry['max_rotation_error_degrees']<.9 and entry['max_camera_error_degrees']<.3
archive=ROOT/'blockbench/上古水果果酱-v35.zip'
with zipfile.ZipFile(archive) as z:
    assert len([n for n in z.namelist() if n.endswith('.bbmodel')])==2
    for f in folder.iterdir():
        if f.is_file():assert z.read(f.name)==f.read_bytes()
report=dict(version='1.8.6',sha256=sha(jar),unit_tests=count,profiles=profiles,aged_aliases=baseline['aged_aliases'],
    blockbench=bb,blockbench_fit=fit,blockbench_archive=str(archive),blockbench_sha256=sha(archive),
    client_runs=runs,client_retested=True,soundVolume=.80,inspectionVolumeMultiplier=1.70,liquidGainMultiplier=.5,
    configuration_sha256=sha(config),scope='Adds ancient fruit jam: corner-pried wooden stopper, coupled hand grip, left/right/base inspection, mild camera, viscous delayed content response and gradual consumption. 96 unit tests; hidden isolated Forge client on slim and Steve arms with shaders disabled; half-second visual review. Two v35 Blockbench projects. Existing 53 profiles and 72 aged variants retained. Full modpack and user worlds not launched.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.8.6: 96 tests, two isolated client runs, two Blockbench projects, original source assets intact.')
