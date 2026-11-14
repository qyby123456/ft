/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.lemoncasting;

public final class CastingClock {
    public static final int CHARGE = 0;
    public static final int RELEASE = 1;
    public static final int CANCEL = 2;

    private CastingClock() {
    }

    public static boolean supported(String string) {
        return "searing_pan".equals(string) || "hearth_feast".equals(string) || "citrus_nova".equals(string);
    }

    public static boolean valid(Cue cue) {
        return cue != null && CastingClock.supported(cue.spell()) && cue.token() > 0L && cue.started() >= 0L && cue.changed() >= cue.started() && cue.changed() - cue.started() <= 2420L && cue.duration() > 0 && cue.duration() <= 2400 && cue.phase() >= 0 && cue.phase() <= 2;
    }

    public static int recovery(int n) {
        return Math.max(3, Math.min(10, (n + 3) / 4));
    }

    public static double age(Cue cue, double d) {
        return d - (double)(cue.phase() == 0 ? cue.started() : cue.changed());
    }

    /*
     * Enabled force condition propagation
     * Lifted jumps to return sites
     */
    public static boolean stale(Cue cue, double d) {
        if (!CastingClock.valid(cue)) return true;
        if (!Double.isFinite(d)) return true;
        if (d < (double)(cue.started() - 20L)) return true;
        double d2 = CastingClock.age(cue, d);
        int n = cue.phase() == 0 ? cue.duration() + 6 : CastingClock.recovery(cue.duration());
        if (!(d2 > (double)n)) return false;
        return true;
    }

    public static boolean expiredHistory(Cue cue, double d) {
        return !CastingClock.valid(cue) || d > (double)Math.max(cue.started() + (long)cue.duration() + 8L, cue.changed() + (long)CastingClock.recovery(cue.duration()) + 8L);
    }

    public static boolean supersedes(Cue cue, Cue cue2) {
        if (!CastingClock.valid(cue2)) {
            return false;
        }
        if (cue == null) {
            return true;
        }
        if (cue.token() == cue2.token()) {
            return cue2.phase() > cue.phase() && cue2.changed() >= cue.changed();
        }
        if (cue2.started() != cue.started()) {
            return cue2.started() > cue.started();
        }
        if (cue.primary() != cue2.primary()) {
            return cue2.primary();
        }
        return cue2.token() > cue.token();
    }

    public static double fade(double d, int n) {
        double d2 = Math.max(0.0, Math.min(1.0, 1.0 - d / (double)n));
        return d2 * d2 * (3.0 - 2.0 * d2);
    }

    public record Cue(String spell, long token, long started, long changed, int duration, int phase, boolean primary) {
    }
}

