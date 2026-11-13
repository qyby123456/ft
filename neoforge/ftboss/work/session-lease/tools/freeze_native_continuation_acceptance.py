import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}
evidence = {}
for name in ['native-save-terminal-focused-evidence', 'native-reload-successor-focused-evidence',
             'native-reload-rejection-focused-evidence', 'native-logout-original-reload-focused-evidence',
             'native-old-signed-save-focused-evidence']:
    path = WORK / 'reports' / (name + '.json')
    receipt = json.loads(path.read_text(encoding='utf-8-sig'))
    assert receipt['installable'] is False and receipt['deployed'] is False and receipt['fullLifecycleAccepted'] is False
    for source, wanted in receipt['inputHashes'].items():
        assert hashlib.sha256(Path(source).read_bytes()).hexdigest() == wanted, source
        hashes[source] = wanted
    hashes[str(path)] = hashlib.sha256(path.read_bytes()).hexdigest()
    evidence[name] = str(path)
candidate = WORK / 'reports/native-repair-candidate-2ca083af.json'
adopted = WORK.parent / 'session-chain/reports/actual-current.json'
assert hashlib.sha256(candidate.read_bytes()).hexdigest() == '842ff9f8dbc7d746fa9979595e4172ff16c0279fc92bb91fa0c9b92c7652aa74'
assert hashlib.sha256(adopted.read_bytes()).hexdigest() == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
for source in [candidate, adopted, Path(__file__)]:
    hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
result = {
    'candidate': str(candidate), 'adoptedCandidateUnchanged': str(adopted),
    'originalSuccessfulPlayerSaveFocusedAccepted': True,
    'realDeleteShareFailureSaveEvidenceReusedFromUnchangedSaveProtocolC8790d91': True,
    'twoActualNewProcessNonNullPaidLeaseRestartsAndCompleteSignedSessions': True,
    'sameEpochGenerations': [0, 1, 2],
    'actualWornWholeItemAndSignedContinuationSurviveSecondRestart': True,
    'consumedSaveReplayDoesNotDuplicateOrRetireFirstSuccessor': True,
    'twelveModifiedOriginalLoadNegativesDoNotConsumeGenuineTicket': True,
    'formerlyValidSupersededSignedSaveRejected': True,
    'actualRestoredLeaseProviderRawAbaAndPublicTimerAbaRevoke': True,
    'otherwiseCoherentCommittedSaveCannotReviveInactiveLineageOnNewBoot': True,
    'actualOriginalPlayerListRemoveSaveAndSameProcessStorageLoadFocusedAccepted': True,
    'worldJoinAndNegotiatedAuthenticatedClientLoginAccepted': False,
    'postMixinExportsCapturedButLoaderFinalBytesNotClaimed': True,
    'preservedFailures': ['native-repair-68ea5bc2/repair-runtime-95c2c2c3',
                          'repair-runtime-5d444a38-incomplete-network-fixture'],
    'remainingAcceptance': [
        'actual failed-save PENDING checkpoint new-process load',
        'process interruption between active-lineage CAS and physical publication',
        'independently changed original wear-state checkpoint',
        'actual transformed Curios factory and loader-final provenance',
        'six retained families including axe and staff, fractional wear and lifecycle recovery',
        'ten external recipes and retired protocol/catalog/translation closure',
        'actual client, dedicated server, authenticated multiplayer, four GL directions and final migration closure',
    ],
    'authorityWrittenByReflection': False,
    'fullLifecycleAccepted': False, 'fullMigrationAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False,
    'focusedEvidence': evidence, 'inputHashes': hashes,
}
output = WORK / 'reports/native-continuation-acceptance-2ca083af.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'focusedContinuationAcceptedOnly': True, 'inputCount': len(hashes), 'installable': False})
