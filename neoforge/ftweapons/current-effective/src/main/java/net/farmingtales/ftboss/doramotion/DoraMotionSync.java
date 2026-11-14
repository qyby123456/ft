package net.farmingtales.ftboss.doramotion;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.DoraSniperCycle;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;

public final class DoraMotionSync {
    private DoraMotionSync() {
    }

    private static DoraMotionAccess access(EasterBossEntity var0) {
        return var0.getVariant() == 2 && !var0.isMini() && !var0.level().isClientSide && (Object)var0 instanceof DoraMotionAccess ? (DoraMotionAccess)(Object)var0 : null;
    }

    public static void capture(EasterBossEntity var0, int var1, int var2, double var3, boolean var5) {
        DoraMotionAccess var6 = access(var0);
        if (var6 != null) {
            CompoundTag var7 = var6.doraMotionState();
            long var8 = var0.level().getGameTime();
            CompoundTag var10 = DoraMotionSnapshot.update(var7, var8, var1, var2, var3, var5, var0.getChallengeDifficulty().attackRate() / 60.0);
            if (var10 != var7) {
                var6.doraMotionState(var10);
            }
        }
    }

    public static void shot(EasterBossEntity var0) {
        stamp(var0, "shotAt");
    }

    public static void spear(EasterBossEntity var0) {
        stamp(var0, "spearAt");
    }

    public static void fire(EasterBossEntity var0) {
        stamp(var0, "fireAt");
        DoraMotionAccess var1 = access(var0);
        if (var1 != null) {
            CompoundTag var2 = var1.doraMotionState().copy();
            var2.putInt("recovery", var0.isImpossibleChallenge() ? 6 : 12);
            var1.doraMotionState(var2);
        }
    }

    private static void stamp(EasterBossEntity var0, String var1) {
        DoraMotionAccess var2 = access(var0);
        if (var2 != null) {
            CompoundTag var3 = var2.doraMotionState().copy();
            var3.putLong(var1, var0.level().getGameTime());
            var3.putLong("sampleAt", var0.level().getGameTime());
            var3.putInt(var1 + "Serial", var3.getInt(var1 + "Serial") + 1);
            var2.doraMotionState(var3);
        }
    }

    public static void sniper(EasterBossEntity var0) {
        DoraMotionAccess var1 = access(var0);
        if (var1 != null) {
            CompoundTag var2 = var1.doraMotionState().copy();
            var2.putLong("sniperAt", var0.level().getGameTime());
            var2.putLong("sampleAt", var0.level().getGameTime());
            var2.putInt("charge", var0.isImpossibleChallenge() ? (DoraSniperCycle.CHARGE_TICKS + 1) / 2 : DoraSniperCycle.CHARGE_TICKS);
            var2.putInt("lock", var0.isImpossibleChallenge() ? (DoraSniperCycle.TRACK_TICKS + 1) / 2 : DoraSniperCycle.TRACK_TICKS);
            var2.putBoolean("sniper", true);
            var2.remove("fireAt");
            var1.doraMotionState(var2);
        }
    }

    public static void clear(EasterBossEntity var0) {
        DoraMotionAccess var1 = access(var0);
        if (var1 != null && !var1.doraMotionState().isEmpty()) {
            var1.doraMotionState(new CompoundTag());
        }
    }

    public static void cancelSniper(EasterBossEntity var0) {
        DoraMotionAccess var1 = access(var0);
        if (var1 != null) {
            CompoundTag var2 = var1.doraMotionState().copy();
            if (var2.contains("sniperAt") || var2.contains("fireAt")) {
                var2.putBoolean("sniper", false);
                var2.remove("sniperAt");
                var2.remove("fireAt");
                if (!var2.equals(var1.doraMotionState())) {
                    var1.doraMotionState(var2);
                }
            }
        }
    }

    public static void begin(Kind var0, LivingEntity var1, MagicData var2) {
        if (var0 != null
            && var0.variant == 2
            && var1 instanceof EasterBossEntity var3
            && var2 != null
            && DoraMotionSnapshot.primary(var2.getCastingEquipmentSlot())) {
            DoraMotionAccess var4 = access(var3);
            if (var4 == null) {
                return;
            }

            CompoundTag var5 = var4.doraMotionState().copy();
            CompoundTag var6 = new CompoundTag();
            var6.putString("id", var0.id);
            var6.putLong("start", var3.level().getGameTime());
            var6.putInt("duration", Math.max(1, Math.min(2400, var2.getCastDuration())));
            var6.putInt("phase", 0);
            var5.put("spell", var6);
            var4.doraMotionState(var5);
            return;
        }
    }

    public static void release(Kind var0, LivingEntity var1, MagicData var2) {
        end(var0, var1, var2, false);
    }

    public static void complete(Kind var0, LivingEntity var1, MagicData var2, boolean var3) {
        if (var3) {
            end(var0, var1, var2, true);
        }
    }

    private static void end(Kind var0, LivingEntity var1, MagicData var2, boolean var3) {
        if (var0 != null
            && var0.variant == 2
            && var1 instanceof EasterBossEntity var4
            && var2 != null
            && DoraMotionSnapshot.primary(var2.getCastingEquipmentSlot())) {
            DoraMotionAccess var5 = access(var4);
            if (var5 == null) {
                return;
            }

            CompoundTag var6 = var5.doraMotionState().copy();
            CompoundTag var7 = var6.getCompound("spell");
            if (var0.id.equals(var7.getString("id")) && var7.getInt("phase") == 0) {
                var7.putInt("phase", var3 ? 2 : 1);
                var7.putLong("stop", var4.level().getGameTime());
                var5.doraMotionState(var6);
                return;
            }

            return;
        }
    }
}
