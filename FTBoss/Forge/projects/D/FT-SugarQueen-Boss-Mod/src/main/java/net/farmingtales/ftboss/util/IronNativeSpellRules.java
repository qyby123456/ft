package net.farmingtales.ftboss.util;

import java.util.Set;
import net.farmingtales.ftboss.util.SpellVisualRules.Phase;
import net.farmingtales.ftboss.util.SpellVisualRules.Spell;

public final class IronNativeSpellRules {
    public static final int MAX_ACTIVE = 24;
    public static final int MAX_MODELS_PER_FRAME = 48;
    public static final double VIEW_RANGE = 48;
    public enum Style { ICE_CROWN, BLOOD_REAP, SPORES, HEAL, CASK_BLAST, GOLD_SHIELD,
        IVORY_SHIELD, SHELL_BLAST, HEARTH, MEDICINAL_RAIN, WINGS, SNOW }

    public static final Set<Spell> LINE_THREAD = Set.of(Spell.SUGAR_HAIL, Spell.GLACIAL_PRISM,
            Spell.ROYAL_SHATTER, Spell.BRIAR_PROCESSION, Spell.SCARLET_POLLEN, Spell.CAMELLIA_REBIRTH,
            Spell.FLESH_BLOOM, Spell.SANGUINE_SUTURE, Spell.CINDER_BREATH, Spell.INTOXICATING_TOAST,
            Spell.VINTAGE_MINEFIELD, Spell.ION_RAIL, Spell.STATIC_LATTICE, Spell.EMERGENCY_REBOOT,
            Spell.SPORE_ERUPTION, Spell.ROTTING_REQUIEM, Spell.LIVING_MULCH, Spell.SOLAR_ORCHARD,
            Spell.JUDGMENT_SEED, Spell.GOLDEN_HARVEST, Spell.UNDERTOW_SNARE, Spell.ABYSSAL_CARAPACE,
            Spell.RIPTIDE_CORRIDOR);

    private IronNativeSpellRules() {}

    public static Style style(Spell spell, Phase phase) {
        if (spell == null || phase == null || phase == Phase.CHARGE || phase == Phase.BEAM) return null;
        if (phase == Phase.RELEASE) return switch (spell) {
            case CRYSTAL_CORONATION -> Style.ICE_CROWN;
            case CANDLE_REAP -> Style.BLOOD_REAP;
            case TIDAL_LOTUS -> Style.HEAL;
            case ORCHARD_AEGIS -> Style.GOLD_SHIELD;
            case EFFIGY_WARD -> Style.IVORY_SHIELD;
            case WINGSTEP -> Style.WINGS;
            default -> null;
        };
        return switch (spell) {
            case PRISMATIC_BLIGHT -> Style.SPORES;
            case CASK_RUPTURE -> Style.CASK_BLAST;
            case BREACH_SHELL -> Style.SHELL_BLAST;
            case HEARTH_FEAST -> Style.HEARTH;
            case ELIXIR_RAIN -> Style.MEDICINAL_RAIN;
            case WHITEOUT_EDICT -> Style.SNOW;
            default -> null;
        };
    }

    public static boolean model(Style style) {
        return style == Style.ICE_CROWN || style == Style.GOLD_SHIELD || style == Style.IVORY_SHIELD || style == Style.WINGS;
    }

    public static int lifetime(Style style) {
        return switch (style) {
            case GOLD_SHIELD, IVORY_SHIELD -> 60;
            case WINGS -> 40;
            case ICE_CROWN -> 28;
            default -> 20;
        };
    }

    public static boolean follows(Style style) {
        return style == Style.GOLD_SHIELD || style == Style.IVORY_SHIELD || style == Style.WINGS || style == Style.HEAL;
    }

    public static int emissionWaves(Style style) {
        return switch (style) {
            case CASK_BLAST, SHELL_BLAST, ICE_CROWN, GOLD_SHIELD, IVORY_SHIELD, WINGS -> 1;
            case BLOOD_REAP -> 3;
            default -> 5;
        };
    }

    public static int wave(long age, Style style) {
        return age < 0 || age >= lifetime(style) ? -1 : Math.min((int) age / 4, emissionWaves(style) - 1);
    }

    public static int particleBudget(int quality) { return quality >= 2 ? 24 : quality == 1 ? 64 : 128; }
    public static int particlesPerWave(int quality) { return quality >= 2 ? 4 : quality == 1 ? 8 : 16; }

    public static int monochrome(int color) {
        int brightness = Math.max(color & 255, Math.max(color >> 8 & 255, color >> 16 & 255));
        return color & 0xFF000000 | brightness * 0x010101;
    }

    public static float growth(double age, double delay) {
        if (!Double.isFinite(age) || !Double.isFinite(delay)) return 0;
        double progress = Math.max(0, Math.min(1, (age - delay) / 7));
        return (float) (progress * progress * (3 - 2 * progress));
    }
}
