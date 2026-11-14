package net.farmingtales.ftboss.youzi.spells;

import net.farmingtales.ftboss.youzi.YouziHomingFlight.Vector;

public final class YouziVisibility {
    public static double opacity(Vector var0, Vector var1, Vector var2, boolean var3) {
        return 1.0;
    }

    public static double smooth(double var0) {
        double var2 = Math.max(0.0, Math.min(1.0, var0));
        return var2 * var2 * (3.0 - 2.0 * var2);
    }
}
