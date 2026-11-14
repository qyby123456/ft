/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.animation.qingyicast;

import net.farmingtales.ftboss.animation.qingyicast.QingyiCastAccess;
import net.farmingtales.ftboss.animation.qingyicast.QingyiCastTime;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.util.ManorSpellRules;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class QingyiCastSync {
    private QingyiCastSync() {
    }

    private static boolean valid(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 5 && easterBossEntity instanceof QingyiCastAccess && !easterBossEntity.m_9236_().f_46443_;
    }

    private static long now(EasterBossEntity easterBossEntity) {
        return easterBossEntity.m_9236_().m_46467_();
    }

    private static CompoundTag state(EasterBossEntity easterBossEntity, String string) {
        return ((QingyiCastAccess)easterBossEntity).qingyiCastState().m_128469_(string).m_6426_();
    }

    private static void save(EasterBossEntity easterBossEntity, String string, CompoundTag compoundTag) {
        QingyiCastAccess qingyiCastAccess = easterBossEntity;
        CompoundTag compoundTag2 = qingyiCastAccess.qingyiCastState().m_6426_();
        compoundTag2.m_128365_(string, (Tag)compoundTag);
        qingyiCastAccess.qingyiCastState(compoundTag2);
    }

    private static QingyiCastTime.Sample sample(CompoundTag compoundTag, long l) {
        int n = compoundTag.m_128451_("stage");
        long l2 = compoundTag.m_128454_("start");
        long l3 = compoundTag.m_128454_("stop");
        return QingyiCastTime.sample(compoundTag.m_128451_("track"), l - (n == 0 ? l2 : l3), compoundTag.m_128451_("duration"), n, l3 - l2, compoundTag.m_128471_("final"), compoundTag.m_128459_("origin"), compoundTag.m_128454_("finalAt") - l3);
    }

    private static void begin(EasterBossEntity easterBossEntity, String string, int n, int n2) {
        if (!QingyiCastSync.valid(easterBossEntity) || n < 0 || n2 < 1 || n2 > 200) {
            return;
        }
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("track", n);
        compoundTag.m_128405_("duration", n2);
        compoundTag.m_128405_("stage", 0);
        compoundTag.m_128356_("start", QingyiCastSync.now(easterBossEntity));
        compoundTag.m_128347_("origin", 0.42);
        QingyiCastSync.save(easterBossEntity, string, compoundTag);
    }

    public static void start(EasterBossEntity easterBossEntity, ManorSpellRules.Kind kind, int n) {
        if (kind == null) {
            return;
        }
        int n2 = switch (kind.id) {
            case "four_tides" -> 0;
            case "elixir_rain" -> 1;
            case "tidal_lotus" -> 2;
            default -> -1;
        };
        QingyiCastSync.begin(easterBossEntity, "iron", n2, n);
    }

    public static void startNative(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill) {
        if (skill == null || skill.variant != 5) {
            return;
        }
        int n = switch (skill.id()) {
            case "qingyi_tide" -> 3;
            case "qingyi_thunder" -> 4;
            case "qingyi_star" -> 5;
            default -> -1;
        };
        QingyiCastSync.begin(easterBossEntity, "native", n, easterBossEntity.getChallengeDifficulty().acceleratedTicks(skill.windup));
    }

    public static void startConfluence(EasterBossEntity easterBossEntity, int n) {
        QingyiCastSync.begin(easterBossEntity, "native", 6, 32 - n);
    }

    public static void startRain(EasterBossEntity easterBossEntity) {
        QingyiCastSync.begin(easterBossEntity, "native", 7, 1);
    }

    private static void pulse(EasterBossEntity easterBossEntity, String string, boolean bl) {
        if (!QingyiCastSync.valid(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = QingyiCastSync.state(easterBossEntity, string);
        if (compoundTag.m_128456_() || compoundTag.m_128451_("stage") == 2 || compoundTag.m_128471_("final")) {
            return;
        }
        long l = QingyiCastSync.now(easterBossEntity);
        double d = QingyiCastSync.sample(compoundTag, l).progress();
        compoundTag.m_128405_("stage", 1);
        compoundTag.m_128356_("stop", l);
        compoundTag.m_128347_("origin", d);
        compoundTag.m_128379_("final", bl);
        compoundTag.m_128405_("pulses", compoundTag.m_128451_("pulses") + 1);
        QingyiCastSync.save(easterBossEntity, string, compoundTag);
    }

    public static void release(EasterBossEntity easterBossEntity) {
        QingyiCastSync.pulse(easterBossEntity, "iron", true);
    }

    public static void pulseNative(EasterBossEntity easterBossEntity) {
        QingyiCastSync.pulse(easterBossEntity, "native", false);
    }

    public static void finishNative(EasterBossEntity easterBossEntity) {
        if (!QingyiCastSync.valid(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = QingyiCastSync.state(easterBossEntity, "native");
        if (compoundTag.m_128456_() || compoundTag.m_128451_("stage") != 1 || compoundTag.m_128471_("final")) {
            return;
        }
        if (compoundTag.m_128451_("track") == 7) {
            compoundTag.m_128347_("origin", QingyiCastSync.sample(compoundTag, QingyiCastSync.now(easterBossEntity)).progress());
        }
        compoundTag.m_128356_("finalAt", QingyiCastSync.now(easterBossEntity));
        compoundTag.m_128379_("final", true);
        QingyiCastSync.save(easterBossEntity, "native", compoundTag);
    }

    private static void cancel(EasterBossEntity easterBossEntity, String string) {
        if (!QingyiCastSync.valid(easterBossEntity)) {
            return;
        }
        CompoundTag compoundTag = QingyiCastSync.state(easterBossEntity, string);
        if (compoundTag.m_128456_() || compoundTag.m_128451_("stage") == 2 || compoundTag.m_128471_("final")) {
            return;
        }
        long l = QingyiCastSync.now(easterBossEntity);
        compoundTag.m_128347_("origin", QingyiCastSync.sample(compoundTag, l).progress());
        compoundTag.m_128405_("stage", 2);
        compoundTag.m_128356_("stop", l);
        QingyiCastSync.save(easterBossEntity, string, compoundTag);
    }

    public static void finish(EasterBossEntity easterBossEntity, boolean bl) {
        QingyiCastSync.cancel(easterBossEntity, "iron");
    }

    public static void cancelNative(EasterBossEntity easterBossEntity) {
        QingyiCastSync.cancel(easterBossEntity, "native");
    }

    public static void clear(EasterBossEntity easterBossEntity) {
        QingyiCastSync.cancel(easterBossEntity, "iron");
        QingyiCastSync.cancel(easterBossEntity, "native");
    }

    public static void rainPulse(EasterBossEntity easterBossEntity, int n) {
        QingyiCastSync.pulseNative(easterBossEntity);
        if (n <= 0) {
            QingyiCastSync.finishNative(easterBossEntity);
        }
    }

    public static void rainTick(EasterBossEntity easterBossEntity, int n) {
        if (!QingyiCastSync.valid(easterBossEntity) || n > 0) {
            return;
        }
        CompoundTag compoundTag = QingyiCastSync.state(easterBossEntity, "native");
        if (compoundTag.m_128456_() || compoundTag.m_128451_("track") != 7 || compoundTag.m_128471_("final")) {
            return;
        }
        QingyiCastSync.cancelNative(easterBossEntity);
    }
}

