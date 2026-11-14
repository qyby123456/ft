/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Locale;

public final class ManorDomainRules {
    public static final double RADIUS = 76.0;
    public static final int REFRESH_TICKS = 20;

    private ManorDomainRules() {
    }

    public static boolean supports(int n) {
        return n >= 0 && n <= 11 && n != 9;
    }

    public static boolean contains(double d, double d2, double d3) {
        return Double.isFinite(d) && Double.isFinite(d2) && Double.isFinite(d3) && d * d + d3 * d3 <= 5776.0;
    }

    public static double outgoing(int n, boolean bl, double d, double d2, boolean bl2, int n2) {
        return switch (n) {
            case 1, 5 -> {
                if (bl) {
                    yield 1.18;
                }
                yield 1.0;
            }
            case 2 -> {
                if (bl && d >= 6.0) {
                    yield 1.22;
                }
                yield 1.0;
            }
            case 3 -> {
                if (!bl && d2 < 0.6) {
                    yield 1.25;
                }
                yield 1.0;
            }
            case 6 -> {
                if (bl) {
                    yield 1.2;
                }
                yield 1.0;
            }
            case 7 -> {
                if (!bl && bl2) {
                    yield 1.2;
                }
                yield 1.0;
            }
            case 11 -> {
                if (bl && n2 % 3 == 0) {
                    yield 1.35;
                }
                yield 1.0;
            }
            default -> 1.0;
        };
    }

    public static boolean rangedHit(int n, boolean bl, String string, String string2, double d) {
        String string3 = string.toLowerCase(Locale.ROOT);
        return bl || string3.contains("magic") || string3.contains("spell") || string2.startsWith("io.redspace.ironsspellbooks.damage.") || n == 2 && d >= 6.0;
    }

    public static double armor(int n) {
        return n == 0 ? 4.0 : (n == 8 ? 2.0 : (n == 10 ? 3.0 : 0.0));
    }

    public static double speed(int n) {
        return n == 7 ? 0.12 : 0.0;
    }

    public static double knockbackResistance(int n) {
        return n == 6 ? 0.35 : (n == 10 ? 0.2 : 0.0);
    }

    public static double attack(int n) {
        return n == 4 ? 0.12 : 0.0;
    }
}

