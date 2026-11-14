# 故障核心 · 充能球 Boss 模组规划（Slay the Spire「故障机器人」机制融合）

> 目标：在现有 `ftboss` 工程（Forge 1.20.1 + GeckoLib 4.8.4 + Iron's Spells）里，
> 新增**第 6 个 Boss「故障核心 / Defect Core」**：三阶段核心战 + 三色充能球系统 + 故障球封印 + 掉落进度线。
> 文中所有类名/API 都对齐本工程现有代码与已核实过的第三方接口，力求"照做即可编译"。

---

## 0. 设计支柱（先定调，避免后期走形）

1. **球是本 Boss 的语言**：玩家看到球 → 就能预判接下来会发生什么（雷电=反击、冰霜=护盾、黑暗=倒计时压力）。
2. **阶段 = 规则升级**，不是单纯加数值：P1 只会闪电球，P2 加入冰霜并解锁"召回"，P3 加入黑暗与"过载"。
3. **惩罚可读、可反应**：故障球封印物品栏是**短时(6s)、有图标、有倒计时**，且**绝不移动/删除玩家物品**。
4. **不和整合包打架**：不毁地形、不改经济模组、不动别人模组的数据；对真实伤害/流血沿用既有免疫口径。
5. **复用而非重写**：限伤、狂暴、防卡、连死怜悯、血条、粒子工具、模型生成器全部沿用现有组件。

---

## 1. Boss 本体与阶段

| 项目 | 数值/规则 |
|---|---|
| 实体 id | `ftboss:defect_core`（`DefectCoreEntity`） |
| 生命 | **404**（纪念《杀戮尖塔》故障机器人，1.44×现有 280 档） |
| 体型/碰撞箱 | 2.0 × 3.2 格（悬浮，`noGravity`，贴地 1.2 格悬停） |
| 护甲/抗击退 | ARMOR 8 / KNOCKBACK_RESISTANCE 1.0 |
| 阶段阈值 | **P1 ≥70%（≥282.8）** → **P2 35%~70%（141.4~282.8）** → **P3 <35%（<141.4）** |
| 悬浮移动 | 悬停并环绕玩家（复用 `SugarQueenBoss` 的悬停写法：目标上方 2 格 + 0.25/t 逼近） |
| 死亡演出 | 3t 蓄力（核心收缩 + 音效渐强）→ **核心超载爆炸**：`ExplosionInteraction.NONE`、威力 8、半径内玩家吃 20 点伤害；20t 后在**半径外**生成掉落（防止爆炸销毁掉落物） |
| 免疫 | 沿用 `BossDamageGate`：`armamentarium:*` 真实伤害/流血全免疫、单次上限、连击递减、反逃课距离（本 Boss 建议 36 格） |
| 负面效果 | 沿用"Boss 免疫一切负面效果"的 `MobEffectEvent.Applicable` DENY（第 6 个类型加进白名单） |
| 防卡 | `BossUnstuck.tick`（悬浮 Boss 也会被方块卡住判定误伤 → 在 `BossUnstuck` 里加一个"悬浮 Boss 跳过位移判定"的开关） |
| 连死怜悯 | 加进 `BossMercy.typeOf`（同种连死 5 次 → 离场、无掉落无经验） |
| 血条 | `BossBars.track`，颜色 `PURPLE`，进度=生命；阶段切换时改名（`§d故障核心 §7[阶段 II]`） |
| 战斗 BGM | 复用 `BossMusic.request`，新增一幕"机械"乐段（可先用现有三幕剧的第 2 幕占位） |

---

## 2. 充能球系统

### 2.1 数据模型（**不落静态字段**）

> 教训：本工程女王/屠夫用静态字段做"单例大脑"，导致无法多只同种 Boss。新 Boss 一律**把状态放在实体实例上**。

```java
// DefectCoreEntity 内部
private final List<Orb> orbs = new ArrayList<>();     // 有序：index 0 = 最老
private int orbSlots = 3;                             // 槽位上限（3 → 5 → 7 → 9 → 11）
private int autoEvokeCd = 100;                        // 自动激发计时器

public static final class Orb {
    public OrbType type;      // LIGHTNING / FROST / DARK / GLITCH
    public int age;           // 存活 tick（黑暗球伤害随它增长，且决定自动激发顺序）
    public float rot;         // 自转相位（渲染用）
    public boolean corrupted; // 是否已被"故障回声"腐化
}
```

