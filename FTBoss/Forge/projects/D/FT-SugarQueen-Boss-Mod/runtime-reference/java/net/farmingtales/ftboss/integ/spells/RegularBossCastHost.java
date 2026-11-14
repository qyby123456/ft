/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.integ.spells;

import net.farmingtales.ftboss.util.BossSpellRules;

public interface RegularBossCastHost {
    public static final String ATTACK_ACTION = "signature_cast";
    public static final String SUPPORT_ACTION = "signature_self_cast";
    public static final int MIN_VISUAL_TICKS = 30;
    public static final int RELEASE_TICKS = 8;

    public void startSignatureCast(BossSpellRules.Kind var1, int var2);

    public static String action(BossSpellRules.Kind kind) {
        return BossSpellRules.support(kind) ? SUPPORT_ACTION : ATTACK_ACTION;
    }

    public static int visualTicks(int castTicks) {
        return Math.max(30, Math.max(0, castTicks) + 8);
    }
}

