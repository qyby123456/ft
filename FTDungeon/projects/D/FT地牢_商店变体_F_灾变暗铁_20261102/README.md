# FT 地牢商店变体 F · 灾变暗铁商店「赤契铁窖」

**独立候选结构。静态 QA、Forge 四向原生放置及 200 tick 稳定性通过；未接入核心、未部署、未分配正式槽位。**

本目录只负责 F，不修改其他房间、正式玩家存档或已部署 JAR。未调用收费生图接口，未启动玩家客户端。Cataclysm 仅作暗铁/金饰/红符文的视觉参考，所有坐标布局和几何原创；结构全部使用 Minecraft Java 1.20.1 原版方块，不需要安装灾变模组。

## 四件套与接收入口

| 文件 | 含义 |
| --- | --- |
| `out/room.nbt` | gzip、单一调色板、DataVersion 3465，完整显式 3,179 条 XYZ 记录 |
| `out/room.json` | 准确尺寸、北向接口、安全点、暂定身份、输入与结构 SHA-256 |
| `out/preview.png` | 从实际交付 NBT 和 1.20.1 原生模型/贴图生成的剖视预览，不是游戏截图或 AI 概念图 |
| `out/qa.json` | 静态/原生/回采/可重建/视觉证据汇总与明确限制 |

`handoff.json` 给出四件套绝对路径与逐件 SHA-256；`delivery-manifest.json` 给出完整发布包逐文件指纹。ZIP 与 ZIP SHA 位于 `release/`。ZIP 不含游戏客户端、Forge/Gradle 缓存、测试世界、测试 Mod 二进制或正式存档。

## 接口与空间

- 暂定 ID：`SHOP_F_DARK_IRON`，**不是协调人已授予的槽位**。`assigned_slot=null`；接收前必须统一协调。
- 尺寸 **17×11×17（XYZ）**。地板 Y=0，完整屋顶 Y=10；南墙加厚至 Z=15～16，容纳封闭炉芯。
- 北入口玩家脚点 **(8,1,0)**，内部安全脚点 **(8,1,5)**。北开口 X=7～9、Y=1～3、Z=0；门下完整实心地板。
- 主路 X=7～9、Y=1～3、Z=0～9 全为空气；地面红陶瓦符文无高差，不使用会抬高碰撞面的地毯。
- 独立柜台 X=4～12、Y=1、Z=10，九格宽。Z=9/11 前后及 X=3/13 两端留一格以上净空；可绕到柜后与空箱前。
- 店主预留脚点 (8,1,12)，**不生成 NPC、怪物、奖励、交易逻辑**。
- 八段承重墙柱只在 X=0/16 或墙角线上。两根梁 Y=9、Z=6/12。柱与柜、架、箱和展台没有重叠或面接触；中央无承重落地柱。
- 双侧后货架在 Z=14，完整实墙背板 Z=15；四个端部立脚、上半台阶层板、落地石底座支撑陈设。双侧封闭玻璃展柜位于 X=1～3/13～15、Z=2～4，有完整底座、玻璃侧壁和顶盖。
- 悬灯脚点 (7,5,6)/(9,5,6)，链 Y=6～8 接梁 Y=9 与屋顶 Y=10；投影只落在通道，不悬在柜台或货架上。

## 危险物隔离

两源熔岩位于 **(3,2,15)/(13,2,15)**，每源六个正交相邻面全部由黑曜石/玻璃封闭；面向室内为完整玻璃方块，不用铁栏杆、活板门或含空隙围挡。它们在加厚后墙内，不在玩家通行空间，无敞口、无明火、无热地板。附近半径四格逐方块逐面检查 Forge 燃烧/火势蔓延属性为零。黑旗位于与炉芯隔开的高处/中央，未进入炉芯易燃风险范围。

空箱两只、空酿造台一座。骷髅头仅为无主人数据的装饰方块实体；旗帜为原版菱形/中竖纹/金边图案 NBT。没有库存、LootTable、预放实体、刷怪笼、命令块、拼图块、结构块、TNT 或复活锚。

## 验收证据

