/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Set;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class IronNativeSpellRules {
    public static final int MAX_ACTIVE = 24;
    public static final int MAX_MODELS_PER_FRAME = 48;
    public static final double VIEW_RANGE = 48.0;
    public static final Set<SpellVisualRules.Spell> LINE_THREAD = Set.of(SpellVisualRules.Spell.SUGAR_HAIL, SpellVisualRules.Spell.GLACIAL_PRISM, SpellVisualRules.Spell.ROYAL_SHATTER, SpellVisualRules.Spell.BRIAR_PROCESSION, SpellVisualRules.Spell.SCARLET_POLLEN, SpellVisualRules.Spell.CAMELLIA_REBIRTH, SpellVisualRules.Spell.FLESH_BLOOM, SpellVisualRules.Spell.SANGUINE_SUTURE, SpellVisualRules.Spell.CINDER_BREATH, SpellVisualRules.Spell.INTOXICATING_TOAST, SpellVisualRules.Spell.VINTAGE_MINEFIELD, SpellVisualRules.Spell.ION_RAIL, SpellVisualRules.Spell.STATIC_LATTICE, SpellVisualRules.Spell.EMERGENCY_REBOOT, SpellVisualRules.Spell.SPORE_ERUPTION, SpellVisualRules.Spell.ROTTING_REQUIEM, SpellVisualRules.Spell.LIVING_MULCH, SpellVisualRules.Spell.SOLAR_ORCHARD, SpellVisualRules.Spell.JUDGMENT_SEED, SpellVisualRules.Spell.GOLDEN_HARVEST, SpellVisualRules.Spell.UNDERTOW_SNARE, SpellVisualRules.Spell.ABYSSAL_CARAPACE, SpellVisualRules.Spell.RIPTIDE_CORRIDOR);

    private IronNativeSpellRules() {
    }

    public static Style style(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if (spell == null || phase == null || phase == SpellVisualRules.Phase.CHARGE || phase == SpellVisualRules.Phase.BEAM) {
            return null;
        }
        if (phase == SpellVisualRules.Phase.RELEASE) {
            return switch (spell) {
                case SpellVisualRules.Spell.CRYSTAL_CORONATION -> Style.ICE_CROWN;
                case SpellVisualRules.Spell.CANDLE_REAP -> Style.BLOOD_REAP;
                case SpellVisualRules.Spell.TIDAL_LOTUS -> Style.HEAL;
                case SpellVisualRules.Spell.ORCHARD_AEGIS -> Style.GOLD_SHIELD;
                case SpellVisualRules.Spell.EFFIGY_WARD -> Style.IVORY_SHIELD;
                case SpellVisualRules.Spell.WINGSTEP -> Style.WINGS;
                default -> null;
            };
        }
        return switch (spell) {
            case SpellVisualRules.Spell.PRISMATIC_BLIGHT -> Style.SPORES;
            case SpellVisualRules.Spell.CASK_RUPTURE -> Style.CASK_BLAST;
            case SpellVisualRules.Spell.BREACH_SHELL -> Style.SHELL_BLAST;
            case SpellVisualRules.Spell.HEARTH_FEAST -> Style.HEARTH;
            case SpellVisualRules.Spell.ELIXIR_RAIN -> Style.MEDICINAL_RAIN;
            case SpellVisualRules.Spell.WHITEOUT_EDICT -> Style.SNOW;
            default -> null;
        };
    }

    public static boolean model(Style style) {
        return style == Style.ICE_CROWN || style == Style.GOLD_SHIELD || style == Style.IVORY_SHIELD || style == Style.WINGS;
    }

    public static int lifetime(Style style) {
        return switch (style) {
            case Style.GOLD_SHIELD, Style.IVORY_SHIELD -> 60;
            case Style.WINGS -> 40;
            case Style.ICE_CROWN -> 28;
            default -> 20;
        };
    }

    public static boolean follows(Style style) {
        return style == Style.GOLD_SHIELD || style == Style.IVORY_SHIELD || style == Style.WINGS || style == Style.HEAL;
    }

    public static int emissionWaves(Style style) {
        return switch (style) {
            case Style.GOLD_SHIELD, Style.IVORY_SHIELD, Style.WINGS, Style.ICE_CROWN, Style.CASK_BLAST, Style.SHELL_BLAST -> 1;
            case Style.BLOOD_REAP -> 3;
            default -> 5;
        };
    }

    public static int wave(long age, Style style) {
        return age < 0L || age >= (long)IronNativeSpellRules.lifetime(style) ? -1 : Math.min((int)age / 4, IronNativeSpellRules.emissionWaves(style) - 1);
    }

    public static int particleBudget(int quality) {
        return quality >= 2 ? 24 : (quality == 1 ? 64 : 128);
    }

    public static int particlesPerWave(int quality) {
        return quality >= 2 ? 4 : (quality == 1 ? 8 : 16);
    }

    public static int monochrome(int color) {
        int brightness = Math.max(color & 0xFF, Math.max(color >> 8 & 0xFF, color >> 16 & 0xFF));
        return color & 0xFF000000 | brightness * 65793;
    }

    public static float growth(double age, double delay) {
        if (!Double.isFinite(age) || !Double.isFinite(delay)) {
            return 0.0f;
        }
        double progress = Math.max(0.0, Math.min(1.0, (age - delay) / 7.0));
        return (float)(progress * progress * (3.0 - 2.0 * progress));
    }

    public static enum Style {
        ICE_CROWN,
        BLOOD_REAP,
        SPORES,
        HEAL,
        CASK_BLAST,
        GOLD_SHIELD,
        IVORY_SHIELD,
        SHELL_BLAST,
        HEARTH,
        MEDICINAL_RAIN,
        WINGS,
        SNOW;

    }
}

