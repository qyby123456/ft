/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.dandanrun;

import net.farmingtales.ftboss.dandanironmotion.IronActorRules;

public final class RunLease {
    private long cast = -1L;
    private long event = Long.MIN_VALUE;
    private long until;
    private String phase = "";
    private boolean recovering;

    public boolean receive(String string, long l, long l2, long l3, int n, boolean bl, long l4) {
        if (IronActorRules.autonomous(string) || n < 1 || n > 200) {
            return false;
        }
        if (bl) {
            if (this.cast != l || !this.phase.equals("CHARGE") || this.event > l2) {
                return false;
            }
            this.event = l2;
            this.until = l4 + 7L;
            this.recovering = true;
            return true;
        }
        if (IronActorRules.stale(this.phase, this.cast, this.event, string, l, l2)) {
            return false;
        }
        this.phase = string;
        this.cast = l;
        this.event = l2;
        this.until = l3 + (long)n;
        this.recovering = false;
        return true;
    }

    public boolean busy(long l) {
        return l < this.until && (this.recovering || !this.phase.equals("CHARGE") || l - this.event <= 8L);
    }
}

