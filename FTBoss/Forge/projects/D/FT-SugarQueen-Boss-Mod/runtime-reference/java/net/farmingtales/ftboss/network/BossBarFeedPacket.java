/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.network;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.BossBarOverlay;
import net.farmingtales.ftboss.util.BossBarTheme;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record BossBarFeedPacket(List<Entry> entries) {
    public static final int MAX_ENTRIES = 24;
    public static final int MAX_THEME_ID = 64;
    public static final int MAX_MANOR_VARIANTS = 16;

    public static void encode(BossBarFeedPacket packet, FriendlyByteBuf buffer) {
        List values = packet != null && packet.entries != null ? packet.entries : List.of();
        int count = Math.min(values.size(), 24);
        buffer.m_130130_(count);
        for (int index = 0; index < count; ++index) {
            Entry entry = (Entry)values.get(index);
            buffer.m_130130_(entry.entityId());
            buffer.m_130070_(BossBarFeedPacket.trim(entry.themeId(), 64));
            buffer.writeFloat(entry.ratio());
            buffer.m_130130_(Math.max(0, entry.hp()));
            buffer.m_130130_(Math.max(0, entry.maxHp()));
            buffer.writeByte(entry.phase());
            buffer.m_130130_(Math.max(0, entry.minions()));
            buffer.writeBoolean(entry.finale());
            buffer.writeFloat(entry.cast());
            buffer.writeByte(entry.manorVariant());
            buffer.writeByte(entry.manorCount());
            buffer.m_130130_(entry.defeatedMask());
            byte[] variants = entry.manorVariants() == null ? new byte[]{} : entry.manorVariants();
            int variantCount = Math.min(variants.length, 16);
            buffer.m_130130_(variantCount);
            for (int variantIndex = 0; variantIndex < variantCount; ++variantIndex) {
                buffer.writeByte((int)variants[variantIndex]);
            }
        }
    }

    public static BossBarFeedPacket decode(FriendlyByteBuf buffer) {
        int count = Math.max(0, Math.min(24, buffer.m_130242_()));
        ArrayList<Entry> values = new ArrayList<Entry>(count);
        for (int index = 0; index < count; ++index) {
            int entityId = buffer.m_130242_();
            String themeId = BossBarFeedPacket.trim(buffer.m_130136_(64), 64);
            float ratio = buffer.readFloat();
            int hp = Math.max(0, buffer.m_130242_());
            int maxHp = Math.max(0, buffer.m_130242_());
            int phase = Math.max(0, buffer.readByte());
            int minions = Math.max(0, buffer.m_130242_());
            boolean finale = buffer.readBoolean();
            float cast = buffer.readFloat();
            byte manorVariant = buffer.readByte();
            int manorCount = buffer.readByte() & 0xFF;
            int defeatedMask = buffer.m_130242_();
            int variantCount = Math.max(0, Math.min(16, buffer.m_130242_()));
            byte[] variants = new byte[variantCount];
            for (int variantIndex = 0; variantIndex < variantCount; ++variantIndex) {
                variants[variantIndex] = buffer.readByte();
            }
            if (entityId < 0 || themeId.isEmpty() || BossBarTheme.byId(themeId) == null) continue;
            values.add(new Entry(entityId, themeId, BossBarTheme.clamp01(ratio), hp, maxHp, phase, minions, finale, BossBarTheme.clamp01(cast), manorVariant, manorCount, defeatedMask, variants));
        }
        return new BossBarFeedPacket(List.copyOf(values));
    }

    public static void handle(BossBarFeedPacket packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
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
        }
        return value.length() <= max ? value : value.substring(0, max);
    }

    public record Entry(int entityId, String themeId, float ratio, int hp, int maxHp, int phase, int minions, boolean finale, float cast, int manorVariant, int manorCount, int defeatedMask, byte[] manorVariants) {
    }
}

