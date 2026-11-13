# Boss 移植部署进度（2026-11-13）

## 2026-11-13：源码已推送并完成远端恢复校验

三批源码提交已推送到 `codex/neoforge-source-20261113`，源码检查点 `204b13de017c6cb2c8b6e06513f4436e550ff412`。重新拉取的1903个源码文件及1个导入清单全部通过来源SHA256校验，原始main和README保持不变。这只证明源码恢复；大型原始归档、JAR和世界证据仍保留本地，不据此删除。当前两个正式实例只读，不再部署。武器与Session开发继续暂停；下一步验证并移植原版紫菜Boss的Curios饰品沉默，完整Boss验收仍未完成。

武器系统继续暂停。源码、证据和独立恢复 JAR 已压缩封存；恢复 JAR 暂不可作为正式武器插件安装。当前正式 Boss JAR 无需安装该武器快照。

## 本轮已验证并部署

| 增量 | 结果 | 验证 |
| --- | --- | --- |
| Stage14：Boss 客户端资源 | 补齐314个原版资源，21个OBJ模型改用NeoForge加载器；原1443个类不变 | 136个Boss机位画面、44个雕像画面、21个模型烘焙及贴图检查；生存黑暗效果和自然到期后的完整画面恢复 |
| Stage15：玄岚遭遇限制 | 还原每个Boss最多15个飞轮、独立归属和自然清理；还原所属援军限制 | 86项基线和101项候选原生检查，实际入场、补位、自然最旧优先清理及公开归属变更 |

## 当前正式文件

`C:\Users\qyby\AppData\Roaming\.minecraft\versions\FarmingTales_NeoForge 机械动力\mods\qys-bosses-1.21.1.jar`

SHA256：`92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4`

本轮每段均先验证、校验回滚备份、部署，再压缩归档。正式整合包未在本轮直接启动；相同哈希JAR已在隔离原生NeoForge环境中加载验证。

## 压缩归档与回滚

- Stage14：`work/session-boss/archives/boss-core-stage14-client-resources-20261113.zip`，18164个文件逐项读回和SHA256核验。
- Stage15：`work/session-boss/archives/boss-core-stage15-xuanlan-limits-20261113.zip`，1252个文件逐项读回和SHA256核验。
- Stage15部署凭据与上一版回滚包：`work/session-boss/releases/boss-core-stage15-xuanlan-limits-20261113/`。
- 武器封存：`work/session-boss/archives/weapons-paused-cf217105-boss-priority-v2-20261113/`。

## 继续移植的范围

Boss整体尚未全部验收。Stage13召唤特效仍单独保留并未混入正式部署；真实登录入口、奖励、多人与完整战斗验收，以及其余原版行为差异仍需继续完成。下一段先核对工地施法生命周期的原版与现有实现，再做原生基线验证。原Forge实例和历史D盘工程保持只读。


## 2026-11-13 新增 Stage16：工地施法生命周期

- 已部署原版五处施法/重置守卫：错误变种、小型、禁AI、死亡状态不能开施法；无有效施法时不能空放冲击，中断或重置会清理未释放提示，跨世界调用不修改原Boss。
- 基线35项、候选35项原生检查通过；310个方法ASM验证通过，305个无关方法和修改方法原主体保持一致。
- 当前正式JAR SHA256：`cb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d`。
- 归档：`work/session-boss/archives/boss-core-stage16-gongdi-lifecycle-20261113.zip`，1263个文件逐项读回/SHA256校验。回滚包和部署凭据位于`work/session-boss/releases/boss-core-stage16-gongdi-lifecycle-20261113/`。
- 下一段核对工地原版目标类型、伤害和技能节奏；Boss整体尚未全部验收，真实登录、奖励、多人和完整战斗仍待继续。武器开发保持暂停。


## 2026-11-13 Stage17：工地普通生物目标与技能控制器

- 已恢复原版普通生物目标、三档距离选招、出血、原版技能轮转和中断清理。保留玩家兼容入口和提示；Boss原有295个方法保持不变，增加两处原版生物目标重载。
- 原生入口基线25项/候选28项；技能132项、93次自然服务器回调及35项生命周期回归通过。实测断头台、重击伤害、拉回、减速、出血和目标失效中断；未把公开控制器定向测试表述成完整自然AI战斗。
- 正式JAR SHA256：`021de8b498baf3d215220ca2399e68471864872a7be75b775bb6ba3e588d7162`；已保存Stage16精确回滚。归档2480个文件，逐项读回并校验SHA256：`5708ea1f23d6ca2a0241067552843acfc61f468613e3e0a72c2be375951420eb`。
- 下一段继续原版共享常量和实体近战/大杀四方生物目标路径。真实登录、奖励、多人及完整战斗仍待完成；武器开发保持暂停，分离JAR仍为不可安装的恢复快照。


