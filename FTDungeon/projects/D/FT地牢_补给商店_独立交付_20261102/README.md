# FT 地牢补给商店 · 原交易桌 / 不接入刷怪点

**独立候选 v2：已放入原有两格地牢交易桌，房间标记为商店并禁用 FT 刷怪点；附真实核心接线候选补丁，尚未接入/部署共享核心或正式存档。**

本任务只写本交付目录，不改正在进行的九间房修正、第三批 M5/M6/M7/S5/S6/S7 队列、共享 FT 核心、运行实例 mods 或正式世界。没有启动玩家客户端，没有再次生图或改 API 配置。原 v1 文件保存在本地 `baseline-v1/`，旧 ZIP 保留，v2 用新文件名交付。

## 本次修改

- 把普通五格木柜台替换为之前制作的 **`ftdungeontrading:trading_table` 1.2.0**，不是相似外观的装饰方块。使用原 JAR、原模型、原贴图和原交互 API，不修改交易模组。
- 主半格 `(4,1,6)`、副半格 `(5,1,6)`，朝北，`part=left/right`，只有左主格含方块实体；两格都通过原 API 定位同一主桌。
- 模板 `OfferId=ftdungeontrading:unbound`、`RemainingStock=0`，未绑定商品或价格，不生成免费物品。交易配置留给服主，不在结构任务里擅自设置经济。
- `room_type="shop"`、`spawn_points=false`。候选核心规则使商店在建立 encounter Area 前被排除，连误填 `spawn_points=true` 的商店也不布点。
- 原核心默认给已登记房间布点，且只允许原版方块，因此**不能只复制 JSON 就宣称禁刷生效**。本包 `integration/shop-import.patch` 提供必要接线与精确方块白名单，但没有越权应用到共享核心。详情见 `integration/README.md`。

## 文件入口

| 文件 | 用途 |
| --- | --- |
| `out/room.nbt` | 最终 Minecraft Java 1.20.1、DataVersion 3465、gzip 单调色板结构 |
| `out/room.json` | 尺寸、北向接口、商店禁刷标记、依赖、完整结构指纹和未部署状态 |
| `out/qa.json` | 最终静态、原生、视觉、可重建及候选补丁证据汇总 |
| `out/blueprint.json` / `out/materials.json` | 精确坐标与实际材料表 |
| `out/preview.png` / `preview/delivery-sheet.png` | 最终 NBT 软件剖视与坐标平面，不是游戏截图 |
| `preview/four-views.png` | 四向剖视，保留梁柱家具 |
| `preview/native-world-capture.png` | 真实 Forge 放置后回采 NBT 的软件预览 |
| `qa/native-placed-*.nbt` | 四向放置稳定 100 tick 后的原生回采结构 |
| `dependencies/ft-dungeon-trading-forge-1.20.1-1.2.0.jar` | 之前制作的原交易模组，同版本客户端/服务端依赖；未安装到正式实例 |
| `integration/shop-import.patch` / `integration/patch-manifest.json` | 三处核心候选改动、前后指纹和快照应用验证 |
| `src/` / `tools/` | 独立 Forge QA 源码与生成、渲染、校验脚本，不作为正式 Mod 安装 |

交付包：`release/FT_Dungeon_Supply_Shop_Trading_NoSpawns_20261102.zip`。含指定交易依赖，不含 Minecraft/Forge 客户端资源包、QA Mod JAR、缓存或任何测试/正式世界。

## 房间与接口

- 暂定 ID `SHOP_SUPPLY_DSH`，**未由协调人正式分配**；`assigned_slot=null`，没有登记到共享 catalog。隔离测试里使用槽位 9 只是测试数据，不代表预留。
- 尺寸 **11×8×11（XYZ）**，内部平面 9×9；Y=0 完整实心地板，Y=7 完整屋顶。入口脚部 `(5,1,0)`，安全内点 `(5,1,3)`，朝北。
- 北开口 X=4…6、Y=1…3；入口至桌前 X=4…6、Y=1…3、Z=0…5 全为空气。交易桌四周至少一格通行净空，可绕至后侧空箱。
- 四根建筑柱只在墙线四角，Y=1…6；柜桌/货架不穿柱。货架只沿西、东、南墙一格进深，四只空桶只在室内四角。
- 两只吊灯位于 `(4,4,3)` / `(6,4,3)`，上接 Y=5 灯链和 Y=6 承重梁，投影在进门通道，不压桌子。陈设有实体上表面支撑。
- 无模板生物、刷怪笼、命令/结构/拼图方块，无液体、火方块、奖励库存或 LootTable。三只头颅是装饰方块，不是预置实体。
- “不接入刷怪点”是排除 FT encounter 系统，**不是**禁止一切原版自然生成、其它模组刷怪或清除已有生物；绝对安全区是另一个核心功能。

