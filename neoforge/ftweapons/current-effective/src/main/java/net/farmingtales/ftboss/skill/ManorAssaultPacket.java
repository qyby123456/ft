package net.farmingtales.ftboss.skill;

import java.util.UUID;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.farmingtales.ftboss.client.render.ManorAssaultVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public record ManorAssaultPacket(ResourceLocation dimension, UUID owner, int ownerId, Vec3 center, int variant, boolean release) implements CustomPacketPayload {
    public static final int PACKET_ID = 10;

    public static final Type<ManorAssaultPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "manor_assault"));
    public static final StreamCodec<FriendlyByteBuf, ManorAssaultPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), ManorAssaultPacket::decode);

    @Override
    public Type<ManorAssaultPacket> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").optional().playToClient(TYPE, STREAM_CODEC, ManorAssaultPacket::handle);
    }

    public static void encode(ManorAssaultPacket packet, FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(packet.dimension);
        buffer.writeUUID(packet.owner);
        buffer.writeVarInt(packet.ownerId);
        buffer.writeDouble(packet.center.x).writeDouble(packet.center.y).writeDouble(packet.center.z);
        buffer.writeVarInt(packet.variant);
        buffer.writeBoolean(packet.release);
    }

    public static ManorAssaultPacket decode(FriendlyByteBuf buffer) {
        return new ManorAssaultPacket(
            buffer.readResourceLocation(),
            buffer.readUUID(),
            buffer.readVarInt(),
            new Vec3(buffer.readDouble(), buffer.readDouble(), buffer.readDouble()),
            buffer.readVarInt(),
            buffer.readBoolean()
        );
    }

    public static void handle(ManorAssaultPacket packet, IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND
            && ManorAssaultPacket.ManorAssaultGeometrySafe.supports(packet.variant)
            && Double.isFinite(packet.center.x)
            && Double.isFinite(packet.center.y)
            && Double.isFinite(packet.center.z)) {
            context.enqueueWork(() -> ManorAssaultVisual.accept(packet));
        }

    }

    private static final class ManorAssaultGeometrySafe {
        static boolean supports(int variant) {
            return variant >= 0 && variant <= 11 && variant != 9;
        }
    }
}
