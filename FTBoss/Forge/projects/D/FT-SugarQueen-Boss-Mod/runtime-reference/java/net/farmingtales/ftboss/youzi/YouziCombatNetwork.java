/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.youzi;

import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;
import net.farmingtales.ftboss.youzi.YouziCombatPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class YouziCombatNetwork {
    private static final String VERSION = "2";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "youzi_combat_fx"), () -> "2", string -> VERSION.equals(string) || NetworkRegistry.ABSENT.equals(string) || NetworkRegistry.ACCEPTVANILLA.equals(string), string -> VERSION.equals(string) || NetworkRegistry.ABSENT.equals(string) || NetworkRegistry.ACCEPTVANILLA.equals(string));
    private static boolean registered;
    private static final Map<ServerLevel, Budget> BUDGETS;

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(YouziCombatNetwork::register);
    }

    public static synchronized void register() {
        if (registered) {
            return;
        }
        CHANNEL.registerMessage(0, YouziCombatPacket.class, YouziCombatPacket::encode, YouziCombatPacket::decode, YouziCombatPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
        registered = true;
    }

    public static void send(ServerLevel serverLevel, YouziCombatPacket youziCombatPacket) {
        int n;
        if (!registered) {
            return;
        }
        long l = serverLevel.m_46467_();
        Budget budget = BUDGETS.get(serverLevel);
        int n2 = n = budget != null && budget.tick() == l ? budget.sent() : 0;
        if (n >= 256) {
            return;
        }
        BUDGETS.put(serverLevel, new Budget(l, n + 1));
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (Math.min(serverPlayer.m_20275_(youziCombatPacket.origin().coordinateX(), youziCombatPacket.origin().coordinateY(), youziCombatPacket.origin().coordinateZ()), serverPlayer.m_20275_(youziCombatPacket.target().coordinateX(), youziCombatPacket.target().coordinateY(), youziCombatPacket.target().coordinateZ())) > 4096.0 || serverPlayer.f_8906_ == null || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)youziCombatPacket);
        }
    }

    static {
        BUDGETS = new WeakHashMap<ServerLevel, Budget>();
    }

    private record Budget(long tick, int sent) {
    }
}

