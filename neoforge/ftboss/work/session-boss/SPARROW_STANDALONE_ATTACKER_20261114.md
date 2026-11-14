## Stage36 麻雀原生攻击者与周期持久记录，2026-11-14

产品保持 Stage35 9d713622cc8c8aa3d2e5b5fa340053a7eb45f8128247346c3152a14c975b886e、NOT-INSTALLABLE。真实离线客户端/集成服 7 场景 7227 检查通过：下界公开 vanilla summon 无 NBT 注入，实际生存 ServerPlayer.attack 正伤害写入真实玩家 UUID 和自然时刻，同玩家再击只刷新唯一记录；Incoming 取消、native Pre 零均不记录，原注册蛋建立主世界真实托管战斗后正/零伤害均不进入 standalone 表。原版主世界自动收编和维度门完整保留，没有为测试关掉。实际 natural tick 到 1200 保留、1201 由公开原 participants 清理，未改时间、身份或记录。

实际召唤者原公开投降及 40 自然 tick 清理后，退休记录在下一个真实 2400 tick 的 ServerTick.Post 原订阅中清理；2401 只读观察已消失。正常存盘后实际 NBT 及公开原 load/save 再核对退休记录缺席，5 项存储检查通过。六个实际助手类字节等于候选，342 个实际 Boss 方法图等于前段已证图、1374 检查及两负控保持。未手动调用 onDamage/onTick/prune，不私调玩法、不创建 FakePlayer、不写 fight/owner/attackers/appearance、不手动 tick 或改时间。公开夹具玩家 Pre 承伤限 0.01、目标正 Pre 设 16；这是事件与清理验证，不是完整实战、认证多人或世界重启验收。

三个失败均保留且正常保存/退出 0/回收/输入不变：V1 原版自然收编导致观察器错误认为仍 standalone；V2 当 tick 的实体列表先于自然区块入场；V3 普通原生零伤害会有零值 Post，与 managed 零抑制不同。产品未为通过测试改行为。112 个关闭依赖 JAR 全路径及字节本地保留，按哈希只读去重。源码与白名单文字可上传；模型/贴图/动画/音乐/音频及嵌套或编码资源、JAR、世界禁止上传，Git 不提供排除资源恢复。正式实例及 D 只读、不部署，武器/Session 暂停，Curios 新 JVM 不自动重试。

累计下一项已证缺口：当前 BossRelicDrops 仍委托 OptionalWeaponModule.reward，默认无武器 service 的 reward 是空返回；三个 BossMaterialDrops 类及注册/创造栏入口缺失。已有 Stage03 独立材料源码/原 ID 和限定证据可复用，但尚未累计集成，不能把以前单独候选当当前完整功能。下一段精确接回 Boss 独立材料，不恢复武器服务；真实认证击杀奖励和完整移植仍待验收。详见 work/session-boss/SPARROW_STANDALONE_ATTACKER_20261114.md 与 reports/sparrow-standalone-attacker-closeout-20261114.json。

待办：

- Integrate frozen Stage03 independent Boss materials into cumulative candidate: three classes, initialization/tab and original reward callback; keep weapon service paused
- Authenticated real multiple players, scaled quotas/additional plans/repeat appearances and original full Boss combat/rewards/death/cancel
- World restart/dimensions and original action/visual fidelity; Curios new JVM remains attended and not auto-retried
- Final cumulative installable artifact, explicit formal-write authorization and verified deployment/rollback; source-only remote archive; weapons and Session paused
