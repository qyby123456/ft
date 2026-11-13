# Java 接入接口与维护说明

公开接口包：`net.farmingtales.ftdungeontrading.api`。本版 API 面向 Forge 1.20.1、Java 17，不承诺跨大版本二进制兼容。开发环境应依赖本项目并由 ForgeGradle 重映射；不要将整个本模组或 `net.minecraft` 类再打进自己的 JAR。

## 职责划分

| 类型 | 职责 |
| --- | --- |
| `TradeCost` | 不可变付款条件：Ingredient、数量、精确 NBT / 忽略 NBT 策略 |
| `TradeOffer` | 不可变报价：ID、标题、双方条目；传入/返回物品、标题和 NBT 防御性复制 |
| `TradeContext` | 服务端世界、不可变方块坐标、ServerPlayer |
| `DungeonTradingApi` | 注册/移除代码报价、查询报价、绑定桌子、读取桌子快照、交互入口 |
| `TradeResult` | `status()` 和可显示的 `message()`，不靠字符串判断是否成功 |
| `TradeEvent.BeforePurchase` | Forge 总线的可取消事件，真实提交之前触发 |
| `TradeEvent.Purchased` | 提交成功后的通知，带该次成交后的剩余库存，不可取消 |

不要调用核心类私有状态或直接修改方块实体 NBT 来改价格。业务扩展依赖 API 包；客户端展示由本模组负责，无需扩展自行发送物品同步包。

## 注册并绑定

最小完整代码示例见 `examples/java/DungeonShopIntegration.java`，使用官方映射。报价可在合适的服务端初始化阶段注册；不要在物品注册完成前解析物品，也不要在静态初始化里访问未就绪的 RegistryObject。

```java
ResourceLocation offerId = new ResourceLocation("myaddon", "dungeon_supplies");
TradeOffer offer = new TradeOffer(
        offerId,
        Component.literal("冒险者补给"),
        List.of(TradeCost.item(new ItemStack(Items.EMERALD), 3)),
        List.of(new ItemStack(Items.BREAD, 6), new ItemStack(Items.TORCH, 8)));
DungeonTradingApi.registerOffer(offer);
DungeonTradingApi.bindTable(level, position, offerId, 20);
```

`TradeCost.item(example, count)` 保留例子的 NBT；未带 NBT 的例子只接受无 NBT 物品。更一般的付款使用 `new TradeCost(ingredient, count, exactNbt, allowAnyNbt)`；Ingredient 通过序列化往返复制，适合原版可序列化 Ingredient 和标签。第三方自定义 Ingredient 必须实现正确的 Forge JSON 序列化；该兼容性不在本版测试保证中。

`registerOffer` 替换同 ID 的代码报价；`removeCodeOffer` 只移除代码层，不会删除数据包内容。代码注册不写存档：在每次启动时注册。数据包同 ID 优先。报价列表 `offerIds()` 为排序后的有效 ID 列表，`offer(id)` 返回 Optional。

`bindTable(level, position, offerId, stock)` 要求报价存在、区块已加载且坐标确实为交易桌；否则抛 `IllegalArgumentException`。库存范围 `-1..1000000`，每次绑定使旧确认失效。`table(level, position)` 返回 Optional 的不可变 `TableState(offerId, remainingStock, changeVersion)`，不会强加载区块。

## 双格桌的坐标与生命周期

1.2.0 的交易桌占两格宽、一格深。玩家使用一个桌子物品放下完整左右两半；另一格不可放置时整次操作失败，不扣物品。两半都不可被活塞推动。拆除任意一半会清理整桌，生存只掉一个桌子物品，创造不掉落。

公开方法签名不变。bindTable 和 table 均可传入任一半的坐标，但两半必须已加载、朝向一致且结构完整；残缺桌不可绑定，查询为空，也不可成交。两半共用主半的报价、库存和 changeVersion，仅主半创建方块实体。命令也接受任意半坐标。不要依赖两半分别持有 NBT。

interact 接受任一半坐标；验证点击坐标后规范化到主半，因此可在右半预览、左半确认而不产生两笔报价。BeforePurchase 与 Purchased 中的 TradeContext.position 始终是主半坐标；依赖坐标做权限或区域判断的扩展应按整个两格占地判断，而不是只检查事件坐标。

