import datetime
import hashlib
import json
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
preflight_path=WORK/'reports/goal-continuation-after-stage13-20261112/preflight.json'
preflight=load(preflight_path)
for row in preflight['formalInstancesBefore'].values():assert digest(row['path'])==row['sha256']
for row in preflight['archives']:assert digest(row['archive'])==row['archiveSha256']
candidate_path=WORK/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
assert digest(candidate_path)=='482fd005e8287ef41e48eced0ca5bd740df8b78580526165a2b550d47f666b10'
candidate=load(candidate_path)
for path,wanted in candidate['inputHashes'].items():assert digest(path)==wanted,path
build=Path(candidate['attempt'])
run_reports=[WORK/'reports/native-repair-runtime-domain-retirement-watch-8199dcc2.json',WORK/'reports/native-repair-runtime-domain-value-diff-watch-8199dcc2.json',WORK/'reports/native-repair-runtime-aliased-entity-expected-image-5f283607.json']
runs=[load(path) for path in run_reports]
for result in runs:
    runtime=Path(result['attempt']);assert runtime.parent==WORK/'build'
    terminal=load(runtime/'terminal308.json');debugger=load(runtime/'diagnostic-terminal.json')
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
    assert debugger['readOnly'] and not debugger['methodInvocations'] and not debugger['authorityWrites']
    if result is not runs[-1]:assert debugger['exitCode']==0
    else:assert debugger['exitCode']==1 and result['exactDomainRetirementWatch']['diagnosticErrors']==1
    assert result['privateReadGuardProbe']['failures']==0
