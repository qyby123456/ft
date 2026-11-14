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
package net.farmingtales.ftboss.zheergennightfall;

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
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public final class NightfallSignals {
    private static final Map<EasterBossEntity, Intent> INTENTS = Collections.synchronizedMap(new WeakHashMap());
    private static boolean failureReported;

    private NightfallSignals() {
    }

    private static boolean eligible(EasterBossEntity easterBossEntity) {
        return easterBossEntity != null && easterBossEntity.getVariant() == 7 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_() && easterBossEntity.m_9236_() instanceof ServerLevel && easterBossEntity instanceof NightfallAccess;
    }

    public static boolean includes(String string, String string2) {
        return "nightfall_blades".equals(string) || "nightfall_blades".equals(string2);
    }

    public static boolean acquired(boolean bl, boolean bl2, long l, int n, long l2, int n2) {
        return bl && bl2 && l == l2 && n == n2 && n2 >= 1 && n2 <= 200;
    }

    public static String completion(boolean bl, long l, int n, long l2, long l3) {
        if (n < 1 || n > 200 || l < 0L || l3 < l || l3 - l > (long)n + 20L) {
            return "";
        }
        if (bl) {
            return "cancel";
        }
        return l2 >= l && l2 <= l3 && l3 - l2 <= 1L ? "release" : "";
    }

    private static void failed(RuntimeException runtimeException) {
        if (!failureReported) {
            failureReported = true;
            FTBossMod.LOGGER.warn("Nightfall character motion delivery failed; original gameplay continues", (Throwable)runtimeException);
        }
    }

    public static CompoundTag stamped(CompoundTag compoundTag, String string, long l, int n, double d) {
        CompoundTag compoundTag2 = compoundTag.m_6426_();
        CompoundTag compoundTag3 = new CompoundTag();
        compoundTag3.m_128359_("id", "nightfall_blades");
        compoundTag3.m_128405_("schema", 1);
        compoundTag3.m_128359_("phase", string);
        compoundTag3.m_128356_("start", l);
        compoundTag3.m_128405_("duration", n);
        compoundTag3.m_128347_("frozen", d);
        compoundTag2.m_128365_("zheergenNightfallBody", (Tag)compoundTag3);
        return compoundTag2;
    }

    public static void begin(EasterBossEntity easterBossEntity, int n, ManorSpellRules.Kind kind, ManorSpellRules.Kind kind2) {
        easterBossEntity.playParallelSpell(n);
        try {
            if (!NightfallSignals.eligible(easterBossEntity) || kind == null || !NightfallSignals.includes(kind.id, kind2 == null ? "" : kind2.id) || n < 1 || n > 200) {
                return;
            }
            NightfallAccess nightfallAccess = easterBossEntity;
            CompoundTag compoundTag = nightfallAccess.ftboss$nightfallRead();
            long l = easterBossEntity.m_9236_().m_46467_();
            if (!NightfallSignals.acquired(compoundTag.m_128471_("parallelSpell"), compoundTag.m_128471_("cast"), compoundTag.m_128454_("start"), compoundTag.m_128451_("duration"), l, n)) {
                return;
            }
            if (INTENTS.size() >= 256 && !INTENTS.containsKey(easterBossEntity)) {
                return;
            }
            INTENTS.put(easterBossEntity, new Intent(l, n, -1L));
            nightfallAccess.ftboss$nightfallWrite(NightfallSignals.stamped(compoundTag, "charge", l, n, 0.0));
        }
        catch (RuntimeException runtimeException) {
            NightfallSignals.failed(runtimeException);
        }
    }

    public static void cast(ManorSignatureSpell manorSignatureSpell, Level level, int n, LivingEntity livingEntity, CastSource castSource, MagicData magicData) {
        boolean bl = false;
        try {
            bl = level instanceof ServerLevel && livingEntity.m_6084_() && manorSignatureSpell.isEnabled() && "nightfall_blades".equals(manorSignatureSpell.kind.id);
        }
        catch (RuntimeException runtimeException) {
            NightfallSignals.failed(runtimeException);
        }
        manorSignatureSpell.onCast(level, n, livingEntity, castSource, magicData);
        try {
            EasterBossEntity easterBossEntity;
            if (!(bl && livingEntity instanceof EasterBossEntity && NightfallSignals.eligible(easterBossEntity = (EasterBossEntity)livingEntity))) {
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
            NightfallSignals.failed(runtimeException);
        }
    }

    public static void complete(EasterBossEntity easterBossEntity, boolean bl) {
        easterBossEntity.finishParallelSpell();
        try {
            Intent intent = INTENTS.remove(easterBossEntity);
            if (intent == null || !NightfallSignals.eligible(easterBossEntity)) {
                return;
            }
            NightfallAccess nightfallAccess = easterBossEntity;
            CompoundTag compoundTag = nightfallAccess.ftboss$nightfallRead();
            if (compoundTag.m_128451_("duration") > 0 || compoundTag.m_128471_("cast")) {
                return;
            }
            long l = easterBossEntity.m_9236_().m_46467_();
            String string = NightfallSignals.completion(bl, intent.started, intent.duration, intent.released, l);
            if (string.equals("cancel")) {
                double d = Math.max(0.0, Math.min(1.0, (double)(l - intent.started) / (double)intent.duration));
                nightfallAccess.ftboss$nightfallWrite(NightfallSignals.stamped(compoundTag, "cancel", l, 3, d));
            } else if (string.equals("release")) {
                nightfallAccess.ftboss$nightfallWrite(NightfallSignals.stamped(compoundTag, "release", intent.released, 12, 0.0));
            }
        }
        catch (RuntimeException runtimeException) {
            NightfallSignals.failed(runtimeException);
        }
    }

    private record Intent(long started, int duration, long released) {
    }
}

