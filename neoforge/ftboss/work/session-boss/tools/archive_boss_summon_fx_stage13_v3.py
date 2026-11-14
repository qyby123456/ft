import argparse
import datetime
import hashlib
import json
import os
import shutil
import subprocess
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
BUILD=WORK/'build/boss-summon-fx-v2'

def load(path):
    return json.loads(Path(path).read_text(encoding='utf-8-sig'))

def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()

def save(path,data):
    with Path(path).open('x',encoding='utf-8') as stream:json.dump(data,stream,ensure_ascii=False,indent=2)

helper=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py'
namespace={'__file__':str(helper)};text=helper.read_text(encoding='utf-8');exec(compile(text[:text.index('assert not OUT.exists()')],str(helper),'exec'),namespace)
patch_file=namespace['replace_file']
parser=argparse.ArgumentParser();parser.add_argument('candidate_report',type=Path);arguments=parser.parse_args()
baseline_path=WORK/'reports/summon-fx-v7-baseline-ab4d8ccc.json';candidate_path=arguments.candidate_report.resolve()
baseline=load(baseline_path);candidate=load(candidate_path)
for result in [baseline,candidate]:
    terminal=result['terminal'];assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert result['probe']['failures']==0 and not result['authenticatedMultiplayerAccepted'] and not result['fullBossAcceptance']
    for name,wanted in result['inputHashes'].items():assert sha(name)==wanted,name
