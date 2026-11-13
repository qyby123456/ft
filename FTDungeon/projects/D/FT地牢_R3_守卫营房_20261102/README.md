# R3 守卫营房 · 独立交付

按已存在的 image2.5 设计图制作的 Minecraft Java 1.20.1 原版方块房间；不调用生图接口，不部署、不写玩家存档、不改 FT 核心或其他线程的交付。

## 文件与入口

- `room.nbt`：gzip、DataVersion 3465、单一调色板、完整显式体积 17×10×17，共 2,890 条方块记录。
- `room.json`：ID `R3`；北向入口脚部坐标 `(8,1,0)`，安全点 `(8,1,3)`；三个入口脚位均有完整地板，入口及直行主路三宽三高。
- `preview.png`：从最终 NBT 回读的软件材质预览；`preview/four-views.png` 为四面剖切，`preview/exterior.png` 为完整屋顶外壳，`preview/plan.png` 为坐标平面。
- `qa.json`：验收总报告；`qa/static.json`、`qa/native.json`、`qa/forge-gametest.log`、四个 `qa/native-placed-*.nbt` 提供分项与世界回采证据。
- `blueprint.json`：床铺、兵器架、灯链、通道与必达点；`SHA256SUMS.txt` 包含交付和证据文件的完整哈希。

## 参考与落地调整

原图：`D:/ft工程文件/image2.5/dungeon-rooms-medium-random-thread3-20261102/out/R3-guard-barracks.png`，1536×1024。

原图 SHA-256：`99a2051453f62c923eb60ee8dd9679f82346c1745dfbf7141c221b154dbc7a98`。

保留苔裂石砖、深色木构、两侧双层铺位、十字军旗、落地兵器架、后墙铁栏、中央议事桌与长凳、空箱桶、暖色链灯及前侧石炉。为家具环路、北侧接口和真实支撑，将概念图近似尺寸调整为 **17×10×17**；不宣称逐像素等比例复刻。

**床铺安全调整**：只读核对发现 FT 维度 `bed_works=false`，现有规则仍开放方块右键。原版床在此类维度可能爆炸，因此最终八组上下铺采用木平台、红地毯床褥与白地毯枕头，**没有任何可交互睡床或重生锚**，不依赖核心新增拦截。所有铺位都有实心承重面，双层床架木柱从地面连续上升；梯子有连续背靠及上层落脚平台。

兵器架为木栅栏、链与铁栏的无实体装饰剪影，不放可领取武器、盔甲架、物品展示框。旗帜为真实原版带图案方块实体；预览简化其几何。地图台用无书讲台表达，不塞入可领取地图或书本。箱、桶、讲台、营火均为空、无战利品表。炉火熄灭，既无火焰伤害也不引燃木构；石砖屋顶完整。

## 验收口径

最终以与 `room.json.sha256` 相同的 `qa.json.structure_sha256`、`qa/native.json.structure_sha256` 和日志通过标记为准。静态检查包括命名空间、调色板、完整体积、完整地板/屋顶、入口、往返路径、家具环路、床铺/灯具/兵器架支撑、空库存、无危险流体及无爆炸睡床。

隔离 Forge 47.4.10 / Minecraft 1.20.1 / Java 17 GameTest 真正调用原生结构放置，检查 0°/90°/180°/270°，每向稳定 **200 tick** 后逐块复核，共 **11,560** 条；检查原生注册表与完整属性、碰撞、`canSurvive`、方块实体、库存、掉落/实体、八组床铺、两座兵器架、四组链灯和完整屋顶，并导出四向世界回采 NBT 再加载逐块对照。每向另测缺灯链、缺床平台、缺架底与堵路四种反例，共 16 个反例探针。

测试夹具固定在**隔离主世界 Y=288**，避免默认随机地形向开门灌水。早期环境干扰失败报告与日志保留为 `qa/previous-*`；较早的原版床候选通过也不计入最终验收，最终必须使用安全装饰床铺版本的哈希。未删除失败历史或放宽状态漂移断言。

**证据限制**：不是正式 FT 维度接入、完整整合包客户端、多人或 FPS 验收；软件图不是游戏截图，不模拟精确光照。剖切只在渲染内存隐藏屋顶/顶梁和近侧墙；最终 NBT 不拆顶。未测试或部署任何正式大厅道路、槽位、幂等、占用拒绝、返程门或刷怪奖励逻辑。

## 复现

Python 需 Pillow；本机可用 `C:/Users/YOUR_USERNAME/.codex/.venvs/imagegen/Scripts/python.exe`。脚本不调用图像 API。按顺序运行：

```powershell
python tools/build_room.py
python tools/qa_static.py
python tools/run_native.py
python tools/render_room.py
python tools/finalize_delivery.py
```

`run_native.py` 使用本机 Gradle 8.8、Minecraft 目录自带 Java 17 与缓存的 Forge 47.4.10，以离线模式运行，工作目录仅为本工程 `qa/forge-run`。构建夹具不是内容模组，不能部署。重建 NBT 后必须重跑原生和渲染再汇总；汇总脚本拒绝过期证据。

遵循只读接入契约：`D:/ft工程文件/FT地牢_Image25接入_20261101/CONTRACT.md`。唯一核心负责人后续分配新 ID/槽位并接路、完整预检目标体积、保护既有生成标记及玩家改建；本工程不写共享 `incoming/`、catalog、queue 或正式 `mods/`。
