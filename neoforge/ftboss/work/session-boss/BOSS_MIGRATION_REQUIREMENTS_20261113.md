## Stage30 麻雀原版受击闪避限定验收，2026-11-14

原版 SparrowHitEvasion/State 的 25 个映射方法图及 25 个 ASM 检查通过，规则类字节原样保留，接入原版三处钩子。真实离线玩家空手攻击基础场景 971 项、原同伴掩护场景 772 项通过；分别观察到 22/16 个自然 tick 的原版自有路径，同伴控制保护 16 tick。原 60 tick 到期、no-AI 拒绝、实际实体离开清理通过，正常完整保存/退出 0，冻结输入未变。最终定义 EasterBoss 类 351 项检查限定复用，339 个方法不变，仅 hitFeedback/sparrowTick 改变；五个 Boss 定义字节一致，LivingEntity 不同且不在该复用证明范围。

候选 SHA256 877309d1ff2332ab6d407cd1b456d39987109d17e8f6a8e98184abe55703c25b。原始 bootstrap、两次正向寻路失败和首次六类绑定错误均保留；原寻路安全阈值未放宽。同伴夹具自然空 fight/summoner ID 相等，仅证明该分支，不声称认证管理战斗；冻结同伴回执的 noAllyFixture=true 属继承标签错误，在限定收口中更正解释，不改写原证据。详见 work/session-boss/SPARROW_HIT_EVASION_20261114.md 与 reports/sparrow-hit-evasion-v2-closeout-20261114.json。

关闭的三次失败运行 84 个 JAR 路径/全部字节保留，82 个只读同内容硬链接约释放 504 MiB，仅为本地去重；不声称资源远端可恢复。后续只上传审查过的源码及文字证据，必须先完整树资源/秘密扫描、独立远端逐 blob 恢复核验，才清理 Git 重复源码缓存。全部模型、贴图、动画、音乐/音频和嵌套资源禁止上传。完整 Boss 仍未完成，原生 LivingDamage.Post 尚缺（本轮 0 回调），NOT-INSTALLABLE、不部署、正式实例与 D 只读、武器/Session 暂停。旧 Stage29 的“闪避未移植”为当时历史状态。

## Stage29 庄园减伤上下文限定验收，2026-11-14

真实空手玩家攻击已复现并修复 NeoForge damageContainers 空栈异常；仅修改 NativeManorFinalDamageInstaller，一个候选类变化。72 项合成生命周期检查、701 项真实玩家/原自定义 BeforeHealth/生命变化与自然 tick 检查、22 项最终定义类检查通过，完整保存/退出 0，冻结输入不变。采集中文选项编码事故的 11 个自有文件已按 SHA256 迁回，原失败回执保留，后续使用 Base64URL UTF-8 选项。详见 work/session-boss/MANOR_DAMAGE_CONTEXT_20261114.md 与 reports/manor-damage-context-v1-closeout-20261114.json。原生 LivingDamage.Post 契约尚未完成（本轮 0 回调）；麻雀闪避未移植，现有真实正向基线可用。NOT-INSTALLABLE，不部署；模型/音乐等资源禁止上传，武器/Session 暂停，两个正式实例只读。

## 新 JVM 有人值守工具与 V18 超时证据，2026-11-14

V18 仅延长真实确认页等待至 180 秒并记录页面状态，保留原严格登录/物品/120 自然 tick 断言。已从接受的 V6 正常存档创建全新副本，启动前及实际页面出现时通知用户立即点击 Create Backup and Load；本次到期未确认，严格失败、正常退出且无服务端或玩家登录，不能宣称持久化验收通过。当前没有等待窗口或残留进程。下次须先由用户确认已在电脑前，再启动准备好的工具；不得自动重试、复用失败世界、猜测点击、调用私有确认回调或修改警告标志。见 work/session-boss/CURIO_ATTENDED_RELOAD_RECOVERY_20261114.md 和对应检查点。

