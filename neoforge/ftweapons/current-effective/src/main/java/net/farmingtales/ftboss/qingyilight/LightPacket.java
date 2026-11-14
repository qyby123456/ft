package net.farmingtales.ftboss.qingyilight;

import java.util.UUID;
import net.farmingtales.ftboss.qingyilight.client.LightClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record LightPacket(
    int kind,
    int phase,
    int ownerId,
    UUID owner,
    ResourceLocation dimension,
    long born,
    Vec3 origin,
    Vec3 direction,
    double radius,
    int duration,
    int pulse,
    int entityId,
    UUID entity
) implements CustomPacketPayload {
    public static final Type<LightPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "qingyi_native_lights"));
    public static final StreamCodec<FriendlyByteBuf, LightPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), LightPacket::decode);

    @Override
    public Type<LightPacket> type() {
        return TYPE;
    }

    public boolean valid() {
        return LightRules.valid(this.kind, this.phase, this.duration, this.radius, this.pulse)
            && this.ownerId >= 0
            && this.owner != null
            && this.dimension != null
            && this.born >= 0L
            && this.origin != null
            && this.direction != null
            && LightRules.point(this.origin.x, this.origin.y, this.origin.z)
            && LightRules.direction(this.direction.x, this.direction.y, this.direction.z)
            && this.entityId >= -1
            && (this.entityId < 0 ? this.entity == null : this.entity != null);
    }

    public static void encode(LightPacket var0, FriendlyByteBuf var1) {
        var1.writeByte(var0.kind);
        var1.writeByte(var0.phase);
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
        var1.writeByte(var0.pulse);
        var1.writeInt(var0.entityId);
        if (var0.entityId >= 0) {
            var1.writeUUID(var0.entity);
        }
    }

    public static LightPacket decode(FriendlyByteBuf var0) {
        short var1 = var0.readUnsignedByte();
        short var2 = var0.readUnsignedByte();
        int var3 = var0.readVarInt();
        UUID var4 = var0.readUUID();
        ResourceLocation var5 = var0.readResourceLocation();
        long var6 = var0.readLong();
        Vec3 var8 = new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble());
        Vec3 var9 = new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble());
        double var10 = var0.readDouble();
        int var12 = var0.readVarInt();
        short var13 = var0.readUnsignedByte();
        int var14 = var0.readInt();
        UUID var15 = var14 >= 0 ? var0.readUUID() : null;
        return new LightPacket(var1, var2, var3, var4, var5, var6, var8, var9, var10, var12, var13, var14, var15);
    }

    public static void handle(LightPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND) {
            var1.enqueueWork(() -> {
                if (var0.valid()) {
                    LightClient.accept(var0);
                }
            });
        }
    }
}
