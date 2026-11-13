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
BUILD = WORK / 'build/boss-butcher-phase-lifecycle-v2'

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

owner = '01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert load(ROOT/'WRITE_OWNER.claim')['threadId']==owner and load(ROOT/'WRITE_OWNER.json')['activeWriterThreadId']==owner
baseline_path = WORK/'reports/butcher-phase-baseline-7c62ac2e.json'
candidate_path = Path(__import__('sys').argv[1]).resolve()
baseline,candidate = load(baseline_path),load(candidate_path)
for result in [baseline,candidate]:
    terminal = result['terminal']
    assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and not result['authenticatedMultiplayerAccepted']
    for name,wanted in result['inputHashes'].items():
        assert digest(name)==wanted,name
assert baseline['baselineGapObserved'] and candidate['phaseLifecycleFeatureAccepted'] and candidate['probe']['phaseLifecycleFeatureAccepted']
assert candidate['probe']['naturalDefeatEvents']==3 and candidate['probe']['naturalCanceledDefeatEvents']==2 and candidate['probe']['completeRecoveryWindowEntityTicks']==1200
for label in ['NativeBypassesInvulnerabilityDamageNeverAdvancesPhase','OriginalSuppressedLemonHelperCannotResurrect','ExistingNativeScalingPreservesModifiedMaxHealthAndRatio','TimeoutPreservesIndependentHealthModifier','NativeSaveLoadPreservesPhaseAndElapsed','OrdinaryVanillaDeathUnaffected','PausedWeaponImplementationAbsent']:
    assert label in candidate['probe']['cases'],label
built = load(BUILD/'candidate-build-receipt.json')
context = load(BUILD/'compile-context.json')
for name,wanted in context['inputHashes'].items():
    assert digest(name)==wanted,name
proof = (BUILD/'proof-run.log').read_text(encoding='utf-8')
assert 'failures=0;methodsBasicVerified=84;unrelatedMethodsPreserved=52;' in proof
jar = Path(built['jar'])
assert digest(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]
assert built['classCount']==1435 and len(built['addedClasses'])==2 and len(built['changedClasses'])==3
with zipfile.ZipFile(built['baseJar']) as base, zipfile.ZipFile(jar) as changed, zipfile.ZipFile(ROOT/'reference/forge-current.jar') as original:
    allowed = set(built['addedClasses']+built['changedClasses'])
    assert not set(base.namelist())-set(changed.namelist())
    assert all(base.read(name)==changed.read(name) for name in base.namelist() if name not in allowed)
    rules = 'net/farmingtales/ftboss/boss/ai/ButcherPhaseRules.class'
    assert changed.read(rules)==original.read(rules)
    assert changed.testzip() is None
