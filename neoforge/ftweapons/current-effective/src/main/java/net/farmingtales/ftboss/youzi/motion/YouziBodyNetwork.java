package net.farmingtales.ftboss.youzi.motion;

import java.util.Map;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.entity.EasterBossEntity;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(
    modid = "ftboss",
    bus = Bus.MOD
)
public final class YouziBodyNetwork {

    private static final Map<EasterBossEntity, Long> SUMMONS = new WeakHashMap<>();
    private static boolean registered;

    @SubscribeEvent
    public static void setup(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar("1").optional().playToClient(YouziBodyPacket.TYPE, YouziBodyPacket.STREAM_CODEC, YouziBodyPacket::handle);
            registered = true;
        }
    }

    public static void summoned(boolean var0, EasterBossEntity var1, EasterBossEntity var2) {
        if (var0
            && registered
            && var1.getVariant() == 0
            && !var1.isMini()
            && var1.isAlive()
            && var2 != null
            && var2.isMini()
            && var2.getVariant() == 0
            && var1.level() instanceof ServerLevel var3) {
            long var9 = var3.getGameTime();
            if (SUMMONS.getOrDefault(var1, -1L) != var9) {
                SUMMONS.put(var1, var9);
                YouziBodyPacket var6 = new YouziBodyPacket(var3.dimension().location().toString(), var1.getId(), var1.getUUID(), var9);

                for (ServerPlayer var8 : var3.players()) {
                    if (var8.connection != null && var8.distanceToSqr(var1) <= 4096.0 && var8.connection.hasChannel(YouziBodyPacket.TYPE)) {
                        PacketDistributor.sendToPlayer(var8, var6);
                    }
                }
            }
        }
    }
}
