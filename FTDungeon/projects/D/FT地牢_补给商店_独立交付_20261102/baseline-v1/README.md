# FT 地牢补给商店 · 独立候选交付

**状态：结构制作、静态 QA、四向 Forge 原生放置及 100 tick 稳定性通过；未接入核心、未部署、未打开或修改正式存档。**

本工程只写自身目录，不改九间房 `incoming`、四视图修正工程、第三批 M5/M6/M7/S5/S6/S7 队列或 FT 核心源码。没有调用付费生图接口，没有改图像连接配置，也没有启动 Minecraft 客户端。

## 接收文件

| 文件 | 用途 |
| --- | --- |
| `out/room.nbt` | gzip、单调色板、Minecraft Java 1.20.1 DataVersion 3465 的最终结构 |
| `out/room.json` | 北向接口、尺寸、安全点、完整结构指纹与暂定身份 |
| `out/qa.json` | 最终质量报告，关联静态、原生、可重建与视觉证据 |
| `out/preview.png` | 从最终结构渲染的剖视图，不是 AI 图或游戏截图 |
| `out/blueprint.json` | 家具、柱、灯链、净空、通行检查的精确坐标 |
| `out/materials.json` | 非空气方块数量表 |
| `preview/four-views.png` | 保留柱梁和家具的四向剖视 |
| `preview/exterior.png` | 最终屋顶和北侧 3×3 接口 |
| `preview/plan.png` | 标注三格通道与柜台净空的坐标平面 |
| `preview/native-world-capture.png` | 原生放置稳定 100 tick 后回采 NBT 的软件预览 |
| `qa/native-placed-*.nbt` | 四种旋转的原生世界回采结构 |

交付 ZIP 在 `release/`，含生成脚本、NBT 工具、隔离 Forge 测试源码、原图副本、证据和逐文件指纹；不含 Minecraft/Forge 缓存、二进制测试 Mod、客户端 JAR、测试世界或正式世界。

## 房间接口与布局

- 暂定 ID：`SHOP_SUPPLY_DSH`。这是可被 FT 合法 ID 格式接受的候选名，**不是协调人已分配的正式 ID**；`assigned_slot=null`，没有抢占新槽位。
- 尺寸：**11×8×11（XYZ）**；内部平面 9×9，地板 Y=0、屋顶 Y=7。相对北入口脚部 **(5,1,0)**，安全内点 **(5,1,3)**。
- 北开口 X=4…6、Y=1…3、Z=0；门口至柜台前通道 X=4…6、Y=1…3、Z=0…5 全为空气，有完整实心地板。
- 柜台 X=3…7、Y=1、Z=6，为 5×1 独立柜；X=2/8 两侧及 Z=5/7 前后净空至少一格，可从两端绕到店主侧和空箱前。
- 四柱只在墙线四角 (0,0)、(10,0)、(0,10)、(10,10)，Y=1…6；与柜、架、桶无重叠或面接触。
- 西/东货架只占 X=1/9，南墙货架只占 Z=9；一格进深、紧贴完整墙体，端部立木、墙挂层板和上表面支撑均保留。
- 四只空桶只在室内四角 (1,1)、(9,1)、(1,9)、(9,9)。一只空箱和一座空酿造台不带物品或 LootTable。
- 灯笼脚点 (4,4,3)/(6,4,3)，链 Y=5，承重梁 Y=6；投影在进门通道，不在柜台或货架上方。柜台蜡烛不产生蔓延火焰；模板没有火方块、岩浆、水或其它危险物。
- 概念图约 9×7 的内部纵深扩为 9 格，保证通道和后侧净空；地毯改为地板内嵌陶瓦。小瓶、卷轴、天平、武器等非原版精细道具用空花盆、书架、空酿造台、蜡烛及工作台转译。**不生成店主/顾客，不实现交易、奖励、波次或刷怪逻辑。**

## 图片与结构指纹

| 输入 / 输出 | 尺寸 | SHA-256 |
| --- | --- | --- |
| 主图 hero | 1536×1024 | `f26921137e349646d08690a5964a2235636945a41d39179d48d37d5cac68a355` |
| 备选 merchant | 1536×1024 | `3be525340bb4602e9dbec01e8179fb45dd4acdbb5ecd2c081bdb69d60c0cc23e` |
| 布局核验图 | **1536×1324** | `96449cd195ac4af7a88c9d4cef5b50dab898c232f8d01b81748e5a09270918b0` |
| 最终 room.nbt | 11×8×11 | `09d53aaedb3ad8996ff768b4679da549d2e6610fdf7d35ec5ee0cc652b4885a7` |

备选图的交接文字少了一个十六进制字符，原文为 63 位，并非有效完整 SHA-256；这里采用实际源文件与副本重算且与源 manifest 一致的 64 位指纹。布局核验图则以实际 PNG 为准，**不采用源 manifest 中已过期的 2156… 指纹或旧尺寸**。原 manifest 只作为历史材料原样保留在 `reference/SOURCE_MANIFEST_UNCORRECTED.json`，权威重算记录是 `reference/input-fingerprints.json`。

