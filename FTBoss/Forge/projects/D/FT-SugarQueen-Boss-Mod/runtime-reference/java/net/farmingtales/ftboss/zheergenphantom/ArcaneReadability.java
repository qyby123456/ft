/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenphantom;

import net.farmingtales.ftboss.util.SpellVisualRules;

public final class ArcaneReadability {
    private ArcaneReadability() {
    }

    public static int lifetime(int n, SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if ("shadow_brand".equals(spell.id) && phase != SpellVisualRules.Phase.CHARGE && phase != SpellVisualRules.Phase.BEAM) {
            return Math.max(60, n);
        }
        return phase == SpellVisualRules.Phase.CHARGE && ("nightfall_blades".equals(spell.id) || "void_echo".equals(spell.id) || "shadow_brand".equals(spell.id)) ? Math.max(20, n) : n;
    }
}