last=runs[-1];runtime=Path(last['attempt']);probe=last['probe'];diff=runs[1]['exactDomainValueDifference']
assert diff['expectedAfterPlayer']['sha256']==diff['actualPlayer']['sha256']
assert [row['path'] for row in diff['differences']]==['after/carrier/FTBossRangedProcTimers']
assert last['actualDomainAlias']['sameDomainBefore'] and last['actualDomainAlias']['sameNonNullPersistentRoot'] and last['actualDomainAlias']['plannedAliasFact']
process_result=subprocess.run(['powershell','-NoProfile','-Command',"$workspace='"+str(ROOT).replace("'","''")+"'; $items=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -like ('*'+$workspace+'*') }); @($items | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId; name=$_.Name} }) | ConvertTo-Json -Compress"],capture_output=True,text=True)
assert process_result.returncode==0
live=json.loads(process_result.stdout) if process_result.stdout.strip() else []
if isinstance(live,dict):live=[live]
assert not live,live
summary_path=WORK/'reports/aliased-entity-expected-image-progress-20261112.json'
summary={'createdAt':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(),'goalTurnClassification':'progress','goalStatus':'active','firstOriginalRetirementSite':'OwnerFieldExpectedAuthority.advance after-image comparison, original line668','readOnlyDifferenceDiagnostic':str(run_reports[1]),'rootCause':'Independent expected planner treated actual identical player/carrier DomainBefore as separate roots; original ranged root write also changes the carrier image','candidate':str(candidate_path),'candidateSha256':digest(candidate_path),'aliasFactSource':'Only actual DomainBefore identity var3 == var2 in unchanged original Prepared construction; never inferred from equal snapshots','newBoundaryProof':(build/'boundary-proof.log').read_text(encoding='utf-8'),'runtime':str(runtime),'nativeExitCode':0,'completeOriginalSave':True,'debuggerExitCode':last['debuggerExitCode'],'debuggerDiagnosticErrors':last['exactDomainRetirementWatch']['diagnosticErrors'],'diagnosticLimitation':'Read-only optional tree reader only accepts advance(Session,Step). First later retirement moved to changed(Object), outside CURRENT scope; known diagnostic argument mismatch retained, not accepted as a successful tree read. Original native nineteen assertions remain independent and unchanged.','debuggerReadOnly':True,'actualDomainAlias':last['actualDomainAlias'],'runtimeProbeFailures':probe['failures'],'runtimeFailure':probe.get('failure'),'originalFullNineteenSignedSessionsPassed':probe['failures']==0 and probe.get('nineteenConfiguredOriginalMeleeGenerationTwoFullSignedSessions') is True,'firstRetirementCapturedAfterCandidate':last['exactDomainRetirementWatch']['firstExactRetirementCaptured'],'candidateAfterImageDifference':last['exactDomainValueDifference'],'foreignGuardFailures':0,'formalInstancesUnchanged':preflight['formalInstancesBefore'],'frozenArchivesReused':preflight['archives'],'failedWorldsExcludedAsRestartSeeds':True,'originalAssertionsUnchanged':True,'allNamedLeaseRepairAndFinalRequirementsStillApply':True,'fullLifecycleAccepted':False,'fullSignedSessionAccepted':False,'installable':False,'deployed':False,'remainingOwnedJava':live,'next':'Continue from actual unchanged original full-nineteen outcome; no assertion relaxation, authority synthesis, failed-world seeds or deployment'}
save(summary_path,summary)
helper=WORK/'tools/reactivate_goal_after_boss_stage12.py';namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],namespace)
for relative in ['WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json']:
    path=ROOT/relative;before=path.read_text(encoding='utf-8');data=json.loads(before)
    data.update(activeRuntimePid=None,activeRuntimePids=[],activeDebuggerPid=None,activeSupervisor=None,activeWorkstream='signed-session-aliased-entity-expected-image-terminal-5f283607',latestNativeAliasedExpectedImageEvidence=str(summary_path),latestNativeAliasedExpectedImageRuntime=str(run_reports[-1]),currentGoalStatus='active',currentFormalInstancePolicy='read_only_current_goal_no_deployment',fullSignedSessionAccepted=False,installable=False,deployed=False)
    if relative!='WRITE_OWNER.json':data['status']='aliased_expected_image_runtime_terminal_final_acceptance_pending'
    namespace['replace_file'](path,before,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
files=[WORK/'ALIASED_ENTITY_EXPECTED_IMAGE_5F283607.md',Path(__file__),summary_path,candidate_path,preflight_path,ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',ROOT/'AGENTS.md',WORK/'CURRENT_INCREMENTAL_BATCH.json',*run_reports,*[Path(row['receipt']) for row in preflight['archives']],WORK/'reports/aliased-entity-expected-image-preparation-20261112.json',WORK/'reports/native-repair-aliased-entity-expected-image-candidate.json',WORK/'reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json']
for name in ['continue_explicit_goal_after_stage13.py','run_native_domain_retirement_watch_8199dcc2.py','run_native_domain_value_diff_watch_8199dcc2.py','build_aliased_entity_expected_image.py','verify_aliased_entity_expected_image.py','prepare_aliased_entity_expected_image_runtime.py','run_native_aliased_entity_expected_image_5f283607.py']:files.append(WORK/'tools'/name)
for directory in ['proposal-aliased-entity-expected-image','tests/native-domain-retirement-watch-8199dcc2','tests/native-domain-value-diff-watch-8199dcc2','tests/native-aliased-domain-value-diff-watch-5f283607']:
    files.extend(path for path in (WORK/directory).rglob('*') if path.is_file())
files.append(WORK/'tests/AliasedEntityExpectedImageProof.java')
files.extend(path for path in build.rglob('*') if path.is_file())
for result in runs:
    current=Path(result['attempt'])
    files.extend(path for path in current.iterdir() if path.is_file())
    for directory in ['diagnostic-classes','observer-classes']:
        files.extend(path for path in (current/directory).rglob('*') if path.is_file())
    for relative in ['server/actual-storage-view308.json','server/actual-private-read-guard.json','server/logs/latest.log','server/.mixin.out/class/net/minecraft/nbt/CompoundTag.class','server/.mixin.out/class/net/farmingtales/ftboss/item/FoodWeaponRanged.class']:
        path=current/relative
        if path.is_file():files.append(path)
files=sorted(set(path.resolve() for path in files))
assert all(path.is_relative_to(ROOT) for path in files)
manifest={path.relative_to(ROOT).as_posix():{'sha256':digest(path),'bytes':path.stat().st_size} for path in files}
metadata=(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
restore=('Evidence and independent expected-image candidate only. NOT INSTALLABLE. No deployment. Restore against the frozen weapon8199 archive recorded by preflight and its predecessor chain; retain historical D read-only. This supplement includes the complete1381-class candidate graph, scoped source overrides, source-binding paths, unchanged original assertions, original failed diagnostics and terminal outcomes. Never use these failed runtime worlds as restart seeds; only the accepted849680bb paid seed is allowed. Both formal instances remain read-only. Full goal and final acceptance remain pending; see progress report.\n').encode('utf-8')
archive=WORK/'archives/aliased-entity-expected-image-5f283607-20261112.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as output:
    for path in files:output.write(path,path.relative_to(ROOT).as_posix())
    output.writestr('MANIFEST.json',metadata);output.writestr('RESTORE.md',restore)
with zipfile.ZipFile(archive) as output:
    assert len(output.namelist())==len(manifest)+2
    assert output.read('MANIFEST.json')==metadata and output.read('RESTORE.md')==restore
    for name,row in manifest.items():assert hashlib.sha256(output.read(name)).hexdigest()==row['sha256']
for path in files:assert digest(path)==manifest[path.relative_to(ROOT).as_posix()]['sha256']
receipt_path=WORK/'reports/aliased-entity-expected-image-archive-receipt-20261112.json'
save(receipt_path,{'archive':str(archive),'sha256':digest(archive),'manifestFiles':len(manifest),'allMembersIncludingMetadataReadBackAndVerified':True,'summary':str(summary_path),'summarySha256':digest(summary_path),'formalInstancesUnchanged':preflight['formalInstancesBefore'],'reusedArchives':preflight['archives'],'runtimeFailures':probe['failures'],'goalStatus':'active','fullLifecycleAccepted':False,'installable':False,'deployed':False})
print(json.dumps({'archive':str(archive),'sha256':digest(archive),'files':len(manifest),'runtimeFailures':probe['failures'],'goalStatus':'active','deployed':False}),flush=True)
