# 故障机器人

`实体 id`: ftboss:defect_core ｜ 源文件: DefectCoreEntity.java（技能在 boss/ai/DefectOrbs.java，球体在 entity/ChargedOrbEntity.java）

## 一句话定位
悬浮型法师 Boss：本体不追人，靠环绕的充能球循环施法；血量 70% 与 35% 各开一阶段，球槽从 5 涨到 11，后期还有超能光束与闪电落雷。

## 基础数值

| 项目 | 数值 | 说明 |
|---|---|---|
| 血量 | 404.0 | MAX_HEALTH，createAttributes() |
| 护甲 | 4.0 | ARMOR；不在 BossEmpower 名单内 |
| 攻击力 | 9.0 | ATTACK_DAMAGE；仅贴脸 doHurtTarget 使用 |
| 攻击速度 | 1.0 | ATTACK_SPEED |
| 移动速度 | 0.16 | MOVEMENT_SPEED；位移由 tick 传送控制 |
| 跟随范围 | 40.0 | FOLLOW_RANGE |
| 击退抗性 | 1.0 | KNOCKBACK_RESISTANCE |
| 经验 | 120 | xpReward，构造器 |
| 碰撞箱 | 2.4 × 3.8 格 | FTEntities.DEFECT_CORE register |
| Boss 条 | PURPLE / PROGRESS | 名后附" [阶段 I/II/III]"，只发 48 格内玩家 |
| 是否定身 | 否，悬浮 | setNoGravity(true)；漂移半径 3.5 格、高度 = 目标 Y + 1.3 + sin × 0.2、位移上限 0.11 格每 tick |
| 召唤物 | 充能球 ftboss:charged_orb，蛆 ftboss:maggot_minion | 见下 |
| 掉落物 | 故障机器人石像 | 真死时由 BossMercy.giveStatue 发放 |
| 人数缩放 | 血量 ×1.25^(n-1)，技能伤害 ×(1+0.15(n-1)) | BossScaling；自定义伤害统一走 BossScaling.dmg() |
| 全局限伤 | 原“单次超过 500 点整下无效”规则已取消 | 反逃课 36.0 因 LIMIT_ENABLED = false 未生效 |

## 阶段机制
- 三阶段：PHASE2_AT = 0.70F、PHASE3_AT = 0.35F；tickPhases() 里 want = ratio < 0.35 ? 3 : (ratio < 0.70 ? 2 : 1)，只前进不回退。
- 球槽 = min(11, 5 + 3 × (phase-1)) → P1 5 / P2 8 / P3 11；基础出手间隔 P3 36 / P2 48 / P1 60t。
- 球体寿命 = 400 + (phase-1) × 80t（黑球再 +240）；光束冷却 P3 200 / P2 280 / P1 360t，回想形态 ×0.7。
- 全局 BossRage：≤50% 狂暴 I、≤25% 狂暴 II，但本体走"纯进攻档"（只给迅捷与力量，不给抗性/再生，见 refreshRage() 的 offenseOnly 分支）；突破播 T_PHASE = 52t 动画并在 48 格内提示。

## 技能与机制

### 基础出手
- 触发: 出手冷却归零，且非衰弱、非光束蓄力、有目标、orbsCastCooldown <= 0
- 效果: 距离 < 3.4 格用 doHurtTarget（9.0 × BossScaling）；< 26.0 格施铁魔法，法术取不到时回退 4.5 + phase × 1.5 点 indirectMagic（不减半）

### 充能球循环
- 触发: 每 48t（回想形态 80t）一个周期，先补满空槽，再吸掉一颗已有球
- 效果: 球生成在 ±1.4 格、Y + 1.6；吸收成熟度 chargeTicks / lifeTicks 最高的一颗，释放延迟 玻璃 11 / 电 9 / 冰 6 / 离子 6 / 黑 26t，之后本体 50t 施法冷却
- 拆球: 任何伤害都能打爆球；`PUNISH_CD = 40` —— **v110 把判定补上了**：拆球后 40t（2 秒）内周期块只做"激活已有球"、**不补空槽**（以前这个字段只赋值与自减、从没被读取，"拆球停补"等于没实现；只挡补球不挡激活，免得拆一颗球就让 Boss 哑火 2 秒）

### 玻璃球（AOE）
- 触发: 被吸收且铁魔法施放失败时走内置回退
- 效果: 8 格内每名玩家各吃 20 × orbDamageMul × BossScaling 的 0.25 物理 + 0.25 魔法（约 10 点）+ 缓慢 60t

