/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.zheergenmotion;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class ShadowSwordAnimation {
    private ShadowSwordAnimation() {
    }

    public static boolean accepts(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4, String string, float f, float f2) {
        return n == 7 && !bl && !bl2 && bl3 && !bl4 && "ftboss:shadow_sword".equals(string) && Float.isFinite(f) && Float.isFinite(f2) && f >= 0.0f && f <= 1.0f && f2 > 0.0f && f2 <= 1.0f;
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
    }
}

