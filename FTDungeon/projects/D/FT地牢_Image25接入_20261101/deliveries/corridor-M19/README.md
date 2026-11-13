# M19 · 苔砖蛛巢积水直廊

独立交付，未接入大厅、未部署 JAR、未打开或修改玩家存档。基于已存在的 image2.5 参考图制作，没有重新生图或付费调用。

## 四件套

- `room.nbt`：Minecraft Java 1.20.1 / DataVersion 3465，gzip、单调色板、7380 条完整体积记录（含显式空气）。
- `room.json`：相对坐标、北南接口、参考指纹、结构指纹、安全适配及独立交付边界。
- `preview.png`：最终 NBT 的四向内部剖视总览，不是 AI 设计图或游戏截图。
- `qa.json`：最终指纹绑定的静态、四视图与 Forge 原生四向放置证据。

尺寸 **15×12×41**（XYZ）；长度 41 格，约 2.56 区块。入口脚位 `[7,2,0]`，安全点 `[7,2,4]`，南出口脚位 `[7,2,40]`，朝北。主路 X=6–8、Y=2、Z=0–40，全程三格宽、至少三格净高、同一高度直线贯通，无转弯、分支或楼梯。两个端口均至少 3×3，地板在 Y=1。

## 对照与安全适配

参考：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M19-webbed-spider-flooded.png`

参考 SHA-256：`8f3c423cf62ca1d8fd47704f0635c2e478d037295f5a6531a16468ba2a5f927f`

结构 SHA-256：`1b9d962d1e340ef638ccab38817b00d400e6784461898be3a452a5bfbc5aded2`

保留灰色苔砖、六跨连续石拱、五盏暖色链灯、十处蛛网壁龛、卵囊与骨堆。原图满地浅水调整为三格中央干燥堤道，两侧 202 格真实水源形成一格深水槽；底部、两侧及北南端均以完整方块密封。蛛网不接触主路，灯链锚在屋顶，骨堆及卵囊落在侧台或壁龛。卵囊使用方解石与石英半砖近似表现，不放实体、刷怪笼或战利品。两处局部缺损仅去掉内拱肩装饰，不破坏承重柱与完整屋顶。

## 验收证据

- `qa/static.json`：完整体积、方块白名单、615 格地基和615格顶面、连续外墙、通路、支撑与封水全部通过；163 个干燥脚位可达。
- `views/interior-four-views.png`：最终 NBT 的北、南、西、东内部剖视；只在渲染时去掉近侧墙和屋面，NBT 完整保留。
- `views/exterior-four-views.png`：最终 NBT 的北、南、西、东完整外壳。
- `views/lodestone-eye-north.png`、`views/lodestone-eye-south.png`：Minecraft Builder / Lodestone 读取同一 NBT 得到的正反向眼平软件预览。
- `qa/forge-native.json`：Minecraft 1.20.1 / Forge 47.4.10 实际 `StructureTemplate.placeInWorld`，0°/90°/180°/270°各7380格，共29520格；强制邻块形状、邻居事件、水源更新，等待240 tick，四向均完整保持状态、无漏水、无掉落，主路碰撞检查通过。
- `qa/forge-run.log`：`M19_FORGE_PLACEMENT_PASS`、`All 1 required tests passed`、`BUILD SUCCESSFUL`。
- `qa/render-manifest.json`：结构与各图像 SHA-256；水面检查图应用原版默认水色 #3f76e4，不代表目标生物群系最终水色。

原生 GameTest 世界仅位于此交付的 `qa/forge/run/m19-only-gametest/`，不是玩家存档。完整整合包客户端、FPS、多人实玩与大厅实际连接未测试，不能将隔离 Forge 证据称为整合包实机游玩通过。

## 重建与复验

配方为 `source/build.mjs`，使用 `C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill/scripts/run-build.mjs`，参数 `--name room --data-version 3465 --out-dir <本交付目录>`。技能 Lodestone 依赖已在本机安装。

`source/verify.py static` 重新生成元数据及静态报告，`source/verify.py render` 生成八向检查图及总览，`source/verify.py finalize` 要求当前 NBT、元数据、图像与原生报告指纹一致后生成最终 `qa.json`。使用 Codex 自带 Python（Pillow）环境；本机已有共享 NBT 解析器及1.20.1原版材质检查渲染器，以脚本中的绝对路径为准。

原生测试工程：`qa/forge/`。运行时将当前进程 `JAVA_HOME` 指向 `C:/Users/YOUR_USERNAME/AppData/Roaming/.minecraft/runtime/java-runtime-gamma-snapshot`（JDK17），使用本机 Gradle 8.8 执行 `--offline --no-daemon runGameTestServer`。系统默认 JDK25 不适配该 Gradle，不要因此修改全局 Java 配置。

## 接入边界

模板 ID 使用 `corridor-M19`，`source_id` 为 `M19`，避免与其他批次房间 M19 混淆；未分配正式唯一槽位。统一接入方须在第一次放置前检查完整15×12×41体积（含显式空气）及液体、实体、世界边界，占用即拒绝；不得将本模板当作无条件清空命令。保留每间独立持久化生成标记，已生成结构不得反复覆盖。北南接口均需维持地面高度与至少3×3净空，不要用关闭门阻塞主路。
