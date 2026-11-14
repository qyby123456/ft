package net.farmingtales.ftboss.worldevent;

import java.util.ArrayList;
import java.util.List;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

/** Day-boundary synchronization, oracle forecasts, announcements, and event-wide ambient work. */
public final class DailyWorldEventManager {
    private static final ResourceLocation ICE_TEA = new ResourceLocation("smc", "ice_tea");

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
        synchronize(server, false);
        return DailyWorldEventData.get(server).activeEvent();
    }

    public static void tick(MinecraftServer server) {
        synchronize(server, true);
        DailyWorldEvent active = DailyWorldEventData.get(server).activeEvent();
        if (active != null) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                notifyOracleRealization(player, active);
                if (active == DailyWorldEvent.KOBE_MEMORIAL) grantIceTea(player);
            }
        }
        if (active == DailyWorldEvent.HEAVY_SNOW && server.getTickCount() % 200 == 0) {
            ServerLevel overworld = server.overworld();
            if (!overworld.isRaining()) overworld.setWeatherParameters(0, 12000, true, false);
        }
    }

    public static void onLogin(ServerPlayer player) {
        synchronize(player.server, false);
        DailyWorldEvent active = DailyWorldEventData.get(player.server).activeEvent();
        if (active != null) {
            player.sendSystemMessage(Component.translatable("message.ftboss.daily_event.login", active.title())
                    .withStyle(ChatFormatting.GOLD));
            player.sendSystemMessage(active.description().copy().withStyle(ChatFormatting.GRAY));
        }
        if (active != null) notifyOracleRealization(player, active);
        if (active == DailyWorldEvent.KOBE_MEMORIAL) grantIceTea(player);
    }

    public static List<Forecast> forecast(MinecraftServer server, int days) {
        int count = Math.max(1, Math.min(30, days));
        long today = day(server);
        List<Forecast> result = new ArrayList<>(count);
        for (int offset = 1; offset <= count; offset++) {
            long target = today + offset;
            result.add(new Forecast(target, offset, candidate(server, target), DailyWorldEventRules.DAILY_TRIGGER_CHANCE));
        }
        return result;
    }

    public static void showForecast(ServerPlayer player, int days) {
        synchronize(player.server, false);
        DailyWorldEventData data = DailyWorldEventData.get(player.server);
        data.recordOracleConsultation(player.getUUID(), data.currentDay());
        DailyWorldEvent current = data.activeEvent();
        player.sendSystemMessage(Component.translatable("message.ftboss.oracle.header", days)
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        player.sendSystemMessage((current == null
                ? Component.translatable("message.ftboss.oracle.today_none")
                : Component.translatable("message.ftboss.oracle.today", current.title()))
                .withStyle(current == null ? ChatFormatting.DARK_GRAY : ChatFormatting.GOLD));
        for (Forecast entry : forecast(player.server, days)) {
            player.sendSystemMessage(Component.translatable("message.ftboss.oracle.line", entry.offset(), entry.day() + 1L,
                    entry.event().title(), (int) Math.round(entry.chance() * 100.0D)).withStyle(ChatFormatting.GRAY));
        }
        player.sendSystemMessage(Component.translatable("message.ftboss.oracle.disclaimer")
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }

    private static void synchronize(MinecraftServer server, boolean announce) {
        DailyWorldEventData data = DailyWorldEventData.get(server);
        long today = day(server);
        if (data.currentDay() == today) return;
        DailyWorldEvent previous = data.activeEvent();
        DailyWorldEvent selected = DailyWorldEventRules.triggers(server.overworld().getSeed(), today)
                ? candidate(server, today) : null;
        data.beginDay(today, selected);
        if (selected == DailyWorldEvent.HEAVY_SNOW) {
            server.overworld().setWeatherParameters(0, 24000, true, false);
        } else if (previous == DailyWorldEvent.HEAVY_SNOW) {
            server.overworld().setWeatherParameters(0, 0, false, false);
        }
        if (announce && selected != null) {
            Component title = Component.translatable("message.ftboss.daily_event.start", selected.title())
                    .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
            server.getPlayerList().broadcastSystemMessage(title, false);
            server.getPlayerList().broadcastSystemMessage(selected.description().copy().withStyle(ChatFormatting.YELLOW), false);
        }
        FTBossMod.LOGGER.info("[FTBoss][DailyEvent] day={} candidate={} active={}", today,
                candidate(server, today).id(), selected == null ? "none" : selected.id());
    }

    private static void notifyOracleRealization(ServerPlayer player, DailyWorldEvent active) {
        DailyWorldEventData data = DailyWorldEventData.get(player.server);
        if (!data.claimOracleAlert(player.getUUID(), data.currentDay(), true)) return;
        player.sendSystemMessage(Component.translatable("message.ftboss.oracle.realized", active.title())
                .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD));
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.15D, player.getZ(),
                    28, 0.65D, 0.85D, 0.65D, 0.12D);
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
        }
        FTBossMod.LOGGER.info("[FTBoss][Oracle] yesterday's forecast realized for {}: {}",
                player.getGameProfile().getName(), active.id());
    }
    private static void grantIceTea(ServerPlayer player) {
        DailyWorldEventData data = DailyWorldEventData.get(player.server);
        if (!data.claimGift(player.getUUID(), data.currentDay())) return;
        Item item = ForgeRegistries.ITEMS.getValue(ICE_TEA);
        ItemStack gift = new ItemStack(item == null || item == Items.AIR ? Items.HONEY_BOTTLE : item);
        if (item == null || item == Items.AIR) gift.setHoverName(Component.translatable("item.ftboss.fallback_ice_tea"));
        if (!player.getInventory().add(gift)) player.drop(gift, false);
        player.sendSystemMessage(Component.translatable("message.ftboss.kobe_gift").withStyle(ChatFormatting.AQUA));
        if (player.level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 1.0D, player.getZ(),
                    24, 0.55D, 0.75D, 0.55D, 0.03D);
            level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.25F);
        }
    }

    public record Forecast(long day, int offset, DailyWorldEvent event, double chance) {}
}