### 电球（单体雷击）
- 触发: 被吸收时；无目标则空放
- 效果: 目标吃 30 × orbDamageMul × BossScaling 的 0.25 物理 + 0.25 魔法（约 15 点）+ 缓慢 50t III 级；每颗电球让 lightningUsed +1

### 黑球（蓄力）
- 触发: 被吸收时；不走铁魔法，改调 launchDarkOrb()（case DARK 分支是不可达死代码）
- 效果: 额外伤害 2.0 × (chargeTicks / 20)、球存活 200t；回退伤害 (16 + min(chargeTicks/14, 30)) 的一半 + 反胃 120t；charge 上限 60，每满 6 点触发闪电打击（BossScaling.dmg(12 + phase × 6) 的一半、半径 32 格）

### 冰球（护盾）
- 触发: 被吸收时
- 效果: 本体得 吸收 200t（I 级）+ 铁魔法冰系；场上每颗冰球再给 吸收 60t + 抗火 90t

### 离子球（充能与加速）
- 触发: 被吸收时
- 效果: 本体 charge +12（上限 60）+ 速度 II 260t；每颗离子球再给一组增益，等级 min(球数-1 + 阶段-1, 3)、90t

### 超能光束
- 触发: 光束冷却归零（初值 240t，无目标重置 40t）+ 24 格内有玩家 + 无排队施法；动画 T_BEAM = 285t
- 效果: 蓄力 160t（8 秒）定身并剥掉自身 抗性/吸收/抗火/生命提升，给 40 格内玩家 速度 I 190t；开火 60t、每 10t 结算一次，沿射线 40.0 格、垂直 ≤ 3.5 + 目标半宽命中，每脉冲把 22f 拆成四系各 1/8（约 11 点，一次 6 脉冲约 66 点）+ 击退 0.32；结束进入落地衰弱

### 落地衰弱
- 触发: 光束与电流相生结束等由 enterExhausted() 调用
- 效果: 120t（6 秒）不出手、不漂移、每 tick 最多下沉 0.9 格；每 5t 回 总量/24，普通衰弱总量 80.0；代码中**没有**受伤倍率（已去掉虚弱期 ×1.5，广播文案未改）

### 电流相生
- 触发: phase >= 2（代码判定；v110 已把本法里 6 处"三阶段"旧注释与日志统一改成二阶段）且 currentCd 归零（初值 400t）且 lightningUsed >= 2，否则 currentCd 重置 100t
- 效果: 落 min(lightningUsed, 12) 道纯视觉闪电，落点 ±3 格；落点 12.0 平方格内玩家吃 BossScaling.dmg(12 + phase × 6) 点 lightningBolt 伤害（全额）；代价 enterExhausted(100, 0.10f) 名义回 40.4、实回约 33.7，之后 currentCd = 600t

### 集中碎片（一次性）
- 触发: 尚未使用过且 phase >= 2
- 效果: orbDamageMul 1.0 → 1.5（球伤害 +50%）、maggotBase 2 → 5、currentCd 提前到 min(currentCd, 200)

### 蛆群召唤
- 触发: phase >= 2，maggotCd 归零（初值 200t，之后 240t）
- 效果: 召唤 maggotBase + rnd(2) 只蛆（碎片前 2~3、之后 5~6），半径 2.0 环形出生；不走 SummonLimit，无数量上限

### 回想形态
- 触发: BossRage.tierOf() >= 1（≤50% 血）或 phase >= 2，只开一次；T_ECHO = 28t
- 效果: 球周期 48t → 80t；铁魔法等级 +2、稀有度门槛抬到传说；每次法术 16t 后原样复读；球冲刺改双撞；光束冷却 ×0.7

### 反制与免疫
- 免疫充能球伤害与 ftboss 阵营伤害（isInvulnerableTo）；免疫一切负面效果（每 20t 自清 + 全局 DENY）
- 原“单次超过 500 点整下无效”规则已取消；免疫流血/真实伤害；removeWhenFarAway() = false；死亡时 SummonSweep.clear(80 格, 蛆)

