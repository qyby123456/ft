package net.farmingtales.ftboss.xueli_cast_actions;

import java.util.Set;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.core.registries.BuiltInRegistries;

public final class CastServer {
    private static final Set<String> WIRED = Set.of(
        "frost_needles", "whiteout_edict", "winter_clock", "pan_cast", "stockpot_throw", "knife_throw", "clone_call", "maid_call", "roll_cast", "slam_cast"
    );

    private CastServer() {
    }

    public static boolean wired(String var0) {
        return var0 != null && WIRED.contains(var0);
    }

    public static boolean validCue(String var0, String var1, int var2) {
        return wired(var0)
            && var1 != null
            && var2 >= 1
            && var2 <= 240
            && (var1.equals("charge") || var1.equals("release") || var1.equals("instant") || var1.equals("hold"));
    }

    public static boolean supported(EasterBossEntity var0) {
        return var0 != null && var0.getVariant() == 10 && !var0.isMini() && !var0.isNoAi() && var0.isAlive() && var0 instanceof CastAccess;
    }

    public static String weapon(EasterBossEntity var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getMainHandItem().getItem());
        return var1 == null ? "" : var1.toString();
    }

    private static CompoundTag root(EasterBossEntity var0) {
        return ((CastAccess)var0).ftboss$getXueliCastActions();
    }

    private static void write(EasterBossEntity var0, String var1, CompoundTag var2) {
        CompoundTag var3 = root(var0).copy();
        var3.put(var1, var2);
        ((CastAccess)var0).ftboss$setXueliCastActions(var3);
    }

    private static CompoundTag make(EasterBossEntity var0, String var1, String var2, String var3, int var4) {
        CompoundTag var5 = new CompoundTag();
        var5.putString("id", var1);
        var5.putString("secondary", var2);
        var5.putString("stage", var3);
        var5.putLong("start", var0.level().getGameTime());
        var5.putInt("duration", Math.max(1, Math.min(240, var4)));
        var5.putString("weapon", weapon(var0));
        var5.putBoolean("left", var0.getMainArm() == HumanoidArm.LEFT);
        return var5;
    }

    public static void iron(EasterBossEntity var0, Kind var1, Kind var2, int var3) {
        if (supported(var0) && var0.level() instanceof ServerLevel && var1 != null && var1.variant == 10 && wired(var1.id) && var3 >= 1) {
            write(var0, "iron", make(var0, var1.id, var2 != null && var2.variant == 10 && wired(var2.id) ? var2.id : "", "charge", var3));
        }
    }

    public static void finishIron(EasterBossEntity var0, boolean var1) {
        if (var0 instanceof CastAccess && var0.level() instanceof ServerLevel && var0.getVariant() == 10) {
            CompoundTag var2 = root(var0).getCompound("iron");
            if (!var2.isEmpty()) {
                if (!var1 && wired(var2.getString("id"))) {
                    write(var0, "iron", make(var0, var2.getString("id"), var2.getString("secondary"), "release", 10));
                } else {
                    write(var0, "iron", new CompoundTag());
                }
            }
        }
    }

    public static void nativeAction(EasterBossEntity var0, String var1, String var2, int var3) {
        if (validCue(var1, var2, var3) && supported(var0) && var0.level() instanceof ServerLevel) {
            write(var0, "native", make(var0, var1, "", var2, var3));
        }
    }

    public static void spawned(boolean var0, EasterBossEntity var1, String var2) {
        if (var0 && supported(var1)) {
            CompoundTag var3 = root(var1).getCompound("native");
            if (!var3.getString("id").equals(var2) || var3.getLong("start") != var1.level().getGameTime()) {
                nativeAction(var1, var2, "instant", var2.equals("knife_throw") ? 16 : (var2.equals("stockpot_throw") ? 20 : 12));
            }
        }
    }

    public static void reset(EasterBossEntity var0) {
        if (var0 instanceof CastAccess && !var0.level().isClientSide) {
            ((CastAccess)var0).ftboss$setXueliCastActions(new CompoundTag());
        }
    }

    public static void cancel(EasterBossEntity var0) {
        if (var0 instanceof CastAccess && var0.getVariant() == 10 && !var0.level().isClientSide) {
            write(var0, "native", new CompoundTag());
        }
    }
}
