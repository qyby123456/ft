/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.Projectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.boss.BossAdvancements;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.boss.SugarQueenBoss;
import net.farmingtales.ftboss.boss.ai.BossMercyData;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.entity.ChargedOrbEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.GingerbreadMinionEntity;
import net.farmingtales.ftboss.entity.MaggotMinionEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.ReefMinionEntity;
import net.farmingtales.ftboss.entity.SmallBarrelEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaMinionEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.farmingtales.ftboss.item.FTBossItems;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

public final class BossMercy {
    public static final int LIMIT = 5;
    private static final double NEAR_RADIUS = 32.0;
    private static final long THREAT_MEMORY = 200L;
    private static final Map<UUID, Map<String, Long>> THREATS = new ConcurrentHashMap<UUID, Map<String, Long>>();
    private static final int THREAT_PLAYER_CAP = 128;

    private static void pruneThreats(long now) {
        try {
            if (THREATS.size() > 128) {
                THREATS.entrySet().removeIf(en -> {
                    ((Map)en.getValue()).entrySet().removeIf(t -> now - (Long)t.getValue() > 200L);
                    return ((Map)en.getValue()).isEmpty();
                });
                if (THREATS.size() > 128) {
                    THREATS.clear();
                }
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    private BossMercy() {
    }

    public static void noteThreat(ServerPlayer p, Entity attacker, long gameTime) {
        try {
            Entity a = BossMercy.resolveOwner(attacker);
            if (a == null) {
                return;
            }
            String key = BossMercy.keyOf(a);
            if (key == null) {
                LivingEntity boss = BossMercy.nearestBoss(p, 64.0);
                if (boss == null) {
                    return;
                }
                key = BossMercy.keyOf((Entity)boss);
            }
            if (key == null) {
                return;
            }
            THREATS.computeIfAbsent(p.m_20148_(), k -> new ConcurrentHashMap()).put(key, gameTime);
            BossMercy.pruneThreats(gameTime);
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static void onPlayerDeath(ServerPlayer p) {
        try {
            Attribution at = BossMercy.attribute(p);
            if (at == null || at.boss == null) {
                FTBossMod.LOGGER.info("[FTBoss] \u8fde\u6b7b\u8ba1\u6570: {} \u6b7b\u4ea1\u672a\u5f52\u56e0\u5230\u4efb\u4f55 Boss({}), \u4e0d\u8ba1\u6570", (Object)p.m_7755_().getString(), (Object)(at == null ? "?" : at.reason));
                return;
            }
            BossMercyData data = BossMercyData.get(p.m_284548_());
            long now = p.m_284548_().m_46467_();
            String key = BossMercy.keyOf((Entity)at.boss);
            String counterKey = BossMercy.typeCounterKey((Entity)at.boss);
            String name = at.boss.m_5446_().getString();
            if (at.boss instanceof EasterBossEntity) {
                FTBossMod.LOGGER.info("[FTBoss] \u8fde\u6b7b\u8ba1\u6570: {} \u6b7b\u4e8e\u9690\u85cf Boss\u300c{}\u300d[{}] \u2014\u2014 \u9690\u85cf Boss \u4e0d\u53c2\u4e0e\u8fde\u6b7b\u601c\u60af, \u4e0d\u8ba1\u6570", new Object[]{p.m_7755_().getString(), name, at.reason});
                return;
            }
            int prev = data.streak(p.m_20148_(), counterKey, now);
            int n = prev + 1;
            if (n >= 5) {
                data.setStreak(p.m_20148_(), counterKey, 0, now);
                BossMercy.despawnBoss(at.boss);
                p.m_5661_((Component)Component.m_237113_((String)("\u00a7e\u4f60\u5728\u300c" + name + "\u300d\u4e0b\u8fde\u7eed\u6b7b\u4ea1 5 \u6b21 \u2014 \u5b83\u5df2\u79bb\u573a(\u00a77\u65e0\u6389\u843d\u3001\u65e0\u7ecf\u9a8c\u00a7e)\u3002\u91cd\u65b0\u53ec\u5524\u5373\u53ef\u518d\u6765\u3002")), false);
                FTBossMod.LOGGER.info("[FTBoss] \u8fde\u6b7b\u601c\u60af\u89e6\u53d1: {} \u5728\u300c{}\u300d({}) \u4e0b\u8fde\u7eed\u6b7b\u4ea1 {} \u6b21 [\u5f52\u56e0: {}], \u8be5 Boss \u5df2\u79bb\u573a(\u65e0\u6389\u843d/\u65e0\u7ecf\u9a8c)", new Object[]{p.m_7755_().getString(), name, counterKey, 5, at.reason});
            } else {
                data.setStreak(p.m_20148_(), counterKey, n, now);
                p.m_5661_((Component)Component.m_237113_((String)("\u00a77\u5728\u300c" + name + "\u300d\u4e0b\u8fde\u7eed\u6b7b\u4ea1 \u00a7c" + n + "\u00a77/5 \u00a77(\u518d\u6b7b \u00a7c" + (5 - n) + "\u00a77 \u6b21\u5b83\u5c31\u4f1a\u79bb\u573a; \u00a78\u8d85\u8fc7 10 \u5206\u949f\u672a\u518d\u6b7b\u4ea1\u4f1a\u81ea\u52a8\u91cd\u7f6e\u00a77)")), true);
                FTBossMod.LOGGER.info("[FTBoss] \u8fde\u6b7b\u8ba1\u6570: {} \u5728\u300c{}\u300d({} \u2190 \u5b9e\u4f8b={}) \u4e0b\u6b7b\u4ea1 {}/{} [\u5f52\u56e0: {}{}]", new Object[]{p.m_7755_().getString(), name, counterKey, key, n, 5, at.reason, prev == 0 ? ", \u65e0\u65e7\u8ba1\u6570/\u5df2\u8fc7\u671f" : ""});
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u8fde\u6b7b\u601c\u60af\u7ed3\u7b97\u5f02\u5e38: {}", (Object)t.toString());
        }
    }

    public static void onBossDefeated(LivingEntity boss) {
        BossMercy.giveStatue(boss);
        BossMercy.giveWeaponDrop(boss);
        BossAdvancements.onBossDefeated(boss);
        try {
            Level level = boss.m_9236_();
            if (!(level instanceof ServerLevel)) {
                return;
            }
            ServerLevel sl = (ServerLevel)level;
            String key = BossMercy.keyOf((Entity)boss);
            if (key == null) {
                return;
            }
            String counterKey = BossMercy.typeCounterKey((Entity)boss);
            BossMercyData data = BossMercyData.get(sl);
            long now = sl.m_46467_();
            List players = sl.m_45976_(ServerPlayer.class, boss.m_20191_().m_82400_(96.0));
            for (ServerPlayer p : players) {
                data.setStreak(p.m_20148_(), key, 0, now);
                if (counterKey == null) continue;
                data.setStreak(p.m_20148_(), counterKey, 0, now);
            }
            FTBossMod.LOGGER.info("[FTBoss] \u8fde\u6b7b\u8ba1\u6570\u5df2\u6e05\u96f6(Boss\u300c{}\u300d\u88ab\u51fb\u6740, \u5f71\u54cd {} \u540d\u73a9\u5bb6)", (Object)boss.m_5446_().getString(), (Object)players.size());
        }
        catch (Throwable throwable) {
            // empty catch block
        }
    }

    public static int streakOf(ServerPlayer p, LivingEntity boss) {
        String key = BossMercy.keyOf((Entity)boss);
        if (key == null) {
            return 0;
        }
        return BossMercyData.get(p.m_284548_()).streak(p.m_20148_(), key, p.m_284548_().m_46467_());
    }

    private static Attribution attribute(ServerPlayer p) {
        LivingEntity near;
        Level owner;
        DamageSource src = p.m_21225_();
        if (src != null) {
            LivingEntity near2;
            LivingEntity le;
            Entity killer = src.m_7639_();
            if (killer == null) {
                killer = src.m_7640_();
            }
            if ((owner = BossMercy.resolveOwner(killer)) instanceof LivingEntity && BossMercy.typeOf((Entity)(le = (LivingEntity)owner)) != null) {
                return new Attribution(le, "\u51fb\u6740\u8005=" + le.m_5446_().getString());
            }
            if (BossMercy.isMinion((Entity)owner) && (near2 = BossMercy.nearestBoss(p, 64.0)) != null) {
                return new Attribution(near2, "\u53ec\u5524\u7269\u51fb\u6740(" + owner.m_6095_().m_20676_().getString() + ")\u2192\u6700\u8fd1Boss");
            }
        }
        if ((owner = p.m_9236_()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)owner;
            long now = sl.m_46467_();
            Map<String, Long> map = THREATS.get(p.m_20148_());
            if (map != null) {
                LivingEntity b;
                String best = null;
                long bestT = Long.MIN_VALUE;
                for (Map.Entry<String, Long> e : map.entrySet()) {
                    LivingEntity b2;
                    if (now - e.getValue() > 200L || (b2 = BossMercy.findAliveByKey(sl, p, e.getKey(), 48.0)) == null || e.getValue() <= bestT) continue;
                    bestT = e.getValue();
                    best = e.getKey();
                }
                if (best != null && (b = BossMercy.findAliveByKey(sl, p, best, 48.0)) != null) {
                    return new Attribution(b, "\u6700\u8fd1\u5a01\u80c1(" + (now - bestT) + "t \u524d)");
                }
            }
        }
        if ((near = BossMercy.nearestBoss(p, 32.0)) != null) {
            return new Attribution(near, "\u9644\u8fd1\u6700\u8fd1\u7684Boss");
        }
        return new Attribution(null, "\u9644\u8fd1\u6ca1\u6709 Boss");
    }

    private static Entity resolveOwner(Entity e) {
        Projectile proj;
        ServerLevel level;
        Entity owner;
        Level level2;
        ReefMinionEntity minion;
        if (e instanceof ReefMinionEntity && (minion = (ReefMinionEntity)e).ownerId() != null && (level2 = minion.m_9236_()) instanceof ServerLevel && (owner = (level = (ServerLevel)level2).m_8791_(minion.ownerId())) != null) {
            return owner;
        }
        if (e == null) {
            return null;
        }
        if (e instanceof Projectile && (proj = (Projectile)e).m_19749_() != null) {
            return proj.m_19749_();
        }
        return e;
    }

    private static boolean dropsLoot(LivingEntity boss) {
        return boss != null && !boss.m_9236_().f_46443_ && !BossMercy.isMinion((Entity)boss) && !LemonSummonRewards.suppressed((Entity)boss);
    }

    private static boolean isMinion(Entity e) {
        if (e == null) {
            return false;
        }
        if (e instanceof RatBossEntity) {
            RatBossEntity r = (RatBossEntity)e;
            return r.isMinion();
        }
        return e instanceof TeaMinionEntity || e instanceof GingerbreadMinionEntity || e instanceof SmallBarrelEntity || e instanceof ChargedOrbEntity;
    }

    private static String keyOf(Entity e) {
        return BossMercy.typeOf(e) == null ? null : "i:" + e.m_20149_();
    }

    private static String typeCounterKey(Entity e) {
        String type = BossMercy.typeOf(e);
        return type == null ? null : "t:" + type;
    }

    public static String typeOf(Entity e) {
        if (e instanceof TideFishBossEntity) {
            return "ftboss:tide_fish_boss";
        }
        if (e instanceof SugarQueenEntity) {
            return "ftboss:sugar_queen";
        }
        if (e instanceof MeatMountainEntity) {
            return "ftboss:meat_mountain";
        }
        if (e instanceof TeaTreeBossEntity) {
            return "ftboss:tea_tree";
        }
        if (e instanceof WineBarrelBossEntity) {
            return "ftboss:wine_barrel";
        }
        if (e instanceof RatBossEntity) {
            RatBossEntity r = (RatBossEntity)e;
            return r.isMinion() ? null : "ftboss:rat_boss";
        }
        if (e instanceof DefectCoreEntity) {
            return "ftboss:defect_core";
        }
        if (e instanceof EasterBossEntity) {
            EasterBossEntity eb = (EasterBossEntity)e;
            return eb.isMini() ? null : "ftboss:easter_boss_" + eb.getVariant();
        }
        return null;
    }

    private static LivingEntity nearestBoss(ServerPlayer p, double radius) {
        LivingEntity best = null;
        double bd = radius * radius;
        for (LivingEntity e : p.m_9236_().m_45976_(LivingEntity.class, p.m_20191_().m_82400_(radius))) {
            double d;
            if (!e.m_6084_() || BossMercy.typeOf((Entity)e) == null || !((d = p.m_20280_((Entity)e)) < bd)) continue;
            bd = d;
            best = e;
        }
        return best;
    }

    private static LivingEntity findAliveByKey(ServerLevel sl, ServerPlayer p, String key, double radius) {
        for (LivingEntity e : sl.m_45976_(LivingEntity.class, p.m_20191_().m_82400_(radius))) {
            if (!e.m_6084_() || !key.equals(BossMercy.keyOf((Entity)e))) continue;
            return e;
        }
        return null;
    }

    public static int despawnBoss(LivingEntity boss) {
        return BossMercy.despawnBoss(boss, "\u8fde\u6b7b\u601c\u60af: \u73a9\u5bb6\u8fde\u7eed\u6b7b\u4ea1 5 \u6b21");
    }

    public static int despawnBoss(LivingEntity boss, String reason) {
        ServerLevel s;
        if (boss == null) {
            return 0;
        }
        Level level = boss.m_9236_();
        ServerLevel sl = level instanceof ServerLevel ? (s = (ServerLevel)level) : null;
        int n = 0;
        try {
            if (sl != null) {
                UUID id = boss.m_20148_();
                ArrayList<Entity> pets = new ArrayList<Entity>();
                for (Entity e : sl.m_8583_()) {
                    RatBossEntity r;
                    ChargedOrbEntity orb;
                    if (e instanceof ChargedOrbEntity && (orb = (ChargedOrbEntity)e).isOwnedByUuid(id)) {
                        pets.add(e);
                        continue;
                    }
                    if (e instanceof RatBossEntity && (r = (RatBossEntity)e).isMinion() && r.m_20280_((Entity)boss) < 4096.0) {
                        pets.add(e);
                        continue;
                    }
                    if (e instanceof SmallBarrelEntity && e.m_20280_((Entity)boss) < 2304.0) {
                        pets.add(e);
                        continue;
                    }
                    if (e instanceof GingerbreadMinionEntity && e.m_20280_((Entity)boss) < 2304.0) {
                        pets.add(e);
                        continue;
                    }
                    if (e instanceof TeaMinionEntity && e.m_20280_((Entity)boss) < 2304.0) {
                        pets.add(e);
                        continue;
                    }
                    if (!(e instanceof MaggotMinionEntity) || !(e.m_20280_((Entity)boss) < 2304.0)) continue;
                    pets.add(e);
                }
                for (Entity e : pets) {
                    e.m_146870_();
                    ++n;
                }
            }
            if (boss instanceof SugarQueenEntity) {
                SugarQueenEntity q = (SugarQueenEntity)boss;
                if (q.isCloneQueen()) {
                    q.m_146870_();
                } else {
                    SugarQueenBoss.cleanup(reason);
                }
            } else if (boss instanceof MeatMountainEntity) {
                MeatMountainEntity m = (MeatMountainEntity)boss;
                if (m.isCloneButcher()) {
                    m.hideBossBar();
                    m.m_146870_();
                } else {
                    MeatBoss.cleanup(reason);
                }
            } else if (boss instanceof DefectCoreEntity) {
                DefectCoreEntity d = (DefectCoreEntity)boss;
                d.orbs().reset();
                d.m_146870_();
            } else {
                if (boss instanceof RatBossEntity) {
                    RatBossEntity r = (RatBossEntity)boss;
                    r.hideBossBar();
                } else if (boss instanceof TeaTreeBossEntity) {
                    TeaTreeBossEntity t = (TeaTreeBossEntity)boss;
                    t.hideBossBar();
                }
                boss.m_146870_();
            }
            FTBossMod.LOGGER.info("[FTBoss] {}: Boss\u300c{}\u300d\u53ca\u5176\u53ec\u5524\u7269\u5df2\u79bb\u573a(\u5171\u6e05\u7406 {} \u4e2a\u5b9e\u4f53)", new Object[]{reason, boss.m_5446_().getString(), ++n});
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] {}\u79bb\u573a\u5f02\u5e38: {}", (Object)reason, (Object)t.toString());
        }
        return n;
    }

    private static void giveStatue(LivingEntity boss) {
        if (!BossMercy.dropsLoot(boss)) {
            return;
        }
        try {
            EntityType t = boss.m_6095_();
            Item statue = null;
            if (boss instanceof TideFishBossEntity) {
                statue = ((Block)FTBlocks.STATUE_TIDE_FISH.get()).m_5456_();
            }
            if (t == FTEntities.SUGAR_QUEEN.get()) {
                statue = ((Block)FTBlocks.STATUE_SUGAR_QUEEN.get()).m_5456_();
            } else if (t == FTEntities.MEAT_MOUNTAIN.get()) {
                statue = ((Block)FTBlocks.STATUE_BUTCHER.get()).m_5456_();
            } else if (t == FTEntities.TEA_TREE.get()) {
                statue = ((Block)FTBlocks.STATUE_SAYA.get()).m_5456_();
            } else if (t == FTEntities.WINE_BARREL.get()) {
                statue = ((Block)FTBlocks.STATUE_WINE_BARREL.get()).m_5456_();
            } else if (t == FTEntities.RAT_BOSS.get()) {
                statue = ((Block)FTBlocks.STATUE_RAT_SWARM.get()).m_5456_();
            } else if (t == FTEntities.APPLE_BOSS.get()) {
                statue = ((Block)FTBlocks.STATUE_APPLE.get()).m_5456_();
            } else if (t == FTEntities.COMPOST_BOSS.get()) {
                statue = ((Block)FTBlocks.STATUE_COMPOST.get()).m_5456_();
            } else if (t == FTEntities.DEFECT_CORE.get()) {
                statue = ((Block)FTBlocks.STATUE_DEFECT.get()).m_5456_();
            } else if (t == FTEntities.BENGER.get()) {
                statue = ((Block)FTBlocks.STATUE_BENGER.get()).m_5456_();
            }
            if (statue != null) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)statue));
                FTBossMod.LOGGER.info("[FTBoss] \u77f3\u50cf\u6389\u843d: {}", (Object)statue.m_41466_().getString());
            }
        }
        catch (Throwable t2) {
            FTBossMod.LOGGER.warn("[FTBoss] \u77f3\u50cf\u6389\u843d\u5931\u8d25: {}", (Object)t2.toString());
        }
    }

    private static void giveWeaponDrop(LivingEntity boss) {
        if (!BossMercy.dropsLoot(boss)) {
            return;
        }
        if (boss instanceof TideFishBossEntity) {
            NonManorBossLootRouter.spawnAtBossLike(boss, (ItemLike)FTBossItems.TIDE_TRIDENT.get());
        }
        try {
            if (boss.m_6095_() == FTEntities.SUGAR_QUEEN.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.CANDY_CANE.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Candy Cane (Gingerbread King)");
            } else if (boss.m_6095_() == FTEntities.APPLE_BOSS.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.APPLE_STEM.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Apple Stem (Sir Honeycrisp)");
            } else if (boss.m_6095_() == FTEntities.MEAT_MOUNTAIN.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.TURKEY_LEGS.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Giant Turkey Leg (Butcher)");
            } else if (boss.m_6095_() == FTEntities.DEFECT_CORE.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.DEFECT_CLAW.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Charged Claw (Defect)");
            } else if (boss.m_6095_() == FTEntities.BENGER.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.DOG_BEATING_STICK.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Dog-Beating Stick (benger)");
            } else if (boss.m_6095_() == FTEntities.RAT_BOSS.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.CHEESE_WHEEL_MACE.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Cheese Wheel Mace (Rat Swarm)");
            } else if (boss.m_6095_() == FTEntities.WINE_BARREL.get()) {
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.WINE_BOTTLE_MACE.get()));
                NonManorBossLootRouter.spawnAtBoss(boss, new ItemStack((ItemLike)FTBossItems.BARREL_CROSSBOW.get()));
                FTBossMod.LOGGER.info("[FTBoss] weapon drop: Wine Bottle Mace + Barrel Crossbow (Wine Barrel King)");
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] weapon drop failed: {}", (Object)t.toString());
        }
    }

    private record Attribution(LivingEntity boss, String reason) {
    }
}

