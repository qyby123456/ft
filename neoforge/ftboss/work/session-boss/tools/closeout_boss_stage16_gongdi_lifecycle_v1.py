import datetime
import json
from pathlib import Path

ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
RELEASE=WORK/'releases/boss-core-stage16-gongdi-lifecycle-20261113'
helper=WORK/'tools/finalize_boss_stage14_resources_v4.py'
functions={'__file__':str(helper)}
exec(helper.read_text(encoding='utf-8').split('parser = argparse.ArgumentParser()')[0],functions)
load=functions['load'];sha=functions['sha'];save=functions['save'];archive=functions['archive'];processes=functions['processes']
process_audit=processes()
deployed=load(RELEASE/'deployment-receipt.json');archived=load(RELEASE/'archive-receipt.json');feature=load(RELEASE/'feature-verification.json')
assert sha(deployed['target'])==deployed['deployedSha256']=='cb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d'
assert sha(deployed['backup'])==deployed['backupSha256']=='92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4'
assert sha(archived['archive'])==archived['archiveSha256'] and archived['files']==1263 and archived['failures']==0
assert feature['baselineCases']==35 and feature['candidateCases']==35
patcher_path=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py';patcher={'__file__':str(patcher_path)}
exec(patcher_path.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],patcher)
patch=patcher['replace_file']
next_action='Audit original Gongdi target support,skill damage/cadence and full native living-target paths against primary instructions;no class-name inference. Authenticated/reward/multiplayer/full-battle gates pending;weapons paused.'
for name in ['WRITE_OWNER.json','work/session-boss/CURRENT_BOSS_BATCH.json']:
    path=ROOT/name;before=path.read_text(encoding='utf-8');data=json.loads(before)
    data.update({'currentGoalStatus':'active','activeGoalObjective':'继续完成boss部分部署移植','currentSignedSessionGoalStatus':'paused_by_current_direct_user','weaponValidationPaused':True,'noNewWeaponRuns':True,'activeRuntimePid':None,'activeRuntimePids':[],'bossActiveRuntimePid':None,'activeSupervisor':None,'ownedJavaPythonProcessesRemaining':False,'ownedJavaPythonRemaining':0,'status':'stage16_verified_deployed_archived_full_boss_active','currentBatch':'stage16 complete:original Gongdi lifecycle5entry guards;baseline35/candidate35;310ASMverified,305unrelated methods preserved;all other entries unchanged','latestFormalDeployment':'stage16 Gongdi lifecycle on stage15;stage13 excluded','latestBossDeploymentReceipt':str(RELEASE/'deployment-receipt.json'),'latestStageDeploymentReceipt':str(RELEASE/'deployment-receipt.json'),'latestBossArchiveReceipt':str(RELEASE/'archive-receipt.json'),'latestStageArchiveSha256':archived['archiveSha256'],'deployedSha256':deployed['deployedSha256'],'stage16DeploymentPerformed':True,'stage16ArchiveFiles':1263,'stage16CandidateCases':35,'stage16BaselineCases':35,'stage13DeploymentPerformed':False,'stage13IncludedInCurrentDeployment':False,'fullBossInstallable':False,'fullBossMigrationComplete':False,'installable':False,'nextBossAction':next_action,'nextBossScope':next_action})
    patch(path,before,json.dumps(data,ensure_ascii=False,indent=2)+chr(10))
