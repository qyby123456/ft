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
BUILD = WORK / 'build/boss-butcher-cadence-v1'

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
previous_release=WORK/'releases/boss-core-stage07-butcher-phase-20261112'
previous_path=previous_release/'deployment-receipt.json'
previous=load(previous_path)
reused_stage07={'featureVerification':str(previous_release/'feature-verification.json'),'directedChecks':53,'terminalAudit':str(WORK/'reports/stage07-terminal-audit-20261112.json'),'archiveReceipt':str(previous_release/'archive-receipt.json'),'archiveFiles':164,'acceptanceAndArchiveVerificationRerun':False}
assert load(previous_release/'feature-verification.json')['candidateCases']==53 and load(previous_release/'archive-receipt.json')['files']==164
baseline_path=WORK/'reports/butcher-cadence-baseline-48ec8c31.json'
candidate_path=Path(__import__('sys').argv[1]).resolve()
baseline,candidate=load(baseline_path),load(candidate_path)
for result in [baseline,candidate]:
    terminal=result['terminal']
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and result['probe']['completedScenarios']==9
    assert not result['authenticatedMultiplayerAccepted'] and not result['probe']['authorityWrittenByReflection']
    rows=result['probe']['rows']
    assert len(rows)==9
    assert sum(len(row['nativeActionStartGameTimes']) for row in rows)==18
    assert sum(len(row['actualStartIntervals']) for row in rows)==12
    for row in rows[6:]:
        assert not row['nativeActionStartGameTimes']
    for name,wanted in result['inputHashes'].items():
        assert digest(name)==wanted,name
assert baseline['baselineGapObserved'] and candidate['attackCadenceFeatureAccepted'] and candidate['probe']['attackCadenceFeatureAccepted']
for index,row in enumerate(baseline['probe']['rows'][:6]):
    expected=6 if index%2==0 else 4
    assert row['scheduledNextAttackDeltas']==[expected]*3 and row['actualStartIntervals']==[expected]*2
for row in candidate['probe']['rows'][:6]:
    expected={1:20,2:10,3:14}[row['phase']]
    assert row['scheduledNextAttackDeltas']==[expected]*3 and row['actualStartIntervals']==[expected]*2
for label in ['NoTargetCannotAttack','OutOfReachCannotAttack','DisabledAiCannotAttack','ScratchTargetIsNotAuthenticatedPlayer','PausedWeaponImplementationAbsent']:
    assert label in candidate['probe']['cases']
built=load(BUILD/'candidate-build-receipt.json')
context=load(BUILD/'compile-context.json')
for name,wanted in context['inputHashes'].items():
    assert digest(name)==wanted,name
