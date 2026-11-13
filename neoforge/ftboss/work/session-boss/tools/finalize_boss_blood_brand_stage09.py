import datetime
import difflib
import hashlib
import json
import os
from pathlib import Path
import shutil
import subprocess
import uuid
import zipfile

ROOT = Path(__file__).resolve().parents[3]
WORK = ROOT / 'work/session-boss'
BUILD = WORK / 'build/boss-blood-brand-v1'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def save(path, value):
    with Path(path).open('x', encoding='utf-8') as stream:
        json.dump(value, stream, ensure_ascii=False, indent=2)

def digest(path):
    checksum = hashlib.sha256()
    with Path(path).open('rb') as stream:
        for block in iter(lambda: stream.read(1048576), b''):
            checksum.update(block)
    return checksum.hexdigest()

def patch_text(path, new):
    old = path.read_text(encoding='utf-8-sig') if path.exists() else None
    patch = '*** Begin Patch' + chr(10)
    if old is None:
        patch += '*** Add File: ' + path.relative_to(ROOT).as_posix() + chr(10)
        patch += ''.join('+' + line + chr(10) for line in new.splitlines())
    else:
        delta = list(difflib.unified_diff(old.splitlines(), new.splitlines(), lineterm=''))[2:]
        if not delta:
            return
        patch += '*** Update File: ' + path.relative_to(ROOT).as_posix() + chr(10)
        patch += chr(10).join('@@' if line.startswith('@@') else line for line in delta) + chr(10)
    patch += '*** End Patch' + chr(10)
    subprocess.run(['C:/Users/qyby/AppData/Local/OpenAI/Codex/bin/b6c7fc9497179c98/codex.exe', '--codex-run-as-apply-patch', patch], check=True, stdout=subprocess.DEVNULL)

def audit_processes():
    command = "Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -like '*session-boss*' -or $_.CommandLine -like '*session-lease*');target=($_.CommandLine -like '*FarmingTales_NeoForge 机械动力*')} } | ConvertTo-Json -Compress"
    result = json.loads(subprocess.check_output(['powershell.exe','-NoProfile','-Command',command], encoding='utf-8-sig'))
    result = result if isinstance(result,list) else [result]
    result = [row for row in result if row['pid'] != os.getpid()]
    assert not any(row['owned'] or row['target'] for row in result), 'Owned or formal-instance JVM remains active'
    return {'ownedJavaPythonRemaining':0,'targetInstanceJavaRemaining':0,'rawCommandLinesRecorded':False,'unrelatedProcessesPreserved':result}

def archive_verified(path, files):
    manifest = {}
    with zipfile.ZipFile(path,'x',compression=zipfile.ZIP_DEFLATED) as archive:
        for item in sorted(set(files)):
            if not item.is_file():
                continue
            assert item.resolve().is_relative_to(ROOT) and not item.is_symlink()
            name = item.relative_to(ROOT).as_posix()
            manifest[name] = {'bytes':item.stat().st_size,'sha256':digest(item)}
            archive.write(item,name)
        archive.writestr('ARCHIVE-MANIFEST.json',json.dumps(manifest,ensure_ascii=False,indent=2))
    with zipfile.ZipFile(path) as archive:
        assert archive.testzip() is None
        for name,row in manifest.items():
            payload = archive.read(name)
            assert len(payload)==row['bytes'] and hashlib.sha256(payload).hexdigest()==row['sha256']
    return {'archive':str(path),'archiveSha256':digest(path),'archiveBytes':path.stat().st_size,'files':len(manifest),'allFilesReadBackAndHashVerified':True,'failures':0}

