import hashlib
import json
from datetime import datetime
from pathlib import Path

review = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

completion = load(workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355/completion-audit.json')
groups = []
for expected in completion['accepted_groups']:
    acceptance_path = Path(expected['acceptance'])
    assert hashlib.sha256(acceptance_path.read_bytes()).hexdigest() == expected['acceptance_sha256'], acceptance_path
    acceptance = load(acceptance_path)
    assert acceptance['accepted'] and acceptance['source_sha256'] == expected['source_sha256']
    assert not acceptance.get('unresolved_required_items')
    records = {}
    manifests = []
    for snapshot_path in sorted(acceptance_path.parent.glob('*snapshot*.json')):
        snapshot = load(snapshot_path)
        if 'files' not in snapshot:
            continue
        for item in snapshot['files']:
            frozen = Path(item['snapshot'])
            assert frozen.is_relative_to(acceptance_path.parent)
            assert frozen.is_file(), frozen
            digest = hashlib.sha256(frozen.read_bytes()).hexdigest()
            assert digest == item['sha256'], frozen
            if 'bytes' in item:
                assert frozen.stat().st_size == item['bytes'], frozen
            if str(frozen) in records:
                assert records[str(frozen)]['sha256'] == digest
            records[str(frozen)] = item
        manifests.append(dict(path=str(snapshot_path), sha256=hashlib.sha256(snapshot_path.read_bytes()).hexdigest(), records=len(snapshot['files'])))
    matching_sources = [record for record in records.values() if record['sha256'] == expected['source_sha256']]
    assert matching_sources, expected['group']
    groups.append(dict(group=expected['group'], accepted=True, acceptance=str(acceptance_path), acceptance_sha256=expected['acceptance_sha256'], source_sha256=expected['source_sha256'], matching_source=[record['snapshot'] for record in matching_sources], frozen_files_rehashed=len(records), manifests=manifests, limits=acceptance.get('limits', []), scope='Current frozen delivery seals and file integrity; original acceptance evidence retains its explicit scope, no new world test claimed'))
assert {group['group'] for group in groups} == {'S1','S2','S3','S4','S5','S6','S7'}
references = load(Path('D:/ft工程文件/FT地牢_功能房间第二轮_20261106/reference-audit.json'))
frozen_reference_candidates = {}
for directory in [review, workspace / 'preserved-review-snapshots', workspace / 's8-independent-visual-review-20261107-1600']:
    for snapshot_file in directory.rglob('*snapshot*.json'):
        snapshot_data = load(snapshot_file)
        for item in snapshot_data.get('files', []):
            if Path(item.get('snapshot', '')).suffix.lower() == '.png':
                frozen_reference_candidates.setdefault(item['sha256'], []).append(Path(item['snapshot']))
    expected_digests = {item['sha256'] for group in references['groups'] for item in group['references']}
    for image in directory.rglob('*.png'):
        if image.name == 'reference.png' or image.name.startswith(tuple('S' + str(number) + '-' for number in range(1, 9))):
            digest = hashlib.sha256(image.read_bytes()).hexdigest()
            if digest in expected_digests:
                frozen_reference_candidates.setdefault(digest, []).append(image)
reference_rows = []
missing_references = []
for group in references['groups']:
    assert len(group['references']) == 10
    for reference in group['references']:
        image = Path(reference['image_path'])
        original_available = image.is_file()
        if not original_available:
            matching = [frozen for frozen in frozen_reference_candidates.get(reference['sha256'], []) if frozen.is_file() and hashlib.sha256(frozen.read_bytes()).hexdigest() == reference['sha256']]
            if not matching:
                missing_references.append(dict(id=reference['id'],original_path=str(image),expected_sha256=reference['sha256'],status='ORIGINAL_PATH_MISSING_NO_EXACT_COPY_IN_REVIEW_ROOTS'))
                continue
            image = matching[0]
        digest = hashlib.sha256(image.read_bytes()).hexdigest()
        assert digest == reference['sha256'], image
        reference_rows.append(dict(id=reference['id'], original_path=reference['image_path'], original_available=original_available, exact_frozen_reference=str(image), sha256=digest))
assert len(reference_rows) == len({row['id'] for row in reference_rows}) == len({row['sha256'] for row in reference_rows})
assert len(reference_rows) + len(missing_references) == 80
report = dict(status='SEVEN_ACCEPTANCE_SEALS_PASS_REFERENCE_RETENTION_AUDITED', audited_at=datetime.now().isoformat(), goal_complete=False, accepted_groups=7, groups=sorted(groups,key=lambda item:item['group']), total_current_frozen_files_rehashed=sum(group['frozen_files_rehashed'] for group in groups), references=reference_rows,missing_references=missing_references,all80_reference_bytes_available=not missing_references,pending_group='S8', pending_detail='New S8-20 candidate7636 is frozen and undergoing parent-thread independent validation', no_production_files_modified=True)
(review / 'SEVEN-GROUPS-SEAL-RECHECK-20261108.json').write_text(json.dumps(report,ensure_ascii=False,indent=2),encoding='utf-8')
print(json.dumps(dict(status=report['status'],accepted_groups=7,frozen_files_rehashed=report['total_current_frozen_files_rehashed'],original_references_verified=len(reference_rows),missing_references=[row['id'] for row in missing_references],goal_complete=False),ensure_ascii=False))
