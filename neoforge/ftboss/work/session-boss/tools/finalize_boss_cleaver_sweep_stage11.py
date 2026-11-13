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
BUILD=WORK/'build/boss-cleaver-sweep-v1'

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
baseline_path=WORK/'reports/cleaver-sweep-baseline-5db0470f.json'
candidate_path=arguments.candidate_report.resolve()
baseline=load(baseline_path)
candidate=load(candidate_path)
for result in (baseline,candidate):
    terminal=result['terminal']
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and result['probe']['completedScenarios']==7
    assert not result['authenticatedMultiplayerAccepted'] and not result['fullBossAcceptance'] and not result['probe']['authorityWrittenByReflection']
    for name,wanted in result['inputHashes'].items():assert digest(name)==wanted,name
assert baseline['baselineGapObserved'] and not baseline['probe']['nativeBloodDamageEvents']
assert candidate['cleaverSweepFeatureAccepted'] and candidate['probe']['cleaverSweepFeatureAccepted']
rows=candidate['probe']['rows']
positive=rows[0]
assert len(positive['castStarts'])==len(positive['castEnds'])==2
assert positive['castStarts'][0]-positive['firstAiTick']==314
assert positive['castStarts'][1]-positive['castStarts'][0]==338
assert all(end-start==24 for start,end in zip(positive['castStarts'],positive['castEnds']))
assert positive['brandStarts'][0]-positive['firstAiTick']==80 and positive['brandStarts'][1]-positive['brandStarts'][0]==338
assert positive['tideStarts'][0]-positive['firstAiTick']==200 and positive['tideStarts'][1]-positive['tideStarts'][0]==338
hits=candidate['probe']['nativeBloodDamageEvents']
incoming=candidate['probe']['nativeBloodIncomingEvents']
assert len(hits)==len(incoming)==3 and [hit['targetIndex'] for hit in hits]==[0,1,1]
assert all(hit['scenario']==0 and hit['actualDamage']>0 and hit['actualSourceIsButcher'] for hit in hits)
assert all(abs(hit['originalIncomingAmount']-20.0*(hit['liveAttackAttribute']/5.5)*2.0)<0.0001 for hit in incoming)
assert incoming[2]['liveAttackAttribute']>incoming[0]['liveAttackAttribute']
for hit in hits:
    start=positive['castStarts'][0] if hit['gameTime']<positive['castStarts'][1] else positive['castStarts'][1]
    assert hit['gameTime']-start==14