owner='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==owner and load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']==owner
previous_release=WORK/'releases/boss-core-stage08-melee-cadence-20261112'
previous_path=previous_release/'deployment-receipt.json'
previous=load(previous_path)
previous_feature=load(previous_release/'feature-verification.json')
previous_archive=load(previous_release/'archive-receipt.json')
reused_stage08={'featureVerification':str(previous_release/'feature-verification.json'),'scenarios':9,'actualSwingStarts':18,'observedIntervals':12,'relatedBasicVerifiedMethods':10,'unrelatedMethodsPreserved':9,'terminalAudit':str(WORK/'reports/stage08-terminal-audit-20261112.json'),'archiveReceipt':str(previous_release/'archive-receipt.json'),'archiveFiles':127,'archiveSha256':previous_archive['archiveSha256'],'acceptanceAndArchiveVerificationRerun':False}
assert previous_feature['scenariosPerRuntime']==9 and previous_archive['files']==127
baseline_path=WORK/'reports/blood-brand-baseline-70bb399c.json'
candidate_path=Path(__import__('sys').argv[1]).resolve()
baseline,candidate=load(baseline_path),load(candidate_path)
for result in [baseline,candidate]:
    terminal=result['terminal']
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and result['probe']['completedScenarios']==7 and result['probe']['nativeServerTickCallbacks']==1120
    assert not result['probe']['authenticatedRewardsAccepted'] and not result['probe']['authorityWrittenByReflection']
    for name,wanted in result['inputHashes'].items():
        assert digest(name)==wanted,name
assert baseline['baselineGapObserved'] and not baseline['probe']['nativeBloodDamageEvents']
assert candidate['bloodBrandFeatureAccepted'] and candidate['probe']['bloodBrandFeatureAccepted'] and not candidate['probe']['otherActiveSkillsAccepted']
rows=candidate['probe']['rows']
positive=rows[0]
assert len(positive['castStarts'])==2 and len(positive['castEnds'])==2
assert positive['castStarts'][0]-positive['firstAiTick']==80 and positive['castStarts'][1]-positive['castStarts'][0]==240
assert all(end-start==40 for start,end in zip(positive['castStarts'],positive['castEnds']))
assert positive['npcHealth']==[968.0,936.0,1000.0,1000.0]
hits=candidate['probe']['nativeBloodDamageEvents']
assert len(hits)==3 and all(hit['scenario']==0 and hit['actualDamage']==32.0 and hit['actualSourceIsButcher'] for hit in hits)
for hit in hits:
    start=positive['castStarts'][0] if hit['gameTime']<positive['castStarts'][1] else positive['castStarts'][1]
    assert hit['gameTime']-start==28
for index in [1,2,3,4,6]:
    assert not rows[index]['castStarts']
assert len(rows[5]['castStarts'])==len(rows[5]['castEnds'])==1 and rows[5]['castEnds'][0]-rows[5]['castStarts'][0]==10
for label in ['NoPlayerOrAuthenticationFabricated','OriginalFirstCastDelay80','OriginalPerSkillCooldown240','OriginalReleaseOnWindupTick28','MovedTargetEscapesLockedCenterWhileNearbyNpcStillHit','OriginalRadiusThreeAndVerticalTwoPointFiveExemptions','LostNativeTargetCancelsPendingSkill','CanceledWindupDealsNoBloodDamage','PausedWeaponImplementationAbsent']:
    assert label in candidate['probe']['cases'],label
built=load(BUILD/'candidate-build-receipt.json')
context=load(BUILD/'compile-context.json')
for name,wanted in context['inputHashes'].items():
    assert digest(name)==wanted,name
