package net.farmingtales.ftboss.util;

/** 麻雀受击走位纯规则；2026-10-15：受击后3秒择路撤离，安全时靠拢主Boss，不增加瞬移或无敌。 */
public final class SparrowHitEvasionRules {
    public static final int REACTION_TICKS = 60;
    public static final int REPATH_TICKS = 8;
    public static final double SPEED = 1.25D;
    public static final double SAFE_DISTANCE = 10.0D;
    public static final double ALLY_COMFORT = 6.0D;
    public static final double ALLY_RANGE = 48.0D;
    public static final double ATTACKER_RANGE = 64.0D;

    private SparrowHitEvasionRules() {}

    public static boolean active(long now, long last, long until) {
        return now >= last && now < until;
    }

    public static boolean settled(double nearestPlayer, boolean hasAlly, double allyDistance) {
        return Double.isFinite(nearestPlayer) && nearestPlayer >= SAFE_DISTANCE
                && (!hasAlly || Double.isFinite(allyDistance) && allyDistance <= ALLY_COMFORT);
    }

    public static double score(boolean hasAlly, double currentThreat, double currentNearest, double currentAlly,
                               double endThreat, double endNearest, double endAlly, double travel) {
        for (double value : new double[]{currentThreat, currentNearest, currentAlly, endThreat, endNearest, endAlly, travel}) {
            if (!Double.isFinite(value) || value < 0.0D) return Double.NEGATIVE_INFINITY;
        }
        if (travel < 1.0D || travel > 12.0D || hasAlly && endAlly > ALLY_RANGE) return Double.NEGATIVE_INFINITY;
        boolean closerToAlly = hasAlly && currentAlly > ALLY_COMFORT && endAlly <= currentAlly - 1.0D
                && endThreat >= 6.0D && endNearest >= 6.0D;
        if (endThreat < currentThreat + 0.5D && !closerToAlly) return Double.NEGATIVE_INFINITY;
        if (endNearest < Math.min(currentNearest + 0.5D, 6.0D)) return Double.NEGATIVE_INFINITY;
        double cover = hasAlly ? (Math.min(32.0D, currentAlly) - Math.min(32.0D, endAlly)) * 2.0D : 0.0D;
        return Math.min(12.0D, endNearest) * 4.0D + Math.min(16.0D, endThreat) + cover - travel * 0.15D;
    }
}
