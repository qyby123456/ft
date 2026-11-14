import hashlib
import json
import sys
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
OUT=WORK/'reports/manor-original-entry-closeout-20261114.json'
def load(path):return json.loads(Path(path).read_text(encoding='utf-8-sig'))
def sha(path):
    with Path(path).open('rb') as stream:return hashlib.file_digest(stream,'sha256').hexdigest()
owner=load(ROOT/'WRITE_OWNER.json');assert owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f'
assert not owner['activeRuntimePids'] and owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited']
assert not OUT.exists()
loaded_path=WORK/'reports/manor-original-entry-loaded-audit-v2-20261114/receipt.json';loaded=load(loaded_path);assert loaded['originalRaw500EntryDivergenceCorrectedAndNativeVerified'] and loaded['originalNegativeGuardsNativeVerified']
assert all(Path(path).is_file() and sha(path)==value for path,value in loaded['inputHashes'].items())
base_path=Path(loaded['nativeBaselineReport']);fixed_path=Path(loaded['nativeFixedReport']);baseline=load(base_path);fixed=load(fixed_path)
build_path=WORK/'build/boss-manor-original-entry-v2-20261114/candidate-build-receipt.json';build=load(build_path);candidate=Path(build['jar']);assert sha(candidate)==build['jarSha256']=='a49f1772a60d72a4240eb3f293b29fb38629a2112ad326120e81377eeeeca1ec'
dedup_path=WORK/'reports/manor-original-entry-closed-runtime-dedup-v1-20261114/receipt.json';dedup=load(dedup_path);assert dedup['allFrozenRuntimeInputsUnchanged'] and dedup['allSelectedEvidenceWorldsRetained']
formal={str(Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar')):'6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0',str(Path('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar')):'7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3'}
assert all(sha(Path(path))==value for path,value in formal.items())
remaining=['Full original managed Manor sharing and Youzi mini range/cancel/modifier/lethal cases through legitimate fight ingress','Authenticated original summon and managed full Boss normal/impossible phases, attacks, rewards, death and cancellation','Stage03 actual authenticated positive-kill material/reward integration','Multiplayer and original action/visual fidelity outside scoped existing tests','Dimensions, persistence, restart and attended Curios new-JVM gate; no automatic V18 retry','Final cumulative installable artifact and explicit formal deployment authorization, rollback/archive','Weapon standalone JAR remains paused, recovery-only and NOT-INSTALLABLE']
result={'scope':'Stage32 one original public hurt entry upper-bound correction; paired actual offline survival attacks and separate public API negative controls','candidate':str(candidate),'candidateJarSha256':sha(candidate),'buildReceipt':str(build_path),'baselineReport':str(base_path),'fixedReport':str(fixed_path),'loadedDefinitionAudit':str(loaded_path),'baselineChecks':baseline['probe']['checks'],'fixedChecks':fixed['probe']['checks'],'pairedCases':15,'actualPlayerAttackModesPerRun':8,'publicApiNegativeModesPerRun':7,'structuralProof':build['compositionProof'],'originalCompleteMethodsProof':loaded['originalSixMethodProof'].splitlines()[-1],'actualDefinedMethodsPreserved':342,'unchangedOtherActualMethods':341,'onlyChangedJarEntry':build['changedJarEntries'],'unchangedOtherJarEntries':build['unchangedOtherJarEntries'],'extraPublicHurtRaw500RejectionFixed':True,'actualOver500PositiveBoundaryAndCancelAccepted':True,'allSevenNegativeProtectionControlsAccepted':True,'previousNativeMatrixAndSparrowScopeRetainedByUnchangedMethodsAndActualHelpers':True,'previousMatrixAndSparrowRuntimeNotRerunOrClaimedNew':True,'originalManagedSharingAccepted':False,'authenticated':False,'livingEntityCrossJvmEqualityClaimed':False,'formalExpectedHashesVerified':formal,'closedRuntimeLocalDedup':str(dedup_path),'modelAndMusicResourcesExcludedFromRemoteArchive':True,'rawJarAndWorldsRemainLocalOnly':True,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'remaining':remaining}
OUT.write_text(json.dumps(result,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
helper=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py';namespace={'__file__':str(helper),'__name__':'replace_only'};exec(compile(helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],str(helper),'exec'),namespace)
intro=f"""## Stage32 原版 hurt 入口上限修正与真实边界，2026-11-14

仅改变 Stage31 原 JAR 的 EasterBossEntity.class 一项，移除 public hurt 多加的 raw >500 拒绝五条指令并恢复原版正数分支；有限正数、来源、无敌等原始入口图完整保留，其他原版上限未动。Stage32 NOT-INSTALLABLE 候选 SHA256 {sha(candidate)}。2624 项结构证明与完整六个原方法 32 项对照通过；实际加载的 342 个方法全部与证明组合相同，除 hurt 外 341 个方法不变。两个独立 JVM 各实际捕获 13 类，11 个其他 Boss 实际定义相同，8 个助手与候选完全相同；不声称 LivingEntity 跨 JVM 相等。

同一观察器、独立新世界各 15 场景：旧版 {baseline['probe']['checks']} 项检查复现 500.5、501、1024 及 600 被入口拒绝；修正版 {fixed['probe']['checks']} 项通过，真实生存玩家空手满蓄力的 500.5、501、1024 均触发原生阶段并实际扣血，600 的 Incoming 取消无扣血。499.5/500、首尾 7 正控及七个单独 public API 非法值/非玩家来源/无敌负控通过。只临时校准玩家公开攻击属性基值，原增益和修饰未清除；正例未用直接 hurt 替代 ServerPlayer.attack。两次完整保存/退出 0、进程回收、冻结输入不变。边界范围未自然加载 MaqueSupportHooks，未为凑数强制加载；首轮实际定义审查错误要求全部 14 类的严格失败保留，V2 精确核对 13 类和唯一未加载项。V1 逆向证明锚点过宽的构建失败单独保存；V2 收窄精确有限值序列，不改产品变换。

本段仅限定原始入口修正，并非认证完整 Boss 或完整管理分摊验收；Stage31 原生上下文和麻雀历史回归由未变方法与实际助手绑定保留，未伪称全部矩阵重跑。详见 work/session-boss/MANOR_ORIGINAL_ENTRY_BOUNDARY_20261114.md 与 reports/manor-original-entry-closeout-20261114.json。后续优先合法战斗入口的管理分摊/小柚子/取消/致死，再推进完整认证、多人与持久化。两个正式实例哈希不变；NOT-INSTALLABLE、不部署，D/正式实例只读、武器与 Session 暂停，Curios 不自动重试。模型/贴图/动画/音乐/音频及其嵌套内容禁止上传；JAR、世界与排除资源只保留本地，远端只含允许源码和文字证据。

"""
for relative in ['AGENTS.md','work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md']:
    path=ROOT/relative;before=path.read_text(encoding='utf-8');assert not before.startswith('## Stage32 原版 hurt');namespace['replace_file'](path,before,intro+before)
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path);assert not state['activeRuntimePids'];state.update({'currentWorkstream':'boss_stage32_original_public_hurt_guard_scoped_accepted_source_archive_pending','latestScopedBossCandidate':str(candidate),'latestScopedBossCandidateSha256':sha(candidate),'latestScopedBossCandidateBuildReceipt':str(build_path),'latestOriginalEntryGuardCloseout':str(OUT),'latestOriginalEntryLoadedAudit':str(loaded_path),'extraPublicHurtRaw500RejectionFixed':True,'raw500NativeBoundaryScopedAccepted':True,'fullManagedManorSharingNativeAccepted':False,'fullBossMigrationComplete':False,'latestScopedBossCandidateInstallable':False,'latestScopedBossCandidateDeployed':False});path.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
print(json.dumps({'closeout':str(OUT),'candidateSha256':sha(candidate),'baselineChecks':baseline['probe']['checks'],'fixedChecks':fixed['probe']['checks'],'formalHashesUnchanged':True}),flush=True)
