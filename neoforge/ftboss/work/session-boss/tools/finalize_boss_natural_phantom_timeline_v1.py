import json
import re
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/boss-stage24-natural-phantom-timeline-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py'
functions={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
expected='4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0'
assert not OUT.exists()
process_audit=processes()
owner=load(ROOT/'WRITE_OWNER.json')
assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert owner['weaponValidationPaused'] and owner['noNewWeaponRuns'] and owner['weaponSnapshotInstallable'] is False and owner['currentGoalStatus']=='active'
deployed=load(WORK/'releases/boss-core-stage24-manor-body-chain-20261113/deployment-receipt.json')
assert sha(deployed['target'])==deployed['deployedSha256']==expected
forge=Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar')
assert sha(forge)=='7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'
report_path=WORK/'reports/phantom-timeline-v1-candidate-51c3d135.json'
report=load(report_path);terminal=report['terminal'];probe=report['probe']
assert terminal['exitCode']==0 and terminal['stopSent'] and terminal['completeSave'] and terminal['inputHashesUnchanged'] and terminal['supervisionFailure'] is None
assert probe['failures']==0 and probe['completedCases']==12 and probe['naturalEntityTimelineSchedulingAccepted'] and probe['manualTickRestoreReplayOrPhantomHookInvocation'] is False
assert len(probe['checks'])==2896 and probe['naturalPostCallbacks']==1207 and report['deployedJarSha256']==expected and not report['formalDeploymentChanged']
assert [row['action'] for row in probe['cases']]==['normal']*4+['cancel','queue','variant','giant','fallback','missing','discard','dimension']
assert sum(len(row['captures']) for row in probe['cases'])==13
assert sum(len(row['lights']) for row in probe['cases'])==18
assert sum(len(row['damages']) for row in probe['cases'])==8
selected={Path(__file__),helper,report_path};external={}
for evidence in [report,load(WORK/'build/boss-phantom-timeline-rules-v1/proof-receipt.json')]:
    for name,wanted in evidence['inputHashes'].items():
        assert sha(name)==wanted,name
        path=Path(name)
        if path.is_relative_to(ROOT):selected.add(path)
        else:external[name]=wanted
pure=load(WORK/'build/boss-phantom-timeline-rules-v1/proof-receipt.json')
assert pure['failures']==0 and pure['proofs']['original']==pure['proofs']['deployed'] and pure['proofs']['deployed']['checks']==26
for source in (WORK/'tests/phantom-timeline-v1').glob('*.java'):
    text=source.read_text(encoding='utf-8')
    assert 'ftboss$phantomRestore' not in text and 'ftboss$phantomReplay' not in text
    assert not re.search(r'PhantomHooks\.(?:capture|tick)\s*\(',text)
for directory in [Path(report['attempt']),WORK/'tests/phantom-timeline-v1',WORK/'build/boss-phantom-timeline-audit-v1-20261113',WORK/'build/boss-phantom-timeline-rules-v1']:
    selected.update(path for path in directory.rglob('*') if path.is_file())
for path in [WORK/'tools/run_boss_phantom_timeline_v1.py',WORK/'tools/audit_boss_phantom_timeline_v1.py',WORK/'tools/prove_boss_phantom_timeline_rules_v1.py',WORK/'tests/PhantomTimelineRulesProofV1.java',WORK/'build/boss-manor-body-chain-v5/candidate-build-receipt.json',WORK/'build/boss-manor-body-chain-v5/composition-recipe.json',WORK/'build/boss-manor-body-chain-v5/effective-expected/net/farmingtales/ftboss/entity/EasterBossEntity.class']:
    selected.add(path)
OUT.mkdir()
for source in [ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md']:
    (OUT/(source.name+'.before')).write_bytes(source.read_bytes())
save(OUT/'external-read-only-inputs.json',external)
acceptance={'failures':0,'nativeReport':str(report_path),'nativeReportSha256':sha(report_path),'pureRulesReport':str(WORK/'build/boss-phantom-timeline-rules-v1/proof-receipt.json'),'deployedJarSha256':expected,'formalJarUnchanged':True,'runtimeCodeChanged':False,'newJarDeploymentRequired':False,'nativeCases':12,'nativeChecks':2896,'naturalServerPostCallbacks':1207,'actualCaptures':13,'actualTimelineLightPayloadsAndCodecRoundtrips':18,'actualAdaptiveHelperLivingDamageCalls':8,'originalAndDeployedPureRuleChecksEach':26,'all12CasesAccepted':True,'naturalEntityTimelineAccepted':True,'originalNoAiAndControllerCancelAllowCapturedTimelineToContinue':True,'twoEntryQueueStableAndExactlyOnce':True,'variantAndGiantInvalidationNoStaleReplayAfterReenable':True,'removedTargetEligibleFallbackAndNoEligibleDamageControl':True,'publicDimensionTransferAndOwnerDiscardNoStaleReplay':True,'allReplayTimesExactlyCaptureTimePlus84':True,'noSyntheticTickRestoreReplayOrHookInvocation':True,'privateWeakMapCleanupForDiscardOrDeathClaimed':False,'deathPersistenceAuthenticatedClientAndFullBattleAcceptance':False,'weaponsPaused':True,'stage13Included':False,'fullBossGoalStatus':'active','processAudit':process_audit}
save(OUT/'acceptance.json',acceptance)
save(OUT/'terminal-audit.json',{'processAudit':processes(),'targetSha256':sha(deployed['target']),'forgeSha256':sha(forge),'fullBossGoalStatus':'active','weaponsPaused':True})
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
creation['create'](OUT/'RESTORE.md',chr(10).join(['# Stage24 natural phantom timeline supplemental acceptance','','Exact already deployed Stage24 Boss JAR SHA256 '+expected+'. No runtime code changed or replacement deployment required. This archive adds scoped native acceptance and does not supersede the Stage24 release/rollback archives.','Twelve real admitted Boss cases,2896checks,1207natural ServerTick.Post callbacks;13actual seeded captures,18original echo/shape light payload codec roundtrips,8actual quarter-multiplier helper damage calls. Replay due exactly actual capture time+84;15tick tail excludes duplicates. Original and deployed public pure queue/delay rules each pass26controls.','Normal/impossible Cross/Cage;original NoAI/controller-cancel continuation;two queued entries;variant/giant invalidation and reenable;removed target eligible fallback/no-eligible control;owner discard and real public dimension transfer. Public controller/RNG,NoAI,stone platform,health/hurt reset fixtures. No direct Entity.tick,PhantomHooks invocation,restore/replay,reflection,FakePlayer or authentication fabrication.','No natural full Boss AI/battle,client network delivery/rendering,authenticated/multiplayer,persistence/death or private WeakHashMap cleanup claim. Stage13 excluded.Weapons remain paused;recovery-only addon noninstallable.Full Boss goal active.','Archive includes actual fresh world,complete stop/save logs,terminal,input hashes,observer sources/classes,native common dependencies,original/current primary phantom audit and pure proof. External read-only input references are pinned in external-read-only-inputs.json.','Formal deployment/rollback remain in releases/boss-core-stage24-manor-body-chain-20261113;Forge and historical D untouched.']))
selected.update(path for path in OUT.rglob('*') if path.is_file())
result=archive(WORK/'archives/boss-stage24-natural-phantom-timeline-20261113.zip',selected)
save(OUT/'archive-receipt.json',result)
assert sha(deployed['target'])==expected
print(json.dumps({'acceptance':str(OUT/'acceptance.json'),'archive':result,'fullBossGoalStatus':'active'}),flush=True)
