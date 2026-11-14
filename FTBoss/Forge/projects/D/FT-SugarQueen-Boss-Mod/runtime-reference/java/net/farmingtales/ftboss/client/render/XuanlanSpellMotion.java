/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.client.render.FirstPersonSpellVisibility;
import net.farmingtales.ftboss.client.render.XuanlanNativeTiming;
import net.farmingtales.ftboss.client.render.XuanlanSpellMesh;

public final class XuanlanSpellMotion {
    private XuanlanSpellMotion() {
    }

    public static boolean supports(String string) {
        return "orbital_gears".equals(string) || "pressure_lance".equals(string) || "magnetic_storm".equals(string);
    }

    public static double unfold(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, Double.isFinite(d) ? d / 6.0 : 0.0));
        return 0.88 + 0.12 * d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double scale(String string, double d, double d2) {
        double d3;
        double d4 = d3 = Double.isFinite(d) ? Math.max(0.5, Math.min(2.0, d)) : 1.0;
        double d5 = "pressure_lance".equals(string) ? 1.45 : ("magnetic_storm".equals(string) ? 1.7 : 1.48);
        return d5 * Math.sqrt(d3) * XuanlanSpellMotion.unfold(d2);
    }

    public static void local(XuanlanSpellMesh.Face face, int n, double d, double[] dArray) {
        System.arraycopy(face.points()[n], 0, dArray, 0, 3);
        if (face.bone() != 0) {
            double d2 = d * (face.bone() == 3 ? 0.085 : -0.071) + XuanlanNativeTiming.secondarySwing(d);
            double d3 = face.part().equals("four_counterrotating_flux_satellites") ? XuanlanNativeTiming.shardScale(face.pivot(), d) : 1.0;
            double d4 = (dArray[0] - face.pivot()[0]) * d3;
            double d5 = (dArray[1] - face.pivot()[1]) * d3;
            dArray[0] = face.pivot()[0] + d4 * Math.cos(d2) - d5 * Math.sin(d2);
            dArray[1] = face.pivot()[1] + d4 * Math.sin(d2) + d5 * Math.cos(d2);
            dArray[2] = face.pivot()[2] + (dArray[2] - face.pivot()[2]) * d3;
        }
    }

    public static float cameraFade(double d) {
        if (FirstPersonSpellVisibility.firstPerson()) {
            return 1.0f;
        }
        if (!Double.isFinite(d) || d <= 0.16) {
            return 0.0f;
        }
        double d2 = Math.min(1.0, (d - 0.16) / 0.56);
        return (float)(d2 * d2 * (3.0 - 2.0 * d2));
    }

    public static double recoil(double d) {
        return Double.isFinite(d) ? XuanlanNativeTiming.rayRecoil(d) : 0.0;
    }

    public static double orbitalScale(double d, double d2) {
        return Math.max(d, (Math.max(0.1, d2) * 0.5 + 0.3) / 0.72);
    }

    public static double forwardOffset(XuanlanSpellMesh.Model model, double d, double d2, double d3, double d4, double d5, double d6, double d7) {
        double d8 = 0.0;
        for (XuanlanSpellMesh.Face face : model.faces()) {
            if (face.group().startsWith("beam")) continue;
            for (double[] dArray : face.points()) {
                d8 = Math.min(d8, dArray[2]);
            }
        }
        double d9 = (Math.abs(d5) + Math.abs(d7)) * Math.max(0.1, d2) * 0.5 + d6 * (d6 >= 0.0 ? d3 - d4 + 0.22 : -d4 + 0.22);
        return Math.max(1.75, d9 + 0.45 - d8 * d);
    }
}

