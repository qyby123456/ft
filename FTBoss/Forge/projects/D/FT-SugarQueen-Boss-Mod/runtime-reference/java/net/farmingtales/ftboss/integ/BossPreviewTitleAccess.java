/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.player.Player
 */
package net.farmingtales.ftboss.integ;

import java.util.Optional;
import net.minecraft.world.entity.player.Player;

public final class BossPreviewTitleAccess {
    private BossPreviewTitleAccess() {
    }

    public static boolean wearingManorTitle(Player player) {
        try {
            Object object;
            Object object2;
            Class<?> clazz = Class.forName("aurilux.titles.common.core.TitleManager");
            Object object3 = clazz.getMethod("getCapability", Player.class).invoke(null, player);
            if (object3 == null) {
                return false;
            }
            Object object4 = object3.getClass().getMethod("resolve", new Class[0]).invoke(object3, new Object[0]);
            if (object4 instanceof Optional) {
                object2 = (Optional)object4;
                v0 = ((Optional)object2).orElse(null);
            } else {
                v0 = object = null;
            }
            if (object == null) {
                return false;
            }
            object2 = object.getClass().getMethod("getDisplayTitle", new Class[0]).invoke(object, new Object[0]);
            if (object2 == null) {
                return false;
            }
            Object object5 = object2.getClass().getMethod("getID", new Class[0]).invoke(object2, new Object[0]);
            return object5 != null && "farmingtales:sweet_apple_manor".equals(object5.toString());
        }
        catch (LinkageError | ReflectiveOperationException throwable) {
            return false;
        }
    }
}

