import json
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S1-07-independent'
record = json.loads((folder / 'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
source = Path(record['candidate']).parent
metadata = json.loads((source / 'room.json').read_text(encoding='utf-8'))
template = audit.decode(folder / 'room.nbt')
blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
closed = json.loads((folder / 'independent-routes.json').read_text())[0]
opened = json.loads((folder / 'open-door-analysis/independent-routes.json').read_text())[0]
boundary = json.loads((folder / 'independent-boundaries.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert closed['start_clear'] and not next(target for target in closed['targets'] if target['label'] == 'interior')['reachable']
assert not next(target for target in closed['targets'] if target['label'] == 'north')['reachable']
assert opened['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in opened['targets'])
assert len(metadata['route']) == 80 and len(opened['targets']) == 83
assert not template.get('entities') and not any('nbt' in entry for entry in template['blocks'])
doors = [state for state in blocks.values() if state['Name'].endswith('_door')]
assert len(doors) == 4 and all(state['Properties']['open'] == 'false' for state in doors)
assert all(blocks[east,16,south]['Name'] not in audit.AIR for east in range(27) for south in range(29))
assert not boundary['solids_not_connected_to_y0_by_six_face_adjacency'] and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
render = json.loads((source / 'qa/render.json').read_text(encoding='utf-8'))
assert len(render['views']) == 14 and render['source_nbt_sha256'] == audit.digest(folder / 'room.nbt')
for view in render['views']:
    assert audit.digest(source / view['file']) == view['sha256']
    if view['mode'] == 'full':
        expected = len(blocks)
    else:
        direction = view['mode']
        def removed(position):
            east,height,south = position
            if height >= 16:
                return True
            if view['mode'] == 'hero':
                return height >= 2 and (east >= 25 or south >= 27)
            near = {'north': south <= 1, 'south': south >= 27, 'west': east <= 1, 'east': east >= 25}
            return height >= 2 and near[direction]
        expected = sum(not removed(position) for position in blocks)
    assert view['displayed_records'] == expected, view['id']
result = {'status': 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION', 'native_selected': native['survival_checked'], 'closed_blocks_interior_and_north': True, 'open_only_four_half_doors': True, 'open_body_targets': len(opened['targets']), 'declared_route_targets': 80, 'roof_columns': 783, 'render_source_and_display_count_checks': 14, 'scope': 'Independent static native closed/open analysis. Producer400ticks randomTickSpeed0 not independent natural ticking, real interaction or deployment certification.'}
(folder / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
