/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.phys.Vec3
 *  org.joml.Matrix4f
 *  org.joml.Quaternionf
 *  org.joml.Quaternionfc
 */
package net.farmingtales.ftboss.client.xuanlanarcane;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.xuanlanarcane.ArcaneForms;
import net.farmingtales.ftboss.client.xuanlannative.NativeFacing;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

public final class ArcaneRhythm {
    private static final double[] TICKS = new double[]{0.0, 15.0, 27.5, 39.166};
    private static final double[] PITCH = new double[]{0.0, -115.9351, 134.06, 0.0};
    private static final double[] FINISH = new double[]{28.0, 14.4, 22.4, 19.2};

    private ArcaneRhythm() {
    }

    public static double ease(double d) {
        double d2 = Double.isFinite(d) ? Math.max(0.0, Math.min(1.0, d)) : 0.0;
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double nativeSwing(double d) {
        double d2 = Math.max(0.0, d % 39.166);
        for (int i = 1; i < TICKS.length; ++i) {
            if (!(d2 <= TICKS[i])) continue;
            return Math.toRadians(PITCH[i - 1] + (PITCH[i] - PITCH[i - 1]) * ArcaneRhythm.ease((d2 - TICKS[i - 1]) / (TICKS[i] - TICKS[i - 1])));
        }
        return 0.0;
    }

    public static double spin(ArcaneForms.Joint joint, double d) {
        return joint.rhythm().equals("focus") ? 0.2 * Math.sin(d * 0.13) : d * (joint.slot() % 2 == 0 ? 0.047 : -0.038) + ArcaneRhythm.nativeSwing(d) * 0.018;
    }

    public static double growth(ArcaneForms.Joint joint, double d) {
        return joint.rhythm().equals("stagger") ? 0.68 + 0.32 * ArcaneRhythm.ease(d / FINISH[joint.slot()]) : (joint.rhythm().equals("focus") ? 0.88 + 0.12 * ArcaneRhythm.ease(d / 6.0) : 1.0);
    }

    public static double recoil(double d) {
        if (!Double.isFinite(d) || d < 0.0 || d >= 8.0) {
            return 0.0;
        }
        return -0.1 * ArcaneRhythm.ease(d <= 2.5 ? d / 2.5 : (8.0 - d) / 5.5);
    }

    public static double size(String string, double d, double d2) {
        double d3 = Double.isFinite(d) ? Math.sqrt(Math.max(0.5, Math.min(2.0, d))) : 1.0;
        double d4 = (string.equals("magnetic_storm") ? 1.25 : 1.0) * d3;
        return string.equals("orbital_gears") ? Math.max(d4, (Math.max(0.1, d2) * 0.5 + 0.26) / 0.72) : d4;
    }

    public static double muzzle(ArcaneForms.Spell spell, double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        double d8 = 0.0;
        for (ArcaneForms.Layer layer : spell.layers()) {
            if (layer.role().equals("beam")) continue;
            for (ArcaneForms.Facet facet : layer.facets()) {
                for (float[] fArray : facet.position()) {
                    d8 = Math.min(d8, (double)fArray[2]);
                }
            }
        }
        double d9 = (Math.abs(d5) + Math.abs(d7)) * d2 * 0.5 + d6 * (d6 >= 0.0 ? d3 - d4 : -d4);
        return Math.max(1.35, d9 + 0.42 - d8 * d);
    }

    public static Matrix4f transform(ArcaneForms.Spell spell, ArcaneForms.Layer layer, double d, double d2, double d3, double d4, Quaternionf quaternionf, double d5, double d6) {
        Matrix4f matrix4f = new Matrix4f().translation((float)d2, (float)d3, (float)d4).rotate((Quaternionfc)quaternionf).scale((float)d5, (float)d5, (float)d6);
        if (layer.joint() >= 0) {
            ArcaneForms.Joint joint = spell.joints().get(layer.joint());
            float[] fArray = joint.pivot();
            float[] fArray2 = joint.axis();
            matrix4f.translate(fArray[0], fArray[1], fArray[2]).rotate((float)ArcaneRhythm.spin(joint, d), fArray2[0], fArray2[1], fArray2[2]).scale((float)ArcaneRhythm.growth(joint, d)).translate(-fArray[0], -fArray[1], -fArray[2]);
        }
        return matrix4f;
    }

    public static Matrix4f relative(ArcaneForms.Spell spell, ArcaneForms.Layer layer, double d, double d2, double d3, double d4, Vec3 vec3, Quaternionf quaternionf, double d5, double d6) {
        return ArcaneRhythm.transform(spell, layer, d, d2 - vec3.f_82479_, d3 - vec3.f_82480_, d4 - vec3.f_82481_, quaternionf, d5, d6);
    }

    public static boolean wholeLayer(ArcaneForms.Layer layer, int n) {
        return layer.facets().size() <= n;
    }

    public static float nearFade(double d) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        return (float)ArcaneRhythm.ease((d - 0.18) / 0.55);
    }

    public static float pulse(ArcaneForms.Layer layer, double d) {
        return layer.role().equals("solid") ? 1.0f : (float)(0.83 + 0.17 * Math.sin(d * 0.19 + (double)layer.tile() * 0.71));
    }

    public static float viewWeight(String string, boolean bl, boolean bl2) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        return !bl || bl2 ? 1.0f : (string.equals("solid") ? 0.0f : 0.4f);
    }

    public static Quaternionf orientation(Vec3 vec3) {
        return NativeFacing.orientation(vec3);
    }

    public static Quaternionf orientPositiveZ(Quaternionf quaternionf, float f, float f2, float f3, float f4, float f5, float f6) {
        return NativeFacing.orientPositiveZ(quaternionf, f, f2, f3, f4, f5, f6);
    }
}

