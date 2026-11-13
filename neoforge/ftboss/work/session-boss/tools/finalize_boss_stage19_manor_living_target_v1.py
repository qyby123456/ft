import argparse
import datetime
import json
import os
import shutil
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage19-manor-living-target-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py';functions={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
parser=argparse.ArgumentParser();parser.add_argument('entry',type=Path);parser.add_argument('skills',type=Path);arguments=parser.parse_args()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f';assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
process_audit=processes();assert not RELEASE.exists()
baseline_path=WORK/'reports/manor-living-target-v2-baseline-4296ed67.json';entry_path=arguments.entry.resolve();skills_path=arguments.skills.resolve();assert entry_path.parent==skills_path.parent==WORK/'reports';assert entry_path.name.startswith('manor-living-target-v3-candidate-') and skills_path.name.startswith('manor-living-skills-')
baseline=load(baseline_path);entry=load(entry_path);skills=load(skills_path)
for value in [baseline,entry,skills]:
    terminal=value['terminal'];assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None;assert value['probe']['failures']==0
    for name,wanted in value['inputHashes'].items():assert sha(name)==wanted,name
assert baseline['probe']['actualManorLivingTargetGapDemonstrated'] and len(baseline['probe']['cases'])==55
assert entry['probe']['manorLivingTargetEntryAccepted'] and len(entry['probe']['cases'])==61 and {row['variant'] for row in entry['probe']['rows']}=={0,1,4,5,6,7}
assert all(row['selectedNonEmptySkill'] and row['actuallyCasting'] for row in entry['probe']['rows'])
probe=skills['probe'];assert probe['manorLivingSkillExecutionAccepted'] and len(probe['rows'])==len(probe['plans'])>=12 and probe['completedSkills']==len(probe['plans'])
assert {row['variant'] for row in probe['rows']}=={0,1,4,5,6,7};assert all(row['actualPulseHits']==row['expectedExistingPulses'] and row['actualPulseHits']>0 and row['actualSkillDamage']>0 for row in probe['rows'])
required=['OriginalPolicyRejectsNull','OriginalPolicyRejectsSelf','OriginalPolicyRejectsArmorStand','OriginalPolicyRejectsForeignLevelLiving','ExistingNoAiRejectsLivingEntry','RejectedTargetsDoNotAdvanceRotationOrCreateCue','RemovedLivingTargetCancelsExpandedCast','NoAiCancelsExpandedLivingCast','InterruptedExpandedCastNoLivingDamage'];assert all(name in probe['cases'] for name in required)
build_path=WORK/'build/boss-manor-living-target-v3/candidate-build-receipt.json';built=load(build_path);jar=Path(built['jar']);expected='7dcf0e671590698a0fc411c531e7723264ef34d5ffdea05e99e2a667d526f3ce';assert sha(jar)==built['jarSha256']==expected
assert built['allOtherEntriesByteIdentical'] and built['originalLivingBoundaryMethods']==5 and built['stage13Included'] is False
assert 'unrelatedMethodsPreserved=20;methodsBasicVerified=25;originalNearbyCalls=2;originalResolveCalls=1;' in built['proof']
for name,wanted in built['inputHashes'].items():assert sha(name)==wanted,name
for value in [entry,skills]:assert value['inputHashes'][str(jar)]==expected
oracle=WORK/'build/manor-rotation-public-oracle-v1/1.log';assert 'originalPublicRotationOracleCases=25284;emptySlotsAndNegativeRotationsPreserved=true;' in oracle.read_text(encoding='utf-8')
previous_path=WORK/'releases/boss-core-stage18-gongdi-public-contract-20261113/deployment-receipt.json';previous=load(previous_path);prior_archive_path=previous_path.parent/'archive-receipt.json';prior_archive=load(prior_archive_path);assert sha(prior_archive['archive'])==prior_archive['archiveSha256'] and prior_archive['failures']==0
prior_feature_path=previous_path.parent/'feature-verification.json';prior_feature=load(prior_feature_path);regression_path=Path(prior_feature['nativeReport']);regression=load(regression_path);stage18_build=load(WORK/'build/boss-gongdi-public-contract-v1/candidate-build-receipt.json')
assert regression['probe']['failures']==0 and regression['probe']['publicLivingHookAccepted'] and regression['terminal']['exitCode']==0 and regression['terminal']['completeSave'] and regression['inputHashes'][stage18_build['jar']]==built['baseJarSha256']
target=Path(previous['target']);old=previous['deployedSha256'];assert sha(target)==old==built['baseJarSha256']=='5aabc09b09e836eee820bda998077561b6fbe7b5a6d9673e80e6ca0d3f9a0cfa'
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None and set(current.namelist())==set(base.namelist());assert sorted(name for name in base.namelist() if current.read(name)!=base.read(name))==built['changedExistingEntries']==['net/farmingtales/ftboss/boss/ai/ManorFiveSkills.class'];assert sum(name.endswith('.class') for name in current.namelist())==1446
    assert all(current.read(name)==base.read(name) for name in base.namelist() if name not in built['changedExistingEntries'])