交互入口先检查玩家与传入的点击半格中心距离不超过六格；两格都必须通过原版 mayInteract。确认提交前再次验证结构完整性、两格权限以及玩家仍在桌旁六格内。直接调用 API 仍不自动触发第三方 RightClickBlock 领地保护。

旧存档的单格桌不会自动扩展，以免覆盖邻格。升级前备份并记录绑定和库存；拆除后重新摆放完整桌，再通过命令或 API 重新绑定。结构生成器必须放置朝向相同、part 分别为 left / right 的两个方块，不能只设置主半。

## 线程、权限与交互

绑定桌子、查询桌子、调用 `interact` 必须在所属服务器线程；异步入口应通过 `server.execute(...)` 调度，并在回调内重新校验玩家/区块/权限。报价注册也建议在服务器线程完成，避免应用层在监听器里并发换价。

`DungeonTradingApi.interact(new TradeContext(level, position, player))` 与方块右键复用同一套状态机：第一次返回 `PREVIEW`，满足间隔的第二次才可能 `PURCHASED`。它不是“免费直接发货”入口；调用者可自行展示 `result.message()`，不要重复发送成功物品。

交互检查玩家存活、非旁观、维度一致、距离不超过 6 格、区块加载和原版 `mayInteract`。管理员 `/fttrade` 命令统一要求权限等级 2；**Java API 不代替扩展自己的管理员/领地权限检查**。正常方块右键遵循 Forge 交互事件链；直接调用 API 并不自动触发第三方 RightClickBlock 保护，需要扩展自行校验。

状态包括 `PREVIEW`、`PURCHASED`、`TOO_SOON`、`INSUFFICIENT_PAYMENT`、`INVENTORY_FULL`、`SOLD_OUT`、`MISSING_OFFER`、`QUOTE_CHANGED`、`UNAVAILABLE`、`DENIED`、`CANCELED`。失败后应重新预览；`TOO_SOON` 保留原确认时间，不能通过刷包缩短间隔。

## 扩展规则与难度挂钩

在 `MinecraftForge.EVENT_BUS` 注册 `Consumer<TradeEvent.BeforePurchase>`。事件只在第二次确认且基础检查通过后发出，但此时还没提交付款，因此**不能当成支付成功通知**。例如读取已有 FT 难度 API，不满足级别时 `event.setCanceled(true)`；同样适用于队伍、任务进度、会员或领地规则。当前没有硬依赖 FT 难度模组，不预设其类名或捏造集成。

监听器不要扣款、发奖或修改玩家背包；取消只保证本模组不结算，不能回滚监听器自己做出的副作用。本版只内建物品换物品，没有外部余额扣款事务协议。若要接经济余额/卖家容器，应先设计可预留、提交、回滚的适配层，不能简单在 `BeforePurchase` 扣钱后声称跨系统原子交易。

`Purchased` 适合记账、成就、统计。它在结算后发出，监听器异常只记录日志，不回滚已经发放的商品，禁止在这里再发同一份奖励。事件必须保持短时间、同步执行；网络/磁盘工作在取得必要不可变数据后移到异步处理。

## 核心代码阅读顺序

1. `TradeOfferCodec`：严格数据格式与参数上限。
2. `TradeOfferRepository`：数据包/代码两层报价与内容版本。
3. `TradeTableBlockEntity`、`TradeTableAccess`：每桌报价、库存、持久化和最多六个同步预览。
4. `TradeService`：报价确认、防重入、事件与提交时序。
5. `InventoryTransaction`：37 个付款槽的快照、最大流匹配、模拟发奖、一次提交。
6. `TradeTableBlock`、`TradeCommands`：交互/管理员入口。
7. `client/TradingTableRenderer`：两侧商品与标牌，服务端不加载此类。

确认绑定玩家 UUID、维度、方块实体实例 UUID、桌子版本、全局报价版本和完整报价签名。重载/换价/改库存使旧确认失效；一次成功的无限库存交易也会递增桌子版本，其他玩家须重新核价。登出/停服清理待确认状态，换桌或重新加载实例不能沿用旧确认。

结算只处理玩家主背包与副手的内存 ItemStack，不调用外部 ItemHandler。提交前再次对比快照；成功后再减库存并广播背包。此处“全部成功或不提交”针对同步服务端内存事务，不是跨数据库 ACID，也不保证进程在两次世界/玩家保存之间崩溃时的磁盘级原子性。
