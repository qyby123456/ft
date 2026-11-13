## Stage28 scoped Zicai Curios candidate checkpoint, 2026-11-13

- The original Curios silence helper,rules andChoice are ported as three added classes;all1451existing classes andall resources stay byte-identical to Stage27. Candidate SHA256 c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e. NOT-INSTALLABLE andnot deployed;both formal instances remain read-only.
- Effective unchanged-Stage27 baseline404checks demonstrates two gaps. Candidate basic contract1099checks passes. Valid original boss_relic native death/respawn/pre-expiry logout589checks passes withglobal exactly-one item checks. Invalid gold-nugget head fixture strict failure remains;unchanged Stage27 also reproduces count2. No assertion was relaxed andno failed world was reused.
- Native new-JVM login remains UNACCEPTED. V7 bounded failure andV8 public read-only screen diagnostics stop before any integrated server/player at experimental-world BackupConfirmScreen. No warning/private callback/data flag was bypassed. Supported UI input is unavailable in this run. Continue independent fresh-world slot-removal/occupied-slot inventory fallback andoriginal owner/eligibility controls;complete actual reload confirmation separately.
- Local content-addressed archive15497files/2221unique blobs passed complete hash readback;all originals retained. See github-sync/checkpoint-boss-stage28-zicai-20261113.json andwork/session-boss/ZICAI_CURIO_SILENCE_STAGE28.md. Full lifecycle matrix/full Boss/authenticated multiplayer/rewards/battle remain incomplete. Weapons/Session remain paused andthe separate weapon recovery JAR is not installable.
- Source persistence uses the existing safe branch/subdirectory. Preceding remotely recovered checkpoint head4caf6c1d9799eb443ff0058941bbd38dc7e988ee preserves original main/README. Review,push andfreshly retrieve this bounded source increment;raw binaries/worlds/logs/archives/private state remain local. No large-artifact remote recovery orlocal deletion is authorized by source-only proof.

# Boss 优先移植：已部署阶段记录

## 2026-11-13：源码已推送并完成远端恢复校验

三批源码提交已推送到 `codex/neoforge-source-20261113`，源码检查点 `204b13de017c6cb2c8b6e06513f4436e550ff412`。重新拉取的1903个源码文件及1个导入清单全部通过来源SHA256校验，原始main和README保持不变。这只证明源码恢复；大型原始归档、JAR和世界证据仍保留本地，不据此删除。当前两个正式实例只读，不再部署。武器与Session开发继续暂停；下一步验证并移植原版紫菜Boss的Curios饰品沉默，完整Boss验收仍未完成。

## 2026-11-12 第六段：原版全战斗禁飞的末优先级兜底

第六段基于第五段，补回原版 FullFightFlightBan 的 LOWEST 兜底。仅新增一个类，将 Forge 双阶段事件映射为 NeoForge 具体 Pre/Post；原版全部指令、原构造器、优先级和 ManorFlyBan 行为通过定向等价证明，三个方法通过 ASM。1432 个既有类及全部资源字节不变，没有改变伤害、时间、范围、Boss AI 或 mayfly 权限。

两个真实 NeoForge 服务端进程各通过 41 项检查：基线复现普通处理后被 LOW 监听器重授飞行/鞘翅的缺口，候选在 LOWEST 清除；真实 ServerPlayer.doTick 双阶段以及创造/旁观/范围外/无窗口对照全部通过。两次都正常保存退出，输入哈希不变。测试明确使用未认证的 scratch 玩家，不能替代认证多人、客户端或完整战斗验收。首轮抽象事件注册失败单独保留。

已备份第五段并部署第六段，当前正式 SHA256 为 `e4dd3c8d4fff8a66b5eefeac6aa96eb24304cf716e9cd8c5b7424c4ae65c034f`。归档 `archives/boss-core-stage06-flight-guard-20261112.zip`，99 文件全部读回校验，SHA256 为 `0f8b88fa9ef68c3f6e5f53c09871430202789ec432e699349cfec7dc47b0a2d7`。部署、回滚和恢复说明在 `releases/boss-core-stage06-flight-guard-20261112/`。

武器功能已重新确认暂停并补充压缩 dd1ad841 尝试记录，详见 `archives/weapons-paused-dd1ad841-20261112/`。独立武器 JAR 依然只是不可安装的恢复快照。第三段材料修复未混入本次部署，认证玩家正向击杀和已知奖励遗漏仍待解决。完整 Boss/GL/认证多人/战斗尚未验收。用户正在运行的 1.19.2 游戏与两个 Python 服务未动；Forge 和历史 D 保持只读。


## 2026-11-12 第五段：禁疗和暗蚀的原版移除保护

