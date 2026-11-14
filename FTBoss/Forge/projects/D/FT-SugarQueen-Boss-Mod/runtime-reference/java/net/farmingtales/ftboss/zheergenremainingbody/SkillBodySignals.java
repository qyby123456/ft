/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 */
package net.farmingtales.ftboss.zheergenremainingbody;

import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.zheergennightfall.NightfallAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;

public final class SkillBodySignals {
    public static final String KEY = "ftboss_zheergen_remaining_body";
    private static final WeakHashMap<EasterBossEntity, CompoundTag> ACTIVE = new WeakHashMap();

    private SkillBodySignals() {
    }

    public static boolean idValid(String string) {
        return "shadow_cross".equals(string) || "shadow_cage".equals(string) || "shadow_strike".equals(string);
    }

    private static boolean accepts(EasterBossEntity easterBossEntity) {
        return easterBossEntity.getVariant() == 7 && !easterBossEntity.isMini() && !easterBossEntity.isGiant() && easterBossEntity.m_6084_() && !easterBossEntity.m_9236_().f_46443_ && easterBossEntity instanceof NightfallAccess;
    }

    private static void write(EasterBossEntity easterBossEntity, CompoundTag compoundTag) {
        if (!(easterBossEntity instanceof NightfallAccess)) {
            return;
        }
        NightfallAccess nightfallAccess = easterBossEntity;
        CompoundTag compoundTag2 = nightfallAccess.ftboss$nightfallRead().m_6426_();
        compoundTag2.m_128365_(KEY, (Tag)compoundTag);
        nightfallAccess.ftboss$nightfallWrite(compoundTag2);
    }

    private static CompoundTag create(EasterBossEntity easterBossEntity, String string, String string2, int n) {
        CompoundTag compoundTag = new CompoundTag();
        compoundTag.m_128405_("schema", 1);
        compoundTag.m_128359_("id", string);
        compoundTag.m_128359_("phase", string2);
        compoundTag.m_128356_("start", easterBossEntity.m_9236_().m_46467_());
        compoundTag.m_128405_("duration", Math.max(1, Math.min(200, n)));
        return compoundTag;
    }

    public static void start(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, int n) {
        if (!SkillBodySignals.accepts(easterBossEntity) || skill == null || !SkillBodySignals.idValid(skill.id())) {
            return;
        }
        SkillBodySignals.begin(easterBossEntity, skill.id(), skill.windup - n);
    }

    private static void begin(EasterBossEntity easterBossEntity, String string, int n) {
        if (ACTIVE.size() > 256) {
            ACTIVE.clear();
        }
        CompoundTag compoundTag = SkillBodySignals.create(easterBossEntity, string, "charge", n);
        ACTIVE.put(easterBossEntity, compoundTag);
        SkillBodySignals.write(easterBossEntity, compoundTag);
    }

    public static void strikeStart(EasterBossEntity easterBossEntity) {
        if (SkillBodySignals.accepts(easterBossEntity)) {
            SkillBodySignals.begin(easterBossEntity, "shadow_strike", easterBossEntity.getSkillCue().m_128451_("duration"));
        }
    }

    public static void pulse(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, int n) {
        if (!SkillBodySignals.accepts(easterBossEntity) || n != 0 || skill == null) {
            return;
        }
        CompoundTag compoundTag = ACTIVE.get(easterBossEntity);
        if (compoundTag == null || !skill.id().equals(compoundTag.m_128461_("id")) || !"charge".equals(compoundTag.m_128461_("phase"))) {
            return;
        }
        SkillBodySignals.release(easterBossEntity, compoundTag, Math.min(100, (skill.pulses - 1) * skill.interval + 14));
    }

    private static void release(EasterBossEntity easterBossEntity, CompoundTag compoundTag, int n) {
        CompoundTag compoundTag2 = SkillBodySignals.create(easterBossEntity, compoundTag.m_128461_("id"), "release", n);
        ACTIVE.put(easterBossEntity, compoundTag2);
        SkillBodySignals.write(easterBossEntity, compoundTag2);
    }

    public static void strikeEnd(EasterBossEntity easterBossEntity) {
        CompoundTag compoundTag = ACTIVE.get(easterBossEntity);
        if (!(SkillBodySignals.accepts(easterBossEntity) && compoundTag != null && "shadow_strike".equals(compoundTag.m_128461_("id")) && "charge".equals(compoundTag.m_128461_("phase")))) {
            return;
        }
        if (easterBossEntity.getSkillCue().m_128471_("released")) {
            SkillBodySignals.release(easterBossEntity, compoundTag, 14);
        } else {
            SkillBodySignals.cancel(easterBossEntity);
        }
    }

    public static void finish(EasterBossEntity easterBossEntity) {
        if (easterBossEntity.m_9236_().f_46443_) {
            return;
        }
        CompoundTag compoundTag = ACTIVE.get(easterBossEntity);
        if (compoundTag == null) {
            return;
        }
        long l = easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start");
        if (l >= 0L && l < (long)compoundTag.m_128451_("duration")) {
            SkillBodySignals.write(easterBossEntity, compoundTag);
        } else {
            ACTIVE.remove(easterBossEntity);
        }
    }

    public static void captureCancel(EasterBossEntity easterBossEntity) {
        if (easterBossEntity.m_9236_().f_46443_) {
            return;
        }
        CompoundTag compoundTag = ACTIVE.get(easterBossEntity);
        if (compoundTag == null) {
            return;
        }
        String string = compoundTag.m_128461_("phase");
        if ("cancel".equals(string)) {
            return;
        }
        long l = easterBossEntity.m_9236_().m_46467_() - compoundTag.m_128454_("start");
        int n = compoundTag.m_128451_("duration");
        if (l < 0L || l >= (long)n) {
            ACTIVE.remove(easterBossEntity);
            return;
        }
        CompoundTag compoundTag2 = SkillBodySignals.create(easterBossEntity, compoundTag.m_128461_("id"), "cancel", 5);
        compoundTag2.m_128359_("track", string);
        compoundTag2.m_128347_("frozen", Math.max(0.0, Math.min(1.0, (double)l / (double)n)));
        ACTIVE.put(easterBossEntity, compoundTag2);
    }

    public static void cancel(EasterBossEntity easterBossEntity) {
        SkillBodySignals.captureCancel(easterBossEntity);
        SkillBodySignals.finish(easterBossEntity);
    }
}

