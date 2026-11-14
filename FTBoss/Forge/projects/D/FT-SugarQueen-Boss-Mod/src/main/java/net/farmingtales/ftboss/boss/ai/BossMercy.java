package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.boss.SugarQueenBoss;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * 「连死怜悯」: 同一名玩家在**同一只 Boss**下连续死亡 {@value #LIMIT} 次 → 那只 Boss **自动离场**,
 * 且**不给任何掉落物与经验**(直接 {@code discard()}, 不触发死亡流程 → 不跑战利品表、不掉经验)。
 *
 * <p><b>2026-09-11 第三版(按用户反馈"计算死亡还是有问题")</b> —— 计数粒度从"Boss 种类"改成 **"Boss 实例"**:
 * <ul>
 *   <li>用户的原话是"连续在**同一个 boss** 下死亡", 而按种类累计时, 玩家打死一只再召唤一只, 旧账仍然留着 →
 *       新 Boss 一死人就突然离场。现在按**实体 UUID**记账, 换新一只就是全新计数;</li>
 *   <li>离场也只清**这一只** Boss(以及它自己的召唤物), 不再"该种类全员离场", 不会误伤玩家正在打的另一只;</li>
 *   <li>每次死亡都会打出**完整归因日志**(命中哪个分支、击杀者是谁、计数前后), 方便核对;</li>
 *   <li>计数**带时间戳**, 超过 {@link BossMercyData#STREAK_TIMEOUT} 未再死亡即过期归零(旧存档自动按过期处理);</li>
 *   <li>动作栏提示用 Boss 的**显示名**(「屠夫」而不是 ftboss:meat_mountain)。</li>
 * </ul>
 *
 * <p>归因顺序: ① 直接击杀者(投射物取主人) → ② 击杀者是召唤物 → ③ 最近 10 秒内打中过玩家、且**仍活着且在 48 格内**的 Boss
 * → ④ 32 格内最近的 Boss。都找不到就不计数(并打日志说明)。
 */
public final class BossMercy {

    /** 连死次数上限 */
    public static final int LIMIT = 5;
    /** ④ 兜底判定半径 */
    private static final double NEAR_RADIUS = 32.0;

    /** 威胁记忆有效期(tick): 10 秒 */
    private static final long THREAT_MEMORY = 200L;
    /** 玩家 UUID -> (Boss 实例 UUID/类型键 -> 最后被打中的游戏刻)
     *  <p>v112: 这张表以前**只增不减**(每个玩家 × 每个见过的 Boss 实例键一条), 长期运行会一直涨;
     *  现在每次写入顺手淘汰过期项 + 超过上限时整表清一次(记忆只有 10 秒, 清掉无害)。 */
    private static final java.util.Map<UUID, java.util.Map<String, Long>> THREATS =
            new java.util.concurrent.ConcurrentHashMap<>();
    /** 最多记这么多玩家(超了就把过期项清掉, 还不够就整表清空 —— 最坏只是丢 10 秒的威胁记忆) */
    private static final int THREAT_PLAYER_CAP = 128;

    private static void pruneThreats(long now) {
        try {
            if (THREATS.size() > THREAT_PLAYER_CAP) {
                THREATS.entrySet().removeIf(en -> {
                    en.getValue().entrySet().removeIf(t -> now - t.getValue() > THREAT_MEMORY);
                    return en.getValue().isEmpty();
                });
                if (THREATS.size() > THREAT_PLAYER_CAP) THREATS.clear();
            }
        } catch (Throwable ignored) {
        }
    }

    private BossMercy() {}

    // ================= 对外入口 =================

    /** 玩家被某个 Boss(或其召唤物)打中时调用(由 BossHandler 在该事件里转发) */
    public static void noteThreat(ServerPlayer p, Entity attacker, long gameTime) {
        try {
            Entity a = resolveOwner(attacker);
            if (a == null) return;
            String key = keyOf(a);
            if (key == null) {
                // 召唤物: 记它附近那一只 Boss
                LivingEntity boss = nearestBoss(p, 64.0);
                if (boss == null) return;
                key = keyOf(boss);
            }
            if (key == null) return;
            THREATS.computeIfAbsent(p.getUUID(), k -> new java.util.concurrent.ConcurrentHashMap<>()).put(key, gameTime);
            pruneThreats(gameTime);          // v112: 顺手淘汰过期项(这张表以前只增不减)
        } catch (Throwable ignored) {
        }
    }

    /** 玩家死亡时调用(BossHandler 的 LivingDeathEvent) */
    public static void onPlayerDeath(ServerPlayer p) {
        try {
            Attribution at = attribute(p);
            if (at == null || at.boss == null) {
                FTBossMod.LOGGER.info("[FTBoss] 连死计数: {} 死亡未归因到任何 Boss({}), 不计数", p.getName().getString(), at == null ? "?" : at.reason);
                return;
            }
            BossMercyData data = BossMercyData.get(p.serverLevel());
            long now = p.serverLevel().getGameTime();
            String key = keyOf(at.boss);
            /* ★ v170 修「屠夫连杀退场失效」：记账键从**实例 UUID** 改成**Boss 类型**。
               # 原实现（第三版）用 "i:" + uuid，注释里写的理由是"玩家打死一只再召唤一只，旧账仍留着
               # -> 新 Boss 一死人就突然离场"。但那条理由的代价是：**只要实例在两次死亡之间被换掉，
               # 计数就永远是 1/5**，怜悯永远触发不了。
               # 日志实证（latest.log 16:33）：召唤(1392) -> 死亡 1/5 -> 清场 -> 召唤(1684) -> 死亡 1/5，
               # 两次死亡各挂在不同的实例 UUID 上；499 个日志文件里屠夫的"连死怜悯触发"是 0 次。
               # 屠夫正是最容易换实例的 Boss（玩家一死它就清场，下次再召唤就是全新一只），
               # 所以它踩得最狠，用户报的也正是它。
               # 现在改用类型键，并把"击杀清零"同时作用到类型键与实例键（见 onBossDefeated），
               # 于是原设计想防的两件事仍然成立：**打死它就清零**、**超时也清零**。 */
            String counterKey = typeCounterKey(at.boss);
            String name = at.boss.getDisplayName().getString();
            /* 2026-09-13 v102 按用户要求「boss 连战死亡限制和隐藏 boss 无关」:
               甜苹果庄园那十位(隐藏 Boss / 彩蛋十人)**不参与连死怜悯** ——
               不累计、不提示"再死几次它就会离场"、更不会把它移出场地
               (庄园是"一位一位轮换 + 独立血条"的连战, 半途把当前这位抽走会直接破坏战场流程)。
               归因本身照旧(日志里仍能看到是被谁打死的), 只是不走怜悯。 */
            if (at.boss instanceof net.farmingtales.ftboss.entity.EasterBossEntity) {
                FTBossMod.LOGGER.info("[FTBoss] 连死计数: {} 死于隐藏 Boss「{}」[{}] —— 隐藏 Boss 不参与连死怜悯, 不计数",
                        p.getName().getString(), name, at.reason);
                return;
            }
            int prev = data.streak(p.getUUID(), counterKey, now);
            int n = prev + 1;
            if (n >= LIMIT) {
                data.setStreak(p.getUUID(), counterKey, 0, now);
                despawnBoss(at.boss);
                p.displayClientMessage(Component.literal(
                        "§e你在「" + name + "」下连续死亡 " + LIMIT + " 次 — 它已离场(§7无掉落、无经验§e)。重新召唤即可再来。"), false);
                FTBossMod.LOGGER.info("[FTBoss] 连死怜悯触发: {} 在「{}」({}) 下连续死亡 {} 次 [归因: {}], 该 Boss 已离场(无掉落/无经验)",
                        p.getName().getString(), name, counterKey, LIMIT, at.reason);
            } else {
                data.setStreak(p.getUUID(), counterKey, n, now);
                p.displayClientMessage(Component.literal(
                        "§7在「" + name + "」下连续死亡 §c" + n + "§7/" + LIMIT
                                + " §7(再死 §c" + (LIMIT - n) + "§7 次它就会离场; §8超过 10 分钟未再死亡会自动重置§7)"), true);
                FTBossMod.LOGGER.info("[FTBoss] 连死计数: {} 在「{}」({} ← 实例={}) 下死亡 {}/{} [归因: {}{}]",
                        p.getName().getString(), name, counterKey, key, n, LIMIT, at.reason,
                        prev == 0 ? ", 无旧计数/已过期" : "");
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] 连死怜悯结算异常: {}", t.toString());
        }
    }

    /** Boss 被击杀(真死)时调用: 清掉附近玩家的该实例计数(以及该类型的兼容键) */
    public static void onBossDefeated(LivingEntity boss) {
        if (DoraPursuitClone.isClone(boss)) return;
        // ===== 2026-09-12 按用户要求: **每个 Boss 都掉落自己的石像** =====
        giveStatue(boss);
        giveWeaponDrop(boss);
        /* ★ v172 按用户要求「战胜 boss 的成就」：非庄园 Boss 被真身击杀 -> 给 96 格内的玩家发进度。
           放在这里是因为本方法已经是"Boss 真死"的唯一中央钩子（掉落也走这里），
           而且下面恰好就要取同一份 96 格玩家名单，不必再扫一次世界。 */
        net.farmingtales.ftboss.boss.BossAdvancements.onBossDefeated(boss);
        try {
            if (!(boss.level() instanceof ServerLevel sl)) return;
            String key = keyOf(boss);
            if (key == null) return;
            /* v170: 连死计数现在按**类型**记账，所以击杀时要把类型键与实例键**都**清掉 ——
               类型键对应"打死它了，重新来过"，实例键是旧格式残留（顺便清干净）。 */
            String counterKey = typeCounterKey(boss);
            BossMercyData data = BossMercyData.get(sl);
            long now = sl.getGameTime();
            List<ServerPlayer> players = sl.getEntitiesOfClass(ServerPlayer.class, boss.getBoundingBox().inflate(96));
            for (ServerPlayer p : players) {
                data.setStreak(p.getUUID(), key, 0, now);
                if (counterKey != null) data.setStreak(p.getUUID(), counterKey, 0, now);
            }
            FTBossMod.LOGGER.info("[FTBoss] 连死计数已清零(Boss「{}」被击杀, 影响 {} 名玩家)",
                    boss.getDisplayName().getString(), players.size());
        } catch (Throwable ignored) {
        }
    }

    /** 玩家对某只 Boss 的当前连死次数(已含过期判定) */
    public static int streakOf(ServerPlayer p, LivingEntity boss) {
        String key = keyOf(boss);
        if (key == null) return 0;
        return BossMercyData.get(p.serverLevel()).streak(p.getUUID(), key, p.serverLevel().getGameTime());
    }

    // ================= 归因 =================

    /** 归因结果: 命中的那**一只** Boss + 说明(写日志用) */
    private record Attribution(LivingEntity boss, String reason) {}

    private static Attribution attribute(ServerPlayer p) {
        DamageSource src = p.getLastDamageSource();
        if (src != null) {
            Entity killer = src.getEntity();
            if (killer == null) killer = src.getDirectEntity();
            Entity owner = resolveOwner(killer);
            // ① 直接击杀者就是我们的 Boss
            if (owner instanceof LivingEntity le && typeOf(le) != null) {
                return new Attribution(le, "击杀者=" + le.getDisplayName().getString());
            }
            // ② 击杀者是召唤物 -> 记最近的那只 Boss
            if (isMinion(owner)) {
                LivingEntity near = nearestBoss(p, 64.0);
                if (near != null) return new Attribution(near, "召唤物击杀(" + owner.getType().getDescription().getString() + ")→最近Boss");
            }
        }
        if (p.level() instanceof ServerLevel sl) {
            long now = sl.getGameTime();
            // ③ 最近 10 秒内打中过我、且仍活着且在 48 格内的那只(覆盖摔死/环境死/无实体法术)
            java.util.Map<String, Long> map = THREATS.get(p.getUUID());
            if (map != null) {
                String best = null;
                long bestT = Long.MIN_VALUE;
                for (var e : map.entrySet()) {
                    if (now - e.getValue() > THREAT_MEMORY) continue;
                    LivingEntity b = findAliveByKey(sl, p, e.getKey(), 48.0);
                    if (b == null) continue;
                    if (e.getValue() > bestT) { bestT = e.getValue(); best = e.getKey(); }
                }
                if (best != null) {
                    LivingEntity b = findAliveByKey(sl, p, best, 48.0);
                    if (b != null) return new Attribution(b, "最近威胁(" + (now - bestT) + "t 前)");
                }
            }
        }
        // ④ 兜底: 附近最近的 Boss
        LivingEntity near = nearestBoss(p, NEAR_RADIUS);
        if (near != null) return new Attribution(near, "附近最近的Boss");
        return new Attribution(null, "附近没有 Boss");
    }

    private static Entity resolveOwner(Entity e) {
        if (e instanceof net.farmingtales.ftboss.entity.ReefMinionEntity minion
                && minion.ownerId() != null && minion.level() instanceof ServerLevel level) {
            Entity owner = level.getEntity(minion.ownerId());
            if (owner != null) return owner;
        }
        if (e == null) return null;
        if (e instanceof Projectile proj && proj.getOwner() != null) return proj.getOwner();
        return e;
    }

    /**
     * 这次死亡该不该发"Boss 专属掉落"（石像 / 专属武器）—— 2026-09-19 v167 新增。
     *
     * <p><b>修的是什么</b>：用户反馈「鼠群的奶酪锤爆率异常」。原因是
     * {@link net.farmingtales.ftboss.boss.BossHandler#onBossDeath} 对**任何** Mob 死亡都会调
     * {@link #onBossDefeated}，而{@code giveWeaponDrop} / {@code giveStatue} 只按
     * {@code getType()} 比对 —— 小老鼠与鼠群首领**是同一个实体类型**
     * （{@link net.farmingtales.ftboss.entity.FTEntities#RAT_BOSS}，靠 {@code isMinion()} 区分），
     * 于是每打死一只小老鼠都会掉一把奶酪轮锤。鼠群一场仗能召出十几只小老鼠，
     * 掉落自然"异常"。
     *
     * <p>用 {@link #isMinion} 统一挡住：鼠群小老鼠、茶树小妖、姜饼小怪、小酒桶、充能球
     * 都不再吐 Boss 掉落。**只拦掉落**，不动连死怜悯那套记账（{@code onBossDefeated} 后半段原样保留），
     * 所以这次改动不会影响任何玩家的连死计数或 Boss 离场判定。
     */
    private static boolean dropsLoot(LivingEntity boss) {
        return boss != null && !boss.level().isClientSide && !isMinion(boss)
                && !LemonSummonRewards.suppressed(boss);
    }

    private static boolean isMinion(Entity e) {
        if (e == null) return false;
        if (e instanceof RatBossEntity r) return r.isMinion();
        return e instanceof TeaMinionEntity
                || e instanceof net.farmingtales.ftboss.entity.GingerbreadMinionEntity
                || e instanceof net.farmingtales.ftboss.entity.SmallBarrelEntity
                || e instanceof net.farmingtales.ftboss.entity.ChargedOrbEntity;
    }

    /** 每只 Boss 的记账键 = 实例 UUID(日志用; 连死计数已改用 {@link #typeCounterKey}) */
    private static String keyOf(Entity e) {
        return typeOf(e) == null ? null : "i:" + e.getStringUUID();
    }

    /**
     * 连死计数用的键 = **Boss 类型**（不是实例）：{@code "t:ftboss:meat_mountain"}。
     *
     * <p>★ v170 修「屠夫连杀退场失效」。用实例 UUID 记账时，只要两次死亡之间 Boss 被换成新实例，
     * 计数就永远是 1/5 —— 而屠夫恰恰最容易换：玩家一死它就清场，下次召唤是全新一只
     * （日志实证：召唤(1392) → 死亡 1/5 → 清场 → 召唤(1684) → 死亡 1/5，
     * 499 个日志里屠夫的"连死怜悯触发"是 0 次）。
     *
     * <p>换成类型键不会把原设计想防的事情放回来：{@link #onBossDefeated} 在真身被击杀时会
     * **同时清掉类型键与实例键**，所以"打死它就清零"照旧；另外计数本身还有
     * {@link BossMercyData#STREAK_TIMEOUT}（10 分钟）自动过期。
     */
    private static String typeCounterKey(Entity e) {
        String type = typeOf(e);
        return type == null ? null : "t:" + type;
    }

    /** 实体 -> Boss 类型 id(非 Boss 返回 null; 小老鼠/迷你分身不算) */
    public static String typeOf(Entity e) {
        if (e instanceof net.farmingtales.ftboss.entity.TideFishBossEntity) return "ftboss:tide_fish_boss";
        if (e instanceof SugarQueenEntity) return "ftboss:sugar_queen";
        if (e instanceof MeatMountainEntity) return "ftboss:meat_mountain";
        if (e instanceof TeaTreeBossEntity) return "ftboss:tea_tree";
        if (e instanceof WineBarrelBossEntity) return "ftboss:wine_barrel";
        if (e instanceof RatBossEntity r) return r.isMinion() ? null : "ftboss:rat_boss";
        if (e instanceof net.farmingtales.ftboss.entity.DefectCoreEntity) return "ftboss:defect_core";
        // 彩蛋十人(甜苹果庄园): 以前整个漏掉 -> 玩家在庄园里被打死会被记成"附近没有 Boss",
        // 连死怜悯不计数、也不给雕像。迷你分身不算(它们只是柚子的分身, 记到本体头上)。
        if (e instanceof net.farmingtales.ftboss.entity.EasterBossEntity eb) {
            return eb.isMini() ? null : "ftboss:easter_boss_" + eb.getVariant();
        }
        return null;
    }

    /** 半径内最近的一只 Boss */
    private static LivingEntity nearestBoss(ServerPlayer p, double radius) {
        LivingEntity best = null;
        double bd = radius * radius;
        for (LivingEntity e : p.level().getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(radius))) {
            if (!e.isAlive() || typeOf(e) == null) continue;
            double d = p.distanceToSqr(e);
            if (d < bd) { bd = d; best = e; }
        }
        return best;
    }

    /** 按记账键找"仍活着且在半径内"的那只 Boss */
    private static LivingEntity findAliveByKey(ServerLevel sl, ServerPlayer p, String key, double radius) {
        for (LivingEntity e : sl.getEntitiesOfClass(LivingEntity.class, p.getBoundingBox().inflate(radius))) {
            if (e.isAlive() && key.equals(keyOf(e))) return e;
        }
        return null;
    }

    // ================= 离场 =================

    /**
     * 让**这一只** Boss 离场: 不触发死亡流程 => 不掉落、不给经验。
     * 顺带清掉它自己的召唤物(球/小怪), 以及它自己的血条。
     */
    public static int despawnBoss(LivingEntity boss) {
        return despawnBoss(boss, "连死怜悯: 玩家连续死亡 " + LIMIT + " 次");
    }

    /** 指定原因的无奖励退场；供极限模式死亡前中止等安全路径复用。 */
    public static int despawnBoss(LivingEntity boss, String reason) {
        if (boss == null) return 0;
        ServerLevel sl = (boss.level() instanceof ServerLevel s) ? s : null;
        int n = 0;
        try {
            // 该实例的召唤物
            if (sl != null) {
                UUID id = boss.getUUID();
                List<Entity> pets = new ArrayList<>();
                for (Entity e : sl.getAllEntities()) {
                    if (e instanceof net.farmingtales.ftboss.entity.ChargedOrbEntity orb && orb.isOwnedByUuid(id)) pets.add(e);
                    else if (e instanceof RatBossEntity r && r.isMinion() && r.distanceToSqr(boss) < 64 * 64) pets.add(e);
                    else if (e instanceof net.farmingtales.ftboss.entity.SmallBarrelEntity && e.distanceToSqr(boss) < 48 * 48) pets.add(e);
                    else if (e instanceof net.farmingtales.ftboss.entity.GingerbreadMinionEntity && e.distanceToSqr(boss) < 48 * 48) pets.add(e);
                    else if (e instanceof TeaMinionEntity && e.distanceToSqr(boss) < 48 * 48) pets.add(e);
                    /* v112: 蛆(故障机器人/答辩超人的召唤物)以前漏在白名单外 ——
                       MaggotMinionEntity 有 setOwnerBoss/isOwnedByUuid, 但全工程没人调用,
                       怜悯离场带不走它们。这里按"离本体 48 格内"一起收。 */
                    else if (e instanceof net.farmingtales.ftboss.entity.MaggotMinionEntity && e.distanceToSqr(boss) < 48 * 48) pets.add(e);
                }
                for (Entity e : pets) { e.discard(); n++; }
            }
            // 本体
            if (boss instanceof SugarQueenEntity q) {
                if (q.isCloneQueen()) { q.discard(); } else { SugarQueenBoss.cleanup(reason); }
            } else if (boss instanceof MeatMountainEntity m) {
                if (m.isCloneButcher()) { m.hideBossBar(); m.discard(); } else { MeatBoss.cleanup(reason); }
            } else if (boss instanceof net.farmingtales.ftboss.entity.DefectCoreEntity d) {
                d.orbs().reset();
                d.discard();
            } else {
                if (boss instanceof RatBossEntity r) r.hideBossBar();
                else if (boss instanceof TeaTreeBossEntity t) t.hideBossBar();
                boss.discard();
            }
            n++;
            FTBossMod.LOGGER.info("[FTBoss] {}: Boss「{}」及其召唤物已离场(共清理 {} 个实体)",
                    reason, boss.getDisplayName().getString(), n);
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] {}离场异常: {}", reason, t.toString());
        }
        return n;
    }

    /**
     * 统一掉落石像(2026-09-12): 按 Boss 类型给对应石像。
     * 之前只有屠夫会掉专属石像, 女皇掉的是通用白膜, 沙耶/酒桶王/鼠群**完全没有** -> 现在一处补齐。
     */
    private static void giveStatue(LivingEntity boss) {
        if (!dropsLoot(boss)) return;
        try {
            var t = boss.getType();
            net.minecraft.world.item.Item statue = null;
            if (boss instanceof net.farmingtales.ftboss.entity.TideFishBossEntity) {
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_TIDE_FISH.get().asItem();
            }
            if (t == net.farmingtales.ftboss.entity.FTEntities.SUGAR_QUEEN.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_SUGAR_QUEEN.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.MEAT_MOUNTAIN.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_BUTCHER.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.TEA_TREE.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_SAYA.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.WINE_BARREL.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_WINE_BARREL.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.RAT_BOSS.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_RAT_SWARM.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.APPLE_BOSS.get())
            statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_APPLE.get().asItem();   // 2026-09-12: 苹果爵士
        else if (t == net.farmingtales.ftboss.entity.FTEntities.COMPOST_BOSS.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_COMPOST.get().asItem();
            else if (t == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_DEFECT.get().asItem();   // 2026-09-12: 机器人石像
            else if (t == net.farmingtales.ftboss.entity.FTEntities.BENGER.get())
                statue = net.farmingtales.ftboss.block.FTBlocks.STATUE_BENGER.get().asItem();
            if (statue != null) {
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(statue));
                FTBossMod.LOGGER.info("[FTBoss] 石像掉落: {}", statue.getDescription().getString());
            }
        } catch (Throwable t2) {
            FTBossMod.LOGGER.warn("[FTBoss] 石像掉落失败: {}", t2.toString());
        }
    }

    /**
     * 按 Boss 类型掉落专属武器(2026-09-12): 姜饼王 -> 糖果拐杖。
     * 与石像一样走中央钩子, 保证任何死亡路径都能掉。
     */
    private static void giveWeaponDrop(LivingEntity boss) {
        if (!dropsLoot(boss)) return;
        if (boss instanceof net.farmingtales.ftboss.entity.TideFishBossEntity) {
            NonManorBossLootRouter.spawnAtBossLike(boss, net.farmingtales.ftboss.item.FTBossItems.TIDE_TRIDENT.get());
        }
        try {
            if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.SUGAR_QUEEN.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.CANDY_CANE.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Candy Cane (Gingerbread King)");
            } else if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.APPLE_BOSS.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.APPLE_STEM.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Apple Stem (Sir Honeycrisp)");
            } else if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.MEAT_MOUNTAIN.get()) {
                /* v140: 屠夫的武器【大肉腿】—— 击败后掉落 */
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.TURKEY_LEGS.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Giant Turkey Leg (Butcher)");
            } else if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.DEFECT_CORE.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.DEFECT_CLAW.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Charged Claw (Defect)");
            } else if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.BENGER.get()) {
                /* v127: 第 9 位主 Boss 边牧犬王 benger —— 掉落【打狗棒】 */
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.DOG_BEATING_STICK.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Dog-Beating Stick (benger)");
            } else if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.RAT_BOSS.get()) {
                /* ★ 2026-09-19 用户反馈「鼠群未掉落武器」：鼠群原本**没有任何武器掉落代码**，
                   它的专属武器【奶酪轮锤】只存在于创造模式。这里走中央钩子补齐。
                   (小老鼠是 rat_minion，类型不同，不会跟着掉。) */
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.CHEESE_WHEEL_MACE.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Cheese Wheel Mace (Rat Swarm)");
            } else if (boss.getType() == net.farmingtales.ftboss.entity.FTEntities.WINE_BARREL.get()) {
                /* ★ 2026-09-19 用户反馈「酒桶弩也没有掉落」：酒桶王原本同样没有武器掉落代码
                   （手上的弩在 WineBarrelBossEntity 里被设成 dropChance 0 = 纯外观），
                   于是【酒瓶锤】与【酒桶弩】都只存在于创造模式。这里一次补齐两件。 */
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.WINE_BOTTLE_MACE.get()));
                NonManorBossLootRouter.spawnAtBoss(boss, new net.minecraft.world.item.ItemStack(
                        net.farmingtales.ftboss.item.FTBossItems.BARREL_CROSSBOW.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Wine Bottle Mace + Barrel Crossbow (Wine Barrel King)");
            }
        } catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] weapon drop failed: {}", t.toString());
        }
    }
}
