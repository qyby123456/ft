package net.farmingtales.ftboss.dandanmotion;

import java.util.Set;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.minecraft.nbt.CompoundTag;

public final class MotionServer {
    private static final String[] KEYS = new String[]{
        "dandan_motion_clip", "dandan_motion_start", "dandan_motion_ticks", "dandan_motion_reverse", "dandan_motion_schema"
    };

    private MotionServer() {
    }

    private static boolean eligible(EasterBossEntity var0) {
        return var0.getVariant() == 1 && !var0.isMini() && var0.isAlive() && !var0.level().isClientSide && (Object)var0 instanceof MotionAccess;
    }

    public static void play(EasterBossEntity var0, String var1, int var2, boolean var3) {
        if (eligible(var0)) {
            MotionAccess var4 = (MotionAccess)(Object)var0;
            CompoundTag var5 = var4.dandanMotionData().copy();
            var5.putString("dandan_motion_clip", var1);
            var5.putLong("dandan_motion_start", var0.level().getGameTime());
            var5.putInt("dandan_motion_ticks", Math.max(1, Math.min(200, var2)));
            var5.putBoolean("dandan_motion_reverse", var3);
            var5.putInt("dandan_motion_schema", 1);
            var4.dandanMotionData(var5);
        }
    }

    public static void start(EasterBossEntity var0, Skill var1, int var2) {
        if (var1 == Skill.DANDAN_REAP) {
            play(var0, "reap_charge", var1.windup - var2, false);
        }

        if (var1 == Skill.DANDAN_STAR) {
            play(var0, "star_charge", var1.windup - var2, false);
        }
    }

    public static void pulse(EasterBossEntity var0, Skill var1) {
        if (var1 == Skill.DANDAN_REAP) {
            play(var0, "reap_cut", var1.interval, false);
        }

        if (var1 == Skill.DANDAN_STAR) {
            play(var0, "star_release", var1.interval, false);
        }
    }

    public static void finish(EasterBossEntity var0) {
        if (eligible(var0)) {
            String var1 = ((MotionAccess)(Object)var0).dandanMotionData().getString("dandan_motion_clip");
            if (var1.equals("reap_cut")) {
                play(var0, "settle", 7, false);
            }

            if (var1.equals("star_release")) {
                play(var0, "star_charge", 7, true);
            }
        }
    }

    public static void carry(EasterBossEntity var0, CompoundTag var1) {
        if (eligible(var0)) {
            CompoundTag var2 = ((MotionAccess)(Object)var0).dandanMotionData();
            String var3 = var2.getString("dandan_motion_clip");
            if (var1.getBoolean("cast") || !Set.of("vortex_open", "swap_fold", "rush_cut").contains(var3)) {
                long var4 = var0.level().getGameTime() - var2.getLong("dandan_motion_start");
                if (var2.getInt("dandan_motion_schema") == 1 && var4 >= 0L && var4 < (long)var2.getInt("dandan_motion_ticks")) {
                    for (String var9 : KEYS) {
                        if (var2.contains(var9)) {
                            var1.put(var9, var2.get(var9).copy());
                        }
                    }
                }
            }
        }
    }

    public static void clear(EasterBossEntity var0) {
        if (eligible(var0)) {
            MotionAccess var1 = (MotionAccess)(Object)var0;
            CompoundTag var2 = var1.dandanMotionData().copy();

            for (String var6 : KEYS) {
                var2.remove(var6);
            }

            var1.dandanMotionData(var2);
        }
    }

    public static void swap(EasterBossEntity var0) {
        play(var0, "swap_fold", 9, false);
    }

    public static void rush(EasterBossEntity var0) {
        play(var0, "rush_cut", 5, false);
    }

    public static void vortex(EasterBossEntity var0) {
        play(var0, "vortex_open", 16, false);
    }
}
