package net.farmingtales.ftboss.lemoncasting;

import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.lemoncasting.CastingClock.Cue;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class CastingNetwork {


    private static boolean registered;

    private CastingNetwork() {
    }

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar("1").optional().playToClient(CastingPacket.TYPE, CastingPacket.STREAM_CODEC, CastingPacket::handle);
            registered = true;
        }
    }

    public static void send(LivingEntity var0, Cue var1) {
        if (var0.level() instanceof ServerLevel var2) {
            CastingPacket var8 = new CastingPacket(var0.getUUID(), var2.dimension().location(), var1);
            if (var8.valid()) {
                for (ServerPlayer var5 : var2.players()) {
                    if (!(var5 instanceof FakePlayer)
                        && var5.connection != null
                        && !(var5.distanceToSqr(var0) > 9216.0)
                        && var5.connection.hasChannel(CastingPacket.TYPE)) {
                        try {
                            PacketDistributor.sendToPlayer(var5, var8);
                        } catch (RuntimeException var7) {
                            FTBossMod.LOGGER.warn("[LemonCasting] Cue delivery skipped: {}", var7.toString());
                        }
                    }
                }
            }
        }
    }
}
