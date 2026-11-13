import collections
import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
reports = []
for group, case, source, candidate in [
    (3,'S3-02-independent-final','FT地牢_第3组重做B_20261104/S3-02','room.nbt'),
    (4,'S4-02-R1-independent-final','FT地牢_第4组逐图返工_20261104/S4-02','incoming/S4-02/room.nbt')
]:
    folder = ROOT/case
    directory = audit.BASE/source
    template = audit.decode(folder/'room.nbt')
    blocks = {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
    assert audit.digest(folder/'room.nbt')==audit.digest(directory/candidate)
    metadata = json.loads((directory/candidate).with_suffix('.json').read_text(encoding='utf-8'))
    for port in metadata['connections']:
        east,up,south = port['position']
        for offset in range(-(port['width']//2),port['width']//2+1):
            horizontal = (east+offset,south) if port['facing'] in {'north','south'} else (east,south+offset)
            assert blocks[(horizontal[0],up-1,horizontal[1])]['Name'] not in audit.AIR
            assert all(blocks[(horizontal[0],up+height,horizontal[1])]['Name']=='minecraft:air' for height in range(port['height']))
    entities = [entry for entry in template['blocks'] if 'nbt' in entry]
    assert not template.get('entities')
    assert all(not entry['nbt'].get('Items') and not entry['nbt'].get('LootTable') for entry in entities)
    routes = json.loads((folder/'independent-routes.json').read_text(encoding='utf-8'))[0]
    assert routes['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in routes['targets'])
    details = {}
    if group==3:
        assert all(any(blocks.get((east,up,south),{}).get('Name','minecraft:air') not in audit.AIR for up in range(12,17)) for east in range(29) for south in range(27))
        derivative = audit.decode(directory/'preview-only/preview-only-cutaway.nbt')
        derived = {tuple(entry['pos']):derivative['palette'][entry['state']] for entry in derivative['blocks']}
        assert derived.keys()<=blocks.keys()
        for position, state in blocks.items():
            east,up,south = position
            cut = up>=15 or (up>=12 and east>3 and south>3) or (up>=3 and (east>=27 or south>=25)) or (up>=9 and (east>=25 or south>=23))
            assert derived.get(position,{'Name':'minecraft:air'}) == ({'Name':'minecraft:air'} if cut else state)
        details = {'roof_columns':783,'preview_air_cut_only':True,'empty_block_entities':len(entities)}
    else:
        assert sum(state['Name']=='minecraft:verdant_froglight' for state in blocks.values())==5
        waters = {position for position,state in blocks.items() if state['Name']=='minecraft:water' or state.get('Properties',{}).get('waterlogged')=='true'}
        queue = collections.deque([next(iter(waters))])
        visited = {queue[0]}
        while queue:
            position=queue.popleft()
            for direction in [(1,0,0),(-1,0,0),(0,1,0),(0,-1,0),(0,0,1),(0,0,-1)]:
                neighbor=tuple(position[index]+direction[index] for index in range(3))
                if neighbor in waters and neighbor not in visited:
                    visited.add(neighbor)
                    queue.append(neighbor)
        assert len(visited)==len(waters)
        boundary=json.loads((folder/'independent-boundaries.json').read_text(encoding='utf-8'))[0]
        assert boundary['fluid_outside_or_air_neighbors_excluding_up_count']==0
        assert all(set(component['materials'])<={'minecraft:vine','minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
        details={'green_inlay_blocks':5,'water_and_waterlogged_cells_connected':len(waters),'empty_block_entities':len(entities),'water_side_bottom_air_edges':0}
    reports.append({'group':group,'image':2,'sha256':audit.digest(folder/'room.nbt'),'ports_supported_and_declared_air_clear':True,'reachable_targets_including_duplicates':len(routes['targets']),**details,'scope':'Independent static evidence, not world tick simulation.'})
(ROOT/'second-rooms-independent.json').write_text(json.dumps(reports,indent=2),encoding='utf-8')
print(json.dumps(reports,indent=2))