## 2026-11-13 Stage18：原版公开技能入口与共享参数

- 已补齐普通生物版本的大杀四方命中回调，并恢复原版6个共享常量。原有297个Boss方法及9个规则方法保持不变；没有改动原版有意保留的近战玩家限制。其他已内联旧数值的技能路径继续单独核对。
- 32项原生检查通过，实测真实普通生物承受伤害后经公开回调叠加护甲侵蚀至5层，血怒叠满。Stage17的28/132/35项证据按相关方法/类字节一致性复用。
- 正式JAR SHA256：`5aabc09b09e836eee820bda998077561b6fbe7b5a6d9673e80e6ca0d3f9a0cfa`；Stage17回滚包已保存。主归档453个文件逐项读回/SHA256校验：`57606ffaa378d74e37735b509fcc498de7ecda779561ff4150d13c34803e3edd`。
- 下一段核对原版ManorFiveSkills等Boss行为。整体移植、真实登录、奖励、多人及完整战斗仍未全部验收；武器继续暂停。


## 2026-11-13 Stage19：六类Boss普通生物技能目标

- 恢复原版普通生物目标校验、范围查询和UUID解析，仅改5个目标边界方法；其他20个方法及全部其他类和资源字节保持一致。已修正实测发现的Level/ServerLevel链接错误，并逐项验证6个辅助方法签名。
- 原生基线55项、候选入口61项通过；六类Boss共13种当前技能实测伤害、效果、各次脉冲、范围外对照及移除目标/禁用AI中断，共1116项检查，425次自然服务端Post回调。测试通过公开控制器调用并在脉冲间恢复普通生物生命与受伤冷却，不代表完整自然Boss战斗验收。
- 正式JAR SHA256：`15feeefbbed6dbd4d488b721b8b2e34562a1e0fbeb3a2eb844a6d80da6bc6bf9`；Stage18回滚包已保存。主归档3288个文件逐项读回/SHA256：`442ce0d653fb94ff7a7b584b1f4e4f7b72c021e5b6cb84085425675b3c084d7e`。
- 继续移植原版剩余回声、对空、平衡、动作及特效。真实登录、奖励、多人和完整战斗待验收；武器开发与验证继续暂停。


## 2026-11-13 Stage20：恢复原版对空技能选择

- 补回原版选招后的5条指令，其他24个方法及全部其他类与资源保持不变。恢复12处对空选择差异，其中8处原来会空转；落地目标与没有对应远程技能的分支保持原样。
- 原生基线470项、候选486项通过，覆盖6类Boss全部52种落地/空中轮换状态，候选实际34次施法，并核对公开动画时长与技能提示。公开落地标志是明确的测试状态，不代表自然飞行或完整战斗验收。另有8195项原版/当前远程选择纯函数对照通过。
- 正式JAR SHA256：`775986661eacd6686431e21d0779bd67bba3f51e2569891a742236850f0ef30f`；Stage19回滚包已保存。主归档871个文件逐项读回/SHA256：`c659cbe69a80a485d4610aca45087d8563412e7bf85ecf0b202823be11b74c79`。
- 继续核对原版剩余施法时序、回声、平衡、动作及特效；真实登录、奖励、多人和完整战斗待验收。武器开发与验证继续暂停。


## 2026-11-13 Stage21：恢复普通/不可能难度原版施法时序

- 补回原版可读性与平衡时序的两层计算，仅增加6条原版指令。其他24个方法及全部其他类和资源保持不变，对空选招逻辑保留。恢复两种难度下6处前摇时序差异。
- 原生基线212项通过；候选2233项检查通过，覆盖13种技能、两种难度的26次施法与74次真实普通生物伤害脉冲，并检查效果、动画、完成时刻、范围外对照和中断。使用公开落地/生命/受伤冷却测试状态，不代表完整自然Boss战斗验收。
- 正式JAR SHA256：`21af23af5246b284bd5939b9b46735c1a61fc69627a9b86d6811c8c91d0d329d`；Stage20回滚包已保存。主归档862个文件逐项读回/SHA256：`18e525a45654dfee8f8fff3d970e30e4ebf182705f32122eaec6b5e9d0e338f1`。
- 继续核对原版剩余回声、动作及特效；真实登录、奖励、多人和完整战斗仍待验收。武器开发与验证继续暂停。


