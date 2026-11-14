/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.skill.qizaicross;

public final class TriadRules {
    public static final int KIND = 5;
    public static final int COUNT = 3;
    public static final int MAX_OWNERS = 64;
    public static final int MAX_TICKS = 200;

    private TriadRules() {
    }

    public static boolean active(int n, long l, int n2, long l2) {
        return n == 5 && l2 >= 0L && l >= 0L && n2 > 0 && n2 <= 200 && l2 >= l && l2 - l < (long)n2;
    }

    public static double scale(double d, double d2) {
        return Double.isFinite(d) && d > 0.0 ? Math.max(0.55, Math.min(8.0, d / d2)) : 1.0;
    }

    public static double[] position(int n, double d, double d2, double d3) {
        if (n < 0 || n >= 3 || !Double.isFinite(d) || d < 0.0) {
            throw new IllegalArgumentException("Invalid triad role or age");
        }
        double d4 = d * 0.022 + (double)n * Math.PI * 2.0 / 3.0 + 1.0471975511965976;
        double d5 = TriadRules.scale(d2, 0.6);
        double d6 = TriadRules.scale(d3, 1.8);
        return new double[]{Math.cos(d4) * 6.0 * d5, (2.8 + Math.sin(d * 0.06 + (double)n) * 0.4) * d6, Math.sin(d4) * 6.0 * d5};
    }

    public static final class Lease {
        private final long created;
        private final boolean[] attempted = new boolean[3];
        private long lastTick;
        private long lastStart;
        private boolean closed;

        public Lease(long l, long l2) {
            if (l < 0L || l2 < 0L || l2 > l) {
                throw new IllegalArgumentException("Invalid episode start");
            }
            this.created = this.lastTick = l;
            this.lastStart = l2;
        }

        public boolean advance(int n, long l, int n2, long l2) {
            if (this.closed) {
                return false;
            }
            if (!TriadRules.active(n, l, n2, l2) || l2 < this.lastTick || l < this.lastStart || l2 - this.created >= 200L) {
                this.close();
                return false;
            }
            this.lastTick = l2;
            this.lastStart = l;
            return true;
        }

        public boolean current(int n, long l, int n2, long l2) {
            return !this.closed && l2 >= this.lastTick && l >= this.lastStart && l2 - this.created < 200L && TriadRules.active(n, l, n2, l2);
        }

        public boolean currentDuringTick(int n, long l, int n2, long l2) {
            return this.current(n, l, n2, l2) || !this.closed && n == 5 && n2 > 0 && n2 <= 200 && l >= this.lastStart && l2 - this.created < 200L && l2 - this.lastTick == 1L && l2 >= l && l2 - l == (long)n2;
        }

        public boolean claim(int n) {
            if (n < 0 || n >= 3 || this.closed || this.attempted[n]) {
                return false;
            }
            this.attempted[n] = true;
            return true;
        }

        public void close() {
            this.closed = true;
        }
    }
}

