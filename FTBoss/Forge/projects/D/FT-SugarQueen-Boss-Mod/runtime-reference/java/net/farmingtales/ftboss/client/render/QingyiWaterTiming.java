/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import java.util.Set;
import net.farmingtales.ftboss.client.render.QingyiWaveTiming;

public final class QingyiWaterTiming {
    public static final Set<String> IDS = Set.of("tidal_lotus", "elixir_rain");

    private QingyiWaterTiming() {
    }

    public static boolean handles(String string) {
        return "four_tides".equals(string) || IDS.contains(string);
    }

    public static int lifetime(String string, String string2, int n) {
        if ("tidal_lotus".equals(string) && "RELEASE".equals(string2)) {
            return 60;
        }
        if ("elixir_rain".equals(string) && "RELEASE".equals(string2)) {
            return 80;
        }
        if ("elixir_rain".equals(string) && "PULSE".equals(string2)) {
            return 20;
        }
        return n;
    }

    public static State state(String string, String string2, double d, double d2, int n) {
        int n2 = QingyiWaterTiming.lifetime(string, string2, n);
        if (!IDS.contains(string) || !Double.isFinite(d) || !Double.isFinite(d2) || d < 0.0 || d >= (double)n2) {
            return new State("gather", 0.0f, 0.0f, false);
        }
        boolean bl = "CHARGE".equals(string2);
        boolean bl2 = !bl && "RELEASE".equals(string2) && ("undertow_snare".equals(string) || "riptide_corridor".equals(string));
        float f = (float)(QingyiWaveTiming.smooth(d / (double)(bl ? 1 : 2)) * QingyiWaveTiming.smooth(((double)n2 - d) / Math.min(8.0, (double)n2 * 0.35)));
        if (bl || bl2) {
            float f2 = "riptide_corridor".equals(string) ? 1.0f : 0.5f;
            return new State("gather", (float)Math.max(0.0, Math.min((double)f2, d2 * 0.05)), f * (bl ? 0.48f : 0.55f), bl2);
        }
        String string3 = switch (string) {
            case "tidal_lotus" -> "bloom";
            case "elixir_rain" -> "rain";
            default -> throw new IllegalArgumentException("Unknown Qingyi water cast");
        };
        return new State(string3, (float)(d * 0.05), f, false);
    }

    public static float faceOpacity(String string, String string2, double d, boolean bl) {
        if (bl && "abyssal_carapace".equals(string) && string2.contains("shell")) {
            return 0.08f;
        }
        if (string2.startsWith("initial_")) {
            return (float)(1.0 - QingyiWaveTiming.smooth((d - 0.45) / 0.45));
        }
        return 1.0f;
    }

    public record State(String clip, float time, float opacity, boolean warning) {
    }
}

