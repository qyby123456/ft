package cn.farmingtales.qysevent;

import java.util.Locale;

/**
 * Everyday weather labels. Several entries intentionally have no gameplay
 * modifier; they add variety while mapping onto Minecraft's clear/rain states.
 */
public enum OrdinaryWeatherType {
    CLEAR("clear", "晴天", false, false, false, "阳光充足，短期内小幅增产。"),
    RAIN("rain", "雨天", true, false, false, "普通降雨，短期内促进作物生长。"),
    THUNDER("thunder", "原版雷雨", true, true, false, "原版雷暴，短期内小幅促进作物生长。"),
    CLOUDY("cloudy", "多云", false, false, true, "云层增多，无额外效果。"),
    OVERCAST("overcast", "阴天", false, false, true, "天色阴沉，无额外效果。"),
    DRIZZLE("drizzle", "毛毛雨", true, false, true, "细小雨滴持续落下，无额外效果。"),
    BREEZE("breeze", "微风", false, false, true, "温和微风吹过，无额外效果。"),
    SUNSHOWER("sunshower", "太阳雨", true, false, true, "短暂的晴雨交错，无额外效果。");

    private final String id;
    private final String chineseName;
    private final boolean rain;
    private final boolean thunder;
    private final boolean neutral;
    private final String description;

    OrdinaryWeatherType(String id, String chineseName, boolean rain, boolean thunder, boolean neutral, String description) {
        this.id = id;
        this.chineseName = chineseName;
        this.rain = rain;
        this.thunder = thunder;
        this.neutral = neutral;
        this.description = description;
    }

    public String id() { return id; }
    public String chineseName() { return chineseName; }
    public boolean rains() { return rain; }
    public boolean thunders() { return thunder; }
    public boolean neutral() { return neutral; }
    public String description() { return description; }

    public int precipitationColor() {
        return switch (this) {
            case RAIN -> 0xA9C7E8;
            case THUNDER -> 0x7085A8;
            case DRIZZLE -> 0xC6DDEA;
            case SUNSHOWER -> 0xE2CF86;
            default -> 0xFFFFFF;
        };
    }

    public static OrdinaryWeatherType byId(String value) {
        OrdinaryWeatherType found = find(value);
        return found == null ? CLEAR : found;
    }

    public static OrdinaryWeatherType find(String value) {
        if (value == null) return null;
        String normalized = value.toLowerCase(Locale.ROOT);
        for (OrdinaryWeatherType type : values()) {
            if (type.id.equals(normalized) || type.name().toLowerCase(Locale.ROOT).equals(normalized)) return type;
        }
        return null;
    }
}
