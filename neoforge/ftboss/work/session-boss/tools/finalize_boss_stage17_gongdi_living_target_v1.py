import argparse
import datetime
import json
import os
import shutil
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage17-gongdi-living-target-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py'
namespace={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],namespace)
load=namespace['load'];sha=namespace['sha'];save=namespace['save'];archive=namespace['archive'];processes=namespace['processes']
parser=argparse.ArgumentParser();parser.add_argument('skills',type=Path);parser.add_argument('lifecycle',type=Path);arguments=parser.parse_args()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False
process_audit=processes();assert not RELEASE.exists()
base_path=WORK/'reports/gongdi-living-target-v1-baseline-01f9f88c.json';entry_path=WORK/'reports/gongdi-living-target-v1-candidate-28e794f2.json';skills_path=arguments.skills.resolve();lifecycle_path=arguments.lifecycle.resolve()
assert skills_path.parent==lifecycle_path.parent==WORK/'reports' and skills_path.name.startswith('gongdi-skills-') and lifecycle_path.name.startswith('gongdi-lifecycle-v2-candidate-')
reports=[load(path) for path in [base_path,entry_path,skills_path,lifecycle_path]]
for report in reports:
    terminal=report['terminal'];assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
    assert report['probe']['failures']==0
    for name,wanted in report['inputHashes'].items():assert sha(name)==wanted,name
baseline,entry,skills,lifecycle=reports
assert baseline['probe']['actualOriginalLivingTargetGapDemonstrated'] and len(baseline['probe']['cases'])==25
assert entry['probe']['livingTargetEntryAccepted'] and len(entry['probe']['cases'])==28
assert skills['probe']['livingTargetSkillExecutionAccepted'] and skills['probe']['naturalPostCallbacks']>=90
required=['ActualLivingGuillotineDamageMatchesOriginalRules','ActualLivingStrikeDamageMatchesOriginalRules','ActualLivingStrikeSlowAmplifier3','ActualLivingPullVelocityTowardBoss','ActualLivingPullSlow','OutsideConeLivingTargetUnaffected','ActualAdmittedLivingBleedMatchesOriginalPulseRules','ResetStopsLivingTargetBleed','ChangedCueCancelsPendingCast','ChangedCueNoLivingDamage','DiscardedLivingTargetCancelsPendingCast','DiscardedTargetCueCancelled']
assert all(name in skills['probe']['cases'] for name in required)
assert lifecycle['probe']['lifecycleFeatureAccepted'] and len(lifecycle['probe']['cases'])==35
build_path=WORK/'build/boss-gongdi-living-target-v2/candidate-build-receipt.json';built=load(build_path);jar=Path(built['jar'])
assert sha(jar)==built['jarSha256']=='021de8b498baf3d215220ca2399e68471864872a7be75b775bb6ba3e588d7162'
assert built['allOtherEntriesByteIdentical'] and built['stage13Included'] is False
assert 'allExistingEntityMethodsPreserved=295;methodsBasicVerified=297;' in built['proof']
for report in [entry,skills,lifecycle]:assert report['inputHashes'][str(jar)]==built['jarSha256']
for name,wanted in built['inputHashes'].items():assert sha(name)==wanted,name
assert 'originalPureRuleMethodBodiesEquivalent=16' in (WORK/'build/gongdi-pure-rules-proof-v2/1.log').read_text(encoding='utf-8')
previous_path=WORK/'releases/boss-core-stage16-gongdi-lifecycle-20261113/deployment-receipt.json';previous=load(previous_path)
previous_archive_path=previous_path.parent/'archive-receipt.json';previous_archive=load(previous_archive_path)
assert sha(previous_archive['archive'])==previous_archive['archiveSha256'] and previous_archive['failures']==0
target=Path(previous['target']);old=previous['deployedSha256'];assert sha(target)==old==built['baseJarSha256']
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar');forge_sha='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3';assert sha(forge)==forge_sha
with zipfile.ZipFile(built['baseJar']) as base,zipfile.ZipFile(jar) as current:
    assert current.testzip() is None and set(current.namelist())-set(base.namelist())=={built['addedClass']}
    assert sorted(name for name in base.namelist() if current.read(name)!=base.read(name))==built['changedExistingEntries']
    assert all(current.read(name)==base.read(name) for name in base.namelist() if name not in built['changedExistingEntries'])
    assert sum(name.endswith('.class') for name in current.namelist())==1446
