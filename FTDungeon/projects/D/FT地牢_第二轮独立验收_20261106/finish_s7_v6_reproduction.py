import copy
import hashlib
import json
import sys
from pathlib import Path

from audit_s8_submission import decode

root = Path(sys.argv[1]).resolve()
rebuilt = Path(sys.argv[2]).resolve()
snapshot = json.loads((root / 'submission-snapshot.json').read_text(encoding='utf-8'))
ready = json.loads((root / 'ready-for-review.json').read_text(encoding='utf-8'))
paths = {item['source']: Path(item['snapshot']) for item in snapshot['files']}
for item in snapshot['files']:
    assert hashlib.sha256(Path(item['snapshot']).read_bytes()).hexdigest() == item['sha256']
outputs = [paths[room['candidate']] for room in ready['rooms']]
outputs += [paths[ready['assembly']['candidate']]]
outputs += [paths[part['path']] for part in ready['assembly']['parts']]
records = []
for source in outputs:
    destination = rebuilt / source.relative_to(root)
    digest = hashlib.sha256(destination.read_bytes()).hexdigest()
    assert digest == hashlib.sha256(source.read_bytes()).hexdigest()
    records.append({'file': str(source.relative_to(root)), 'sha256': digest})
merged, cells = decode(paths[ready['assembly']['candidate']])
entities = []
for part in ready['assembly']['parts']:
    data, unused_cells = decode(paths[part['path']])
    for original in data.get('entities', []):
        entity = copy.deepcopy(original)
        for key in ('pos', 'blockPos'):
            entity[key] = [value + offset for value, offset in zip(entity[key], part['origin'])]
        entity['nbt']['Pos'] = [value + offset for value, offset in zip(entity['nbt']['Pos'], part['origin'])]
        entities.append(entity)
canonical = lambda values: sorted(json.dumps(value, sort_keys=True) for value in values)
assert canonical(entities) == canonical(merged.get('entities', []))
assert len(entities) == 3 and len(records) == 26
result = {'status': 'PASS_SCOPED', 'accepted': False, 'frozen_files_unchanged': len(snapshot['files']), 'recipe_outputs': records, 'all_26_outputs_binary_equal': True, 'candidate_nbt_used_as_recipe_input': False, 'reproduction_directory': str(rebuilt), 'part_entities_full_positions_equipment_and_payload_equal': True, 'entity_count': len(entities), 'note': 'Recipe execution completed all 26 equality assertions; initial reporter lacked independent-qa directory. This separate finalizer verifies persisted outputs and writes evidence without rerunning generation.'}
(root / 'independent-qa').mkdir(exist_ok=True)
(root / 'independent-qa/reproduction.json').write_text(json.dumps(result, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(result, ensure_ascii=False, indent=2))
