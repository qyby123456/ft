/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.youzi.spells;

import net.farmingtales.ftboss.youzi.YouziHomingFlight;

public final class YouziVisibility {
    public static double opacity(YouziHomingFlight.Vector vector, YouziHomingFlight.Vector vector2, YouziHomingFlight.Vector vector3, boolean bl) {
        return 1.0;
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }
}

