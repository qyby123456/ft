import json
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
BASE = Path('D:/ft工程文件')
sources = {
    2: BASE / 'FT地牢_第2组重做B_20261104/S2-09',
    3: BASE / 'FT地牢_第3组重做B_20261104/S3-09',
}
for group, source in sources.items():
    folder = ROOT / f'S{group}-09-independent'
    template = audit.decode(folder / 'room.nbt')
    blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
    entities = {tuple(entry['pos']): entry['nbt'] for entry in template['blocks'] if 'nbt' in entry}
    native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
    routes = json.loads((folder / 'independent-routes.json').read_text())[0]
    assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
    assert not native['survival_failures'] and not native['survival_unsupported']
    assert routes['start_clear'] and all(target['reachable'] and target['standing_clear'] for target in routes['targets'])
    assert not template.get('entities')
    def air(position):
        return blocks[tuple(position)]['Name'] in {'minecraft:air', 'minecraft:cave_air', 'minecraft:void_air'}
    if group == 2:
        metadata = json.loads((source / 'incoming/G2B-09/room.json').read_text(encoding='utf-8'))
        assert len(entities) == 3
        for chest in metadata['chests']:
            position = tuple(chest['position'])
            state = blocks[position]
            payload = entities[position]
            assert state['Name'] == payload['id'] == 'minecraft:chest'
            assert state.get('Properties', {}).get('facing') == chest['facing']
            assert not payload.get('LootTable') and not payload.get('Lock')
            items = payload['Items']
            assert len(items) == chest['expected_slots']
            assert len({item['Slot'] for item in items}) == len(items)
            assert all(0 <= item['Slot'] < 27 for item in items)
            assert sorted((item['id'].removeprefix('minecraft:'), item['Count']) for item in items) == sorted(tuple(item) for item in chest['expected_items'])
            assert all(air((position[0], position[1] + height, position[2])) for height in (1, 2))
            front = chest['front_feet']
            assert all(air((front[0], front[1] + height, front[2])) for height in range(3))
        roof_solid = sum(blocks[east, 14, south]['Name'] not in audit.AIR for east in range(29) for south in range(3, 29))
        assert roof_solid == metadata['roof']['solid_columns'] == 448
        assert all(air((east, height, south)) for east in range(6, 23) for south in range(6, 24) for height in range(14, 18))
        derivation = json.loads((source / 'derivation.json').read_text(encoding='utf-8'))
        assert len(derivation['derivatives']) == 4
        for item in derivation['derivatives']:
            derivative = audit.decode(Path(item['derived']))
            derivative_blocks = {tuple(entry['pos']): derivative['palette'][entry['state']] for entry in derivative['blocks']}
            removed = []
            for position, state in blocks.items():
                east, height, south = position
                near = {'north': south <= 3, 'south': south >= 27, 'west': east <= 2, 'east': east >= 26}[item['direction']]
                cut = height >= 14 or (height >= 3 and near)
                assert derivative_blocks[position] == ({'Name': 'minecraft:air'} if cut else state)
                if cut and state['Name'] != 'minecraft:air':
                    removed.append(position)
            assert set(removed) == {tuple(position) for position in item['removed_positions']}
            assert len(removed) == item['removed_nonair']
            assert {tuple(entry['pos']): entry['nbt'] for entry in derivative['blocks'] if 'nbt' in entry} == entities
        roof_result = {'solid_roof_columns': roof_solid, 'true_sky_columns': 306, 'exact_air_only_cutaways': 4}
    else:
        assert len(entities) == 4
        payload = entities[18, 8, 10]
        assert payload['id'] == blocks[18, 8, 10]['Name'] == 'minecraft:chest'
        assert not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock')
        assert blocks[18, 8, 10]['Properties']['facing'] == 'south'
        assert all(air((18, height, 10)) for height in range(9, 13))
        assert all(air((18, height, 12)) for height in range(8, 11))
        banners = [payload for payload in entities.values() if payload['id'] == 'minecraft:banner']
        assert len(banners) == 3 and all(payload.get('Patterns') for payload in banners)
        assert all(blocks[east, height, south]['Name'] not in audit.AIR for east in range(37) for south in range(37) for height in (23, 24))
        roof_result = {'solid_roof_columns': 1369, 'solid_roof_layers': [23, 24]}
        derivative = audit.decode(source / 'preview-only/preview-only-cutaway.nbt')
        derivative_blocks = {tuple(entry['pos']): derivative['palette'][entry['state']] for entry in derivative['blocks']}
        removed = 0
        for position, state in blocks.items():
            east, height, south = position
            cut = height >= 23 or (height >= 8 and south >= 34) or (height >= 15 and east >= 34)
            if cut:
                assert derivative_blocks[position]['Name'] == 'minecraft:air'
                removed += state['Name'] != 'minecraft:air'
            else:
                assert derivative_blocks[position] == state
        derivative_entities = {tuple(entry['pos']): entry['nbt'] for entry in derivative['blocks'] if 'nbt' in entry}
        assert derivative_entities == entities
        roof_result['cut_only_removed_solid_blocks'] = removed
    result = {'status': 'PASS', 'route_targets': len(routes['targets']), 'survival_checked': native['survival_checked'], 'block_entities': len(entities), **roof_result, 'scope': 'Independent decoding, native static collision and declared cut verification; world ticks, real interaction and deployment NOT_RUN.'}
    (folder / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
    print(group, result)
