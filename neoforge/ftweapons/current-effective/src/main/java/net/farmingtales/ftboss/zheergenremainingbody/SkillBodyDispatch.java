package net.farmingtales.ftboss.zheergenremainingbody;

import java.lang.System.Logger.Level;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.farmingtales.ftboss.util.ManorFiveCombatRules.Skill;

public final class SkillBodyDispatch {
    private static boolean reported;

    private SkillBodyDispatch() {
    }

    private static void visual(Runnable var0) {
        try {
            var0.run();
        } catch (RuntimeException var2) {
            if (!reported) {
                reported = true;
                System.getLogger("ftboss.zheergen.body").log(Level.WARNING, "Body cue failed; original gameplay continues unchanged", var2);
            }
        }
    }

    public static void start(EasterBossEntity var0, Skill var1, int var2) {
        visual(() -> SkillBodySignals.start(var0, var1, var2));
    }

    public static void pulse(EasterBossEntity var0, Skill var1, int var2) {
        visual(() -> SkillBodySignals.pulse(var0, var1, var2));
    }

    public static void strikeStart(EasterBossEntity var0) {
        visual(() -> SkillBodySignals.strikeStart(var0));
    }

    public static void strikeEnd(EasterBossEntity var0) {
        visual(() -> SkillBodySignals.strikeEnd(var0));
    }

    public static void captureCancel(EasterBossEntity var0) {
        visual(() -> SkillBodySignals.captureCancel(var0));
    }

    public static void finish(EasterBossEntity var0) {
        visual(() -> SkillBodySignals.finish(var0));
    }
}
