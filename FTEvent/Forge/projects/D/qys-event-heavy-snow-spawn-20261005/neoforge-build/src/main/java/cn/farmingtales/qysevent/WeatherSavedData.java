package cn.farmingtales.qysevent;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

public final class WeatherSavedData extends SavedData {
    private static final String DATA_NAME = "solarweather_state";

    public long lastProcessedDay = Long.MIN_VALUE;
    public int lastTerm = -1;
    public WeatherType activeEvent = WeatherType.NONE;
    public long activeEventDay = Long.MIN_VALUE;
    public String ordinaryWeather = "clear";
    public int ordinaryWeatherStreak = 0;
    public long cooldownUntilDay = Long.MIN_VALUE;

    // A forecaster locks tomorrow's result so the paid prediction is always accurate.
    public long forecastDay = Long.MIN_VALUE;
    public int forecastTerm = -1;
    public String forecastOrdinaryWeather = "clear";
    public WeatherType forecastEvent = WeatherType.NONE;

    private static final SavedData.Factory<WeatherSavedData> FACTORY =
            new SavedData.Factory<>(WeatherSavedData::new, (tag, registries) -> load(tag));

    public static WeatherSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(FACTORY, DATA_NAME);
    }

    public static WeatherSavedData load(CompoundTag tag) {
        WeatherSavedData data = new WeatherSavedData();
        data.lastProcessedDay = tag.contains("LastProcessedDay") ? tag.getLong("LastProcessedDay") : Long.MIN_VALUE;
        data.lastTerm = tag.contains("LastTerm") ? tag.getInt("LastTerm") : -1;
        data.activeEvent = WeatherType.byId(tag.getString("ActiveEvent"));
        data.activeEventDay = tag.contains("ActiveEventDay") ? tag.getLong("ActiveEventDay") : Long.MIN_VALUE;
        data.ordinaryWeather = tag.getString("OrdinaryWeather");
        if (data.ordinaryWeather.isBlank()) data.ordinaryWeather = "clear";
        data.ordinaryWeatherStreak = tag.getInt("OrdinaryWeatherStreak");
        data.cooldownUntilDay = tag.contains("CooldownUntilDay") ? tag.getLong("CooldownUntilDay") : Long.MIN_VALUE;
        data.forecastDay = tag.contains("ForecastDay") ? tag.getLong("ForecastDay") : Long.MIN_VALUE;
        data.forecastTerm = tag.contains("ForecastTerm") ? tag.getInt("ForecastTerm") : -1;
        data.forecastOrdinaryWeather = tag.getString("ForecastOrdinaryWeather");
        if (data.forecastOrdinaryWeather.isBlank()) data.forecastOrdinaryWeather = "clear";
        data.forecastEvent = WeatherType.byId(tag.getString("ForecastEvent"));
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putLong("LastProcessedDay", lastProcessedDay);
        tag.putInt("LastTerm", lastTerm);
        tag.putString("ActiveEvent", activeEvent.id());
        tag.putLong("ActiveEventDay", activeEventDay);
        tag.putString("OrdinaryWeather", ordinaryWeather);
        tag.putInt("OrdinaryWeatherStreak", ordinaryWeatherStreak);
        tag.putLong("CooldownUntilDay", cooldownUntilDay);
        tag.putLong("ForecastDay", forecastDay);
        tag.putInt("ForecastTerm", forecastTerm);
        tag.putString("ForecastOrdinaryWeather", forecastOrdinaryWeather);
        tag.putString("ForecastEvent", forecastEvent.id());
        return tag;
    }
}




