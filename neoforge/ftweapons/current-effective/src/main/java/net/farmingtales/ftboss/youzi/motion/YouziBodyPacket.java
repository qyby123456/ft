package net.farmingtales.ftboss.youzi.motion;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record YouziBodyPacket(String dimension, int actorId, UUID actor, long tick) implements CustomPacketPayload {
    public static final Type<YouziBodyPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_body"));
    public static final StreamCodec<FriendlyByteBuf, YouziBodyPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), YouziBodyPacket::decode);

    @Override
    public Type<YouziBodyPacket> type() {
        return TYPE;
    }

    public YouziBodyPacket(String dimension, int actorId, UUID actor, long tick) {
        if (dimension != null && dimension.length() <= 256 && actor != null && tick >= 0L) {
            this.dimension = dimension;
            this.actorId = actorId;
            this.actor = actor;
            this.tick = tick;
        } else {
            throw new IllegalArgumentException("Invalid summon body packet");
        }
    }

    public static void encode(YouziBodyPacket var0, FriendlyByteBuf var1) {
        var1.writeUtf(var0.dimension(), 256);
        var1.writeVarInt(var0.actorId());
        var1.writeLong(var0.actor().getMostSignificantBits());
        var1.writeLong(var0.actor().getLeastSignificantBits());
        var1.writeLong(var0.tick());
    }

    public static YouziBodyPacket decode(FriendlyByteBuf var0) {
        return new YouziBodyPacket(var0.readUtf(256), var0.readVarInt(), new UUID(var0.readLong(), var0.readLong()), var0.readLong());
    }

    public static void handle(YouziBodyPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND) {
            var1.enqueueWork(() -> YouziBodyState.summon(var0));
        }

    }
}
