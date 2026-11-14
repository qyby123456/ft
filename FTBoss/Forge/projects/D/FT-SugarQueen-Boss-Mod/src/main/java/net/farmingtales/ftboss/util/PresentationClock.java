package net.farmingtales.ftboss.util;

public final class PresentationClock {
    private static final PresentationClock SHARED = new PresentationClock();
    private long pausedAtNanos;
    private long pausedNanos;
    private boolean paused;

    public static long millis() {
        return SHARED.readNanos(System.nanoTime()) / 1_000_000L;
    }

    public static void setPaused(boolean paused) {
        SHARED.pauseAt(paused, System.nanoTime());
    }

    public synchronized long readNanos(long nowNanos) {
        return (paused ? pausedAtNanos : nowNanos) - pausedNanos;
    }

    public synchronized void pauseAt(boolean pause, long nowNanos) {
        if (pause == paused) return;
        if (pause) pausedAtNanos = nowNanos;
        else pausedNanos += nowNanos - pausedAtNanos;
        paused = pause;
    }
}
