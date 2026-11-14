/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.ManorCeremonyGeometry;

public final class ManorIntroMesh {
    private ManorIntroMesh() {
    }

    public static void emit(double elapsedMillis, boolean reduced, Sink sink) {
        ManorCeremonyGeometry.intro(elapsedMillis, reduced, sink::vertex);
    }

    @FunctionalInterface
    public static interface Sink {
        public void vertex(double var1, double var3, double var5, int var7, float var8);
    }
}

