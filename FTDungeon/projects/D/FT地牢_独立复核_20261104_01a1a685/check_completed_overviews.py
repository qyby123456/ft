import json
from pathlib import Path
import audit

root = Path(__file__).resolve().parent
results = []
for group in range(1, 9):
    case = root / f'S{group}-10-independent'
    details = json.loads((case / 'independent-details.json').read_text(encoding='utf-8'))
    assert details['status'] in {'PASS', 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'}
    frozen_hash = audit.digest(case / 'room.nbt')
    if group == 5:
        candidate = audit.BASE / 'FT地牢_第5组逐图重做_20261104/10-coral-ocean-complex/nbt/s5-10-coral-ocean-complex.nbt'
        evidence = json.loads((case / 'world-independent.json').read_text(encoding='utf-8'))
        assert evidence['sha256'] == frozen_hash
    else:
        record = json.loads((case / 'file-audit.json').read_text(encoding='utf-8'))['groups'][0]
        candidate = Path(record['candidate'])
        assert record['sha256'] == frozen_hash and all(not manifest['mismatches'] for manifest in record['manifests'])
    assert audit.digest(candidate) == frozen_hash
    notes = root / f'REVIEW-20261105-S{group}10.md'
    text = notes.read_text(encoding='utf-8')
    assert frozen_hash in text and 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' in text
    results.append({'group': group, 'image': 10, 'candidate': str(candidate), 'sha256': frozen_hash, 'frozen_source_still_matches_current': True, 'acceptance_note': str(notes), 'scope': 'Sequential-production source acceptance; runtime/deployment limits remain in the linked review.'})
report = {'status': 'EIGHT_OVERVIEWS_ACCEPTED_CURRENT_SOURCES_MATCH', 'overview_checks': results, 'scope': 'Current-hash recheck of accepted image-10 sources and their existing independent evidence. Earlier images are indexed separately in FINAL-80-SOURCE-INDEX.json. Runtime and deployment limitations remain in individual reviews.'}
(root / 'FINAL-EIGHT-OVERVIEWS.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print('Current source hashes match frozen acceptance evidence for groups', [item['group'] for item in results])
