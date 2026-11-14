# 沙耶
`实体 id`: ftboss:tea_tree ｜ 源文件: TeaTreeBossEntity.java + boss/ai/TeaTactics.java + boss/ai/SayaBuds.java + entity/TeaMinionEntity.java

## 一句话定位
扎根不动、白天不烧、免疫负面效果的铁魔法远程 Boss（茶树古树）：根刺、茶毒、法术连发、茶芽小妖与守护茶芽；半血开盾免伤，首次死亡二命重生。

## 基础数值
| 项目 | 数值 | 说明 |
|---|---|---|
| 血量 | 340 | MAX_HP 340.0 |
| 护甲 | 10.0 | Attributes.ARMOR（注释记录由 6 提到 10） |
| 攻击力 | 3.5 | ATTACK_DAMAGE，仅用于 3 格内轻推；注释写 5 到 7，实际 3.5 |
| 移动速度 | 0.0 | MOVEMENT_SPEED，扎根不动 |
| 跟随范围 | 40.0 | FOLLOW_RANGE |
| 击退抗性 | 1.0 | KNOCKBACK_RESISTANCE，完全免疫击退 |
| 经验 | xpReward 150 | 另有 ExperienceOrb 150 |
| 碰撞箱尺寸 | 1.3 × 2.1 格 | FTEntities.TEA_TREE 注册尺寸 |
| Boss 条颜色 | GREEN，PROGRESS，半径 48 | 构造器 new ServerBossEvent；小妖不显示 |
| 是否定身不移动 | 是 | 移速 0，registerGoals() 为空，故不调用 BossUnstuck |
| 召唤物与掉落物 | 茶芽小妖（满编 3 / 半血 5）+ 守护茶芽 5 株 | 掉落 树叶 24、树苗 8、绽放的荆棘 1、荆棘头冠 1、石像·沙耶、经验球 150 |

## 阶段机制
- 二阶段「茶心觉醒」: health/max < 0.5 时置 phase2（只一次），播 BEACON_ACTIVATE 1.0/0.6 并 buds.trigger() 开盾。
- 二阶段数值变化: 根刺 CD 100 降 62、茶毒 160 降 100、castCd 62 降 44、茶芽小妖满编 3 升 5、补员间隔 150t 缩到 90t。
- 茶芽庇佑护盾: shielded() 为真时 hurt() 直接 return false，完全免伤；5 株守护茶芽全被清除时播 BEACON_DEACTIVATE 与 GLASS_BREAK、放 45 点 CRIT 粒子并解除免伤；区块重载后场上仍有守护茶芽会自动重新接管。
- 二命「茶根再生」: BossHandler.onBossDeath 先调 tryRevive()，成功则取消死亡事件：血设回 30%（102/340）、removeAllEffects()、fangCd 40、spitCd 70、buds.reset() 后再次 trigger()；全场只一次（secondLifeUsed）。
- 生命提升成长: 每 1200t（60 秒）且 48 格内有玩家时 +1 级（每级 +4 最大生命，上限 10 级即 +40），升级时 heal(4) 并播报。
- 通用狂暴 BossRage: ≤50% 狂暴 I，≤25% 狂暴 II，给 迅捷 I 与 抗性 I（狂暴 II 追加 力量 I、再生 I），各 80t、每 20t 刷新；沙耶不在 BossEmpower 名单内，只受 BossScaling 人数倍率影响。

## 技能与机制
### 根刺爆发
- 触发: fangCd 到时且距离在 1.5 到 16.0 格之间；CD = 100（一阶段）/ 62（二阶段）
- 效果: 沿目标方向每 1.6 格生成一根原版 EvokerFangs，数量 9+rand(3) 即 9 到 11 根，warmupDelay = 20 + i×4 tick，高度取 MOTION_BLOCKING；伤害取原版 EvokerFangs 默认值（代码里未写数值 ?）；音效 ROOTED_DIRT_PLACE 1.0/0.7
### 茶毒飞沫
- 触发: spitCd 到时且距离 <16.0 格；CD = 160（一阶段）/ 100（二阶段）
- 效果: 给目标 中毒 amp2 与 220t，并造成 2.5 点 indirectMagic（原 5 已折半）；音效 CAVE_VINES_PICK_BERRIES 1.0/1.3
### 铁魔法连发与专属自施法
- 触发: castCd 62（一阶段）/ 44（二阶段）；队列空时先填 2 个互不相同的法术，之后逐个 initiateCastSpell
- 效果: 法术等级 = clamp(6 + 狂暴层级, min, max)，施法前反射写入 TargetEntityCastData 锁定玩家；狂暴层级 ≥1 时 80% 概率换成 spellPoolRare(8, 3)（3 = EPIC）；另有 35% 概率追加 SAYA_SELF_SPELL_FIELDS = OAKSKIN_SPELL 或 ABSORPTION_SPELL（树皮护体 / 伤害吸收）
### 贴身轻推
- 触发与效果: contactCd 30t、距离 <3.0 格时 doHurtTarget，用 3.5 攻击力
### 茶芽小妖召唤
- 触发: TeaTactics.tickMinionSummon，minionCd 150（一阶段）/ 90（半血）
- 效果: 统计 48 格内的茶芽小妖（守护茶芽不计入），上限 3 只（半血 5 只），每轮最多补 3 只；落点半径 2.5 到 6.5 格；场上已有者回满血并同步目标
### 茶心汲取
- 触发: absorbCd = 240t（12 秒）且自身血量低于满血 0.5 以上
- 效果: 24 格内最多汲取 3 株茶芽小妖（守护茶芽除外），每株回 7.5% 最大生命即 25.5 点并叠 1 层共鸣，上限 4；被汲取的小妖静默 discard 不掉落
- 效果（强化）: 自身获得 力量 400t amp = min(1, 层数-1)、抗性 400t amp = 层数 ≥3 时 1 否则 0、吸收 400t amp = min(1, 层数/2)；32 格内播报 msg.ftboss.saya_absorb
### 茶芽庇佑
- 触发: 半血时 buds.trigger()；二命重生后再触发一次
- 效果: 召唤 5 株守护茶芽（最大生命 +100% 即 20 变 40、xpReward 12、持久化、显示名 entity.ftboss.guard_bud），按 2π/5 均分角度、半径 4.5 到 7.0 格落位
- 效果（供能）: 每 40t 给沙耶 再生 amp1 与 抗性 amp1 各 140t；每 5t 一圈绿色尘粒子；每 100t 提醒剩余株数；被攻击时给攻击者"先破盾"提示（限频 40t）；统计半径 64、提示半径 48
### 常驻茶buff与随机轮盘
- 光环: 每 10t 检查 herbalbrews:renewal，缺失或剩余 <120t 时续 600t 与 amp1（焕新 II）
- 轮盘与免疫: buffWheelCd = 110+rand(60) 时从 11 种原版增益随机取 2 个（100+rand(120)t、等级 0 到 2）；每 20t 清空自身非增益并被 DENY 一切负面效果
### 反制与通用规则
- 原“单次 >500 点整下无效”规则已取消；免疫 armamentarium 流血与真实伤害；传奇怪物式限伤 LIMIT_ENABLED = false；Boss 阵营友伤免疫；主世界限定召唤
- 破困 TrapBreaker 每 10t 清身上的矿车、船与坐垫类方块；白天不烧（isSunBurnTick 为 false）

