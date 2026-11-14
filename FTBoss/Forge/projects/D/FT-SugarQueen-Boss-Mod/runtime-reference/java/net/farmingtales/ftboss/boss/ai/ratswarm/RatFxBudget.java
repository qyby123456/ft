/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.boss.ai.ratswarm;

public final class RatFxBudget {
    public static final int MAX_PACKETS = 96;
    public static final int MAX_PARTICLES = 160;
    private long tick = Long.MIN_VALUE;
    private int packets;
    private int particles;

    public int claim(long l, int n) {
        if (this.tick != l) {
            this.tick = l;
            this.packets = 0;
            this.particles = 0;
        }
        if (n <= 0 || this.packets >= 96 || this.particles >= 160) {
            return 0;
        }
        int n2 = Math.min(n, 160 - this.particles);
        ++this.packets;
        this.particles += n2;
        return n2;
    }

    public int packets() {
        return this.packets;
    }

    public int particles() {
        return this.particles;
    }
}

