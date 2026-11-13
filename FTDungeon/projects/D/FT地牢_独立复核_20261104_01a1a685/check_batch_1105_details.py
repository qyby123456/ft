import json
from pathlib import Path
from PIL import Image
import audit
from review_batch_1105 import CASES,ROOT

def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def verify_cut(blocks,path,predicate):
    unused,derived=mapping(path)
    assert derived.keys()<=blocks.keys(),path
    for position,state in blocks.items():
        assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if predicate(*position) else state),(path,position)

results=[]
for group,image,label,expected,relative,candidate,preview in CASES:
    folder=ROOT/label
    source=audit.BASE/relative
    template,blocks=mapping(folder/'room.nbt')
    assert audit.digest(source/candidate)==audit.digest(folder/'room.nbt')==expected
    metadata=read((source/candidate).with_suffix('.json'))
    routes=read(folder/'independent-routes.json')[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets']),(group,routes)
    native=read(folder/'independent-native-frozen.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS'
    assert not native['survival_failures'] and not native['survival_unsupported'],(group,native)
    for port in metadata.get('connections',metadata.get('connectors',[])):
        east,up,south=port.get('position',port.get('feet'))
        direction=port.get('facing',port.get('direction'))
        width,height=port.get('opening',[port.get('width'),port.get('height',port.get('minimum_height'))])
        assert width%2==1
        for offset in range(-(width//2),width//2+1):
            horizontal=(east+offset,south) if direction in {'north','south'} else (east,south+offset)
            assert blocks.get((horizontal[0],up-1,horizontal[1]),{'Name':'minecraft:air'})['Name'] not in audit.AIR,(group,port,'support')
            assert all(blocks.get((horizontal[0],up+rise,horizontal[1]),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(height)),(group,port,'clear')
    chests=[]
    for entry in template['blocks']:
        if template['palette'][entry['state']]['Name']=='minecraft:chest':
            assert 'nbt' in entry and not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable')
            east,up,south=entry['pos']
            assert blocks.get((east,up+1,south),{'Name':'minecraft:air'})['Name']=='minecraft:air'
            chests.append(entry['pos'])
    boundary=read(folder/'independent-boundaries.json')[0]
    assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    components=boundary['solids_not_connected_to_y0_by_six_face_adjacency']
    assert not components or (group==4 and all(item['materials']=={'minecraft:lily_pad':1} for item in components))
    cuts=0
    extra={}
    if group==2:
        registration=read(source/'registration.json')
        reference=Path(registration['reference'])
        assert audit.digest(reference)==registration['reference_sha256']
        comparison=Image.new('RGB',(1600,560))
        comparison.paste(audit.panel(reference,f'{reference.stem} REFERENCE'),(0,0))
        comparison.paste(audit.panel(source/preview,'ACTUAL NBT'),(800,0))
        comparison.save(folder/'S2-03-correct-reference-comparison.jpg')
        for item in read(source/'derivation.json')['derivatives']:
            direction=item['direction']
            predicate=lambda east,up,south,direction=direction: up>=2 and {'north':4<=south<=5,'south':south>=17,'west':east<=3 and south>=4,'east':east>=15 and south>=4}[direction]
            verify_cut(blocks,Path(item['derived']),predicate)
            cuts+=1
        extra['reference_correction']='S2-03 registered reference explicitly hashed; initial metadata fallback S2-01 comparison not used for verdict'
    if group==3:
        predicate=lambda east,up,south: up>=24 or (up>=17 and 4<east<30 and not(any(abs(east-center)<=3 for center in [9,25]) and any(abs(south-center)<=2 for center in [10,22,34,46]))) or (up>=3 and (south<2 or east>=33)) or (up>=15 and (east>=30 or south<4))
        verify_cut(blocks,source/'preview-only/preview-only-cutaway.nbt',predicate)
        cuts=1
        assert not any(state['Name'] in {'minecraft:snow','minecraft:ice'} for state in blocks.values())
    if group==4:
        unused,old=mapping(ROOT/'S4-03-independent/room.nbt')
        changes=[position for position in old.keys()|blocks.keys() if old.get(position)!=blocks.get(position)]
        assert len(changes)==140
        allowed={'minecraft:dark_oak_log','minecraft:mangrove_roots','minecraft:dark_oak_wood','minecraft:oak_log'}
        assert all(blocks[position]['Name'] in allowed for position in changes),{blocks[position]['Name'] for position in changes}
        assert all(old.get(position)==blocks.get(position) for position,state in old.items() if state['Name'] in {'minecraft:chest','minecraft:sea_lantern','minecraft:vine','minecraft:dark_oak_fence'})
        extra['root_only_changed_positions']=len(changes)
    if group==6:
        unused,derived=mapping(source/'lodestone/inspection-cutaway.nbt')
        assert derived=={position:state for position,state in blocks.items() if not(position[1]>=10 or ((position[0]==0 or position[2]==0) and position[1]>=3))}
        cuts=1
    if group==7:
        verify_cut(blocks,source/'views/PREVIEW_ONLY_cutaway.nbt',lambda east,up,south: up==19 or (south==34 and up>=2))
        cuts=1
    if group==8:
        for direction,cuboids in read(source/'qa/cut-rules.json').items():
            predicate=lambda east,up,south,cuboids=cuboids: any(all(first[index]<=coordinate<=last[index] for index,coordinate in enumerate([east,up,south])) for first,last in cuboids)
            verify_cut(blocks,source/f'views/{direction}/inspection-only-{direction}.nbt',predicate)
            cuts+=1
    roof_columns=None
    if group in {3,6,7,8}:
        width,height,depth=template['size']
        roof_columns=sum(any(blocks.get((east,up,south),{'Name':'minecraft:air'})['Name'] not in audit.AIR for up in range(height-3,height)) for east in range(width) for south in range(depth))
        assert roof_columns==width*depth,(group,roof_columns)
    result={'group':group,'image':image,'sha256':expected,'routes_including_duplicates':len(routes['targets']),'native_selected_checks':native['survival_checked'],'roof_columns':roof_columns,'empty_chests_lid_clear':chests,'verified_air_only_derivatives':cuts,'water_supported_lily_pad_components':len(components),**extra,'scope':'Static independent checks, not world tick or gameplay certification.'}
    (folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    results.append(result)
    print(json.dumps(result,ensure_ascii=True))
(ROOT/'batch-1105-details.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
