package net.farmingtales.ftboss.youzi.spells;

import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.farmingtales.ftboss.youzi.motion.YouziBodyState;
import net.farmingtales.ftboss.youzi.spells.client.YouziSpellVisual;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record YouziSpellPacket(SpellVisualPacket visual, long sentTick, long castStarted, int castDuration, double scale) implements CustomPacketPayload {
    public static final Type<YouziSpellPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "youzi_spell_runtime"));
    public static final StreamCodec<FriendlyByteBuf, YouziSpellPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), YouziSpellPacket::decode);

    @Override
    public Type<YouziSpellPacket> type() {
        return TYPE;
    }

    public YouziSpellPacket(SpellVisualPacket visual, long sentTick, long castStarted, int castDuration, double scale) {
        if (visual != null
            && visual.valid()
            && sentTick >= 0L
            && castStarted >= 0L
            && castDuration >= 0
            && castDuration <= 12000
            && Double.isFinite(scale)
            && !(scale <= 0.0)
            && !(scale > 4.0)) {
            this.visual = visual;
            this.sentTick = sentTick;
            this.castStarted = castStarted;
            this.castDuration = castDuration;
            this.scale = scale;
        } else {
            throw new IllegalArgumentException("Invalid authoritative Youzi visual event");
        }
    }

    public static void encode(YouziSpellPacket var0, FriendlyByteBuf var1) {
        net.farmingtales.ftboss.network.NativeSpellVisualWire.encode(var0.visual(), var1);
        var1.writeLong(var0.sentTick());
        var1.writeLong(var0.castStarted());
        var1.writeVarInt(var0.castDuration());
        var1.writeDouble(var0.scale());
    }

    public static YouziSpellPacket decode(FriendlyByteBuf var0) {
        return new YouziSpellPacket(net.farmingtales.ftboss.network.NativeSpellVisualWire.decode(var0), var0.readLong(), var0.readLong(), var0.readVarInt(), var0.readDouble());
    }

    public static void handle(YouziSpellPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND) {
            var1.enqueueWork(() -> {
                        YouziSpellVisual.acceptAuthoritative(var0);
                        YouziBodyState.spell(var0);
                    });
        }

    }
}
