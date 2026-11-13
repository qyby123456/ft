import datetime
import hashlib
import json
import os
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUT=WORK/'reports/goal-continuation-after-stage13-20261112'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,data):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)

helper=WORK/'tools/reactivate_goal_after_boss_stage12.py';content=helper.read_text(encoding='utf-8');namespace={'__file__':str(helper)};exec(compile(content[:content.index('assert not OUT.exists()')],str(helper),'exec'),namespace)
replace_file=namespace['replace_file']
assert not OUT.exists()
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
candidate=WORK/'reports/native-repair-certified-ranged-timer-frame-candidate-8199dcc2.json'
assert sha(candidate)=='23f7a20eae05b48826098112dcee71f67e3c61e51f03ba9c0f897866970f49bc'
frozen=load(candidate)
for name,wanted in frozen['inputHashes'].items():assert sha(name)==wanted,name
assert frozen['originalFullNineteenAssertionsPreserved'] and not frozen['fullSignedSessionAccepted']
latest=WORK/'reports/native-repair-runtime-full-nineteen-certified-ranged-timer-frame-8199dcc2.json';result=load(latest);terminal=load(Path(result['attempt'])/'terminal308.json')
assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent']
assert result['probe']['failures']>0 and result['privateReadGuardProbe']['failures']==0
archives=[]
for name in ['work/session-boss/archives/weapons-paused-8199dcc2-boss-priority-20261112/archive-receipt.json','work/session-boss/candidates/boss-core-stage13-summon-fx-20261112/archive-receipt.json','work/session-boss/releases/boss-core-stage12-bone-crush-20261112/archive-receipt.json']:
    path=ROOT/name;receipt=load(path);assert receipt['failures']==0 and receipt['allFilesReadBackAndHashVerified'] and sha(receipt['archive'])==receipt['archiveSha256'];archives.append({'receipt':str(path),'receiptSha256':sha(path),'archive':receipt['archive'],'archiveSha256':receipt['archiveSha256'],'frozenEvidenceReused':True})
formal=load(ROOT/'work/session-boss/candidates/boss-core-stage13-summon-fx-20261112/archive-receipt.json')['formalInstancesUnchanged']
for row in formal.values():assert sha(row['path'])==row['sha256']
assert formal['neoforge']['sha256']=='58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease')} } | ConvertTo-Json -Compress"
processes=json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',command],encoding='utf-8-sig'));processes=processes if isinstance(processes,list) else [processes]
assert not any(row['owned'] and row['pid']!=os.getpid() for row in processes)
OUT.mkdir(parents=True)
save(OUT/'preflight.json',{'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'previousGoalTurnClassification':'progress: Boss stage13 original summon FX candidate, actual native evidence and 457-file verified archive; weapon pause archive verified; no deployment','latestObjectiveExplicitlyNamesSessionPrerequisites':True,'restrictedScope':'genuine non-null lease, repair/payment and complete signed Session; remaining original final acceptance','candidate':str(candidate),'candidateSha256':sha(candidate),'frozenInputsVerified':len(frozen['inputHashes']),'archives':archives,'formalInstancesBefore':formal,'ownedJavaPythonRemaining':0,'previousNativeExitCode':0,'previousNativeSavedNormally':True,'previousFullNineteenStillFailed':True,'originalAssertionsUnchanged':True,'fullSignedSessionAccepted':False,'failedWorldsExcludedFromRestartSeeds':True,'deploymentAllowed':False,'readOnlyOrientationScanStopped':{'pid':13400,'verifiedExactParentAndCommandBeforeStopping':True,'noFilesOrRuntimeChanged':True},'next':'Compile archived read-only exact-instance JDI watchpoint; capture first actual domain retirement without target invocations or authority writes'})
for name in ['AGENTS.md','WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');save(OUT/(name.replace('/','_')+'.before.json'),{'path':str(path),'text':before,'sha256':sha(path)})
    if name=='AGENTS.md':
        heading='## Latest explicit user goal continuation after stage13, 2026-11-12\n\n- The current user-provided objective explicitly requests genuine non-null lease, repair/payment and complete signed Session as prerequisites for NFboss migration. Pursue those named prerequisites in this same sole C workspace. This explicit objective supersedes the older local pause restriction only for its named prerequisites; unrelated weapon gameplay remains paused and all pause archives remain frozen.\n- Both formal instances remain read-only. Do not deploy or overwrite either instance or hand off/switch the sole writer. Reuse frozen stage12/stage13 Boss and weapon archive evidence.\n- Start from frozen candidate8199dcc2 and the original failed whole-nineteen Session. Capture the actual first domain retirement through a read-only exact-instance watchpoint; never reset retired state, fabricate lease/payment authority, invoke target methods from the debugger, weaken stack/value/caller gates or relax original assertions. All original restart, foreign-control and final acceptance requirements remain pending.\n\n'
        after=before.replace('# NeoForge continuation on C\n\n','# NeoForge continuation on C\n\n'+heading,1)
    else:
        data=json.loads(before);data.update({'activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'activeWorkstream':'signed-session-exact-domain-retirement-diagnostic-after-stage13','currentGoalStatus':'active','currentGoalPreflight':str(OUT/'preflight.json'),'currentFormalInstancePolicy':'read_only_current_goal_no_deployment','weaponFunctionalDevelopment':'restricted_to_current_explicit_goal_lease_repair_payment_full_session_prerequisites','weaponValidationPaused':False,'weaponCandidateValidationResumed':True,'fullSignedSessionAccepted':False,'fullBossMigrationComplete':False})
        if 'session-lease' in name:data.update({'status':'active_current_explicit_goal_prerequisites','next':'Read-only exact live domain retirement watchpoint on original whole nineteen; no failure-world seed or assertion relaxation'})
        elif 'session-boss' in name:data.update({'status':'stage13_frozen_reused_current_explicit_goal_prerequisites','next':'Reuse frozen stage13 evidence; no further Boss deployment under current objective'})
        after=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    replace_file(path,before,after)
print(json.dumps({'frozenInputsVerified':len(frozen['inputHashes']),'archivesReused':len(archives),'formalUnchanged':True,'fullSignedSessionAccepted':False,'deploymentAllowed':False}),flush=True)