- 球**不是实体**（避免 11 个实体带来的 tick/AI/保存负担与卡顿）：数据在实体上，视觉用 **GeckoLib 骨骼**呈现。
- **存档**：`addAdditionalSaveData/readAdditionalSaveData` 写 `orbs` 列表 + `orbSlots`（**实例 NBT**，不是 SavedData；Boss 本身是实体）。
- **上限**：`orbSlots` 封顶 **11**（3 + 2×4 次回声）；同类型球同时存在上限 4，超出则把最老的转成故障球。

### 2.2 三色球：被动 / 激发 / 触发源

| 球 | 被动（在场时常驻） | 激发效果（消耗该球） | 触发源 |
|---|---|---|---|
| **闪电球** | Boss 每 40t 向最近玩家放一道小电弧（3 点魔法伤害） | 对 8 格内所有玩家落雷：**8 点闪电伤害 + 感电 3s** | **受击时**（每次受击消耗 1 颗，最多 2 颗/次） |
| **冰霜球** | Boss 获得 1 层吸收(上限 6 层)，每 60t 刷新 | 给 Boss **吸收 II 8s + 抗性 I 6s**，并冻结 4 格内玩家 1.5s | **Boss 施法时**（每施法消耗 1 颗） |
| **黑暗球** | 无被动；`age` 每 tick +1 | 对最近玩家造成 `4 + min(age/20, 16)` 点无视护甲的魔法伤害（**封顶 20**），并施加"黑暗" 4s | **超时**（`age ≥ 240t` 自动激发）/ **P3 过载**（一次性全激发） |
| **故障球** | 无被动；外观为紫黑裂纹球 | 命中玩家时给 **「故障封印」6s**（见 §3.3） | 只由"故障回声"产生，激发源同黑暗球（超时 200t） |

**激发顺序**：P1 只出闪电；P2 起冰霜；P3 起黑暗。自动激发每 `autoEvokeCd`（P1 140t / P2 110t / P3 80t）激发**最老的一颗**并播放机械音。

### 2.3 生成规则（每 50t 补一颗，直到填满槽位）

| 阶段 | 闪电 | 冰霜 | 黑暗 |
|---|---|---|---|
| P1 | 70% | 30% | 0% |
| P2 | 50% | 30% | 20% |
| P3 | 35% | 25% | 40% |

---

## 3. Boss 技能与元素效果

### 3.1 阶段技能表

| 阶段 | 新增技能 | 效果 |
|---|---|---|
| **P1 启动** | 电弧冲击 | 前方 8 格扇形：10 点伤害 + 击退（用 `dashSlam` 同款结算写法） |
| | 充能 | 立即补满槽位（每 200t 一次） |
| **P2 过载** | 冰霜护盾 | 消耗全部冰霜球，每颗给 2 层吸收 + 2s 抗性 |
| | 召回 | 把所有球收束成一次 6 格 AoE：每颗造成 6 点伤害（不分颜色） |
| | 闪现 | 6 格内瞬移（借用 `BossUnstuck` 的安全落点逻辑），冷却 160t |
| **P3 崩坏** | 黑暗吞噬 | 以自己为中心 10 格黑洞吸附 2s，之后 14 点伤害 + 缓慢 II |
| | **过载** | 全部球同时激发（一次性爆发），然后 `orbSlots` 重置为 3、进入 8s 虚弱（给玩家窗口） |
| **全局** | **故障回声** | 每累计损失 **20%** 生命（323.2 / 242.4 / 161.6 / 80.8 四个阈值，各触发一次） |

### 3.2 故障回声（核心机制）

触发时依次执行：
1. 随机把 **1 颗球腐化为故障球**（不足则先生成 1 颗再腐化）；
2. **槽位 +2**（`orbSlots = min(11, orbSlots + 2)`）；
3. 全屏视觉/音频提示（`Fx` 粒子环 + `BLOCK_ANVIL_LAND` 变调）；
4. 立刻补 2 颗新球（避免槽位空着没有压迫感）；
5. 动作栏提示玩家：`§5故障回声 §7—— 球槽 5/11，1 颗球被腐化`。

