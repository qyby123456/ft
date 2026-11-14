import hashlib
import json
import os
from pathlib import Path


WORK = Path(__file__).resolve().parents[1]
ROOT = WORK.parents[1]
THREAD = '01a1c40b-b9ff-7f71-8abb-dcecbb37929f'


def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


owner = load(ROOT / 'WRITE_OWNER.json')
claim = load(ROOT / 'WRITE_OWNER.claim')
assert owner['activeWriterThreadId'] == THREAD and claim['threadId'] == THREAD
assert THREAD not in owner['revokedWriterThreadIds']
current = ROOT / 'work/session-chain/reports/actual-current.json'
assert digest(current) == 'ccf1096fe2503d2385f3ee34e000981b85d09e9bf649c9b40cb11cab30aae6bc'
candidate_path = ROOT / 'work/session-continuity/reports/candidate-transactional.json'
assert digest(candidate_path) == '813f1156373e2c0aa68edb94ca3267ce623bfba98d7a343741d9a6250a71be5f'
checked = ROOT / 'work/session-continuity/reports/checked-transactional.json'
assert digest(checked) == '544440bff757fdc16db0cd50bace4140110c8ed6aff626029763d894c9669496'
accepted_path = WORK / 'reports/accepted-runtime.json'
accepted = load(accepted_path)
assert accepted['failures'] == 0 and accepted['activeCoveredNegativesAccepted']
for result in accepted['results'].values():
    assert result['failures'] == 0
    for name, expected in result['inputHashes'].items():
        assert digest(Path(name)) == expected, name
candidate = load(candidate_path)
classes = Path(candidate['attempt']) / 'complete-classes'
for relative in candidate['changedRawClasses']:
    source = classes / relative
    assert digest(source) == candidate['inputHashes'][str(source)], relative
for name, expected in candidate['inputHashes'].items():
    if name.endswith('.java') or name.endswith('complete-arguments316.json'):
        assert digest(Path(name)) == expected, name
previous = WORK / 'reports/predecessor-current.json'
with previous.open('xb') as stream:
    stream.write(current.read_bytes())
candidate.update({
    'cumulativeCandidate': True,
    'previousCandidateReceipt': str(previous),
    'previousCandidateReceiptSha256': digest(previous),
    'focusedNativeContinuityAccepted': True,
    'activeScopeNegativesAccepted': True,
    'runtimeAccepted': True,
    'runtimeAcceptanceScope': 'Isolated native bow fixture: three repeated Sessions and four covered active-scope faults; no test Mixin; detached binary observation.',
    'positiveSignedSessionAccepted': False,
    'nonNullLeaseAccepted': False,
    'repairPaymentAccepted': False,
    'fullLifecycleAccepted': False,
    'authenticatedMultiplayerAccepted': False,
    'installable': False,
    'deployed': False,
    'promotedByThreadId': THREAD,
    'runtimeEvidence': str(accepted_path),
    'reusedStaticChecks': str(checked),
})
for path in [previous, accepted_path, candidate_path, checked, Path(__file__)]:
    candidate['inputHashes'][str(path)] = digest(path)
encoded = (json.dumps(candidate, ensure_ascii=False, indent=2) + chr(10)).encode('utf-8')
promoted = WORK / 'reports/promoted-current.json'
with promoted.open('xb') as stream:
    stream.write(encoded)
temporary = current.with_suffix('.promoting.json')
with temporary.open('xb') as stream:
    stream.write(encoded)
    stream.flush()
    os.fsync(stream.fileno())
os.replace(temporary, current)
assert current.read_bytes() == promoted.read_bytes()
owner['candidateReceiptSha256'] = digest(current)
owner['status'] = 'active_successor_writer'
owner['latestFocusedRuntimeEvidence'] = str(accepted_path.relative_to(ROOT)).replace(os.sep, '/')
(ROOT / 'WRITE_OWNER.json').write_text(json.dumps(owner, ensure_ascii=False, indent=2) + chr(10), encoding='utf-8')
print(json.dumps({'candidate': str(current), 'sha256': digest(current), 'classCount': candidate['classCount'],
                  'guardCount': candidate['guardCount'], 'installable': False, 'deployed': False}, ensure_ascii=False))
