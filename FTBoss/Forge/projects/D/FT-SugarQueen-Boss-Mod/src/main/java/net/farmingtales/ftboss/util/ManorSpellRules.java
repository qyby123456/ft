package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.List;

public final class ManorSpellRules {
    public static final int MAX_LEVEL = 1;
    public static final int FULL_POWER_LEVEL = 5;
    public static final int MAX_TARGETS = 12;
    public static final int MAX_FIELDS = 64;
    public static final double RANGE = 24;
    public static final String LOCK_KEY = "ftboss:manor_spell_until";

    public enum Kind {
        EFFIGY_VOLLEY(0, "effigy_volley", "evocation", 8, 65, 16, 24, 0xE6AFD5),
        EFFIGY_WARD(0, "effigy_ward", "holy", 0, 75, 26, 28, 0xF5DAB7),
        CANDLE_REAP(1, "candle_reap", "blood", 18, 70, 16, 24, 0xEAE1C9),
        CANDLE_PRISON(1, "candle_prison", "ender", 6, 90, 24, 32, 0xBEA1EF),
        ARCANE_FUSILLADE(2, "arcane_fusillade", "evocation", 7, 65, 14, 24, 0xE9AD64),
        BREACH_SHELL(2, "breach_shell", "fire", 24, 90, 24, 32, 0xF27857),
        RENDING_CLEAVE(3, "rending_cleave", "blood", 20, 70, 18, 28, 0xC65570),
        CRIMSON_OATH(3, "crimson_oath", "blood", 0, 80, 30, 28, 0xF16D79),
        SEARING_PAN(4, "searing_pan", "fire", 16, 65, 16, 24, 0xFFC56D),
        HEARTH_FEAST(4, "hearth_feast", "holy", 0, 95, 30, 32, 0xF7CE8D),
        FOUR_TIDES(5, "four_tides", "ice", 18, 75, 18, 28, 0x64D9DA),
        ELIXIR_RAIN(5, "elixir_rain", "nature", 5, 90, 24, 32, 0x8DD986),
        ORBITAL_GEARS(6, "orbital_gears", "evocation", 9, 80, 20, 28, 0xA0DCE7),
        PRESSURE_LANCE(6, "pressure_lance", "lightning", 26, 90, 22, 32, 0x79BCFA),
        SHADOW_BRAND(7, "shadow_brand", "ender", 7, 85, 22, 32, 0x9C79CE),
        VOID_ECHO(7, "void_echo", "ender", 16, 95, 26, 32, 0xCB94E4),
        LUMINOUS_REBUKE(8, "luminous_rebuke", "holy", 18, 70, 18, 24, 0xEFE4A9),
        HALO_SANCTUARY(8, "halo_sanctuary", "holy", 0, 90, 30, 32, 0xF1DFA0),
        FEATHER_FAN(9, "feather_fan", "nature", 17, 65, 8, 24, 0xDBC6AF),
        WINGSTEP(9, "wingstep", "nature", 0, 60, 24, 20, 0xEEE4D6),
        FROST_NEEDLES(10, "frost_needles", "ice", 16, 75, 18, 28, 0xACDAFA),
        WHITEOUT_EDICT(10, "whiteout_edict", "ice", 6, 100, 28, 36, 0xD7E8FF),
        EFFIGY_PROCESSION(0, "effigy_procession", "evocation", 9, 100, 28, 32, 0xF4A8DF),
        CANDLE_ECLIPSE(1, "candle_eclipse", "blood", 28, 105, 30, 36, 0xC6A0FF),
        PRISM_BATTERY(2, "prism_battery", "lightning", 12, 105, 26, 32, 0x65DDF4),
        BLOOD_VERDICT(3, "blood_verdict", "blood", 30, 110, 30, 36, 0xF06483),
        CITRUS_NOVA(4, "citrus_nova", "fire", 24, 100, 26, 32, 0xFFD15B),
        TIDAL_LOTUS(5, "tidal_lotus", "holy", 0, 110, 32, 32, 0x5DE3C8),
        MAGNETIC_STORM(6, "magnetic_storm", "lightning", 8, 110, 30, 36, 0x66BBFF),
        NIGHTFALL_BLADES(7, "nightfall_blades", "ender", 14, 100, 28, 32, 0xB08FFF),
        SERAPH_DESCENT(8, "seraph_descent", "holy", 26, 105, 28, 36, 0xFFE3A0),
        WINTER_CLOCK(10, "winter_clock", "ice", 8, 110, 30, 36, 0x9EDEFF),
        FEATHER_REFUGE(9, "feather_refuge", "nature", 0, 100, 32, 28, 0xE4D4AA),
        STAR_CROSS(11, "star_cross", "evocation", 26, 95, 24, 36, 0xEC79B3),
        ROSE_STARFALL(11, "rose_starfall", "evocation", 16, 110, 30, 40, 0xFA437C),
        FADED_VOID_SPIRAL(8, "faded_void_spiral", "holy", 10, 110, 30, 36, 0xF7D47C),
        VOID_FOUNTAIN_CASCADE(8, "void_fountain_cascade", "holy", 18, 130, 36, 40, 0xFFF0C5),
        STAR_INFERNO(11, "qizai_star_inferno", "evocation", 48, 600, 90, 300, 0xFA437C);

