/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Arrays;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import net.farmingtales.ftboss.zheergenphantom.ArcaneReadability;

public final class SpellVisualRules {
    public static final int MAX_ACTIVE = 96;
    public static final int MAX_QUADS = 1800;
    public static final double VIEW_RANGE = 64.0;
    public static final int ATLAS_COLUMNS = 16;
    public static final int ATLAS_SIZE = 2048;
    public static final int SOLID_TILE = 255;
    private static final Map<String, Spell> SPELLS = Arrays.stream(Spell.values()).collect(Collectors.toUnmodifiableMap(spell -> spell.id, Function.identity()));

    private SpellVisualRules() {
    }

    public static Spell find(String id) {
        return SPELLS.get(id);
    }

    public static boolean hiddenForCamera(Spell spell, boolean bl, boolean bl2) {
        return false;
    }

    public static boolean modelHiddenForCamera(Spell spell, boolean bl, boolean bl2) {
        return false;
    }

    public static boolean validPoint(double coordinate) {
        return Double.isFinite(coordinate) && Math.abs(coordinate) <= 3.2E7;
    }

    public static float opacity(double age, int lifetime) {
        if (!Double.isFinite(age) || age < 0.0 || age >= (double)lifetime) {
            return 0.0f;
        }
        return (float)Math.min(1.0, Math.min((age + 1.0) / 3.0, ((double)lifetime - age) / Math.min(8.0, (double)lifetime / 2.0)));
    }

    public static float modelOpacity(double age, int lifetime) {
        if (!Double.isFinite(age) || age < 0.0 || lifetime <= 0 || age >= (double)lifetime) {
            return 0.0f;
        }
        double remaining = (double)lifetime - age;
        double fadeTicks = Math.min(3.0, Math.max(1.0, (double)lifetime / 4.0));
        if (remaining >= fadeTicks) {
            return 1.0f;
        }
        double progress = Math.max(0.0, Math.min(1.0, remaining / fadeTicks));
        return (float)(progress * progress * (3.0 - 2.0 * progress));
    }