### 3.3 故障封印（对玩家的"禁用物品栏 + 饰品效果"）—— **安全实现是重点**

**默认实现：封印模式（不改动任何物品，零丢失风险）**

- 新增效果 `ftboss:glitch_seal`（自定义 `MobEffect`，`isBeneficial()=false`，颜色紫黑，**可见图标 + 倒计时**）。
- 持有期间由事件处理器执行：
  - `PlayerInteractEvent`（左右键）→ 取消；
  - `LivingEntityUseItemEvent.Start/Tick/Finish` → 取消（含进食/喝药/拉弓/举盾）；
  - `AttackEntityEvent` → 取消（不能挥击）；
  - `PlayerTickEvent`（每 tick）→ **移除玩家身上所有 `isBeneficial()` 的效果**（这一条覆盖"药水 + 绝大多数以 MobEffect 形式实现的饰品效果"）；
  - `ItemTossEvent` → 取消（封住"丢东西"这条绕过路径）。
- **完全不动物品栏与 Curios 栏**：不解装备、不移动、不删物品 → 从工程上排除丢装备风险。
- 明确写入文档的**能力边界**：以"属性修饰符"方式生效的饰品（如纯 +最大生命/+攻击）不会被封印；这是为安全性做的取舍。

**可选强化：快照模式（默认关闭，配置项 `sealMode = SEAL | SNAPSHOT`）**
- `CuriosApi.getCuriosInventory(player)` → `ICuriosItemHandler` 取全部已装备饰品 → 写入 `IItemHandler` 的"临时保管"（**先整份 NBT 快照存档**，再移出）。
- 到期/死亡/掉线/服务器重启时**必须**完整还原；还原失败则**掉落在玩家脚下**（永不删除）。
- 快照落 `SavedData`（`DefectSealData`），登录时自检未还原项。
- 只有在玩家明确要求"饰品真的摘下来"时才启用；风险与收益在实现日志里写清。

---

## 4. 模型、贴图与动画

### 4.1 设计与骨骼

**外观基准(用户提供的参考图)**: `桌面\FT-模型检查\参考_故障机器人.jpg`(《杀戮尖塔》故障机器人头像特写) ——
**裂开的金色/黄铜外壳**(边缘呈锯齿状崩口) + **青蓝色玻璃眼球(一大一小)** + 深蓝灰**罗纹机械颈** + **青绿披风**(搭在肩后) + 悬浮爪臂 + 雷电氛围。
关键取舍:**"眼球就是充能球"** —— 参考图里的玻璃球正好是 Boss 的球槽视觉母题, 所以球不加新造型, 直接沿用玻璃球语言。

**配色表(实测取色)**:

| 材质 | 基色 (RGB) | 变化 |
|---|---|---|
| 金色外壳 | 208,178,118 | 崩口/裂纹压暗到 120~150 档; 横向压痕 -34 |
| 外壳暗部(胸甲/下颌) | 170,140,86 | — |
| 头顶锯齿残片 | 224,194,130 | 崩口 168,138,84 |
| 青蓝玻璃 | 112,208,240 | 高光 232,250,255; 眼圈 54,132,186 |
| 罗纹机械金属 | 62,72,88 | 罗纹行 44,52,66 |
| 青绿披风 | 72,150,158 | 折痕 52,116,124 |
| 爪臂 | 188,158,104 | 关节 150,120,66 |

| 骨骼 | 作用 | 备注 |
|---|---|---|
| `root` | 整体悬浮/抖动/死亡旋转 | 所有骨骼的父级 |
| `neck` | 罗纹机械颈(3 段) | 底盘, y 0~7 |
| `body` | 金色裂壳躯干 + 两侧崩口块 + 下颌 + 胸甲 | y 7~31, 崩口块**错位**制造锯齿感 |
| `helm` | 头顶 4 片锯齿残片 | 高度不一 => "碎掉的王冠" |
| `eye` / `eye_small` | 大玻璃眼球(4 层递进做球感) + 小眼球 | 金属眼圈 + 玻璃 + 亮面 |
| `core` | 胸口发光核心(自转) | 眼圈 + 玻璃 |
| `cape_l` / `cape_r` / `cape_back` | 青绿披风(只盖后半身) | 初版做大成"墙"挡住本体, 已收窄 |
| `arm_l` / `arm_r` / `arm_back` | 三根悬浮爪臂 | 激发对应球时甩臂 |
| `orb_socket` | 充能球挂点(M2 起使用) | 头顶上方, 悬浮 |

