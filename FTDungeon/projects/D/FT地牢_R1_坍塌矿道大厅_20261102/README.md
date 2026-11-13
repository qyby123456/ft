# R1 坍塌矿道大厅 · 独立交付

2026-11-02，Asia/Shanghai。只在本工程写入；未部署、未修改 FT 核心/正式存档/已安装 JAR/共享 incoming/其他房间，不重新调用 image2.5，也没有付费生图请求。

## 四件套与接口

- 工程根：`D:/ft工程文件/FT地牢_R1_坍塌矿道大厅_20261102`。
- 交付：根目录 `room.nbt`、`room.json`、`preview.png`、`qa.json`。
- Minecraft Java 1.20.1 / DataVersion 3465，gzip 单一调色板，尺寸 **17×11×17（XYZ）**，3,179 条显式记录，1,351 个非空气方块，35 个状态。
- 北入口脚位 `entrance=[8,1,0]`，内部安全脚位 `interior=[8,1,4]`，北侧三格宽、三格高接口，下方完整方块地板。
- 中央 `x=7..9,z=0..15` 全程三格宽、三格高通路，轨道无玩家碰撞。入口、安全点及六处观景/工作点可往返，178 个干燥脚位可达。
- 候选模板名 `ftdungeonmobs:image25/r1` 只作建议，未登记。固定槽位、共享道路、怪物层级、生成标记及正式接入由统一负责人决定；不提供未经协调的玩家施工命令。

## 原图与设计取舍

- 原图：`D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R1-collapsed-mine-hall.png`，1536×1024。
- 原图 SHA-256：`cfe244bb4c9220b41ecd464b4c02c5fd33192d268e6f727372713ebcfb2a86df`，与派发、批次 manifest 一致。
- NBT SHA-256：`8521470dc375874496f291b400695e29bc0980d36dd67526c9d0ea2c7d9e8760`。
- 保留深板岩矿墙、铁矿/煤矿露头、双组木制矿井支架、锁链灯、矿轨、矿工工作台、铁砧、空矿石桶、右侧塌方与落地断梁。原图约13格占地，为三格入口、无障碍中路和侧边布景扩为17格。
- 54 个塌方/断梁方块自下而上落地；包含5格砂砾，全部有完整支撑。完整四柱由石脚、竖木、横梁与上部撑木连续连接屋顶，两层横向承重联系保留。斜梁用原版阶梯形落地断木代替任意角度模型。
- 为遵守零实体契约，概念中的矿车改为侧轨上的静态空炼药锅矿斗；不含真正矿车、掉落物、怪物或盔甲架。主轨位于中路，不被矿斗阻断。
- 六个木桶均为空库存，无战利品表、刷怪笼、命令/结构/拼图方块，无水、熔岩或火源。五灯、八格锁链和两挂钩都有支撑。主路不依赖跳跃、开门或破坏方块。
- 非露天房，17×17整层石顶封闭；坍塌表示先前局部事故和已落地碎石，不留通天缺口。预览剖切只影响绘图，不改NBT。

## 验收证据

1. `qa/static.json`：从已写入的实际NBT独立重读，验证坐标唯一/完整体积、原版命名空间、完整地板/屋顶/围墙、北口、落地塌方、砂砾/铁砧基座、连续支架、灯具支撑和空容器。
2. `qa/native.json`：Java17 + Minecraft1.20.1 + Forge47.4.10，真正 GameTestServer、ServerLevel、`StructureTemplate.placeInWorld`；四向独立放置0°/90°/180°/270°，逐格核验共12,716条记录及全部原生状态/支撑/容器。
3. 每向验证1,351个非空气块原生`canSurvive`、6个原生空木桶实体、54个落地塌方、5灯、19轨、6个重力方块、完整地板/屋顶578格、1,514格外围留白；用0.6×1.8玩家碰撞体验证178个干燥脚位和6个地标往返。
4. 强加载测试区块，显式调度全部重力方块tick并发出邻居更新，启用随机tick和火蔓延，运行400tick后逐格复检四向；零移位、零掉落实体、零新增方块/液体，轨道、砂砾、铁砧、灯具与路线不变。
5. 六个测试区破坏性负例均被识别并恢复：地板洞、屋顶洞、入口堵塞、塌方下方被掏空、支柱断裂、灯链锚点移除。它们没有修改交付NBT，也没有修改玩家世界。
6. `qa/forge-native-console.log`保留完整构建输出；成功标记`R1_NATIVE_GAME_TEST_PASS: rotations=4/4; records=12716; ticks=400`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。独立服务器日志另保留为`qa/forge-server.log`。
7. `preview.png`、`qa/four-views.png`、`qa/exterior.png`、`qa/floorplan.png`从最终NBT生成，采用本机1.20.1原版模型/纹理，不使用AI图冒充成品，也不是游戏截图。上述视图已逐张目视检查，渲染前后NBT哈希不变。`qa/render.json`含模型、贴图和图像指纹。

本轮原生测试首次即通过。日志保留了新建隔离世界时自动生成默认配置/缺少初始server.properties的提示，以及施工/区块准备期间一次10,205ms、204tick落后的警告；它不影响功能检查通过，但不能据此宣称无卡顿或达到实机FPS目标。未测试完整整合包客户端、多人、FT大厅共享连接、占用拒绝或核心持久化幂等。

## 复现与交接

Python：`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`，Pillow已配置。按顺序执行：

```powershell
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' tools/build_r1.py
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' tools/render_r1.py
$env:JAVA_HOME='C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot'
$env:GRADLE_USER_HOME='C:/Users/YOUR_USERNAME/.gradle'
& 'C:/Users/YOUR_USERNAME/.gradle/wrapper/dists/gradle-8.8-bin/dl7vupf4psengwqhwktix4v1/gradle-8.8/bin/gradle.bat' --offline --console plain runGameTestServer *> qa/forge-native-console.log
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' tools/finalize_r1.py
```

重建会将总QA改回待原生验证，不应只运行建造脚本就宣称已验收。原生测试只准在`qa/forge-run`隔离世界运行，测试模组不得复制到整合包mods。`tools/nbtio.py`和`tools/mc_models.py`为M6独立交付中的工具原样复制，不曾修改来源工程。

`delivery-manifest.json`包含四件套、源代码、渲染与原生证据的完整SHA-256；`R1-collapsed-mine-hall-handoff.zip`仅打包这些交付/证据/复现源文件，不包含Gradle缓存、测试模组JAR或世界。`package-receipt.json`含压缩包和manifest指纹。ZIP内部文件解压前已逐项验证哈希。

未部署，因此没有生产回滚操作。接入负责人应先校验四件套指纹并复验，随后单独安排目录/固定槽位/大厅道路/全体积占用拒绝/独立持久生成标记/生产备份。禁止直接清空占用范围、重建已生成房间或删除玩家返程记录。
