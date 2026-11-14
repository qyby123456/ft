package net.farmingtales.ftboss.dandaniron;

import java.util.Set;
import net.farmingtales.ftboss.dandaniron.client.IronRenderer;
import net.farmingtales.ftboss.skill.SpellVisualPacket;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.PacketFlow;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record IronPacket(SpellVisualPacket visual, long cast, long start, long event, int ticks, float scale, boolean cancelled) implements CustomPacketPayload {
    public static final Type<IronPacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("ftboss", "dandan_iron_timeline"));
    public static final StreamCodec<FriendlyByteBuf, IronPacket> STREAM_CODEC = StreamCodec.of((buffer, packet) -> encode(packet, buffer), IronPacket::decode);

    @Override
    public Type<IronPacket> type() {
        return TYPE;
    }

    public static boolean matches(String var0) {
        return Set.of("candle_reap", "candle_prison", "candle_eclipse").contains(var0);
    }

    public boolean valid() {
        return this.visual != null
            && this.visual.valid()
            && matches(this.visual.spell().id)
            && this.cast >= 0L
            && this.start >= 0L
            && this.event >= this.start
            && this.event - this.start <= 1200L
            && this.ticks > 0
            && this.ticks <= 1200
            && Float.isFinite(this.scale)
            && this.scale >= 0.25F
            && this.scale <= 8.0F;
    }

    public static void encode(IronPacket var0, FriendlyByteBuf var1) {
        net.farmingtales.ftboss.network.NativeSpellVisualWire.encode(var0.visual(), var1);
        var1.writeLong(var0.cast())
            .writeLong(var0.start())
            .writeLong(var0.event())
            .writeInt(var0.ticks())
            .writeFloat(var0.scale())
            .writeBoolean(var0.cancelled());
    }

    public static IronPacket decode(FriendlyByteBuf var0) {
        return new IronPacket(
            net.farmingtales.ftboss.network.NativeSpellVisualWire.decode(var0), var0.readLong(), var0.readLong(), var0.readLong(), var0.readInt(), var0.readFloat(), var0.readBoolean()
        );
    }

    public static void handle(IronPacket var0, IPayloadContext var1) {
        if (var1.flow() == PacketFlow.CLIENTBOUND && var0.valid()) {
            var1.enqueueWork(() -> IronRenderer.receive(var0));
        }

    }
}
