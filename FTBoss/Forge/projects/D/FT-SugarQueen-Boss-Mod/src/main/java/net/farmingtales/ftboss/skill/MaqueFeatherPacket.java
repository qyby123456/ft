package net.farmingtales.ftboss.skill;

import net.farmingtales.ftboss.client.render.MaqueFeatherVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.function.Supplier;

public record MaqueFeatherPacket(ResourceLocation dimension, int casterId, Vec3 position, boolean wind) {
    private static final String PROTOCOL = "1";
    private static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation("ftboss", "maque_feather"), () -> PROTOCOL,
            NetworkRegistry.acceptMissingOr(PROTOCOL), NetworkRegistry.acceptMissingOr(PROTOCOL));
    private static boolean registered;

    public static void register() {
        if (registered) return;
        registered = true;
        CHANNEL.messageBuilder(MaqueFeatherPacket.class, 0, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(MaqueFeatherPacket::encode).decoder(MaqueFeatherPacket::decode)
                .consumerMainThread(MaqueFeatherPacket::handle).add();
    }

    public static void encode(MaqueFeatherPacket message, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(message.dimension);
        buffer.writeVarInt(message.casterId);
        buffer.writeDouble(message.position.x);
        buffer.writeDouble(message.position.y);
        buffer.writeDouble(message.position.z);
        buffer.writeBoolean(message.wind);
    }

    public static MaqueFeatherPacket decode(FriendlyByteBuf buffer) {
        return new MaqueFeatherPacket(buffer.readResourceLocation(), buffer.readVarInt(),
                new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()), buffer.readBoolean());
    }

    public static void handle(MaqueFeatherPacket message, Supplier<NetworkEvent.Context> context) {
        if (FMLEnvironment.dist == Dist.CLIENT) MaqueFeatherVisual.receive(message);
        context.get().setPacketHandled(true);
    }

    public static void send(ServerPlayer caster, boolean wind) {
        MaqueFeatherPacket message = new MaqueFeatherPacket(caster.level().dimension().location(),
                caster.getId(), caster.position(), wind);
        for (ServerPlayer viewer : caster.serverLevel().players()) {
            if (viewer.distanceToSqr(caster) <= 48.0 * 48.0 && CHANNEL.isRemotePresent(viewer.connection.connection)) {
                CHANNEL.send(PacketDistributor.PLAYER.with(() -> viewer), message);
            }
        }
    }
}
