"""Verify audio-only release against 1.7.0 and the sounds actually played in Forge."""
from pathlib import Path
import hashlib,json,math,struct,zipfile,xml.etree.ElementTree as ET
from prepare_audio_balance import ROOT,SOUNDS,measure

def read(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(Path(path).read_bytes()).hexdigest()

backup=ROOT/'backups/1.7.0-before-audio-balance'
previous=read(backup/'verification.json')
jar=ROOT/'build/libs/farmingtales_drink_anim-1.7.1.jar'
balance=read(ROOT/'preview/audio-balance-1.7.1.json')
prefix='assets/farmingtales_drink_anim/'
changed_events={'slosh','cork','cork_pull','inspect_liquid','inspect_settle','cap_twist','cap_release','can_open'}
with zipfile.ZipFile(jar) as current,zipfile.ZipFile(backup/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.7.0.jar') as old:
    assert 'version="1.7.1"' in current.read('META-INF/mods.toml').decode()
    a={n for n in current.namelist() if not n.endswith('/')};b={n for n in old.namelist() if not n.endswith('/')}
    assert not b-a
    added={prefix+'sounds/balanced/'+r['name']+'.ogg' for r in balance['samples']}
    assert a-b==added
    permitted={prefix+'sounds.json','META-INF/mods.toml','com/farmingtales/drinkanim/client/DrinkAudio.class'}
    for name in a&b:
        if name not in permitted:assert current.read(name)==old.read(name),name
        if name.endswith('.class'):assert struct.unpack('>H',current.read(name)[6:8])[0]==61
    events=json.loads(current.read(prefix+'sounds.json'));old_events=json.loads(old.read(prefix+'sounds.json'))
    assert set(events)==set(old_events)
    for event in events:
        if event not in changed_events:assert events[event]==old_events[event]
        else:
            assert events[event]['subtitle']==old_events[event]['subtitle']
            for sample in events[event]['sounds']:
                assert sample['volume']==1
                assert prefix+'sounds/'+sample['name'].split(':')[1]+'.ogg' in added
    for sample in balance['samples']:
        src=SOUNDS/sample['source'];dst=Path(sample['output'])
        assert sha(src)==sample['source_sha256'] and sha(dst)==sample['sha256']
        assert current.read(prefix+'sounds/balanced/'+sample['name']+'.ogg')==dst.read_bytes()
        actual=measure(dst)
        assert abs(actual['active_db']-balance['reference']['active_db'])<.25
        assert actual['peak_db']<-.4
        assert abs(actual['seconds']-sample['before']['seconds'])<.0001

tests=[ET.parse(p).getroot().attrib for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert all(int(t['failures'])==0 and int(t['errors'])==0 for t in tests)
assert sum(int(t['tests']) for t in tests)==72
logpath=ROOT/'preview/smoke-slim-1.7.1.log';log=logpath.read_text(encoding='utf-8-sig')
for marker in ['BUILD SUCCESSFUL','CATALOG_SMOKE_COMPLETE 5','SILENT_CLIENT: GLFW create/show/focus suppressed before first window','splash screen is disabled','Shaders are disabled']:assert marker in log
for forbidden in ['CATALOG_SMOKE FAIL','DRINK_SMOKE FAIL','SWITCH_SMOKE FAIL','Using shaderpack','Unable to play unknown soundEvent']:assert forbidden not in log,forbidden
known_vanilla_missing={'minecraft:item.goat_horn.play','minecraft:entity.goat.screaming.horn_break'}
for line in log.splitlines():
    if 'Missing sound for event:' in line:
        assert line.split('Missing sound for event:',1)[1].strip() in known_vanilla_missing,line
catalog=(ROOT/'preview/catalog-results-SlimSteve29-1.7.1.txt').read_text().splitlines()
assert len(catalog)==81 and all(s.startswith('PASS ') for s in catalog)
played=read(ROOT/'preview/audio-playback-1.7.1.json')
normalized={r['name']:r['after'] for r in balance['samples']}
reference=balance['reference']['active_db']+20*math.log10(.50*.65*.8)
effective=[]
for sample in played:
    if sample['event'] not in changed_events:continue
    assert sample['resource'].startswith('farmingtales_drink_anim:balanced/')
    key=sample['resource'].split('/')[-1];source=normalized[key]
    loudness=source['active_db']+20*math.log10(sample['volume'])
    delta=loudness-reference
    assert -3.5<delta<3.2,(sample,delta)
    assert sample['volume']<1 and source['peak_db']+20*math.log10(sample['volume'])<-8
    effective.append(dict(**sample,active_db=loudness,relative_to_drinking_db=delta))
assert {s['event'] for s in effective}==changed_events
assert all(.239<s['volume']<.282 for s in played if s['event']=='gulp')
assert any(s['event']=='cloth' and s['action']=='INSPECT' and abs(s['volume']-.02448)<1e-6 for s in played)
for event in ['inspect_lift','inspect_support','inspect_turn','inspect_return']:
    assert any(s['event']==event for s in played)
    assert all(s['volume']<.09 for s in played if s['event']==event)
for entry in previous['blockbench']:assert sha(entry['path'])==entry['sha256']
for entry in read(ROOT/'drink-profile-sources.json'):assert sha(entry['path'])==entry['sha256']
for entry in read(ROOT/'original-hashes.json'):
    path=Path(entry['Path'])
    if not path.exists():path=Path(str(path)+'.disabled')
    assert sha(path).upper()==entry['Hash']
assert (backup/'farmingtales_drink_anim-client.toml').read_bytes()==(ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml').read_bytes()
report=dict(previous)
report.update(version='1.7.1',sha256=sha(jar),soundVolume=.8,inspectionVolumeMultiplier=1.7,
    audio_balance=dict(samples=balance,reference_output_db=reference,actual_playback=effective,
        quiet_fabric_unchanged=True,drinking_unchanged=True),
    client_runs={'slim':dict(catalog=catalog,log=str(logpath),sha256=sha(logpath),window_hidden_before_creation=True,shaders=False)},
    previous_client_runs=dict(version='1.7.0',runs=previous['client_runs']),catalog_assertions=catalog,
    scope='Audio-only update. One hidden Forge client with shaders off tested orange wine, brandy, water, beer and can; 81 checks and actual sound gains/resources validated. The two movement-audio limits were explicitly played through Forge for audio coverage. Seventy-two Java tests passed. All animation classes, models, original samples and 48 v29 Blockbench projects byte-identical to 1.7.0. Arm visual checks retained from 1.7.0. Full modpack not tested. Loudness comparison uses active 50 ms RMS windows; perceived loudness varies by headphones and sound timbre.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.7.1: 72 tests, 81 client checks, all eight physical events balanced; fabric/drinking/animations unchanged.')
print('Physical loudness relative to drink:',round(min(s['relative_to_drinking_db'] for s in effective),2),'to',round(max(s['relative_to_drinking_db'] for s in effective),2),'dB')
print('SHA256',report['sha256'])
