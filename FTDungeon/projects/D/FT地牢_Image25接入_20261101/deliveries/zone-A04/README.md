# A04 砸毁工坊 · 独立房间交付

四件套位于本目录：`room.nbt`、`room.json`、`preview.png`、`qa.json`。制作使用 Minecraft Builder；只写入 `deliveries/zone-A04`，没有部署或修改正式实例、玩家存档、其他房间。

- 尺寸 **19 × 11 × 19**；北入口脚位 **[9, 1, 0]**，安全点 **[9, 1, 3]**，朝北（−Z）。地板 y=0。入口三格宽、三格高；中央 x=8..10、z=0..14 为三格宽三格高的水平安全通路。
- 原图：`D:/ft工程文件/image2.5/dungeon-zone-batch-20261103/out/abandoned/A04-abandoned-smashed-workshop.png`。
- 原图 SHA-256：`514982633d51b5b667a0edac720160ec3ae2ce16c92df65df478be0ea824f7c2`。
- 最终 NBT SHA-256：`6bb3b157f5a457515cc39cbb9f8532526823f8e28dc0c1a80c45e509ee89ff7e`。

## 建筑与图像转译

保留苔石/裂石墙、四根落地石柱、木梁网、双链吊灯、长工作台、工具架、磨石、切石机、破损铁砧、冷炉烟道、封板窗、空箱桶和地面木石残骸。木梁与柱顶衔接，工作台有完整底座，工具链连接上方木架，灯与残骸有支撑。工位和容器位于安全通路两侧。

概念图的屋顶缺口改为密封裂石修补区；最终模板 y=10 的 361 个屋顶坐标全部完整。窗洞用玻璃封底，木板装饰不形成额外出口。原图的精细金属工具用原版磨石、链条和附墙按钮表达；不使用展示实体或非原版方块。三只木桶、一只箱子和一座冷炉全部空库存、无战利品表。

`preview.png` 是最终 NBT 的入口内部视图。`views/interior-four-views.png` 和 `views/exterior-four-views.png` 为北、南、西、东四向证据；`views/four-direction-review.png` 汇总八视图。内部剖视仅在渲染内存中移除 y≥9 屋顶与近侧外墙，保留承重梁和陈设；未改动交付 NBT。视图来源和逐图 SHA 见 `views/manifest.json`。

## QA 结果

- 静态 **PASS**：Java 1.20.1 DataVersion 3465，gzip 单调色板，3,971 个唯一坐标（含显式空气），1,920 个非空气方块，35 个方块状态；19 项检查通过。零实体、零流体；无控制方块、刷怪笼或危险通路方块。
- 制作方四向人工视觉审阅 **PASS**：支撑、柱梁、通路、入口、容器和屋顶见 `qa/visual-review.json`。
- 隔离 **Forge 47.4.10 / Minecraft 1.20.1 PASS**：原生 `StructureTemplate.placeInWorld` 进行 NONE、90°、180°、270° 四向放置；触发全部邻居更新、强制区块 tick，观察 **240 tick**。稳定后逐坐标检查 **15,884** 条记录，四向捕获并重新加载 NBT，方块状态均与旋转后的源结构相同。
- 每方向 240 个水平可站脚位，7 个安全点/工作站交互点可达，5 个容器为空，灯链与底座存活，完整屋顶及箱盖空间通过真实碰撞/支撑检查。
- 每方向执行占用空气格、缺吊灯链、堵安全点、拆地板、容器塞入物品五类负向探针，均成功检出后恢复。详见 `qa/native.json`、`qa/forge-gametest.log`、四份 `qa/placed-*.nbt`。

本次原生验收在本目录的 `forge/run-isolated` 全新临时世界完成。预览是 Lodestone 软件渲染；Forge 验收是无界面隔离运行。完整整合包客户端、多人、FPS、从正式大厅走到房间的连接和运行时幂等性属于接收与接入阶段，未在本交付宣称完成。接收线程仍须保留自己的独立验收结论。

## 复现

Minecraft Builder 技能目录：`C:/Users/YOUR_USERNAME/.codex/skills/minecraft-builder-skill`。在该目录执行：

```powershell
node scripts/run-build.mjs 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-A04/source/recipe.mjs' --out-dir 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-A04' --data-version 3465
node scripts/preview.mjs --nbt 'D:/ft工程文件/FT地牢_Image25接入_20261101/deliveries/zone-A04/room.nbt' --port 5284 --no-open
```

在本交付目录执行静态检查、视图捕获、拼图和封装：

```powershell
node source/qa-static.mjs
node source/render-views.mjs
& 'C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe' -B source/make-sheets.py
& './source/run-native.ps1'
node source/finalize.mjs
```

原生重放脚本会使用新的带时间戳隔离目录，避免复用已有测试世界。原生工程仅是 QA 夹具，不应安装到整合包。清单 `SHA256SUMS.txt` 包含四件套、源文件和证据的完整哈希。

## 接入条件

`room.json` 的所有坐标为模板原点相对的玩家脚部坐标。运行时旋转须同步旋转端口与原生方块状态。首次施工前应检查整个 19×11×19 体积（包括空气格）、边界和实体，发现占用则拒绝；不得用本模板覆盖玩家改动。按独立 ID 保存持久生成指纹，重复入场不重建。唯一运行时槽位与大厅路线由统一接入线程处理，本交付不预占核心 ID，也未部署。
