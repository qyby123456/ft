package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class SpellVisualRules {
    public static final int MAX_ACTIVE = 96;
    public static final int MAX_QUADS = 1800;
    public static final double VIEW_RANGE = 64;
    public static final int ATLAS_COLUMNS = 16;
    public static final int ATLAS_SIZE = ATLAS_COLUMNS * 128;
    public static final int SOLID_TILE = ATLAS_COLUMNS * ATLAS_COLUMNS - 1;

    public enum Phase { CHARGE, RELEASE, PULSE, BEAM }
    public enum Form { CROWN, FLOWER, VORTEX, CORE, SPORES, HALO, WAVE, LANCE, GARDEN, BARREL,
        REPULSOR, REAP, SHIELD, GEYSER, DOLL, CANDLES, LASER, SHELL, CLEAVE, OATH, PAN,
        HEARTH, TRIDENT, RAIN, GEARS, PRESSURE, BRAND, VOID, FEATHERS, WINGS, NEEDLES, BLIZZARD,
        PROCESSION, ECLIPSE, BATTERY, VERDICT, CITRUS, LOTUS, MAGNET, NIGHTBLADES, SERAPH, CLOCK, REFUGE, STAR_CROSS, STARFALL }

    public enum Spell {
        CRYSTAL_CORONATION("crystal_coronation", Form.CROWN, 0xA0E7FF, 4, 28, false),
        CAMELLIA_BLOOD_BLOOM("camellia_blood_bloom", Form.FLOWER, 0xEF6591, 4, 28, false),
        VINTAGE_VORTEX("vintage_vortex", Form.VORTEX, 0xD99545, 4, 32, false),
        CAPACITOR_OVERLOAD("capacitor_overload", Form.CORE, 0x7EDCFF, 1.5, 18, true),
        PRISMATIC_BLIGHT("prismatic_blight", Form.SPORES, 0xB1E85D, 4, 22, false),
        GILDED_VERDICT("gilded_verdict", Form.HALO, 0xFFDC64, 1.8, 18, true),
        TIDAL_EDICT("tidal_edict", Form.WAVE, 0x50DFC9, 4, 24, true),
        FROSTGLASS_LANCE("frostglass_lance", Form.LANCE, 0xD2F9FF, 1, 16, true),
        CAMELLIA_SANCTUARY("camellia_sanctuary", Form.GARDEN, 0xFFC5D9, 4, 40, true),
        CASK_RUPTURE("cask_rupture", Form.BARREL, 0xFF934F, 5, 16, false),
        REPULSOR_DISCHARGE("repulsor_discharge", Form.REPULSOR, 0xB0A0FF, 5, 22, true),
        VIRULENT_HARVEST("virulent_harvest", Form.REAP, 0xDCFF78, 4, 28, false),
        ORCHARD_AEGIS("orchard_aegis", Form.SHIELD, 0xFFE6A8, 1.5, 80, true),
        ABYSSAL_GEYSER("abyssal_geyser", Form.GEYSER, 0x70C0FF, 2.5, 20, false),
        EFFIGY_VOLLEY("effigy_volley", Form.DOLL, 0xE6AFD5, 1.4, 16, true),
        EFFIGY_WARD("effigy_ward", Form.SHIELD, 0xF5DAB7, 2, 80, true),
        CANDLE_REAP("candle_reap", Form.REAP, 0xEAE1C9, 3, 24, true),
        CANDLE_PRISON("candle_prison", Form.CANDLES, 0xBEA1EF, 3.5, 22, false),
        ARCANE_FUSILLADE("arcane_fusillade", Form.LASER, 0x39CFC5, 1, 16, true),
        BREACH_SHELL("breach_shell", Form.SHELL, 0xF27857, 4, 20, false),
        RENDING_CLEAVE("rending_cleave", Form.CLEAVE, 0xC65570, 3, 22, true),
        CRIMSON_OATH("crimson_oath", Form.OATH, 0xF16D79, 1.5, 60, true),
        SEARING_PAN("searing_pan", Form.PAN, 0xFFC56D, 3, 24, true),
        HEARTH_FEAST("hearth_feast", Form.HEARTH, 0xF7CE8D, 4, 22, false),
        FOUR_TIDES("four_tides", Form.TRIDENT, 0x64D9DA, 4, 24, true),
        ELIXIR_RAIN("elixir_rain", Form.RAIN, 0x8DD986, 3.5, 22, false),
        ORBITAL_GEARS("orbital_gears", Form.GEARS, 0xA0DCE7, 3, 14, true),
        PRESSURE_LANCE("pressure_lance", Form.PRESSURE, 0x79BCFA, 1, 18, true),
        SHADOW_BRAND("shadow_brand", Form.BRAND, 0x9C79CE, 3.5, 22, false),
        VOID_ECHO("void_echo", Form.VOID, 0xCB94E4, 2.5, 26, false),
        LUMINOUS_REBUKE("luminous_rebuke", Form.HALO, 0xEFE4A9, 5, 26, true),
        HALO_SANCTUARY("halo_sanctuary", Form.GARDEN, 0xF1DFA0, 4, 22, false),
        FEATHER_FAN("feather_fan", Form.FEATHERS, 0xDBC6AF, 1, 18, true),
        WINGSTEP("wingstep", Form.WINGS, 0xEEE4D6, 2, 60, true),
        FROST_NEEDLES("frost_needles", Form.NEEDLES, 0xACDAFA, 1, 18, true),
        WHITEOUT_EDICT("whiteout_edict", Form.BLIZZARD, 0xD7E8FF, 3.5, 22, false),
        SUGAR_HAIL("sugar_hail", Form.BLIZZARD, 0xA9DCFF, 4, 20, false),
        GLACIAL_PRISM("glacial_prism", Form.SHIELD, 0xCEF6FF, 1.5, 60, true),
        ROYAL_SHATTER("royal_shatter", Form.CROWN, 0xAFAAFF, 4, 24, false),
        BRIAR_PROCESSION("briar_procession", Form.REAP, 0x85C77C, 2.2, 18, false),
        SCARLET_POLLEN("scarlet_pollen", Form.SPORES, 0xED92B0, 4, 20, false),
        CAMELLIA_REBIRTH("camellia_rebirth", Form.GARDEN, 0xACE5B1, 5, 20, true),
        FLESH_BLOOM("flesh_bloom", Form.FLOWER, 0xC9566E, 4, 26, false),
        SANGUINE_SUTURE("sanguine_suture", Form.BRAND, 0xEF6475, 1, 16, true),
        CINDER_BREATH("cinder_breath", Form.WAVE, 0xFFB052, 3, 16, true),
        INTOXICATING_TOAST("intoxicating_toast", Form.OATH, 0xEDC46F, 1.5, 60, true),
        VINTAGE_MINEFIELD("vintage_minefield", Form.BARREL, 0xF77844, 2.2, 18, false),
        ION_RAIL("ion_rail", Form.PRESSURE, 0x7FEBF5, 1, 18, true),
        STATIC_LATTICE("static_lattice", Form.GEARS, 0xAB91FF, 4, 20, false),
        EMERGENCY_REBOOT("emergency_reboot", Form.CORE, 0x82F2BC, 1.5, 30, true),
        SPORE_ERUPTION("spore_eruption", Form.GEYSER, 0xD3DE69, 4, 20, false),
        ROTTING_REQUIEM("rotting_requiem", Form.VORTEX, 0xBA85D0, 6, 20, false),
        LIVING_MULCH("living_mulch", Form.GARDEN, 0x96D772, 4, 30, true),
        SOLAR_ORCHARD("solar_orchard", Form.HALO, 0xFFE9A1, 3.5, 20, true),
        JUDGMENT_SEED("judgment_seed", Form.HALO, 0xFFE064, 4, 24, false),
        GOLDEN_HARVEST("golden_harvest", Form.REAP, 0xD6F093, 4, 22, false),
        UNDERTOW_SNARE("undertow_snare", Form.VORTEX, 0x67B4CE, 4, 20, false),
        ABYSSAL_CARAPACE("abyssal_carapace", Form.SHIELD, 0x80E3D9, 1.5, 60, true),
        RIPTIDE_CORRIDOR("riptide_corridor", Form.WAVE, 0x86BEFF, 3, 20, false),
        EFFIGY_PROCESSION("effigy_procession", Form.PROCESSION, 0xF4A8DF, 4, 24, false),
        CANDLE_ECLIPSE("candle_eclipse", Form.ECLIPSE, 0xC6A0FF, 3.5, 28, false),
        PRISM_BATTERY("prism_battery", Form.BATTERY, 0x65DDF4, 4, 24, false),
        BLOOD_VERDICT("blood_verdict", Form.VERDICT, 0xF06483, 3.5, 24, false),
        CITRUS_NOVA("citrus_nova", Form.CITRUS, 0xFFD15B, 4, 24, false),
        TIDAL_LOTUS("tidal_lotus", Form.LOTUS, 0x5DE3C8, 4, 60, true),
        MAGNETIC_STORM("magnetic_storm", Form.MAGNET, 0x66BBFF, 4, 24, false),
        NIGHTFALL_BLADES("nightfall_blades", Form.NIGHTBLADES, 0xB08FFF, 3.5, 24, false),
        SERAPH_DESCENT("seraph_descent", Form.SERAPH, 0xFFE3A0, 4, 24, false),
        WINTER_CLOCK("winter_clock", Form.CLOCK, 0x9EDEFF, 3.5, 24, false),
        FEATHER_REFUGE("feather_refuge", Form.REFUGE, 0xE4D4AA, 4, 60, true),
        STAR_CROSS("star_cross", Form.STAR_CROSS, 0xEC79B3, 6, 20, false),
        ROSE_STARFALL("rose_starfall", Form.STARFALL, 0xFA437C, 1.6, 20, false);

        public final String id;
        public final Form form;
        public final int color;
        public final double radius;
        public final int duration;
        public final boolean attached;

        Spell(String id, Form form, int color, double radius, int duration, boolean attached) {
            this.id = id;
            this.form = form;
            this.color = color;
            this.radius = radius;
            this.duration = duration;
            this.attached = attached;
        }

        public int lifetime(Phase phase) {
            return switch (phase) {
                case CHARGE -> 10;
                case BEAM -> 12;
                case PULSE -> this == CASK_RUPTURE || this == ABYSSAL_GEYSER || this == BREACH_SHELL ? 24 : duration;
                case RELEASE -> duration;
            };
        }

        public boolean followsCaster(Phase phase) {
            return attached && (phase == Phase.CHARGE || form == Form.SHIELD || form == Form.OATH
                    || form == Form.WINGS || form == Form.GEARS || form == Form.GARDEN || this == SOLAR_ORCHARD);
        }
    }

    private static final Map<String, Spell> SPELLS = Arrays.stream(Spell.values())
            .collect(Collectors.toUnmodifiableMap(spell -> spell.id, Function.identity()));

    private SpellVisualRules() {}

    public static Spell find(String id) { return SPELLS.get(id); }

    public static boolean hiddenForCamera(Spell spell, boolean firstPerson, boolean cameraIsCaster) {
        return false;
    }

    public static boolean modelHiddenForCamera(Spell spell, boolean firstPerson, boolean cameraIsCaster) {
        return false;
    }

    public static boolean validPoint(double coordinate) { return Double.isFinite(coordinate) && Math.abs(coordinate) <= 32_000_000; }

    public static float opacity(double age, int lifetime) {
        if (!Double.isFinite(age) || age < 0 || age >= lifetime) return 0;
        return (float) Math.min(1, Math.min((age + 1) / 3, (lifetime - age) / Math.min(8.0, lifetime / 2.0)));
    }

    /** Keep solid spell models at authored size, then dissolve over only their final few ticks. */
    public static float modelOpacity(double age, int lifetime) {
        if (!Double.isFinite(age) || age < 0 || lifetime <= 0 || age >= lifetime) return 0;
        double fadeTicks = Math.min(3.0, Math.max(1.0, lifetime / 4.0));
        double remaining = lifetime - age;
        if (remaining >= fadeTicks) return 1;
        double progress = Math.max(0, Math.min(1, remaining / fadeTicks));
        return (float) (progress * progress * (3 - 2 * progress));
    }
}
