/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.common.util.FakePlayer
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 *  net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.dandaniron;

import java.util.Optional;
import net.farmingtales.ftboss.dandaniron.IronPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

@Mod.EventBusSubscriber(modid="ftboss", bus=Mod.EventBusSubscriber.Bus.MOD)
public final class IronNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "dandan_iron_timeline"), () -> "1", "1"::equals, "1"::equals);
    private static boolean registered;

    private IronNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(() -> {
            if (registered) {
                return;
            }
            CHANNEL.registerMessage(0, IronPacket.class, IronPacket::encode, IronPacket::decode, IronPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
            registered = true;
        });
    }

    public static void send(ServerLevel serverLevel, IronPacket ironPacket) {
        if (!registered || !ironPacket.valid()) {
            return;
        }
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (serverPlayer.f_8906_ == null || serverPlayer instanceof FakePlayer || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_) || !(serverPlayer.m_20238_(ironPacket.visual().start()) <= 9216.0) && !(serverPlayer.m_20238_(ironPacket.visual().end()) <= 9216.0)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)ironPacket);
        }
    }
}

