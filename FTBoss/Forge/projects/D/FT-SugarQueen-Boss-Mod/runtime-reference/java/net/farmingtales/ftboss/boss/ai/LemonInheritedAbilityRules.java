/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.LemonSacrifice;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class LemonInheritedAbilityRules {
    public static final float MULTIPLIER = 5.0f;

    private LemonInheritedAbilityRules() {
    }

    public static float damage(float baseDamage) {
        if (!Float.isFinite(baseDamage) || baseDamage <= 0.0f) {
            return baseDamage;
        }
        return baseDamage * 5.0f;
    }

    public static boolean inherited(Entity entity) {
        return entity != null && LemonSacrifice.isInheritedSummon(entity);
    }

    public static float allySkillDamage(LivingEntity attacker, float damage) {
        if (!(attacker instanceof EasterBossEntity)) {
            return damage;
        }
        return LemonInheritedAbilityRules.inherited((Entity)attacker) ? LemonInheritedAbilityRules.damage(damage) : damage;
    }
}

