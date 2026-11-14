/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.client.render;

import java.util.EnumSet;
import java.util.Set;
import net.farmingtales.ftboss.util.SpellVisualRules;

public final class ManorSpellMergeRules {
    private static final Set<SpellVisualRules.Spell> LEGACY_SOLID = EnumSet.of(SpellVisualRules.Spell.CANDLE_PRISON, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.BREACH_SHELL, SpellVisualRules.Spell.RENDING_CLEAVE, SpellVisualRules.Spell.CRIMSON_OATH, SpellVisualRules.Spell.SEARING_PAN, SpellVisualRules.Spell.HEARTH_FEAST, SpellVisualRules.Spell.FOUR_TIDES, SpellVisualRules.Spell.ELIXIR_RAIN, SpellVisualRules.Spell.ORBITAL_GEARS, SpellVisualRules.Spell.SHADOW_BRAND, SpellVisualRules.Spell.VOID_ECHO, SpellVisualRules.Spell.LUMINOUS_REBUKE, SpellVisualRules.Spell.HALO_SANCTUARY, SpellVisualRules.Spell.WHITEOUT_EDICT, SpellVisualRules.Spell.BLOOD_VERDICT, SpellVisualRules.Spell.CITRUS_NOVA, SpellVisualRules.Spell.TIDAL_LOTUS, SpellVisualRules.Spell.WINTER_CLOCK, SpellVisualRules.Spell.FEATHER_REFUGE});
    private static final Set<SpellVisualRules.Spell> NESTED_LEGACY = EnumSet.of(SpellVisualRules.Spell.CANDLE_PRISON, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.CRIMSON_OATH, SpellVisualRules.Spell.HEARTH_FEAST, SpellVisualRules.Spell.ELIXIR_RAIN, SpellVisualRules.Spell.ORBITAL_GEARS, SpellVisualRules.Spell.SHADOW_BRAND, SpellVisualRules.Spell.VOID_ECHO, SpellVisualRules.Spell.HALO_SANCTUARY, SpellVisualRules.Spell.WHITEOUT_EDICT, SpellVisualRules.Spell.CITRUS_NOVA, SpellVisualRules.Spell.TIDAL_LOTUS, SpellVisualRules.Spell.WINTER_CLOCK, SpellVisualRules.Spell.FEATHER_REFUGE});
    private static final Set<SpellVisualRules.Spell> PAIRED_LEGACY = EnumSet.of(SpellVisualRules.Spell.BREACH_SHELL, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.RENDING_CLEAVE, SpellVisualRules.Spell.SEARING_PAN, SpellVisualRules.Spell.FOUR_TIDES, SpellVisualRules.Spell.LUMINOUS_REBUKE, SpellVisualRules.Spell.BLOOD_VERDICT});
    private static final Set<SpellVisualRules.Spell> LEGACY_BEAM = EnumSet.of(SpellVisualRules.Spell.EFFIGY_VOLLEY, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.CANDLE_REAP, SpellVisualRules.Spell.ARCANE_FUSILLADE, SpellVisualRules.Spell.PRESSURE_LANCE, SpellVisualRules.Spell.FEATHER_FAN, SpellVisualRules.Spell.FROST_NEEDLES, SpellVisualRules.Spell.EFFIGY_PROCESSION, SpellVisualRules.Spell.CANDLE_ECLIPSE, SpellVisualRules.Spell.PRISM_BATTERY, SpellVisualRules.Spell.MAGNETIC_STORM, SpellVisualRules.Spell.NIGHTFALL_BLADES, SpellVisualRules.Spell.SERAPH_DESCENT, SpellVisualRules.Spell.STAR_CROSS, SpellVisualRules.Spell.ROSE_STARFALL});
    private static final Set<SpellVisualRules.Spell> FLOATING = EnumSet.of(SpellVisualRules.Spell.EFFIGY_VOLLEY, new SpellVisualRules.Spell[]{SpellVisualRules.Spell.CANDLE_REAP, SpellVisualRules.Spell.ARCANE_FUSILLADE, SpellVisualRules.Spell.RENDING_CLEAVE, SpellVisualRules.Spell.SEARING_PAN, SpellVisualRules.Spell.FOUR_TIDES, SpellVisualRules.Spell.ORBITAL_GEARS, SpellVisualRules.Spell.PRESSURE_LANCE, SpellVisualRules.Spell.VOID_ECHO, SpellVisualRules.Spell.LUMINOUS_REBUKE, SpellVisualRules.Spell.FEATHER_FAN, SpellVisualRules.Spell.FROST_NEEDLES, SpellVisualRules.Spell.CITRUS_NOVA, SpellVisualRules.Spell.MAGNETIC_STORM, SpellVisualRules.Spell.NIGHTFALL_BLADES, SpellVisualRules.Spell.WINTER_CLOCK, SpellVisualRules.Spell.STAR_CROSS, SpellVisualRules.Spell.ROSE_STARFALL});

    private ManorSpellMergeRules() {
    }

    public static boolean hideBulkyInFirstPerson(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, boolean bl, boolean bl2) {
        return false;
    }

    public static float modelScale(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        boolean bl = ManorSpellMergeRules.hasLegacySolid(spell, phase);
        if (phase == SpellVisualRules.Phase.BEAM) {
            return bl ? 0.6f : 0.74f;
        }
        if (bl && NESTED_LEGACY.contains((Object)spell)) {
            return 0.58f;
        }
        if (bl && PAIRED_LEGACY.contains((Object)spell)) {
            return 0.66f;
        }
        if (bl) {
            return 0.62f;
        }
        return 0.82f;
    }

    public static float modelBottomOffset(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double d, float f, double d2) {
        if (phase == SpellVisualRules.Phase.BEAM) {
            return (float)(0.06 - Math.max(0.0, d) * (double)Math.max(0.0f, f));
        }
        float f2 = ManorSpellMergeRules.baseLift(spell);
        if (FLOATING.contains((Object)spell) && Double.isFinite(d2)) {
            f2 += (float)((Math.sin(d2 * 0.11 + (double)spell.ordinal() * 0.67) + 1.0) * 0.015);
        }
        return f2;
    }

    public static float forwardOffset(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase, double d, float f) {
        if (phase == SpellVisualRules.Phase.BEAM) {
            return (float)ManorSpellMergeRules.beamAdvance(d, f);
        }
        if (ManorSpellMergeRules.hasLegacySolid(spell, phase) && PAIRED_LEGACY.contains((Object)spell)) {
            return 0.44f;
        }
        return switch (spell.form) {
            case SpellVisualRules.Form.DOLL, SpellVisualRules.Form.REAP, SpellVisualRules.Form.LASER, SpellVisualRules.Form.SHELL, SpellVisualRules.Form.CLEAVE, SpellVisualRules.Form.PAN, SpellVisualRules.Form.TRIDENT, SpellVisualRules.Form.PRESSURE, SpellVisualRules.Form.FEATHERS, SpellVisualRules.Form.NEEDLES, SpellVisualRules.Form.VERDICT -> 0.3f;
            case SpellVisualRules.Form.WINGS -> -0.2f;
            default -> 0.0f;
        };
    }

    public static float yawOffset(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if (!ManorSpellMergeRules.hasLegacySolid(spell, phase)) {
            return 0.0f;
        }
        float f = PAIRED_LEGACY.contains((Object)spell) ? 0.12f : 0.08f;
        return (spell.ordinal() & 1) == 0 ? f : -f;
    }

    public static float modelAlpha(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if (!ManorSpellMergeRules.hasLegacySolid(spell, phase)) {
            return 1.0f;
        }
        if (phase == SpellVisualRules.Phase.BEAM) {
            return 0.9f;
        }
        return NESTED_LEGACY.contains((Object)spell) ? 0.88f : 0.94f;
    }

    public static float legacyVerticalOffset(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if (phase == SpellVisualRules.Phase.BEAM) {
            return 0.0f;
        }
        return switch (spell) {
            case SpellVisualRules.Spell.RENDING_CLEAVE -> 0.21f;
            case SpellVisualRules.Spell.BLOOD_VERDICT -> 0.68f;
            case SpellVisualRules.Spell.WINTER_CLOCK -> 0.77f;
            case SpellVisualRules.Spell.FEATHER_REFUGE -> 0.22f;
            case SpellVisualRules.Spell.VOID_ECHO -> 0.045f;
            default -> 0.03f;
        };
    }

    public static double beamAdvance(double d, float f) {
        if (!Double.isFinite(d) || d <= 0.0 || !Float.isFinite(f) || f <= 0.0f) {
            return 0.0;
        }
        return Math.min(d * 0.46, Math.max(0.8, 1.55 * (double)f));
    }

    public static boolean hasLegacySolid(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if (phase == SpellVisualRules.Phase.BEAM) {
            return LEGACY_BEAM.contains((Object)spell);
        }
        if (spell == SpellVisualRules.Spell.LUMINOUS_REBUKE) {
            return phase == SpellVisualRules.Phase.RELEASE;
        }
        return LEGACY_SOLID.contains((Object)spell);
    }

    private static float baseLift(SpellVisualRules.Spell spell) {
        return switch (spell) {
            case SpellVisualRules.Spell.EFFIGY_VOLLEY -> 0.55f;
            case SpellVisualRules.Spell.EFFIGY_WARD -> 0.035f;
            case SpellVisualRules.Spell.CANDLE_REAP -> 0.58f;
            case SpellVisualRules.Spell.CANDLE_PRISON -> 0.035f;
            case SpellVisualRules.Spell.ARCANE_FUSILLADE -> 0.72f;
            case SpellVisualRules.Spell.BREACH_SHELL -> 0.04f;
            case SpellVisualRules.Spell.RENDING_CLEAVE -> 0.52f;
            case SpellVisualRules.Spell.CRIMSON_OATH -> 0.035f;
            case SpellVisualRules.Spell.SEARING_PAN -> 0.52f;
            case SpellVisualRules.Spell.HEARTH_FEAST -> 0.035f;
            case SpellVisualRules.Spell.FOUR_TIDES -> 0.46f;
            case SpellVisualRules.Spell.ELIXIR_RAIN -> 0.04f;
            case SpellVisualRules.Spell.ORBITAL_GEARS -> 0.26f;
            case SpellVisualRules.Spell.PRESSURE_LANCE -> 0.68f;
            case SpellVisualRules.Spell.SHADOW_BRAND -> 0.04f;
            case SpellVisualRules.Spell.VOID_ECHO -> 0.3f;
            case SpellVisualRules.Spell.LUMINOUS_REBUKE -> 0.52f;
            case SpellVisualRules.Spell.HALO_SANCTUARY -> 0.035f;
            case SpellVisualRules.Spell.FEATHER_FAN -> 0.52f;
            case SpellVisualRules.Spell.WINGSTEP -> 0.025f;
            case SpellVisualRules.Spell.FROST_NEEDLES -> 0.48f;
            case SpellVisualRules.Spell.WHITEOUT_EDICT -> 0.05f;
            case SpellVisualRules.Spell.EFFIGY_PROCESSION -> 0.035f;
            case SpellVisualRules.Spell.CANDLE_ECLIPSE -> 0.035f;
            case SpellVisualRules.Spell.PRISM_BATTERY -> 0.04f;
            case SpellVisualRules.Spell.BLOOD_VERDICT -> 0.04f;
            case SpellVisualRules.Spell.CITRUS_NOVA -> 0.2f;
            case SpellVisualRules.Spell.TIDAL_LOTUS -> 0.035f;
            case SpellVisualRules.Spell.MAGNETIC_STORM -> 0.24f;
            case SpellVisualRules.Spell.NIGHTFALL_BLADES -> 0.28f;
            case SpellVisualRules.Spell.SERAPH_DESCENT -> 0.035f;
            case SpellVisualRules.Spell.WINTER_CLOCK -> 0.2f;
            case SpellVisualRules.Spell.FEATHER_REFUGE -> 0.035f;
            case SpellVisualRules.Spell.STAR_CROSS -> 0.28f;
            case SpellVisualRules.Spell.ROSE_STARFALL -> 0.22f;
            default -> 0.03f;
        };
    }
}

