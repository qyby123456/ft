package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftdungeonmobs.DungeonEncounterPool.Rank;
import net.minecraftforge.common.ForgeConfigSpec;

public final class DungeonSpawnConfig {
    public static final String FILE_NAME = "ftdungeonmobs-spawns.toml";
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.IntValue ACTIVATION_RADIUS;
    private static final ForgeConfigSpec.IntValue MINIMUM_GROUP;
    private static final ForgeConfigSpec.IntValue MAXIMUM_GROUP;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> NORMAL_POOL;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> ELITE_POOL;
    private static final List<ForgeConfigSpec.IntValue> POINT_COUNTS = new ArrayList<>();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("One-shot floor encounters in installed FT dungeon rooms. Changes affect newly generated rooms/rounds.")
                .push("spawnPoints");
        ENABLED = builder.define("enabled", true);
        ACTIVATION_RADIUS = builder.comment("Survival/adventure players must be inside the room and have line of sight.")
                .defineInRange("activationRadius", 6, 2, 16);
        MINIMUM_GROUP = builder.comment("The fixed group is rolled once when its point is generated, never on activation or retry.")
                .defineInRange("minimumMobsPerPoint", 1, 1, 8);
        MAXIMUM_GROUP = builder.comment("Set both group limits to 1 for single-monster points; a reversed range is normalized.")
                .defineInRange("maximumMobsPerPoint", 3, 1, 8);
        NORMAL_POOL = builder.comment("Small rooms only. Namespaced IDs from the ordinary-monster allowlist; missing optional mods are skipped.",
                        "Replaces the pre-0.20 mobPool option. Bosses, pets and water-only creatures are never eligible.")
                .defineList("normalMobPool", DungeonEncounterPool.defaults(Rank.NORMAL), value -> valid(value, Rank.NORMAL));
        ELITE_POOL = builder.comment("Medium rooms and large/challenge rooms only. Cataclysm minibosses plus vanilla elites.",
                        "An empty/unavailable pool falls back to available defaults of the SAME rank, never to ordinary monsters.")
                .defineList("eliteMobPool", DungeonEncounterPool.defaults(Rank.ELITE), value -> valid(value, Rank.ELITE));
        builder.comment("Desired points per room at each locked entrance-brazier tier. Higher tiers never use a lower target.",
                "Small or obstructed rooms may have fewer points; unsafe terrain is never overwritten.")
                .push("pointsPerRoom");
        for (int tier = 1; tier <= 5; tier++) {
            POINT_COUNTS.add(builder.defineInRange("tier" + tier, tier * 2, 1, 32));
        }
        builder.pop(2);
        SPEC = builder.build();
    }

    private DungeonSpawnConfig() {}

    public static boolean enabled() {
        return ENABLED.get();
    }

    public static int activationRadius() {
        return ACTIVATION_RADIUS.get();
    }

    public static int pointCount(int tier) {
        DungeonDifficultyConfig.validateTier(tier);
        int count = 0;
        for (int index = 0; index < tier; index++) {
            count = Math.max(count, POINT_COUNTS.get(index).get());
        }
        return count;
    }

    public static List<? extends String> mobPool(Rank rank) {
        return (rank == Rank.ELITE ? ELITE_POOL : NORMAL_POOL).get();
    }

    private static boolean valid(Object value, Rank rank) {
        return value instanceof String identity && DungeonEncounterPool.rank(DungeonEncounterPool.identity(identity)) == rank;
    }

    public static int groupSize(net.minecraft.util.RandomSource random) {
        int minimum = Math.min(MINIMUM_GROUP.get(), MAXIMUM_GROUP.get());
        int maximum = Math.max(MINIMUM_GROUP.get(), MAXIMUM_GROUP.get());
        return minimum + random.nextInt(maximum - minimum + 1);
    }
}
