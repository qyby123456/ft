/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class WeaponEvolutionSkillPool {
    public static final int EXPECTED_FUSION_COUNT = 55;
    private static final Map<Pair, Fusion> FUSIONS;
    private static final Map<Aspect, Map<Aspect, Fusion>> MATRIX;

    private WeaponEvolutionSkillPool() {
    }

    private static void add(Map<Pair, Fusion> output, Aspect first, Aspect second, String id, String primaryVisual, String secondaryVisual, FusionStyle style) {
        Pair pair = new Pair(first, second);
        int primaryCooldown = Math.max(180, (pair.first.cooldownTicks + pair.second.cooldownTicks) / 2 - 40);
        int secondaryCooldown = switch (style) {
            default -> throw new IncompatibleClassChangeError();
            case FusionStyle.DASH -> 360;
            case FusionStyle.CHAIN, FusionStyle.BEAM -> 440;
            case FusionStyle.WARD -> 480;
            case FusionStyle.VORTEX -> 500;
            case FusionStyle.BURST, FusionStyle.NOVA -> 400;
        };
        Fusion replaced = output.put(pair, new Fusion(pair, id, primaryVisual, secondaryVisual, style, primaryCooldown, secondaryCooldown));
        if (replaced != null) {
            throw new IllegalStateException("Duplicate evolution pair " + pair.id());
        }
    }

    public static Fusion fusion(Aspect first, Aspect second) {
        Map<Aspect, Fusion> row = MATRIX.get((Object)first);
        return row == null ? null : row.get((Object)second);
    }

    public static Map<Pair, Fusion> allFusions() {
        return FUSIONS;
    }

    static {
        LinkedHashMap<Pair, Fusion> f = new LinkedHashMap<Pair, Fusion>();
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.SUGAR, "royal_whiteout", "royal_shatter", "whiteout_edict", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.MEAT, "carnivore_frenzy", "blood_verdict", "sanguine_suture", FusionStyle.NOVA);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.TEA, "camellia_procession", "briar_procession", "camellia_rebirth", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.WINE, "vintage_rupture", "vintage_vortex", "vintage_minefield", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.CHEESE, Aspect.CHEESE, "rat_king_feast", "gilded_verdict", "golden_harvest", FusionStyle.NOVA);
        WeaponEvolutionSkillPool.add(f, Aspect.SHEPHERD, Aspect.SHEPHERD, "alpha_hunt", "feather_fan", "feather_refuge", FusionStyle.DASH);
        WeaponEvolutionSkillPool.add(f, Aspect.ARC, Aspect.ARC, "capacitor_overdrive", "capacitor_overload", "magnetic_storm", FusionStyle.CHAIN);
        WeaponEvolutionSkillPool.add(f, Aspect.COMPOST, Aspect.COMPOST, "virulent_harvest", "virulent_harvest", "rotting_requiem", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.APPLE, Aspect.APPLE, "solar_orchard", "solar_orchard", "golden_harvest", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.TIDE, Aspect.TIDE, "four_tides", "four_tides", "abyssal_carapace", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.MEAT, "frostblood_carving", "frostglass_lance", "scarlet_pollen", FusionStyle.BEAM);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.TEA, "frost_camellia", "frost_needles", "camellia_sanctuary", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.WINE, "mulled_ice", "sugar_hail", "vintage_vortex", FusionStyle.BURST);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.CHEESE, "frozen_fondue", "glacial_prism", "gilded_verdict", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.SHEPHERD, "whitefang_hunt", "whiteout_edict", "wingstep", FusionStyle.DASH);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.ARC, "frostglass_capacitor", "frostglass_lance", "prism_battery", FusionStyle.BEAM);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.COMPOST, "cryospore_blight", "whiteout_edict", "spore_eruption", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.APPLE, "candied_orchard", "crystal_coronation", "solar_orchard", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.SUGAR, Aspect.TIDE, "brine_whiteout", "glacial_prism", "riptide_corridor", FusionStyle.BURST);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.TEA, "flesh_bloom", "flesh_bloom", "camellia_blood_bloom", FusionStyle.NOVA);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.WINE, "blood_vintage", "crimson_oath", "intoxicating_toast", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.CHEESE, "butchers_board", "hearth_feast", "blood_verdict", FusionStyle.NOVA);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.SHEPHERD, "red_hunt", "rending_cleave", "wingstep", FusionStyle.DASH);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.ARC, "galvanic_butchery", "ion_rail", "rending_cleave", FusionStyle.BEAM);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.COMPOST, "living_mulch", "living_mulch", "rotting_requiem", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.APPLE, "red_orchard", "sanguine_suture", "orchard_aegis", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.MEAT, Aspect.TIDE, "bloodtide", "blood_verdict", "tidal_edict", FusionStyle.NOVA);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.WINE, "fermented_camellia", "elixir_rain", "vintage_vortex", FusionStyle.BURST);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.CHEESE, "teahouse_banquet", "scarlet_pollen", "golden_harvest", FusionStyle.BURST);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.SHEPHERD, "briar_pack", "briar_procession", "feather_refuge", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.ARC, "static_pollen", "static_lattice", "scarlet_pollen", FusionStyle.CHAIN);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.COMPOST, "compost_garden", "spore_eruption", "living_mulch", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.APPLE, "grafted_orchard", "camellia_rebirth", "solar_orchard", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.TEA, Aspect.TIDE, "lotus_garden", "tidal_lotus", "camellia_sanctuary", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.CHEESE, "tavern_feast", "hearth_feast", "intoxicating_toast", FusionStyle.NOVA);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.SHEPHERD, "drunken_hunt", "vintage_minefield", "wingstep", FusionStyle.DASH);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.ARC, "charged_vintage", "pressure_lance", "capacitor_overload", FusionStyle.BEAM);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.COMPOST, "fermentation_rot", "vintage_vortex", "virulent_harvest", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.APPLE, "cider_orchard", "intoxicating_toast", "golden_harvest", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.WINE, Aspect.TIDE, "riptide_cask", "cask_rupture", "undertow_snare", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.CHEESE, Aspect.SHEPHERD, "ratpack_hunt", "feather_fan", "hearth_feast", FusionStyle.DASH);
        WeaponEvolutionSkillPool.add(f, Aspect.CHEESE, Aspect.ARC, "static_cheese_moon", "orbital_gears", "static_lattice", FusionStyle.CHAIN);
        WeaponEvolutionSkillPool.add(f, Aspect.CHEESE, Aspect.COMPOST, "molded_feast", "spore_eruption", "hearth_feast", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.CHEESE, Aspect.APPLE, "orchard_cheeseboard", "golden_harvest", "orchard_aegis", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.CHEESE, Aspect.TIDE, "brined_moon", "gilded_verdict", "undertow_snare", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.SHEPHERD, Aspect.ARC, "thunderpack", "ion_rail", "magnetic_storm", FusionStyle.CHAIN);
        WeaponEvolutionSkillPool.add(f, Aspect.SHEPHERD, Aspect.COMPOST, "feral_spores", "briar_procession", "rotting_requiem", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.SHEPHERD, Aspect.APPLE, "orchard_guardian", "wingstep", "orchard_aegis", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.SHEPHERD, Aspect.TIDE, "sea_wolf_charge", "riptide_corridor", "four_tides", FusionStyle.DASH);
        WeaponEvolutionSkillPool.add(f, Aspect.ARC, Aspect.COMPOST, "bio_capacitor", "capacitor_overload", "prismatic_blight", FusionStyle.CHAIN);
        WeaponEvolutionSkillPool.add(f, Aspect.ARC, Aspect.APPLE, "orchard_dynamo", "prism_battery", "solar_orchard", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.ARC, Aspect.TIDE, "stormtide_rail", "ion_rail", "magnetic_storm", FusionStyle.BEAM);
        WeaponEvolutionSkillPool.add(f, Aspect.COMPOST, Aspect.APPLE, "living_orchard", "living_mulch", "solar_orchard", FusionStyle.WARD);
        WeaponEvolutionSkillPool.add(f, Aspect.COMPOST, Aspect.TIDE, "brine_blight", "virulent_harvest", "abyssal_geyser", FusionStyle.VORTEX);
        WeaponEvolutionSkillPool.add(f, Aspect.APPLE, Aspect.TIDE, "tidal_orchard", "tidal_lotus", "golden_harvest", FusionStyle.WARD);
        if (f.size() != 55) {
            throw new IllegalStateException("Expected 55 weapon evolution fusions, got " + f.size());
        }
        FUSIONS = Collections.unmodifiableMap(f);
        EnumMap<Aspect, Map> matrix = new EnumMap<Aspect, Map>(Aspect.class);
        for (Aspect row2 : Aspect.values()) {
            matrix.put(row2, new EnumMap(Aspect.class));
        }
        for (Fusion fusion : f.values()) {
            ((Map)matrix.get((Object)fusion.pair.first)).put(fusion.pair.second, fusion);
            ((Map)matrix.get((Object)fusion.pair.second)).put(fusion.pair.first, fusion);
        }
        EnumMap immutable = new EnumMap(Aspect.class);
        matrix.forEach((aspect, row) -> immutable.put((Aspect)((Object)aspect), Collections.unmodifiableMap(row)));
        MATRIX = Collections.unmodifiableMap(immutable);
    }

    public record Pair(Aspect first, Aspect second) {
        public Pair {
            if (first == null || second == null) {
                throw new IllegalArgumentException("Evolution paths cannot be null");
            }
            if (first.ordinal() > second.ordinal()) {
                Aspect swap = first;
                first = second;
                second = swap;
            }
        }

        public String id() {
            return this.first.id + "_" + this.second.id;
        }
    }

    public static enum Aspect {
        SUGAR("sugar", "statue_sugar_queen", BaseStyle.CONE, 14, 6.0, 3.0, 2.0, 1.15, "sugar_hail", "royal_shatter"),
        MEAT("meat", "statue_butcher", BaseStyle.NOVA, 14, 4.25, 4.25, 4.0, 1.45, "flesh_bloom", "blood_verdict"),
        TEA("tea", "statue_saya", BaseStyle.BURST, 13, 9.0, 3.25, 2.0, 1.15, "briar_procession", "camellia_rebirth"),
        WINE("wine", "statue_wine_barrel", BaseStyle.BURST, 15, 9.0, 3.5, 3.0, 1.25, "vintage_vortex", "vintage_minefield"),
        CHEESE("cheese", "statue_rat_swarm", BaseStyle.CONE, 12, 5.5, 3.0, 2.0, 1.3, "gilded_verdict", "golden_harvest"),
        SHEPHERD("shepherd", "statue_benger", BaseStyle.DASH, 12, 7.0, 3.0, 3.0, 1.35, "feather_fan", "wingstep"),
        ARC("arc", "statue_defect", BaseStyle.CHAIN, 14, 11.0, 4.5, 4.0, 1.15, "ion_rail", "magnetic_storm"),
        COMPOST("compost", "statue_compost", BaseStyle.BURST, 15, 9.0, 3.75, 2.0, 1.05, "spore_eruption", "rotting_requiem"),
        APPLE("apple", "statue_apple", BaseStyle.SELF, 18, 0.0, 0.0, 0.0, 0.0, "solar_orchard", "orchard_aegis"),
        TIDE("tide", "statue_tide_fish", BaseStyle.NOVA, 16, 4.5, 4.5, 2.0, 1.1, "four_tides", "tidal_lotus");

        public final String id;
        public final String statueId;
        public final BaseStyle style;
        public final int cooldownTicks;
        public final double range;
        public final double radius;
        public final double flatDamage;
        public final double attackMultiplier;
        public final String visual;
        public final String ultimateVisual;

        private Aspect(String id, String statueId, BaseStyle style, int cooldownSeconds, double range, double radius, double flatDamage, double attackMultiplier, String visual, String ultimateVisual) {
            this.id = id;
            this.statueId = statueId;
            this.style = style;
            this.cooldownTicks = cooldownSeconds * 20;
            this.range = range;
            this.radius = radius;
            this.flatDamage = flatDamage;
            this.attackMultiplier = attackMultiplier;
            this.visual = visual;
            this.ultimateVisual = ultimateVisual;
        }

        public String translationKey() {
            return "skill.ftboss.evolution.base." + this.id;
        }

        public String descriptionKey() {
            return this.translationKey() + ".description";
        }

        public String pathTranslationKey() {
            return "path.ftboss.evolution." + this.id;
        }

        public static Aspect fromId(String value) {
            if (value == null || value.isBlank()) {
                return null;
            }
            String normalized = value.toLowerCase(Locale.ROOT);
            int separator = normalized.indexOf(58);
            if (separator >= 0) {
                normalized = normalized.substring(separator + 1);
            }
            if (normalized.startsWith("statue_")) {
                normalized = normalized.substring("statue_".length());
            }
            return switch (normalized) {
                case "sugar", "sugar_queen", "gingerbread" -> SUGAR;
                case "meat", "butcher" -> MEAT;
                case "tea", "saya", "tea_tree" -> TEA;
                case "wine", "wine_barrel", "barrel" -> WINE;
                case "cheese", "rat", "rat_swarm" -> CHEESE;
                case "shepherd", "benger", "dog" -> SHEPHERD;
                case "arc", "defect", "defect_robot" -> ARC;
                case "compost" -> COMPOST;
                case "apple", "apple_boss" -> APPLE;
                case "tide", "tide_fish", "fish" -> TIDE;
                default -> null;
            };
        }
    }

    public static enum FusionStyle {
        BURST,
        NOVA,
        CHAIN,
        DASH,
        WARD,
        VORTEX,
        BEAM;

    }

    public record Fusion(Pair pair, String id, String primaryVisual, String secondaryVisual, FusionStyle secondaryStyle, int primaryCooldownTicks, int secondaryCooldownTicks) {
        public String primaryTranslationKey() {
            return "skill.ftboss.evolution.fusion." + this.id + ".primary";
        }

        public String secondaryTranslationKey() {
            return "skill.ftboss.evolution.fusion." + this.id + ".secondary";
        }

        public String styleDescriptionKey() {
            return "tooltip.ftboss.evolution.style." + this.secondaryStyle.name().toLowerCase(Locale.ROOT);
        }
    }

    public static enum BaseStyle {
        SELF,
        CONE,
        NOVA,
        BURST,
        CHAIN,
        DASH;

    }
}

