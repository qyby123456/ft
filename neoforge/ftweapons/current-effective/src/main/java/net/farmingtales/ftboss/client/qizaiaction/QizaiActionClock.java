package net.farmingtales.ftboss.client.qizaiaction;

public final class QizaiActionClock {
    private String signature;
    private double changed;
    private double lastSeen = Double.NEGATIVE_INFINITY;
    private double lastActive = Double.NEGATIVE_INFINITY;
    private double[] current;
    private double[] correction;

    public static boolean eligible(int var0, boolean var1, boolean var2, String var3) {
        return var0 == 11 && var1 && !var2 && "ftboss:qizai_prayer_staff".equals(var3);
    }

    public static QizaiActionClock.Selection select(int var0, long var1, int var3, boolean var4, int var5, double var6) {
        if (Double.isFinite(var6) && var3 > 0 && !(var6 < (double)var1) && !(var6 - (double)var1 >= (double)var3) && var0 >= 1 && var0 <= 10) {
            String var8 = switch (var0) {
                case 1 -> "volley";
                case 2 -> "fall";
                case 3 -> "prayer";
                case 4 -> "step";
                case 5 -> "cross";
                case 6 -> "refrain";
                case 7 -> "sweep";
                case 8 -> "nova";
                case 9 -> "prism";
                default -> "barrage";
            };
            String var9 = var0 == 8
                ? (var4 ? "nova_release" : "nova_charge")
                : (var0 == 4 ? "step_release" : (var4 ? (var5 >= 3 ? var8 + "_release" : var8 + "_hold") : var8 + "_charge"));
            return new QizaiActionClock.Selection(var0 + ":" + var1 + ":" + var4 + ":" + var5, var9, (var6 - (double)var1) / (double)var3);
        } else {
            return null;
        }
    }

    private static double clamp(double var0) {
        return Math.max(0.0, Math.min(1.0, var0));
    }

    public static QizaiActionClock.Selection auxiliary(int var0, long var1, boolean var3, double var4) {
        double var6 = var4 - (double)var1;
        int var8 = var0 == 101 ? 42 : (var3 ? 12 : 16);
        if (Double.isFinite(var4) && !(var6 < 0.0) && !(var6 >= (double)var8) && (var0 == 101 || var0 == 102)) {
            String var9 = var0 == 102 ? (var3 ? "release" : "charge") : (var6 < 24.0 ? "charge" : (var6 < 34.0 ? "hold" : "release"));
            double var10 = var0 == 102
                ? var6 / (double)var8
                : (var9.equals("charge") ? var6 / 24.0 : (var9.equals("hold") ? (var6 - 24.0) / 10.0 : (var6 - 34.0) / 8.0));
            return new QizaiActionClock.Selection(var0 + ":" + var1 + ":" + var9, (var0 == 101 ? "arsenal" : "repel") + "_" + var9, var10);
        } else {
            return null;
        }
    }

