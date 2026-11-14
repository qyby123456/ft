package net.farmingtales.ftboss.qingyilight;

import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
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
    private static boolean loggedFailure;

    private LightNetwork() {
    }

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent var0) {
        if (!registered) {
            var0.registrar("1").optional().playToClient(LightPacket.TYPE, LightPacket.STREAM_CODEC, LightPacket::handle);
            registered = true;
        }
    }

    public static void send(ServerLevel var0, LightPacket var1) {
        if (registered && var1.valid()) {
            for (ServerPlayer var3 : var0.players()) {
                try {
                    if (!(var3 instanceof FakePlayer)
                        && var3.connection != null
                        && var3.connection.hasChannel(LightPacket.TYPE)
                        && !(var3.position().distanceToSqr(var1.origin()) > 4096.0)) {
                        PacketDistributor.sendToPlayer(var3, var1);
                    }
                } catch (RuntimeException var5) {
                    if (!loggedFailure) {
                        loggedFailure = true;
                        FTBossMod.LOGGER.warn("[FTBoss][QingyiLight] Cosmetic packet skipped; native combat continues", (Throwable)var5);
                    }
                }
            }
        }
    }
}
