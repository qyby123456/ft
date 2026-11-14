/*
 * Decompiled with CFR 0.152.
 */
package net.farmingtales.ftboss.util;

import java.util.List;
import java.util.Map;

public final class BossSpellRules {
    public static final int MAX_LEVEL = 1;
    public static final int FULL_POWER_LEVEL = 5;
    public static final int CAST_TICKS = 32;
    public static final double RANGE = 24.0;
    public static final double RADIUS = 4.0;
    public static final int MAX_TARGETS = 16;
    private static final Map<String, List<Kind>> PAIRS = Map.of("ftboss:sugar_queen", List.of(Kind.FROST, Kind.FROST_LANCE, Kind.FROST_HAIL, Kind.FROST_PRISM, Kind.FROST_SHATTER), "ftboss:tea_tree", List.of(Kind.TEA, Kind.TEA_SANCTUARY, Kind.TEA_THORNS, Kind.TEA_FLESH, Kind.TEA_POLLEN, Kind.TEA_SUTURE, Kind.TEA_RENEWAL), "ftboss:wine_barrel", List.of(Kind.WINE, Kind.WINE_BOMB, Kind.WINE_BREATH, Kind.WINE_TOAST, Kind.WINE_FUSE), "ftboss:defect_core", List.of(Kind.DEFECT, Kind.DEFECT_PULSE, Kind.DEFECT_RAIL, Kind.DEFECT_STORM, Kind.DEFECT_REBOOT), "ftboss:compost_boss", List.of(Kind.COMPOST, Kind.COMPOST_REAP, Kind.COMPOST_ERUPTION, Kind.COMPOST_ROT, Kind.COMPOST_MULCH), "ftboss:apple_boss", List.of(Kind.APPLE, Kind.APPLE_AEGIS, Kind.APPLE_ORBIT, Kind.APPLE_SEED, Kind.APPLE_HARVEST), "ftboss:tide_fish_boss", List.of(Kind.TIDE, Kind.TIDE_GEYSER, Kind.TIDE_RIP, Kind.TIDE_BUBBLE, Kind.TIDE_SURGE));

    private BossSpellRules() {
    }

    public static Kind forEntity(String id) {
        List<Kind> pair = BossSpellRules.forBoss(id);
        return pair.isEmpty() ? null : pair.get(0);
    }

    public static List<Kind> forBoss(String id) {
        return id == null ? List.of() : PAIRS.getOrDefault(id, List.of());
    }

    public static boolean healing(Kind kind) {
        return kind == Kind.TEA_SANCTUARY || kind == Kind.TEA_RENEWAL || kind == Kind.DEFECT_REBOOT || kind == Kind.COMPOST_MULCH;
    }

    public static boolean shielding(Kind kind) {
        return kind == Kind.APPLE_AEGIS || kind == Kind.FROST_PRISM || kind == Kind.WINE_TOAST || kind == Kind.TIDE_BUBBLE;
    }

    public static boolean support(Kind kind) {
        return BossSpellRules.healing(kind) || BossSpellRules.shielding(kind);
    }

    public static boolean selfCentered(Kind kind) {
        return BossSpellRules.support(kind) || kind == Kind.DEFECT_PULSE || kind == Kind.APPLE_ORBIT;
    }

    public static boolean expanded(Kind kind) {
        return kind.ordinal() >= Kind.FROST_HAIL.ordinal();
    }

    public static int firstPulse(Kind kind) {
        return switch (kind) {
            case Kind.FROST_SHATTER, Kind.TEA_FLESH, Kind.WINE_FUSE, Kind.TIDE_SURGE -> 20;
            case Kind.APPLE_SEED -> 24;
            case Kind.COMPOST_ERUPTION -> 16;
            default -> 0;
        };
    }

    public static int pulseCount(Kind kind) {
        return switch (kind) {
            case Kind.WINE_FUSE, Kind.TIDE_SURGE, Kind.FROST_HAIL, Kind.TEA_THORNS, Kind.TEA_RENEWAL, Kind.TEA_SUTURE, Kind.WINE_BREATH, Kind.DEFECT_STORM, Kind.COMPOST_ROT, Kind.APPLE_ORBIT, Kind.TIDE_RIP -> 3;
            case Kind.TEA_POLLEN -> 4;
            default -> 1;
        };
    }

    public static int pulseInterval(Kind kind) {
        return switch (kind) {
            case Kind.TIDE_SURGE, Kind.TEA_THORNS -> 10;
            case Kind.WINE_FUSE, Kind.WINE_BREATH -> 12;
            default -> 20;
        };
    }

    public static int lastPulse(Kind kind) {
        return BossSpellRules.firstPulse(kind) + (BossSpellRules.pulseCount(kind) - 1) * BossSpellRules.pulseInterval(kind);
    }

    public static int pulseIndex(Kind kind, long age) {
        long elapsed = age - (long)BossSpellRules.firstPulse(kind);
        return elapsed < 0L || age > (long)BossSpellRules.lastPulse(kind) || elapsed % (long)BossSpellRules.pulseInterval(kind) != 0L ? -1 : (int)(elapsed / (long)BossSpellRules.pulseInterval(kind));
    }

    public static int castTicks(Kind kind) {
        return kind == Kind.FROST_LANCE || kind == Kind.DEFECT_PULSE ? 20 : 32;
    }

    public static int delay(Kind kind) {
        return kind == Kind.WINE_BOMB ? 16 : (kind == Kind.TIDE_GEYSER ? 20 : 0);
    }

    public static float bonusDamage(float damage, boolean marked) {
        return damage * (marked ? 1.5f : 1.0f);
    }