**已实现(2026-09-11 升级为 v3 人形)**: **44 cube / 20 骨骼(真父子层级)** / 256×256 贴图 + `defect_core_glowmask.png` 发光遮罩(1265 像素,
由生成器按材质掩码自动导出, 渲染器挂 `AutoGlowingGeoLayer`)。
造型 = 金色裂壳头(青蓝玻璃大眼 + 小眼 + 4 片锯齿残冠) + 深色裂开躯干(透蓝光裂纹) + **胸口自转发光核心** +
罗纹机械颈 + 金色肩甲 + **与肩明显断开的悬浮机械手**(3 根爪指) + 青绿披风 + 深色分段腿 + 金色脚甲。
四视图对照稿: `桌面\FT-模型检查\故障核心_v3三视图.png`。

> 出对照稿的用法(已给 `render_tex.cjs` 加上 back/right 视图与视图选择):
> `FT_VIEWS=front,right,back,iso FT_PANEL=330 FT_SCALE=6.6 FT_OY=290 node render_tex.cjs <geo> <tex> <out.png> 2 3.2`
> (末尾两参数 = 碰撞箱宽/高, 用来画地面基准线。)
> 注意: 骨骼必须建**父子层级**, 扁平列表里动 root/torso 不会带动子部件(v1 踩过)。

> 实现要点：GeckoLib 里按"球的类型/是否为空"逐帧设置骨骼可见性与颜色（用 `RenderLayer` 或 `GeoBone#setHidden` + 独立 `RenderType`），
> 不要为每颗球生成独立模型文件。

### 4.2 贴图

- 尺寸 **128×128**，分区：铜件（含铜锈绿斑）/ 深板岩 / 紫水晶 / 球体四色 / 发光纹路。
- **发光**：另出一张 `${name}_glowmask.png`，渲染器加 `AutoGlowingGeoLayer`（已核实 GeckoLib 4.8.4 提供 `AutoGlowingTexture` + `AutoGlowingGeoLayer`），
  发光层用 `getRenderType(...)` 覆写指向 `RenderType.eyes(...)`。
- 遵守本工程既有铁律：方块模型**禁止**引用 `textures/entity/**`（本 Boss 是实体，不受此限，但雕像会，见 §6）。

### 4.3 动画清单

`idle`（悬浮+核心自转） / `float_move`（位移时前倾） / `cast`（核心前推，双臂张开） /
`evoke_lightning`（甩右臂+核心放电） / `evoke_frost`（收拢+凝霜） / `evoke_dark`（双臂下压+核心变暗） /
`echo`（全屏震动式抖动） / `hurt` / `death_overload`（3t 蓄力后炸开）。

### 4.4 生成与验证流程（照搬现有工具链）

1. `tools/model3d/gen_defect_core.cjs`（照 `gen_barrel_minion.cjs` 的写法）产出 `geo + animation + png(+glowmask)`；
2. `node validate.cjs` / `validate_anim.cjs`（结构错误必须为 0）；
3. `node render_tex.cjs` 出四视图 → 人工确认配色/球挂点；
4. `node audit.cjs` 十项全 0；
5. 生成图放 `桌面\FT-模型检查\故障核心_*.png`。

---

## 5. 粒子与音效

