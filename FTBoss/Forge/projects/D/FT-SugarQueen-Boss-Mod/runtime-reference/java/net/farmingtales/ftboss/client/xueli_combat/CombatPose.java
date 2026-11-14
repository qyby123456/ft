/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.xueli_combat;

public final class CombatPose {
    private CombatPose() {
    }

    private static double clamp(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double smooth(double d) {
        double d2 = CombatPose.clamp(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static State sample(String string, double d) {
        String[] stringArray = string.split(":");
        String string2 = stringArray[0];
        int n = stringArray.length > 1 ? Integer.parseInt(stringArray[1]) : 0;
        double[] dArray = new double[]{0.0, 0.0, 0.0};
        double[] dArray2 = new double[]{0.0, 0.0, 0.0};
        double d2 = 1.0;
        double d3 = 1.0;
        switch (string2) {
            case "guard": {
                d3 = 0.68 + 0.1 * Math.sin(d * 2.6 + (double)n * 1.7);
                dArray[1] = 0.016 * Math.sin(d * 2.0 + (double)n);
                break;
            }
            case "guard_mote": {
                dArray[1] = 0.12 * Math.sin(d * 2.0 + (double)n);
                d3 = 0.5 + 0.25 * Math.sin(d * 3.0 + (double)n);
                break;
            }
            case "thread": {
                d3 = 0.62 + 0.17 * Math.sin(d * 4.0 + (double)n * 0.4);
                break;
            }
            case "thread_flow": {
                dArray[2] = (d * 0.6 + (double)n * 0.23) % 0.88;
                d3 = 0.6 * Math.sin(Math.PI * dArray[2] / 0.88);
                break;
            }
            case "break": {
                double d4 = CombatPose.clamp(d / 0.8);
                d2 = 1.0 + d4 * 0.52;
                dArray[1] = -0.25 * d4 * d4;
                dArray2[1] = (double)n * d4 * 5.0;
                d3 = 1.0 - CombatPose.smooth(d / 0.8);
                break;
            }
            case "hit": {
                d2 = 0.62 + 0.38 * CombatPose.smooth(d / 0.12);
                d3 = (1.0 - CombatPose.smooth(d / 0.42)) * CombatPose.smooth(d / 0.045);
                break;
            }
            case "charge": {
                double d5 = CombatPose.clamp(d);
                d3 = 0.35 + 0.55 * d5;
                d2 = n == 0 ? 1.0 : 0.23 + 0.73 * CombatPose.smooth(d5);
                break;
            }
            case "charge_mote": {
                dArray[1] = 0.035 * (1.0 - CombatPose.clamp(d));
                d3 = 0.3 + 0.55 * CombatPose.clamp(d);
                break;
            }
            case "sweep": {
                dArray2[1] = -58.0 + 136.0 * CombatPose.smooth(d / 0.27);
                d3 = CombatPose.smooth(d / 0.045) * (1.0 - CombatPose.smooth((d - 0.22) / 0.23));
                break;
            }
            case "wave": {
                d2 = 0.08 + 0.92 * CombatPose.smooth(d / 0.25);
                d3 = 1.0 - CombatPose.smooth((d - 0.12) / 0.43);
                break;
            }
            case "wake": {
                double d6 = (d * 3.0 + (double)n * 0.17) % 1.0;
                dArray[2] = -d6 * 1.8;
                dArray[1] = d6 * 0.045;
                d3 = 0.8 * Math.sin(d6 * Math.PI);
                break;
            }
            case "crush": {
                dArray[0] = (double)(n % 2 != 0 ? -1 : 1) * 0.07 * Math.sin(d * 14.0);
                d3 = 0.55 + 0.25 * Math.sin(d * 14.0 + (double)n);
                break;
            }
            case "impact": {
                d2 = 0.06 + 0.94 * CombatPose.smooth(d / 0.24);
                d3 = 1.0 - CombatPose.smooth((d - 0.12) / 0.55);
                break;
            }
            case "spray": {
                double d7 = CombatPose.clamp(d / 0.68);
                double d8 = (double)n * 2.399963;
                dArray = new double[]{Math.sin(d8) * d7 * 0.64, Math.max(-0.02, 0.72 * d7 - 0.8 * d7 * d7), Math.cos(d8) * d7 * 0.64};
                d3 = 1.0 - CombatPose.smooth(d7);
                break;
            }
            case "fuse": {
                double d9 = CombatPose.clamp(d / 5.0);
                double d10 = Math.pow(0.5 + 0.5 * Math.sin(d * (5.0 + d9 * 11.0) + (double)n), 3.0);
                d2 = 1.0 + 0.035 * d10;
                d3 = 0.35 + 0.35 * d10 + 0.22 * d9;
                break;
            }
            case "stun": {
                dArray2[1] = d * 100.0 + (double)(n * 120);
                d3 = 0.6 + 0.15 * Math.sin(d * 5.0 + (double)n);
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown combat track: " + string);
            }
        }
        return new State(dArray, dArray2, d2, d3);
    }

    public static double[] place(double[] dArray, double[] dArray2, State state) {
        double d = (dArray[0] - dArray2[0]) * state.size();
        double d2 = (dArray[1] - dArray2[1]) * state.size();
        double d3 = (dArray[2] - dArray2[2]) * state.size();
        double[] dArray3 = new double[3];
        for (int i = 0; i < 3; ++i) {
            dArray3[i] = Math.toRadians(state.turn()[i]);
        }
        double d4 = d2 * Math.cos(dArray3[0]) - d3 * Math.sin(dArray3[0]);
        d3 = d2 * Math.sin(dArray3[0]) + d3 * Math.cos(dArray3[0]);
        d2 = d4;
        d4 = d * Math.cos(dArray3[1]) + d3 * Math.sin(dArray3[1]);
        d3 = -d * Math.sin(dArray3[1]) + d3 * Math.cos(dArray3[1]);
        d = d4;
        d4 = d * Math.cos(dArray3[2]) - d2 * Math.sin(dArray3[2]);
        d2 = d * Math.sin(dArray3[2]) + d2 * Math.cos(dArray3[2]);
        d = d4;
        return new double[]{d + dArray2[0] + state.move()[0], d2 + dArray2[1] + state.move()[1], d3 + dArray2[2] + state.move()[2]};
    }

    public record State(double[] move, double[] turn, double size, double alpha) {
    }
}

