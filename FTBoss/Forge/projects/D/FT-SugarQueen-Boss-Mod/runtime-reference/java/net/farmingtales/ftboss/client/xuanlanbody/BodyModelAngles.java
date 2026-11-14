/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Quaternionf
 *  org.joml.Vector3f
 */
package net.farmingtales.ftboss.client.xuanlanbody;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BodyModelAngles {
    private BodyModelAngles() {
    }

    public static Vector3f zyx(Quaternionf quaternionf, Vector3f vector3f) {
        double d;
        double d2;
        double d3;
        double d4;
        double d5;
        double d6 = quaternionf.x;
        double d7 = quaternionf.y;
        double d8 = quaternionf.z;
        double d9 = quaternionf.w;
        double d10 = d6 * d6 + d7 * d7 + d8 * d8 + d9 * d9;
        if (!Double.isFinite(d10) || d10 < 1.0E-24) {
            return vector3f.set(0.0f, 0.0f, 0.0f);
        }
        double d11 = 2.0 / d10;
        double d12 = 1.0 - d11 * (d7 * d7 + d8 * d8);
        double d13 = d11 * (d6 * d7 + d9 * d8);
        double d14 = d11 * (d6 * d8 - d9 * d7);
        double d15 = d11 * (d7 * d8 + d9 * d6);
        double d16 = 1.0 - d11 * (d6 * d6 + d7 * d7);
        double d17 = Math.hypot(d12, d13);
        if (d17 > 1.0E-6) {
            d5 = Math.atan2(d15, d16);
            d4 = Math.atan2(d13, d12);
        } else {
            d3 = 1.0 - d11 * (d6 * d6 + d8 * d8);
            d2 = d11 * (d7 * d8 - d9 * d6);
            d5 = Math.atan2(-d2, d3);
            d4 = 0.0;
        }
        d3 = Math.atan2(-d14, d17);
        d2 = d5 >= 0.0 ? d5 - Math.PI : d5 + Math.PI;
        double d18 = d = d4 >= 0.0 ? d4 - Math.PI : d4 + Math.PI;
        if (d2 * d2 + d * d < d5 * d5 + d4 * d4) {
            d5 = d2;
            d4 = d;
            d3 = d3 >= 0.0 ? Math.PI - d3 : -Math.PI - d3;
        }
        return vector3f.set((float)d5, (float)d3, (float)d4);
    }
}

