/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.farmingtales.ftboss.client.xuanlannative;

import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class NativeFacing {
    private NativeFacing() {
    }

    public static Quaternionf orientation(Vec3 vec3) {
        if (!(vec3 != null && Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_))) {
            return new Quaternionf();
        }
        double d = Math.hypot(vec3.f_82479_, vec3.f_82481_);
        if (Math.hypot(d, vec3.f_82480_) < 1.0E-12) {
            return new Quaternionf();
        }
        float f = d == 0.0 ? 0.0f : (float)Math.atan2(vec3.f_82479_, vec3.f_82481_);
        float f2 = (float)(-Math.atan2(vec3.f_82480_, d));
        return new Quaternionf().rotationY(f).rotateX(f2).normalize();
    }

    public static Vec3 projectileDirection(Vec3 vec3, float f, float f2) {
        if (vec3 != null && Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_) && vec3.m_82556_() > 1.0E-12) {
            return vec3;
        }
        double d = Math.toRadians(Float.isFinite(f) ? (double)f : 0.0);
        double d2 = Math.toRadians(Float.isFinite(f2) ? (double)f2 : 0.0);
        return new Vec3(Math.sin(d) * Math.cos(d2), -Math.sin(d2), Math.cos(d) * Math.cos(d2));
    }

    public static Quaternionf orientPositiveZ(Quaternionf quaternionf, float f, float f2, float f3, float f4, float f5, float f6) {
        if (f != 0.0f || f2 != 0.0f || f3 != 1.0f) {
            throw new IllegalArgumentException("Only the reviewed local +Z forward axis is supported");
        }
        return quaternionf.set((Quaternionfc)NativeFacing.orientation(new Vec3((double)f4, (double)f5, (double)f6)));
    }

    public static Matrix4f frame(Vec3 vec3, Vec3 vec32, double d, double d2, double d3) {
        return new Matrix4f().translation((float)vec3.f_82479_, (float)vec3.f_82480_, (float)vec3.f_82481_).rotate((Quaternionfc)NativeFacing.orientation(vec32)).scale((float)d, (float)d2, (float)d3);
    }

    public static Matrix4f projectile(Vec3 vec3, double d, float f) {
        return new Matrix4f().rotate((Quaternionfc)NativeFacing.orientation(vec3)).rotateZ((float)Math.toRadians(d * 25.0)).scale(f);
    }

    public static boolean useDedicated(boolean bl, boolean bl2) {
        return bl && bl2;
    }
}

