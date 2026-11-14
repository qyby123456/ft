# 炫彩答辩超人

`实体 id`: ftboss:compost_boss ｜ 源文件: CompostBossEntity.java（投射物 entity/RainbowPoopEntity.java，召唤物见 PoopMinionEntity / FlyMinionEntity / MaggotMinionEntity / CompostSlimeEntity / SheepDungEntity）

## 一句话定位
手持 Create 土豆加农炮的炮击型 Boss：一阶段地面抛射，血量 70% 后起飞环绕投弹、俯冲撞击，35% 再进一阶段；所有伤害公式都带阶段系数，越拖越疼。

## 基础数值

| 项目 | 数值 | 说明 |
|---|---|---|
| 血量 | 340.0 | MAX_HEALTH，createAttributes() |
| 护甲 | 4.0 | ARMOR；BossEmpower 再 +4.0 |
| 攻击力 | 4.0 | ATTACK_DAMAGE；BossEmpower 再 ×1.30 |
| 移动速度 | 0.185 | MOVEMENT_SPEED（注释：0.24 → 0.185）；BossEmpower 再 ×1.25 |
| 跟随范围 | 32.0 | FOLLOW_RANGE |
| 击退抗性 | 0.4 | KNOCKBACK_RESISTANCE；BossEmpower 再 +0.30 |
| 攻击速度 | 未设置 | createAttributes() 未加 ATTACK_SPEED |
| 经验 | 120 | xpReward，构造器 |
| 碰撞箱 | 1.6 × 3.4 格 | FTEntities.COMPOST_BOSS register("compost_boss", 1.6f, 3.4f) |
| Boss 条 | GREEN / PROGRESS | ServerBossEvent(this.getDisplayName(), ...)；48 格内玩家自动加入 |
| 是否定身 | 否；二阶段起飞 | MeleeAttackGoal(1.0)；getPhase() >= 2 时 setNoGravity(true)，悬停在地面高度图上方 5.0 格 |
| 召唤物 | 大便小怪 / 苍蝇 / 蛆 / 棕黑史莱姆 / 羊屎蛋 | 数值见下 |
| 额外材料掉落 | 无 | 石像由 BossMercy.giveStatue 统一发放 |
| 人数缩放与强化 | 血量 ×1.25^(n-1)、攻击 ×(1+0.15(n-1))；另移速 +25%、攻击 +30%、护甲 +4、击退抗性 +0.30 与常驻 速度I/抗性I/抗火I/力量II | BossScaling + BossEmpower（名单含 COMPOST_BOSS，注释"自身攻防不足"） |
| 全局限伤与免疫 | 原“单次超过 500 点整下无效”规则已取消；免疫一切负面效果 | 旧兼容入口（永远放行）；onEffectApplicable 对本体 DENY |

## 阶段机制
- 三阶段：P2_AT = 0.70f、P3_AT = 0.35f；tick() 里 want = ratio <= 0.35 ? 3 : (ratio <= 0.70 ? 2 : 1)，只有 want > getPhase() 才切换；阶段存 DATA_PHASE 与 NBT "phase"。
- 进 2 阶段：EMPOWERED 置 true，播 T_TRANSFORM = 56t 的 transform 动作 + WITHER_SPAWN(1.4, 0.7)；给 64 格内玩家发停止包停掉《堆肥·强袭》，改放二阶段曲《堆肥·熔毁》（音量 4.0）。
- 进 3 阶段：播 phase_shift 动作（T_SHIFT = 44t），其余演出同 2 阶段，不再换曲。
- 飞行姿态：高度 = 地面高度图 Y + 5.0 + sin(tickCount × 0.08) × 0.3，每 tick 垂直限幅 ±0.3，始终转向目标。

## 技能与机制

### 连射答辩（主炮）
- 触发: 冷却初值 120t；打空后 = max(16, 55 - hitStreak × 4) + rnd(12)t，弹匣 4 + rnd(2) 发，开火前 16t 抬炮
- 效果: 每发设计伤害 6.0 + 阶段 × 2.0（RainbowPoopEntity.setDamage 内部再 ×0.5，实到玩家 3.0 + 阶段 × 1.0）；按目标速度预判（lead = min(1.0, 距离/24)），散布 0.6

### 攻速叠层
- 触发: 答辩弹命中玩家时回调 noteHitOnPlayer()
- 效果: hitStreak +1（上限 12），60t（3 秒）未命中归零；连射冷却随之变短，弹匣内间隔 = max(1, 3 - hitStreak / 4)

