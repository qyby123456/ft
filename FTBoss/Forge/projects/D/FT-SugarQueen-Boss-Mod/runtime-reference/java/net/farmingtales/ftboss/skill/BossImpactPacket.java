/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.BossImpactVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record BossImpactPacket(ResourceLocation dimension, Vec3 position, Vec3 normal, float size, float strength, int color, boolean shield) {
    public boolean valid() {
        return this.dimension != null && this.position != null && this.normal != null && BossImpactPacket.validCoordinate(this.position.f_82479_) && BossImpactPacket.validCoordinate(this.position.f_82480_) && BossImpactPacket.validCoordinate(this.position.f_82481_) && Double.isFinite(this.normal.m_82556_()) && Math.abs(this.normal.m_82556_() - 1.0) < 0.01 && Float.isFinite(this.size) && this.size >= 0.35f && this.size <= 1.5f && Float.isFinite(this.strength) && this.strength > 0.0f && this.strength <= 1.0f && this.color >= 0 && this.color <= 0xFFFFFF;
    }

    private static boolean validCoordinate(double value) {
        return Double.isFinite(value) && Math.abs(value) <= 3.0000064E7;
    }

    public static void encode(BossImpactPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130085_(packet.dimension);
        buffer.writeDouble(packet.position.f_82479_).writeDouble(packet.position.f_82480_).writeDouble(packet.position.f_82481_);
        buffer.writeFloat((float)packet.normal.f_82479_).writeFloat((float)packet.normal.f_82480_).writeFloat((float)packet.normal.f_82481_);
        buffer.writeFloat(packet.size).writeFloat(packet.strength).writeInt(packet.color).writeBoolean(packet.shield);
    }

    public static BossImpactPacket decode(FriendlyByteBuf buffer) {
        return new BossImpactPacket(buffer.m_130281_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), new Vec3((double)buffer.readFloat(), (double)buffer.readFloat(), (double)buffer.readFloat()), buffer.readFloat(), buffer.readFloat(), buffer.readInt(), buffer.readBoolean());
    }

    public static void handle(BossImpactPacket packet, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT && packet.valid()) {
            BossImpactVisual.receive(packet);
        }
        context.get().setPacketHandled(true);
    }
}

