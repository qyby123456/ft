import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
def block_map(path):
    template = audit.decode(path)
    return template, {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}

results = []
for group, case, source in [
    (3,'S3-01-R2-independent',audit.BASE/'FT地牢_第3组重做B_20261104/S3-01'),
    (6,'S6-01-R3-independent',audit.BASE/'FT地牢_Image25接入_20261101/deliveries/S6-redo-single-20261104-01/01-large-chamber-r3')
]:
    folder = ROOT/case
    template, blocks = block_map(folder/'room.nbt')
    assert audit.digest(source/'room.nbt') == audit.digest(folder/'room.nbt')
    if group == 3:
        changes = json.loads((folder/'revision-diff.json').read_text(encoding='utf-8'))['changes']
        assert len(changes)==122
        assert all(change['before']['Name']=='minecraft:snow' and change['after']['Name']=='minecraft:air' for change in changes)
        preview = source/'preview-only/preview-only-cutaway.nbt'
        predicate = lambda east,up,south: up>=20 or (up>=15 and east>4 and south>4) or (up>=3 and (east>=30 or south>=30))
        mapping = json.loads((source/'supplement/view-map.json').read_text(encoding='utf-8'))
        assert audit.digest(preview)==mapping['cutaway_sha256']
        for view in mapping['views']:
            assert audit.digest(Path(view['file']))==view['sha256']
        route_data = json.loads((source/'supplement/route-targets.json').read_text(encoding='utf-8'))
        specification = json.loads((folder/'route-input.json').read_text(encoding='utf-8'))
        specification[0]['targets'] = [{'label':target['id'],'position':target['pos']} for target in route_data['targets']]
        (folder/'route-input.json').write_text(json.dumps(specification,indent=2),encoding='utf-8')
    else:
        preview = source/'lodestone/inspection-cutaway.nbt'
        mapping = json.loads((source/'lodestone/cutaway-provenance.json').read_text(encoding='utf-8'))
        assert audit.digest(preview)==mapping['cutaway_nbt_sha256']
        predicate = lambda east,up,south: up>=15 or (up>=2 and (east==0 or south==0))
        previous_template, previous = block_map(ROOT/'S6-01-R2-independent/room.nbt')
        protected = [position for position in blocks if 9<=position[0]<=19 and 9<=position[2]<=19]
        assert all(blocks[position]==previous[position] for position in protected)
    derivative_template, derivative = block_map(preview)
    assert derivative.keys() <= blocks.keys()
    changed = 0
    for position,original in blocks.items():
        state = derivative.get(position,{'Name':'minecraft:air'})
        if predicate(*position):
            assert state['Name']=='minecraft:air'
        else:
            assert state==original
        changed += state != original
    assert all(any(blocks.get((east,up,south),{}).get('Name','minecraft:air') not in audit.AIR for up in range(15,template['size'][1])) for east in range(template['size'][0]) for south in range(template['size'][2]))
    results.append({'group':group,'source_sha256':audit.digest(folder/'room.nbt'),'preview_air_cut_only':True,'cut_changes':changed,'roof_all_columns_above_y15':True,'snow_removal_only_122':group==3,'r2_center_11x11_all_heights_unchanged':group==6})
(ROOT/'S3-S6-revision-independent.json').write_text(json.dumps(results,indent=2),encoding='utf-8')
print(json.dumps(results,indent=2))