    public static enum Spell {
        CRYSTAL_CORONATION("crystal_coronation", Form.CROWN, 10545151, 4.0, 28, false),
        CAMELLIA_BLOOD_BLOOM("camellia_blood_bloom", Form.FLOWER, 15689105, 4.0, 28, false),
        VINTAGE_VORTEX("vintage_vortex", Form.VORTEX, 14259525, 4.0, 32, false),
        CAPACITOR_OVERLOAD("capacitor_overload", Form.CORE, 8314111, 1.5, 18, true),
        PRISMATIC_BLIGHT("prismatic_blight", Form.SPORES, 11659357, 4.0, 22, false),
        GILDED_VERDICT("gilded_verdict", Form.HALO, 16768100, 1.8, 18, true),
        TIDAL_EDICT("tidal_edict", Form.WAVE, 5300169, 4.0, 24, true),
        FROSTGLASS_LANCE("frostglass_lance", Form.LANCE, 13826559, 1.0, 16, true),
        CAMELLIA_SANCTUARY("camellia_sanctuary", Form.GARDEN, 16762329, 4.0, 40, true),
        CASK_RUPTURE("cask_rupture", Form.BARREL, 16749391, 5.0, 16, false),
        REPULSOR_DISCHARGE("repulsor_discharge", Form.REPULSOR, 11575551, 5.0, 22, true),
        VIRULENT_HARVEST("virulent_harvest", Form.REAP, 14483320, 4.0, 28, false),
        ORCHARD_AEGIS("orchard_aegis", Form.SHIELD, 16770728, 1.5, 80, true),
        ABYSSAL_GEYSER("abyssal_geyser", Form.GEYSER, 7389439, 2.5, 20, false),
        EFFIGY_VOLLEY("effigy_volley", Form.DOLL, 15118293, 1.4, 16, true),
        EFFIGY_WARD("effigy_ward", Form.SHIELD, 16112311, 2.0, 80, true),
        CANDLE_REAP("candle_reap", Form.REAP, 15393225, 3.0, 24, true),
        CANDLE_PRISON("candle_prison", Form.CANDLES, 12493295, 3.5, 22, false),
        ARCANE_FUSILLADE("arcane_fusillade", Form.LASER, 3788741, 1.0, 16, true),
        BREACH_SHELL("breach_shell", Form.SHELL, 15890519, 4.0, 20, false),
        RENDING_CLEAVE("rending_cleave", Form.CLEAVE, 12998000, 3.0, 22, true),
        CRIMSON_OATH("crimson_oath", Form.OATH, 15822201, 1.5, 60, true),
        SEARING_PAN("searing_pan", Form.PAN, 16762221, 3.0, 24, true),
        HEARTH_FEAST("hearth_feast", Form.HEARTH, 16240269, 4.0, 22, false),
        FOUR_TIDES("four_tides", Form.TRIDENT, 6609370, 4.0, 24, true),
        ELIXIR_RAIN("elixir_rain", Form.RAIN, 9296262, 3.5, 22, false),
        ORBITAL_GEARS("orbital_gears", Form.GEARS, 10542311, 3.0, 14, true),
        PRESSURE_LANCE("pressure_lance", Form.PRESSURE, 7978234, 1.0, 18, true),
        SHADOW_BRAND("shadow_brand", Form.BRAND, 10254798, 3.5, 22, false),
        VOID_ECHO("void_echo", Form.VOID, 13341924, 2.5, 26, false),
        LUMINOUS_REBUKE("luminous_rebuke", Form.HALO, 15721641, 5.0, 26, true),
        HALO_SANCTUARY("halo_sanctuary", Form.GARDEN, 15851424, 4.0, 22, false),
        FEATHER_FAN("feather_fan", Form.FEATHERS, 14403247, 1.0, 18, true),
        WINGSTEP("wingstep", Form.WINGS, 15656150, 2.0, 60, true),
        FROST_NEEDLES("frost_needles", Form.NEEDLES, 11328250, 1.0, 18, true),
        WHITEOUT_EDICT("whiteout_edict", Form.BLIZZARD, 14149887, 3.5, 22, false),
        SUGAR_HAIL("sugar_hail", Form.BLIZZARD, 11132159, 4.0, 20, false),
        GLACIAL_PRISM("glacial_prism", Form.SHIELD, 13563647, 1.5, 60, true),
        ROYAL_SHATTER("royal_shatter", Form.CROWN, 0xAFAAFF, 4.0, 24, false),
        BRIAR_PROCESSION("briar_procession", Form.REAP, 8767356, 2.2, 18, false),
        SCARLET_POLLEN("scarlet_pollen", Form.SPORES, 15569584, 4.0, 20, false),
        CAMELLIA_REBIRTH("camellia_rebirth", Form.GARDEN, 11330993, 5.0, 20, true),
        FLESH_BLOOM("flesh_bloom", Form.FLOWER, 13194862, 4.0, 26, false),
        SANGUINE_SUTURE("sanguine_suture", Form.BRAND, 15688821, 1.0, 16, true),
        CINDER_BREATH("cinder_breath", Form.WAVE, 16756818, 3.0, 16, true),
        INTOXICATING_TOAST("intoxicating_toast", Form.OATH, 15582319, 1.5, 60, true),
        VINTAGE_MINEFIELD("vintage_minefield", Form.BARREL, 16218180, 2.2, 18, false),
        ION_RAIL("ion_rail", Form.PRESSURE, 8383477, 1.0, 18, true),
        STATIC_LATTICE("static_lattice", Form.GEARS, 11244031, 4.0, 20, false),
        EMERGENCY_REBOOT("emergency_reboot", Form.CORE, 8581820, 1.5, 30, true),
        SPORE_ERUPTION("spore_eruption", Form.GEYSER, 13885033, 4.0, 20, false),
        ROTTING_REQUIEM("rotting_requiem", Form.VORTEX, 12223952, 6.0, 20, false),
        LIVING_MULCH("living_mulch", Form.GARDEN, 9885554, 4.0, 30, true),
        SOLAR_ORCHARD("solar_orchard", Form.HALO, 16771489, 3.5, 20, true),
        JUDGMENT_SEED("judgment_seed", Form.HALO, 16769124, 4.0, 24, false),
        GOLDEN_HARVEST("golden_harvest", Form.REAP, 14086291, 4.0, 22, false),
        UNDERTOW_SNARE("undertow_snare", Form.VORTEX, 6796494, 4.0, 20, false),
        ABYSSAL_CARAPACE("abyssal_carapace", Form.SHIELD, 8446937, 1.5, 60, true),
        RIPTIDE_CORRIDOR("riptide_corridor", Form.WAVE, 8830719, 3.0, 20, false),
        EFFIGY_PROCESSION("effigy_procession", Form.PROCESSION, 16034015, 4.0, 24, false),
        CANDLE_ECLIPSE("candle_eclipse", Form.ECLIPSE, 13017343, 3.5, 28, false),
        PRISM_BATTERY("prism_battery", Form.BATTERY, 6675956, 4.0, 24, false),
        BLOOD_VERDICT("blood_verdict", Form.VERDICT, 15754371, 3.5, 24, false),
        CITRUS_NOVA("citrus_nova", Form.CITRUS, 16765275, 4.0, 24, false),
        TIDAL_LOTUS("tidal_lotus", Form.LOTUS, 6153160, 4.0, 60, true),
        MAGNETIC_STORM("magnetic_storm", Form.MAGNET, 0x66BBFF, 4.0, 24, false),
        NIGHTFALL_BLADES("nightfall_blades", Form.NIGHTBLADES, 11571199, 3.5, 24, false),
        SERAPH_DESCENT("seraph_descent", Form.SERAPH, 16769952, 4.0, 24, false),
        WINTER_CLOCK("winter_clock", Form.CLOCK, 10411775, 3.5, 24, false),
        FEATHER_REFUGE("feather_refuge", Form.REFUGE, 14996650, 4.0, 60, true),
        STAR_CROSS("star_cross", Form.STAR_CROSS, 15497651, 6.0, 20, false),
        ROSE_STARFALL("rose_starfall", Form.STARFALL, 16401276, 1.6, 20, false);

