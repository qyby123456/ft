import json
from collections import Counter
from pathlib import Path
import audit

folder = Path(__file__).resolve().parent / 'S4-09-independent'
source = audit.BASE / 'FT地牢_第4组逐图返工_20261104/S4-09'
def mapping(path):
    template = audit.decode(path)
    return template, {tuple(entry['pos']): {'state':template['palette'][entry['state']], 'nbt':entry.get('nbt')} for entry in template['blocks']}
template, records = mapping(folder / 'room.nbt')
air = {'state':{'Name':'minecraft:air'}, 'nbt':None}
def at(position):
    return records.get(tuple(position), air)['state']
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
route = json.loads((folder / 'independent-routes.json').read_text())[0]
boundary = json.loads((folder / 'independent-boundaries.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
assert boundary['fluid_counts'] == {'minecraft:water':156} and not boundary['fluid_outside_or_air_neighbors_excluding_up_count']
assert all(set(component['materials']) == {'minecraft:lily_pad'} for component in boundary['solids_not_connected_to_y0_by_six_face_adjacency'])
entities = {position:record['nbt'] for position,record in records.items() if record['nbt'] is not None}
assert Counter(payload['id'] for payload in entities.values()) == {'minecraft:chest':1, 'minecraft:barrel':4, 'minecraft:banner':2}
assert all(not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock') for payload in entities.values())
assert all(len(payload.get('Patterns',[])) == 3 for payload in entities.values() if payload['id'] == 'minecraft:banner')
assert at((15,9,7))['Properties'] == {'facing':'south', 'type':'single', 'waterlogged':'false'}
assert all(at((east,height,south))['Name'] == 'minecraft:air' for east in range(14,17) for height in (10,11) for south in range(6,9))
assert all(at((15,height,8))['Name'] == 'minecraft:air' for height in range(9,12))
assert all(at((east,12,7))['Name'] == 'minecraft:chiseled_stone_bricks' for east in (9,21))
leaves = [record['state'] for record in records.values() if record['state']['Name'].endswith('_leaves')]
assert len(leaves) == 8 and all(state['Properties']['persistent'] == 'true' for state in leaves)
assert sum(record['state']['Name'].endswith('_stairs') for record in records.values()) == 25
assert all(at((east,height,south))['Name'] == 'minecraft:air' for east in range(3,28) for south in range(18,28) for height in range(10,22))
metadata = json.loads((source / 'incoming/S4-09/room.json').read_text(encoding='utf-8'))
for port in metadata['connections']:
    east,height,south = port['position']
    for inward in range(3):
        for offset in range(-1,2):
            horizontal = {'south':(east+offset,south-inward), 'west':(east+inward,south+offset), 'east':(east-inward,south+offset)}[port['facing']]
            assert at((horizontal[0],height-1,horizontal[1]))['Name'] not in audit.AIR
            assert all(at((horizontal[0],height+rise,horizontal[1]))['Name'] == 'minecraft:air' for rise in range(4))
derivative, derived = mapping(source / 'preview-only/upper-cutaway.nbt')
assert derivative['size'] == template['size']
removed = 0
for east in range(31):
    for height in range(22):
        for south in range(31):
            position = east,height,south
            expected = air if height >= 14 else records.get(position,air)
            assert derived.get(position,air) == expected
            removed += height >= 14 and at(position)['Name'] != 'minecraft:air'
assert removed == 945
assert sum(record['nbt'] is not None for record in derived.values()) == 5
result = {'status':'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION','native_selected':native['survival_checked'],'body_targets':len(route['targets']),'empty_chests':1,'empty_barrels':4,'patterned_banners':2,'lid_air_cells':18,'sealed_water_cells':156,'open_forecourt_columns':250,'persistent_leaf_blocks':8,'stairs':25,'supported_3x4x3_ports':3,'exact_air_only_cut_nonair_removed':removed,'scope':'Static native shape and source checks; open ruined roof intentional. World water updates, real opening/GUI, physics, gameplay and deployment NOT_RUN.'}
(folder / 'independent-details.json').write_text(json.dumps(result,indent=2),encoding='utf-8')
print(json.dumps(result,indent=2))
