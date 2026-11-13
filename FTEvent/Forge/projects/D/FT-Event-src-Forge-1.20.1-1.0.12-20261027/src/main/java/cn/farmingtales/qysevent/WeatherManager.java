package cn.farmingtales.qysevent;

import cn.farmingtales.qysevent.worldevent.DailyWorldEvent;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventData;
import cn.farmingtales.qysevent.worldevent.DailyWorldEventManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerXpEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class WeatherManager {
    private static final int DAY_TICKS = 24000;
    private static final Map<UUID, ClientWeatherSnapshot> LAST_CLIENT_WEATHER = new HashMap<>();

    public record Forecast(long targetDay, SolarTerms.TermInfo term, String ordinaryWeather,
                           WeatherType specialEvent, int predictedStreak, double yieldMultiplier) {}

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        long gameTime = level.getGameTime();
        if (gameTime % 20L != 0L) return;

        WeatherSavedData data = WeatherSavedData.get(level);
        tickDay(level, data);
        WeatherType active = effectiveEvent(level, data);
        syncClientWeather(server, active, data);
        WeatherEffects.applyPlayerAtmosphere(level, active);

        if (active != WeatherType.NONE && gameTime % 100L == 0L) {
            WeatherEffects.pulseBeneficialTerrain(level, active);
            WeatherEffects.pulseTerrain(level, active);
            WeatherEffects.pulseRareHazards(level, active);
        }
        if (active == WeatherType.SLIME_RAIN && gameTime % 200L == 0L) {
            WeatherEffects.spawnFallingSlimes(level);
        }
    }

    private static void syncClientWeather(MinecraftServer server, WeatherType active, WeatherSavedData data) {
        OrdinaryWeatherType ordinary = OrdinaryWeatherType.byId(data.ordinaryWeather);
        DailyWorldEvent activeDaily = DailyWorldEventManager.active(server);
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            boolean inOverworld = player.serverLevel().dimension().equals(Level.OVERWORLD);
            DailyWorldEvent daily = inOverworld ? activeDaily : null;
            ClientWeatherSnapshot visible = new ClientWeatherSnapshot(
                    inOverworld ? active : WeatherType.NONE,
                    inOverworld ? ordinary : OrdinaryWeatherType.CLEAR,
                    daily
            );
            UUID playerId = player.getUUID();
            if (visible.equals(LAST_CLIENT_WEATHER.get(playerId))) continue;
            LAST_CLIENT_WEATHER.put(playerId, visible);
            WeatherNetwork.sync(player, visible.special(), visible.ordinary(), visible.daily());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) LAST_CLIENT_WEATHER.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        LAST_CLIENT_WEATHER.clear();
    }

    @SubscribeEvent
    public static void onXpChange(PlayerXpEvent.XpChange event) {
        if (event.getAmount() <= 0 || !(event.getEntity().level() instanceof ServerLevel level)) return;
        WeatherType type = getEffectiveEvent(level.getServer().overworld());
        if (type == WeatherType.AURORA) event.setAmount(event.getAmount() * 2);
        else if (type == WeatherType.COMET_NIGHT) event.setAmount(Math.max(1, (int) Math.ceil(event.getAmount() * 1.75D)));
        else if (type == WeatherType.METEOR_SHOWER || type == WeatherType.STAR_DEW)
            event.setAmount(Math.max(1, (int) Math.ceil(event.getAmount() * 1.5D)));
        else if (type == WeatherType.FIREFLY_NIGHT)
            event.setAmount(Math.max(1, (int) Math.ceil(event.getAmount() * 1.15D)));
    }

    @SubscribeEvent
    public static void onCropGrowPre(BlockEvent.CropGrowEvent.Pre event) {
        if (!WeatherConfig.CROP_GROWTH_EFFECTS.get() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!CropUtil.isCrop(event.getState())) return;
        double multiplier = getYieldMultiplier(level.getServer().overworld());
        if (multiplier < 1.0D && level.random.nextDouble() > multiplier) event.setResult(Event.Result.DENY);
    }

    @SubscribeEvent
    public static void onCropGrowPost(BlockEvent.CropGrowEvent.Post event) {
        if (!WeatherConfig.CROP_GROWTH_EFFECTS.get() || !(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        if (!CropUtil.isCrop(state) || CropUtil.isMature(state)) return;
        double multiplier = getYieldMultiplier(level.getServer().overworld());
        if (multiplier <= 1.0D) return;
        double chance = Math.min(0.45D, (multiplier - 1.0D) * 0.35D);
        if (level.random.nextDouble() >= chance) return;
        WeatherEffects.growOnce(level, pos, state);
    }

    private static void tickDay(ServerLevel level, WeatherSavedData data) {
        long day = Math.floorDiv(level.getDayTime(), DAY_TICKS);
        if (data.lastProcessedDay == day) return;

        SolarTerms.TermInfo term = SolarTerms.current(level);
        boolean termChanged = data.lastTerm != term.index();
        data.lastProcessedDay = day;
        data.lastTerm = term.index();
        data.activeEvent = WeatherType.NONE;
        data.activeEventDay = day;

        if (data.forecastDay == day) {
            applyOrdinaryWeather(data, data.forecastOrdinaryWeather);
            data.activeEvent = data.forecastEvent;
        } else {
            applyOrdinaryWeather(data, chooseOrdinaryWeather(level.random, term.index()));
            if (day >= data.cooldownUntilDay && level.random.nextDouble() < WeatherConfig.SPECIAL_EVENT_CHANCE.get()) {
                data.activeEvent = rollSpecialEvent(level.random, term.index());
            }
        }

        if (data.activeEvent != WeatherType.NONE) {
            data.activeEventDay = day;
            data.cooldownUntilDay = day + WeatherConfig.SPECIAL_EVENT_COOLDOWN_DAYS.get() + 1L;
        }

        applyVanillaWeather(level, data);
        data.setDirty();

        String special = data.activeEvent == WeatherType.NONE
                ? "§7未触发"
                : "§a已触发：§f§l" + data.activeEvent.chineseName();
        String termText = termChanged && WeatherConfig.ANNOUNCE_SOLAR_TERMS.get()
                ? " §7| §6节气：§f" + term.name() : "";
        broadcast(level, Component.literal("§6[今日天气] §f普通天气：§e" + ordinaryName(data.ordinaryWeather) +
                " §7| §f特殊天气：" + special + termText));
    }

    private static String chooseOrdinaryWeather(RandomSource random, int term) {
        SolarTerms.SeasonBand season = SolarTerms.seasonForTerm(term);
        double roll = random.nextDouble();
        if (roll < SolarTerms.thunderChance(term)) return OrdinaryWeatherType.THUNDER.id();

        if (roll < SolarTerms.thunderChance(term) + SolarTerms.rainChance(term)) {
            double variation = random.nextDouble();
            return switch (season) {
                case SPRING -> variation < 0.45D ? OrdinaryWeatherType.DRIZZLE.id() : OrdinaryWeatherType.RAIN.id();
                case SUMMER -> variation < 0.30D ? OrdinaryWeatherType.SUNSHOWER.id()
                        : variation < 0.42D ? OrdinaryWeatherType.DRIZZLE.id() : OrdinaryWeatherType.RAIN.id();
                case AUTUMN -> variation < 0.25D ? OrdinaryWeatherType.DRIZZLE.id() : OrdinaryWeatherType.RAIN.id();
                case WINTER -> OrdinaryWeatherType.RAIN.id();
            };
        }

        double variation = random.nextDouble();
        return switch (season) {
            case SPRING -> variation < 0.22D ? OrdinaryWeatherType.BREEZE.id()
                    : variation < 0.40D ? OrdinaryWeatherType.CLOUDY.id()
                    : variation < 0.48D ? OrdinaryWeatherType.OVERCAST.id() : OrdinaryWeatherType.CLEAR.id();
            case SUMMER -> variation < 0.16D ? OrdinaryWeatherType.BREEZE.id()
                    : variation < 0.28D ? OrdinaryWeatherType.CLOUDY.id() : OrdinaryWeatherType.CLEAR.id();
            case AUTUMN -> variation < 0.28D ? OrdinaryWeatherType.OVERCAST.id()
                    : variation < 0.45D ? OrdinaryWeatherType.CLOUDY.id()
                    : variation < 0.58D ? OrdinaryWeatherType.BREEZE.id() : OrdinaryWeatherType.CLEAR.id();
            case WINTER -> variation < 0.38D ? OrdinaryWeatherType.OVERCAST.id()
                    : variation < 0.58D ? OrdinaryWeatherType.CLOUDY.id() : OrdinaryWeatherType.CLEAR.id();
        };
    }

    private static void applyOrdinaryWeather(WeatherSavedData data, String weather) {
        if (weather.equals(data.ordinaryWeather)) data.ordinaryWeatherStreak++;
        else {
            data.ordinaryWeather = weather;
            data.ordinaryWeatherStreak = 1;
        }
    }

    private static WeatherType rollSpecialEvent(RandomSource random, int term) {
        List<WeightedWeather> pool = new ArrayList<>();
        SolarTerms.SeasonBand season = SolarTerms.seasonForTerm(term);

        // Year-round celestial events remain possible, but destructive climate
        // events are added only after the current season has been determined.
        add(pool, WeatherType.BLOOD_MOON, 3);
        add(pool, WeatherType.SOLAR_ECLIPSE, season == SolarTerms.SeasonBand.SUMMER ? 4 : 2);
        add(pool, WeatherType.METEOR_SHOWER, 4);
        add(pool, WeatherType.COMET_NIGHT, 2);
        add(pool, WeatherType.STAR_DEW, 3);
        add(pool, WeatherType.FIREFLY_NIGHT, 4);

        switch (season) {
            case SPRING -> {
                add(pool, WeatherType.SLIME_RAIN, 6);
                add(pool, WeatherType.SANDSTORM, 5);
                add(pool, WeatherType.THUNDERSTORM, 6);
                add(pool, WeatherType.REDSTONE_STORM, 4);
                add(pool, WeatherType.GREEN_RAIN, 8);
                add(pool, WeatherType.DENSE_FOG, 6);
                add(pool, WeatherType.RAINBOW_RAIN, 7);
                add(pool, WeatherType.POLLEN_STORM, 12);
                add(pool, WeatherType.SPRING_BLOOM, 10);
                add(pool, WeatherType.HAILSTORM, 7);
                add(pool, WeatherType.ACID_RAIN, 5);
                add(pool, WeatherType.MISTY_DRIZZLE, 11);
                add(pool, WeatherType.BEE_FLIGHT, 10);
            }
            case SUMMER -> {
                add(pool, WeatherType.SLIME_RAIN, 5);
                add(pool, WeatherType.HURRICANE, 9);
                add(pool, WeatherType.SANDSTORM, 4);
                add(pool, WeatherType.THUNDERSTORM, 13);
                add(pool, WeatherType.REDSTONE_STORM, 8);
                add(pool, WeatherType.GREEN_RAIN, 6);
                add(pool, WeatherType.HEATWAVE, 12);
                add(pool, WeatherType.RAINBOW_RAIN, 5);
                add(pool, WeatherType.HAILSTORM, 5);
                add(pool, WeatherType.MONSOON, 10);
                add(pool, WeatherType.ACID_RAIN, 7);
                add(pool, WeatherType.ASH_FALL, 4);
                add(pool, WeatherType.DRY_LIGHTNING, 8);
                add(pool, WeatherType.BEE_FLIGHT, 7);
            }
            case AUTUMN -> {
                add(pool, WeatherType.HARVEST_MOON, 14);
                add(pool, WeatherType.HURRICANE, 5);
                add(pool, WeatherType.THUNDERSTORM, 4);
                add(pool, WeatherType.REDSTONE_STORM, 5);
                add(pool, WeatherType.DENSE_FOG, 8);
                add(pool, WeatherType.AURORA, 4);
                add(pool, WeatherType.GOLDEN_WIND, 11);
                add(pool, WeatherType.FROST_NIGHT, 7);
                add(pool, WeatherType.ASH_FALL, 6);
                add(pool, WeatherType.MISTY_DRIZZLE, 8);
                add(pool, WeatherType.DRY_LIGHTNING, 5);
            }
            case WINTER -> {
                if (term == 21) add(pool, WeatherType.CHRISTMAS_EVE, 20);
                add(pool, WeatherType.BLIZZARD, 13);
                add(pool, WeatherType.AURORA, 12);
                add(pool, WeatherType.DENSE_FOG, 5);
                add(pool, WeatherType.FROST_NIGHT, 6);
                add(pool, WeatherType.SILENT_SNOW, 11);
                add(pool, WeatherType.THUNDERSNOW, 10);
            }
        }

        int total = pool.stream().mapToInt(WeightedWeather::weight).sum();
        if (total <= 0) return WeatherType.NONE;
        int roll = random.nextInt(total);
        for (WeightedWeather entry : pool) {
            roll -= entry.weight();
            if (roll < 0) return entry.type();
        }
        return WeatherType.NONE;
    }
    private static void add(List<WeightedWeather> pool, WeatherType type, int weight) {
        if (weight > 0) pool.add(new WeightedWeather(type, weight));
    }

    private static void applyVanillaWeather(ServerLevel level, WeatherSavedData data) {
        if (!WeatherConfig.CONTROL_VANILLA_WEATHER.get()) return;
        OrdinaryWeatherType ordinary = OrdinaryWeatherType.byId(data.ordinaryWeather);
        boolean rain = ordinary.rains();
        boolean thunder = ordinary.thunders();
        switch (data.activeEvent) {
            case CHRISTMAS_EVE, SLIME_RAIN, BLIZZARD, GREEN_RAIN, RAINBOW_RAIN,
                    HAILSTORM, MONSOON, SILENT_SNOW, ACID_RAIN, MISTY_DRIZZLE -> rain = true;
            case HURRICANE, THUNDERSTORM, REDSTONE_STORM, THUNDERSNOW -> { rain = true; thunder = true; }
            case HARVEST_MOON, BLOOD_MOON, SOLAR_ECLIPSE, METEOR_SHOWER, SANDSTORM,
                    AURORA, HEATWAVE, DENSE_FOG, POLLEN_STORM, SPRING_BLOOM, GOLDEN_WIND,
                    FROST_NIGHT, COMET_NIGHT, ASH_FALL, STAR_DEW, DRY_LIGHTNING,
                    FIREFLY_NIGHT, BEE_FLIGHT -> { rain = false; thunder = false; }
            default -> { }
        }
        // Daily Heavy Snow is an event overlay, so it wins over the ordinary weather timer.
        if (DailyWorldEventData.get(level.getServer()).activeEvent() == DailyWorldEvent.HEAVY_SNOW) {
            rain = true;
            thunder = false;
        }
        level.setWeatherParameters(rain ? 0 : DAY_TICKS, rain ? DAY_TICKS : 0, rain, thunder);
    }

    public static void reapplyVanillaWeather(ServerLevel level) {
        WeatherSavedData data = WeatherSavedData.get(level);
        applyVanillaWeather(level, data);
    }

    private static void announceEvent(ServerLevel level, WeatherType type) {
        broadcast(level, Component.literal("§d[特殊天气] §a已触发：§f§l" + type.chineseName()));
    }

    public static String seasonDescription(WeatherType type) {
        return switch (type) {
            case HARVEST_MOON -> "秋季";
            case CHRISTMAS_EVE, BLIZZARD, SILENT_SNOW, THUNDERSNOW -> "冬季";
            case SLIME_RAIN, SANDSTORM, GREEN_RAIN, RAINBOW_RAIN, HAILSTORM, ACID_RAIN -> "春夏";
            case HURRICANE, ASH_FALL, DRY_LIGHTNING -> "夏秋";
            case THUNDERSTORM, REDSTONE_STORM -> "春夏秋";
            case AURORA -> "秋冬";
            case HEATWAVE, MONSOON -> "夏季";
            case DENSE_FOG -> "春秋冬";
            case POLLEN_STORM, SPRING_BLOOM -> "春季";
            case MISTY_DRIZZLE -> "春秋";
            case BEE_FLIGHT -> "春夏";
            case GOLDEN_WIND -> "秋季";
            case FROST_NIGHT -> "秋冬";
            case BLOOD_MOON, SOLAR_ECLIPSE, METEOR_SHOWER, COMET_NIGHT, STAR_DEW,
                    FIREFLY_NIGHT, NONE -> "全年";
        };
    }

    public static String effectDescription(WeatherType type) {
        return switch (type) {
            case HARVEST_MOON -> "月华令成熟作物收成翻倍，并赐予幸运。";
            case CHRISTMAS_EVE -> "雪夜带来幸运、再生与抗性，作物增产50%。";
            case BLOOD_MOON -> "怪物获得力量与速度，玩家虚弱，作物减产28%。";
            case SOLAR_ECLIPSE -> "黑色滤镜笼罩白昼，玩家挖掘疲劳，作物减产22%。";
            case METEOR_SHOWER -> "经验获取提高50%，小型陨石坠落时极小概率带来矿石块。";
            case SLIME_RAIN -> "玩家跳跃增强，史莱姆会从天空落下。";
            case BLIZZARD -> "原版雪花席卷天空，玩家寒冷、缓慢且虚弱，并可能毁坏作物与农田。";
            case HURRICANE -> "强风推动露天实体，并破坏玻璃、土地与作物。";
            case SANDSTORM -> "浅沙色滤镜伴随缓慢，并侵蚀农田与作物。";
            case THUNDERSTORM -> "雷暴会毁坏作物并在露天区域召来落雷。";
            case REDSTONE_STORM -> "猩红雨幕伴随雷暴，露天苦力怕可能被充能，作物减产22%。";
            case GREEN_RAIN -> "绿雨持续催熟作物、湿润农田，收成提高75%。";
            case AURORA -> "极光赐予夜视、幸运与缓降，经验获取翻倍。";
            case HEATWAVE -> "热浪造成饥饿与虚弱，烤干农田，作物减产50%。";
            case DENSE_FOG -> "较深灰雾滤镜伴随缓慢，作物收成降低12%。";
            case RAINBOW_RAIN -> "彩虹雨带来幸运、再生、速度并催熟作物，增产60%。";
            case POLLEN_STORM -> "花粉促进授粉和生长，但会使露天玩家眩晕，增产32%。";
            case SPRING_BLOOM -> "花潮带来再生与幸运，促进作物生长并增产45%。";
            case HAILSTORM -> "浅冰蓝滤镜伴随冰屑与轻微碎冰声，冰雹会损害玻璃和作物，减产28%。";
            case MONSOON -> "季风暴雨湿润农田并轻推露天实体，增产12%。";
            case GOLDEN_WIND -> "金风带来速度与幸运，促进作物生长并增产30%。";
            case FROST_NIGHT -> "浅霜色滤镜伴随寒意，可能冻伤作物，减产18%。";
            case SILENT_SNOW -> "静雪带来抗性与缓降，温和增产10%。";
            case COMET_NIGHT -> "彗星赐予夜视与幸运，经验获取提高75%。";
            case ACID_RAIN -> "酸雨使未戴头盔的露天玩家中毒，并腐蚀作物与农田，减产40%。";
            case ASH_FALL -> "灰色雪片般的灰烬飘落，造成虚弱与挖掘疲劳并损伤作物和土地，减产30%。";
            case STAR_DEW -> "夜间星露带来夜视、幸运与再生，催熟作物且经验提高50%，增产50%。";
            case THUNDERSNOW -> "雷雪带来冻结、缓慢与落雷，并损坏玻璃和作物，减产42%。";
            case MISTY_DRIZZLE -> "温和烟雨带来再生、湿润农田并促进生长，增产28%。";
            case DRY_LIGHTNING -> "无雨干雷造成饥饿和虚弱，频繁落雷并烤坏农田，减产45%。";
            case FIREFLY_NIGHT -> "萤火照亮夜色，赐予夜视与幸运，经验提高15%，增产18%。";
            case BEE_FLIGHT -> "晴日蜂群授粉，带来速度与幸运并显著促进作物生长，增产42%。";
            case NONE -> "未预测到特殊天气，仅受基础天气和连续天数影响。";
        };
    }

    private static void broadcast(ServerLevel level, Component component) {
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) player.sendSystemMessage(component);
    }

    private static WeatherType effectiveEvent(ServerLevel level, WeatherSavedData data) {
        long day = Math.floorDiv(level.getDayTime(), DAY_TICKS);
        return data.activeEvent != WeatherType.NONE && data.activeEventDay == day
                && data.activeEvent.isActiveAt(level.getDayTime()) ? data.activeEvent : WeatherType.NONE;
    }

    public static WeatherType getEffectiveEvent(ServerLevel level) {
        return effectiveEvent(level, WeatherSavedData.get(level));
    }

    public static double getYieldMultiplier(ServerLevel level) {
        WeatherSavedData data = WeatherSavedData.get(level);
        WeatherType special = effectiveEvent(level, data);
        return yieldMultiplier(data.ordinaryWeather, Math.max(1, data.ordinaryWeatherStreak), special);
    }

    private static double yieldMultiplier(String ordinary, int streak, WeatherType special) {
        if (special != WeatherType.NONE) return special.yieldMultiplier();
        OrdinaryWeatherType ordinaryType = OrdinaryWeatherType.byId(ordinary);
        if (ordinaryType.neutral()) return 1.0D;

        int threshold = WeatherConfig.DIMINISHING_STREAK_START.get();
        if (streak < threshold) {
            return switch (ordinaryType) {
                case RAIN -> 1.25D;
                case THUNDER -> 1.18D;
                case CLEAR -> 1.15D;
                default -> 1.0D;
            };
        }
        if (streak == threshold) return 1.0D;
        return Math.max(0.65D, 1.0D - 0.10D * (streak - threshold));
    }

    public static Forecast forecastTomorrow(ServerLevel sourceLevel) {
        ServerLevel level = sourceLevel.getServer().overworld();
        long today = Math.floorDiv(level.getDayTime(), DAY_TICKS);
        long targetDay = today + 1L;
        WeatherSavedData data = WeatherSavedData.get(level);
        SolarTerms.TermInfo term = SolarTerms.atDayOffset(level, 1);

        if (data.forecastDay != targetDay) {
            data.forecastDay = targetDay;
            data.forecastOrdinaryWeather = chooseOrdinaryWeather(level.random, term.index());
            data.forecastEvent = WeatherType.NONE;
            if (targetDay >= data.cooldownUntilDay && level.random.nextDouble() < WeatherConfig.SPECIAL_EVENT_CHANCE.get()) {
                data.forecastEvent = rollSpecialEvent(level.random, term.index());
            }
            data.setDirty();
        }

        int predictedStreak = data.forecastOrdinaryWeather.equals(data.ordinaryWeather)
                ? Math.max(1, data.ordinaryWeatherStreak) + 1 : 1;
        double multiplier = yieldMultiplier(data.forecastOrdinaryWeather, predictedStreak, data.forecastEvent);
        return new Forecast(targetDay, term, data.forecastOrdinaryWeather, data.forecastEvent, predictedStreak, multiplier);
    }

    public static String forecastLine(Forecast forecast) {
        String special = forecast.specialEvent() == WeatherType.NONE
                ? "§7未触发"
                : "§a已触发：§f§l" + forecast.specialEvent().chineseName();
        return "§6[天气预报] §f普通天气：§e" + ordinaryName(forecast.ordinaryWeather()) +
                " §7| §f特殊天气：" + special;
    }

    public static String statusLine(ServerLevel level) {
        WeatherSavedData data = WeatherSavedData.get(level);
        String special = data.activeEvent == WeatherType.NONE
                ? "§7未触发"
                : "§a已触发：§f§l" + data.activeEvent.chineseName();
        return "§6[当前天气] §f普通天气：§e" + ordinaryName(data.ordinaryWeather) +
                " §7| §f特殊天气：" + special;
    }

    public static String ordinaryName(String id) {
        return OrdinaryWeatherType.byId(id).chineseName();
    }

    /**
     * Applies gameplay effects only. The client handles precipitation tint and a small
     * local particle budget, avoiding per-player weather particle packets.
     */
    public static void forceOrdinaryWeather(ServerLevel level, String weather, int durationTicks) {
        ServerLevel overworld = level.getServer().overworld();
        OrdinaryWeatherType ordinary = OrdinaryWeatherType.find(weather);
        if (ordinary == null) throw new IllegalArgumentException("Unsupported ordinary weather: " + weather);

        WeatherSavedData data = WeatherSavedData.get(overworld);
        data.activeEvent = WeatherType.NONE;
        data.activeEventDay = Math.floorDiv(overworld.getDayTime(), DAY_TICKS);
        data.ordinaryWeather = ordinary.id();
        data.ordinaryWeatherStreak = 1;
        overworld.setWeatherParameters(ordinary.rains() ? 0 : durationTicks, ordinary.rains() ? durationTicks : 0,
                ordinary.rains(), ordinary.thunders());

        data.setDirty();
        syncClientWeather(level.getServer(), effectiveEvent(overworld, data), data);
    }

    public static void forceEvent(ServerLevel level, WeatherType type) {
        ServerLevel overworld = level.getServer().overworld();
        long day = Math.floorDiv(overworld.getDayTime(), DAY_TICKS);
        WeatherSavedData data = WeatherSavedData.get(overworld);
        data.activeEvent = type;
        data.activeEventDay = day;
        applyVanillaWeather(overworld, data);
        data.setDirty();
        syncClientWeather(level.getServer(), effectiveEvent(overworld, data), data);
        announceEvent(overworld, type);
    }

    public static void stopEvent(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        WeatherSavedData data = WeatherSavedData.get(overworld);
        data.activeEvent = WeatherType.NONE;
        applyVanillaWeather(overworld, data);
        data.setDirty();
        syncClientWeather(level.getServer(), effectiveEvent(overworld, data), data);
        broadcast(overworld, Component.literal("§6[节气天气] §f特殊天气已停止。"));
    }

    public static void rerollToday(ServerLevel level) {
        ServerLevel overworld = level.getServer().overworld();
        WeatherSavedData data = WeatherSavedData.get(overworld);
        data.lastProcessedDay = Long.MIN_VALUE;
        data.cooldownUntilDay = Long.MIN_VALUE;
        data.setDirty();
        tickDay(overworld, data);
    }

    private record WeightedWeather(WeatherType type, int weight) {}
    private record ClientWeatherSnapshot(WeatherType special, OrdinaryWeatherType ordinary,
                                         DailyWorldEvent daily) {}

    private WeatherManager() {}
}











