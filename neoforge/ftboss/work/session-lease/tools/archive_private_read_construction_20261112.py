import difflib
import hashlib
import json
import subprocess
import zipfile
from datetime import datetime,timezone
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
THREAD='01a1c43e-ae80-7390-b604-b43b4ab53f2f'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']==THREAD
preflight=load(WORK/'reports/goal-reactivation-after-stage11-20261112/preflight.json')
formal=preflight['formalInstancesBefore']
for entry in formal.values():assert digest(entry['path'])==entry['sha256']
runtime=WORK/'build/repair-runtime-4f6738ff'
terminal=load(runtime/'terminal308.json')
assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
diagnostic=WORK/'build/repair-runtime-c86bee3a'
diagnostic_terminal=load(diagnostic/'terminal308.json')
assert diagnostic_terminal['exitCode']==0 and diagnostic_terminal['completeSave']
debugger=load(diagnostic/'diagnostic-terminal.json')
assert debugger=={'exitCode':0,'readOnly':True,'methodInvocations':False,'authorityWrites':False}
trace=(diagnostic/'private-image-failure.log').read_text(encoding='utf-8')
assert 'PRIVATE_IMAGE_LEDGER_RETURN result=true' in trace and 'fact0.key=Encoding' in trace
candidate=WORK/'reports/native-repair-private-fingerprint-candidate-613d4f6f.json'
assert digest(candidate)=='59051d1e1377b954278e76e9deadc54d433e4e8f9f03f16ea113dcde037be4c8'
report=WORK/'reports/private-read-construction-progress-20261112.json'
assert not report.exists()
progress={'createdAt':datetime.now(timezone.utc).isoformat(),'goalStatus':'active','formalInstancePolicy':'read_only_no_deployment','formalInstanceHashesUnchanged':formal,'candidate':str(candidate),'candidateSha256':digest(candidate),'timerWholeImageAdvanceActuallyReturnedTrue':True,'diagnosticFirstFailure':'Unpublished material fingerprint Encoding field was written through observed CompoundTag.putString inside original RANGED_SAVE','diagnosticNativeExitCode':0,'diagnosticCompleteSave':True,'diagnosticDebuggerReadOnly':True,'mapGuardUnchanged':True,'privateFingerprintOnlyReusesExistingRestrictedFactory':True,'privateTimerBoundaryExistingMethodsPreserved':669,'privateFingerprintBoundaryExistingMethodsPreserved':13,'currentFullNineteenRuntime':str(runtime),'currentRuntimeFailures':terminal['probe']['failures'],'currentRuntimeFailure':terminal['probe'].get('failure'),'currentRuntimeNativeExitCode':0,'currentRuntimeCompleteSave':True,'originalFullSessionAssertionsPreserved':True,'onlyAdditionalEarlyFailureObservation':True,'failedWorldsExcludedAsRestartSeeds':True,'wholeMigrationAccepted':False,'fullSignedLifecycleAccepted':False,'installable':False,'deployed':False,'sourcePreparationFailuresRetained':['private-image diagnostic marker mismatch before any file creation','private-fingerprint builder pattern count mismatch after one product file staged; resumed suffix only','first ASM proof needed exact discarded Tag return normalization; original proof and failure retained'],'parentCheckpointExternal':'work/session-boss/archives/weapons-paused-73d460d2-20261112','parentCheckpointArchiveSha256':preflight['pausedCheckpointArchiveSha256']}
report.write_text(json.dumps(progress,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
patch='*** Begin Patch\n'
for rel in ['WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json']:
    path=ROOT/rel;before=path.read_text(encoding='utf-8');value=json.loads(before)
    value.update(activeRuntimePid=None,activeRuntimePids=[],activeSupervisor=None,nativePassiveRootRuntimePid=None,nativePassiveRootSupervisor=None,activeWorkstream='signed-session-full-nineteen-private-read-construction',latestPrivateReadConstructionEvidence=str(report),latestNativePrivateImageRuntimeAttempt=str(runtime),currentGoalStatus='active',formalInstancePolicy='read_only_no_deployment',fullSignedSessionAccepted=False,installable=False,deployed=False)
    if rel!='WRITE_OWNER.json':value['status']='private_read_construction_runtime_terminal_acceptance_pending'
    after=json.dumps(value,ensure_ascii=False,indent=2)+'\n'
    delta=list(difflib.unified_diff(before.splitlines(True),after.splitlines(True),n=2))
    patch+='*** Update File: '+rel+'\n'+''.join('@@\n' if line.startswith('@@') else line for line in delta[2:])
patch+='*** End Patch\n'
subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
files=set()
def add(path):
    path=Path(path)
    if path.is_file():files.add(path.resolve())
def tree(path):
    path=Path(path)
    assert path.is_relative_to(ROOT) and path.exists(),path
    for entry in path.rglob('*'):
        if entry.is_file() and '__pycache__' not in entry.parts:add(entry)
for name in ['proposal-private-timer-expectation','proposal-private-timer-expectation-v2','proposal-private-fingerprint-composition','tests/native-map-rejection-diagnostic-20261112','tests/private-image-failure-diagnostic-acf29373','tests/private-timer-image-foreign-guard','tests/private-fingerprint-foreign-guard','reports/goal-reactivation-after-stage11-20261112','build/native-repair-acf29373','build/native-repair-613d4f6f']:
    tree(WORK/name)
for prefix in ['reactivate_goal_after_boss_stage11','prepare_native_map_rejection_diagnostic','run_native_map_rejection_73d460d2','prepare_private_timer_expectation_image','build_private_timer_expectation_image','prepare_private_timer_full_nineteen','run_native_full_nineteen_private_image_acf29373','prepare_private_image_failure_diagnostic','run_native_private_image_failure_acf29373','prepare_private_fingerprint_composition','build_private_fingerprint_composition','prepare_private_fingerprint_full_nineteen','run_native_full_nineteen_private_fingerprint_613d4f6f','archive_private_read_construction_20261112']:
    for path in (WORK/'tools').glob(prefix+'*.py'):add(path)
for pattern in ['*private*image*.json','*private*fingerprint*.json','*map-rejection-73d460d2.json','*private*console.log','private-read-construction-progress-20261112.json']:
    for path in (WORK/'reports').glob(pattern):add(path)
for name in ['PrivateTimerImageBoundaryProof.java','PrivateFingerprintCompositionBoundaryProof.java','native-full-nineteen-signed-dd1ad841/PositiveWeapon436.java']:
    add(WORK/'tests'/name)
for name in ['repair-runtime-061b8ac0','repair-runtime-b9b68767','repair-runtime-c86bee3a','repair-runtime-4f6738ff','native-repair-d3588394']:
    folder=WORK/'build'/name
    for path in folder.iterdir():
        if path.is_file():add(path)
    if (folder/'server').exists():
        for path in (folder/'server').glob('actual-*.json'):add(path)
for name in ['AGENTS.md','WRITE_OWNER.json','WRITE_OWNER.claim','work/session-lease/CURRENT_INCREMENTAL_BATCH.json']:add(ROOT/name)
manifest={path.relative_to(ROOT).as_posix():{'sha256':digest(path),'bytes':path.stat().st_size} for path in sorted(files)}
destination=WORK/'archives/private-read-construction-613d4f6f-20261112.zip'
destination.parent.mkdir(exist_ok=True)
assert not destination.exists()
restore='Evidence-only checkpoint; NOT INSTALLABLE; no deployment. Restore as an overlay into a separate C workspace. First restore the hash-verified paused 73d460d2 checkpoint referenced by reports/private-read-construction-progress-20261112.json. Historical D inputs, paid19 seed world, Java/runtime libraries and formal instances remain external read-only dependencies. Candidate 613d4f6f is compile/ASM and directed-runtime evidence only; full original lifecycle/client/authenticated multiplayer acceptance is incomplete. Failed runtime worlds must never be used as restart seeds. Source assertions remain intact; only an early read-only failure observation was added. No files or processes are removed by this archiver.\n'
with zipfile.ZipFile(destination,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
    for path in sorted(files):archive.write(path,path.relative_to(ROOT).as_posix())
    archive.writestr('MANIFEST.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
    archive.writestr('RESTORE.md',restore)
with zipfile.ZipFile(destination) as archive:
    assert len(archive.namelist())==len(manifest)+2
    for name,entry in manifest.items():assert hashlib.sha256(archive.read(name)).hexdigest()==entry['sha256']
for path in files:assert digest(path)==manifest[path.relative_to(ROOT).as_posix()]['sha256']
receipt={'archive':str(destination),'sha256':digest(destination),'manifestFiles':len(manifest),'allMembersReadBackAndHashVerified':True,'sourceFilesUnchangedDuringArchive':True,'formalInstancesUnchanged':formal,'runtimeFailures':terminal['probe']['failures'],'fullLifecycleAccepted':False,'goalStatus':'active','installable':False,'deployed':False,'externalDependenciesExplicit':True,'scratchRetained':True}
receipt_path=WORK/'reports/private-read-construction-archive-receipt-20261112.json'
assert not receipt_path.exists()
receipt_path.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'archive':str(destination),'sha256':receipt['sha256'],'files':len(manifest),'runtimeFailures':receipt['runtimeFailures'],'goalStatus':'active','deployed':False}),flush=True)
