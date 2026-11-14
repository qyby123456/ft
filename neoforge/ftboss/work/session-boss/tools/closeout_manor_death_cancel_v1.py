import hashlib,json,sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3];WORK=ROOT/'work/session-boss'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
h=WORK/'tools/prepare_boss_blood_tide_stage10.py';create={'__file__':str(h),'__name__':'create_only'};exec(compile(h.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(h),'exec'),create)
h=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py';edit={'__file__':str(h),'__name__':'replace_only'};exec(compile(h.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],str(h),'exec'),edit)
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
loaded_path=WORK/'reports/manor-death-cancel-actual-loaded-audit-v1-20261114/receipt.json';loaded=load(loaded_path);native_path=Path(loaded['nativeReport']);native=load(native_path);probe=native['probe'];assert loaded['actualNativeCanceledDeathSurvivalAndNextStageAccepted'] and loaded['actualNativeCataclysmCursiumGhostRebirthObserved'];assert 'allOriginalGraphsRecoveredExactly=true' in loaded['actualBossProof'] and 'negativeControls=2' in loaded['originalCallbackProof']
build_path=WORK/'build/boss-manor-death-cancel-v4-20261114/candidate-build-receipt.json';built=load(build_path);candidate=Path(built['jar']);assert sha(candidate)==loaded['candidateJarSha256']==built['jarSha256'];composition=load(build_path.with_name('composition-proof.json'));assert composition['checks']==2532 and composition['nativeFinalDeathGates']==3 and composition['negativeControls']==4
boundary_path=WORK/'build/boss-manor-death-cancel-boundary-v2-20261114/boundary-proof.json';boundary=load(boundary_path);assert boundary['checks']==49 and boundary['negativeControls']==2 and boundary['bothInstalledVersionsHaveSameRebirthContract'] and boundary['nativeDeadFlagFollowsUncanceledDeathEvent']
prior=load(WORK/'reports/shadow-sigil-render-closeout-20261114.json');formal=prior['formalExpectedHashesVerified']
for path,digest in formal.items():assert sha(path)==digest
for evidence in [loaded,native]:
    for path,digest in evidence['inputHashes'].items():assert Path(path).is_file() and sha(path)==digest
