package net.farmingtales.ftboss.zheergenfx;

import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.farmingtales.ftboss.zheergenfx.client.LightClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;


public record LightPacket(
    int kind, int ownerId, UUID owner, ResourceLocation dimension, long born, Vec3 origin, Vec3 direction, double radius, int duration, int index
) implements CustomPacketPayload {
    public static final Type<LightPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "zheergen_skill_lights"));
    public static final StreamCodec<FriendlyByteBuf,LightPacket> STREAM_CODEC = StreamCodec.of((buffer,packet) -> encode(packet,buffer),LightPacket::decode);
    public Type<LightPacket> type() { return TYPE; }

    public boolean valid() {
        return this.kind >= 0
            && this.kind <= 13
            && this.ownerId >= 0
            && this.owner != null
            && this.dimension != null
            && this.born >= 0L
            && finite(this.origin)
            && finite(this.direction)
            && Double.isFinite(this.radius)
            && this.radius > 0.0
            && this.radius <= 32.0
            && this.duration >= 1
            && this.duration <= 140
            && this.index >= 0
            && this.index <= 5;
    }

    private static boolean finite(Vec3 var0) {
        return var0 != null
            && Double.isFinite(var0.x)
            && Double.isFinite(var0.y)
            && Double.isFinite(var0.z)
            && Math.abs(var0.x) <= 3.0E7
            && Math.abs(var0.z) <= 3.0E7
            && Math.abs(var0.y) <= 4096.0;
    }

    public static void encode(LightPacket var0, FriendlyByteBuf var1) {
        var1.writeByte(var0.kind);
        var1.writeVarInt(var0.ownerId);
        var1.writeUUID(var0.owner);
        var1.writeResourceLocation(var0.dimension);
        var1.writeLong(var0.born);

        for (Vec3 var5 : new Vec3[]{var0.origin, var0.direction}) {
            var1.writeDouble(var5.x);
            var1.writeDouble(var5.y);
            var1.writeDouble(var5.z);
        }

        var1.writeDouble(var0.radius);
        var1.writeVarInt(var0.duration);
        var1.writeByte(var0.index);
    }

    public static LightPacket decode(FriendlyByteBuf var0) {
        return new LightPacket(
            var0.readUnsignedByte(),
            var0.readVarInt(),
            var0.readUUID(),
            var0.readResourceLocation(),
            var0.readLong(),
            new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble()),
            new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble()),
            var0.readDouble(),
            var0.readVarInt(),
            var0.readUnsignedByte()
        );
    }

    public static void handle(LightPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> { if (packet.valid()) LightClient.accept(packet); });
    }
}
