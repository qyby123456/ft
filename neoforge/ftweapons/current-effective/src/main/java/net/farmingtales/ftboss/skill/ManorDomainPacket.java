package net.farmingtales.ftboss.skill;

import java.util.UUID;
import net.farmingtales.ftboss.client.render.ManorDomainVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

@EventBusSubscriber(modid = "ftboss", bus = EventBusSubscriber.Bus.MOD)
public record ManorDomainPacket(ResourceLocation dimension, UUID owner, int ownerId, Vec3 center, int variant, boolean supported) implements CustomPacketPayload {
    public static final int PACKET_ID = 9;
    public static final Type<ManorDomainPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "manor_domain"));
    public static final StreamCodec<FriendlyByteBuf, ManorDomainPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), ManorDomainPacket::decode);
    private static boolean registered;

    @Override
    public Type<ManorDomainPacket> type() {
        return TYPE;
    }

    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        if (!registered) {
            registered = true;
            event.registrar("1").playToClient(TYPE, STREAM_CODEC, ManorDomainPacket::handle);
        }
    }

    public static void encode(ManorDomainPacket var0, FriendlyByteBuf var1) {
        var1.writeResourceLocation(var0.dimension);
        var1.writeUUID(var0.owner);
        var1.writeVarInt(var0.ownerId);
        var1.writeDouble(var0.center.x).writeDouble(var0.center.y).writeDouble(var0.center.z);
        var1.writeVarInt(var0.variant);
        var1.writeBoolean(var0.supported);
    }

    public static ManorDomainPacket decode(FriendlyByteBuf var0) {
        return new ManorDomainPacket(
            var0.readResourceLocation(),
            var0.readUUID(),
            var0.readVarInt(),
            new Vec3(var0.readDouble(), var0.readDouble(), var0.readDouble()),
            var0.readVarInt(),
            var0.readBoolean()
        );
    }

    public static void handle(ManorDomainPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND && var0.variant >= -1 && var0.variant <= 11 && Double.isFinite(var0.center.x) && Double.isFinite(var0.center.y) && Double.isFinite(var0.center.z)) {
            var1.enqueueWork(() -> ManorDomainVisual.accept(var0));
        }

    }
}