第五段基于已部署的第四段，恢复原版禁疗/暗蚀不可提前移除的规则，以及庄园 Boss 自净的例外。Forge 空治疗物品列表按 NeoForge 接口映射为空 cures；移除事件保留 LOWEST、receiveCanceled 和原版 ManorStatusImmunity.protectRemoval。仅替换两个效果类，其他类、资源、伤害、持续时间及 AI 字节不变；没有混入第三段材料修复。

真实 NeoForge 基线 47 项确认直接移除、清空效果、牛奶/图腾及外部扩充 cure 列表的缺口；候选 49 项通过，同时验证自然到期、到期后恢复治疗、原版效果对照、Boss 入场及自然 tick 自净，包括高优先级取消事件后的原版例外。四个原有方法指令完整保留，15 个相关方法通过 ASM；两次运行均正常保存退出。

已备份第四段、部署第五段并归档 72 文件，全部读回校验。当前部署 SHA256 为 `4f89f69fbc9c90f1b7cd8a29f463568448442c60085538944de199b730fa0016`。部署/回滚/恢复说明：`releases/boss-core-stage05-status-removal-20261112/`；归档：`archives/boss-core-stage05-status-removal-20261112.zip`。

本工程 JVM/Python 已释放，没有部署临时文件或半写入，用户其他进程未动。此前自动审批拒绝的临时目录删除未重试或绕过；四个新隔离临时目录已归档并保留。武器压缩检查点、清单、外部依赖清单和恢复说明哈希再次核对；独立武器 JAR 仍是恢复快照，尚非通过验收的可运行扩展。武器开发保持暂停。

Boss 客户端渲染、认证多人及完整战斗尚未验收，整体不能称完整可安装候选。第三段材料修复仍待认证玩家正向击杀验收，未混入第五段，当前分支的该项奖励遗漏仍需解决。

## 2026-11-12 第四段：庄园 Boss 原版状态免疫

第四段直接基于第二段正式文件，只加入庄园 Boss 对禁疗/暗影侵蚀的原版免疫及入场、自然 tick 清除规则；没有混入第三段尚未通过认证玩家击杀验收的材料修复。原部署的 12 个变体共 24 项基线检查确认缺口；修复后的 40 项真实 NeoForge 检查及 303 方法定向 ASM 通过。仅增加 ManorStatusImmunity 类，并在原 EasterBossEntity.canBeAffected 的三个返回点接入守卫；其余类、资源、战斗数值字节不变。

已备份第二段、部署第四段并归档 74 文件，全部读取校验。第四段部署时的历史 SHA256 为 `c97eb0541bca6235dd5d3c92f5d4658165dc71e07e36a723de96dcbe1d7dc5b3`（当前见第五段）。部署/回滚/恢复记录在 `releases/boss-core-stage04-status-immunity-20261112/`，归档为 `archives/boss-core-stage04-status-immunity-20261112.zip`。

本工程 JVM/Python 已退出，无部署临时文件或半写入；用户其他游戏及 Python 服务未动。此前自动审批拒绝的临时目录删除没有重试；新运行的四个隔离临时目录已归档并保留。Boss 客户端渲染、认证多人和完整战斗仍未验收，不能称完整可安装版本。第三段材料修复仍未部署，当前分支的该项奖励遗漏尚未修复。武器系统继续暂停。

## 2026-11-12 后续：第三段候选未部署

最新委派要求完整验收前不得覆盖正式实例。本轮没有恢复武器候选验证，原武器压缩包、清单、哈希和恢复说明已复核。

核对发现第一/二段拆分把十种 Boss 基础材料及其掉落委托给了暂停武器模块，造成该部分奖励缺失。这是之前拆分的错误，第三段已修正为 Boss 自有基础物品及直接奖励回调。冻结 Forge 原版十个材料 ID 和每次数量 1 均已验证；六个原掉落方法仅材料回调所有者改变，224 个编译方法通过定向 ASM，真实 NeoForge 68 项检查通过，注册为 126 个物品和 49 类实体。

第三段认证玩家击杀正向路径尚未验收，因此候选保持 NOT-INSTALLABLE，仅压缩归档，不覆盖正式实例。当前正式文件仍为第二段，材料奖励缺失尚未通过本候选部署修复。其余原机制、实际客户端渲染、完整战斗与认证多人仍未完成。

第三段候选与恢复说明：`candidates/boss-core-stage03-material-drops-20261112/`；版本档：`archives/boss-core-stage03-material-drops-NOT-INSTALLABLE-20261112.zip`，119 文件全部读取并校验，SHA256 `9e10e1d697c4344814241b9d21c5803ab2caca19923b3658dce9865bd779ed1e`。