## 2026-11-13 Stage22：蛋蛋原版动作生命周期

- 补回主技能启动/命中与实体收招/状态续传四处原版动作调用。原始Boss类和所有原有注入器及校验保持字节一致；新增严格前后指纹的安装器，在原Boss回调末尾执行，无额外Mixin回调顺序依赖。
- 原生基线2363项通过；候选2475项检查与327项现有接口/同步编码检查通过，覆盖28次动作状态、13技能/两种难度的26次施法、74次真实普通生物伤害脉冲及完成/打断。保留公开落地/生命/受伤冷却测试状态，不代表完整自然Boss战斗或客户端动作渲染验收。
- 正式JAR SHA256：`a310afc254a748baecf19b2258af45a667ab55cf89081ea02fe27e8c991f2f9a`；Stage21回滚包已保存。主归档3115个文件逐项读回/SHA256：`2c6c13bcca163fe76d6c4bb960239559e6b6498f487408987bdd6d16c1261af1`。
- 静态缺失接口与中间导出不能代表实际运行期缺失；保留早期错误假设、原版收招预期修正、注入前像/回调顺序失败和诊断，全部未部署、不作种子。
- 继续核对有效运行流程中的回声/动作/特效；真实登录、奖励、多人及完整战斗仍待验收。武器开发与验证继续暂停。


## 2026-11-13 Stage23：三位Boss原版光效启动与脉冲调用

- 补回蛋蛋、清一、折耳根的六处原版主技能光效调用；仅修改技能控制器的两个方法，增加66条指令。原有指令、六个原版辅助方法图、方向/中心/脉冲参数和光效/动作调用顺序已校验，其余1446类与资源逐字节保持。
- 基线3340项、候选3920项检查通过；58份真实原生光效载荷及58次编解码往返，覆盖身份、维度、时间、中心/方向、半径、时长、脉冲、类型与阶段。26次施法、74次普通生物伤害、时序及伤害总量与基线逐项相同，保留327项既有动作辅助检查与28次动作状态。
- 本段验收服务端载荷构造；测试无玩家接收方，未宣称联网送达或客户端可见效果，也未替代真实登录、自然Boss AI、多人及完整战斗验收。所有原有通道、距离与资格限制不变。
- 正式JAR SHA256：`97d564e42b907f13a25382e31dff798d6e8899d90d11a9de74c5694504a97c43`；Stage22回滚已保存。主归档1220个文件，完整读回/SHA256：`a8000d2d8973b4b49e29d5dd97bc35f398c30f0f952fedc61690f6f079a59d8c`。
- V1观察器Mixin包覆盖测试入口导致启动失败，证据保留；V2分离观察器子包后正常停止、保存并退出。失败运行未部署、不作种子。
- 继续原版身体动作/回声及客户端光效验收；Stage13仍未纳入，武器开发和验证继续暂停，分离武器包仍是不可安装的恢复快照。


## 2026-11-13 Stage24：原版身体动作与起手、命中、收招链

- 恢复原版完整命中分派、剩余起手调用、清一施法轨道、折耳根传送/留影入口、玄澜身体锁定方向与原生提示，以及完整收招。原始Boss类及全部原有注入器/校验保持字节一致；复用已存在的原版hitShape/echo辅助方法，无额外Mixin标记或同步数据编号。
-45个辅助方法行为图、局部参数与存储来源已证明等价，4项错误接收者/分支/键/参数负向控制通过。374个有效方法ASM校验、369个其他有效方法保持，63个原版辅助方法描述符含继承解析通过。
- 候选4101项主检查、1106项身体检查、36项起手原生粒子/声音检查、327项既有动作辅助检查通过。26次施法/13技能/两种难度/74次主命中，时序与总伤害逐项匹配基线；76次身体和提示同步编码及注册服务端镜像赋值、62份光效载荷、28次动作状态、8种打断通过。
-6次折耳根实际起手传送、2次真实留影捕获及快照立即交接清空得到验证；公开恢复真实快照后重放2次，原版一次hitShape/25%辅助方法输入41.0625与后续处理后最终扣血10.265625分别记录。未将公开恢复重放冒充自然倒计时或自然Boss AI验收。
- 正式JAR SHA256：`4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0`；Stage23回滚已保存。主归档3285个文件完整读回/SHA256：`22124a74501091376015dc3d23ec529055a9b9861e96cfac9c7de0ccfc2fcabc`。
- 客户端可见身体/特效、自然留影时间线、真实登录/奖励、多人、跨维度/重启与完整战斗仍待验证。保留所有静态校验与测试预期修正失败证据；Stage13未纳入，武器系统继续暂停，独立武器包仍是不可安装恢复快照。