1. `qa/static.json`：29 项通过，包括完整坐标、外壳/屋顶、接口、主路、柜台环路、柱家具分离、架脚/背板、灯链、旗帜/陈设支撑、空库存、封闭熔岩与三种负例。
2. `qa/native.json`、`qa/forge-gametest.log`：**Java 17.0.8 / Minecraft 1.20.1 / Forge 47.4.10 隔离 GameTestServer**；真实注册表属性解码、方块实体加载、`StructureTemplate.load/placeInWorld/fillFromWorld`，非仅解压 NBT 或 DataGen。
3. 四种旋转分别验证 3,179 条完整方块状态、12 个方块实体、存活条件、真实碰撞、北接口及绕柜/箱前连通。**200 tick 后共复核 12,716 条状态**，每向 172 个可达脚位，额外实体零。
4. 每向强制加载相关测试区块、显式调度两源熔岩 tick，`doFireTick=true`。房外另建独立有界流体对照盆，四个对照均确认真实熔岩扩散，防止“未实际 tick”假通过；对照盆不在交付模板中。
5. 每向 1,514 个模板外流体/火边界探针通过；每向两炉共 1,458 个半径四格不燃位置检查通过。200 tick 后室内与模板外均没有额外液体/火。
6. 断链、打开放火玻璃、阻塞主路三类原生负例，每向各一次，共 12 次拒绝成功；恢复后才继续稳定性验证。
7. `qa/capture-audit.json`：独立 Python 重新读取四向原生世界回采，复核坐标旋转、显式源状态以及全部 12 份方块实体的原始字段（含旗帜图案与空库存）。
8. `qa/reproducibility.json`：独立子目录重建 NBT、room.json、蓝图、材料表，四份字节完全一致，不覆盖原成品。
9. `preview/` 含四向、完整屋顶、坐标平面、原图对照和稳定 200 tick 后世界回采预览。`qa/visual-review.json` 记录已视觉检查的文件与指纹。

首次 Forge 启动会在本目录测试区生成默认服务器配置，日志包含缺省 `server.properties`、FML 设置修正和弃用 API/Gradle 提示；这不代表验收失败。日志末尾保留 required test PASS 与 BUILD SUCCESSFUL。没有隐藏或擦掉这些警告。

## 原图与转译

参考图：`D:\ft工程文件\image2.5\dungeon-shop-variants-20261102\out\shop-variant-F-cataclysm-iron.png`。

SHA-256：`28ba3cc97775b9f94cb68083d19dee1778079488f678f22bdfd0b5cdfe08e65f`。已将原图和源 README / manifest / DISPATCH / F 提示词保存在 `reference/`。源 README 提到的 **STYLES.md 实际不存在**，没有伪造或回填它，差异见 `reference/input-fingerprints.json`。

原图的暗铁转译为磨制深板岩/黑石/黑曜石；金饰使用金块及镀金黑石，红符文用原版旗帜与红陶瓦。天平、武器、小瓶等细粒度非方块物品转译为有支撑的雕纹台、头骨、菌盆、空酿造台及蜡烛。开放火盆改为完整玻璃隔离的墙内熔岩炉。封闭玻璃展柜是整方块近似，不宣称与概念图像素一致。

软件渲染使用本机 Minecraft 1.20.1 `client.jar` 的原版方块贴图与模型。箱体/头骨/旗帜/液面几何是近似；旗帜图案读取实际 NBT 并使用原版蒙版，液体取首动画帧。没有光照、阴影、shader 或客户端性能模拟。剖切只在渲染内存里移除屋顶与相机近侧外墙，不改最终 NBT；墙柱、梁、拱与家具保留。

## 本机重建

本机可用的 Python：`C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe`，只使用其 Python/Pillow，不调用图像 API。使用 PowerShell：

```powershell
$root = 'D:\ft工程文件\FT地牢_商店变体_F_灾变暗铁_20261102'
$python = 'C:\Users\YOUR_USERNAME\.codex\.venvs\imagegen\Scripts\python.exe'
$env:PYTHONIOENCODING = 'utf-8'
& $python "$root\tools\build_room.py"
& $python "$root\tools\qa_static.py"
& $python "$root\tools\run_native.py"
& $python "$root\tools\render_room.py"
& $python "$root\tools\finalize_delivery.py"
```

原生 QA 需要 Java 17、Gradle 8.8 和本机已有 ForgeGradle / Minecraft 缓存；入口支持 `--java-home`、`--gradle`，默认离线运行。测试只写自身 `qa/forge-run` 临时世界。不要把测试 Mod 放到玩家 `mods/`；不要把模板直接写入正式存档。

## 接入边界与回滚

本轮未覆盖 FT 目录槽位分配、大厅连接道路、防覆盖拒绝、持久化幂等、实际玩家/多人/FPS、真实交易和整合包客户端效果。只有统一接入协调人可以在复核四件套、分配正式身份并完成完整 FT 回归后决定部署。

当前未部署，因此丢弃本候选无需改正式服。将来若被接入，必须先备份核心 JAR、配置、正式世界和地牢生成持久数据，再按协调人回滚方案处理。删除模板或卸载 JAR 不会自动拆除已经生成的建筑；不得擅自删除已有生成标记、覆盖玩家修改或热替换正在运行的客户端/服务端 JAR。
