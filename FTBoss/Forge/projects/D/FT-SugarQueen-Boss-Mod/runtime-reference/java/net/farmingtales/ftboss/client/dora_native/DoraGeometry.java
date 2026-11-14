/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.dora_native;

import java.util.ArrayList;
import java.util.List;

public final class DoraGeometry {
    public static final int CYAN = 4250329;
    public static final int ICE = 0xDFFFF6;
    public static final int GOLD = 16103514;
    public static final int MAX_QUADS = 216;

    private DoraGeometry() {
    }

    public static double bounded(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static Mesh clipForward(Mesh mesh, double d) {
        for (Face face : mesh.faces) {
            for (int i = 2; i < 12; i += 3) {
                face.points()[i] = Math.max(0.0, Math.min(d, face.points()[i]));
            }
        }
        return mesh;
    }

    public static Mesh shot(String string, double d, double d2, double d3, double d4, int n) {
        int n2;
        boolean bl;
        Mesh mesh = new Mesh();
        if (string == null || !List.of("DORA_CHARGE", "DORA_PULSE", "DORA_NOVA", "DORA_TRACER", "DORA_IMPACT").contains(string)) {
            return mesh;
        }
        if (!Double.isFinite(d) || !Double.isFinite(d2) || !Double.isFinite(d4) || d < 0.0 || d2 <= 0.0 || d >= d2 || !Double.isFinite(d3) || d3 < 0.0) {
            return mesh;
        }
        double d5 = DoraGeometry.bounded(d / d2);
        double d6 = Math.pow(1.0 - d5, 1.7);
        if (string.equals("DORA_IMPACT")) {
            double d7 = Math.max(0.1, Math.min(1.25, d4)) * (0.18 + d5 * 0.95);
            mesh.dart(0.0, 0.0, 0.0, d7 * 0.25, d7 * 0.36, 0xDFFFF6, d6);
            int n3 = n > 0 ? 8 : 4;
            for (int i = 0; i < n3; ++i) {
                double d8 = (double)i * Math.PI * 2.0 / (double)n3 + 0.24;
                double d9 = Math.cos(d8);
                double d10 = Math.sin(d8);
                mesh.quad(new double[]{d9 * d7 * 0.2 - d10 * 0.024, d10 * d7 * 0.2 + d9 * 0.024, 0.0}, new double[]{d9 * d7 * 0.2 + d10 * 0.024, d10 * d7 * 0.2 - d9 * 0.024, 0.0}, new double[]{d9 * d7, d10 * d7, 0.12 * Math.sin(i)}, new double[]{d9 * d7 * 0.78, d10 * d7 * 0.78, 0.02}, i % 2 == 0 ? 4250329 : 16103514, d6 * 0.8);
                mesh.box(d9 * d7 * 0.85, d10 * d7 * 0.85, 0.08 * Math.cos(i), 0.028, 0.042, 0.07, 16103514, d6 * 0.7);
            }
            return mesh;
        }
        if (string.equals("DORA_CHARGE")) {
            double d11 = 0.09 + 0.17 * (1.0 - d5);
            mesh.bracket(Math.min(0.5, d3 * 0.3), d11, d11 * 1.3, 4250329, d6 * 0.75);
            mesh.dart(0.0, 0.0, Math.min(0.4, d3 * 0.2), 0.055, 0.08, 0xDFFFF6, d6);
            return DoraGeometry.clipForward(mesh, d3);
        }
        if (d3 < 0.05) {
            return mesh;
        }
        double d12 = Math.min(0.62, d3 * 0.28);
        double d13 = Math.max(0.01, d3 - d12);
        boolean bl2 = string.equals("DORA_NOVA");
        boolean bl3 = bl = bl2 || string.equals("DORA_PULSE");
        double d14 = bl ? (bl2 ? 0.085 : 0.055) : 0.028;
        mesh.streak(0.0, 0.0, d12, d3, d14 * 1.8, 4250329, d6 * 0.24);
        mesh.streak(0.0, 0.0, d12, d3, d14 * 0.28, 0xDFFFF6, d6 * 0.84);
        int n4 = n > 0 ? (bl2 ? 5 : 3) : 1;
        for (n2 = 0; n2 < n4; ++n2) {
            double d15 = (d5 * (bl2 ? 1.4 : 1.9) + (double)n2 / (double)n4) % 1.0;
            double d16 = Math.min(0.24, d13 * 0.08);
            double d17 = Math.min(d3 - d16, d12 + d15 * d13);
            mesh.dart(0.0, 0.0, d17, d14 * 1.2, d16, n2 % 2 == 0 ? 0xDFFFF6 : 16103514, d6 * 0.82);
        }
        mesh.bracket(d12, d14 * 2.1, d14 * 2.3, 16103514, d6);
        if (n > 0) {
            for (n2 = -1; n2 <= 1; n2 += 2) {
                mesh.streak((double)n2 * d14 * 2.2, 0.0, d12, Math.min(d3, d12 + 0.42), d14 * 0.24, 16103514, d6 * 0.68);
            }
        }
        return DoraGeometry.clipForward(mesh, d3);
    }

    public static Mesh sniper(double d, double d2, double d3, boolean bl, boolean bl2, int n) {
        Mesh mesh = new Mesh();
        if (!Double.isFinite(d) || !Double.isFinite(d2) || d < 0.0 || d2 <= 0.0 || d >= d2 || !Double.isFinite(d3) || d3 < 0.01) {
            return mesh;
        }
        double d4 = DoraGeometry.bounded(d / d2);
        double d5 = bl ? Math.pow(1.0 - d4, 1.4) : 1.0;
        double d6 = Math.min(0.64, d3 * 0.25);
        if (bl) {
            mesh.streak(0.0, 0.0, d6, d3, 0.082 * (1.0 - d4) + 0.015, 4250329, d5 * 0.45);
            mesh.streak(0.0, 0.0, d6, d3, 0.014, 0xDFFFF6, d5);
            int n2 = n > 0 ? 7 : 3;
            for (int i = 0; i < n2; ++i) {
                double d7 = d6 + (d3 - d6) * ((double)i + 0.4) / (double)n2;
                mesh.bracket(d7, 0.14 + d4 * 0.22, 0.19, i % 2 == 0 ? 4250329 : 16103514, d5 * 0.72);
                mesh.dart(0.0, 0.0, d7, 0.062, 0.16, 0xDFFFF6, d5 * 0.65);
            }
            mesh.bracket(d3, 0.18 + d4 * 0.28, 0.35, 16103514, d5 * 0.72);
            return DoraGeometry.clipForward(mesh, d3);
        }
        double d8 = bl2 ? 0.11 : 0.3 - d4 * 0.16;
        for (int i = 0; i < (n > 0 ? 3 : 2); ++i) {
            double d9 = Math.min(d3, d6 + (double)i * 0.14);
            mesh.bracket(d9, d8 + (double)i * 0.033, d8 * 1.7, bl2 ? 16103514 : 4250329, 0.64);
        }
        mesh.dart(0.0, 0.0, d6, 0.045 + d4 * 0.02, 0.085, 0xDFFFF6, 0.72);
        double d10 = bl2 ? 0.5 : 0.14 + d4 * 0.1;
        int n3 = n > 0 ? 12 : 6;
        for (int i = 0; i < n3; ++i) {
            double d11 = d6 + (d3 - d6) * ((double)i + 0.35) / (double)n3;
            mesh.streak(0.0, 0.0, d11, Math.min(d3, d11 + 0.2), 0.009, bl2 ? 16103514 : 4250329, d10);
        }
        mesh.bracket(d3, bl2 ? 0.16 : 0.24, 0.31, bl2 ? 16103514 : 4250329, 0.72);
        return DoraGeometry.clipForward(mesh, d3);
    }

    public static Mesh spear(double d, double d2, int n) {
        Mesh mesh = new Mesh();
        if (!Double.isFinite(d) || d < 0.0 || d >= 4.0 || d2 < 0.1 || !Double.isFinite(d2)) {
            return mesh;
        }
        double d3 = DoraGeometry.bounded(d / 4.0);
        double d4 = Math.pow(1.0 - d3, 1.2);
        double d5 = Math.min(0.34, d2 * 0.22);
        double d6 = Math.max(0.0, Math.min(d2 - d5, 0.75 + 4.9 * Math.sin(d3 * Math.PI * 0.8)));
        mesh.dart(0.0, 0.0, d6, 0.095 * (1.0 - d3 * 0.55), d5, 0xDFFFF6, d4);
        mesh.streak(0.0, 0.0, Math.min(0.65, d2 * 0.25), d6, 0.033, 4250329, d4 * 0.68);
        for (int i = -1; i <= 1; i += 2) {
            mesh.streak((double)i * 0.065, -0.02, Math.max(0.2, d6 - 1.4), d6, 0.016, 16103514, d4 * 0.65);
        }
        if (n > 0 && d2 > 0.4) {
            mesh.bracket(Math.min(d2 - 0.035, Math.max(0.3, d6 - 0.5)), 0.115, 0.13, 4250329, d4 * 0.65);
            mesh.dart(0.0, -0.1, Math.min(d2 - 0.13, Math.max(0.3, d6 - 0.9)), 0.028, 0.13, 16103514, d4 * 0.5);
        }
        return DoraGeometry.clipForward(mesh, d2);
    }

    public static Mesh reload(double d, int n) {
        Mesh mesh = new Mesh();
        if (d < 0.0 || !Double.isFinite(d)) {
            return mesh;
        }
        double d2 = d % 10.0 / 10.0;
        int n2 = n > 0 ? 5 : 3;
        for (int i = 0; i < n2; ++i) {
            double d3 = 0.35 + (double)i * 0.3 + d2 * 0.24;
            double d4 = (0.54 - (double)i * 0.07) * (1.0 - d2 * 0.65);
            for (int j = -1; j <= 1; j += 2) {
                mesh.quad(new double[]{(double)j * 0.22, 0.06, -d3 + 0.16}, new double[]{(double)j * 0.25, 0.06, -d3 + 0.1}, new double[]{(double)j * 0.63, 0.06, -d3 - 0.15}, new double[]{(double)j * 0.57, 0.06, -d3 - 0.15}, 4250329, d4);
                mesh.streak((double)j * 0.3, 0.78, -d3, -d3 - 0.45, 0.021, 0xDFFFF6, d4 * 0.5);
            }
        }
        double d5 = d % 14.0 / 14.0;
        mesh.box(0.38 + d5 * 0.15, 0.91 - d5 * 0.45, -0.18 - d5 * 0.3, 0.07, 0.11, 0.05, 16103514, 0.6 * (1.0 - d5));
        mesh.bracket(0.25, 0.36, 0.2, 16103514, 0.36);
        return mesh;
    }

    public static final class Mesh {
        public final List<Face> faces = new ArrayList<Face>();

        public void quad(double[] dArray, double[] dArray2, double[] dArray3, double[] dArray4, int n, double d) {
            if (this.faces.size() >= 216 || !Double.isFinite(d) || d <= 0.003) {
                return;
            }
            double[] dArray5 = new double[12];
            double[][] dArrayArray = new double[][]{dArray, dArray2, dArray3, dArray4};
            for (int i = 0; i < 4; ++i) {
                for (int j = 0; j < 3; ++j) {
                    double d2 = dArrayArray[i][j];
                    if (!Double.isFinite(d2)) {
                        return;
                    }
                    dArray5[i * 3 + j] = d2;
                }
            }
            this.faces.add(new Face(dArray5, n, (float)Math.min(1.0, d)));
        }

        public void box(double d, double d2, double d3, double d4, double d5, double d6, int n, double d7) {
            double d8 = d - d4 / 2.0;
            double d9 = d + d4 / 2.0;
            double d10 = d2 - d5 / 2.0;
            double d11 = d2 + d5 / 2.0;
            double d12 = d3 - d6 / 2.0;
            double d13 = d3 + d6 / 2.0;
            double[] dArray = new double[]{d8, d10, d12};
            double[] dArray2 = new double[]{d9, d10, d12};
            double[] dArray3 = new double[]{d9, d11, d12};
            double[] dArray4 = new double[]{d8, d11, d12};
            double[] dArray5 = new double[]{d8, d10, d13};
            double[] dArray6 = new double[]{d9, d10, d13};
            double[] dArray7 = new double[]{d9, d11, d13};
            double[] dArray8 = new double[]{d8, d11, d13};
            this.quad(dArray, dArray2, dArray3, dArray4, n, d7);
            this.quad(dArray6, dArray5, dArray8, dArray7, n, d7);
            this.quad(dArray5, dArray, dArray4, dArray8, n, d7);
            this.quad(dArray2, dArray6, dArray7, dArray3, n, d7);
            this.quad(dArray4, dArray3, dArray7, dArray8, n, d7);
            this.quad(dArray5, dArray6, dArray2, dArray, n, d7);
        }

        public void streak(double d, double d2, double d3, double d4, double d5, int n, double d6) {
            this.quad(new double[]{d - d5, d2, d3}, new double[]{d + d5, d2, d3}, new double[]{d + d5 * 0.12, d2, d4}, new double[]{d - d5 * 0.12, d2, d4}, n, d6);
            this.quad(new double[]{d, d2 - d5, d3}, new double[]{d, d2 + d5, d3}, new double[]{d, d2 + d5 * 0.12, d4}, new double[]{d, d2 - d5 * 0.12, d4}, n, d6);
        }

        public void dart(double d, double d2, double d3, double d4, double d5, int n, double d6) {
            double[] dArray = new double[]{d, d2, d3 + d5};
            double[] dArray2 = new double[]{d, d2, d3 - d5 * 0.38};
            for (int i = 0; i < 4; ++i) {
                double d7 = (double)i * Math.PI / 2.0;
                double d8 = d7 + 1.5707963267948966;
                double[] dArray3 = new double[]{d + Math.cos(d7) * d4, d2 + Math.sin(d7) * d4, d3};
                double[] dArray4 = new double[]{d + Math.cos(d8) * d4, d2 + Math.sin(d8) * d4, d3};
                this.quad(dArray3, dArray4, dArray, dArray, n, d6);
                this.quad(dArray4, dArray3, dArray2, dArray2, n, d6 * 0.65);
            }
        }

        public void bracket(double d, double d2, double d3, int n, double d4) {
            for (int i = -1; i <= 1; i += 2) {
                this.glowBar((double)i * d2, 0.0, d, 0.018, d3, 0.035, n, d4);
                this.glowBar((double)i * (d2 - 0.045), d3 * 0.5, d, 0.108, 0.018, 0.035, n, d4);
                this.glowBar((double)i * (d2 - 0.045), -d3 * 0.5, d, 0.108, 0.018, 0.035, n, d4);
            }
        }

        private void glowBar(double d, double d2, double d3, double d4, double d5, double d6, int n, double d7) {
            double d8 = d - d4 / 2.0;
            double d9 = d + d4 / 2.0;
            double d10 = d2 - d5 / 2.0;
            double d11 = d2 + d5 / 2.0;
            double d12 = d3 - d6 / 2.0;
            double d13 = d3 + d6 / 2.0;
            this.quad(new double[]{d8, d10, d3}, new double[]{d9, d10, d3}, new double[]{d9, d11, d3}, new double[]{d8, d11, d3}, n, d7);
            this.quad(new double[]{d8, d2, d12}, new double[]{d9, d2, d12}, new double[]{d9, d2, d13}, new double[]{d8, d2, d13}, n, d7 * 0.65);
            this.quad(new double[]{d, d10, d12}, new double[]{d, d11, d12}, new double[]{d, d11, d13}, new double[]{d, d10, d13}, n, d7 * 0.65);
        }
    }

    public record Face(double[] points, int color, float alpha) {
    }
}

