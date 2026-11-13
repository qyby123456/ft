import json
from pathlib import Path
import audit

root=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
for group,label in [(2,'S2-06-independent'),(4,'S4-05-independent')]:
    folder=root/label
    record=read(folder/'file-audit.json')['groups'][0]
    source=Path(record['candidate'])
    base=source.parents[2]
    blocks=mapping(folder/'room.nbt')
    assert audit.digest(source)==audit.digest(folder/'room.nbt')
    native=read(folder/'independent-native-frozen.json')[0]
    routes=read(folder/'independent-routes.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS' and not native['survival_failures'] and not native['survival_unsupported']
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    for port in read(source.with_suffix('.json'))['connections']:
        east,up,south=port['position']
        for offset in range(-(port['width']//2),port['width']//2+1):
            horizontal=(east+offset,south) if port['facing'] in {'north','south'} else (east,south+offset)
            assert blocks.get((horizontal[0],up-1,horizontal[1]),{'Name':'minecraft:air'})['Name'] not in audit.AIR
            assert all(blocks.get((horizontal[0],up+rise,horizontal[1]),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(port['height']))
    boundary=read(folder/'independent-boundaries.json')[0]
    assert not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
    assert all(set(component['materials'])=={'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
    if group==2:
        skylight={tuple(position) for position in read(base/'qa/dynamic-boundaries.json')['roof']['intentional_skylight_xz']}
        roof=0
        for east in range(25):
            for south in range(25):
                footprint=1<=east<=24 and south<=23 and (east<=13 or south>=11)
                if not footprint:
                    assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for up in range(13))
                elif (east,south) in skylight:
                    assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for up in [11,12])
                else:
                    assert blocks[(east,11,south)]['Name'] not in audit.AIR
                    roof+=1
        for item in read(base/'derivation.json')['derivatives']:
            derived=mapping(Path(item['derived']))
            assert derived.keys()<=blocks.keys()
            direction=item['direction']
            for position,state in blocks.items():
                east,up,south=position
                remove=up>=9 or (up>=2 and {'north':south<=2,'south':south>=22,'west':east<=3,'east':east>=22}[direction])
                assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if remove else state)
        details={'L_roof_columns_excluding_skylight':roof,'intentional_skylight_cells':len(skylight),'air_only_cuts':4,'outside_L_explicit_air_erases_terrain':True}
    else:
        assert all(blocks[(east,13,south)]['Name'] not in audit.AIR for east in range(15) for south in range(39))
        derived=mapping(base/'preview-only/roof-west-cutaway.nbt')
        assert derived.keys()<=blocks.keys()
        for position,state in blocks.items():
            east,up,south=position
            remove=up>=11 or (east<=1 and up>=4)
            assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if remove else state)
        details={'roof_columns':585,'air_only_cuts':1,'waterlogged_top_slab_path_offline_native_shape_checked':True}
    result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':record['sha256'],'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'ports':2,**details,'scope':'Offline vanilla selected survival and body routes plus source geometry. World ticks, gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(label,json.dumps(result))
