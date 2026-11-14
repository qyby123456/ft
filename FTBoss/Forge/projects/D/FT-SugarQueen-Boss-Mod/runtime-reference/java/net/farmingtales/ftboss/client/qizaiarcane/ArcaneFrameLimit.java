/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.qizaiarcane;

public final class ArcaneFrameLimit {
    private int body;
    private int status;

    public void reset(int n) {
        int n2 = n == 0 ? 2200 : (this.body = n == 1 ? 4800 : 9000);
        this.status = n == 0 ? 900 : (n == 1 ? 1800 : 3200);
    }

    public int available(boolean bl) {
        return bl ? this.status : this.body;
    }

    public int consume(int n, boolean bl) {
        int n2 = Math.min(Math.max(0, n), this.available(bl));
        if (bl) {
            this.status -= n2;
        } else {
            this.body -= n2;
        }
        return n2;
    }
}

