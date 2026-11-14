package net.farmingtales.ftboss.integ;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public final class BossPreviewTitleAccess {
    private BossPreviewTitleAccess() {
    }

    public static boolean wearingManorTitle(Player var0) {
        try {
            Class var1 = Class.forName("aurilux.titles.common.core.TitleManager");
            Object var2 = var1.getMethod("getCapability", Player.class).invoke(null, var0);
            if (var2 == null) {
                return false;
            } else {
                Object var4 = var2.getClass().getMethod("resolve").invoke(var2) instanceof Optional var5 ? var5.orElse(null) : null;
                if (var4 == null) {
                    return false;
                } else {
                    Object var8 = var4.getClass().getMethod("getDisplayTitle").invoke(var4);
                    if (var8 == null) {
                        return false;
                    } else {
                        Object var6 = var8.getClass().getMethod("getID").invoke(var8);
                        return var6 != null && "farmingtales:sweet_apple_manor".equals(var6.toString());
                    }
                }
            }
        } catch (LinkageError | ReflectiveOperationException var7) {
            return false;
        }
    }
}
