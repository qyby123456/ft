package cn.farmingtales.qysevent;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class WeatherConfig {
    public static final ModConfigSpec SPEC;
    public static final ModConfigSpec.BooleanValue CONTROL_VANILLA_WEATHER;
    public static final ModConfigSpec.BooleanValue ANNOUNCE_SOLAR_TERMS;
    public static final ModConfigSpec.DoubleValue SPECIAL_EVENT_CHANCE;
    public static final ModConfigSpec.IntValue SPECIAL_EVENT_COOLDOWN_DAYS;
    public static final ModConfigSpec.BooleanValue DESTRUCTIVE_WEATHER;
    public static final ModConfigSpec.BooleanValue REQUIRE_MOB_GRIEFING;
    public static final ModConfigSpec.IntValue TERRAIN_SAMPLES_PER_PLAYER;
    public static final ModConfigSpec.BooleanValue CROP_GROWTH_EFFECTS;
    public static final ModConfigSpec.BooleanValue CROP_YIELD_EFFECTS;
    public static final ModConfigSpec.IntValue DIMINISHING_STREAK_START;
    public static final ModConfigSpec.IntValue SLIME_CAP_PER_PLAYER;
    public static final ModConfigSpec.DoubleValue METEOR_MINERAL_CHANCE;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();
        b.push("general");
        CONTROL_VANILLA_WEATHER = b.comment("Let the 24 solar terms choose season-aware ordinary weather each dawn.")
                .define("controlVanillaWeather", true);
        ANNOUNCE_SOLAR_TERMS = b.comment("Announce each new solar term to all players.")
                .define("announceSolarTerms", true);
        SPECIAL_EVENT_CHANCE = b.comment("Daily chance to roll a special weather event after strict season eligibility is checked. Default 15% keeps ordinary weather dominant.")
                .defineInRange("specialEventChance", 0.15D, 0.0D, 1.0D);
        SPECIAL_EVENT_COOLDOWN_DAYS = b.comment("Minimum ordinary days after a random special event. Default 2 prevents special-weather clusters.")
                .defineInRange("specialEventCooldownDays", 2, 0, 30);
        b.pop();

        b.push("damage");
        DESTRUCTIVE_WEATHER = b.comment("Allow storms to break glass/crops and damage farmland/terrain.")
                .define("destructiveWeather", true);
        REQUIRE_MOB_GRIEFING = b.comment("Only damage blocks when the mobGriefing gamerule is enabled.")
                .define("requireMobGriefing", true);
        TERRAIN_SAMPLES_PER_PLAYER = b.comment("Random nearby blocks checked per destructive pulse per player. Lower this for servers.")
                .defineInRange("terrainSamplesPerPlayer", 12, 0, 64);
        SLIME_CAP_PER_PLAYER = b.comment("Approximate local slime cap during slime rain.")
                .defineInRange("slimeCapPerPlayer", 16, 0, 64);
        METEOR_MINERAL_CHANCE = b.comment("Very small chance that a meteor impact also drops a falling ore block.")
                .defineInRange("meteorMineralChance", 0.06D, 0.0D, 1.0D);
        b.pop();

        b.push("farming");
        CROP_GROWTH_EFFECTS = b.comment("Apply weather bonuses/penalties to crop growth.")
                .define("cropGrowthEffects", true);
        CROP_YIELD_EFFECTS = b.comment("Apply weather bonuses/penalties to mature crop loot.")
                .define("cropYieldEffects", true);
        DIMINISHING_STREAK_START = b.comment("Consecutive identical ordinary-weather days before the bonus turns into a penalty.")
                .defineInRange("diminishingStreakStart", 3, 2, 10);
        b.pop();
        SPEC = b.build();
    }

    private WeatherConfig() {}
}






