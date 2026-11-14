package net.farmingtales.ftboss.zheergenremainingbody;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.minecraft.nbt.CompoundTag;

public final class SkillBodySignals {
    public static final String KEY = "ftboss_zheergen_remaining_body";
    private static final WeakHashMap<EasterBossEntity, CompoundTag> ACTIVE = new WeakHashMap<>();

    private SkillBodySignals() {
    }

    public static boolean idValid(String var0) {
        return "shadow_cross".equals(var0) || "shadow_cage".equals(var0) || "shadow_strike".equals(var0);
    }

    private static boolean accepts(EasterBossEntity var0) {
        return var0.getVariant() == 7 && !var0.isMini() && !var0.isGiant() && var0.isAlive() && !var0.level().isClientSide && (Object)var0 instanceof NightfallAccess;
    }

    private static void write(EasterBossEntity var0, CompoundTag var1) {
        if ((Object)var0 instanceof NightfallAccess var2) {
            CompoundTag var3 = var2.ftboss$nightfallRead().copy();
            var3.put("ftboss_zheergen_remaining_body", var1);
            var2.ftboss$nightfallWrite(var3);
        }
    }

    private static CompoundTag create(EasterBossEntity var0, String var1, String var2, int var3) {
        CompoundTag var4 = new CompoundTag();
        var4.putInt("schema", 1);
        var4.putString("id", var1);
        var4.putString("phase", var2);
        var4.putLong("start", var0.level().getGameTime());
        var4.putInt("duration", Math.max(1, Math.min(200, var3)));
        return var4;
    }

    public static void start(EasterBossEntity var0, Skill var1, int var2) {
        if (accepts(var0) && var1 != null && idValid(var1.id())) {
            begin(var0, var1.id(), var1.windup - var2);
        }
    }

    private static void begin(EasterBossEntity var0, String var1, int var2) {
        if (ACTIVE.size() > 256) {
            ACTIVE.clear();
        }

        CompoundTag var3 = create(var0, var1, "charge", var2);
        ACTIVE.put(var0, var3);
        write(var0, var3);
    }

    public static void strikeStart(EasterBossEntity var0) {
        if (accepts(var0)) {
            begin(var0, "shadow_strike", var0.getSkillCue().getInt("duration"));
        }
    }

    public static void pulse(EasterBossEntity var0, Skill var1, int var2) {
        if (accepts(var0) && var2 == 0 && var1 != null) {
            CompoundTag var3 = ACTIVE.get(var0);
            if (var3 != null && var1.id().equals(var3.getString("id")) && "charge".equals(var3.getString("phase"))) {
                release(var0, var3, Math.min(100, (var1.pulses - 1) * var1.interval + 14));
            }
        }
    }

    private static void release(EasterBossEntity var0, CompoundTag var1, int var2) {
        CompoundTag var3 = create(var0, var1.getString("id"), "release", var2);
        ACTIVE.put(var0, var3);
        write(var0, var3);
    }

    public static void strikeEnd(EasterBossEntity var0) {
        CompoundTag var1 = ACTIVE.get(var0);
        if (accepts(var0) && var1 != null && "shadow_strike".equals(var1.getString("id")) && "charge".equals(var1.getString("phase"))) {
            if (var0.getSkillCue().getBoolean("released")) {
                release(var0, var1, 14);
            } else {
                cancel(var0);
            }
        }
    }

    public static void finish(EasterBossEntity var0) {
        if (!var0.level().isClientSide) {
            CompoundTag var1 = ACTIVE.get(var0);
            if (var1 != null) {
                long var2 = var0.level().getGameTime() - var1.getLong("start");
                if (var2 >= 0L && var2 < (long)var1.getInt("duration")) {
                    write(var0, var1);
                } else {
                    ACTIVE.remove(var0);
                }
            }
        }
    }

    public static void captureCancel(EasterBossEntity var0) {
        if (!var0.level().isClientSide) {
            CompoundTag var1 = ACTIVE.get(var0);
            if (var1 != null) {
                String var2 = var1.getString("phase");
                if (!"cancel".equals(var2)) {
                    long var3 = var0.level().getGameTime() - var1.getLong("start");
                    int var5 = var1.getInt("duration");
                    if (var3 >= 0L && var3 < (long)var5) {
                        CompoundTag var6 = create(var0, var1.getString("id"), "cancel", 5);
                        var6.putString("track", var2);
                        var6.putDouble("frozen", Math.max(0.0, Math.min(1.0, (double)var3 / (double)var5)));
                        ACTIVE.put(var0, var6);
                    } else {
                        ACTIVE.remove(var0);
                    }
                }
            }
        }
    }

    public static void cancel(EasterBossEntity var0) {
        captureCancel(var0);
        finish(var0);
    }
}
