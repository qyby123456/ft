package net.farmingtales.ftdungeonchallenges;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftdungeonmobs.DungeonDifficultyConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.config.ModConfigEvent;

public final class ArenaChallengeConfig {
    public static final String FILE_NAME = "ftdungeonmobs-challenges.toml";
    public static final ForgeConfigSpec SPEC;
    private static final ForgeConfigSpec.BooleanValue ENABLED;
    private static final ForgeConfigSpec.IntValue PREPARATION_SECONDS;
    private static final ForgeConfigSpec.IntValue INTERMISSION_SECONDS;
    private static final ForgeConfigSpec.IntValue WAVE_SECONDS;
    private static final ForgeConfigSpec.IntValue SPAWN_SECONDS;
    private static final ForgeConfigSpec.IntValue ABSENCE_SECONDS;
    private static final ForgeConfigSpec.IntValue COOLDOWN_SECONDS;
    private static final ForgeConfigSpec.IntValue MAX_CHALLENGES;
    private static final ForgeConfigSpec.IntValue MAX_PARTICIPANTS;
    private static final ForgeConfigSpec.IntValue MAX_MOBS;
    private static final ForgeConfigSpec.IntValue MOBS_PER_WAVE;
    private static final ForgeConfigSpec.IntValue MOBS_PER_PLAYER;
    private static final ForgeConfigSpec.IntValue SPAWN_BATCH;
    private static final ForgeConfigSpec.IntValue EXPERIENCE_PER_TIER;
    private static final List<TierValues> TIERS = new ArrayList<>();
    private static volatile Settings settings = defaults();

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.comment("Server-only arena wave challenges. No Majrusz dependency or automatic world invasions.",
                "Empty both hands and sneak-right-click a torch to start or join before wave one.")
                .push("challenge");
        ENABLED = builder.define("enabled", true);
        PREPARATION_SECONDS = builder.defineInRange("preparationSeconds", 15, 1, 300);
        INTERMISSION_SECONDS = builder.defineInRange("intermissionSeconds", 8, 1, 120);
        WAVE_SECONDS = builder.defineInRange("waveTimeoutSeconds", 300, 10, 3600);
        SPAWN_SECONDS = builder.comment("Fail rather than force-load chunks or spawn inside walls when no safe floor is available.")
                .defineInRange("spawnTimeoutSeconds", 30, 1, 120);
        ABSENCE_SECONDS = builder.defineInRange("absenceGraceSeconds", 30, 1, 300);
        COOLDOWN_SECONDS = builder.comment("Persistent per-torch AND per-participant cooldown, including interrupted challenges.")
                .defineInRange("cooldownSeconds", 180, 0, 86400);
        MAX_CHALLENGES = builder.comment("Server-wide active challenge limit across all dimensions.")
                .defineInRange("maxConcurrentChallenges", 3, 1, 16);
        MAX_PARTICIPANTS = builder.defineInRange("maxParticipants", 4, 1, 16);
        MAX_MOBS = builder.defineInRange("maxMobsPerWave", 24, 2, 64);
        MOBS_PER_WAVE = builder.defineInRange("extraMobsPerWave", 2, 0, 8);
        MOBS_PER_PLAYER = builder.defineInRange("extraMobsPerAdditionalPlayer", 2, 0, 8);
        SPAWN_BATCH = builder.comment("Maximum spawn attempts per challenge per second; each attempt checks at most 24 columns.")
                .defineInRange("spawnBatchSize", 4, 1, 8);
        EXPERIENCE_PER_TIER = builder.comment("Victory experience points per tier, paid only to surviving present participants.")
                .defineInRange("victoryExperiencePerTier", 10, 0, 1000);
        builder.pop().push("tiers");
        for (int tier = 1; tier <= 5; tier++) {
            builder.push("tier" + tier);
            TIERS.add(new TierValues(builder, defaultTier(tier)));
            builder.pop();
        }
        builder.pop();
        SPEC = builder.build();
    }

    private ArenaChallengeConfig() {}

    public static Settings settings() { return settings; }

    public static void onConfig(ModConfigEvent event) {
        if (event.getConfig().getSpec() == SPEC
                && (event instanceof ModConfigEvent.Loading || event instanceof ModConfigEvent.Reloading)) {
            bake();
        }
    }

    public static synchronized void reloadFromDisk() {
        ModConfig config = ConfigTracker.INSTANCE.fileMap().get(FILE_NAME);
        if (config == null || config.getSpec() != SPEC || !(config.getConfigData() instanceof CommentedFileConfig file)) {
            throw new IllegalStateException("Arena challenge config has not been loaded yet.");
        }
        file.load();
        if (!SPEC.isCorrect(file)) {
            throw new IllegalArgumentException("Arena challenge config contains invalid or missing settings.");
        }
        SPEC.afterReload();
        bake();
    }

    private static synchronized void bake() {
        settings = new Settings(ENABLED.get(), PREPARATION_SECONDS.get(), INTERMISSION_SECONDS.get(),
                WAVE_SECONDS.get(), SPAWN_SECONDS.get(), ABSENCE_SECONDS.get(), COOLDOWN_SECONDS.get(),
                MAX_CHALLENGES.get(), MAX_PARTICIPANTS.get(), MAX_MOBS.get(), MOBS_PER_WAVE.get(),
                MOBS_PER_PLAYER.get(), SPAWN_BATCH.get(), EXPERIENCE_PER_TIER.get(),
                TIERS.stream().map(TierValues::read).toList());
    }

    private static Settings defaults() {
        List<Tier> tiers = new ArrayList<>();
        for (int tier = 1; tier <= 5; tier++) {
            tiers.add(defaultTier(tier));
        }
        return new Settings(true, 15, 8, 300, 30, 30, 180, 3, 4, 24, 2, 2, 4, 10, List.copyOf(tiers));
    }

    private static Tier defaultTier(int tier) {
        return new Tier(tier + 2, tier + 3, 4, 2, tier == 1 ? 0 : tier - 1, tier > 1);
    }

    public record Settings(boolean enabled, int preparationSeconds, int intermissionSeconds, int waveSeconds,
                           int spawnSeconds, int absenceSeconds, int cooldownSeconds, int maxChallenges,
                           int maxParticipants, int maxMobs, int mobsPerWave, int mobsPerPlayer,
                           int spawnBatch, int experiencePerTier, List<Tier> tiers) {
        public Tier tier(int tier) {
            DungeonDifficultyConfig.validateTier(tier);
            return tiers.get(tier - 1);
        }
    }

    public record Tier(int waves, int baseMobs, int warriorWeight, int archerWeight, int mageWeight, boolean finalBoss) {
        public int mobCount(int wave, int participants, Settings settings) {
            return Math.min(settings.maxMobs(), baseMobs + Math.max(0, wave - 1) * settings.mobsPerWave()
                    + Math.max(0, participants - 1) * settings.mobsPerPlayer());
        }
    }

    private static final class TierValues {
        private final ForgeConfigSpec.IntValue waves;
        private final ForgeConfigSpec.IntValue mobs;
        private final ForgeConfigSpec.IntValue warriors;
        private final ForgeConfigSpec.IntValue archers;
        private final ForgeConfigSpec.IntValue mages;
        private final ForgeConfigSpec.BooleanValue boss;

        private TierValues(ForgeConfigSpec.Builder builder, Tier defaults) {
            waves = builder.defineInRange("waves", defaults.waves(), 1, 20);
            mobs = builder.defineInRange("baseMobs", defaults.baseMobs(), 2, 64);
            warriors = builder.defineInRange("warriorWeight", defaults.warriorWeight(), 1, 100);
            archers = builder.defineInRange("archerWeight", defaults.archerWeight(), 0, 100);
            mages = builder.defineInRange("mageWeight", defaults.mageWeight(), 0, 100);
            boss = builder.comment("The last wave replaces one regular enemy with the existing FT Skeleton King.")
                    .define("finalBoss", defaults.finalBoss());
        }

        private Tier read() {
            return new Tier(waves.get(), mobs.get(), warriors.get(), archers.get(), mages.get(), boss.get());
        }
    }
}
