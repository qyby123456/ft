import hashlib
import json
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
hashes = {}
def digest(path):
    path = Path(path)
    value = hashlib.sha256(path.read_bytes()).hexdigest()
    hashes[str(path)] = value
    return value

def load(path):
    digest(path)
    return json.loads(path.read_text(encoding='utf-8'))

def frozen(data):
    for name, wanted in data.get('inputHashes', {}).items():
        assert digest(name) == wanted, Path(name).name

def runtime(name, wanted=0):
    path = work / ('reports/native-repair-runtime-exact-melee-' + name + '-41d445cf.json')
    data = load(path)
    frozen(data)
    attempt = Path(data['attempt'])
    end = load(attempt / 'terminal308.json')
    assert data['exitCode'] == end['exitCode'] == 0
    assert data['probe']['failures'] == end['probe']['failures'] == wanted
    assert end['completeSave'] and end['stopSent']
    for name in ['runtime308.log', 'live-process308.json', 'server/actual-storage-view308.json', 'compile-command308.json', 'compile308.log', 'restart-world-seed.json']:
        digest(attempt / name)
    if wanted == 0:
        probe = data['probe']
        assert not probe['authorityWrittenByReflection'] and not probe['authenticatedMultiplayerAccepted'] and not probe['installable']
        assert len(probe['cases']) == 1 and probe['cases'][0]['id'] == 'minecraft:wooden_sword'
        world = attempt / 'server/world'
        for entry in [world / 'level.dat', world / 'data/ftboss_native_weapon_wear.dat', *sorted((world / 'ftboss-authority').rglob('*.bin')), *sorted((world / 'playerdata').glob('*.dat'))]:
            digest(entry)
    return path, data

paid_evidence = load(work / 'reports/native-exact-melee-wooden-configured-paid-focused-evidence-41d445cf.json')
frozen(paid_evidence)
assert paid_evidence['failures'] == 0 and paid_evidence['originalRealInventoryMaterialInstallationsBeforeAnyAuthority'] == 3
paid_path, paid = runtime('wooden-original-installed-paid')
positive_path, positive = runtime('wooden-configured-signed-book-qualified')
restart_path, restart = runtime('wooden-configured-restart')
controls_path, controls = runtime('wooden-configured-controls')
row = positive['probe']['cases'][0]
for key in ['genuineNonNullLeaseAndGenerationBeforeAttack', 'actualPaidReloadRetainedOriginalInstalledFunctionReadyConfiguration',
            'repeatedOriginalEnsureTrackedPreservedExactComponentIdentityWholeImageAndLiveAuthority',
            'foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation', 'foreignDirectEntryPreservesOriginalCallbackExceptionIdentity',
            'originalPlayerAttackReturned', 'originalMeleeSnapshotPresent', 'originalMeleeSnapshotValid', 'originalMeleeSnapshotRepairEpochPresent',
            'sameLeaseAndGenerationAfterAttack', 'sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope',
            'actualOriginalPostCombatSaveCalled', 'actualOriginalPostCombatSaveAuthenticatedCommitted']:
    assert row[key], key
assert row['originalConfiguredMaximumDamage'] == 59 and not row['generationRetiredAfterAttack']
assert row['wearBefore'] == 0 and row['wearAfter'] == 1 and row['healthAfter'] < row['healthBefore']
assert len(row['damageEvents']) == 2
for event in row['damageEvents']:
    assert event['resolvedSnapshotPresent'] and event['resolvedSnapshotValid'] and event['repairEpochPresent'] and event['serverSignaturePresent']
