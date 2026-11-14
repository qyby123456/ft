package net.farmingtales.ftboss.animation.zicaicast;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;

public final class ZicaiCastSync {
    private ZicaiCastSync() {
    }

    public static void start(EasterBossEntity var0, Kind var1, int var2) {
        if (var0.getVariant() == 8 && var0 instanceof ZicaiCastAccess var3) {
            String var5 = var1.id;

            byte var4 = switch (var5) {
                case "luminous_rebuke" -> 0;
                case "halo_sanctuary" -> 1;
                case "seraph_descent" -> 2;
                default -> -1;
            };
            if (var4 >= 0 && var2 >= 1 && var2 <= 200) {
                CompoundTag var7 = new CompoundTag();
                var7.putInt("track", var4);
                var7.putInt("duration", var2);
                var7.putLong("start", var0.level().getGameTime());
                var7.putInt("stage", 0);
                var3.zicaiCastState(var7);
            }
        }
    }

    public static void release(EasterBossEntity var0) {
        stop(var0, 1);
    }

    public static void finish(EasterBossEntity var0, boolean var1) {
        stop(var0, 2);
    }

    private static void stop(EasterBossEntity var0, int var1) {
        if (var0.getVariant() == 8 && var0 instanceof ZicaiCastAccess var2) {
            CompoundTag var3 = var2.zicaiCastState();
            if (!var3.isEmpty() && var3.getInt("stage") == 0) {
                var3.putLong("stop", var0.level().getGameTime());
                var3.putInt("stage", var1);
                var2.zicaiCastState(var3);
            }
        }
    }
}
