package net.farmingtales.ftboss.qingyi_basic;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.item.weapons.QingyiConfluenceTridentItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.BuiltInRegistries;

public final class BasicServer {
    private static final String[] CLIPS = new String[]{"trident_thrust", "trident_sweep", "trident_backcut"};

    private BasicServer() {
    }

    public static String weapon(EasterBossEntity var0) {
        ResourceLocation var1 = BuiltInRegistries.ITEM.getKey(var0.getMainHandItem().getItem());
        return var1 == null ? "" : var1.toString();
    }

    public static boolean supported(EasterBossEntity var0) {
        return var0 != null
            && var0.getVariant() == 5
            && !var0.isMini()
            && !var0.isGiant()
            && !var0.isNoAi()
            && var0.isAlive()
            && var0 instanceof BasicAccess
            && var0.getMainHandItem().getItem() instanceof QingyiConfluenceTridentItem;
    }

    private static CompoundTag cue(EasterBossEntity var0) {
        return ((BasicAccess)var0).ftboss$getQingyiBasic();
    }

    private static void set(EasterBossEntity var0, CompoundTag var1) {
        ((BasicAccess)var0).ftboss$setQingyiBasic(var1);
    }

    private static boolean ownServer(EasterBossEntity var0) {
        return supported(var0) && !var0.level().isClientSide;
    }

    public static void retainTail(CompoundTag var0, CompoundTag var1) {
        if (var0.contains("tailStart")) {
            var1.putLong("tailStart", var0.getLong("tailStart"));
            var1.putString("tailWeapon", var0.getString("tailWeapon"));
            var1.putBoolean("tailLeft", var0.getBoolean("tailLeft"));
        }
    }

    private static CompoundTag create(EasterBossEntity var0, Entity var1, boolean var2, int var3, int var4, int var5) {
        CompoundTag var6 = new CompoundTag();
        retainTail(cue(var0), var6);
        var6.putString("clip", CLIPS[Math.floorMod(var5, 3)]);
        var6.putBoolean("preparation", var2);
        var6.putBoolean("active", true);
        var6.putLong("start", var0.level().getGameTime());
        var6.putInt("duration", var3);
        var6.putInt("interval", var4);
        var6.putInt("sequence", var5 + (var2 ? 0 : 1));
        var6.putString("weapon", weapon(var0));
        var6.putBoolean("left", var0.getMainArm() == HumanoidArm.LEFT);
        if (!var2) {
            var6.putLong("tailStart", var0.level().getGameTime());
            var6.putString("tailWeapon", weapon(var0));
            var6.putBoolean("tailLeft", var0.getMainArm() == HumanoidArm.LEFT);
        }

        if (var1 != null) {
            var6.putUUID("target", var1.getUUID());
        }

        return var6;
    }

    public static void prepare(EasterBossEntity var0, LivingEntity var1, double var2, int var4, int var5, double var6, boolean var8) {
        if (var0 instanceof BasicAccess && var0.getVariant() == 5 && !var0.level().isClientSide) {
            CompoundTag var9 = cue(var0);
            boolean var10 = var9.getBoolean("active") && var9.getBoolean("preparation");
            if (ownServer(var0) && !var8 && var1 != null && var1.isAlive() && !(var2 > var6) && var0.hasLineOfSight(var1)) {
                var5 = Math.max(1, var5);
                if (var9.getInt("interval") != var5) {
                    var9 = var9.copy();
                    var9.putInt("interval", var5);
                    set(var0, var9);
                }

                if (var4 >= 1 && var4 <= BasicMotion.preparation(var5)) {
                    long var11 = var0.level().getGameTime();
                    if (!var10
                        || !var9.hasUUID("target")
                        || !var9.getUUID("target").equals(var1.getUUID())
                        || !var9.getString("weapon").equals(weapon(var0))
                        || var9.getBoolean("left") != (var0.getMainArm() == HumanoidArm.LEFT)
                        || var11 - var9.getLong("start") >= (long)var9.getInt("duration")) {
                        set(var0, create(var0, var1, true, var4, var5, var9.getInt("sequence")));
                    }
                }
            } else {
                if (var10) {
                    cancel(var0);
                }
            }
        }
    }

    public static void strike(EasterBossEntity var0, Entity var1) {
        if (ownServer(var0)) {
            CompoundTag var2 = cue(var0);
            int var3 = Math.max(1, var2.getInt("interval"));
            set(var0, create(var0, var1, false, BasicMotion.recovery(var3), var3, var2.getInt("sequence")));
        }
    }

    public static void cancel(EasterBossEntity var0) {
        if (var0 instanceof BasicAccess && var0.getVariant() == 5 && !var0.level().isClientSide) {
            CompoundTag var1 = cue(var0);
            if (!var1.getBoolean("active")) {
                return;
            }

            CompoundTag var2 = new CompoundTag();
            retainTail(var1, var2);
            var2.putInt("sequence", var1.getInt("sequence"));
            var2.putInt("interval", var1.getInt("interval"));
            set(var0, var2);
        }
    }

    public static void validate(EasterBossEntity var0, boolean var1) {
        if (var0 instanceof BasicAccess && var0.getVariant() == 5 && !var0.level().isClientSide) {
            CompoundTag var2 = cue(var0);
            if (var2.getBoolean("active")) {
                LivingEntity var3 = var0.getTarget();
                if (!supported(var0)
                    || var1
                    || !var2.getString("weapon").equals(weapon(var0))
                    || var2.getBoolean("left") != (var0.getMainArm() == HumanoidArm.LEFT)
                    || var0.level().getGameTime() - var2.getLong("start") >= (long)var2.getInt("duration")
                    || var2.getBoolean("preparation")
                        && (var3 == null || !var3.isAlive() || !var2.hasUUID("target") || !var2.getUUID("target").equals(var3.getUUID()))) {
                    cancel(var0);
                }
            }
        }
    }
}