remaining=[item for item in prior['remaining'] if not item.startswith('Resolve actual native managed Gongdi death cancellation')]
receipt={'stage':41,'candidate':str(candidate),'candidateJarSha256':sha(candidate),'productChanged':True,'nativeReport':str(native_path),'loadedAudit':str(loaded_path),'boundaryProof':str(boundary_path),'compositionChecks':composition['checks'],'compositionNegativeControls':composition['negativeControls'],'actualDefinedBossMethods':342,'changedExistingBossMethods':2,'unchangedOtherBossMethodGraphs':340,'nativeFinalDeathGates':3,'allOriginalBossGraphsRecoveredExactly':True,'allPriorNativeInstallersAndOrderRetained':True,'originalDamageRebirthAndRewardAlgorithmsRetained':True,'noEngineOrThirdPartyProductClassChanges':True,'allOtherExistingJarEntriesAndResourcesIdentical':True,'unchangedResourceEntries':1530,'nativeChecks':probe['checks'],'actualNativeCanceledDeathSurvivalAndNextStageAccepted':True,'actualNativeCataclysmCursiumGhostRebirthObserved':True,'actualNativeDeathBoundary':probe['deathBoundary'],'actualBossProof':loaded['actualBossProof'],'originalCallbackProof':loaded['originalCallbackProof'],'actualNormalVictoryEarnedUnlock':loaded['normalVictoryEarned'],'actualEligibleFinaleAttributeBaselineAccepted':loaded['eligibleFinaleAttributeBaselineAccepted'],'fullProbeFailures':probe['failures'],'fullProbeFailure':probe.get('failure'),'normalFightNativeDeathCancellationRemovalGapRemains':False,'originalGapAlsoPresentInInstalledForgeScript':True,'nativeGenericKillPathAccepted':False,'arbitraryCancelWithoutHealthRestorationSurvivalClaimed':False,'fixture':probe['playerFixtureProtection'],'observerNoPrivateGameplayManualTickOrVictoryIdentityWrites':True,'formalExpectedHashesVerified':formal,'modelOrMusicUploaded':False,'authenticated':False,'actualMultiplayerGameplayAccepted':False,'curiosReloadAcceptanceClaimed':False,'allBossWorldRestartAcceptanceClaimed':False,'fullBossCombatAcceptanceClaimed':False,'remaining':remaining,'fullBossMigrationComplete':False,'installable':False,'deployed':False}
output=WORK/'reports/manor-death-cancel-closeout-20261114.json';create['create'](output,json.dumps(receipt,ensure_ascii=False,indent=2)+'\n')
intro=f'''## Stage41 原生死亡取消与原脚本移除边界，2026-11-14

候选 {receipt['candidateJarSha256']}，NOT-INSTALLABLE。对原有效 Easter 的 manorHurt 两个 die/discard 路径及 die 的 super 返回点共加三个原生 dead 最终提交标志判断，仅改变两方法、其余 340 图精确保持；原伤害、重生和奖励算法不变，不修改引擎或 Cataclysm。2532 组合检查、四负控、342 方法 ASM；实际类反向还原全部 342 原图，原回调两负控通过。1530 资源条目及其他既有 JAR entry 字节不变。

原 Forge/当前 NeoForge 的 Cataclysm 均有胸甲重生取消契约，该脚本缺口在原 Forge 也存在。实际离线客户端/集成服使用原注册宫地蛋、真实玩家公开攻击：原胸甲提供者取消致死，恢复 5 和 ghost_form；40/140 自然 tick 同一原 Boss、owner/fight 保留；冷却后原生未取消死亡正常提交，原状态机自然换阶段。原生边界验收通过。后续完整探针失败数 {probe['failures']}；完整普通胜利={loaded['normalVictoryEarned']}，合法终结属性基线={loaded['eligibleFinaleAttributeBaselineAccepted']}。更广范围与本段边界分开记录，失败原样保留；未写胜利/解锁/身份，不私调玩法、不手动 tick。

玩家 Pre 0.01/生命 1000、公开基础攻击校准 128、Boss Pre 250 只是隔离观察夹具，不代表生存难度或认证实战。GENERIC_KILL 路径只有静态/ASM 同构证明，未冒充原生运行正例；任意取消但不恢复生命的存活未声称通过。完整需求清单继续保留，武器和 Session 暂停，Curios V18 有人值守门槛不自动重试，正式实例及 D 只读、不部署。

全部模型/贴图/动画/音乐/音频、JAR、世界、二进制及嵌套/编码资源不上传。Git 仅源码/配置/文档/限定文字证据及路径/大小/哈希元数据，不能恢复排除资源；独立远端逐 blob 恢复后才清理合格重复 Git 源码工作副本。详见 work/session-boss/reports/manor-death-cancel-closeout-20261114.json。

'''
create['create'](WORK/'MANOR_DEATH_CANCEL_20261114.md',intro)
for path in [ROOT/'AGENTS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md']:
    before=path.read_text(encoding='utf-8');assert before.startswith('## Stage40');edit['replace_file'](path,before,intro+before)
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    before=path.read_text(encoding='utf-8');state=json.loads(before);state.update({'currentWorkstream':'boss_stage41_native_death_cancel_accepted_pending_source_archive','latestScopedBossCandidate':str(candidate),'latestScopedBossCandidateSha256':sha(candidate),'latestScopedBossCandidateBuildReceipt':str(build_path),'latestScopedBossCandidateAcceptanceReceipt':str(output),'latestManorDeathCancelCloseout':str(output),'actualNativeCanceledDeathSurvivalAndNextStageAccepted':True,'managedDeathCancellationRemovalGapRemains':False,'actualEarnedNormalVictoryAccepted':loaded['normalVictoryEarned'],'actualEligibleFinaleAttributeBaselineAccepted':loaded['eligibleFinaleAttributeBaselineAccepted'],'fullBossMigrationComplete':False,'latestScopedBossCandidateInstallable':False,'latestScopedBossCandidateDeployed':False});edit['replace_file'](path,before,json.dumps(state,ensure_ascii=False,indent=2)+'\n')
files=[ROOT/'AGENTS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md',WORK/'MANOR_DEATH_CANCEL_20261114.md',output]
for pattern in ['tests/ManorDeathCancel*.java','tests/LoadedBossClassCaptureV11.java','src/manor-death-cancel-v1/**/*.java','tools/*manor_death_cancel*.py','tools/finish_manor_death_cancel_archive_v1.py']:
    files.extend(WORK.glob(pattern))