heading='## Latest deployed stage16 Gongdi lifecycle, 2026-11-13\n\n- Deployed SHAcb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d. Original helper and five entry/reset guards restored. Existing instructions preserved in modified methods;305 unrelated methods identical,310 methods ASM verified. Native baseline35/candidate35 verify correct eligible release,wrong variant/mini/noAI/dead start refusal,idle finish,interrupted cancel/reset and foreign-level/null-target controls. No manual Entity.tick,private mutation or authenticated player/session.\n- Stage16 archive1263files SHA78f2ac0637cf9ffcb60fbf4f180e5d350d2e330d30463c548603b49e2f678745 fully readback/SHA verified. Exact stage15 rollback preserved;stages14/15 remain included. Stage13 excluded. Prior failed fixture preserved.\n- Full Boss goal active:original general Living-target/skill parity,authenticated entry/rewards/multiplayer and full battles pending. Weapons paused,recovery JAR noninstallable,same sole C writer. Forge/historical D read-only. Next actual primary Gongdi target/skill audit before native gap work.\n\n'
path=ROOT/'AGENTS.md';before=path.read_text(encoding='utf-8');assert '## Latest deployed stage16 Gongdi lifecycle' not in before
patch(path,before,before.replace('# NeoForge continuation on C'+chr(10),'# NeoForge continuation on C'+chr(10)*2+heading,1))
paragraph='\n\n## 2026-11-13 Stage16: original Gongdi lifecycle\n\nRestores original helper and five cast entry/reset guards only. All old instructions preserved in modified methods;305 unrelated methods preserved,310 ASM verified. Native baseline35/candidate35 checks original eligible start/release,invalid variant/mini/noAI/dead rejection,idle impact refusal,interrupted cancel/reset,foreign-level no-mutation and invalid tryStart return controls. Tests use real public entity/controller methods,not manual Entity.tick/reflection/authenticated players.\n\nDeployed SHA256 cb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d;exact stage15 rollback92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4. Archive1263files SHA78f2ac0637cf9ffcb60fbf4f180e5d350d2e330d30463c548603b49e2f678745 readback/SHA verified. Full target/damage/cadence and authenticated/full battle remain pending. Stage13 excluded,weapons paused;full goal active.\n'
for name in ['DEPLOYED_PROGRESS.md','BOSS_MIGRATION_REQUIREMENTS_20261113.md']:
    path=WORK/name;before=path.read_text(encoding='utf-8');patch(path,before,before+paragraph)
creator=WORK/'tools/prepare_boss_blood_tide_stage10.py';creation={'__file__':str(creator)};exec(creator.read_text(encoding='utf-8').split('probe=(WORK/')[0],creation)
status_path=WORK/'BOSS_DEPLOYMENT_STATUS_20261113.md'
before=status_path.read_text(encoding='utf-8')
patch(status_path,before,before+'\n\n## 2026-11-13 新增 Stage16：工地施法生命周期\n\n- 已部署原版五处施法/重置守卫：错误变种、小型、禁AI、死亡状态不能开施法；无有效施法时不能空放冲击，中断或重置会清理未释放提示，跨世界调用不修改原Boss。\n- 基线35项、候选35项原生检查通过；310个方法ASM验证通过，305个无关方法和修改方法原主体保持一致。\n- 当前正式JAR SHA256：`cb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d`。\n- 归档：`work/session-boss/archives/boss-core-stage16-gongdi-lifecycle-20261113.zip`，1263个文件逐项读回/SHA256校验。回滚包和部署凭据位于`work/session-boss/releases/boss-core-stage16-gongdi-lifecycle-20261113/`。\n- 下一段核对工地原版目标类型、伤害和技能节奏；Boss整体尚未全部验收，真实登录、奖励、多人和完整战斗仍待继续。武器开发保持暂停。\n')
save(RELEASE/'closeout-terminal-audit.json',{'processes':process_audit,'ownedRuntimeOrSupervisorRemaining':0,'targetSha256':sha(deployed['target']),'backupSha256':sha(deployed['backup']),'fullBossGoalStatus':'active','weaponsPaused':True,'next':next_action})
files={Path(__file__),ROOT/'AGENTS.md',ROOT/'WRITE_OWNER.json',ROOT/'WRITE_OWNER.claim',WORK/'CURRENT_BOSS_BATCH.json',WORK/'DEPLOYED_PROGRESS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',WORK/'BOSS_DEPLOYMENT_STATUS_20261113.md',RELEASE/'deployment-receipt.json',RELEASE/'archive-receipt.json',RELEASE/'feature-verification.json',RELEASE/'terminal-audit.json',RELEASE/'RESTORE.md',RELEASE/'closeout-terminal-audit.json'}
receipt=archive(WORK/'archives/boss-core-stage16-closeout-20261113.zip',files);save(RELEASE/'closeout-archive-receipt.json',receipt)
assert sha(deployed['target'])==deployed['deployedSha256']
print(json.dumps({'stage16Closed':True,'closeoutArchive':receipt,'fullBossGoalStatus':'active','weaponWorkPaused':True}),flush=True)
