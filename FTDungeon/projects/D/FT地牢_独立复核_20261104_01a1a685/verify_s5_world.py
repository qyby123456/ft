import json
import xml.etree.ElementTree as ET
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
WORLD=ROOT/'S5-01-worldtest-R4-20261104'
EXPECTED='9450884a348eda61ea55f0f11a8dae87a0f7d5183312b1528d9a519449f6b9a2'
assert audit.digest(WORLD/'frozen.nbt')==EXPECTED
manifest=json.loads((WORLD/'SHA256.json').read_text(encoding='utf-8'))
for entry in manifest:
    assert audit.digest(WORLD/entry['path']).lower()==entry['sha256'].lower(),entry['path']
assert json.loads((WORLD/'exit.json').read_text(encoding='utf-8'))['exit_code']==0
xml=ET.parse(WORLD/'tests.xml')
assert len(xml.findall('.//testcase'))==4
assert not xml.findall('.//failure') and not xml.findall('.//error')

def mapping(template):
    return {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}

source=mapping(audit.decode(WORLD/'frozen.nbt'))
cases=[]
for turn,name in enumerate(['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']):
    folder=WORLD/name
    report=json.loads((folder/'report.json').read_text(encoding='utf-8'))
    assert report['nbt_sha256']==EXPECTED and report['server_ticks']==1200
    assert report['random_tick_speed']==3 and report['knownShape'] is False and report['placement_flags']==3
    assert report['random_tick_probes']['lower_section_grown']>0 and report['random_tick_probes']['upper_section_grown']>0
    captures=[audit.decode(folder/file) for file in ['before-explicit-updates.nbt','after-explicit-updates.nbt','after-1200-ticks.nbt']]
    assert all(capture['size']==[51,30,51] for capture in captures)
    snapshots=[mapping(capture) for capture in captures]
    assert all(len(snapshot)==51*30*51 for snapshot in snapshots)
    assert snapshots[0]==snapshots[1]==snapshots[2]
    adaptations=[]
    for position,state in source.items():
        east,up,south=position
        properties=dict(state.get('Properties',{}))
        for unused in range(turn):
            east,south=34-south,east
            if properties.get('facing') in ['north','east','south','west']:
                properties['facing']={'north':'east','east':'south','south':'west','west':'north'}[properties['facing']]
        actual=snapshots[0][(east+8,up,south+8)]
        assert actual['Name']==state['Name'],(name,position,state,actual)
        differences={key:(value,actual.get('Properties',{}).get(key)) for key,value in properties.items() if actual.get('Properties',{}).get(key)!=value}
        if differences:
            assert state['Name']=='minecraft:prismarine_brick_stairs' and differences=={'waterlogged':('false','true')},(position,differences)
            adaptations.append(position)
    assert len(adaptations)==14
    assert len(report['placement_expected_vs_actual'])==14
    assert report['settled']['dead_coral']==0 and not report['settled']['dropped_items']
    assert report['settled']['invalid_aquatic_survival']==0 and report['settled']['original_water_no_longer_source_water']==0
    assert not report['explicit_neighbor_update_diff'] and not report['before_vs_1200_diff']
    cases.append({'rotation':name,'ticks':1200,'decoded_world_blocks_per_capture':51*30*51,'independent_world_capture_differences':0,'independent_source_placement_adaptations':len(adaptations),'probe_lower_grown':report['random_tick_probes']['lower_section_grown'],'probe_upper_grown':report['random_tick_probes']['upper_section_grown']})
output={'group':5,'sha256':EXPECTED,'evidence_manifest_verified':len(manifest),'junit_passed':4,'cases':cases,'scope':'Independent read-only re-decoding of producer-executed isolated Forge world captures and source placement; not an independent second server run, full modpack test, or unbounded-time guarantee.'}
(ROOT/'S5-01-world-independent.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