### 抛射答辩
- 触发: 冷却初值 160t；归零后 140 + rnd(80)t，配 T_FIRE = 32t 抬手动作
- 效果: 一次 3 发、间隔 4t；每发设计伤害 7.0 + 阶段 × 2.5（内部 ×0.5），出膛高度 +2.1，速度 1.05、散布 1.2，明显上抛弧线

### 沼气爆炸
- 触发: 冷却初值 200t；归零后 200 + rnd(100)t，gas_charge 动作 52t，40t 后引爆
- 效果: 自身位置 explode(power = 1.6f, ExplosionInteraction.NONE)；8 格内玩家吃 8.0 + 阶段 × 1.5 点并点燃 4 秒

### 空中弹幕（2 阶段起）
- 触发: 飞行且未俯冲时冷却归零 → 重设 170 + rnd(60)t
- 效果: 扇形 5 发（i = -2 ~ 2），每发设计伤害 9.0 + 阶段 × 2.5（内部 ×0.5），横向偏移 i × 1.9，俯角 min(-0.15, dy)，速度 1.3、散布 0.8

### 空中移动投弹
- 触发: 飞行中冷却归零（固定重设 30t）→ 抬手，10t 后投弹
- 效果: 从身下投弹，设计伤害 10.0 + 阶段 × 3.0（内部 ×0.5），shoot(水平 ×0.95, 1.05, ×0.95)、速度 1.15、散布 0.45
- 环绕走位: 每 40t 反转方向，半径 = 6.5 + sin(tickCount × 0.02) × 2.0（4.5~8.5 格），角速度 0.045；非冲刺时每 tick 至多移动 0.55 格

### 直线冲刺
- 触发: 冷却归零 → 重设 26 + rnd(14)t，持续 14t
- 效果: 沿当前朝向每 tick 直冲 0.95 格（注释：学习光之女王的移动方式）

### 俯冲撞击（2 阶段起）
- 触发: 冷却归零 → 重设 180 + rnd(60)t，先悬停蓄势 14t，随后俯冲 40t
- 效果: 朝目标每 tick 追击 0.55 格；1.8 格内玩家吃 9.0 + 阶段 × 2.0 点、被击退 1.2、点燃 3 秒，命中即结束

### 悬停激光（已停用）
- 触发: 代码条件写成 if (false && ...)，永不成立
- 效果: 实现仍在（每 2t 结算 4.5 + 阶段 × 1.0 点，射线每 0.6 格一个 END_ROD 粒子、判定半径平方 1.7、射程 24 格），按注释已去除，当前不生效

### 屎？！！（概率诅咒）
- 触发: 冷却归零 → 重设 200 + rnd(120)t（约 10~16 秒一次尝试）；目标为 32 格内玩家且 rnd < 0.35f 才施放
- 效果: PoopCurse.castOnce() 挂固定 I 级、160t（8 秒）的"屎？！！"（方向键混乱 + 法力流失），不叠层、召唤物命中不再刷新

