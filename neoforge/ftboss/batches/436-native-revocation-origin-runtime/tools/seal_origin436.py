import sys
from pathlib import Path

BATCH = Path(__file__).resolve().parents[1]
HISTORY = Path('D:/ft工程文件/FTBoss-NeoForge-1.21.1-移植-20261022')
sys.path.insert(0, str(HISTORY / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save

runtime_path = BATCH / 'reports/actual-coverage436-read.json'
diagnostic_path = BATCH / 'reports/actual-first-rejection436-diagnostic.json'
ranged_path = BATCH / 'reports/actual-ranged436-sealed-experimental.json'
runtime = load(runtime_path)
diagnostic = load(diagnostic_path)
assert runtime['exitCode'] == 0 and runtime['probe']['failures'] == 0
assert load(ranged_path)['positiveNonemptyTimerGroupsDone']
origin = diagnostic['firstRevocationOrigin']
assert isinstance(origin, dict)
trace = origin['trace']
assert origin['failure'] == 'java.lang.IllegalStateException: Original sequence first revoked at phase 0'
frames = [line.strip() for line in trace.splitlines() if line.strip().startswith('at ')]
expected = ['FoodWeaponOriginalSequence$Session.revoke', 'FoodWeaponOriginalSequence$Session.interferenceObserved',
    'FoodWeaponFinalDamage.revokeExtraAuthority', 'FoodWeaponFinalDamage.publishListedProducer']
assert len(frames) >= 4 and all(name in frame for name, frame in zip(expected, frames))
case = runtime['probe']['cases'][0]
publication = case['publications'][0]
assert all(group['done'] for group in publication['actualTimerGroups436'])
session = publication['sessionDetail436']
assert session['next'] == 4 and session['traceRevoked'] and session['firstObservationFailure'] == 'null'
source = HISTORY / 'batches/374-native-consumer-lock-boundary/proposal/net/farmingtales/ftboss/item/FoodWeaponFinalDamage.java'
text = source.read_text(encoding='utf-8')
assert 'if (extraOwnerAuthorityLive(var0) && !selectPublishingWriter(var3)) {' in text
assert 'FoodWeaponFinalDamage.java:2106' in frames[3]
save(BATCH / 'reports/actual-origin436-sealed-experimental.json', {
    'failures': 0, 'firstRevocationCaptured': True, 'firstRevocationPhase': 0,
    'firstRevocationFrames': frames[:4], 'nextProducerWriterSelectionRejected': True,
    'fourOriginalTimerStepsClosedBeforeRevocation': True, 'timerGroupsDone': True,
    'playerDomainRetiredInPostPublicationSnapshot': publication['player_retired'],
    'exactFailedWriterSelectionSubconditionEstablished': False,
    'authorityFieldsWritten': False, 'fullSignedSessionAccepted': False, 'installable': False,
    'inputHashes': {str(path): digest(path) for path in [runtime_path, diagnostic_path, ranged_path, source, Path(__file__)]},
})
print({'failures': 0, 'firstRevocationCaptured': True, 'site': 'publishListedProducer/selectPublishingWriter', 'timerGroupsDone': True, 'exactFailedSubconditionEstablished': False})
