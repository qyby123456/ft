import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
reports=[]
for group,case,source,candidate in [
    (2,'S2-02-independent-final','FT地牢_第2组重做B_20261104/S2-02','incoming/G2B-02/room.nbt'),
    (7,'S7-02-independent','FT地牢_第7组逐图重做_20261104/02-medium-room','incoming/S7R2-02/room.nbt'),
    (8,'S8-02-independent','FT地牢_Image25接入_20261101/deliveries/S8-rebuild-single-20261104-02','incoming/S8R_IMG02/room.nbt')
]:
    folder=ROOT/case
    directory=audit.BASE/source
    template=audit.decode(folder/'room.nbt')
    blocks={tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
    assert audit.digest(folder/'room.nbt')==audit.digest(directory/candidate)
    metadata=json.loads((directory/candidate).with_suffix('.json').read_text(encoding='utf-8'))
    for port in metadata['connections']:
        east,up,south=port.get('position',port.get('feet'))
        width=port.get('width',port.get('opening',[None,None])[0])
        height=port.get('height',port.get('opening',[None,None])[1])
        facing=port.get('facing',port.get('direction'))
        for offset in range(-(width//2),width//2+1):
            horizontal=(east+offset,south) if facing in {'north','south'} else (east,south+offset)
            assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
            assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(height))
    routes=json.loads((folder/'independent-routes.json').read_text(encoding='utf-8'))[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    assert not template.get('entities')
    entities=[entry for entry in template['blocks'] if 'nbt' in entry]
    assert all(not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable') for entry in entities)
    derivatives=[]
    if group==2:
        for record in json.loads((directory/'derivation.json').read_text(encoding='utf-8'))['derivatives']:
            direction=record['direction']
            predicate=lambda east,up,south,direction=direction: (up>=12 and south>=4) or (up>=2 and {'north':4<=south<=7,'south':south>=21,'west':east<=3 and south>=4,'east':east>=19 and south>=4}[direction])
            derivatives.append((Path(record['derived']),predicate))
    elif group==7:
        predicate=lambda east,up,south: up==13 or (up>=2 and (east==0 or south==22)) or (east==1 and up>=10)
        derivatives.append((directory/'views/PREVIEW_ONLY_cutaway.nbt',predicate))
        for east in [9,13]:
            for south in [9,13]:
                assert blocks[(east,2,south)]['Name']=='minecraft:polished_blackstone_bricks'
        assert blocks[(11,2,11)]['Name']=='minecraft:air'
    else:
        boxes={'north':[((0,2,0),(24,13,1)),((1,13,1),(23,13,2))],'east':[((23,2,0),(24,13,24)),((22,13,1),(23,13,23))],'south':[((0,2,23),(24,13,24)),((1,13,22),(23,13,23))],'west':[((0,2,0),(1,13,24)),((1,13,1),(2,13,23))]}
        for direction,bounds in boxes.items():
            predicate=lambda east,up,south,bounds=bounds: up==14 or any(all(first[axis]<=position<=last[axis] for axis,position in enumerate((east,up,south))) for first,last in bounds)
            derivatives.append((directory/f'views/{direction}/inspection-only-{direction}.nbt',predicate))
    if group in {7,8}:
        assert all(blocks[(east,template['size'][1]-1,south)]['Name'] not in audit.AIR for east in range(template['size'][0]) for south in range(template['size'][2]))
    changes=[]
    for path,predicate in derivatives:
        preview=audit.decode(path)
        derived={tuple(entry['pos']):preview['palette'][entry['state']] for entry in preview['blocks']}
        assert derived.keys()<=blocks.keys()
        count=0
        for position,state in blocks.items():
            actual=derived.get(position,{'Name':'minecraft:air'})
            assert actual==({'Name':'minecraft:air'} if predicate(*position) else state),(path,position)
            count+=actual!=state
        changes.append({'file':str(path),'air_only_changes':count})
    boundary=json.loads((folder/'independent-boundaries.json').read_text(encoding='utf-8'))[0]
    assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    reports.append({'group':group,'sha256':audit.digest(folder/'room.nbt'),'ports_clear_supported':True,'routes_including_duplicates':len(routes['targets']),'empty_block_entities':len(entities),'derivations':changes,'disconnected_solid_components':boundary['solids_not_connected_to_y0_by_six_face_adjacency'],'scope':'Static offline independent verification, not world testing.'})
(ROOT/'more-second-rooms-independent.json').write_text(json.dumps(reports,indent=2),encoding='utf-8')
print(json.dumps(reports,indent=2))
