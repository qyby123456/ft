# M17 · 紫晶浅水拱廊

独立中型直廊，17 × 13 × 41 方块，轴长约 2.56 区块。使用 Minecraft Builder / Lodestone 生成 Minecraft Java 1.20.1、DataVersion 3465、gzip 单调色板结构。北南两端贯通，无转弯、支路和主路楼梯。最终状态与指纹以 `qa.json`、`room.json` 为准。

## 交付

- `room.nbt`：完整结构，含明确 air 的 9,061 格体积；最终版本保留全部墙顶。
- `room.json`：尺寸、接口、脚位、最终 SHA-256、参考图指纹、集成约束和设计适配。
- `preview.png`：最终 NBT 的 Lodestone 中轴透视，不是概念图或游戏截图。
- `qa.json`：静态、四视图、Forge 四向放置与稳定性验收索引。
- `views/interior-four-views.png`、`views/exterior-four-views.png`：北南西东剖视及完整外壳，各含四张独立 PNG。
- `views/cutaway-overview.png`：实际 NBT 的软件剖视；水使用原版默认蓝色 tint。
- `views/lodestone-axis-north.png`、`lodestone-axis-south.png`、`lodestone-axis-night.png`：实际 NBT 的中轴辅助预览。
- `qa/`、`evidence/`：机器可读检查、完整日志和首次夹具占用拒绝记录。
- `tools/`、`native-test/`：建造配方、渲染、封包脚本和独立 QA 工程，均只服务本 ID。

## 原图与还原

参考批次：`D:\ft工程文件\image2.5\dungeon-corridors-20261103`。已阅读批次 README、SPEC、DISPATCH、M17 原始提示词及项目 CONTRACT。

原图：`D:\ft工程文件\image2.5\dungeon-corridors-20261103\out\M17-crystal-cave-flooded.png`。

原图 SHA-256：`29a6f9575aae21c6ea8f5863b62cde1aa63e7de9d9273248d82e51ff0325bf50`。

保留灰石、紫晶矿脉和晶簇、六道石拱、苔痕、两侧浅水与橙色熔岩壁光。参考图中心的积水改为五格宽同高干燥石脊；裸露熔岩改为玻璃封闭的墙内竖槽。破损以裂纹和侵蚀材质表达，拱券持续连接墙顶。原版没有紫色动态光，紫晶、紫玻璃和珠光蛙明灯仅表现紫色主题，不承诺光影包的反射或彩色光。

## 通路与接口

坐标均为模板局部整数 XYZ，脚位不是地板坐标。

| 接口 | 脚位 | 朝向 | 开口 |
| --- | --- | --- | --- |
| 北入口 | `[8,3,0]` | north | x=6…10、y=3…7 |
| 北安全点 | `[8,3,3]` | — | 干燥平地 |
| 南安全点 | `[8,3,37]` | — | 干燥平地 |
| 南出口 | `[8,3,40]` | south | x=6…10、y=3…7 |

全程干路 x=6…10、z=0…40、地板 y=2、脚部 y=3。集成连接器应对齐脚部 y=3。两侧水渠深一格，水仅在 y=2，入口和出口均有完整止水端坎。集成时按完整 17×13×41 体积做占用、世界边界、液体和实体检查；有占用就拒绝，不允许无条件清空。

## 验证

静态检查覆盖指纹、1.20.1 单调色板、显式体积、两端净空、五格干路、连通、屋顶和底板、液体底边密封、晶簇及地衣支撑、结构固体连接基础、无容器、实体或危险功能块。

Forge 47.4.10 / Minecraft 1.20.1 的独立 GameTest 用原生 StructureTemplate 放置北、东、南、西四向。在每向 9,061 格逐格发出邻居通知，并对全部 204 格液体显式安排流体 tick。经过 600 游戏 tick，每向的 9,061 格状态完全一致，174 格水源、30 格熔岩源保持不变；模板外两格 halo 共 7,004 格保持空、无液体。每向复核原生支撑、0.6×1.8 玩家体积及扫掠碰撞路线、两端安全点、无实体和空夹具回滚。

首次测试在隔离世界 y=96 发现夹具被生成地形占用，按规则拒绝放置；保留失败日志。第二次改用 y=248 的已验证空高空夹具，完整重跑通过。最终 NBT 没有因这次夹具调整变更。

软件四视图和 Lodestone 预览均读取最终 NBT，并与其 SHA-256 绑定。Lodestone 的液体纹理、透明排序、灯光和生物群系 tint 存在预览近似；液体种类、碰撞与行为以 NBT 和 Forge 报告为准。软件四视图用于观察封闭熔岩槽、拱券连接和完整外壳。预览剖切仅影响渲染，结构文件保留完整墙顶。

本交付完成独立结构验收；未部署、未写入玩家存档，也未完成 FT 核心统一接入或整合包客户端游玩验收。全局槽位由统一接入负责人分配。

## 复现

在本交付目录执行 `./Run-QA.ps1 -Rebuild` 可重建 NBT、重跑静态与 Forge QA。JDK 17、Gradle 8.8、Forge 依赖使用现有本机环境及离线缓存；GameTest 的 `server-port=0`，测试世界仅在本目录下。

软件四视图：使用已配置 Python 执行 `tools/render_views.py`。Lodestone：从 `C:\Users\YOUR_USERNAME\.codex\skills\minecraft-builder-skill` 运行 `node scripts/preview.mjs --nbt "D:\ft工程文件\FT地牢_Image25接入_20261101\deliveries\corridor-M17\room.nbt" --no-open --port 5197`，再执行 `node tools/capture-preview.mjs` 保存主预览。最后执行 `tools/finalize_delivery.py` 聚合现有真实报告，指纹不匹配或任一终态未通过时拒绝封包。
