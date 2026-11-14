/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.DandanScytheVisual;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public record DandanScytheFxPacket(ResourceLocation dimension, Vec3 position, float yaw, float radius, int kind, int targetId) {
    public static final int SWEEP = 0;
    public static final int BLINK = 1;
    public static final int MARK = 2;
    public static final int BURST = 3;

    public static void encode(DandanScytheFxPacket message, FriendlyByteBuf buffer) {
        buffer.m_130085_(message.dimension);
        buffer.writeDouble(message.position.f_82479_);
        buffer.writeDouble(message.position.f_82480_);
        buffer.writeDouble(message.position.f_82481_);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.radius);
        buffer.writeByte(message.kind);
        buffer.writeInt(message.targetId);
    }

    public static DandanScytheFxPacket decode(FriendlyByteBuf buffer) {
        return new DandanScytheFxPacket(buffer.m_130281_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readFloat(), buffer.readFloat(), buffer.readUnsignedByte(), buffer.readInt());
    }

    public static void handle(DandanScytheFxPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT && message.kind >= 0 && message.kind <= 5 && Float.isFinite(message.yaw) && Float.isFinite(message.radius) && message.radius > 0.0f && message.radius <= 6.0f && Double.isFinite(message.position.f_82479_) && Double.isFinite(message.position.f_82480_) && Double.isFinite(message.position.f_82481_)) {
            DandanScytheVisual.receive(message);
        }
    }

    public static void send(ServerPlayer source, Vec3 position, float yaw, float radius, int kind, int targetId) {
        DandanScytheFxPacket message = new DandanScytheFxPacket(source.m_9236_().m_46472_().m_135782_(), position, yaw, radius, kind, targetId);
        for (ServerPlayer viewer : source.m_284548_().m_6907_()) {
            if (!(viewer.m_20238_(position) <= 4096.0) || !FTNetwork.CHANNEL.isRemotePresent(viewer.f_8906_.f_9742_)) continue;
            FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)message);
        }
    }
}

