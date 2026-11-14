/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class ManorBaseMovementSpeed {
    public static final double MULTIPLIER = 1.3;

    private ManorBaseMovementSpeed() {
    }

    public static double scale(double d, boolean bl) {
        return bl ? d : d * 1.3;
    }
}

