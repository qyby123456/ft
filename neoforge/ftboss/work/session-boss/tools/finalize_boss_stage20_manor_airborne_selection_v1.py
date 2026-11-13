import argparse
import datetime
import json
import os
import shutil
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage20-manor-airborne-selection-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';functions={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
parser=argparse.ArgumentParser();parser.add_argument('candidate',type=Path);arguments=parser.parse_args()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f';assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
process_audit=processes();assert not RELEASE.exists()
baseline_path=WORK/'reports/manor-airborne-selection-v1-baseline-09ea5771.json';candidate_path=arguments.candidate.resolve();assert candidate_path.parent==WORK/'reports' and candidate_path.name.startswith('manor-airborne-selection-v1-candidate-')
baseline=load(baseline_path);candidate=load(candidate_path)
for value,wanted,cases,casts in [(baseline,False,470,26),(candidate,True,486,34)]:
    terminal=value['terminal'];probe=value['probe'];assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None;assert probe['failures']==0 and len(probe['cases'])==cases and len(probe['rows'])==52 and probe['selectionDifferences']==12 and probe['emptyAirborneDifferences']==8 and probe['actualCasts']==casts;assert probe['manorAirborneSelectionAccepted']==wanted and probe['actualManorAirborneSelectionGapDemonstrated']!=wanted
    assert {row['variant'] for row in probe['rows']}=={0,1,4,5,6,7} and sum(row['groundedPublicFixture'] for row in probe['rows'])==26
    assert all(row['actualStarted']==bool(row['originalSkill'] if wanted else row['baselineSkill']) for row in probe['rows'])
    assert all(name in probe['cases'] for name in ['AllSixExpandedVariants','AllGroundedAndAirborneOriginalRotationSlots','OriginalTwelveSelectionDifferencesAndEightAirborneEmptySlots','ActualExpectedNativeCastCount','OriginalNullAndSelfPolicyStillRejects','RejectedTargetsDoNotAdvanceOriginalRotation'])
    for name,expected in value['inputHashes'].items():assert sha(name)==expected,name
