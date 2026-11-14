## 原生伤害事件真实兼容缺口基线，2026-11-14

Stage30 产品字节不变，新增真实玩家对 Boss/普通牛的两次空手正伤害对照。353 项诊断检查通过：Boss 1024→1023.72，仅原自定义 BeforeMitigation/BeforeHealth 各 1 次，原生 Incoming/Pre/Post 都为 0；相同监听器在牛 10→3 上原生三个事件各 1 次。原始 incoming amount 为 7。已正常完整保存/退出 0、进程回收、最终定义捕获和冻结输入哈希不变。实际定义的 ManorFinalDamage 与候选字节相同；原版/当前调用和本地官方 API 审查确证兼容缺口，不代表已经修复。详见 work/session-boss/MANOR_NATIVE_DAMAGE_EVENT_BASELINE_20261114.md 与 reports/manor-native-damage-event-baseline-closeout-20261114.json。

下一段必须采用 Boss 独立、同一调用完整 DamageContainer 生命周期，保留原事件可变/取消语义、护甲/吸收/分摊顺序、减伤修饰、无敌帧及嵌套/异常清理；不能只补发通知凑回调。暂停的武器源码树及 Independent Event 不动。另对已关闭的三次通过范围运行本地去重，84 个 JAR 路径/字节全部保留、84 个已有只读硬链接，约再释放 618 MiB，无任何资源上传。原生事件契约仍未接受，完整 Boss 未完成，NOT-INSTALLABLE、不部署、正式实例及 D 只读、武器/Session 暂停。

## Stage30 麻雀原版受击闪避限定验收，2026-11-14

源码及限定文字证据已推送 70ead9b29fdca72e924c8afc352058ebcafc50fc，44 个文件经独立远端 fetch/blob 大小与 SHA256 核验，完整 4425 文件树资源检查通过。核验后仅删除 Git 工作副本中 24 份重复 Java/Python 缓存（248040 字节），逐路径 skip-worktree，canonical 编译/运行输入、世界及所有资源保留。恢复入口 github-sync/checkpoint-sparrow-hit-evasion-20261114.json，清理持久化索引 github-sync/checkpoint-sparrow-hit-evasion-cleanup-20261114.json；必须按索引按需恢复，不重跑要求已清理源码全在场的旧发布器。

原版 SparrowHitEvasion/State 的 25 个映射方法图及 25 个 ASM 检查通过，规则类字节原样保留，接入原版三处钩子。真实离线玩家空手攻击基础场景 971 项、原同伴掩护场景 772 项通过；分别观察到 22/16 个自然 tick 的原版自有路径，同伴控制保护 16 tick。原 60 tick 到期、no-AI 拒绝、实际实体离开清理通过，正常完整保存/退出 0，冻结输入未变。最终定义 EasterBoss 类 351 项检查限定复用，339 个方法不变，仅 hitFeedback/sparrowTick 改变；五个 Boss 定义字节一致，LivingEntity 不同且不在该复用证明范围。

候选 SHA256 877309d1ff2332ab6d407cd1b456d39987109d17e8f6a8e98184abe55703c25b。原始 bootstrap、两次正向寻路失败和首次六类绑定错误均保留；原寻路安全阈值未放宽。同伴夹具自然空 fight/summoner ID 相等，仅证明该分支，不声称认证管理战斗；冻结同伴回执的 noAllyFixture=true 属继承标签错误，在限定收口中更正解释，不改写原证据。详见 work/session-boss/SPARROW_HIT_EVASION_20261114.md 与 reports/sparrow-hit-evasion-v2-closeout-20261114.json。

关闭的三次失败运行 84 个 JAR 路径/全部字节保留，82 个只读同内容硬链接约释放 504 MiB，仅为本地去重；不声称资源远端可恢复。后续只上传审查过的源码及文字证据，必须先完整树资源/秘密扫描、独立远端逐 blob 恢复核验，才清理 Git 重复源码缓存。全部模型、贴图、动画、音乐/音频和嵌套资源禁止上传。完整 Boss 仍未完成，原生 LivingDamage.Post 尚缺（本轮 0 回调），NOT-INSTALLABLE、不部署、正式实例与 D 只读、武器/Session 暂停。旧 Stage29 的“闪避未移植”为当时历史状态。

## Stage29 庄园减伤上下文限定验收，2026-11-14

源码与限定证据已推送提交 c1092fd1dc70c49ed0a507e5a2a01233775984cd，32 个文件经独立远端 fetch/blob 对象核对原字节；资源排除守卫覆盖完整 4382 文件树。只清理了 Git 工作副本的 28 份重复文件（238419 字节，其中 19 个跟踪源码标记 skip-worktree），当前候选/编译输入、世界、模型与音乐原件均未删除。恢复索引见 github-sync/checkpoint-manor-damage-context-20261114.json；长路径恢复用 work/session-boss/tools/restore_boss_git_blob_v1.py，通过 ls-tree 对象 ID 与 cat-file 读取，禁止重跑要求全部旧 canonical 源码存在的发布器。修改已清理路径时应先按索引恢复并清除对应 skip-worktree 标记。

真实空手玩家攻击已复现并修复 NeoForge damageContainers 空栈异常；仅修改 NativeManorFinalDamageInstaller，一个候选类变化。72 项合成生命周期检查、701 项真实玩家/原自定义 BeforeHealth/生命变化与自然 tick 检查、22 项最终定义类检查通过，完整保存/退出 0，冻结输入不变。采集中文选项编码事故的 11 个自有文件已按 SHA256 迁回，原失败回执保留，后续使用 Base64URL UTF-8 选项。详见 work/session-boss/MANOR_DAMAGE_CONTEXT_20261114.md 与 reports/manor-damage-context-v1-closeout-20261114.json。原生 LivingDamage.Post 契约尚未完成（本轮 0 回调）；麻雀闪避未移植，现有真实正向基线可用。NOT-INSTALLABLE，不部署；模型/音乐等资源禁止上传，武器/Session 暂停，两个正式实例只读。

## 模型资源与音乐禁止上传，2026-11-14 最新用户指令

执行收口：旧草稿 Release 的 95 个附件已全部撤回并逐个验证 404，远端附件数量为 0；撤回前恢复并校验缺失本地原件共 3089431948 字节，同内容只保留一份并记录恢复别名。资源排除规则、撤回回执和源码上传拦截器已推送提交 58f19d05b0a2a5b3c9075608d4e200ab3865592c，独立远端 fetch 和 archive-storage 全文件字节核验通过；完整 4352 文件 Git 树通过纯源码检查。旧 JAR 清理计划已作废，未执行 JAR 缓存删除。Boss 与武器源码子树保持一致。

用户明确要求“全部模型资源文件和音乐文件不能上传”。此限制优先于此前远端归档及本地缓存清理要求；适用于 Git、Release 附件、压缩包、CAS blobs 和 JAR 内嵌资源。模型、贴图、动画、音频等资源仅保留本地必要副本，禁止将其放进上传包。允许上传不含资源的源码、配置、路径/哈希清单及恢复说明；模型相关 Java 行为源码不等于模型资源。

旧的未过滤归档上传器与基于旧归档的缓存清理器已冻结。正在对既有草稿附件先恢复缺失本地原件、验证大小/SHA256，再撤下远端附件；必须记录每个资产的恢复和撤回回执，不能先删唯一可恢复副本。旧 JAR/生成目录清理计划不再有效，不得据其删除本地文件。后续发布必须在上传前检查整个负载，包括嵌套归档、JAR 和内容寻址对象；无法判定安全的二进制默认排除。不得以仅检查文件名代替内容检查。

## 用户明确要求归档远端化、本地仅作缓存，2026-11-14

最新直接指令：归档后清理本地文件，不继续堆在磁盘；GitHub 作为持久存储，本地只作缓存工作区。此指令授权在完整远端恢复核验和清单持久化后清理本地归档、历史运行/构建证据副本；取代此前要求永久保留所有本地 ZIP/JAR/失败证据的限制。必须保留远端的原字节失败证据，失败世界永远不能作为重启种子。当前必要的 Boss 候选、编译输入与共用运行依赖可作为有用途的缓存保留；两个正式实例和历史 D 工作区仍只读。

