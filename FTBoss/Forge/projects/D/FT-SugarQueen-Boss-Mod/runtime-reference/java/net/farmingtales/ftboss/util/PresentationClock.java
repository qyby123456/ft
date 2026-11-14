/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

public final class PresentationClock {
    private static final PresentationClock SHARED = new PresentationClock();
    private long pausedAtNanos;
    private long pausedNanos;
    private boolean paused;

    public static long millis() {
        return SHARED.readNanos(System.nanoTime()) / 1000000L;
    }

    public static void setPaused(boolean paused) {
        SHARED.pauseAt(paused, System.nanoTime());
    }

    public synchronized long readNanos(long nowNanos) {
        return (this.paused ? this.pausedAtNanos : nowNanos) - this.pausedNanos;
    }

    public synchronized void pauseAt(boolean pause, long nowNanos) {
        if (pause == this.paused) {
            return;
        }
        if (pause) {
            this.pausedAtNanos = nowNanos;
        } else {
            this.pausedNanos += nowNanos - this.pausedAtNanos;
        }
        this.paused = pause;
    }
}