## 召唤物 / 掉落 / 音乐
- 入口: 茶树种子 ftboss:tea_seed 右键消耗 1 个，落在视线前 3 格，音效 CROP_PLANTED 1.0/0.8；非主世界被 SummonGate 拦下
- 召唤物清理: 死亡时 dropRewards 与 remove() 都调 SummonSweep.clear(TeaMinionEntity.class)（半径 80），守护茶芽随 buds.reset() 移除
- 掉落: 橡树树叶 24、橡树树苗 8、绽放的荆棘 1 把（FTBossItems.BLOOMING_THORNS）、荆棘头冠 1 顶（FTBossItems.THORN_CROWN）、石像·沙耶、ExperienceOrb 150，另有 xpReward 150
- 荆棘头冠（头盔）：护甲 4、护甲韧性 3、击退抗性 10%、耐久 528；用藤蔓修复。近战实受伤害的 25% 作为荆棘反击，单次最低 2、最高 6 点，冷却 10 tick（0.5 秒）；不反击荆棘、弹射物、爆炸、环境伤害或友方，不绕过目标减伤。仅头盔栏佩戴时生效；视野为中心淡红、边缘深血红，摘下立即恢复。最终击败沙耶必掉 1 顶，复活阶段与怜悯离场不掉。
- BGM: 茶の唄，music/saya.ogg（Group.SAYA）整场不切
- 专属规则: 只有 20 格内有玩家时才执行根刺、茶毒、施法、轻推（nearestTarget(20.0) 为 null 就返回，但光环、轮盘、生命提升、汲取仍会跑）；连死怜悯 5 次离场且无掉落无经验

## 应对要点
- 半血后必须先清 5 株守护茶芽：护盾期间 hurt() 直接返回 false，任何伤害都打不掉沙耶一滴血
- 守护茶芽不能被"茶心汲取"吃掉（isGuardBud 被跳过），只能硬打：每株 40 血、经验 12，且会追人
- 根刺是沿直线铺开的 9 到 11 根尖牙，侧向移动即可躲开大部分；16 格内既是技能圈也是施法目标圈
- 别让 24 格内堆满茶芽小妖：每 12 秒它汲取最多 3 株，每株回 25.5 点血并叠力量、抗性、吸收
- 拖久了对你不利（每 60 秒最大生命 +4，最多 +40），且两次"击杀"才算真死：第一次只回 30% 血并再放一轮庇佑

## 关键常量（代码位置）
- `TeaTreeBossEntity.java: MAX_HP 340.0 / ARMOR 10.0 / ATTACK_DAMAGE 3.5 / MOVEMENT_SPEED 0.0 / KNOCKBACK_RESISTANCE 1.0 / FOLLOW_RANGE 40.0 / BOSS_BAR_RANGE 48.0`
- `TeaTreeBossEntity.java: FANG_CD 100 或 62 / SPIT_CD 160 或 100 / castCd 62 或 44 / 法术等级 6 + 狂暴层级 / 队列 n 2 / 自施法概率 0.35 / 根刺 9+rand(3) 根 step 1.6 warmup 20+i×4 / 茶毒 amp2 220t 与 2.5 点`
- `TeaTreeBossEntity.java: HP_BOOST_INTERVAL 1200 / HP_BOOST_MAX 10 / HP_BOOST_PER_LEVEL 4.0f / 二命回血 ×0.3 且 fangCd 40 与 spitCd 70`
- `TeaTactics.java: MINION_RANGE 48 / minionCd 150 或 90 / 满编 3 或 5 / ABSORB_RANGE 24 / absorbCd 240 / 每次 3 株 / 层数上限 4 / 每株 7.5% 最大生命`
- `SayaBuds.java: BUD_COUNT 5 / BUFF_CD 40 / REMIND_CD 100 / SCAN 64 / PLAYER_RANGE 48；TeaMinionEntity.java: MAX_HEALTH 20.0 / ATTACK_DAMAGE 1.5 / MOVEMENT_SPEED 0.28 / FOLLOW_RANGE 24 / xpReward 4（守护茶芽 12 且生命 +100%）`
