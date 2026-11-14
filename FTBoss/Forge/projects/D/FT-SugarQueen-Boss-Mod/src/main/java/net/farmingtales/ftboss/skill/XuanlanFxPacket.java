package net.farmingtales.ftboss.skill;

import java.util.function.Supplier;
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
        XuanlanFxPacket packet = new XuanlanFxPacket(level.dimension().location(), kind, entityId, from, to);
        for (ServerPlayer viewer : level.players()) {
            if (viewer.distanceToSqr(from) <= 64 * 64 || viewer.distanceToSqr(to) <= 64 * 64) {
                XuanlanNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), packet);
            }
        }
    }

    public static void encode(XuanlanFxPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeByte(packet.kind);
        buffer.writeVarInt(packet.entityId);
        buffer.writeDouble(packet.from.x);
        buffer.writeDouble(packet.from.y);
        buffer.writeDouble(packet.from.z);
        buffer.writeDouble(packet.to.x);
        buffer.writeDouble(packet.to.y);
        buffer.writeDouble(packet.to.z);
    }

    public static XuanlanFxPacket decode(FriendlyByteBuf buffer) {
        return new XuanlanFxPacket(buffer.readResourceLocation(), buffer.readUnsignedByte(), buffer.readVarInt(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(XuanlanFxPacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        if (FMLEnvironment.dist == Dist.CLIENT) net.farmingtales.ftboss.client.render.XuanlanWeaponVisual.receive(packet);
    }
}
