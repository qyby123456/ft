/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.HashSet;
import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.HiddenBossIntro;
import net.farmingtales.ftboss.boss.ManorFight;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.boss.ai.BossAggro;
import net.farmingtales.ftboss.boss.ai.BossScaling;
import net.farmingtales.ftboss.boss.ai.BossTargets;
import net.farmingtales.ftboss.boss.ai.ManorAdaptiveShield;
import net.farmingtales.ftboss.boss.ai.ManorContributionData;
import net.farmingtales.ftboss.boss.ai.ManorRaidLedger;
import net.farmingtales.ftboss.boss.ai.ManorRaidRules;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class ManorRaidCombat {
    private static final int OWNER_DEPTH_LIMIT = 8;
    private static final int CLEANUP_INTERVAL_TICKS = 100;
    private static final String HEALTH_PREPARED = "ftboss:manor_raid_health_prepared";

    private ManorRaidCombat() {
    }

    public static UUID mainFight(LivingEntity entity) {
        EasterBossEntity boss;
        block3: {
            block2: {
                if (!(entity instanceof EasterBossEntity)) break block2;
                boss = (EasterBossEntity)entity;
                if (entity.m_9236_() instanceof ServerLevel && !boss.isMini() && boss.getVariant() != 9) break block3;
            }
            return null;
        }
        UUID fight = ManorFight.fightId((Entity)boss);
        return fight != null && ManorFight.hasActive(fight) ? fight : null;
    }

    public static double scaleMaximumHealth(double normalScaledHealth, LivingEntity boss) {
        return ManorRaidRules.maximumHealth(normalScaledHealth, ManorRaidCombat.mainFight(boss) != null && ((EasterBossEntity)boss).isImpossibleChallenge());
    }

    public static void beforeHealthWrite(LivingEntity entity, float requestedHealth, DamageSource source) {
        UUID fight = ManorRaidCombat.mainFight(entity);
        if (fight == null || !entity.m_20148_().equals(ManorFight.currentBoss(fight))) {
            return;
        }
        double damage = ManorRaidRules.acceptedDamage(entity.m_21223_(), requestedHealth);
        if (damage <= 0.0 || source == null) {
            return;
        }
        EasterBossEntity boss = (EasterBossEntity)entity;
        ManorContributionData data = ManorContributionData.get(((ServerLevel)entity.m_9236_()).m_7654_());
        data.observe(fight, boss.m_20148_()).recordKind(ManorAdaptiveShield.damageKind(source), damage);
        data.m_77762_();
        ServerPlayer player = ManorRaidCombat.playerOwner(source.m_7639_());
        if (player == null) {
            player = ManorRaidCombat.playerOwner(source.m_7640_());
        }
        if (!BossTargets.valid((Player)player) || player.m_9236_() != entity.m_9236_()) {
            return;
        }
        data.observe(fight, boss.m_20148_()).record(boss.m_20148_(), player.m_20148_(), damage);
        data.m_77762_();
        ManorFight.participate((Entity)boss, player);
    }

    private static ServerPlayer playerOwner(Entity entity) {
        HashSet<UUID> visited = new HashSet<UUID>();
        for (int depth = 0; entity != null && depth < 8 && visited.add(entity.m_20148_()); ++depth) {
            if (entity instanceof ServerPlayer) {
                ServerPlayer player = (ServerPlayer)entity;
                return player;
            }
            Entity owner = ManorFightMembers.owner(entity);
            if (owner == null) {
                try {
                    Object summoner = entity.getClass().getMethod("getSummoner", new Class[0]).invoke(entity, new Object[0]);
                    if (summoner instanceof Entity) {
                        Entity parent;
                        owner = parent = (Entity)summoner;
                    }
                }
                catch (ReflectiveOperationException reflectiveOperationException) {
                    // empty catch block
                }
            }
            entity = owner;
        }
        return null;
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onJoin(EntityJoinLevelEvent event) {
        LivingEntity boss;
        Entity entity = event.getEntity();
        if (entity instanceof LivingEntity && ManorRaidCombat.mainFight(boss = (LivingEntity)entity) != null) {
            BossScaling.tick(boss);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        ManorContributionData data;
        ManorRaidLedger ledger;
        LivingEntity entity = event.getEntity();
        UUID fight = ManorRaidCombat.mainFight(entity);
        if (fight == null || !entity.m_6084_() || !entity.m_20148_().equals(ManorFight.currentBoss(fight))) {
            return;
        }
        EasterBossEntity boss = (EasterBossEntity)entity;
        ServerLevel level = (ServerLevel)boss.m_9236_();
        if (!boss.getPersistentData().m_128471_(HEALTH_PREPARED)) {
            BossScaling.tick((LivingEntity)boss);
            boss.getPersistentData().m_128379_(HEALTH_PREPARED, true);
        }
        if ((ledger = (data = ManorContributionData.get(level.m_7654_())).observe(fight, boss.m_20148_())).bonusConsumed() || boss.m_21525_() || boss.m_20145_() || HiddenBossIntro.isActing(boss.m_20148_())) {
            return;
        }
        long players = level.m_6907_().stream().filter(BossTargets::valid).filter(player -> player.m_20280_((Entity)boss) <= 4096.0).count();
        UUID winner = ledger.consumeBonus(players > 1L, playerId -> {
            ServerPlayer player = level.m_7654_().m_6846_().m_11259_(playerId);
            return BossTargets.valid((Player)player) && player.m_9236_() == level && player.m_20280_((Entity)boss) <= 4096.0;
        });
        data.m_77762_();
        if (winner == null) {
            return;
        }
        ServerPlayer player2 = level.m_7654_().m_6846_().m_11259_(winner);
        BossAggro.record((LivingEntity)boss, (Entity)player2, 100.0, level.m_46467_());
        FTBossMod.LOGGER.info("[FTBoss][\u5e84\u56ed\u8f93\u51fa\u4ec7\u6068] \u4e0a\u4e00\u4f4d\u6700\u9ad8\u8f93\u51fa={}\uff0c\u6709\u6548\u6263\u8840={}\uff0c\u4e0b\u4e00\u4f4d={}\uff0c\u521d\u59cb\u4ec7\u6068+{}", new Object[]{player2.m_7755_().getString(), ledger.previous().get(winner), boss.m_7755_().getString(), Math.min(100.0, (double)boss.m_21223_())});
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().m_129921_() % 100 != 0) {
            return;
        }
        ManorFight.hasActive(event.getServer().m_129783_());
        ManorContributionData.get(event.getServer()).prune(ManorFight::hasActive);
    }
}

