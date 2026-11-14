package net.farmingtales.ftboss.client.render;

import net.farmingtales.ftboss.util.SpellVisualGeometry.Point;
import net.farmingtales.ftboss.util.SpellVisualGeometry.Sink;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class OptimizedSpellTelegraphs {
    private OptimizedSpellTelegraphs() {
    }

    public static int draw(Spell var0, Phase var1, double var2, float var4, double var5, boolean var7, Sink var8) {
        if (Double.isFinite(var2) && Double.isFinite(var5) && Float.isFinite(var4) && !(var4 <= 0.0F)) {
            OptimizedSpellTelegraphs.Painter var9 = new OptimizedSpellTelegraphs.Painter(
                var8, var0.color, Math.min(1.0F, var4) * (var1 == Phase.CHARGE ? 0.65F : 0.38F)
            );
            if (var1 == Phase.BEAM) {
                if (!var7 && var5 >= 0.015) {
                    var9.line(-0.035, 0.0, 0.035, var5, 0.028);
                    return var9.count;
                } else {
                    return 0;
                }
            } else {
                String var10 = OptimizedSpellPlacement.cue(var0);
                if (var10.equals("NONE")) {
                    return 0;
                } else {
                    double var11 = Math.max(0.35, var0.radius);
                    double var13 = 0.2 + Math.min(0.42, var11 * 0.08);
                    double var15 = var11 / Math.sqrt(2.0);
                    if (var10.equals("CROSS")) {
                        var9.line(-6.0, 0.0, 6.0, 0.0, 0.12);
                        var9.line(0.0, -6.0, 0.0, 6.0, 0.12);

                        for (double var52 : new double[]{-6.0, -3.0, 3.0, 6.0}) {
                            var9.line(var52, -0.7, var52, 0.7, 0.036);
                            var9.line(-0.7, var52, 0.7, var52, 0.036);
                        }

                        return var9.count;
                    } else if (!var10.equals("WAVE") && !var10.equals("CORRIDOR") && !var10.equals("LANES")) {
                        if (var10.equals("STARS")) {
                            for (int var27 = 0; var27 < 4; var27++) {
                                var9.corner((double)(var27 % 2 == 0 ? -1 : 1) * var15, (double)(var27 < 2 ? -1 : 1) * var15, var13);
                            }

                            var9.line(-0.1, 0.0, 0.1, 0.0, 0.025);
                            var9.line(0.0, -0.1, 0.0, 0.1, 0.025);
                            return var9.count;
                        } else {
                            for (int var26 = 0; var26 < 4; var26++) {
                                double var18 = (double)(var26 % 2 == 0 ? -1 : 1) * var15;
                                double var42 = (double)(var26 < 2 ? -1 : 1) * var15;
                                var9.corner(var18, var42, var13);
                            }

                            switch (var10) {
                                case "SLASH":
                                case "SCARS":
                                    for (int var40 = 0; var40 < 3; var40++) {
                                        double var51 = (double)(var40 - 1) * var11 * 0.4;
                                        var9.line(var51 - 0.3, -var11 * 0.35, var51 + 0.3, var11 * 0.35, 0.025);
                                    }
                                    break;
                                case "LATTICE":
                                    for (int var39 = 0; var39 < 3; var39++) {
                                        double var50 = (double)(var39 - 1) * var11 * 0.55;
                                        var9.line(var50, -var11 * 0.48, var50, -var11 * 0.26, 0.025);
                                        var9.line(var50, var11 * 0.26, var50, var11 * 0.48, 0.025);
                                    }
                                    break;
                                case "PULL":
                                case "BIND":
                                    for (int var38 = 0; var38 < 4; var38++) {
                                        double var49 = (double)var38 * Math.PI / 2.0;
                                        double var57 = Math.cos(var49) * var11 * 0.7;
                                        double var59 = Math.sin(var49) * var11 * 0.7;
                                        var9.line(var57, var59, var57 * 0.6, var59 * 0.6, 0.031);
                                        var9.line(var57 * 0.6, var59 * 0.6, var57 * 0.72 - var59 * 0.12, var59 * 0.72 + var57 * 0.12, 0.026);
                                    }
                                    break;
                                case "REPULSE":
                                    for (int var37 = 0; var37 < 4; var37++) {
                                        double var48 = (double)var37 * Math.PI / 2.0;
                                        double var56 = Math.cos(var48) * var11 * 0.57;
                                        double var58 = Math.sin(var48) * var11 * 0.57;
                                        var9.line(var56, var58, var56 * 1.4, var58 * 1.4, 0.031);
                                    }
                                    break;
                                case "RAIN":
                                case "FROST":
                                    for (int var36 = 0; var36 < 6; var36++) {
                                        double var47 = ((double)(var36 * 7 % 6) - 2.5) * var11 * 0.23;
                                        double var55 = (double)(var36 * 3 % 5 - 2) * var11 * 0.22;
                                        var9.line(var47 - 0.08, var55, var47 + 0.08, var55, 0.03);
                                        if (var10.equals("FROST")) {
                                            var9.line(var47, var55 - 0.08, var47, var55 + 0.08, 0.03);
                                        }
                                    }
                                    break;
                                case "PETALS":
                                case "SPORES":
                                case "SPRAY":
                                    for (int var35 = 0; var35 < 7; var35++) {
                                        double var46 = (double)(var35 * 5 % 7 - 3) * var11 * 0.2;
                                        double var54 = (double)(var35 * 3 % 7 - 3) * var11 * 0.2;
                                        var9.line(var46 - 0.035, var54 - 0.06, var46 + 0.035, var54 + 0.06, 0.038);
                                    }
                                    break;
                                case "HEAL":
                                    for (int var34 = 0; var34 < 4; var34++) {
                                        double var45 = (double)(var34 % 2 == 0 ? -1 : 1) * var11 * 0.5;
                                        double var53 = (double)(var34 < 2 ? -1 : 1) * var11 * 0.42;
                                        var9.line(var45 - 0.13, var53, var45 + 0.13, var53, 0.028);
                                        var9.line(var45, var53 - 0.13, var45, var53 + 0.13, 0.028);
                                    }
                                    break;
                                case "MINES":
                                    for (int var33 = 0; var33 < 3; var33++) {
                                        var9.bracket((double)(var33 - 1) * var11 * 0.55, (double)(var33 % 2) * var11 * 0.33 - 0.16, 0.22, 0.14);
                                    }
                                    break;
                                case "ECHO":
                                    for (int var32 = 0; var32 < 2; var32++) {
                                        double var44 = var32 == 0 ? -var11 * 0.47 : var11 * 0.47;
                                        var9.line(var44, -var11 * 0.34, var44, var11 * 0.34, 0.035);
                                    }
                                    break;
                                case "BLAST":
                                    for (int var31 = 0; var31 < 4; var31++) {
                                        double var43 = (double)(var31 % 2 == 0 ? -1 : 1) * var11 * 0.48;
                                        double var22 = (double)(var31 < 2 ? -1 : 1) * var11 * 0.48;
                                        var9.line(var43 * 0.75, var22 * 0.75, var43, var22, 0.032);
                                    }
                                    break;
                                default:
                                    throw new IllegalArgumentException("Unknown spell cue " + var10);
                            }

                            return var9.count;
                        }
                    } else {
                        double var17 = var10.equals("CORRIDOR")
                            ? (double)(var0.id.equals("riptide_corridor") ? 15 : 12)
                            : (var10.equals("WAVE") ? (double)(var0.id.equals("tidal_edict") ? 12 : 10) : Math.min(12.0, Math.max(4.0, var5)));
                        int var19 = var10.equals("LANES") ? 3 : 2;

                        for (int var20 = 0; var20 < var19; var20++) {
                            double var21 = var19 == 3 ? (double)(var20 - 1) * 0.55 : (double)(var20 == 0 ? -1 : 1) * Math.min(var11, 2.7);

                            for (int var23 = 0; var23 < 3; var23++) {
                                double var24 = var17 * (double)(var23 + 1) / 3.0;
                                var9.line(var21, var24 - 0.3, var21, var24 + 0.3, 0.032);
                                var9.line(var21 - 0.14, var24 - 0.13, var21, var24 + 0.1, 0.028);
                            }
                        }

                        return var9.count;
                    }
                }
            }
        } else {
            return 0;
        }
    }

    private static final class Painter {
        private final Sink sink;
        private final int color;
        private final float opacity;
        private int count;

        private Painter(Sink var1, int var2, float var3) {
            this.sink = var1;
            this.color = var2;
            this.opacity = var3;
        }

        private void line(double var1, double var3, double var5, double var7, double var9) {
            double var11 = Math.hypot(var5 - var1, var7 - var3);
            if (!(var11 < 1.0E-6)) {
                double var13 = -(var7 - var3) / var11 * var9 * 0.5;
                double var15 = (var5 - var1) / var11 * var9 * 0.5;
                double var17 = 0.045 + (double)this.count * 4.0E-4;
                this.sink
                    .quad(
                        new Point(var1 + var13, var17, var3 + var15),
                        new Point(var5 + var13, var17, var7 + var15),
                        new Point(var5 - var13, var17, var7 - var15),
                        new Point(var1 - var13, var17, var3 - var15),
                        this.color,
                        this.opacity,
                        255
                    );
                this.count++;
            }
        }

        private void corner(double var1, double var3, double var5) {
            double var7 = var1 < 0.0 ? 1.0 : -1.0;
            double var9 = var3 < 0.0 ? 1.0 : -1.0;
            this.line(var1, var3, var1 + var7 * var5, var3, 0.043);
            this.line(var1, var3, var1, var3 + var9 * var5, 0.043);
        }

        private void bracket(double var1, double var3, double var5, double var7) {
            for (int var9 = 0; var9 < 4; var9++) {
                double var10 = var9 % 2 == 0 ? -1.0 : 1.0;
                double var12 = var9 < 2 ? -1.0 : 1.0;
                this.line(var1 + var10 * var5, var3 + var12 * var5, var1 + var10 * (var5 - var7), var3 + var12 * var5, 0.03);
            }
        }
    }
}
