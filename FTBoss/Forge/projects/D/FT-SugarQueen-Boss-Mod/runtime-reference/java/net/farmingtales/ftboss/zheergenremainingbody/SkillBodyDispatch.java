/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.zheergenremainingbody;

import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules;
import net.farmingtales.ftboss.zheergenremainingbody.SkillBodySignals;

public final class SkillBodyDispatch {
    private static boolean reported;

    private SkillBodyDispatch() {
    }

    private static void visual(Runnable runnable) {
        block2: {
            try {
                runnable.run();
            }
            catch (RuntimeException runtimeException) {
                if (reported) break block2;
                reported = true;
                System.getLogger("ftboss.zheergen.body").log(System.Logger.Level.WARNING, "Body cue failed; original gameplay continues unchanged", (Throwable)runtimeException);
            }
        }
    }

    public static void start(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, int n) {
        SkillBodyDispatch.visual(() -> SkillBodySignals.start(easterBossEntity, skill, n));
    }

    public static void pulse(EasterBossEntity easterBossEntity, ManorFiveCombatRules.Skill skill, int n) {
        SkillBodyDispatch.visual(() -> SkillBodySignals.pulse(easterBossEntity, skill, n));
    }

    public static void strikeStart(EasterBossEntity easterBossEntity) {
        SkillBodyDispatch.visual(() -> SkillBodySignals.strikeStart(easterBossEntity));
    }

    public static void strikeEnd(EasterBossEntity easterBossEntity) {
        SkillBodyDispatch.visual(() -> SkillBodySignals.strikeEnd(easterBossEntity));
    }

    public static void captureCancel(EasterBossEntity easterBossEntity) {
        SkillBodyDispatch.visual(() -> SkillBodySignals.captureCancel(easterBossEntity));
    }

    public static void finish(EasterBossEntity easterBossEntity) {
        SkillBodyDispatch.visual(() -> SkillBodySignals.finish(easterBossEntity));
    }
}

