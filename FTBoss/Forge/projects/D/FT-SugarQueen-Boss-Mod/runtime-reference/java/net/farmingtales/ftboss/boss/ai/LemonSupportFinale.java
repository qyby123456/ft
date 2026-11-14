/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.NonManorFinaleDifficulty;
import net.farmingtales.ftboss.boss.ai.LemonSummonRewards;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;

public final class LemonSupportFinale {
    private static final String PREPARED = "ftboss:regular_finale_prepared";
    private static final String SCALING_HEALTH = "ftboss_base_hp";
    private static final String SCALING_ATTACK = "ftboss_base_atk";

    private LemonSupportFinale() {
    }

    public static boolean overrides(boolean bl, boolean bl2) {
        return bl && bl2;
    }

    public static float migratedHealth(float f, float f2, float f3) {
        float f4 = f2 > 0.0f ? Math.max(0.0f, Math.min(1.0f, f / f2)) : 1.0f;
        return Math.max(0.0f, Math.min(f3, f4 * f3));
    }

    public static boolean onJoin(EntityJoinLevelEvent entityJoinLevelEvent) {
        LivingEntity livingEntity;
        Entity entity;
        if (!(entityJoinLevelEvent.getLevel() instanceof ServerLevel && (entity = entityJoinLevelEvent.getEntity()) instanceof LivingEntity && LemonSupportFinale.overrides(NonManorFinaleDifficulty.isRegularBoss(livingEntity = (LivingEntity)entity), LemonSummonRewards.suppressed((Entity)livingEntity)))) {
            return false;
        }
        if (NonManorFinaleDifficulty.isFinale(livingEntity)) {
            return false;
        }
        float f = livingEntity.m_21223_();
        float f2 = livingEntity.m_21233_();
        CompoundTag compoundTag = livingEntity.getPersistentData();
        compoundTag.m_128473_(PREPARED);
        NonManorFinaleDifficulty.prepare(livingEntity, true);
        AttributeInstance attributeInstance = livingEntity.m_21051_(Attributes.f_22276_);
        AttributeInstance attributeInstance2 = livingEntity.m_21051_(Attributes.f_22281_);
        if (attributeInstance != null && compoundTag.m_128441_(SCALING_HEALTH)) {
            compoundTag.m_128347_(SCALING_HEALTH, attributeInstance.m_22115_());
        }
        if (attributeInstance2 != null && compoundTag.m_128441_(SCALING_ATTACK)) {
            compoundTag.m_128347_(SCALING_ATTACK, attributeInstance2.m_22115_());
        }
        if (entityJoinLevelEvent.loadedFromDisk()) {
            livingEntity.m_21153_(LemonSupportFinale.migratedHealth(f, f2, livingEntity.m_21233_()));
        }
        return true;
    }
}

