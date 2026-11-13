import hashlib
import json
from pathlib import Path


work = Path(__file__).resolve().parents[1]
tag = '9d1607e8'
hashes = {}


def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))


def retain(path, expected=None):
    path = Path(path)
    actual = hashlib.sha256(path.read_bytes()).hexdigest()
    assert expected is None or actual == expected, str(path)
    assert str(path) not in hashes or hashes[str(path)] == actual, str(path)
    hashes[str(path)] = actual


def inputs(path):
    value = load(path)
    retain(path)
    for name, expected in value['inputHashes'].items():
        retain(name, expected)
    return value


def runtime(path):
    value = inputs(path)
    attempt = Path(value['attempt'])
    terminal = load(attempt / 'terminal308.json')
    assert value['exitCode'] == value['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    assert [case['id'] for case in value['probe']['cases']] == ['minecraft:crossbow']
    assert not value['probe']['authorityWrittenByReflection'] and not value['probe']['authenticatedMultiplayerAccepted']
    for name in ['terminal308.json', 'runtime308.log', 'live-process308.json', 'server/actual-storage-view308.json', 'restart-world-seed.json', 'compile-command308.json', 'compile308.log']:
        retain(attempt / name)
    world = attempt / 'server/world'
    for path in [world / 'level.dat', world / 'data/ftboss_native_weapon_wear.dat', *sorted((world / 'ftboss-authority').rglob('*.bin')), *sorted((world / 'playerdata').glob('*.dat'))]:
        retain(path)
    assert not [path for directory in ['ftboss-authority', 'playerdata'] for path in (world / directory).rglob('*') if path.is_file() and path.suffix in ['.tmp', '.pending']]
    return value['probe']['cases'][0]


for name in ['native-ranged-use-head-delta-f4f2deae.json', 'native-crossbow-component-delta-76bc665d.json', 'native-crossbow-factory-delta-89f2b629.json', 'native-crossbow-noop-delta-9d1607e8.json']:
    delta = inputs(work / 'reports' / name)
    assert delta['failures'] == 0 and delta['classes'] == 1381 and delta['sources'] == 716 and delta['guards'] == 208 and not delta['newClasses']
for name in ['native-ranged-use-preparation-89f2b629.json', 'native-ranged-use-preparation-' + tag + '.json', 'native-crossbow-post-combat-preparation-' + tag + '.json', 'native-crossbow-foreign-control-preparation-' + tag + '.json']:
    inputs(work / 'reports' / name)
positive_paths = [work / ('reports/native-repair-runtime-paid-crossbow-combat-' + variant + '.json') for variant in ['89f2b629', tag]]
positives = [runtime(path) for path in positive_paths]
for case in positives:
    assert case['genuineNonNullLeaseAndGenerationBeforeAttack']
    assert case['actualOriginalUseResult'] == case['actualOriginalFireUseResult'] == 'CONSUME' and case['actualUseStarted']
    assert case['ammoBefore'] == 16 and case['ammoAfterCharge'] == 15 and case['chargedProjectilesPresent']
    assert not any(case[key] for key in ['generationRetiredAfterUse', 'generationRetiredAfterCharge', 'generationRetiredAfterFire', 'generationRetiredAfterAttack'])
    assert case['healthAfter'] < case['healthBefore'] and case['wearBefore'] == 0 and case['wearAfter'] == 1
    assert case['sameLeaseAndGenerationAfterAttack'] and case['actualOriginalPostCombatSaveAuthenticatedCommitted']
    assert len(case['actualProjectileFlights']) == len(case['publications']) == 1
    flight = case['actualProjectileFlights'][0]
    assert all(flight[key] for key in ['snapshotPresent', 'snapshotValid', 'repairEpochPresent', 'signaturePresent', 'paidCarrierPresent'])
    assert flight['purpose'] == 'NATIVE_PROJECTILE' and flight['nativeEntry'] == 'CROSSBOW'
    publication = case['publications'][0]
    assert all(publication[key] for key in ['traceComplete', 'preparedCommitted', 'prepared_sealed', 'prepared_bound', 'session_coverageInstalled'])
    assert not publication['prepared_retired'] and not publication['session_traceRevoked']
assert positives[1]['originalEarlyReleasePreservedInputImageAmmoAndAuthority']
restart_path = work / ('reports/native-repair-runtime-crossbow-post-combat-restart-' + tag + '.json')
restart = runtime(restart_path)
assert all(restart[key] for key in ['originalWholeItemAndWearCoherent', 'genuineNonNullLeaseAndGeneration', 'sameEpochGenerationThreeToFour', 'consumedReplayDeniedWithoutChangingFirstSuccessor', 'actualOriginalGenerationFourSaveCommitted'])
control_path = work / ('reports/native-repair-runtime-crossbow-foreign-control-' + tag + '.json')
control = runtime(control_path)
assert all(control[key] for key in ['originalEarlyReleasePreservedInputImageAmmoAndAuthority', 'outsideOriginalCallbacksPreservedCurrentInputImageAndAuthority', 'foreignNativeUseOriginalReturnedOnce', 'foreignNativeUseAuthorityRetired', 'foreignNativeUseNoSignedCarrier', 'privateScopesClosedWithoutWrites'])
failed_paths = []
for variant in ['2e5c6034', 'f4f2deae', 'b18a804d', '428a86f9', '76bc665d']:
    path = work / ('reports/native-repair-runtime-paid-crossbow-combat-' + variant + '.json')
    failed = load(path)
    terminal_path = Path(failed['attempt']) / 'terminal308.json'
    terminal = load(terminal_path)
    assert failed['exitCode'] == 0 and failed['probe']['failures'] == 1 and terminal['stopSent'] and terminal['completeSave']
    retain(path)
    for name in ['terminal308.json', 'runtime308.log', 'server/actual-storage-view308.json']:
        retain(Path(failed['attempt']) / name)
    failed_paths.append(str(path))
retain(work / 'reports/crossbow-original-use-charge-fire-b18a804d.javap.txt')
retain(work / 'tools/prepare_native_crossbow_post_combat_restart.py')
retain(work / 'tools/prepare_native_melee_post_combat_restart.py')
retain(work / ('reports/native-crossbow-final-process-check-' + tag + '.json'))
assert not load(work / ('reports/native-crossbow-final-process-check-' + tag + '.json'))['remainingOwnedJavaProcesses']
retain(Path(__file__))
output = work / ('reports/native-crossbow-paid-signed-combat-focused-evidence-' + tag + '.json')
assert not output.exists()
output.write_text(json.dumps({'candidate': str(work / ('reports/native-repair-ranged-use-candidate-' + tag + '.json')), 'acceptedRuntimeReceipts': [str(path) for path in positive_paths + [restart_path, control_path]], 'excludedFailedRuntimeReceipts': failed_paths, 'actualOriginalUseChargeReleaseFireArrowTickOnGenuinePaidLease': True, 'completeLiveSignedSessionAndOriginalWearOne': True, 'earlyOriginalReleaseNoopPreservesSameInputImageAmmoAndAuthority': True, 'actualOriginalPostCombatSaveAndNewProcessGenerationThreeToFour': True, 'consumedReplayDeniedWithoutChangingLiveSuccessor': True, 'outsideOriginalCallbacksNoopAndForeignNativeUseDelegatesOriginalWithoutSignedAuthority': True, 'retainedCrossbowVariantsAccepted': False, 'enchantmentsMultishotFireworksAccepted': False, 'authorityWrittenByReflection': False, 'fullLifecycleAccepted': False, 'authenticatedMultiplayerAccepted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes}, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'hashes': len(hashes), 'focusedFailures': 0, 'completeMigration': False}))
