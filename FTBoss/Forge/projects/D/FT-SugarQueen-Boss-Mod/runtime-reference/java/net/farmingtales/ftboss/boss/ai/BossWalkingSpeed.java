/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import java.util.UUID;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.AppleBossEntity;
import net.farmingtales.ftboss.entity.BengerEntity;
import net.farmingtales.ftboss.entity.CompostBossEntity;
import net.farmingtales.ftboss.entity.DefectCoreEntity;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.entity.MeatMountainEntity;
import net.farmingtales.ftboss.entity.RatBossEntity;
import net.farmingtales.ftboss.entity.SugarQueenEntity;
import net.farmingtales.ftboss.entity.TideFishBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class BossWalkingSpeed {
    public static final double BASE_SPEED = 0.1;
    public static final double BLOCKS_PER_TICK = 0.21585;
    public static final double WATER_SPEED_BONUS = 0.45;
    public static final int WATER_SPEED_BONUS_PERCENT = 45;
    public static final float WATER_SLOW_DOWN = 0.96f;
    private static final UUID WATER_SPEED_MODIFIER = UUID.fromString("2f4a1c6e-6c2b-4a1e-9d3f-1b7c5e0a9d21");

    private BossWalkingSpeed() {
    }

    public static boolean applies(Mob boss) {
        if (boss instanceof EasterBossEntity) {
            EasterBossEntity easter = (EasterBossEntity)boss;
            return !easter.isMini() && !easter.isImpossibleChallenge();
        }
        if (boss instanceof RatBossEntity) {
            RatBossEntity rat = (RatBossEntity)boss;
            return !rat.isMinion();
        }
        return boss instanceof SugarQueenEntity || boss instanceof MeatMountainEntity || boss instanceof AppleBossEntity || boss instanceof CompostBossEntity || boss instanceof BengerEntity || boss instanceof TideFishBossEntity || boss instanceof DefectCoreEntity;
    }

    public static boolean inWater(Mob boss) {
        return boss != null && boss.m_20069_();
    }

    public static float speed(Mob boss, float requested) {
        if (!BossWalkingSpeed.applies(boss)) {
            return requested;
        }
        double attribute = boss.m_21133_(Attributes.f_22279_);
        double base = boss instanceof EasterBossEntity ? attribute : Math.min(0.1, attribute);
        double maximum = base * (BossWalkingSpeed.inWater(boss) ? 1.45 : 1.0);
        float f = Float.isFinite(requested) ? Math.max(0.0f, Math.min((float)maximum, requested)) : 0.0f;
        if (!(boss instanceof EasterBossEntity || BossWalkingSpeed.inWater(boss) || boss.m_20068_())) {
            f = f * 2.0f;
        }
        return f;
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        boolean applied;
        Mob boss;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Mob) || !BossWalkingSpeed.applies(boss = (Mob)livingEntity)) {
            return;
        }
        AttributeInstance movement = boss.m_21051_(Attributes.f_22279_);
        if (movement == null) {
            return;
        }
        boolean wet = BossWalkingSpeed.inWater(boss);
        boolean bl = applied = movement.m_22111_(WATER_SPEED_MODIFIER) != null;
        if (wet == applied) {
            return;
        }
        try {
            if (wet) {
                movement.m_22118_(new AttributeModifier(WATER_SPEED_MODIFIER, "ftboss_water_speed", 0.45, AttributeModifier.Operation.MULTIPLY_TOTAL));
            } else {
                movement.m_22120_(WATER_SPEED_MODIFIER);
            }
        }
        catch (Throwable t) {
            FTBossMod.LOGGER.warn("[FTBoss] \u6c34\u4e2d\u79fb\u901f\u4fee\u9970\u7b26\u5207\u6362\u5931\u8d25(\u5df2\u5ffd\u7565): {}", (Object)t.toString());
        }
    }

    public static void normalizeInput(Mob boss) {
        if (BossWalkingSpeed.applies(boss)) {
            boss.m_21564_(boss.m_6113_() > 0.0f ? 1.0f : 0.0f);
        }
    }

    @SubscribeEvent
    public static void onJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        Mob boss;
        Entity entity;
        EntityJoinLevelEvent event;
        if (entityJoinLevelEvent.getEntity() instanceof EasterBossEntity && !entityJoinLevelEvent.getLevel().m_5776_() && !((EasterBossEntity)entityJoinLevelEvent.getEntity()).isMini()) {
            ((EasterBossEntity)entityJoinLevelEvent.getEntity()).refreshWalkingSpeed();
            return;
        }
        if (event.getLevel().m_5776_() || !((entity = event.getEntity()) instanceof Mob) || !BossWalkingSpeed.applies(boss = (Mob)entity)) {
            return;
        }
        AttributeInstance movement = boss.m_21051_(Attributes.f_22279_);
        if (movement != null) {
            movement.m_22100_(0.1);
        }
    }
}

