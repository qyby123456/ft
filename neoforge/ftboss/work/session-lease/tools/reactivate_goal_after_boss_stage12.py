import datetime
import difflib
import hashlib
import json
import os
import subprocess
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
OUT=WORK/'reports/goal-reactivation-after-stage12-20261112'
PATCH='C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)
def replace_file(path,before,after):
    lines=list(difflib.unified_diff(before.splitlines(),after.splitlines(),n=3))[2:]
    patch='*** Begin Patch\n*** Update File: '+path.relative_to(ROOT).as_posix()+'\n'+''.join('@@\n' if line.startswith('@@') else line+'\n' for line in lines)+'*** End Patch\n'
    result=subprocess.run([PATCH,'--codex-run-as-apply-patch',patch],capture_output=True,text=True,cwd=ROOT)
    assert result.returncode==0,result.stderr
    assert path.read_text(encoding='utf-8')==after
assert not OUT.exists()
claim=load(ROOT/'WRITE_OWNER.claim')
assert claim['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'==load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']
candidate=WORK/'reports/native-repair-private-fingerprint-candidate-613d4f6f.json'
assert sha(candidate)=='59051d1e1377b954278e76e9deadc54d433e4e8f9f03f16ea113dcde037be4c8'
frozen=load(candidate)
for path,wanted in frozen['inputHashes'].items():assert sha(path)==wanted,path
release=ROOT/'work/session-boss/releases/boss-core-stage12-bone-crush-20261112'
deployment=load(release/'deployment-receipt.json')
assert sha(deployment['target'])==deployment['deployedSha256']=='58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3'
archives=[]
for receipt_path in [release/'archive-receipt.json',release/'closeout-archive-receipt.json',ROOT/'work/session-boss/archives/weapons-paused-613d4f6f-boss-priority-v2-20261112/archive-receipt.json']:
    receipt=load(receipt_path);assert receipt['failures']==0 and sha(receipt['archive'])==receipt['archiveSha256']
    archives.append({'receipt':str(receipt_path),'receiptSha256':sha(receipt_path),'archive':receipt['archive'],'archiveSha256':receipt['archiveSha256'],'existingReadBackEvidenceReused':True})
pause=load(Path(archives[-1]['receipt']))
assert sha(pause['snapshotJar'])==pause['snapshotJarSha256']
formal={'neoforge':{'path':deployment['target'],'sha256':deployment['deployedSha256']},'forge':pause['formalInstancesUnchanged']['forge']}
for row in formal.values():assert sha(row['path'])==row['sha256']
assert not list(Path(deployment['target']).parent.glob(Path(deployment['target']).name+'.stage*.tmp'))
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-lease|session-boss')} } | ConvertTo-Json -Compress"
rows=json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',command],encoding='utf-8-sig'))
rows=rows if isinstance(rows,list) else [rows]
assert not any(row['owned'] and row['pid']!=os.getpid() for row in rows)
latest=WORK/'build/repair-runtime-98fb0193'
assert load(latest/'terminal308.json')['exitCode']==0 and load(latest/'terminal308.json')['completeSave']
assert load(latest/'diagnostic-terminal.json')['exitCode']==1
OUT.mkdir(parents=True)
save(OUT/'preflight.json',{'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'previousGoalTurnClassification':'progress: stage12 Boss increment verified/deployed/archived; new goal forbids further deployment','soleWriter':claim['threadId'],'parentCandidate':str(candidate),'parentCandidateSha256':sha(candidate),'frozenInputsVerified':len(frozen['inputHashes']),'archives':archives,'formalInstancesBefore':formal,'ownedProcessesRemaining':0,'lastDiagnosticJavaExitCode':0,'lastDiagnosticSavedNormally':True,'lastReadOnlyDebuggerExitCode':1,'previousFailurePreserved':True,'failedWorldsExcludedFromRestartSeeds':True,'fullSignedSessionAccepted':False,'deploymentAllowed':False,'next':'Use certified transformed putLong private original identity; preserve full nineteen assertions and genuine paid lease seed'})
for name in ['AGENTS.md','WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');save(OUT/(name.replace('/','_')+'.before.json'),{'path':str(path),'text':before,'sha256':sha(path)})
    if name=='AGENTS.md':
        heading='## Latest active goal after stage12, 2026-11-12\n\n- The latest goal explicitly names genuine non-null lease, repair/payment and complete signed Session as prerequisites for NFboss migration. Continue those named prerequisites in this same sole C workspace, preserving the weapon pause archive as frozen history. Do not expand this into unrelated weapon gameplay work.\n- Both formal instances are read-only under this goal. No deployment or overwriting, no workspace/writer handoff. Reuse stage12 and earlier frozen Boss evidence rather than rerunning it.\n- Start from frozen candidate613d4f6f and its failed original nineteen Sessions. The next gap is recognition of the authenticated actual MixinExtras private putLong implementation; do not glob method names, hardcode ordinals, reset retired domains, fabricate lease authority or relax original assertions. Full original acceptance remains pending.\n\n'
        after=before.replace('# NeoForge continuation on C\n\n','# NeoForge continuation on C\n\n'+heading,1)
    else:
        data=json.loads(before);data.update({'activeWorkstream':'signed-session-certified-transformed-ranged-putLong-identity','formalInstancePolicy':'read_only_no_deployment','currentFormalInstancePolicy':'read_only_no_deployment','activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'weaponFunctionalDevelopment':'restricted_to_explicit_goal_lease_repair_payment_full_session_prerequisites','weaponValidationPaused':False,'latestGoalReactivationPreflight':str(OUT/'preflight.json'),'fullSignedSessionAccepted':False,'fullBossMigrationComplete':False})
        if 'CURRENT_INCREMENTAL' in name:data['status']='active_goal_certified_transformed_ranged_method_identity'
        if 'CURRENT_BOSS' in name:data['status']='stage12_closed_reused_current_goal_non_deploying'
        after=json.dumps(data,ensure_ascii=False,indent=2)+'\n'
    replace_file(path,before,after)
print(json.dumps({'preflight':str(OUT/'preflight.json'),'frozenInputsVerified':len(frozen['inputHashes']),'formalReadOnly':True,'parentCandidate':'613d4f6f','fullAcceptance':False}),flush=True)