本工程 Java/Python 已全部退出，验证服务端完整保存。另一个用户游戏和两个 Agents Anywhere Python 服务未动。临时目录清理命令被自动审批策略拦截且未执行；已归档的 254464 字节临时文件保留，未尝试绕过。最终状态见 `reports/stage03-terminal-cleanup-20261112.json`。

武器系统功能开发按用户要求暂停。完整源码、构建和失败证据已压缩归档；另有独立的武器暂停快照 JAR，保存 966 个已分离类。这是恢复用压缩包，不是可运行的 NeoForge 武器插件，不应放入 mods。恢复开发后仍需完成包名隔离、可选接口接入、注册与完整签名 Session 验收。

第一段：Boss 核心拆分并部署。真实 NeoForge 21.1.233 在没有武器系统类及武器插件的环境下，通过 21 项注册/归属检查。24 个原始庄园 Boss（12 变体×普通/不可能）保持实际 UUID、AI、生命和年龄，完成 660 自然 tick 与 33 次快照。最终共有 1430 个 Boss 核心类；与自然循环包共有的 1428 个类字节完全一致，另外加入客户端预览屏幕及注册类；实际客户端画面尚未验收。完整武器压缩档、暂停快照、原正式 JAR 回滚备份和第一段版本档均已保存。

第二段：恢复原版 HealBanAbsorptionGuard。现有禁疗本来只能拒绝新增恢复效果，未清掉此前吸收生命；本段按冻结 Forge 原逻辑补上效果添加、适用性、自然实体/玩家 tick 和受伤前清理路径，600 tick 禁疗时长不改。真实实体的 21 项运行检查通过，含禁疗前吸收、即时清除、原生受伤、20 自然 tick、未禁疗对照和禁疗解除后的恢复。玩家专属 tick 及认证多人尚未验收。此前 1430 个核心类字节不变，仅增加一个守卫类。

两段都已按“验证→备份→NeoForge 部署→版本压缩归档”执行，归档均逐文件解压读取并校验 SHA256。没有启动正式整合包实例，也没有调整其重复依赖。Forge 当前文件相对历史基线已有外部变化，已只读记录，本轮没有写 Forge 或 D 历史工程。

第二段部署时 NeoForge mods/qys-bosses-1.21.1.jar 的历史 SHA256（当前版本见第四段收据）：
`d78cb4c04428ffd8899e0ceb2fe97754170275fe64d40595e512f6eb31c6f9de`

## 文件位置

- `archives/weapons-paused-a08a7373/weapons-source-evidence.zip`：完整武器暂停档，SHA256 `5fb38194a79abeed7ccb97a7e6127cd765dd7ad7df8bae85730e7df45dc0115f`。
- `releases/boss-core-stage01-20261112/ftboss-weapons-1.21.1-PAUSED-SNAPSHOT.jar`：独立武器暂停快照，不能安装。
- `archives/boss-core-stage01-20261112.zip`：第一段版本档与原正式版本回滚文件。
- `archives/boss-core-stage02-absorption-20261112.zip`：第二段版本档与第一段回滚文件。
- `releases/boss-core-stage06-flight-guard-20261112/deployment-receipt.json`：当前部署精确路径、SHA256、回滚路径及验证范围。

## 继续开发

继续 Boss 原机制逐项比对，不能把“缺类名”直接当成“缺功能”重复添加事件处理器。待完成客户端预览与渲染、技能/战斗、认证多人和正式实例闭环后才可称完整 Boss 移植完成。武器功能、完整签名 Session 和可安装武器附属 JAR保持暂停。


## 2026-11-12 Stage10: original Blood Tide server skill

Added original Blood Tide to the previous Blood Brand controller. Seven directed native scenarios pass in both baseline and candidate (1950 server callbacks per process), including the original 22-tick windup, 12-tick recovery, 220 per-skill cooldown, 80 global cooldown, radius 8, 45-degree cone and phase/AI/range/sight/lost-target guards. Two casts complete, one cancels, and three genuine native damage events match original attribute scaling; Blood Brand retains its 80 first delay and 240 interval. Only two controller class bytes change; all other classes/resources remain identical. Eight methods pass ASM and four unrelated helper methods remain identical.

Deployed SHA256: `6f3482b32f0361c664f5e524d44ee48e3d9965198300ea5b37484e299a57a211`. Stage09 rollback verified. Archive: `archives/boss-core-stage10-blood-tide-20261112.zip`, SHA256 `6c9dc928fcafbb10bde1f77ceef113eae892d7112866933022992aa54550f226`. Both isolated processes exited zero after normal complete saves. Weapon source/evidence and separate noninstallable recovery JAR remain paused in `archives/weapons-paused-73d460d2-20261112`. Forge and historical D were unchanged. Two melee skills, full rotation, summon/client motion, authenticated rewards and complete battle remain pending.


