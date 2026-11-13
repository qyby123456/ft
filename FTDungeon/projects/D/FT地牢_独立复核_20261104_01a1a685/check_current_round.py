import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
def read(path):
    return json.loads(path.read_text(encoding='utf-8'))
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
def cut(blocks,path,predicate):
    unused,derived=mapping(path)
    assert derived.keys()<=blocks.keys()
    for position,state in blocks.items():
        assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if predicate(*position) else state),(path,position)

for label in ['S4-04-R1-independent','S2-05-independent','S3-05-independent','S7-06-independent','S8-06-independent']:
    folder=ROOT/label
    record=read(folder/'file-audit.json')['groups'][0]
    source=Path(record['candidate'])
    assert audit.digest(source)==audit.digest(folder/'room.nbt')==record['sha256']
    native=read(folder/'independent-native-frozen.json')[0]
    assert native['registry_properties']=='PASS' and native['template_roundtrip_block_count']=='PASS'
    assert not native['survival_failures'] and not native['survival_unsupported']
    routes=read(folder/'independent-routes.json')[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    boundary=read(folder/'independent-boundaries.json')[0]
    assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency']:
        assert set(component['materials'])=={'minecraft:lily_pad'},component
    template,blocks=mapping(folder/'room.nbt')
    metadata=read(source.with_suffix('.json'))
    ports=metadata.get('connections',metadata.get('connectors',[]))
    for port in ports:
        east,up,south=port.get('position',port.get('feet'))
        direction=port.get('facing',port.get('direction'))
        width,height=port.get('opening',[port.get('width'),port.get('height',port.get('minimum_height'))])
        for offset in range(-(width//2),width//2+1):
            horizontal=(east+offset,south) if direction in {'north','south'} else (east,south+offset)
            assert blocks.get((horizontal[0],up-1,horizontal[1]),{'Name':'minecraft:air'})['Name'] not in audit.AIR
            assert all(blocks.get((horizontal[0],up+rise,horizontal[1]),{'Name':'minecraft:air'})['Name']=='minecraft:air' for rise in range(height))
    group=record['group']
    details={}
    if group==4:
        old,baseline=mapping(ROOT/'S4-04-independent/room.nbt')
        changed={position for position in blocks.keys()|baseline.keys() if blocks.get(position,{'Name':'minecraft:air'})!=baseline.get(position,{'Name':'minecraft:air'})}
        assert changed=={(east,7,south) for east in [10,28] for south in [11,23,35]},changed
        assert all(baseline.get(position,{'Name':'minecraft:air'})['Name']=='minecraft:air' and blocks[position]['Name']=='minecraft:mossy_stone_bricks' for position in changed)
        assert {tuple(entry['pos']):entry.get('nbt') for entry in old['blocks'] if entry.get('nbt')}=={tuple(entry['pos']):entry.get('nbt') for entry in template['blocks'] if entry.get('nbt')}
        details={'exact_six_support_additions':True,'unchanged_block_entities':True,'open_roof_intentional':True}
    if group==2:
        base=source.parents[2]
        for item in read(base/'derivation.json')['derivatives']:
            direction=item['direction']
            cut(blocks,Path(item['derived']),lambda east,up,south,direction=direction:up>=8 or (up>=2 and {'north':south<=2,'south':south>=38,'west':east<=3,'east':east>=11}[direction]))
        assert all(blocks.get((east,2+rise,south),{'Name':'minecraft:air'})['Name']=='minecraft:air' for east in [6,7,8] for south in range(41) for rise in range(2))
        details={'air_only_cuts':4,'three_wide_center_clear':True,'roof_skylights_intentional':True}
    if group==3:
        cut(blocks,source.parent/'preview-only/preview-only-cutaway.nbt',lambda east,up,south:up>=13 or (up>=11 and 5<=east<=15) or (up>=3 and (east>=19 or south<2)))
        assert not any(state['Name'] in {'minecraft:snow','minecraft:ice'} for state in blocks.values())
        details={'air_only_cuts':1,'unstable_snow_layers_and_ordinary_ice':0}
    if group==7:
        base=source.parents[2]
        cut(blocks,base/'views/PREVIEW_ONLY_cutaway.nbt',lambda east,up,south:up==13 or (up>=2 and (east<=1 or south>=23)))
        for east in range(25):
            for south in range(25):
                if east<=11 or south>=13:
                    assert blocks[(east,13,south)]['Name'] not in audit.AIR
                else:
                    assert all(blocks[(east,up,south)]['Name']=='minecraft:air' for up in range(14))
        details={'air_only_cuts':1,'L_roof_columns':456,'outside_L_explicit_air_erases_existing_terrain':True}
    if group==8:
        base=source.parents[2]
        for direction,cuboids in read(base/'qa/cut-rules.json').items():
            cut(blocks,base/f'views/{direction}/inspection-only-{direction}.nbt',lambda east,up,south,cuboids=cuboids:any(all(first[index]<=coordinate<=last[index] for index,coordinate in enumerate([east,up,south])) for first,last in cuboids))
        details={'air_only_cuts':4}
    result={'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','sha256':record['sha256'],'size':template['size'],'native_selected':native['survival_checked'],'reachable_targets':len(routes['targets']),'clear_supported_ports':len(ports),**details,'scope':'Offline vanilla registry, selected survival, body route and file/geometry checks. World ticks, gameplay and deployment NOT_RUN.'}
    (folder/'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
    print(label,json.dumps(result))
