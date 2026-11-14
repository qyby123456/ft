/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorCeremonyGeometry;

public final class ManorVictoryGeometry {
    public static final int DURATION = 120;

    private ManorVictoryGeometry() {
    }

    public static void draw(double age, boolean reduced, Sink sink) {
        ManorCeremonyGeometry.victory(age, reduced, sink::vertex);
    }

    @FunctionalInterface
    public static interface Sink {
        public void vertex(double var1, double var3, double var5, int var7, float var8);
    }
}

