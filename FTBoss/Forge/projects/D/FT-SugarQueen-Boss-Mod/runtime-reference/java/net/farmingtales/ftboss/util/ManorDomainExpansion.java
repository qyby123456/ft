/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.ManorCeremonyModel;

public final class ManorDomainExpansion {
    public static final double DURATION = 64.0;

    private ManorDomainExpansion() {
    }

    public static double smooth(double d) {
        double d2 = Math.max(0.0, Math.min(1.0, d));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double scale(String string, double d) {
        return switch (string) {
            case "sigil" -> ManorDomainExpansion.smooth(d / 12.0);
            case "foundation" -> ManorDomainExpansion.smooth((d - 8.0) / 24.0);
            case "boundary" -> ManorDomainExpansion.smooth((d - 10.0) / 28.0);
            case "monuments" -> ManorDomainExpansion.smooth((d - 26.0) / 22.0);
            default -> 0.0;
        };
    }

    public static double radius(double d) {
        return 0.35 + 8.61 * ManorDomainExpansion.smooth((d - 8.0) / 30.0);
    }

    public static double flare(double d) {
        return ManorDomainExpansion.smooth((d - 28.0) / 12.0) * (1.0 - ManorDomainExpansion.smooth((d - 48.0) / 16.0));
    }

    public static void emit(int n, int n2, int n3, double d, boolean bl, float f, ManorCeremonyModel.Sink sink) {
        if (!Double.isFinite(d) || !Float.isFinite(f) || d < 0.0 || f <= 0.001f) {
            return;
        }
        f = Math.min(1.0f, f);
        int n4 = bl ? 64 : 128;
        double d2 = ManorDomainExpansion.smooth(d / 6.0) * (1.0 - ManorDomainExpansion.smooth((d - 42.0) / 18.0));
        if (d2 > 0.001) {
            ManorDomainExpansion.annulus(sink, ManorDomainExpansion.radius(d), 0.045 + d2 * 0.065, 0.17, n4, n2, f * (float)(d2 * 0.8));
            ManorDomainExpansion.annulus(sink, Math.max(0.25, ManorDomainExpansion.radius(d) - 0.22), 0.018, 0.19, n4, n3, f * (float)(d2 * 0.38));
        }
        double d3 = 0.7 + 0.15 * Math.sin(d * 0.035 + (double)n);
        double d4 = ManorDomainExpansion.smooth((d - 36.0) / 22.0);
        int n5 = bl ? 8 : 16;
        for (int i = 0; d4 > 0.001 && i < n5; ++i) {
            double d5 = (double)i * Math.PI * 2.0 / (double)n5 + d * 0.003 * (n % 2 == 0 ? 1.0 : -1.0);
            ManorDomainExpansion.arc(sink, 8.97, 0.044, d5, d5 + 0.045, 0.18, 2, n2, f * (float)(d4 * d3 * 0.6));
        }
        double d6 = ManorDomainExpansion.flare(d);
        if (d6 <= 0.001) {
            return;
        }
        int n6 = bl ? 6 : 12;
        double d7 = (double)(n % 3 - 1) * 0.24;
        for (int i = 0; i < n6; ++i) {
            double d8 = (double)i * Math.PI * 2.0 / (double)n6 + (double)n * 0.17;
            double d9 = 1.5 + 0.5 * Math.sin((double)i * 1.7 + (double)n);
            int n7 = bl ? 10 : 18;
            for (int j = 0; j < n7; ++j) {
                double d10 = (double)j / (double)n7;
                double d11 = (double)(j + 1) / (double)n7;
                ManorDomainExpansion.ribbon(sink, d8, d7, d10, d11, d9 * d6, n2, f * (float)(d6 * Math.sin((d10 + d11) * Math.PI * 0.5) * 0.3));
            }
            double d12 = d6 * (0.6 + d9 * 0.55);
            double d13 = 0.055;
            double d14 = 8.7;
            double d15 = Math.cos(d8) * d13;
            double d16 = -Math.sin(d8) * d13;
            double d17 = Math.sin(d8) * d14;
            double d18 = Math.cos(d8) * d14;
            ManorDomainExpansion.quad(sink, d17 - d15, 0.22, d18 - d16, d17 + d15, 0.22, d18 + d16, d17 + d15 * 0.15, d12, d18 + d16 * 0.15, d17 - d15 * 0.15, d12, d18 - d16 * 0.15, n3, f * (float)(d6 * 0.28));
        }
    }

    private static void ribbon(ManorCeremonyModel.Sink sink, double d, double d2, double d3, double d4, double d5, int n, float f) {
        double d6 = 1.4 + 7.3 * d3;
        double d7 = 1.4 + 7.3 * d4;
        double d8 = d + d2 * Math.sin(d3 * Math.PI);
        double d9 = d + d2 * Math.sin(d4 * Math.PI);
        double d10 = 0.22 + Math.sin(d3 * Math.PI) * d5;
        double d11 = 0.22 + Math.sin(d4 * Math.PI) * d5;
        double d12 = 0.025;
        ManorDomainExpansion.quad(sink, Math.sin(d8 - d12 / d6) * d6, d10, Math.cos(d8 - d12 / d6) * d6, Math.sin(d8 + d12 / d6) * d6, d10, Math.cos(d8 + d12 / d6) * d6, Math.sin(d9 + d12 / d7) * d7, d11, Math.cos(d9 + d12 / d7) * d7, Math.sin(d9 - d12 / d7) * d7, d11, Math.cos(d9 - d12 / d7) * d7, n, f);
    }

    private static void annulus(ManorCeremonyModel.Sink sink, double d, double d2, double d3, int n, int n2, float f) {
        ManorDomainExpansion.arc(sink, d, d2, 0.0, Math.PI * 2, d3, n, n2, f);
    }

    private static void arc(ManorCeremonyModel.Sink sink, double d, double d2, double d3, double d4, double d5, int n, int n2, float f) {
        for (int i = 0; i < n; ++i) {
            double d6 = d3 + (d4 - d3) * (double)i / (double)n;
            double d7 = d3 + (d4 - d3) * (double)(i + 1) / (double)n;
            double d8 = d - d2 * 0.5;
            double d9 = d + d2 * 0.5;
            ManorDomainExpansion.quad(sink, Math.sin(d6) * d8, d5, Math.cos(d6) * d8, Math.sin(d6) * d9, d5, Math.cos(d6) * d9, Math.sin(d7) * d9, d5, Math.cos(d7) * d9, Math.sin(d7) * d8, d5, Math.cos(d7) * d8, n2, f);
        }
    }

    private static void quad(ManorCeremonyModel.Sink sink, double d, double d2, double d3, double d4, double d5, double d6, double d7, double d8, double d9, double d10, double d11, double d12, int n, float f) {
        if (f <= 0.001f) {
            return;
        }
        sink.vertex(d, d2, d3, n, f);
        sink.vertex(d4, d5, d6, n, f);
        sink.vertex(d7, d8, d9, n, f);
        sink.vertex(d10, d11, d12, n, f);
    }
}

