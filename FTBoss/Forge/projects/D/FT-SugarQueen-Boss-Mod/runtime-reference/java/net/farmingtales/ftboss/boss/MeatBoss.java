/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.ExperienceOrb
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.enchantment.Enchantments
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.phys.Vec3
 */
package net.farmingtales.ftboss.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.block.FTBlocks;
import net.farmingtales.ftboss.boss.SummonGate;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ButcherCombatRules;
import net.farmingtales.ftboss.boss.ai.ButcherMinionMeleeGoal;
import net.farmingtales.ftboss.boss.ai.ButcherPhases;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.boss.ai.NonManorBossLootRouter;
import net.farmingtales.ftboss.butcherskills.ButcherSkills;
import net.farmingtales.ftboss.entity.FTEntities;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.util.BossCollision;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

public final class MeatBoss {
    private static final double MAX_HP = 380.0;
    private static final double DESPAWN_RANGE = 50.0;
    private static final int SUMMON_CD = 360;
    private static final int SUMMON_CD_P2 = 240;
    private static final int[][] WAVES = new int[][]{{2, 0}, {2, 1}, {3, 1}, {3, 1}};
    private static final int NO_PLAYER_GRACE = 1200;
    private static int noPlayerTicks = 0;
    private static final String[] MINION_NAMES = new String[]{"\u5c60\u592b\u5b66\u5f92", "\u814c\u8089\u5c38\u58f3", "\u5c60\u592b\u961f\u957f\u00b7\u7cbe\u82f1"};
    private static final ChatFormatting[] MINION_COLORS = new ChatFormatting[]{ChatFormatting.WHITE, ChatFormatting.GOLD, ChatFormatting.RED};
    private static boolean active = false;
    private static ServerLevel level;
    private static UUID bossUUID;
    private static UUID ownerUUID;
    private static boolean phase2;
    private static boolean finalMode;
    private static boolean totemUsed;
    private static int tick;
    private static int summonCd;
    private static int waveIdx;
    private static final List<UUID> minions;

    private static boolean anyPlayerPresentOrDying(Entity boss) {
        if (level == null) {
            return false;
        }
        for (ServerPlayer p : level.m_6907_()) {
            if (!p.m_6084_()) {
                return true;
            }
            if (!(p.m_20280_(boss) <= 2500.0)) continue;
            return true;
        }
        return false;
    }

    private MeatBoss() {
    }