    public static QizaiActionClock.Selection select(QizaiActionClock.Cue var0, double var1) {
        if (var0.kind != 101 && var0.kind != 102) {
            if (Double.isFinite(var1)
                && var0.duration > 0
                && !(var1 < (double)var0.start)
                && !(var1 - (double)var0.start >= (double)var0.duration)
                && var0.kind >= 1
                && var0.kind <= 10) {
                double var3 = var1 - (double)var0.start;
                if (var0.kind == 7) {
                    boolean var11 = var0.volley == 3;
                    return new QizaiActionClock.Selection(
                        "7:" + var0.windupStart + ":" + var0.round + ":" + var0.released + ":" + var11,
                        var11 ? "sweep_release" : (var0.released ? "sweep_hold" : "sweep_charge"),
                        var11
                            ? var3 / (double)var0.duration
                            : (
                                var0.released
                                    ? (double)var0.elapsed / (double)Math.max(1, var0.activeTicks) * 4.0 % 1.0
                                    : clamp((var1 - (double)var0.windupStart) / (double)Math.max(1, var0.windupDuration))
                            )
                    );
                } else if (var0.kind == 9) {
                    int var10 = Math.max(1, var0.trackTicks);
                    int var12 = Math.max(1, var0.lockTicks);
                    int var13 = var10 + var12;
                    int var8 = var13 + Math.max(1, var0.fireGap) * 5 + 8;
                    String var14 = var3 < (double)var10 ? "charge" : (var3 < (double)var8 ? "hold" : "release");
                    return new QizaiActionClock.Selection(
                        "9:" + var0.start + ":" + var14,
                        "prism_" + var14,
                        var14.equals("charge")
                            ? var3 / (double)var10
                            : (
                                var14.equals("hold")
                                    ? clamp((var3 - (double)var10) / (double)(var8 - var10))
                                    : clamp((var3 - (double)var8) / (double)Math.max(1, var0.duration - var8))
                            )
                    );
                } else if (var0.kind == 10) {
                    int var5 = Math.max(1, var0.activeTicks);
                    int var6 = Math.max(1, var0.warningTicks);
                    double var7 = var3 - (double)var0.roundStart;
                    String var9 = var7 < (double)var6 ? "charge" : (var7 < (double)var5 ? "hold" : "release");
                    return new QizaiActionClock.Selection(
                        "10:" + var0.start + ":" + var0.round + ":" + var9,
                        "barrage_" + var9,
                        var9.equals("charge")
                            ? clamp(var7 / (double)var6)
                            : (
                                var9.equals("hold")
                                    ? (var7 - (double)var6) % 24.0 / 24.0
                                    : clamp((var7 - (double)var5) / Math.max(1.0, Math.min(20.0, (double)var0.duration - var3 + var7 - (double)var5)))
                            )
                    );
                } else {
                    return select(var0.kind, var0.start, var0.duration, var0.released, var0.volley, var1);
                }
            } else {
                return null;
            }
        } else {
            return auxiliary(var0.kind, var0.start, var0.released, var1);
        }
    }

    public double[] sample(QizaiActionRig var1, QizaiActionClock.Selection var2, double var3) {
        if (var3 < this.lastSeen) {
            this.signature = null;
            this.current = null;
            this.correction = null;
            this.lastActive = Double.NEGATIVE_INFINITY;
        }

        if (var2 != null) {
            this.lastActive = var3;
        }

        String var5 = var2 == null ? "ready" : var2.signature();
        String var6 = var2 == null ? "ready" : var2.clip();
        double var7 = var2 == null ? var3 / 80.0 % 1.0 : var2.progress();
        double[] var9 = var1.channels(var6, var7);
        if (!var5.equals(this.signature)) {
            this.correction = new double[14];
            if (this.current != null) {
                for (int var10 = 0; var10 < 14; var10++) {
                    this.correction[var10] = this.current[var10] - var9[var10];
                }
            }

            this.signature = var5;
            this.changed = var3;
        }

        double var15 = Math.max(0.0, Math.min(1.0, (var3 - this.changed) / (double)(var2 == null ? 6 : 3)));
        double var12 = 1.0 - var15 * var15 * (3.0 - 2.0 * var15);
        this.current = (double[])var9.clone();

        for (int var14 = 0; var14 < 14; var14++) {
            this.current[var14] = this.current[var14] + this.correction[var14] * var12;
        }

        this.lastSeen = var3;
        return (double[])this.current.clone();
    }

    public boolean expired(double var1) {
        return var1 < this.lastSeen || var1 - this.lastSeen > 60.0;
    }

    public boolean canRecover(double var1) {
        return Double.isFinite(var1) && var1 >= this.lastActive && var1 - this.lastActive <= 6.0;
    }

    public static record Cue(
        int kind,
        long start,
        int duration,
        boolean released,
        int volley,
        long windupStart,
        int windupDuration,
        int elapsed,
        int round,
        int trackTicks,
        int lockTicks,
        int fireGap,
        int activeTicks,
        int warningTicks,
        int roundStart
    ) {
    }

    public static record Selection(String signature, String clip, double progress) {
    }
}
