/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class XuanlanGunPoseMath {
    public static final float REST_ARM_PITCH = -0.4f;

    private XuanlanGunPoseMath() {
    }

    public static boolean activeCue(float age, int duration) {
        return age >= 0.0f && duration > 0 && age < (float)duration;
    }

    public static float armPitch(float aimPitch) {
        return aimPitch - 1.5707964f;
    }

    public static float beamPitch(double directionX, double directionY, double directionZ) {
        return (float)(-Math.atan2(directionY, Math.hypot(directionX, directionZ)));
    }

    public static float beamYaw(double directionX, double directionZ, float bodyYawDegrees) {
        double relative = -Math.atan2(directionX, directionZ) - Math.toRadians(bodyYawDegrees);
        return (float)Math.atan2(Math.sin(relative), Math.cos(relative));
    }
}

