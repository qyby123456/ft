import json
from pathlib import Path
import audit

ROOT=Path(__file__).resolve().parent
def mapping(path):
    template=audit.decode(path)
    return template,{tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}

results=[]
for group,case,relative,candidate in [
    (2,'S2-02-R2-independent','FT地牢_第2组重做B_20261104/S2-02/revision-02','incoming/G2B-02/room.nbt'),
    (3,'S3-03-independent','FT地牢_第3组重做B_20261104/S3-03','room.nbt')
]:
    folder=ROOT/case
    source=audit.BASE/relative
    template,blocks=mapping(folder/'room.nbt')
    assert audit.digest(folder/'room.nbt')==audit.digest(source/candidate)
    metadata=json.loads((source/candidate).with_suffix('.json').read_text(encoding='utf-8'))
    for port in metadata['connections']:
        east,up,south=port['position']
        for offset in range(-(port['width']//2),port['width']//2+1):
            horizontal=(east+offset,south) if port['facing'] in {'north','south'} else (east,south+offset)
            assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
            assert all(blocks[(horizontal[0],up+rise,horizontal[1])]['Name']=='minecraft:air' for rise in range(port['height']))
    routes=json.loads((folder/'independent-routes.json').read_text(encoding='utf-8'))[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    boundary=json.loads((folder/'independent-boundaries.json').read_text(encoding='utf-8'))[0]
    assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
    derivatives=[]
    if group==2:
        old_template,old=mapping(ROOT/'S2-02-independent-final/room.nbt')
        differences=[position for position in blocks if blocks[position]!=old[position]]
        assert set(differences)=={(6,11,20),(15,11,20)}
        assert all(old[position]['Name']=='minecraft:air' and blocks[position]['Name']=='minecraft:chain' and blocks[position]['Properties']['axis']=='y' for position in differences)
        for record in json.loads((source/'derivation.json').read_text(encoding='utf-8'))['derivatives']:
            direction=record['direction']
            predicate=lambda east,up,south,direction=direction: (up>=12 and south>=4) or (up>=2 and {'north':4<=south<=7,'south':south>=21,'west':east<=3 and south>=4,'east':east>=19 and south>=4}[direction])
            derivatives.append((Path(record['derived']),predicate))
    else:
        predicate=lambda east,up,south: up>=13 or (up>=11 and east>3 and south<15) or (up>=3 and (east>=19 or south<=1)) or (up>=8 and (east>=17 or south<=3))
        derivatives.append((source/'preview-only/preview-only-cutaway.nbt',predicate))
        assert all(any(blocks[(east,up,south)]['Name'] not in audit.AIR for up in range(11,15)) for east in range(21) for south in range(19))
        chest=next(entry for entry in template['blocks'] if entry['pos']==[11,3,11])
        assert template['palette'][chest['state']]['Name']=='minecraft:chest'
        assert not chest['nbt'].get('Items') and not chest['nbt'].get('LootTable')
        assert all(blocks[(11,up,11)]['Name']=='minecraft:air' for up in [4,5])
    for path,predicate in derivatives:
        unused,derived=mapping(path)
        assert derived.keys()<=blocks.keys()
        for position,state in blocks.items():
            assert derived.get(position,{'Name':'minecraft:air'})==({'Name':'minecraft:air'} if predicate(*position) else state),(path,position)
    results.append({'group':group,'sha256':audit.digest(folder/'room.nbt'),'port_checks':'PASS','route_targets_including_duplicates':len(routes['targets']),'disconnected_solids':0,'bottom_side_fluid_air_edges':0,'verified_air_only_previews':len(derivatives),'minimal_two_chain_diff':True if group==2 else None,'scope':'Independent static review; world ticks and interaction not tested.'})
(ROOT/'20261105-first-independent.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps(results,indent=2))
