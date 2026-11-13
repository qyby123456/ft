# M5 末地观星台 · 独立交付

状态：仅 M5 制作与独立原生验收完成，待接入负责人复核；未部署。

## 交付位置

- `out/M5/room.nbt`：Java 1.20.1、DataVersion 3465、gzip 单调色板结构。
- `out/M5/room.json`：真实尺寸、北入口、安全内点、原图指纹和改编说明。
- `out/M5/preview.png`：读取实际 NBT、原版纹理和原生方块形状生成的软件剖切预览，不是 AI 图或实机截图。
- `out/M5/qa.json`：静态、原生注册表、Forge 实际放置与回滚结果。
- `M5-independent-delivery.zip`：四件交付、全部 QA、源代码、测试项目和文本证据；不打包隔离存档与构建缓存。
- `delivery-manifest.json` / `package-receipt.json`：交付文件、结构与压缩包 SHA-256。

![实际NBT剖切预览](out/M5/preview.png)

## 接口与布局

- 实际尺寸 XYZ：`15×10×15`；北面 `z=0`；入口脚位 `[7,1,0]`，安全内点 `[7,1,2]`。
- 三格宽、三格高北开口：`x=6..8, y=1..3, z=0`；入口向内四排地板完整，无门扇和凸出石饰。
- 225 格完整底板，225 格完整屋顶；玻璃星顶带紫珀肋架。绕行环路从北口连通内点与四个观测位，128 个平地脚位可达。
- 中央紫珀/黑曜石观星台、侧向紫晶镜头望远镜、四组末地烛、两座惰性水晶柱、南侧装饰破损门与六处合法紫颂花茎。
- 共 2250 条显式记录，其中 1019 非空气方块、25 种方块状态；零实体、零方块实体、零容器库存、零流体。

## 原图与必要改编

原图：`D:\ft工程文件\image2.5\dungeon-design-batch3-20261101\out\M5-end-stargazer-terrace-hero.png`。

原图 SHA-256：`dcd0ffbe2727cc55608994a09e224cac2a42d78bd1b60aca13d399d6cf858cae`。

结构 SHA-256：`37689df85de484a955fe4be126a03f25b4d9f13646f7b7a65707268ab6370717`。

遵守当前 CONTRACT.md：非花园必须有完整屋顶，因此概念露台改为玻璃星顶，尺寸从概念 `15×15 h8` 调整为 `15×10×15`。虚空浮岛意境不通过危险坑洞实现；不放活动传送门、末地传送门框、爆炸水晶实体。水晶以紫晶和紫玻璃转译，紫颂花 `age=5` 抑制生长。未重新调用 image2.5，也未修改图片或连接配置。

## 测试证据

1. `qa/static-qa.json`：24 项静态检查；指纹、尺寸、完整体积、三格北开口、屋顶、地板、零库存、装饰支撑、全部非空气方块连到底板、五条干燥路径均通过。
2. `qa/native-registry-qa.json`：Minecraft 原生注册表核验全部 25 种状态及属性，原生轮廓形状一并保存用于预览。
3. `qa/native-world-qa.json`：Java 17 / Minecraft 1.20.1 / Forge 47.4.10 独立 GameTest，四种旋转分别实际放置 2250 条记录。每格核对实际状态、`canSurvive`、灯具挂点、完整地板/屋顶；按玩家 0.6×1.8 碰撞体检查原生通路。放置后与 80 tick 后均通过，共两次快照、每次 9000 条记录，随后回滚夹具为空气并复查成功。
4. `evidence/forge-gametest-attempt2.log`：真实 `All 1 required tests passed`、`M5_NATIVE_QA_PASS` 与 `BUILD SUCCESSFUL`。第一次测试因夹具高度相对原点落入地下被占用保护拒绝，未施工，保留在 `forge-gametest-attempt1.log` 与 `native-world-attempt1-fixture-occupied.json`；只调整隔离夹具世界高度为 Y=200 后通过，交付 NBT 未变。
5. `evidence/preview-full-envelope.png` 为完整屋顶外观；`evidence/plan-dry-routes.png` 为实际 NBT 首层与通路；`qa/preview-provenance.json` 标明软件渲染范围及局限。透明/交叉材质以原生 AABB 近似，不等于客户端渲染。
6. 没有测试整合包客户端、多人游玩、FT 大厅实际连接、生成标记幂等或正式存档；这些由核心接入负责人按当前规范验收，不能凭独立 GameTest 宣称已接入。

## 并行边界与回滚

本工程只写自己的根目录。既有九间、其他第三批工程、FT 核心源码、已部署 JAR、玩家存档与生图配置均不修改。原有交付与部署 JAR 的只读指纹见 `evidence/read-only-boundary-baseline.json`，完成时比较见 `qa/read-only-boundary-review.json`；若其他负责人同时修改，以比较记录为准，不能反向覆盖。

接入目标建议：`ftdungeonmobs:image25/m5`。接入负责人先审核四件交付与哈希，再自行导入 `incoming/M5`、追加唯一槽位与大厅支路；本线程不抢写共享 incoming、catalog 或部署目录。

当前未部署，撤回仅移除 M5 候选暂存，不会改变游戏。原生测试已验证“已知空夹具 → 实际放置 → 80 tick 复查 → 恢复原空夹具”。这不是生产存档的通用回滚命令；将来施工前必须保留整个目标体积的世界快照，并沿用既有逐房标记，禁止直接 `fill air`、覆盖玩家改建或重建旧房。

## 重跑

在 PowerShell 运行 `& .\Run-QA.ps1`；要按脚本重建同一 NBT 则使用 `& .\Run-QA.ps1 -Rebuild`。需要已配置 JDK 17、Forge 47.4.10 离线 Gradle 缓存以及 bundled Python/Pillow。脚本临时设置进程级 Java 路径并在 finally 恢复，GameTest 只使用本目录 `native-test/run/m5-isolated-gametest`，测试类还检查 GameTestServer 类型和存档绝对路径。

`tools/nbtio.py` 是从既有 `output/dungeon-expansion-20261101/tools/nbtio.py` 只读复制的本地 NBT 工具，其余生成、QA 与渲染脚本在本工程独立维护。独立测试模组 `m5qa` 只服务验收，禁止安装到正式实例。
