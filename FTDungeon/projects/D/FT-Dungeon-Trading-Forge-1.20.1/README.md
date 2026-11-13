# FT 地牢交易 · 暮铁契约桌

独立的 Minecraft **1.20.1 / Forge 47.4.10 / Java 17** 模组。包含交易桌方块、创造栏、可编辑模型和交易接口。没有额外模组前置；不是 Fabric 模组。不修改现有 FT 地牢怪物、难度或挑战代码，也不沿用已放弃的火盆模型。

交付文件夹中：JAR 用于安装，`模型源文件/` 放 Blockbench 工程与贴图，`交易数据包示例.zip` 可放入世界的 datapacks，`验证记录/` 放证据；可构建源码另附 ZIP。下文开发目录路径均相对**解压后的源码根目录**。

## 玩家怎么购买

1. 桌面两个黄铜托盘分别预览「玩家付款」与「玩家获得」的物品，各最多显示三种。
2. 第一次右键只展示完整报价、双方数量和剩余交易次数，不扣款；打开聊天可悬停具体物品名称查看提示。
3. 核对后，至少间隔 **6 tick**、在 **100 tick** 内再次右键确认。正常 20 TPS 时约为 0.3—5 秒，服务器卡顿时以 tick 为准。
4. 付款不足、奖励放不下、售罄或扩展取消时，本模组不扣款、不发半份商品、不将商品扔到地上。成功后下一次购买仍要先预览。

请单次点击并核对报价。原版客户端可能在长按右键时重复发送交互；本模组阻止同 tick/过快确认和副手重复结算，但不声称能识别物理鼠标是否已松开。

## 管理员快速配置

在「FT 地牢交易」创造栏获取桌子，或执行：

```mcfunction
/give @s ftdungeontrading:trading_table
```

放置后执行以下示例，**将 `100 64 100` 换成桌子坐标**。命令需要权限等级 2，桌子必须已加载，位置属于执行者当前维度：

```mcfunction
/fttrade offers
/fttrade bind 100 64 100 ftdungeontrading:rations
/fttrade stock 100 64 100 32
/fttrade inspect 100 64 100
```

`bind` 将桌子绑定到指定交易，并把库存设为不限；`stock` 设置剩余可购买次数：`-1` 不限、`0` 停售、`1..1000000` 为有限次数。每次购买整份配方消耗一次库存，不按单件商品数量扣次数。

新放置的桌子默认**未绑定交易、库存为零**。绑定和库存随世界保存；拆掉只掉落普通桌子，再放不会保留或补满原交易。需要保护的地牢商店请配合领地/地图保护，避免被破坏。桌子没有真实卖家背包，不接漏斗：付款物品销毁，商品按配置生成到玩家主背包；有限库存是交易次数，不是容器物品。

内置三个可立即绑定的示例：

| 交易 ID | 玩家付款 | 玩家获得 |
| --- | --- | --- |
| `ftdungeontrading:rations` | 3 绿宝石 | 6 面包 + 2 金胡萝卜 |
| `ftdungeontrading:bone_exchange` | 16 骨头 | 1 绿宝石 |
| `ftdungeontrading:iron_sword` | 5 绿宝石 + 2 铁锭 | 1 把命名铁剑 |

## 自定义入口

- 不改 Java：参见 [数据包与交易格式](docs/DATAPACK.md)，修改付款 `costs`、商品 `rewards`，然后 `/reload`。
- 其他模组接入：参见 [Java API 与事件](docs/API.md)。支持注册交易、绑定桌子、查看库存、复用确认流程、购买前取消和购买后通知。
- 可安装示例数据包的源码在 `examples/datapack/`；交付包附带同内容 ZIP。
- 模型主文件：`model_sources/地牢交易桌.bbmodel`，已内嵌原创贴图；运行贴图在 `src/main/resources/assets/ftdungeontrading/textures/block/trading_table.png`。

模型为原创木铁交易桌：厚木桌面、黑铁包角、紫色桌旗、双托盘、账本与封蜡。桌下箱子、卷轴是装饰，不是可打开容器。57 个编辑组件、290 个声明面、单张 128×128 图集，无几何动画副本。商品由客户端渲染器动态显示，不在静态 Blockbench 工程里烘死。

## 安装与构建

备份世界后，把交付 JAR 放入客户端及服务器 `mods`，重启加载。不要同时安装同一 mod ID 的多个版本；仅服务端安装不足以显示自定义方块。**本轮只交付，不自动部署到现有整合包，也不启动游戏客户端。**

Windows 构建：安装 Java 17 和 Node.js，设置 `JAVA_HOME_17` 指向 Java 17；首次构建需要下载 Gradle/Forge 依赖。有本机缓存时可加 `-Offline`。

```powershell
.\BUILD.ps1 -Task build
.\BUILD.ps1 -Task runGameTestServer -Offline
```

`build` 执行资源检查与重映射，结果在 `build/libs/`。GameTest 在独立的 `run/trading-verification/` 测试世界中运行，不使用现有游戏存档。测试代码和测试标签不会进入正式 JAR。模型重建需要 Pillow；`tools/build-model.py` 会重写本项目生成资源和模型，**手工修改模型后请勿直接重建覆盖**。

## 验证边界

Forge 服务端 GameTest 通过 **130 项断言**；Minecraft 1.20.1 实际模型解析、资源引用、Blockbench 八视角与七种 Display、保存重开像素对照均已完成。详细范围和证据见 [验证记录](docs/VERIFICATION.md)。

未启动 Minecraft 客户端，因此不声称完成整合包内视觉、多人网络、第三方领地或性能实测。现有 FT 难度系统未被改动；如需按挑战等级锁定商品，可在 `BeforePurchase` 接入规则，不把“预留接口”写成“已联动”。
