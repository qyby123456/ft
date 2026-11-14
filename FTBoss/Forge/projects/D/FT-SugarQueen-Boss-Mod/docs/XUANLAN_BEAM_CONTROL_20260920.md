# 文玄蓝「超能光束」改版：删伤害 / 眩晕 + 清增益 / 不锁头（2026-09-20 v185）

用户原话：「删除玄蓝超能光束伤害，改成眩晕和清空玩家buff效果，不要锁头，可以锁定一会后放一个前摇施放」。

## 一、三段式流程（改版后）

| 阶段 | 时长 | 会跟头吗 | 玩家看到什么 |
|---|---|---|---|
| **锁定** `XUANLAN_BEAM_LOCK` | 30 tick（1.5 秒） | **会**（这是唯一的"锁头"阶段） | 预警线跟着自己走 + 低音提示，「文玄蓝开始锁定你」 |
| **前摇** `XUANLAN_BEAM_CHARGE` | 20 tick（1 秒） | 不会，方向已冻结 | 红色预警线定格、收缩红环在收、音调递增，「锁定完成 —— 方向固定，侧移躲开！」 |
| **引导** `XUANLAN_BEAM_FIRE` | 40 tick | 不会 | 能量柱开火；每 `XUANLAN_BEAM_PULSE` = 10 tick 判定一次 |

- 预警收缩环的时间轴 = 锁定 + 前摇（`xuanlanBeamPreFireTicks()`），所以环**收满的那一 tick 正好开火**，玩家可以靠环判断时机。
- 躲避方式：锁定期间跑没用（它在跟），**前摇那 1 秒横移出光束线**就安全。
- 冷却 `XUANLAN_BEAM_CD` = 320 tick（16 秒），上场后 `8 × 20` tick 放第一次，均未改。
- 困难难度（IMPOSSIBLE）仍按老规矩把锁定/前摇各减半（`ManorDifficulty.acceleratedTicks`），引导时长不变。

## 二、命中的效果（伤害已整块删除）

| 项 | 改版前 | 改版后 |
|---|---|---|
| 伤害 | 每脉冲 `XUANLAN_BEAM_DMG` = 460，物理 / 魔法 / 虚空 / 真实**四系均分**，4 脉冲共 1840 | **没有任何伤害**（常量本身已删除） |
| 控制 | 无 | **眩晕 60 tick（3 秒）** |
| 增益 | 无 | **清空玩家的全部增益效果**（等价喝奶；减益保留） |
| 击退 | 每脉冲 0.5175 + 上抛 0.15（当场推，随即被定身锚点抹掉，玩家基本没感觉） | **`XUANLAN_BEAM_KNOCKBACK` = 1.5 + 上抛 `XUANLAN_BEAM_LIFT` = 0.35**，在**眩晕解除的那一 tick** 甩出去 |
| 结算次数 | 每 10 tick 一次（共 4 次） | 每 10 tick 只判定一次，**每次施法每个玩家只结算一次**（第一次被照到就吃满） |
| 落地衰弱 | 不进入（用户早先要求） | 不变 |

### 击退为什么必须"攒到解除才推"（2026-09-20 v186 用户要求「加强超能光束击退」）

眩晕走的是硬定身：每 tick 把玩家**拉回锚点**并把水平速度清零。所以命中那一刻当场推的击退，
**下一 tick 就被抹掉了**，只把常量调大也看不出任何变化。现在的做法：

1. 命中时 `stunPlayerWithLaunch(p, ticks, source, dx, dy, dz)`：先上眩晕，再把这一发推力存进 `stunLaunch`（UUID → dx/dy/dz）；
2. `xueliStunTick` 在眩晕**结束/失效**的那一 tick 调 `applyStunLaunch(p)`，用
   `BossKnockback.push(this, p, …)` 真正推出去（action bar 会写「眩晕结束 —— 你被【超能光束】击飞！」+ 脚下白烟）；
