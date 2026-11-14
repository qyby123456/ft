import difflib
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

runtime=WORK/'build/repair-runtime-49fce7fc'
terminal=load(runtime/'terminal308.json')
debugger=load(runtime/'diagnostic-terminal.json')
assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent']
assert debugger['readOnly'] and not debugger['methodInvocations'] and not debugger['authorityWrites']
if debugger['exitCode']!=0:
    detach=load(runtime/'diagnostic-detach-receipt.json')
    assert detach['diagnosticIdentityVerified'] and detach['nativeIdentityVerified'] and not detach['nativeServerStoppedByThisRecovery']
previous=load(WORK/'reports/field-gate-diagnostic-archive-receipt-20261112.json')
assert digest(previous['archive'])==previous['sha256']=='52f3fcc8292b3ec9bf1a1ca54eb1ba348f21b7de824bdd5ac5914a260624aaa4'
formal=previous['formalInstancesUnchanged']
for entry in formal.values():assert digest(entry['path'])==entry['sha256']
trace=(runtime/'field-retirement-diagnostic.log').read_text(encoding='utf-8')
summary=[line for line in trace.splitlines() if line.startswith(('FIELD_RETIRE_BEFORE_WRITE','PLANNED_RANGED_RETURN','scope','fact','before','bindingBefore','FALSE_FIELD_GATE','prepared','issuer','sessionAtGate','gateOperation','player','carrier','FIELD_ADVANCE','SESSION_REVOKE','session.next','session.traceRevoked','step.phase','step.originalReturned','operation.kind','operation.key','STACK 0 ','STACK 1 ','STACK 2 ','STACK 3 ','STACK 4 ','STACK 5 ','OWNED_JVM'))]
report_path=WORK/'reports/native-field-retirement-diagnosis-613d4f6f.json'
assert not report_path.exists()
report={'previousArchive':previous['archive'],'previousArchiveSha256':previous['sha256'],'runtime':str(runtime),'runtimeFailures':terminal['probe']['failures'],'runtimeFailure':terminal['probe'].get('failure'),'nativeExitCode':0,'normalCompleteSave':True,'debuggerReadOnly':True,'targetMethodInvocations':False,'targetAuthorityWrites':False,'diagnosticSummary':summary,'productCandidateUnchanged':'work/session-lease/reports/native-repair-private-fingerprint-candidate-613d4f6f.json','originalAssertionsPreserved':True,'formalInstancesUnchanged':formal,'failedWorldExcludedAsRestartSeed':True,'fullLifecycleAccepted':False,'goalStatus':'active','installable':False,'deployed':False}
report.update({'debuggerExitCode':debugger['exitCode'],'diagnosticAccepted':False,'firstRetirementNotCaptured':True,'interruptedForStartupBreakpointOverhead':debugger['exitCode']!=0})
report_path.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
patch='*** Begin Patch\n'
for rel in ['WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json']:
    path=ROOT/rel;before=path.read_text(encoding='utf-8');value=json.loads(before)
    value.update(activeRuntimePid=None,activeRuntimePids=[],activeSupervisor=None,nativePassiveRootRuntimePid=None,nativePassiveRootSupervisor=None,activeWorkstream='signed-session-domain-retirement-diagnostic-overhead-recovered',latestNativeFieldRetirementEvidence=str(report_path),currentGoalStatus='active',formalInstancePolicy='read_only_no_deployment',fullSignedSessionAccepted=False,installable=False,deployed=False)
    if rel!='WRITE_OWNER.json':value['status']='native_field_retirement_diagnostic_terminal_acceptance_pending'
    after=json.dumps(value,ensure_ascii=False,indent=2)+'\n';delta=list(difflib.unified_diff(before.splitlines(True),after.splitlines(True),n=2))
    patch+='*** Update File: '+rel+'\n'+''.join('@@\n' if line.startswith('@@') else line for line in delta[2:])
patch+='*** End Patch\n'
subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
files=[report_path,Path(__file__),WORK/'tools/prepare_native_field_retirement_diagnostic.py',WORK/'tools/run_native_field_retirement_613d4f6f.py',WORK/'tests/native-field-retirement-diagnostic-613d4f6f/NativeFieldRetirementDiagnostic.java',runtime/'server/actual-storage-view308.json',ROOT/'WRITE_OWNER.json',WORK/'CURRENT_INCREMENTAL_BATCH.json']
files.extend(path for path in runtime.iterdir() if path.is_file())
files.extend(path for path in (WORK/'build/native-field-retirement-diagnostic-613d4f6f').iterdir() if path.is_file())
files=sorted(set(path.resolve() for path in files))
manifest={path.relative_to(ROOT).as_posix():{'sha256':digest(path),'bytes':path.stat().st_size} for path in files}
archive_path=WORK/'archives/field-retirement-diagnostic-613d4f6f-20261112.zip'
assert not archive_path.exists()
with zipfile.ZipFile(archive_path,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
    for path in files:archive.write(path,path.relative_to(ROOT).as_posix())
    archive.writestr('MANIFEST.json',json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
    archive.writestr('RESTORE.md','Evidence-only supplement; NOT INSTALLABLE. Restore after the hash-verified field-gate-diagnostic-613d4f6f-20261112.zip, session-revoke-diagnostic-613d4f6f-20261112.zip and private-read-construction-613d4f6f-20261112.zip; the latter references the frozen paused 73d460d2 baseline. Runtime world and historical dependencies remain retained externally. Failed worlds are not valid restart seeds. No deployment, acceptance promotion, cleanup or writer handoff.\n')
with zipfile.ZipFile(archive_path) as archive:
    assert len(archive.namelist())==len(manifest)+2
    for name,entry in manifest.items():assert hashlib.sha256(archive.read(name)).hexdigest()==entry['sha256']
receipt_path=WORK/'reports/field-retirement-diagnostic-archive-receipt-20261112.json'
assert not receipt_path.exists()
receipt={'archive':str(archive_path),'sha256':digest(archive_path),'manifestFiles':len(manifest),'allMembersReadBackAndHashVerified':True,'previousArchive':previous['archive'],'previousArchiveSha256':previous['sha256'],'formalInstancesUnchanged':formal,'goalStatus':'active','fullLifecycleAccepted':False,'deployed':False}
receipt_path.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'archive':str(archive_path),'sha256':receipt['sha256'],'files':len(manifest),'runtimeFailures':terminal['probe']['failures'],'goalStatus':'active','deployed':False}),flush=True)
