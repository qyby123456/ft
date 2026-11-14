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

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ManorTransitionVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record ManorTransitionPacket(UUID id, ResourceLocation dimension, Vec3 center, int variant, int age, boolean active) {
    public boolean valid() {
        return this.id != null && this.dimension != null && this.center != null && Double.isFinite(this.center.f_82479_ + this.center.f_82480_ + this.center.f_82481_) && Math.abs(this.center.f_82479_) <= 3.0E7 && Math.abs(this.center.f_82480_) <= 3.0E7 && Math.abs(this.center.f_82481_) <= 3.0E7 && this.variant >= -1 && this.variant < 12 && this.variant != 9 && this.age >= 0 && this.age < 90;
    }

    public static void encode(ManorTransitionPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130077_(packet.id);
        buffer.m_130085_(packet.dimension);
        buffer.writeDouble(packet.center.f_82479_);
        buffer.writeDouble(packet.center.f_82480_);
        buffer.writeDouble(packet.center.f_82481_);
        buffer.writeInt(packet.variant);
        buffer.m_130130_(packet.age);
        buffer.writeBoolean(packet.active);
    }

    public static ManorTransitionPacket decode(FriendlyByteBuf buffer) {
        return new ManorTransitionPacket(buffer.m_130259_(), buffer.m_130281_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readInt(), buffer.m_130242_(), buffer.readBoolean());
    }

    public static void handle(ManorTransitionPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT) {
            ManorTransitionVisual.receive(packet);
        }
    }
}

