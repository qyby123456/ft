import json
from collections import deque
from pathlib import Path
import audit

ROOT = Path(__file__).resolve().parent
folder = ROOT / 'S5-09-independent'
source = audit.BASE / 'FT地牢_第5组逐图重做_20261104/09-coral-trident-vault'
template = audit.decode(folder / 'room.nbt')
blocks = {tuple(entry['pos']): template['palette'][entry['state']] for entry in template['blocks']}
entities = {tuple(entry['pos']): entry['nbt'] for entry in template['blocks'] if 'nbt' in entry}
assert len(entities) == 5
assert sum(payload['id'] == 'minecraft:chest' for payload in entities.values()) == 1
assert sum(payload['id'] == 'minecraft:barrel' for payload in entities.values()) == 4
assert all(not payload.get('Items') and not payload.get('LootTable') and not payload.get('Lock') for payload in entities.values())
assert blocks[15, 6, 7]['Properties']['facing'] == 'south'
water = {'Name': 'minecraft:water', 'Properties': {'level': '0'}}
assert all(blocks[15, height, 7] == water for height in range(7, 10))
assert all(blocks[15, height, 8] == water for height in range(6, 9))
stairs = [state for state in blocks.values() if state['Name'].endswith('_stairs')]
assert len(stairs) == 15 and all(state['Properties']['waterlogged'] == 'true' for state in stairs)
features = json.loads((source / 'qa/vault-features.json').read_text(encoding='utf-8'))
checks = []
for turn, name in enumerate(['NONE', 'CLOCKWISE_90', 'CLOCKWISE_180', 'COUNTERCLOCKWISE_90']):
    capture = audit.decode(source / f'worldtest/{name}/after-1200-ticks.nbt')
    captured = {tuple(entry['pos']): {'state': capture['palette'][entry['state']], 'nbt': entry.get('nbt')} for entry in capture['blocks']}
    def at(position, rotate=False):
        east, height, south = position
        if rotate:
            for rotation in range(turn):
                east, south = 30 - south, east
        return captured[east + 8, height, south + 8]
    for east in range(-8, 39):
        for south in range(-8, 39):
            assert all(at((east, height, south))['state']['Name'] not in audit.AIR for height in range(3))
            if east < 0 or east >= 31 or south < 0 or south >= 31:
                assert all(at((east, height, south))['state'] == water for height in range(3, 22))
            assert all(at((east, height, south))['state'] == water for height in range(22, 30))
    for gate, direction in [((0, 3, 18), -1), ((30, 3, 18), 1)]:
        for distance in range(-2, 4):
            for offset in range(-2, 3):
                east, height, south = gate[0] + distance * direction, gate[1], gate[2] + offset
                assert at((east, 2, south), True)['state']['Name'] not in audit.AIR
                assert all(at((east, height + rise, south), True)['state'] == water for rise in range(3))
    points = features['cases'][turn]['approach_source']
    assert points[0] == [15, 3, 18] and points[-1] == [15, 6, 8]
    assert all(sum(abs(first[index] - second[index]) for index in range(3)) == 1 for first, second in zip(points, points[1:]))
    for east, height, south in points:
        assert at((east, height, south), True)['state'] == at((east, height + 1, south), True)['state'] == water
    for position, payload in entities.items():
        loaded = at(position, True)['nbt']
        assert {key: value for key, value in loaded.items() if key not in {'x', 'y', 'z'}} == payload
    checks.append({'rotation': name, 'source_water_ring': 8, 'source_cover_layers': 8, 'solid_bed_layers': 3, 'wet_gates': 2, 'explicit_wet_approach_points': len(points), 'container_payloads_preserved': 5})
derivative = audit.decode(source / 'preview-only/preview-only-s5-09-water-hidden-not-importable.nbt')
derived = {tuple(entry['pos']): {'state': derivative['palette'][entry['state']], 'nbt': entry.get('nbt')} for entry in derivative['blocks']}
removed = changed = 0
for position, state in blocks.items():
    expected = json.loads(json.dumps(state))
    if state['Name'] == 'minecraft:water':
        expected = {'Name': 'minecraft:air'}
        removed += 1
    elif state.get('Properties', {}).get('waterlogged') == 'true':
        expected['Properties']['waterlogged'] = 'false'
        changed += 1
    assert derived[position] == {'state': expected, 'nbt': entities.get(position)}
assert removed == 14996 and changed == 48
native = json.loads((folder / 'independent-native-frozen.json').read_text())[0]
route = json.loads((folder / 'independent-routes.json').read_text())[0]
assert native['registry_properties'] == native['template_roundtrip_block_count'] == 'PASS'
assert not native['survival_failures'] and not native['survival_unsupported']
assert route['start_clear'] and all(target['standing_clear'] and target['reachable'] for target in route['targets'])
result = {'status': 'PASS', 'native_selected': native['survival_checked'], 'swim_body_targets': len(route['targets']), 'captures': checks, 'derivative_only_water_removed': removed, 'derivative_only_waterlogged_changed': changed, 'scope': 'Independent read-only capture decoding and offline body collision. Mandatory full wet integration contract; not a new server run, breathing/physics/real container GUI or deployment certification.'}
(folder / 'independent-details.json').write_text(json.dumps(result, indent=2), encoding='utf-8')
print(json.dumps(result, indent=2))
