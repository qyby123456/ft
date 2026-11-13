import hashlib
import json
import math
from collections import Counter
from pathlib import Path

from audit_s8_submission import decode, load

root = Path(__file__).parent / 'S7-submission-20261107-102850-176442'
records = load(root / 'submission-snapshot.json')['files']
paths = {item['source']: Path(item['snapshot']) for item in records}
for item in records:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
specifications = load(root / 'worldupdates/inputs.json')
report = load(root / 'worldupdates/world-report.json')
empty = {'state': {'Name': 'minecraft:air'}, 'nbt': None}
summaries = []
for specification in specifications:
    identifier = specification['id']
    source = paths[specification['candidate']]
    assert hashlib.sha256(source.read_bytes()).hexdigest() == specification['sha256']
    original, before = decode(source)
    current, after = decode(root / 'worldupdates' / (identifier + '-after-1200.nbt'))
    assert original['size'] == current['size']
    evidence = next(item for item in report['rooms'] if item['id'] == identifier)
    assert evidence['sha256'] == specification['sha256'] and evidence['ticks'] == 1200
    state_kinds = Counter()
    block_entities = []
    for position in before.keys() | after.keys():
        first, second = before.get(position, empty), after.get(position, empty)
        assert first['state']['Name'] == second['state']['Name'], (identifier, position, first, second)
        if first['state'] != second['state']:
            properties = first['state'].get('Properties', {})
            changed = {key for key, value in properties.items() if second['state'].get('Properties', {}).get(key) != value}
            name = first['state']['Name']
            if changed:
                assert (name in ('minecraft:red_mushroom_block', 'minecraft:mushroom_stem') and changed <= {'up','down','north','south','east','west'}) or (name == 'minecraft:iron_door' and changed <= {'open','powered'}), (identifier, position, changed)
            state_kinds[('explicit:' if changed else 'defaults:') + name] += 1
        if first['nbt'] != second['nbt']:
            assert first['nbt'] is not None and second['nbt'] is not None
            assert all(second['nbt'].get(key) == value for key, value in first['nbt'].items())
            added = {key: value for key, value in second['nbt'].items() if key not in first['nbt']}
            assert added == {'RecipesUsed': {}} and first['state']['Name'] == 'minecraft:furnace'
            block_entities.append({'position': position, 'added': added})
    summaries.append({'id': identifier, 'sha256': specification['sha256'], 'full_volume_compared': math.prod(original['size']), 'type_changes': 0, 'state_kinds': dict(state_kinds), 'be_normalizations': block_entities, 'capture_entities': len(current.get('entities', [])), 'ticks': 1200})
    print(identifier, dict(state_kinds), flush=True)
assert len(summaries) == 11
result = {'accepted': False, 'stage': 'INDEPENDENT_INSPECTION_OF_PRODUCER_1200_TICK_SPARSE_CAPTURES', 'all_11_type_payloads_retained': True, 'frozen_files_unchanged': len(records), 'rooms': summaries, 'limits': ['Producer world runs, independently decoded here; parent independently ran installation, eyes and natural movement separately', 'Sparse captures use Blocks.AIR exclusion; missing entries interpreted as air over stated full dimensions', 'Finite 1200 tick, unrotated scope']}
(root / 'independent-qa/world-updates-inspection.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
