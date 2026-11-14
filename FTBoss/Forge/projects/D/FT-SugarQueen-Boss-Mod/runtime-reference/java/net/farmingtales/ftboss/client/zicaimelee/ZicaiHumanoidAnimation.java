/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.zicaimelee;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class ZicaiHumanoidAnimation {
    private ZicaiHumanoidAnimation() {
    }

    public static boolean accepts(int n, boolean bl, boolean bl2, String string, float f, float f2) {
        return n == 8 && bl && !bl2 && "ftboss:puffer_bat".equals(string) && Float.isFinite(f) && Float.isFinite(f2) && f > 0.0f && f2 >= 0.0f && f2 <= 1.0f;
    }

    public static void apply(EasterBossModel easterBossModel, EasterBossEntity easterBossEntity, float f, float f2, float f3, float f4, float f5) {
    }
}

