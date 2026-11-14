package net.farmingtales.ftboss.client.render;

import java.util.Set;

public final class QingyiWaterTiming {
    public static final Set<String> IDS = Set.of("tidal_lotus", "elixir_rain");

    private QingyiWaterTiming() {
    }

    public static boolean handles(String var0) {
        return "four_tides".equals(var0) || IDS.contains(var0);
    }

    public static int lifetime(String var0, String var1, int var2) {
        if ("tidal_lotus".equals(var0) && "RELEASE".equals(var1)) {
            return 60;
        } else if ("elixir_rain".equals(var0) && "RELEASE".equals(var1)) {
            return 80;
        } else {
            return "elixir_rain".equals(var0) && "PULSE".equals(var1) ? 20 : var2;
        }
    }

    public static QingyiWaterTiming.State state(String var0, String var1, double var2, double var4, int var6) {
        int var7 = lifetime(var0, var1, var6);
        if (IDS.contains(var0) && Double.isFinite(var2) && Double.isFinite(var4) && !(var2 < 0.0) && !(var2 >= (double)var7)) {
            boolean var8 = "CHARGE".equals(var1);
            boolean var9 = !var8 && "RELEASE".equals(var1) && ("undertow_snare".equals(var0) || "riptide_corridor".equals(var0));
            float var10 = (float)(
                QingyiWaveTiming.smooth(var2 / (double)(var8 ? 1 : 2)) * QingyiWaveTiming.smooth(((double)var7 - var2) / Math.min(8.0, (double)var7 * 0.35))
            );
            if (!var8 && !var9) {
                String var14 = switch (var0) {
                    case "tidal_lotus" -> "bloom";
                    case "elixir_rain" -> "rain";
                    default -> throw new IllegalArgumentException("Unknown Qingyi water cast");
                };
                return new QingyiWaterTiming.State(var14, (float)(var2 * 0.05), var10, false);
            } else {
                float var11 = "riptide_corridor".equals(var0) ? 1.0F : 0.5F;
                return new QingyiWaterTiming.State("gather", (float)Math.max(0.0, Math.min((double)var11, var4 * 0.05)), var10 * (var8 ? 0.48F : 0.55F), var9);
            }
        } else {
            return new QingyiWaterTiming.State("gather", 0.0F, 0.0F, false);
        }
    }

    public static float faceOpacity(String var0, String var1, double var2, boolean var4) {
        if (var4 && "abyssal_carapace".equals(var0) && var1.contains("shell")) {
            return 0.08F;
        } else {
            return var1.startsWith("initial_") ? (float)(1.0 - QingyiWaveTiming.smooth((var2 - 0.45) / 0.45)) : 1.0F;
        }
    }

    public static record State(String clip, float time, float opacity, boolean warning) {
    }
}