源码、内容索引及恢复说明进入 qyby123456/ft 既有源码分支；超过普通 Git 文件限制的大归档进入同仓库 ft-cache-archive-20261114 草稿 Release 附件，避免将备份标为正式可玩发布。草稿附件需仓库写入权限访问。逐文件流式上传后，必须用独立的远端完整下载计算大小/SHA256，随后提交推送索引并独立 fetch 验证，才能清理对应本地副本。不得以服务器返回的 digest 单独代替完整下载验证。

当前执行工具为 work/session-boss/tools/offload_archives_to_github_v1.py；运行进度、资产 ID、逐字节下载证明、Git 提交和本地清理清单位于 work/session-boss/reports/github-large-artifact-offload-v1-20261114/。archive-storage/INDEX.json 是远端归档定位入口；archive-storage/coverage/ 保存清理运行副本所需的归档路径/哈希目录。后续工具应识别可回收缓存的预期缺失，按需从 GitHub 下载并验证，不能仅因本地归档已清理就认为证据遗失或自动重跑旧验证。武器开发/验证暂停，Boss NOT-INSTALLABLE、不部署与唯一写入者限制继续有效。

## 用户要求武器源码上传与本地清理，2026-11-14

本轮用户明确要求将 FT 武器部分上传 Git 并清理本地工程源码。已在既有 codex/neoforge-source-20261113 分支的 neoforge/ftweapons/ 单独保存 718 个当前编译输入、开发历史、JSON 资源和维护工具；最终提交 aaff29b64aea74bf54f6c32ef35d96d752e8931a。独立远端恢复核验 2083 个文件的路径、大小、SHA256；历史 neoforge/ftboss 子树和根 README 对象保持一致。修正 Git CRLF 转换的先前失败核验已保留。

根据本轮直接授权，在所有文件远端核验与进程/句柄检查通过后，已删除 1361 个本地武器源码文件（29480062 字节），保留 38 个 Boss/共用引用或暂停状态输入；另清理 2076 个已核验重复 Git 工作副本（33933026 字节）。精确清单与回执位于 work/session-boss/reports/ftweapons-git-source-cleanup-v1-20261114/；远端 LOCAL_CLEANUP.json 与 FILES.json 给出字节绑定和恢复位置。此清理是预期状态，替代此前要求保留全部武器源码原件的限制；后续源码核验/发布必须先读取清理清单，不得直接假设旧 2040 个本地源码仍全部存在，也不要直接重跑仅支持 ftboss 单前缀与完整本地源码的旧发布器。

恢复工具 work/session-boss/tools/restore_ftweapons_git_snapshot_v1.py 支持从现有独立 Git 对象核验，或恢复到新目录。保留的 reactivate_goal_after_boss_stage12.py 等共用输入继续可用。ZIP 恢复包、非安装版武器 JAR、世界及二进制依赖未删除，也未声称远端可恢复。武器和 Session 开发/验证继续暂停；未启动新的游戏或验证进程。当前 Boss Stage28 的构建输入未被删除，正式实例只读、NOT-INSTALLABLE、不部署、原唯一写入者与 Boss 目标继续保持。

## 新 JVM 有人值守工具与 V18 超时证据，2026-11-14

V18 仅延长真实确认页等待至 180 秒并记录页面状态，保留原严格登录/物品/120 自然 tick 断言。已从接受的 V6 正常存档创建全新副本，启动前及实际页面出现时通知用户立即点击 Create Backup and Load；本次到期未确认，严格失败、正常退出且无服务端或玩家登录，不能宣称持久化验收通过。当前没有等待窗口或残留进程。下次须先由用户确认已在电脑前，再启动准备好的工具；不得自动重试、复用失败世界、猜测点击、调用私有确认回调或修改警告标志。见 work/session-boss/CURIO_ATTENDED_RELOAD_RECOVERY_20261114.md 和对应检查点。

此前所有通过的生命周期增量仅引用，未重跑。另对非庄园终结难度完成原/当前类字节绑定的初步契约审查；属性缩放范围及方式有静态差异，尚需真实有效基线，未判为已证明的运行时缺陷，未绕过挑战解锁。完整 Boss 及完整生命周期仍未完成。保持同一唯一写入者、NOT-INSTALLABLE、不部署、正式实例只读、武器及 Session 暂停。诊断源码和证据归档同步不代表接受失败功能；远端独立核验后只清理合格重复源码缓存，所有原件保留。

## Curios 无确认页依赖项逐批完成，2026-11-14

同一 C 工作区、同一唯一写入者已分别完成 V13 默认不保留背包的死亡/真实掉落/复活/自然拾取（784 检查）、V15 真实 Curios 界面与原容器网络装备拒绝及到期后装备（515 检查）、V16 双非装饰槽选择/刷新与装饰槽排除（1528 检查）、V17 真实停止后去重表的只读直接观测（140 检查）。每项单独归档、提交推送、独立远端恢复核验后才清理合格的重复源码缓存。V14 读取事件结果过早的严格失败保留，V15 只修正探针读取时序，未修改产品或放宽断言。已有跨维度、四类回退和 18 项控制直接引用，未重跑。

剩余生命周期门槛为新 JVM 真实登录与持久化：需先准备有人值守的隔离观察器，再由用户点击实验性页面的 Create Backup and Load。当前没有等待窗口；不得绕过、猜测点击、修改世界标志或调用私有确认回调。V17 仅用只读 Field.get 观测表内容，未写字段/Map 或调用私有玩法方法；停止前已为空，不能把空表结果单独归因于 onStopped.clear，原方法仍由冻结图证明支持。状态表见 github-sync/checkpoint-curios-lifecycle-matrix-20261114.json。完整生命周期和 Boss 仍未完成，NOT-INSTALLABLE、不部署，两个正式实例只读，武器和 Session 暂停。已归档公共依赖仅合并同字节只读硬链接，所有路径、失败世界和冻结输入哈希保留；新实例必须独立复制。

## Stage28 dimension lifecycle increment, 2026-11-14

V12 newfreshnativeworld passes998checks:activepending survives actualoverworld-to-Nether playertransition withoutresettingUntil,naturaloriginaloverworld-clockexpiry restores exactlyone component-preservingrelicinNether,actualclient sees suppressed/restoredstates,andnative return staysstable40ticks. OneactualpositiveBosshit/twoactualdimensionevents;candidateunchanged. Priorfourfallback/18ownercontrols arecitedwithout rerun. See github-sync/checkpoint-boss-curios-dimension-20261114.json andwork/session-boss/CURIO_DIMENSION_RECOVERY_20261114.md. NOT-INSTALLABLE,nodeployment,formalinstancesread-only,weapons/Sessionpaused,samesolewriter.

New-JVM login isstillunaccepted:priorV7/V8experimentalBackupConfirmScreenevidence remains. Currentcomputer-use requiresunavailable node_repl;noalternateautomation/inputguess/privatecallback/world-flagchange. Anattendedisolatedreloadobserver mustbeprepared;theusermustactuallyclick Create Backup and Load. No windowiscurrentlywaiting. Remainingnative lifecycle gapsareexplicitin github-sync/checkpoint-curios-lifecycle-matrix-20261114.json. Archiveonlynewnativeevidence againsttheimmutablebase;freshremoteverifybeforeguardedduplicate-cachecleanup. Preservealloriginals/rawartifacts.

## Stage28 native Curios increment, 2026-11-14

Four scoped fallback cases now pass with actual integrated-player/server/client item synchronization:missing slot index,missing slot identifier after natural empty-slot sync,occupied slot without overwrite,andfull inventory retaining pending until capacity is freed. V9 whole-run failure remains preserved;only its already-passed missing-index case is accepted. V10 runs only remaining three cases. V11 passes18owner/projectile/eligibility controls,including actual final-positive creative andspectator exemptions and21registered projectiles;natural projectile collisions are not claimed. Native checks V9=1059(withone preservedfailure),V10=1145,V11=2000. Product candidate unchanged.

