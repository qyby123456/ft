/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.living.LivingDropsEvent
 *  net.minecraftforge.event.entity.living.LivingExperienceDropEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.boss.ManorFightMembers;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class LemonSummonRewards {
    public static final String NO_REWARDS_TAG = "ftboss:lemon_summon_no_rewards";
    public static final String POST_SACRIFICE_HELPER_TAG = "ftboss:lemon_post_sacrifice_helper";
    public static final String POST_SACRIFICE_OWNER_TAG = "ftboss:lemon_post_sacrifice_owner";
    private static final String DEFEAT_COUNTED_TAG = "ftboss:lemon_helper_defeat_counted";

    private LemonSummonRewards() {
    }

    public static void mark(Entity entity) {
        if (entity != null) {
            entity.getPersistentData().m_128379_(NO_REWARDS_TAG, true);
        }
    }

    public static boolean suppressed(Entity entity) {
        return entity != null && entity.getPersistentData().m_128471_(NO_REWARDS_TAG);
    }

    public static void markPostSacrificeHelper(Entity entity, UUID ownerId) {
        if (entity == null) {
            return;
        }
        entity.getPersistentData().m_128379_(POST_SACRIFICE_HELPER_TAG, true);
        if (ownerId != null) {
            entity.getPersistentData().m_128362_(POST_SACRIFICE_OWNER_TAG, ownerId);
        }
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void countPostSacrificeHelperDefeat(LivingDeathEvent event) {
        Entity entity;
        UUID ownerId;
        Level level;
        LivingEntity helper = event.getEntity();
        if (!helper.getPersistentData().m_128471_(POST_SACRIFICE_HELPER_TAG) || helper.getPersistentData().m_128471_(DEFEAT_COUNTED_TAG) || !((level = helper.m_9236_()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        UUID stamped = helper.getPersistentData().m_128403_(POST_SACRIFICE_OWNER_TAG) ? helper.getPersistentData().m_128342_(POST_SACRIFICE_OWNER_TAG) : null;
        UUID uUID = ownerId = stamped != null ? stamped : ManorFightMembers.ownerId((Entity)helper);
        if (ownerId == null || !((entity = level2.m_8791_(ownerId)) instanceof EasterBossEntity)) {
            FTBossMod.LOGGER.warn("[FTBoss][Lemon] \u52a9\u6218 {} \u6b7b\u4ea1\u4f46\u627e\u4e0d\u5230\u53ec\u5524\u8005(\u70d9\u5370={}, ownerId={}) -> \u672c\u6b21\u4e0d\u8ba1\u5165\u4e09\u53ea", new Object[]{helper.m_7755_().getString(), stamped, ownerId});
            helper.getPersistentData().m_128379_(DEFEAT_COUNTED_TAG, true);
            return;
        }
        EasterBossEntity lemon = (EasterBossEntity)entity;
        if (!lemon.onPostSacrificeHelperDefeated((Entity)helper)) {
            return;
        }
        helper.getPersistentData().m_128379_(DEFEAT_COUNTED_TAG, true);
        FTBossMod.LOGGER.info("[FTBoss][Lemon] \u786e\u8ba4\u52a9\u6218 Boss \u771f\u6b63\u6b7b\u4ea1: {}", (Object)helper.m_7755_().getString());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void suppressDrops(LivingDropsEvent event) {
        if (!LemonSummonRewards.suppressed((Entity)event.getEntity())) {
            return;
        }
        event.getDrops().clear();
        event.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void suppressExperience(LivingExperienceDropEvent event) {
        if (LemonSummonRewards.suppressed((Entity)event.getEntity())) {
            event.setDroppedExperience(0);
        }
    }
}