## 2026-11-13 Stage25：恢复原版暗影法术显示时长

- 暗影烙印蓄力10→20 tick、释放/脉冲22→60 tick；虚空回响与夜幕刃阵蓄力10→20 tick。完整288组纯数据与原版相同，其余283组、枚举序号、颜色/半径/附着等保持。
- 只修改一个枚举方法的3条包装指令，加入字节完全相同的原版辅助类；其余1448个现有类和全部资源不变，原始Boss/控制器/插件/校验不变。9方法ASM校验、3项负向控制通过。
- 基线与候选各72法术×4阶段、288次原生注册表载荷编码往返及公开SpellVisuals.show调用；基线5处差异、候选0处差异，两个新世界均完整保存/正常退出。此为服务端枚举及编码验收，未声称实际客户端可见效果/到期消失或真实多人战斗通过。
- Stage24自然留影补充验收已独立完成归档：12场景、2896检查、1207自然Post；13次真实捕获、18份载荷编码、8次伤害辅助入口。
- 当前正式JAR SHA256：`7eb3b4c2e4ef8e35c806a4df8b53cb32879a9cec9abee961a963bf558d39d7b6`，Stage24回滚保留。主归档934文件，读回/SHA256：`c02d09eee4afa10a7b251e66f2fe48e1cabebb731e470b5a14ad68f1bf48c08e`。
- 下一段检查原版侵蚀及已退役致盲效果的实际有效路径。真实客户端/登录入口/奖励、多人、持久化/死亡和完整战斗仍待完成。Stage13未纳入，武器继续暂停，独立武器包仅为不可安装恢复快照。


## 2026-11-13 Stage26：恢复原版退役致盲与增益清除逻辑

- 恢复原版旧暗影标记清理：仅移除匹配的隐藏致盲/黑暗伴随效果，保留可见、图标、不同等级、时长差异和无关效果；公开致盲入口不再施加已退役效果。
- 对照原版确认玩家增益清除器全部7个公开方法已停用，恢复原版ENABLED=false及4常量；无限速度与40000tick抗性V正常施加并保留。未弱化无限时长测试。
- 原生客户端新世界通过正常原版登录创建真实本地玩家；20组自然玩家tick与实际客户端状态同步、85项公开规则、6组非玩家入口，共470检查零失败。206服务端Post、202客户端tick，完整保存正常退出；基线6处旧逻辑差异已复现。
- 17个原版方法按1.21Holder API映射，54方法ASM与4负向控制通过，37其他方法保留；只改3个现有类，其余1447类和全部资源逐字节不变。原始Boss/控制器/鉴权校验不变。
- 当前正式SHA256：`b56e786c97dc9ed903abc851a4ffd0c21977746a51e1dfafcab19e634437a0a6`；Stage25回滚保留。主归档7763文件，读回/SHA256：`1ee076658e99e5c93371acf8027ad61a81d066d175bfd2bf43ae3377a40c4f5a`。
- V2无限标记被旧增益清除器拒绝、首次模块分包失败及错误静态假设均保存为失败证据，未作为世界种子。真实法术显示/到期、鉴权入口/奖励、多人和完整战斗仍待验收。武器暂停，独立包仅为不可安装恢复快照。


## 2026-11-13 Stage27：恢复原版侵蚀分类与伤害

