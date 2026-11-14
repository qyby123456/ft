package net.farmingtales.ftboss.boss.ai;

public final class ButcherMotionTiming {
    private ButcherMotionTiming() {}

    public static int duration(int phase, String action, int fallback) {
        if (phase != 1) return fallback;
        return switch (action) {
            case "chop" -> 25;
            case "backhand" -> 35;
            default -> fallback;
        };
    }

    public static float impact(int phase, String action) {
        if (phase != 1) return ButcherCombatRules.MELEE_IMPACT;
        return switch (action) {
            case "chop" -> 12;
            case "backhand" -> 17;
            default -> ButcherCombatRules.MELEE_IMPACT;
        };
    }
}