## 已执行验收

1. `qa/static.json`：23 项通过。检查完整 968 条坐标记录、单调色板、北向接口、实体/危险方块/库存、完整屋顶、柱家具分离、54 格主通道净空、柜台环路、灯链和陈设支撑等。
2. `qa/native.json`、`qa/forge-gametest.log`：**Java 17.0.8 / Minecraft 1.20.1 / Forge 47.4.10 的隔离 GameTestServer**；真实 `StructureTemplate.load/placeInWorld/fillFromWorld`，不是只做 NBT 解码或 DataGen。
3. 0°、90°、180°、270° 四向放置；每向在邻居更新及 **100 tick** 后复核 968 条实际方块状态、10 个方块实体、碰撞、存活条件和空库存；共 **3,872** 条稳定后状态核验。每向 60 个可达脚位，柜台两端、前后及空箱前均连通，额外实体为零。
4. 四个断链负例及四个主路碰撞负例均被识别。断链探针使用 `UPDATE_KNOWN_SHAPE` 暂停自动邻居拆除，再调用原生 `canSurvive` 检查，复原后继续稳定性测试。早期错误探针因原生引擎已将失去链条的灯拆为空气而误判，保留失败日志，不把失败说成通过。
5. 四向世界回采 NBT 已原生保存、重新加载，并保留完整指纹；`NONE` 回采的全部方块状态进一步与最终源模板逐格相等。`qa/reproducibility.json` 证明重复构建输出一致；6 个 Python 脚本语法编译通过，阻塞通道、缺地板、缺屋顶、缺灯链四种独立副本的静态负例均被对应检查项拒绝，见 `qa/static-negative-tests.json`。
6. 日志末尾同时包含 `SHOP_NATIVE_GAME_TEST_PASS: rotations=4/4, records=3872, ticks=100, stock=0`、`All 1 required tests passed` 和 `BUILD SUCCESSFUL`。视觉复核记录在 `qa/visual-review.json`，软件预览的实体外观、光照和字体近似不冒充客户端证据。

首次 Gradle 调用的 PowerShell `-D` 参数解析问题，以及第二次负例夹具误判均保留在 `qa/attempt-*`。最终通过针对同一个上述结构指纹；没有为迁就测试而删灯或降低净空要求。

## 重建与复测

本机 Python：`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`，需要 Pillow。NBT 工具是只读复制的 `tools/nbtio.py`，来自实例已有 `output/dungeon-expansion-20261101/tools/nbtio.py`。渲染使用已存在的 vanilla 1.20.1 `client.jar`，只读，不随包再分发。

在本目录执行：

```powershell
$python = 'C:\Users\YOUR_USERNAME\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
& $python tools/build_room.py
& $python tools/qa_static.py
& $python tools/run_native.py
& $python tools/render_room.py
& $python tools/finalize_delivery.py
```

`run_native.py` 使用本机 Java 17 和缓存 Gradle 8.8，只启动本工程 `qa/forge-run` 下的无界面测试服，并保留上一份测试结果；不会启动玩家客户端。其它电脑可显式传 `--java-home` / `--gradle`，首次缺失的 ForgeGradle 依赖须另行配置；默认离线，不自动替换为其它 Minecraft/Forge 版本。搬迁后应更新渲染器的本地客户端资源路径。本包不是脱离 Java/Gradle/Minecraft 缓存即可执行的二进制服务端。

## 后续接入及回滚边界

观察时正式目录有 L1/L2/M1/M2/S1/S2/L3/L4/S3，占用槽 0…8；第三批 M5/M6/M7/S5/S6/S7 是其它线程队列，不分配其槽位。`qa/boundary-snapshot.json` 是只读观察，**不是锁，也不据此保证并行线程期间文件不变**。

本次没有发送核心修改、没有写入共享 `incoming`，没有生成/安装运行 JAR。接入前须先由「检查并接入 FT 地牢设计」（`01a19758-b0e5-75c2-822a-4860527a1e8b`）协调当前核心负责人确认唯一 ID、可用槽和接收时机，再将本包验收后的 NBT/元数据作为新条目接收。保持原入口、返程、难度和已生成标记；完整体积及缓冲区占用预检、大厅实际连路、逐房持久化、拒绝覆盖/实体、幂等及正式 JAR 回归由接入方完成，不能把本测试夹具的清空代码用于正式施工。

**当前回滚只需撤回这个未部署的独立候选，不涉及玩家世界。** 若未来正式接入，必须先停服并备份旧 JAR、配置、玩家世界及地牢 SavedData，按协调人的完整回滚方案处理；仅删除模板或卸载 JAR 不会拆除已建建筑，不得盲删已生成标记。

剩余限制：没有完整 FarmingTales 整合包客户端、多人、FPS、实际玩家视角或功能性交易验收；没有声称 FT 大厅道路、核心幂等、防覆盖和返程问题在本轮已验证或修复。
