import hashlib
import json
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def read_run(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    assert result['exitCode'] == terminal['exitCode'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    for source in [path, terminal_path]:
        hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
    return result, result['probe']['cases'][0]


seed, checkpoint = read_run('repair-claimed-checkpoint')
assert seed['probe']['failures'] == 0 and checkpoint['claimedWeaponSavedForRestart']
assert checkpoint['fullSignedSessionRan'] is False
restart, loaded = read_run('repair-claimed-coherent-restart')
assert restart['probe']['failures'] == 1
assert restart['probe']['failure'] == 'java.lang.AssertionError: Actual fully claimed weapon reload did not restore a genuine repair lease and generation'
for key in ['actualNewServerBoot', 'claimedWeaponLoadedFromOriginalPlayerSave',
            'claimedWholeImageEqualsSaved', 'loadedDamageMatchesOriginalSavedWear',
            'loadedMaximumMatchesOriginalSavedWear', 'originalSavedWearTaskConsumed',
            'loadedWholeImageMatchesSignedClaim']:
    assert loaded[key]
assert loaded['originalSignedJournalStatus'] == 'LEASE_ISSUED'
assert loaded['originalSignedJournalSequence'] == 5
assert loaded['originalSignedJournalPaid'] == [3, 3, 2]
assert loaded['nonNullLeaseAfterActualReload'] is False
assert loaded['repairGenerationAfterActualReload'] is False
observed, combat = read_run('repair-claimed-coherent-observe')
assert combat['observeOriginalCombatWithReloadedAuthority']
assert combat['claimedWholeImageEqualsSaved'] and combat['loadedWholeImageMatchesSignedClaim']
candidate = WORK / 'reports/native-repair-candidate-24201031.json'
hashes[str(candidate)] = hashlib.sha256(candidate.read_bytes()).hexdigest()
assert hashes[str(candidate)] == 'e863d86b83c24b9158b15280afbf0e5996d48dcda97e33821d153543b7d79309'
evidence = {
    'candidate': 'native-repair-24201031',
    'coherentActualPlayerWearJournalCheckpointProven': True,
    'actualClaimedWeaponReloadedInNewProcess': True,
    'claimedWeaponNonNullLeaseRestored': False,
    'claimedWeaponRepairGenerationRestored': False,
    'strictClaimedContinuationAccepted': False,
    'combatObservationFailures': observed['probe']['failures'],
    'combatObservationAttackReturned': combat.get('attackReturned', False),
    'combatObservationAttackFailure': combat.get('attackFailure'),
    'combatObservationOuterFailure': observed['probe'].get('failure'),
    'combatObservationTargetHealthAfter': combat.get('targetHealthAfter'),
    'combatObservationTraceComplete': [entry.get('traceComplete') for entry in combat.get('publications', [])],
    'mixedSavePointEarlierRuntime': 'repair-runtime-7c08976e',
    'mixedSavePointEarlierResultExcludedFromCoherentAcceptance': True,
    'sourceCheckpointWorldModified': False, 'authorityWrittenByReflection': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/claimed-restart-coherent-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(evidence, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'strictClaimedContinuationAccepted': False,
       'combatObservationFailures': observed['probe']['failures']})
