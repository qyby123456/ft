package cn.farmingtales.qysevent;

import java.util.Locale;

public enum WeatherType {
    NONE("none", "平稳天气", Phase.ALL, 1.00D),
    HARVEST_MOON("harvest_moon", "丰收月", Phase.NIGHT, 2.00D),
    CHRISTMAS_EVE("christmas_eve", "圣诞夜", Phase.NIGHT, 1.50D),
    BLOOD_MOON("blood_moon", "血月", Phase.NIGHT, 0.72D),
    SOLAR_ECLIPSE("solar_eclipse", "日食", Phase.DAY, 0.78D),
    METEOR_SHOWER("meteor_shower", "流星雨", Phase.NIGHT, 1.20D),
    SLIME_RAIN("slime_rain", "史莱姆从天而降", Phase.ALL, 0.92D),
    BLIZZARD("blizzard", "暴风雪", Phase.ALL, 0.52D),
    HURRICANE("hurricane", "飓风天", Phase.ALL, 0.42D),
    SANDSTORM("sandstorm", "沙尘暴", Phase.ALL, 0.55D),
    THUNDERSTORM("thunderstorm", "雷雨", Phase.ALL, 0.68D),
    REDSTONE_STORM("redstone_storm", "红石雷暴", Phase.ALL, 0.78D),
    GREEN_RAIN("green_rain", "绿雨", Phase.ALL, 1.75D),

    // Farming Tales original weather concepts.
    AURORA("aurora", "极光夜", Phase.NIGHT, 1.35D),
    HEATWAVE("heatwave", "灼热热浪", Phase.ALL, 0.50D),
    DENSE_FOG("dense_fog", "迷途浓雾", Phase.ALL, 0.88D),
    RAINBOW_RAIN("rainbow_rain", "彩虹雨", Phase.ALL, 1.60D),
    POLLEN_STORM("pollen_storm", "花粉风暴", Phase.ALL, 1.32D),

    // Additional season-locked events.
    SPRING_BLOOM("spring_bloom", "春日花潮", Phase.ALL, 1.45D),
    HAILSTORM("hailstorm", "冰雹", Phase.ALL, 0.72D),
    MONSOON("monsoon", "季风暴雨", Phase.ALL, 1.12D),
    GOLDEN_WIND("golden_wind", "金风送爽", Phase.ALL, 1.30D),
    FROST_NIGHT("frost_night", "霜夜", Phase.NIGHT, 0.82D),
    SILENT_SNOW("silent_snow", "静雪", Phase.ALL, 1.10D),
    COMET_NIGHT("comet_night", "彗星夜", Phase.NIGHT, 1.25D),

    // Expanded weather set.
    ACID_RAIN("acid_rain", "酸雨", Phase.ALL, 0.60D),
    ASH_FALL("ash_fall", "灰烬飘落", Phase.ALL, 0.70D),
    STAR_DEW("star_dew", "星露夜", Phase.NIGHT, 1.50D),
    THUNDERSNOW("thundersnow", "雷雪", Phase.ALL, 0.58D),
    MISTY_DRIZZLE("misty_drizzle", "烟雨", Phase.ALL, 1.28D),
    DRY_LIGHTNING("dry_lightning", "干雷", Phase.ALL, 0.55D),
    FIREFLY_NIGHT("firefly_night", "萤火之夜", Phase.NIGHT, 1.18D),
    BEE_FLIGHT("bee_flight", "蜂舞晴日", Phase.DAY, 1.42D);

    public enum Phase { DAY, NIGHT, ALL }

    private final String id;
    private final String chineseName;
    private final Phase phase;
    private final double yieldMultiplier;

    WeatherType(String id, String chineseName, Phase phase, double yieldMultiplier) {
        this.id = id;
        this.chineseName = chineseName;
        this.phase = phase;
        this.yieldMultiplier = yieldMultiplier;
    }

    public String id() { return id; }
    public String chineseName() { return chineseName; }
    public Phase phase() { return phase; }
    public double yieldMultiplier() { return yieldMultiplier; }

    /** RGB multiplier for vanilla precipitation. No particles are allocated. */
    public int precipitationColor() {
        return switch (this) {
            case CHRISTMAS_EVE -> 0xDCEEFF;
            case SLIME_RAIN -> 0x79C95A;
            case BLIZZARD -> 0xFFFFFF;
            case HURRICANE -> 0x718A9B;
            case THUNDERSTORM -> 0x687DA8;
            case REDSTONE_STORM -> 0xCF3B35;
            case GREEN_RAIN -> 0x58B957;
            case RAINBOW_RAIN -> 0xC56DDB;
            case HAILSTORM -> 0xC4E8F2;
            case MONSOON -> 0x477AB8;
            case SILENT_SNOW -> 0xD4E7F0;
            case ACID_RAIN -> 0xA0D33C;
            case THUNDERSNOW -> 0x83C7DE;
            case MISTY_DRIZZLE -> 0x9CB8C2;
            default -> 0xFFFFFF;
        };
    }

    public boolean isActiveAt(long dayTime) {
        long t = Math.floorMod(dayTime, 24000L);
        return phase == Phase.ALL || (phase == Phase.DAY ? t < 12500L : t >= 12500L);
    }

    public static WeatherType byId(String value) {
        if (value == null) return NONE;
        String normalized = value.toLowerCase(Locale.ROOT);
        for (WeatherType type : values()) {
            if (type.id.equals(normalized) || type.name().toLowerCase(Locale.ROOT).equals(normalized) || type.chineseName.equals(value)) {
                return type;
            }
        }
        return NONE;
    }
}