此前所有通过的生命周期增量仅引用，未重跑。另对非庄园终结难度完成原/当前类字节绑定的初步契约审查；属性缩放范围及方式有静态差异，尚需真实有效基线，未判为已证明的运行时缺陷，未绕过挑战解锁。完整 Boss 及完整生命周期仍未完成。保持同一唯一写入者、NOT-INSTALLABLE、不部署、正式实例只读、武器及 Session 暂停。诊断源码和证据归档同步不代表接受失败功能；远端独立核验后只清理合格重复源码缓存，所有原件保留。

## Stage28 scoped Zicai Curios candidate checkpoint, 2026-11-13

- The original Curios silence helper,rules andChoice are ported as three added classes;all1451existing classes andall resources stay byte-identical to Stage27. Candidate SHA256 c45cc10ffb7b572d876e869b692a75496c88c337434ed7140225df298b7bb65e. NOT-INSTALLABLE andnot deployed;both formal instances remain read-only.
- Effective unchanged-Stage27 baseline404checks demonstrates two gaps. Candidate basic contract1099checks passes. Valid original boss_relic native death/respawn/pre-expiry logout589checks passes withglobal exactly-one item checks. Invalid gold-nugget head fixture strict failure remains;unchanged Stage27 also reproduces count2. No assertion was relaxed andno failed world was reused.
- Native new-JVM login remains UNACCEPTED. V7 bounded failure andV8 public read-only screen diagnostics stop before any integrated server/player at experimental-world BackupConfirmScreen. No warning/private callback/data flag was bypassed. Supported UI input is unavailable in this run. Continue independent fresh-world slot-removal/occupied-slot inventory fallback andoriginal owner/eligibility controls;complete actual reload confirmation separately.
- Local content-addressed archive15497files/2221unique blobs passed complete hash readback;all originals retained. See github-sync/checkpoint-boss-stage28-zicai-20261113.json andwork/session-boss/ZICAI_CURIO_SILENCE_STAGE28.md. Full lifecycle matrix/full Boss/authenticated multiplayer/rewards/battle remain incomplete. Weapons/Session remain paused andthe separate weapon recovery JAR is not installable.
- Source persistence uses the existing safe branch/subdirectory. Preceding remotely recovered checkpoint head4caf6c1d9799eb443ff0058941bbd38dc7e988ee preserves original main/README. Review,push andfreshly retrieve this bounded source increment;raw binaries/worlds/logs/archives/private state remain local. No large-artifact remote recovery orlocal deletion is authorized by source-only proof.

# Full Boss migration and deployment requirements

## Verified source persistence, 2026-11-13

Three source commits pushed to `codex/neoforge-source-20261113`,head `204b13de017c6cb2c8b6e06513f4436e550ff412`. Fresh independent retrieval verifies1903source files plus one import manifest bySHA256;original main/README preserved. This is source recovery only;large raw artifacts remain local andmust not be deleted. Both formal instances remain read-only,no new deployment. Full Boss migration remains incomplete;weapons/Session paused. Next:original Zicai Curios silence effective native baseline,then complete behavior port.

The active direct-user objective is 继续完成boss部分部署移植. This checklist preserves the complete requested end state; a passing scoped probe is not full completion. The same sole C writer retains authority. Weapon development, validation, lease/payment/Session and its noninstallable recovery JAR remain paused.

