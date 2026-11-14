/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.XuanlanWeaponVisual;
import net.farmingtales.ftboss.skill.XuanlanNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public record XuanlanFxPacket(ResourceLocation dimension, int kind, int entityId, Vec3 from, Vec3 to) {
    public static final int BEAM = 0;
    public static final int EMPOWERED_BEAM = 1;
    public static final int MARK = 2;
    public static final int BURST = 3;
    public static final int SWITCH = 4;
    public static final int HIT = 5;
    public static final int SKY_ASCEND = 20;
    public static final int SKY_WARNING = 21;
    public static final int SKY_IMPACT = 22;
    public static final int SKY_DESCEND = 23;
    public static final int SKY_CANCEL = 24;

    public static void send(ServerLevel level, int kind, int entityId, Vec3 from, Vec3 to) {
        XuanlanFxPacket packet = new XuanlanFxPacket(level.m_46472_().m_135782_(), kind, entityId, from, to);
        for (ServerPlayer viewer : level.m_6907_()) {
            if (!(viewer.m_20238_(from) <= 4096.0) && !(viewer.m_20238_(to) <= 4096.0)) continue;
            XuanlanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), (Object)packet);
        }
    }

    public static void encode(XuanlanFxPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130085_(packet.dimension);
        buffer.writeByte(packet.kind);
        buffer.m_130130_(packet.entityId);
        buffer.writeDouble(packet.from.f_82479_);
        buffer.writeDouble(packet.from.f_82480_);
        buffer.writeDouble(packet.from.f_82481_);
        buffer.writeDouble(packet.to.f_82479_);
        buffer.writeDouble(packet.to.f_82480_);
        buffer.writeDouble(packet.to.f_82481_);
    }

    public static XuanlanFxPacket decode(FriendlyByteBuf buffer) {
        return new XuanlanFxPacket(buffer.m_130281_(), buffer.readUnsignedByte(), buffer.m_130242_(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(XuanlanFxPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            XuanlanWeaponVisual.receive(packet);
        }
    }
}

