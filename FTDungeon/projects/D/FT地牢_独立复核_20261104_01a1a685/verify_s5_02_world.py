import json
import shutil
import sys
import xml.etree.ElementTree as ET
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/02-coral-study'
IMAGE=int(sys.argv[1]) if len(sys.argv)>1 else 2
assert IMAGE in {2,3,4,5,6,7,8,9,10}
if IMAGE==3:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/03-coral-lantern-cell'
if IMAGE==4:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/04-coral-processional-hall'
WORLD=SOURCE/('worldtest' if IMAGE==4 else 'worldtest-r2')
EXPECTED='29fb0a6261847b1817cc5edcbb72aa2824c7e2af1e527bb70d041a7fbd80d552' if IMAGE==2 else '8b037a9e1a1fa594b47ccfbb86f984f117fbd0885ac9cd370a046afe8a42c621'
SIZE=[27,19,25] if IMAGE==2 else [19,16,19]
if IMAGE==4:
    EXPECTED='7619ddcd241bca9e8a97753677904e3c0b8f71585c53b794522f2966facde793'
    SIZE=[35,20,43]
CASE=ROOT/(sys.argv[2] if len(sys.argv)>2 else f'S5-0{IMAGE}-independent')
if IMAGE==5:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/05-coral-ribbed-corridor'
    WORLD=SOURCE/'worldtest'
    EXPECTED='f85b42cc6d78b51d656c5829b46803cee6844903461a38b93fe23f290977e5cb'
    SIZE=[17,18,39]
CASE.mkdir(exist_ok=False)
if IMAGE==6:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/06-coral-lantern-corner'
    WORLD=SOURCE/'worldtest'
    EXPECTED='12c4d28d023b7e6e28ad27f631e222f50d2040bb325daa0b7433dbff7eac890c'
    SIZE=[31,18,31]
candidate=SOURCE/('nbt/s5-02-coral-study.nbt' if IMAGE==2 else 'nbt/s5-03-coral-lantern-cell.nbt')
if IMAGE==7:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/07-coral-gate-vestibule'
    WORLD=SOURCE/'worldtest'
    EXPECTED='b5c2cb4c96f3345ab93f4e609fd5aabc2b5a7061f58b6fbfad742eabe7e8fdf5'
    SIZE=[31,22,35]
    candidate=SOURCE/'nbt/s5-07-coral-gate-vestibule.nbt'
if IMAGE==8:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/08-coral-skylight-courtyard'
    WORLD=SOURCE/'worldtest-r2'
    EXPECTED='71c21f20e39462b0cbcb018c4c125d6348e13372ec0cc3a3045d7fabafaf7cea'
    SIZE=[35,22,35]
    candidate=SOURCE/'nbt/s5-08-coral-skylight-courtyard.nbt'
if IMAGE==9:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/09-coral-trident-vault'
    WORLD=SOURCE/'worldtest'
    EXPECTED='57bca6ec9a9cbaac058ef611a9cf518173cb4a6d0549fc3558182d2b38925c11'
    SIZE=[31,22,31]
    candidate=SOURCE/'nbt/s5-09-coral-trident-vault.nbt'
if IMAGE==4:
    candidate=SOURCE/'nbt/s5-04-coral-processional-hall.nbt'
if IMAGE==10:
    SOURCE=audit.BASE/'FT地牢_第5组逐图重做_20261104/10-coral-ocean-complex'
    WORLD=SOURCE/'worldtest'
    EXPECTED='1cb69d8ec260dd3a97da75b2b3a6db3f11e140da351e18bcec10c1c35dd7f92b'
    SIZE=[65,22,65]
    candidate=SOURCE/'nbt/s5-10-coral-ocean-complex.nbt'
if IMAGE==5:
    candidate=SOURCE/'nbt/s5-05-coral-ribbed-corridor.nbt'
if IMAGE==6:
    candidate=SOURCE/'nbt/s5-06-coral-lantern-corner.nbt'
assert audit.digest(candidate)==audit.digest(WORLD/'frozen.nbt')==EXPECTED
shutil.copyfile(candidate,CASE/'room.nbt')
shutil.copyfile(ROOT/'vanilla-block-tags.json',CASE/'vanilla-block-tags.json')
manifest=json.loads((SOURCE/'qa/SHA256.json').read_text(encoding='utf-8'))
if isinstance(manifest,dict):
    manifest=manifest['files']
for entry in manifest:
    assert audit.digest(SOURCE/entry['path']).lower()==entry['sha256'].lower(),entry['path']
template=audit.decode(CASE/'room.nbt')
assert template['DataVersion']==3465 and template['size']==SIZE
assert not template.get('entities')
assert all(state['Name'].startswith('minecraft:') for state in template['palette'])
assert all(0<=entry['state']<len(template['palette']) and all(0<=coordinate<template['size'][axis] for axis,coordinate in enumerate(entry['pos'])) for entry in template['blocks'])
def mapping(value):
    result={tuple(entry['pos']):{'state':value['palette'][entry['state']],'nbt':entry.get('nbt')} for entry in value['blocks']}
    assert len(result)==len(value['blocks'])
    return result
