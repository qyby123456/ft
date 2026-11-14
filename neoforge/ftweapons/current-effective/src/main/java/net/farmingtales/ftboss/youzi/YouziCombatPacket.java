package net.farmingtales.ftboss.youzi;

import java.util.UUID;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.farmingtales.ftboss.youzi.client.YouziCombatVisual;
import net.minecraft.network.FriendlyByteBuf;

public record YouziCombatPacket(
    String effect,
    String dimension,
    long sequence,
    long startedTick,
    YouziHomingFlight.Vector origin,
    YouziHomingFlight.Vector target,
    double scale,
    double yaw,
    int actorId,
    UUID actor
) implements CustomPacketPayload {
    public static final Type<YouziCombatPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_combat_fx"));
    public static final StreamCodec<RegistryFriendlyByteBuf, YouziCombatPacket> STREAM_CODEC = StreamCodec.ofMember(YouziCombatPacket::encode, YouziCombatPacket::decode);

    @Override
    public Type<YouziCombatPacket> type() {
        return TYPE;
    }

    public YouziCombatPacket(
        String effect,
        String dimension,
        long sequence,
        long startedTick,
        YouziHomingFlight.Vector origin,
        YouziHomingFlight.Vector target,
        double scale,
        double yaw,
        int actorId,
        UUID actor
    ) {
        if (YouziCombatEvents.validIdentifier(effect)
            && dimension != null
            && dimension.length() <= 256
            && sequence >= 0L
            && startedTick >= 0L
            && origin != null
            && target != null
            && actor != null
            && Double.isFinite(scale)
            && !(scale <= 0.0)
            && !(scale > 4.0)
            && Double.isFinite(yaw)) {
            this.effect = effect;
            this.dimension = dimension;
            this.sequence = sequence;
            this.startedTick = startedTick;
            this.origin = origin;
            this.target = target;
            this.scale = scale;
            this.yaw = yaw;
            this.actorId = actorId;
            this.actor = actor;
        } else {
            throw new IllegalArgumentException("Invalid Youzi combat packet");
        }
    }

    public static void encode(YouziCombatPacket var0, FriendlyByteBuf var1) {
        var1.writeUtf(var0.effect, 32);
        var1.writeUtf(var0.dimension, 256);
        var1.writeLong(var0.sequence);
        var1.writeLong(var0.startedTick);

        for (YouziHomingFlight.Vector var5 : new YouziHomingFlight.Vector[]{var0.origin, var0.target}) {
            var1.writeDouble(var5.coordinateX());
            var1.writeDouble(var5.coordinateY());
            var1.writeDouble(var5.coordinateZ());
        }

        var1.writeDouble(var0.scale);
        var1.writeDouble(var0.yaw);
        var1.writeVarInt(var0.actorId);
        var1.writeLong(var0.actor.getMostSignificantBits());
        var1.writeLong(var0.actor.getLeastSignificantBits());
    }

    public static YouziCombatPacket decode(FriendlyByteBuf var0) {
        return new YouziCombatPacket(
            var0.readUtf(32),
            var0.readUtf(256),
            var0.readLong(),
            var0.readLong(),
            new YouziHomingFlight.Vector(var0.readDouble(), var0.readDouble(), var0.readDouble()),
            new YouziHomingFlight.Vector(var0.readDouble(), var0.readDouble(), var0.readDouble()),
            var0.readDouble(),
            var0.readDouble(),
            var0.readVarInt(),
            new UUID(var0.readLong(), var0.readLong())
        );
    }

    public static void handle(YouziCombatPacket packet, IPayloadContext context) {
        if (context.flow() == PacketFlow.CLIENTBOUND) {
            context.enqueueWork(() -> ClientOnly.accept(packet));
        }
    }

    private static final class ClientOnly {
        private static void accept(YouziCombatPacket packet) {
            YouziCombatVisual.accept(packet);
        }
    }
}
