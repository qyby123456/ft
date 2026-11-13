# FT 地牢随机战利品展示台 · 0.1.0 独立候选

Minecraft **1.20.1 / Forge 47.4.10 / Java 17**，模组 ID `ftdungeonloot`。不是旧双格交易桌的覆盖版，不修改 `ftdungeontrading`、FTBoss、原地牢或经济源码。**未部署至整合包或正式存档，未启动 Minecraft 客户端。**

交付包的 `模组/` 放 JAR，`模型源文件/` 放可编辑工程，`贴图/` 放图集，`预览/` 放成品与检查图，另附可构建源码和配置数据包 ZIP。下文 `src/`、`model_sources/`、`tools/` 与构建命令均相对**解压后的源码根目录**。

## 当前玩法

- 每个展示台每轮从明确配置的**普通房宝箱掉落表白名单**抽取一次，再从该次结果中随机选一叠可定价商品；与已绑定普通房宝箱使用同一组掉落表，不偷取世界内箱子中的实物。
- 排除金币自身、未配置价值的物品、超过最高报价的物品；没有可定价结果则本轮空台，不反复重抽直到出稀有品。原数量、附魔及其它物品 NBT 随商品保留。
- 暂定示例参数为 **价值的 50% 向上取整，最低 1 金币**；附魔等级增加价值，损耗按剩余耐久修正。这些是可调整的试运行数值，不是已确认的最终平衡。
- 一台每轮**全服共享一份**。第一次右键报价，至少隔 6 tick、100 tick 内再次右键确认；币不足、背包放不下、报价变化、已被别人买走均不扣款。商品进入主背包，不散落地面。
- 管理员明确绑定的普通房单箱/木桶，每轮首次有效开启时生成普通战利品，另给 **8～18 枚实体金币**。重开、清空、读档不重复生成；新轮次也不覆盖旧物品。
- 默认货币为整合包已有的 **`kubejs:coin`**，从主背包和副手扣除，不扣 SDM 账户余额，不接受改名或自定义 NBT 的金币。货币不存在即停用，**不会偷偷改用绿宝石或另造一套金币**。
- 默认轮次跟随 FT 地牢入口火炬的 `DungeonGateData.roundActive()/roundId()`。无活动轮次或接口不可用时不补货、不购买。展示台在加载/交互时更新商品，宝箱在交互时更新；没有持续旋转、逐台 ticker 或扫全世界容器。

## 安装与建图

先备份世界。客户端和服务器均需此 JAR，勿同时放同 mod ID 的多个版本。默认配置依赖整合包的 FT 地牢维度和 KubeJS 金币；两者作为可选依赖，不存在时模组仍可加载，但默认交易不会启用。不要将候选视为已在完整整合包验收。

在创造栏「FT 地牢战利品」获取展示台，或执行：

~~~mcfunction
/give @s ftdungeonloot:loot_stand
~~~

**管理员只在非 Boss 房选点绑定**；将示例坐标替换为实际坐标，在目标维度执行。命令需要 OP2，区块必须已加载：

~~~mcfunction
/ftloot profiles
/execute in ftdungeonmobs:dungeon run ftloot stand 100 64 100 ftdungeonloot:ordinary
/execute in ftdungeonmobs:dungeon run ftloot chest 102 64 100 ftdungeonloot:ordinary
/execute in ftdungeonmobs:dungeon run ftloot inspect 100 64 100
~~~

`stand` 绑定展示台；重新绑定是管理员主动重置，不向普通玩家开放。`chest` 仅接受**空、未绑定且没有其它 LootTable 的独立单箱/木桶**，不清箱、不改大箱子、不覆盖原有掉落表。不自动搜索或绑定任何 Boss 房、普通房、已有结构或实体掉落；现在的 FT 房间模板本身没有战利品，本候选提供的是第一份共享普通房示例池，须建图者明确接入。

默认 `round_source: "ftdungeon"` 自动读取 FT 轮次，不改其状态。独立测试或其它地图可改为 `manual`，此时才使用 `/ftloot nextrun` 更新当前维度的手动轮次。该命令不会重开 FT 地牢。重进、重连、开关箱子及资源重载不会自行重开轮次。

## 调整掉落与价格

用数据包覆盖：

- `data/ftdungeonloot/loot_stands/ordinary.json`：货币、维度、轮次来源、普通房表白名单、价值、价格比例及金币上下限。
- `data/ftdungeonloot/loot_tables/chests/ordinary.json`：真正的共享普通房战利品表，包含数量、权重、附魔等原版掉落函数。

包根目录放 `pack.mcmeta`，`pack_format: 15`，然后 `/reload`。可参考 [数据与边界](docs/DATAPACK.md)。重载会让待确认报价失效；已保存的本轮商品/价格保留到下一轮，避免改配置时悄悄重抽。货币变更、配置删除或配置无效时停止该台购买。

示例中面包 4、钻石 25、红石 1、煤炭 1 是 2026-11-03 对本机 `a_price_list.js` 的只读基础值快照；其它列出的价值是**候选平衡占位值**。没有实时调用经济模组的动态行情/品质价，也没有修改出货箱价格表。半价买入后可能在其它系统转售获利；本系统只用每轮有限库存限制产出，不声称拦截所有跨模组套利。

## 模型与验证

`model_sources/地牢随机战利品展示台_动态版.bbmodel` 可直接在 Blockbench 打开，内嵌贴图。通过 MCP 从原 113 组件版本另存动态底座：移除固定绿宝石、金币/铁碟与卷轴，保留石台、薄红毯、铁架、符文牌、纸价签及绳链。原模型交付未改。

静态底座为 **71 个实体长方体 / 394 声明面 / 一张不透明 256×256 图集**，另有运行时单个商品与价格文字。动态物品模型本身的面数、游戏 draw calls/FPS 未测。碰撞为石台本体的一整格，铁架、绳链仅装饰。

构建和离线校验：

~~~powershell
.\BUILD.ps1 -Task build -Offline
.\BUILD.ps1 -Task runGameTestServer -Offline
~~~

构建需 Java17 和 Node.js 在 PATH；首次下载依赖可去掉 `-Offline`。`build` 包含实际 Minecraft 1.20.1 模型解析器检查与资源引用校验。GameTest 只用本项目 `run/loot-verification/` 隔离世界，以金粒测试夹具代替缺席的 KubeJS，不会将该夹具打入正式 JAR。详见 [验证记录](verification/VERIFICATION.md)。完整整合包、FT 自动轮次桥接、KubeJS 实体金币及客户端动态陈列仍须联合实测。
