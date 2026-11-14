package net.farmingtales.ftboss.dandaniron;

import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class IronNetwork {

    private static boolean registered;

    private IronNetwork() {
    }

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar("1").playToClient(IronPacket.TYPE, IronPacket.STREAM_CODEC, IronPacket::handle);
            registered = true;
        }
    }

    public static void send(ServerLevel var0, IronPacket var1) {
        if (registered && var1.valid()) {
            for (ServerPlayer var3 : var0.players()) {
                if (var3.connection != null
                    && !(var3 instanceof FakePlayer)
                    && var3.connection.hasChannel(IronPacket.TYPE)
                    && (var3.distanceToSqr(var1.visual().start()) <= 9216.0 || var3.distanceToSqr(var1.visual().end()) <= 9216.0)) {
                    PacketDistributor.sendToPlayer(var3, var1);
                }
            }
        }
    }
}
