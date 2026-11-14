/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai;

public final class GongdiGuillotineCooldown {
    public static final int COOLDOWN_TICKS = 600;
    public static final int RESET_WINDOW_TICKS = 200;
    private long resetExpiresAt;
    private boolean resetAvailable;

    public static long eligibleAt(long l, GongdiGuillotineCooldown gongdiGuillotineCooldown, long l2) {
        return l2;
    }

    public static long triggered(long l, GongdiGuillotineCooldown gongdiGuillotineCooldown) {
        gongdiGuillotineCooldown.clear();
        return 0L;
    }

    public void grant(long l) {
        this.resetExpiresAt = l + 200L;
        this.resetAvailable = true;
    }

    public void clear() {
        this.resetAvailable = false;
        this.resetExpiresAt = 0L;
    }
}

