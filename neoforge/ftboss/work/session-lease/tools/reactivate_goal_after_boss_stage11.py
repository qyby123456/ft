import datetime
import hashlib
import json
import os
import subprocess
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
BOSS=ROOT/'work/session-boss'
THREAD='01a1c43e-ae80-7390-b604-b43b4ab53f2f'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

output=WORK/'reports/goal-reactivation-after-stage11-20261112'
assert not output.exists()
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']==THREAD
candidate=WORK/'reports/native-repair-session-timer-expectation-candidate-73d460d2.json'
assert digest(candidate)=='e290bee4f709f7912cfadfb6f103300100f6a264cdeb13422f8df843fab3e9ac'
binding=load(candidate)
for name,wanted in binding['inputHashes'].items():assert digest(name)==wanted,name
paused_path=BOSS/'archives/weapons-paused-73d460d2-20261112/archive-receipt.json'
paused=load(paused_path)
assert paused['failures']==0 and not paused['installable']
assert digest(paused['archive'])==paused['archiveSha256']
assert digest(paused_path.parent/'manifest.jsonl')==paused['manifestSha256']
assert digest(paused_path.parent/'RESTORE.md')==paused['restoreInstructionsSha256']
assert digest(paused['snapshotJar'])==paused['snapshotJarSha256']
formal={name:{'path':entry['live'],'sha256':digest(entry['live'])} for name,entry in load(ROOT/'baseline.json').items() if isinstance(entry,dict) and 'live' in entry}
stage11=load(BOSS/'releases/boss-core-stage11-cleaver-sweep-20261112/deployment-receipt.json')
assert formal['neoforge']['sha256']==stage11['deployedSha256'] and formal['forge']['sha256']==stage11['forgeSha256Unchanged']
assert not list(Path(formal['neoforge']['path']).parent.glob('qys-bosses-1.21.1.jar.stage*.tmp'))
last=load(WORK/'reports/native-repair-runtime-full-nineteen-signed-73d460d2.json')
terminal=load(Path(last['attempt'])/'terminal308.json')
assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and last['probe']['failures']==1
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease')} } | ConvertTo-Json -Compress"
processes=json.loads(subprocess.check_output(['powershell.exe','-NoProfile','-Command',command],encoding='utf-8-sig'))
processes=processes if isinstance(processes,list) else [processes]
processes=[row for row in processes if row['pid']!=os.getpid()]
assert not any(row['owned'] for row in processes)
output.mkdir()
save(output/'owner-before.json',owner)
save(output/'lease-batch-before.json',load(WORK/'CURRENT_INCREMENTAL_BATCH.json'))
save(output/'boss-batch-before.json',load(BOSS/'CURRENT_BOSS_BATCH.json'))
report={'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'previousGoalTurn':'progress: stage11 single-gap Boss release verified, deployed and archived under preceding user authorization','activeGoal':'NFboss migration: genuine non-null lease, repair/payment and full signed Session first, then remaining final acceptance','latestExplicitGoalSupersedesPauseForNamedSessionWork':True,'policy':'No deployment or overwrite of either formal instance; historical D read-only; same sole C writer; no handoff','candidate':str(candidate),'candidateSha256':digest(candidate),'verifiedFrozenInputs':len(binding['inputHashes']),'pausedCheckpointReceipt':str(paused_path),'pausedCheckpointArchiveSha256':paused['archiveSha256'],'checkpointHashesVerifiedWithoutRepeatingAcceptance':True,'formalInstancesBefore':formal,'latestFailedRuntime':last['attempt'],'latestFailedRuntimeJavaExitedZeroAndSaved':True,'latestFailedWorldExcludedAsRestartSeed':True,'processAudit':processes,'fullSignedSessionAccepted':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
save(output/'preflight.json',report)
scope='Latest explicit active user goal after stage11: genuine lease, repair/payment and complete signed Session, then remaining final acceptance; sole C writer; both formal instances read-only; no deployment or handoff'
owner.update({'currentGoalStatus':'active','currentScopeInstruction':scope,'currentFormalInstancePolicy':'read_only_no_deployment','weaponCandidateValidationResumed':True,'weaponFunctionalDevelopmentPaused':False,'weaponFunctionalDevelopment':'named_session_goal_work_resumed_by_latest_explicit_active_goal','weaponFeatureExpansionAuthorized':False,'activeWorkstream':'signed-session-unbound-native-map-diagnostic','activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'latestGoalReactivationPreflight':str(output/'preflight.json'),'nativeContinuationStatus':'73d460d2 whole nineteen Session failed at nested/unbound Map guard; genuine lease and paid ancestry frozen; read-only actual failing Fact/call-stack diagnosis next','nextNativeContinuationWork':'Diagnose the actual rejected Map parent/owner and original preplanned transition without relaxing assertions; complete full Session and restart/control matrix before acceptance'})
batch=load(WORK/'CURRENT_INCREMENTAL_BATCH.json')
batch.update({'status':'explicit_goal_resumed_native_map_diagnostic_pending','currentScopeInstruction':scope,'formalInstancePolicy':'read_only_no_deployment','resumedByExplicitUserGoal':True,'weaponCandidateValidationResumed':True,'weaponFunctionalDevelopmentPaused':False,'activeRuntimePid':None,'activeSupervisor':None,'nextResumeTarget':'Read-only JDI of original nested Map rejection, then genuine full Session, negative controls and fresh-process restart; all remaining final acceptance gates unchanged','goalReactivationPreflight':str(output/'preflight.json')})
for path,data in ((ROOT/'WRITE_OWNER.json',owner),(WORK/'CURRENT_INCREMENTAL_BATCH.json',batch)):
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
print(json.dumps({'preflight':str(output/'preflight.json'),'verifiedFrozenInputs':len(binding['inputHashes']),'ownedProcesses':0,'formalPolicy':'read_only_no_deployment','fullAcceptance':False}),flush=True)
