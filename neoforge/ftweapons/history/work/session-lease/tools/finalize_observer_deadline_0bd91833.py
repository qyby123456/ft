import hashlib
import json
import subprocess
from pathlib import Path

work = Path(__file__).resolve().parents[1]
root = work.parents[1]
attempt = work / 'build/repair-runtime-0bd91833'

def load(path):
    return json.loads(path.read_text(encoding='utf-8-sig'))

def digest(path):
    return hashlib.sha256(path.read_bytes()).hexdigest()

process = subprocess.run(['powershell.exe', '-NoProfile', '-Command', "if (Get-Process -Id 51268 -ErrorAction SilentlyContinue) { exit 1 }"], capture_output=True)
assert process.returncode == 0, 'Original observed process is still live; do not restart'
probe_path = attempt / 'server/actual-storage-view308.json'
probe = load(probe_path)
assert probe['failures'] == 1 and len(probe['cases']) == 1
case = probe['cases'][0]
assert case['genuineNonNullLeaseAndGenerationBeforeAttack']
assert case['foreignDirectEntryDelegatesExactlyOnceWithoutAuthorityOrDataMutation']
assert case['foreignDirectEntryPreservesOriginalCallbackExceptionIdentity']
assert case['sameOriginalSourceAndSignedSnapshotDeniedOutsideActualHurtScope']
assert len(case['publications']) == 1 and case['publications'][0]['traceComplete']
assert case['generationRetiredAfterAttack'] and not case['publications'][0]['prepared_retired']
log_path = attempt / 'runtime308.log'
log = log_path.read_text(encoding='utf-8', errors='replace')
assert 'Completed-observation helper requests original MinecraftServer.halt(false)' in log
assert 'All dimensions are saved' in log and 'Stopping server' in log
inputs_path = attempt / 'inputs-before308.json'
inputs = load(inputs_path)
for name, expected in inputs.items():
    assert digest(Path(name)) == expected, name
hashes = dict(inputs)
for path in [probe_path, log_path, inputs_path, attempt / 'observation-deadline308.json',
             attempt / 'normal-stop-helper.jar', work / 'tests/normal-stop-helper/NormalStopAgent.java',
             work / 'tests/normal-stop-helper/NormalStopAttach.java', Path(__file__)]:
    hashes[str(path)] = digest(path)
authority = attempt / 'server/world/ftboss-authority'
playerdata = attempt / 'server/world/playerdata'
temporary = [str(path.relative_to(attempt)) for directory in [authority, playerdata]
             for path in directory.rglob('*') if path.is_file() and (path.name.endswith('.tmp') or path.name.endswith('.pending'))]
authority_files = list(authority.rglob('*.bin'))
for path in authority_files:
    hashes[str(path)] = digest(path)
report = {
    'attempt': str(attempt), 'pid': 51268, 'originalSupervisorObservationDeadline': True,
    'originalSupervisorExitCode': 1, 'javaExitCode': None, 'javaExitCodeUnavailableAfterSupervisorEnded': True,
    'originalProcessVerifiedAbsentAfterNormalStop': True,
    'probeWrittenCompletelyBeforeNormalStopHelper': True, 'probe': probe,
    'nativeMeleeFullSessionObservedButCompleteCombatRejected': True,
    'actualGenerationRetiredAfterOriginalPostDamageCopy': True,
    'actualRawAliasEscapeObservedBeforeOriginalPostDamageCopy': True,
    'normalShutdownRequestedThroughOriginalServerExecuteAndHalt': True, 'completeNormalWorldSave': True,
    'helperAttachedOnlyAfterObservationAndOnlyRequestsNormalShutdown': True,
    'helperRedefinesOrRetransformsClasses': False,
    'helperRunNotEligibleForEarlierNoAgentJVMDefinedFactoryProof': True,
    'restartedBecauseObservationExpired': False, 'forceKilled': False,
    'temporaryAuthorityOrPlayerFilesAfterSave': temporary,
    'temporaryFileAbsenceIsNotProofAllAuthenticatedTransactionsCommitted': True,
    'authenticatedAuthorityDocumentsResetOrEdited': False,
    'authorityDocumentsFrozenAfterNormalShutdown': len(authority_files),
    'fullMeleeCombatAccepted': False, 'authenticatedMultiplayerAccepted': False,
    'installable': False, 'deployed': False, 'inputHashes': hashes
}
output = work / 'reports/native-melee-observer-deadline-0bd91833.json'
assert not output.exists()
output.write_text(json.dumps(report, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
owner = load(root / 'WRITE_OWNER.json')
assert owner['activeWriterThreadId'] == '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
owner['lastRuntimePid'] = 51268
owner['lastRuntimeTerminal'] = True
owner['ownedJavaOrPythonProcessesRemaining'] = False
owner['latestNativeRepairPrototype'] = 'work/session-lease/reports/native-repair-melee-ingress-candidate-25cdcaa5.json'
owner['latestNativeRepairPrototypeSha256'] = digest(work / 'reports/native-repair-melee-ingress-candidate-25cdcaa5.json')
owner['latestNativeMeleeDeadlineEvidence'] = output.relative_to(root).as_posix()
owner['nativeContinuationStatus'] = '25cdcaa5 actual sword signed native melee Session traceComplete true; post-damage raw alias and original ItemStack.copy retire generation so full melee acceptance remains false; supervised deadline preserved, same process normally saved and stopped, no restart; same sole writer continues'
(root / 'WRITE_OWNER.json').write_text(json.dumps(owner, ensure_ascii=False, indent=2) + '\n', encoding='utf-8')
print(json.dumps({'originalPidTerminal': True, 'completeNormalWorldSave': True, 'fullMeleeCombatAccepted': False, 'temporaryAuthorityOrPlayerFiles': len(temporary), 'verifiedHashes': len(hashes), 'sameWriterRetained': True}))
