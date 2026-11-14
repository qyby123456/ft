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
package net.farmingtales.ftboss.qingyilight;

import java.util.Optional;
import net.farmingtales.ftboss.FTBossMod;
import net.farmingtales.ftboss.qingyilight.LightPacket;
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
public final class LightNetwork {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel((ResourceLocation)new ResourceLocation("ftboss", "qingyi_native_lights"), () -> "1", string -> "1".equals(string) || NetworkRegistry.ABSENT.equals(string) || NetworkRegistry.ACCEPTVANILLA.equals(string), string -> "1".equals(string) || NetworkRegistry.ABSENT.equals(string) || NetworkRegistry.ACCEPTVANILLA.equals(string));
    private static boolean registered;
    private static boolean loggedFailure;

    private LightNetwork() {
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent fMLCommonSetupEvent) {
        fMLCommonSetupEvent.enqueueWork(() -> {
            if (!registered) {
                CHANNEL.registerMessage(0, LightPacket.class, LightPacket::encode, LightPacket::decode, LightPacket::handle, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
                registered = true;
            }
        });
    }

    public static void send(ServerLevel serverLevel, LightPacket lightPacket) {
        if (!registered || !lightPacket.valid()) {
            return;
        }
        for (ServerPlayer serverPlayer : serverLevel.m_6907_()) {
            try {
                if (serverPlayer instanceof FakePlayer || serverPlayer.f_8906_ == null || !CHANNEL.isRemotePresent(serverPlayer.f_8906_.f_9742_) || serverPlayer.m_20182_().m_82557_(lightPacket.origin()) > 4096.0) continue;
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), (Object)lightPacket);
            }
            catch (RuntimeException runtimeException) {
                if (loggedFailure) continue;
                loggedFailure = true;
                FTBossMod.LOGGER.warn("[FTBoss][QingyiLight] Cosmetic packet skipped; native combat continues", (Throwable)runtimeException);
            }
        }
    }
}