| Requirement | Authoritative evidence/current state | Remaining proof |
| --- | --- | --- |
| Preserve weapon implementation while separating it from Boss runtime | archives/weapons-paused-cf217105-boss-priority-v2-20261113: 5807-file verified archive and separate recovery JAR; no runnable addon accepted | Preserve pause; do not count a recovery snapshot as an installable addon |
| Boss independent startup, registry, owner inheritance, natural entity scheduling | reports/independent-boss-loop-96794243.json: original scoped 24 Boss/660-post-tick evidence; older candidate | Bind any reuse to unchanged relevant class instructions; no claim of authenticated combat |
| Existing accepted Boss increments remain intact | DEPLOYED_PROGRESS.md: stage04 immunity,05 removal protection,06 flight guard,07 phases,08 cadence,09 Blood Brand,10 Blood Tide,11 Cleaver Sweep,12 Bone Crush | Preserve exact scoped accepted behavior when integrating later changes |
| Original Boss materials/rewards independent of weapon growth | STAGE03_BOSS_MATERIAL_DROPS.md and archived candidate, undeployed | Real authenticated positive kill/reward path and integration into cumulative Boss candidate |
| Original Butcher summon effects and native scheduling | 315 checks,66 actual natural ticks and verified normal-save archive; reused, not rerun | Real authenticated start/clone ingress and remaining full battle integration |
| Actual original particle delivery and visible front frame | candidates/boss-core-stage13-client-native-fresh-world-20261113/acceptance.json:396 packets,806 checks, actual baseline/active framebuffer review, normal complete save/exit0 | Other views and authenticated dedicated multiplayer; V13 broad-red green flag is explicitly invalidated |
| Original Boss client resources | build/boss-client-resource-completion-v1:294 missing original resources restored; weapon guide excluded. v2:21 OBJ loader namespace ports,1446 class bytes unchanged | Real model baking/statue rendering and per-Boss body/action/effect fidelity; check complete retained renderer resource closure |
| Four actual idle GL views per Boss | client-boss-matrix-v2 actual native run:12 Manor variants x normal/impossible server fixtures plus ten non-Manor Boss types,136 views planned | Require original client variants,real render returns,texture presence,visible body ROI,GL errors,visual review and complete save. This is idle rendering, not skill/full battle acceptance |
| Original remaining Boss behavior parity | build/boss-remaining-behavior-audit-20261112 and boss-xuanlan-boundary-audit-20261113 contain primary original/native evidence | Establish true runtime baselines before changing NonManorFinale,Gongdi lifecycle/cooldowns,Xuanlan support/wheel caps,Sparrow evasion,Manor air/predictive attacks,training dummy rules,Zicai silence,Lemon support and other original handlers. Missing class names alone prove no gap |
| Actual player/Boss entry and complete battles | No authenticated session supplied to this batch; no private credential store was read | Real authenticated summon/targeting,all required Boss attacks/phases/rewards/death/cancel flows,normal and impossible difficulty,multiple players and persistence/restart/dimension controls |
| Installable cumulative artifact | All current stage13/resources candidates explicitly NOT-INSTALLABLE; stage03 separate candidate not integrated | Build and verify cumulative artifact with all accepted Boss features and no mandatory paused weapon dependency; no forged authentication or relaxed gameplay rules |
| Deployment and rollback preservation | Formal Stage27 SHA6563f431dca1871eb7870653b74550ba2f5c0f66c104eea90ce58a99ca30f7c0;latest direct instruction prohibits further deployment | Keep both formal instances read-only;preserve all scoped releases/rollback/archive hashes. Further deployment requires a new direct authorization andcomplete applicable gates. |
| Final goal completion | Unproven; goal remains active | Requirement-by-requirement current-state audit,all required artifacts/runtime evidence,final deployment/rollback/archive checks and no required work outstanding |

All native launches are serial. A verified live handle must be polled to terminal and reaped; never restart because an observation window expires. Failed worlds are never seeds. Frozen acceptance is reused only within its actual scope and only with proved input/class identity. Preserve every preparation/test/product failure and every explicit invalidation.


## Stage14 current evidence update

Resource-only stage14 is verified/deployed/archived,based on stage12 with1443 unchanged classes and314 resource additions. V4 original partial-negative report preserves96 completed Manor views;V5 supplies40 remaining body and4 survival images. V5 reset green flag is invalidated;V8 accepts4 focused survival images and complete natural reset after238 ticks,zero actual effects,original overlay inactive,92160 bright sky and251507 bright world pixels,with screenshot review. StatueV1 accepts44 real world views and21 actual baked OBJ models/textures. Archive18164files SHA09eed5d380cc659c0bb5e57b40bc191fa930749e44aed518e1369d4f8bc16886. Earlier preparation entries are superseded only within this actual resource/body scope. Stage13 excluded and undeployed. Full authenticated/reward/multiplayer/full-battle goal remains active.


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