3. 走的是**带施法者**的重载，所以吃玄蓝自己的 `XUANLAN_MULTIPLIER` = 1.5（与蒸汽泄压、光束连招一致），
   再按 `BossKnockback` 的对玩家折扣（水平 ×0.25、竖直 ×0.5）结算：
   **实际水平 ≈ 1.5 × 1.5 × 0.25 = 0.5625，上抛 ≈ 0.35 × 1.5 × 0.5 = 0.2625**（改版前的实际值只有 0.129 / 0.075）；
4. 两处会清掉攒着的击退：`setVariant`（换角色）与 `prepareArenaTransfer`（转场），与眩晕表同步；
   `cancelSkillWindups` **故意不清** —— 那一招被打断时眩晕还在跑，击退也该照旧兑现。

想再调：只改 `XUANLAN_BEAM_KNOCKBACK`（水平）与 `XUANLAN_BEAM_LIFT`（上抛）两个数。

眩晕走的不是"挂个缓慢效果"，而是庄园本体早就有的**硬定身**（雪黎瞬砸那一套）：
新增 `stunPlayerAny(p, ticks, source)` 记锚点 → `xueliStunTick` 每 tick 把玩家**拉回锚点** +
清速度 + 停疾跑 + 打断进食/拉弓 + 头顶转金星，所以"眩晕期间真的走不动"。
（原来这段结算只在 `variant == 雪黎` 分支里跑，文玄蓝用不上，本次把它改成所有变体都跑；
只有"有效性判定"从召唤者放宽成任意存活参战玩家。）

## 三、代码位置

| 内容 | 位置 |
|---|---|
| 常量与状态字段 | `entity/EasterBossEntity.java:641-675`（`XUANLAN_BEAM_LOCK/CHARGE/FIRE/PULSE/STUN/LEN/RADIUS/KNOCKBACK`） |
| 状态机 | `entity/EasterBossEntity.java:5136` `xuanlanBeamTick`（锁定 → 前摇 → 引导） |
| 跟头 | `entity/EasterBossEntity.java:5105` `xuanlanAimAtTarget`（注释里写死"只允许锁定阶段调用"） |
| 视觉 | `entity/EasterBossEntity.java:5221` `xuanlanBeamFx`（预警期给"锁定 + 前摇"总时长） |
| 结算 | `entity/EasterBossEntity.java:5240` `xuanlanBeamPunish`（眩晕 + 清增益，无伤害） |
| 眩晕 | `entity/EasterBossEntity.java:5044` `stunPlayerAny`、`:5056` `xueliStunTick` |
| 清增益 | `util/PlayerBuffRules.java` `clearBuffs`（只清 `isBeneficial()`，先拷贝再删） |

## 四、回归

- `tools/verification/check_xuanlan_beam.cjs`（注册名 `xuanlan-beam`）：
  ① 全 `src` 里不许再有 `XUANLAN_BEAM_DMG`、结算里不许出现 `p.hurt(` / `manorSkillDamage(` / `ManorAdaptiveDamage` / `trueDamage(`；
  ② 结算必须走 `stunPlayerAny(p, XUANLAN_BEAM_STUN, "超能光束")` + `PlayerBuffRules.clearBuffs(p)`，且必须记录已结算玩家；
  ③ `xueliStunTick` 必须对所有变体跑、且不再被召唤者闸门卡住；
  ④ 引导分支与前摇分支都不许出现 `xuanlanAimAtTarget()`，锁定分支必须有；三段顺序与"锁定结束接前摇"必须成立；
  ⑤ `cancelSkillWindups` 必须清掉新的锁定状态与已结算名单，`xuanlanBeamBusy()` 必须包含锁定阶段。
- `tools/verification/manor_integration_gametest/BossCombatBalanceTests.java` 的 `xuanlanBeamWaitsOneSecond`：
  常量断言换成新的一套，并新增「锁定阶段跟随移动目标 / 前摇阶段方向冻结」两条实机断言。

## 五、没动的东西

- 玄蓝其它伤害（飞轮绞杀 / 蒸汽泄压 / 光束连招）与 `XUANLAN_DAMAGE_MULT` = 4.19、三处击退数值；
- 磁轨连射 / 超压震环（庄园五技能，`ManorFiveSkills`，仍是伤害技能）；
- 故障机器人（`DefectOrbs`）的「超能光束」—— 那是另一只 Boss 的技能，本次完全没碰。
