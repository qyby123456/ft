# 苹果爵士·蜜脆

`实体 id`: ftboss:apple_boss ｜ 源文件: AppleBossEntity.java（召唤物见 AppleCloneEntity / AppleWormEntity / AppleMothEntity / RottenAppleEntity / GreenAppleEntity / GiantGrubEntity）

## 一句话定位
技能最密集的近战 Boss：冲撞到点后炸三段，另有扇形喷吐、天光轰炸与巨型火球；二阶段换贴图并获得再生/吸收/抗火，同时放出分身与五种召唤物。

## 基础数值

| 项目 | 数值 | 说明 |
|---|---|---|
| 血量 | 320.0 | MAX_HEALTH，createAttributes()；按人数 ×1.25^(n-1) |
| 护甲 | 4.0 | ARMOR；不在 BossEmpower 名单内 |
| 攻击力 | 3.5 | ATTACK_DAMAGE；本体未注册 MeleeAttackGoal，doHurtTarget 是保留路径 |
| 移动速度 | 0.38 | MOVEMENT_SPEED；二阶段变身改 0.45 |
| 跟随范围 | 42.0 | FOLLOW_RANGE |
| 击退抗性 | 1.0 | KNOCKBACK_RESISTANCE（完全免疫击退） |
| 经验 | 140 | xpReward，构造器 |
| 碰撞箱 | 1.7 × 2.7 格 | FTEntities.APPLE_BOSS register |
| Boss 条 | RED / NOTCHED_10 | ServerBossEvent(this.getDisplayName(), ...) |
| 是否定身 | 否 | FloatGoal + PursueGoal(1.5)，距离平方 ≤ 6.25 时停下 |
| 免疫 | 火 + 击退 + 负面效果 | fireImmune() = true；每 20t 自清 + 全局 DENY |
| 召唤物 | 分身 / 果虫 / 果蛾 / 烂苹果堆 / 青苹果 / 金币人 / 巨型果蛆 | 见下 |
| 掉落物 | 苹果核 2~4 + 附魔金苹果 1~2 + 石像 + 武器 | die() 内掉落；无 JSON 实体战利品表 |
| 强化与狂暴 | 均不参与 | 不在 BossEmpower 白名单，也未调用 BossRage |
| 全局限伤 | 原“单次超过 500 点整下无效”规则已取消 | 旧兼容入口（永远放行）；技能伤害不走人数倍率 |

## 阶段机制
- 两阶段：!transformed && getHealth() <= getMaxHealth() * 0.5f 时播 T_TRANSFORM = 56t 变身（第 24 帧写入 DATA_PHASE = 2）。
- 二阶段：移速改 0.45，挂 力量 II 600t + 再生 II 600t + 吸收 IV 2400t + 抗火 6000t + 抗性 I 6000t，立刻 heal(40.0f)，appleCd / enchantCd 重置为 300 / 900。
- 变身必放一次天光光束（skyT = 60）与一次巨型火球（metT = 30），并给 64 格内玩家停掉一阶段曲、播放二阶段 BGM；换贴图 apple_boss_p2.png。
- 仅二阶段可用：附魔金苹果、汁液喷洒、钻地突袭；其余技能冷却在二阶段整体变短（蜡护膜除外）。

## 技能与机制

### 冲撞自爆
- 视觉: 自爆使用 0.5 秒金白色扩散细光环，中心局部闪光持续 0.1 秒；本体与分身不生成爆炸烟幕或黏液碎屑。冲刺每 tick 只留 1 个白色光点；蓄力只留 4 个提示粒子。伤害、半径、三段间隔与击退不受光效影响。
- 触发: 冷却初值 10t，之后每次 22 + rnd(16)t；蓄力 10t 后冲刺 40t，每 tick 冲 1.2 格
- 效果: 接触（距离平方 < 6.25）或冲满 40t 即炸三段、间隔 8t；三段半径 6.0 / 6.5 / 7.0 格、基准 45.0f / 52.0f / 60.0f，实际 base × 0.5 × max(0.25, 1 - 距离/半径) → 最高 22.5 / 26 / 30，击退 0.9 + 0.4 × 段号；纯视觉爆炸，不毁掉落物

