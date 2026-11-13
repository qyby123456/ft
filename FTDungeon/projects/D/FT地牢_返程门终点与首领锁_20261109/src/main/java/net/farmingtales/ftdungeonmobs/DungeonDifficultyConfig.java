package net.farmingtales.ftdungeonmobs;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.world.Difficulty;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public final class DungeonDifficultyConfig {
    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 5;
    public static final String FILE_NAME = "ftdungeonmobs-common.toml";
    private static final List<DifficultyProfile> DEFAULT_TIERS = List.of(
            DifficultyProfile.IDENTITY,
            new DifficultyProfile(1.5, 1.2, 2, 0.1, 1.2),
            new DifficultyProfile(2, 1.5, 4, 0.2, 1.5),
            new DifficultyProfile(3, 1.85, 7, 0.35, 1.85),
            new DifficultyProfile(4, 2.25, 10, 0.5, 2.25));
    private static final ForgeConfigSpec.BooleanValue USE_WORLD_DIFFICULTY;
    private static final ForgeConfigSpec.IntValue DEFAULT_TIER;
    private static final ForgeConfigSpec.IntValue ARENA_RADIUS;
    private static final Map<Difficulty, ForgeConfigSpec.IntValue> WORLD_TIERS = new EnumMap<>(Difficulty.class);
    private static final List<ProfileValues> TIERS = new ArrayList<>();
    private static final Map<SkeletonVariant, ProfileValues> MOBS = new EnumMap<>(SkeletonVariant.class);
    public static final ForgeConfigSpec SPEC;
    private static volatile Settings settings = defaults();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Server-authoritative difficulty settings. Use /ftdungeon difficulty reload after editing; reload events apply within 20 ticks.",
                "Only FT Dungeon Mobs are affected. Explicitly assigned tiers are not overwritten by world difficulty.")
                .push("general");
        USE_WORLD_DIFFICULTY = builder.comment("Automatically choose a tier from the vanilla world difficulty.")
                .define("useWorldDifficulty", true);
        DEFAULT_TIER = builder.comment("Tier for automatic mobs when useWorldDifficulty is false.")
                .defineInRange("defaultTier", 1, MIN_TIER, MAX_TIER);
        for (Difficulty difficulty : Difficulty.values()) {
            int tier = defaultWorldTier(difficulty);
            WORLD_TIERS.put(difficulty, builder.defineInRange(difficulty.getKey() + "Tier", tier, MIN_TIER, MAX_TIER));
        }
        builder.pop();
        builder.comment("Arena torches control automatic FT mobs locally. Explicit mob tiers retain priority.")
                .push("arenaTorch");
        ARENA_RADIUS = builder.comment("Spherical influence radius in blocks for existing and newly spawned FT mobs.")
                .defineInRange("radius", 32, 8, 128);
        builder.pop();
        builder.comment("Five independently configurable difficulty tiers. Multipliers affect the complete native attribute.")
                .push("tiers");
        for (int tier = MIN_TIER; tier <= MAX_TIER; tier++) {
            builder.push("tier" + tier);
            TIERS.add(new ProfileValues(builder, DEFAULT_TIERS.get(tier - 1)));
            builder.pop();
        }
        builder.pop();
        builder.comment("Per-monster adjustments, composed with its tier and per-entity overrides.",
                "Health, attack and spell multipliers multiply; armor and magic armor bonuses add.")
                .push("mobs");
        for (SkeletonVariant variant : SkeletonVariant.values()) {
            builder.push(variant.id());
            MOBS.put(variant, new ProfileValues(builder, DifficultyProfile.IDENTITY));
            builder.pop();
        }
        builder.pop();
        SPEC = builder.build();
    }

    private DungeonDifficultyConfig() {}

    public static Settings settings() { return settings; }

    public static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() != SPEC
                || !(event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading)) {
            return;
        }
        bake();
    }

    public static synchronized void reloadFromDisk() {
        ModConfig config = ConfigTracker.INSTANCE.fileMap().get(FILE_NAME);
        if (config == null || config.getSpec() != SPEC || !(config.getConfigData() instanceof CommentedFileConfig fileConfig)) {
            throw new IllegalStateException("FT Dungeon Mobs config has not been loaded yet.");
        }
        fileConfig.load();
        if (!SPEC.isCorrect(fileConfig)) {
            throw new IllegalArgumentException("Difficulty config is incomplete or contains values outside the supported ranges.");
        }
        SPEC.afterReload();
        bake();
    }

    private static synchronized void bake() {
        Map<Difficulty, Integer> worldTiers = new EnumMap<>(Difficulty.class);
        WORLD_TIERS.forEach((difficulty, value) -> worldTiers.put(difficulty, value.get()));
        Map<SkeletonVariant, DifficultyProfile> mobs = new EnumMap<>(SkeletonVariant.class);
        MOBS.forEach((variant, values) -> mobs.put(variant, values.read()));
        settings = new Settings(settings.revision() + 1, USE_WORLD_DIFFICULTY.get(), DEFAULT_TIER.get(), ARENA_RADIUS.get(),
                Map.copyOf(worldTiers), TIERS.stream().map(ProfileValues::read).toList(), Map.copyOf(mobs));
    }

    public static void validateTier(int tier) {
        if (tier < MIN_TIER || tier > MAX_TIER) {
            throw new IllegalArgumentException("Difficulty tier must be between " + MIN_TIER + " and " + MAX_TIER);
        }
    }

    private static int defaultWorldTier(Difficulty difficulty) {
        return switch (difficulty) {
            case PEACEFUL, EASY -> 1;
            case NORMAL -> 2;
            case HARD -> 3;
        };
    }

    private static Settings defaults() {
        Map<Difficulty, Integer> worldTiers = new EnumMap<>(Difficulty.class);
        for (Difficulty difficulty : Difficulty.values()) {
            worldTiers.put(difficulty, defaultWorldTier(difficulty));
        }
        Map<SkeletonVariant, DifficultyProfile> mobs = new EnumMap<>(SkeletonVariant.class);
        for (SkeletonVariant variant : SkeletonVariant.values()) {
            mobs.put(variant, DifficultyProfile.IDENTITY);
        }
        return new Settings(0, true, 1, 32, Map.copyOf(worldTiers), DEFAULT_TIERS, Map.copyOf(mobs));
    }

    public record Settings(long revision, boolean useWorldDifficulty, int defaultTier, int arenaRadius,
                           Map<Difficulty, Integer> worldTiers, List<DifficultyProfile> tiers,
                           Map<SkeletonVariant, DifficultyProfile> mobs) {
        public int automaticTier(Difficulty difficulty) {
            return useWorldDifficulty ? worldTiers.get(difficulty) : defaultTier;
        }

        public DifficultyProfile profile(SkeletonVariant variant, int tier) {
            validateTier(tier);
            return tiers.get(tier - 1).combine(mobs.get(variant));
        }
    }

    private static final class ProfileValues {
        private final ForgeConfigSpec.DoubleValue health;
        private final ForgeConfigSpec.DoubleValue attack;
        private final ForgeConfigSpec.DoubleValue armor;
        private final ForgeConfigSpec.DoubleValue magicArmor;
        private final ForgeConfigSpec.DoubleValue spellPower;

        private ProfileValues(ForgeConfigSpec.Builder builder, DifficultyProfile defaults) {
            health = builder.comment("Maximum-health multiplier. Native attribute limits still apply.")
                    .defineInRange("healthMultiplier", defaults.healthMultiplier(), 0.05, 100);
            attack = builder.comment("Native attack-damage multiplier, also applied once to native arrow base damage.")
                    .defineInRange("attackMultiplier", defaults.attackMultiplier(), 0, 100);
            armor = builder.comment("Added to minecraft:generic.armor. This is armor points, not flat damage reduction.")
                    .defineInRange("armorBonus", defaults.armorBonus(), -30, 30);
            magicArmor = builder.comment("Added to irons_spellbooks:spell_resist (base 1.0), not physical armor.",
                    "Final reduction uses Iron's native school resistance and soft cap, not a second damage hook.")
                    .defineInRange("magicArmorBonus", defaults.magicArmorBonus(), -0.9, 99);
            spellPower = builder.comment("Multiplies irons_spellbooks:spell_power once; spells use their native scaling.")
                    .defineInRange("spellPowerMultiplier", defaults.spellPowerMultiplier(), 0, 100);
        }

        private DifficultyProfile read() {
            return new DifficultyProfile(health.get(), attack.get(), armor.get(), magicArmor.get(), spellPower.get());
        }
    }
}
