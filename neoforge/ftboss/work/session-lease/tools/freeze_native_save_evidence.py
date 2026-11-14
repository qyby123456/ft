import hashlib
import json
import shutil
import subprocess
from pathlib import Path

WORK = Path(__file__).resolve().parents[1]
hashes = {}


def run_receipt(name):
    path = WORK / 'reports' / ('native-repair-runtime-' + name + '.json')
    result = json.loads(path.read_text(encoding='utf-8-sig'))
    terminal_path = Path(result['attempt']) / 'terminal308.json'
    terminal = json.loads(terminal_path.read_text(encoding='utf-8-sig'))
    assert result['exitCode'] == terminal['exitCode'] == result['probe']['failures'] == 0
    assert terminal['stopSent'] and terminal['completeSave']
    for source in [path, terminal_path]:
        hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
    return result, result['probe']['cases'][0]


saved, positive = run_receipt('repair-save-terminal-final-route')
faulted, failure = run_receipt('repair-save-io-failure')
for key in ['originalPlayerFileCarriesSaveIdentity', 'originalSavedEntryCarriesContinuationTicket',
            'signedSaveExpectedEqualsActualWholeItem', 'originalSerializedItemEqualsIndependentExpected',
            'secondOriginalSaveRotatesContinuationIdentity', 'secondOriginalSaveCommitted',
            'savePreservedActualLeaseAndGeneration', 'savePreservedActualWholeItem']:
    assert positive[key]
assert positive['signedSaveJournalStatus'] == positive['signedSaveEntryStatus'] == 'COMMITTED_ORIGINAL_SAVE'
for key in ['realOriginalPlayerFileDeleteShareFailureInjected', 'failedOriginalSavePlayerFileBytesUnchanged',
            'failedOriginalSaveDidNotCommitPendingTicket', 'successfulOriginalSaveAfterRealIoFailure',
            'ioFailureKeptActualLeaseGenerationAndWholeItem']:
    assert failure[key]
assert failure['failedOriginalSaveJournalStatus'] == failure['failedOriginalSaveEntryStatus'] == 'PENDING_ORIGINAL_SAVE'
candidate = WORK / 'reports/native-repair-candidate-c8790d91.json'
assert hashlib.sha256(candidate.read_bytes()).hexdigest() == '0def2df491a53adf8b00cd6533202b9494cf3c0ed8116b03468f1cf2c5c853d2'
exported = Path(saved['attempt']) / 'server/.mixin.out/class/net/minecraft/world/level/storage/PlayerDataStorage.class'
directory = WORK / 'reports/native-save-storage-provenance-c8790d91'
directory.mkdir(exist_ok=False)
snapshot = directory / 'PlayerDataStorage.class'
shutil.copyfile(exported, snapshot)
disassembly = directory / 'PlayerDataStorage.javap.txt'
with disassembly.open('wb') as stream:
    completed = subprocess.run(['C:/Program Files/Zulu/zulu-21/bin/javap.exe', '-p', '-c', str(snapshot)], stdout=stream, stderr=subprocess.STDOUT)
assert completed.returncode == 0
text = disassembly.read_text(encoding='utf-8', errors='replace')
for method in ['ftboss$nativeRepairSaveBody', 'ftboss$nativeRepairSaveEnterGuard',
               'ftboss$nativeRepairSaveEncodeGuard', 'ftboss$nativeRepairSaveReplaceGuard', 'ftboss$nativeRepairSaveExitGuard']:
    assert method in text
for source in [candidate, exported, snapshot, disassembly]:
    hashes[str(source)] = hashlib.sha256(source.read_bytes()).hexdigest()
result = {
    'candidate': 'native-repair-c8790d91',
    'actualOriginalSaveTerminalFocusedAccepted': True,
    'repeatedOriginalSaveTicketRotationFocusedAccepted': True,
    'actualWindowsDeleteShareFailureLeavesPendingOnly': True,
    'originalPlayerFilePreservedOnFailedReplacement': True,
    'successfulOriginalSaveAfterRealIoFailure': True,
    'wholeItemAndPhysicalLeaseGenerationUnchangedBySave': True,
    'playerStorageMixinExportCaptured': True,
    'loaderFinalBytesClaimed': False,
    'reloadSuccessorAccepted': False,
    'authenticatedMultiplayerAccepted': False,
    'fullLifecycleAccepted': False, 'authorityWrittenByReflection': False,
    'promoted': False, 'installable': False, 'deployed': False, 'inputHashes': hashes,
}
output = WORK / 'reports/native-save-terminal-focused-evidence.json'
with output.open('x', encoding='utf-8') as stream:
    json.dump(result, stream, ensure_ascii=False, indent=2)
    stream.write(chr(10))
print({'evidence': str(output), 'focusedSaveChecksPassed': True, 'reloadSuccessorAccepted': False})