| 场合 | 粒子（用 `Fx.particles`） | 音效 |
|---|---|---|
| 闪电球在场 | `ELECTRIC_SPARK` + `END_ROD`（慢速上升） | `BLOCK_BEACON_AMBIENT`（低音量循环） |
| 冰霜球在场 | `SNOWFLAKE` + `CLOUD`(白雾) | `BLOCK_POWDER_SNOW_STEP`（轻响） |
| 黑暗球在场 | `SMOKE`(黑) + `PORTAL`(紫) | `ENTITY_ENDERMAN_AMBIENT`(极低音量) |
| 故障球在场 | `WARPED_SPORE` + `REVERSE_PORTAL` | `BLOCK_RESPAWN_ANCHOR_DEPLETE`(短) |
| 激发（任意） | 对应色粒子**爆发环** 24~40 个 | **机械充能音**：`BLOCK_BEACON_ACTIVATE` + `BLOCK_ANVIL_USE` 叠合 |
| 故障回声 | `SONIC_BOOM` + 紫色环 | `ENTITY_WARDEN_SONIC_BOOM`(降调) |
| 过载(死亡/技能) | `EXPLOSION_EMITTER` + `ELECTRIC_SPARK` 井喷 | `ENTITY_GENERIC_EXPLODE` + `BLOCK_BEACON_DEACTIVATE` |
| 封印命中玩家 | `SCULK_SOUL` 贴脸 + `DAMAGE_INDICATOR` | `BLOCK_SCULK_SHRIEKER_SHRIEK` |

> 所有爆炸一律 `ExplosionInteraction.NONE`（本工程既有铁律：不毁地形），只伤实体。

---

## 6. 掉落与进度

| 掉落 | 数量 | 说明 |
|---|---|---|
| **故障核心碎片** `ftboss:defect_fragment` | 8~14（受幸运影响） | 战利品表 `data/ftboss/loot_tables/entities/defect_core.json`；**必须延迟 20t 且在爆炸半径外生成**，避免被超载爆炸销毁 |
| 铜齿轮/紫水晶碎片（整合包已有物品） | 2~4 | 用 `ForgeRegistries.ITEMS` 运行期查找，找不到就跳过（不硬依赖） |
| 经验 | 120 | `xpReward` |

**进度线**（三条，玩家可自选，互不冲突）：
1. `故障核心碎片 ×8 + 铜块 ×4 + 紫水晶碎片 ×2` → **故障核心（召唤物）**，右键地面召唤 Boss（默认关闭重复召唤冷却；支持多只，见下）；
2. `故障核心碎片 ×4 + 任意饰品槽物品` → **充能球护符**（Curios 项链）：**给玩家** 3 个迷你球槽（闪电/冰霜/黑暗各 1，右键主动激发），把 Boss 的机制变成玩家自己的玩法；
3. `故障核心碎片 ×2` → 用于**雕像**（`ftboss` 石像体系，见 §7.4）与交易。

> 经济模组 `ftecon` **只读不改**：碎片定价由玩家自己在经济模组里配置，本模组不写任何价格文件。

---

## 7. 技术落地方案（按现有工程结构）

### 7.1 新增文件清单

```
entity/DefectCoreEntity.java            实体本体 + 球系统 + 阶段机
entity/OrbType.java                     枚举(名称/配色/粒子/音效/权重)
entity/DefectFragmentItem.java          碎片物品
entity/DefectCoreItem.java              召唤物(右键召唤)
boss/DefectCoreBoss.java                静态大脑(照 SugarQueenBoss 写法, 但**状态放实体**)
boss/ai/OrbController.java              球生成/激发/腐化/超时(纯逻辑, 便于单测)
boss/ai/GlitchSeal.java                 封印效果的全部事件钩子 + 可选快照模式
boss/ai/DefectSealData.java             (可选)快照存档 SavedData
client/model/DefectCoreGeoModel.java    GeoModel
client/render/DefectCoreRenderer.java   渲染器(+AutoGlowingGeoLayer)
data/.../loot_tables/entities/defect_core.json
assets/ftboss/geo|animations|textures/entity/defect_core.*
```

复用（不改）：`BossDamageGate` / `BossBars` / `BossUnstuck` / `TrapBreaker` / `BossMercy` / `MinionBlessing` / `Fx` / `WizardSummon`。

### 7.2 注册点（都有现成位置）

