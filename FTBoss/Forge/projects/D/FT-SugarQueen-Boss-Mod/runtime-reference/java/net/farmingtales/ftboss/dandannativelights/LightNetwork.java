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
package net.farmingtales.ftboss.dandannativelights;

import java.util.Optional;
import net.farmingtales.ftboss.dandannativelights.LightPacket;
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
public final class LightNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "dandan_native_lights"), () -> "1", "1"::equals, "1"::equals);
    private static boolean registered;

    private LightNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(() -> {
            if (registered) {
                return;
            }
            CHANNEL.registerMessage(0, LightPacket.class, LightPacket::encode, LightPacket::decode, LightPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
            registered = true;
        });
    }

    public static void send(ServerLevel serverLevel, LightPacket lightPacket) {
        if (!registered || !lightPacket.valid()) {
            return;
        }
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            if (!(serverPlayer.m_20238_(lightPacket.origin()) <= 9216.0) || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_)) continue;
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)lightPacket);
        }
    }
}

