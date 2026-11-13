import gc
import hashlib
import json
import math
import sys
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode


root = Path(sys.argv[1]).resolve()
base = root.parent / 'S3-submission-20261107-072954-274564'
world = root / 'independent-worldqa-r3-r2'
snapshot = json.loads((root / 'supplement-snapshot.json').read_text(encoding='utf-8'))
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
report = json.loads((world / 'world-report.json').read_text(encoding='utf-8'))
assert len(report['candidates']) == 11 and report['issues'] == 0
results = []
for candidate in report['candidates']:
    name = candidate['id']
    source = base / 'rooms' / name / (name + '.nbt')
    assert hashlib.sha256(source.read_bytes()).hexdigest() == candidate['source_sha256']
    current = world / (name + '-after-1200.nbt')
    assert current.read_bytes() == (root / 'worldqa-r3' / current.name).read_bytes()
    original, before = decode(source)
    captured, after = decode(current)
    assert original['size'] == captured['size'] == candidate['size']
    assert len(after) == math.prod(original['size']) == candidate['cleared_volume']
    assert candidate['elapsed_ticks'] == 1200
    assert not candidate['support_failures'] and not candidate['dropped_items']
    assert candidate['walk']['failures'] == 0
    changes = []
    defaults = Counter()
    entities = Counter()
    for position, actual in after.items():
        expected = before.get(position, {'state': {'Name': 'minecraft:air'}, 'nbt': None})
        expected_state, actual_state = expected['state'], actual['state']
        assert expected_state['Name'] == actual_state['Name'], (name, position, expected_state, actual_state)
        expected_properties = expected_state.get('Properties', {})
        actual_properties = actual_state.get('Properties', {})
        for key, value in expected_properties.items():
            if actual_properties.get(key) != value:
                assert expected_state['Name'] in ('minecraft:iron_bars', 'minecraft:light_blue_stained_glass_pane', 'minecraft:glass_pane', 'minecraft:blue_stained_glass_pane'), (name, position, expected_state, actual_state)
                assert key in ('east', 'west', 'north', 'south')
                changes.append(dict(position=position, key=key, before=value, after=actual_properties[key]))
        for key in actual_properties.keys() - expected_properties.keys():
            value = actual_properties[key]
            assert (key in ('waterlogged', 'powered') and value == 'false') or (key == 'power' and value == '0' and actual_state['Name'] == 'minecraft:target') or (actual_state['Name'] == 'minecraft:stone_brick_wall' and ((key in ('east', 'west', 'north', 'south') and value == 'none') or (key == 'up' and value == 'true'))), (name, position, key, value)
            defaults[actual_state['Name'] + ':' + key + '=' + actual_properties[key]] += 1
        source_entity = expected['nbt'] or {}
        actual_entity = actual['nbt'] or {}
        assert all(actual_entity.get(key) == value for key, value in source_entity.items()), (name, position, source_entity, actual_entity)
        for key in actual_entity.keys() - source_entity.keys():
            value = actual_entity[key]
            if key == 'id':
                assert actual_state['Name'] == 'minecraft:lectern' and value == 'minecraft:lectern'
            elif key == 'Patterns':
                assert actual_entity['id'] == 'minecraft:banner' and value == []
            elif key in ('CookTime', 'BurnTime', 'CookTimeTotal'):
                assert actual_entity['id'] in ('minecraft:furnace', 'minecraft:blast_furnace') and value == 0
            elif key == 'RecipesUsed':
                assert actual_entity['id'] in ('minecraft:furnace', 'minecraft:blast_furnace') and value == {}
            else:
                raise AssertionError((name, position, key, value))
            entities[actual_entity['id'] + ':' + key] += 1
        assert not actual_entity.get('Items') and not actual_entity.get('LootTable')
    results.append(dict(id=name, full_volume=len(after), source_sha256=candidate['source_sha256'], producer_capture_byte_exact=True, state_connection_changes=changes, default_properties=dict(defaults), default_entity_fields=dict(entities), walk_targets=len(candidate['walk']['targets'])))
    del original, before, captured, after
    gc.collect()
assert sum(item['walk_targets'] for item in results) == 81
locks = [lock for candidate in report['candidates'] for lock in candidate['locks']]
assert len(locks) == 6
assert all(lock['empty_hand_denied'] and lock['wrong_name_denied'] and lock['correct_key_opened_menu'] for lock in locks)
doors = next(item for item in report['candidates'] if item['id'] == 's3-12')
assert len(doors['door_control']) == 6 and len(doors['doors_before_interaction']) == 6
assert all(item['open'] and not item['powered'] for item in doors['doors_before_interaction'])
assert all(item['lever_use_powered_open'] and item['lever_use_unpowered_closed'] and item['restored_open'] for item in doors['door_control'])
ladder = json.loads((root / 'independent-ladderqa-r2-r2/ladder-physics.json').read_text(encoding='utf-8'))
assert all(ladder[key] for key in ('top_landed', 'returned_to_base', 'climbable_observed', 'descent_climbable_observed'))
assert ladder['source_sha256'] == report['candidates'][0]['source_sha256']
assert len(ladder['trace']) == 90
assert [item['tick'] for item in ladder['trace']] == list(range(4, 361, 4))
assert ladder['trace'][-1]['on_ground'] and ladder['trace'][-1]['position'][1] < 2.1
for folder in (world, root / 'independent-ladderqa-r2-r2'):
    assert json.loads((folder / 'independent-result.json').read_text())['exit_code'] == 0
result = dict(status='PASS_SCOPED', accepted=False, source_sha256=snapshot['source_sha256'], verified_frozen_files=len(snapshot['files']), world_captures=results, lock_checks=18, temporary_lever_checks=12, normal_ladder_climb_and_descent=True, ladder_ticks=360, open_requirements=['Actual package installation and documented external functions', 'All functional actual eye ray and use', 'Normal whole-map travel and return'])
(root / 'independent-runtime-inspection.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({key:value for key,value in result.items() if key!='world_captures'}, ensure_ascii=False))
