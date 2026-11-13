import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}

results=[]
for group,case in [(6,'S6-02-R2-independent'),(4,'S4-03-independent'),(7,'S7-03-independent')]:
    folder=ROOT/case
    record=json.loads((folder/'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
    source=Path(record['candidate'])
    template,blocks=mapping(folder/'room.nbt')
    assert audit.digest(source)==audit.digest(folder/'room.nbt')
    routes=json.loads((folder/'independent-routes.json').read_text(encoding='utf-8'))[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    boundary=json.loads((folder/'independent-boundaries.json').read_text(encoding='utf-8'))[0]
    assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    components=boundary['solids_not_connected_to_y0_by_six_face_adjacency']
    assert not components or (group==4 and all(item['materials']=={'minecraft:lily_pad':1} for item in components))
    metadata=json.loads(source.with_suffix('.json').read_text(encoding='utf-8'))
    for port in metadata.get('connections',metadata.get('connectors',[])):
        east,up,south=port.get('position',port.get('feet'))
        direction=port.get('facing',port.get('direction'))
        width,height=port.get('opening',[port.get('width'),port.get('height')])
        offsets=range(-(width//2),width//2+1) if width%2 else range(width)
        for offset in offsets:
            position=(east+offset,up,south) if direction in {'north','south'} else (east,up,south+offset)
            assert blocks.get((position[0],up-1,position[2]),{'Name':'minecraft:air'})['Name'] not in audit.AIR
            assert all(blocks.get((position[0],up+rise,position[2]),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(height))
    chests=[]
    for entry in template['blocks']:
        if template['palette'][entry['state']]['Name']=='minecraft:chest':
            assert 'nbt' in entry and not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable')
            east,up,south=entry['pos']
            assert blocks.get((east,up+1,south),{'Name':'minecraft:air'})['Name']=='minecraft:air'
            chests.append(entry['pos'])
    extra={}
    if group==6:
        unused,old=mapping(ROOT/'S6-02-independent/room.nbt')
        changes=[position for position in blocks.keys()|old.keys() if blocks.get(position)!=old.get(position)]
        assert len(changes)==37 and all(3<=east<=9 and 2<=up<=9 and 20<=south<=24 for east,up,south in changes)
        directory=source.parent
        unused,detail=mapping(directory/'lodestone/hearth-detail.nbt')
        expected={(east-3,up-2,south-20):state for (east,up,south),state in blocks.items() if 3<=east<=9 and 2<=up<=9 and 20<=south<=24}
        assert detail==expected
        unused,cut=mapping(directory/'lodestone/inspection-cutaway.nbt')
        expected={position:state for position,state in blocks.items() if not(position[1]>=12 or ((position[0]==0 or position[2]==0) and position[1]>=3))}
        assert cut==expected
        fuels=[position for position,state in blocks.items() if state['Name']=='minecraft:bookshelf']
        lava=[position for position,state in blocks.items() if state['Name']=='minecraft:lava']
        assert fuels and lava and not any(abs(fuel[0]-fluid[0])<=4 and abs(fuel[2]-fluid[2])<=4 and 0<=fuel[1]-fluid[1]<=4 for fuel in fuels for fluid in lava)
        extra={'hearth_only_changed_positions':len(changes),'detail_translation_and_cutaway':'EXACT','bookshelf_lava_conservative_buffer':'PASS'}
    if group==7:
        unused,cut=mapping(source.parents[2]/'views/PREVIEW_ONLY_cutaway.nbt')
        for position,state in blocks.items():
            east,up,south=position
            removed=up==12 or (up>=2 and (east==0 or south==16)) or (east==1 and (up>=9 or state['Name']=='minecraft:vine'))
            assert cut.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if removed else state),position
        assert all(blocks[(east,12,south)]['Name'] not in audit.AIR for east in range(17) for south in range(17))
        extra={'preview_air_only':'PASS','roof_columns':289}
    results.append({'group':group,'sha256':audit.digest(source),'route_targets_including_duplicates':len(routes['targets']),'ports':'PASS','empty_chests_lid_clear':chests,'floating_lily_pads':len(components),'fluid_side_bottom_air_edges':0,**extra,'scope':'Static independent review only; world ticks and interaction NOT_RUN.'})
(ROOT/'20261105-second-independent.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps(results,indent=2))
