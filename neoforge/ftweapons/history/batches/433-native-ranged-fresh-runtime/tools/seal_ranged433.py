import sys
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save

coverage = BATCH / 'reports/actual-coverage433-read.json'
sealed = BATCH / 'reports/actual-nonempty433-sealed-experimental.json'
runtime = load(coverage)
assert runtime['exitCode'] == 0 and runtime['probe']['failures'] == 0
assert load(sealed)['diagnosticFailures'] == 0
positive, negative = runtime['probe']['cases']
assert positive['rangedRootPresentBefore433'] is False
publication = positive['publications'][0]
identity = publication['rangedAcquisitionIdentity433']
assert identity['plannedRootIsActualPersistentRoot'] and identity['plannedBufferWasNull']
assert identity['freshBufferBound'] and identity['freshBufferIsCurrentStoredBuffer']
assert identity['storedBufferPresentAfter'] and not identity['authorityFieldsWritten']
groups = publication['actualTimerGroups433']
assert groups and all(group['done'] and group['acquired'] and group['rooted'] for group in groups)
assert publication['sessionDetail433']['next'] == 4
assert positive['playerHealthBefore'] == 10.0 and positive['playerHealthAfter'] == 11.0
assert positive['actualPlayerRangedTimersAfter'] == '{LastSkillProc.blood_feast:80L}'
rejected = negative['publications'][0]
assert rejected['prepared_unsupported'] and not rejected['coveragePresent']
assert rejected['carrier_retired'] and not rejected['rangedAcquisitionIdentity433']['freshBufferBound']
save(BATCH / 'reports/actual-ranged433-sealed-experimental.json', {
    'failures': 0, 'actualNativeFreshGetterReceiptBound': True,
    'positiveNonemptyTimerGroupsDone': True, 'originalTimerStepsClosed': 4,
    'playerHealth': [10.0, 11.0], 'rangedTimers': positive['actualPlayerRangedTimersAfter'],
    'unknownProjectileJoinMutationStillRejected': True,
    'traceStillRevoked': publication['sessionDetail433']['traceRevoked'],
    'firstObservationFailure': publication['sessionDetail433']['firstObservationFailure'],
    'firstFailedStep': publication['sessionDetail433']['firstFailedStep'],
    'authorityFieldsWritten': False, 'fullSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [coverage, sealed, Path(__file__), BATCH / 'tests/LoadedWeapon433.java']},
})
print({'failures': 0, 'nativeFreshGetterBound': True, 'timerGroupsDone': True, 'originalStepsClosed': 4, 'fullSessionAccepted': False})