## 2026-11-12 Stage11: original Cleaver Sweep single-gap migration

Stage10 59-check acceptance, normal saved terminals, rollback and archive are referenced directly and were not rerun. Preflight records the archived stage10 early proof-count failure; no owned processes, target temporary deployment files or half writes remained. New baseline and candidate each execute seven directed native scenarios. The original Forge pre-melee skill callsite and genuine native zero-Sweep baseline confirm the selected gap.

Only Cleaver Sweep is added. Original 14-tick windup, 10-tick recovery, 160 per-skill cooldown, 80 global cooldown, radius/cast range 4.5, 60-degree cone, melee damage/source, strong-attack sound at pitch 0.65 and successful-hit knockback 0.45 are preserved. Two casts complete, one cancels and three genuine native hits match original attribute scaling. Cone/radius/vertical, phase/AI/range/sight and target-loss controls pass. Eight related methods pass ASM; six helper/scheduler method bodies and all other class/resource bytes remain unchanged. One controller class changes.

Deployed SHA256 `5378b5657405a14d46fa5b7ad8bad78fd89764c83a9c89d901fbf51789b5e75e`; stage10 rollback verified. Independent archive `archives/boss-core-stage11-cleaver-sweep-20261112.zip`, SHA256 `fc4b91b0aaa20f7ff63069ad0b5a8e57eec1b12fdf66df2d7c11c2f5865e2071`. Both owned JVMs saved normally and exited zero. The first generated build failed its missing-base-path preflight before compilation; the failed tool and correction are preserved in the archive. Weapon work stays paused and its separate recovery JAR stays noninstallable. Bone Crush, full rotation parity, client motion/GL, authenticated reward kills and complete battle are unaccepted. Forge and historical D remain unchanged.


## 2026-11-12 Stage12: original Bone Crush

Restored only the fourth original skill choice. Baseline and candidate each completed seven directed native scenarios with 3670 server callbacks and normal zero-exit complete saves. Candidate passes 78 checks, two complete casts, one target-loss cancellation and five actual native melee hits. Original 20+14 timing, cooldown200, global80, castRange5, circular radius3, vertical2.5, damage28 with original attribute/phase scaling, sound0.65 and knockback0.45 are verified. Earlier three skills retain their exact first delays and the directed four-skill interval is452. Eight methods pass ASM; seven existing method bodies and all other class/resource bytes remain unchanged.

Deployed SHA256 `58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3`; exact stage11 rollback verified. Archive SHA256 `73d4d538d829f4855458e150046db1d2a7b878747d89b450b60824f6c167e6b8`, 161 files read back and hash verified. Weapon development/validation remains paused; latest separate recovery JAR and complete compressed evidence are under weapons-paused-613d4f6f-boss-priority-v2-20261112. It is not an installable weapon addon. Forge and historical D unchanged. Full rotation/fight parity, summon FX, client/GL and authenticated multiplayer/rewards remain pending.


## 2026-11-13 Stage14: Boss client resources

Only314 original assets added to deployed stage12,including21 original OBJ models ported to neoforge:obj. Every existing1443 class and resource byte is preserved;stage13 excluded. Exact-JAR native tests:96 Manor views,40 non-Manor views,44 statue views,21 model bakes,and full native darkness reset after238 natural expiry ticks. Negative fixtures and invalidated V5 reset flag preserved.

Deployed SHA256 `6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c`;stage12 rollback `58002a729b48fba41bd4f4832b967760879114b214b93ff0729dfc8680483dc3`. Archive18164files,readback/SHA verified:`09eed5d380cc659c0bb5e57b40bc191fa930749e44aed518e1369d4f8bc16886`. Forge/historical D unchanged;weapons paused. Full authenticated entry/rewards,multiplayer,complete battles and original remaining behavior pending;goal active.


## 2026-11-13 Stage15: original Xuanlan limits

Adds one original helper class. All1443 prior class and resource bytes stay identical. Native baseline86 demonstrates missing original15 cap;candidate101 verifies owner-specific15 cap,independent second owner,other variant16 control,distant3D admission,discarded-slot replacement,natural every20tick oldest15 pruning,and real public support owner transitions. Registered entities and original native scheduling;no private fight/session authority or manual entity tick.

Deployed SHA256 `92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4`;stage14 rollback `6c8ab5192bd95aa9ec8684211b37c0313a11fae81d19357b350c043ae468e50c`. Archive1252files fully readback/SHA verified:`9241511925202e10f9f79c1e8ba09332db839417532a015994390d214167bc36`. Forge/historical D unchanged. Weapons paused;stage13 excluded. Real authenticated active fight/reward/multiplayer and complete battles remain pending. Full goal remains active.


## 2026-11-13 Stage16: original Gongdi lifecycle