process_path = WORK/'reports/stage07-predeployment-process-audit.json'
process_audit = audit_processes()
save(process_path,process_audit)
previous_path = WORK/'releases/boss-core-stage06-flight-guard-20261112/deployment-receipt.json'
previous = load(previous_path)
target = Path(previous['target']).resolve()
expected = Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar').resolve()
assert target==expected and digest(target)==built['baseJarSha256']==previous['deployedSha256']
assert not list(target.parent.glob(target.name+'.stage*.tmp'))
assert not list(target.parent.glob('ftboss-weapons*.jar'))
forge_target = Path(load(ROOT/'baseline.json')['forge']['live'])
forge_before = digest(forge_target)
assert forge_before=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
release = WORK/'releases/boss-core-stage07-butcher-phase-20261112'
release.mkdir()
backup = release/'rollback-stage06-qys-bosses-1.21.1.jar'
shutil.copy2(target,backup)
assert digest(backup)==previous['deployedSha256']
released = release/'ftboss-boss-core-1.21.1-stage07.jar'
shutil.copyfile(jar,released)
assert digest(released)==built['jarSha256']
verification = {'feature':'Original Butcher three-phase lifecycle only','baselineReceipt':str(baseline_path),'baselineCases':len(baseline['probe']['cases']),'candidateReceipt':str(candidate_path),'candidateCases':len(candidate['probe']['cases']),'relatedBasicVerifiedMethods':84,'unrelatedMethodsPreserved':52,'addedClasses':built['addedClasses'],'changedClasses':built['changedClasses'],'originalPhaseRulesByteIdentical':True,'realNativeHurtDeathCancelTransitions':True,'nativeTickRecoveryWindow':1200,'nativeScalingUnchangedAndCompatible':True,'combatAttackProfilesAndButcherSkillsDeferred':True,'incrementalFeatureAccepted':True,'fullBossInstallable':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False,'stage03PositiveKillAccepted':False,'stage03CandidateIncluded':False,'weaponFunctionalDevelopmentPaused':True}
save(release/'feature-verification.json',verification)
restore = chr(10).join(['# Stage07: verified Butcher phase lifecycle increment','', 'Scope: real native entity tick and hurt progress phase 1 to 2 to 3, cancel only the first two ordinary deaths, preserve bypass and Lemon suppression, recover 5% each second in phase 3 and return to phase 1 after 1200 ticks. Phase health uses original total multiplier with the 1.21 ResourceLocation modifier ID ftboss:butcher_phase_health. The original pure rule class is byte-identical; particles and sounds use pinned SRG-to-official mappings. Only whitelisted lifecycle methods are transplanted. Existing native health scaling already handles modified maximum correctly and remains unchanged.','', 'Combat update/hit, attack skill profiles and ButcherSkills are deliberately unchanged and remain next-batch comparison work. This is not full Butcher or full Boss acceptance. Scratch entities are not authenticated players and are not a natural complete boss encounter. Client rendering and full battle remain unaccepted. Stage03 rewards still need authenticated positive kills and are not included. Weapons remain paused; the separated recovery JAR is NOT INSTALLABLE.','', 'Rollback: stop the target NeoForge instance, verify deployment-receipt.json target and hash, restore rollback-stage06-qys-bosses-1.21.1.jar to that exact target and verify backupSha256. Never write Forge or historical D.','', 'Rebuild: check all input hashes and select NEW build/runtime output directories. Use build_boss_butcher_phase_stage07_v2.py, ButcherPhaseBoundaryProof and run_boss_butcher_phase_probe_v3.py from this archive with the recorded frozen SDK. Do not overwrite failed or accepted evidence.','', 'The v1 observer baseline and v2 candidate observer are distinct frozen tests. Candidate covers additional lifecycle controls; it does not repeat stage06 acceptance. Initial runner context-path failure, source-preparation errors and first compile failure are preserved. JVMs save all dimensions and exit normally. Scratch is archived and retained under the prior deletion rejection; no removal workaround is attempted.',''])
(release/'RESTORE.md').write_text(restore,encoding='utf-8')
external = {}
for name in context['classpath'].split(';')+[str(ROOT/'reference/forge-current.jar')]:
    path=Path(name)
    if path.is_file():external[str(path)]={'bytes':path.stat().st_size,'sha256':digest(path)}
