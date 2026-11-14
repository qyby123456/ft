package net.farmingtales.ftboss.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.BossBarOverlay;
import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent.Context;

public record BossBarFeedPacket(List<BossBarFeedPacket.Entry> entries) {
   public static final int MAX_ENTRIES = 24;
   public static final int MAX_THEME_ID = 64;
   public static final int MAX_MANOR_VARIANTS = 16;

   public static void encode(BossBarFeedPacket packet, FriendlyByteBuf buffer) {
      List<BossBarFeedPacket.Entry> values = packet != null && packet.entries != null ? packet.entries : List.of();
      int count = Math.min(values.size(), 24);
      buffer.writeVarInt(count);

      for (int index = 0; index < count; index++) {
         BossBarFeedPacket.Entry entry = values.get(index);
         buffer.writeVarInt(entry.entityId());
         buffer.writeUtf(trim(entry.themeId(), 64));
         buffer.writeFloat(entry.ratio());
         buffer.writeVarInt(Math.max(0, entry.hp()));
         buffer.writeVarInt(Math.max(0, entry.maxHp()));
         buffer.writeByte(entry.phase());
         buffer.writeVarInt(Math.max(0, entry.minions()));
         buffer.writeBoolean(entry.finale());
         buffer.writeFloat(entry.cast());
         buffer.writeByte(entry.manorVariant());
         buffer.writeByte(entry.manorCount());
         buffer.writeVarInt(entry.defeatedMask());
         byte[] variants = entry.manorVariants() == null ? new byte[0] : entry.manorVariants();
         int variantCount = Math.min(variants.length, 16);
         buffer.writeVarInt(variantCount);

         for (int variantIndex = 0; variantIndex < variantCount; variantIndex++) {
            buffer.writeByte(variants[variantIndex]);
         }
      }
   }

   public static BossBarFeedPacket decode(FriendlyByteBuf buffer) {
      int count = Math.max(0, Math.min(24, buffer.readVarInt()));
      ArrayList<BossBarFeedPacket.Entry> values = new ArrayList<>(count);

      for (int index = 0; index < count; index++) {
         int entityId = buffer.readVarInt();
         String themeId = trim(buffer.readUtf(64), 64);
         float ratio = buffer.readFloat();
         int hp = Math.max(0, buffer.readVarInt());
         int maxHp = Math.max(0, buffer.readVarInt());
         int phase = Math.max(0, buffer.readByte());
         int minions = Math.max(0, buffer.readVarInt());
         boolean finale = buffer.readBoolean();
         float cast = buffer.readFloat();
         int manorVariant = buffer.readByte();
         int manorCount = buffer.readByte() & 255;
         int defeatedMask = buffer.readVarInt();
         int variantCount = Math.max(0, Math.min(16, buffer.readVarInt()));
         byte[] variants = new byte[variantCount];

         for (int variantIndex = 0; variantIndex < variantCount; variantIndex++) {
            variants[variantIndex] = buffer.readByte();
         }

         if (entityId >= 0 && !themeId.isEmpty() && BossBarTheme.byId(themeId) != null) {
            values.add(
               new BossBarFeedPacket.Entry(
                  entityId,
                  themeId,
                  BossBarTheme.clamp01(ratio),
                  hp,
                  maxHp,
                  phase,
                  minions,
                  finale,
                  BossBarTheme.clamp01(cast),
                  manorVariant,
                  manorCount,
                  defeatedMask,
                  variants
               )
            );
         }
      }

      return new BossBarFeedPacket(List.copyOf(values));
   }

   public static void handle(BossBarFeedPacket packet, Supplier<Context> supplier) {
      Context context = supplier.get();
      context.enqueueWork(() -> {
         if (FMLEnvironment.dist == Dist.CLIENT) {
            BossBarOverlay.receive(packet);
         }
      });
      context.setPacketHandled(true);
   }

   private static String trim(String value, int max) {
      if (value == null) {
         return "";
      } else {
         return value.length() <= max ? value : value.substring(0, max);
      }
   }

   public static record Entry(
      int entityId,
      String themeId,
      float ratio,
      int hp,
      int maxHp,
      int phase,
      int minions,
      boolean finale,
      float cast,
      int manorVariant,
      int manorCount,
      int defeatedMask,
      byte[] manorVariants
   ) {
   }
}