knocks=[row for row in candidate['probe']['nativeSweepKnockbackEvents'] if row['scenario']==0 and abs(row['strength']-0.45)<0.00001]
assert len(knocks)==3 and all(abs(row['ratioX']-row['expectedRatioX'])<0.00001 and abs(row['ratioZ']-row['expectedRatioZ'])<0.00001 for row in knocks)
sounds=[row for row in candidate['probe']['nativeSweepSoundEvents'] if row['scenario']==0]
assert len(sounds)==2 and all(row['sound']=='minecraft:entity.player.attack.strong' and abs(row['pitch']-0.65)<0.00001 and row['volume']==1.0 for row in sounds)
for index in (1,2,3,4,6):assert not rows[index]['castStarts']
assert len(rows[5]['castStarts'])==len(rows[5]['castEnds'])==1 and rows[5]['castEnds'][0]-rows[5]['castStarts'][0]==10
built=load(BUILD/'candidate-build-receipt.json')
for name,wanted in built['inputHashes'].items():assert digest(name)==wanted,name
assert 'failures=0;methodsBasicVerified=8;unrelatedMethodsPreserved=6;' in built['proof'] and 'originalSchedulerInstructionsPreserved=true' in built['proof']
jar=Path(built['jar'])
assert digest(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as changed:
    assert base.namelist()==changed.namelist() and changed.testzip() is None
    assert sorted(name for name in base.namelist() if base.read(name)!=changed.read(name))==built['changedEntries']==['net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.class']
    assert 'net/farmingtales/ftboss/item/FoodWeaponFinalDamage.class' not in changed.namelist()
previous_path=WORK/'releases/boss-core-stage10-blood-tide-20261112/deployment-receipt.json'
previous=load(previous_path)
previous_feature=load(previous_path.parent/'feature-verification.json')
previous_archive=load(previous_path.parent/'archive-receipt.json')
previous_terminal=WORK/'reports/stage10-terminal-audit-20261112.json'
assert previous_feature['candidateCases']==59 and previous_archive['failures']==0
assert previous_archive['deployedIncrementSha256']==previous['deployedSha256']==built['baseJarSha256']
stage11_preflight=WORK/'build/boss-cleaver-sweep-audit-20261112/preflight.json'
assert load(stage11_preflight)['stage10AcceptanceReusedWithoutRerun']
target=Path(previous['target']).resolve()
formal=load(ROOT/'work/session-lease/reports/goal-reactivation-session-diagnostic-20261112.json')['formalInstancesBefore']
assert target==Path(formal['neoforge']['path']).resolve() and digest(target)==previous['deployedSha256']
forge=Path(formal['forge']['path'])
forge_before=digest(forge)
assert forge_before==formal['forge']['sha256']
paused_path=WORK/'archives/weapons-paused-73d460d2-20261112/archive-receipt.json'
paused=load(paused_path)
assert paused['failures']==0 and paused['weaponDevelopmentPaused'] and not paused['installable'] and not paused['weaponStandaloneRunnableJarAccepted']
audit=processes()
assert not list(target.parent.glob(target.name+'.stage*.tmp'))
release=WORK/'releases/boss-core-stage11-cleaver-sweep-20261112'
release.mkdir()
save(release/'predeployment-process-audit.json',audit)
backup=release/'rollback-stage10-qys-bosses-1.21.1.jar'
shutil.copyfile(target,backup)
assert digest(backup)==built['baseJarSha256']
released=release/'ftboss-boss-core-1.21.1-stage11.jar'
shutil.copyfile(jar,released)
assert digest(released)==built['jarSha256']
reused={'stage10CandidateCases':59,'stage10FullProbeRerun':False,'featureReceipt':str(previous_path.parent/'feature-verification.json'),'deploymentReceipt':str(previous_path),'normalExitReceipt':str(previous_terminal),'archiveReceipt':str(previous_path.parent/'archive-receipt.json'),'archiveSha256':previous_archive['archiveSha256'],'previousRollbackSha256':previous['backupSha256']}
feature={'feature':'stage11 original Cleaver Sweep server skill only','reusedStage10Evidence':reused,'baselineReceipt':str(baseline_path),'candidateReceipt':str(candidate_path),'scenariosPerRuntime':7,'baselineCases':len(baseline['probe']['cases']),'candidateCases':len(candidate['probe']['cases']),'nativeServerTickCallbacks':candidate['probe']['nativeServerTickCallbacks'],'baselineSweepCasts':0,'completedSweepCasts':2,'canceledSweepCasts':1,'nativeDamageEvents':hits,'nativeIncomingEvents':incoming,'nativeOriginalKnockbackEvents':knocks,'nativeOriginalSoundEvents':sounds,'windup':14,'recovery':10,'perSkillCooldown':160,'globalCooldown':80,'firstSweepAfterExistingSkills':314,'observedThreeSkillRotationInterval':338,'castRangeAndRadius':4.5,'coneHalfAngleDegrees':60,'verticalHalfRange':2.5,'baseDamage':20.0,'damageType':'ftboss:butcher_melee','successfulHitKnockback':0.45,'soundPitch':0.65,'relatedMethodsBasicVerified':8,'unrelatedHelperAndSchedulerMethodsPreserved':6,'onlyOneClassChanged':True,'allOtherClassesAndResourcesByteIdentical':True,'boneCrushEnabled':False,'weaponDevelopmentPaused':True,'fullFourSkillRotationAccepted':False,'clientMotionAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False,'fullBossInstallable':False,'fullBossMigrationComplete':False}
save(release/'feature-verification.json',feature)
save(release/'external-dependencies.json',{'reusedStage10Evidence':reused,'stage10ReceiptHashes':{str(path):digest(path) for path in (previous_path,previous_path.parent/'feature-verification.json',previous_path.parent/'archive-receipt.json',previous_terminal)},'weaponPauseReceipt':str(paused_path),'weaponPauseReceiptSha256':digest(paused_path),'weaponArchiveSha256':paused['archiveSha256'],'runtimeInputHashes':{'baseline':baseline['inputHashes'],'candidate':candidate['inputHashes']},'frozenBaseJar':built['baseJar'],'frozenBaseJarSha256':built['baseJarSha256'],'historicalDReadOnly':True,'allExternalInputsAtOriginalLocations':True})
(release/'RESTORE.md').write_text('# Stage11 Cleaver Sweep restore and rollback\n\nVerify archive-receipt.json, ARCHIVE_MANIFEST.json, all member hashes and sizes, then restore into a NEW empty workspace with destination containment checks. Verify external-dependencies.json and the frozen stage10 evidence; its 59 checks and saved exits are reused, not rerun. Historical D and Forge remain read-only.\n\nThis archive contains the deployed Boss JAR, exact hash-verified stage10 rollback, scoped source, frozen Forge/native callsite comparison, directed compiler/ASM evidence and both saved native runtime worlds. Runtime dependencies remain referenced at their original locations. Stop the correct NeoForge instance before any authorized rollback; verify deployment-receipt.json first. Never replace a running instance or unrelated user file.\n\nOnly original Cleaver Sweep is added: 14 windup, 10 recovery, 160 per-skill cooldown, radius/cast range 4.5, 60-degree cone, native melee damage/sound and 0.45 successful-hit knockback. The shared scheduler instructions are unchanged. Bone Crush remains disabled. Weapon work remains paused; its separate recovery JAR is not installable. Stage03 authenticated reward fix, client motion/GL, summon FX, authenticated multiplayer and complete battle remain pending. Prior scratch stays archived and retained without retrying the earlier deletion rejection.\n',encoding='utf-8')
save(release/'deployment-intent.json',{'target':str(target),'beforeSha256':digest(target),'candidateSha256':digest(released),'backup':str(backup),'backupSha256':digest(backup),'backupVerified':True,'authorization':'Explicit delegated instruction: one confirmed Boss gap, verify, back up, deploy and independently archive','formalInstanceLaunched':False})
assert processes()['ownedJavaPythonRemaining']==0 and digest(target)==built['baseJarSha256']
temporary=target.with_name(target.name+'.stage11-'+uuid.uuid4().hex[:8]+'.tmp')
assert temporary.parent==target.parent and not temporary.exists()
shutil.copyfile(released,temporary)
assert digest(temporary)==built['jarSha256']
os.replace(temporary,target)
assert digest(target)==built['jarSha256'] and digest(forge)==forge_before
deployment={'deployedAt':datetime.datetime.now().astimezone().isoformat(),'target':str(target),'deployedSha256':digest(target),'backup':str(backup),'backupSha256':digest(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'formalInstanceLaunched':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'authenticatedRewardsAccepted':False,'completeBattleAccepted':False,'boneCrushEnabled':False,'fullFourSkillRotationAccepted':False,'stage03CandidateIncluded':False,'stage03MaterialDropCorrectionStillPending':True,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_before,'historicalDModified':False,'inputHashes':{str(path):digest(path) for path in (Path(__file__),baseline_path,candidate_path,BUILD/'candidate-build-receipt.json',release/'feature-verification.json')}}
save(release/'deployment-receipt.json',deployment)
canonical=WORK/'boss-core/src/main/java/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java'
shutil.copyfile(canonical,release/'stage10-controller-source.java')
old=canonical.read_text(encoding='utf-8')
new=(WORK/'proposal-stage11-cleaver-sweep/net/farmingtales/ftboss/butcherskills/ButcherBloodBrand.java').read_text(encoding='utf-8')
delta=list(difflib.unified_diff(old.splitlines(),new.splitlines(),lineterm=''))[2:]
patch='*** Begin Patch\n*** Update File: '+canonical.relative_to(ROOT).as_posix()+'\n'+'\n'.join('@@' if line.startswith('@@') else line for line in delta)+'\n*** End Patch\n'
subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe','--codex-run-as-apply-patch',patch],check=True,stdout=subprocess.DEVNULL)
assert canonical.read_text(encoding='utf-8')==new
save(release/'release-file-manifest.json',{path.name:{'sha256':digest(path),'bytes':path.stat().st_size} for path in release.iterdir() if path.is_file()})
files=list(release.rglob('*'))+list(BUILD.rglob('*'))+list((WORK/'build/boss-cleaver-sweep-audit-20261112').rglob('*'))+list((WORK/'proposal-stage11-cleaver-sweep').rglob('*'))
files+=[baseline_path,candidate_path,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.claim',canonical,WORK/'tests/CleaverSweepBoundaryProof.java',WORK/'tests/cleaver-sweep-v1/BossCleaverSweepRuntimeProbe.java',paused_path,previous_terminal]
files+=[WORK/'tools'/name for name in ('preflight_boss_stage11.py','prepare_boss_cleaver_sweep_stage11.py','confirm_and_prepare_cleaver_sweep_stage11.py','build_boss_cleaver_sweep_stage11.py','build_boss_cleaver_sweep_stage11_v2.py','run_boss_cleaver_sweep_probe_v1.py',Path(__file__).name)]
files+=[previous_path.parent/name for name in ('feature-verification.json','deployment-receipt.json','archive-receipt.json','closeout-archive-receipt.json','RESTORE.md','external-dependencies.json')]
files+=[WORK/'build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java',WORK/'build/boss-butcher-phase-callsite-audit-20261112/original-sources/net/farmingtales/ftboss/boss/ai/ButcherCombat.java',WORK/'build/boss-butcher-active-skill-audit-20261112/source-recovery-receipt.json']
for result in (baseline,candidate):
    runtime=Path(result['attempt'])
    files+=[path for path in runtime.iterdir() if path.is_file()]
    for folder in ('observer-classes','observer-resources','isolated-home','isolated-tmp','server/logs','server/crash-reports','server/world'):files+=list((runtime/folder).rglob('*'))
archive_path=WORK/'archives/boss-core-stage11-cleaver-sweep-20261112.zip'
archived=archive(archive_path,files)
archived.update({'deployedIncrementSha256':digest(target),'fullBossInstallable':False,'scratchArchivedAndRetained':True,'scratchDeletionNotAttemptedBecausePriorPolicyRejection':True,'stage10EvidenceReusedNotRerun':True})
save(release/'archive-receipt.json',archived)
terminal=processes()
terminal.update({'stage':'stage11','deployedSha256':digest(target),'backupSha256':digest(backup),'archiveSha256':archived['archiveSha256'],'forgeUnchanged':digest(forge)==forge_before,'weaponFunctionalDevelopmentPaused':True,'weaponSnapshotInstallable':False,'stage03PositiveKillAccepted':False,'authenticatedRewardsAccepted':False,'boneCrushEnabled':False,'fullFourSkillRotationAccepted':False,'clientRenderingAccepted':False,'completeBattleAccepted':False,'fullBossInstallable':False,'deploymentTemporaryFiles':[path.name for path in target.parent.glob(target.name+'.stage*.tmp')],'earlyBuildFailureRecorded':True,'halfWrittenDeploymentDetected':False,'stage10EvidenceReusedWithoutRerun':True})
assert not terminal['deploymentTemporaryFiles']
terminal_path=WORK/'reports/stage11-terminal-audit-20261112.json'
save(terminal_path,terminal)
closeout=WORK/'archives/boss-core-stage11-closeout-20261112.zip'
for path in (ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json'):
    data=load(path)
    data.update({'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'bossActiveAttempt':None,'activeRuntimePid':None,'activeSupervisor':None,'activeWorkstream':'boss-stage11-closed','weaponFunctionalDevelopment':'paused_by_user','weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidationResumed':False,'currentGoalStatus':'paused','currentFormalInstancePolicy':'verified_boss_increments_only_with_backup_and_archive','latestBossIncrementStatus':'stage11 original Cleaver Sweep server skill verified/deployed/archived','latestDeploymentReceipt':str(release/'deployment-receipt.json'),'latestBossDeploymentReceipt':str(release/'deployment-receipt.json'),'latestDeployedSha256':digest(target),'latestBossArchive':str(archive_path),'latestBossArchiveSha256':archived['archiveSha256'],'latestBossArchiveReceipt':str(release/'archive-receipt.json'),'latestStageArchive':str(archive_path),'latestStageArchiveSha256':archived['archiveSha256'],'latestStageArchiveReceipt':str(release/'archive-receipt.json'),'latestStageCloseoutArchive':str(closeout),'latestStageCloseoutArchiveReceipt':str(release/'closeout-archive-receipt.json'),'stage11TerminalAudit':str(terminal_path),'stage11CandidateReceipt':str(candidate_path),'latestVerifiedBossRuntimeReceipt':str(candidate_path),'nextBossAction':'Current single-gap batch is closed. Next authorized Boss batch may compare frozen BONE_CRUSH and genuine native baseline only; client motion, authenticated rewards and full battle remain pending. Weapons stay paused.','fullBossInstallable':False,'fullBossMigrationComplete':False,'ownedJavaPythonRemaining':0,'latestWeaponPauseCheckpoint':str(paused_path),'weaponLatestSnapshotJar':paused['snapshotJar'],'weaponLatestSnapshotInstallable':False})
    if path.name=='CURRENT_BOSS_BATCH.json':
        data.update({'status':'stage11_cleaver_sweep_completed_deployed_archived_weapons_paused','currentStage':'stage11 original Cleaver Sweep single gap closed; Bone Crush remains disabled','latestFormalDeployment':'stage11 Cleaver Sweep on stage10 Blood Tide','latestStageDeploymentReceipt':str(release/'deployment-receipt.json')})
        data.setdefault('completedBossIncrements',[]).append('stage11 original Cleaver Sweep server release, cone, sound and knockback; original shared scheduler preserved')
    path.write_text(json.dumps(data,ensure_ascii=False,indent=2)+chr(10),encoding='utf-8')
progress=WORK/'DEPLOYED_PROGRESS.md'
with progress.open('a',encoding='utf-8') as stream:
    stream.write('\n\n## 2026-11-12 Stage11: original Cleaver Sweep single-gap migration\n\nStage10 59-check acceptance, normal saved terminals, rollback and archive are referenced directly and were not rerun. Preflight records the archived stage10 early proof-count failure; no owned processes, target temporary deployment files or half writes remained. New baseline and candidate each execute seven directed native scenarios. The original Forge pre-melee skill callsite and genuine native zero-Sweep baseline confirm the selected gap.\n\nOnly Cleaver Sweep is added. Original 14-tick windup, 10-tick recovery, 160 per-skill cooldown, 80 global cooldown, radius/cast range 4.5, 60-degree cone, melee damage/source, strong-attack sound at pitch 0.65 and successful-hit knockback 0.45 are preserved. Two casts complete, one cancels and three genuine native hits match original attribute scaling. Cone/radius/vertical, phase/AI/range/sight and target-loss controls pass. Eight related methods pass ASM; six helper/scheduler method bodies and all other class/resource bytes remain unchanged. One controller class changes.\n\nDeployed SHA256 `'+digest(target)+'`; stage10 rollback verified. Independent archive `archives/boss-core-stage11-cleaver-sweep-20261112.zip`, SHA256 `'+archived['archiveSha256']+'`. Both owned JVMs saved normally and exited zero. The first generated build failed its missing-base-path preflight before compilation; the failed tool and correction are preserved in the archive. Weapon work stays paused and its separate recovery JAR stays noninstallable. Bone Crush, full rotation parity, client motion/GL, authenticated reward kills and complete battle are unaccepted. Forge and historical D remain unchanged.\n')
remaining=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md'
remaining.write_text('# Boss remaining checkpoint after stage11\n\nStage11 restores only original Cleaver Sweep on stage10. It is verified, deployed and independently archived with exact stage10 rollback. Stage10 59-check/normal-exit/rollback/archive evidence is reused directly, not rerun. Only one controller class changes; the shared scheduler and six unrelated helper methods remain identical.\n\nBone Crush remains disabled and unaccepted. A later authorized batch must first compare its frozen Forge callsite against genuine native behavior before implementing that single gap. Full four-skill rotation parity, summon FX, phase chase/hit balance and client motion/rendering/GL remain pending. Do not infer these from this directed NPC-only server acceptance.\n\nStage03 basic material registration and reward correction remain archived but undeployed pending authenticated positive player kills. The live material-reward omission remains known. Authenticated multiplayer, rewards and full battles remain unaccepted; fullBossInstallable and fullBossMigrationComplete are false.\n\nWeapon work and its old signed-session goal remain paused. The separate hash-verified recovery JAR in weapons-paused-73d460d2-20261112 is not a runnable or installable addon. Do not resume weapon development/testing until explicitly requested. Keep the same sole C writer. Forge and historical D remain read-only; deploy/archive each fully verified authorized Boss segment. Scratch is archived and retained without retrying the prior deletion rejection.\n',encoding='utf-8')
closed=archive(closeout,[ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',progress,remaining,terminal_path,release/'archive-receipt.json',release/'release-file-manifest.json',release/'deployment-receipt.json',release/'feature-verification.json',release/'RESTORE.md',paused_path,Path(__file__)])
save(release/'closeout-archive-receipt.json',closed)
assert digest(target)==built['jarSha256'] and digest(backup)==built['baseJarSha256'] and digest(forge)==forge_before
print(json.dumps({'stage11Deployed':True,'selectedSkill':'CLEAVER_SWEEP','candidateCases':len(candidate['probe']['cases']),'deployedSha256':digest(target),'archiveFiles':archived['files'],'archiveSha256':archived['archiveSha256'],'closeoutArchiveSha256':closed['archiveSha256'],'ownedJavaPythonRemaining':0,'weaponDevelopmentPaused':True,'fullBossMigrationComplete':False}),flush=True)

