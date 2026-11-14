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
import net.farmingtales.ftboss.client.render.DoraGunFxVisual;
import net.farmingtales.ftboss.client.render.PufferBatFxVisual;
import net.farmingtales.ftboss.client.render.WeaponFxVisual;
import net.farmingtales.ftboss.util.WeaponFxRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record WeaponFxPacket(ResourceLocation dimension, WeaponFxRules.Kind kind, WeaponFxRules.Theme theme, Vec3 start, Vec3 end, float radius, float yaw) {
    public boolean valid() {
        return this.dimension != null && this.kind != null && this.theme != null && this.start != null && this.end != null && WeaponFxRules.validPosition(this.start.f_82479_) && WeaponFxRules.validPosition(this.start.f_82480_) && WeaponFxRules.validPosition(this.start.f_82481_) && WeaponFxRules.validPosition(this.end.f_82479_) && WeaponFxRules.validPosition(this.end.f_82480_) && WeaponFxRules.validPosition(this.end.f_82481_) && WeaponFxRules.validSize(this.radius, this.yaw, this.start.m_82557_(this.end));
    }

    public static void encode(WeaponFxPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130085_(packet.dimension);
        buffer.m_130068_((Enum)packet.kind);
        buffer.m_130068_((Enum)packet.theme);
        buffer.writeDouble(packet.start.f_82479_).writeDouble(packet.start.f_82480_).writeDouble(packet.start.f_82481_);
        buffer.writeDouble(packet.end.f_82479_).writeDouble(packet.end.f_82480_).writeDouble(packet.end.f_82481_);
        buffer.writeFloat(packet.radius).writeFloat(packet.yaw);
    }

    public static WeaponFxPacket decode(FriendlyByteBuf buffer) {
        return new WeaponFxPacket(buffer.m_130281_(), (WeaponFxRules.Kind)buffer.m_130066_(WeaponFxRules.Kind.class), (WeaponFxRules.Theme)buffer.m_130066_(WeaponFxRules.Theme.class), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat(), buffer.readFloat());
    }

    public static void handle(WeaponFxPacket packet, Supplier<NetworkEvent.Context> supplier) {
        if (FMLEnvironment.dist == Dist.CLIENT && packet.valid()) {
            if (packet.kind().doraGun()) {
                DoraGunFxVisual.receive(packet);
            } else if (packet.kind().pufferBat()) {
                PufferBatFxVisual.receive(packet);
            } else {
                WeaponFxVisual.receive(packet);
            }
        }
        supplier.get().setPacketHandled(true);
    }
}

