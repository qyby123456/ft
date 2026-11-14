package net.farmingtales.ftboss.client.zicaimelee;

import net.farmingtales.ftboss.client.model.EasterBossModel;
import net.farmingtales.ftboss.entity.EasterBossEntity;

public final class ZicaiHumanoidAnimation {
    private ZicaiHumanoidAnimation() {
    }

    public static boolean accepts(int var0, boolean var1, boolean var2, String var3, float var4, float var5) {
        return var0 == 8
            && var1
            && !var2
            && "ftboss:puffer_bat".equals(var3)
            && Float.isFinite(var4)
            && Float.isFinite(var5)
            && var4 > 0.0F
            && var5 >= 0.0F
            && var5 <= 1.0F;
    }

    public static void apply(EasterBossModel var0, EasterBossEntity var1, float var2, float var3, float var4, float var5, float var6) {
    }
}
