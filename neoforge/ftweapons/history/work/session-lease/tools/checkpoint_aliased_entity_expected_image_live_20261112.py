import datetime
import hashlib
import json
import re
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUT=WORK/'reports/aliased-entity-expected-image-live-checkpoint-20261112'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

assert not OUT.exists()
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['activeRuntimePid']==21176 and owner['activeDebuggerPid']==68388 and owner['activeSupervisor']==38677
runtime=WORK/'build/repair-runtime-bc621ceb'
assert not (runtime/'terminal308.json').exists() and not (runtime/'server/actual-storage-view308.json').exists()
processes=subprocess.run(['powershell','-NoProfile','-Command',"$items=@(Get-CimInstance Win32_Process | Where-Object { $_.ProcessId -in @(21176,68388) }); @($items | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId; name=$_.Name; ownsExactAttempt=$_.CommandLine.Contains('repair-runtime-bc621ceb')} }) | ConvertTo-Json -Compress"],capture_output=True,text=True)
assert processes.returncode==0
live=json.loads(processes.stdout);assert len(live)==2 and all(row['ownsExactAttempt'] and row['name']=='java.exe' for row in live)
preflight_path=WORK/'reports/goal-continuation-after-stage13-20261112/preflight.json';preflight=load(preflight_path)
for row in preflight['formalInstancesBefore'].values():assert digest(row['path'])==row['sha256']
for row in preflight['archives']:assert digest(row['archive'])==row['archiveSha256']
candidate_path=WORK/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
assert digest(candidate_path)=='482fd005e8287ef41e48eced0ca5bd740df8b78580526165a2b550d47f666b10'
candidate=load(candidate_path)
for path,wanted in candidate['inputHashes'].items():assert digest(path)==wanted,path
build=Path(candidate['attempt']);assert load(build/'asm.json')['failures']==0 and load(build/'boundary-proof-terminal.json')['exitCode']==0
OUT.mkdir()
for name,target in [('runtime308.log','native-runtime-prefix.log'),('domain-retirement-watch.log','domain-watch-prefix.log')]:
    with (OUT/target).open('xb') as output:output.write((runtime/name).read_bytes())
