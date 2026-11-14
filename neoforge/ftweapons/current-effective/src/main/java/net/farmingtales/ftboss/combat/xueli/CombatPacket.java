package net.farmingtales.ftboss.combat.xueli;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.farmingtales.ftboss.client.xueli_combat.XueliCombatRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public record CombatPacket(
    ResourceLocation dimension,
    int ownerId,
    UUID owner,
    int kind,
    long stamp,
    Vec3 origin,
    double radius,
    int duration,
    double progress,
    float yaw,
    List<CombatPacket.Anchor> guards
) implements CustomPacketPayload {
    public static final Type<CombatPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "xueli_combat"));
    public static final StreamCodec<FriendlyByteBuf, CombatPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), CombatPacket::decode);

    @Override
    public Type<CombatPacket> type() {
        return TYPE;
    }

    private static boolean position(Vec3 var0) {
        return var0 != null
            && Double.isFinite(var0.x)
            && Double.isFinite(var0.y)
            && Double.isFinite(var0.z)
            && Math.abs(var0.x) <= 3.0E7
            && Math.abs(var0.z) <= 3.0E7
            && Math.abs(var0.y) < 4096.0;
    }

    public boolean valid() {
        return this.dimension != null
            && this.ownerId >= 0
            && this.owner != null
            && this.stamp >= 0L
            && Float.isFinite(this.yaw)
            && position(this.origin)
            && this.guards != null
            && CombatRules.valid(this.kind, this.duration, this.radius, this.progress, this.guards.size())
            && this.guards.stream().allMatch(var0 -> var0.entityId() >= 0 && var0.uuid() != null && position(var0.position()));
    }

    private static void point(FriendlyByteBuf var0, Vec3 var1) {
        var0.writeDouble(var1.x);
        var0.writeDouble(var1.y);
        var0.writeDouble(var1.z);
    }

    private static Vec3 point(FriendlyByteBuf var0) {
        return new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble());
    }

    public static void encode(CombatPacket var0, FriendlyByteBuf var1) {
        var1.writeResourceLocation(var0.dimension());
        var1.writeVarInt(var0.ownerId());
        var1.writeUUID(var0.owner());
        var1.writeByte(var0.kind());
        var1.writeLong(var0.stamp());
        point(var1, var0.origin());
        var1.writeDouble(var0.radius());
        var1.writeVarInt(var0.duration());
        var1.writeDouble(var0.progress());
        var1.writeFloat(var0.yaw());
        var1.writeByte(var0.guards().size());

        for (CombatPacket.Anchor var3 : var0.guards()) {
            var1.writeVarInt(var3.entityId());
            var1.writeUUID(var3.uuid());
            point(var1, var3.position());
        }
    }

    public static CombatPacket decode(FriendlyByteBuf var0) {
        ResourceLocation var1 = var0.readResourceLocation();
        int var2 = var0.readVarInt();
        UUID var3 = var0.readUUID();
        short var4 = var0.readUnsignedByte();
        long var5 = var0.readLong();
        Vec3 var7 = point(var0);
        double var8 = var0.readDouble();
        int var10 = var0.readVarInt();
        double var11 = var0.readDouble();
        float var13 = var0.readFloat();
        short var14 = var0.readUnsignedByte();
        if (var14 > 6) {
            throw new IllegalArgumentException("Unbounded maid snapshot");
        } else {
            List<CombatPacket.Anchor> var15 = new ArrayList<>();

            for (int var16 = 0; var16 < var14; var16++) {
                var15.add(new CombatPacket.Anchor(var0.readVarInt(), var0.readUUID(), point(var0)));
            }

            CombatPacket var17 = new CombatPacket(var1, var2, var3, var4, var5, var7, var8, var10, var11, var13, List.copyOf(var15));
            if (!var17.valid()) {
                throw new IllegalArgumentException("Invalid Xueli combat snapshot");
            } else {
                return var17;
            }
        }
    }

    public static void handle(CombatPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND) {
            var1.enqueueWork(() -> XueliCombatRenderer.accept(var0));
        }
    }

    public static record Anchor(int entityId, UUID uuid, Vec3 position) {
    }
}
