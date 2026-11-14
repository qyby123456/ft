package net.farmingtales.ftboss.skill;

import java.util.UUID;
import java.util.function.Supplier;
import net.farmingtales.ftboss.client.render.ManorRecallBoundaryVisual;
import net.farmingtales.ftboss.util.ManorRecallBoundaryGeometry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

/** Keeps each participant's client attached to the current moving Manor recall boundary. */
public record ManorRecallBoundaryPacket(UUID fightId, ResourceLocation dimension, int bossEntityId,
                                        UUID bossId, float radius, float disengageWarning, boolean active) {
    private static final UUID EMPTY = new UUID(0, 0);

    public static ManorRecallBoundaryPacket stop(UUID fightId, ResourceLocation dimension) {
        return new ManorRecallBoundaryPacket(fightId, dimension, -1, EMPTY,
                (float) ManorRecallBoundaryGeometry.RADIUS, 0, false);
    }

    public boolean valid() {
        if (fightId == null || dimension == null || !Float.isFinite(radius) || radius < 2 || radius > 128
                || !Float.isFinite(disengageWarning) || disengageWarning < 0 || disengageWarning > 1) return false;
        return !active || bossEntityId >= 0 && bossId != null && !EMPTY.equals(bossId);
    }

    public static void encode(ManorRecallBoundaryPacket packet, FriendlyByteBuf buffer) {
        buffer.writeUUID(packet.fightId);
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeVarInt(packet.bossEntityId + 1);
        buffer.writeUUID(packet.bossId == null ? EMPTY : packet.bossId);
        buffer.writeFloat(packet.radius);
        buffer.writeFloat(packet.disengageWarning);
        buffer.writeBoolean(packet.active);
    }

    public static ManorRecallBoundaryPacket decode(FriendlyByteBuf buffer) {
        return new ManorRecallBoundaryPacket(buffer.readUUID(), buffer.readResourceLocation(),
                buffer.readVarInt() - 1, buffer.readUUID(), buffer.readFloat(), buffer.readFloat(), buffer.readBoolean());
    }

    public static void handle(ManorRecallBoundaryPacket packet, Supplier<NetworkEvent.Context> context) {
        if (packet.valid() && FMLEnvironment.dist == Dist.CLIENT) ManorRecallBoundaryVisual.receive(packet);
    }

    public static void send(ServerPlayer player, ManorRecallBoundaryPacket packet) {
        if (player == null || packet == null || !packet.valid()) return;
        FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }
}