## 已执行校验

1. **25 项静态检查通过**：全体 968 坐标、单调色板、接口、空库存、屋顶/地板、梁柱与家具、54 格主通道净空、桌子环路、灯链、原依赖指纹和禁刷字段。
2. **真实隔离 Forge GameTestServer**：Java 17.0.8、Minecraft 1.20.1、Forge 47.4.10、原交易模组 1.2.0。实际 `StructureTemplate.load/placeInWorld/fillFromWorld`，不是只解码 NBT 或 DataGen。
3. 四方向放置并等待 100 tick，每向复核 968 条方块记录、11 个方块实体、63 个可达脚点、碰撞、存活、空库存及两半桌的原共享 API；总计 **3,872** 条稳定状态，无额外世界实体。原结构与无旋转回采的方块状态逐项相同。
4. 候选核心 Catalog / Layout 在同一 Forge 测试里编译运行：5 组禁刷策略；完整原桌放行；缺少半桌、预载非零库存各被拒绝。完整 `DungeonSpawnPoints` 接线只做快照补丁应用验证，**没有编译/运行完整 FT 核心**。
5. 静态反例 6 组（堵主路、缺地板、缺屋顶、断灯链、缺半桌、预载库存）必须触发指定失败项；原生另有四向断支撑和四向堵通道反例。
6. 可重复构建指纹、Python 语法、PNG 可解码、人工预览、ZIP CRC、逐文件 SHA-256、实际打包源与测试源一致性均由收尾脚本验证。

尚未完成的接收方工作：对当前核心重新合并补丁，完整核心构建与 encounter 回归、正式槽位/catalog、画廊通道与幂等、客户端视觉/交易实测，以及经批准后的部署。**本包 QA 的 PASS 不代表已部署，也不代表完整整合包实测。**

## 关键指纹

| 项目 | SHA-256 |
| --- | --- |
| v2 `room.nbt` | `aaf77713506a36106e611ccd213f34baf2e765bd1756890a8e690a68ffad84a0` |
| 原交易 1.2.0 JAR | `88e248923de4c17da275ed8af294914f413ede9ef43fa1b5f0f277d8879ec383` |
| hero 1536×1024 | `f26921137e349646d08690a5964a2235636945a41d39179d48d37d5cac68a355` |
| merchant 1536×1024 | `3be525340bb4602e9dbec01e8179fb45dd4acdbb5ecd2c081bdb69d60c0cc23e` |
| layout checked 1536×1324 | `96449cd195ac4af7a88c9d4cef5b50dab898c232f8d01b81748e5a09270918b0` |

原始交接的 merchant 指纹少一个字符；布局源 manifest 的尺寸与指纹过期。权威记录是 `reference/input-fingerprints.json`，原 manifest 保持原样作为历史证据。最终 ZIP 指纹见本地 `qa/package-verification.json`。

## 重建与接收

Python 3.10+、Pillow；隔离原生测试需要 Java 17、Gradle 8.8 和已有 Forge 开发依赖缓存。渲染还读取本机 1.20.1 客户端 JAR 的原版模型/贴图，该受版权保护资源不打入包。

```powershell
python tools/build_room.py
python tools/qa_static.py
python tools/run_native.py
python tools/render_room.py
python tools/build_integration_patch.py
python tools/finalize_delivery.py
```

预览复核后才可运行收尾；源或原生测试输入发生变化必须重测。客户端和服务器要一致使用 1.2.0 两格桌，1.1.x 单格模型不适用。接入前先读 `integration/README.md`，不要将 `src/compat` 的快照或 QA Mod 装到共享核心旁边；不要用旧快照覆盖并发修改的整个核心文件。