        public final int variant;
        public final String id;
        public final String school;
        public final int damage;
        public final int mana;
        public final int cooldown;
        public final int castTicks;
        public final int color;

        Kind(int variant, String id, String school, int damage, int mana, int cooldown, int castTicks, int color) {
            this.variant = variant;
            this.id = id;
            this.school = school;
            this.damage = damage;
            this.mana = mana;
            this.cooldown = cooldown;
            this.castTicks = ManorSpellCastRules.castTicks(id, damage, false);
            this.color = color;
        }

        public boolean support() { return damage == 0; }

        public boolean additional() {
            return ordinal() >= EFFIGY_PROCESSION.ordinal() && variant != 11
                    && this != FADED_VOID_SPIRAL && this != VOID_FOUNTAIN_CASCADE;
        }
    }

    private ManorSpellRules() {}

    public static List<Kind> pair(int variant) {
        return Arrays.stream(Kind.values()).filter(kind -> kind.variant == variant).toList();
    }

    public static int level(int value) { return FULL_POWER_LEVEL; }

    public static double extraRadius(Kind kind) {
        return switch (kind) {
            case CANDLE_ECLIPSE, BLOOD_VERDICT, NIGHTFALL_BLADES, WINTER_CLOCK -> 3.5;
            default -> 4;
        };
    }

    public static int extraDelay(Kind kind) {
        return switch (kind) {
            case BLOOD_VERDICT, SERAPH_DESCENT -> 20;
            case CANDLE_ECLIPSE -> 24;
            default -> 12;
        };
    }

    public static boolean extraPulse(Kind kind, int age) {
        if (!kind.additional() || kind.support()) return false;
        int count = switch (kind) {
            case EFFIGY_PROCESSION, PRISM_BATTERY, WINTER_CLOCK -> 3;
            case MAGNETIC_STORM -> 4;
            case NIGHTFALL_BLADES -> 2;
            default -> 1;
        };
        return pulse(age - extraDelay(kind), 20, count);
    }

    public static boolean validPoint(double distanceSquared) {
        return Double.isFinite(distanceSquared) && distanceSquared >= 0 && distanceSquared <= 32 * 32;
    }

    public static boolean cone(double forward, double sideways, double length) {
        return Double.isFinite(forward) && Double.isFinite(sideways) && forward >= 0 && forward <= length
                && Math.abs(sideways) <= 0.8 + forward * 0.7;
    }

    public static boolean pulse(int age, int interval, int count) {
        return age >= 0 && interval > 0 && count > 0 && age % interval == 0 && age / interval < count;
    }

    public static boolean eligible(boolean mini, boolean giant, boolean noAi, boolean finale) {
        return !mini && !giant && !noAi && !finale;
    }
}
