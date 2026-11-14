package net.farmingtales.ftboss.butcherskills;

import net.farmingtales.ftboss.butcherskills.ButcherSkillRules.Skill;

public final class ButcherSkillMotion {
    private static final Skill[] SKILLS = Skill.values();
    private ButcherSkillMotion() {}

    private static Skill skill(String action) {
        for (Skill candidate : SKILLS) if (candidate.action.equals(action)) return candidate;
        return null;
    }

    public static String name(String action) {
        Skill selected = skill(action);
        if (selected == null) return action;
        return selected == Skill.CLEAVER_SWEEP || selected == Skill.BLOOD_TIDE ? "backhand" : "chop";
    }

    public static float age(String action, float elapsed) {
        Skill selected = skill(action);
        if (selected == null) return elapsed;
        if (elapsed < selected.windup - 2) return 4 * (1 - (float) ButcherSkillRules.progress((long) (elapsed * 1000), (selected.windup - 2) * 1000));
        if (elapsed < selected.windup) return (elapsed - selected.windup + 2) * 0.5F;
        float released = elapsed - selected.windup;
        return Math.min(4, 1 + 3 * released / selected.recovery);
    }

    public static void finish(String action, float elapsed, net.minecraft.client.model.geom.ModelPart body) {
        Skill selected = skill(action);
        if (selected == null) return;
        net.minecraft.client.model.geom.ModelPart head = body.getChild("head");
        net.minecraft.client.model.geom.ModelPart arm = body.getChild("arm_r");
        net.minecraft.client.model.geom.ModelPart hand = arm.getChild("forearm_r").getChild("hand_r");
        head.xRot = -0.0698F;
        head.yRot = 0;
        if (selected == Skill.CLEAVER_SWEEP || selected == Skill.BLOOD_TIDE) {
            float pose = age(action, elapsed);
            arm.yRot = (float) Math.toRadians(pose < 1 ? -65 + 95 * pose : 30 - 38 * (pose - 1) / 3);
            hand.zRot = (float) Math.toRadians(pose < 1 ? -35 * pose : -35 * (4 - pose) / 3);
        }
    }
}