Restores original helper and five cast entry/reset guards only. All old instructions preserved in modified methods;305 unrelated methods preserved,310 ASM verified. Native baseline35/candidate35 checks original eligible start/release,invalid variant/mini/noAI/dead rejection,idle impact refusal,interrupted cancel/reset,foreign-level no-mutation and invalid tryStart return controls. Tests use real public entity/controller methods,not manual Entity.tick/reflection/authenticated players.

Deployed SHA256 cb7bf44402aa18b2d051e8d388352285d2c5015713774dcbf3b0644a19d2d30d;exact stage15 rollback92851246c4ed7c9eb2e2ba3946a4fa74280353d6dc4c50e0e64bcdc25087ffd4. Archive1263files SHA78f2ac0637cf9ffcb60fbf4f180e5d350d2e330d30463c548603b49e2f678745 readback/SHA verified. Full target/damage/cadence and authenticated/full battle remain pending. Stage13 excluded,weapons paused;full goal active.


## Latest deployed stage17 Gongdi Living target controller, 2026-11-13

- Deployed SHA021de8b498baf3d215220ca2399e68471864872a7be75b775bb6ba3e588d7162. Original controller restores actual Living targets,bleed identity/ranges/rotation/cue cancellation and original cooldown helper. Two original Living hooks added;all295 existing entity methods unchanged;297 ASM verified. Three previous classes changed,one added;other entries identical.
- Native entry baseline25/candidate28,skills132 checks/93 natural server Post callbacks and lifecycle regression35 pass. Actual original pure-rule method bodies16 equivalent. No manual Entity.tick,reflection,fake player/authenticated encounter claimed. Prior failed pull fixtures preserved;no failed-world seeds.
- Archive2480files SHA5708ea1f23d6ca2a0241067552843acfc61f468613e3e0a72c2be375951420eb fully readback/hash verified. Exact stage16 rollback preserved;stage13 excluded,weapons paused,recovery addon not installable. Shared legacy constants,entity melee/cleave Living paths and full authenticated/reward/multiplayer/battle gates remain. Full Boss goal active,same sole C writer;Forge/historical D read-only.

Next: Audit original shared Gongdi constants and actual entity melee/cleave Living-target paths before next native behavior increment. Authenticated ingress/rewards/multiplayer/full battles pending;weapons remain paused.


## Latest deployed stage18 Gongdi original public contract, 2026-11-13

- Deployed SHA5aabc09b09e836eee820bda998077561b6fbe7b5a6d9673e80e6ca0d3f9a0cfa. Exact original public Living onSignatureCleaveHit overload added;6shared ConstantValue fields restored to original. All297 previous entity methods and9rule executable methods unchanged,298ASMverified;all other class/resource bytes identical. Original melee/executeCleave Player restrictions intentional and unchanged;existing inlined consumers not rewritten.
- Native32checks show six actual admitted Cow hurt calls followed by original public hook,armor shred1/2/3/4/5/5 and full blood rage. Stage17entry28/skills132/lifecycle35 reused only through exact relevant method/class identity. No manual Entity.tick,private reflection or authenticated/natural Boss AI acceptance claimed.
- Archive453files SHA57606ffaa378d74e37735b509fcc498de7ecda779561ff4150d13c34803e3edd fully readback/hash verified;exact stage17 rollback preserved. Stage13 excluded,weapons paused,recovery addon noninstallable. Original ManorFiveSkills/other behavior and authenticated/reward/multiplayer/full battles pending. Full Boss goal active,same sole C writer;Forge/historical D read-only.

Next: Audit actual original ManorFiveSkills Living-target/constant consumers before next native Boss increment. Original melee Player restrictions intentional and unchanged. Authenticated ingress/rewards/multiplayer/full battles pending;weapons remain paused.


## Latest deployed stage19 ManorFive Living target boundaries, 2026-11-13

- Deployed SHA15feeefbbed6dbd4d488b721b8b2e34562a1e0fbeb3a2eb844a6d80da6bc6bf9. Original policy,Living target roles,nearby combat query and UUID resolution restored in five methods;20 unrelated methods and every other class/resource byte identical.25ASM verified,all6public helper descriptors linked. Existing numeric/cadence/shape/FX instructions preserved.
- Native baseline55 and candidate entry61 pass across six variants,including intentionally empty rotation slots and shape-specific cues. All13 current expanded skills pass 1116 checks on 425 natural server Post callbacks,with actual admitted Cow pulse damage/effects,outside controls and interruptions. Public health and hurt-cooldown restored between directed pulse assertions. No manual Entity.tick,private reflection,authenticated or natural Boss AI/full battle acceptance claimed.
- Archive3288files SHA442ce0d653fb94ff7a7b584b1f4e4f7b72c021e5b6cb84085425675b3c084d7e fully readback/hash verified;exact Stage18 rollback preserved. Failed null-slot/cue assumptions and actual resolve-descriptor link failure retained,not accepted or used as seeds. Original remaining echo/airborne/balance/body/FX and authenticated/rewards/multiplayer/full battles still pending. Stage13 excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.