assert baseline['baselineGapObserved'] and baseline['probe']['actualSummonFxMissingDemonstrated']
assert candidate['summonFxFeatureAccepted'] and candidate['probe']['summonFxFeatureAccepted'] and candidate['probe']['completedStages']==2
assert candidate['probe']['directedRegisteredEntityTicksPerNativeCallback'] and not candidate['probe']['naturalEntitySchedulingAccepted']
assert candidate['probe']['nativeTickCallbacks']>=66
assert candidate['probe']['fixtureUsesPublicCloneStateWithoutAuthenticatingPlayerIngress']
assert candidate['probe']['firstTickDiagnostic']['ownerAlive'] and not candidate['probe']['firstTickDiagnostic']['ownerRemoved']
built=load(BUILD/'candidate-build-receipt.json')
for name,wanted in built['inputHashes'].items():assert sha(name)==wanted,name
assert sha(built['jar'])==built['jarSha256']=='60cfcc8fd1ae10e4249b807a8f68c23074d251d091c4e3a804b2be20903d3b6a'
assert built['existingClassInstructionsPatchedDirectly'] and built['unrelatedRecompiledMethodsNeverPackaged']
previous=WORK/'releases/boss-core-stage12-bone-crush-20261112/deployment-receipt.json';deployed=load(previous)
paused_path=WORK/'archives/weapons-paused-8199dcc2-boss-priority-20261112/archive-receipt.json';paused=load(paused_path)
assert paused['weaponDevelopmentPaused'] and paused['weaponValidationPaused'] and sha(paused['archive'])==paused['archiveSha256']
formal=paused['formalInstancesUnchanged']
for row in formal.values():assert sha(row['path'])==row['sha256']
command="Get-CimInstance Win32_Process | Where-Object { $_.Name -in @('java.exe','javaw.exe','python.exe','pythonw.exe') } | ForEach-Object { [pscustomobject]@{pid=$_.ProcessId;name=$_.Name;owned=($_.CommandLine -match 'session-boss|session-lease')} } | ConvertTo-Json -Compress"
processes=json.loads(subprocess.check_output(['powershell','-NoProfile','-Command',command],encoding='utf-8-sig'));processes=processes if isinstance(processes,list) else [processes]
assert not any(row['owned'] and row['pid']!=os.getpid() for row in processes)
output=WORK/'candidates/boss-core-stage13-summon-fx-20261112';assert not output.exists();output.mkdir(parents=True)
jar=output/'ftboss-boss-core-1.21.1-stage13-summon-fx-NOT-INSTALLABLE.jar';shutil.copyfile(built['jar'],jar);assert sha(jar)==built['jarSha256']
feature={'feature':'Original Butcher summon FX server queue and three native admission hooks','baselineReceipt':str(baseline_path),'candidateReceipt':str(candidate_path),'baselineChecks':len(baseline['probe']['cases']),'candidateChecks':len(candidate['probe']['cases']),'nativeTickCallbacks':candidate['probe']['nativeTickCallbacks'],'directedRegisteredEntityTicks':True,'naturalEntitySchedulingAccepted':False,'actualOriginalSummonWaveVerified':True,'publicCloneFixtureUsed':True,'unchangedOriginalOrphanAdmissionGuardVerified':True,'authenticatedStartAndCloneIngressAccepted':False,'queueLimit':24,'expiryAfterTick':32,'uuidPhaseModulo':4,'perTickSummonParticleCeiling':160,'sameTickDeduplicationVerified':True,'duplicateCanceledAndNonlivingControlsVerified':True,'allFourSkillInstructionsPreserved':True,'methodsVerified':49,'existingMethodsPreserved':34,'changedClasses':built['changedEntries'],'addedClasses':built['addedEntries'],'allOtherClassAndResourceBytesPreserved':True,'serverDirectedFeatureAccepted':True,'fullBossMigrationComplete':False,'fullBossInstallable':False,'clientRenderingAccepted':False,'authenticatedMultiplayerAccepted':False,'authenticatedRewardsAccepted':False,'completeBattleAccepted':False,'weaponDevelopmentPaused':True,'weaponValidationPaused':True,'deployed':False,'formalInstancePolicy':'read_only_current_goal; current stage12 retained'}
save(output/'feature-verification.json',feature)
save(output/'external-dependencies.json',{'previousDeployment':str(previous),'previousDeploymentReceiptSha256':sha(previous),'previousDeployedJarSha256':deployed['deployedSha256'],'weaponPauseReceipt':str(paused_path),'weaponPauseReceiptSha256':sha(paused_path),'weaponPauseArchiveSha256':paused['archiveSha256'],'runtimeInputHashes':{'baseline':baseline['inputHashes'],'candidate':candidate['inputHashes']},'buildInputHashes':built['inputHashes'],'formalInstancesUnchanged':formal,'historicalDReadOnly':True,'runtimeDependenciesRemainAtOriginalRecordedLocations':True,'failedWorldsNeverUsedAsRestartSeeds':True})
save(output/'process-audit.json',{'ownedJavaPythonRemaining':0,'unrelatedProcessesPreserved':[row for row in processes if row['pid']!=os.getpid()],'rawCommandLinesRecorded':False})
restore=output/'RESTORE.md';restore.write_text('# Stage13 original Butcher summon FX candidate\n\nThis is an archived candidate, not a deployment or full migration acceptance. Verify archive-receipt.json and every ARCHIVE_MANIFEST.json member hash and size before restoring into a new empty workspace. Reuse the stage12 deployment/archive and latest separate weapon pause archive at their recorded paths; do not modify historical D.\n\nOnly three original MeatBoss admission sites and one actual Butcher controller tick callback change. All existing method instructions otherwise match the stage12 classes. The new helper preserves the original Forge addWithFx/tickSummons bodies under official native names: 24 pulses, UUID phase modulo four, original boss/minion point plans, per-level same-tick deduplication, greater-than-32 expiry and unchanged particle budgets. Directed real native wave/entity tests pass; natural no-player entity scheduling, authenticated start/clone ingress, actual client particle delivery/rendering, authenticated rewards/multiplayer and complete battles remain pending.\n\nThe first build proof failure and initial untracked nonclone fixture removal failures are retained. Native BossHandler.onEntityJoin discards untracked nonclone butchers without canceling admission; earlier fixtures incorrectly assumed true admission implied liveness. The successful observer uses the public clone state, verifies the unchanged orphan guard, and explicitly directs actual registered butcher entity ticks from native server callbacks; it does not claim natural scheduling. Weapon development/validation remains paused. Its separate JAR is a recovery container and must not be installed in mods. Neither formal instance was written for this candidate.\n',encoding='utf-8')
archive=WORK/'archives/boss-core-stage13-summon-fx-NOT-INSTALLABLE-20261112.zip'
for name in ['WRITE_OWNER.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');data=json.loads(before)
    data.update({'bossActiveRuntimePid':None,'bossActiveSupervisor':None,'bossActiveAttempt':None,'activeRuntimePid':None,'activeRuntimePids':[],'activeSupervisor':None,'ownedBossRuntimeStillRunning':False,'ownedJavaPythonProcessesRemaining':False,'activeWorkstream':'boss-core-stage13-summon-fx-server-verified-archived-not-deployed','weaponFunctionalDevelopment':'paused_by_latest_direct_user','weaponValidationPaused':True,'weaponCandidateValidationResumed':False,'latestBossCandidateReceipt':str(output/'feature-verification.json'),'latestBossCandidate':str(jar),'latestBossCandidateInstallable':False,'latestBossRuntimeReceipt':str(candidate_path),'latestBossCandidateArchive':str(archive),'latestFormalDeployment':'stage12 Bone Crush','latestDeployedSha256':deployed['deployedSha256'],'latestBossArchive':str(WORK/'archives/boss-core-stage12-bone-crush-20261112-complete.zip'),'latestBossArchiveSha256':'73d4d538d829f4855458e150046db1d2a7b878747d89b450b60824f6c167e6b8','latestVerifiedBossIncrementReceipt':str(output/'feature-verification.json'),'latestVerifiedBossRuntimeReceipt':str(candidate_path),'next':'Boss authenticated summon/start/clone and actual client FX acceptance; weapons remain paused','currentFormalInstancePolicy':'read_only_current_goal','latestBossIncrementStatus':'stage12 deployed and archived; stage13 summon FX directed-server candidate verified and archived, not deployed','lastBossRuntimePid':load(Path(candidate['attempt'])/'live-process.json')['pid'],'lastBossRuntimeTerminal':True,'nextBossIncrement':'Boss authenticated summon/start/clone and client FX acceptance; preserve weapon pause, no automatic lease/Session reactivation','fullBossMigrationComplete':False,'weaponStandaloneJarAccepted':False})
    if 'session-boss' in name:data.update({'status':'stage13_directed_server_feature_verified_archived_not_deployed','currentStage':'stage13 summon FX candidate; stage12 remains deployed','stage13DeploymentPerformed':False,'latestVerifiedBossRuntimeCases':len(candidate['probe']['cases'])})
    patch_file(path,before,json.dumps(data,ensure_ascii=False,indent=2)+'\n')
