import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
for group,label in [(1,'S1-03-independent'),(3,'S3-07-independent'),(4,'S4-06-independent')]:
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
    assert not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
    assert all(set(component['materials'])=={'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
    metadata=read(source.with_suffix('.json'))
    ports=metadata.get('ports',metadata.get('connections'))
    for port in ports:
        east,up,south=port.get('position',port.get('feet'))
        width=port['width']
        offsets=port.get('width_offsets',[-(width//2),width//2])
        for offset in range(offsets[0],offsets[1]+1):
            horizontal=(east+offset,south) if port.get('facing',port.get('side')) in {'north','south'} else (east,south+offset)
            assert blocks.get((horizontal[0],up-1,horizontal[1]),{'Name':'minecraft:air'})['Name'] not in audit.AIR
            assert all(blocks.get((horizontal[0],up+rise,horizontal[1]),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(port.get('height',port.get('clearHeight'))))
    if group==1:
        for east,up,south in metadata['route']:
            assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
            assert all(blocks[(east,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
        assert all(blocks[(east,11,south)]['Name'] not in audit.AIR for east in range(19) for south in range(17))
        chests=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name']=='minecraft:chest']
        assert len(chests)==1 and not chests[0].get('nbt',{}).get('Items') and not chests[0].get('nbt',{}).get('LootTable')
        east,up,south=chests[0]['pos']
        assert blocks[(east,up+1,south)]['Name']=='minecraft:air'
        details={'roof_columns':323,'empty_chest_with_clear_lid':True,'three_high_declared_route':True}
    else:
        base=source.parent if group==3 else source.parents[2]
        derivative=base/('preview-only/preview-only-cutaway.nbt' if group==3 else 'preview-only/upper-cutaway.nbt')
        unused,derived=mapping(derivative)
        assert derived.keys()<=blocks.keys()
        for position,state in blocks.items():
            east,up,south=position
            remove=((up>=18 and south<=8 and 10<=east<=23) or (up>=8 and (east>29 or east<3))) if group==3 else up>=8
            assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if remove else state),position
        details={'air_only_cut':'PASS','open_sky_intentional':True}
        if group==3:
            assert all(blocks[(east,22,south)]['Name'] not in audit.AIR for east in range(9,25) for south in range(9))
            doors=[state for state in blocks.values() if state['Name'].endswith('_door')]
            assert len(doors)==4 and all(state['Properties']['open']=='true' for state in doors)
            details.update(rear_roof_columns=144,open_door_halves=4,stairs=sum(state['Name'].endswith('_stairs') for state in blocks.values()))
    result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':record['sha256'],'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'ports':len(ports),**details,'scope':'Offline selected native survival/body routes and source geometry. Natural random ticks, actual interactions, campfire damage, gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(label,json.dumps(result))
