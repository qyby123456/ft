# M18 · 冰霜烛龛悬笼直廊

独立 Minecraft Java 1.20.1 结构交付，使用已安装 Minecraft Builder / Lodestone 0.6.0 制作。只制作 `corridor-M18`，不部署、不修改 FT 核心、不写入玩家存档，也未重新调用付费生图接口。

## 原图与批次

- 批次：`D:/ft工程文件/image2.5/dungeon-corridors-20261103`；已核对 README、manifest、SPEC、corridor_index 和 M18 提示词。
- 原图：`D:/ft工程文件/image2.5/dungeon-corridors-20261103/out/M18-ice-frost-intact.png`。
- 原图 SHA-256：`8d2f06845f6e1310619e555ab8d3d9cf389cc8415cafa70f728eb94127a11ea8`。
- 主题：完好冰霜、暖烛、悬吊铁笼；一点透视直廊，不转弯、无分岔、主路无楼梯。

## 模板参数

- 尺寸 XYZ：`15 × 12 × 48`；长度 48 方块，即 3 区块，属于中型档。
- gzip 压缩单调色板 NBT，DataVersion `3465`。全体积明确记录 8640 格，含空气；不能把这些空气当作允许强行清空占用的授权。
- 北入口玩家脚位 `[7,1,0]`，内部安全脚位 `[7,1,3]`，南出口脚位 `[7,1,47]`；朝北。
- 双端门洞至少 5 宽、4 高；全长主通路保证 5 宽、3 高净空，地板 y=0，脚位 y=1。
- 12 组暖烛壁龛、6 个链条悬吊空铁笼、12 组冰晶垂饰、7 道柱梁节奏；两端均可步行贯通。
- 只用 1.20.1 原版方块。浮冰与蓝冰不融化；白色细节用方解石/石英及少量低光雪块；无普通冰、薄雪、粉雪、流体或危险主路。
- 冰锥以蓝冰和浅蓝玻璃板转译，铁笼顶底用石砖台阶固定；属于原版方块化复刻，不声称逐像素复制概念图。
- 完整屋顶始终保留于最终 `room.nbt`；四视图移除屋顶/近墙仅为只读渲染过滤。

## 验收证据

- `qa/static.json`：原图指纹、gzip/版本、全体积、主路、完整屋顶、封闭侧墙、烛台/铁笼/冰锥支撑、连接幂等检查。
- `qa/forge-native.json`：隔离 Forge 47.4.10 GameTestServer，原生方块注册表及完整属性、模板加载/保存、四向放置、原生碰撞/可生存、邻居更新和 120 tick 稳定性。
- 四向每向核对 8640 格，共 34560 格；每向 398 个可达脚位，入口/内部点/出口连通；12 烛组有支撑、4 雪块光照安全，无意外实体。
- `qa/native-placed-*.nbt`：四向稳定后原生世界回采，逐格与旋转源模板一致；各自 SHA-256 在原生报告中。
- 负例探针：每向一次堵路和一次撤除烛台支撑，均被检测到并恢复。
- `qa/forge-gametest.log`：完整最终运行日志；`qa/forge-compile-attempt-1.log` 保留已修正的首轮 QA 源码编译诊断，不是最终失败。
- `preview.png`：从最终完整 NBT 渲染的北向内部图；`preview/south-interior.png` 为反向。
- `preview/four-view.png`：西北/东北/西南/东南只读剖切；`preview/complete-exterior.png` 为完整外壳。
- 所有图都是 Lodestone 软件预览，不是整合包实机截图；烛光表现为近似，游戏中方块光与雪块安全另由 Forge 验证。

## 可复现

从安装的 Minecraft Builder 技能目录执行其 `scripts/run-build.mjs`，参数为本目录 `recipe.mjs --out-dir <本目录> --name room --data-version 3465 --compression gzip --json`。

在本目录依次运行 `node tools/check.mjs`、`powershell -NoProfile -File tools/run-forge.ps1`、`node tools/render.mjs`，目视复核输出后运行 `node tools/record-visual-review.mjs --confirmed-after-viewing`，最后运行 `node tools/finalize.mjs`。本机 Node、Java 17、Gradle 8.8、Forge 缓存与 Edge 路径在脚本中显式记录。Forge 夹具只在本交付的 `qa/forge-run` 创建独立测试世界，不打开玩家世界；`forge` 工程不是可部署游戏模组。

## 接入边界

未验证 FT 核心目录登记、主大厅连接、占用拒绝/幂等、整合包实机、多人或帧率。接入方仍须按现有契约验证整个结构体积、实体、流体和世界边界，占用则拒绝，不得无条件清空；也不得将本隔离 QA JAR 放入整合包。模板指纹、预览指纹和汇总见 `qa.json` 及 `delivery-manifest.json`。
