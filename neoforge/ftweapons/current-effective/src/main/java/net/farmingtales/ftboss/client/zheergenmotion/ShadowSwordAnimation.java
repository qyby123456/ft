package net.farmingtales.ftboss.client.zheergenmotion;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class ShadowSwordAnimation {
    private ShadowSwordAnimation() {
    }

    public static boolean accepts(int var0, boolean var1, boolean var2, boolean var3, boolean var4, String var5, float var6, float var7) {
        return var0 == 7
            && !var1
            && !var2
            && var3
            && !var4
            && "ftboss:shadow_sword".equals(var5)
            && Float.isFinite(var6)
            && Float.isFinite(var7)
            && var6 >= 0.0F
            && var6 <= 1.0F
            && var7 > 0.0F
            && var7 <= 1.0F;
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
    }
}