Incremental local archive has5804paths andrequires the hash-bound original Stage28 base;all paths,size andpayload hashes verified. No full physical restore orlarge-artifact remote recovery is claimed. See github-sync/checkpoint-boss-curios-fallback-owner-20261114.json andCURIO_INCREMENT_RECOVERY_20261114.md. Continue actual supported new-JVM confirmation/remaining lifecycle work without guessed experimental-screen input. NOT-INSTALLABLE,no deployment,formal instances read-only,weapons/Session paused,same sole writer. Clean only independently remote-verified duplicate source-cache files under the published plan;retain canonical frozen inputs andall raw artifacts.

## Explicit verified-cache cleanup policy, 2026-11-13

The user now requires GitHub as durable storage andlocal files only as temporary cache. Delete only uploaded,independently path/size/SHA256/commit-verified unused duplicates after the checkpoint/restore notes are remote. First target is the persistence sidecar working files only,using github-sync/checkpoint-source-cache-cleanup-plan-20261113.json andSOURCE_CACHE_RECOVERY_20261113.md. Guard all absolute paths,process references andfile handles;record every path/hash,commit andfreed bytes. Preserve .git,minimum checkpoints/recovery scripts,current required C source,frozen baseline/failed evidence/formal receipts,unuploaded archives/JARs/worlds andall D/formal-instance files. No large-artifact remote recovery is proven. Earlier blanket no-delete notes remain binding for these protected originals;this explicit authorization permits only the verified redundant source cache. Single writer,NOT-INSTALLABLE,weapons/Session paused andno deployment remain unchanged.

## Stage28 scoped Zicai Curios candidate checkpoint, 2026-11-13

- The original Curios silence helper,rules andChoice are ported as three added classes;all1451existing classes andall resources stay byte-identical to Stage27. Candidate SHA256 c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e. NOT-INSTALLABLE andnot deployed;both formal instances remain read-only.
- Effective unchanged-Stage27 baseline404checks demonstrates two gaps. Candidate basic contract1099checks passes. Valid original boss_relic native death/respawn/pre-expiry logout589checks passes withglobal exactly-one item checks. Invalid gold-nugget head fixture strict failure remains;unchanged Stage27 also reproduces count2. No assertion was relaxed andno failed world was reused.
- Native new-JVM login remains UNACCEPTED. V7 bounded failure andV8 public read-only screen diagnostics stop before any integrated server/player at experimental-world BackupConfirmScreen. No warning/private callback/data flag was bypassed. Supported UI input is unavailable in this run. Continue independent fresh-world slot-removal/occupied-slot inventory fallback andoriginal owner/eligibility controls;complete actual reload confirmation separately.
- Local content-addressed archive15497files/2221unique blobs passed complete hash readback;all originals retained. See github-sync/checkpoint-boss-stage28-zicai-20261113.json andwork/session-boss/ZICAI_CURIO_SILENCE_STAGE28.md. Full lifecycle matrix/full Boss/authenticated multiplayer/rewards/battle remain incomplete. Weapons/Session remain paused andthe separate weapon recovery JAR is not installable.
- Source persistence uses the existing safe branch/subdirectory. Preceding remotely recovered checkpoint head4caf6c1d9799eb443ff0058941bbd38dc7e988ee preserves original main/README. Review,push andfreshly retrieve this bounded source increment;raw binaries/worlds/logs/archives/private state remain local. No large-artifact remote recovery orlocal deletion is authorized by source-only proof.

# NeoForge continuation on C

## Latest verified GitHub source persistence and next Boss work, 2026-11-13

- The user explicitly confirmed commit author identity. Three reviewed commits are now pushed to codex/neoforge-source-20261113;source import head 204b13de017c6cb2c8b6e06513f4436e550ff412,tree 39a10e211d086135248cdceb405a01cb09f8f145. Original main 27d20d5e1d2eb960f4a64cde9bd21230efae766c andREADME remain byte-identical. No force push. Earlier pending-author/URL notes below are historical andsuperseded.
- Fresh independent remote retrieval verified every one of1903source files plus the1904th import manifest against original SHA256;git fsck passes. github-sync/checkpoint-first-push-20261113.json records the scoped proof. Raw archives,JARs,worlds,SDKs andprivate/account/session data were not uploaded;large-artifact recovery remains unproven. Delete no local evidence on the basis of this source-only proof.
- Direct Git HTTPS was intermittent;using the existing credential-free Windows system proxy for individual Git operations succeeded. No global proxy/credential settings changed andTLS verification stayed enabled. Use the existing Credential Manager account;do not request or store passwords/tokens in source.
- Resume Boss work with an actual effective baseline for original Zicai Curios silence:positive damage andeligible owner,100natural ticks,noncosmetic equip denial andlossless logout/death/respawn/slot-missing restoration. Raw missing classes alone are not acceptance proof. Preserve original behavior andall failed evidence.
- Both formal instances remain read-only with no further deployment;Stage27 stays SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0. Full Boss product remains incomplete/NOT-INSTALLABLE;all weapon andSession development/validation stays paused,same sole C writer. Future accepted increments require source review,push andfresh recovery proof.

## Latest supplied GitHub target and authentication gate, 2026-11-13

- Existing target supplied directly: https://github.com/qyby123456/ft.git. Read verified;public repository,default main,one existing commit27d20d5e1d2eb960f4a64cde9bd21230efae766c andone README. Preserve that history and README;do not rewrite/force-push main. Authorized safe import branch codex/neoforge-source-20261113 and explicit neoforge/ftboss subdirectory.
- Clone for persistence only is github-sync/repositories/ft,ignored by the sole C source workspace. No workspace/writer handoff. Both formal instances remain read-only;no further deployment. Stage27 SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0 stays unchanged;full Boss product remains NOT-INSTALLABLE,weapons and Session paused.
- First no-account-hint dry-run failed to obtain a username;the existing Credential Manager account matching the directly supplied repository owner was then selected locally. IPv4 receive-pack dry-run now succeeds(exit0);an intervening HTTPS failure is retained. No tokens were printed/copied andno global credential settings changed. Actual remote branch write/recovery is not yet proven. Git user.useConfigOnly author check fails:name/email andauthor environment are unset. Current sole blocker to committing is explicit author name/email;do not invent it or unnecessarily request replacement credentials. No commits,pushes orremote changes yet.
- Native Git ignore audit14controls passed. Old extension-based scan missed19Git-eligible files;new verifier enumerates actual Git candidates,parses JSON andcan scan exact staged blobs.13detector controls and2exact-review hash/line negative controls pass.Three original matches were AST-reviewed deployment-intent narrative labels or synthetic detector-control text;only exact source hashes/lines are excepted.No file-wide bypass;failed first scan retained.
- Preserve exact source bytes in Git via * -text attributes andverify every staged blob SHA256 against original source manifest. Keep source/config/test/reviewed sanitized manifests only;no raw evidence/account/session/private configuration,D history,formal data,cache,worlds orlarge ordinary-Git artifacts. Large recovery artifacts await authenticated supported LFS/Release review;delete nothing before proven remote recovery.


## Latest GitHub persistence instruction and deployment prohibition, 2026-11-13

- Latest relayed user requirement requests GitHub as the main durable store for reviewed NeoForge source/history/checkpoints. Preserve this sole C writer. It expressly prohibits further deployment and formal-instance overwrites;both formal instances are now read-only. Existing Stage27 was deployed/closed before the new restriction and remains SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0. Do not revert or replace it.
- Current workspace/parent/work/session-boss/session-lease have no Git repository or remote. Git is installed;GitHub CLI is not on PATH. Target repository URL is missing,so remote visibility,permissions,branch history,LFS/Release support and authorization remain unverified. No Git repository,branch,commit,push,upload or public repository was created. Report this actual missing target;do not guess credentials or inspect private launcher/account stores.
- Root .gitignore/.gitattributes and github-sync/GITHUB_RESTORE_AND_SYNC.md prepared. Initial text source/config/script/doc scan1873candidates found0high-confidence pattern hits;this is initial scanning,not complete publish clearance. Generated/runtime/raw receipt/account/session/private configuration data and binaries are excluded from ordinary Git. Review necessary exports individually;never stage the entire tree blindly.
- Keep all original archives,failed evidence and active inputs local until exact remote retrieval/hash/recovery passes. Large essential artifacts require the existing remote supported LFS/Release channel;no oversized ordinary Git or implicit public repository. No local file may be deleted merely to assume GitHub recovery. Closed immutable JAR dedup retained339paths/88contents and reclaimed about3.4GiB with all path/content hashes preserved;no worlds,formal products,weapon workspace or D history changed.
- Stage26 and27 verified increments/main+closeout archives remain scoped accepted.Full Boss goal active and full product NOT-INSTALLABLE;weapons/development/validation/lease/payment/Session paused.Stage13excluded. After GitHub is configured,each future feature must pass focused validation,secret/staged review,commit/push/hash/test/next-goal recording and remote recovery verification before any permitted local cleanup. Current deployment prohibition persists.


