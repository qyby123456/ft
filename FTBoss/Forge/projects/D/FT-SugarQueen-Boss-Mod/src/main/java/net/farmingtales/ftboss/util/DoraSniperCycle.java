package net.farmingtales.ftboss.util;

import java.util.UUID;

public final class DoraSniperCycle {
    public static final int CHARGE_TICKS = 50;
    public static final int TRACK_TICKS = 35;
    public static final int RECOVERY_TICKS = 12;
    public static final double RANGE = 36.0;
    public static final double RADIUS = 1.44;

    public enum Step { IDLE, TRACKING, LOCK, LOCKED, FIRE, RECOVERY, FINISHED, CANCELLED }

    private UUID target;
    private int age;
    private int chargeTicks = CHARGE_TICKS;
    private int trackTicks = TRACK_TICKS;
    private int recoveryTicks = RECOVERY_TICKS;

    public boolean begin(UUID victim) {
        return begin(victim, false);
    }

    public boolean begin(UUID victim, boolean accelerated) {
        if (victim == null || busy()) return false;
        chargeTicks = CHARGE_TICKS;
        trackTicks = TRACK_TICKS;
        recoveryTicks = accelerated ? (RECOVERY_TICKS + 1) / 2 : RECOVERY_TICKS;
        target = victim;
        age = 0;
        return true;
    }

    public static int cueTicks(boolean fired, boolean accelerated) {
        return fired ? (accelerated ? (RECOVERY_TICKS + 1) / 2 : RECOVERY_TICKS) : CHARGE_TICKS;
    }

    public Step tick(UUID victim, boolean valid) {
        if (!busy()) return Step.IDLE;
        if (!valid || !target.equals(victim)) {
            reset();
            return Step.CANCELLED;
        }
        age++;
        if (age < trackTicks) return Step.TRACKING;
        if (age == trackTicks) return Step.LOCK;
        if (age < chargeTicks) return Step.LOCKED;
        if (age == chargeTicks) return Step.FIRE;
        if (age < chargeTicks + recoveryTicks) return Step.RECOVERY;
        reset();
        return Step.FINISHED;
    }

    public boolean busy() {
        return target != null;
    }

    public boolean charging() {
        return busy() && age < chargeTicks;
    }

    public boolean locked() {
        return busy() && age >= trackTicks;
    }

    public static boolean inRange(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared >= 0 && distanceSquared <= RANGE * RANGE;
    }

    public void reset() {
        target = null;
        age = 0;
    }
}
