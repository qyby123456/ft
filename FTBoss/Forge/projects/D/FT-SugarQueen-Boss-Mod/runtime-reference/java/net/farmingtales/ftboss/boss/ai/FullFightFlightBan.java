/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package net.farmingtales.ftboss.boss.ai;

import net.farmingtales.ftboss.boss.ai.ManorFlyBan;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="ftboss")
public final class FullFightFlightBan {
    private FullFightFlightBan() {
    }

    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onPlayerTick(TickEvent.PlayerTickEvent playerTickEvent) {
        ServerPlayer serverPlayer;
        if (playerTickEvent.player instanceof ServerPlayer && ManorFlyBan.active(serverPlayer = (ServerPlayer)playerTickEvent.player) && ManorFlyBan.stripFlight(serverPlayer)) {
            ManorFlyBan.notifyPlayer(serverPlayer);
        }
    }
}