| 注册 | 位置 |
|---|---|
| 实体类型 + 属性 | `FTEntities`：`register("defect_core", ...)`、`onAttribute` 里加一行 |
| 渲染器 | `FTClientSetup.registerRenderers` |
| 物品 | `FTBossItems` |
| 效果 | `FTEffects`（若无则新建 `DeferredRegister<MobEffect>`） |
| 战利品表 / 配方 / 语言键 | 现有 `data/ftboss/...`、`assets/ftboss/lang/{zh_cn,en_us}.json` |
| 石像 | `FTBlocks.statue(id, tint)` + `gen_statue_real.cjs` 的 `BOSSES` 表加一行 |
| 连死怜悯 | `BossMercy.typeOf` 加一个 `instanceof DefectCoreEntity` 分支 |
| 免疫负面效果 | `BossHandler.onEffectApplicable` 的类型白名单 |
| 友伤免疫 | `BossHandler.isBossSide`（本 Boss 与其召唤物一并列入） |

### 7.3 关键实现注意（本工程血泪教训，逐条对齐）

1. **投射物/实体若用 GeckoLib 渲染，实体必须 `implements GeoAnimatable`**，否则编译期报"类型参数不在 T 的范围内"（已在炮弹上踩过）。
2. **跨死亡保存数据用 `SavedData`，不要用玩家 persistentData**（死亡重生会换 ServerPlayer 实例 → 数据清空；连死计数就是这个问题）。
3. **"不掉的离场"用 `discard()`**，不要 `kill()`，否则会跑战利品表掉经验。
4. **`LivingDeathEvent.getEntity()` / `Projectile.getOwner()` 本身就是目标类型** → 不要写 `instanceof` 模式（javac 报"模式类型是表达式类型的子类型"）。
5. **实体模型烘方块模型时**：cube 每个方向膨胀 0.02 单位 + `"ambientocclusion": false`（防 z-fighting 与脏阴影）。
6. **Bedrock 骨骼展开：子级继承完整矩阵**（`translate(piv)·rot·translate(-piv)`），否则越深的部件偏得越远。
7. **方块模型禁止引用 `textures/entity/**`**（会紫黑且无日志）→ 石像要复制贴图到 `textures/block/`。
8. **javac 中文路径/编码**：日志用 UTF-8 读、错误行过滤 `[removal]`（deprecation 噪音）。

### 7.4 石像接入（可选但推荐）

- `gen_statue_real.cjs` 的 `BOSSES` 表加 `{id:'defect_core', geo:..., tex:..., texW, texH}`；
- 本 Boss 的 geo 有大量非轴对齐旋转 → 先跑一次 `butcher_bones.cjs` 式核查，确认没有旋转骨骼或接受"轴对齐近似"；
- 石像落 `textures/block/statue_defect_core.png`（复制而非引用实体贴图）。

### 7.5 性能预算

| 项 | 预算 |
|---|---|
| 球数量 | ≤11（纯数据 + 11 个骨骼） |
| 每 tick 粒子 | 球在场上时每球 ≤2 个/4t（合并在一次 `Fx` 调用里），峰值 < 30/s |
| 玩家封印期间 | 每 tick 只遍历一次效果表（`getActiveEffects()` 快照），无物品栏 IO |
| 阶段技能 | 全部走 tick 计时器，不新增 permanent AI goal |

---

## 8. 玩法不冲突核对表

- [ ] 不与现有 5 Boss 共用任何静态状态（避免"多只同种"这类回归）。
- [ ] 不毁地形；不与 `farmingtales_tnt_control` / 领地类模组冲突（无方块破坏）。
- [ ] 封印只用事件取消 + 效果清除，**不动物品**（默认模式）→ 与背包类/饰品类模组无数据竞争。
- [ ] 对 `armamentarium` 的真实伤害/流血沿用既有免疫；不掉落该 mod 的物品。
- [ ] 对 Iron's Spells 只通过 `WizardSummon` 的反射入口施法（法术池按稀有度过滤，避开仅玩家可用的法术）。
- [ ] 连死怜悯 / 防卡瞬移 / 陷阱破除 三条通用规则同样适用于本 Boss。
- [ ] 语言键 zh/en 全配（`audit.cjs` 会查缺键）。
- [ ] 召唤物走的都是"友军"名单（友伤免疫生效）。

---

## 9. 里程碑（每步都能独立编译 + 部署 + 进游戏验证）

