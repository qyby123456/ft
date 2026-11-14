/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.BossMusicClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record BossMusicPacket(long revision, UUID encounter, ResourceLocation dimension, List<ResourceLocation> tracks) {
    public BossMusicPacket {
        tracks = List.copyOf(tracks);
        if (revision < 0L || tracks.size() > 3 || tracks.stream().anyMatch(track -> !track.m_135827_().equals("ftboss") || !track.m_135815_().startsWith("boss_music_"))) {
            throw new IllegalArgumentException("Invalid boss music state");
        }
    }

    public static void encode(BossMusicPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130103_(packet.revision);
        buffer.m_130077_(packet.encounter);
        buffer.m_130085_(packet.dimension);
        buffer.m_130130_(packet.tracks.size());
        for (ResourceLocation track : packet.tracks) {
            buffer.m_130085_(track);
        }
    }

    public static BossMusicPacket decode(FriendlyByteBuf buffer) {
        long revision = buffer.m_130258_();
        UUID encounter = buffer.m_130259_();
        ResourceLocation dimension = buffer.m_130281_();
        int count = buffer.m_130242_();
        if (count < 0 || count > 3) {
            throw new IllegalArgumentException("Invalid boss music playlist length");
        }
        ArrayList<ResourceLocation> tracks = new ArrayList<ResourceLocation>();
        for (int index = 0; index < count; ++index) {
            tracks.add(buffer.m_130281_());
        }
        return new BossMusicPacket(revision, encounter, dimension, tracks);
    }

    public static void handle(BossMusicPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            BossMusicClient.receive(packet);
        }
    }
}

