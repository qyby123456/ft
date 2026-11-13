# P02 苔石水渠跳台

独立地牢分区房间候选，原版 Java 1.20.1，Minecraft Builder / Lodestone 配方生成。未部署、未接入 FT 核心、未写入玩家存档，所有工程、测试与证据保存在本目录。

四件套：room.nbt、room.json、preview.png、qa.json。结构尺寸 **17 × 11 × 39**，DataVersion **3465**，gzip、单一调色板，7,293 个坐标全部显式记录（包含空气）。摆放前仍须由接入方检查整个体积，不能把空气记录当作无条件清空授权。

原图：D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/parkour/P02-parkour-water-channel-jumps.png。

原图 SHA-256：518f9e89fadea35943d46096f0fb7d5eae409ef8718eaf15f1151cb9ef4a22c9。

NBT SHA-256：6a147a2b744cf8f6e4ea425c84f54591c2dcd65459e09da0191dd0c000800fcf。

## 接口与通关

- 北侧入口玩家脚位 [8,3,0]，内部安全点 [8,3,3]，南侧出口 [8,3,38]。两端均为 3 宽 × 3 高干燥开口，脚下完整方块；连接方按入口 Y=3 接路。
- 八次同高短跳，每次方块边缘净空隙 1 格，落脚台最小 3×3。平台沿 Z 方向交错排列，X=8 为可验证的中心跳跃线。原生普通跳跃正反向均通过。
- 中途 [8,3,19] 是完整的干燥休息台，提供调整方向和重新起跳的位置；它没有改变玩家重生点或执行任何指令。
- 水渠为 1 格深浅水，落水下落 1 格。Z=8、16、24、32 的两侧均设三格宽恢复台阶；原生玩家从水底经两次半格抬升回到侧廊，再返回北侧起点。两段水池均有恢复出口。
- 南端侧廊设挡墙，阻止从起点沿干地直接走到出口。玩家仍可主动游水或从侧廊重试，本建筑不提供计时、计分或强制挑战机制。
- 原图的苔石、木栏、石柱梁、暖色灯笼、木桶与青旗保留；水景改为玻璃护面封闭竖井，避免水流冲刷跳跃线。最终 NBT 有完整石屋顶，剖视仅用于渲染。

## 验收证据

- qa/static.json：来源 README/manifest/verify 指纹、完整体积、屋面/地板/水底、开口、安全点、八段跳距、空容器、灯饰支撑、池体局部密封和恢复出口。
- qa/native.json、qa/forge-20261103-p02-final-2.log：Forge **47.4.10**、Minecraft **1.20.1** 独立 GameTestServer，Y=160 隔离夹具，四旋转实际放置，100/200 tick 后全部 7,293 条状态和支撑不变，每旋转 156 个液体格、0 外漏，7 个空家具方块实体。
- qa/route-summary.json：最终稳定状态下共 **64 次**原生玩家跳跃（每旋转正向8次、反向8次），**32 条**真实落水与爬阶轨迹。原生 jumpFromGround、baseTick、travel 与 0.6×1.8 玩家碰撞；落水不掉血、眼部不淹没。侧廊至入口由原生碰撞体连续采样。
- preview/four-view-sheet.png、preview/four-shell-sheet.png：内部/完整外壳各四向；preview/interior.png 为完整结构北口内景，preview/roof.png 为未剖切屋面。
- preview/native-placement-sheet.png 与 qa/native-placed-*.nbt：四旋转稳定后的世界回捕；qa/render.json 记录19张 PNG 与实际渲染 NBT 的 SHA-256。checksums.sha256 覆盖四件套和主要证据。
- 首次资源包元数据缺失、低空地形水干扰、扩展测试内存不足的日志保留。最终测试内存 1 GiB，成功判定同时核对新鲜报告、四旋转结果和明确的 GameTest 通过日志，未把旧报告或 Gradle 的误成功当作验收。

这是隔离 Forge 的服务端原生放置与运动证据，不宣称整合包客户端真人连续通关、多人或 FPS 验收。外部大厅连接、占用拒绝、幂等持久化及正式部署由核心接入负责人处理。

## 复现

1. 在 C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill 执行 node scripts/run-build.mjs "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-P02/recipe.mjs" --out-dir "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-P02" --name room --data-version 3465 --json。
2. 本目录执行 node tools/static-qa.mjs，再执行 ./tools/run-forge.ps1 -RunId 20261103-p02-final-2。重复验证建议选新的 RunId，并同步调整汇总脚本指定的最终日志名。
3. 本目录执行 node tools/preview-server.mjs，另终端执行 node tools/capture-previews.mjs --native。只读本房间和技能自带纹理包，不修改交付 NBT。
4. 视觉检查后执行 node tools/finalize-delivery.mjs，汇总四件套状态和证据指纹。
