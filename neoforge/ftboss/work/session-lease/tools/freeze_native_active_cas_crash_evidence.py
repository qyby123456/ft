import hashlib
import json
import uuid
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()


def runtime(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    for source, wanted in result['inputHashes'].items():
        assert digest(Path(source)) == wanted, source
        hashes[source] = wanted
    for source in [path, terminal_path]:
        hashes[str(source)] = digest(source)
    return result, terminal


crash, terminal = runtime('repair-active-cas-crash-capability-monitor')
assert crash['exitCode'] == terminal['exitCode'] == 86
assert crash['deliberateCrashAtVerifiedActiveCasCut'] and terminal['deliberateCrashAtVerifiedActiveCasCut']
assert not crash['normallySavedWorldClaimed'] and not terminal['stopSent'] and not terminal['completeSave']
assert crash['helperExitCode'] == terminal['helperExitCode'] == 0
cut = crash['probe']
assert cut['failures'] == 0 and cut['plannedExitCode'] == 86
for key in ['originalReservationAuthenticated', 'originalActivePredecessorCasActuallyPersisted', 'continuationStillReservedAndUnconsumed',
            'actualLoadedItemHasNoPhysicalLeaseOrGeneration', 'serverThreadBlockedInOriginalPublishNativeReload',
            'originalConsumeFileLockWaitObservedBeforeMonitorAcquisition', 'authenticatedReservedDocumentBytesUnchangedAtCut']:
    assert cut[key]
assert cut['heldOriginalPublicationCapabilityMonitor'] == 'net.farmingtales.ftboss.item.NativeCuriosProviderFields260'
assert cut['successorGeneration'] == cut['predecessorGeneration'] + 1
assert cut['monitorHolderThreadId'] == cut['observedLockOwnerId'] != cut['serverThreadId']
assert any('NativeCuriosProviderFields260.repairVersion' in frame for frame in cut['serverStack'])
assert any('OwnerRepairGenerationAuthority.publishNativeReload' in frame for frame in cut['serverStack'])
assert any('OwnerLineageRevocationStore.consumeNativeReload' in frame for frame in cut['preMonitorFileLockStack'])
assert any('FileChannelImpl.lock' in frame for frame in cut['preMonitorFileLockStack'])
attempt = Path(crash['attempt'])
helper_terminal = json.loads((attempt / 'helper-terminal.json').read_text(encoding='utf-8'))
helper_ready = json.loads((attempt / 'server/lineage-lock-ready.json').read_text(encoding='utf-8'))
assert helper_terminal['exitCode'] == 0 and helper_terminal['releasedOriginalLineageLock']
assert helper_ready['owner'] == cut['owner'] and helper_ready['weapon'] == cut['weapon'] and helper_ready['epoch'] == cut['epoch']
assert helper_ready['generation'] == cut['predecessorGeneration']
document_owner = uuid.UUID(bytes=hashlib.md5(('ftboss/native-repair-continuation/v1/' + cut['owner']).encode('utf-8')).digest(), version=3)
document = attempt / 'server/world/ftboss-authority' / str(document_owner) / 'repair-tasks-v1.bin'
assert digest(document) == cut['authenticatedReservedDocumentSha256']
assert digest(attempt / 'server/world/playerdata' / (cut['owner'] + '.dat')) == cut['originalSavedPlayerSha256']
for source, wanted in crash['crashWorldHashes'].items():
    assert digest(Path(source)) == wanted
    hashes[source] = wanted
for source in [attempt / 'helper-terminal.json', attempt / 'helper-runtime.log', attempt / 'server/active-cas-crash-cut.json']:
    hashes[str(source)] = digest(source)
resumed, resumed_terminal = runtime('repair-reload-after-active-cas-crash')
assert resumed['exitCode'] == resumed_terminal['exitCode'] == resumed['probe']['failures'] == 0
assert resumed_terminal['stopSent'] and resumed_terminal['completeSave']
probe = resumed['probe']
for key in ['actualNewServerBoot', 'originalCrashSavedTicketWholeItemAndWearCoherent', 'originalActiveCasSuccessorAlreadyPersistedBeforeLoad',
            'reservedCrashContinuationAndSecondReplayGrantedNoAuthority', 'continuationReservedNoReissue',
            'originalAdvancedActiveLineageBytesUnchanged', 'originalReservedDocumentUnchanged',
            'originalPostCrashOpenDidNotReopenClaimOrAdmitAuthority']:
    assert probe[key]
assert probe['originalTicketGeneration'] == cut['predecessorGeneration']
assert probe['activeCasSuccessorGeneration'] == cut['successorGeneration']
failures = []
for name in ['repair-active-cas-crash', 'repair-active-cas-crash-cached']:
    failed, failed_terminal = runtime(name)
    assert failed['exitCode'] == failed_terminal['exitCode'] == 0 and failed['probe']['failures'] == 1
    assert failed_terminal['stopSent'] and failed_terminal['completeSave']
    assert failed['helperExitCode'] == 0 and not failed['deliberateCrashAtVerifiedActiveCasCut']
    failures.append(str(WORK / 'reports' / ('native-repair-runtime-' + name + '.json')))
hashes[str(Path(__file__))] = digest(Path(__file__))
result = {
    'candidate': 'native-repair-2ca083af',
    'verifiedGenuineActivePredecessorCasThenPrePhysicalPublicationProcessInterruption': True,
    'deliberateCrashExitCode': 86,
    'crashWorldNormallySavedClaimed': False,
    'externalOriginalFileLockWaitBeforeCapabilityMonitorAcquisitionWitnessed': True,
    'sameEpochGenerationsAtCut': [cut['predecessorGeneration'], cut['successorGeneration']],
    'actualOriginalPublicationBlockedAndPhysicalLeaseGenerationAbsent': True,
    'authenticatedReservedDocumentBytesPreservedAtCut': True,
    'actualNewProcessCrashCheckpointAndReplayDoNotReissueOrConsumeAuthority': True,
    'actualPostCrashOpenDoesNotReopenClaimOrAdmitAuthority': True,
    'originalAdvancedActiveLineageAndReservedDocumentUnchangedAfterLoadsAndOpen': True,
    'newProcessWholeItemAndWearRemainCoherent': True,
    'externalHelperTerminatedNormally': True,
    'preservedUnsuccessfulFixtureRuns': failures,
    'authorityWrittenByReflection': False, 'testMixinUsed': False,
    'authenticatedMultiplayerAccepted': False, 'fullLifecycleAccepted': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-active-cas-crash-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'verifiedActiveCasCrashAndNewBootNoReissuePassed': True, 'inputCount': len(hashes)})
