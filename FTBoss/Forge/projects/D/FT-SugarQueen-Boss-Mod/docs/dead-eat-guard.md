# 死亡瞬间停止进食 —— solcarrot「死吃」崩溃护栏（2026-09-19 v159）

## 一、修的是什么崩溃

| 项 | 值 |
|---|---|
| 崩报 | `versions\FarmingTales_ForgeⅡ v1.5.2_beta3\crash-reports\crash-2026-09-19_08.08.18-server.txt` |
| Description | `Ticking player` |
| 异常 | `com.cazsius.solcarrot.tracking.FoodList$FoodListNotFoundException: Player must have food capability attached, but none was found.` |
| 自报 Suspected Mod | **Spice of Life: Carrot Edition (solcarrot) 1.15.1**（不是本模组） |
| 触发链 | `LivingEntity.tick → updateUsingItem → completeUsingItem → eat → ForgeEventFactory.onItemUseFinish` → 事件派发 `LivingEntityUseItemEvent.Finish` → solcarrot `FoodTracker.onFoodEaten` |
| 铁证 | 崩报 `-- Player being ticked --` 段落 NBT：`Health:0.0f` + `DeathTime:27s`（死亡后第 27 tick）——**在死人身上把这口饭吃完了** |

经过：玩家 08:08:16.579 被「潮宴鱼王·沧鳞」打死，嘴里那口**金苹果**还剩 ~27 tick，
死亡动画期间数完 → solcarrot 去取"食物清单"能力（`FoodList.get`，`LazyOptional.orElseThrow`）
时该能力对死人已失效 → 异常被 Forge 事件总线**原样抛出** → 集成服务器崩溃（08:08:19 存档退出）。

历史同类崩溃（主实例 `versions\FarmingTales Forge\crash-reports\`）：

- `crash-2026-09-12_23.39.06-server.txt`：同异常、同 `-- Player being ticked --`、`Health:0.0f` + `DeathTime:29s` + 附魔金苹果；
- `2026-09-10-3.log.gz`、`2026-09-12-3.log.gz` 里各一次同样的 `FoodListNotFoundException`。

→ **老问题**，与鱼王改版无关；只是新节奏让"死亡时嘴里有东西"的概率变高，更容易撞上。

## 二、社区修复做了什么（我们照抄它的语义，但不引第三方 mod）

社区 mod「Solcarrot Fix / 死吃修复」的两个 mixin（已核对源码）：

```java
// 1) LivingEntityMixin：@Inject(method="eat", at=@At("HEAD"), cancellable=true)
if (player instanceof Player p && (!p.isAlive() || p.isDeadOrDying())) { cir.setReturnValue(itemStack); cir.cancel(); }
// 2) FoodTrackerMixin：@Inject(method="onFoodEaten", at=@At("HEAD"), cancellable=true, remap=false)
if (!(event.getEntity() instanceof Player p)) return;
if (!p.isAlive() || p.isDeadOrDying()) ci.cancel();
```

一句话：**死人别吃饭 / 死人别记账**。

## 三、我们的等价实现（`BossHandler.onDeadPlayerStopEating`）

```java
@SubscribeEvent(priority = EventPriority.HIGHEST)
public void onDeadPlayerStopEating(TickEvent.PlayerTickEvent e) {
    if (e.phase != TickEvent.Phase.START) return;
    if (!(e.player instanceof ServerPlayer sp) || sp.level().isClientSide) return;
    if (!sp.isUsingItem()) return;
    if (sp.isAlive() && !sp.isDeadOrDying()) return;   // 活着照旧
    var stack = sp.getUseItem();
    if (!stack.isEdible()) return;                     // solcarrot 只对食物取能力
    …log…
    sp.stopUsingItem();
}
```

**为什么一定是 `Phase.START`**（已用 javap 反编译 Forge 1.20.1 核对调用点）：

```
Player#tick() offset   1 : ForgeEventFactory.onPlayerPreTick  → TickEvent.PlayerTickEvent(Phase.START)
Player#tick() offset 140 : super.tick()                       → LivingEntity.tick → updateUsingItem → completeUsingItem → eat
Player#tick() offset 393 : ForgeEventFactory.onPlayerPostTick → TickEvent.PlayerTickEvent(Phase.END)
```

START 阶段**必定早于本 tick 的"吃完"判定**，所以不存在"同一 tick 抢先吃完"的窗口。
唯一漏网情形：玩家在**自己那一 tick 内**被火烧/中毒/摔死，且同一 tick 正好吃完
（那种只有 mixin 才盖得住 —— 社区修复用的就是 mixin；本模组暂不引 mixin 框架）。

**为什么只拦可食用物品**：solcarrot 的 `onFoodEaten` 字节码在取能力**之前**就先
`if (!item.isEdible()) return;`（`Item.m_41472_`），药水/弓箭根本走不到会崩的那行；
顺带也避免了"死人在放箭"的怪现象。

**踩坑记录（勿改）**：不要改成"取消 `LivingEntityUseItemEvent.Tick`"——
`ForgeEventFactory.onItemUseTick` 在事件被取消时返回 `-1`，而 vanilla 是
`if (--this.useItemRemaining <= 0 && !level.isClientSide && !item.useOnRelease()) completeUsingItem();`，
返回 -1 会**立刻吃完**，反而必崩。

## 四、回归检查

`tools/verification/check_dead_eat_guard.cjs`（已挂进 `tools/verify_project.cjs` 的 `dead-eat-guard`）：

- 注册存在（`FTBossMod` 里 `register(new BossHandler())`）+ 优先级必须是 `HIGHEST`；
- 阶段必须是 `Phase.START`，且方法体内不许出现 `Phase.END`；
- 三个条件（死人 / 正在使用 / 可食用）都必须**排在** `stopUsingItem()` 之前，且日志也必须在停之前取物品；
- 反面：剥掉注释后的真代码里不许出现 `LivingEntityUseItemEvent.Tick`；
- 顺带钉住"飞行禁令"那条 `onPlayerTick` 仍是 `Phase.END`，别被合并。

## 五、实机验收建议

1. 打任意 Boss，**边啃金苹果边被打死**（旧版必崩的那种死法）；
2. 看日志应出现：`[FTBoss] 死亡瞬间停止进食(防 solcarrot 死吃崩溃): <玩家> 停下 minecraft:golden_apple`；
3. 死亡界面 → 重生，服务器 **不崩**、存档正常；
4. 正常吃饭/喝药/拉弓（活着的时候）行为不变。
