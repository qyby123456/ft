import argparse
import datetime
import difflib
import hashlib
import json
import os
import shutil
import subprocess
import uuid
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
BUILD=WORK/'build/boss-blood-tide-v2'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def save(path,value):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(value,stream,ensure_ascii=False,indent=2)

def digest(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def processes():
    command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease');target=($_.CommandLine -like '*FarmingTales_NeoForge 机械动力*')} } | ConvertTo-Json -Compress"
    result=json.loads(subprocess.check_output(['powershell.exe','-NoProfile','-Command',command],encoding='utf-8-sig'))
    result=result if isinstance(result,list) else [result]
    result=[row for row in result if row['pid']!=os.getpid()]
    assert not any(row['owned'] or row['target'] for row in result),'Owned or target JVM still active'
    return {'ownedJavaPythonRemaining':0,'targetInstanceJavaRemaining':0,'rawCommandLinesRecorded':False,'unrelatedProcessesPreserved':result}

def archive(path,files):
    manifest={}
    with zipfile.ZipFile(path,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
        for item in sorted(set(files)):
            if not item.is_file():continue
            assert item.resolve().is_relative_to(ROOT) and not item.is_symlink()
            name=item.relative_to(ROOT).as_posix()
            manifest[name]={'sha256':digest(item),'bytes':item.stat().st_size}
            bundle.write(item,name)
        bundle.writestr('ARCHIVE_MANIFEST.json',json.dumps(manifest,ensure_ascii=False,indent=2))
    with zipfile.ZipFile(path) as bundle:
        assert bundle.testzip() is None
        for name,row in manifest.items():
            payload=bundle.read(name)
            assert len(payload)==row['bytes'] and hashlib.sha256(payload).hexdigest()==row['sha256']
    return {'archive':str(path),'archiveSha256':digest(path),'archiveBytes':path.stat().st_size,'files':len(manifest),'allFilesReadBackAndHashVerified':True,'failures':0}

parser=argparse.ArgumentParser()
parser.add_argument('candidate_report',type=Path)
arguments=parser.parse_args()
baseline_path=WORK/'reports/blood-tide-baseline-eb7853cb.json'
candidate_path=arguments.candidate_report.resolve()
baseline=load(baseline_path)
candidate=load(candidate_path)
for result in (baseline,candidate):
    terminal=result['terminal']
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and result['probe']['completedScenarios']==7
    assert not result['authenticatedMultiplayerAccepted'] and not result['fullBossAcceptance']
    for name,wanted in result['inputHashes'].items():assert digest(name)==wanted,name
assert baseline['baselineGapObserved'] and not baseline['probe']['nativeBloodDamageEvents']
assert candidate['bloodTideFeatureAccepted'] and candidate['probe']['bloodTideFeatureAccepted']
rows=candidate['probe']['rows']
positive=rows[0]
assert len(positive['castStarts'])==len(positive['castEnds'])==2
assert positive['castStarts'][0]-positive['firstAiTick']==200
assert positive['castStarts'][1]-positive['castStarts'][0]==240
assert all(end-start==34 for start,end in zip(positive['castStarts'],positive['castEnds']))
assert positive['brandStarts'][0]-positive['firstAiTick']==80 and positive['brandStarts'][1]-positive['brandStarts'][0]==240
hits=candidate['probe']['nativeBloodDamageEvents']
incoming=candidate['probe']['nativeBloodIncomingEvents']
assert len(hits)==len(incoming)==3 and [hit['targetIndex'] for hit in hits]==[0,1,1]
assert all(hit['scenario']==0 and hit['actualDamage']>0 and hit['actualSourceIsButcher'] for hit in hits)
assert all(abs(hit['originalIncomingAmount']-14.0*(hit['liveAttackAttribute']/5.5)*2.0)<0.0001 for hit in incoming)
assert incoming[2]['liveAttackAttribute']>incoming[0]['liveAttackAttribute']
for hit in hits:
    start=positive['castStarts'][0] if hit['gameTime']<positive['castStarts'][1] else positive['castStarts'][1]
    assert hit['gameTime']-start==22
for index in (1,2,3,4,6):assert not rows[index]['castStarts']
assert len(rows[5]['castStarts'])==len(rows[5]['castEnds'])==1 and rows[5]['castEnds'][0]-rows[5]['castStarts'][0]==10
built=load(BUILD/'candidate-build-receipt.json')
for name,wanted in built['inputHashes'].items():assert digest(name)==wanted,name
assert 'failures=0;methodsBasicVerified=8;unrelatedMethodsPreserved=4;' in built['proof']
jar=Path(built['jar'])
assert digest(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as changed:
    assert base.namelist()==changed.namelist() and changed.testzip() is None
    assert sorted(name for name in base.namelist() if base.read(name)!=changed.read(name))==built['changedEntries']
    assert len(built['changedEntries'])==2
    assert 'net/farmingtales/ftboss/item/FoodWeaponFinalDamage.class' not in changed.namelist()
previous_path=WORK/'releases/boss-core-stage09-blood-brand-20261112/deployment-receipt.json'
previous=load(previous_path)
target=Path(previous['target']).resolve()
baseline_config=load(ROOT/'baseline.json')
preflight=load(ROOT/'work/session-lease/reports/goal-reactivation-session-diagnostic-20261112.json')
assert target==Path(preflight['formalInstancesBefore']['neoforge']['path']).resolve()
assert digest(target)==previous['deployedSha256']==built['baseJarSha256']
forge=Path(preflight['formalInstancesBefore']['forge']['path'])
forge_before=digest(forge)
assert forge_before==preflight['formalInstancesBefore']['forge']['sha256']
audit=processes()
release=WORK/'releases/boss-core-stage10-blood-tide-20261112'
release.mkdir()
save(release/'predeployment-process-audit.json',audit)
backup=release/'rollback-stage09-qys-bosses-1.21.1.jar'
shutil.copyfile(target,backup)
assert digest(backup)==built['baseJarSha256']
released=release/'ftboss-boss-core-1.21.1-stage10.jar'
shutil.copyfile(jar,released)
assert digest(released)==built['jarSha256']
previous_archive=load(previous_path.parent/'archive-receipt.json')
assert digest(previous_archive['archive'])==previous_archive['archiveSha256']
paused_path=WORK/'archives/weapons-paused-73d460d2-20261112/archive-receipt.json'
paused=load(paused_path)
assert paused['failures']==0 and paused['weaponDevelopmentPaused'] and not paused['installable'] and not paused['deployed']
assert digest(paused['archive'])==paused['archiveSha256'] and digest(paused['snapshotJar'])==paused['snapshotJarSha256']
feature={'feature':'stage10 original Blood Tide server skill','baselineReceipt':str(baseline_path),'candidateReceipt':str(candidate_path),'scenariosPerRuntime':7,'baselineCases':len(baseline['probe']['cases']),'candidateCases':len(candidate['probe']['cases']),'nativeServerTickCallbacks':candidate['probe']['nativeServerTickCallbacks'],'baselineBloodTideCasts':0,'completedBloodTideCasts':2,'canceledBloodTideCasts':1,'actualBloodTideDamageEvents':3,'nativeIncomingDamageEvidence':incoming,'nativeFinalDamageEvidence':hits,'firstBrandDelay':80,'firstTideAfterBrand':200,'windup':22,'recovery':12,'perSkillCooldown':220,'observedTwoSkillRotationInterval':240,'globalCooldown':80,'castRangeAndRadius':8,'coneHalfAngleDegrees':45,'verticalHalfRange':2.5,'phaseAiRangeSightAndLostTargetControlsAccepted':True,'originalBloodBrandTimingAndDamagePreserved':True,'changedClassCount':2,'allOtherClassAndResourceBytesPreserved':True,'basicVerifiedMethods':8,'unchangedHelperMethods':4,'stage03Included':False,'weaponImplementationResumed':False,'fullFourSkillRotationAccepted':False,'clientMotionAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False,'fullBossMigrationComplete':False}
save(release/'feature-verification.json',feature)
save(release/'external-dependencies.json',{'previousReleaseReceipt':str(previous_path),'previousReleaseReceiptSha256':digest(previous_path),'previousArchiveReceipt':str(previous_path.parent/'archive-receipt.json'),'previousArchiveSha256':previous_archive['archiveSha256'],'weaponPauseReceipt':str(paused_path),'weaponPauseReceiptSha256':digest(paused_path),'weaponPauseArchiveSha256':paused['archiveSha256'],'runtimeInputs':{'baseline':baseline['inputHashes'],'candidate':candidate['inputHashes']},'currentBaseJar':built['baseJar'],'currentBaseJarSha256':built['baseJarSha256'],'baselineFormalInputAfterDeployment':'The baseline receipt records the then-live stage09 JAR. Deployment intentionally changes that mutable path; verify the same frozen stage09 bytes in rollback-stage09-qys-bosses-1.21.1.jar or the stage09 release, never rewrite historical receipts.','historicalDInputsReadOnly':True,'allOriginalExternalDependencyLocationsPreserved':True})
restore='# Stage10 Blood Tide restore and rollback\n\nVerify archive-receipt.json and every ARCHIVE_MANIFEST.json member hash and size. Restore only to a NEW empty workspace with destination containment checks. Verify external-dependencies.json and the previous stage09 archive before reuse. Historical D and the Forge instance remain read-only.\n\nThis archive contains the exact deployed Boss JAR, the hash-verified stage09 rollback JAR, scoped sources, compiler/ASM evidence, both saved directed runtime worlds and observer evidence. Runtime mods/libraries remain referenced at their original locations. After stopping the correct NeoForge instance, use the rollback JAR only if its SHA256 matches deployment-receipt.json; never overwrite a running instance or unrelated user files.\n\nBlood Tide alone is added to the previous Blood Brand server controller. Other two active skills, full rotation parity, summon FX and client motion remain pending. No weapon implementation or pending stage03 reward fix is installed. Weapon work is paused; its separate JAR is a noninstallable recovery snapshot. Scratch is archived and retained; do not bypass the earlier deletion rejection. No authenticated player combat, rewards, client rendering or full battle acceptance is inferred.\n'
(release/'RESTORE.md').write_text(restore,encoding='utf-8')
save(release/'deployment-intent.json',{'target':str(target),'beforeSha256':digest(target),'candidateSha256':digest(released),'backup':str(backup),'backupSha256':digest(backup),'backupVerified':True,'authorization':'User requested deployment and archive after each completed Boss function','formalInstanceLaunchAuthorizedByThisStep':False})
assert processes()['ownedJavaPythonRemaining']==0
assert digest(target)==built['baseJarSha256']
temporary=target.with_name(target.name+'.stage10-'+uuid.uuid4().hex[:8]+'.tmp')
assert temporary.parent==target.parent and not temporary.exists()
shutil.copyfile(released,temporary)
assert digest(temporary)==built['jarSha256']
os.replace(temporary,target)
assert digest(target)==built['jarSha256'] and digest(forge)==forge_before
deployment={'deployedAt':datetime.datetime.now().astimezone().isoformat(),'target':str(target),'deployedSha256':digest(target),'backup':str(backup),'backupSha256':digest(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'formalInstanceLaunched':False,'fullBossMigrationComplete':False,'fullBossInstallable':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'authenticatedRewardsAccepted':False,'completeBattleAccepted':False,'fullFourSkillRotationAccepted':False,'clientMotionAdapterIncluded':False,'stage03CandidateIncluded':False,'stage03MaterialDropCorrectionStillPending':True,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_before,'historicalDModified':False,'inputHashes':{str(path):digest(path) for path in (Path(__file__),baseline_path,candidate_path,BUILD/'candidate-build-receipt.json',release/'feature-verification.json')}}
save(release/'deployment-receipt.json',deployment)
canonical=WORK/'boss-core/src/main/java/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
shutil.copyfile(canonical,release/'stage09-controller-source.java')
old=canonical.read_text(encoding='utf-8');new=(WORK/'proposal-stage10-blood-tide/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java').read_text(encoding='utf-8')
delta=list(difflib.unified_diff(old.splitlines(),new.splitlines(),lineterm=''))[2:]
patch='*** Begin Patch\n*** Update File: '+canonical.relative_to(ROOT).as_posix()+'\n'+'\n'.join('@@' if line.startswith('@@') else line for line in delta)+'\n*** End Patch\n'
subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
assert canonical.read_text(encoding='utf-8')==new
save(release/'release-file-manifest.json',{path.name:{'sha256':digest(path),'bytes':path.stat().st_size} for path in release.iterdir() if path.is_file()})
files=list(release.rglob('*'))+list(BUILD.rglob('*'))+list((WORK/'build/boss-blood-tide-v1').rglob('*'))+list((WORK/'build/boss-blood-tide-audit-20261112').rglob('*'))+list((WORK/'proposal-stage10-blood-tide').rglob('*'))
files+=[baseline_path,candidate_path,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.claim',canonical,WORK/'tests/BloodTideBoundaryProof.java',WORK/'tests/blood-tide-boundary-v2/BloodTideBoundaryProof.java',WORK/'tests/blood-tide-v1/BossBloodTideRuntimeProbe.java',paused_path]
files+=[WORK/'tools'/name for name in ('prepare_boss_blood_tide_stage10.py','prepare_boss_blood_tide_candidate.py','build_boss_blood_tide_stage10.py','build_boss_blood_tide_stage10_v2.py','run_boss_blood_tide_probe_v1.py',Path(__file__).name)]
files+=[previous_path.parent/name for name in ('feature-verification.json','deployment-receipt.json','archive-receipt.json','RESTORE.md','external-dependencies.json')]
files+=[WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java',WORK/'build/boss-butcher-active-skill-audit-20261112/blood-brand-callsite-audit.json']
for result in (baseline,candidate):
    runtime=Path(result['attempt'])
    files+=[path for path in runtime.iterdir() if path.is_file()]
    for folder in ('observer-classes','observer-resources','isolated-home','isolated-tmp','server/logs','server/crash-reports','server/world'):files+=list((runtime/folder).rglob('*'))
archive_path=WORK/'archives/boss-core-stage10-blood-tide-20261112.zip'
archived=archive(archive_path,files)
archived.update({'deployedIncrementSha256':digest(target),'fullBossInstallable':False,'scratchArchivedAndRetained':True,'scratchDeletionNotAttemptedBecausePriorPolicyRejection':True,'stage09EvidenceReusedNotRerun':True})
save(release/'archive-receipt.json',archived)
terminal=processes()
terminal.update({'stage':'stage10','deployedSha256':digest(target),'backupSha256':digest(backup),'archiveSha256':archived['archiveSha256'],'forgeUnchanged':digest(forge)==forge_before,'weaponFunctionalDevelopmentPaused':True,'weaponSnapshotInstallable':False,'stage03PositiveKillAccepted':False,'authenticatedRewardsAccepted':False,'remainingTwoSkillsAndFullRotationAccepted':False,'clientRenderingAccepted':False,'completeBattleAccepted':False,'fullBossInstallable':False,'deploymentTemporaryFiles':[path.name for path in target.parent.glob(target.name+'.stage*.tmp')]})
assert not terminal['deploymentTemporaryFiles']
terminal_path=WORK/'reports/stage10-terminal-audit-20261112.json'
save(terminal_path,terminal)
closeout=WORK/'archives/boss-core-stage10-closeout-20261112.zip'
for path in (ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json'):
    data=load(path)
    data.update({'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'bossActiveAttempt':None,'activeRuntimePid':None,'activeSupervisor':None,'activeWorkstream':'boss-stage10-closed-next-single-original-melee-skill','weaponFunctionalDevelopment':'paused_by_user','weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidationResumed':False,'currentGoalStatus':'paused','currentFormalInstancePolicy':'verified_boss_increments_only_with_backup_and_archive','latestBossIncrementStatus':'stage10 Blood Tide server skill verified/deployed/archived','latestDeploymentReceipt':str(release/'deployment-receipt.json'),'latestBossDeploymentReceipt':str(release/'deployment-receipt.json'),'latestDeployedSha256':digest(target),'latestBossArchive':str(archive_path),'latestBossArchiveSha256':archived['archiveSha256'],'latestBossArchiveReceipt':str(release/'archive-receipt.json'),'latestStageArchive':str(archive_path),'latestStageArchiveSha256':archived['archiveSha256'],'latestStageArchiveReceipt':str(release/'archive-receipt.json'),'latestStageCloseoutArchive':str(closeout),'latestStageCloseoutArchiveReceipt':str(release/'closeout-archive-receipt.json'),'stage10TerminalAudit':str(terminal_path),'stage10CandidateReceipt':str(candidate_path),'latestVerifiedBossRuntimeReceipt':str(candidate_path),'nextBossAction':'Compare and restore one original melee skill, CLEAVER_SWEEP or BONE_CRUSH; full four-skill rotation and client motion remain pending. Weapons stay paused.','fullBossInstallable':False,'fullBossMigrationComplete':False,'ownedJavaPythonRemaining':0,'latestWeaponPauseCheckpoint':str(paused_path),'weaponLatestSnapshotJar':paused['snapshotJar'],'weaponLatestSnapshotInstallable':False})
    if path.name=='CURRENT_BOSS_BATCH.json':
        data.update({'status':'stage10_blood_tide_completed_deployed_archived_weapons_paused','currentStage':'stage10 Blood Tide server skill complete; two original melee skills remain pending','latestFormalDeployment':'stage10 Blood Tide on stage09 Blood Brand','latestStageDeploymentReceipt':str(release/'deployment-receipt.json')})
        data.setdefault('completedBossIncrements',[]).append('stage10 original Blood Tide server cast/release and shared two-skill cooldown rotation')
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
progress=WORK/'DEPLOYED_PROGRESS.md'
with progress.open('a',encoding='utf-8') as stream:
    stream.write('\n\n## 2026-11-12 Stage10: original Blood Tide server skill\n\nAdded original Blood Tide to the previous Blood Brand controller. Seven directed native scenarios pass in both baseline and candidate (1950 server callbacks per process), including the original 22-tick windup, 12-tick recovery, 220 per-skill cooldown, 80 global cooldown, radius 8, 45-degree cone and phase/AI/range/sight/lost-target guards. Two casts complete, one cancels, and three genuine native damage events match original attribute scaling; Blood Brand retains its 80 first delay and 240 interval. Only two controller class bytes change; all other classes/resources remain identical. Eight methods pass ASM and four unrelated helper methods remain identical.\n\nDeployed SHA256: `'+digest(target)+'`. Stage09 rollback verified. Archive: `archives/boss-core-stage10-blood-tide-20261112.zip`, SHA256 `'+archived['archiveSha256']+'`. Both isolated processes exited zero after normal complete saves. Weapon source/evidence and separate noninstallable recovery JAR remain paused in `archives/weapons-paused-73d460d2-20261112`. Forge and historical D were unchanged. Two melee skills, full rotation, summon/client motion, authenticated rewards and complete battle remain pending.\n')
remaining=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md'
remaining.write_text('# Boss remaining checkpoint after stage10\n\nStage10 Blood Tide server casting and release is verified, deployed and archived. See the stage10 feature/deployment/archive receipts and terminal audit. Its original shared two-skill rotation preserves Blood Brand; the other two original skills (CLEAVER_SWEEP and BONE_CRUSH) are still not enabled. Restore exactly one at a time after observing the current native baseline. Preserve original damage, sound, knockback, windup, cooldown, targeting and combat scheduler.\n\nWeapon work and its old signed-session goal are explicitly paused. A separate hash-verified weapon recovery JAR and 2995-file delta checkpoint exist, but are not installable or accepted as a runnable addon. No weapon development/testing is authorized until the user resumes it.\n\nStage03 ten material registrations and reward correction remain archived and undeployed pending real authenticated positive player kills. The live material-reward omission remains known. Do not substitute scratch or NPC targets for authenticated kill acceptance. Summon FX, full four-skill rotation parity, chase/hit balance, client motion/rendering/GL, authenticated multiplayer and complete battles remain pending. Formal NeoForge is stage10 with stage09 rollback; Forge/historical D remain read-only. Keep the same sole C writer and deploy/archive each completed verified Boss segment.\n',encoding='utf-8')
closed=archive(closeout,[ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',progress,remaining,terminal_path,release/'archive-receipt.json',release/'release-file-manifest.json',release/'deployment-receipt.json',release/'feature-verification.json',release/'RESTORE.md',paused_path,Path(__file__)])
save(release/'closeout-archive-receipt.json',closed)
assert digest(target)==built['jarSha256'] and digest(backup)==built['baseJarSha256'] and digest(forge)==forge_before
print(json.dumps({'stage10Deployed':True,'deployedSha256':digest(target),'archiveFiles':archived['files'],'archiveSha256':archived['archiveSha256'],'closeoutArchiveSha256':closed['archiveSha256'],'ownedJavaPythonRemaining':0,'weaponFunctionalDevelopmentPaused':True,'fullBossMigrationComplete':False}),flush=True)
