package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.client.render.YouziStatueVisual;
import net.farmingtales.ftboss.FTBossMod;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record YouziStatuePacket(int actorId, int kind, int duration, Vec3 origin, Vec3 target) {
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(FTBossMod.MOD_ID, "youzi_statue"), () -> "1", "1"::equals, "1"::equals);
    public static final int ECHO = 0;
    public static final int WARD = 1;
    public static final int BEAM = 2;
    public static final int STOP = 3;
    public static final int BLOCK = 4;

    public static void register() {
        CHANNEL.messageBuilder(YouziStatuePacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(YouziStatuePacket::encode).decoder(YouziStatuePacket::decode)
                .consumerMainThread(YouziStatuePacket::handle).add();
    }

    public static void send(ServerPlayer player, int kind, int duration, Vec3 origin, Vec3 target) {
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player),
                new YouziStatuePacket(player.getId(), kind, duration, origin, target));
    }

    public static void encode(YouziStatuePacket packet, FriendlyByteBuf buffer) {
        buffer.writeVarInt(packet.actorId);
        buffer.writeByte(packet.kind);
        buffer.writeVarInt(packet.duration);
        buffer.writeDouble(packet.origin.x);
        buffer.writeDouble(packet.origin.y);
        buffer.writeDouble(packet.origin.z);
        buffer.writeDouble(packet.target.x);
        buffer.writeDouble(packet.target.y);
        buffer.writeDouble(packet.target.z);
    }

    public static YouziStatuePacket decode(FriendlyByteBuf buffer) {
        return new YouziStatuePacket(buffer.readVarInt(), buffer.readUnsignedByte(), buffer.readVarInt(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()));
    }

    public static void handle(YouziStatuePacket packet, Supplier<NetworkEvent.Context> context) {
        context.get().setPacketHandled(true);
        if (FMLEnvironment.dist == Dist.CLIENT) YouziStatueVisual.receive(packet);
    }
}