        public final String id;
        public final Form form;
        public final int color;
        public final double radius;
        public final int duration;
        public final boolean attached;

        private Spell(String id, Form form, int color, double radius, int duration, boolean attached) {
            this.id = id;
            this.form = form;
            this.color = color;
            this.radius = radius;
            this.duration = duration;
            this.attached = attached;
        }

        public int lifetime(Phase phase) {
            return ArcaneReadability.lifetime(switch (phase) {
                default -> throw new IncompatibleClassChangeError();
                case Phase.CHARGE -> 10;
                case Phase.BEAM -> 12;
                case Phase.PULSE -> {
                    if (this == CASK_RUPTURE || this == ABYSSAL_GEYSER || this == BREACH_SHELL) {
                        yield 24;
                    }
                    yield this.duration;
                }
                case Phase.RELEASE -> this.duration;
            }, this, phase);
        }

        public boolean followsCaster(Phase phase) {
            return this.attached && (phase == Phase.CHARGE || this.form == Form.SHIELD || this.form == Form.OATH || this.form == Form.WINGS || this.form == Form.GEARS || this.form == Form.GARDEN || this == SOLAR_ORCHARD);
        }
    }

    public static enum Form {
        CROWN,
        FLOWER,
        VORTEX,
        CORE,
        SPORES,
        HALO,
        WAVE,
        LANCE,
        GARDEN,
        BARREL,
        REPULSOR,
        REAP,
        SHIELD,
        GEYSER,
        DOLL,
        CANDLES,
        LASER,
        SHELL,
        CLEAVE,
        OATH,
        PAN,
        HEARTH,
        TRIDENT,
        RAIN,
        GEARS,
        PRESSURE,
        BRAND,
        VOID,
        FEATHERS,
        WINGS,
        NEEDLES,
        BLIZZARD,
        PROCESSION,
        ECLIPSE,
        BATTERY,
        VERDICT,
        CITRUS,
        LOTUS,
        MAGNET,
        NIGHTBLADES,
        SERAPH,
        CLOCK,
        REFUGE,
        STAR_CROSS,
        STARFALL;

    }

    public static enum Phase {
        CHARGE,
        RELEASE,
        PULSE,
        BEAM;

    }
}