## Latest deployed stage27 original erosion damage and category, 2026-11-13

- Deployed SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0. Only two instruction references in ShadowErosionEffect changed:original BENEFICIAL category and ErosionRules.unscaledDamage(4*(amplifier+1)),clamped4..20,instead of obsolete quarter multiplier.Whole original constructor,tick anddamage graphs exact with explicit official-name/boolean-return platform adaptation;2whole original pure damage helper graphs exact.1450other classes/all resources byte-identical,1451totalclasses.RawBoss/controllers/plugins/verification andall prior retirement classes unchanged.
- Baseline 358checks/candidate 331checks;each14actual admitted NoAI cows and1real vanilla integrated-login player,8amplifiers and6duration boundaries,34natural incoming damage events.Native healthdelta,exact20tick cadence andnatural expiry;actual local client live effect/category/health/expiry synchronization.Baseline35original-contract gaps,candidate0;fresh worlds normalstop/save/exit0.
- 4methods ASMverified,2other native methods preserved,4negative controls,doublepatch rejected.1000max-health,food10/platform/no-gravity public fixtures;no manual Entity.tick/effect callback,synthetic player/private bridge/reflection.No framebuffer/Boss renderer,authenticated dedicated multiplayer,natural fullBossbattle or formal full-pack launch acceptance.Stage13excluded;weapons paused,separate recovery addon noninstallable.Forge/historical D read-only.Full Boss goal active.
- Main archive3920files SHAc3131850489ad5b85d2f174f92b8c9aeb50d958837c68514574780d9b31221f1 fully readback/hash verified.Stage26rollback preserved.


## Latest deployed stage26 original shadow and buff retirement, 2026-11-13

- Deployed SHAb56e786c97dc9ed903abc851a4ffd0c21977746a51e1dfafcab19e634437a0a6. Restored exact original retired ShadowBlindEffect apply/remove/synchronize,whole6method ErosionFilter,5instruction spell-effect early return and7retired PlayerBuffGuard public methods/four constants(ENABLED=false).17original mapped method bodies;3RegistryObject.get and2effect casts adapted to1.21Holder API.Three existing classes changed;1447other classes/all resources byte-identical,1451total classes.RawBoss/controllers/plugins/authentication and verification guards unchanged;obsolete buff purging intentionally retired to original behavior.
- Native baseline203checks/6original-contract gaps;candidate470checks/zero failures,20real naturally ticking integrated-player cleanup/removal and actual local client state synchronization cases,75public erosion controls,9retired buff predicates,1public sweep control,6nonplayer duration cases.Infinite speed and40000tickResistanceV admitted and retained in all20player scenes.206natural serverPost,202clientticks. Accepted worlds fresh,normalstop/save/exit0;no synthetic player/private bridge/reflection.
- 54methods ASMverified,37other methods preserved,4negative controls;205levels original/current strength exact float bits each.V2failed infinite-marker admission is retained;authoritative original audit proved obsolete PlayerBuffGuard retirement,restored without relaxing infinite-duration assertions.Split-package startup failure and incorrectguard proof also retained;no failed world reused.
- Public suppression API and exact original spell guard proven;not claiming observed natural Boss spell/AI/fullbattle,framebuffer darkness/expiry,authenticated multiplayer or full formal-pack launch.Stage13excluded.Full Boss goal active;weapons paused,separate recovery addon noninstallable.Forge/historical D read-only.
- Main archive7763files SHA1ee076658e99e5c93371acf8027ad61a81d066d175bfd2bf43ae3377a40c4f5a fully readback/hash verified.Stage25rollback preserved.


## Latest deployed stage25 original shadow spell readable lifetimes, 2026-11-13

- Deployed SHA7eb3b4c2e4ef8e35c806a4df8b53cb32879a9cec9abee961a963bf558d39d7b6. Restored exact original ArcaneReadability helper bytes and three original return-wrapper instructions in one enum method.Only one existing enum class changed;1448other current classes/all resources byte-identical,1450total classes.Raw Boss/controllers/plugins/guards and all existing body/light/motion/timeline implementations unchanged.
- Entire288row pure original oracle exact;5restored lifetimes:shadow_brand CHARGE10to20,RELEASE/PULSE22to60;void_echo andnightfall_blades CHARGE10to20.All283other rows,spell metadata/ordinals/attachment unchanged.9methods ASMverified,6otherenum methods preserved;wrongreceiver/helper/preimage3controls rejected.
- Native baseline 2166checks/candidate 2166checks;each72spells/fourphases/288actual registry packet codec roundtrips and288public SpellVisuals.show calls with actual admitted naturally ticking NoAI Boss.Baseline5gaps,candidate0;both fresh worlds normalstop/save/exit0. No authenticated/recipient fabrication or actual client rendering/expiry acceptance.
- Stage24 supplemental natural phantom timeline remains scoped accepted:12cases/2896checks/1207Post,13captures/18payloadcodecs/8damagehelpers;original continuation/queue/invalidation/fallback/discard/dimension rules.No full natural AI/battle,persistence/death/private cleanup claim.
- Main archive934files SHAc02d09eee4afa10a7b251e66f2fe48e1cabebb731e470b5a14ad68f1bf48c08e fully readback/hash verified.Stage24rollback preserved;failed dependency filename selector recorded(no compiler/server start).Weapons paused,separate recovery snapshot noninstallable,Stage13excluded;Forge/historical D read-only.Full Boss goal active.


## Stage24 supplemental natural phantom timeline accepted, 2026-11-13

- Exact deployed Stage24 JAR remains SHA4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0. No runtime code change or replacement deployment.12real admitted Boss cases,2896checks,1207natural Post callbacks;13seeded captures,18actual original light payload/codec roundtrips,8native living damage helper calls.All replay due exactly capture+84,15tick duplicate tail.26public pure rules each pass original and deployed.
- Normal/impossible Cross/Cage,original NoAI/controller-cancel continuation,two-entry queue,variant/giant invalidation and reenable,target eligible fallback/removal,owner discard,real public dimension transfer.No synthetic tick/restore/replay/PhantomHooks invocation. Public controller/RNG/platform/health fixtures documented.No full natural AI/battle,authenticated/client,multiplayer,persistence/death or private WeakHashMap cleanup claim.507file archive fully readback/hash verified: archives/boss-stage24-natural-phantom-timeline-20261113.zip.
- Weapons paused,recovery addon noninstallable;Stage13excluded,Forge/historical D read-only.Same sole C writer.Full Boss goal active.Next audit remaining original shadow readability/effect contracts.

## Latest deployed stage24 original ManorFive body chain, 2026-11-13

