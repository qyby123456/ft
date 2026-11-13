package cn.farmingtales.qysevent.worldevent;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import cn.farmingtales.qysevent.QysEventMod;
import cn.farmingtales.qysevent.WeatherManager;

/** Daily world events and the deliberately unreliable Oracle presentation. */
public final class DailyWorldEventManager {
    private static final ResourceLocation ICE_TEA = ResourceLocation.fromNamespaceAndPath("smc", "ice_tea");
    private static final int ORACLE_CLEAR_PERCENT = 45;
    private static final int ORACLE_FALSE_PERCENT = 28;
    private static final int ORACLE_GLITCH_COUNT = 8;
    private static final Map<MinecraftServer, TestOverride> TEST_OVERRIDES = new HashMap<>();

    private DailyWorldEventManager() {}

    public static long day(MinecraftServer server) {
        return Math.floorDiv(server.overworld().getDayTime(), 24000L);
    }

    public static DailyWorldEvent candidate(MinecraftServer server, long day) {
        DailyWorldEvent[] events = DailyWorldEvent.values();
        return events[DailyWorldEventRules.eventIndex(server.overworld().getSeed(), day, events.length)];
    }

    @Nullable
    public static DailyWorldEvent active(MinecraftServer server) {
        return DailyWorldEventData.get(server).activeEvent();
    }

