package net.farmingtales.ftboss.lemoncasting;

import io.netty.handler.codec.DecoderException;
import java.util.UUID;
import net.farmingtales.ftboss.lemoncasting.client.LemonCastingActor;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record CastingPacket(UUID owner, ResourceLocation dimension, CastingClock.Cue cue) implements CustomPacketPayload {
    public static final Type<CastingPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "lemon_cast_v1"));
    public static final StreamCodec<FriendlyByteBuf, CastingPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), CastingPacket::decode);

    @Override
    public Type<CastingPacket> type() {
        return TYPE;
    }

    public boolean valid() {
        return this.owner != null && this.dimension != null && CastingClock.valid(this.cue);
    }

    public static void encode(CastingPacket var0, FriendlyByteBuf var1) {
        var1.writeUUID(var0.owner());
        var1.writeResourceLocation(var0.dimension());
        CastingClock.Cue var2 = var0.cue();
        var1.writeUtf(var2.spell());
        var1.writeLong(var2.token());
        var1.writeLong(var2.started());
        var1.writeLong(var2.changed());
        var1.writeInt(var2.duration());
        var1.writeByte(var2.phase());
        var1.writeBoolean(var2.primary());
    }

    public static CastingPacket decode(FriendlyByteBuf var0) {
        CastingPacket var1 = new CastingPacket(
            var0.readUUID(),
            var0.readResourceLocation(),
            new CastingClock.Cue(
                var0.readUtf(32), var0.readLong(), var0.readLong(), var0.readLong(), var0.readInt(), var0.readUnsignedByte(), var0.readBoolean()
            )
        );
        if (!var1.valid()) {
            throw new DecoderException("Invalid Lemoncat casting cue");
        } else {
            return var1;
        }
    }

    public static void handle(CastingPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND && var0.valid()) {
            LemonCastingActor.receive(var0);
        }

    }
}
