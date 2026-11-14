package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.client.render.DandanScytheVisual;
import net.farmingtales.ftboss.util.DandanScytheMotionRules;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

public record DandanScytheFxPacket(ResourceLocation dimension, Vec3 position, float yaw, float radius, int kind, int targetId) {
    public static final int SWEEP = 0;
    public static final int BLINK = 1;
    public static final int MARK = 2;
    public static final int BURST = 3;

    public static void encode(DandanScytheFxPacket message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.dimension);
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeFloat(message.yaw);
        buffer.writeFloat(message.radius);
        buffer.writeByte(message.kind);
        buffer.writeInt(message.targetId);
    }

    public static DandanScytheFxPacket decode(FriendlyByteBuf buffer) {
        return new DandanScytheFxPacket(buffer.readResourceLocation(), new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                buffer.readFloat(), buffer.readFloat(), buffer.readUnsignedByte(), buffer.readInt());
    }

    public static void handle(DandanScytheFxPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT && message.kind >= SWEEP && message.kind <= DandanScytheMotionRules.BLINK_ACTION
                && Float.isFinite(message.yaw) && Float.isFinite(message.radius) && message.radius > 0 && message.radius <= 6
                && Double.isFinite(message.position.x) && Double.isFinite(message.position.y) && Double.isFinite(message.position.z)) {
            DandanScytheVisual.receive(message);
        }
    }

    public static void send(ServerPlayer source, Vec3 position, float yaw, float radius, int kind, int targetId) {
        DandanScytheFxPacket message = new DandanScytheFxPacket(source.level().dimension().location(), position, yaw, radius, kind, targetId);
        for (ServerPlayer viewer : source.serverLevel().players()) {
            if (viewer.distanceToSqr(position) <= 64 * 64 && FTNetwork.CHANNEL.isRemotePresent(viewer.connection.connection)) {
                FTNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), message);
            }
        }
    }
}
