import datetime
import hashlib
import json
import os
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-lease'
BOSS=ROOT/'work/session-boss'
OUT=WORK/'reports/material-proc-session-gap-checkpoint-20261112'

def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
def save(path,value):
    with path.open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

assert not OUT.exists()
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
query="$taskRoot='"+str(ROOT).replace("'","''")+"'; @((Get-CimInstance Win32_Process | Where-Object { $_.Name -eq 'java.exe' -and $_.CommandLine -like ('*'+$taskRoot+'*') }) | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;owned=$true} }) | ConvertTo-Json -Compress"
process=subprocess.run(['powershell','-NoProfile','-Command',query],capture_output=True,text=True,check=True)
assert not process.stdout.strip(),process.stdout
candidate=WORK/'reports/native-repair-aliased-entity-expected-image-candidate-5f283607.json'
assert sha(candidate)=='482fd005e8287ef41e48eced0ca5bd740df8b78580526165a2b550d47f666b10'
for name,wanted in load(candidate)['inputHashes'].items():assert sha(name)==wanted,name
parent_archive=load(WORK/'reports/aliased-entity-expected-image-archive-receipt-20261112.json')
assert sha(parent_archive['archive'])==parent_archive['sha256']
pause=load(BOSS/'archives/weapons-paused-5f283607-boss-priority-20261112/archive-receipt.json')
assert sha(pause['archive'])==pause['archiveSha256'] and sha(pause['snapshotJar'])==pause['snapshotJarSha256']
for row in pause['formalInstancesUnchanged'].values():assert sha(row['path'])==row['sha256']
full=load(WORK/'reports/native-repair-runtime-aliased-entity-expected-image-5f283607.json')
assert full['probe']['failures']==1 and len(full['probe']['cases'])==14
for case in full['probe']['cases'][:-1]:
    assert case['genuineNonNullLeaseAndGenerationBeforeAttack'] and case['actualOriginalPostCombatSaveAuthenticatedCommitted']
    assert all(row['traceComplete'] and row['session_coverageInstalled'] and not row['session_traceRevoked'] for row in case['publications'])
reports=[WORK/'reports/native-repair-runtime-defect-claw-session-gap-5f283607.json',WORK/'reports/native-repair-runtime-defect-claw-writer-gate-watch-5f283607.json',WORK/'reports/native-repair-runtime-defect-claw-domain-retirement-5f283607.json',WORK/'reports/native-repair-runtime-original-nineteen-controls-5f283607.json']
for index,path in enumerate(reports):
    result=load(path);attempt=Path(result['attempt']);terminal=load(attempt/'terminal308.json')
    assert result['exitCode']==0 and terminal['stopSent'] and terminal['completeSave']
    assert result['privateReadGuardProbe']['failures']==0
    assert result['probe']['failures']==(0 if index==3 else 1)
    assert len(result['probe']['cases'])==(19 if index==3 else 1)
    for name,wanted in result['inputHashes'].items():assert sha(name)==wanted,name
    if index in [1,2]:
        diagnostic=load(attempt/'diagnostic-terminal.json')
        assert diagnostic['exitCode']==0 and diagnostic['readOnly'] and not diagnostic['methodInvocations'] and not diagnostic['authorityWrites']
        assert result['exactDomainRetirementWatch']['diagnosticErrors']==0
