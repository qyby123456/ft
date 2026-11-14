/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.zheergenfx;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.zheergenfx.client.LightClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public record LightPacket(int kind, int ownerId, UUID owner, ResourceLocation dimension, long born, Vec3 origin, Vec3 direction, double radius, int duration, int index) {
    public boolean valid() {
        return this.kind >= 0 && this.kind <= 13 && this.ownerId >= 0 && this.owner != null && this.dimension != null && this.born >= 0L && LightPacket.finite(this.origin) && LightPacket.finite(this.direction) && Double.isFinite(this.radius) && this.radius > 0.0 && this.radius <= 32.0 && this.duration >= 1 && this.duration <= 140 && this.index >= 0 && this.index <= 5;
    }

    private static boolean finite(Vec3 vec3) {
        return vec3 != null && Double.isFinite(vec3.f_82479_) && Double.isFinite(vec3.f_82480_) && Double.isFinite(vec3.f_82481_) && Math.abs(vec3.f_82479_) <= 3.0E7 && Math.abs(vec3.f_82481_) <= 3.0E7 && Math.abs(vec3.f_82480_) <= 4096.0;
    }

    public static void encode(LightPacket lightPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.writeByte(lightPacket.kind);
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
        friendlyByteBuf.writeByte(lightPacket.index);
    }

    public static LightPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new LightPacket(friendlyByteBuf.readUnsignedByte(), friendlyByteBuf.m_130242_(), friendlyByteBuf.m_130259_(), friendlyByteBuf.m_130281_(), friendlyByteBuf.readLong(), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), friendlyByteBuf.readDouble(), friendlyByteBuf.m_130242_(), friendlyByteBuf.readUnsignedByte());
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