Next: Audit actual original/current ManorFiveSkills remaining echo,airborne,balance,body and FX instructions and their helper contracts before the next bounded Boss increment. Authenticated ingress/rewards/multiplayer/full battles pending;weapons remain paused.


## Latest deployed stage20 original ManorFive airborne selection, 2026-11-13

- Deployed SHA775986661eacd6686431e21d0779bd67bba3f51e2569891a742236850f0ef30f. Exact5original instructions restore airborneFive after select in tryStart;24other methods and all other class/resource bytes unchanged.25ASM verified,7actual helper descriptors linked;2helper bodies equivalent and8195public pure ranged selection cases pass. Original grounded identity and no-ranged fallback preserved.
- Native baseline470/candidate486checks across all52grounded/airborne rotation slots and6variants restore12choices,including8empty airborne casts.34candidate casts observe actual public animation duration and shape cue. Public setOnGround state fixture,not natural flight/physics/authenticated or full Boss AI battle acceptance. Stage19damage/effects evidence reused only for unchanged24non-entry methods;not a new combined battle claim.
- Archive871files SHAc659cbe69a80a485d4610aca45087d8563412e7bf85ecf0b202823be11b74c79 fully readback/hash verified;exact Stage19 rollback preserved. Failed helper textual proof retained;no candidate produced from it. Original remaining timing/echo/balance/body/FX and authenticated/rewards/multiplayer/full battles pending. Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.

Next: Audit actual original/current remaining ManorFiveSkills timing,echoCast,balance,body and FX hooks and actual helper contracts before next bounded Boss increment. Authenticated ingress/rewards/multiplayer/full battles pending;weapons remain paused.


## Latest deployed stage21 original ManorFive cast timing, 2026-11-13

- Deployed SHA21af23af5246b284bd5939b9b46735c1a61fc69627a9b86d6811c8c91d0d329d. Exact6original instructions restore nested CastReadability/ManorBalance age hooks;24other methods and every other class/resource byte unchanged.25ASM verified,3original pure helper bodies equivalent and9public helper descriptors linked. Stage20airborne-selection expression preserved.
- Native baseline212checks identify6timing differences across13skill types in normal/impossible. Candidate2233checks exercise all26casts and74actual Cow pulse hits,effects,public animation timing,completion,outside controls and interruptions over840natural ServerTick.Post callbacks. Public grounded/health/hurt-cooldown fixture;no manual Entity.tick,private reflection,authenticated,natural Boss AI/full battle acceptance claimed.
- Archive862files SHA18e525a45654dfee8f8fff3d970e30e4ebf182705f32122eaec6b5e9d0e338f1 fully readback/hash verified;exact Stage20 rollback preserved. Outer matrix runner inherited scope text is inapplicable to restored timing;actual inner probe/source/feature scope is authoritative. Remaining original echo/body/FX and authenticated/rewards/multiplayer/full battles pending. Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.

Next: Audit actual original/current remaining ManorFiveSkills echoCast,hitShape and body/FX contracts before next bounded Boss increment. Authenticated ingress/rewards/multiplayer/full battles pending;weapons remain paused.


## Latest deployed stage22 original Dandan motion lifecycle, 2026-11-13

- Deployed SHAa310afc254a748baecf19b2258af45a667ab55cf89081ea02fe27e8c991f2f9a. Four original calls restore start/pulse/finish/carry,17original lifecycle instructions.12in two controller methods,5in two effective entity methods via one guarded installer appended to original Boss plugin branch after all previous verifications.Only two existing class entries changed,one class added.Raw Boss bytecode,original installers/guards,original callbacks and dynamic mixin list unchanged.No extra marker Mixin.33raw/339effective methods ASMverified;all previous executable instructions retained.
- Baseline2363checks and327existing helper/packet controls pass;candidate2475checks observe28Dandan phases over26casts/13skills/2difficulties/74actualCowpulses and completion/interruptions over840natural Post callbacks.12original helper operation/control-flow graphs equivalent,2branch/key negative controls;late wrong-owner/preimage2controls and idempotence pass.No new dataIDs.
- Directed public controller/grounded/health/hurt-cooldown fixtures and native packet/server-mirror scope only;not authenticated/client rendering/natural Boss AI/full battles.Raw missing-contract lists and intermediate Mixin exports do not establish runtime absence.Actual Mixin target/order is required authority.Earlier fixture/preimage/marker-order failures and failed-closed diagnostics preserved,never seeds or deployments.
- Archive3115files SHA2c6c13bcca163fe76d6c4bb960239559e6b6498f487408987bdd6d16c1261af1 fully readback/hash verified;exact Stage21 rollback preserved.Remaining original echo/body/FX,authenticated/rewards/multiplayer/full battles pending.Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active,same sole C writer,Forge/historical D read-only.

