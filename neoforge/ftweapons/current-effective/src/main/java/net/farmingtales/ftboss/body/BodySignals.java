package net.farmingtales.ftboss.body;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

public final class BodySignals {
    private BodySignals() {
    }

    public static void mark(EasterBossEntity var0, String var1, int var2) {
        if (var0.getVariant() == 6 && !var0.level().isClientSide) {
            BodyCueAccess var3 = (BodyCueAccess)var0;
            CompoundTag var4 = var3.ftboss$bodyRead().copy();
            CompoundTag var5 = new CompoundTag();
            var5.putString("id", var1);
            var5.putLong("start", var0.level().getGameTime());
            var5.putInt("windup", Math.max(0, Math.min(200, var2)));
            var4.put("xuanlanBody", var5);
            var3.ftboss$bodyWrite(var4);
        }
    }

    public static void flywheel(EasterBossEntity var0) {
        mark(var0, "flywheel", var0.getChallengeDifficulty().acceleratedTicks(8) * 6);
    }

    public static void beam(EasterBossEntity var0) {
        mark(var0, "beam", 0);
    }

    public static void combo(EasterBossEntity var0) {
        mark(var0, "combo", 12);
    }

    public static void vent(EasterBossEntity var0) {
        mark(var0, "vent", var0.getChallengeDifficulty().acceleratedTicks(10));
    }

    public static void five(EasterBossEntity var0, Skill var1, Vec3 var2, int var3) {
        if (var1 == Skill.XUANLAN_RAIL) {
            mark(var0, "rail", var1.windup - var3);
        } else {
            if (var1 != Skill.XUANLAN_RING) {
                return;
            }

            mark(var0, "ring", var1.windup - var3);
        }

        if (var0.getVariant() == 6) {
            BodyCueAccess var4 = (BodyCueAccess)var0;
            CompoundTag var5 = var4.ftboss$bodyRead().copy();
            CompoundTag var6 = var5.getCompound("xuanlanBody");
            var6.putDouble("aimYaw", Math.toDegrees(-Math.atan2(var2.x, var2.z)));
            var6.putDouble("aimPitch", Math.toDegrees(-Math.atan2(var2.y, Math.hypot(var2.x, var2.z))));
            var6.putBoolean("lockedAim", true);
            var4.ftboss$bodyWrite(var5);
        }
    }

    public static void iron(EasterBossEntity var0, Kind var1, int var2) {
        if (var0.getVariant() == 6) {
            CompoundTag var3 = ((BodyCueAccess)var0).ftboss$bodyRead();
            if (var3.getBoolean("parallelSpell") && var3.getLong("start") == var0.level().getGameTime()) {
                if (var1.id.equals("orbital_gears") || var1.id.equals("pressure_lance") || var1.id.equals("magnetic_storm")) {
                    mark(var0, var1.id, var2);
                }
            }
        }
    }

    public static void sky(EasterBossEntity var0, int var1) {
        if (var1 == 20) {
            mark(var0, "sky_rise", 20);
        } else if (var1 == 23) {
            mark(var0, "sky_return", 20);
        } else if (var1 == 24 && var0.getVariant() == 6) {
            BodyCueAccess var2 = (BodyCueAccess)var0;
            CompoundTag var3 = var2.ftboss$bodyRead().copy();
            String var4 = var3.getCompound("xuanlanBody").getString("id");
            long var5 = var0.level().getGameTime() - var3.getCompound("xuanlanBody").getLong("start");
            if (var4.equals("sky_return") && var5 >= 19L) {
                return;
            }

            if (var4.startsWith("sky_")) {
                var3.remove("xuanlanBody");
                var2.ftboss$bodyWrite(var3);
            }
        }
    }
}
