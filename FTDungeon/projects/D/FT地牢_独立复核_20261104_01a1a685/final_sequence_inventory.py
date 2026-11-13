import json
import re
from pathlib import Path
import audit

root = Path(__file__).resolve().parent
notes = [(path, path.read_text(encoding='utf-8')) for path in root.glob('REVIEW*.md')]
audits = []
for path in root.glob('**/file-audit.json'):
    for record in json.loads(path.read_text(encoding='utf-8')).get('groups', []):
        audits.append((path, record))
inventory = []
issues = []
for group in range(1, 9):
    for image in range(1, 11):
        identifier = f'S{group}-{image:02}'
        acceptance_path = root / f'{identifier}-ACCEPTANCE.json'
        acceptance = json.loads(acceptance_path.read_text(encoding='utf-8')) if acceptance_path.is_file() else None
        desired = acceptance.get('nbt_sha256') if acceptance else None
        options = []
        for path, record in audits:
            if record['group'] != group:
                continue
            folder_match = re.match(r'S(\d+)-(\d+)', path.parent.name)
            if folder_match:
                if int(folder_match[2]) != image:
                    continue
            elif image != 1:
                continue
            candidate = Path(record['candidate'])
            if not candidate.is_file():
                continue
            current_hash = audit.digest(candidate)
            if current_hash != record['sha256'] or desired and current_hash != desired:
                continue
            options.append((path.stat().st_mtime, candidate, current_hash, path))
        if group == 5:
            directories = list((audit.BASE / 'FT地牢_第5组逐图重做_20261104').glob(f'{image:02}-*'))
            assert len(directories) == 1
            candidates = list((directories[0] / 'nbt').glob('*.nbt'))
            assert len(candidates) == 1
            candidate = candidates[0]
            current_hash = audit.digest(candidate)
            evidence_path = root / (f'S5-{image:02}-independent/world-independent.json' if image > 1 else 'S5-01-world-independent.json')
            if image == 8:
                evidence_path = root / 'S5-08-independent-r1/world-independent.json'
            world = json.loads(evidence_path.read_text(encoding='utf-8'))
            assert world['sha256'] == current_hash
            assert not desired or desired == current_hash
            options.append((evidence_path.stat().st_mtime, candidate, current_hash, evidence_path))
        if not options:
            issues.append({'id': identifier, 'problem': 'No current source matching independent hash and applicable explicit acceptance'})
            continue
        _, candidate, current_hash, evidence_path = max(options, key=lambda option: option[0])
        supporting_notes = [str(path) for path, text in notes if (current_hash in text or current_hash[:12] in text or current_hash[:8] in text) and 'ACCEPTED' in text]
        if acceptance:
            assert acceptance['status'] == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION'
            supporting_notes.append(str(acceptance_path))
        for supplemental_name in ('ACCEPTANCE.json', 'independent-details.json'):
            supplemental_path = evidence_path.parent / supplemental_name
            if supplemental_path.is_file():
                supplemental = json.loads(supplemental_path.read_text(encoding='utf-8'))
                if supplemental.get('status') == 'ACCEPTED_FOR_SEQUENTIAL_PRODUCTION' and supplemental.get('nbt_sha256', supplemental.get('sha256', supplemental.get('source_sha256'))) == current_hash:
                    supporting_notes.append(str(supplemental_path))
        if not supporting_notes:
            issues.append({'id': identifier, 'problem': 'Acceptance note matching source hash needs manual linkage', 'candidate': str(candidate), 'sha256': current_hash, 'evidence': str(evidence_path)})
        inventory.append({'group': group, 'image': image, 'id': identifier, 'candidate': str(candidate), 'sha256': current_hash, 'current_hash_matches_independent_evidence': True, 'independent_evidence': str(evidence_path), 'acceptance_records': supporting_notes})
report = {'status': 'COMPLETE_CURRENT_SOURCE_AND_ACCEPTANCE_INDEX' if len(inventory) == 80 and not issues else 'NEEDS_INDEX_RECONCILIATION', 'expected': 80, 'indexed': len(inventory), 'issues': issues, 'items': inventory, 'scope': 'Rechecks current source SHA256 against prior independent evidence and links explicit sequential-production acceptance. Preserves individual limitations; does not replace visual/native/route/world evidence or certify deployment.'}
(root / 'FINAL-80-SOURCE-INDEX.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps({'status': report['status'], 'indexed': len(inventory), 'issues': issues}, ensure_ascii=False, indent=2))