source=mapping(template)
assert len(source)==SIZE[0]*SIZE[1]*SIZE[2]
if IMAGE>=4:
    initial=json.loads((WORLD/'NONE/report.json').read_text(encoding='utf-8'))['initial_routes']
    coordinates=[coordinate for target in initial['targets'] for coordinate in target['source']]
else:
    port_data=json.loads((SOURCE/'qa/ports-and-routes.json').read_text(encoding='utf-8'))[0]
    coordinates=port_data['targets']
assert len(coordinates)==(81 if IMAGE==10 else (54 if IMAGE==8 else (42 if IMAGE>=4 else 30)))
start=[8,3,0] if IMAGE==5 else ([17,3,0] if IMAGE==4 else ([0,3,9] if IMAGE==2 else [0,3,8]))
if IMAGE==6:
    start=[0,3,24]
if IMAGE==7:
    start=[15,3,34]
if IMAGE==8:
    start=[0,3,18]
if IMAGE==9:
    start=[0,3,18]
if IMAGE==10:
    start=[36,3,64]
specification=[{'group':5,'candidate':str(CASE/'room.nbt'),'sha256':EXPECTED,'size':template['size'],'start':start,'swimming':True,'targets':[{'label':f'swim-target-{index//3}','position':coordinates[index:index+3]} for index in range(0,len(coordinates),3)]}]
if IMAGE in {7,8,9,10}:
    specification[0]['vertical_swimming']=True
(CASE/'route-input.json').write_text(json.dumps(specification,indent=2),encoding='utf-8')
assert json.loads((WORLD/'exit.json').read_text(encoding='utf-8-sig'))['exit_code']==0
xml=ET.parse(WORLD/'tests.xml')
assert len(xml.findall('.//testcase'))==4 and not xml.findall('.//failure') and not xml.findall('.//error')
cases=[]
facing_rotation={'north':'east','east':'south','south':'west','west':'north'}
for turn,name in enumerate(['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']):
    folder=WORLD/name
    report=json.loads((folder/'report.json').read_text(encoding='utf-8'))
    assert report['nbt_sha256']==EXPECTED and report['server_ticks']==1200 and report['random_tick_speed']==3
    assert report['knownShape'] is False and report['placement_flags']==3
    assert report['random_tick_probes']['lower_section_grown']>0 and report['random_tick_probes']['upper_section_grown']>0
    assert report['empty_chests']=={2:2,3:1,4:0,5:0,6:0,7:0,8:0,9:1,10:2}[IMAGE] and report['native_canSurvive_checked']==len(source)
    assert all(not report[key] for key in ['placement_expected_vs_actual','explicit_neighbor_update_diff','before_vs_1200_diff','after_update_vs_1200_diff'])
    assert all(report['settled'][key]==0 for key in ['dead_coral','coral_identity_changes','invalid_aquatic_survival','original_water_no_longer_source_water'])
    assert not report['settled']['dropped_items']
    captures=[audit.decode(folder/filename) for filename in ['before-explicit-updates.nbt','after-explicit-updates.nbt','after-1200-ticks.nbt']]
    capture_size=[81,30,81] if IMAGE==10 else ([59,30,59] if IMAGE>=4 else [51,30,51])
    capture_points=capture_size[0]*capture_size[1]*capture_size[2]
    assert all(value['size']==capture_size for value in captures)
    snapshots=[mapping(value) for value in captures]
    assert all(len(value)==capture_points for value in snapshots)
    assert snapshots[0]==snapshots[1]==snapshots[2]
    for position,entry in source.items():
        east,up,south=position
        width,depth=SIZE[0],SIZE[2]
        state=entry['state']
        properties=dict(state.get('Properties',{}))
        for unused in range(turn):
            east,south=depth-1-south,east
            width,depth=depth,width
            properties={facing_rotation.get(key,key):value for key,value in properties.items()}
            if properties.get('facing') in facing_rotation:
                properties['facing']=facing_rotation[properties['facing']]
            if properties.get('axis') in {'x','z'}:
                properties['axis']={'x':'z','z':'x'}[properties['axis']]
        actual=snapshots[0][(east+8,up,south+8)]
        assert actual['state']['Name']==state['Name'],(name,position,state,actual)
        assert all(actual['state'].get('Properties',{}).get(key)==value for key,value in properties.items()),(name,position,properties,actual)
        if entry['nbt']:
            assert actual['nbt']
            assert not actual['nbt'].get('Items') and not actual['nbt'].get('LootTable')
    cases.append({'rotation':name,'ticks':1200,'capture_points_each':capture_points,'full_capture_state_and_entity_diff':0,'source_to_placement_explicit_state_diff':0,'probes':[report['random_tick_probes']['lower_section_grown'],report['random_tick_probes']['upper_section_grown']]})
output={'group':5,'image':IMAGE,'sha256':EXPECTED,'manifest_entries_verified':len(manifest),'cases':cases,'scope':'Independent read-only full re-decoding and source rotation mapping of producer-executed isolated Forge test. Not another server run, formal integration or unlimited-time guarantee.'}
(CASE/'world-independent.json').write_text(json.dumps(output,indent=2),encoding='utf-8')
print(json.dumps(output,indent=2))