Next: Audit actual effective original/current remaining ManorFive echo/body/FX invocation paths,including actual Mixin target/order;do not infer runtime absence from raw class lists. Authenticated ingress/rewards/multiplayer/full battles pending;Stage13excluded,weapons remain paused.


## Latest deployed stage23 original ManorFive light hooks, 2026-11-13

- Deployed SHA97d564e42b907f13a25382e31dff798d6e8899d90d11a9de74c5694504a97c43. Six original startup/pulse callbacks for Dandan,Qingyi,Zheergen;66instructions inserted in two controller methods.Only ManorFiveSkills class changed;all other1446classes/resources,raw Boss/plugin/installer/guard bodies unchanged.Six original helper operation/control-flow graphs and immutable pure center match;original argument binding and light/motion order checked;wrong helper operand rejected;25methods ASMverified.
- Baseline3340checks shows missing main controller sends.Candidate3920checks validates58native immutable light payloads and58actual codec roundtrips,identity/dimension/time/geometry/kind/phase/duration/index.26casts/13skills/2difficulties/74Cowhits,damage totals/timing exactly match baseline;327existing motion helper checks and28motion states retained over840natural Post callbacks.
- Public directed controller/grounded/health/hurt-cooldown fixtures;no recipients/network delivery/client rendering/authenticated/natural Boss AI/fullbattle acceptance.No player/channel/auth fabrication;all original cosmetic channel,distance,eligibility/failure gates unchanged.Other original body/echo and actual client effects pending.Stage13excluded,weapons paused,recovery addon noninstallable.
- Main archive1220files SHAa8000d2d8973b4b49e29d5dd97bc35f398c30f0f952fedc61690f6f079a59d8c fully readback/hash verified.Exact Stage22 rollback preserved.V1observer Mixin package startup error retained,not accepted/seed;V2isolates observer mixins;bothaccepted worlds normalstop/save/exit0.Same sole C writer;Forge/historical D read-only;full Boss goal active.

Next: Continue actual effective original ManorFive body/echo/QingyiCast/Xuanlan native cue integration audit;reuse unchanged accepted evidence. Actual client light delivery/rendering,authenticated ingress/rewards,multiplayer and full battles pending. Stage13excluded;weapon development/validation paused,recovery addon noninstallable.


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

Next: Audit original actual PhantomHooks timeline owner tick/capture/death/cancel/dimension scheduling and other Boss remaining behavior gaps;preserve exact original rules and reuse accepted body/light/motion evidence.Real client rendering/delivery,authenticated ingress/rewards,multiplayer,persistence/full battles pending.Stage13excluded,weapons paused,recovery addon noninstallable.


## Latest deployed stage25 original shadow spell readable lifetimes, 2026-11-13

- Deployed SHA7eb3b4c2e4ef8e35c806a4df8b53cb32879a9cec9abee961a963bf558d39d7b6. Restored exact original ArcaneReadability helper bytes and three original return-wrapper instructions in one enum method.Only one existing enum class changed;1448other current classes/all resources byte-identical,1450total classes.Raw Boss/controllers/plugins/guards and all existing body/light/motion/timeline implementations unchanged.
- Entire288row pure original oracle exact;5restored lifetimes:shadow_brand CHARGE10to20,RELEASE/PULSE22to60;void_echo andnightfall_blades CHARGE10to20.All283other rows,spell metadata/ordinals/attachment unchanged.9methods ASMverified,6otherenum methods preserved;wrongreceiver/helper/preimage3controls rejected.
- Native baseline 2166checks/candidate 2166checks;each72spells/fourphases/288actual registry packet codec roundtrips and288public SpellVisuals.show calls with actual admitted naturally ticking NoAI Boss.Baseline5gaps,candidate0;both fresh worlds normalstop/save/exit0. No authenticated/recipient fabrication or actual client rendering/expiry acceptance.
- Stage24 supplemental natural phantom timeline remains scoped accepted:12cases/2896checks/1207Post,13captures/18payloadcodecs/8damagehelpers;original continuation/queue/invalidation/fallback/discard/dimension rules.No full natural AI/battle,persistence/death/private cleanup claim.
- Main archive934files SHAc02d09eee4afa10a7b251e66f2fe48e1cabebb731e470b5a14ad68f1bf48c08e fully readback/hash verified.Stage24rollback preserved;failed dependency filename selector recorded(no compiler/server start).Weapons paused,separate recovery snapshot noninstallable,Stage13excluded;Forge/historical D read-only.Full Boss goal active.

