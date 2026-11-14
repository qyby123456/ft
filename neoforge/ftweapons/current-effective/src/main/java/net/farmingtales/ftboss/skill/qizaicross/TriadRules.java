package net.farmingtales.ftboss.skill.qizaicross;

public final class TriadRules {
    public static final int KIND = 5;
    public static final int COUNT = 3;
    public static final int MAX_OWNERS = 64;
    public static final int MAX_TICKS = 200;

    private TriadRules() {
    }

    public static boolean active(int var0, long var1, int var3, long var4) {
        return var0 == 5 && var4 >= 0L && var1 >= 0L && var3 > 0 && var3 <= 200 && var4 >= var1 && var4 - var1 < (long)var3;
    }

    public static double scale(double var0, double var2) {
        return Double.isFinite(var0) && var0 > 0.0 ? Math.max(0.55, Math.min(8.0, var0 / var2)) : 1.0;
    }

    public static double[] position(int var0, double var1, double var3, double var5) {
        if (var0 >= 0 && var0 < 3 && Double.isFinite(var1) && !(var1 < 0.0)) {
            double var7 = var1 * 0.022 + (double)var0 * Math.PI * 2.0 / 3.0 + (Math.PI / 3);
            double var9 = scale(var3, 0.6);
            double var11 = scale(var5, 1.8);
            return new double[]{Math.cos(var7) * 6.0 * var9, (2.8 + Math.sin(var1 * 0.06 + (double)var0) * 0.4) * var11, Math.sin(var7) * 6.0 * var9};
        } else {
            throw new IllegalArgumentException("Invalid triad role or age");
        }
    }

    public static final class Lease {
        private final long created;
        private final boolean[] attempted = new boolean[3];
        private long lastTick;
        private long lastStart;
        private boolean closed;

        public Lease(long var1, long var3) {
            if (var1 >= 0L && var3 >= 0L && var3 <= var1) {
                this.created = this.lastTick = var1;
                this.lastStart = var3;
            } else {
                throw new IllegalArgumentException("Invalid episode start");
            }
        }

        public boolean advance(int var1, long var2, int var4, long var5) {
            if (this.closed) {
                return false;
            } else if (TriadRules.active(var1, var2, var4, var5) && var5 >= this.lastTick && var2 >= this.lastStart && var5 - this.created < 200L) {
                this.lastTick = var5;
                this.lastStart = var2;
                return true;
            } else {
                this.close();
                return false;
            }
        }

        public boolean current(int var1, long var2, int var4, long var5) {
            return !this.closed && var5 >= this.lastTick && var2 >= this.lastStart && var5 - this.created < 200L && TriadRules.active(var1, var2, var4, var5);
        }

        public boolean currentDuringTick(int var1, long var2, int var4, long var5) {
            return this.current(var1, var2, var4, var5)
                || !this.closed
                    && var1 == 5
                    && var4 > 0
                    && var4 <= 200
                    && var2 >= this.lastStart
                    && var5 - this.created < 200L
                    && var5 - this.lastTick == 1L
                    && var5 >= var2
                    && var5 - var2 == (long)var4;
        }

        public boolean claim(int var1) {
            if (var1 >= 0 && var1 < 3 && !this.closed && !this.attempted[var1]) {
                this.attempted[var1] = true;
                return true;
            } else {
                return false;
            }
        }

        public void close() {
            this.closed = true;
        }
    }
}
