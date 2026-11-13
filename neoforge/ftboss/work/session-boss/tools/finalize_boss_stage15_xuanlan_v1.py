import argparse
import datetime
import hashlib
import json
import os
import shutil
import zipfile
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage15-xuanlan-limits-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py'
namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],namespace)
load=namespace['load'];sha=namespace['sha'];save=namespace['save'];archive=namespace['archive'];processes=namespace['processes']
parser=argparse.ArgumentParser();parser.add_argument('baseline',type=Path);parser.add_argument('candidate',type=Path);arguments=parser.parse_args()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
process_audit=processes()
assert not RELEASE.exists()
baseline_path=arguments.baseline.resolve();candidate_path=arguments.candidate.resolve()
assert baseline_path.name=='xuanlan-flywheel-v2-baseline-8643a55b.json'
assert candidate_path.parent==WORK/'reports' and candidate_path.name.startswith('xuanlan-flywheel-v3-candidate-')
baseline=load(baseline_path);candidate=load(candidate_path)
for report in [baseline,candidate]:
    terminal=report['terminal'];assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert report['probe']['failures']==0
    for name,wanted in report['inputHashes'].items():assert sha(name)==wanted,name
assert baseline['probe']['actualOriginal15CapMissingDemonstrated'] and len(baseline['probe']['cases'])==86
probe=candidate['probe'];assert probe['nativeAdmissionAndPruneAccepted'] and probe['naturalServerPostCallbacks']>=20
required=['OwnerAExactNativeCount','OwnerBCapIndependentOfOwnerA','OtherVariantNotCapped','CapScansActualLevelBeyondOwner48BlockBox','PublicDiscardReleasesOneSlot','NativeAdmissionAfterDiscardedSlot','ExactLiveOwnerCountAfterReplacement','NativeJoinRejectsAndDiscardsVariant9SupportOfXuanlan','NativeJoinAllowsOtherChildVariantOfXuanlan','NativeJoinAllowsVariant9SupportOfOtherOwner','ActualSupportAdmittedBeforeOwnerTransition','ActualPublicTransitionToVariant6Owner','ActualNaturalOwnerTickedWithoutObserverTickCalls','NaturalPruneKeepsOriginalOldestEntityIds','NaturalTickRemovesVariant9AfterActualOwnerTransition']
assert all(name in probe['cases'] for name in required)
assert 'Natural20TickPruneExactLiveCount:15' in probe['cases'] and len(probe['naturallySurvivingPruneWheelIds'])==15
assert probe['noSyntheticFightOrSessionAuthority']
build_path=WORK/'build/boss-xuanlan-encounter-limits-v1/candidate-build-receipt.json';built=load(build_path);jar=Path(built['jar'])
assert sha(jar)==built['jarSha256']=='92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4'
assert built['existingEntriesByteIdentical'] and built['unchangedExistingClassCount']==1443 and built['stage13Included'] is False
assert candidate['inputHashes'][str(jar)]==built['jarSha256']
for name,wanted in built['inputHashes'].items():assert sha(name)==wanted,name
previous_path=WORK/'releases/boss-core-stage14-client-resources-20261113/deployment-receipt.json';previous=load(previous_path)
previous_archive_path=previous_path.parent/'archive-receipt.json';previous_archive=load(previous_archive_path)
assert sha(previous_archive['archive'])==previous_archive['archiveSha256'] and previous_archive['failures']==0
target=Path(previous['target']);old=previous['deployedSha256'];assert sha(target)==old==built['baseJarSha256']
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None
    assert set(current.namelist())-set(base.namelist())=={built['addedClass']}
    assert all(current.read(name)==base.read(name) for name in base.namelist())
    assert sum(name.endswith('.class') for name in current.namelist())==1444
