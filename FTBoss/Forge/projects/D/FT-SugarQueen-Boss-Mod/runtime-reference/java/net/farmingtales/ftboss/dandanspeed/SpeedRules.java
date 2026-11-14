/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandanspeed;

import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class SpeedRules {
    public static final float MULTIPLIER = 1.3f;

    private SpeedRules() {
    }

    public static float scale(float f, int n, boolean bl, boolean bl2) {
        return n == 1 && !bl && !bl2 ? f * 1.3f : f;
    }

    public static float apply(float f, EasterBossEntity easterBossEntity) {
        return SpeedRules.scale(f, easterBossEntity.getVariant(), easterBossEntity.isMini(), easterBossEntity.isGiant());
    }
}