build_path=WORK/'build/boss-manor-airborne-selection-v2/candidate-build-receipt.json';built=load(build_path);jar=Path(built['jar']);expected='775986661eacd6686431e21d0779bd67bba3f51e2569891a742236850f0ef30f';assert sha(jar)==built['jarSha256']==candidate['inputHashes'][str(jar)]==expected
assert built['allOtherEntriesByteIdentical'] and built['originalAirborneSelectionCallRestored']==1 and built['stage13Included'] is False
assert 'exactOriginalInstructionsInserted=5;unrelatedMethodsPreserved=24;methodsBasicVerified=25;originalHelperBodiesEquivalent=2;originalRangedFivePublicOracleCases=8195;actualPublicHelperDescriptorsLinked=7;' in built['proof']
for name,wanted in built['inputHashes'].items():assert sha(name)==wanted,name
previous_path=WORK/'releases/boss-core-stage19-manor-living-target-20261113/deployment-receipt.json';previous=load(previous_path);prior_archive_path=previous_path.parent/'archive-receipt.json';prior_archive=load(prior_archive_path);assert sha(prior_archive['archive'])==prior_archive['archiveSha256'] and prior_archive['failures']==0 and prior_archive['allFilesReadBackAndHashVerified']
prior_feature_path=previous_path.parent/'feature-verification.json';prior_feature=load(prior_feature_path);regression_path=Path(prior_feature['skills']);regression=load(regression_path);assert regression['probe']['failures']==0 and regression['probe']['completedSkills']==13 and len(regression['probe']['cases'])==1116 and regression['terminal']['exitCode']==0 and regression['terminal']['completeSave'];assert built['baseJarSha256'] in regression['inputHashes'].values()
assert baseline['inputHashes'][str(Path(load(WORK/'build/boss-manor-living-target-v4/candidate-build-receipt.json')['jar']))]==built['baseJarSha256']
target=Path(previous['target']);old=previous['deployedSha256'];assert sha(target)==old==built['baseJarSha256']=='15feeefbbed6dbd4d488b721b8b2e34562a1e0fbeb3a2eb844a6d80da6bc6bf9'
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None and set(current.namelist())==set(base.namelist());assert sorted(name for name in base.namelist() if current.read(name)!=base.read(name))==built['changedExistingEntries']==['net/farmingtales/ftboss/boss/ai/ManorFiveSkills.class'];assert sum(name.endswith('.class') for name in current.namelist())==1446
paused_path=WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json';paused=load(paused_path);assert paused['failures']==0 and sha(paused['archive'])==paused['archiveSha256'] and sha(paused['snapshotJar'])==paused['snapshotJarSha256']
RELEASE.mkdir();released=RELEASE/'ftboss-boss-core-1.21.1-stage20-manor-airborne-selection.jar';backup=RELEASE/'rollback-stage19-qys-bosses-1.21.1.jar';shutil.copyfile(jar,released);shutil.copyfile(target,backup);assert sha(released)==expected and sha(backup)==old
save(RELEASE/'feature-verification.json',{'scope':'Exact5original instructions restore airborneFive immediately after select in tryStart.24other methods and every other class/resource byte unchanged. Original grounded identity and null ranged fallback preserved.','baseline':str(baseline_path),'candidate':str(candidate_path),'baselineCases':470,'candidateCases':486,'nativeGroundedAndAirborneSlots':52,'variants':[0,1,4,5,6,7],'selectionDifferencesRestored':12,'airborneEmptySlotsRestored':8,'nativeCandidateCasts':34,'originalHelperBodiesEquivalent':2,'rangedFivePublicPureOracleCases':8195,'actualPublicHelperDescriptorsLinked':7,'publicSetOnGroundStateFixture':True,'naturalPhysicsTransitionAccepted':False,'stage19UnchangedSkillExecutionEvidenceReused':{'report':str(regression_path),'sha256':sha(regression_path),'cases':1116,'skills':13,'naturalPostCallbacks':425,'basis':'All24non-entry methods including tick/strike/effects/geometry unchanged;only choice expression restored. Candidate actual grounded/airborne entry/animation observations bind expected chosen skill;not a new combined damage/full battle claim.'},'otherOriginalTimingEchoBalanceBodyFxParityPending':True,'stage13Included':False,'weaponsPaused':True,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'naturalBossAiLoopAccepted':False,'fullBattleAccepted':False,'fullBossMigrationComplete':False})
save(RELEASE/'deployment-intent.json',{'target':str(target),'oldSha256':old,'newSha256':expected,'backup':str(backup),'backupSha256':sha(backup),'userAuthorizedVerifiedBossIncrement':True,'processAudit':process_audit,'formalInstanceLaunched':False})
temporary=target.with_name(target.name+'.stage20-airborne.tmp');assert not temporary.exists();shutil.copyfile(released,temporary);assert sha(temporary)==expected and sha(target)==old and sha(backup)==old;os.replace(temporary,target);assert sha(target)==expected and sha(forge)==forge_sha
save(RELEASE/'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':sha(target),'backup':str(backup),'backupSha256':sha(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'actualIsolatedNativeServerLoadedExactJar':True,'formalInstanceLaunched':False,'stage13IncludedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_sha,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False});print(json.dumps({'stage20Deployed':True,'sha256':sha(target),'rollbackSha256':sha(backup),'fullBossGoalStatus':'active'}),flush=True)
selected={Path(__file__),helper,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',previous_path,prior_archive_path,previous_path.parent/'closeout-archive-receipt.json',prior_feature_path,regression_path,paused_path};external={};copies={}
reused=load(WORK/'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json');assert sha(reused['archive'])==reused['archiveSha256']
with zipfile.ZipFile(reused['archive']) as bundle:
    manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'));by_name={Path(name).name:row['sha256'] for name,row in manifest.items() if '/server/mods/' in name}
for attempt in sorted((WORK/'build').glob('manor-airborne-selection-v*')):
    terminal=load(attempt/'terminal.json');assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent'];selected.add(WORK/'reports'/(attempt.name+'.json'))
    for name,wanted in load(attempt/'inputs-before.json').items():
        assert sha(name)==wanted,name;path=Path(name)
        if path.is_relative_to(ROOT):selected.add(path)
        else:external[name]=wanted
    for path in attempt.rglob('*'):
        if not path.is_file() or path.name=='session.lock':continue
        relative=path.relative_to(attempt)
        if relative.parts[:2]==('server','mods') and 'ftboss-boss-core-' not in path.name:
            wanted=sha(path);assert by_name.get(path.name)==wanted,path;copies[path.relative_to(ROOT).as_posix()]={'sha256':wanted,'bytes':path.stat().st_size,'restoreFrom':reused['archive']};continue
        selected.add(path)
for pattern in ['build/boss-manor-airborne-selection-v*','build/boss-manor-airborne-audit-*','tests/manor-airborne-selection-v*']:
    for folder in WORK.glob(pattern):selected.update(path for path in folder.rglob('*') if path.is_file())
for pattern in ['tests/ManorAirborneSelectionProof*','tools/*manor*airborne*']:
    selected.update(path for path in WORK.glob(pattern) if path.is_file())
save(RELEASE/'external-read-only-inputs.json',external);save(RELEASE/'copied-mods-restore-manifest.json',copies);save(RELEASE/'reused-archive.json',reused);save(RELEASE/'terminal-audit.json',{'processes':processes(),'ownedRuntimeOrSupervisorRemaining':0,'targetTemporaryAbsent':not temporary.exists(),'targetSha256':sha(target),'backupSha256':sha(backup),'forgeSha256':sha(forge),'fullBossGoalStatus':'active'})
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](RELEASE/'RESTORE.md',chr(10).join(['# Stage20 original Manor airborne skill choice','','Only exact5original instructions inserted after select in tryStart;24other methods unchanged. Native baseline470/candidate486check all52 grounded/airborne rotation slots across6variants,12changed choices and8formerly empty airborne casts. Public setOnGround state fixture and animation/cue observations;not a natural physics or full battle claim.8195public pure ranged cases equivalent,2helper bodies equivalent,7descriptors linked.','Stage19unchanged damage/effects execution evidence reused by exact non-entry method identity. Original remaining timing/echo/balance/body/FX and authenticated/rewards/multiplayer/full battles pending;Stage13excluded,weapons paused.','','Deployment: '+str(target),'Deployed SHA256: '+expected,'Rollback to Stage19: '+str(backup),'Rollback SHA256: '+old,'Stop Minecraft before restoring;verify file SHA256. Failed proofV1 retained;no candidate produced from it.','Native worlds retained and normal complete saved;not reused as seeds.','Common native copied mods restored using copied-mods-restore-manifest.json and reused-archive.json. Historical D library inputs remain external read-only dependencies.','Full Boss goal active,same sole C writer.']))
selected.update(path for path in RELEASE.rglob('*') if path.is_file());receipt=archive(WORK/'archives/boss-core-stage20-manor-airborne-selection-20261113.zip',selected);save(RELEASE/'archive-receipt.json',receipt);assert sha(target)==expected and sha(forge)==forge_sha;print(json.dumps({'stage20Deployed':True,'archive':receipt,'fullBossGoalStatus':'active'}),flush=True)
