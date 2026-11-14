/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
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
import net.farmingtales.ftboss.client.render.AppleBlastLightVisual;
import net.farmingtales.ftboss.util.AppleBlastLightRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
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
public final class AppleBlastNetwork {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "apple_blast"), () -> "1", "1"::equals, "1"::equals);
    private static boolean registered;

    private AppleBlastNetwork() {
    }

    @SubscribeEvent
    public static void commonSetup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(AppleBlastNetwork::register);
    }

    private static synchronized void register() {
        if (registered) {
            return;
        }
        registered = true;
        CHANNEL.messageBuilder(Burst.class, 0, NetworkDirection.PLAY_TO_CLIENT).encoder(Burst::encode).decoder(Burst::decode).consumerMainThread(Burst::handle).add();
    }

    public static void show(ServerLevel serverLevel, double d, double d2, double d3, double d4) {
        if (serverLevel == null || !AppleBlastLightRules.valid(d, d2, d3, d4)) {
            return;
        }
        Burst burst = new Burst(serverLevel.m_46472_().m_135782_(), d, d2, d3, d4);
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!(serverPlayer.m_20275_(d, d2, d3) <= 4096.0)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)burst);
        }
    }

    public record Burst(ResourceLocation dimension, double x, double y, double z, double radius) {
        public boolean valid() {
            return this.dimension != null && AppleBlastLightRules.valid(this.x, this.y, this.z, this.radius);
        }

        public static void encode(Burst burst, FriendlyByteBuf friendlyByteBuf) {
            friendlyByteBuf.m_130085_(burst.dimension);
            friendlyByteBuf.writeDouble(burst.x).writeDouble(burst.y).writeDouble(burst.z).writeDouble(burst.radius);
        }

        public static Burst decode(FriendlyByteBuf friendlyByteBuf) {
            return new Burst(friendlyByteBuf.m_130281_(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
        }

        public static void handle(Burst burst, Supplier<NetworkEvent.Context> supplier) {
            if (burst.valid()) {
                DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> AppleBlastLightVisual.accept(burst));
            }
            supplier.get().setPacketHandled(true);
        }
    }
}