paused_path=WORK/'archives/weapons-paused-cf217105-boss-priority-v2-20261113/archive-receipt.json';paused=load(paused_path)
assert paused['failures']==0 and sha(paused['archive'])==paused['archiveSha256'] and sha(paused['snapshotJar'])==paused['snapshotJarSha256']
RELEASE.mkdir();released=RELEASE/'ftboss-boss-core-1.21.1-stage17-gongdi-living-target.jar';backup=RELEASE/'rollback-stage16-qys-bosses-1.21.1.jar'
shutil.copyfile(jar,released);shutil.copyfile(target,backup);assert sha(released)==built['jarSha256'] and sha(backup)==old
feature={'scope':'Original Gongdi Living controller/bleed/target ranges/rotation/cue cancellation and cooldown helper restored. Two original Living entity hooks added with guarded Player HUD;all295 existing entity methods unchanged. Three previous classes changed,one helper added;every other entry unchanged.','baseline':str(base_path),'candidate':str(entry_path),'skills':str(skills_path),'lifecycleRegression':str(lifecycle_path),'baselineCases':25,'candidateCases':28,'skillCases':len(skills['probe']['cases']),'naturalPostCallbacks':skills['probe']['naturalPostCallbacks'],'skillRequiredChecks':required,'lifecycleCases':35,'pureRuleExecutableMethodsEquivalent':16,'legacySharedConstantFieldsStillPending':True,'naturalBossAiLoopAccepted':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'fullBattleAccepted':False,'stage13Included':False,'weaponsPaused':True,'fullBossMigrationComplete':False}
save(RELEASE/'feature-verification.json',feature)
save(RELEASE/'deployment-intent.json',{'target':str(target),'oldSha256':old,'newSha256':built['jarSha256'],'backup':str(backup),'backupSha256':sha(backup),'userAuthorizedVerifiedBossIncrement':True,'formalInstanceLaunched':False,'processAudit':process_audit})
temporary=target.with_name(target.name+'.stage17-gongdi.tmp');assert not temporary.exists();shutil.copyfile(released,temporary)
assert sha(temporary)==built['jarSha256'] and sha(target)==old and sha(backup)==old
os.replace(temporary,target);assert sha(target)==built['jarSha256'] and sha(forge)==forge_sha
save(RELEASE/'deployment-receipt.json',{'deployedAt':datetime.datetime.now(datetime.timezone.utc).isoformat(),'target':str(target),'deployedSha256':sha(target),'backup':str(backup),'backupSha256':sha(backup),'backupHashVerifiedBeforeDeployment':True,'previousDeployment':str(previous_path),'incrementalFeatureAccepted':True,'actualIsolatedNativeServerLoadedExactJar':True,'formalInstanceLaunched':False,'stage13IncludedOrDeployed':False,'weaponFunctionalDevelopmentPaused':True,'weaponCandidateValidatedOrDeployed':False,'forgeSha256Unchanged':forge_sha,'historicalDModified':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'authenticatedEntryAccepted':False,'authenticatedRewardsAccepted':False,'authenticatedMultiplayerAccepted':False,'completeBattleAccepted':False})
print(json.dumps({'stage17Deployed':True,'sha256':sha(target),'rollbackSha256':sha(backup),'fullBossMigrationComplete':False}),flush=True)
selected={Path(__file__),helper,ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',previous_path,previous_archive_path,previous_path.parent/'closeout-archive-receipt.json',paused_path};external={};copies={}
reused=load(WORK/'reports/stage13-client-fx-20261113/reuse-frozen-evidence.json');assert sha(reused['archive'])==reused['archiveSha256']
with zipfile.ZipFile(reused['archive']) as bundle:
    manifest=json.loads(bundle.read('ARCHIVE_MANIFEST.json'));by_name={Path(name).name:row['sha256'] for name,row in manifest.items() if '/server/mods/' in name}
attempts=set()
for prefix in ['gongdi-living-target-v1-','gongdi-skills-v1-','gongdi-skills-v2-','gongdi-skills-v3-']:
    attempts.update((WORK/'build').glob(prefix+'*'))
attempts.add(Path(lifecycle['attempt']))
for attempt in sorted(attempts):
    terminal=load(attempt/'terminal.json');assert terminal['exitCode']==0 and terminal['completeSave'] and terminal['stopSent']
    selected.add(WORK/'reports'/(attempt.name+'.json'))
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
for pattern in ['build/boss-gongdi-living-target-v*','build/boss-gongdi-target-audit-*','build/gongdi-pure-rules-proof-*','tests/gongdi-living-target-v*','tests/gongdi-skills-v*','proposal-stage17-gongdi-living-target']:
    for folder in WORK.glob(pattern):selected.update(path for path in folder.rglob('*') if path.is_file())
for pattern in ['tests/GongdiLivingHookProof*','tests/GongdiPureRulesProof*','tools/*gongdi*living*','tools/*gongdi*skills*','tools/audit_boss_gongdi_targets*']:
    selected.update(path for path in WORK.glob(pattern) if path.is_file())
selected.add(WORK/'build/boss-remaining-behavior-audit-20261112/original-sources/net/farmingtales/ftboss/boss/ai/GongdiGuillotineCooldown.java')
save(RELEASE/'external-read-only-inputs.json',external);save(RELEASE/'copied-mods-restore-manifest.json',copies);save(RELEASE/'reused-archive.json',reused)
save(RELEASE/'terminal-audit.json',{'processes':processes(),'ownedRuntimeOrSupervisorRemaining':0,'targetTemporaryAbsent':not temporary.exists(),'targetSha256':sha(target),'backupSha256':sha(backup),'forgeSha256':sha(forge),'fullBossGoalStatus':'active'})
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](RELEASE/'RESTORE.md','# Stage17 Gongdi Living target skills\n\nOriginal Living target controller,bleed identity/ranges/rotation/cooldown and interrupted cue rules restored. Player bridge descriptors and guarded HUD retained.295 existing entity methods unchanged;two original Living hooks added. Public controller calls on naturally scheduled server Post callbacks verify actual skill damage,pull,bleed and cancellation. This is not a natural Boss AI/full authenticated encounter acceptance. Legacy shared field constants remain a separate audit.\n\nAll failed fixtures retained;never seed from failed worlds. Common mods restore from exact-name/SHA frozen315 archive via copied-mods manifest;external SDK paths remain read-only. Rollback:stop correct NeoForge instance,verify exact target/current and backup hashes,restore rollback-stage16-qys-bosses-1.21.1.jar. Forge/historical D read-only;weapons paused;full Boss goal remains active.\n'.replace('\\n','\n'))
selected.update(path for path in RELEASE.rglob('*') if path.is_file())
archived=archive(WORK/'archives/boss-core-stage17-gongdi-living-target-20261113.zip',selected);save(RELEASE/'archive-receipt.json',archived)
print(json.dumps({'archive':archived,'stage17Deployed':True,'targetSha256':sha(target),'fullBossGoalStatus':'active'}),flush=True)
