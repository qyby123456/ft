package net.farmingtales.ftboss.client.xuanlanbody;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public final class BodyModelAngles {
    private BodyModelAngles() {
    }

    public static Vector3f zyx(Quaternionf var0, Vector3f var1) {
        double var2 = (double)var0.x;
        double var4 = (double)var0.y;
        double var6 = (double)var0.z;
        double var8 = (double)var0.w;
        double var10 = var2 * var2 + var4 * var4 + var6 * var6 + var8 * var8;
        if (Double.isFinite(var10) && !(var10 < 1.0E-24)) {
            double var12 = 2.0 / var10;
            double var14 = 1.0 - var12 * (var4 * var4 + var6 * var6);
            double var16 = var12 * (var2 * var4 + var8 * var6);
            double var18 = var12 * (var2 * var6 - var8 * var4);
            double var20 = var12 * (var4 * var6 + var8 * var2);
            double var22 = 1.0 - var12 * (var2 * var2 + var4 * var4);
            double var24 = Math.hypot(var14, var16);
            double var26;
            double var28;
            if (var24 > 1.0E-6) {
                var26 = Math.atan2(var20, var22);
                var28 = Math.atan2(var16, var14);
            } else {
                double var30 = 1.0 - var12 * (var2 * var2 + var6 * var6);
                double var32 = var12 * (var4 * var6 - var8 * var2);
                var26 = Math.atan2(-var32, var30);
                var28 = 0.0;
            }

            double var36 = Math.atan2(-var18, var24);
            double var37 = var26 >= 0.0 ? var26 - Math.PI : var26 + Math.PI;
            double var34 = var28 >= 0.0 ? var28 - Math.PI : var28 + Math.PI;
            if (var37 * var37 + var34 * var34 < var26 * var26 + var28 * var28) {
                var26 = var37;
                var28 = var34;
                var36 = var36 >= 0.0 ? Math.PI - var36 : -Math.PI - var36;
            }

            return var1.set((float)var26, (float)var36, (float)var28);
        } else {
            return var1.set(0.0F, 0.0F, 0.0F);
        }
    }
}
