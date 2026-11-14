import hashlib
import json
import zipfile
from pathlib import Path
ROOT=Path(__file__).resolve().parents[3]
WORK=ROOT/'work/session-boss'
checks=0
def load(path):return json.loads(path.read_text(encoding='utf-8-sig'))
def sha(path):return hashlib.sha256(path.read_bytes()).hexdigest()
def require(test,label):
    global checks
    checks+=1
    assert test,label
def save(path,value):
    assert not path.exists(),path
    path.write_text(json.dumps(value,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
owner=load(ROOT/'WRITE_OWNER.json');require(owner['activeWriterThreadId']==load(ROOT/'WRITE_OWNER.claim')['threadId']=='01a1c43e-ae80-7390-b604-b43b4ab53f2f' and not owner['activeRuntimePids'],'Sole owner and terminal native processes')
require(owner['weaponValidationPaused'] and owner['noFurtherFormalWritesOrDeployment'] and owner['modelResourceUploadProhibited'] and owner['musicUploadProhibited'],'Pause, formal-write and resource boundaries')
helper=WORK/'tools/prepare_boss_blood_tide_stage10.py';namespace={'__file__':str(helper),'__name__':'create_only'};exec(compile(helper.read_text(encoding='utf-8').split('probe=(WORK/')[0],str(helper),'exec'),namespace);create=namespace['create']
helper=ROOT/'work/session-lease/tools/reactivate_goal_after_boss_stage12.py';namespace={'__file__':str(helper),'__name__':'replace_only'};exec(compile(helper.read_text(encoding='utf-8').split('assert not OUT.exists()')[0],str(helper),'exec'),namespace);replace_file=namespace['replace_file']
report_path=WORK/'reports/manor-unmanaged-damage-routing-native-v2-5a21af33.json';runtime=load(report_path);probe=runtime['probe'];terminal=runtime['terminal']
require(probe['failures']==0 and probe['checks']==880 and probe['completedModes']==len(probe['requiredModes'])==6,'All six diagnostic cases and original strict assertions completed')
require(probe['actualUnmanagedBossDamageRoutingDiagnosticCompleted'] and probe['actualSameListenerBossAndVanillaPositiveControlsAccepted'],'Both explicit probe-level diagnostic controls passed')
require(terminal['exitCode']==0 and terminal['clientProcessReaped'] and terminal['integratedWorldCompleteSave'] and terminal['inputHashesUnchanged'] and terminal['finalDefinedClassesCaptured'],'Complete save, exit, reap and frozen inputs')
require(runtime['actualUnmanagedBossDamageRoutingDiagnosticCompleted'] is False and 'allScenarioCasesAccepted' not in probe,'Frozen outer runner retains inherited inapplicable matrix flag; no evidence rewrite')
runner=WORK/'tools/run_boss_manor_unmanaged_damage_routing_native_v2.py';require("probe.get('allScenarioCasesAccepted')" in runner.read_text(encoding='utf-8'),'Exact outer-label cause identified')
cases={case['mode']:case for case in probe['cases']};cow=cases['vanilla-cow-control'];sparrow=cases['sparrow-control'];solo=cases['variant-one-ai'];shared=cases['variant-one-with-sparrow'];managed=cases['variant-one-noai']
for case in [cow,sparrow,solo,shared]:
    require(case['beforeHealth']>case['afterHealth'],'Actual positive health loss '+case['mode'])
    require(all(case['counts'].get('target.'+phase)==1 for phase in ['incoming','pre','post']),'One real native phase chain '+case['mode'])
    require(abs(case['beforeHealth']-case['afterHealth']-case['postAmount'])<0.00025,'Native Post matches nonlethal actual primary health loss '+case['mode'])
require(not solo['beforeGuards']['managed'] and not shared['beforeGuards']['managed'],'Actual unmanaged original route, independent of fixture labels')
require(solo['beforeGuards']['playerAttackDamage']==shared['beforeGuards']['playerAttackDamage']==1.0 and solo['beforeGuards']['playerAttackStrength']==shared['beforeGuards']['playerAttackStrength']==1.0,'Same actual full-strength one-point player input for share control')
require(shared['supportBeforeHealth']==1024.0 and shared['supportAfterHealth']==1023.5 and not shared['supportRemoved'],'Original ordinary nearby Sparrow takes exactly one-half raw damage')
require(abs(shared['postAmount']-solo['postAmount']*0.5)<0.000001,'Original unmanaged returned share fraction halves actual primary settlement')
require(shared['counts'].get('target.originalMitigation',0)==0 and shared['counts'].get('target.originalHealth',0)==0,'Unmanaged parent route is distinct from custom Manor settlement')
incoming=[event for event in probe['events'] if event['mode']=='variant-one-with-sparrow' and event['phase']=='target.incoming'];require(len(incoming)==1 and incoming[0]['amount']==0.5 and not incoming[0]['canceled'],'Sharing occurs before actual unmanaged native incoming phase')
require(managed['beforeGuards']['managed'] and not managed['beforeGuards']['noAI'] and managed['beforeGuards']['hasTarget'],'Original manager naturally changed actual role after ordinary admission; labels do not override observations')
static_path=WORK/'reports/manor-original-sharing-mapped-audit-v2-20261114/receipt.json';static=load(static_path);require(static['allFiveOriginalSharingMethodsPreservedAfterThreeProvenStackEquivalences'] and static['provenExistingEquivalentStackSites']==3,'Complete five original helper graphs with explicit equivalent stack proof')
base_actual=WORK/'build/sparrow-native-events-regression-v1-45d4271b/captured-defined-classes/net_farmingtales_ftboss_entity_EasterBossEntity.class';captures=Path(runtime['attempt'])/'captured-defined-classes';actual=captures/base_actual.name;require(sha(actual)==static['actualEasterBossSha256']==sha(base_actual),'Entire actual Boss definition matches five-method original audit')
built_path=WORK/'build/boss-manor-native-events-v3-20261114/candidate-build-receipt.json';built=load(built_path);require(sha(Path(built['jar']))==built['jarSha256']=='e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c','Product candidate unchanged')
selected=[path for path in captures.glob('*.class')];require(len(selected)==14,'All fourteen selected actual definitions captured')
helpers=['boss/ai/NativeManorDamageBridge','boss/ai/NativeManorDamageBridge$Frame','boss/ai/NativeManorDamageScopeAccess','boss/ai/NativeManorDamageBridgeInstaller','event/NativeManorDamageEventFactory','boss/ai/NativeManorFinalDamageInstaller','maque/support/MaqueSupportHooks','youzi/YouziCombatEvents'];binding=[]
with zipfile.ZipFile(built['jar']) as archive:
    for name in helpers:
        name='net/farmingtales/ftboss/'+name;path=captures/(name.replace('/','_')+'.class');require(sha(path)==hashlib.sha256(archive.read(name+'.class')).hexdigest(),'Actual helper equals candidate '+name);binding.append({'class':name,'actualPath':str(path),'actualAndCandidateSha256':sha(path)})
for name in ['manor-original-sharing-native-v1-f0488a69.json','manor-unmanaged-damage-routing-native-v1-1bf75e89.json','manor-unmanaged-damage-routing-native-v2-5a21af33.json']:
    data=load(WORK/'reports'/name);require(data['terminal']['exitCode']==0 and data['terminal']['clientProcessReaped'] and data['terminal']['inputHashesUnchanged'],'Closed evidence '+name)
    for path,expected in data['inputHashes'].items():require(sha(Path(path))==expected,'Frozen evidence input '+path)
for path,expected in static['inputHashes'].items():require(sha(Path(path))==expected,'Frozen original audit input '+path)
formal=[('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_NeoForge 机械动力/mods/qys-bosses-1.21.1.jar','6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0'),('C:/Users/qyby/AppData/Roaming/.minecraft/versions/FarmingTales_ForgeⅡ v1.5.2/mods/qys-bosses.jar','7c88b9e51ccdc6616cd2f70d6fa3c195772fc3f083f848085053d40cfe2015a3')]
for name,expected in formal:require(sha(Path(name))==expected,'Formal instance remains unchanged')
dedup_path=WORK/'reports/manor-sharing-routing-closed-runtime-dedup-v1-20261114/receipt.json';dedup=load(dedup_path);require(dedup['jarPathsPreserved']==84 and dedup['readonlyAliasesCreated']==84 and dedup['allFrozenRuntimeInputsUnchanged'],'All closed runtime copies deduplicated without losing paths or bytes')
folder=dedup_path.parent;save(WORK/'reports/manor-sharing-routing-local-dedup-journal-export-20261114.json',{'operations':[json.loads(line) for line in (folder/'journal.jsonl').read_text(encoding='utf-8-sig').splitlines() if line.strip()],'rawResourcesExported':False})
remaining=['Remove only proven added public hurt raw-500 rejection using Boss-owned code and verify actual boundary attacks; retain original finite/positive/source/protection rules and other original limits','Full managed sharing with actual legitimate fight ingress, Youzi minis, cancellations/modifiers/range/lethal paths; do not reuse invalid hardcoded raw-7 fixture','Authenticated original summon and all Boss normal/impossible attacks, phases, deaths, rewards and cancellation; offline natural adoption is not authentication','Multiplayer, original audiovisual fidelity, dimension and persistence; Curios new JVM requires attended user click and no automatic V18 retry','Installable cumulative artifact, explicit formal-write authorization, final deployment/rollback/archive remain']
closeout={'scope':'Original five-method sharing fidelity and one actual unmanaged nearby-Sparrow split; full managed matrix not accepted','candidateSha256':built['jarSha256'],'productChanged':False,'originalSharingMethodAudit':str(static_path),'originalSharingMethodAuditChecks':30,'fiveCompleteOriginalMethodsVerified':True,'threeExistingEquivalentCategoryOneStackSitesProven':True,'twoNegativeControls':True,'actualDiagnosticRuntime':str(report_path),'actualDiagnosticChecks':880,'actualDiagnosticCases':6,'scopedEvidenceRecheckAssertions':checks,'actualUnmanagedSparrowSplitScopedAccepted':True,'sameOnePointInputSoloAndSharingPrimaryDamage':[solo['postAmount'],shared['postAmount']],'actualSupportDamage':0.5,'naturalManagedAdoptionObservedWithoutFabrication':True,'fixtureLabelsAreNotAuthority':True,'outerFrozenRunnerFalseExplainedByMissingInapplicableMatrixFlag':True,'outerFrozenReportRewritten':False,'fourteenSelectedActualDefinitionsCaptured':True,'actualEasterBossDefinitionMatchesMappedAudit':True,'eightActualHelpersMatchCandidate':binding,'livingEntityCrossJvmEqualityClaimed':False,'strictFailedNativeTrialsPreserved':['manor-original-sharing-native-v1-f0488a69','manor-unmanaged-damage-routing-native-v1-1bf75e89'],'strictOriginalGraphFailuresPreserved':['manor-original-sharing-mapped-audit-v1-20261114','manor-original-routing-mapped-audit-v1-20261114'],'raw500StaticDivergence':str(WORK/'reports/manor-original-routing-static-divergence-20261114.json'),'currentExtraRaw500RejectionNotInOriginalPublicHurt':True,'raw500NativeBoundaryAccepted':False,'raw500DivergenceFixed':False,'fullManagedSharingAccepted':False,'localDedupReceipt':str(dedup_path),'all84RuntimeJarPathsAndEveryFrozenInputRetained':True,'formalInstances':[{'path':name,'sha256':expected,'readOnlyVerified':True} for name,expected in formal],'allModelsTexturesAnimationsMusicAudioAndNestedResourcePayloadsLocalOnly':True,'modelOrMusicUploaded':False,'authenticated':False,'weaponAndSessionDevelopmentAndValidationPaused':True,'fullBossMigrationComplete':False,'installable':False,'deployed':False,'remaining':remaining}
closeout_path=WORK/'reports/manor-sharing-route-audit-closeout-20261114.json';save(closeout_path,closeout)
summary="""## 原版分摊入口审查与额外 500 上限差异，2026-11-14

Stage31 候选 e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c 字节不变。五个完整原版分摊方法 30 项审查通过；柚子三处既有栈写法经精确类别一符号栈等价证明后，完整还原原方法图，并保留错误接收者/分摊比例两个负控。扩大到完整 hurt 入口的严格对照发现额外原始伤害 >500 拒绝，原版该入口无此条件；这是真实静态差异，尚未修复或验证运行时边界，不能跳过该差异宣称原方法完全一致。

新的真实离线六场景诊断 880 项通过，同一监听器的牛和麻雀正伤害对照均通过。一个真实普通未管理目标由麻雀分摊：同为玩家实际满蓄力 1 点输入，单独主目标原生 Post 约 0.04，附近有麻雀时主目标约 0.02，麻雀真实扣血 0.5，分摊后的实际 Incoming 为 0.5。14 个实际定义捕获，EasterBoss 整类与原方法审查相同，8 个实际助手与候选相同；不声称 LivingEntity 跨 JVM 相等。自然管理器实际接管过一名角色，未注入 fight/owner 权限，但仍非认证完整战斗证明。

20 场景分摊尝试和首轮路由诊断均严格失败且保存；不同入口/管理状态及真实玩家增益影响夹具，不能硬编码 raw=7 或混同管理与普通 parent 结算。V2 外层 runner 继承了不存在的 allScenarioCasesAccepted 条件而保留 false；真实探针 6 场景/880 项/两项正控通过，在独立限定收口中逐项重核，不改写冻结回执。三个客户端均完整保存/退出 0、回收，84 个 JAR 路径和全部冻结字节保留，仅以已有只读硬链接去重，无任何资源上传。

下一段先由 Boss 独立代码精确移除该 public hurt 入口多加的 500 拒绝，并做真实玩家边界验证；保留原版有限正数/来源/无敌等保护及其他原版上限。完整管理分摊/小柚子/取消/致死、多人与认证完整 Boss、持久化仍未完成。详见 work/session-boss/MANOR_SHARING_ROUTE_AUDIT_20261114.md 与 reports/manor-sharing-route-audit-closeout-20261114.json。模型/贴图/动画/音乐/音频及嵌套资源禁止上传；NOT-INSTALLABLE，不部署，正式实例/D 只读、武器/Session 暂停，Curios 不自动重试有人值守页面。

"""
for path in [ROOT/'AGENTS.md',WORK/'BOSS_MIGRATION_REQUIREMENTS_20261113.md']:
    before=path.read_text(encoding='utf-8');assert not before.startswith('## 原版分摊入口审查与额外 500 上限差异');replace_file(path,before,summary+before)
doc="""# Original Manor sharing routes and raw-damage guard audit

The Stage31 candidate remains byte-identical (SHA256 e63617ebd89d5e78055fef649db3392304a678abcafa4d74996a025d93f79c6c). This increment adds original-code evidence, actual scoped runtime evidence and a newly isolated fidelity defect. No product JAR or formal instance is changed; the full Boss goal remains active.

## Exact original sharing and two different routes

Five entire original methods are compared against the actually final-defined EasterBoss: ftboss$shareManorDamage, sparrowShare, youziMinionShare, nearbySparrow and blocksCombatDamage. Four are already exactly equal after 20 primary official member mappings. Youzi contains three pre-existing equivalent stack forms: the original DUP2/POP/owner0/SWAP and the current owner0/recipient8 reloads both preserve [recipient8, floatHealth, owner0, recipient8]. The proof checks each exact receiver, value type/local, call descriptor and next health write, reconstructs only those three sites, and then compares the complete original method graph. Five actual methods pass ASM, sharing order is preserved, and wrong-recipient and changed-ratio controls are rejected. Thirty assertions pass. The first strict unequal-graph failure and all exact instruction differences remain frozen.

The original managed Manor settlement calls ftboss$shareManorDamage with the capped pre-mitigation amount and discards its return. Primary health follows the original mutable final amount; helper loss is a side effect. The ordinary unmanaged parent route instead uses sparrowShare's returned fraction before ManorDefense.reduceAfterSharing and Monster.hurt. These are different original contracts. Do not substitute one for the other or manually call a settlement helper to claim real player coverage.

## Actual six-case diagnostic and one scoped sharing success

In manor-unmanaged-damage-routing-native-v2-5a21af33, a real survival ServerPlayer attacks through the actual public attack path, after 40 natural primary ticks per case. All six cases and 880 assertions pass; the actual vanilla cow and Sparrow positive controls both produce real Incoming/Pre/Post and positive HP loss. Player damage attribute, attack strength, mode/effects and actual managed/no-AI/target/intro state are recorded. No effects are cleared, fight IDs or authentication fabricated, private gameplay callbacks invoked, or entity ticks manually run.

A positive unmanaged variant-one control has one point of full-strength actual attack, native final damage about 0.04, and no custom Manor phases. With an actual ordinary nearby Sparrow, the same one-point input becomes native Incoming 0.5 and primary Post about 0.02, while Sparrow HP changes 1024 to 1023.5. This is the original unmanaged half-share route; the exact HP deltas and native amounts are independently checked in the closeout with float tolerance. It is one nonlethal unmodified ordinary branch, not the complete managed/minion/cancel/lethal matrix.

One role is naturally adopted by the original manager between public ordinary admission and attack; its actual snapshot is managed=true/noAI=false/target=true. The mode label variant-one-noai is not the actual state authority. This proves natural adoption was observed without fabricated fight tags; authentication and complete battle are still unproven. Actor and player state changes also mean raw incoming must be observed, not assumed to be seven for every case.

The unchanged V2 outer runner preserves a false flag because it inherited allScenarioCasesAccepted from another matrix, while this diagnostic uses its own explicit completion/positive-control fields. The frozen probe has zero failures and all six cases; the independent closeout validates its strict controls, exact observations, complete save/exit/reap and all frozen inputs. The frozen runner/report is not edited. No broad managed-sharing acceptance is inferred from the scoped diagnostic.

All 14 selected actual classes are captured by a transformer returning null. The entire actual EasterBoss definition matches the original-method proof. Six bridge/integration helpers plus actual MaqueSupportHooks and YouziCombatEvents match candidate entries exactly. Raw classes stay local; only path/hash bindings are exported. No cross-JVM LivingEntity byte-equality claim is made.

## Preserved failed runs and unresolved raw-500 divergence

The first 20-mode sharing fixture fails its initial control and is not accepted. The first six-case routing fixture fails its final cow control; its intermediate actors change actual scope naturally. Both failures, sources, worlds and unchanged-input receipts are retained. V2 reorders the vanilla positive control before Boss carryover and adds read-only player-state evidence while retaining both original strict positive-control assertions. It does not prove a single root cause for the earlier failures and does not reclassify them.

Extending exact original comparison to the whole public hurt method fails on a real additional branch. Original Forge checks finite and positive raw input here, then continues. The current method also rejects raw damage greater than 500. The full instruction diff and both method hashes are archived; this comparison stays failed. Only this added entry guard is implicated: do not remove other legitimate original damage gates/caps. Actual >500 boundary coverage and a Boss-owned surgical correction are the next step. No runtime high-damage fix is claimed yet.

## Storage, recovery and remaining work

The three closed runtimes save fully, exit zero and are reaped. Every frozen input is rehashed unchanged. Eighty-four JAR paths and all evidence worlds remain; the duplicate JARs become 84 existing read-only local hardlinks, with no new canonical binary content or remote binary backup claim. New runtimes must create independent writable copies.

Only reviewed Java/Python, configuration and textual evidence may enter Git. All model/texture/animation/music/audio, raw JAR/class/world/binary files and nested resource payloads remain excluded. Source Git recovery cannot recover excluded resources. Duplicate Git worktree source caches may be evicted only after independent remote blob recovery and handle checks; canonical frozen inputs remain local.

Full original managed sharing, Youzi minis, cancellation/modifiers/range/lethal branches, authenticated summon/full battles/all Boss phases/rewards/death/cancel, multiplayer, dimension and persistence remain. Curios reload needs an attended user click and must not automatically repeat V18. Both formal instance hashes are read-only rechecked unchanged; D is read-only and weapon/Session development and validation stay paused. Current cumulative candidate remains NOT-INSTALLABLE and undeployed.

Primary receipt: work/session-boss/reports/manor-sharing-route-audit-closeout-20261114.json. Exact raw-500 divergence: work/session-boss/reports/manor-original-routing-static-divergence-20261114.json.
"""
create(WORK/'MANOR_SHARING_ROUTE_AUDIT_20261114.md',doc)
for path in [ROOT/'WRITE_OWNER.json',WORK/'CURRENT_BOSS_BATCH.json']:
    state=load(path);state.update({'activeWorkstream':'boss_original_sharing_route_source_checkpoint','currentWorkstream':'boss_original_sharing_route_source_checkpoint','latestOriginalSharingRouteCloseout':str(closeout_path),'originalManorUnmanagedSparrowSharingScopedAccepted':True,'fullManagedManorSharingNativeAccepted':False,'unmanagedManorDamageRoutingDiagnosticScopedAccepted':True,'extraPublicHurtRaw500RejectionProven':True,'extraPublicHurtRaw500RejectionFixed':False,'latestRaw500StaticDivergence':str(WORK/'reports/manor-original-routing-static-divergence-20261114.json'),'nextBossScope':remaining[0],'fullBossMigrationComplete':False,'latestBossCandidateInstallable':False});path.write_text(json.dumps(state,ensure_ascii=False,indent=2)+'\n',encoding='utf-8')
files=['AGENTS.md','work/session-boss/BOSS_MIGRATION_REQUIREMENTS_20261113.md','work/session-boss/MANOR_SHARING_ROUTE_AUDIT_20261114.md',closeout_path.relative_to(ROOT).as_posix()]
files+=['work/session-boss/tests/'+name for name in ['ManorOriginalSharingNativeV1.java','LoadedBossClassCaptureV5.java','ManorOriginalSharingMappedAuditV1.java','ManorOriginalSharingMappedAuditV2.java','ManorOriginalSharingDifferenceV1.java','ManorOriginalDamageRoutingMappedAuditV1.java','ManorOriginalRoutingDifferenceV1.java','ManorUnmanagedDamageRoutingNativeV1.java','ManorUnmanagedDamageRoutingNativeV2.java']]
files+=['work/session-boss/tools/'+name for name in ['run_boss_manor_original_sharing_native_v1.py','audit_manor_sharing_platform_mapping_v1.py','audit_boss_manor_original_sharing_mapped_v1.py','audit_boss_manor_original_sharing_mapped_v2.py','audit_manor_routing_platform_mapping_v1.py','audit_boss_manor_original_routing_mapped_v1.py','run_boss_manor_unmanaged_damage_routing_native_v1.py','run_boss_manor_unmanaged_damage_routing_native_v2.py','closeout_boss_manor_sharing_route_audit_v1.py']]
files+=['work/session-boss/reports/'+name for name in ['manor-original-sharing-native-v1-f0488a69.json','manor-unmanaged-damage-routing-native-v1-1bf75e89.json','manor-unmanaged-damage-routing-native-v2-5a21af33.json','manor-original-sharing-mapped-audit-v1-failure-20261114.json','manor-original-routing-static-divergence-20261114.json','manor-sharing-routing-dedup-configuration-v1-20261114.json','manor-sharing-routing-local-dedup-journal-export-20261114.json','manor-native-events-final-remote-proof-20261114.json']]
for folder,leaves in [('manor-original-sharing-mapped-audit-v1-20261114',['audit.txt','audit-terminal.json','method-differences.json']),('manor-original-sharing-mapped-audit-v2-20261114',['receipt.json','audit.txt','audit-command.json','compile-terminal.json','audit-terminal.json']),('manor-original-routing-mapped-audit-v1-20261114',['audit.txt','audit-terminal.json','method-differences.json']),('manor-sharing-routing-closed-runtime-dedup-v1-20261114',['plan.json','receipt.json'])]:
    files+=['work/session-boss/reports/'+folder+'/'+name for name in leaves]
files+=['work/session-boss/build/'+name+'/mapping-proof.json' for name in ['boss-manor-sharing-platform-audit-v1-20261114','boss-manor-routing-platform-audit-v1-20261114']]
require(len(files)==len(set(files)) and all((ROOT/name).is_file() for name in files),'Exact source-only checkpoint inputs present')
config={'key':'manor-sharing-route-audit-20261114','output':'work/session-boss/reports/manor-sharing-route-source-checkpoint-v1-20261114','files':files,'scope':closeout['scope'],'acceptance':{'fiveOriginalMethodAuditChecks':30,'actualRoutingDiagnosticChecks':880,'actualRoutingDiagnosticCases':6,'oneActualUnmanagedSparrowSplitAccepted':True,'fullManagedSharingAccepted':False,'raw500StaticDivergenceProven':True,'raw500Fixed':False,'productChanged':False,'fullBossMigrationComplete':False,'installable':False,'deployed':False},'remaining':remaining,'message':'Archive original sharing route evidence and unresolved raw-500 fidelity divergence; exclude resources','pruneDuplicateSources':[name for name in files if Path(name).suffix in {'.java','.py'}]}
save(WORK/'reports/manor-sharing-route-source-checkpoint-configuration-v1-20261114.json',config)
print(json.dumps({'scopedRecheckAssertions':checks,'selectedSourceTextFiles':len(files),'duplicateSourceCopiesEligibleAfterRecovery':len(config['pruneDuplicateSources']),'actualDiagnosticChecks':880,'fullManagedSharingAccepted':False,'raw500Fixed':False}),flush=True)