proof=(BUILD/'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;methodsBasicVerified=10;unrelatedMethodsPreserved=9;' in proof and 'singleSchedulerReplacementOnly=true' in proof and 'normalizedUpdateInstructionsIdentical=true' in proof
jar=Path(built['jar'])
assert digest(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]
expected_changed=['net/farmingtales/ftboss/boss/ai/ButcherCombat.class']
assert built['classCount']==1435 and built['changedClasses']==expected_changed and not built['addedClasses']
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as changed:
    assert set(base.namelist())==set(changed.namelist())
    assert sorted(name for name in base.namelist() if base.read(name)!=changed.read(name))==expected_changed
    assert changed.testzip() is None
process_path=WORK/'reports/stage08-predeployment-process-audit.json'
save(process_path,audit_processes())
target=Path(previous['target']).resolve()
expected=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar').resolve()
assert target==expected and digest(target)==built['baseJarSha256']==previous['deployedSha256']
assert not list(target.parent.glob(target.name+'.stage*.tmp')) and not list(target.parent.glob('ftboss-weapons*.jar'))
forge_target=Path(load(ROOT/'baseline.json')['forge']['live'])
forge_before=digest(forge_target)
assert forge_before=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
release=WORK/'releases/boss-core-stage08-melee-cadence-20261112'
release.mkdir()
backup=release/'rollback-stage07-qys-bosses-1.21.1.jar'
shutil.copy2(target,backup)
assert digest(backup)==previous['deployedSha256']
released=release/'ftboss-boss-core-1.21.1-stage08.jar'
shutil.copyfile(jar,released)
assert digest(released)==built['jarSha256']
verification={'feature':'Butcher melee attack cadence follows original phase instead of legacy health boolean','reusedStage07Evidence':reused_stage07,'baselineReceipt':str(baseline_path),'candidateReceipt':str(candidate_path),'scenariosPerRuntime':9,'phaseHealthPositiveControls':6,'negativeControls':['no target','out of reach','AI disabled'],'actualSwingStartsPerRuntime':18,'observedSwingIntervalsPerRuntime':12,'baselineIntervalsByHealth':[6,4],'restoredIntervalsByPhase':[20,10,14],'serverTicksObserved':candidate['probe']['nativeServerTickCallbacks'],'relatedBasicVerifiedMethods':10,'unrelatedMethodsPreserved':9,'singleSchedulerReplacementOnly':True,'normalizedUpdateInstructionsIdentical':True,'changedClasses':expected_changed,'allOtherClassAndResourceBytesPreserved':True,'chaseDamageLifestealDebuffsAndActiveSkillsUnchanged':True,'incrementalFeatureAccepted':True,'fullBossInstallable':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False,'stage03PositiveKillAccepted':False,'stage03CandidateIncluded':False,'weaponFunctionalDevelopmentPaused':True}
save(release/'feature-verification.json',verification)
restore=chr(10).join(['# Stage08: verified Butcher melee cadence increment','', 'The frozen Forge ButcherCombat.update callsite uses ButcherPhaseRules.interval(phase): 20, 10 and 14 ticks. Stage07 still called ButcherCombatRules.interval(healthBelowHalf): 6 or 4 ticks. A genuine native ServerTickEvent.Post baseline observed 18 actual chop/backhand starts and 12 gaps across all three phases at full and 40% health. The candidate repeats those nine focused scenarios and restores phase cadence independent of the health control. Missing target, out-of-reach target and disabled AI never start an attack.','', 'Production change: only one scheduler expression in ButcherCombat.update. The production class is derived directly from stage07 bytecode. The boolean-load/legacy-call pair becomes owner.getButcherPhase()/phase interval. Normalizing that exact replacement restores every original native update instruction; all nine other methods and every other JAR entry are preserved. The separately compiled Java source proves native API linkage. Chase speed, pitch, damage, lifesteal, debuffs and active ButcherSkills are intentionally unchanged and remain future comparison work.','', 'Reused stage07 evidence: 53 directed checks, terminal audit and the 164-file verified archive. These were referenced rather than rerun. This batch tests cadence only. Targets are explicitly unauthenticated scratch ServerPlayer entities; reflection only reads combat fields. It does not accept authenticated rewards, a full encounter, client rendering or complete battle. Stage03 material rewards remain undeployed and pending authenticated positive kills; the known live omission remains. Weapons remain paused and their separate recovery JAR must not be installed.','', 'Rollback: stop the exact NeoForge instance, verify deployment-receipt.json target and deployedSha256, copy rollback-stage07-qys-bosses-1.21.1.jar to that target and verify backupSha256. Forge and historical D are read-only. The formal instance was not launched by deployment.','', 'Rebuild: verify recorded input hashes, use a NEW build/runtime output directory, then run the archived build_boss_butcher_cadence_stage08.py, ButcherCadenceBoundaryProof and run_boss_butcher_cadence_probe_v1.py with the frozen SDK. Both baseline and candidate use the same observer source in separate genuine NeoForge JVMs. Do not overwrite frozen evidence.','', 'Both test servers saved all dimensions and exited normally. Scratch evidence is archived and retained under the earlier deletion-policy rejection; no recursive deletion workaround is attempted. External SDK hashes are referenced from the frozen stage07 dependency manifest, not a new stage07 acceptance run.',''])
patch_text(release/'RESTORE.md',restore)
sdk_manifest=previous_release/'external-dependencies.json'
save(release/'external-dependencies.json',{'frozenStage07Manifest':str(sdk_manifest),'frozenManifestSha256':digest(sdk_manifest),'sdkEntriesReusedWithoutRerunningStage07':load(sdk_manifest),'currentBaseJar':str(Path(built['baseJar'])),'currentBaseJarSha256':built['baseJarSha256'],'originalForgeReference':str(ROOT/'reference/forge-current.jar'),'originalForgeReferenceSha256':context['inputHashes'][str(ROOT/'reference/forge-current.jar')]})
temporary=target.with_name(target.name+'.stage08-'+uuid.uuid4().hex[:8]+'.tmp')
assert temporary.parent==expected.parent and not temporary.exists()
shutil.copyfile(released,temporary)
assert digest(temporary)==built['jarSha256']
os.replace(temporary,target)
assert digest(target)==built['jarSha256'] and digest(forge_target)==forge_before
deployment={'deployedAt':datetime.datetime.now().astimezone().isoformat(),'target':str(target),'deployedSha256':digest(target),'backup':str(backup),'backupSha256':digest(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'formalInstanceLaunched':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False,'activeSkillsAndHitDamageAccepted':False,'stage03CandidateIncluded':False,'stage03MaterialDropCorrectionStillPending':True,'weaponCandidateValidatedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'forgeSha256Unchanged':forge_before,'historicalDModified':False,'inputHashes':{str(path):digest(path) for path in [Path(__file__),baseline_path,candidate_path,BUILD/'candidate-build-receipt.json',BUILD/'compile-context.json',process_path,release/'feature-verification.json']}}
save(release/'deployment-receipt.json',deployment)
save(release/'release-file-manifest.json',{path.name:{'bytes':path.stat().st_size,'sha256':digest(path)} for path in release.iterdir() if path.is_file()})
archive_path=WORK/'archives/boss-core-stage08-melee-cadence-20261112.zip'
files=list(release.rglob('*'))+list(BUILD.rglob('*'))+list((WORK/'build/boss-butcher-cadence-audit-20261112').rglob('*'))
files += [baseline_path,candidate_path,process_path,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.claim',WORK/'tests/ButcherCadenceBoundaryProof.java',WORK/'tests/butcher-cadence-v1/BossButcherCadenceRuntimeProbe.java',WORK/'boss-core/src/main/java/net/farmingtales/ftboss/boss/ai/ButcherCombat.java']
files += [WORK/'tools'/name for name in ['build_boss_butcher_cadence_stage08.py','run_boss_butcher_cadence_probe_v1.py',Path(__file__).name]]
files += [previous_release/name for name in ['feature-verification.json','deployment-receipt.json','archive-receipt.json','closeout-archive-receipt.json','external-dependencies.json','RESTORE.md']]
files += [WORK/'reports/stage07-terminal-audit-20261112.json',WORK/'build/boss-butcher-phase-callsite-audit-20261112/original-sources/net/farmingtales/ftboss/boss/ai/ButcherCombat.java',WORK/'build/boss-remaining-behavior-audit-20261112/original-sources/net/farmingtales/ftboss/boss/ai/ButcherPhaseRules.java']
for result in [baseline,candidate]:
    runtime=Path(result['attempt'])
    for name in ['runtime.log','terminal.json','runtime-command.json','inputs-before.json','compile-command.json','compile-terminal.json','compile.log','live-process.json','server/actual-butcher-cadence-probe.json']:files.append(runtime/name)
    for name in ['observer-classes','observer-resources','isolated-home','isolated-tmp','server/logs','server/crash-reports','server/world']:files+=list((runtime/name).rglob('*'))
archive=archive_verified(archive_path,files)
archive.update({'deployedIncrementSha256':digest(target),'fullBossInstallable':False,'scratchArchivedAndRetained':True,'scratchDeletionNotAttemptedBecausePriorPolicyRejection':True,'stage07EvidenceReusedNotRerun':True})
save(release/'archive-receipt.json',archive)
terminal=audit_processes()
terminal.update({'stage':'stage08','deployedSha256':digest(target),'backupSha256':digest(backup),'archiveSha256':archive['archiveSha256'],'deploymentTemporaryFiles':[path.name for path in target.parent.glob(target.name+'.stage*.tmp')],'forgeUnchanged':digest(forge_target)==forge_before,'weaponFunctionalDevelopmentPaused':True,'stage03PositiveKillAccepted':False,'activeSkillsAndHitDamageAccepted':False,'clientRenderingAccepted':False,'completeBattleAccepted':False,'fullBossInstallable':False,'reusedStage07Evidence':reused_stage07})
assert not terminal['deploymentTemporaryFiles']
terminal_path=WORK/'reports/stage08-terminal-audit-20261112.json'
save(terminal_path,terminal)
closeout_path=WORK/'archives/boss-core-stage08-closeout-20261112.zip'
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path)
    state.update({'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'bossActiveAttempt':None,'activeWorkstream':'boss-stage08-closed-next-original-butcher-hit-or-skill-baseline','latestBossIncrementStatus':'stage08 single melee scheduler verified/deployed/archived','latestDeploymentReceipt':str(release/'deployment-receipt.json'),'latestDeployedSha256':digest(target),'latestBossArchive':str(archive_path),'latestBossArchiveSha256':archive['archiveSha256'],'latestStageCloseoutArchive':str(closeout_path),'latestStageCloseoutArchiveReceipt':str(release/'closeout-archive-receipt.json'),'stage08TerminalAudit':str(terminal_path),'nextBossAction':'Compare original Butcher hit damage/lifesteal/debuff or active skill callsites with a genuine native baseline, then select only one proved gap. Phase-dependent chase remains unaccepted. Weapons stay paused.','fullBossInstallable':False,'weaponFunctionalDevelopmentPaused':True,'ownedJavaPythonRemaining':0})
    if path.name=='CURRENT_BOSS_BATCH.json':state.update({'status':'stage08_completed_deployed_archived_weapons_paused_next_butcher_hit_skill_audit','currentStage':'stage08 melee cadence completed/deployed/archived; hit and active skills remain next batch'})
    patch_text(path,json.dumps(state,ensure_ascii=False,indent=2)+chr(10))
checkpoint=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md'
text=chr(10).join(['# Boss remaining checkpoint after stage08','', 'Stage08 fixes one proved melee-scheduler gap: frozen Forge uses phase intervals 20/10/14, while stage07 genuinely started swings every 6/4 ticks according to health. Both fresh native baseline/candidate runs observe 18 swing starts and 12 timing gaps across six phase/health scenarios, plus no-target/out-of-range/disabled-AI controls. Candidate timing is phase-correct at both health values. Only the scheduler read/call in ButcherCombat.update changes; the rest of update normalizes to exactly its previous instructions. Ten related methods pass ASM; the other nine methods and all other JAR entries remain unchanged.','', 'Stage07 lifecycle acceptance (53 checks), terminal audit and 164-file verified archive are reused without rerunning. Current formal NeoForge Boss JAR is stage08; its rollback is stage07. New archive, manifest, SHA256, restore instructions and terminal audit are in the stage08 release.','', 'Next batch: compare frozen Forge hit damage/lifesteal/debuff or active ButcherSkills callsites to actual current native behavior; select one proved gap and make the smallest change. Cadence success does not accept phase chase speed, hit balance, debuffs, active skills or complete Butcher combat. Missing class names alone are not proof of missing behavior.','', 'Stage03 ten basic material registrations and reward correction remain archived but undeployed pending an authenticated positive player kill. The known live material-reward omission is still present. Never substitute scratch entities or direct reward calls for authenticated kill integration.','', 'Weapons remain paused and noninstallable. Both test servers save all dimensions and exit normally; unrelated user Java/Python processes are preserved. Scratch directories are archived and retained because of the earlier recursive-deletion rejection, without a workaround.','', 'Client rendering/GL, authenticated multiplayer and complete battle remain unaccepted; fullBossInstallable stays false. The earlier remaining behavior audit is reports/boss-remaining-behavior-audit-20261112.json. Do not double-apply native behavior merely because original helper class names differ.',''])
patch_text(checkpoint,text)
closeout=archive_verified(closeout_path,[ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',checkpoint,terminal_path,release/'archive-receipt.json',release/'deployment-receipt.json',release/'RESTORE.md',release/'release-file-manifest.json'])
save(release/'closeout-archive-receipt.json',closeout)
assert digest(target)==built['jarSha256'] and digest(backup)==built['baseJarSha256']
print(json.dumps({'deploymentReceipt':str(release/'deployment-receipt.json'),'deployedSha256':digest(target),'archive':archive,'closeout':closeout,'scenariosPerRuntime':9,'swingStartsPerRuntime':18,'intervalsPerRuntime':12,'ownedJavaPythonRemaining':0,'fullBossInstallable':False},ensure_ascii=False),flush=True)