- Deployed SHA4faa8fcb3e0746bfed8176a2c51f0e2e3a0fd13ec7592cd199e016399c203bb0. Entire original26instruction strike dispatcher,34remaining startup instructions,exact original cast-start callback and Xuanlan native-cue setter,entire original finish restored.Two current class entries changed,two classes and pinned3method binary template added,1449classes.Raw Boss and all old installers/guards unchanged;existing original hitShape/echo reused;late installer after Stage22verification,no extra Mixin marker/data IDs.
-45original helper operation/control-flow graphs equivalent with local argument/store provenance;two proven compiler copies andone exact capture normalization,four wrongreceiver/guard/key/local controls rejected.Strict complete before/after/template SHA guards,two wrong owner/preimage controls,idempotence;374effective methods ASMverified,369other effective methods preserved,63original helper descriptors linked including inherited members.
- Baseline4089mainchecks;candidate4101main+1106body+36vanillaFX checks,327existing motion helper controls.26casts/13skills/2difficulties/74mainCowhits/identical damage totals andtiming;76actual body/cue native codecs assigned to registered server mirrors,62lightcodecs,28motion states,8cancellations,846natural Post callbacks.
- Six actual Shadow cast-start teleports/original particle/sound geometry;two public-seeded actual phantom captures.Two public restore/replay cases use those actual snapshots and original single hitShape,25percent helper input41.0625 versus final healthdelta10.265625after unchanged downstream processing.Immediate original capture transfer/clear observed.Not natural phantom countdown/AI,client delivery/rendering,authenticated/multiplayer/fullbattle acceptance.Directed platform/RNG/controller/health/hurt-cooldown/server-mirror fixtures documented.
- Archive3285files SHA22124a74501091376015dc3d23ec529055a9b9861e96cfac9c7de0ccfc2fcabc fully readback/hash verified.Stage23rollback preserved.All original-shape/capture/single-pulse/final-damage fixture errors andstatic proof failures retained,never seeds.Weapons paused,weapon recovery addon noninstallable;Stage13excluded,same sole C writer,Forge/historical D read-only;full Boss goal active.


## Latest deployed stage23 original ManorFive light hooks, 2026-11-13

- Deployed SHA97d564e42b907f13a25382e31dff798d6e8899d90d11a9de74c5694504a97c43. Six original startup/pulse callbacks for Dandan,Qingyi,Zheergen;66instructions inserted in two controller methods.Only ManorFiveSkills class changed;all other1446classes/resources,raw Boss/plugin/installer/guard bodies unchanged.Six original helper operation/control-flow graphs and immutable pure center match;original argument binding and light/motion order checked;wrong helper operand rejected;25methods ASMverified.
- Baseline3340checks shows missing main controller sends.Candidate3920checks validates58native immutable light payloads and58actual codec roundtrips,identity/dimension/time/geometry/kind/phase/duration/index.26casts/13skills/2difficulties/74Cowhits,damage totals/timing exactly match baseline;327existing motion helper checks and28motion states retained over840natural Post callbacks.
- Public directed controller/grounded/health/hurt-cooldown fixtures;no recipients/network delivery/client rendering/authenticated/natural Boss AI/fullbattle acceptance.No player/channel/auth fabrication;all original cosmetic channel,distance,eligibility/failure gates unchanged.Other original body/echo and actual client effects pending.Stage13excluded,weapons paused,recovery addon noninstallable.
- Main archive1220files SHAa8000d2d8973b4b49e29d5dd97bc35f398c30f0f952fedc61690f6f079a59d8c fully readback/hash verified.Exact Stage22 rollback preserved.V1observer Mixin package startup error retained,not accepted/seed;V2isolates observer mixins;bothaccepted worlds normalstop/save/exit0.Same sole C writer;Forge/historical D read-only;full Boss goal active.


## Latest deployed stage22 original Dandan motion lifecycle, 2026-11-13

- Deployed SHAa310afc254a748baecf19b2258af45a667ab55cf89081ea02fe27e8c991f2f9a. Four original calls restore start/pulse/finish/carry,17original lifecycle instructions.12in two controller methods,5in two effective entity methods via one guarded installer appended to original Boss plugin branch after all previous verifications.Only two existing class entries changed,one class added.Raw Boss bytecode,original installers/guards,original callbacks and dynamic mixin list unchanged.No extra marker Mixin.33raw/339effective methods ASMverified;all previous executable instructions retained.
- Baseline2363checks and327existing helper/packet controls pass;candidate2475checks observe28Dandan phases over26casts/13skills/2difficulties/74actualCowpulses and completion/interruptions over840natural Post callbacks.12original helper operation/control-flow graphs equivalent,2branch/key negative controls;late wrong-owner/preimage2controls and idempotence pass.No new dataIDs.
- Directed public controller/grounded/health/hurt-cooldown fixtures and native packet/server-mirror scope only;not authenticated/client rendering/natural Boss AI/full battles.Raw missing-contract lists and intermediate Mixin exports do not establish runtime absence.Actual Mixin target/order is required authority.Earlier fixture/preimage/marker-order failures and failed-closed diagnostics preserved,never seeds or deployments.
- Archive3115files SHA2c6c13bcca163fe76d6c4bb960239559e6b6498f487408987bdd6d16c1261af1 fully readback/hash verified;exact Stage21 rollback preserved.Remaining original echo/body/FX,authenticated/rewards/multiplayer/full battles pending.Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.


## Latest deployed stage21 original ManorFive cast timing, 2026-11-13

- Deployed SHA21af23af5246b284bd5939b9b46735c1a61fc69627a9b86d6811c8c91d0d329d. Exact6original instructions restore nested CastReadability/ManorBalance age hooks;24other methods and every other class/resource byte unchanged.25ASM verified,3original pure helper bodies equivalent and9public helper descriptors linked. Stage20airborne-selection expression preserved.
- Native baseline212checks identify6timing differences across13skill types in normal/impossible. Candidate2233checks exercise all26casts and74actual Cow pulse hits,effects,public animation timing,completion,outside controls and interruptions over840natural ServerTick.Post callbacks. Public grounded/health/hurt-cooldown fixture;no manual Entity.tick,private reflection,authenticated,natural Boss AI/full battle acceptance claimed.
- Archive862files SHA18e525a45654dfee8f8fff3d970e30e4ebf182705f32122eaec6b5e9d0e338f1 fully readback/hash verified;exact Stage20 rollback preserved. Outer matrix runner inherited scope text is inapplicable to restored timing;actual inner probe/source/feature scope is authoritative. Remaining original echo/body/FX and authenticated/rewards/multiplayer/full battles pending. Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.


## Stage22 archived native common dependency cache extension, 2026-11-13

- After verified Stage22 archive,216additional archived common mod paths share the same27immutable C-local files;540cumulative archived aliases.All original paths/bytes/SHA256 and all8native report input references reverified.Worlds,sources,formal JARs and historical D unchanged.Cache and aliases read-only;never edit in place.New runtime copies must be independent. See reports/boss-common-mod-cache-dedup-stage22-20261113/dedup-receipt.json and fully readback-verified maintenance archive.BossStage22remains current;goal active,weapons paused.

## Stage24 archived native common dependency cache extension, 2026-11-13

- After verified Stage24 archive,297additional archived common mod paths share the same27immutable C-local files;837cumulative archived aliases.All original paths/bytes/SHA256 and all10native report input references reverified.Worlds,sources,formal JARs and historical D unchanged.Cache and aliases read-only;never edit in place.New runtime copies must be independent. See reports/boss-common-mod-cache-dedup-stage24-20261113/dedup-receipt.json and fully readback-verified maintenance archive.BossStage24remains current;goal active,weapons paused.

## Local archived native common dependency cache, 2026-11-13

- 324archived Stage19/20/21common mod paths now share27immutable C-local cache files;all SHA256 contents and frozen native input references reverified. All aliases/cache read-only. Do not edit linked archived JARs in place;use a separate new-runtime copy from frozen restore archives. Worlds,sources,formal Boss/Forge JARs and historical D untouched. About1.6GiB reclaimed. See work/session-boss/LOCAL_IMMUTABLE_MOD_CACHE.md and maintenance receipt/archive. Full Boss goal active,weapons paused.

## Latest deployed stage20 original ManorFive airborne selection, 2026-11-13

- Deployed SHA775986661eacd6686431e21d0779bd67bba3f51e2569891a742236850f0ef30f. Exact5original instructions restore airborneFive after select in tryStart;24other methods and all other class/resource bytes unchanged.25ASM verified,7actual helper descriptors linked;2helper bodies equivalent and8195public pure ranged selection cases pass. Original grounded identity and no-ranged fallback preserved.
- Native baseline470/candidate486checks across all52grounded/airborne rotation slots and6variants restore12choices,including8empty airborne casts.34candidate casts observe actual public animation duration and shape cue. Public setOnGround state fixture,not natural flight/physics/authenticated or full Boss AI battle acceptance. Stage19damage/effects evidence reused only for unchanged24non-entry methods;not a new combined battle claim.
- Archive871files SHAc659cbe69a80a485d4610aca45087d8563412e7bf85ecf0b202823be11b74c79 fully readback/hash verified;exact Stage19 rollback preserved. Failed helper textual proof retained;no candidate produced from it. Original remaining timing/echo/balance/body/FX and authenticated/rewards/multiplayer/full battles pending. Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.


