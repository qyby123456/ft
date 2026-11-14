/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.lemonmovement;

import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class LemonMovementRules {
    public static final double SPEED_MULTIPLIER = 1.25;

    private LemonMovementRules() {
    }

    public static double walkingSpeed(double d, EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 4 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() ? d * 1.25 : d;
    }
}

