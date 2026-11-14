package net.farmingtales.ftboss.combat.xueli;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class CombatNetwork {
    private static boolean registered;

    private CombatNetwork() {
    }

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent var0) {
        if (!registered) {
            registered = true;
            var0.registrar("1").optional().playToClient(CombatPacket.TYPE, CombatPacket.STREAM_CODEC, CombatPacket::handle);
        }
    }
}
