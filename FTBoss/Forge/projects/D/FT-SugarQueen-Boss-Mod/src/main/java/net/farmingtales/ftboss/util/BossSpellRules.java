package net.farmingtales.ftboss.util;

import java.util.List;
import java.util.Map;

public final class BossSpellRules {
    public static final int MAX_LEVEL = 1;
    public static final int FULL_POWER_LEVEL = 5;
    public static final int CAST_TICKS = 32;
    public static final double RANGE = 24;
    public static final double RADIUS = 4;
    public static final int MAX_TARGETS = 16;

    public enum Kind {
        FROST("sugar_queen", "crystal_coronation", "ice", 18, 80, 18, 0xA0E7FF),
        TEA("tea_tree", "camellia_blood_bloom", "nature", 14, 85, 20, 0xEF6591),
        WINE("wine_barrel", "vintage_vortex", "evocation", 16, 80, 18, 0xD99545),
        DEFECT("defect_core", "capacitor_overload", "lightning", 18, 90, 20, 0x7EDCFF),
        COMPOST("compost_boss", "prismatic_blight", "nature", 8, 100, 24, 0xB1E85D),
        APPLE("apple_boss", "gilded_verdict", "holy", 20, 95, 20, 0xFFDC64),
        TIDE("tide_fish_boss", "tidal_edict", "ice", 16, 85, 18, 0x50DFC9),
        FROST_LANCE("sugar_queen", "frostglass_lance", "ice", 24, 65, 14, 0xD2F9FF),
        TEA_SANCTUARY("tea_tree", "camellia_sanctuary", "nature", 6, 90, 24, 0xFFC5D9),
        WINE_BOMB("wine_barrel", "cask_rupture", "fire", 26, 90, 22, 0xFF934F),
        DEFECT_PULSE("defect_core", "repulsor_discharge", "lightning", 20, 75, 16, 0xB0A0FF),
        COMPOST_REAP("compost_boss", "virulent_harvest", "nature", 18, 75, 16, 0xDCFF78),
        APPLE_AEGIS("apple_boss", "orchard_aegis", "holy", 4, 95, 28, 0xFFE6A8),
        TIDE_GEYSER("tide_fish_boss", "abyssal_geyser", "ice", 28, 90, 20, 0x70C0FF),
        FROST_HAIL("sugar_queen", "sugar_hail", "ice", 8, 85, 22, 0xA9DCFF),
        FROST_PRISM("sugar_queen", "glacial_prism", "ice", 8, 80, 28, 0xCEF6FF),
        FROST_SHATTER("sugar_queen", "royal_shatter", "ice", 26, 100, 26, 0xAFAAFF),
        TEA_THORNS("tea_tree", "briar_procession", "nature", 10, 75, 18, 0x85C77C),
        TEA_POLLEN("tea_tree", "scarlet_pollen", "nature", 5, 90, 26, 0xED92B0),
        TEA_RENEWAL("tea_tree", "camellia_rebirth", "nature", 4, 95, 32, 0xACE5B1),
        TEA_FLESH("tea_tree", "flesh_bloom", "blood", 24, 100, 26, 0xC9566E),
        TEA_SUTURE("tea_tree", "sanguine_suture", "blood", 8, 90, 24, 0xEF6475),
        WINE_BREATH("wine_barrel", "cinder_breath", "fire", 7, 80, 20, 0xFFB052),
        WINE_TOAST("wine_barrel", "intoxicating_toast", "evocation", 6, 75, 30, 0xEDC46F),
        WINE_FUSE("wine_barrel", "vintage_minefield", "fire", 18, 100, 28, 0xF77844),
        DEFECT_RAIL("defect_core", "ion_rail", "lightning", 28, 90, 22, 0x7FEBF5),
        DEFECT_STORM("defect_core", "static_lattice", "lightning", 7, 95, 26, 0xAB91FF),
        DEFECT_REBOOT("defect_core", "emergency_reboot", "lightning", 6, 100, 34, 0x82F2BC),
        COMPOST_ERUPTION("compost_boss", "spore_eruption", "nature", 16, 85, 22, 0xD3DE69),
        COMPOST_ROT("compost_boss", "rotting_requiem", "nature", 20, 90, 26, 0xBA85D0),
        COMPOST_MULCH("compost_boss", "living_mulch", "nature", 5, 80, 30, 0x96D772),
        APPLE_ORBIT("apple_boss", "solar_orchard", "holy", 8, 85, 24, 0xFFE9A1),
        APPLE_SEED("apple_boss", "judgment_seed", "holy", 24, 100, 28, 0xFFE064),
        APPLE_HARVEST("apple_boss", "golden_harvest", "holy", 18, 80, 20, 0xD6F093),
        TIDE_RIP("tide_fish_boss", "undertow_snare", "ice", 6, 90, 24, 0x67B4CE),
        TIDE_BUBBLE("tide_fish_boss", "abyssal_carapace", "ice", 8, 85, 30, 0x80E3D9),
        TIDE_SURGE("tide_fish_boss", "riptide_corridor", "ice", 14, 90, 22, 0x86BEFF);

        public final String boss;
        public final String spell;
        public final String school;
        public final int damage;
        public final int mana;
        public final int cooldown;
        public final int color;

        Kind(String boss, String spell, String school, int damage, int mana, int cooldown, int color) {
            this.boss = boss;
            this.spell = spell;
            this.school = school;
            this.damage = damage;
            this.mana = mana;
            this.cooldown = cooldown;
            this.color = color;
        }
    }

