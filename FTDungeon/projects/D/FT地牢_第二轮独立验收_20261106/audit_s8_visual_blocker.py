import hashlib
import json
from datetime import datetime
from pathlib import Path

review = Path('D:/ft工程文件/FT地牢_第二轮独立验收_20261106')
workspace = Path('C:/Users/YOUR_USERNAME/.codex/visualizations/2026/11/04/01a1a685-045b-76d2-aa2b-2a3f24520603')
candidate = workspace / 'preserved-review-snapshots/S8-interaction-revision-20261107-1355'
gallery = workspace / 's8-independent-visual-review-20261107-1600'
def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

completion = load(candidate / 'completion-audit.json')
assert not completion['accepted']
pending = [item for item in completion['requirements'] if item['status'] == 'PENDING_NOT_PROVEN']
assert len(completion['requirements']) == 12 and len(pending) == 1
assert '视觉' in pending[0]['requirement']
source = candidate / 'access-revision-r4/assembly/s8-functional-access-revision.nbt'
assert hashlib.sha256(source.read_bytes()).hexdigest() == completion['source_sha256']
manifest = load(gallery / 'visual-manifest.json')
assert manifest['source_sha256'] == completion['source_sha256'] and not manifest['accepted']
for room in manifest['rooms']:
    directory = gallery / room['id']
    assert not room['visual_inspected']
    room_source = next(directory.glob('*.nbt'))
    assert hashlib.sha256(room_source.read_bytes()).hexdigest() == room['candidate_sha256']
    assert hashlib.sha256((directory / 'reference.png').read_bytes()).hexdigest() == room['reference_sha256']
page = load(gallery / 'html-qa.json')
assert page['image_count'] == 25 and len(page['screenshots']) == 4 and len(page['local_links']) == 14
assert not page['visual_inspected'] and not page['accepted']
for screenshot in page['screenshots']:
    assert hashlib.sha256((gallery / screenshot['filename']).read_bytes()).hexdigest() == screenshot['sha256']
for record in completion['accepted_groups']:
    acceptance = Path(record['acceptance'])
    assert hashlib.sha256(acceptance.read_bytes()).hexdigest() == record['acceptance_sha256']
    assert load(acceptance)['accepted']
assert len(completion['accepted_groups']) == 7
report = dict(
    status='BLOCKED_ACTUAL_VISUAL_OBSERVATION_UNAVAILABLE',
    accepted=False,
    audited_at=datetime.now().isoformat(),
    source_sha256=completion['source_sha256'],
    accepted_groups=7,
    pending_requirement=pending[0]['requirement'],
    previous_goal_turn_classification='PROGRESS: browser verified 25 images and 14 links, source-paired screenshots and updated evidence index',
    repeated_condition='Image tool reports success but supplies no image content; source and render checks cannot establish visual fidelity',
    consecutive_goal_turn_observations=[
        dict(stage='Technical completion and visual-page packaging', observed='Direct current S8-20 cutaway view_image returned no image content; technical gates completed and visual gate retained'),
        dict(stage='Browser gallery QA and evidence index', observed='New S8-19-comparison.png view_image returned no image content; 25-image browser QA passed without aesthetic approval'),
        dict(stage='Current blocker revalidation', observed='New S8-20-comparison.png view_image returned no image content; MCP resource inventory exposes no local review-image resource'),
    ],
    current_candidate_and_gallery_unchanged=True,
    live_process_wait_claimed=False,
    missing_external_change='Restore image content delivery, or obtain actual human visual observations for S8-13/16/19/20 against their references',
    next_action_after_unblock='Record source-bound actual observations; fix proven defects if any; re-audit every requirement before accepting S8 and changing 7/8 to 8/8',
    goal_complete=False,
)
(review / 'S8-VISUAL-BLOCKER-AUDIT-20261107.json').write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding='utf-8')
print(json.dumps(dict(status=report['status'], accepted_groups=7, pending=len(pending), repeated_goal_turn_observations=len(report['consecutive_goal_turn_observations']), candidate_unchanged=True), ensure_ascii=False))