## Latest deployed stage19 ManorFive Living target boundaries, 2026-11-13

- Deployed SHA15feeefbbed6dbd4d488b721b8b2e34562a1e0fbeb3a2eb844a6d80da6bc6bf9. Original policy,Living target roles,nearby combat query and UUID resolution restored in five methods;20 unrelated methods and every other class/resource byte identical.25ASM verified,all6public helper descriptors linked. Existing numeric/cadence/shape/FX instructions preserved.
- Native baseline55 and candidate entry61 pass across six variants,including intentionally empty rotation slots and shape-specific cues. All13 current expanded skills pass 1116 checks on 425 natural server Post callbacks,with actual admitted Cow pulse damage/effects,outside controls and interruptions. Public health and hurt-cooldown restored between directed pulse assertions. No manual Entity.tick,private reflection,authenticated or natural Boss AI/full battle acceptance claimed.
- Archive3288files SHA442ce0d653fb94ff7a7b584b1f4e4f7b72c021e5b6cb84085425675b3c084d7e fully readback/hash verified;exact Stage18 rollback preserved. Failed null-slot/cue assumptions and actual resolve-descriptor link failure retained,not accepted or used as seeds. Original remaining echo/airborne/balance/body/FX and authenticated/rewards/multiplayer/full battles still pending. Stage13 excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.


## Latest deployed stage18 Gongdi original public contract, 2026-11-13

- Deployed SHA5aabc09b09e836eee820bda998077561b6fbe7b5a6d9673e80e6ca0d3f9a0cfa. Exact original public Living onSignatureCleaveHit overload added;6shared ConstantValue fields restored to original. All297 previous entity methods and9rule executable methods unchanged,298ASMverified;all other class/resource bytes identical. Original melee/executeCleave Player restrictions intentional and unchanged;existing inlined consumers not rewritten.
- Native32checks show six actual admitted Cow hurt calls followed by original public hook,armor shred1/2/3/4/5/5 and full blood rage. Stage17entry28/skills132/lifecycle35 reused only through exact relevant method/class identity. No manual Entity.tick,private reflection or authenticated/natural Boss AI acceptance claimed.
- Archive453files SHA57606ffaa378d74e37735b509fcc498de7ecda779561ff4150d13c34803e3edd fully readback/hash verified;exact stage17 rollback preserved. Stage13 excluded,weapons paused,recovery addon noninstallable. Original ManorFiveSkills/other behavior and authenticated/reward/multiplayer/full battles pending. Full Boss goal active,same sole C writer;Forge/historical D read-only.


## Latest deployed stage17 Gongdi Living target controller, 2026-11-13

- Deployed SHA021de8b498baf3d215220ca2399e68471864872a7be75b775bb6ba3e588d7162. Original controller restores actual Living targets,bleed identity/ranges/rotation/cue cancellation and original cooldown helper. Two original Living hooks added;all295 existing entity methods unchanged;297 ASM verified. Three previous classes changed,one added;other entries identical.
- Native entry baseline25/candidate28,skills132 checks/93 natural server Post callbacks and lifecycle regression35 pass. Actual original pure-rule method bodies16 equivalent. No manual Entity.tick,reflection,fake player/authenticated encounter claimed. Prior failed pull fixtures preserved;no failed-world seeds.
- Archive2480files SHA5708ea1f23d6ca2a0241067552843acfc61f468613e3e0a72c2be375951420eb fully readback/hash verified. Exact stage16 rollback preserved;stage13 excluded,weapons paused,recovery addon not installable. Shared legacy constants,entity melee/cleave Living paths and full authenticated/reward/multiplayer/battle gates remain. Full Boss goal active,same sole C writer;Forge/historical D read-only.


## Latest deployed stage16 Gongdi lifecycle, 2026-11-13

- Deployed SHAcb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d. Original helper and five entry/reset guards restored. Existing instructions preserved in modified methods;305 unrelated methods identical,310 methods ASM verified. Native baseline35/candidate35 verify correct eligible release,wrong variant/mini/noAI/dead start refusal,idle finish,interrupted cancel/reset and foreign-level/null-target controls. No manual Entity.tick,private mutation or authenticated player/session.
- Stage16 archive1263files SHA78f2ac0637cf9ffcb60fbf4f180e5d350d2e330d30463c548603b49e2f678745 fully readback/SHA verified. Exact stage15 rollback preserved;stages14/15 remain included. Stage13 excluded. Prior failed fixture preserved.
- Full Boss goal active:original general Living-target/skill parity,authenticated entry/rewards/multiplayer and full battles pending. Weapons paused,recovery JAR noninstallable,same sole C writer. Forge/historical D read-only. Next actual primary Gongdi target/skill audit before native gap work.


## Latest deployed stage15 Xuanlan limits, 2026-11-13

- Deployed SHA92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4. Adds only original XuanlanEncounterLimits helper with NeoForge API/event names; every1443 previous class/resource byte remains identical. Native baseline86/candidate101 checks verify per-owner variant6 cap15,other-owner/variant controls,global-level admission,discarded-slot reuse,natural25post callbacks and oldest15 pruning,and actual public support owner transitions. No manually invoked Entity.tick or synthetic fight/session authority.
- Stage15 archive1252files SHA9241511925202e10f9f79c1e8ba09332db839417532a015994390d214167bc36 fully readback/SHA verified. Exact stage14 rollback preserved. Stage14 resource release and18164-file archive remain accepted. Stage13 excluded and undeployed. Original failed baseline preserved.
- Full Boss migration/deployment goal remains active. Authenticated active fight/reward/multiplayer,full battles and remaining behavior still pending. Weapon work paused,recovery JAR noninstallable. Same sole C writer,historical D and Forge read-only. Next compare actual Gongdi lifecycle primary instructions before native gap validation.


## Latest deployed stage14 resource increment, 2026-11-13

- Deployed resource-only stage14 SHA6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c is based on stage12. Every existing1443 class and resource byte stays identical. Adds314 original Boss resources including21 OBJ namespace ports. Stage13 classes are excluded and undeployed.
- Actual exact-JAR native evidence:96 Manor body views,40 non-Manor views,44 statue views and21 baked OBJ models. Focused4-view v8 proves complete reset after238 natural expiry ticks; V5 reset green flag is invalidated by actual image review. Archive18164files SHA09eed5d380cc659c0bb5e57b40bc191fa930749e44aed518e1369d4f8bc16886 fully readback/SHA verified. Exact stage12 rollback preserved. All negative fixtures retained.
- Full Boss goal remains active:authenticated entry/rewards/multiplayer,complete battles and original behavior remain pending. Weapons paused,recovery JAR noninstallable,same sole C writer. Next actual native Xuanlan cap15 baseline before product change;historical D and Forge read-only.


## Latest verified native client FX checkpoint, 2026-11-13

- V14 uses the stock public createFreshLevel flow with a new flat world. It received all 396 original dust packets and passed a real front framebuffer review with both dust colors in a world-only ROI; the baseline must show visible sky and exclude the Boss HUD. V13 had a misleading green broad-red-pixel flag and was explicitly invalidated after visual review; preserve it, never cite it as visible particle acceptance.
- Native complete save/exit0 and all frozen input hashes pass. Reuse the earlier 315 checks and 66 natural ticks. New authority: work/session-boss/candidates/boss-core-stage13-client-native-fresh-world-20261113/acceptance.json. All owned child processes are terminal.
- Full Boss migration/deployment goal stays active. Authenticated entry/rewards, dedicated multiplayer, remaining GL views/resources/behavior parity and full battles remain pending. Weapon work stays paused. Stage13 remains NOT-INSTALLABLE and undeployed. Same sole C writer.

