# M6 地狱烈焰堡垒 · 独立建筑交付

2026-11-02（Asia/Shanghai）。仅负责 M6；未修改 FT 核心、现有九间房、玩家存档、已部署 JAR 或图片配置。

## 文件与接口

- 四件套：`delivery/M6/room.nbt`、`room.json`、`preview.png`、`qa.json`。
- 原图：`D:/ft工程文件/image2.5/dungeon-design-batch3-20261101/out/M6-nether-bastion-hall-hero.png`，1536×1024，2,567,629 字节；SHA-256 `4fbbc2048340fbff123189e5df3bc011b8c5ba1fedef83a0950f2795113e0c91`。
- 成品 NBT SHA-256：`04c05c4329f079b5093bfd61638c52421e6e9e9089cb2c96d163f63134c5be27`。
- Java 1.20.1，DataVersion 3465，gzip 单调色板，模板 **21×12×21**，5,292 条记录，其中非空气 2,539 格，43 个状态。
- 北向 `entrance=[10,2,0]`，安全内点 `interior=[10,2,4]`；均为模板相对脚部坐标。入口三格宽、三格高、六格直进净空，下方完整地板。
- 规划模板名 `ftdungeonmobs:image25/m6`；固定槽位、大厅支路及目录只能由统一接入负责人分配，此工程不提供未经协调的施工命令。

## 概念落地

保留红地狱砖与黑石墙、玄武岩四柱、绯红拱梁、双熔岩瀑布与承池、魂火战祭坛和金石基座、两只锁链吊笼、两只金钟、诡异木小桥、根须与骨饰。

概念占地约15×15，为三格接口和可绕行通道合理放大为21×21；墙高九格，双层基座和完整顶盖使模板总高为12。实际屋顶不剖除。双瀑布和小槽使用原版完整玻璃密封，桥面跨封闭槽；地面亮纹用菌光体而不是裸露熔岩。绯红木、诡异木均使用原版不可燃材质；没有可燃主世界木材、羊毛或普通火。

魂火在灵魂沙上，四向石栏围护，主路不穿祭坛火源。两笼均为静态结构，无生物或物品实体。六盏灯、两钟、42格锁链都有实际支撑。仅有两钟和两头骨，共四个合法方块实体；零容器、零奖励库存、零战利品表。

## 验证证据

1. `qa/static.json`：独立重读实际序列化NBT，检查尺寸、坐标唯一性、原版命名空间、底板/屋顶/围墙、北接口、密封、魂火基座和吊链锚点。
2. `qa/native.json`、`qa/gametest.log`：Java17 / Minecraft1.20.1 / Forge47.4.10，真正 `GameTestServer` / `ServerLevel` / `StructureTemplate.placeInWorld`，不是仅NBT解码或软件模拟。
3. 同一原件按0°、90°、180°、270°独立放置，21,168条记录逐格复核；每个方向检查2,539实方块的原生 `canSurvive`、四个方块实体、319个可达干燥脚位、九个地标往返路线、三格入口和2,114格外围留白。
4. 显式调度熔岩tick，启用火蔓延和随机tick，在强加载的独立夹具区运行400tick，再次复核：每个方向38格熔岩仍密封、一处魂火存续、所有灯钟链条存续、无外溢/掉落实体/新增火、干燥路线保持。日志 `M6_NATIVE_GAME_TEST_PASS: rotations=4/4; records=21168; ticks=400`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。
5. `qa/four-views.png`、`qa/exterior.png`、`qa/floorplan.png`：从实际NBT读取，用本机1.20.1原版模型和纹理软件渲染；熔岩取首帧，钟用简化金色体块。不是游戏截图，剖切不修改最终NBT。

首轮因测试资源缺少 `pack.mcmeta` 导致夹具模板未加载；补齐后第二轮通过。原始失败日志保留为 `qa/gametest-attempt1-missing-pack-metadata.log`，不把失败轮算成功证据。

## 复现

Python使用 `C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`（Pillow已配置）。依次运行 `tools/build_m6.py`、`tools/render_m6.py`。构建会将QA重新标为待原生验收，需随后重跑原生测试及终检。

原生验证在工程根运行，`JAVA_HOME` 指向 `C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot`（17.0.8），使用现有Gradle8.8执行 `--offline --console plain runGameTestServer`，保存完整输出至 `qa/gametest.log`。`tools/finalize_m6.py` 重新核验全部指纹；添加 `--publish` 时只向尚不存在的 `incoming/M6` 写四件套。

测试用模组源码与临时世界只用于独立QA，不能打包进生产模组或复制到整合包mods。400tick功能检查不代表完整整合包客户端、FPS或多人游玩验收。接入时仍须核心负责人做固定槽位、共享路、占用拒绝、幂等、回滚和生产打包验收。

## 回滚边界

此交付尚未部署；新建的 `incoming/M6` 是唯一共享目录写入，未覆盖旧文件。接入前回滚只需协调者移除本次M6四件套；保留独立工程和ZIP即可复原。将来正式接入后的撤销与已生成世界回滚由核心负责人按其发布备份处理；不要自动删除玩家维度、生成标记或返程记录。
