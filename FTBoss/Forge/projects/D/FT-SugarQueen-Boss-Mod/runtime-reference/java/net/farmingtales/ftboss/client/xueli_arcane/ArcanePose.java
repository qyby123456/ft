/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.xueli_arcane;

public final class ArcanePose {
    private ArcanePose() {
    }

    private static double limit(double d) {
        return Math.max(0.0, Math.min(1.0, d));
    }

    private static double ease(double d) {
        double d2 = ArcanePose.limit(d);
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public static double nativeAdvance(double d) {
        double d2 = ArcanePose.limit(d / 0.28) * 0.8333;
        double[][] dArrayArray = new double[][]{{0.0, 0.0}, {0.3333, 5.67}, {0.4583, 9.21}, {0.5833, 12.75}, {0.8333, 17.0}};
        for (int i = 1; i < dArrayArray.length; ++i) {
            if (!(d2 <= dArrayArray[i][0])) continue;
            double d3 = (d2 - dArrayArray[i - 1][0]) / (dArrayArray[i][0] - dArrayArray[i - 1][0]);
            if (i == 1) {
                d3 = 1.0 - Math.cos(d3 * Math.PI / 2.0);
            }
            if (i == 4) {
                d3 = Math.sin(d3 * Math.PI / 2.0);
            }
            return (dArrayArray[i - 1][1] + (dArrayArray[i][1] - dArrayArray[i - 1][1]) * d3) / 17.0 * 0.94;
        }
        return 0.94;
    }

    private static double nativeBloom(int n, double d) {
        double[] dArray = new double[]{0.72, 0.96, 1.12, 1.4};
        double d2 = ArcanePose.limit(d / (dArray[n % 4] * 0.22));
        double d3 = d2 == 0.0 ? 0.0 : (d2 == 1.0 ? 1.0 : -Math.pow(2.0, 10.0 * d2 - 10.0) * Math.sin((10.0 * d2 - 10.75) * Math.PI * 2.0 / 3.0));
        return 0.92 + d3 * 0.08;
    }

    private static double[] guidance(int n, double d) {
        double d2 = ArcanePose.limit(d / 0.28);
        double d3 = n - 1;
        return new double[]{d3 * (0.18 * Math.sin(d2 * Math.PI) - 0.17 * ArcanePose.ease(d2)) + 0.038 * Math.sin(d2 * Math.PI), (0.072 + (double)n * 0.012) * Math.sin(d2 * Math.PI) + (n == 1 ? -0.025 : 0.018) * ArcanePose.ease(d2), ArcanePose.nativeAdvance(d)};
    }

    public static Flight fragmentFlight(int n, double d) {
        double d2 = 1.0 + (double)(n % 3) * 0.007;
        double d3 = Math.max(0.0, d - d2);
        double d4 = 0.18 + (double)(n % 5) * 0.061;
        double d5 = 0.045 + (double)(n % 3) * 0.015;
        double d6 = 0.022 + d5 * 0.7;
        double d7 = 8.8;
        double d8 = 1.9 + (double)(n % 4) * 0.22;
        double d9 = 1.24 + (double)(n % 5) * 0.11;
        double d10 = (d8 + Math.sqrt(d8 * d8 + 2.0 * d7 * (d4 - d6))) / d7;
        double d11 = Math.min(d3, d10);
        double d12 = Math.max(0.0, d3 - d10);
        double d13 = 0.34 + (double)(n % 3) * 0.055;
        double d14 = 2.0 * d13 / d7;
        double d15 = Math.min(d12, d14);
        double d16 = Math.max(0.0, d12 - d14);
        double d17 = d9 * d11 + d9 * 0.22 * d15 + d9 * 0.1 * (1.0 - Math.exp(-d16 * 14.0)) / 14.0;
        double d18 = d3 < d10 ? d4 + d8 * d11 - 0.5 * d7 * d11 * d11 : d6 + d13 * d15 - 0.5 * d7 * d15 * d15;
        return new Flight(d2, d3, d4, d6, d7, d8, d9, d10, d17, d18, d12, d14, d11 / d10);
    }

    public static State at(String string, double d) {
        String string2;
        double[] dArray = new double[]{0.0, 0.0, 0.0};
        double[] dArray2 = new double[]{0.0, 0.0, 0.0};
        double d2 = 1.0;
        double d3 = 1.0;
        int n = string.lastIndexOf(58);
        int n2 = n < 0 ? 0 : Integer.parseInt(string.substring(n + 1));
        switch (string2 = n < 0 ? string : string.substring(0, n)) {
            case "lance": {
                dArray = ArcanePose.guidance(n2, d);
                double d4 = ArcanePose.limit(d / 0.28);
                dArray2 = new double[]{-4.5 * Math.sin(d4 * Math.PI), (double)((n2 - 1) * 7) * Math.sin(d4 * Math.PI * 2.0), 0.0};
                d3 = (1.0 - ArcanePose.ease((d - 0.31) / 0.19)) * (0.88 + 0.12 * Math.sin(d * 4.0));
                break;
            }
            case "afterimage": {
                dArray = ArcanePose.guidance(n2, Math.max(0.0, d - 0.028));
                d3 = (0.7 + 0.3 * ArcanePose.ease(d / 0.04)) * (1.0 - ArcanePose.ease((d - 0.31) / 0.25));
                break;
            }
            case "trail": {
                int n3 = n2 / 7;
                int n4 = n2 % 7;
                double d5 = (double)(n4 + 1) * 0.013;
                dArray = ArcanePose.guidance(n3, Math.max(0.0, d - d5));
                d3 = d < d5 ? 0.0 : (0.83 - (double)n4 * 0.079) * (1.0 - ArcanePose.ease((d - 0.3 - d5) / 0.19));
                break;
            }
            case "denial": {
                double d6 = d - 0.09 - (double)n2;
                double d7 = ArcanePose.limit(d6 / 0.36);
                d2 = (1.02 - 0.26 * ArcanePose.ease(d7)) * ArcanePose.nativeBloom(n2, Math.max(0.0, d6));
                dArray2[1] = -6.0 * d7;
                d3 = d6 < 0.0 ? 0.0 : Math.pow(Math.sin(d7 * Math.PI), 0.75) * (1.0 - ArcanePose.ease((d6 - 0.29) / 0.07));
                break;
            }
            case "severed": {
                double d8 = (d + (double)n2 * 0.043) % 0.32 / 0.32;
                dArray[1] = 0.23 * Math.min(0.7, d8);
                d3 = ArcanePose.ease(d8 / 0.17) * (1.0 - ArcanePose.ease((d8 - 0.58) / 0.19)) * (0.6 + (double)(n2 % 3) * 0.12);
                break;
            }
            case "silence": {
                d3 = 0.64 + 0.18 * Math.pow(Math.sin(d * Math.PI / 0.32), 2.0);
                break;
            }
            case "rain": {
                double d9 = d - 0.065 - (double)n2 * 0.011;
                double d10 = d9 < 0.0 ? 0.0 : d9 % 1.0;
                double d11 = ArcanePose.limit(d10 / 0.46);
                dArray = new double[]{0.09 * d11 * Math.sin((double)n2 * 2.4), 0.07 - 1.58 * Math.pow(d11, 1.3), 0.0};
                dArray2[1] = d11 * 32.0;
                d2 = ArcanePose.nativeBloom(n2, Math.max(0.0, d9));
                d3 = d9 < 0.0 || d9 >= 2.46 || d10 >= 0.46 ? 0.0 : ArcanePose.ease(d11 / 0.075) * (1.0 - ArcanePose.ease((d11 - 0.95) / 0.05));
                break;
            }
            case "splash": {
                double d12 = d - 0.065 - (double)n2 * 0.011;
                double d13 = d12 < 0.0 ? 0.0 : d12 % 1.0;
                double d14 = ArcanePose.limit(d13 / 0.46);
                d2 = 0.72 + 0.44 * ArcanePose.limit((d14 - 0.81) / 0.19);
                d3 = d12 < 0.0 || d12 >= 2.46 || d13 >= 0.46 ? 0.0 : Math.sin(ArcanePose.limit((d14 - 0.81) / 0.19) * Math.PI);
                break;
            }
            case "deposit": {
                d3 = ArcanePose.ease((d - 0.4 - (double)n2) / 0.16) * 0.6;
                break;
            }
            case "veil": {
                d2 = ArcanePose.nativeBloom(n2, d);
                dArray[1] = 0.008 * Math.sin(d * 2.7 + (double)n2);
                d3 = 0.65 + 0.22 * Math.pow(Math.sin(d * 2.8 + (double)n2 * 1.1), 2.0);
                break;
            }
            case "perimeter": {
                d2 = 1.0 + 0.018 * Math.sin(d * 2.5 + (double)n2);
                d3 = 0.38 + 0.26 * Math.pow(Math.sin(d * 2.5 + (double)n2), 2.0);
                break;
            }
            case "tension": {
                d2 = (1.04 - 0.22 * ArcanePose.ease(d)) * ArcanePose.nativeBloom(n2, d);
                d3 = (0.28 + 0.66 * ArcanePose.ease(d)) * (1.0 - ArcanePose.ease((d - 1.0) / 0.12));
                break;
            }
            case "compression": {
                d2 = (1.1 - 0.47 * ArcanePose.ease(d)) * ArcanePose.nativeBloom(0, d);
                d3 = (0.24 + 0.64 * ArcanePose.ease(d)) * (1.0 - ArcanePose.ease((d - 1.0) / 0.11));
                break;
            }
            case "fracture": {
                Flight flight = ArcanePose.fragmentFlight(n2, d);
                double d15 = (double)n2 * Math.PI * 2.0 / 17.0;
                double d16 = Math.sin(flight.phase() * Math.PI);
                dArray = new double[]{Math.sin(d15) * flight.travel(), flight.elevation() - flight.height(), Math.cos(d15) * flight.travel()};
                dArray2 = new double[]{d16 * (double)(51 + n2 % 3 * 14), flight.phase() * (double)(31 + n2 % 4 * 11), d16 * (double)(n2 % 2 == 1 ? -34 : 34)};
                d3 = d < flight.launch() ? 0.0 : ArcanePose.ease(flight.age() / 0.026) * (1.0 - ArcanePose.ease((flight.contactAge() - 0.27) / 0.34));
                break;
            }
            case "landing": {
                Flight flight = ArcanePose.fragmentFlight(n2, d);
                double d17 = (double)n2 * Math.PI * 2.0 / 17.0;
                dArray = new double[]{Math.sin(d17) * flight.speed() * flight.impact(), 0.0, Math.cos(d17) * flight.speed() * flight.impact()};
                d2 = 0.65 + 0.75 * ArcanePose.ease(flight.contactAge() / 0.2);
                d3 = flight.age() < flight.impact() ? 0.0 : ArcanePose.ease(flight.contactAge() / 0.014) * (1.0 - ArcanePose.ease((flight.contactAge() - 0.055) / 0.24));
                break;
            }
            case "shock": {
                double d18 = d - 1.0 - (double)n2 * 0.034;
                double d19 = ArcanePose.limit(d18 / 0.45);
                d2 = 0.64 + 1.03 * (1.0 - Math.pow(1.0 - d19, 3.0));
                d3 = d18 < 0.0 ? 0.0 : ArcanePose.ease(d18 / 0.03) * (1.0 - ArcanePose.ease((d18 - 0.18) / 0.27));
                break;
            }
            case "echo": {
                double d20 = d - 0.6 - (double)n2;
                double d21 = ArcanePose.limit(d20 / 0.48);
                d2 = (0.67 + 0.88 * ArcanePose.ease(d21)) * ArcanePose.nativeBloom(n2, Math.max(0.0, d20));
                dArray[1] = -0.018 * d21;
                dArray2[1] = (double)((n2 % 2 == 1 ? -1 : 1) * 8) * ArcanePose.ease(d21);
                d3 = d20 < 0.0 ? 0.0 : ArcanePose.ease(d20 / 0.028) * (1.0 - ArcanePose.ease((d20 - 0.16) / 0.32));
                break;
            }
            case "echo_after": {
                double d22 = d - 0.69 - (double)n2;
                double d23 = ArcanePose.limit(d22 / 0.48);
                d2 = 0.89 + 0.72 * ArcanePose.ease(d23);
                dArray2[1] = (double)((n2 % 2 == 1 ? 1 : -1) * 5) * d23;
                d3 = d22 < 0.0 ? 0.0 : ArcanePose.ease(d22 / 0.04) * (1.0 - ArcanePose.ease((d22 - 0.1) / 0.38));
                break;
            }
            case "arrest": {
                double d24 = d;
                double d25 = 0.0;
                for (double d26 : new double[]{0.6, 1.6, 2.6}) {
                    d24 -= Math.min(0.09, Math.max(0.0, d - d26));
                    if (!(d >= d26) || !(d <= d26 + 0.09)) continue;
                    d25 = 1.0;
                }
                dArray = new double[]{0.026 * Math.sin(d24 * 1.6 + (double)(n2 * 2)), 0.043 * Math.sin(d24 * 2.6 + (double)n2), 0.018 * Math.cos(d24 * 1.5 + (double)n2)};
                d3 = (0.26 + 0.6 * d25) * (1.0 - ArcanePose.ease((d - 2.91) / 0.29));
                break;
            }
            case "chrono_veil": 
            case "chrono_dust": {
                double d27 = 0.0;
                for (double d28 : new double[]{0.6, 1.6, 2.6}) {
                    if (!(d >= d28)) continue;
                    d27 += Math.exp(-Math.pow((d - d28 - 0.065) / 0.11, 2.0));
                }
                d3 = (string2.equals("chrono_veil") ? 0.22 + 0.44 * d27 : 0.4 + 0.48 * d27) * (1.0 - ArcanePose.ease((d - 2.91) / 0.31));
                dArray[1] = 0.008 * Math.sin(d * 2.1);
                break;
            }
            case "prelude": {
                d3 = 0.44 * (1.0 - ArcanePose.ease((d - 0.58) / 0.16));
                break;
            }
            default: {
                throw new IllegalArgumentException("Unknown fresh animation track: " + string);
            }
        }
        return new State(dArray, dArray2, d2, d3);
    }

    public static double[] point(double[] dArray, double[] dArray2, State state) {
        int n;
        double[] dArray3 = new double[3];
        for (n = 0; n < 3; ++n) {
            dArray3[n] = (dArray[n] - dArray2[n]) * state.size();
        }
        for (n = 0; n < 3; ++n) {
            int n2 = (n + 1) % 3;
            int n3 = (n + 2) % 3;
            double d = state.turn()[n] * Math.PI / 180.0;
            double d2 = dArray3[n2];
            double d3 = dArray3[n3];
            dArray3[n2] = d2 * Math.cos(d) - d3 * Math.sin(d);
            dArray3[n3] = d2 * Math.sin(d) + d3 * Math.cos(d);
        }
        for (n = 0; n < 3; ++n) {
            int n4 = n;
            dArray3[n4] = dArray3[n4] + (dArray2[n] + state.move()[n]);
        }
        return dArray3;
    }

    public record Flight(double launch, double age, double height, double rest, double gravity, double upward, double speed, double impact, double travel, double elevation, double contactAge, double reboundDuration, double phase) {
    }

    public record State(double[] move, double[] turn, double size, double alpha) {
    }
}

