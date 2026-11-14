/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.QingyiWaveTiming;
import org.joml.Matrix4f;

public final class QingyiPlacement {
    private QingyiPlacement() {
    }

    public static Matrix4f frame(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = QingyiWaveTiming.scale(d6);
        double d8 = Math.hypot(d4, d5);
        double d9 = d8 < 1.0E-5 ? 0.0 : Math.atan2(d4, d5);
        return new Matrix4f().translation((float)d, (float)d2, (float)d3).rotateY((float)d9).scale((float)d7);
    }
}

