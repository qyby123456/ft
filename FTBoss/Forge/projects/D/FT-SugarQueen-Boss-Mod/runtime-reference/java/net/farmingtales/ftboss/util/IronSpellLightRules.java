/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import net.farmingtales.ftboss.util.SpellVisualRules;

public final class IronSpellLightRules {
    private IronSpellLightRules() {
    }

    public static Beam beam(SpellVisualRules.Spell spell) {
        return switch (spell) {
            default -> throw new IncompatibleClassChangeError();
            case SpellVisualRules.Spell.CAPACITOR_OVERLOAD, SpellVisualRules.Spell.STATIC_LATTICE, SpellVisualRules.Spell.MAGNETIC_STORM -> Beam.ZAP;
            case SpellVisualRules.Spell.PRESSURE_LANCE, SpellVisualRules.Spell.ION_RAIL, SpellVisualRules.Spell.STAR_CROSS -> Beam.TRACE;
            case SpellVisualRules.Spell.FROSTGLASS_LANCE, SpellVisualRules.Spell.FROST_NEEDLES -> Beam.FROST_TRACE;
            case SpellVisualRules.Spell.CAMELLIA_BLOOD_BLOOM, SpellVisualRules.Spell.SANGUINE_SUTURE, SpellVisualRules.Spell.FLESH_BLOOM, SpellVisualRules.Spell.CANDLE_ECLIPSE -> Beam.SIPHON;
            case SpellVisualRules.Spell.GILDED_VERDICT, SpellVisualRules.Spell.ARCANE_FUSILLADE, SpellVisualRules.Spell.EFFIGY_VOLLEY, SpellVisualRules.Spell.FEATHER_FAN, SpellVisualRules.Spell.CAMELLIA_SANCTUARY, SpellVisualRules.Spell.CAMELLIA_REBIRTH, SpellVisualRules.Spell.VIRULENT_HARVEST, SpellVisualRules.Spell.GOLDEN_HARVEST, SpellVisualRules.Spell.PRISM_BATTERY, SpellVisualRules.Spell.EFFIGY_PROCESSION, SpellVisualRules.Spell.NIGHTFALL_BLADES, SpellVisualRules.Spell.SERAPH_DESCENT -> Beam.TRACE;
            case SpellVisualRules.Spell.CRYSTAL_CORONATION, SpellVisualRules.Spell.VINTAGE_VORTEX, SpellVisualRules.Spell.PRISMATIC_BLIGHT, SpellVisualRules.Spell.TIDAL_EDICT, SpellVisualRules.Spell.CASK_RUPTURE, SpellVisualRules.Spell.REPULSOR_DISCHARGE, SpellVisualRules.Spell.ORCHARD_AEGIS, SpellVisualRules.Spell.ABYSSAL_GEYSER, SpellVisualRules.Spell.EFFIGY_WARD, SpellVisualRules.Spell.CANDLE_REAP, SpellVisualRules.Spell.CANDLE_PRISON, SpellVisualRules.Spell.BREACH_SHELL, SpellVisualRules.Spell.RENDING_CLEAVE, SpellVisualRules.Spell.CRIMSON_OATH, SpellVisualRules.Spell.SEARING_PAN, SpellVisualRules.Spell.HEARTH_FEAST, SpellVisualRules.Spell.FOUR_TIDES, SpellVisualRules.Spell.ELIXIR_RAIN, SpellVisualRules.Spell.ORBITAL_GEARS, SpellVisualRules.Spell.SHADOW_BRAND, SpellVisualRules.Spell.VOID_ECHO, SpellVisualRules.Spell.LUMINOUS_REBUKE, SpellVisualRules.Spell.HALO_SANCTUARY, SpellVisualRules.Spell.WINGSTEP, SpellVisualRules.Spell.WHITEOUT_EDICT, SpellVisualRules.Spell.SUGAR_HAIL, SpellVisualRules.Spell.GLACIAL_PRISM, SpellVisualRules.Spell.ROYAL_SHATTER, SpellVisualRules.Spell.BRIAR_PROCESSION, SpellVisualRules.Spell.SCARLET_POLLEN, SpellVisualRules.Spell.CINDER_BREATH, SpellVisualRules.Spell.INTOXICATING_TOAST, SpellVisualRules.Spell.VINTAGE_MINEFIELD, SpellVisualRules.Spell.EMERGENCY_REBOOT, SpellVisualRules.Spell.SPORE_ERUPTION, SpellVisualRules.Spell.ROTTING_REQUIEM, SpellVisualRules.Spell.LIVING_MULCH, SpellVisualRules.Spell.SOLAR_ORCHARD, SpellVisualRules.Spell.JUDGMENT_SEED, SpellVisualRules.Spell.UNDERTOW_SNARE, SpellVisualRules.Spell.ABYSSAL_CARAPACE, SpellVisualRules.Spell.RIPTIDE_CORRIDOR, SpellVisualRules.Spell.BLOOD_VERDICT, SpellVisualRules.Spell.CITRUS_NOVA, SpellVisualRules.Spell.TIDAL_LOTUS, SpellVisualRules.Spell.WINTER_CLOCK, SpellVisualRules.Spell.FEATHER_REFUGE, SpellVisualRules.Spell.ROSE_STARFALL -> Beam.NONE;
        };
    }

