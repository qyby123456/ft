import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
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

CASES=[(2,4,'S2-04-independent','FT地牢_第2组重做B_20261104/S2-04','incoming/G2B-04/room.nbt'),(7,5,'S7-05-independent','FT地牢_第7组逐图重做_20261104/05-corridor','incoming/S7R2-05/room.nbt'),(8,5,'S8-05-independent','FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-05','incoming/S8R_IMG05/room.nbt')]
for group,image,label,relative,candidate in CASES:
    folder=ROOT/label
    source=audit.BASE/relative
    template,blocks=mapping(folder/'room.nbt')
    assert audit.digest(folder/'room.nbt')==audit.digest(source/candidate)
    native=read(folder/'independent-native-frozen.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
    routes=read(folder/'independent-routes.json')[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    metadata=read((source/candidate).with_suffix('.json'))
    ports=metadata.get('connections',metadata.get('connectors',[]))
    for port in ports:
        east,up,south=port.get('position',port.get('feet'))
        direction=port.get('facing',port.get('direction'))
        width,height=port.get('opening',[port.get('width'),port.get('height',port.get('minimum_height'))])
        assert width%2
        for offset in range(-(width//2),width//2+1):
            horizontal=(east+offset,south) if direction in {'north','south'} else (east,south+offset)
            assert blocks.get((horizontal[0],up-1,horizontal[1]),{'Name':'minecraft:air'})['Name'] not in audit.AIR
            assert all(blocks.get((horizontal[0],up+rise,horizontal[1]),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(height))
    boundary=read(folder/'independent-boundaries.json')[0]
    assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    cuts=0
    if group==2:
        for item in read(source/'derivation.json')['derivatives']:
            direction=item['direction']
            predicate=lambda east,up,south,direction=direction: (up>=16 and south>=8) or (up>=2 and {'north':4<=south<=6,'south':south>=35,'west':east<=3 and south>=4,'east':east>=25 and south>=4}[direction])
            verify_cut(blocks,Path(item['derived']),predicate)
            cuts+=1
        banners=[entry for entry in template['blocks'] if template['palette'][entry['state']]['Name'].endswith('_wall_banner')]
        assert len(banners)==9 and all(entry.get('nbt',{}).get('Patterns') for entry in banners)
    if group==7:
        verify_cut(blocks,source/'views/PREVIEW_ONLY_cutaway.nbt',lambda east,up,south: up==11 or (south==42 and up>=2))
        cuts=1
    if group==8:
        for direction,cuboids in read(source/'qa/cut-rules.json').items():
            verify_cut(blocks,source/f'views/{direction}/inspection-only-{direction}.nbt',lambda east,up,south,cuboids=cuboids:any(all(first[index]<=coordinate<=last[index] for index,coordinate in enumerate([east,up,south])) for first,last in cuboids))
            cuts+=1
    roof_columns=None
    if group in {7,8}:
        assert all(blocks[(east,11,south)]['Name'] not in audit.AIR for east in range(15) for south in range(43))
        assert len(ports)==2 and all(port.get('direction',port.get('facing')) in {'north','south'} for port in ports)
        roof_columns=645
    report={'group':group,'image':image,'sha256':audit.digest(folder/'room.nbt'),'size':template['size'],'native_selected':native['survival_checked'],'route_targets_including_duplicates':len(routes['targets']),'clear_supported_ports':len(ports),'verified_air_only_derivatives':cuts,'roof_columns':roof_columns,'scope':'Offline native shapes and selected static survival;world ticks,gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps(report,indent=2))
