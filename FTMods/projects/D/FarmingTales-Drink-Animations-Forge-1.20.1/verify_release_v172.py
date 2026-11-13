"""Verify the two liquid-event resource gains are exactly half of release 1.7.1."""
from pathlib import Path
import copy, hashlib, json, zipfile, xml.etree.ElementTree as ET

ROOT=Path(__file__).absolute().parent
BACKUP=ROOT/'backups/1.7.1-before-liquid-half'
def sha(path): return hashlib.sha256(Path(path).read_bytes()).hexdigest()
def read(path): return json.loads(Path(path).read_text(encoding='utf-8-sig'))

jar=ROOT/'build/libs/farmingtales_drink_anim-1.7.2.jar'
previous=read(BACKUP/'verification.json')
oldjar=BACKUP/'[农场物语-饮用与检视动画]farmingtales_drink_anim-1.7.1.jar'
assert sha(oldjar)==previous['sha256']
sounds_path='assets/farmingtales_drink_anim/sounds.json'
with zipfile.ZipFile(jar) as new, zipfile.ZipFile(oldjar) as old:
    names={n for n in new.namelist() if not n.endswith('/')}
    assert names=={n for n in old.namelist() if not n.endswith('/')}
    changed={n for n in names if new.read(n)!=old.read(n)}
    assert changed=={sounds_path,'META-INF/mods.toml'},changed
    assert new.read('META-INF/mods.toml')==old.read('META-INF/mods.toml').replace(b'1.7.1',b'1.7.2')
    expected=copy.deepcopy(json.loads(old.read(sounds_path)))
    for event in ('slosh','inspect_liquid'):
        for sample in expected[event]['sounds']: sample['volume']*=.5
    assert json.loads(new.read(sounds_path))==expected

tests=[ET.parse(p).getroot().attrib for p in (ROOT/'build/test-results/test').glob('TEST-*.xml')]
assert sum(int(t['tests']) for t in tests)==72
assert all(int(t['failures'])==0 and int(t['errors'])==0 for t in tests)
assert 'BUILD SUCCESSFUL' in (ROOT/'preview/build-1.7.2.log').read_text(encoding='utf-8-sig')
assert sha(BACKUP/'farmingtales_drink_anim-client.toml')==sha(ROOT.parents[2]/'config/farmingtales_drink_anim-client.toml')
report={k:previous[k] for k in ('profiles','blockbench','soundVolume','inspectionVolumeMultiplier','previous_interaction_regression')}
report.update(version='1.7.2',sha256=sha(jar),unit_tests=72,liquidGainMultiplier=.5,
    changed_jar_entries=sorted(changed),client_runs={},catalog_assertions=[],
    previous_client_runs={'version':'1.7.1','runs':previous['client_runs']},
    previous_verification=str(BACKUP/'verification.json'),
    scope='Resource-gain-only update. Slosh and inspection liquid events are exactly half of 1.7.1; every other sound definition, audio sample, class and model is byte-identical. Build and 72 existing Java tests passed. Client not relaunched for this gain-only change; the 81 client checks remain historical 1.7.1 results.')
(ROOT/'verification.json').write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print('Verified 1.7.2: both liquid events at 50%; all other sounds and compiled code unchanged; 72 tests passed.')
print('SHA256',report['sha256'])
