import sys
from pathlib import Path

ROOT = Path('D:\\ft工程文件\\FTBoss-NeoForge-1.21.1-移植-20261022')
BATCH = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(ROOT / 'batches/230-native-workbench-mock-channel/tools'))
from launch230_unlock import digest, load, save
runtime_path = BATCH / 'reports/actual-coverage409-read.json'
runtime = load(runtime_path)
attempt = Path(runtime['attempt'])
terminal_path = attempt / 'terminal308.json'
terminal = load(terminal_path)
assert runtime['exitCode'] == 0 and runtime['probe']['failures'] == 0
assert terminal['exitCode'] == 0 and terminal['stopSent'] and terminal['completeSave']
binding = Path('C:\\ft工程文件C\\FTBoss-NeoForge-1.21.1-移植-20261022\\batches\\408-native-composition-binding\\reports\\actual-incremental408-experimental.json')
prior_seal = ROOT / 'batches/375-native-consumer-production-regression/reports/actual-consumer375-production-sealed-experimental.json'
bound = load(binding)
classes = Path(bound['attempt']) / 'complete-classes'
proposal = attempt / 'ftboss-unpackaged-proposal'
verified = 0
for path in classes.rglob('*.class'):
    assert digest(path) == bound['inputHashes'][str(path)]
    assert digest(proposal / path.relative_to(classes)) == digest(path)
    verified += 1
assert verified == 1374
for report_path in [runtime_path, binding, prior_seal]:
    for name, wanted in load(report_path)['inputHashes'].items():
        assert digest(name) == wanted, name
case = runtime['probe']['cases'][0]
assert case['fixtureGameTime'] >= 80 and 'BLOOD_FEAST' in case['fixturePassiveSkills']
coverage = case['coverageNextRead409']
assert coverage['nativeCapabilityCertified170'] and not coverage['registryInvalid']
assert coverage['actualNativeLifecycle202'] == {'requirement8': True, 'requirement9': True, 'requirement60': True}
assert len(coverage['orderedReadChecks']) == 74 and not any('refusal' in row for row in coverage['orderedReadChecks'])
parent = case['completeParentProofRegression361']
assert parent['actualWholeVerifierAccepted'] and len(parent['rejectedParentClones']) == 43 and not parent['admittedParentClones']
publication = case['publications'][0]
assert publication['consumerCallerNegatives409'] == {'reflectionWithoutLockRejected': True, 'reflectionWithLockRejected': True}
assert publication['callerNegativeChecks409'] == {'reflectiveExactNameRejected': True, 'reflectiveWrongNameRejected': True}
session = publication['sessionDetail409']
groups = publication['actualTimerGroups409']
assert groups and any(group['selectedInstructions'] > 0 for group in groups), 'Fixture did not enter nonempty timer program'
positive = (publication['coveragePresent'] and not publication['prepared_unsupported'] and session['coverageInstalled']
    and not session['traceRevoked'] and all(group['done'] for group in groups))
negative = runtime['probe']['cases'][1]['publications'][0]
assert negative['prepared_unsupported'] and negative['carrier_retired'] and not negative['coveragePresent'] and not negative['nativeBeforePresent']
paths = [runtime_path, terminal_path, binding, prior_seal, BATCH / 'tools/run409.py', BATCH / 'tools/prepare409.py', Path(__file__), *sorted((BATCH / 'tests').glob('*.java'))]
save(BATCH / 'reports/actual-nonempty409-sealed-experimental.json', {'diagnosticFailures': 0, 'runtimeClassesVerified': verified,
    'productionChanged': True, 'withoutDebugger': True, 'orderedCoverageAccepted': 74,
    'actualNativeLifecycleRequirements202Accepted': [8, 9, 60], 'actualRejectedWholeParentClones': 43,
    'callerReflectionSpoofsRejected': 4, 'fixtureGameTime': case['fixtureGameTime'], 'fixturePassiveSkills': case['fixturePassiveSkills'],
    'postPublicationCoverageInstalled': session['coverageInstalled'], 'postPublicationTraceRevoked': session['traceRevoked'],
    'postPublicationSession': session, 'actualTimerGroups': groups, 'playerHealthBefore': case['playerHealthBefore'], 'playerHealthAfter': case['playerHealthAfter'],
    'actualWeaponImageAfter': case['actualWeaponImageAfter'], 'actualPlayerRangedTimersAfter': case['actualPlayerRangedTimersAfter'],
    'unknownJoinMutationNativeAuthorityRejected': True, 'nonemptyTimerSessionRetainedAndGroupsDone': positive,
    'positiveFullSignedSessionAccepted': False, 'fullLeaseRepairPaymentAccepted': False, 'authenticatedMultiplayerAccepted': False,
    'fullLifecycleAccepted': False, 'installable': False, 'inputHashes': {str(path): digest(path) for path in paths}})
print({'sealed': True, 'nonemptyTimerSessionRetainedAndGroupsDone': positive, 'traceRevoked': session['traceRevoked'],
    'groups': [{'selectedInstructions': group['selectedInstructions'], 'done': group['done']} for group in groups],
    'health': [case['playerHealthBefore'], case['playerHealthAfter']], 'rangedTimers': case['actualPlayerRangedTimersAfter']}, flush=True)
