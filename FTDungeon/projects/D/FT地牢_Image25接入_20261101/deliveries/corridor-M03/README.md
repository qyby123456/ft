# M03 · 血誓烛廊（中型独立走道）

参考图：D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M03-blood-ritual-intact.png。已按 manifest 与索引核验，未调用任何付费生图接口。

- 图片 SHA-256：2bd12059b40ca01c7f4576e3af80f099382dc20b1c87ae34128332f5391c33ea。
- 结构 SHA-256：a2b0b27d1d046aab771cfa84fea24d3985273b74380c7a19d62af3ea44d9e5ca。
- 尺寸 X×Y×Z：13×12×41；轴线 +Z，长度 41 格，约 2.56 区块。
- 北入口脚位 [6,1,0]，内部安全脚位 [6,1,4]，南出口脚位 [6,1,40]。
- 中央主路 x=4..8，全长 5 格净宽、至少 3 格净高、平地直通；没有转弯、侧分岔或通行楼梯。

## 设计落地

灰石砖与深板岩水平分带，6 道递进石拱，5 组左右对称烛龛，10 面黑红纹样旗、20 簇点燃蜡烛。中央三格血红嵌毯与两条暗红槽线保留参考图的祭仪识别度。所有红槽为实心地面，不使用真实血液、水或熔岩；红毯与石路齐平。屋顶完整封闭。仅少量裂纹和苔石点缀，不把 intact 主题改成坍塌废墟。

## 交付与证据

- room.nbt：Minecraft Java 1.20.1、DataVersion 3465、gzip、单调色板；6396 条完整体积记录，3468 条非空气方块。
- room.json：入口、出口、尺寸、双端净空、原图和结构指纹、装饰支撑清单。
- preview.png：最终完整 NBT 的北入口中轴透视；南向反看在 views/perspective-south.png。
- views/interior-four-directions.png、views/exterior-four-directions.png：北南西东内部剖切和完整外壳；每个方向另有原尺寸图。剖切只发生于图片，未删改 NBT。
- qa.json：汇总验收；qa/static.json、qa/native.json、qa/capture-review.json、qa/visuals.json：机器可读细项。
- qa/forge-gametest.log、qa/forge-latest.log、qa/forge-debug.log：真实隔离测试日志；qa/world-*.nbt：四向运行后世界回采。

## 已通过验证

1. Minecraft Builder 的 Lodestone 生成与 gzip NBT 往返；原图 SHA-256、单调色板、完整坐标体积与 1.20.1 方块范围检查。
2. 205 个主路脚位全部连通，双端入口通畅，完整屋面；没有液体、陷阱、容器、战利品、指令方块或预置实体。
3. Forge 47.4.10 / MC 1.20.1 / Java 17.0.8 原生注册表、属性、旗帜方块实体和 StructureTemplate 往返。
4. NONE、CLOCKWISE_90、CLOCKWISE_180、COUNTERCLOCKWISE_90 实际放置，强制加载测试区块并进行邻居更新；200 tick 后 4/4 通过。逐向核对 6396 条，共 25584 条，无状态漂移或掉落物。
5. 原生碰撞、完整地板/屋顶、旗帜纹样和所有蜡烛/旗帜支撑通过；8 次缺支撑和 4 次阻塞主路负向探针均能检出。
6. 对四份实际世界回采 NBT 独立逆向核对，25584 条语义完全一致，10 面旗帜纹样逐向保留。

## 预览与复现

Python 使用 C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe，仅用于本地 NBT、Pillow 图像与检查，不调用生图 API。

1. tools/build.ps1：调用已安装 Minecraft Builder 的 scripts/run-build.mjs，再生成 room.json。
2. Python 执行 tools/qa_static.py、tools/render_m03.py。
3. tools/run_native.ps1：离线 Gradle 8.8 执行独立 Forge GameTestServer；只写本目录 forge/run 的临时世界，绝不安装到游戏实例。
4. Python 执行 tools/verify_captures.py、tools/finalize.py。

可用 Minecraft Builder 的 scripts/preview.mjs --nbt 本目录/room.nbt --no-open 打开 Lodestone 互动预览。本次本地服务为 http://127.0.0.1:5303/；离屏浏览器首次截图停在加载页，已标记为 qa/lodestone-loading-rejected.png，不计入验收图片。全部正式图像由最终 NBT 和原版 1.20.1 模型/材质计算生成，非 AI 图替代品。

## 边界与接入注意

未部署、未改 FT 核心、未动玩家存档、未修改其他 ID 或并行交付。没有验证正式槽位、接入道路、幂等持久化、整合包客户端、多人或 FPS，也不声称完成它们。Forge 生成一次性配置、版本联网检查和临时 DUMMY 方块实体警告保留在原始日志，最终逐方块/支撑/纹样及回采全部通过。

整合负责人施工前必须检查整个 13×12×41 放置体积、实体及液体占用；NBT 的显式空气不意味着可以无条件清空已有建筑。不可把本目录的 Forge QA 测试模组部署到玩家实例。