### 果籽霰弹
- 触发: 冷却初值 90t、距离平方 < 400；效果: T_SEED = 32t，第 16 帧锥形 7.0 格 / 半角 70°、设计伤害 9.0 + 阶段 × 2.0（coneAttack 内部 ×0.5），击退 0.7；重设 二阶段 130 + rnd(60) / 一阶段 200 + rnd(90)t

### 酸蚀喷吐
- 触发: 冷却初值 200t、距离平方 < 144；效果: T_ACID = 36t，第 15 帧锥形 6.5 格 / 半角 60°、设计伤害 12.0 + 阶段 × 3.0（×0.5），命中挂 中毒 140t + 缓慢 100t（II 级）；重设 二阶段 200 + rnd(90) / 一阶段 300 + rnd(120)t

### 汁液喷洒（二阶段）
- 触发: 冷却初值 240t、距离平方 < 196；效果: 复用 T_ACID = 36t，锥形 8.0 格 / 半角 55°、伤害 3.0f，命中挂 缓慢 140t（III 级）+ 挖掘疲劳 120t（II 级）+ 反胃 100t；重设 220 + rnd(90)t

### 烂苹果投掷
- 触发: 冷却初值 140t、距离 > 7 格且 < 26 格；效果: T_THROW = 28t，第 12 帧投 RainbowPoopEntity（外观物品 = 苹果核），设计伤害 12.0 + 阶段 × 3.0（×0.5 → 7.5 / 9.0），命中附带 缓慢 100t + 反胃 140t + 饥饿 160t

### 头顶三苹果
- 触发: 冷却初值 160t；效果: T_ORBIT = 32t，第 21 帧一次抛 3 颗、扇形 12°、出膛 Y + 2.6，设计伤害 14.0 + 阶段 × 3.0（×0.5 → 8.5 / 10.0）

### 蜡护膜
- 触发: 冷却初值 320t → 重设 460 + rnd(160)t（唯一不随阶段变短的技能）；效果: T_WAX = 40t，第 20 帧挂 抗性 III 140t + 吸收 II 200t 并 heal(20.0f)

### 钻地突袭（二阶段）
- 触发: 冷却初值 320t、距离平方 < 900 → 重设 400 + rnd(160)t、持续 26t；效果: 期间 setInvisible + setInvulnerable（本 Boss 唯一无敌机制），出土传送到目标坐标，30 平方格内玩家吃 10.0 + 阶段 × 1.5 点并被 push(0, 0.85, 0)

### 分身召唤
- 触发: 冷却初值 120t，重设 二阶段 140 + rnd(80) / 一阶段 200 + rnd(100)t，分身立即生成
- 效果: 数量 = min(5 + rnd(3) + (二阶段 ? 2 : 0), SummonLimit 余量) → 一阶段 5~7 / 二阶段 7~9，沿圆周均分、半径 4.5 + rnd × 1.5

### 巨型果蛆
- 触发: 冷却初值 200t；效果: 重设 二阶段 400 + rnd(160) / 一阶段 600 + rnd(240)t；64 格内已有同种就不再召唤（同场仅 1 条），出生点距本体 4.0 格

### 天光光束
- 触发: 冷却初值 220t；重设 二阶段 380 + rnd(140) / 一阶段 560 + rnd(200)t；T_SKY = 44t，skyT = 60t 预警并锁定目标 x/z
- 效果: 归零后 10 波、每波间隔 10t、每波 16 处；落点 60% 概率在目标 6.0 格内，否则在锁定点 15.0 格内，i == 5 强制劈目标
- 伤害: 3.5 格内玩家吃 7.0 × max(0.35, 1 - 距离/3.5)（最高 7 点）+ push(0, 0.4, 0)；前 5 处天火（点燃 4 秒）、其余为雷（真实闪电、无药水效果）；冰霜与凋零分支不可达