assert row['publications']
for publication in row['publications']:
    for key in ['publicationPresent', 'preparedCommitted', 'originalSequencePresent', 'preparationPresent', 'prepared_sealed', 'prepared_bound',
                'sessionPresent', 'traceComplete', 'session_coverageInstalled', 'originalDynamicSemanticBookPresent', 'exactOriginalNativeBindingAndTargetReturnIdentityCoverage']:
        assert publication[key], key
    assert not publication['prepared_unsupported'] and not publication['prepared_retired'] and not publication['session_traceRevoked']
    assert publication.get('firstObservationFailure') is None
    assert publication['originalStaticExpectedPlanSize'] == publication['actualStaticSessionStepCount'] == 0
    assert publication['session_next'] == publication['originalExpectedReturnedStepCount'] == 1
    assert publication['originalQuotedNativePutCount'] == publication['originalReturnedNativeLeafCount'] == 0
    assert publication['originalQuotedTargetWriteCount'] == publication['originalReturnedTargetLeafCount'] == 0
    groups = publication['actualOriginalCompletedSemanticGroups']
    assert len(groups) == 1 and groups[0]['profile'] == 'butcher'
    assert groups[0]['done'] and groups[0]['exactQuotedKeyCoverage']
    assert groups[0]['quotedTimerWrites'] == groups[0]['returnedTimers'] == 1 and groups[0]['returnedRanged'] == 0
    assert not groups[0]['acquired'] and not groups[0]['rooted']
for key in ['originalWholeItemAndWearCoherent', 'genuineNonNullLeaseAndGeneration', 'sameEpochGenerationTwoToThree',
            'consumedReplayDeniedWithoutChangingFirstSuccessor', 'actualOriginalGenerationThreeSaveCommitted']:
    assert restart['probe']['cases'][0][key], key
for key in ['genuineGenerationTwoBeforeControl', 'originalForeignCallbackExactlyOnce', 'originalCallbackExceptionIdentityPreservedWithLiveUnchangedBeforeMutation',
            'foreignOriginalMutationRetiresGenuineAuthority', 'noPrivatePermissionOrAuthorityReissued', 'copyControl']:
    assert controls['probe']['cases'][0][key], key
excluded = []
for suffix in ['wooden-configured-signed', 'wooden-configured-signed-plan-qualified']:
    path, data = runtime(suffix, 1)
    assert 'Session' in data['probe']['failure'] or 'non-timer melee plan' in data['probe']['failure']
    excluded.append(str(path))
adopted = root / 'work/session-chain/reports/actual-current.json'
assert digest(adopted) == '1ec369cff511adab2c2c57d4e1d5cc89bca546a5ba9be4be9e70d0817dfd2243'
assert load(work / 'reports/native-repair-ranged-use-candidate.json') == load(work / 'reports/native-repair-ranged-use-candidate-41d445cf.json')
for source in (work / 'proposal-native').rglob('*.java'):
    expected = work / 'build/native-repair-41d445cf/sources' / source.relative_to(work / 'proposal-native')
    assert digest(source) == digest(expected), source.name
for path in [Path(__file__), root / 'WRITE_OWNER.claim']:
    digest(path)
helper = work / 'tools/prepare_native_frost_ingress_runtime.py'
digest(helper)
namespace = {'__file__': str(helper)}
exec(compile(helper.read_text(encoding='utf-8').split('candidate = work /')[0], str(helper), 'exec'), namespace)
output = work / 'reports/native-exact-melee-wooden-configured-paid-signed-restart-focused-evidence-41d445cf.json'
namespace['add'](output.relative_to(root).as_posix(), json.dumps({
    'failures': 0, 'actualRegisteredItems': ['minecraft:wooden_sword'],
    'actualOriginalConsumedMaterialInstallationsBeforeAnyAuthority': 3, 'genuineOriginalRepairPayment': [3, 3, 2],
    'acceptedRuntimeReceipts': [str(path) for path in [paid_path, positive_path, restart_path, controls_path]],
    'originalFunctionThresholdsAndClassMaxDamage59Preserved': True,
    'genuineNonNullLeaseActualAttackCompleteOriginalDynamicSessionAndAuthenticatedSave': True,
    'completeSessionUsesEveryActualQuotedOriginalReturnNotArtificialFixedFour': True,
    'actualNewJvmSameEpochTwoToThreeWholeItemWearAndConsumedReplayDenial': True,
    'originalForeignCallbackExceptionAndMutationWithdrawalControlAccepted': True,
    'excludedFixtureAssumptionsPreserved': excluded,
    'configuredNineteenMeleeMatrixAccepted': False, 'productChanged': False, 'authorityWrittenByReflection': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False, 'installable': False, 'deployed': False,
    'inputHashes': hashes
}, ensure_ascii=False, indent=2) + '\n')
print(json.dumps({'report': output.name, 'configuredActualItems': 1, 'runtimePhases': 4, 'hashes': len(hashes), 'failures': 0}))