    public static int shield(float power) {
        return Math.max(4, Math.min(40, Math.round(power / 4.0f) * 4));
    }

    public static float blastScale(double distance) {
        return (float)Math.max(0.5, Math.min(1.0, 1.0 - distance / 10.0));
    }

    public static int level(int level) {
        return 5;
    }

    public static int chainTargets(int level) {
        return 3 + (BossSpellRules.level(level) - 1) / 2;
    }

    public static float chainScale(int jump) {
        return (float)Math.pow(0.8, Math.max(0, Math.min(4, jump)));
    }

    public static boolean cone(double forward, double sideways) {
        return Double.isFinite(forward) && Double.isFinite(sideways) && forward >= 0.0 && forward <= 12.0 && Math.abs(sideways) <= 1.2 + forward * 0.45;
    }

    public static boolean sporePulse(int elapsed) {
        return elapsed == 0 || elapsed == 20 || elapsed == 40;
    }

    public static enum Kind {
        FROST("sugar_queen", "crystal_coronation", "ice", 18, 80, 18, 10545151),
        TEA("tea_tree", "camellia_blood_bloom", "nature", 14, 85, 20, 15689105),
        WINE("wine_barrel", "vintage_vortex", "evocation", 16, 80, 18, 14259525),
        DEFECT("defect_core", "capacitor_overload", "lightning", 18, 90, 20, 8314111),
        COMPOST("compost_boss", "prismatic_blight", "nature", 8, 100, 24, 11659357),
        APPLE("apple_boss", "gilded_verdict", "holy", 20, 95, 20, 16768100),
        TIDE("tide_fish_boss", "tidal_edict", "ice", 16, 85, 18, 5300169),
        FROST_LANCE("sugar_queen", "frostglass_lance", "ice", 24, 65, 14, 13826559),
        TEA_SANCTUARY("tea_tree", "camellia_sanctuary", "nature", 6, 90, 24, 16762329),
        WINE_BOMB("wine_barrel", "cask_rupture", "fire", 26, 90, 22, 16749391),
        DEFECT_PULSE("defect_core", "repulsor_discharge", "lightning", 20, 75, 16, 11575551),
        COMPOST_REAP("compost_boss", "virulent_harvest", "nature", 18, 75, 16, 14483320),
        APPLE_AEGIS("apple_boss", "orchard_aegis", "holy", 4, 95, 28, 16770728),
        TIDE_GEYSER("tide_fish_boss", "abyssal_geyser", "ice", 28, 90, 20, 7389439),
        FROST_HAIL("sugar_queen", "sugar_hail", "ice", 8, 85, 22, 11132159),
        FROST_PRISM("sugar_queen", "glacial_prism", "ice", 8, 80, 28, 13563647),
        FROST_SHATTER("sugar_queen", "royal_shatter", "ice", 26, 100, 26, 0xAFAAFF),
        TEA_THORNS("tea_tree", "briar_procession", "nature", 10, 75, 18, 8767356),
        TEA_POLLEN("tea_tree", "scarlet_pollen", "nature", 5, 90, 26, 15569584),
        TEA_RENEWAL("tea_tree", "camellia_rebirth", "nature", 4, 95, 32, 11330993),
        TEA_FLESH("tea_tree", "flesh_bloom", "blood", 24, 100, 26, 13194862),
        TEA_SUTURE("tea_tree", "sanguine_suture", "blood", 8, 90, 24, 15688821),
        WINE_BREATH("wine_barrel", "cinder_breath", "fire", 7, 80, 20, 16756818),
        WINE_TOAST("wine_barrel", "intoxicating_toast", "evocation", 6, 75, 30, 15582319),
        WINE_FUSE("wine_barrel", "vintage_minefield", "fire", 18, 100, 28, 16218180),
        DEFECT_RAIL("defect_core", "ion_rail", "lightning", 28, 90, 22, 8383477),
        DEFECT_STORM("defect_core", "static_lattice", "lightning", 7, 95, 26, 11244031),
        DEFECT_REBOOT("defect_core", "emergency_reboot", "lightning", 6, 100, 34, 8581820),
        COMPOST_ERUPTION("compost_boss", "spore_eruption", "nature", 16, 85, 22, 13885033),
        COMPOST_ROT("compost_boss", "rotting_requiem", "nature", 20, 90, 26, 12223952),
        COMPOST_MULCH("compost_boss", "living_mulch", "nature", 5, 80, 30, 9885554),
        APPLE_ORBIT("apple_boss", "solar_orchard", "holy", 8, 85, 24, 16771489),
        APPLE_SEED("apple_boss", "judgment_seed", "holy", 24, 100, 28, 16769124),
        APPLE_HARVEST("apple_boss", "golden_harvest", "holy", 18, 80, 20, 14086291),
        TIDE_RIP("tide_fish_boss", "undertow_snare", "ice", 6, 90, 24, 6796494),
        TIDE_BUBBLE("tide_fish_boss", "abyssal_carapace", "ice", 8, 85, 30, 8446937),
        TIDE_SURGE("tide_fish_boss", "riptide_corridor", "ice", 14, 90, 22, 8830719);

        public final String boss;
        public final String spell;
        public final String school;
        public final int damage;
        public final int mana;
        public final int cooldown;
        public final int color;

        private Kind(String boss, String spell, String school, int damage, int mana, int cooldown, int color) {
            this.boss = boss;
            this.spell = spell;
            this.school = school;
            this.damage = damage;
            this.mana = mana;
            this.cooldown = cooldown;
            this.color = color;
        }
    }
}

