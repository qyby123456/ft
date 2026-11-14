package net.farmingtales.ftboss.youzi;

import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public final class YouziCombatNetwork {
    private static final String VERSION = "2";
    private static boolean registered;
    private static final Map<ServerLevel, YouziCombatNetwork.Budget> BUDGETS = new WeakHashMap<>();

    @SubscribeEvent
    public static synchronized void register(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            event.registrar(VERSION).optional().playToClient(YouziCombatPacket.TYPE, YouziCombatPacket.STREAM_CODEC, YouziCombatPacket::handle);
            registered = true;
        }
    }

    public static void send(ServerLevel var0, YouziCombatPacket var1) {
        if (registered) {
            long var2 = var0.getGameTime();
            YouziCombatNetwork.Budget var4 = BUDGETS.get(var0);
            int var5 = var4 != null && var4.tick() == var2 ? var4.sent() : 0;
            if (var5 < 256) {
                BUDGETS.put(var0, new YouziCombatNetwork.Budget(var2, var5 + 1));

                for (ServerPlayer var7 : var0.players()) {
                    if (!(
                            Math.min(
                                    var7.distanceToSqr(var1.origin().coordinateX(), var1.origin().coordinateY(), var1.origin().coordinateZ()),
                                    var7.distanceToSqr(var1.target().coordinateX(), var1.target().coordinateY(), var1.target().coordinateZ())
                                )
                                > 4096.0
                        )
                        && var7.connection != null
                        && var7.connection.hasChannel(YouziCombatPacket.TYPE)) {
                        PacketDistributor.sendToPlayer(var7, var1);
                    }
                }
            }
        }
    }

    private static record Budget(long tick, int sent) {
    }
}
