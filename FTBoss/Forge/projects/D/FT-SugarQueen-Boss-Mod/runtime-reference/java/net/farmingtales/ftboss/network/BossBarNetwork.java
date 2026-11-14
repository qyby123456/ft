/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkDirection
 *  net.minecraftforge.network.NetworkRegistry$ChannelBuilder
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.network.simple.SimpleChannel
 */
package net.farmingtales.ftboss.network;

import net.farmingtales.ftboss.network.BossBarFeedPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BossBarNetwork {
    private static final String VERSION = "1";
    private static int discriminator;
    private static final SimpleChannel CHANNEL;
    private static boolean registered;

    private BossBarNetwork() {
    }

    public static synchronized void register() {
        if (!registered) {
            registered = true;
            CHANNEL.messageBuilder(BossBarFeedPacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT).encoder(BossBarFeedPacket::encode).decoder(BossBarFeedPacket::decode).consumerMainThread(BossBarFeedPacket::handle).add();
        }
    }

    public static void send(ServerPlayer player, BossBarFeedPacket packet) {
        if (player != null && packet != null) {
            CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
        }
    }

    static {
        CHANNEL = NetworkRegistry.ChannelBuilder.named((ResourceLocation)new ResourceLocation("ftboss", "boss_bar_feed")).networkProtocolVersion(() -> VERSION).clientAcceptedVersions(VERSION::equals).serverAcceptedVersions(VERSION::equals).simpleChannel();
    }
}

