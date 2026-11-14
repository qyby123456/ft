package net.farmingtales.ftboss.zheergenfx;

import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public final class LightNetwork {
    private static boolean registered;
    private LightNetwork() {
    }
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar("1").playToClient(LightPacket.TYPE,LightPacket.STREAM_CODEC,LightPacket::handle);
            registered = true;
        }
    }
    public static void send(ServerLevel level, LightPacket packet) {
        if (registered && packet.valid()) PacketDistributor.sendToPlayersNear(level,null,packet.origin().x,packet.origin().y,packet.origin().z,64.0,packet);
    }
}
