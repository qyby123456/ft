package net.farmingtales.ftdungeonmobs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.common.ForgeConfigSpec;

public final class DungeonSpawnConfig {
    public static final String FILE_NAME = "ftdungeonmobs-spawns.toml";
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.IntValue ACTIVATION_RADIUS;
    private static final ForgeConfigSpec.IntValue MINIMUM_GROUP;
    private static final ForgeConfigSpec.IntValue MAXIMUM_GROUP;
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> MOB_POOL;
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
        MOB_POOL = builder.comment("FT skeleton IDs without namespace. skeleton_king is opt-in and needs four blocks of clearance.")
                .defineList("mobPool", List.of("skeleton_warrior", "skeleton_archer", "skeleton_mage"),
                        value -> value instanceof String identity && Arrays.stream(SkeletonVariant.values())
                                .anyMatch(variant -> variant.id().equals(identity)));
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

    public static List<SkeletonVariant> mobPool() {
        return MOB_POOL.get().stream().map(DungeonSpawnConfig::variant).toList();
    }

    public static SkeletonVariant variant(String identity) {
        return Arrays.stream(SkeletonVariant.values()).filter(variant -> variant.id().equals(identity)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown FT spawn-point monster: " + identity));
    }

    public static int groupSize(net.minecraft.util.RandomSource random) {
        int minimum = Math.min(MINIMUM_GROUP.get(), MAXIMUM_GROUP.get());
        int maximum = Math.max(MINIMUM_GROUP.get(), MAXIMUM_GROUP.get());
        return minimum + random.nextInt(maximum - minimum + 1);
    }
}