### 巨型火球砸地
- 触发: 冷却初值 300t；重设 二阶段 320 + rnd(140) / 一阶段 460 + rnd(200)t；metT = 30t 预警并锁定目标 x/y/z
- 效果: 落地半径 8.0，伤害 30.0 × max(0.4, 1 - 距离/8.0)（最高 30 点），点燃 6 秒 + push(0, 1.0, 0)

### 召唤五选一
- 触发: 冷却初值 160t；效果: 重设 二阶段 180 + rnd(80) / 一阶段 260 + rnd(100)t，第 19 帧结算；pick = nextInt(5) 等概率五选一（果虫 / 果蛾 / 烂苹果堆 / 青苹果 / 金币人），want = 3 + rnd(3) + (二阶段 ? 2 : 0)，果蛾 +2、青苹果 +3、金币人另算，均受 SummonLimit 余量限制

### 金苹果与附魔金苹果
- 触发: 金苹果冷却初值 300t、每 15 秒一次（两阶段都有）；附魔金苹果 900t、仅二阶段（45 秒一次）
- 效果: 金苹果 = 再生 II 100t + 吸收 I 2400t + heal(4.0f)；附魔金苹果 = 再生 II 600t + 吸收 IV 2400t + 抗火 6000t + 抗性 I 6000t + heal(40.0f)

### 头衔共鸣·蜕变
- 触发: 每 20t 检查，48 格内玩家须同时持有头衔 farmingtales:sweet_apple_manor 与契约 tridents_n_stuff:sanguine_bond（或显示名"血腥契约"）
- 效果: 大量粒子与音效演出、64 格内提示"§6【头衔共鸣】"；随后走 HiddenBossChallenge 五句台词并 discard()，**不触发任何掉落**

### 反制与免疫
- 免火、免击退、免一切负面效果（本体与分身都在 DENY 名单内；六种召唤物不在）
- 苹果阵营免友伤：取消闪电与爆炸类伤害，判定 isAppleSide() 不含巨型果蛆；原“单次超过 500 点整下无效”规则已取消、免疫流血/真实伤害；无连死怜悯；被打后转向攻击者（记忆 200t、上限 64 格）；死亡时 SummonSweep.clear(80 格) 清场

## 召唤物 / 掉落 / 音乐
- 分身 ftboss:apple_clone: 20 血 / 攻击 0 / 移速 0.28 / 跟随 40，渲染缩放 0.85；寿命 280t（14 秒）后自爆；冲撞冷却 15 + rnd(20) 与 60 + rnd(40)，蓄力 8t、冲刺 30t、每 tick 0.95 格、接触距离平方 < 6.25；自爆半径 主动 5.0 / 被打死 3.5，基准 28.0f / 14.0f，实际 ×0.5 × 衰减 → 主动最高 7.0、被打死最高 3.5，击退 1.3；无自定义名，靠体型与 20 血区分
- 果虫 ftboss:apple_worm: 12 血 / 攻击 2.0 / 移速 0.30 / LIFE 600t，命中 缓慢 70t + 饥饿 90t；果蛾 ftboss:apple_moth: 8 血 / 攻击 1.5 / 飞行速度 0.36 / LIFE 600t，无重力飞行、绕目标 1.5 格盘旋，命中 虚弱 100t
- 其余三种: 烂苹果堆 ftboss:rotten_apple（18 血 / LIFE 900t，死亡或到期 explode power 1.1F、HURT_RANGE 3.4）、青苹果 ftboss:green_apple（16 血 / LIFE 600t，命中 中毒 120t + 虚弱 160t + 缓慢 120t）、金币人 ftboss:coin_man（24 血 / 护甲 4.0 / LIFE 800t，命中 缓慢 140t III + 挖掘疲劳 160t II + 短定身）
- 巨型果蛆 ftboss:giant_grub: 180 血 / 攻击 4.5 / 护甲 6.0 / 击退抗性 0.8 / 经验 30，渲染缩放 3.5，命中 反胃 220t + 饥饿 300t；同类上限 10 只 / 64 格（SummonLimit），到上限改给同类全体挂 速度 II + 力量 I + 抗性 I 400t
- 召唤入口: 禁果 ftboss:forbidden_apple（堆叠 1）右键，在朝向 4 格生成；非主世界直接失败且不消耗；WITHER_SPAWN(1.6, 0.75)；配方 forbidden_apple.json = 金块 ×4 + 红石块 ×4 + 苹果 ×1。禁果本身没有药水效果/减益/冷却（代码里未找到），只是召唤物
- 掉落: 苹果核 2~4 个（2 + rnd(3)）+ 附魔金苹果 1~2 个（1 + rnd(2)）；石像 ftboss:statue_apple 与武器"苹果爵士的果梗"（下界合金级剑）走 BossMercy 中央钩子；禁果掉落已移除
- 音乐: **v110 起一阶段走专属曲** —— `BossMusic.groupOf` 新增 `Group.APPLE` → `ftboss:boss_music_apple`（以前没有 APPLE 分支，一阶段回落到三幕剧）；变身帧由实体自己给 64 格内玩家发 `BOSS_MUSIC_APPLE` 停止包后播放 `ftboss:boss_music_apple_p2`。两首曲（含 compost_p2）v110 也加进了 `BossMusic.allTrackEvents()` 的"停止时全停"名单 —— 以前不在这里，Boss 死后二阶段曲没人停

