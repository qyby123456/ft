# S07 紫晶积水短廊

依据 2026-11-03 地牢走道批次 S07 制作。使用 Minecraft Builder/Lodestone 导出 gzip 单调色板 Java 1.20.1 模板，DataVersion 3465。尺寸为 13 × 11 × 16 方块，Z 轴长 16 格，即一个区块长度；无转弯、分岔和地形台阶。

## 四件套

- room.nbt：完整体积 2288 条记录，包含显式空气，完整屋顶和地基。
- room.json：原图指纹、局部坐标、两端开口、水池、设计偏差、接入边界。
- preview.png：从最终 NBT 和 1.20.1 原版材质生成的内部剖视图。
- qa.json：静态、四视图人工复核、Forge 原生放置与最终文件一致性的综合结果。

模板 SHA-256：ba47a1f1f426b18f8a6698217fb36a073cbc9fe649e4efc10bc050734afecb19。

## 与原图对应

原图：D:\ft工程文件\image2.5\dungeon-corridors-20261103\out\S07-crystal-cave-flooded.png。

原图 SHA-256：7775b4e93df8aca46065fcc18c12ceba8cf20979d263ceaf2947f539b9759be9，制作前已核对。参考批次同目录 README.md、SPEC.md、manifest.json 和 prompts/S07-crystal-cave-flooded.txt。

保留苔石砖/裂石砖、紫水晶脉和侧生晶簇、冷白末地烛、两座悬吊铁笼、天花板横梁与潮湿水池。原版紫水晶不自发光，实际照明由十二支末地烛提供。

为满足主路安全和一字贯通，原图全幅积水调整为两侧各 3 × 12 格、一格深的密封浅水池，共 72 格水源；中央为三格宽的全程干燥石面通路。原图尽头封闭铁栅改成两侧紫晶栅窗和中央开放拱口。这些差异已登记在元数据和 QA 中。

## 局部坐标与接入

北入口脚位 [6,2,0]，内部安全点 [6,2,3]，南出口脚位 [6,2,15]。地板顶为 Y=2；中央通路 X=5..7、Z=0..15，脚位 Y=2，全长净空至少四格。两个端口均为三格宽、至少四格高。

接入方须按自身走道脚位对齐模板原点 Y+2，并按原契约验证整个 13 × 11 × 16 体积的占用、世界边界和液体。模板含显式空气，不可据此无条件清空正式世界。模板 ID 使用独立的 corridor-S07，不复用旧房间 S7；正式槽位、持久标记和大厅连接由统一交接方分配。

## QA 证据

- qa/static.json：12 项静态检查，包含安全通路 BFS、72 格水源底和四边密封、完整屋顶/基底、装饰支撑、调色板和实体检查。
- four-views/：北南西东四向内部剖视、四向完整外壳、两张拼图及最终 NBT 指纹清单。剖切只改渲染可见集合。
- qa/visual-review.json：对照原图逐项审查支撑、柱梁、吊笼、通路、开口、水池和屋顶。
- qa/forge-native.json、qa/forge-run.log：Forge 47.4.10 / Minecraft 1.20.1 隔离 GameTest，四旋转真实 StructureTemplate.placeInWorld，观察 240 tick，主动触发邻块形状/事件更新及水源流体 tick。
- 原生测试逐格核对 9152 次放置状态、真实碰撞通路、15 处晶簇与 12 支灯支撑、8 条吊笼顶链、完整屋顶/基底，零外泄、零掉落、零库存/战利品表/预置实体。
- qa/lodestone-preview.png、qa/lodestone.json：本机 Edge 加载 Minecraft Builder 交互预览的截图和浏览器无异常证据。

软件图的水色使用固定蓝色预览染色；它和 Lodestone 截图均不是整合包客户端截图。原生测试只包含原版和本地隔离 QA mod，未进行整合包客户端游玩、性能或多人验证。

早期 QA 日志保留在 qa/forge-attempt*.log。原生复验发现 Lodestone 自动铁栅连接会把晶簇/末地烛误当完整连接面，已在配方中显式计算铁栅连接，并使用 --no-block-state-updates 导出。自然地形测试产生苔藓/杜鹃掉落，来源见 qa/environment-diagnostic.json；最终使用本目录新建受控虚空平坦测试世界与高处放置，未放宽零掉落和零外泄断言。

## 复现

执行 tools/verify.ps1 可在本目录重建、静态复验、重新渲染及运行隔离 Forge 测试。它使用已安装 Minecraft Builder、Java 17、缓存的 Gradle/Forge 和 Python/Pillow，不下载或修改技能脚本。forge/gradle.properties 仅对离线测试关闭 ForgeGradle 外部证书连通探测；未修改系统、正式游戏或证书信任设置。

修改配方后，如 NBT 指纹变化，必须重新执行四视图和浏览器复核并更新审查证据；打包工具会拒绝混用旧指纹。delivery-manifest.json 记录交付文件哈希与大小。

仅在指定交付子目录写入。未部署、未改正式源码/JAR/配置、未写玩家存档、未分配正式房间槽位。
