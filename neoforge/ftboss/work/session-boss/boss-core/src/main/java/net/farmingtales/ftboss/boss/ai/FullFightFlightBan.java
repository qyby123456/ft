package net.farmingtales.ftboss.boss.ai;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = "ftboss")
public final class FullFightFlightBan {
    private FullFightFlightBan() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player && ManorFlyBan.active(player) && ManorFlyBan.stripFlight(player)) {
            ManorFlyBan.notifyPlayer(player);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player && ManorFlyBan.active(player) && ManorFlyBan.stripFlight(player)) {
            ManorFlyBan.notifyPlayer(player);
        }
    }
}
