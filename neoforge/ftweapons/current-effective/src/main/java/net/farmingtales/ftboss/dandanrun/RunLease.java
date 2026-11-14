package net.farmingtales.ftboss.dandanrun;

import net.farmingtales.ftboss.dandanironmotion.IronActorRules;

public final class RunLease {
    private long cast = -1L;
    private long event = Long.MIN_VALUE;
    private long until;
    private String phase = "";
    private boolean recovering;

    public boolean receive(String var1, long var2, long var4, long var6, int var8, boolean var9, long var10) {
        if (IronActorRules.autonomous(var1) || var8 < 1 || var8 > 200) {
            return false;
        } else if (var9) {
            if (this.cast == var2 && this.phase.equals("CHARGE") && this.event <= var4) {
                this.event = var4;
                this.until = var10 + 7L;
                this.recovering = true;
                return true;
            } else {
                return false;
            }
        } else if (IronActorRules.stale(this.phase, this.cast, this.event, var1, var2, var4)) {
            return false;
        } else {
            this.phase = var1;
            this.cast = var2;
            this.event = var4;
            this.until = var6 + (long)var8;
            this.recovering = false;
            return true;
        }
    }

    public boolean busy(long var1) {
        return var1 < this.until && (this.recovering || !this.phase.equals("CHARGE") || var1 - this.event <= 8L);
    }
}
