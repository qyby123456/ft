/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.ManorLivingTargets;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class GongdiCastLifecycle {
    private GongdiCastLifecycle() {
    }

    public static boolean canBegin(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 3 && easterBossEntity.m_9236_() instanceof ServerLevel && easterBossEntity.m_6084_() && !easterBossEntity.isMini() && !easterBossEntity.m_21525_();
    }

    public static boolean canStart(EasterBossEntity easterBossEntity, ServerLevel serverLevel, LivingEntity livingEntity) {
        return GongdiCastLifecycle.canBegin(easterBossEntity) && easterBossEntity.m_9236_() == serverLevel && livingEntity != null && ManorLivingTargets.valid((Entity)easterBossEntity, livingEntity);
    }

    public static boolean canFinish(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = easterBossEntity.getSkillCue();
        return GongdiCastLifecycle.canBegin(easterBossEntity) && compoundTag.m_128441_("gongdiSkill") && !compoundTag.m_128471_("released");
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        if (easterBossEntity.getVariant() == 3 && easterBossEntity.m_9236_() instanceof ServerLevel && easterBossEntity.getSkillCue().m_128441_("gongdiSkill") && !easterBossEntity.getSkillCue().m_128471_("released")) {
            easterBossEntity.cancelManorFiveCast();
        }
    }
}