| # | 内容 | 验收 |
|---|---|---|
| **M1 ✅ 已完成(2026-09-11)** | 实体 `ftboss:defect_core`(404血/8甲/抗击退1/无重力悬浮) + 三阶段阈值(70%/35%, 血条改名+音效+粒子+日志) + 悬浮索敌 + 血条 + 限伤接入(`BossDamageGate.ANTI_CHEESE` 36格) + 负面免疫 + 死亡**核心超载爆炸**(威力8, 不毁地形) + 延迟20t在爆炸外掉落碎片 + 召唤物/碎片两个物品 + 模型(15 cube / 9 骨骼父子层级 / 4 段动画) — **可无限召唤多只**(无单例闸门) | ✅ build 成功、四处部署哈希一致、`audit` 十项全 0、模型校验 0 错 0 警 |
| **M2** | `OrbType` + `OrbController`：3 槽、随机生成、被动、受击/施法/超时激发（先用粒子表示球，不建模） | 日志/动作栏能看到球的变化；无卡顿 |
| **M3** | 故障回声（4 次阈值 → 腐化 + 2 槽）+ 故障球 + 封印效果（**封印模式**：只禁用使用与清增益，绝不动物品） | 封印期间不能吃喝/挥击/丢物；6s 后一切恢复；物品无任何变化 |
| **M4** | 模型/贴图/动画/glowmask 精修 + 11 个球槽骨骼 + 分层粒子 + 机械音效 | 四视图与实机截图；发光层正确 |
| **M5** | 阶段技能（P2 冰盾/召回/闪现，P3 黑暗吞噬/过载）+ 战斗 BGM 幕 | 完整三阶段可打完一轮，无异常日志 |
| **M6** | 进度线（召唤物配方 / 充能球护符 / 石像 / 交易）+ 文档与 `AGENTS.md` 更新 | `audit` 全 0、`lang` 无缺键、四处部署哈希一致 |

### M1 实施记录（含已踩到的坑）

- **扁平骨骼不动子树**：第一版 geo 是"平铺骨骼列表"，`validate_anim.cjs` 直接报 `骨骼 root 不在 geo 中`；
  即使把 root 补进去，扁平列表里动 root 也不会带动其他部件 → 已改成 **`parent: root` 的父子层级**，悬浮/抖动才整体生效。
- **`Monster` + 无重力**：`setNoGravity(true)` 后不能用寻路移动，改由 `tickHover()` 每 tick 小步 `teleportTo`（与女皇同款写法）。
- **掉落不能被自己的爆炸销毁**：死亡爆炸立刻发生，碎片用 `MinecraftServer.tell(new TickTask(+20t, ...))` **延后 20t 且在半径 2.5~4 格外**生成。
- **物品贴图/模型**：`parent: item/generated` 的 `textures.layer0` **必须是字符串**（写成对象会 `UnsupportedOperationException` → 模型丢失，这是本工程踩过的坑）。


---

## 10. 需要你拍板的开放问题

1. **强度定位**：404 血 + 8 甲，是"比现有 5 个 Boss 更硬的新天花板"，还是与现有 280 档持平（那就 404→320）？
2. **封印力度**：默认**封印模式**（不动物品，仅禁用使用与增益效果）够用吗？是否需要启用**快照模式**（真把饰品摘下来，风险更高）？
3. **多做几只**：要不要一开始就支持"同种多只召唤"（复用女皇/屠夫的复制体方案）？
4. **玩家向饰品**：充能球护符（玩家自己也有 3 个小球）要做进 M6，还是先只做 Boss？
5. **石像**：要不要同步出"故障核心石像"（会牵动 `gen_statue_real.cjs` 与贴图复制流程）？

---

## 11. 与现有 `ftboss` 的关系

- 本 Boss 作为**第 6 个 Boss** 加入，独立掉落与进度线，不改动现有 5 个 Boss 的数值与机制；
- 共用 `qys-bosses.jar` 一个产物、同一套构建/部署/校验流程（build → 四处复制 → 清 `lightspeed-cache` + `modernfix`）；
- 文档落点：本文件 + `PROJECT_STATUS.md` 里程碑勾选 + `AGENTS.md` 新增踩坑条目。
