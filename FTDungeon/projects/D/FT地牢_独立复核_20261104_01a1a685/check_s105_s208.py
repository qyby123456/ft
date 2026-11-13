import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
for group,label in [(1,'S1-05-independent'),(2,'S2-08-independent')]:
    folder=root/label
    record=read(folder/'file-audit.json')['groups'][0]
    source=Path(record['candidate'])
    template,blocks=mapping(folder/'room.nbt')
    assert audit.digest(source)==audit.digest(folder/'room.nbt')
    native=read(folder/'independent-native-frozen.json')[0]
    routes=read(folder/'independent-routes.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    boundary=read(folder/'independent-boundaries.json')[0]
    assert all(set(component['materials'])=={'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
    metadata=read(source.with_suffix('.json'))
    ports=metadata.get('ports',metadata.get('connections'))
    for port in ports:
        east,up,south=port.get('position',port.get('feet'))
        for offset in range(-(port['width']//2),port['width']//2+1):
            block_x,block_z=(east+offset,south) if port.get('side',port.get('facing')) in {'north','south'} else (east,south+offset)
            assert blocks[(block_x,up-1,block_z)]['Name'] not in audit.AIR
            assert all(blocks[(block_x,up+rise,block_z)]['Name']=='minecraft:air' for rise in range(port.get('height',port.get('clearHeight'))))
    assert not any('nbt' in entry for entry in template['blocks'])
    if group==1:
        assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
        assert len(metadata['route'])==185
        for east,up,south in metadata['route']:
            assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
            assert all(blocks[(east,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
        assert all(blocks[(east,11,south)]['Name'] not in audit.AIR for east in range(15) for south in range(37))
        features={'three_high_route_cells':185,'roof_columns':555,'no_containers_or_entities':True}
    else:
        base=source.parents[2]
        for item in read(base/'derivation.json')['derivatives']:
            direction=item['direction']
            unused,derived=mapping(Path(item['derived']))
            assert derived.keys()<=blocks.keys()
            for position,state in blocks.items():
                east,up,south=position
                near={'north':south<=4,'south':south>=33,'west':east<=2,'east':east>=30}[direction]
                cut=(up>=18 and(east<=5 or east>=27 or south>=29)) or(up>=3 and near)
                assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if cut else state)
        roof={(east,south) for east in range(33) for south in range(4,35) if east<=5 or east>=27 or south>=29}
        assert all(blocks[(east,19,south)]['Name'] not in audit.AIR for east,south in roof)
        assert all(blocks[(east,23,south)]['Name']=='minecraft:air' for east in range(7,26) for south in range(6,28))
        for step in range(8):
            for east in range(3,6):
                state=blocks[(east,3+step,6+step)]
                assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='south' and state['Properties']['half']=='bottom'
        water=[(position,state) for position,state in blocks.items() if state['Name']=='minecraft:water']
        assert len(water)==72
        for (east,up,south),state in water:
            assert 13<=east<=21 and 13<=south<=21
            if up==3:
                assert state['Properties']['level']=='0'
                assert blocks[(east,2,south)]['Name']=='minecraft:cut_sandstone'
                for delta in [(1,0),(-1,0),(0,1),(0,-1)]:
                    assert blocks[(east+delta[0],up,south+delta[1])]['Name'] not in {'minecraft:air','minecraft:cave_air','minecraft:void_air'}
            else:
                assert (east,up,south) in {(17,8,16),(17,8,17)}|{(17,height,17) for height in range(4,8)}
        assert blocks[(17,8,16)]=={'Name':'minecraft:water','Properties':{'level':'0'}}
        assert blocks[(17,8,17)]=={'Name':'minecraft:water','Properties':{'level':'1'}}
        assert all(blocks[(17,height,17)]=={'Name':'minecraft:water','Properties':{'level':'8'}} for height in range(4,8))
        assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==15
        assert len(boundary['fluid_edge_samples'])==15
        assert all(tuple(edge['position']) in {(17,8,17)}|{(17,height,17) for height in range(4,8)} and not edge['outside'] for edge in boundary['fluid_edge_samples'])
        leaves=[state for state in blocks.values() if state['Name']=='minecraft:oak_leaves']
        assert leaves and all(state['Properties']['persistent']=='true' for state in leaves)
        pads=[position for position,state in blocks.items() if state['Name']=='minecraft:lily_pad']
        assert len(pads)==4 and all(blocks[(east,up-1,south)]=={'Name':'minecraft:water','Properties':{'level':'0'}} for east,up,south in pads)
        features={'upper_stair_blocks':24,'covered_roof_columns':len(roof),'persistent_leaves':len(leaves),'source_water_and_fall_records':72,'intentional_falling_water_air_edges':15,'basin_side_and_bottom_contained':True,'legal_lily_pads':4,'air_only_cuts':4,'fluid_simulation':'NOT_RUN'}
    report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':record['sha256'],'native_selected':native['survival_checked'],'body_targets_including_duplicates':len(routes['targets']),'ports':len(ports),**features,'scope':'Offline native shapes and selected survival. World ticks, fluid propagation, natural light, actual interaction, gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(label,json.dumps(report))
