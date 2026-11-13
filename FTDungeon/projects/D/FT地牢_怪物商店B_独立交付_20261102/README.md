# FT 地牢变体 B「怪物商店」独立候选交付

状态：结构、静态检查和 Forge 四向原生放置通过；100 tick 稳定。未登记正式槽位、未接入核心、未部署、未打开正式存档。

## 四件套与证据

- `out/room.nbt`：gzip、单调色板、Java 1.20.1 / DataVersion 3465；968 条完整坐标记录。
- `out/room.json`：暂定 ID `SHOP_B_MONSTER`，11×8×11；北入口脚点 `(5,1,0)`，内部脚点 `(5,1,3)`，`facing=north`。
- `out/preview.png`：从最终 NBT 渲染的陈设检视剖面，不是 AI 图，也不是游戏截图。
- `out/qa.json`：汇总静态、原生、视觉、确定性重建与指纹证据。
- `out/blueprint.json`、`out/materials.json`：所有陈设、通路、支撑坐标及原版方块数量。
- `preview/four-views.png`：保留柱梁的四向剖视；`preview/exterior.png` 展示完整屋顶和 3×3 北门。
- `preview/plan.png`、`preview/counter-detail.png`：净空平面和陈设检视。
- `qa/native-placed-*.nbt`：四种旋转经过 100 tick 后的真实世界回采；`preview/native-world-capture.png` 是回采 NBT 的软件预览。
- `release/FT_Dungeon_Monster_Shop_B_Standalone_20261102.zip`：候选交付包；包 SHA-256 见 `qa/package-verification.json`。

## 设计落地

主图 `D:/ft工程文件/image2.5/dungeon-shop-variants-20261102/out/shop-variant-B-monster-shop.png`，1536×1024，SHA-256：

`c21adfa37ec561da27affb7def791fc78f5e727b74cb2dbb7b92666210522212`

最终 NBT SHA-256：`68b5c406511795ec145a4d80be895c66e38199e5dfc24a9b21540b20e9c410fa`。

- 四根柱只在外墙四角，家具不穿柱、没有中央落柱。四周封墙、Y=7 屋顶完整。
- 北门 X=4…6、Y=1…3、Z=0；三格宽高主路一直到 Z=5，全空气、有完整实心地板。
- 独立柜台 X=3…7、Y=1、Z=6；前后 Z=5/7、两侧 X=2/8 至少一格净空，可双向绕行。
- 紫袍怪物店主是 Z=8 的**连地静态方块雕像**，有骨角、双发光眼与绿爪，和柜台间隔一格。没有实体、AI、交易、刷怪或奖励系统。
- 西北角 X=1…3、Z=1…3 为眼球标本大罐：完整木底座、绿玻璃罐体、菌光内核、黑瞳与木盖。是原版方块转译，不是精细自定义模型。
- 东北角 X=7…9、Z=1…3 为落地铁笼：深板岩底座、两层铁栏杆、连续半砖笼顶，内部只有静态头骨，没有小怪或刷怪笼。两侧大型陈设均不侵占三格主路。
- 一格深的货架贴东西墙，端部立木；盆栽、头骨、蜡烛均有层板。菌光体下托蘑菇盆栽，表现概念图发光蘑菇。
- 普通灯与灵魂灯脚点 `(4,4,3)` / `(6,4,3)`，链在 Y=5、梁在 Y=6。原版没有绿色灯笼，用青色灵魂灯转译。
- 地毯改为内嵌红/黄陶瓦，避免主路增加碰撞高度。所有容器为空，没有 LootTable、物品实体、液体、裸火或危险地面。

参考目录里的 README、manifest 和 DISPATCH 原样归档；README 提到的 `STYLES.md` 实际缺失，已明确记录，未编造。没有调用生图接口，没有更换主图、改密钥或生成付费测试图片。

## 验收范围

1. `qa/static.json`：24 项检查，涵盖版本与哈希、968 坐标、入口、完整屋顶、柱家具分离、54 格主路、柜台环路、雕像连地、眼球罐和铁笼支撑、空库存和候选状态。
2. `qa/native.json`、`qa/forge-gametest.log`：Java 17.0.8、Minecraft 1.20.1、Forge 47.4.10；使用真实注册表、`StructureTemplate.load/placeInWorld/fillFromWorld`，并进行 NBT 原生读写回环。
3. 0°/90°/180°/270° 四向，邻居更新及 100 tick 后共核验 3,872 条状态；每向 8 个方块实体、60 条陈设支撑链、39 个可达脚位；额外世界实体为零，所有状态、存活、碰撞和空库存检查通过。
4. 负例：四个断链、八个眼球罐/铁笼断支撑、四个主路堵塞，共 16 个；确保检查确实能发现故障。负例只发生在隔离测试世界且已复原。
5. `qa/reproducibility.json`：重新构建后结构、元数据、蓝图和材料表逐字节一致，gzip mtime=0。`qa/visual-review.json` 记录实际查看的图片和限制。

软件剖面不模拟游戏照明、着色器和完整实体渲染；头骨采用近似实体外观，告示牌文字不渲染。四向图保留全部梁；交付预览/柜台检视仅在渲染内存中额外隐藏 Z=3、Y=6 的横梁，标题明确标注；最终 NBT 及原生回采不删梁。

没有测试 FT 大厅连接、槽位、占用拒绝、幂等、五档难度、完整整合包客户端、多人、FPS 或交易行为。原生 GameTest 通过不冒充已接入/已部署或完整实机游玩验收。

## 边界与复现

所有输出和测试世界只在本独立目录。没有改其他房间、原始设计目录、核心源码、部署 JAR 或正式存档；读取的核心目录及原商店指纹观察记于 `qa/boundary-observations.json`，不是跨线程锁或对其他线程行为的保证。

正式 ID/槽位保持未分配；未经统一接入聊天 `01a19758-b0e5-75c2-822a-4860527a1e8b` 协调不得部署。

复现命令（Python 需 Pillow；本机使用 `C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe`）：

```powershell
python tools/build_room.py
python tools/qa_static.py
python tools/run_native.py
python tools/render_room.py
python tools/finalize_delivery.py
```

Forge 测试使用 Java 17、已有 Gradle 8.8 和离线 Forge 缓存，运行目录固定 `qa/forge-run`，不启动玩家客户端；不应把 QA Mod 安装进整合包。渲染使用本机 1.20.1 `client.jar` 原版材质与模型，包内不分发客户端资源。复现后的视觉报告必须由真实查图更新，不自动伪造人工验收。

代码来源：复用独立补给商店工程的 NBT 编解码、软件渲染与隔离 Forge 夹具到本目录，按 B 改写结构生成、静态检查、原生支撑负例与文案；不修改来源工程。ZIP 排除 Gradle 缓存、测试世界、JAR 和任何生产存档。
