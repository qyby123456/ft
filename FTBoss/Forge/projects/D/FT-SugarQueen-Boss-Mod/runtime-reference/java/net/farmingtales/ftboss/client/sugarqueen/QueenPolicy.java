/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.joml.Matrix4f
 */
package net.farmingtales.ftboss.client.sugarqueen;

import java.util.Set;
import org.joml.Matrix4f;

public final class QueenPolicy {
    public static final Set<String> SPELLS = Set.of("crystal_coronation", "frostglass_lance", "sugar_hail", "glacial_prism", "royal_shatter");

    private QueenPolicy() {
    }

    public static boolean accepts(String string, String string2) {
        return "ftboss:sugar_queen".equals(string) && SPELLS.contains(string2);
    }

    public static boolean phase(String string, String string2) {
        return SPELLS.contains(string) && ("CHARGE".equals(string2) || "RELEASE".equals(string2) || ("sugar_hail".equals(string) || "glacial_prism".equals(string) || "royal_shatter".equals(string)) && "PULSE".equals(string2) || "frostglass_lance".equals(string) && "BEAM".equals(string2));
    }

    public static float validScale(double d) {
        return Double.isFinite(d) && d > 0.0 && d <= 16.0 ? (float)d : 1.0f;
    }

    public static float chargeTime(int n, int n2, float f) {
        if (n <= 0 || n2 < 0 || n2 > n || !Float.isFinite(f)) {
            return 0.18f;
        }
        float f2 = Math.max(0.0f, Math.min(1.0f, ((float)(n - n2) + Math.max(0.0f, Math.min(1.0f, f))) / (float)n));
        return 0.04f + f2 * 0.14f;
    }

    public static float chargePose(String string, float f) {
        return "sugar_hail".equals(string) ? 0.24f + Math.max(0.0f, Math.min(1.0f, (f - 0.04f) / 0.14f)) * 0.035f : f;
    }

    public static void spatial(Matrix4f matrix4f, boolean bl, String string, float f, double d) {
        float f2 = QueenPolicy.validScale(f);
        if (bl) {
            matrix4f.scale(f2, f2, "BEAM".equals(string) ? (float)(d / 4.75) : 0.2f);
        } else {
            matrix4f.scale(f2, 1.0f, f2);
        }
    }

    public static int lifeTicks(String string, String string2) {
        if ("CHARGE".equals(string2)) {
            return 6;
        }
        return switch (string) {
            case "sugar_hail" -> 56;
            case "royal_shatter" -> 36;
            case "glacial_prism" -> 160;
            case "frostglass_lance" -> {
                if ("BEAM".equals(string2)) {
                    yield 9;
                }
                yield 5;
            }
            default -> 32;
        };
    }

    public static float time(String string, String string2, float f) {
        if ("CHARGE".equals(string2)) {
            return 0.18f;
        }
        if ("glacial_prism".equals(string)) {
            if (f < 6.0f) {
                return f / 6.0f * 0.18f;
            }
            if (f > 150.0f) {
                return 0.8f + (f - 150.0f) / 10.0f * 0.2f;
            }
            return 0.18f + (f - 6.0f) / 144.0f * 0.62f;
        }
        return Math.max(0.0f, Math.min(1.0f, f / (float)QueenPolicy.lifeTicks(string, string2)));
    }

    public static float opacity(String string, float f, int n) {
        if ("CHARGE".equals(string)) {
            return 0.56f;
        }
        return Math.max(0.0f, Math.min(1.0f, ((float)n - f) / Math.min(7.0f, (float)n * 0.3f)));
    }

    public static boolean detailPart(String string) {
        return string.startsWith("silver_spine_") || string.startsWith("rose_jewel_") || string.startsWith("facet_ridge_") || string.startsWith("floating_tail_chip_") || string.startsWith("shield_silver_trim_") || string.startsWith("sugar_clasp_");
    }
}