## 召唤物 / 掉落 / 音乐
- 充能球 ftboss:charged_orb: 20.0 血 / 移速 0 / 击退抗性 1.0 / 跟随 48.0 / 经验 0 / LIFE 400t（黑球 640t）；冲刺每 tick 1.35 格，撞人伤害 12 + 6 × phase（18 / 24 / 30）、冷却 100t，命中给 0.25 物理 + 0.25 魔法并击退 1.5，玻璃球另有半径 4.0 的 0.15 + 0.15 溅射；黑球满充能撞击 5% 概率放黑洞
- 蛆 ftboss:maggot_minion: 12.0 血 / 攻击 4.0 / 移速 0.32 / LIFE 600t；命中叠【感染】（上限 10 层、单层 600t），玩家带感染攻击敌人时自伤 = 层数；只吃玩家伤害
- 召唤入口: 右键 ftboss:defect_core（堆叠 16，EPIC）在视线前 4 格生成，可同时召唤多只；非主世界直接失败且不消耗
- 掉落：故障机器人石像 `ftboss:statue_defect`；死亡超载爆炸威力 `4.0f`，`ExplosionInteraction.NONE` 不毁地形。
- 音乐: 专属《故障机器人》ftboss:boss_music_defect_robot（music/defect_robot.ogg，Group.DEFECT）；空场 5 秒宽限后停

## 应对要点
- 拆球是主要节奏点：球有独立冲撞行为，拆球触发 40t 停补；2026-09-17 起拆掉正在聚焦的球还会取消该次激发，排队球被拆则移出队列。
- 超能光束蓄力 8 秒会剥掉它自己的抗性/吸收/抗火/生命提升且定身，这是最佳爆发窗口；每脉冲约 11 点、全中约 66 点。
- 光束结束与电流相生之后它都会进入落地衰弱：不还手、不漂移，且没有额外受伤加成，抓紧补刀。
- 优先拆冰球（护盾）与离子球（充能 + 加速），压低施法等级与闪电打击频率；黑球拖越久越疼。
- 原“单次伤害超过 500 点整下无效”规则已取消；毒、凋零、流血对它全部无效。

## 关键常量（代码位置）
- `DefectCoreEntity.java: PHASE2_AT 0.70F / PHASE3_AT 0.35F / OVERLOAD_POWER 4.0F / EXHAUST_TICKS 120 / exhaustHealTotal 80f`
- `DefectCoreEntity.java: T_BEAM 285.0 / T_CAST 20.0 / T_PHASE 52.0 / T_SUMMON 17.0 / T_ECHO 28.0；悬浮半径 3.5 / 高度 +1.3 / 步进 0.03`
- `DefectOrbs.java: SLOTS_BASE 5 / SLOTS_PER_PHASE 3 / SLOTS_MAX 11；CYCLE 48 / ECHO_CYCLE 80 / ECHO_DELAY 16 / PUNISH_CD 40（v110 起真正生效）`
- `DefectOrbs.java: BEAM_CHARGE 160 / BEAM_DURATION 60 / BEAM_PULSE 10 / BEAM_DMG 22f / BEAM_LEN 40.0 / BEAM_RADIUS 3.5 / CHARGE_MAX 60`
- `DefectOrbs.java: CURRENT_MAX_ORBS 12 / CURRENT_EXHAUST 100 / CURRENT_REGEN 0.10f；ORB_WEIGHTS 按阶段换表`
- `ChargedOrbEntity.java: LIFE 400 / DASH_SPEED 1.35 / SLAM_DMG_BASE 12f / SLAM_DMG_PER_PHASE 6f / SLAM_CD 100`
- `MaggotMinionEntity.java: LIFE 600 / MAX_STACKS 10 / STACK_TICKS 600`；`BossDamageGate.java: 旧 500 点阈值（已移除） 500.0`

## 2026-09-17 模型与技能联动

五种球改为棱晶容器、六极电容、六瓣冰晶、交叉磁环和破裂暗核，各有八段骨骼动作与独立发光贴图。
补球沿弧线进入环绕槽，选球聚焦后按 11 / 9 / 6 / 6 / 16t（玻璃 / 雷电 / 冰霜 / 离子 / 黑暗）结算。
黑暗直接发射所选球；其他球回流收拢，消散动画 6t。成熟球不会重复入队，队列上限 11。
超能光束期间球围绕本体输能，连线端点实时跟随胸口与双手骨骼；被动、阶段突破、回想、集中碎片也有对应脉冲。
伤害、阶段槽位、光束蓄力与持续时间沿用原数值。实现与验收说明见 `tools/model3d/defect_orbs_v2/README.md`。
