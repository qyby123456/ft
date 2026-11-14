/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.saya;

import net.farmingtales.ftboss.integ.spells.RegularBossCastHost;
import net.farmingtales.ftboss.saya.SayaCastMotion;
import net.farmingtales.ftboss.util.BossSpellRules;

public final class SayaCastSync {
    private SayaCastSync() {
    }

    public static String action(BossSpellRules.Kind kind, int n) {
        return SayaCastMotion.index(kind.spell) < 0 ? RegularBossCastHost.action(kind) : SayaCastMotion.encode(kind.spell, Math.max(1, n));
    }

    public static int duration(int n) {
        return Math.max(1, n) + 8;
    }
}

