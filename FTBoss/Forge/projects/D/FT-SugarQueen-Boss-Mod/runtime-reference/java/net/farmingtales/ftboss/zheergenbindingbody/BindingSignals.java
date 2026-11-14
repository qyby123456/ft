/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  io.redspace.ironsspellbooks.api.magic.MagicData
 *  io.redspace.ironsspellbooks.api.spells.CastSource
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.level.Level
 */
package net.farmingtales.ftboss.zheergenbindingbody;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.integ.spells.ManorSignatureSpell;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.farmingtales.ftboss.zheergennightfall.NightfallPhase;
import net.farmingtales.ftboss.zheergennightfall.NightfallSignals;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class BindingSignals {
    private static final Map<EasterBossEntity, Intent> INTENTS = Collections.synchronizedMap(new WeakHashMap());
    private static boolean failureReported;

    private BindingSignals() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 7 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_() && easterBossEntity.m_9236_() instanceof ServerLevel && easterBossEntity instanceof NightfallAccess;
    }

    public static boolean includes(String string, String string2) {
        return ("shadow_brand".equals(string) || "shadow_brand".equals(string2)) && !NightfallSignals.includes(string, string2);
    }

    public static boolean acquired(boolean bl, boolean bl2, long l, int n, long l2, int n2) {
        return NightfallSignals.acquired(bl, bl2, l, n, l2, n2);
    }

    public static String completion(boolean bl, long l, int n, long l2, long l3) {
        return NightfallSignals.completion(bl, l, n, l2, l3);
    }

    private static void failed(RuntimeException runtimeException) {
        if (!failureReported) {
            failureReported = true;
            FTBossMod.LOGGER.warn("Binding character motion delivery failed; original gameplay continues", (Throwable)runtimeException);
        }
    }

    public static CompoundTag stamped(CompoundTag compoundTag, String string, long l, int n, double d) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = new CompoundTag();
        compoundTag3.m_128359_("id", "shadow_brand");
        compoundTag3.m_128405_("schema", 1);
        compoundTag3.m_128359_("phase", string);
        compoundTag3.m_128356_("start", l);
        compoundTag3.m_128405_("duration", n);
        compoundTag3.m_128347_("frozen", d);
        compoundTag2.m_128365_("zheergenBindingBody", (Tag)compoundTag3);
        return compoundTag2;
    }

    public static boolean nightfallActive(CompoundTag compoundTag, long l, double d) {
        if (!Double.isFinite(d) || d < 0.0 || d > 1.0 || !compoundTag.m_128425_("zheergenNightfallBody", 10)) {
            return false;
        }
        CompoundTag compoundTag2 = compoundTag.m_128469_("zheergenNightfallBody");
        if (!"nightfall_blades".equals(compoundTag2.m_128461_("id")) || compoundTag2.m_128451_("schema") != 1) {
            return false;
        }
        long l2 = compoundTag2.m_128454_("start");
        if (l2 < 0L || l < l2) {
            return false;
        }
        return NightfallPhase.sample(compoundTag2.m_128461_("phase"), (double)(l - l2) + d, compoundTag2.m_128451_("duration"), compoundTag2.m_128459_("frozen")).active();
    }

    public static void begin(EasterBossEntity easterBossEntity, int n, ManorSpellRules.Kind kind, ManorSpellRules.Kind kind2) {
        NightfallSignals.begin(easterBossEntity, n, kind, kind2);
        try {
            INTENTS.remove(easterBossEntity);
            if (!BindingSignals.eligible(easterBossEntity) || kind == null || !BindingSignals.includes(kind.id, kind2 == null ? "" : kind2.id) || n < 1 || n > 200) {
                return;
            }
            NightfallAccess nightfallAccess = easterBossEntity;
            CompoundTag compoundTag = nightfallAccess.ftboss$nightfallRead();
            long l = easterBossEntity.m_9236_().m_46467_();
            if (!BindingSignals.acquired(compoundTag.m_128471_("parallelSpell"), compoundTag.m_128471_("cast"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), l, n)) {
                return;
            }
            if (INTENTS.size() >= 256) {
                return;
            }
            INTENTS.put(easterBossEntity, new Intent(l, n, -1L));
            nightfallAccess.ftboss$nightfallWrite(BindingSignals.stamped(compoundTag, "charge", l, n, 0.0));
        }
        catch (RuntimeException runtimeException) {
            BindingSignals.failed(runtimeException);
        }
    }

    public static void cast(ManorSignatureSpell manorSignatureSpell, Level level, int n, LivingEntity livingEntity, CastSource castSource, MagicData magicData) {
        boolean bl = false;
        try {
            bl = level instanceof ServerLevel && livingEntity.m_6084_() && manorSignatureSpell.isEnabled() && "shadow_brand".equals(manorSignatureSpell.kind.id);
        }
        catch (RuntimeException runtimeException) {
            BindingSignals.failed(runtimeException);
        }
        NightfallSignals.cast(manorSignatureSpell, level, n, livingEntity, castSource, magicData);
        try {
            EasterBossEntity easterBossEntity;
            if (!(bl && livingEntity instanceof EasterBossEntity && BindingSignals.eligible(easterBossEntity = (EasterBossEntity)livingEntity))) {
                return;
            }
            Intent intent = INTENTS.get(easterBossEntity);
            long l = level.m_46467_();
            if (intent == null || intent.released >= 0L || l < intent.started || l > intent.started + (long)intent.duration + 20L) {
                return;
            }
            INTENTS.put(easterBossEntity, new Intent(intent.started, intent.duration, l));
        }
        catch (RuntimeException runtimeException) {
            BindingSignals.failed(runtimeException);
        }
    }

    public static void complete(EasterBossEntity easterBossEntity, boolean bl) {
        NightfallSignals.complete(easterBossEntity, bl);
        try {
            Intent intent = INTENTS.remove(easterBossEntity);
            if (intent == null || !BindingSignals.eligible(easterBossEntity)) {
                return;
            }
            NightfallAccess nightfallAccess = easterBossEntity;
            CompoundTag compoundTag = nightfallAccess.ftboss$nightfallRead();
            if (compoundTag.m_128451_("duration") > 0 || compoundTag.m_128471_("cast")) {
                return;
            }
            long l = easterBossEntity.m_9236_().m_46467_();
            if (BindingSignals.nightfallActive(compoundTag, l, 0.0)) {
                return;
            }
            String string = BindingSignals.completion(bl, intent.started, intent.duration, intent.released, l);
            if (string.equals("cancel")) {
                double d = Math.max(0.0, Math.min(1.0, (double)(l - intent.started) / (double)intent.duration));
                nightfallAccess.ftboss$nightfallWrite(BindingSignals.stamped(compoundTag, "cancel", l, 4, d));
            } else if (string.equals("release")) {
                nightfallAccess.ftboss$nightfallWrite(BindingSignals.stamped(compoundTag, "release", intent.released, 16, 0.0));
            }
        }
        catch (RuntimeException runtimeException) {
            BindingSignals.failed(runtimeException);
        }
    }

    private record Intent(long started, int duration, long released) {
    }
}

