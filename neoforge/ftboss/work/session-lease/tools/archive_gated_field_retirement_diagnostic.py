import argparse
import difflib
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
parser=argparse.ArgumentParser()
parser.add_argument('version',choices=['v2','v3'])
version=parser.parse_args().version

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
run_report=WORK/f'reports/native-repair-runtime-field-retirement-{version}-613d4f6f.json'
result=load(run_report)
runtime=Path(result['attempt']).resolve()
assert runtime.parent==WORK/'build' and runtime.name.startswith('repair-runtime-')
terminal=load(runtime/'terminal308.json')
debugger=load(runtime/'diagnostic-terminal.json')
assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent']
assert debugger=={'exitCode':0,'readOnly':True,'methodInvocations':False,'authorityWrites':False}
previous_path=WORK/('reports/field-retirement-diagnostic-archive-receipt-20261112.json' if version=='v2' else 'reports/field-retirement-v2-diagnostic-archive-receipt-20261112.json')
previous=load(previous_path)
assert digest(previous['archive'])==previous['sha256']
formal=load(WORK/'reports/goal-reactivation-after-stage11-20261112/preflight.json')['formalInstancesBefore']
for entry in formal.values():assert digest(entry['path'])==entry['sha256']
candidate=WORK/'reports/native-repair-private-fingerprint-candidate-613d4f6f.json'
assert digest(candidate)=='59051d1e1377b954278e76e9deadc54d433e4e8f9f03f16ea113dcde037be4c8'
trace=(runtime/f'field-retirement-{version}-diagnostic.log').read_text(encoding='utf-8')
armed='ARMED_AFTER_ACTUAL_BOUND_ORIGINAL_RANGED_SAVE' in trace
captured='FIELD_RETIRE_BEFORE_WRITE' in trace
report_path=WORK/f'reports/native-field-retirement-{version}-diagnosis-613d4f6f.json'
assert not report_path.exists()
summary=[line for line in trace.splitlines() if line.startswith(('ARMED','METHOD_ARGUMENTS','FIELD_RETIRE','PLANNED_RANGED','scope','fact','bindingBefore','before','operation','playerDomain','FRAME_ARGUMENTS','STACK 0 ','STACK 1 ','STACK 2 ','STACK 3 ','STACK 4 ','STACK 5 ','STACK 6 ','OWNED_JVM'))]
report={'runtime':str(runtime),'runtimeFailures':terminal['probe']['failures'],'runtimeFailure':terminal['probe'].get('failure'),'nativeExitCode':0,'normalCompleteSave':True,'debuggerExitCode':0,'debuggerReadOnly':True,'diagnosticArmed':armed,'firstRetirementCaptured':captured,'diagnosticAdmissionKnownWrongObject':version=='v2','rootCauseProven':False,'diagnosticSummary':summary,'previousArchive':previous['archive'],'previousArchiveSha256':previous['sha256'],'formalInstancesUnchanged':formal,'candidateUnchanged':str(candidate),'originalAssertionsRetained':True,'failedWorldExcludedAsRestartSeed':True,'fullLifecycleAccepted':False,'goalStatus':'active','installable':False,'deployed':False}
report_path.write_text(json.dumps(report,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
patch='*** Begin Patch\n'
for rel in ['WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json']:
    path=ROOT/rel;before=path.read_text(encoding='utf-8');value=json.loads(before)
    value.update(activeRuntimePid=None,activeRuntimePids=[],activeSupervisor=None,nativePassiveRootRuntimePid=None,nativePassiveRootSupervisor=None,activeWorkstream='signed-session-gated-retirement-diagnostic-terminal',latestNativeGatedRetirementEvidence=str(report_path),currentGoalStatus='active',formalInstancePolicy='read_only_no_deployment',fullSignedSessionAccepted=False,installable=False,deployed=False)
    if rel!='WRITE_OWNER.json':value['status']='gated_retirement_diagnostic_terminal_acceptance_pending'
    after=json.dumps(value,ensure_ascii=False,indent=2)+'\n';delta=list(difflib.unified_diff(before.splitlines(True),after.splitlines(True),n=2))
    patch+='*** Update File: '+rel+'\n'+''.join('@@\n' if line.startswith('@@') else line for line in delta[2:])
patch+='*** End Patch\n'
subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
class_suffix='V2' if version=='v2' else 'V3'
files=[report_path,run_report,Path(__file__),ROOT/'WRITE_OWNER.json',WORK/'CURRENT_INCREMENTAL_BATCH.json',WORK/f'tools/prepare_native_field_retirement_diagnostic_{version}.py',WORK/f'tools/run_native_field_retirement_{version}_613d4f6f.py',WORK/f'tests/native-field-retirement-diagnostic-{version}-613d4f6f/NativeFieldRetirementDiagnostic{class_suffix}.java',runtime/'server/actual-storage-view308.json',previous_path]
files.extend(path for path in runtime.iterdir() if path.is_file())
files.extend(path for path in (WORK/'build/native-field-retirement-diagnostic-613d4f6f').iterdir() if path.is_file())
if version=='v3':files.append(WORK/'reports/field-retirement-diagnostic-v3-preparation-20261112.json')
files=sorted(set(path.resolve() for path in files))
manifest={path.relative_to(ROOT).as_posix():{'sha256':digest(path),'bytes':path.stat().st_size} for path in files}
manifest_bytes=(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
restore_bytes=(f'Evidence-only supplement; NOT INSTALLABLE. Restore after {previous["archive"]}, SHA256 {previous["sha256"]}, and its documented predecessor archives. Candidate and baseline dependencies remain frozen; historical D and both formal instances are read-only. Failed worlds are never restart seeds. Complete original signed lifecycle and final migration acceptance remain pending. No deployment, authority mutation, cleanup or writer handoff.\n').encode('utf-8')
archive_path=WORK/f'archives/field-retirement-{version}-diagnostic-613d4f6f-20261112.zip'
assert not archive_path.exists()
with zipfile.ZipFile(archive_path,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as archive:
    for path in files:archive.write(path,path.relative_to(ROOT).as_posix())
    archive.writestr('MANIFEST.json',manifest_bytes)
    archive.writestr('RESTORE.md',restore_bytes)
with zipfile.ZipFile(archive_path) as archive:
    assert len(archive.namelist())==len(manifest)+2
    assert archive.read('MANIFEST.json')==manifest_bytes and archive.read('RESTORE.md')==restore_bytes
    for name,entry in manifest.items():assert hashlib.sha256(archive.read(name)).hexdigest()==entry['sha256']
for path in files:assert digest(path)==manifest[path.relative_to(ROOT).as_posix()]['sha256']
receipt_path=WORK/f'reports/field-retirement-{version}-diagnostic-archive-receipt-20261112.json'
assert not receipt_path.exists()
receipt={'archive':str(archive_path),'sha256':digest(archive_path),'manifestFiles':len(manifest),'allMembersIncludingMetadataReadBackAndVerified':True,'previousArchive':previous['archive'],'previousArchiveSha256':previous['sha256'],'formalInstancesUnchanged':formal,'diagnosticArmed':armed,'firstRetirementCaptured':captured,'goalStatus':'active','fullLifecycleAccepted':False,'deployed':False}
receipt_path.write_text(json.dumps(receipt,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'archive':str(archive_path),'sha256':receipt['sha256'],'files':len(manifest),'diagnosticArmed':armed,'firstRetirementCaptured':captured,'runtimeFailures':terminal['probe']['failures'],'goalStatus':'active','deployed':False}),flush=True)
