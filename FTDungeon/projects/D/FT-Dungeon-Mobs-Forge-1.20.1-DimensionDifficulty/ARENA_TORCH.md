# 旧竞技场长矛火炬：已于 0.12.0 退役

旧 DSH 模型、动画、贴图、渲染代码、配方和创造栏入口均已移除；旧 ID 仅作存档兼容，使用血棘祭炉外观。新物品、五档变色及旧摆放处理请看 `BLOODTHORN_BRAZIER.md`。以下仅保留历史设计记录，不代表当前运行资源。

## 历史：0.10.0（v8 · GeckoLib 4.8.4 骨骼动画版）

## 使用

- 方块与物品 ID：`ftdungeonmobs:arena_torch`。获取命令：`/give @s ftdungeonmobs:arena_torch`。
- 已加入“FT地牢怪物”和原版“功能方块”创造栏。v8 为 **GeckoLib 4.8.4 骨骼动画火盆**（2 格 BaseEntityBlock + BE；视觉/占用均两格）；不会点燃周围方块，也不会接触烧伤玩家。
- 放在具有完整上表面的支撑方块上，需要上方两格空位；不支持水下放置。新放置从一级开始。
- 右键任意半部：**1 → 2 → 3 → 4 → 5 → 1**。生存、创造和冒险玩家均能使用，不要求 OP；旁观者不能修改。仍受服务端出生点保护及 Forge 交互事件约束。
- 上下半部与五色火焰使用原生方块状态同步；v8 渲染为 **GeckoLib 骨骼动画 BE**（lower 半部持有 BE，upper 为占位块）。操作栏显示等级、颜色名称、服务器作用半径、更新的自动怪物数，并播放提示音；副手不会导致同次操作切两级。
- 任一半部被破坏或下方支撑被移走，两半都会消失，生存条件下合计仅掉落一个火炬。回收物重新放置从一级开始，不携带上一竞技场等级；旧存档的 blockstate（HALF+Tier）继续兼容。

| 等级 | 火焰颜色 | 名称 |
| --- | --- | --- |
| 1 | 白蓝 | 初试 |
| 2 | 翠绿 | 挑战 |
| 3 | 鎏金 | 精英 |
| 4 | 紫色 | 噩梦 |
| 5 | 猩红 | 炼狱 |

火焰为 **body 图集中的 flame tile（5 档换色）+ GeckoLib `idle` 骨骼动画（loop，2.0s）**；5 档纹理 `arena_torch_body_t1..t5.png`（64×64，`arena_torch_body.png` 为 t3 兼容副本）。火焰骨骼 `flame_inner/flame_outer` 的 UV 全部映射到 flame tile (48,48)；Forge 全亮 + 发光层（`ArenaTorchEmissiveLayer`）呈现亮度。原版方块光不是彩色动态光源，不宣称周围环境会被染成对应颜色。

## 影响范围与怪物属性

默认以火炬下半部中心上方半格为球心，**半径 32 格**。只影响本模组四类骷髅的自动档位，不影响玩家、原版或其他模组生物，也不修改世界难度。

来源优先级：**明确指定的单体固定档位 > 最近的范围内火炬 > 世界难度映射 / 默认档位**。范围重叠选最近的火炬；同距离按方块位置稳定排序，不随机抖动。离开范围或拆掉最后一支覆盖火炬后，自动怪物回到世界映射 / 默认档位。

右键立即重算附近自动怪物；后续刷怪在入场时选档；移动进出范围最多约 20 游戏刻更新。怪物自己的加成与各类型加成保留，生命、攻击、物理护甲、铁魔法魔抗、法强继续复用 `DIFFICULTY.md` 中的配置与原生结算，不增加第二层伤害乘算。改变最大生命时保留剩余血量比例，不回血、不复活。

若用命令固定过怪物，希望改回跟随火炬：

```mcfunction
/ftdungeon difficulty auto @e[type=ftdungeonmobs:skeleton_warrior,distance=..32]
```

服务器配置 `config/ftdungeonmobs-common.toml`：

```toml
[arenaTorch]
radius = 32
```

可设 8～128，不重复添加同名表。保存后执行 `/ftdungeon difficulty reload`，现存自动怪物也会重新选档。球形范围包含竖直距离；如果多层竞技场离得很近，需调整火炬位置或半径。

档位存于原生方块状态；每个维度独立保存火炬位置索引 `data/ftdungeonmobs_arenas.dat`。索引按区块分桶，仅查询附近桶，不遍历半径内每个方块。未加载火炬所在区块时继续使用已保存的档位，不强制加载该区块；重新加载后核对实际方块状态并剔除失效条目。

## 合成

工作台按以下排列合成 1 支：

```text
铁锭  铁锭    铁锭
木棍  烈焰粉  木棍
圆石  圆石    圆石
```

## Java 接口