for folder in [*WORK.glob('build/boss-manor-death-cancel-*20261114'),WORK/'reports/manor-death-cancel-actual-loaded-audit-v1-20261114']:
    files.extend(path for path in folder.rglob('*') if path.is_file() and path.suffix in ['.json','.txt'])
files.extend([native_path,Path(native['attempt'])/'terminal.json',Path(native['attempt'])/'raw-jvm-terminal.json'])
files.extend(WORK.glob('reports/manor-death-cancel-*preflight*20261114.json'))
if len(sys.argv)>1:
    dedup_path=Path(sys.argv[1]).resolve();dedup=load(dedup_path);assert dedup['allFrozenRuntimeInputsUnchanged'] and dedup['sourceAndFormalInstancesUnchanged'];files.extend(path for path in dedup_path.parent.rglob('*') if path.is_file() and path.suffix in ['.json','.txt'])
selected=[];excluded=[]
for path in sorted(set(files)):
    assert path.is_file() and path.resolve().is_relative_to(ROOT.resolve())
    try:data=path.read_bytes();assert b'\0' not in data;data.decode('utf-8-sig')
    except (UnicodeError,AssertionError):excluded.append({'path':path.relative_to(ROOT).as_posix(),'bytes':path.stat().st_size,'sha256':sha(path),'contentUploaded':False});continue
    selected.append(path.relative_to(ROOT).as_posix())
acceptance={key:receipt[key] for key in ['candidateJarSha256','productChanged','nativeChecks','compositionChecks','compositionNegativeControls','actualNativeCanceledDeathSurvivalAndNextStageAccepted','actualNativeCataclysmCursiumGhostRebirthObserved','actualNormalVictoryEarnedUnlock','actualEligibleFinaleAttributeBaselineAccepted','fullProbeFailures','modelOrMusicUploaded','fullBossMigrationComplete','installable','deployed']}
config={'key':'manor-death-cancel-20261114','output':'work/session-boss/reports/manor-death-cancel-source-checkpoint-v1-20261114','files':selected,'scope':'Stage41 minimal original-script/native canceled-death lifecycle adapter; three final-death gates in two original methods; actual Cataclysm chestplate rebirth retained and natural next stage; broader failed probe and excluded local resources preserved','acceptance':acceptance,'remaining':remaining,'message':'Respect native canceled death in original Manor lifecycle and verify actual rebirth','pruneDuplicateSources':[path for path in selected if Path(path).suffix in ['.py','.java']],'excludedNonUtf8MetadataOnly':excluded}
create['create'](WORK/'reports/manor-death-cancel-source-checkpoint-configuration-v1-20261114.json',json.dumps(config,ensure_ascii=False,indent=2)+'\n');print(json.dumps({'stage':41,'nativeChecks':probe['checks'],'files':len(selected),'fullProbeFailures':probe['failures'],'normalVictory':loaded['normalVictoryEarned'],'deployed':False}),flush=True)
