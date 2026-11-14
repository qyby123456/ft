package net.farmingtales.ftboss.sound;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record BossMusicPacket(long revision, UUID encounter, ResourceLocation dimension, List<ResourceLocation> tracks) {
    public BossMusicPacket {
        tracks = List.copyOf(tracks);
        if (revision < 0 || tracks.size() > 3 || tracks.stream().anyMatch(track ->
                !track.getNamespace().equals("ftboss") || !track.getPath().startsWith("boss_music_"))) {
            throw new IllegalArgumentException("Invalid boss music state");
        }
    }

    public static void encode(BossMusicPacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarLong(packet.revision);
        buffer.writeUUID(packet.encounter);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.tracks.size());
        for (ResourceLocation track : packet.tracks) buffer.writeResourceLocation(track);
    }

    public static BossMusicPacket decode(FriendlyByteBuf buffer) {
        long revision = buffer.readVarLong();
        UUID encounter = buffer.readUUID();
        ResourceLocation dimension = buffer.readResourceLocation();
        int count = buffer.readVarInt();
        if (count < 0 || count > 3) throw new IllegalArgumentException("Invalid boss music playlist length");
        var tracks = new ArrayList<ResourceLocation>();
        for (int index = 0; index < count; index++) tracks.add(buffer.readResourceLocation());
        return new BossMusicPacket(revision, encounter, dimension, tracks);
    }

    public static void handle(BossMusicPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        if (FMLEnvironment.dist == Dist.CLIENT) net.farmingtales.ftboss.client.BossMusicClient.receive(packet);
    }
}
