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
import net.farmingtales.ftboss.client.render.LemonCatSkillVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record LemonCatFxPacket(ResourceLocation dimension, Vec3 position, float yaw, int kind) {
    public static void encode(LemonCatFxPacket message, FriendlyByteBuf buffer) {
        buffer.m_130085_(message.dimension);
        buffer.writeDouble(message.position.f_82479_);
        buffer.writeDouble(message.position.f_82480_);
        buffer.writeDouble(message.position.f_82481_);
        buffer.writeFloat(message.yaw);
        buffer.writeByte(message.kind);
    }

    public static LemonCatFxPacket decode(FriendlyByteBuf buffer) {
        return new LemonCatFxPacket(buffer.m_130281_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat(), buffer.readUnsignedByte());
    }

    public static void handle(LemonCatFxPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            LemonCatSkillVisual.receive(message);
        }
    }
}

