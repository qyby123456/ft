import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def verify_cut(blocks,path,predicate):
    unused,derived=mapping(path)
    assert derived.keys()<=blocks.keys()
    for position,state in blocks.items():
        assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if predicate(*position) else state),(path,position)
for group,label in [(1,'S1-04-independent'),(7,'S7-08-independent'),(8,'S8-08-independent-r1')]:
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
    assert not boundary['fluid_outside_or_air_neighbors_excluding_up_count'] and not boundary['solids_not_connected_to_y0_by_six_face_adjacency']
    metadata=read(source.with_suffix('.json'))
    ports=metadata.get('ports',metadata.get('connections'))
    for port in ports:
        east,up,south=port.get('position',port.get('feet'))
        width,height=port.get('opening',[port.get('width'),port.get('height',port.get('clearHeight',port.get('minimum_height')))])
        facing=port.get('direction',port.get('side',port.get('facing')))
        for offset in range(-(width//2),width//2+1):
            block_x,block_z=(east+offset,south) if facing in {'north','south'} else (east,south+offset)
            assert blocks[(block_x,up-1,block_z)]['Name'] not in audit.AIR
            assert all(blocks[(block_x,up+rise,block_z)]['Name']=='minecraft:air' for rise in range(height))
    if group==1:
        for east,up,south in metadata['route']:
            assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
            assert all(blocks[(east,up+rise,south)]['Name']=='minecraft:air' for rise in range(3))
        assert all(blocks[(east,17,south)]['Name'] not in audit.AIR for east in range(31) for south in range(35))
        banners=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name'].endswith('_wall_banner')]
        assert len(banners)==2 and all(entry.get('nbt',{}).get('Patterns') for entry in banners)
        assert all(template['palette'][entry['state']]['Name'].endswith('_wall_banner') for entry in template['blocks'] if 'nbt' in entry)
        features={'three_high_route_cells':len(metadata['route']),'roof_columns':1085,'patterned_banner_entities':2,'display_cuts_only':True}
    else:
        assert not any('nbt' in entry for entry in template['blocks'])
        first,last=(8,24) if group==7 else (11,21)
        for east in range(33):
            for south in range(33):
                sky=first<=east<=last and first<=south<=last
                assert (blocks[(east,22,south)]['Name']=='minecraft:air')==sky
        if group==7:
            base=source.parents[2]
            verify_cut(blocks,base/'views/PREVIEW_ONLY_cutaway.nbt',lambda east,up,south: up==22 or (up>=2 and (east==0 or south==32)))
            fences=[position for position,state in blocks.items() if state['Name']=='minecraft:oak_fence']
            assert len(fences)==71 and all(position[1]==11 and blocks[(position[0],10,position[2])]['Name']=='minecraft:stone_bricks' for position in fences)
            assert sum(state['Name']=='minecraft:water' for state in blocks.values())==100
            features={'source_skylight_air':289,'top_rim_solid':800,'supported_oak_fences':71,'water':100,'air_only_cut':True,'upper_ground_access':'NOT_AUTHORED_NOT_ACCEPTED'}
        else:
            base=source.parents[2]
            for direction,boxes in read(base/'qa/cut-rules.json').items():
                verify_cut(blocks,base/f'views/{direction}/inspection-only-{direction}.nbt',lambda east,up,south,boxes=boxes:any(all(first[index]<=coordinate<=last[index] for index,coordinate in enumerate([east,up,south])) for first,last in boxes))
            rail_positions={(east,13,south) for east in range(7,26) for south in range(7,26) if east in {7,25} or south in {7,25}}
            assert len(rail_positions)==72
            for east,up,south in rail_positions:
                state=blocks[(east,up,south)]
                assert state['Name']=='minecraft:deepslate_brick_wall' and state['Properties']['up']=='true'
                assert blocks[(east,up-1,south)]['Name'] not in audit.AIR
                for direction,delta in {'north':(0,-1),'south':(0,1),'east':(1,0),'west':(-1,0)}.items():
                    expected='low' if (east+delta[0],up,south+delta[1]) in rail_positions else 'none'
                    assert state['Properties'][direction]==expected
            lava=[position for position,state in blocks.items() if state['Name']=='minecraft:lava']
            assert len(lava)==9
            allowed={'minecraft:lava','minecraft:deepslate_bricks','minecraft:glass'}
            for east,up,south in lava:
                assert blocks[(east,up+1,south)]['Name']=='minecraft:glass'
                for delta in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
                    assert blocks[tuple(coordinate+offset for coordinate,offset in zip((east,up,south),delta))]['Name'] in allowed
            for step in range(10):
                for east in range(3,6):
                    state=blocks[(east,3+step,8+step)]
                    assert state['Name']=='minecraft:deepslate_brick_stairs' and state['Properties']['facing']=='south' and state['Properties']['half']=='bottom'
            features={'source_skylight_air':121,'top_rim_solid':968,'continuous_wall_rails':72,'six_face_sealed_lava':9,'stair_blocks':30,'air_only_cuts':4,'upper_walkway_reachable':True}
    report={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','group':group,'sha256':record['sha256'],'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'ports':len(ports),**features,'scope':'Offline native registry, selected survival, body-shape routes and source geometry. World ticks, physics, interaction, gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(label,json.dumps(report))
