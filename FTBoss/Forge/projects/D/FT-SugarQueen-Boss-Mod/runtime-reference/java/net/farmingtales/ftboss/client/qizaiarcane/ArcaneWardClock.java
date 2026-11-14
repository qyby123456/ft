/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiarcane;

public final class ArcaneWardClock {
    private long sequence = Long.MIN_VALUE;
    private double received;
    private double activated;
    private boolean protectedState;

    public static boolean eligible(int n, boolean bl, boolean bl2, boolean bl3, boolean bl4) {
        return n == 11 && bl && !bl2 && (bl3 || bl4);
    }

    public boolean update(long l, double d, boolean bl) {
        if (!Double.isFinite(d) || l < this.sequence) {
            return false;
        }
        if (bl && !this.protectedState) {
            this.activated = d;
        }
        this.sequence = l;
        this.received = d;
        this.protectedState = bl;
        return true;
    }

    public boolean active(double d) {
        return this.protectedState && d >= this.received && d - this.received < 25.0;
    }

    public boolean expired(double d) {
        return d < this.received || d - this.received >= 25.0;
    }

    public float opacity(double d) {
        return this.active(d) ? (float)Math.min(1.0, Math.max(0.0, (d - this.activated) / 2.0)) : 0.0f;
    }
}