    public static Accent accent(SpellVisualRules.Spell spell, SpellVisualRules.Phase phase) {
        if (phase != SpellVisualRules.Phase.RELEASE && phase != SpellVisualRules.Phase.PULSE) {
            return Accent.NONE;
        }
        return switch (spell) {
            case SpellVisualRules.Spell.REPULSOR_DISCHARGE -> {
                if (phase == SpellVisualRules.Phase.RELEASE) {
                    yield Accent.RADIAL_ZAP;
                }
                yield Accent.NONE;
            }
            case SpellVisualRules.Spell.EMERGENCY_REBOOT -> {
                if (phase == SpellVisualRules.Phase.PULSE) {
                    yield Accent.REBOOT_ZAP;
                }
                yield Accent.NONE;
            }
            case SpellVisualRules.Spell.LUMINOUS_REBUKE -> {
                if (phase == SpellVisualRules.Phase.RELEASE) {
                    yield Accent.HOLY_RAYS;
                }
                yield Accent.NONE;
            }
            default -> Accent.NONE;
        };
    }

    public static boolean adapted(SpellVisualRules.Spell spell) {
        return IronSpellLightRules.beam(spell) != Beam.NONE || IronSpellLightRules.accent(spell, SpellVisualRules.Phase.RELEASE) != Accent.NONE || IronSpellLightRules.accent(spell, SpellVisualRules.Phase.PULSE) != Accent.NONE;
    }

    public static boolean traceSafe(double coordinate) {
        return SpellVisualRules.validPoint(coordinate) && Math.abs(coordinate - (double)((float)coordinate)) <= 0.015625;
    }

    public static int samples(double distance, boolean reduced) {
        if (!Double.isFinite(distance) || distance < 0.05 || distance > 64.0) {
            return 0;
        }
        return Math.min(reduced ? 6 : 12, Math.max(2, (int)Math.ceil(distance / 2.0)));
    }

    public static int cost(Beam beam, double distance, boolean reduced) {
        int count = IronSpellLightRules.samples(distance, reduced);
        if (count == 0 || beam == Beam.NONE) {
            return 0;
        }
        return switch (beam) {
            case Beam.SOUL_RAY -> 0;
            case Beam.SIPHON -> count * 2;
            case Beam.ZAP -> 4;
            default -> count;
        };
    }

    public static enum Beam {
        NONE,
        TRACE,
        FROST_TRACE,
        ZAP,
        SOUL_RAY,
        SIPHON;

    }

    public static enum Accent {
        NONE,
        RADIAL_ZAP,
        REBOOT_ZAP,
        HOLY_RAYS;

    }

    public static final class Budget {
        private long tick = Long.MIN_VALUE;
        private int spent;

        public boolean reserve(long now, int amount, int limit) {
            if (now != this.tick) {
                this.tick = now;
                this.spent = 0;
            }
            if (amount <= 0 || amount > limit || this.spent > limit - amount) {
                return false;
            }
            this.spent += amount;
            return true;
        }
    }
}

