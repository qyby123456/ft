import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def common(folder):
    native=read(folder/'independent-native-frozen.json')[0]
    route=read(folder/'independent-routes.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
    assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
    return native['survival_checked'],len(route['targets'])

folder=ROOT/'S6-04-independent'
source=audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/04-great-hall'
template,blocks=mapping(folder/'room.nbt')
assert audit.digest(folder/'room.nbt')==audit.digest(source/'room.nbt')
native,routes=common(folder)
for port in read(source/'room.json')['connectors']:
    east,up,south=port['position']
    for offset in range(-(port['width']//2),port['width']//2+1):
        horizontal=(east+offset,south) if port['facing'] in {'north','south'} else (east,south+offset)
        assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
        assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(port['height']))
unused,cut=mapping(source/'lodestone/inspection-cutaway.nbt')
assert cut=={position:state for position,state in blocks.items() if not(position[1]>=15 or ((position[0]<=1 or position[2]<=1) and position[1]>=3))}
detail_spec=read(source/'lodestone/detail-provenance.json')
first,last=detail_spec['bounds_inclusive']
unused,detail=mapping(source/'lodestone/arch-detail.nbt')
assert detail=={tuple(position[index]-first[index] for index in range(3)):state for position,state in blocks.items() if all(first[index]<=position[index]<=last[index] for index in range(3))}
assert all(blocks[(east,16,south)]['Name'] not in audit.AIR for east in range(27) for south in range(37))
boundary=read(folder/'independent-boundaries.json')[0]
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
(folder/'independent-details.json').write_text(json.dumps({'sha256':audit.digest(folder/'room.nbt'),'native_selected':native,'route_targets_including_duplicates':routes,'clear_supported_ports':4,'roof_columns':999,'source_cutaway_and_arch_detail':'EXACT','scope':'Static offline checks only;world ticks and interaction NOT_RUN'},indent=2),encoding='utf-8')

folder=ROOT/'S5-04-independent'
source=audit.BASE/'FT地牢_第5组逐图重做_20261104/04-coral-processional-hall'
template,blocks=mapping(folder/'room.nbt')
native,routes=common(folder)
doors=[((17,3,0),(0,0,-1),7),((17,3,42),(0,0,1),7),((0,3,20),(-1,0,0),5),((34,3,20),(1,0,0),5)]
def transform(point,turn):
    east,up,south=point
    width,depth=35,43
    for unused in range(turn):
        east,south=depth-1-south,east
        width,depth=depth,width
    return east,up,south
checks=[]
for turn,name in enumerate(['NONE','CLOCKWISE_90','CLOCKWISE_180','COUNTERCLOCKWISE_90']):
    unused,capture=mapping(source/f'worldtest/{name}/after-1200-ticks.nbt')
    width,depth=(35,43) if turn%2==0 else (43,35)
    def at(point):
        east,up,south=point
        return capture[(east+8,up,south+8)]
    def wet(point):
        assert at(point)=={'Name':'minecraft:water','Properties':{'level':'0'}},(name,point)
    def solid(point):
        assert at(point)['Name'] not in audit.AIR,(name,point)
    ring=cover=bed=0
    for east in range(-8,width+8):
        for south in range(-8,depth+8):
            for up in range(3):
                solid((east,up,south))
                bed+=1
            if east<0 or east>=width or south<0 or south>=depth:
                for up in range(3,20):
                    wet((east,up,south))
                    ring+=1
            for up in range(20,28):
                wet((east,up,south))
                cover+=1
    for anchor,direction,door_width in doors:
        for inward in range(3):
            for offset in range(-(door_width//2),door_width//2+1):
                east=anchor[0]-direction[0]*inward+(offset if direction[2] else 0)
                south=anchor[2]-direction[2]*inward+(offset if direction[0] else 0)
                assert blocks[(east,2,south)]['Name'] not in audit.AIR
                assert all(blocks[(east,up,south)]=={'Name':'minecraft:water','Properties':{'level':'0'}} for up in [3,4,5])
        for distance in [1,2,3]:
            for offset in range(-(door_width//2),door_width//2+1):
                east=anchor[0]+direction[0]*distance+(offset if direction[2] else 0)
                south=anchor[2]+direction[2]*distance+(offset if direction[0] else 0)
                solid(transform((east,2,south),turn))
                for up in [3,4,5]:
                    wet(transform((east,up,south),turn))
    checks.append({'rotation':name,'bed_cells':bed,'source_ring_cells':ring,'source_cover_cells':cover,'four_gates_source_and_outside_3deep':'PASS'})
assert not any(state['Name']=='minecraft:chest' for state in blocks.values())
(folder/'independent-details.json').write_text(json.dumps({'sha256':audit.digest(folder/'room.nbt'),'native_selected':native,'swim_targets':routes,'capture_integration_checks':checks,'scope':'Independent source and settled-capture readback of producer isolated Forge test,not another server run;source-water ocean/tank contract mandatory'},indent=2),encoding='utf-8')
print('S6-04 detailed source checks PASS;S5-04 four-rotation full integration capture checks PASS')