paused_path=WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json';paused=load(paused_path);assert paused['failures']==0 and sha(paused['archive'])==paused['archiveSha256'] and sha(paused['snapshotJar'])==paused['snapshotJarSha256']
RELEASE.mkdir();released=RELEASE/'ftboss-boss-core-1.21.1-stage19-manor-living-target.jar';backup=RELEASE/'rollback-stage18-qys-bosses-1.21.1.jar';shutil.copyfile(jar,released);shutil.copyfile(target,backup);assert sha(released)==expected and sha(backup)==old
feature={'scope':'Five target-boundary methods in ManorFiveSkills only:original Living validity/policy,nearby query,UUID resolution and guarded text/effects type roles.20 unrelated methods and all other JAR entries byte-identical. Existing cadence/damage/shape/FX instructions preserved. Original additional echo/airborne/balance/body/FX parity remains separate.','baseline':str(baseline_path),'entry':str(entry_path),'skills':str(skills_path),'baselineCases':55,'entryCases':61,'skillCases':len(probe['cases']),'completedSkills':probe['completedSkills'],'naturalPostCallbacks':probe['naturalPostCallbacks'],'directedPulseFixtureUsesPublicHealthRestore':True,'requiredNativeControls':required,'originalRotationPureCases':25284,'originalIntentionalEmptySlotsPreserved':True,'stage18NativePublicHookEvidenceReused':{'report':str(regression_path),'sha256':sha(regression_path),'cases':32,'basis':'EasterBossEntity,GongdiCombatRules and every other entry except ManorFiveSkills unchanged bytes'},'priorWrongNullSlotAssumptionPreservedAndNotAcceptance':True,'stage13Included':False,'fullBossMigrationComplete':False,'naturalBossAiLoopAccepted':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'fullBattleAccepted':False,'weaponsPaused':True};save(RELEASE/'feature-verification.json',feature)
save(RELEASE/'deployment-intent.json',{'target':str(target),'oldSha256':old,'newSha256':expected,'backup':str(backup),'backupSha256':sha(backup),'userAuthorizedVerifiedBossIncrement':True,'formalInstanceLaunched':False,'processAudit':process_audit})
temporary=target.with_name(target.name+'.stage19-manor.tmp');assert not temporary.exists();shutil.copyfile(released,temporary);assert sha(temporary)==expected and sha(target)==old and sha(backup)==old;os.replace(temporary,target);assert sha(target)==expected and sha(forge)==forge_sha
save(RELEASE/'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':sha(target),'backup':str(backup),'backupSha256':sha(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'actualIsolatedNativeServerLoadedExactJar':True,'formalInstanceLaunched':False,'stage13IncludedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_sha,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False});print(json.dumps({'stage19Deployed':True,'sha256':sha(target),'rollbackSha256':sha(backup),'fullBossGoalStatus':'active'}),flush=True)
selected={Path(__file__),helper,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',previous_path,prior_archive_path,previous_path.parent/'closeout-archive-receipt.json',prior_feature_path,regression_path,paused_path};external={};copies={}
reused=load(WORK/'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json');assert sha(reused['archive'])==reused['archiveSha256']
with zipfile.ZipFile(reused['archive']) as bundle:
    manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'));by_name={Path(name).name:row['sha256'] for name,row in manifest.items() if '/server/mods/' in name}
attempts=set()
for prefix in ['manor-living-target-v1-','manor-living-target-v2-','manor-living-target-v3-','manor-living-skills-v']:
    attempts.update((WORK/'build').glob(prefix+'*'))
for attempt in sorted(attempts):
    if (attempt/'terminal.json').exists():
        terminal=load(attempt/'terminal.json');assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent'];selected.add(WORK/'reports'/(attempt.name+'.json'))
        for name,wanted in load(attempt/'inputs-before.json').items():
            assert sha(name)==wanted,name;path=Path(name)
            if path.is_relative_to(ROOT):selected.add(path)
            else:external[name]=wanted
    else:assert (attempt/'compile-terminal.json').exists() and load(attempt/'compile-terminal.json')['exitCode']!=0
    for path in attempt.rglob('*'):
        if not path.is_file() or path.name=='session.lock':continue
        relative=path.relative_to(attempt)
        if relative.parts[:2]==('server','mods') and 'ftboss-boss-core-' not in path.name:
            wanted=sha(path);assert by_name.get(path.name)==wanted,path;copies[path.relative_to(ROOT).as_posix()]={'sha256':wanted,'bytes':path.stat().st_size,'restoreFrom':reused['archive']};continue
        selected.add(path)
for pattern in ['build/boss-manor-living-target-v*','build/boss-manor-five-audit-*','build/manor-rotation-public-oracle-*','tests/manor-living-target-v*','tests/manor-living-skills-v*']:
    for folder in WORK.glob(pattern):selected.update(path for path in folder.rglob('*') if path.is_file())
for pattern in ['tests/ManorLivingTargetBoundaryProof*','tests/ManorRotationPublicOracle*','tools/*manor*living*','tools/audit_boss_manor_five*']:
    selected.update(path for path in WORK.glob(pattern) if path.is_file())
save(RELEASE/'external-read-only-inputs.json',external);save(RELEASE/'copied-mods-restore-manifest.json',copies);save(RELEASE/'reused-archive.json',reused);save(RELEASE/'terminal-audit.json',{'processes':processes(),'ownedRuntimeOrSupervisorRemaining':0,'targetTemporaryAbsent':not temporary.exists(),'targetSha256':sha(target),'backupSha256':sha(backup),'forgeSha256':sha(forge),'fullBossGoalStatus':'active'})
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](RELEASE/'RESTORE.md',chr(10).join(['# Stage19 Manor expanded Living target boundaries','','Original Living policy,nearby query/UUID resolution and text/effect target types restored in five methods;20 unrelated methods and every other class/resource byte unchanged. All current expanded skill rotations tested with actual vanilla Cow targets/public controller calls on natural server Post callbacks,public health restore between directed pulses. This is not continuous authenticated/natural Boss AI battle acceptance. Original empty rotation slots retained;old wrong firstslot assumption and failed fixtures preserved. Original echo/airborne/balance/FX/body hooks remain pending.','','Rollback:stop correct NeoForge instance,verify exact target/current/backup hashes,restore rollback-stage18-qys-bosses-1.21.1.jar. Common mod copies restore by exact-name/SHA from frozen315 archive. Forge/historical D read-only,Stage13 excluded,weapons paused,full Boss goal active.','']))
selected.update(path for path in RELEASE.rglob('*') if path.is_file());archived=archive(WORK/'archives/boss-core-stage19-manor-living-target-20261113.zip',selected);save(RELEASE/'archive-receipt.json',archived);print(json.dumps({'stage19Deployed':True,'archive':archived,'fullBossGoalStatus':'active'}),flush=True)
