import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def decode(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def entities(template):
    return {tuple(entry['pos']):entry['nbt'] for entry in template['blocks'] if 'nbt' in entry}
sources={2:audit.BASE/'FT地牢_第2组重做B_20261104/S2-07',6:audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/07-entrance'}
for group,source in sources.items():
    folder=root/f'S{group}-07-independent'
    template,blocks=decode(folder/'room.nbt')
    candidate=source/('incoming/G2B-07/room.nbt' if group==2 else 'room.nbt')
    assert audit.digest(candidate)==audit.digest(folder/'room.nbt')
    native=read(folder/'independent-native-frozen.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS'
    assert not native['survival_failures'] and not native['survival_unsupported']
    boundary=read(folder/'independent-boundaries.json')[0]
    assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency']
    assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    closed=read(folder/'independent-routes.json')[0]
    routes=read(folder/'open-door-analysis/independent-routes.json')[0] if group==2 else closed
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    metadata=read(candidate.with_suffix('.json'))
    ports=metadata.get('connections',metadata.get('connectors'))
    for port in ports:
        east,up,south=port['position']
        first,last=port.get('lateral_x_inclusive',[east-port['width']//2,east+port['width']//2])
        assert last-first+1==port['width']
        for block_x in range(first,last+1):
            assert blocks[(block_x,up-1,south)]['Name'] not in audit.AIR
            assert all(blocks[(block_x,up+rise,south)]['Name']=='minecraft:air' for rise in range(port['height']))
    doors=[(position,state) for position,state in blocks.items() if state['Name'].endswith('_door')]
    assert len(doors)==4 and all(state['Properties']['open']==('false' if group==2 else 'true') for position,state in doors)
    if group==2:
        assert closed['start_clear']
        assert all(not target['reachable'] for target in closed['targets'] if target['label'] in {'interior','south'})
        opened,unused=decode(folder/'open-door-analysis/room.nbt')
        assert entities(opened)==entities(template)
        for item in read(source/'derivation.json')['derivatives']:
            direction=item['direction']
            unused,derived=decode(Path(item['derived']))
            assert derived.keys()<=blocks.keys()
            for position,state in blocks.items():
                east,up,south=position
                cut=(up>=12 and south>=20) or (up>=3 and {'north':south<=5,'south':south>=20,'west':east<=2,'east':east>=24}[direction])
                assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if cut else state)
        for up,south in [(1,1),(2,2)]:
            for east in range(11,15):
                state=blocks[(east,up,south)]
                assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='south' and state['Properties']['half']=='bottom'
        features={'only_four_door_open_properties_changed':True,'block_entities_unchanged_in_open_analysis':True,'air_only_cuts':4,'step_blocks':8,'default_closed_blocks_interior':True}
    else:
        unused,cut=decode(source/'lodestone/inspection-cutaway.nbt')
        expected={position:state for position,state in blocks.items() if not(position[1]>=10 or (position[1]>=5 and(position[0]<=2 or position[2]<=10)))}
        assert cut==expected
        unused,detail=decode(source/'lodestone/gate-stairs-detail.nbt')
        expected={tuple(position[index]-[9,2,4][index] for index in range(3)):state for position,state in blocks.items() if all([9,2,4][index]<=position[index]<=[17,11,11][index] for index in range(3))}
        assert detail==expected
        lava=[position for position,state in blocks.items() if state['Name']=='minecraft:lava']
        assert len(lava)==48
        for east,up,south in lava:
            for delta in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
                neighbor=blocks[tuple(coordinate+offset for coordinate,offset in zip((east,up,south),delta))]['Name']
                assert neighbor not in audit.AIR or neighbor=='minecraft:lava'
        for up,south in [(3,4),(4,7)]:
            for east in range(10,17):
                state=blocks[(east,up,south)]
                assert state['Name'].endswith('_stairs') and state['Properties']['facing']=='south' and state['Properties']['half']=='bottom'
        assert all(any(blocks[(east,up,south)]['Name'] not in audit.AIR for up in [13,14]) for east in range(10,17) for south in range(26,33))
        features={'exact_record_removal_cut':True,'exact_translated_detail':True,'six_face_sealed_lava':48,'step_blocks':14,'rear_roof_columns':49}
    report={'group':group,'image':7,'sha256':audit.digest(candidate),'size':template['size'],'native_selected':native['survival_checked'],'route_targets':len(routes['targets']),'supported_clear_ports':len(ports),'features':features,'scope':'Offline registry, selected survival and body-shape paths. Door interaction, world ticks, gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps(report,indent=2))
