import hashlib
import json
import zipfile
from datetime import datetime, timezone
from pathlib import Path

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-lease'
THREAD = '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
REPORT = WORK / 'reports/goal-reactivation-session-diagnostic-20261112.json'

def digest(path):
    return hashlib.sha256(Path(path).read_bytes()).hexdigest()

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def save(path, value):
    Path(path).write_text(json.dumps(value, ensure_ascii=True, indent=2) + chr(10), encoding='utf-8')

assert not REPORT.exists(), 'Reactivation evidence must not be overwritten'
owner = load(ROOT / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == THREAD
assert load(ROOT / 'WRITE_OWNER.claim')['threadId'] == THREAD
candidate = WORK / 'reports/native-repair-ranged-use-candidate-dd1ad841.json'
assert digest(candidate) == '787596b981d42b24a80047bdde8ae76e3bfc6185b9f0ca6a45b3380ad7e1c6fa'
binding = load(candidate)
for name, expected in binding['inputHashes'].items():
    assert digest(name) == expected, name
archives = []
for directory in ['weapons-paused-a08a7373', 'weapons-paused-dd1ad841-20261112']:
    folder = ROOT / 'work/session-boss/archives' / directory
    receipt = load(folder / 'archive-receipt.json')
    assert digest(receipt['archive']) == receipt['archiveSha256']
    assert digest(folder / 'manifest.jsonl') == receipt['manifestSha256']
    assert digest(folder / 'RESTORE.md') == receipt['restoreInstructionsSha256']
    with zipfile.ZipFile(receipt['archive']) as archive:
        assert archive.testzip() is None
    archives.append({'receipt': str(folder / 'archive-receipt.json'), 'archiveSha256': receipt['archiveSha256'], 'zipCrcPassed': True})
baseline = load(ROOT / 'baseline.json')
formal = {name: {'path': entry['live'], 'sha256': digest(entry['live'])} for name, entry in baseline.items() if isinstance(entry, dict) and 'live' in entry}
save(REPORT, {'createdAt': datetime.now(timezone.utc).isoformat(), 'threadId': THREAD,
    'previousGoalTurn': 'progress: verified Boss stage09 deployment/archive under previous authorization',
    'currentUserResumeSupersedesPause': True, 'policy': 'formal instances and historical D read-only; no deployment',
    'candidate': str(candidate), 'candidateSha256': digest(candidate), 'verifiedFrozenInputs': len(binding['inputHashes']),
    'archives': archives, 'formalInstancesBefore': formal, 'fullSignedSessionAccepted': False, 'installable': False})
save(WORK / 'reports/owner-before-session-reactivation-20261112.json', owner)
owner.update({'currentGoalStatus': 'active', 'currentFormalInstancePolicy': 'read_only_no_deployment',
    'currentGoalContinuation': 'Explicit user reactivation: genuine lease, repair/payment, complete signed Session then final acceptance; same sole writer',
    'activeWorkstream': 'signed-session-original-damage-authorization-diagnostic', 'weaponCandidateValidationResumed': True,
    'weaponFunctionalDevelopment': 'resumed_by_explicit_active_goal', 'latestGoalReactivationPreflight': str(REPORT),
    'activeRuntimePids': [], 'activeRuntimePid': None, 'activeSupervisor': None})
save(ROOT / 'WRITE_OWNER.json', owner)
print(json.dumps({'preflight': str(REPORT), 'verifiedFrozenInputs': len(binding['inputHashes']), 'archives': len(archives), 'formalPolicy': 'read_only_no_deployment'}))
