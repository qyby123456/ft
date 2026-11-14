/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class SparrowHitEvasionRules {
    public static final int REACTION_TICKS = 60;
    public static final int REPATH_TICKS = 8;
    public static final double SPEED = 1.25;
    public static final double SAFE_DISTANCE = 10.0;
    public static final double ALLY_COMFORT = 6.0;
    public static final double ALLY_RANGE = 48.0;
    public static final double ATTACKER_RANGE = 64.0;

    private SparrowHitEvasionRules() {
    }

    public static boolean active(long now, long last, long until) {
        return now >= last && now < until;
    }

    public static boolean settled(double nearestPlayer, boolean hasAlly, double allyDistance) {
        return Double.isFinite(nearestPlayer) && nearestPlayer >= 10.0 && (!hasAlly || Double.isFinite(allyDistance) && allyDistance <= 6.0);
    }

    public static double score(boolean hasAlly, double currentThreat, double currentNearest, double currentAlly, double endThreat, double endNearest, double endAlly, double travel) {
        boolean closerToAlly;
        for (double value : new double[]{currentThreat, currentNearest, currentAlly, endThreat, endNearest, endAlly, travel}) {
            if (Double.isFinite(value) && !(value < 0.0)) continue;
            return Double.NEGATIVE_INFINITY;
        }
        if (travel < 1.0 || travel > 12.0 || hasAlly && endAlly > 48.0) {
            return Double.NEGATIVE_INFINITY;
        }
        boolean bl = closerToAlly = hasAlly && currentAlly > 6.0 && endAlly <= currentAlly - 1.0 && endThreat >= 6.0 && endNearest >= 6.0;
        if (endThreat < currentThreat + 0.5 && !closerToAlly) {
            return Double.NEGATIVE_INFINITY;
        }
        if (endNearest < Math.min(currentNearest + 0.5, 6.0)) {
            return Double.NEGATIVE_INFINITY;
        }
        double cover = hasAlly ? (Math.min(32.0, currentAlly) - Math.min(32.0, endAlly)) * 2.0 : 0.0;
        return Math.min(12.0, endNearest) * 4.0 + Math.min(16.0, endThreat) + cover - travel * 0.15;
    }
}

