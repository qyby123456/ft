/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.xueli_melee;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.xueli_melee.MeleeRules;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class MeleePolicy {
    private MeleePolicy() {
    }

    public static boolean preferRanged(boolean bl, boolean bl2, EasterBossEntity easterBossEntity) {
        if (!bl || bl2 || easterBossEntity == null || easterBossEntity.getVariant() != 10 || easterBossEntity.isMini() || easterBossEntity.isGiant()) {
            return bl;
        }
        LivingEntity livingEntity = easterBossEntity.m_5448_();
        if (livingEntity == null) {
            return bl;
        }
        return MeleeRules.preferRanged(bl, bl2, easterBossEntity.getVariant(), easterBossEntity.isMini(), easterBossEntity.isGiant(), easterBossEntity.m_20280_((Entity)livingEntity), livingEntity.m_20186_() - easterBossEntity.m_20186_());
    }
}

