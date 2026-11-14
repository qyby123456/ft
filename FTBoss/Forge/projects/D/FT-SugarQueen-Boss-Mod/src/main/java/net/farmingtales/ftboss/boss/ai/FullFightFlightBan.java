package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = FTBossMod.MOD_ID)
public final class FullFightFlightBan {
    private FullFightFlightBan() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && ManorFlyBan.active(player)
                && ManorFlyBan.stripFlight(player)) {
            ManorFlyBan.notifyPlayer(player);
        }
    }
}