## Active direct-user Boss migration/deployment goal, 2026-11-13

- The new active goal is to finish Boss migration and deployment. This supersedes the previous one-acceptance batch limit; do not shrink completion to the existing stage13 checkpoint. Continue meaningful Boss work and keep this goal active until full requested acceptance/deployment is verified.
- All weapon development/validation and the old signed-Session work remain paused; its recovery JAR remains noninstallable. Same sole C writer. Preserve all frozen tests and failures.
- Use native public fresh-world creation for actual client FX acceptance; no failed world seeds, no world-version forgery, no private UI callback or fake authentication. Real authenticated acceptance remains separate and unproven. Stage13 stays NOT-INSTALLABLE and undeployed until its required gates pass; deploy accepted increments only with backup/rollback hashes and archive. Historical D and Forge remain read-only.

## Current delegated stage13 single-acceptance checkpoint, 2026-11-13

- Reused 315 checks,66 natural ticks, normal exit/save and the frozen archive without rerunning them. Current authentication readiness is truthfully blocked: no real authenticated client/session was provided. No personal launcher credential stores were read and offline fixtures never establish authentication.
- The scoped actual-client summon packet/front-render result is not_accepted_actual_client_failure_preserved; authoritative evidence is work/session-boss/candidates/boss-core-stage13-client-fx-20261113/acceptance.json. Preserve all preparation/bootstrap failures and exact input hashes. All owned native/supervisor processes are terminal at this checkpoint.
- Keep this same sole C writer, all weapon work paused and its recovery JAR noninstallable. Stage13 stays NOT-INSTALLABLE until complete required acceptance; no deployment. Both formal instances and historical D remain read-only.

## Current direct user: weapons paused; Boss priority, 2026-11-13

- The direct user pauses all weapon development and validation, including lease/payment/signed Session. The persisted Session goal is paused. No automatic reactivation or queued weapon runs. cf217105 whole19 is terminal with an original assertion failure; preserve it and never use its world as a restart seed. Coordinator v2 terminated before dependent stages.
- Continue Boss migration with the same sole C writer. Separate weapon recovery JAR and source/evidence archive are in work/session-boss/archives/weapons-paused-cf217105-boss-priority-v2-20261113. This recovery JAR is NOT an installable NeoForge addon; runnable weapon addon work remains paused.
- Reuse stage12/stage13 evidence. Verify each Boss segment, deploy only accepted increments under the user release instruction with backup and rollback hashes, then archive. Unaccepted stage13 remains NOT-INSTALLABLE. Forge and historical D stay read-only. Preserve Boss base rewards independently of paused weapon growth.

## Live serial acceptance coordinator cf217105, 2026-11-12

- The same writer now runs work/session-lease/tools/continue_native_signed_acceptance_v2_cf217105.py. Its authoritative process/stage record is work/session-lease/reports/native-signed-acceptance-pipeline-v2-cf217105/status.json. Inspect that status and actual process handles first; do not manually duplicate its queued stages. WRITE_OWNER.activeRuntimePid is the initial-run snapshot and may become stale after an automatic child launch.
- It waits for both original whole19 native58040 and supervisor7984 to terminate, validates the exact current candidate/observer/paid-seed hashes, original nineteen assertions and complete original save, archives the accepted world/evidence, then serially starts the prepared new-JVM generation2-to3/replay fixture and original nineteen foreign controls. Each successful segment is archived and read-back/hash verified before the next native stage. Any strict failure stops dependent stages; failed/partial worlds are never seeds. Full six-family/103-variant, multiplayer, Boss and final acceptance remain pending.
- Coordinatorv1 was explicitly cancelled before any dependent native launch to strengthen report-to-current-candidate bindings; its script, negative terminal and cancellation record remain frozen. This did not interrupt or restart original native58040. v2 rejects three historical/wrong-candidate/partial reports in read-only preflight. Preserve all of that evidence.
- Keep the goal active and both formal instances/historical D read-only. A direct pause must prohibit further stage starts: write the coordinator directory's cancel-request.json, preserve the current native process for normal shutdown, and update the explicit goal/owner state. Never treat an observation timeout as terminal or use a stale pid/lock alone as authority to restart.

## Current native material integration checkpoint, 2026-11-12

- Candidateee613dfe focused defect_claw passed the original signed Session assertions: four material writes and nine complete original steps, same lease/generation, authenticated post-combat save, native exit0/stopSent/completeSave. This is focused evidence only.
- Candidatecf217105 preserves original enum ordinals and tightens the full original material mutation caller path. Compilation/ASM,1802 unchanged original methods and the15121-case pure oracle pass. Its unchanged original whole-nineteen run repair-runtime-b3299174 is currently live; inspect its actual terminal outcome before another heavy native JVM. Do not use a partial/failed world as a seed.
- The next prepared runner is tools/run_native_material_proc_nineteen_restart_cf217105.py under work/session-lease. Its accepted complete whole-nineteen seed gate must remain intact. Both formal instances and historical D stay read-only; full acceptance and installable/deployed status remain false.
- Restore checkpoint: work/session-lease/reports/material-proc-native-implementation-checkpoint-20261112/archive-receipt.json;3347 files read-back/hash verified. Earlier paused weapon archives remain frozen. The explicit full goal remains active in this same sole C workspace.

## Latest explicit goal resumption after weapon pause5f283607, 2026-11-12

- The user explicitly reactivated the goal after the pause: genuine non-null lease, repair/payment and complete signed Session, then remaining NFboss final acceptance. Pursue only these named prerequisites in this sole C workspace. The pause archive remains frozen; unrelated weapon gameplay remains paused.
- Both formal instances and historical D remain read-only. No deployment/overwrite, writer handoff, synthesized authority, debugger target invocation, guard weakening, or assertion relaxation.
- Candidate5f283607 original whole-nineteen run is terminal, native exit0 and complete save, with13 complete original Sessions and a strict defect_claw leaf-return failure at sample13. Debugger exit1 is a separate known diagnostic limitation. Preserve both; never use this failed world as a restart seed. Continue from the genuine paid849680bb seed and exact current graph only.

## Current direct user pause overrides persisted Session goal, 2026-11-12

- Pause weapon development and validation immediately. The previous persisted signed-Session prerequisite goal is explicitly paused by the current direct user instruction; do not resume it automatically. Preserve candidate5f283607 and failed/partial runtime evidence. The separate weapon JAR is a recovery snapshot, not an accepted runnable addon.
- Continue only Boss migration in this same sole C workspace. Request normal shutdown of the in-flight isolated weapon observer; preserve its partial prefix and eventual terminal outcome without converting it into acceptance or a restart seed. No new weapon runs.
- Keep both formal instances and historical D read-only while finishing the remaining Boss candidate acceptance. Never deploy an unaccepted candidate. Archive each completed Boss segment and preserve rewards/base item ownership independently of paused weapons.

## Latest explicit user goal continuation after stage13, 2026-11-12

- The current user-provided objective explicitly requests genuine non-null lease, repair/payment and complete signed Session as prerequisites for NFboss migration. Pursue those named prerequisites in this same sole C workspace. This explicit objective supersedes the older local pause restriction only for its named prerequisites; unrelated weapon gameplay remains paused and all pause archives remain frozen.
- Both formal instances remain read-only. Do not deploy or overwrite either instance or hand off/switch the sole writer. Reuse frozen stage12/stage13 Boss and weapon archive evidence.
- Start from frozen candidate8199dcc2 and the original failed whole-nineteen Session. Capture the actual first domain retirement through a read-only exact-instance watchpoint; never reset retired state, fabricate lease/payment authority, invoke target methods from the debugger, weaken stack/value/caller gates or relax original assertions. All original restart, foreign-control and final acceptance requirements remain pending.

## Latest direct user priority: weapons paused, Boss only, 2026-11-12

- The current direct user explicitly pauses the weapon system. Do not reactivate weapon development, validation, lease/payment or signed-Session work from a persisted older goal or an automatic continuation. Preserve all failed evidence; resume only on a new direct instruction.
- Continue Boss migration in the same sole C workspace. The weapon JAR is a separate recovery container, not an accepted installable addon. Keep that distinction explicit. Reuse accepted stage12 Boss evidence and archive each new completed segment.
- Keep Forge and historical D read-only. Never deploy unaccepted candidates; full client, authenticated rewards, multiplayer and complete battle acceptance remain pending.

