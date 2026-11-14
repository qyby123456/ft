package net.farmingtales.ftboss.lemoncasting;

public final class CastingClock {
    public static final int CHARGE = 0;
    public static final int RELEASE = 1;
    public static final int CANCEL = 2;

    private CastingClock() {
    }

    public static boolean supported(String var0) {
        return "searing_pan".equals(var0) || "hearth_feast".equals(var0) || "citrus_nova".equals(var0);
    }

    public static boolean valid(CastingClock.Cue var0) {
        return var0 != null
            && supported(var0.spell())
            && var0.token() > 0L
            && var0.started() >= 0L
            && var0.changed() >= var0.started()
            && var0.changed() - var0.started() <= 2420L
            && var0.duration() > 0
            && var0.duration() <= 2400
            && var0.phase() >= 0
            && var0.phase() <= 2;
    }

    public static int recovery(int var0) {
        return Math.max(3, Math.min(10, (var0 + 3) / 4));
    }

    public static double age(CastingClock.Cue var0, double var1) {
        return var1 - (double)(var0.phase() == 0 ? var0.started() : var0.changed());
    }

    public static boolean stale(CastingClock.Cue var0, double var1) {
        return !valid(var0)
            || !Double.isFinite(var1)
            || var1 < (double)(var0.started() - 20L)
            || age(var0, var1) > (double)(var0.phase() == 0 ? var0.duration() + 6 : recovery(var0.duration()));
    }

    public static boolean expiredHistory(CastingClock.Cue var0, double var1) {
        return !valid(var0) || var1 > (double)Math.max(var0.started() + (long)var0.duration() + 8L, var0.changed() + (long)recovery(var0.duration()) + 8L);
    }

    public static boolean supersedes(CastingClock.Cue var0, CastingClock.Cue var1) {
        if (!valid(var1)) {
            return false;
        } else if (var0 == null) {
            return true;
        } else if (var0.token() != var1.token()) {
            if (var1.started() != var0.started()) {
                return var1.started() > var0.started();
            } else {
                return var0.primary() != var1.primary() ? var1.primary() : var1.token() > var0.token();
            }
        } else {
            return var1.phase() > var0.phase() && var1.changed() >= var0.changed();
        }
    }

    public static double fade(double var0, int var2) {
        double var3 = Math.max(0.0, Math.min(1.0, 1.0 - var0 / (double)var2));
        return var3 * var3 * (3.0 - 2.0 * var3);
    }

    public static record Cue(String spell, long token, long started, long changed, int duration, int phase, boolean primary) {
    }
}
