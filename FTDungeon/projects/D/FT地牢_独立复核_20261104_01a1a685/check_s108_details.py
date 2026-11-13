import json
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S1-08-independent'
record = json.loads((folder / 'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
source = Path(record['candidate']).parent
metadata = json.loads((source / 'room.json').read_text(encoding='utf-8'))
template = audit.decode(folder / 'room.nbt')
blocks = {tuple(entry['pos']):template['palette'][entry['state']] for entry in template['blocks']}
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
route = json.loads((folder / 'independent-routes.json').read_text())[0]
boundary = json.loads((folder / 'independent-boundaries.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert len(route['targets']) == 696 and len(metadata['upper_targets']) == 252 and len(metadata['stair_targets']) == 48
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert not template.get('entities') and not any('nbt' in entry for entry in template['blocks'])
exceptions = {tuple(position) for position in metadata['roof_exceptions']}
assert exceptions == {(east,south) for east in range(8,27) for south in range(8,27)}
for east in range(35):
    for south in range(35):
        assert (blocks[east,21,south]['Name'] == 'minecraft:air') == ((east,south) in exceptions)
assert all(blocks[22,height,15]['Name'] == 'minecraft:air' for height in range(2,22))
leaves = [state for state in blocks.values() if state['Name'].endswith('_leaves')]
assert leaves and all(state['Properties']['persistent'] == 'true' for state in leaves)
for port in metadata['ports']:
    east,height,south = port['feet']
    assert port['width'] == port['clearHeight'] == 3
    for offset in (-1,0,1):
        block_x,block_z = (east+offset,south) if port['side'] in {'north','south'} else (east,south+offset)
        assert blocks[block_x,height-1,block_z]['Name'] not in audit.AIR
        assert all(blocks[block_x,height+rise,block_z]['Name'] == 'minecraft:air' for rise in range(3))
render = json.loads((source / 'qa/render.json').read_text(encoding='utf-8'))
assert len(render['views']) == 16 and render['source_nbt_sha256'] == audit.digest(folder / 'room.nbt')
for view in render['views']:
    assert audit.digest(source / view['file']) == view['sha256']
    def removed(position):
        east,height,south = position
        if view['mode'] == 'full':
            return False
        if height >= 21:
            return True
        if view['mode'] == 'hero':
            return height >= 2 and (east>=33 or south>=33)
        return height >= 2 and {'north':south<=1,'south':south>=33,'west':east<=1,'east':east>=33}[view['mode']]
    assert view['displayed_records'] == sum(not removed(position) for position in blocks)
result = {'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','native_selected':native['survival_checked'],'body_targets':len(route['targets']),'declared_body_targets':691,'upper_targets':252,'half_step_targets':48,'true_skylight_columns':361,'roof_columns':864,'persistent_leaves':len(leaves),'source_render_count_and_hash_views':16,'scope':'Independent offline native collision and selected survival. Sealed glazed source-water column is not an open waterfall; U gallery has no upper external port. Producer400ticks randomTickSpeed0 do not certify natural random ticks; real interaction, weather/light, gameplay and deployment NOT_RUN.'}
(folder / 'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