    /** Called once per second by the event hook. */
    public static void tick(MinecraftServer server) {
        synchronize(server, true);
        DailyWorldEvent active = DailyWorldEventData.get(server).activeEvent();
        if (active == DailyWorldEvent.KOBE_MEMORIAL) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) grantIceTea(player);
        }
        if (server.getTickCount() % 200 == 0 && active == DailyWorldEvent.HEAVY_SNOW
                && !server.overworld().isRaining()) {
            server.overworld().setWeatherParameters(0, 12000, true, false);
        }
    }

    public static void onLogin(ServerPlayer player) {
        DailyWorldEvent active = DailyWorldEventData.get(player.server).activeEvent();
        if (active == DailyWorldEvent.KOBE_MEMORIAL) grantIceTea(player);
    }

    public static List<Forecast> forecast(MinecraftServer server, int days) {
        int count = Math.max(1, Math.min(30, days));
        long today = day(server);
        ArrayList<Forecast> result = new ArrayList<>(count);
        for (int offset = 1; offset <= count; offset++) {
            long target = today + offset;
            result.add(new Forecast(target, offset, candidate(server, target),
                    DailyWorldEventRules.triggers(server.overworld().getSeed(), target)));
        }
        return result;
    }

    public static void showForecast(ServerPlayer player, int days) {
        DailyWorldEvent current = DailyWorldEventData.get(player.server).activeEvent();
        player.sendSystemMessage(Component.translatable("message.qys_event.oracle.header", days)
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage((current == null
                ? Component.translatable("message.qys_event.oracle.today_none")
                : Component.translatable("message.qys_event.oracle.today", current.title()))
                .withStyle(current == null ? ChatFormatting.DARK_GRAY : ChatFormatting.GOLD));
        RandomSource random = player.getRandom();
        for (Forecast entry : forecast(player.server, days)) showOracleLine(player, entry, random);
    }

    private static void showOracleLine(ServerPlayer player, Forecast entry, RandomSource random) {
        int mode = random.nextInt(100);
        if (mode < ORACLE_CLEAR_PERCENT) {
            player.sendSystemMessage(oracleStateLine(entry.offset(), entry.triggered(), entry.event())
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        if (mode < ORACLE_CLEAR_PERCENT + ORACLE_FALSE_PERCENT) {
            // This branch is deliberately wrong: invert the trigger state, or name the wrong event.
            if (entry.triggered()) {
                player.sendSystemMessage(oracleStateLine(entry.offset(), false, entry.event())
                        .withStyle(ChatFormatting.DARK_AQUA));
            } else {
                player.sendSystemMessage(oracleStateLine(entry.offset(), true, differentEvent(entry.event(), random))
                        .withStyle(ChatFormatting.DARK_AQUA));
            }
            return;
        }
        int glitch = random.nextInt(ORACLE_GLITCH_COUNT);
        player.sendSystemMessage(Component.translatable("message.qys_event.oracle.glitch." + glitch, entry.offset())
                .withStyle(glitch % 2 == 0 ? ChatFormatting.DARK_PURPLE : ChatFormatting.DARK_GRAY));
    }

    private static MutableComponent oracleStateLine(int offset, boolean triggered, DailyWorldEvent event) {
        return triggered
                ? Component.translatable("message.qys_event.oracle.triggered", offset, event.title())
                : Component.translatable("message.qys_event.oracle.not_triggered", offset);
    }

    private static DailyWorldEvent differentEvent(DailyWorldEvent actual, RandomSource random) {
        DailyWorldEvent[] events = DailyWorldEvent.values();
        int shift = 1 + random.nextInt(events.length - 1);
        return events[(actual.ordinal() + shift) % events.length];
    }

    public static void setActiveForTesting(MinecraftServer server, @Nullable DailyWorldEvent selected) {
        synchronize(server, false);
        DailyWorldEventData data = DailyWorldEventData.get(server);
        DailyWorldEvent previous = data.activeEvent();
        if (selected == null) TEST_OVERRIDES.remove(server);
        else TEST_OVERRIDES.put(server, new TestOverride(day(server), selected));
        data.beginDay(day(server), selected);
        applyWeatherTransition(server, previous, selected);
        int previewSpawns = selected == null ? 0 : DailyWorldEventHooks.spawnImmediatePreview(server, selected);
        QysEventMod.LOGGER.info("[QysEvent][DailyEventTest] day={} previous={} forced={} previewSpawns={}", day(server),
                previous == null ? "none" : previous.id(), selected == null ? "none" : selected.id(), previewSpawns);
    }

    static void reset(MinecraftServer server) {
        TEST_OVERRIDES.remove(server);
    }

    private static void synchronize(MinecraftServer server, boolean announce) {
        DailyWorldEventData data = DailyWorldEventData.get(server);
        long today = day(server);
        TestOverride override = TEST_OVERRIDES.get(server);
        if (override != null && override.day() != today) {
            TEST_OVERRIDES.remove(server);
            override = null;
        }
        DailyWorldEvent forced = override == null ? null : override.event();
        if (data.currentDay() == today) {
            if (forced != null && data.activeEvent() != forced) {
                DailyWorldEvent previous = data.activeEvent();
                data.beginDay(today, forced);
                applyWeatherTransition(server, previous, forced);
            }
            return;
        }

        DailyWorldEvent previous = data.activeEvent();
        DailyWorldEvent selected = forced != null ? forced
                : (DailyWorldEventRules.triggers(server.overworld().getSeed(), today)
                ? candidate(server, today) : null);
        data.beginDay(today, selected);
        applyWeatherTransition(server, previous, selected);
        if (announce) {
            MutableComponent title = selected == null
                    ? Component.translatable("message.qys_event.daily_event.not_triggered")
                            .withStyle(ChatFormatting.DARK_GRAY)
                    : Component.translatable("message.qys_event.daily_event.start", selected.title())
                            .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            server.getPlayerList().broadcastSystemMessage(title, false);
        }
        QysEventMod.LOGGER.info("[QysEvent][DailyEvent] day={} candidate={} active={}", today,
                candidate(server, today).id(), selected == null ? "none" : selected.id());
    }

    private static void applyWeatherTransition(MinecraftServer server, @Nullable DailyWorldEvent previous,
                                               @Nullable DailyWorldEvent selected) {
        if (selected == DailyWorldEvent.HEAVY_SNOW) {
            server.overworld().setWeatherParameters(0, 24000, true, false);
        } else if (previous == DailyWorldEvent.HEAVY_SNOW) {
            WeatherManager.reapplyVanillaWeather(server.overworld());
        }
    }

    private static void grantIceTea(ServerPlayer player) {
        DailyWorldEventData data = DailyWorldEventData.get(player.server);
        if (!data.claimGift(player.getUUID(), data.currentDay())) return;
        Item item = ForgeRegistries.ITEMS.getValue(ICE_TEA);
        ItemStack gift = new ItemStack(item == null || item == Items.AIR ? Items.HONEY_BOTTLE : item);
        if (item == null || item == Items.AIR) gift.setHoverName(Component.translatable("item.qys_event.fallback_ice_tea"));
        if (!player.getInventory().add(gift)) player.drop(gift, false);
        player.sendSystemMessage(Component.translatable("message.qys_event.kobe_gift").withStyle(ChatFormatting.AQUA));
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(),
                    8, 0.35, 0.5, 0.35, 0.02);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP,
                    SoundSource.PLAYERS, 0.6F, 1.25F);
        }
    }

    private record TestOverride(long day, DailyWorldEvent event) {}
    public record Forecast(long day, int offset, DailyWorldEvent event, boolean triggered) {}
}
