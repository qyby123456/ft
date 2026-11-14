package net.farmingtales.ftboss.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkRegistry.ChannelBuilder;
import net.minecraftforge.network.simple.SimpleChannel;

public final class BossBarNetwork {
   private static int discriminator;
   private static final SimpleChannel CHANNEL = ChannelBuilder.named(new ResourceLocation("ftboss", "boss_bar_feed"))
      .networkProtocolVersion(() -> "1")
      .clientAcceptedVersions("1"::equals)
      .serverAcceptedVersions("1"::equals)
      .simpleChannel();
   private static boolean registered;

   private BossBarNetwork() {
   }

   public static synchronized void register() {
      if (!registered) {
         registered = true;
         CHANNEL.messageBuilder(BossBarFeedPacket.class, discriminator++, NetworkDirection.PLAY_TO_CLIENT)
            .encoder(BossBarFeedPacket::encode)
            .decoder(BossBarFeedPacket::decode)
            .consumerMainThread(BossBarFeedPacket::handle)
            .add();
      }
   }

   public static void send(ServerPlayer player, BossBarFeedPacket packet) {
      if (player != null && packet != null) {
         CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
      }
   }
}