## Latest active goal after stage12, 2026-11-12

- The latest goal explicitly names genuine non-null lease, repair/payment and complete signed Session as prerequisites for NFboss migration. Continue those named prerequisites in this same sole C workspace, preserving the weapon pause archive as frozen history. Do not expand this into unrelated weapon gameplay work.
- Both formal instances are read-only under this goal. No deployment or overwriting, no workspace/writer handoff. Reuse stage12 and earlier frozen Boss evidence rather than rerunning it.
- Start from frozen candidate613d4f6f and its failed original nineteen Sessions. The next gap is recognition of the authenticated actual MixinExtras private putLong implementation; do not glob method names, hardcode ordinals, reset retired domains, fabricate lease authority or relax original assertions. Full original acceptance remains pending.

## Latest direct user instruction: pause weapons, Boss priority, 2026-11-12

- The direct user pauses weapon development and validation. Archive all newer weapon evidence and keep its separate recovery JAR noninstallable. Earlier signed-Session reactivation does not authorize further weapon work under this instruction.
- Keep the same sole C writer. Prioritize Boss migration in single-function increments; verify, back up, deploy each accepted NeoForge increment and archive it. Forge and historical D remain read-only. Never deploy an unaccepted weapon candidate or claim full Boss/client/authenticated acceptance.

## Explicit active goal after stage11, 2026-11-12

- The latest user activates the NFboss migration goal and explicitly names genuine lease, repair/payment and complete signed Session as the next prerequisites. This reauthorizes the named Session work in this same sole C workspace; the earlier weapon pause remains frozen history and does not block this explicit goal.
- Both formal instances are read-only under the latest goal. Do not deploy or overwrite either instance, resume historical deployment permission, or switch writers/workspaces. Stage11 release evidence remains frozen and is reused rather than rerun.
- Start from the frozen 73d460d2 candidate and failed whole-nineteen evidence. Diagnose the actual nested/unbound Map rejection without assertion relaxation, synthesized authority, gameplay changes or failed-world restart seeds. Full original acceptance requirements remain intact; compilation or one successful case cannot establish completion.

## Explicit user pause and Boss priority, 2026-11-12

- The latest user instruction pauses weapon work and validation again. The signed-session goal is paused, not completed. The 73d460d2 supplement is hash-verified under work/session-boss/archives/weapons-paused-73d460d2-20261112; preserve failed evidence and all historical inputs.
- Continue Boss migration in the same sole C workspace. A separate weapon recovery JAR exists but is not an accepted runnable addon; do not install it or resume weapon fixes.
- Complete one focused Boss function at a time. After directed verification, deploy the authorized NeoForge increment with a checked rollback backup, then create a versioned archive. Forge and historical D remain read-only. Full client, authenticated reward and complete-battle gates remain pending.

## Latest delegated scope and acceptance boundary, 2026-11-12

- Keep this thread as the sole writer. Do not hand off or resume weapon candidate validation. Reverify the compressed weapon checkpoint, manifest, hashes, restore notes and owned-process/half-write status before Boss continuation.
- Boss core must retain original Boss rewards and their basic item definitions independently of paused weapon growth/customization. A separate paused weapon snapshot JAR is not an accepted runnable addon.
- Use single-function Boss batches with explicit completion criteria and only necessary compile, directed runtime and ASM checks. Archive outcomes and release owned processes and archived disposable scratch.
- Only fully verified batches may deploy under user authorization. Incomplete acceptance remains NOT-INSTALLABLE and must not overwrite the formal instance. Stage01/02 deployments are historical recorded facts, not proof that complete Boss/client/authenticated acceptance passed. Stage03 reward correction remains undeployed pending authenticated positive kill integration.
- Unrelated user game clients or Python services are not remnants to stop. Process outputs must omit raw command lines and credentials.

## Latest user scope and release instructions, 2026-11-11

- Pause weapon-system functional development and first create a compressed, hash-verified checkpoint with sources, candidates, evidence, failures and restore instructions. Keep original referenced files and historical D inputs intact.
- Prioritize completing the Boss migration. Separate the weapon system into a new JAR; verify the Boss JAR can load independently. Preserve original Boss drops and base item definitions while separating weapon growth/customization services and their UI/protocol/hooks.
- The user now authorizes deployment of completed, verified increments to the NeoForge instance identified in baseline.json, followed by a versioned archive after each completed functional segment. This supersedes earlier blanket no-deployment rules only for these verified incremental releases; it does not authorize deploying an incomplete weapon prototype or claiming final acceptance.
- The Forge instance and historical D workspace remain read-only. Only the active C workspace and explicitly authorized NeoForge deployment files may be written. Back up every deployment target, verify rollback hashes, and keep final acceptance distinct from incremental deployment.
- Current weapon candidate a08a7373 is compile/ASM verified only. Its full signed Session is unaccepted. Preserve the paused weapon checkpoint; resume it only when the user requests weapon work again.

## Explicit user resume override, 2026-11-10

- The latest user instruction explicitly reauthorizes thread 01a1c43e-ae80-7390-b604-b43b4ab53f2f to continue in this same thread after a read-only ownership audit and exclusive FileMode.CreateNew claim. Its historical self-revocation is superseded; other revoked threads remain read-only.
- Keep the current sole writer and continue the active goal: genuine non-null lease, repair/payment and complete signed Session, then remaining final acceptance. Do not proactively revoke or hand off because context is near capacity or ordinary compaction occurs.
- Only unrecoverable systemError, inability to execute commands or an actual engineering safety requirement permits minimal safe stopping. Do not automatically create a successor thread.
- All read-only historical/formal-instance boundaries, frozen-evidence reuse and NOT-INSTALLABLE/no-deployment requirements remain in force. This section supersedes conflicting automatic context-handoff rules below.

## Mandatory single-writer handoff, 2026-11-10

- Read HANDOFF_SESSION_CHAIN.md and WRITE_OWNER.json before any further implementation.
- Every thread listed in WRITE_OWNER.json.revokedWriterThreadIds must remain read-only; automatic continuations cannot restore authority.
- Invoke Python tools with -B before the script argument, or disable bytecode caching before importing historical helpers. Two automatic D-drive cache writes are recorded in work/session-continuity/reports/historical-cache-boundary-incident.json; do not repeat them or clean up D by writing there.
- Thread 01a1ab89-ceb8-7ea0-ae8e-a1e4c83f400f has relinquished and revoked its write authority. It may only read and report; automatic goal continuations do not restore that authority.
- No successor currently owns implementation writes. A new, non-revoked thread must first perform the read-only handoff audit, then exclusively create WRITE_OWNER.claim with FileMode.CreateNew and record its thread ID in WRITE_OWNER.json before becoming the sole writer. An existing claim means remain read-only.
- Continue from the functional candidate in work/session-chain/reports/actual-current.json, rather than assuming historical batch 435 is latest. Reuse unchanged frozen evidence; focus on actual functional gaps and targeted checks.
- On context exhaustion, clear inability to carry the remaining context, or the first compaction failure: stop new implementation writes, safely finish the current command, record processes/failures/partial state and ownership, and hand off without repeated compaction attempts.

- Write only inside this continuation workspace. The D drive migration is a read-only historical evidence input; both formal game instances are read-only.
- Preserve baseline.json and reference snapshots. Do not adopt live drift, deploy, change gameplay values or Independent Event.
- Keep NOT-INSTALLABLE until the complete migration and client/server/authenticated multiplayer acceptance passes.
- Historical receipts retain their original paths and hashes. New cumulative candidates and runtime outputs belong here.
- Preserve all baseline and handoff evidence; document external historical dependencies honestly.
- User scope update 2026-11-10: retain sword, bow, crossbow, axe, trident and staff systems; remove legacy weapon adaptation for other item families and remove the FTBoss food-enchantment system. This supersedes historical preservation requirements for those retired features. Do not change vanilla global enchantment rules.