save(release/'external-dependencies.json',external)
temporary = target.with_name(target.name+'.stage07-'+uuid.uuid4().hex[:8]+'.tmp')
assert temporary.parent==expected.parent and not temporary.exists()
shutil.copyfile(released,temporary)
assert digest(temporary)==built['jarSha256']
os.replace(temporary,target)
assert digest(target)==built['jarSha256'] and digest(forge_target)==forge_before
deployment={'deployedAt':datetime.datetime.now().astimezone().isoformat(),'target':str(target),'deployedSha256':digest(target),'backup':str(backup),'backupSha256':digest(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'formalInstanceLaunched':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False,'combatAttackProfilesAccepted':False,'stage03CandidateIncluded':False,'stage03MaterialDropCorrectionStillPending':True,'weaponCandidateValidatedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'forgeSha256Unchanged':forge_before,'historicalDModified':False,'inputHashes':{str(path):digest(path) for path in [Path(__file__),baseline_path,candidate_path,BUILD/'candidate-build-receipt.json',BUILD/'compile-context.json',process_path,release/'feature-verification.json']}}
save(release/'deployment-receipt.json',deployment)
archive_path=WORK/'archives/boss-core-stage07-butcher-phase-20261112.zip'
files=list(release.rglob('*'))+list(BUILD.rglob('*'))+list((WORK/'build/boss-butcher-phase-lifecycle-v1').rglob('*'))+list((WORK/'build/boss-butcher-phase-callsite-audit-20261112').rglob('*'))
files += [baseline_path,candidate_path,process_path,WORK/'reports/stage07-baseline-runner-v1-failure.json',ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.claim']
for name in ['ButcherPhaseBoundaryProof.java','butcher-phase-v1/BossButcherPhaseRuntimeProbe.java','butcher-phase-v2/BossButcherPhaseRuntimeProbe.java']:files.append(WORK/'tests'/name)
for name in ['build_boss_butcher_phase_stage07.py','build_boss_butcher_phase_stage07_v2.py','run_boss_butcher_phase_probe_v1.py','run_boss_butcher_phase_probe_v2.py','run_boss_butcher_phase_probe_v3.py',Path(__file__).name]:files.append(WORK/'tools'/name)
for name in ['boss/ai/ButcherPhases.java','boss/ai/ButcherPhaseRules.java','entity/MeatMountainEntity.java','boss/ai/ButcherCombat.java','boss/MeatBoss.java']:files.append(WORK/'boss-core/src/main/java/net/farmingtales/ftboss'/name)
for name in ['reports/stage06-terminal-audit-20261112.json','reports/stage06-closeout-initial-failure.json','releases/boss-core-stage06-flight-guard-20261112/closeout-archive-receipt.json','releases/boss-core-stage06-flight-guard-20261112/archive-receipt.json']:files.append(WORK/name)
for result in [baseline,candidate]:
    runtime=Path(result['attempt'])
    for name in ['runtime.log','terminal.json','runtime-command.json','inputs-before.json','compile-command.json','compile-terminal.json','compile.log','live-process.json','server/actual-butcher-phase-probe.json']:files.append(runtime/name)
    for name in ['observer-classes','observer-resources','isolated-home','isolated-tmp','server/logs','server/crash-reports','server/world']:files+=list((runtime/name).rglob('*'))
archive=archive_verified(archive_path,files)
archive.update({'deployedIncrementSha256':digest(target),'fullBossInstallable':False,'scratchArchivedAndRetained':True,'scratchDeletionNotAttemptedBecausePriorPolicyRejection':True})
save(release/'archive-receipt.json',archive)
terminal=audit_processes()
terminal.update({'stage':'stage07','deployedSha256':digest(target),'backupSha256':digest(backup),'archiveSha256':archive['archiveSha256'],'deploymentTemporaryFiles':[path.name for path in target.parent.glob(target.name+'.stage*.tmp')],'forgeUnchanged':digest(forge_target)==forge_before,'weaponFunctionalDevelopmentPaused':True,'stage03PositiveKillAccepted':False,'combatAttackProfilesAccepted':False,'fullBossInstallable':False})
assert not terminal['deploymentTemporaryFiles']
terminal_path=WORK/'reports/stage07-terminal-audit-20261112.json'
save(terminal_path,terminal)
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path)
    state.update({'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'bossActiveAttempt':None,'activeWorkstream':'boss-stage07-closed-next-butcher-combat-profile-comparison','latestBossIncrementStatus':'stage07 verified/deployed/archived','latestDeploymentReceipt':str(release/'deployment-receipt.json'),'latestDeployedSha256':digest(target),'latestBossArchive':str(archive_path),'latestBossArchiveSha256':archive['archiveSha256'],'stage07TerminalAudit':str(terminal_path),'nextBossAction':'Compare original Butcher attack profiles and ButcherSkills callsites with a genuine native baseline before any further product changes. Weapons stay paused.','fullBossInstallable':False,'weaponFunctionalDevelopmentPaused':True,'ownedJavaPythonRemaining':0})
    if path.name=='CURRENT_BOSS_BATCH.json':state.update({'status':'stage07_completed_deployed_archived_weapons_paused_next_butcher_combat_audit','currentStage':'stage07 phase lifecycle completed/deployed/archived; attack profiles remain next batch'})
    patch_text(path,json.dumps(state,ensure_ascii=False,indent=2)+chr(10))
checkpoint=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md'
text=checkpoint.read_text(encoding='utf-8')
text='# Boss remaining checkpoint after stage07'+chr(10)+chr(10)+'Stage07 Butcher phase lifecycle is deployed and hash-verified in its archive. Stage06 audit/archive/closeout recovery evidence was reused. Next: compare original Butcher phase attack profiles, hit effects and ButcherSkills to a genuine native baseline; do not infer a missing feature merely from missing class names. The phase lifecycle passes scoped real NeoForge tests, not authenticated full-battle/client acceptance. Weapons stay paused and noninstallable. Stage03 positive authenticated reward integration remains pending.'+chr(10)+chr(10)+'## Retained earlier investigation and acceptance boundaries'+chr(10)+chr(10)+text
patch_text(checkpoint,text)
closeout_path=WORK/'archives/boss-core-stage07-closeout-20261112.zip'
closeout=archive_verified(closeout_path,[ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',checkpoint,terminal_path,release/'archive-receipt.json',release/'deployment-receipt.json',release/'RESTORE.md'])
save(release/'closeout-archive-receipt.json',closeout)
assert digest(target)==built['jarSha256'] and digest(backup)==built['baseJarSha256']
print(json.dumps({'deploymentReceipt':str(release/'deployment-receipt.json'),'deployedSha256':digest(target),'archive':archive,'closeout':closeout,'candidateCases':len(candidate['probe']['cases']),'ownedJavaPythonRemaining':0,'fullBossInstallable':False},ensure_ascii=False),flush=True)
