/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.loading.FMLEnvironment
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ManorRecallBoundaryVisual;
import net.farmingtales.ftboss.skill.FTNetwork;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public record ManorRecallBoundaryPacket(UUID fightId, ResourceLocation dimension, int bossEntityId, UUID bossId, float radius, float disengageWarning, boolean active) {
    private static final UUID EMPTY = new UUID(0L, 0L);

    public static ManorRecallBoundaryPacket stop(UUID fightId, ResourceLocation dimension) {
        return new ManorRecallBoundaryPacket(fightId, dimension, -1, EMPTY, 76.0f, 0.0f, false);
    }

    public boolean valid() {
        if (this.fightId == null || this.dimension == null || !Float.isFinite(this.radius) || this.radius < 2.0f || this.radius > 128.0f || !Float.isFinite(this.disengageWarning) || this.disengageWarning < 0.0f || this.disengageWarning > 1.0f) {
            return false;
        }
        return !this.active || this.bossEntityId >= 0 && this.bossId != null && !EMPTY.equals(this.bossId);
    }

    public static void encode(ManorRecallBoundaryPacket packet, FriendlyByteBuf buffer) {
        buffer.m_130077_(packet.fightId);
        buffer.m_130085_(packet.dimension);
        buffer.m_130130_(packet.bossEntityId + 1);
        buffer.m_130077_(packet.bossId == null ? EMPTY : packet.bossId);
        buffer.writeFloat(packet.radius);
        buffer.writeFloat(packet.disengageWarning);
        buffer.writeBoolean(packet.active);
    }

    public static ManorRecallBoundaryPacket decode(FriendlyByteBuf buffer) {
        return new ManorRecallBoundaryPacket(buffer.m_130259_(), buffer.m_130281_(), buffer.m_130242_() - 1, buffer.m_130259_(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(ManorRecallBoundaryPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT) {
            ManorRecallBoundaryVisual.receive(packet);
        }
    }

    public static void send(ServerPlayer player, ManorRecallBoundaryPacket packet) {
        if (player == null || packet == null || !packet.valid()) {
            return;
        }
        FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)packet);
    }
}

