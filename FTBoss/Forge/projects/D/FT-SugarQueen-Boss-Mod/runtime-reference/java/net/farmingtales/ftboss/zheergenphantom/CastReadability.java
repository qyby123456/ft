/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.zheergenphantom;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.minecraft.world.entity.LivingEntity;

public final class CastReadability {
    private CastReadability() {
    }

    public static int windup(int n, boolean bl, int n2, int n3) {
        return n == 7 && bl ? Math.max(n3, n2) : n2;
    }

    public static int fiveAge(int n, ManorFiveCombatRules.Skill skill) {
        return skill == ManorFiveCombatRules.Skill.SHADOW_CROSS || skill == ManorFiveCombatRules.Skill.SHADOW_CAGE ? skill.windup - Math.max(16, skill.windup - n) : n;
    }

    public static int strikeWindup(int n, EasterBossEntity easterBossEntity) {
        return CastReadability.windup(easterBossEntity.getVariant(), !easterBossEntity.isMini() && !easterBossEntity.isGiant(), n, 16);
    }

    public static int ironWindup(int n, EasterBossEntity easterBossEntity) {
        return CastReadability.windup(easterBossEntity.getVariant(), !easterBossEntity.isMini() && !easterBossEntity.isGiant(), n, 20);
    }

    public static int ironCaster(int n, LivingEntity livingEntity) {
        int n2;
        if (livingEntity instanceof EasterBossEntity) {
            EasterBossEntity easterBossEntity = (EasterBossEntity)livingEntity;
            n2 = CastReadability.ironWindup(n, easterBossEntity);
        } else {
            n2 = n;
        }
        return n2;
    }
}

