/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.model.geom.ModelPart
 */
package net.farmingtales.ftboss.butcherskills;

import net.farmingtales.ftboss.butcherskills.ButcherSkillRules;
import net.minecraft.client.model.geom.ModelPart;

public final class ButcherSkillMotion {
    private static final ButcherSkillRules.Skill[] SKILLS = ButcherSkillRules.Skill.values();

    private ButcherSkillMotion() {
    }

    private static ButcherSkillRules.Skill skill(String string) {
        for (ButcherSkillRules.Skill skill : SKILLS) {
            if (!skill.action.equals(string)) continue;
            return skill;
        }
        return null;
    }

    public static String name(String string) {
        ButcherSkillRules.Skill skill = ButcherSkillMotion.skill(string);
        if (skill == null) {
            return string;
        }
        return skill == ButcherSkillRules.Skill.CLEAVER_SWEEP || skill == ButcherSkillRules.Skill.BLOOD_TIDE ? "backhand" : "chop";
    }

    public static float age(String string, float f) {
        ButcherSkillRules.Skill skill = ButcherSkillMotion.skill(string);
        if (skill == null) {
            return f;
        }
        if (f < (float)(skill.windup - 2)) {
            return 4.0f * (1.0f - (float)ButcherSkillRules.progress((long)(f * 1000.0f), (skill.windup - 2) * 1000));
        }
        if (f < (float)skill.windup) {
            return (f - (float)skill.windup + 2.0f) * 0.5f;
        }
        float f2 = f - (float)skill.windup;
        return Math.min(4.0f, 1.0f + 3.0f * f2 / (float)skill.recovery);
    }

    public static void finish(String string, float f, ModelPart modelPart) {
        ButcherSkillRules.Skill skill = ButcherSkillMotion.skill(string);
        if (skill == null) {
            return;
        }
        ModelPart modelPart2 = modelPart.m_171324_("head");
        ModelPart modelPart3 = modelPart.m_171324_("arm_r");
        ModelPart modelPart4 = modelPart3.m_171324_("forearm_r").m_171324_("hand_r");
        modelPart2.f_104203_ = -0.0698f;
        modelPart2.f_104204_ = 0.0f;
        if (skill == ButcherSkillRules.Skill.CLEAVER_SWEEP || skill == ButcherSkillRules.Skill.BLOOD_TIDE) {
            float f2 = ButcherSkillMotion.age(string, f);
            modelPart3.f_104204_ = (float)Math.toRadians(f2 < 1.0f ? (double)(-65.0f + 95.0f * f2) : (double)(30.0f - 38.0f * (f2 - 1.0f) / 3.0f));
            modelPart4.f_104205_ = (float)Math.toRadians(f2 < 1.0f ? (double)(-35.0f * f2) : (double)(-35.0f * (4.0f - f2) / 3.0f));
        }
    }
}

