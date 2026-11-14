/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.network;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ZhuniBlackwallVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class ZhuniBlackwallNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "zhuni_blackwall"), () -> "1", "1"::equals, "1"::equals);
    private static boolean registered;

    private ZhuniBlackwallNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(ZhuniBlackwallNetwork::register);
    }

    private static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(Breach.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(Breach::encode).decoder(Breach::decode).consumerMainThread(Breach::handle).add();
    }

    public static void show(ServerPlayer serverPlayer, int n, int n2) {
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)new Breach(serverPlayer.m_9236_().m_46472_().m_135782_(), n, n2));
    }

    public record Breach(ResourceLocation dimension, int seed, int count) {
        public static void encode(Breach breach, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130085_(breach.dimension);
            friendlyByteBuf.writeInt(breach.seed);
            friendlyByteBuf.m_130130_(breach.count);
        }

        public static Breach decode(FriendlyByteBuf friendlyByteBuf) {
            return new Breach(friendlyByteBuf.m_130281_(), friendlyByteBuf.readInt(), friendlyByteBuf.m_130242_());
        }

        public static void handle(Breach breach, Supplier<NetworkEvent.Context> supplier) {
            if (breach.count > 0 && breach.count <= 3) {
                DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ZhuniBlackwallVisual.accept(breach));
            }
            supplier.get().setPacketHandled(true);
        }
    }
}

