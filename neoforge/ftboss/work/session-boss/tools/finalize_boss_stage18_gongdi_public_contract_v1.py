import argparse
import datetime
import json
import os
import shutil
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage18-gongdi-public-contract-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';functions={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
parser=argparse.ArgumentParser();parser.add_argument('report',type=Path);arguments=parser.parse_args()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f';assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
process_audit=processes();assert not RELEASE.exists()
report_path=arguments.report.resolve();assert report_path.parent==WORK/'reports' and report_path.name.startswith('gongdi-public-contract-v1-candidate-');report=load(report_path);terminal=report['terminal'];probe=report['probe']
assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
assert probe['failures']==0 and probe['publicLivingHookAccepted'] and len(probe['cases'])==32
for name,wanted in report['inputHashes'].items():assert sha(name)==wanted,name
required=['OriginalInnerRadius675','OriginalStrikeRange765','OriginalPullRange1575','OriginalExecuteRange10125','OriginalExecuteCooldown600','OriginalExecuteReset200','OriginalLivingCleaveHookBuildsBloodRageFull','ActualLivingTargetSurvivesDirectedHookChecks']
assert all(name in probe['cases'] for name in required)
assert [row['armorShredStacks'] for row in probe['rows']]==[1,2,3,4,5,5] and all(row['actualHealthLoss']>0 for row in probe['rows'])
build_path=WORK/'build/boss-gongdi-public-contract-v1/candidate-build-receipt.json';built=load(build_path);jar=Path(built['jar']);expected='5aabc09b09e836eee820bda998077561b6fbe7b5a6d9673e80e6ca0d3f9a0cfa';assert sha(jar)==built['jarSha256']==expected
assert built['allOtherEntriesByteIdentical'] and built['allExistingEntityMethodsPreserved']==297 and built['allRuleExecutableMethodsPreserved']==9 and built['originalSharedConstantFieldsRestored']==6 and built['stage13Included'] is False
for name,wanted in built['inputHashes'].items():assert sha(name)==wanted,name
assert report['inputHashes'][str(jar)]==expected
previous_path=WORK/'releases/boss-core-stage17-gongdi-living-target-20261113/deployment-receipt.json';previous=load(previous_path);prior_archive_path=previous_path.parent/'archive-receipt.json';prior_archive=load(prior_archive_path);assert sha(prior_archive['archive'])==prior_archive['archiveSha256'] and prior_archive['failures']==0
prior_feature_path=previous_path.parent/'feature-verification.json';prior_feature=load(prior_feature_path);regression={}
for key in ['candidate','skills','lifecycleRegression']:
    path=Path(prior_feature[key]);value=load(path);assert value['probe']['failures']==0 and value['terminal']['exitCode']==0 and value['terminal']['completeSave'] and value['terminal']['inputHashesUnchanged'];stage17_build=load(WORK/'build/boss-gongdi-living-target-v2/candidate-build-receipt.json');assert sha(stage17_build['jar'])==built['baseJarSha256'] and value['inputHashes'][stage17_build['jar']]==built['baseJarSha256'];regression[key]={'report':str(path),'sha256':sha(path),'cases':len(value['probe']['cases']),'reuseBasis':'All297 previous entity method instructions and9 rule executable bodies unchanged;all controller/helper/other class bytes identical. Shared field constants and new public overload are separately original-proof/native accepted.'}
target=Path(previous['target']);old=previous['deployedSha256'];assert sha(target)==old==built['baseJarSha256']=='021de8b498baf3d215220ca2399e68471864872a7be75b775bb6ba3e588d7162'
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None and set(current.namelist())==set(base.namelist());assert sorted(name for name in base.namelist() if current.read(name)!=base.read(name))==built['changedExistingEntries'];assert len(built['changedExistingEntries'])==2
    assert all(current.read(name)==base.read(name) for name in base.namelist() if name not in built['changedExistingEntries']);assert sum(name.endswith('.class') for name in current.namelist())==1446
