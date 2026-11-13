package cn.farmingtales.qysevent;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.TimeArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.valueproviders.IntProvider;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Locale;
import java.util.concurrent.CompletableFuture;

public final class WeatherCommands {
    private static final String WEATHER_NAMESPACE = QysEventMod.MOD_ID + ":";

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        registerSolarWeatherCommand(event);
        registerVanillaWeatherExtension(event);
    }

    private static void registerSolarWeatherCommand(RegisterCommandsEvent event) {
        // New unified names plus the old /solarweather compatibility alias.
        event.getDispatcher().register(buildWeatherRoot("qysweather"));
        event.getDispatcher().register(buildWeatherRoot("solarweather"));
        event.getDispatcher().register(Commands.literal("qysevent").then(buildWeatherRoot("weather")));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> buildWeatherRoot(String name) {
        return Commands.literal(name)
                .then(Commands.literal("status")
                        .executes(context -> status(context.getSource().getLevel(), context.getSource())))
                .then(Commands.literal("forecast")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            WeatherManager.Forecast forecast = WeatherManager.forecastTomorrow(context.getSource().getLevel());
                            context.getSource().sendSuccess(() -> Component.literal(WeatherManager.forecastLine(forecast)), false);
                            return 1;
                        }))
                .then(Commands.literal("start")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("type", StringArgumentType.greedyString())
                                .suggests(WeatherCommands::suggestShortSpecialWeather)
                                .executes(context -> start(context.getSource().getLevel(), context.getSource(),
                                        StringArgumentType.getString(context, "type")))))
                .then(Commands.literal("stop")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            WeatherManager.stopEvent(context.getSource().getLevel());
                            return 1;
                        }))
                .then(Commands.literal("reroll")
                        .requires(source -> source.hasPermission(2))
                        .executes(context -> {
                            WeatherManager.rerollToday(context.getSource().getLevel());
                            context.getSource().sendSuccess(() -> Component.literal("§a已重新抽取今日天气。"), true);
                            return 1;
                        }));
    }

    /**
     * Merges custom weather into Minecraft's existing /weather command. The
     * vanilla literals are re-registered so they also clear the active special
     * event and keep the farming state consistent with the visible weather.
     */
    private static void registerVanillaWeatherExtension(RegisterCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("weather")
                .requires(source -> source.hasPermission(2))
                .then(ordinaryWeatherCommand("clear", ServerLevel.RAIN_DELAY))
                .then(ordinaryWeatherCommand("rain", ServerLevel.RAIN_DURATION))
                .then(ordinaryWeatherCommand("thunder", ServerLevel.THUNDER_DURATION))
                .then(Commands.argument("solar_weather", StringArgumentType.greedyString())
                        .suggests(WeatherCommands::suggestWeather)
                        .executes(context -> setSpecialFromWeatherCommand(
                                context.getSource(), StringArgumentType.getString(context, "solar_weather")))));
    }

    private static LiteralArgumentBuilder<CommandSourceStack> ordinaryWeatherCommand(String weather, IntProvider defaultDuration) {
        return Commands.literal(weather)
                .executes(context -> setOrdinaryWeather(context.getSource(), weather, -1, defaultDuration))
                .then(Commands.argument("duration", TimeArgument.time(1))
                        .executes(context -> setOrdinaryWeather(
                                context.getSource(), weather,
                                IntegerArgumentType.getInteger(context, "duration"), defaultDuration)));
    }

    private static int setOrdinaryWeather(CommandSourceStack source, String weather, int requestedDuration, IntProvider defaultDuration) {
        int duration = requestedDuration == -1
                ? defaultDuration.sample(source.getLevel().getRandom())
                : requestedDuration;
        WeatherManager.forceOrdinaryWeather(source.getLevel(), weather, duration);
        source.sendSuccess(() -> Component.translatable("commands.weather.set." + weather), true);
        return duration;
    }

    private static int setSpecialFromWeatherCommand(CommandSourceStack source, String value) {
        String id = stripNamespace(value);
        if (id.equals("none") || id.equals("normal") || id.equals("auto")) {
            WeatherManager.stopEvent(source.getLevel());
            source.sendSuccess(() -> Component.literal("§a已恢复二十四节气自动天气。"), true);
            return 1;
        }

        OrdinaryWeatherType ordinary = OrdinaryWeatherType.find(id);
        if (ordinary != null) {
            WeatherManager.forceOrdinaryWeather(source.getLevel(), ordinary.id(), 24000);
            source.sendSuccess(() -> Component.literal("§a普通天气已触发：§f" + ordinary.chineseName()), true);
            return 24000;
        }
        return start(source.getLevel(), source, id);
    }

    private static CompletableFuture<Suggestions> suggestShortSpecialWeather(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (WeatherType type : WeatherType.values()) {
            if (type == WeatherType.NONE) continue;
            if (SharedSuggestionProvider.matchesSubStr(remaining, type.id())) {
                builder.suggest(type.id(), weatherTooltip(type));
            }
        }
        return builder.buildFuture();
    }

    private static CompletableFuture<Suggestions> suggestWeather(
            CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String remaining = builder.getRemaining().toLowerCase(Locale.ROOT);
        for (OrdinaryWeatherType ordinary : OrdinaryWeatherType.values()) {
            if (SharedSuggestionProvider.matchesSubStr(remaining, ordinary.id())) {
                builder.suggest(ordinary.id(), Component.literal(ordinary.chineseName() + "｜普通天气")
                        .withStyle(ChatFormatting.GRAY));
            }
        }

        if (SharedSuggestionProvider.matchesSubStr(remaining, "none")) {
            builder.suggest("none", Component.literal("自动天气｜停止特殊天气，恢复二十四节气天气循环")
                    .withStyle(ChatFormatting.GRAY));
        }
        for (WeatherType type : WeatherType.values()) {
            if (type == WeatherType.NONE) continue;
            if (SharedSuggestionProvider.matchesSubStr(remaining, type.id())) {
                builder.suggest(type.id(), weatherTooltip(type));
            }
        }
        return builder.buildFuture();
    }


    private static Component weatherTooltip(WeatherType type) {
        return Component.literal(type.chineseName() + "｜特殊天气")
                .withStyle(ChatFormatting.GOLD);
    }

    private static String stripNamespace(String value) {
        String normalized = value.toLowerCase(Locale.ROOT);
        return normalized.startsWith(WEATHER_NAMESPACE)
                ? normalized.substring(WEATHER_NAMESPACE.length())
                : normalized;
    }

    private static int status(ServerLevel level, CommandSourceStack source) {
        source.sendSuccess(() -> Component.literal(WeatherManager.statusLine(level)), false);
        return 1;
    }

    private static int start(ServerLevel level, CommandSourceStack source, String id) {
        WeatherType type = WeatherType.byId(stripNamespace(id));
        if (type == WeatherType.NONE) {
            source.sendFailure(Component.literal("§c未知天气类型：" + id));
            return 0;
        }
        WeatherManager.forceEvent(level, type);
        source.sendSuccess(() -> Component.literal("§a已强制启动：" + type.chineseName()), true);
        return 1;
    }

    private WeatherCommands() {}
}