checkpoint=WORK/'REMAINING_BOSS_CHECKPOINT_20261112.md';before=checkpoint.read_text(encoding='utf-8');after='# Boss remaining checkpoint after stage13 server candidate\n\nOriginal Butcher summon FX now passes a directed native server candidate: actual summonWave, boss/minion pulses, original UUID phases, 24-entry eviction, per-tick budgets, duplicate/canceled/nonliving admission, same-tick deduplication and expiry. Two existing classes change only at three spawn redirects and one actual level callback; all 34 existing methods otherwise preserve instructions. The first build/untracked nonclone fixture failures are retained.\n\nStage13 is archived NOT-INSTALLABLE and not deployed. Both formal instances remain unchanged at their recorded hashes; NeoForge remains stage12. Natural entity scheduling, authenticated start/clone ingress, actual client particle delivery/rendering and all earlier full battle/client/authenticated gates remain pending. Weapon development and validation remain paused; the latest separate recovery JAR and 1490-file checkpoint are in archives/weapons-paused-8199dcc2-boss-priority-20261112. Do not automatically resume lease/payment/signed Session from an older persisted goal.\n\n## Previous stage12 checkpoint\n\n'+before;patch_file(checkpoint,before,after)
selected={Path(__file__),previous,paused_path,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',checkpoint,baseline_path,candidate_path}
selected.update(path for path in output.iterdir() if path.is_file())
for folder in ['proposal-stage13-summon-fx','build/boss-summon-fx-v1','build/boss-summon-fx-v2','tests/summon-fx-v1','tests/summon-fx-v2','tests/summon-fx-v3','tests/summon-fx-v4','tests/summon-fx-v5','tests/summon-fx-v6','tests/summon-fx-v7']:
    selected.update(path for path in (WORK/folder).rglob('*') if path.is_file() and '__pycache__' not in path.parts)
for name in ['tests/SummonFxBoundaryProof.java','tests/SummonFxBoundaryProofV2.java','tools/build_boss_summon_fx_stage13.py','tools/build_boss_summon_fx_stage13_v2.py','tools/run_boss_summon_fx_probe_v1.py','tools/run_boss_summon_fx_probe_v2.py','tools/run_boss_summon_fx_probe_v3.py','tools/run_boss_summon_fx_probe_v4.py','tools/run_boss_summon_fx_probe_v5.py','tools/run_boss_summon_fx_probe_v6.py','tools/run_boss_summon_fx_probe_v7.py','tools/archive_boss_summon_fx_stage13_v2.py','tools/archive_boss_summon_fx_stage13.py','build/boss-butcher-active-skill-audit-20261112/original-sources/net/farmingtales/ftboss/butcherskills/ButcherSkills.java']:
    selected.add(WORK/name)
for report in sorted((WORK/'reports').glob('summon-fx-v*.json')):
    selected.add(report);attempt=Path(load(report)['attempt']);selected.update(path for path in attempt.iterdir() if path.is_file());selected.update((attempt/'server').glob('actual-*.json'))
    for folder in ['server/logs','server/world']:
        selected.update(path for path in (attempt/folder).rglob('*') if path.is_file())
manifest={}
with zipfile.ZipFile(archive,'x',compression=zipfile.ZIP_DEFLATED) as bundle:
    for path in sorted(selected):
        assert path.is_file() and path.resolve().is_relative_to(ROOT) and not path.is_symlink(),str(path)
        name=path.relative_to(ROOT).as_posix();manifest[name]={'sha256':sha(path),'bytes':path.stat().st_size};bundle.write(path,name)
    encoded=json.dumps(manifest,ensure_ascii=False,indent=2).encode('utf-8');bundle.writestr('ARCHIVE_MANIFEST.json',encoded)
with zipfile.ZipFile(archive) as bundle:
    assert bundle.testzip() is None and bundle.read('ARCHIVE_MANIFEST.json')==encoded
    for name,row in manifest.items():
        content=bundle.read(name);assert len(content)==row['bytes'] and hashlib.sha256(content).hexdigest()==row['sha256']
for row in formal.values():assert sha(row['path'])==row['sha256']
receipt={'failures':0,'createdAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'archive':str(archive),'archiveSha256':sha(archive),'files':len(manifest),'allFilesReadBackAndHashVerified':True,'candidateJar':str(jar),'candidateJarSha256':sha(jar),'candidateDirectedServerFeatureAccepted':True,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'formalInstancesUnchanged':formal,'weaponDevelopmentPaused':True,'weaponValidationPaused':True,'firstBuildAndRuntimeFailuresPreserved':True,'allSummonRuntimeAttemptsPreserved':True,'successfulFixturePreparedAfterStartupHandlers':candidate['probe']['fixturePreparedAfterServerStartedHandlers'],'ownedJavaPythonRemaining':0}
save(output/'archive-receipt.json',receipt)
print(json.dumps({'archiveSha256':receipt['archiveSha256'],'files':len(manifest),'candidateChecks':feature['candidateChecks'],'nativeTickCallbacks':feature['nativeTickCallbacks'],'formalUnchanged':True,'deployed':False,'weaponsPaused':True}),flush=True)