入口 `net.farmingtales.ftdungeonmobs.ArenaDifficultyApi`，仅在逻辑服务端主线程调用：

```java
int updated = ArenaDifficultyApi.setTier(serverLevel, torchPosition, 5);
OptionalInt localTier = ArenaDifficultyApi.findTier(serverLevel, monster.position());
DungeonDifficultyApi.useAutomaticTier(monster);
```

`setTier` 支持上、下半部位置，验证档位 1～5 和双格结构，同步两半并更新范围内自动怪物，返回更新数量；它不会修改固定档位实体。`findTier` 返回指定位置的局部档位，没有覆盖时为空；世界难度回退由 `DungeonDifficultyApi` 完成。异步地牢生成器应使用 `server.execute(...)`，不能异步写世界。

## 模型与维护（v8 · GeckoLib 骨骼动画）

- **0.10.0 = 0.9.0 代码/逻辑基座 + v8 GeckoLib 渲染（Java 7 文件）**：方块/物品 ID、HALF+Tier blockstate、右键循环、难度接口、配方/loot/语言键均不变。
- 运行资源（`src/main/resources/assets/ftdungeonmobs/`，arena 资源 **11 项**）：
  - `blockstates/arena_torch.json`：**10 variants**（half=lower/upper × tier=1..5）→ `ftdungeonmobs:block/arena_torch`（空占位，渲染由 BE 提供）；
  - `models/block/arena_torch.json`：空占位模型（`elements=[]`，particle/body=block/arena_torch_body）；
  - `models/item/arena_torch.json`：**静态 item 模型**（59 元素、display 7、ymax 32，无 `texture_size`/`credit` 私有字段）；
  - `geo/arena_brazier.geo.json`：Bedrock **1.12.0**，**16 bones / 59 cubes / 354 faces**，整数网格 0 违规；`flame_inner/flame_outer` 火焰面 UV=(48,48)；
  - `animations/arena_brazier.animation.json`：`animation.arena_brazier.idle`（**loop=true，2.0s**，10 个参与骨骼）；
  - `textures/block/arena_torch_body.png` + `arena_torch_body_t1..t5.png`：均 **64×64**（火焰 tile 5 档换色；body=t3 兼容副本）。
- **旧 v7 静态资源清理**：`arena_torch_lower/upper/upper_1..5`（7 模型）+ `arena_flame_1..5.png` + 5 mcmeta 共 **17 项**已删除。
- **Java（7 文件）**：改 `ArenaTorchBlock.java`（→BaseEntityBlock + HALF/TIER + ENTITYBLOCK_ANIMATED + lower-only BE + 粒子高度 +20..31px）、`FTDungeonMobs.java`（BLOCK_ENTITY_TYPES/ARENA_TORCH_ENTITY）、`client/DungeonClient.java`（+registerBlockEntityRenderer + FMLClientSetupEvent cutout）；新 `ArenaTorchBlockEntity.java`（GeoBlockEntity）、`client/ArenaTorchModel.java`、`client/ArenaTorchRenderer.java`（GeoRenderer）、`client/ArenaTorchEmissiveLayer.java`；其余 12 Java 未动。
- **验证器**：工程内 `tools/VerifyArenaAssets.java` 为 **v8 GeckoLib 验证器**（真实 GeckoLib 4.8.4 `JsonUtil.GEO_GSON`/`BakedModelFactory` 解析烘焙 geo + `BakedAnimations` 解析动画；整数网格、火焰 UV、16/59/354、body_t1..t5、blockstates/占位/item、旧静态清理、Java BE/Renderer/注册），`verifyAssets` 继续校验四套骷髅 GeckoLib 资源；`build.gradle` 的 `check` 同时依赖两者，且 `verifyArenaAssets` 使用 `compileAssetVerifier + extractVerifierLibraries + asset-verifier-libs`（GeckoLib jarjar 库）。
- **参考来源与原创声明**（沿用 v7 造型来源）：造型参考『灾变（Cataclysm）』Altar of Fire 与用户提供的 gpt 解包模型风格（仅风格/构图参考）；**几何与贴图均为本项目原创**，未复制第三方模型/贴图文件；参考作品许可未核实（内部/个人使用，公开分发前需确认）。
- 验证包括真实 GeckoLib 解析/烘焙（`ARENA_ASSETS_V8_VERIFIED`）、骷髅线 GeckoLib 回归（4 模型/218 骨骼/17 动画）与完整 Gradle `build`（含 reobf）通过；**没有启动完整整合包客户端**，不把静态预览/解析器验证当作游戏内画面或 FPS 测量。
- **已知 P2（待实机验收）**：idle 动画/渲染观感、火焰 tile 5 档换色、EmissiveLayer 发光层、BE 同步/性能、item 静态模型 GUI/手持差异、旧存档首次加载兼容。