Next: Audit remaining original shadow erosion/retired-blindness effect contracts against actual effective runtime. Preserve scoped stage24 natural timeline acceptance and all original Boss behavior. Actual client rendering/expiry,authenticated ingress/rewards,multiplayer,persistence/death/full battles and full formal-pack load remain pending. Stage13excluded,weapons paused,recovery addon noninstallable.


## Latest deployed stage26 original shadow and buff retirement, 2026-11-13

- Deployed SHAb56e786c97dc9ed903abc851a4ffd0c21977746a51e1dfafcab19e634437a0a6. Restored exact original retired ShadowBlindEffect apply/remove/synchronize,whole6method ErosionFilter,5instruction spell-effect early return and7retired PlayerBuffGuard public methods/four constants(ENABLED=false).17original mapped method bodies;3RegistryObject.get and2effect casts adapted to1.21Holder API.Three existing classes changed;1447other classes/all resources byte-identical,1451total classes.RawBoss/controllers/plugins/authentication and verification guards unchanged;obsolete buff purging intentionally retired to original behavior.
- Native baseline203checks/6original-contract gaps;candidate470checks/zero failures,20real naturally ticking integrated-player cleanup/removal and actual local client state synchronization cases,75public erosion controls,9retired buff predicates,1public sweep control,6nonplayer duration cases.Infinite speed and40000tickResistanceV admitted and retained in all20player scenes.206natural serverPost,202clientticks. Accepted worlds fresh,normalstop/save/exit0;no synthetic player/private bridge/reflection.
- 54methods ASMverified,37other methods preserved,4negative controls;205levels original/current strength exact float bits each.V2failed infinite-marker admission is retained;authoritative original audit proved obsolete PlayerBuffGuard retirement,restored without relaxing infinite-duration assertions.Split-package startup failure and incorrectguard proof also retained;no failed world reused.
- Public suppression API and exact original spell guard proven;not claiming observed natural Boss spell/AI/fullbattle,framebuffer darkness/expiry,authenticated multiplayer or full formal-pack launch.Stage13excluded.Full Boss goal active;weapons paused,separate recovery addon noninstallable.Forge/historical D read-only.
- Main archive7763files SHA1ee076658e99e5c93371acf8027ad61a81d066d175bfd2bf43ae3377a40c4f5a fully readback/hash verified.Stage25rollback preserved.

Next: Audit remaining original Boss effect/readability contracts on cumulative Stage26;then native client full spell delivery/render/expiry and remaining actual battle paths. Authenticated ingress/rewards,multiplayer,persistence/death/full battles and full formal-pack load remain pending. Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active.


## Latest deployed stage27 original erosion damage and category, 2026-11-13

- Deployed SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0. Only two instruction references in ShadowErosionEffect changed:original BENEFICIAL category and ErosionRules.unscaledDamage(4*(amplifier+1)),clamped4..20,instead of obsolete quarter multiplier.Whole original constructor,tick anddamage graphs exact with explicit official-name/boolean-return platform adaptation;2whole original pure damage helper graphs exact.1450other classes/all resources byte-identical,1451totalclasses.RawBoss/controllers/plugins/verification andall prior retirement classes unchanged.
- Baseline 358checks/candidate 331checks;each14actual admitted NoAI cows and1real vanilla integrated-login player,8amplifiers and6duration boundaries,34natural incoming damage events.Native healthdelta,exact20tick cadence andnatural expiry;actual local client live effect/category/health/expiry synchronization.Baseline35original-contract gaps,candidate0;fresh worlds normalstop/save/exit0.
- 4methods ASMverified,2other native methods preserved,4negative controls,doublepatch rejected.1000max-health,food10/platform/no-gravity public fixtures;no manual Entity.tick/effect callback,synthetic player/private bridge/reflection.No framebuffer/Boss renderer,authenticated dedicated multiplayer,natural fullBossbattle or formal full-pack launch acceptance.Stage13excluded;weapons paused,separate recovery addon noninstallable.Forge/historical D read-only.Full Boss goal active.
- Main archive3920files SHAc3131850489ad5b85d2f174f92b8c9aeb50d958837c68514574780d9b31221f1 fully readback/hash verified.Stage26rollback preserved.

Next: Audit original Zicai curio-silence and remaining non-Manor effective hooks against cumulative Stage27;continue real client full spell rendering and remaining battle paths. Authenticated ingress/rewards,multiplayer,persistence/death/full battles and full formal-pack load pending. Stage13excluded,weapons paused,recovery addon noninstallable;full Boss goal active.


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
