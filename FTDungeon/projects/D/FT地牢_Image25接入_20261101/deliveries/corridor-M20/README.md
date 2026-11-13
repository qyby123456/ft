# corridor-M20 · 铜械机廊·完好型

独立中型地牢直廊，13×11×40，长轴Z，40格约2.5区块。由 Minecraft Builder 的 Lodestone recipe 生成 gzip 单调色板 Java 1.20.1 NBT（DataVersion 3465），含完整体积5720条记录及完整顶盖。所有坐标为模板相对XYZ，入口/出口/安全点均为玩家脚部坐标。

- 北入口 `(6,2,0)`；南出口 `(6,2,39)`；内部安全点 `(6,2,3)`；朝北。
- 两端5格宽5格高，门槛为完整铜地板。同高直通主路 `X=4…8, Y=2, Z=0…39`，至少3格净高。无转弯、侧分岔或台阶。
- 橙铜和铜绿墙体、顶梁、侧管、12组压力表、10条橙色壁灯、地面灯槽、4组铁笼吊灯和铁格栅。全部铜材上蜡，避免随机氧化改变状态。
- 主路格栅为关闭的上半铁活板门，表面与相邻整块地板齐平，下一格有底。吊笼下板上半、顶板下半，中间铁栏直接接触两板；中央铁帽连接铁链与顶梁。
- 无实体、容器、库存、奖励、流体、火、刷怪笼或执行方块。

## 参考及1.20.1转译

原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M20-copper-machinery-intact.png`。

原图SHA-256：`ba38b1a755aed26654acd83aa1cfc4fdeb3b722e989f7f80ce963b3096acbae5`。

遵守原图批次 `SPEC.md`、提示词及独立接收契约。1.20.1没有铜格栅、铜灯或独立铜管，分别以铁活板门/铜底、橙色玻璃/菌光体和铜台阶转译；压力表用金/铜框、石英表盘、黑石按钮。熔岩感灯槽采用固体光源，没有真实熔岩。未新增付费生图请求。

## 交付与证据

四件套：`room.nbt`、`room.json`、`preview.png`、`qa.json`。完整指纹见 `room.json`、`delivery-manifest.json`，接收摘要见 `handoff.json`。

`preview.png` 是读取最终NBT的一点透视软件渲染；`preview/interior-{north,south,west,east}.png` 与 `preview/exterior-{north,south,west,east}.png` 为八张单图，另附两张四联图。剖切只在渲染内存中移除屋顶和近侧外墙，保留梁、管线、吊笼与屋顶锚点；实际NBT不剖切。全部使用1.20.1原版模型和纹理，软件预览不等同游戏截图，不模拟动态光照。

`qa/static.json` 检查尺寸、原图指纹、完整记录、边界、屋顶、全程净空与同高地板、悬挂锚点及禁用方块；`qa/visual-review.json` 记录八张图逐图审阅与吊笼顶板衔接修正；`qa/native.json` 是隔离Forge 47.4.10 GameTest实际放置结果。每个旋转比较5720个方块，4向每个快照22880项，明确触发邻居更新并模拟320tick，检查方块状态、支撑、两端与全程碰撞、外边界及掉落实体。稳定后四份 `qa/native-placed-*.nbt` 是世界回采；`qa/capture-comparison.json` 独立逐坐标核验22880项旋转状态与最终结构一致。

首次测试在临时世界Y=100遇到自然陶瓦地形而拒绝放置，失败日志保留；夹具改在Y=300，最终修订在全新的独立平坦世界重跑通过。`qa/baseline/` 中是旧吊笼顶板版本及其证据，不可作为最终版；所有最终报告均绑定最终NBT完整SHA-256。

## 复现

生成：`C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe tools/build-and-check.py`。

渲染：同一Python执行 `tools/render-views.py`；依赖现有 `D:/ft工程文件/FT地牢_R5_石匠工坊_20261102/tools/render_engine.py` 和缓存1.20.1 `client.jar`。该Python环境只提供Pillow；没有调用imagegen接口。

原生：在此独立目录运行 `powershell -NoProfile -ExecutionPolicy Bypass -File tools/run-forge.ps1`。需要本机缓存的Gradle 8.8、ForgeGradle6及Java17，离线构建；默认创建新的 `qa/forge-runs/<时间戳>/` 临时测试世界。测试模组只用于独立验证，不安装到玩家实例。`src/main/`、Gradle配置、空测试模板和启动脚本均随交付保留，便于接收方复制夹具后独立复验；复制后的工程目录名保持 `corridor-M20`。

交互预览：在 Minecraft Builder skill 根目录运行 `node scripts/preview.mjs --nbt "D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/corridor-M20/room.nbt" --port 5320 --no-open`，访问启动器返回的本机地址。

## 接收边界

全局ID为 `corridor-M20`，候选模板 `ftdungeonmobs:image25/corridor_m20`，本批接收槽位60；不可合并为旧房间M20/M2。制作方证据已通过，仅供“DSH 地牢交接与四视图续接”冻结收件并独立复验；`ready_for_unified_integration=false` 表示尚未替代接收者验收。大厅路线、端口连接、占用拒绝、持久化/幂等和整合包实机回归由统一接入阶段处理。

本次只写此独立交付目录，没有部署JAR、修改正式源码/配置或打开玩家存档。Forge测试世界仅位于此目录的 `qa/forge-runs/`；没有整合包客户端/多人游玩或FPS验收声明。
