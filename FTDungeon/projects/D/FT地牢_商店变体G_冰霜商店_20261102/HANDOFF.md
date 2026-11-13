# G 冰霜商店 · 统一接入交接

接收负责人：统一接入聊天 `01a19758-b0e5-75c2-822a-4860527a1e8b`。本文件是可直接读取的候选报告，未自行写入接入目录或注册正式槽位。**制作、静态和四向 Forge 原生 QA 已通过，未接入或部署。**

- 独立根目录：`D:\ft工程文件\FT地牢_商店变体G_冰霜商店_20261102`。
- 四件套目录：`D:\ft工程文件\FT地牢_商店变体G_冰霜商店_20261102\out`，仅本变体 G。
- 原图：`D:\ft工程文件\image2.5\dungeon-shop-variants-thread3-20261102\out\shop-variant-G-frost-shop.png`，SHA-256 `defa7b24b65ee89ec5db3d7575826363f295da10b0b34a397c8a606888af2df1`。
- 暂定身份 `SHOP_G_FROST`（可由协调者改名，未占正式槽），XYZ 17×10×15；北入口 `(8,1,0)`，内点 `(8,1,3)`，3×3 北侧开口。
- NBT SHA-256：`7d837a3959cb7656e28883c5b238352e682c06ddd705abe60ca2ef36a4917150`，与 `out/room.json.sha256`、`out/qa.json.structure.sha256` 和 `qa/package-verification.json.quartet` 保持一致。
- 静态 QA：20 项；原生 Forge 47.4.10 四向放置、原生碰撞/支撑/空库存、邻居更新、100 tick 稳定性、每向完整回采核对与两种反例探针。最终是否通过以 `out/qa.json` / `qa/native.json` 的真实最终状态为准。
- 核心结构：独立四根冰柱全高一格八邻域净空；柜台四面通行；落地双玻璃展柜；贴墙货架与斧形浮雕；两盏三节链悬冷光灯；7 组有屋顶支撑的冰锥。地板无真实水或粉雪，所有要求走点均为非滑冰表面。
- 无预置实体、奖励物品、战利品表或真实交易功能。没有修改其它房间/核心/正式存档/已部署 JAR，也没有代做部署。
- 设计适配：为柱周与展柜扩大尺寸；原版缺失的小商品以合法方块雕塑表达，详情见 README 和 `room.json.design_adaptations`。
- 证据图：`preview/four-views.png`、`preview/exterior.png`、`preview/plan.png`、`preview/native-world-capture.png`；最后一张同样是世界回采数据的软件渲染，不冒充客户端实拍。
- 交付包：`release/FT_Dungeon_Shop_G_Frost_Standalone_20261102.zip`，包指纹见 `qa/package-verification.json`。不包含隔离测试世界和构建缓存。

接入时请保留其它线程 ID/槽位和用户修改；按 FT 契约先审核目标整个体积、占用拒绝、路径连接与幂等，再由唯一负责人构建和部署。本线程不绕过统一验收。
