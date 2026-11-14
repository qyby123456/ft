/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.dandannativelights;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.dandannativelights.client.LightRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

public record LightPacket(ResourceLocation dimension, Vec3 origin, float yaw, float radius, int kind, int ticks, int index, int ownerId, UUID owner) {
    public boolean valid() {
        return this.kind >= 0 && this.kind <= 12 && this.ticks > 0 && this.ticks <= 200 && this.index >= 0 && this.index < 64 && this.owner != null && Float.isFinite(this.yaw) && Float.isFinite(this.radius) && this.radius > 0.0f && this.radius <= 48.0f && Double.isFinite(this.origin.f_82479_) && Double.isFinite(this.origin.f_82480_) && Double.isFinite(this.origin.f_82481_);
    }

    public static void encode(LightPacket lightPacket, FriendlyByteBuf friendlyByteBuf) {
        friendlyByteBuf.m_130085_(lightPacket.dimension());
        friendlyByteBuf.writeDouble(lightPacket.origin().f_82479_);
        friendlyByteBuf.writeDouble(lightPacket.origin().f_82480_);
        friendlyByteBuf.writeDouble(lightPacket.origin().f_82481_);
        friendlyByteBuf.writeFloat(lightPacket.yaw());
        friendlyByteBuf.writeFloat(lightPacket.radius());
        friendlyByteBuf.writeByte(lightPacket.kind());
        friendlyByteBuf.writeShort(lightPacket.ticks());
        friendlyByteBuf.writeByte(lightPacket.index());
        friendlyByteBuf.writeInt(lightPacket.ownerId());
        friendlyByteBuf.m_130077_(lightPacket.owner());
    }

    public static LightPacket decode(FriendlyByteBuf friendlyByteBuf) {
        return new LightPacket(friendlyByteBuf.m_130281_(), new Vec3(friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble(), friendlyByteBuf.readDouble()), friendlyByteBuf.readFloat(), friendlyByteBuf.readFloat(), friendlyByteBuf.readUnsignedByte(), friendlyByteBuf.readUnsignedShort(), friendlyByteBuf.readUnsignedByte(), friendlyByteBuf.readInt(), friendlyByteBuf.m_130259_());
    }

    public static void handle(LightPacket lightPacket, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();
        if (lightPacket.valid()) {
            context.enqueueWork(() -> LightRenderer.receive(lightPacket));
        }
        context.setPacketHandled(true);
    }
}

