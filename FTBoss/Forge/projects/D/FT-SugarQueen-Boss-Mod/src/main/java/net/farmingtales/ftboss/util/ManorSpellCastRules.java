package net.farmingtales.ftboss.util;

import java.util.Arrays;

public final class ManorSpellCastRules {
    public static final String INFERNO = "qizai_star_inferno";
    public static final int SUPPORT_TICKS = 20;
    public static final int MIN_ATTACK_TICKS = 24;
    public static final int TICKS_PER_RANK = 4;
    private static final int[] DAMAGE_STEPS = Arrays.stream(new int[]{
            48, 52, 56, 64, 68, 72, 76, 80, 88, 90, 96, 102, 104, 112, 120, 128,
            136, 140, 144, 240, zicaiHits(true) * 52, zicaiHits(false) * 36, 750, infernoHits() * 112
    }).distinct().sorted().toArray();

    private ManorSpellCastRules() {}

    public static int hits(String id) {
        return switch (id) {
            case "effigy_volley", "effigy_procession", "prism_battery", "winter_clock", "shadow_brand",
                    "qizai_star_volley" -> 3;
            case "arcane_fusillade", "elixir_rain", "orbital_gears", "whiteout_edict", "magnetic_storm" -> 4;
            case "candle_prison", "rose_starfall" -> 5;
            case "void_echo", "nightfall_blades" -> 2;
            case "feather_fan" -> MaqueRicochetRules.FEATHERS * MaqueRicochetRules.HITS;
            case "faded_void_spiral" -> zicaiHits(false);
            case "void_fountain_cascade" -> zicaiHits(true);
            case INFERNO -> infernoHits();
            default -> 1;
        };
    }

    public static int infernoHits() {
        int beams = (QizaiSweepRules.SWEEP_TICKS + QizaiSweepRules.BEAM_HIT_COOLDOWN - 1)
                / QizaiSweepRules.BEAM_HIT_COOLDOWN;
        int volleys = (QizaiSweepRules.SWEEP_TICKS + QizaiSweepRules.VOLLEY_INTERVAL - 1)
                / QizaiSweepRules.VOLLEY_INTERVAL;
        return QizaiRules.RELEASES * (beams + volleys * QizaiSweepRules.VOLLEY_STARS
                + QizaiSweepRules.RAIN_WAVES * QizaiSweepRules.RAIN_COUNT);
    }

    public static int zicaiHits(boolean fountain) {
        int[] strikes = ZicaiVoidRules.layout(fountain, 0, 8).stream()
                .flatMapToInt(mark -> fountain ? java.util.stream.IntStream.of(mark.strike(), mark.strike() + 10, mark.strike() + 20)
                        : java.util.stream.IntStream.of(mark.strike())).distinct().sorted().toArray();
        int previous = -100;
        int hits = 0;
        for (int strike : strikes) {
            if (strike - previous < ZicaiVoidRules.HIT_INTERVAL) continue;
            previous = strike;
            hits++;
        }
        return hits;
    }

    public static int totalDamage(String id, int damage, boolean qizai) {
        return damage <= 0 ? 0 : 2 * (damage + (qizai ? 0 : 8)) * hits(id);
    }

    public static int castTicks(String id, int damage, boolean qizai) {
        if (INFERNO.equals(id) || QizaiBarrageRules.ID.equals(id)) return 240;
        int total = totalDamage(id, damage, qizai);
        if (total == 0) return SUPPORT_TICKS;
        int rank = 0;
        for (int step : DAMAGE_STEPS) if (step < total) rank++;
        return MIN_ATTACK_TICKS + rank * TICKS_PER_RANK;
    }
}