paused_path=WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json';paused=load(paused_path);assert paused['failures']==0 and sha(paused['archive'])==paused['archiveSha256'] and sha(paused['snapshotJar'])==paused['snapshotJarSha256']
RELEASE.mkdir();released=RELEASE/'ftboss-boss-core-1.21.1-stage18-gongdi-public-contract.jar';backup=RELEASE/'rollback-stage17-qys-bosses-1.21.1.jar';shutil.copyfile(jar,released);shutil.copyfile(target,backup);assert sha(released)==expected and sha(backup)==old
feature={'scope':'Original public Living onSignatureCleaveHit overload and six shared ConstantValue fields only. All297 previous entity methods/9 rule executable methods unchanged;every other class/resource byte identical. Original melee/executeCleave Player restrictions intentionally preserved. Existing inlined legacy consumers not rewritten.','nativeReport':str(report_path),'nativeReportSha256':sha(report_path),'nativeCases':32,'requiredNativeChecks':required,'reusedStage17Regression':regression,'newHookInstructionsEqualOriginal':True,'originalSharedFieldsRestored':6,'stage13Included':False,'fullBossMigrationComplete':False,'naturalBossAiLoopAccepted':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'fullBattleAccepted':False,'weaponsPaused':True,'next':'Audit original ManorFiveSkills and other original Boss behaviors;actual authenticated/full encounter gates remain pending.','outerGenericRunnerScopeNotAuthority':'The inherited top-level report scope text describes the old three-range observer and is not applicable. The inner probe.scope,actual32 cases/source and this exact boundary proof define this run.'};save(RELEASE/'feature-verification.json',feature)
save(RELEASE/'deployment-intent.json',{'target':str(target),'oldSha256':old,'newSha256':expected,'backup':str(backup),'backupSha256':sha(backup),'userAuthorizedVerifiedBossIncrement':True,'formalInstanceLaunched':False,'processAudit':process_audit})
temporary=target.with_name(target.name+'.stage18-gongdi.tmp');assert not temporary.exists();shutil.copyfile(released,temporary);assert sha(temporary)==expected and sha(target)==old and sha(backup)==old;os.replace(temporary,target);assert sha(target)==expected and sha(forge)==forge_sha
save(RELEASE/'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':sha(target),'backup':str(backup),'backupSha256':sha(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'actualIsolatedNativeServerLoadedExactJar':True,'formalInstanceLaunched':False,'stage13IncludedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_sha,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False})
print(json.dumps({'stage18Deployed':True,'sha256':sha(target),'rollbackSha256':sha(backup),'fullBossGoalStatus':'active'}),flush=True)
selected={Path(__file__),helper,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',previous_path,prior_archive_path,previous_path.parent/'closeout-archive-receipt.json',prior_feature_path,paused_path};external={};copies={}
for value in regression.values():selected.add(Path(value['report']))
reused=load(WORK/'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json');assert sha(reused['archive'])==reused['archiveSha256']
with zipfile.ZipFile(reused['archive']) as bundle:
    manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'));by_name={Path(name).name:row['sha256'] for name,row in manifest.items() if '/server/mods/' in name}
attempt=Path(report['attempt']);assert load(attempt/'terminal.json')==terminal;selected.add(report_path)
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
for folder in ['build/boss-gongdi-public-contract-v1','build/boss-gongdi-melee-audit-v1-20261113','tests/gongdi-public-contract-v1']:
    selected.update(path for path in (WORK/folder).rglob('*') if path.is_file())
selected.add(WORK/'tests/GongdiPublicContractProof.java')
for pattern in ['tools/*gongdi*public*','tools/audit_boss_gongdi_melee*']:
    selected.update(path for path in WORK.glob(pattern) if path.is_file())
save(RELEASE/'external-read-only-inputs.json',external);save(RELEASE/'copied-mods-restore-manifest.json',copies);save(RELEASE/'reused-archive.json',reused);save(RELEASE/'reused-stage17-regression.json',regression)
save(RELEASE/'terminal-audit.json',{'processes':processes(),'ownedRuntimeOrSupervisorRemaining':0,'targetTemporaryAbsent':not temporary.exists(),'targetSha256':sha(target),'backupSha256':sha(backup),'forgeSha256':sha(forge),'fullBossGoalStatus':'active'})
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](RELEASE/'RESTORE.md','# Stage18 original Gongdi public contract\n\nAdds exact original Living cleave callback and restores six public constant values. All previous297 entity methods and9 rule method bodies unchanged;all other entries byte-identical.32 native checks show actual Cow damage followed by public original hook,armor shred1/2/3/4/5/5 and full blood rage. Earlier28/132/35 regression accepted only through exact unchanged relevant instructions. Original melee Player restrictions stay intentional;existing inlined consumers are a separate scope. Inner native probe.scope is authoritative;inherited generic outer runner scope is inapplicable.\n\nRollback:stop correct NeoForge instance,verify exact target/current and backup hashes,restore rollback-stage17-qys-bosses-1.21.1.jar. Common mod copies restore from exact-name/SHA manifest and frozen315 archive. Full authenticated/reward/multiplayer/battle acceptance pending;Stage13 excluded,weapons paused,Forge/historical D read-only.\n')
selected.update(path for path in RELEASE.rglob('*') if path.is_file());archived=archive(WORK/'archives/boss-core-stage18-gongdi-public-contract-20261113.zip',selected);save(RELEASE/'archive-receipt.json',archived)
print(json.dumps({'stage18Deployed':True,'archive':archived,'fullBossGoalStatus':'active'}),flush=True)
