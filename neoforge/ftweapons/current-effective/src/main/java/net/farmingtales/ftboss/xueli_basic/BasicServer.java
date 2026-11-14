package net.farmingtales.ftboss.xueli_basic;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BasicServer {
    private static final String[] PIN = new String[]{"pin_diagonal", "pin_cross", "pin_press"};
    private static final String[] PAN = new String[]{"pan_forehand", "pan_backhand", "pan_rise"};
    private static final Map<EasterBossEntity, Integer> INTERVALS = new WeakHashMap<>();

    private BasicServer() {
    }

    public static String weapon(EasterBossEntity var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getMainHandItem().getItem());
        if (var1 == null) {
            return "";
        } else {
            String var2 = var1.toString();
            return !var2.equals("bakery:rolling_pin") && !var2.equals("ftboss:xueli_rolling_pin")
                ? (!var2.equals("farmersdelight:skillet") && !var2.equals("ftboss:xueli_skillet") ? "" : "pan")
                : "pin";
        }
    }

    public static boolean supported(EasterBossEntity var0) {
        return var0 != null
            && var0.getVariant() == 10
            && var0 instanceof BasicAccess
            && var0.isAlive()
            && !var0.isNoAi()
            && !var0.isCastingLike()
            && !weapon(var0).isEmpty();
    }

    private static String attack(String var0, int var1) {
        return (var0.equals("pin") ? PIN : PAN)[Math.floorMod(var1, 3)];
    }

    private static CompoundTag cue(EasterBossEntity var0) {
        return ((BasicAccess)var0).ftboss$getXueliBasic();
    }

    private static void set(EasterBossEntity var0, CompoundTag var1) {
        ((BasicAccess)var0).ftboss$setXueliBasic(var1);
    }

    private static CompoundTag create(EasterBossEntity var0, String var1, int var2, int var3, int var4, Entity var5) {
        CompoundTag var6 = new CompoundTag();
        String var7 = weapon(var0);
        var6.putString("phase", var1);
        var6.putString("weapon", var7);
        var6.putString("attack", attack(var7, var4));
        var6.putLong("start", var0.level().getGameTime());
        var6.putInt("duration", Math.max(1, var2));
        var6.putInt("interval", Math.max(1, var3));
        var6.putInt("sequence", var4);
        var6.putBoolean("left", var0.getMainArm() == HumanoidArm.LEFT);
        if (var5 != null) {
            var6.putUUID("target", var5.getUUID());
        }

        return var6;
    }

    public static void prepare(EasterBossEntity var0, LivingEntity var1, double var2, int var4, int var5, double var6) {
        if (!var0.level().isClientSide && var0 instanceof BasicAccess) {
            if (var0.getVariant() == 10) {
                INTERVALS.put(var0, Math.max(1, var5));
            }

            CompoundTag var8 = cue(var0);
            boolean var9 = var8.getString("phase").equals("wind");
            if (supported(var0) && var1 != null && var1.isAlive() && !(var2 > var6) && var0.hasLineOfSight(var1)) {
                if (var4 > 0 && var4 <= BasicMath.wind(var5)) {
                    long var10 = var0.level().getGameTime();
                    if (!var9
                        || !var8.getString("weapon").equals(weapon(var0))
                        || !var8.hasUUID("target")
                        || !var8.getUUID("target").equals(var1.getUUID())
                        || var10 - var8.getLong("start") >= (long)var8.getInt("duration")) {
                        int var12 = var8.getInt("sequence") + (var8.getString("phase").equals("release") ? 1 : 0);
                        set(var0, create(var0, "wind", var4, var5, var12, var1));
                    }
                }
            } else {
                if (var9) {
                    set(var0, new CompoundTag());
                }
            }
        }
    }

    public static void strike(EasterBossEntity var0, Entity var1) {
        if (!var0.level().isClientSide && supported(var0)) {
            CompoundTag var2 = cue(var0);
            int var3 = INTERVALS.getOrDefault(var0, var2.isEmpty() ? 20 : Math.max(1, var2.getInt("interval")));
            int var4 = var2.getInt("sequence") + (var2.getString("phase").equals("release") ? 1 : 0);
            set(var0, create(var0, "release", BasicMath.recovery(var3), var3, var4, var1));
        }
    }

    public static void validate(EasterBossEntity var0, boolean var1) {
        if (!var0.level().isClientSide && var0.getVariant() == 10 && var0 instanceof BasicAccess) {
            CompoundTag var2 = cue(var0);
            if (var2.getString("phase").equals("wind")) {
                LivingEntity var3 = var0.getTarget();
                if (!supported(var0) || var1 || var3 == null || !var3.isAlive() || !var2.hasUUID("target") || !var2.getUUID("target").equals(var3.getUUID())) {
                    set(var0, new CompoundTag());
                }
            }
        }
    }

    public static void cancel(EasterBossEntity var0, boolean var1) {
        if (!var0.level().isClientSide && var0.getVariant() == 10 && var0 instanceof BasicAccess) {
            CompoundTag var2 = cue(var0);
            if (var1 || !var2.getString("phase").equals("release") || var2.getLong("start") != var0.level().getGameTime()) {
                if (!var2.isEmpty()) {
                    set(var0, new CompoundTag());
                }
            }
        }
    }
}
