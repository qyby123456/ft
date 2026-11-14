/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiaction;

import net.farmingtales.ftboss.client.qizaiaction.QizaiActionRig;

public final class QizaiActionClock {
    private String signature;
    private double changed;
    private double lastSeen = Double.NEGATIVE_INFINITY;
    private double lastActive = Double.NEGATIVE_INFINITY;
    private double[] current;
    private double[] correction;

    public static boolean eligible(int n, boolean bl, boolean bl2, String string) {
        return n == 11 && bl && !bl2 && "ftboss:qizai_prayer_staff".equals(string);
    }

    public static Selection select(int n, long l, int n2, boolean bl, int n3, double d) {
        String string;
        if (!Double.isFinite(d) || n2 <= 0 || d < (double)l || d - (double)l >= (double)n2 || n < 1 || n > 10) {
            return null;
        }
        switch (n) {
            case 1: {
                String string2 = "volley";
                break;
            }
            case 2: {
                String string2 = "fall";
                break;
            }
            case 3: {
                String string2 = "prayer";
                break;
            }
            case 4: {
                String string2 = "step";
                break;
            }
            case 5: {
                String string2 = "cross";
                break;
            }
            case 6: {
                String string2 = "refrain";
                break;
            }
            case 7: {
                String string2 = "sweep";
                break;
            }
            case 8: {
                String string2 = "nova";
                break;
            }
            case 9: {
                String string2 = "prism";
                break;
            }
            default: {
                String string2 = string = "barrage";
            }
        }
        String string3 = n == 8 ? (bl ? "nova_release" : "nova_charge") : (n == 4 ? "step_release" : (bl ? (n3 >= 3 ? string + "_release" : string + "_hold") : string + "_charge"));
        return new Selection(n + ":" + l + ":" + bl + ":" + n3, string3, (d - (double)l) / (double)n2);
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    public static Selection auxiliary(int n, long l, boolean bl, double d) {
        String string;
        int n2;
        double d2 = d - (double)l;
        int n3 = n == 101 ? 42 : (n2 = bl ? 12 : 16);
        if (!Double.isFinite(d) || d2 < 0.0 || d2 >= (double)n2 || n != 101 && n != 102) {
            return null;
        }
        String string2 = n == 102 ? (bl ? "release" : "charge") : (d2 < 24.0 ? "charge" : (string = d2 < 34.0 ? "hold" : "release"));
        double d3 = n == 102 ? d2 / (double)n2 : (string.equals("charge") ? d2 / 24.0 : (string.equals("hold") ? (d2 - 24.0) / 10.0 : (d2 - 34.0) / 8.0));
        return new Selection(n + ":" + l + ":" + string, (n == 101 ? "arsenal" : "repel") + "_" + string, d3);
    }

    public static Selection select(Cue cue, double d) {
        if (cue.kind == 101 || cue.kind == 102) {
            return QizaiActionClock.auxiliary(cue.kind, cue.start, cue.released, d);
        }
        if (!Double.isFinite(d) || cue.duration <= 0 || d < (double)cue.start || d - (double)cue.start >= (double)cue.duration || cue.kind < 1 || cue.kind > 10) {
            return null;
        }
        double d2 = d - (double)cue.start;
        if (cue.kind == 7) {
            boolean bl = cue.volley == 3;
            return new Selection("7:" + cue.windupStart + ":" + cue.round + ":" + cue.released + ":" + bl, bl ? "sweep_release" : (cue.released ? "sweep_hold" : "sweep_charge"), bl ? d2 / (double)cue.duration : (cue.released ? (double)cue.elapsed / (double)Math.max(1, cue.activeTicks) * 4.0 % 1.0 : QizaiActionClock.clamp((d - (double)cue.windupStart) / (double)Math.max(1, cue.windupDuration))));
        }
        if (cue.kind == 9) {
            int n = Math.max(1, cue.trackTicks);
            int n2 = Math.max(1, cue.lockTicks);
            int n3 = n + n2;
            int n4 = n3 + Math.max(1, cue.fireGap) * 5 + 8;
            String string = d2 < (double)n ? "charge" : (d2 < (double)n4 ? "hold" : "release");
            return new Selection("9:" + cue.start + ":" + string, "prism_" + string, string.equals("charge") ? d2 / (double)n : (string.equals("hold") ? QizaiActionClock.clamp((d2 - (double)n) / (double)(n4 - n)) : QizaiActionClock.clamp((d2 - (double)n4) / (double)Math.max(1, cue.duration - n4))));
        }
        if (cue.kind == 10) {
            int n = Math.max(1, cue.activeTicks);
            double d3 = d2 - (double)cue.roundStart;
            int n5 = Math.max(1, cue.warningTicks);
            String string = d3 < (double)n5 ? "charge" : (d3 < (double)n ? "hold" : "release");
            return new Selection("10:" + cue.start + ":" + cue.round + ":" + string, "barrage_" + string, string.equals("charge") ? QizaiActionClock.clamp(d3 / (double)n5) : (string.equals("hold") ? (d3 - (double)n5) % 24.0 / 24.0 : QizaiActionClock.clamp((d3 - (double)n) / Math.max(1.0, Math.min(20.0, (double)cue.duration - d2 + d3 - (double)n)))));
        }
        return QizaiActionClock.select(cue.kind, cue.start, cue.duration, cue.released, cue.volley, d);
    }

    public double[] sample(QizaiActionRig qizaiActionRig, Selection selection, double d) {
        if (d < this.lastSeen) {
            this.signature = null;
            this.current = null;
            this.correction = null;
            this.lastActive = Double.NEGATIVE_INFINITY;
        }
        if (selection != null) {
            this.lastActive = d;
        }
        String string = selection == null ? "ready" : selection.signature();
        String string2 = selection == null ? "ready" : selection.clip();
        double d2 = selection == null ? d / 80.0 % 1.0 : selection.progress();
        double[] dArray = qizaiActionRig.channels(string2, d2);
        if (!string.equals(this.signature)) {
            this.correction = new double[14];
            if (this.current != null) {
                for (int i = 0; i < 14; ++i) {
                    this.correction[i] = this.current[i] - dArray[i];
                }
            }
            this.signature = string;
            this.changed = d;
        }
        double d3 = Math.max(0.0, Math.min(1.0, (d - this.changed) / (double)(selection == null ? 6 : 3)));
        double d4 = 1.0 - d3 * d3 * (3.0 - 2.0 * d3);
        this.current = (double[])dArray.clone();
        for (int i = 0; i < 14; ++i) {
            int n = i;
            this.current[n] = this.current[n] + this.correction[i] * d4;
        }
        this.lastSeen = d;
        return (double[])this.current.clone();
    }

    public boolean expired(double d) {
        return d < this.lastSeen || d - this.lastSeen > 60.0;
    }

    public boolean canRecover(double d) {
        return Double.isFinite(d) && d >= this.lastActive && d - this.lastActive <= 6.0;
    }

    public record Selection(String signature, String clip, double progress) {
    }

    public record Cue(int kind, long start, int duration, boolean released, int volley, long windupStart, int windupDuration, int elapsed, int round, int trackTicks, int lockTicks, int fireGap, int activeTicks, int warningTicks, int roundStart) {
    }
}

