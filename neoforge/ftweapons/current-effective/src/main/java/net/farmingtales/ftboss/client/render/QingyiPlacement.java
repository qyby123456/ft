package net.farmingtales.ftboss.client.render;

import org.joml.Matrix4f;

public final class QingyiPlacement {
    private QingyiPlacement() {
    }

    public static Matrix4f frame(double var0, double var2, double var4, double var6, double var8, double var10) {
        double var12 = QingyiWaveTiming.scale(var10);
        double var14 = Math.hypot(var6, var8);
        double var16 = var14 < 1.0E-5 ? 0.0 : Math.atan2(var6, var8);
        return new Matrix4f().translation((float)var0, (float)var2, (float)var4).rotateY((float)var16).scale((float)var12);
    }
}