- 仅修改侵蚀效果的两处引用：恢复原版增益分类，以及每层4点、最低4/最高20点的原版伤害辅助入口，去掉旧四分之一计算。原版每20tick周期、伤害来源、无敌帧重置和平台布尔返回保持。
- 基线与候选各14只真实入场NoAI牛和1名原版正常登录本地玩家，8组等级、6组时长边界、34次自然伤害；实际血量、20tick间距、自然到期及客户端效果/分类/血量/到期同步通过。基线35处差异，候选0处；新世界完整保存正常退出。
- 原版3个完整方法及2个纯伤害辅助方法图精确相同；4方法ASM、4项负向控制及重复修改拒绝通过。其余1450类和所有资源逐字节不变，累计1451类。
- 当前正式SHA256：`6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0`；Stage26回滚保留。主归档3920文件，读回/SHA256：`c3131850489ad5b85d2f174f92b8c9aeb50d958837c68514574780d9b31221f1`。
- 1000最大生命、食物10、无重力/平台等公开夹具仅用于受控伤害验收；未声称原生完整BossAI/战斗、画面渲染、真实鉴权多人和完整正式整合包启动通过。继续剩余Boss移植，武器暂停，恢复快照不可安装。


## Latest GitHub persistence instruction and deployment prohibition, 2026-11-13

- Latest relayed user requirement requests GitHub as the main durable store for reviewed NeoForge source/history/checkpoints. Preserve this sole C writer. It expressly prohibits further deployment and formal-instance overwrites;both formal instances are now read-only. Existing Stage27 was deployed/closed before the new restriction and remains SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0. Do not revert or replace it.
- Current workspace/parent/work/session-boss/session-lease have no Git repository or remote. Git is installed;GitHub CLI is not on PATH. Target repository URL is missing,so remote visibility,permissions,branch history,LFS/Release support and authorization remain unverified. No Git repository,branch,commit,push,upload or public repository was created. Report this actual missing target;do not guess credentials or inspect private launcher/account stores.
- Root .gitignore/.gitattributes and github-sync/GITHUB_RESTORE_AND_SYNC.md prepared. Initial text source/config/script/doc scan1873candidates found0high-confidence pattern hits;this is initial scanning,not complete publish clearance. Generated/runtime/raw receipt/account/session/private configuration data and binaries are excluded from ordinary Git. Review necessary exports individually;never stage the entire tree blindly.
- Keep all original archives,failed evidence and active inputs local until exact remote retrieval/hash/recovery passes. Large essential artifacts require the existing remote supported LFS/Release channel;no oversized ordinary Git or implicit public repository. No local file may be deleted merely to assume GitHub recovery. Closed immutable JAR dedup retained339paths/88contents and reclaimed about3.4GiB with all path/content hashes preserved;no worlds,formal products,weapon workspace or D history changed.
- Stage26 and27 verified increments/main+closeout archives remain scoped accepted.Full Boss goal active and full product NOT-INSTALLABLE;weapons/development/validation/lease/payment/Session paused.Stage13excluded. After GitHub is configured,each future feature must pass focused validation,secret/staged review,commit/push/hash/test/next-goal recording and remote recovery verification before any permitted local cleanup. Current deployment prohibition persists.

Next: GitHub persistence first:obtain existing target repository URL,verify remote visibility/permissions/history/LFS or Release support,then review exact source/config/test/sanitized evidence batches,commit,push and verify fresh remote recovery. No further deployments or formal overwrites. Do not delete any local evidence based on unverified remote storage. Full Boss migration active;weapons and Session work paused.


## 2026-11-13 GitHub reviewed source staging;author identity pending

- Supplied existing public repository qyby123456/ft:default main,base27d20d5e1d2eb960f4a64cde9bd21230efae766c,one README/history commit preserved. Authorized local branch codex/neoforge-source-20261113,source only under neoforge/ftboss. No force push orremote changes.
- Initial1899source files plus one import manifest staged,all1900raw blob SHA256 match originals. Full staged scan covers1900files/12JSON,13detector controls and2exact hash/line negative controls,zero unreviewed findings. Three deployment-label/synthetic-control matches are exact-byte AST-reviewed;failed first scan retained. V3 batch verifier matches all V2 hashes/findings/controls exactly andchecks unchanged index tree.
- Existing matching Credential Manager account,locally selected,passes IPv4 receive-pack dry-run. No token printed/stored in source. Native Git useConfigOnly confirms no explicit author name/email,so no commit orpush performed. Do not invent identity. Final synced counts are recorded in the current source-import manifest andreceipt;initial counts above are historical evidence.
- Both formal instances stay read-only;Stage27 SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0 unchanged. All archives/failed evidence retained,no remote-dependent cleanup. Full Boss migration andactual remote recovery remain incomplete;weapons/Session paused.