paused_path=WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json';paused=load(paused_path)
assert paused['failures']==0 and sha(paused['archive'])==paused['archiveSha256'] and sha(paused['snapshotJar'])==paused['snapshotJarSha256']
RELEASE.mkdir()
released=RELEASE/'ftboss-boss-core-1.21.1-stage15-xuanlan.jar';backup=RELEASE/'rollback-stage14-qys-bosses-1.21.1.jar'
shutil.copyfile(jar,released);shutil.copyfile(target,backup);assert sha(released)==built['jarSha256'] and sha(backup)==old
feature={'scope':'Original XuanlanEncounterLimits helper restored with NeoForge event/API names. Adds one class; all1443 prior classes and all resources unchanged. Native admission caps15 per actual variant6 owner across level,other variant controls,discarded-slot reuse,natural every20tick oldest15 prune. Actual variant9 support joins and natural public owner transitions verify owner branch. Real authenticated active fight branch remains separate.','baseline':str(baseline_path),'baselineSha256':sha(baseline_path),'candidate':str(candidate_path),'candidateSha256':sha(candidate_path),'baselineCases':86,'candidateCases':len(probe['cases']),'naturalServerPostCallbacks':probe['naturalServerPostCallbacks'],'requiredNativeChecks':required,'originalPerOwnerLimit':15,'existing1443ClassesAndResourcesPreserved':True,'stage13Included':False,'noSyntheticFightOrSessionAuthority':True,'weaponDevelopmentPaused':True,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'fullBattleAccepted':False,'fullBossMigrationComplete':False}
save(RELEASE/'feature-verification.json',feature)
save(RELEASE/'deployment-intent.json',{'target':str(target),'oldSha256':old,'newSha256':built['jarSha256'],'backup':str(backup),'backupSha256':sha(backup),'userAuthorizedVerifiedBossIncrement':True,'formalInstanceLaunched':False,'processAudit':process_audit})
temporary=target.with_name(target.name+'.stage15-xuanlan.tmp');assert not temporary.exists()
shutil.copyfile(released,temporary);assert sha(temporary)==built['jarSha256'] and sha(target)==old and sha(backup)==old
os.replace(temporary,target);assert sha(target)==built['jarSha256'] and sha(forge)==forge_sha
save(RELEASE/'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':sha(target),'backup':str(backup),'backupSha256':sha(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'actualIsolatedNativeServerLoadedExactJar':True,'formalInstanceLaunched':False,'stage13IncludedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_sha,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False})
print(json.dumps({'stage15Deployed':True,'sha256':sha(target),'rollbackSha256':sha(backup),'fullBossMigrationComplete':False}),flush=True)
selected={Path(__file__),helper,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',previous_path,previous_archive_path,previous_path.parent/'closeout-archive-receipt.json',paused_path,WORK/'reports/boss-xuanlan-v1-baseline-fixture-correction-20261113.json'}
external={};copies={}
reused=load(WORK/'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json');assert sha(reused['archive'])==reused['archiveSha256']
with zipfile.ZipFile(reused['archive']) as bundle:
    manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'));by_name={Path(name).name:row['sha256'] for name,row in manifest.items() if '/server/mods/' in name}
for prefix in ['xuanlan-flywheel-v1-baseline-','xuanlan-flywheel-v2-baseline-','xuanlan-flywheel-v3-candidate-']:
    attempts=list((WORK/'build').glob(prefix+'*'));assert len(attempts)==1,attempts
    attempt=attempts[0];terminal=load(attempt/'terminal.json');assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent']
    selected.add(WORK/'reports'/(attempt.name+'.json'))
    for name,wanted in load(attempt/'inputs-before.json').items():
        assert sha(name)==wanted,name
        path=Path(name)
        if path.is_relative_to(ROOT):selected.add(path)
        else:external[name]=wanted
    for path in attempt.rglob('*'):
        if not path.is_file() or path.name=='session.lock':continue
        relative=path.relative_to(attempt)
        if relative.parts[:2]==('server','mods') and 'ftboss-boss-core-' not in path.name:
            wanted=sha(path);assert by_name.get(path.name)==wanted,path
            copies[path.relative_to(ROOT).as_posix()]={'sha256':wanted,'bytes':path.stat().st_size,'restoreFrom':reused['archive']};continue
        selected.add(path)
for folder in ['build/boss-xuanlan-encounter-limits-v1','build/boss-xuanlan-boundary-audit-20261113','build/xuanlan-primary-owner-audit-20261113','build/xuanlan-flywheel-compile-preflight-v1','build/xuanlan-source-port-compile-preflight-v1','tests/xuanlan-flywheel-v1','tests/xuanlan-flywheel-v2','tests/xuanlan-flywheel-v3','proposal-xuanlan-encounter-limits']:
    selected.update(path for path in (WORK/folder).rglob('*') if path.is_file())
for pattern in ['*xuanlan*']:
    selected.update(path for path in (WORK/'tools').glob(pattern) if path.is_file())
original=WORK/'build/boss-remaining-behavior-audit-20261112/original-sources/net/farmingtales/ftboss/boss/ai/XuanlanEncounterLimits.java';selected.add(original)
save(RELEASE/'external-read-only-inputs.json',external);save(RELEASE/'copied-mods-restore-manifest.json',copies);save(RELEASE/'reused-archive.json',reused)
save(RELEASE/'terminal-audit.json',{'processes':processes(),'ownedRuntimeOrSupervisorRemaining':0,'targetTemporaryAbsent':not temporary.exists(),'targetSha256':sha(target),'backupSha256':sha(backup),'forgeSha256':sha(forge),'fullBossGoalStatus':'active'})
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](RELEASE/'RESTORE.md','# Stage15 Xuanlan encounter limits\n\nAdds only original XuanlanEncounterLimits as a NeoForge subscriber. All1443 previous classes/resources stay byte identical. Original per-owner variant6 cap15,actual join rejection,other variant control,natural oldest15 pruning and support owner transitions pass native directed checks. No manually invoked Entity.tick or forged fight/session authority. Authenticated active fight,full battle and multiplayer remain pending. Stage13 remains excluded. Weapons paused.\n\nArchive manifest verifies every included file. Copied common mod JARs restore by exact filename/SHA from the recorded original315 archive;external SDK/runtime remain read-only. Preserve original negative v1 world and never use it as a seed.\n\nRollback:stop the correct NeoForge instance,verify deployment-receipt target/new/backup hashes,and restore rollback-stage14-qys-bosses-1.21.1.jar to that exact target. Forge/historical D stay read-only. Full Boss goal remains active.\n')
selected.update(path for path in RELEASE.rglob('*') if path.is_file())
archived=archive(WORK/'archives/boss-core-stage15-xuanlan-limits-20261113.zip',selected);save(RELEASE/'archive-receipt.json',archived)
print(json.dumps({'archive':archived,'stage15Deployed':True,'targetSha256':sha(target),'fullBossGoalStatus':'active'}),flush=True)
