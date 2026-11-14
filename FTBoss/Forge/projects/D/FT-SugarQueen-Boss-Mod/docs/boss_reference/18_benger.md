# 边牧犬王 benger（第 9 位主 Boss）

- 血量 `MAX_HP = 330.0`（由 `createAttributes()` 挂到 `MAX_HEALTH`）；跟班小狗 `ftboss:benger_pup` 30 血、45 秒后退场，同场上限 `PUP_CAP = 8`。
- 体型 1.7 × 3.5 格（`FTEntities.BENGER` 注册尺寸），模型 19 骨 / 贴图 `textures/entity/benger.png`；8 段动画 idle / walk / run（犬类 bound 步态）/ bite / pounce / roar / hurt / death。
- 召唤入口两份：刷怪蛋 `ftboss:benger_spawn_egg`（黑+白配色）；召唤物【牧羊哨】`ftboss:benger_whistle` 右键、面前 4 格生成，配方 `data/ftboss/recipes/benger_whistle.json`（中心金桶 `farmingtales:gold_bucket`）。仅主世界可召唤，其余维度被 `SummonGate` 拦下。
- 掉落：武器【打狗棒】`ftboss:dog_beating_stick`（`FTWeapons.DogBeatingStick`，掉落走 `BossMercy.giveWeaponDrop`）。
- 音效直接借原版狼（growl / howl / hurt / death），**零新增音效资产**。
- 通用机制：`BossDamageGate`（单次超限无效 + 比例夹取 + 连击递减 + 反逃课）、`BossRage`、`TrapBreaker`、`BossUnstuck`、顶部血条 `BossBars`、`BossMusic`。

## 招式

| 招式 | 关键常量 | 效果 |
|---|---|---|
| 撕咬 | `T_BITE = 10` | 近战命中：伤害 + 流血 7 秒 + 缓慢 + 击退 |
| 狼群扑击 | `PACK_CD = 280`、`PACK_LEAD = 30` | 长嗥后延迟 1.5 秒集体扑出；锁 `PACK_RANGE = 20.0` 格内目标，半径 `PACK_RADIUS = 3.5`，`PACK_DAMAGE = 36.0` |
| 连续下砸 | `SLAM_CD = 340`、`SLAM_COUNT = 3` | 连砸 3 次：每次滞空预警 `SLAM_HANG = 8` tick、间隔 `SLAM_GAP = 22` tick；落点半径 `SLAM_RADIUS = 4.0`，`SLAM_DAMAGE = 39.0` |
| 长嗥 | `ROAR_CD = 320`、`ROAR_RADIUS = 14.0` | 范围内玩家吃减益，自己拿增益；`T_ROAR = 20` tick 动画 |
| 咬住不放 | `CLAMP_CD = 320`、`CLAMP_TICKS = 60` | `CLAMP_RANGE = 5.5` 格内咬住 3 秒：每秒对玩家 `CLAMP_DPS = 20.0`、自己回血 `CLAMP_HEAL = 16.0` |
| 追猎突击 | `CHARGE_CD = 380`、`CHARGE_MIN = 3.5`~`CHARGE_MAX = 22.0` | 有视线的目标进入范围、冷却结束且空闲时优先发动；方向预警 8 tick，沿锁定方向冲刺 16 tick，速度 1.6 格/tick；扫掠命中一次，`CHARGE_DAMAGE = 34.0` + 眩晕 `CHARGE_STUN = 40` tick，遇方块停止 |
| 狗叫 | `BARK_CD = 400`、`BARK_RADIUS = 14.0` | 范围内眩晕 `BARK_STUN = 50` tick（2.5 秒） |
| 附魔金苹果 | `MILK_CD = 560`、`MILK_TICKS = 400` | 给自己上 20 秒增益（附魔金苹果 / 血王子那一类） |
| 打狗棒横扫 | `SWEEP_CD = 220`、`SWEEP_RANGE = 5.5` | 半径 `SWEEP_RADIUS = 4.2` 内 `SWEEP_DAMAGE = 33.0` + 击飞 + 缓慢 |
| 虚化 | `INTANG_CD = 500`、`INTANG_TICKS = 60` | 每 25 秒获得 3 秒无敌；喝奶也进入虚化。客户端同步青蓝半透明模型，透明度在 20%~36% 间脉动，环绕魂火和逆向传送粒子，结束时闪光显形 |

- 动画时长：`T_BITE = 10` / `T_POUNCE = 14` / `T_ROAR = 20` / `T_HURT = 8` / `T_DEATH = 24` tick。
- 场面压力来自"狗多"：跟班 `PUP_CAP = 8` 只、会咬人挂流血、45 秒退场；打狗棒对它类目标额外 6 点真实伤害（穿甲）。

## 音乐

- 专属战斗曲《牧野犬王》：事件 `ftboss:boss_music_benger`，音频 `assets/ftboss/sounds/music/benger.ogg`，**3:05 = 3708 刻**；`BossMusic.Group.BENGER`，`trackTicks()` 用 `BENGER_TICKS = 3708`。
- 唱片 `ftboss:music_disc_benger`（显示名"边牧犬王唱片"），`RecordItem` 长度同为 **3708**。
