package net.farmingtales.ftboss.dandannativelights;

import java.util.UUID;
import net.farmingtales.ftboss.dandannativelights.client.LightRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LightPacket(ResourceLocation dimension, Vec3 origin, float yaw, float radius, int kind, int ticks, int index, int ownerId, UUID owner) implements CustomPacketPayload {
    public static final Type<LightPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "dandan_native_lights"));
    public static final StreamCodec<FriendlyByteBuf, LightPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), LightPacket::decode);

    @Override
    public Type<LightPacket> type() {
        return TYPE;
    }

    public boolean valid() {
        return this.kind >= 0
            && this.kind <= 12
            && this.ticks > 0
            && this.ticks <= 200
            && this.index >= 0
            && this.index < 64
            && this.owner != null
            && Float.isFinite(this.yaw)
            && Float.isFinite(this.radius)
            && this.radius > 0.0F
            && this.radius <= 48.0F
            && Double.isFinite(this.origin.x)
            && Double.isFinite(this.origin.y)
            && Double.isFinite(this.origin.z);
    }

    public static void encode(LightPacket var0, FriendlyByteBuf var1) {
        var1.writeResourceLocation(var0.dimension());
        var1.writeDouble(var0.origin().x);
        var1.writeDouble(var0.origin().y);
        var1.writeDouble(var0.origin().z);
        var1.writeFloat(var0.yaw());
        var1.writeFloat(var0.radius());
        var1.writeByte(var0.kind());
        var1.writeShort(var0.ticks());
        var1.writeByte(var0.index());
        var1.writeInt(var0.ownerId());
        var1.writeUUID(var0.owner());
    }

    public static LightPacket decode(FriendlyByteBuf var0) {
        return new LightPacket(
            var0.readResourceLocation(),
            new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble()),
            var0.readFloat(),
            var0.readFloat(),
            var0.readUnsignedByte(),
            var0.readUnsignedShort(),
            var0.readUnsignedByte(),
            var0.readInt(),
            var0.readUUID()
        );
    }

    public static void handle(LightPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND && var0.valid()) {
            var1.enqueueWork(() -> LightRenderer.receive(var0));
        }
    }
}
