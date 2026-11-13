# 商店禁刷与原交易桌接入候选补丁

本目录不是已部署核心。`shop-import.patch` 只在本交付的临时快照目录做过 `git apply --check`、实际应用和逐文件回读比对；没有应用到共享 FT 核心。`source-before/` 保存取样时原文件，`source-after/` 是候选结果，指纹见 `patch-manifest.json`。若共享核心已变化，接收方应按方法合并，不能覆盖整份旧文件。

## 必需改动

1. `DungeonRoomCatalog.spawnPointsEnabled` 读取房间 JSON：`room_type="shop"` 始终返回 false；其他房间 `spawn_points=false` 退出布点；未设置字段的既有房间仍保持原行为。类型错误抛出异常，不把字符串 `"false"` 当成布尔值。
2. `DungeonSpawnPoints.catalog` 在构造商店 Area 前跳过禁刷房间，所以不为商店创建本轮刷怪点；tick 还跳过不在当前 catalog 的旧房间记录。catalog 有本轮缓存，**接收后须重启或开启新一轮**，不宣称支持元数据热重载。
3. `DungeonRoomLayout.validate` 精确放行 `ftdungeontrading:trading_table`，不放行整个模组命名空间。要求两格配对、同朝向、有地板、未绑定且库存为 0，继续禁止奖励库存、LootTable、刷怪笼和命令方块等。

当前未修补核心会默认给每间已登记房间布点，并拒绝非原版结构方块。**仅复制 room.json 中的禁刷字段无效，也不能将本候选标记为可直接导入未修补核心。**

## 接收顺序

1. 由核心维护者在当前源码核对补丁，保留其正在进行的九间房修正与其它变更。`before_sha256` 不同就重新对照合并，不用快照整文件覆盖。
2. 客户端与服务端一致提供 `ft-dungeon-trading-forge-1.20.1-1.2.0.jar`；本包 `dependencies/` 是原有 1.2.0 构建，未改模型、贴图或交易逻辑。1.1.x 单格桌不兼容本结构。
3. 给暂定 `SHOP_SUPPLY_DSH` 分配正式身份/槽位后，按核心现有格式登记结构与 JSON，更新结构资源路径和完整 SHA-256；不能把测试资源中的槽位 9 当成已预留。
4. 完整回归核心编译、catalog、画廊通道与幂等、存档恢复、普通战斗房刷怪及商店无 encounter 点。隔离 QA 不替代这些接入验收。不得把 `src/compat` 类或 QA Mod 再装到正式核心旁边，避免重复类。
5. 使用隔离测试世界验证后，再依项目部署流程批准安装；本任务没有安装依赖、替换核心 JAR 或改正式存档。

桌子放在局部 `(4,1,6)` / `(5,1,6)`，朝北，左主右副，共享原 `TradeTableAccess`。模板保留 `OfferId=ftdungeontrading:unbound`、`RemainingStock=0`，未擅自设置商品/价格。应由服主按原交易模组流程配置；本房间交付不发免费物品。

“不接入刷怪点”指 FT encounter 系统，不是禁止一切原版自然生成、其它模组刷怪或强制清除已有实体。若需要绝对安全区，应另行由核心设计安全区规则。

## 已有证据及边界

- 候选 Catalog / Layout 与真实交易 1.2.0 在隔离 Forge GameTest 中编译运行：5 组禁刷策略、正确结构放行、不完整桌子和非零库存拒绝。
- 四方向完整放置，100 tick 后每向 968 记录和 11 方块实体通过；两半配对、共享 API、未绑定/零库存，63 个可达脚点。
- `DungeonSpawnPoints` 的完整运行时接线没有在本工程编译或执行，须由核心维护者补完整项目回归。补丁未应用，不宣称正式游戏已禁刷。