### 负面效果转强化
- 触发: 每 40t 统计自身非增益效果数量
- 效果: 数量 > 0 时挂 90t 的 力量（amp = min(4, 数量-1)）、速度与抗性（amp = min(3, 同值））；注意 BossHandler 对它的负面效果一律 DENY，两处逻辑并存

### 召唤门控
- 大便小怪: 2 阶段起、240t 一次，召唤 3 + rnd(2) 只；苍蝇: 2 阶段起、200 + rnd(80)t 一次，4 + rnd(3) 只；蛆: 全阶段、170 + rnd(80)t 一次，3 + rnd(3) 只
- 棕黑史莱姆: 220 + rnd(90)t 一次，3 + rnd(2) 只、尺寸 2 + rnd(3)；羊屎蛋: 200 + rnd(80)t 一次，3 + rnd(3) 只、尺寸 1 + rnd(2)
- 上限规则: 每种召唤物在 64 格内最多 10 只（SummonLimit.MAX_PER_TYPE）；到上限时不召唤，改给场上同类全体挂 速度 II / 力量 I / 抗性 I 400t（20 秒）

### 反制与免疫
- 免疫一切负面效果；免疫友军伤害（onAllyFriendlyFire 取消 Boss 阵营互伤）；原“单次超过 500 点整下无效”规则已取消；免疫流血/真实伤害类型
- removeWhenFarAway() = false 且 setPersistenceRequired()；displayFireAnimation() = false；死亡时 SummonSweep.clear(80 格, 大便小怪 / 苍蝇 / 蛆)

## 召唤物 / 掉落 / 音乐
- 答辩弹 ftboss:rainbow_poop: 默认伤害字段 3.0f；命中玩家固定附带 缓慢 100t + 反胃 140t + 饥饿 160t，落地只留飞溅（肥水坑已移除）
- 大便小怪 ftboss:poop_minion: 16.0 血 / 攻击 2.0 / 移速 0.34 / 跟随 28.0；命中挂 缓慢 80t + 饥饿 100t；600t（30 秒）后消散
- 苍蝇 ftboss:fly_minion: 6.0 血 / 攻击 1.5 / 移速 0.40，600t 后消散；蛆 ftboss:maggot_minion: 12.0 血 / 攻击 4.0 / 移速 0.32，LIFE 600t
- 棕黑史莱姆 ftboss:compost_slime: 继承原版 Slime，LIFE 900t；羊屎蛋 ftboss:sheep_dung: 继承原版 MagmaCube，LIFE 900t，死亡或到期 explode(power = 0.95f)
- 召唤入口: 炫彩堆肥桶 ftboss:compost_summon 右键，在玩家朝向 4 格生成；非主世界直接失败且不消耗；非创造模式消耗 1 个；配方 compost_summon.json = 4 个 create:precision_mechanism + 4 个 bone_meal + 1 个 composter
- 额外材料掉落：无；石像由 `BossMercy.giveStatue` 统一发放。
- 音乐: 一阶段《堆肥·强袭》ftboss:boss_music_compost（Group.COMPOST）；二阶段切《堆肥·熔毁》ftboss:boss_music_compost_p2，切换时对 64 格内玩家先发停止包再 playSound（音量 4.0）

## 应对要点
- 一阶段它在地上抛射、可近身换血；血量 70% 一破就起飞，近战要提前备远程或击落手段。
- 连射冷却随命中次数变快（上限 12 层、最低 16t），中弹越多越难躲；被弹幕黏上先拉开距离等 3 秒清零。
- 沼气爆炸（8 格、8.0 + 阶段 × 1.5 且点燃 4 秒）与俯冲（点燃 3 秒）是主要致死源，看到 gas_charge 抬手就出场。
- 召唤物每种限 10 只，到上限会集体强化而不是继续堆；先清场反而让 buff 集中到新召唤的怪身上。
- 单次伤害无需再受 500 点全局阈值限制；毒、凋零、缓慢对它全部无效，别指望 debuff 输出。

## 关键常量（代码位置）
- `CompostBossEntity.java: P2_AT 0.70f / P3_AT 0.35f；T_FIRE 32 / T_CHARGE 52 / T_SHIFT 44 / T_TRANSFORM 56 / T_HURT 9`
- `CompostBossEntity.java: burstCd 120；burstLeft 4+rnd(2)；burstGap 16；hitStreak 上限 12；lobCd 160 → 140+rnd(80)；gasCd 200 → 200+rnd(100)、gasFuse 40`
- `CompostBossEntity.java: barrageCd 90 → 170+rnd(60)；bombCd 30；dashCd 26+rnd(14)、dashTicks 14、step 0.95；diveCd 200 → 180+rnd(60)、windup 14、diveTicks 40`
- `CompostBossEntity.java: 环绕半径 6.5 + sin × 2.0、每 40t 换向；悬停高度 gp.getY() + 5.0；curseCd 200、概率 0.35f、距离 32.0`
- `RainbowPoopEntity.java: dmg 3.0f；setDamage 内部 ×0.5；命中 缓慢 100t / 反胃 140t / 饥饿 160t`；`PoopCurse.java: DURATION 160 / AMPLIFIER 0`
- `SummonLimit.java: MAX_PER_TYPE 10 / RADIUS 64.0 / BUFF_TICKS 400`；`SummonSweep.java: RANGE 80.0`；`BossDamageGate.java: 旧 500 点阈值（已移除） 500.0`
- `PoopMinionEntity.java: 16.0 血 / 2.0 攻 / 0.34 速；600t 消散`；`SheepDungEntity.java: LIFE 900 / BLAST_RADIUS 0.95F`；`CompostSlimeEntity.java: LIFE 900`
