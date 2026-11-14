/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 */
package net.farmingtales.ftboss.boss;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ManorMercy;
import net.farmingtales.ftboss.boss.MeatBoss;
import net.farmingtales.ftboss.boss.SugarQueenBoss;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossMercy;
import net.farmingtales.ftboss.boss.ai.MinionBlessing;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.ReefMinionEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TeaTreeBossEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.farmingtales.ftboss.entity.WineBarrelBossEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class HardcoreBossSafety {
    public static final int MANOR_GRACE_TICKS = 60;
    public static final float MANOR_GRACE_HEALTH = 1.0f;
    private static final int MANOR_GRACE_RESISTANCE_AMPLIFIER = 4;
    private static final double ATTRIBUTION_RADIUS = 64.0;
    private static final Map<UUID, Long> MANOR_GRACE_UNTIL = new ConcurrentHashMap<UUID, Long>();

    private HardcoreBossSafety() {
    }

    public static boolean beforePlayerDeath(LivingDeathEvent event) {
        UUID fight;
        Entity attacker;
        ServerPlayer player;
        LivingEntity livingEntity;
        if (event == null || event.isCanceled() || !((livingEntity = event.getEntity()) instanceof ServerPlayer) || (player = (ServerPlayer)livingEntity).m_5833_() || !player.f_8924_.m_7035_()) {
            return false;
        }
        if (HardcoreBossSafety.hasManorGrace(player)) {
            HardcoreBossSafety.keepAlive(event, player);
            return true;
        }
        DamageSource source = event.getSource();
        Entity entity = attacker = source == null ? null : source.m_7639_();
        if (attacker == null && source != null) {
            attacker = source.m_7640_();
        }
        if (attacker == null) {
            attacker = ManorMercy.damageOwner();
        }
        if (ManorFight.abortForHardcore(player, fight = ManorFight.fightId(attacker))) {
            HardcoreBossSafety.grantManorGrace(event, player);
            return true;
        }
        LivingEntity boss = HardcoreBossSafety.resolveBoss(attacker, player);
        if (boss == null && ManorMercy.damageOwner() != attacker) {
            boss = HardcoreBossSafety.resolveBoss(ManorMercy.damageOwner(), player);
        }
        if (boss != null) {
            UUID bossFight = ManorFight.fightId((Entity)boss);
            if (ManorFight.abortForHardcore(player, bossFight)) {
                HardcoreBossSafety.grantManorGrace(event, player);
                return true;
            }
            String name = boss.m_5446_().getString();
            BossMercy.despawnBoss(boss, "\u6781\u9650\u6a21\u5f0f\u73a9\u5bb6\u6b7b\u4ea1\u524d\u4e2d\u6b62");
            HardcoreBossSafety.report(player, name);
            return true;
        }
        boolean stopped = false;
        if (SugarQueenBoss.isActive() && player.m_20148_().equals(SugarQueenBoss.ownerIdOf())) {
            SugarQueenBoss.cleanup("\u6781\u9650\u6a21\u5f0f\u53ec\u5524\u8005\u6b7b\u4ea1\u524d\u4e2d\u6b62");
            stopped = true;
        }
        if (MeatBoss.isActive() && player.m_20148_().equals(MeatBoss.ownerIdOf())) {
            MeatBoss.cleanup("\u6781\u9650\u6a21\u5f0f\u53ec\u5524\u8005\u6b7b\u4ea1\u524d\u4e2d\u6b62");
            stopped = true;
        }
        if (stopped) {
            HardcoreBossSafety.report(player, "\u53ec\u5524\u8005\u6240\u5c5e Boss");
        }
        return stopped;
    }

    public static boolean cancelManorGraceDamage(LivingHurtEvent event) {
        block3: {
            block2: {
                LivingEntity livingEntity;
                if (event == null || event.isCanceled() || !((livingEntity = event.getEntity()) instanceof ServerPlayer)) break block2;
                ServerPlayer player = (ServerPlayer)livingEntity;
                if (player.f_8924_.m_7035_() && HardcoreBossSafety.hasManorGrace(player)) break block3;
            }
            return false;
        }
        event.setCanceled(true);
        return true;
    }

    public static void tickGrace(MinecraftServer server) {
        if (server == null || MANOR_GRACE_UNTIL.isEmpty()) {
            return;
        }
        long now = server.m_129921_();
        MANOR_GRACE_UNTIL.entrySet().removeIf(entry -> (Long)entry.getValue() <= now);
    }

    public static void clearGrace() {
        MANOR_GRACE_UNTIL.clear();
    }

    private static boolean hasManorGrace(ServerPlayer player) {
        Long until = MANOR_GRACE_UNTIL.get(player.m_20148_());
        if (until == null) {
            return false;
        }
        if ((long)player.f_8924_.m_129921_() < until) {
            return true;
        }
        MANOR_GRACE_UNTIL.remove(player.m_20148_(), until);
        return false;
    }

    private static void grantManorGrace(LivingDeathEvent event, ServerPlayer player) {
        long until = player.f_8924_.m_129921_() + 60;
        MANOR_GRACE_UNTIL.put(player.m_20148_(), until);
        player.m_21153_(1.0f);
        player.f_19802_ = Math.max(player.f_19802_, 60);
        player.m_7292_(new MobEffectInstance(MobEffects.f_19606_, 60, 4, false, true, true));
        event.setCanceled(true);
        player.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u3010\u6781\u9650\u6a21\u5f0f\u3011\u00a7f\u5e84\u56ed\u81f4\u547d\u4f24\u5bb3\u5df2\u62e6\u622a\uff1a\u4fdd\u7559 1 \u70b9\u751f\u547d\u5e76\u83b7\u5f97 3 \u79d2\u65e0\u654c\uff0cBoss \u6218\u5df2\u4e2d\u6b62\uff1b\u65e0\u6389\u843d\u3001\u65e0\u7ecf\u9a8c\u3002"));
        FTBossMod.LOGGER.warn("[FTBoss][Hardcore] {} \u906d\u53d7\u5e84\u56ed\u81f4\u547d\u4f24\u5bb3\uff1a\u6218\u6597\u5df2\u4e2d\u6b62\uff0c\u6b7b\u4ea1\u5df2\u53d6\u6d88\u5e76\u7ed9\u4e88 {}t \u65e0\u654c", (Object)player.m_36316_().getName(), (Object)60);
    }

    private static void keepAlive(LivingDeathEvent event, ServerPlayer player) {
        player.m_21153_(Math.max(1.0f, player.m_21223_()));
        event.setCanceled(true);
        FTBossMod.LOGGER.debug("[FTBoss][Hardcore] {} \u7684\u5e84\u56ed\u65e0\u654c\u62e6\u622a\u4e86\u4fdd\u62a4\u671f\u5185\u7684\u6b7b\u4ea1\u4e8b\u4ef6", (Object)player.m_36316_().getName());
    }

    private static LivingEntity resolveBoss(Entity entity, ServerPlayer player) {
        Entity current = entity;
        for (int depth = 0; current != null && depth < 8; ++depth) {
            LivingEntity living;
            if (current instanceof LivingEntity && HardcoreBossSafety.isBoss(living = (LivingEntity)current)) {
                return living;
            }
            Entity parent = HardcoreBossSafety.owner(current);
            if (parent == null || parent == current) break;
            current = parent;
        }
        if (entity == null || !BossAggro.isBossSide(entity)) {
            return null;
        }
        return HardcoreBossSafety.nearestBoss(player);
    }

    private static Entity owner(Entity entity) {
        ServerLevel level;
        Level level2;
        ReefMinionEntity reef;
        Entity owner = ManorFightMembers.owner(entity);
        if (owner != null && owner != entity) {
            return owner;
        }
        if (entity instanceof ReefMinionEntity && (reef = (ReefMinionEntity)entity).ownerId() != null && (level2 = reef.m_9236_()) instanceof ServerLevel && (owner = (level = (ServerLevel)level2).m_8791_(reef.ownerId())) != null) {
            return owner;
        }
        UUID marked = MinionBlessing.ownerId(entity);
        if (marked != null && (level2 = entity.m_9236_()) instanceof ServerLevel && (owner = (level = (ServerLevel)level2).m_8791_(marked)) != null) {
            return owner;
        }
        try {
            Entity found;
            Object summoner = entity.getClass().getMethod("getSummoner", new Class[0]).invoke(entity, new Object[0]);
            if (summoner instanceof Entity && (found = (Entity)summoner) != entity) {
                return found;
            }
        }
        catch (Throwable throwable) {
            // empty catch block
        }
        return null;
    }

    private static LivingEntity nearestBoss(ServerPlayer player) {
        LivingEntity nearest = null;
        double best = 4096.0;
        for (LivingEntity candidate : player.m_284548_().m_45976_(LivingEntity.class, player.m_20191_().m_82400_(64.0))) {
            double distance;
            if (!candidate.m_6084_() || !HardcoreBossSafety.isBoss(candidate) || !((distance = player.m_20280_((Entity)candidate)) < best)) continue;
            best = distance;
            nearest = candidate;
        }
        return nearest;
    }

    private static boolean isBoss(LivingEntity entity) {
        if (entity instanceof RatBossEntity) {
            RatBossEntity rat = (RatBossEntity)entity;
            return !rat.isMinion();
        }
        if (entity instanceof EasterBossEntity) {
            EasterBossEntity easter = (EasterBossEntity)entity;
            return !easter.isMini();
        }
        return entity instanceof TideFishBossEntity || entity instanceof SugarQueenEntity || entity instanceof MeatMountainEntity || entity instanceof TeaTreeBossEntity || entity instanceof WineBarrelBossEntity || entity instanceof DefectCoreEntity || entity instanceof CompostBossEntity || entity instanceof AppleBossEntity || entity instanceof BengerEntity;
    }

    private static void report(ServerPlayer player, String boss) {
        player.m_213846_((Component)Component.m_237113_((String)"\u00a7c\u3010\u6781\u9650\u6a21\u5f0f\u3011\u00a77\u68c0\u6d4b\u5230\u81f4\u547d\u4f24\u5bb3\uff0c\u5df2\u5728\u6b7b\u4ea1\u7ed3\u7b97\u524d\u4e2d\u6b62 Boss \u6218\uff1b\u65e0\u6389\u843d\u3001\u65e0\u7ecf\u9a8c\u3002"));
        FTBossMod.LOGGER.warn("[FTBoss][Hardcore] {} \u5373\u5c06\u6b7b\u4ea1\uff0c\u5df2\u5148\u4e2d\u6b62 Boss \u6218\uff1a{}", (Object)player.m_36316_().getName(), (Object)boss);
    }
}

