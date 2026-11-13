package cn.farmingtales.qysevent.client;

import cn.farmingtales.qysevent.OrdinaryWeatherType;
import cn.farmingtales.qysevent.WeatherType;
import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventRules;

public final class ClientWeatherState {
    private static WeatherType weather = WeatherType.NONE;
    private static OrdinaryWeatherType ordinaryWeather = OrdinaryWeatherType.CLEAR;
    private static DailyWorldEvent dailyEvent;

    public static WeatherType weather() {
        return weather;
    }

    public static OrdinaryWeatherType ordinaryWeather() {
        return ordinaryWeather;
    }

    public static DailyWorldEvent dailyEvent() {
        return dailyEvent;
    }

    public static int precipitationColor() {
        if (dailyEvent == DailyWorldEvent.HEAVY_SNOW) {
            return DailyWorldEventRules.HEAVY_SNOW_PRECIPITATION_COLOR;
        }
        int specialColor = weather.precipitationColor();
        return weather != WeatherType.NONE && specialColor != 0xFFFFFF
                ? specialColor : ordinaryWeather.precipitationColor();
    }

    public static void setWeather(WeatherType special, OrdinaryWeatherType ordinary, DailyWorldEvent daily) {
        weather = special == null ? WeatherType.NONE : special;
        ordinaryWeather = ordinary == null ? OrdinaryWeatherType.CLEAR : ordinary;
        dailyEvent = daily;
    }

    public static void clear() {
        weather = WeatherType.NONE;
        ordinaryWeather = OrdinaryWeatherType.CLEAR;
        dailyEvent = null;
    }

    private ClientWeatherState() {}
}