proof=load(WORK/'reports/material-proc-expected-plan-proof-v2-20261112.json')
assert proof['oracle']['oracleCases']==15121 and proof['oracle']['failures']==0 and not proof['nativeSessionIntegrationAccepted']
for name,wanted in proof['inputHashes'].items():assert sha(name)==wanted,name
failed=WORK/'build/material-proc-expected-plan-proof-20261112'
failed_inputs=load(failed/'inputs-before.json')
assert sha(failed/'MaterialProcExpectedPlanProof.before.java')==failed_inputs[str(WORK/'tests/MaterialProcExpectedPlanProof.java')]
OUT.mkdir(parents=True)
summary={'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'goalTurnClassification':'progress','goalStatus':'active','scope':'Original full NFboss migration acceptance intact','currentCandidate':'5f283607','currentCandidateProductionGraphChanged':False,'originalCompleteSessionsBeforeFirstFailure':13,'originalFullNineteenAccepted':False,'strictFailureSample':'ftboss:defect_claw','nativeLeaseSeed':'genuine paid849680bb only','failedWorldsUsedAsRestartSeeds':False,'firstFalseWriterGate':'ownerWriterPublicationLive bci130 before Session trace revocation','firstExactDomainRetirement':'OwnerFieldExpectedAuthority.changed bci50','actualOriginalMutation':'WeaponMaterialPowers.proc Stamp/Delay/material child/FTBossMaterialPowers root','firstObservedOriginalMaterialBranch':'ANCIENT_METAL bci552 line361','readOnlyDebuggersAccepted':2,'debuggerTargetInvocations':False,'authorityWrites':False,'originalNineteenForeignControlsAccepted':True,'foreignControlCases':19,'pureExpectedPlanOracle':proof['oracle'],'nativeMaterialProcSessionIntegrationAccepted':False,'next':'Bind the predeclared material proc expected program to the genuine closed original MaterialPowersLeaf and observe all four original mutations without weakening any guard; then original19 and required new-JVM restart','fullLifecycleAccepted':False,'installable':False,'deployed':False,'ownedNativeProcessesRemaining':0,'formalInstancesUnchanged':pause['formalInstancesUnchanged'],'frozenCandidateArchive':parent_archive,'frozenPauseArchive':{'archive':pause['archive'],'sha256':pause['archiveSha256'],'snapshotJar':pause['snapshotJar'],'snapshotJarSha256':pause['snapshotJarSha256']},'failedPureOracleSourceRecovery':str(failed/'MaterialProcExpectedPlanProof.before.java')}
save(OUT/'progress.json',summary)
helper=WORK/'tools/reactivate_goal_after_boss_stage12.py';namespace={'__file__':str(helper)};text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('assert not OUT.exists()')],str(helper),'exec'),namespace)
for relative in ['WRITE_OWNER.json','work/session-lease/CURRENT_INCREMENTAL_BATCH.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/relative;before=path.read_text(encoding='utf-8');save(OUT/(relative.replace('/','_')+'.before.json'),{'path':str(path),'text':before,'sha256':sha(path)})
    data=json.loads(before);data.update({'activeRuntimePid':None,'activeRuntimePids':[],'activeDebuggerPid':None,'activeSupervisor':None,'activeWorkstream':'signed-session-material-proc-native-integration-pending','currentGoalStatus':'active','currentFormalInstancePolicy':'read_only_current_goal_no_deployment','fullSignedSessionAccepted':False,'installable':False,'deployed':False,'latestMaterialProcSessionCheckpoint':str(OUT/'progress.json'),'latestMaterialProcSessionArchiveReceipt':str(OUT/'archive-receipt.json'),'originalNineteenForeignControlsAcceptedOn5f283607':True,'nativeMaterialProcSessionIntegrationAccepted':False})
    if 'CURRENT_INCREMENTAL' in relative:data['status']='material_proc_expected_plan_pure_oracle_passed_native_session_integration_pending'
    namespace['replace_file'](path,before,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
selected={Path(__file__),helper,candidate,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.claim',ROOT/'WRITE_OWNER.json',WORK/'CURRENT_INCREMENTAL_BATCH.json',BOSS/'CURRENT_BOSS_BATCH.json',WORK/'MATERIAL_PROC_SESSION_GAP_5F283607.md',WORK/'reports/aliased-entity-expected-image-archive-receipt-20261112.json',BOSS/'archives/weapons-paused-5f283607-boss-priority-20261112/archive-receipt.json',WORK/'tests/MaterialProcExpectedPlanProof.java'}
selected.update([WORK/'tests/exact-melee-configured-controls-849680bb/PositiveWeapon436.java',WORK/'reports/native-exact-melee-configured-controls-preparation-849680bb.json',WORK/'reports/native-repair-runtime-exact-melee-configured-paid-849680bb.json'])
for folder in ['proposal-material-proc-expected-plan','tests/defect-claw-session-gap-5f283607','tests/defect-claw-writer-gate-watch-5f283607','tests/defect-claw-domain-retirement-5f283607','reports/goal-resumption-after-weapon-pause-5f283607-20261112','reports/final-acceptance-audit-5f283607-20261112','build/material-proc-expected-plan-proof-20261112','build/material-proc-expected-plan-proof-v2-20261112']:
    selected.update(path for path in (WORK/folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
for path in (WORK/'tools').iterdir():
    if path.is_file() and any(term in path.name for term in ['defect_claw','material_proc_expected','original_nineteen_controls_5f283607']):selected.add(path)
for path in (WORK/'reports').iterdir():
    if path.is_file() and any(term in path.name for term in ['defect-claw','material-proc-expected-plan','original-nineteen-controls-5f283607']):selected.add(path)
for path in reports:
    selected.add(path);attempt=Path(load(path)['attempt'])
    selected.update(current for current in attempt.iterdir() if current.is_file())
    selected.update(current for current in (attempt/'server').glob('actual-*.json'))
    for relative in ['logs/latest.log','logs/debug.log']:
        current=attempt/'server'/relative
        if current.is_file():selected.add(current)
    for name in ['inputs-before308.json','restart-world-seed.json']:
        for original in load(attempt/name).get('inputHashes',{}):
            current=Path(original)
            if current.resolve().is_relative_to(ROOT) and current.is_file():selected.add(current)
selected.update(path for path in OUT.iterdir() if path.is_file())
restore=OUT/'RESTORE.md'
with restore.open('x',encoding='utf-8') as stream:stream.write('# Active goal checkpoint: material proc signed Session gap\n\nRestore this supplement in the same sole C workspace, together with the immutable candidate5f283607 terminal archive referenced by progress.json. Earlier paused weapon JAR/archive remain recovery history and are not installable addons. Production candidate graph is unchanged; the new expected-plan helper and oracle classes are isolated and not linked into it. All native processes are terminal. Three focused strict failures and19 original foreign controls are preserved, as are the test-only first oracle failure and prior source. No failed world is a restart seed. Genuine paid849680bb remains the only authorized prerequisite seed. Historical D inputs remain external and read-only, with exact runtime input hashes. Both formal instances remain unchanged. Continue native material-proc quote/admission/return integration, then original whole19 and required accepted-save new-JVM continuation. Goal remains active and full acceptance unproven.\n')
selected.add(restore)
archive=WORK/'archives/material-proc-session-gap-5f283607-20261112.zip';manifest={}
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(),path
        name=path.relative_to(ROOT).as_posix();manifest[name]={'sha256':sha(path),'bytes':path.stat().st_size};bundle.write(path,name)
    encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
    for name,row in manifest.items():
        payload=bundle.read(name);assert len(payload)==row['bytes'] and hashlib.sha256(payload).hexdigest()==row['sha256']
for row in pause['formalInstancesUnchanged'].values():assert sha(row['path'])==row['sha256']
receipt={'archive':str(archive),'sha256':sha(archive),'files':len(manifest),'allMembersReadBackAndVerified':True,'goalStatus':'active','originalNineteenForeignControlsAccepted':True,'pureOracleCases':15121,'nativeMaterialProcSessionIntegrationAccepted':False,'originalFullNineteenAccepted':False,'installable':False,'deployed':False,'formalInstancesUnchanged':pause['formalInstancesUnchanged']}
save(OUT/'archive-receipt.json',receipt)
print(json.dumps({'archive':str(archive),'sha256':receipt['sha256'],'files':len(manifest),'goalStatus':'active','nativeSessionIntegrationAccepted':False,'formalUnchanged':True}),flush=True)
