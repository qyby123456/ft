import hashlib
import json
import shutil
import subprocess
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def accepted(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    assert result['exitCode'] == terminal['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    for source, wanted in result['inputHashes'].items():
        assert digest(Path(source)) == wanted
        hashes[source] = wanted
    for source in [path, terminal_path]:
        hashes[str(source)] = digest(source)
    case = result['probe']['cases'][0]
    for key in ['nonNullLeaseAfterActualReload', 'repairGenerationAfterActualReload', 'originalContinuationTicketConsumed',
                'reloadedSuccessorKeptOriginalEpoch', 'replayedOriginalSaveHasSameWholeWeapon',
                'replayedConsumedTicketGrantedNoLease', 'replayKeptFirstGenuineSuccessorLive',
                'reloadedPaidWeaponSavedAfterSignedCombat', 'attackReturned']:
        assert case[key], (name, key)
    publications = case['publications']
    assert len(publications) == 1 and publications[0]['traceComplete'] and not publications[0]['prepared_retired']
    assert case['reloadedSuccessorLeaseGeneration'] == case['originalSavedLeaseGeneration'] + 1
    return result, case


first, pristine = accepted('repair-reload-successor-source')
second, worn = accepted('repair-reload-worn-successor')
assert pristine['originalSavedLeaseGeneration'] == 0 and pristine['reloadedSuccessorLeaseGeneration'] == 1
assert worn['originalSavedLeaseGeneration'] == 1 and worn['reloadedSuccessorLeaseGeneration'] == 2
assert worn['loadedWholeImageMatchesSignedContinuation'] and worn['loadedWornSuccessorAfterEarlierSignedCombat']
candidate = WORK / 'reports/native-repair-candidate-2ca083af.json'
assert digest(candidate) == '842ff9f8dbc7d746fa9979595e4172ff16c0279fc92bb91fa0c9b92c7652aa74'
build = Path(json.loads(candidate.read_text(encoding='utf-8-sig'))['attempt'])
for name in ['OwnerStatueWearAdmission', 'OwnerStatueWearBoundary', 'NativeFoodWeaponWear', 'OwnerNativeSemanticAuthority',
             'OwnerLineageRevocationStore', 'OwnerRepairGenerationAuthority', 'NativeCuriosProviderFields260', 'NativeMapPutLateProofExtension']:
    for source in [build / ('sources/net/farmingtales/ftboss/item/' + name + '.java'),
                   build / ('complete-classes/net/farmingtales/ftboss/item/' + name + '.class')]:
        hashes[str(source)] = digest(source)
directory = WORK / 'reports/native-reload-provenance-2ca083af'
directory.mkdir(exist_ok=False)
for qualified, methods in [
    ('world/level/storage/PlayerDataStorage', ['ftboss$nativeRepairSaveBody', 'ftboss$nativeRepairSaveExitGuard']),
    ('world/entity/player/Inventory', ['ftboss$statueWearLoadExitGuard', 'ftboss$statueWearBindGuard'])
]:
    exported = Path(first['attempt']) / ('server/.mixin.out/class/net/minecraft/' + qualified + '.class')
    snapshot = directory / (qualified.rsplit('/', 1)[-1] + '.class')
    shutil.copyfile(exported, snapshot)
    disassembly = snapshot.with_suffix('.javap.txt')
    with disassembly.open('wb') as stream:
        completed = subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javap.exe', '-p', '-c', str(snapshot)], stdout=stream, stderr=subprocess.STDOUT)
    assert completed.returncode == 0
    text = disassembly.read_text(encoding='utf-8', errors='replace')
    assert all(method in text for method in methods)
    for source in [exported, snapshot, disassembly]:
        hashes[str(source)] = digest(source)
for source in [candidate, Path(__file__)]:
    hashes[str(source)] = digest(source)
result = {
    'candidate': 'native-repair-2ca083af',
    'actualNewProcessNonNullPaidLeaseSuccessorFocusedAccepted': True,
    'originalSuccessfulSaveAndAuthenticatedOneUseLoad': True,
    'twoOrderlyRestartsSameEpochGenerations': [0, 1, 2],
    'secondRestartUsesActualWornItemAndSignedContinuation': True,
    'consumedOriginalSaveReplayDeniesSecondAuthorityAndPreservesFirstSuccessor': True,
    'completeOriginalSignedSessionAfterBothRestarts': True,
    'originalPlayerSaveAfterBothSignedCombatTransitions': True,
    'originalParseSourceIdentityAndEarliestImmutableQuoteBoundToIndependentWholeItem': True,
    'rawEscapedMapTrustRelaxed': False,
    'postMixinExportsCaptured': True,
    'loaderFinalBytesClaimed': False,
    'sameProcessLogoutLoginAccepted': False,
    'forgedTicketAndInactiveLineageAccepted': False,
    'authenticatedMultiplayerAccepted': False,
    'fullLifecycleAccepted': False,
    'authorityWrittenByReflection': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-reload-successor-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'twoRestartSuccessorsAndSignedSessionsPassed': True, 'inputCount': len(hashes)})
