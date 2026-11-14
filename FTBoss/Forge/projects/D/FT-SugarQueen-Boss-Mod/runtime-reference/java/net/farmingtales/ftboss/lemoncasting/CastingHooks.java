/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  net.minecraft.world.entity.LivingEntity
 */
package net.farmingtales.ftboss.lemoncasting;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.lemoncasting.CastingClock;
import net.farmingtales.ftboss.lemoncasting.CastingNetwork;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.world.entity.LivingEntity;

public final class CastingHooks {
    private static final Map<MagicData, Session> SESSIONS = new WeakHashMap<MagicData, Session>();
    private static final AtomicLong TOKENS = new AtomicLong();

    private CastingHooks() {
    }

    private static boolean eligible(ManorSpellRules.Kind kind, LivingEntity livingEntity) {
        EasterBossEntity easterBossEntity;
        return kind != null && CastingClock.supported(kind.id) && livingEntity instanceof EasterBossEntity && (easterBossEntity = (EasterBossEntity)livingEntity).getVariant() == 4 && !easterBossEntity.isMini() && !easterBossEntity.m_9236_().f_46443_;
    }

    public static void begin(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData) {
        try {
            if (!CastingHooks.eligible(kind, livingEntity) || magicData == null) {
                return;
            }
            long l = livingEntity.m_9236_().m_46467_();
            CastingClock.Cue cue = new CastingClock.Cue(kind.id, TOKENS.incrementAndGet(), l, l, Math.max(1, Math.min(2400, magicData.getCastDuration())), 0, !"offhand".equals(magicData.getCastingEquipmentSlot()));
            SESSIONS.put(magicData, new Session(livingEntity, cue));
            CastingNetwork.send(livingEntity, cue);
        }
        catch (RuntimeException runtimeException) {
            FTBossMod.LOGGER.warn("[LemonCasting] Start skipped: {}", (Object)runtimeException.toString());
        }
    }

    public static void release(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData) {
        CastingHooks.phase(kind, livingEntity, magicData, false);
    }

    public static void complete(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData, boolean bl) {
        try {
            if (bl) {
                CastingHooks.phase(kind, livingEntity, magicData, true);
            }
            SESSIONS.remove(magicData);
        }
        catch (RuntimeException runtimeException) {
            FTBossMod.LOGGER.warn("[LemonCasting] Completion skipped: {}", (Object)runtimeException.toString());
        }
    }

    private static void phase(ManorSpellRules.Kind kind, LivingEntity livingEntity, MagicData magicData, boolean bl) {
        try {
            if (!CastingHooks.eligible(kind, livingEntity)) {
                return;
            }
            Session session = SESSIONS.get(magicData);
            if (session == null || session.owner() != livingEntity || !session.cue().spell().equals(kind.id) || session.cue().phase() != 0) {
                return;
            }
            CastingClock.Cue cue = session.cue();
            CastingClock.Cue cue2 = new CastingClock.Cue(cue.spell(), cue.token(), cue.started(), livingEntity.m_9236_().m_46467_(), cue.duration(), bl ? 2 : 1, cue.primary());
            SESSIONS.put(magicData, new Session(livingEntity, cue2));
            CastingNetwork.send(livingEntity, cue2);
        }
        catch (RuntimeException runtimeException) {
            FTBossMod.LOGGER.warn("[LemonCasting] Phase skipped: {}", (Object)runtimeException.toString());
        }
    }

    private record Session(LivingEntity owner, CastingClock.Cue cue) {
    }
}

