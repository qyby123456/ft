# FT 地牢商店 H · 沼泽女巫药铺

**独立制作、静态检查和 Forge 原生四向放置已通过；尚未接入、未部署。** 只制作 H，不修改其他房间、共享 incoming、正式存档、已部署 JAR 或 FT 核心源码。没有调用付费生图，也没有启动玩家客户端。

## 四件套

| 文件 | 用途 |
| --- | --- |
| `out/room.nbt` | gzip 单调色板 Minecraft Java 1.20.1 结构，DataVersion 3465 |
| `out/room.json` | 暂定 ID、尺寸、北入口、安全内点、指纹及转译说明 |
| `out/preview.png` | 从最终 NBT 读取的主视图，非概念图和客户端截图 |
| `out/qa.json` | 关联全部证据的最终检查报告 |

另有 `out/blueprint.json`、`out/materials.json`、四向剖视、平面图、完整屋顶外观与原生回采预览；源脚本与隔离 Forge 测试工程一并保留。交付 ZIP 位于 `release/`，不含运行 JAR、Gradle 缓存或测试世界。

![实际结构软件预览](out/preview.png)

## 接口与布局

- 暂定 ID：`SHOP_H_SWAMP_WITCH`，不是已占用的正式 ID/槽位；由统一接入线程分配。
- 尺寸 XYZ：`15×10×15`；北入口玩家脚位 `(7,1,0)`，安全内点 `(7,1,3)`，朝北。
- 北开口为 X=6…8、Y=1…3；主通道三格宽三格高。柜台 X=5…9、Y=1…2、Z=8；柜台四周至少一格走道，原生 BFS 每方向确认 137 个安全脚位。
- 四根独立苔石柱中心 XZ：`(3,4)/(11,4)/(3,10)/(11,10)`，Y=1…8 落地到顶；各柱八邻域 Y=1…7 留空，不与家具嵌套。
- 完整地板与屋顶均交付。剖切只在渲染内存中移除屋顶与近侧外墙，保留柱、梁和家具；主图用低角度显示台面，不用删除部件掩盖问题。

## 设计来源及安全转译

来源目录 `D:/ft工程文件/image2.5/dungeon-shop-variants-thread3-20261102`，H 原图 SHA-256：

`0c7a6641ef53e737e675dbedbcaf2f3c88f4afc475eabf5de38fb3ba9718fc20`

交付副本 `reference/shop-variant-H-swamp-witch-apothecary.png` 可读取，1536×1024；`reference/input-fingerprints.json` 指向原 README、manifest、提示词和设计 QA。实际制作扩大空间，以满足独立柱、靠墙陈设和柜台环通，不将 AI 图的近似坐标视作严格布局。

苔石、泥砖、深色木梁、壁挂藤蔓、后墙草药、蘑菇标本、绿玻璃瓶罐意象、双吊笼、双铁链灯笼、角落炼药锅、睡莲水盆及芦苇已转译为原版合法方块。吊笼为 3×3 底板/顶盖/铁栏围合，中心菌光体是固定方块，不生成生物。角锅使用满水炼药锅，青翠蛙明灯替代裸火和不存在的绿色流体。扫帚、天平和金币不使用可领取掉落物堆砌；原版灯笼仍为琥珀光，不冒称彩色光、雾或粒子。

9 个方块实体包括 4 个空木桶、3 个空酿造台、1 个无书讲台和1块封蜡告示牌；无 Items、战利品表、可取书、药水、燃料或实体库存。绿色标本是玻璃/盆栽，书架是普通方块；没有新增交易、奖励、刷怪逻辑。这里的“零库存”不承诺玩家在允许破坏方块时永远得不到建筑材料，也不添加新的权限保护。

一格水源 `(12,1,1)` 有完整底和四边；睡莲在水上，甘蔗下方为邻水草方块；炼药锅里的水不是世界流体。水盆不占主路，无熔岩和裸火。

## 验证证据

- `qa/static.json`：2250 条完整体积记录、1135 非空气；合法支持、屋顶地板、空库存/燃料、柱环与柜台环、3×3入口、封闭水盆等通过。7 个破坏性负例在内存副本中正确拒绝，重建指纹一致。
- `qa/native.json`：Minecraft 1.20.1 / Forge 47.4.10 / Java 17.0.8；原生注册表、全部状态属性、方块实体解码、StructureTemplate 往返、四向 `placeInWorld`、真实碰撞/BFS、`canSurvive`、库存和水边界均通过。100 tick 后复核 9000 条记录，四向每向 9 个空方块实体、137 个连通脚位、零意外实体。
- 原生负例：四向分别移除灯链、移除笼链、堵通道均被拒绝；注入玻璃瓶的库存负例被拒绝。仅修改隔离夹具及内存，不修改最终模板。
- `qa/native-capture-comparison.json`：四向世界回采 NBT 各2250条与最终源逐格对照，方块名及旋转属性完全一致。`qa/forge-gametest.log` 含 `SWAMP_WITCH_NATIVE_GAME_TEST_PASS`、`All 1 required tests passed` 和 `BUILD SUCCESSFUL`。
- `qa/visual-review.json`、`qa/render-manifest.json`：已打开核对四向、主图、平面、屋顶及世界回采预览；软件渲染采用近似植物/水色、简化无字告示牌，不模拟真实光照，不冒充游戏截图。

首次原生调用因测试资源遗漏 `pack.mcmeta` 而未加载空模板，未完成房间验证；已补齐并复测通过。原始失败日志在 `qa/previous-*-forge-gametest.log`，保留而不删证据。

测试服只位于本工程 `qa/forge-run`，随机生长 tick 关闭以固定方块状态，计划水 tick 开启并主动调度；不据此宣称藤蔓/甘蔗在正常长期游玩不会生长。原生测试日志有一次瞬时 `Can't keep up`，不据此宣称性能/FPS通过。尚无完整整合包客户端、多人、实际玩家视角、功能交易验收；FT 大厅道路、占用拒绝、持久化、幂等及部署回归由统一接入方负责。

## 重建

本机 Python：`C:/Users/YOUR_USERNAME/.cache/codex-runtimes/codex-primary-runtime/dependencies/python/python.exe`，需要 Pillow。只读复用本机 vanilla 1.20.1 客户端资源作渲染，不随包重分发。NBT 编解码器、渲染基础和原生测试框架从已有独立补给商店工程复制后在 H 目录内改造，未修改来源工程。

在本目录使用上述 Python 依次执行：

```powershell
python tools/build_room.py
python tools/qa_static.py
python tools/run_native.py
python tools/render_room.py
python tools/finalize_delivery.py
```

`run_native.py` 使用 Java 17、缓存 Gradle 8.8、ForgeGradle 6.0.54，默认离线，无界面、无玩家客户端；全局编译缓存会由 Gradle 自身管理。机器迁移时可传 `--java-home`/`--gradle` 并调整渲染客户端路径，缺少依赖不能当作通过。结构变更后旧视觉检查指纹会阻止打包，必须重新打开核对并更新视觉报告。

## 接入与回滚边界

详见 `HANDOFF.md`。本候选没有正式槽位、没有写共享 incoming，也没有改 JAR 或世界。接入方须复核哈希、分配唯一 ID/槽、保持原入口返程与五档难度，并验收完整体积/液体/实体占用拒绝、真实大厅连路、持久化、幂等与部署回归。不得将本测试夹具的清空代码用于正式施工。

当前回滚仅撤回这个独立未部署候选；后续接入若需回滚，应先停服并备份 JAR、配置、世界及地牢 SavedData，不能靠删模板/JAR拆除建筑，不能盲删生成标记。