    private static final Map<String, List<Kind>> PAIRS = Map.of(
            "ftboss:sugar_queen", List.of(Kind.FROST, Kind.FROST_LANCE, Kind.FROST_HAIL, Kind.FROST_PRISM, Kind.FROST_SHATTER),
            "ftboss:tea_tree", List.of(Kind.TEA, Kind.TEA_SANCTUARY, Kind.TEA_THORNS, Kind.TEA_FLESH, Kind.TEA_POLLEN, Kind.TEA_SUTURE, Kind.TEA_RENEWAL),
            "ftboss:wine_barrel", List.of(Kind.WINE, Kind.WINE_BOMB, Kind.WINE_BREATH, Kind.WINE_TOAST, Kind.WINE_FUSE),
            "ftboss:defect_core", List.of(Kind.DEFECT, Kind.DEFECT_PULSE, Kind.DEFECT_RAIL, Kind.DEFECT_STORM, Kind.DEFECT_REBOOT),
            "ftboss:compost_boss", List.of(Kind.COMPOST, Kind.COMPOST_REAP, Kind.COMPOST_ERUPTION, Kind.COMPOST_ROT, Kind.COMPOST_MULCH),
            "ftboss:apple_boss", List.of(Kind.APPLE, Kind.APPLE_AEGIS, Kind.APPLE_ORBIT, Kind.APPLE_SEED, Kind.APPLE_HARVEST),
            "ftboss:tide_fish_boss", List.of(Kind.TIDE, Kind.TIDE_GEYSER, Kind.TIDE_RIP, Kind.TIDE_BUBBLE, Kind.TIDE_SURGE));

    private BossSpellRules() {}

    public static Kind forEntity(String id) {
        List<Kind> pair = forBoss(id);
        return pair.isEmpty() ? null : pair.get(0);
    }

    public static List<Kind> forBoss(String id) { return id == null ? List.of() : PAIRS.getOrDefault(id, List.of()); }

    public static boolean healing(Kind kind) {
        return kind == Kind.TEA_SANCTUARY || kind == Kind.TEA_RENEWAL || kind == Kind.DEFECT_REBOOT || kind == Kind.COMPOST_MULCH;
    }

    public static boolean shielding(Kind kind) {
        return kind == Kind.APPLE_AEGIS || kind == Kind.FROST_PRISM || kind == Kind.WINE_TOAST || kind == Kind.TIDE_BUBBLE;
    }

    public static boolean support(Kind kind) { return healing(kind) || shielding(kind); }

    public static boolean selfCentered(Kind kind) { return support(kind) || kind == Kind.DEFECT_PULSE || kind == Kind.APPLE_ORBIT; }

    public static boolean expanded(Kind kind) { return kind.ordinal() >= Kind.FROST_HAIL.ordinal(); }

    public static int firstPulse(Kind kind) {
        return switch (kind) {
            case FROST_SHATTER, TEA_FLESH, WINE_FUSE, TIDE_SURGE -> 20;
            case APPLE_SEED -> 24;
            case COMPOST_ERUPTION -> 16;
            default -> 0;
        };
    }

    public static int pulseCount(Kind kind) {
        return switch (kind) {
            case FROST_HAIL, TEA_THORNS, TEA_RENEWAL, TEA_SUTURE, WINE_BREATH,
                    WINE_FUSE, DEFECT_STORM, COMPOST_ROT, APPLE_ORBIT, TIDE_RIP, TIDE_SURGE -> 3;
            case TEA_POLLEN -> 4;
            default -> 1;
        };
    }

    public static int pulseInterval(Kind kind) {
        return switch (kind) {
            case TEA_THORNS, TIDE_SURGE -> 10;
            case WINE_BREATH, WINE_FUSE -> 12;
            default -> 20;
        };
    }

    public static int lastPulse(Kind kind) { return firstPulse(kind) + (pulseCount(kind) - 1) * pulseInterval(kind); }

    public static int pulseIndex(Kind kind, long age) {
        long elapsed = age - firstPulse(kind);
        return elapsed < 0 || age > lastPulse(kind) || elapsed % pulseInterval(kind) != 0
                ? -1 : (int) (elapsed / pulseInterval(kind));
    }

    public static int castTicks(Kind kind) { return kind == Kind.FROST_LANCE || kind == Kind.DEFECT_PULSE ? 20 : CAST_TICKS; }

    public static int delay(Kind kind) { return kind == Kind.WINE_BOMB ? 16 : kind == Kind.TIDE_GEYSER ? 20 : 0; }

    public static float bonusDamage(float damage, boolean marked) { return damage * (marked ? 1.5F : 1); }

    public static int shield(float power) { return Math.max(4, Math.min(40, Math.round(power / 4) * 4)); }

    public static float blastScale(double distance) { return (float) Math.max(0.5, Math.min(1, 1 - distance / 10)); }

    public static int level(int level) { return FULL_POWER_LEVEL; }

    public static int chainTargets(int level) { return 3 + (level(level) - 1) / 2; }

    public static float chainScale(int jump) { return (float) Math.pow(0.8, Math.max(0, Math.min(4, jump))); }

    public static boolean cone(double forward, double sideways) {
        return Double.isFinite(forward) && Double.isFinite(sideways) && forward >= 0 && forward <= 12
                && Math.abs(sideways) <= 1.2 + forward * 0.45;
    }

    public static boolean sporePulse(int elapsed) { return elapsed == 0 || elapsed == 20 || elapsed == 40; }
}
