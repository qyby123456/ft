package net.farmingtales.ftboss.dandannativelights;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class LightNetwork {
    private static boolean registered;

    private LightNetwork() {
    }

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent var0) {
        if (!registered) {
            var0.registrar("1").playToClient(LightPacket.TYPE, LightPacket.STREAM_CODEC, LightPacket::handle);
            registered = true;
        }
    }

    public static void send(ServerLevel var0, LightPacket var1) {
        if (registered && var1.valid()) {
            for (ServerPlayer var3 : var0.players()) {
                if (var3.distanceToSqr(var1.origin()) <= 9216.0 && var3.connection.hasChannel(LightPacket.TYPE)) {
                    PacketDistributor.sendToPlayer(var3, var1);
                }
            }
        }
    }
}