proof=(BUILD/'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;methodsBasicVerified=57;unrelatedCombatMethodsPreserved=9;' in proof and 'originalPureClassesByteIdentical=5' in proof and 'onlyBloodBrandEnabled=true' in proof and 'normalizedUpdateInstructionsIdentical=true' in proof and 'stage08PhaseSchedulerPreserved=true' in proof
jar=Path(built['jar'])
assert digest(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]
assert built['classCount']==1443 and len(built['addedEntries'])==9 and len(built['changedEntries'])==3
assert built['changedEntries']==['assets/ftboss/lang/en_us.json','assets/ftboss/lang/zh_cn.json','net/farmingtales/ftboss/boss/ai/ButcherCombat.class']
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as changed,zipfile.ZipFile(ROOT/'reference/forge-current.jar') as original:
    assert not set(base.namelist())-set(changed.namelist())
    assert sorted(set(changed.namelist())-set(base.namelist()))==built['addedEntries']
    assert sorted(name for name in base.namelist() if base.read(name)!=changed.read(name))==built['changedEntries']
    damage_name='data/ftboss/damage_type/butcher_blood.json'
    assert changed.read(damage_name)==original.read(damage_name)
    for name in ['ButcherSkillRules','ButcherSkillRules$Skill','ButcherSkillRules$ParticleBudget','ButcherFxPlan','ButcherFxPlan$Point']:
        full='net/farmingtales/ftboss/butcherskills/'+name+'.class'
        assert changed.read(full)==original.read(full)
    for name,delta in built['languageDelta'].items():
        before_values=json.loads(base.read(name))
        assert json.loads(changed.read(name))==dict(before_values,**delta)
    assert 'net/farmingtales/ftboss/butcherskills/ButcherSkills.class' not in changed.namelist()
    assert 'net/farmingtales/ftboss/butcherskills/ButcherSkillMotion.class' not in changed.namelist()
    assert changed.testzip() is None
process_path=WORK/'reports/stage09-predeployment-process-audit.json'
save(process_path,audit_processes())
target=Path(previous['target']).resolve()
expected=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar').resolve()
assert target==expected and digest(target)==built['baseJarSha256']==previous['deployedSha256']
assert not list(target.parent.glob(target.name+'.stage*.tmp')) and not list(target.parent.glob('ftboss-weapons*.jar'))
forge_target=Path(load(ROOT/'baseline.json')['forge']['live'])
forge_before=digest(forge_target)
assert forge_before=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
release=WORK/'releases/boss-core-stage09-blood-brand-20261112'
release.mkdir()
backup=release/'rollback-stage08-qys-bosses-1.21.1.jar'
shutil.copy2(target,backup)
assert digest(backup)==previous['deployedSha256']
released=release/'ftboss-boss-core-1.21.1-stage09.jar'
shutil.copyfile(jar,released)
assert digest(released)==built['jarSha256']
verification={'feature':'Blood Brand server-side active skill only','reusedStage08Evidence':reused_stage08,'baselineReceipt':str(baseline_path),'candidateReceipt':str(candidate_path),'scenariosPerRuntime':7,'nativeServerTickCallbacksPerRuntime':1120,'baselineBloodBrandCasts':0,'baselineBloodDamageEvents':0,'candidateCompletedBloodBrandCasts':2,'candidateCanceledBloodBrandCasts':1,'actualNpcBloodDamageEvents':3,'nativeDamageAmountPerHit':32.0,'firstCastDelay':80,'windup':28,'recovery':12,'perSkillCooldown':240,'anchoredRadius':3.0,'verticalHalfRange':2.5,'phaseAiRangeLosAndLostTargetControlsAccepted':True,'relatedBasicVerifiedMethods':57,'unrelatedCombatMethodsPreserved':9,'pureOriginalRuleAndFxPlanClassesByteIdentical':5,'existingMeleeInstructionsAndStage08SchedulerPreserved':True,'addedEntries':built['addedEntries'],'changedEntries':built['changedEntries'],'unrelatedEntriesByteIdentical':True,'otherThreeSkillsAndFullRotationParityAccepted':False,'clientSkillMotionAdapterIncluded':False,'serverTelegraphParticlePathLinkedAndExecuted':True,'clientRenderingAccepted':False,'incrementalFeatureAccepted':True,'fullBossInstallable':False,'authenticatedMultiplayerAccepted':False,'authenticatedRewardsAccepted':False,'completeBattleAccepted':False,'stage03PositiveKillAccepted':False,'stage03CandidateIncluded':False,'weaponFunctionalDevelopmentPaused':True}
save(release/'feature-verification.json',verification)
restore=chr(10).join(['# Stage09: verified Blood Brand server-side increment','', 'Scope: only BLOOD_BRAND is enabled through a pre-melee consumed-action gate matching the frozen Forge ButcherCombat.update callsite. Native stage08 had no active entry or butcher_blood damage definition; its genuine 1120-tick baseline used registered NPC targets and produced no casts or blood damage. Candidate uses the same observer in a separate fresh NeoForge JVM: two completed casts, one canceled cast, three real NPC LivingDamageEvent.Post hits of 32 damage, plus phase/AI/range/LOS/cancellation controls. No player or authentication is fabricated.','', 'Original data: first cast delay 80 ticks, windup 28, recovery 12, per-skill cooldown 240 and global cooldown 80. Target position is captured at cast start; the released circle stays there with radius 3 and vertical half-range 2.5. A target moving away escapes the second release while a nearby NPC at the captured center is still hit. Lost target cancels before damage. Server particle plan, colors and world-tick budgets are retained through the mapped original FX helper. Five pure rule/enum/budget/point-plan classes and the blood damage JSON are byte-identical to frozen Forge. Only four original death-message translations are added to the existing language payloads. No unreleased stage03 language source is packaged.','', 'Production boundary: only an initial skill gate is added to the existing ButcherCombat.update method. Removing that gate restores every stage08 update instruction; the nine other combat methods remain unchanged, including stage08 phase cadence. Eight server/pure helper classes and one damage definition are added. No other active ability or summon skill is enabled. The scheduler is restricted to Blood Brand while the other three skills are deferred; full original four-skill rotation parity is NOT accepted.','', 'Stage08 nine-scenario/18-swing/12-interval/ASM/terminal/127-file archive evidence is referenced directly and not rerun. This is a narrow verified server increment, not a complete Butcher or Boss candidate. Client skill motion adaptation, client rendering, authenticated rewards and complete battle remain unaccepted. Stage03 materials/rewards are still undeployed pending authenticated positive kills and the known live omission remains. Weapons remain paused; their separate recovery JAR must not be installed.','', 'Rollback: stop the exact NeoForge instance, verify deployment-receipt.json target and deployedSha256, restore rollback-stage08-qys-bosses-1.21.1.jar to that target and verify backupSha256. Forge and historical D remain read-only. Deployment does not launch the formal instance.','', 'Rebuild: verify input hashes and use NEW build/runtime output directories. Run the archived build_boss_blood_brand_stage09.py, BloodBrandBoundaryProof and run_boss_blood_brand_probe_v1.py with the frozen SDK. Production combat bytes derive from the frozen stage08 class; source compiles separately to prove API linkage. Never overwrite frozen evidence. External SDK dependency records are inherited from the stage08 manifest without rerunning stage08 acceptance.','', 'Both test servers save all dimensions and exit normally. Scratch directories are archived and retained under the earlier deletion-policy rejection; no recursive deletion workaround is attempted.',''])
patch_text(release/'RESTORE.md',restore)
sdk_manifest=previous_release/'external-dependencies.json'
save(release/'external-dependencies.json',{'frozenStage08Manifest':str(sdk_manifest),'frozenManifestSha256':digest(sdk_manifest),'dependencyRecordsReused':load(sdk_manifest),'currentBaseJar':str(Path(built['baseJar'])),'currentBaseJarSha256':built['baseJarSha256'],'originalForgeReference':str(ROOT/'reference/forge-current.jar'),'originalForgeReferenceSha256':context['inputHashes'][str(ROOT/'reference/forge-current.jar')]})
temporary=target.with_name(target.name+'.stage09-'+uuid.uuid4().hex[:8]+'.tmp')
assert temporary.parent==expected.parent and not temporary.exists()
shutil.copyfile(released,temporary)
assert digest(temporary)==built['jarSha256']
os.replace(temporary,target)
assert digest(target)==built['jarSha256'] and digest(forge_target)==forge_before
deployment={'deployedAt':datetime.datetime.now().astimezone().isoformat(),'target':str(target),'deployedSha256':digest(target),'backup':str(backup),'backupSha256':digest(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'formalInstanceLaunched':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'authenticatedRewardsAccepted':False,'completeBattleAccepted':False,'otherThreeSkillsAndFullRotationAccepted':False,'clientMotionAdapterIncluded':False,'stage03CandidateIncluded':False,'stage03MaterialDropCorrectionStillPending':True,'weaponCandidateValidatedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'forgeSha256Unchanged':forge_before,'historicalDModified':False,'inputHashes':{str(path):digest(path) for path in [Path(__file__),baseline_path,candidate_path,BUILD/'candidate-build-receipt.json',BUILD/'compile-context.json',process_path,release/'feature-verification.json']}}
save(release/'deployment-receipt.json',deployment)
save(release/'release-file-manifest.json',{path.name:{'bytes':path.stat().st_size,'sha256':digest(path)} for path in release.iterdir() if path.is_file()})
archive_path=WORK/'archives/boss-core-stage09-blood-brand-20261112.zip'
files=list(release.rglob('*'))+list(BUILD.rglob('*'))+list((WORK/'build/boss-butcher-active-skill-audit-20261112').rglob('*'))
files += [baseline_path,candidate_path,process_path,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.claim',WORK/'tests/BloodBrandBoundaryProof.java',WORK/'tests/blood-brand-v1/BossBloodBrandRuntimeProbe.java',WORK/'boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/ButcherCombat.java',WORK/'resource-patches/stage09-blood-brand-language-delta.json',WORK/'boss-core/src/main/resources/data/ftboss/damage_type/butcher_blood.json']
files += list((WORK/'boss-core/src/main/java/net/farmingtales/ftboss/butcherskills').rglob('*.java'))
files += [WORK/'tools'/name for name in ['build_boss_blood_brand_stage09.py','run_boss_blood_brand_probe_v1.py',Path(__file__).name]]
files += [previous_release/name for name in ['feature-verification.json','deployment-receipt.json','archive-receipt.json','closeout-archive-receipt.json','external-dependencies.json','RESTORE.md']]
files += [WORK/'reports/stage08-terminal-audit-20261112.json',WORK/'build/boss-butcher-phase-callsite-audit-20261112/original-sources/net/farmingtales/ftboss/boss/ai/ButcherCombat.java']
for result in [baseline,candidate]:
    runtime=Path(result['attempt'])
    for name in ['runtime.log','terminal.json','runtime-command.json','inputs-before.json','compile-command.json','compile-terminal.json','compile.log','live-process.json','server/actual-blood-brand-probe.json']:files.append(runtime/name)
    for name in ['observer-classes','observer-resources','isolated-home','isolated-tmp','server/logs','server/crash-reports','server/world']:files+=list((runtime/name).rglob('*'))
archive=archive_verified(archive_path,files)
archive.update({'deployedIncrementSha256':digest(target),'fullBossInstallable':False,'scratchArchivedAndRetained':True,'scratchDeletionNotAttemptedBecausePriorPolicyRejection':True,'stage08EvidenceReusedNotRerun':True})
save(release/'archive-receipt.json',archive)
terminal=audit_processes()
terminal.update({'stage':'stage09','deployedSha256':digest(target),'backupSha256':digest(backup),'archiveSha256':archive['archiveSha256'],'deploymentTemporaryFiles':[path.name for path in target.parent.glob(target.name+'.stage*.tmp')],'forgeUnchanged':digest(forge_target)==forge_before,'weaponFunctionalDevelopmentPaused':True,'stage03PositiveKillAccepted':False,'authenticatedRewardsAccepted':False,'otherThreeSkillsAndFullRotationAccepted':False,'clientRenderingAccepted':False,'completeBattleAccepted':False,'fullBossInstallable':False,'reusedStage08Evidence':reused_stage08})
assert not terminal['deploymentTemporaryFiles']
terminal_path=WORK/'reports/stage09-terminal-audit-20261112.json'
save(terminal_path,terminal)
closeout_path=WORK/'archives/boss-core-stage09-closeout-20261112.zip'
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path)
    state.update({'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'bossActiveAttempt':None,'activeWorkstream':'boss-stage09-closed-next-single-original-active-skill-baseline','latestBossIncrementStatus':'stage09 Blood Brand server skill verified/deployed/archived','latestDeploymentReceipt':str(release/'deployment-receipt.json'),'latestDeployedSha256':digest(target),'latestBossArchive':str(archive_path),'latestBossArchiveSha256':archive['archiveSha256'],'latestStageCloseoutArchive':str(closeout_path),'latestStageCloseoutArchiveReceipt':str(release/'closeout-archive-receipt.json'),'stage09TerminalAudit':str(terminal_path),'nextBossAction':'Compare one of BLOOD_TIDE, CLEAVER_SWEEP or BONE_CRUSH against the frozen Forge callsite and genuine current native behavior before implementation. Full four-skill rotation and client motion remain deferred. Weapons stay paused.','fullBossInstallable':False,'weaponFunctionalDevelopmentPaused':True,'ownedJavaPythonRemaining':0})
    if path.name=='CURRENT_BOSS_BATCH.json':state.update({'status':'stage09_blood_brand_completed_deployed_archived_weapons_paused','currentStage':'stage09 Blood Brand server skill completed/deployed/archived; three other skills and full rotation remain pending'})
    patch_text(path,json.dumps(state,ensure_ascii=False,indent=2)+chr(10))
checkpoint=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md'
text=chr(10).join(['# Boss remaining checkpoint after stage09','', 'Stage09 restores only Blood Brand server-side casting and release. Frozen Forge calls its skill controller before melee; current native baseline produced zero Blood Brand casts or blood damage over 1120 genuine server ticks. Candidate passes seven directed scenarios using ordinary registered NPCs: two completed casts, one canceled cast and three native blood-damage hits of 32. Timing is 80 first-delay, 28 windup, 12 recovery and 240 per-skill cooldown; anchored circle radius 3 and vertical half-range 2.5 preserve original targeting. Phase, AI, range, line-of-sight and lost-target guards pass. No player is created or authenticated.','', 'Stage08 nine scenarios, 18 actual swings, 12 intervals, ASM, terminal audit and 127-file verified archive are referenced and not rerun. The new pre-melee gate leaves the complete old update instruction sequence unchanged when removed and preserves all nine other combat methods. Five pure original rule/FX-plan classes and damage JSON are byte-identical; only the new damage definition and four death-message translations accompany the server helpers. Fifty-seven related methods pass ASM.','', 'Formal NeoForge Boss JAR is now stage09; rollback is stage08. New independent release archive, per-file manifest, SHA256, restore instructions and terminal audit are recorded in the stage09 release. Both runtimes save all dimensions and exit normally. Unrelated user game/Python processes are preserved; scratch is archived and retained under the prior recursive-deletion rejection without a workaround.','', 'Next: choose exactly one of BLOOD_TIDE, CLEAVER_SWEEP or BONE_CRUSH after comparing frozen Forge callsites and genuine native behavior. Only Blood Brand is enabled now, with its original cooldown guards. Full original four-skill rotation parity, summon effects, phase chase/hit balance and client skill-motion adapter remain pending. Do not infer acceptance of these from Blood Brand success or from pure enum metadata for other skills.','', 'Stage03 ten basic material registrations and reward correction remain archived but undeployed pending authenticated positive player kills. The known live material-reward omission remains. Never substitute NPC/scratch targets or direct reward calls for authenticated kill integration.','', 'Weapon development and weapon goal remain paused, and its separate recovery JAR is noninstallable. Client rendering/GL, authenticated multiplayer/rewards and complete battle remain unaccepted; fullBossInstallable remains false. Earlier remaining-behavior audit is reports/boss-remaining-behavior-audit-20261112.json.',''])
patch_text(checkpoint,text)
closeout=archive_verified(closeout_path,[ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',checkpoint,terminal_path,release/'archive-receipt.json',release/'deployment-receipt.json',release/'RESTORE.md',release/'release-file-manifest.json'])
save(release/'closeout-archive-receipt.json',closeout)
assert digest(target)==built['jarSha256'] and digest(backup)==built['baseJarSha256']
print(json.dumps({'deploymentReceipt':str(release/'deployment-receipt.json'),'deployedSha256':digest(target),'archive':archive,'closeout':closeout,'directedScenarios':7,'nativeServerTicksPerRuntime':1120,'completedCasts':2,'canceledCasts':1,'nativeNpcBloodDamageEvents':3,'ownedJavaPythonRemaining':0,'fullBossInstallable':False},ensure_ascii=False),flush=True)