## 应对要点
- 三段爆炸（6.0 / 6.5 / 7.0 格，最高 22.5 / 26 / 30 点）是最稳定的伤害来源，冲刺方向锁死后立刻横向拉开 7 格。
- 二阶段变身会白送一次天光光束与一次巨型火球，并叠 5 分钟抗火 + 抗性；尽量在 50% 前打完爆发。
- 天光有 3 秒 END_ROD 光柱预警，10 波 × 16 处、60% 压在玩家 6 格内；预警期内走出锁定光柱即可，但雷是真实闪电。
- 分身只有 20 血且被打死的爆炸仅 3.5 点（主动炸 7.0 点），优先点掉贴脸的分身。
- 巨型果蛆不在自家友伤豁免名单里，可以引它去吃本体与分身的爆炸/闪电；单次伤害无需再受 500 点全局阈值限制。

## 关键常量（代码位置）
- `AppleBossEntity.java: 320.0 / 4.0 / 3.5 / 0.38 / 1.0 / 42.0；xpReward 140；转阶段 <= getMaxHealth() * 0.5f`
- `AppleBossEntity.java: T_ROLL 32 / T_SEED 32 / T_THROW 28 / T_WAX 40 / T_SUMMON 44 / T_TRANSFORM 56 / T_ACID 36 / T_HURT 9 / T_ATTACK 18 / T_ORBIT 32 / T_SKY 44`
- `AppleBossEntity.java: 三段爆炸半径 6.0/6.5/7.0、基准 45/52/60、×0.5、段间隔 8t；METEOR_TELEGRAPH 30；R 8.0；appleCd 300 / enchantCd 900`
- `AppleCloneEntity.java: 20 血 / 0 攻 / 0.28 速；life > 280 自爆；R 5.0/3.5；base 28.0f/14.0f；×0.5；击退 1.3`
- `AppleWormEntity.java: LIFE 600`；`AppleMothEntity.java: LIFE 600`；`RottenAppleEntity.java: LIFE 900 / BLAST_RADIUS 1.1F`；`GreenAppleEntity.java: LIFE 600`；`CoinManEntity.java: LIFE 800`；`GiantGrubEntity.java: 180 血 / 护甲 6.0`
- `SummonLimit.java: MAX_PER_TYPE 10 / RADIUS 64.0 / BUFF_TICKS 400`；`SummonSweep.java: RANGE 80.0`；`BossDamageGate.java: 旧 500 点阈值（已移除） 500.0`
