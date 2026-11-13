import hashlib
import json
import sys
from pathlib import Path

from audit_s8_submission import decode, load


def main():
    root = Path(sys.argv[1]).resolve()
    previous = root.parent / 'S1-submission-20261107-081456-028610'
    inputs = load(root / 'independent-supplement-qa/route-input.json')
    originals = load(root / 'worldqa-interactions/inputs.json')
    specifications = {item['sha256']: item for item in originals}
    known = {Path(item['candidate']).name: item['sha256'] for item in load(previous / 'independent-posttick-qa/route-input.json')}
    comparison = []
    for route in inputs:
        specification = specifications[route['sha256']]
        name = specification['id'] + '-after-1200.nbt'
        candidate = root / 'worldqa-run4/posttick-candidates' / name
        digest = hashlib.sha256(candidate.read_bytes()).hexdigest()
        assert digest == known[name]
        if specification['id'] in {'15-training', 'merged'}:
            before_data, before = decode(Path(route['candidate']))
            after_data, after = decode(candidate)
            target = (2, 2, 15) if specification['id'] == '15-training' else (204, 6, 93)
            comparison.append({'id': specification['id'], 'fence_coordinate': target, 'before': before[target], 'after': after[target]})
        route['candidate'] = str(candidate)
        route['sha256'] = digest
    destination = root / 'independent-posttick-interaction-qa'
    destination.mkdir()
    (destination / 'route-input.json').write_text(json.dumps(inputs, ensure_ascii=False, indent=2), encoding='utf-8')
    (destination / 'vanilla-block-tags.json').write_bytes((root / 'independent-supplement-qa/vanilla-block-tags.json').read_bytes())
    (destination / 'fence-state-comparison.json').write_text(json.dumps(comparison, ensure_ascii=False, indent=2), encoding='utf-8')
    print(json.dumps(comparison, ensure_ascii=False))


if __name__ == '__main__':
    main()