    private static boolean spawnClone(ServerPlayer sp) {
        try {
            ServerLevel lvl = sp.m_284548_();
            Entity raw = ((EntityType)FTEntities.MEAT_MOUNTAIN.get()).m_20615_((Level)lvl);
            if (!(raw instanceof MeatMountainEntity)) {
                return false;
            }
            MeatMountainEntity boss = (MeatMountainEntity)raw;
            Vec3 look = sp.m_20154_();
            Vec3 pos = sp.m_20182_().m_82520_(look.f_82479_ * 3.0, 0.0, look.f_82481_ * 3.0);
            boss.setCloneButcher(true);
            boss.equipMeatLeg();
            boss.m_6593_((Component)Component.m_237113_((String)"\u00a7c\u00a7l").m_7220_((Component)Component.m_237115_((String)"boss.ftboss.butcher")).m_7220_((Component)Component.m_237113_((String)"\u00a77\u00b7\u590d\u5236\u4f53")));
            boss.m_20340_(true);
            boss.m_21153_(380.0f);
            if (!BossCollision.place(lvl, (Entity)boss, new Vec3(pos.f_82479_, sp.m_20186_(), pos.f_82481_), BossCollision.SpawnMode.GROUND, sp.m_146908_() + 180.0f)) {
                BossCollision.tellNoRoom((Player)sp, "\u5c60\u592b\u590d\u5236\u4f53");
                return false;
            }
            boss.m_21530_();
            if (!ButcherSkills.addWithFx(lvl, (Entity)boss)) {
                BossCollision.tellNoRoom((Player)sp, "\u5c60\u592b\u590d\u5236\u4f53");
                return false;
            }
            lvl.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11868_, SoundSource.HOSTILE, 1.0f, 0.8f);
            sp.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u5c60\u592b\u590d\u5236\u4f53\u52a0\u5165\u6218\u6597\uff01"), true);
            FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b\u590d\u5236\u4f53\u52a0\u5165\u6218\u6597 @ {} {} {}", new Object[]{pos.f_82479_, pos.f_82480_, pos.f_82481_});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u5c60\u592b\u590d\u5236\u4f53\u53ec\u5524\u5931\u8d25: {}", (Object)t.toString());
            return false;
        }
    }

    public static boolean tryStart(ServerPlayer sp) {
        if (!SummonGate.allow((Level)sp.m_284548_(), (Player)sp, "\u5c60\u592b")) {
            return false;
        }
        if (active && !MeatBoss.trackedBossAlive()) {
            FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b\u72b6\u6001\u81ea\u6108: \u767b\u8bb0\u7684\u5b9e\u4f53\u5df2\u4e0d\u5728\u573a, \u6e05\u7406\u540e\u5141\u8bb8\u91cd\u65b0\u53ec\u5524");
            MeatBoss.cleanup("\u81ea\u6108: \u767b\u8bb0\u7684\u5b9e\u4f53\u4e0d\u5728\u573a");
        }
        if (active) {
            return MeatBoss.spawnClone(sp);
        }
        try {
            ServerLevel lvl = sp.m_284548_();
            Entity raw = ((EntityType)FTEntities.MEAT_MOUNTAIN.get()).m_20615_((Level)lvl);
            if (!(raw instanceof MeatMountainEntity)) {
                sp.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u5c60\u592b\u5b9e\u4f53\u521b\u5efa\u5931\u8d25"), true);
                FTBossMod.LOGGER.warn("[FTBoss] \u5c60\u592b\u53ec\u5524\u5931\u8d25: create() \u672a\u8fd4\u56de\u5c60\u592b\u5b9e\u4f53");
                return false;
            }
            MeatMountainEntity boss = (MeatMountainEntity)raw;
            int strays = MeatBoss.purgeStrays(sp.m_20194_());
            if (strays > 0) {
                FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b\u53ec\u5524\u524d\u6e05\u7406\u6b8b\u7559\u5b9e\u4f53 {} \u53ea", (Object)strays);
            }
            Vec3 look = sp.m_20154_();
            Vec3 pos = sp.m_20182_().m_82520_(look.f_82479_ * 2.5, 0.0, look.f_82481_ * 2.5);
            if (!BossCollision.place(lvl, (Entity)boss, new Vec3(pos.f_82479_, sp.m_20186_(), pos.f_82481_), BossCollision.SpawnMode.GROUND, sp.m_146908_() + 180.0f)) {
                BossCollision.tellNoRoom((Player)sp, "\u5c60\u592b");
                return false;
            }
            boss.m_6593_((Component)Component.m_237113_((String)"\u00a7c\u00a7l").m_7220_((Component)Component.m_237115_((String)"boss.ftboss.butcher")));
            boss.m_20340_(true);
            boss.m_21153_(380.0f);
            boss.m_21530_();
            boss.equipMeatLeg();
            level = lvl;
            bossUUID = boss.m_20148_();
            ownerUUID = sp.m_20148_();
            active = true;
            phase2 = false;
            finalMode = false;
            totemUsed = false;
            tick = 0;
            summonCd = 160;
            waveIdx = 0;
            noPlayerTicks = 0;
            minions.clear();
            if (!ButcherSkills.addWithFx(lvl, (Entity)boss)) {
                MeatBoss.cleanup("\u5165\u4e16\u754c\u5931\u8d25(addFreshEntity \u8fd4\u56de false)");
                BossCollision.tellNoRoom((Player)sp, "\u5c60\u592b");
                return false;
            }
            lvl.m_6263_(null, pos.f_82479_, pos.f_82480_, pos.f_82481_, SoundEvents.f_11957_, SoundSource.HOSTILE, 1.2f, 0.7f);
            FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b \u53ec\u5524 @ {} {} {} (\u5b9e\u4f53id={}, \u5165\u4e16\u754c\u540e\u53ef\u6309UUID\u53d6\u56de={})", new Object[]{String.format("%.1f", pos.f_82479_), String.format("%.1f", pos.f_82480_), String.format("%.1f", pos.f_82481_), boss.m_19879_(), lvl.m_8791_(bossUUID) == boss});
            return true;
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] \u5c60\u592b\u53ec\u5524\u5f02\u5e38: ", t);
            MeatBoss.cleanup("\u53ec\u5524\u8fc7\u7a0b\u629b\u5f02\u5e38");
            sp.m_5661_((Component)Component.m_237113_((String)"\u00a7c\u5c60\u592b\u53ec\u5524\u5f02\u5e38, \u8be6\u89c1\u65e5\u5fd7"), true);
            return false;
        }
    }

    private static boolean trackedBossAlive() {
        MeatMountainEntity m;
        if (level == null || bossUUID == null) {
            return false;
        }
        Entity e = level.m_8791_(bossUUID);
        return e instanceof MeatMountainEntity && !(m = (MeatMountainEntity)e).m_213877_() && m.m_6084_();
    }

    public static int purgeStrays(MinecraftServer srv) {
        if (srv == null) {
            return 0;
        }
        ArrayList<MeatMountainEntity> strays = new ArrayList<MeatMountainEntity>();
        for (ServerLevel sl : srv.m_129785_()) {
            for (Entity e : sl.m_8583_()) {
                MeatMountainEntity m;
                if (!(e instanceof MeatMountainEntity) || (m = (MeatMountainEntity)e).m_20148_().equals(bossUUID) || m.isCloneButcher()) continue;
                strays.add(m);
            }
        }
        for (MeatMountainEntity m : strays) {
            m.hideBossBar();
            m.m_146870_();
        }
        return strays.size();
    }

    public static float onMeatHitPlayer(ServerPlayer player, float dealt) {
        return dealt;
    }

    private static int summonWave(int count, int elites, LivingEntity target) {
        count = ButcherCombatRules.summonCount(count, minions.size());
        int spawned = 0;
        int tries = 0;
        while (spawned < count && tries < count * 5) {
            ++tries;
            try {
                boolean elite;
                int kind;
                Mob mini;
                double z;
                double a = Math.random() * Math.PI * 2.0;
                double r = 3.0 + Math.random() * 6.0;
                double x = MeatBoss.bossX() + Math.cos(a) * r;
                double y = MeatBoss.findSafeY(x, z = MeatBoss.bossZ() + Math.sin(a) * r);
                if (y == Double.MIN_VALUE || (mini = MeatBoss.createMinion(kind = (elite = spawned < elites) ? 2 : spawned % 2)) == null) continue;
                mini.m_6021_(x, y, z);
                mini.m_6593_((Component)Component.m_237113_((String)MINION_NAMES[kind]).m_130940_(MINION_COLORS[kind]));
                mini.m_20340_(true);
                mini.m_146915_(true);
                MinionBlessing.mark((Entity)mini, bossUUID);
                MeatBoss.boostMinion(mini, kind);
                if (!ButcherSkills.addWithFx(level, (Entity)mini)) continue;
                minions.add(mini.m_20148_());
                if (target != null) {
                    mini.m_6710_(target);
                }
                ++spawned;
            }
            catch (Throwable t) {
                FTBossMod.LOGGER.error("[FTBoss] \u5c60\u592b\u6ce2\u6b21\u53ec\u5524\u5f02\u5e38: ", t);
            }
        }
        return spawned;
    }

    private static Mob createMinion(int kind) {
        try {
            return kind == 1 ? (Mob)EntityType.f_20458_.m_20615_((Level)level) : (Mob)EntityType.f_20493_.m_20615_((Level)level);
        }
        catch (Throwable t) {
            return null;
        }
    }

    private static void boostMinion(Mob mini, int kind) {
        mini.m_21051_(Attributes.f_22276_).m_22100_(ButcherCombatRules.minionHealth(kind));
        mini.m_21051_(Attributes.f_22281_).m_22100_(ButcherCombatRules.minionDamage(kind));
        mini.m_21051_(Attributes.f_22284_).m_22100_(ButcherCombatRules.minionArmor(kind));
        mini.m_21051_(Attributes.f_22279_).m_22100_(ButcherCombatRules.minionSpeed(kind));
        mini.m_21051_(Attributes.f_22278_).m_22100_(kind == 2 ? 0.7 : 0.4);
        mini.m_21051_(Attributes.f_22277_).m_22100_(48.0);
        mini.m_21153_(mini.m_21233_());
        mini.m_21530_();
        mini.m_21553_(false);
        if (mini instanceof PathfinderMob) {
            PathfinderMob pathfinder = (PathfinderMob)mini;
            mini.f_21345_.m_25352_(1, (Goal)new ButcherMinionMeleeGoal(pathfinder, kind));
        }
        if (kind != 1) {
            ItemStack axe = new ItemStack((ItemLike)(kind == 2 ? Items.f_42391_ : Items.f_42386_));
            axe.m_41663_(Enchantments.f_44977_, kind == 2 ? 4 : 3);
            mini.m_8061_(EquipmentSlot.MAINHAND, axe);
            mini.m_21409_(EquipmentSlot.MAINHAND, 0.0f);
        }
        if (kind == 2) {
            mini.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 999999, 0, false, false));
        }
    }

    private static double bossX() {
        Entity e = level.m_8791_(bossUUID);
        return e != null ? e.m_20185_() : 0.0;
    }

    private static double bossY() {
        Entity e = level.m_8791_(bossUUID);
        return e != null ? e.m_20186_() : 100.0;
    }

    private static double bossZ() {
        Entity e = level.m_8791_(bossUUID);
        return e != null ? e.m_20189_() : 0.0;
    }

    private static double findSafeY(double x, double z) {
        int y;
        int dy;
        int base = (int)Math.floor(MeatBoss.bossY());
        for (dy = 0; dy >= -14; --dy) {
            y = base + dy;
            if (!MeatBoss.isSafe(x, y, z)) continue;
            return (double)y + 0.6;
        }
        for (dy = 1; dy <= 14; ++dy) {
            y = base + dy;
            if (!MeatBoss.isSafe(x, y, z)) continue;
            return (double)y + 0.6;
        }
        return Double.MIN_VALUE;
    }

    private static boolean isSafe(double x, int y, double z) {
        BlockPos here = BlockPos.m_274561_((double)x, (double)y, (double)z);
        return level.m_46859_(here) && level.m_46859_(here.m_7494_()) && !level.m_46859_(here.m_7495_());
    }

    private static void broadcast(String msg) {
        FTBossMod.LOGGER.info("[FTBoss] {}", (Object)msg);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void onBossDeath(Mob boss) {
        if (LemonSummonRewards.suppressed((Entity)boss)) {
            return;
        }
        if (!active || boss == null || !boss.m_20148_().equals(bossUUID)) {
            return;
        }
        try {
            double x = boss.m_20185_();
            double y = boss.m_20186_();
            double z = boss.m_20189_();
            int pork = 12 + MeatBoss.level.f_46441_.m_188503_(16);
            int beef = 8 + MeatBoss.level.f_46441_.m_188503_(8);
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y, z, new ItemStack((ItemLike)Items.f_42486_, pork)), (LivingEntity)boss);
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y + 0.3, z, new ItemStack((ItemLike)Items.f_42580_, beef)), (LivingEntity)boss);
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y + 0.6, z, new ItemStack((ItemLike)((Block)FTBlocks.STATUE_BUTCHER.get()).m_5456_(), 1)), (LivingEntity)boss);
            if (MeatBoss.level.f_46441_.m_188501_() < 0.3f) {
                ItemStack book = new ItemStack((ItemLike)Items.f_42516_);
                book.m_41714_((Component)Component.m_237113_((String)"\u00a76\u79d8\u5236\u70e4\u8089\u914d\u65b9"));
                NonManorBossLootRouter.spawn((Level)level, (Entity)new ItemEntity((Level)level, x, y + 0.9, z, book), (LivingEntity)boss);
            }
            NonManorBossLootRouter.spawn((Level)level, (Entity)new ExperienceOrb((Level)level, x, y, z, 150), (LivingEntity)boss);
            level.m_6263_(null, x, y, z, SoundEvents.f_12513_, SoundSource.PLAYERS, 1.0f, 1.0f);
            MeatBoss.broadcast("\u00a7c\u5c60\u592b \u00a77\u5012\u4e0b\u4e86\u2014\u2014\u6389\u4e86\u4e00\u5730\u70e4\u8089\u548c\u4e00\u5ea7\u77f3\u50cf\u2026");
            FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b \u88ab\u51fb\u8d25");
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.error("[FTBoss] \u5c60\u592b\u6b7b\u4ea1\u6389\u843d\u5f02\u5e38: ", t);
        }
        finally {
            MeatBoss.cleanup("\u88ab\u51fb\u8d25");
        }
    }

    public static void cleanup(String reason) {
        if (active) {
            FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b \u6e05\u573a: {}", (Object)reason);
        }
        active = false;
        if (level != null) {
            Entity b;
            for (UUID u : minions) {
                Entity e = level.m_8791_(u);
                if (e == null) continue;
                e.m_146870_();
            }
            minions.clear();
            if (bossUUID != null && (b = level.m_8791_(bossUUID)) != null) {
                if (b instanceof MeatMountainEntity) {
                    MeatMountainEntity mm = (MeatMountainEntity)b;
                    mm.hideBossBar();
                }
                b.m_146870_();
            }
        }
        bossUUID = null;
        ownerUUID = null;
        level = null;
        tick = 0;
        finalMode = false;
        totemUsed = false;
    }

    public static void cleanup() {
        MeatBoss.cleanup("\u672a\u6ce8\u660e(\u5916\u90e8\u8c03\u7528)");
    }

    public static boolean isActive() {
        return active;
    }

    public static boolean isBoss(Entity e) {
        return active && e != null && e.m_20148_().equals(bossUUID);
    }

    public static UUID ownerIdOf() {
        return ownerUUID;
    }

    static {
        phase2 = false;
        finalMode = false;
        totemUsed = false;
        tick = 0;
        summonCd = 160;
        waveIdx = 0;
        minions = new ArrayList<UUID>();
    }

    public static void tick() {
        if (!active || level == null) {
            return;
        }
        try {
            Object object;
            Mob mob;
            ++tick;
            Entity entity = level.m_8791_(bossUUID);
            if (entity == null) {
                MeatBoss.cleanup("\u5b9e\u4f53\u4e0d\u5728\u5df2\u52a0\u8f7d\u533a\u5757(\u88ab\u5378\u8f7d/\u672a\u540c\u6b65)");
                return;
            }
            if (!(entity instanceof Mob) || (mob = (Mob)entity).m_213877_()) {
                MeatBoss.cleanup("\u5b9e\u4f53\u5df2\u88ab\u79fb\u9664");
                return;
            }
            if (mob.m_21223_() <= 0.0f) {
                MeatBoss.cleanup("\u8840\u91cf\u5f52\u96f6\u4f46\u672a\u89e6\u53d1\u6b7b\u4ea1\u903b\u8f91");
                return;
            }
            LivingEntity livingEntity = BossTargets.combatTarget(mob, 30.0);
            if (livingEntity == null) {
                if (MeatBoss.anyPlayerPresentOrDying((Entity)mob)) {
                    noPlayerTicks = 0;
                } else if (++noPlayerTicks > 1200) {
                    MeatBoss.cleanup("\u9644\u8fd1\u957f\u65f6\u95f4(60 \u79d2)\u65e0\u73a9\u5bb6");
                }
                return;
            }
            noPlayerTicks = 0;
            double d = mob.m_20270_((Entity)livingEntity);
            mob.m_6710_(livingEntity);
            if (phase2) {
                --summonCd;
            }
            if (phase2 && summonCd <= 0) {
                summonCd = 240;
                object = WAVES[waveIdx % WAVES.length];
                ++waveIdx;
                if (MeatBoss.summonWave((int)object[0], (int)object[1], livingEntity) > 0) {
                    MeatBoss.broadcast("\u00a7c\u5c60\u592b \u00a77\u568e\u53eb\u4e00\u58f0\uff0c\u5c60\u592b\u5b66\u5f92\u4eec\u56f4\u4e86\u4e0a\u6765\u2026");
                }
            }
            if (tick % 5 == 0) {
                minions.removeIf(id -> {
                    Entity summon = level.m_8791_(id);
                    return summon == null || !summon.m_6084_() || summon.m_213877_();
                });
                object = minions.iterator();
                while (object.hasNext()) {
                    Mob mob2;
                    UUID uUID = (UUID)object.next();
                    Entity entity2 = level.m_8791_(uUID);
                    if (!(entity2 instanceof Mob) || !(mob2 = (Mob)entity2).m_6084_()) continue;
                    mob2.m_6710_(mob2.m_7307_((Entity)livingEntity) ? null : livingEntity);
                }
            }
            if (tick % 10 == 0) {
                int n = (int)Math.max(0.0, Math.ceil(mob.m_21223_()));
                for (Entity entity2 : level.m_6907_()) {
                    if (!(entity2.m_20280_((Entity)mob) < 2500.0)) continue;
                    entity2.m_5661_((Component)Component.m_237110_((String)"msg.ftboss.butcher_hp", (Object[])new Object[]{n, (int)mob.m_21233_()}), true);
                }
            }
            if (tick % 100 == 0) {
                FTBossMod.LOGGER.info("[FTBoss] \u5c60\u592b: \u8840={}/{} \u8ddd\u79bb={} \u6ce2\u6b21CD={}", new Object[]{(int)mob.m_21223_(), (int)mob.m_21233_(), String.format("%.1f", d), summonCd});
            }
        }
        catch (Throwable throwable) {
            FTBossMod.LOGGER.error("[FTBoss] \u5c60\u592b\u6218\u6597\u5f02\u5e38: ", throwable);
        }
    }

    public static boolean tryPopTotem(Mob mob) {
        MeatMountainEntity meatMountainEntity;
        if (LemonSummonRewards.suppressed((Entity)mob)) {
            return false;
        }
        return mob instanceof MeatMountainEntity && ButcherPhases.advance(meatMountainEntity = (MeatMountainEntity)mob);
    }

    public static void onPhaseChanged(MeatMountainEntity meatMountainEntity, int n) {
        if (!MeatBoss.isBoss((Entity)meatMountainEntity)) {
            return;
        }
        phase2 = n == 2;
        finalMode = n == 3;
        summonCd = 160;
        waveIdx = 0;
        if (phase2 || level == null) {
            return;
        }
        for (UUID uUID : minions) {
            Entity entity = level.m_8791_(uUID);
            if (entity == null) continue;
            entity.m_146870_();
        }
        minions.clear();
    }
}

