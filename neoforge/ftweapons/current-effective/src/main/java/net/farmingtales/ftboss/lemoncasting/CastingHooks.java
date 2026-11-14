package net.farmingtales.ftboss.lemoncasting;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.lemoncasting.CastingClock.Cue;
import net.farmingtales.ftboss.util.ManorSpellRules.Kind;
import net.minecraft.world.entity.LivingEntity;

public final class CastingHooks {
    private static final Map<MagicData, CastingHooks.Session> SESSIONS = new WeakHashMap<>();
    private static final AtomicLong TOKENS = new AtomicLong();

    private CastingHooks() {
    }

    private static boolean eligible(Kind var0, LivingEntity var1) {
        if (var0 != null
            && CastingClock.supported(var0.id)
            && var1 instanceof EasterBossEntity var2
            && var2.getVariant() == 4
            && !var2.isMini()
            && !var2.level().isClientSide) {
            return true;
        }

        return false;
    }

    public static void begin(Kind var0, LivingEntity var1, MagicData var2) {
        try {
            if (!eligible(var0, var1) || var2 == null) {
                return;
            }

            long var3 = var1.level().getGameTime();
            Cue var5 = new Cue(
                var0.id,
                TOKENS.incrementAndGet(),
                var3,
                var3,
                Math.max(1, Math.min(2400, var2.getCastDuration())),
                0,
                !"offhand".equals(var2.getCastingEquipmentSlot())
            );
            SESSIONS.put(var2, new CastingHooks.Session(var1, var5));
            CastingNetwork.send(var1, var5);
        } catch (RuntimeException var6) {
            FTBossMod.LOGGER.warn("[LemonCasting] Start skipped: {}", var6.toString());
        }
    }

    public static void release(Kind var0, LivingEntity var1, MagicData var2) {
        phase(var0, var1, var2, false);
    }

    public static void complete(Kind var0, LivingEntity var1, MagicData var2, boolean var3) {
        try {
            if (var3) {
                phase(var0, var1, var2, true);
            }

            SESSIONS.remove(var2);
        } catch (RuntimeException var5) {
            FTBossMod.LOGGER.warn("[LemonCasting] Completion skipped: {}", var5.toString());
        }
    }

    private static void phase(Kind var0, LivingEntity var1, MagicData var2, boolean var3) {
        try {
            if (!eligible(var0, var1)) {
                return;
            }

            CastingHooks.Session var4 = SESSIONS.get(var2);
            if (var4 == null || var4.owner() != var1 || !var4.cue().spell().equals(var0.id) || var4.cue().phase() != 0) {
                return;
            }

            Cue var5 = var4.cue();
            Cue var6 = new Cue(var5.spell(), var5.token(), var5.started(), var1.level().getGameTime(), var5.duration(), var3 ? 2 : 1, var5.primary());
            SESSIONS.put(var2, new CastingHooks.Session(var1, var6));
            CastingNetwork.send(var1, var6);
        } catch (RuntimeException var7) {
            FTBossMod.LOGGER.warn("[LemonCasting] Phase skipped: {}", var7.toString());
        }
    }

    private static record Session(LivingEntity owner, Cue cue) {
    }
}
