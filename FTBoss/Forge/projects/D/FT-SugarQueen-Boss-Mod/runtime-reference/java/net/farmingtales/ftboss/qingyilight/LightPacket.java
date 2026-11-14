/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.qingyilight;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.qingyilight.LightRules;
import net.farmingtales.ftboss.qingyilight.client.LightClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public record LightPacket(int kind, int phase, int ownerId, UUID owner, ResourceLocation dimension, long born, Vec3 origin, Vec3 direction, double radius, int duration, int pulse, int entityId, UUID entity) {
    public boolean valid() {
        return LightRules.valid(this.kind, this.phase, this.duration, this.radius, this.pulse) && this.ownerId >= 0 && this.owner != null && this.dimension != null && this.born >= 0L && this.origin != null && this.direction != null && LightRules.point(this.origin.f_82479_, this.origin.f_82480_, this.origin.f_82481_) && LightRules.direction(this.direction.f_82479_, this.direction.f_82480_, this.direction.f_82481_) && this.entityId >= -1 && (this.entityId < 0 ? this.entity == null : this.entity != null);
    }

    public static void encode(LightPacket lightPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(lightPacket.kind);
        friendlyByteBuf.writeByte(lightPacket.phase);
        friendlyByteBuf.m_130130_(lightPacket.ownerId);
        friendlyByteBuf.m_130077_(lightPacket.owner);
        friendlyByteBuf.m_130085_(lightPacket.dimension);
        friendlyByteBuf.writeLong(lightPacket.born);
        for (Vec3 vec3 : new Vec3[]{lightPacket.origin, lightPacket.direction}) {
            friendlyByteBuf.writeDouble(vec3.f_82479_);
            friendlyByteBuf.writeDouble(vec3.f_82480_);
            friendlyByteBuf.writeDouble(vec3.f_82481_);
        }
        friendlyByteBuf.writeDouble(lightPacket.radius);
        friendlyByteBuf.m_130130_(lightPacket.duration);
        friendlyByteBuf.writeByte(lightPacket.pulse);
        friendlyByteBuf.writeInt(lightPacket.entityId);
        if (lightPacket.entityId >= 0) {
            friendlyByteBuf.m_130077_(lightPacket.entity);
        }
    }

    public static LightPacket decode(FriendlyByteBuf friendlyByteBuf) {
        short s = friendlyByteBuf.readUnsignedByte();
        short s2 = friendlyByteBuf.readUnsignedByte();
        int n = friendlyByteBuf.m_130242_();
        UUID uUID = friendlyByteBuf.m_130259_();
        ResourceLocation resourceLocation = friendlyByteBuf.m_130281_();
        long l = friendlyByteBuf.readLong();
        Vec3 vec3 = new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
        Vec3 vec32 = new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble());
        double d = friendlyByteBuf.readDouble();
        int n2 = friendlyByteBuf.m_130242_();
        short s3 = friendlyByteBuf.readUnsignedByte();
        int n3 = friendlyByteBuf.readInt();
        UUID uUID2 = n3 >= 0 ? friendlyByteBuf.m_130259_() : null;
        return new LightPacket(s, s2, n, uUID, resourceLocation, l, vec3, vec32, d, n2, s3, n3, uUID2);
    }

    public static void handle(LightPacket lightPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        context.enqueueWork(() -> {
            if (lightPacket.valid()) {
                LightClient.accept(lightPacket);
            }
        });
        context.setPacketHandled(true);
    }
}

