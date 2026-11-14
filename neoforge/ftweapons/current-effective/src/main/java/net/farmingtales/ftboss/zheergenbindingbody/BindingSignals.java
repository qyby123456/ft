package net.farmingtales.ftboss.zheergenbindingbody;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.farmingtales.ftboss.zheergennightfall.NightfallPhase;
import net.farmingtales.ftboss.zheergennightfall.NightfallSignals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class BindingSignals {
    private static final Map<EasterBossEntity, BindingSignals.Intent> INTENTS = Collections.synchronizedMap(new WeakHashMap<>());
    private static boolean failureReported;

    private BindingSignals() {
    }

    private static boolean eligible(EasterBossEntity var0) {
        return var0 != null
            && var0.getVariant() == 7
            && !var0.isMini()
            && !var0.isGiant()
            && var0.isAlive()
            && var0.level() instanceof ServerLevel
            && var0 instanceof NightfallAccess;
    }

    public static boolean includes(String var0, String var1) {
        return ("shadow_brand".equals(var0) || "shadow_brand".equals(var1)) && !NightfallSignals.includes(var0, var1);
    }

    public static boolean acquired(boolean var0, boolean var1, long var2, int var4, long var5, int var7) {
        return NightfallSignals.acquired(var0, var1, var2, var4, var5, var7);
    }

    public static String completion(boolean var0, long var1, int var3, long var4, long var6) {
        return NightfallSignals.completion(var0, var1, var3, var4, var6);
    }

    private static void failed(RuntimeException var0) {
        if (!failureReported) {
            failureReported = true;
            FTBossMod.LOGGER.warn("Binding character motion delivery failed; original gameplay continues", (Throwable)var0);
        }
    }

    public static CompoundTag stamped(CompoundTag var0, String var1, long var2, int var4, double var5) {
        CompoundTag var7 = var0.copy();
        CompoundTag var8 = new CompoundTag();
        var8.putString("id", "shadow_brand");
        var8.putInt("schema", 1);
        var8.putString("phase", var1);
        var8.putLong("start", var2);
        var8.putInt("duration", var4);
        var8.putDouble("frozen", var5);
        var7.put("zheergenBindingBody", var8);
        return var7;
    }

    public static boolean nightfallActive(CompoundTag var0, long var1, double var3) {
        if (Double.isFinite(var3) && !(var3 < 0.0) && !(var3 > 1.0) && var0.contains("zheergenNightfallBody", 10)) {
            CompoundTag var5 = var0.getCompound("zheergenNightfallBody");
            if ("nightfall_blades".equals(var5.getString("id")) && var5.getInt("schema") == 1) {
                long var6 = var5.getLong("start");
                return var6 >= 0L && var1 >= var6
                    ? NightfallPhase.sample(var5.getString("phase"), (double)(var1 - var6) + var3, var5.getInt("duration"), var5.getDouble("frozen")).active()
                    : false;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public static void begin(EasterBossEntity var0, int var1, Kind var2, Kind var3) {
        NightfallSignals.begin(var0, var1, var2, var3);

        try {
            INTENTS.remove(var0);
            if (!eligible(var0) || var2 == null || !includes(var2.id, var3 == null ? "" : var3.id) || var1 < 1 || var1 > 200) {
                return;
            }

            NightfallAccess var4 = (NightfallAccess)var0;
            CompoundTag var5 = var4.ftboss$nightfallRead();
            long var6 = var0.level().getGameTime();
            if (!acquired(var5.getBoolean("parallelSpell"), var5.getBoolean("cast"), var5.getLong("start"), var5.getInt("duration"), var6, var1)) {
                return;
            }

            if (INTENTS.size() >= 256) {
                return;
            }

            INTENTS.put(var0, new BindingSignals.Intent(var6, var1, -1L));
            var4.ftboss$nightfallWrite(stamped(var5, "charge", var6, var1, 0.0));
        } catch (RuntimeException var8) {
            failed(var8);
        }
    }

    public static void cast(ManorSignatureSpell var0, Level var1, int var2, LivingEntity var3, CastSource var4, MagicData var5) {
        boolean var6 = false;

        try {
            var6 = var1 instanceof ServerLevel && var3.isAlive() && var0.isEnabled() && "shadow_brand".equals(var0.kind.id);
        } catch (RuntimeException var12) {
            failed(var12);
        }

        NightfallSignals.cast(var0, var1, var2, var3, var4, var5);

        try {
            if (var6 && var3 instanceof EasterBossEntity var7 && eligible(var7)) {
                BindingSignals.Intent var8 = INTENTS.get(var7);
                long var9 = var1.getGameTime();
                if (var8 == null || var8.released >= 0L || var9 < var8.started || var9 > var8.started + (long)var8.duration + 20L) {
                    return;
                }

                INTENTS.put(var7, new BindingSignals.Intent(var8.started, var8.duration, var9));
                return;
            }
        } catch (RuntimeException var11) {
            failed(var11);
        }
    }

    public static void complete(EasterBossEntity var0, boolean var1) {
        NightfallSignals.complete(var0, var1);

        try {
            BindingSignals.Intent var2 = INTENTS.remove(var0);
            if (var2 == null || !eligible(var0)) {
                return;
            }

            NightfallAccess var3 = (NightfallAccess)var0;
            CompoundTag var4 = var3.ftboss$nightfallRead();
            if (var4.getInt("duration") > 0 || var4.getBoolean("cast")) {
                return;
            }

            long var5 = var0.level().getGameTime();
            if (nightfallActive(var4, var5, 0.0)) {
                return;
            }

            String var7 = completion(var1, var2.started, var2.duration, var2.released, var5);
            if (var7.equals("cancel")) {
                double var8 = Math.max(0.0, Math.min(1.0, (double)(var5 - var2.started) / (double)var2.duration));
                var3.ftboss$nightfallWrite(stamped(var4, "cancel", var5, 4, var8));
            } else if (var7.equals("release")) {
                var3.ftboss$nightfallWrite(stamped(var4, "release", var2.released, 16, 0.0));
            }
        } catch (RuntimeException var10) {
            failed(var10);
        }
    }

    private static record Intent(long started, int duration, long released) {
    }
}
