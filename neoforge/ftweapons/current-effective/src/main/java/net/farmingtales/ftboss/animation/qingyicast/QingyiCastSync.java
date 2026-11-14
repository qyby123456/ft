package net.farmingtales.ftboss.animation.qingyicast;

import net.farmingtales.ftboss.animation.qingyicast.QingyiCastTime.Sample;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;

public final class QingyiCastSync {
    private QingyiCastSync() {
    }

    private static boolean valid(EasterBossEntity var0) {
        return var0.getVariant() == 5 && var0 instanceof QingyiCastAccess && !var0.level().isClientSide;
    }

    private static long now(EasterBossEntity var0) {
        return var0.level().getGameTime();
    }

    private static CompoundTag state(EasterBossEntity var0, String var1) {
        return ((QingyiCastAccess)var0).qingyiCastState().getCompound(var1).copy();
    }

    private static void save(EasterBossEntity var0, String var1, CompoundTag var2) {
        QingyiCastAccess var3 = (QingyiCastAccess)var0;
        CompoundTag var4 = var3.qingyiCastState().copy();
        var4.put(var1, var2);
        var3.qingyiCastState(var4);
    }

    private static Sample sample(CompoundTag var0, long var1) {
        int var3 = var0.getInt("stage");
        long var4 = var0.getLong("start");
        long var6 = var0.getLong("stop");
        return QingyiCastTime.sample(
            var0.getInt("track"),
            (double)(var1 - (var3 == 0 ? var4 : var6)),
            var0.getInt("duration"),
            var3,
            (double)(var6 - var4),
            var0.getBoolean("final"),
            var0.getDouble("origin"),
            (double)(var0.getLong("finalAt") - var6)
        );
    }

    private static void begin(EasterBossEntity var0, String var1, int var2, int var3) {
        if (valid(var0) && var2 >= 0 && var3 >= 1 && var3 <= 200) {
            CompoundTag var4 = new CompoundTag();
            var4.putInt("track", var2);
            var4.putInt("duration", var3);
            var4.putInt("stage", 0);
            var4.putLong("start", now(var0));
            var4.putDouble("origin", 0.42);
            save(var0, var1, var4);
        }
    }

    public static void start(EasterBossEntity var0, Kind var1, int var2) {
        if (var1 != null) {
            String var4 = var1.id;

            byte var3 = switch (var4) {
                case "four_tides" -> 0;
                case "elixir_rain" -> 1;
                case "tidal_lotus" -> 2;
                default -> -1;
            };
            begin(var0, "iron", var3, var2);
        }
    }

    public static void startNative(EasterBossEntity var0, Skill var1) {
        if (var1 != null && var1.variant == 5) {
            String var3 = var1.id();

            byte var2 = switch (var3) {
                case "qingyi_tide" -> 3;
                case "qingyi_thunder" -> 4;
                case "qingyi_star" -> 5;
                default -> -1;
            };
            begin(var0, "native", var2, var0.getChallengeDifficulty().acceleratedTicks(var1.windup));
        }
    }

    public static void startConfluence(EasterBossEntity var0, int var1) {
        begin(var0, "native", 6, 32 - var1);
    }

    public static void startRain(EasterBossEntity var0) {
        begin(var0, "native", 7, 1);
    }

    private static void pulse(EasterBossEntity var0, String var1, boolean var2) {
        if (valid(var0)) {
            CompoundTag var3 = state(var0, var1);
            if (!var3.isEmpty() && var3.getInt("stage") != 2 && !var3.getBoolean("final")) {
                long var4 = now(var0);
                double var6 = sample(var3, var4).progress();
                var3.putInt("stage", 1);
                var3.putLong("stop", var4);
                var3.putDouble("origin", var6);
                var3.putBoolean("final", var2);
                var3.putInt("pulses", var3.getInt("pulses") + 1);
                save(var0, var1, var3);
            }
        }
    }

    public static void release(EasterBossEntity var0) {
        pulse(var0, "iron", true);
    }

    public static void pulseNative(EasterBossEntity var0) {
        pulse(var0, "native", false);
    }

    public static void finishNative(EasterBossEntity var0) {
        if (valid(var0)) {
            CompoundTag var1 = state(var0, "native");
            if (!var1.isEmpty() && var1.getInt("stage") == 1 && !var1.getBoolean("final")) {
                if (var1.getInt("track") == 7) {
                    var1.putDouble("origin", sample(var1, now(var0)).progress());
                }

                var1.putLong("finalAt", now(var0));
                var1.putBoolean("final", true);
                save(var0, "native", var1);
            }
        }
    }

    private static void cancel(EasterBossEntity var0, String var1) {
        if (valid(var0)) {
            CompoundTag var2 = state(var0, var1);
            if (!var2.isEmpty() && var2.getInt("stage") != 2 && !var2.getBoolean("final")) {
                long var3 = now(var0);
                var2.putDouble("origin", sample(var2, var3).progress());
                var2.putInt("stage", 2);
                var2.putLong("stop", var3);
                save(var0, var1, var2);
            }
        }
    }

    public static void finish(EasterBossEntity var0, boolean var1) {
        cancel(var0, "iron");
    }

    public static void cancelNative(EasterBossEntity var0) {
        cancel(var0, "native");
    }

    public static void clear(EasterBossEntity var0) {
        cancel(var0, "iron");
        cancel(var0, "native");
    }

    public static void rainPulse(EasterBossEntity var0, int var1) {
        pulseNative(var0);
        if (var1 <= 0) {
            finishNative(var0);
        }
    }

    public static void rainTick(EasterBossEntity var0, int var1) {
        if (valid(var0) && var1 <= 0) {
            CompoundTag var2 = state(var0, "native");
            if (!var2.isEmpty() && var2.getInt("track") == 7 && !var2.getBoolean("final")) {
                cancelNative(var0);
            }
        }
    }
}