log=(OUT/'native-runtime-prefix.log').read_text(encoding='utf-8',errors='replace');started=sorted(set(map(int,re.findall(r'ft_exact_(\d+)',log))))
assert started and started==list(range(started[-1]+1)) and started[-1]>=2
watch=(OUT/'domain-watch-prefix.log').read_text(encoding='utf-8')
alias_line=next(line for line in watch.splitlines() if line.startswith('EXACT_DOMAIN_ALIAS_AT_ARM '))
alias=json.loads(alias_line.split(' ',1)[1]);assert alias['sameDomainBefore'] and alias['sameNonNullPersistentRoot'] and alias['plannedAliasFact']
assert 'OwnerFieldExpectedAuthority.changed(Ljava/lang/Object;)V bci=50 line=254' in watch and 'scope=null' in watch
status={'createdAt':datetime.datetime.now(datetime.timezone(datetime.timedelta(hours=8))).isoformat(),'goalTurnClassification':'progress','goalStatus':'active','candidate':str(candidate_path),'candidateSha256':digest(candidate_path),'frozenCandidateInputsVerified':len(candidate['inputHashes']),'boundaryProof':(build/'boundary-proof.log').read_text(encoding='utf-8'),'rootCauseEvidence':str(WORK/'reports/native-repair-runtime-domain-value-diff-watch-8199dcc2.json'),'actualAliasAtArm':alias,'originalFullNineteenAssertionsUnchanged':True,'actualRuntime':str(runtime),'ownedNativePid':21176,'ownedReadOnlyDebuggerPid':68388,'ownedSupervisorToolSession':38677,'liveProcesses':live,'attacksStartedAtSnapshot':started,'priorOriginalIterationCompletionsInferredFromReachingNextStrictIteration':started[-1],'notYetFinalNativeProbe':True,'originalNineteenTerminalResultPending':True,'laterDiagnosticLimitation':'Optional tree reader rejects changed(Object), outside CURRENT, after the original advance before/after mirror gate has passed; preserve diagnostic error. Supervisor may exit1 solely for that debugger error even if original native probe passes. Inspect native original result before any next action.','formalInstancesUnchanged':preflight['formalInstancesBefore'],'frozenArchivesReused':preflight['archives'],'fullSignedSessionAccepted':False,'fullLifecycleAccepted':False,'installable':False,'deployed':False,'sameRuntimeMustContinue':True,'doNotRelaunchOrKillOwnedNative':True,'failedWorldsNeverRestartSeeds':True,'next':'Wait on existing tool session38677. Once native terminates, inspect original actual-storage-view308.json, terminal308.json and diagnostic-terminal.json. Run tools/archive_aliased_entity_expected_image_20261112.py after terminal; keep diagnostic failure distinct from original product assertions. Do not switch writer, relaunch a duplicate runtime, deploy or weaken assertions.'}
save(OUT/'progress.json',status)
helper=WORK/'tools/reactivate_goal_after_boss_stage12.py';namespace={'__file__':str(helper)};exec(helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],namespace)
for name in ['WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');data=json.loads(before)
    data.update(latestNativeAliasedExpectedImageLiveCheckpoint=str(OUT/'progress.json'),currentGoalStatus='active',activeRuntimePid=21176,activeRuntimePids=[21176],activeDebuggerPid=68388,activeSupervisor=38677,pendingTerminalArchiveTool='work/session-lease/tools/archive_aliased_entity_expected_image_20261112.py',originalFullNineteenTerminalResultPending=True,fullSignedSessionAccepted=False,currentFormalInstancePolicy='read_only_current_goal_no_deployment')
    namespace['replace_file'](path,before,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
files=[Path(__file__),candidate_path,preflight_path,ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',ROOT/'AGENTS.md',WORK/'CURRENT_INCREMENTAL_BATCH.json',WORK/'ALIASED_ENTITY_EXPECTED_IMAGE_5F283607.md',WORK/'reports/aliased-entity-expected-image-preparation-20261112.json',WORK/'reports/native-repair-aliased-entity-expected-image-candidate.json',WORK/'reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json',WORK/'tests/AliasedEntityExpectedImageProof.java',*[Path(row['receipt']) for row in preflight['archives']]]
for directory in [build,WORK/'proposal-aliased-entity-expected-image',OUT,WORK/'tests/native-domain-retirement-watch-8199dcc2',WORK/'tests/native-domain-value-diff-watch-8199dcc2',WORK/'tests/native-aliased-domain-value-diff-watch-5f283607']:
    files.extend(path for path in directory.rglob('*') if path.is_file())
for name in ['continue_explicit_goal_after_stage13.py','run_native_domain_retirement_watch_8199dcc2.py','run_native_domain_value_diff_watch_8199dcc2.py','build_aliased_entity_expected_image.py','verify_aliased_entity_expected_image.py','prepare_aliased_entity_expected_image_runtime.py','run_native_aliased_entity_expected_image_5f283607.py','archive_aliased_entity_expected_image_20261112.py']:files.append(WORK/'tools'/name)
for report in ['native-repair-runtime-domain-retirement-watch-8199dcc2.json','native-repair-runtime-domain-value-diff-watch-8199dcc2.json']:
    path=WORK/'reports'/report;files.append(path);previous=Path(load(path)['attempt'])
    files.extend(path for path in previous.iterdir() if path.is_file())
    for name in ['server/actual-storage-view308.json','server/actual-private-read-guard.json']:
        files.append(previous/name)
for name in ['live-process308.json','diagnostic-process.json','diagnostic-command.json','diagnostic-compile-terminal.json','inputs-before308.json','restart-world-seed.json','runtime-command308.json','read-only-native-progress-thread-sample.json','read-only-native-progress-thread-sample.log']:
    files.append(runtime/name)
files=sorted(set(path.resolve() for path in files));assert all(path.is_relative_to(ROOT) for path in files)
manifest={path.relative_to(ROOT).as_posix():{'sha256':digest(path),'bytes':path.stat().st_size} for path in files}
metadata=(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n').encode('utf-8')
restore=('Live progress checkpoint, NOT INSTALLABLE; native original nineteen is still running. This is not a terminal runtime archive or complete final acceptance. Reuse frozen8199 weapon archive and predecessor chain recorded in preflight; the complete1381-class5f283607 candidate graph and scoped source overrides are included. Native21176/debugger68388/supervisor38677 belong to the same sole C writer. Reconcile their current actual state before any continuation, wait on the same tool session and never relaunch a duplicate. Do not stop the native run, deploy, mutate authority, relax original assertions or use diagnostic/failed worlds as restart seeds. Runtime logs are explicitly bounded prefix snapshots. Terminal archive tool is staged separately and must only run after normal native terminal evidence. Both formal instances remain read-only.\n').encode('utf-8')
archive=WORK/'archives/aliased-entity-expected-image-5f283607-LIVE-PROGRESS-20261112.zip'
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED,compresslevel=6) as output:
    for path in files:output.write(path,path.relative_to(ROOT).as_posix())
    output.writestr('MANIFEST.json',metadata);output.writestr('RESTORE.md',restore)
with zipfile.ZipFile(archive) as output:
    assert len(output.namelist())==len(manifest)+2
    assert output.read('MANIFEST.json')==metadata and output.read('RESTORE.md')==restore
    for name,row in manifest.items():assert hashlib.sha256(output.read(name)).hexdigest()==row['sha256']
for path in files:assert digest(path)==manifest[path.relative_to(ROOT).as_posix()]['sha256']
receipt={'archive':str(archive),'sha256':digest(archive),'manifestFiles':len(manifest),'allMembersIncludingMetadataReadBackAndVerified':True,'liveProgressOnly':True,'originalNineteenTerminalResultPending':True,'runtime':str(runtime),'nativePid':21176,'debuggerPid':68388,'supervisorToolSession':38677,'progress':str(OUT/'progress.json'),'progressSha256':digest(OUT/'progress.json'),'formalInstancesUnchanged':preflight['formalInstancesBefore'],'goalStatus':'active','fullSignedSessionAccepted':False,'fullLifecycleAccepted':False,'installable':False,'deployed':False}
save(OUT/'archive-receipt.json',receipt)
print(json.dumps({'archive':str(archive),'sha256':receipt['sha256'],'files':len(manifest),'priorStrictIterationsCompleted':started[-1],'nativeOriginalNineteenStillRunning':True,'goalStatus':'active','deployed':False}),flush=True)
