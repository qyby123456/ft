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
import net.farmingtales.ftboss.client.render.ManorIntroVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;

public record ManorIntroPacket(UUID id, ResourceLocation dimension, Vec3 position, long elapsedMillis, boolean active) {
    public static void encode(ManorIntroPacket message, FriendlyByteBuf buffer) {
        buffer.m_130077_(message.id);
        buffer.m_130085_(message.dimension);
        buffer.writeDouble(message.position.f_82479_);
        buffer.writeDouble(message.position.f_82480_);
        buffer.writeDouble(message.position.f_82481_);
        buffer.m_130103_(message.elapsedMillis);
        buffer.writeBoolean(message.active);
    }

    public static ManorIntroPacket decode(FriendlyByteBuf buffer) {
        return new ManorIntroPacket(buffer.m_130259_(), buffer.m_130281_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.m_130258_(), buffer.readBoolean());
    }

    public static void handle(ManorIntroPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            ManorIntroVisual.receive(message);
        }
    }
}

